# Quick Start - Testing Your API Keys

## ✅ What Was Done

1. **Fixed `build.gradle`** - Now reads from `local.properties`
2. **Created `local.properties`** - Your API keys are there
3. **Added Test Tools** - Scripts to verify APIs work
4. **Pushed to GitHub** - All changes committed

---

## 🧪 Test API Keys in Terminal

### Option 1: Python Test (Easiest)

```bash
python test_api_keys.py
```

### Option 2: PowerShell (Windows)

Copy-paste this into PowerShell:

**Test Gemini:**
```powershell
# Replace YOUR_GEMINI_KEY with your actual key
$body = '{"contents":[{"parts":[{"text":"Hello"}]}]}'
Invoke-RestMethod -Uri "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=YOUR_GEMINI_KEY" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Test Hugging Face:**
```powershell
# Replace YOUR_HF_KEY with your actual key
$headers = @{"Authorization"="Bearer YOUR_HF_KEY"}
$body = '{"inputs":"I feel happy"}'
Invoke-RestMethod -Uri "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base" -Method Post -Headers $headers -Body $body -ContentType "application/json" | ConvertTo-Json
```

---

## ⚠️ If You Get Errors (404/401)

Your API keys might be **invalid or expired**. Get new ones:

### Get New Gemini Key:
1. Go to: **https://makersuite.google.com/app/apikey**
2. Click "Create API Key"
3. Copy the key

### Get New Hugging Face Key:
1. Go to: **https://huggingface.co/settings/tokens**
2. Click "New token"
3. Select "Read" permission
4. Copy the key

### Update Keys:

Edit `local.properties`:
```properties
GEMINI_API_KEY=your_new_gemini_key_here
HUGGING_FACE_API_KEY=your_new_hf_key_here
```

---

## 🏗️ Build & Test the App

Once APIs work in terminal:

```bash
# Clean build
.\gradlew clean

# Build APK
.\gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then test in the app:
- **Chatbot tab** - Send a message
- **Mood tab** - Analyze emotion

---

## 📁 Files Created

- `ApiTester.kt` - API testing utility
- `ApiTestFragment.kt` - In-app test screen (optional)
- `local.properties` - Your API keys (gitignored)
- `test_api_keys.py` - Python test script
- `test_api_keys.ps1` - PowerShell test script
- `API_TESTING_GUIDE.md` - Full testing guide
- `TERMINAL_API_TEST.md` - Terminal test instructions

---

## 🎯 Next Steps

1. **Test APIs in terminal** (see commands above)
2. **If they work** → Rebuild app → Test in app
3. **If they fail** → Get new API keys → Update local.properties → Test again

---

## 💡 Key Learning

- **.env doesn't work in Android** → Use `local.properties`
- **Keys are compiled at build time** → Need to rebuild after changing keys
- **Test in terminal first** → Faster than rebuilding the app
- **local.properties is gitignored** → Safe for local development

---

## Need Help?

Check these files:
- `API_TESTING_GUIDE.md` - Comprehensive guide
- `TERMINAL_API_TEST.md` - Terminal testing methods
- `API_KEYS_SETUP.md` - Original API keys setup

The API keys in your `.env` are now in `local.properties` and the app will use them after rebuilding!
