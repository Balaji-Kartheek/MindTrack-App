# 🔧 Usage Stats Troubleshooting Guide

## Problem: "Not capturing app usage / Not updating"

### Quick Fixes (Try These First)

#### 1. **Grant Usage Access Permission** ⚙️

**Steps:**
1. Open **Settings** on your Android device
2. Go to **Apps** → **Special Access** (or **Advanced**)
3. Tap **Usage Access**
4. Find **MindApp**
5. **Enable** the toggle
6. **Restart the app**

**Alternative path:**
Settings → Security → Usage Access → MindApp → Enable

#### 2. **Wait for Data Collection** ⏱️

Android needs time to collect usage data:
- **Initial**: 5-10 minutes of app usage
- **Accurate**: 1-2 hours of usage
- **Real-time**: May have 5-15 minute delay

**Solution:**
1. Use your phone normally (Instagram, WhatsApp, Chrome, etc.)
2. Wait 10-15 minutes
3. Open MindApp → Usage tab
4. Tap **Refresh** button
5. Check "Last updated" time

#### 3. **Restart Your Device** 🔄

Android's usage stats service sometimes needs a restart:
1. Reboot your phone
2. Use apps for 10 minutes
3. Open MindApp
4. Tap Refresh

---

## Understanding Usage Stats

### How It Works:

```
You use apps (Instagram, WhatsApp, Chrome)
         ↓
Android's UsageStatsManager collects data
         ↓
Data updates every 5-15 minutes
         ↓
MindApp reads and displays the data
```

### Why It's Not Real-Time:

- **Android limitation**: Data updates in intervals (5-15 min)
- **Battery optimization**: Real-time would drain battery
- **Privacy**: Android aggregates data for security

---

## Checking If It's Working

### Test Steps:

1. **Check Permission**:
   ```
   Settings → Apps → Special Access → Usage Access
   MindApp should show "Allowed"
   ```

2. **Use Apps**:
   - Open Instagram, scroll for 5 minutes
   - Open WhatsApp, chat for 5 minutes
   - Open Chrome, browse for 5 minutes

3. **Check Logcat** (for developers):
   ```bash
   adb logcat | grep UsageStatsHelper
   ```
   
   Should see:
   ```
   Total stats entries: 50+
   App: com.instagram.android, Time: 5m
   Found with usage: 10+ apps
   ```

4. **Check in MindApp**:
   - Open Usage tab
   - Tap Refresh
   - Should show:
     - Total screen time > 0
     - List of apps
     - "Last updated" time

---

## Common Issues

### ❌ "Permission Required" Error

**Cause**: Usage Access not granted

**Fix**:
1. Go to Settings → Usage Access
2. Enable for MindApp
3. **Restart MindApp** (force stop + reopen)

### ❌ "0m" Shows But You've Used Apps

**Causes**:
1. Android hasn't updated data yet (wait 10 min)
2. Only system apps used (not tracked)
3. Device-specific limitations

**Fix**:
```
1. Use popular apps (Instagram, WhatsApp, Chrome)
2. Wait 15 minutes
3. Tap Refresh
4. Check "Last updated" time
```

### ❌ Data Shows But Doesn't Update

**Cause**: Need to refresh manually

**Fix**:
- Tap **Refresh** button after using apps
- Android updates every 5-15 minutes
- Not instant/real-time

### ❌ Only Shows Few Apps

**Cause**: System apps filtered out

**Tracked apps:**
- ✅ All user-installed apps
- ✅ Social media (Instagram, Facebook, etc.)
- ✅ Browsers (Chrome, etc.)
- ✅ Messaging (WhatsApp, Telegram, etc.)
- ✅ Updated system apps (YouTube, etc.)
- ❌ Pure system apps (Settings, Phone, etc.)

---

## Device-Specific Issues

### Samsung Devices:
- Go to: Settings → Battery → App Power Management
- Disable power saving for MindApp

### Xiaomi/MIUI Devices:
- Go to: Settings → Apps → Manage Apps → MindApp
- Set "Autostart": Enabled
- Set "Battery saver": No restrictions

### Huawei Devices:
- Go to: Settings → Battery → App Launch → MindApp
- Set to "Manual"
- Enable all options

---

## Advanced Debugging

### Check Android Version:
```kotlin
// MindApp requires Android 8.0+ (API 26+)
// Check: Settings → About Phone → Android version
```

### Check Logcat Output:
```bash
adb logcat -s UsageStatsHelper:D

# Should see:
# Querying from: 2026-02-10 00:00:00 to 2026-02-10 15:30:00
# Total stats entries: 150
# App: com.instagram.android, Time: 1h 23m
# Processed: 150 apps, Found with usage: 45 apps, Final list: 25 apps
```

### Test Permission Programmatically:
```kotlin
val hasPermission = UsageStatsHelper.hasUsageStatsPermission(context)
Log.d("TEST", "Has permission: $hasPermission")
```

---

## What's Been Fixed

### Recent Improvements:

1. ✅ **Combined data sources**: Daily + Recent (last hour)
2. ✅ **Better filtering**: Tracks more apps (Chrome, YouTube, etc.)
3. ✅ **Real-time updates**: Includes last hour data
4. ✅ **Auto permission prompt**: Opens settings automatically
5. ✅ **Last updated time**: Shows when data was refreshed
6. ✅ **Better error messages**: Clear guidance
7. ✅ **More logging**: For debugging

---

## Expected Behavior

### ✅ Working Correctly:

```
Usage Stats Tab:
─────────────────────────────
Total Screen Time: 2h 15m
Last updated: 03:45:32 PM

Social Media: 1h 30m

Top Apps:
1. Instagram      1h 23m  [Social]
2. WhatsApp       45m     [Social]
3. Chrome         32m     [Other]
4. YouTube        28m     [Entertainment]
5. Reddit         15m     [Social]
```

### Updates:
- After using apps for 10-15 minutes
- Tap Refresh button
- Data updates (Last updated time changes)

---

## Still Not Working?

### Last Resort Checklist:

1. **Check Android version**: Must be 8.0+ (API 26+)
2. **Reinstall app**: Clean install
3. **Clear app data**: Settings → Apps → MindApp → Clear Data
4. **Check phone storage**: Must have free space
5. **Disable battery optimization**: For MindApp
6. **Check for OS updates**: Update Android if available

### Report Issue:
If still not working, provide:
- Device model
- Android version
- Logcat output (adb logcat)
- Screenshot of Usage Access settings

---

## Summary

**Common Causes:**
1. 🔐 Permission not granted (90% of cases)
2. ⏱️ Not enough time passed (Android needs 10+ min)
3. 📱 Device power saving blocking data
4. 🔄 Need to tap Refresh button

**Quick Solution:**
```
1. Grant Usage Access permission
2. Use apps for 15 minutes
3. Open MindApp → Usage tab
4. Tap Refresh
5. ✅ Should work!
```

**Remember**: Usage stats have 5-15 minute delay. Not instant!
