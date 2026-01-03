#!/bin/bash
# Parses Allure results and creates Jira/Xray defects via Claude Code + MCP
# Enhanced version with detailed failure parsing and structured output

set -e

ALLURE_RESULTS="${ALLURE_RESULTS:-target/allure-results}"
JIRA_PROJECT="${JIRA_PROJECT:-GEOFENCE}"
BUILD_NUMBER="${BUILD_NUMBER:-local}"

echo "===== Geofence Automation - Xray Defect Creator ====="
echo "Allure Results: $ALLURE_RESULTS"
echo "Jira Project: $JIRA_PROJECT"
echo "Build: $BUILD_NUMBER"

# Check prerequisites
if [ ! -d "$ALLURE_RESULTS" ]; then
    echo "No allure-results directory found. Skipping defect creation."
    exit 0
fi

if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is required. Install via: brew install jq"
    exit 1
fi

if ! command -v claude &> /dev/null; then
    echo "ERROR: Claude Code CLI not found."
    echo "Install: https://docs.anthropic.com/en/docs/claude-code"
    exit 1
fi

# Parse failed tests from Allure JSON
echo ""
echo "===== Parsing Allure Results ====="

FAILURES=$(find "$ALLURE_RESULTS" -name "*-result.json" -exec cat {} \; 2>/dev/null | \
    jq -s '[.[] | select(.status == "failed" or .status == "broken")] |
           .[] | {
             name: .name,
             status: .status,
             statusDetails: .statusDetails,
             labels: [.labels[]? | {(.name): .value}] | add,
             start: .start,
             stop: .stop
           }')

FAILURE_COUNT=$(echo "$FAILURES" | jq -s 'length')

if [ "$FAILURE_COUNT" -eq 0 ]; then
    echo "No failures detected. All tests passed!"
    exit 0
fi

echo "Found $FAILURE_COUNT failed/broken tests"

# Create defects for each failure
echo ""
echo "===== Creating Xray Defects via Claude Code + MCP ====="

# Build detailed failure report
FAILURE_REPORT=$(echo "$FAILURES" | jq -s '
    .[] |
    "## \(.name)\n" +
    "- Status: \(.status)\n" +
    "- Platform: \(.labels.platform // "unknown")\n" +
    "- Epic: \(.labels.epic // "Mobile Geofence Automation")\n" +
    "- Feature: \(.labels.feature // "unknown")\n" +
    "- Severity: \(.labels.severity // "normal")\n" +
    "- TmsLink: \(.labels.tms // "none")\n" +
    "- Error: \(.statusDetails.message // "No message")\n" +
    "- Trace: \(.statusDetails.trace // "No trace" | .[0:500])\n"
' | tr -d '"')

# Invoke Claude Code with MCP to create defects
claude --print << EOF
You have access to Atlassian MCP. Please analyze these test failures and create Xray Bug issues:

$FAILURE_REPORT

For each failure, create a Jira Bug in project $JIRA_PROJECT with:

1. **Summary**: "[AUTOMATION] {TestName} - {Platform}"
2. **Description**:
   - Test Name: {name}
   - Platform: {platform}
   - Severity: {severity}
   - Error Message: {error}
   - Stack Trace (first 500 chars): {trace}
   - Build Number: $BUILD_NUMBER
   - Steps to Reproduce:
     1. Run test suite via Jenkins
     2. Test case: {TestName}
     3. Expected: Test passes
     4. Actual: {Error details}
3. **Issue Type**: Bug
4. **Priority**: High (for CRITICAL severity), Medium (for NORMAL)
5. **Labels**: automation, mobile, geofence, {platform}
6. **Components**: Automation (if exists)
7. **Link**: If TmsLink is present, link to that test case

After creating each defect, output the Jira issue key (e.g., GEOFENCE-123).

Please proceed with creating the defects now.
EOF

echo ""
echo "===== Defect Creation Complete ====="
echo "Check Jira project $JIRA_PROJECT for new automation defects"
