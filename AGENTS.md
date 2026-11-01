# ShowPic — AGENTS.md

## Project
Android app to browse and view pictures on an Amazon Echo Show (AEOCN).

- **Package**: `com.jocala.showpic`
- **Version**: 1.1 (versionCode 2)
- **Target device**: Echo Show, 480x960 landscape, Android 7.1.2 (API 25)
- **Min SDK**: 25 | **Target SDK**: 35 | **Compile SDK**: 35
- **Do not rename the package.** When the id moved to `com.jeff.showpic` and back
  (`792fa02`, 2026-08-15) the external boot helper kept pointing at the old id and
  silently stopped launching the app for ~2.5 months. Firmware-level references and
  `persist.sys.*` props outlive a rename

## Build system
- Gradle wrapper 8.7 • AGP 8.5.2 • Kotlin 1.9.24
- Only a **debug** build exists (`assembleDebug`); there is no release signing config, so
  the installed APK is `DEBUGGABLE` — fine for a sideloaded kiosk, not for distribution
- Build: `./gradlew assembleDebug`
- Install & run: `adb -s 192.168.1.136:5555 install -r app/build/outputs/apk/debug/app-debug.apk`
- Launch: `adb -s 192.168.1.136:5555 shell am start -n com.jocala.showpic/.MainActivity`

## Architecture
Two activities plus a boot receiver, no ViewModel, no fragments, no image loading library.

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

### BootReceiver (`app/.../BootReceiver.kt`)
- Handles `BOOT_COMPLETED` only; starts `MainActivity` with `goAsync()` + `Handler.postDelayed(8000)`
- Flags: `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP`
- The 8s delay exists because `com.amazon.ambienthome` is HOME and comes up during boot;
  firing immediately gets covered by it
- **Fire OS delivers `BOOT_COMPLETED` to non-priority apps ~2.5 minutes after boot**, so
  showpic appears around the 3-minute mark. A check at 60s is too early and looks like a
  failure — do not treat that as a regression
- Works because the device is API 25: the background-activity-start restriction is an
  API 29+ behaviour, so a boot receiver may start an activity here despite targetSdk 35
- Replaced the third-party helper `com.autostart`, which fired at boot but still targeted
  the pre-rename id `com.jeff.showpic` and silently launched nothing (uninstalled 2026-11-01)
- Manifest declares `RECEIVE_BOOT_COMPLETED` (install-time permission, no runtime prompt);
  `READ_EXTERNAL_STORAGE` is still requested at runtime by `MainActivity`
- **If showpic ever stops appearing at boot after a Fire OS OTA**, run
  `logcat -b events -d | grep showpic` *before* touching code:
  - `am_proc_start ... com.jocala.showpic/.BootReceiver` present → the broadcast arrived
    and the fault is our launch (delay too short, or `startActivity` refused)
  - absent → the platform stopped delivering `BOOT_COMPLETED` to this app; fall back to
    `cmd package set-home-activity com.jocala.showpic/.MainActivity`

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
- App starts itself at boot via `BootReceiver` (~3 min after power-on, see above); no external
  helper app is involved

## Dependencies (app/build.gradle.kts)
- `androidx.core:core-ktx:1.13.1`
- `androidx.appcompat:appcompat:1.7.0`

## ADB connection
- **Normal path**: Wi-Fi ADB at `192.168.1.136:5555` — wireless only 99% of the time, so
  `adb connect 192.168.1.136:5555` after every reboot of the device or the Mac
- **Debug path**: USB serial `G6G1MK061472065U`, used only when debugging (e.g. the boot
  receiver). Same physical device — confirm with `getprop ro.serialno`
- With both transports attached, every command needs `-s <transport>`, otherwise adb fails
  with "more than one device"
- Echo Show (Amazon AEOCN, device `cronos`, MT8163, 480x960 panel), Android 7.1.2 (API 25)
