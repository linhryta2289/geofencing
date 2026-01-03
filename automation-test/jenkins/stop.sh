#!/bin/bash
# Stop Jenkins Docker container
# Usage: ./stop.sh [--clean]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Determine docker compose command
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

if [ "$1" == "--clean" ]; then
    echo "Stopping and removing Jenkins (including volumes)..."
    $COMPOSE_CMD down -v
    echo "Jenkins stopped and data removed."
else
    echo "Stopping Jenkins..."
    $COMPOSE_CMD down
    echo "Jenkins stopped. Data preserved in volume."
    echo ""
    echo "To remove all data, run: ./stop.sh --clean"
fi
