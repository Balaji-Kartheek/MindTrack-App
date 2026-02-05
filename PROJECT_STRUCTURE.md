# MindApp - Project Structure

## Complete File Tree

```
MindApp/
├── .github/
│   └── workflows/
│       └── build.yml                    # GitHub Actions CI/CD workflow
├── .gitignore                           # Git ignore rules
├── app/
│   ├── build.gradle                     # App-level Gradle configuration
│   ├── proguard-rules.pro               # ProGuard rules for code obfuscation
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml      # App manifest with permissions
│           ├── java/com/mindapp/
│           │   ├── ApiConfig.kt         # API keys configuration (EDIT THIS!)
│           │   ├── MainActivity.kt      # Main activity with bottom navigation
│           │   ├── chatbot/
│           │   │   ├── ChatAdapter.kt           # RecyclerView adapter for chat
│           │   │   ├── ChatbotFragment.kt       # Chatbot UI fragment
│           │   │   └── GeminiService.kt          # Retrofit interface for Gemini API
│           │   ├── mood/
│           │   │   ├── EmotionService.kt        # Retrofit interface for Hugging Face
│           │   │   └── MoodCheckFragment.kt     # Emotion detection fragment
│           │   ├── notification/
│           │   │   └── NotificationHelper.kt    # Notification management
│           │   └── usage/
│           │       ├── TopAppsAdapter.kt        # RecyclerView adapter for top apps
│           │       ├── UsageStatsFragment.kt    # Usage stats UI fragment
│           │       └── UsageStatsHelper.kt      # Usage stats logic
│           └── res/
│               ├── drawable/
│               │   └── ic_notification.xml      # Notification icon
│               ├── layout/
│               │   ├── activity_main.xml                # Main activity layout
│               │   ├── fragment_chatbot.xml             # Chatbot fragment layout
│               │   ├── fragment_mood_check.xml          # Mood check fragment layout
│               │   ├── fragment_usage_stats.xml          # Usage stats fragment layout
│               │   ├── item_message_bot.xml              # Bot message item layout
│               │   ├── item_message_user.xml             # User message item layout
│               │   └── item_top_app.xml                  # Top app item layout
│               ├── menu/
│               │   └── bottom_navigation_menu.xml        # Bottom navigation menu
│               └── values/
│                   ├── colors.xml                        # Color definitions
│                   ├── strings.xml                       # String resources
│                   └── themes.xml                        # App theme
├── build.gradle                          # Project-level Gradle configuration
├── gradle.properties                     # Gradle properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties         # Gradle wrapper properties
├── settings.gradle                       # Gradle settings
├── README.md                             # Main documentation
└── PROJECT_STRUCTURE.md                   # This file
```

## Key Files to Edit

### 1. API Configuration
**File**: `app/src/main/java/com/mindapp/ApiConfig.kt`

This is the **MOST IMPORTANT** file to edit before building:
- Replace `YOUR_GEMINI_API_KEY_HERE` with your actual Gemini API key
- Replace `YOUR_HUGGING_FACE_API_KEY_HERE` with your actual Hugging Face token

### 2. Build Configuration
**Files**: 
- `app/build.gradle` - App dependencies and build settings
- `build.gradle` - Project-level settings
- `settings.gradle` - Project structure

### 3. Manifest
**File**: `app/src/main/AndroidManifest.xml`
- Contains all required permissions
- Defines MainActivity

## Package Structure

```
com.mindapp
├── MainActivity.kt              # Entry point
├── ApiConfig.kt                 # API keys
├── chatbot/                     # Chatbot feature
│   ├── ChatbotFragment.kt       # UI
│   ├── GeminiService.kt         # API interface
│   └── ChatAdapter.kt           # RecyclerView adapter
├── mood/                        # Emotion detection feature
│   ├── MoodCheckFragment.kt     # UI
│   └── EmotionService.kt        # API interface
├── usage/                       # Usage tracking feature
│   ├── UsageStatsFragment.kt   # UI
│   ├── UsageStatsHelper.kt      # Business logic
│   └── TopAppsAdapter.kt       # RecyclerView adapter
└── notification/                # Notifications
    └── NotificationHelper.kt    # Notification logic
```

## Dependencies

All dependencies are defined in `app/build.gradle`:
- AndroidX libraries (Core, AppCompat, Material Design)
- Navigation Component
- Retrofit for API calls
- Kotlin Coroutines
- RecyclerView for lists

## Build Process

1. **Local Build**: Use Android Studio or `./gradlew assembleDebug`
2. **CI/CD Build**: Push to GitHub, workflow runs automatically
3. **Output**: APK at `app/build/outputs/apk/debug/app-debug.apk`

## Next Steps

1. Edit `ApiConfig.kt` with your API keys
2. Sync Gradle in Android Studio
3. Build and run on device/emulator
4. Grant Usage Stats permission when prompted
5. Test all three tabs (Usage, Chat, Mood)
