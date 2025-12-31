package com.poc.geofence.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestNG retry analyzer for flaky tests.
 * Retries failed tests up to MAX_RETRY_COUNT times.
 * Thread-safe: uses test result attribute to track retry count per test instance.
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2;
    private static final String RETRY_COUNT_ATTR = "retryCount";

    @Override
    public boolean retry(ITestResult result) {
        int retryCount = getRetryCount(result);
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            setRetryCount(result, retryCount);
            log.info("Retrying test '{}' - attempt {}/{}",
                    result.getName(), retryCount, MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }

    private int getRetryCount(ITestResult result) {
        Object count = result.getAttribute(RETRY_COUNT_ATTR);
        return count == null ? 0 : (int) count;
    }

    private void setRetryCount(ITestResult result, int count) {
        result.setAttribute(RETRY_COUNT_ATTR, count);
    }
}
