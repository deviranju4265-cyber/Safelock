# SafeLock — Native Android Student Application

SafeLock is the student-side native Android application within a two-application parental-control system, designed to work seamlessly with **Lock Monitor** (the parent application) through a unified Google Cloud Firestore & Firebase Authentication backend.

---

## 1. System Architecture & Features

### Core Capabilities
1. **Automatic Student ID Generation**:
   - Unique identifier in `SL-XXXXX` format (e.g., `SL-A7K92`).
   - Generated automatically using cryptographically secure randomization.
   - Dedicated "Copy ID" affordance with instant clipboard feedback.
   - Distinct from authentication credentials; preserved across device reboots and app restarts in local DataStore.
2. **Secure Parent-Student Pairing**:
   - Parent submits a pairing request from Lock Monitor using the Student ID.
   - Student receives a real-time invitation card and can explicitly Approve or Reject.
   - Authorized relationships are permanently synchronized in Cloud Firestore.
   - Supports multiple independent student relationships per parent.
3. **Approved Application Management**:
   - Live query of installed launcher activities on the device.
   - Parent-approved package allowlist.
   - Native Intent launching (`getLaunchIntentForPackage`) with flags `FLAG_ACTIVITY_NEW_TASK`.
   - Handles uninstalled or missing applications gracefully.
   - **Emergency Phone Dialer is always accessible** (`tel:`) under all enforcement states.
4. **Schedule & Restriction Engine**:
   - Multi-day, multi-period schedule evaluation (supporting school periods and overnight bedtime periods).
   - Timezone and device clock resilience.
   - Automatic reboot recovery via `BootCompletedReceiver`.
5. **Student-Controlled Wallpaper**:
   - Built-in theme presets: Cyber Slate, Deep Nebula, Emerald Matrix, Solar Twilight.
   - Custom photo selection using the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
   - Zero storage permission requirement (`READ_EXTERNAL_STORAGE` is NOT requested).
   - Saved locally and preserved across restarts. Parents cannot override wallpaper.
6. **Student Requests & Temporary Emergency Unlock**:
   - Application permission requests.
   - Schedule modification requests.
   - Emergency unlock requests (5 min, 15 min, 30 min, or custom).
   - Real-time status tracking (Pending, Approved, Rejected) with parent decision notes.
7. **Native Android Device Management**:
   - Supported Lock Task Mode (`startLockTask()` / `stopLockTask()`).
   - Device Administration via `SafeLockDeviceAdminReceiver` and `DevicePolicyManager`.
   - Fully Managed / Device Owner enrollment support for kiosks or school deployments.
   - Strict adherence to official Android APIs without accessibility service abuse or system exploits.

---

## 2. Cloud Backend Contract (Firestore Data Model)

Both SafeLock and Lock Monitor communicate via the same Firestore database instance (`ai-studio-android-safelock-f10d1447-9c12-422c-96ef-5a9c5e2496e9`).

| Collection | Document ID | Key Fields | Description |
| :--- | :--- | :--- | :--- |
| `/students/{studentId}` | `SL-XXXXX` | `userId`, `studentName`, `pairingStatus`, `parentUids`, `activeScheduleId`, `isLockedManually`, `emergencyUnlockUntil` | Student device profile & status |
| `/pairing_requests/{requestId}` | `req_xxx` | `studentId`, `parentId`, `parentName`, `status` (`pending`, `approved`, `rejected`), `expiresAt` | Parent-to-student pairing invitations |
| `/schedules/{scheduleId}` | `sch_xxx` | `studentId`, `name`, `daysOfWeek`, `periodsJson`, `isApproved`, `status` | Usage schedules |
| `/approved_apps/{studentId}` | `SL-XXXXX` | `packages`, `appNames`, `updatedBy`, `updatedAt` | Parent-controlled application allowlist |
| `/student_requests/{requestId}` | `req_xxx` | `studentId`, `userId`, `type` (`emergency_unlock`, `app_permission`, `schedule_change`), `details`, `status` | Student permission & unlock requests |
| `/device_commands/{commandId}` | `cmd_xxx` | `studentId`, `parentId`, `commandType` (`lock_now`, `unlock`, `emergency_unlock`), `status` (`pending`, `applied`, `failed`) | Remote parent commands |

---

## 3. Supported Device Management & Setup

### A. Device Administrator Mode
1. Open the SafeLock app.
2. Tap the **Protection / Setup Admin** button on the bottom navigation bar.
3. Tap **Enable Device Administrator**.
4. Confirm system permissions for screen lock control and policy monitoring.

### B. Device Owner (Kiosk / School Managed Device)
For complete tamper-resistant lockdown without exit escapes:
```bash
adb shell dpm set-device-owner com.aistudio.safelock.krvpxt/.receiver.SafeLockDeviceAdminReceiver
```
*(Requires device to be provisioned before adding user accounts).*

---

## 4. Building & Running Unit Tests

### Build Debug APK
```bash
gradle assembleDebug
```

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### Validate Firestore Security Rules
```bash
FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
```

---

## 5. Known Limitations & Safety Guarantees

1. **Emergency Calling Accessibility**: The emergency phone dialer is permanently accessible regardless of schedule, manual lock, or lock-task mode.
2. **Supported Android Boundaries**: In standard non-device-owner user mode, Android OS design allows users to unpin screens via physical button combinations (`Back + Overview`). For strict uninstallation prevention and kiosk lock-task enforcement, the app must be enrolled as Device Owner.
3. **No Storage Permission Overreach**: Gallery wallpapers are selected strictly via the zero-permission Android Photo Picker. Images remain private to the device and are never uploaded to the cloud.
