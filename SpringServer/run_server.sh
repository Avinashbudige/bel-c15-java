#!/bin/bash
# Ensure the script is executable: chmod +x run_server.sh

echo "Building and starting Spring Server..."
# Use the Gradle wrapper to run the application
./gradlew run
