#!/bin/bash

# Android TV Telegram Player Installation Script
# This script helps install the APK on a connected Android TV device

echo "Android TV Telegram Player Installation Script"
echo "=============================================="

# Check if APK exists
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "APK not found at: $APK_PATH"
    echo "Please build the project first using: ./build.sh"
    exit 1
fi

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "Error: adb (Android Debug Bridge) not found!"
    echo "Please install Android SDK platform-tools."
    exit 1
fi

# Check for connected devices
echo "Checking for connected Android TV devices..."
DEVICES=$(adb devices | grep -w "device" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "No Android TV devices found."
    echo "Please ensure:"
    echo "1. Your Android TV is connected to the same network"
    echo "2. USB debugging is enabled on your TV"
    echo "3. ADB debugging is enabled in TV developer options"
    echo ""
    echo "To connect wirelessly (if TV supports it):"
    echo "adb connect <TV_IP_ADDRESS>:5555"
    exit 1
fi

if [ "$DEVICES" -gt 1 ]; then
    echo "Multiple devices found. Please specify device serial:"
    adb devices
    echo ""
    read -p "Enter device serial: " DEVICE_SERIAL
    ADB_CMD="adb -s $DEVICE_SERIAL"
else
    ADB_CMD="adb"
    echo "Found 1 device"
fi

# Install the APK
echo "Installing APK..."
$ADB_CMD install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo ""
    echo "Installation successful!"
    echo ""
    echo "You can now launch the app from your Android TV home screen."
    echo "Look for 'Telegram TV Player' in your apps."
else
    echo ""
    echo "Installation failed."
    echo "Common issues:"
    echo "1. Insufficient storage on device"
    echo "2. App already installed with different signature"
    echo "3. Incompatible Android version"
fi