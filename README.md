# 🚨 RoadSOS — Automatic Accident Detection & Emergency Response System

RoadSOS is an Android application that automatically detects vehicular accidents in real-time using device sensors, instantly alerts emergency contacts via SMS, shares live location, and helps locate the nearest hospitals — all without the user needing to do anything.

---

## 🔥 What It Does

- **Automatic crash detection** using accelerometer + gyroscope fusion
- **ML-based severity classification** — Minor, Medium, Severe
- **Instant SMS alerts** to saved emergency contacts with live location
- **Nearest hospital finder** using Google Maps & Places API
- **"Are you safe?" countdown** — auto-triggers SOS if no response in time
- **Firebase backend** — contacts, profile, and accident history synced to cloud
- **Works in background** — survives app close, screen lock, and device reboot

---

## 👥 Team

| Member | Role | Responsibility |
|--------|------|----------------|
| Raj Singh | Sensor & AI Logic Lead | Accident detection engine, gyroscope + accelerometer fusion, severity logic |
| Aditya Pareek | Android Frontend Lead | UI screens, navigation, UX design |
| Alok Maurya | Maps & Emergency System Lead | GPS tracking, nearby hospitals, SMS dispatch |
| Satish Yadav | Firebase & Backend Lead | Firestore, authentication, cloud data sync |
| Lakshya Singh | Integration & Presentation Lead | Module integration, testing, documentation, demo |

---


## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | XML Layouts, Material Design |
| Sensors | Android SensorManager (Accelerometer, Gyroscope) |
| Location | Google Maps SDK, Places API, FusedLocationProvider |
| Backend | Firebase Firestore, Firebase Auth |
| Messaging | Android SMS Manager |
| Background | Android Foreground Services, BroadcastReceiver |
| Build | Gradle, Android Studio |

---

## 🚀 Setup

1. Clone the repo
```bash
git clone https://github.com/Adi-Pareek/RoadSoS.git
cd RoadSoS
```

2. Add your `google-services.json` from Firebase Console into `/app`

3. Add your Google Maps API key in `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_KEY_HERE" />
```

4. Open in Android Studio → **Build → Run**

---

## 📱 Required Permissions

```
ACCESS_FINE_LOCATION
ACCESS_BACKGROUND_LOCATION
FOREGROUND_SERVICE_LOCATION
SEND_SMS
CALL_PHONE
POST_NOTIFICATIONS
RECEIVE_BOOT_COMPLETED
WAKE_LOCK
```

---

## 📂 Project Structure

```
RoadSoS/
├── detection/
│   ├── AccidentDetector.kt
│   ├── AccidentBroadcaster.kt
│   └── CrashEvent.kt
├── service/
│   ├── SensorService.kt
│   └── LocationService.kt
├── receivers/
│   ├── AccidentReceiver.kt
│   └── BootReceiver.kt
├── DashboardActivity.kt
├── SOSActivity.kt
├── ContactsActivity.kt
├── HistoryActivity.kt
├── EmergencyMapActivity.kt
└── SettingsActivity.kt
```

---

## ⚠️ Disclaimer

RoadSOS is a hackathon prototype. It is not a certified medical or emergency device. Always call emergency services directly in a life-threatening situation.

---

*Built for IIT Madras Hackathon 2026*
