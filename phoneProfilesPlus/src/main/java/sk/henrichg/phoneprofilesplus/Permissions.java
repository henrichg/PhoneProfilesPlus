package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

public class Permissions {

    /////// Profile permissions
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

    private static final int PERMISSION_VOLUME_PREFERENCES = 1;
    private static final int PERMISSION_VIBRATION_ON_TOUCH = 2;
    private static final int PERMISSION_RINGTONES = 3;
    private static final int PERMISSION_SCREEN_TIMEOUT = 4;
    private static final int PERMISSION_SCREEN_BRIGHTNESS = 5;
    private static final int PERMISSION_AUTOROTATION = 6;
    private static final int PERMISSION_WALLPAPER = 7;
    private static final int PERMISSION_RADIO_PREFERENCES = 8;
    private static final int PERMISSION_PHONE_BROADCAST = 9;

    public static List<Integer> checkProfilePermissions(Context context, Profile profile) {
        List<Integer>  permissions = new ArrayList<Integer>();
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!checkProfileVolumePreferences(context, profile)) permissions.add(PERMISSION_VOLUME_PREFERENCES);
            if (!checkProfileVibrationOnTouch(context, profile)) permissions.add(PERMISSION_VIBRATION_ON_TOUCH);
            if (!checkProfileRingtones(context, profile)) permissions.add(PERMISSION_RINGTONES);
            if (!checkProfileScreenTimeout(context, profile)) permissions.add(PERMISSION_SCREEN_TIMEOUT);
            if (!checkProfileScreenBrightness(context, profile)) permissions.add(PERMISSION_SCREEN_BRIGHTNESS);
            if (!checkProfileAutoRotation(context, profile)) permissions.add(PERMISSION_AUTOROTATION);
            if (!checkProfileWallpaper(context, profile)) permissions.add(PERMISSION_WALLPAPER);
            if (!checkProfileRadioPreferences(context, profile)) permissions.add(PERMISSION_RADIO_PREFERENCES);
            if (!checkProfilePhoneBroadcast(context, profile)) permissions.add(PERMISSION_PHONE_BROADCAST);
            return permissions;
        }
        else
            return permissions;
    }

    public static boolean checkProfileVolumePreferences(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0); -- NOT WORKING
            if ((profile._volumeRingerMode != 0) ||
                    profile.getVolumeRingtoneChange() ||
                    profile.getVolumeNotificationChange() ||
                    profile.getVolumeMediaChange() ||
                    profile.getVolumeAlarmChange() ||
                    profile.getVolumeSystemChange() ||
                    profile.getVolumeVoiceChange()) {
                //return Settings.System.canWrite(context);
                return true;
            }
            else
                return true;
        }
        else
            return true;
    }


    public static boolean checkSavedProfileRingerMode(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0); -- NOT WORKING
            int ringerMode = GlobalData.getRingerMode(context);
            if (ringerMode != 0)
                //return Settings.System.canWrite(context);
                return true;
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkSavedProfileVolumes(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0); -- NOT WORKING
            int ringerVolume = GlobalData.getRingerVolume(context);
            int notificationVolume = GlobalData.getNotificationVolume(context);
            if ((ringerVolume != -999) ||
                    (notificationVolume != -999)) {
                //return Settings.System.canWrite(context);
                return true;
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

    public static boolean checkProfileVibrationOnTouch(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._vibrationOnTouch != 0) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileRingtones(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile._soundRingtoneChange != 0) ||
                    (profile._soundNotificationChange != 0) ||
                    (profile._soundAlarmChange != 0)) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileScreenTimeout(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._deviceScreenTimeout != 0) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileScreenBrightness(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile == null) || profile.getDeviceBrightnessChange()) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileAutoRotation(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile == null) || profile._deviceAutoRotate != 0) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkProfileWallpaper(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile == null) || profile._deviceWallpaperChange != 0) {
                return (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkCustomProfileIcon(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkGallery(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkImport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkExport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkProfileRadioPreferences(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if ((profile._deviceWiFiAP != 0)) {
                return Settings.System.canWrite(context);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkPPHelperInstall(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkProfilePhoneBroadcast(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._volumeSpeakerPhone != 0) {
                boolean granted = (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                granted = granted && (context.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean grantProfilePermissions(Context context, Profile profile, boolean interactive) {
        List<Integer> permissions = checkProfilePermissions(context, profile);
        if (permissions.size() > 0) {
            //if (activity.shouldShowRequestPermissionRationale()) {

            //}

        }
        return permissions.size() == 0;
    }

}
