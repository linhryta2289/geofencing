# Mobile Geofence Testing - Copilot Instructions

Context for GitHub Copilot when working in this repository.

## Framework Overview

Mobile automation framework for geofence testing:
- **Java 17** - LTS version
- **Appium 8.6.0** - Java Client with Selenium 4.x
- **TestNG 7.10.2** - Test framework
- **RestAssured 5.5.0** - API testing
- **Allure 2.29.0** - Reporting
- **WireMock 3.9.1** - API mocking
- **BrowserStack App Automate** - Cloud execution

## Design Patterns

### Factory Pattern (DriverFactory)
Creates platform-specific drivers:
```java
public AppiumDriver createDriver(PlatformType platform, Environment environment) {
    return switch (platform) {
        case IOS -> createIOSDriver(environment);
        case ANDROID -> createAndroidDriver(environment);
    };
}
```

### Builder Pattern (CapabilityBuilder)
Fluent API for capabilities:
```java
new AndroidCapabilities()
    .withApp(appPath)
    .withDevice(deviceName, osVersion)
    .withBrowserStack(username, accessKey)
    .build();
```

### Strategy Pattern (PageFactory)
Platform-specific page resolution:
```java
PageFactory.create(GeofencePage.class, driver, platformType);
```

### Singleton (ConfigManager)
Single configuration instance:
```java
ConfigManager.getInstance().getProperty("key");
```

### ThreadLocal (DriverManager)
Thread-safe driver storage:
```java
DriverManager.setDriver(driver);
DriverManager.getDriver();
```

## Code Patterns

### Page Object Structure
```java
public class GeofencePageAndroid extends BasePage implements GeofencePage {
    private static final By ELEMENT = AppiumBy.id("com.eebax.geofencing:id/element");

    @Override
    public void createGeofence(double lat, double lng, int radius, String title) {
        // Long-click on map to create geofence
    }

    public void simulateExitFromGeofence(double exitLat, double exitLng) {
        // Set GPS to exit location
    }
}
```

### Test Structure
```java
@Epic("Mobile Geofence Automation")
@Feature("Feature Name")
public class TestClass extends BaseTest {

    @Test(dataProvider = "data",
          dataProviderClass = TestDataProvider.class,
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Story Name")
    @Description("Test description")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-001")
    public void testMethod(String tcId, double lat, double lng, ...) {
        log.info("Executing {}: Test Name", tcId);
        Allure.parameter("Test Case ID", tcId);

        AllureUtils.step("Step description");
        // Test code

        AllureUtils.attachScreenshot("Screenshot Name");
    }
}
```

### Locator Strategies
- **iOS**: Accessibility IDs or XPath
- **Android**: Resource ID (`AppiumBy.id("com.eebax.geofencing:id/element")`)

### Location Simulation
```java
LocationSimulator.setLocation(latitude, longitude);
double[] exit = LocationSimulator.calculateOffset(lat, lng, 250, 0);
```

### BrowserStack Geofence Workaround

BrowserStack GPS simulation doesn't trigger Android GeofencingClient. Use deep links:
```java
// Trigger geofence event via deep link (bypasses GPS limitation)
GeofenceTestHelper.triggerExitEvent(DriverManager.getDriver());
GeofenceTestHelper.triggerEnterEvent(DriverManager.getDriver());

// Constants
GeofenceTestHelper.GEOFENCE_ENTER  // 1
GeofenceTestHelper.GEOFENCE_EXIT   // 2
GeofenceTestHelper.GEOFENCE_DWELL  // 4
```

### Notification/Toast Detection (Android)
```java
NotificationHandler handler = new NotificationHandler();

// Check app status indicator (most reliable)
String status = handler.getAppStatusText();

// Check Toast message
String toast = handler.getToastText();

// Wait for any indicator
boolean found = handler.waitForToastOrNotification("GEOFENCE_TRANSITION_EXIT", 30);
```

### Wait Patterns
```java
// Standard timeout from config
int timeout = config.getDefaultTimeout(); // 30s

// Geofence triggers - use reduced timeout with deep link
int geofenceTimeout = 30; // Reduced from 120s when using deep link trigger

boolean detected = geofencePage.waitForGeofenceExitEvent(geofenceTimeout);
```

## Conventions

1. Test method names: `test{Platform}{Feature}` (e.g., `testAndroidGeofenceExit`)
2. Data providers: Per-platform in `TestDataProvider` (e.g., `androidGeofenceData`, `iosGeofenceData`)
3. Assertions: TestNG Assert with descriptive messages
4. Logging: SLF4J via `log.info()`, `log.debug()`
5. Screenshots: `AllureUtils.attachScreenshot("name")`
6. Step descriptions: `AllureUtils.step("description")`

## Avoid

- Implicit waits (Appium timing unpredictable)
- Hardcoded wait times (use ConfigManager)
- Skipping Allure annotations (required for reporting)
- Committing BrowserStack credentials (use `browserstack.properties`)
- Relying on GPS simulation for geofence events on BrowserStack (use deep link trigger)

## File Locations

| Type | Location |
|------|----------|
| Config | `src/test/resources/config/` |
| BrowserStack Creds | `src/test/resources/config/browserstack.properties` |
| Page Objects | `src/main/java/.../pages/` |
| Platform Pages | `src/main/java/.../pages/android/`, `src/main/java/.../pages/ios/` |
| Tests | `src/test/java/.../e2e/` |
| Utilities | `src/main/java/.../utils/` |
| Components | `src/main/java/.../components/` |
| Test Data | `src/test/resources/testdata/` |
| TestNG Suites | `testng.xml`, `testng-android.xml`, `testng-ios.xml` |

## Key Utilities

| Class | Purpose |
|-------|---------|
| `GeofenceTestHelper` | Deep link trigger for geofence events on BrowserStack |
| `NotificationHandler` | Android notification panel, Toast, status text detection |
| `LocationSimulator` | GPS location simulation via Appium |
| `AllureUtils` | Allure reporting helpers (steps, screenshots) |
| `RetryAnalyzer` | TestNG retry on flaky tests |
| `JiraDefectCreator` | Auto-create Jira defects on test failure |
