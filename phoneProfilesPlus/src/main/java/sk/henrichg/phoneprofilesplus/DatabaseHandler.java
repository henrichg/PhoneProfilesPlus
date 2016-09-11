package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class DatabaseHandler extends SQLiteOpenHelper {


    // All Static variables

    // singleton fields
    private static DatabaseHandler instance;
    private static SQLiteDatabase writableDb;
    
    Context context;
    
    // Database Version
    private static final int DATABASE_VERSION = 1700;

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

    // import/export
    private final String EXPORT_DBFILENAME = DATABASE_NAME + ".backup";

    // event type
    public static final int ETYPE_TIME = 1;
    public static final int ETYPE_BATTERY = 2;
    public static final int ETYPE_CALL = 3;
    public static final int ETYPE_PERIPHERAL = 4;
    public static final int ETYPE_CALENDAR = 5;
    public static final int ETYPE_WIFICONNECTED = 6;
    public static final int ETYPE_WIFIINFRONT = 7;
    public static final int ETYPE_SCREEN = 8;
    public static final int ETYPE_BLUETOOTHCONNECTED = 9;
    public static final int ETYPE_BLUETOOTHINFRONT = 10;
    public static final int ETYPE_SMS = 11;
    public static final int ETYPE_NOTIFICATION = 12;
    public static final int ETYPE_APPLICATION = 13;
    public static final int ETYPE_LOCATION = 14;
    public static final int ETYPE_ORIENTATION = 15;
    public static final int ETYPE_MOBILE_CELLS = 16;

    // activity log types
    public static final int ALTYPE_PROFILEACTIVATION = 1;
    public static final int ALTYPE_AFTERDURATION_UNDOPROFILE = 21;
    public static final int ALTYPE_AFTERDURATION_BACKGROUNDPROFILE = 22;
    public static final int ALTYPE_AFTERDURATION_RESTARTEVENTS = 23;
    public static final int ALTYPE_EVENTSTART = 3;
    public static final int ALTYPE_EVENTSTARTDELAY = 4;
    public static final int ALTYPE_EVENTEND_NONE = 51;
    public static final int ALTYPE_EVENTEND_ACTIVATEPROFILE = 52;
    public static final int ALTYPE_EVENTEND_UNDOPROFILE = 53;
    public static final int ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE = 54;
    public static final int ALTYPE_EVENTEND_RESTARTEVENTS = 55;
    public static final int ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS = 56;
    public static final int ALTYPE_RESTARTEVENTS = 6;
    public static final int ALTYPE_RUNEVENTS_DISABLE = 7;
    public static final int ALTYPE_RUNEVENTS_ENABLE = 8;
    public static final int ALTYPE_APPLICATIONSTART = 9;
    public static final int ALTYPE_APPLICATIONEXIT = 10;
    public static final int ALTYPE_DATAIMPORT = 11;
    public static final int ALTYPE_PAUSEDLOGGING = 12;
    public static final int ALTYPE_STARTEDLOGGING = 13;
    public static final int ALTYPE_EVENTENDDELAY = 14;

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
    private static final String KEY_DEVICE_KEYGUARD = "deviceKeyguard";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrateOnTouch";
    private static final String KEY_DEVICE_WIFI_AP = "deviceWifiAP";
    private static final String KEY_DEVICE_POWER_SAVE_MODE = "devicePowerSaveMode";
    private static final String KEY_SHOW_DURATION_BUTTON = "showDurationButton";
    private static final String KEY_ASK_FOR_DURATION = "askForDuration";
    private static final String KEY_DEVICE_NETWORK_TYPE = "deviceNetworkType";
    private static final String KEY_NOTIFICATION_LED = "notificationLed";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrateWhenRinging";
    private static final String KEY_DEVICE_WALLPAPER_FOR = "deviceWallpaperFor";

    // Events Table Columns names
    private static final String KEY_E_ID = "id";
    private static final String KEY_E_NAME = "name";
    private static final String KEY_E_TYPE = "type";
    private static final String KEY_E_FK_PROFILE_START = "fkProfile";
    private static final String KEY_E_STATUS = "status";
    private static final String KEY_E_START_TIME = "startTime";
    private static final String KEY_E_END_TIME = "endTime";
    private static final String KEY_E_DAYS_OF_WEEK = "daysOfWeek";
    private static final String KEY_E_USE_END_TIME = "useEndTime";
    private static final String KEY_E_BATTERY_LEVEL = "batteryLevel";
    private static final String KEY_E_BATTERY_DETECTOR_TYPE = "batteryDetectorType";
    private static final String KEY_E_NOTIFICATION_SOUND = "notificationSound";
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

    // EventTimeLine Table Columns names
    private static final String KEY_ET_ID = "id";
    private static final String KEY_ET_EORDER = "eorder";
    private static final String KEY_ET_FK_EVENT = "fkEvent";
    private static final String KEY_ET_FK_PROFILE_RETURN = "fkProfileReturn";

    // ActivityLog Columns names
    public static final String KEY_AL_ID = "_id";  // for CursorAdapter must by this name
    public static final String KEY_AL_LOG_TYPE = "logType";
    public static final String KEY_AL_LOG_DATE_TIME = "logDateTime";
    public static final String KEY_AL_EVENT_NAME = "eventName";
    public static final String KEY_AL_PROFILE_NAME = "profileName";
    public static final String KEY_AL_PROFILE_ICON = "profileIcon";
    public static final String KEY_AL_DURATION_DELAY = "durationDelay";

    // Geofences Columns names
    public static final String KEY_G_ID = "_id";  // for CursorAdapter must by this name
    public static final String KEY_G_LATITUDE = "latitude";
    public static final String KEY_G_LONGITUDE = "longitude";
    public static final String KEY_G_RADIUS = "radius";
    public static final String KEY_G_NAME = "name";
    public static final String KEY_G_CHECKED = "checked";
    public static final String KEY_G_TRANSITION = "transition";

    // Shortcuts Colums names
    public static final String KEY_S_ID = "_id";  // for CursorAdapter must by this name
    public static final String KEY_S_INTENT = "intent";
    public static final String KEY_S_NAME = "name";

    // Mobile cells Colums names
    public static final String KEY_MC_ID = "_id";  // for CursorAdapter must by this name
    public static final String KEY_MC_CELL_ID = "cellId";
    public static final String KEY_MC_NAME = "name";
    public static final String KEY_MC_NEW = "new";

    /**
     * Constructor takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     *            the application context
     */	
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Get default instance of the class to keep it a singleton
     *
     * @param context
     *            the application context
     */
    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }
        return instance;
    }
    
    /**
     * Returns a writable database instance in order not to open and close many
     * SQLiteDatabase objects simultaneously
     *
     * @return a writable instance to SQLiteDatabase
     */
    public SQLiteDatabase getMyWritableDatabase() {
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
    
    // be sure to call this method by: DatabaseHandler.getInstance().closeConnecion() 
    // when application is closed by somemeans most likely
    // onDestroy method of application
    public synchronized void closeConnection() {
        if (instance != null)
        {
            instance.close();
            instance = null;
        }
    }    

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
                + KEY_SHOW_DURATION_BUTTON + " INTEGER,"
                + KEY_ASK_FOR_DURATION + " INTEGER,"
                + KEY_DEVICE_NETWORK_TYPE + " INTEGER,"
                + KEY_NOTIFICATION_LED + " INTEGER,"
                + KEY_VIBRATE_WHEN_RINGING + " INTEGER,"
                + KEY_DEVICE_WALLPAPER_FOR + " INTEGER"
                + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_PROFILES_TABLE = profileTableCreationString(TABLE_PROFILES);
        db.execSQL(CREATE_PROFILES_TABLE);

        db.execSQL("CREATE INDEX IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        db.execSQL("CREATE INDEX IDX_SHOW_IN_ACTIVATOR ON " + TABLE_PROFILES + " (" + KEY_SHOW_IN_ACTIVATOR + ")");
        db.execSQL("CREATE INDEX IDX_P_NAME ON " + TABLE_PROFILES + " (" + KEY_NAME + ")");

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
                + KEY_E_NOTIFICATION_SOUND + " TEXT,"
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
                + KEY_E_MOBILE_CELLS_CELLS + " TEXT"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        db.execSQL("CREATE INDEX IDX_FK_PROFILE ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START + ")");
        db.execSQL("CREATE INDEX IDX_E_NAME ON " + TABLE_EVENTS + " (" + KEY_E_NAME + ")");
        db.execSQL("CREATE INDEX IDX_FK_PROFILE_END ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_END + ")");
        db.execSQL("CREATE INDEX IDX_PRIORITY ON " + TABLE_EVENTS + " (" + KEY_E_PRIORITY + ")");

        final String CREATE_EVENTTIME_TABLE = "CREATE TABLE " + TABLE_EVENT_TIMELINE + "("
                + KEY_ET_ID + " INTEGER PRIMARY KEY,"
                + KEY_ET_EORDER + " INTEGER,"
                + KEY_ET_FK_EVENT + " INTEGER,"
                + KEY_ET_FK_PROFILE_RETURN + " INTEGER"
                + ")";
        db.execSQL(CREATE_EVENTTIME_TABLE);

        db.execSQL("CREATE INDEX IDX_ET_PORDER ON " + TABLE_EVENT_TIMELINE + " (" + KEY_ET_EORDER + ")");

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

        db.execSQL("CREATE INDEX IDX_AL_LOG_DATE_TIME ON " + TABLE_ACTIVITY_LOG + " (" + KEY_AL_LOG_DATE_TIME + ")");

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
                + KEY_MC_NEW + " INTEGER"
                + ")";
        db.execSQL(CREATE_MOBILE_CELLS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        GlobalData.logE("DatabaseHandler.onUpgrade", "oldVersion="+oldVersion);
        GlobalData.logE("DatabaseHandler.onUpgrade", "newVersion="+newVersion);

        /*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);

        // Create tables again
        onCreate(db);
        */

        if (oldVersion < 16)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "='-'");
        }

        if (oldVersion < 18)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ICON + "=replace(" + KEY_ICON + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_RINGTONE + "=replace(" + KEY_VOLUME_RINGTONE + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_NOTIFICATION + "=replace(" + KEY_VOLUME_NOTIFICATION + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_MEDIA + "=replace(" + KEY_VOLUME_MEDIA + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ALARM + "=replace(" + KEY_VOLUME_ALARM + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SYSTEM + "=replace(" + KEY_VOLUME_SYSTEM + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_VOICE + "=replace(" + KEY_VOLUME_VOICE + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_BRIGHTNESS + "=replace(" + KEY_DEVICE_BRIGHTNESS + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "=replace(" + KEY_DEVICE_WALLPAPER + ",':','|')");
        }

        if (oldVersion < 19)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA + "=0");
        }

        if (oldVersion < 20)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA_PREFS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_PREFS + "=0");
        }

        if (oldVersion < 21)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_GPS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_GPS + "=0");
        }

        if (oldVersion < 22)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 23)
        {
            // index na PORDER
            db.execSQL("CREATE INDEX IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        }

        if (oldVersion < 24)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOSYNC + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOSYNC + "=0");

            db.execSQL("CREATE INDEX IDX_P_NAME ON " + TABLE_PROFILES + " (" + KEY_NAME + ")");
            db.execSQL("CREATE INDEX IDX_E_NAME ON " + TABLE_EVENTS + " (" + KEY_E_NAME + ")");
        }

        if (oldVersion < 25)
        {
            final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                    + KEY_E_ID + " INTEGER PRIMARY KEY,"
                    + KEY_E_NAME + " TEXT,"
                    + KEY_E_TYPE + " INTEGER,"
                    + KEY_E_FK_PROFILE_START + " INTEGER"
                    + ")";
            db.execSQL(CREATE_EVENTS_TABLE);
        }

        if (oldVersion < 26)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SHOW_IN_ACTIVATOR + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SHOW_IN_ACTIVATOR + "=1");
        }

        if (oldVersion < 28)
        {
            // index na SHOW_IN_ACTIVATOR
            db.execSQL("CREATE INDEX IDX_SHOW_IN_ACTIVATOR ON " + TABLE_PROFILES + " (" + KEY_SHOW_IN_ACTIVATOR + ")");
        }

        if (oldVersion < 29)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_END_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DAYS_OF_WEEK + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DAYS_OF_WEEK + "=\"#ALL#\"");

            // pridame index
            db.execSQL("CREATE INDEX IDX_FK_PROFILE ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START + ")");
        }

        if (oldVersion < 30)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_USE_END_TIME + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_USE_END_TIME + "=0");
        }

        if (oldVersion < 32)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_STATUS + " INTEGER");

            // updatneme zaznamy
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

            db.execSQL("CREATE INDEX IDX_ET_PORDER ON " + TABLE_EVENT_TIMELINE + " (" + KEY_ET_EORDER + ")");
        }

        if (oldVersion < 1001)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOROTATE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=0");
        }

        if (oldVersion < 1002)
        {
            // updatneme zaznamy
            // autorotate off -> rotation 0
            // autorotate on -> autorotate
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=1");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=3");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=2 WHERE " + KEY_DEVICE_AUTOROTATE + "=2");
        }

        if (oldVersion < 1012)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL + "=15");
        }

        if (oldVersion < 1015)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_LOCATION_SERVICE_PREFS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LOCATION_SERVICE_PREFS + "=0");
        }

        if (oldVersion < 1016)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_DETECTOR_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_DETECTOR_TYPE + "=0");
        }

        if (oldVersion < 1020)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_SPEAKER_PHONE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SPEAKER_PHONE + "=0");
        }

        if (oldVersion < 1022)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_SOUND + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND + "=\"\"");
        }

        if (oldVersion < 1023)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL_LOW + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_LEVEL_HIGHT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_CHARGING + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_LOW + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=100");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_CHARGING + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=" + KEY_E_BATTERY_LEVEL + " WHERE " + KEY_E_BATTERY_DETECTOR_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_LOW + "=" + KEY_E_BATTERY_LEVEL + " WHERE " + KEY_E_BATTERY_DETECTOR_TYPE + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_CHARGING + "=1 WHERE " + KEY_E_BATTERY_DETECTOR_TYPE + "=2");
        }

        if (oldVersion < 1030)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_TIME_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_ENABLED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_ENABLED + "=1 WHERE " + KEY_E_TYPE + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_ENABLED + "=1 WHERE " + KEY_E_TYPE + "=2");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=0 WHERE " + KEY_E_TYPE + "=2");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=0 WHERE " + KEY_E_TYPE + "=2");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DAYS_OF_WEEK + "=\"#ALL#\" WHERE " + KEY_E_TYPE + "=2");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_USE_END_TIME + "=0 WHERE " + KEY_E_TYPE + "=2");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_LOW + "=0 WHERE " + KEY_E_TYPE + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=100 WHERE " + KEY_E_TYPE + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_CHARGING + "=0 WHERE " + KEY_E_TYPE + "=1");
        }

        if (oldVersion < 1035)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NFC + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NFC + "=0");
        }

        if (oldVersion < 1040)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_EVENT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACTS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACT_LIST_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1045)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FK_PROFILE_END + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_END + "=" + GlobalData.PROFILE_NO_ACTIVATE);

            // pridame index
            db.execSQL("CREATE INDEX IDX_FK_PROFILE_END ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_END + ")");
        }

        if (oldVersion < 1050)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FORCE_RUN + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FORCE_RUN + "=0");
        }

        if (oldVersion < 1051)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLOCKED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLOCKED + "=0");
        }

        if (oldVersion < 1060)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_UNDONE_PROFILE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_UNDONE_PROFILE + "=1");
        }

        if (oldVersion < 1070)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PRIORITY + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=0");

            // pridame index
            db.execSQL("CREATE INDEX IDX_PRIORITY ON " + TABLE_EVENTS + " (" + KEY_E_PRIORITY + ")");
        }

        if (oldVersion < 1080)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PERIPHERAL_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PERIPHERAL_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIPHERAL_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIPHERAL_TYPE + "=0");
        }

        if (oldVersion < 1081)
        {
            // conversion into GMT
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=" + KEY_E_START_TIME + "+" + gmtOffset);
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=" + KEY_E_END_TIME + "+" + gmtOffset);
        }

        if (oldVersion < 1090)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_CALENDARS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_SEARCH_FIELD + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_SEARCH_STRING + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_CALENDARS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_FIELD + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"\"");
        }

        if (oldVersion < 1095)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_END_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_EVENT_FOUND + " INTEGER");

            // updatneme zaznamy
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
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_SSID + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_SSID + "=\"\"");
        }

        if (oldVersion < 1106)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_WIFI_CONNECTION_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_CONNECTION_TYPE + "=1");
        }

        if (oldVersion < 1110)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_ENABLED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_ENABLED + "=0");
        }

        if (oldVersion < 1111)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_EVENT_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_EVENT_TYPE + "=1");
        }

        if (oldVersion < 1112)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DELAY_START + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_START + "=0");
        }

        if (oldVersion < 1113)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_IS_IN_DELAY_START + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_START + "=0");
        }

        if (oldVersion < 1120)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_AFTER_DURATION_DO + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_AFTER_DURATION_DO + "=0");
        }

        if (oldVersion < 1125)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SCREEN_WHEN_UNLOCKED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_WHEN_UNLOCKED + "=0");
        }

        if (oldVersion < 1130)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_ADAPTER_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_CONNECTION_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0");
        }

        if (oldVersion < 1140)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_ENABLED + " INTEGER");
            //db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_EVENT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACTS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACT_LIST_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_ENABLED + "=0");
            //db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1141)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_START_TIME + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_START_TIME + "=0");
        }

        if (oldVersion < 1150)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_ZEN_MODE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ZEN_MODE + "=0");
        }

        if (oldVersion < 1156)
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
            {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(0));
                        String brightness = cursor.getString(1);

                        //value|noChange|automatic|defaultProfile
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
            }
        }

        if (oldVersion < 1160)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_KEYGUARD + " INTEGER");

            // updatneme zaznamy
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
                    long id = Long.parseLong(cursor.getString(0));
                    String brightness = cursor.getString(1);

                    //value|noChange|automatic|defaultProfile
                    String[] splits = brightness.split("\\|");

                    int perc = Integer.parseInt(splits[0]);
                    perc = (int)Profile.convertBrightnessToPercents(perc, 255, 1, context);

                    // hm, found brightness values without default profile :-/
                    if (splits.length == 4)
                        brightness = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                    else
                        brightness = perc+"|"+splits[1]+"|"+splits[2]+"|0";

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                                 " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\" " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

        }

        if (oldVersion < 1170)
        {
            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                            KEY_E_DELAY_START +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(0));
                    int delayStart = cursor.getInt(1) * 60;  // conversiont to seconds

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_DELAY_START + "=" + delayStart + " " +
                                "WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }
        }

        if (oldVersion < 1175)
        {
            if (android.os.Build.VERSION.SDK_INT < 21)
            {
                // updatneme zaznamy
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(0));
                        String brightness = cursor.getString(1);

                        //value|noChange|automatic|defaultProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            int perc = 50;

                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = perc+"|"+splits[1]+"|"+splits[2]+"|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                         " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                        "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }
            }
        }

        if (oldVersion < 1180)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALL_CONTACT_GROUPS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_CONTACT_GROUPS + " TEXT");

            // updatneme zaznamy
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

            db.execSQL("CREATE INDEX IDX_AL_LOG_DATE_TIME ON " + TABLE_ACTIVITY_LOG + " (" + KEY_AL_LOG_DATE_TIME + ")");
        }

        if (oldVersion < 1210)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_ON_TOUCH + " INTEGER");

            // updatneme zaznamy
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
                    long id = Long.parseLong(cursor.getString(0));
                    long startTime = cursor.getLong(2);

                    if (cursor.getInt(1) != 1)
                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                     " SET " + KEY_E_END_TIME + "=" + (startTime+5000) + ", "
                                             + KEY_E_USE_END_TIME + "=1" +
                                   " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }
        }

        if (oldVersion < 1295)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_AT_END_DO + " INTEGER");

            final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                                   KEY_E_UNDONE_PROFILE +
                                        " FROM " + TABLE_EVENTS;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(0));
                    int atEndDo;

                    if (cursor.isNull(1) || (cursor.getInt(1) == 0))
                        atEndDo = Event.EATENDDO_NONE;
                    else
                        atEndDo = Event.EATENDDO_UNDONE_PROFILE;

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_AT_END_DO + "=" + atEndDo +
                               " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }
        }

        if (oldVersion < 1300)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_AVAILABILITY + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_AVAILABILITY + "=0");
        }

        if (oldVersion < 1310)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MANUAL_PROFILE_ACTIVATION + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MANUAL_PROFILE_ACTIVATION + "=0");
        }

        boolean mergedTableCreate = false;
        if (oldVersion < 1320) {
            final String CREATE_MERGED_PROFILE_TABLE = profileTableCreationString(TABLE_MERGED_PROFILE);
            db.execSQL(CREATE_MERGED_PROFILE_TABLE);
            mergedTableCreate = true;
        }

        if (oldVersion < 1330)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WIFI_AP + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1340)
        {
            if (!mergedTableCreate) {
                // pridame nove stlpce
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_WIFI_AP + " INTEGER");
            }

            // updatneme zaznamy
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
                    long id = Long.parseLong(cursor.getString(0));
                    int delayStart = cursor.getInt(1) * 60;  // conversiont to seconds

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                            " SET " + KEY_DURATION + "=" + delayStart + " " +
                            "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }
        }

        if (oldVersion < 1370)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + "=-999");

            // pridame index
            db.execSQL("CREATE INDEX IDX_FK_PROFILE_START_WHEN_ACTIVATED ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + ")");
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
                    long id = Long.parseLong(cursor.getString(0));
                    String calendarSearchString = cursor.getString(1).replace("%", "\\%").replace("_", "\\_");
                    String wifiSSID = cursor.getString(2).replace("%", "\\%").replace("_", "\\_");
                    String bluetoothAdapterName = cursor.getString(3).replace("%", "\\%").replace("_", "\\_");

                    db.execSQL("UPDATE " + TABLE_EVENTS +
                                 " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"" + calendarSearchString + "\"," +
                                           KEY_E_WIFI_SSID + "=\"" + wifiSSID + "\"," +
                                           KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"" + bluetoothAdapterName + "\"" +
                               " WHERE " + KEY_E_ID + "=" + id);

                } while (cursor.moveToNext());
            }

        }

        if (oldVersion < 1390)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_SMS_DURATION + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_DURATION + "=5");
        }

        if (oldVersion < 1400)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_APPLICATIONS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_START_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_DURATION + " INTEGER");

            // updatneme zaznamy
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
                    long id = Long.parseLong(cursor.getString(0));
                    int zenMode = cursor.getInt(1);

                    if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23)) // Alarms only zen mode is supported from Android 6.0
                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_VOLUME_ZEN_MODE + "=3" + " " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }
        }

        if (oldVersion < 1420)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1430)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BATTERY_POWER_SAVE_MODE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1440)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_BLUETOOTH_DEVICES_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_DEVICES_TYPE + "=0");
        }

        if (oldVersion < 1450)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_APPLICATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_APPLICATION_APPLICATIONS + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1460)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_NOTIFICATION_END_WHEN_REMOVED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_END_WHEN_REMOVED + "=0");
        }

        if (oldVersion < 1470)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + " INTEGER");

            // updatneme zaznamy
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
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_GEOFENCES + " ADD COLUMN " + KEY_G_CHECKED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_CHECKED + "=0");
        }

        if (oldVersion < 1500)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_FK_GEOFENCE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_FK_GEOFENCE + "=0");
        }

        if (oldVersion < 1510) {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_GEOFENCES + " ADD COLUMN " + KEY_G_TRANSITION + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_TRANSITION + "=0");
        }

        if (oldVersion < 1520) {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_LOCATION_WHEN_OUTSIDE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1530)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_DELAY_END + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_IS_IN_DELAY_END + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_END + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_END + "=0");
        }

        if (oldVersion < 1540)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_START_STATUS_TIME + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_PAUSE_STATUS_TIME + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_STATUS_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PAUSE_STATUS_TIME + "=0");
        }

        if (oldVersion < 1550)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SHOW_DURATION_BUTTON + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_SHOW_DURATION_BUTTON + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SHOW_DURATION_BUTTON + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SHOW_DURATION_BUTTON + "=0");
        }

        if (oldVersion < 1560)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_ASK_FOR_DURATION + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_ASK_FOR_DURATION + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ASK_FOR_DURATION + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ASK_FOR_DURATION + "=0");
        }

        if (oldVersion < 1570)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
        }

        if (oldVersion < 1580)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_NOTIFICATION_LED + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_NOTIFICATION_LED + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_NOTIFICATION_LED + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_NOTIFICATION_LED + "=0");
        }

        if (oldVersion < 1600)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_SIDES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_DISTANCE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_SIDES + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISTANCE + "=0");
        }

        if (oldVersion < 1610)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_DISPLAY + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISPLAY + "=\"\"");
        }

        if (oldVersion < 1620)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1630)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_WHEN_RINGING + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1640) {
            if (!mergedTableCreate)
                // pridame nove stlpce
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_VIBRATE_WHEN_RINGING + " INTEGER");

            // updatneme zaznamy
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
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_FOR + " INTEGER");
            if (!mergedTableCreate) {
                db.execSQL("ALTER TABLE " + TABLE_MERGED_PROFILE + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_FOR + " INTEGER");
            }

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
        }

        if (oldVersion < 1670)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_ENABLED + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1680)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + KEY_E_MOBILE_CELLS_CELLS + " TEXT");

            // updatneme zaznamy
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
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_MOBILE_CELLS + " ADD COLUMN " + KEY_MC_NEW + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_NEW +  "=0");
        }

        GlobalData.logE("DatabaseHandler.onUpgrade", "END");

    }


// PROFILES --------------------------------------------------------------------------------

    // Adding new profile
    void addProfile(Profile profile, boolean merged) {

        int porder = getMaxPOrder() + 1;

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
        values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutosync);
        values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
        values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
        values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
        values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
        values.put(KEY_DEVICE_NFC, profile._deviceNFC);
        values.put(KEY_DURATION, profile._duration);
        values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
        values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
        values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
        values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
        values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
        values.put(KEY_SHOW_DURATION_BUTTON, 0);
        values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
        values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
        values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
        values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
        values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);

        // Inserting Row
        if (!merged) {
            profile._id = db.insert(TABLE_PROFILES, null, values);
            profile._porder = porder;
        }
        else {
            values.put(KEY_ID, profile._id);
            db.insert(TABLE_MERGED_PROFILE, null, values);
        }
        //db.close(); // Closing database connection
    }

    // Getting single profile
    Profile getProfile(long profile_id, boolean merged) {
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
                        KEY_VOLUME_ZEN_MODE,
                        KEY_DEVICE_KEYGUARD,
                        KEY_VIBRATE_ON_TOUCH,
                        KEY_DEVICE_WIFI_AP,
                        KEY_DEVICE_POWER_SAVE_MODE,
                        KEY_SHOW_DURATION_BUTTON,
                        KEY_ASK_FOR_DURATION,
                        KEY_DEVICE_NETWORK_TYPE,
                        KEY_NOTIFICATION_LED,
                        KEY_VIBRATE_WHEN_RINGING,
                        KEY_DEVICE_WALLPAPER_FOR
                },
                KEY_ID + "=?",
                new String[]{String.valueOf(profile_id)}, null, null, null, null);

        Profile profile = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                profile = new Profile(Long.parseLong(cursor.getString(0)),
                                              cursor.getString(1),
                                              cursor.getString(2),
                                              (Integer.parseInt(cursor.getString(3)) == 1) ? true : false,
                                              Integer.parseInt(cursor.getString(4)),
                                              Integer.parseInt(cursor.getString(5)),
                                              cursor.getString(6),
                                              cursor.getString(7),
                                              cursor.getString(8),
                                              cursor.getString(9),
                                              cursor.getString(10),
                                              cursor.getString(11),
                                              Integer.parseInt(cursor.getString(12)),
                                              cursor.getString(13),
                                              Integer.parseInt(cursor.getString(14)),
                                              cursor.getString(15),
                                              Integer.parseInt(cursor.getString(16)),
                                              cursor.getString(17),
                                              Integer.parseInt(cursor.getString(18)),
                                              Integer.parseInt(cursor.getString(19)),
                                              Integer.parseInt(cursor.getString(20)),
                                              Integer.parseInt(cursor.getString(21)),
                                              cursor.getString(22),
                                              Integer.parseInt(cursor.getString(23)),
                                              cursor.getString(24),
                                              Integer.parseInt(cursor.getString(25)),
                                              Integer.parseInt(cursor.getString(26)),
                                              Integer.parseInt(cursor.getString(27)),
                                              Integer.parseInt(cursor.getString(28)),
                                              cursor.getString(29),
                                              Integer.parseInt(cursor.getString(30)),
                                              cursor.isNull(31) ? true : ((Integer.parseInt(cursor.getString(31)) == 1) ? true : false),
                                              Integer.parseInt(cursor.getString(32)),
                                              Integer.parseInt(cursor.getString(33)),
                                              Integer.parseInt(cursor.getString(34)),
                                              Integer.parseInt(cursor.getString(35)),
                                              Integer.parseInt(cursor.getString(36)),
                                              Integer.parseInt(cursor.getString(37)),
                                              Integer.parseInt(cursor.getString(38)),
                                              Integer.parseInt(cursor.getString(39)),
                                              Integer.parseInt(cursor.getString(40)),
                                              Integer.parseInt(cursor.getString(41)),
                                              Integer.parseInt(cursor.getString(42)),
                                              (Integer.parseInt(cursor.getString(44)) == 1) ? true : false,
                                              Integer.parseInt(cursor.getString(45)),
                                              Integer.parseInt(cursor.getString(46)),
                                              Integer.parseInt(cursor.getString(47)),
                                              Integer.parseInt(cursor.getString(48))
                                              );
            }

            cursor.close();
        }

        //db.close();

        // return profile
        return profile;
    }

    // Getting All Profiles
    public List<Profile> getAllProfiles() {
        List<Profile> profileList = new ArrayList<Profile>();

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
                                         KEY_VOLUME_ZEN_MODE + "," +
                                         KEY_DEVICE_KEYGUARD + "," +
                                         KEY_VIBRATE_ON_TOUCH + ","+
                                         KEY_DEVICE_WIFI_AP + ","+
                                         KEY_DEVICE_POWER_SAVE_MODE + "," +
                                         KEY_SHOW_DURATION_BUTTON + "," +
                                         KEY_ASK_FOR_DURATION + "," +
                                         KEY_DEVICE_NETWORK_TYPE + "," +
                                         KEY_NOTIFICATION_LED + "," +
                                         KEY_VIBRATE_WHEN_RINGING + "," +
                                         KEY_DEVICE_WALLPAPER_FOR +
                             " FROM " + TABLE_PROFILES;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Profile profile = new Profile();
                profile._id = Long.parseLong(cursor.getString(0));
                profile._name = cursor.getString(1);
                profile._icon = (cursor.getString(2));
                profile._checked = ((Integer.parseInt(cursor.getString(3)) == 1) ? true : false);
                profile._porder = (Integer.parseInt(cursor.getString(4)));
                profile._volumeRingerMode = Integer.parseInt(cursor.getString(5));
                profile._volumeRingtone = cursor.getString(6);
                profile._volumeNotification = cursor.getString(7);
                profile._volumeMedia = cursor.getString(8);
                profile._volumeAlarm = cursor.getString(9);
                profile._volumeSystem = cursor.getString(10);
                profile._volumeVoice = cursor.getString(11);
                profile._soundRingtoneChange = Integer.parseInt(cursor.getString(12));
                profile._soundRingtone = cursor.getString(13);
                profile._soundNotificationChange = Integer.parseInt(cursor.getString(14));
                profile._soundNotification = cursor.getString(15);
                profile._soundAlarmChange = Integer.parseInt(cursor.getString(16));
                profile._soundAlarm = cursor.getString(17);
                profile._deviceAirplaneMode = Integer.parseInt(cursor.getString(18));
                profile._deviceWiFi = Integer.parseInt(cursor.getString(19));
                profile._deviceBluetooth = Integer.parseInt(cursor.getString(20));
                profile._deviceScreenTimeout = Integer.parseInt(cursor.getString(21));
                profile._deviceBrightness = cursor.getString(22);
                profile._deviceWallpaperChange = Integer.parseInt(cursor.getString(23));
                profile._deviceWallpaper = cursor.getString(24);
                profile._deviceMobileData = Integer.parseInt(cursor.getString(25));
                profile._deviceMobileDataPrefs = Integer.parseInt(cursor.getString(26));
                profile._deviceGPS = Integer.parseInt(cursor.getString(27));
                profile._deviceRunApplicationChange = Integer.parseInt(cursor.getString(28));
                profile._deviceRunApplicationPackageName = cursor.getString(29);
                profile._deviceAutosync = Integer.parseInt(cursor.getString(30));
                profile._showInActivator = cursor.isNull(31) ? true : ((Integer.parseInt(cursor.getString(31)) == 1) ? true : false);
                profile._deviceAutoRotate = Integer.parseInt(cursor.getString(32));
                profile._deviceLocationServicePrefs = Integer.parseInt(cursor.getString(33));
                profile._volumeSpeakerPhone = Integer.parseInt(cursor.getString(34));
                profile._deviceNFC = Integer.parseInt(cursor.getString(35));
                profile._duration = Integer.parseInt(cursor.getString(36));
                profile._afterDurationDo = Integer.parseInt(cursor.getString(37));
                profile._volumeZenMode = Integer.parseInt(cursor.getString(38));
                profile._deviceKeyguard = Integer.parseInt(cursor.getString(39));
                profile._vibrationOnTouch = Integer.parseInt(cursor.getString(40));
                profile._deviceWiFiAP = Integer.parseInt(cursor.getString(41));
                profile._devicePowerSaveMode = Integer.parseInt(cursor.getString(42));
                profile._askForDuration = ((Integer.parseInt(cursor.getString(44)) == 1) ? true : false);
                profile._deviceNetworkType = Integer.parseInt(cursor.getString(45));
                profile._notificationLed = Integer.parseInt(cursor.getString(46));
                profile._vibrateWhenRinging = Integer.parseInt(cursor.getString(47));
                profile._deviceWallpaperFor = Integer.parseInt(cursor.getString(48));
                // Adding contact to list
                profileList.add(profile);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return profile list
        return profileList;
    }

    // Updating single profile
    public int updateProfile(Profile profile) {
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
        values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutosync);
        values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
        values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
        values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
        values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
        values.put(KEY_DEVICE_NFC, profile._deviceNFC);
        values.put(KEY_DURATION, profile._duration);
        values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
        values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
        values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
        values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
        values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
        values.put(KEY_SHOW_DURATION_BUTTON, 0);
        values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
        values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
        values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
        values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
        values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);

        // updating row
        int r = db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                        new String[] { String.valueOf(profile._id) });
        //db.close();
        
        return r;
    }

    // Deleting single profile
    public void deleteProfile(Profile profile) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();
        try {

            // unlink shortcuts from profile
            String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
            for (int i = 0; i < splits.length; i++)
            {
                boolean shortcut = ApplicationsCache.isShortcut(splits[i]);
                if (shortcut) {
                    long shortcutId = ApplicationsCache.getShortcutId(splits[i]);
                    deleteShortcut(shortcutId);
                }
            }

            db.delete(TABLE_PROFILES, KEY_ID + " = ?",
                    new String[] { String.valueOf(profile._id) });

            // unlink profile from events
            ContentValues values = new ContentValues();
            values.put(KEY_E_FK_PROFILE_START, 0);
            db.update(TABLE_EVENTS, values, KEY_E_FK_PROFILE_START + " = ?",
                    new String[]{String.valueOf(profile._id)});

            ContentValues values2 = new ContentValues();
            values2.put(KEY_E_FK_PROFILE_END, GlobalData.PROFILE_NO_ACTIVATE);
            db.update(TABLE_EVENTS, values2, KEY_E_FK_PROFILE_END + " = ?",
                    new String[]{String.valueOf(profile._id)});

            ContentValues values3 = new ContentValues();
            values3.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, GlobalData.PROFILE_NO_ACTIVATE);
            db.update(TABLE_EVENTS, values3, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                    new String[] { String.valueOf(profile._id) });

            db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }	

        //db.close();
    }

    // Deleting all profiles
    public void deleteAllProfiles() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {
            db.delete(TABLE_PROFILES, null, null);

            db.delete(TABLE_SHORTCUTS, null, null);

            // unlink profiles from events
            ContentValues values = new ContentValues();
            values.put(KEY_E_FK_PROFILE_START, 0);
            values.put(KEY_E_FK_PROFILE_END, GlobalData.PROFILE_NO_ACTIVATE);
            values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, GlobalData.PROFILE_NO_ACTIVATE);
            db.update(TABLE_EVENTS, values, null, null);

            db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }	

        //db.close();
    }

    // Getting profiles Count
    public int getProfilesCount(boolean forActivator) {
        final String countQuery;
        if (forActivator)
          countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES + " WHERE " + KEY_SHOW_IN_ACTIVATOR + "=1";
        else
          countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        if (forActivator && (!GlobalData.applicationActivatorHeader))
        {
            Profile profile = getActivatedProfile();
            if ((profile != null) && (!profile._showInActivator))
            {
                r++;
            }
        }

        return r;
    }

    // Getting max(porder)
    public int getMaxPOrder() {
        String countQuery = "SELECT MAX("+KEY_PORDER+") FROM " + TABLE_PROFILES;
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor.getCount() == 0)
        {
            r = 0;
        }
        else
        {
            if (cursor.moveToFirst())
            {
                r = cursor.getInt(0);
            }
            else
            {
                r = 0;
            }
        }

        cursor.close();
        //db.close();

        return r;

    }

    public void doActivateProfile(Profile profile, boolean activate)
    {
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

            if (activate && (profile != null))
            {
                ContentValues values = new ContentValues();
                //values.put(KEY_CHECKED, (profile.getChecked()) ? 1 : 0);
                values.put(KEY_CHECKED, 1);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

         //db.close();
    }

    public void activateProfile(Profile profile)
    {
        doActivateProfile(profile, true);
    }

    public void deactivateProfile()
    {
        doActivateProfile(null, false);
    }

    public Profile getActivatedProfile()
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Profile profile;

        Cursor cursor = db.query(TABLE_PROFILES,
                                 new String[] { KEY_ID,
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
                                                KEY_VOLUME_ZEN_MODE,
                                                KEY_DEVICE_KEYGUARD,
                                                KEY_VIBRATE_ON_TOUCH,
                                                KEY_DEVICE_WIFI_AP,
                                                KEY_DEVICE_POWER_SAVE_MODE,
                                                KEY_SHOW_DURATION_BUTTON,
                                                KEY_ASK_FOR_DURATION,
                                                KEY_DEVICE_NETWORK_TYPE,
                                                KEY_NOTIFICATION_LED,
                                                KEY_VIBRATE_WHEN_RINGING,
                                                KEY_DEVICE_WALLPAPER_FOR
                                                },
                                 KEY_CHECKED + "=?",
                                 new String[] { "1" }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            int rc = cursor.getCount();

            if (rc == 1)
            {

                profile = new Profile(Long.parseLong(cursor.getString(0)),
                                              cursor.getString(1),
                                              cursor.getString(2),
                                              (Integer.parseInt(cursor.getString(3)) == 1) ? true : false,
                                              Integer.parseInt(cursor.getString(4)),
                                              Integer.parseInt(cursor.getString(5)),
                                              cursor.getString(6),
                                              cursor.getString(7),
                                              cursor.getString(8),
                                              cursor.getString(9),
                                              cursor.getString(10),
                                              cursor.getString(11),
                                              Integer.parseInt(cursor.getString(12)),
                                              cursor.getString(13),
                                              Integer.parseInt(cursor.getString(14)),
                                              cursor.getString(15),
                                              Integer.parseInt(cursor.getString(16)),
                                              cursor.getString(17),
                                              Integer.parseInt(cursor.getString(18)),
                                              Integer.parseInt(cursor.getString(19)),
                                              Integer.parseInt(cursor.getString(20)),
                                              Integer.parseInt(cursor.getString(21)),
                                              cursor.getString(22),
                                              Integer.parseInt(cursor.getString(23)),
                                              cursor.getString(24),
                                              Integer.parseInt(cursor.getString(25)),
                                              Integer.parseInt(cursor.getString(26)),
                                              Integer.parseInt(cursor.getString(27)),
                                              Integer.parseInt(cursor.getString(28)),
                                              cursor.getString(29),
                                              Integer.parseInt(cursor.getString(30)),
                                              cursor.isNull(31) ? true : ((Integer.parseInt(cursor.getString(31)) == 1) ? true : false),
                                              Integer.parseInt(cursor.getString(32)),
                                              Integer.parseInt(cursor.getString(33)),
                                              Integer.parseInt(cursor.getString(34)),
                                              Integer.parseInt(cursor.getString(35)),
                                              Integer.parseInt(cursor.getString(36)),
                                              Integer.parseInt(cursor.getString(37)),
                                              Integer.parseInt(cursor.getString(38)),
                                              Integer.parseInt(cursor.getString(39)),
                                              Integer.parseInt(cursor.getString(40)),
                                              Integer.parseInt(cursor.getString(41)),
                                              Integer.parseInt(cursor.getString(42)),
                                              (Integer.parseInt(cursor.getString(44)) == 1) ? true : false,
                                              Integer.parseInt(cursor.getString(45)),
                                              Integer.parseInt(cursor.getString(46)),
                                              Integer.parseInt(cursor.getString(47)),
                                              Integer.parseInt(cursor.getString(48))
                                              );
            }
            else
                profile = null;

            cursor.close();
        }
        else
            profile = null;

        //db.close();

        // return profile
        return profile;

    }

    public long getProfileIdByName(String name)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        long id = 0;

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[] { KEY_ID },
                KEY_NAME + "=?",
                new String[] { name }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            int rc = cursor.getCount();

            if (rc == 1)
            {
                id = Long.parseLong(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

        // return id
        return id;

    }

    public void setPOrder(List<Profile> list)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();
        try {

            for (int i = 0; i < list.size(); i++)
            {
                Profile profile = list.get(i);
                profile._porder = i+1;

                values.put(KEY_PORDER, profile._porder);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

        //db.close();
    }

    public void setChecked(List<Profile> list)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();
        try {

            for (Profile profile : list)
            {
                values.put(KEY_CHECKED, profile._checked);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

        //db.close();
    }

    public int getActiveProfileSpeakerphone()
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[]{KEY_VOLUME_SPEAKER_PHONE},
                KEY_CHECKED + "=?",
                new String[]{"1"}, null, null, null, null);

        int speakerPhone;

        if (cursor != null)
        {
            cursor.moveToFirst();

            int rc = cursor.getCount();

            if (rc == 1)
            {
                speakerPhone = Integer.parseInt(cursor.getString(0));
            }
            else
                speakerPhone = 0;

            cursor.close();
        }
        else
            speakerPhone = 0;
        //db.close();

        return speakerPhone;
    }

    public void getProfileIcon(Profile profile)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[]{KEY_ICON},
                KEY_ID + "=?",
                new String[]{Long.toString(profile._id)}, null, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
                profile._icon = cursor.getString(0);
        }

        cursor.close();
        //db.close();

    }

    public void saveMergedProfile(Profile profile) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {
            db.delete(TABLE_MERGED_PROFILE, null, null);

            addProfile(profile, true);

            db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close();

    }

// EVENTS --------------------------------------------------------------------------------

    // Adding new event
    void addEvent(Event event) {

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_NAME, event._name); // Event Name
        values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart); // profile start
        values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd); // profile end
        values.put(KEY_E_STATUS, event.getStatus()); // event status
        values.put(KEY_E_NOTIFICATION_SOUND, event._notificationSound); // Event Name
        values.put(KEY_E_FORCE_RUN, event._forceRun ? 1 : 0); // force run when manual profile activation
        values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0); // temporary blocked
        values.put(KEY_E_PRIORITY, event._priority); // priority
        values.put(KEY_E_DELAY_START, event._delayStart); // delay for start
        values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0); // event is in delay before start
        values.put(KEY_E_AT_END_DO, event._atEndDo); //at end of event do
        values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0); // manual profile activation
        values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, event._fkProfileStartWhenActivated); // start when profile is activated
        values.put(KEY_E_DELAY_END, event._delayEnd); // delay for end
        values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0); // event is in delay after pause
        values.put(KEY_E_START_STATUS_TIME, event._startStatusTime); // time for status RUNNING
        values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime); // time for change status from RUNNING to PAUSE

        db.beginTransaction();

        try {
            // Inserting Row
            event._id = db.insert(TABLE_EVENTS, null, values);
            updateEventPreferences(event, db);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting single event
    Event getEvent(long event_id) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ID,
                        KEY_E_NAME,
                        KEY_E_FK_PROFILE_START,
                        KEY_E_FK_PROFILE_END,
                        KEY_E_STATUS,
                        KEY_E_NOTIFICATION_SOUND,
                        KEY_E_FORCE_RUN,
                        KEY_E_BLOCKED,
                        KEY_E_PRIORITY,
                        KEY_E_DELAY_START,
                        KEY_E_IS_IN_DELAY_START,
                        KEY_E_AT_END_DO,
                        KEY_E_MANUAL_PROFILE_ACTIVATION,
                        KEY_E_FK_PROFILE_START_WHEN_ACTIVATED,
                        KEY_E_DELAY_END,
                        KEY_E_IS_IN_DELAY_END,
                        KEY_E_START_STATUS_TIME,
                        KEY_E_PAUSE_STATUS_TIME
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event_id)}, null, null, null, null);

        Event event = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {

                event = new Event(Long.parseLong(cursor.getString(0)),
                                          cursor.getString(1),
                                          Long.parseLong(cursor.getString(2)),
                                          Long.parseLong(cursor.getString(3)),
                                          Integer.parseInt(cursor.getString(4)),
                                          cursor.getString(5),
                                          Integer.parseInt(cursor.getString(6)) == 1,
                                          Integer.parseInt(cursor.getString(7)) == 1,
                                          Integer.parseInt(cursor.getString(8)),
                                          Integer.parseInt(cursor.getString(9)),
                                          Integer.parseInt(cursor.getString(10)) == 1,
                                          Integer.parseInt(cursor.getString(11)),
                                          Integer.parseInt(cursor.getString(12)) == 1,
                                          Long.parseLong(cursor.getString(13)),
                                          Integer.parseInt(cursor.getString(14)),
                                          Integer.parseInt(cursor.getString(15)) == 1,
                                          Long.parseLong(cursor.getString(16)),
                                          Long.parseLong(cursor.getString(17))
                                          );
            }

            cursor.close();
        }

        if (event != null)
            getEventPreferences(event, db);

        //db.close();

        // return profile
        return event;
    }

    // Getting All Events
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<Event>();

        // Select All Query
        final String selectQuery = "SELECT " + KEY_E_ID + "," +
                                         KEY_E_NAME + "," +
                                         KEY_E_FK_PROFILE_START + "," +
                                         KEY_E_FK_PROFILE_END + "," +
                                         KEY_E_STATUS + "," +
                                         KEY_E_NOTIFICATION_SOUND + "," +
                                         KEY_E_FORCE_RUN + "," +
                                         KEY_E_BLOCKED + "," +
                                         KEY_E_PRIORITY + "," +
                                         KEY_E_DELAY_START + "," +
                                         KEY_E_IS_IN_DELAY_START + "," +
                                         KEY_E_AT_END_DO + "," +
                                         KEY_E_MANUAL_PROFILE_ACTIVATION + "," +
                                         KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + ","  +
                                         KEY_E_DELAY_END + "," +
                                         KEY_E_IS_IN_DELAY_END + "," +
                                         KEY_E_START_STATUS_TIME + "," +
                                         KEY_E_PAUSE_STATUS_TIME +
                             " FROM " + TABLE_EVENTS +
                             " ORDER BY " + KEY_E_ID;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event._id = Long.parseLong(cursor.getString(0));
                event._name = cursor.getString(1);
                event._fkProfileStart = Long.parseLong(cursor.getString(2));
                event._fkProfileEnd = Long.parseLong(cursor.getString(3));
                event.setStatus(Integer.parseInt(cursor.getString(4)));
                event._notificationSound = cursor.getString(5);
                event._forceRun = Integer.parseInt(cursor.getString(6)) == 1;
                event._blocked = Integer.parseInt(cursor.getString(7)) == 1;
                event._priority = Integer.parseInt(cursor.getString(8));
                event._delayStart = Integer.parseInt(cursor.getString(9));
                event._isInDelayStart = Integer.parseInt(cursor.getString(10)) == 1;
                event._atEndDo = Integer.parseInt(cursor.getString(11));
                event._manualProfileActivation = Integer.parseInt(cursor.getString(12)) == 1;
                event._fkProfileStartWhenActivated = Long.parseLong(cursor.getString(13));
                event._delayEnd = Integer.parseInt(cursor.getString(14));
                event._isInDelayEnd = Integer.parseInt(cursor.getString(15)) == 1;
                event._startStatusTime = Long.parseLong(cursor.getString(16));
                event._pauseStatusTime = Long.parseLong(cursor.getString(17));
                event.createEventPreferences();
                getEventPreferences(event, db);
                // Adding contact to list
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return evemt list
        return eventList;
    }

    // Updating single event
    public int updateEvent(Event event) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_NAME, event._name);
        values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart);
        values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd);
        values.put(KEY_E_STATUS, event.getStatus());
        values.put(KEY_E_NOTIFICATION_SOUND, event._notificationSound);
        values.put(KEY_E_FORCE_RUN, event._forceRun ? 1 : 0);
        values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);
        //values.put(KEY_E_UNDONE_PROFILE, 0);
        values.put(KEY_E_PRIORITY, event._priority);
        values.put(KEY_E_DELAY_START, event._delayStart);
        values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
        values.put(KEY_E_AT_END_DO, event._atEndDo);
        values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0);
        values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, event._fkProfileStartWhenActivated);
        values.put(KEY_E_DELAY_END, event._delayEnd);
        values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
        values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);
        values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                    new String[]{String.valueOf(event._id)});
            updateEventPreferences(event, db);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEvent", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;
    }

    // Deleting single event
    public void deleteEvent(Event event) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();
        db.delete(TABLE_EVENTS, KEY_E_ID + " = ?",
                new String[]{String.valueOf(event._id)});
        //db.close();
    }

    // Deleting all events
    public void deleteAllEvents() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();
        db.delete(TABLE_EVENTS, null, null);
        //db.close();
    }

    // Getting events Count
    public int getEventsCount() {
        final String countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS;
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r;
    }

    public void unlinkEventsFromProfile(Profile profile)
    {
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_E_FK_PROFILE_START, 0);
            // updating row
            db.update(TABLE_EVENTS, values, KEY_E_FK_PROFILE_START + " = ?",
                        new String[] { String.valueOf(profile._id) });

            ContentValues values2 = new ContentValues();
            values2.put(KEY_E_FK_PROFILE_END, GlobalData.PROFILE_NO_ACTIVATE);
            // updating row
            db.update(TABLE_EVENTS, values2, KEY_E_FK_PROFILE_END + " = ?",
                        new String[] { String.valueOf(profile._id) });

            ContentValues values3 = new ContentValues();
            values3.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, GlobalData.PROFILE_NO_ACTIVATE);
            // updating row
            db.update(TABLE_EVENTS, values3, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                    new String[] { String.valueOf(profile._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    public void unlinkAllEvents()
    {
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_FK_PROFILE_START, 0);
        values.put(KEY_E_FK_PROFILE_END, GlobalData.PROFILE_NO_ACTIVATE);
        values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, GlobalData.PROFILE_NO_ACTIVATE);

        // updating row
        db.update(TABLE_EVENTS, values, null, null);

        //db.close();
    }

    public void getEventPreferences(Event event) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();
        getEventPreferences(event, db);
        //db.close();
    }

    public void getEventPreferences(Event event, SQLiteDatabase db) {
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
    }

    private void getEventPreferencesTime(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_TIME_ENABLED,
                        KEY_E_DAYS_OF_WEEK,
                        KEY_E_START_TIME,
                        KEY_E_END_TIME//,
                        //KEY_E_USE_END_TIME
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesTime eventPreferences = event._eventPreferencesTime;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);

                String daysOfWeek = cursor.getString(1);

                if (daysOfWeek != null)
                {
                String[] splits = daysOfWeek.split("\\|");
                if (splits[0].equals(DaysOfWeekPreference.allValue))
                {
                    eventPreferences._sunday = true;
                    eventPreferences._monday = true;
                    eventPreferences._tuesday = true;
                    eventPreferences._wendesday = true;
                    eventPreferences._thursday = true;
                    eventPreferences._friday = true;
                    eventPreferences._saturday = true;
                }
                else
                {
                    eventPreferences._sunday = false;
                    eventPreferences._monday = false;
                    eventPreferences._tuesday = false;
                    eventPreferences._wendesday = false;
                    eventPreferences._thursday = false;
                    eventPreferences._friday = false;
                    eventPreferences._saturday = false;
                    for (String value : splits)
                    {
                        eventPreferences._sunday = eventPreferences._sunday || value.equals("0");
                        eventPreferences._monday = eventPreferences._monday || value.equals("1");
                        eventPreferences._tuesday = eventPreferences._tuesday || value.equals("2");
                        eventPreferences._wendesday = eventPreferences._wendesday || value.equals("3");
                        eventPreferences._thursday = eventPreferences._thursday || value.equals("4");
                        eventPreferences._friday = eventPreferences._friday || value.equals("5");
                        eventPreferences._saturday = eventPreferences._saturday || value.equals("6");
                    }
                }
                }
                eventPreferences._startTime = Long.parseLong(cursor.getString(2));
                eventPreferences._endTime = Long.parseLong(cursor.getString(3));
                //eventPreferences._useEndTime = (Integer.parseInt(cursor.getString(4)) == 1) ? true : false;
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
                        KEY_E_BATTERY_POWER_SAVE_MODE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._levelLow = Integer.parseInt(cursor.getString(1));
                eventPreferences._levelHight = Integer.parseInt(cursor.getString(2));
                eventPreferences._charging = (Integer.parseInt(cursor.getString(3)) == 1);
                eventPreferences._powerSaveMode = (Integer.parseInt(cursor.getString(4)) == 1);
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
                        KEY_E_CALL_CONTACT_GROUPS
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCall eventPreferences = event._eventPreferencesCall;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._callEvent = Integer.parseInt(cursor.getString(1));
                eventPreferences._contacts = cursor.getString(2);
                eventPreferences._contactListType = Integer.parseInt(cursor.getString(3));
                eventPreferences._contactGroups = cursor.getString(4);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesPeripheral(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_PERIPHERAL_ENABLED,
                        KEY_E_PERIPHERAL_TYPE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesPeripherals eventPreferences = event._eventPreferencesPeripherals;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._peripheralType = Integer.parseInt(cursor.getString(1));
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
                        KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._calendars = cursor.getString(1);
                eventPreferences._searchField = Integer.parseInt(cursor.getString(2));
                eventPreferences._searchString = cursor.getString(3);
                eventPreferences._startTime = Long.parseLong(cursor.getString(4));
                eventPreferences._endTime = Long.parseLong(cursor.getString(5));
                eventPreferences._eventFound = (Integer.parseInt(cursor.getString(6)) == 1);
                eventPreferences._availability = Integer.parseInt(cursor.getString(7));
                eventPreferences._ignoreAllDayEvents = (Integer.parseInt(cursor.getString(8)) == 1);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesWifi(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_WIFI_ENABLED,
                                                KEY_E_WIFI_SSID,
                                                KEY_E_WIFI_CONNECTION_TYPE
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._SSID = cursor.getString(1);
                eventPreferences._connectionType = Integer.parseInt(cursor.getString(2));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesScreen(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_SCREEN_ENABLED,
                                                KEY_E_SCREEN_EVENT_TYPE,
                                                KEY_E_SCREEN_WHEN_UNLOCKED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._eventType = Integer.parseInt(cursor.getString(1));
                eventPreferences._whenUnlocked = (Integer.parseInt(cursor.getString(2)) == 1);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_BLUETOOTH_ENABLED,
                                                KEY_E_BLUETOOTH_ADAPTER_NAME,
                                                KEY_E_BLUETOOTH_CONNECTION_TYPE,
                                                KEY_E_BLUETOOTH_DEVICES_TYPE
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._adapterName = cursor.getString(1);
                eventPreferences._connectionType = Integer.parseInt(cursor.getString(2));
                eventPreferences._devicesType = Integer.parseInt(cursor.getString(3));
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
                        KEY_E_SMS_DURATION
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                //eventPreferences._smsEvent = Integer.parseInt(cursor.getString(1));
                eventPreferences._contacts = cursor.getString(1);
                eventPreferences._contactListType = Integer.parseInt(cursor.getString(2));
                eventPreferences._startTime = Long.parseLong(cursor.getString(3));
                eventPreferences._contactGroups = cursor.getString(4);
                eventPreferences._duration = cursor.getInt(5);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesNotification(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_NOTIFICATION_ENABLED,
                        KEY_E_NOTIFICATION_APPLICATIONS,
                        KEY_E_NOTIFICATION_START_TIME,
                        KEY_E_NOTIFICATION_DURATION,
                        KEY_E_NOTIFICATION_END_WHEN_REMOVED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._applications = cursor.getString(1);
                eventPreferences._startTime = Long.parseLong(cursor.getString(2));
                eventPreferences._duration = cursor.getInt(3);
                eventPreferences._endWhenRemoved = (Integer.parseInt(cursor.getString(4)) == 1);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesApplication(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_APPLICATION_ENABLED,
                        KEY_E_APPLICATION_APPLICATIONS /*,
                        KEY_E_NOTIFICATION_START_TIME,
                        KEY_E_NOTIFICATION_DURATION*/
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._applications = cursor.getString(1);
                //eventPreferences._startTime = Long.parseLong(cursor.getString(2));
                //eventPreferences._duration = cursor.getInt(3);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesLocation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_LOCATION_ENABLED,
                        KEY_E_LOCATION_FK_GEOFENCE,
                        KEY_E_LOCATION_WHEN_OUTSIDE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._geofenceId = cursor.getLong(1);
                eventPreferences._whenOutside = cursor.getInt(2) == 1;

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
                        KEY_E_ORIENTATION_IGNORE_APPLICATIONS
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._sides = cursor.getString(1);
                eventPreferences._distance = cursor.getInt(2);
                eventPreferences._display = cursor.getString(3);
                eventPreferences._ignoredApplications = cursor.getString(4);
            }
            cursor.close();
        }
    }

    private void getEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_MOBILE_CELLS_ENABLED,
                        KEY_E_MOBILE_CELLS_CELLS,
                        KEY_E_MOBILE_CELLS_WHEN_OUTSIDE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

                eventPreferences._enabled = (Integer.parseInt(cursor.getString(0)) == 1);
                eventPreferences._cells = cursor.getString(1);
                eventPreferences._whenOutside = cursor.getInt(2) == 1;

            }
            cursor.close();
        }
    }


    public int updateEventPreferences(Event event) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();
        int r = updateEventPreferences(event, db);
        //db.close();
        return r;
    }

    public int updateEventPreferences(Event event, SQLiteDatabase db) {
        int r;

        r = updateEventPreferencesTime(event, db);
        if (r != 0)
            r = updateEventPreferencesBattery(event, db);
        if (r != 0)
            r = updateEventPreferencesCall(event, db);
        if (r != 0)
            r = updateEventPreferencesPeripheral(event, db);
        if (r != 0)
            r = updateEventPreferencesCalendar(event, db);
        if (r != 0)
            r = updateEventPreferencesWifi(event, db);
        if (r != 0)
            r = updateEventPreferencesScreen(event, db);
        if (r != 0)
            r = updateEventPreferencesBluetooth(event, db);
        if (r != 0)
            r = updateEventPreferencesSMS(event, db);
        if (r != 0)
            r = updateEventPreferencesNotification(event, db);
        if (r != 0)
            r = updateEventPreferencesApplication(event, db);
        if (r != 0)
            r = updateEventPreferencesLocation(event, db);
        if (r != 0)
            r = updateEventPreferencesOrientation(event, db);
        if (r != 0)
            r = updateEventPreferencesMobileCells(event, db);

        return r;
    }

    private int updateEventPreferencesTime(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesTime eventPreferences = event._eventPreferencesTime;

        String daysOfWeek = "";
        if (eventPreferences._sunday) daysOfWeek = daysOfWeek + "0|";
        if (eventPreferences._monday) daysOfWeek = daysOfWeek + "1|";
        if (eventPreferences._tuesday) daysOfWeek = daysOfWeek + "2|";
        if (eventPreferences._wendesday) daysOfWeek = daysOfWeek + "3|";
        if (eventPreferences._thursday) daysOfWeek = daysOfWeek + "4|";
        if (eventPreferences._friday) daysOfWeek = daysOfWeek + "5|";
        if (eventPreferences._saturday) daysOfWeek = daysOfWeek + "6|";

        values.put(KEY_E_TIME_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_DAYS_OF_WEEK, daysOfWeek);
        values.put(KEY_E_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_END_TIME, eventPreferences._endTime);
        //values.put(KEY_E_USE_END_TIME, (eventPreferences._useEndTime) ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesBattery(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

        values.put(KEY_E_BATTERY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BATTERY_LEVEL_LOW, eventPreferences._levelLow);
        values.put(KEY_E_BATTERY_LEVEL_HIGHT, eventPreferences._levelHight);
        values.put(KEY_E_BATTERY_CHARGING, eventPreferences._charging ? 1 : 0);
        values.put(KEY_E_BATTERY_POWER_SAVE_MODE, eventPreferences._powerSaveMode ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesCall(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCall eventPreferences = event._eventPreferencesCall;

        values.put(KEY_E_CALL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_CALL_EVENT, eventPreferences._callEvent);
        values.put(KEY_E_CALL_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_CALL_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_CALL_CONTACT_GROUPS, eventPreferences._contactGroups);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesPeripheral(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesPeripherals eventPreferences = event._eventPreferencesPeripherals;

        values.put(KEY_E_PERIPHERAL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_PERIPHERAL_TYPE, eventPreferences._peripheralType);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesCalendar(Event event, SQLiteDatabase db) {
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

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesWifi(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

        values.put(KEY_E_WIFI_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_WIFI_SSID, eventPreferences._SSID);
        values.put(KEY_E_WIFI_CONNECTION_TYPE, eventPreferences._connectionType);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesScreen(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

        values.put(KEY_E_SCREEN_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_SCREEN_EVENT_TYPE, eventPreferences._eventType);
        values.put(KEY_E_SCREEN_WHEN_UNLOCKED, (eventPreferences._whenUnlocked) ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

        values.put(KEY_E_BLUETOOTH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BLUETOOTH_ADAPTER_NAME, eventPreferences._adapterName);
        values.put(KEY_E_BLUETOOTH_CONNECTION_TYPE, eventPreferences._connectionType);
        values.put(KEY_E_BLUETOOTH_DEVICES_TYPE, eventPreferences._devicesType);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesSMS(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

        values.put(KEY_E_SMS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        //values.put(KEY_E_SMS_EVENT, eventPreferences._smsEvent);
        values.put(KEY_E_SMS_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_SMS_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_SMS_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_SMS_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_SMS_DURATION, eventPreferences._duration);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
        
        return r;
    }

    private int updateEventPreferencesNotification(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

        values.put(KEY_E_NOTIFICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_APPLICATIONS, eventPreferences._applications);
        values.put(KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);
        values.put(KEY_E_NOTIFICATION_END_WHEN_REMOVED, (eventPreferences._endWhenRemoved) ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

        return r;
    }

    private int updateEventPreferencesApplication(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

        values.put(KEY_E_APPLICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_APPLICATION_APPLICATIONS, eventPreferences._applications);
        //values.put(KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        //values.put(KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

        return r;
    }

    private int updateEventPreferencesLocation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

        values.put(KEY_E_LOCATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_LOCATION_FK_GEOFENCE, eventPreferences._geofenceId);
        values.put(KEY_E_LOCATION_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

        return r;
    }

    private int updateEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

        values.put(KEY_E_ORIENTATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ORIENTATION_SIDES, eventPreferences._sides);
        values.put(KEY_E_ORIENTATION_DISTANCE, eventPreferences._distance);
        values.put(KEY_E_ORIENTATION_DISPLAY, eventPreferences._display);
        values.put(KEY_E_ORIENTATION_IGNORE_APPLICATIONS, eventPreferences._ignoredApplications);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

        return r;
    }

    private int updateEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

        values.put(KEY_E_MOBILE_CELLS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);
        values.put(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);

        // updating row
        int r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

        return r;
    }

    public int getEventStatus(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        int eventStatus = 0;

        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] {
                                                KEY_E_STATUS
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                eventStatus = Integer.parseInt(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

        return eventStatus;

    }

    public int updateEventStatus(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        int status = event.getStatus();
        ContentValues values = new ContentValues();
        values.put(KEY_E_STATUS, status);
        if (status == Event.ESTATUS_RUNNING)
            values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);
        if (status == Event.ESTATUS_PAUSE)
            values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEventStatus", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public int updateEventBlocked(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEventBlocked", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public int unblockAllEvents()
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_BLOCKED, 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating rows
            r = db.update(TABLE_EVENTS, values, null, null);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.unblockAllEvents", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public int updateAllEventsStatus(int fromStatus, int toStatus)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_STATUS, toStatus);

        int r = 0;

        db.beginTransaction();

        try {
            // updating rows
            r = db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                    new String[] { String.valueOf(fromStatus) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateAllEventsStatus", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public int getTypeEventsCount(int eventType)
    {
        final String countQuery;
        String eventTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
        if (eventType == ETYPE_TIME)
            eventTypeChecked = eventTypeChecked + KEY_E_TIME_ENABLED + "=1";
        else
        if (eventType == ETYPE_BATTERY)
            eventTypeChecked = eventTypeChecked + KEY_E_BATTERY_ENABLED + "=1";
        else
        if (eventType == ETYPE_CALL)
            eventTypeChecked = eventTypeChecked + KEY_E_CALL_ENABLED + "=1";
        else
        if (eventType == ETYPE_PERIPHERAL)
            eventTypeChecked = eventTypeChecked + KEY_E_PERIPHERAL_ENABLED + "=1";
        else
        if (eventType == ETYPE_CALENDAR)
            eventTypeChecked = eventTypeChecked + KEY_E_CALENDAR_ENABLED + "=1";
        else
        if (eventType == ETYPE_WIFICONNECTED)
            eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                    "(" + KEY_E_WIFI_CONNECTION_TYPE + "=0 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=2)";
        else
        if (eventType == ETYPE_WIFIINFRONT)
            eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                    "(" + KEY_E_WIFI_CONNECTION_TYPE + "=1 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=3)";
        else
        if (eventType == ETYPE_SCREEN)
            eventTypeChecked = eventTypeChecked + KEY_E_SCREEN_ENABLED + "=1";
        else
        if (eventType == ETYPE_BLUETOOTHCONNECTED)
            eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                    "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=2)";
        else
        if (eventType == ETYPE_BLUETOOTHINFRONT)
            eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                    "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3)";
        else
        if (eventType == ETYPE_SMS)
            eventTypeChecked = eventTypeChecked + KEY_E_SMS_ENABLED + "=1";
        else
        if (eventType == ETYPE_NOTIFICATION)
            eventTypeChecked = eventTypeChecked + KEY_E_NOTIFICATION_ENABLED + "=1";
        else
        if (eventType == ETYPE_APPLICATION)
            eventTypeChecked = eventTypeChecked + KEY_E_APPLICATION_ENABLED + "=1";
        else
        if (eventType == ETYPE_LOCATION)
            eventTypeChecked = eventTypeChecked + KEY_E_LOCATION_ENABLED + "=1";
        else
        if (eventType == ETYPE_ORIENTATION)
            eventTypeChecked = eventTypeChecked + KEY_E_ORIENTATION_ENABLED + "=1";
        else
        if (eventType == ETYPE_MOBILE_CELLS)
            eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1";

        countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                     " WHERE " + eventTypeChecked;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r;
    }

    public int updateEventCalendarTimes(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_CALENDAR_EVENT_START_TIME, event._eventPreferencesCalendar._startTime);
        values.put(KEY_E_CALENDAR_EVENT_END_TIME, event._eventPreferencesCalendar._endTime);
        values.put(KEY_E_CALENDAR_EVENT_FOUND, event._eventPreferencesCalendar._eventFound ? 1 : 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEventCalendarTimes", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public void setEventCalendarTimes(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] {
                                                KEY_E_CALENDAR_EVENT_START_TIME,
                                                KEY_E_CALENDAR_EVENT_END_TIME,
                                                KEY_E_CALENDAR_EVENT_FOUND
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                event._eventPreferencesCalendar._startTime = Long.parseLong(cursor.getString(0));
                event._eventPreferencesCalendar._endTime = Long.parseLong(cursor.getString(1));
                event._eventPreferencesCalendar._eventFound = (Integer.parseInt(cursor.getString(2)) == 1);
            }

            cursor.close();
        }

        //db.close();

    }

    public boolean isSSIDScanned(String SSID, int connectionType)
    {
        final String countQuery;
        String eventTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events

        eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                                                KEY_E_WIFI_CONNECTION_TYPE + "=" + connectionType+ " AND " +
                                                "\"" + SSID + "\" LIKE " + KEY_E_WIFI_SSID + " ESCAPE '\\'";

        countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                     " WHERE " + eventTypeChecked;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r != 0;
    }

    public boolean getEventInDelayStart(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        int eventInDelay = 0;

        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] {
                                                KEY_E_IS_IN_DELAY_START
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                eventInDelay = Integer.parseInt(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

        return (eventInDelay == 1);

    }

    public int updateEventInDelayStart(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEventInDelayStart", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public int resetAllEventsInDelayStart()
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_IS_IN_DELAY_START, 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating rows
            r = db.update(TABLE_EVENTS, values, null, null);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.resetAllEventsInDelayStart", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public boolean getEventInDelayEnd(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        int eventInDelay = 0;

        Cursor cursor = db.query(TABLE_EVENTS,
                new String[] {
                        KEY_E_IS_IN_DELAY_END
                },
                KEY_E_ID + "=?",
                new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                eventInDelay = Integer.parseInt(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

        return (eventInDelay == 1);

    }

    public int updateEventInDelayEnd(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                    new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEventInDelayEnd", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;

    }

    public int resetAllEventsInDelayEnd()
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_IS_IN_DELAY_END, 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating rows
            r = db.update(TABLE_EVENTS, values, null, null);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.resetAllEventsInDelayEnd", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;

    }

    public boolean isBluetoothAdapterNameScanned(String adapterName, int connectionType)
    {
        final String countQuery;
        String eventTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events

        eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                                                KEY_E_BLUETOOTH_CONNECTION_TYPE + "=" + connectionType + " AND " +
                                                "\"" + adapterName + "\" LIKE " + KEY_E_BLUETOOTH_ADAPTER_NAME + " ESCAPE '\\'";


        countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                     " WHERE " + eventTypeChecked;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r != 0;
    }

    public int updateSMSStartTime(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_SMS_START_TIME, event._eventPreferencesSMS._startTime);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateSMSStartTimes", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();
        
        return r;

    }

    public void setSMSStartTime(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] {
                                                KEY_E_SMS_START_TIME
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                event._eventPreferencesSMS._startTime = Long.parseLong(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

    }

    public int updateNotificationStartTime(Event event)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_NOTIFICATION_START_TIME, event._eventPreferencesNotification._startTime);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                    new String[] { String.valueOf(event._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateNotificationStartTimes", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;

    }

    public void setNotificationStartTime(Event event)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS,
                new String[] {
                        KEY_E_NOTIFICATION_START_TIME
                },
                KEY_E_ID + "=?",
                new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                event._eventPreferencesNotification._startTime = Long.parseLong(cursor.getString(0));
            }

            cursor.close();
        }

        //db.close();

    }

    public int getBluetoothDevicesTypeCount(int devicesType, int forceScan)
    {
        if (forceScan != GlobalData.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            final String countQuery;
            String devicesTypeChecked = "";
            devicesTypeChecked = devicesTypeChecked + KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
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

            int r;

            if (cursor != null) {
                cursor.moveToFirst();
                r = Integer.parseInt(cursor.getString(0));
            } else
                r = 0;

            cursor.close();
            //db.close();

            return r;
        }
        else
            return 999;
    }


// EVENT TIMELINE ------------------------------------------------------------------

    // Adding time line
    void addEventTimeline(EventTimeline eventTimeline) {

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ET_FK_EVENT, eventTimeline._fkEvent); // Event id
        values.put(KEY_ET_FK_PROFILE_RETURN, eventTimeline._fkProfileEndActivated); // Profile id returned on pause/stop event
        values.put(KEY_ET_EORDER, getMaxEOrderET()+1); // event running order

        db.beginTransaction();

        try {
            // Inserting Row
            eventTimeline._id = db.insert(TABLE_EVENT_TIMELINE, null, values);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting max(eorder)
    public int getMaxEOrderET() {
        String countQuery = "SELECT MAX("+KEY_ET_EORDER+") FROM " + TABLE_EVENT_TIMELINE;
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor.getCount() == 0)
        {
            r = 0;
        }
        else
        {
            if (cursor.moveToFirst())
            {
                r = cursor.getInt(0);
            }
            else
            {
                r = 0;
            }
        }

        cursor.close();
        //db.close();

        return r;

    }

    // Getting all event timeline
    public List<EventTimeline> getAllEventTimelines() {
        List<EventTimeline> eventTimelineList = new ArrayList<EventTimeline>();

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

                eventTimeline._id = Long.parseLong(cursor.getString(0));
                eventTimeline._fkEvent = Long.parseLong(cursor.getString(1));
                eventTimeline._fkProfileEndActivated = Long.parseLong(cursor.getString(2));
                eventTimeline._eorder = Integer.parseInt(cursor.getString(3));

                // Adding event timeline to list
                eventTimelineList.add(eventTimeline);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return event timeline list
        return eventTimelineList;
    }

    // Deleting event timeline
    public void deleteEventTimeline(EventTimeline eventTimeline) {

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();
        db.delete(TABLE_EVENT_TIMELINE, KEY_ET_ID + " = ?",
                new String[]{String.valueOf(eventTimeline._id)});
        //db.close();
    }

    // Deleting all events from timeline
    public void deleteAllEventTimelines(boolean updateEventStatus) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_E_STATUS, Event.ESTATUS_PAUSE);

        db.beginTransaction();

        try {

            db.delete(TABLE_EVENT_TIMELINE, null, null);

            if (updateEventStatus)
            {
                db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                    new String[] { String.valueOf(Event.ESTATUS_RUNNING) });
            }

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteAllEventTimelines", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    // Getting Count in timelines
    public int getEventTimelineCount() {
        final String countQuery = "SELECT  count(*) FROM " + TABLE_EVENT_TIMELINE;
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r;
    }

    public void updateProfileReturnET(List<EventTimeline> eventTimelineList)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();

        try {

            for (EventTimeline eventTimeline : eventTimelineList)
            {
                values.put(KEY_ET_FK_PROFILE_RETURN, eventTimeline._fkProfileEndActivated);

                // updating row
                db.update(TABLE_EVENT_TIMELINE, values, KEY_ET_ID + " = ?",
                    new String[] { String.valueOf(eventTimeline._id) });
            }

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateProfileReturnET", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
        
    }

// ACTIVITY LOG -------------------------------------------------------------------

    // Adding activity log
    public void addActivityLog(int logType, String eventName, String profileName, String profileIcon,
                        int durationDelay) {

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
            // delete older than 7 days old records
            db.delete(TABLE_ACTIVITY_LOG, KEY_AL_LOG_DATE_TIME + " < date('now','-7 days')", null);

            // Inserting Row
            db.insert(TABLE_ACTIVITY_LOG, null, values);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    public void clearActivityLog() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        //db.beginTransaction();

        try {
            db.delete(TABLE_ACTIVITY_LOG, null, null);

           // db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            //db.endTransaction();
        }

        //db.close();
    }

    public Cursor getActivityLogCursor() {

        final String selectQuery = "SELECT " + KEY_AL_ID + "," +
                                               KEY_AL_LOG_DATE_TIME + "," +
                                               KEY_AL_LOG_TYPE + "," +
                                               KEY_AL_EVENT_NAME + "," +
                                               KEY_AL_PROFILE_NAME + "," +
                                               KEY_AL_PROFILE_ICON + "," +
                                               KEY_AL_DURATION_DELAY +
                                    " FROM " + TABLE_ACTIVITY_LOG +
                                " ORDER BY " + KEY_AL_LOG_DATE_TIME + " DESC";

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        return db.rawQuery(selectQuery, null);
    }

// GEOFENCES ----------------------------------------------------------------------

    // Adding new geofence
    void addGeofence(Geofence geofence) {

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

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting single geofence
    Geofence getGeofence(long geofenceId) {
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

        Geofence geofence = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                geofence = new Geofence();
                geofence._id = Long.parseLong(cursor.getString(0));
                geofence._name = cursor.getString(1);
                geofence._latitude = cursor.getDouble(2);
                geofence._longitude = cursor.getDouble(3);
                geofence._radius = cursor.getFloat(4);
            }

            cursor.close();
        }

        //db.close();

        return geofence;
    }

    // Getting All geofences
    public List<Geofence> getAllGeofences() {
        List<Geofence> geofenceList = new ArrayList<Geofence>();

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
                geofence._id = Long.parseLong(cursor.getString(0));
                geofence._name = cursor.getString(1);
                geofence._latitude = cursor.getDouble(2);
                geofence._longitude = cursor.getDouble(3);
                geofence._radius = cursor.getFloat(4);
                geofenceList.add(geofence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        return geofenceList;
    }

    // Updating single geofence
    public int updateGeofence(Geofence geofence) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_G_NAME, geofence._name);
        values.put(KEY_G_LATITUDE, geofence._latitude);
        values.put(KEY_G_LONGITUDE, geofence._longitude);
        values.put(KEY_G_RADIUS, geofence._radius);
        values.put(KEY_G_CHECKED, 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?",
                    new String[] { String.valueOf(geofence._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEvent", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;
    }

    public void updateGeofenceTransition(long geofenceId, int geofenceTransition) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        //db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_G_TRANSITION, geofenceTransition);
            db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{String.valueOf(geofenceId)});

            //db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateGeofenceTransition", e.toString());
        } finally {
            //db.endTransaction();
        }

        //db.close();
    }

    public void clearAllGeofenceTransitions() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        //db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_G_TRANSITION, 0);
            db.update(TABLE_GEOFENCES, values, null, null);

            //db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.clearAllGeofenceTransitions", e.toString());
        } finally {
            //db.endTransaction();
        }

        //db.close();
    }

    // Deleting single geofence
    public void deleteGeofence(long geofenceId) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            // delete geofence
            db.delete(TABLE_GEOFENCES, KEY_G_ID + " = ?",
                    new String[]{String.valueOf(geofenceId)});

            // unlink geofence from events
            ContentValues values = new ContentValues();
            values.put(KEY_E_LOCATION_FK_GEOFENCE, 0);
            db.update(TABLE_EVENTS, values, KEY_E_LOCATION_FK_GEOFENCE + " = ? AND " + KEY_E_LOCATION_ENABLED + "=0",
                    new String[]{String.valueOf(geofenceId)});

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteGeofence", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }
    public void deleteGeofence(Geofence geofence) {
        deleteGeofence(geofence._id);
    }

    // Deleting all geofences
    public void deleteAllGeofences() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            db.delete(TABLE_GEOFENCES, null, null);

            ContentValues values = new ContentValues();
            values.put(KEY_E_LOCATION_FK_GEOFENCE, 0);
            db.update(TABLE_EVENTS, values, KEY_E_LOCATION_FK_GEOFENCE + "=0", null);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteGeofence", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    public void checkGeofence(long geofenceId) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();

        try {
            // uncheck geofences
            values.clear();
            values.put(KEY_G_CHECKED, 0);
            db.update(TABLE_GEOFENCES, values, null, null);

            if (geofenceId > 0) {
                // check geofence
                values.clear();
                values.put(KEY_G_CHECKED, 1);
                db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?",
                        new String[]{String.valueOf(geofenceId)});
            }

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.checkGeofence", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    public Cursor getGeofencesCursor() {

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

        return db.rawQuery(selectQuery, null);
    }

    public String getGeofenceName(long geofenceId) {
        final String countQuery = "SELECT " + KEY_G_NAME +
                                   " FROM " + TABLE_GEOFENCES +
                                  " WHERE " + KEY_G_ID + "=" + String.valueOf(geofenceId);

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        String r = "";

        if (cursor != null)
        {
            if (cursor.moveToFirst())
                r = cursor.getString(0);
        }

        cursor.close();
        //db.close();

        return r;
    }

    public int getCheckedGeofence() {
        final String countQuery = "SELECT " + KEY_G_ID +
                                   " FROM " + TABLE_GEOFENCES +
                                  " WHERE " + KEY_G_CHECKED + "=1";

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r = 0;

        if (cursor != null)
        {
            if (cursor.moveToFirst())
                r = cursor.getInt(0);
        }

        cursor.close();
        //db.close();

        return r;
    }

    public int getGeofencePosition(long geofenceId) {
        final String selectQuery = "SELECT " + KEY_G_ID +
                                    " FROM " + TABLE_GEOFENCES +
                                " ORDER BY " + KEY_G_NAME + " ASC";

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        int position = -1;
        if (cursor.moveToFirst()) {
            do {
                ++position;
                if (cursor.getLong(0) == geofenceId) {
                    break;
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return event timeline list
        return position;
    }

    public int getGeofenceCount() {
        String countQuery = "SELECT  count(*) FROM " + TABLE_GEOFENCES;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r;
    }

    public boolean isGeofenceUsed(long geofenceId, boolean onlyEnabledEvents) {
        String countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                            " WHERE " + KEY_E_LOCATION_FK_GEOFENCE + "=" + String.valueOf(geofenceId) +
                            " AND " + KEY_E_LOCATION_ENABLED + "=1";
        if (onlyEnabledEvents)
            countQuery = countQuery + " AND " + KEY_E_STATUS + " IN (" +
                    String.valueOf(Event.ESTATUS_PAUSE) + "," +
                    String.valueOf(Event.ESTATUS_RUNNING) + ")";

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
        }
        else
            r = 0;

        cursor.close();
        //db.close();

        return r > 0;
    }

    public int getGeofenceTransition(long geofenceId) {
        final String countQuery = "SELECT " + KEY_G_TRANSITION +
                                    " FROM " + TABLE_GEOFENCES +
                                    " WHERE " + KEY_G_ID + "=" + String.valueOf(geofenceId);

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r = 0;

        if (cursor != null)
        {
            if (cursor.moveToFirst())
                r = cursor.getInt(0);
        }

        cursor.close();
        //db.close();

        return r;
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    void addShortcut(Shortcut shortcut) {

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

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting single shortcut
    Shortcut getShortcut(long shortcutId) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_SHORTCUTS,
                new String[]{KEY_S_ID,
                        KEY_S_INTENT,
                        KEY_S_NAME
                },
                KEY_S_ID + "=?",
                new String[]{String.valueOf(shortcutId)}, null, null, null, null);

        Shortcut shortcut = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                shortcut = new Shortcut();
                shortcut._id = Long.parseLong(cursor.getString(0));
                shortcut._intent = cursor.getString(1);
                shortcut._name = cursor.getString(2);
            }

            cursor.close();
        }

        //db.close();

        return shortcut;
    }

    // Updating single shortcut
    public int updateShortcut(Shortcut shortcut) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_S_INTENT, shortcut._intent);
        values.put(KEY_S_NAME, shortcut._name);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_SHORTCUTS, values, KEY_S_ID + " = ?",
                    new String[] { String.valueOf(shortcut._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateShortcut", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;
    }

    // Deleting single shortcut
    public void deleteShortcut(long shortcutId) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            // delete geofence
            db.delete(TABLE_SHORTCUTS, KEY_S_ID + " = ?",
                    new String[]{String.valueOf(shortcutId)});

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteShortcut", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

// MOBILE_CELLS ----------------------------------------------------------------------

    // Adding new mobile cell
    void addMobileCell(MobileCell mobileCell) {

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MC_CELL_ID, mobileCell._cellId);
        values.put(KEY_MC_NAME, mobileCell._name);
        values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);

        db.beginTransaction();

        try {
            // Inserting Row
            mobileCell._id = db.insert(TABLE_MOBILE_CELLS, null, values);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting single mobile cell
    MobileCell getMobileCell(long mobileCellId) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_MOBILE_CELLS,
                new String[]{KEY_MC_ID,
                        KEY_MC_CELL_ID,
                        KEY_MC_NAME,
                        KEY_MC_NEW
                },
                KEY_MC_ID + "=?",
                new String[]{String.valueOf(mobileCellId)}, null, null, null, null);

        MobileCell mobileCell = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                mobileCell = new MobileCell();
                mobileCell._id = Long.parseLong(cursor.getString(0));
                mobileCell._cellId = cursor.getInt(1);
                mobileCell._name = cursor.getString(2);
                mobileCell._new = cursor.getInt(3) == 1;
            }

            cursor.close();
        }

        //db.close();

        return mobileCell;
    }

    // Updating single mobile cell
    public int updateMobileCell(MobileCell mobileCell) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MC_CELL_ID, mobileCell._cellId);
        values.put(KEY_MC_NAME, mobileCell._name);
        values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_MOBILE_CELLS, values, KEY_MC_ID + " = ?",
                    new String[] { String.valueOf(mobileCell._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateMobileCell", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;
    }

    // Deleting single mobile cell
    public void deleteMobileCellId(long mobileCellId) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            // delete geofence
            db.delete(TABLE_MOBILE_CELLS, KEY_MC_ID + " = ?",
                    new String[]{String.valueOf(mobileCellId)});

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteMobileCell", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    public void deleteMobileCell(int mobileCell) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            // delete geofence
            db.delete(TABLE_MOBILE_CELLS, KEY_MC_CELL_ID + " = ?",
                    new String[]{String.valueOf(mobileCell)});

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteMobileCell", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    // add mobile cells to list
    public void addMobileCellsToList(List<MobileCellsData> cellsList) {
        // Select All Query
        final String selectQuery = "SELECT " + KEY_MC_CELL_ID + "," +
                            KEY_MC_NAME + "," +
                            KEY_MC_NEW +
                " FROM " + TABLE_MOBILE_CELLS;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                int cellId = cursor.getInt(0);
                String name = cursor.getString(1);
                boolean _new = cursor.getInt(2) == 1;
                Log.d("DatabaseHandler.addMobileCellsToList", "cellId="+cellId + " new="+_new);
                boolean found = false;
                for (MobileCellsData cell : cellsList) {
                    if (cell.cellId == cellId) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MobileCellsData cell = new MobileCellsData(cellId, name, false, _new);
                    cellsList.add(cell);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();
    }

    public void saveMobileCellsList(List<MobileCellsData> cellsList, boolean _new) {

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
                    String dbCellId = Integer.toString(cursor.getInt(1));
                    if (dbCellId.equals(Integer.toString(cell.cellId))) {
                        foundedDbId = cursor.getLong(0);
                        found = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
            if (!found) {
                Log.d("DatabaseHandler.saveMobileCellsList", "!found");
                MobileCell mobileCell = new MobileCell();
                mobileCell._cellId = cell.cellId;
                mobileCell._name = cell.name;
                mobileCell._new = true;
                addMobileCell(mobileCell);
            } else {
                Log.d("DatabaseHandler.saveMobileCellsList", "found="+foundedDbId+" cell.new="+cell._new+" new="+_new);
                MobileCell mobileCell = new MobileCell();
                mobileCell._id = foundedDbId;
                mobileCell._cellId = cell.cellId;
                mobileCell._name = cell.name;
                mobileCell._new = _new && cell._new;
                updateMobileCell(mobileCell);
            }
        }

        cursor.close();
        //db.close();

    }

// OTHERS -------------------------------------------------------------------------

    public boolean tableExists(String tableName, SQLiteDatabase db)
    {
        @SuppressWarnings("unused")
        Cursor c = null;

        boolean tableExists = false;

        /* get cursor on it */
        try
        {
            c = db.query(tableName, null,
                null, null, null, null, null);
            tableExists = true;
        }
        catch (Exception e) {
            /* not exists ? */
        }

        return tableExists;
    }

    //@SuppressWarnings("resource")
    public int importDB(String applicationDataPath) {
        int ret = 0;
        List<Long> exportedDBEventProfileIds = new ArrayList<Long>();
        List<Long> importDBEventProfileIds = new ArrayList<Long>();
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
                // zistenie verzie zalohy
                SQLiteDatabase exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

                // db z SQLiteOpenHelper
                //SQLiteDatabase db = this.getWritableDatabase();
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

                    if (cursorExportedDB.moveToFirst()) {
                        do {
                            values.clear();
                            for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                    String value = cursorExportedDB.getString(i);

                                    // update values
                                    if (((exportedDBObj.getVersion() < 1002) && (applicationDataPath.equals(GlobalData.EXPORT_PATH)))
                                            ||
                                            ((exportedDBObj.getVersion() < 52) && (applicationDataPath.equals(GUIData.REMOTE_EXPORT_PATH)))) {
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
                                            if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                                            {
                                                //value|noChange|automatic|defaultProfile
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
                                            }
                                        }
                                    }
                                    if (exportedDBObj.getVersion() < 1165) {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS)) {
                                            //value|noChange|automatic|defaultProfile
                                            String[] splits = value.split("\\|");

                                            int perc = Integer.parseInt(splits[0]);
                                            perc = (int) Profile.convertBrightnessToPercents(perc, 255, 1, context);

                                            // hm, found brightness values without default profile :-/
                                            if (splits.length == 4)
                                                value = perc + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                                            else
                                                value = perc + "|" + splits[1] + "|" + splits[2] + "|0";
                                        }
                                    }
                                    if (exportedDBObj.getVersion() < 1175) {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS)) {
                                            if (android.os.Build.VERSION.SDK_INT < 21) {
                                                //value|noChange|automatic|defaultProfile
                                                String[] splits = value.split("\\|");

                                                if (splits[2].equals("1")) // automatic is set
                                                {
                                                    int perc = 50;

                                                    // hm, found brightness values without default profile :-/
                                                    if (splits.length == 4)
                                                        value = perc + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                                                    else
                                                        value = perc + "|" + splits[1] + "|" + splits[2] + "|0";
                                                }
                                            }
                                        }
                                    }

                                    values.put(columnNamesExportedDB[i], value);
                                }
                                if (columnNamesExportedDB[i].equals(KEY_DURATION))
                                    duration = cursorExportedDB.getInt(i);
                                if (columnNamesExportedDB[i].equals(KEY_VOLUME_ZEN_MODE))
                                    zenMode = cursorExportedDB.getInt(i);
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
                            if (applicationDataPath.equals(GUIData.REMOTE_EXPORT_PATH)
                                    ||
                                    ((exportedDBObj.getVersion() < 26) && (applicationDataPath.equals(GlobalData.EXPORT_PATH)))) {
                                values.put(KEY_SHOW_IN_ACTIVATOR, 1);
                            }
                            if (((exportedDBObj.getVersion() < 1001) && (applicationDataPath.equals(GlobalData.EXPORT_PATH)))
                                    ||
                                    ((exportedDBObj.getVersion() < 51) && (applicationDataPath.equals(GUIData.REMOTE_EXPORT_PATH)))) {
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
                                values.put(KEY_AFTER_DURATION_DO, 0);
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
                            if (exportedDBObj.getVersion() < 1550) {
                                values.put(KEY_SHOW_DURATION_BUTTON, 0);
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

                            ///////////////////////////////////////////////////////

                            // Inserting Row do db z SQLiteOpenHelper
                            profileId = db.insert(TABLE_PROFILES, null, values);
                            // save profile ids
                            exportedDBEventProfileIds.add(cursorExportedDB.getLong(cursorExportedDB.getColumnIndex(KEY_ID)));
                            importDBEventProfileIds.add(profileId);

                        } while (cursorExportedDB.moveToNext());
                    }
                    cursorExportedDB.close();
                    cursorImportDB.close();

                    db.execSQL("DELETE FROM " + TABLE_EVENTS);

                    if (tableExists(TABLE_EVENTS, exportedDBObj)) {
                        // cusor for events exportedDB
                        cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
                        columnNamesExportedDB = cursorExportedDB.getColumnNames();

                        // cursor for profiles of destination db
                        cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

                        int batteryLevel = 15;
                        int batteryDetectorType = 0;
                        int eventType = 0;
                        long fkProfileEnd = GlobalData.PROFILE_NO_ACTIVATE;
                        long startTime = 0;
                        long endTime = 0;
                        int priority = 0;
                        int delayStart = 0;
                        int useEndTime = 0;
                        int undoneProfile = 0;
                        String calendarSearchString = "";
                        String wifiSSID = "";
                        String bluetoothAdapterName = "";

                        if (cursorExportedDB.moveToFirst()) {
                            do {
                                values.clear();
                                for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                    // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                    if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                        if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START) ||
                                                columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) ||
                                                columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED)) {
                                            // importnuty profil ma nove id
                                            // ale mame mapovacie polia, z ktorych vieme
                                            // ktore povodne id za zmenilo na ktore nove
                                            int profileIdx = exportedDBEventProfileIds.indexOf(cursorExportedDB.getLong(i));
                                            if (profileIdx != -1)
                                                values.put(columnNamesExportedDB[i], importDBEventProfileIds.get(profileIdx));
                                            else {
                                                if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) &&
                                                        (cursorExportedDB.getLong(i) == GlobalData.PROFILE_NO_ACTIVATE))
                                                    values.put(columnNamesExportedDB[i], GlobalData.PROFILE_NO_ACTIVATE);
                                                else if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) &&
                                                        (cursorExportedDB.getLong(i) == GlobalData.PROFILE_NO_ACTIVATE))
                                                    values.put(columnNamesExportedDB[i], GlobalData.PROFILE_NO_ACTIVATE);
                                                else
                                                    values.put(columnNamesExportedDB[i], 0);
                                            }
                                        } else
                                            values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                    }

                                    if (columnNamesExportedDB[i].equals(KEY_E_BATTERY_LEVEL))
                                        batteryLevel = cursorExportedDB.getInt(i);
                                    if (columnNamesExportedDB[i].equals(KEY_E_BATTERY_DETECTOR_TYPE))
                                        batteryDetectorType = cursorExportedDB.getInt(i);
                                    if (columnNamesExportedDB[i].equals(KEY_E_TYPE))
                                        eventType = cursorExportedDB.getInt(i);
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
                                    values.put(KEY_E_BATTERY_DETECTOR_TYPE, 0);
                                }

                                if (exportedDBObj.getVersion() < 1022) {
                                    values.put(KEY_E_NOTIFICATION_SOUND, "");
                                }

                                if (exportedDBObj.getVersion() < 1023) {
                                    values.put(KEY_E_BATTERY_LEVEL_LOW, 0);
                                    values.put(KEY_E_BATTERY_LEVEL_HIGHT, 100);
                                    values.put(KEY_E_BATTERY_CHARGING, 0);
                                    if (batteryDetectorType == 0)
                                        values.put(KEY_E_BATTERY_LEVEL_HIGHT, batteryLevel);
                                    if (batteryDetectorType == 1)
                                        values.put(KEY_E_BATTERY_LEVEL_LOW, batteryLevel);
                                    if (batteryDetectorType == 2)
                                        values.put(KEY_E_BATTERY_CHARGING, 1);
                                }

                                if (exportedDBObj.getVersion() < 1030) {
                                    values.put(KEY_E_TIME_ENABLED, 0);
                                    values.put(KEY_E_BATTERY_ENABLED, 0);
                                    if (eventType == 1) {
                                        values.put(KEY_E_TIME_ENABLED, 1);
                                        values.put(KEY_E_BATTERY_LEVEL_LOW, 0);
                                        values.put(KEY_E_BATTERY_LEVEL_HIGHT, 100);
                                        values.put(KEY_E_BATTERY_CHARGING, 0);
                                    }
                                    if (eventType == 2) {
                                        values.put(KEY_E_BATTERY_ENABLED, 1);
                                        values.put(KEY_E_START_TIME, 0);
                                        values.put(KEY_E_END_TIME, 0);
                                        values.put(KEY_E_DAYS_OF_WEEK, "#ALL#");
                                        values.put(KEY_E_USE_END_TIME, 0);
                                    }

                                }

                                if (exportedDBObj.getVersion() < 1040) {
                                    values.put(KEY_E_CALL_ENABLED, 0);
                                    values.put(KEY_E_CALL_EVENT, 0);
                                    values.put(KEY_E_CALL_CONTACTS, "");
                                    values.put(KEY_E_CALL_CONTACT_LIST_TYPE, 0);
                                }

                                if (exportedDBObj.getVersion() < 1045) {
                                    values.put(KEY_E_FK_PROFILE_END, GlobalData.PROFILE_NO_ACTIVATE);
                                }

                                if (exportedDBObj.getVersion() < 1050) {
                                    values.put(KEY_E_FORCE_RUN, 0);
                                }

                                if (exportedDBObj.getVersion() < 1051) {
                                    values.put(KEY_E_BLOCKED, 0);
                                }

                                if (exportedDBObj.getVersion() < 1060) {
                                    if (fkProfileEnd == GlobalData.PROFILE_NO_ACTIVATE)
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
                                    values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, GlobalData.PROFILE_NO_ACTIVATE);
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

                                // Inserting Row do db z SQLiteOpenHelper
                                db.insert(TABLE_EVENTS, null, values);

                            } while (cursorExportedDB.moveToNext());
                        }
                        cursorExportedDB.close();
                        cursorImportDB.close();

                    }

                    db.execSQL("DELETE FROM " + TABLE_ACTIVITY_LOG);

                    if (tableExists(TABLE_ACTIVITY_LOG, exportedDBObj)) {
                        // cusor for events exportedDB
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
                        // cusor for events exportedDB
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
                        // cusor for events exportedDB
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
                        // cusor for exportedDB
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

                                // Inserting Row do db z SQLiteOpenHelper
                                db.insert(TABLE_MOBILE_CELLS, null, values);

                            } while (cursorExportedDB.moveToNext());
                        }

                        cursorExportedDB.close();
                        cursorImportDB.close();

                    }

                    db.setTransactionSuccessful();

                    ret = 1;
                } finally {
                    db.endTransaction();
                    if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                        cursorExportedDB.close();
                    if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                        cursorImportDB.close();
                    //db.close();
                }
            }
        }
        catch (Exception e) {
            Log.e("DatabaseHandler.importDB", e.toString());
        }

        updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
        deactivateProfile();
        unblockAllEvents();

        return ret;
    }

    public int disableNotAllowedPreferences(Context context)
    {
        int ret = 0;

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
                                                    KEY_NOTIFICATION_LED + "," +
                                                    KEY_VIBRATE_WHEN_RINGING +
                                         " FROM " + TABLE_PROFILES;
        final String selectEventsQuery = "SELECT " + KEY_E_ID + "," +
                                                    KEY_E_WIFI_ENABLED + "," +
                                                    KEY_E_BLUETOOTH_ENABLED + "," +
                                                    KEY_E_NOTIFICATION_ENABLED + "," +
                                                    KEY_E_ORIENTATION_ENABLED +
                                           " FROM " + TABLE_EVENTS;

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();
        try {

            Cursor cursor = db.rawQuery(selectProfilesQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    if ((Integer.parseInt(cursor.getString(1)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_AIRPLANE_MODE, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(2)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_WIFI, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(3)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_BLUETOOTH, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(4)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_MOBILE_DATA, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(5)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(6)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_GPS, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_GPS, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(7)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(8)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_NFC, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_NFC, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(10)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_WIFI_AP, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if (Integer.parseInt(cursor.getString(9)) == 5) {
                        boolean notRemove = GlobalData.canChangeZenMode(context, true);
                        if (!notRemove) {
                            int zenMode = cursor.getInt(12);
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
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }
                    }

                    if ((Integer.parseInt(cursor.getString(11)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context)
                                    == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(13)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, context)
                                    == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(14)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_NOTIFICATION_LED, context)
                                    == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_NOTIFICATION_LED, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                    if ((Integer.parseInt(cursor.getString(15)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, context)
                                    == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }

                } while (cursor.moveToNext());
            }

            cursor.close();

            //-----------------------

            cursor = db.rawQuery(selectEventsQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    if ((Integer.parseInt(cursor.getString(1)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_E_WIFI_ENABLED, 0);
                        db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }
                    if ((Integer.parseInt(cursor.getString(2)) != 0) &&
                            (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) == GlobalData.PREFERENCE_NOT_ALLOWED))
                    {
                        values.clear();
                        values.put(KEY_E_BLUETOOTH_ENABLED, 0);
                        db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }
                    if ((Integer.parseInt(cursor.getString(3)) != 0) &&
                            (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2))
                    {
                        values.clear();
                        values.put(KEY_E_NOTIFICATION_ENABLED, 0);
                        db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                    }
                    if (Integer.parseInt(cursor.getString(4)) != 0) {
                        boolean enabled = (PhoneProfilesService.getAccelerometerSensor(context.getApplicationContext()) != null) &&
                                          (PhoneProfilesService.getMagneticFieldSensor(context.getApplicationContext()) != null);
                        if (!enabled) {
                            values.clear();
                            values.put(KEY_E_ORIENTATION_DISPLAY, "");
                            values.put(KEY_E_ORIENTATION_SIDES, "");
                            db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(Integer.parseInt(cursor.getString(0)))});
                        }
                        enabled = (PhoneProfilesService.getProximitySensor(context.getApplicationContext()) != null) &&
                                  ForegroundApplicationChangedService.isEnabled(context.getApplicationContext());
                        if (!enabled) {
                            values.clear();
                            values.put(KEY_E_ORIENTATION_DISTANCE, 0);
                            db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(Integer.parseInt(cursor.getString(0)))});
                        }
                    }

                } while (cursor.moveToNext());
            }

            cursor.close();

            db.setTransactionSuccessful();

            ret = 1;
        } catch (Exception e){
            //Error in between database transaction
            ret = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return ret;
    }

    @SuppressWarnings("resource")
    public int exportDB()
    {
        int ret = 0;

        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            File dataDB = new File(data, GUIData.DB_FILEPATH + "/" + DATABASE_NAME);
            File exportedDB = new File(sd, GlobalData.EXPORT_PATH + "/" + EXPORT_DBFILENAME);

            if (dataDB.exists())
            {
                // close db
                close();

                File exportDir = new File(sd, GlobalData.EXPORT_PATH);
                if (!(exportDir.exists() && exportDir.isDirectory()))
                {
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
            Log.e("DatabaseHandler.exportDB", e.toString());
        }

        return ret;
    }

}
