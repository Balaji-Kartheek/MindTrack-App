# How to Configure API Keys (Chat & Mood Features)

The **Chat** tab (Gemini) and **Mood** tab (Hugging Face) show "API is not configured" until you add your keys in `ApiConfig.kt`. Follow the steps below.

---

## Step 1: Get Your API Keys

### Gemini API Key (for Chat tab)

1. Open: **https://makersuite.google.com/app/apikey** (or https://aistudio.google.com/app/apikey)
2. Sign in with your **Google account**.
3. Click **"Create API Key"** (or "Get API key").
4. Choose a Google Cloud project or create one if asked.
5. **Copy** the key (it usually starts with `AIza...`).

### Hugging Face API Key (for Mood / emotion detection)

1. Open: **https://huggingface.co/settings/tokens**
2. Sign in or **create a free account**.
3. Click **"New token"** (or "Create new token").
4. Name it (e.g. `MindApp`).
5. Select **"Read"** permission.
6. Click **Create** and **copy** the token (it starts with `hf_...`).

---

## Step 2: Add Keys in the App

1. In your project, open:
   ```
   app/src/main/java/com/mindapp/ApiConfig.kt
   ```

2. Replace the placeholders with your **actual** keys:

   **Before:**
   ```kotlin
   const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE"
   const val HUGGING_FACE_API_KEY = "YOUR_HUGGING_FACE_API_KEY_HERE"
   ```

   **After (example – use your own keys):**
   ```kotlin
   const val GEMINI_API_KEY = "AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
   const val HUGGING_FACE_API_KEY = "hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
   ```

3. **Save** the file.

---

## Step 3: Build and Run

- **If you build on your PC (Android Studio / Gradle):**  
  Build the app again and run it. Chat and Mood will use the keys you added.

- **If you build via GitHub Actions:**  
  Do **not** commit `ApiConfig.kt` with real keys (GitHub will block the push).  
  Either:
  - Add keys **only on your PC**, build the APK locally, and install that APK on your phone, or  
  - Use GitHub Secrets and inject keys at build time (advanced).

---

## Quick Reference

| Feature   | Used in  | Key to set              | Where to get it                          |
|----------|----------|--------------------------|------------------------------------------|
| Chat tab | Gemini   | `GEMINI_API_KEY`         | https://makersuite.google.com/app/apikey |
| Mood tab | Hugging Face | `HUGGING_FACE_API_KEY` | https://huggingface.co/settings/tokens   |

---

## Troubleshooting

- **"API is not configured"**  
  Keys are still placeholders or empty. Make sure you replaced `YOUR_GEMINI_API_KEY_HERE` and `YOUR_HUGGING_FACE_API_KEY_HERE` with real keys and saved the file.

- **Chat/Mood works on PC but not on APK from GitHub**  
  The APK from GitHub is built without your keys. Build the APK on your PC (after adding keys in `ApiConfig.kt`) and install that APK on your phone.

- **Never commit real keys to a public repo.**  
  Use placeholders in the repo and add keys only locally when you build for yourself.
