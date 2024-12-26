How to configure airplane mode radios
=====================================

Is possible to configure, which radios are to be switched off when is enabled in device airplane mode.

If you do not have adb installed on your PC:
1. Download SDK Platform Tools from:

<https://developer.android.com/studio/releases/platform-tools.html>

2. Extract zip contents.

Configure your device:

1. On your device, go into Settings > About > Software Information and click on Build Number 7 times. This will unlock and display Developer Options in Settings.</string>
2. Go into Settings > Developer Options and enable USB Debugging.
   NOTE:
   For some devices must be in Developer options enabled:
   Xiaomi: "USB debugging (Security settings)".
   Oppo, OnePlus: "Disable permission monitoring".
   Maybe similar option exists in another devices.

##### To get, which radios are configured for airplane mode:

On your PC, open a Command Prompt (Windows), or Terminal (Linux, OSX). Navigate to the folder where you extracted your adb files, and EXACTLY execute the following command:

`adb shell settings get global airplane_mode_radios`

`adb shell content query --uri content://settings/global  --projection name:value --where "name=\'airplane_mode_toggleable_radios\'"`

You must allow in device, access to phone data for adb. In device will be displayed prompt for this.

In terminal will be dispalyed these results from commands:

`cell,bluetooth,nfc,wimax,wifi`

`Row: 0 name=airplane_mode_toggleable_radios, value=bluetooth,wifi,nfc`

##### To configure radios for airplane mode:

Supported are these radios:
- cell
- bluetooth
- nfc
- wimax
- wifi

Example commands are for these radios: cell,nfc,wimax:

On your PC, open a Command Prompt (Windows), or Terminal (Linux, OSX). Navigate to the folder where you extracted your adb files, and EXACTLY execute the following command:

`adb shell settings put global airplane_mode_radios  "cell,nfc,wimax"`

`adb shell content update --uri content://settings/global --bind value:s:'cell,nfc,wimax' --where "name=\'airplane_mode_radios\'`

And please, do reboot of device after change of this configuration.

### Source:
https://defkey.com/2020/12/15/keep-wi-fi-bluetooth-airplane-mode


