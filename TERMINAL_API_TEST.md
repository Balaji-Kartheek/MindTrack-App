# Quick Terminal API Test Guide

## Test Your API Keys in Terminal

### Method 1: Using Python (Recommended)

If you have Python installed, run:

```bash
python test_api_keys.py
```

This will test both APIs and show you detailed results.

---

### Method 2: Manual cURL Test (Windows PowerShell)

#### Test Gemini API:

```powershell
# Get your key from local.properties or .env
$geminiKey = "YOUR_GEMINI_API_KEY_HERE"
$body = @{
    contents = @(
        @{
            parts = @(
                @{ text = "Say hello" }
            )
        }
    )
} | ConvertTo-Json -Depth 10

$response = Invoke-RestMethod -Uri "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$geminiKey" -Method Post -Body $body -ContentType "application/json"

if ($response.candidates) {
    Write-Host "✅ Gemini API Working!" -ForegroundColor Green
    Write-Host "Response: $($response.candidates[0].content.parts[0].text)" -ForegroundColor Green
} else {
    Write-Host "❌ Gemini API Failed" -ForegroundColor Red
    Write-Host $response
}
```

#### Test Hugging Face API:

```powershell
# Get your key from local.properties or .env
$hfKey = "YOUR_HF_API_KEY_HERE"
$headers = @{ "Authorization" = "Bearer $hfKey" }
$body = '{"inputs":"I am feeling happy today"}'

try {
    $response = Invoke-RestMethod -Uri "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base" -Method Post -Headers $headers -Body $body -ContentType "application/json"
    
    Write-Host "✅ Hugging Face API Working!" -ForegroundColor Green
    Write-Host "Top emotions:" -ForegroundColor Green
    $response[0..2] | ForEach-Object {
        $pct = [math]::Round($_.score * 100, 1)
        Write-Host "  • $($_.label): $pct%" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Hugging Face API Error: $($_.Exception.Message)" -ForegroundColor Red
}
```

---

### Method 3: Online Testing (No Terminal)

#### Test Gemini:
1. Go to: https://aistudio.google.com/
2. Enter your API key
3. Try a test prompt

#### Test Hugging Face:
1. Go to: https://huggingface.co/j-hartmann/emotion-english-distilroberta-base
2. Click "Deploy" → "Inference API"
3. Enter your API key and test

---

## What the Tests Mean

### ✅ Success Indicators:
- **Gemini**: You see a text response
- **Hugging Face**: You see emotion labels with confidence scores

### ❌ Error Indicators:
- **404 Not Found**: Model name or endpoint wrong
- **401 Unauthorized**: API key invalid
- **410 Gone**: API endpoint deprecated  
- **503 Service Unavailable**: Model is loading (wait 30s and retry)

---

## Next Steps After Testing

### If Both APIs Work:
```bash
# Rebuild your app
.\gradlew clean assembleDebug

# Install the new APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### If APIs Don't Work:
1. **Check API keys** - Verify they're correct in `.env`
2. **Get new keys**:
   - Gemini: https://makersuite.google.com/app/apikey
   - Hugging Face: https://huggingface.co/settings/tokens
3. **Update local.properties** with the new keys
4. **Rebuild** the app

---

## Why Test in Terminal?

Testing in terminal is **faster** because:
- No need to rebuild Android app
- Instant feedback on API validity
- Can verify keys before committing to build

Once terminal tests pass, your Android app **will work** after rebuilding!
