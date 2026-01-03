# Geofence Automation Framework

Production-ready mobile automation framework demonstrating senior-level architecture, AI integration, and cross-platform geofence testing.

## Framework Highlights

| Aspect | Implementation |
|--------|----------------|
| **Architecture** | Factory, Builder, Strategy, POM, Singleton, ThreadLocal |
| **Mobile** | Appium 9.3.0 + BrowserStack + iOS + Android |
| **API** | RestAssured 5.5.0 + WireMock |
| **Reporting** | Allure 2.29.0 |
| **CI/CD** | Jenkins Declarative Pipeline |
| **AI Integration** | GitHub Copilot + Claude Code + Atlassian MCP |

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+ (or use included `./mvnw` wrapper)
- BrowserStack account
- Claude Code CLI (for AI features)

### Setup

```bash
# Clone and navigate
cd automation-test

# Copy config templates
cp src/test/resources/config/config.properties.example src/test/resources/config/config.properties
cp src/test/resources/config/browserstack.properties.example src/test/resources/config/browserstack.properties

# Configure credentials
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_key

# Build
./mvnw clean compile
```

### Run Tests

```bash
# All tests
./mvnw test -DsuiteXml=testng.xml

# API tests only
./mvnw test -DsuiteXml=testng-api.xml

# Android tests
./mvnw test -Dplatform=android -DsuiteXml=testng-android.xml

# iOS tests
./mvnw test -Dplatform=ios -DsuiteXml=testng-ios.xml
```

### Generate Report

```bash
./mvnw allure:serve
```

## Project Structure

```
automation-test/
├── src/main/java/com/poc/geofence/
│   ├── config/        # ConfigManager, Environment, PlatformType
│   ├── driver/        # DriverFactory, DriverManager, capabilities/
│   ├── pages/         # BasePage, GeofencePage, ios/, android/
│   ├── components/    # NotificationHandler, AlertHandler
│   ├── utils/         # LocationSimulator, WaitUtils, AllureUtils
│   └── api/           # ApiClient, GeofenceApiService
├── src/test/java/com/poc/geofence/
│   ├── base/          # BaseTest, TestListener
│   ├── e2e/           # GeofenceExitTest, LocationAccuracyTest
│   ├── api/           # GeofenceApiTest
│   └── data/          # TestDataProvider
└── src/test/resources/
    ├── config/        # config.properties, browserstack.properties
    └── testdata/      # geofence-locations.json
```

## Test Cases

| ID | Type | Scenario | Platform |
|----|------|----------|----------|
| TC-001 | E2E | Geofence exit detection | iOS |
| TC-002 | E2E | Geofence entry detection (return to safe zone) | iOS |
| TC-003 | E2E | Geofence exit detection | Android |
| TC-004 | E2E | Geofence entry detection (return to safe zone) | Android |
| API-001 | API | POST /api/geofence | N/A |
| API-002 | API | GET /api/geofence/{id} | N/A |

## Design Patterns

### Factory Pattern (DriverFactory)
Creates platform-specific drivers without exposing instantiation logic.

### Builder Pattern (CapabilityBuilder)
Fluent API for constructing complex capability configurations.

### Strategy Pattern (PageFactory)
Resolves platform-specific page implementations at runtime.

### Singleton Pattern (ConfigManager)
Single configuration instance shared across framework.

### ThreadLocal (DriverManager)
Thread-safe driver storage for parallel execution.

## AI-Augmented Features

### GitHub Copilot
- Used throughout development for code generation
- Instructions defined in `.github/copilot-instructions.md`

### Atlassian MCP Integration
- Auto-create Xray defects from test failures
- Traceability: Test Case → Test Execution → Bug
- Setup guide: `docs/mcp-config.md`

### Claude Code
- Code review and refactoring
- MCP-powered Jira integration
- Defect creation script: `scripts/create-xray-defects.sh`

## CI/CD Pipeline

```
Checkout → Build → API Tests → Android Tests → iOS Tests → Allure Report → Xray Defects
```

**Constraints**: BrowserStack Free Plan (1 parallel session, 100 min/month)

### Jenkins Configuration

Required credentials:
- `browserstack-username`
- `browserstack-access-key`

Environment variables:
```
JIRA_PROJECT=GEOFENCE
BUILD_TAG={BUILD_NUMBER}-{timestamp}
```

## Geofence Testing Notes

- Apps use 200m geofence radius (hardcoded)
- Exit simulation: 250m+ from center
- Trigger delay: Up to 3 minutes (iOS batches location updates)
- Use 120s timeout for exit detection

## Configuration

### Environment Variables

| Variable | Description |
|----------|-------------|
| `BROWSERSTACK_USERNAME` | BrowserStack username |
| `BROWSERSTACK_ACCESS_KEY` | BrowserStack access key |
| `BUILD_NUMBER` | CI build number (optional) |
| `JIRA_PROJECT` | Jira project key for defects |

### App Setup

1. Build iOS (.ipa) and Android (.apk) apps
2. Upload to BrowserStack: `browserstack-cli upload <app-file>`
3. Update `config.properties` with app IDs:
   ```properties
   app.ios.path=bs://YOUR_IOS_APP_ID
   app.android.path=bs://YOUR_ANDROID_APP_ID
   ```

## Documentation

| Document | Location |
|----------|----------|
| Documentation Index | `docs/INDEX.md` |
| Project Overview & PDR | `docs/project-overview-pdr.md` |
| Codebase Summary | `docs/codebase-summary.md` |
| Code Standards | `docs/code-standards.md` |
| System Architecture | `docs/system-architecture.md` |
| CI/CD Pipeline | `docs/cicd-pipeline.md` |
| Reporting Guide | `docs/reporting-guide.md` |
| MCP Setup | `docs/mcp-config.md` |
| Quick Reference | `docs/QUICK-REFERENCE.md` |
| Copilot Instructions | `.github/copilot-instructions.md` |

## Security

- Never commit real credentials
- Use environment variables for sensitive data
- `.gitignore` excludes `.properties` files (except examples)

## License

MIT License - See LICENSE file
