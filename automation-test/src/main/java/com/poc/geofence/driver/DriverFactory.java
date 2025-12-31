package com.poc.geofence.driver;

import com.poc.geofence.config.ConfigManager;
import com.poc.geofence.config.Environment;
import com.poc.geofence.config.PlatformType;
import com.poc.geofence.driver.capabilities.AndroidCapabilities;
import com.poc.geofence.driver.capabilities.IOSCapabilities;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * DriverFactory - Creates Appium drivers for iOS and Android platforms.
 */
public class DriverFactory {
    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    private static final String BROWSERSTACK_HUB = "https://hub.browserstack.com/wd/hub";
    private static final String LOCAL_HUB = "http://127.0.0.1:4723";

    private final ConfigManager config;

    public DriverFactory() {
        this.config = ConfigManager.getInstance();
    }

    /**
     * Creates a driver using configured platform and environment.
     * @return the initialized AppiumDriver
     */
    public AppiumDriver createDriver() {
        PlatformType platform = config.getPlatform();
        Environment environment = config.getEnvironment();
        return createDriver(platform, environment);
    }

    /**
     * Creates a driver for specified platform and environment.
     * @param platform the target platform (IOS or ANDROID)
     * @param environment the execution environment (LOCAL or BROWSERSTACK)
     * @return the initialized AppiumDriver
     */
    public AppiumDriver createDriver(PlatformType platform, Environment environment) {
        log.info("Creating {} driver for {} environment", platform.getValue(), environment.getValue());
        return switch (platform) {
            case IOS -> createIOSDriver(environment);
            case ANDROID -> createAndroidDriver(environment);
        };
    }

    private IOSDriver createIOSDriver(Environment environment) {
        IOSCapabilities caps = new IOSCapabilities()
                .withApp(config.getAppPath(PlatformType.IOS))
                .withDevice(
                        config.getProperty("device.ios.name", "iPhone 14"),
                        config.getProperty("device.ios.version", "16")
                );

        if (environment == Environment.BROWSERSTACK) {
            String username = config.getBrowserStackProperty("browserstack.username");
            String accessKey = config.getBrowserStackProperty("browserstack.accesskey");
            validateBrowserStackCredentials(username, accessKey);
            caps.withBrowserStack(username, accessKey);
        }

        try {
            String hubUrl = environment == Environment.BROWSERSTACK
                    ? BROWSERSTACK_HUB : LOCAL_HUB;
            log.info("Connecting to Appium hub: {}", hubUrl);
            return new IOSDriver(URI.create(hubUrl).toURL(), caps.build());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium hub URL", e);
        }
    }

    private AndroidDriver createAndroidDriver(Environment environment) {
        String appPath = config.getAppPath(PlatformType.ANDROID);
        AndroidCapabilities caps = new AndroidCapabilities()
                .withApp(appPath)
                .withDevice(
                        config.getProperty("device.android.name", "Google Pixel 7"),
                        config.getProperty("device.android.version", "13")
                );

        // Only set app package/activity for local apps, BrowserStack auto-detects from uploaded APK
        if (!appPath.startsWith("bs://")) {
            caps.withAppPackage(
                    config.getProperty("app.android.package", "com.eebax.geofencing"),
                    config.getProperty("app.android.activity", ".MapsActivity")
            );
        }

        if (environment == Environment.BROWSERSTACK) {
            String username = config.getBrowserStackProperty("browserstack.username");
            String accessKey = config.getBrowserStackProperty("browserstack.accesskey");
            validateBrowserStackCredentials(username, accessKey);
            caps.withBrowserStack(username, accessKey);
        }

        try {
            String hubUrl = environment == Environment.BROWSERSTACK
                    ? BROWSERSTACK_HUB : LOCAL_HUB;
            log.info("Connecting to Appium hub: {}", hubUrl);
            return new AndroidDriver(URI.create(hubUrl).toURL(), caps.build());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium hub URL", e);
        }
    }

    private void validateBrowserStackCredentials(String username, String accessKey) {
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException(
                    "BrowserStack username not configured. Set BROWSERSTACK_USERNAME env var or browserstack.username property.");
        }
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException(
                    "BrowserStack access key not configured. Set BROWSERSTACK_ACCESSKEY env var or browserstack.accesskey property.");
        }
    }
}
