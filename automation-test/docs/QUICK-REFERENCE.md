# Quick Reference Guide

**Project:** Geofence Mobile Automation POC
**Last Updated:** 2025-12-22

---

## Getting Started (5 minutes)

### Prerequisites
- Java 17+ JDK
- Maven 3.9+ (or use `./mvnw` wrapper)
- BrowserStack account with App Automate

### First Time Setup

```bash
# 1. Navigate to project
cd automation-test

# 2. Copy configuration templates
cp src/test/resources/config/config.properties.example src/test/resources/config/config.properties
cp src/test/resources/config/browserstack.properties.example src/test/resources/config/browserstack.properties

# 3. Set environment variables
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key

# 4. Build project
./mvnw clean compile

# 5. Verify setup
./mvnw --version
java -version
```

---

## Common Commands

### Build
```bash
./mvnw clean compile           # Compile source code
./mvnw clean verify            # Compile + Run tests
./mvnw clean package           # Create JAR package
```

### Run Tests
```bash
./mvnw test                    # All tests (default suite)
./mvnw test -Dplatform=android # Android tests
./mvnw test -Dplatform=ios     # iOS tests
./mvnw test -DsuiteXml=testng-api.xml  # API tests only
```

### Reporting
```bash
./mvnw allure:serve            # Generate and serve Allure report
./mvnw clean test allure:serve # Run tests and serve report
```

### Debugging
```bash
./mvnwDebug test               # Debug tests with IDE
./mvnw test -X                 # Maven debug output
```

---

## Project Structure at a Glance

```
automation-test/
├── pom.xml                    # Maven dependencies & build config
├── mvnw                       # Maven wrapper (use instead of 'mvn')
├── src/
│   ├── main/java/com/poc/geofence/
│   │   ├── config/            # Configuration management (Phase 02)
│   │   ├── driver/            # Driver factory (Phase 02)
│   │   ├── pages/             # Page Objects (Phase 02)
│   │   ├── components/        # Reusable components (Phase 02)
│   │   ├── utils/             # Utilities (Phase 02)
│   │   └── api/               # API clients (Phase 02)
│   └── test/
│       ├── java/com/poc/geofence/
│       │   ├── base/          # Base test classes (Phase 02)
│       │   ├── e2e/           # E2E tests (Phase 03)
│       │   ├── api/           # API tests (Phase 03)
│       │   └── data/          # Test data (Phase 02)
│       └── resources/
│           ├── config/        # Config files & examples
│           └── testdata/      # Test data (JSON files)
├── docs/                      # Documentation
└── plans/reports/             # Phase reports
```

---

## Configuration Quick Reference

### Core Config (`config.properties`)
```properties
environment=browserstack        # Environment: browserstack|local
platform=android               # Platform: android|ios
default.timeout=30             # Element wait timeout (seconds)
geofence.wait.timeout=120      # Geofence notification timeout (seconds)
app.ios.path=bs://YOUR_IOS_APP_ID
app.android.path=bs://YOUR_ANDROID_APP_ID
api.base.url=http://localhost:8080
api.timeout=10000
```

### Environment Variables (Required)
```bash
BROWSERSTACK_USERNAME=your_username
BROWSERSTACK_ACCESS_KEY=your_access_key
BUILD_NUMBER=123  # Optional, CI build number
```

### Test Data (`geofence-locations.json`)
```json
{
  "locations": {
    "bonn_center": {"latitude": 50.7333, "longitude": 7.1032},
    "bonn_exit": {"latitude": 50.7358, "longitude": 7.1032},
    "ahmedabad_mi": {"latitude": 23.057582, "longitude": 72.534458},
    "ahmedabad_exit": {"latitude": 23.060082, "longitude": 72.534458}
  }
}
```

---

## Technology Stack

| Tool | Version | Use |
|------|---------|-----|
| Appium | 9.3.0 | Mobile automation driver |
| TestNG | 7.10.2 | Test framework & execution |
| Java | 17 | Programming language |
| RestAssured | 5.5.0 | API testing |
| Allure | 2.29.0 | Test reporting |
| WireMock | 3.9.1 | API mocking |
| Maven | 3.9+ | Build tool |

---

## Naming Conventions (Key Rules)

### Classes
```java
LoginPage             // Page Objects: {Name}Page
GeofenceTest          // Tests: {Feature}Test
ConfigManager         // Managers: {Name}Manager
WaitUtils             // Utils: {Name}Utils
DriverFactory         // Factories: {Name}Factory
```

### Methods
```java
shouldEnterGeofenceWhenLocationMatches()  // Tests: should{Expect}When{Condition}
waitForElement()                          // Utils: {action}{Target}()
isElementVisible()                        // Boolean: is{State}()
clickLoginButton()                        // Actions: {verb}{Target}()
```

### Constants
```java
DEFAULT_TIMEOUT = 30;          // UPPER_SNAKE_CASE
GEOFENCE_RADIUS = 200;
ANDROID_HOME_BUTTON = "homeBtn";
```

---

## Code Organization

### Page Object Example
```java
public class LoginPage extends BasePage {
    private static final String EMAIL_FIELD = "emailInput";
    private static final String PASSWORD_FIELD = "passwordInput";
    private static final String LOGIN_BUTTON = "loginButton";

    public LoginPage(IOSDriver driver) {
        super(driver);
    }

    public HomePage login(String email, String password) {
        type(EMAIL_FIELD, email);
        type(PASSWORD_FIELD, password);
        click(LOGIN_BUTTON);
        return new HomePage(driver);
    }
}
```

### Test Structure (AAA Pattern)
```java
@Test
public void shouldEnterGeofenceWhenLocationMatches() {
    // Arrange - Setup
    GeofenceLocation location = testData.getLocation("bonn_center");

    // Act - Execute
    app.navigateTo(location);
    app.waitForGeofenceAlert(GEOFENCE_WAIT_TIMEOUT);

    // Assert - Verify
    assertThat(app.getGeofenceStatus()).isEqualTo("ENTERED");
}
```

---

## Useful Documentation Links

| Document | Purpose | When to Read |
|----------|---------|-------------|
| README.md | Quick start & setup | First time setup |
| docs/code-standards.md | Coding rules & conventions | Before writing code |
| docs/codebase-summary.md | Architecture overview | Understanding structure |
| docs/system-architecture.md | System design & components | Design phase, Phase 02 |
| docs/project-overview-pdr.md | Requirements & roadmap | Planning & leadership |

---

## Troubleshooting

### Build Issues
```bash
# Clear cache and rebuild
./mvnw clean compile -DskipTests

# Check Java version (must be 17+)
java -version

# Update dependencies
./mvnw clean dependency:resolve
```

### Test Execution Issues
```bash
# Run single test
./mvnw test -Dtest=GeofenceEntryTest

# Run with verbose output
./mvnw test -e

# Run without parallel execution
./mvnw test -DsuiteThreadPoolSize=1
```

### Configuration Issues
```bash
# Verify environment variables are set
echo $BROWSERSTACK_USERNAME
echo $BROWSERSTACK_ACCESS_KEY

# Check config file exists
cat src/test/resources/config/config.properties

# Verify app IDs in BrowserStack
# https://app-automate.browserstack.com/builds
```

---

## Commit Standards

### Commit Message Format
```
[TYPE] Brief description (50 chars max)

Detailed explanation if needed.
- Bullet point 1
- Bullet point 2

Fixes #123
```

### Commit Types
- `[FEAT]` New feature
- `[FIX]` Bug fix
- `[REFACTOR]` Code restructuring
- `[DOCS]` Documentation
- `[CONFIG]` Configuration
- `[DEPS]` Dependency updates
- `[TEST]` Test cases

### Example
```
[FEAT] Add geofence entry test for Android

- Test navigates to bonn_center location
- Validates geofence entry notification
- Verifies app status changes to ENTERED

Tests: GeofenceEntryTest.shouldEnterGeofenceWhenLocationMatches
Fixes #42
```

---

## IDE Setup (IntelliJ)

1. Open project: File → Open → select automation-test folder
2. Configure JDK: File → Project Structure → Project → Set SDK to Java 17+
3. Configure Maven: File → Settings → Build, Execution, Deployment → Maven
4. Set encoding: File → Settings → Editor → File Encodings → UTF-8
5. Enable inspection warnings: File → Settings → Editor → Inspections

---

## Before Submitting Code

- [ ] Code follows naming conventions
- [ ] Code compiles: `./mvnw clean compile`
- [ ] Tests pass: `./mvnw test`
- [ ] No hardcoded values (use constants)
- [ ] Documentation updated
- [ ] Commit message follows standards
- [ ] No security issues (credentials, etc.)

---

## Phase Roadmap at a Glance

```
Phase 01 ✓ COMPLETE (Dec 22)
├─ Maven setup
├─ Configuration system
├─ Test data
└─ Documentation

Phase 02 (Jan 2026)
├─ Page Objects
├─ Base classes
├─ Driver factory
└─ Utilities

Phase 03 (Feb 2026)
├─ E2E tests
├─ API tests
└─ Reporting

Phase 04 (Mar 2026)
├─ CI/CD pipeline
└─ Dashboards
```

---

## Key Contacts & Resources

**Documentation:** See `/docs` folder
**Config Templates:** `src/test/resources/config/`
**Test Data:** `src/test/resources/testdata/`
**BrowserStack:** https://app-automate.browserstack.com

---

## Quick Tips

1. **Always use `./mvnw`** - Don't install Maven locally, use wrapper
2. **Environment variables first** - Set BROWSERSTACK_USERNAME/KEY before running tests
3. **Check examples** - Reference *.example files for configuration
4. **Read standards early** - Start with code-standards.md before writing code
5. **Use Allure reports** - `./mvnw clean test allure:serve` for rich test results
6. **Keep it DRY** - Use Page Objects to avoid code duplication
7. **Explicit waits** - Never use `Thread.sleep()`, use WebDriverWait instead
8. **Meaningful assertions** - Use AssertJ: `assertThat(actual).isEqualTo(expected)`

---

## FAQ

**Q: How do I run Android tests?**
A: `./mvnw test -Dplatform=android`

**Q: How do I run iOS tests?**
A: `./mvnw test -Dplatform=ios`

**Q: Where are config files?**
A: `src/test/resources/config/` (copy `.example` files and edit)

**Q: How do I view test reports?**
A: `./mvnw allure:serve` after running tests

**Q: Do I need to install Maven?**
A: No, use `./mvnw` wrapper included in project

**Q: Where is test data?**
A: `src/test/resources/testdata/geofence-locations.json`

**Q: How do I add a new dependency?**
A: Edit `pom.xml`, then run `./mvnw dependency:resolve`

**Q: What's my Java version requirement?**
A: Java 17 minimum (JDK 17+)

---

**Last Updated:** 2025-12-22
**Next Update:** 2026-01-22
