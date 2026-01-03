# Documentation Index

## Quick Navigation

### For New Team Members
1. Start: [README.md](../README.md) - Project overview & quick start
2. Read: [QUICKREF.md](./QUICKREF.md) - Quick reference guide
3. Learn: [cicd-pipeline.md](./cicd-pipeline.md) - CI/CD pipeline details

### For QA Engineers
- [QUICKREF.md](./QUICKREF.md) - Common commands and troubleshooting
- [reporting-guide.md](./reporting-guide.md) - Allure, TestNG, Jira reporting
- [project-overview-pdr.md](./project-overview-pdr.md) - Project scope and requirements

### For DevOps/Jenkins Admins
- [cicd-pipeline.md](./cicd-pipeline.md) - Pipeline architecture and configuration
- [system-architecture.md](./system-architecture.md) - Infrastructure overview
- [mcp-config.md](./mcp-config.md) - MCP/Jira defect creation setup
- [QUICKREF.md](./QUICKREF.md) - Troubleshooting guide

### For Developers
- [code-standards.md](./code-standards.md) - Coding standards and patterns
- [codebase-summary.md](./codebase-summary.md) - Project structure overview
- [system-architecture.md](./system-architecture.md) - System design
- [README.md](../README.md) - Test execution
- [../.github/copilot-instructions.md](../.github/copilot-instructions.md) - GitHub Copilot guidelines (Phase 06)

---

## Documentation Files

### README.md (Project Root)
**Purpose:** Project overview, prerequisites, quick start guide
**Sections:**
- Prerequisites (Java 17, Maven, BrowserStack, Claude Code CLI)
- Quick start (clone, config, build, test)
- Project structure
- Tech stack (including AI integration)
- Design patterns (Factory, Builder, Strategy, POM, Singleton, ThreadLocal)
- AI-augmented features (GitHub Copilot, Atlassian MCP, Claude Code)
- Configuration (environment variables, app setup)
- Test execution commands
- CI/CD Pipeline overview
- Documentation index
- Security guidelines

**Audience:** Everyone
**Last Updated:** Phase 06 (AI Integration)

---

### docs/QUICKREF.md (This Phase)
**Purpose:** Quick reference for daily development and troubleshooting
**Sections:**
- Jenkins URLs and credentials
- Quick test execution commands
- Local development setup
- Configuration files reference
- Troubleshooting table
- Common Maven commands
- Documentation index

**Size:** 157 lines
**Audience:** QA, Developers, Quick lookups

---

### docs/cicd-pipeline.md (Phase 05)
**Purpose:** Complete CI/CD pipeline documentation
**Sections:**
- Pipeline stages breakdown (7 stages)
- Stage commands and parameters
- Environment variables
- Pipeline options (timeout, retention)
- Reporting configuration files explained
- Failure categorization rules (6 categories)
- Jenkins setup prerequisites and steps
- Troubleshooting guide
- Performance optimization
- Scaling considerations

**Size:** 297 lines
**Audience:** DevOps, Jenkins admins, CI/CD engineers

---

### docs/reporting-guide.md (Phase 05)
**Purpose:** Comprehensive reporting architecture guide
**Sections:**
- Three-layer reporting (Allure, TestNG, Jira)
- Allure configuration and annotations
- Report structure and navigation (8 tabs)
- Failure categories (6 types)
- Environment info and executor metadata
- Local report generation
- TestNG/JUnit format and Jenkins integration
- Jira/Xray defect creation process
- Artifact retention and backup
- Troubleshooting
- Best practices

**Size:** 390 lines
**Audience:** QA, Test engineers, Report reviewers

---

### docs/project-overview-pdr.md
**Purpose:** Project scope, requirements, and acceptance criteria
**Sections:**
- Project overview
- Business objectives
- Product development requirements (PDR)
- Functional requirements
- Non-functional requirements
- Acceptance criteria
- Technical constraints
- Dependencies and risks

**Size:** 484 lines
**Audience:** Product, QA, Developers, Project Managers

---

### docs/code-standards.md
**Purpose:** Coding standards and architectural patterns
**Sections:**
- Code style and conventions
- Naming conventions
- File organization
- Error handling patterns
- Testing standards
- Documentation standards
- Performance guidelines
- Security practices

**Size:** 498 lines
**Audience:** Developers, Code reviewers

---

### docs/codebase-summary.md
**Purpose:** High-level codebase structure and overview
**Sections:**
- Project structure
- Module breakdown
- Key classes and dependencies
- Package organization
- Build configuration
- Integration points
- External dependencies

**Size:** 537 lines
**Audience:** Developers, Architects, Newcomers

---

### docs/system-architecture.md
**Purpose:** System design and architecture documentation
**Sections:**
- System overview
- Architecture layers
- Component diagrams
- Data flow
- Integration points
- BrowserStack integration
- Allure reporting architecture
- Scaling considerations
- Technology stack details

**Size:** 618 lines
**Audience:** Architects, Senior developers, DevOps

---

### docs/mcp-config.md (Phase 06)
**Purpose:** Atlassian MCP configuration and Jira integration setup
**Sections:**
- Prerequisites (Atlassian Cloud, Jira, Claude Code CLI)
- Setup steps (install CLI, add MCP server, authenticate)
- Available MCP actions (create issues, search, update)
- Example usage for defect creation
- Automated defect creation via scripts
- Troubleshooting guide
- Security notes (OAuth2, API tokens)

**Size:** 103 lines
**Audience:** DevOps, QA Engineers, Developers (MCP integration)

---

## Phase-07 Updates (User Journey Testing)

### New Files Created
1. **src/test/java/com/poc/geofence/e2e/GeofenceEntryTest.java** - E2E tests for geofence entry (User Journey TC-002, TC-004)

### Files Updated
1. **src/main/java/com/poc/geofence/pages/GeofencePage.java** - Added `waitForGeofenceEntryEvent()` method
2. **src/main/java/com/poc/geofence/pages/ios/GeofencePageIOS.java** - Implemented `waitForGeofenceEntryEvent()` with entry detection logic
3. **src/main/java/com/poc/geofence/pages/android/GeofencePageAndroid.java** - Implemented `waitForGeofenceEntryEvent()` + new `simulateEntryToGeofence()` method
4. **src/test/java/com/poc/geofence/data/TestDataProvider.java** - Added `iosGeofenceEntryData()` and `androidGeofenceEntryData()` providers
5. **testng.xml files** - Updated with new test case configurations
6. **docs/** - All documentation updated with User Journey information

### Files Removed
1. **src/test/java/com/poc/geofence/e2e/LocationAccuracyTest.java** - Replaced with GeofenceEntryTest.java (same test cases: TC-002, TC-004)

### Architecture Improvements
- User Journey Pattern: Tests validate full lifecycle (Safe → Exit → Entry)
- Android-specific entry simulation: `simulateEntryToGeofence()` for consistent testing
- Entry event detection: Platform-specific implementations (iOS alerts, Android notifications)

---

## Phase-06 Updates (AI Integration)

### New Files Created
1. **docs/mcp-config.md** - Atlassian MCP + Claude Code CLI setup guide
2. **.github/copilot-instructions.md** - GitHub Copilot context instructions

### Files Updated
1. **README.md** - Added AI-Augmented Features section with GitHub Copilot, MCP, Claude Code details
2. **src/main/java/com/poc/geofence/driver/DriverFactory.java** - AI-assisted comments
3. **src/test/java/com/poc/geofence/e2e/GeofenceExitTest.java** - AI-assisted comments
4. **scripts/create-xray-defects.sh** - Enhanced with AI integration notes

### Files Referenced
- Atlassian MCP integration (documented in mcp-config.md)
- GitHub Copilot instructions (documented in copilot-instructions.md)

---

## Phase-05 Updates

### New Files Created
1. **docs/cicd-pipeline.md** - Complete CI/CD pipeline documentation
2. **docs/reporting-guide.md** - Allure, TestNG, Jira reporting guide
3. **docs/QUICKREF.md** - Quick reference for daily use
4. **docs/INDEX.md** - Documentation index

### Files Updated
1. **README.md** - Added CI/CD Pipeline section (80+ lines)

### Files Referenced (Not Modified)
- Jenkinsfile (new, documented in cicd-pipeline.md)
- scripts/create-xray-defects.sh (new, documented in cicd-pipeline.md)
- allure.properties (updated, documented in cicd-pipeline.md)
- environment.properties (new, documented in reporting-guide.md)
- categories.json (new, documented in reporting-guide.md)
- executor.json (new, documented in reporting-guide.md)

---

## Key Concepts

### Pipeline Flow
```
Checkout → Build → API Tests → Android Tests → iOS Tests → Report → Jira Tickets (conditional)
```

### Failure Categories
1. Geofence Trigger Failures - Feature-level bugs
2. Location Accuracy Issues - Location detection problems
3. Notification Capture Failures - Notification system issues
4. API Failures - Backend validation errors
5. Infrastructure Issues - Timeouts, connection, BrowserStack
6. Test Defects - Test code assertion errors

### Reporting Layers
- **Allure** - Rich HTML reports with failure categories and environment info
- **TestNG/JUnit** - Jenkins integration and test result tracking
- **Jira/Xray** - Auto-created defects on pipeline failure

### Jenkins Credentials
- `browserstack-username` - BrowserStack authentication
- `browserstack-access-key` - BrowserStack authentication

---

## Common Workflows

### Local Test Execution
```bash
# API tests
./mvnw test -DsuiteXml=testng-api.xml

# Android tests
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key
./mvnw test -Dplatform=android -Denvironment=browserstack

# View Allure report
./mvnw allure:serve  # http://localhost:4040
```

### Jenkins Pipeline Trigger
- Push to main/develop branch
- Or manually via Jenkins UI → "Build Now"

### Check Pipeline Status
- Jenkins build page: `https://jenkins.example.com/job/geofence-automation/`
- Latest Allure report: `https://jenkins.example.com/job/geofence-automation/{BUILD_NUMBER}/allure/`

### Review Test Failures
1. Check Jenkins build log (Pipeline execution details)
2. View Allure report (rich failure info with screenshots)
3. Review Jira defects (auto-created issues)

---

## Troubleshooting Quick Links

| Issue | Doc | Solution |
|-------|-----|----------|
| Build timeout | cicd-pipeline.md | Increase timeout, check BrowserStack |
| No Allure report | reporting-guide.md | Verify directory, check Maven config |
| Defects not created | reporting-guide.md | Run script manually, check Claude Code CLI |
| Missing credentials | cicd-pipeline.md | Add to Jenkins Manage Credentials |
| Local test failure | QUICKREF.md | Run clean build, check config files |

---

## Documentation Maintenance

### Update Schedule
- Monthly: Review for accuracy
- Per release: Update examples and versions
- Quarterly: Review failure categories and patterns

### How to Update
1. Edit relevant markdown file
2. Test examples locally
3. Update version/date in file header if applicable
4. Commit with clear message

### File Organization
```
docs/
├── INDEX.md (this file)
├── QUICKREF.md
├── cicd-pipeline.md
├── reporting-guide.md
├── project-overview-pdr.md
├── code-standards.md
├── codebase-summary.md
└── system-architecture.md
```

---

## External References

- [Allure Documentation](https://docs.qameta.io/allure/)
- [TestNG Documentation](https://testng.org/)
- [Appium Documentation](https://appium.io/)
- [BrowserStack Docs](https://www.browserstack.com/docs)
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Maven Documentation](https://maven.apache.org/)
- [Jira REST API](https://developer.atlassian.com/cloud/jira/platform/rest/)

---

**Last Updated:** 2025-12-24
**Phase:** Phase 07 (User Journey Testing)
**Status:** Complete
