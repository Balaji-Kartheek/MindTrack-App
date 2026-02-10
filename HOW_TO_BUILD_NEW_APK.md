# How to Build a NEW APK with Your API Keys

## ⚠️ CRITICAL: You Must Build a NEW APK After Setting Secrets!

Your GitHub Secrets are set up correctly, but your current APK was built **before** you added the secrets. API keys are "baked into" the APK during build time, so you need a fresh build.

## Quick Steps (5 Minutes)

### Step 1: Trigger a New Build on GitHub

1. **Open your repository**: https://github.com/Balaji-Kartheek/MindTrack-App

2. **Click the "Actions" tab** (at the top)

3. **Click "Build APK"** in the left sidebar (under "All workflows")

4. **Click the "Run workflow" button** (green button on the right side)

5. **Select branch**: `main` (should be pre-selected)

6. **Click "Run workflow"** (the green button in the popup)

7. **Wait 3-5 minutes** for the build to complete
   - You'll see a yellow dot 🟡 (building)
   - Then a green checkmark ✅ (success) or red X ❌ (failed)

### Step 2: Download the NEW APK

1. **Click on the workflow run** that just completed (the one at the top of the list)

2. **Scroll down to the "Artifacts" section** (at the bottom of the page)

3. **Click "app-debug"** to download the ZIP file

4. **Extract the ZIP file** on your computer
   - You'll get a file named `app-debug.apk`

### Step 3: Install the NEW APK on Your Phone

**Option A: Via USB Cable**
1. Connect your phone to your computer
2. Copy `app-debug.apk` to your phone's Downloads folder
3. On your phone, open the Downloads folder
4. Tap on `app-debug.apk`
5. If prompted, allow installation from this source
6. If the old app is installed, you'll be asked to update it - tap "Install"

**Option B: Via Cloud/Email**
1. Upload `app-debug.apk` to Google Drive, OneDrive, or email it to yourself
2. Open the link on your phone
3. Download the APK
4. Tap to install
5. If prompted, allow installation from this source

### Step 4: Verify It Worked

1. **Open the MindApp** on your phone

2. **Go to the "Chat" tab**
   - ✅ **Success**: No error message, you can type and send messages
   - ❌ **Still broken**: Shows "⚠️ API key not configured!" - you didn't install the NEW APK

3. **Go to the "Mood" tab**
   - ✅ **Success**: No error message, you can analyze emotions
   - ❌ **Still broken**: Shows "⚠️ API key not configured!" - you didn't install the NEW APK

## Screenshots Guide

### Step 1: Actions Tab
```
[Repository] → [Actions Tab] → [Build APK] → [Run workflow ▼] → [Run workflow]
```

### Step 2: Download Artifact
```
[Completed Run] → Scroll down → [Artifacts] → [app-debug] (Click to download)
```

### Step 3: Install
```
[Phone] → [Downloads] → [app-debug.apk] → [Install] → [Open]
```

## Troubleshooting

### ❌ "Build failed" with red X

**Check the build logs:**
1. Click on the failed workflow run
2. Click "Build debug APK" step
3. Read the error message
4. Common causes:
   - Syntax errors in code (already fixed in latest commit)
   - Missing files (shouldn't happen)

### ❌ Still shows "Please configure API key" after installing

This means you didn't install the NEW APK. Check:
1. Did you download the APK **after** the latest workflow run?
2. Did you actually tap "Install" on your phone?
3. Did the installation succeed?
4. Try uninstalling the old app completely, then install the new one

### ❌ "No artifacts" or can't find app-debug

The build might have failed. Check:
1. Does the workflow run have a green checkmark ✅?
2. If it has a red X ❌, click it and check the error
3. If there's no checkmark yet, wait - it's still building

### ❌ Can't install APK on phone

1. **Go to phone Settings** → **Security** → **Install unknown apps**
2. **Find the app you're using** to install (Chrome, Files, Downloads, etc.)
3. **Toggle "Allow from this source"** to ON
4. Try installing again

## Verify GitHub Secrets Are Set

Before building, double-check your secrets:

1. Go to: https://github.com/Balaji-Kartheek/MindTrack-App/settings/secrets/actions

2. You should see:
   - ✅ `GEMINI_API_KEY` (with a green checkmark)
   - ✅ `HUGGING_FACE_API_KEY` (with a green checkmark)

3. If they're missing, click "New repository secret" and add them:
   - **Name**: `GEMINI_API_KEY`
   - **Secret**: Your Gemini API key from https://makersuite.google.com/app/apikey
   
   - **Name**: `HUGGING_FACE_API_KEY`
   - **Secret**: Your Hugging Face token from https://huggingface.co/settings/tokens

## What Happens During Build?

1. GitHub Actions reads your secrets (`GEMINI_API_KEY`, `HUGGING_FACE_API_KEY`)
2. It sets them as environment variables during build
3. Gradle reads the environment variables
4. It generates `BuildConfig.java` with your actual API keys
5. The app code reads from `BuildConfig.GEMINI_API_KEY` at runtime
6. Your APK now has working API keys!

## Expected Timeline

- **Setting secrets**: 2 minutes
- **Triggering build**: 30 seconds
- **Build time**: 3-5 minutes
- **Download**: 1 minute
- **Transfer to phone**: 1-2 minutes
- **Install**: 30 seconds

**Total**: ~10 minutes from start to finish

## Still Having Issues?

1. **Share a screenshot** of:
   - Your GitHub Secrets page (Settings → Secrets → Actions)
   - The Actions page showing the workflow run
   - The error message on your phone (if any)

2. **Confirm**:
   - Did you trigger a NEW build after setting secrets?
   - Did you download the artifact from that NEW build?
   - Did you actually install the APK on your phone?

---

**Remember**: You need to build a NEW APK every time you change:
- API keys in GitHub Secrets
- Code in the repository
- Dependencies or configuration

The old APK won't magically update - you need to rebuild and reinstall!
