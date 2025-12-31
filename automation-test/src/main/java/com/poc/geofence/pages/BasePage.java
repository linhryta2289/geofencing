package com.poc.geofence.pages;

import com.poc.geofence.config.ConfigManager;
import com.poc.geofence.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.Duration;

/**
 * Abstract base page for all page objects.
 * Provides common methods for element interaction and waiting.
 */
public abstract class BasePage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final AppiumDriver driver;
    protected final WebDriverWait wait;
    protected final ConfigManager config;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.config = ConfigManager.getInstance();
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(config.getDefaultTimeout()));
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (WebDriverException e) {
            // Catches TimeoutException (subclass) and other WebDriver errors
            log.debug("Element not present: {} - {}", locator, e.getMessage());
            return false;
        }
    }

    protected void click(By locator) {
        WebElement element = waitForClickable(locator);
        element.click();
        log.debug("Clicked element: {}", locator);
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
        log.debug("Typed '{}' into element: {}", text, locator);
    }

    protected void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    protected String getText(WebElement element) {
        return element.getText();
    }

    protected void hideKeyboard() {
        try {
            // Use mobile: command for Appium 9.x compatibility
            driver.executeScript("mobile: hideKeyboard");
        } catch (Exception e) {
            log.debug("Keyboard already hidden or not present");
        }
    }

    public byte[] getScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public void attachScreenshot(String name) {
        byte[] screenshot = getScreenshot();
        Allure.addAttachment(name, "image/png",
                new ByteArrayInputStream(screenshot), "png");
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
