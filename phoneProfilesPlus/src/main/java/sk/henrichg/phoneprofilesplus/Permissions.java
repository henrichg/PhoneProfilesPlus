package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission;

class Permissions {

    //private static final int PERMISSION_PROFILE_VOLUME_PREFERENCES = 1;
    private static final int PERMISSION_PROFILE_VIBRATION_ON_TOUCH = 2;
    private static final int PERMISSION_PROFILE_RINGTONES = 3;
    private static final int PERMISSION_PROFILE_SCREEN_TIMEOUT = 4;
    private static final int PERMISSION_PROFILE_SCREEN_BRIGHTNESS = 5;
    private static final int PERMISSION_PROFILE_AUTOROTATION = 6;
    private static final int PERMISSION_PROFILE_WALLPAPER = 7;
    private static final int PERMISSION_PROFILE_RADIO_PREFERENCES = 8;
    private static final int PERMISSION_PROFILE_PHONE_BROADCAST = 9;
    private static final int PERMISSION_PROFILE_CUSTOM_PROFILE_ICON = 10;
    private static final int PERMISSION_INSTALL_TONE = 11;
    private static final int PERMISSION_EXPORT = 12;
    private static final int PERMISSION_IMPORT = 13;
    private static final int PERMISSION_EVENT_CALENDAR_PREFERENCES = 15;
    private static final int PERMISSION_EVENT_CALL_PREFERENCES = 16;
    private static final int PERMISSION_EVENT_SMS_PREFERENCES = 17;
    private static final int PERMISSION_EVENT_LOCATION_PREFERENCES = 18;
    private static final int PERMISSION_EVENT_CONTACTS = 19;
    private static final int PERMISSION_PROFILE_NOTIFICATION_LED = 20;
    private static final int PERMISSION_PROFILE_VIBRATE_WHEN_RINGING = 21;
    private static final int PERMISSION_PLAY_RINGTONE_NOTIFICATION = 22;
    private static final int PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY = 23;
    private static final int PERMISSION_PROFILE_LOCK_DEVICE = 24;
    private static final int PERMISSION_RINGTONE_PREFERENCE = 25;
    private static final int PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING = 26;
    private static final int PERMISSION_PROFILE_SOUND_ON_TOUCH = 27;

    static final int GRANT_TYPE_PROFILE = 1;
    static final int GRANT_TYPE_INSTALL_TONE = 2;
    static final int GRANT_TYPE_WALLPAPER = 3;
    static final int GRANT_TYPE_CUSTOM_PROFILE_ICON = 4;
    static final int GRANT_TYPE_EXPORT = 5;
    static final int GRANT_TYPE_IMPORT = 6;
    static final int GRANT_TYPE_EVENT = 8;
    static final int GRANT_TYPE_WIFI_BT_SCAN_DIALOG = 9;
    static final int GRANT_TYPE_CALENDAR_DIALOG = 10;
    static final int GRANT_TYPE_CONTACT_DIALOG = 11;
    static final int GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY = 12;
    static final int GRANT_TYPE_BRIGHTNESS_DIALOG = 13;
    static final int GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION = 14;
    static final int GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG = 15;
    static final int GRANT_TYPE_RINGTONE_PREFERENCE = 16;

    static final String EXTRA_GRANT_TYPE = "grant_type";
    static final String EXTRA_MERGED_PROFILE = "merged_profile";
    static final String EXTRA_PERMISSION_TYPES = "permission_types";
    static final String EXTRA_ONLY_NOTIFICATION = "only_notification";
    static final String EXTRA_FORCE_GRANT = "force_grant";
    //static final String EXTRA_FOR_GUI = "for_gui";
    //static final String EXTRA_MONOCHROME = "monochrome";
    //static final String EXTRA_MONOCHROME_VALUE = "monochrome_value";
    //static final String EXTRA_INTERACTIVE = "interactive";
    static final String EXTRA_APPLICATION_DATA_PATH = "application_data_path";
    static final String EXTRA_ACTIVATE_PROFILE = "activate_profile";
    static final String EXTRA_GRANT_ALSO_CONTACTS = "grant_also_contacts";

    static Activity profileActivationActivity = null;
    static WallpaperViewPreference wallpaperViewPreference = null;
    static ProfileIconPreference profileIconPreference = null;
    static EditorProfilesActivity editorActivity = null;
    static WifiSSIDPreference wifiSSIDPreference = null;
    static BluetoothNamePreference bluetoothNamePreference = null;
    static CalendarsMultiSelectDialogPreference calendarsMultiSelectDialogPreference = null;
    static ContactsMultiSelectDialogPreference contactsMultiSelectDialogPreference = null;
    static ContactGroupsMultiSelectDialogPreference contactGroupsMultiSelectDialogPreference = null;
    static LocationGeofenceEditorActivity locationGeofenceEditorActivity = null;
    static BrightnessDialogPreference brightnessDialogPreference = null;
    static MobileCellsPreference mobileCellsPreference = null;
    static RingtonePreference ringtonePreference = null;

    private static final String PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION = "show_request_write_settings_permission";
    private static final String PREF_MERGED_PERMISSIONS = "merged_permissions";
    private static final String PREF_MERGED_PERMISSIONS_COUNT = "merged_permissions_count";
    private static final String PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION = "show_request_access_notification_policy_permission";
    private static final String PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION = "show_request_draw_overlays_permission";

    private static final String PREF_PERMISSIONS_CHANGED = "permissionsChanged";

    // permission groups
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSION = "writeSystemSettingsPermission";
    private static final String PREF_NOTIFICATION_POLICY_PERMISSION = "notificationPolicyPermission";
    private static final String PREF_DRAW_OVERLAY_PERMISSION= "drawOverlayPermission";
    private static final String PREF_CALENDAR_PERMISSION = "calendarPermission";
    private static final String PREF_CAMERA_PERMISSION = "cameraPermission";
    private static final String PREF_CONTACTS_PERMISSION = "contactsPermission";
    private static final String PREF_LOCATION_PERMISSION = "locationPermission";
    private static final String PREF_MICROPHONE_PERMISSION = "microphonePermission";
    private static final String PREF_PHONE_PERMISSION = "phonePermission";
    private static final String PREF_SENSORS_PERMISSION = "sensorsPermission";
    private static final String PREF_SMS_PERMISSION = "smsPermission";
    private static final String PREF_STORAGE_PERMISSION = "storagePermission";

    static class PermissionType implements Parcelable {
        final int type;
        final String permission;

        PermissionType (int type, String permission) {
            this.type = type;
            this.permission = permission;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.type);
            dest.writeString(this.permission);
        }

        PermissionType(Parcel in) {
            this.type = in.readInt();
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

    static boolean hasPermission(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    /*
    static boolean isSystemApp(Context context) {
        return (context.getApplicationInfo().flags
                & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
    }
    */

    static List<PermissionType> recheckPermissions(Context context, List<PermissionType> _permissions) {
        List<PermissionType>  permissions = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            for (PermissionType _permission : _permissions) {
                if (_permission.permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    if (!Settings.System.canWrite(context))
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else if (_permission.permission.equals(permission.ACCESS_NOTIFICATION_POLICY)) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        if (!mNotificationManager.isNotificationPolicyAccessGranted())
                            permissions.add(new PermissionType(_permission.type, _permission.permission));
                    }
                    else
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else if (_permission.permission.equals(permission.SYSTEM_ALERT_WINDOW)) {
                    if (!Settings.canDrawOverlays(context))
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                } else {
                    if (ContextCompat.checkSelfPermission(context, _permission.permission) != PackageManager.PERMISSION_GRANTED)
                        permissions.add(new PermissionType(_permission.type, _permission.permission));
                }
            }
        }
        return permissions;
    }

    private static List<PermissionType> checkProfilePermissions(Context context, Profile profile) {
        List<PermissionType>  permissions = new ArrayList<>();
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            /*if (!checkProfileVolumePreferences(context, profile)) {
                permissions.add(new PermissionType(PERMISSION_PROFILE_VOLUME_PREFERENCES, permission.WRITE_SETTINGS));
                //Log.d("Permissions.checkProfilePermissions","PERMISSION_PROFILE_VOLUME_PREFERENCES");
            }*/
            checkProfileVibrationOnTouch(context, profile, permissions);
            checkProfileVibrateWhenRinging(context, profile, permissions);
            checkProfileRingtones(context, profile, permissions);
            checkProfileScreenTimeout(context, profile, permissions);
            checkProfileScreenBrightness(context, profile, permissions);
            checkProfileAutoRotation(context, profile, permissions);
            checkProfileNotificationLed(context, profile, permissions);
            checkProfileWallpaper(context, profile, permissions);
            checkProfileRadioPreferences(context, profile, permissions);
            checkProfilePhoneBroadcast(context, profile, permissions);
            checkCustomProfileIcon(context, profile, permissions);
            checkProfileAccessNotificationPolicy(context, profile, permissions);
            checkProfileLockDevice(context, profile, permissions);
            checkProfileDtmfToneWhenDialing(context, profile, permissions);
            checkProfileSoundOnTouch(context, profile, permissions);

            return permissions;
        }
        else
            return permissions;
    }

    /*
    static boolean checkProfileVolumePreferences(Context context, Profile profile) {
        //Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0); -- NOT WORKING, used is root
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
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
    */

    static boolean checkInstallTone(Context context, List<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean granted = ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if ((permissions != null) && (!granted))
                    permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                return granted;
            } else
                return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkPlayRingtoneNotification(Context context, boolean alsoContacts, List<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean grantedReadExternalStorage = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                boolean grantedContacts = true;
                if (alsoContacts)
                    grantedContacts = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                if (permissions != null) {
                    if (!grantedReadExternalStorage)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION, Manifest.permission.READ_EXTERNAL_STORAGE));
                    if (!grantedContacts)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_PLAY_RINGTONE_NOTIFICATION, Manifest.permission.READ_CONTACTS));
                }
                return grantedReadExternalStorage && grantedContacts;
            } else {
                boolean granted = hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                if (alsoContacts)
                    granted = granted && hasPermission(context, permission.READ_CONTACTS);
                return granted;
            }
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkProfileVibrationOnTouch(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._vibrationOnTouch != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_VIBRATION_ON_TOUCH, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    private static void checkProfileVibrateWhenRinging(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return;// true;
        PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "profile._name=" + profile._name);
        PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "permissions=" + permissions);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "profile._vibrateWhenRinging=" + profile._vibrateWhenRinging);
                if (profile._vibrateWhenRinging != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "granted=" + granted);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_VIBRATE_WHEN_RINGING, permission.WRITE_SETTINGS));
                    //return granted;
                }/* else
                    return true;*/
            } catch (Exception ignored) {
                //return false;
            }
        }
        //else
        //    return true;
    }

    static boolean checkVibrateWhenRinging(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean granted = Settings.System.canWrite(context);
                PPApplication.logE("Permissions.checkVibrateWhenRinging", "granted=" + granted);
                if (granted)
                    setShowRequestWriteSettingsPermission(context, true);
                return granted;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    private static void checkProfileNotificationLed(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._notificationLed != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_PROFILE_NOTIFICATION_LED, permission.WRITE_SETTINGS));
                    //return granted;
                }
                //else
                //    return true;
            } catch (Exception ignored) {
                //return false;
            }
        }
        //else
        //    return true;
    }

    static boolean checkProfileRingtones(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if ((profile._soundRingtoneChange != 0) ||
                        (profile._soundNotificationChange != 0) ||
                        (profile._soundAlarmChange != 0)) {
                    boolean grantedSystemSettings = Settings.System.canWrite(context);
                    boolean grantedStorage = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if (grantedSystemSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (permissions != null) {
                        if (!grantedSystemSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_RINGTONES, permission.WRITE_SETTINGS));
                        if (!grantedStorage)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_RINGTONES, permission.READ_EXTERNAL_STORAGE));
                    }
                    return grantedSystemSettings && grantedStorage;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkScreenTimeout(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenTimeout(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._deviceScreenTimeout != 0) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkScreenBrightness(Context context, List<PermissionType>  permissions) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                if (permissions != null) {
                    if (!grantedWriteSettings)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
                    if (!grantedDrawOverlays)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));
                }
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenBrightness(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile.getDeviceBrightnessChange()) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileAutoRotation(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._deviceAutoRotate != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_PROFILE_AUTOROTATION, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileWallpaper(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (profile._deviceWallpaperChange != 0) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_PROFILE_WALLPAPER, permission.READ_EXTERNAL_STORAGE));
                    return granted;
                } else
                    return true;
            } else {
                if (profile._deviceWallpaperChange != 0) {
                    return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                } else
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static void checkCustomProfileIcon(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0);
                Profile _profile = DatabaseHandler.getInstance(dataWrapper.context).getProfile(profile._id, false);
                if (_profile == null) return;// true;
                if (!_profile.getIsIconResourceID()) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_PROFILE_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));
                    //return granted;
                }
                //else
                //    return true;
            } catch (Exception ignored) {
                //return false;
            }
        }
        /*else {
            try {
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                Profile _profile = dataWrapper.getDatabaseHandler().getProfile(profile._id, false);
                if (_profile == null) return true;
                if (!_profile.getIsIconResourceID()) {
                    return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
                }
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    static boolean checkGallery(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkImport(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkExport(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkStorage(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    /*
    static boolean checkNFC(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            else
                return true;
        } catch (Exception e) {
            return false;
        }
    }
    */

    private static boolean checkRingtonePreference(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static void checkProfileRadioPreferences(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = true;
                if (profile._deviceWiFiAP != 0) {
                    grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                }
                boolean grantedReadPhoneState = true;
                if ((profile._deviceMobileData != 0) || (profile._deviceNetworkType != 0))
                    grantedReadPhoneState = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                //if (profile._deviceNFC != 0)
                //    granted = checkNFC(context);
                if (permissions != null) {
                    if (!grantedWriteSettings)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.WRITE_SETTINGS));
                    if (!grantedReadPhoneState)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.READ_PHONE_STATE));
                    //permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.MODIFY_PHONE_STATE));
                }
                boolean grantedLocation = true;
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                    grantedLocation = checkLocation(context);
                if (permissions != null) {
                    if (!grantedLocation) {
                        permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                        permissions.add(new PermissionType(PERMISSION_PROFILE_RADIO_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                    }
                }
                //return grantedWriteSettings && grantedReadPhoneState && grantedLocation;
            } catch (Exception ignored) {
                //return false;
            }
        }
        /*else {
            try {
                if ((profile._deviceMobileData != 0) || (profile._deviceNetworkType != 0))
                    return hasPermission(context, permission.READ_PHONE_STATE);
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                    return checkLocation(context);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    static boolean checkPhone(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.READ_CALENDAR);
        } catch (Exception e) {
            return false;
        }
    }

    private static void checkProfilePhoneBroadcast(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._volumeSpeakerPhone != 0) {
                    boolean grantedReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedOutgoingCall = ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedReadPhoneState)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_PHONE_BROADCAST, permission.READ_PHONE_STATE));
                        if (!grantedOutgoingCall)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_PHONE_BROADCAST, permission.PROCESS_OUTGOING_CALLS));
                    }
                    //return grantedOutgoingCall && grantedReadPhoneState;
                }
                //else
                //    return true;
            } catch (Exception ignored) {
                //return false;
            }
        }
        /*else {
            try {
                if (profile._volumeSpeakerPhone != 0)
                    return hasPermission(context, permission.READ_PHONE_STATE) &&
                            hasPermission(context, permission.PROCESS_OUTGOING_CALLS);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    static boolean checkProfileAccessNotificationPolicy(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    if ((profile._volumeRingerMode != 0) ||
                            profile.getVolumeRingtoneChange() ||
                            profile.getVolumeNotificationChange() ||
                            profile.getVolumeSystemChange()) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        boolean granted = false;
                        if (mNotificationManager != null)
                            granted = mNotificationManager.isNotificationPolicyAccessGranted();
                        if (granted)
                            setShowRequestAccessNotificationPolicyPermission(context, true);
                        if ((permissions != null) && (!granted))
                            permissions.add(new PermissionType(PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY, permission.ACCESS_NOTIFICATION_POLICY));
                        return granted;
                    } else
                        return true;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkAccessNotificationPolicy(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    boolean granted = false;
                    if (mNotificationManager != null)
                        granted = mNotificationManager.isNotificationPolicyAccessGranted();
                    if (granted)
                        setShowRequestAccessNotificationPolicyPermission(context, true);
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkLockDevice(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);
                return grantedWriteSettings && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileLockDevice(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._lockDevice == 1) {
                    // only for lockDevice = Screen off
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_LOCK_DEVICE, permission.WRITE_SETTINGS));
                        if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_LOCK_DEVICE, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileDtmfToneWhenDialing(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._dtmfToneWhenDialing != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileSoundOnTouch(Context context, Profile profile, List<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._soundOnTouch != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    else if (permissions != null)
                        permissions.add(new PermissionType(PERMISSION_PROFILE_SOUND_ON_TOUCH, permission.WRITE_SETTINGS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    private static List<PermissionType> checkEventPermissions(Context context, Event event) {
        List<PermissionType>  permissions = new ArrayList<>();
        if (event == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkEventCalendar(context, event, permissions);
            checkEventCallContacts(context, event, permissions);
            checkEventPhoneBroadcast(context, event, permissions);
            checkEventSMSContacts(context, event, permissions);
            checkEventSMSBroadcast(context, event, permissions);
            checkEventLocation(context, event, permissions);

            return permissions;
        }
        else
            return permissions;
    }


    static boolean checkContacts(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.READ_CONTACTS);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkEventCallContacts(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCall._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_CALL_PREFERENCES, permission.READ_CONTACTS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if (event._eventPreferencesCall._enabled)
                    return hasPermission(context, permission.READ_CONTACTS);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkEventSMSContacts(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesSMS._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.READ_CONTACTS));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if (event._eventPreferencesSMS._enabled)
                    return hasPermission(context, permission.READ_CONTACTS);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkCalendar(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.READ_CALENDAR);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkEventCalendar(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCalendar._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_CALENDAR_PREFERENCES, permission.READ_CALENDAR));
                    return granted;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if (event._eventPreferencesCalendar._enabled)
                    return hasPermission(context, permission.READ_CALENDAR);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkSMS(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.READ_CALENDAR);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkEventSMSBroadcast(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesSMS._enabled) {
                    boolean grantedReceiveSMS = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedReadSMS = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedReceiveMMS = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedReceiveSMS)
                            permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.RECEIVE_SMS));
                        if (!grantedReadSMS)
                            permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.READ_SMS));
                        if (!grantedReceiveMMS)
                            permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.RECEIVE_MMS));
                    }
                    return grantedReceiveSMS && grantedReadSMS && grantedReceiveMMS;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if (event._eventPreferencesSMS._enabled) {
                    return hasPermission(context, permission.RECEIVE_SMS) &&
                            hasPermission(context, permission.READ_SMS) &&
                            hasPermission(context, permission.RECEIVE_MMS);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkLocation(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.ACCESS_COARSE_LOCATION) &&
                        hasPermission(context, permission.ACCESS_FINE_LOCATION);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkEventLocation(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if ((event._eventPreferencesWifi._enabled &&
                        ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_INFRONT) ||
                                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))) ||
                        (event._eventPreferencesBluetooth._enabled &&
                                ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_INFRONT) ||
                                        (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT))) ||
                        (event._eventPreferencesLocation._enabled) ||
                        (event._eventPreferencesMobileCells._enabled)) {
                    boolean grantedAccessCoarseLocation = ContextCompat.checkSelfPermission(context, permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedAccessFineLocation = ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedAccessCoarseLocation)
                            permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                        if (!grantedAccessFineLocation)
                            permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                    }
                    return grantedAccessCoarseLocation && grantedAccessFineLocation;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if ((event._eventPreferencesWifi._enabled &&
                        ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_INFRONT) ||
                                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))) ||
                        (event._eventPreferencesBluetooth._enabled &&
                                ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_INFRONT) ||
                                        (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT))) ||
                        (event._eventPreferencesLocation._enabled) ||
                        (event._eventPreferencesMobileCells._enabled)) {
                    return hasPermission(context, permission.ACCESS_COARSE_LOCATION) &&
                            hasPermission(context, permission.ACCESS_FINE_LOCATION);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkEventPhoneBroadcast(Context context, Event event, List<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCall._enabled || event._eventPreferencesOrientation._enabled) {
                    boolean grantedPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedOutgoingCall = ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedPhoneState)
                            permissions.add(new PermissionType(PERMISSION_EVENT_CALL_PREFERENCES, permission.READ_PHONE_STATE));
                        if (!grantedOutgoingCall)
                            permissions.add(new PermissionType(PERMISSION_EVENT_CALL_PREFERENCES, permission.PROCESS_OUTGOING_CALLS));
                    }
                    return grantedPhoneState && grantedOutgoingCall;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else {
            try {
                if (event._eventPreferencesCall._enabled || event._eventPreferencesOrientation._enabled) {
                    return hasPermission(context, permission.READ_PHONE_STATE) &&
                            hasPermission(context, permission.PROCESS_OUTGOING_CALLS);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static boolean checkCamera(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return true;
            } else
                return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkMicrophone(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return true;
            } else
                return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkSensors(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return true;
            } else
                return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean grantProfilePermissions(Context context, Profile profile, boolean mergedProfile,
                                                  boolean onlyNotification,
                                                  //boolean forGUI, boolean monochrome, int monochromeValue,
                                                  int startupSource, /*boolean interactive,*/ Activity activity,
                                                  boolean activateProfile) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = checkProfilePermissions(context, profile);
            PPApplication.logE("Permissions.grantProfilePermissions", "permissions.size()=" + permissions.size());
            PPApplication.logE("Permissions.grantProfilePermissions", "startupSource=" + startupSource);
            if (permissions.size() > 0) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PROFILE);
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(EXTRA_MERGED_PROFILE, mergedProfile);
                    if (onlyNotification)
                        addMergedPermissions(context, permissions);
                    else
                        intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
                    //intent.putExtra(EXTRA_FOR_GUI, forGUI);
                    //intent.putExtra(EXTRA_MONOCHROME, monochrome);
                    //intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                    //intent.putExtra(EXTRA_INTERACTIVE, interactive);
                    intent.putExtra(EXTRA_ACTIVATE_PROFILE, activateProfile);
                    if (!onlyNotification)
                        profileActivationActivity = activity;
                    else
                        profileActivationActivity = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return permissions.size() == 0;
        }
        else
            return true;
    }

    static boolean grantInstallTonePermissions(Context context, boolean onlyNotification) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkInstallTone(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_INSTALL_TONE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static void grantPlayRingtoneNotificationPermissions(Context context/*, boolean onlyNotification*/, boolean alsoContacts) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkPlayRingtoneNotification(context, alsoContacts, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, true/*onlyNotification*/);
                    intent.putExtra(EXTRA_GRANT_ALSO_CONTACTS, alsoContacts);
                    context.startActivity(intent);
                } catch (Exception e) {
                    //return false;
                }
            }
            //return granted;
        }
        //else
        //    return true;
    }

    static boolean grantWallpaperPermissions(Context context, WallpaperViewPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_PROFILE_WALLPAPER, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WALLPAPER);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    wallpaperViewPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantCustomProfileIconPermissions(Context context, ProfileIconPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_PROFILE_CUSTOM_PROFILE_ICON, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CUSTOM_PROFILE_ICON);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    profileIconPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantBrightnessDialogPermissions(Context context, BrightnessDialogPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkScreenBrightness(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_BRIGHTNESS_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    brightnessDialogPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantExportPermissions(Context context, EditorProfilesActivity editor) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkExport(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EXPORT, permission.WRITE_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    editorActivity = editor;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantImportPermissions(Context context, EditorProfilesActivity editor, String applicationDataPath) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkImport(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_IMPORT, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_IMPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_APPLICATION_DATA_PATH, applicationDataPath);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    editorActivity = editor;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantEventPermissions(Context context, Event event,
                                                  boolean onlyNotification) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            List<PermissionType> permissions = checkEventPermissions(context, event);
            if (permissions.size() > 0) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EVENT);
                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                    if (onlyNotification)
                        addMergedPermissions(context, permissions);
                    else
                        intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return permissions.size() == 0;
        }
        else
            return true;
    }

    static boolean grantWifiScanDialogPermissions(Context context, WifiSSIDPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    wifiSSIDPreference = preference;
                    bluetoothNamePreference = null;
                    locationGeofenceEditorActivity = null;
                    mobileCellsPreference = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantBluetoothScanDialogPermissions(Context context, BluetoothNamePreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    bluetoothNamePreference = preference;
                    wifiSSIDPreference = null;
                    locationGeofenceEditorActivity = null;
                    mobileCellsPreference = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantCalendarDialogPermissions(Context context, CalendarsMultiSelectDialogPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkCalendar(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_CALENDAR_PREFERENCES, permission.READ_CALENDAR));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CALENDAR_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    calendarsMultiSelectDialogPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantContactsDialogPermissions(Context context, ContactsMultiSelectDialogPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkContacts(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_CONTACTS, permission.READ_CONTACTS));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CONTACT_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    contactsMultiSelectDialogPreference = preference;
                    contactGroupsMultiSelectDialogPreference = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantContactGroupsDialogPermissions(Context context, ContactGroupsMultiSelectDialogPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkContacts(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_CONTACTS, permission.READ_CONTACTS));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CONTACT_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    contactGroupsMultiSelectDialogPreference = preference;
                    contactsMultiSelectDialogPreference = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantLocationGeofenceEditorPermissions(Context context, LocationGeofenceEditorActivity activity) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    bluetoothNamePreference = null;
                    wifiSSIDPreference = null;
                    locationGeofenceEditorActivity = activity;
                    mobileCellsPreference = null;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantMobileCellsDialogPermissions(Context context, MobileCellsPreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    wifiSSIDPreference = null;
                    bluetoothNamePreference = null;
                    locationGeofenceEditorActivity = null;
                    mobileCellsPreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantRingtonePreferenceDialogPermissions(Context context, RingtonePreference preference) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkRingtonePreference(context);
            if (!granted) {
                try {
                    List<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_RINGTONE_PREFERENCE, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_RINGTONE_PREFERENCE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, (ArrayList<PermissionType>) permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ringtonePreference = preference;
                    context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static void removeProfileNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removeInstallToneNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removePlayRingtoneNotificationNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removeEventNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID);
    }

    static void removeNotifications(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
            notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
            notificationManager.cancel(PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID);
        }
    }

    static void releaseReferences() {
        profileActivationActivity = null;
        wallpaperViewPreference = null;
        profileIconPreference = null;
        editorActivity = null;
        wifiSSIDPreference = null;
        bluetoothNamePreference = null;
        calendarsMultiSelectDialogPreference = null;
        contactsMultiSelectDialogPreference = null;
        contactGroupsMultiSelectDialogPreference = null;
        locationGeofenceEditorActivity = null;
        brightnessDialogPreference = null;
        mobileCellsPreference = null;
        ringtonePreference = null;
    }


    static boolean getShowRequestWriteSettingsPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, true);
    }

    static void setShowRequestWriteSettingsPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, value);
        editor.apply();
    }

    static boolean getShowRequestAccessNotificationPolicyPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, true);
    }

    static void setShowRequestAccessNotificationPolicyPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, value);
        editor.apply();
    }

    static boolean getShowRequestDrawOverlaysPermission(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, true);
    }

    static void setShowRequestDrawOverlaysPermission(Context context, boolean value)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, value);
        editor.apply();
    }

    static List<Permissions.PermissionType> getMergedPermissions(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);

        List<Permissions.PermissionType> permissions = new ArrayList<>();

        int count = preferences.getInt(PREF_MERGED_PERMISSIONS_COUNT, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++) {
            String json = preferences.getString(PREF_MERGED_PERMISSIONS + i, "");
            if (!json.isEmpty()) {
                Permissions.PermissionType permission = gson.fromJson(json, Permissions.PermissionType.class);
                permissions.add(permission);
            }
        }

        return permissions;
    }

    private static void addMergedPermissions(Context context, List<Permissions.PermissionType> permissions)
    {
        List<Permissions.PermissionType> savedPermissions = getMergedPermissions(context);

        for (Permissions.PermissionType permission : permissions) {
            boolean found = false;
            for (Permissions.PermissionType savedPermission : savedPermissions) {

                if (savedPermission.permission.equals(permission.permission)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                savedPermissions.add(new Permissions.PermissionType(permission.type, permission.permission));
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(PREF_MERGED_PERMISSIONS_COUNT, savedPermissions.size());

        Gson gson = new Gson();

        for (int i = 0; i < savedPermissions.size(); i++)
        {
            String json = gson.toJson(savedPermissions.get(i));
            editor.putString(PREF_MERGED_PERMISSIONS+i, json);
        }

        editor.apply();
    }

    static void clearMergedPermissions(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


    @TargetApi(Build.VERSION_CODES.M)
    static void saveAllPermissions(Context context, boolean permissionsChanged) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_WRITE_SYSTEM_SETTINGS_PERMISSION, Settings.System.canWrite(context));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        editor.putBoolean(PREF_NOTIFICATION_POLICY_PERMISSION, (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted()));
        editor.putBoolean(PREF_DRAW_OVERLAY_PERMISSION, Settings.canDrawOverlays(context));
        editor.putBoolean(PREF_CALENDAR_PERMISSION, Permissions.checkCalendar(context));
        editor.putBoolean(PREF_CAMERA_PERMISSION, Permissions.checkCamera(context));
        editor.putBoolean(PREF_CONTACTS_PERMISSION, Permissions.checkContacts(context));
        editor.putBoolean(PREF_LOCATION_PERMISSION, Permissions.checkLocation(context));
        editor.putBoolean(PREF_MICROPHONE_PERMISSION, Permissions.checkMicrophone(context));
        editor.putBoolean(PREF_PHONE_PERMISSION, Permissions.checkPhone(context));
        editor.putBoolean(PREF_SENSORS_PERMISSION, Permissions.checkSensors(context));
        editor.putBoolean(PREF_SMS_PERMISSION, Permissions.checkSMS(context));
        editor.putBoolean(PREF_STORAGE_PERMISSION, Permissions.checkStorage(context));

        editor.putBoolean(PREF_PERMISSIONS_CHANGED, permissionsChanged);

        editor.apply();
    }

    static void disablePermissionsChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_PERMISSIONS_CHANGED, false);
        editor.apply();
    }

    static boolean getPermissionsChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_PERMISSIONS_CHANGED, false);
    }

    static boolean getWriteSystemSettingsPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_WRITE_SYSTEM_SETTINGS_PERMISSION, false);
    }

    static boolean getNotificationPolicyPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_POLICY_PERMISSION, false);
    }

    static boolean getDrawOverlayPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_DRAW_OVERLAY_PERMISSION, false);
    }

    static boolean getCalendarPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_CALENDAR_PERMISSION, false);
    }

    static boolean getContactsPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_CONTACTS_PERMISSION, false);
    }

    static boolean getLocationPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_LOCATION_PERMISSION, false);
    }

    static boolean getSMSPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SMS_PERMISSION, false);
    }

    static boolean getPhonePermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_PHONE_PERMISSION, false);
    }

    static boolean getStoragePermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_STORAGE_PERMISSION, false);
    }

    static boolean getCameraPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_CAMERA_PERMISSION, false);
    }

    static boolean getMicrophonePermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_MICROPHONE_PERMISSION, false);
    }

    static boolean getSensorsPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SENSORS_PERMISSION, false);
    }

}
