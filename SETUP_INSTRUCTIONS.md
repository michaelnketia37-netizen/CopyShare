# CopyShare - Project Setup Instructions

## Step 1: Extract the Project Archive

Since your project is currently stored as `CopyShare_Phase5_Integrated.zip`, follow these steps:

### On Your Local Machine:

```bash
# 1. Clone the repository
git clone https://github.com/michaelnketia37-netizen/CopyShare.git
cd CopyShare

# 2. Extract the ZIP file
unzip CopyShare_Phase5_Integrated.zip -d extracted/

# 3. Navigate to the extracted directory
cd extracted/

# 4. Verify the structure (you should see these directories)
# - app/
# - gradle/
# - build/
# - .gradle/
# - gradle.properties
# - settings.gradle
# - build.gradle
# - gradlew
# - gradlew.bat

# 5. Check if gradlew exists
ls -la gradlew
```

## Step 2: Extract Contents to Repository Root

Once extracted, you'll have the Android project structure. Now you need to move everything from `extracted/` to the root:

```bash
# Go back to repository root
cd ..

# Copy all extracted files to root (careful with this - back up first!)
cp -r extracted/* .

# Verify the structure
ls -la
# You should now see:
# - app/
# - gradle/
# - .github/
# - build.gradle
# - settings.gradle
# - gradlew
# - gradlew.bat
# - etc.
```

## Step 3: Update .gitignore

Create a `.gitignore` file to exclude unnecessary files:

```bash
# Add the standard Android .gitignore
```

## Step 4: Push the Reorganized Structure

```bash
# Stage all changes
git add .

# Commit the reorganized structure
git commit -m "Extract and reorganize Android project structure

- Extract contents from CopyShare_Phase5_Integrated.zip
- Move all files to repository root for proper GitHub structure
- Maintain gradle configuration and source code organization"

# Push to feature branch
git push origin feature/files-bottom-nav
```

## Step 5: The Files Feature is Ready to Integrate

Once the project structure is in place, the Files feature files (already pushed to `feature/files-bottom-nav`) can be integrated:

- **Data Models**: `app/src/main/java/com/copyshare/data/model/MediaFile.kt`
- **Repository**: `app/src/main/java/com/copyshare/data/repository/MediaRepository.kt`
- **ViewModel**: `app/src/main/java/com/copyshare/ui/viewmodel/FilesViewModel.kt`
- **Fragment**: `app/src/main/java/com/copyshare/ui/fragment/FilesFragment.kt`
- **Adapter**: `app/src/main/java/com/copyshare/ui/adapter/MediaFileAdapter.kt`
- **Layouts**: Under `app/src/main/res/layout/`
- **Configuration**: Permissions and FileProvider setup

## Alternative: Automated Setup (Recommended)

If you want to automate this, run this script in your repository root:

```bash
#!/bin/bash

# Extract ZIP
unzip -q CopyShare_Phase5_Integrated.zip -d temp_extract/

# Move contents to root
for item in temp_extract/*; do
    if [ "$(basename "$item")" != ".git" ]; then
        mv "$item" .
    fi
done

# Clean up
rm -rf temp_extract/

echo "✅ Project extraction and reorganization complete!"
ls -la
```

---

**Next Steps**: Once your project structure is organized, you can:
1. Create a pull request from `feature/files-bottom-nav` to `main`
2. Merge the Files feature with your actual Android code
3. Update AndroidManifest.xml with the required permissions
4. Run `./gradlew assembleDebug` to verify everything builds
