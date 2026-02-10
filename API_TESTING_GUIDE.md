# API Testing & Fix Guide

## 🔍 What Was The Problem?

Your `.env` file had API keys, but Android apps don't read `.env` files. The app reads from `BuildConfig` which is generated at **build time** from:
1. Environment variables (for CI/CD)
2. Now also from `local.properties` (for local development) ✅

## ✅ What's Been Fixed?

### 1. **Updated `build.gradle`**
Now reads API keys from `local.properties` first, then environment variables, then placeholders.

### 2. **Created `local.properties`**
Your API keys from `.env` are now in `local.properties` (proper Android way).

### 3. **Created Testing Tools**
- `ApiTester.kt` - Utility to test API connections
- `ApiTestFragment.kt` - UI screen to test both APIs

## 🧪 How To Test APIs

### Option 1: Add Test Fragment to Your App

1. Open `app/src/main/res/navigation/nav_graph.xml`
2. Add a new fragment:

```xml
<fragment
    android:id="@+id/navigation_api_test"
    android:name="com.mindapp.ApiTestFragment"
    android:label="API Test"
    tools:layout="@layout/fragment_api_test" />
```

3. Add to bottom navigation menu in `res/menu/bottom_nav_menu.xml`:

```xml
<item
    android:id="@+id/navigation_api_test"
    android:icon="@android:drawable/ic_menu_info_details"
    android:title="API Test" />
```

4. Rebuild and run the app
5. Navigate to "API Test" tab
6. Click "Test Both APIs"

### Option 2: Use Logcat (Quick Test)

Add this to `MainActivity.onCreate()`:

```kotlin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.mindapp.ApiTester

// In onCreate, after super.onCreate()
CoroutineScope(Dispatchers.Main).launch {
    val results = ApiTester.testCurrentConfiguration()
    android.util.Log.d("API_TEST", results.first)
    android.util.Log.d("API_TEST", results.second)
}
```

Then check Logcat for results.

## 🏗️ How To Build & Test

### Step 1: Clean and Rebuild

```bash
# Windows PowerShell
cd "D:\Internship\FreeLance\MindTrack\MindApp"
.\gradlew clean
.\gradlew assembleDebug
```

### Step 2: Install APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Test in App

Open the app and try:
- **Chatbot tab** - Send a message
- **Mood tab** - Analyze emotion
- **API Test tab** (if you added it) - Test both APIs

## 🔑 Understanding The Flow

```
1. You put keys in local.properties
         ↓
2. Gradle reads them during build
         ↓
3. Keys are compiled into BuildConfig
         ↓
4. ApiConfig reads from BuildConfig
         ↓
5. App uses ApiConfig at runtime
```

**Important**: Changes to `local.properties` require a rebuild!

## 🐛 Still Having Issues?

### Check 1: Verify Keys Are Loaded

Add this temporary code to `MainActivity`:

```kotlin
android.util.Log.d("API_CONFIG", "Gemini configured: ${ApiConfig.isGeminiConfigured()}")
android.util.Log.d("API_CONFIG", "Gemini key: ${ApiConfig.GEMINI_API_KEY}")
android.util.Log.d("API_CONFIG", "HF configured: ${ApiConfig.isHuggingFaceConfigured()}")
android.util.Log.d("API_CONFIG", "HF key: ${ApiConfig.HUGGING_FACE_API_KEY}")
```

### Check 2: Verify Internet Permissions

Ensure `AndroidManifest.xml` has:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Check 3: Test APIs Manually

Use online tools:
- **Gemini**: Test at https://aistudio.google.com/
- **Hugging Face**: Test at https://huggingface.co/j-hartmann/emotion-english-distilroberta-base

## 📚 Learning Points

1. **Android doesn't use .env files** - Use `local.properties` instead
2. **BuildConfig is compile-time** - Need to rebuild after changing keys
3. **local.properties is gitignored** - Safe for local development
4. **Environment variables** - Used for CI/CD (GitHub Actions)

## 🎯 Next Steps

1. Rebuild your app with the updated `build.gradle`
2. Test using the `ApiTestFragment` or Logcat method
3. If successful, you can delete the `.env` file (it's not being used)
4. Keep `local.properties` for local development
5. Keep using GitHub Secrets for CI/CD builds
