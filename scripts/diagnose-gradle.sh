#!/bin/bash

echo "=== Gradle Environment Diagnostics ==="
echo ""

echo "1. Gradle Version:"
./gradlew --version

echo ""
echo "2. Active Daemons:"
./gradlew --status

echo ""
echo "3. JVM Info:"
java -XX:+PrintFlagsFinal -version 2>&1 | grep -i "maxheap\|metaspace"

echo ""
echo "4. System Resources:"
echo "  Total RAM: $(free -h | awk '/^Mem:/{print $2}')"
echo "  Available RAM: $(free -h | awk '/^Mem:/{print $7}')"
echo "  Disk Space: $(df -h . | awk 'NR==2 {print $4}')"

echo ""
echo "5. Gradle Properties:"
cat gradle.properties | grep -v '^#' | grep -v '^$'

echo ""
echo "6. Recent Build Logs:"
ls -lht build/logs/ 2>/dev/null || echo "  No logs found"

echo ""
echo "7. Heap Dump Location:"
ls -lh build/heap-dumps/*.hprof 2>/dev/null || echo "  No heap dumps found"
