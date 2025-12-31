package com.poc.geofence.driver.capabilities;

import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.Capabilities;
import com.poc.geofence.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for iOS-specific Appium capabilities.
 * Uses XCUITest automation engine and W3C-compliant bstack:options.
 */
public class IOSCapabilities implements CapabilityBuilder<IOSCapabilities> {
    private final XCUITestOptions options;
    private final Map<String, Object> bstackOptions;

    public IOSCapabilities() {
        this.options = new XCUITestOptions();
        this.bstackOptions = new HashMap<>();

        // Default XCUITest settings
        options.setAutomationName("XCUITest");
        options.setNewCommandTimeout(java.time.Duration.ofSeconds(300));
        options.setAutoAcceptAlerts(true);
    }

    @Override
    public IOSCapabilities withApp(String appPath) {
        if (appPath != null && appPath.startsWith("bs://")) {
            bstackOptions.put("app", appPath);
        } else if (appPath != null) {
            options.setApp(appPath);
        }
        return this;
    }

    @Override
    public IOSCapabilities withDevice(String deviceName, String osVersion) {
        bstackOptions.put("deviceName", deviceName);
        bstackOptions.put("osVersion", osVersion);
        bstackOptions.put("platformName", "ios");
        return this;
    }

    @Override
    public IOSCapabilities withBrowserStack(String username, String accessKey) {
        ConfigManager config = ConfigManager.getInstance();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("projectName", config.getBrowserStackProperty("browserstack.project"));
        bstackOptions.put("buildName", config.getBrowserStackProperty("browserstack.build"));
        bstackOptions.put("sessionName", "iOS Geofence Test");
        bstackOptions.put("debug", "true");
        bstackOptions.put("networkLogs", "true");
        bstackOptions.put("appiumVersion", "2.0.0");
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
