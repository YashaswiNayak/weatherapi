#!/bin/bash
# Exit on error is OFF for Windows compatibility
# set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

log_info "Running pre-flight checks..."

# Check Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    log_error "Java 17+ required. Found: Java $JAVA_VERSION"
    exit 1
fi
log_info "✓ Java version: $JAVA_VERSION"

# Skip memory check on Windows (doesn't have 'free' command)
if command -v free &> /dev/null; then
    TOTAL_MEM_MB=$(free -m | awk '/^Mem:/{print $2}')
    log_info "✓ System memory: ${TOTAL_MEM_MB}MB"
else
    log_warn "⚠ Memory check skipped (Windows)"
fi

# Check disk space (works on Git Bash)
DISK_GB=$(df -h . 2>/dev/null | awk 'NR==2 {print $4}')
if [ -n "$DISK_GB" ]; then
    log_info "✓ Disk space: $DISK_GB available"
fi

# Clean daemons
log_info "Stopping Gradle daemons..."
./gradlew --stop > /dev/null 2>&1 || true
sleep 2
log_info "✓ Gradle daemons stopped"

# Check .env
if [ ! -f ".env" ]; then
    log_error "❌ .env file not found"
    exit 1
fi

if ! grep -q "WEATHER_API_KEY" .env; then
    log_error "❌ WEATHER_API_KEY not in .env"
    exit 1
fi

log_info "✓ Environment configured"

# Create logs directory
mkdir -p build/logs

# Build profiles
FAILED=()
for PROFILE in dev staging prod; do
    log_info "================================================"
    log_info "Building profile: $PROFILE"
    log_info "================================================"

    export SPRING_PROFILES_ACTIVE="$PROFILE"

    if ./gradlew clean build -x test --no-daemon --console=plain 2>&1 | tee "build/logs/build-${PROFILE}.log" | grep -q "BUILD SUCCESSFUL"; then
        log_info "✓ Profile '$PROFILE' succeeded"
    else
        log_error "✗ Profile '$PROFILE' failed"
        FAILED+=("$PROFILE")
    fi
    sleep 2
done

# Run tests
log_info "================================================"
log_info "Running tests..."
log_info "================================================"

export SPRING_PROFILES_ACTIVE="dev"

if ./gradlew test --no-daemon --console=plain 2>&1 | tee "build/logs/test.log" | grep -q "BUILD SUCCESSFUL"; then
    log_info "✓ Tests passed"
else
    log_error "✗ Tests failed"
    FAILED+=("tests")
fi

# Stop daemons
./gradlew --stop > /dev/null 2>&1 || true

# Report
log_info "================================================"
if [ ${#FAILED[@]} -eq 0 ]; then
    log_info "🎉 All validations passed!"
    log_info "Build artifacts: build/libs/"
    log_info "Build logs: build/logs/"
    exit 0
else
    log_error "❌ Failed: ${FAILED[*]}"
    log_error "Check logs: build/logs/"
    exit 1
fi
