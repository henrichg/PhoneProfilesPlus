package sk.henrichg.phoneprofilesplus;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/** @noinspection ExtractMethodRecommender*/
class DatabaseHandlerCreateUpdateDB {

    static private String profileTableCreationString(String tableName) {
        String idField = DatabaseHandler.KEY_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,";
        if (tableName.equals(DatabaseHandler.TABLE_MERGED_PROFILE))
            idField = DatabaseHandler.KEY_ID + " " + DatabaseHandler.INTEGER_TYPE + ",";
        return "CREATE TABLE IF NOT EXISTS " + tableName + "("
                + idField
                + DatabaseHandler.KEY_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_ICON + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_CHECKED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_PORDER + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_RINGER_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_RINGTONE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_NOTIFICATION + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_MEDIA + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_ALARM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_SYSTEM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_VOICE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_ALARM_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_ALARM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WIFI + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_BLUETOOTH + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_BRIGHTNESS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WALLPAPER + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_MOBILE_DATA + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_GPS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_AUTOSYNC + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_AUTOROTATE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_NFC + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_AFTER_DURATION_DO + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_ZEN_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_KEYGUARD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VIBRATE_ON_TOUCH + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WIFI_AP + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_ASK_FOR_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_NOTIFICATION_LED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_LOCK_DEVICE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SCREEN_DARK_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_ON_TOUCH + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_DTMF + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_ACCESSIBILITY + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_AFTER_DURATION_PROFILE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_ALWAYS_ON_DISPLAY + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SCREEN_ON_PERMANENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_MUTE_SOUND + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_LOCATION_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_GENERATE_NOTIFICATION + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_CAMERA_FLASH + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_CHANGE_WALLPAPER_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_END_OF_ACTIVATION_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_DEVICE_VPN + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE
                + ")";
    }

    static void createTables(SQLiteDatabase db) {
        final String CREATE_PROFILES_TABLE = profileTableCreationString(DatabaseHandler.TABLE_PROFILES);
        db.execSQL(CREATE_PROFILES_TABLE);

        final String CREATE_MERGED_PROFILE_TABLE = profileTableCreationString(DatabaseHandler.TABLE_MERGED_PROFILE);
        db.execSQL(CREATE_MERGED_PROFILE_TABLE);

        final String CREATE_EVENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_EVENTS + "("
                + DatabaseHandler.KEY_E_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_E_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_FK_PROFILE_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_END_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DAYS_OF_WEEK + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_USE_END_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_STATUS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_CHARGING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_TIME_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_EVENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_CONTACTS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_FK_PROFILE_END + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_FORCE_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BLOCKED + " " + DatabaseHandler.INTEGER_TYPE + ","
                //+ DatabaseHandler.KEY_E_UNDONE_PROFILE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PRIORITY + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACCESSORY_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                //+ DatabaseHandler.KEY_E_ACCESSORY_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_CALENDARS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_WIFI_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_WIFI_SSID + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SCREEN_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DELAY_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_IS_IN_DELAY_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                //+ DatabaseHandler.KEY_E_SMS_EVENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_CONTACTS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_AT_END_DO + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_APPLICATION_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_LOCATION_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_LOCATION_FK_GEOFENCE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DELAY_END + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_IS_IN_DELAY_END + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_START_STATUS_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PAUSE_STATUS_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_SIDES + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_DISTANCE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_DISPLAY + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_LOCATION_GEOFENCES + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_START_ORDER + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_NFC_TAGS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_GPS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_NFC + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NFC_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_TIME_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BATTERY_PLUGGED + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_TIME_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_TEXT + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_AT_END_HOW_UNDO + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_STATUS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACCESSORY_TYPE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_FROM_SIM_SLOT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_START_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_COUNTER + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ROAMING_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ROAMING_CHECK_DATA + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VPN_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VPN_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_ALARM_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_VOICE_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_TO + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_E_APPLICATION_DURATION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_E_APPLICATION_START_TIME + " " + DatabaseHandler.INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        final String CREATE_EVENTTIME_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_EVENT_TIMELINE + "("
                + DatabaseHandler.KEY_ET_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_ET_EORDER + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_ET_FK_EVENT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_ET_FK_PROFILE_RETURN + " " + DatabaseHandler.INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_EVENTTIME_TABLE);

        final String CREATE_ACTIVITYLOG_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_ACTIVITY_LOG + "("
                + DatabaseHandler.KEY_AL_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_AL_LOG_DATE_TIME + " " + DatabaseHandler.DATETIME_TYPE + " DEFAULT CURRENT_TIMESTAMP,"
                + DatabaseHandler.KEY_AL_LOG_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_AL_EVENT_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_AL_PROFILE_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_AL_PROFILE_ICON + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_AL_DURATION_DELAY + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT + " " + DatabaseHandler.TEXT_TYPE
                + ")";
        db.execSQL(CREATE_ACTIVITYLOG_TABLE);

        final String CREATE_GEOFENCES_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_GEOFENCES + "("
                + DatabaseHandler.KEY_G_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_G_LATITUDE + " " + DatabaseHandler.DOUBLE_TYPE + ","
                + DatabaseHandler.KEY_G_LONGITUDE + " " + DatabaseHandler.DOUBLE_TYPE + ","
                + DatabaseHandler.KEY_G_RADIUS + " " + DatabaseHandler.FLOAT_TYPE + ","
                + DatabaseHandler.KEY_G_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_G_CHECKED + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_G_TRANSITION + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_G_LATITUDE_T + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_G_LONGITUDE_T + " " + DatabaseHandler.TEXT_TYPE
                + ")";
        db.execSQL(CREATE_GEOFENCES_TABLE);

        final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_SHORTCUTS + "("
                + DatabaseHandler.KEY_S_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_S_INTENT + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_S_NAME + " " + DatabaseHandler.TEXT_TYPE
                + ")";
        db.execSQL(CREATE_SHORTCUTS_TABLE);

        final String CREATE_MOBILE_CELLS_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_MOBILE_CELLS + "("
                + DatabaseHandler.KEY_MC_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_MC_CELL_ID + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_MC_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_MC_NEW + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_MC_DO_NOT_DETECT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_MC_CELL_ID_T + " " + DatabaseHandler.TEXT_TYPE
                + ")";
        db.execSQL(CREATE_MOBILE_CELLS_TABLE);

        final String CREATE_NFC_TAGS_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_NFC_TAGS + "("
                + DatabaseHandler.KEY_NT_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_NT_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_NT_UID + " " + DatabaseHandler.TEXT_TYPE
                + ")";
        db.execSQL(CREATE_NFC_TAGS_TABLE);

        final String CREATE_INTENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + DatabaseHandler.TABLE_INTENTS + "("
                + DatabaseHandler.KEY_IN_ID + " " + DatabaseHandler.INTEGER_TYPE + " PRIMARY KEY,"
                + DatabaseHandler.KEY_IN_PACKAGE_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_CLASS_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_ACTION + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_DATA + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_MIME_TYPE + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_1 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_1 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_1 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_2 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_2 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_2 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_3 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_3 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_3 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_4 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_4 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_4 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_5 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_5 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_5 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_6 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_6 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_6 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_7 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_7 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_7 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_8 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_8 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_8 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_9 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_9 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_9 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_KEY_10 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_VALUE_10 + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_EXTRA_TYPE_10 + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_CATEGORIES + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_FLAGS + " " + DatabaseHandler.TEXT_TYPE + ","
                + DatabaseHandler.KEY_IN_NAME + " " + DatabaseHandler.TEXT_TYPE + ","
                //+ DatabaseHandler.KEY_IN_USED_COUNT + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_INTENT_TYPE + " " + DatabaseHandler.INTEGER_TYPE + ","
                + DatabaseHandler.KEY_IN_DO_NOT_DELETE + " " + DatabaseHandler.INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_INTENTS_TABLE);

    }

    static void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PORDER ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_PORDER + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SHOW_IN_ACTIVATOR ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_P_NAME ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_FK_PROFILE_START + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_E_NAME ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_END ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_FK_PROFILE_END + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRIORITY ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_PRIORITY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_START_ORDER ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_START_ORDER + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ET_PORDER ON " + DatabaseHandler.TABLE_EVENT_TIMELINE + " (" + DatabaseHandler.KEY_ET_EORDER + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_AL_LOG_DATE_TIME ON " + DatabaseHandler.TABLE_ACTIVITY_LOG + " (" + DatabaseHandler.KEY_AL_LOG_DATE_TIME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_AUTOROTATE ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_DEVICE_AUTOROTATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_CONNECT_TO_SSID ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_LOCATION_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_LOCATION_ENABLED + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__TIME_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_TIME_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BATTERY_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_BATTERY_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALL_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_CALL_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIPHERAL_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_ACCESSORY_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALENDAR_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_CALENDAR_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__WIFI_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_WIFI_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SCREEN_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_SCREEN_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BLUETOOTH_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SMS_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_SMS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__APPLICATION_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_APPLICATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__LOCATION_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_LOCATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ORIENTATION_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_ORIENTATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NFC_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_NFC_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__RADIO_SWITCH_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ALARM_CLOCK_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__DEVICE_BOOT_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SOUND_PROFILE_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIODIC_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_PERIODIC_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__VOLUMES_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_VOLUMES_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ACTIVATED_PROFILE_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ROAMING_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_ROAMING_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__VPN_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_VPN_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BRIGHTNESS_ENABLED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED + ")");

        //db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED_WHEN_OUTSIDE ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_STATUS + "," + DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + "," + DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + DatabaseHandler.TABLE_GEOFENCES + " (" + DatabaseHandler.KEY_G_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + DatabaseHandler.TABLE_MOBILE_CELLS + " (" + DatabaseHandler.KEY_MC_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_CELL_ID ON " + DatabaseHandler.TABLE_MOBILE_CELLS + " (" + DatabaseHandler.KEY_MC_CELL_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + DatabaseHandler.TABLE_NFC_TAGS + " (" + DatabaseHandler.KEY_NT_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ACTIVATION_BY_USER_COUNT ON " + DatabaseHandler.TABLE_PROFILES + " (" + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_START_WHEN_ACTIVATED ON " + DatabaseHandler.TABLE_EVENTS + " (" + DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + ")");
    }

    static private List<String> getTableColums(SQLiteDatabase db, java.lang.String table) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            //cursor = db.rawQuery("PRAGMA DatabaseHandler.TABLE_info("+ table +")", null);
            //cursor = db.rawQuery("PRAGMA phoneProfilesManager.TABLE_info("+ table +")", null);

            cursor = db.rawQuery("select * from "+table + " LIMIT 1", null);
            if (cursor != null) {
                String[] _columns = cursor.getColumnNames();
                Collections.addAll(columns, _columns);
                //while (cursor.moveToNext()) {
                //    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                //    columns.add(name);
                //}
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return columns;
    }

    static void createTableColumsWhenNotExists(SQLiteDatabase db, String table) {
        List<String> columns = getTableColums(db, table);
        switch (table) {
            case DatabaseHandler.TABLE_PROFILES:
            case DatabaseHandler.TABLE_MERGED_PROFILE:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ICON, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_CHECKED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_PORDER,  DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_RINGER_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_RINGTONE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_NOTIFICATION, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_MEDIA, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_ALARM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_SYSTEM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_VOICE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_ALARM_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_ALARM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WIFI, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_BLUETOOTH, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_BRIGHTNESS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WALLPAPER, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_MOBILE_DATA, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_GPS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_AUTOSYNC, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SHOW_IN_ACTIVATOR, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_AUTOROTATE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_NFC, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AFTER_DURATION_DO, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_ZEN_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_KEYGUARD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATE_ON_TOUCH, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WIFI_AP, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ASK_FOR_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_NETWORK_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_NOTIFICATION_LED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATE_WHEN_RINGING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_LOCK_DEVICE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SCREEN_DARK_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_ON_TOUCH, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_DTMF, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_ACCESSIBILITY, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AFTER_DURATION_PROFILE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ALWAYS_ON_DISPLAY, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SCREEN_ON_PERMANENT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_MUTE_SOUND, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_LOCATION_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_GENERATE_NOTIFICATION, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_CAMERA_FLASH, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_ONOFF_SIM1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_ONOFF_SIM2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE_SIM1, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_RINGTONE_SIM2, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_CHANGE_WALLPAPER_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_END_OF_ACTIVATION_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_DEVICE_VPN, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_EVENTS:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_FK_PROFILE_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_END_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DAYS_OF_WEEK, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_USE_END_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_STATUS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_CHARGING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_TIME_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_EVENT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_CONTACTS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_FK_PROFILE_END, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_FORCE_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLOCKED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PRIORITY, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACCESSORY_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                //createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACCESSORY_TYPE, TDatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_CALENDARS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_WIFI_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_WIFI_SSID, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SCREEN_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DELAY_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_IS_IN_DELAY_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLUETOOTH_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_CONTACTS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_AT_END_DO, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_APPLICATION_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_LOCATION_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_LOCATION_FK_GEOFENCE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DELAY_END, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_IS_IN_DELAY_END, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_START_STATUS_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PAUSE_STATUS_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_SIDES, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_DISTANCE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_DISPLAY, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_LOCATION_GEOFENCES, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_START_ORDER, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_NFC_TAGS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_GPS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_NFC, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NFC_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_TIME_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BATTERY_PLUGGED, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_TIME_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_TEXT, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_AT_END_HOW_UNDO, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_STATUS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACCESSORY_TYPE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_FROM_SIM_SLOT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_COUNTER, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ROAMING_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ROAMING_CHECK_DATA, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VPN_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VPN_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_ALARM_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_VOICE_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_TO, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_APPLICATION_DURATION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_E_APPLICATION_START_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_EVENT_TIMELINE:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ET_EORDER, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ET_FK_EVENT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_ET_FK_PROFILE_RETURN, DatabaseHandler.INTEGER_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_ACTIVITY_LOG:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_LOG_DATE_TIME, DatabaseHandler.DATETIME_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_LOG_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_EVENT_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_PROFILE_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_PROFILE_ICON, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_DURATION_DELAY, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT, DatabaseHandler.TEXT_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_GEOFENCES:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_LATITUDE, DatabaseHandler.DOUBLE_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_LONGITUDE, DatabaseHandler.DOUBLE_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_RADIUS, DatabaseHandler.FLOAT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_CHECKED, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_TRANSITION, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_LATITUDE_T, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_G_LONGITUDE_T, DatabaseHandler.TEXT_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_SHORTCUTS:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_S_INTENT, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_S_NAME, DatabaseHandler.TEXT_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_MOBILE_CELLS:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_CELL_ID, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_NEW, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_DO_NOT_DETECT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_MC_CELL_ID_T, DatabaseHandler.TEXT_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_NFC_TAGS:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_NT_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_NT_UID, DatabaseHandler.TEXT_TYPE, columns);
                break;
            case DatabaseHandler.TABLE_INTENTS:
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_PACKAGE_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_CLASS_NAME, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_ACTION, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_DATA, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_MIME_TYPE, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_1, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_1, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_1, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_2, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_2, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_2, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_3, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_3, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_3, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_4, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_4, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_4, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_5, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_5, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_5, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_6, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_6, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_6, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_7, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_7, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_7, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_8, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_8, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_8, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_9, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_9, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_9, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_KEY_10, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_VALUE_10, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_EXTRA_TYPE_10, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_CATEGORIES, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_FLAGS, DatabaseHandler.TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_NAME, DatabaseHandler.TEXT_TYPE, columns);
                //createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_USED_COUNT, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_INTENT_TYPE, DatabaseHandler.INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, DatabaseHandler.KEY_IN_DO_NOT_DELETE, DatabaseHandler.INTEGER_TYPE, columns);
                break;
        }
    }

    static private boolean columnExists (String column, List<String> columns/*, String table*/) {
/*        boolean exists = false;
        for (String _column : columns) {
            if (column.equalsIgnoreCase(_column)) {
                exists = true;
                break;
            }
        }*/
        boolean exists;
        exists = columns.contains(column);
        return exists;
    }

    static private void createColumnWhenNotExists(SQLiteDatabase db, String table, String column, String columnType, List<String> columns) {
        if (!columnExists(column, columns/*, table*/))
            // create column
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnType);
    }

    // this is called only from onUpgrade and importDB
    // for this, is not needed calling importExportLock.lock();
    static private void changePictureFilePathToUri(DatabaseHandler instance, SQLiteDatabase database/*, boolean lock*/) {
        try {
            SQLiteDatabase db;
            if (database == null) {
                //SQLiteDatabase db = this.getWritableDatabase();
                db = instance.getMyWritableDatabase();
            } else
                db = database;

            final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                    DatabaseHandler.KEY_ICON + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE + "," +
                    DatabaseHandler.KEY_DEVICE_WALLPAPER +
                    " FROM " + DatabaseHandler.TABLE_PROFILES;

            try (Cursor cursor = db.rawQuery(selectQuery, null)) {
                if (database == null)
                    db.beginTransaction();

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        String icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON));
                        int wallpaperChange = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE));

                        ContentValues values = new ContentValues();

                        try {
                            String[] splits = icon.split(StringConstants.STR_SPLIT_REGEX);
                            String isIconResourceId = splits[1];
                            if (!isIconResourceId.equals("1")) {
                                values.put(DatabaseHandler.KEY_ICON, StringConstants.PROFILE_ICON_DEFAULT + "|1|0|0");
                            }
                        } catch (Exception e) {
                            //Log.e("DatabaseHandlerCreateUpdateDB.changePictureFilePathToUri", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                            values.put(DatabaseHandler.KEY_ICON, StringConstants.PROFILE_ICON_DEFAULT + "|1|0|0");
                        }
                        if (wallpaperChange == 1) {
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, 0);
                        }
                        values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER, "-");

                        //noinspection SizeReplaceableByIsEmpty
                        if (values.size() > 0) {
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?", new String[]{String.valueOf(id)});
                        }

                    } while (cursor.moveToNext());
                }

                if (database == null)
                    db.setTransactionSuccessful();

            } catch (Exception e) {
                //Error in between database transaction
                PPApplicationStatic.recordException(e);
                //Log.e("DatabaseHandlerCreateUpdateDB.changePictureFilePathToUri", Log.getStackTraceString(e));
            } finally {
                if (database == null)
                    db.endTransaction();
            }

            //db.close();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    static void afterUpdateDb(SQLiteDatabase db) {
        //Cursor cursorUpdateDB = null;
        //String intentName;
        //boolean found;
        ContentValues values = new ContentValues();

        //noinspection EmptyFinallyBlock
        try {
            /*db.delete(DatabaseHandler.TABLE_INTENTS,
                    DatabaseHandler.KEY_IN_ACTION + " = ? AND " +
                    DatabaseHandler.KEY_IN_PACKAGE_NAME + " = ?",
                    new String[]{ "net.openvpn.openvpn.CONNECT", "net.openvpn.openvpn" });
            db.delete(DatabaseHandler.TABLE_INTENTS,
                    DatabaseHandler.KEY_IN_ACTION + " = ? AND " +
                            DatabaseHandler.KEY_IN_PACKAGE_NAME + " = ?",
                    new String[]{ "net.openvpn.openvpn.DISCONNECT", "net.openvpn.openvpn" });
            db.delete(DatabaseHandler.TABLE_INTENTS,
                    DatabaseHandler.KEY_IN_ACTION + " = ? AND " +
                            DatabaseHandler.KEY_IN_PACKAGE_NAME + " = ? AND " +
                            DatabaseHandler.KEY_IN_CLASS_NAME + " = ?",
                    new String[]{ "android.intent.action.MAIN", "de.blinkt.openvpn", "de.blinkt.openvpn.api.ConnectVPN" });
            db.delete(DatabaseHandler.TABLE_INTENTS,
                    DatabaseHandler.KEY_IN_ACTION + " = ? AND " +
                            DatabaseHandler.KEY_IN_PACKAGE_NAME + " = ? AND " +
                            DatabaseHandler.KEY_IN_CLASS_NAME + " = ?",
                    new String[]{ "android.intent.action.MAIN", "de.blinkt.openvpn", "de.blinkt.openvpn.api.DisconnectVPN" });*/

            values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "0");
            db.update(DatabaseHandler.TABLE_INTENTS, values, DatabaseHandler.KEY_IN_DO_NOT_DELETE + " = ?", new String[]{String.valueOf(1)});

            /*
            intentName = "[OpenVPN Connect - connect URL profile]";
            cursorUpdateDB = db.rawQuery("SELECT " + DatabaseHandler.KEY_IN_NAME + " FROM " + DatabaseHandler.TABLE_INTENTS +
                            " WHERE " + DatabaseHandler.KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            if (!found) {
                values.put(DatabaseHandler.KEY_IN_NAME, intentName);
                values.put(DatabaseHandler.KEY_IN_ACTION, "net.openvpn.openvpn.CONNECT");
                //values.put(DatabaseHandler.KEY_IN_ACTION, "android.intent.action.VIEW");
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.AUTOSTART_PROFILE_NAME");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, "AS {your_profile_name}");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_2, "net.openvpn.openvpn.AUTOCONNECT");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_2, "true");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_2, 0); // string
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, 0); // activity
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "1");
                db.insert(DatabaseHandler.TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN Connect - connect file profile]";
            cursorUpdateDB = db.rawQuery("SELECT " + DatabaseHandler.KEY_IN_NAME + " FROM " + DatabaseHandler.TABLE_INTENTS +
                            " WHERE " + DatabaseHandler.KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            if (!found) {
                values.clear();
                values.put(DatabaseHandler.KEY_IN_NAME, intentName);
                values.put(DatabaseHandler.KEY_IN_ACTION, "net.openvpn.openvpn.CONNECT");
                //values.put(DatabaseHandler.KEY_IN_ACTION, "android.intent.action.VIEW");
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.AUTOSTART_PROFILE_NAME");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, "PC {your_profile_name}");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_2, "net.openvpn.openvpn.AUTOCONNECT");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_2, "true");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_2, 0); // string
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, 0); // activity
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "1");
                db.insert(DatabaseHandler.TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN Connect - disconnect]";
            cursorUpdateDB = db.rawQuery("SELECT " + DatabaseHandler.KEY_IN_NAME + " FROM " + DatabaseHandler.TABLE_INTENTS +
                            " WHERE " + DatabaseHandler.KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            if (!found) {
                values.clear();
                values.put(DatabaseHandler.KEY_IN_NAME, intentName);
                values.put(DatabaseHandler.KEY_IN_ACTION, "net.openvpn.openvpn.DISCONNECT");
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.STOP");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, "true");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, 0); // activity
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "1");
                db.insert(DatabaseHandler.TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN for Android - connect]";
            cursorUpdateDB = db.rawQuery("SELECT " + DatabaseHandler.KEY_IN_NAME + " FROM " + DatabaseHandler.TABLE_INTENTS +
                            " WHERE " + DatabaseHandler.KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            if (!found) {
                values.put(DatabaseHandler.KEY_IN_NAME, intentName);
                values.put(DatabaseHandler.KEY_IN_ACTION, "android.intent.action.MAIN");
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, "de.blinkt.openvpn");
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, "de.blinkt.openvpn.api.ConnectVPN");
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, "de.blinkt.openvpn.api.profileName");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, "{your_profile_name}");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, 0); // activity
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "1");
                db.insert(DatabaseHandler.TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN for Android - disconnect]";
            cursorUpdateDB = db.rawQuery("SELECT " + DatabaseHandler.KEY_IN_NAME + " FROM " + DatabaseHandler.TABLE_INTENTS +
                            " WHERE " + DatabaseHandler.KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(DatabaseHandler.KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            if (!found) {
                values.put(DatabaseHandler.KEY_IN_NAME, intentName);
                values.put(DatabaseHandler.KEY_IN_ACTION, "android.intent.action.MAIN");
                values.put(DatabaseHandler.KEY_IN_PACKAGE_NAME, "de.blinkt.openvpn");
                values.put(DatabaseHandler.KEY_IN_CLASS_NAME, "de.blinkt.openvpn.api.DisconnectVPN");
                values.put(DatabaseHandler.KEY_IN_EXTRA_KEY_1, "de.blinkt.openvpn.api.profileName");
                values.put(DatabaseHandler.KEY_IN_EXTRA_VALUE_1, "{your_profile_name}");
                values.put(DatabaseHandler.KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(DatabaseHandler.KEY_IN_INTENT_TYPE, 0); // activity
                values.put(DatabaseHandler.KEY_IN_DO_NOT_DELETE, "1");
                db.insert(DatabaseHandler.TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();*/
        } finally {
            //if ((cursorSearchIntent != null) && (!cursorSearchIntent.isClosed()))
            //    cursorSearchIntent.close();
        }
    }

    static void updateDb(DatabaseHandler instance, SQLiteDatabase db, int oldVersion) {
        // check colums existence

        if (oldVersion < 16)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER + "='-'");
        }

        if (oldVersion < 18)
        {
            String value = "=replace(" + DatabaseHandler.KEY_ICON + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_ICON + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_RINGTONE + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_RINGTONE + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_NOTIFICATION + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_NOTIFICATION + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_MEDIA + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_MEDIA + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_ALARM + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_ALARM + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_SYSTEM + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_SYSTEM + value);
            value = "=replace(" + DatabaseHandler.KEY_VOLUME_VOICE + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_VOICE + value);
            value = "=replace(" + DatabaseHandler.KEY_DEVICE_BRIGHTNESS + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_BRIGHTNESS + value);
            value = "=replace(" + DatabaseHandler.KEY_DEVICE_WALLPAPER + ",':','|')";
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER + value);

        }

        if (oldVersion < 19)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA + "=0");
        }

        if (oldVersion < 20)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS + "=0");
        }

        if (oldVersion < 21)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_GPS + "=0");
        }

        if (oldVersion < 22)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 24)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_AUTOSYNC + "=0");
        }

        if (oldVersion < 26)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SHOW_IN_ACTIVATOR + "=1");
        }

        if (oldVersion < 29)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_END_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DAYS_OF_WEEK + "=\"#ALL#\"");
        }

        if (oldVersion < 30)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_USE_END_TIME + "=0");
        }

        if (oldVersion < 32)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_STATUS + "=0");
        }

        if (oldVersion < 1001)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=0");
        }

        if (oldVersion < 1002)
        {
            // autorotate off -> rotation 0
            // autorotate on -> autorotate
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=1 WHERE " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=1");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=1 WHERE " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=3");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=2 WHERE " + DatabaseHandler.KEY_DEVICE_AUTOROTATE + "=2");
        }

        if (oldVersion < 1012)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_LEVEL + "=15");
        }

        if (oldVersion < 1015)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS + "=0");
        }

        if (oldVersion < 1020)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE + "=0");
        }

        if (oldVersion < 1022)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START + "=\"\"");
        }

        if (oldVersion < 1023)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_LEVEL_LOW + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT + "=100");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_CHARGING + "=0");

            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_BATTERY_LEVEL +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        String batteryLevel = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BATTERY_LEVEL));

                        db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                " SET " + DatabaseHandler.KEY_E_BATTERY_LEVEL_HIGHT + "=" + batteryLevel + " " +
                                "WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1030)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_TIME_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_ENABLED + "=0");
        }

        if (oldVersion < 1035)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NFC + "=0");
        }

        if (oldVersion < 1040)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_EVENT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1045)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_FK_PROFILE_END + "=" + Profile.PROFILE_NO_ACTIVATE);
        }

        if (oldVersion < 1050)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_FORCE_RUN + "=0");
        }

        if (oldVersion < 1051)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLOCKED + "=0");
        }

        if (oldVersion < 1060)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_UNDONE_PROFILE + "=0");

            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_FK_PROFILE_END +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int fkProfileEnd = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_END));

                        if (fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                    " SET " + DatabaseHandler.KEY_E_UNDONE_PROFILE + "=1 " +
                                    "WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1070)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PRIORITY + "=0");
        }

        if (oldVersion < 1080)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACCESSORY_ENABLED + "=0");
            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACCESSORY_TYPE + "=0");
        }

        if (oldVersion < 1081)
        {
            // conversion to GMT
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_TIME + "=" + DatabaseHandler.KEY_E_START_TIME + "+" + gmtOffset);
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_END_TIME + "=" + DatabaseHandler.KEY_E_END_TIME + "+" + gmtOffset);
        }

        if (oldVersion < 1090)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_CALENDARS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_SEARCH_FIELD + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING + "=\"\"");
        }

        if (oldVersion < 1095)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_EVENT_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_EVENT_END_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_EVENT_FOUND + "=0");
        }

        if (oldVersion < 1100)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PRIORITY + "=4 WHERE " + DatabaseHandler.KEY_E_PRIORITY + "=2");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PRIORITY + "=2 WHERE " + DatabaseHandler.KEY_E_PRIORITY + "=1");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PRIORITY + "=-2 WHERE " + DatabaseHandler.KEY_E_PRIORITY + "=-1");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PRIORITY + "=-4 WHERE " + DatabaseHandler.KEY_E_PRIORITY + "=-2");
        }

        if (oldVersion < 1105)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_WIFI_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_WIFI_SSID + "=\"\"");
        }

        if (oldVersion < 1106)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_WIFI_CONNECTION_TYPE + "=1");
        }

        if (oldVersion < 1110)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SCREEN_ENABLED + "=0");
        }

        if (oldVersion < 1111)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SCREEN_EVENT_TYPE + "=1");
        }

        if (oldVersion < 1112)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DELAY_START + "=0");
        }

        if (oldVersion < 1113)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_IS_IN_DELAY_START + "=0");
        }

        if (oldVersion < 1120)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DURATION + "=" + Profile.AFTER_DURATION_DO_RESTART_EVENTS);
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_AFTER_DURATION_DO + "=" + Profile.AFTER_DURATION_DO_RESTART_EVENTS);
        }

        if (oldVersion < 1125)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SCREEN_WHEN_UNLOCKED + "=0");
        }

        if (oldVersion < 1130)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLUETOOTH_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0");
        }

        if (oldVersion < 1140)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_ENABLED + "=0");
            //db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_EVENT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1141)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_START_TIME + "=0");
        }

        if (oldVersion < 1150)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_ZEN_MODE + "=0");
        }

        if (oldVersion < 1156)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DEVICE_BRIGHTNESS +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        String brightness = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS));

                        // Index = decription
                        // -----
                        // 0 = level
                        // 1 = change, no change
                        // 2 = automatic
                        // 3 = from shared profile
                        // 4 = change level

                        String[] splits = brightness.split(StringConstants.STR_SPLIT_REGEX);

                        if (splits[2].equals("1")) // automatic is set
                        {
                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                            else
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|0";

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_DEVICE_BRIGHTNESS + "=\"" + brightness + "\" " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1160)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_KEYGUARD + "=0");
        }

        if (oldVersion < 1165)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DEVICE_BRIGHTNESS +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        String brightness = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS));

                        // Index = decription
                        // -----
                        // 0 = level
                        // 1 = change, no change
                        // 2 = automatic
                        // 3 = from shared profile
                        // 4 = change level

                        String[] splits = brightness.split(StringConstants.STR_SPLIT_REGEX);

                        // change percentage only for manual brightness
                        if (!splits[2].equals("1")) // automatic is not set
                        {
                            int percentage = Integer.parseInt(splits[0]);

                            percentage = (int) ProfileStatic.convertBrightnessToPercents(percentage/*, 255, 1*/);

                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = percentage + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                            else
                                brightness = percentage + "|" + splits[1] + "|" + splits[2] + "|0";

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_DEVICE_BRIGHTNESS + "=\"" + brightness + "\" " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1170)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_DELAY_START +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        int delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_DELAY_START)) * 60;  // conversion to seconds

                        db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                " SET " + DatabaseHandler.KEY_E_DELAY_START + "=" + delayStart + " " +
                                "WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1180)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_CONTACT_GROUPS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_CONTACT_GROUPS + "=\"\"");
        }

        if (oldVersion < 1210)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATE_ON_TOUCH + "=0");
        }

        if (oldVersion < 1220)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_USE_END_TIME + "," +
                        DatabaseHandler.KEY_E_START_TIME +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_TIME));

                        if (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_USE_END_TIME)) != 1)
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                    " SET " + DatabaseHandler.KEY_E_END_TIME + "=" + (startTime + 5000) + ", "
                                    + DatabaseHandler.KEY_E_USE_END_TIME + "=1" +
                                    " WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1295)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_UNDONE_PROFILE +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        int atEndDo;

                        if ((cursor.getColumnIndex(DatabaseHandler.KEY_E_UNDONE_PROFILE) == -1) || (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_UNDONE_PROFILE)) == 0))
                            atEndDo = Event.EATENDDO_NONE;
                        else
                            atEndDo = Event.EATENDDO_UNDONE_PROFILE;

                        db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                " SET " + DatabaseHandler.KEY_E_AT_END_DO + "=" + atEndDo +
                                " WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1300)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_AVAILABILITY + "=0");
        }

        if (oldVersion < 1310)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION + "=0");
        }

        if (oldVersion < 1330)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1340)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1350)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DURATION +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION)) * 60;  // conversion to seconds

                        db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                " SET " + DatabaseHandler.KEY_DURATION + "=" + delayStart + " " +
                                "WHERE " + DatabaseHandler.KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1370)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + "=-999");
        }

        if (oldVersion < 1380)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING + "," +
                        DatabaseHandler.KEY_E_WIFI_SSID + "," +
                        DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        String calendarSearchString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING)).replace("%", "\\%").replace("_", "\\_");
                        String wifiSSID = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_WIFI_SSID)).replace("%", "\\%").replace("_", "\\_");
                        String bluetoothAdapterName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME)).replace("%", "\\%").replace("_", "\\_");

                        db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                " SET " + DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING + "=\"" + calendarSearchString + "\"," +
                                DatabaseHandler.KEY_E_WIFI_SSID + "=\"" + wifiSSID + "\"," +
                                DatabaseHandler.KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"" + bluetoothAdapterName + "\"" +
                                " WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1390)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_DURATION + "=5");
        }

        if (oldVersion < 1400)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_APPLICATIONS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_DURATION + "=5");
        }

        if (oldVersion < 1420)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1430)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1440)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLUETOOTH_DEVICES_TYPE + "=0");
        }

        if (oldVersion < 1450)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_APPLICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_APPLICATION_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1460)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_END_WHEN_REMOVED + "=0");
        }

        if (oldVersion < 1470)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + "=0");
        }

        if (oldVersion < 1490)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_GEOFENCES + " SET " + DatabaseHandler.KEY_G_CHECKED + "=0");
        }

        if (oldVersion < 1500)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_LOCATION_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_LOCATION_FK_GEOFENCE + "=0");
        }

        if (oldVersion < 1510) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_GEOFENCES + " SET " + DatabaseHandler.KEY_G_TRANSITION + "=0");
        }

        if (oldVersion < 1520) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_LOCATION_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1530)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DELAY_END + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_IS_IN_DELAY_END + "=0");
        }

        if (oldVersion < 1540)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_STATUS_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PAUSE_STATUS_TIME + "=0");
        }

        if (oldVersion < 1560)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_ASK_FOR_DURATION + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_ASK_FOR_DURATION + "=0");
        }

        if (oldVersion < 1570)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE + "=0");
        }

        if (oldVersion < 1580)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_NOTIFICATION_LED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_NOTIFICATION_LED + "=0");
        }

        if (oldVersion < 1600)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_SIDES + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_DISTANCE + "=0");
        }

        if (oldVersion < 1610)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_DISPLAY + "=\"\"");
        }

        if (oldVersion < 1620)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_IGNORE_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1630)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1640) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1660) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR + "=0");
        }

        if (oldVersion < 1670)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1680)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS +  "=\"\"");
        }

        if (oldVersion < 1700)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_NEW +  "=0");
        }

        if (oldVersion < 1710)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_LOCATION_FK_GEOFENCE +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long geofenceId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_LOCATION_FK_GEOFENCE));

                        ContentValues values = new ContentValues();

                        if (geofenceId > 0) {
                            values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, String.valueOf(geofenceId));
                        } else {
                            values.put(DatabaseHandler.KEY_E_LOCATION_GEOFENCES, "");
                        }
                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1720)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_ORDER +  "=0");
        }

        if (oldVersion < 1740)
        {
            try {
                // initialize startOrder
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID +
                        " FROM " + DatabaseHandler.TABLE_EVENTS +
                        " ORDER BY " + DatabaseHandler.KEY_E_PRIORITY;

                Cursor cursor = db.rawQuery(selectQuery, null);

                int startOrder = 0;
                if (cursor.moveToFirst()) {
                    do {
                        //long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHandler.KEY_E_START_ORDER, ++startOrder);
                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1750)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_ENABLED + "=0");
        }

        if (oldVersion < 1770)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_NFC_TAGS + "=\"\"");
        }

        if (oldVersion < 1780)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_DURATION + "=5");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_START_TIME + "=0");
        }

        if (oldVersion < 1790)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_PERMANENT_RUN + "=1");
        }

        if (oldVersion < 1800)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_LAST_CONNECTED_TIME +  "=0");
        }

        if (oldVersion < 1810) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON + "=0");
        }

        if (oldVersion < 1820)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_LOCK_DEVICE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_LOCK_DEVICE + "=0");
        }

        if (oldVersion < 1830)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_START_BEFORE_EVENT + "=0");
        }

        if (oldVersion < 1840)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_WIFI + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_BLUETOOTH + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_MOBILE_DATA + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_GPS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_NFC + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "=0");
        }

        /*if (oldVersion < 1850)
        {
        }*/

        if (oldVersion < 1860)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + "=\""+StringConstants.CONNECTTOSSID_JUSTANY+"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID + "=\""+StringConstants.CONNECTTOSSID_JUSTANY+"\"");
        }

        if (oldVersion < 1870)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1880)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1890) {
            changePictureFilePathToUri(instance, db/*, false*/);
        }

        if (oldVersion < 1900)
        {
            // conversion into local time
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_TIME + "=" + DatabaseHandler.KEY_E_START_TIME + "-" + gmtOffset);
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_END_TIME + "=" + DatabaseHandler.KEY_E_END_TIME + "-" + gmtOffset);
        }

        if (oldVersion < 1910)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_START + "=0");
        }

        if (oldVersion < 1920)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + "=0");
        }

        if (oldVersion < 1930)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_START_TIME + "," +
                        DatabaseHandler.KEY_E_END_TIME +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_START_TIME));
                        long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_END_TIME));

                        Calendar calendar = Calendar.getInstance();

                        calendar.setTimeInMillis(startTime);
                        values.put(DatabaseHandler.KEY_E_START_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                        calendar.setTimeInMillis(endTime);
                        values.put(DatabaseHandler.KEY_E_END_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));

                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }


        if (oldVersion < 1950)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE + "=0");
        }

        if (oldVersion < 1960)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_DURATION + "=5");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_START_TIME + "=0");
        }

        if (oldVersion < 1970)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_START + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "=15");
        }

        if (oldVersion < 1980)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS + "=0");
        }

        if (oldVersion < 1990) {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        int repeatInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START));

                        values.put(DatabaseHandler.KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, repeatInterval * 60);

                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2000)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_IN_CALL + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_MISSED_CALL + "=0");
        }

        if (oldVersion < 2010)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING + "=0");
        }

        if (oldVersion < 2020)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING + "=0");
        }

        if (oldVersion < 2030)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS + "=0");
        }

        if (oldVersion < 2040)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE + "=\"\"");

            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        long fkProfile = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_FK_PROFILE_START_WHEN_ACTIVATED));

                        if (fkProfile != Profile.PROFILE_NO_ACTIVATE) {
                            values.put(DatabaseHandler.KEY_E_START_WHEN_ACTIVATED_PROFILE, String.valueOf(fkProfile));
                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2050)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 2060)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT + "=0");
        }

        if (oldVersion < 2070)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
        }

        if (oldVersion < 2080)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
        }

        if (oldVersion < 2090)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SCREEN_DARK_MODE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SCREEN_DARK_MODE + "=0");
        }

        if (oldVersion < 2100)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_ON_TOUCH + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_ON_TOUCH + "=0");
        }

        if (oldVersion < 2110)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DEVICE_WIFI_AP +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int wifiAP = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP));

                        if ((wifiAP == 3)) // Toggle is not supported for wifi AP in Android 8+
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_DEVICE_WIFI_AP + "=0" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2120) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BLUETOOTH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_LOCATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_WIFI_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2130) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_NFC_TAGS + " SET " + DatabaseHandler.KEY_NT_UID + "=\"\"");
        }

        if (oldVersion < 2140) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_APPLICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NFC_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACCESSORY_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SCREEN_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_TIME_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2150) {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_LOCK_DEVICE +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int lockDevice = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE));

                        if (lockDevice == 3) {
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_LOCK_DEVICE + "=1" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2160)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_ALL_EVENTS + "=0");
        }

        if (oldVersion < 2170)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_DURATION + "=5");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2180)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_VIBRATE_END + "=0");
        }

        if (oldVersion < 2200)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_INTENTS + " SET " + DatabaseHandler.KEY_IN_NAME + "=\"\"");
        }

        if (oldVersion < 2230)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_INTENTS + " SET " + DatabaseHandler.KEY_IN_ACTION + "=\"\"");
        }

        if (oldVersion < 2240)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BATTERY_PLUGGED + "=\"\"");
        }

        if (oldVersion < 2270) {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        String calendarSearchString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING));
                        //String searchStringNew = "";
                        StringBuilder str = new StringBuilder();
                        String[] searchStringSplits = calendarSearchString.split(StringConstants.STR_SPLIT_REGEX);
                        for (String split : searchStringSplits) {
                            if (!split.isEmpty()) {
                                //String searchPattern = split;
                                //if (searchPattern.startsWith("!")) {
                                //    searchPattern = "\\" + searchPattern;
                                //}
                                //if (!searchStringNew.isEmpty())
                                //    searchStringNew = searchStringNew + "|";
                                //searchStringNew = searchStringNew + searchPattern;
                                if (str.length() > 0)
                                    str.append("|");
                                if (split.startsWith("!"))
                                    str.append("\\");
                                str.append(split);
                            }
                        }

                        ContentValues values = new ContentValues();
                        //values.put(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING, searchStringNew);
                        values.put(DatabaseHandler.KEY_E_CALENDAR_SEARCH_STRING, str.toString());

                        db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        /*if (oldVersion < 2280)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_INTENTS + " SET " + DatabaseHandler.KEY_IN_USED_COUNT + "=0");
        }*/

        if (oldVersion < 2290)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_INTENTS + " SET " + DatabaseHandler.KEY_IN_INTENT_TYPE + "=0");
        }

        if (oldVersion < 2300) {
            try {
                final String selectQuery = "SELECT *" +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                //Profile sharedProfile = Profile.getProfileFromSharedPreferences(context/*, "profile_preferences_default_profile"*/);

                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ICON)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_CHECKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_PORDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_VOICE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_RINGTONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ALARM)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BLUETOOTH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_BRIGHTNESS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_GPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOSYNC)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR) != -1) && (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR)) == 1),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_AUTOROTATE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NFC)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_AFTER_DURATION_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_KEYGUARD)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_ON_TOUCH)),
                                (cursor.getColumnIndex(DatabaseHandler.KEY_DEVICE_WIFI_AP) != -1) ? cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP)) : 0,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ASK_FOR_DURATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_NOTIFICATION_LED)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_LOCK_DEVICE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SCREEN_DARK_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_SOUND_ON_TOUCH)),
                                "-1|1|0",
                                "-1|1|0",
                                "-1|1|0",
                                Profile.PROFILE_NO_ACTIVATE,
                                0,
                                0,
                                false,
                                0,
                                0,
                                "0|0||",
                                0,
                                0,
                                0,
                                //0,
                                //0,
                                "0|0|0",
                                0,
                                0,
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "-",
                                0,
                                0,
                                0,
                                0,
                                0,
                                "0|0|||0",
                                "-1|1",
                                "-1|1",
                                "-1|1",
                                false,
                                15,
                                15,
                                15,
                                15,
                                10,
                                15
                        );

                        // this change old, no longer used SHARED_PROFILE_VALUE to "Not used" value
                        //profile = Profile.getMappedProfile(profile, sharedProfile);
                        profile = ProfileStatic.removeSharedProfileParameters(profile);
                        if (profile != null) {
                            ContentValues values = new ContentValues();
                            values.put(DatabaseHandler.KEY_NAME, profile._name);
                            values.put(DatabaseHandler.KEY_ICON, profile._icon);
                            values.put(DatabaseHandler.KEY_CHECKED, (profile._checked) ? 1 : 0);
                            values.put(DatabaseHandler.KEY_PORDER, profile._porder);
                            values.put(DatabaseHandler.KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                            values.put(DatabaseHandler.KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                            values.put(DatabaseHandler.KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                            values.put(DatabaseHandler.KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                            values.put(DatabaseHandler.KEY_VOLUME_MEDIA, profile._volumeMedia);
                            values.put(DatabaseHandler.KEY_VOLUME_ALARM, profile._volumeAlarm);
                            values.put(DatabaseHandler.KEY_VOLUME_SYSTEM, profile._volumeSystem);
                            values.put(DatabaseHandler.KEY_VOLUME_VOICE, profile._volumeVoice);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE, profile._soundRingtone);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION, profile._soundNotification);
                            values.put(DatabaseHandler.KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                            values.put(DatabaseHandler.KEY_SOUND_ALARM, profile._soundAlarm);
                            values.put(DatabaseHandler.KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                            values.put(DatabaseHandler.KEY_DEVICE_WIFI, profile._deviceWiFi);
                            values.put(DatabaseHandler.KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                            values.put(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                            values.put(DatabaseHandler.KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                            values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                            values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                            values.put(DatabaseHandler.KEY_DEVICE_GPS, profile._deviceGPS);
                            values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                            values.put(DatabaseHandler.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                            values.put(DatabaseHandler.KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                            values.put(DatabaseHandler.KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                            values.put(DatabaseHandler.KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                            values.put(DatabaseHandler.KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                            values.put(DatabaseHandler.KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                            values.put(DatabaseHandler.KEY_DEVICE_NFC, profile._deviceNFC);
                            values.put(DatabaseHandler.KEY_DURATION, profile._duration);
                            values.put(DatabaseHandler.KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                            values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                            values.put(DatabaseHandler.KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                            values.put(DatabaseHandler.KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                            values.put(DatabaseHandler.KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                            values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                            values.put(DatabaseHandler.KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                            values.put(DatabaseHandler.KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                            values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                            values.put(DatabaseHandler.KEY_NOTIFICATION_LED, profile._notificationLed);
                            values.put(DatabaseHandler.KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                            values.put(DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS, profile._vibrateNotifications);
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                            values.put(DatabaseHandler.KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                            values.put(DatabaseHandler.KEY_LOCK_DEVICE, profile._lockDevice);
                            values.put(DatabaseHandler.KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_WIFI_SCANNING, profile._applicationEnableWifiScanning);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_BLUETOOTH_SCANNING, profile._applicationEnableBluetoothScanning);
                            values.put(DatabaseHandler.KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_LOCATION_SCANNING, profile._applicationEnableLocationScanning);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, profile._applicationEnableMobileCellScanning);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_ORIENTATION_SCANNING, profile._applicationEnableOrientationScanning);
                            values.put(DatabaseHandler.KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                            values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                            values.put(DatabaseHandler.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                            values.put(DatabaseHandler.KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                            values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                            values.put(DatabaseHandler.KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                            values.put(DatabaseHandler.KEY_SCREEN_DARK_MODE, profile._screenDarkMode);
                            values.put(DatabaseHandler.KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                            values.put(DatabaseHandler.KEY_SOUND_ON_TOUCH, profile._soundOnTouch);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING, profile._applicationEnableNotificationScanning);
                            values.put(DatabaseHandler.KEY_GENERATE_NOTIFICATION, profile._generateNotification);
                            values.put(DatabaseHandler.KEY_CAMERA_FLASH, profile._cameraFlash);
                            values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1, profile._deviceNetworkTypeSIM1);
                            values.put(DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2, profile._deviceNetworkTypeSIM2);
                            //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1, profile._deviceMobileDataSIM1);
                            //values.put(DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2, profile._deviceMobileDataSIM2);
                            values.put(DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS, profile._deviceDefaultSIMCards);
                            values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM1, profile._deviceOnOffSIM1);
                            values.put(DatabaseHandler.KEY_DEVICE_ONOFF_SIM2, profile._deviceOnOffSIM2);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1, profile._soundRingtoneChangeSIM1);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM1, profile._soundRingtoneSIM1);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2, profile._soundRingtoneChangeSIM2);
                            values.put(DatabaseHandler.KEY_SOUND_RINGTONE_SIM2, profile._soundRingtoneSIM2);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1, profile._soundNotificationChangeSIM1);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1, profile._soundNotificationSIM1);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2, profile._soundNotificationChangeSIM2);
                            values.put(DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2, profile._soundNotificationSIM2);
                            values.put(DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, profile._soundSameRingtoneForBothSIMCards);
                            values.put(DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER, profile._deviceLiveWallpaper);
                            values.put(DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER, profile._deviceWallpaperFolder);
                            values.put(DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, profile._applicationDisableGloabalEventsRun);
                            values.put(DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING, profile._applicationEnablePeriodicScanning);

                            // updating row
                            db.update(DatabaseHandler.TABLE_PROFILES, values, DatabaseHandler.KEY_ID + " = ?",
                                    new String[]{String.valueOf(profile._id)});
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2310)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_DTMF + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_ACCESSIBILITY + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VOLUME_DTMF + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VOLUME_ACCESSIBILITY + "=\"-1|1|0\"");
        }

        if (oldVersion < 2320)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VOLUME_BLUETOOTH_SCO + "=\"-1|1|0\"");
        }

        if (oldVersion < 2340)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_VOLUME_RINGER_MODE +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int ringerMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE));

                        if (ringerMode == 2) {
                            ringerMode = 1;

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_VOLUME_RINGER_MODE + "=" + ringerMode + ", " +
                                    DatabaseHandler.KEY_VIBRATE_WHEN_RINGING + "=1" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2350)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_LAST_RUNNING_EVENTS + "=\"\"");
        }

        if (oldVersion < 2360)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_LAST_PAUSED_EVENTS + "=\"\"");
        }

        if (oldVersion < 2370)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_DO_NOT_DETECT + "=0");
        }

        if (oldVersion < 2380)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_TIME_TYPE + "=0");
        }

        if (oldVersion < 2390)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_DURATION + "=0");
        }

        if (oldVersion < 2400)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_AFTER_DURATION_PROFILE + "=" + Profile.PROFILE_NO_ACTIVATE);
        }
        if (oldVersion < 2401)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_AFTER_DURATION_PROFILE + "=" + Profile.PROFILE_NO_ACTIVATE);
        }
        if (oldVersion < 2402)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_ALWAYS_ON_DISPLAY + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_ALWAYS_ON_DISPLAY + "=0");
        }

        if (oldVersion < 2403)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_VOLUME_RINGER_MODE + "," +
                        DatabaseHandler.KEY_VOLUME_ZEN_MODE +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int ringerMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_RINGER_MODE));
                        int zenMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE));

                        if ((ringerMode == 5) && (zenMode == 0)) {
                            ringerMode = 0;

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_VOLUME_RINGER_MODE + "=" + ringerMode + ", " +
                                    DatabaseHandler.KEY_VOLUME_ZEN_MODE + "=1" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2404)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_CHECK_LIGHT + "=0");
        }
        if (oldVersion < 2405)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX + "=0");
        }

        if (oldVersion < 2406)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_CHECK_CONTACTS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_GROUPS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_CHECK_TEXT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_TEXT + "=\"\"");
        }
        if (oldVersion < 2407)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 2408)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SCREEN_ON_PERMANENT + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SCREEN_ON_PERMANENT + "=0");
        }

        if (oldVersion < 2409) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_ACTIVITY_LOG + " SET " + DatabaseHandler.KEY_AL_PROFILE_EVENT_COUNT + "=\"1 [0]\"");
        }

        if (oldVersion < 2410) {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int screenTimeout = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT));

                        if ((screenTimeout == 6) || (screenTimeout == 8)) {
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_DEVICE_SCREEN_TIMEOUT + "=0, " +
                                    DatabaseHandler.KEY_SCREEN_ON_PERMANENT + "=1" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2420)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DEVICE_BOOT_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DEVICE_BOOT_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DEVICE_BOOT_DURATION + "=5");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DEVICE_BOOT_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_DEVICE_BOOT_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2421)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 2422)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ALARM_CLOCK_PACKAGE_NAME + "=\"\"");
        }

        if (oldVersion < 2423)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_MUTE_SOUND + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VOLUME_MUTE_SOUND + "=0");
        }

        if (oldVersion < 2424)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_LOCATION_MODE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_LOCATION_MODE + "=0");
        }

        if (oldVersion < 2425)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_ID + "," +
                        DatabaseHandler.KEY_VOLUME_ZEN_MODE +
                        " FROM " + DatabaseHandler.TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int zenMode = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_VOLUME_ZEN_MODE));

                        if (zenMode == 0) {
                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES +
                                    " SET " + DatabaseHandler.KEY_VOLUME_ZEN_MODE + "=1" + " " +
                                    "WHERE " + DatabaseHandler.KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2437)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_NOTIFICATION_SCANNING + "=0");
        }

        if (oldVersion < 2439)
        {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN + "," +
                        DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_ID));
                        int lightMin = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN));
                        int lightMax = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX));

                        PPApplicationStatic.startHandlerThreadOrientationScanner();
                        if (PPApplication.handlerThreadOrientationScanner.maxLightDistance > 1.0f) {
                            lightMin = (int) Math.round(lightMin / 10000.0 * PPApplication.handlerThreadOrientationScanner.maxLightDistance);
                            lightMax = (int) Math.round(lightMax / 10000.0 * PPApplication.handlerThreadOrientationScanner.maxLightDistance);

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                    " SET " + DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MIN + "=" + lightMin + "," +
                                    DatabaseHandler.KEY_E_ORIENTATION_LIGHT_MAX + "=" + lightMax + " " +
                                    "WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2440)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_STATUS + "=0");
        }

        if (oldVersion < 2441)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_GENERATE_NOTIFICATION + "=\"0|0||\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_GENERATE_NOTIFICATION + "=\"0|0||\"");
        }

        if (oldVersion < 2442)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + "=0");
        }

        if (oldVersion < 2443)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_EVENT_TODAY_EXISTS + "=0");
        }

        if (oldVersion < 2444)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_DAY_CONTAINS_EVENT + "=0");
        }

        if (oldVersion < 2446)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS + "=0");

            try {
                List<String> columns = getTableColums(db, DatabaseHandler.TABLE_EVENTS);
                if (columnExists(DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, columns/*, DatabaseHandler.TABLE_EVENTS*/)) {
                    final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                            DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS +
                            " FROM " + DatabaseHandler.TABLE_EVENTS;

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                            int ignoreAllDayEvents = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS));

                            if (ignoreAllDayEvents == 1) {
                                db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                        " SET " + DatabaseHandler.KEY_E_CALENDAR_ALL_DAY_EVENTS + "=1 " +
                                        "WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                            }
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
            } catch (Exception ignored) {
                //Log.e("DatabaseHandlerCreateUpdateDB.updateDb", Log.getStackTraceString(e));
            }
        }

        if (oldVersion < 2448)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACCESSORY_TYPE + "=\"\"");

            try {
                List<String> columns = getTableColums(db, DatabaseHandler.TABLE_EVENTS);
                if (columnExists(DatabaseHandler.KEY_E_PERIPHERAL_TYPE, columns/*, DatabaseHandler.TABLE_EVENTS*/)) {
                    final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                            DatabaseHandler.KEY_E_PERIPHERAL_TYPE +
                            " FROM " + DatabaseHandler.TABLE_EVENTS;

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID));
                            int peripheralType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIPHERAL_TYPE));

                            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS +
                                    " SET " + DatabaseHandler.KEY_E_ACCESSORY_TYPE + "=\"" + peripheralType + "\"" +
                                    " WHERE " + DatabaseHandler.KEY_E_ID + "=" + id);

                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
            } catch (Exception ignored) {
                //Log.e("DatabaseHandlerCreateUpdateDB.updateDb", Log.getStackTraceString(e));
            }
        }

        if (oldVersion < 2449)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_CAMERA_FLASH + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_CAMERA_FLASH + "=0");
        }

        if (oldVersion < 2450)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");
        }

        if (oldVersion < 2451)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");
        }

        if (oldVersion < 2453)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");
        }

        if (oldVersion < 2454)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_ONOFF_SIM2 + "=0");
        }

        if (oldVersion < 2459)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");
        }

        if (oldVersion < 2460)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");
        }

        if (oldVersion < 2461)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_FROM_SIM_SLOT + "=0");
        }

        if (oldVersion < 2462)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_CALL_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2463)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SMS_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2464)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_MOBILE_CELLS_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2466)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SOUND_PROFILE_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SOUND_PROFILE_RINGER_MODES + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SOUND_PROFILE_ZEN_MODES + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_SOUND_PROFILE_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2467)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER + "=\"\"");
        }

        if (oldVersion < 2468)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_LIVE_WALLPAPER + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VIBRATE_NOTIFICATIONS + "=0");
        }

        if (oldVersion < 2469)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_START_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_COUNTER + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_DURATION + "=5");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL + "=1");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_PERIODIC_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2470)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_CHANGE_WALLPAPER_TIME + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_CHANGE_WALLPAPER_TIME + "=0");
        }

        if (oldVersion < 2471)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER + "='-'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_WALLPAPER_FOLDER + "='-'");
        }

        if (oldVersion < 2472) {
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_PERIODIC_DURATION + "," +
                        DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        int multipleInterval = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL));
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_PERIODIC_DURATION));

                        if ((multipleInterval == 0) || (duration == 0)) {
                            if (multipleInterval == 0)
                                values.put(DatabaseHandler.KEY_E_PERIODIC_MULTIPLY_INTERVAL, 1);
                            if (duration == 0)
                                values.put(DatabaseHandler.KEY_E_PERIODIC_DURATION, 5);

                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2473)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + "=0");
        }

        if (oldVersion < 2474)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_VPN_SETTINGS_PREFS + "=0");
        }

        if (oldVersion < 2475)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_INTENTS + " SET " + DatabaseHandler.KEY_IN_DO_NOT_DELETE + "=0");
        }

        if (oldVersion < 2477) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "=0");
        }

        if (oldVersion < 2478) {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_RADIO_SWITCH_SIM_ON_OFF + "=0");
        }

        if (oldVersion < 2479)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_END_OF_ACTIVATION_TIME + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_END_OF_ACTIVATION_TYPE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_END_OF_ACTIVATION_TIME + "=0");
        }

        if (oldVersion < 2487)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2488)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE + "=0");
        }

        if (oldVersion < 2490)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_RINGTONE_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_MEDIA_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_ALARM_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_SYSTEM_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_VOICE_FROM  + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_FROM + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_FROM + "='0|0|0'");
        }

        if (oldVersion < 2491)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING + "=0");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ENABLE_PERIODIC_SCANNING + "=0");
        }

        if (oldVersion < 2492)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2493)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_START_PROFILE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_END_PROFILE + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ACTIVATED_PROFILE_RUNNING + "=0");
        }

        if (oldVersion < 2497)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_CHECK_NETWORK + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_CHECK_DATA + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_ROAMING_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2498)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_DEVICE_VPN + "='0|0|||0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_DEVICE_VPN + "='0|0|||0'");
        }

        if (oldVersion < 2500)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VPN_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VPN_CONNECTION_STATUS + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VPN_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2501)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + "='-1|1'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_RINGING + "='-1|1'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + "='-1|1'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_NOTIFICATIONS + "='-1|1'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION + "='-1|1'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VIBRATION_INTENSITY_TOUCH_INTERACTION + "='-1|1'");
        }

        if (oldVersion < 2502)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_VOLUME_MEDIA_CHANGE_DURING_PLAY + "=0");
        }

        if (oldVersion < 2504)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_ENABLED + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_FROM + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM + "='50|0|1|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_OPERATOR_TO + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_BRIGHTNESS_LEVEL_TO + "='50|0|1|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_BRIGHTNESS_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2505)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_RINGTONE_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_NOTIFICATION_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_MEDIA_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_ALARM_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_SYSTEM_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_VOICE_TO  + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_BLUETOOTHSCO_TO + "='0|0|0'");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_VOLUMES_ACCESSIBILITY_TO + "='0|0|0'");
        }

        if (oldVersion < 2510) {
//            Log.e("DatabaseHandler.updateDb", "---xxxx ---");
            try {
                final String selectQuery = "SELECT " + DatabaseHandler.KEY_E_ID + "," +
                        DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS +
                        " FROM " + DatabaseHandler.TABLE_EVENTS;
//                Log.e("DatabaseHandler.updateDb", "selectQuery="+selectQuery);

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        String cellsInDB = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS));
//                        Log.e("DatabaseHandler.updateDb", "cellsInDB="+cellsInDB);

                        if (!cellsInDB.isEmpty()) {

                            String cellNames = "";

                            String[] splits = cellsInDB.split(StringConstants.STR_SPLIT_REGEX);
                            for (String cell : splits) {
//                                Log.e("DatabaseHandler.updateDb", "cell=" + cell);

                                final String selectQuery2 = "SELECT " + DatabaseHandler.KEY_MC_NAME +
                                        " FROM " + DatabaseHandler.TABLE_MOBILE_CELLS +
                                        " WHERE " + DatabaseHandler.KEY_MC_CELL_ID + "=" + cell;
//                                Log.e("DatabaseHandler.updateDb", "selectQuery2=" + selectQuery2);

                                Cursor cursor2 = db.rawQuery(selectQuery2, null);

                                if (cursor2.moveToFirst()) {
                                    do {
                                        String cellName = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseHandler.KEY_MC_NAME));
//                                        Log.e("DatabaseHandler.updateDb", "cellName=" + cellName);

                                        if ((cellName != null) && (!cellName.isEmpty())) {
                                            boolean found = false;
                                            if (cellNames.startsWith(cellName + "|"))
                                                found = true;
                                            else if (cellNames.endsWith("|" + cellName))
                                                found = true;
                                            else if (cellNames.contains("|" + cellName + "|"))
                                                found = true;
                                            else if (cellNames.equals(cellName))
                                                found = true;

                                            if (!found) {
                                                if (!cellNames.isEmpty())
                                                    //noinspection StringConcatenationInLoop
                                                    cellNames = cellNames + "|";
                                                //noinspection StringConcatenationInLoop
                                                cellNames = cellNames + cellName;
                                            }
                                        }
                                    } while (cursor2.moveToNext());
                                }

                                cursor2.close();
                            }

//                            Log.e("DatabaseHandler.updateDb", "cellNames=" + cellNames);

                            ContentValues values = new ContentValues();

                            values.put(DatabaseHandler.KEY_E_MOBILE_CELLS_CELLS, cellNames);
                            db.update(DatabaseHandler.TABLE_EVENTS, values, DatabaseHandler.KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.KEY_E_ID))});
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2511)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL + "=10");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_PROFILES + " SET " + DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL + "=15");

            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_WIFI_SCAN_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_SCAN_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_BLUETOOTH_LE_SCAN_DURATION + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_LOCATION_UPDATE_INTERVAL + "=15");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_ORIENTATION_SCAN_INTERVAL + "=10");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MERGED_PROFILE + " SET " + DatabaseHandler.KEY_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL + "=15");
        }

        if (oldVersion < 2512)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_GEOFENCES + " SET " + DatabaseHandler.KEY_G_LATITUDE_T + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_GEOFENCES + " SET " + DatabaseHandler.KEY_G_LONGITUDE_T + "=\"\"");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_MOBILE_CELLS + " SET " + DatabaseHandler.KEY_MC_CELL_ID_T + "=\"\"");
        }

        if (oldVersion < 2513)
        {
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_APPLICATION_DURATION + "=0");
            db.execSQL("UPDATE " + DatabaseHandler.TABLE_EVENTS + " SET " + DatabaseHandler.KEY_E_APPLICATION_START_TIME + "=0");
        }

    }

}
