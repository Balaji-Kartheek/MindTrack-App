# PowerShell script to test API keys locally
# Usage: .\test_api_keys.ps1

Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan
Write-Host "  MindApp API Key Tester (PowerShell)" -ForegroundColor Yellow
Write-Host "="*50 -ForegroundColor Cyan

# Read .env file
$envVars = @{}
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split("=", 2)
            if ($parts.Length -eq 2) {
                $envVars[$parts[0].Trim()] = $parts[1].Trim()
            }
        }
    }
} else {
    Write-Host "❌ .env file not found!" -ForegroundColor Red
    exit 1
}

# Get API keys
$geminiKey = $envVars["GEMINI_API_KEY"]
$hfKey = $envVars["HUGGING_FACE_API_KEY"]

if (-not $geminiKey) {
    Write-Host "❌ GEMINI_API_KEY not found in .env" -ForegroundColor Red
    exit 1
}

if (-not $hfKey) {
    Write-Host "❌ HUGGING_FACE_API_KEY not found in .env" -ForegroundColor Red
    exit 1
}

# Test Gemini API
Write-Host "`n🔍 Testing Gemini API..." -ForegroundColor Cyan
Write-Host "Key: $($geminiKey.Substring(0, [Math]::Min(20, $geminiKey.Length)))..." -ForegroundColor Gray

$geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$geminiKey"
$geminiBody = @{
    contents = @(
        @{
            parts = @(
                @{ text = "Say hello in one word" }
            )
        }
    )
} | ConvertTo-Json -Depth 10

try {
    $geminiResponse = Invoke-RestMethod -Uri $geminiUrl -Method Post -Body $geminiBody -ContentType "application/json" -TimeoutSec 10
    
    if ($geminiResponse.candidates) {
        $result = $geminiResponse.candidates[0].content.parts[0].text
        Write-Host "✅ Gemini API Working!" -ForegroundColor Green
        Write-Host "Response: $result" -ForegroundColor Green
        $geminiOk = $true
    } else {
        Write-Host "❌ Unexpected response format" -ForegroundColor Red
        $geminiOk = $false
    }
} catch {
    Write-Host "❌ Gemini API Error: $($_.Exception.Message)" -ForegroundColor Red
    $geminiOk = $false
}

# Test Hugging Face API
Write-Host "`n🔍 Testing Hugging Face API..." -ForegroundColor Cyan
Write-Host "Key: $($hfKey.Substring(0, [Math]::Min(20, $hfKey.Length)))..." -ForegroundColor Gray

$hfUrl = "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base"
$hfHeaders = @{
    "Authorization" = "Bearer $hfKey"
}
$hfBody = @{
    inputs = "I am feeling happy today"
} | ConvertTo-Json

try {
    $hfResponse = Invoke-RestMethod -Uri $hfUrl -Method Post -Headers $hfHeaders -Body $hfBody -ContentType "application/json" -TimeoutSec 30
    
    Write-Host "✅ Hugging Face API Working!" -ForegroundColor Green
    Write-Host "Detected emotions:" -ForegroundColor Green
    foreach ($emotion in $hfResponse[0..2]) {
        $percentage = [math]::Round($emotion.score * 100, 1)
        Write-Host "  • $($emotion.label): $percentage%" -ForegroundColor Green
    }
    $hfOk = $true
} catch {
    Write-Host "❌ Hugging Face API Error: $($_.Exception.Message)" -ForegroundColor Red
    $hfOk = $false
}

# Summary
Write-Host "`n$("="*50)" -ForegroundColor Cyan
Write-Host "  SUMMARY" -ForegroundColor Yellow
Write-Host "$("="*50)" -ForegroundColor Cyan

if ($geminiOk) {
    Write-Host "Gemini API: ✅ Working" -ForegroundColor Green
} else {
    Write-Host "Gemini API: ❌ Failed" -ForegroundColor Red
}

if ($hfOk) {
    Write-Host "Hugging Face API: ✅ Working" -ForegroundColor Green
} else {
    Write-Host "Hugging Face API: ❌ Failed" -ForegroundColor Red
}

if ($geminiOk -and $hfOk) {
    Write-Host "`n🎉 All APIs are working! You can now build the app." -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Some APIs failed. Check the keys in .env file." -ForegroundColor Yellow
}
