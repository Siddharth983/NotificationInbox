# 📬 NotificationInbox – Android Application

A complete, production-ready Android Notification Inbox application built using **Kotlin**, **MVVM Architecture**, **Room Database**, **Firebase Cloud Messaging (FCM)**, **Glide**, and **Material Design 3 (XML Layouts)**.

---

## 📱 Features

### 1. Notification Inbox Screen
* **Toolbar & Status**: Custom toolbar displaying the title `"Notification Inbox"` with a real-time dynamic badge showing the unread notification count.
* **Modern Notification Cards**:
  * **Unread State**: Styled with a subtle blue-tinted background (`#F0F5FF`), light blue border (`#BFDBFE`), bold title typography, an active unread indicator dot, and an `"Unread"` status tag.
  * **Read State**: Clean pure-white background (`#FFFFFF`), neutral border (`#E2E8F0`), standard title weight, hidden unread dot, and a `"Read"` status tag.
* **Banner / Thumbnail Loading**: Integrated with **Glide** featuring rounded corners, center-crop scaling, smooth placeholders (`ic_placeholder_image`), and error fallbacks (`ic_broken_image`).
* **Empty State**: An intuitive and visually pleasing empty state when no notifications are present, including a one-tap button to insert sample notifications for testing.
* **Toolbar Actions**:
  * **Add Test Notification**: Injects sample notifications with images and descriptions to easily verify Room persistence and UI states without needing a live backend.
  * **Mark All as Read**: Marks all saved notifications as read with a single tap.
  * **Clear All**: Clears all notifications from the local database.

### 2. Notification Details Screen
* **Detailed View**: Displays the complete full-width banner image, complete title, formatted received date and time, and full body description inside a scrollable layout.
* **Automatic Read Marking**: When a user clicks a notification in the inbox, opening the details screen automatically marks it as read in the Room database and immediately decrements the unread count.
* **Interactive Actions**:
  * Toggle between **"Mark as read"** and **"Mark as unread"**.
  * **Delete**: Removes the notification and returns seamlessly to the inbox.
  * Native back navigation to return to the inbox.

### 3. Firebase Cloud Messaging (FCM) Integration
* **Notification Channel**: Automatically configures the system notification channel with ID **`assignment`** (Name: `"Inbox Notifications"`, Importance: `HIGH`, Vibration & Lights enabled).
* **Foreground & Background Notifications**: Handled via `NotificationInboxFirebaseService`. Incoming push messages are parsed, saved to Room, and alerted in the status bar with `BigPictureStyle` banners.
* **Notification Tap Navigation**: Tapping a system notification opens `NotificationDetailActivity` directly for that specific item.
* **Android 13+ (API 33+) Permission**: Fully supports runtime permission requests for `POST_NOTIFICATIONS` with user-friendly permission rationales.

### 4. Room Local Database
* **Offline Persistence**: Notifications are saved in SQLite via Room, persisting across app restarts and device reboots.
* **Duplicate Prevention**: Uses `@Insert(onConflict = OnConflictStrategy.REPLACE)` with a unique string ID. Repeated notifications with the same ID update existing items rather than creating duplicate entries.
* **Reactive UI**: Exposes `Flow<List<NotificationEntity>>` and `Flow<Int>` for zero-latency UI updates without polling.

---

## 🏗️ Architecture & Tech Stack

```
com.example.notificationinbox/
├── NotificationInboxApp.kt              # Application class (channel setup & dependency injection)
├── MainActivity.kt                      # Main Notification Inbox Activity
├── data/
│   ├── local/
│   │   ├── NotificationEntity.kt        # Room database entity
│   │   ├── NotificationDao.kt           # Room Data Access Object (Flow-based)
│   │   └── AppDatabase.kt               # Room database singleton
│   ├── model/
│   │   └── NotificationItem.kt          # Domain model mapping
│   └── repository/
│       └── NotificationRepository.kt    # Repository managing data access & Dispatchers.IO
├── ui/
│   ├── inbox/
│   │   ├── NotificationAdapter.kt       # ListAdapter with Glide & read/unread styling
│   │   └── NotificationDiffCallback.kt  # DiffUtil callback for smooth list animations
│   └── details/
│       └── NotificationDetailActivity.kt# Notification Details Activity (auto-marks read)
├── viewmodel/
│   ├── NotificationViewModel.kt         # ViewModel managing StateFlow streams & actions
│   └── NotificationViewModelFactory.kt  # ViewModelProvider.Factory implementation
├── service/
│   └── NotificationInboxFirebaseService.kt # FCM service for background/foreground pushes
└── utils/
    ├── Constants.kt                     # Notification channel ID ("assignment"), keys, URLs
    ├── DateUtils.kt                     # Relative ("5m ago", "Yesterday") & full date formatting
    └── NotificationHelper.kt            # Notification channel creation & system alerts
```

| Component | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 2.2+ |
| **Architecture** | MVVM (Model-View-ViewModel) + Repository Pattern |
| **UI Framework** | XML Layouts with ViewBinding (Material Design 3) |
| **Local Database** | Room Database (with KSP) |
| **Concurrency** | Kotlin Coroutines & Kotlin StateFlow / Flow |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Image Loading** | Bumptech Glide 4.16 |
| **Unit Testing** | JUnit 4, Robolectric, Coroutines Test, AndroidX Core Testing |

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug, Meerkat, Hedgehog, or newer
* **JDK**: Version 21 (recommended) or Version 17
* **Android SDK**: `minSdk = 24`, `targetSdk = 37`, `compileSdk = 37`

### 1. Build & Run
1. Open the project folder in Android Studio:
   ```bash
   File -> Open -> Select "NotificationInbox"
   ```
2. Let Gradle sync project dependencies.
3. Build the debug APK via terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   Or on Windows PowerShell:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. Run the app on an Android device or emulator.

---

## 🔥 Firebase Setup Instructions

To receive live push notifications from Firebase:

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create or select your project.
3. Add an Android App with the package name:
   ```
   com.example.notificationinbox
   ```
4. Download the `google-services.json` file.
5. Place `google-services.json` into the `app/` directory:
   ```
   NotificationInbox/app/google-services.json
   ```
6. Rebuild the app:
   ```bash
   ./gradlew assembleDebug
   ```
   *(The build script automatically detects `google-services.json` and activates the `com.google.gms.google-services` plugin).*

### FCM Payload Format
You can send notifications from the Firebase Console or via FCM HTTP v1 API.

**Example Data & Notification Payload:**
```json
{
  "message": {
    "topic": "all",
    "notification": {
      "title": "Special Offer",
      "body": "Enjoy 30% discount on your next purchase!",
      "image": "https://picsum.photos/600/300"
    },
    "data": {
      "id": "promo_2026_01",
      "title": "Special Offer",
      "description": "Enjoy 30% discount on your next purchase! Tap to view voucher code.",
      "image_url": "https://picsum.photos/600/300"
    },
    "android": {
      "notification": {
        "channel_id": "assignment"
      }
    }
  }
}
```

---

## 🧪 Testing & Verification

### Running Automated Unit Tests
A comprehensive test suite verifies Room operations, duplicate prevention, read/unread states, and date formatting:

```powershell
.\gradlew.bat testDebugUnitTest
```

### Verified Test Cases
* **`testInsertAndRetrieveNotification`**: Verifies inserting and fetching notifications from Room.
* **`testDuplicatePrevention`**: Verifies that sending/inserting a notification with an identical ID replaces/updates existing data and prevents duplicates.
* **`testReadStatusUpdate`**: Verifies marking an unread notification as read and updating the unread count.
* **`testMarkAllAsRead`**: Verifies batch marking all items as read.
* **`testQueryOrderingByTimestamp`**: Verifies newest notifications are ordered first.
* **`testDeleteNotification` & `testDeleteAllNotifications`**: Verifies single and bulk deletions.
* **`DateUtilsTest`**: Verifies relative time calculation ("Just now", "5m ago", "Yesterday").
* **`NotificationModelTest`**: Verifies entity to domain model mapping.

---

## 📦 Build Artifacts

The generated debug APK is located at:
```
app/build/outputs/apk/debug/app-debug.apk
```
