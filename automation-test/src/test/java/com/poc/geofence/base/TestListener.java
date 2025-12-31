package com.poc.geofence.base;

import com.poc.geofence.driver.DriverManager;
import com.poc.geofence.utils.AllureUtils;
import com.poc.geofence.utils.JiraDefectCreator;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestNG listener for test lifecycle hooks, reporting, and auto defect creation.
 *
 * Features:
 * - Allure artifact attachment (screenshots, page source, error details)
 * - Automatic Jira defect creation on test failure (optional)
 * - Suite-level statistics logging
 */
public class TestListener implements ITestListener {
    private static final Logger log = LoggerFactory.getLogger(TestListener.class);
    private final JiraDefectCreator jiraDefectCreator;

    public TestListener() {
        this.jiraDefectCreator = new JiraDefectCreator();
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("========== Starting test: {} ==========", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("========== Test PASSED: {} ==========", result.getName());
        if (DriverManager.hasDriver()) {
            try {
                AllureUtils.attachScreenshot("Final State - Success");
            } catch (Exception e) {
                log.debug("Could not attach success screenshot: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("========== Test FAILED: {} ==========", result.getName());
        log.error("Error: {}", result.getThrowable().getMessage());

        // Attach failure artifacts to Allure report
        attachFailureArtifacts(result);

        // Auto-create Jira defect (if enabled)
        createJiraDefect(result);
    }

    /**
     * Attaches failure artifacts to Allure report.
     */
    private void attachFailureArtifacts(ITestResult result) {
        try {
            if (DriverManager.hasDriver()) {
                AllureUtils.attachScreenshot("Failure Screenshot");
                AllureUtils.attachPageSource("Page Source on Failure");
            }
            AllureUtils.attachText("Error Details", result.getThrowable().toString());
        } catch (Exception e) {
            log.debug("Could not attach failure artifacts: {}", e.getMessage());
        }
    }

    /**
     * Creates Jira defect for failed test using MCP or REST API fallback.
     */
    private void createJiraDefect(ITestResult result) {
        try {
            String issueKey = jiraDefectCreator.createDefectForFailure(result);
            if (issueKey != null) {
                AllureUtils.attachText("Jira Defect Created", issueKey);
                AllureUtils.addLink("Jira Defect", getJiraIssueUrl(issueKey));
            }
        } catch (Exception e) {
            log.warn("Jira defect creation failed: {}", e.getMessage());
        }
    }

    /**
     * Builds Jira issue URL from key.
     */
    private String getJiraIssueUrl(String issueKey) {
        String baseUrl = System.getenv("JIRA_BASE_URL");
        if (baseUrl != null) {
            return baseUrl + "/browse/" + issueKey;
        }
        return "https://jira.atlassian.net/browse/" + issueKey;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("========== Test SKIPPED: {} ==========", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("========== Test Suite Started: {} ==========", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========== Test Suite Finished: {} ==========", context.getName());
        log.info("Passed: {}, Failed: {}, Skipped: {}",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());

        // Cleanup executor service to prevent resource leak
        jiraDefectCreator.shutdown();
    }
}
