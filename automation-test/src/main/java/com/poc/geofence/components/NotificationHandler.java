package com.poc.geofence.components;

import com.poc.geofence.driver.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Handles Android notification panel interactions and Toast detection.
 * Used to detect geofence enter/exit notifications and Toast messages.
 */
public class NotificationHandler {
    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);
    private static final By NOTIFICATION_TEXT = AppiumBy.xpath(
            "//android.widget.TextView[contains(@resource-id, 'android:id/text') or " +
            "contains(@resource-id, 'android:id/title')]");

    // Toast detection - Android Toast appears as a TextView within Toast container
    private static final By TOAST_MESSAGE = AppiumBy.xpath(
            "//android.widget.Toast//android.widget.TextView | " +
            "//android.widget.Toast | " +
            "//*[contains(@class, 'Toast')]//android.widget.TextView");

    private AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    public void openNotificationPanel() {
        try {
            // Use swipe gesture from top - works on BrowserStack
            getDriver().executeScript("mobile: swipeGesture", Map.of(
                    "left", 500,
                    "top", 0,
                    "width", 100,
                    "height", 500,
                    "direction", "down",
                    "percent", 0.75
            ));
            sleep(1000);
            log.debug("Opened notification panel via swipe");
        } catch (Exception e) {
            log.warn("openNotifications via swipe failed: {}", e.getMessage());
        }
    }

    public void closeNotificationPanel() {
        try {
            // Swipe up to close notification panel
            getDriver().executeScript("mobile: swipeGesture", Map.of(
                    "left", 500,
                    "top", 500,
                    "width", 100,
                    "height", 500,
                    "direction", "up",
                    "percent", 0.75
            ));
            sleep(500);
        } catch (Exception e) {
            log.warn("closeNotifications via swipe failed: {}", e.getMessage());
        }
    }

    /**
     * Gets notification text using mobile: getNotifications command.
     * Falls back to UI scraping if command fails.
     */
    @SuppressWarnings("unchecked")
    public String getNotificationText() {
        // First try mobile: getNotifications (supported on BrowserStack)
        try {
            Object result = getDriver().executeScript("mobile: getNotifications");
            if (result instanceof List) {
                List<?> notifications = (List<?>) result;
                StringBuilder sb = new StringBuilder();
                for (Object item : notifications) {
                    if (item instanceof Map) {
                        Map<String, Object> notification = (Map<String, Object>) item;
                        // Extract text fields from notification
                        Object text = notification.get("text");
                        Object title = notification.get("title");
                        Object subText = notification.get("subText");
                        Object bigText = notification.get("bigText");
                        if (title != null) sb.append(title).append(" ");
                        if (text != null) sb.append(text).append(" ");
                        if (subText != null) sb.append(subText).append(" ");
                        if (bigText != null) sb.append(bigText).append(" ");
                    }
                }
                String text = sb.toString().trim();
                if (!text.isEmpty()) {
                    log.info("Notification text: {}", text);
                    return text;
                }
            }
        } catch (Exception e) {
            log.debug("getNotifications command result: {}", e.getMessage());
        }

        // Fallback: try UI scraping
        try {
            openNotificationPanel();
            sleep(500);
            List<WebElement> textElements = getDriver().findElements(NOTIFICATION_TEXT);
            StringBuilder sb = new StringBuilder();
            for (WebElement element : textElements) {
                try {
                    String text = element.getText();
                    if (text != null && !text.isEmpty()) {
                        sb.append(text).append(" ");
                    }
                } catch (Exception ignored) {}
            }
            String result = sb.toString().trim();
            log.info("Notification text from UI: {}", result.isEmpty() ? "(empty)" : result);
            closeNotificationPanel();
            return result;
        } catch (Exception e) {
            log.warn("getNotificationText from UI failed: {}", e.getMessage());
            closeNotificationPanel();
        }
        return "";
    }

    /**
     * Gets current Toast text if visible on screen.
     * Toast messages are short-lived (2-3 seconds), so this must be called quickly.
     */
    public String getToastText() {
        try {
            List<WebElement> toastElements = getDriver().findElements(TOAST_MESSAGE);
            for (WebElement element : toastElements) {
                try {
                    String text = element.getText();
                    if (text != null && !text.isEmpty()) {
                        log.info("Toast text: {}", text);
                        return text;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.debug("Toast detection failed: {}", e.getMessage());
        }

        // Fallback: Try to find any visible text containing our target patterns
        try {
            By anyText = AppiumBy.xpath("//android.widget.TextView[contains(@text, 'GEOFENCE') or contains(@text, 'Test:')]");
            List<WebElement> textElements = getDriver().findElements(anyText);
            for (WebElement element : textElements) {
                try {
                    String text = element.getText();
                    if (text != null && !text.isEmpty()) {
                        log.info("Found geofence-related text: {}", text);
                        return text;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.debug("Fallback text search failed: {}", e.getMessage());
        }

        return "";
    }

    /**
     * Gets the test status text from the app's visible UI element.
     * This is more reliable than Toast detection.
     */
    public String getAppStatusText() {
        try {
            // Look for the testStatusText TextView in the app
            By statusText = AppiumBy.id("com.eebax.geofencing:id/testStatusText");
            List<WebElement> elements = getDriver().findElements(statusText);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        String text = element.getText();
                        if (text != null && !text.isEmpty()) {
                            log.info("App status text: {}", text);
                            return text;
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.debug("App status text detection failed: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Waits for either a Toast, Notification, or visible app status text containing specified text.
     * Checks all sources on each poll.
     */
    public boolean waitForToastOrNotification(String containsText, int timeoutSeconds) {
        log.info("Waiting for Toast, Status, or Notification containing: '{}'", containsText);
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            // Check app status text first (most reliable - visible in UI)
            String statusText = getAppStatusText();
            if (statusText.contains(containsText)) {
                log.info("Found in App Status: {}", statusText);
                return true;
            }

            // Check Toast (might disappear quickly)
            String toastText = getToastText();
            if (toastText.contains(containsText)) {
                log.info("Found in Toast: {}", containsText);
                return true;
            }

            // Check notification panel less frequently
            String notificationText = getNotificationText();
            if (notificationText.contains(containsText)) {
                log.info("Found in Notification: {}", containsText);
                return true;
            }

            sleep(2000);
        }

        log.warn("Toast/Status/Notification not found within {}s", timeoutSeconds);
        return false;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
