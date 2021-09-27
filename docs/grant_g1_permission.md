How to grant (G1) permission
============================

##### Profile parameters marked with (G1) need special permission for PhoneProfilesPlus. This is done through your PC with installed adb.

If you do not have adb installed on your PC:
1. Download SDK Platform Tools from:

`https://developer.android.com/studio/releases/platform-tools.html`

2. Extract zip contents.

##### How to grant WRITE_SECURE_SETTINGS permission:

1. On your device, go into Settings >About > Software Information and click on Build Number 7 times. This will unlock and display Developer Options in Settings.</string>
2. Go into Settings > Developer Options and enable USB Debugging.
3. On your PC, open a Command Prompt (Windows), or Terminal (Linux, OSX). Navigate to the folder where you extracted your adb files, and EXACTLY execute the following command:

`adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS`

4. After successful execution, all profile parameters marked with (G1) will be enabled.
