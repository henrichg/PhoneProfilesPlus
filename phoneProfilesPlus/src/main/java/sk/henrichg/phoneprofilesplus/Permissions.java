package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission;

//import com.google.gson.Gson;

class Permissions {

    //static final int PERMISSION_PROFILE_VOLUME_PREFERENCES = 1;
    static final int PERMISSION_PROFILE_VIBRATION_ON_TOUCH = 2;
    static final int PERMISSION_PROFILE_RINGTONES = 3;
    static final int PERMISSION_PROFILE_SCREEN_TIMEOUT = 4;
    static final int PERMISSION_PROFILE_SCREEN_BRIGHTNESS = 5;
    static final int PERMISSION_PROFILE_AUTOROTATION = 6;
    static final int PERMISSION_PROFILE_WALLPAPER = 7;
    static final int PERMISSION_PROFILE_RADIO_PREFERENCES = 8;
    static final int PERMISSION_PROFILE_PHONE_STATE_BROADCAST = 9;
    static final int PERMISSION_PROFILE_CUSTOM_PROFILE_ICON = 10;
    //static final int PERMISSION_INSTALL_TONE = 11;
    static final int PERMISSION_EXPORT = 12;
    static final int PERMISSION_IMPORT = 13;
    static final int PERMISSION_EVENT_CALENDAR_PREFERENCES = 15;
    static final int PERMISSION_EVENT_CALL_PREFERENCES = 16;
    static final int PERMISSION_EVENT_SMS_PREFERENCES = 17;
    static final int PERMISSION_EVENT_LOCATION_PREFERENCES = 18;
    static final int PERMISSION_EVENT_CONTACTS_PREFERENCE = 19;
    static final int PERMISSION_PROFILE_NOTIFICATION_LED = 20;
    static final int PERMISSION_PROFILE_VIBRATE_WHEN_RINGING = 21;
    static final int PERMISSION_PLAY_RINGTONE_NOTIFICATION = 22;
    //static final int PERMISSION_PROFILE_ACCESS_NOTIFICATION_POLICY = 23;
    static final int PERMISSION_PROFILE_LOCK_DEVICE = 24;
    static final int PERMISSION_RINGTONE_PREFERENCE = 25;
    static final int PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING = 26;
    static final int PERMISSION_PROFILE_SOUND_ON_TOUCH = 27;
    static final int PERMISSION_BRIGHTNESS_PREFERENCE = 28;
    static final int PERMISSION_WALLPAPER_PREFERENCE = 29;
    static final int PERMISSION_CUSTOM_PROFILE_ICON_PREFERENCE = 30;
    static final int PERMISSION_LOCATION_PREFERENCE = 31;
    static final int PERMISSION_CALENDAR_PREFERENCE = 32;
    static final int PERMISSION_EVENT_ORIENTATION_PREFERENCES = 33;
    static final int PERMISSION_EVENT_WIFI_PREFERENCES = 34;
    static final int PERMISSION_EVENT_BLUETOOTH_PREFERENCES = 35;
    static final int PERMISSION_EVENT_MOBILE_CELLS_PREFERENCES = 36;
    //static final int PERMISSION_LOG_TO_FILE = 37;
    static final int PERMISSION_EVENT_BLUETOOTH_SWITCH_PREFERENCES = 38;
    static final int PERMISSION_EVENT_TIME_PREFERENCES = 39;
    static final int PERMISSION_PROFILE_ALWAYS_ON_DISPLAY = 40;
    static final int PERMISSION_PROFILE_CONNECT_TO_SSID_PREFERENCE = 41;

    static final int GRANT_TYPE_PROFILE = 1;
    //static final int GRANT_TYPE_INSTALL_TONE = 2;
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
    static final int GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG = 17;
    //static final int GRANT_TYPE_LOG_TO_FILE = 18;
    //static final int GRANT_TYPE_GRANT_ROOT = 19;
    static final int GRANT_TYPE_EXPORT_AND_EMAIL = 20;
    static final int GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR = 21;
    static final int GRANT_TYPE_CONNECT_TO_SSID_DIALOG = 22;

    static final int REQUEST_CODE = 5000;
    //static final int REQUEST_CODE_FORCE_GRANT = 6000;

    static final String EXTRA_GRANT_TYPE = "grant_type";
    static final String EXTRA_MERGED_PROFILE = "merged_profile";
    static final String EXTRA_PERMISSION_TYPES = "permission_types";
    static final String EXTRA_ONLY_NOTIFICATION = "only_notification";
    static final String EXTRA_FORCE_GRANT = "force_grant";
    //static final String EXTRA_FOR_GUI = "for_gui";
    //static final String EXTRA_MONOCHROME = "monochrome";
    //static final String EXTRA_MONOCHROME_VALUE = "monochrome_value";
    static final String EXTRA_INTERACTIVE = "interactive";
    static final String EXTRA_APPLICATION_DATA_PATH = "application_data_path";
    static final String EXTRA_ACTIVATE_PROFILE = "activate_profile";
    static final String EXTRA_GRANT_ALSO_CONTACTS = "grant_also_contacts";
    //static final String EXTRA_FORCE_START_SCANNER = "force_start_scanner";
    static final String EXTRA_FROM_NOTIFICATION = "from_notification";

    //static Activity profileActivationActivity = null;
    //static WallpaperViewPreference wallpaperViewPreference = null;
    //static ProfileIconPreference profileIconPreference = null;
    //static EditorProfilesActivity editorActivity = null;
    //static WifiSSIDPreference wifiSSIDPreference = null;
    //static BluetoothNamePreference bluetoothNamePreference = null;
    //static CalendarsMultiSelectDialogPreference calendarsMultiSelectDialogPreference = null;
    //static ContactsMultiSelectDialogPreference contactsMultiSelectDialogPreference = null;
    //static ContactGroupsMultiSelectDialogPreference contactGroupsMultiSelectDialogPreference = null;
    //static LocationGeofenceEditorActivity locationGeofenceEditorActivity = null;
    //static BrightnessDialogPreferenceX brightnessDialogPreference = null;
    //static MobileCellsPreference mobileCellsPreference = null;
    //static MobileCellsRegistrationDialogPreference mobileCellsRegistrationDialogPreference = null;
    //static RingtonePreference ringtonePreference = null;

    private static final String PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION = "show_request_write_settings_permission";
    //private static final String PREF_MERGED_PERMISSIONS = "merged_permissions";
    //private static final String PREF_MERGED_PERMISSIONS_COUNT = "merged_permissions_count";
    //private static final String PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION = "show_request_access_notification_policy_permission";
    private static final String PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION = "show_request_draw_overlays_permission";

    private static final String PREF_PERMISSIONS_CHANGED = "permissionsChanged";

    // permission groups
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSION = "writeSystemSettingsPermission";
    //private static final String PREF_NOTIFICATION_POLICY_PERMISSION = "notificationPolicyPermission";
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
    //private static final String PREF_CALL_LOGS_PERMISSION = "callLogsPermission";

    static boolean grantRootChanged = false;

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

    static ArrayList<PermissionType> recheckPermissions(Context context, ArrayList<PermissionType> _permissions) {
        ArrayList<PermissionType>  permissions = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (_permissions == null)
                return permissions;
            for (PermissionType _permission : _permissions) {
                switch (_permission.permission) {
                    case permission.WRITE_SETTINGS:
                        if (!Settings.System.canWrite(context)) {
                            if (getShowRequestWriteSettingsPermission(context))
                                permissions.add(new PermissionType(_permission.type, _permission.permission));
                        }
                        break;
                    /*case permission.ACCESS_NOTIFICATION_POLICY:
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (!mNotificationManager.isNotificationPolicyAccessGranted())
                                if (getShowRequestAccessNotificationPolicyPermission(context))
                                    permissions.add(new PermissionType(_permission.type, _permission.permission));
                        } else {
                            if (getShowRequestAccessNotificationPolicyPermission(context))
                                permissions.add(new PermissionType(_permission.type, _permission.permission));
                        }
                        break;*/
                    case permission.SYSTEM_ALERT_WINDOW:
                        if (!Settings.canDrawOverlays(context)) {
                            if (getShowRequestDrawOverlaysPermission(context))
                                permissions.add(new PermissionType(_permission.type, _permission.permission));
                        }
                        break;
                    default:
                        if (ContextCompat.checkSelfPermission(context, _permission.permission) != PackageManager.PERMISSION_GRANTED)
                            permissions.add(new PermissionType(_permission.type, _permission.permission));
                        break;
                }
            }
        }
        return permissions;
    }

    static ArrayList<PermissionType> checkProfilePermissions(Context context, Profile profile) {
        ArrayList<PermissionType>  permissions = new ArrayList<>();
        if (profile == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
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
            //checkProfileAccessNotificationPolicy(context, profile, permissions);
            checkProfileLockDevice(context, profile, permissions);
            checkProfileDtmfToneWhenDialing(context, profile, permissions);
            checkProfileSoundOnTouch(context, profile, permissions);
            checkProfileAlwaysOnDisplay(context, profile, permissions);

            return permissions;
        }
        else
            return permissions;
    }

    /*
    static boolean checkInstallTone(Context context, ArrayList<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean writeGranted = ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                boolean readGranted = ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (permissions != null) {
                    if (!readGranted)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.READ_EXTERNAL_STORAGE));
                    if (!writeGranted)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_INSTALL_TONE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                }
                return readGranted && writeGranted;
            } else
                return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE) &&
                        hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static boolean checkPlayRingtoneNotification(Context context, boolean alsoContacts, ArrayList<PermissionType>  permissions) {
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

    static boolean checkProfileVibrationOnTouch(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._vibrationOnTouch != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (!granted) {
                        if (permissions != null)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_VIBRATION_ON_TOUCH, permission.WRITE_SETTINGS));
                    }
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

    private static void checkProfileVibrateWhenRinging(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return;// true;
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "profile._name=" + profile._name);
            PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "permissions=" + permissions);
        }*/
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                //PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "profile._vibrateWhenRinging=" + profile._vibrateWhenRinging);
                if (profile._vibrateWhenRinging != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    //PPApplication.logE("Permissions.checkProfileVibrateWhenRinging", "granted=" + granted);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (!granted) {
                        if (permissions != null)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_VIBRATE_WHEN_RINGING, permission.WRITE_SETTINGS));
                    }
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
                //PPApplication.logE("Permissions.checkVibrateWhenRinging", "granted=" + granted);
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

    private static void checkProfileNotificationLed(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
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

    static boolean checkProfileRingtones(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
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
                /*boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                if (grantedDrawOverlays)
                    setShowRequestDrawOverlaysPermission(context, true);*/
                return grantedWriteSettings;// && grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenTimeout(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._deviceScreenTimeout != 0) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    /*boolean grantedDrawOverlays = (profile._deviceScreenTimeout != 8) || Settings.canDrawOverlays(context);
                    if (grantedDrawOverlays)
                        setShowRequestDrawOverlaysPermission(context, true);*/
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.WRITE_SETTINGS));
                        /*if (!grantedDrawOverlays)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_TIMEOUT, permission.SYSTEM_ALERT_WINDOW));*/
                    }
                    return grantedWriteSettings;// && grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkScreenBrightness(Context context, ArrayList<PermissionType>  permissions) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = Settings.System.canWrite(context);
                if (grantedWriteSettings)
                    setShowRequestWriteSettingsPermission(context, true);
                //boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                //if (grantedDrawOverlays)
                //    setShowRequestDrawOverlaysPermission(context, true);
                if (permissions != null) {
                    if (!grantedWriteSettings)
                        permissions.add(new PermissionType(PERMISSION_BRIGHTNESS_PREFERENCE, permission.WRITE_SETTINGS));
                    //if (!grantedDrawOverlays)
                    //    permissions.add(new PermissionType(PERMISSION_BRIGHTNESS_PREFERENCE, permission.SYSTEM_ALERT_WINDOW));
                }
                return grantedWriteSettings; //&& grantedDrawOverlays;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileScreenBrightness(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile.getDeviceBrightnessChange()) {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    //boolean grantedDrawOverlays = Settings.canDrawOverlays(context);
                    //if (grantedDrawOverlays)
                    //    setShowRequestDrawOverlaysPermission(context, true);
                    if (permissions != null) {
                        if (!grantedWriteSettings)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_BRIGHTNESS, permission.WRITE_SETTINGS));
                        //if (!grantedDrawOverlays)
                        //    permissions.add(new PermissionType(PERMISSION_PROFILE_SCREEN_BRIGHTNESS, permission.SYSTEM_ALERT_WINDOW));
                    }
                    return grantedWriteSettings; //&& grantedDrawOverlays;
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return true;
    }

    static boolean checkProfileAutoRotation(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
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

    static boolean checkProfileWallpaper(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
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

    private static void checkCustomProfileIcon(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
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

    static boolean checkImport(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkExport(Context context) {
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

    static boolean checkRingtonePreference(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                return (ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            else
                return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            return false;
        }
    }

    private static void checkProfileRadioPreferences(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean grantedWriteSettings = true;
                if (profile._deviceWiFiAP != 0) {
                    grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                }
                if (grantedWriteSettings) {
                    if (profile._deviceBluetooth != 0) {
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                grantedWriteSettings = Settings.System.canWrite(context);
                                if (grantedWriteSettings)
                                    setShowRequestWriteSettingsPermission(context, true);
                            }
                        }
                    }
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
                return (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, Manifest.permission.READ_PHONE_STATE);
        } catch (Exception e) {
            return false;
        }
    }

    static boolean checkBluetoothForEMUI(Context context) {
        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                try {
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    return grantedWriteSettings;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return true;
        }
        else
            return true;
    }

    private static void checkProfilePhoneBroadcast(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() &&
                        ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
                if (unlinkEnabled || (profile._volumeSpeakerPhone != 0)) {
                    boolean grantedReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedReadPhoneState)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_PHONE_STATE_BROADCAST, permission.READ_PHONE_STATE));
                    }
                    //return grantedReadPhoneState;
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
                    return hasPermission(context, permission.READ_PHONE_STATE);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    /*static boolean checkProfileAccessNotificationPolicy(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                    PPApplication.logE("Permissions.checkProfileAccessNotificationPolicy", "ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS exists");
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
    }*/

/*    static boolean checkAccessNotificationPolicy(Context context) {
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
    }*/

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

    private static void checkProfileAlwaysOnDisplay(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            try {
                if (profile._alwaysOnDisplay != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (!granted) {
                        if (permissions != null)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_ALWAYS_ON_DISPLAY, permission.WRITE_SETTINGS));
                    }
                    //return granted;
                }// else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        //else
        //    return true;
    }

    static boolean checkProfileLockDevice(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._lockDevice == 1) {
                    // only for lockDevice = Screen off
                    boolean grantedWriteSettings = Settings.System.canWrite(context);
                    if (grantedWriteSettings)
                        setShowRequestWriteSettingsPermission(context, true);
                    boolean grantedDrawOverlays = (profile._lockDevice != 1) || Settings.canDrawOverlays(context);
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

    static boolean checkProfileDtmfToneWhenDialing(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._dtmfToneWhenDialing != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (!granted) {
                        if (permissions != null)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_DTMF_TONE_WHEN_DIALING, permission.WRITE_SETTINGS));
                    }
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

    static boolean checkProfileSoundOnTouch(Context context, Profile profile, ArrayList<PermissionType>  permissions) {
        if (profile == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (profile._soundOnTouch != 0) {
                    boolean granted = Settings.System.canWrite(context);
                    if (granted)
                        setShowRequestWriteSettingsPermission(context, true);
                    if (!granted) {
                        if (permissions != null)
                            permissions.add(new PermissionType(PERMISSION_PROFILE_SOUND_ON_TOUCH, permission.WRITE_SETTINGS));
                    }
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

    static ArrayList<PermissionType> checkEventPermissions(Context context, Event event) {
        ArrayList<PermissionType>  permissions = new ArrayList<>();
        if (event == null) return permissions;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkEventCalendar(context, event, permissions);
            checkEventPhoneBroadcast(context, event, permissions);
            checkEventCallContacts(context, event, permissions);
            checkEventSMSContacts(context, event, permissions);
            checkEventLocation(context, event, permissions);
            checkEventBluetoothForEMUI(context, event, permissions);
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

    static private void checkEventCallContacts(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return;// true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCall._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_CALL_PREFERENCES, permission.READ_CONTACTS));
                    //return granted;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if (event._eventPreferencesCall._enabled)
                    return hasPermission(context, permission.READ_CONTACTS);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    static private void checkEventSMSContacts(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return; // true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesSMS._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.READ_CONTACTS));
                    //return granted;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if (event._eventPreferencesSMS._enabled)
                    return hasPermission(context, permission.READ_CONTACTS);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
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

    static private void checkEventCalendar(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return; // true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCalendar._enabled) {
                    boolean granted = ContextCompat.checkSelfPermission(context, permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
                    if ((permissions != null) && (!granted))
                        permissions.add(new PermissionType(PERMISSION_EVENT_CALENDAR_PREFERENCES, permission.READ_CALENDAR));
                    //return granted;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if (event._eventPreferencesCalendar._enabled)
                    return hasPermission(context, permission.READ_CALENDAR);
                else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    @SuppressWarnings("SameReturnValue")
    static boolean checkSMS(@SuppressWarnings("unused") Context context) {
        return true;
        /*
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                return (ContextCompat.checkSelfPermission(context, permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) &&
                        // not needed, mobile number is in bundle of receiver intent, data of sms/mms is not read
                        //(ContextCompat.checkSelfPermission(context, permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(context, permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED);
            } else
                return hasPermission(context, permission.READ_CALENDAR);
        } catch (Exception e) {
            return false;
        }
        */
    }

    /*
    static boolean checkEventSMSBroadcast(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesSMS._enabled) {
                    boolean grantedReceiveSMS = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
                    // not needed, mobile number is in bundle of receiver intent, data of sms/mms is not read
                    //boolean grantedReadSMS = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedReceiveMMS = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_MMS) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (!grantedReceiveSMS)
                            permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.RECEIVE_SMS));
                        // not needed, mobile number is in bundle of receiver intent, data of sms/mms is not read
                        //if (!grantedReadSMS)
                        //    permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.READ_SMS));
                        if (!grantedReceiveMMS)
                            permissions.add(new PermissionType(PERMISSION_EVENT_SMS_PREFERENCES, permission.RECEIVE_MMS));
                    }
                    return grantedReceiveSMS && grantedReceiveMMS; // && grantedReadSMS;
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
                            // not needed, mobile number is in bundle of receiver intent, data of sms/mms is not read
                            //hasPermission(context, permission.READ_SMS) &&
                            hasPermission(context, permission.RECEIVE_MMS);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
    */

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

    static private void checkEventLocation(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return; // true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if ((event._eventPreferencesWifi._enabled &&
                        ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                         (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY))) ||
                    (event._eventPreferencesBluetooth._enabled &&
                        ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                         (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY))) ||
                    (event._eventPreferencesLocation._enabled) ||
                    (event._eventPreferencesMobileCells._enabled) ||
                    (event._eventPreferencesTime._enabled &&
                        (event._eventPreferencesTime._timeType != EventPreferencesTime.TIME_TYPE_EXACT))) {
                    boolean grantedAccessCoarseLocation = ContextCompat.checkSelfPermission(context, permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    boolean grantedAccessFineLocation = ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (event._eventPreferencesWifi._enabled &&
                                ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                                        (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY))) {
                            if (!grantedAccessCoarseLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_WIFI_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                            if (!grantedAccessFineLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_WIFI_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                        }
                        else
                        if (event._eventPreferencesBluetooth._enabled &&
                                ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                                        (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY))) {
                            if (!grantedAccessCoarseLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_BLUETOOTH_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                            if (!grantedAccessFineLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_BLUETOOTH_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                        }
                        else
                        if (event._eventPreferencesMobileCells._enabled) {
                            if (!grantedAccessCoarseLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_MOBILE_CELLS_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                            if (!grantedAccessFineLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_MOBILE_CELLS_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                        }
                        else
                        if (event._eventPreferencesTime._enabled) {
                            if (!grantedAccessCoarseLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_TIME_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                            if (!grantedAccessFineLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_TIME_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                        }
                        else {
                            if (!grantedAccessCoarseLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_COARSE_LOCATION));
                            if (!grantedAccessFineLocation)
                                permissions.add(new PermissionType(PERMISSION_EVENT_LOCATION_PREFERENCES, permission.ACCESS_FINE_LOCATION));
                        }
                    }
                    //return grantedAccessCoarseLocation && grantedAccessFineLocation;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if ((event._eventPreferencesWifi._enabled &&
                        ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                         (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY))) ||
                    (event._eventPreferencesBluetooth._enabled &&
                        ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                         (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY))) ||
                    (event._eventPreferencesLocation._enabled) ||
                    (event._eventPreferencesMobileCells._enabled) ||
                    (event._eventPreferencesTime._enabled &&
                        (event._eventPreferencesTime._timeType != EventPreferencesTime.TIME_TYPE_EXACT))) {
                    return hasPermission(context, permission.ACCESS_COARSE_LOCATION) &&
                            hasPermission(context, permission.ACCESS_FINE_LOCATION);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    static private void checkEventBluetoothForEMUI(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return; // true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesBluetooth._enabled &&
                    ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                     (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY))) {
                    boolean granted = checkBluetoothForEMUI(context);
                    if (permissions != null) {
                        if (!granted)
                            permissions.add(new PermissionType(PERMISSION_EVENT_BLUETOOTH_SWITCH_PREFERENCES, permission.WRITE_SETTINGS));
                    }
                    //return granted;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if (event._eventPreferencesBluetooth._enabled &&
                    ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                     (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY))) {
                    return hasPermission(context, permission.WRITE_SETTINGS);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    private static void checkEventPhoneBroadcast(Context context, Event event, ArrayList<PermissionType>  permissions) {
        if (event == null) return /*true*/;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            try {
                if (event._eventPreferencesCall._enabled) {
                    boolean grantedPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    if (permissions != null) {
                        if (event._eventPreferencesCall._enabled) {
                            if (!grantedPhoneState)
                                permissions.add(new PermissionType(PERMISSION_EVENT_CALL_PREFERENCES, permission.READ_PHONE_STATE));
                        }
                    }
                    //return grantedPhoneState;
                } //else
                    //return true;
            } catch (Exception e) {
                //return false;
            }
        }
        /*else {
            try {
                if (event._eventPreferencesCall._enabled) {
                    return hasPermission(context, permission.READ_PHONE_STATE);
                } else
                    return true;
            } catch (Exception e) {
                return false;
            }
        }*/
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    /*
    static boolean checkLogToFile(Context context, ArrayList<PermissionType>  permissions) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                boolean grantedWriteExternalStorage = ContextCompat.checkSelfPermission(context, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (permissions != null) {
                    if (!grantedWriteExternalStorage)
                        permissions.add(new Permissions.PermissionType(Permissions.PERMISSION_LOG_TO_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE));
                }
                return grantedWriteExternalStorage;
            } else {
                return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            return false;
        }
    }
    */

    static void /*boolean*/ grantProfilePermissions(Context context, Profile profile/*, boolean mergedProfile,*/
                                                  /*boolean onlyNotification,*/
                                                  //boolean forGUI, boolean monochrome, int monochromeValue,
                                                  /*int startupSource, boolean interactive,*/
                                                  /*boolean activateProfile,*/
                                                  /*boolean fromPreferences*/) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = checkProfilePermissions(context, profile);
            //if (PPApplication.logEnabled()) {
                //PPApplication.logE("Permissions.grantProfilePermissions", "permissions.size()=" + permissions.size());
            //    PPApplication.logE("Permissions.grantProfilePermissions", "startupSource=" + startupSource);
            //}
            if (permissions.size() > 0) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //if (!fromPreferences || onlyNotification)
                    //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PROFILE);
                    intent.putExtra(EXTRA_FORCE_GRANT, true/*fromPreferences*/);
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(EXTRA_MERGED_PROFILE, false);
                    //if (onlyNotification)
                    //    addMergedPermissions(context, permissions);
                    //else
                        intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    //intent.putExtra(EXTRA_FOR_GUI, forGUI);
                    //intent.putExtra(EXTRA_MONOCHROME, monochrome);
                    //intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR);
                    intent.putExtra(EXTRA_INTERACTIVE, false);
                    intent.putExtra(EXTRA_ACTIVATE_PROFILE, false);
                    //if (fromPreferences && (!onlyNotification))
                        ((Activity) context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_PROFILE);
                    //else
                    //    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("Permissions.grantProfilePermissions", Log.getStackTraceString(e));
                    //FirebaseCrashlytics.getInstance().recordException(e);
                    Crashlytics.logException(e);
                    //return false;
                }
            }
            //return permissions.size() == 0;
        }
        //else
        //    return true;
    }

    /*
    static boolean grantInstallTonePermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkInstallTone(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_INSTALL_TONE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, onlyNotification);
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
    */

    static void grantPlayRingtoneNotificationPermissions(Context context/*, boolean onlyNotification*/, boolean alsoContacts) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkPlayRingtoneNotification(context, alsoContacts, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //if (!fromPreferences || onlyNotification)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_PLAY_RINGTONE_NOTIFICATION);
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                    intent.putExtra(EXTRA_MERGED_PROFILE, false);
                    //if (onlyNotification)
                    //    addMergedPermissions(context, permissions);
                    //else
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, true);
                    //intent.putExtra(EXTRA_FOR_GUI, forGUI);
                    //intent.putExtra(EXTRA_MONOCHROME, monochrome);
                    //intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
                    intent.putExtra(EXTRA_INTERACTIVE, true);
                    intent.putExtra(EXTRA_ACTIVATE_PROFILE, 0);
                    //if (fromPreferences && (!onlyNotification))
                    //    ((Activity) context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_PROFILE);
                    //else
                    context.startActivity(intent);
                } catch (Exception ignored) {}
            }
            //return granted;
        }
        //else
        //    return true;
    }

    static boolean grantWallpaperPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_WALLPAPER_PREFERENCE, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WALLPAPER);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_WALLPAPER);
                    //wallpaperViewPreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantCustomProfileIconPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkGallery(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_CUSTOM_PROFILE_ICON_PREFERENCE, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CUSTOM_PROFILE_ICON);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_CUSTOM_PROFILE_ICON);
                    //profileIconPreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantBrightnessDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkScreenBrightness(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_BRIGHTNESS_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_BRIGHTNESS_DIALOG);
                    //brightnessDialogPreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantExportPermissions(Context context, EditorProfilesActivity editor, boolean email, boolean toAuthor) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkExport(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EXPORT, permission.WRITE_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    if (email) {
                        if (toAuthor)
                            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR);
                        else
                            intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT_AND_EMAIL);
                    }
                    else
                        intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EXPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    if (email) {
                        if (toAuthor)
                            editor.startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR);
                        else
                            editor.startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_EXPORT_AND_EMAIL);
                    }
                    else
                        editor.startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_EXPORT);
                    //editorActivity = editor;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantImportPermissions(Context context, EditorProfilesActivity editor/*, String applicationDataPath*/) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkImport(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_IMPORT, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_IMPORT);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_APPLICATION_DATA_PATH, PPApplication.EXPORT_PATH);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    editor.startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_IMPORT);
                    //editorActivity = editor;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static void /*boolean*/ grantEventPermissions(Context context, Event event/*,
                                                  boolean onlyNotification,
                                                  boolean fromPreferences*/) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = checkEventPermissions(context, event);
            if (permissions.size() > 0) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //if (!fromPreferences || onlyNotification)
                    //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_EVENT);
                    intent.putExtra(EXTRA_FORCE_GRANT, true/*fromPreferences*/);
                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                    //if (onlyNotification)
                    //    addMergedPermissions(context, permissions);
                    //else
                        intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    //if (fromPreferences && (!onlyNotification))
                        ((Activity) context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_EVENT);
                    //else
                    //    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("Permissions.grantEventPermissions", Log.getStackTraceString(e));
                    //FirebaseCrashlytics.getInstance().recordException(e);
                    Crashlytics.logException(e);
                    //return false;
                }
            }
            //return permissions.size() == 0;
        }
        //else
        //    return true;
    }

    static boolean grantWifiScanDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    //intent.putExtra(EXTRA_FORCE_START_SCANNER, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    //wifiSSIDPreference = preference;
                    //bluetoothNamePreference = null;
                    //locationGeofenceEditorActivity = null;
                    //mobileCellsPreference = null;
                    //mobileCellsRegistrationDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantBluetoothScanDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    //intent.putExtra(EXTRA_FORCE_START_SCANNER, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_WIFI_BT_SCAN_DIALOG);
                    //bluetoothNamePreference = preference;
                    //wifiSSIDPreference = null;
                    //locationGeofenceEditorActivity = null;
                    //mobileCellsPreference = null;
                    //mobileCellsRegistrationDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantCalendarDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkCalendar(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_CALENDAR_PREFERENCE, permission.READ_CALENDAR));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CALENDAR_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_CALENDAR_DIALOG);
                    //calendarsMultiSelectDialogPreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantContactsDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkContacts(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_CONTACTS_PREFERENCE, permission.READ_CONTACTS));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CONTACT_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_CONTACT_DIALOG);
                    //contactsMultiSelectDialogPreference = preference;
                    //contactGroupsMultiSelectDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantContactGroupsDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkContacts(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_EVENT_CONTACTS_PREFERENCE, permission.READ_CONTACTS));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CONTACT_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_CONTACT_DIALOG);
                    //contactGroupsMultiSelectDialogPreference = preference;
                    //contactsMultiSelectDialogPreference = null;
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
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    activity.startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_LOCATION_GEOFENCE_EDITOR_ACTIVITY);
                    //bluetoothNamePreference = null;
                    //wifiSSIDPreference = null;
                    //locationGeofenceEditorActivity = activity;
                    //mobileCellsPreference = null;
                    //mobileCellsRegistrationDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantMobileCellsDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG);
                    //wifiSSIDPreference = null;
                    //bluetoothNamePreference = null;
                    //locationGeofenceEditorActivity = null;
                    //mobileCellsPreference = preference;
                    //mobileCellsRegistrationDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantMobileCellsRegistrationDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_LOCATION_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG);
                    //wifiSSIDPreference = null;
                    //bluetoothNamePreference = null;
                    //locationGeofenceEditorActivity = null;
                    //mobileCellsPreference = null;
                    //mobileCellsRegistrationDialogPreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantRingtonePreferenceDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkRingtonePreference(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_RINGTONE_PREFERENCE, permission.READ_EXTERNAL_STORAGE));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_RINGTONE_PREFERENCE);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_RINGTONE_PREFERENCE);
                    //ringtonePreference = preference;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    static boolean grantConnectToSSIDDialogPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean granted = checkLocation(context);
            if (!granted) {
                try {
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    permissions.add(new PermissionType(PERMISSION_PROFILE_CONNECT_TO_SSID_PREFERENCE, permission.ACCESS_COARSE_LOCATION));
                    permissions.add(new PermissionType(PERMISSION_PROFILE_CONNECT_TO_SSID_PREFERENCE, permission.ACCESS_FINE_LOCATION));

                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_CONNECT_TO_SSID_DIALOG);
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    //intent.putExtra(EXTRA_ONLY_NOTIFICATION, false);
                    intent.putExtra(EXTRA_FORCE_GRANT, true);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_CONNECT_TO_SSID_DIALOG);
                    //contactsMultiSelectDialogPreference = preference;
                    //contactGroupsMultiSelectDialogPreference = null;
                    //context.startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
            }
            return granted;
        }
        else
            return true;
    }

    /*
    static void grantLogToFilePermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ArrayList<PermissionType> permissions = new ArrayList<>();
            boolean granted = checkLogToFile(context, permissions);
            if (!granted) {
                try {
                    Intent intent = new Intent(context, GrantPermissionActivity.class);
                    //if (!fromPreferences || onlyNotification)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);  // this close all activities with same taskAffinity
                    intent.putExtra(EXTRA_GRANT_TYPE, GRANT_TYPE_LOG_TO_FILE);
                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                    intent.putExtra(EXTRA_MERGED_PROFILE, false);
                    //if (onlyNotification)
                    //    addMergedPermissions(context, permissions);
                    //else
                    intent.putParcelableArrayListExtra(EXTRA_PERMISSION_TYPES, permissions);
                    intent.putExtra(EXTRA_ONLY_NOTIFICATION, true);
                    //intent.putExtra(EXTRA_FOR_GUI, forGUI);
                    //intent.putExtra(EXTRA_MONOCHROME, monochrome);
                    //intent.putExtra(EXTRA_MONOCHROME_VALUE, monochromeValue);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR);
                    intent.putExtra(EXTRA_INTERACTIVE, true);
                    intent.putExtra(EXTRA_ACTIVATE_PROFILE, 0);
                    //if (fromPreferences && (!onlyNotification))
                    //    ((Activity) context).startActivityForResult(intent, REQUEST_CODE + GRANT_TYPE_PROFILE);
                    //else
                    context.startActivity(intent);
                } catch (Exception ignored) {}
            }
        }
    }
    */

    static void removeProfileNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
    }

    /*
    static void removeInstallToneNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
    }
    */

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

    /*
    static void removeLogToFileNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID);
    }
    */

    static void removeNotifications(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(PPApplication.GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID);
            //notificationManager.cancel(PPApplication.GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID);
            notificationManager.cancel(PPApplication.GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID);
            notificationManager.cancel(PPApplication.GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID);
            //notificationManager.cancel(PPApplication.GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID);
        }
    }

    /*
    static void releaseReferences() {
        //profileActivationActivity = null;
        //wallpaperViewPreference = null;
        //profileIconPreference = null;
        //editorActivity = null;
        //wifiSSIDPreference = null;
        //bluetoothNamePreference = null;
        //calendarsMultiSelectDialogPreference = null;
        //contactsMultiSelectDialogPreference = null;
        //contactGroupsMultiSelectDialogPreference = null;
        //locationGeofenceEditorActivity = null;
        //brightnessDialogPreference = null;
        //mobileCellsPreference = null;
        //mobileCellsRegistrationDialogPreference = null;
        //ringtonePreference = null;
    }
    */

    //---------

    static boolean getShowRequestWriteSettingsPermission(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, true);
    }

    static void setShowRequestWriteSettingsPermission(Context context, boolean value)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, value);
        editor.apply();
    }

    /*
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
     */

    static boolean getShowRequestDrawOverlaysPermission(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29)
            return true;
        else
            return ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, true);
    }

    static void setShowRequestDrawOverlaysPermission(Context context, boolean value)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, value);
        editor.apply();
    }

    static void setAllShowRequestPermissions(Context context,
                                             @SuppressWarnings("SameParameterValue") boolean value) {
        //Permissions.setShowRequestAccessNotificationPolicyPermission(context, value);
        Permissions.setShowRequestWriteSettingsPermission(context, value);
        Permissions.setShowRequestDrawOverlaysPermission(context, value);
    }

    //--------

    /*
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
    */

    @TargetApi(Build.VERSION_CODES.M)
    static void saveAllPermissions(Context context, boolean permissionsChanged) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_WRITE_SYSTEM_SETTINGS_PERMISSION, Settings.System.canWrite(context));
        //NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //editor.putBoolean(PREF_NOTIFICATION_POLICY_PERMISSION, (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted()));
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

    /*
    static void disablePermissionsChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_PERMISSIONS_CHANGED, false);
        editor.apply();
    }
    */

    static boolean getPermissionsChanged(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_PERMISSIONS_CHANGED, false);
    }

    static boolean getWriteSystemSettingsPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_WRITE_SYSTEM_SETTINGS_PERMISSION, false);
    }

    /*
    static boolean getNotificationPolicyPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_POLICY_PERMISSION, false);
    }
    */

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

    /*
    static boolean getCallLogsPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.PERMISSIONS_STATUS_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_CALL_LOGS_PERMISSION, false);
    }
    */

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

    //---------------------

    static void grantRootX(final ProfilesPrefsFragment fragment, final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.phone_profiles_pref_grantRootPermission);
        dialogBuilder.setMessage(R.string.phone_profiles_pref_grantRootPermission_summary);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        //dialogBuilder.setView(doNotShowAgain);

        if (fragment != null) {
            AppCompatCheckBox doNotShowAgain = new AppCompatCheckBox(activity);

            FrameLayout container = new FrameLayout(activity);
            container.addView(doNotShowAgain);
            FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            containerParams.leftMargin = GlobalGUIRoutines.dpToPx(20);
            container.setLayoutParams(containerParams);

            FrameLayout superContainer = new FrameLayout(activity);
            superContainer.addView(container);

            dialogBuilder.setView(superContainer);

            doNotShowAgain.setText(R.string.alert_message_enable_event_check_box);
            doNotShowAgain.setChecked(ApplicationPreferences.applicationNeverAskForGrantRoot);
            doNotShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(activity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, isChecked);
                    editor.apply();
                    ApplicationPreferences.applicationNeverAskForGrantRoot(activity.getApplicationContext());
                }
            });
        }

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (fragment != null) {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(activity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                    editor.apply();
                    ApplicationPreferences.applicationNeverAskForGrantRoot(activity.getApplicationContext());

                    grantRootChanged = true;
                    fragment.setRedTextToPreferences();
                }

                boolean ok = false;
                PackageManager packageManager = activity.getPackageManager();
                // SuperSU
                Intent intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                if (intent != null) {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        // startActivityForResult not working, it is external application
                        activity.startActivity(intent/*, Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT*/);
                        PPApplication.initRoot();
                        ok = true;
                    } catch (Exception ignore) {
                    }
                }
                if (!ok) {
                    // MAGISK
                    intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            intent.putExtra("section", "superuser");
                            // startActivityForResult not working, it is external application
                            activity.startActivity(intent/*, Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT*/);
                            PPApplication.initRoot();
                            ok = true;
                        } catch (Exception ignore) {
                        }
                    }
                }
                if (!ok) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.phone_profiles_pref_grantRootPermission_otherManagers);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog2 = dialogBuilder.create();
                    /*dialog2.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button positive = ((AlertDialog)dialog2).getButton(DialogInterface.BUTTON_POSITIVE);
                            if (positive != null) positive.setAllCaps(false);
                            Button negative = ((AlertDialog)dialog2).getButton(DialogInterface.BUTTON_NEGATIVE);
                            if (negative != null) negative.setAllCaps(false);
                        }
                    });*/
                    if (!activity.isFinishing())
                        dialog2.show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (fragment != null) {
                    grantRootChanged = true;
                    fragment.setRedTextToPreferences();
                }
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!activity.isFinishing())
            dialog.show();
    }

    static void grantG1Permission(final ProfilesPrefsFragment fragment, final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.profile_preferences_types_G1_permission);
        dialogBuilder.setMessage(R.string.phone_profiles_pref_grantG1Permission_summary);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        //dialogBuilder.setView(doNotShowAgain);

        if (fragment != null) {
            AppCompatCheckBox doNotShowAgain = new AppCompatCheckBox(activity);

            FrameLayout container = new FrameLayout(activity);
            container.addView(doNotShowAgain);
            FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            containerParams.leftMargin = GlobalGUIRoutines.dpToPx(20);
            container.setLayoutParams(containerParams);

            FrameLayout superContainer = new FrameLayout(activity);
            superContainer.addView(container);

            dialogBuilder.setView(superContainer);

            doNotShowAgain.setText(R.string.alert_message_enable_event_check_box);
            doNotShowAgain.setChecked(ApplicationPreferences.applicationNeverAskForGrantG1Permission);
            doNotShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(activity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, isChecked);
                    editor.apply();
                    ApplicationPreferences.applicationNeverAskForGrantG1Permission(activity.getApplicationContext());
                }
            });
        }

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (fragment != null) {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(activity);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
                    editor.apply();
                    ApplicationPreferences.applicationNeverAskForGrantG1Permission(activity.getApplicationContext());

                    //grantRootChanged = true;
                    fragment.setRedTextToPreferences();
                }

                Intent intentLaunch = new Intent(activity, ImportantInfoActivity.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, 0);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SCROLL_TO, R.id.activity_info_notification_profile_grant_1_howTo_1);
                activity.startActivity(intentLaunch);
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (fragment != null) {
                    //grantRootChanged = true;
                    fragment.setRedTextToPreferences();
                }
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!activity.isFinishing())
            dialog.show();
    }

}
