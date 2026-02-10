@echo off
REM Simple batch script to test API keys

echo ========================================
echo   MindApp API Key Tester
echo ========================================

REM Read API keys from .env
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if "%%a"=="GEMINI_API_KEY" set GEMINI_KEY=%%b
    if "%%a"=="HUGGING_FACE_API_KEY" set HF_KEY=%%b
)

echo.
echo Testing Gemini API...
echo Key: %GEMINI_KEY:~0,20%...
echo.

curl -s -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=%GEMINI_KEY%" ^
  -H "Content-Type: application/json" ^
  -d "{\"contents\":[{\"parts\":[{\"text\":\"Say hello in one word\"}]}]}" > gemini_test.json

type gemini_test.json
echo.

echo.
echo Testing Hugging Face API...
echo Key: %HF_KEY:~0,20%...
echo.

curl -s -X POST "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base" ^
  -H "Authorization: Bearer %HF_KEY%" ^
  -H "Content-Type: application/json" ^
  -d "{\"inputs\":\"I am feeling happy today\"}" > hf_test.json

type hf_test.json
echo.

echo.
echo ========================================
echo   Check the outputs above
echo ========================================

del gemini_test.json
del hf_test.json
pause
