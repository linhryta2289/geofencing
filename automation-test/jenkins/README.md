# Jenkins Setup for Geofence Automation

Docker-based Jenkins CI/CD setup for running mobile automation tests.

## Prerequisites

- Docker & Docker Compose installed
- Ports 8080 and 50000 available
- BrowserStack and Jira credentials

## Quick Start

```bash
# 1. Configure credentials
cp .env.example .env
# Edit .env with your credentials

# 2. Start Jenkins
./start.sh

# 3. Access Jenkins at http://localhost:8080
# Login: admin / admin (or your configured password)
```

## Included Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Jenkins | 2.479.2 LTS | CI/CD Server |
| Java | 17 (Temurin) | Runtime |
| Maven | 3.9.9 | Build tool |
| Allure | 2.29.0 | Test reporting |

## Pre-installed Plugins

- Pipeline (workflow-aggregator)
- Git integration
- Credentials Binding
- Allure Jenkins Plugin
- JUnit
- AnsiColor, Timestamper

## Credentials

The following credentials are auto-configured from `.env`:

| ID | Environment Variable |
|----|---------------------|
| browserstack-username | BROWSERSTACK_USERNAME |
| browserstack-access-key | BROWSERSTACK_ACCESS_KEY |
| jira-base-url | JIRA_BASE_URL |
| jira-username | JIRA_USERNAME |
| jira-api-token | JIRA_API_TOKEN |
| api-base-url-staging | API_BASE_URL_STAGING |
| api-base-url-uat | API_BASE_URL_UAT |
| api-base-url-prod | API_BASE_URL_PROD |
| api-auth-token | API_AUTH_TOKEN |

## Management Commands

```bash
# Start Jenkins
./start.sh

# Stop Jenkins (preserve data)
./stop.sh

# Stop and remove all data
./stop.sh --clean

# View logs
docker-compose logs -f

# Shell into container
docker exec -it jenkins-geofence bash

# Verify tools
docker exec jenkins-geofence mvn --version
docker exec jenkins-geofence allure --version
```

## Local Project Mount

The Jenkins container mounts your local project directory for direct access to source code and Jenkinsfile.

### macOS Setup (Required)

1. Open **Docker Desktop** → Settings → Resources → File Sharing
2. Add path: `/Users/soanguyen/Development/poc`
3. Click **Apply & Restart**

### Configuration

Set `PROJECT_PATH` in `.env`:
```bash
PROJECT_PATH=/Users/soanguyen/Development/poc
```

### Verification

```bash
# Check mount inside container
docker exec jenkins-geofence ls -la /workspace/poc/automation-test/

# Should show: Jenkinsfile, pom.xml, src/, etc.
```

### Pipeline Job

The job is pre-configured to:
1. Verify mount exists
2. Build project with Maven
3. Run API tests (WireMock)
4. Optionally run mobile tests (BrowserStack)
5. Generate Allure report

**Build Parameters:**
- `API_MODE`: mock, staging, or uat
- `RUN_MOBILE_TESTS`: Enable BrowserStack tests (uses quota)

### Security Note (POC Only)

The embedded pipeline uses `sandbox(false)` which disables script security.
This is required for local mount approach but **not suitable for production**.

For production, use SCM-based Jenkinsfile instead:
1. Push code to Git repository
2. Configure job to use "Pipeline script from SCM"
3. Set Script Path to `automation-test/Jenkinsfile`

## Security Notes

**This setup is for POC/development use.** For production:

1. **Secrets Management**: Replace env vars with Docker Secrets or HashiCorp Vault
2. **Docker Socket**: Remove `/var/run/docker.sock` mount if Docker-in-Docker not needed
3. **Admin Password**: Set strong password via `JENKINS_ADMIN_PASSWORD`
4. **Network**: Use private network, add reverse proxy with TLS
5. **Resource Limits**: Add CPU/memory limits to docker-compose.yml

## Files

```
jenkins/
├── docker-compose.yml   # Docker Compose stack
├── Dockerfile           # Custom Jenkins image
├── plugins.txt          # Jenkins plugins
├── jenkins.yaml         # JCasC configuration
├── .env.example         # Environment template
├── start.sh             # Start script
├── stop.sh              # Stop script
└── init.groovy.d/       # Initialization scripts
    ├── 01-disable-setup-wizard.groovy
    ├── 02-credentials.groovy
    ├── 03-tools.groovy
    └── 04-job-dsl.groovy
```
