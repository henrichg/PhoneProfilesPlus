How to disable Wi-Fi scan throttling
====================================

##### In some devices not exists in Developer options parameter "Wi-Fi scan throttling". But is possible to disable it through your PC with installed adb.

If you do not have adb installed on your PC:
1. Download SDK Platform Tools from:

<https://developer.android.com/studio/releases/platform-tools.html>

2. Extract zip contents.

##### dislable Wi-Fi scan throttling:

1. On your device, go into Settings > About > Software Information and click on Build Number 7 times. This will unlock and display Developer Options in Settings.</string>
2. Go into Settings > Developer Options and enable USB Debugging.
3. On your PC, open a Command Prompt (Windows), or Terminal (Linux, OSX). Navigate to the folder where you extracted your adb files, and EXACTLY execute the following command:

`adb shell settings put global wifi_scan_throttle_enabled 0`
