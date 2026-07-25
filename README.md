# Android TV Telegram Player

An Android TV application for playing and recording videos from Telegram with sender verification.

## Features

- **Android TV Interface**: Leanback-based UI optimized for TV screens
- **Telegram Integration**: Connect to Telegram to access video content
- **Video Playback**: High-quality video playback using ExoPlayer
- **Recording**: Record videos while watching
- **Sender Verification**: Only authorized senders can use the app
- **Error Handling**: Robust handling of Telegram API errors including 403 fixes

## Project Structure

```
AndroidTvTelegramPlayer/
├── app/
│   ├── src/main/
│   │   ├── java/com/telegram/tvplayer/
│   │   │   ├── MainActivity.kt           # Main TV browse interface
│   │   │   ├── VideoPlayerActivity.kt    # Video playback and recording
│   │   │   ├── AuthActivity.kt           # Phone authentication
│   │   │   ├── TelegramManager.kt        # Telegram API integration
│   │   │   └── VideoPresenter.kt         # TV card presenter
│   │   ├── res/
│   │   │   ├── layout/                   # UI layouts
│   │   │   ├── values/                   # Strings, themes, colors
│   │   │   └── drawable/                 # Graphics
│   │   └── AndroidManifest.xml
│   └── build.gradle                      # App-level dependencies
├── build.gradle                          # Project-level build
├── settings.gradle                       # Project settings
└── README.md
```

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK (API 21+)
- Telegram API ID and Hash (get from https://my.telegram.org)

## Setup Instructions

### 1. Configure Telegram API

Open `TelegramManager.kt` and replace with your credentials:

```kotlin
companion object {
    private const val API_ID = 12345 // Replace with your API ID
    private const val API_HASH = "your_api_hash_here" // Replace with your API hash
}
```

### 2. Set Authorized User ID

Replace the authorized user ID in `TelegramManager.kt`:

```kotlin
private val AUTHORIZED_USER_ID = 123456789L // Replace with your Telegram user ID
```

### 3. Build with Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `AndroidTvTelegramPlayer` directory
4. Wait for Gradle sync to complete
5. Select Build > Build Bundle(s) / APK(s) > Build APK(s)

### 4. Build with Command Line

If you have Android SDK command line tools installed:

```bash
cd AndroidTvTelegramPlayer
./gradlew assembleDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## Configuration

### Telegram API Setup

1. Visit https://my.telegram.org
2. Sign in with your phone number
3. Go to "API development tools"
4. Create a new application
5. Copy the API ID and API Hash
6. Update them in `TelegramManager.kt`

### Find Your Telegram User ID

1. Send a message to @userinfobot in Telegram
2. The bot will reply with your user ID
3. Update `AUTHORIZED_USER_ID` in `TelegramManager.kt`

## Features Implementation

### Video Playback
- Uses ExoPlayer for robust video playback
- Supports multiple video formats
- Optimized for Android TV remote control

### Recording Functionality
- Records video and audio while playing
- Saves to app's private storage
- Simple start/stop controls

### Sender Verification
- Verifies sender ID before allowing access
- Prevents unauthorized use
- Configurable authorized user list

### Error Handling
- Handles 403 Forbidden errors with retry logic
- Rate limit handling with exponential backoff
- User-friendly error messages

## Dependencies

- AndroidX Leanback (TV UI)
- ExoPlayer (Media playback)
- TDLib (Telegram client library)
- Kotlin Coroutines (Async operations)
- Retrofit (Network requests)

## Troubleshooting

### Build Issues

**Gradle sync fails:**
- Ensure Android SDK is installed
- Check that ANDROID_HOME is set
- Update Android Studio to latest version

**Telegram API errors:**
- Verify API ID and Hash are correct
- Check internet connection
- Ensure phone number is verified

### 403 Errors

The app includes comprehensive 403 error handling:

- **USER_PRIVACY_RESTRICTED**: User privacy settings prevent access
- **CHAT_WRITE_FORBIDDEN**: Cannot write to the specified chat  
- **PEER_FLOOD**: Too many requests, automatic retry with backoff

## Security Notes

- API credentials should be kept secure
- Consider using environment variables for sensitive data
- The app requests necessary permissions for recording and network access
- Sender verification helps prevent unauthorized access

## Future Enhancements

- Support for multiple authorized users
- Cloud storage integration
- Advanced recording options
- Channel/subscription management
- Offline mode support

## License

This project is provided as-is for educational and personal use.

## Support

For issues related to:
- **Telegram API**: Check https://core.telegram.org/api
- **Android TV**: See https://developer.android.com/training/tv
- **ExoPlayer**: Visit https://developer.android.com/guide/topics/media/exoplayer