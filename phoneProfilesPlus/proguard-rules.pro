# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/demollol/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# TabTargetView
-keep class android.support.v7.widget.Toolbar { *** mMenuView; }
-keep class android.support.v7.widget.ActionMenuView { *** mPresenter; }
-keep class android.support.v7.widget.ActionMenuPresenter { *** mOverflowButton; }

#android-job
#-dontwarn com.evernote.android.job.gcm.**
#-dontwarn com.evernote.android.job.GcmAvailableHelper
#
#-keep public class com.evernote.android.job.v21.PlatformJobService
#-keep public class com.evernote.android.job.v14.PlatformAlarmService
#-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
#-keep public class com.evernote.android.job.JobBootReceiver
#-keep public class com.evernote.android.job.JobRescheduleService
