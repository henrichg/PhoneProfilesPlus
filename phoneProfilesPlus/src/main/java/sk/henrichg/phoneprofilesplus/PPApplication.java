package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.stericson.RootShell.RootShell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PPApplication extends Application {

    static String PACKAGE_NAME;

    private static boolean logIntoLogCat = false;
    private static boolean logIntoFile = false;
    private static boolean rootToolsDebug = false;
    public static String logFilterTags =  "PhoneProfilesHelper.doUninstallPPHelper"
                                         +"|PhoneProfilesHelper.isPPHelperInstalled"

                                         //+"|##### PPApplication.onCreate"
                                         +"|PPApplication._isRooted"
                                         +"|PPApplication.isRootGranted"

                                         //+"|$$$ PhoneProfilesService.onCreate"
                                         //+"|PhoneProfilesService.onDestroy"

                                         /*+"|##### PackageReplacedReceiver.onReceive"
                                         +"|ActivateProfileHelper.doExecuteForRadios"
                                         +"|$$$B ScannerService.onHandleIntent"
                                         +"|$$$BCL ScannerService.onHandleIntent"
                                         +"|$$$BLE ScannerService.onHandleIntent"
                                         +"|BluetoothScanBroadcastReceiver.finishScan"
                                         +"|@@@ BluetoothStateChangedBroadcastReceiver.onReceive"*/
                                         +"|BluetoothConnectionBroadcastReceiver"

                                         //+"|PhoneProfilesService.onStartCommand"
                                         //+"|PhoneProfilesService.startSimulatingRingingCall"
                                         //+"|PhoneProfilesService.stopSimulatingRingingCall"

                                         //+"|$$$ PhoneProfilesService.setMergedRingNotificationVolumes"

                                         //+"|$$$ WifiStateChangedBroadcastReceiver.onReceive"
                                         //+"|$$$ WifiConnectionBroadcastReceiver.onReceive"
                                         //+"|WifiScanBroadcastReceiver.onReceive"
                                         //+"|#### EventsService.onHandleIntent"

            ;


    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    public static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_BOOT = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_SERVICE = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    static final int STARTUP_SOURCE_LAUNCHER_START = 10;
    static final int STARTUP_SOURCE_LAUNCHER = 11;
    static final int STARTUP_SOURCE_SERVICE_MANUAL = 12;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 13;

    static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    static final int PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE = 3;

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 700425;
    static final int LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID = 700426;
    static final int LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID = 700427;
    static final int GEOFENCE_SCANNER_ERROR_NOTIFICATION_ID = 700428;
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 700429;
    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 700430;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 700431;

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";

    public static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    public static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    public static final String PREF_APPLICATION_ALERT = "applicationAlert";
    public static final String PREF_APPLICATION_CLOSE = "applicationClose";
    public static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    public static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    public static final String PREF_APPLICATION_THEME = "applicationTheme";
    public static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    public static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    public static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    public static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    public static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    public static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    public static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    public static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    public static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    public static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR  = "notificationShowInStatusBar";
    public static final String PREF_NOTIFICATION_TEXT_COLOR = "notificationTextColor";
    public static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    public static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    public static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    public static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    public static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    public static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
    public static final String PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER = "applicationEditorAutoCloseDrawer";
    public static final String PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE = "applicationEditorSaveEditorState";
    public static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    public static final String PREF_APPLICATION_HOME_LAUNCHER = "applicationHomeLauncher";
    public static final String PREF_APPLICATION_WIDGET_LAUNCHER = "applicationWidgetLauncher";
    public static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER = "applicationNotificationLauncher";
    public static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL = "applicationEventWifiScanInterval";
    public static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    public static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    public static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    public static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    public static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    public static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    public static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    public static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    public static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE = "applicationEventWifiScanInPowerSaveMode";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE = "applicationEventBluetoothScanInPowerSaveMode";
    public static final String PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    public static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION = "applicationEventBluetoothLEScanDuration";
    public static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL = "applicationEventLocationUpdateInterval";
    public static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE = "applicationEventLocationUpdateInPowerSaveMode";
    public static final String PREF_APPLICATION_EVENT_LOCATION_USE_GPS = "applicationEventLocationUseGPS";
    public static final String PREF_APPLICATION_EVENT_LOCATION_RESCAN = "applicationEventLocationRescan";
    public static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL = "applicationEventOrientationScanInterval";
    public static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventOrientationScanInPowerSaveMode";
    public static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE = "applicationEventMobileCellScanInPowerSaveMode";
    public static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN = "applicationEventMobileCellsRescan";
    public static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    public static final String PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS = "applicationDeleteOldActivityLogs";
    public static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    public static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";
    public static final String PREF_APPLICATION_EVENT_USE_PRIORITY = "applicationEventUsePriority";
    public static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    public static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";

    public static final int PREFERENCE_NOT_ALLOWED = 0;
    public static final int PREFERENCE_ALLOWED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 0;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 2;
    public static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 3;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 4;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 5;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 6;

    public static final String SCANNER_TYPE_WIFI = "wifi";
    public static final String SCANNER_TYPE_BLUETOOTH = "bluetooth";

    //public static final String RESCAN_TYPE_NONE = "0";
    public static final String RESCAN_TYPE_SCREEN_ON = "1";
    public static final String RESCAN_TYPE_RESTART_EVENTS = "2";
    public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

    // global internal preferences
    private static final String PREF_GLOBAL_EVENTS_RUN_STOP = "globalEventsRunStop";
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_EVENTS_BLOCKED = "eventsBlocked";
    private static final String PREF_FORCE_RUN_EVENT_RUNNING = "forceRunEventRunning";
    private static final String PREF_ACTIVATED_PROFILE_FOR_DURATION = "activatedProfileForDuration";
    private static final String PREF_ACTIVATED_PROFILE_END_DURATION_TIME = "activatedProfileEndDurationTime";
    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";
    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";
    private static final String PREF_FORCE_ONE_GEOFENCE_SCAN = "forceOneGeofenceScanInt";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START = "show_info_notification_on_start";
    private static final String PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION = "show_info_notification_on_start_version";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION = "show_request_write_settings_permission";
    private static final String PREF_MERGED_PERMISSIONS = "merged_permissions";
    private static final String PREF_MERGED_PERMISSIONS_COUNT = "merged_permissions_count";
    private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION = "show_enable_location_notification";
    private static final String PREF_APPLICATION_IN_FOREGROUND = "application_in_foreground";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION = "show_request_access_notification_policy_permission";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION = "mobile_cells_autoregistration_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION = "mobile_cells_autoregistration_remaining_duration";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME = "mobile_cells_autoregistration_cell_name";
    private static final String PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED = "mobile_cells_autoregistration_enabled";
    private static final String PREF_SCREEN_UNLOCKED = "screen_unlocked";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION = "show_request_draw_overlays_permission";
    private static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";

    public static final int FORCE_ONE_SCAN_DISABLED = 0;
    public static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    // alarm time offset (miliseconds) for events with generated alarms
    public static final int EVENT_ALARM_TIME_OFFSET = 15000;

    // preferences for event - filled with broadcast receivers
    static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
    static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
    static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
    static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";
    static final String PREF_EVENT_WIFI_SCAN_REQUEST = "eventWifiScanRequest";
    static final String PREF_EVENT_WIFI_WAIT_FOR_RESULTS = "eventWifiWaitForResults";
    static final String PREF_EVENT_WIFI_ENABLED_FOR_SCAN = "eventWifiEnabledForScan";
    static final String PREF_EVENT_BLUETOOTH_SCAN_REQUEST = "eventBluetoothScanRequest";
    static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS = "eventBluetoothWaitForResults";
    static final String PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST = "eventBluetoothLEScanRequest";
    static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS = "eventBluetoothWaitForLEResults";
    static final String PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN = "eventBluetoothEnabledForScan";
    //static final String PREF_EVENT_WIFI_LAST_STATE = "eventWifiLastState";
    //static final String PREF_EVENT_BLUETOOTH_LAST_STATE = "eventBluetoothLastState";

    // scanner start/stop types
    static final int SCANNER_START_GEOFENCE_SCANNER = 1;
    static final int SCANNER_STOP_GEOFENCE_SCANNER = 2;
    static final int SCANNER_START_ORIENTATION_SCANNER = 3;
    static final int SCANNER_STOP_ORIENTATION_SCANNER = 4;
    static final int SCANNER_START_PHONE_STATE_SCANNER = 5;
    static final int SCANNER_STOP_PHONE_STATE_SCANNER = 6;

    public static boolean applicationStartOnBoot;
    public static boolean applicationActivate;
    public static boolean applicationActivateWithAlert;
    public static boolean applicationClose;
    public static boolean applicationLongClickActivation;
    public static String applicationLanguage;
    public static String applicationTheme;
    public static boolean applicationActivatorPrefIndicator;
    public static boolean applicationEditorPrefIndicator;
    public static boolean applicationActivatorHeader;
    public static boolean applicationEditorHeader;
    public static boolean notificationsToast;
    public static boolean notificationStatusBar;
    public static boolean notificationStatusBarPermanent;
    public static String notificationStatusBarCancel;
    public static String notificationStatusBarStyle;
    public static boolean notificationShowInStatusBar;
    public static String notificationTextColor;
    public static String notificationTheme;
    public static boolean notificationHideInLockscreen;
    public static boolean applicationWidgetListPrefIndicator;
    public static boolean applicationWidgetListHeader;
    public static String applicationWidgetListBackground;
    public static String applicationWidgetListLightnessB;
    public static String applicationWidgetListLightnessT;
    public static String applicationWidgetIconColor;
    public static String applicationWidgetIconLightness;
    public static String applicationWidgetListIconColor;
    public static String applicationWidgetListIconLightness;
    public static boolean applicationEditorAutoCloseDrawer;
    public static boolean applicationEditorSaveEditorState;
    public static boolean notificationPrefIndicator;
    public static String applicationHomeLauncher;
    public static String applicationWidgetLauncher;
    public static String applicationNotificationLauncher;
    public static int applicationEventWifiScanInterval;
    public static boolean applicationEventWifiEnableWifi;
    public static String applicationBackgroundProfile;
    public static boolean applicationActivatorGridLayout;
    public static boolean applicationWidgetListGridLayout;
    public static int applicationEventBluetoothScanInterval;
    public static boolean applicationEventBluetoothEnableBluetooth;
    public static String applicationEventWifiRescan;
    public static String applicationEventBluetoothRescan;
    public static boolean applicationWidgetIconHideProfileName;
    public static boolean applicationUnlinkRingerNotificationVolumes;
    public static boolean applicationShortcutEmblem;
    public static String applicationEventWifiScanInPowerSaveMode;
    public static String applicationEventBluetoothScanInPowerSaveMode;
    public static String applicationPowerSaveModeInternal;
    public static int applicationEventBluetoothLEScanDuration;
    public static int applicationEventLocationUpdateInterval;
    public static String applicationEventLocationUpdateInPowerSaveMode;
    public static boolean applicationEventLocationUseGPS;
    public static String applicationEventLocationRescan;
    public static int applicationEventOrientationScanInterval;
    public static String applicationEventOrientationScanInPowerSaveMode;
    public static String applicationEventMobileCellsScanInPowerSaveMode;
    public static String applicationEventMobileCellsRescan;
    public static int applicationDeleteOldActivityLogs;
    public static String applicationWidgetIconBackground;
    public static String applicationWidgetIconLightnessB;
    public static String applicationWidgetIconLightnessT;
    public static boolean applicationEventUsePriority;
    public static int applicationForceSetMergeRingNotificationVolumes;

    public static int notAllowedReason;
    public static String notAllowedReasonDetail;

    //public static final RootMutex rootMutex = new RootMutex();
    public static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    public static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    public static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    public static final ScanResultsMutex scanResultsMutex = new ScanResultsMutex();

    public static boolean isPowerSaveMode = false;

    public static Notification phoneProfilesNotification = null;

    public static boolean startedOnBoot = false;

    public static LockDeviceActivity lockDeviceActivity = null;
    public static int screenTimeoutBeforeDeviceLock = 0;

    @Override
    public void onCreate()
    {
        int actualVersionCode = 0;
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(actualVersionCode));

    //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        super.onCreate();

        PPApplication.logE("##### PPApplication.onCreate", "xxx");

        //firstStartServiceStarted = false;

        PACKAGE_NAME = this.getPackageName();

        // initialization
        loadPreferences(this);

        PPApplication.initRoot();

        /*
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        */

        //Log.d("PPApplication.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());

        //Log.d("PPApplication.onCreate","xxx");

    }

    @Override
    public void onTerminate ()
    {
        DatabaseHandler.getInstance(this).closeConnection();
        super.onTerminate();
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    @SuppressLint("SimpleDateFormat")
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException ignored) {
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = logFilterTags.split("\\|");
        for (int i = 0; i < splits.length; i++)
        {
            if (tag.contains(splits[i]))
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    static public void logI(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    static public void logW(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    static public void logE(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    static public void logD(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    //--------------------------------------------------------------

    static public void loadPreferences(Context context)
    {
        PPApplication.logE("PPApplication.loadPreferences", "xxx");

        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        applicationStartOnBoot = preferences.getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
        applicationActivate = preferences.getBoolean(PREF_APPLICATION_ACTIVATE, true);
        applicationActivateWithAlert = preferences.getBoolean(PREF_APPLICATION_ALERT, true);
        applicationClose = preferences.getBoolean(PREF_APPLICATION_CLOSE, true);
        applicationLongClickActivation = preferences.getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
        applicationLanguage = preferences.getString(PREF_APPLICATION_LANGUAGE, "system");
        applicationTheme = preferences.getString(PREF_APPLICATION_THEME, "material");
        applicationActivatorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        applicationEditorPrefIndicator = preferences.getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        applicationActivatorHeader = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        applicationEditorHeader = preferences.getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        notificationsToast = preferences.getBoolean(PREF_NOTIFICATION_TOAST, true);
        notificationStatusBar = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        notificationStatusBarPermanent = preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        notificationStatusBarCancel = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        notificationStatusBarStyle = preferences.getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
        notificationShowInStatusBar = preferences.getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
        notificationTextColor = preferences.getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
        notificationHideInLockscreen = preferences.getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
        notificationTheme = preferences.getString(PREF_NOTIFICATION_THEME, "0");
        applicationWidgetListPrefIndicator = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
        applicationWidgetListHeader = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
        applicationWidgetListBackground = preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
        applicationWidgetListLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
        applicationWidgetListLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
        applicationWidgetIconColor = preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
        applicationWidgetIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
        applicationWidgetListIconColor = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
        applicationWidgetListIconLightness = preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
        applicationEditorAutoCloseDrawer = preferences.getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
        applicationEditorSaveEditorState = preferences.getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
        notificationPrefIndicator = preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
        applicationHomeLauncher = preferences.getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
        applicationWidgetLauncher = preferences.getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
        applicationNotificationLauncher = preferences.getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
        applicationEventWifiScanInterval = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "10"));
        applicationEventWifiEnableWifi = preferences.getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
        applicationBackgroundProfile = preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
        applicationActivatorGridLayout = preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
        applicationWidgetListGridLayout = preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
        applicationEventBluetoothScanInterval = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "10"));
        applicationEventBluetoothEnableBluetooth = preferences.getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
        applicationEventWifiRescan = preferences.getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
        applicationEventBluetoothRescan = preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
        applicationWidgetIconHideProfileName = preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
        applicationUnlinkRingerNotificationVolumes = preferences.getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
        applicationShortcutEmblem = preferences.getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
        applicationEventWifiScanInPowerSaveMode = preferences.getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
        applicationEventBluetoothScanInPowerSaveMode = preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
        applicationPowerSaveModeInternal = preferences.getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "2");
        applicationEventBluetoothLEScanDuration = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
        applicationEventLocationUpdateInterval  = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "5"));
        applicationEventLocationUpdateInPowerSaveMode = preferences.getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
        applicationEventLocationUseGPS = preferences.getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
        applicationEventLocationRescan = preferences.getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
        applicationEventOrientationScanInterval = Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "5"));
        applicationEventOrientationScanInPowerSaveMode = preferences.getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
        applicationEventMobileCellsScanInPowerSaveMode = preferences.getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
        applicationEventMobileCellsRescan = preferences.getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
        applicationDeleteOldActivityLogs = Integer.valueOf(preferences.getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
        applicationWidgetIconBackground = preferences.getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "0");
        applicationWidgetIconLightnessB = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
        applicationWidgetIconLightnessT = preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
        applicationEventUsePriority = preferences.getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
        applicationForceSetMergeRingNotificationVolumes = Integer.valueOf(preferences.getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));

        if (applicationTheme.equals("light"))
        {
            applicationTheme = "material";
            Editor editor = preferences.edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.commit();
        }
    }

    static public boolean getGlobalEventsRuning(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_GLOBAL_EVENTS_RUN_STOP, true);
    }

    static public void setGlobalEventsRuning(Context context, boolean globalEventsRuning)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_GLOBAL_EVENTS_RUN_STOP, globalEventsRuning);
        editor.commit();
    }

    static public boolean getApplicationStarted(Context context, boolean testService)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        if (testService)
            return preferences.getBoolean(PREF_APPLICATION_STARTED, false) && (PhoneProfilesService.instance != null);
        else
            return preferences.getBoolean(PREF_APPLICATION_STARTED, false);
    }

    static public void setApplicationStarted(Context context, boolean appStarted)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
        editor.commit();
    }

    static public boolean getEventsBlocked(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_EVENTS_BLOCKED, false);
    }

    static public void setEventsBlocked(Context context, boolean eventsBlocked)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENTS_BLOCKED, eventsBlocked);
        editor.commit();
    }

    static public boolean getForceRunEventRunning(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_FORCE_RUN_EVENT_RUNNING, false);
    }

    static public void setForceRunEventRunning(Context context, boolean forceRunEventRunning)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_FORCE_RUN_EVENT_RUNNING, forceRunEventRunning);
        editor.commit();
    }

    static public long getActivatedProfileForDuration(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, 0);
    }

    static public void setActivatedProfileForDuration(Context context, long profileId)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_FOR_DURATION, profileId);
        editor.commit();
    }

    static public long getActivatedProfileEndDurationTime(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, 0);
    }

    static public void setActivatedProfileEndDurationTime(Context context, long time)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(PREF_ACTIVATED_PROFILE_END_DURATION_TIME, time);
        editor.commit();
    }

    static public int getForceOneWifiScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneWifiScan(Context context, int forceScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
        editor.commit();
    }

    static public int getForceOneBluetoothScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneBluetoothScan(Context context, int forceScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
        editor.commit();
    }

    static public int getForceOneLEBluetoothScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneLEBluetoothScan(Context context, int forceScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, forceScan);
        editor.commit();
    }

    /*
    static public int getForceOneGeofenceScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_FORCE_ONE_GEOFENCE_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneGeofenceScan(Context context, int forceScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_FORCE_ONE_GEOFENCE_SCAN, forceScan);
        editor.commit();
    }
    */

    static public boolean getLockscreenDisabled(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_LOCKSCREEN_DISABLED, false);
    }

    static public void setLockscreenDisabled(Context context, boolean disabled)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_LOCKSCREEN_DISABLED, disabled);
        editor.commit();
    }

    static public boolean getScreenUnlocked(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SCREEN_UNLOCKED, true);
    }

    static public void setScreenUnlocked(Context context, boolean unlocked)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SCREEN_UNLOCKED, unlocked);
        editor.commit();
    }

    static public int getRingerVolume(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_RINGER_VOLUME, -999);
    }

    static public void setRingerVolume(Context context, int volume)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_RINGER_VOLUME, volume);
        editor.commit();
    }

    static public int getNotificationVolume(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_NOTIFICATION_VOLUME, -999);
    }

    static public void setNotificationVolume(Context context, int volume)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
        editor.commit();
    }

    static public int getRingerMode(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_RINGER_MODE, 0);
    }

    static public void setRingerMode(Context context, int mode)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_RINGER_MODE, mode);
        editor.commit();
    }

    static public int getZenMode(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_ZEN_MODE, 0);
    }

    static public void setZenMode(Context context, int mode)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_ZEN_MODE, mode);
        editor.commit();
    }

    static public int getSavedVersionCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static public void setSavedVersionCode(Context context, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.commit();
    }

    static public boolean getShowInfoNotificationOnStart(Context context, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        boolean show = preferences.getBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, true);
        int _version = preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        return ((_version >= version) && show);
    }

    static public void setShowInfoNotificationOnStart(Context context, boolean show, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_INFO_NOTIFICATION_ON_START, show);
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.commit();
    }

    static public int getShowInfoNotificationOnStartVersion(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, 0);
    }

    static public void setShowInfoNotificationOnStartVersion(Context context, int version)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_SHOW_INFO_NOTIFICATION_ON_START_VERSION, version);
        editor.commit();
    }

    static public boolean getShowEnableLocationNotification(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION, true);
    }

    static public void setShowEnableLocationNotification(Context context, boolean show)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION, show);
        editor.commit();
    }

    static public boolean getShowRequestWriteSettingsPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, true);
    }

    static public void setShowRequestWriteSettingsPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_WRITE_SETTINGS_PERMISSION, value);
        editor.commit();
    }

    static public boolean getShowRequestAccessNotificationPolicyPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, true);
    }

    static public void setShowRequestAccessNotificationPolicyPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_ACCESS_NOTIFICATION_POLICY_PERMISSION, value);
        editor.commit();
    }

    static public boolean getShowRequestDrawOverlaysPermission(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, true);
    }

    static public void setShowRequestDrawOverlaysPermission(Context context, boolean value)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_SHOW_REQUEST_DRAW_OVERLAYS_PERMISSION, value);
        editor.commit();
    }

    static public List<Permissions.PermissionType> getMergedPermissions(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);

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

    static public void addMergedPermissions(Context context, List<Permissions.PermissionType> permissions)
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
                savedPermissions.add(new Permissions.PermissionType(permission.preference, permission.permission));
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(PREF_MERGED_PERMISSIONS_COUNT, savedPermissions.size());

        Gson gson = new Gson();

        for (int i = 0; i < savedPermissions.size(); i++)
        {
            String json = gson.toJson(savedPermissions.get(i));
            editor.putString(PREF_MERGED_PERMISSIONS+i, json);
        }

        editor.commit();
    }

    static public void clearMergedPermissions(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PERMISSIONS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    static public String getApplicationInForeground(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_IN_FOREGROUND, "");
    }

    static public void setApplicationInForeground(Context context, String application)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(PREF_APPLICATION_IN_FOREGROUND, application);
        editor.commit();
    }

    static public boolean getActivityLogEnabled(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_ACTIVITY_LOG_ENABLED, true);
    }

    static public void setActivityLogEnabled(Context context, boolean enabled)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_ACTIVITY_LOG_ENABLED, enabled);
        editor.commit();
    }

    static public void getMobileCellsAutoRegistration(Context context) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            PhoneProfilesService.phoneStateScanner.durationForAutoRegistration = preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, 0);
            PhoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration = preferences.getString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, "");
            PhoneProfilesService.phoneStateScanner.enabledAutoRegistration = preferences.getBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
        }
    }

    static public void setMobileCellsAutoRegistration(Context context, boolean firstStart) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_DURATION, PhoneProfilesService.phoneStateScanner.durationForAutoRegistration);
            editor.putString(PREF_MOBILE_CELLS_AUTOREGISTRATION_CELLS_NAME, PhoneProfilesService.phoneStateScanner.cellsNameForAutoRegistration);
            if (firstStart)
                editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, false);
            else
                editor.putBoolean(PREF_MOBILE_CELLS_AUTOREGISTRATION_ENABLED, PhoneProfilesService.phoneStateScanner.enabledAutoRegistration);
            editor.commit();
        }
    }

    static public int getMobileCellsAutoRegistrationRemainingDuration(Context context) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, 0);
        }
        return 0;
    }

    static public void setMobileCellsAutoRegistrationRemainingDuration(Context context, int remainingDuration) {
        if (PhoneProfilesService.isPhoneStateStarted()) {
            SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putInt(PREF_MOBILE_CELLS_AUTOREGISTRATION_REMAINING_DURATION, remainingDuration);
            editor.commit();
        }
    }

    static public int getDaysAfterFirtsStart(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }

    static public void setDaysAfterFirstStart(Context context, int days)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.commit();
    }

    static public int getActivatedProfileScreenTimeout(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, 0);
    }

    static public void setActivatedProfileScreenTimeout(Context context, int timeout)
    {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, timeout);
        editor.commit();
    }

    static int isProfilePreferenceAllowed(String preferenceKey, Context context)
    {
        int featurePresented = PREFERENCE_NOT_ALLOWED;

        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 17)
            {
                if (isRooted())
                {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device ma Wifi
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                // device ma bluetooth
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    if (isRooted()) {
                        // zariadenie je rootnute
                        //if (serviceBinaryExists() && telephonyServiceExists(context, PREF_PROFILE_DEVICE_MOBILE_DATA))
                            featurePresented = PREFERENCE_ALLOWED;
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                {
                    if (canSetMobileData(context))
                        featurePresented = PREFERENCE_ALLOWED;
                    else {
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                        notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                    }
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            {
                featurePresented = PREFERENCE_ALLOWED;
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_GPS))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))
            {
                // device ma gps
                /*if (android.os.Build.VERSION.SDK_INT < 17)
                {
                    if (isRooted(false))
                        featurePresented = PREFERENCE_ALLOWED;
                }
                else*/
                if (isRooted())
                {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                if (canExploitGPS(context))
                {
                    featurePresented = PREFERENCE_ALLOWED;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_NFC))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
            {
                logE("PPApplication.hardwareCheck","NFC=presented");

                // device ma nfc
                if (isRooted())
                    featurePresented = PREFERENCE_ALLOWED;
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            {
                logE("PPApplication.hardwareCheck","NFC=not presented");
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                // device ma Wifi
                if (WifiApManager.canExploitWifiAP(context))
                {
                    featurePresented = PREFERENCE_ALLOWED;
                }
                else {
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_cant_be_change);
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
        {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                {
                    if (isRooted())
                    {
                        // zariadenie je rootnute
                        if (settingsBinaryExists())
                            featurePresented = PREFERENCE_ALLOWED;
                        else
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else
                    featurePresented = PREFERENCE_ALLOWED;
            }
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
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
                    if (isRooted()) {
                        // zariadenie je rootnute
                        if (telephonyServiceExists(context, Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                            if (serviceBinaryExists())
                                featurePresented = PREFERENCE_ALLOWED;
                            else
                                notAllowedReason = PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND;
                        }
                        else {
                            notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                        }
                    }
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
                }
                else {
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                    notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_network_type);
                }
            }
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            int value = Settings.System.getInt(context.getContentResolver(), "notification_light_pulse", -10);
            if ((value != -10) && (android.os.Build.VERSION.SDK_INT >= 23)) {
                if (isRooted()) {
                    // zariadenie je rootnute
                    if (settingsBinaryExists())
                        featurePresented = PREFERENCE_ALLOWED;
                    else
                        notAllowedReason = PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND;
                }
                else
                    notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_ROOTED;
            }
            else
            if (value != -10)
                featurePresented = PREFERENCE_ALLOWED;
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
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
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_secure_lock);
            }
            else
                featurePresented = PREFERENCE_ALLOWED;
        }
        else
        if (preferenceKey.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device ma Wifi
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
            featurePresented = PREFERENCE_ALLOWED;

        return featurePresented;
    }

    static int isEventPreferenceAllowed(String preferenceKey, Context context)
    {
        int featurePresented = PREFERENCE_NOT_ALLOWED;

        if (preferenceKey.equals(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
                // device ma Wifi
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                // device ma bluetooth
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED))
        {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                featurePresented = PREFERENCE_ALLOWED;
            else {
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }
        }
        else
        if (preferenceKey.equals(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED))
        {
            boolean enabled = (PhoneProfilesService.getAccelerometerSensor(context.getApplicationContext()) != null) &&
                              (PhoneProfilesService.getMagneticFieldSensor(context.getApplicationContext()) != null) &&
                              (PhoneProfilesService.getAccelerometerSensor(context.getApplicationContext()) != null);
            if (enabled)
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
                // device ma bluetooth
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
        if (preferenceKey.equals(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED))
        {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
                // device ma bluetooth
                featurePresented = PREFERENCE_ALLOWED;
            else
                notAllowedReason = PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
        }
        else
            featurePresented = PREFERENCE_ALLOWED;

        return featurePresented;
    }

    public static String getNotAllowedPreferenceReasonString(Context context) {
        switch (notAllowedReason) {
            case PREFERENCE_NOT_ALLOWED_NO_HARDWARE: return context.getString(R.string.preference_not_allowed_reason_no_hardware);
            case PREFERENCE_NOT_ALLOWED_NOT_ROOTED: return context.getString(R.string.preference_not_allowed_reason_not_rooted);
            case PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_settings_not_found);
            case PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND: return context.getString(R.string.preference_not_allowed_reason_service_not_found);
            case PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS: return context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM:
                return context.getString(R.string.preference_not_allowed_reason_not_supported) + " (" + notAllowedReasonDetail + ")";
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_by_application) + " (" + notAllowedReasonDetail + ")";
            default: return context.getString(R.string.empty_string);
        }
    }

    static boolean canExploitGPS(Context context)
    {
        // test expoiting power manager widget
        PackageManager pacman = context.getPackageManager();
        try {
            PackageInfo pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

            if(pacInfo != null){
                for(ActivityInfo actInfo : pacInfo.receivers){
                    //test if recevier is exported. if so, we can toggle GPS.
                    if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                        return true;
                    }
                }
            }
        } catch (NameNotFoundException e) {
            return false; //package not found
        }
        return false;
    }

    static boolean canSetMobileData(Context context)
    {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    static public String getTransactionCode(Context context, String fieldName) throws Exception {
        //try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        //} catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            //e.printStackTrace();
        //    throw e;
        //}
    }

    /*static public String getTransactionCode(String className, String methodName) throws Exception {
        //try {
        final String stubName = className + "$Stub";
        final String fieldName = "TRANSACTION_" + methodName;

        final Class<?> cls = Class.forName(stubName);
        final Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return String.valueOf(declaredField.getInt(cls));
        //} catch (Exception e) {
        // The "TRANSACTION_setDataEnabled" field is not available,
        // or named differently in the current API level, so we throw
        // an exception and inform users that the method is not available.
        //e.printStackTrace();
        //    throw e;
        //}
    }*/

    static boolean telephonyServiceExists(Context context, String preference) {
        try {
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                getTransactionCode(context, "TRANSACTION_setDataEnabled");
            }
            else
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    // --------------------------------

    //static private boolean rootChecked;
    static private boolean rooted;
    //static private boolean settingsBinaryChecked;
    static private boolean settingsBinaryExists;
    //static private boolean isSELinuxEnforcingChecked;
    static private boolean isSELinuxEnforcing;
    //static private String suVersion;
    //static private boolean suVersionChecked;
    //static private boolean serviceBinaryChecked;
    static private boolean serviceBinaryExists;

    static synchronized void initRoot() {
        //synchronized (PPApplication.rootMutex) {
            //rootChecked = false;
            rooted = false;
            //settingsBinaryChecked = false;
            settingsBinaryExists = false;
            //isSELinuxEnforcingChecked = false;
            isSELinuxEnforcing = false;
            //suVersion = null;
            //suVersionChecked = false;
            //serviceBinaryChecked = false;
            serviceBinaryExists = false;
        //}
    }

    private static boolean _isRooted()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!rootChecked)
        if (!rooted)
        {
            PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //rootChecking = true;
            /*try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //if (RootTools.isRootAvailable()) {
            if (RootToolsSmall.isRooted()) {
                // zariadenie je rootnute
                PPApplication.logE("PPApplication._isRooted", "root available");
                //rootChecked = true;
                rooted = true;
            } else {
                PPApplication.logE("PPApplication._isRooted", "root NOT available");
                //rootChecked = true;
                rooted = false;
                settingsBinaryExists = false;
                //settingsBinaryChecked = false;
                //isSELinuxEnforcingChecked = false;
                isSELinuxEnforcing = false;
                //suVersionChecked = false;
                //suVersion = null;
                serviceBinaryExists = false;
                //serviceBinaryChecked = false;
            }
        }
        //if (rooted)
        //	getSUVersion();
        return rooted;
    }

    static boolean isRooted() {
        //synchronized (PPApplication.rootMutex) {
            _isRooted();
        //}
        return rooted;
    }

    static boolean isRootGranted()
    {
        RootShell.debugMode = rootToolsDebug;

        //synchronized (PPApplication.rootMutex) {

            if (_isRooted()) {
                PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                //grantChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                if (RootTools.isAccessGiven()) {
                    // root grantnuty
                    PPApplication.logE("PPApplication.isRootGranted", "root granted");
                    return true;
                } else {
                    // grant odmietnuty
                    PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                    return false;
                }
            }
            else {
                PPApplication.logE("PPApplication.isRootGranted", "not rooted");
                return false;
            }
        //}
    }

    static boolean settingsBinaryExists()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!settingsBinaryChecked)
        if (!settingsBinaryExists)
        {
            //synchronized (PPApplication.rootMutex) {
                PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                //settingsBinaryChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //List<String> settingsPaths = RootTools.findBinary("settings");
                //settingsBinaryExists = settingsPaths.size() > 0;
                settingsBinaryExists = RootToolsSmall.hasSettingBin();
                //settingsBinaryChecked = true;
            //}
        }
        PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists="+settingsBinaryExists);
        return settingsBinaryExists;
    }

    static boolean serviceBinaryExists()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!serviceBinaryChecked)
        if (!serviceBinaryExists)
        {
            //synchronized (PPApplication.rootMutex) {
                PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                //serviceBinaryChecking = true;
                /*try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //List<String> servicePaths = RootTools.findBinary("service");
                //serviceBinaryExists = servicePaths.size() > 0;
                serviceBinaryExists = RootToolsSmall.hasServiceBin();
                //serviceBinaryChecked = true;
            //}
        }
        PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists="+serviceBinaryExists);
        return serviceBinaryExists;
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     * 
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    public static boolean isSELinuxEnforcing()
    {
        RootShell.debugMode = rootToolsDebug;

        //if (!isSELinuxEnforcingChecked)
        if (!isSELinuxEnforcing)
        {
            //synchronized (PPApplication.rootMutex) {
                boolean enforcing = false;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                            try {
                                enforcing = (is.read() == '1');
                            } finally {
                                is.close();
                            }
                        } catch (Exception ignored) {
                        }
                    }

                /*
                // 4.4+ builds are enforcing by default, take the gamble
                if (!enforcing)
                {
                    enforcing = (android.os.Build.VERSION.SDK_INT >= 19);
                }
                */
                }

                isSELinuxEnforcing = enforcing;
                //isSELinuxEnforcingChecked = true;
            //}
        }
        
        PPApplication.logE("PPApplication.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);
        
        return isSELinuxEnforcing;
    }

    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
        if ((suVersion != null) && suVersion.contains("SUPERSU"))
            return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
        else
            return command;
    }

    public static String getSUVersion()
    {
        if (!suVersionChecked)
        {
            Command command = new Command(0, false, "su -v")
            {
                @Override
                public void commandOutput(int id, String line) {
                    suVersion = line;

                    super.commandOutput(id, line);
                }
            }
            ;
            try {
                RootTools.closeAllShells();
                RootTools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
            } catch (Exception e) {
                Log.e("PPApplication.getSUVersion", "Error on run su");
            }
        }
        return suVersion;
    }
    
    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 msec (3200 * 2 - 50)

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("GlobaData.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
    */

    public static String getJavaCommandFile(Class<?> mainClass, String name, Context context, Object cmdParam) {
        try {
            String cmd =
                    "#!/system/bin/sh\n" +
                            "base=/system\n" +
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            /*String dir = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).dataDir;
            File fDir = new File(dir);
            File file = new File(fDir, name);
            OutputStream out = new FileOutputStream(file);
            out.write(cmd.getBytes());
            out.close();*/

            FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(name);
            //noinspection ResultOfMethodCallIgnored
            file.setExecutable(true);

            return file.getAbsolutePath();

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void startGeofenceScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_GEOFENCE_SCANNER);
        context.startService(lIntent);
    }

    public static void stopGeofenceScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_GEOFENCE_SCANNER);
        context.startService(lIntent);
    }

    public static void startOrientationScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_ORIENTATION_SCANNER);
        context.startService(lIntent);
    }

    public static void stopOrientationScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_ORIENTATION_SCANNER);
        context.startService(lIntent);
    }

    public static void startPhoneStateScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_PHONE_STATE_SCANNER);
        context.startService(lIntent);
    }

    public static void stopPhoneStateScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_PHONE_STATE_SCANNER);
        context.startService(lIntent);
    }

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    public static int getResourceId(String pVariableName, String pResourcename, Context context)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourcename, context.getPackageName());
        } catch (Exception e) {
            //e.printStackTrace();
            return -1;
        }
    }

    public static void sleep(long ms) {
        long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);
    }

    public static boolean canChangeZenMode(Context context, boolean notCheckAccess) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60) {
                if (notCheckAccess)
                    return true;
                else
                    return Permissions.checkAccessNotificationPolicy(context);
            }
            else
                return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23))
            return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSystemZenMode(Context context, int defaultValue) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60) {
                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                int interuptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                switch (interuptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        return ActivateProfileHelper.ZENMODE_ALL;
                }
            }
            else {
                int interuptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
                switch (interuptionFilter) {
                    case 0:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case 1:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case 2:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case 3:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                }
            }
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) {
            int interuptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
            switch (interuptionFilter) {
                case 0:
                    return ActivateProfileHelper.ZENMODE_ALL;
                case 1:
                    return ActivateProfileHelper.ZENMODE_PRIORITY;
                case 2:
                    return ActivateProfileHelper.ZENMODE_NONE;
                case 3:
                    return ActivateProfileHelper.ZENMODE_ALARMS;
            }
        }
        return defaultValue;
    }

    public static boolean getMergedRingNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        if (applicationForceSetMergeRingNotificationVolumes > 0)
            return applicationForceSetMergeRingNotificationVolumes == 1;
        else
            return preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
    }

    // test if ring and notification volumes are merged
    public static void setMergedRingNotificationVolumes(Context context, boolean force) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        PPApplication.logE("$$$ PhoneProfilesService.setMergedRingNotificationVolumes", "xxx");

        if (!preferences.contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
            try {
                boolean merged;
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = audioManager.getRingerMode();
                int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                int oldRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                int oldNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                if (oldRingVolume == oldNotificationVolume) {
                    int newNotificationVolume;
                    if (oldNotificationVolume == maximumNotificationValue)
                        newNotificationVolume = oldNotificationVolume - 1;
                    else
                        newNotificationVolume = oldNotificationVolume + 1;
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, 0);
                    PPApplication.sleep(1000);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume)
                        merged = true;
                    else
                        merged = false;
                } else
                    merged = false;
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                audioManager.setRingerMode(ringerMode);

                //Log.d("PPApplication.setMergedRingNotificationVolumes", "merged="+merged);

                Editor editor = preferences.edit();
                editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                editor.commit();
            } catch (Exception ignored) { }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean vibrationIsOn(Context context, AudioManager audioManager, boolean testRingerMode) {
        int ringerMode = -999;
        if (testRingerMode)
            ringerMode = audioManager.getRingerMode();
        int vibrateType = -999;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        //int vibrateWhenRinging;
        //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        //else
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        PPApplication.logE("PPApplication.vibrationIsOn", "ringerMode="+ringerMode);
        PPApplication.logE("PPApplication.vibrationIsOn", "vibrateType="+vibrateType);
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateWhenRinging="+vibrateWhenRinging);

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT);// ||
                //(vibrateWhenRinging == 1);
    }

    public static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

}
