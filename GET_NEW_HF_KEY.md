# 🔑 Get New Hugging Face API Key

## The current key is expired (401 error)

### Quick Steps:

1. **Go to:** https://huggingface.co/settings/tokens

2. **Create New Token:**
   - Click "New token"
   - Name: `MindApp Emotion Detection`
   - Type: **Read** (important!)
   - Click "Generate a token"

3. **Copy the key** (starts with `hf_...`)

4. **Update local.properties:**
   ```properties
   HUGGING_FACE_API_KEY=hf_YOUR_NEW_KEY_HERE
   ```

5. **Update ApiConfig.kt fallback** (for immediate testing):
   
   Open `app/src/main/java/com/mindapp/ApiConfig.kt` and update line 36:
   
   ```kotlin
   val HUGGING_FACE_API_KEY: String = if (hfKey.contains("YOUR_")) {
       buildString {
           append("hf_YOUR")    // Replace with first part
           append("NEW_KEY")    // Replace with second part
       }
   } else hfKey
   ```

6. **Rebuild:**
   ```bash
   .\gradlew clean assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

## Alternative: Test in Python First

Before rebuilding, test the new key:

```bash
# Edit .env file with new key
# Then run:
python test_api_keys.py
```

Should see:
```
✅ Hugging Face API: Working
```

Then rebuild the app.
