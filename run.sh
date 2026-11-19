#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
javac -encoding UTF-8 -cp 'lib/*' CarRentalRepairSystem.java
echo 'Open http://localhost:8080 and press Ctrl+C to stop.'
java -cp '.:lib/*' CarRentalRepairSystem
