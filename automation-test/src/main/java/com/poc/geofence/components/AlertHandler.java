package com.poc.geofence.components;

import com.poc.geofence.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Handles native alert dialogs for iOS.
 * iOS geofence app uses UIAlertController for enter/exit notifications.
 */
public class AlertHandler {
    private static final Logger log = LoggerFactory.getLogger(AlertHandler.class);

    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    public boolean isAlertPresent() {
        try {
            getDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    public boolean waitForAlert(int timeoutSeconds) {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getAlertText() {
        try {
            Alert alert = getDriver().switchTo().alert();
            String text = alert.getText();
            log.debug("Alert text: {}", text);
            return text;
        } catch (NoAlertPresentException e) {
            log.warn("No alert present");
            return "";
        }
    }

    public void acceptAlert() {
        try {
            Alert alert = getDriver().switchTo().alert();
            alert.accept();
            log.info("Accepted alert");
        } catch (NoAlertPresentException e) {
            log.warn("No alert to accept");
        }
    }
}
