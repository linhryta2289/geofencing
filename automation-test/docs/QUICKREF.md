# Quick Reference Guide

## Jenkins Pipeline

### URLs
- Build: `https://jenkins.example.com/job/geofence-automation/`
- Allure Report: `https://jenkins.example.com/job/geofence-automation/{BUILD_NUMBER}/allure/`

### Trigger Build
- Push to main/develop branch
- Or: Jenkins UI → "Build Now"

### Pipeline Stages (45 min timeout)
```
Checkout → Build → API Tests → Android Tests → iOS Tests → Report → Jira Tickets
```

### Credentials Required
```
Jenkins Manage Credentials:
- browserstack-username: [BrowserStack username]
- browserstack-access-key: [BrowserStack access key]
```

## Local Development

### Run Tests Locally

**API Tests:**
```bash
./mvnw test -DsuiteXml=testng-api.xml
```

**Android Tests:**
```bash
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key
./mvnw test -Dplatform=android -Denvironment=browserstack
```

**iOS Tests:**
```bash
./mvnw test -Dplatform=ios -Denvironment=browserstack
```

### Generate Report
```bash
./mvnw allure:serve  # Opens http://localhost:4040
```

### Clean Build
```bash
./mvnw clean compile
```

## Reporting

### Report Files
| File | Purpose |
|------|---------|
| `allure.properties` | Allure configuration |
| `categories.json` | Failure categorization |
| `environment.properties` | Execution environment info |
| `executor.json` | Jenkins executor metadata |

### Failure Categories
1. Geofence Trigger Failures
2. Location Accuracy Issues
3. Notification Capture Failures
4. API Failures
5. Infrastructure Issues
6. Test Defects

### Jira Defect Creation
- **Auto**: Pipeline creates on failure (via `scripts/create-xray-defects.sh`)
- **Manual**: Via Allure report
- **Format**: `[AUTOMATION] {TestName} failed on {Platform}`

## Configuration Files

### Environment Variables (Jenkins)
```
BROWSERSTACK_USERNAME    # From credentials
BROWSERSTACK_ACCESS_KEY  # From credentials
JIRA_PROJECT=GEOFENCE    # For defect creation
BUILD_TAG                # Auto: {BUILD_NUMBER}-{timestamp}
MAVEN_OPTS=-Xmx1024m     # Memory limit
```

### TestNG Suites
```
testng-api.xml      # API tests
testng-android.xml  # Android E2E tests
testng-ios.xml      # iOS E2E tests
```

## Artifact Retention

**Archived per Build:**
- `target/allure-results/**` - Allure JSON + screenshots
- `target/surefire-reports/**` - JUnit XMLs

**Retention:** Last 10 builds (configurable)

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build timeout | Increase timeout in Jenkinsfile (default 45 min) |
| Credentials not found | Add credentials in Jenkins Manage Credentials |
| No Allure report | Check `target/allure-results/` directory exists |
| Defects not created | Run `scripts/create-xray-defects.sh` manually |
| JUnit not published | Verify `target/surefire-reports/*.xml` files exist |

## Common Commands

```bash
# Clean and rebuild
./mvnw clean compile

# Run all tests
./mvnw test

# Run specific suite
./mvnw test -DsuiteXml=testng-api.xml

# Skip tests during build
./mvnw clean compile -DskipTests

# View logs
tail -f target/surefire-reports/testng-results.xml

# Clear Allure results
rm -rf target/allure-results/

# Build with custom build number
./mvnw test -Dbrowserstack.build=Geofence-CustomBuild
```

## Documentation Files

| File | Purpose |
|------|---------|
| `/docs/cicd-pipeline.md` | Detailed CI/CD pipeline docs |
| `/docs/reporting-guide.md` | Allure, TestNG, Jira reporting |
| `README.md` | Project overview & quick start |
| `Jenkinsfile` | Jenkins pipeline definition |
| `scripts/create-xray-defects.sh` | Auto-defect creation script |

## Links

- [Full CI/CD Docs](./cicd-pipeline.md)
- [Reporting Guide](./reporting-guide.md)
- [Allure Docs](https://docs.qameta.io/allure/)
- [TestNG Docs](https://testng.org/)
- [Appium Docs](https://appium.io/)
- [BrowserStack Docs](https://www.browserstack.com/docs)
