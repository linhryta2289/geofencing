package com.poc.geofence.utils;

import com.poc.geofence.config.ConfigManager;
import com.poc.geofence.config.PlatformType;
import com.poc.geofence.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Utility for simulating GPS location on mobile devices.
 * Supports both Android and iOS platforms using mobile: commands for Appium 9.x.
 */
public class LocationSimulator {
    private static final Logger log = LoggerFactory.getLogger(LocationSimulator.class);

    private LocationSimulator() {
        // Utility class
    }

    public static void setLocation(double latitude, double longitude) {
        AppiumDriver driver = DriverManager.getDriver();
        PlatformType platform = ConfigManager.getInstance().getPlatform();

        if (platform == PlatformType.ANDROID) {
            setAndroidLocation(driver, latitude, longitude);
        } else if (platform == PlatformType.IOS) {
            setIOSLocation(driver, latitude, longitude);
        } else {
            throw new IllegalStateException("Unknown platform: " + platform);
        }
    }

    private static void setAndroidLocation(AppiumDriver driver,
                                            double latitude, double longitude) {
        log.info("Setting Android location: {}, {}", latitude, longitude);

        // Cast to AndroidDriver for context switching and setLocation
        if (!(driver instanceof io.appium.java_client.android.AndroidDriver androidDriver)) {
            // Fallback to mobile: command if not AndroidDriver
            try {
                driver.executeScript("mobile: setLocation", Map.of(
                        "latitude", latitude,
                        "longitude", longitude,
                        "altitude", 0.0
                ));
                log.info("Location set via mobile: setLocation command");
            } catch (Exception e) {
                log.warn("setLocation failed: {}", e.getMessage());
            }
            return;
        }

        // Store current context to restore later
        String currentContext = null;
        try {
            currentContext = androidDriver.getContext();
        } catch (Exception e) {
            log.debug("Could not get current context: {}", e.getMessage());
        }

        try {
            // IMPORTANT: Switch to NATIVE_APP context before setLocation
            // This ensures location is set at device level, not just webview
            // Required for geofence triggers to work on BrowserStack
            try {
                androidDriver.context("NATIVE_APP");
                log.debug("Switched to NATIVE_APP context for location update");
            } catch (Exception e) {
                log.debug("Context switch not needed or failed: {}", e.getMessage());
            }

            // Set location
            androidDriver.setLocation(new org.openqa.selenium.html5.Location(latitude, longitude, 0.0));
            log.info("Location set via AndroidDriver.setLocation()");

            // Allow time for location to propagate to FusedLocationProvider
            Thread.sleep(2000);

        } catch (Exception e) {
            log.warn("setLocation failed: {}", e.getMessage());
            // Swallow exception - GPS might already be set via capabilities
        } finally {
            // Restore original context if needed
            if (currentContext != null && !currentContext.equals("NATIVE_APP")) {
                try {
                    androidDriver.context(currentContext);
                    log.debug("Restored context to: {}", currentContext);
                } catch (Exception e) {
                    log.debug("Could not restore context: {}", e.getMessage());
                }
            }
        }
    }

    private static void setIOSLocation(AppiumDriver driver,
                                        double latitude, double longitude) {
        log.info("Setting iOS location: {}, {}", latitude, longitude);
        // iOS uses different command
        driver.executeScript("mobile: setSimulatedLocation", Map.of(
                "latitude", latitude,
                "longitude", longitude
        ));
    }
}
