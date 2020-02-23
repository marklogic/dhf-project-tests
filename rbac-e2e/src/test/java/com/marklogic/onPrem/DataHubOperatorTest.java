package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import io.restassured.path.json.JsonPath;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataHubOperatorTest extends BaseTest {

    private String username = "-PmlUsername=dh-operator";
    private String password = "-PmlPassword=dh-operator";

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
    public void provenanceInfoTest() {
        String res = given()
                .auth().digest("dh-operator", "dh-operator")
                .when()
                .get("http://localhost:8013/v1/search?collection=http://marklogic.com/provenance-services/record&format=json")
                .then()
                .extract().asString();
        //Verify operator cant view provenance info
        assertTrue(res.contains("\"total\":0"), res);
    }

    @Test
    @Order(1)
    public void task21And31And38And39CRUDTest() {
        configDir = MLMODULES.toString()+"/root/custom-modules";
        copyDirectoryIntoProject("ml-modules/root/mapping-functions/",configDir);
        copyResourcesIntoProject("Order.entity.json","entities");
        copyResourcesIntoProject("ingestion_mapping_mastering-flow.flow.json","flows");
        copyResourcesIntoProject("ingestion_mapping_mastering-flow-mapping-step-xml-1.mapping.json","mappings/ingestion_mapping_mastering-flow-mapping-step-xml");

        BuildResult loadModules = runTask(":mlLoadModules", "-PmlUsername=dh-developer", "-PmlPassword=dh-developer");
        assertTrue(loadModules.getOutput().contains("BUILD SUCCESSFUL"), loadModules.getOutput());

        BuildResult runFlow = runTask(":hubRunFlow", "-PflowName=ingestion_mapping_mastering-flow", username, password);
        assertTrue(runFlow.getOutput().contains("\"failedEvents\" : 0"), runFlow.getOutput());
    }

    @Test
    public void task32MonitorMetersTest() {
        given()
                .auth().digest("dh-operator", "dh-operator")
                .when()
                .get("http://localhost:8002/manage/v2/meters")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    public void task33ViewJobsDocTest() {
        JsonPath res = given()
                .auth().digest("dh-operator", "dh-operator")
                .when()
                .get("http://localhost:8013/v1/search?collection=Jobs&format=json")
                .then()
                .extract().response().body().jsonPath();
        assertFalse(res.getInt("total")==0, "Expected non 0 output. Got: "+res.getInt("total"));
    }
}
