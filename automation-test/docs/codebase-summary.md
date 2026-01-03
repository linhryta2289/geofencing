# Automation Test - Codebase Summary

**Project:** Geofence Mobile Automation POC
**Last Updated:** 2025-12-25
**Version:** 1.0.0-SNAPSHOT
**Status:** Phase 08 - Real API Testing Implemented

## Overview

Mobile test automation framework for testing geofence functionality on iOS and Android platforms using Appium and BrowserStack. The framework is built on Maven with TestNG for test execution and Allure for reporting.

## Architecture

### Technology Stack

| Component | Version | Role |
|-----------|---------|------|
| **Appium** | 9.3.0 | Mobile automation driver (W3C protocol) |
| **TestNG** | 7.10.2 | Test framework & test execution |
| **Java** | 17 | Language & JDK |
| **Maven** | 3.9+ | Build & dependency management |
| **RestAssured** | 5.5.0 | API testing & validation |
| **Allure** | 2.29.0 | Test reporting & analytics |
| **WireMock** | 3.9.1 | API mocking for tests |

### Project Structure

```
automation-test/
├── pom.xml                      # Maven POM with all dependencies
├── mvnw / mvnwDebug            # Maven wrapper scripts (no install needed)
├── .mvn/wrapper/               # Maven wrapper configuration
├── src/
│   ├── main/java/com/poc/geofence/
│   │   ├── config/
│   │   │   ├── ConfigManager.java        # Configuration singleton (Phase 02)
│   │   │   ├── PlatformType.java         # Platform enum: IOS/ANDROID (Phase 02)
│   │   │   └── Environment.java          # Environment enum: LOCAL/BROWSERSTACK (Phase 02)
│   │   ├── driver/
│   │   │   ├── DriverFactory.java        # Appium driver creation factory (Phase 02)
│   │   │   ├── DriverManager.java        # ThreadLocal driver management (Phase 02)
│   │   │   └── capabilities/
│   │   │       ├── CapabilityBuilder.java     # Fluent builder interface (Phase 02)
│   │   │       ├── IOSCapabilities.java       # iOS capability builder (Phase 02)
│   │   │       └── AndroidCapabilities.java   # Android capability builder (Phase 02)
│   │   ├── pages/              # Page Objects (iOS/Android)
│   │   │   ├── BasePage.java              # Base page class with common methods (Phase 03)
│   │   │   ├── GeofencePage.java          # Interface for geofence page (Phase 03)
│   │   │   ├── PageFactory.java           # Factory for platform-specific pages (Phase 03)
│   │   │   ├── ios/
│   │   │   │   └── GeofencePageIOS.java   # iOS geofence page implementation (Phase 03)
│   │   │   └── android/
│   │   │       └── GeofencePageAndroid.java # Android geofence page implementation (Phase 03)
│   │   ├── components/         # Reusable UI components
│   │   │   ├── AlertHandler.java          # Alert/notification handling (Phase 03)
│   │   │   └── NotificationHandler.java   # Notification component (Phase 03)
│   │   ├── utils/              # Utilities & helpers
│   │   │   ├── LocationSimulator.java     # Location simulation utility (Phase 03)
│   │   │   └── WaitUtils.java             # Wait conditions & waits (Phase 03)
│   │   └── api/                # API clients [Phase 03+]
│   └── test/
│       ├── java/com/poc/geofence/
│       │   ├── config/         # Configuration tests (Phase 02)
│       │   │   ├── ConfigManagerTest.java
│       │   │   ├── PlatformTypeTest.java
│       │   │   └── EnvironmentTest.java
│       │   ├── driver/         # Driver tests (Phase 02)
│       │   │   └── DriverManagerTest.java
│       │   ├── base/           # Base test classes (Phase 04)
│       │   │   ├── BaseTest.java              # Abstract base test with setup/teardown
│       │   │   └── TestListener.java          # TestNG listener for test lifecycle
│       │   ├── e2e/            # End-to-end test suites (Phase 04)
│       │   │   ├── GeofenceExitTest.java      # Geofence exit detection tests (TC-001, TC-003)
│       │   │   └── GeofenceEntryTest.java     # Geofence entry detection tests - User Journey (TC-002, TC-004)
│       │   ├── api/            # API test suites (Phase 04)
│       │   │   └── GeofenceApiTest.java       # WireMock-based API tests (API-001, API-002)
│       │   └── data/           # Test data providers (Phase 04)
│       │       └── TestDataProvider.java      # DataProvider for geofence test locations
│       └── resources/
│           ├── config/         # Configuration files
│           │   ├── config.properties*
│           │   ├── config.properties.example
│           │   ├── browserstack.properties*
│           │   ├── browserstack.properties.example
│           │   ├── testng-unit.xml       # Unit test suite (Phase 02)
│           │   └── environments/
│           │       ├── dev.properties
│           │       └── staging.properties
│           ├── testdata/       # Test data (JSON)
│           └── allure.properties
├── docs/                       # Documentation
└── scripts/                    # CI/CD scripts
```

## Configuration System

### Files (Phase 01)

**Resource Configuration:**
- `config.properties.example` - Core test configuration template
- `browserstack.properties.example` - BrowserStack credentials template
- `config/environments/dev.properties` - Development environment settings
- `config/environments/staging.properties` - Staging environment settings
- `allure.properties` - Allure reporting configuration

**Security:**
- Real config files (`.properties`) are Git-ignored
- Examples provided for setup guidance
- Credentials loaded from environment variables

### Configuration Hierarchy

1. **Base Config** (`config.properties`)
   - Environment selection
   - Platform (android/ios)
   - Timeouts
   - App paths (BrowserStack app IDs)
   - API base URL

2. **BrowserStack Config** (`browserstack.properties`)
   - Username & access key (from env vars)
   - Device specifications
   - App capabilities

3. **Environment Configs** (`environments/*.properties`)
   - Environment-specific settings
   - API endpoints
   - Device pools

### Environment Variables

```bash
BROWSERSTACK_USERNAME     # Required: BrowserStack account username
BROWSERSTACK_ACCESS_KEY   # Required: BrowserStack access token
BUILD_NUMBER             # Optional: CI build identifier
```

## Test Data

### Geofence Locations (`testdata/geofence-locations.json`)

Test location data for geofence entry/exit scenarios:

**Android Locations:**
- `bonn_center`: 50.7333, 7.1032 (default app center)
- `bonn_exit`: 50.7358, 7.1032 (250m north - triggers exit)

**iOS Locations:**
- `ahmedabad_mi`: 23.057582, 72.534458 (default MI location)
- `ahmedabad_exit`: 23.060082, 72.534458 (250m north - triggers exit)

**Geofence Settings:**
- Default radius: 200m
- Exit trigger distance: 250m

## Phase 01 & 02 Deliverables

### Phase 01 - Completed (Foundation)

1. **Maven Build System**
   - Full dependency management
   - Maven wrapper for reproducible builds
   - Appium, TestNG, RestAssured, Allure, WireMock

2. **Configuration Management (Initial)**
   - Environment-based configuration template
   - BrowserStack integration template
   - Environment-specific property files
   - Secure credential handling via env vars

3. **Project Structure**
   - Standard Maven layout
   - Page Object Model ready
   - Test base classes preparation
   - API testing infrastructure

4. **Test Data**
   - Geofence test locations (Android & iOS)
   - Location data in JSON format
   - Entry/exit scenarios defined

5. **Documentation**
   - README with setup instructions
   - Quick start guide
   - Project structure overview
   - Tech stack documentation
   - Security guidelines

### Phase 02 - Completed (Framework Foundation)

1. **Configuration Management (Implemented)**
   - `ConfigManager` singleton with double-checked locking
   - System property override support
   - Environment variable fallback for BrowserStack credentials
   - Property hierarchy: System > Config > Default
   - Typed getters for platform, environment, timeouts, app paths, API settings

2. **Enums & Type Safety**
   - `PlatformType` enum (IOS/ANDROID) with string conversion
   - `Environment` enum (LOCAL/BROWSERSTACK) with string conversion
   - Case-insensitive enum parsing

3. **Driver Management**
   - `DriverFactory` - Creates iOS/Android drivers for both environments
   - `DriverManager` - ThreadLocal driver management for parallel tests
   - Supports both local Appium server (http://127.0.0.1:4723)
   - Supports BrowserStack cloud hub (https://hub.browserstack.com/wd/hub)
   - Thread-safe for parallel test execution

4. **Capabilities Builder (Fluent Pattern)**
   - `CapabilityBuilder` interface - Generic builder contract
   - `IOSCapabilities` - iOS-specific capability builder
   - `AndroidCapabilities` - Android-specific capability builder
   - Fluent API for readability
   - GPS location support for geofence testing
   - Device name & OS version configuration
   - BrowserStack authentication integration

5. **Unit Tests (Mockito)**
   - `ConfigManagerTest` - Property loading, override, env var fallback
   - `PlatformTypeTest` - Enum conversion, validation
   - `EnvironmentTest` - Enum conversion, validation
   - `DriverManagerTest` - ThreadLocal operations, null checks
   - Mockito mocking for isolated unit tests

6. **Test Infrastructure**
   - `testng-unit.xml` - Unit test suite configuration
   - Test resource configuration (config.properties in test resources)
   - Device configuration examples for testing

### Phase 03 - Completed (Page Objects Implementation)

1. **Page Objects Base Framework**
   - `BasePage` - Abstract base class with common page operations
   - `GeofencePage` - Interface defining geofence page contract:
     - `createGeofence(lat, lng, radius, title)` - Create geofence zone
     - `verifyGeofenceCreated()` - Verify creation success
     - `getGeofenceEventText()` - Get event alert/notification text
     - `handlePermissions()` - Handle location permission dialogs
     - `waitForGeofenceExitEvent(timeoutSeconds)` - Wait for exit detection
     - `waitForGeofenceEntryEvent(timeoutSeconds)` - Wait for entry detection (User Journey support)
   - `PageFactory` - Factory for creating platform-specific page objects
   - `GeofencePageIOS` - iOS-specific implementation:
     - TextFields for lat, lng, radius, title, message (XPath-based)
     - AlertHandler for modal dialogs
     - Entry/exit event detection via alert text ("enter"/"exit" keywords)
   - `GeofencePageAndroid` - Android-specific implementation:
     - Map long-click for geofence creation
     - GPS location setting via Appium
     - `simulateExitFromGeofence(lat, lng)` - Move outside geofence
     - `simulateEntryToGeofence(lat, lng)` - Return to safe zone (NEW)
     - NotificationHandler for system notifications
     - Entry/exit event detection via notification text ("GEOFENCE_TRANSITION_*")

2. **Component Classes**
   - `AlertHandler` - Dialog/alert notification handling
   - `NotificationHandler` - Notification display and interaction

3. **Utility Classes**
   - `LocationSimulator` - GPS location simulation for geofence testing
   - `WaitUtils` - Explicit waits and condition polling

### Phase 04 - Completed (Test Implementation)

1. **Base Test Infrastructure**
   - `BaseTest` - Abstract base test class with:
     - `@BeforeSuite` - Suite-level initialization (ConfigManager, logging)
     - `@BeforeMethod` - Test method setup (driver creation, page object init, permissions)
     - `@AfterMethod` - Test cleanup (failure screenshots, driver quit)
     - `@AfterSuite` - Suite completion logging
     - Thread-safe driver management via DriverManager
     - Allure parameter recording
     - Screenshot attachment on failures

   - `TestListener` - TestNG ITestListener implementation:
     - Lifecycle hooks: onTestStart, onTestSuccess, onTestFailure, onTestSkipped
     - Suite-level hooks: onStart, onFinish with statistics logging
     - Automatic screenshot attachment on pass/fail
     - Page source attachment on failure
     - Error detail text attachment

2. **Test Data Management**
   - `TestDataProvider` - TestNG @DataProvider class:
     - Loads test locations from `geofence-locations.json`
     - Six DataProviders:
       - `iosGeofenceData` (TC-001) - iOS geofence exit detection
       - `androidGeofenceData` (TC-003) - Android geofence exit detection
       - `iosGeofenceEntryData` (TC-002) - iOS geofence entry detection (User Journey: exit then return)
       - `androidGeofenceEntryData` (TC-004) - Android geofence entry detection (User Journey: exit then return)
     - Static initialization with error handling
     - Returns test case ID, centerLat, centerLng, exitLat, exitLng, radius, title
     - User Journey pattern: Tests validate full lifecycle (safe zone → exit → entry)

3. **Utility Classes for Testing**
   - `RetryAnalyzer` - IRetryAnalyzer implementation:
     - Retries failed tests up to 2 times (configurable)
     - Thread-safe via ITestResult attributes
     - Logs retry attempts with attempt counter

   - `AllureUtils` - Enhanced Allure reporting:
     - `attachScreenshot(name)` - Captures & attaches PNG screenshots
     - `attachPageSource(name)` - Attaches XML page source on failure
     - `attachText(name, content)` - Attaches text attachments
     - `step(description)` - Records Allure steps
     - `addLink(name, url)` - Links to external resources
     - Exception handling with fallback empty bytes

4. **End-to-End Tests (Mobile UI)**
   - `GeofenceExitTest` - Geofence exit detection:
     - TC-001: iOS geofence exit with 6-step workflow
       - Set location inside geofence
       - Create geofence zone
       - Verify creation
       - Simulate exit (250m away)
       - Wait for exit event (configurable timeout)
       - Assert event contains "exit"

     - TC-003: Android geofence exit with 6-step workflow
       - Set location inside geofence
       - Long-click map to create geofence
       - Verify creation
       - Simulate exit via GeofencePageAndroid
       - Wait for exit notification
       - Assert notification contains "EXIT"

     - Uses @Allure decorators: Epic, Feature, Story, Description, Severity, TmsLink
     - Retry analyzer enabled for flaky tests
     - Screenshot attachments at key steps

   - `LocationAccuracyTest` - GPS location accuracy:
     - TC-002: iOS location simulation accuracy
       - Set target GPS location
       - Wait 5s for stabilization
       - Validate coordinate ranges [-90,90], [-180,180]
       - Validate positive tolerance
       - No exception = success (BrowserStack validation)

     - TC-004: Android location simulation accuracy
       - Same workflow as iOS
       - Validates map displays at simulated location
       - Tolerance validation

     - Allure parameters for lat/lng/tolerance
     - Retry analyzer enabled

5. **API Tests (Integration)**
   - `GeofenceApiTest` - REST API testing with WireMock:
     - Standalone API tests (no base class dependency)
     - Port 8089 WireMock server management
     - SetupStubs() configures:
       - POST /api/geofence (201 Created response)
       - GET /api/geofence/{id} (200 OK response)

     - API-001: testCreateGeofence
       - POST request with location/radius JSON
       - Validates 201, JSON content type
       - Asserts response fields: id, status, coordinates, radius

     - API-002: testGetGeofence
       - Depends on API-001 (orderly execution)
       - GET request by ID
       - Validates 200, all fields match

     - Uses RestAssured fluent API
     - Hamcrest matchers for assertions
     - WireMock JSON path matching for stubs
     - Allure reporting with Epic/Feature/Story/Severity

6. **Test Suite Configuration (TestNG XML)**
   - `testng.xml` - Master suite:
     - Global TestListener configured
     - Three test groups:
       - API Tests (GeofenceApiTest)
       - Android E2E Tests (filtered methods)
       - iOS E2E Tests (filtered methods)
     - Platform parameter passing
     - Parallel execution: false (sequential)

   - `testng-api.xml` - API-only suite:
     - Single test: GeofenceApiTest
     - Standalone API execution

   - `testng-android.xml` - Android-only suite:
     - Android E2E test methods only
     - Platform: android parameter

   - `testng-ios.xml` - iOS-only suite:
     - iOS E2E test methods only
     - Platform: ios parameter

**Phase 04 Test Coverage:**
- 4 E2E test cases (TC-001 to TC-004)
- 2 API test cases (API-001, API-002)
- Test data driven via @DataProvider
- Retry logic for flaky tests (up to 2 retries)
- Rich Allure reporting with screenshots, logs, steps
- Cross-platform testing (iOS & Android)
- Location simulation for geofence testing
- WireMock API mocking for isolated API testing

**Not Yet Implemented (Phase 04+)**
- Additional utility classes (ActionUtils, AssertionUtils)
- Additional component classes (InputComponent, ButtonComponent)
- CI/CD pipeline configuration
- Performance/load testing
- More advanced geofence scenarios (overlapping zones, boundary testing)

## Build & Execution

### Build

```bash
./mvnw clean compile
./mvnw clean package
```

### Test Execution

```bash
# Android tests
./mvnw test -DsuiteXml=testng-android.xml -Dplatform=android

# iOS tests
./mvnw test -DsuiteXml=testng-ios.xml -Dplatform=ios

# API tests
./mvnw test -DsuiteXml=testng-api.xml

# Generate Allure report
./mvnw allure:serve
```

## Security Considerations

- Credentials stored as environment variables (never in Git)
- `.gitignore` excludes all `.properties` files except examples
- BrowserStack access key sensitive - use CI/CD secrets
- Test data files are public (no sensitive info)

## Dependencies Managed

All dependencies declared in `pom.xml` with versions pinned:
- **Appium Java Client** (9.3.0) - Mobile automation driver
- **TestNG** (7.10.2) - Test framework
- **RestAssured** (5.5.0) - API testing
- **Allure** (2.29.0) - Test reporting
- **WireMock** (3.9.1) - API mocking
- **Mockito** (5.14.2) - Unit test mocking (Phase 02)
- **Mockito Subclass** (5.14.2) - Mocking final classes (Phase 02)
- **AspectJ** (1.9.22) - Bytecode weaving (Allure)
- **Jackson** (2.18.0) - JSON processing
- **SLF4J** (2.0.16) - Logging facade
- **Lombok** (1.18.34) - Code generation (optional)

## Code Standards

- **Java Version:** 17 (target & source)
- **Encoding:** UTF-8
- **Build:** Maven 3.9+
- **Platform:** Cross-platform (mvnw wrapper)

## Test Execution

### Run All Tests (E2E + API)
```bash
./mvnw test
```

### Run API Tests Only
```bash
./mvnw test -DsuiteXml=testng-api.xml
```

### Run Android E2E Tests
```bash
./mvnw test -DsuiteXml=testng-android.xml -Dplatform=android
```

### Run iOS E2E Tests
```bash
./mvnw test -DsuiteXml=testng-ios.xml -Dplatform=ios
```

### Generate Allure Report
```bash
./mvnw allure:report
./mvnw allure:serve
```

## Phase 04 Test Implementation Details

### Test Organization
- **Base Classes:** Centralized setup/teardown logic (driver init, permissions, cleanup)
- **Test Data:** JSON-driven with @DataProvider for maintainability
- **Listeners:** TestNG lifecycle hooks for logging & artifact capture
- **Retry Logic:** Configurable retry analyzer for transient failures
- **Reporting:** Allure integration with screenshots, steps, parameters, links

### Test Scenarios Covered
| Test Case | Platform | Feature | Status |
|-----------|----------|---------|--------|
| TC-001 | iOS | Geofence Exit Detection | CRITICAL |
| TC-002 | iOS | Location Accuracy | NORMAL |
| TC-003 | Android | Geofence Exit Detection | CRITICAL |
| TC-004 | Android | Location Accuracy | NORMAL |
| API-001 | N/A | Create Geofence (POST) | CRITICAL |
| API-002 | N/A | Get Geofence (GET) | NORMAL |

### Key Testing Patterns
1. **Arrange-Act-Assert:** Clear step definitions in tests
2. **Page Object Model:** Platform-specific implementations
3. **Data-Driven Tests:** External JSON data source
4. **Test Isolation:** Independent tests with retry capability
5. **Rich Reporting:** Allure Epic/Feature/Story/Severity hierarchy

## Phase 05 - CI/CD & Reporting

**Status:** Completed - Jenkinsfile, Allure reporting, Jira/Xray integration

### Additions
- **Jenkinsfile** - Declarative Jenkins pipeline (7 stages)
- **Allure Configuration** - Report generation with failure categories
- **Xray Integration** - Automated defect creation from test failures
- **docs/cicd-pipeline.md** - Complete CI/CD documentation
- **docs/reporting-guide.md** - Reporting architecture guide

## Phase 06 - AI Integration

**Status:** Completed - GitHub Copilot, Claude Code, Atlassian MCP integration

### New Components

#### 1. GitHub Copilot Integration
- **File:** `.github/copilot-instructions.md`
- **Purpose:** Context instructions for AI code generation
- **Coverage:** Design patterns, code conventions, test structures, locator strategies

#### 2. Claude Code CLI Integration
- **Framework:** Atlassian MCP (Model Context Protocol)
- **Setup Guide:** `docs/mcp-config.md`
- **Capabilities:**
  - Automated Jira issue creation from test failures
  - Defect traceability (Test → Execution → Bug)
  - OAuth2-based authentication

#### 3. Code Enhancements
- **DriverFactory.java** - AI-assisted documentation comments
- **GeofenceExitTest.java** - AI-assisted test step descriptions
- **create-xray-defects.sh** - Enhanced Jira integration script

### AI-Assisted Development Patterns

#### Repository Guidelines (Copilot Instructions)
```
- Use AppiumBy for modern Appium locators
- Implement Page Object Model for UI changes
- Apply ThreadLocal for thread-safe driver management
- Use DataProviders for test parameterization
- Attach Allure artifacts for failure analysis
```

#### MCP Defect Creation Flow
```
Test Failure → Allure Report → create-xray-defects.sh → Jira Bug (Xray)
```

### Documentation Updates
- **README.md** - Added AI-Augmented Features section
- **docs/INDEX.md** - Added navigation links for AI resources
- **docs/codebase-summary.md** - This section

### Benefits Achieved
- Accelerated code generation with Copilot
- Automated defect tracking via MCP
- Consistent coding patterns through AI guidelines
- Seamless Jira integration without manual ticket creation
- Senior-level AI-assisted development workflow

## Phase 07 - Jira REST Fallback

**Status:** Completed - Automatic defect creation with MCP + REST API fallback

### New Components

#### 1. JiraDefectCreator Utility
- **File:** `src/main/java/com/poc/geofence/utils/JiraDefectCreator.java`
- **Purpose:** Automatic Jira defect creation on test failures
- **Strategy:** MCP script (OAuth) → REST API fallback (Basic Auth)
- **Features:**
  - Jackson ObjectMapper for safe JSON (injection-proof)
  - Async execution via ExecutorService (non-blocking)
  - Sanitized error logging (credential protection)
  - Configurable HTTP timeouts
  - Allure @Severity annotation parsing for priority mapping

#### 2. ConfigManager Extensions
- `isJiraEnabled()` - Master switch for Jira integration
- `isCreateDefectsOnFailure()` - Auto-create toggle
- `getJiraBaseUrl()` - From JIRA_BASE_URL env var
- `getJiraUsername()` - From JIRA_USERNAME env var
- `getJiraApiToken()` - From JIRA_API_TOKEN env var
- `getJiraProject()` - From env var or config property

#### 3. TestListener Enhancement
- Triggers JiraDefectCreator on test failure
- Attaches Jira issue key to Allure report
- Adds link to created defect

### Configuration

```properties
# config.properties
jira.enabled=false
jira.create.defects.on.failure=false
jira.project=GEOFENCE
```

```bash
# Environment Variables
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_USERNAME=your-email@example.com
export JIRA_API_TOKEN=your-api-token
```

### Unit Tests
- **File:** `src/test/java/com/poc/geofence/utils/JiraDefectCreatorTest.java`
- 5 test cases covering: disabled state, config flags, credential handling, null safety

### Jenkinsfile Integration
- **File:** `Jenkinsfile`
- Added Jira credentials to environment block:
  - `JIRA_BASE_URL = credentials('jira-base-url')`
  - `JIRA_USERNAME = credentials('jira-username')`
  - `JIRA_API_TOKEN = credentials('jira-api-token')`
- Added system properties to all test stages (API, Android, iOS):
  - `-Djira.enabled=true`
  - `-Djira.create.defects.on.failure=true`
- MCP shell script stage retained as backup

**Jenkins Credentials Setup:**
```
Manage Jenkins → Credentials → Add:
- jira-base-url (Secret text)
- jira-username (Secret text)
- jira-api-token (Secret text)
```

## Phase 08 - Real API Testing

**Status:** Completed - Conditional WireMock, real API support, TestNG groups, Jenkins stages

### New Components

#### 1. API Mode Configuration (ConfigManager)
- **New Methods:**
  - `getApiMode()` - Returns api.mode: mock (default), staging, uat, prod
  - `isRealApiMode()` - True if mode != "mock"
  - `getApiBaseUrl()` - Returns URL from env vars (API_BASE_URL_STAGING, API_BASE_URL_UAT, API_BASE_URL_PROD) or config property
  - `getApiToken()` - Returns API_AUTH_TOKEN env var (required for real API)
  - `isProdMode()` - True if api.mode == "prod"
  - `getApiTimeout()` - Returns api.timeout property (default: 10000ms)

- **Configuration:**
  ```properties
  api.mode=mock                    # mock (default), staging, uat, prod
  api.base.url=http://localhost:8080  # Used for mock mode only
  api.timeout=10000               # Request timeout in milliseconds
  ```

- **Environment Variables:**
  ```bash
  API_BASE_URL_STAGING        # Required for staging mode
  API_BASE_URL_UAT            # Required for uat mode
  API_BASE_URL_PROD           # Required for prod mode (read-only)
  API_AUTH_TOKEN              # Required for real API modes
  ```

#### 2. Conditional WireMock (GeofenceApiTest)
- **Mock Mode (api.mode=mock):**
  - WireMockServer started on port 8089
  - Stubs configured for POST /api/geofence (201) and GET /api/geofence/{id} (200)
  - Runs by default in CI/CD
  - No external dependencies

- **Real API Mode (staging/uat/prod):**
  - WireMock server NOT started
  - Uses real API base URL and token
  - Request spec includes Bearer token auth header
  - Cleanup of TEST_E2E_* geofences in staging/uat (skipped in prod)

- **Test Isolation:**
  - `getRequestSpec()` - Returns configured RestAssured RequestSpecification
  - Conditional auth header injection
  - Test data cleanup with error tracking

#### 3. TestNG Groups
- **Group: mock-api**
  - Tests: testCreateGeofence(), testGetGeofence()
  - Runs by default
  - Uses WireMock stubs
  - No external setup required

- **Group: real-api**
  - Tests: testCreateGeofenceReal(), testGetGeofenceReal()
  - Opt-in execution
  - Requires environment variables
  - Includes cleanup logic

- **Test Dependencies:**
  - testGetGeofence depends on testCreateGeofence (same method)
  - testGetGeofenceReal depends on testCreateGeofenceReal (same method)

#### 4. Test Suite Files
- **testng-api.xml** - Mock API test suite
  ```xml
  <include name="mock-api"/>
  ```
  Usage: `mvn test -DsuiteXml=testng-api.xml`

- **testng-api-real.xml** - Real API test suite
  ```xml
  <include name="real-api"/>
  ```
  Usage: `mvn test -DsuiteXml=testng-api-real.xml -Dapi.mode=staging`

#### 5. Jenkinsfile Integration
- **New Parameters:**
  ```groovy
  API_MODE = choice(['mock', 'staging', 'uat'])
  RUN_REAL_API_TESTS = booleanParam(defaultValue: false)
  ```

- **New Environment Variables:**
  - API_BASE_URL_STAGING (from Jenkins credentials)
  - API_BASE_URL_UAT (from Jenkins credentials)
  - API_BASE_URL_PROD (from Jenkins credentials)
  - API_AUTH_TOKEN (from Jenkins credentials)

- **New Stage: API Tests (Real)**
  ```groovy
  when {
    allOf {
      expression { params.RUN_REAL_API_TESTS == true }
      expression { params.API_MODE != 'mock' }
    }
  }
  steps {
    mvn test -DsuiteXml=testng-api-real.xml -Dapi.mode=${params.API_MODE}
  }
  ```
  - Conditional execution (only if enabled AND api.mode != mock)
  - Passes API_MODE as Maven property
  - Runs after mock API tests

#### 6. Test Data Cleanup
- **Real API Only:** DELETE /api/geofence/{id}
- **Tracking:** List<String> createdGeofenceIds
- **Test Naming:** TEST_E2E_Zone_<timestamp> for easy identification
- **Error Handling:**
  - Logs failures but doesn't fail test
  - Warns about TEST DATA POLLUTION risk
  - Tracks success/fail counts

### Configuration Examples

**Mock Mode (CI/CD Default):**
```bash
./mvnw test -DsuiteXml=testng-api.xml
# Uses WireMock on port 8089
```

**Staging Real API:**
```bash
export API_BASE_URL_STAGING=https://api-staging.example.com
export API_AUTH_TOKEN=your-token
./mvnw test -DsuiteXml=testng-api-real.xml -Dapi.mode=staging
```

**UAT Real API:**
```bash
export API_BASE_URL_UAT=https://api-uat.example.com
export API_AUTH_TOKEN=your-token
./mvnw test -DsuiteXml=testng-api-real.xml -Dapi.mode=uat
```

**Production Read-Only (No Write Tests):**
```bash
export API_BASE_URL_PROD=https://api.example.com
export API_AUTH_TOKEN=your-token
./mvnw test -DsuiteXml=testng-api-real.xml -Dapi.mode=prod
# testCreateGeofenceReal() skipped automatically
# Only testGetGeofenceReal() runs
```

### Benefits Achieved
- Flexible API testing: WireMock for speed, real API for integration
- Environment isolation: mock vs staging vs uat vs prod
- Test data safety: Automatic cleanup in non-prod environments
- CI/CD friendly: opt-in real API testing
- Credential security: Environment variables only (no hardcoded secrets)
- Production protection: Read-only mode prevents accidental writes

## Next Steps (Phase 09+)

1. Implement advanced utility classes:
   - ActionUtils - Touch, swipe, scroll, drag operations
   - AssertionUtils - Custom assertion helpers
   - FileUtils - File I/O & log operations
   - ScreenSizeUtils - Device dimension handling

2. Implement additional component classes:
   - InputComponent - Reusable input field abstractions
   - ButtonComponent - Reusable button abstractions
   - ListComponent - List/scroll handling
   - DialogComponent - Modal dialog handling

3. Expand test coverage:
   - Overlapping geofence zones
   - Boundary condition testing (edge of radius)
   - Multiple simultaneous geofences
   - Error handling scenarios
   - Real API integration tests for all endpoints

4. Performance & Load Testing:
   - Multiple location updates per second
   - Long-running geofence monitoring
   - Battery/resource consumption validation

5. Advanced CI/CD:
   - Nightly real API test runs
   - Performance regression detection
   - Multi-environment parallel execution
   - Slack/email notifications with detailed reports

6. Enhanced Error Handling:
   - Flaky test pattern detection
   - Automatic screenshot/log capture
   - Test result correlation
   - Regression detection
