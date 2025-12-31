package com.poc.geofence.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for loading and accessing test configuration.
 * Supports system properties override and environment variable fallback for secrets.
 *
 * Priority order: System Property > Config Property > Default Value
 */
public class ConfigManager {
    private static volatile ConfigManager instance;
    private final Properties properties;
    private final Properties bsProperties;

    private ConfigManager() {
        properties = new Properties();
        bsProperties = new Properties();
        loadProperties();
    }

    /**
     * Returns the singleton instance using double-checked locking pattern.
     * Thread-safe initialization for parallel test execution.
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        // Load base config
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config/config.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }

        // Load environment-specific config (overrides base config)
        String apiMode = System.getProperty("api.mode", properties.getProperty("api.mode", "mock"));
        if (!apiMode.equals("mock")) {
            String envFile = "config/environments/" + apiMode + ".properties";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(envFile)) {
                if (is != null) {
                    properties.load(is);
                }
            } catch (IOException e) {
                // Environment-specific file optional
            }
        }

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config/browserstack.properties")) {
            if (is != null) {
                bsProperties.load(is);
            }
        } catch (IOException e) {
            // BrowserStack properties optional for local execution
        }
    }

    /**
     * Gets a property value with system property override support.
     * Empty system properties are treated as unset (uses config file value).
     * @param key the property key
     * @return the property value or null if not found
     */
    public String getProperty(String key) {
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isEmpty()) {
            return sysValue;
        }
        return properties.getProperty(key);
    }

    /**
     * Gets a property value with default fallback.
     * @param key the property key
     * @param defaultValue the default value if property not found
     * @return the property value or default
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a BrowserStack property with environment variable support.
     * Environment variables use uppercase with underscores (e.g., BROWSERSTACK_USERNAME).
     * @param key the property key
     * @return the property value from env var or properties file
     */
    public String getBrowserStackProperty(String key) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        return envValue != null ? envValue : bsProperties.getProperty(key);
    }

    /**
     * Returns the configured platform type (IOS or ANDROID).
     * Default: ANDROID
     */
    public PlatformType getPlatform() {
        return PlatformType.fromString(getProperty("platform", "android"));
    }

    /**
     * Returns the configured execution environment.
     * Default: BROWSERSTACK
     */
    public Environment getEnvironment() {
        return Environment.fromString(getProperty("environment", "browserstack"));
    }

    /**
     * Returns the default element wait timeout in seconds.
     * Default: 30
     */
    public int getDefaultTimeout() {
        return Integer.parseInt(getProperty("default.timeout", "30"));
    }

    /**
     * Returns the geofence trigger wait timeout in seconds.
     * Default: 120 (geofence events can take time)
     */
    public int getGeofenceWaitTimeout() {
        return Integer.parseInt(getProperty("geofence.wait.timeout", "120"));
    }

    /**
     * Returns the app path/URL for the specified platform.
     * For BrowserStack, this is the bs:// app ID.
     * @param platform the target platform
     * @return the app path or BrowserStack app ID
     */
    public String getAppPath(PlatformType platform) {
        return platform == PlatformType.IOS
                ? getProperty("app.ios.path")
                : getProperty("app.android.path");
    }

    // ==================== API Mode Configuration ====================

    /**
     * Returns the API mode: mock, staging, uat, or prod.
     * Default: mock (backward compatible with WireMock tests)
     */
    public String getApiMode() {
        return getProperty("api.mode", "mock");
    }

    /**
     * Returns true if running against a real API (staging/uat/prod).
     * When false, tests should use WireMock for mocking.
     */
    public boolean isRealApiMode() {
        return !getApiMode().equals("mock");
    }

    /**
     * Returns the API base URL based on current api.mode.
     * Priority: Environment variable > Property file > Default
     * Supports: mock, dev, test, uat, prod
     */
    public String getApiBaseUrl() {
        String mode = getApiMode();
        // Check env var first (format: API_BASE_URL_MODE)
        String envVarName = "API_BASE_URL_" + mode.toUpperCase();
        String envUrl = System.getenv(envVarName);
        if (envUrl != null && !envUrl.isEmpty()) {
            return envUrl;
        }
        // Fall back to property file (loaded from environments/{mode}.properties)
        return getProperty("api.base.url", "http://localhost:8080");
    }

    /**
     * Returns the API authentication token from environment variable.
     * Env var: API_AUTH_TOKEN
     * @throws IllegalStateException if real API mode but token is missing
     */
    public String getApiToken() {
        String token = System.getenv("API_AUTH_TOKEN");
        if (isRealApiMode() && (token == null || token.isEmpty())) {
            throw new IllegalStateException(
                "API_AUTH_TOKEN env var is required for " + getApiMode() + " mode");
        }
        return token;
    }

    /**
     * Returns true if current mode is production (read-only operations only).
     */
    public boolean isProdMode() {
        return getApiMode().equals("prod");
    }

    /**
     * Returns the API request timeout in milliseconds.
     */
    public int getApiTimeout() {
        return Integer.parseInt(getProperty("api.timeout", "10000"));
    }

    // ==================== Jira Integration Configuration ====================

    /**
     * Returns whether Jira integration is enabled.
     * Default: false
     */
    public boolean isJiraEnabled() {
        return Boolean.parseBoolean(getProperty("jira.enabled", "false"));
    }

    /**
     * Returns whether to auto-create defects on test failure.
     * Default: false
     */
    public boolean isCreateDefectsOnFailure() {
        return Boolean.parseBoolean(getProperty("jira.create.defects.on.failure", "false"));
    }

    /**
     * Returns the Jira base URL from environment variable.
     * Env var: JIRA_BASE_URL
     */
    public String getJiraBaseUrl() {
        return System.getenv("JIRA_BASE_URL");
    }

    /**
     * Returns the Jira username/email from environment variable.
     * Env var: JIRA_USERNAME
     */
    public String getJiraUsername() {
        return System.getenv("JIRA_USERNAME");
    }

    /**
     * Returns the Jira API token from environment variable.
     * Env var: JIRA_API_TOKEN
     */
    public String getJiraApiToken() {
        return System.getenv("JIRA_API_TOKEN");
    }

    /**
     * Returns the Jira project key from environment variable or config.
     * Env var: JIRA_PROJECT (priority) or config property jira.project
     */
    public String getJiraProject() {
        String envValue = System.getenv("JIRA_PROJECT");
        return envValue != null ? envValue : getProperty("jira.project", "GEOFENCE");
    }

    /**
     * Resets the singleton instance (for testing purposes only).
     */
    public static void resetInstance() {
        instance = null;
    }
}
