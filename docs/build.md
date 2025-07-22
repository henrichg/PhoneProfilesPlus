How to build
============

PPP can be build in Android Studio. However, certain requirements must be met.

1. Required is special version of android.jar in SDK platforms.\n 
   This special android.jar must be from https://github.com/Reginer/aosp-android-jar.\n
   Downlaod android.jar from its source code, from folder android-xx. xx must be value from dependencies.gradle, parameter compileSdk.\n
   Copy downloaded android.jar to folder of your SDK subforlder platforms/android-xx (rename original android.jar to android_orig.jar and then copy downloaded version).
2. Copy from PPP project from "templates" folder file local.properties into root folder of PPP project.
   Then in it cofigure sdk.dir with correct directory of your Android SDK. 
3. Copy from PPP project from "templates" folder file passwords_keys.gradle into root folder of PPP project.
   Then in it configure all parameters with values for your PPP version.
   This file contains parameters for encrypting data in PPP backup files and parameters for the keystore of the APK build.
   Note: Backups from original PPP will not be usable in your version of PPP.



