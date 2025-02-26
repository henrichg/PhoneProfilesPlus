package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.ArrayMap;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import java.util.Calendar;

/** @noinspection ExtractMethodRecommender*/
class Profile {

    long _id;
    String _name;
    String _icon;
    boolean _checked;
    int _porder;
    int _duration;
    int _afterDurationDo;
    boolean _askForDuration;
    String _durationNotificationSound;
    boolean _durationNotificationVibrate;
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
    int _deviceAutoSync;
    boolean _showInActivator;
    int _deviceAutoRotate;
    int _volumeSpeakerPhone;
    int _deviceNFC;
    int _deviceKeyguard;
    int _vibrationOnTouch;
    int _deviceWiFiAP;
    int _devicePowerSaveMode;
    int _deviceNetworkType;
    int _notificationLed;
    int _vibrateWhenRinging;
    int _deviceWallpaperFor;
    boolean _hideStatusBarIcon;
    int _lockDevice;
    String _deviceConnectToSSID;
    int _applicationEnableWifiScanning;
    int _applicationEnableBluetoothScanning;
    int _deviceWiFiAPPrefs;
    int _applicationEnableLocationScanning;
    int _applicationEnableMobileCellScanning;
    int _applicationEnableOrientationScanning;
    int _headsUpNotifications;
    int _deviceForceStopApplicationChange;
    String _deviceForceStopApplicationPackageName;
    long _activationByUserCount;
    int _deviceNetworkTypePrefs;
    int _deviceCloseAllApplications;
    int _screenDarkMode;
    int _dtmfToneWhenDialing;
    int _soundOnTouch;
    String _volumeDTMF;
    String _volumeAccessibility;
    String _volumeBluetoothSCO;
    long _afterDurationProfile;
    int _alwaysOnDisplay;
    int _screenOnPermanent;
    boolean _volumeMuteSound;
    int _deviceLocationMode;
    int _applicationEnableNotificationScanning;
    String _generateNotification;
    int _cameraFlash;
    int _deviceNetworkTypeSIM1;
    int _deviceNetworkTypeSIM2;
    //int _deviceMobileDataSIM1;
    //int _deviceMobileDataSIM2;
    String _deviceDefaultSIMCards;
    int _deviceOnOffSIM1;
    int _deviceOnOffSIM2;
    int _soundRingtoneChangeSIM1;
    String _soundRingtoneSIM1;
    int _soundRingtoneChangeSIM2;
    String _soundRingtoneSIM2;
    int _soundNotificationChangeSIM1;
    String _soundNotificationSIM1;
    int _soundNotificationChangeSIM2;
    String _soundNotificationSIM2;
    int _soundSameRingtoneForBothSIMCards;
    String _deviceLiveWallpaper;
    int _vibrateNotifications;
    String _deviceWallpaperFolder;
    int _applicationDisableGloabalEventsRun;
    int _deviceVPNSettingsPrefs;
    int _endOfActivationType;
    int _endOfActivationTime;
    int _applicationEnablePeriodicScanning;
    String _deviceVPN;
    String _vibrationIntensityRinging;
    String _vibrationIntensityNotifications;
    String _vibrationIntensityTouchInteraction;
    boolean _volumeMediaChangeDuringPlay;
    int _applicationWifiScanInterval;
    int _applicationBluetoothScanInterval;
    int _applicationBluetoothLEScanDuration;
    int _applicationLocationScanInterval;
    int _applicationOrientationScanInterval;
    int _applicationPeriodicScanInterval;
    String _sendSMSContacts; // contactId#phoneId|...
    String _sendSMSContactGroups; // groupId|...
    //int _sendSMSContactListType;
    boolean _sendSMSSendSMS;
    String _sendSMSSMSText;
    String _deviceWallpaperLockScreen;
    boolean _clearNotificationEnabled;
    String _clearNotificationApplications;
    boolean _clearNotificationCheckContacts;
    String _clearNotificationContacts; // contactId#phoneId|...
    String _clearNotificationContactGroups; // groupId|...
    boolean _clearNotificationCheckText;
    String _clearNotificationText;
    int _screenNightLight;
    int _screenNightLightPrefs;
    int _screenOnOff;

    Bitmap _iconBitmap;
    Bitmap _preferencesIndicator;
    //int _ringerModeForZenMode;

    static final long PROFILE_NO_ACTIVATE = -999;
    static final long RESTART_EVENTS_PROFILE_ID = -888L;
    //static final long SHARED_PROFILE_ID = -999L;
    //static final int SHARED_PROFILE_VALUE = 99;
    //static final String SHARED_PROFILE_VALUE_STR = "99";

    //private static final String PREF_PROFILE_ID = "prf_pref_id";
    static final String PREF_PROFILE_NAME = "prf_pref_profileName";
    static final String PREF_PROFILE_ICON = "prf_pref_profileIcon";
    static final String PREF_PROFILE_ICON_WITHOUT_ICON = "prf_pref_profileIcon_withoutIcon";
    //private static final String PREF_PROFILE_CHECKED = "prf_pref_checked";
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
    static final String PREF_PROFILE_DEVICE_BRIGHTNESS_WITHOUT_LEVEL = "prf_pref_deviceBrightness_withoutLevel";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_CHANGE = "prf_pref_deviceWallpaperChange";
    static final String PREF_PROFILE_DEVICE_WALLPAPER = "prf_pref_deviceWallpaper";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA = "prf_pref_deviceMobileData";
    static final String PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS = "prf_pref_deviceMobileDataPrefs";
    static final String PREF_PROFILE_DEVICE_GPS = "prf_pref_deviceGPS";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE = "prf_pref_deviceRunApplicationChange";
    static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "prf_pref_deviceRunApplicationPackageName";
    static final String PREF_PROFILE_DEVICE_AUTOSYNC = "prf_pref_deviceAutosync";
    static final String PREF_PROFILE_SHOW_IN_ACTIVATOR = "prf_pref_showInActivator";
    static final String PREF_PROFILE_SHOW_IN_ACTIVATOR_NOT_SHOW = "prf_pref_showInActivator_notShow";
    static final String PREF_PROFILE_DEVICE_AUTOROTATE = "prf_pref_deviceAutoRotation";
    static final String PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS = "prf_pref_deviceLocationServicePrefs";
    static final String PREF_PROFILE_VOLUME_SPEAKER_PHONE = "prf_pref_volumeSpeakerPhone";
    static final String PREF_PROFILE_DEVICE_NFC = "prf_pref_deviceNFC";
    static final String PREF_PROFILE_DURATION = "prf_pref_duration";
    static final String PREF_PROFILE_AFTER_DURATION_DO = "prf_pref_afterDurationDo";
    static final String PREF_PROFILE_ASK_FOR_DURATION = "prf_pref_askForDuration";
    static final String PREF_PROFILE_DURATION_NOTIFICATION_SOUND = "prf_pref_durationNotificationSound";
    static final String PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE = "prf_pref_durationNotificationVibrate";
    static final String PREF_PROFILE_DEVICE_KEYGUARD = "prf_pref_deviceKeyguard";
    static final String PREF_PROFILE_VIBRATION_ON_TOUCH = "prf_pref_vibrationOnTouch";
    static final String PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS = "prf_pref_volumeUnlinkVolumesAppSettings";
    static final String PREF_PROFILE_DEVICE_WIFI_AP = "prf_pref_deviceWiFiAP";
    static final String PREF_PROFILE_DEVICE_POWER_SAVE_MODE = "prf_pref_devicePowerSaveMode";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE = "prf_pref_deviceNetworkType";
    static final String PREF_PROFILE_NOTIFICATION_LED = "prf_pref_notificationLed";
    static final String PREF_PROFILE_VIBRATE_WHEN_RINGING = "prf_pref_vibrateWhenRinging";
    static final String PREF_PROFILE_VIBRATE_NOTIFICATIONS = "prf_pref_vibrateNotifications";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_FOR = "prf_pref_deviceWallpaperFor";
    static final String PREF_PROFILE_HIDE_STATUS_BAR_ICON = "prf_pref_hideStatusBarIcon";
    static final String PREF_PROFILE_LOCK_DEVICE = "prf_pref_lockDevice";
    static final String PREF_PROFILE_DEVICE_CONNECT_TO_SSID = "prf_pref_deviceConnectToSSID";
    static final String PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING = "prf_pref_applicationEnableWifiScanning";
    static final String PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING = "prf_pref_applicationEnableBluetoothScanning";
    //static final String PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS = "prf_pref_deviceAdaptiveBrightness";
    static final String PREF_PROFILE_DEVICE_WIFI_AP_PREFS = "prf_pref_deviceWiFiAPPrefs";
    static final String PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING = "prf_pref_applicationEnableLocationScanning";
    static final String PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING = "prf_pref_applicationEnableMobileCellScanning";
    static final String PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING = "prf_pref_applicationEnableOrientationScanning";
    static final String PREF_PROFILE_HEADS_UP_NOTIFICATIONS = "prf_pref_headsUpNotifications";
    static final String PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE = "prf_pref_deviceForceStopApplicationChange";
    static final String PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = "prf_pref_deviceForceStopApplicationPackageName";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS = "prf_pref_deviceNetworkTypePrefs";
    static final String PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS = "prf_pref_deviceCloseAllApplications";
    static final String PREF_PROFILE_SCREEN_DARK_MODE = "prf_pref_screenDarkMode";
    static final String PREF_PROFILE_DTMF_TONE_WHEN_DIALING = "prf_pref_dtmfToneWhenDialing";
    static final String PREF_PROFILE_SOUND_ON_TOUCH = "prf_pref_soundOnTouch";
    static final String PREF_PROFILE_VOLUME_DTMF = "prf_pref_volumeDTMF";
    static final String PREF_PROFILE_VOLUME_ACCESSIBILITY = "prf_pref_volumeAccessibility";
    static final String PREF_PROFILE_VOLUME_BLUETOOTH_SCO = "prf_pref_volumeBluetoothSCO";
    static final String PREF_PROFILE_AFTER_DURATION_PROFILE = "prf_pref_afterDurationProfile";
    static final String PREF_PROFILE_ALWAYS_ON_DISPLAY = "prf_pref_alwaysOnDisplay";
    static final String PREF_PROFILE_SCREEN_ON_PERMANENT = "prf_pref_screenOnPermanent";
    static final String PREF_PROFILE_VOLUME_MUTE_SOUND = "prf_pref_volumeMuteSound";
    static final String PREF_PROFILE_DEVICE_LOCATION_MODE = "prf_pref_deviceLocationMode";
    static final String PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING = "prf_pref_applicationEnableNotificationScanning";
    static final String PREF_PROFILE_GENERATE_NOTIFICATION = "prf_pref_generateNotification";
    static final String PREF_PROFILE_CAMERA_FLASH = "prf_pref_cameraFlash";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1 = "prf_pref_deviceNetworkTypeSIM1";
    static final String PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2 = "prf_pref_deviceNetworkTypeSIM2";
    //static final String PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1 = "prf_pref_deviceMobileDataSIM1";
    //static final String PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2 = "prf_pref_deviceMobileDataSIM2";
    static final String PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS = "prf_pref_deviceDefaultSIMCards";
    static final String PREF_PROFILE_DEVICE_ONOFF_SIM1 = "prf_pref_deviceOnOffSIM1";
    static final String PREF_PROFILE_DEVICE_ONOFF_SIM2 = "prf_pref_deviceOnOffSIM2";
    static final String PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1 = "prf_pref_soundRingtoneChangeSIM1";
    static final String PREF_PROFILE_SOUND_RINGTONE_SIM1 = "prf_pref_soundRingtoneSIM1";
    static final String PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2 = "prf_pref_soundRingtoneChangeSIM2";
    static final String PREF_PROFILE_SOUND_RINGTONE_SIM2 = "prf_pref_soundRingtoneSIM2";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1 = "prf_pref_soundNotificationChangeSIM1";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_SIM1 = "prf_pref_soundNotificationSIM1";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2 = "prf_pref_soundNotificationChangeSIM2";
    static final String PREF_PROFILE_SOUND_NOTIFICATION_SIM2 = "prf_pref_soundNotificationSIM2";
    static final String PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS = "prf_pref_soundSameRingtoneForBothSIMCards";
    static final String PREF_PROFILE_DEVICE_LIVE_WALLPAPER = "prf_pref_deviceLiveWallpaper";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_FOLDER = "prf_pref_deviceWallpaperFolder";
    static final String PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN = "prf_pref_applicationDisableGloabalEventsRun";
    static final String PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS = "prf_pref_deviceVPNSettingsPrefs";
    static final String PREF_PROFILE_END_OF_ACTIVATION_TYPE = "prf_pref_endOfActivationType";
    static final String PREF_PROFILE_END_OF_ACTIVATION_TIME = "prf_pref_endOfActivationTime";
    static final String PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING = "prf_pref_applicationEnablePeriodicScanning";
    static final String PREF_PROFILE_DEVICE_VPN = "prf_pref_deviceVPN";
    static final String PREF_PROFILE_VIBRATION_INTENSITY_RINGING = "prf_pref_vibrationIntensityRinging";
    static final String PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS = "prf_pref_vibrationIntensityNotifications";
    static final String PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION = "prf_pref_vibrationIntensityTouchInteraction";
    static final String PREF_PROFILE_VOLUME_MEDIA_CHANGE_DURING_PLAY = "prf_pref_volumeMediaChangeDuringPlay";
    static final String PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL = "prf_pref_applicationWifiScanInterval";
    static final String PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL = "prf_pref_applicationBluetoothScanInterval";
    static final String PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION = "prf_pref_applicationBluetoothLEScanDuration";
    static final String PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL = "prf_pref_applicationLocationUpdateInterval";
    static final String PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL = "prf_pref_applicationOrientationScanInterval";
    static final String PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL = "prf_pref_applicationPeriodicScanningScanInterval";
    static final String PREF_PROFILE_SEND_SMS_CONTACTS = "prf_pref_sendSMS_contacts";
    static final String PREF_PROFILE_SEND_SMS_CONTACT_GROUPS = "prf_pref_semdSMS_contactGroups";
    //static final String PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE = "prf_pref_sendSMS_contactListType";
    static final String PREF_PROFILE_SEND_SMS_SEND_SMS = "prf_pref_sendSMS_sendSMS";
    static final String PREF_PROFILE_SEND_SMS_SMS_TEXT = "prf_pref_sendSMS_SMSText";
    static final String PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN = "prf_pref_deviceWallpaperLockScreen";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED = "prf_pref_clearNotificationEnbaled";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS = "prf_pref_clearNotificationApplications";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS = "prf_pref_clearNotificationCheckContacts";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT = "prf_pref_clearNotificationCheckText";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS = "prf_pref_clearNotificationContactGroups";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS = "prf_pref_clearNotificationContacts";
    static final String PREF_PROFILE_CLEAR_NOTIFICATION_TEXT = "prf_pref_clearNotificationText";
    static final String PREF_PROFILE_SCREEN_NIGHT_LIGHT = "prf_pref_screenNightLight";
    static final String PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS = "prf_pref_screenNightLightPrefs";
    static final String PREF_PROFILE_SCREEN_ON_OFF = "prf_pref_screenOnOff";

    static final int RINGERMODE_RING = 1;
    static final int RINGERMODE_RING_AND_VIBRATE = 2;
    static final int RINGERMODE_VIBRATE = 3;
    static final int RINGERMODE_SILENT = 4;
    static final int RINGERMODE_ZENMODE = 5;

    static final int ZENMODE_ALL = 1;
    static final int ZENMODE_PRIORITY = 2;
    static final int ZENMODE_NONE = 3;
    static final int ZENMODE_ALL_AND_VIBRATE = 4;
    static final int ZENMODE_PRIORITY_AND_VIBRATE = 5;
    static final int ZENMODE_ALARMS = 6;

    static final int AFTER_DURATION_DO_NOTHING = 0;
    static final int AFTER_DURATION_DO_UNDO_PROFILE = 1;
    static final int AFTER_DURATION_DO_DEFAULT_PROFILE = 2;
    static final int AFTER_DURATION_DO_RESTART_EVENTS = 3;
    static final int AFTER_DURATION_DO_SPECIFIC_PROFILE = 4;
    static final int AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS = 5;
    static final int AFTER_DURATION_DURATION_TYPE_DURATION = 0;
    static final int AFTER_DURATION_DURATION_TYPE_EXACT_TIME = 1;

    static final int BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET = -99;

    static final int NO_CHANGE_VALUE = 0;
    static final String NO_CHANGE_VALUE_STR = "0";

    static final int BRIGHTNESS_VALUE_FOR_DARK_MODE = 30;
    static final double MIN_PROFILE_ICON_LUMINANCE = 0.3d;

    static final int CHANGE_WALLPAPER_IMAGE = 1;
    static final int CHANGE_WALLPAPER_IMAGE_WITH = 4;
    static final int CHANGE_WALLPAPER_LIVE = 2;
    static final int CHANGE_WALLPAPER_FOLDER = 3;

    static final ArrayMap<String, Boolean> defaultValuesBoolean;
    static {
        defaultValuesBoolean = new ArrayMap<>();
        defaultValuesBoolean.put(PREF_PROFILE_SHOW_IN_ACTIVATOR, false);
        defaultValuesBoolean.put(PREF_PROFILE_SHOW_IN_ACTIVATOR_NOT_SHOW, false);
        defaultValuesBoolean.put(PREF_PROFILE_ASK_FOR_DURATION, false);
        defaultValuesBoolean.put(PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false);
        defaultValuesBoolean.put(PREF_PROFILE_HIDE_STATUS_BAR_ICON, false);
        defaultValuesBoolean.put(PREF_PROFILE_VOLUME_MUTE_SOUND, false);
        defaultValuesBoolean.put(PREF_PROFILE_VOLUME_MEDIA_CHANGE_DURING_PLAY, false);
        defaultValuesBoolean.put(PREF_PROFILE_SEND_SMS_SEND_SMS, false);
        defaultValuesBoolean.put(PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, false);
        defaultValuesBoolean.put(PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS, false);
        defaultValuesBoolean.put(PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT, false);
    }
    static final ArrayMap<String, String> defaultValuesString;
    static {
        defaultValuesString = new ArrayMap<>();
        defaultValuesString.put(PREF_PROFILE_NAME, "");
        defaultValuesString.put(PREF_PROFILE_ICON, StringConstants.PROFILE_ICON_DEFAULT+"|1|0|0");
        defaultValuesString.put(PREF_PROFILE_ICON_WITHOUT_ICON, "|1|0|0");
        defaultValuesString.put(PREF_PROFILE_DURATION, "0");
        defaultValuesString.put(PREF_PROFILE_AFTER_DURATION_DO, "0");
        defaultValuesString.put(PREF_PROFILE_DURATION_NOTIFICATION_SOUND, "");
        defaultValuesString.put(PREF_PROFILE_VOLUME_RINGER_MODE, "0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_ZEN_MODE, "1");
        defaultValuesString.put(PREF_PROFILE_VIBRATION_ON_TOUCH, "0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_RINGTONE, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_NOTIFICATION, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_MEDIA, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_ALARM, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_SYSTEM, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_VOICE, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_ALARM_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_ALARM, "");
        defaultValuesString.put(PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_AUTOSYNC, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_MOBILE_DATA, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WIFI, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WIFI_AP, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_BLUETOOTH, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_GPS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_NFC, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_KEYGUARD, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_BRIGHTNESS, "50|1|1|0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_BRIGHTNESS_WITHOUT_LEVEL, "|1|1|0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_AUTOROTATE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_POWER_SAVE_MODE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WALLPAPER, "-");
        defaultValuesString.put(PREF_PROFILE_DEVICE_NETWORK_TYPE, "0");
        defaultValuesString.put(PREF_PROFILE_NOTIFICATION_LED, "0");
        defaultValuesString.put(PREF_PROFILE_VIBRATE_WHEN_RINGING, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WALLPAPER_FOR, "0");
        defaultValuesString.put(PREF_PROFILE_LOCK_DEVICE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_CONNECT_TO_SSID, StringConstants.CONNECTTOSSID_JUSTANY);
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WIFI_AP_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_HEADS_UP_NOTIFICATIONS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
        defaultValuesString.put(PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, "0");
        defaultValuesString.put(PREF_PROFILE_SCREEN_DARK_MODE, "0");
        defaultValuesString.put(PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_ON_TOUCH, "0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_DTMF, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_ACCESSIBILITY, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_VOLUME_BLUETOOTH_SCO, "-1|1|0");
        defaultValuesString.put(PREF_PROFILE_AFTER_DURATION_PROFILE, String.valueOf(PROFILE_NO_ACTIVATE));
        defaultValuesString.put(PREF_PROFILE_ALWAYS_ON_DISPLAY, "0");
        defaultValuesString.put(PREF_PROFILE_SCREEN_ON_PERMANENT, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_LOCATION_MODE, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_GENERATE_NOTIFICATION, "0|0||");
        defaultValuesString.put(PREF_PROFILE_CAMERA_FLASH, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0");
        //defaultValuesString.put(PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0");
        //defaultValuesString.put(PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_ONOFF_SIM1, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_ONOFF_SIM2, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE_SIM1, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_RINGTONE_SIM2, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION_SIM1, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0");
        defaultValuesString.put(PREF_PROFILE_SOUND_NOTIFICATION_SIM2, "");
        defaultValuesString.put(PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_LIVE_WALLPAPER, "");
        defaultValuesString.put(PREF_PROFILE_VIBRATE_NOTIFICATIONS, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WALLPAPER_FOLDER, "-");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_END_OF_ACTIVATION_TYPE, String.valueOf(AFTER_DURATION_DURATION_TYPE_DURATION));
        defaultValuesString.put(PREF_PROFILE_END_OF_ACTIVATION_TIME, "0");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING, "0");
        defaultValuesString.put(PREF_PROFILE_DEVICE_VPN, "0|0|||0");
        defaultValuesString.put(PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
        defaultValuesString.put(PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
        defaultValuesString.put(PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL, "15");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL, "15");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, "15");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL, "15");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL, "10");
        defaultValuesString.put(PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, "15");
        defaultValuesString.put(PREF_PROFILE_SEND_SMS_CONTACTS, "");
        defaultValuesString.put(PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, "");
        //defaultValuesString.put(PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE, "0");
        defaultValuesString.put(PREF_PROFILE_SEND_SMS_SMS_TEXT, "");
        defaultValuesString.put(PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN, "-");
        defaultValuesString.put(PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS, "");
        defaultValuesString.put(PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS, "");
        defaultValuesString.put(PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS, "");
        defaultValuesString.put(PREF_PROFILE_CLEAR_NOTIFICATION_TEXT, "");
        defaultValuesString.put(PREF_PROFILE_SCREEN_NIGHT_LIGHT, "0");
        defaultValuesString.put(PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS, "0");
        defaultValuesString.put(PREF_PROFILE_SCREEN_ON_OFF, "0");
    }

    static final int[] profileIconId = {
            R.drawable.ic_profile_default,

            R.drawable.ic_profile_home, R.drawable.ic_profile_home_2, R.drawable.ic_profile_home_3,
            R.drawable.ic_profile_home_4, R.drawable.ic_profile_home_5, R.drawable.ic_profile_home_6,

            R.drawable.ic_profile_outdoors_1, R.drawable.ic_profile_outdoors_2, R.drawable.ic_profile_outdoors_3,
            R.drawable.ic_profile_outdoors_4, R.drawable.ic_profile_outdoors_5, R.drawable.ic_profile_outdoors_6,
            R.drawable.ic_profile_outdoors_7, R.drawable.ic_profile_outdoors_8, R.drawable.ic_profile_outdoors_9,
            R.drawable.ic_profile_running_1,

            R.drawable.ic_profile_meeting, R.drawable.ic_profile_meeting_2, R.drawable.ic_profile_meeting_3,
            R.drawable.ic_profile_meeting_4, R.drawable.ic_profile_mute, R.drawable.ic_profile_mute_2,
            R.drawable.ic_profile_volume_4, R.drawable.ic_profile_volume_1, R.drawable.ic_profile_volume_2,
            R.drawable.ic_profile_volume_3, R.drawable.ic_profile_vibrate_1,

            R.drawable.ic_profile_work_1, R.drawable.ic_profile_work_2, R.drawable.ic_profile_work_12,
            R.drawable.ic_profile_work_3, R.drawable.ic_profile_work_4, R.drawable.ic_profile_work_5,
            R.drawable.ic_profile_work_6, R.drawable.ic_profile_work_7, R.drawable.ic_profile_work_8,
            R.drawable.ic_profile_work_9, R.drawable.ic_profile_work_10, R.drawable.ic_profile_work_11,
            R.drawable.ic_profile_work_13, R.drawable.ic_profile_work_14, R.drawable.ic_profile_work_15,
            R.drawable.ic_profile_work_16, R.drawable.ic_profile_work_17, R.drawable.ic_profile_work_18,

            R.drawable.ic_profile_sleep, R.drawable.ic_profile_sleep_2, R.drawable.ic_profile_sleep_3,
            R.drawable.ic_profile_night, R.drawable.ic_profile_call_1, R.drawable.ic_profile_food_1,
            R.drawable.ic_profile_food_2, R.drawable.ic_profile_food_3, R.drawable.ic_profile_food_4,
            R.drawable.ic_profile_food_5, R.drawable.ic_profile_alarm,

            R.drawable.ic_profile_car_1, R.drawable.ic_profile_car_2, R.drawable.ic_profile_car_3,
            R.drawable.ic_profile_car_4, R.drawable.ic_profile_car_5, R.drawable.ic_profile_car_6,
            R.drawable.ic_profile_car_7, R.drawable.ic_profile_car_8, R.drawable.ic_profile_car_9,
            R.drawable.ic_profile_car_10, R.drawable.ic_profile_car_11, R.drawable.ic_profile_steering_1,
            R.drawable.ic_profile_airplane_4, R.drawable.ic_profile_airplane_1, R.drawable.ic_profile_airplane_2,
            R.drawable.ic_profile_airplane_3, R.drawable.ic_profile_ship_1, R.drawable.ic_profile_ship_2,
            R.drawable.ic_profile_ship_3, R.drawable.ic_profile_tram_1, R.drawable.ic_profile_tickets_1,
            R.drawable.ic_profile_tickets_2, R.drawable.ic_profile_travel_1,

            R.drawable.ic_profile_culture_1, R.drawable.ic_profile_culture_6, R.drawable.ic_profile_culture_7,
            R.drawable.ic_profile_culture_2, R.drawable.ic_profile_culture_8, R.drawable.ic_profile_culture_9,
            R.drawable.ic_profile_culture_3, R.drawable.ic_profile_culture_10, R.drawable.ic_profile_culture_11,
            R.drawable.ic_profile_culture_12, R.drawable.ic_profile_culture_13, R.drawable.ic_profile_culture_5,
            R.drawable.ic_profile_culture_14, R.drawable.ic_profile_culture_4, R.drawable.ic_profile_culture_15,
            R.drawable.ic_profile_culture_16, R.drawable.ic_profile_culture_17,

            R.drawable.ic_profile_battery_1, R.drawable.ic_profile_battery_2, R.drawable.ic_profile_battery_3,

            R.drawable.ic_profile_lock, R.drawable.ic_profile_wifi, R.drawable.ic_profile_mobile_data
    };

    static final int[] profileIconColor = {
            // default
            0xff1c9cd7,

            // home
            0xff99cc00, 0xff99cc00, 0xff99cc00, 0xff99cc00, 0xff99cc00,
            0xff99cc00,

            // outdoors
            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,
            0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33, 0xffffbc33,

            // meeting, volume
            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,
            0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000, 0xffcc0000,
            0xffcc0000,

            // work
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff, 0xffa801ff,
            0xffa801ff, 0xffa801ff, 0xffa801ff,

            // sleep, food, alarm
            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,
            0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc, 0xff0099cc,
            0xff0099cc,

            // car, airplane, ship, tickets
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174, 0xff28a174,
            0xff28a174, 0xff28a174, 0xff28a174,

            // culture
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63, 0xffe91e63,
            0xffe91e63, 0xffe91e63,

            // battery
            0xffdb3514, 0xffdb9714, 0xff2aa561,

            //lock
            0xff6a3e18,

            // wifi, mobile data
            0xff1ea0df, 0xff1ea0df
    };

    static final String ic_profile_home = "ic_profile_home";
    static final String ic_profile_home_2 = "ic_profile_home_2";
    static final String ic_profile_home_3 = "ic_profile_home_3";
    static final String ic_profile_home_4 = "ic_profile_home_4";
    static final String ic_profile_home_5 = "ic_profile_home_5";
    static final String ic_profile_home_6 = "ic_profile_home_6";
    static final String ic_profile_outdoors_1 = "ic_profile_outdoors_1";
    static final String ic_profile_outdoors_2 = "ic_profile_outdoors_2";
    static final String ic_profile_outdoors_3 = "ic_profile_outdoors_3";
    static final String ic_profile_outdoors_4 = "ic_profile_outdoors_4";
    static final String ic_profile_outdoors_5 = "ic_profile_outdoors_5";
    static final String ic_profile_outdoors_6 = "ic_profile_outdoors_6";
    static final String ic_profile_outdoors_7 = "ic_profile_outdoors_7";
    static final String ic_profile_outdoors_8 = "ic_profile_outdoors_8";
    static final String ic_profile_outdoors_9 = "ic_profile_outdoors_9";
    static final String ic_profile_running_1 = "ic_profile_running_1";
    static final String ic_profile_meeting = "ic_profile_meeting";
    static final String ic_profile_meeting_2 = "ic_profile_meeting_2";
    static final String ic_profile_meeting_3 = "ic_profile_meeting_3";
    static final String ic_profile_meeting_4 = "ic_profile_meeting_4";
    static final String ic_profile_mute = "ic_profile_mute";
    static final String ic_profile_mute_2 = "ic_profile_mute_2";
    static final String ic_profile_volume_4 = "ic_profile_volume_4";
    static final String ic_profile_volume_1 = "ic_profile_volume_1";
    static final String ic_profile_volume_2 = "ic_profile_volume_2";
    static final String ic_profile_volume_3 = "ic_profile_volume_3";
    static final String ic_profile_vibrate_1 = "ic_profile_vibrate_1";
    static final String ic_profile_work_1 = "ic_profile_work_1";
    static final String ic_profile_work_2 = "ic_profile_work_2";
    static final String ic_profile_work_12 = "ic_profile_work_12";
    static final String ic_profile_work_3 = "ic_profile_work_3";
    static final String ic_profile_work_4 = "ic_profile_work_4";
    static final String ic_profile_work_5 = "ic_profile_work_5";
    static final String ic_profile_work_6 = "ic_profile_work_6";
    static final String ic_profile_work_7 = "ic_profile_work_7";
    static final String ic_profile_work_8 = "ic_profile_work_8";
    static final String ic_profile_work_9 = "ic_profile_work_9";
    static final String ic_profile_work_10 = "ic_profile_work_10";
    static final String ic_profile_work_11 = "ic_profile_work_11";
    static final String ic_profile_work_13 = "ic_profile_work_13";
    static final String ic_profile_work_14 = "ic_profile_work_14";
    static final String ic_profile_work_15 = "ic_profile_work_15";
    static final String ic_profile_work_16 = "ic_profile_work_16";
    static final String ic_profile_work_17 = "ic_profile_work_17";
    static final String ic_profile_work_18 = "ic_profile_work_18";
    static final String ic_profile_sleep = "ic_profile_sleep";
    static final String ic_profile_sleep_2 = "ic_profile_sleep_2";
    static final String ic_profile_sleep_3 = "ic_profile_sleep_3";
    static final String ic_profile_night = "ic_profile_night";
    static final String ic_profile_call_1 = "ic_profile_call_1";
    static final String ic_profile_food_1 = "ic_profile_food_1";
    static final String ic_profile_food_2 = "ic_profile_food_2";
    static final String ic_profile_food_3 = "ic_profile_food_3";
    static final String ic_profile_food_4 = "ic_profile_food_4";
    static final String ic_profile_food_5 = "ic_profile_food_5";
    static final String ic_profile_alarm = "ic_profile_alarm";
    static final String ic_profile_car_1 = "ic_profile_car_1";
    static final String ic_profile_car_2 = "ic_profile_car_2";
    static final String ic_profile_car_3 = "ic_profile_car_3";
    static final String ic_profile_car_4 = "ic_profile_car_4";
    static final String ic_profile_car_5 = "ic_profile_car_5";
    static final String ic_profile_car_6 = "ic_profile_car_6";
    static final String ic_profile_car_7 = "ic_profile_car_7";
    static final String ic_profile_car_8 = "ic_profile_car_8";
    static final String ic_profile_car_9 = "ic_profile_car_9";
    static final String ic_profile_car_10 = "ic_profile_car_10";
    static final String ic_profile_car_11 = "ic_profile_car_11";
    static final String ic_profile_steering_1 = "ic_profile_steering_1";
    static final String ic_profile_airplane_1 = "ic_profile_airplane_1";
    static final String ic_profile_airplane_2 = "ic_profile_airplane_2";
    static final String ic_profile_airplane_3 = "ic_profile_airplane_3";
    static final String ic_profile_airplane_4 = "ic_profile_airplane_4";
    static final String ic_profile_ship_1 = "ic_profile_ship_1";
    static final String ic_profile_ship_2 = "ic_profile_ship_2";
    static final String ic_profile_ship_3 = "ic_profile_ship_3";
    static final String ic_profile_tram_1 = "ic_profile_tram_1";
    static final String ic_profile_tickets_1 = "ic_profile_tickets_1";
    static final String ic_profile_tickets_2 = "ic_profile_tickets_2";
    static final String ic_profile_travel_1 = "ic_profile_travel_1";
    static final String ic_profile_culture_1 = "ic_profile_culture_1";
    static final String ic_profile_culture_6 = "ic_profile_culture_6";
    static final String ic_profile_culture_7 = "ic_profile_culture_7";
    static final String ic_profile_culture_2 = "ic_profile_culture_2";
    static final String ic_profile_culture_8 = "ic_profile_culture_8";
    static final String ic_profile_culture_9 = "ic_profile_culture_9";
    static final String ic_profile_culture_3 = "ic_profile_culture_3";
    static final String ic_profile_culture_10 = "ic_profile_culture_10";
    static final String ic_profile_culture_11 = "ic_profile_culture_11";
    static final String ic_profile_culture_12 = "ic_profile_culture_12";
    static final String ic_profile_culture_13 = "ic_profile_culture_13";
    static final String ic_profile_culture_5 = "ic_profile_culture_5";
    static final String ic_profile_culture_14 = "ic_profile_culture_14";
    static final String ic_profile_culture_4 = "ic_profile_culture_4";
    static final String ic_profile_culture_15 = "ic_profile_culture_15";
    static final String ic_profile_culture_16 = "ic_profile_culture_16";
    static final String ic_profile_culture_17 = "ic_profile_culture_17";
    static final String ic_profile_battery_1 = "ic_profile_battery_1";
    static final String ic_profile_battery_2 = "ic_profile_battery_2";
    static final String ic_profile_battery_3 = "ic_profile_battery_3";
    static final String ic_profile_lock = "ic_profile_lock";
    static final String ic_profile_wifi = "ic_profile_wifi";
    static final String ic_profile_mobile_data = "ic_profile_mobile_data";

    static final ArrayMap<String, Integer> profileIconIdMap;
    static {
        profileIconIdMap = new ArrayMap<>();

        profileIconIdMap.put(StringConstants.PROFILE_ICON_RESTART_EVENTS, R.drawable.ic_profile_restart_events);

        profileIconIdMap.put(StringConstants.PROFILE_ICON_DEFAULT, R.drawable.ic_profile_default);
        profileIconIdMap.put(ic_profile_home, R.drawable.ic_profile_home);
        profileIconIdMap.put(ic_profile_home_2, R.drawable.ic_profile_home_2);
        profileIconIdMap.put(ic_profile_home_3, R.drawable.ic_profile_home_3);
        profileIconIdMap.put(ic_profile_home_4, R.drawable.ic_profile_home_4);
        profileIconIdMap.put(ic_profile_home_5, R.drawable.ic_profile_home_5);
        profileIconIdMap.put(ic_profile_home_6, R.drawable.ic_profile_home_6);
        profileIconIdMap.put(ic_profile_outdoors_1, R.drawable.ic_profile_outdoors_1);
        profileIconIdMap.put(ic_profile_outdoors_2, R.drawable.ic_profile_outdoors_2);
        profileIconIdMap.put(ic_profile_outdoors_3, R.drawable.ic_profile_outdoors_3);
        profileIconIdMap.put(ic_profile_outdoors_4, R.drawable.ic_profile_outdoors_4);
        profileIconIdMap.put(ic_profile_outdoors_5, R.drawable.ic_profile_outdoors_5);
        profileIconIdMap.put(ic_profile_outdoors_6, R.drawable.ic_profile_outdoors_6);
        profileIconIdMap.put(ic_profile_outdoors_7, R.drawable.ic_profile_outdoors_7);
        profileIconIdMap.put(ic_profile_outdoors_8, R.drawable.ic_profile_outdoors_8);
        profileIconIdMap.put(ic_profile_outdoors_9, R.drawable.ic_profile_outdoors_9);
        profileIconIdMap.put(ic_profile_running_1, R.drawable.ic_profile_running_1);
        profileIconIdMap.put(ic_profile_meeting, R.drawable.ic_profile_meeting);
        profileIconIdMap.put(ic_profile_meeting_2, R.drawable.ic_profile_meeting_2);
        profileIconIdMap.put(ic_profile_meeting_3, R.drawable.ic_profile_meeting_3);
        profileIconIdMap.put(ic_profile_meeting_4, R.drawable.ic_profile_meeting_4);
        profileIconIdMap.put(ic_profile_mute, R.drawable.ic_profile_mute);
        profileIconIdMap.put(ic_profile_mute_2, R.drawable.ic_profile_mute_2);
        profileIconIdMap.put(ic_profile_volume_4, R.drawable.ic_profile_volume_4);
        profileIconIdMap.put(ic_profile_volume_1, R.drawable.ic_profile_volume_1);
        profileIconIdMap.put(ic_profile_volume_2, R.drawable.ic_profile_volume_2);
        profileIconIdMap.put(ic_profile_volume_3, R.drawable.ic_profile_volume_3);
        profileIconIdMap.put(ic_profile_vibrate_1, R.drawable.ic_profile_vibrate_1);
        profileIconIdMap.put(ic_profile_work_1, R.drawable.ic_profile_work_1);
        profileIconIdMap.put(ic_profile_work_2, R.drawable.ic_profile_work_2);
        profileIconIdMap.put(ic_profile_work_12, R.drawable.ic_profile_work_12);
        profileIconIdMap.put(ic_profile_work_3, R.drawable.ic_profile_work_3);
        profileIconIdMap.put(ic_profile_work_4, R.drawable.ic_profile_work_4);
        profileIconIdMap.put(ic_profile_work_5, R.drawable.ic_profile_work_5);
        profileIconIdMap.put(ic_profile_work_6, R.drawable.ic_profile_work_6);
        profileIconIdMap.put(ic_profile_work_7, R.drawable.ic_profile_work_7);
        profileIconIdMap.put(ic_profile_work_8, R.drawable.ic_profile_work_8);
        profileIconIdMap.put(ic_profile_work_9, R.drawable.ic_profile_work_9);
        profileIconIdMap.put(ic_profile_work_10, R.drawable.ic_profile_work_10);
        profileIconIdMap.put(ic_profile_work_11, R.drawable.ic_profile_work_11);
        profileIconIdMap.put(ic_profile_work_13, R.drawable.ic_profile_work_13);
        profileIconIdMap.put(ic_profile_work_14, R.drawable.ic_profile_work_14);
        profileIconIdMap.put(ic_profile_work_15, R.drawable.ic_profile_work_15);
        profileIconIdMap.put(ic_profile_work_16, R.drawable.ic_profile_work_16);
        profileIconIdMap.put(ic_profile_work_17, R.drawable.ic_profile_work_17);
        profileIconIdMap.put(ic_profile_work_18, R.drawable.ic_profile_work_18);
        profileIconIdMap.put(ic_profile_sleep, R.drawable.ic_profile_sleep);
        profileIconIdMap.put(ic_profile_sleep_2, R.drawable.ic_profile_sleep_2);
        profileIconIdMap.put(ic_profile_sleep_3, R.drawable.ic_profile_sleep_3);
        profileIconIdMap.put(ic_profile_night, R.drawable.ic_profile_night);
        profileIconIdMap.put(ic_profile_call_1, R.drawable.ic_profile_call_1);
        profileIconIdMap.put(ic_profile_food_1, R.drawable.ic_profile_food_1);
        profileIconIdMap.put(ic_profile_food_2, R.drawable.ic_profile_food_2);
        profileIconIdMap.put(ic_profile_food_3, R.drawable.ic_profile_food_3);
        profileIconIdMap.put(ic_profile_food_4, R.drawable.ic_profile_food_4);
        profileIconIdMap.put(ic_profile_food_5, R.drawable.ic_profile_food_5);
        profileIconIdMap.put(ic_profile_alarm, R.drawable.ic_profile_alarm);
        profileIconIdMap.put(ic_profile_car_1, R.drawable.ic_profile_car_1);
        profileIconIdMap.put(ic_profile_car_2, R.drawable.ic_profile_car_2);
        profileIconIdMap.put(ic_profile_car_3, R.drawable.ic_profile_car_3);
        profileIconIdMap.put(ic_profile_car_4, R.drawable.ic_profile_car_4);
        profileIconIdMap.put(ic_profile_car_5, R.drawable.ic_profile_car_5);
        profileIconIdMap.put(ic_profile_car_6, R.drawable.ic_profile_car_6);
        profileIconIdMap.put(ic_profile_car_7, R.drawable.ic_profile_car_7);
        profileIconIdMap.put(ic_profile_car_8, R.drawable.ic_profile_car_8);
        profileIconIdMap.put(ic_profile_car_9, R.drawable.ic_profile_car_9);
        profileIconIdMap.put(ic_profile_car_10, R.drawable.ic_profile_car_10);
        profileIconIdMap.put(ic_profile_car_11, R.drawable.ic_profile_car_11);
        profileIconIdMap.put(ic_profile_steering_1, R.drawable.ic_profile_steering_1);
        profileIconIdMap.put(ic_profile_airplane_4, R.drawable.ic_profile_airplane_4);
        profileIconIdMap.put(ic_profile_airplane_1, R.drawable.ic_profile_airplane_1);
        profileIconIdMap.put(ic_profile_airplane_2, R.drawable.ic_profile_airplane_2);
        profileIconIdMap.put(ic_profile_airplane_3, R.drawable.ic_profile_airplane_3);
        profileIconIdMap.put(ic_profile_ship_1, R.drawable.ic_profile_ship_1);
        profileIconIdMap.put(ic_profile_ship_2, R.drawable.ic_profile_ship_2);
        profileIconIdMap.put(ic_profile_ship_3, R.drawable.ic_profile_ship_3);
        profileIconIdMap.put(ic_profile_tram_1, R.drawable.ic_profile_tram_1);
        profileIconIdMap.put(ic_profile_tickets_1, R.drawable.ic_profile_tickets_1);
        profileIconIdMap.put(ic_profile_tickets_2, R.drawable.ic_profile_tickets_2);
        profileIconIdMap.put(ic_profile_travel_1, R.drawable.ic_profile_travel_1);
        profileIconIdMap.put(ic_profile_culture_1, R.drawable.ic_profile_culture_1);
        profileIconIdMap.put(ic_profile_culture_6, R.drawable.ic_profile_culture_6);
        profileIconIdMap.put(ic_profile_culture_7, R.drawable.ic_profile_culture_7);
        profileIconIdMap.put(ic_profile_culture_2, R.drawable.ic_profile_culture_2);
        profileIconIdMap.put(ic_profile_culture_8, R.drawable.ic_profile_culture_8);
        profileIconIdMap.put(ic_profile_culture_9, R.drawable.ic_profile_culture_9);
        profileIconIdMap.put(ic_profile_culture_3, R.drawable.ic_profile_culture_3);
        profileIconIdMap.put(ic_profile_culture_10, R.drawable.ic_profile_culture_10);
        profileIconIdMap.put(ic_profile_culture_11, R.drawable.ic_profile_culture_11);
        profileIconIdMap.put(ic_profile_culture_12, R.drawable.ic_profile_culture_12);
        profileIconIdMap.put(ic_profile_culture_13, R.drawable.ic_profile_culture_13);
        profileIconIdMap.put(ic_profile_culture_5, R.drawable.ic_profile_culture_5);
        profileIconIdMap.put(ic_profile_culture_14, R.drawable.ic_profile_culture_14);
        profileIconIdMap.put(ic_profile_culture_4, R.drawable.ic_profile_culture_4);
        profileIconIdMap.put(ic_profile_culture_15, R.drawable.ic_profile_culture_15);
        profileIconIdMap.put(ic_profile_culture_16, R.drawable.ic_profile_culture_16);
        profileIconIdMap.put(ic_profile_culture_17, R.drawable.ic_profile_culture_17);
        profileIconIdMap.put(ic_profile_battery_1, R.drawable.ic_profile_battery_1);
        profileIconIdMap.put(ic_profile_battery_2, R.drawable.ic_profile_battery_2);
        profileIconIdMap.put(ic_profile_battery_3, R.drawable.ic_profile_battery_3);
        profileIconIdMap.put(ic_profile_lock, R.drawable.ic_profile_lock);
        profileIconIdMap.put(ic_profile_wifi, R.drawable.ic_profile_wifi);
        profileIconIdMap.put(ic_profile_mobile_data, R.drawable.ic_profile_mobile_data);
    }
    
    static final ArrayMap<String, Integer> profileIconNotifyId;
    static {
        profileIconNotifyId = new ArrayMap<>();
        profileIconNotifyId.put(StringConstants.PROFILE_ICON_DEFAULT, R.drawable.ic_profile_default_notify);
        profileIconNotifyId.put(ic_profile_home, R.drawable.ic_profile_home_notify);
        profileIconNotifyId.put(ic_profile_home_2, R.drawable.ic_profile_home_2_notify);
        profileIconNotifyId.put(ic_profile_home_3, R.drawable.ic_profile_home_3_notify);
        profileIconNotifyId.put(ic_profile_home_4, R.drawable.ic_profile_home_4_notify);
        profileIconNotifyId.put(ic_profile_home_5, R.drawable.ic_profile_home_5_notify);
        profileIconNotifyId.put(ic_profile_home_6, R.drawable.ic_profile_home_6_notify);
        profileIconNotifyId.put(ic_profile_outdoors_1, R.drawable.ic_profile_outdoors_1_notify);
        profileIconNotifyId.put(ic_profile_outdoors_2, R.drawable.ic_profile_outdoors_2_notify);
        profileIconNotifyId.put(ic_profile_outdoors_3, R.drawable.ic_profile_outdoors_3_notify);
        profileIconNotifyId.put(ic_profile_outdoors_4, R.drawable.ic_profile_outdoors_4_notify);
        profileIconNotifyId.put(ic_profile_outdoors_5, R.drawable.ic_profile_outdoors_5_notify);
        profileIconNotifyId.put(ic_profile_outdoors_6, R.drawable.ic_profile_outdoors_6_notify);
        profileIconNotifyId.put(ic_profile_outdoors_7, R.drawable.ic_profile_outdoors_7_notify);
        profileIconNotifyId.put(ic_profile_outdoors_8, R.drawable.ic_profile_outdoors_8_notify);
        profileIconNotifyId.put(ic_profile_outdoors_9, R.drawable.ic_profile_outdoors_9_notify);
        profileIconNotifyId.put(ic_profile_running_1, R.drawable.ic_profile_running_1_notify);
        profileIconNotifyId.put(ic_profile_meeting, R.drawable.ic_profile_meeting_notify);
        profileIconNotifyId.put(ic_profile_meeting_2, R.drawable.ic_profile_meeting_2_notify);
        profileIconNotifyId.put(ic_profile_meeting_3, R.drawable.ic_profile_meeting_3_notify);
        profileIconNotifyId.put(ic_profile_meeting_4, R.drawable.ic_profile_meeting_4_notify);
        profileIconNotifyId.put(ic_profile_mute, R.drawable.ic_profile_mute_notify);
        profileIconNotifyId.put(ic_profile_mute_2, R.drawable.ic_profile_mute_2_notify);
        profileIconNotifyId.put(ic_profile_volume_4, R.drawable.ic_profile_volume_4_notify);
        profileIconNotifyId.put(ic_profile_volume_1, R.drawable.ic_profile_volume_1_notify);
        profileIconNotifyId.put(ic_profile_volume_2, R.drawable.ic_profile_volume_2_notify);
        profileIconNotifyId.put(ic_profile_volume_3, R.drawable.ic_profile_volume_3_notify);
        profileIconNotifyId.put(ic_profile_vibrate_1, R.drawable.ic_profile_vibrate_1_notify);
        profileIconNotifyId.put(ic_profile_work_1, R.drawable.ic_profile_work_1_notify);
        profileIconNotifyId.put(ic_profile_work_2, R.drawable.ic_profile_work_2_notify);
        profileIconNotifyId.put(ic_profile_work_12, R.drawable.ic_profile_work_12_notify);
        profileIconNotifyId.put(ic_profile_work_3, R.drawable.ic_profile_work_3_notify);
        profileIconNotifyId.put(ic_profile_work_4, R.drawable.ic_profile_work_4_notify);
        profileIconNotifyId.put(ic_profile_work_5, R.drawable.ic_profile_work_5_notify);
        profileIconNotifyId.put(ic_profile_work_6, R.drawable.ic_profile_work_6_notify);
        profileIconNotifyId.put(ic_profile_work_7, R.drawable.ic_profile_work_7_notify);
        profileIconNotifyId.put(ic_profile_work_8, R.drawable.ic_profile_work_8_notify);
        profileIconNotifyId.put(ic_profile_work_9, R.drawable.ic_profile_work_9_notify);
        profileIconNotifyId.put(ic_profile_work_10, R.drawable.ic_profile_work_10_notify);
        profileIconNotifyId.put(ic_profile_work_11, R.drawable.ic_profile_work_11_notify);
        profileIconNotifyId.put(ic_profile_work_13, R.drawable.ic_profile_work_13_notify);
        profileIconNotifyId.put(ic_profile_work_14, R.drawable.ic_profile_work_14_notify);
        profileIconNotifyId.put(ic_profile_work_15, R.drawable.ic_profile_work_15_notify);
        profileIconNotifyId.put(ic_profile_work_16, R.drawable.ic_profile_work_16_notify);
        profileIconNotifyId.put(ic_profile_work_17, R.drawable.ic_profile_work_17_notify);
        profileIconNotifyId.put(ic_profile_work_18, R.drawable.ic_profile_work_18_notify);
        profileIconNotifyId.put(ic_profile_sleep, R.drawable.ic_profile_sleep_notify);
        profileIconNotifyId.put(ic_profile_sleep_2, R.drawable.ic_profile_sleep_2_notify);
        profileIconNotifyId.put(ic_profile_sleep_3, R.drawable.ic_profile_sleep_3_notify);
        profileIconNotifyId.put(ic_profile_night, R.drawable.ic_profile_night_notify);
        profileIconNotifyId.put(ic_profile_call_1, R.drawable.ic_profile_call_1_notify);
        profileIconNotifyId.put(ic_profile_food_1, R.drawable.ic_profile_food_1_notify);
        profileIconNotifyId.put(ic_profile_food_2, R.drawable.ic_profile_food_2_notify);
        profileIconNotifyId.put(ic_profile_food_3, R.drawable.ic_profile_food_3_notify);
        profileIconNotifyId.put(ic_profile_food_4, R.drawable.ic_profile_food_4_notify);
        profileIconNotifyId.put(ic_profile_food_5, R.drawable.ic_profile_food_5_notify);
        profileIconNotifyId.put(ic_profile_alarm, R.drawable.ic_profile_alarm_notify);
        profileIconNotifyId.put(ic_profile_car_1, R.drawable.ic_profile_car_1_notify);
        profileIconNotifyId.put(ic_profile_car_2, R.drawable.ic_profile_car_2_notify);
        profileIconNotifyId.put(ic_profile_car_3, R.drawable.ic_profile_car_3_notify);
        profileIconNotifyId.put(ic_profile_car_4, R.drawable.ic_profile_car_4_notify);
        profileIconNotifyId.put(ic_profile_car_5, R.drawable.ic_profile_car_5_notify);
        profileIconNotifyId.put(ic_profile_car_6, R.drawable.ic_profile_car_6_notify);
        profileIconNotifyId.put(ic_profile_car_7, R.drawable.ic_profile_car_7_notify);
        profileIconNotifyId.put(ic_profile_car_8, R.drawable.ic_profile_car_8_notify);
        profileIconNotifyId.put(ic_profile_car_9, R.drawable.ic_profile_car_9_notify);
        profileIconNotifyId.put(ic_profile_car_10, R.drawable.ic_profile_car_10_notify);
        profileIconNotifyId.put(ic_profile_car_11, R.drawable.ic_profile_car_11_notify);
        profileIconNotifyId.put(ic_profile_steering_1, R.drawable.ic_profile_steering_1_notify);
        profileIconNotifyId.put(ic_profile_airplane_4, R.drawable.ic_profile_airplane_4_notify);
        profileIconNotifyId.put(ic_profile_airplane_1, R.drawable.ic_profile_airplane_1_notify);
        profileIconNotifyId.put(ic_profile_airplane_2, R.drawable.ic_profile_airplane_2_notify);
        profileIconNotifyId.put(ic_profile_airplane_3, R.drawable.ic_profile_airplane_3_notify);
        profileIconNotifyId.put(ic_profile_ship_1, R.drawable.ic_profile_ship_1_notify);
        profileIconNotifyId.put(ic_profile_ship_2, R.drawable.ic_profile_ship_2_notify);
        profileIconNotifyId.put(ic_profile_ship_3, R.drawable.ic_profile_ship_3_notify);
        profileIconNotifyId.put(ic_profile_tram_1, R.drawable.ic_profile_tram_1_notify);
        profileIconNotifyId.put(ic_profile_tickets_1, R.drawable.ic_profile_tickets_1_notify);
        profileIconNotifyId.put(ic_profile_tickets_2, R.drawable.ic_profile_tickets_2_notify);
        profileIconNotifyId.put(ic_profile_travel_1, R.drawable.ic_profile_travel_1_notify);
        profileIconNotifyId.put(ic_profile_culture_1, R.drawable.ic_profile_culture_1_notify);
        profileIconNotifyId.put(ic_profile_culture_6, R.drawable.ic_profile_culture_6_notify);
        profileIconNotifyId.put(ic_profile_culture_7, R.drawable.ic_profile_culture_7_notify);
        profileIconNotifyId.put(ic_profile_culture_2, R.drawable.ic_profile_culture_2_notify);
        profileIconNotifyId.put(ic_profile_culture_8, R.drawable.ic_profile_culture_8_notify);
        profileIconNotifyId.put(ic_profile_culture_9, R.drawable.ic_profile_culture_9_notify);
        profileIconNotifyId.put(ic_profile_culture_3, R.drawable.ic_profile_culture_3_notify);
        profileIconNotifyId.put(ic_profile_culture_10, R.drawable.ic_profile_culture_10_notify);
        profileIconNotifyId.put(ic_profile_culture_11, R.drawable.ic_profile_culture_11_notify);
        profileIconNotifyId.put(ic_profile_culture_12, R.drawable.ic_profile_culture_12_notify);
        profileIconNotifyId.put(ic_profile_culture_13, R.drawable.ic_profile_culture_13_notify);
        profileIconNotifyId.put(ic_profile_culture_5, R.drawable.ic_profile_culture_5_notify);
        profileIconNotifyId.put(ic_profile_culture_14, R.drawable.ic_profile_culture_14_notify);
        profileIconNotifyId.put(ic_profile_culture_4, R.drawable.ic_profile_culture_4_notify);
        profileIconNotifyId.put(ic_profile_culture_15, R.drawable.ic_profile_culture_15_notify);
        profileIconNotifyId.put(ic_profile_culture_16, R.drawable.ic_profile_culture_16_notify);
        profileIconNotifyId.put(ic_profile_culture_17, R.drawable.ic_profile_culture_17_notify);
        profileIconNotifyId.put(ic_profile_battery_1, R.drawable.ic_profile_battery_1_notify);
        profileIconNotifyId.put(ic_profile_battery_2, R.drawable.ic_profile_battery_2_notify);
        profileIconNotifyId.put(ic_profile_battery_3, R.drawable.ic_profile_battery_3_notify);
        profileIconNotifyId.put(ic_profile_lock, R.drawable.ic_profile_lock_notify);
        profileIconNotifyId.put(ic_profile_wifi, R.drawable.ic_profile_wifi_notify);
        profileIconNotifyId.put(ic_profile_mobile_data, R.drawable.ic_profile_mobile_data_notify);
    }

    static final ArrayMap<String, Integer> profileIconNotifyColorId;
    static {
        profileIconNotifyColorId = new ArrayMap<>();
        profileIconNotifyColorId.put(StringConstants.PROFILE_ICON_DEFAULT, R.drawable.ic_profile_default_notify_color);
        profileIconNotifyColorId.put(ic_profile_home, R.drawable.ic_profile_home_notify_color);
        profileIconNotifyColorId.put(ic_profile_home_2, R.drawable.ic_profile_home_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_home_3, R.drawable.ic_profile_home_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_home_4, R.drawable.ic_profile_home_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_home_5, R.drawable.ic_profile_home_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_home_6, R.drawable.ic_profile_home_6_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_1, R.drawable.ic_profile_outdoors_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_2, R.drawable.ic_profile_outdoors_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_3, R.drawable.ic_profile_outdoors_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_4, R.drawable.ic_profile_outdoors_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_5, R.drawable.ic_profile_outdoors_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_6, R.drawable.ic_profile_outdoors_6_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_7, R.drawable.ic_profile_outdoors_7_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_8, R.drawable.ic_profile_outdoors_8_notify_color);
        profileIconNotifyColorId.put(ic_profile_outdoors_9, R.drawable.ic_profile_outdoors_9_notify_color);
        profileIconNotifyColorId.put(ic_profile_running_1, R.drawable.ic_profile_running_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_meeting, R.drawable.ic_profile_meeting_notify_color);
        profileIconNotifyColorId.put(ic_profile_meeting_2, R.drawable.ic_profile_meeting_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_meeting_3, R.drawable.ic_profile_meeting_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_meeting_4, R.drawable.ic_profile_meeting_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_mute, R.drawable.ic_profile_mute_notify_color);
        profileIconNotifyColorId.put(ic_profile_mute_2, R.drawable.ic_profile_mute_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_volume_4, R.drawable.ic_profile_volume_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_volume_1, R.drawable.ic_profile_volume_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_volume_2, R.drawable.ic_profile_volume_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_volume_3, R.drawable.ic_profile_volume_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_vibrate_1, R.drawable.ic_profile_vibrate_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_1, R.drawable.ic_profile_work_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_2, R.drawable.ic_profile_work_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_12, R.drawable.ic_profile_work_12_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_3, R.drawable.ic_profile_work_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_4, R.drawable.ic_profile_work_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_5, R.drawable.ic_profile_work_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_6, R.drawable.ic_profile_work_6_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_7, R.drawable.ic_profile_work_7_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_8, R.drawable.ic_profile_work_8_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_9, R.drawable.ic_profile_work_9_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_10, R.drawable.ic_profile_work_10_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_11, R.drawable.ic_profile_work_11_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_13, R.drawable.ic_profile_work_13_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_14, R.drawable.ic_profile_work_14_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_15, R.drawable.ic_profile_work_15_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_16, R.drawable.ic_profile_work_16_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_17, R.drawable.ic_profile_work_17_notify_color);
        profileIconNotifyColorId.put(ic_profile_work_18, R.drawable.ic_profile_work_18_notify_color);
        profileIconNotifyColorId.put(ic_profile_sleep, R.drawable.ic_profile_sleep_notify_color);
        profileIconNotifyColorId.put(ic_profile_sleep_2, R.drawable.ic_profile_sleep_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_sleep_3, R.drawable.ic_profile_sleep_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_night, R.drawable.ic_profile_night_notify_color);
        profileIconNotifyColorId.put(ic_profile_call_1, R.drawable.ic_profile_call_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_food_1, R.drawable.ic_profile_food_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_food_2, R.drawable.ic_profile_food_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_food_3, R.drawable.ic_profile_food_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_food_4, R.drawable.ic_profile_food_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_food_5, R.drawable.ic_profile_food_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_alarm, R.drawable.ic_profile_alarm_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_1, R.drawable.ic_profile_car_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_2, R.drawable.ic_profile_car_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_3, R.drawable.ic_profile_car_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_4, R.drawable.ic_profile_car_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_5, R.drawable.ic_profile_car_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_6, R.drawable.ic_profile_car_6_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_7, R.drawable.ic_profile_car_7_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_8, R.drawable.ic_profile_car_8_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_9, R.drawable.ic_profile_car_9_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_10, R.drawable.ic_profile_car_10_notify_color);
        profileIconNotifyColorId.put(ic_profile_car_11, R.drawable.ic_profile_car_11_notify_color);
        profileIconNotifyColorId.put(ic_profile_steering_1, R.drawable.ic_profile_steering_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_airplane_4, R.drawable.ic_profile_airplane_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_airplane_1, R.drawable.ic_profile_airplane_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_airplane_2, R.drawable.ic_profile_airplane_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_airplane_3, R.drawable.ic_profile_airplane_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_ship_1, R.drawable.ic_profile_ship_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_ship_2, R.drawable.ic_profile_ship_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_ship_3, R.drawable.ic_profile_ship_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_tram_1, R.drawable.ic_profile_tram_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_tickets_1, R.drawable.ic_profile_tickets_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_tickets_2, R.drawable.ic_profile_tickets_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_travel_1, R.drawable.ic_profile_travel_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_1, R.drawable.ic_profile_culture_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_6, R.drawable.ic_profile_culture_6_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_7, R.drawable.ic_profile_culture_7_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_2, R.drawable.ic_profile_culture_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_8, R.drawable.ic_profile_culture_8_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_9, R.drawable.ic_profile_culture_9_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_3, R.drawable.ic_profile_culture_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_10, R.drawable.ic_profile_culture_10_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_11, R.drawable.ic_profile_culture_11_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_12, R.drawable.ic_profile_culture_12_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_13, R.drawable.ic_profile_culture_13_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_5, R.drawable.ic_profile_culture_5_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_14, R.drawable.ic_profile_culture_14_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_4, R.drawable.ic_profile_culture_4_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_15, R.drawable.ic_profile_culture_15_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_16, R.drawable.ic_profile_culture_16_notify_color);
        profileIconNotifyColorId.put(ic_profile_culture_17, R.drawable.ic_profile_culture_17_notify_color);
        profileIconNotifyColorId.put(ic_profile_battery_1, R.drawable.ic_profile_battery_1_notify_color);
        profileIconNotifyColorId.put(ic_profile_battery_2, R.drawable.ic_profile_battery_2_notify_color);
        profileIconNotifyColorId.put(ic_profile_battery_3, R.drawable.ic_profile_battery_3_notify_color);
        profileIconNotifyColorId.put(ic_profile_lock, R.drawable.ic_profile_lock_notify_color);
        profileIconNotifyColorId.put(ic_profile_wifi, R.drawable.ic_profile_wifi_notify_color);
        profileIconNotifyColorId.put(ic_profile_mobile_data, R.drawable.ic_profile_mobile_data_notify_color);
    }

//    static final int PARAMETER_TYPE_WIFI = 1;
//    static final int PARAMETER_TYPE_WIFIAP = 2;
//    static final int PARAMETER_CLOSE_ALL_APPLICATION = 3;

    // Empty constructor
    Profile(){
        //this._useCustomColor = true;
        //this._customColor = Color.YELLOW;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
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
                   int deviceAutoSync,
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
                   int applicationEnableWifiScanning,
                   int applicationEnableBluetoothScanning,
                   String durationNotificationSound,
                   boolean durationNotificationVibrate,
                   int deviceWiFiAPPrefs,
                   int applicationEnableLocationScanning,
                   int applicationEnableMobileCellScanning,
                   int applicationEnableOrientationScanning,
                   int headsUpNotifications,
                   int deviceForceStopApplicationChange,
                   String deviceForceStopApplicationPackageName,
                   long activationByUserCount,
                   int deviceNetworkTypePrefs,
                   int deviceCloseAllApplications,
                   int screenDarkMode,
                   int dtmfToneWhenDialing,
                   int soundOnTouch,
                   String volumeDTMF,
                   String volumeAccessibility,
                   String volumeBluetoothSCO,
                   long afterDurationProfile,
                   int alwaysOnDisplay,
                   int screenOnPermanent,
                   boolean volumeMuteSound,
                   int deviceLocationMode,
                   int applicationEnableNotificationScanning,
                   String generateNotification,
                   int cameraFlash,
                   int deviceNetworkTypeSIM1,
                   int deviceNetworkTypeSIM2,
                   //int deviceMobileDataSIM1,
                   //int deviceMobileDataSIM2,
                   String deviceDefaultSIMCards,
                   int deviceOnOffSIM1,
                   int deviceOnOffSIM2,
                   int soundRingtoneChangeSIM1,
                   String soundRingtoneSIM1,
                   int soundRingtoneChangeSIM2,
                   String soundRingtoneSIM2,
                   int soundNotificationChangeSIM1,
                   String soundNotificationSIM1,
                   int soundNotificationChangeSIM2,
                   String soundNotificationSIM2,
                   int soundSameRingtoneForBothSIMCards,
                   String deviceLiveWallpaper,
                   int vibrateNotifications,
                   String deviceWallpaperFolder,
                   int applicationDisableGlobalEventsRun,
                   int deviceVPNSettingsPrefs,
                   int endOfActivationType,
                   int endOfActivationTime,
                   int applicationEnablePeriodicScanning,
                   String deviceVPN,
                   String vibrationIntensityRinging,
                   String vibrationIntensityNotifications,
                   String vibrationIntensityTouchInteraction,
                   boolean volumeMediaChangeDuringPlay,
                   int applicationWifiScanInterval,
                   int applicationBluetoothScanInterval,
                   int applicationBluetoothLEScanDuration,
                   int applicationLocationScanInterval,
                   int applicationOrientationScanInterval,
                   int applicationPeriodicScanInterval,
                   String sendSMSContacts,
                   String sendSMSContactGroups,
                   //int sendSMSContactListType,
                   boolean sendSMSSendSMS,
                   String sendSMSSMSText,
                   String deviceWallpaperLockscreen,
                   boolean clearNotificationEnabled,
                   String clearNotificationApplications,
                   boolean clearNotificationCheckContacts,
                   String clearNotificationContacts,
                   String clearNotificationContactGroups,
                   boolean clearNotificationCheckText,
                   String clearNotificationText,
                   int screenNightLight,
                   int screenNightLightPrefs,
                   int screenOnOff
            )
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
        this._deviceAutoSync = deviceAutoSync;
        this._showInActivator = showInActivator;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._askForDuration = askForDuration;
        this._durationNotificationSound = durationNotificationSound;
        this._durationNotificationVibrate = durationNotificationVibrate;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWifiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._applicationEnableWifiScanning = applicationEnableWifiScanning;
        this._applicationEnableBluetoothScanning = applicationEnableBluetoothScanning;
        this._deviceWiFiAPPrefs = deviceWiFiAPPrefs;
        this._applicationEnableLocationScanning = applicationEnableLocationScanning;
        this._applicationEnableMobileCellScanning = applicationEnableMobileCellScanning;
        this._applicationEnableOrientationScanning = applicationEnableOrientationScanning;
        this._headsUpNotifications = headsUpNotifications;
        this._deviceForceStopApplicationChange = deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = deviceCloseAllApplications;
        this._screenDarkMode = screenDarkMode;
        this._dtmfToneWhenDialing = dtmfToneWhenDialing;
        this._soundOnTouch = soundOnTouch;
        this._volumeDTMF = volumeDTMF;
        this._volumeAccessibility = volumeAccessibility;
        this._volumeBluetoothSCO = volumeBluetoothSCO;
        this._afterDurationProfile = afterDurationProfile;
        this._alwaysOnDisplay = alwaysOnDisplay;
        this._screenOnPermanent = screenOnPermanent;
        this._volumeMuteSound = volumeMuteSound;
        this._deviceLocationMode = deviceLocationMode;
        this._applicationEnableNotificationScanning = applicationEnableNotificationScanning;
        this._generateNotification = generateNotification;
        this._cameraFlash = cameraFlash;
        this._deviceNetworkTypeSIM1 = deviceNetworkTypeSIM1;
        this._deviceNetworkTypeSIM2 = deviceNetworkTypeSIM2;
        //this._deviceMobileDataSIM1 = deviceMobileDataSIM1;
        //this._deviceMobileDataSIM2 = deviceMobileDataSIM2;
        this._deviceDefaultSIMCards = deviceDefaultSIMCards;
        this._deviceOnOffSIM1 = deviceOnOffSIM1;
        this._deviceOnOffSIM2 = deviceOnOffSIM2;
        this._soundRingtoneChangeSIM1 = soundRingtoneChangeSIM1;
        this._soundRingtoneSIM1 = soundRingtoneSIM1;
        this._soundRingtoneChangeSIM2 = soundRingtoneChangeSIM2;
        this._soundRingtoneSIM2 = soundRingtoneSIM2;
        this._soundNotificationChangeSIM1 = soundNotificationChangeSIM1;
        this._soundNotificationSIM1 = soundNotificationSIM1;
        this._soundNotificationChangeSIM2 = soundNotificationChangeSIM2;
        this._soundNotificationSIM2 = soundNotificationSIM2;
        this._soundSameRingtoneForBothSIMCards = soundSameRingtoneForBothSIMCards;
        this._deviceLiveWallpaper = deviceLiveWallpaper;
        this._vibrateNotifications = vibrateNotifications;
        this._deviceWallpaperFolder = deviceWallpaperFolder;
        this._applicationDisableGloabalEventsRun = applicationDisableGlobalEventsRun;
        this._deviceVPNSettingsPrefs = deviceVPNSettingsPrefs;
        this._endOfActivationType = endOfActivationType;
        this._endOfActivationTime = endOfActivationTime;
        this._applicationEnablePeriodicScanning = applicationEnablePeriodicScanning;
        this._deviceVPN = deviceVPN;
        this._vibrationIntensityRinging = vibrationIntensityRinging;
        this._vibrationIntensityNotifications = vibrationIntensityNotifications;
        this._vibrationIntensityTouchInteraction = vibrationIntensityTouchInteraction;
        this._volumeMediaChangeDuringPlay = volumeMediaChangeDuringPlay;
        this._applicationWifiScanInterval = applicationWifiScanInterval;
        this._applicationBluetoothScanInterval = applicationBluetoothScanInterval;
        this._applicationBluetoothLEScanDuration = applicationBluetoothLEScanDuration;
        this._applicationLocationScanInterval = applicationLocationScanInterval;
        this._applicationOrientationScanInterval = applicationOrientationScanInterval;
        this._applicationPeriodicScanInterval = applicationPeriodicScanInterval;
        this._sendSMSContacts = sendSMSContacts;
        this._sendSMSContactGroups = sendSMSContactGroups;
        //this._phoneCallsContactListType = sendSMSContactListType;
        this._sendSMSSendSMS = sendSMSSendSMS;
        this._sendSMSSMSText = sendSMSSMSText;
        this._deviceWallpaperLockScreen = deviceWallpaperLockscreen;
        this._clearNotificationEnabled = clearNotificationEnabled;
        this._clearNotificationApplications = clearNotificationApplications;
        this._clearNotificationCheckContacts = clearNotificationCheckContacts;
        this._clearNotificationContacts = clearNotificationContacts;
        this._clearNotificationContactGroups = clearNotificationContactGroups;
        this._clearNotificationCheckText = clearNotificationCheckText;
        this._clearNotificationText = clearNotificationText;
        this._screenNightLight = screenNightLight;
        this._screenNightLightPrefs = screenNightLightPrefs;
        this._screenOnOff = screenOnOff;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
        this._activationByUserCount = activationByUserCount;
    }

    // constructor
    Profile(String name,
            String icon,
            @SuppressWarnings("SameParameterValue") Boolean checked,
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
            int deviceAutoSync,
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
            int applicationEnableWifiScanning,
            int applicationEnableBluetoothScanning,
            String durationNotificationSound,
            boolean durationNotificationVibrate,
            int deviceWiFiAPPrefs,
            int applicationEnableLocationScanning,
            int applicationEnableMobileCellScanning,
            int applicationEnableOrientationScanning,
            int headsUpNotifications,
            int deviceForceStopApplicationChange,
            String deviceForceStopApplicationPackageName,
            long activationByUserCount,
            int deviceNetworkTypePrefs,
            int deviceCloseAllApplications,
            int screenDarkMode,
            int dtmfToneWhenDialing,
            int soundOnTouch,
            String volumeDTMF,
            String volumeAccessibility,
            String volumeBluetoothSCO,
            long afterDurationProfile,
            int alwaysOnDisplay,
            int screenOnPermanent,
            boolean volumeMuteSound,
            int deviceLocationMode,
            int applicationEnableNotificationScanning,
            String generateNotification,
            int cameraFlash,
            int deviceNetworkTypeSIM1,
            int deviceNetworkTypeSIM2,
            //int deviceMobileDataSIM1,
            //int deviceMobileDataSIM2,
            String deviceDefaultSIMCards,
            int deviceOnOffSIM1,
            int deviceOnOffSIM2,
            int soundRingtoneChangeSIM1,
            String soundRingtoneSIM1,
            int soundRingtoneChangeSIM2,
            String soundRingtoneSIM2,
            int soundNotificationChangeSIM1,
            String soundNotificationSIM1,
            int soundNotificationChangeSIM2,
            String soundNotificationSIM2,
            int soundSameRingtoneForBothSIMCards,
            String deviceLiveWallpaper,
            int vibrateNotifications,
            String deviceWallpaperFolder,
            int applicationDisableGlobalEventsRun,
            int deviceVPNSettingsPrefs,
            int endOfActivationType,
            int endOfActivationTime,
            int applicationEnablePeriodicScanning,
            String deviceVPN,
            String vibrationIntensityRinging,
            String vibrationIntensityNotifications,
            String vibrationIntensityTouchInteraction,
            boolean volumeMediaChangeDuringPlay,
            int applicationWifiScanInterval,
            int applicationBluetoothScanInterval,
            int applicationBluetoothLEScanDuration,
            int applicationLocationScanInterval,
            int applicationOrientationScanInterval,
            int applicationPeriodicScanInterval,
            String sendSMSContacts,
            String sendSMSContactGroups,
            //int sendSMSContactListType,
            boolean sendSMSSendSMS,
            String sendSMSSMSText,
            String deviceWallpaperLockscreen,
            boolean clearNotificationEnabled,
            String clearNotificationApplications,
            boolean clearNotificationCheckContacts,
            String clearNotificationContacts,
            String clearNotificationContactGroups,
            boolean clearNotificationCheckText,
            String clearNotificationText,
            int screenNightLight,
            int screenNightLightPrefs,
            int screenOnOff
    )
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
        this._deviceAutoSync = deviceAutoSync;
        this._showInActivator = showInActivator;
        this._deviceAutoRotate = deviceAutoRotate;
        this._deviceLocationServicePrefs = deviceLocationServicePrefs;
        this._volumeSpeakerPhone = volumeSpeakerPhone;
        this._deviceNFC = deviceNFC;
        this._duration = duration;
        this._afterDurationDo = afterDurationDo;
        this._askForDuration = askForDuration;
        this._durationNotificationSound = durationNotificationSound;
        this._durationNotificationVibrate = durationNotificationVibrate;
        this._deviceKeyguard = deviceKeyguard;
        this._vibrationOnTouch = vibrationOnTouch;
        this._deviceWiFiAP = deviceWiFiAP;
        this._devicePowerSaveMode = devicePowerSaveMode;
        this._deviceNetworkType = deviceNetworkType;
        this._notificationLed = notificationLed;
        this._vibrateWhenRinging = vibrateWhenRinging;
        this._deviceWallpaperFor = deviceWallpaperFor;
        this._hideStatusBarIcon = hideStatusBarIcon;
        this._lockDevice = lockDevice;
        this._deviceConnectToSSID = deviceConnectToSSID;
        this._applicationEnableWifiScanning = applicationEnableWifiScanning;
        this._applicationEnableBluetoothScanning = applicationEnableBluetoothScanning;
        this._deviceWiFiAPPrefs = deviceWiFiAPPrefs;
        this._applicationEnableLocationScanning = applicationEnableLocationScanning;
        this._applicationEnableMobileCellScanning = applicationEnableMobileCellScanning;
        this._applicationEnableOrientationScanning = applicationEnableOrientationScanning;
        this._headsUpNotifications = headsUpNotifications;
        this._deviceForceStopApplicationChange = deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = deviceCloseAllApplications;
        this._screenDarkMode = screenDarkMode;
        this._dtmfToneWhenDialing = dtmfToneWhenDialing;
        this._soundOnTouch = soundOnTouch;
        this._volumeDTMF = volumeDTMF;
        this._volumeAccessibility = volumeAccessibility;
        this._volumeBluetoothSCO = volumeBluetoothSCO;
        this._afterDurationProfile = afterDurationProfile;
        this._alwaysOnDisplay = alwaysOnDisplay;
        this._screenOnPermanent = screenOnPermanent;
        this._volumeMuteSound = volumeMuteSound;
        this._deviceLocationMode = deviceLocationMode;
        this._applicationEnableNotificationScanning = applicationEnableNotificationScanning;
        this._generateNotification = generateNotification;
        this._cameraFlash = cameraFlash;
        this._deviceNetworkTypeSIM1 = deviceNetworkTypeSIM1;
        this._deviceNetworkTypeSIM2 = deviceNetworkTypeSIM2;
        //this._deviceMobileDataSIM1 = deviceMobileDataSIM1;
        //this._deviceMobileDataSIM2 = deviceMobileDataSIM2;
        this._deviceDefaultSIMCards = deviceDefaultSIMCards;
        this._deviceOnOffSIM1 = deviceOnOffSIM1;
        this._deviceOnOffSIM2 = deviceOnOffSIM2;
        this._soundRingtoneChangeSIM1 = soundRingtoneChangeSIM1;
        this._soundRingtoneSIM1 = soundRingtoneSIM1;
        this._soundRingtoneChangeSIM2 = soundRingtoneChangeSIM2;
        this._soundRingtoneSIM2 = soundRingtoneSIM2;
        this._soundNotificationChangeSIM1 = soundNotificationChangeSIM1;
        this._soundNotificationSIM1 = soundNotificationSIM1;
        this._soundNotificationChangeSIM2 = soundNotificationChangeSIM2;
        this._soundNotificationSIM2 = soundNotificationSIM2;
        this._soundSameRingtoneForBothSIMCards = soundSameRingtoneForBothSIMCards;
        this._deviceLiveWallpaper = deviceLiveWallpaper;
        this._vibrateNotifications = vibrateNotifications;
        this._deviceWallpaperFolder = deviceWallpaperFolder;
        this._applicationDisableGloabalEventsRun = applicationDisableGlobalEventsRun;
        this._deviceVPNSettingsPrefs = deviceVPNSettingsPrefs;
        this._endOfActivationType = endOfActivationType;
        this._endOfActivationTime = endOfActivationTime;
        this._applicationEnablePeriodicScanning = applicationEnablePeriodicScanning;
        this._deviceVPN = deviceVPN;
        this._vibrationIntensityRinging = vibrationIntensityRinging;
        this._vibrationIntensityNotifications = vibrationIntensityNotifications;
        this._vibrationIntensityTouchInteraction = vibrationIntensityTouchInteraction;
        this._volumeMediaChangeDuringPlay = volumeMediaChangeDuringPlay;
        this._applicationWifiScanInterval = applicationWifiScanInterval;
        this._applicationBluetoothScanInterval = applicationBluetoothScanInterval;
        this._applicationBluetoothLEScanDuration = applicationBluetoothLEScanDuration;
        this._applicationLocationScanInterval = applicationLocationScanInterval;
        this._applicationOrientationScanInterval = applicationOrientationScanInterval;
        this._applicationPeriodicScanInterval = applicationPeriodicScanInterval;
        this._sendSMSContacts = sendSMSContacts;
        this._sendSMSContactGroups = sendSMSContactGroups;
        //this._sendSMSContactListType = sendSMSContactListType;
        this._sendSMSSendSMS = sendSMSSendSMS;
        this._sendSMSSMSText = sendSMSSMSText;
        this._deviceWallpaperLockScreen = deviceWallpaperLockscreen;
        this._clearNotificationEnabled = clearNotificationEnabled;
        this._clearNotificationApplications = clearNotificationApplications;
        this._clearNotificationCheckContacts = clearNotificationCheckContacts;
        this._clearNotificationContacts = clearNotificationContacts;
        this._clearNotificationContactGroups = clearNotificationContactGroups;
        this._clearNotificationCheckText = clearNotificationCheckText;
        this._clearNotificationText = clearNotificationText;
        this._screenNightLight = screenNightLight;
        this._screenNightLightPrefs = screenNightLightPrefs;
        this._screenOnOff = screenOnOff;

        this._iconBitmap = null;
        this._preferencesIndicator = null;
        this._activationByUserCount = activationByUserCount;
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
        this._deviceAutoSync = profile._deviceAutoSync;
        this._showInActivator = profile._showInActivator;
        this._deviceAutoRotate = profile._deviceAutoRotate;
        this._deviceLocationServicePrefs = profile._deviceLocationServicePrefs;
        this._volumeSpeakerPhone = profile._volumeSpeakerPhone;
        this._deviceNFC = profile._deviceNFC;
        this._duration = profile._duration;
        this._afterDurationDo = profile._afterDurationDo;
        this._askForDuration = profile._askForDuration;
        this._durationNotificationSound = profile._durationNotificationSound;
        this._durationNotificationVibrate = profile._durationNotificationVibrate;
        this._deviceKeyguard = profile._deviceKeyguard;
        this._vibrationOnTouch = profile._vibrationOnTouch;
        this._deviceWiFiAP = profile._deviceWiFiAP;
        this._devicePowerSaveMode = profile._devicePowerSaveMode;
        this._deviceNetworkType = profile._deviceNetworkType;
        this._notificationLed = profile._notificationLed;
        this._vibrateWhenRinging = profile._vibrateWhenRinging;
        this._deviceWallpaperFor = profile._deviceWallpaperFor;
        this._hideStatusBarIcon = profile._hideStatusBarIcon;
        this._lockDevice = profile._lockDevice;
        this._deviceConnectToSSID = profile._deviceConnectToSSID;
        this._applicationEnableWifiScanning = profile._applicationEnableWifiScanning;
        this._applicationEnableBluetoothScanning = profile._applicationEnableBluetoothScanning;
        this._deviceWiFiAPPrefs = profile._deviceWiFiAPPrefs;
        this._applicationEnableLocationScanning = profile._applicationEnableLocationScanning;
        this._applicationEnableMobileCellScanning = profile._applicationEnableMobileCellScanning;
        this._applicationEnableOrientationScanning = profile._applicationEnableOrientationScanning;
        this._headsUpNotifications = profile._headsUpNotifications;
        this._deviceForceStopApplicationChange = profile._deviceForceStopApplicationChange;
        this._deviceForceStopApplicationPackageName = profile._deviceForceStopApplicationPackageName;
        this._deviceNetworkTypePrefs = profile._deviceNetworkTypePrefs;
        this._deviceCloseAllApplications = profile._deviceCloseAllApplications;
        this._screenDarkMode = profile._screenDarkMode;
        this._dtmfToneWhenDialing = profile._dtmfToneWhenDialing;
        this._soundOnTouch = profile._soundOnTouch;
        this._volumeDTMF = profile._volumeDTMF;
        this._volumeAccessibility = profile._volumeAccessibility;
        this._volumeBluetoothSCO = profile._volumeBluetoothSCO;
        this._afterDurationProfile = profile._afterDurationProfile;
        this._alwaysOnDisplay = profile._alwaysOnDisplay;
        this._screenOnPermanent = profile._screenOnPermanent;
        this._volumeMuteSound = profile._volumeMuteSound;
        this._deviceLocationMode = profile._deviceLocationMode;
        this._applicationEnableNotificationScanning = profile._applicationEnableNotificationScanning;
        this._generateNotification = profile._generateNotification;
        this._cameraFlash = profile._cameraFlash;
        this._deviceNetworkTypeSIM1 = profile._deviceNetworkTypeSIM1;
        this._deviceNetworkTypeSIM2 = profile._deviceNetworkTypeSIM2;
        //this._deviceMobileDataSIM1 = profile._deviceMobileDataSIM1;
        //this._deviceMobileDataSIM2 = profile._deviceMobileDataSIM2;
        this._deviceDefaultSIMCards = profile._deviceDefaultSIMCards;
        this._deviceOnOffSIM1 = profile._deviceOnOffSIM1;
        this._deviceOnOffSIM2 = profile._deviceOnOffSIM2;
        this._soundRingtoneChangeSIM1 = profile._soundRingtoneChangeSIM1;
        this._soundRingtoneSIM1 = profile._soundRingtoneSIM1;
        this._soundRingtoneChangeSIM2 = profile._soundRingtoneChangeSIM2;
        this._soundRingtoneSIM2 = profile._soundRingtoneSIM2;
        this._soundNotificationChangeSIM1 = profile._soundNotificationChangeSIM1;
        this._soundNotificationSIM1 = profile._soundNotificationSIM1;
        this._soundNotificationChangeSIM2 = profile._soundNotificationChangeSIM2;
        this._soundNotificationSIM2 = profile._soundNotificationSIM2;
        this._soundSameRingtoneForBothSIMCards = profile._soundSameRingtoneForBothSIMCards;
        this._deviceLiveWallpaper = profile._deviceLiveWallpaper;
        this._vibrateNotifications = profile._vibrateNotifications;
        this._deviceWallpaperFolder = profile._deviceWallpaperFolder;
        this._applicationDisableGloabalEventsRun = profile._applicationDisableGloabalEventsRun;
        this._deviceVPNSettingsPrefs = profile._deviceVPNSettingsPrefs;
        this._endOfActivationType = profile._endOfActivationType;
        this._endOfActivationTime = profile._endOfActivationTime;
        this._applicationEnablePeriodicScanning = profile._applicationEnablePeriodicScanning;
        this._deviceVPN = profile._deviceVPN;
        this._vibrationIntensityRinging = profile._vibrationIntensityRinging;
        this._vibrationIntensityNotifications = profile._vibrationIntensityNotifications;
        this._vibrationIntensityTouchInteraction = profile._vibrationIntensityTouchInteraction;
        this._volumeMediaChangeDuringPlay = profile._volumeMediaChangeDuringPlay;
        this._applicationWifiScanInterval = profile._applicationWifiScanInterval;
        this._applicationBluetoothScanInterval = profile._applicationBluetoothScanInterval;
        this._applicationBluetoothLEScanDuration = profile._applicationBluetoothLEScanDuration;
        this._applicationLocationScanInterval = profile._applicationLocationScanInterval;
        this._applicationOrientationScanInterval = profile._applicationOrientationScanInterval;
        this._applicationPeriodicScanInterval = profile._applicationPeriodicScanInterval;
        this._sendSMSContacts = profile._sendSMSContacts;
        this._sendSMSContactGroups = profile._sendSMSContactGroups;
        //this._sendSMSContactListType = profile._sendSMSContactListType;
        this._sendSMSSendSMS = profile._sendSMSSendSMS;
        this._sendSMSSMSText = profile._sendSMSSMSText;
        this._deviceWallpaperLockScreen = profile._deviceWallpaperLockScreen;
        this._clearNotificationEnabled = profile._clearNotificationEnabled;
        this._clearNotificationApplications = profile._clearNotificationApplications;
        this._clearNotificationCheckContacts = profile._clearNotificationCheckContacts;
        this._clearNotificationContacts = profile._clearNotificationContacts;
        this._clearNotificationContactGroups = profile._clearNotificationContactGroups;
        this._clearNotificationCheckText = profile._clearNotificationCheckText;
        this._clearNotificationText = profile._clearNotificationText;
        this._screenNightLight = profile._screenNightLight;
        this._screenNightLightPrefs = profile._screenNightLightPrefs;
        this._screenOnOff = profile._screenOnOff;

        this._iconBitmap = profile._iconBitmap;
        this._preferencesIndicator = profile._preferencesIndicator;
        this._activationByUserCount = profile._activationByUserCount;
    }

    void mergeProfiles(long withProfileId, DataWrapper dataWrapper/*, boolean setDuration*/)
    {
        Profile withProfile = dataWrapper.getProfileById(withProfileId, false, false, false);

        if (withProfile != null) {
            if (this._id == 0) {
                // copy all data from withProfile when this profile is initialized
                copyProfile(withProfile);
            }
            else {
                this._id = withProfile._id;
                this._name = withProfile._name;
                this._icon = withProfile._icon;
                this._iconBitmap = withProfile._iconBitmap;
                this._preferencesIndicator = withProfile._preferencesIndicator;
                if (!withProfile._askForDuration/* && setDuration*/) {
                    this._duration = withProfile._duration;
                    this._afterDurationDo = withProfile._afterDurationDo;
                    this._afterDurationProfile = withProfile._afterDurationProfile;
                    this._endOfActivationType = withProfile._endOfActivationType;
                    this._endOfActivationTime = withProfile._endOfActivationTime;
                } else {
                    this._duration = 0;
                    this._afterDurationDo = AFTER_DURATION_DO_RESTART_EVENTS;
                    this._afterDurationProfile = PROFILE_NO_ACTIVATE;
                    this._endOfActivationType = Profile.AFTER_DURATION_DURATION_TYPE_DURATION;
                    this._endOfActivationTime = 0;
                }

                this._durationNotificationSound = withProfile._durationNotificationSound;
                this._durationNotificationVibrate = withProfile._durationNotificationVibrate;

                this._hideStatusBarIcon = withProfile._hideStatusBarIcon;
                this._deviceConnectToSSID = withProfile._deviceConnectToSSID;
                this._activationByUserCount = withProfile._activationByUserCount;

                if (withProfile._volumeRingerMode != 0) {
                    this._volumeRingerMode = withProfile._volumeRingerMode;
                    // also look at ProfilesPrefsFragment.disableDependedPref()
                    if (withProfile._volumeZenMode != 0) {
                        this._volumeZenMode = withProfile._volumeZenMode;
                        /*if ((this._volumeRingerMode == 5) && ((this._volumeZenMode == 1) || (this._volumeZenMode == 2))){
                            if (withProfile._vibrateWhenRinging != 0)
                                this._vibrateWhenRinging = withProfile._vibrateWhenRinging;
                            if (withProfile._vibrateNotifications != 0)
                                this._vibrateNotifications = withProfile._vibrateNotifications;
                        }*/
                    }
                    if (withProfile._vibrateWhenRinging != 0)
                        this._vibrateWhenRinging = withProfile._vibrateWhenRinging;
                    if (withProfile._vibrateNotifications != 0)
                        this._vibrateNotifications = withProfile._vibrateNotifications;
                }
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
                if (withProfile._deviceAutoSync != 0) {
                    if (withProfile._deviceAutoSync != 3) // toggle
                        this._deviceAutoSync = withProfile._deviceAutoSync;
                    else {
                        if (this._deviceAutoSync == 1)
                            this._deviceAutoSync = 2;
                        else if (this._deviceAutoSync == 2)
                            this._deviceAutoSync = 1;
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
                    this._deviceWallpaperChange = withProfile._deviceWallpaperChange;
                    this._deviceWallpaper = withProfile._deviceWallpaper;
                    this._deviceWallpaperLockScreen = withProfile._deviceWallpaperLockScreen;
                    this._deviceLiveWallpaper = withProfile._deviceLiveWallpaper;
                    this._deviceWallpaperFor = withProfile._deviceWallpaperFor;
                    this._deviceWallpaperFolder = withProfile._deviceWallpaperFolder;
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
                if (withProfile._lockDevice != 0)
                    this._lockDevice = withProfile._lockDevice;
                if (withProfile._applicationEnableWifiScanning != 0)
                    this._applicationEnableWifiScanning = withProfile._applicationEnableWifiScanning;
                if (withProfile._applicationEnableBluetoothScanning != 0)
                    this._applicationEnableBluetoothScanning = withProfile._applicationEnableBluetoothScanning;
                if (withProfile._deviceWiFiAPPrefs != 0)
                    this._deviceWiFiAPPrefs = withProfile._deviceWiFiAPPrefs;
                if (withProfile._applicationEnableLocationScanning != 0)
                    this._applicationEnableLocationScanning = withProfile._applicationEnableLocationScanning;
                if (withProfile._applicationEnableMobileCellScanning != 0)
                    this._applicationEnableMobileCellScanning = withProfile._applicationEnableMobileCellScanning;
                if (withProfile._applicationEnableOrientationScanning != 0)
                    this._applicationEnableOrientationScanning = withProfile._applicationEnableOrientationScanning;
                if (withProfile._headsUpNotifications != 0)
                    this._headsUpNotifications = withProfile._headsUpNotifications;
                if (withProfile._deviceForceStopApplicationChange != 0) {
                    this._deviceForceStopApplicationChange = withProfile._deviceForceStopApplicationChange;
                    if (this._deviceForceStopApplicationPackageName.isEmpty())
                        this._deviceForceStopApplicationPackageName = withProfile._deviceForceStopApplicationPackageName;
                    else
                        this._deviceForceStopApplicationPackageName = this._deviceForceStopApplicationPackageName + "|" +
                                withProfile._deviceForceStopApplicationPackageName;
                }
                if (withProfile._deviceNetworkTypePrefs != 0)
                    this._deviceNetworkTypePrefs = withProfile._deviceNetworkTypePrefs;
                if (withProfile._deviceCloseAllApplications != 0)
                    this._deviceCloseAllApplications = withProfile._deviceCloseAllApplications;
                if (withProfile._screenDarkMode != 0)
                    this._screenDarkMode = withProfile._screenDarkMode;
                if (withProfile._dtmfToneWhenDialing != 0)
                    this._dtmfToneWhenDialing = withProfile._dtmfToneWhenDialing;
                if (withProfile._soundOnTouch != 0)
                    this._soundOnTouch = withProfile._soundOnTouch;
                if (withProfile.getVolumeDTMFChange())
                    this._volumeDTMF = withProfile._volumeDTMF;
                if (withProfile.getVolumeAccessibilityChange())
                    this._volumeAccessibility = withProfile._volumeAccessibility;
                if (withProfile.getVolumeBluetoothSCOChange())
                    this._volumeBluetoothSCO = withProfile._volumeBluetoothSCO;
                if (withProfile._alwaysOnDisplay != 0)
                    this._alwaysOnDisplay = withProfile._alwaysOnDisplay;
                if (withProfile._screenOnPermanent != 0)
                    this._screenOnPermanent = withProfile._screenOnPermanent;
                if (withProfile._deviceLocationMode != 0)
                    this._deviceLocationMode = withProfile._deviceLocationMode;
                if (withProfile._applicationEnableNotificationScanning != 0)
                    this._applicationEnableNotificationScanning = withProfile._applicationEnableNotificationScanning;
                if (withProfile.getGenerateNotificationGenerate())
                    this._generateNotification = withProfile._generateNotification;
                if (withProfile._cameraFlash != 0)
                    this._cameraFlash = withProfile._cameraFlash;
                if (withProfile._deviceNetworkTypeSIM1 != 0)
                    this._deviceNetworkTypeSIM1 = withProfile._deviceNetworkTypeSIM1;
                if (withProfile._deviceNetworkTypeSIM2 != 0)
                    this._deviceNetworkTypeSIM2 = withProfile._deviceNetworkTypeSIM2;
                /*
                if (withProfile._deviceMobileDataSIM1 != 0) {
                    if (withProfile._deviceMobileDataSIM1 != 3) // toggle
                        this._deviceMobileDataSIM1 = withProfile._deviceMobileDataSIM1;
                    else {
                        if (this._deviceMobileDataSIM1 == 1)
                            this._deviceMobileDataSIM1 = 2;
                        else if (this._deviceMobileDataSIM1 == 2)
                            this._deviceMobileDataSIM1 = 1;
                    }
                }
                if (withProfile._deviceMobileDataSIM2 != 0) {
                    if (withProfile._deviceMobileDataSIM2 != 3) // toggle
                        this._deviceMobileDataSIM2 = withProfile._deviceMobileDataSIM2;
                    else {
                        if (this._deviceMobileDataSIM2 == 1)
                            this._deviceMobileDataSIM2 = 2;
                        else if (this._deviceMobileDataSIM2 == 2)
                            this._deviceMobileDataSIM2 = 1;
                    }
                }
                */
                if (!withProfile._deviceDefaultSIMCards.equals("0|0|0"))
                    this._deviceDefaultSIMCards = withProfile._deviceDefaultSIMCards;
                if (withProfile._deviceOnOffSIM1 != 0) {
                    if (withProfile._deviceOnOffSIM1 != 3) // toggle
                        this._deviceOnOffSIM1 = withProfile._deviceOnOffSIM1;
                    else {
                        if (this._deviceOnOffSIM1 == 1)
                            this._deviceOnOffSIM1 = 2;
                        else if (this._deviceOnOffSIM1 == 2)
                            this._deviceOnOffSIM1 = 1;
                    }
                }
                if (withProfile._deviceOnOffSIM2 != 0) {
                    if (withProfile._deviceOnOffSIM2 != 3) // toggle
                        this._deviceOnOffSIM2 = withProfile._deviceOnOffSIM2;
                    else {
                        if (this._deviceOnOffSIM2 == 1)
                            this._deviceOnOffSIM2 = 2;
                        else if (this._deviceOnOffSIM2 == 2)
                            this._deviceOnOffSIM2 = 1;
                    }
                }
                if (withProfile._soundRingtoneChangeSIM1 != 0) {
                    this._soundRingtoneChangeSIM1 = withProfile._soundRingtoneChangeSIM1;
                    this._soundRingtoneSIM1 = withProfile._soundRingtoneSIM1;
                }
                if (withProfile._soundRingtoneChangeSIM2 != 0) {
                    this._soundRingtoneChangeSIM2 = withProfile._soundRingtoneChangeSIM2;
                    this._soundRingtoneSIM2 = withProfile._soundRingtoneSIM2;
                }
                if (withProfile._soundNotificationChangeSIM1 != 0) {
                    this._soundNotificationChangeSIM1 = withProfile._soundNotificationChangeSIM1;
                    this._soundNotificationSIM1 = withProfile._soundNotificationSIM1;
                }
                if (withProfile._soundNotificationChangeSIM2 != 0) {
                    this._soundNotificationChangeSIM2 = withProfile._soundNotificationChangeSIM2;
                    this._soundNotificationSIM2 = withProfile._soundNotificationSIM2;
                }
                if (withProfile._soundSameRingtoneForBothSIMCards != 0)
                    this._soundSameRingtoneForBothSIMCards = withProfile._soundSameRingtoneForBothSIMCards;
                if (withProfile._applicationDisableGloabalEventsRun != 0)
                    this._applicationDisableGloabalEventsRun = withProfile._applicationDisableGloabalEventsRun;
                if (withProfile._deviceVPNSettingsPrefs != 0)
                    this._deviceVPNSettingsPrefs = withProfile._deviceVPNSettingsPrefs;
                if (withProfile._applicationEnablePeriodicScanning != 0)
                    this._applicationEnablePeriodicScanning = withProfile._applicationEnablePeriodicScanning;
                if (!withProfile._deviceVPN.startsWith("0"))
                    this._deviceVPN = withProfile._deviceVPN;
                if (withProfile.getVibrationIntensityRingingChange())
                    this._vibrationIntensityRinging = withProfile._vibrationIntensityRinging;
                if (withProfile.getVibrationIntensityNotificationsChange())
                    this._vibrationIntensityNotifications = withProfile._vibrationIntensityNotifications;
                if (withProfile.getVibrationIntensityTouchInteractionChange())
                    this._vibrationIntensityTouchInteraction = withProfile._vibrationIntensityTouchInteraction;
                if (withProfile._volumeMuteSound)
                    this._volumeMuteSound = true;
                if (withProfile._volumeMediaChangeDuringPlay)
                    this._volumeMediaChangeDuringPlay = true;
                if (withProfile._applicationWifiScanInterval != 0)
                    this._applicationWifiScanInterval = withProfile._applicationWifiScanInterval;
                if (withProfile._applicationBluetoothScanInterval != 0)
                    this._applicationBluetoothScanInterval = withProfile._applicationBluetoothScanInterval;
                if (withProfile._applicationBluetoothLEScanDuration != 0)
                    this._applicationBluetoothLEScanDuration = withProfile._applicationBluetoothLEScanDuration;
                if (withProfile._applicationLocationScanInterval != 0)
                    this._applicationLocationScanInterval = withProfile._applicationLocationScanInterval;
                if (withProfile._applicationOrientationScanInterval != 0)
                    this._applicationOrientationScanInterval = withProfile._applicationOrientationScanInterval;
                if (withProfile._applicationPeriodicScanInterval != 0)
                    this._applicationPeriodicScanInterval = withProfile._applicationPeriodicScanInterval;
                if (!withProfile._sendSMSContacts.isEmpty())
                    this._sendSMSContacts = withProfile._sendSMSContacts;
                if (!withProfile._sendSMSContactGroups.isEmpty())
                    this._sendSMSContactGroups = withProfile._sendSMSContactGroups;
                //if (withProfile._phoneCallsContactListType != 0)
                //    this._phoneCallsContactListType = withProfile._phoneCallsContactListType;
                if (withProfile._sendSMSSendSMS)
                    this._sendSMSSendSMS = true;
                if (!withProfile._sendSMSSMSText.isEmpty())
                    this._sendSMSSMSText = withProfile._sendSMSSMSText;
                if (withProfile._clearNotificationEnabled)
                    this._clearNotificationEnabled = true;
                if (!withProfile._clearNotificationApplications.isEmpty())
                    this._clearNotificationApplications = withProfile._clearNotificationApplications;
                if (withProfile._clearNotificationCheckContacts)
                    this._clearNotificationCheckContacts = true;
                if (!withProfile._clearNotificationContacts.isEmpty())
                    this._clearNotificationContacts = withProfile._clearNotificationContacts;
                if (!withProfile._clearNotificationContactGroups.isEmpty())
                    this._clearNotificationContactGroups = withProfile._clearNotificationContactGroups;
                if (withProfile._clearNotificationCheckText)
                    this._clearNotificationCheckText = true;
                if (!withProfile._clearNotificationText.isEmpty())
                    this._clearNotificationText = withProfile._clearNotificationText;
                if (withProfile._screenNightLight != 0)
                    this._screenNightLight = withProfile._screenNightLight;
                if (withProfile._screenNightLightPrefs != 0)
                    this._screenNightLightPrefs = withProfile._screenNightLightPrefs;
                if (withProfile._screenOnOff != 0)
                    this._screenOnOff = withProfile._screenOnOff;
            }

            // set merged profile as activated
            DatabaseHandler.getInstance(dataWrapper.context).activateProfile(withProfile);
//            Log.e("Pofile.mergeProfiles", "profile to db="+withProfile._id);
            dataWrapper.setProfileActive(withProfile);

            /* Do not log this, logged is merged profile in EventsHandler
            String profileIcon = withProfile._icon;
            PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_PROFILE_ACTIVATION, null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(withProfile, true, "", false, false, false, dataWrapper, false, dataWrapper.context),
                                    profileIcon, 0);*/

        }
    }

    // compare profies for check if withProfile has any change
    // used for profilie activation by event
    // return: false = compared profiles are not the same
    boolean compareProfile(Profile withProfile)
    {
        if (withProfile != null) {

            if (this._id != withProfile._id) {
                return false;
            }

            if (this._afterDurationDo == AFTER_DURATION_DO_SPECIFIC_PROFILE) {
                if (this._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) {
                    // duration
                    if (this._duration > 0) {
                        if (this._afterDurationDo != withProfile._afterDurationDo) {
                            return false;
                        }
                    }
                }
                else
                if (this._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) {
                    // exact time
                    Calendar now = Calendar.getInstance();

                    Calendar configuredTime = Calendar.getInstance();
                    configuredTime.set(Calendar.HOUR_OF_DAY, this._endOfActivationTime / 60);
                    configuredTime.set(Calendar.MINUTE, this._endOfActivationTime % 60);
                    configuredTime.set(Calendar.SECOND, 0);
                    configuredTime.set(Calendar.MILLISECOND, 0);

                    if (now.getTimeInMillis() < configuredTime.getTimeInMillis()) {
                        // configured time is not expired
                        if (this._afterDurationDo != withProfile._afterDurationDo) {
                            return false;
                        }
                    }
                }
            }

            if (this._volumeRingerMode != withProfile._volumeRingerMode) {
                return false;
            }
            if (this._volumeZenMode != withProfile._volumeZenMode) {
                return false;
            }
            if ((this._volumeRingerMode == 1) || (this._volumeRingerMode == 4)) {
                if (this._vibrateWhenRinging != withProfile._vibrateWhenRinging) {
                    return false;
                }
                if (this._vibrateNotifications != withProfile._vibrateNotifications) {
                    return false;
                }
            }
            if ((this._volumeRingerMode == 5) && ((this._volumeZenMode == 1) || (this._volumeZenMode == 2))){
                if (this._vibrateWhenRinging != withProfile._vibrateWhenRinging) {
                    return false;
                }
                if (this._vibrateNotifications != withProfile._vibrateNotifications) {
                    return false;
                }
            }
            if (!this._volumeRingtone.equals(withProfile._volumeRingtone)) {
                return false;
            }
            if (!this._volumeNotification.equals(withProfile._volumeNotification)) {
                return false;
            }
            if (!this._volumeMedia.equals(withProfile._volumeMedia)) {
                return false;
            }
            if (!this._volumeAlarm.equals(withProfile._volumeAlarm)) {
                return false;
            }
            if (!this._volumeSystem.equals(withProfile._volumeSystem)) {
                return false;
            }
            if (!this._volumeVoice.equals(withProfile._volumeVoice)) {
                return false;
            }
            if (this._soundRingtoneChange != withProfile._soundRingtoneChange) {
                return false;
            }
            if (this._soundRingtoneChange != 0) {
                if (!this._soundRingtone.equals(withProfile._soundRingtone)) {
                    return false;
                }
            }
            if (this._soundNotificationChange != withProfile._soundNotificationChange) {
                return false;
            }
            if (this._soundNotificationChange != 0) {
                if (!this._soundNotification.equals(withProfile._soundNotification)) {
                    return false;
                }
            }
            if (this._soundAlarmChange != withProfile._soundAlarmChange) {
                return false;
            }
            if (this._soundAlarmChange != 0) {
                if (!this._soundAlarm.equals(withProfile._soundAlarm)) {
                    return false;
                }
            }
            if (this._deviceAirplaneMode != withProfile._deviceAirplaneMode) {
                return false;
            }
            if (this._deviceMobileData != withProfile._deviceMobileData) {
                return false;
            }
            if (this._deviceMobileDataPrefs != withProfile._deviceMobileDataPrefs) {
                return false;
            }
            if (this._deviceWiFi != withProfile._deviceWiFi) {
                return false;
            }
            if (this._deviceBluetooth != withProfile._deviceBluetooth) {
                return false;
            }
            if (this._deviceGPS != withProfile._deviceGPS) {
                return false;
            }
            if (this._deviceLocationServicePrefs != withProfile._deviceLocationServicePrefs) {
                return false;
            }
            if (this._deviceScreenTimeout != withProfile._deviceScreenTimeout) {
                return false;
            }
            if (!this._deviceBrightness.equals(withProfile._deviceBrightness)) {
                return false;
            }
            if (this._deviceWallpaperChange != withProfile._deviceWallpaperChange) {
                return false;
            }
            if (this._deviceWallpaperChange != 0) {
                if (!this._deviceWallpaper.equals(withProfile._deviceWallpaper)) {
                    return false;
                }
                if (!this._deviceWallpaperLockScreen.equals(withProfile._deviceWallpaperLockScreen)) {
                    return false;
                }
                if (!this._deviceLiveWallpaper.equals(withProfile._deviceLiveWallpaper)) {
                    return false;
                }
                if (this._deviceWallpaperFor != withProfile._deviceWallpaperFor) {
                    return false;
                }
                if (!this._deviceWallpaperFolder.equals(withProfile._deviceWallpaperFolder)) {
                    return false;
                }
            }
            if (this._deviceRunApplicationChange != withProfile._deviceRunApplicationChange) {
                return false;
            }
            if (this._deviceRunApplicationChange != 0) {
                if (!this._deviceRunApplicationPackageName.equals(withProfile._deviceRunApplicationPackageName)) {
                    return false;
                }
            }
            if (this._deviceAutoSync != withProfile._deviceAutoSync) {
                return false;
            }
            if (this._deviceAutoRotate != withProfile._deviceAutoRotate) {
                return false;
            }
            if (this._volumeSpeakerPhone != withProfile._volumeSpeakerPhone) {
                return false;
            }
            if (this._deviceNFC != withProfile._deviceNFC) {
                return false;
            }
            if (this._deviceKeyguard != withProfile._deviceKeyguard) {
                return false;
            }
            if (this._vibrationOnTouch != withProfile._vibrationOnTouch) {
                return false;
            }
            if (this._deviceWiFiAP != withProfile._deviceWiFiAP) {
                return false;
            }
            if (this._devicePowerSaveMode != withProfile._devicePowerSaveMode) {
                return false;
            }
            if (this._deviceNetworkType != withProfile._deviceNetworkType) {
                return false;
            }
            if (this._notificationLed != withProfile._notificationLed) {
                return false;
            }
            if (this._lockDevice != withProfile._lockDevice) {
                return false;
            }
            if (!this._deviceConnectToSSID.equals(withProfile._deviceConnectToSSID)) {
                return false;
            }
            if (this._applicationEnableWifiScanning != withProfile._applicationEnableWifiScanning) {
                return false;
            }
            if (this._applicationWifiScanInterval != withProfile._applicationWifiScanInterval) {
                return false;
            }
            if (this._applicationEnableBluetoothScanning != withProfile._applicationEnableBluetoothScanning) {
                return false;
            }
            if (this._applicationBluetoothScanInterval != withProfile._applicationBluetoothScanInterval) {
                return false;
            }
            if (this._applicationBluetoothLEScanDuration != withProfile._applicationBluetoothLEScanDuration) {
                return false;
            }
            if (this._deviceWiFiAPPrefs != withProfile._deviceWiFiAPPrefs) {
                return false;
            }
            if (this._applicationEnableLocationScanning != withProfile._applicationEnableLocationScanning) {
                return false;
            }
            if (this._applicationLocationScanInterval != withProfile._applicationLocationScanInterval) {
                return false;
            }
            if (this._applicationEnableMobileCellScanning != withProfile._applicationEnableMobileCellScanning) {
                return false;
            }
            if (this._applicationEnableOrientationScanning != withProfile._applicationEnableOrientationScanning) {
                return false;
            }
            if (this._applicationOrientationScanInterval != withProfile._applicationOrientationScanInterval) {
                return false;
            }
            if (this._headsUpNotifications != withProfile._headsUpNotifications) {
                return false;
            }
            if (this._deviceForceStopApplicationChange != withProfile._deviceForceStopApplicationChange) {
                return false;
            }
            if (this._deviceForceStopApplicationChange != 0) {
                if (!this._deviceForceStopApplicationPackageName.equals(withProfile._deviceForceStopApplicationPackageName)) {
                    return false;
                }
            }
            if (this._deviceNetworkTypePrefs != withProfile._deviceNetworkTypePrefs) {
                return false;
            }
            if (this._deviceCloseAllApplications != withProfile._deviceCloseAllApplications) {
                return false;
            }
            if (this._screenDarkMode != withProfile._screenDarkMode) {
                return false;
            }
            if (this._dtmfToneWhenDialing != withProfile._dtmfToneWhenDialing) {
                return false;
            }
            if (this._soundOnTouch != withProfile._soundOnTouch) {
                return false;
            }
            if (!this._volumeDTMF.equals(withProfile._volumeDTMF)) {
                return false;
            }
            if (!this._volumeAccessibility.equals(withProfile._volumeAccessibility)) {
                return false;
            }
            if (!this._volumeBluetoothSCO.equals(withProfile._volumeBluetoothSCO)) {
                return false;
            }
            if (this._alwaysOnDisplay != withProfile._alwaysOnDisplay) {
                return false;
            }
            if (this._screenOnPermanent != withProfile._screenOnPermanent) {
                return false;
            }
            if (this._volumeMuteSound != withProfile._volumeMuteSound) {
                return false;
            }
            if (this._deviceLocationMode != withProfile._deviceLocationMode) {
                return false;
            }
            if (this._applicationEnableNotificationScanning != withProfile._applicationEnableNotificationScanning) {
                return false;
            }
            if (!this._generateNotification.equals(withProfile._generateNotification)) {
                return false;
            }
            if (this._cameraFlash != withProfile._cameraFlash) {
                return false;
            }
            if (this._deviceNetworkTypeSIM1 != withProfile._deviceNetworkTypeSIM1) {
                return false;
            }
            if (this._deviceNetworkTypeSIM2 != withProfile._deviceNetworkTypeSIM2) {
                return false;
            }
            //if (this._deviceMobileDataSIM1 != withProfile._deviceMobileDataSIM1) {
            //    return false;
            //}
            //if (this._deviceMobileDataSIM2 != withProfile._deviceMobileDataSIM2) {
            //    return false;
            //}
            if (!this._deviceDefaultSIMCards.equals(withProfile._deviceDefaultSIMCards)) {
                return false;
            }
            if (this._deviceOnOffSIM1 != withProfile._deviceOnOffSIM1) {
                return false;
            }
            if (this._deviceOnOffSIM2 != withProfile._deviceOnOffSIM2) {
                return false;
            }
            if (this._soundRingtoneChangeSIM1 != withProfile._soundRingtoneChangeSIM1) {
                return false;
            }
            if (this._soundRingtoneChangeSIM1 != 0) {
                if (!this._soundRingtoneSIM1.equals(withProfile._soundRingtoneSIM1)) {
                    return false;
                }
            }
            if (this._soundRingtoneChangeSIM2 != withProfile._soundRingtoneChangeSIM2) {
                return false;
            }
            if (this._soundRingtoneChangeSIM2 != 0) {
                if (!this._soundRingtoneSIM2.equals(withProfile._soundRingtoneSIM2)) {
                    return false;
                }
            }
            if (this._soundNotificationChangeSIM1 != withProfile._soundNotificationChangeSIM1) {
                return false;
            }
            if (this._soundNotificationChangeSIM1 != 0) {
                if (!this._soundNotificationSIM1.equals(withProfile._soundNotificationSIM1)) {
                    return false;
                }
            }
            if (this._soundNotificationChangeSIM2 != withProfile._soundNotificationChangeSIM2) {
                return false;
            }
            if (this._soundNotificationChangeSIM2 != 0) {
                if (!this._soundNotificationSIM2.equals(withProfile._soundNotificationSIM2)) {
                    return false;
                }
            }
            if (this._soundSameRingtoneForBothSIMCards != withProfile._soundSameRingtoneForBothSIMCards) {
                return false;
            }
            if (this._applicationDisableGloabalEventsRun != withProfile._applicationDisableGloabalEventsRun) {
                return false;
            }
            if (this._deviceVPNSettingsPrefs != withProfile._deviceVPNSettingsPrefs) {
                return false;
            }
            if (this._applicationEnablePeriodicScanning != withProfile._applicationEnablePeriodicScanning) {
                return false;
            }
            if (this._applicationPeriodicScanInterval != withProfile._applicationPeriodicScanInterval) {
                return false;
            }
            if (!this._deviceVPN.equals(withProfile._deviceVPN)) {
                return false;
            }
            if (!this._vibrationIntensityRinging.equals(withProfile._vibrationIntensityRinging)) {
                return false;
            }
            if (!this._vibrationIntensityNotifications.equals(withProfile._vibrationIntensityNotifications)) {
                return false;
            }
            if (!this._vibrationIntensityTouchInteraction.equals(withProfile._vibrationIntensityTouchInteraction)) {
                return false;
            }
            if (this._volumeMediaChangeDuringPlay != withProfile._volumeMediaChangeDuringPlay) {
                return false;
            }
            if (!this._sendSMSContacts.equals(withProfile._sendSMSContacts)) {
                return false;
            }
            if (!this._sendSMSContactGroups.equals(withProfile._sendSMSContactGroups)) {
                return false;
            }
            //if (this._phoneCallsContactListType != withProfile._phoneCallsContactListType) {
            //    return false;
            //}
            if (this._sendSMSSendSMS != withProfile._sendSMSSendSMS) {
                return false;
            }
            if (!this._sendSMSSMSText.equals(withProfile._sendSMSSMSText)) {
                return false;
            }
            if (this._clearNotificationEnabled != withProfile._clearNotificationEnabled) {
                return false;
            }
            if (!this._clearNotificationApplications.equals(withProfile._clearNotificationApplications)) {
                return false;
            }
            if (this._clearNotificationCheckContacts != withProfile._clearNotificationCheckContacts) {
                return false;
            }
            if (!this._clearNotificationContacts.equals(withProfile._clearNotificationContacts)) {
                return false;
            }
            if (!this._clearNotificationContactGroups.equals(withProfile._clearNotificationContactGroups)) {
                return false;
            }
            if (this._clearNotificationCheckText != withProfile._clearNotificationCheckText) {
                return false;
            }
            if (!this._clearNotificationText.equals(withProfile._clearNotificationText)) {
                return false;
            }
            if (this._screenNightLight != withProfile._screenNightLight) {
                return false;
            }

            return true;
        }
        return false;
    }

    // getting icon identifier
    String getIconIdentifier()
    {
        String value;
        try {
            String[] splits = _icon.split(StringConstants.STR_SPLIT_REGEX);
            value = splits[0];
        } catch (Exception e) {
            value = StringConstants.PROFILE_ICON_DEFAULT;
        }
        return value;
    }

    // getting where icon is resource id
    boolean getIsIconResourceID()
    {
        boolean value;
        try {
            String[] splits = _icon.split(StringConstants.STR_SPLIT_REGEX);
            value = splits[1].equals("1");

        } catch (Exception e) {
            value = true;
        }
        return value;
    }

    //get where icon has custom color
    boolean getUseCustomColorForIcon() {
        boolean value;
        try {
            String[] splits = _icon.split(StringConstants.STR_SPLIT_REGEX);
            value = splits[2].equals("1");

        } catch (Exception e) {
            value = false;
        }
        return value;
    }

    // get icon custom color
    int getIconCustomColor() {
        int value;
        try {
            String[] splits = _icon.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[3]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    int getVolumeRingtoneValue() {
        return ProfileStatic.getVolumeValue(_volumeRingtone);
    }

    boolean getVolumeRingtoneChange()
    {
        return ProfileStatic.getVolumeChange(_volumeRingtone);
    }

    boolean getVolumeRingtoneSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeRingtone.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    void setVolumeRingtoneValue(@SuppressWarnings("SameParameterValue") int value) {
        try {
            String[] splits = _volumeRingtone.split(StringConstants.STR_SPLIT_REGEX);
            splits[0] = String.valueOf(value);
            _volumeRingtone = "";
            StringBuilder _volume = new StringBuilder();
            for (String split : splits) {
                //if (!_volumeRingtone.isEmpty())
                //    _volumeRingtone = _volumeRingtone + "|";
                //_volumeRingtone = _volumeRingtone + split;
                if (_volume.length() > 0)
                    _volume.append("|");
                _volume.append(split);
            }
            _volumeRingtone = _volume.toString();
        } catch (Exception ignore) {
        }
    }

    int getVolumeNotificationValue()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeNotificationSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    void setVolumeNotificationValue(@SuppressWarnings("SameParameterValue") int value) {
        try {
            String[] splits = _volumeNotification.split(StringConstants.STR_SPLIT_REGEX);
            splits[0] = String.valueOf(value);
            _volumeNotification = "";
            StringBuilder _volume = new StringBuilder();
            for (String split : splits) {
                //if (!_volumeNotification.isEmpty())
                //    _volumeNotification = _volumeNotification + "|";
                //_volumeNotification = _volumeNotification + split;
                if (_volume.length() > 0)
                    _volume.append("|");
                _volume.append(split);
            }
            _volumeNotification = _volume.toString();
        } catch (Exception ignore) {
        }
    }

    int getVolumeMediaValue()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeMedia.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeMediaSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeMedia.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeAlarm.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeAlarm.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeAlarmSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeAlarm.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeSystem.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeSystem.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeSystemSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeSystem.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeVoice.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _volumeVoice.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeVoiceSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeVoice.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeDTMFValue()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeDTMFChange()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeDTMFSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeDTMF.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeAccessibilityValue()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeAccessibilityChange()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeAccessibilitySharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeAccessibility.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getVolumeBluetoothSCOValue()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value;
    }

    boolean getVolumeBluetoothSCOChange()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getVolumeBluetoothSCOSharedProfile()
    {
        int value;
        try {
            String[] splits = _volumeBluetoothSCO.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    private int getDeviceBrightnessValue()
    {
        int maximumValue = 100;
        int defaultValue = 50;
        int value;
        try {
            String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[0]);
            if ((value < 0) || (value > maximumValue))
                value = defaultValue;
        } catch (Exception e) {
            value = defaultValue;
        }
        return value;
    }

    boolean getDeviceBrightnessChange()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 0; // in preference dialog is checked=No change
    }

    boolean getDeviceBrightnessSharedProfile()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
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
            String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[2]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    boolean getDeviceBrightnessChangeLevel()
    {
        int value;
        try {
            String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            value = 1;
        }
        return value == 1;
    }

    int getDeviceBrightnessManualValue(Context context)
    {
        int percentage = getDeviceBrightnessValue();
        return ProfileStatic.convertPercentsToBrightnessManualValue(percentage, context);
    }

    /*
    float getDeviceBrightnessAdaptiveValue(Context context)
    {
        int percentage = getDeviceBrightnessValue();
        return ProfileStatic.convertPercentsToBrightnessAdaptiveValue(percentage, context);
    }
    */

    /*
    public void setDeviceBrightnessManualValue(int value)
    {
        int maxValue = getMaximumScreenBrightnessSetting();
        int minValue = getMinimumScreenBrightnessSetting();

        if (maxValue-minValue > 65535) {
            minValue = 0;
            maxValue = 65535;
        }

        long percentage = convertBrightnessToPercents(value, maxValue, minValue);

        //value|noChange|automatic|sharedProfile
        String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */
    /*
    public void setDeviceBrightnessAdaptiveValue(float value)
    {
        long percentage;
        if (value == BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET)
            percentage = Math.round(value); // keep BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET
        else
            percentage = Math.round(value * 50 + 50);

        //value|noChange|automatic|sharedProfile
        String[] splits = _deviceBrightness.split(StringConstants.STR_SPLIT_REGEX);
        // hm, found brightness values without default profile :-/
        if (splits.length == 4)
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|"+ splits[3];
        else
            _deviceBrightness = String.valueOf(percentage)+"|"+splits[1]+"|"+splits[2]+"|0";
    }
    */

    boolean getGenerateNotificationGenerate()
    {
        int value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[0]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    int getGenerateNotificationIconType()
    {
        int value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[1]);
        } catch (Exception e) {
            value = 1;
        }
        return value;
    }

    String getGenerateNotificationTitle()
    {
        String value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = splits[2];
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    String getGenerateNotificationBody()
    {
        String value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = splits[3];
        } catch (Exception e) {
            value = "";
        }
        return value;
    }

    boolean getGenerateNotificationShowLargeIcon()
    {
        int value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    boolean getGenerateNotificationReplaceWithPPPIcon()
    {
        int value;
        try {
            String[] splits = _generateNotification.split(StringConstants.STR_SPLIT_REGEX);
            value = Integer.parseInt(splits[5]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;
    }

    @SuppressWarnings("SameReturnValue")
    boolean getGenerateNotificationSharedProfile()
    {
        /*int value;
        try {
            String[] splits = _generateNotification.split("\\|");
            value = Integer.parseInt(splits[4]);
        } catch (Exception e) {
            value = 0;
        }
        return value == 1;*/
        return false;
    }

    //----------------------------------

    void generateIconBitmap(Context context, boolean monochrome, int monochromeValue, boolean useMonochromeValueForCustomIcon)
    {
        releaseIconBitmap();

        if (!getIsIconResourceID())
        {
            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            //Log.d("---- generateIconBitmap","resampleBitmapUri");
            _iconBitmap = BitmapManipulator.resampleBitmapUri(getIconIdentifier(), width, height, true, false, context);

            if (_iconBitmap == null)
            {
                // no icon found, set default icon
                _icon = StringConstants.PROFILE_ICON_DEFAULT+"|1|0|0";
                if (monochrome)
                {
                    //int iconResource = context.getResources().getIdentifier(getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME);
                    int iconResource = ProfileStatic.getIconResource(getIconIdentifier());
                    //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                    Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                    _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue/*, context*/);
                    // getIsIconResourceID must return false
                    //_icon = getIconIdentifier() + "|0";
                }
            }
            else
            if (monochrome) {
                float monoValue = 255f;
                if (monochromeValue == 0x00) monoValue = -255f;
                if (monochromeValue == 0x20) monoValue = -192f;
                if (monochromeValue == 0x40) monoValue = -128f;
                if (monochromeValue == 0x60) monoValue = -64f;
                if (monochromeValue == 0x80) monoValue = 0f;
                if (monochromeValue == 0xA0) monoValue = 64f;
                if (monochromeValue == 0xC0) monoValue = 128f;
                if (monochromeValue == 0xE0) monoValue = 192f;
                //if (monochromeValue == 0xFF) monoValue = 255f;
                _iconBitmap = BitmapManipulator.grayScaleBitmap(_iconBitmap);
                if (useMonochromeValueForCustomIcon)
                    _iconBitmap = BitmapManipulator.setBitmapBrightness(_iconBitmap, monoValue);
            }
            //_iconDrawable = null;
        }
        else
        if (monochrome)
        {
            //Resources resources = context.getResources();
            //int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME);
            int iconResource = ProfileStatic.getIconResource(getIconIdentifier());
            //int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
            _iconBitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue/*, context*/);
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
            /*Drawable drawable;
            drawable = ContextCompat.getDrawable(context, iconResource);
            drawable = context.getResources().getDrawable(iconResource, context.getTheme());
            _iconDrawable = BitmapManipulator.tintDrawableByValue(drawable, monochromeValue);
            _iconBitmap = null;*/
        }
        else
        if (getUseCustomColorForIcon()) {
            //Resources resources = context.getResources();
            //int iconResource = resources.getIdentifier(getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME);
            int iconResource = ProfileStatic.getIconResource(getIconIdentifier());
            //int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Bitmap bitmap = BitmapManipulator.resampleResource(resources, iconResource, width, height);
            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
            _iconBitmap = BitmapManipulator.recolorBitmap(bitmap, getIconCustomColor()/*, context*/);
            // getIsIconResourceID must return false
            //_icon = getIconIdentifier() + "|0";
        }
        else
            _iconBitmap = null;
    }

    void generatePreferencesIndicator(Context context, boolean monochrome, int indicatorMonochromeValue,
                                      int indicatorsType, float indicatorsLightnessValue)
    {
        releasePreferencesIndicator();

        ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();
        _preferencesIndicator = indicators.paint(this, monochrome, indicatorsType,indicatorsLightnessValue, context);
        if (_preferencesIndicator != null) {
            if (monochrome)
                _preferencesIndicator = BitmapManipulator.monochromeBitmap(_preferencesIndicator, indicatorMonochromeValue/*, context*/);
        }
    }

    Bitmap increaseProfileIconBrightnessForContext(Context context, Bitmap iconBitmap) {
        //if (ApplicationPreferences.applicationIncreaseBrightnessForProfileIcon) {
        try {
            //boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
            //(context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            //== Configuration.UI_MODE_NIGHT_YES;
            String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
            boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);

            if (nightModeOn) {
                int iconColor;
                if (getIsIconResourceID()) {
                    if (getUseCustomColorForIcon())
                        iconColor = getIconCustomColor();
                    else
                        iconColor = ProfileStatic.getIconDefaultColor(getIconIdentifier());
                } else {
                    //iconColor = BitmapManipulator.getDominantColor(_iconBitmap);
                    Palette palette = Palette.from(_iconBitmap).generate();
                    iconColor = palette.getDominantColor(0xff1c9cd7);
                }
                if (ColorUtils.calculateLuminance(iconColor) < Profile.MIN_PROFILE_ICON_LUMINANCE) {
                    if (iconBitmap != null) {
                        return BitmapManipulator.setBitmapBrightness(iconBitmap, BRIGHTNESS_VALUE_FOR_DARK_MODE);
                    } else {
                        int iconResource = ProfileStatic.getIconResource(getIconIdentifier());
                        Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                        return BitmapManipulator.setBitmapBrightness(bitmap, BRIGHTNESS_VALUE_FOR_DARK_MODE);
                    }
                }
            }
        } catch (Exception ignored) {}
        //}
        return null;
    }
    Bitmap increaseProfileIconBrightnessForActivity(Activity activity, Bitmap iconBitmap) {
        //if (ApplicationPreferences.applicationIncreaseBrightnessForProfileIcon) {
        try {
            if (activity != null) {
                //boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(activity.getApplicationContext());
                //(activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                //== Configuration.UI_MODE_NIGHT_YES;
                String applicationTheme = ApplicationPreferences.applicationTheme(activity, true);
                boolean nightModeOn = !applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE);

                if (nightModeOn) {
                    int iconColor;
                    if (getIsIconResourceID()) {
                        if (getUseCustomColorForIcon())
                            iconColor = getIconCustomColor();
                        else
                            iconColor = ProfileStatic.getIconDefaultColor(getIconIdentifier());
                    } else {
                        //iconColor = BitmapManipulator.getDominantColor(_iconBitmap);
                        Palette palette = Palette.from(_iconBitmap).generate();
                        iconColor = palette.getDominantColor(0xff1c9cd7);
                    }
                    if (ColorUtils.calculateLuminance(iconColor) < Profile.MIN_PROFILE_ICON_LUMINANCE) {
                        if (iconBitmap != null) {
                            return BitmapManipulator.setBitmapBrightness(iconBitmap, BRIGHTNESS_VALUE_FOR_DARK_MODE);
                        } else {
                            int iconResource = ProfileStatic.getIconResource(getIconIdentifier());
                            Bitmap bitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, activity);
                            return BitmapManipulator.setBitmapBrightness(bitmap, BRIGHTNESS_VALUE_FOR_DARK_MODE);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        //}
        return null;
    }

/*    int increaseNotificationDecorationBrightness(Context context) {
        boolean nightModeOn = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        if (nightModeOn) {
            int iconColor;
            if (getIsIconResourceID())
            {
                if (getUseCustomColorForIcon())
                    iconColor = getIconCustomColor();
                else
                    iconColor = Profile.getIconDefaultColor(getIconIdentifier());
            } else {
                //iconColor = BitmapManipulator.getDominantColor(_iconBitmap);
                Palette palette = Palette.from(_iconBitmap).generate();
                iconColor = palette.getDominantColor(0xff1c9cd7);
            }
            if (ColorUtils.calculateLuminance(iconColor) < Profile.MIN_PROFILE_ICON_LUMINANCE) {
                float[] hsv = new float[3];
                Color.colorToHSV(iconColor, hsv); // color to hsv
                hsv[2] = BRIGHTNESS_VALUE_FOR_DARK_MODE / 255f; // value component --> brightness
                //if (hsv[2] > 1.0f)
                //    hsv[2] = 1.0f;
                return Color.HSVToColor(hsv); // hsv to color
            }
        }
        return 0;
    }*/

    void releaseIconBitmap()
    {
        if (_iconBitmap != null)
        {
            //if (!_iconBitmap.isRecycled())
            try {
                _iconBitmap.recycle();
            } catch (Exception ignored) {}
            _iconBitmap = null;
        }
    }

    void releasePreferencesIndicator()
    {
        if (_preferencesIndicator != null)
        {
            //if (!_preferencesIndicator.isRecycled())
            try {
                _preferencesIndicator.recycle();
            } catch (Exception ignored) {}
            _preferencesIndicator = null;
        }
    }

    Spannable getProfileNameWithDuration(String eventName, String indicators, boolean multiLine, boolean durationInNextLine, Context context) {
        String profileName = _name;
        if (!eventName.isEmpty())
            profileName = profileName + " " + eventName;
        String durationString = "";
        boolean showEndTime = false;
        if (_askForDuration) {
            if (_checked) {
                if (ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id) != null) {
                    //noinspection DataFlowIssue
                    long endDurationTime = ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id);
                    if (endDurationTime > 0) {
                        durationString = "(" + StringConstants.DURATION_END + " " + ProfileStatic.timeDateStringFromTimestamp(context, endDurationTime) + ")";
                        showEndTime = true;
                    }
                }
            }
            if (!showEndTime) {
                durationString = "[" + StringConstants.CHAR_HARD_SPACE + context.getString(R.string.profile_event_name_ask_for_duration) + StringConstants.CHAR_HARD_SPACE + "]";
            }
        }
        else
        if (_endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) {
            if ((_duration > 0) && (_afterDurationDo != AFTER_DURATION_DO_NOTHING)) {
                if (_checked) {
                    if (ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id) != null) {
                        //noinspection DataFlowIssue
                        long endDurationTime = ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id);
                        if (endDurationTime > 0) {
                            durationString = "(" + StringConstants.DURATION_END + " " +
                                    ProfileStatic.timeDateStringFromTimestamp(context, endDurationTime) + ")";
                            showEndTime = true;
                        }
                    }
                }
                if (!showEndTime) {
                    durationString = "[" + StringConstants.END_OF_ACTIVATION_DURATION + " " + StringFormatUtils.getDurationString(_duration) + "]";
                }
            }
        }
        else
        if (_endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) {
            if (_afterDurationDo != AFTER_DURATION_DO_NOTHING) {
                if (_checked) {
                    // saved was configured ond of activation time
                    // (look at ProfileDurationAlarmBroadcastReceiver.setAlarm())
                    if (ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id) != null) {
                        //noinspection DataFlowIssue
                        long endOfActivationTime = ApplicationPreferences.prefActivatedProfileEndDurationTime.get(_id);
                        if (endOfActivationTime > 0) {
                            Calendar now = Calendar.getInstance();

                            Calendar configuredTime = Calendar.getInstance();
                            configuredTime.set(Calendar.HOUR_OF_DAY, (int) (endOfActivationTime / 60));
                            configuredTime.set(Calendar.MINUTE, (int) (endOfActivationTime % 60));
                            configuredTime.set(Calendar.SECOND, 0);
                            configuredTime.set(Calendar.MILLISECOND, 0);

                            if (now.getTimeInMillis() < configuredTime.getTimeInMillis()) {
                                // configured time is not expired
                                durationString = "(" + StringConstants.END_OF_ACTIVATION_TIME_END + " " +
                                        StringFormatUtils.getTimeString((int) endOfActivationTime) + ")";
                                showEndTime = true;
                            }
                        }
                    }
                }
                if (!showEndTime) {
                    //if (!_checked)
                        durationString = "[" + StringConstants.END_OF_ACTIVATION_TIME + " " + StringFormatUtils.getTimeString(_endOfActivationTime) + "]";
                }
            }
        }
        int startSpan = profileName.length();
        if (!indicators.isEmpty()) {
            if (multiLine)
                profileName = profileName + StringConstants.CHAR_NEW_LINE + indicators;
            else
                profileName = profileName + " " + indicators;
        }
        if (!durationString.isEmpty()) {
            if (durationInNextLine) {
                if (showEndTime /*_checked*/)
                    profileName = durationString + StringConstants.CHAR_NEW_LINE + profileName;
                else
                    profileName = profileName + StringConstants.CHAR_NEW_LINE + durationString;
            }
            else
                profileName = profileName + " " + durationString;
        }
        Spannable sbt = new SpannableString(profileName);
        if (!durationString.isEmpty()) {
            if (durationInNextLine && showEndTime/*_checked*/)
                sbt.setSpan(new RelativeSizeSpan(0.8f), 0, durationString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            else
                sbt.setSpan(new RelativeSizeSpan(0.8f), startSpan, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sbt;
    }

    void saveProfileToSharedPreferences(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(PREF_PROFILE_NAME, this._name);
        editor.putString(PREF_PROFILE_ICON, this._icon);
        editor.putBoolean(PREF_PROFILE_SHOW_IN_ACTIVATOR, this._showInActivator);
        editor.putString(PREF_PROFILE_DURATION, Integer.toString(this._duration));
        editor.putString(PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(this._afterDurationDo));
        editor.putBoolean(PREF_PROFILE_ASK_FOR_DURATION, this._askForDuration);
        editor.putString(PREF_PROFILE_END_OF_ACTIVATION_TYPE, Integer.toString(this._endOfActivationType));
        editor.putInt(PREF_PROFILE_END_OF_ACTIVATION_TIME, this._endOfActivationTime);
        editor.putString(PREF_PROFILE_DURATION_NOTIFICATION_SOUND, this._durationNotificationSound);
        editor.putBoolean(PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, this._durationNotificationVibrate);
        editor.putBoolean(PREF_PROFILE_HIDE_STATUS_BAR_ICON, this._hideStatusBarIcon);
        editor.putString(PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(this._volumeRingerMode));
        editor.putString(PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(this._volumeZenMode));
        editor.putString(PREF_PROFILE_VOLUME_RINGTONE, this._volumeRingtone);
        editor.putString(PREF_PROFILE_VOLUME_NOTIFICATION, this._volumeNotification);
        editor.putString(PREF_PROFILE_VOLUME_MEDIA, this._volumeMedia);
        editor.putString(PREF_PROFILE_VOLUME_ALARM, this._volumeAlarm);
        editor.putString(PREF_PROFILE_VOLUME_SYSTEM, this._volumeSystem);
        editor.putString(PREF_PROFILE_VOLUME_VOICE, this._volumeVoice);
        editor.putString(PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(this._soundRingtoneChange));
        String[] splits = this._soundRingtone.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_RINGTONE, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(this._soundNotificationChange));
        splits = this._soundNotification.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(this._soundAlarmChange));
        splits = this._soundAlarm.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_ALARM, splits[0]);
        editor.putString(PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(this._deviceAirplaneMode));
        editor.putString(PREF_PROFILE_DEVICE_WIFI, Integer.toString(this._deviceWiFi));
        editor.putString(PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(this._deviceBluetooth));
        editor.putString(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(this._deviceScreenTimeout));
        editor.putString(PREF_PROFILE_DEVICE_BRIGHTNESS, this._deviceBrightness);
        editor.putString(PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(this._deviceWallpaperChange));
        editor.putString(PREF_PROFILE_DEVICE_WALLPAPER, this._deviceWallpaper);
        editor.putString(PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(this._deviceMobileData));
        editor.putString(PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(this._deviceMobileDataPrefs));
        editor.putString(PREF_PROFILE_DEVICE_GPS, Integer.toString(this._deviceGPS));
        editor.putString(PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(this._deviceRunApplicationChange));
        editor.putString(PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, this._deviceRunApplicationPackageName);
        editor.putString(PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(this._deviceAutoSync));
        editor.putString(PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(this._deviceAutoRotate));
        editor.putString(PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(this._deviceLocationServicePrefs));
        editor.putString(PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(this._volumeSpeakerPhone));
        editor.putString(PREF_PROFILE_DEVICE_NFC, Integer.toString(this._deviceNFC));
        editor.putString(PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(this._deviceKeyguard));
        editor.putString(PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(this._vibrationOnTouch));
        editor.putString(PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(this._deviceWiFiAP));
        editor.putString(PREF_PROFILE_DEVICE_POWER_SAVE_MODE, Integer.toString(this._devicePowerSaveMode));
        editor.putString(PREF_PROFILE_DEVICE_NETWORK_TYPE, Integer.toString(this._deviceNetworkType));
        editor.putString(PREF_PROFILE_NOTIFICATION_LED, Integer.toString(this._notificationLed));
        editor.putString(PREF_PROFILE_VIBRATE_WHEN_RINGING, Integer.toString(this._vibrateWhenRinging));
        editor.putString(PREF_PROFILE_VIBRATE_NOTIFICATIONS, Integer.toString(this._vibrateNotifications));
        editor.putString(PREF_PROFILE_DEVICE_WALLPAPER_FOR, Integer.toString(this._deviceWallpaperFor));
        editor.putString(PREF_PROFILE_LOCK_DEVICE, Integer.toString(this._lockDevice));
        editor.putString(PREF_PROFILE_DEVICE_CONNECT_TO_SSID, this._deviceConnectToSSID);
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING, Integer.toString(this._applicationEnableWifiScanning));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING, Integer.toString(this._applicationEnableBluetoothScanning));
        editor.putString(PREF_PROFILE_DEVICE_WIFI_AP_PREFS, Integer.toString(this._deviceWiFiAPPrefs));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING, Integer.toString(this._applicationEnableLocationScanning));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, Integer.toString(this._applicationEnableMobileCellScanning));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING, Integer.toString(this._applicationEnableOrientationScanning));
        editor.putString(PREF_PROFILE_HEADS_UP_NOTIFICATIONS, Integer.toString(this._headsUpNotifications));
        editor.putString(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, Integer.toString(this._deviceForceStopApplicationChange));
        editor.putString(PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, this._deviceForceStopApplicationPackageName);
        editor.putString(PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, Integer.toString(this._deviceNetworkTypePrefs));
        editor.putString(PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, Integer.toString(this._deviceCloseAllApplications));
        editor.putString(PREF_PROFILE_SCREEN_DARK_MODE, Integer.toString(this._screenDarkMode));
        editor.putString(PREF_PROFILE_DTMF_TONE_WHEN_DIALING, Integer.toString(this._dtmfToneWhenDialing));
        editor.putString(PREF_PROFILE_SOUND_ON_TOUCH, Integer.toString(this._soundOnTouch));
        editor.putString(PREF_PROFILE_VOLUME_DTMF, this._volumeDTMF);
        editor.putString(PREF_PROFILE_VOLUME_ACCESSIBILITY, this._volumeAccessibility);
        editor.putString(PREF_PROFILE_VOLUME_BLUETOOTH_SCO, this._volumeBluetoothSCO);
        editor.putString(PREF_PROFILE_AFTER_DURATION_PROFILE, Long.toString(this._afterDurationProfile));
        editor.putString(PREF_PROFILE_ALWAYS_ON_DISPLAY, Integer.toString(this._alwaysOnDisplay));
        editor.putString(PREF_PROFILE_SCREEN_ON_PERMANENT, Integer.toString(this._screenOnPermanent));
        editor.putBoolean(PREF_PROFILE_VOLUME_MUTE_SOUND, this._volumeMuteSound);
        editor.putString(PREF_PROFILE_DEVICE_LOCATION_MODE, Integer.toString(this._deviceLocationMode));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING, Integer.toString(this._applicationEnableNotificationScanning));
        editor.putString(PREF_PROFILE_GENERATE_NOTIFICATION, this._generateNotification);
        editor.putString(PREF_PROFILE_CAMERA_FLASH, Integer.toString(this._cameraFlash));
        editor.putString(PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, Integer.toString(this._deviceNetworkTypeSIM1));
        editor.putString(PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, Integer.toString(this._deviceNetworkTypeSIM2));
        //editor.putString(PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, Integer.toString(this._deviceMobileDataSIM1));
        //editor.putString(PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, Integer.toString(this._deviceMobileDataSIM2));
        editor.putString(PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, this._deviceDefaultSIMCards);
        editor.putString(PREF_PROFILE_DEVICE_ONOFF_SIM1, Integer.toString(this._deviceOnOffSIM1));
        editor.putString(PREF_PROFILE_DEVICE_ONOFF_SIM2, Integer.toString(this._deviceOnOffSIM2));
        editor.putString(PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, Integer.toString(this._soundRingtoneChangeSIM1));
        splits = this._soundRingtoneSIM1.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_RINGTONE_SIM1, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, Integer.toString(this._soundNotificationChangeSIM1));
        splits = this._soundNotificationSIM1.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION_SIM1, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, Integer.toString(this._soundRingtoneChangeSIM2));
        splits = this._soundRingtoneSIM2.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_RINGTONE_SIM2, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, Integer.toString(this._soundNotificationChangeSIM2));
        splits = this._soundNotificationSIM2.split(StringConstants.STR_SPLIT_REGEX);
        editor.putString(PREF_PROFILE_SOUND_NOTIFICATION_SIM2, splits[0]);
        editor.putString(PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, Integer.toString(this._soundSameRingtoneForBothSIMCards));
        editor.putString(PREF_PROFILE_DEVICE_LIVE_WALLPAPER, this._deviceLiveWallpaper);
        editor.putString(PREF_PROFILE_DEVICE_WALLPAPER_FOLDER, this._deviceWallpaperFolder);
        editor.putString(PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, Integer.toString(this._applicationDisableGloabalEventsRun));
        editor.putString(PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, Integer.toString(this._deviceVPNSettingsPrefs));
        editor.putString(PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING, Integer.toString(this._applicationEnablePeriodicScanning));
        editor.putString(PREF_PROFILE_DEVICE_VPN, this._deviceVPN);
        editor.putString(PREF_PROFILE_VIBRATION_INTENSITY_RINGING, this._vibrationIntensityRinging);
        editor.putString(PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, this._vibrationIntensityNotifications);
        editor.putString(PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, this._vibrationIntensityTouchInteraction);
        editor.putBoolean(PREF_PROFILE_VOLUME_MEDIA_CHANGE_DURING_PLAY, this._volumeMediaChangeDuringPlay);
        editor.putString(PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL, Integer.toString(this._applicationWifiScanInterval));
        editor.putString(PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL, Integer.toString(this._applicationBluetoothScanInterval));
        editor.putString(PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, Integer.toString(this._applicationBluetoothLEScanDuration));
        editor.putString(PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL, Integer.toString(this._applicationLocationScanInterval));
        editor.putString(PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL, Integer.toString(this._applicationOrientationScanInterval));
        editor.putString(PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, Integer.toString(this._applicationPeriodicScanInterval));
        editor.putString(PREF_PROFILE_SEND_SMS_CONTACTS, this._sendSMSContacts);
        editor.putString(PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, this._sendSMSContactGroups);
        //editor.putString(PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE, Integer.toString(this._phoneCallsContactListType));
        editor.putBoolean(PREF_PROFILE_SEND_SMS_SEND_SMS, this._sendSMSSendSMS);
        editor.putString(PREF_PROFILE_SEND_SMS_SMS_TEXT, this._sendSMSSMSText);
        editor.putString(PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN, this._deviceWallpaperLockScreen);
        editor.putBoolean(PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, this._clearNotificationEnabled);
        editor.putString(PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS, this._clearNotificationApplications);
        editor.putBoolean(PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS, this._clearNotificationCheckContacts);
        editor.putBoolean(PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT, this._clearNotificationCheckText);
        editor.putString(PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS, this._clearNotificationContactGroups);
        editor.putString(PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS, this._clearNotificationContacts);
        editor.putString(PREF_PROFILE_CLEAR_NOTIFICATION_TEXT, this._clearNotificationText);
        editor.putString(PREF_PROFILE_SCREEN_NIGHT_LIGHT, Integer.toString(this._screenNightLight));
        editor.putString(PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS, Integer.toString(this._screenNightLightPrefs));
        editor.putString(PREF_PROFILE_SCREEN_ON_OFF, Integer.toString(this._screenOnOff));

        editor.apply();
    }

    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay) {
        int accessibilityEnabled = -99;

        if ((this._deviceForceStopApplicationChange == 1) ||
            (this._lockDevice != 0)) {

            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);

            if (this._deviceForceStopApplicationChange == 1) {
                if (extenderVersion == 0)
                    // not installed
                    accessibilityEnabled = -2;
                else
                if ((extenderVersion > 0) &&
                        (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED))
                    // old version
                    accessibilityEnabled = -1;
                else
                    accessibilityEnabled = -98;
            }
            if (this._lockDevice == 3) {
                if (extenderVersion == 0)
                    // not installed
                    accessibilityEnabled = -2;
                else
                if ((extenderVersion > 0) &&
                        (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED))
                    // old version
                    accessibilityEnabled = -1;
                else
                    accessibilityEnabled = -98;
            }
            if (accessibilityEnabled == -98) {
                // Extender is in right version
                if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                        /*, "Profile.isAccessibilityServiceEnabled (profile=" + _name + ")"*/))
                    // accessibility enabled
                    accessibilityEnabled = 1;
                else
                    // accessibility disabled
                    accessibilityEnabled = 0;
            }
        }

        if (accessibilityEnabled == -99)
            accessibilityEnabled = 1;

        return accessibilityEnabled;
    }

    int getVibrationIntensityRingingValue() {
        return ProfileStatic.getVibrationIntensityValue(_vibrationIntensityRinging);
    }

    boolean getVibrationIntensityRingingChange()
    {
        return ProfileStatic.getVibrationIntensityChange(_vibrationIntensityRinging);
    }

    /*
    void setVibrationIntensityRingingValue(int value) {
        try {
            String[] splits = _vibrationIntensityRinging.split(StringConstants.STR_SPLIT_REGEX);
            splits[0] = String.valueOf(value);
            _vibrationIntensityRinging = "";
            StringBuilder _value = new StringBuilder();
            for (String split : splits) {
                //if (!_vibrationIntensityRinging.isEmpty())
                //    _vibrationIntensityRinging = _vibrationIntensityRinging + "|";
                //_vibrationIntensityRinging = _vibrationIntensityRinging + split;
                if (_value.length() > 0)
                    _value.append("|");
                _value.append(split);
            }
            _vibrationIntensityRinging = _value.toString();
        } catch (Exception ignore) {
        }
    }
    */

    int getVibrationIntensityNotificationsValue() {
        return ProfileStatic.getVibrationIntensityValue(_vibrationIntensityNotifications);
    }

    boolean getVibrationIntensityNotificationsChange()
    {
        return ProfileStatic.getVibrationIntensityChange(_vibrationIntensityNotifications);
    }

    /*
    void setVibrationIntensityNotificationsValue(int value) {
        try {
            String[] splits = _vibrationIntensityNotifications.split(StringConstants.STR_SPLIT_REGEX);
            splits[0] = String.valueOf(value);
            _vibrationIntensityNotifications = "";
            StringBuilder _value = new StringBuilder();
            for (String split : splits) {
                //if (!_vibrationIntensityNotifications.isEmpty())
                //    _vibrationIntensityNotifications = _vibrationIntensityNotifications + "|";
                //_vibrationIntensityNotifications = _vibrationIntensityNotifications + split;
                if (_value.length() > 0)
                    _value.append("|");
                _value.append(split);
            }
            _vibrationIntensityNotifications = _value.toString();
        } catch (Exception ignore) {
        }
    }
    */

    int getVibrationIntensityTouchInteractionValue() {
        return ProfileStatic.getVibrationIntensityValue(_vibrationIntensityTouchInteraction);
    }

    boolean getVibrationIntensityTouchInteractionChange()
    {
        return ProfileStatic.getVibrationIntensityChange(_vibrationIntensityTouchInteraction);
    }

    /*
    void setVibrationIntensityTouchInteractionValue(int value) {
        try {
            String[] splits = _vibrationIntensityTouchInteraction.split(StringConstants.STR_SPLIT_REGEX);
            splits[0] = String.valueOf(value);
            _vibrationIntensityTouchInteraction = "";
            StringBuilder _value = new StringBuilder();
            for (String split : splits) {
                //if (!_vibrationIntensityTouchInteraction.isEmpty())
                //    _vibrationIntensityTouchInteraction = _vibrationIntensityTouchInteraction + "|";
                //_vibrationIntensityTouchInteraction = _vibrationIntensityTouchInteraction + split;
                if (_value.length() > 0)
                    _value.append("|");
                _value.append(split);
            }
            _vibrationIntensityTouchInteraction = _value.toString();
        } catch (Exception ignore) {
        }
    }
    */

}
