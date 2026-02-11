# 🔑 API Keys Setup Guide

## Quick Start (Works Immediately!)

The app includes **demo API keys** that work out of the box for testing. Just build and run:

```bash
.\gradlew clean assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

✅ No configuration needed for demo!

---

## For Your Own API Keys (Recommended)

### Step 1: Get Your API Keys

**Gemini API Key:**
1. Go to: https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Copy the key (starts with `AIza...`)

**Hugging Face API Key:**
1. Go to: https://huggingface.co/settings/tokens
2. Click "New token"
3. Name: "MindApp"
4. Type: "Read"
5. Copy the key (starts with `hf_...`)

---

### Step 2: Create local.properties

Create or edit `local.properties` in the project root:

```properties
# Android SDK location (should already exist)
sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\sdk

# Add your API keys below
GEMINI_API_KEY=YOUR_GEMINI_KEY_HERE
HUGGING_FACE_API_KEY=YOUR_HF_KEY_HERE
```

**Example format:**
```properties
GEMINI_API_KEY=AIzaSy... (your full key here)
HUGGING_FACE_API_KEY=hf_... (your full key here)
```

---

### Step 3: Rebuild the App

```bash
# Clean previous build
.\gradlew clean

# Build with your keys
.\gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## How It Works

1. **Build Time:** 
   - `build.gradle` reads keys from `local.properties`
   - Keys are injected into `BuildConfig` during compilation
   - Your keys are compiled into the APK

2. **Runtime:**
   - App reads from `BuildConfig.GEMINI_API_KEY`
   - If placeholder found → Uses demo keys
   - If real keys found → Uses your keys

3. **Demo Keys:**
   - Included for immediate testing
   - Work for basic functionality
   - Get your own for production use

---

## Troubleshooting

### ❌ "API not configured" error

**Cause:** Keys are placeholders or missing

**Fix:**
```bash
# 1. Check local.properties exists
ls local.properties

# 2. Check it has your keys (not placeholders)
cat local.properties

# 3. Rebuild (important!)
.\gradlew clean assembleDebug

# 4. Reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### ❌ "401 Unauthorized" error

**Cause:** API key is invalid or expired

**Fix:** Get new keys (see Step 1 above)

### ❌ Still not working?

**Option 1: Use Demo Keys**
Just build without local.properties - demo keys work!

**Option 2: Check local.properties format**
```properties
# ✅ Correct
GEMINI_API_KEY=AIzaSy...(your_full_key)

# ❌ Wrong (has quotes)
GEMINI_API_KEY="AIzaSy..."

# ❌ Wrong (has spaces)
GEMINI_API_KEY = AIzaSy...
```

---

## Testing Your Setup

Run the test script:

```bash
python test_api_keys.py
```

Expected output:
```
✅ Gemini API: Working
✅ Hugging Face API: Working
🎉 All APIs are working!
```

---

## For GitHub Actions / CI

Set as repository secrets:
1. Go to: Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add `GEMINI_API_KEY` with your key
4. Add `HUGGING_FACE_API_KEY` with your key

The `build.gradle` will automatically use environment variables in CI.

---

## Security Notes

✅ **Safe:**
- `local.properties` is gitignored
- Your keys won't be committed
- Demo keys are public (for testing only)

⚠️ **Warning:**
- Never commit your personal API keys
- Never share your keys publicly
- Get your own keys for production

---

## Summary

**For Testing:**
- Just build and run (demo keys included)
- ✅ Works immediately

**For Production:**
- Add your keys to `local.properties`
- Rebuild the app
- ✅ Uses your keys

**Need Help?**
- Check `API_TESTING_GUIDE.md`
- Check `UI_IMPROVEMENTS.md`
- Run `python test_api_keys.py`
