package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.core.content.ContextCompat;

class ApplicationPreferences {

    static SharedPreferences preferences = null;

    static final String PREF_APPLICATION_FIRST_START = "applicationFirstStart";
    static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    static final String PREF_APPLICATION_START_EVENTS = "applicationStartEvents";
    static final String PREF_APPLICATION_ALERT = "applicationAlert";
    static final String PREF_APPLICATION_CLOSE = "applicationClose";
    static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    static final String PREF_APPLICATION_THEME = "applicationTheme";
    static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    //static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    //static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    static final String PREF_NOTIFICATION_STATUS_BAR  = "notificationStatusBar";
    static final String PREF_NOTIFICATION_STATUS_BAR_STYLE  = "notificationStatusBarStyle";
    static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT  = "notificationStatusBarPermanent";
    //static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
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
    //static final String PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER = "applicationEditorAutoCloseDrawer";
    //static final String PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE = "applicationEditorSaveEditorState";
    static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    static final String PREF_APPLICATION_HOME_LAUNCHER = "applicationHomeLauncher";
    static final String PREF_APPLICATION_WIDGET_LAUNCHER = "applicationWidgetLauncher";
    static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER = "applicationNotificationLauncher";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL = "applicationEventWifiScanInterval";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE = "applicationBackgroundProfile";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND = "applicationBackgroundProfileNotificationSound";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE = "applicationBackgroundProfileNotificationVibrate";
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT= "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT= "applicationWidgetListGridLayout";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    //static final String PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO = "applicationRingerNotificationVolumesUnlinkedInfo";
    static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE = "applicationEventWifiScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE = "applicationEventBluetoothScanInPowerSaveMode";
    static final String PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION = "applicationEventBluetoothLEScanDuration";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL = "applicationEventLocationUpdateInterval";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE = "applicationEventLocationUpdateInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_LOCATION_USE_GPS = "applicationEventLocationUseGPS";
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
    //static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR = "applicationSamsungEdgePrefIndicator";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_HEADER = "applicationSamsungEdgeHeader";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND = "applicationSamsungEdgeBackground";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B = "applicationSamsungEdgeLightnessB";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T = "applicationSamsungEdgeLightnessT";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR = "applicationSamsungEdgeIconColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS = "applicationSamsungEdgeIconLightness";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT= "applicationSamsungEdgeGridLayout";
    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventLocationScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventWifiScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventBluetoothScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventMobileCellScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventOrientationScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_RESTART_EVENTS_ALERT = "applicationRestartEventsAlert";
    static final String PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS = "applicationWidgetListRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS = "applicationWidgetIconRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE = "applicationWidgetListBackgroundType";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR = "applicationWidgetListBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE = "applicationWidgetIconBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR = "applicationWidgetIconBackgroundColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE = "applicationSamsungEdgeBackgroundType";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR = "applicationSamsungEdgeBackgroundColor";
    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING = "applicationEventWifiEnableScannig";
    static final String PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE = "applicationEventWifiDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF = "applicationEventWifiScanIfWifiOff";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING = "applicationEventBluetoothEnableScannig";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE = "applicationEventBluetoothDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF = "applicationEventBluetoothScanIfBluetoothOff";
    static final String PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING = "applicationEventLocationEnableScannig";
    static final String PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventLocationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING = "applicationEventMobileCellEnableScannig";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE = "applicationEventMobileCellDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING = "applicationEventOrientationEnableScannig";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventOrientationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN = "applicationEventNeverAskForEnableRun";
    static final String PREF_APPLICATION_USE_ALARM_CLOCK = "applicationUseAlarmClock";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT = "applicationNeverAskForGrantRoot";
    static final String PREF_NOTIFICATION_SHOW_BUTTON_EXIT = "notificationShowButtonExit";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE_USAGE = "applicationBackgroundProfileUsage";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR = "applicationWidgetOneRowPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND = "applicationWidgetOneRowBackground";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B = "applicationWidgetOneRowLightnessB";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T = "applicationWidgetOneRowLightnessT";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR = "applicationWidgetOneRowIconColor";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS = "applicationWidgetOneRowIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS = "applicationWidgetOneRowRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE = "applicationWidgetOneRowBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR = "applicationWidgetOneRowBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER = "applicationWidgetListLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER = "applicationWidgetOneRowLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER = "applicationWidgetIconLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER = "applicationWidgetListShowBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER = "applicationWidgetOneRowShowBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER = "applicationWidgetIconShowBorder";
    //static final String PREF_APPLICATION_NOT_DARK_THEME = "applicationNotDarkTheme";
    static final String PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS = "applicationWidgetListCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS = "applicationWidgetOneRowCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS = "applicationWidgetIconCustomIconLightness";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS = "applicationSamsungEdgeCustomIconLightness";
    //static final String PREF_NOTIFICATION_DARK_BACKGROUND = "notificationDarkBackground";
    static final String PREF_NOTIFICATION_USE_DECORATION = "notificationUseDecoration";
    static final String PREF_NOTIFICATION_LAYOUT_TYPE = "notificationLayoutType";
    static final String PREF_NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor";
    //static final String PREF_APPLICATION_NIGHT_MODE_OFF_THEME = "applicationNightModeOffTheme";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED = "applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION = "applicationSamsungEdgeVerticalPosition";
    static final String PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR = "notificationBackgroundCustomColor";

    //static boolean forceNotUseAlarmClock = false;

    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    static boolean applicationFirstStart(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_FIRST_START, true);
    }

    static boolean applicationStartOnBoot(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static boolean applicationActivate(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static boolean applicationStartEvents(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_EVENTS, true);
    }

    static boolean applicationActivateWithAlert(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static boolean applicationClose(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static boolean applicationLongClickActivation(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    static String applicationLanguage(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
    }

    static public String applicationTheme(Context context, boolean useNightMode) {
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        String applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, defaultValue);
        if (applicationTheme.equals("light") ||
                applicationTheme.equals("material") ||
                applicationTheme.equals("color")  ||
                applicationTheme.equals("dlight")){
            applicationTheme = defaultValue;
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putString(PREF_APPLICATION_THEME, applicationTheme);
            editor.apply();
        }
        if (applicationTheme.equals("night_mode") && useNightMode) {
            int nightModeFlags =
                    context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    applicationTheme = "dark";
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    applicationTheme = "white"; //getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
                    break;
            }
        }
        return applicationTheme;
    }

    static boolean applicationActivatorPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
    }

    static public boolean applicationEditorPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }
    /*
    static boolean applicationActivatorHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
    }

    static boolean applicationEditorHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
    }
    */
    static boolean notificationsToast(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    /*
    static boolean notificationStatusBar(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
    }

    static boolean notificationStatusBarPermanent(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
    }

    static String notificationStatusBarCancel(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
    }
    */

    static String notificationStatusBarStyle(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static boolean notificationShowInStatusBar(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static String notificationTextColor(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static boolean notificationHideInLockScreen(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    /*
    static String notificationTheme(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
    }
    */

    static boolean applicationWidgetListPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static boolean applicationWidgetListHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static String applicationWidgetListBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static String applicationWidgetListLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static String applicationWidgetListLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static String applicationWidgetIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static String applicationWidgetIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static String applicationWidgetListIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static String applicationWidgetListIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationEditorAutoCloseDrawer(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
    }
    */
    /*
    static boolean applicationEditorSaveEditorState(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
    }
    */
    static boolean notificationPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static String applicationHomeLauncher(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
    }

    static String applicationWidgetLauncher(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
    }

    static String applicationNotificationLauncher(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
    }

    static int applicationEventWifiScanInterval(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
    }

    static String applicationBackgroundProfile(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
    }

    static String applicationBackgroundProfileNotificationSound(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND, "");
    }
    static boolean applicationBackgroundProfileNotificationVibrate(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE, false);
    }

    static boolean applicationBackgroundProfileUsage(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_USAGE, false);
    }

    static boolean applicationActivatorGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static boolean applicationWidgetListGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static int applicationEventBluetoothScanInterval(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
    }

    static String applicationEventWifiRescan(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
    }

    static String applicationEventBluetoothRescan(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
    }

    static boolean applicationWidgetIconHideProfileName(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static boolean applicationShortcutEmblem(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static String applicationEventWifiScanInPowerSaveMode(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventBluetoothScanInPowerSaveMode(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationPowerSaveModeInternal(Context context) {
        //if (Build.VERSION.SDK_INT >= 21)
            return getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3");
        //else
        //    return getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "0");
    }

    static int applicationEventBluetoothLEScanDuration(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
    }

    static int applicationEventLocationUpdateInterval(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
    }

    static String applicationEventLocationUpdateInPowerSaveMode(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
    }

    static boolean applicationEventLocationUseGPS(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
    }

    static String applicationEventLocationRescan(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
    }

    static int applicationEventOrientationScanInterval(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));
    }

    static String applicationEventOrientationScanInPowerSaveMode(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static String applicationEventMobileCellsRescan(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
    }

    static int applicationDeleteOldActivityLogs(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
    }

    static String applicationWidgetIconBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25");
    }

    static String applicationWidgetIconLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static String applicationWidgetIconLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static boolean applicationEventUsePriority(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
    }

    static boolean applicationUnlinkRingerNotificationVolumes(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static int applicationForceSetMergeRingNotificationVolumes(Context context) {
        return Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
    }
    */

    static boolean applicationSamsungEdgeHeader(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true);
    }

    static String applicationSamsungEdgeBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25");
    }

    static String applicationSamsungEdgeLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0");
    }

    static String applicationSamsungEdgeLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100");
    }

    static String applicationSamsungEdgeIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0");
    }

    static String applicationSamsungEdgeIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
    }
    */

    static boolean applicationEventLocationScanOnlyWhenScreenIsOn(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static boolean applicationEventWifiScanOnlyWhenScreenIsOn(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static boolean applicationEventBluetoothScanOnlyWhenScreenIsOn(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static boolean applicationEventMobileCellScanOnlyWhenScreenIsOn(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static boolean applicationEventOrientationScanOnlyWhenScreenIsOn(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, true);
    }

    static boolean applicationRestartEventsWithAlert(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_RESTART_EVENTS_ALERT, true);
    }

    static boolean applicationWidgetListRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetIconRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetListBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetListBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"); // white color
    }

    static boolean applicationWidgetIconBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetIconBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"); // white color
    }

    static boolean applicationSamsungEdgeBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false);
    }

    static String applicationSamsungEdgeBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"); // white color
    }

    /*
    static boolean applicationEventWifiEnableWifi(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
    }

    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
    }
    */

    static boolean applicationEventWifiScanIfWifiOff(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, true);
    }

    static boolean applicationEventBluetoothScanIfBluetoothOff(Context context) {
            return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, true);
    }

    static boolean applicationEventWifiEnableScanning(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
    }

    static boolean applicationEventBluetoothEnableScanning(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
    }

    static boolean applicationEventLocationEnableScanning(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
    }

    static boolean applicationEventMobileCellEnableScanning(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
    }

    static boolean applicationEventOrientationEnableScanning(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
    }

    static boolean applicationEventWifiDisabledScannigByProfile(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static boolean applicationEventBluetoothDisabledScannigByProfile(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static boolean applicationEventLocationDisabledScannigByProfile(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static boolean applicationEventMobileCellDisabledScannigByProfile(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static boolean applicationEventOrientationDisabledScannigByProfile(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static boolean applicationEventNeverAskForEnableRun(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
    }

    static boolean applicationUseAlarmClock(Context context) {
        //if (forceNotUseAlarmClock)
        //    return false;
        //else
            return getSharedPreferences(context).getBoolean(PREF_APPLICATION_USE_ALARM_CLOCK, false);
    }

    static boolean applicationNeverAskForGrantRoot(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
    }

    static boolean notificationShowButtonExit(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false);
    }

    static boolean applicationWidgetOneRowPrefIndicator(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
    }

    static String applicationWidgetOneRowBackground(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25");
    }

    static String applicationWidgetOneRowLightnessB(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0");
    }

    static String applicationWidgetOneRowLightnessT(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100");
    }

    static String applicationWidgetOneRowIconColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0");
    }

    static String applicationWidgetOneRowIconLightness(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100");
    }

    static boolean applicationWidgetOneRowRoundedCorners(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
    }

    static boolean applicationWidgetOneRowBackgroundType(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false);
    }

    static String applicationWidgetOneRowBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"); // white color
    }

    static String applicationWidgetListLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100");
    }

    static String applicationWidgetOneRowLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100");
    }

    static String applicationWidgetIconLightnessBorder(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100");
    }

    static boolean applicationWidgetListShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false);
    }

    static boolean applicationWidgetOneRowShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false);
    }

    static boolean applicationWidgetIconShowBorder(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false);
    }

    static boolean applicationWidgetListCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationWidgetOneRowCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationWidgetIconCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false);
    }

    static boolean applicationSamsungEdgeCustomIconLightness(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false);
    }

    /*
    static boolean notificationDarkBackground(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
    }
    */

    static boolean notificationUseDecoration(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, true);
    }

    static String notificationLayoutType(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, "0");
    }

    static String notificationBackgroundColor(Context context) {
        return getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
    }

    /*
    static String applicationNightModeOffTheme(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
    }
    */

    static boolean applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, true);
    }

    static String applicationSamsungEdgeVerticalPosition(Context context) {
        return getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, "0");
    }

    static int notificationBackgroundCustomColor(Context context) {
        return getSharedPreferences(context).getInt(PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, ContextCompat.getColor(context, R.color.notification_background_color_default));
    }

}
