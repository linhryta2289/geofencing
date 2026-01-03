# Code Standards & Guidelines

**Project:** Geofence Mobile Automation POC
**Last Updated:** 2025-12-22
**Version:** 1.0.0
**Phase:** 03 - Page Objects Implementation

## Java & Build Standards

### Language & Compilation
- **Java Version:** 17 (JDK 17+)
- **Source Encoding:** UTF-8
- **Build Tool:** Maven 3.9+
- **Build Wrapper:** Use `./mvnw` (Maven wrapper - no local Maven install required)

### Project Structure
Follow Maven standard directory layout:
```
src/main/java/com/poc/geofence/
├── config/         Configuration management classes
├── driver/         WebDriver/AppDriver factory & capabilities
├── pages/          Page Objects (iOS/Android)
├── components/     Reusable UI components
├── utils/          Utility & helper classes
└── api/            API client implementations

src/test/java/com/poc/geofence/
├── base/           Base test classes & fixtures
├── e2e/            End-to-end test suites
├── api/            API test suites
└── data/           Test data providers & factories
```

## Phase 03 Implementation Patterns

### Page Objects (BasePage, GeofencePage, PageFactory)
```java
// Base class with common operations
public abstract class BasePage {
    protected AppiumDriver driver;
    protected WebDriverWait wait;

    protected WebElement waitForVisible(By locator) { ... }
    protected void click(By locator) { ... }
    protected void type(By locator, String text) { ... }
    protected byte[] getScreenshot() { ... }
}

// Interface for platform-specific implementation
public interface GeofencePage {
    void navigateTo(Location location);
    void waitForGeofenceAlert(int timeoutSeconds);
    String getGeofenceStatus();
}

// Factory for creating correct implementation
public class PageFactory {
    public static GeofencePage getGeofencePage() {
        PlatformType platform = config.getPlatform();
        return switch (platform) {
            case IOS -> new GeofencePageIOS();
            case ANDROID -> new GeofencePageAndroid();
        };
    }
}

// Platform-specific implementation
public class GeofencePageIOS extends BasePage implements GeofencePage {
    private static final String ALERT_TITLE = "XCUIElementTypeStaticText[@name='alert']";

    @Override
    public void navigateTo(Location location) {
        // iOS-specific location navigation
    }
}
```

### Utility Classes (WaitUtils, LocationSimulator)
```java
// Wait utilities for explicit waits
public class WaitUtils {
    public static WebElement waitForElement(AppiumDriver driver, By locator) { ... }
    public static void waitForCondition(Predicate<AppiumDriver> condition) { ... }
}

// Location simulation
public class LocationSimulator {
    public void simulateLocation(double latitude, double longitude) { ... }
}
```

### Component Classes (AlertHandler, NotificationHandler)
```java
// Alert handling
public class AlertHandler {
    public void handleAlert(String action) { ... }
    public String getAlertText() { ... }
}

// Notification handling
public class NotificationHandler {
    public boolean isNotificationDisplayed() { ... }
    public void tapNotification() { ... }
}
```

## Phase 02 Implementation Patterns

### Configuration Management (ConfigManager)
Singleton pattern with double-checked locking for thread-safe initialization:
```java
// Property priority: System > Config File > Default
String platform = ConfigManager.getInstance().getProperty("platform", "android");

// Environment variables for secrets
String bsKey = ConfigManager.getInstance().getBrowserStackProperty("browserstack.accesskey");

// Typed getters
PlatformType platform = config.getPlatform();
Environment env = config.getEnvironment();
int timeout = config.getDefaultTimeout();
```

### Enum Conversion Pattern
Safe enum parsing with descriptive error messages:
```java
public enum PlatformType {
    IOS("ios"),
    ANDROID("android");

    public static PlatformType fromString(String text) {
        for (PlatformType pt : PlatformType.values()) {
            if (pt.value.equalsIgnoreCase(text)) return pt;
        }
        throw new IllegalArgumentException("Unknown platform: " + text);
    }
}
```

### Driver Factory Pattern
Factory method pattern with environment-specific configuration:
```java
// Single method creates correct driver based on config
AppiumDriver driver = new DriverFactory().createDriver();

// Or explicit control
AppiumDriver iosDriver = new DriverFactory()
    .createDriver(PlatformType.IOS, Environment.BROWSERSTACK);
```

### Capabilities Builder (Fluent Pattern)
Fluent interface for readable capability configuration:
```java
IOSCapabilities caps = new IOSCapabilities()
    .withApp("bs://ios_app_id")
    .withDevice("iPhone 14", "16")
    .withGpsLocation(50.7333, 7.1032)
    .withBrowserStack(username, accessKey);
Capabilities built = caps.build();
```

### ThreadLocal Driver Management
Safe driver storage for parallel test execution:
```java
// Setup in @BeforeMethod
DriverManager.setDriver(driver);

// Access in test
AppiumDriver driver = DriverManager.getDriver();

// Cleanup in @AfterMethod
DriverManager.quitDriver();
```

## Naming Conventions

### Java Classes
- **Package Objects:** `ObjectNamePage` (e.g., `LoginPage`, `HomeScreen`)
- **Base Classes:** `BaseTest`, `BasePage`, `BaseComponent`
- **Utilities:** `UtilityNameUtil` or `UtilityNameHelper` (e.g., `DateUtil`, `FileHelper`)
- **API Clients:** `ServiceNameClient` or `ServiceNameAPI` (e.g., `GeofenceAPIClient`)
- **Data Classes:** `ModelNameData` or `ModelNameProvider` (e.g., `LocationData`, `UserProvider`)
- **Test Classes:** `FeatureNameTest` or `FeatureNameE2ETest` (e.g., `GeofenceEntryTest`)

### Methods
- **Test Methods:** `should{Describe}When{Condition}` (e.g., `shouldEnterGeofenceWhenLocationMatches`)
- **Helper Methods:** `camelCase` (e.g., `waitForElement`, `clickButton`)
- **Boolean Methods:** `is{State}` or `has{Property}` (e.g., `isElementVisible`, `hasGeofenceAlert`)

### Constants
- **Class Constants:** `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`, `GEOFENCE_RADIUS`)
- **Locators:** `{PLATFORM}_{ELEMENT_NAME}` (e.g., `ANDROID_HOME_BUTTON`, `IOS_SETTINGS_GEAR`)

### Variables
- **Private Fields:** `camelCase` with underscore prefix optional (e.g., `_timeout`, `driverInstance`)
- **Local Variables:** `camelCase` (e.g., `isVisible`, `foundElement`)

## Code Organization

### Package Organization
```java
// com.poc.geofence.config
ConfigManager          // Single responsibility: load & manage config
ConfigProvider         // Interface for config access
EnvironmentConfig      // Environment-specific settings

// com.poc.geofence.driver
DriverFactory          // Create driver instances
CapabilitiesBuilder    // Build device capabilities
DriverManager          // Manage driver lifecycle

// com.poc.geofence.pages
BasePage               // Base class with common page methods
LoginPage              // Page Object for login screen
HomeScreen             // Page Object for home screen (iOS/Android specific)

// com.poc.geofence.components
InputComponent         // Reusable input field component
ButtonComponent        // Reusable button component

// com.poc.geofence.utils
WaitUtils              // Explicit waits
ActionUtils            // Touch/swipe actions
AssertionUtils         // Common assertions
FileUtils              // File I/O operations

// com.poc.geofence.api
GeofenceAPIClient      // Geofence API calls
LocationAPIClient      // Location API calls
```

## Coding Practices

### Page Objects
```java
public class LoginPage extends BasePage {
    private static final String EMAIL_INPUT = "emailField";
    private static final String PASSWORD_INPUT = "passwordField";
    private static final String LOGIN_BUTTON = "loginButton";

    public LoginPage(IOSDriver driver) {
        super(driver);
    }

    public HomePage login(String email, String password) {
        type(EMAIL_INPUT, email);
        type(PASSWORD_INPUT, password);
        click(LOGIN_BUTTON);
        return new HomePage(driver);
    }
}
```

### Test Cases
```java
public class GeofenceEntryTest extends BaseTest {

    @Test
    public void shouldEnterGeofenceWhenLocationMatches() {
        // Arrange
        GeofenceLocation location = testData.getLocation("bonn_center");

        // Act
        app.navigateTo(location);
        app.waitForGeofenceAlert(GEOFENCE_WAIT_TIMEOUT);

        // Assert
        assertThat(app.getGeofenceStatus()).isEqualTo("ENTERED");
    }
}
```

### Utility Methods
```java
public class WaitUtils {
    private static final int DEFAULT_TIMEOUT = 30;

    public static WebElement waitForElement(
            WebDriver driver,
            By locator,
            int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.presenceOfElementLocated(locator));
    }
}
```

## Configuration Management

### Loading Configuration
```java
// Use environment variables for sensitive data
String username = System.getenv("BROWSERSTACK_USERNAME");
String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

// Load from properties files for non-sensitive config
Properties props = new Properties();
props.load(new FileInputStream("config.properties"));
String timeout = props.getProperty("default.timeout", "30");
```

### Environment-Specific Configuration
```java
// Load environment-specific settings
String environment = System.getProperty("env", "dev");
Properties envProps = new Properties();
envProps.load(new FileInputStream("environments/" + environment + ".properties"));
```

## Dependency Management

### Adding Dependencies
1. Add to `pom.xml` with explicit version
2. Use version properties for consistency:
   ```xml
   <appium.version>9.3.0</appium.version>
   <dependency>
       <groupId>io.appium</groupId>
       <artifactId>java-client</artifactId>
       <version>${appium.version}</version>
   </dependency>
   ```
3. Document the rationale in commit message

### Dependency Guidelines
- Use stable releases (no SNAPSHOT versions in production code)
- Lock all versions explicitly
- Keep dependencies up-to-date quarterly
- Review security advisories before updates

## Testing Standards

### Test Structure
```java
public class FeatureTest extends BaseTest {

    @BeforeMethod
    public void setup() {
        // Initialize page objects
        // Set up test data
    }

    @Test(description = "Clear description of what is being tested")
    public void shouldExpectedBehaviorWhenCondition() {
        // Arrange - Set up test data
        // Act - Perform actions
        // Assert - Verify results
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        // Clean up test data
        // Capture screenshots if needed
    }
}
```

### Assertions
- Use AssertJ for fluent assertions: `assertThat(actual).isEqualTo(expected)`
- Avoid TestNG assertions: `assertEquals()` (less readable)
- Provide meaningful error messages

### Test Data
- Store in JSON or YAML files (`src/test/resources/testdata/`)
- Use TestNG `@DataProvider` for parameterized tests
- Create separate data classes for type safety

## Logging

### Logging Standards
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);

    public void doSomething() {
        LOG.info("Starting operation");
        LOG.debug("Debug details: {}", details);
        LOG.warn("Warning message");
        LOG.error("Error occurred", exception);
    }
}
```

### Log Levels
- **ERROR:** Errors that need immediate attention
- **WARN:** Potentially harmful situations
- **INFO:** General informational messages
- **DEBUG:** Detailed diagnostic information
- **TRACE:** Very detailed trace information (use sparingly)

## Security Guidelines

### Credentials
- Never commit `.properties` files with real credentials
- Use environment variables for sensitive data
- Load credentials in code, not from version control
- Use `.gitignore` to exclude sensitive files

### Secure Coding
- Validate all input data
- Use HTTPS for API calls
- Don't log sensitive data (passwords, tokens)
- Use proper exception handling (don't expose stack traces to users)

## Documentation

### Code Comments
- Document "why", not "what"
- Use JavaDoc for public APIs
- Keep comments synchronized with code
- Avoid obvious comments

### JavaDoc Example
```java
/**
 * Waits for an element to be visible and returns it.
 *
 * @param locator the element locator strategy
 * @param timeoutSeconds maximum time to wait in seconds
 * @return the visible WebElement
 * @throws TimeoutException if element not visible within timeout
 */
public WebElement waitForVisibleElement(By locator, int timeoutSeconds) {
    // Implementation
}
```

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
- `[FEAT]` New feature or test case
- `[FIX]` Bug fix
- `[REFACTOR]` Code restructuring
- `[DOCS]` Documentation updates
- `[CONFIG]` Configuration changes
- `[DEPS]` Dependency updates

## Code Review Checklist

- [ ] Follows naming conventions
- [ ] Proper error handling
- [ ] No hardcoded values (use constants)
- [ ] Appropriate logging
- [ ] Test coverage added
- [ ] Documentation updated
- [ ] No security issues
- [ ] Compatible with existing code style
- [ ] Builds successfully with `./mvnw clean verify`

## Tools & IDE Setup

### Recommended IDEs
- IntelliJ IDEA (Community or Ultimate)
- VS Code with Java Extension Pack
- Eclipse IDE

### IDE Configuration
- Set Java language level to 17
- Configure code formatter to follow conventions
- Enable inspection warnings
- Set UTF-8 encoding

### Maven Commands
```bash
./mvnw clean compile        # Compile source
./mvnw test                 # Run all tests
./mvnw verify              # Full verification (compile + test)
./mvnw clean package       # Create distributable
./mvnw clean install       # Install to local repository
```

## Quality Gates

Before committing:
1. Code compiles: `./mvnw clean compile`
2. All tests pass: `./mvnw test`
3. Code follows standards (manual review)
4. No security issues
5. Documentation updated

---

**Last Review:** 2025-12-22
**Next Review:** 2026-03-22
