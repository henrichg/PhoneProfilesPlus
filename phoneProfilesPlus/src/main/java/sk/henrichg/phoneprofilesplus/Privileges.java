package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

public class Privileges {

    /////// Profile privileges
    // PREF_PROFILE_VOLUME_RINGER_MODE
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - System.Settings -> "vibrate_when_ringing"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_ZEN_MODE
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - NotificationListenerService
    // PREF_PROFILE_VOLUME_RINGTONE
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_NOTIFICATION
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_MEDIA
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_ALARM
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_SYSTEM
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_VOLUME_VOICE
    //  - System.Settings -> "notifications_use_ring_volume"
    //  - AudioManager
    // PREF_PROFILE_SOUND_RINGTONE_CHANGE
    //  - none
    // PREF_PROFILE_SOUND_RINGTONE
    //  - RingtoneManager
    // PREF_PROFILE_SOUND_NOTIFICATION_CHANGE
    //  - none
    // PREF_PROFILE_SOUND_NOTIFICATION
    //  - RingtoneManager
    // PREF_PROFILE_SOUND_ALARM_CHANGE
    //  - none
    // PREF_PROFILE_SOUND_ALARM
    //  - RingtoneManager
    // PREF_PROFILE_DEVICE_AIRPLANE_MODE
    //  - PPHelper
    // PREF_PROFILE_DEVICE_WIFI
    //  - WifiManager
    // PREF_PROFILE_DEVICE_BLUETOOTH
    //  - BluetoothAdapter
    // PREF_PROFILE_DEVICE_SCREEN_TIMEOUT
    //  - Settings.System
    // PREF_PROFILE_DEVICE_BRIGHTNESS
    //  - Settings.System
    // PREF_PROFILE_DEVICE_WALLPAPER_CHANGE
    //  - none
    // PREF_PROFILE_DEVICE_WALLPAPER
    //  - WallpaperManager
    // PREF_PROFILE_DEVICE_MOBILE_DATA
    //  - PPHelper
    // PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS
    //  - start intent
    // PREF_PROFILE_DEVICE_GPS
    //  - ROOT
    // PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE
    //  - none
    // PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME
    //  - start intent
    // PREF_PROFILE_DEVICE_AUTOSYNC
    //  - ContentResolver
    // PREF_PROFILE_DEVICE_AUTOROTATE
    //  - Settings.System
    // PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS
    //  - start intent
    // PREF_PROFILE_VOLUME_SPEAKER_PHONE
    //  - AudioManager
    // PREF_PROFILE_DEVICE_NFC
    //  - PPHelper
    // PREF_PROFILE_DEVICE_KEYGUARD
    //  - KeyguardManager
    // PREF_PROFILE_VIBRATION_ON_TOUCH
    //  - Settings.System
    // PREF_PROFILE_DEVICE_WIFI_AP
    //  - WifiManager

    public static boolean checkProfilePrivileges(Context context, Profile profile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return true;
        }
        else
            return true;
    }

    public static boolean checkProfileVolumePreferences(Context context, Profile profile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile._volumeRingerMode != 0) ||
                    profile.getVolumeRingtoneChange() ||
                    profile.getVolumeNotificationChange() ||
                    profile.getVolumeMediaChange() ||
                    profile.getVolumeAlarmChange() ||
                    profile.getVolumeSystemChange() ||
                    profile.getVolumeVoiceChange()) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }


    public static boolean checkSavedRingerMode(Context context, Profile profile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int ringerMode = GlobalData.getRingerMode(context);
            if (ringerMode != 0)
                return Settings.System.canWrite(context);
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkSavedVolumes(Context context, Profile profile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int ringerVolume = GlobalData.getRingerVolume(context);
            int notificationVolume = GlobalData.getNotificationVolume(context);
            if ((ringerVolume != -999) ||
                (notificationVolume != -999)) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkInstallTone(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

}
