package com.poc.geofence.pages;

/**
 * Interface for geofence page operations.
 * Implemented separately for iOS and Android due to different UI interactions.
 */
public interface GeofencePage {
    /**
     * Creates a geofence with specified parameters.
     * iOS: Fills text fields and clicks Add button.
     * Android: Long-clicks on map at coordinates.
     */
    void createGeofence(double latitude, double longitude, int radius, String title);

    /**
     * Verifies geofence was created successfully.
     * iOS: Checks for "Data Added" alert.
     * Android: Verifies marker and circle on map.
     */
    boolean verifyGeofenceCreated();

    /**
     * Gets the geofence event text (enter/exit).
     * iOS: Alert text.
     * Android: Notification or toast text.
     */
    String getGeofenceEventText();

    /**
     * Handles location permission dialogs.
     */
    void handlePermissions();

    /**
     * Waits for geofence exit event to trigger.
     * @param timeoutSeconds Maximum wait time (geofence can be slow)
     * @return true if exit event detected
     */
    boolean waitForGeofenceExitEvent(int timeoutSeconds);

    /**
     * Waits for geofence entry event to trigger (child returns to safe zone).
     * iOS: Alert with "Entered" or "Enter" text.
     * Android: Notification with "GEOFENCE_TRANSITION_ENTER".
     * @param timeoutSeconds Maximum wait time (geofence can be slow)
     * @return true if entry event detected
     */
    boolean waitForGeofenceEntryEvent(int timeoutSeconds);
}
