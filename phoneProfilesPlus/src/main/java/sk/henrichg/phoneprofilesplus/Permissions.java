package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.*;

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
    private static final int PERMISSION_CUSTOM_PROFILE_ICON = 10;

    public static final String EXTRA_MERGED_PROFILE = "merged_profile";
    public static final String EXTRA_PERMISSION_TYPES = "permission_types";
    public static final String EXTRA_STARTUP_SOURCE = "startup_source";
    public static final String EXTRA_INTERACTIVE = "interactive";
    public static final String EXTRA_FOR_GUI = "for_gui";
    public static final String EXTRA_MONOCHROME = "monochrome";
    public static final String EXTRA_MONOCHROME_VALUE = "monochrome_value";
    public static final String EXTRA_EVENT_NOTIFICATION_SOUND = "event_notification_sound";
    public static final String EXTRA_LOG = "log";

    public static class PermissionType implements Parcelable {
        public int preference;
        public String permission;

        PermissionType (int preference, String permission) {
            this.preference = preference;
            this.permission = permission;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.preference);
            dest.writeString(this.permission);
        }

        protected PermissionType(Parcel in) {
            this.preference = in.readInt();
            this.permission = in.readString();
        }

        public static final Parcelable.Creator<PermissionType> CREATOR = new Parcelable.Creator<PermissionType>() {
            public PermissionType createFromParcel(Parcel source) {
                return new PermissionType(source);
            }

            public PermissionType[] newArray(int size) {
                return new PermissionType[size];
            }
        };
    }

    public static List<PermissionType> checkProfilePermissions(Context context, Profile profile) {
        List<PermissionType>  permissions = new ArrayList<PermissionType>();
        Log.e("Permissions", "checkProfilePermissions - profile.icon="+profile._icon);
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!checkProfileVolumePreferences(context, profile)) permissions.add(new PermissionType(PERMISSION_VOLUME_PREFERENCES, permission.WRITE_SETTINGS));
            if (!checkProfileVibrationOnTouch(context, profile)) permissions.add(new PermissionType(PERMISSION_VIBRATION_ON_TOUCH, permission.WRITE_SETTINGS));
            if (!checkProfileRingtones(context, profile)) permissions.add(new PermissionType(PERMISSION_RINGTONES, permission.WRITE_SETTINGS));
            if (!checkProfileScreenTimeout(context, profile)) permissions.add(new PermissionType(PERMISSION_SCREEN_TIMEOUT, permission.WRITE_SETTINGS));
            if (!checkProfileScreenBrightness(context, profile)) permissions.add(new PermissionType(PERMISSION_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
            if (!checkProfileAutoRotation(context, profile)) permissions.add(new PermissionType(PERMISSION_AUTOROTATION, permission.WRITE_SETTINGS));
            if (!checkProfileWallpaper(context, profile)) permissions.add(new PermissionType(PERMISSION_WALLPAPER, permission.READ_EXTERNAL_STORAGE));
            if (!checkProfileRadioPreferences(context, profile)) permissions.add(new PermissionType(PERMISSION_RADIO_PREFERENCES, permission.WRITE_SETTINGS));
            if (!checkProfilePhoneBroadcast(context, profile)) {
                permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.READ_PHONE_STATE));
                permissions.add(new PermissionType(PERMISSION_PHONE_BROADCAST, permission.PROCESS_OUTGOING_CALLS));
            }
            if (!checkCustomProfileIcon(context, profile)) permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));
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
            return (ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
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

    public static boolean checkScreenBrightness(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return Settings.System.canWrite(context);
        }
        else
            return true;
    }

    public static boolean checkProfileScreenBrightness(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile.getDeviceBrightnessChange()) {
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
            if (profile._deviceAutoRotate != 0) {
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
            if (profile._deviceWallpaperChange != 0) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkCustomProfileIcon(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            Profile _profile = dataWrapper.getDatabaseHandler().getProfile(profile._id, false);
            if (_profile == null) return true;
            if (!_profile.getIsIconResourceID()) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean checkGallery(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkImport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkExport(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23)
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
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
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        else
            return true;
    }

    public static boolean checkProfilePhoneBroadcast(Context context, Profile profile) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (profile._volumeSpeakerPhone != 0) {
                boolean granted = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                granted = granted && (ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED);
                return granted;
            }
            else
                return true;
        }
        else
            return true;
    }

    public static boolean grantProfilePermissionsAndActivate(Context context, Profile profile, boolean mergedProfile,
                                                             int startupSource, boolean interactive,
                                                             boolean forGUI, boolean monochrome, int monochromeValue,
                                                             String eventNotificationSound, boolean log) {
        List<PermissionType> permissions = checkProfilePermissions(context, profile);
        if (permissions.size() >= 0) {
            Intent intent = new Intent(context, GrantPermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(EXTRA_MERGED_PROFILE, mergedProfile);
            intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
            intent.putExtra(EXTRA_STARTUP_SOURCE, startupSource);
            intent.putExtra(EXTRA_INTERACTIVE, interactive);
            intent.putExtra(EXTRA_FOR_GUI, forGUI);
            intent.putExtra(EXTRA_MONOCHROME, monochrome);
            intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
            intent.putExtra(EXTRA_EVENT_NOTIFICATION_SOUND, eventNotificationSound);
            intent.putExtra(EXTRA_LOG, log);
            context.startActivity(intent);
        }
        return permissions.size() == 0;
    }

}
