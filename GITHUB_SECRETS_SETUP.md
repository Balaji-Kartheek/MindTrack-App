# Setting up GitHub Secrets for API Keys

This guide explains how to configure your API keys securely using GitHub Secrets so the APK built by GitHub Actions has working API integrations.

## Why Use GitHub Secrets?

GitHub Secrets allow you to store sensitive information (like API keys) securely in your repository without committing them to your code. When GitHub Actions builds your APK, it injects these secrets as environment variables.

## Prerequisites

1. A GitHub account with access to your repository
2. Your API keys ready:
   - **Gemini API Key**: Get from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - **Hugging Face API Key**: Get from [Hugging Face Tokens](https://huggingface.co/settings/tokens)

## Step-by-Step Setup

### 1. Get Your API Keys

#### Gemini API Key
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click **"Create API Key"**
4. Copy the generated key (starts with `AIza...`)
5. Keep this key safe - you'll need it in the next step

#### Hugging Face API Key
1. Visit [Hugging Face Settings](https://huggingface.co/settings/tokens)
2. Sign in or create a free account
3. Click **"New token"**
4. Name it (e.g., "MindApp")
5. Select **"Read"** permission
6. Click **"Generate"**
7. Copy the token (starts with `hf_...`)
8. Keep this token safe

### 2. Add Secrets to GitHub Repository

1. **Navigate to your repository** on GitHub
   - Go to `https://github.com/YOUR_USERNAME/YOUR_REPO_NAME`

2. **Open Settings**
   - Click the **Settings** tab at the top of your repository

3. **Access Secrets**
   - In the left sidebar, click **Secrets and variables** → **Actions**

4. **Add GEMINI_API_KEY**
   - Click **"New repository secret"**
   - Name: `GEMINI_API_KEY`
   - Secret: Paste your Gemini API key (the one starting with `AIza...`)
   - Click **"Add secret"**

5. **Add HUGGING_FACE_API_KEY**
   - Click **"New repository secret"** again
   - Name: `HUGGING_FACE_API_KEY`
   - Secret: Paste your Hugging Face token (the one starting with `hf_...`)
   - Click **"Add secret"**

### 3. Verify Setup

After adding both secrets, you should see:
- ✅ `GEMINI_API_KEY`
- ✅ `HUGGING_FACE_API_KEY`

Listed under **"Repository secrets"**.

### 4. Trigger a Build

Now that your secrets are configured, trigger a new build:

#### Option A: Push a Commit
```bash
# Make a small change (e.g., update README)
echo "# Updated" >> README.md
git add README.md
git commit -m "Trigger build with API keys"
git push
```

#### Option B: Manual Workflow Dispatch
1. Go to the **Actions** tab in your repository
2. Select the **"Build APK"** workflow
3. Click **"Run workflow"**
4. Select the branch (usually `main`)
5. Click **"Run workflow"**

### 5. Download and Install APK

1. Wait for the build to complete (usually 3-5 minutes)
2. Go to the **Actions** tab
3. Click on the latest workflow run
4. Scroll down to **Artifacts**
5. Download **app-debug**
6. Extract the ZIP file
7. Transfer `app-debug.apk` to your Android device
8. Install and test

### 6. Test the Features

After installing the APK:

1. **Open the app** and grant Usage Stats permission
2. **Test Chatbot** (Chat tab):
   - Type a message like "How much time did I spend on social media today?"
   - You should get a response from Gemini AI
3. **Test Mood Analysis** (Mood tab):
   - Enter text like "I feel happy and accomplished today"
   - Click "Analyze Mood"
   - You should see emotion detection results

If you see "Please configure API key" errors, double-check:
- Secret names are exactly `GEMINI_API_KEY` and `HUGGING_FACE_API_KEY` (case-sensitive)
- The API keys are valid and active
- The workflow ran after you added the secrets

## Building Locally with API Keys

If you prefer to build locally instead of using GitHub Actions:

### On Linux/Mac:
```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
export HUGGING_FACE_API_KEY="your_hugging_face_token_here"
./gradlew assembleDebug
```

### On Windows (PowerShell):
```powershell
$env:GEMINI_API_KEY="your_gemini_api_key_here"
$env:HUGGING_FACE_API_KEY="your_hugging_face_token_here"
.\gradlew.bat assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Security Best Practices

✅ **DO:**
- Store API keys in GitHub Secrets only
- Revoke and regenerate keys if accidentally exposed
- Use separate keys for development and production
- Regularly rotate your API keys

❌ **DON'T:**
- Commit API keys to your repository
- Share your API keys publicly
- Use production keys in debug builds
- Push API keys in commit history

## Troubleshooting

### "Please configure API key" error in app
- **Cause**: API keys weren't injected during build
- **Fix**: Verify secrets are set correctly in GitHub, then trigger a new build

### GitHub Actions build fails
- **Cause**: Secret names might be incorrect
- **Fix**: Ensure secrets are named exactly `GEMINI_API_KEY` and `HUGGING_FACE_API_KEY`

### API calls return 401/403 errors
- **Cause**: Invalid or expired API keys
- **Fix**: Regenerate keys and update GitHub Secrets

### Secrets don't show in workflow logs
- **Cause**: This is expected behavior for security
- **Fix**: GitHub automatically redacts secret values in logs (shows `***`)

## Additional Resources

- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Google AI Studio](https://makersuite.google.com/app/apikey)
- [Hugging Face Tokens](https://huggingface.co/settings/tokens)
- [Project README](./README.md)
- [API Keys Setup Guide](./API_KEYS_SETUP.md)

---

**Need help?** Check existing GitHub Issues or create a new one with the `help wanted` label.
