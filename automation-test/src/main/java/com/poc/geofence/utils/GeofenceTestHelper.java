package com.poc.geofence.utils;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Helper for triggering geofence events via deep link on the Android app.
 * This bypasses the GeofencingClient limitation on BrowserStack where simulated GPS
 * doesn't trigger FusedLocationProvider events.
 *
 * Uses deep links (geofence://test/trigger?transition=X) which work on BrowserStack
 * because Appium can open URLs that the app handles.
 *
 * The Android app must be a debug build with deep link handler enabled.
 */
public class GeofenceTestHelper {
    private static final Logger log = LoggerFactory.getLogger(GeofenceTestHelper.class);

    // Geofence transition types (matches Android Geofence constants)
    public static final int GEOFENCE_ENTER = 1;
    public static final int GEOFENCE_EXIT = 2;

    // Deep link base URL (must match AndroidManifest intent-filter)
    private static final String DEEP_LINK_BASE = "geofence://test/trigger";

    private GeofenceTestHelper() {
        // Utility class
    }

    /**
     * Triggers a geofence event using multiple methods.
     * Strategy: Terminate app, then activate with deep link to ensure intent is received.
     *
     * @param driver The Appium driver
     * @param transition The transition type: GEOFENCE_ENTER (1), GEOFENCE_EXIT (2), or GEOFENCE_DWELL (4)
     * @return true if the trigger was successful
     */
    public static boolean triggerGeofenceEvent(AppiumDriver driver, int transition) {
        String transitionName = getTransitionName(transition);
        String intentUri = DEEP_LINK_BASE + "?transition=" + transition;

        log.info("Triggering geofence event: {} ({})", transitionName, intentUri);

        // Method 1: Try mobile: deepLink with terminate/activate cycle
        try {
            log.info("Attempting terminate-deepLink-activate approach");
            // Terminate app first
            driver.executeScript("mobile: terminateApp", Map.of("appId", "com.eebax.geofencing"));
            Thread.sleep(1000);

            // Launch app with deep link
            driver.executeScript("mobile: deepLink", Map.of(
                    "url", intentUri,
                    "package", "com.eebax.geofencing"
            ));
            Thread.sleep(3000);
            log.info("deepLink executed after terminate: {}", transitionName);
            return true;
        } catch (Exception e) {
            log.warn("terminate-deepLink failed: {}, trying startActivity", e.getMessage());
        }

        // Method 2: Try mobile: startActivity
        try {
            driver.executeScript("mobile: startActivity", Map.of(
                    "intent", "android.intent.action.VIEW",
                    "package", "com.eebax.geofencing",
                    "activity", ".MapsActivity",
                    "optionalIntentArguments", "-d " + intentUri
            ));
            Thread.sleep(2000);
            log.info("startActivity executed: {}", transitionName);
            return true;
        } catch (Exception e) {
            log.warn("startActivity failed: {}, trying driver.get()", e.getMessage());
        }

        // Method 3: Try driver.get() as fallback
        try {
            driver.get(intentUri);
            Thread.sleep(2000);
            log.info("driver.get() executed: {}", transitionName);
            return true;
        } catch (Exception e) {
            log.error("All trigger methods failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Triggers a geofence EXIT event.
     * Convenience method for common use case.
     *
     * @param driver The Appium driver
     * @return true if successful
     */
    public static boolean triggerExitEvent(AppiumDriver driver) {
        return triggerGeofenceEvent(driver, GEOFENCE_EXIT);
    }

    /**
     * Triggers a geofence ENTER event.
     * Convenience method for common use case.
     *
     * @param driver The Appium driver
     * @return true if successful
     */
    public static boolean triggerEnterEvent(AppiumDriver driver) {
        return triggerGeofenceEvent(driver, GEOFENCE_ENTER);
    }

    private static String getTransitionName(int transition) {
        return switch (transition) {
            case GEOFENCE_ENTER -> "GEOFENCE_TRANSITION_ENTER";
            case GEOFENCE_EXIT -> "GEOFENCE_TRANSITION_EXIT";
            default -> "UNKNOWN(" + transition + ")";
        };
    }
}
