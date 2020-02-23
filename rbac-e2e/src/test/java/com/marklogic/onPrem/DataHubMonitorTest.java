package com.marklogic.onPrem;

import com.marklogic.utils.BaseTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DataHubMonitorTest extends BaseTest {

    @Test
    public void task15ViewAuditingInfoTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8002/manage/v2/logs?filename=AuditLog.txt")
                .then()
                .statusCode(200);
    }

    @Test
    public void task16MonitorDatabaseStatusTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8002/manage/v2/databases/data-hub-FINAL?view=status")
                .then()
                .statusCode(200);
    }

    @Test
    public void task17MonitorBackUpsTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8002/manage/v2/forests/data-hub-FINAL-1?view=status")
                .then()
                .statusCode(200);
    }

    @Test
    public void task33ViewJobsDocTest() {
        //JsonPath res =
                given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8013/v1/search?collection=Jobs&format=json")
                .then()
                .statusCode(404);
                //.extract().response().body().jsonPath();
        //assertFalse(res.getInt("total")==0, "Expected non 0 output. Got: "+res.getInt("total"));
    }

    @Test
    public void task34MonitorAppServerTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8002/history")
                .then()
                .statusCode(200);
    }

    @Test
    public void monitorMetersTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8002/meters")
                .then()
                .statusCode(404);
    }

    @Test
    public void viewAppServerLogsTest() {
        given()
                .auth().digest("dh-monitor", "dh-monitor")
                .when()
                .get("http://localhost:8001/get-error-log.xqy?filename=8010_RequestLog.txt")
                .then()
                .statusCode(403);
    }
}
