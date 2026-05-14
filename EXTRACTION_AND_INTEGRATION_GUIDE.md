# CopyShare - Extraction and Integration Guide

## 📋 Overview

Your `CopyShare_Phase5_Integrated.zip` file contains the complete Android project structure. This guide will help you extract it and integrate it properly into the GitHub repository.

## 🎯 Current Situation

✅ Your GitHub Actions workflow already extracts the ZIP during CI/CD build
❌ The source code is NOT committed to the repository yet
❌ The Files feature files need to be integrated into the actual Android source

## 📥 Step 1: Extract the ZIP Locally

### On Windows:
```bash
# Navigate to your repository directory
cd path/to/CopyShare

# Extract using Windows built-in tool or 7-Zip:
# Right-click CopyShare_Phase5_Integrated.zip > Extract All
# OR use PowerShell:
Expand-Archive -Path CopyShare_Phase5_Integrated.zip -DestinationPath extracted/
```

### On macOS/Linux:
```bash
cd ~/path/to/CopyShare
unzip CopyShare_Phase5_Integrated.zip -d extracted/
```

## 📂 Step 2: Verify Extracted Structure

After extraction, your structure should look like:
```
extracted/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle
│   └── ...
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── gradle.properties
```

## 🔧 Step 3: Move Source Code to Repository Root

```bash
# From your CopyShare repository directory:

# 1. Copy all contents from extracted/ to repository root
cp -r extracted/* .

# OR on Windows PowerShell:
Copy-Item -Path "extracted\*" -Destination "." -Recurse -Force

# 2. Verify the structure
ls -la
# You should see: app/, gradle/, build.gradle, settings.gradle, gradlew, etc.
```

## 📝 Step 4: Update .gitignore

Create a `.gitignore` file in the repository root:

```
# Gradle
.gradle/
build/
*.apk
*.aar
*.ap_
*.aab

# Android Studio
.idea/
*.iml
*.iws
*.ipr
local.properties
captures/
.externalNativeBuild
.cxx/

# Kotlin
*.kt.bak

# macOS
.DS_Store

# IDE
.vscode/
*.swp
*.swo
*~

# Python (if using scripts)
__pycache__/
*.pyc

# Logs
*.log
```

## 🚀 Step 5: Commit and Push

```bash
# Stage all files
git add .

# Verify what will be committed
git status

# Commit with message
git commit -m "Extract and integrate CopyShare source code from Phase 5"

# Push to main branch
git push origin main
```

## 🎨 Step 6: Integrate Files Feature

Once the source code is committed, the Files feature files will be ready at:
- `app/src/main/java/com/copyshare/data/model/MediaFile.kt`
- `app/src/main/java/com/copyshare/data/repository/MediaRepository.kt`
- `app/src/main/java/com/copyshare/ui/viewmodel/FilesViewModel.kt`
- `app/src/main/java/com/copyshare/ui/fragment/FilesFragment.kt`
- `app/src/main/java/com/copyshare/ui/adapter/MediaFileAdapter.kt`
- `app/src/main/res/layout/fragment_files.xml`
- `app/src/main/res/layout/item_media_file.xml`
- `app/src/main/res/xml/file_paths.xml`

## ⚙️ Step 7: Update Your Workflows

Update `.github/workflows/main.yml` to work directly with the source code:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v2

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Build APK
        run: ./gradlew assembleRelease

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: apk-builds
          path: app/build/outputs/apk/release/*.apk
          retention-days: 7
```

## 📋 Checklist

- [ ] Extract `CopyShare_Phase5_Integrated.zip`
- [ ] Verify extracted structure contains `app/`, `gradle/`, `build.gradle`, `gradlew`
- [ ] Copy files to repository root
- [ ] Create `.gitignore` file
- [ ] Run `git add .` and verify files with `git status`
- [ ] Commit: `git commit -m "Extract and integrate CopyShare source code"`
- [ ] Push: `git push origin main`
- [ ] Update GitHub Actions workflow (optional)
- [ ] Verify build succeeds in GitHub Actions
- [ ] Merge `feature/files-bottom-nav` branch with integrated Files feature

## 🆘 Troubleshooting

### ZIP extraction fails
- Ensure you have the correct permissions
- Try using 7-Zip or alternative extraction tool
- Verify the ZIP file is not corrupted

### Git shows many untracked files
- This is normal. Review with `git status` before committing
- Update `.gitignore` if needed before staging

### Build fails after extraction
- Ensure `gradlew` has execute permissions: `chmod +x gradlew`
- Check Java version: `java -version` (should be 17+)
- Verify Android SDK is installed

## ✅ Success Indicators

After completing these steps, you should have:
1. Full source code in the repository root
2. GitHub Actions can build directly without unzipping
3. Ready to integrate the Files feature
4. Smaller repository size (source code compresses better than compiled binaries)

For more help, check the [GitHub Actions documentation](https://docs.github.com/en/actions)
