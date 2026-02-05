# Setup Gradle Wrapper

## Current Status
The `gradle-wrapper.jar` file is missing from your repository. The GitHub Actions workflow has been updated to handle this automatically using the official Gradle setup action.

## Option 1: Let GitHub Actions Handle It (Recommended)
The updated workflow uses `gradle/actions/setup-gradle@v4` which automatically:
- Generates the wrapper if missing
- Validates the wrapper
- Sets up Gradle properly

**Just push your code and the workflow will work!**

## Option 2: Generate Wrapper Locally (Optional)

If you want to commit the wrapper jar to your repo (recommended for faster builds):

### On Windows (PowerShell):
```powershell
# Install Gradle first (if not installed)
# Download from: https://gradle.org/releases/
# Or use Chocolatey: choco install gradle

# Then run:
gradle wrapper --gradle-version 8.2
```

### On Mac/Linux:
```bash
# Install Gradle first (if not installed)
# brew install gradle  # Mac
# sudo apt install gradle  # Ubuntu

# Then run:
gradle wrapper --gradle-version 8.2
```

### Using Docker (No Local Install Needed):
```bash
docker run --rm -v "$PWD":/project -w /project gradle:8.2-jdk17 gradle wrapper --gradle-version 8.2
```

After running any of these commands, you'll get:
- `gradle/wrapper/gradle-wrapper.jar` ✅
- `gradle/wrapper/gradle-wrapper.properties` (already exists)
- `gradlew` (already exists)
- `gradlew.bat` (already exists)

Then commit:
```bash
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "Add gradle-wrapper.jar"
git push
```

## Option 3: Download Directly (Quick Fix)

You can download the wrapper jar directly:

**Windows PowerShell:**
```powershell
New-Item -ItemType Directory -Force -Path "gradle\wrapper" | Out-Null
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.2-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

**Mac/Linux:**
```bash
mkdir -p gradle/wrapper
curl -L https://services.gradle.org/distributions/gradle-8.2-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
```

Then commit:
```bash
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "Add gradle-wrapper.jar"
git push
```

## Verification

After setup, verify the wrapper works:
```bash
./gradlew --version
```

You should see:
```
Gradle 8.2
```

## Current Workflow Solution

The GitHub Actions workflow now uses `gradle/actions/setup-gradle@v4` which:
- ✅ Automatically generates wrapper if missing
- ✅ Validates wrapper integrity
- ✅ Handles caching
- ✅ Works without local setup

**You don't need to do anything - just push and it will work!**
