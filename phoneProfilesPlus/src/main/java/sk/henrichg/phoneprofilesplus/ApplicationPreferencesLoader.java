package sk.henrichg.phoneprofilesplus;

import android.annotation.CheckResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

class ApplicationPreferencesLoader {

    private static SharedPreferences preferences = null;

    static final String PREF_APPLICATION_FIRST_START = "applicationFirstStart";
    static final String PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN = "applicationEventNeverAskForEnableRun";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT = "applicationNeverAskForGrantRoot";

    static final String EDITOR_ORDER_SELECTED_ITEM = "editor_order_selected_item";
    static final String EDITOR_SELECTED_VIEW = "editor_selected_view";
    static final String EDITOR_PROFILES_VIEW_SELECTED_ITEM = "editor_profiles_view_selected_item";
    static final String EDITOR_EVENTS_VIEW_SELECTED_ITEM = "editor_events_view_selected_item";

    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    static final String PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE = "applicationEventWifiDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE = "applicationEventBluetoothDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventLocationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE = "applicationEventMobileCellDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventOrientationDisabledScannigByProfile";

    static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    static final String PREF_APPLICATION_START_EVENTS = "applicationStartEvents";
    static final String PREF_APPLICATION_ALERT = "applicationAlert";
    static final String PREF_APPLICATION_CLOSE = "applicationClose";
    static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    //static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    static final String PREF_APPLICATION_THEME = "applicationTheme";
    //static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    //static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    //static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    static final String PREF_NOTIFICATION_STATUS_BAR = "notificationStatusBar";
    static final String PREF_NOTIFICATION_STATUS_BAR_STYLE = "notificationStatusBarStyle";
    static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT = "notificationStatusBarPermanent";
    //static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR = "notificationShowInStatusBar";
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
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT = "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT = "applicationWidgetListGridLayout";
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
    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING = "applicationEventWifiEnableScannig";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF = "applicationEventWifiScanIfWifiOff";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING = "applicationEventBluetoothEnableScannig";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF = "applicationEventBluetoothScanIfBluetoothOff";
    static final String PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING = "applicationEventLocationEnableScannig";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING = "applicationEventMobileCellEnableScannig";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING = "applicationEventOrientationEnableScannig";
    static final String PREF_APPLICATION_USE_ALARM_CLOCK = "applicationUseAlarmClock";
    static final String PREF_NOTIFICATION_SHOW_BUTTON_EXIT = "notificationShowButtonExit";
    static final String PREF_APPLICATION_BACKGROUND_PROFILE_USAGE = "applicationBackgroundProfileUsage";
    //static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR = "applicationWidgetOneRowPrefIndicator";
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
    static final String PREF_NOTIFICATION_NIGHT_MODE = "notificationNightMode";
    static final String PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR = "applicationEditorHideHeaderOrBottomBar";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION = "applicationWidgetIconShowProfileDuration";

    @CheckResult
    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    @CheckResult
    static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    static void applicationEventNeverAskForEnableRun(Context context) {
        ApplicationPreferences.applicationEventNeverAskForEnableRun = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
    }

    static void applicationNeverAskForGrantRoot(Context context) {
        ApplicationPreferences.applicationNeverAskForGrantRoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
    }

    static void editorOrderSelectedItem(Context context) {
        ApplicationPreferences.editorOrderSelectedItem = getSharedPreferences(context).getInt(EDITOR_ORDER_SELECTED_ITEM, 0);
    }

    static void editorSelectedView(Context context) {
        ApplicationPreferences.editorSelectedView = getSharedPreferences(context).getInt(EDITOR_SELECTED_VIEW, 0);
    }

    static void editorProfilesViewSelectedItem(Context context) {
        ApplicationPreferences.editorProfilesViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
    }

    static void editorEventsViewSelectedItem(Context context) {
        ApplicationPreferences.editorEventsViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
    }

    static void applicationStartOnBoot(Context context) {
        ApplicationPreferences.applicationStartOnBoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static void applicationActivate(Context context) {
        ApplicationPreferences.applicationActivate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static void applicationStartEvents(Context context) {
        ApplicationPreferences.applicationStartEvents = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_EVENTS, true);
    }

    static void applicationActivateWithAlert(Context context) {
        ApplicationPreferences.applicationActivateWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static void applicationClose(Context context) {
        ApplicationPreferences.applicationClose = getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static void applicationLongClickActivation(Context context) {
        ApplicationPreferences.applicationLongClickActivation = getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    /*
    static String applicationLanguage(Context context) {
         ApplicationPreferences.applicationLanguage = getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
         return  applicationLanguage;
    }
    */

    static void applicationTheme(Context context){
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        ApplicationPreferences.applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, defaultValue);
    }

    /*
    static boolean applicationActivatorPrefIndicator(Context context) {
        ApplicationPreferences.applicationActivatorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        return applicationActivatorPrefIndicator;
    }
    */

    static void applicationEditorPrefIndicator(Context context) {
        ApplicationPreferences.applicationEditorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }

    /*
    static boolean applicationActivatorHeader(Context context) {
        ApplicationPreferences.applicationActivatorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        return applicationActivatorHeader;
    }

    static boolean applicationEditorHeader(Context context) {
        ApplicationPreferences.applicationEditorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        return applicationEditorHeader;
    }
    */

    static void notificationsToast(Context context) {
        ApplicationPreferences.notificationsToast = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    /*
    static boolean notificationStatusBar(Context context) {
        ApplicationPreferences.notificationStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        return notificationStatusBar;
    }

    static boolean notificationStatusBarPermanent(Context context) {
        ApplicationPreferences.notificationStatusBarPermanent = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        return notificationStatusBarPermanent;
    }

    static String notificationStatusBarCancel(Context context) {
        ApplicationPreferences.notificationStatusBarCancel = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        return notificationStatusBarCancel;
    }
    */

    static void notificationStatusBarStyle(Context context) {
        ApplicationPreferences.notificationStatusBarStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static void notificationShowInStatusBar(Context context) {
        ApplicationPreferences.notificationShowInStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static void notificationTextColor(Context context) {
        ApplicationPreferences.notificationTextColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static void notificationHideInLockScreen(Context context) {
        ApplicationPreferences.notificationHideInLockScreen = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    /*
    static String notificationTheme(Context context) {
        ApplicationPreferences.notificationTheme = getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
        return notificationTheme;
    }
    */

    static void applicationWidgetListPrefIndicator(Context context) {
        ApplicationPreferences.applicationWidgetListPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static void applicationWidgetListHeader(Context context) {
        ApplicationPreferences.applicationWidgetListHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static void applicationWidgetListBackground(Context context) {
        ApplicationPreferences.applicationWidgetListBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static void applicationWidgetListLightnessB(Context context) {
        ApplicationPreferences.applicationWidgetListLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static void applicationWidgetListLightnessT(Context context) {
        ApplicationPreferences.applicationWidgetListLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static void applicationWidgetIconColor(Context context) {
        ApplicationPreferences.applicationWidgetIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static void applicationWidgetIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static void applicationWidgetListIconColor(Context context) {
        ApplicationPreferences.applicationWidgetListIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static void applicationWidgetListIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetListIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationEditorAutoCloseDrawer(Context context) {
        ApplicationPreferences.applicationEditorAutoCloseDrawer = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
        return applicationEditorAutoCloseDrawer;
    }
    */
    /*
    static boolean applicationEditorSaveEditorState(Context context) {
        ApplicationPreferences.applicationEditorSaveEditorState = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
        return applicationEditorSaveEditorState;
    }
    */

    static void notificationPrefIndicator(Context context) {
        ApplicationPreferences.notificationPrefIndicator = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static void applicationHomeLauncher(Context context) {
        ApplicationPreferences.applicationHomeLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
    }

    static void applicationWidgetLauncher(Context context) {
        ApplicationPreferences.applicationWidgetLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
    }

    static void applicationNotificationLauncher(Context context) {
        ApplicationPreferences.applicationNotificationLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
    }

    static void applicationEventWifiScanInterval(Context context) {
        ApplicationPreferences.applicationEventWifiScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
    }

    static void applicationBackgroundProfile(Context context) {
        ApplicationPreferences.applicationBackgroundProfile = getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
    }

    static void applicationBackgroundProfileNotificationSound(Context context) {
        ApplicationPreferences.applicationBackgroundProfileNotificationSound = getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND, "");
    }

    static void applicationBackgroundProfileNotificationVibrate(Context context) {
        ApplicationPreferences.applicationBackgroundProfileNotificationVibrate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE, false);
    }

    static void applicationBackgroundProfileUsage(Context context) {
        ApplicationPreferences.applicationBackgroundProfileUsage = getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_USAGE, false);
    }

    static void applicationActivatorGridLayout(Context context) {
        ApplicationPreferences.applicationActivatorGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static void applicationWidgetListGridLayout(Context context) {
        ApplicationPreferences.applicationWidgetListGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static void applicationEventBluetoothScanInterval(Context context) {
        ApplicationPreferences.applicationEventBluetoothScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
    }

    static void applicationEventWifiRescan(Context context) {
        ApplicationPreferences.applicationEventWifiRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
    }

    static void applicationEventBluetoothRescan(Context context) {
        ApplicationPreferences.applicationEventBluetoothRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
    }

    static void applicationWidgetIconHideProfileName(Context context) {
        ApplicationPreferences.applicationWidgetIconHideProfileName = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static void applicationShortcutEmblem(Context context) {
        ApplicationPreferences.applicationShortcutEmblem = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static void applicationEventWifiScanInPowerSaveMode(Context context) {
        ApplicationPreferences.applicationEventWifiScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventBluetoothScanInPowerSaveMode(Context context) {
        ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationPowerSaveModeInternal(Context context) {
        ApplicationPreferences.applicationPowerSaveModeInternal = getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3");
    }

    static void applicationEventBluetoothLEScanDuration(Context context) {
        ApplicationPreferences.applicationEventBluetoothLEScanDuration = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
    }

    static void applicationEventLocationUpdateInterval(Context context) {
        ApplicationPreferences.applicationEventLocationUpdateInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
    }

    static void applicationEventLocationUpdateInPowerSaveMode(Context context) {
        ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventLocationUseGPS(Context context) {
        ApplicationPreferences.applicationEventLocationUseGPS = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
    }

    static void applicationEventLocationRescan(Context context) {
        ApplicationPreferences.applicationEventLocationRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
    }

    static void applicationEventOrientationScanInterval(Context context) {
        ApplicationPreferences.applicationEventOrientationScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));
    }

    static void applicationEventOrientationScanInPowerSaveMode(Context context) {
        ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventMobileCellsRescan(Context context) {
        ApplicationPreferences.applicationEventMobileCellsRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
    }

    static void applicationDeleteOldActivityLogs(Context context) {
        ApplicationPreferences.applicationDeleteOldActivityLogs = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
    }

    static void applicationWidgetIconBackground(Context context) {
        ApplicationPreferences.applicationWidgetIconBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25");
    }

    static void applicationWidgetIconLightnessB(Context context) {
        ApplicationPreferences.applicationWidgetIconLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static void applicationWidgetIconLightnessT(Context context) {
        ApplicationPreferences.applicationWidgetIconLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static void applicationEventUsePriority(Context context) {
        ApplicationPreferences.applicationEventUsePriority = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
    }

    static void applicationUnlinkRingerNotificationVolumes(Context context) {
        ApplicationPreferences.applicationUnlinkRingerNotificationVolumes = getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static void applicationForceSetMergeRingNotificationVolumes(Context context) {
        ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        ApplicationPreferences.applicationSamsungEdgePrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
        return applicationSamsungEdgePrefIndicator;
    }
    */

    static void applicationSamsungEdgeHeader(Context context) {
        ApplicationPreferences.applicationSamsungEdgeHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true);
    }

    static void applicationSamsungEdgeBackground(Context context) {
        ApplicationPreferences.applicationSamsungEdgeBackground = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25");
    }

    static void applicationSamsungEdgeLightnessB(Context context) {
        ApplicationPreferences.applicationSamsungEdgeLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0");
    }

    static void applicationSamsungEdgeLightnessT(Context context) {
        ApplicationPreferences.applicationSamsungEdgeLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100");
    }

    static void applicationSamsungEdgeIconColor(Context context) {
        ApplicationPreferences.applicationSamsungEdgeIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0");
        //return applicationSamsungEdgeIconColor;
    }

    static void applicationSamsungEdgeIconLightness(Context context) {
        ApplicationPreferences.applicationSamsungEdgeIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        ApplicationPreferences.applicationSamsungEdgeGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
        return applicationSamsungEdgeGridLayout;
    }
    */

    static void applicationEventLocationScanOnlyWhenScreenIsOn(Context context) {
        ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventWifiScanOnlyWhenScreenIsOn(Context context) {
        ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventBluetoothScanOnlyWhenScreenIsOn(Context context) {
        ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventMobileCellScanOnlyWhenScreenIsOn(Context context) {
        ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventOrientationScanOnlyWhenScreenIsOn(Context context) {
        ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, true);
    }

    static void applicationRestartEventsWithAlert(Context context) {
        ApplicationPreferences.applicationRestartEventsWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_RESTART_EVENTS_ALERT, true);
    }

    static void applicationWidgetListRoundedCorners(Context context) {
        ApplicationPreferences.applicationWidgetListRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetIconRoundedCorners(Context context) {
        ApplicationPreferences.applicationWidgetIconRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetListBackgroundType(Context context) {
        ApplicationPreferences.applicationWidgetListBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetListBackgroundColor(Context context) {
        ApplicationPreferences.applicationWidgetListBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationWidgetIconBackgroundType(Context context) {
        ApplicationPreferences.applicationWidgetIconBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetIconBackgroundColor(Context context) {
        ApplicationPreferences.applicationWidgetIconBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationSamsungEdgeBackgroundType(Context context) {
        ApplicationPreferences.applicationSamsungEdgeBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false);
    }

    static void applicationSamsungEdgeBackgroundColor(Context context) {
        ApplicationPreferences.applicationSamsungEdgeBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"); // white color
    }

    /*
    static boolean applicationEventWifiEnableWifi(Context context) {
        ApplicationPreferences.applicationEventWifiEnableWifi = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
        return applicationEventWifiEnableWifi;
    }

    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        ApplicationPreferences.applicationEventBluetoothEnableBluetooth = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
        return applicationEventBluetoothEnableBluetooth;
    }
    */

    static void applicationEventWifiScanIfWifiOff(Context context) {
        ApplicationPreferences.applicationEventWifiScanIfWifiOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, true);
    }

    static void applicationEventBluetoothScanIfBluetoothOff(Context context) {
        ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, true);
    }

    static void applicationEventWifiEnableScanning(Context context) {
        ApplicationPreferences.applicationEventWifiEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
    }

    static void applicationEventBluetoothEnableScanning(Context context) {
        ApplicationPreferences.applicationEventBluetoothEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
    }

    static void applicationEventLocationEnableScanning(Context context) {
        ApplicationPreferences.applicationEventLocationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
    }

    static void applicationEventMobileCellEnableScanning(Context context) {
        ApplicationPreferences.applicationEventMobileCellEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
    }

    static void applicationEventOrientationEnableScanning(Context context) {
        ApplicationPreferences.applicationEventOrientationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
    }

    static void applicationEventWifiDisabledScannigByProfile(Context context) {
        ApplicationPreferences.applicationEventWifiDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventBluetoothDisabledScannigByProfile(Context context) {
        ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventLocationDisabledScannigByProfile(Context context) {
        ApplicationPreferences.applicationEventLocationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventMobileCellDisabledScannigByProfile(Context context) {
        ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventOrientationDisabledScannigByProfile(Context context) {
        ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationUseAlarmClock(Context context) {
        ApplicationPreferences.applicationUseAlarmClock = getSharedPreferences(context).getBoolean(PREF_APPLICATION_USE_ALARM_CLOCK, false);
    }

    static void notificationShowButtonExit(Context context) {
        ApplicationPreferences.notificationShowButtonExit = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false);
    }

    /*
    static boolean applicationWidgetOneRowPrefIndicator(Context context) {
        ApplicationPreferences.applicationWidgetOneRowPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
        return applicationWidgetOneRowPrefIndicator;
    }
    */

    static void applicationWidgetOneRowBackground(Context context) {
        ApplicationPreferences.applicationWidgetOneRowBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25");
    }

    static void applicationWidgetOneRowLightnessB(Context context) {
        ApplicationPreferences.applicationWidgetOneRowLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0");
    }

    static void applicationWidgetOneRowLightnessT(Context context) {
        ApplicationPreferences.applicationWidgetOneRowLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100");
    }

    static void applicationWidgetOneRowIconColor(Context context) {
        ApplicationPreferences.applicationWidgetOneRowIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0");
    }

    static void applicationWidgetOneRowIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetOneRowIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100");
    }

    static void applicationWidgetOneRowRoundedCorners(Context context) {
        ApplicationPreferences.applicationWidgetOneRowRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetOneRowBackgroundType(Context context) {
        ApplicationPreferences.applicationWidgetOneRowBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetOneRowBackgroundColor(Context context) {
        ApplicationPreferences.applicationWidgetOneRowBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationWidgetListLightnessBorder(Context context) {
        ApplicationPreferences.applicationWidgetListLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetOneRowLightnessBorder(Context context) {
        ApplicationPreferences.applicationWidgetOneRowLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetIconLightnessBorder(Context context) {
        ApplicationPreferences.applicationWidgetIconLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetListShowBorder(Context context) {
        ApplicationPreferences.applicationWidgetListShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false);
    }

    static void applicationWidgetOneRowShowBorder(Context context) {
        ApplicationPreferences.applicationWidgetOneRowShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false);
    }

    static void applicationWidgetIconShowBorder(Context context) {
        ApplicationPreferences.applicationWidgetIconShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false);
    }

    static void applicationWidgetListCustomIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetListCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationWidgetOneRowCustomIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetOneRowCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationWidgetIconCustomIconLightness(Context context) {
        ApplicationPreferences.applicationWidgetIconCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationSamsungEdgeCustomIconLightness(Context context) {
        ApplicationPreferences.applicationSamsungEdgeCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false);
    }

    /*
    static boolean notificationDarkBackground(Context context) {
        ApplicationPreferences.notificationDarkBackground = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
        return notificationDarkBackground;
    }
    */

    static void notificationUseDecoration(Context context) {
        ApplicationPreferences.notificationUseDecoration = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, true);
    }

    static void notificationLayoutType(Context context) {
        ApplicationPreferences.notificationLayoutType = getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, "0");
    }

    static void notificationBackgroundColor(Context context) {
        ApplicationPreferences.notificationBackgroundColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
    }

    /*
    static String applicationNightModeOffTheme(Context context) {
        ApplicationPreferences.applicationNightModeOffTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        return applicationNightModeOffTheme;
    }
    */

    static void applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(Context context) {
        ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, true);
    }

    static void applicationSamsungEdgeVerticalPosition(Context context) {
        ApplicationPreferences.applicationSamsungEdgeVerticalPosition = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, "0");
    }

    static void notificationBackgroundCustomColor(Context context) {
        ApplicationPreferences.notificationBackgroundCustomColor = getSharedPreferences(context).getInt(PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
    }

    static void notificationNightMode(Context context) {
        ApplicationPreferences.notificationNightMode = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_NIGHT_MODE, false);
    }

    static void applicationEditorHideHeaderOrBottomBar(Context context) {
        ApplicationPreferences.applicationEditorHideHeaderOrBottomBar = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, true);
    }

    static void applicationWidgetIconShowProfileDuration(Context context) {
        ApplicationPreferences.applicationWidgetIconShowProfileDuration = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, true);
    }

    static void loadStartTargetHelps(Context context) {
        SharedPreferences _preferences = getSharedPreferences(context);
        ApplicationPreferences.prefActivatorActivityStartTargetHelps = _preferences.getBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = _preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = _preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelps = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsFilterSpinner = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, true);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = _preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, true);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, true);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelpsSave = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, true);
        ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = _preferences.getBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, true);
    }

}
