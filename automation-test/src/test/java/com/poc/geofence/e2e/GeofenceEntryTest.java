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
 * GeofenceEntryTest - E2E tests for geofence entry detection (return to safe zone).
 *
 * <p>User Journey Flow:
 * <ol>
 *   <li>State 1 (Safe): Child starts inside geofence</li>
 *   <li>Action 1 (Exit): Child leaves -> Alert Triggered (TC-001/TC-003)</li>
 *   <li>Action 2 (Return): Child returns -> Safe Zone Message (TC-002/TC-004)</li>
 * </ol>
 *
 * <p>Test Cases:
 * <ul>
 *   <li>TC-002: iOS geofence entry (child returns to safe zone)</li>
 *   <li>TC-004: Android geofence entry (child returns to safe zone)</li>
 * </ul>
 *
 * <p>Why Exit & Entry Testing:
 * For Principal/Senior roles, interviewers look for engineers who understand
 * the Full Lifecycle of a feature, not just the happy path.
 */
@Epic("Mobile Geofence Automation")
@Feature("Geofence Entry Detection")
public class GeofenceEntryTest extends BaseTest {

    @Test(dataProvider = "iosGeofenceEntryData",
          dataProviderClass = TestDataProvider.class,
          retryAnalyzer = RetryAnalyzer.class,
          dependsOnMethods = {})
    @Story("iOS Geofence Entry")
    @Description("TC-002: Verify iOS app detects geofence entry (child returns to safe zone)")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-002")
    public void testIOSGeofenceEntry(String tcId, double centerLat, double centerLng,
                                      double exitLat, double exitLng,
                                      int radius, String title) {
        log.info("Executing {}: iOS Geofence Entry Test (User Journey)", tcId);
        Allure.parameter("Test Case ID", tcId);

        // Step 1: Set initial location inside geofence (Safe State)
        AllureUtils.step("Set GPS location inside geofence (Safe Zone)");
        LocationSimulator.setLocation(centerLat, centerLng);
        AllureUtils.attachScreenshot("State 1 - Child Inside Safe Zone");

        // Step 2: Create geofence
        AllureUtils.step("Create geofence zone");
        geofencePage.createGeofence(centerLat, centerLng, radius, title);

        // Step 3: Verify geofence created
        AllureUtils.step("Verify geofence created successfully");
        Assert.assertTrue(geofencePage.verifyGeofenceCreated(),
                "Geofence should be created successfully");

        // Step 4: Simulate exit (move to location 250m away) - Danger State
        AllureUtils.step("Simulate child leaving safe zone (Exit)");
        LocationSimulator.setLocation(exitLat, exitLng);
        AllureUtils.attachScreenshot("State 2 - Child Outside Safe Zone");

        // Step 5: Wait for exit event first (to complete the journey)
        AllureUtils.step("Wait for exit event (prerequisite for entry test)");
        int geofenceTimeout = config.getGeofenceWaitTimeout();
        boolean exitDetected = geofencePage.waitForGeofenceExitEvent(geofenceTimeout);
        Assert.assertTrue(exitDetected, "Exit event must be detected before testing entry");

        // Step 6: Simulate return to safe zone (move back to center)
        AllureUtils.step("Simulate child returning to safe zone (Entry)");
        LocationSimulator.setLocation(centerLat, centerLng);
        AllureUtils.attachScreenshot("State 3 - Child Returns to Safe Zone");

        // Step 7: Wait for geofence entry event
        AllureUtils.step("Wait for geofence entry event (Safe Zone notification)");
        boolean entryDetected = geofencePage.waitForGeofenceEntryEvent(geofenceTimeout);

        // Step 8: Verify entry event
        Assert.assertTrue(entryDetected,
                "Geofence entry event should be detected within " + geofenceTimeout + "s");

        String eventText = geofencePage.getGeofenceEventText();
        Assert.assertTrue(eventText.toLowerCase().contains("enter"),
                "Event text should contain 'enter': " + eventText);

        log.info("TC-002 PASSED: iOS geofence entry detected - child returned to safe zone");
    }

    @Test(dataProvider = "androidGeofenceEntryData",
          dataProviderClass = TestDataProvider.class,
          retryAnalyzer = RetryAnalyzer.class,
          dependsOnMethods = {})
    @Story("Android Geofence Entry")
    @Description("TC-004: Verify Android app detects geofence entry (child returns to safe zone)")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-004")
    public void testAndroidGeofenceEntry(String tcId, double centerLat, double centerLng,
                                          double exitLat, double exitLng,
                                          int radius, String title) {
        log.info("Executing {}: Android Geofence Entry Test (User Journey)", tcId);
        Allure.parameter("Test Case ID", tcId);

        // Step 1: Set initial location inside geofence (Safe State)
        AllureUtils.step("Set GPS location inside geofence (Safe Zone)");
        LocationSimulator.setLocation(centerLat, centerLng);
        AllureUtils.attachScreenshot("State 1 - Child Inside Safe Zone");

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

        // Step 4: Simulate exit (move to location 250m away) - Danger State
        AllureUtils.step("Simulate child leaving safe zone (Exit)");
        if (geofencePage instanceof GeofencePageAndroid androidPage) {
            androidPage.simulateExitFromGeofence(exitLat, exitLng);
        } else {
            LocationSimulator.setLocation(exitLat, exitLng);
        }
        AllureUtils.attachScreenshot("State 2 - Child Outside Safe Zone");

        // Step 5: Trigger exit event via deep link (bypasses BrowserStack GPS limitation)
        AllureUtils.step("Trigger geofence exit event via deep link");
        boolean exitTriggered = GeofenceTestHelper.triggerExitEvent(DriverManager.getDriver());
        Assert.assertTrue(exitTriggered, "Exit deep link trigger should succeed");

        // Step 6: Wait for exit event first (to complete the journey)
        AllureUtils.step("Wait for exit event (prerequisite for entry test)");
        int geofenceTimeout = 30; // Reduced timeout since we trigger directly
        boolean exitDetected = geofencePage.waitForGeofenceExitEvent(geofenceTimeout);
        Assert.assertTrue(exitDetected, "Exit event must be detected before testing entry");

        // Step 7: Simulate return to safe zone (move back to center)
        AllureUtils.step("Simulate child returning to safe zone (Entry)");
        if (geofencePage instanceof GeofencePageAndroid androidPage) {
            androidPage.simulateEntryToGeofence(centerLat, centerLng);
        } else {
            LocationSimulator.setLocation(centerLat, centerLng);
        }
        AllureUtils.attachScreenshot("State 3 - Child Returns to Safe Zone");

        // Step 8: Trigger entry event via deep link (bypasses BrowserStack GPS limitation)
        AllureUtils.step("Trigger geofence entry event via deep link");
        boolean entryTriggered = GeofenceTestHelper.triggerEnterEvent(DriverManager.getDriver());
        Assert.assertTrue(entryTriggered, "Entry deep link trigger should succeed");

        // Step 9: Wait for geofence entry notification
        AllureUtils.step("Wait for geofence entry notification (Safe Zone)");
        boolean entryDetected = geofencePage.waitForGeofenceEntryEvent(geofenceTimeout);

        // Step 8: Verify entry event
        Assert.assertTrue(entryDetected,
                "Geofence entry event should be detected within " + geofenceTimeout + "s");

        String eventText = geofencePage.getGeofenceEventText();
        Assert.assertTrue(eventText.contains("ENTER"),
                "Notification should contain 'ENTER': " + eventText);

        log.info("TC-004 PASSED: Android geofence entry detected - child returned to safe zone");
    }
}
