# Reporting Guide

## Overview

Three-layer reporting system: Allure (test results), TestNG (JUnit), and Jira (defect tracking).

## Allure Reporting

### Configuration

**Properties File:** `src/test/resources/allure.properties`

```properties
allure.results.directory=target/allure-results
allure.link.issue.pattern=https://jira.example.com/browse/{}
allure.link.tms.pattern=https://jira.example.com/browse/{}
allure.testng.hideTestStarted=false
allure.results.environment.reportName=Geofence Automation Report
allure.results.environment.browserstack=true
allure.results.environment.framework=Appium 9.3.0 + TestNG 7.10
```

### Report Structure

```
target/allure-results/
├── *-result.json          # Individual test results (TestNG)
├── categories.json        # Failure categorization rules
├── environment.properties # Environment display metadata
├── executor.json          # Build executor info (Jenkins)
└── *-attachment.*         # Screenshots, logs, etc.
```

### Allure Annotations

Use in test code for richer reports:

```java
@Test
@Description("Verify geofence trigger notification")
@Severity(CRITICAL)
@TmsLink("GEO-123")
@Feature("Geofence Triggering")
@Story("User Location Detection")
public void testGeofenceTrigger() {
    // test code
}
```

**Annotation Fields:**
- `@Feature` - Feature grouping
- `@Story` - Story under feature
- `@Severity` - BLOCKER, CRITICAL, NORMAL, MINOR, TRIVIAL
- `@TmsLink` - Test management system ID (Jira)
- `@Issue` - Bug/issue tracking ID
- `@Description` - Test description

### Failure Categories

**File:** `src/test/resources/categories.json`

Categories auto-group failures by root cause:

| Category | Pattern | Status | Purpose |
|----------|---------|--------|---------|
| Geofence Trigger Failures | Message contains "geofence" | Failed/Broken | Feature-level bugs |
| Location Accuracy Issues | Message contains "location" | Failed/Broken | Location detection issues |
| Notification Capture Failures | Message contains "notification" | Failed/Broken | Notification system issues |
| API Failures | Message contains "API" or "status" | Failed/Broken | Backend validation issues |
| Infrastructure Issues | "timeout"/"connection"/"BrowserStack" | Broken | System-level problems |
| Test Defects | Trace contains "AssertionError" | Failed | Test code issues |

**Example Failure Categorization in Report:**
```
Summary: 12 failed/broken tests
├── Geofence Trigger Failures (4)
├── Location Accuracy Issues (3)
├── Infrastructure Issues (2)
├── API Failures (2)
├── Test Defects (1)
└── Notification Capture Failures (0)
```

### Environment Info

**File:** `src/test/resources/environment.properties`

Displayed in Allure "Environment" tab:

```properties
Framework=Appium 9.3.0 + Java 17
TestRunner=TestNG 7.10.2
Reporting=Allure 2.29.0
Cloud=BrowserStack App Automate
API=RestAssured 5.5.0 + WireMock
Platforms=iOS + Android
```

Add custom properties:

```properties
Browser=Chrome
OS=Windows 10
Java=17.0.2
Maven=3.9.0
BuildNumber=42
```

### Build Executor

**File:** `src/test/resources/executor.json`

Metadata about test execution environment:

```json
{
  "name": "Jenkins",
  "type": "jenkins",
  "url": "https://jenkins.example.com/job/geofence-automation/42/",
  "buildOrder": "42",
  "buildName": "Geofence-42-20251222-1430",
  "buildUrl": "https://jenkins.example.com/job/geofence-automation/42/",
  "reportUrl": "https://jenkins.example.com/job/geofence-automation/42/allure/",
  "reportName": "Allure Report"
}
```

### Local Report Generation

**Serve Allure Report Locally:**
```bash
./mvnw allure:serve
```
Opens at `http://localhost:4040` with file watcher (regenerates on save).

**Generate Static Report:**
```bash
./mvnw allure:report
# Output: target/site/allure-report/index.html
```

**Clear Previous Results:**
```bash
rm -rf target/allure-results/
```

### Report Navigation

**Tabs:**
1. **Overview** - Summary, failures, timings
2. **Categories** - Grouped failures by root cause
3. **Suites** - TestNG suite hierarchy
4. **Graphs** - Duration, status trends
5. **Timeline** - Chronological execution
6. **Behaviors** - Feature/Story organization
7. **Environment** - Execution environment
8. **History** - Trend across builds (Jenkins)

## TestNG/JUnit Reporting

### Configuration

**TestNG XML Suite:** `testng-android.xml`, `testng-ios.xml`, `testng-api.xml`

```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-current.dtd">
<suite name="Geofence Android Tests">
    <test name="API Tests">
        <classes>
            <class name="com.poc.geofence.e2e.android.GeofenceE2ETest"/>
        </classes>
    </test>
</suite>
```

### Report Format

**Output Path:** `target/surefire-reports/`

**File Types:**
- `*.xml` - JUnit XML format (Jenkins reads)
- `*.txt` - Summary text

**Example XML:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite tests="12" failures="2" errors="0" skipped="0">
    <testcase classname="GeofenceE2ETest" name="testGeofenceTrigger" time="15.2"/>
    <testcase classname="GeofenceE2ETest" name="testLocationAccuracy" time="12.5">
        <failure type="AssertionError">Expected accuracy 50m, got 150m</failure>
    </testcase>
</testsuite>
```

### Jenkins Integration

**JUnit Publisher:**
```groovy
junit allowEmptyResults: true,
      testResults: 'automation-test/target/surefire-reports/*.xml'
```

**Displays in Jenkins:**
- Build page: Test results count
- Test report link: Drill-down per test
- Trend graph: Pass/fail over builds

## Jira/Xray Integration

### Auto-Defect Creation

**Trigger:** Pipeline failure or unstable status

**Script:** `scripts/create-xray-defects.sh`

**Process:**
1. Analyze Allure results (`target/allure-results/*-result.json`)
2. Extract failed/broken tests
3. For each failure, create Jira Bug:
   - Summary: `[AUTOMATION] {TestName} failed on {Platform}`
   - Description: Error message + stack trace + steps to reproduce
   - Labels: `automation`, `geofence`, `{platform}`, `regression`
   - Priority: P1 (critical tests), P2 (normal tests)
   - Attachments: Screenshots (if available)

**Example Issue Created:**
```
Type: Bug
Key: GEO-456
Summary: [AUTOMATION] testGeofenceTrigger failed on Android
Description:
  Test: testGeofenceTrigger
  Platform: Android
  Error: AssertionError: Geofence not triggered within 30s
  Stack trace: [...]
  Steps to reproduce:
    1. Launch app
    2. Enable location
    3. Move into geofence zone
    4. Wait for notification
  Environment: BrowserStack-Android-12
Labels: automation, geofence, android, regression
Priority: Critical (P1)
```

### Linking Tests to Jira

Use `@TmsLink` annotation:

```java
@Test
@TmsLink("GEO-123")
public void testGeofenceTrigger() {
    // Allure report will link to https://jira.example.com/browse/GEO-123
}
```

Configure pattern in `allure.properties`:
```properties
allure.link.tms.pattern=https://jira.example.com/browse/{}
```

### Manual Defect Creation

If auto-creation fails:

1. Open Allure report
2. Click failed test
3. Review error, stack trace, screenshots
4. Create Jira issue:
   - Use same summary format
   - Attach screenshot
   - Link to test case ID

## Artifacts Retention

### Jenkins Artifact Storage

**Archived per Build:**
```
automation-test/target/allure-results/**  /* Allure JSON results + attachments */
automation-test/target/surefire-reports/** /* JUnit XMLs */
```

**Retention Policy:**
- Keep last 10 builds
- Auto-cleanup on success
- Manual cleanup available

**Access:**
```
https://jenkins.example.com/job/geofence-automation/42/artifact/
```

### Local Backup

```bash
# Backup Allure results before cleanup
cp -r target/allure-results ~/backups/allure-results-{BUILD_NUMBER}
```

## Troubleshooting

### Allure Report Not Generated

**Check:**
1. Directory exists: `target/allure-results/`
2. Files present: `*-result.json` files
3. Maven plugin configured in `pom.xml`
4. Java home set in Jenkins

**Fix:**
```bash
./mvnw clean test allure:report
```

### Categories Not Applied

**Check:**
1. File exists: `src/test/resources/categories.json`
2. Valid JSON syntax
3. Patterns match failure messages (case-sensitive)

**Test Pattern:**
```bash
jq '.[] | select(.name == "Geofence Trigger Failures")' \
  src/test/resources/categories.json
```

### Jira Defects Not Created

**Check:**
1. Claude Code CLI installed: `which claude`
2. Atlassian MCP configured
3. Jira credentials available
4. Failure threshold met (must have failures)

**Debug:**
```bash
cd automation-test
chmod +x scripts/create-xray-defects.sh
./scripts/create-xray-defects.sh  # Run manually
```

### Missing Screenshots in Allure

**Ensure in test code:**
```java
@Attachment(value = "Screenshot", type = "image/png")
public byte[] takeScreenshot() {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
}

// In test:
takeScreenshot();
```

## Best Practices

1. **Annotate Tests**
   - Use `@Feature`, `@Story`, `@Severity`
   - Add `@TmsLink` for test case tracking
   - Keep descriptions short but clear

2. **Monitor Categories**
   - Review categorization trends
   - Update `categories.json` patterns quarterly
   - Track root cause improvements

3. **Defect Quality**
   - Review auto-created defects for accuracy
   - Merge duplicate defects
   - Close fixed issues promptly

4. **Report Access**
   - Share Allure links in standup
   - Archive important reports
   - Clean up old artifacts regularly

5. **Performance**
   - Keep screenshot file sizes < 100KB
   - Limit attachment count per test
   - Use efficient image formats (PNG/WebP)

## Links

- Allure Documentation: https://docs.qameta.io/allure/
- TestNG Documentation: https://testng.org/
- Jira REST API: https://developer.atlassian.com/cloud/jira/platform/rest/
- Jenkins Allure Plugin: https://plugins.jenkins.io/allure-jenkins-plugin/
