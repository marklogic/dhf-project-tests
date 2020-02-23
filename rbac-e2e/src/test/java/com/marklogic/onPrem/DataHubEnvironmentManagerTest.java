package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataHubEnvironmentManagerTest extends BaseTest {


    private String username = "-PmlUsername=dh-env-manager";
    private String password = "-PmlPassword=dh-env-manager";
    private String configDir;

    @BeforeEach
    public void setup() {
        configDir = null;
    }

    @AfterEach
    public void teardown() {
        if(configDir!=null)
            removeDirectoriesFromProject(configDir);
    }

    @Test
    @Order(1)
    public void task1InstallDHAsEnvManager() {
        BuildResult result = runTask(":mlDeploy", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
        assertEquals(result.task(":mlDeploy").getOutcome(), SUCCESS);
    }

    @Test
    public void task2ConfigDBBackupAsEnvManager() {
        configDir = "databases/final-backup-config.json";
        copyResourcesIntoConfigDir("ml-config/databases/final-backup-config.json", "databases");
        BuildResult result = runTask(":mlDeployDatabases", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    public void hubUpdateAsEnvManager() {
        configDir = "../hub-internal-config-5.2-SNAPSHOT";
        BuildResult result = runTask(":hubUpdate", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    @Order(2)
    public void createGroupTest() {
        configDir = "groups";
        copyDirectoriesIntoConfigDir("ml-config/groups", ".");
        BuildResult result = runTask(":mlDeployGroups", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }

    @Test
    @Order(3)
    public void mlDeployTasksToNonDefaultGroupAsDeveloper() {
        configDir = "tasks";
        copyDirectoriesIntoConfigDir("ml-config/tasks/SomeGroup", configDir);
        BuildResult result = runFailedTask(":mlDeployTasks", "-PmlUsername=dh-developer", "-PmlPassword=dh-developer");
        assertTrue(result.getOutput().contains("\"statusCode\":\"403\", \"status\":\"Forbidden\""), result.getOutput());
        assertTrue(result.getOutput().contains("BUILD FAILED"), result.getOutput());
    }

    @Test
    public void createUsersAsEnvManager() {
        BuildResult result = runTask(":mlDeployUsers", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"));
    }
}
