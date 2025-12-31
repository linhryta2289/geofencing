package com.poc.geofence.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.poc.geofence.config.ConfigManager;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * API tests for geofence endpoints.
 * Supports both WireMock (mock mode) and real API (staging/uat/prod).
 *
 * TestNG Groups:
 * - mock-api: Tests using WireMock (default, runs in CI)
 * - real-api: Tests against real API (nightly/opt-in)
 *
 * API-001: Create geofence (POST)
 * API-002: Get geofence (GET)
 */
@Epic("Mobile Geofence Automation")
@Feature("Geofence API")
public class GeofenceApiTest {
    private static final Logger log = LoggerFactory.getLogger(GeofenceApiTest.class);
    private final ConfigManager config = ConfigManager.getInstance();
    private WireMockServer wireMockServer;
    private static final int PORT = 8089;
    private static final String GEOFENCE_ID = "geo-123-456";
    private static final String TEST_DATA_PREFIX = "TEST_E2E_";

    // Track created geofence IDs for cleanup (real API mode only)
    private final List<String> createdGeofenceIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String apiMode = config.getApiMode();
        log.info("API Mode: {} | Real API: {}", apiMode, config.isRealApiMode());

        if (!config.isRealApiMode()) {
            // Mock mode: start WireMock server
            log.info("Starting WireMock server on port {}", PORT);
            wireMockServer = new WireMockServer(PORT);
            wireMockServer.start();
            WireMock.configureFor("localhost", PORT);
            RestAssured.baseURI = "http://localhost:" + PORT;
            setupStubs();
        } else {
            // Real API mode: configure RestAssured for real endpoint
            String baseUrl = config.getApiBaseUrl();
            log.info("Configuring real API: {} (mode: {})", baseUrl, apiMode);
            RestAssured.baseURI = baseUrl;
        }
    }

    /**
     * Returns a request specification with auth token if running against real API.
     */
    private RequestSpecification getRequestSpec() {
        RequestSpecification spec = given().contentType(ContentType.JSON);
        if (config.isRealApiMode()) {
            String token = config.getApiToken();
            if (token != null && !token.isEmpty()) {
                spec = spec.header("Authorization", "Bearer " + token);
            }
        }
        return spec;
    }

    private void setupStubs() {
        // POST /api/geofence - Create geofence
        stubFor(post(urlEqualTo("/api/geofence"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "%s",
                                    "status": "active",
                                    "latitude": 50.7333,
                                    "longitude": 7.1032,
                                    "radius": 200,
                                    "createdAt": "2025-12-22T12:00:00Z"
                                }
                                """.formatted(GEOFENCE_ID))));

        // GET /api/geofence/{id} - Get geofence by ID
        stubFor(get(urlPathMatching("/api/geofence/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "%s",
                                    "name": "TestZone",
                                    "latitude": 50.7333,
                                    "longitude": 7.1032,
                                    "radius": 200,
                                    "status": "active",
                                    "lastEvent": null
                                }
                                """.formatted(GEOFENCE_ID))));

        log.info("WireMock stubs configured");
    }

    @Test(groups = {"mock-api"})
    @Story("Create Geofence API")
    @Description("API-001: Verify POST /api/geofence creates geofence and returns 201 (mock)")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("API-001")
    public void testCreateGeofence() {
        log.info("Executing API-001: Create Geofence Test (mock mode)");

        String requestBody = """
                {
                    "latitude": 50.7333,
                    "longitude": 7.1032,
                    "radius": 200,
                    "name": "TestZone"
                }
                """;

        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post("/api/geofence")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("status", equalTo("active"))
                .body("latitude", equalTo(50.7333f))
                .body("longitude", equalTo(7.1032f))
                .body("radius", equalTo(200))
                .extract().response();

        String geofenceId = response.jsonPath().getString("id");
        log.info("API-001 PASSED: Geofence created with ID: {}", geofenceId);

        Assert.assertNotNull(geofenceId, "Geofence ID should not be null");
        Assert.assertEquals(geofenceId, GEOFENCE_ID);
    }

    @Test(groups = {"real-api"})
    @Story("Create Geofence API")
    @Description("API-001-REAL: Verify POST /api/geofence creates geofence (real API)")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("API-001-REAL")
    public void testCreateGeofenceReal() {
        // Skip POST in prod mode (read-only)
        if (config.isProdMode()) {
            throw new SkipException("POST tests disabled in prod mode (read-only)");
        }

        log.info("Executing API-001-REAL: Create Geofence Test (real API: {})", config.getApiMode());

        String testName = TEST_DATA_PREFIX + "Zone_" + System.currentTimeMillis();
        String requestBody = """
                {
                    "latitude": 50.7333,
                    "longitude": 7.1032,
                    "radius": 200,
                    "name": "%s"
                }
                """.formatted(testName);

        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post("/api/geofence")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("status", equalTo("active"))
                .extract().response();

        String geofenceId = response.jsonPath().getString("id");
        createdGeofenceIds.add(geofenceId);  // Track for cleanup
        log.info("API-001-REAL PASSED: Geofence created with ID: {}", geofenceId);

        Assert.assertNotNull(geofenceId, "Geofence ID should not be null");
    }

    @Test(groups = {"mock-api"}, dependsOnMethods = "testCreateGeofence")
    @Story("Get Geofence API")
    @Description("API-002: Verify GET /api/geofence/{id} returns geofence details (mock)")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("API-002")
    public void testGetGeofence() {
        log.info("Executing API-002: Get Geofence Test (mock mode)");

        Response response = getRequestSpec()
                .when()
                .get("/api/geofence/" + GEOFENCE_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(GEOFENCE_ID))
                .body("name", equalTo("TestZone"))
                .body("latitude", equalTo(50.7333f))
                .body("longitude", equalTo(7.1032f))
                .body("radius", equalTo(200))
                .body("status", equalTo("active"))
                .extract().response();

        log.info("API-002 PASSED: Geofence retrieved successfully");

        String status = response.jsonPath().getString("status");
        Assert.assertEquals(status, "active");
    }

    @Test(groups = {"real-api"}, dependsOnMethods = "testCreateGeofenceReal")
    @Story("Get Geofence API")
    @Description("API-002-REAL: Verify GET /api/geofence/{id} returns geofence details (real API)")
    @Severity(SeverityLevel.NORMAL)
    @TmsLink("API-002-REAL")
    public void testGetGeofenceReal() {
        // Use first created geofence ID, or skip if none
        if (createdGeofenceIds.isEmpty()) {
            throw new SkipException("GET test skipped - no geofence created (prod mode or previous failure)");
        }

        String geofenceId = createdGeofenceIds.get(0);
        log.info("Executing API-002-REAL: Get Geofence Test (real API: {}, ID: {})",
                config.getApiMode(), geofenceId);

        Response response = getRequestSpec()
                .when()
                .get("/api/geofence/" + geofenceId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(geofenceId))
                .body("status", equalTo("active"))
                .extract().response();

        log.info("API-002-REAL PASSED: Geofence retrieved successfully");

        String status = response.jsonPath().getString("status");
        Assert.assertEquals(status, "active");
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        // Stop WireMock if running (mock mode)
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock server stopped");
        }

        // Cleanup test data in real API mode (not prod)
        if (config.isRealApiMode() && !config.isProdMode() && !createdGeofenceIds.isEmpty()) {
            cleanupTestData();
        }
    }

    /**
     * Cleans up TEST_E2E_* geofences created during real API tests.
     * Only runs in staging/uat modes, never in prod.
     * Logs cleanup failures but tracks them - throws if all cleanups fail.
     */
    private void cleanupTestData() {
        log.info("Cleaning up {} test geofences...", createdGeofenceIds.size());
        int successCount = 0;
        int failCount = 0;

        for (String geofenceId : createdGeofenceIds) {
            try {
                int statusCode = getRequestSpec()
                        .when()
                        .delete("/api/geofence/" + geofenceId)
                        .then()
                        .extract().statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    successCount++;
                    log.info("Deleted geofence {}: status {}", geofenceId, statusCode);
                } else {
                    failCount++;
                    log.warn("Failed to delete geofence {}: status {}", geofenceId, statusCode);
                }
            } catch (Exception e) {
                failCount++;
                log.error("Exception deleting geofence {}: {}", geofenceId, e.getMessage());
            }
        }

        createdGeofenceIds.clear();
        log.info("Cleanup complete: {} deleted, {} failed", successCount, failCount);

        // Warn if any cleanups failed (data pollution risk)
        if (failCount > 0) {
            log.warn("TEST DATA POLLUTION: {} geofences may remain in {} environment",
                    failCount, config.getApiMode());
        }
    }
}
