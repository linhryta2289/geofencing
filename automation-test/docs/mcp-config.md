# Atlassian MCP Configuration Guide

Setup guide for Claude Code + Atlassian MCP integration for automated Xray defect creation.

## Prerequisites

- Atlassian Cloud account (jira.atlassian.com)
- Jira project with Xray app installed
- Claude Code CLI installed

## Setup Steps

### 1. Install Claude Code CLI

```bash
# macOS
brew install claude-code

# Or via npm
npm install -g @anthropic-ai/claude-code

# Verify installation
claude --version
```

### 2. Add MCP Server

```bash
# Global scope (recommended)
claude mcp add --transport sse atlassian https://mcp.atlassian.com/v1/sse

# Project scope only
cd /path/to/automation-test
claude mcp add --transport sse atlassian https://mcp.atlassian.com/v1/sse --scope project
```

### 3. Authenticate

On first use, Claude Code opens browser for OAuth:
1. Login with Atlassian account
2. Grant permissions to Claude Code
3. Return to terminal

### 4. Verify Connection

```bash
claude
> /mcp
# Output: atlassian (sse) - connected

> Use Atlassian MCP to list projects in Jira
# Should return accessible projects
```

## Available MCP Actions

| Action | Description |
|--------|-------------|
| Create Jira Issue | Create Bug, Test, Task issues |
| Update Jira Issue | Modify existing issues |
| Search Jira | JQL queries |
| Create Confluence Page | Documentation pages |
| Get Issue Details | Fetch full issue data |

## Example Usage

```
> Create a Jira Bug in project GEOFENCE with:
  - Summary: [AUTOMATION] TC-001 failed on iOS
  - Description: Geofence exit not detected within 120s
  - Priority: High
  - Labels: automation, ios, geofence
```

## Automated Defect Creation

The `scripts/create-xray-defects.sh` script automates defect creation:

```bash
# Set Jira project key
export JIRA_PROJECT=GEOFENCE

# Run after test failures
./scripts/create-xray-defects.sh
```

Script parses Allure results and creates Xray Bug issues for each failure.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| MCP not connecting | Check network, re-add server |
| Auth expired | Run any MCP command to re-auth |
| Project not found | Verify project key, check permissions |
| Rate limits | Batch defect creation, add delays |

## Security Notes

- MCP uses OAuth2 - no credentials stored locally
- Never log or commit Jira API tokens
- Revoke access: Atlassian Account > Security > Connected apps
