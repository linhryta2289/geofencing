#!/bin/bash
# Start Jenkins Docker container for Geofence Automation
# Usage: ./start.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  Geofence Automation - Jenkins Setup"
echo "=========================================="

# Check .env file
if [ ! -f .env ]; then
    echo ""
    echo "ERROR: .env file not found"
    echo ""
    echo "To configure:"
    echo "  1. cp .env.example .env"
    echo "  2. Edit .env with your credentials"
    echo "  3. Run ./start.sh again"
    echo ""
    exit 1
fi

# Load environment variables
set -a
source .env
set +a

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker not installed"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "ERROR: Docker Compose not installed"
    exit 1
fi

# Determine docker compose command
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# Build and start
echo ""
echo "Building Jenkins image..."
$COMPOSE_CMD build

echo ""
echo "Starting Jenkins container..."
$COMPOSE_CMD up -d

echo ""
echo "=========================================="
echo "  Jenkins starting at http://localhost:${JENKINS_PORT:-8080}"
echo "  Default login: admin / ${JENKINS_ADMIN_PASSWORD:-admin}"
echo "=========================================="

# Wait for Jenkins to be ready
echo ""
echo "Waiting for Jenkins to be ready..."
MAX_WAIT=180
WAIT=0

while [ $WAIT -lt $MAX_WAIT ]; do
    if curl -s "http://localhost:${JENKINS_PORT:-8080}/login" > /dev/null 2>&1; then
        echo ""
        echo "Jenkins is ready!"

        # Validate project mount
        echo ""
        echo "Validating project mount..."
        if docker exec jenkins-geofence test -f /workspace/poc/automation-test/Jenkinsfile 2>/dev/null; then
            echo "  ✓ Project mounted successfully at /workspace/poc"
        else
            echo "  ✗ WARNING: Project mount not accessible"
            echo "    Check Docker Desktop → Settings → Resources → File Sharing"
            echo "    Ensure ${PROJECT_PATH:-/Users/soanguyen/Development/poc} is shared"
        fi

        echo ""
        echo "Next steps:"
        echo "  1. Open http://localhost:${JENKINS_PORT:-8080}"
        echo "  2. Login with admin / ${JENKINS_ADMIN_PASSWORD:-admin}"
        echo "  3. Run 'geofence-automation' job (Build with Parameters)"
        echo ""
        exit 0
    fi
    sleep 5
    WAIT=$((WAIT + 5))
    echo "  Waiting... (${WAIT}s / ${MAX_WAIT}s)"
done

echo ""
echo "WARNING: Jenkins not ready after ${MAX_WAIT}s"
echo "Check logs with: $COMPOSE_CMD logs -f"
exit 1
