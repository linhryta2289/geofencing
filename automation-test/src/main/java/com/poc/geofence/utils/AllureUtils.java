package com.poc.geofence.utils;

import com.poc.geofence.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Utility class for Allure reporting attachments.
 */
public class AllureUtils {
    private static final Logger log = LoggerFactory.getLogger(AllureUtils.class);

    private AllureUtils() {
        // Utility class
    }

    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachScreenshot(String name) {
        if (!DriverManager.hasDriver()) {
            log.debug("Skipping screenshot - no driver (API test)");
            return new byte[0];
        }
        try {
            AppiumDriver driver = DriverManager.getDriver();
            byte[] screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
            log.debug("Attached screenshot: {}", name);
            return screenshot;
        } catch (Exception e) {
            log.warn("Failed to capture screenshot: {} - {}", name, e.getMessage());
            return new byte[0];
        }
    }

    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        return content;
    }

    public static void attachPageSource(String name) {
        if (!DriverManager.hasDriver()) {
            log.debug("Skipping page source - no driver (API test)");
            return;
        }
        try {
            String pageSource = DriverManager.getDriver().getPageSource();
            Allure.addAttachment(name, "application/xml",
                    new ByteArrayInputStream(pageSource.getBytes()), "xml");
        } catch (Exception e) {
            log.warn("Failed to attach page source: {} - {}", name, e.getMessage());
        }
    }

    public static void step(String stepDescription) {
        Allure.step(stepDescription);
    }

    public static void addLink(String name, String url) {
        Allure.link(name, url);
    }
}
