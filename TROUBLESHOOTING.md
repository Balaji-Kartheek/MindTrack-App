# Troubleshooting Guide - MindApp

## Issue 1: "Please Configure API Key" Errors

### Symptoms
- Chatbot shows: "Please configure Gemini API key"
- Mood Analysis shows: "Please configure Hugging Face API key"

### Root Cause
The APK was built **without** your API keys. This happens when:
1. GitHub Secrets are not set up, OR
2. You're using an old APK built before setting up secrets

### Solution

#### Step 1: Verify GitHub Secrets Are Set
1. Go to your repository: https://github.com/Balaji-Kartheek/MindTrack-App
2. Click **Settings** → **Secrets and variables** → **Actions**
3. You should see:
   - ✅ `GEMINI_API_KEY`
   - ✅ `HUGGING_FACE_API_KEY`

If they're NOT there:
- Follow [GITHUB_SECRETS_SETUP.md](./GITHUB_SECRETS_SETUP.md) to add them

#### Step 2: Build a NEW APK After Setting Secrets
**CRITICAL:** You must build a **new** APK after adding secrets!

1. Go to **Actions** tab in your repo
2. Click **"Build APK"** workflow
3. Click **"Run workflow"** (green button on the right)
4. Select branch: `main`
5. Click **"Run workflow"**
6. Wait 3-5 minutes
7. Download the **new** APK from Artifacts
8. **Uninstall the old app** from your phone
9. Install the **new** APK

#### Step 3: Verify the Build Logs
1. Go to Actions → Click on the latest workflow run
2. Expand **"Build debug APK"** step
3. Look for: `BUILD SUCCESSFUL`
4. If it fails, check the error messages

### Quick Test
After installing the new APK:
1. Open the Chat tab
2. The welcome message should NOT say "Please configure API key"
3. If it still does, the APK was built without secrets

---

## Issue 2: Usage Statistics Not Updating / WhatsApp Not Showing

### Symptoms
- Usage stats show 0 hours 0 minutes
- Apps you've used (like WhatsApp) don't appear
- Stats don't update when you pull to refresh

### Understanding Android's UsageStats API

**IMPORTANT:** Android's UsageStats API has limitations:

1. **Delayed Updates**: 
   - Android aggregates usage data every 1-2 hours
   - Immediately after using an app, it might not show up yet
   - This is an Android system limitation, not an app bug

2. **Minimum Usage Time**:
   - Very brief app usage (< 30 seconds) might not be recorded
   - You need to actively use an app for a noticeable duration

3. **Permission Timing**:
   - If you just granted permission, Android might not have data yet
   - Wait a few hours after first granting permission

### Solutions

#### Solution A: Wait and Test Properly
1. **Use apps normally for 1-2 hours**:
   - Use WhatsApp for at least 5-10 minutes
   - Use other apps (YouTube, Chrome, etc.)
   
2. **Wait 1-2 hours** for Android to aggregate the data

3. **Then check MindApp**:
   - Open the Usage tab
   - Pull down to refresh
   - Stats should now appear

#### Solution B: Check Permission Was Granted Correctly
1. Open **Android Settings**
2. Go to **Apps** → **Special app access** → **Usage access**
3. Find **MindApp**
4. Ensure the toggle is **ON**
5. If it's OFF, turn it ON
6. Return to MindApp and try again

#### Solution C: Test with Known High-Usage Apps
Instead of immediately checking for WhatsApp:
1. **Use YouTube for 10 minutes** (watch a video)
2. **Use Chrome for 5 minutes** (browse websites)
3. **Wait 1-2 hours**
4. Open MindApp and check stats

#### Solution D: Enable Debug Logging
I'll add a debug mode to help diagnose the issue.

### Why WhatsApp Might Not Show

Even though WhatsApp is in our tracking list, it might not appear because:

1. **Background Usage**: WhatsApp runs in background but Android only tracks foreground time
2. **System App Status**: On some devices, WhatsApp is pre-installed and flagged differently
3. **OEM Modifications**: Some phone manufacturers (Xiaomi, Oppo, etc.) have modified Android's UsageStats behavior

### Testing Method

**Day 1:**
1. Install the app
2. Grant Usage Stats permission
3. Use your phone normally
4. Don't check the app yet

**Day 2:**
1. Open MindApp
2. Go to Usage tab
3. You should now see yesterday's data

This tests whether it's a timing issue or a code issue.

---

## Issue 3: Text Input Not Visible (White Text)

### Status
✅ **FIXED** in latest commit

If you still see white text:
- You're using an old APK
- Download and install the latest APK from GitHub Actions

---

## Advanced Debugging

### Check What Apps Are Installed
To see what Android considers as "apps" on your device:

1. Install the latest APK
2. Open MindApp
3. Grant permissions
4. Check the Usage tab

If you see:
- **Some apps but not all**: Filtering is working but might be too strict
- **No apps at all**: Permission issue or Android delay
- **All apps including system apps**: Filtering is too loose

### Enable ADB Logging (For Developers)

If you have ADB installed:

```bash
# Clear logs
adb logcat -c

# Run the app and go to Usage tab
# Then capture logs
adb logcat | grep -i "MindApp\|UsageStats\|mindapp"
```

Look for errors or exceptions related to usage statistics.

---

## Common Mistakes

### ❌ Mistake 1: Not Rebuilding After Setting Secrets
- **Wrong**: Add secrets → Install old APK → Test
- **Right**: Add secrets → Trigger new build → Download new APK → Install → Test

### ❌ Mistake 2: Expecting Instant Usage Data
- **Wrong**: Use app → Immediately check stats → Nothing shows
- **Right**: Use app → Wait 1-2 hours → Check stats → Data appears

### ❌ Mistake 3: Checking Only Brief Usage
- **Wrong**: Open WhatsApp for 10 seconds → Check stats
- **Right**: Use WhatsApp actively for 5-10 minutes → Wait → Check stats

### ❌ Mistake 4: Not Refreshing Stats
- **Wrong**: Open Usage tab once and expect real-time updates
- **Right**: Pull down to refresh to fetch latest data

---

## Still Having Issues?

If none of the above solutions work:

1. **For API Keys**: Share a screenshot of your GitHub Secrets page (blur the values)
2. **For Usage Stats**: 
   - Tell me what Android version you're using
   - Tell me your phone manufacturer (Samsung, Xiaomi, etc.)
   - How long you waited after using apps
   - Whether you see ANY apps or ZERO apps

3. **Create a GitHub Issue** with:
   - Your phone model and Android version
   - Screenshots of the errors
   - What you've already tried

---

## Expected Behavior (When Everything Works)

### Chatbot Tab
- Welcome message does NOT mention "configure API key"
- You can type messages and get AI responses
- Responses are relevant to your usage data

### Mood Tab
- No "configure API key" message
- You can enter text and click "Analyze Mood"
- Emotions are detected and displayed
- Correlation with usage shows meaningful insights

### Usage Tab
- Shows total screen time (hours and minutes)
- Shows top 5 apps you've used
- Updates when you pull to refresh
- Data is from "today" (since midnight)

---

## Quick Checklist

Before reporting issues, verify:

**API Keys:**
- [ ] GitHub Secrets are added (GEMINI_API_KEY and HUGGING_FACE_API_KEY)
- [ ] You triggered a new build AFTER adding secrets
- [ ] You downloaded the NEW APK (not an old one)
- [ ] You uninstalled old app before installing new one

**Usage Stats:**
- [ ] Permission is granted (Settings → Apps → Special → Usage access → MindApp → ON)
- [ ] You've used apps for more than just a few seconds
- [ ] You've waited at least 1-2 hours after using apps
- [ ] You tried pulling to refresh in the Usage tab
- [ ] You're checking "today's" usage (data since midnight)

---

**Last Updated:** February 10, 2026
