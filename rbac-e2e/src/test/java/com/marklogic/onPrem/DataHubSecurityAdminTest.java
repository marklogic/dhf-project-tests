package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataHubSecurityAdminTest extends BaseTest {

    private String username = "-PmlUsername=dh-sec-admin";
    private String password = "-PmlPassword=dh-sec-admin";
    private String configDir = "security/";

    @AfterEach
    public void teardown() {
            removeDirectoriesFromProject(configDir);
    }

    @Test
    public void installDHAsSecAdmin() {
        BuildResult result = runFailedTask(":mlDeploy", username, password);
        assertTrue(result.getOutput().contains("\"statusCode\":\"403\", \"status\":\"Forbidden\""), result.getOutput());
    }

    @Test
    public void deployRolesAsSecAdmin() {
        BuildResult result = runFailedTask(":mlDeployRoles", username, password);
        assertTrue(result.getOutput().contains("BUILD FAILED"), result.getOutput());
        assertTrue(result.getOutput().contains("\"statusCode\":\"400\", \"status\":\"Bad Request\""), result.getOutput());
    }

    @Test
    @Order(1)
    public void task3And5CustomRolesAndPrivilegesAsSecAdmin() {
        copyResourcesIntoConfigDir("ml-config/security/roles/custom-role1.json", "security/roles");
        copyResourcesIntoConfigDir("ml-config/security/privileges/privilege1.json", "security/privileges");
        BuildResult result = runTask(":hubDeployAsSecurityAdmin", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    @Order(2)
    public void deleteCustomRoleAsSecAdmin() {
        copyResourcesIntoConfigDir("ml-config/security/roles/custom-role1.json", "security/roles");
        BuildResult result = runTask(":mlUnDeployRoles", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

}
