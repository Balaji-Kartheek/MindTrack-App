# Quick Start Guide

## Step-by-Step Setup

### 1. Get API Keys (5 minutes)

#### Gemini API Key
1. Go to https://makersuite.google.com/app/apikey
2. Sign in with Google
3. Click "Create API Key"
4. Copy the key

#### Hugging Face Token
1. Go to https://huggingface.co/settings/tokens
2. Sign in or create account
3. Click "New token"
4. Name it "MindApp"
5. Select "Read" permission
6. Copy the token

### 2. Configure API Keys (1 minute)

1. Open `app/src/main/java/com/mindapp/ApiConfig.kt`
2. Find these lines:
   ```kotlin
   const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
   const val HUGGING_FACE_API_KEY = "YOUR_HUGGING_FACE_API_KEY_HERE"
   ```
3. Replace with your actual keys:
   ```kotlin
   const val GEMINI_API_KEY = "AIzaSy..." // Your actual key
   const val HUGGING_FACE_API_KEY = "hf_..." // Your actual token
   ```

### 3. Build the App

#### Option A: Android Studio (Recommended for testing)
1. Open Android Studio
2. File → Open → Select this project folder
3. Wait for Gradle sync
4. Click Run (green play button)
5. Select device/emulator

#### Option B: GitHub Actions (For APK)
1. Push code to GitHub repository
2. Go to Actions tab
3. Wait for workflow to complete
4. Download APK from Artifacts

### 4. First Launch

1. Install APK on device (or run from Android Studio)
2. App will request Usage Stats permission
3. Tap "Open Settings"
4. Find "MindApp" in the list
5. Toggle the switch ON
6. Return to app

### 5. Test Features

#### Usage Tab
- Tap "Refresh" button
- Should show screen time and top apps
- If social media > 3 hours, you'll get a notification

#### Chat Tab
- Type: "How can I reduce my screen time?"
- Wait for AI response
- Try asking about your usage patterns

#### Mood Tab
- Type: "I'm feeling stressed about my exams"
- Tap "Analyze Emotion"
- View emotion results and correlation with usage

## Troubleshooting

### "Please configure your API key" error
- Check `ApiConfig.kt` - keys must be set
- No quotes around keys, just the key itself
- Restart app after changing keys

### Usage stats not showing
- Grant Usage Stats permission in Settings
- Restart app
- Tap Refresh button

### API errors
- Check internet connection
- Verify API keys are correct
- Check API quotas/limits

### Build errors
- Ensure JDK 17 is installed
- Run `./gradlew clean` then rebuild
- Check Android Studio SDK settings

## File Locations Reference

- **API Keys**: `app/src/main/java/com/mindapp/ApiConfig.kt`
- **Main Code**: `app/src/main/java/com/mindapp/`
- **Layouts**: `app/src/main/res/layout/`
- **Build Config**: `app/build.gradle`
- **Manifest**: `app/src/main/AndroidManifest.xml`

## Presentation Tips

1. **Demo Flow**:
   - Show Usage tab with real data
   - Demonstrate chatbot asking about wellbeing
   - Show emotion detection with correlation

2. **Key Points to Highlight**:
   - UsageStatsManager API for tracking
   - Gemini API for AI responses
   - Hugging Face for emotion detection
   - Material Design UI
   - GitHub Actions CI/CD

3. **Code Highlights**:
   - `UsageStatsHelper.kt` - Core tracking logic
   - `ChatbotFragment.kt` - AI integration
   - `MoodCheckFragment.kt` - Emotion detection
   - `ApiConfig.kt` - Configuration management

## Common Questions

**Q: Why Usage Stats permission?**
A: Android requires special permission to track app usage for privacy reasons.

**Q: Can I use this without API keys?**
A: Usage tracking works, but Chat and Mood features need API keys.

**Q: How do I get the APK?**
A: Build locally or use GitHub Actions - APK will be in `app/build/outputs/apk/debug/`

**Q: What Android version is needed?**
A: Android 8.0 (API 26) or higher.
