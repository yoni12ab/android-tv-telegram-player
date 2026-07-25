#!/bin/bash

# Android TV Telegram Player Build Script
# This script builds the APK for the Android TV Telegram Player

echo "Android TV Telegram Player Build Script"
echo "========================================"

# Check if Android SDK is configured
if [ ! -f "local.properties" ]; then
    echo "Creating local.properties from template..."
    cp local.properties.template local.properties
    echo "Please edit local.properties and set your Android SDK path:"
    echo "sdk.dir=/path/to/your/android/sdk"
    echo ""
    read -p "Press Enter after you've configured local.properties..."
fi

# Check if gradlew exists and is executable
if [ ! -f "gradlew" ]; then
    echo "Error: gradlew not found!"
    echo "Please ensure you're running this script from the project root."
    exit 1
fi

if [ ! -x "gradlew" ]; then
    echo "Making gradlew executable..."
    chmod +x gradlew
fi

# Build the project
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on connected Android TV device:"
    echo "adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "Build failed. Please check the error messages above."
    echo ""
    echo "Common issues:"
    echo "1. Android SDK not configured - edit local.properties"
    echo "2. Java not installed - install JDK 8 or higher"
    echo "3. Missing dependencies - run './gradlew build --refresh-dependencies'"
    exit 1
fi