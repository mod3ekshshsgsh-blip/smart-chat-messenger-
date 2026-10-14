# Smart Chat Messenger (Android)

Smart Chat Messenger is a production-oriented, native Android messaging application built with Kotlin, Jetpack Compose, and Material 3, designed using Google Stitch references. It features real-time conversation threads, voice note visualizers, active moments stories, user discovery, friend requests, customizable themes, and robust Firebase integrations.

---

## 📱 Features

- **Conversations & Real-Time Messaging**:
  - Live conversation threads with bidirectional message streaming.
  - Interactive voice note player with animated waveform bars and 1.0x / 1.5x / 2.0x playback controls.
  - Image mockup previews with tags (e.g. "Design Specs v2.4").
  - Reply-to quoted message snippets and emoji reactions (👍, ❤️, add reaction).
  - Animated typing indicator and "New messages" quick-scroll pill.
  - Contextual attachment drawer (Photos, Files, Location, Poll).
  - Dynamic composer bar (microphone switch to send arrow upon typing).
- **Active Moments & Stories**:
  - Horizontal stories carousel with live pulsating rings, creator avatars, and story creation.
- **Category Filter Chips**:
  - All, Unread, Groups, Channels, and Archived filtering.
- **User Discovery & Friend Request System**:
  - Network Pulse hero banner.
  - Pending incoming requests with immediate Accept and Decline feedback.
  - Suggested friends with professional interest pills (UI Design, Swift, WebGL) and instant add/cancel toggles.
  - Community exploration (Mobile UI Designers Club, Tech Founders Lounge).
- **User Profile & Customization**:
  - Interactive profile banner with status indicators.
  - Status switcher ("Available", "In a Meeting", "Focus Mode ☕️", "Away").
  - Member ID copy-to-clipboard with instant visual feedback.
  - Theme mode switch (Light Tone vs Dark Indigo) and Chat Bubble Palette selector (Indigo, Blue, Violet, Emerald).
  - Interactive personal QR Code dialog.
- **Authentication**:
  - Email and password registration & login with client-side and backend validation.
  - Password recovery by email.
  - One-tap demo account exploration as Alex Rivera.

---

## 🛠️ Required Software & Toolchain

- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
- **JDK**: Java Development Kit 17 or 21.
- **Android SDK**:
  - `compileSdk`: 36
  - `targetSdk`: 36
  - `minSdk`: 24 (Android 7.0+)
- **Build System**: Gradle 9.x with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`).

---

## 🚀 How to Open and Run the Project

1. Clone or extract the project repository to your development machine.
2. Open Android Studio, select **Open**, and navigate to the root directory of this repository.
3. Allow Gradle to synchronize dependencies.
4. Select a connected physical device or Android Virtual Device (AVD) running API 24 or newer.
5. Click **Run 'app'** (`Shift + F10`).
6. To assemble a debug APK via the command line:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔥 Firebase Configuration & Setup

### 1. Create a Firebase Project
1. Navigate to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and enter a name (e.g., `smart-chat-messenger`).

### 2. Register the Android App
1. In the Project Overview, click the **Android** icon to add an Android app.
2. Enter the Android package name:
   ```
   com.aistudio.smartchat.jqvzrl
   ```
3. Enter your debug SHA-1 signing certificate fingerprint (obtainable by running `./gradlew signingReport`).
4. Click **Register app**.
5. Download `google-services.json` and place it in the `app/` directory (`app/google-services.json`).

### 3. Enable Authentication
1. In Firebase Console, open **Build > Authentication**.
2. Click **Get Started**.
3. Under the **Sign-in method** tab:
   - Enable **Email/Password**.
   - Enable **Google** (ensure the Web Client ID is configured for Credential Manager).

### 4. Enable Cloud Firestore & Deploy Rules
1. In Firebase Console, open **Build > Firestore Database**.
2. Click **Create database** and select your preferred Cloud region.
3. Start in **Production mode**.
4. Deploy the rules defined in `firestore.rules` using the Firebase CLI:
   ```bash
   firebase deploy --only firestore:rules,firestore:indexes
   ```

### 5. Configure Realtime Database for Presence (Optional)
1. Open **Build > Realtime Database**.
2. Deploy the presence rules in `database.rules.json`:
   ```bash
   firebase deploy --only database
   ```

### 6. Cloud Functions & FCM Push Notifications
1. Install Firebase CLI: `npm install -g firebase-tools`
2. Log in and initialize: `firebase login`
3. Deploy functions located in the `functions/` directory:
   ```bash
   firebase deploy --only functions
   ```

---

## 🧪 Automated Testing

Run JVM unit tests using Gradle:
```bash
gradle :app:testDebugUnitTest
```

---

## 📦 What Remains Before Production Release

1. **Production Keystore Signing**: Replace the placeholder `debug.keystore` with a Google Play upload key.
2. **Firebase Storage**: If users should upload original high-resolution attachments from their device camera/gallery, enable Firebase Storage buckets and add storage security rules.
3. **App Check**: Register your SHA-256 fingerprint in the Firebase App Check console to protect APIs against abuse.
