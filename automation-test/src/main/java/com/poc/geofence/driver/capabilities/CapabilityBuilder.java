package com.poc.geofence.driver.capabilities;

import org.openqa.selenium.Capabilities;

/**
 * Builder interface for constructing platform-specific Appium capabilities.
 * Uses fluent builder pattern for readable capability configuration.
 *
 * @param <T> the concrete builder type for method chaining
 */
public interface CapabilityBuilder<T extends CapabilityBuilder<T>> {

    /**
     * Sets the application path or BrowserStack app ID.
     * @param appPath local path or bs:// URL
     * @return the builder instance
     */
    T withApp(String appPath);

    /**
     * Sets the target device and OS version.
     * @param deviceName the device name (e.g., "iPhone 14", "Google Pixel 7")
     * @param osVersion the OS version (e.g., "16", "13")
     * @return the builder instance
     */
    T withDevice(String deviceName, String osVersion);

    /**
     * Configures BrowserStack authentication and project settings.
     * @param username BrowserStack username
     * @param accessKey BrowserStack access key
     * @return the builder instance
     */
    T withBrowserStack(String username, String accessKey);

    /**
     * Builds and returns the configured capabilities.
     * @return Selenium Capabilities object
     */
    Capabilities build();
}
