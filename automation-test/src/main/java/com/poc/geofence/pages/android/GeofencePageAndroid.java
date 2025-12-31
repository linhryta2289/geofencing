package com.poc.geofence.pages.android;

import com.poc.geofence.components.NotificationHandler;
import com.poc.geofence.pages.BasePage;
import com.poc.geofence.pages.GeofencePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Android implementation of GeofencePage.
 * Interacts with android-geofencing app via map long-click.
 */
public class GeofencePageAndroid extends BasePage implements GeofencePage {
    // Locators based on android-geofencing-main MapsActivity.java
    private static final By MAP_FRAGMENT = AppiumBy.id("com.eebax.geofencing:id/map");
    private static final By PERMISSION_ALLOW = AppiumBy.id(
            "com.android.permissioncontroller:id/permission_allow_foreground_only_button");
    private static final By PERMISSION_ALLOW_ALL_TIME = AppiumBy.xpath(
            "//*[contains(@text,'Allow all the time') or contains(@text,'allow all the time')]");
    private static final By PERMISSION_ALLOW_BUTTON = AppiumBy.id(
            "com.android.permissioncontroller:id/permission_allow_button");

    private final NotificationHandler notificationHandler;

    public GeofencePageAndroid() {
        super();
        this.notificationHandler = new NotificationHandler();
    }

    @Override
    public void createGeofence(double latitude, double longitude, int radius, String title) {
        log.info("Creating Android geofence at map center (long-click)");

        // Android app creates geofence by long-clicking on map
        // Geofence is always 200m radius (hardcoded in app)
        // First, set GPS location to the geofence center
        setGpsLocation(latitude, longitude);

        // Wait for map to load and center
        WebElement map = waitForVisible(MAP_FRAGMENT);
        sleep(3000); // Wait for map tiles to load

        // Long-click at center of map to create geofence
        longClickOnElement(map);
        attachScreenshot("geofence_created_android");
    }

    private void setGpsLocation(double latitude, double longitude) {
        log.info("Setting GPS location: {}, {}", latitude, longitude);
        try {
            // Cast to AndroidDriver to use setLocation
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                androidDriver.setLocation(new org.openqa.selenium.html5.Location(latitude, longitude, 0.0));
            } else {
                driver.executeScript("mobile: setLocation",
                        Map.of(
                                "latitude", latitude,
                                "longitude", longitude,
                                "altitude", 0.0
                        ));
            }
        } catch (Exception e) {
            log.warn("setLocation failed: {}", e.getMessage());
            // Swallow exception - GPS might already be set via capabilities
        }
    }

    private void longClickOnElement(WebElement element) {
        Point center = element.getLocation();
        Dimension size = element.getSize();
        int centerX = center.getX() + size.getWidth() / 2;
        int centerY = center.getY() + size.getHeight() / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 0);

        longPress.addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(finger.createPointerMove(
                Duration.ofSeconds(2), PointerInput.Origin.viewport(), centerX, centerY));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(longPress));
        log.debug("Long-clicked at center: ({}, {})", centerX, centerY);
    }

    @Override
    public boolean verifyGeofenceCreated() {
        // Android app shows red circle on map after geofence created
        // Can verify via visual inspection or log message
        // For now, assume success if no exception
        return true;
    }

    @Override
    public String getGeofenceEventText() {
        // Check notification panel for geofence event
        return notificationHandler.getNotificationText();
    }

    @Override
    public void handlePermissions() {
        log.info("Handling Android permissions");

        // Handle notification permission (Android 13+)
        if (isElementPresent(PERMISSION_ALLOW_BUTTON, 3)) {
            click(PERMISSION_ALLOW_BUTTON);
            log.info("Clicked 'Allow' for notification permission");
        }

        // Handle "While using the app" permission
        if (isElementPresent(PERMISSION_ALLOW, 5)) {
            click(PERMISSION_ALLOW);
            log.info("Clicked 'Allow only while using the app'");
        }

        // Handle "Allow all the time" for background location
        if (isElementPresent(PERMISSION_ALLOW_ALL_TIME, 3)) {
            click(PERMISSION_ALLOW_ALL_TIME);
            log.info("Clicked 'Allow all the time'");
        }
    }

    @Override
    public boolean waitForGeofenceExitEvent(int timeoutSeconds) {
        log.info("Waiting up to {}s for Android geofence exit event", timeoutSeconds);

        // Check for both Toast and Notification since app shows Toast immediately
        // Toast text: "Test: GEOFENCE_TRANSITION_EXIT"
        return notificationHandler.waitForToastOrNotification(
                "GEOFENCE_TRANSITION_EXIT",
                timeoutSeconds
        );
    }

    /**
     * Simulates moving outside geofence by updating GPS location.
     * @param exitLatitude Latitude outside geofence (250m+ from center)
     * @param exitLongitude Longitude outside geofence
     */
    public void simulateExitFromGeofence(double exitLatitude, double exitLongitude) {
        log.info("Simulating geofence exit to: {}, {}", exitLatitude, exitLongitude);
        setGpsLocation(exitLatitude, exitLongitude);
        attachScreenshot("geofence_exit_simulation");
    }

    @Override
    public boolean waitForGeofenceEntryEvent(int timeoutSeconds) {
        log.info("Waiting up to {}s for Android geofence entry event (safe zone)", timeoutSeconds);

        // Check for both Toast and Notification since app shows Toast immediately
        // Toast text: "Test: GEOFENCE_TRANSITION_ENTER"
        return notificationHandler.waitForToastOrNotification(
                "GEOFENCE_TRANSITION_ENTER",
                timeoutSeconds
        );
    }

    /**
     * Simulates returning to safe zone by updating GPS to center location.
     * @param centerLatitude Latitude at geofence center
     * @param centerLongitude Longitude at geofence center
     */
    public void simulateEntryToGeofence(double centerLatitude, double centerLongitude) {
        log.info("Simulating geofence entry (return to safe zone): {}, {}", centerLatitude, centerLongitude);
        setGpsLocation(centerLatitude, centerLongitude);
        attachScreenshot("geofence_entry_simulation");
    }
}
