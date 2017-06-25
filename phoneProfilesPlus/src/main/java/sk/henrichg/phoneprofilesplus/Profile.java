package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Profile {

    long _id;
    String _name;
    String _icon;
    boolean _checked;
    int _porder;
    int _duration;
    int _afterDurationDo;
    int _volumeRingerMode;
    int _volumeZenMode;
    String _volumeRingtone;
    String _volumeNotification;
    String _volumeMedia;
    String _volumeAlarm;
    String _volumeSystem;
    String _volumeVoice;
    int _soundRingtoneChange;
    String _soundRingtone;
    int _soundNotificationChange;
    String _soundNotification;
    int _soundAlarmChange;
    String _soundAlarm;
    int _deviceAirplaneMode;
    int _deviceMobileData;
    int _deviceMobileDataPrefs;
    int _deviceWiFi;
    int _deviceBluetooth;
    int _deviceGPS;
    int _deviceLocationServicePrefs;
    int _deviceScreenTimeout;
    String _deviceBrightness;
    int _deviceWallpaperChange;
    String _deviceWallpaper;
    int _deviceRunApplicationChange;
    String _deviceRunApplicationPackageName;
    int _deviceAutosync;
    boolean _showInActivator;
    int _deviceAutoRotate;
    int _volumeSpeakerPhone;
    int _deviceNFC;
    int _deviceKeyguard;
    int _vibrationOnTouch;
    int _deviceWiFiAP;
    int _devicePowerSaveMode;
    boolean _askForDuration;
    int _deviceNetworkType;
    int _notificationLed;
    int _vibrateWhenRinging;
    int _deviceWallpaperFor;
    boolean _hideStatusBarIcon;
    int _lockDevice;
    String _deviceConnectToSSID;
    int _applicationDisableWifiScanning;
    int _applicationDisableBluetoothScanning;

    Bitmap _iconBitmap;
    Bitmap _preferencesIndicator;

    static final String PREF_PROFILE_NAME = "prf_pref_profileName";
    static final String PREF_PROFILE_ICON = "prf_pref_profileIcon";
    static final String PREF_PROFILE_VOLUME_RINGER_MODE = "prf_pref_volumeRingerMode";
    static final String PREF_PROFILE_VOLUME_ZEN_MODE = "prf_pref_volumeZenMode";
    static final String PREF_PROFILE_VOLUME_RINGTONE = "prf_pref_volumeRingtone";
    static final String PREF_PROFILE_VOLUME_NOTIFICATION = "prf_pref_volumeNotification";
    static final String PREF_PROFILE_VOLUME_MEDIA = "prf_pref_volumeMedia";
    static final String PREF_PROFILE_VOLUME_ALARM = "prf_pref_volumeAlarm";
    static final String PREF_PROFILE_VOLUME_SYSTEM = "prf_pref_volumeSystem";
    static final String PREF_PROFILE_VOLUME_VOICE = "prf_pref_volumeVoice";
    static final String PREF_PROFILE_SOUND_RINGTONE_CHANGE = "prf_pref_soundRingtoneChange";
    static final String PREF_PROFILE_SOUND_RINGTONE = "prf_pref_soundRingtone";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_CHANGE = "prf_pref_soundNotificationChange";
    static final String PREF_PROFILE_SOUND_NOTIFICATION = "prf_pref_soundNotification";
    static final String PREF_PROFILE_SOUND_ALARM_CHANGE = "prf_pref_soundAlarmChange";
    static final String PREF_PROFILE_SOUND_ALARM = "prf_pref_soundAlarm";
    static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE = "prf_pref_deviceAirplaneMode";
    static final String PREF_PROFILE_DEVICE_WIFI = "prf_pref_deviceWiFi";
    static final String PREF_PROFILE_DEVICE_BLUETOOTH = "prf_pref_deviceBluetooth";
    static final String PREF_PROFILE_DEVICE_SCREEN_TIMEOUT = "prf_pref_deviceScreenTimeout";
    static final String PREF_PROFILE_DEVICE_BRIGHTNESS = "prf_pref_deviceBrightness";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_CHANGE = "prf_pref_deviceWallpaperChange";
    static final String PREF_PROFILE_DEVICE_WALLPAPER = "prf_pref_deviceWallpaper";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA = "prf_pref_deviceMobileData";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS = "prf_pref_deviceMobileDataPrefs";
    static final String PREF_PROFILE_DEVICE_GPS = "prf_pref_deviceGPS";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE = "prf_pref_deviceRunApplicationChange";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "prf_pref_deviceRunApplicationPackageName";
    static final String PREF_PROFILE_DEVICE_AUTOSYNC = "prf_pref_deviceAutosync";
    static final String PREF_PROFILE_SHOW_IN_ACTIVATOR = "prf_pref_showInActivator";
    static final String PREF_PROFILE_DEVICE_AUTOROTATE = "prf_pref_deviceAutoRotation";
    static final String PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS = "prf_pref_deviceLocationServicePrefs";
    static final String PREF_PROFILE_VOLUME_SPEAKER_PHONE = "prf_pref_volumeSpeakerPhone";
    static final String PREF_PROFILE_DEVICE_NFC = "prf_pref_deviceNFC";
    static final String PREF_PROFILE_DURATION = "prf_pref_duration";
    static final String PREF_PROFILE_AFTER_DURATION_DO = "prf_pref_afterDurationDo";
    static final String PREF_PROFILE_DEVICE_KEYGUARD = "prf_pref_deviceKeyguard";
    static final String PREF_PROFILE_VIBRATION_ON_TOUCH = "prf_pref_vibrationOnTouch";
    static final String PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS = "prf_pref_volumeUnlinkVolumesAppSettings";
    static final String PREF_PROFILE_DEVICE_WIFI_AP = "prf_pref_deviceWiFiAP";
    static final String PREF_PROFILE_DEVICE_POWER_SAVE_MODE = "prf_pref_devicePowerSaveMode";
    //static final String PREF_PROFILE_SHOW_DURATION_BUTTON = "prf_pref_showDurationButton";
    static final String PREF_PROFILE_ASK_FOR_DURATION = "prf_pref_askForDuration";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE = "prf_pref_deviceNetworkType";
    static final String PREF_PROFILE_NOTIFICATION_LED = "prf_pref_notificationLed";
    static final String PREF_PROFILE_VIBRATE_WHEN_RINGING = "prf_pref_vibrateWhenRinging";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_FOR = "prf_pref_deviceWallpaperFor";
    static final String PREF_PROFILE_HIDE_STATUS_BAR_ICON = "prf_pref_hideStatusBarIcon";
    static final String PREF_PROFILE_LOCK_DEVICE = "prf_pref_lockDevice";
    static final String PREF_PROFILE_DEVICE_CONNECT_TO_SSID = "prf_pref_deviceConnectToSSID";
    static final String PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING = "prf_pref_applicationDisableWifiScanning";
    static final String PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING = "prf_pref_applicationDisableBluetoothScanning";
    // no preferences, but checked from isProfilePreferenceAllowed
    static final String PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS = "prf_pref_deviceAdaptiveBrightness";

    static final int AFTERDURATIONDO_NOTHING = 0;
    static final int AFTERDURATIONDO_UNDOPROFILE = 1;
    static final int AFTERDURATIONDO_BACKGROUNPROFILE = 2;
    static final int AFTERDURATIONDO_RESTARTEVENTS = 3;

    static final int BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET = -99;

    static final String CONNECTTOSSID_JUSTANY = "^just_any^";
    static final String CONNECTTOSSID_DEFAULTPROFILE = "^default_profile^";

    static final long DEFAULT_PROFILE_ID = -999L;  // source profile id
    static final String PROFILE_ICON_DEFAULT = "ic_profile_default";
    static final long PROFILE_NO_ACTIVATE = -999;

    private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
    private static final String PREF_ACTIVATED_PROFILE_END_DURATION_TIME = "activatedProfileEndDurationTime";

    static final String[] profileIconId = {
            "ic_profile_default",

            "ic_profile_home", "ic_profile_home_2",

            "ic_profile_outdoors_1", "ic_profile_outdoors_2", "ic_profile_outdoors_3", "ic_profile_outdoors_4",
            "ic_profile_outdoors_5", "ic_profile_outdoors_6", "ic_profile_outdoors_7",

            "ic_profile_meeting", "ic_profile_meeting_2", "ic_profile_meeting_3", "ic_profile_mute", "ic_profile_mute_2",
            "ic_profile_volume_1", "ic_profile_volume_2", "ic_profile_volume_3",

            "ic_profile_work_1", "ic_profile_work_2", "ic_profile_work_3", "ic_profile_work_4", "ic_profile_work_5",
            "ic_profile_work_6", "ic_profile_work_7", "ic_profile_work_8", "ic_profile_work_9", "ic_profile_work_10",
            "ic_profile_work_11", "ic_profile_work_12",

            "ic_profile_sleep", "ic_profile_sleep_2", "ic_profile_night", "ic_profile_call_1", "ic_profile_food_1",
            "ic_profile_food_2","ic_profile_food_3","ic_profile_food_4","ic_profile_food_5",

            "ic_profile_car_1", "ic_profile_car_2", "ic_profile_car_3", "ic_profile_car_4", "ic_profile_car_5",
            "ic_profile_car_6", "ic_profile_car_7", "ic_profile_car_8", "ic_profile_car_9", "ic_profile_airplane_1",
            "ic_profile_airplane_2", "ic_profile_airplane_3", "ic_profile_tickets_1", "ic_profile_tickets_2",
            "ic_profile_tickets_3",

            "ic_profile_battery_1", "ic_profile_battery_2", "ic_profile_battery_3",

            "ic_profile_culture_1", "ic_profile_culture_2", "ic_profile_culture_3", "ic_profile_culture_4"
    };

    static final int[] profileIconColor = {
            0xff1c9cd7,

            0xff99cc00, 0xff99cc00,

            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,
            0xffffbc33, 0xffffbc33, 0xffffbc33,

            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,
            0xffcc0000, 0xffcc0000, 0xffcc0000,

            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff,

            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,
            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,

            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174,

            0xffdb3514, 0xffdb9714, 0xff2aa561,

            0xff38d043, 0xff38d043, 0xff38d043, 0xff38d043
    };

    // Empty constructorn
    Profile(){
        //this._useCustomColor = true;
        //this._customColor = Color.YELLOW;

        this._iconBitmap = null;
    }

    // constructor
    Profile(long id,
                   String name,
                   String icon,
                   Boolean checked,
                   int porder,
                   int volumeRingerMode,
                   String volumeRingtone,
                   String volumeNotification,
                   String volumeMedia,
                   String volumeAlarm,
                   String volumeSystem,
                   String volumeVoice,
                   int soundRingtoneChange,
                   String soundRingtone,
                   int soundNotificationChange,
                   String soundNotification,
                   int soundAlarmChange,
                   String soundAlarm,
                   int deviceAirplaneMode,
                   int deviceWiFi,
                   int deviceBluetooth,
                   int deviceScreenTimeout,
                   String deviceBrightness,
                   int deviceWallpaperChange,
                   String deviceWallpaper,
                   int deviceMobileData,
                   int deviceMobileDataPrefs,
                   int deviceGPS,
                   int deviceRunApplicationChange,
                   String deviceRunApplicationPackageName,
                   int deviceAutosync,
                   boolean showInActivator,
                   int deviceAutoRotate,
                   int deviceLocationServicePrefs,
                   int volumeSpeakerPhone,
                   int deviceNFC,
                   int duration,
                   int afterDurationDo,
                   int volumeZenMode,
                   int deviceKeyguard,
                   int vibrationOnTouch,
                   int deviceWifiAP,
                   int devicePowerSaveMode,
                   boolean askForDuration,
                   int deviceNetworkType,
                   int notificationLed,
                   int vibrateWhenRinging,
                   int deviceWallpaperFor,
                   boolean hideStatusBarIcon,
                   int lockDevice,
                   String deviceConnectToSSID,
                   int applicationDisableWifiScanning,
                   int applicationDisableBluetoothScanning)
    {
        this._id = id;
        this._name = name;
        this._icon = icon;
        this._checked = checked;
        this._porder = porder;
        this._volumeRingerMode = volumeRingerMode;
        this._volumeZenMode = volumeZenMode;
        this._volumeRingtone = volumeRingtone;
        this._volumeNotification = volumeNotification;
        this._volumeMedia = volumeMedia;
        this._volumeAlarm = volumeAlarm;
        this._volumeSystem = volumeSystem;
        this._volumeVoice = volumeVoice;
        this._soundRingtoneChange = soundRingtoneChange;
        this._soundRingtone = soundRingtone;
        this._soundNotificationChange = soundNotificationChange;
        this._soundNotification = soundNotification;
        this._soundAlarmChange = soundAlarmChange;
        this._soundAlarm = soundAlarm;
        this._deviceAirplaneMode = deviceAirplaneMode;
        this._deviceMobileData = deviceMobileData;
        this._deviceMobileDataPrefs = deviceMobileDataPrefs;
        this._deviceWiFi = deviceWiFi;
        this._deviceBluetooth = deviceBluetooth;
        this._deviceGPS = deviceGPS;
        this._deviceScreenTimeout = deviceScreenTimeout;
        this._deviceBrightness = deviceBrightness;
        this._deviceWallpaperChange = deviceWallpaperChange;
        this._deviceWallpaper = deviceWallpaper;
        this._deviceRunApplicationChange = deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = deviceRunApplicationPackageName;
        this._deviceAutosync = deviceAutosync;
        this._showInActivator = showInActivator;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._deviceKeyguard = deviceKeyguard;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWifiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._askForDuration = askForDuration;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._applicationDisableWifiScanning = applicationDisableWifiScanning;
        this._applicationDisableBluetoothScanning = applicationDisableBluetoothScanning;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
    }

    // constructor
    Profile(String name,
                   String icon,
                   Boolean checked,
                   int porder,
                   int volumeRingerMode,
                   String volumeRingtone,
                   String volumeNotification,
                   String volumeMedia,
                   String volumeAlarm,
                   String volumeSystem,
                   String volumeVoice,
                   int soundRingtoneChange,
                   String soundRingtone,
                   int soundNotificationChange,
                   String soundNotification,
                   int soundAlarmChange,
                   String soundAlarm,
                   int deviceAirplaneMode,
                   int deviceWiFi,
                   int deviceBluetooth,
                   int deviceScreenTimeout,
                   String deviceBrightness,
                   int deviceWallpaperChange,
                   String deviceWallpaper,
                   int deviceMobileData,
                   int deviceMobileDataPrefs,
                   int deviceGPS,
                   int deviceRunApplicationChange,
                   String deviceRunApplicationPackageName,
                   int deviceAutosync,
                   boolean showInActivator,
                   int deviceAutoRotate,
                   int deviceLocationServicePrefs,
                   int volumeSpeakerPhone,
                   int deviceNFC,
                   int duration,
                   int afterDurationDo,
                   int volumeZenMode,
                   int deviceKeyguard,
                   int vibrationOnTouch,
                   int deviceWiFiAP,
                   int devicePowerSaveMode,
                   boolean askForDuration,
                   int deviceNetworkType,
                   int notificationLed,
                   int vibrateWhenRinging,
                   int deviceWallpaperFor,
                   boolean hideStatusBarIcon,
                   int lockDevice,
                   String deviceConnectToSSID,
                   int applicationDisableWifiScanning,
                   int applicationDisableBluetoothScanning)
    {
        this._name = name;
        this._icon = icon;
        this._checked = checked;
        this._porder = porder;
        this._volumeRingerMode = volumeRingerMode;
        this._volumeZenMode = volumeZenMode;
        this._volumeRingtone = volumeRingtone;
        this._volumeNotification = volumeNotification;
        this._volumeMedia = volumeMedia;
        this._volumeAlarm = volumeAlarm;
        this._volumeSystem = volumeSystem;
        this._volumeVoice = volumeVoice;
        this._soundRingtoneChange = soundRingtoneChange;
        this._soundRingtone = soundRingtone;
        this._soundNotificationChange = soundNotificationChange;
        this._soundNotification = soundNotification;
        this._soundAlarmChange = soundAlarmChange;
        this._soundAlarm = soundAlarm;
        this._deviceAirplaneMode = deviceAirplaneMode;
        this._deviceMobileData = deviceMobileData;
        this._deviceMobileDataPrefs = deviceMobileDataPrefs;
        this._deviceWiFi = deviceWiFi;
        this._deviceBluetooth = deviceBluetooth;
        this._deviceGPS = deviceGPS;
        this._deviceScreenTimeout = deviceScreenTimeout;
        this._deviceBrightness = deviceBrightness;
        this._deviceWallpaperChange = deviceWallpaperChange;
        this._deviceWallpaper = deviceWallpaper;
        this._deviceRunApplicationChange = deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = deviceRunApplicationPackageName;
        this._deviceAutosync = deviceAutosync;
        this._showInActivator = showInActivator;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._askForDuration = askForDuration;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._applicationDisableWifiScanning = applicationDisableWifiScanning;
        this._applicationDisableBluetoothScanning = applicationDisableBluetoothScanning;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
    }

    void copyProfile(Profile profile)
    {
        this._id = profile._id;
        this._name = profile._name;
        this._icon = profile._icon;
        this._checked = profile._checked;
        this._porder = profile._porder;
        this._volumeRingerMode = profile._volumeRingerMode;
        this._volumeZenMode = profile._volumeZenMode;
        this._volumeRingtone = profile._volumeRingtone;
        this._volumeNotification = profile._volumeNotification;
        this._volumeMedia = profile._volumeMedia;
        this._volumeAlarm = profile._volumeAlarm;
        this._volumeSystem = profile._volumeSystem;
        this._volumeVoice = profile._volumeVoice;
        this._soundRingtoneChange = profile._soundRingtoneChange;
        this._soundRingtone = profile._soundRingtone;
        this._soundNotificationChange = profile._soundNotificationChange;
        this._soundNotification = profile._soundNotification;
        this._soundAlarmChange = profile._soundAlarmChange;
        this._soundAlarm = profile._soundAlarm;
        this._deviceAirplaneMode = profile._deviceAirplaneMode;
        this._deviceMobileData = profile._deviceMobileData;
        this._deviceMobileDataPrefs = profile._deviceMobileDataPrefs;
        this._deviceWiFi = profile._deviceWiFi;
        this._deviceBluetooth = profile._deviceBluetooth;
        this._deviceGPS = profile._deviceGPS;
        this._deviceScreenTimeout = profile._deviceScreenTimeout;
        this._deviceBrightness = profile._deviceBrightness;
        this._deviceWallpaperChange = profile._deviceWallpaperChange;
        this._deviceWallpaper = profile._deviceWallpaper;
        this._deviceRunApplicationChange = profile._deviceRunApplicationChange;
        this._deviceRunApplicationPackageName = profile._deviceRunApplicationPackageName;
        this._deviceAutosync = profile._deviceAutosync;
        this._showInActivator = profile._showInActivator;
        this._deviceAutoRotate = profile._deviceAutoRotate;
        this._deviceLocationServicePrefs = profile._deviceLocationServicePrefs;
        this._volumeSpeakerPhone = profile._volumeSpeakerPhone;
        this._deviceNFC = profile._deviceNFC;
        this._duration = profile._duration;
        this._afterDurationDo = profile._afterDurationDo;
        this._deviceKeyguard = profile._deviceKeyguard;
        this._vibrationOnTouch = profile._vibrationOnTouch;
        this._deviceWiFiAP = profile._deviceWiFiAP;
        this._devicePowerSaveMode = profile._devicePowerSaveMode;
        this._askForDuration = profile._askForDuration;
        this._deviceNetworkType = profile._deviceNetworkType;
        this._notificationLed = profile._notificationLed;
        this._vibrateWhenRinging = profile._vibrateWhenRinging;
        this._deviceWallpaperFor = profile._deviceWallpaperFor;
        this._hideStatusBarIcon = profile._hideStatusBarIcon;
        this._lockDevice = profile._lockDevice;
        this._deviceConnectToSSID = profile._deviceConnectToSSID;
        this._applicationDisableWifiScanning = profile._applicationDisableWifiScanning;
        this._applicationDisableBluetoothScanning = profile._applicationDisableBluetoothScanning;

        this._iconBitmap = profile._iconBitmap;
        this._preferencesIndicator = profile._preferencesIndicator;
    }

    void mergeProfiles(long withProfileId, DataWrapper dataWrapper)
    {
        PPApplication.logE("$$$ Profile.mergeProfiles","withProfileId="+withProfileId);

        Profile withProfile = dataWrapper.getProfileById(withProfileId, false);

        if (withProfile != null) {
            this._id = withProfile._id;
            this._name = withProfile._name;
            this._icon = withProfile._icon;
            this._iconBitmap = withProfile._iconBitmap;
            this._preferencesIndicator = withProfile._preferencesIndicator;
            this._duration = 0;
            this._afterDurationDo = AFTERDURATIONDO_RESTARTEVENTS;
            this._hideStatusBarIcon = withProfile._hideStatusBarIcon;
            this._deviceConnectToSSID = withProfile._deviceConnectToSSID;

            if (withProfile._volumeRingerMode != 0)
                this._volumeRingerMode = withProfile._volumeRingerMode;
            if (withProfile._volumeZenMode != 0)
                this._volumeZenMode = withProfile._volumeZenMode;
            if (withProfile.getVolumeRingtoneChange())
                this._volumeRingtone = withProfile._volumeRingtone;
            if (withProfile.getVolumeNotificationChange())
                this._volumeNotification = withProfile._volumeNotification;
            if (withProfile.getVolumeAlarmChange())
                this._volumeAlarm = withProfile._volumeAlarm;
            if (withProfile.getVolumeMediaChange())
                this._volumeMedia = withProfile._volumeMedia;
            if (withProfile.getVolumeSystemChange())
                this._volumeSystem = withProfile._volumeSystem;
            if (withProfile.getVolumeVoiceChange())
                this._volumeVoice = withProfile._volumeVoice;
            if (withProfile._soundRingtoneChange != 0) {
                this._soundRingtoneChange = withProfile._soundRingtoneChange;
                this._soundRingtone = withProfile._soundRingtone;
            }
            if (withProfile._soundNotificationChange != 0) {
                this._soundNotificationChange = withProfile._soundNotificationChange;
                this._soundNotification = withProfile._soundNotification;
            }
            if (withProfile._soundAlarmChange != 0) {
                this._soundAlarmChange = withProfile._soundAlarmChange;
                this._soundAlarm = withProfile._soundAlarm;
            }
            if (withProfile._deviceAirplaneMode != 0) {
                if (withProfile._deviceAirplaneMode != 3) // toggle
                    this._deviceAirplaneMode = withProfile._deviceAirplaneMode;
                else {
                    if (this._deviceAirplaneMode == 1)
                        this._deviceAirplaneMode = 2;
                    else if (this._deviceAirplaneMode == 2)
                        this._deviceAirplaneMode = 1;
                }
            }
            if (withProfile._deviceAutosync != 0) {
                if (withProfile._deviceAutosync != 3) // toggle
                    this._deviceAutosync = withProfile._deviceAutosync;
                else {
                    if (this._deviceAutosync == 1)
                        this._deviceAutosync = 2;
                    else if (this._deviceAutosync == 2)
                        this._deviceAutosync = 1;
                }
            }
            if (withProfile._deviceMobileData != 0) {
                if (withProfile._deviceMobileData != 3) // toggle
                    this._deviceMobileData = withProfile._deviceMobileData;
                else {
                    if (this._deviceMobileData == 1)
                        this._deviceMobileData = 2;
                    else if (this._deviceMobileData == 2)
                        this._deviceMobileData = 1;
                }
            }
            if (withProfile._deviceMobileDataPrefs != 0)
                this._deviceMobileDataPrefs = withProfile._deviceMobileDataPrefs;
            if (withProfile._deviceWiFi != 0) {
                if (withProfile._deviceWiFi != 3) // toggle
                    this._deviceWiFi = withProfile._deviceWiFi;
                else {
                    if (this._deviceWiFi == 1)
                        this._deviceWiFi = 2;
                    else if (this._deviceWiFi == 2)
                        this._deviceWiFi = 1;
                }
            }
            if (withProfile._deviceBluetooth != 0) {
                if (withProfile._deviceBluetooth != 3) // toggle
                    this._deviceBluetooth = withProfile._deviceBluetooth;
                else {
                    if (this._deviceBluetooth == 1)
                        this._deviceBluetooth = 2;
                    else if (this._deviceBluetooth == 2)
                        this._deviceBluetooth = 1;
                }
            }
            if (withProfile._deviceGPS != 0) {
                if (withProfile._deviceGPS != 3) // toggle
                    this._deviceGPS = withProfile._deviceGPS;
                else {
                    if (this._deviceGPS == 1)
                        this._deviceGPS = 2;
                    else if (this._deviceGPS == 2)
                        this._deviceGPS = 1;
                }
            }
            if (withProfile._deviceLocationServicePrefs != 0)
                this._deviceLocationServicePrefs = withProfile._deviceLocationServicePrefs;
            if (withProfile._deviceScreenTimeout != 0)
                this._deviceScreenTimeout = withProfile._deviceScreenTimeout;
            if (withProfile.getDeviceBrightnessChange())
                this._deviceBrightness = withProfile._deviceBrightness;
            if (withProfile._deviceAutoRotate != 0)
                this._deviceAutoRotate = withProfile._deviceAutoRotate;
            if (withProfile._deviceRunApplicationChange != 0) {
                this._deviceRunApplicationChange = 1;
                if (this._deviceRunApplicationPackageName.isEmpty())
                    this._deviceRunApplicationPackageName = withProfile._deviceRunApplicationPackageName;
                else
                    this._deviceRunApplicationPackageName = this._deviceRunApplicationPackageName + "|" +
                            withProfile._deviceRunApplicationPackageName;
            }
            if (withProfile._deviceWallpaperChange != 0) {
                this._deviceWallpaperChange = 1;
                this._deviceWallpaper = withProfile._deviceWallpaper;
                this._deviceWallpaperFor = withProfile._deviceWallpaperFor;
            }
            if (withProfile._volumeSpeakerPhone != 0)
                this._volumeSpeakerPhone = withProfile._volumeSpeakerPhone;
            if (withProfile._deviceNFC != 0) {
                if (withProfile._deviceNFC != 3) // toggle
                    this._deviceNFC = withProfile._deviceNFC;
                else {
                    if (this._deviceNFC == 1)
                        this._deviceNFC = 2;
                    else if (this._deviceNFC == 2)
                        this._deviceNFC = 1;
                }
            }
            if (withProfile._deviceKeyguard != 0)
                this._deviceKeyguard = withProfile._deviceKeyguard;
            if (withProfile._vibrationOnTouch != 0)
                this._vibrationOnTouch = withProfile._vibrationOnTouch;
            if (withProfile._deviceWiFiAP != 0) {
                if (withProfile._deviceWiFiAP != 3) // toggle
                    this._deviceWiFiAP = withProfile._deviceWiFiAP;
                else {
                    if (this._deviceWiFiAP == 1)
                        this._deviceWiFiAP = 2;
                    else if (this._deviceWiFiAP == 2)
                        this._deviceWiFiAP = 1;
                }
            }
            if (withProfile._devicePowerSaveMode != 0) {
                if (withProfile._devicePowerSaveMode != 3) // toggle
                    this._devicePowerSaveMode = withProfile._devicePowerSaveMode;
                else {
                    if (this._devicePowerSaveMode == 1)
                        this._devicePowerSaveMode = 2;
                    else if (this._devicePowerSaveMode == 2)
                        this._devicePowerSaveMode = 1;
                }
            }
            if (withProfile._deviceNetworkType != 0)
                this._deviceNetworkType = withProfile._deviceNetworkType;
            if (withProfile._notificationLed != 0)
                this._notificationLed = withProfile._notificationLed;
            if (withProfile._vibrateWhenRinging != 0)
                this._vibrateWhenRinging = withProfile._vibrateWhenRinging;
            if (withProfile._lockDevice != 0)
                this._lockDevice = withProfile._lockDevice;
            if (withProfile._applicationDisableWifiScanning != 0)
                this._applicationDisableWifiScanning = withProfile._applicationDisableWifiScanning;
            if (withProfile._applicationDisableBluetoothScanning != 0)
                this._applicationDisableBluetoothScanning = withProfile._applicationDisableBluetoothScanning;

            dataWrapper.getDatabaseHandler().activateProfile(withProfile);
            dataWrapper.setProfileActive(withProfile);

            String profileIcon = withProfile._icon;
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_PROFILEACTIVATION, null,
                                    dataWrapper.getProfileNameWithManualIndicator(withProfile, true, false, false),
                                    profileIcon, 0);
        }
    }

    // getting icon identifier
    public String getIconIdentifier()
    {
        String value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[0];
        } catch (Exception e) {
            value = "ic_profile_default";
        }
        return value;
    }

    // getting where icon is resource id
    public boolean getIsIconResourceID()
    {
        boolean value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[1].equals("1");

        } catch (Exception e) {
            value = true;
        }
        return value;
    }

    //gettig where icon has custom color
    boolean getUseCustomColorForIcon() {
        boolean value;
        try {
            String[] splits = _icon.split("\\|");
            value = splits[2].equals("1");

        } catch (Exception e) {
            value = false;
        }
        return value;
    }

    // geting icon custom color
    int getIconCustomColor() {
        int value;
        try {
            String[] splits = _icon.split("\\|");
            value = Integer.valueOf(splits[3]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    static int getVolumeRingtoneValue(String volumeRingtone)
    {
        int value;
        try {
            String[] splits = volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    int getVolumeRingtoneValue() {
        return getVolumeRingtoneValue(_volumeRingtone);
    }

    static boolean getVolumeRingtoneChange(String volumeRingtone)
    {
        int value;
        try {
            String[] splits = volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeRingtoneChange()
    {
        return getVolumeRingtoneChange(_volumeRingtone);
    }

    private boolean getVolumeRingtoneDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeRingtone.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    void setVolumeRingtoneValue(int value) {

        try {
            String[] splits = _volumeRingtone.split("\\|");
            splits[0] = String.valueOf(value);
            _volumeRingtone = "";
            for (String split : splits) {
                if (!_volumeRingtone.isEmpty())
                    _volumeRingtone = _volumeRingtone + "|";
                _volumeRingtone = _volumeRingtone + split;
            }
        } catch (Exception ignore) {
        }
    }

    int getVolumeNotificationValue()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeNotificationChange()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeNotificationDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    void setVolumeNotificationValue(int value) {

        try {
            String[] splits = _volumeNotification.split("\\|");
            splits[0] = String.valueOf(value);
            _volumeNotification = "";
            for (String split : splits) {
                if (!_volumeNotification.isEmpty())
                    _volumeNotification = _volumeNotification + "|";
                _volumeNotification = _volumeNotification + split;
            }
        } catch (Exception ignore) {
        }
    }

    int getVolumeMediaValue()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeMediaChange()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeMediaDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeAlarmValue()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeAlarmChange()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeAlarmDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeSystemValue()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeSystemChange()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeSystemDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeVoiceValue()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeVoiceChange()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getVolumeVoiceDefaultProfile()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getDeviceBrightnessValue()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getDeviceBrightnessChange()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    private boolean getDeviceBrightnessDefaultProfile()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[3]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    boolean getDeviceBrightnessAutomatic()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split("\\|");
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    private static int getMinimumScreenBrightnessSetting ()
    {
        /*final Resources res = Resources.getSystem();
        int id = res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"); // API17+
        if (id == 0)
            id = res.getIdentifier("config_screenBrightnessDim", "integer", "android"); // lower API levels
        if (id != 0)
        {
            try {
                return res.getInteger(id);
            }
            catch (Resources.NotFoundException e) {
                // ignore
            }
        }*/
        return 0;
    }

    private static int getMaximumScreenBrightnessSetting ()
    {
        /*final Resources res = Resources.getSystem();
        final int id = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");  // API17+
        if (id != 0)
        {
            try {
                int value = res.getInteger(id);
                if (value > 255)
                    value = 255;
                return value;
            }
            catch (Resources.NotFoundException e) {
                // ignore
            }
        }*/
        return 255;
    }

    static int convertPercentsToBrightnessManualValue(int perc, Context context)
    {
        int maximumValue = getMaximumScreenBrightnessSetting();
        int minimumValue = getMinimumScreenBrightnessSetting();

        //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "maximumValue="+maximumValue);
        //PPApplication.logE("Profile.convertPercentsToBrightnessManualValue", "minimumValue="+minimumValue);

        if (maximumValue-minimumValue > 255) {
            minimumValue = 0;
            maximumValue = 255;
        }

        int value;

        if (perc == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            // brightness is not set, change it to default manual brightness value
            value = Settings.System.getInt(context.getContentResolver(),
                                            Settings.System.SCREEN_BRIGHTNESS, 128);
        else
            value = Math.round((float)(maximumValue - minimumValue) / 100 * perc) + minimumValue;

        return value;
    }

    int getDeviceBrightnessManualValue(Context context)
    {
        int perc = getDeviceBrightnessValue();
        return convertPercentsToBrightnessManualValue(perc, context);
    }

    static float convertPercentsToBrightnessAdaptiveValue(int perc, Context context)
    {
        float value;

        if (perc == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            // brightness is not set, change it to default adaptive brightness value
            value = Settings.System.getFloat(context.getContentResolver(),
                                ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, 0f);
        else
            value = (perc - 50) / 50f;

        return value;
    }

    float getDeviceBrightnessAdaptiveValue(Context context)
    {
        int perc = getDeviceBrightnessValue();
        return convertPercentsToBrightnessAdaptiveValue(perc, context);
    }

    static long convertBrightnessToPercents(int value, int maxValue, int minValue)
    {
        long perc;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            perc = value; // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            perc = Math.round((float)(value-minValue) / (maxValue - minValue) * 100.0);

        return perc;
    }

    /*
    public void setDeviceBrightnessManualValue(int value)
    {
        int maxValue = getMaximumScreenBrightnessSetting();
        int minValue = getMinimumScreenBrightnessSetting();

        if (maxValue-minValue > 65535) {
            minValue = 0;
            maxValue = 65535;
        }

        long perc = convertBrightnessToPercents(value, maxValue, minValue);

        //value|noChange|automatic|defaultProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */
    /*
    public void setDeviceBrightnessAdaptiveValue(float value)
    {
        long perc;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            perc = Math.round(value); // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            perc = Math.round(value * 50 + 50);

        //value|noChange|automatic|defaultProfile
        String[] splits = _deviceBrightness.split("\\|");
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(perc)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */

    //----------------------------------

    void generateIconBitmap(Context context, boolean monochrome, int monochromeValue)
    {
        if (!getIsIconResourceID())
        {
            releaseIconBitmap();

            Resources resources = context.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Log.d("---- Profile.generateIconBitmap","resampleBitmapUri");
            _iconBitmap = BitmapManipulator.resampleBitmapUri(getIconIdentifier(), width, height, context);

            if (_iconBitmap == null)
            {
                // no icon found, set default icon
                _icon = "ic_profile_default|1|0|0";
                if (monochrome)
                {
                    int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue/*, context*/);
                    // getIsIconResourceID must return false
                    //_icon = getIconIdentifier() + "|0";
                }
            }
            else
            if (monochrome)
                _iconBitmap = BitmapManipulator.grayscaleBitmap(_iconBitmap);
            //_iconDrawable = null;
        }
        else
        if (monochrome)
        {
            Resources resources = context.getResources();
            int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue/*, context*/);
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
            /*Drawable drawable;
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                drawable = context.getResources().getDrawable(iconResource, context.getTheme());
            } else {
                drawable = context.getResources().getDrawable(iconResource);
            }
            _iconDrawable = BitmapManipulator.tintDrawableByValue(drawable, monochromeValue);
            _iconBitmap = null;*/
        }
        else
        if (getUseCustomColorForIcon()) {
            Resources resources = context.getResources();
            int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.getPackageName());
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            _iconBitmap = BitmapManipulator.recolorBitmap(bitmap, getIconCustomColor()/*, context*/);
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
        }
        else
            _iconBitmap = null;
    }

    void generatePreferencesIndicator(Context context, boolean monochrome, int monochromeValue)
    {
        releasePreferencesIndicator();

        _preferencesIndicator = ProfilePreferencesIndicator.paint(this, context);
        if (_preferencesIndicator != null) {
            if (monochrome)
                _preferencesIndicator = BitmapManipulator.monochromeBitmap(_preferencesIndicator, monochromeValue/*, context*/);
        }
    }

    void releaseIconBitmap()
    {
        if (_iconBitmap != null)
        {
            //if (!_iconBitmap.isRecycled())
            //    _iconBitmap.recycle();
            _iconBitmap = null;
        }
    }

    void releasePreferencesIndicator()
    {
        if (_preferencesIndicator != null)
        {
            //if (!_preferencesIndicator.isRecycled())
            //    _preferencesIndicator.recycle();
            _preferencesIndicator = null;
        }
    }

    String getProfileNameWithDuration(boolean multyline, Context context) {
        String profileName = _name;
        if ((_duration > 0) && (_afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
            boolean showEndTime = false;
            if (_checked) {
                long endDurationTime = getActivatedProfileEndDurationTime(context);
                if (endDurationTime > 0) {
                    if (multyline)
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ")\n" + profileName;
                    else
                        profileName = "(de:" + timeDateStringFromTimestamp(context, endDurationTime) + ") " + profileName;
                    showEndTime = true;
                }
            }
            if (!showEndTime) {
                //profileName = "[" + _duration + "] " + profileName;
                if (multyline)
                    profileName = "[" + GlobalGUIRoutines.getDurationString(_duration) + "]\n" + profileName;
                else
                    profileName = "[" + GlobalGUIRoutines.getDurationString(_duration) + "] " + profileName;
            }
        }
        return profileName;
    }

    @SuppressLint("SimpleDateFormat")
    private static String timeDateStringFromTimestamp(Context applicationContext, long timestamp){
        String timeDate;
        String timestampDate = android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp));
        Calendar calendar = Calendar.getInstance();
        String currentDate = android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(calendar.getTimeInMillis()));
        String androidDateTime;
        if (timestampDate.equals(currentDate))
            androidDateTime=android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        else
            androidDateTime=android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp))+" "+
                    android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm="";
        if(!Character.isDigit(androidDateTime.charAt(androidDateTime.length()-1))) {
            if(androidDateTime.contains(new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])){
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM];
            }else{
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM];
            }
            androidDateTime=androidDateTime.replace(AmPm, "");
        }
        if(!Character.isDigit(javaDateTime.charAt(javaDateTime.length()-1))){
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM], "");
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime=javaDateTime.substring(javaDateTime.length()-3);
        timeDate=androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    private static String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    static Profile getDefaultProfile(Context context)
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int	maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int	maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int	maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int	maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int	maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int	maximumValueVoicecall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.DEFAULT_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);

        Profile profile = new Profile();
        profile._id = Profile.DEFAULT_PROFILE_ID;
        profile._name = context.getResources().getString(R.string.default_profile_name);
        profile._icon = Profile.PROFILE_ICON_DEFAULT+"1|0|0";
        profile._checked = false;
        profile._porder = 0;
        profile._duration = 0;
        profile._afterDurationDo = Profile.AFTERDURATIONDO_RESTARTEVENTS;
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_RINGER_MODE, "1")); // ring
        profile._volumeZenMode = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_ZEN_MODE, "1")); // all
        profile._volumeRingtone = preferences.getString(PREF_PROFILE_VOLUME_RINGTONE, getVolumeLevelString(71, maximumValueRing) + "|0|0");
        profile._volumeNotification = preferences.getString(PREF_PROFILE_VOLUME_NOTIFICATION, getVolumeLevelString(86, maximumValueNotification)+"|0|0");
        profile._volumeMedia = preferences.getString(PREF_PROFILE_VOLUME_MEDIA, getVolumeLevelString(80, maximumValueMusic)+"|0|0");
        profile._volumeAlarm = preferences.getString(PREF_PROFILE_VOLUME_ALARM, getVolumeLevelString(100, maximumValueAlarm)+"|0|0");
        profile._volumeSystem = preferences.getString(PREF_PROFILE_VOLUME_SYSTEM, getVolumeLevelString(70, maximumValueSystem)+"|0|0");
        profile._volumeVoice = preferences.getString(PREF_PROFILE_VOLUME_VOICE, getVolumeLevelString(70, maximumValueVoicecall)+"|0|0");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
        profile._soundRingtone = preferences.getString(PREF_PROFILE_SOUND_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI.toString());
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
        profile._soundNotification = preferences.getString(PREF_PROFILE_SOUND_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
        profile._soundAlarm = preferences.getString(PREF_PROFILE_SOUND_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AIRPLANE_MODE, "2")); // OFF
        profile._deviceWiFi = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WIFI, "2")); // OFF
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_BLUETOOTH, "2")); //OFF
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "2")); // 30 seconds
        profile._deviceBrightness = preferences.getString(PREF_PROFILE_DEVICE_BRIGHTNESS, Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|0|1|0");  // automatic on
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        profile._deviceWallpaper = preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER, "-");
        profile._deviceMobileData = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_MOBILE_DATA, "1")); //ON
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
        profile._deviceGPS = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_GPS, "2")); //OFF
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
        profile._deviceRunApplicationPackageName = preferences.getString(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        profile._deviceAutosync = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AUTOSYNC, "1")); // ON
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_AUTOROTATE, "1")); // ON
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0"));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
        profile._deviceNFC = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_NFC, "0"));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_KEYGUARD, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WIFI_AP, "2")); // OFF
        profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_POWER_SAVE_MODE, "0"));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
        profile._notificationLed = Integer.parseInt(preferences.getString(PREF_PROFILE_NOTIFICATION_LED, "0"));
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
        profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(PREF_PROFILE_DEVICE_WALLPAPER_FOR, "0"));
        profile._lockDevice = Integer.parseInt(preferences.getString(PREF_PROFILE_LOCK_DEVICE, "0"));
        profile._deviceConnectToSSID = preferences.getString(PREF_PROFILE_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
        profile._applicationDisableWifiScanning = Integer.parseInt(preferences.getString(PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, "0"));
        profile._applicationDisableBluetoothScanning = Integer.parseInt(preferences.getString(PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, "0"));

        return profile;
    }

    static public Profile getMappedProfile(Profile profile, Context context)
    {
        if (profile != null)
        {
            Profile defaultProfile = getDefaultProfile(context);

            Profile mappedProfile = new Profile(
                    profile._id,
                    profile._name,
                    profile._icon,
                    profile._checked,
                    profile._porder,
                    profile._volumeRingerMode,
                    profile._volumeRingtone,
                    profile._volumeNotification,
                    profile._volumeMedia,
                    profile._volumeAlarm,
                    profile._volumeSystem,
                    profile._volumeVoice,
                    profile._soundRingtoneChange,
                    profile._soundRingtone,
                    profile._soundNotificationChange,
                    profile._soundNotification,
                    profile._soundAlarmChange,
                    profile._soundAlarm,
                    profile._deviceAirplaneMode,
                    profile._deviceWiFi,
                    profile._deviceBluetooth,
                    profile._deviceScreenTimeout,
                    profile._deviceBrightness,
                    profile._deviceWallpaperChange,
                    profile._deviceWallpaper,
                    profile._deviceMobileData,
                    profile._deviceMobileDataPrefs,
                    profile._deviceGPS,
                    profile._deviceRunApplicationChange,
                    profile._deviceRunApplicationPackageName,
                    profile._deviceAutosync,
                    profile._showInActivator,
                    profile._deviceAutoRotate,
                    profile._deviceLocationServicePrefs,
                    profile._volumeSpeakerPhone,
                    profile._deviceNFC,
                    profile._duration,
                    profile._afterDurationDo,
                    profile._volumeZenMode,
                    profile._deviceKeyguard,
                    profile._vibrationOnTouch,
                    profile._deviceWiFiAP,
                    profile._devicePowerSaveMode,
                    profile._askForDuration,
                    profile._deviceNetworkType,
                    profile._notificationLed,
                    profile._vibrateWhenRinging,
                    profile._deviceWallpaperFor,
                    profile._hideStatusBarIcon,
                    profile._lockDevice,
                    profile._deviceConnectToSSID,
                    profile._applicationDisableWifiScanning,
                    profile._applicationDisableBluetoothScanning);

            boolean zenModeMapped = false;
            if (profile._volumeRingerMode == 99) {
                mappedProfile._volumeRingerMode = defaultProfile._volumeRingerMode;
                if (mappedProfile._volumeRingerMode == 5) {
                    mappedProfile._volumeZenMode = defaultProfile._volumeZenMode;
                    zenModeMapped = true;
                }
            }
            if ((profile._volumeZenMode == 99) && (!zenModeMapped))
                mappedProfile._volumeZenMode = defaultProfile._volumeZenMode;
            if (profile.getVolumeRingtoneDefaultProfile())
                mappedProfile._volumeRingtone = defaultProfile._volumeRingtone;
            if (profile.getVolumeNotificationDefaultProfile())
                mappedProfile._volumeNotification = defaultProfile._volumeNotification;
            if (profile.getVolumeAlarmDefaultProfile())
                mappedProfile._volumeAlarm = defaultProfile._volumeAlarm;
            if (profile.getVolumeMediaDefaultProfile())
                mappedProfile._volumeMedia = defaultProfile._volumeMedia;
            if (profile.getVolumeSystemDefaultProfile())
                mappedProfile._volumeSystem = defaultProfile._volumeSystem;
            if (profile.getVolumeVoiceDefaultProfile())
                mappedProfile._volumeVoice = defaultProfile._volumeVoice;
            if (profile._soundRingtoneChange == 99)
            {
                mappedProfile._soundRingtoneChange = defaultProfile._soundRingtoneChange;
                mappedProfile._soundRingtone = defaultProfile._soundRingtone;
            }
            if (profile._soundNotificationChange == 99)
            {
                mappedProfile._soundNotificationChange = defaultProfile._soundNotificationChange;
                mappedProfile._soundNotification = defaultProfile._soundNotification;
            }
            if (profile._soundAlarmChange == 99)
            {
                mappedProfile._soundAlarmChange = defaultProfile._soundAlarmChange;
                mappedProfile._soundAlarm = defaultProfile._soundAlarm;
            }
            if (profile._deviceAirplaneMode == 99)
                mappedProfile._deviceAirplaneMode = defaultProfile._deviceAirplaneMode;
            if (profile._deviceAutosync == 99)
                mappedProfile._deviceAutosync = defaultProfile._deviceAutosync;
            if (profile._deviceMobileData == 99)
                mappedProfile._deviceMobileData = defaultProfile._deviceMobileData;
            if (profile._deviceMobileDataPrefs == 99)
                mappedProfile._deviceMobileDataPrefs = defaultProfile._deviceMobileDataPrefs;
            if (profile._deviceWiFi == 99)
                mappedProfile._deviceWiFi = defaultProfile._deviceWiFi;
            if (profile._deviceBluetooth == 99)
                mappedProfile._deviceBluetooth = defaultProfile._deviceBluetooth;
            if (profile._deviceGPS == 99)
                mappedProfile._deviceGPS = defaultProfile._deviceGPS;
            if (profile._deviceLocationServicePrefs == 99)
                mappedProfile._deviceLocationServicePrefs = defaultProfile._deviceLocationServicePrefs;
            if (profile._deviceScreenTimeout == 99)
                mappedProfile._deviceScreenTimeout = defaultProfile._deviceScreenTimeout;
            if (profile.getDeviceBrightnessDefaultProfile())
                mappedProfile._deviceBrightness = defaultProfile._deviceBrightness;
            if (profile._deviceAutoRotate == 99)
                mappedProfile._deviceAutoRotate = defaultProfile._deviceAutoRotate;
            if (profile._deviceRunApplicationChange == 99)
            {
                mappedProfile._deviceRunApplicationChange = defaultProfile._deviceRunApplicationChange;
                mappedProfile._deviceRunApplicationPackageName = defaultProfile._deviceRunApplicationPackageName;
            }
            if (profile._deviceWallpaperChange == 99)
            {
                mappedProfile._deviceWallpaperChange = defaultProfile._deviceWallpaperChange;
                mappedProfile._deviceWallpaper = defaultProfile._deviceWallpaper;
                mappedProfile._deviceWallpaperFor = defaultProfile._deviceWallpaperFor;
            }
            if (profile._volumeSpeakerPhone == 99)
                mappedProfile._volumeSpeakerPhone = defaultProfile._volumeSpeakerPhone;
            if (profile._deviceNFC == 99)
                mappedProfile._deviceNFC = defaultProfile._deviceNFC;
            if (profile._deviceKeyguard == 99)
                mappedProfile._deviceKeyguard = defaultProfile._deviceKeyguard;
            if (profile._vibrationOnTouch == 99)
                mappedProfile._vibrationOnTouch = defaultProfile._vibrationOnTouch;
            if (profile._deviceWiFiAP == 99)
                mappedProfile._deviceWiFiAP = defaultProfile._deviceWiFiAP;
            if (profile._devicePowerSaveMode == 99)
                mappedProfile._devicePowerSaveMode = defaultProfile._devicePowerSaveMode;
            if (profile._deviceNetworkType == 99)
                mappedProfile._deviceNetworkType = defaultProfile._deviceNetworkType;
            if (profile._notificationLed == 99)
                mappedProfile._notificationLed = defaultProfile._notificationLed;
            if (profile._vibrateWhenRinging == 99)
                mappedProfile._vibrateWhenRinging = defaultProfile._vibrateWhenRinging;
            if (profile._lockDevice == 99)
                mappedProfile._lockDevice = defaultProfile._lockDevice;
            if ((profile._deviceConnectToSSID != null) && (profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_DEFAULTPROFILE)))
                mappedProfile._deviceConnectToSSID = defaultProfile._deviceConnectToSSID;
            if (profile._applicationDisableWifiScanning == 99)
                mappedProfile._applicationDisableWifiScanning = defaultProfile._applicationDisableWifiScanning;
            if (profile._applicationDisableBluetoothScanning == 99)
                mappedProfile._applicationDisableBluetoothScanning = defaultProfile._applicationDisableBluetoothScanning;

            mappedProfile._iconBitmap = profile._iconBitmap;
            mappedProfile._preferencesIndicator = profile._preferencesIndicator;

            return mappedProfile;
        }
        else
            return null;
    }

    static int isProfilePreferenceAllowed(String preferenceKey, Context context)
    {
        int featurePresented = PPApplication.PREFERENCE_NOT_ALLOWED;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 17)
            {
                if (PPApplication.isRooted())
                {
                    // device is rooted
                    if (PPApplication.settingsBinaryExists())
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device has Wifi
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                // device has bluetooth
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA))
        {
            boolean mobileDataSupported = false;
            if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Network[] networks = cm.getAllNetworks();
                    for (Network network : networks) {
                        NetworkInfo ni = cm.getNetworkInfo(network);
                        Log.d("Profile.isProfilePreferenceAllowed", "ni.getType()="+ni.getType());
                        if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                            Log.d("Profile.isProfilePreferenceAllowed", "network type = mobile data");
                            mobileDataSupported = true;
                            break;
                        }
                    }
                }
                else {*/
                    //noinspection deprecation
                    NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    mobileDataSupported = ni != null;
                //}
            }
            else
                mobileDataSupported = true;
            if (mobileDataSupported)
            {
                //Log.d("Profile.isProfilePreferenceAllowed", "mobile data supported");
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
                    // not working :-/
                    if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
                        if (ActivateProfileHelper.canSetMobileData(context))
                            featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    }
                    else
                    if (PPApplication.isRooted()) {
                        // zariadenie je rootnute
                        //if (serviceBinaryExists() && telephonyServiceExists(context, PREF_PROFILE_DEVICE_MOBILE_DATA))
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    }
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                {
                    if (ActivateProfileHelper.canSetMobileData(context))
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else {
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
            }
            else {
                //Log.d("Profile.isProfilePreferenceAllowed", "mobile data not supported");
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            }
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_GPS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
            {
                // device has gps
                // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    if (ActivateProfileHelper.canSetMobileData(context))
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted())
                {
                    // device is rooted
                    if (PPApplication.settingsBinaryExists())
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                if (ActivateProfileHelper.canExploitGPS(context))
                {
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
                }
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NFC))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
            {
                PPApplication.logE("PPApplication.hardwareCheck","NFC=presented");

                // device has nfc
                if (PPApplication.isRooted())
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            {
                PPApplication.logE("PPApplication.hardwareCheck","NFC=not presented");
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                // device has Wifi
                if (WifiApManager.canExploitWifiAP(context))
                {
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
                }
                else {
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
        {
            if (android.os.Build.VERSION.SDK_INT == 23) {
                if (PPApplication.isRooted()) {
                    // device is rooted
                    if (PPApplication.settingsBinaryExists())
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                {
                    /* not working (private secure settings) :-/
                    if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    }
                    else {*/
                        if (PPApplication.isRooted()) {
                            // device is rooted
                            if (PPApplication.settingsBinaryExists())
                                featurePresented = PPApplication.PREFERENCE_ALLOWED;
                            else
                                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                        } else
                            PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                    //}
                }
                else
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
            }
            else {
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
                }
                else
                if (PPApplication.isRooted()) {
                    // device is rooted
                    if (PPApplication.settingsBinaryExists())
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else {
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final int phoneType = telephonyManager.getPhoneType();
                if ((phoneType == TelephonyManager.PHONE_TYPE_GSM) || (phoneType == TelephonyManager.PHONE_TYPE_CDMA)) {
                    if (PPApplication.isRooted()) {
                        // device is rooted
                        if (ActivateProfileHelper.telephonyServiceExists(context, Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (PPApplication.serviceBinaryExists())
                                featurePresented = PPApplication.PREFERENCE_ALLOWED;
                            else
                                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        }
                        else {
                            PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                        }
                    }
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else {
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            int value = Settings.System.getInt(context.getContentResolver(), "notification_light_pulse", -10);
            if ((value != -10) && (android.os.Build.VERSION.SDK_INT >= 23)) {
                /* not working (private secure settings) :-/
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    featurePresented = PPApplication.PREFERENCE_ALLOWED;
                }
                else*/
                if (PPApplication.isRooted()) {
                    // device is rooted
                    if (PPApplication.settingsBinaryExists())
                        featurePresented = PPApplication.PREFERENCE_ALLOWED;
                    else
                        PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            if (value != -10)
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else {
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            boolean secureKeyguard;
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                secureKeyguard = keyguardManager.isKeyguardSecure();
            else
                secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
            if (secureKeyguard) {
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
            }
            else
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device has Wifi
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device has Wifi
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                // device has bluetooth
                featurePresented = PPApplication.PREFERENCE_ALLOWED;
            else
                PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
            featurePresented = PPApplication.PREFERENCE_ALLOWED;

        return featurePresented;
    }

    static long getActivatedProfileForDuration(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, 0);
    }

    static void setActivatedProfileForDuration(Context context, long profileId)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, profileId);
        editor.apply();
    }

    private static long getActivatedProfileEndDurationTime(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, 0);
    }

    static void setActivatedProfileEndDurationTime(Context context, long time)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, time);
        editor.apply();
    }


}
