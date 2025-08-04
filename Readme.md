# AR Drill Assignment App

## 📱 Overview
An Android AR (Augmented Reality) application that allows users to select training drills and place virtual markers in real-world environments using ARCore technology.

## ✨ Features

### 🎯 Core Functionality
- **Drill Selection**: Choose from 3 different training drills (Drill 1, Drill 2, Drill 3)
- **Drill Details**: View comprehensive information including description, tips, difficulty level, and duration
- **AR Marker Placement**: Use AR to place drill markers on detected ground planes
- **Single Marker Mode**: Only one marker can be placed at a time (replaces previous marker)

### 🏗️ Technical Features
- **Modern UI**: Built with Jetpack Compose
- **Navigation**: Seamless navigation between screens
- **Permission Handling**: Proper camera permission management
- **ARCore Integration**: Real-time plane detection and object placement
- **Responsive Design**: Material Design 3 components

## 📋 Requirements Met

✅ **Basic UI - Drill Selector**
- Dropdown/list of 3 drills (Drill 1, Drill 2, Drill 3)
- Drill detail pages with dummy data, descriptions, and tips
- "Start AR Drill" button

✅ **AR Scene - Tap to Place Drill Object**
- Detects horizontal planes (floor)
- Places colored markers on tap
- Only allows one object at a time
- Real-time visual feedback

## 🚀 How to Run

### Prerequisites
- Android Studio Arctic Fox or later
- Android device with ARCore support (API level 24+)
- Camera permission

### Installation Steps

1. **Clone/Download the Project**
   ```bash
   git clone [your-repo-url]
   # OR download and extract ZIP file
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an existing project"
    - Navigate to the project folder

3. **Sync Dependencies**
    - Android Studio will automatically sync Gradle dependencies
    - Wait for the build to complete

4. **Connect Android Device**
    - Enable Developer Options and USB Debugging
    - Connect via USB or use wireless debugging
    - Ensure device supports ARCore

5. **Run the App**
    - Click the "Run" button (green play icon)
    - Select your connected device
    - Wait for installation and launch

### Alternative: Install APK
1. Build the APK: `Build > Build Bundle(s)/APK(s) > Build APK(s)`
2. Transfer APK to your Android device
3. Enable "Install from Unknown Sources" in device settings
4. Install and run the APK

## 📱 App Usage

### Step 1: Select Drill
- Launch the app
- Browse the list of available drills
- Tap on any drill card to view details

### Step 2: View Drill Details
- Read the drill description and tips
- Check difficulty level and duration
- Tap "Start AR Drill" button

### Step 3: AR Session
- Grant camera permission when prompted
- Point camera at a flat surface (floor/table)
- Wait for plane detection (white grid appears)
- Tap on detected surface to place marker
- Marker will appear as a colored cone/cube

### Step 4: Marker Management
- Only one marker exists at a time
- Tap elsewhere to move the marker
- Use back button to return to drill selection

## 🏗️ Project Structure

```
app/src/main/java/com/devsneha/ar/
├── MainActivity.kt              # Main navigation activity
├── ARActivity.kt               # Alternative simplified AR activity
├── data/
│   └── DrillData.kt            # Drill data models and repository
├── ui/
│   ├── screens/
│   │   ├── DrillSelectionScreen.kt    # Drill selection UI
│   │   ├── DrillDetailScreen.kt       # Drill detail UI
│   │   └── ARSessionScreen.kt         # AR session UI
│   └── theme/
│       ├── Theme.kt            # App theme
│       └── Type.kt             # Typography
└── ARSessionScreen.kt          # Complete AR implementation
```

## 🛠️ Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **AR Framework**: Google ARCore
- **Architecture**: MVVM with Compose
- **Navigation**: Navigation Compose
- **Build System**: Gradle

## 📋 Supported Devices

### ARCore Supported Devices
- Most modern Android devices (2017+)
- Minimum Android API level 24
- Devices with rear camera and motion sensors

### Testing Recommendations
- Google Pixel series
- Samsung Galaxy S8+
- OnePlus 5+
- Huawei P20+

## 🔍 Key Implementation Details

### AR Features
- **Plane Detection**: Detects horizontal surfaces automatically
- **Hit Testing**: Accurate tap-to-place functionality
- **Anchor Management**: Proper AR anchor lifecycle
- **Visual Feedback**: Real-time plane visualization

### UI/UX Features
- **Material Design 3**: Modern, accessible interface
- **Smooth Navigation**: Seamless screen transitions
- **Permission Handling**: User-friendly permission requests
- **Error Handling**: Graceful error management

