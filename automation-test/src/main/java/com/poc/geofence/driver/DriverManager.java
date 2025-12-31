package com.poc.geofence.driver;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe driver manager using ThreadLocal for parallel test execution.
 * Ensures each test thread has its own isolated driver instance.
 */
public class DriverManager {
    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {
        // Private constructor - utility class
    }

    /**
     * Gets the driver for the current thread.
     * @return the AppiumDriver instance
     * @throws IllegalStateException if driver not initialized
     */
    public static AppiumDriver getDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Call setDriver() first.");
        }
        return driver;
    }

    /**
     * Sets the driver for the current thread.
     * @param driver the AppiumDriver to store
     * @throws IllegalArgumentException if driver is null
     */
    public static void setDriver(AppiumDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }
        driverThreadLocal.set(driver);
        log.info("Driver set for thread: {}", Thread.currentThread().getName());
    }

    /**
     * Quits and removes the driver for the current thread.
     * Always called in test cleanup to prevent resource leaks.
     */
    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Driver quit for thread: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.error("Error quitting driver", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * Checks if a driver exists for the current thread.
     * @return true if driver is initialized
     */
    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
}
