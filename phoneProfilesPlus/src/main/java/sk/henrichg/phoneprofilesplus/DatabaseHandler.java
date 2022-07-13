package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseHandler extends SQLiteOpenHelper {


    // All Static variables

    // singleton fields
    private static volatile DatabaseHandler instance;
    //private SQLiteDatabase writableDb;

    final Context context;
    
    // Database Version
    static final int DATABASE_VERSION = 2499;

    // Database Name
    static final String DATABASE_NAME = "phoneProfilesManager";

    // Table names
    static final String TABLE_PROFILES = "profiles";
    static final String TABLE_MERGED_PROFILE = "merged_profile";
    static final String TABLE_EVENTS = "events";
    static final String TABLE_EVENT_TIMELINE = "event_timeline";
    static final String TABLE_ACTIVITY_LOG = "activity_log";
    static final String TABLE_GEOFENCES = "geofences";
    static final String TABLE_SHORTCUTS = "shortcuts";
    static final String TABLE_MOBILE_CELLS = "mobile_cells";
    static final String TABLE_NFC_TAGS = "nfc_tags";
    static final String TABLE_INTENTS = "intents";

    // import/export
    static final String EXPORT_DBFILENAME = DATABASE_NAME + ".backup";
    final Lock importExportLock = new ReentrantLock();
    final Condition runningImportExportCondition  = importExportLock.newCondition();
    final Condition runningCommandCondition = importExportLock.newCondition();
    private boolean runningImportExport = false;
    private boolean runningCommand = false;
    static final int IMPORT_ERROR_BUG = 0;
    static final int IMPORT_ERROR_NEVER_VERSION = -999;
    static final int IMPORT_OK = 1;

//    // create, upgrade, downgrade
//    private final Condition runningUpgradeCondition = importExportLock.newCondition();
//    private boolean runningUpgrade = false;

    // profile type
    static final int PTYPE_CONNECT_TO_SSID = 1;
    static final int PTYPE_FORCE_STOP = 2;
    static final int PTYPE_LOCK_DEVICE = 3;

    // event type
    static final int ETYPE_ALL = -1;
    static final int ETYPE_TIME = 1;
    static final int ETYPE_BATTERY = 2;
    static final int ETYPE_CALL = 3;
    static final int ETYPE_ACCESSORY = 4;
    static final int ETYPE_CALENDAR = 5;
    static final int ETYPE_WIFI_CONNECTED = 6;
    static final int ETYPE_WIFI_NEARBY = 7;
    static final int ETYPE_SCREEN = 8;
    static final int ETYPE_BLUETOOTH_CONNECTED = 9;
    static final int ETYPE_BLUETOOTH_NEARBY = 10;
    static final int ETYPE_SMS = 11;
    static final int ETYPE_NOTIFICATION = 12;
    static final int ETYPE_APPLICATION = 13;
    static final int ETYPE_LOCATION = 14;
    static final int ETYPE_ORIENTATION = 15;
    static final int ETYPE_MOBILE_CELLS = 16;
    static final int ETYPE_NFC = 17;
    static final int ETYPE_RADIO_SWITCH = 18;
    static final int ETYPE_RADIO_SWITCH_WIFI = 19;
    static final int ETYPE_RADIO_SWITCH_BLUETOOTH = 20;
    static final int ETYPE_RADIO_SWITCH_MOBILE_DATA = 21;
    static final int ETYPE_RADIO_SWITCH_GPS = 22;
    static final int ETYPE_RADIO_SWITCH_NFC = 23;
    static final int ETYPE_RADIO_SWITCH_AIRPLANE_MODE = 24;
    static final int ETYPE_WIFI = 25;
    static final int ETYPE_BLUETOOTH = 26;
    static final int ETYPE_ALARM_CLOCK = 27;
    static final int ETYPE_TIME_TWILIGHT = 28;
    static final int ETYPE_BATTERY_WITH_LEVEL = 29;
    static final int ETYPE_ALL_SCANNER_SENSORS = 30;
    static final int ETYPE_DEVICE_BOOT = 31;
    static final int ETYPE_SOUND_PROFILE = 36;
    static final int ETYPE_PERIODIC = 37;
    static final int ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = 38;
    static final int ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = 39;
    static final int ETYPE_RADIO_SWITCH_SIM_ON_OFF = 40;
    static final int ETYPE_VOLUMES = 41;
    static final int ETYPE_ACTIVATED_PROFILE = 42;
    static final int ETYPE_ROAMING = 43;
    static final int ETYPE_VPN = 44;

    // Profiles Table Columns names
    static final String KEY_ID = "id";
    static final String KEY_NAME = "name";
    static final String KEY_ICON = "icon";
    static final String KEY_CHECKED = "checked";
    static final String KEY_PORDER = "porder";
    static final String KEY_VOLUME_RINGER_MODE = "volumeRingerMode";
    static final String KEY_VOLUME_ZEN_MODE = "volumeZenMode";
    static final String KEY_VOLUME_RINGTONE = "volumeRingtone";
    static final String KEY_VOLUME_NOTIFICATION = "volumeNotification";
    static final String KEY_VOLUME_MEDIA = "volumeMedia";
    static final String KEY_VOLUME_ALARM = "volumeAlarm";
    static final String KEY_VOLUME_SYSTEM = "volumeSystem";
    static final String KEY_VOLUME_VOICE = "volumeVoice";
    static final String KEY_SOUND_RINGTONE_CHANGE = "soundRingtoneChange";
    static final String KEY_SOUND_RINGTONE = "soundRingtone";
    static final String KEY_SOUND_NOTIFICATION_CHANGE = "soundNotificationChange";
    static final String KEY_SOUND_NOTIFICATION = "soundNotification";
    static final String KEY_SOUND_ALARM_CHANGE = "soundAlarmChange";
    static final String KEY_SOUND_ALARM = "soundAlarm";
    static final String KEY_DEVICE_AIRPLANE_MODE = "deviceAirplaneMode";
    static final String KEY_DEVICE_WIFI = "deviceWiFi";
    static final String KEY_DEVICE_BLUETOOTH = "deviceBluetooth";
    static final String KEY_DEVICE_SCREEN_TIMEOUT = "deviceScreenTimeout";
    static final String KEY_DEVICE_BRIGHTNESS = "deviceBrightness";
    static final String KEY_DEVICE_WALLPAPER_CHANGE = "deviceWallpaperChange";
    static final String KEY_DEVICE_WALLPAPER = "deviceWallpaper";
    static final String KEY_DEVICE_MOBILE_DATA = "deviceMobileData";
    static final String KEY_DEVICE_MOBILE_DATA_PREFS = "deviceMobileDataPrefs";
    static final String KEY_DEVICE_GPS = "deviceGPS";
    static final String KEY_DEVICE_RUN_APPLICATION_CHANGE = "deviceRunApplicationChange";
    static final String KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "deviceRunApplicationPackageName";
    static final String KEY_DEVICE_AUTOSYNC = "deviceAutosync";
    static final String KEY_SHOW_IN_ACTIVATOR = "showInActivator";
    static final String KEY_DEVICE_AUTOROTATE = "deviceAutoRotate";
    static final String KEY_DEVICE_LOCATION_SERVICE_PREFS = "deviceLocationServicePrefs";
    static final String KEY_VOLUME_SPEAKER_PHONE = "volumeSpeakerPhone";
    static final String KEY_DEVICE_NFC = "deviceNFC";
    static final String KEY_DURATION = "duration";
    static final String KEY_AFTER_DURATION_DO = "afterDurationDo";
    static final String KEY_ASK_FOR_DURATION = "askForDuration";
    static final String KEY_DURATION_NOTIFICATION_SOUND = "durationNotificationSound";
    static final String KEY_DURATION_NOTIFICATION_VIBRATE = "durationNotificationVibrate";
    static final String KEY_DEVICE_KEYGUARD = "deviceKeyguard";
    static final String KEY_VIBRATE_ON_TOUCH = "vibrateOnTouch";
    static final String KEY_DEVICE_WIFI_AP = "deviceWifiAP";
    static final String KEY_DEVICE_POWER_SAVE_MODE = "devicePowerSaveMode";
    static final String KEY_DEVICE_NETWORK_TYPE = "deviceNetworkType";
    static final String KEY_NOTIFICATION_LED = "notificationLed";
    static final String KEY_VIBRATE_WHEN_RINGING = "vibrateWhenRinging";
    static final String KEY_VIBRATE_NOTIFICATIONS = "vibrateNotifications";
    static final String KEY_DEVICE_WALLPAPER_FOR = "deviceWallpaperFor";
    static final String KEY_HIDE_STATUS_BAR_ICON = "hideStatusBarIcon";
    static final String KEY_LOCK_DEVICE = "lockDevice";
    static final String KEY_DEVICE_CONNECT_TO_SSID = "deviceConnectToSSID";
    static final String KEY_APPLICATION_DISABLE_WIFI_SCANNING = "applicationDisableWifiScanning";
    static final String KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING = "applicationDisableBluetoothScanning";
    static final String KEY_DEVICE_WIFI_AP_PREFS = "deviceWifiAPPrefs";
    static final String KEY_APPLICATION_DISABLE_LOCATION_SCANNING = "applicationDisableLocationScanning";
    static final String KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING = "applicationDisableMobileCellScanning";
    static final String KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING = "applicationDisableOrientationScanning";
    static final String KEY_HEADS_UP_NOTIFICATIONS = "headsUpNotifications";
    static final String KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE = "deviceForceStopApplicationChange";
    static final String KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = "deviceForceStopApplicationPackageName";
    static final String KEY_ACTIVATION_BY_USER_COUNT = "activationByUserCount";
    static final String KEY_DEVICE_NETWORK_TYPE_PREFS = "deviceNetworkTypePrefs";
    static final String KEY_DEVICE_CLOSE_ALL_APPLICATIONS = "deviceCloseAllApplications";
    static final String KEY_SCREEN_DARK_MODE = "screenNightMode";
    static final String KEY_DTMF_TONE_WHEN_DIALING = "dtmfToneWhenDialing";
    static final String KEY_SOUND_ON_TOUCH = "soundOnTouch";
    static final String KEY_VOLUME_DTMF = "volumeDTMF";
    static final String KEY_VOLUME_ACCESSIBILITY = "volumeAccessibility";
    static final String KEY_VOLUME_BLUETOOTH_SCO = "volumeBluetoothSCO";
    static final String KEY_AFTER_DURATION_PROFILE = "afterDurationProfile";
    static final String KEY_ALWAYS_ON_DISPLAY = "alwaysOnDisplay";
    static final String KEY_SCREEN_ON_PERMANENT = "screenOnPermanent";
    static final String KEY_VOLUME_MUTE_SOUND = "volumeMuteSound";
    static final String KEY_DEVICE_LOCATION_MODE = "deviceLocationMode";
    static final String KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING = "applicationDisableNotificationScanning";
    static final String KEY_GENERATE_NOTIFICATION = "generateNotification";
    static final String KEY_CAMERA_FLASH = "cameraFlash";
    static final String KEY_DEVICE_NETWORK_TYPE_SIM1 = "deviceNetworkTypeSIM1";
    static final String KEY_DEVICE_NETWORK_TYPE_SIM2 = "deviceNetworkTypeSIM2";
    static final String KEY_DEVICE_MOBILE_DATA_SIM1 = "deviceMobileDataSIM1";
    static final String KEY_DEVICE_MOBILE_DATA_SIM2 = "deviceMobileDataSIM2";
    static final String KEY_DEVICE_DEFAULT_SIM_CARDS = "deviceDefaultSIMCards";
    static final String KEY_DEVICE_ONOFF_SIM1 = "deviceOnOffSIM1";
    static final String KEY_DEVICE_ONOFF_SIM2 = "deviceOnOffSIM2";
    static final String KEY_SOUND_RINGTONE_CHANGE_SIM1 = "soundRingtoneChangeSIM1";
    static final String KEY_SOUND_RINGTONE_SIM1 = "soundRingtoneSIM1";
    static final String KEY_SOUND_RINGTONE_CHANGE_SIM2 = "soundRingtoneChangeSIM2";
    static final String KEY_SOUND_RINGTONE_SIM2 = "soundRingtoneSIM2";
    static final String KEY_SOUND_NOTIFICATION_CHANGE_SIM1 = "soundNotificationChangeSIM1";
    static final String KEY_SOUND_NOTIFICATION_SIM1 = "soundNotificationSIM1";
    static final String KEY_SOUND_NOTIFICATION_CHANGE_SIM2 = "soundNotificationChangeSIM2";
    static final String KEY_SOUND_NOTIFICATION_SIM2 = "soundNotificationSIM2";
    static final String KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS = "soundSameRingtoneForBothSIMCards";
    static final String KEY_DEVICE_LIVE_WALLPAPER = "deviceLiveWallpaper";
    static final String KEY_CHANGE_WALLPAPER_TIME = "deviceChangeWallpapaerTime";
    static final String KEY_DEVICE_WALLPAPER_FOLDER = "deviceWallpaperFolder";
    static final String KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN = "applicationDisableGlobalEventsRun";
    static final String KEY_DEVICE_VPN_SETTINGS_PREFS = "deviceVPNSettingsPrefs";
    static final String KEY_END_OF_ACTIVATION_TYPE = "endOfActivationType";
    static final String KEY_END_OF_ACTIVATION_TIME = "endOfActivationTime";
    static final String KEY_APPLICATION_DISABLE_PERIODIC_SCANNING = "applicationDisablePeriodicScanning";
    static final String KEY_DEVICE_VPN = "deviceVPN";

    // Events Table Columns names
    static final String KEY_E_ID = "id";
    static final String KEY_E_NAME = "name";
    static final String KEY_E_START_ORDER = "startOrder";
    static final String KEY_E_FK_PROFILE_START = "fkProfile";
    static final String KEY_E_STATUS = "status";
    static final String KEY_E_START_TIME = "startTime";
    static final String KEY_E_END_TIME = "endTime";
    static final String KEY_E_DAYS_OF_WEEK = "daysOfWeek";
    static final String KEY_E_USE_END_TIME = "useEndTime";
    static final String KEY_E_BATTERY_LEVEL = "batteryLevel";
    static final String KEY_E_NOTIFICATION_SOUND_START = "notificationSound";
    static final String KEY_E_BATTERY_LEVEL_LOW = "batteryLevelLow";
    static final String KEY_E_BATTERY_LEVEL_HIGHT = "batteryLevelHight";
    static final String KEY_E_BATTERY_CHARGING = "batteryCharging";
    static final String KEY_E_TIME_ENABLED = "timeEnabled";
    static final String KEY_E_BATTERY_ENABLED = "batteryEnabled";
    static final String KEY_E_CALL_ENABLED = "callEnabled";
    static final String KEY_E_CALL_EVENT = "callEvent";
    static final String KEY_E_CALL_CONTACTS = "callContacts";
    static final String KEY_E_CALL_CONTACT_LIST_TYPE = "contactListType";
    static final String KEY_E_FK_PROFILE_END = "fkProfileEnd";
    static final String KEY_E_FORCE_RUN = "forceRun";
    static final String KEY_E_BLOCKED = "blocked";
    static final String KEY_E_UNDONE_PROFILE = "undoneProfile";
    static final String KEY_E_PRIORITY = "priority";
    static final String KEY_E_ACCESSORY_ENABLED = "peripheralEnabled";
    static final String KEY_E_PERIPHERAL_TYPE = "peripheralType";
    static final String KEY_E_CALENDAR_ENABLED = "calendarEnabled";
    static final String KEY_E_CALENDAR_CALENDARS = "calendarCalendars";
    static final String KEY_E_CALENDAR_SEARCH_FIELD = "calendarSearchField";
    static final String KEY_E_CALENDAR_SEARCH_STRING = "calendarSearchString";
    static final String KEY_E_CALENDAR_EVENT_START_TIME = "calendarEventStartTime";
    static final String KEY_E_CALENDAR_EVENT_END_TIME = "calendarEventEndTime";
    static final String KEY_E_CALENDAR_EVENT_FOUND = "calendarEventFound";
    static final String KEY_E_WIFI_ENABLED = "wifiEnabled";
    static final String KEY_E_WIFI_SSID = "wifiSSID";
    static final String KEY_E_WIFI_CONNECTION_TYPE = "wifiConnectionType";
    static final String KEY_E_SCREEN_ENABLED = "screenEnabled";
    //static final String KEY_E_SCREEN_DELAY = "screenDelay";
    static final String KEY_E_SCREEN_EVENT_TYPE = "screenEventType";
    static final String KEY_E_DELAY_START = "delayStart";
    static final String KEY_E_IS_IN_DELAY_START = "isInDelay";
    static final String KEY_E_SCREEN_WHEN_UNLOCKED = "screenWhenUnlocked";
    static final String KEY_E_BLUETOOTH_ENABLED = "bluetoothEnabled";
    static final String KEY_E_BLUETOOTH_ADAPTER_NAME = "bluetoothAdapterName";
    static final String KEY_E_BLUETOOTH_CONNECTION_TYPE = "bluetoothConnectionType";
    static final String KEY_E_SMS_ENABLED = "smsEnabled";
    //static final String KEY_E_SMS_EVENT = "smsEvent";
    static final String KEY_E_SMS_CONTACTS = "smsContacts";
    static final String KEY_E_SMS_CONTACT_LIST_TYPE = "smsContactListType";
    static final String KEY_E_SMS_START_TIME = "smsStartTime";
    static final String KEY_E_CALL_CONTACT_GROUPS = "callContactGroups";
    static final String KEY_E_SMS_CONTACT_GROUPS = "smsContactGroups";
    static final String KEY_E_AT_END_DO = "atEndDo";
    static final String KEY_E_CALENDAR_AVAILABILITY = "calendarAvailability";
    static final String KEY_E_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    static final String KEY_E_FK_PROFILE_START_WHEN_ACTIVATED = "fkProfileStartWhenActivated";
    static final String KEY_E_SMS_DURATION = "smsDuration";
    static final String KEY_E_NOTIFICATION_ENABLED = "notificationEnabled";
    static final String KEY_E_NOTIFICATION_APPLICATIONS = "notificationApplications";
    static final String KEY_E_NOTIFICATION_DURATION = "notificationDuration";
    static final String KEY_E_NOTIFICATION_START_TIME = "notificationStartTime";
    static final String KEY_E_BATTERY_POWER_SAVE_MODE = "batteryPowerSaveMode";
    static final String KEY_E_BLUETOOTH_DEVICES_TYPE = "bluetoothDevicesType";
    static final String KEY_E_APPLICATION_ENABLED = "applicationEnabled";
    static final String KEY_E_APPLICATION_APPLICATIONS = "applicationApplications";
    static final String KEY_E_NOTIFICATION_END_WHEN_REMOVED = "notificationEndWhenRemoved";
    static final String KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS = "calendarIgnoreAllDayEvents";
    static final String KEY_E_LOCATION_ENABLED = "locationEnabled";
    static final String KEY_E_LOCATION_FK_GEOFENCE = "fklocationGeofenceId";
    static final String KEY_E_LOCATION_WHEN_OUTSIDE = "locationWhenOutside";
    static final String KEY_E_DELAY_END = "delayEnd";
    static final String KEY_E_IS_IN_DELAY_END = "isInDelayEnd";
    static final String KEY_E_START_STATUS_TIME = "startStatusTime";
    static final String KEY_E_PAUSE_STATUS_TIME = "pauseStatusTime";
    static final String KEY_E_ORIENTATION_ENABLED = "orientationEnabled";
    static final String KEY_E_ORIENTATION_SIDES = "orientationSides";
    static final String KEY_E_ORIENTATION_DISTANCE = "orientationDistance";
    static final String KEY_E_ORIENTATION_DISPLAY = "orientationDisplay";
    static final String KEY_E_ORIENTATION_IGNORE_APPLICATIONS = "orientationIgnoreApplications";
    static final String KEY_E_MOBILE_CELLS_ENABLED = "mobileCellsEnabled";
    static final String KEY_E_MOBILE_CELLS_WHEN_OUTSIDE = "mobileCellsWhenOutside";
    static final String KEY_E_MOBILE_CELLS_CELLS = "mobileCellsCells";
    static final String KEY_E_LOCATION_GEOFENCES = "fklocationGeofences";
    static final String KEY_E_NFC_ENABLED = "nfcEnabled";
    static final String KEY_E_NFC_NFC_TAGS = "nfcNfcTags";
    static final String KEY_E_NFC_START_TIME = "nfcStartTime";
    static final String KEY_E_NFC_DURATION = "nfcDuration";
    static final String KEY_E_SMS_PERMANENT_RUN = "smsPermanentRun";
    static final String KEY_E_NOTIFICATION_PERMANENT_RUN = "notificationPermanentRun";
    static final String KEY_E_NFC_PERMANENT_RUN = "nfcPermanentRun";
    static final String KEY_E_CALENDAR_START_BEFORE_EVENT = "calendarStartBeforeEvent";
    static final String KEY_E_RADIO_SWITCH_ENABLED = "radioSwitchEnabled";
    static final String KEY_E_RADIO_SWITCH_WIFI = "radioSwitchWifi";
    static final String KEY_E_RADIO_SWITCH_BLUETOOTH = "radioSwitchBluetooth";
    static final String KEY_E_RADIO_SWITCH_MOBILE_DATA = "radioSwitchMobileData";
    static final String KEY_E_RADIO_SWITCH_GPS = "radioSwitchGPS";
    static final String KEY_E_RADIO_SWITCH_NFC = "radioSwitchNFC";
    static final String KEY_E_RADIO_SWITCH_AIRPLANE_MODE = "radioSwitchAirplaneMode";
    static final String KEY_E_NOTIFICATION_VIBRATE_START = "notificationVibrate";
    static final String KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION = "eventNoPauseByManualActivation";
    static final String KEY_E_CALL_DURATION = "callDuration";
    static final String KEY_E_CALL_PERMANENT_RUN = "callPermanentRun";
    static final String KEY_E_CALL_START_TIME = "callStartTime";
    static final String KEY_E_NOTIFICATION_SOUND_REPEAT_START = "notificationSoundRepeat";
    static final String KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START = "notificationSoundRepeatInterval";
    static final String KEY_E_NOTIFICATION_IN_CALL = "notificationRingingCall";
    static final String KEY_E_NOTIFICATION_MISSED_CALL = "notificationMissedCall";
    static final String KEY_E_START_WHEN_ACTIVATED_PROFILE = "startWhenActivatedProfile";
    static final String KEY_E_BLUETOOTH_SENSOR_PASSED = "bluetoothSensorPassed";
    static final String KEY_E_LOCATION_SENSOR_PASSED = "locationSensorPassed";
    static final String KEY_E_MOBILE_CELLS_SENSOR_PASSED = "mobileCellsSensorPassed";
    static final String KEY_E_ORIENTATION_SENSOR_PASSED = "orientationSensorPassed";
    static final String KEY_E_WIFI_SENSOR_PASSED = "wifiSensorPassed";
    static final String KEY_E_APPLICATION_SENSOR_PASSED = "applicationSensorPassed";
    static final String KEY_E_BATTERY_SENSOR_PASSED = "batterySensorPassed";
    static final String KEY_E_CALENDAR_SENSOR_PASSED = "calendarSensorPassed";
    static final String KEY_E_CALL_SENSOR_PASSED = "callSensorPassed";
    static final String KEY_E_NFC_SENSOR_PASSED = "nfcSensorPassed";
    static final String KEY_E_NOTIFICATION_SENSOR_PASSED = "notificationSensorPassed";
    static final String KEY_E_ACCESSORY_SENSOR_PASSED = "peripheralSensorPassed";
    static final String KEY_E_RADIO_SWITCH_SENSOR_PASSED = "radioSwitchSensorPassed";
    static final String KEY_E_SCREEN_SENSOR_PASSED = "screenSensorPassed";
    static final String KEY_E_SMS_SENSOR_PASSED = "smsSensorPassed";
    static final String KEY_E_TIME_SENSOR_PASSED = "timeSensorPassed";
    static final String KEY_E_CALENDAR_ALL_EVENTS = "calendarAllEvents";
    static final String KEY_E_ALARM_CLOCK_ENABLED = "alarmClockEnabled";
    static final String KEY_E_ALARM_CLOCK_PERMANENT_RUN = "alarmClockPermanentRun";
    static final String KEY_E_ALARM_CLOCK_DURATION = "alarmClockDuration";
    static final String KEY_E_ALARM_CLOCK_START_TIME = "alarmClockStartTime";
    static final String KEY_E_ALARM_CLOCK_SENSOR_PASSED = "alarmClockSensorPassed";
    static final String KEY_E_NOTIFICATION_SOUND_END = "notificationSoundEnd";
    static final String KEY_E_NOTIFICATION_VIBRATE_END = "notificationVibrateEnd";
    static final String KEY_E_BATTERY_PLUGGED = "batteryPlugged";
    static final String KEY_E_TIME_TYPE = "timeType";
    static final String KEY_E_ORIENTATION_CHECK_LIGHT = "orientationCheckLight";
    static final String KEY_E_ORIENTATION_LIGHT_MIN = "orientationLightMin";
    static final String KEY_E_ORIENTATION_LIGHT_MAX = "orientationLightMax";
    static final String KEY_E_NOTIFICATION_CHECK_CONTACTS = "notificationCheckContacts";
    static final String KEY_E_NOTIFICATION_CONTACTS = "notificationContacts";
    static final String KEY_E_NOTIFICATION_CONTACT_GROUPS = "notificationContactGroups";
    static final String KEY_E_NOTIFICATION_CHECK_TEXT = "notificationCheckText";
    static final String KEY_E_NOTIFICATION_TEXT = "notificationText";
    static final String KEY_E_NOTIFICATION_CONTACT_LIST_TYPE = "notificationContactListType";
    static final String KEY_E_DEVICE_BOOT_ENABLED = "deviceBootEnabled";
    static final String KEY_E_DEVICE_BOOT_PERMANENT_RUN = "deviceBootPermanentRun";
    static final String KEY_E_DEVICE_BOOT_DURATION = "deviceBootDuration";
    static final String KEY_E_DEVICE_BOOT_START_TIME = "deviceBootStartTime";
    static final String KEY_E_DEVICE_BOOT_SENSOR_PASSED = "deviceBootSensorPassed";
    static final String KEY_E_ALARM_CLOCK_APPLICATIONS = "alarmClockApplications";
    static final String KEY_E_ALARM_CLOCK_PACKAGE_NAME = "alarmClockPackageName";
    static final String KEY_E_AT_END_HOW_UNDO = "atEndHowUndo";
    static final String KEY_E_CALENDAR_STATUS = "calendarStatus";
    static final String KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END = "manualProfileActivationAtEnd";
    static final String KEY_E_CALENDAR_EVENT_TODAY_EXISTS = "calendarEventTodayExists";
    static final String KEY_E_CALENDAR_DAY_CONTAINS_EVENT = "calendarDayContainsEvent";
    static final String KEY_E_CALENDAR_ALL_DAY_EVENTS = "calendarAllDayEvents";
    static final String KEY_E_ACCESSORY_TYPE = "accessoryType";
    static final String KEY_E_CALL_FROM_SIM_SLOT = "callFromSIMSlot";
    static final String KEY_E_CALL_FOR_SIM_CARD = "callForSIMCard";
    static final String KEY_E_SMS_FROM_SIM_SLOT = "smsFromSIMSlot";
    static final String KEY_E_SMS_FOR_SIM_CARD = "smsForSIMCard";
    static final String KEY_E_MOBILE_CELLS_FOR_SIM_CARD = "mobileCellsForSIMCard";
    static final String KEY_E_SOUND_PROFILE_ENABLED = "soundProfileEnabled";
    static final String KEY_E_SOUND_PROFILE_RINGER_MODES = "soundProfileRingerModes";
    static final String KEY_E_SOUND_PROFILE_ZEN_MODES = "soundProfileZenModes";
    static final String KEY_E_SOUND_PROFILE_SENSOR_PASSED = "soundProfileSensorPassed";
    static final String KEY_E_PERIODIC_ENABLED = "periodicEnabled";
    static final String KEY_E_PERIODIC_MULTIPLY_INTERVAL = "periodicMultiplyInterval";
    static final String KEY_E_PERIODIC_DURATION = "periodicDuration";
    static final String KEY_E_PERIODIC_START_TIME = "periodicStartTime";
    static final String KEY_E_PERIODIC_COUNTER = "periodicCounter";
    static final String KEY_E_PERIODIC_SENSOR_PASSED = "periodicSensorPassed";
    static final String KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = "radioSwitchDefaultSIMForCalls";
    static final String KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = "radioSwitchDefaultSIMForSMS";
    static final String KEY_E_RADIO_SWITCH_SIM_ON_OFF = "radioSwitchSIMOnOff";
    static final String KEY_E_VOLUMES_ENABLED = "volumesEnabled";
    static final String KEY_E_VOLUMES_SENSOR_PASSED = "volumesSensorPassed";
    static final String KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE = "notificationSoundStartPlayAlsoInSilentMode";
    static final String KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE = "notificationSoundEndPlayAlsoInSilentMode";
    static final String KEY_E_VOLUMES_RINGTONE = "volumesRingtone";
    static final String KEY_E_VOLUMES_NOTIFICATION = "volumesNotification";
    static final String KEY_E_VOLUMES_MEDIA = "volumesMedia";
    static final String KEY_E_VOLUMES_ALARM = "volumesAlarm";
    static final String KEY_E_VOLUMES_SYSTEM = "volumesSystem";
    static final String KEY_E_VOLUMES_VOICE = "volumesVoice";
    static final String KEY_E_VOLUMES_BLUETOOTHSCO = "volumesBluetoothSCO";
    static final String KEY_E_VOLUMES_ACCESSIBILITY = "volumesAccessibility";
    static final String KEY_E_ACTIVATED_PROFILE_ENABLED = "activatedProfileEnabled";
    static final String KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED = "activatedProfileSensorPassed";
    static final String KEY_E_ACTIVATED_PROFILE_START_PROFILE = "activatedProfileStartProfile";
    static final String KEY_E_ACTIVATED_PROFILE_END_PROFILE = "activatedProfileEndProfile";
    static final String KEY_E_ACTIVATED_PROFILE_RUNNING = "activatedProfileRunning";
    static final String KEY_E_ROAMING_ENABLED = "roamingEnabled";
    static final String KEY_E_ROAMING_CHECK_NETWORK = "roamingCheckNetwork";
    static final String KEY_E_ROAMING_CHECK_DATA = "roamingCheckData";
    static final String KEY_E_ROAMING_SENSOR_PASSED = "roamingSensorPassed";
    static final String KEY_E_ROAMING_FOR_SIM_CARD = "roamingForSIMCard";

    // EventTimeLine Table Columns names
    static final String KEY_ET_ID = "id";
    static final String KEY_ET_EORDER = "eorder";
    static final String KEY_ET_FK_EVENT = "fkEvent";
    static final String KEY_ET_FK_PROFILE_RETURN = "fkProfileReturn";

    // ActivityLog Columns names
    static final String KEY_AL_ID = "_id";  // for CursorAdapter must by this name
    static final String KEY_AL_LOG_TYPE = "logType";
    static final String KEY_AL_LOG_DATE_TIME = "logDateTime";
    static final String KEY_AL_EVENT_NAME = "eventName";
    static final String KEY_AL_PROFILE_NAME = "profileName";
    static final String KEY_AL_PROFILE_ICON = "profileIcon";
    static final String KEY_AL_DURATION_DELAY = "durationDelay";
    static final String KEY_AL_PROFILE_EVENT_COUNT = "profileEventCount";

    // Geofences Columns names
    static final String KEY_G_ID = "_id";
    static final String KEY_G_LATITUDE = "latitude";
    static final String KEY_G_LONGITUDE = "longitude";
    static final String KEY_G_RADIUS = "radius";
    static final String KEY_G_NAME = "name";
    static final String KEY_G_CHECKED = "checked";
    static final String KEY_G_TRANSITION = "transition";

    // Shortcuts Columns names
    static final String KEY_S_ID = "_id";
    static final String KEY_S_INTENT = "intent";
    static final String KEY_S_NAME = "name";

    // Mobile cells Columns names
    static final String KEY_MC_ID = "_id";
    static final String KEY_MC_CELL_ID = "cellId";
    static final String KEY_MC_NAME = "name";
    static final String KEY_MC_NEW = "new";
    static final String KEY_MC_LAST_CONNECTED_TIME = "lastConnectedTime";
    static final String KEY_MC_LAST_RUNNING_EVENTS = "lastRunningEvents";
    static final String KEY_MC_LAST_PAUSED_EVENTS = "lastPausedEvents";
    static final String KEY_MC_DO_NOT_DETECT = "doNotDetect";

    // NFC tags Columns names
    static final String KEY_NT_ID = "_id";
    static final String KEY_NT_NAME = "name";
    static final String KEY_NT_UID = "uid";

    // Intents Columns names
    static final String KEY_IN_ID = "_id";
    static final String KEY_IN_NAME = "_name";
    static final String KEY_IN_PACKAGE_NAME = "packageName";
    static final String KEY_IN_CLASS_NAME = "className";
    static final String KEY_IN_ACTION = "_action";
    static final String KEY_IN_DATA = "data";
    static final String KEY_IN_MIME_TYPE = "mimeType";
    static final String KEY_IN_EXTRA_KEY_1 = "extraKey1";
    static final String KEY_IN_EXTRA_VALUE_1 = "extraValue1";
    static final String KEY_IN_EXTRA_TYPE_1 = "extraType1";
    static final String KEY_IN_EXTRA_KEY_2 = "extraKey2";
    static final String KEY_IN_EXTRA_VALUE_2 = "extraValue2";
    static final String KEY_IN_EXTRA_TYPE_2 = "extraType2";
    static final String KEY_IN_EXTRA_KEY_3 = "extraKey3";
    static final String KEY_IN_EXTRA_VALUE_3 = "extraValue3";
    static final String KEY_IN_EXTRA_TYPE_3 = "extraType3";
    static final String KEY_IN_EXTRA_KEY_4 = "extraKey4";
    static final String KEY_IN_EXTRA_VALUE_4 = "extraValue4";
    static final String KEY_IN_EXTRA_TYPE_4 = "extraType4";
    static final String KEY_IN_EXTRA_KEY_5 = "extraKey5";
    static final String KEY_IN_EXTRA_VALUE_5 = "extraValue5";
    static final String KEY_IN_EXTRA_TYPE_5 = "extraType5";
    static final String KEY_IN_EXTRA_KEY_6 = "extraKey6";
    static final String KEY_IN_EXTRA_VALUE_6 = "extraValue6";
    static final String KEY_IN_EXTRA_TYPE_6 = "extraType6";
    static final String KEY_IN_EXTRA_KEY_7 = "extraKey7";
    static final String KEY_IN_EXTRA_VALUE_7 = "extraValue7";
    static final String KEY_IN_EXTRA_TYPE_7 = "extraType7";
    static final String KEY_IN_EXTRA_KEY_8 = "extraKey8";
    static final String KEY_IN_EXTRA_VALUE_8 = "extraValue8";
    static final String KEY_IN_EXTRA_TYPE_8 = "extraType8";
    static final String KEY_IN_EXTRA_KEY_9 = "extraKey9";
    static final String KEY_IN_EXTRA_VALUE_9 = "extraValue9";
    static final String KEY_IN_EXTRA_TYPE_9 = "extraType9";
    static final String KEY_IN_EXTRA_KEY_10 = "extraKey10";
    static final String KEY_IN_EXTRA_VALUE_10 = "extraValue10";
    static final String KEY_IN_EXTRA_TYPE_10 = "extraType10";
    static final String KEY_IN_CATEGORIES = "categories";
    static final String KEY_IN_FLAGS = "flags";
    //static final String KEY_IN_USED_COUNT = "usedCount";
    static final String KEY_IN_INTENT_TYPE = "intentType";
    static final String KEY_IN_DO_NOT_DELETE = "doNotDelete";

    static final String TEXT_TYPE = "TEXT";
    static final String INTEGER_TYPE = "INTEGER";
    static final String DATETIME_TYPE = "DATETIME";
    static final String DOUBLE_TYPE = "DOUBLE";
    static final String FLOAT_TYPE = "FLOAT";

    private DatabaseHandler(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static DatabaseHandler getInstance(Context context) {
        //Double check locking pattern
        if (instance == null) { //Check for the first time
            synchronized (DatabaseHandler.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) instance = new DatabaseHandler(context);
            }
        }
        return instance;
    }
    
    SQLiteDatabase getMyWritableDatabase() {
        //if ((writableDb == null) || (!writableDb.isOpen())) {
        //    writableDb = this.getWritableDatabase();
        //}
        //return writableDb;
        return this.getWritableDatabase();
    }
 
//    @Override
//    public synchronized void close() {
//        super.close();
//        if (writableDb != null) {
//            writableDb.close();
//            writableDb = null;
//        }
//    }

    /*
    // be sure to call this method by: DatabaseHandler.getInstance().closeConnection()
    // when application is closed by some means most likely
    // onDestroy method of application
    synchronized void closeConnection() {
        if (instance != null)
        {
            instance.close();
            instance = null;
        }
    }
    */

    @Override
    public void onCreate(SQLiteDatabase db) {
//        importExportLock.lock();
//        try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onCreate", "xxx");

                DatabaseHandlerCreateUpdateDB.createTables(db);
                DatabaseHandlerCreateUpdateDB.createIndexes(db);

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    /*@Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);
    }*/

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
//        importExportLock.lock();
//        try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onDowngrade", "xxx");

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("DatabaseHandler.onDowngrade", "oldVersion=" + oldVersion);
                    PPApplication.logE("DatabaseHandler.onDowngrade", "newVersion=" + newVersion);
                }*/

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MERGED_PROFILE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_TIMELINE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_LOG);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHORTCUTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_CELLS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NFC_TAGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTENTS);

                DatabaseHandlerCreateUpdateDB.createTables(db);
                DatabaseHandlerCreateUpdateDB.createIndexes(db);

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        importExportLock.lock();
        //try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onUpgrade", "xxx");

                if (PPApplication.logEnabled()) {
                    PPApplication.logE("DatabaseHandler.onUpgrade", "--------- START");
                    PPApplication.logE("DatabaseHandler.onUpgrade", "oldVersion=" + oldVersion);
                    PPApplication.logE("DatabaseHandler.onUpgrade", "newVersion=" + newVersion);
                }

                /*
                // Drop older table if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);

                // Create tables again
                onCreate(db);
                */

//                Log.e("DatabaseHandler.onUpgrade", "createTables");
                DatabaseHandlerCreateUpdateDB.createTables(db);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_PROFILES)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_PROFILES);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_MERGED_PROFILE)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_MERGED_PROFILE);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_EVENTS)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_EVENTS);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_EVENT_TIMELINE)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_EVENT_TIMELINE);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_ACTIVITY_LOG)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_ACTIVITY_LOG);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_GEOFENCES)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_GEOFENCES);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_SHORTCUTS)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_SHORTCUTS);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_MOBILE_CELLS)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_MOBILE_CELLS);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_NFC_TAGS)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_NFC_TAGS);
//                Log.e("DatabaseHandler.onUpgrade", "createTableColumsWhenNotExists (TABLE_INTENTS)");
                DatabaseHandlerCreateUpdateDB.createTableColumsWhenNotExists(db, TABLE_INTENTS);
//                Log.e("DatabaseHandler.onUpgrade", "createIndexes");
                DatabaseHandlerCreateUpdateDB.createIndexes(db);

//                Log.e("DatabaseHandler.onUpgrade", "updateDB");
                DatabaseHandlerCreateUpdateDB.updateDb(this, db, oldVersion);

//                Log.e("DatabaseHandler.onUpgrade", "afterUpdateDB");
                DatabaseHandlerCreateUpdateDB.afterUpdateDb(db);

                DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, 0, 0, 0f);
//                PPApplication.logE("[APP_START] DatabaseHandler.onUpgrade", "xxx");
                dataWrapper.restartEventsWithRescan(true, true, true, false, false, false);

                //PPApplication.sleep(10000); // for test only

                PPApplication.logE("DatabaseHandler.onUpgrade", " --------- END");

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//                Log.e("DatabaseHandler.onUpgrade", Log.getStackTraceString(e));
//                throw e;
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    void startRunningCommand() throws Exception {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "lock");
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningUpgrade=" + runningUpgrade);
//        }

//        if (runningUpgrade)
//            runningUpgradeCondition.await();
        if (runningImportExport)
            runningImportExportCondition.await();
        runningCommand = true;
    }

    void stopRunningCommand() {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "unlock");
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningUpgrade=" + runningUpgrade);
//        }

        runningCommand = false;
        runningCommandCondition.signalAll();
        importExportLock.unlock();
    }

    void startRunningImportExport() throws Exception {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "lock");
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningUpgrade=" + runningUpgrade);
//        }

//        if (runningUpgrade)
//            runningUpgradeCondition.await();
        if (runningCommand)
            runningCommandCondition.await();
        //PPApplication.logE("----------- DatabaseHandler.startRunningImportExport", "continue");
        runningImportExport = true;
    }

    void stopRunningImportExport() {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "unlock");
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningUpgrade=" + runningUpgrade);
//        }

        runningImportExport = false;
        runningImportExportCondition.signalAll();
        importExportLock.unlock();
        //PPApplication.logE("----------- DatabaseHandler.stopRunningImportExport", "unlock");
    }

/*
    private void startRunningUpgrade() throws Exception {
        if (PPApplication.logEnabled()) {
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "lock");
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningCommand=" + runningCommand);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningImportExport=" + runningImportExport);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningUpgrade=" + runningUpgrade);
        }

        if (runningImportExport)
            runningImportExportCondition.await();
        if (runningCommand)
            runningCommandCondition.await();
        runningUpgrade = true;
    }

    private void stopRunningUpgrade() {
        if (PPApplication.logEnabled()) {
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "unlock");
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningCommand=" + runningCommand);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningImportExport=" + runningImportExport);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningUpgrade=" + runningUpgrade);
        }

        runningUpgrade = false;
        runningUpgradeCondition.signalAll();
        importExportLock.unlock();
    }
 */

// PROFILES --------------------------------------------------------------------------------

    // Adding new profile
    void addProfile(Profile profile, @SuppressWarnings("SameParameterValue") boolean merged) {
        DatabaseHandlerProfiles.addProfile(this, profile, merged);
    }

    // Getting single profile
    Profile getProfile(long profile_id, boolean merged) {
        return DatabaseHandlerProfiles.getProfile(this, profile_id, merged);
    }

    // Getting All Profiles
    List<Profile> getAllProfiles() {
        return DatabaseHandlerProfiles.getAllProfiles(this);
    }

    // Updating single profile
    void updateProfile(Profile profile) {
        DatabaseHandlerProfiles.updateProfile(this, profile);
    }

    // Deleting single profile
    void deleteProfile(Profile profile) {
        DatabaseHandlerProfiles.deleteProfile(this, profile);
    }

    // Deleting all profiles
    boolean deleteAllProfiles() {
        return DatabaseHandlerProfiles.deleteAllProfiles(this);
    }

    void activateProfile(Profile profile)
    {
        DatabaseHandlerProfiles.activateProfile(this, profile);
    }

    void deactivateProfile()
    {
        DatabaseHandlerProfiles.deactivateProfile(this);
    }

    Profile getActivatedProfile()
    {
        return DatabaseHandlerProfiles.getActivatedProfile(this);
    }

    long getProfileIdByName(String name)
    {
        return DatabaseHandlerProfiles.getProfileIdByName(this, name);
    }

    void setProfileOrder(List<Profile> list)
    {
        DatabaseHandlerProfiles.setProfileOrder(this, list);
    }

    void getProfileIcon(Profile profile)
    {
        DatabaseHandlerProfiles.getProfileIcon(this, profile);
    }

    void saveMergedProfile(Profile profile) {
        DatabaseHandlerProfiles.saveMergedProfile(this, profile);
    }

    /*
    long getActivationByUserCount(long profileId) {
        return DatabaseHandlerProfiles.getActivationByUserCount(this, profileId);
    }
    */

    void increaseActivationByUserCount(Profile profile) {
        DatabaseHandlerProfiles.increaseActivationByUserCount(this, profile);
    }

    List<Profile> getProfilesForDynamicShortcuts(/*boolean counted*/) {
        return DatabaseHandlerProfiles.getProfilesForDynamicShortcuts(this);
    }

    List<Profile> getProfilesInQuickTilesForDynamicShortcuts() {
        return DatabaseHandlerProfiles.getProfilesInQuickTilesForDynamicShortcuts(this);
    }

    void updateProfileShowInActivator(Profile profile) {
        DatabaseHandlerProfiles.updateProfileShowInActivator(this, profile);
    }

// EVENTS --------------------------------------------------------------------------------

    // Adding new event
    void addEvent(Event event) {
        DatabaseHandlerEvents.addEvent(this, event);
    }

    // Getting single event
    Event getEvent(long event_id) {
        return DatabaseHandlerEvents.getEvent(this, event_id);
    }

    // Getting All Events
    List<Event> getAllEvents() {
        return DatabaseHandlerEvents.getAllEvents(this);
    }

    // Updating single event
    void updateEvent(Event event) {
        DatabaseHandlerEvents.updateEvent(this, event);
    }

    // Deleting single event
    void deleteEvent(Event event) {
        DatabaseHandlerEvents.deleteEvent(this, event);
    }

    // Deleting all events
    void deleteAllEvents() {
        DatabaseHandlerEvents.deleteAllEvents(this);
    }

    void unlinkEventsFromProfile(Profile profile)
    {
        DatabaseHandlerEvents.unlinkEventsFromProfile(this, profile);
    }

    void unlinkAllEvents()
    {
        DatabaseHandlerEvents.unlinkAllEvents(this);
    }

    void setEventStartOrder(List<Event> list)
    {
        DatabaseHandlerEvents.setEventStartOrder(this, list);
    }

    boolean isAnyEventEnabled() {
        return DatabaseHandlerEvents.isAnyEventEnabled(this);
    }

    int getEventStatus(Event event)
    {
        return DatabaseHandlerEvents.getEventStatus(this, event);
    }

    void updateEventStatus(Event event)
    {
        DatabaseHandlerEvents.updateEventStatus(this, event);
    }

    void updateEventBlocked(Event event)
    {
        DatabaseHandlerEvents.updateEventBlocked(this, event);
    }

    void unblockAllEvents()
    {
        DatabaseHandlerEvents.unblockAllEvents(this);
    }

    void updateAllEventsStatus(@SuppressWarnings("SameParameterValue") int fromStatus,
                               @SuppressWarnings("SameParameterValue") int toStatus)
    {
        DatabaseHandlerEvents.updateAllEventsStatus(this, fromStatus, toStatus);
    }

    long getEventIdByName(String name)
    {
        return DatabaseHandlerEvents.getEventIdByName(this, name);
    }

    int getEventSensorPassed(EventPreferences eventPreferences, int eventType)
    {
        return DatabaseHandlerEvents.getEventSensorPassed(this, eventPreferences, eventType);
    }

    void updateEventSensorPassed(Event event, int eventType)
    {
        DatabaseHandlerEvents.updateEventSensorPassed(this, event, eventType);
    }

    void updateAllEventSensorsPassed(Event event)
    {
        DatabaseHandlerEvents.updateAllEventSensorsPassedForEvent(this, event);
    }

    void updateAllEventsSensorsPassed(int sensorPassed)
    {
        DatabaseHandlerEvents.updateAllEventsSensorsPassed(this, sensorPassed);
    }

    int getTypeEventsCount(int eventType/*, boolean onlyRunning*/)
    {
        return DatabaseHandlerEvents.getTypeEventsCount(this, eventType);
    }

    int getNotStoppedEventsCount() {
        return DatabaseHandlerEvents.getNotStoppedEventsCount(this);
    }

    void updateEventCalendarTimes(Event event)
    {
        DatabaseHandlerEvents.updateEventCalendarTimes(this, event);
    }

    void setEventCalendarTimes(Event event)
    {
        DatabaseHandlerEvents.setEventCalendarTimes(this, event);
    }

    void updateEventCalendarTodayExists(Event event)
    {
        DatabaseHandlerEvents.updateEventCalendarTodayExists(this, event);
    }

    boolean getEventInDelayStart(Event event)
    {
        return DatabaseHandlerEvents.getEventInDelayStart(this, event);
    }

    void updateEventInDelayStart(Event event)
    {
        DatabaseHandlerEvents.updateEventInDelayStart(this, event);
    }

    void resetAllEventsInDelayStart()
    {
        DatabaseHandlerEvents.resetAllEventsInDelayStart(this);
    }

    boolean getEventInDelayEnd(Event event)
    {
        return DatabaseHandlerEvents.getEventInDelayEnd(this, event);
    }

    void updateEventInDelayEnd(Event event)
    {
        DatabaseHandlerEvents.updateEventInDelayEnd(this, event);
    }

    void updateSMSStartTime(Event event)
    {
        DatabaseHandlerEvents.updateSMSStartTime(this, event);
    }

    void getSMSStartTime(Event event)
    {
        DatabaseHandlerEvents.getSMSStartTime(this, event);
    }

    void updateNFCStartTime(Event event)
    {
        DatabaseHandlerEvents.updateNFCStartTime(this, event);
    }

    void getNFCStartTime(Event event)
    {
        DatabaseHandlerEvents.getNFCStartTime(this, event);
    }

    void updateCallStartTime(Event event)
    {
        DatabaseHandlerEvents.updateCallStartTime(this, event);
    }

    void getCallStartTime(Event event)
    {
        DatabaseHandlerEvents.getCallStartTime(this, event);
    }

    void updateAlarmClockStartTime(Event event)
    {
        DatabaseHandlerEvents.updateAlarmClockStartTime(this, event);
    }

    void getAlarmClockStartTime(Event event)
    {
        DatabaseHandlerEvents.getAlarmClockStartTime(this, event);
    }

    void updateDeviceBootStartTime(Event event)
    {
        DatabaseHandlerEvents.updateDeviceBootStartTime(this, event);
    }

    void getDeviceBootStartTime(Event event)
    {
        DatabaseHandlerEvents.getDeviceBootStartTime(this, event);
    }

    void updatePeriodicCounter(Event event)
    {
        DatabaseHandlerEvents.updatePeriodicCounter(this, event);
    }

    void updatePeriodicStartTime(Event event)
    {
        DatabaseHandlerEvents.updatePeriodicStartTime(this, event);
    }

    void getPeriodicStartTime(Event event)
    {
        DatabaseHandlerEvents.getPeriodicStartTime(this, event);
    }

    void updateEventForceRun(Event event) {
        DatabaseHandlerEvents.updateEventForceRun(this, event);
    }

    int getOrientationWithLightSensorEventsCount()
    {
        return DatabaseHandlerEvents.getOrientationWithLightSensorEventsCount(this);
    }

    void updateActivatedProfileSensorRunningParameter(Event event) {
        DatabaseHandlerEvents.updateActivatedProfileSensorRunningParameter(this, event);
    }

// EVENT TIMELINE ------------------------------------------------------------------

    // Adding time line
    void addEventTimeline(EventTimeline eventTimeline) {
        DatabaseHandlerEvents.addEventTimeline(this, eventTimeline);
    }

    // Getting all event timeline
    List<EventTimeline> getAllEventTimelines() {
        return DatabaseHandlerEvents.getAllEventTimelines(this);
    }

    // Deleting event timeline
    void deleteEventTimeline(EventTimeline eventTimeline) {
        DatabaseHandlerEvents.deleteEventTimeline(this, eventTimeline);
    }

    // Deleting all events from timeline
    void deleteAllEventTimelines(/*boolean updateEventStatus*/) {
        DatabaseHandlerEvents.deleteAllEventTimelines(this);
    }

    String getLastStartedEventName() {
        return DatabaseHandlerEvents.getLastStartedEventName(this);
    }

// GEOFENCES ----------------------------------------------------------------------

    // Adding new geofence
    void addGeofence(Geofence geofence) {
        DatabaseHandlerEvents.addGeofence(this, geofence);
    }

    // Getting single geofence
    Geofence getGeofence(long geofenceId) {
        return DatabaseHandlerEvents.getGeofence(this, geofenceId);
    }

    // Getting All geofences
    List<Geofence> getAllGeofences() {
        return DatabaseHandlerEvents.getAllGeofences(this);
    }

    // Updating single geofence
    void updateGeofence(Geofence geofence) {
        DatabaseHandlerEvents.updateGeofence(this, geofence);
    }

    void updateGeofenceTransition(long geofenceId, int geofenceTransition) {
        DatabaseHandlerEvents.updateGeofenceTransition(this, geofenceId, geofenceTransition);
    }

    void clearAllGeofenceTransitions() {
        DatabaseHandlerEvents.clearAllGeofenceTransitions(this);
    }

    // Deleting single geofence
    void deleteGeofence(long geofenceId) {
        DatabaseHandlerEvents.deleteGeofence(this, geofenceId);
    }

    void checkGeofence(String geofences, int check) {
        DatabaseHandlerEvents.checkGeofence(this, geofences, check);
    }

    Cursor getGeofencesCursor() {
        return DatabaseHandlerEvents.getGeofencesCursor(this);
    }

    String getGeofenceName(long geofenceId) {
        return DatabaseHandlerEvents.getGeofenceName(this, geofenceId);
    }

    String getCheckedGeofences() {
        return DatabaseHandlerEvents.getCheckedGeofences(this);
    }

    int getGeofenceCount() {
        return DatabaseHandlerEvents.getGeofenceCount(this);
    }

    boolean isGeofenceUsed(long geofenceId/*, boolean onlyEnabledEvents*/) {
        return DatabaseHandlerEvents.isGeofenceUsed(this, geofenceId);
    }

    int getGeofenceTransition(long geofenceId) {
        return DatabaseHandlerEvents.getGeofenceTransition(this, geofenceId);
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    void addShortcut(Shortcut shortcut) {
        DatabaseHandlerProfiles.addShortcut(this, shortcut);
    }

    // Getting single shortcut
    Shortcut getShortcut(long shortcutId) {
        return DatabaseHandlerProfiles.getShortcut(this, shortcutId);
    }

    // Deleting single shortcut
    void deleteShortcut(long shortcutId) {
        DatabaseHandlerProfiles.deleteShortcut(this, shortcutId);
    }

// MOBILE_CELLS ----------------------------------------------------------------------

    // add mobile cells to list
    void addMobileCellsToList(List<MobileCellsData> cellsList, int onlyCellId) {
        DatabaseHandlerEvents.addMobileCellsToList(this, cellsList, onlyCellId);
    }

    void saveMobileCellsList(List<MobileCellsData> cellsList, boolean _new, boolean renameExistingCell) {
        DatabaseHandlerEvents.saveMobileCellsList(this, cellsList, _new, renameExistingCell);
    }

    void renameMobileCellsList(List<MobileCellsData> cellsList, String name, boolean _new, String value) {
        DatabaseHandlerEvents.renameMobileCellsList(this, cellsList, name, _new, value);
    }

    void deleteMobileCell(int mobileCell) {
        DatabaseHandlerEvents.deleteMobileCell(this, mobileCell);
    }

    void updateMobileCellLastConnectedTime(int mobileCell, long lastConnectedTime) {
        DatabaseHandlerEvents.updateMobileCellLastConnectedTime(this, mobileCell, lastConnectedTime);
    }

    void addMobileCellNamesToList(List<String> cellNamesList) {
        DatabaseHandlerEvents.addMobileCellNamesToList(this, cellNamesList);
    }

    int getNewMobileCellsCount() {
        return DatabaseHandlerEvents.getNewMobileCellsCount(this);
    }

    // Updating single event
    void updateMobileCellsCells(long eventId, String cells) {
        DatabaseHandlerEvents.updateMobileCellsCells(this, eventId, cells);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isMobileCellSaved(int mobileCell) {
        return DatabaseHandlerEvents.isMobileCellSaved(this, mobileCell);
    }

    void loadMobileCellsSensorRunningPausedEvents(List<NotUsedMobileCells> eventList/*, boolean outsideParameter*/) {
        DatabaseHandlerEvents.loadMobileCellsSensorRunningPausedEvents(this, eventList);
    }

    String getEventMobileCellsCells(long eventId) {
        return DatabaseHandlerEvents.getEventMobileCellsCells(this, eventId);
    }


// NFC_TAGS ----------------------------------------------------------------------

    // Adding new nfc tag
    void addNFCTag(NFCTag tag) {
        DatabaseHandlerEvents.addNFCTag(this, tag);
    }

    // Getting All nfc tags
    List<NFCTag> getAllNFCTags() {
        return DatabaseHandlerEvents.getAllNFCTags(this);
    }

    // Updating single nfc tag
    void updateNFCTag(NFCTag tag) {
        DatabaseHandlerEvents.updateNFCTag(this, tag);
    }

    // Deleting single nfc tag
    void deleteNFCTag(NFCTag tag) {
        DatabaseHandlerEvents.deleteNFCTag(this, tag);
    }

// INTENTS ----------------------------------------------------------------------

    // Adding new intent
    void addIntent(PPIntent intent) {
        DatabaseHandlerProfiles.addIntent(this, intent);
    }

    // Getting All intents
    List<PPIntent> getAllIntents() {
        return DatabaseHandlerProfiles.getAllIntents(this);
    }

    // Updating single intent
    void updateIntent(PPIntent intent) {
        DatabaseHandlerProfiles.updateIntent(this, intent);
    }

    // Getting single intent
    PPIntent getIntent(long intentId) {
        return DatabaseHandlerProfiles.getIntent(this, intentId);
    }

    // Deleting single intent
    void deleteIntent(long intentId) {
        DatabaseHandlerProfiles.deleteIntent(this, intentId);
    }

// ACTIVITY LOG -------------------------------------------------------------------

    // Adding activity log
    void addActivityLog(int deleteOldActivityLogs,
                        int logType, String eventName, String profileName, String profileEventsCount) {
        DatabaseHandlerOthers.addActivityLog(this, deleteOldActivityLogs, logType, eventName, profileName, profileEventsCount);
    }

    void clearActivityLog() {
        DatabaseHandlerOthers.clearActivityLog(this);
    }

    Cursor getActivityLogCursor() {
        return DatabaseHandlerOthers.getActivityLogCursor(this);
    }

// OTHERS -------------------------------------------------------------------------

    void disableNotAllowedPreferences()
    {
        DatabaseHandlerOthers.disableNotAllowedPreferences(this);
    }

// IMPORT/EXPORT -------------------------------------------------------------------------

    int importDB(/*String applicationDataPath*/) {
        return DatabaseHandlerImportExport.importDB(this);
    }

    int exportDB()
    {
        return DatabaseHandlerImportExport.exportDB(this);
    }

}
