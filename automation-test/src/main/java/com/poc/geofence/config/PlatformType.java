package com.poc.geofence.config;

/**
 * Enum representing supported mobile platforms for automation testing.
 * Used by DriverFactory to determine which driver type to create.
 */
public enum PlatformType {
    IOS("ios"),
    ANDROID("android");

    private final String value;

    PlatformType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string to PlatformType enum.
     * @param text the platform string (case-insensitive)
     * @return the corresponding PlatformType
     * @throws IllegalArgumentException if platform string is unknown
     */
    public static PlatformType fromString(String text) {
        for (PlatformType pt : PlatformType.values()) {
            if (pt.value.equalsIgnoreCase(text)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + text);
    }
}
