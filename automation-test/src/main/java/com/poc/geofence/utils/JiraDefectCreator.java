package com.poc.geofence.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poc.geofence.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for automatic Jira defect creation on test failures.
 *
 * Strategy:
 * 1. Try MCP path via shell script (preferred for OAuth)
 * 2. Fallback to direct Jira REST API if MCP fails
 *
 * Security:
 * - Uses Jackson ObjectMapper for safe JSON serialization (prevents injection)
 * - Credentials never logged
 * - Error responses sanitized before logging
 *
 * Performance:
 * - Async execution to avoid blocking test runner
 * - Configurable HTTP timeouts
 *
 * Environment Variables Required:
 * - JIRA_BASE_URL: Jira instance URL (e.g., https://your-domain.atlassian.net)
 * - JIRA_USERNAME: Jira username/email
 * - JIRA_API_TOKEN: Jira API token (NOT password)
 * - JIRA_PROJECT: Jira project key (e.g., GEOFENCE)
 */
public class JiraDefectCreator {
    private static final Logger log = LoggerFactory.getLogger(JiraDefectCreator.class);
    private static final int MCP_TIMEOUT_SECONDS = 30;
    private static final int HTTP_TIMEOUT_MS = 30000;

    private final ConfigManager config;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;

    public JiraDefectCreator() {
        this.config = ConfigManager.getInstance();
        this.objectMapper = new ObjectMapper();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "jira-defect-creator");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Creates a Jira defect for a failed test asynchronously.
     * Non-blocking to avoid delaying test execution.
     *
     * @param result TestNG test result containing failure info
     * @return Jira issue key if created, null otherwise
     */
    public String createDefectForFailure(ITestResult result) {
        if (result == null) {
            log.debug("Null test result - skipping defect creation");
            return null;
        }

        if (!config.isJiraEnabled()) {
            log.debug("Jira integration disabled - skipping defect creation");
            return null;
        }

        if (!config.isCreateDefectsOnFailure()) {
            log.debug("Auto defect creation disabled - skipping");
            return null;
        }

        // Execute async to avoid blocking test runner
        CompletableFuture<String> future = CompletableFuture.supplyAsync(
                () -> createDefectSync(result), executor);

        try {
            // Wait with timeout - don't block indefinitely
            return future.get(MCP_TIMEOUT_SECONDS + HTTP_TIMEOUT_MS / 1000 + 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Async defect creation timeout or error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Synchronous defect creation - called from async wrapper.
     */
    private String createDefectSync(ITestResult result) {
        String issueKey = null;

        // Strategy 1: Try MCP path
        try {
            log.info("Attempting defect creation via MCP...");
            issueKey = createDefectViaMcp(result);
            if (issueKey != null) {
                log.info("Defect created via MCP: {}", issueKey);
                return issueKey;
            }
        } catch (Exception e) {
            log.warn("MCP defect creation failed: {}", e.getMessage());
        }

        // Strategy 2: Fallback to REST API
        try {
            log.info("Falling back to Jira REST API...");
            issueKey = createDefectViaRestApi(result);
            if (issueKey != null) {
                log.info("Defect created via REST API: {}", issueKey);
                return issueKey;
            }
        } catch (Exception e) {
            log.error("REST API defect creation failed: {}", e.getMessage());
        }

        log.error("Failed to create defect for test: {}", result.getName());
        return null;
    }

    /**
     * Attempts to create defect via Claude Code MCP script.
     */
    private String createDefectViaMcp(ITestResult result) throws Exception {
        String scriptPath = "scripts/create-xray-defects.sh";

        // Validate script exists
        java.io.File script = new java.io.File(scriptPath);
        if (!script.exists() || !script.canExecute()) {
            throw new RuntimeException("MCP script not found or not executable: " + scriptPath);
        }

        ProcessBuilder pb = new ProcessBuilder("bash", scriptPath);
        pb.environment().put("SINGLE_TEST_NAME", result.getName());
        pb.environment().put("SINGLE_TEST_ERROR", getErrorMessage(result));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Consume output stream to prevent blocking
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(MCP_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("MCP script timeout after " + MCP_TIMEOUT_SECONDS + "s");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("MCP script exited with code: " + process.exitValue());
        }

        // Parse output for issue key (format: PROJ-123)
        String[] lines = output.toString().split("\n");
        for (String line : lines) {
            if (line.matches(".*[A-Z]+-\\d+.*")) {
                return line.replaceAll(".*?([A-Z]+-\\d+).*", "$1");
            }
        }

        return null;
    }

    /**
     * Creates defect via direct Jira REST API using Jackson for safe JSON.
     * Uses Basic Auth with API token.
     */
    private String createDefectViaRestApi(ITestResult result) {
        String baseUrl = config.getJiraBaseUrl();
        String username = config.getJiraUsername();
        String apiToken = config.getJiraApiToken();
        String projectKey = config.getJiraProject();

        if (baseUrl == null || username == null || apiToken == null || projectKey == null) {
            log.error("Jira REST API credentials not configured");
            return null;
        }

        try {
            // Build JSON safely using Jackson ObjectMapper (prevents injection)
            ObjectNode requestBody = buildJiraRequestBody(result, projectKey);

            // Configure timeouts
            RestAssuredConfig restConfig = RestAssured.config()
                    .httpClient(HttpClientConfig.httpClientConfig()
                            .setParam("http.connection.timeout", HTTP_TIMEOUT_MS)
                            .setParam("http.socket.timeout", HTTP_TIMEOUT_MS));

            Response response = RestAssured.given()
                    .config(restConfig)
                    .baseUri(baseUrl)
                    .basePath("/rest/api/3/issue")
                    .auth().preemptive().basic(username, apiToken)
                    .contentType(ContentType.JSON)
                    .body(objectMapper.writeValueAsString(requestBody))
                    .when()
                    .post()
                    .then()
                    .extract().response();

            if (response.getStatusCode() == 201) {
                String issueKey = response.jsonPath().getString("key");
                log.info("Jira defect created: {}", issueKey);
                return issueKey;
            } else {
                // Sanitize error response - don't log sensitive data
                logSanitizedError(response);
                return null;
            }
        } catch (Exception e) {
            log.error("Jira REST API call failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Builds Jira issue request body using Jackson (safe JSON construction).
     */
    private ObjectNode buildJiraRequestBody(ITestResult result, String projectKey) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode fields = root.putObject("fields");

        // Project
        fields.putObject("project").put("key", projectKey);

        // Summary
        fields.put("summary", buildSummary(result));

        // Description (Atlassian Document Format)
        ObjectNode description = fields.putObject("description");
        description.put("type", "doc");
        description.put("version", 1);
        ArrayNode content = description.putArray("content");
        ObjectNode paragraph = content.addObject();
        paragraph.put("type", "paragraph");
        ArrayNode paragraphContent = paragraph.putArray("content");
        ObjectNode text = paragraphContent.addObject();
        text.put("type", "text");
        text.put("text", buildDescription(result));

        // Issue type
        fields.putObject("issuetype").put("name", "Bug");

        // Priority
        fields.putObject("priority").put("name", getPriority(result));

        // Labels
        ArrayNode labels = fields.putArray("labels");
        labels.add("automation");
        labels.add("mobile");
        labels.add("geofence");

        return root;
    }

    /**
     * Logs error response with sensitive data sanitized.
     */
    private void logSanitizedError(Response response) {
        int statusCode = response.getStatusCode();
        String body = response.getBody().asString();

        // Remove potential sensitive data patterns
        String sanitized = body
                .replaceAll("\"(password|token|key|secret|auth)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"[REDACTED]\"")
                .replaceAll("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", "Bearer [REDACTED]");

        // Truncate long responses
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500) + "...[truncated]";
        }

        log.error("Jira API returned {}: {}", statusCode, sanitized);
    }

    /**
     * Builds defect summary from test result.
     */
    private String buildSummary(ITestResult result) {
        String platform = config.getPlatform().toString().toLowerCase();
        return String.format("[AUTOMATION] %s - %s", result.getName(), platform);
    }

    /**
     * Builds defect description with test details.
     */
    private String buildDescription(ITestResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test Name: ").append(result.getName()).append("\n\n");
        sb.append("Platform: ").append(config.getPlatform()).append("\n\n");
        sb.append("Test Class: ").append(result.getTestClass().getName()).append("\n\n");
        sb.append("Error Message: ").append(getErrorMessage(result)).append("\n\n");
        sb.append("Stack Trace (first 500 chars):\n").append(getStackTrace(result, 500));
        sb.append("\n\nBuild: ").append(System.getenv("BUILD_NUMBER") != null
                ? System.getenv("BUILD_NUMBER") : "local");
        return sb.toString();
    }

    /**
     * Gets error message from test result.
     */
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable != null ? throwable.getMessage() : "Unknown error";
    }

    /**
     * Gets truncated stack trace.
     */
    private String getStackTrace(ITestResult result, int maxLength) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return "No stack trace available";
        }
        String trace = throwable.toString();
        return trace.length() > maxLength ? trace.substring(0, maxLength) + "..." : trace;
    }

    /**
     * Determines priority based on test annotations or defaults.
     */
    private String getPriority(ITestResult result) {
        try {
            var method = result.getMethod().getConstructorOrMethod().getMethod();
            var severity = method.getAnnotation(io.qameta.allure.Severity.class);
            if (severity != null) {
                return switch (severity.value()) {
                    case BLOCKER, CRITICAL -> "High";
                    case NORMAL -> "Medium";
                    case MINOR, TRIVIAL -> "Low";
                };
            }
        } catch (Exception e) {
            log.debug("Could not determine severity from annotations");
        }
        return "Medium";
    }

    /**
     * Shuts down the executor service gracefully.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
