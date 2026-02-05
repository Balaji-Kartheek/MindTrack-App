#!/bin/bash
# Helper script to download gradle-wrapper.jar
# This can be run locally if needed

mkdir -p gradle/wrapper

echo "Downloading gradle-wrapper.jar for Gradle 8.2..."

# Try multiple sources
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar || \
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://services.gradle.org/distributions/gradle-8.2-wrapper.jar || {
  echo "Download failed. Please run: gradle wrapper --gradle-version 8.2"
  exit 1
}

echo "Download complete!"
ls -lh gradle/wrapper/gradle-wrapper.jar
