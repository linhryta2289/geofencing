package com.poc.geofence.config;

/**
 * Enum representing test execution environments.
 * LOCAL - runs against local Appium server
 * BROWSERSTACK - runs against BrowserStack cloud devices
 */
public enum Environment {
    LOCAL("local"),
    BROWSERSTACK("browserstack");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string to Environment enum.
     * @param text the environment string (case-insensitive)
     * @return the corresponding Environment
     * @throws IllegalArgumentException if environment string is unknown
     */
    public static Environment fromString(String text) {
        for (Environment env : Environment.values()) {
            if (env.value.equalsIgnoreCase(text)) {
                return env;
            }
        }
        throw new IllegalArgumentException("Unknown environment: " + text);
    }
}
