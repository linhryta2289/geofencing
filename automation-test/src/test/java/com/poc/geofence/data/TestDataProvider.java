package com.poc.geofence.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * TestNG DataProvider for geofence test data.
 * Loads test locations from geofence-locations.json.
 */
public class TestDataProvider {
    private static final Logger log = LoggerFactory.getLogger(TestDataProvider.class);
    private static JsonNode locationData;

    static {
        loadTestData();
    }

    private static void loadTestData() {
        try (InputStream is = TestDataProvider.class.getClassLoader()
                .getResourceAsStream("testdata/geofence-locations.json")) {
            ObjectMapper mapper = new ObjectMapper();
            locationData = mapper.readTree(is);
            log.info("Loaded test data from geofence-locations.json");
        } catch (Exception e) {
            log.error("Failed to load test data", e);
            throw new RuntimeException("Test data initialization failed", e);
        }
    }

    @DataProvider(name = "iosGeofenceData")
    public static Object[][] iosGeofenceData() {
        JsonNode ahmedabad = locationData.get("locations").get("ahmedabad_mi");
        JsonNode exit = locationData.get("locations").get("ahmedabad_exit");

        return new Object[][]{
                {
                        "TC-001",
                        ahmedabad.get("latitude").asDouble(),
                        ahmedabad.get("longitude").asDouble(),
                        exit.get("latitude").asDouble(),
                        exit.get("longitude").asDouble(),
                        200,
                        "TestZone_iOS"
                }
        };
    }

    @DataProvider(name = "androidGeofenceData")
    public static Object[][] androidGeofenceData() {
        JsonNode bonn = locationData.get("locations").get("bonn_center");
        JsonNode exit = locationData.get("locations").get("bonn_exit");

        return new Object[][]{
                {
                        "TC-003",
                        bonn.get("latitude").asDouble(),
                        bonn.get("longitude").asDouble(),
                        exit.get("latitude").asDouble(),
                        exit.get("longitude").asDouble(),
                        200,
                        "TestZone_Android"
                }
        };
    }

    /**
     * Data for iOS geofence entry test (TC-002).
     * User Journey: After exit, child returns to safe zone.
     * Returns: tcId, centerLat, centerLng, exitLat, exitLng, radius, title
     */
    @DataProvider(name = "iosGeofenceEntryData")
    public static Object[][] iosGeofenceEntryData() {
        JsonNode ahmedabad = locationData.get("locations").get("ahmedabad_mi");
        JsonNode exit = locationData.get("locations").get("ahmedabad_exit");

        return new Object[][]{
                {
                        "TC-002",
                        ahmedabad.get("latitude").asDouble(),  // center (safe zone)
                        ahmedabad.get("longitude").asDouble(),
                        exit.get("latitude").asDouble(),       // exit point
                        exit.get("longitude").asDouble(),
                        200,
                        "TestZone_iOS"
                }
        };
    }

    /**
     * Data for Android geofence entry test (TC-004).
     * User Journey: After exit, child returns to safe zone.
     * Returns: tcId, centerLat, centerLng, exitLat, exitLng, radius, title
     */
    @DataProvider(name = "androidGeofenceEntryData")
    public static Object[][] androidGeofenceEntryData() {
        JsonNode bonn = locationData.get("locations").get("bonn_center");
        JsonNode exit = locationData.get("locations").get("bonn_exit");

        return new Object[][]{
                {
                        "TC-004",
                        bonn.get("latitude").asDouble(),       // center (safe zone)
                        bonn.get("longitude").asDouble(),
                        exit.get("latitude").asDouble(),       // exit point
                        exit.get("longitude").asDouble(),
                        200,
                        "TestZone_Android"
                }
        };
    }
}
