package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.SENSOR_SERVICE;

public class DatabaseHandler extends SQLiteOpenHelper {


    // All Static variables

    // singleton fields
    private static volatile DatabaseHandler instance;
    private SQLiteDatabase writableDb;

    private final Context context;
    
    // Database Version
    private static final int DATABASE_VERSION = 2300;

    // Database Name
    private static final String DATABASE_NAME = "phoneProfilesManager";

    // Table names
    private static final String TABLE_PROFILES = "profiles";
    private static final String TABLE_MERGED_PROFILE = "merged_profile";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_EVENT_TIMELINE = "event_timeline";
    private static final String TABLE_ACTIVITY_LOG = "activity_log";
    private static final String TABLE_GEOFENCES = "geofences";
    private static final String TABLE_SHORTCUTS = "shortcuts";
    private static final String TABLE_MOBILE_CELLS = "mobile_cells";
    private static final String TABLE_NFC_TAGS = "nfc_tags";
    private static final String TABLE_INTENTS = "intents";

    // import/export
    private final String EXPORT_DBFILENAME = DATABASE_NAME + ".backup";
    private final Lock importExportLock = new ReentrantLock();
    private final Condition runningImportExportCondition  = importExportLock.newCondition();
    private final Condition runningCommandCondition = importExportLock.newCondition();
    private boolean runningImportExport = false;
    private boolean runningCommand = false;
    private static final int IMPORT_ERROR_BUG = 0;
    static final int IMPORT_ERROR_NEVER_VERSION = -999;
    static final int IMPORT_OK = 1;

    // profile type
    static final int PTYPE_CONNECT_TO_SSID = 1;
    static final int PTYPE_FORCE_STOP = 2;
    static final int PTYPE_LOCK_DEVICE = 3;

    // event type
    static final int ETYPE_TIME = 1;
    static final int ETYPE_BATTERY = 2;
    static final int ETYPE_CALL = 3;
    static final int ETYPE_PERIPHERAL = 4;
    static final int ETYPE_CALENDAR = 5;
    static final int ETYPE_WIFICONNECTED = 6;
    static final int ETYPE_WIFIINFRONT = 7;
    static final int ETYPE_SCREEN = 8;
    static final int ETYPE_BLUETOOTHCONNECTED = 9;
    static final int ETYPE_BLUETOOTHINFRONT = 10;
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

    // activity log types
    static final int ALTYPE_UNDEFINED = 0;
    static final int ALTYPE_PROFILEACTIVATION = 1;
    static final int ALTYPE_AFTERDURATION_UNDOPROFILE = 21;
    static final int ALTYPE_AFTERDURATION_BACKGROUNDPROFILE = 22;
    static final int ALTYPE_AFTERDURATION_RESTARTEVENTS = 23;
    static final int ALTYPE_EVENTSTART = 3;
    static final int ALTYPE_EVENTSTARTDELAY = 4;
    static final int ALTYPE_EVENTEND_NONE = 51;
    static final int ALTYPE_EVENTEND_ACTIVATEPROFILE = 52;
    static final int ALTYPE_EVENTEND_UNDOPROFILE = 53;
    static final int ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE = 54;
    static final int ALTYPE_EVENTEND_RESTARTEVENTS = 55;
    static final int ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS = 56;
    static final int ALTYPE_RESTARTEVENTS = 6;
    static final int ALTYPE_RUNEVENTS_DISABLE = 7;
    static final int ALTYPE_RUNEVENTS_ENABLE = 8;
    static final int ALTYPE_APPLICATIONSTART = 9;
    static final int ALTYPE_APPLICATIONEXIT = 10;
    static final int ALTYPE_DATAIMPORT = 11;
    static final int ALTYPE_PAUSEDLOGGING = 12;
    static final int ALTYPE_STARTEDLOGGING = 13;
    static final int ALTYPE_EVENTENDDELAY = 14;
    static final int ALTYPE_EVENTSTOP = 15;
    static final int ALTYPE_APPLICATIONSTARTONBOOT = 16;
    static final int ALTYPE_EVENTPREFERENCESCHANGED = 17;
    static final int ALTYPE_EVENTDELETED = 18;
    static final int ALTYPE_PROFILEDELETED = 19;
    static final int ALTYPE_PROFILEPREFERENCESCHANGED = 30;
    static final int ALTYPE_SHAREDPROFILEPREFERENCESCHANGED = 31;
    static final int ALTYPE_ALLEVENTSDELETED = 32;
    static final int ALTYPE_ALLPROFILESDELETED = 33;
    static final int ALTYPE_APPLICATIONUPGRADE = 34;


    // Profiles Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ICON = "icon";
    private static final String KEY_CHECKED = "checked";
    private static final String KEY_PORDER = "porder";
    private static final String KEY_VOLUME_RINGER_MODE = "volumeRingerMode";
    private static final String KEY_VOLUME_ZEN_MODE = "volumeZenMode";
    private static final String KEY_VOLUME_RINGTONE = "volumeRingtone";
    private static final String KEY_VOLUME_NOTIFICATION = "volumeNotification";
    private static final String KEY_VOLUME_MEDIA = "volumeMedia";
    private static final String KEY_VOLUME_ALARM = "volumeAlarm";
    private static final String KEY_VOLUME_SYSTEM = "volumeSystem";
    private static final String KEY_VOLUME_VOICE = "volumeVoice";
    private static final String KEY_SOUND_RINGTONE_CHANGE = "soundRingtoneChange";
    private static final String KEY_SOUND_RINGTONE = "soundRingtone";
    private static final String KEY_SOUND_NOTIFICATION_CHANGE = "soundNotificationChange";
    private static final String KEY_SOUND_NOTIFICATION = "soundNotification";
    private static final String KEY_SOUND_ALARM_CHANGE = "soundAlarmChange";
    private static final String KEY_SOUND_ALARM = "soundAlarm";
    private static final String KEY_DEVICE_AIRPLANE_MODE = "deviceAirplaneMode";
    private static final String KEY_DEVICE_WIFI = "deviceWiFi";
    private static final String KEY_DEVICE_BLUETOOTH = "deviceBluetooth";
    private static final String KEY_DEVICE_SCREEN_TIMEOUT = "deviceScreenTimeout";
    private static final String KEY_DEVICE_BRIGHTNESS = "deviceBrightness";
    private static final String KEY_DEVICE_WALLPAPER_CHANGE = "deviceWallpaperChange";
    private static final String KEY_DEVICE_WALLPAPER = "deviceWallpaper";
    private static final String KEY_DEVICE_MOBILE_DATA = "deviceMobileData";
    private static final String KEY_DEVICE_MOBILE_DATA_PREFS = "deviceMobileDataPrefs";
    private static final String KEY_DEVICE_GPS = "deviceGPS";
    private static final String KEY_DEVICE_RUN_APPLICATION_CHANGE = "deviceRunApplicationChange";
    private static final String KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "deviceRunApplicationPackageName";
    private static final String KEY_DEVICE_AUTOSYNC = "deviceAutosync";
    private static final String KEY_SHOW_IN_ACTIVATOR = "showInActivator";
    private static final String KEY_DEVICE_AUTOROTATE = "deviceAutoRotate";
    private static final String KEY_DEVICE_LOCATION_SERVICE_PREFS = "deviceLocationServicePrefs";
    private static final String KEY_VOLUME_SPEAKER_PHONE = "volumeSpeakerPhone";
    private static final String KEY_DEVICE_NFC = "deviceNFC";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_AFTER_DURATION_DO = "afterDurationDo";
    private static final String KEY_ASK_FOR_DURATION = "askForDuration";
    private static final String KEY_DURATION_NOTIFICATION_SOUND = "durationNotificationSound";
    private static final String KEY_DURATION_NOTIFICATION_VIBRATE = "durationNotificationVibrate";
    private static final String KEY_DEVICE_KEYGUARD = "deviceKeyguard";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrateOnTouch";
    private static final String KEY_DEVICE_WIFI_AP = "deviceWifiAP";
    private static final String KEY_DEVICE_POWER_SAVE_MODE = "devicePowerSaveMode";
    private static final String KEY_DEVICE_NETWORK_TYPE = "deviceNetworkType";
    private static final String KEY_NOTIFICATION_LED = "notificationLed";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrateWhenRinging";
    private static final String KEY_DEVICE_WALLPAPER_FOR = "deviceWallpaperFor";
    private static final String KEY_HIDE_STATUS_BAR_ICON = "hideStatusBarIcon";
    private static final String KEY_LOCK_DEVICE = "lockDevice";
    private static final String KEY_DEVICE_CONNECT_TO_SSID = "deviceConnectToSSID";
    private static final String KEY_APPLICATION_DISABLE_WIFI_SCANNING = "applicationDisableWifiScanning";
    private static final String KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING = "applicationDisableBluetoothScanning";
    private static final String KEY_DEVICE_WIFI_AP_PREFS = "deviceWifiAPPrefs";
    private static final String KEY_APPLICATION_DISABLE_LOCATION_SCANNING = "applicationDisableLocationScanning";
    private static final String KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING = "applicationDisableMobileCellScanning";
    private static final String KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING = "applicationDisableOrientationScanning";
    private static final String KEY_HEADS_UP_NOTIFICATIONS = "headsUpNotifications";
    private static final String KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE = "deviceForceStopApplicationChange";
    private static final String KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = "deviceForceStopApplicationPackageName";
    private static final String KEY_ACTIVATION_BY_USER_COUNT = "activationByUserCount";
    private static final String KEY_DEVICE_NETWORK_TYPE_PREFS = "deviceNetworkTypePrefs";
    private static final String KEY_DEVICE_CLOSE_ALL_APPLICATIONS = "deviceCloseAllApplications";
    private static final String KEY_SCREEN_NIGHT_MODE = "screenNightMode";
    private static final String KEY_DTMF_TONE_WHEN_DIALING = "dtmfToneWhenDialing";
    private static final String KEY_SOUND_ON_TOUCH = "soundOnTouch";

    // Events Table Columns names
    private static final String KEY_E_ID = "id";
    private static final String KEY_E_NAME = "name";
    private static final String KEY_E_START_ORDER = "startOrder";
    private static final String KEY_E_FK_PROFILE_START = "fkProfile";
    private static final String KEY_E_STATUS = "status";
    private static final String KEY_E_START_TIME = "startTime";
    private static final String KEY_E_END_TIME = "endTime";
    private static final String KEY_E_DAYS_OF_WEEK = "daysOfWeek";
    private static final String KEY_E_USE_END_TIME = "useEndTime";
    private static final String KEY_E_BATTERY_LEVEL = "batteryLevel";
    private static final String KEY_E_NOTIFICATION_SOUND_START = "notificationSound";
    private static final String KEY_E_BATTERY_LEVEL_LOW = "batteryLevelLow";
    private static final String KEY_E_BATTERY_LEVEL_HIGHT = "batteryLevelHight";
    private static final String KEY_E_BATTERY_CHARGING = "batteryCharging";
    private static final String KEY_E_TIME_ENABLED = "timeEnabled";
    private static final String KEY_E_BATTERY_ENABLED = "batteryEnabled";
    private static final String KEY_E_CALL_ENABLED = "callEnabled";
    private static final String KEY_E_CALL_EVENT = "callEvent";
    private static final String KEY_E_CALL_CONTACTS = "callContacts";
    private static final String KEY_E_CALL_CONTACT_LIST_TYPE = "contactListType";
    private static final String KEY_E_FK_PROFILE_END = "fkProfileEnd";
    private static final String KEY_E_FORCE_RUN = "forceRun";
    private static final String KEY_E_BLOCKED = "blocked";
    private static final String KEY_E_UNDONE_PROFILE = "undoneProfile";
    private static final String KEY_E_PRIORITY = "priority";
    private static final String KEY_E_PERIPHERAL_ENABLED = "peripheralEnabled";
    private static final String KEY_E_PERIPHERAL_TYPE = "peripheralType";
    private static final String KEY_E_CALENDAR_ENABLED = "calendarEnabled";
    private static final String KEY_E_CALENDAR_CALENDARS = "calendarCalendars";
    private static final String KEY_E_CALENDAR_SEARCH_FIELD = "calendarSearchField";
    private static final String KEY_E_CALENDAR_SEARCH_STRING = "calendarSearchString";
    private static final String KEY_E_CALENDAR_EVENT_START_TIME = "calendarEventStartTime";
    private static final String KEY_E_CALENDAR_EVENT_END_TIME = "calendarEventEndTime";
    private static final String KEY_E_CALENDAR_EVENT_FOUND = "calendarEventFound";
    private static final String KEY_E_WIFI_ENABLED = "wifiEnabled";
    private static final String KEY_E_WIFI_SSID = "wifiSSID";
    private static final String KEY_E_WIFI_CONNECTION_TYPE = "wifiConnectionType";
    private static final String KEY_E_SCREEN_ENABLED = "screenEnabled";
    //private static final String KEY_E_SCREEN_DELAY = "screenDelay";
    private static final String KEY_E_SCREEN_EVENT_TYPE = "screenEventType";
    private static final String KEY_E_DELAY_START = "delayStart";
    private static final String KEY_E_IS_IN_DELAY_START = "isInDelay";
    private static final String KEY_E_SCREEN_WHEN_UNLOCKED = "screenWhenUnlocked";
    private static final String KEY_E_BLUETOOTH_ENABLED = "bluetoothEnabled";
    private static final String KEY_E_BLUETOOTH_ADAPTER_NAME = "bluetoothAdapterName";
    private static final String KEY_E_BLUETOOTH_CONNECTION_TYPE = "bluetoothConnectionType";
    private static final String KEY_E_SMS_ENABLED = "smsEnabled";
    //private static final String KEY_E_SMS_EVENT = "smsEvent";
    private static final String KEY_E_SMS_CONTACTS = "smsContacts";
    private static final String KEY_E_SMS_CONTACT_LIST_TYPE = "smsContactListType";
    private static final String KEY_E_SMS_START_TIME = "smsStartTime";
    private static final String KEY_E_CALL_CONTACT_GROUPS = "callContactGroups";
    private static final String KEY_E_SMS_CONTACT_GROUPS = "smsContactGroups";
    private static final String KEY_E_AT_END_DO = "atEndDo";
    private static final String KEY_E_CALENDAR_AVAILABILITY = "calendarAvailability";
    private static final String KEY_E_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    private static final String KEY_E_FK_PROFILE_START_WHEN_ACTIVATED = "fkProfileStartWhenActivated";
    private static final String KEY_E_SMS_DURATION = "smsDuration";
    private static final String KEY_E_NOTIFICATION_ENABLED = "notificationEnabled";
    private static final String KEY_E_NOTIFICATION_APPLICATIONS = "notificationApplications";
    private static final String KEY_E_NOTIFICATION_DURATION = "notificationDuration";
    private static final String KEY_E_NOTIFICATION_START_TIME = "notificationStartTime";
    private static final String KEY_E_BATTERY_POWER_SAVE_MODE = "batteryPowerSaveMode";
    private static final String KEY_E_BLUETOOTH_DEVICES_TYPE = "bluetoothDevicesType";
    private static final String KEY_E_APPLICATION_ENABLED = "applicationEnabled";
    private static final String KEY_E_APPLICATION_APPLICATIONS = "applicationApplications";
    private static final String KEY_E_NOTIFICATION_END_WHEN_REMOVED = "notificationEndWhenRemoved";
    private static final String KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS = "calendarIgnoreAllDayEvents";
    private static final String KEY_E_LOCATION_ENABLED = "locationEnabled";
    private static final String KEY_E_LOCATION_FK_GEOFENCE = "fklocationGeofenceId";
    private static final String KEY_E_LOCATION_WHEN_OUTSIDE = "locationWhenOutside";
    private static final String KEY_E_DELAY_END = "delayEnd";
    private static final String KEY_E_IS_IN_DELAY_END = "isInDelayEnd";
    private static final String KEY_E_START_STATUS_TIME = "startStatusTime";
    private static final String KEY_E_PAUSE_STATUS_TIME = "pauseStatusTime";
    private static final String KEY_E_ORIENTATION_ENABLED = "orientationEnabled";
    private static final String KEY_E_ORIENTATION_SIDES = "orientationSides";
    private static final String KEY_E_ORIENTATION_DISTANCE = "orientationDistance";
    private static final String KEY_E_ORIENTATION_DISPLAY = "orientationDisplay";
    private static final String KEY_E_ORIENTATION_IGNORE_APPLICATIONS = "orientationIgnoreApplications";
    private static final String KEY_E_MOBILE_CELLS_ENABLED = "mobileCellsEnabled";
    private static final String KEY_E_MOBILE_CELLS_WHEN_OUTSIDE = "mobileCellsWhenOutside";
    private static final String KEY_E_MOBILE_CELLS_CELLS = "mobileCellsCells";
    private static final String KEY_E_LOCATION_GEOFENCES = "fklocationGeofences";
    private static final String KEY_E_NFC_ENABLED = "nfcEnabled";
    private static final String KEY_E_NFC_NFC_TAGS = "nfcNfcTags";
    private static final String KEY_E_NFC_START_TIME = "nfcStartTime";
    private static final String KEY_E_NFC_DURATION = "nfcDuration";
    private static final String KEY_E_SMS_PERMANENT_RUN = "smsPermanentRun";
    private static final String KEY_E_NOTIFICATION_PERMANENT_RUN = "notificationPermanentRun";
    private static final String KEY_E_NFC_PERMANENT_RUN = "nfcPermanentRun";
    private static final String KEY_E_CALENDAR_START_BEFORE_EVENT = "calendarStartBeforeEvent";
    private static final String KEY_E_RADIO_SWITCH_ENABLED = "radioSwitchEnabled";
    private static final String KEY_E_RADIO_SWITCH_WIFI = "radioSwitchWifi";
    private static final String KEY_E_RADIO_SWITCH_BLUETOOTH = "radioSwitchBluetooth";
    private static final String KEY_E_RADIO_SWITCH_MOBILE_DATA = "radioSwitchMobileData";
    private static final String KEY_E_RADIO_SWITCH_GPS = "radioSwitchGPS";
    private static final String KEY_E_RADIO_SWITCH_NFC = "radioSwitchNFC";
    private static final String KEY_E_RADIO_SWITCH_AIRPLANE_MODE = "radioSwitchAirplaneMode";
    private static final String KEY_E_NOTIFICATION_VIBRATE_START = "notificationVibrate";
    private static final String KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION = "eventNoPauseByManualActivation";
    private static final String KEY_E_CALL_DURATION = "callDuration";
    private static final String KEY_E_CALL_PERMANENT_RUN = "callPermanentRun";
    private static final String KEY_E_CALL_START_TIME = "callStartTime";
    private static final String KEY_E_NOTIFICATION_SOUND_REPEAT_START = "notificationSoundRepeat";
    private static final String KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START = "notificationSoundRepeatInterval";
    private static final String KEY_E_NOTIFICATION_IN_CALL = "notificationRingingCall";
    private static final String KEY_E_NOTIFICATION_MISSED_CALL = "notificationMissedCall";
    private static final String KEY_E_START_WHEN_ACTIVATED_PROFILE = "startWhenActivatedProfile";
    private static final String KEY_E_BLUETOOTH_SENSOR_PASSED = "bluetoothSensorPassed";
    private static final String KEY_E_LOCATION_SENSOR_PASSED = "locationSensorPassed";
    private static final String KEY_E_MOBILE_CELLS_SENSOR_PASSED = "mobileCellsSensorPassed";
    private static final String KEY_E_ORIENTATION_SENSOR_PASSED = "orientationSensorPassed";
    private static final String KEY_E_WIFI_SENSOR_PASSED = "wifiSensorPassed";
    private static final String KEY_E_APPLICATION_SENSOR_PASSED = "applicationSensorPassed";
    private static final String KEY_E_BATTERY_SENSOR_PASSED = "batterySensorPassed";
    private static final String KEY_E_CALENDAR_SENSOR_PASSED = "calendarSensorPassed";
    private static final String KEY_E_CALL_SENSOR_PASSED = "callSensorPassed";
    private static final String KEY_E_NFC_SENSOR_PASSED = "nfcSensorPassed";
    private static final String KEY_E_NOTIFICATION_SENSOR_PASSED = "notificationSensorPassed";
    private static final String KEY_E_PERIPHERAL_SENSOR_PASSED = "peripheralSensorPassed";
    private static final String KEY_E_RADIO_SWITCH_SENSOR_PASSED = "radioSwitchSensorPassed";
    private static final String KEY_E_SCREEN_SENSOR_PASSED = "screenSensorPassed";
    private static final String KEY_E_SMS_SENSOR_PASSED = "smsSensorPassed";
    private static final String KEY_E_TIME_SENSOR_PASSED = "timeSensorPassed";
    private static final String KEY_E_CALENDAR_ALL_EVENTS = "calendarAllEvents";
    private static final String KEY_E_ALARM_CLOCK_ENABLED = "alarmClockEnabled";
    private static final String KEY_E_ALARM_CLOCK_PERMANENT_RUN = "alarmClockPermanentRun";
    private static final String KEY_E_ALARM_CLOCK_DURATION = "alarmClockDuration";
    private static final String KEY_E_ALARM_CLOCK_START_TIME = "alarmClockStartTime";
    private static final String KEY_E_ALARM_CLOCK_SENSOR_PASSED = "alarmClockSensorPassed";
    private static final String KEY_E_NOTIFICATION_SOUND_END = "notificationSoundEnd";
    private static final String KEY_E_NOTIFICATION_VIBRATE_END = "notificationVibrateEnd";
    private static final String KEY_E_BATTERY_PLUGGED = "batteryPlugged";

    // EventTimeLine Table Columns names
    private static final String KEY_ET_ID = "id";
    private static final String KEY_ET_EORDER = "eorder";
    private static final String KEY_ET_FK_EVENT = "fkEvent";
    private static final String KEY_ET_FK_PROFILE_RETURN = "fkProfileReturn";

    // ActivityLog Columns names
    private static final String KEY_AL_ID = "_id";  // for CursorAdapter must by this name
    static final String KEY_AL_LOG_TYPE = "logType";
    static final String KEY_AL_LOG_DATE_TIME = "logDateTime";
    static final String KEY_AL_EVENT_NAME = "eventName";
    static final String KEY_AL_PROFILE_NAME = "profileName";
    private static final String KEY_AL_PROFILE_ICON = "profileIcon";
    private static final String KEY_AL_DURATION_DELAY = "durationDelay";

    // Geofences Columns names
    static final String KEY_G_ID = "_id";
    private static final String KEY_G_LATITUDE = "latitude";
    private static final String KEY_G_LONGITUDE = "longitude";
    private static final String KEY_G_RADIUS = "radius";
    static final String KEY_G_NAME = "name";
    static final String KEY_G_CHECKED = "checked";
    private static final String KEY_G_TRANSITION = "transition";

    // Shortcuts Columns names
    private static final String KEY_S_ID = "_id";
    private static final String KEY_S_INTENT = "intent";
    private static final String KEY_S_NAME = "name";

    // Mobile cells Columns names
    private static final String KEY_MC_ID = "_id";
    private static final String KEY_MC_CELL_ID = "cellId";
    private static final String KEY_MC_NAME = "name";
    private static final String KEY_MC_NEW = "new";
    private static final String KEY_MC_LAST_CONNECTED_TIME = "lastConnectedTime";

    // NFC tags Columns names
    private static final String KEY_NT_ID = "_id";
    private static final String KEY_NT_NAME = "name";
    private static final String KEY_NT_UID = "uid";

    // Intents Columns names
    private static final String KEY_IN_ID = "_id";
    private static final String KEY_IN_NAME = "_name";
    private static final String KEY_IN_PACKAGE_NAME = "packageName";
    private static final String KEY_IN_CLASS_NAME = "className";
    private static final String KEY_IN_ACTION = "_action";
    private static final String KEY_IN_DATA = "data";
    private static final String KEY_IN_MIME_TYPE = "mimeType";
    private static final String KEY_IN_EXTRA_KEY_1 = "extraKey1";
    private static final String KEY_IN_EXTRA_VALUE_1 = "extraValue1";
    private static final String KEY_IN_EXTRA_TYPE_1 = "extraType1";
    private static final String KEY_IN_EXTRA_KEY_2 = "extraKey2";
    private static final String KEY_IN_EXTRA_VALUE_2 = "extraValue2";
    private static final String KEY_IN_EXTRA_TYPE_2 = "extraType2";
    private static final String KEY_IN_EXTRA_KEY_3 = "extraKey3";
    private static final String KEY_IN_EXTRA_VALUE_3 = "extraValue3";
    private static final String KEY_IN_EXTRA_TYPE_3 = "extraType3";
    private static final String KEY_IN_EXTRA_KEY_4 = "extraKey4";
    private static final String KEY_IN_EXTRA_VALUE_4 = "extraValue4";
    private static final String KEY_IN_EXTRA_TYPE_4 = "extraType4";
    private static final String KEY_IN_EXTRA_KEY_5 = "extraKey5";
    private static final String KEY_IN_EXTRA_VALUE_5 = "extraValue5";
    private static final String KEY_IN_EXTRA_TYPE_5 = "extraType5";
    private static final String KEY_IN_EXTRA_KEY_6 = "extraKey6";
    private static final String KEY_IN_EXTRA_VALUE_6 = "extraValue6";
    private static final String KEY_IN_EXTRA_TYPE_6 = "extraType6";
    private static final String KEY_IN_EXTRA_KEY_7 = "extraKey7";
    private static final String KEY_IN_EXTRA_VALUE_7 = "extraValue7";
    private static final String KEY_IN_EXTRA_TYPE_7 = "extraType7";
    private static final String KEY_IN_EXTRA_KEY_8 = "extraKey8";
    private static final String KEY_IN_EXTRA_VALUE_8 = "extraValue8";
    private static final String KEY_IN_EXTRA_TYPE_8 = "extraType8";
    private static final String KEY_IN_EXTRA_KEY_9 = "extraKey9";
    private static final String KEY_IN_EXTRA_VALUE_9 = "extraValue9";
    private static final String KEY_IN_EXTRA_TYPE_9 = "extraType9";
    private static final String KEY_IN_EXTRA_KEY_10 = "extraKey10";
    private static final String KEY_IN_EXTRA_VALUE_10 = "extraValue10";
    private static final String KEY_IN_EXTRA_TYPE_10 = "extraType10";
    private static final String KEY_IN_CATEGORIES = "categories";
    private static final String KEY_IN_FLAGS = "flags";
    private static final String KEY_IN_USED_COUNT = "usedCount";
    private static final String KEY_IN_INTENT_TYPE = "intentType";


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
    
    private SQLiteDatabase getMyWritableDatabase() {
        if ((writableDb == null) || (!writableDb.isOpen())) {
            writableDb = this.getWritableDatabase();
        }
        return writableDb;
    }
 
    @Override
    public synchronized void close() {
        super.close();
        if (writableDb != null) {
            writableDb.close();
            writableDb = null;
        }
    }

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

    private String profileTableCreationString(String tableName) {
        String idField = KEY_ID + " INTEGER PRIMARY KEY,";
        if (tableName.equals(TABLE_MERGED_PROFILE))
            idField = KEY_ID + " INTEGER,";
        return "CREATE TABLE " + tableName + "("
                + idField
                + KEY_NAME + " TEXT,"
                + KEY_ICON + " TEXT,"
                + KEY_CHECKED + " INTEGER,"
                + KEY_PORDER + " INTEGER,"
                + KEY_VOLUME_RINGER_MODE + " INTEGER,"
                + KEY_VOLUME_RINGTONE + " TEXT,"
                + KEY_VOLUME_NOTIFICATION + " TEXT,"
                + KEY_VOLUME_MEDIA + " TEXT,"
                + KEY_VOLUME_ALARM + " TEXT,"
                + KEY_VOLUME_SYSTEM + " TEXT,"
                + KEY_VOLUME_VOICE + " TEXT,"
                + KEY_SOUND_RINGTONE_CHANGE + " INTEGER,"
                + KEY_SOUND_RINGTONE + " TEXT,"
                + KEY_SOUND_NOTIFICATION_CHANGE + " INTEGER,"
                + KEY_SOUND_NOTIFICATION + " TEXT,"
                + KEY_SOUND_ALARM_CHANGE + " INTEGER,"
                + KEY_SOUND_ALARM + " TEXT,"
                + KEY_DEVICE_AIRPLANE_MODE + " INTEGER,"
                + KEY_DEVICE_WIFI + " INTEGER,"
                + KEY_DEVICE_BLUETOOTH + " INTEGER,"
                + KEY_DEVICE_SCREEN_TIMEOUT + " INTEGER,"
                + KEY_DEVICE_BRIGHTNESS + " TEXT,"
                + KEY_DEVICE_WALLPAPER_CHANGE + " INTEGER,"
                + KEY_DEVICE_WALLPAPER + " TEXT,"
                + KEY_DEVICE_MOBILE_DATA + " INTEGER,"
                + KEY_DEVICE_MOBILE_DATA_PREFS + " INTEGER,"
                + KEY_DEVICE_GPS + " INTEGER,"
                + KEY_DEVICE_RUN_APPLICATION_CHANGE + " INTEGER,"
                + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " TEXT,"
                + KEY_DEVICE_AUTOSYNC + " INTEGER,"
                + KEY_SHOW_IN_ACTIVATOR + " INTEGER,"
                + KEY_DEVICE_AUTOROTATE + " INTEGER,"
                + KEY_DEVICE_LOCATION_SERVICE_PREFS + " INTEGER,"
                + KEY_VOLUME_SPEAKER_PHONE + " INTEGER,"
                + KEY_DEVICE_NFC + " INTEGER,"
                + KEY_DURATION + " INTEGER,"
                + KEY_AFTER_DURATION_DO + " INTEGER,"
                + KEY_VOLUME_ZEN_MODE + " INTEGER,"
                + KEY_DEVICE_KEYGUARD + " INTEGER,"
                + KEY_VIBRATE_ON_TOUCH + " INTEGER,"
                + KEY_DEVICE_WIFI_AP + " INTEGER,"
                + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER,"
                + KEY_ASK_FOR_DURATION + " INTEGER,"
                + KEY_DEVICE_NETWORK_TYPE + " INTEGER,"
                + KEY_NOTIFICATION_LED + " INTEGER,"
                + KEY_VIBRATE_WHEN_RINGING + " INTEGER,"
                + KEY_DEVICE_WALLPAPER_FOR + " INTEGER,"
                + KEY_HIDE_STATUS_BAR_ICON + " INTEGER,"
                + KEY_LOCK_DEVICE + " INTEGER,"
                + KEY_DEVICE_CONNECT_TO_SSID + " TEXT,"
                + KEY_APPLICATION_DISABLE_WIFI_SCANNING + " INTEGER,"
                + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + " INTEGER,"
                + KEY_DURATION_NOTIFICATION_SOUND + " TEXT,"
                + KEY_DURATION_NOTIFICATION_VIBRATE + " INTEGER,"
                + KEY_DEVICE_WIFI_AP_PREFS + " INTEGER,"
                + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + " INTEGER,"
                + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + " INTEGER,"
                + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + " INTEGER,"
                + KEY_HEADS_UP_NOTIFICATIONS + " INTEGER,"
                + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + " INTEGER,"
                + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + " TEXT,"
                + KEY_ACTIVATION_BY_USER_COUNT + " INTEGER,"
                + KEY_DEVICE_NETWORK_TYPE_PREFS + " INTEGER,"
                + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + " INTEGER,"
                + KEY_SCREEN_NIGHT_MODE + " INTEGER,"
                + KEY_DTMF_TONE_WHEN_DIALING + " INTEGER,"
                + KEY_SOUND_ON_TOUCH + " INTEGER"
                + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_PROFILES_TABLE = profileTableCreationString(TABLE_PROFILES);
        db.execSQL(CREATE_PROFILES_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SHOW_IN_ACTIVATOR ON " + TABLE_PROFILES + " (" + KEY_SHOW_IN_ACTIVATOR + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_P_NAME ON " + TABLE_PROFILES + " (" + KEY_NAME + ")");

        final String CREATE_MERGED_PROFILE_TABLE = profileTableCreationString(TABLE_MERGED_PROFILE);
        db.execSQL(CREATE_MERGED_PROFILE_TABLE);

        final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + KEY_E_ID + " INTEGER PRIMARY KEY,"
                + KEY_E_NAME + " TEXT,"
                + KEY_E_FK_PROFILE_START + " INTEGER,"
                + KEY_E_START_TIME + " INTEGER,"
                + KEY_E_END_TIME + " INTEGER,"
                + KEY_E_DAYS_OF_WEEK + " TEXT,"
                + KEY_E_USE_END_TIME + " INTEGER,"
                + KEY_E_STATUS + " INTEGER,"
                + KEY_E_NOTIFICATION_SOUND_START + " TEXT,"
                + KEY_E_BATTERY_LEVEL_LOW + " INTEGER,"
                + KEY_E_BATTERY_LEVEL_HIGHT + " INTEGER,"
                + KEY_E_BATTERY_CHARGING + " INTEGER,"
                + KEY_E_TIME_ENABLED + " INTEGER,"
                + KEY_E_BATTERY_ENABLED + " INTEGER,"
                + KEY_E_CALL_ENABLED + " INTEGER,"
                + KEY_E_CALL_EVENT + " INTEGER,"
                + KEY_E_CALL_CONTACTS + " TEXT,"
                + KEY_E_CALL_CONTACT_LIST_TYPE + " INTEGER,"
                + KEY_E_FK_PROFILE_END + " INTEGER,"
                + KEY_E_FORCE_RUN + " INTEGER,"
                + KEY_E_BLOCKED + " INTEGER,"
                //+ KEY_E_UNDONE_PROFILE + " INTEGER,"
                + KEY_E_PRIORITY + " INTEGER,"
                + KEY_E_PERIPHERAL_ENABLED + " INTEGER,"
                + KEY_E_PERIPHERAL_TYPE + " INTEGER,"
                + KEY_E_CALENDAR_ENABLED + " INTEGER,"
                + KEY_E_CALENDAR_CALENDARS + " TEXT,"
                + KEY_E_CALENDAR_SEARCH_FIELD + " INTEGER,"
                + KEY_E_CALENDAR_SEARCH_STRING + " TEXT,"
                + KEY_E_CALENDAR_EVENT_START_TIME + " INTEGER,"
                + KEY_E_CALENDAR_EVENT_END_TIME + " INTEGER,"
                + KEY_E_CALENDAR_EVENT_FOUND + " INTEGER,"
                + KEY_E_WIFI_ENABLED + " INTEGER,"
                + KEY_E_WIFI_SSID + " TEXT,"
                + KEY_E_WIFI_CONNECTION_TYPE + " INTEGER,"
                + KEY_E_SCREEN_ENABLED + " INTEGER,"
                + KEY_E_SCREEN_EVENT_TYPE + " INTEGER,"
                + KEY_E_DELAY_START + " INTEGER,"
                + KEY_E_IS_IN_DELAY_START + " INTEGER,"
                + KEY_E_SCREEN_WHEN_UNLOCKED + " INTEGER,"
                + KEY_E_BLUETOOTH_ENABLED + " INTEGER,"
                + KEY_E_BLUETOOTH_ADAPTER_NAME + " TEXT,"
                + KEY_E_BLUETOOTH_CONNECTION_TYPE + " INTEGER,"
                + KEY_E_SMS_ENABLED + " INTEGER,"
                //+ KEY_E_SMS_EVENT + " INTEGER,"
                + KEY_E_SMS_CONTACTS + " TEXT,"
                + KEY_E_SMS_CONTACT_LIST_TYPE + " INTEGER,"
                + KEY_E_SMS_START_TIME + " INTEGER,"
                + KEY_E_CALL_CONTACT_GROUPS + " TEXT,"
                + KEY_E_SMS_CONTACT_GROUPS + " TEXT,"
                + KEY_E_AT_END_DO + " INTEGER,"
                + KEY_E_CALENDAR_AVAILABILITY + " INTEGER,"
                + KEY_E_MANUAL_PROFILE_ACTIVATION + " INTEGER,"
                + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " INTEGER,"
                + KEY_E_SMS_DURATION + " INTEGER,"
                + KEY_E_NOTIFICATION_ENABLED + " INTEGER,"
                + KEY_E_NOTIFICATION_APPLICATIONS + " TEXT,"
                + KEY_E_NOTIFICATION_START_TIME + " INTEGER,"
                + KEY_E_NOTIFICATION_DURATION + " INTEGER,"
                + KEY_E_BATTERY_POWER_SAVE_MODE + " INTEGER,"
                + KEY_E_BLUETOOTH_DEVICES_TYPE + " INTEGER,"
                + KEY_E_APPLICATION_ENABLED + " INTEGER,"
                + KEY_E_APPLICATION_APPLICATIONS + " TEXT,"
                + KEY_E_NOTIFICATION_END_WHEN_REMOVED + " INTEGER,"
                + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + " INTEGER,"
                + KEY_E_LOCATION_ENABLED + " INTEGER,"
                + KEY_E_LOCATION_FK_GEOFENCE + " INTEGER,"
                + KEY_E_LOCATION_WHEN_OUTSIDE + " INTEGER,"
                + KEY_E_DELAY_END + " INTEGER,"
                + KEY_E_IS_IN_DELAY_END + " INTEGER,"
                + KEY_E_START_STATUS_TIME + " INTEGER,"
                + KEY_E_PAUSE_STATUS_TIME + " INTEGER,"
                + KEY_E_ORIENTATION_ENABLED + " INTEGER,"
                + KEY_E_ORIENTATION_SIDES + " TEXT,"
                + KEY_E_ORIENTATION_DISTANCE + " INTEGER,"
                + KEY_E_ORIENTATION_DISPLAY + " TEXT,"
                + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + " TEXT,"
                + KEY_E_MOBILE_CELLS_ENABLED + " INTEGER,"
                + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + " INTEGER,"
                + KEY_E_MOBILE_CELLS_CELLS + " TEXT,"
                + KEY_E_LOCATION_GEOFENCES + " TEXT,"
                + KEY_E_START_ORDER + " INTEGER,"
                + KEY_E_NFC_ENABLED + " INTEGER,"
                + KEY_E_NFC_NFC_TAGS + " TEXT,"
                + KEY_E_NFC_DURATION + " INTEGER,"
                + KEY_E_NFC_START_TIME + " INTEGER,"
                + KEY_E_SMS_PERMANENT_RUN + " INTEGER,"
                + KEY_E_NOTIFICATION_PERMANENT_RUN + " INTEGER,"
                + KEY_E_NFC_PERMANENT_RUN + " INTEGER,"
                + KEY_E_CALENDAR_START_BEFORE_EVENT + " INTEGER,"
                + KEY_E_RADIO_SWITCH_ENABLED + " INTEGER,"
                + KEY_E_RADIO_SWITCH_WIFI + " INTEGER,"
                + KEY_E_RADIO_SWITCH_BLUETOOTH + " INTEGER,"
                + KEY_E_RADIO_SWITCH_MOBILE_DATA + " INTEGER,"
                + KEY_E_RADIO_SWITCH_GPS + " INTEGER,"
                + KEY_E_RADIO_SWITCH_NFC + " INTEGER,"
                + KEY_E_RADIO_SWITCH_AIRPLANE_MODE + " INTEGER,"
                + KEY_E_NOTIFICATION_VIBRATE_START + " INTEGER,"
                + KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + " INTEGER,"
                + KEY_E_CALL_DURATION + " INTEGER,"
                + KEY_E_CALL_PERMANENT_RUN + " INTEGER,"
                + KEY_E_CALL_START_TIME + " INTEGER,"
                + KEY_E_NOTIFICATION_SOUND_REPEAT_START + " INTEGER,"
                + KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + " INTEGER,"
                + KEY_E_NOTIFICATION_IN_CALL + " INTEGER,"
                + KEY_E_NOTIFICATION_MISSED_CALL + " INTEGER,"
                + KEY_E_START_WHEN_ACTIVATED_PROFILE + " TEXT,"
                + KEY_E_BLUETOOTH_SENSOR_PASSED + " INTEGER,"
                + KEY_E_LOCATION_SENSOR_PASSED + " INTEGER,"
                + KEY_E_MOBILE_CELLS_SENSOR_PASSED + " INTEGER,"
                + KEY_E_ORIENTATION_SENSOR_PASSED + " INTEGER,"
                + KEY_E_WIFI_SENSOR_PASSED + " INTEGER,"
                + KEY_E_APPLICATION_SENSOR_PASSED + " INTEGER,"
                + KEY_E_BATTERY_SENSOR_PASSED + " INTEGER,"
                + KEY_E_CALENDAR_SENSOR_PASSED + " INTEGER,"
                + KEY_E_CALL_SENSOR_PASSED + " INTEGER,"
                + KEY_E_NFC_SENSOR_PASSED + " INTEGER,"
                + KEY_E_NOTIFICATION_SENSOR_PASSED + " INTEGER,"
                + KEY_E_PERIPHERAL_SENSOR_PASSED + " INTEGER,"
                + KEY_E_RADIO_SWITCH_SENSOR_PASSED + " INTEGER,"
                + KEY_E_SCREEN_SENSOR_PASSED + " INTEGER,"
                + KEY_E_SMS_SENSOR_PASSED + " INTEGER,"
                + KEY_E_TIME_SENSOR_PASSED + " INTEGER,"
                + KEY_E_CALENDAR_ALL_EVENTS + " INTEGER,"
                + KEY_E_ALARM_CLOCK_ENABLED + " INTEGER,"
                + KEY_E_ALARM_CLOCK_PERMANENT_RUN + " INTEGER,"
                + KEY_E_ALARM_CLOCK_DURATION + " INTEGER,"
                + KEY_E_ALARM_CLOCK_START_TIME + " INTEGER,"
                + KEY_E_ALARM_CLOCK_SENSOR_PASSED + " INTEGER,"
                + KEY_E_NOTIFICATION_SOUND_END + " TEXT,"
                + KEY_E_NOTIFICATION_VIBRATE_END + " INTEGER,"
                + KEY_E_BATTERY_PLUGGED + " TEXT"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_E_NAME ON " + TABLE_EVENTS + " (" + KEY_E_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_END ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_END + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRIORITY ON " + TABLE_EVENTS + " (" + KEY_E_PRIORITY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_START_ORDER ON " + TABLE_EVENTS + " (" + KEY_E_START_ORDER + ")");

        final String CREATE_EVENTTIME_TABLE = "CREATE TABLE " + TABLE_EVENT_TIMELINE + "("
                + KEY_ET_ID + " INTEGER PRIMARY KEY,"
                + KEY_ET_EORDER + " INTEGER,"
                + KEY_ET_FK_EVENT + " INTEGER,"
                + KEY_ET_FK_PROFILE_RETURN + " INTEGER"
                + ")";
        db.execSQL(CREATE_EVENTTIME_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ET_PORDER ON " + TABLE_EVENT_TIMELINE + " (" + KEY_ET_EORDER + ")");

        final String CREATE_ACTIVITYLOG_TABLE = "CREATE TABLE " + TABLE_ACTIVITY_LOG + "("
                + KEY_AL_ID + " INTEGER PRIMARY KEY,"
                + KEY_AL_LOG_DATE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_AL_LOG_TYPE + " INTEGER,"
                + KEY_AL_EVENT_NAME + " TEXT,"
                + KEY_AL_PROFILE_NAME + " TEXT,"
                + KEY_AL_PROFILE_ICON + " TEXT,"
                + KEY_AL_DURATION_DELAY + " INTEGER"
                + ")";
        db.execSQL(CREATE_ACTIVITYLOG_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_AL_LOG_DATE_TIME ON " + TABLE_ACTIVITY_LOG + " (" + KEY_AL_LOG_DATE_TIME + ")");

        final String CREATE_GEOFENCES_TABLE = "CREATE TABLE " + TABLE_GEOFENCES + "("
                + KEY_G_ID + " INTEGER PRIMARY KEY,"
                + KEY_G_LATITUDE + " DOUBLE,"
                + KEY_G_LONGITUDE + " DOUBLE,"
                + KEY_G_RADIUS + " FLOAT,"
                + KEY_G_NAME + " TEXT,"
                + KEY_G_CHECKED + " INTEGER,"
                + KEY_G_TRANSITION + " INTEGER"
                + ")";
        db.execSQL(CREATE_GEOFENCES_TABLE);

        final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE " + TABLE_SHORTCUTS + "("
                + KEY_S_ID + " INTEGER PRIMARY KEY,"
                + KEY_S_INTENT + " TEXT,"
                + KEY_S_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_SHORTCUTS_TABLE);

        final String CREATE_MOBILE_CELLS_TABLE = "CREATE TABLE " + TABLE_MOBILE_CELLS + "("
                + KEY_MC_ID + " INTEGER PRIMARY KEY,"
                + KEY_MC_CELL_ID + " INTEGER,"
                + KEY_MC_NAME + " TEXT,"
                + KEY_MC_NEW + " INTEGER,"
                + KEY_MC_LAST_CONNECTED_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_MOBILE_CELLS_TABLE);

        final String CREATE_NFC_TAGS_TABLE = "CREATE TABLE " + TABLE_NFC_TAGS + "("
                + KEY_NT_ID + " INTEGER PRIMARY KEY,"
                + KEY_NT_NAME + " TEXT,"
                + KEY_NT_UID + " TEXT"
                + ")";
        db.execSQL(CREATE_NFC_TAGS_TABLE);

        final String CREATE_INTENTS_TABLE = "CREATE TABLE " + TABLE_INTENTS + "("
                + KEY_IN_ID + " INTEGER PRIMARY KEY,"
                + KEY_IN_PACKAGE_NAME + " TEXT,"
                + KEY_IN_CLASS_NAME + " TEXT,"
                + KEY_IN_ACTION + " TEXT,"
                + KEY_IN_DATA + " TEXT,"
                + KEY_IN_MIME_TYPE + " TEXT,"
                + KEY_IN_EXTRA_KEY_1 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_1 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_1 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_2 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_2 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_2 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_3 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_3 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_3 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_4 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_4 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_4 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_5 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_5 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_5 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_6 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_6 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_6 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_7 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_7 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_7 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_8 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_8 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_8 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_9 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_9 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_9 + " INTEGER,"
                + KEY_IN_EXTRA_KEY_10 + " TEXT,"
                + KEY_IN_EXTRA_VALUE_10 + " TEXT,"
                + KEY_IN_EXTRA_TYPE_10 + " INTEGER,"
                + KEY_IN_CATEGORIES + " TEXT,"
                + KEY_IN_FLAGS + " TEXT,"
                + KEY_IN_NAME + " TEXT,"
                + KEY_IN_USED_COUNT + " INTEGER,"
                + KEY_IN_INTENT_TYPE + " INTEGER"
                + ")";
        db.execSQL(CREATE_INTENTS_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_AUTOROTATE ON " + TABLE_PROFILES + " (" + KEY_DEVICE_AUTOROTATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_CONNECT_TO_SSID ON " + TABLE_PROFILES + " (" + KEY_DEVICE_CONNECT_TO_SSID + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_LOCATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__TIME_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_TIME_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BATTERY_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BATTERY_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALL_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIPHERAL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_PERIPHERAL_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALENDAR_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALENDAR_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__WIFI_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_WIFI_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SCREEN_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SCREEN_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BLUETOOTH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BLUETOOTH_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SMS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SMS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NOTIFICATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_NOTIFICATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__APPLICATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_APPLICATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_LOCATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ORIENTATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ORIENTATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_MOBILE_CELLS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NFC_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_NFC_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__RADIO_SWITCH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_RADIO_SWITCH_ENABLED + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_GEOFENCES + " (" + KEY_G_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_MOBILE_CELLS + " (" + KEY_MC_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_NFC_TAGS + " (" + KEY_NT_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ACTIVATION_BY_USER_COUNT ON " + TABLE_PROFILES + " (" + KEY_ACTIVATION_BY_USER_COUNT + ")");

    }

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        PPApplication.logE("DatabaseHandler.onDowngrade", "oldVersion="+oldVersion);
        PPApplication.logE("DatabaseHandler.onDowngrade", "newVersion="+newVersion);

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

        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        PPApplication.logE("DatabaseHandler.onUpgrade", "oldVersion="+oldVersion);
        PPApplication.logE("DatabaseHandler.onUpgrade", "newVersion="+newVersion);

        /*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);

        // Create tables again
        onCreate(db);
        */

        if (oldVersion < 16)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER + " TEXT");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "='-'");
        }

        if (oldVersion < 18)
        {
            String value = "=replace(" + KEY_ICON + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ICON + value);
            value = "=replace(" + KEY_VOLUME_RINGTONE + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_RINGTONE + value);
            value = "=replace(" + KEY_VOLUME_NOTIFICATION + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_NOTIFICATION + value);
            value = "=replace(" + KEY_VOLUME_MEDIA + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_MEDIA + value);
            value = "=replace(" + KEY_VOLUME_ALARM + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ALARM + value);
            value = "=replace(" + KEY_VOLUME_SYSTEM + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SYSTEM + value);
            value = "=replace(" + KEY_VOLUME_VOICE + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_VOICE + value);
            value = "=replace(" + KEY_DEVICE_BRIGHTNESS + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_BRIGHTNESS + value);
            value = "=replace(" + KEY_DEVICE_WALLPAPER + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + value);

        }

        if (oldVersion < 19)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA + "=0");
        }

        if (oldVersion < 20)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA_PREFS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_PREFS + "=0");
        }

        if (oldVersion < 21)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_GPS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_GPS + "=0");
        }

        if (oldVersion < 22)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " TEXT");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 23)
        {
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        }

        if (oldVersion < 24)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOSYNC + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOSYNC + "=0");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_P_NAME ON " + TABLE_PROFILES + " (" + KEY_NAME + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_E_NAME ON " + TABLE_EVENTS + " (" + KEY_E_NAME + ")");
        }

        if (oldVersion < 26)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SHOW_IN_ACTIVATOR + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SHOW_IN_ACTIVATOR + "=1");
        }

        if (oldVersion < 28)
        {
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SHOW_IN_ACTIVATOR ON " + TABLE_PROFILES + " (" + KEY_SHOW_IN_ACTIVATOR + ")");
        }

        if (oldVersion < 29)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_END_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DAYS_OF_WEEK + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DAYS_OF_WEEK + "=\"#ALL#\"");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START + ")");
        }

        if (oldVersion < 30)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_USE_END_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_USE_END_TIME + "=0");
        }

        if (oldVersion < 32)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_STATUS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_STATUS + "=0");
        }

        if (oldVersion < 34)
        {
            final String CREATE_EVENTTIME_TABLE = "CREATE TABLE " + TABLE_EVENT_TIMELINE + "("
                    + KEY_ET_ID + " INTEGER PRIMARY KEY,"
                    + KEY_ET_EORDER + " INTEGER,"
                    + KEY_ET_FK_EVENT + " INTEGER,"
                    + KEY_ET_FK_PROFILE_RETURN + " INTEGER"
                    + ")";
            db.execSQL(CREATE_EVENTTIME_TABLE);

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ET_PORDER ON " + TABLE_EVENT_TIMELINE + " (" + KEY_ET_EORDER + ")");
        }

        if (oldVersion < 1001)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOROTATE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=0");
        }

        if (oldVersion < 1002)
        {
            // autorotate off -> rotation 0
            // autorotate on -> autorotate
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=1");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=3");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=2 WHERE " + KEY_DEVICE_AUTOROTATE + "=2");
        }

        if (oldVersion < 1012)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL + "=15");
        }

        if (oldVersion < 1015)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_LOCATION_SERVICE_PREFS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LOCATION_SERVICE_PREFS + "=0");
        }

        if (oldVersion < 1020)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_SPEAKER_PHONE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SPEAKER_PHONE + "=0");
        }

        if (oldVersion < 1022)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SOUND_START + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_START + "=\"\"");
        }

        if (oldVersion < 1023)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL_LOW + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL_HIGHT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_CHARGING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_LOW + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=100");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_CHARGING + "=0");
        }

        if (oldVersion < 1030)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_TIME_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_ENABLED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_ENABLED + "=0");
        }

        if (oldVersion < 1035)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NFC + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NFC + "=0");
        }

        if (oldVersion < 1040)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_EVENT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACTS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACT_LIST_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1045)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FK_PROFILE_END + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_END + "=" + Profile.PROFILE_NO_ACTIVATE);

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_END ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_END + ")");
        }

        if (oldVersion < 1050)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FORCE_RUN + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FORCE_RUN + "=0");
        }

        if (oldVersion < 1051)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLOCKED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLOCKED + "=0");
        }

        if (oldVersion < 1060)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_UNDONE_PROFILE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_UNDONE_PROFILE + "=1");
        }

        if (oldVersion < 1070)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PRIORITY + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=0");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRIORITY ON " + TABLE_EVENTS + " (" + KEY_E_PRIORITY + ")");
        }

        if (oldVersion < 1080)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PERIPHERAL_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PERIPHERAL_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIPHERAL_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIPHERAL_TYPE + "=0");
        }

        if (oldVersion < 1081)
        {
            // conversion to GMT
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=" + KEY_E_START_TIME + "+" + gmtOffset);
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=" + KEY_E_END_TIME + "+" + gmtOffset);
        }

        if (oldVersion < 1090)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_CALENDARS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_SEARCH_FIELD + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_SEARCH_STRING + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_CALENDARS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_FIELD + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"\"");
        }

        if (oldVersion < 1095)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_END_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_FOUND + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_END_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_FOUND + "=0");
        }

        if (oldVersion < 1100)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=4 WHERE " + KEY_E_PRIORITY + "=2");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=2 WHERE " + KEY_E_PRIORITY + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=-2 WHERE " + KEY_E_PRIORITY + "=-1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=-4 WHERE " + KEY_E_PRIORITY + "=-2");
        }

        if (oldVersion < 1105)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_SSID + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_SSID + "=\"\"");
        }

        if (oldVersion < 1106)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_CONNECTION_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_CONNECTION_TYPE + "=1");
        }

        if (oldVersion < 1110)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_ENABLED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_ENABLED + "=0");
        }

        if (oldVersion < 1111)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_EVENT_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_EVENT_TYPE + "=1");
        }

        if (oldVersion < 1112)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DELAY_START + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_START + "=0");
        }

        if (oldVersion < 1113)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_IS_IN_DELAY_START + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_START + "=0");
        }

        if (oldVersion < 1120)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_AFTER_DURATION_DO + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION + "=" + Profile.AFTERDURATIONDO_RESTARTEVENTS);
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_AFTER_DURATION_DO + "=" + Profile.AFTERDURATIONDO_RESTARTEVENTS);
        }

        if (oldVersion < 1125)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_WHEN_UNLOCKED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_WHEN_UNLOCKED + "=0");
        }

        if (oldVersion < 1130)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_ADAPTER_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_CONNECTION_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0");
        }

        if (oldVersion < 1140)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_ENABLED + " INTEGER");
            //db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_EVENT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACTS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACT_LIST_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_ENABLED + "=0");
            //db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1141)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_START_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_START_TIME + "=0");
        }

        if (oldVersion < 1150)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_ZEN_MODE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ZEN_MODE + "=0");
        }

        if (oldVersion < 1156)
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
            //{
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                        String brightness = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS));

                        //value|noChange|automatic|sharedProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            // hm, found brightness values without default profile :-/
                            /*
                            if (splits.length == 4)
                                brightness = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|0";
                            */
                            if (splits.length == 4)
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                         " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\" " +
                                        "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            //}
        }

        if (oldVersion < 1160)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_KEYGUARD + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_KEYGUARD + "=0");
        }

        if (oldVersion < 1165)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                                            KEY_DEVICE_BRIGHTNESS +
                                        " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    String brightness = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS));

                    //value|noChange|automatic|sharedProfile
                    String[] splits = brightness.split("\\|");

                    int percentage = Integer.parseInt(splits[0]);
                    percentage = (int)Profile.convertBrightnessToPercents(percentage, 255, 1);

                    // hm, found brightness values without default profile :-/
                    if (splits.length == 4)
                        brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                    else
                        brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|0";

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                                 " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\" " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1170)
        {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                            KEY_E_DELAY_START +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID)));
                    int delayStart = cursor.getInt(cursor.getColumnIndex(KEY_E_DELAY_START)) * 60;  // conversion to seconds

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_DELAY_START + "=" + delayStart + " " +
                                "WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        /*
        if (oldVersion < 1175)
        {
            if (android.os.Build.VERSION.SDK_INT < 21)
            {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                        String brightness = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS));

                        //value|noChange|automatic|sharedProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            int percentage = 50;

                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                         " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                        "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        }
        */

        if (oldVersion < 1180)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACT_GROUPS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACT_GROUPS + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACT_GROUPS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACT_GROUPS + "=\"\"");
        }


        if (oldVersion < 1203) {

            //db.execSQL("drop table " + TABLE_ACTIVITY_LOG);

            final String CREATE_ACTIVITYLOG_TABLE = "CREATE TABLE " + TABLE_ACTIVITY_LOG + "("
                    + KEY_AL_ID + " INTEGER PRIMARY KEY,"
                    + KEY_AL_LOG_DATE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + KEY_AL_LOG_TYPE + " INTEGER,"
                    + KEY_AL_EVENT_NAME + " TEXT,"
                    + KEY_AL_PROFILE_NAME + " TEXT,"
                    + KEY_AL_PROFILE_ICON + " TEXT,"
                    + KEY_AL_DURATION_DELAY + " INTEGER"
                    + ")";
            db.execSQL(CREATE_ACTIVITYLOG_TABLE);

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_AL_LOG_DATE_TIME ON " + TABLE_ACTIVITY_LOG + " (" + KEY_AL_LOG_DATE_TIME + ")");
        }

        if (oldVersion < 1210)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_ON_TOUCH + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_ON_TOUCH + "=0");
        }

        if (oldVersion < 1220)
        {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                   KEY_E_USE_END_TIME + "," +
                                                   KEY_E_START_TIME +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID)));
                    long startTime = cursor.getLong(cursor.getColumnIndex(KEY_E_START_TIME));

                    if (cursor.getInt(cursor.getColumnIndex(KEY_E_USE_END_TIME)) != 1)
                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                     " SET " + KEY_E_END_TIME + "=" + (startTime+5000) + ", "
                                             + KEY_E_USE_END_TIME + "=1" +
                                   " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1295)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_AT_END_DO + " INTEGER");

            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                   KEY_E_UNDONE_PROFILE +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID)));
                    int atEndDo;

                    if (cursor.isNull(cursor.getColumnIndex(KEY_E_UNDONE_PROFILE)) || (cursor.getInt(cursor.getColumnIndex(KEY_E_UNDONE_PROFILE)) == 0))
                        atEndDo = Event.EATENDDO_NONE;
                    else
                        atEndDo = Event.EATENDDO_UNDONE_PROFILE;

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_AT_END_DO + "=" + atEndDo +
                               " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1300)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_AVAILABILITY + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_AVAILABILITY + "=0");
        }

        if (oldVersion < 1310)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MANUAL_PROFILE_ACTIVATION + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MANUAL_PROFILE_ACTIVATION + "=0");
        }

        boolean doMergedTableCreate = false;
        if (oldVersion < 1320) {
            final String CREATE_MERGED_PROFILE_TABLE = profileTableCreationString(TABLE_MERGED_PROFILE);
            db.execSQL(CREATE_MERGED_PROFILE_TABLE);
            doMergedTableCreate = true;
        }

        if (oldVersion < 1330)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WIFI_AP + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1340)
        {
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_WIFI_AP + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1350)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                    KEY_DURATION +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    int delayStart = cursor.getInt(cursor.getColumnIndex(KEY_DURATION)) * 60;  // conversion to seconds

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                            " SET " + KEY_DURATION + "=" + delayStart + " " +
                            "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1370)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + "=-999");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_START_WHEN_ACTIVATED ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + ")");
        }

        if (oldVersion < 1380)
        {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                   KEY_E_CALENDAR_SEARCH_STRING + "," +
                                                   KEY_E_WIFI_SSID + "," +
                                                   KEY_E_BLUETOOTH_ADAPTER_NAME +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID)));
                    String calendarSearchString = cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_SEARCH_STRING)).replace("%", "\\%").replace("_", "\\_");
                    String wifiSSID = cursor.getString(cursor.getColumnIndex(KEY_E_WIFI_SSID)).replace("%", "\\%").replace("_", "\\_");
                    String bluetoothAdapterName = cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_ADAPTER_NAME)).replace("%", "\\%").replace("_", "\\_");

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"" + calendarSearchString + "\"," +
                                           KEY_E_WIFI_SSID + "=\"" + wifiSSID + "\"," +
                                           KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"" + bluetoothAdapterName + "\"" +
                               " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1390)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_DURATION + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_DURATION + "=5");
        }

        if (oldVersion < 1400)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_APPLICATIONS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_DURATION + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_APPLICATIONS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_DURATION + "=5");
        }
        if (oldVersion < 1410)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                    KEY_VOLUME_ZEN_MODE +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    int zenMode = cursor.getInt(cursor.getColumnIndex(KEY_VOLUME_ZEN_MODE));

                    if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23)) // Alarms only zen mode is supported from Android 6.0
                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_VOLUME_ZEN_MODE + "=3" + " " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1420)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1430)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_POWER_SAVE_MODE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1440)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_DEVICES_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_DEVICES_TYPE + "=0");
        }

        if (oldVersion < 1450)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_APPLICATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_APPLICATION_APPLICATIONS + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1460)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_END_WHEN_REMOVED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_END_WHEN_REMOVED + "=0");
        }

        if (oldVersion < 1470)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + "=0");
        }

        if (oldVersion < 1480) {
            final String CREATE_GEOFENCES_TABLE = "CREATE TABLE " + TABLE_GEOFENCES + "("
                    + KEY_G_ID + " INTEGER PRIMARY KEY,"
                    + KEY_G_LATITUDE + " DOUBLE,"
                    + KEY_G_LONGITUDE + " DOUBLE,"
                    + KEY_G_RADIUS + " FLOAT,"
                    + KEY_G_NAME + " TEXT"
                    + ")";
            db.execSQL(CREATE_GEOFENCES_TABLE);
        }

        if (oldVersion < 1490)
        {
            db.execSQL("ALTER TABLE " + TABLE_GEOFENCES + " ADD COLUMN " + KEY_G_CHECKED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_CHECKED + "=0");
        }

        if (oldVersion < 1500)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_FK_GEOFENCE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_FK_GEOFENCE + "=0");
        }

        if (oldVersion < 1510) {
            db.execSQL("ALTER TABLE " + TABLE_GEOFENCES + " ADD COLUMN " + KEY_G_TRANSITION + " INTEGER");

            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_TRANSITION + "=0");
        }

        if (oldVersion < 1520) {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_WHEN_OUTSIDE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1530)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DELAY_END + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_IS_IN_DELAY_END + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_END + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_END + "=0");
        }

        if (oldVersion < 1540)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_STATUS_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PAUSE_STATUS_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_STATUS_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PAUSE_STATUS_TIME + "=0");
        }

        if (oldVersion < 1560)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_ASK_FOR_DURATION + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_ASK_FOR_DURATION + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ASK_FOR_DURATION + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ASK_FOR_DURATION + "=0");
        }

        if (oldVersion < 1570)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
        }

        if (oldVersion < 1580)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_NOTIFICATION_LED + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_NOTIFICATION_LED + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_NOTIFICATION_LED + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_NOTIFICATION_LED + "=0");
        }

        if (oldVersion < 1600)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_SIDES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_DISTANCE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_SIDES + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISTANCE + "=0");
        }

        if (oldVersion < 1610)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_DISPLAY + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISPLAY + "=\"\"");
        }

        if (oldVersion < 1620)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1630)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_WHEN_RINGING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1640) {
            if (!doMergedTableCreate)
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_VIBRATE_WHEN_RINGING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1650) {
            final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE " + TABLE_SHORTCUTS + "("
                    + KEY_S_ID + " INTEGER PRIMARY KEY,"
                    + KEY_S_INTENT + " TEXT,"
                    + KEY_S_NAME + " TEXT"
                    + ")";
            db.execSQL(CREATE_SHORTCUTS_TABLE);
        }

        if (oldVersion < 1660) {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_FOR + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_FOR + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
        }

        if (oldVersion < 1670)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1680)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_CELLS + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_CELLS +  "=\"\"");
        }

        if (oldVersion < 1690) {
            final String CREATE_MOBILE_CELLS_TABLE = "CREATE TABLE " + TABLE_MOBILE_CELLS + "("
                    + KEY_MC_ID + " INTEGER PRIMARY KEY,"
                    + KEY_MC_CELL_ID + " INTEGER,"
                    + KEY_MC_NAME + " TEXT"
                    + ")";
            db.execSQL(CREATE_MOBILE_CELLS_TABLE);
        }

        if (oldVersion < 1700)
        {
            db.execSQL("ALTER TABLE " + TABLE_MOBILE_CELLS + " ADD COLUMN " + KEY_MC_NEW + " INTEGER");

            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_NEW +  "=0");
        }

        if (oldVersion < 1710)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_GEOFENCES + " TEXT");

            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                    KEY_E_LOCATION_FK_GEOFENCE +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long geofenceId = cursor.getLong(cursor.getColumnIndex(KEY_E_LOCATION_FK_GEOFENCE));

                    ContentValues values = new ContentValues();

                    if (geofenceId > 0) {
                        values.put(KEY_E_LOCATION_GEOFENCES, String.valueOf(geofenceId));
                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                    }
                    else {
                        values.put(KEY_E_LOCATION_GEOFENCES, "");
                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                    }

                } while (cursor.moveToNext());
            }

            cursor.close();

        }

        if (oldVersion < 1720)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_ORDER + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_ORDER +  "=0");
        }

        if (oldVersion < 1740)
        {
            // initialize startOrder
            final String selectQuery = "SELECT " + KEY_E_ID +
                    " FROM " + TABLE_EVENTS +
                    " ORDER BY " + KEY_E_PRIORITY;

            Cursor cursor = db.rawQuery(selectQuery, null);

            int startOrder = 0;
            if (cursor.moveToFirst()) {
                do {
                    //long id = cursor.getLong(cursor.getColumnIndex(KEY_E_ID));
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_START_ORDER, ++startOrder);
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1750)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_ENABLED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_ENABLED + "=0");
        }

        if (oldVersion < 1760) {
            final String CREATE_MOBILE_CELLS_TABLE = "CREATE TABLE " + TABLE_NFC_TAGS + "("
                    + KEY_NT_ID + " INTEGER PRIMARY KEY,"
                    + KEY_NT_NAME + " TEXT"
                    + ")";
            db.execSQL(CREATE_MOBILE_CELLS_TABLE);
        }

        if (oldVersion < 1770)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_NFC_TAGS + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_NFC_TAGS + "=\"\"");
        }

        if (oldVersion < 1780)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_START_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_START_TIME + "=0");
        }

        if (oldVersion < 1790)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_PERMANENT_RUN + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_PERMANENT_RUN + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_PERMANENT_RUN + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_PERMANENT_RUN + "=1");
        }

        if (oldVersion < 1800)
        {
            db.execSQL("ALTER TABLE " + TABLE_MOBILE_CELLS + " ADD COLUMN " + KEY_MC_LAST_CONNECTED_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_LAST_CONNECTED_TIME +  "=0");
        }

        if (oldVersion < 1810) {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_HIDE_STATUS_BAR_ICON + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_HIDE_STATUS_BAR_ICON + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_HIDE_STATUS_BAR_ICON + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_HIDE_STATUS_BAR_ICON + "=0");
        }

        if (oldVersion < 1820)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_LOCK_DEVICE + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_LOCK_DEVICE + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_LOCK_DEVICE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_LOCK_DEVICE + "=0");
        }

        if (oldVersion < 1830)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_START_BEFORE_EVENT + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_START_BEFORE_EVENT + "=0");
        }

        if (oldVersion < 1840)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_WIFI + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_BLUETOOTH + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_MOBILE_DATA + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_GPS + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_NFC + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_AIRPLANE_MODE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_WIFI + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_BLUETOOTH + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_MOBILE_DATA + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_GPS + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_NFC + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "=0");
        }

        /*if (oldVersion < 1850)
        {
        }*/

        if (oldVersion < 1860)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_CONNECT_TO_SSID + " TEXT");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_CONNECT_TO_SSID + " TEXT");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_CONNECT_TO_SSID + "=\""+Profile.CONNECTTOSSID_JUSTANY+"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_CONNECT_TO_SSID + "=\""+Profile.CONNECTTOSSID_JUSTANY+"\"");
        }

        if (oldVersion < 1870)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1880)
        {
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1890) {
            changePictureFilePathToUri(db/*, false*/);
        }

        if (oldVersion < 1900)
        {
            // conversion into local time
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=" + KEY_E_START_TIME + "-" + gmtOffset);
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=" + KEY_E_END_TIME + "-" + gmtOffset);
        }

        if (oldVersion < 1910)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_VIBRATE_START + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_VIBRATE_START + "=0");
        }

        if (oldVersion < 1920)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + "=0");
        }

        if (oldVersion < 1930)
        {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                   KEY_E_START_TIME + "," +
                                                   KEY_E_END_TIME +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();

                    long startTime = cursor.getLong(cursor.getColumnIndex(KEY_E_START_TIME));
                    long endTime = cursor.getLong(cursor.getColumnIndex(KEY_E_END_TIME));

                    Calendar calendar = Calendar.getInstance();

                    calendar.setTimeInMillis(startTime);
                    values.put(KEY_E_START_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                    calendar.setTimeInMillis(endTime);
                    values.put(KEY_E_END_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));

                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                } while (cursor.moveToNext());
            }

            cursor.close();
        }


        if (oldVersion < 1940) {
            PPApplication.logE("------------ DatabaseHandler.onUpgrade", "1940");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_AUTOROTATE ON " + TABLE_PROFILES + " (" + KEY_DEVICE_AUTOROTATE + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_CONNECT_TO_SSID ON " + TABLE_PROFILES + " (" + KEY_DEVICE_CONNECT_TO_SSID + ")");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_LOCATION_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__TIME_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_TIME_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BATTERY_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BATTERY_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALL_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIPHERAL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_PERIPHERAL_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALENDAR_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALENDAR_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__WIFI_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_WIFI_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SCREEN_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SCREEN_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BLUETOOTH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BLUETOOTH_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SMS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SMS_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NOTIFICATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_NOTIFICATION_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__APPLICATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_APPLICATION_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_LOCATION_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ORIENTATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ORIENTATION_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_MOBILE_CELLS_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NFC_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_NFC_ENABLED + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__RADIO_SWITCH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_RADIO_SWITCH_ENABLED + ")");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_GEOFENCES + " (" + KEY_G_NAME + ")");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_MOBILE_CELLS + " (" + KEY_MC_NAME + ")");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_NFC_TAGS + " (" + KEY_NT_NAME + ")");
        }

        if (oldVersion < 1950)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DURATION_NOTIFICATION_SOUND + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DURATION_NOTIFICATION_VIBRATE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION_NOTIFICATION_SOUND + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=0");

            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DURATION_NOTIFICATION_SOUND + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DURATION_NOTIFICATION_VIBRATE + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=0");
        }

        if (oldVersion < 1960)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_PERMANENT_RUN + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_START_TIME + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_START_TIME + "=0");
        }

        if (oldVersion < 1970)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SOUND_REPEAT_START + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_REPEAT_START + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "=15");
        }

        if (oldVersion < 1980)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WIFI_AP_PREFS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP_PREFS + "=0");

            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_WIFI_AP_PREFS + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WIFI_AP_PREFS + "=0");
        }

        if (oldVersion < 1990) {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                    KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START +
                    " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();

                    int repeatInterval = cursor.getInt(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START));

                    values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, repeatInterval * 60);

                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 2000)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_IN_CALL + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_MISSED_CALL + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_IN_CALL + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_MISSED_CALL + "=0");
        }

        if (oldVersion < 2010)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + "=0");

            if (!doMergedTableCreate)
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + "=0");
        }

        if (oldVersion < 2020)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + " INTEGER");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + "=0");

            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + "=0");
        }

        if (oldVersion < 2030)
        {
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_HEADS_UP_NOTIFICATIONS + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_HEADS_UP_NOTIFICATIONS + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_HEADS_UP_NOTIFICATIONS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_HEADS_UP_NOTIFICATIONS + "=0");
        }

        if (oldVersion < 2040)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_WHEN_ACTIVATED_PROFILE + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_WHEN_ACTIVATED_PROFILE + "=\"\"");

            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                    KEY_E_FK_PROFILE_START_WHEN_ACTIVATED +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();

                    long fkProfile = cursor.getLong(cursor.getColumnIndex(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED));

                    if (fkProfile != Profile.PROFILE_NO_ACTIVATE) {
                        values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, String.valueOf(fkProfile));
                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();

        }

        if (oldVersion < 2050)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2050");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + " TEXT");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + " TEXT");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 2060)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2060");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_ACTIVATION_BY_USER_COUNT + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_ACTIVATION_BY_USER_COUNT + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ACTIVATION_BY_USER_COUNT + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ACTIVATION_BY_USER_COUNT + "=0");

            db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ACTIVATION_BY_USER_COUNT ON " + TABLE_PROFILES + " (" + KEY_ACTIVATION_BY_USER_COUNT + ")");
        }

        if (oldVersion < 2070)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2070");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE_PREFS + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE_PREFS + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
        }

        if (oldVersion < 2080)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2080");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
        }

        if (oldVersion < 2090)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2090");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SCREEN_NIGHT_MODE + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_SCREEN_NIGHT_MODE + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SCREEN_NIGHT_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SCREEN_NIGHT_MODE + "=0");
        }

        if (oldVersion < 2100)
        {
            PPApplication.logE("DatabaseHandler.onUpgrade", "< 2100");

            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DTMF_TONE_WHEN_DIALING + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SOUND_ON_TOUCH + " INTEGER");
            if (!doMergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DTMF_TONE_WHEN_DIALING + " INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_SOUND_ON_TOUCH + " INTEGER");
            }

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_ON_TOUCH + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_ON_TOUCH + "=0");
        }

        if (oldVersion < 2110)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                                            KEY_DEVICE_WIFI_AP +
                                        " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    int wifiAP = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP));

                    if ((wifiAP == 3) && (android.os.Build.VERSION.SDK_INT >= 26)) // Toggle is not supported for wifi AP in Android 8+
                        db.execSQL("UPDATE " + TABLE_PROFILES +
                               " SET " + KEY_DEVICE_WIFI_AP + "=0" + " " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 2120) {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_SENSOR_PASSED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2130) {
            db.execSQL("ALTER TABLE " + TABLE_NFC_TAGS + " ADD COLUMN " + KEY_NT_UID + " TEXT");

            db.execSQL("UPDATE " + TABLE_NFC_TAGS + " SET " + KEY_NT_UID + "=\"\"");
        }

        if (oldVersion < 2140) {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_APPLICATION_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NFC_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PERIPHERAL_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_RADIO_SWITCH_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_SENSOR_PASSED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_TIME_SENSOR_PASSED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIPHERAL_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2150) {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_LOCK_DEVICE +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    int lockDevice = cursor.getInt(cursor.getColumnIndex(KEY_LOCK_DEVICE));

                    if (lockDevice == 3) {
                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_LOCK_DEVICE + "=1" + " " +
                                "WHERE " + KEY_ID + "=" + id);
                    }

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 2160)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_ALL_EVENTS + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ALL_EVENTS + "=0");
        }

        if (oldVersion < 2170)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ALARM_CLOCK_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ALARM_CLOCK_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ALARM_CLOCK_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ALARM_CLOCK_PERMANENT_RUN + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ALARM_CLOCK_SENSOR_PASSED + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2180)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SOUND_END + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_VIBRATE_END + " INTEGER");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_END + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_VIBRATE_END + "=0");
        }

        if (oldVersion < 2190) {
            final String CREATE_INTENTS_TABLE = "CREATE TABLE " + TABLE_INTENTS + "("
                    + KEY_IN_ID + " INTEGER PRIMARY KEY,"
                    + KEY_IN_PACKAGE_NAME + " TEXT,"
                    + KEY_IN_CLASS_NAME + " TEXT,"
                    + KEY_IN_ACTION + " TEXT,"
                    + KEY_IN_DATA + " TEXT,"
                    + KEY_IN_MIME_TYPE + " TEXT,"
                    + KEY_IN_EXTRA_KEY_1 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_1 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_1 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_2 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_2 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_2 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_3 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_3 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_3 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_4 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_4 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_4 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_5 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_5 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_5 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_6 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_6 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_6 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_7 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_7 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_7 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_8 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_8 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_8 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_9 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_9 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_9 + " INTEGER,"
                    + KEY_IN_EXTRA_KEY_10 + " TEXT,"
                    + KEY_IN_EXTRA_VALUE_10 + " TEXT,"
                    + KEY_IN_EXTRA_TYPE_10 + " INTEGER,"
                    + KEY_IN_CATEGORIES + " TEXT,"
                    + KEY_IN_FLAGS + " TEXT"
                    + ")";
            db.execSQL(CREATE_INTENTS_TABLE);
        }

        if (oldVersion < 2200)
        {
            db.execSQL("ALTER TABLE " + TABLE_INTENTS + " ADD COLUMN " + KEY_IN_NAME + " TEXT");

            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_NAME + "=\"\"");
        }

        if (oldVersion < 2230)
        {
            final String selectQuery = "SELECT * FROM " + TABLE_INTENTS;
            Cursor cursor = db.rawQuery(selectQuery, null);
            boolean columnExists =  (cursor.getColumnIndex(KEY_IN_ACTION) != -1);
            PPApplication.logE("DatabaseHandler.onUpgrade", "columnExists="+columnExists);
            cursor.close();
            if (!columnExists) {
                db.execSQL("ALTER TABLE " + TABLE_INTENTS + " ADD COLUMN " + KEY_IN_ACTION + " TEXT");

                db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_ACTION + "=\"\"");
            }
        }

        if (oldVersion < 2240)
        {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_PLUGGED + " TEXT");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_PLUGGED + "=\"\"");
        }

        if (oldVersion < 2270) {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                KEY_E_CALENDAR_SEARCH_STRING +
                    " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String calendarSearchString = cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_SEARCH_STRING));

                    String searchStringNew = "";
                    String[] searchStringSplits = calendarSearchString.split("\\|");
                    for (String split : searchStringSplits) {
                        if (!split.isEmpty()) {
                            String searchPattern = split;
                            if (searchPattern.startsWith("!")) {
                                searchPattern =  "\\" + searchPattern;
                            }
                            if (!searchStringNew.isEmpty())
                                //noinspection StringConcatenationInLoop
                                searchStringNew = searchStringNew + "|";
                            //noinspection StringConcatenationInLoop
                            searchStringNew = searchStringNew + searchPattern;
                        }
                    }

                    ContentValues values = new ContentValues();
                    values.put(KEY_E_CALENDAR_SEARCH_STRING, searchStringNew);

                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 2280)
        {
            db.execSQL("ALTER TABLE " + TABLE_INTENTS + " ADD COLUMN " + KEY_IN_USED_COUNT + " INTEGER");

            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_USED_COUNT + "=0");
        }

        if (oldVersion < 2290)
        {
            db.execSQL("ALTER TABLE " + TABLE_INTENTS + " ADD COLUMN " + KEY_IN_INTENT_TYPE + " INTEGER");

            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_INTENT_TYPE + "=0");
        }

        if (oldVersion < 2300) {
            final String selectQuery = "SELECT *" +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            Profile sharedProfile = Profile.getProfileFromSharedPreferences(context, "profile_preferences_default_profile");

            if (cursor.moveToFirst()) {
                do {
                    Profile profile = new Profile(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                            cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                            cursor.getString(cursor.getColumnIndex(KEY_ICON)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_CHECKED))) == 1,
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_PORDER))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGER_MODE))),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGTONE)),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_NOTIFICATION)),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_MEDIA)),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ALARM)),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SYSTEM)),
                            cursor.getString(cursor.getColumnIndex(KEY_VOLUME_VOICE)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AIRPLANE_MODE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BLUETOOTH))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SCREEN_TIMEOUT))),
                            cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA_PREFS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_GPS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOSYNC))),
                            cursor.isNull(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR)) || (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR))) == 1),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOROTATE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOCATION_SERVICE_PREFS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SPEAKER_PHONE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NFC))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_AFTER_DURATION_DO))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ZEN_MODE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_KEYGUARD))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_ON_TOUCH))),
                            cursor.isNull(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP)) ? 0 : Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_POWER_SAVE_MODE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ASK_FOR_DURATION))) == 1,
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_LED))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_WHEN_RINGING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_FOR))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HIDE_STATUS_BAR_ICON))) == 1,
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_LOCK_DEVICE))),
                            cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CONNECT_TO_SSID)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_WIFI_SCANNING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING))),
                            cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_SOUND)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_VIBRATE))) == 1,
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP_PREFS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_LOCATION_SCANNING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HEADS_UP_NOTIFICATIONS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE))),
                            cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                            Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ACTIVATION_BY_USER_COUNT))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE_PREFS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CLOSE_ALL_APPLICATIONS))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SCREEN_NIGHT_MODE))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DTMF_TONE_WHEN_DIALING))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ON_TOUCH)))
                    );

                    profile = Profile.getMappedProfile(profile, sharedProfile);

                    ContentValues values = new ContentValues();
                    values.put(KEY_NAME, profile._name);
                    values.put(KEY_ICON, profile._icon);
                    values.put(KEY_CHECKED, (profile._checked) ? 1 : 0);
                    values.put(KEY_PORDER, profile._porder);
                    values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                    values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                    values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                    values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                    values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
                    values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
                    values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
                    values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
                    values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                    values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
                    values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                    values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
                    values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                    values.put(KEY_SOUND_ALARM, profile._soundAlarm);
                    values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                    values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
                    values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                    values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                    values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                    values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                    values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                    values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                    values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                    values.put(KEY_DEVICE_GPS, profile._deviceGPS);
                    values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                    values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                    values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                    values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                    values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                    values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                    values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                    values.put(KEY_DEVICE_NFC, profile._deviceNFC);
                    values.put(KEY_DURATION, profile._duration);
                    values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                    values.put(KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                    values.put(KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                    values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                    values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                    values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                    values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                    values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                    values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                    values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
                    values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                    values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                    values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                    values.put(KEY_LOCK_DEVICE, profile._lockDevice);
                    values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                    values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, profile._applicationDisableWifiScanning);
                    values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, profile._applicationDisableBluetoothScanning);
                    values.put(KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                    values.put(KEY_APPLICATION_DISABLE_LOCATION_SCANNING, profile._applicationDisableLocationScanning);
                    values.put(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, profile._applicationDisableMobileCellScanning);
                    values.put(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, profile._applicationDisableOrientationScanning);
                    values.put(KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                    values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                    values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                    values.put(KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                    values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                    values.put(KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                    values.put(KEY_SCREEN_NIGHT_MODE, profile._screenNightMode);
                    values.put(KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                    values.put(KEY_SOUND_ON_TOUCH, profile._soundOnTouch);

                    // updating row
                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[]{String.valueOf(profile._id)});

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        PPApplication.logE("DatabaseHandler.onUpgrade", "END");

    }

    private void startRunningCommand() throws Exception {
        if (runningImportExport)
            runningImportExportCondition.await();
        runningCommand = true;
    }

    private void stopRunningCommand() {
        runningCommand = false;
        runningCommandCondition.signalAll();
        importExportLock.unlock();
    }

// PROFILES --------------------------------------------------------------------------------

    // Adding new profile
    void addProfile(Profile profile, boolean merged) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                int porder = getMaxProfileOrder() + 1;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NAME, profile._name); // Profile Name
                values.put(KEY_ICON, profile._icon); // Icon
                values.put(KEY_CHECKED, (profile._checked) ? 1 : 0); // Checked
                values.put(KEY_PORDER, porder); // POrder
                //values.put(KEY_PORDER, profile._porder); // POrder
                values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
                values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
                values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
                values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
                values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
                values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
                values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                values.put(KEY_SOUND_ALARM, profile._soundAlarm);
                values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
                values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                values.put(KEY_DEVICE_GPS, profile._deviceGPS);
                values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                values.put(KEY_DEVICE_NFC, profile._deviceNFC);
                values.put(KEY_DURATION, profile._duration);
                values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                values.put(KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                values.put(KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
                values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                values.put(KEY_LOCK_DEVICE, profile._lockDevice);
                values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, profile._applicationDisableWifiScanning);
                values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, profile._applicationDisableBluetoothScanning);
                values.put(KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                values.put(KEY_APPLICATION_DISABLE_LOCATION_SCANNING, profile._applicationDisableLocationScanning);
                values.put(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, profile._applicationDisableMobileCellScanning);
                values.put(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, profile._applicationDisableOrientationScanning);
                values.put(KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                values.put(KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                values.put(KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                values.put(KEY_SCREEN_NIGHT_MODE, profile._screenNightMode);
                values.put(KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                values.put(KEY_SOUND_ON_TOUCH, profile._soundOnTouch);

                // Insert Row
                if (!merged) {
                    profile._id = db.insert(TABLE_PROFILES, null, values);
                    profile._porder = porder;
                } else {
                    values.put(KEY_ID, profile._id);
                    db.insert(TABLE_MERGED_PROFILE, null, values);
                }
                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single profile
    Profile getProfile(long profile_id, boolean merged) {
        importExportLock.lock();
        try {

            Profile profile = null;

            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                String tableName = TABLE_PROFILES;
                if (merged)
                    tableName = TABLE_MERGED_PROFILE;
                Cursor cursor = db.query(tableName,
                        new String[]{KEY_ID,
                                KEY_NAME,
                                KEY_ICON,
                                KEY_CHECKED,
                                KEY_PORDER,
                                KEY_VOLUME_RINGER_MODE,
                                KEY_VOLUME_RINGTONE,
                                KEY_VOLUME_NOTIFICATION,
                                KEY_VOLUME_MEDIA,
                                KEY_VOLUME_ALARM,
                                KEY_VOLUME_SYSTEM,
                                KEY_VOLUME_VOICE,
                                KEY_SOUND_RINGTONE_CHANGE,
                                KEY_SOUND_RINGTONE,
                                KEY_SOUND_NOTIFICATION_CHANGE,
                                KEY_SOUND_NOTIFICATION,
                                KEY_SOUND_ALARM_CHANGE,
                                KEY_SOUND_ALARM,
                                KEY_DEVICE_AIRPLANE_MODE,
                                KEY_DEVICE_WIFI,
                                KEY_DEVICE_BLUETOOTH,
                                KEY_DEVICE_SCREEN_TIMEOUT,
                                KEY_DEVICE_BRIGHTNESS,
                                KEY_DEVICE_WALLPAPER_CHANGE,
                                KEY_DEVICE_WALLPAPER,
                                KEY_DEVICE_MOBILE_DATA,
                                KEY_DEVICE_MOBILE_DATA_PREFS,
                                KEY_DEVICE_GPS,
                                KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                KEY_DEVICE_AUTOSYNC,
                                KEY_SHOW_IN_ACTIVATOR,
                                KEY_DEVICE_AUTOROTATE,
                                KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                KEY_VOLUME_SPEAKER_PHONE,
                                KEY_DEVICE_NFC,
                                KEY_DURATION,
                                KEY_AFTER_DURATION_DO,
                                KEY_DURATION_NOTIFICATION_SOUND,
                                KEY_DURATION_NOTIFICATION_VIBRATE,
                                KEY_VOLUME_ZEN_MODE,
                                KEY_DEVICE_KEYGUARD,
                                KEY_VIBRATE_ON_TOUCH,
                                KEY_DEVICE_WIFI_AP,
                                KEY_DEVICE_POWER_SAVE_MODE,
                                KEY_ASK_FOR_DURATION,
                                KEY_DEVICE_NETWORK_TYPE,
                                KEY_NOTIFICATION_LED,
                                KEY_VIBRATE_WHEN_RINGING,
                                KEY_DEVICE_WALLPAPER_FOR,
                                KEY_HIDE_STATUS_BAR_ICON,
                                KEY_LOCK_DEVICE,
                                KEY_DEVICE_CONNECT_TO_SSID,
                                KEY_APPLICATION_DISABLE_WIFI_SCANNING,
                                KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING,
                                KEY_DEVICE_WIFI_AP_PREFS,
                                KEY_APPLICATION_DISABLE_LOCATION_SCANNING,
                                KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING,
                                KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING,
                                KEY_HEADS_UP_NOTIFICATIONS,
                                KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                KEY_ACTIVATION_BY_USER_COUNT,
                                KEY_DEVICE_NETWORK_TYPE_PREFS,
                                KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                KEY_SCREEN_NIGHT_MODE,
                                KEY_DTMF_TONE_WHEN_DIALING,
                                KEY_SOUND_ON_TOUCH
                        },
                        KEY_ID + "=?",
                        new String[]{String.valueOf(profile_id)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        profile = new Profile(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_ICON)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_CHECKED))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_PORDER))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGER_MODE))),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_VOICE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AIRPLANE_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BLUETOOTH))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SCREEN_TIMEOUT))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_GPS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOSYNC))),
                                cursor.isNull(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR)) || (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR))) == 1),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOROTATE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOCATION_SERVICE_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SPEAKER_PHONE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NFC))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_AFTER_DURATION_DO))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ZEN_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_KEYGUARD))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_ON_TOUCH))),
                                cursor.isNull(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP)) ? 0 : Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_POWER_SAVE_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ASK_FOR_DURATION))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_LED))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_WHEN_RINGING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_FOR))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HIDE_STATUS_BAR_ICON))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_LOCK_DEVICE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CONNECT_TO_SSID)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_WIFI_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING))),
                                cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_SOUND)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_VIBRATE))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_LOCATION_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HEADS_UP_NOTIFICATIONS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ACTIVATION_BY_USER_COUNT))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CLOSE_ALL_APPLICATIONS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SCREEN_NIGHT_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DTMF_TONE_WHEN_DIALING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ON_TOUCH)))
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return profile;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All Profiles
    List<Profile> getAllProfiles() {
        importExportLock.lock();
        try {

            List<Profile> profileList = new ArrayList<>();

            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_NAME + "," +
                        KEY_ICON + "," +
                        KEY_CHECKED + "," +
                        KEY_PORDER + "," +
                        KEY_VOLUME_RINGER_MODE + "," +
                        KEY_VOLUME_RINGTONE + "," +
                        KEY_VOLUME_NOTIFICATION + "," +
                        KEY_VOLUME_MEDIA + "," +
                        KEY_VOLUME_ALARM + "," +
                        KEY_VOLUME_SYSTEM + "," +
                        KEY_VOLUME_VOICE + "," +
                        KEY_SOUND_RINGTONE_CHANGE + "," +
                        KEY_SOUND_RINGTONE + "," +
                        KEY_SOUND_NOTIFICATION_CHANGE + "," +
                        KEY_SOUND_NOTIFICATION + "," +
                        KEY_SOUND_ALARM_CHANGE + "," +
                        KEY_SOUND_ALARM + "," +
                        KEY_DEVICE_AIRPLANE_MODE + "," +
                        KEY_DEVICE_WIFI + "," +
                        KEY_DEVICE_BLUETOOTH + "," +
                        KEY_DEVICE_SCREEN_TIMEOUT + "," +
                        KEY_DEVICE_BRIGHTNESS + "," +
                        KEY_DEVICE_WALLPAPER_CHANGE + "," +
                        KEY_DEVICE_WALLPAPER + "," +
                        KEY_DEVICE_MOBILE_DATA + "," +
                        KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                        KEY_DEVICE_GPS + "," +
                        KEY_DEVICE_RUN_APPLICATION_CHANGE + "," +
                        KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "," +
                        KEY_DEVICE_AUTOSYNC + "," +
                        KEY_SHOW_IN_ACTIVATOR + "," +
                        KEY_DEVICE_AUTOROTATE + "," +
                        KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                        KEY_VOLUME_SPEAKER_PHONE + "," +
                        KEY_DEVICE_NFC + "," +
                        KEY_DURATION + "," +
                        KEY_AFTER_DURATION_DO + "," +
                        KEY_DURATION_NOTIFICATION_SOUND + "," +
                        KEY_DURATION_NOTIFICATION_VIBRATE + "," +
                        KEY_VOLUME_ZEN_MODE + "," +
                        KEY_DEVICE_KEYGUARD + "," +
                        KEY_VIBRATE_ON_TOUCH + "," +
                        KEY_DEVICE_WIFI_AP + "," +
                        KEY_DEVICE_POWER_SAVE_MODE + "," +
                        KEY_ASK_FOR_DURATION + "," +
                        KEY_DEVICE_NETWORK_TYPE + "," +
                        KEY_NOTIFICATION_LED + "," +
                        KEY_VIBRATE_WHEN_RINGING + "," +
                        KEY_DEVICE_WALLPAPER_FOR + "," +
                        KEY_HIDE_STATUS_BAR_ICON + "," +
                        KEY_LOCK_DEVICE + "," +
                        KEY_DEVICE_CONNECT_TO_SSID + "," +
                        KEY_APPLICATION_DISABLE_WIFI_SCANNING + "," +
                        KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "," +
                        KEY_DEVICE_WIFI_AP_PREFS + "," +
                        KEY_APPLICATION_DISABLE_LOCATION_SCANNING + "," +
                        KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + "," +
                        KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + "," +
                        KEY_HEADS_UP_NOTIFICATIONS + "," +
                        KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "," +
                        KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "," +
                        KEY_ACTIVATION_BY_USER_COUNT + "," +
                        KEY_DEVICE_NETWORK_TYPE_PREFS + "," +
                        KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "," +
                        KEY_SCREEN_NIGHT_MODE + "," +
                        KEY_DTMF_TONE_WHEN_DIALING + "," +
                        KEY_SOUND_ON_TOUCH +
                " FROM " + TABLE_PROFILES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile();
                        profile._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                        profile._name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        profile._icon = (cursor.getString(cursor.getColumnIndex(KEY_ICON)));
                        profile._checked = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_CHECKED))) == 1;
                        profile._porder = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_PORDER))));
                        profile._volumeRingerMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGER_MODE)));
                        profile._volumeRingtone = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGTONE));
                        profile._volumeNotification = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_NOTIFICATION));
                        profile._volumeMedia = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_MEDIA));
                        profile._volumeAlarm = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ALARM));
                        profile._volumeSystem = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SYSTEM));
                        profile._volumeVoice = cursor.getString(cursor.getColumnIndex(KEY_VOLUME_VOICE));
                        profile._soundRingtoneChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE_CHANGE)));
                        profile._soundRingtone = cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE));
                        profile._soundNotificationChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION_CHANGE)));
                        profile._soundNotification = cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION));
                        profile._soundAlarmChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM_CHANGE)));
                        profile._soundAlarm = cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM));
                        profile._deviceAirplaneMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AIRPLANE_MODE)));
                        profile._deviceWiFi = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI)));
                        profile._deviceBluetooth = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BLUETOOTH)));
                        profile._deviceScreenTimeout = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SCREEN_TIMEOUT)));
                        profile._deviceBrightness = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS));
                        profile._deviceWallpaperChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_CHANGE)));
                        profile._deviceWallpaper = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER));
                        profile._deviceMobileData = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA)));
                        profile._deviceMobileDataPrefs = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA_PREFS)));
                        profile._deviceGPS = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_GPS)));
                        profile._deviceRunApplicationChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_CHANGE)));
                        profile._deviceRunApplicationPackageName = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME));
                        profile._deviceAutoSync = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOSYNC)));
                        profile._showInActivator = cursor.isNull(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR)) || (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR))) == 1);
                        profile._deviceAutoRotate = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOROTATE)));
                        profile._deviceLocationServicePrefs = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOCATION_SERVICE_PREFS)));
                        profile._volumeSpeakerPhone = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SPEAKER_PHONE)));
                        profile._deviceNFC = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NFC)));
                        profile._duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
                        profile._afterDurationDo = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_AFTER_DURATION_DO)));
                        profile._durationNotificationSound = cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_SOUND));
                        profile._durationNotificationVibrate = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_VIBRATE))) == 1);
                        profile._volumeZenMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ZEN_MODE)));
                        profile._deviceKeyguard = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_KEYGUARD)));
                        profile._vibrationOnTouch = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_ON_TOUCH)));
                        profile._deviceWiFiAP = cursor.isNull(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP)) ? 0 : Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP)));
                        profile._devicePowerSaveMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_POWER_SAVE_MODE)));
                        profile._askForDuration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ASK_FOR_DURATION))) == 1;
                        profile._deviceNetworkType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE)));
                        profile._notificationLed = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_LED)));
                        profile._vibrateWhenRinging = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_WHEN_RINGING)));
                        profile._deviceWallpaperFor = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_FOR)));
                        profile._hideStatusBarIcon = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HIDE_STATUS_BAR_ICON))) == 1);
                        profile._lockDevice = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_LOCK_DEVICE)));
                        profile._deviceConnectToSSID = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CONNECT_TO_SSID));
                        profile._applicationDisableWifiScanning = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_WIFI_SCANNING)));
                        profile._applicationDisableBluetoothScanning = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING)));
                        profile._deviceWiFiAPPrefs = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP_PREFS)));
                        profile._applicationDisableLocationScanning = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_LOCATION_SCANNING)));
                        profile._applicationDisableMobileCellScanning = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING)));
                        profile._applicationDisableOrientationScanning = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING)));
                        profile._headsUpNotifications = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HEADS_UP_NOTIFICATIONS)));
                        profile._deviceForceStopApplicationChange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE)));
                        profile._deviceForceStopApplicationPackageName = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME));
                        profile._activationByUserCount = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ACTIVATION_BY_USER_COUNT)));
                        profile._deviceNetworkTypePrefs = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE_PREFS)));
                        profile._deviceCloseAllApplications = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CLOSE_ALL_APPLICATIONS)));
                        profile._screenNightMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SCREEN_NIGHT_MODE)));
                        profile._dtmfToneWhenDialing = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DTMF_TONE_WHEN_DIALING)));
                        profile._soundOnTouch = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ON_TOUCH)));
                        // Adding contact to list
                        profileList.add(profile);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }

            return profileList;

        } finally {
            stopRunningCommand();
        }
    }

    // Updating single profile
    void updateProfile(Profile profile) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NAME, profile._name);
                values.put(KEY_ICON, profile._icon);
                values.put(KEY_CHECKED, (profile._checked) ? 1 : 0);
                values.put(KEY_PORDER, profile._porder);
                values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
                values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
                values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
                values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
                values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
                values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
                values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                values.put(KEY_SOUND_ALARM, profile._soundAlarm);
                values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
                values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                values.put(KEY_DEVICE_GPS, profile._deviceGPS);
                values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                values.put(KEY_DEVICE_NFC, profile._deviceNFC);
                values.put(KEY_DURATION, profile._duration);
                values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                values.put(KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                values.put(KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
                values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                values.put(KEY_LOCK_DEVICE, profile._lockDevice);
                values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, profile._applicationDisableWifiScanning);
                values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, profile._applicationDisableBluetoothScanning);
                values.put(KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                values.put(KEY_APPLICATION_DISABLE_LOCATION_SCANNING, profile._applicationDisableLocationScanning);
                values.put(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, profile._applicationDisableMobileCellScanning);
                values.put(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, profile._applicationDisableOrientationScanning);
                values.put(KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                values.put(KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                values.put(KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                values.put(KEY_SCREEN_NIGHT_MODE, profile._screenNightMode);
                values.put(KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                values.put(KEY_SOUND_ON_TOUCH, profile._soundOnTouch);

                // updating row
                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                        new String[]{String.valueOf(profile._id)});
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single profile
    void deleteProfile(Profile profile) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();
                try {

                    // unlink shortcuts from profile
                    String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
                    for (String split : splits) {
                        boolean shortcut = Application.isShortcut(split);
                        if (shortcut) {
                            long shortcutId = Application.getShortcutId(split);
                            deleteShortcut(shortcutId);
                        }
                    }

                    db.delete(TABLE_PROFILES, KEY_ID + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    // unlink profile from events
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_FK_PROFILE_START, 0);
                    db.update(TABLE_EVENTS, values, KEY_E_FK_PROFILE_START + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values2 = new ContentValues();
                    values2.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values2, KEY_E_FK_PROFILE_END + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values3 = new ContentValues();
                    values3.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values3, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                            KEY_E_START_WHEN_ACTIVATED_PROFILE +
                                                            " FROM " + TABLE_EVENTS;
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            String oldFkProfiles = cursor.getString(cursor.getColumnIndex(KEY_E_START_WHEN_ACTIVATED_PROFILE));
                            if (!oldFkProfiles.isEmpty()) {
                                splits = oldFkProfiles.split("\\|");
                                StringBuilder newFkProfiles = new StringBuilder();
                                for (String split : splits) {
                                    long fkProfile = Long.valueOf(split);
                                    if (fkProfile != profile._id) {
                                        if (newFkProfiles.length() > 0)
                                            newFkProfiles.append("|");
                                        newFkProfiles.append(split);
                                    }
                                }
                                values = new ContentValues();
                                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, newFkProfiles.toString());
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting all profiles
    void deleteAllProfiles() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    db.delete(TABLE_PROFILES, null, null);

                    db.delete(TABLE_SHORTCUTS, null, null);

                    // unlink profiles from events
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_FK_PROFILE_START, 0);
                    values.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, "");
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting profiles Count
    int getProfilesCount(boolean forActivator) {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                if (forActivator)
                    countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES + " WHERE " + KEY_SHOW_IN_ACTIVATOR + "=1";
                else
                    countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();

                if (forActivator && (!ApplicationPreferences.applicationActivatorHeader(context))) {
                    Profile profile = getActivatedProfile();
                    if ((profile != null) && (!profile._showInActivator)) {
                        r++;
                    }
                }

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting max(porder)
    private int getMaxProfileOrder() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT MAX(" + KEY_PORDER + ") FROM " + TABLE_PROFILES;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    private void doActivateProfile(Profile profile, boolean activate)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();
                try {
                    // update all profiles checked to false
                    ContentValues valuesAll = new ContentValues();
                    valuesAll.put(KEY_CHECKED, 0);
                    db.update(TABLE_PROFILES, valuesAll, null, null);

                    // updating checked = true for profile
                    //profile.setChecked(true);

                    if (activate && (profile != null)) {
                        ContentValues values = new ContentValues();
                        //values.put(KEY_CHECKED, (profile.getChecked()) ? 1 : 0);
                        values.put(KEY_CHECKED, 1);

                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[]{String.valueOf(profile._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void activateProfile(Profile profile)
    {
        doActivateProfile(profile, true);
    }

    void deactivateProfile()
    {
        doActivateProfile(null, false);
    }

    Profile getActivatedProfile()
    {
        importExportLock.lock();
        try {
            Profile profile = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_PROFILES,
                        new String[]{KEY_ID,
                                KEY_NAME,
                                KEY_ICON,
                                KEY_CHECKED,
                                KEY_PORDER,
                                KEY_VOLUME_RINGER_MODE,
                                KEY_VOLUME_RINGTONE,
                                KEY_VOLUME_NOTIFICATION,
                                KEY_VOLUME_MEDIA,
                                KEY_VOLUME_ALARM,
                                KEY_VOLUME_SYSTEM,
                                KEY_VOLUME_VOICE,
                                KEY_SOUND_RINGTONE_CHANGE,
                                KEY_SOUND_RINGTONE,
                                KEY_SOUND_NOTIFICATION_CHANGE,
                                KEY_SOUND_NOTIFICATION,
                                KEY_SOUND_ALARM_CHANGE,
                                KEY_SOUND_ALARM,
                                KEY_DEVICE_AIRPLANE_MODE,
                                KEY_DEVICE_WIFI,
                                KEY_DEVICE_BLUETOOTH,
                                KEY_DEVICE_SCREEN_TIMEOUT,
                                KEY_DEVICE_BRIGHTNESS,
                                KEY_DEVICE_WALLPAPER_CHANGE,
                                KEY_DEVICE_WALLPAPER,
                                KEY_DEVICE_MOBILE_DATA,
                                KEY_DEVICE_MOBILE_DATA_PREFS,
                                KEY_DEVICE_GPS,
                                KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                KEY_DEVICE_AUTOSYNC,
                                KEY_SHOW_IN_ACTIVATOR,
                                KEY_DEVICE_AUTOROTATE,
                                KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                KEY_VOLUME_SPEAKER_PHONE,
                                KEY_DEVICE_NFC,
                                KEY_DURATION,
                                KEY_AFTER_DURATION_DO,
                                KEY_DURATION_NOTIFICATION_SOUND,
                                KEY_DURATION_NOTIFICATION_VIBRATE,
                                KEY_VOLUME_ZEN_MODE,
                                KEY_DEVICE_KEYGUARD,
                                KEY_VIBRATE_ON_TOUCH,
                                KEY_DEVICE_WIFI_AP,
                                KEY_DEVICE_POWER_SAVE_MODE,
                                KEY_ASK_FOR_DURATION,
                                KEY_DEVICE_NETWORK_TYPE,
                                KEY_NOTIFICATION_LED,
                                KEY_VIBRATE_WHEN_RINGING,
                                KEY_DEVICE_WALLPAPER_FOR,
                                KEY_HIDE_STATUS_BAR_ICON,
                                KEY_LOCK_DEVICE,
                                KEY_DEVICE_CONNECT_TO_SSID,
                                KEY_APPLICATION_DISABLE_WIFI_SCANNING,
                                KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING,
                                KEY_DEVICE_WIFI_AP_PREFS,
                                KEY_APPLICATION_DISABLE_LOCATION_SCANNING,
                                KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING,
                                KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING,
                                KEY_HEADS_UP_NOTIFICATIONS,
                                KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                KEY_ACTIVATION_BY_USER_COUNT,
                                KEY_DEVICE_NETWORK_TYPE_PREFS,
                                KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                KEY_SCREEN_NIGHT_MODE,
                                KEY_DTMF_TONE_WHEN_DIALING,
                                KEY_SOUND_ON_TOUCH
                        },
                        KEY_CHECKED + "=?",
                        new String[]{"1"}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {

                        profile = new Profile(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_ICON)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_CHECKED))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_PORDER))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGER_MODE))),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndex(KEY_VOLUME_VOICE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_RINGTONE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_NOTIFICATION)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_SOUND_ALARM)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AIRPLANE_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BLUETOOTH))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SCREEN_TIMEOUT))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_BRIGHTNESS)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_GPS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOSYNC))),
                                cursor.isNull(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR)) || (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR))) == 1),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_AUTOROTATE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOCATION_SERVICE_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_SPEAKER_PHONE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NFC))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_AFTER_DURATION_DO))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VOLUME_ZEN_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_KEYGUARD))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_ON_TOUCH))),
                                cursor.isNull(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP)) ? 0 : Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_POWER_SAVE_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ASK_FOR_DURATION))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_LED))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VIBRATE_WHEN_RINGING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_FOR))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HIDE_STATUS_BAR_ICON))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_LOCK_DEVICE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CONNECT_TO_SSID)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_WIFI_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING))),
                                cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_SOUND)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DURATION_NOTIFICATION_VIBRATE))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WIFI_AP_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_LOCATION_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_HEADS_UP_NOTIFICATIONS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE))),
                                cursor.getString(cursor.getColumnIndex(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ACTIVATION_BY_USER_COUNT))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE_PREFS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DEVICE_CLOSE_ALL_APPLICATIONS))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SCREEN_NIGHT_MODE))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DTMF_TONE_WHEN_DIALING))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_SOUND_ON_TOUCH)))
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return profile;
        } finally {
            stopRunningCommand();
        }
    }

    long getProfileIdByName(String name)
    {
        importExportLock.lock();
        try {
            long id = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_PROFILES,
                        new String[]{KEY_ID},
                        KEY_NAME + "=?",
                        new String[]{name}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {
                        id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return id;
        } finally {
            stopRunningCommand();
        }
    }

    void setProfileOrder(List<Profile> list)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();
                try {

                    for (int i = 0; i < list.size(); i++) {
                        Profile profile = list.get(i);
                        profile._porder = i + 1;

                        values.put(KEY_PORDER, profile._porder);

                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[]{String.valueOf(profile._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getProfileIcon(Profile profile)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_PROFILES,
                        new String[]{KEY_ICON},
                        KEY_ID + "=?",
                        new String[]{Long.toString(profile._id)}, null, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        profile._icon = cursor.getString(cursor.getColumnIndex(KEY_ICON));
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void saveMergedProfile(Profile profile) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    db.delete(TABLE_MERGED_PROFILE, null, null);

                    addProfile(profile, true);

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    private void changePictureFilePathToUri(SQLiteDatabase database/*, boolean lock*/) {
        //if (lock)
        //    importExportLock.lock();
        //try {
            try {
                //if (lock)
                //    startRunningCommand();

                SQLiteDatabase db;
                if (database == null) {
                    //SQLiteDatabase db = this.getWritableDatabase();
                    db = getMyWritableDatabase();
                } else
                    db = database;

                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_ICON + "," +
                        KEY_DEVICE_WALLPAPER_CHANGE + "," +
                        KEY_DEVICE_WALLPAPER +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (database == null)
                    db.beginTransaction();
                //noinspection TryFinallyCanBeTryWithResources
                try {

                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                            String icon = cursor.getString(cursor.getColumnIndex(KEY_ICON));
                            int wallpaperChange = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER_CHANGE));
                            String wallpaper = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_WALLPAPER));

                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "id=" + id);
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "icon=" + icon);
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "wallpaperChange=" + wallpaperChange);
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "wallpaper=" + wallpaper);

                            ContentValues values = new ContentValues();

                            try {
                                String[] splits = icon.split("\\|");
                                String iconIdentifier = splits[0];
                                String isIconResourceId = splits[1];
                                String useCustomColorForIcon = "0";
                                String iconCustomColor = "0";
                                if (splits.length == 4) {
                                    useCustomColorForIcon = splits[2];
                                    iconCustomColor = splits[3];
                                }

                                PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "isIconResourceId=" + isIconResourceId);

                                if (!isIconResourceId.equals("1")) {
                                    Uri imageUri = WallpaperViewPreference.getImageContentUri(context, iconIdentifier);
                                    PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "icon uri=" + imageUri);
                                    if (imageUri != null)
                                        values.put(KEY_ICON, imageUri.toString() + "|" +
                                                isIconResourceId + "|" +
                                                useCustomColorForIcon + "|" +
                                                iconCustomColor);
                                    else
                                        values.put(KEY_ICON, "ic_profile_default|1|0|0");
                                }
                            } catch (Exception e) {
                                PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", e.getMessage());
                                values.put(KEY_ICON, "ic_profile_default|1|0|0");
                            }
                            if (wallpaperChange == 1) {
                                try {
                                    String[] splits = wallpaper.split("\\|");
                                    Uri imageUri = WallpaperViewPreference.getImageContentUri(context, splits[0]);
                                    PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "wallpaper uri=" + imageUri);
                                    if (imageUri != null)
                                        values.put(KEY_DEVICE_WALLPAPER, imageUri.toString());
                                    else {
                                        values.put(KEY_DEVICE_WALLPAPER_CHANGE, 0);
                                        values.put(KEY_DEVICE_WALLPAPER, "-");
                                    }
                                } catch (Exception e) {
                                    PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", e.getMessage());
                                    values.put(KEY_DEVICE_WALLPAPER_CHANGE, 0);
                                    values.put(KEY_DEVICE_WALLPAPER, "-");
                                }
                            } else
                                values.put(KEY_DEVICE_WALLPAPER, "-");

                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "values.size()=" + values.size());
                            if (values.size() > 0) {
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
                            }

                        } while (cursor.moveToNext());
                    }

                    if (database == null)
                        db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", e.getMessage());
                } finally {
                    if (database == null)
                        db.endTransaction();
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        //} finally {
        //    if (lock)
        //        stopRunningCommand();
        //}
    }

    int getTypeProfilesCount(int profileType/*, boolean sharedProfile*/)
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String whereString = "";
                if (profileType == PTYPE_CONNECT_TO_SSID) {
                    String profileTypeChecked;
                    //if (!sharedProfile)
                        profileTypeChecked = KEY_DEVICE_CONNECT_TO_SSID + "!=\"" + Profile.CONNECTTOSSID_JUSTANY + "\"";
                    //else
                    //    profileTypeChecked = KEY_DEVICE_CONNECT_TO_SSID + "=\"" + Profile.CONNECTTOSSID_SHAREDPROFILE + "\"";
                    whereString = " WHERE " + profileTypeChecked;
                }
                if (profileType == PTYPE_FORCE_STOP) {
                    String profileTypeChecked;
                    //if (!sharedProfile)
                        profileTypeChecked = KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "!="+Profile.NO_CHANGE_VALUE_STR;
                    //else
                    //    profileTypeChecked = KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "="+Profile.SHARED_PROFILE_VALUE_STR;
                    whereString = " WHERE " + profileTypeChecked;
                }
                if (profileType == PTYPE_LOCK_DEVICE) {
                    String profileTypeChecked;
                    //if (!sharedProfile)
                        profileTypeChecked = KEY_LOCK_DEVICE + "!="+Profile.NO_CHANGE_VALUE_STR;
                    //else
                    //    profileTypeChecked = KEY_LOCK_DEVICE + "="+Profile.SHARED_PROFILE_VALUE_STR;
                    whereString = " WHERE " + profileTypeChecked;
                }

                countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES + whereString;
                //PPApplication.logE("DatabaseHandler.getTypeProfilesCount", "countQuery="+countQuery);

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }
                //PPApplication.logE("DatabaseHandler.getTypeProfilesCount", "r="+r);

                //db.close();

            } catch (Exception ignored) {
                //PPApplication.logE("DatabaseHandler.getTypeProfilesCount", Log.getStackTraceString(e));
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    private long getActivationByUserCount(long profileId) {
        importExportLock.lock();
        try {
            long r = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_PROFILES,
                        new String[]{KEY_ACTIVATION_BY_USER_COUNT},
                        KEY_ID + "=?",
                        new String[]{Long.toString(profileId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Long.parseLong(cursor.getString(0));
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    private void increaseActivationByUserCount(long profileId) {
        long count = getActivationByUserCount(profileId);
        ++count;

        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_ACTIVATION_BY_USER_COUNT, count);

                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[]{String.valueOf(profileId)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void increaseActivationByUserCount(Profile profile) {
        if (profile != null) {
            long count = getActivationByUserCount(profile._id);
            ++count;
            profile._activationByUserCount = count;
            increaseActivationByUserCount(profile._id);
        }
    }

    List<Profile> getProfilesForDynamicShortcuts(boolean counted/*, int limit*/) {
        importExportLock.lock();
        try {

            List<Profile> profileList = new ArrayList<>();

            try {
                startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_NAME + "," +
                                                KEY_ICON + "," +
                                                KEY_ACTIVATION_BY_USER_COUNT +
                                    " FROM " + TABLE_PROFILES +
                                    " WHERE " + KEY_SHOW_IN_ACTIVATOR + "=1";

                if (counted) {
                    selectQuery = selectQuery +
                            " AND " + KEY_ACTIVATION_BY_USER_COUNT + "> 0" +
                            " ORDER BY " + KEY_ACTIVATION_BY_USER_COUNT + " DESC " +
                            " LIMIT " + "4";//limit;
                }
                else {
                    selectQuery = selectQuery +
                            " AND " + KEY_ACTIVATION_BY_USER_COUNT + "= 0" +
                            " ORDER BY " + KEY_PORDER +
                            " LIMIT " + "4";//limit;
                }

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile();
                        profile._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                        /*if (counted)
                            profile._name = "(" + cursor.getString(cursor.getColumnIndex(KEY_ACTIVATION_BY_USER_COUNT)) + ")"  + cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        else*/
                            profile._name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                        profile._icon = (cursor.getString(cursor.getColumnIndex(KEY_ICON)));
                        // Adding contact to list
                        profileList.add(profile);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }

            return profileList;

        } finally {
            stopRunningCommand();
        }
    }

// EVENTS --------------------------------------------------------------------------------

    // Adding new event
    void addEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                int startOrder = getMaxEventStartOrder() + 1;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NAME, event._name); // Event Name
                values.put(KEY_E_START_ORDER, startOrder); // start order
                values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart); // profile start
                values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd); // profile end
                values.put(KEY_E_STATUS, event.getStatus()); // event status
                values.put(KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart); // notification sound
                values.put(KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart); // notification vibrate
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart); // repeat notification sound
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart); // repeat notification sound interval
                values.put(KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd); // notification sound
                values.put(KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd); // notification vibrate
                values.put(KEY_E_FORCE_RUN, event._forceRun ? 1 : 0); // force run when manual profile activation
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0); // temporary blocked
                values.put(KEY_E_PRIORITY, event._priority); // priority
                values.put(KEY_E_DELAY_START, event._delayStart); // delay for start
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0); // event is in delay before start
                values.put(KEY_E_AT_END_DO, event._atEndDo); //at end of event do
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0); // manual profile activation
                values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_DELAY_END, event._delayEnd); // delay for end
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0); // event is in delay after pause
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime); // time for status RUNNING
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime); // time for change status from RUNNING to PAUSE
                values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0); // no pause event by manual profile activation
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile); // start when profile is activated

                db.beginTransaction();

                try {
                    // Inserting Row
                    event._id = db.insert(TABLE_EVENTS, null, values);
                    updateEventPreferences(event, db);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single event
    Event getEvent(long event_id) {
        importExportLock.lock();
        try {
            Event event = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{KEY_E_ID,
                                KEY_E_NAME,
                                KEY_E_START_ORDER,
                                KEY_E_FK_PROFILE_START,
                                KEY_E_FK_PROFILE_END,
                                KEY_E_STATUS,
                                KEY_E_NOTIFICATION_SOUND_START,
                                KEY_E_NOTIFICATION_VIBRATE_START,
                                KEY_E_NOTIFICATION_SOUND_REPEAT_START,
                                KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START,
                                KEY_E_NOTIFICATION_SOUND_END,
                                KEY_E_NOTIFICATION_VIBRATE_END,
                                KEY_E_FORCE_RUN,
                                KEY_E_BLOCKED,
                                KEY_E_PRIORITY,
                                KEY_E_DELAY_START,
                                KEY_E_IS_IN_DELAY_START,
                                KEY_E_AT_END_DO,
                                KEY_E_MANUAL_PROFILE_ACTIVATION,
                                KEY_E_START_WHEN_ACTIVATED_PROFILE,
                                KEY_E_DELAY_END,
                                KEY_E_IS_IN_DELAY_END,
                                KEY_E_START_STATUS_TIME,
                                KEY_E_PAUSE_STATUS_TIME,
                                KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event_id)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {

                        event = new Event(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_E_NAME)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_START_ORDER))),
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_FK_PROFILE_START))),
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_FK_PROFILE_END))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_STATUS))),
                                cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_START)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_FORCE_RUN))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLOCKED))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_PRIORITY))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_DELAY_START))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_START))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_AT_END_DO))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_MANUAL_PROFILE_ACTIVATION))) == 1,
                                cursor.getString(cursor.getColumnIndex(KEY_E_START_WHEN_ACTIVATED_PROFILE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_DELAY_END))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_END))) == 1,
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_START_STATUS_TIME))),
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_PAUSE_STATUS_TIME))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_VIBRATE_START))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_REPEAT_START))) == 1,
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START))),
                                cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_END)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_VIBRATE_END))) == 1
                        );
                    }

                    cursor.close();
                }

                if (event != null)
                    getEventPreferences(event, db);

                //db.close();

            } catch (Exception ignored) {
            }
            return event;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All Events
    List<Event> getAllEvents() {
        importExportLock.lock();
        try {
            List<Event> eventList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_NAME + "," +
                        KEY_E_FK_PROFILE_START + "," +
                        KEY_E_FK_PROFILE_END + "," +
                        KEY_E_STATUS + "," +
                        KEY_E_NOTIFICATION_SOUND_START + "," +
                        KEY_E_NOTIFICATION_VIBRATE_START + "," +
                        KEY_E_NOTIFICATION_SOUND_REPEAT_START + "," +
                        KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "," +
                        KEY_E_NOTIFICATION_SOUND_END + "," +
                        KEY_E_NOTIFICATION_VIBRATE_END + "," +
                        KEY_E_FORCE_RUN + "," +
                        KEY_E_BLOCKED + "," +
                        KEY_E_PRIORITY + "," +
                        KEY_E_DELAY_START + "," +
                        KEY_E_IS_IN_DELAY_START + "," +
                        KEY_E_AT_END_DO + "," +
                        KEY_E_MANUAL_PROFILE_ACTIVATION + "," +
                        KEY_E_START_WHEN_ACTIVATED_PROFILE + "," +
                        KEY_E_DELAY_END + "," +
                        KEY_E_IS_IN_DELAY_END + "," +
                        KEY_E_START_STATUS_TIME + "," +
                        KEY_E_PAUSE_STATUS_TIME + "," +
                        KEY_E_START_ORDER + "," +
                        KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION +
                        " FROM " + TABLE_EVENTS +
                        " ORDER BY " + KEY_E_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Event event = new Event();
                        event._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ID)));
                        event._name = cursor.getString(cursor.getColumnIndex(KEY_E_NAME));
                        event._fkProfileStart = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_FK_PROFILE_START)));
                        event._fkProfileEnd = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_FK_PROFILE_END)));
                        event.setStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_STATUS))));
                        event._notificationSoundStart = cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_START));
                        event._notificationVibrateStart = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_VIBRATE_START))) == 1;
                        event._repeatNotificationStart = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_REPEAT_START))) == 1;
                        event._repeatNotificationIntervalStart = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START)));
                        event._notificationSoundEnd = cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SOUND_END));
                        event._notificationVibrateEnd = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_VIBRATE_END))) == 1;
                        event._forceRun = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_FORCE_RUN))) == 1;
                        event._blocked = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLOCKED))) == 1;
                        event._priority = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_PRIORITY)));
                        event._delayStart = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_DELAY_START)));
                        event._isInDelayStart = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_START))) == 1;
                        event._atEndDo = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_AT_END_DO)));
                        event._manualProfileActivation = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_MANUAL_PROFILE_ACTIVATION))) == 1;
                        event._startWhenActivatedProfile = cursor.getString(cursor.getColumnIndex(KEY_E_START_WHEN_ACTIVATED_PROFILE));
                        event._delayEnd = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_DELAY_END)));
                        event._isInDelayEnd = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_END))) == 1;
                        event._startStatusTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_START_STATUS_TIME)));
                        event._pauseStatusTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_PAUSE_STATUS_TIME)));
                        event._startOrder = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_START_ORDER)));
                        event._noPauseByManualActivation = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION))) == 1;
                        event.createEventPreferences();
                        getEventPreferences(event, db);
                        // Adding contact to list
                        eventList.add(event);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return eventList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single event
    void updateEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NAME, event._name);
                values.put(KEY_E_START_ORDER, event._startOrder);
                values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart);
                values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd);
                values.put(KEY_E_STATUS, event.getStatus());
                values.put(KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart);
                values.put(KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart);
                values.put(KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd);
                values.put(KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd ? 1 : 0);
                values.put(KEY_E_FORCE_RUN, event._forceRun ? 1 : 0);
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);
                //values.put(KEY_E_UNDONE_PROFILE, 0);
                values.put(KEY_E_PRIORITY, event._priority);
                values.put(KEY_E_DELAY_START, event._delayStart);
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(KEY_E_AT_END_DO, event._atEndDo);
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0);
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile);
                values.put(KEY_E_DELAY_END, event._delayEnd);
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);
                values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});
                    updateEventPreferences(event, db);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEvent", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single event
    void deleteEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENTS, KEY_E_ID + " = ?",
                        new String[]{String.valueOf(event._id)});
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting all events
    void deleteAllEvents() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENTS, null, null);
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unlinkEventsFromProfile(Profile profile)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_FK_PROFILE_START, 0);
                    db.update(TABLE_EVENTS, values, KEY_E_FK_PROFILE_START + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values2 = new ContentValues();
                    values2.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values2, KEY_E_FK_PROFILE_END + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values3 = new ContentValues();
                    values3.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values3, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    final String selectQuery = "SELECT " + KEY_E_ID + "," +
                            KEY_E_START_WHEN_ACTIVATED_PROFILE +
                            " FROM " + TABLE_EVENTS;
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            String oldFkProfiles = cursor.getString(cursor.getColumnIndex(KEY_E_START_WHEN_ACTIVATED_PROFILE));
                            if (!oldFkProfiles.isEmpty()) {
                                String[] splits = oldFkProfiles.split("\\|");
                                StringBuilder newFkProfiles = new StringBuilder();
                                for (String split : splits) {
                                    long fkProfile = Long.valueOf(split);
                                    if (fkProfile != profile._id) {
                                        if (newFkProfiles.length() > 0)
                                            newFkProfiles.append("|");
                                        newFkProfiles.append(split);
                                    }
                                }
                                values = new ContentValues();
                                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, newFkProfiles.toString());
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unlinkAllEvents()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_FK_PROFILE_START, 0);
                values.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, "");

                // updating row
                db.update(TABLE_EVENTS, values, null, null);

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting max(startOrder)
    private int getMaxEventStartOrder() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT MAX(" + KEY_E_START_ORDER + ") FROM " + TABLE_EVENTS;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void setEventStartOrder(List<Event> list)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();
                try {

                    for (int i = 0; i < list.size(); i++) {
                        Event event = list.get(i);
                        event._startOrder = i + 1;

                        values.put(KEY_E_START_ORDER, event._startOrder);

                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                new String[]{String.valueOf(event._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    private void getEventPreferences(Event event, SQLiteDatabase db) {
        getEventPreferencesTime(event, db);
        getEventPreferencesBattery(event, db);
        getEventPreferencesCall(event, db);
        getEventPreferencesPeripheral(event, db);
        getEventPreferencesCalendar(event, db);
        getEventPreferencesWifi(event, db);
        getEventPreferencesScreen(event, db);
        getEventPreferencesBluetooth(event, db);
        getEventPreferencesSMS(event, db);
        getEventPreferencesNotification(event, db);
        getEventPreferencesApplication(event, db);
        getEventPreferencesLocation(event, db);
        getEventPreferencesOrientation(event, db);
        getEventPreferencesMobileCells(event, db);
        getEventPreferencesNFC(event, db);
        getEventPreferencesRadioSwitch(event, db);
        getEventPreferencesAlarmClock(event, db);
    }

    private void getEventPreferencesTime(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_TIME_ENABLED,
                        KEY_E_DAYS_OF_WEEK,
                        KEY_E_START_TIME,
                        KEY_E_END_TIME,
                        //KEY_E_USE_END_TIME
                        KEY_E_TIME_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesTime eventPreferences = event._eventPreferencesTime;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_TIME_ENABLED))) == 1);

                String daysOfWeek = cursor.getString(cursor.getColumnIndex(KEY_E_DAYS_OF_WEEK));

                if (daysOfWeek != null)
                {
                    String[] splits = daysOfWeek.split("\\|");
                    if (splits[0].equals(DaysOfWeekPreference.allValue))
                    {
                        eventPreferences._sunday = true;
                        eventPreferences._monday = true;
                        eventPreferences._tuesday = true;
                        eventPreferences._wednesday = true;
                        eventPreferences._thursday = true;
                        eventPreferences._friday = true;
                        eventPreferences._saturday = true;
                    }
                    else
                    {
                        eventPreferences._sunday = false;
                        eventPreferences._monday = false;
                        eventPreferences._tuesday = false;
                        eventPreferences._wednesday = false;
                        eventPreferences._thursday = false;
                        eventPreferences._friday = false;
                        eventPreferences._saturday = false;
                        for (String value : splits)
                        {
                            eventPreferences._sunday = eventPreferences._sunday || value.equals("0");
                            eventPreferences._monday = eventPreferences._monday || value.equals("1");
                            eventPreferences._tuesday = eventPreferences._tuesday || value.equals("2");
                            eventPreferences._wednesday = eventPreferences._wednesday || value.equals("3");
                            eventPreferences._thursday = eventPreferences._thursday || value.equals("4");
                            eventPreferences._friday = eventPreferences._friday || value.equals("5");
                            eventPreferences._saturday = eventPreferences._saturday || value.equals("6");
                        }
                    }
                }
                eventPreferences._startTime = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_START_TIME)));
                eventPreferences._endTime = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_END_TIME)));
                //eventPreferences._useEndTime = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_USE_END_TIME))) == 1) ? true : false;
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_TIME_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesBattery(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_BATTERY_ENABLED,
                        KEY_E_BATTERY_LEVEL_LOW,
                        KEY_E_BATTERY_LEVEL_HIGHT,
                        KEY_E_BATTERY_CHARGING,
                        KEY_E_BATTERY_POWER_SAVE_MODE,
                        KEY_E_BATTERY_SENSOR_PASSED,
                        KEY_E_BATTERY_PLUGGED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_ENABLED))) == 1);
                eventPreferences._levelLow = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_LEVEL_LOW)));
                eventPreferences._levelHight = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_LEVEL_HIGHT)));
                eventPreferences._charging = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_CHARGING)));
                eventPreferences._powerSaveMode = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_POWER_SAVE_MODE))) == 1);
                eventPreferences._plugged = cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_PLUGGED));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BATTERY_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesCall(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_CALL_ENABLED,
                        KEY_E_CALL_EVENT,
                        KEY_E_CALL_CONTACTS,
                        KEY_E_CALL_CONTACT_LIST_TYPE,
                        KEY_E_CALL_CONTACT_GROUPS,
                        KEY_E_CALL_DURATION,
                        KEY_E_CALL_PERMANENT_RUN,
                        KEY_E_CALL_START_TIME,
                        KEY_E_CALL_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCall eventPreferences = event._eventPreferencesCall;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_ENABLED))) == 1);
                eventPreferences._callEvent = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_EVENT)));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndex(KEY_E_CALL_CONTACTS));
                eventPreferences._contactListType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_CONTACT_LIST_TYPE)));
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndex(KEY_E_CALL_CONTACT_GROUPS));
                eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_START_TIME)));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndex(KEY_E_CALL_DURATION));
                eventPreferences._permanentRun = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_PERMANENT_RUN))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesPeripheral(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_PERIPHERAL_ENABLED,
                        KEY_E_PERIPHERAL_TYPE,
                        KEY_E_PERIPHERAL_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesPeripherals eventPreferences = event._eventPreferencesPeripherals;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_PERIPHERAL_ENABLED))) == 1);
                eventPreferences._peripheralType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_PERIPHERAL_TYPE)));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_PERIPHERAL_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_CALENDAR_ENABLED,
                        KEY_E_CALENDAR_CALENDARS,
                        KEY_E_CALENDAR_SEARCH_FIELD,
                        KEY_E_CALENDAR_SEARCH_STRING,
                        KEY_E_CALENDAR_EVENT_START_TIME,
                        KEY_E_CALENDAR_EVENT_END_TIME,
                        KEY_E_CALENDAR_EVENT_FOUND,
                        KEY_E_CALENDAR_AVAILABILITY,
                        KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS,
                        KEY_E_CALENDAR_START_BEFORE_EVENT,
                        KEY_E_CALENDAR_SENSOR_PASSED,
                        KEY_E_CALENDAR_ALL_EVENTS
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_ENABLED))) == 1);
                eventPreferences._calendars = cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_CALENDARS));
                eventPreferences._searchField = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_SEARCH_FIELD)));
                eventPreferences._searchString = cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_SEARCH_STRING));
                eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_START_TIME)));
                eventPreferences._endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_END_TIME)));
                eventPreferences._eventFound = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_FOUND))) == 1);
                eventPreferences._availability = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_AVAILABILITY)));
                eventPreferences._ignoreAllDayEvents = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS))) == 1);
                eventPreferences._startBeforeEvent = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_START_BEFORE_EVENT)));
                eventPreferences._allEvents = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_ALL_EVENTS))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesWifi(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_WIFI_ENABLED,
                                                KEY_E_WIFI_SSID,
                                                KEY_E_WIFI_CONNECTION_TYPE,
                                                KEY_E_WIFI_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_WIFI_ENABLED))) == 1);
                eventPreferences._SSID = cursor.getString(cursor.getColumnIndex(KEY_E_WIFI_SSID));
                eventPreferences._connectionType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_WIFI_CONNECTION_TYPE)));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_WIFI_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesScreen(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_SCREEN_ENABLED,
                                                KEY_E_SCREEN_EVENT_TYPE,
                                                KEY_E_SCREEN_WHEN_UNLOCKED,
                                                KEY_E_SCREEN_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SCREEN_ENABLED))) == 1);
                eventPreferences._eventType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SCREEN_EVENT_TYPE)));
                eventPreferences._whenUnlocked = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SCREEN_WHEN_UNLOCKED))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SCREEN_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_BLUETOOTH_ENABLED,
                                                KEY_E_BLUETOOTH_ADAPTER_NAME,
                                                KEY_E_BLUETOOTH_CONNECTION_TYPE,
                                                KEY_E_BLUETOOTH_DEVICES_TYPE,
                                                KEY_E_BLUETOOTH_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_ENABLED))) == 1);
                eventPreferences._adapterName = cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_ADAPTER_NAME));
                eventPreferences._connectionType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_CONNECTION_TYPE)));
                eventPreferences._devicesType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_DEVICES_TYPE)));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_BLUETOOTH_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesSMS(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_SMS_ENABLED,
                        //KEY_E_SMS_EVENT,
                        KEY_E_SMS_CONTACTS,
                        KEY_E_SMS_CONTACT_LIST_TYPE,
                        KEY_E_SMS_START_TIME,
                        KEY_E_SMS_CONTACT_GROUPS,
                        KEY_E_SMS_DURATION,
                        KEY_E_SMS_PERMANENT_RUN,
                        KEY_E_SMS_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_ENABLED))) == 1);
                //eventPreferences._smsEvent = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_EVENT)));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndex(KEY_E_SMS_CONTACTS));
                eventPreferences._contactListType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_CONTACT_LIST_TYPE)));
                eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_START_TIME)));
                //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                //    PPApplication.logE("[SMS sensor] DatabaseHandler.getEventPreferencesSMS", "startTime="+eventPreferences._startTime);
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndex(KEY_E_SMS_CONTACT_GROUPS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndex(KEY_E_SMS_DURATION));
                eventPreferences._permanentRun = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_PERMANENT_RUN))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesNotification(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_NOTIFICATION_ENABLED,
                        KEY_E_NOTIFICATION_APPLICATIONS,
                        //KEY_E_NOTIFICATION_START_TIME,
                        //KEY_E_NOTIFICATION_DURATION,
                        //KEY_E_NOTIFICATION_END_WHEN_REMOVED,
                        //KEY_E_NOTIFICATION_PERMANENT_RUN,
                        KEY_E_NOTIFICATION_IN_CALL,
                        KEY_E_NOTIFICATION_MISSED_CALL,
                        KEY_E_NOTIFICATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_ENABLED))) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_APPLICATIONS));
                eventPreferences._inCall = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_IN_CALL))) == 1);
                eventPreferences._missedCall = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_MISSED_CALL))) == 1);
                //eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_START_TIME)));
                //eventPreferences._duration = cursor.getInt(cursor.getColumnIndex(KEY_E_NOTIFICATION_DURATION));
                //eventPreferences._endWhenRemoved = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_END_WHEN_REMOVED))) == 1);
                //eventPreferences._permanentRun = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_PERMANENT_RUN))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesApplication(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_APPLICATION_ENABLED,
                        KEY_E_APPLICATION_APPLICATIONS,
                        KEY_E_APPLICATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_APPLICATION_ENABLED))) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndex(KEY_E_APPLICATION_APPLICATIONS));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_APPLICATION_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesLocation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_LOCATION_ENABLED,
                        KEY_E_LOCATION_GEOFENCES,
                        KEY_E_LOCATION_WHEN_OUTSIDE,
                        KEY_E_LOCATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_LOCATION_ENABLED))) == 1);
                eventPreferences._geofences = cursor.getString(cursor.getColumnIndex(KEY_E_LOCATION_GEOFENCES));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndex(KEY_E_LOCATION_WHEN_OUTSIDE)) == 1;
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_LOCATION_SENSOR_PASSED))));

            }
            cursor.close();
        }
    }

    private void getEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ORIENTATION_ENABLED,
                        KEY_E_ORIENTATION_SIDES,
                        KEY_E_ORIENTATION_DISTANCE,
                        KEY_E_ORIENTATION_DISPLAY,
                        KEY_E_ORIENTATION_IGNORE_APPLICATIONS,
                        KEY_E_ORIENTATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_ORIENTATION_ENABLED))) == 1);
                eventPreferences._sides = cursor.getString(cursor.getColumnIndex(KEY_E_ORIENTATION_SIDES));
                eventPreferences._distance = cursor.getInt(cursor.getColumnIndex(KEY_E_ORIENTATION_DISTANCE));
                eventPreferences._display = cursor.getString(cursor.getColumnIndex(KEY_E_ORIENTATION_DISPLAY));
                eventPreferences._ignoredApplications = cursor.getString(cursor.getColumnIndex(KEY_E_ORIENTATION_IGNORE_APPLICATIONS));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_ORIENTATION_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_MOBILE_CELLS_ENABLED,
                        KEY_E_MOBILE_CELLS_CELLS,
                        KEY_E_MOBILE_CELLS_WHEN_OUTSIDE,
                        KEY_E_MOBILE_CELLS_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_MOBILE_CELLS_ENABLED))) == 1);
                eventPreferences._cells = cursor.getString(cursor.getColumnIndex(KEY_E_MOBILE_CELLS_CELLS));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndex(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE)) == 1;
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_MOBILE_CELLS_SENSOR_PASSED))));

            }
            cursor.close();
        }
    }

    private void getEventPreferencesNFC(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_NFC_ENABLED,
                            KEY_E_NFC_NFC_TAGS,
                            KEY_E_NFC_DURATION,
                            KEY_E_NFC_START_TIME,
                            KEY_E_NFC_PERMANENT_RUN,
                            KEY_E_NFC_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NFC_ENABLED))) == 1);
                eventPreferences._nfcTags = cursor.getString(cursor.getColumnIndex(KEY_E_NFC_NFC_TAGS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndex(KEY_E_NFC_DURATION));
                eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_NFC_START_TIME)));
                eventPreferences._permanentRun = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NFC_PERMANENT_RUN))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_NFC_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_RADIO_SWITCH_ENABLED,
                        KEY_E_RADIO_SWITCH_WIFI,
                        KEY_E_RADIO_SWITCH_BLUETOOTH,
                        KEY_E_RADIO_SWITCH_MOBILE_DATA,
                        KEY_E_RADIO_SWITCH_GPS,
                        KEY_E_RADIO_SWITCH_NFC,
                        KEY_E_RADIO_SWITCH_AIRPLANE_MODE,
                        KEY_E_RADIO_SWITCH_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_ENABLED))) == 1);
                eventPreferences._wifi = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_WIFI)));
                eventPreferences._bluetooth = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_BLUETOOTH)));
                eventPreferences._mobileData = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_MOBILE_DATA)));
                eventPreferences._gps = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_GPS)));
                eventPreferences._nfc = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_NFC)));
                eventPreferences._airplaneMode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_AIRPLANE_MODE)));
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_RADIO_SWITCH_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ALARM_CLOCK_ENABLED,
                        KEY_E_ALARM_CLOCK_START_TIME,
                        KEY_E_ALARM_CLOCK_DURATION,
                        KEY_E_ALARM_CLOCK_PERMANENT_RUN,
                        KEY_E_ALARM_CLOCK_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_ENABLED))) == 1);
                eventPreferences._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_START_TIME)));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_DURATION));
                eventPreferences._permanentRun = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_PERMANENT_RUN))) == 1);
                eventPreferences.setSensorPassed(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_SENSOR_PASSED))));
            }
            cursor.close();
        }
    }

    private void updateEventPreferences(Event event, SQLiteDatabase db) {
        updateEventPreferencesTime(event, db);
        updateEventPreferencesBattery(event, db);
        updateEventPreferencesCall(event, db);
        updateEventPreferencesPeripheral(event, db);
        updateEventPreferencesCalendar(event, db);
        updateEventPreferencesWifi(event, db);
        updateEventPreferencesScreen(event, db);
        updateEventPreferencesBluetooth(event, db);
        updateEventPreferencesSMS(event, db);
        updateEventPreferencesNotification(event, db);
        updateEventPreferencesApplication(event, db);
        updateEventPreferencesLocation(event, db);
        updateEventPreferencesOrientation(event, db);
        updateEventPreferencesMobileCells(event, db);
        updateEventPreferencesNFC(event, db);
        updateEventPreferencesRadioSwitch(event, db);
        updateEventPreferencesAlarmClock(event, db);
    }

    private void updateEventPreferencesTime(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesTime eventPreferences = event._eventPreferencesTime;

        String daysOfWeek = "";
        if (eventPreferences._sunday) daysOfWeek = daysOfWeek + "0|";
        if (eventPreferences._monday) daysOfWeek = daysOfWeek + "1|";
        if (eventPreferences._tuesday) daysOfWeek = daysOfWeek + "2|";
        if (eventPreferences._wednesday) daysOfWeek = daysOfWeek + "3|";
        if (eventPreferences._thursday) daysOfWeek = daysOfWeek + "4|";
        if (eventPreferences._friday) daysOfWeek = daysOfWeek + "5|";
        if (eventPreferences._saturday) daysOfWeek = daysOfWeek + "6|";

        values.put(KEY_E_TIME_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_DAYS_OF_WEEK, daysOfWeek);
        values.put(KEY_E_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_END_TIME, eventPreferences._endTime);
        //values.put(KEY_E_USE_END_TIME, (eventPreferences._useEndTime) ? 1 : 0);
        values.put(KEY_E_TIME_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesBattery(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

        values.put(KEY_E_BATTERY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BATTERY_LEVEL_LOW, eventPreferences._levelLow);
        values.put(KEY_E_BATTERY_LEVEL_HIGHT, eventPreferences._levelHight);
        values.put(KEY_E_BATTERY_CHARGING, eventPreferences._charging);
        values.put(KEY_E_BATTERY_POWER_SAVE_MODE, eventPreferences._powerSaveMode ? 1 : 0);
        values.put(KEY_E_BATTERY_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_BATTERY_PLUGGED, eventPreferences._plugged);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesCall(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCall eventPreferences = event._eventPreferencesCall;

        values.put(KEY_E_CALL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_CALL_EVENT, eventPreferences._callEvent);
        values.put(KEY_E_CALL_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_CALL_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_CALL_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_CALL_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_CALL_DURATION, eventPreferences._duration);
        values.put(KEY_E_CALL_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_CALL_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesPeripheral(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesPeripherals eventPreferences = event._eventPreferencesPeripherals;

        values.put(KEY_E_PERIPHERAL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_PERIPHERAL_TYPE, eventPreferences._peripheralType);
        values.put(KEY_E_PERIPHERAL_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

        values.put(KEY_E_CALENDAR_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_CALENDAR_CALENDARS, eventPreferences._calendars);
        values.put(KEY_E_CALENDAR_SEARCH_FIELD, eventPreferences._searchField);
        values.put(KEY_E_CALENDAR_SEARCH_STRING, eventPreferences._searchString);
        values.put(KEY_E_CALENDAR_EVENT_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_CALENDAR_EVENT_END_TIME, eventPreferences._endTime);
        values.put(KEY_E_CALENDAR_EVENT_FOUND, (eventPreferences._eventFound) ? 1 : 0);
        values.put(KEY_E_CALENDAR_AVAILABILITY, eventPreferences._availability);
        values.put(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, (eventPreferences._ignoreAllDayEvents) ? 1 : 0);
        values.put(KEY_E_CALENDAR_START_BEFORE_EVENT, eventPreferences._startBeforeEvent);
        values.put(KEY_E_CALENDAR_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_CALENDAR_ALL_EVENTS, (eventPreferences._allEvents) ? 1 : 0);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesWifi(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

        values.put(KEY_E_WIFI_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_WIFI_SSID, eventPreferences._SSID);
        values.put(KEY_E_WIFI_CONNECTION_TYPE, eventPreferences._connectionType);
        values.put(KEY_E_WIFI_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesScreen(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

        values.put(KEY_E_SCREEN_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_SCREEN_EVENT_TYPE, eventPreferences._eventType);
        values.put(KEY_E_SCREEN_WHEN_UNLOCKED, (eventPreferences._whenUnlocked) ? 1 : 0);
        values.put(KEY_E_SCREEN_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

        values.put(KEY_E_BLUETOOTH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BLUETOOTH_ADAPTER_NAME, eventPreferences._adapterName);
        values.put(KEY_E_BLUETOOTH_CONNECTION_TYPE, eventPreferences._connectionType);
        values.put(KEY_E_BLUETOOTH_DEVICES_TYPE, eventPreferences._devicesType);
        values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesSMS(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

        values.put(KEY_E_SMS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        //values.put(KEY_E_SMS_EVENT, eventPreferences._smsEvent);
        values.put(KEY_E_SMS_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_SMS_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_SMS_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_SMS_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_SMS_DURATION, eventPreferences._duration);
        values.put(KEY_E_SMS_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_SMS_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesNotification(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

        values.put(KEY_E_NOTIFICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_APPLICATIONS, eventPreferences._applications);
        values.put(KEY_E_NOTIFICATION_IN_CALL, (eventPreferences._inCall) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_MISSED_CALL, (eventPreferences._missedCall) ? 1 : 0);
        //values.put(KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        //values.put(KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);
        //values.put(KEY_E_NOTIFICATION_END_WHEN_REMOVED, (eventPreferences._endWhenRemoved) ? 1 : 0);
        //values.put(KEY_E_NOTIFICATION_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesApplication(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

        values.put(KEY_E_APPLICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_APPLICATION_APPLICATIONS, eventPreferences._applications);
        //values.put(KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        //values.put(KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);
        values.put(KEY_E_APPLICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesLocation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

        values.put(KEY_E_LOCATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_LOCATION_GEOFENCES, eventPreferences._geofences);
        values.put(KEY_E_LOCATION_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(KEY_E_LOCATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

        values.put(KEY_E_ORIENTATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ORIENTATION_SIDES, eventPreferences._sides);
        values.put(KEY_E_ORIENTATION_DISTANCE, eventPreferences._distance);
        values.put(KEY_E_ORIENTATION_DISPLAY, eventPreferences._display);
        values.put(KEY_E_ORIENTATION_IGNORE_APPLICATIONS, eventPreferences._ignoredApplications);
        values.put(KEY_E_ORIENTATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

        values.put(KEY_E_MOBILE_CELLS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);
        values.put(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesNFC(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

        values.put(KEY_E_NFC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_NFC_NFC_TAGS, eventPreferences._nfcTags);
        values.put(KEY_E_NFC_DURATION, eventPreferences._duration);
        values.put(KEY_E_NFC_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_NFC_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_NFC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

        values.put(KEY_E_RADIO_SWITCH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_RADIO_SWITCH_WIFI, eventPreferences._wifi);
        values.put(KEY_E_RADIO_SWITCH_BLUETOOTH, eventPreferences._bluetooth);
        values.put(KEY_E_RADIO_SWITCH_MOBILE_DATA, eventPreferences._mobileData);
        values.put(KEY_E_RADIO_SWITCH_GPS, eventPreferences._gps);
        values.put(KEY_E_RADIO_SWITCH_NFC, eventPreferences._nfc);
        values.put(KEY_E_RADIO_SWITCH_AIRPLANE_MODE, eventPreferences._airplaneMode);
        values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

        values.put(KEY_E_ALARM_CLOCK_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ALARM_CLOCK_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_ALARM_CLOCK_DURATION, eventPreferences._duration);
        values.put(KEY_E_ALARM_CLOCK_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    int getEventStatus(Event event)
    {
        importExportLock.lock();
        try {
            int eventStatus = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_STATUS
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventStatus = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_STATUS)));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return eventStatus;
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventStatus(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                int status = event.getStatus();
                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, status);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventStatus", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventBlocked(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventBlocked", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unblockAllEvents()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLOCKED, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.unblockAllEvents", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventsStatus(@SuppressWarnings("SameParameterValue") int fromStatus,
                               @SuppressWarnings("SameParameterValue") int toStatus)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, toStatus);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(fromStatus)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateAllEventsStatus", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getEventSensorPassed(EventPreferences eventPreferences, int eventType)
    {
        if (eventPreferences._event != null) {
            importExportLock.lock();
            try {
                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                try {
                    startRunningCommand();

                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = getMyWritableDatabase();

                    String sensorPassedField = "";
                    switch (eventType) {
                        case ETYPE_BLUETOOTH:
                            sensorPassedField = KEY_E_BLUETOOTH_SENSOR_PASSED;
                            break;
                        case ETYPE_LOCATION:
                            sensorPassedField = KEY_E_LOCATION_SENSOR_PASSED;
                            break;
                        case ETYPE_MOBILE_CELLS:
                            sensorPassedField = KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                            break;
                        case ETYPE_ORIENTATION:
                            sensorPassedField = KEY_E_ORIENTATION_SENSOR_PASSED;
                            break;
                        case ETYPE_WIFI:
                            sensorPassedField = KEY_E_WIFI_SENSOR_PASSED;
                            break;
                        case ETYPE_TIME:
                            sensorPassedField = KEY_E_TIME_SENSOR_PASSED;
                            break;
                        case ETYPE_BATTERY:
                            sensorPassedField = KEY_E_BATTERY_SENSOR_PASSED;
                            break;
                        case ETYPE_CALL:
                            sensorPassedField = KEY_E_CALL_SENSOR_PASSED;
                            break;
                        case ETYPE_PERIPHERAL:
                            sensorPassedField = KEY_E_PERIPHERAL_SENSOR_PASSED;
                            break;
                        case ETYPE_CALENDAR:
                            sensorPassedField = KEY_E_CALENDAR_SENSOR_PASSED;
                            break;
                        case ETYPE_SCREEN:
                            sensorPassedField = KEY_E_SCREEN_SENSOR_PASSED;
                            break;
                        case ETYPE_SMS:
                            sensorPassedField = KEY_E_SMS_SENSOR_PASSED;
                            break;
                        case ETYPE_NOTIFICATION:
                            sensorPassedField = KEY_E_NOTIFICATION_SENSOR_PASSED;
                            break;
                        case ETYPE_APPLICATION:
                            sensorPassedField = KEY_E_APPLICATION_SENSOR_PASSED;
                            break;
                        case ETYPE_NFC:
                            sensorPassedField = KEY_E_NFC_SENSOR_PASSED;
                            break;
                        case ETYPE_RADIO_SWITCH:
                            sensorPassedField = KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                            break;
                        case ETYPE_ALARM_CLOCK:
                            sensorPassedField = KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                            break;
                    }

                    Cursor cursor = db.query(TABLE_EVENTS,
                            new String[]{
                                    sensorPassedField
                            },
                            KEY_E_ID + "=?",
                            new String[]{String.valueOf(eventPreferences._event._id)}, null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        if (cursor.getCount() > 0) {
                            sensorPassed = Integer.parseInt(cursor.getString(cursor.getColumnIndex(sensorPassedField)));
                        }

                        cursor.close();
                    }

                    //db.close();

                } catch (Exception ignored) {
                }
                return sensorPassed;
            } finally {
                stopRunningCommand();
            }
        }
        else
            return EventPreferences.SENSOR_PASSED_NOT_PASSED;
    }

    void updateEventSensorPassed(Event event, int eventType)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                String sensorPassedField = "";
                switch (eventType) {
                    case ETYPE_BLUETOOTH:
                        sensorPassed = event._eventPreferencesBluetooth.getSensorPassed();
                        sensorPassedField = KEY_E_BLUETOOTH_SENSOR_PASSED;
                        break;
                    case ETYPE_LOCATION:
                        sensorPassed = event._eventPreferencesLocation.getSensorPassed();
                        sensorPassedField = KEY_E_LOCATION_SENSOR_PASSED;
                        break;
                    case ETYPE_MOBILE_CELLS:
                        sensorPassed = event._eventPreferencesMobileCells.getSensorPassed();
                        sensorPassedField = KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                        break;
                    case ETYPE_ORIENTATION:
                        sensorPassed = event._eventPreferencesOrientation.getSensorPassed();
                        sensorPassedField = KEY_E_ORIENTATION_SENSOR_PASSED;
                        break;
                    case ETYPE_WIFI:
                        sensorPassed = event._eventPreferencesWifi.getSensorPassed();
                        sensorPassedField = KEY_E_WIFI_SENSOR_PASSED;
                        break;
                    case ETYPE_TIME:
                        sensorPassed = event._eventPreferencesTime.getSensorPassed();
                        sensorPassedField = KEY_E_TIME_SENSOR_PASSED;
                        break;
                    case ETYPE_BATTERY:
                        sensorPassed = event._eventPreferencesBattery.getSensorPassed();
                        sensorPassedField = KEY_E_BATTERY_SENSOR_PASSED;
                        break;
                    case ETYPE_CALL:
                        sensorPassed = event._eventPreferencesCall.getSensorPassed();
                        sensorPassedField = KEY_E_CALL_SENSOR_PASSED;
                        break;
                    case ETYPE_PERIPHERAL:
                        sensorPassed = event._eventPreferencesPeripherals.getSensorPassed();
                        sensorPassedField = KEY_E_PERIPHERAL_SENSOR_PASSED;
                        break;
                    case ETYPE_CALENDAR:
                        sensorPassed = event._eventPreferencesCalendar.getSensorPassed();
                        sensorPassedField = KEY_E_CALENDAR_SENSOR_PASSED;
                        break;
                    case ETYPE_SCREEN:
                        sensorPassed = event._eventPreferencesScreen.getSensorPassed();
                        sensorPassedField = KEY_E_SCREEN_SENSOR_PASSED;
                        break;
                    case ETYPE_SMS:
                        sensorPassed = event._eventPreferencesSMS.getSensorPassed();
                        sensorPassedField = KEY_E_SMS_SENSOR_PASSED;
                        break;
                    case ETYPE_NOTIFICATION:
                        sensorPassed = event._eventPreferencesNotification.getSensorPassed();
                        sensorPassedField = KEY_E_NOTIFICATION_SENSOR_PASSED;
                        break;
                    case ETYPE_APPLICATION:
                        sensorPassed = event._eventPreferencesApplication.getSensorPassed();
                        sensorPassedField = KEY_E_APPLICATION_SENSOR_PASSED;
                        break;
                    case ETYPE_NFC:
                        sensorPassed = event._eventPreferencesNFC.getSensorPassed();
                        sensorPassedField = KEY_E_NFC_SENSOR_PASSED;
                        break;
                    case ETYPE_RADIO_SWITCH:
                        sensorPassed = event._eventPreferencesRadioSwitch.getSensorPassed();
                        sensorPassedField = KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                        break;
                    case ETYPE_ALARM_CLOCK:
                        sensorPassed = event._eventPreferencesAlarmClock.getSensorPassed();
                        sensorPassedField = KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                        break;
                }
                ContentValues values = new ContentValues();
                values.put(sensorPassedField, sensorPassed);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventSensorPassed", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventSensorsPassed(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, event._eventPreferencesBluetooth.getSensorPassed());
                values.put(KEY_E_LOCATION_SENSOR_PASSED, event._eventPreferencesLocation.getSensorPassed());
                values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, event._eventPreferencesMobileCells.getSensorPassed());
                values.put(KEY_E_ORIENTATION_SENSOR_PASSED, event._eventPreferencesOrientation.getSensorPassed());
                values.put(KEY_E_WIFI_SENSOR_PASSED, event._eventPreferencesWifi.getSensorPassed());
                values.put(KEY_E_APPLICATION_SENSOR_PASSED, event._eventPreferencesApplication.getSensorPassed());
                values.put(KEY_E_BATTERY_SENSOR_PASSED, event._eventPreferencesBattery.getSensorPassed());
                values.put(KEY_E_CALENDAR_SENSOR_PASSED, event._eventPreferencesCalendar.getSensorPassed());
                values.put(KEY_E_CALL_SENSOR_PASSED, event._eventPreferencesCall.getSensorPassed());
                values.put(KEY_E_NFC_SENSOR_PASSED, event._eventPreferencesNFC.getSensorPassed());
                values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, event._eventPreferencesNotification.getSensorPassed());
                values.put(KEY_E_PERIPHERAL_SENSOR_PASSED, event._eventPreferencesPeripherals.getSensorPassed());
                values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, event._eventPreferencesRadioSwitch.getSensorPassed());
                values.put(KEY_E_SCREEN_SENSOR_PASSED, event._eventPreferencesScreen.getSensorPassed());
                values.put(KEY_E_SMS_SENSOR_PASSED, event._eventPreferencesSMS.getSensorPassed());
                values.put(KEY_E_TIME_SENSOR_PASSED, event._eventPreferencesTime.getSensorPassed());
                values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, event._eventPreferencesAlarmClock.getSensorPassed());

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventSensorPassed", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventsSensorsPassed(int sensorPassed)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_LOCATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_ORIENTATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_WIFI_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_APPLICATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_BATTERY_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_CALENDAR_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_CALL_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_NFC_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_PERIPHERAL_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_SCREEN_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_SMS_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_TIME_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, sensorPassed);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.clearAllEventsSensorPassed", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getTypeEventsCount(int eventType, boolean onlyRunning)
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String eventTypeChecked;
                if (onlyRunning)
                    eventTypeChecked = KEY_E_STATUS + "=2" + " AND ";  //  only running events
                else
                    eventTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
                if (eventType == ETYPE_TIME)
                    eventTypeChecked = eventTypeChecked + KEY_E_TIME_ENABLED + "=1";
                else if (eventType == ETYPE_BATTERY)
                    eventTypeChecked = eventTypeChecked + KEY_E_BATTERY_ENABLED + "=1";
                else if (eventType == ETYPE_CALL)
                    eventTypeChecked = eventTypeChecked + KEY_E_CALL_ENABLED + "=1";
                else if (eventType == ETYPE_PERIPHERAL)
                    eventTypeChecked = eventTypeChecked + KEY_E_PERIPHERAL_ENABLED + "=1";
                else if (eventType == ETYPE_CALENDAR)
                    eventTypeChecked = eventTypeChecked + KEY_E_CALENDAR_ENABLED + "=1";
                else if (eventType == ETYPE_WIFICONNECTED)
                    eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                            "(" + KEY_E_WIFI_CONNECTION_TYPE + "=0 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=2)";
                else if (eventType == ETYPE_WIFIINFRONT)
                    eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                            "(" + KEY_E_WIFI_CONNECTION_TYPE + "=1 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=3)";
                else if (eventType == ETYPE_SCREEN)
                    eventTypeChecked = eventTypeChecked + KEY_E_SCREEN_ENABLED + "=1";
                else if (eventType == ETYPE_BLUETOOTHCONNECTED)
                    eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                            "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=2)";
                else if (eventType == ETYPE_BLUETOOTHINFRONT)
                    eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                            "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3)";
                else if (eventType == ETYPE_SMS)
                    eventTypeChecked = eventTypeChecked + KEY_E_SMS_ENABLED + "=1";
                else if (eventType == ETYPE_NOTIFICATION)
                    eventTypeChecked = eventTypeChecked + KEY_E_NOTIFICATION_ENABLED + "=1";
                else if (eventType == ETYPE_APPLICATION)
                    eventTypeChecked = eventTypeChecked + KEY_E_APPLICATION_ENABLED + "=1";
                else if (eventType == ETYPE_LOCATION)
                    eventTypeChecked = eventTypeChecked + KEY_E_LOCATION_ENABLED + "=1";
                else if (eventType == ETYPE_ORIENTATION)
                    eventTypeChecked = eventTypeChecked + KEY_E_ORIENTATION_ENABLED + "=1";
                else if (eventType == ETYPE_MOBILE_CELLS)
                    eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1";
                else if (eventType == ETYPE_NFC)
                    eventTypeChecked = eventTypeChecked + KEY_E_NFC_ENABLED + "=1";
                else if (eventType == ETYPE_RADIO_SWITCH)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1";
                else if (eventType == ETYPE_RADIO_SWITCH_WIFI)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_WIFI + "!=0";
                else if (eventType == ETYPE_RADIO_SWITCH_BLUETOOTH)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_BLUETOOTH + "!=0";
                else if (eventType == ETYPE_RADIO_SWITCH_MOBILE_DATA)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_MOBILE_DATA + "!=0";
                else if (eventType == ETYPE_RADIO_SWITCH_GPS)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_GPS + "!=0";
                else if (eventType == ETYPE_RADIO_SWITCH_NFC)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_NFC + "!=0";
                else if (eventType == ETYPE_RADIO_SWITCH_AIRPLANE_MODE)
                    eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                            KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "!=0";
                else if (eventType == ETYPE_ALARM_CLOCK)
                    eventTypeChecked = eventTypeChecked + KEY_E_ALARM_CLOCK_ENABLED + "=1";

                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                        " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventCalendarTimes(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_CALENDAR_EVENT_START_TIME, event._eventPreferencesCalendar._startTime);
                values.put(KEY_E_CALENDAR_EVENT_END_TIME, event._eventPreferencesCalendar._endTime);
                values.put(KEY_E_CALENDAR_EVENT_FOUND, event._eventPreferencesCalendar._eventFound ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventCalendarTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void setEventCalendarTimes(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_CALENDAR_EVENT_START_TIME,
                                KEY_E_CALENDAR_EVENT_END_TIME,
                                KEY_E_CALENDAR_EVENT_FOUND
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCalendar._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_START_TIME)));
                        event._eventPreferencesCalendar._endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_END_TIME)));
                        event._eventPreferencesCalendar._eventFound = (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_CALENDAR_EVENT_FOUND))) == 1);
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean getEventInDelayStart(Event event)
    {
        importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_IS_IN_DELAY_START
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_START)));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return (eventInDelay == 1);
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventInDelayStart(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventInDelayStart", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void resetAllEventsInDelayStart()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_START, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.resetAllEventsInDelayStart", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean getEventInDelayEnd(Event event)
    {
        importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_IS_IN_DELAY_END
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_E_IS_IN_DELAY_END)));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return (eventInDelay == 1);
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventInDelayEnd(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEventInDelayEnd", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateSMSStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_SMS_START_TIME, event._eventPreferencesSMS._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateSMSStartTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getSMSStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_SMS_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesSMS._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_SMS_START_TIME)));
                        //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                        //    PPApplication.logE("[SMS sensor] DatabaseHandler.getSMSStartTime", "startTime="+event._eventPreferencesSMS._startTime);
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    void updateNotificationStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NOTIFICATION_START_TIME, event._eventPreferencesNotification._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateNotificationStartTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getNotificationStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_NOTIFICATION_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesNotification._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_NOTIFICATION_START_TIME)));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }
    */

    int getBluetoothDevicesTypeCount(int devicesType, int forceScan)
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                if (forceScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    final String countQuery;
                    String devicesTypeChecked;
                    devicesTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
                    devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND ";
                    devicesTypeChecked = devicesTypeChecked + "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3) AND ";
                    if (devicesType == EventPreferencesBluetooth.DTYPE_CLASSIC)
                        devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_DEVICES_TYPE + "=0";
                    else if (devicesType == EventPreferencesBluetooth.DTYPE_LE)
                        devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_DEVICES_TYPE + "=1";

                    countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                            " WHERE " + devicesTypeChecked;

                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = getMyWritableDatabase();

                    Cursor cursor = db.rawQuery(countQuery, null);

                    if (cursor != null) {
                        cursor.moveToFirst();
                        r = Integer.parseInt(cursor.getString(0));
                        cursor.close();
                    }

                    //db.close();

                } else
                    r = 999;
            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void updateNFCStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NFC_START_TIME, event._eventPreferencesNFC._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateNFCStartTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getNFCStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_NFC_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesNFC._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_NFC_START_TIME)));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getBatteryEventWithLevelCount()
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String eventChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
                eventChecked = eventChecked + KEY_E_BATTERY_ENABLED + "=1" + " AND ";
                eventChecked = eventChecked + "(" + KEY_E_BATTERY_LEVEL_LOW + ">0" + " OR ";
                eventChecked = eventChecked + KEY_E_BATTERY_LEVEL_HIGHT + "<100" + ")";

                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                        " WHERE " + eventChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void updateCallStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_CALL_START_TIME, event._eventPreferencesCall._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateCallStartTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getCallStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_CALL_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCall._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_CALL_START_TIME)));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAlarmClockStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_ALARM_CLOCK_START_TIME, event._eventPreferencesAlarmClock._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateAlarmClockStartTime", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getAlarmClockStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_ALARM_CLOCK_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesAlarmClock._startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_E_ALARM_CLOCK_START_TIME)));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

// EVENT TIMELINE ------------------------------------------------------------------

    // Adding time line
    void addEventTimeline(EventTimeline eventTimeline) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_ET_FK_EVENT, eventTimeline._fkEvent); // Event id
                values.put(KEY_ET_FK_PROFILE_RETURN, eventTimeline._fkProfileEndActivated); // Profile id returned on pause/stop event
                values.put(KEY_ET_EORDER, getMaxEOrderET() + 1); // event running order

                db.beginTransaction();

                try {
                    // Inserting Row
                    eventTimeline._id = db.insert(TABLE_EVENT_TIMELINE, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting max(eorder)
    private int getMaxEOrderET() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT MAX(" + KEY_ET_EORDER + ") FROM " + TABLE_EVENT_TIMELINE;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting all event timeline
    List<EventTimeline> getAllEventTimelines() {
        importExportLock.lock();
        try {
            List<EventTimeline> eventTimelineList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_ET_ID + "," +
                        KEY_ET_FK_EVENT + "," +
                        KEY_ET_FK_PROFILE_RETURN + "," +
                        KEY_ET_EORDER +
                        " FROM " + TABLE_EVENT_TIMELINE +
                        " ORDER BY " + KEY_ET_EORDER;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        EventTimeline eventTimeline = new EventTimeline();

                        eventTimeline._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ET_ID)));
                        eventTimeline._fkEvent = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ET_FK_EVENT)));
                        eventTimeline._fkProfileEndActivated = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ET_FK_PROFILE_RETURN)));
                        eventTimeline._eorder = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ET_EORDER)));

                        // Adding event timeline to list
                        eventTimelineList.add(eventTimeline);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return eventTimelineList;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting event timeline
    void deleteEventTimeline(EventTimeline eventTimeline) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENT_TIMELINE, KEY_ET_ID + " = ?",
                        new String[]{String.valueOf(eventTimeline._id)});
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting all events from timeline
    void deleteAllEventTimelines(/*boolean updateEventStatus*/) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, Event.ESTATUS_PAUSE);

                db.beginTransaction();

                try {

                    db.delete(TABLE_EVENT_TIMELINE, null, null);

                    //if (updateEventStatus) {
                    db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(Event.ESTATUS_RUNNING)});
                    //}

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteAllEventTimelines", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

// ACTIVITY LOG -------------------------------------------------------------------

    // Adding activity log
    void addActivityLog(int deleteOldActivityLogs, int logType, String eventName, String profileName, String profileIcon,
                        int durationDelay) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_AL_LOG_TYPE, logType);
                values.put(KEY_AL_EVENT_NAME, eventName);
                values.put(KEY_AL_PROFILE_NAME, profileName);
                values.put(KEY_AL_PROFILE_ICON, profileIcon);
                if (durationDelay > 0)
                    values.put(KEY_AL_DURATION_DELAY, durationDelay);

                db.beginTransaction();

                try {
                    if (deleteOldActivityLogs > 0) {
                        // delete older than 7 days old records
                        db.delete(TABLE_ACTIVITY_LOG, KEY_AL_LOG_DATE_TIME +
                                " < date('now','-" + deleteOldActivityLogs + " days')", null);
                    }

                    // Inserting Row
                    db.insert(TABLE_ACTIVITY_LOG, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void clearActivityLog() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    db.delete(TABLE_ACTIVITY_LOG, null, null);

                    // db.setTransactionSuccessful();
                } catch (Exception ignored) {
                    //Error in between database transaction
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    Cursor getActivityLogCursor() {
        importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                startRunningCommand();

                final String selectQuery = "SELECT " + KEY_AL_ID + "," +
                        KEY_AL_LOG_DATE_TIME + "," +
                        KEY_AL_LOG_TYPE + "," +
                        KEY_AL_EVENT_NAME + "," +
                        KEY_AL_PROFILE_NAME + "," +
                        KEY_AL_PROFILE_ICON + "," +
                        KEY_AL_DURATION_DELAY +
                        " FROM " + TABLE_ACTIVITY_LOG +
                        " ORDER BY " + KEY_AL_ID + " DESC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception ignored) {
            }
            return cursor;
        } finally {
            stopRunningCommand();
        }
    }

// GEOFENCES ----------------------------------------------------------------------

    // Adding new geofence
    void addGeofence(Geofence geofence) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_G_NAME, geofence._name); // geofence Name
                values.put(KEY_G_LATITUDE, geofence._latitude);
                values.put(KEY_G_LONGITUDE, geofence._longitude);
                values.put(KEY_G_RADIUS, geofence._radius);
                values.put(KEY_G_CHECKED, 0);
                values.put(KEY_G_TRANSITION, 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    geofence._id = db.insert(TABLE_GEOFENCES, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single geofence
    Geofence getGeofence(long geofenceId) {
        importExportLock.lock();
        try {
            Geofence geofence = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_GEOFENCES,
                        new String[]{KEY_G_ID,
                                KEY_G_NAME,
                                KEY_G_LATITUDE,
                                KEY_G_LONGITUDE,
                                KEY_G_RADIUS
                        },
                        KEY_G_ID + "=?",
                        new String[]{String.valueOf(geofenceId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        geofence = new Geofence();
                        geofence._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_G_ID)));
                        geofence._name = cursor.getString(cursor.getColumnIndex(KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndex(KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndex(KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndex(KEY_G_RADIUS));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return geofence;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All geofences
    List<Geofence> getAllGeofences() {
        importExportLock.lock();
        try {
            List<Geofence> geofenceList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_G_ID + "," +
                        KEY_G_NAME + "," +
                        KEY_G_LATITUDE + "," +
                        KEY_G_LONGITUDE + "," +
                        KEY_G_RADIUS +
                        " FROM " + TABLE_GEOFENCES +
                        " ORDER BY " + KEY_G_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Geofence geofence = new Geofence();
                        geofence._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_G_ID)));
                        geofence._name = cursor.getString(cursor.getColumnIndex(KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndex(KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndex(KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndex(KEY_G_RADIUS));
                        geofenceList.add(geofence);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return geofenceList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single geofence
    void updateGeofence(Geofence geofence) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_G_NAME, geofence._name);
                values.put(KEY_G_LATITUDE, geofence._latitude);
                values.put(KEY_G_LONGITUDE, geofence._longitude);
                values.put(KEY_G_RADIUS, geofence._radius);
                values.put(KEY_G_CHECKED, 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofence._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateEvent", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateGeofenceTransition(long geofenceId, int geofenceTransition) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_G_TRANSITION, geofenceTransition);
                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{String.valueOf(geofenceId)});

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateGeofenceTransition", Log.getStackTraceString(e));
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void clearAllGeofenceTransitions() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_G_TRANSITION, 0);
                    db.update(TABLE_GEOFENCES, values, null, null);

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.clearAllGeofenceTransitions", Log.getStackTraceString(e));
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single geofence
    void deleteGeofence(long geofenceId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_LOCATION_GEOFENCES +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                //noinspection TryFinallyCanBeTryWithResources
                try {

                    // delete geofence
                    db.delete(TABLE_GEOFENCES, KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofenceId)});

                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            String geofences = cursor.getString(cursor.getColumnIndex(KEY_E_LOCATION_GEOFENCES));
                            String[] splits = geofences.split("\\|");
                            boolean found = false;
                            geofences = "";
                            for (String geofence : splits) {
                                if (!geofence.isEmpty()) {
                                    if (!geofence.equals(Long.toString(geofenceId))) {
                                        if (!geofences.isEmpty())
                                            //noinspection StringConcatenationInLoop
                                            geofences = geofences + "|";
                                        //noinspection StringConcatenationInLoop
                                        geofences = geofences + geofence;
                                    } else
                                        found = true;
                                }
                            }
                            if (found) {
                                // unlink geofence from events
                                ContentValues values = new ContentValues();
                                values.put(KEY_E_LOCATION_GEOFENCES, geofences);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{String.valueOf(cursor.getString(cursor.getColumnIndex(KEY_E_ID)))});
                            }
                        } while (cursor.moveToNext());
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteGeofence", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void checkGeofence(String geofences, int check) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();

                try {
                    if (!geofences.isEmpty()) {
                        // check geofences
                        String[] splits = geofences.split("\\|");
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                int _check = check;
                                if (check == 2) {
                                    final String selectQuery = "SELECT " + KEY_G_CHECKED +
                                            " FROM " + TABLE_GEOFENCES +
                                            " WHERE " + KEY_G_ID + "=" + geofence;
                                    Cursor cursor = db.rawQuery(selectQuery, null);
                                    if (cursor != null) {
                                        if (cursor.moveToFirst())
                                            _check = (cursor.getInt(cursor.getColumnIndex(KEY_G_CHECKED)) == 0) ? 1 : 0;
                                        cursor.close();
                                    }
                                }
                                if (_check != 2) {
                                    values.clear();
                                    values.put(KEY_G_CHECKED, _check);
                                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{geofence});
                                }
                            }
                        }
                    } else {
                        // uncheck geofences
                        values.clear();
                        values.put(KEY_G_CHECKED, 0);
                        db.update(TABLE_GEOFENCES, values, null, null);
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.checkGeofence", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    Cursor getGeofencesCursor() {
        importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                startRunningCommand();

                final String selectQuery = "SELECT " + KEY_G_ID + "," +
                        KEY_G_LATITUDE + "," +
                        KEY_G_LONGITUDE + "," +
                        KEY_G_RADIUS + "," +
                        KEY_G_NAME + "," +
                        KEY_G_CHECKED +
                        " FROM " + TABLE_GEOFENCES +
                        " ORDER BY " + KEY_G_NAME + " ASC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception ignored) {
            }
            return cursor;
        } finally {
            stopRunningCommand();
        }
    }

    String getGeofenceName(long geofenceId) {
        importExportLock.lock();
        try {
            String r = "";
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_NAME +
                        " FROM " + TABLE_GEOFENCES +
                        " WHERE " + KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getString(cursor.getColumnIndex(KEY_G_NAME));
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    String getCheckedGeofences() {
        importExportLock.lock();
        try {
            String value = "";
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_ID + ","
                        + KEY_G_CHECKED +
                        " FROM " + TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getInt(cursor.getColumnIndex(KEY_G_CHECKED)) == 1) {
                                if (!value.isEmpty())
                                    //noinspection StringConcatenationInLoop
                                    value = value + "|";
                                //noinspection StringConcatenationInLoop
                                value = value + cursor.getLong(cursor.getColumnIndex(KEY_G_ID));
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return value;
        } finally {
            stopRunningCommand();
        }
    }

    int getGeofenceCount() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT  count(*) FROM " + TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    boolean isGeofenceUsed(long geofenceId/*, boolean onlyEnabledEvents*/) {
        importExportLock.lock();
        try {
            boolean found = false;
            try {
                startRunningCommand();

                String selectQuery = "SELECT " + KEY_E_LOCATION_GEOFENCES +
                        " FROM " + TABLE_EVENTS +
                        " WHERE " + KEY_E_LOCATION_ENABLED + "=1";

                /*
                if (onlyEnabledEvents)
                    selectQuery = selectQuery + " AND " + KEY_E_STATUS + " IN (" +
                            String.valueOf(Event.ESTATUS_PAUSE) + "," +
                            String.valueOf(Event.ESTATUS_RUNNING) + ")";
                */

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String geofences = cursor.getString(cursor.getColumnIndex(KEY_E_LOCATION_GEOFENCES));
                        String[] splits = geofences.split("\\|");
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                if (geofence.equals(Long.toString(geofenceId))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found)
                            break;
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return found;
        } finally {
            stopRunningCommand();
        }
    }

    int getGeofenceTransition(long geofenceId) {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_TRANSITION +
                        " FROM " + TABLE_GEOFENCES +
                        " WHERE " + KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getInt(cursor.getColumnIndex(KEY_G_TRANSITION));
                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    void addShortcut(Shortcut shortcut) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_S_INTENT, shortcut._intent);
                values.put(KEY_S_NAME, shortcut._name);

                db.beginTransaction();

                try {
                    // Inserting Row
                    shortcut._id = db.insert(TABLE_SHORTCUTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single shortcut
    Shortcut getShortcut(long shortcutId) {
        importExportLock.lock();
        try {
            Shortcut shortcut = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_SHORTCUTS,
                        new String[]{KEY_S_ID,
                                KEY_S_INTENT,
                                KEY_S_NAME
                        },
                        KEY_S_ID + "=?",
                        new String[]{String.valueOf(shortcutId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        shortcut = new Shortcut();
                        shortcut._id = Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_S_ID)));
                        shortcut._intent = cursor.getString(cursor.getColumnIndex(KEY_S_INTENT));
                        shortcut._name = cursor.getString(cursor.getColumnIndex(KEY_S_NAME));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return shortcut;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single shortcut
    void deleteShortcut(long shortcutId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {

                    // delete geofence
                    db.delete(TABLE_SHORTCUTS, KEY_S_ID + " = ?",
                            new String[]{String.valueOf(shortcutId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteShortcut", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

// MOBILE_CELLS ----------------------------------------------------------------------

    // Adding new mobile cell
    private void addMobileCell(MobileCell mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(KEY_MC_NAME, mobileCell._name);
                values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);

                db.beginTransaction();

                try {
                    // Inserting Row
                    mobileCell._id = db.insert(TABLE_MOBILE_CELLS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single mobile cell
    private void updateMobileCell(MobileCell mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(KEY_MC_NAME, mobileCell._name);
                values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_MOBILE_CELLS, values, KEY_MC_ID + " = ?",
                            new String[]{String.valueOf(mobileCell._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateMobileCell", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // add mobile cells to list
    void addMobileCellsToList(List<MobileCellsData> cellsList/*, boolean onlyNew*/) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " + KEY_MC_CELL_ID + "," +
                        KEY_MC_NAME + "," +
                        KEY_MC_NEW + "," +
                        KEY_MC_LAST_CONNECTED_TIME +
                        " FROM " + TABLE_MOBILE_CELLS;

                /*if (onlyNew) {
                    selectQuery = selectQuery +
                            " WHERE " + KEY_MC_NEW + "=1";
                }*/

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        int cellId = cursor.getInt(cursor.getColumnIndex(KEY_MC_CELL_ID));
                        String name = cursor.getString(cursor.getColumnIndex(KEY_MC_NAME));
                        boolean _new = cursor.getInt(cursor.getColumnIndex(KEY_MC_NEW)) == 1;
                        long lastConnectedTime = cursor.getLong(cursor.getColumnIndex(KEY_MC_LAST_CONNECTED_TIME));
                        //Log.d("DatabaseHandler.addMobileCellsToList", "cellId="+cellId + " new="+_new);
                        boolean found = false;
                        for (MobileCellsData cell : cellsList) {
                            if (cell.cellId == cellId) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            MobileCellsData cell = new MobileCellsData(cellId, name, false, _new, lastConnectedTime);
                            cellsList.add(cell);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void saveMobileCellsList(List<MobileCellsData> cellsList, boolean _new, boolean renameExistingCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_ID + "," +
                        KEY_MC_CELL_ID + "," +
                        KEY_MC_NAME + "," +
                        KEY_MC_LAST_CONNECTED_TIME +
                        " FROM " + TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    String foundedCellName = "";
                    long foundedLastConnectedTime = 0;
                    if (cursor.moveToFirst()) {
                        do {
                            String dbCellId = Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_MC_CELL_ID)));
                            if (dbCellId.equals(Integer.toString(cell.cellId))) {
                                foundedDbId = cursor.getLong(cursor.getColumnIndex(KEY_MC_ID));
                                foundedCellName = cursor.getString(cursor.getColumnIndex(KEY_MC_NAME));
                                foundedLastConnectedTime = cursor.getLong(cursor.getColumnIndex(KEY_MC_LAST_CONNECTED_TIME));
                                found = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    if (!found) {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "!found");
                        MobileCell mobileCell = new MobileCell();
                        mobileCell._cellId = cell.cellId;
                        mobileCell._name = cell.name;
                        mobileCell._new = true;
                        mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        addMobileCell(mobileCell);
                    } else {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "found="+foundedDbId+" cell.new="+cell._new+" new="+_new);
                        MobileCell mobileCell = new MobileCell();
                        mobileCell._id = foundedDbId;
                        mobileCell._cellId = cell.cellId;
                        mobileCell._name = cell.name;
                        if (!renameExistingCell && !foundedCellName.isEmpty())
                            mobileCell._name = foundedCellName;
                        mobileCell._new = _new && cell._new;
                        if (cell.connected)
                            mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        else
                            mobileCell._lastConnectedTime = foundedLastConnectedTime;
                        updateMobileCell(mobileCell);
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void renameMobileCellsList(List<MobileCellsData> cellsList, String name, boolean _new, String value) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_ID + "," +
                        KEY_MC_CELL_ID +
                        " FROM " + TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    if (cursor.moveToFirst()) {
                        do {
                            String dbCellId = Integer.toString(cursor.getInt(cursor.getColumnIndex(KEY_MC_CELL_ID)));
                            if (dbCellId.equals(Integer.toString(cell.cellId))) {
                                foundedDbId = cursor.getLong(cursor.getColumnIndex(KEY_MC_ID));
                                found = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    if (found) {
                        if (_new) {
                            // change news
                            if (cell._new) {
                                cell.name = name;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._name = cell.name;
                                mobileCell._new = true;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                updateMobileCell(mobileCell);
                            }
                        } else {
                            if (value != null) {
                                // change selected
                                String[] splits = value.split("\\|");
                                for (String valueCell : splits) {
                                    if (valueCell.equals(Integer.toString(cell.cellId))) {
                                        cell.name = name;
                                        MobileCell mobileCell = new MobileCell();
                                        mobileCell._id = foundedDbId;
                                        mobileCell._cellId = cell.cellId;
                                        mobileCell._name = cell.name;
                                        mobileCell._new = cell._new;
                                        mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                        updateMobileCell(mobileCell);
                                    }
                                }
                            }
                            else {
                                // change all
                                cell.name = name;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._name = cell.name;
                                mobileCell._new = cell._new;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                updateMobileCell(mobileCell);
                            }
                        }
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void deleteMobileCell(int mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_MOBILE_CELLS, KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteMobileCell", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateMobileCellLastConnectedTime(int mobileCell, long lastConnectedTime) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_LAST_CONNECTED_TIME, lastConnectedTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_MOBILE_CELLS, values, KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateMobileCellLastConnectedTime", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void addMobileCellNamesToList(List<String> cellNamesList) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_NAME +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_NAME + " IS NOT NULL" +
                        " AND " + KEY_MC_NAME + " <> ''" +
                        " GROUP BY " + KEY_MC_NAME +
                        " ORDER BY " + KEY_MC_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(cursor.getColumnIndex(KEY_MC_NAME));
                        cellNamesList.add(name);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getNewMobileCellsCount() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_NEW + "=1";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single event
    void updateMobileCellsCells(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

                values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[] { String.valueOf(event._id) });

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateMobileCellsCells", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean isMobileCellSaved(int mobileCell) {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_CELL_ID + "=" + mobileCell;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = Integer.parseInt(cursor.getString(0));
                    cursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
            return r > 0;
        } finally {
            stopRunningCommand();
        }
    }

// NFC_TAGS ----------------------------------------------------------------------

    // Adding new nfc tag
    void addNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NT_UID, tag._uid);
                values.put(KEY_NT_NAME, tag._name);

                db.beginTransaction();

                try {
                    // Inserting Row
                    db.insert(TABLE_NFC_TAGS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All nfc tags
    List<NFCTag> getAllNFCTags() {
        importExportLock.lock();
        try {
            List<NFCTag> nfcTagList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_NT_ID + "," +
                        KEY_NT_UID + ", " +
                        KEY_NT_NAME +
                        " FROM " + TABLE_NFC_TAGS +
                        " ORDER BY " + KEY_NT_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        NFCTag nfcTag = new NFCTag(
                            Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_NT_ID))),
                            cursor.getString(cursor.getColumnIndex(KEY_NT_NAME)),
                            cursor.getString(cursor.getColumnIndex(KEY_NT_UID)));
                        nfcTagList.add(nfcTag);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return nfcTagList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single nfc tag
    void updateNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NT_UID, tag._uid);
                values.put(KEY_NT_NAME, tag._name);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_NFC_TAGS, values, KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateNFCTag", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single nfc tag
    void deleteNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_NFC_TAGS, KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteNFCTag", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    String getNFCTagNameByUid(String uid){
        importExportLock.lock();
        try {
            String tagName = "";
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_NT_NAME +
                        " FROM " + TABLE_NFC_TAGS +
                        " WHERE " + KEY_NT_UID + "=" + uid;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    tagName = cursor.getString(cursor.getColumnIndex(KEY_NT_NAME));
                }

                cursor.close();

                //db.close();
            } catch (Exception ignored) {
            }
            return tagName;
        } finally {
            stopRunningCommand();
        }
    }
    */

// INTENTS ----------------------------------------------------------------------

    // Adding new intent
    void addIntent(PPIntent intent) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_IN_NAME, intent._name);
                values.put(KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(KEY_IN_CLASS_NAME, intent._className);
                values.put(KEY_IN_ACTION, intent._action);
                values.put(KEY_IN_DATA, intent._data);
                values.put(KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(KEY_IN_CATEGORIES, intent._categories);
                values.put(KEY_IN_FLAGS, intent._flags);
                values.put(KEY_IN_INTENT_TYPE, intent._intentType);

                values.put(KEY_IN_USED_COUNT, intent._usedCount);

                db.beginTransaction();

                try {
                    // Inserting Row
                    intent._id = db.insert(TABLE_INTENTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All intents
    List<PPIntent> getAllIntents() {
        importExportLock.lock();
        try {
            List<PPIntent> intentList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_IN_ID + "," +
                        KEY_IN_NAME + ", " +
                        KEY_IN_PACKAGE_NAME + ", " +
                        KEY_IN_CLASS_NAME + ", " +
                        KEY_IN_ACTION + ", " +
                        KEY_IN_DATA + ", " +
                        KEY_IN_MIME_TYPE + ", " +
                        KEY_IN_EXTRA_KEY_1 + ", " +
                        KEY_IN_EXTRA_VALUE_1 + ", " +
                        KEY_IN_EXTRA_TYPE_1 + ", " +
                        KEY_IN_EXTRA_KEY_2 + ", " +
                        KEY_IN_EXTRA_VALUE_2 + ", " +
                        KEY_IN_EXTRA_TYPE_2 + ", " +
                        KEY_IN_EXTRA_KEY_3 + ", " +
                        KEY_IN_EXTRA_VALUE_3 + ", " +
                        KEY_IN_EXTRA_TYPE_3 + ", " +
                        KEY_IN_EXTRA_KEY_4 + ", " +
                        KEY_IN_EXTRA_VALUE_4 + ", " +
                        KEY_IN_EXTRA_TYPE_4 + ", " +
                        KEY_IN_EXTRA_KEY_5 + ", " +
                        KEY_IN_EXTRA_VALUE_5 + ", " +
                        KEY_IN_EXTRA_TYPE_5 + ", " +
                        KEY_IN_EXTRA_KEY_6 + ", " +
                        KEY_IN_EXTRA_VALUE_6 + ", " +
                        KEY_IN_EXTRA_TYPE_6 + ", " +
                        KEY_IN_EXTRA_KEY_7 + ", " +
                        KEY_IN_EXTRA_VALUE_7 + ", " +
                        KEY_IN_EXTRA_TYPE_7 + ", " +
                        KEY_IN_EXTRA_KEY_8 + ", " +
                        KEY_IN_EXTRA_VALUE_8 + ", " +
                        KEY_IN_EXTRA_TYPE_8 + ", " +
                        KEY_IN_EXTRA_KEY_9 + ", " +
                        KEY_IN_EXTRA_VALUE_9 + ", " +
                        KEY_IN_EXTRA_TYPE_9 + ", " +
                        KEY_IN_EXTRA_KEY_10 + ", " +
                        KEY_IN_EXTRA_VALUE_10 + ", " +
                        KEY_IN_EXTRA_TYPE_10 + ", " +
                        KEY_IN_CATEGORIES + ", " +
                        KEY_IN_FLAGS + ", " +
                        KEY_IN_INTENT_TYPE + ", " +

                        KEY_IN_USED_COUNT +
                        " FROM " + TABLE_INTENTS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        PPIntent ppIntent = new PPIntent(
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_IN_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_1)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_1))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_2)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_2))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_3)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_3))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_4)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_4))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_5)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_5))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_6)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_6))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_7)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_7))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_8)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_8))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_9)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_9))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_10)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_10))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_FLAGS)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_USED_COUNT))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_INTENT_TYPE)))
                        );
                        intentList.add(ppIntent);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception ignored) {
            }
            return intentList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single intent
    void updateIntent(PPIntent intent) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_IN_NAME, intent._name);
                values.put(KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(KEY_IN_CLASS_NAME, intent._className);
                values.put(KEY_IN_ACTION, intent._action);
                values.put(KEY_IN_DATA, intent._data);
                values.put(KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(KEY_IN_CATEGORIES, intent._categories);
                values.put(KEY_IN_FLAGS, intent._flags);
                values.put(KEY_IN_INTENT_TYPE, intent._intentType);

                values.put(KEY_IN_USED_COUNT, intent._usedCount);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intent._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateIntent", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single intent
    PPIntent getIntent(long intentId) {
        importExportLock.lock();
        try {
            PPIntent intent = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_INTENTS,
                        new String[]{KEY_IN_ID,
                                KEY_IN_NAME,
                                KEY_IN_PACKAGE_NAME,
                                KEY_IN_CLASS_NAME,
                                KEY_IN_ACTION,
                                KEY_IN_DATA,
                                KEY_IN_MIME_TYPE,
                                KEY_IN_EXTRA_KEY_1,
                                KEY_IN_EXTRA_VALUE_1,
                                KEY_IN_EXTRA_TYPE_1,
                                KEY_IN_EXTRA_KEY_2,
                                KEY_IN_EXTRA_VALUE_2,
                                KEY_IN_EXTRA_TYPE_2,
                                KEY_IN_EXTRA_KEY_3,
                                KEY_IN_EXTRA_VALUE_3,
                                KEY_IN_EXTRA_TYPE_3,
                                KEY_IN_EXTRA_KEY_4,
                                KEY_IN_EXTRA_VALUE_4,
                                KEY_IN_EXTRA_TYPE_4,
                                KEY_IN_EXTRA_KEY_5,
                                KEY_IN_EXTRA_VALUE_5,
                                KEY_IN_EXTRA_TYPE_5,
                                KEY_IN_EXTRA_KEY_6,
                                KEY_IN_EXTRA_VALUE_6,
                                KEY_IN_EXTRA_TYPE_6,
                                KEY_IN_EXTRA_KEY_7,
                                KEY_IN_EXTRA_VALUE_7,
                                KEY_IN_EXTRA_TYPE_7,
                                KEY_IN_EXTRA_KEY_8,
                                KEY_IN_EXTRA_VALUE_8,
                                KEY_IN_EXTRA_TYPE_8,
                                KEY_IN_EXTRA_KEY_9,
                                KEY_IN_EXTRA_VALUE_9,
                                KEY_IN_EXTRA_TYPE_9,
                                KEY_IN_EXTRA_KEY_10,
                                KEY_IN_EXTRA_VALUE_10,
                                KEY_IN_EXTRA_TYPE_10,
                                KEY_IN_CATEGORIES,
                                KEY_IN_FLAGS,
                                KEY_IN_INTENT_TYPE,

                                KEY_IN_USED_COUNT
                        },
                        KEY_IN_ID + "=?",
                        new String[]{String.valueOf(intentId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        intent = new PPIntent(
                                Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_IN_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_1)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_1))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_2)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_2))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_3)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_3))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_4)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_4))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_5)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_5))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_6)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_6))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_7)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_7))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_8)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_8))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_9)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_9))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_VALUE_10)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_EXTRA_TYPE_10))),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndex(KEY_IN_FLAGS)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_USED_COUNT))),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_INTENT_TYPE)))
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception ignored) {
            }
            return intent;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single intent
    void deleteIntent(long intentId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_INTENTS, KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intentId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.deleteIntent", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updatePPIntentUsageCount(final List<Application> oldApplicationsList,
                                   final List<Application> applicationsList) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {

                    for (Application application : oldApplicationsList) {
                        if ((application.type == Application.TYPE_INTENT) && (application.intentId > 0)) {

                            Cursor cursor = db.query(TABLE_INTENTS,
                                    new String[]{ KEY_IN_USED_COUNT },
                                    KEY_IN_ID + "=?",
                                    new String[]{String.valueOf(application.intentId)}, null, null, null, null);

                            if (cursor != null) {
                                cursor.moveToFirst();

                                if (cursor.getCount() > 0) {
                                    int usedCount = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_USED_COUNT)));
                                    if (usedCount > 0) {
                                        --usedCount;

                                        ContentValues values = new ContentValues();
                                        values.put(KEY_IN_USED_COUNT, usedCount);
                                        db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                                                new String[]{String.valueOf(application.intentId)});

                                    }
                                }

                                cursor.close();
                            }
                        }
                    }

                    for (Application application : applicationsList) {
                        if ((application.type == Application.TYPE_INTENT) && (application.intentId > 0)) {

                            Cursor cursor = db.query(TABLE_INTENTS,
                                    new String[]{ KEY_IN_USED_COUNT },
                                    KEY_IN_ID + "=?",
                                    new String[]{String.valueOf(application.intentId)}, null, null, null, null);

                            if (cursor != null) {
                                cursor.moveToFirst();

                                if (cursor.getCount() > 0) {
                                    int usedCount = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_IN_USED_COUNT)));
                                    ++usedCount;

                                    ContentValues values = new ContentValues();
                                    values.put(KEY_IN_USED_COUNT, usedCount);
                                    db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                                            new String[]{String.valueOf(application.intentId)});
                                }

                                cursor.close();
                            }
                        }
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updatePPIntentUsageCount", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

// OTHERS -------------------------------------------------------------------------

    void disableNotAllowedPreferences()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                final String selectProfilesQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_AIRPLANE_MODE + "," +
                        KEY_DEVICE_WIFI + "," +
                        KEY_DEVICE_BLUETOOTH + "," +
                        KEY_DEVICE_MOBILE_DATA + "," +
                        KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                        KEY_DEVICE_GPS + "," +
                        KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                        KEY_DEVICE_NFC + "," +
                        KEY_VOLUME_RINGER_MODE + "," +
                        KEY_DEVICE_WIFI_AP + "," +
                        KEY_DEVICE_POWER_SAVE_MODE + "," +
                        KEY_VOLUME_ZEN_MODE + "," +
                        KEY_DEVICE_NETWORK_TYPE + "," +
                        KEY_DEVICE_NETWORK_TYPE_PREFS + "," +
                        KEY_NOTIFICATION_LED + "," +
                        KEY_VIBRATE_WHEN_RINGING + "," +
                        KEY_DEVICE_CONNECT_TO_SSID + "," +
                        KEY_APPLICATION_DISABLE_WIFI_SCANNING + "," +
                        KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "," +
                        KEY_DEVICE_WIFI_AP_PREFS + "," +
                        KEY_HEADS_UP_NOTIFICATIONS +
                        " FROM " + TABLE_PROFILES;
                final String selectEventsQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_WIFI_ENABLED + "," +
                        KEY_E_BLUETOOTH_ENABLED + "," +
                        KEY_E_NOTIFICATION_ENABLED + "," +
                        KEY_E_ORIENTATION_ENABLED + "," +
                        KEY_E_MOBILE_CELLS_ENABLED + "," +
                        KEY_E_NFC_ENABLED + "," +
                        KEY_E_RADIO_SWITCH_ENABLED +
                        " FROM " + TABLE_EVENTS;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                Cursor profilesCursor = db.rawQuery(selectProfilesQuery, null);
                Cursor eventsCursor = db.rawQuery(selectEventsQuery, null);

                db.beginTransaction();
                //noinspection TryFinallyCanBeTryWithResources
                try {

                    if (profilesCursor.moveToFirst()) {
                        do {
                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_AIRPLANE_MODE))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_AIRPLANE_MODE, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_WIFI))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_WIFI, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_BLUETOOTH))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_BLUETOOTH, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_MOBILE_DATA, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_MOBILE_DATA_PREFS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, true, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_GPS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_GPS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_LOCATION_SERVICE_PREFS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_NFC))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_NFC, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_WIFI_AP))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_WIFI_AP, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if (Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_VOLUME_RINGER_MODE))) == 5) {
                                boolean notRemove = ActivateProfileHelper.canChangeZenMode(context, true);
                                if (!notRemove) {
                                    int zenMode = profilesCursor.getInt(profilesCursor.getColumnIndex(KEY_VOLUME_ZEN_MODE));
                                    int ringerMode = 0;
                                    switch (zenMode) {
                                        case 1:
                                            ringerMode = 1;
                                            break;
                                        case 2:
                                            ringerMode = 4;
                                            break;
                                        case 3:
                                            ringerMode = 4;
                                            break;
                                        case 4:
                                            ringerMode = 2;
                                            break;
                                        case 5:
                                            ringerMode = 3;
                                            break;
                                        case 6:
                                            ringerMode = 4;
                                            break;
                                    }
                                    values.clear();
                                    values.put(KEY_VOLUME_RINGER_MODE, ringerMode);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                                }
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_POWER_SAVE_MODE))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_NOTIFICATION_LED))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_NOTIFICATION_LED, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_VIBRATE_WHEN_RINGING))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                                values.clear();
                                values.put(KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_APPLICATION_DISABLE_WIFI_SCANNING))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_WIFI_AP_PREFS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, null, false, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_WIFI_AP_PREFS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_HEADS_UP_NOTIFICATIONS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_HEADS_UP_NOTIFICATIONS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                            if ((Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_DEVICE_NETWORK_TYPE_PREFS))) != 0) &&
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, false, context).allowed
                                            == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, 0);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(profilesCursor.getString(profilesCursor.getColumnIndex(KEY_ID))))});
                            }

                        } while (profilesCursor.moveToNext());
                    }

                    //-----------------------

                    if (eventsCursor.moveToFirst()) {
                        do {
                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_WIFI_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_WIFI_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }
                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_BLUETOOTH_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_BLUETOOTH_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }
                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_NOTIFICATION_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_NOTIFICATION_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }
                            if (Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ORIENTATION_ENABLED))) != 0) {
                                SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
                                boolean hasAccelerometer = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
                                boolean hasMagneticField = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
                                boolean hasProximity = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null);

                                boolean enabled = hasAccelerometer && hasMagneticField;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_DISPLAY, "");
                                    values.put(KEY_E_ORIENTATION_SIDES, "");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                                }
                                enabled = hasAccelerometer;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_SIDES, "");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                                }
                                enabled = hasProximity;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_DISTANCE, 0);
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                                }
                            }
                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_MOBILE_CELLS_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_MOBILE_CELLS_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }

                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_NFC_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_NFC_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }

                            if ((Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_RADIO_SWITCH_ENABLED))) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_RADIO_SWITCH_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(Integer.parseInt(eventsCursor.getString(eventsCursor.getColumnIndex(KEY_E_ID))))});
                            }

                        } while (eventsCursor.moveToNext());
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.disableNotAllowedPreferences", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                    profilesCursor.close();
                    eventsCursor.close();
                }

                //db.close();
            } catch (Exception ignored) {
            }
        } finally {
            stopRunningCommand();
        }
    }

    private void startRunningImportExport() throws Exception {
        PPApplication.logE("----------- DatabaseHandler.startRunningImportExport", "lock");
        PPApplication.logE("----------- DatabaseHandler.startRunningImportExport", "runningCommand="+runningCommand);
        if (runningCommand)
            runningCommandCondition.await();
        PPApplication.logE("----------- DatabaseHandler.startRunningImportExport", "continue");
        runningImportExport = true;
    }

    private void stopRunningImportExport() {
        runningImportExport = false;
        runningImportExportCondition.signalAll();
        importExportLock.unlock();
        PPApplication.logE("----------- DatabaseHandler.stopRunningImportExport", "unlock");
    }

    private boolean tableExists(String tableName, SQLiteDatabase db)
    {
        boolean tableExists = false;

        /* get cursor on it */
        try
        {
            Cursor c = db.query(tableName, null,
                null, null, null, null, null);
            tableExists = true;
            c.close();
        }
        catch (Exception e) {
            /* not exists ? */
        }

        return tableExists;
    }

    //@SuppressWarnings("resource")
    int importDB(String applicationDataPath) {
        importExportLock.lock();
        try {
            int ret = IMPORT_ERROR_BUG;
            try {
                startRunningImportExport();

                List<Long> exportedDBEventProfileIds = new ArrayList<>();
                List<Long> importDBEventProfileIds = new ArrayList<>();
                long profileId;

                // Close SQLiteOpenHelper so it will commit the created empty
                // database to internal storage
                //close();

                try {
                    File sd = Environment.getExternalStorageDirectory();
                    //File data = Environment.getDataDirectory();

                    //File dataDB = new File(data, DB_FILEPATH + "/" + DATABASE_NAME);
                    File exportedDB = new File(sd, applicationDataPath + "/" + EXPORT_DBFILENAME);

                    if (exportedDB.exists()) {
                        SQLiteDatabase exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

                        if (exportedDBObj.getVersion() <= DATABASE_VERSION) {
                            SQLiteDatabase db = getMyWritableDatabase();

                            Cursor cursorExportedDB = null;
                            String[] columnNamesExportedDB;
                            Cursor cursorImportDB = null;
                            ContentValues values = new ContentValues();

                            try {
                                db.beginTransaction();

                                db.execSQL("DELETE FROM " + TABLE_PROFILES);

                                // cursor for profiles exportedDB
                                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_PROFILES, null);
                                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                // cursor for profiles of destination db
                                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_PROFILES, null);

                                int duration = 0;
                                int zenMode = 0;
                                int lockDevice = 0;

                                if (cursorExportedDB.moveToFirst()) {
                                    do {
                                        values.clear();
                                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                String value = cursorExportedDB.getString(i);

                                                // update values
                                                if (((exportedDBObj.getVersion() < 1002) && (applicationDataPath.equals(PPApplication.EXPORT_PATH)))
                                                        ||
                                                        ((exportedDBObj.getVersion() < 52) && (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH)))) {
                                                    if (columnNamesExportedDB[i].equals(KEY_DEVICE_AUTOROTATE)) {
                                                        // change values:
                                                        // autorotate off -> rotation 0
                                                        // autorotate on -> autorotate
                                                        if (value.equals("1") || value.equals("3"))
                                                            value = "1";
                                                        if (value.equals("2"))
                                                            value = "2";
                                                    }
                                                }
                                                if (exportedDBObj.getVersion() < 1156) {
                                                    if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS)) {
                                                        //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                                                        //{
                                                            //value|noChange|automatic|sharedProfile
                                                            String[] splits = value.split("\\|");

                                                            if (splits[2].equals("1")) // automatic is set
                                                            {
                                                                // hm, found brightness values without default profile :-/
                                                            /*if (splits.length == 4)
                                                                value = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                                                            else
                                                                value = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|0";
                                                            */
                                                                if (splits.length == 4)
                                                                    value = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                                                                else
                                                                    value = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|0";
                                                            }
                                                        //}
                                                    }
                                                }
                                                if (exportedDBObj.getVersion() < 1165) {
                                                    if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS)) {
                                                        //value|noChange|automatic|sharedProfile
                                                        String[] splits = value.split("\\|");

                                                        int percentage = Integer.parseInt(splits[0]);
                                                        percentage = (int) Profile.convertBrightnessToPercents(percentage, 255, 1);

                                                        // hm, found brightness values without default profile :-/
                                                        if (splits.length == 4)
                                                            value = percentage + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                                                        else
                                                            value = percentage + "|" + splits[1] + "|" + splits[2] + "|0";
                                                    }
                                                }
                                                /*
                                                if (exportedDBObj.getVersion() < 1175) {
                                                    if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS)) {
                                                        if (android.os.Build.VERSION.SDK_INT < 21) {
                                                            //value|noChange|automatic|sharedProfile
                                                            String[] splits = value.split("\\|");

                                                            if (splits[2].equals("1")) // automatic is set
                                                            {
                                                                int percentage = 50;

                                                                // hm, found brightness values without default profile :-/
                                                                if (splits.length == 4)
                                                                    value = percentage + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                                                                else
                                                                    value = percentage + "|" + splits[1] + "|" + splits[2] + "|0";
                                                            }
                                                        }
                                                    }
                                                }
                                                */

                                                values.put(columnNamesExportedDB[i], value);
                                            }
                                            if (columnNamesExportedDB[i].equals(KEY_DURATION))
                                                duration = cursorExportedDB.getInt(i);
                                            if (columnNamesExportedDB[i].equals(KEY_VOLUME_ZEN_MODE))
                                                zenMode = cursorExportedDB.getInt(i);
                                            if (columnNamesExportedDB[i].equals(KEY_LOCK_DEVICE))
                                                lockDevice = cursorExportedDB.getInt(i);
                                        }

                                        // for non existent fields set default value
                                        if (exportedDBObj.getVersion() < 19) {
                                            values.put(KEY_DEVICE_MOBILE_DATA, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 20) {
                                            values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 21) {
                                            values.put(KEY_DEVICE_GPS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 22) {
                                            values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, 0);
                                            values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
                                        }
                                        if (exportedDBObj.getVersion() < 24) {
                                            values.put(KEY_DEVICE_AUTOSYNC, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 31) {
                                            values.put(KEY_DEVICE_AUTOSYNC, 0);
                                        }
                                        if (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH)
                                                ||
                                                ((exportedDBObj.getVersion() < 26) && (applicationDataPath.equals(PPApplication.EXPORT_PATH)))) {
                                            values.put(KEY_SHOW_IN_ACTIVATOR, 1);
                                        }
                                        if (((exportedDBObj.getVersion() < 1001) && (applicationDataPath.equals(PPApplication.EXPORT_PATH)))
                                                ||
                                                ((exportedDBObj.getVersion() < 51) && (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH)))) {
                                            values.put(KEY_DEVICE_AUTOROTATE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1015) {
                                            values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1020) {
                                            values.put(KEY_VOLUME_SPEAKER_PHONE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1035) {
                                            values.put(KEY_DEVICE_NFC, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1120) {
                                            values.put(KEY_DURATION, 0);
                                            values.put(KEY_AFTER_DURATION_DO, Profile.AFTERDURATIONDO_RESTARTEVENTS);
                                        }
                                        if (exportedDBObj.getVersion() < 1150) {
                                            values.put(KEY_VOLUME_ZEN_MODE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1160) {
                                            values.put(KEY_DEVICE_KEYGUARD, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1210) {
                                            values.put(KEY_VIBRATE_ON_TOUCH, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1330) {
                                            values.put(KEY_DEVICE_WIFI_AP, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1350) {
                                            values.put(KEY_DURATION, duration * 60); // conversion to seconds
                                        }
                                        if (exportedDBObj.getVersion() < 1410) {
                                            if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23))
                                                values.put(KEY_VOLUME_ZEN_MODE, 3); // Alarms only zen mode is supported from Android 6.0
                                        }
                                        if (exportedDBObj.getVersion() < 1420) {
                                            values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1560) {
                                            values.put(KEY_ASK_FOR_DURATION, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1570) {
                                            values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1580) {
                                            values.put(KEY_NOTIFICATION_LED, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1630) {
                                            values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1660) {
                                            values.put(KEY_DEVICE_WALLPAPER_FOR, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1810) {
                                            values.put(KEY_HIDE_STATUS_BAR_ICON, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1820) {
                                            values.put(KEY_LOCK_DEVICE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1860) {
                                            values.put(KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                                        }
                                        if (exportedDBObj.getVersion() < 1870) {
                                            values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, 0);
                                            values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1950) {
                                            values.put(KEY_DURATION_NOTIFICATION_SOUND, "");
                                            values.put(KEY_DURATION_NOTIFICATION_VIBRATE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 1980) {
                                            values.put(KEY_DEVICE_WIFI_AP_PREFS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2010) {
                                            values.put(KEY_APPLICATION_DISABLE_LOCATION_SCANNING, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2020) {
                                            values.put(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, 0);
                                            values.put(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2030) {
                                            values.put(KEY_HEADS_UP_NOTIFICATIONS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2050) {
                                            values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, 0);
                                            values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
                                        }
                                        if (exportedDBObj.getVersion() < 2060) {
                                            values.put(KEY_ACTIVATION_BY_USER_COUNT, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2070) {
                                            values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2080) {
                                            values.put(KEY_DEVICE_CLOSE_ALL_APPLICATIONS, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2090) {
                                            values.put(KEY_SCREEN_NIGHT_MODE, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2100) {
                                            values.put(KEY_DTMF_TONE_WHEN_DIALING, 0);
                                            values.put(KEY_SOUND_ON_TOUCH, 0);
                                        }
                                        if (exportedDBObj.getVersion() < 2150) {
                                            if (lockDevice == 3)
                                                values.put(KEY_LOCK_DEVICE, 1);
                                        }

                                        ///////////////////////////////////////////////////////

                                        // Inserting Row do db z SQLiteOpenHelper
                                        profileId = db.insert(TABLE_PROFILES, null, values);
                                        // save profile ids
                                        exportedDBEventProfileIds.add(cursorExportedDB.getLong(cursorExportedDB.getColumnIndex(KEY_ID)));
                                        importDBEventProfileIds.add(profileId);

                                    } while (cursorExportedDB.moveToNext());
                                }

                                if (exportedDBObj.getVersion() < 1890) {
                                    changePictureFilePathToUri(null/*, false*/);
                                }

                                cursorExportedDB.close();
                                cursorImportDB.close();

                                db.execSQL("DELETE FROM " + TABLE_EVENTS);

                                int exportedDBObjVersion = 0;

                                if (tableExists(TABLE_EVENTS, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

                                    int batteryLevel = 15;
                                    //int batteryDetectorType = 0;
                                    long fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
                                    long startTime = 0;
                                    long endTime = 0;
                                    int priority = 0;
                                    int delayStart = 0;
                                    int useEndTime = 0;
                                    int undoneProfile = 0;
                                    long geofenceId = 0;
                                    String calendarSearchString = "";
                                    String wifiSSID = "";
                                    String bluetoothAdapterName = "";
                                    int notificationRepeatInterval = 0;
                                    long fkProfileStartWhenActivated = Profile.PROFILE_NO_ACTIVATE;

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START) ||
                                                            columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) ||
                                                            columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) ||
                                                            columnNamesExportedDB[i].equals(KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                                        // imported profile has new id
                                                        // map old profile id to new imported id
                                                        if (columnNamesExportedDB[i].equals(KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                                            String fkProfiles = cursorExportedDB.getString(i);
                                                            if (!fkProfiles.isEmpty()) {
                                                                String[] splits = fkProfiles.split("\\|");
                                                                StringBuilder newFkProfiles = new StringBuilder();
                                                                for (String split : splits) {
                                                                    long fkProfile = Long.valueOf(split);
                                                                    int profileIdx = exportedDBEventProfileIds.indexOf(fkProfile);
                                                                    if (profileIdx != -1) {
                                                                        if (newFkProfiles.length() > 0)
                                                                            newFkProfiles.append("|");
                                                                        newFkProfiles.append(importDBEventProfileIds.get(profileIdx));
                                                                    }
                                                                }
                                                                values.put(columnNamesExportedDB[i], newFkProfiles.toString());
                                                            }
                                                            else
                                                                values.put(columnNamesExportedDB[i], "");
                                                        }
                                                        else {
                                                            int profileIdx = exportedDBEventProfileIds.indexOf(cursorExportedDB.getLong(i));
                                                            if (profileIdx != -1)
                                                                values.put(columnNamesExportedDB[i], importDBEventProfileIds.get(profileIdx));
                                                            else {
                                                                if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) &&
                                                                        (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                                    values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                                                else if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) &&
                                                                        (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                                    values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                                                else
                                                                    values.put(columnNamesExportedDB[i], 0);
                                                            }
                                                        }
                                                    } else
                                                        values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }

                                                if (columnNamesExportedDB[i].equals(KEY_E_BATTERY_LEVEL))
                                                    batteryLevel = cursorExportedDB.getInt(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END))
                                                    fkProfileEnd = cursorExportedDB.getLong(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_START_TIME))
                                                    startTime = cursorExportedDB.getLong(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_END_TIME))
                                                    endTime = cursorExportedDB.getLong(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_PRIORITY))
                                                    priority = cursorExportedDB.getInt(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_DELAY_START))
                                                    delayStart = cursorExportedDB.getInt(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_USE_END_TIME))
                                                    useEndTime = cursorExportedDB.getInt(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_UNDONE_PROFILE)) {
                                                    if (cursorExportedDB.isNull(i))
                                                        undoneProfile = 0;
                                                    else
                                                        undoneProfile = cursorExportedDB.getInt(i);
                                                }
                                                if (columnNamesExportedDB[i].equals(KEY_E_CALENDAR_SEARCH_STRING))
                                                    calendarSearchString = cursorExportedDB.getString(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_WIFI_SSID))
                                                    wifiSSID = cursorExportedDB.getString(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_BLUETOOTH_ADAPTER_NAME))
                                                    bluetoothAdapterName = cursorExportedDB.getString(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_LOCATION_FK_GEOFENCE))
                                                    geofenceId = cursorExportedDB.getLong(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START))
                                                    notificationRepeatInterval = cursorExportedDB.getInt(i);
                                                if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED))
                                                    fkProfileStartWhenActivated = cursorExportedDB.getLong(i);
                                            }

                                            // for non existent fields set default value
                                            if (exportedDBObj.getVersion() < 30) {
                                                values.put(KEY_E_USE_END_TIME, 0);
                                            }
                                            if (exportedDBObj.getVersion() < 32) {
                                                values.put(KEY_E_STATUS, 0);
                                            }
                                            if (exportedDBObj.getVersion() < 1016) {
                                                values.put(KEY_E_BATTERY_LEVEL, 15);
                                            }

                                            if (exportedDBObj.getVersion() < 1022) {
                                                values.put(KEY_E_NOTIFICATION_SOUND_START, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1023) {
                                                values.put(KEY_E_BATTERY_LEVEL_LOW, 0);
                                                //values.put(KEY_E_BATTERY_LEVEL_HIGHT, 100);
                                                values.put(KEY_E_BATTERY_CHARGING, 0);
                                                //if (batteryDetectorType == 0)
                                                    values.put(KEY_E_BATTERY_LEVEL_HIGHT, batteryLevel);
                                                //if (batteryDetectorType == 1)
                                                //    values.put(KEY_E_BATTERY_LEVEL_LOW, batteryLevel);
                                                //if (batteryDetectorType == 2)
                                                //    values.put(KEY_E_BATTERY_CHARGING, 1);
                                            }

                                            if (exportedDBObj.getVersion() < 1030) {
                                                values.put(KEY_E_TIME_ENABLED, 0);
                                                values.put(KEY_E_BATTERY_ENABLED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1040) {
                                                values.put(KEY_E_CALL_ENABLED, 0);
                                                values.put(KEY_E_CALL_EVENT, 0);
                                                values.put(KEY_E_CALL_CONTACTS, "");
                                                values.put(KEY_E_CALL_CONTACT_LIST_TYPE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1045) {
                                                values.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                                            }

                                            if (exportedDBObj.getVersion() < 1050) {
                                                values.put(KEY_E_FORCE_RUN, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1051) {
                                                values.put(KEY_E_BLOCKED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1060) {
                                                if (fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                                                    values.put(KEY_E_UNDONE_PROFILE, 1);
                                                else
                                                    values.put(KEY_E_UNDONE_PROFILE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1070) {
                                                values.put(KEY_E_PRIORITY, Event.EPRIORITY_MEDIUM);
                                            }

                                            if (exportedDBObj.getVersion() < 1080) {
                                                values.put(KEY_E_PERIPHERAL_ENABLED, 0);
                                                values.put(KEY_E_PERIPHERAL_TYPE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1081) {
                                                int gmtOffset = TimeZone.getDefault().getRawOffset();
                                                values.put(KEY_E_START_TIME, startTime + gmtOffset);
                                                values.put(KEY_E_END_TIME, endTime + gmtOffset);
                                            }

                                            if (exportedDBObj.getVersion() < 1090) {
                                                values.put(KEY_E_CALENDAR_ENABLED, 0);
                                                values.put(KEY_E_CALENDAR_CALENDARS, "");
                                                values.put(KEY_E_CALENDAR_SEARCH_FIELD, 0);
                                                values.put(KEY_E_CALENDAR_SEARCH_STRING, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1095) {
                                                values.put(KEY_E_CALENDAR_EVENT_START_TIME, 0);
                                                values.put(KEY_E_CALENDAR_EVENT_END_TIME, 0);
                                                values.put(KEY_E_CALENDAR_EVENT_FOUND, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1100) {
                                                switch (priority) {
                                                    case -2:
                                                        values.put(KEY_E_PRIORITY, -4);
                                                        break;
                                                    case -1:
                                                        values.put(KEY_E_PRIORITY, -2);
                                                        break;
                                                    case 1:
                                                        values.put(KEY_E_PRIORITY, 2);
                                                        break;
                                                    case 2:
                                                        values.put(KEY_E_PRIORITY, 4);
                                                        break;
                                                }
                                            }

                                            if (exportedDBObj.getVersion() < 1105) {
                                                values.put(KEY_E_WIFI_ENABLED, 0);
                                                values.put(KEY_E_WIFI_SSID, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1106) {
                                                values.put(KEY_E_WIFI_CONNECTION_TYPE, 1);
                                            }

                                            if (exportedDBObj.getVersion() < 1110) {
                                                values.put(KEY_E_SCREEN_ENABLED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1111) {
                                                values.put(KEY_E_SCREEN_EVENT_TYPE, 1);
                                            }

                                            if (exportedDBObj.getVersion() < 1112) {
                                                values.put(KEY_E_DELAY_START, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1113) {
                                                values.put(KEY_E_IS_IN_DELAY_START, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1125) {
                                                values.put(KEY_E_SCREEN_WHEN_UNLOCKED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1130) {
                                                values.put(KEY_E_BLUETOOTH_ENABLED, 0);
                                                values.put(KEY_E_BLUETOOTH_ADAPTER_NAME, "");
                                                values.put(KEY_E_BLUETOOTH_CONNECTION_TYPE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1140) {
                                                values.put(KEY_E_SMS_ENABLED, 0);
                                                //values.put(KEY_E_SMS_EVENT, 0);
                                                values.put(KEY_E_SMS_CONTACTS, "");
                                                values.put(KEY_E_SMS_CONTACT_LIST_TYPE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1141) {
                                                values.put(KEY_E_SMS_START_TIME, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1170) {
                                                values.put(KEY_E_DELAY_START, delayStart * 60); // conversion to seconds
                                            }

                                            if (exportedDBObj.getVersion() < 1180) {
                                                values.put(KEY_E_CALL_CONTACT_GROUPS, "");
                                                values.put(KEY_E_SMS_CONTACT_GROUPS, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1220) {
                                                if (useEndTime != 1) {
                                                    values.put(KEY_E_END_TIME, startTime + 5000); // add 5 seconds
                                                    values.put(KEY_E_USE_END_TIME, 1);
                                                }
                                            }

                                            if (exportedDBObj.getVersion() < 1295) {
                                                if (undoneProfile == 0)
                                                    values.put(KEY_E_AT_END_DO, Event.EATENDDO_NONE);
                                                else
                                                    values.put(KEY_E_AT_END_DO, Event.EATENDDO_UNDONE_PROFILE);
                                            }

                                            if (exportedDBObj.getVersion() < 1300) {
                                                values.put(KEY_E_CALENDAR_AVAILABILITY, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1310) {
                                                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1370) {
                                                values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                                            }

                                            if (exportedDBObj.getVersion() < 1380) {
                                                calendarSearchString = calendarSearchString.replace("%", "\\%").replace("_", "\\_");
                                                wifiSSID = wifiSSID.replace("%", "\\%").replace("_", "\\_");
                                                bluetoothAdapterName = bluetoothAdapterName.replace("%", "\\%").replace("_", "\\_");
                                                values.put(KEY_E_CALENDAR_SEARCH_STRING, calendarSearchString);
                                                values.put(KEY_E_WIFI_SSID, wifiSSID);
                                                values.put(KEY_E_BLUETOOTH_ADAPTER_NAME, bluetoothAdapterName);
                                            }

                                            if (exportedDBObj.getVersion() < 1390) {
                                                values.put(KEY_E_SMS_DURATION, 5);
                                            }

                                            if (exportedDBObj.getVersion() < 1400) {
                                                values.put(KEY_E_NOTIFICATION_ENABLED, 0);
                                                values.put(KEY_E_NOTIFICATION_APPLICATIONS, "");
                                                values.put(KEY_E_NOTIFICATION_START_TIME, 0);
                                                values.put(KEY_E_NOTIFICATION_DURATION, 5);
                                            }

                                            if (exportedDBObj.getVersion() < 1430) {
                                                values.put(KEY_E_BATTERY_POWER_SAVE_MODE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1434) {
                                                values.put(KEY_E_BLUETOOTH_DEVICES_TYPE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1450) {
                                                values.put(KEY_E_APPLICATION_ENABLED, 0);
                                                values.put(KEY_E_APPLICATION_APPLICATIONS, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1460) {
                                                values.put(KEY_E_NOTIFICATION_END_WHEN_REMOVED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1470) {
                                                values.put(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1500) {
                                                values.put(KEY_E_LOCATION_ENABLED, 0);
                                                values.put(KEY_E_LOCATION_FK_GEOFENCE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1520) {
                                                values.put(KEY_E_LOCATION_WHEN_OUTSIDE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1530) {
                                                values.put(KEY_E_DELAY_END, 0);
                                                values.put(KEY_E_IS_IN_DELAY_END, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1540) {
                                                values.put(KEY_E_START_STATUS_TIME, 0);
                                                values.put(KEY_E_PAUSE_STATUS_TIME, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1600) {
                                                values.put(KEY_E_ORIENTATION_ENABLED, 0);
                                                values.put(KEY_E_ORIENTATION_SIDES, "");
                                                values.put(KEY_E_ORIENTATION_DISTANCE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1610) {
                                                values.put(KEY_E_ORIENTATION_DISPLAY, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1620) {
                                                values.put(KEY_E_ORIENTATION_IGNORE_APPLICATIONS, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1670) {
                                                values.put(KEY_E_MOBILE_CELLS_ENABLED, 0);
                                                values.put(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1680) {
                                                values.put(KEY_E_MOBILE_CELLS_CELLS, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1710) {
                                                if (geofenceId != 0)
                                                    values.put(KEY_E_LOCATION_GEOFENCES, String.valueOf(geofenceId));
                                                else
                                                    values.put(KEY_E_LOCATION_GEOFENCES, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1720) {
                                                values.put(KEY_E_START_ORDER, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1750) {
                                                values.put(KEY_E_NFC_ENABLED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1770) {
                                                values.put(KEY_E_NFC_NFC_TAGS, "");
                                            }

                                            if (exportedDBObj.getVersion() < 1780) {
                                                values.put(KEY_E_NFC_DURATION, 5);
                                                values.put(KEY_E_NFC_START_TIME, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1790) {
                                                values.put(KEY_E_NFC_PERMANENT_RUN, 1);
                                                values.put(KEY_E_NOTIFICATION_PERMANENT_RUN, 0);
                                                values.put(KEY_E_SMS_PERMANENT_RUN, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1830) {
                                                values.put(KEY_E_CALENDAR_START_BEFORE_EVENT, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1840) {
                                                values.put(KEY_E_RADIO_SWITCH_ENABLED, 0);
                                                values.put(KEY_E_RADIO_SWITCH_WIFI, 0);
                                                values.put(KEY_E_RADIO_SWITCH_BLUETOOTH, 0);
                                                values.put(KEY_E_RADIO_SWITCH_MOBILE_DATA, 0);
                                                values.put(KEY_E_RADIO_SWITCH_GPS, 0);
                                                values.put(KEY_E_RADIO_SWITCH_NFC, 0);
                                                values.put(KEY_E_RADIO_SWITCH_AIRPLANE_MODE, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1900) {
                                                int gmtOffset = TimeZone.getDefault().getRawOffset();
                                                values.put(KEY_E_START_TIME, startTime - gmtOffset);
                                                values.put(KEY_E_END_TIME, endTime - gmtOffset);
                                            }

                                            if (exportedDBObj.getVersion() < 1910) {
                                                values.put(KEY_E_NOTIFICATION_VIBRATE_START, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1920) {
                                                values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1930) {
                                                Calendar calendar = Calendar.getInstance();

                                                calendar.setTimeInMillis(startTime);
                                                values.put(KEY_E_START_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                                                calendar.setTimeInMillis(endTime);
                                                values.put(KEY_E_END_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                                            }

                                            if (exportedDBObj.getVersion() < 1960) {
                                                values.put(KEY_E_CALL_DURATION, 5);
                                                values.put(KEY_E_CALL_PERMANENT_RUN, 0);
                                                values.put(KEY_E_CALL_START_TIME, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 1970) {
                                                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_START, 0);
                                                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, 15);
                                            }

                                            if (exportedDBObj.getVersion() < 1990) {
                                                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, notificationRepeatInterval * 60);
                                            }

                                            if (exportedDBObj.getVersion() < 2000) {
                                                values.put(KEY_E_NOTIFICATION_IN_CALL, 0);
                                                values.put(KEY_E_NOTIFICATION_MISSED_CALL, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2040) {
                                                if (fkProfileStartWhenActivated != Profile.PROFILE_NO_ACTIVATE)
                                                    values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, String.valueOf(fkProfileStartWhenActivated));
                                                else
                                                    values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, "");
                                            }

                                            if (exportedDBObj.getVersion() < 2120) {
                                                values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, 0);
                                                values.put(KEY_E_LOCATION_SENSOR_PASSED, 0);
                                                values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, 0);
                                                values.put(KEY_E_ORIENTATION_SENSOR_PASSED, 0);
                                                values.put(KEY_E_WIFI_SENSOR_PASSED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2140) {
                                                values.put(KEY_E_APPLICATION_SENSOR_PASSED, 0);
                                                values.put(KEY_E_BATTERY_SENSOR_PASSED, 0);
                                                values.put(KEY_E_CALENDAR_SENSOR_PASSED, 0);
                                                values.put(KEY_E_CALL_SENSOR_PASSED, 0);
                                                values.put(KEY_E_NFC_SENSOR_PASSED, 0);
                                                values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, 0);
                                                values.put(KEY_E_PERIPHERAL_SENSOR_PASSED, 0);
                                                values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, 0);
                                                values.put(KEY_E_SCREEN_SENSOR_PASSED, 0);
                                                values.put(KEY_E_SMS_SENSOR_PASSED, 0);
                                                values.put(KEY_E_TIME_SENSOR_PASSED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2160) {
                                                values.put(KEY_E_CALENDAR_ALL_EVENTS, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2170) {
                                                values.put(KEY_E_ALARM_CLOCK_ENABLED, 0);
                                                values.put(KEY_E_ALARM_CLOCK_START_TIME, 0);
                                                values.put(KEY_E_ALARM_CLOCK_DURATION, 5);
                                                values.put(KEY_E_ALARM_CLOCK_PERMANENT_RUN, 0);
                                                values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2180) {
                                                values.put(KEY_E_NOTIFICATION_SOUND_END, "");
                                                values.put(KEY_E_NOTIFICATION_VIBRATE_END, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2240) {
                                                values.put(KEY_E_BATTERY_PLUGGED, "");
                                            }

                                            if (exportedDBObj.getVersion() < 2270) {
                                                String searchStringNew = "";
                                                String[] searchStringSplits = calendarSearchString.split("\\|");
                                                for (String split : searchStringSplits) {
                                                    if (!split.isEmpty()) {
                                                        String searchPattern = split;
                                                        if (searchPattern.startsWith("!")) {
                                                            searchPattern =  "\\" + searchPattern;
                                                        }
                                                        if (!searchStringNew.isEmpty())
                                                            //noinspection StringConcatenationInLoop
                                                            searchStringNew = searchStringNew + "|";
                                                        //noinspection StringConcatenationInLoop
                                                        searchStringNew = searchStringNew + searchPattern;
                                                    }
                                                }
                                                values.put(KEY_E_CALENDAR_SEARCH_STRING, searchStringNew);
                                            }

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_EVENTS, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    exportedDBObjVersion = exportedDBObj.getVersion();
                                    cursorExportedDB.close();
                                    cursorImportDB.close();
                                }

                                if (exportedDBObjVersion < 1740) {
                                    // initialize startOrder
                                    final String selectQuery = "SELECT " + KEY_E_ID +
                                            " FROM " + TABLE_EVENTS +
                                            " ORDER BY " + KEY_E_PRIORITY;

                                    Cursor cursor = db.rawQuery(selectQuery, null);

                                    int startOrder = 0;
                                    if (cursor.moveToFirst()) {
                                        do {
                                            ContentValues _values = new ContentValues();
                                            _values.put(KEY_E_START_ORDER, ++startOrder);
                                            db.update(TABLE_EVENTS, _values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndex(KEY_E_ID))});
                                        } while (cursor.moveToNext());
                                    }

                                    cursor.close();
                                }

                                db.execSQL("DELETE FROM " + TABLE_ACTIVITY_LOG);

                                if (tableExists(TABLE_ACTIVITY_LOG, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_ACTIVITY_LOG, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_ACTIVITY_LOG, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                        /*if (exportedDBObj.getVersion() < 30)
                                        {
                                            values.put(KEY_E_USE_END_TIME, 0);
                                        }*/

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_ACTIVITY_LOG, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.execSQL("DELETE FROM " + TABLE_GEOFENCES);

                                if (tableExists(TABLE_GEOFENCES, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_GEOFENCES, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_GEOFENCES, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                            if (exportedDBObj.getVersion() < 1480) {
                                                values.put(KEY_G_CHECKED, 0);
                                            }
                                            if (exportedDBObj.getVersion() < 1510) {
                                                values.put(KEY_G_TRANSITION, 0);
                                            }

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_GEOFENCES, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.execSQL("DELETE FROM " + TABLE_SHORTCUTS);

                                if (tableExists(TABLE_SHORTCUTS, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                            /*if (exportedDBObj.getVersion() < 1480) {
                                                values.put(KEY_G_CHECKED, 0);
                                            }
                                            if (exportedDBObj.getVersion() < 1510) {
                                                values.put(KEY_G_TRANSITION, 0);
                                            }*/

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_SHORTCUTS, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.execSQL("DELETE FROM " + TABLE_MOBILE_CELLS);

                                if (tableExists(TABLE_MOBILE_CELLS, exportedDBObj)) {
                                    // cursor for exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_MOBILE_CELLS, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_MOBILE_CELLS, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                            if (exportedDBObj.getVersion() < 1700) {
                                                values.put(KEY_MC_NEW, 0);
                                            }
                                            if (exportedDBObj.getVersion() < 1800) {
                                                values.put(KEY_MC_LAST_CONNECTED_TIME, 0);
                                            }

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_MOBILE_CELLS, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.execSQL("DELETE FROM " + TABLE_NFC_TAGS);

                                if (tableExists(TABLE_NFC_TAGS, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_NFC_TAGS, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_NFC_TAGS, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                            if (exportedDBObj.getVersion() < 2130) {
                                                values.put(KEY_NT_UID, "");
                                            }

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_NFC_TAGS, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.execSQL("DELETE FROM " + TABLE_INTENTS);

                                if (tableExists(TABLE_INTENTS, exportedDBObj)) {
                                    // cursor for events exportedDB
                                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_INTENTS, null);
                                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                                    // cursor for profiles of destination db
                                    cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_INTENTS, null);

                                    if (cursorExportedDB.moveToFirst()) {
                                        do {
                                            values.clear();
                                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                                }
                                            }

                                            // for non existent fields set default value
                                            if (exportedDBObj.getVersion() < 2200) {
                                                values.put(KEY_IN_NAME, "");
                                            }

                                            if (exportedDBObj.getVersion() < 2280) {
                                                values.put(KEY_IN_USED_COUNT, 0);
                                            }

                                            if (exportedDBObj.getVersion() < 2290) {
                                                values.put(KEY_IN_INTENT_TYPE, 0);
                                            }

                                            // Inserting Row do db z SQLiteOpenHelper
                                            db.insert(TABLE_INTENTS, null, values);

                                        } while (cursorExportedDB.moveToNext());
                                    }

                                    cursorExportedDB.close();
                                    cursorImportDB.close();

                                }

                                db.setTransactionSuccessful();

                                ret = IMPORT_OK;
                            } finally {
                                db.endTransaction();
                                if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                                    cursorExportedDB.close();
                                if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                                    cursorImportDB.close();
                                //db.close();
                            }
                        } else {
                            //    exportedDBObj.close();
                            ret = IMPORT_ERROR_NEVER_VERSION;
                        }
                    }
                } catch (Exception e) {
                    Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e));
                    ret = IMPORT_ERROR_BUG;
                }

            } catch (Exception e) {
                Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e));
            }
            return ret;
        } finally {
            stopRunningImportExport();
        }
    }

    @SuppressWarnings("resource")
    int exportDB()
    {
        importExportLock.lock();
        try {
            int ret = 0;
            try {
                startRunningImportExport();

                try {

                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    File dataDB = new File(data, GlobalGUIRoutines.DB_FILEPATH + "/" + DATABASE_NAME);
                    File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + EXPORT_DBFILENAME);

                    if (dataDB.exists()) {
                        // close db
                        close();

                        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
                        if (!(exportDir.exists() && exportDir.isDirectory())) {
                            //noinspection ResultOfMethodCallIgnored
                            exportDir.mkdirs();
                        }

                        FileChannel src = new FileInputStream(dataDB).getChannel();
                        FileChannel dst = new FileOutputStream(exportedDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();

                        ret = 1;
                    }
                } catch (Exception e) {
                    Log.e("DatabaseHandler.exportDB", Log.getStackTraceString(e));
                }

            } catch (Exception ignored) {}
            return ret;
        } finally {
            stopRunningImportExport();
        }
    }

}
