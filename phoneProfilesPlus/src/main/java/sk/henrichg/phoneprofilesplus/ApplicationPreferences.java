package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;

class ApplicationPreferences {

    static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    private  static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    static final String PREF_APPLICATION_ALERT = "applicationAlert";
    static final String PREF_APPLICATION_CLOSE = "applicationClose";
    static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    static final String PREF_APPLICATION_THEME = "applicationTheme";
    static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    private static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR  = "notificationShowInStatusBar";
    static final String PREF_NOTIFICATION_TEXT_COLOR = "notificationTextColor";
    static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
    private static final String PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER = "applicationEditorAutoCloseDrawer";
    static final String PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE = "applicationEditorSaveEditorState";
    private static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    static final String PREF_APPLICATION_HOME_LAUNCHER = "applicationHomeLauncher";
    static final String PREF_APPLICATION_WIDGET_LAUNCHER = "applicationWidgetLauncher";
    static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER = "applicationNotificationLauncher";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL = "applicationEventWifiScanInterval";
    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    private static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE = "applicationEventWifiScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE = "applicationEventBluetoothScanInPowerSaveMode";
    static final String PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION = "applicationEventBluetoothLEScanDuration";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL = "applicationEventLocationUpdateInterval";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE = "applicationEventLocationUpdateInPowerSaveMode";
    private static final String PREF_APPLICATION_EVENT_LOCATION_USE_GPS = "applicationEventLocationUseGPS";
    static final String PREF_APPLICATION_EVENT_LOCATION_RESCAN = "applicationEventLocationRescan";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL = "applicationEventOrientationScanInterval";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventOrientationScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE = "applicationEventMobileCellScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN = "applicationEventMobileCellsRescan";
    static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    static final String PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS = "applicationDeleteOldActivityLogs";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";
    static final String PREF_APPLICATION_EVENT_USE_PRIORITY = "applicationEventUsePriority";
    static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";

    static boolean applicationStartOnBoot(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static boolean applicationActivate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static boolean applicationActivateWithAlert(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static boolean applicationClose(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static boolean applicationLongClickActivation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    static String applicationLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_LANGUAGE, "system");
    }

    static public String applicationTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        String applicationTheme = preferences.getString(PREF_APPLICATION_THEME, "material");
        if (applicationTheme.equals("light")){
            applicationTheme = "material";
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.commit();
        }
        return applicationTheme;
    }

    static boolean applicationActivatorPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
    }

    static public boolean applicationEditorPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }

    static boolean applicationActivatorHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
    }

    static boolean applicationEditorHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
    }

    static boolean notificationsToast(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    static boolean notificationStatusBar(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
    }

    static boolean notificationStatusBarPermanent(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
    }

    static String notificationStatusBarCancel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
    }

    static String notificationStatusBarStyle(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static boolean notificationShowInStatusBar(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static String notificationTextColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static boolean notificationHideInLockscreen(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    static String notificationTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_NOTIFICATION_THEME, "0");
    }

    static boolean applicationWidgetListPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static boolean applicationWidgetListHeader(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static String applicationWidgetListBackground(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static String applicationWidgetListLightnessB(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static String applicationWidgetListLightnessT(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static String applicationWidgetIconColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static String applicationWidgetIconLightness(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static String applicationWidgetListIconColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static String applicationWidgetListIconLightness(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    static boolean applicationEditorAutoCloseDrawer(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
    }

    static boolean applicationEditorSaveEditorState(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
    }

    static boolean notificationPrefIndicator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static String applicationHomeLauncher(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
    }

    static String applicationWidgetLauncher(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
    }

    static String applicationNotificationLauncher(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
    }

    static int applicationEventWifiScanInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "10"));
    }

    static boolean applicationEventWifiEnableWifi(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
    }

    static String applicationBackgroundProfile(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
    }

    static boolean applicationActivatorGridLayout(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetListGridLayout(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static int applicationEventBluetoothScanInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "10"));
    }

    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
    }

    static String applicationEventWifiRescan(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
    }

    static String applicationEventBluetoothRescan(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
    }

    static boolean applicationWidgetIconHideProfileName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static boolean applicationUnlinkRingerNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static boolean applicationShortcutEmblem(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static String applicationEventWifiScanInPowerSaveMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventBluetoothScanInPowerSaveMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationPowerSaveModeInternal(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "2");
    }

    static int applicationEventBluetoothLEScanDuration(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
    }

    static int applicationEventLocationUpdateInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "5"));
    }

    static String applicationEventLocationUpdateInPowerSaveMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
    }

    static boolean applicationEventLocationUseGPS(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
    }

    static String applicationEventLocationRescan(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
    }

    static int applicationEventOrientationScanInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "5"));
    }

    static String applicationEventOrientationScanInPowerSaveMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventMobileCellsRescan(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
    }

    static int applicationDeleteOldActivityLogs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
    }

    static String applicationWidgetIconBackground(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "0");
    }

    static String applicationWidgetIconLightnessB(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static String applicationWidgetIconLightnessT(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static boolean applicationEventUsePriority(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
    }

    static int applicationForceSetMergeRingNotificationVolumes(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(preferences.getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

}
