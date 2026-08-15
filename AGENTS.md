# ShowPic — AGENTS.md

## Project
Android app to browse and view pictures on an Amazon Echo Show (AEOCN).

- **Package**: `com.jeff.showpic`
- **Target device**: Echo Show, 480x960 landscape, Android 7.1.2 (API 25)
- **Min SDK**: 25 | **Target SDK**: 35 | **Compile SDK**: 35

## Build system
- Gradle wrapper 8.7 • AGP 8.5.2 • Kotlin 1.9.24
- Build: `./gradlew assembleDebug`
- Install & run: `adb -s 192.168.1.136:5555 install -r app/build/outputs/apk/debug/app-debug.apk`
- Launch: `adb -s 192.168.1.136:5555 shell am start -n com.jeff.showpic/.MainActivity`

## Architecture
Two activities, no ViewModel, no fragments, no image loading library.

### MainActivity (`app/.../MainActivity.kt`)
- Requests `READ_EXTERNAL_STORAGE` permission at runtime
- Scans `/sdcard/Pictures/` for `*.jpg`, `*.jpeg`, `*.png`
- 3-column `GridView` with thumbnails (sampled at `inSampleSize=8`, 240px height)
- Tap → starts `ImageViewerActivity` with `image_path` extra
- Cancel / no permission / empty dir → `finishAffinity()` (exits app)

### ImageViewerActivity (`app/.../ImageViewerActivity.kt`)
- Decodes image with sampled bounds: `sampleSize = max(dim) / targetW`, target from `windowManager.defaultDisplay` (960x480 landscape)
- `fitCenter` `ImageView` on black background
- Tap anywhere → `finishAffinity()` (exits app)
- Immersive sticky mode (`SYSTEM_UI_FLAG_IMMERSIVE_STICKY | HIDE_NAVIGATION | FULLSCREEN`), restored on `onWindowFocusChanged`

## Themes
- `Theme.ShowPic` — `Theme.AppCompat.Light.DarkActionBar` (grid)
- `Theme.ShowPic.Viewer` — `Theme.AppCompat.NoActionBar`, fullscreen, no title, no content overlay

## Layouts
- `activity_main.xml` — `GridView` with 3 columns, 4dp spacing
- `activity_viewer.xml` — fill-parent `ImageView`, `scaleType="fitCenter"`, black bg

## Key behaviors
- Permission denied → Toast + `finishAffinity()`
- Directory missing / no images found → Toast + `finishAffinity()`
- Invalid image path in viewer → `finishAffinity()`
- Screen locked to landscape for both activities
- Large images are downsampled to avoid OOM (161MB allocation crash on 1600x900 images is fixed)
- Both activities set `FLAG_KEEP_SCREEN_ON` — screen never sleeps while app is foreground (Echo Show idle timeout was returning to launcher after 5 min)

## Dependencies (app/build.gradle.kts)
- `androidx.core:core-ktx:1.13.1`
- `androidx.appcompat:appcompat:1.7.0`

## ADB connection
- `adb connect 192.168.1.136`
- Echo Show (Amazon AEOCN), Android 7.1.2
