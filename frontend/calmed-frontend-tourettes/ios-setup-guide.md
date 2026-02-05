# iOS OAuth Setup Guide

This guide explains how to set up Google and Apple Sign-In for iOS.

## 1. Google Sign-In Setup

### 1.1 Google Cloud Console Configuration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Go to APIs & Services → Credentials
4. Create a new OAuth 2.0 Client ID for iOS
5. Add your iOS Bundle ID (e.g., `com.calmed.calmedfrontendtourettes`)
6. Download the configuration file

### 1.2 iOS Project Configuration

Add the following to your `Info.plist`:

```xml
<key>GOOGLE_WEB_CLIENT_ID</key>
<string>YOUR_GOOGLE_WEB_CLIENT_ID</string>
```

### 1.3 CocoaPods Installation

```bash
cd iosApp
pod install
```

### 1.4 URL Scheme Configuration

Add the following to your `Info.plist`:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLName</key>
        <string>com.calmed.calmedfrontendtourettes</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>com.calmed.calmedfrontendtourettes</string>
        </array>
    </dict>
</array>
```

## 2. Apple Sign-In Setup

### 2.1 Apple Developer Configuration

1. Go to [Apple Developer Portal](https://developer.apple.com/)
2. Go to Certificates, Identifiers & Profiles
3. Select your App ID
4. Enable "Sign In with Apple"
5. Configure your return URLs

### 2.2 iOS Project Configuration

Add the following to your `Info.plist`:

```xml
<key>APPLE_WEB_CLIENT_ID</key>
<string>com.calmed.calmedfrontendtourettes</string>
<key>APPLE_CALLBACK_URI</key>
<string>calmed://apple</string>
```

### 2.3 URL Scheme Configuration

Add the following to your `Info.plist`:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLName</key>
        <string>calmed</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>calmed</string>
        </array>
    </dict>
</array>
```

## 3. Build Configuration

### 3.1 Update Podfile

Make sure your `Podfile` includes:

```ruby
target 'ComposeApp' do
  use_frameworks!
  pod 'GoogleSignIn', '~> 7.0'
  pod 'Firebase/Auth'
end
```

### 3.2 Install Dependencies

```bash
cd iosApp
pod install --repo-update
```

### 3.3 Build and Run

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Then open the Xcode project and run on simulator/device.

## 4. Testing

### 4.1 Google Sign-In Testing

1. Ensure you're logged into a Google account on the device/simulator
2. Tap the Google Sign-In button
3. Select the account to use
4. Verify the ID token is received

### 4.2 Apple Sign-In Testing

1. On device: Ensure you're logged into an Apple ID
2. On simulator: Configure an Apple ID in Settings
3. Tap the Apple Sign-In button
4. Complete the authentication flow
5. Verify the ID token is received

## 5. Troubleshooting

### 5.1 Common Issues

- **Google Sign-In fails**: Check Bundle ID matches Google Cloud Console
- **Apple Sign-In fails**: Ensure Sign In with Apple is enabled in App ID
- **URL scheme not working**: Verify Info.plist configuration
- **Missing certificates**: Check provisioning profiles

### 5.2 Debug Logging

Enable debug logging by adding:

```kotlin
// In your iOS implementation
println("Google Sign-In Debug: $message")
println("Apple Sign-In Debug: $message")
```

## 6. Production Deployment

### 6.1 App Store Configuration

1. Ensure all certificates and provisioning profiles are production-ready
2. Update Bundle ID to match production
3. Test on physical device
4. Submit to App Store Connect

### 6.2 Privacy Policy

Make sure your privacy policy covers:
- Google Sign-In data collection
- Apple Sign-In data collection
- How authentication data is used
