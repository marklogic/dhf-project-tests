package com.marklogic.datahub.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.hub.flow.Flow;
import com.marklogic.hub.flow.RunFlowResponse;
import com.marklogic.hub.impl.FlowManagerImpl;
import com.marklogic.hub.step.StepDefinition.StepDefinitionType;
import com.marklogic.hub.step.impl.Step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class FlowEndToEndTests extends TestHelper {

  private String role;

  @BeforeAll
  public void setUp() {
    configureHubConfig(username, password);
  }

  @BeforeEach
  public void setUpTest() {
    runTask(":hubDeployUserModules");
  }

  @TestFactory
  public List<DynamicTest> generateRunFlowTests() {
    List<DynamicTest> tests = new ArrayList<>();
    File folder = new File("flows");

    Arrays.stream(allRoles).forEach((role) -> Arrays.stream(folder.listFiles()).forEach((file) -> {
      if (file.isFile()) {
        String flowName = file.getName().split("\\.")[0];
        tests.add(DynamicTest.dynamicTest(file.getName() + "-flow", () -> {
          this.role = role;
          if (role.equals("data-hub-operator")) {
            username = "dh-op";
            password = "dh-op";
          } else if (role.equals("pii-user")) {
            username = "pii-user";
            password = "pii-user";
          } else {
            username = "dh-dev";
            password = "dh-dev";
          }
          configureHubConfig(username, password);
          BuildResult runFlowResult = runFlow(flowName);
          assertFlow(runFlowResult);
        }));
      }
    }));
    return tests;
  }

  private BuildResult runFlow(String flowName) {
    return runTask(":hubRunFlow", "-PflowName=" + flowName, "-PenvironmentName=" + environmentName);
  }

  private BuildResult runFlow(String flowName, String stepNum) {
    return runTask(":hubRunFlow", "-PflowName=" + flowName, "-Psteps=" + stepNum,
        "-PenvironmentName=" + environmentName);
  }

  private void assertFlow(BuildResult runFlowResult) {
    assertTrue(runFlowResult.task(":hubRunFlow").getOutcome().toString().equals("SUCCESS"));

    String taskOutput = runFlowResult.getOutput();
    RunFlowResponse flowResponse = parseFlowResult(taskOutput);

    assertNotNull(flowResponse.getJobId());
    assertTrue(flowResponse.getLastCompletedStep()
        .equals(Integer.toString(flowResponse.getStepResponses().size())));

    flowResponse.getStepResponses().forEach((stepNumber, stepResponse) -> {
      assertTrue(stepResponse.isSuccess());
      assertNull(stepResponse.stepOutput);
      assertTrue(stepResponse.getSuccessfulEvents() > 0);
      // successful events = total events
      assertTrue(stepResponse.getSuccessfulEvents() == stepResponse.getTotalEvents());
      if (stepResponse.getStepDefinitionType().equals(StepDefinitionType.MAPPING.toString())
          || stepResponse.getStepDefinitionType().equals(StepDefinitionType.MERGING.toString())
          || stepResponse.getStepDefinitionType().equals(StepDefinitionType.MASTERING.toString())) {
        assertData(stepResponse.getFlowName(), stepNumber);
      }
    });
  }

  private void assertData(String flowName, String stepNumber) {
    FlowManagerImpl runner = new FlowManagerImpl();
    runner.setHubConfig(hubConfig);
    System.out.println("Inside assertData: " + hubConfig.getMlUsername());
    Flow flow = runner.getFlow(flowName);
    Step step = flow.getStep(stepNumber);
    ArrayNode collections = (ArrayNode) step.getOptions().get("collections");
    System.out.println(collections);
    System.out.println(collections.get(0).asText());
    String query =
        "cts.uris(\"\", null, cts.collectionQuery(\"" + collections.get(0).asText() + "\"))";
    AbstractReadHandle res = runInDatabase(query, "data-hub-FINAL", new StringHandle());
    String uri = ((StringHandle) res).get();
    System.out.println(uri);
    query = "fn.head(fn.doc(\"" + uri + "\")).xpath(\"//*:instance/Concept/concept_id\")";
//    query = "fn.head(fn.doc(\"" + uri + "\")).xpath(\"//concept_id\")";
    StringHandle searchResults = (StringHandle) runInDatabase(query, "data-hub-FINAL",
        new StringHandle());
    if (role.equals("pii-user")) {
      assertNotNull(searchResults);
    } else {
      assertNull(searchResults);
    }
  }

  private RunFlowResponse parseFlowResult(String taskOutput) {
    ObjectMapper mapper = new ObjectMapper();

    int jsonStartIndex = taskOutput.indexOf('{');
    int jsonEndIndex = taskOutput.lastIndexOf('}');
    String jsonString = taskOutput.substring(jsonStartIndex, jsonEndIndex + 1);

    try {
      System.out.println(jsonString);
      return mapper.readValue(jsonString, RunFlowResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
