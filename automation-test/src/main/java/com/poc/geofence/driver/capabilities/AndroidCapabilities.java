package com.poc.geofence.driver.capabilities;

import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.Capabilities;
import com.poc.geofence.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for Android-specific Appium capabilities.
 * Uses UiAutomator2 automation engine and W3C-compliant bstack:options.
 */
public class AndroidCapabilities implements CapabilityBuilder<AndroidCapabilities> {
    private final UiAutomator2Options options;
    private final Map<String, Object> bstackOptions;

    public AndroidCapabilities() {
        this.options = new UiAutomator2Options();
        this.bstackOptions = new HashMap<>();

        // Default UiAutomator2 settings
        options.setAutomationName("UiAutomator2");
        options.setNewCommandTimeout(java.time.Duration.ofSeconds(300));
        options.setAutoGrantPermissions(true);
    }

    @Override
    public AndroidCapabilities withApp(String appPath) {
        if (appPath != null) {
            // App path goes at root level, not inside bstack:options
            options.setApp(appPath);
        }
        return this;
    }

    @Override
    public AndroidCapabilities withDevice(String deviceName, String osVersion) {
        bstackOptions.put("deviceName", deviceName);
        bstackOptions.put("osVersion", osVersion);
        bstackOptions.put("platformName", "android");
        return this;
    }

    @Override
    public AndroidCapabilities withBrowserStack(String username, String accessKey) {
        ConfigManager config = ConfigManager.getInstance();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("projectName", config.getBrowserStackProperty("browserstack.project"));
        bstackOptions.put("buildName", config.getBrowserStackProperty("browserstack.build"));
        bstackOptions.put("sessionName", "Android Geofence Test");
        bstackOptions.put("debug", "true");
        bstackOptions.put("networkLogs", "true");
        bstackOptions.put("appiumVersion", "2.6.0");  // Updated for better location support
        return this;
    }

    /**
     * Sets the Android app package and activity.
     * @param appPackage the package name (e.g., "com.eebax.geofencing")
     * @param appActivity the main activity (e.g., ".MapsActivity")
     * @return the builder instance
     */
    public AndroidCapabilities withAppPackage(String appPackage, String appActivity) {
        options.setAppPackage(appPackage);
        options.setAppActivity(appActivity);
        return this;
    }

    @Override
    public Capabilities build() {
        if (!bstackOptions.isEmpty()) {
            options.setCapability("bstack:options", bstackOptions);
        }
        return options;
    }
}
