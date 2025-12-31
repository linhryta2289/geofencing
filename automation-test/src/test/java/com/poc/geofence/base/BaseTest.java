package com.poc.geofence.base;

import com.poc.geofence.config.ConfigManager;
import com.poc.geofence.config.PlatformType;
import com.poc.geofence.driver.DriverFactory;
import com.poc.geofence.driver.DriverManager;
import com.poc.geofence.pages.GeofencePage;
import com.poc.geofence.pages.PageFactory;
import com.poc.geofence.utils.AllureUtils;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base test class with setup/teardown and common test infrastructure.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ConfigManager config;
    protected GeofencePage geofencePage;
    protected PlatformType platform;

    @BeforeSuite
    public void beforeSuite() {
        config = ConfigManager.getInstance();
        log.info("Test suite initialized");
        log.info("Environment: {}", config.getEnvironment());
        log.info("Default timeout: {}s", config.getDefaultTimeout());
    }

    @BeforeMethod
    @Parameters({"platform"})
    public void setUp(@Optional("") String platformParam) {
        // Ensure config is initialized (defensive - handles suite ordering issues)
        if (config == null) {
            config = ConfigManager.getInstance();
        }

        // Use parameter if provided, otherwise use config
        if (!platformParam.isEmpty()) {
            platform = PlatformType.fromString(platformParam);
        } else {
            platform = config.getPlatform();
        }

        log.info("Setting up test for platform: {}", platform);
        Allure.parameter("Platform", platform.getValue());

        // Create driver
        DriverFactory factory = new DriverFactory();
        AppiumDriver driver = factory.createDriver(platform, config.getEnvironment());
        DriverManager.setDriver(driver);

        // Initialize page object
        geofencePage = PageFactory.getGeofencePage(platform);

        // Handle initial permissions
        geofencePage.handlePermissions();
        try {
            AllureUtils.attachScreenshot("Initial State");
        } catch (Exception e) {
            log.debug("Could not attach initial screenshot: {}", e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                try {
                    AllureUtils.attachScreenshot("Final State - Failure");
                } catch (Exception e) {
                    log.debug("Could not attach failure screenshot: {}", e.getMessage());
                }
            }
        } finally {
            DriverManager.quitDriver();
            log.info("Test cleanup complete");
        }
    }

    @AfterSuite
    public void afterSuite() {
        log.info("Test suite completed");
    }

    /**
     * Utility sleep method for tests.
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
