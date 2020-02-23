package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataHubPortalSecurityAdminTest extends BaseTest {

    private String username = "-PmlUsername=dh-portal-sec-admin";
    private String password = "-PmlPassword=dh-portal-sec-admin";
    private String configDir = "security/";

    @AfterEach
    public void teardown() {
        removeDirectoriesFromProject(configDir);
    }

    @Test
    @Order(1)
    public void task4DeployExternalSecurityAsPortalSecAdmin() {
        copyResourcesIntoConfigDir("ml-config/security/external-security/ldap-internal.json", "security/external-security");
        BuildResult result = runTask(":mlDeployExternalSecurity", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    @Order(2)
    public void undeployExternalSecurityAsPortalSecAdmin() {
        copyResourcesIntoConfigDir("ml-config/security/external-security/ldap-internal.json", "security/external-security");
        BuildResult result = runTask(":mlUndeployExternalSecurity", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }
}
