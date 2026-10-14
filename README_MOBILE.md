# Zenith '26 College Fest Management System — Native Android Application (Kotlin)

This document provides complete instructions for running the **Zenith '26 College Fest Management System** native Android application built with **Kotlin** and **Jetpack Compose**, integrated with the existing **Python FastAPI** backend and **SQLite** database.

---

## 🏗️ Architecture Overview

The project maintains the robust 3-layer architecture:

```
┌────────────────────────────────────────────────────────┐
│               Android Mobile App (Kotlin)              │
│   • Jetpack Compose Material3 UI matching Tailwind CSS │
│   • Retrofit 2 + OkHttp + Gson Networking Layer        │
│   • MVVM Architecture (StateFlow + ViewModels)         │
│   • Dynamic Server Base URL Configurator               │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP REST (JSON)
┌───────────────────────────▼────────────────────────────┐
│              Backend Server (Python + FastAPI)         │
│   • Existing REST Endpoints (/api/events, /api/stats)  │
│   • Server-side Validation & Business Logic            │
│   • Port 8080 (0.0.0.0)                                │
└───────────────────────────┬────────────────────────────┘
                            │ SQLite Driver
┌───────────────────────────▼────────────────────────────┐
│                 SQLite Database (fest.db)              │
│   • events & registrations tables                      │
└────────────────────────────────────────────────────────┘
```

---

## 📁 Project Directory Structure

```
Mobile app Activity 1/
├── android/                             <-- Native Android Mobile Project (Kotlin)
│   ├── app/
│   │   ├── build.gradle.kts             <-- App-level Gradle dependencies
│   │   ├── proguard-rules.pro
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml  <-- Internet permissions & cleartext config
│   │       │   ├── java/com/zenith26/fest/
│   │       │   │   ├── ZenithApp.kt     <-- Application class & API initialization
│   │       │   │   ├── MainActivity.kt  <-- Scaffold, TopBar, screen switching
│   │       │   │   ├── data/
│   │       │   │   │   ├── model/       <-- Event, Registration, Stats data models
│   │       │   │   │   ├── remote/      <-- Retrofit service, ApiClient, NetworkResult
│   │       │   │   │   └── repository/  <-- ZenithRepository
│   │       │   │   ├── ui/
│   │       │   │   │   ├── theme/       <-- Exact Tailwind CSS colors, typography, theme
│   │       │   │   │   ├── components/  <-- TopBar, HeroSection, EventCard, Chips, StateViews
│   │       │   │   │   ├── screens/
│   │       │   │   │   │   ├── student/ <-- StudentPortalScreen & EventDetailModal
│   │       │   │   │   │   └── admin/   <-- AdminDashboardScreen, AddEvent, DeleteDialog
│   │       │   │   │   └── viewmodel/   <-- StudentViewModel & AdminViewModel
│   │       │   └── res/
│   │       │       ├── values/          <-- colors.xml, strings.xml, themes.xml
│   │       │       ├── xml/             <-- network_security_config.xml
│   │       │       └── drawable/        <-- App launcher icons (⚡ Fest logo)
│   │       └── test/                    <-- Unit tests (ZenithUnitTest.kt)
│   ├── build.gradle.kts                 <-- Project root Gradle build script
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew / gradlew.bat            <-- Gradle wrapper scripts
│   └── gradle/wrapper/gradle-wrapper.properties
│
├── backend/                             <-- Preserved FastAPI Backend
│   ├── database.py
│   ├── main.py
│   ├── models.py
│   └── seed_data.py
├── frontend/                            <-- Preserved Web Frontend (HTML/JS)
│   ├── index.html
│   ├── admin.html
│   └── js/
├── data/
│   └── events_seed.xlsx
├── fest.db                              <-- SQLite Database
├── requirements.txt                     <-- Python dependencies
├── test_api.py                          <-- Automated API verification script
└── zenith26-api.postman_collection.json <-- Postman test collection
```

---

## 🚀 Step 1: Running the Python FastAPI Backend

1. Open PowerShell or Command Prompt in the project root:
   ```powershell
   cd "c:\Users\as701\OneDrive\Desktop\Mobile app Activity 1"
   ```

2. Activate the Python environment or run with Python on port 8080:
   ```powershell
   py -m uvicorn backend.main:app --host 0.0.0.0 --port 8080
   ```
   *(Binding to `0.0.0.0` allows connections from both the Android emulator and physical devices on the local Wi-Fi network).*

3. Verify the server is running by opening:
   - Web Student Portal: [http://localhost:8080](http://localhost:8080)
   - Web Admin Dashboard: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)
   - Interactive Swagger API Docs: [http://localhost:8080/docs](http://localhost:8080/docs)

---

## 📱 Step 2: Running the Android Application in Android Studio / Emulator

1. Open **Android Studio**.
2. Select **Open** and choose the `android` folder located at:
   `c:\Users\as701\OneDrive\Desktop\Mobile app Activity 1\android`
3. Android Studio will synchronize Gradle automatically.
4. Launch an Android Virtual Device (AVD) (e.g. Pixel 7 / 8 running Android 10+).
5. Click **Run** (Green play button ▶ or `Shift + F10`).
6. **Backend Connection on Emulator:**
   - The app defaults to `http://10.0.2.2:8080/`.
   - `10.0.2.2` is Android Emulator's loopback alias pointing to your dev computer's `localhost`.
   - The app will immediately load live fest events from the FastAPI backend!

---

## 📲 Step 3: Connecting a Physical Android Device to the Backend

1. Ensure your Android phone and dev computer are connected to the **same Wi-Fi network**.
2. Note your dev computer's local IP address:
   ```powershell
   ipconfig
   ```
   *(Your machine's local IP address is: `172.16.51.33`)*.
3. Open the **Zenith '26** app on your phone.
4. Tap the **⚙️ (Settings)** icon in the top-right corner of the app bar.
5. Tap the preset **"💻 Local Dev Machine (172.16.51.33:8080)"** or enter `http://<YOUR_IP>:8080/`.
6. Tap **Save & Apply**.
7. The app instantly reconnects to your computer's FastAPI backend over Wi-Fi!

---

## 🛠️ Step 4: Building the Android APK

To generate a standalone Debug APK via command line or Android Studio:

### Via Android Studio:
1. Go to menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2. Once complete, click **locate** to find `app-debug.apk` in `android/app/build/outputs/apk/debug/`.

### Via Gradle Command Line:
```powershell
cd "c:\Users\as701\OneDrive\Desktop\Mobile app Activity 1\android"
.\gradlew.bat assembleDebug
```
The output APK is generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

---

## 🎨 Features & Visual Fidelity Preservation

| Web Feature | Android Native Implementation |
|---|---|
| **Branding & Top Bar** | ⚡ gradient logo, "ZENITH '26" title, "Annual Fest" pill, "Student Portal" and "Admin Dashboard" switching tabs |
| **Hero Banner** | Dark slate-to-indigo gradient, Nov 14-15 schedule pill, "Ignite Your Passion. Unleash The Future.", 3 metrics counters |
| **Search & Filters** | Search input with instant debounce & clear button, 6 category filter chips (`All`, `Technical`, `Cultural`, `Sports`, `Gaming`, `Workshop`) |
| **Event Cards** | Category-colored badges, seat ratio / `FULL` badge, emoji container, title, description, date, time, fee (`Free` / `₹XX`), venue, `View Details →` button |
| **Event Details Modal** | Dark gradient header with 4 metric cards, about description, capacity progress bar, client & server error alerts, registration form |
| **Student Registration** | Validations (name, roll, email format), duplicate registration detection, `400`/`404`/`409` server error parsing, seat reduction on success |
| **Admin 4 Stat Cards** | Total Events, Total Registrations, Capacity Utilization with progress bar & ratio, Upcoming Events |
| **Category Breakdown** | 5 category metric cards with live counts and color badges |
| **Admin Events Table** | Live list sorted by utilization % descending, `FULL` (red) and `>90% ALMOST FULL` (amber) critical highlights |
| **Admin Registrations** | Filter by Event dropdown, list of student registrations with roll numbers & timestamps, Cancel registration modal |
| **Admin Add Event** | Full modal form to add events with live update on server |
