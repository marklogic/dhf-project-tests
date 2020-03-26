package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import io.restassured.path.json.JsonPath;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataHubDeveloperTest extends BaseTest {

    private String username = "-PmlUsername=dh-developer";
    private String password = "-PmlPassword=dh-developer";
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
    public void mlDeployTest() {
        BuildResult result = runFailedTask(":mlDeploy", username, password);
        assertTrue(result.getOutput().contains("\"statusCode\":\"403\", \"status\":\"Forbidden\""), result.getOutput());
    }

    @Test
    public void task10TemporalConfigTest() {
        configDir = "databases/data-hub-FINAL";
        copyDirectoriesIntoConfigDir("ml-config/databases/data-hub-FINAL/temporal", configDir);
        BuildResult result = runTask(":mlDeployTemporal", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task11ConfigProtectedPathTest() {
        configDir = "security/protected-paths";
        copyDirectoriesIntoConfigDir("ml-config/security/protected-paths", configDir);
        BuildResult result = runTask(":mlDeployProtectedPaths", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task11ConfigQueryRolesetTest() {
        configDir = "security/query-rolesets";
        copyDirectoriesIntoConfigDir("ml-config/security/query-rolesets", configDir);
        BuildResult result = runTask(":mlDeployQueryRolesets", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }


    @Test
    public void task12TriggersDeployTest() {
        configDir = "databases/data-hub-final-TRIGGERS";
        copyDirectoriesIntoConfigDir("ml-config/databases/data-hub-final-TRIGGERS", configDir);
        BuildResult result = runTask(":mlDeployTriggers", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task14AlertConfigTest() {
        configDir = "databases/data-hub-FINAL";
        copyDirectoriesIntoConfigDir("ml-config/databases/data-hub-FINAL/alert", configDir);
        BuildResult result = runTask(":mlDeployAlerting", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void task15ViewAuditingInfoTest() {
        given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8002/manage/v2/logs?filename=AuditLog.txt")
                .then()
                .statusCode(200);
    }

    @Test
    public void task16MonitorDatabaseStatusTest() {
        given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8002/manage/v2/databases/data-hub-FINAL?view=status")
                .then()
                .statusCode(200);
    }

    @Test
    public void task17MonitorBackUpsTest() {
        given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8002/manage/v2/forests/data-hub-FINAL-1?view=status")
                .then()
                .statusCode(200);
    }

    @Test
    public void task18ScheduledTasksToDefaultGroupTest() {
        configDir = "tasks";
        copyDirectoriesIntoConfigDir("ml-config/tasks/Default", configDir);
        BuildResult result = runTask(":mlDeployTasks", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    /**
     * deploying a scheduled task to non default group will fail because user doesnt
     * have granular privilege for the non-default group. If he did the test would work
    **/
    @Test
    public void scheduledTasksToNonDefaultGroupTest() {
        configDir = "tasks";
        copyDirectoriesIntoConfigDir("ml-config/tasks/SomeGroup", configDir);
        BuildResult result = runFailedTask(":mlDeployTasks", username, password);
        assertTrue(result.getOutput().contains("BUILD FAILED"), result.getOutput());
    }

    @Test
    public void tak19UpdateIndexesToStagingTest() {
        configDir = "databases/staging-database.json";
        copyResourcesIntoConfigDir("ml-config/databases/staging-database.json", "databases");
        BuildResult result = runTask(":hubDeployAsDeveloper", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void tak19UpdateIndexesToFinalTest() {
        configDir = "databases/final-database.json";
        copyResourcesIntoConfigDir("ml-config/databases/final-database.json", "databases");
        BuildResult result = runTask(":hubDeployAsDeveloper", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void tak19UpdateIndexesToJobsTest() {
        configDir = "databases/job-database.json";
        copyResourcesIntoConfigDir("ml-config/databases/job-database.json", "databases");
        BuildResult result = runTask(":hubDeployAsDeveloper", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    public void deleteJobsDBTest() {
        BuildResult result = runTask(":mlClearDatabase", username, password, "-Pdatabase=data-hub-JOBS", "-Pconfirm=true");
        assertTrue(result.getOutput().contains("\"statusCode\":\"403\", \"status\":\"Forbidden\""), result.getOutput());
    }

    @Test
    public void task20HubDeployAsDeveloperTest() {
        configDir="src/main";
        copyDirectoryIntoProject("ml-modules",configDir);
        BuildResult result = runTask(":hubDeployAsDeveloper", username, password);
        assertTrue(result.getOutput().contains("BUILD SUCCESSFUL"), result.getOutput());
    }

    @Test
    @Order(1)
    public void task21And31And38And39CRUDTest() {
        configDir = MLMODULES.toString()+"/root";
        copyDirectoryIntoProject("ml-modules/root/custom-modules",configDir);
        copyResourcesIntoProject("Order.entity.json","entities");
        copyResourcesIntoProject("ingestion_mapping_mastering-flow.flow.json","flows");
        copyResourcesIntoProject("ingestion_mapping_mastering-flow-mapping-step-xml-1.mapping.json","mappings/ingestion_mapping_mastering-flow-mapping-step-xml");

        BuildResult loadModules = runTask(":hubDeployAsDeveloper", username, password);
        assertTrue(loadModules.getOutput().contains("BUILD SUCCESSFUL"), loadModules.getOutput());

        BuildResult runFlow = runTask(":hubRunFlow", "-PflowName=ingestion_mapping_mastering-flow", username, password);
        assertFalse(runFlow.getOutput().contains("\"successfulEvents\" : 0"), runFlow.getOutput());
        assertFalse(runFlow.getOutput().contains("\"failedEvents\" : 10"), runFlow.getOutput());
        assertTrue(runFlow.getOutput().contains("BUILD SUCCESSFUL"), runFlow.getOutput());
    }

    @Test
    public void task23SchemasDeployTest() {
        configDir = "databases/data-hub-staging-SCHEMAS";
        copyDirectoriesIntoConfigDir("ml-config/databases/data-hub-staging-SCHEMAS", configDir);

        BuildResult loadTest = runTask(":mlLoadSchemas", username, password);
        assertTrue(loadTest.getOutput().contains("BUILD SUCCESSFUL"), loadTest.getOutput());

        BuildResult reloadTest = runTask(":mlReLoadSchemas", username, password);
        assertTrue(reloadTest.getOutput().contains("BUILD SUCCESSFUL"), reloadTest.getOutput());
    }

    @Test
    @Order(2)
    public void task24ProvenanceInfoTest() {
        JsonPath res = given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8013/v1/search?collection=http://marklogic.com/provenance-services/record&format=json")
                .then()
                .extract().response().body().jsonPath();
        assertFalse(res.getInt("total")==0, "Expected non 0 output. Got: "+res.getInt("total"));
    }

    @Test
    public void task27ReadStagingArtifactsTest() {
        JsonPath res = given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8010/v1/search?collection=http://marklogic.com/data-hub/flow&format=json")
                .then()
                .extract().response().body().jsonPath();
        assertFalse(res.getInt("total")==0, "Expected non 0 output. Got: "+res.getInt("total"));
    }

    @Test
    public void task28ReadFinalArtifactsTest() {
        JsonPath res = given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8011/v1/search?collection=http://marklogic.com/data-hub/flow&format=json")
                .then()
                .extract().response().body().jsonPath();
        assertFalse(res.getInt("total")==0, "Expected non 0 output. Got: "+res.getInt("total"));
    }

    @Test
    public void task22And30ViewAppServerLogsTest() {
        given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8002/manage/v2/logs")
                .then()
                .statusCode(200);
    }

    @Test
    public void task32ViewMetersTest() {
        given()
                .auth().digest("dh-developer", "dh-developer")
                .when()
                .get("http://localhost:8002/manage/v2/meters")
                .then()
                .statusCode(200);
    }

}
