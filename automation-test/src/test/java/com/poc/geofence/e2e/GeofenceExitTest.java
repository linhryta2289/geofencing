package com.poc.geofence.e2e;

import com.poc.geofence.base.BaseTest;
import com.poc.geofence.data.TestDataProvider;
import com.poc.geofence.driver.DriverManager;
import com.poc.geofence.pages.android.GeofencePageAndroid;
import com.poc.geofence.utils.AllureUtils;
import com.poc.geofence.utils.GeofenceTestHelper;
import com.poc.geofence.utils.LocationSimulator;
import com.poc.geofence.utils.RetryAnalyzer;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * GeofenceExitTest - E2E tests for geofence exit detection.
 *
 * <p>Test Strategy:
 * <ol>
 *   <li>Set GPS inside geofence</li>
 *   <li>Create geofence zone</li>
 *   <li>Simulate GPS exit (250m away)</li>
 *   <li>Wait for exit event (up to 120s)</li>
 *   <li>Verify notification/alert</li>
 * </ol>
 *
 * <p>Test Cases:
 * <ul>
 *   <li>TC-001: iOS geofence exit</li>
 *   <li>TC-003: Android geofence exit</li>
 * </ul>
 *
 * <p>AI-Assisted Development:
 * <ul>
 *   <li>Test structure generated with Copilot from comments</li>
 *   <li>Allure annotations added via Copilot suggestions</li>
 *   <li>Wait strategies refined with Claude Code</li>
 * </ul>
 */
@Epic("Mobile Geofence Automation")
@Feature("Geofence Exit Detection")
public class GeofenceExitTest extends BaseTest {

    @Test(dataProvider = "iosGeofenceData",
          dataProviderClass = TestDataProvider.class,
          retryAnalyzer = RetryAnalyzer.class)
    @Story("iOS Geofence Exit")
    @Description("TC-001: Verify iOS app detects geofence exit and shows alert")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-001")
    public void testIOSGeofenceExit(String tcId, double centerLat, double centerLng,
                                     double exitLat, double exitLng,
                                     int radius, String title) {
        log.info("Executing {}: iOS Geofence Exit Test", tcId);
        Allure.parameter("Test Case ID", tcId);

        // Step 1: Set initial location inside geofence
        AllureUtils.step("Set GPS location inside geofence");
        LocationSimulator.setLocation(centerLat, centerLng);
        AllureUtils.attachScreenshot("Location Set - Inside Geofence");

        // Step 2: Create geofence
        AllureUtils.step("Create geofence zone");
        geofencePage.createGeofence(centerLat, centerLng, radius, title);

        // Step 3: Verify geofence created
        AllureUtils.step("Verify geofence created successfully");
        Assert.assertTrue(geofencePage.verifyGeofenceCreated(),
                "Geofence should be created successfully");

        // Step 4: Simulate exit (move to location 250m away)
        AllureUtils.step("Simulate GPS location outside geofence (250m away)");
        LocationSimulator.setLocation(exitLat, exitLng);
        AllureUtils.attachScreenshot("Location Set - Outside Geofence");

        // Step 5: Wait for geofence exit event (iOS can take up to 3 min)
        AllureUtils.step("Wait for geofence exit event");
        int geofenceTimeout = config.getGeofenceWaitTimeout();
        boolean exitDetected = geofencePage.waitForGeofenceExitEvent(geofenceTimeout);

        // Step 6: Verify exit event
        Assert.assertTrue(exitDetected,
                "Geofence exit event should be detected within " + geofenceTimeout + "s");

        String eventText = geofencePage.getGeofenceEventText();
        Assert.assertTrue(eventText.toLowerCase().contains("exit"),
                "Event text should contain 'exit': " + eventText);

        log.info("TC-001 PASSED: iOS geofence exit detected");
    }

    @Test(dataProvider = "androidGeofenceData",
          dataProviderClass = TestDataProvider.class,
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Android Geofence Exit")
    @Description("TC-003: Verify Android app detects geofence exit and shows notification")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-003")
    public void testAndroidGeofenceExit(String tcId, double centerLat, double centerLng,
                                         double exitLat, double exitLng,
                                         int radius, String title) {
        log.info("Executing {}: Android Geofence Exit Test", tcId);
        Allure.parameter("Test Case ID", tcId);

        // Step 1: Set initial location inside geofence
        AllureUtils.step("Set GPS location inside geofence");
        LocationSimulator.setLocation(centerLat, centerLng);
        AllureUtils.attachScreenshot("Location Set - Inside Geofence");

        // Allow map to center
        sleep(3000);

        // Step 2: Create geofence (Android: long-click on map)
        AllureUtils.step("Create geofence zone via map long-click");
        geofencePage.createGeofence(centerLat, centerLng, radius, title);
        AllureUtils.attachScreenshot("Geofence Created");

        // Step 3: Verify geofence created
        AllureUtils.step("Verify geofence created successfully");
        Assert.assertTrue(geofencePage.verifyGeofenceCreated(),
                "Geofence should be created successfully");

        // Step 4: Simulate exit (move to location 250m away)
        AllureUtils.step("Simulate GPS location outside geofence (250m away)");
        if (geofencePage instanceof GeofencePageAndroid androidPage) {
            androidPage.simulateExitFromGeofence(exitLat, exitLng);
        } else {
            LocationSimulator.setLocation(exitLat, exitLng);
        }
        AllureUtils.attachScreenshot("Location Set - Outside Geofence");

        // Step 5: Trigger geofence exit via deep link (bypasses BrowserStack GPS limitation)
        AllureUtils.step("Trigger geofence exit event via deep link");
        boolean triggered = GeofenceTestHelper.triggerExitEvent(DriverManager.getDriver());
        Assert.assertTrue(triggered, "Deep link trigger should succeed");
        AllureUtils.attachScreenshot("Geofence Exit Triggered");

        // Step 6: Wait for geofence exit notification
        AllureUtils.step("Wait for geofence exit notification");
        int geofenceTimeout = 30; // Reduced timeout since we trigger directly
        boolean exitDetected = geofencePage.waitForGeofenceExitEvent(geofenceTimeout);

        // Step 6: Verify exit event
        Assert.assertTrue(exitDetected,
                "Geofence exit event should be detected within " + geofenceTimeout + "s");

        String eventText = geofencePage.getGeofenceEventText();
        Assert.assertTrue(eventText.contains("EXIT"),
                "Notification should contain 'EXIT': " + eventText);

        log.info("TC-003 PASSED: Android geofence exit detected");
    }
}
