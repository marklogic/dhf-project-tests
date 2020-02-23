package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataHubAdminTest extends BaseTest {

    private String username = "-PmlUsername=dh-admin";
    private String password = "-PmlPassword=dh-admin";

    @Test
    public void installDHTest() {
        BuildResult result = runFailedTask(":mlDeploy", username, password);
        assertTrue(result.getOutput().contains("\"statusCode\":\"403\", \"status\":\"Forbidden\""), result.getOutput());
    }

    @Test
    public void task7ClearStagingDBTest() {
        BuildResult result = runTask(":mlClearDatabase", username, password, "-Pdatabase=data-hub-STAGING", "-Pconfirm=true");
        loadModules();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task8ClearFinalDBTest() {
        BuildResult result = runTask(":mlClearDatabase", username, password, "-Pdatabase=data-hub-FINAL", "-Pconfirm=true");
        loadModules();
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task9ClearJobsDBTest() {
        BuildResult result = runTask(":mlClearDatabase", username, password, "-Pdatabase=data-hub-JOBS", "-Pconfirm=true");
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }
}
