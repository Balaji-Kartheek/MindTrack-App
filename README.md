# MindApp - Digital Wellbeing Android App

A comprehensive Android application for tracking app usage, AI-powered digital wellbeing assistance, and emotion detection. Built with Kotlin and Material Design.

## Features

### 1. Usage Statistics Tracking
- Track daily app usage time
- Categorize apps (Social Media, Productivity, Entertainment, Others)
- Display total screen time
- Show top 5 most used apps
- Send notifications when social media usage exceeds 3 hours

### 2. AI Chatbot (Powered by Google Gemini)
- Chat with AI about digital wellbeing
- Get personalized tips on reducing screen time
- Ask questions about usage patterns
- View conversation history

### 3. Emotion Detection (Powered by Hugging Face)
- Input how you're feeling via text
- Detect emotions: happy, sad, angry, anxious, stressed, neutral
- View emotion analysis with confidence scores
- Correlate emotions with app usage patterns
- Get insights based on emotional state and usage data

## Project Structure

```
MindApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/mindapp/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ApiConfig.kt
│   │   │   │   ├── usage/
│   │   │   │   │   ├── UsageStatsFragment.kt
│   │   │   │   │   ├── UsageStatsHelper.kt
│   │   │   │   │   └── TopAppsAdapter.kt
│   │   │   │   ├── chatbot/
│   │   │   │   │   ├── ChatbotFragment.kt
│   │   │   │   │   ├── GeminiService.kt
│   │   │   │   │   └── ChatAdapter.kt
│   │   │   │   ├── mood/
│   │   │   │   │   ├── MoodCheckFragment.kt
│   │   │   │   │   └── EmotionService.kt
│   │   │   │   └── notification/
│   │   │   │       └── NotificationHelper.kt
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       ├── values/
│   │   │       └── menu/
│   │   └── build.gradle
├── .github/workflows/
│   └── build.yml
├── build.gradle
├── settings.gradle
└── README.md
```

## Setup Instructions

### Prerequisites
- Android Studio (for local development) or GitHub account (for CI/CD)
- Android device or emulator running Android 8.0 (API 26) or higher
- Internet connection for API calls

### Step 1: Get API Keys

#### Google Gemini API Key
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

#### Hugging Face API Key
1. Visit [Hugging Face](https://huggingface.co/settings/tokens)
2. Sign in or create an account
3. Click "New token"
4. Give it a name (e.g., "MindApp")
5. Select "Read" permission
6. Copy the generated token

### Step 2: Configure API Keys

1. Open `app/src/main/java/com/mindapp/ApiConfig.kt`
2. Replace `YOUR_GEMINI_API_KEY_HERE` with your Gemini API key
3. Replace `YOUR_HUGGING_FACE_API_KEY_HERE` with your Hugging Face API key

```kotlin
object ApiConfig {
    const val GEMINI_API_KEY = "your-actual-gemini-api-key-here"
    const val HUGGING_FACE_API_KEY = "your-actual-hugging-face-token-here"
    // ...
}
```

### Step 3: Grant Permissions

When you first launch the app:
1. The app will request Usage Stats permission
2. Tap "Open Settings"
3. Find "MindApp" in the list
4. Toggle the switch to grant permission
5. Return to the app

### Step 4: Build the App

#### Option A: Build Locally with Android Studio
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect your Android device or start an emulator
4. Click "Run" or press Shift+F10

#### Option B: Build with GitHub Actions
1. Push your code to a GitHub repository
2. GitHub Actions will automatically build the APK on push to `main` branch
3. Download the APK from the Actions tab → Artifacts

## Usage

### Usage Statistics Tab
- View your total screen time for today
- See social media usage
- Check top 5 most used apps
- Tap "Refresh" to update statistics
- Receive alerts when social media usage exceeds 3 hours

### Chat Tab
- Type your questions about digital wellbeing
- Get AI-powered responses and tips
- Ask about your usage patterns
- The chatbot uses your current usage stats for context

### Mood Check Tab
- Enter how you're feeling (e.g., "I'm feeling stressed about exams")
- Tap "Analyze Emotion"
- View detected emotions with confidence scores
- See correlation between your mood and app usage
- Get personalized insights

## Technical Details

### Tech Stack
- **Language**: Kotlin
- **UI**: XML layouts with Material Design
- **Architecture**: Fragment-based with Bottom Navigation
- **API Calls**: Retrofit 2.9.0
- **Coroutines**: Kotlin Coroutines for async operations
- **APIs Used**:
  - Android UsageStatsManager for app tracking
  - Google Gemini API for chatbot
  - Hugging Face API for emotion detection

### Permissions Required
- `PACKAGE_USAGE_STATS` - To track app usage
- `INTERNET` - For API calls
- `ACCESS_NETWORK_STATE` - To check network connectivity
- `POST_NOTIFICATIONS` - For alerts (Android 13+)

### Target SDK
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34

## Social Media Apps Tracked
- Instagram
- Facebook
- Twitter/X
- TikTok
- Snapchat
- WhatsApp
- Telegram
- Reddit
- YouTube

## Troubleshooting

### API Key Errors
- Ensure API keys are correctly set in `ApiConfig.kt`
- Check that keys don't have extra spaces or quotes
- Verify API keys are active and have proper permissions

### Usage Stats Not Working
- Grant Usage Stats permission in Settings
- Restart the app after granting permission
- Ensure the device is running Android 8.0 or higher

### Build Errors
- Ensure you have JDK 17 installed
- Run `./gradlew clean` before building
- Check that all dependencies are properly synced

### Network Errors
- Check internet connection
- Verify API endpoints are accessible
- Check API rate limits (especially for Hugging Face)

## GitHub Actions CI/CD

The project includes a GitHub Actions workflow that automatically builds the APK on push to the main branch.

**Workflow File**: `.github/workflows/build.yml`

**To use**:
1. Push code to GitHub
2. Workflow runs automatically
3. Download APK from Actions → Artifacts

## Notes for Presentation

1. **Architecture**: Explain the fragment-based architecture with bottom navigation
2. **Usage Tracking**: Describe how UsageStatsManager API works
3. **AI Integration**: Explain Gemini API integration and how it provides context-aware responses
4. **Emotion Detection**: Describe Hugging Face emotion detection model and correlation logic
5. **Permissions**: Explain why Usage Stats permission is needed and how it's requested
6. **Notifications**: Show how alerts are triggered when usage exceeds thresholds
7. **Material Design**: Highlight the use of Material Components for modern UI

## License

This project is created for educational purposes as part of a college project.

## Author

Created for college project presentation.

---

**Important**: Remember to never commit API keys to public repositories. Consider using environment variables or a secrets management system for production applications.
