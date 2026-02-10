#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Quick API Key Tester for MindApp
Run this to test your API keys without building the Android app.

Usage: python test_api_keys.py
"""

import requests
import json
import sys
import io

# Fix Windows console encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Try importing huggingface_hub for better API support
try:
    from huggingface_hub import InferenceClient
    HAS_HF_HUB = True
except ImportError:
    HAS_HF_HUB = False

# Read API keys from .env file
def read_env_file():
    keys = {}
    try:
        with open('.env', 'r') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#'):
                    key, value = line.split('=', 1)
                    keys[key.strip()] = value.strip()
    except FileNotFoundError:
        print("❌ .env file not found!")
        sys.exit(1)
    return keys

def test_gemini_api(api_key):
    """Test Gemini API"""
    print("\n🔍 Testing Gemini API...")
    print(f"Key: {api_key[:20]}...")
    
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
    
    payload = {
        "contents": [{
            "parts": [{
                "text": "Say hello in one word"
            }]
        }]
    }
    
    try:
        response = requests.post(url, json=payload, timeout=10)
        
        if response.status_code == 200:
            data = response.json()
            if 'candidates' in data:
                result = data['candidates'][0]['content']['parts'][0]['text']
                print(f"✅ Gemini API Working!")
                print(f"Response: {result}")
                return True
            else:
                print(f"❌ Unexpected response format")
                print(json.dumps(data, indent=2))
                return False
        else:
            print(f"❌ API Error: {response.status_code}")
            print(f"Response: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Network Error: {e}")
        return False

def test_huggingface_api(api_key):
    """Test Hugging Face API"""
    print("\n🔍 Testing Hugging Face API...")
    print(f"Key: {api_key[:20]}...")
    
    # Method 1: Try using official InferenceClient if available
    if HAS_HF_HUB:
        try:
            print("Using InferenceClient...")
            client = InferenceClient(
                model="j-hartmann/emotion-english-distilroberta-base",
                token=api_key
            )
            result = client.text_classification("I am feeling happy today")
            print(f"✅ Hugging Face API Working!")
            print("Detected emotions:")
            for emotion in result[:3]:
                print(f"  • {emotion['label']}: {emotion['score']*100:.1f}%")
            return True
        except Exception as e:
            print(f"InferenceClient failed: {e}")
            print("Falling back to direct API call...")
    
    # Method 2: Direct API call
    url = "https://api-inference.huggingface.co/models/j-hartmann/emotion-english-distilroberta-base"
    
    headers = {
        "Authorization": f"Bearer {api_key}"
    }
    
    payload = {
        "inputs": "I am feeling happy today"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers, timeout=30)
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Hugging Face API Working!")
            print("Detected emotions:")
            for emotion in data[:3]:  # Show top 3 emotions
                print(f"  • {emotion['label']}: {emotion['score']*100:.1f}%")
            return True
        elif response.status_code == 503:
            print(f"⏳ Model is loading (503). Please wait 30 seconds and try again.")
            return False
        else:
            print(f"❌ API Error: {response.status_code}")
            print(f"Response: {response.text}")
            print(f"\n💡 Tip: Your HF API key might be expired. Get a new one at:")
            print(f"   https://huggingface.co/settings/tokens")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Network Error: {e}")
        return False

def main():
    print("="*50)
    print("  MindApp API Key Tester")
    print("="*50)
    
    # Read keys
    keys = read_env_file()
    
    if 'GEMINI_API_KEY' not in keys:
        print("❌ GEMINI_API_KEY not found in .env")
        return
    
    if 'HUGGING_FACE_API_KEY' not in keys:
        print("❌ HUGGING_FACE_API_KEY not found in .env")
        return
    
    # Test both APIs
    gemini_ok = test_gemini_api(keys['GEMINI_API_KEY'])
    hf_ok = test_huggingface_api(keys['HUGGING_FACE_API_KEY'])
    
    # Summary
    print("\n" + "="*50)
    print("  SUMMARY")
    print("="*50)
    print(f"Gemini API: {'✅ Working' if gemini_ok else '❌ Failed'}")
    print(f"Hugging Face API: {'✅ Working' if hf_ok else '❌ Failed'}")
    
    if gemini_ok and hf_ok:
        print("\n🎉 All APIs are working! You can now build the app.")
    else:
        print("\n⚠️  Some APIs failed. Check the keys in .env file.")

if __name__ == "__main__":
    main()
