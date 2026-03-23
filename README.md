# Project: flashfoto

## Project Status
Updated: 2026-03-23 18:24

## File Structure
```
.
├── README.md
├── app
│   ├── build.gradle.kts
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── shadow
│           │           └── flashfoto
│           │               ├── Bootstrapper.kt
│           │               ├── CameraHandler.kt
│           │               ├── FileUtils.kt
│           │               ├── GalleryManager.kt
│           │               ├── HistoryManager.kt
│           │               ├── ImageDisplayHelper.kt
│           │               ├── ImageOverlayProcessor.kt
│           │               ├── InteractionManager.kt
│           │               ├── Logger.kt
│           │               ├── MainActivity.kt
│           │               ├── PrintManager.kt
│           │               ├── SettingsDialogHandler.kt
│           │               ├── SettingsManager.kt
│           │               └── WorkflowManager.kt
│           └── res
│               ├── drawable
│               │   ├── easter_horiz_1.png
│               │   └── easter_vert_1.png
│               ├── layout
│               │   └── activity_main.xml
│               └── xml
│                   └── file_paths.xml
├── build.gradle.kts
├── gradle
│   └── wrapper
│       └── gradle-wrapper.properties
├── gradle.properties
└── settings.gradle.kts

14 directories, 25 files
```

## Logical Map (Auto-generated)
- PrintManager.kt: [Logic for socket printing, system dialog, and file decoding]

## Next Steps
- [x] Phase 1: Overlay Foundation (Stabilized)
- [ ] Phase 2: Interaction & PetBrain (Current Goal)
