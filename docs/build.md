How to build
============

It can be built PPP in Android Studio. However, certain requirements must be met.

1. Required is special version of android.jar in SDK platforms. 
   This special android.jar must be from https://github.com/Reginer/aosp-android-jar.
   Downlaod android.jar from its source code, from folder android-xx. xx must be value from dependencies.gradle, parameter compileSdk.
   Copy downloaded android.jar to folder of your SDK subforlder platforms/android-xx (replace android.jar in it).
2. Copy from templates folder file local.properties into root folder of PPP project.
   Then in it cofigure sdk.dir with correct directory of your Android SDK. 
3. Copy from templates folder file passwords_keys.gradle into root folder of PPP project.
   Then in it configure all parameters with values for your PPP version.
   In this file are parameters for encrition of some data in backup files and parameters for keystore of builded apk file.
   Note: Backups from original PPP will not be usable in your version of PPP. 



