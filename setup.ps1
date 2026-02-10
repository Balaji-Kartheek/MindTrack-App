# MindApp Project Setup Script (PowerShell)
# Run this script to set up the project for building

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  MindApp - Digital Wellbeing App Setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check for Java
Write-Host "[1/4] Checking Java..." -ForegroundColor Yellow
$javaCheck = Get-Command java -ErrorAction SilentlyContinue
if ($javaCheck) {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "  ✓ Found: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Java not found. Please install JDK 17 or higher." -ForegroundColor Red
    Write-Host "    Download: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# Check for Gradle wrapper
Write-Host ""
Write-Host "[2/4] Checking Gradle wrapper..." -ForegroundColor Yellow
if (Test-Path ".\gradlew") {
    Write-Host "  ✓ gradlew found" -ForegroundColor Green
} else {
    Write-Host "  ✗ gradlew not found. Generating..." -ForegroundColor Yellow
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        gradle wrapper --gradle-version 8.2
        Write-Host "  ✓ Gradle wrapper generated" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Gradle not installed. Checking for gradle-wrapper.jar..." -ForegroundColor Yellow
        if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
            Write-Host "  ⚠ gradle-wrapper.jar missing. Please run:" -ForegroundColor Yellow
            Write-Host "    gradle wrapper --gradle-version 8.2" -ForegroundColor Yellow
            Write-Host "    Or download from GitHub Actions build" -ForegroundColor Yellow
        }
    }
}

# Check gradle-wrapper.jar
if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
    Write-Host "  ⚠ gradle-wrapper.jar missing. Run: gradle wrapper --gradle-version 8.2" -ForegroundColor Yellow
}

# Check API keys
Write-Host ""
Write-Host "[3/4] Checking API configuration..." -ForegroundColor Yellow
$apiConfigPath = "app\src\main\java\com\mindapp\ApiConfig.kt"
if (Test-Path $apiConfigPath) {
    $apiConfig = Get-Content $apiConfigPath -Raw
    if ($apiConfig -match "YOUR_GEMINI_API_KEY_HERE") {
        Write-Host "  ⚠ Gemini API key not configured (Chat tab will show placeholder message)" -ForegroundColor Yellow
        Write-Host "    See API_KEYS_SETUP.md for instructions" -ForegroundColor Yellow
    } else {
        Write-Host "  ✓ Gemini API key appears to be set" -ForegroundColor Green
    }
    if ($apiConfig -match "YOUR_HUGGING_FACE_API_KEY_HERE") {
        Write-Host "  ⚠ Hugging Face API key not configured (Mood tab will show placeholder message)" -ForegroundColor Yellow
        Write-Host "    See API_KEYS_SETUP.md for instructions" -ForegroundColor Yellow
    } else {
        Write-Host "  ✓ Hugging Face API key appears to be set" -ForegroundColor Green
    }
} else {
    Write-Host "  ⚠ ApiConfig.kt not found" -ForegroundColor Yellow
}

# Build
Write-Host ""
Write-Host "[4/4] Building debug APK..." -ForegroundColor Yellow
if (Test-Path ".\gradlew") {
    if ($IsWindows -or $env:OS -match "Windows") {
        .\gradlew.bat assembleDebug --no-daemon
    } else {
        .\gradlew assembleDebug --no-daemon
    }
} else {
    Write-Host "  ✗ gradlew not found. Cannot build." -ForegroundColor Red
    Write-Host "    Please ensure Gradle wrapper is set up." -ForegroundColor Yellow
    exit 1
}

if (Test-Path "app\build\outputs\apk\debug\app-debug.apk") {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "  ✓ Setup complete! APK built successfully" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "  APK location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Next steps:" -ForegroundColor Yellow
    Write-Host "  - Install APK on your Android device" -ForegroundColor White
    Write-Host "  - Grant Usage Stats permission when prompted" -ForegroundColor White
    Write-Host "  - Add API keys in ApiConfig.kt if you want Chat/Mood features" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "  ✗ Build may have failed. Check the output above." -ForegroundColor Red
    exit 1
}
