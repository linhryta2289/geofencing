# System Architecture

**Project:** Geofence Mobile Automation POC
**Version:** 1.0.0
**Date:** 2025-12-22
**Status:** Phase 03 - Page Objects Implementation In Progress

---

## 1. Architecture Overview

### High-Level System Design

```
┌─────────────────────────────────────────────────────────────────┐
│                     TEST EXECUTION LAYER                        │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │   TestNG       │  │   E2E Tests    │  │   API Tests    │   │
│  │   Framework    │  │   (Geofence)   │  │   (REST)       │   │
│  └────────────────┘  └────────────────┘  └────────────────┘   │
└────────────────────────────────┬──────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│              APPLICATION ABSTRACTION LAYER                    │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │ Page Objects   │  │  Components    │  │  API Clients   │  │
│  │ (iOS/Android)  │  │                │  │                │  │
│  └────────────────┘  └────────────────┘  └────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │               Base Classes & Utilities                 │  │
│  │  (Driver Mgmt, Waits, Actions, Assertions)             │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────────────────────────┬──────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│              DRIVER & CAPABILITY LAYER                        │
│  ┌────────────────┐  ┌────────────────────────────────────┐  │
│  │ Driver Factory │  │  Capabilities Builder              │  │
│  │                │  │  (iOS/Android/BrowserStack)        │  │
│  └────────────────┘  └────────────────────────────────────┘  │
└────────────────────────────────┬──────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│         CONFIGURATION & RESOURCE MANAGEMENT                   │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │ Config Manager │  │ Test Data      │  │ Allure Report  │  │
│  │                │  │                │  │                │  │
│  └────────────────┘  └────────────────┘  └────────────────┘  │
└────────────────────────────────┬──────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│                    EXTERNAL SERVICES                          │
│  ┌──────────────────┐    ┌──────────────────┐                │
│  │  Appium Server   │    │  BrowserStack    │                │
│  │  (Mobile Driver) │    │  (Cloud Devices) │                │
│  └──────────────────┘    └──────────────────┘                │
└──────────────────────────────────────────────────────────────┘
```

---

## 1.5 Phase 02-03 Implementation Status

### Phase 02 - Completed

**Configuration & Enums Layer**
- ConfigManager singleton with property hierarchy (System > Config File > Env Vars > Defaults)
- PlatformType enum (IOS/ANDROID) with safe string conversion
- Environment enum (LOCAL/BROWSERSTACK) with safe string conversion
- Typed configuration getters (getPlatform, getEnvironment, getDefaultTimeout, getAppPath, etc.)

**Driver Layer**
- DriverFactory: Creates iOS/Android drivers for LOCAL and BROWSERSTACK environments
- DriverManager: ThreadLocal driver storage for parallel test execution
- Support for both local Appium hub (http://127.0.0.1:4723) and BrowserStack hub (https://hub.browserstack.com/wd/hub)

**Capabilities Layer**
- CapabilityBuilder interface: Generic fluent builder contract
- IOSCapabilities: Builds iOS-specific capabilities (device, OS, app, GPS location, BrowserStack auth)
- AndroidCapabilities: Builds Android-specific capabilities (device, OS, app, package, activity, GPS location, BrowserStack auth)

**Testing & Validation**
- Unit tests for ConfigManager (property loading, override, env var fallback)
- Unit tests for PlatformType enum (conversion, validation)
- Unit tests for Environment enum (conversion, validation)
- Unit tests for DriverManager (ThreadLocal operations, null checks)
- Mockito integration for isolated unit testing

### Phase 03 - In Progress

**Page Objects Layer**
- BasePage: Abstract base class with common page operations (wait, click, type, screenshot, etc.)
- GeofencePage: Interface defining geofence page contract (navigate, waitForAlert, getStatus)
- PageFactory: Factory for creating platform-specific page objects via Strategy pattern
- GeofencePageIOS: iOS-specific geofence page implementation
- GeofencePageAndroid: Android-specific geofence page implementation

**Component Layer**
- AlertHandler: Dialog/alert notification handling and interaction
- NotificationHandler: Notification display detection and interaction

**Utility Layer**
- LocationSimulator: GPS location simulation for geofence entry/exit testing
- WaitUtils: Explicit waits and condition polling utilities

### Remaining Implementation (Phase 03+)
- Base test classes (BaseTest, BaseComponent)
- E2E test cases
- Additional utility classes (ActionUtils, AssertionUtils)
- Component classes (InputComponent, ButtonComponent)
- API client implementations

---

## 2. Component Architecture

### 2.1 Test Execution Layer

#### TestNG Framework
- **Purpose:** Test definition, execution, and lifecycle management
- **Responsibilities:**
  - Test case execution (sequential/parallel)
  - Test grouping and suites
  - Before/After method hooks
  - DataProvider parameterization
  - Test result collection

#### Test Suites
- **testng-android.xml** - Android-specific tests
- **testng-ios.xml** - iOS-specific tests
- **testng-api.xml** - API tests
- **testng-full.xml** - All tests

#### Test Categories
- **E2E Tests** (`src/test/java/com/poc/geofence/e2e/`)
  - Geofence entry/exit scenarios
  - Location transition flows
  - App behavior validation

- **API Tests** (`src/test/java/com/poc/geofence/api/`)
  - Geofence service API
  - Location API
  - Integration scenarios

---

### 2.2 Application Abstraction Layer

#### Page Objects
```java
BasePage
├── iOS
│   ├── iOSLoginPage
│   ├── iOSHomeScreen
│   ├── iOSGeofenceScreen
│   └── iOSSettingsPage
└── Android
    ├── AndroidLoginPage
    ├── AndroidHomeScreen
    ├── AndroidGeofenceScreen
    └── AndroidSettingsPage
```

**Purpose:**
- Encapsulate UI element locators
- Abstract platform differences
- Provide high-level action methods
- Reduce test code duplication

**Pattern:**
```java
public class GeofenceScreen extends BasePage {
    // Locators (constants)
    // Action methods
    // Helper methods
}
```

#### Components
- **InputComponent** - Text input fields
- **ButtonComponent** - Clickable buttons
- **AlertComponent** - Dialog/alert handling
- **MapComponent** - Map/location display

**Purpose:**
- Reusable UI components across screens
- Consistent interaction patterns
- Reduce code duplication

#### Base Classes
- **BaseTest** - Test lifecycle, driver setup/teardown
- **BasePage** - Common page methods, element interactions
- **BaseComponent** - Component base functionality

---

### 2.3 Driver & Capability Layer

#### Driver Factory
**Purpose:** Create and configure driver instances

**Responsibilities:**
- Instantiate iOS/Android drivers
- Apply device capabilities
- Configure BrowserStack parameters
- Manage driver lifecycle

**Implementation:**
```java
public class DriverFactory {
    public static IOSDriver createIOSDriver(CapabilityConfig config) { ... }
    public static AndroidDriver createAndroidDriver(CapabilityConfig config) { ... }
}
```

#### Capabilities Builder
**Purpose:** Build platform-specific device capabilities

**Android Capabilities:**
```
platformName: Android
automationName: UiAutomator2
deviceName: Samsung Galaxy S24
platformVersion: 15
app: bs://ANDROID_APP_ID
```

**iOS Capabilities:**
```
platformName: iOS
automationName: XCUITest
deviceName: iPhone 16
platformVersion: 18
app: bs://IOS_APP_ID
```

---

### 2.4 Configuration & Resource Layer

#### Configuration Manager
**Purpose:** Load and manage configuration

**Configuration Files:**
```
config.properties            # Core settings
browserstack.properties      # BrowserStack auth
environments/
├── dev.properties          # Dev environment
└── staging.properties      # Staging environment
```

**Configuration Hierarchy:**
1. Load base config.properties
2. Load environment-specific settings
3. Override with system properties
4. Load credentials from env variables

#### Test Data Management
**Purpose:** Provide test data for parameterization

**Test Data Structure:**
```json
{
  "locations": {
    "bonn_center": { "latitude": 50.7333, "longitude": 7.1032 },
    "bonn_exit": { "latitude": 50.7358, "longitude": 7.1032 }
  },
  "users": { ... },
  "scenarios": { ... }
}
```

#### Allure Configuration
**Purpose:** Configure test reporting

**Features:**
- Test result tracking
- Failure screenshots
- Test duration metrics
- Historical trends

---

## 3. Data Flow

### Test Execution Flow

```
1. Test Execution Start
   ├─ Load Configuration
   │  ├─ Load config.properties
   │  ├─ Load environment config
   │  └─ Load credentials from env vars
   │
   ├─ BeforeMethod Hook
   │  ├─ Build driver capabilities
   │  ├─ Create driver instance
   │  └─ Initialize page objects
   │
   ├─ Test Execution
   │  ├─ Perform app actions (via Page Objects)
   │  ├─ Wait for conditions (via WaitUtils)
   │  └─ Verify results (via Assertions)
   │
   ├─ Capture Artifacts (on failure)
   │  ├─ Screenshot
   │  ├─ Logs
   │  └─ Device logs
   │
   └─ AfterMethod Hook
      ├─ Quit driver
      ├─ Cleanup resources
      └─ Generate Allure report
```

### Geofence Test Flow

```
Test: shouldEnterGeofenceWhenLocationMatches
│
├─ Arrange
│  ├─ Load location data (bonn_center: 50.7333, 7.1032)
│  └─ Initialize app
│
├─ Act
│  ├─ Simulate location change → bonn_center
│  ├─ App receives location update
│  ├─ App checks geofence (radius: 200m)
│  ├─ App detects entry
│  └─ Wait for notification (timeout: 120s)
│
└─ Assert
   ├─ Verify notification displayed
   ├─ Verify geofence status = "ENTERED"
   └─ Verify timestamp recorded
```

---

## 4. Class Diagram (Phase 01 Ready)

```
┌──────────────────────┐
│    BaseTest          │
│  ──────────────────  │
│  - driver            │
│  - testData          │
│  + beforeMethod()    │
│  + afterMethod()     │
└──────────────────────┘
          ▲
          │
    ┌─────┴─────┐
    │           │
┌───────────┐ ┌──────────────┐
│ E2ETest   │ │ APITest      │
└───────────┘ └──────────────┘


┌──────────────────────┐
│    BasePage          │
│  ──────────────────  │
│  - driver            │
│  - wait              │
│  + click()           │
│  + type()            │
│  + waitForElement()  │
└──────────────────────┘
          ▲
          │
    ┌─────┴─────────┐
    │               │
┌──────────┐  ┌──────────────┐
│ HomePage │  │ LoginPage    │
└──────────┘  └──────────────┘


┌──────────────────────────┐
│  DriverFactory           │
│  ──────────────────────  │
│  + createIOSDriver()     │
│  + createAndroidDriver() │
└──────────────────────────┘
          │
          ├──► IOSDriver (Appium)
          └──► AndroidDriver (Appium)


┌──────────────────────────┐
│  ConfigManager           │
│  ──────────────────────  │
│  + loadConfig()          │
│  + getProperty()         │
│  + getCredentials()      │
└──────────────────────────┘


┌──────────────────────────┐
│  TestDataProvider        │
│  ──────────────────────  │
│  + getLocation()         │
│  + getUser()             │
│  + getScenario()         │
└──────────────────────────┘
```

---

## 5. Deployment Architecture

### Local Development Environment

```
Developer Machine
├── JDK 17
├── Maven 3.9+
├── Project Code
├── Appium Server (local or cloud)
└── IDE (IntelliJ/VS Code)
```

### CI/CD Pipeline Environment

```
CI/CD Server (GitHub Actions/Jenkins)
├── JDK 17
├── Maven 3.9+
├── Project Code
├── BrowserStack Connection
└── Allure Report Server
```

### Cloud Testing Environment

```
BrowserStack Cloud
├── Android Device Farm
├── iOS Device Farm
├── Appium Server (cloud)
└── Test Result Dashboard
```

---

## 6. Integration Points

### BrowserStack Integration

**Connection:**
```
Test Framework
    │
    ├─ Appium Server (cloud)
    │
    └─ BrowserStack API
        ├── Device selection
        ├── App upload
        ├── Session management
        └── Results dashboard
```

**Capabilities Required:**
```json
{
  "platformName": "iOS",
  "automationName": "XCUITest",
  "browserstack.user": "${BROWSERSTACK_USERNAME}",
  "browserstack.key": "${BROWSERSTACK_ACCESS_KEY}",
  "app": "bs://UPLOADED_APP_ID",
  "device": "iPhone 16",
  "os_version": "18.0"
}
```

### API Testing Integration

**REST Endpoints:**
- Geofence Service API
- Location Service API
- User Service API

**Tools:**
- RestAssured for HTTP calls
- WireMock for API mocking
- Jackson for JSON processing

---

## 7. Technology Stack Justification

| Technology | Reason |
|-----------|--------|
| **Appium 9.3.0** | W3C standard, wide platform support, industry standard |
| **TestNG 7.10.2** | Parallel execution, powerful assertions, DataProviders |
| **Java 17** | LTS version, modern features, wide industry adoption |
| **Maven 3.9+** | Build automation, dependency management, CI/CD friendly |
| **RestAssured 5.5.0** | Fluent API, easy REST testing, JSON handling |
| **Allure 2.29.0** | Rich reporting, failure analysis, historical trends |
| **WireMock 3.9.1** | API mocking, independent testing, stub management |

---

## 8. Scalability Considerations

### Horizontal Scaling
- **Parallel Test Execution:** TestNG supports parallel execution via forks
- **Device Pool:** BrowserStack provides unlimited concurrent devices
- **CI/CD:** Tests can run on multiple agents simultaneously

### Vertical Scaling
- **Local Resource Requirements:**
  - CPU: 2+ cores
  - Memory: 4GB+ RAM
  - Disk: 500MB+ for dependencies

### Performance Optimization
- Use explicit waits instead of fixed delays
- Implement retry logic for flaky operations
- Cache driver capabilities when possible
- Minimize test setup/teardown time

---

## 9. Security Architecture

### Credential Management

```
Configuration System
│
├─ Environment Variables (Runtime)
│  ├── BROWSERSTACK_USERNAME
│  ├── BROWSERSTACK_ACCESS_KEY
│  └── BUILD_NUMBER
│
├─ Property Files (Static)
│  ├── config.properties (non-sensitive)
│  └── environments/dev.properties
│
└─ Git Security
   ├── .gitignore (exclude .properties files)
   └── *.example files (templates only)
```

### Data Protection
- Credentials never logged
- Sensitive data via environment only
- HTTPS for API calls
- No test credentials in version control

---

## 10. Error Handling Architecture

### Driver-Level Errors
```
Appium Exception
├─ Session not created → Retry or skip
├─ Element not found → Wait & retry
└─ Timeout → Log & capture screenshot
```

### Framework-Level Errors
```
Test Execution Error
├─ Configuration error → Clear error message
├─ Test data error → Provide context
└─ Setup/teardown error → Cleanup & report
```

### Recovery Strategies
- Explicit waits with meaningful timeouts
- Screenshot capture on failure
- Automatic driver restart on connection loss
- Comprehensive error logging

---

## 11. Monitoring & Logging

### Log Levels
- **ERROR:** Framework failures requiring investigation
- **WARN:** Potential issues (slow operations, retries)
- **INFO:** Key milestones (test start/end, setup/teardown)
- **DEBUG:** Detailed operation information

### Metrics Tracked
- Test execution time
- Pass/fail rates
- Device availability
- API response times
- Failure categories

---

## 12. Future Enhancements

### Phase 02+
- Performance monitoring
- Test failure analytics
- Device health checks
- Custom reporting dashboards
- Advanced mocking capabilities

### Phase 03+
- Real-time test monitoring
- Automated test generation
- Machine learning-based flakiness detection
- Advanced failure analysis

---

**Document Control**
- **Version:** 1.0
- **Last Updated:** 2025-12-22
- **Next Review:** 2026-01-22
- **Owner:** Technical Architecture
