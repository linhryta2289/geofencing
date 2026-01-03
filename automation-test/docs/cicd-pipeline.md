# CI/CD Pipeline Documentation

## Overview

Jenkins declarative pipeline for automated mobile test execution on BrowserStack with Allure reporting and auto-defect creation via Xray.

**File:** `Jenkinsfile` (project root)

## Pipeline Stages

### 1. Checkout
```groovy
checkout scm
```
Clone repository source code.

### 2. Build
```bash
cd automation-test
./mvnw clean compile -q
```
Maven compilation. Silent mode (-q) to reduce log noise.

### 3. API Tests
**Command:**
```bash
./mvnw test -DsuiteXml=testng-api.xml \
    -Dallure.results.directory=target/allure-results
```

**TestNG Suite:** `testng-api.xml`
- RestAssured + WireMock tests
- Backend validation
- Produces JUnit XML reports

**Post-Action:** Publish JUnit results
```groovy
junit allowEmptyResults: true,
      testResults: 'automation-test/target/surefire-reports/*.xml'
```

### 4. Android Tests
**Command:**
```bash
./mvnw test -Dplatform=android \
    -Denvironment=browserstack \
    -DsuiteXml=testng-android.xml \
    -Dbrowserstack.build=Geofence-${BUILD_TAG} \
    -Dallure.results.directory=target/allure-results
```

**Parameters:**
- `platform=android` - Target Android platform
- `environment=browserstack` - Use BrowserStack driver
- `browserstack.build=Geofence-{BUILD_TAG}` - Build identifier for session tracking
- `allure.results.directory` - Allure JSON output path

**BrowserStack Free Plan Constraint:** 1 parallel session → sequential execution

**Post-Action:** JUnit integration

### 5. iOS Tests
**Command:**
```bash
./mvnw test -Dplatform=ios \
    -Denvironment=browserstack \
    -DsuiteXml=testng-ios.xml \
    -Dbrowserstack.build=Geofence-${BUILD_TAG} \
    -Dallure.results.directory=target/allure-results
```

Same as Android, platform=ios.

### 6. Generate Report
**Plugin:** Allure Jenkins Plugin
```groovy
allure([
    includeProperties: true,
    reportBuildPolicy: 'ALWAYS',
    results: [[path: 'automation-test/target/allure-results']]
])
```

**Output:** Allure HTML report accessible via Jenkins build page

**Report Contents:**
- Test results with screenshots
- Failure categorization (6 categories)
- Environment info
- Timings and history

### 7. Create Jira Tickets (Conditional)
**Trigger:** `currentBuild.result == 'UNSTABLE' || 'FAILURE'`

**Script:** `scripts/create-xray-defects.sh`

**Process:**
1. Parse Allure JSON results (`*-result.json`)
2. Count failed/broken tests
3. Invoke Claude Code CLI with Atlassian MCP
4. Create Jira Bug issues per failure with:
   - Summary: `[AUTOMATION] {TestName} failed on {Platform}`
   - Description: Error, stack trace, steps to reproduce
   - Labels: automation, geofence, {platform}, regression
   - Priority: P1 (critical tests), P2 (normal tests)
   - Attachments: Screenshots if available

**Fallback:** If jq unavailable, count from TestNG XMLs

## Environment Variables

| Variable | Source | Usage |
|----------|--------|-------|
| `BROWSERSTACK_USERNAME` | Jenkins Credentials | BrowserStack auth |
| `BROWSERSTACK_ACCESS_KEY` | Jenkins Credentials | BrowserStack auth |
| `JIRA_PROJECT` | Pipeline env | Xray defect project (default: GEOFENCE) |
| `BUILD_TAG` | Computed | `{BUILD_NUMBER}-{timestamp}` e.g., `42-20251222-1430` |
| `MAVEN_OPTS` | Pipeline env | `-Xmx1024m` memory limit |

## Pipeline Options

```groovy
options {
    timeout(time: 45, unit: 'MINUTES')      // Total pipeline timeout
    buildDiscarder(logRotator(numToKeepStr: '10'))  // Keep last 10 builds
    timestamps()                            // Timestamp log lines
    ansiColor('xterm')                      // Color console output
}
```

## Artifact Management

**Archived per Build:**
```
automation-test/target/allure-results/**
automation-test/target/surefire-reports/**
```

**Retention:** Last 10 builds (configurable via `logRotator`)

**Cleanup:** `cleanWs cleanWhenSuccess: true` removes workspace after successful build

## Post-Build Actions

### Always
- Archive artifacts (Allure, JUnit)
- Clean workspace (on success)

### Success
- Log success message

### Failure
- Log failure message

### Unstable (Test Failures)
- Log unstable message
- Trigger Jira defect creation

## Reporting Configuration Files

### `allure.properties`
```properties
allure.results.directory=target/allure-results
allure.link.issue.pattern=https://jira.example.com/browse/{}
allure.link.tms.pattern=https://jira.example.com/browse/{}
allure.testng.hideTestStarted=false
allure.results.environment.reportName=Geofence Automation Report
allure.results.environment.browserstack=true
allure.results.environment.framework=Appium 9.3.0 + TestNG 7.10
```

### `environment.properties`
Displayed in Allure report "Environment" tab:
```properties
Framework=Appium 9.3.0 + Java 17
TestRunner=TestNG 7.10.2
Reporting=Allure 2.29.0
Cloud=BrowserStack App Automate
API=RestAssured 5.5.0 + WireMock
Platforms=iOS + Android
```

### `categories.json`
Failure categorization rules:

| Category | Conditions | Purpose |
|----------|-----------|---------|
| Geofence Trigger Failures | Failed/broken + "geofence" in message | Feature-specific |
| Location Accuracy Issues | Failed/broken + "location" in message | Feature-specific |
| Notification Capture Failures | Failed/broken + "notification" in message | Feature-specific |
| API Failures | Failed/broken + "API" or "status" in message | Backend validation |
| Infrastructure Issues | Broken + "timeout"/"connection"/"BrowserStack" | System-level |
| Test Defects | Failed + "AssertionError" in trace | Test code issues |

### `executor.json`
Jenkins executor metadata (populated at runtime):
```json
{
  "name": "Jenkins",
  "type": "jenkins",
  "url": "${BUILD_URL}",
  "buildOrder": "${BUILD_NUMBER}",
  "buildName": "Geofence-${BUILD_TAG}",
  "reportUrl": "${BUILD_URL}allure",
  "reportName": "Allure Report"
}
```

## Jenkins Setup

### Prerequisites
- Jenkins 2.x+
- Maven plugin
- Allure Jenkins plugin
- Git plugin
- JUnit plugin (bundled)

### Configuration Steps

1. **Create Pipeline Job**
   - Type: Pipeline
   - Pipeline script from SCM
   - SCM: Git
   - Script path: `Jenkinsfile`

2. **Add Credentials**
   - Kind: Username and password
   - Username: `browserstack-username` (actual username)
   - Password: `browserstack-access-key` (actual access key)
   - ID: `browserstack-username` and `browserstack-access-key`

3. **Configure Build Triggers**
   - Poll SCM (optional): `H H * * *`
   - Webhook (recommended): GitHub/GitLab push trigger

4. **Install Required Plugins**
   - Allure Jenkins Plugin
   - Pipeline plugin (Pipeline: Declarative Agent)

## Troubleshooting

### Build Timeout
- Default: 45 minutes
- Check BrowserStack session creation (slow on free plan)
- Increase if needed: `timeout(time: 60, unit: 'MINUTES')`

### Allure Report Not Generated
- Verify `target/allure-results/` directory exists
- Check Java home path in Jenkins configuration

### Defect Creation Fails
- Verify Claude Code CLI installed on Jenkins agent
- Check Atlassian MCP configuration
- Verify Jira credentials and project access

### JUnit Results Not Published
- Confirm TestNG output in `target/surefire-reports/*.xml`
- Check file permissions

## Performance Optimization

| Optimization | Impact | Notes |
|-------------|--------|-------|
| Parallel API tests | Time savings | Separate from mobile tests |
| BrowserStack parallel session | Not available | Free plan limitation |
| Maven quiet mode (-q) | Log reduction | Reduces artifact size |
| WireMock mocking | Faster API tests | Reduces external dependency |
| Cached dependencies | Faster builds | Configure Maven settings.xml |

## Scaling Considerations

**Current Limitations:**
- BrowserStack Free Plan: 1 parallel session → sequential execution
- Pipeline duration: ~15-20 min (API + Android + iOS)

**For Parallel Execution:**
- Upgrade BrowserStack plan (Pro: 5 parallel sessions)
- Restructure Jenkinsfile to use parallel stages
- Example: `parallel { stage('Android') { } stage('iOS') { } }`

**Recommended:**
```groovy
stage('Mobile Tests') {
    parallel {
        stage('Android') { /* Android stage */ }
        stage('iOS') { /* iOS stage */ }
    }
}
```

## Security Considerations

- Credentials stored in Jenkins credential store (encrypted)
- Environment variables not exposed in logs
- `MAVEN_OPTS` limited to prevent resource abuse
- Artifacts cleaned up after build (successful builds)
- Allure report accessible via Jenkins authentication
