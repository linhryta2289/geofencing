package com.poc.geofence.pages.ios;

import com.poc.geofence.components.AlertHandler;
import com.poc.geofence.pages.BasePage;
import com.poc.geofence.pages.GeofencePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * iOS implementation of GeofencePage.
 * Interacts with iOS-Geofence-Demo app via text fields and Add button.
 */
public class GeofencePageIOS extends BasePage implements GeofencePage {
    // Locators based on iOS-Geofence-Demo-master ViewController.swift
    // Using position-based XPath since no accessibility IDs present
    private static final By TXT_LATITUDE = AppiumBy.xpath(
            "(//XCUIElementTypeTextField)[1]");
    private static final By TXT_LONGITUDE = AppiumBy.xpath(
            "(//XCUIElementTypeTextField)[2]");
    private static final By TXT_RANGE = AppiumBy.xpath(
            "(//XCUIElementTypeTextField)[3]");
    private static final By TXT_TITLE = AppiumBy.xpath(
            "(//XCUIElementTypeTextField)[4]");
    private static final By TXT_MESSAGE = AppiumBy.xpath(
            "(//XCUIElementTypeTextField)[5]");
    private static final By BTN_ADD = AppiumBy.accessibilityId("Add");

    private final AlertHandler alertHandler;

    public GeofencePageIOS() {
        super();
        this.alertHandler = new AlertHandler();
    }

    @Override
    public void createGeofence(double latitude, double longitude, int radius, String title) {
        log.info("Creating iOS geofence: lat={}, lng={}, radius={}, title={}",
                latitude, longitude, radius, title);

        type(TXT_LATITUDE, String.valueOf(latitude));
        type(TXT_LONGITUDE, String.valueOf(longitude));
        type(TXT_RANGE, String.valueOf(radius));
        type(TXT_TITLE, title);
        type(TXT_MESSAGE, "Geofence zone: " + title);

        hideKeyboard();
        click(BTN_ADD);
        attachScreenshot("geofence_created_ios");
    }

    @Override
    public boolean verifyGeofenceCreated() {
        // iOS app shows "Data Added" alert on success
        if (alertHandler.waitForAlert(5)) {
            String alertText = alertHandler.getAlertText();
            boolean success = alertText.contains("Data Added");
            alertHandler.acceptAlert();
            return success;
        }
        return false;
    }

    @Override
    public String getGeofenceEventText() {
        if (alertHandler.waitForAlert(10)) {
            String text = alertHandler.getAlertText();
            log.info("iOS geofence event alert: {}", text);
            alertHandler.acceptAlert();
            return text;
        }
        return "";
    }

    @Override
    public void handlePermissions() {
        // iOS auto-accept alerts enabled via capability
        // But manually accept if present
        try {
            if (alertHandler.isAlertPresent()) {
                String alertText = alertHandler.getAlertText();
                if (alertText.contains("Location") || alertText.contains("location")) {
                    alertHandler.acceptAlert();
                    log.info("Accepted location permission alert");
                }
            }
        } catch (Exception e) {
            log.debug("No permission alert present");
        }
    }

    @Override
    public boolean waitForGeofenceExitEvent(int timeoutSeconds) {
        log.info("Waiting up to {}s for iOS geofence exit event", timeoutSeconds);
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (alertHandler.waitForAlert(5)) {
                String alertText = alertHandler.getAlertText();
                // iOS app shows "Exit from [title]" on exit
                if (alertText.toLowerCase().contains("exit")) {
                    log.info("Geofence exit detected: {}", alertText);
                    alertHandler.acceptAlert();
                    attachScreenshot("geofence_exit_ios");
                    return true;
                }
                alertHandler.acceptAlert();
            }
            sleep(2000);
        }

        log.warn("Geofence exit event not detected within {}s", timeoutSeconds);
        attachScreenshot("geofence_exit_timeout_ios");
        return false;
    }

    @Override
    public boolean waitForGeofenceEntryEvent(int timeoutSeconds) {
        log.info("Waiting up to {}s for iOS geofence entry event (safe zone)", timeoutSeconds);
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (alertHandler.waitForAlert(5)) {
                String alertText = alertHandler.getAlertText();
                // iOS app shows "Entered [title]" or "Enter to [title]" on entry
                if (alertText.toLowerCase().contains("enter")) {
                    log.info("Geofence entry detected (safe zone): {}", alertText);
                    alertHandler.acceptAlert();
                    attachScreenshot("geofence_entry_ios");
                    return true;
                }
                alertHandler.acceptAlert();
            }
            sleep(2000);
        }

        log.warn("Geofence entry event not detected within {}s", timeoutSeconds);
        attachScreenshot("geofence_entry_timeout_ios");
        return false;
    }
}
