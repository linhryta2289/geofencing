package com.poc.geofence.pages;

import com.poc.geofence.config.PlatformType;
import com.poc.geofence.pages.android.GeofencePageAndroid;
import com.poc.geofence.pages.ios.GeofencePageIOS;

/**
 * Factory for creating platform-specific page objects.
 * Uses Strategy pattern to resolve the correct implementation.
 */
public class PageFactory {
    private PageFactory() {
        // Utility class
    }

    public static GeofencePage getGeofencePage(PlatformType platform) {
        return switch (platform) {
            case IOS -> new GeofencePageIOS();
            case ANDROID -> new GeofencePageAndroid();
        };
    }
}
