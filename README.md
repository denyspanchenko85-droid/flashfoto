# Project: flashfoto

## Project Status
Updated: 2026-03-24 09:59

## File Structure
```
.
├── README.md
├── app
│   ├── build.gradle.kts
│   └── src
│       └── main
│           ├── 1024.png
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── shadow
│           │           └── flashfoto
│           │               ├── Bootstrapper.kt
│           │               ├── CameraHandler.kt
│           │               ├── CompositionManager.kt
│           │               ├── FileUtils.kt
│           │               ├── GalleryManager.kt
│           │               ├── HistoryManager.kt
│           │               ├── ImageDisplayHelper.kt
│           │               ├── ImageOverlayProcessor.kt
│           │               ├── InteractionManager.kt
│           │               ├── Logger.kt
│           │               ├── MainActivity.kt
│           │               ├── ManualPrinterHandler.kt
│           │               ├── PrintManager.kt
│           │               ├── PrinterDialogHandler.kt
│           │               ├── PrinterListRenderer.kt
│           │               ├── PrinterManager.kt
│           │               ├── PrinterModel.kt
│           │               ├── SettingsDialogHandler.kt
│           │               ├── SettingsManager.kt
│           │               ├── ShareManager.kt
│           │               ├── WifiDirectLifecycleHelper.kt
│           │               ├── WifiDirectManager.kt
│           │               ├── WifiDirectReceiver.kt
│           │               ├── WifiDiscoveryHandler.kt
│           │               └── WorkflowManager.kt
│           ├── play_store_512.png
│           └── res
│               ├── drawable
│               │   ├── easter_horiz_1.png
│               │   └── easter_vert_1.png
│               ├── layout
│               │   └── activity_main.xml
│               ├── mipmap-anydpi-v26
│               │   └── ic_launcher.xml
│               ├── mipmap-hdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_adaptive_back.png
│               │   └── ic_launcher_adaptive_fore.png
│               ├── mipmap-mdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_adaptive_back.png
│               │   └── ic_launcher_adaptive_fore.png
│               ├── mipmap-xhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_adaptive_back.png
│               │   └── ic_launcher_adaptive_fore.png
│               ├── mipmap-xxhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_adaptive_back.png
│               │   └── ic_launcher_adaptive_fore.png
│               ├── mipmap-xxxhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_adaptive_back.png
│               │   └── ic_launcher_adaptive_fore.png
│               ├── values
│               │   └── themes.xml
│               └── xml
│                   └── file_paths.xml
├── build.gradle.kts
├── gradle
│   └── wrapper
│       └── gradle-wrapper.properties
├── gradle.properties
└── settings.gradle.kts

21 directories, 55 files
```

## Logical Map (Auto-generated)
- MainActivity.kt: [Main entry point with Material 3 Dynamic Colors support]
- PrinterDialogHandler.kt: [Main orchestrator for printer management dialog]
- WifiDiscoveryHandler.kt: [Universal Wi-Fi P2P discovery handler (Android 10 to 14+)]
- PrinterListRenderer.kt: [UI generation for the printer list entries]
- PrintManager.kt: [Logic for socket printing, system dialog, and file decoding]
- PrinterManager.kt: [Persistent storage and management of printer list]
- PrinterModel.kt: [Atomic data structure for printer entity]

## Next Steps
- [x] Phase 1: Overlay Foundation (Stabilized)
- [ ] Phase 2: Interaction & PetBrain (Current Goal)
