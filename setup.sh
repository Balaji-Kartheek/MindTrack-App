#!/bin/bash
# MindApp Project Setup Script
# Run this script to set up the project for building

set -e

echo "=========================================="
echo "  MindApp - Digital Wellbeing App Setup"
echo "=========================================="
echo ""

# Check for Java
echo "[1/4] Checking Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "  ✓ Found: $JAVA_VERSION"
else
    echo "  ✗ Java not found. Please install JDK 17 or higher."
    echo "    Download: https://adoptium.net/"
    exit 1
fi

# Check for Gradle wrapper
echo ""
echo "[2/4] Checking Gradle wrapper..."
if [ -f "./gradlew" ]; then
    echo "  ✓ gradlew found"
    chmod +x gradlew
else
    echo "  ✗ gradlew not found. Generating..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.2
        chmod +x gradlew
        echo "  ✓ Gradle wrapper generated"
    else
        echo "  ✗ Gradle not installed. Installing gradle wrapper..."
        mkdir -p gradle/wrapper
        curl -L -o gradle/wrapper/gradle-wrapper.jar \
            "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || {
            echo "  Could not download wrapper. Please install Gradle: https://gradle.org/install/"
            exit 1
        }
        chmod +x gradlew 2>/dev/null || true
    fi
fi

# Check gradle-wrapper.jar
if [ ! -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "  ⚠ gradle-wrapper.jar missing. Run: gradle wrapper --gradle-version 8.2"
fi

# Check API keys
echo ""
echo "[3/4] Checking API configuration..."
if grep -q "YOUR_GEMINI_API_KEY_HERE" app/src/main/java/com/mindapp/ApiConfig.kt 2>/dev/null; then
    echo "  ⚠ Gemini API key not configured (Chat tab will show placeholder message)"
    echo "    See API_KEYS_SETUP.md for instructions"
else
    echo "  ✓ Gemini API key appears to be set"
fi
if grep -q "YOUR_HUGGING_FACE_API_KEY_HERE" app/src/main/java/com/mindapp/ApiConfig.kt 2>/dev/null; then
    echo "  ⚠ Hugging Face API key not configured (Mood tab will show placeholder message)"
    echo "    See API_KEYS_SETUP.md for instructions"
else
    echo "  ✓ Hugging Face API key appears to be set"
fi

# Build
echo ""
echo "[4/4] Building debug APK..."
./gradlew assembleDebug --no-daemon

if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "=========================================="
    echo "  ✓ Setup complete! APK built successfully"
    echo "=========================================="
    echo ""
    echo "  APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "  Next steps:"
    echo "  - Install APK on your Android device"
    echo "  - Grant Usage Stats permission when prompted"
    echo "  - Add API keys in ApiConfig.kt if you want Chat/Mood features"
    echo ""
else
    echo ""
    echo "  ✗ Build may have failed. Check the output above."
    exit 1
fi
