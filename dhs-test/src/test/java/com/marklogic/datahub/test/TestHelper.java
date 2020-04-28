package com.marklogic.datahub.test;

import java.io.File;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.eval.ServerEvaluationCall;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.hub.ApplicationConfig;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.impl.DataHubImpl;
import com.marklogic.hub.impl.HubConfigImpl;

import org.apache.commons.lang3.StringUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestHelper {

  public String projectDir = new File("").getAbsolutePath();
  public String environmentName = System.getProperty("environmentName");

  protected HubConfigImpl hubConfig;
  protected DataHubImpl dataHub;
  protected String[] allRoles = {"pii-user", "data-hub-developer", "data-hub-operator"};

  protected String username;
  protected String password;

  protected AbstractReadHandle runInDatabase(String query, String databaseName,
      AbstractReadHandle handle) {
    try {
      return getServerEval(databaseName).javascript(query).eval(handle);
    } catch (FailedRequestException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getCause());
    }
  }

  protected BuildResult runTask(String... task) {
    return GradleRunner.create()
        .withProjectDir(new File(projectDir))
        .withArguments(task)
        .withDebug(true)
        .build();
  }

  private ServerEvaluationCall getServerEval(String databaseName) {
    return getClientByName(databaseName).newServerEval();
  }

  protected DatabaseClient getClientByName(String databaseName) {
    switch (databaseName) {
      case HubConfig.DEFAULT_FINAL_NAME:
        return hubConfig.newFinalClient();
      default:
        return hubConfig.newStagingClient();
    }
  }

  protected void configureHubConfig(String username, String password) {
    if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      username = "admin";
      password = "admin";
    } else {
      hubConfig.setMlUsername(username);
      hubConfig.setMlUsername(password);
    }
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(ApplicationConfig.class);
    ctx.refresh();
    hubConfig = ctx.getBean(HubConfigImpl.class);
    hubConfig.createProject(projectDir);
    hubConfig.withPropertiesFromEnvironment(environmentName);
    dataHub = ctx.getBean(DataHubImpl.class);
    hubConfig.setMlUsername(username);
    hubConfig.setMlPassword(password);
    hubConfig.refreshProject();
    hubConfig.hydrateConfigs();
    System.out.println(hubConfig.getMlUsername());
    System.out.println(hubConfig.getMlPassword());
  }
}
