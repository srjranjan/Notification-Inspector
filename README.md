# Notification Inspector 🔔

<p align="center">
  <a href="https://kotlinlang.org/docs/multiplatform.html">
    <img src="https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg?style=flat-square&logo=kotlin" alt="Kotlin Multiplatform" />
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20JVM%20%7C%20JS%20%7C%20Wasm-orange.svg?style=flat-square" alt="Platform Support" />
  </a>
  <a href="https://search.maven.org/artifact/io.github.srjranjan/shared">
    <img src="https://img.shields.io/maven-central/v/io.github.srjranjan/shared.svg?label=Maven%20Central&style=flat-square" alt="Maven Central" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License" />
  </a>
</p>

---

## 🚀 Introduction

**Notification Inspector** is a lightweight, pure Kotlin Multiplatform (KMP) on-device DevTools for push notifications, designed to capture, log, and inspect push notification payloads directly on-device. Supporting **Android, iOS, JVM (Desktop), JavaScript, and WebAssembly**, it provides developers with an interactive, on-device UI console to monitor and audit Firebase Cloud Messages (FCM) or Apple Push Notifications (APNs) in real-time, greatly accelerating the development and QA cycle.

---

## 📱 Platform Support & Compatibility

| Platform | Support Status | Payload Type | Features |
| :--- | :--- | :--- | :--- |
| **Android** (API 21+) | ✅ Fully Implemented | `RemoteMessage` (FCM) | Auto-interception, Standalone Activity UI, Room SQLite DB |
| **iOS** (iOS 12+) | ✅ Fully Implemented | `NSDictionary` (APNs) | Auto-interception, Embeddable SwiftUI Component, Room SQLite DB |
| **JVM (Desktop)** | 🚧 Work In Progress | `Any` | Coming Soon (no-op stub) |
| **JavaScript (JS)** | 🚧 Work In Progress | `Any` | Coming Soon (no-op stub) |
| **WebAssembly (Wasm)** | 🚧 Work In Progress | `Any` | Coming Soon (no-op stub) |

---

## 🎯 Why It's Needed (The Problem It Solves)

Debugging push notifications is historically a tedious process. Developers typically have to search through verbose IDE system logs (Logcat/Xcode console), set up complex local proxy tools (Charles, Proxyman), or rely on backend logs to verify that payload properties are being delivered correctly. 

Moreover, keeping debugging code and log history in production builds poses significant security risks. 

**Notification Inspector** solves this elegantly by providing:
* **Direct Interception**: It hooks into platform-level push handlers (`RemoteMessage` on Android, `NSDictionary` on iOS) to automatically extract and format payloads.
* **On-Device Console**: Built using Compose Multiplatform, the inspector database and console UI let you view, search, and parse payloads directly on the testing device.
* **No-Op Production Safety**: A matching `shared-no-op` library variant is provided. By swapping dependencies in production, all database code, notification interceptors, and UI components are completely stripped out, achieving zero overhead and total security.

---

## 💡 Use Cases

* 🔍 **On-Device Payload Auditing**: Quickly inspect the exact raw JSON structure of push notifications on physical devices without needing server access or CLI tools.
* 🌳 **Interactive Tree-Folding JSON View**: Parse complex, nested payload dictionaries through a clean, hierarchical tree-view with collapsible nodes.
* 📂 **Persistent Notification Log History**: Automatically persist notification payloads locally using a Room SQLite database, allowing inspection of historical logs across application restarts.
* 🛡️ **Zero-Footprint Releases**: Easily isolate the debug console to development/staging builds using the no-op variant to prevent leaks in production.

---

## 📦 Installation & Setup

### A. Kotlin Multiplatform / Android (via Maven Central)

Add the dependency to your shared/common module's `build.gradle.kts` file:

```kotlin
sourceSets {
    commonMain.dependencies {
        // Debug configuration uses the full inspector library
        implementation("io.github.srjranjan:shared:1.0.13")
    }
}
```

To automatically isolate the inspector to development builds and use the safe no-op implementation in production, declare the dependencies conditionally inside your Android application module's `build.gradle.kts`:

```kotlin
dependencies {
    // Standard debug builds contain the inspector UI and Room DB
    debugImplementation("io.github.srjranjan:shared:1.0.13")
    
    // Release builds compile against the empty no-op variant
    releaseImplementation("io.github.srjranjan:shared-no-op:1.0.13")
}
```

### B. Native iOS (via Swift Package Manager)

For native iOS apps or when linking the shared framework directly via Swift Package Manager in Xcode:

1. In Xcode, navigate to **File** -> **Add Packages...**
2. Enter the repository URL: `https://github.com/srjranjan/Notification-Inspector`
3. Define your package dependency rule (e.g., Up to Next Major **1.0.13** or select the `main` branch).

---

## 🛠️ How to Use

### 1. Kotlin Example (Common & Android Layer)

#### Initializing the Inspector
Create an instance of `NotificationInspector` by passing a `PlatformContext`. On Android, this requires the application/activity context, whereas on other platforms it requires no arguments:

```kotlin
import com.srj.notificationinspector.NotificationInspector
import com.srj.notificationinspector.PlatformContext

// Android Initialization (typically in Application, Activity, or Service)
val platformContext = PlatformContext(androidContext)
val inspector = NotificationInspector(platformContext)

// iOS / JVM / Desktop Initialization
val platformContext = PlatformContext()
val inspector = NotificationInspector(platformContext)
```

#### Intercepting FCM Payloads (Android)
To automatically record incoming notifications, capture them inside your `FirebaseMessagingService`:

```kotlin
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var inspector: NotificationInspector

    override fun onCreate() {
        super.onCreate()
        inspector = NotificationInspector(PlatformContext(applicationContext))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Captures, parses, and logs the payload
        inspector.capture(remoteMessage)
        
        // Handle your message rendering here...
    }
}
```

#### Launching the Inspector UI (Android)
Launch the standalone Inspector console Activity from anywhere in your debug menu or shake detector:

```kotlin
// Launches InspectorActivity which hosts the Compose Multiplatform UI
inspector.launch()
```

#### Embedding the Composable UI
If you want to host the logs UI inside your own custom settings or debug Composable, use `NotificationInspectorApp` directly:

```kotlin
import com.srj.notificationinspector.ui.NotificationInspectorApp
import com.srj.notificationinspector.getNotificationRepository

@Composable
fun DebugConsoleScreen(context: PlatformContext) {
    val repository = remember(context) { getNotificationRepository(context) }
    
    NotificationInspectorApp(
        repository = repository,
        onClose = { /* Handle navigation back */ }
    )
}
```

---

### 2. Swift Example (iOS / SwiftUI Layer)

On iOS, push notification payloads are received as `NSDictionary`. You can intercept them inside your AppDelegate and display the interface in SwiftUI.

#### Intercepting APNs Push Payloads
Add payload interception to your `UNUserNotificationCenterDelegate` implementation:

```swift
import Shared
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    private let inspector = NotificationInspector(context: PlatformContext())

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        
        // Convert APNs userInfo payload to NSDictionary
        let userInfo = response.notification.request.content.userInfo as NSDictionary
        
        // Record and parse payload
        inspector.capture(message: userInfo)
        
        completionHandler()
    }
}
```

#### Presenting the Console in SwiftUI
Wrap the exported Compose View Controller inside `UIViewControllerRepresentable` and display it in a sheet or navigation view:

```swift
import SwiftUI
import Shared

// Wrap the Compose controller for SwiftUI
struct NotificationInspectorView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // MainViewController is generated by the shared KMP library
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// Host it in your debug settings
struct DebugSettingsMenu: View {
    @State private var isInspectorOpen = false

    var body: some View {
        VStack {
            Button(action: { isInspectorOpen = true }) {
                Label("Open Notification Inspector", systemImage: "bell.badge.fill")
                    .font(.headline)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .sheet(isPresented: $isInspectorOpen) {
            NotificationInspectorView()
                .edgesIgnoringSafeArea(.all)
        }
    }
}
```

---

## 🔄 Notification Replay (Push Simulation)

The **Replay** feature allows you to **simulate a native push notification delivery locally** on your device using a previously captured payload. 

Unlike a simple "re-show" of a banner, this feature acts as a **local push injector**. It programmatically re-delivers the exact raw FCM (Android) or APNs (iOS) payload directly back into your application's system entry points. This allows your app's **existing, unmodified push-handling code** (routing, deep-linking, data parsing) to execute naturally as if a real push had arrived from the network.

### 🛠️ How it Works

*   **Android**: The library dynamically discovers your app's `FirebaseMessagingService` and broadcasts a local `com.google.android.c2dm.intent.RECEIVE` intent. This triggers your `onMessageReceived(RemoteMessage)` method programmatically within the same process. No ADB or root required.
*   **iOS**: The library reconstructs the `NSDictionary` payload and dynamically invokes your App Delegate's `didReceiveRemoteNotification` methods using the Objective-C runtime.

### 🚀 Usage

1.  Open any captured log in the **Notification Inspector UI**.
2.  Click the **Replay** icon (🔄) in the top action bar.
3.  Your app will process the notification as if it were just delivered by the system.

#### (Optional) Registering a Replay Listener
If you need to perform specific debug logic when a replay is triggered, you can register a global listener:

```kotlin
import com.srj.notificationinspector.NotificationInspector

// Register in your Application class or MainActivity
NotificationInspector.replayListener = { log ->
    println("Manual replay triggered for log ID: ${log.id}")
}
```

---

## 🤝 Contributing

We welcome contributions of all types! Whether you are reporting a bug, suggesting a new feature, or submitting code changes, please read our [Contributing Guidelines](CONTRIBUTING.md) to get started.

---

## 📄 License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
