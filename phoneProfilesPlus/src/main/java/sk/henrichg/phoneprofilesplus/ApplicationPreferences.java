package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

class ApplicationPreferences {

    static SharedPreferences preferences = null;

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

    static int prefRingerVolume;
    static int prefNotificationVolume;
    static int prefRingerMode;
    static int prefZenMode;
    static boolean prefLockScreenDisabled;
    static int prefActivatedProfileScreenTimeout;
    static boolean prefMergedRingNotificationVolumes;
    static long prefActivatedProfileForDuration;
    static long prefActivatedProfileEndDurationTime;
    static boolean prefShowIgnoreBatteryOptimizationNotificationOnStart;
    static boolean prefEventsBlocked;
    static boolean prefForceRunEventRunning;
    static String prefApplicationInForeground;
    static int prefEventCallEventType;
    static long prefEventCallEventTime;
    static String prefEventCallPhoneNumber;
    static boolean prefWiredHeadsetConnected;
    static boolean prefWiredHeadsetMicrophone;
    static boolean prefBluetoothHeadsetConnected;
    static boolean prefBluetoothHeadsetMicrophone;
    static int prefForceOneWifiScan;
    static int prefForceOneBluetoothScan;
    static int prefForceOneBluetoothLEScan;
    static boolean prefEventBluetoothScanRequest;
    static boolean prefEventBluetoothLEScanRequest;
    static boolean prefEventBluetoothWaitForResult;
    static boolean prefEventBluetoothLEWaitForResult;
    static boolean prefEventBluetoothScanKilled;
    static boolean prefEventBluetoothEnabledForScan;
    static boolean prefEventWifiScanRequest;
    static boolean prefEventWifiWaitForResult;
    static boolean prefEventWifiEnabledForScan;

    static boolean prefActivatorActivityStartTargetHelps;
    static boolean prefActivatorFragmentStartTragetHelps;
    static boolean prefActivatorAdapterStartTargetHelps;
    static boolean prefEditorActivityStartTargetHelps;
    static boolean prefEditorActivityStartTargetHelpsDefaultProfile;
    static boolean prefEditorActivityStartTargetHelpsFilterSpinner;
    static boolean prefEditorActivityStartTargetHelpsRunStopIndicator;
    static boolean prefEditorActivityStartTargetHelpsBottomNavigation;
    static boolean prefEditorProfilesFragmentStartTargetHelps;
    static boolean prefEditorProfilesAdapterStartTargetHelps;
    static boolean prefEditorProfilesAdapterStartTargetHelpsOrder;
    static boolean prefEditorProfilesAdapterStartTargetHelpsShowInActivator;
    static boolean prefEditorEventsFragmentStartTargetHelps;
    static boolean prefEditorEventsFragmentStartTargetHelpsOrderSpinner;
    static boolean prefEditorEventsAdapterStartTargetHelps;
    static boolean prefEditorEventsAdapterStartTargetHelpsOrder;
    static boolean prefEditorEventsAdapterStartTargetHelpsStatus;
    static boolean prefProfilePrefsActivityStartTargetHelps;
    static boolean prefProfilePrefsActivityStartTargetHelpsSave;
    static boolean prefEventPrefsActivityStartTargetHelps;

    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }


    static boolean applicationFirstStart(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_APPLICATION_FIRST_START, true);
    }

    static String applicationTheme(Context context, boolean useNightMode) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            String _applicationTheme = applicationTheme;
            if (_applicationTheme.equals("light") ||
                    _applicationTheme.equals("material") ||
                    _applicationTheme.equals("color") ||
                    _applicationTheme.equals("dlight")) {
                String defaultValue = "white";
                if (Build.VERSION.SDK_INT >= 28)
                    defaultValue = "night_mode";
                _applicationTheme = defaultValue;
                SharedPreferences.Editor editor = getSharedPreferences(context).edit();
                editor.putString(PREF_APPLICATION_THEME, _applicationTheme);
                editor.apply();
                applicationTheme = _applicationTheme;
            }
            if (_applicationTheme.equals("night_mode") && useNightMode) {
                int nightModeFlags =
                        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        _applicationTheme = "dark";
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        _applicationTheme = "white";
                        break;
                }
            }
            return _applicationTheme;
        }
    }


    static boolean applicationEventNeverAskForEnableRun;

    static void applicationEventNeverAskForEnableRun(Context context) {
        applicationEventNeverAskForEnableRun = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
        //return applicationEventNeverAskForEnableRun;
    }

    static boolean applicationNeverAskForGrantRoot;

    static void applicationNeverAskForGrantRoot(Context context) {
        applicationNeverAskForGrantRoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
        //return applicationNeverAskForGrantRoot;
    }

    static int editorOrderSelectedItem;
    static void editorOrderSelectedItem(Context context) {
        editorOrderSelectedItem = getSharedPreferences(context).getInt(EDITOR_ORDER_SELECTED_ITEM, 0);
        //return editorOrderSelectedItem;
    }

    static int editorSelectedView;
    static void editorSelectedView(Context context) {
        editorSelectedView = getSharedPreferences(context).getInt(EDITOR_SELECTED_VIEW, 0);
        //return editorSelectedView;
    }

    static int editorProfilesViewSelectedItem;
    static void editorProfilesViewSelectedItem(Context context) {
        editorProfilesViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
        //return editorProfilesViewSelectedItem;
    }

    static int editorEventsViewSelectedItem;
    static void editorEventsViewSelectedItem(Context context) {
        editorEventsViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
        //return editorEventsViewSelectedItem;
    }

    static boolean applicationStartOnBoot;
    static void applicationStartOnBoot(Context context) {
        applicationStartOnBoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
        //return applicationStartOnBoot;
    }

    static boolean applicationActivate;
    static void applicationActivate(Context context) {
        applicationActivate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, true);
        //return applicationActivate;
    }

    static boolean applicationStartEvents;
    static void applicationStartEvents(Context context) {
        applicationStartEvents = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_EVENTS, true);
        //return applicationStartEvents;
    }

    static boolean applicationActivateWithAlert;
    static void applicationActivateWithAlert(Context context) {
        applicationActivateWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, true);
        //return applicationActivateWithAlert;
    }

    static boolean applicationClose;
    static void applicationClose(Context context) {
        applicationClose = getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, true);
        //return applicationClose;
    }

    static boolean applicationLongClickActivation;
    static void applicationLongClickActivation(Context context) {
        applicationLongClickActivation = getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
        //return applicationLongClickActivation;
    }

    /*
    static String  applicationLanguage;
    static String applicationLanguage(Context context) {
         applicationLanguage = getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
         return  applicationLanguage;
    }
    */

    static String applicationTheme;
    static void applicationTheme(Context context){
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, defaultValue);
        //return applicationTheme;
    }

    /*
    static boolean applicationActivatorPrefIndicator;
    static boolean applicationActivatorPrefIndicator(Context context) {
        applicationActivatorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        return applicationActivatorPrefIndicator;
    }
    */

    static boolean applicationEditorPrefIndicator;
    static void applicationEditorPrefIndicator(Context context) {
        applicationEditorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        //return applicationEditorPrefIndicator;
    }

    /*
    static boolean applicationActivatorHeader;
    static boolean applicationActivatorHeader(Context context) {
        applicationActivatorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        return applicationActivatorHeader;
    }

    static boolean applicationEditorHeader;
    static boolean applicationEditorHeader(Context context) {
        applicationEditorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        return applicationEditorHeader;
    }
    */

    static boolean notificationsToast;
    static void notificationsToast(Context context) {
        notificationsToast = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, true);
        //return notificationsToast;
    }

    /*
    static boolean notificationStatusBar;
    static boolean notificationStatusBar(Context context) {
        notificationStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        return notificationStatusBar;
    }

    static boolean notificationStatusBarPermanent;
    static boolean notificationStatusBarPermanent(Context context) {
        notificationStatusBarPermanent = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        return notificationStatusBarPermanent;
    }

    static String notificationStatusBarCancel;
    static String notificationStatusBarCancel(Context context) {
        notificationStatusBarCancel = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        return notificationStatusBarCancel;
    }
    */

    static String notificationStatusBarStyle;
    static void notificationStatusBarStyle(Context context) {
        notificationStatusBarStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
        //return notificationStatusBarStyle;
    }

    static boolean notificationShowInStatusBar;
    static void notificationShowInStatusBar(Context context) {
        notificationShowInStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
        //return notificationShowInStatusBar;
    }

    static String notificationTextColor;
    static void notificationTextColor(Context context) {
        notificationTextColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
        //return notificationTextColor;
    }

    static boolean notificationHideInLockScreen;
    static void notificationHideInLockScreen(Context context) {
        notificationHideInLockScreen = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
        //return notificationHideInLockScreen;
    }

    /*
    static String notificationTheme;
    static String notificationTheme(Context context) {
        notificationTheme = getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
        return notificationTheme;
    }
    */

    static boolean applicationWidgetListPrefIndicator;
    static void applicationWidgetListPrefIndicator(Context context) {
        applicationWidgetListPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
        //return applicationWidgetListPrefIndicator;
    }

    static boolean applicationWidgetListHeader;
    static void applicationWidgetListHeader(Context context) {
        applicationWidgetListHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
        //return applicationWidgetListHeader;
    }

    static String applicationWidgetListBackground;
    static void applicationWidgetListBackground(Context context) {
        applicationWidgetListBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
        //return applicationWidgetListBackground;
    }

    static String applicationWidgetListLightnessB;
    static void applicationWidgetListLightnessB(Context context) {
        applicationWidgetListLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
        //return applicationWidgetListLightnessB;
    }

    static String applicationWidgetListLightnessT;
    static void applicationWidgetListLightnessT(Context context) {
        applicationWidgetListLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
        //return applicationWidgetListLightnessT;
    }

    static String applicationWidgetIconColor;
    static void applicationWidgetIconColor(Context context) {
        applicationWidgetIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
        //return applicationWidgetIconColor;
    }

    static String applicationWidgetIconLightness;
    static void applicationWidgetIconLightness(Context context) {
        applicationWidgetIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
        //return applicationWidgetIconLightness;
    }

    static String applicationWidgetListIconColor;
    static void applicationWidgetListIconColor(Context context) {
        applicationWidgetListIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
        //return applicationWidgetListIconColor;
    }

    static String applicationWidgetListIconLightness;
    static void applicationWidgetListIconLightness(Context context) {
        applicationWidgetListIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
        //return applicationWidgetListIconLightness;
    }

    /*
    static boolean applicationEditorAutoCloseDrawer;
    static boolean applicationEditorAutoCloseDrawer(Context context) {
        applicationEditorAutoCloseDrawer = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
        return applicationEditorAutoCloseDrawer;
    }
    */
    /*
    static boolean applicationEditorSaveEditorState;
    static boolean applicationEditorSaveEditorState(Context context) {
        applicationEditorSaveEditorState = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
        return applicationEditorSaveEditorState;
    }
    */

    static boolean notificationPrefIndicator;
    static void notificationPrefIndicator(Context context) {
        notificationPrefIndicator = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
        //return notificationPrefIndicator;
    }

    static String applicationHomeLauncher;
    static void applicationHomeLauncher(Context context) {
        applicationHomeLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
        //return applicationHomeLauncher;
    }

    static String applicationWidgetLauncher;
    static void applicationWidgetLauncher(Context context) {
        applicationWidgetLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
        //return applicationWidgetLauncher;
    }

    static String applicationNotificationLauncher;
    static void applicationNotificationLauncher(Context context) {
        applicationNotificationLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
        //return applicationNotificationLauncher;
    }

    static int applicationEventWifiScanInterval;
    static void applicationEventWifiScanInterval(Context context) {
        applicationEventWifiScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
        //return applicationEventWifiScanInterval;
    }

    static String applicationBackgroundProfile;
    static void applicationBackgroundProfile(Context context) {
        applicationBackgroundProfile = getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE, "-999");
        //return applicationBackgroundProfile;
    }

    static String applicationBackgroundProfileNotificationSound;
    static void applicationBackgroundProfileNotificationSound(Context context) {
        applicationBackgroundProfileNotificationSound = getSharedPreferences(context).getString(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND, "");
        //return applicationBackgroundProfileNotificationSound;
    }

    static boolean applicationBackgroundProfileNotificationVibrate;
    static void applicationBackgroundProfileNotificationVibrate(Context context) {
        applicationBackgroundProfileNotificationVibrate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE, false);
        //return applicationBackgroundProfileNotificationVibrate;
    }

    static boolean applicationBackgroundProfileUsage;
    static void applicationBackgroundProfileUsage(Context context) {
        applicationBackgroundProfileUsage = getSharedPreferences(context).getBoolean(PREF_APPLICATION_BACKGROUND_PROFILE_USAGE, false);
        //return applicationBackgroundProfileUsage;
    }

    static boolean applicationActivatorGridLayout;
    static void applicationActivatorGridLayout(Context context) {
        applicationActivatorGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
        //return  applicationActivatorGridLayout;
    }

    static boolean applicationWidgetListGridLayout;
    static void applicationWidgetListGridLayout(Context context) {
        applicationWidgetListGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
        //return  applicationWidgetListGridLayout;
    }

    static int applicationEventBluetoothScanInterval;
    static void applicationEventBluetoothScanInterval(Context context) {
        applicationEventBluetoothScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
        //return applicationEventBluetoothScanInterval;
    }

    static String applicationEventWifiRescan;
    static void applicationEventWifiRescan(Context context) {
        applicationEventWifiRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
        //return applicationEventWifiRescan;
    }

    static String applicationEventBluetoothRescan;
    static void applicationEventBluetoothRescan(Context context) {
        applicationEventBluetoothRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
        //return  applicationEventBluetoothRescan;
    }

    static boolean applicationWidgetIconHideProfileName;
    static void applicationWidgetIconHideProfileName(Context context) {
        applicationWidgetIconHideProfileName = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
        //return  applicationWidgetIconHideProfileName;
    }

    static boolean applicationShortcutEmblem;
    static void applicationShortcutEmblem(Context context) {
        applicationShortcutEmblem = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
        //return  applicationShortcutEmblem;
    }

    static String applicationEventWifiScanInPowerSaveMode;
    static void applicationEventWifiScanInPowerSaveMode(Context context) {
        applicationEventWifiScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
        //return applicationEventWifiScanInPowerSaveMode;
    }

    static String applicationEventBluetoothScanInPowerSaveMode;
    static void applicationEventBluetoothScanInPowerSaveMode(Context context) {
        applicationEventBluetoothScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
        //return  applicationEventBluetoothScanInPowerSaveMode;
    }

    static String applicationPowerSaveModeInternal;
    static void applicationPowerSaveModeInternal(Context context) {
        applicationPowerSaveModeInternal = getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3");
        //return applicationPowerSaveModeInternal;
    }

    static int applicationEventBluetoothLEScanDuration;
    static void applicationEventBluetoothLEScanDuration(Context context) {
        applicationEventBluetoothLEScanDuration = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
        //return applicationEventBluetoothLEScanDuration;
    }

    static int applicationEventLocationUpdateInterval;
    static void applicationEventLocationUpdateInterval(Context context) {
        applicationEventLocationUpdateInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
        //return applicationEventLocationUpdateInterval;
    }

    static String applicationEventLocationUpdateInPowerSaveMode;
    static void applicationEventLocationUpdateInPowerSaveMode(Context context) {
        applicationEventLocationUpdateInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
        //return  applicationEventLocationUpdateInPowerSaveMode;
    }

    static boolean  applicationEventLocationUseGPS;
    static void applicationEventLocationUseGPS(Context context) {
        applicationEventLocationUseGPS = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
        //return  applicationEventLocationUseGPS;
    }

    static String  applicationEventLocationRescan;
    static void applicationEventLocationRescan(Context context) {
        applicationEventLocationRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
        //return  applicationEventLocationRescan;
    }

    static int applicationEventOrientationScanInterval;
    static void applicationEventOrientationScanInterval(Context context) {
        applicationEventOrientationScanInterval = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));
        //return applicationEventOrientationScanInterval;
    }

    static String applicationEventOrientationScanInPowerSaveMode;
    static void applicationEventOrientationScanInPowerSaveMode(Context context) {
        applicationEventOrientationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
        //return applicationEventOrientationScanInPowerSaveMode;
    }

    static String applicationEventMobileCellsScanInPowerSaveMode;
    static void applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        applicationEventMobileCellsScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
        //return applicationEventMobileCellsScanInPowerSaveMode;
    }

    static String applicationEventMobileCellsRescan;
    static void applicationEventMobileCellsRescan(Context context) {
        applicationEventMobileCellsRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
        //return applicationEventMobileCellsRescan;
    }

    static int applicationDeleteOldActivityLogs;
    static void applicationDeleteOldActivityLogs(Context context) {
        applicationDeleteOldActivityLogs = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
        //return applicationDeleteOldActivityLogs;
    }

    static String applicationWidgetIconBackground;
    static void applicationWidgetIconBackground(Context context) {
        applicationWidgetIconBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25");
        //return applicationWidgetIconBackground;
    }

    static String applicationWidgetIconLightnessB;
    static void applicationWidgetIconLightnessB(Context context) {
        applicationWidgetIconLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
        //return applicationWidgetIconLightnessB;
    }

    static String applicationWidgetIconLightnessT;
    static void applicationWidgetIconLightnessT(Context context) {
        applicationWidgetIconLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
        //return applicationWidgetIconLightnessT;
    }

    static boolean applicationEventUsePriority;
    static void applicationEventUsePriority(Context context) {
        applicationEventUsePriority = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
        //return applicationEventUsePriority;
    }

    static boolean applicationUnlinkRingerNotificationVolumes;
    static void applicationUnlinkRingerNotificationVolumes(Context context) {
        applicationUnlinkRingerNotificationVolumes = getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
        //return applicationUnlinkRingerNotificationVolumes;
    }

    static int applicationForceSetMergeRingNotificationVolumes;
    static void applicationForceSetMergeRingNotificationVolumes(Context context) {
        applicationForceSetMergeRingNotificationVolumes = Integer.valueOf(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
        //return applicationForceSetMergeRingNotificationVolumes;
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator;
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        applicationSamsungEdgePrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
        return applicationSamsungEdgePrefIndicator;
    }
    */

    static boolean applicationSamsungEdgeHeader;
    static void applicationSamsungEdgeHeader(Context context) {
        applicationSamsungEdgeHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true);
        //return applicationSamsungEdgeHeader;
    }

    static String applicationSamsungEdgeBackground;
    static void applicationSamsungEdgeBackground(Context context) {
        applicationSamsungEdgeBackground = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25");
        //return applicationSamsungEdgeBackground;
    }

    static String applicationSamsungEdgeLightnessB;
    static void applicationSamsungEdgeLightnessB(Context context) {
        applicationSamsungEdgeLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0");
        //return applicationSamsungEdgeLightnessB;
    }

    static String applicationSamsungEdgeLightnessT;
    static void applicationSamsungEdgeLightnessT(Context context) {
        applicationSamsungEdgeLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100");
        //return applicationSamsungEdgeLightnessT;
    }

    static String applicationSamsungEdgeIconColor;
    static void applicationSamsungEdgeIconColor(Context context) {
        applicationSamsungEdgeIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0");
        //return applicationSamsungEdgeIconColor;
    }

    static String applicationSamsungEdgeIconLightness;
    static void applicationSamsungEdgeIconLightness(Context context) {
        applicationSamsungEdgeIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100");
        //return applicationSamsungEdgeIconLightness;
    }

    /*
    static boolean applicationSamsungEdgeGridLayout;
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        applicationSamsungEdgeGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
        return applicationSamsungEdgeGridLayout;
    }
    */

    static boolean applicationEventLocationScanOnlyWhenScreenIsOn;
    static void applicationEventLocationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventLocationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
       //return applicationEventLocationScanOnlyWhenScreenIsOn;
    }

    static boolean applicationEventWifiScanOnlyWhenScreenIsOn;
    static void applicationEventWifiScanOnlyWhenScreenIsOn(Context context) {
        applicationEventWifiScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
        //return applicationEventWifiScanOnlyWhenScreenIsOn;
    }

    static boolean applicationEventBluetoothScanOnlyWhenScreenIsOn;
    static void applicationEventBluetoothScanOnlyWhenScreenIsOn(Context context) {
        applicationEventBluetoothScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
        //return applicationEventBluetoothScanOnlyWhenScreenIsOn;
    }

    static boolean applicationEventMobileCellScanOnlyWhenScreenIsOn;
    static void applicationEventMobileCellScanOnlyWhenScreenIsOn(Context context) {
        applicationEventMobileCellScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
        //return applicationEventMobileCellScanOnlyWhenScreenIsOn;
    }

    static boolean  applicationEventOrientationScanOnlyWhenScreenIsOn;
    static void applicationEventOrientationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventOrientationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, true);
        //return  applicationEventOrientationScanOnlyWhenScreenIsOn;
    }

    static boolean applicationRestartEventsWithAlert;
    static void applicationRestartEventsWithAlert(Context context) {
        applicationRestartEventsWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_RESTART_EVENTS_ALERT, true);
        //return applicationRestartEventsWithAlert;
    }

    static boolean applicationWidgetListRoundedCorners;
    static void applicationWidgetListRoundedCorners(Context context) {
        applicationWidgetListRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
        //return applicationWidgetListRoundedCorners;
    }

    static boolean applicationWidgetIconRoundedCorners;
    static void applicationWidgetIconRoundedCorners(Context context) {
        applicationWidgetIconRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
        //return applicationWidgetIconRoundedCorners;
    }

    static boolean applicationWidgetListBackgroundType;
    static void applicationWidgetListBackgroundType(Context context) {
        applicationWidgetListBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false);
        //return applicationWidgetListBackgroundType;
    }

    static String applicationWidgetListBackgroundColor;
    static void applicationWidgetListBackgroundColor(Context context) {
        applicationWidgetListBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"); // white color
        //return applicationWidgetListBackgroundColor;
    }

    static boolean applicationWidgetIconBackgroundType;
    static void applicationWidgetIconBackgroundType(Context context) {
        applicationWidgetIconBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false);
        //return applicationWidgetIconBackgroundType;
    }

    static String applicationWidgetIconBackgroundColor;
    static void applicationWidgetIconBackgroundColor(Context context) {
        applicationWidgetIconBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"); // white color
        //return applicationWidgetIconBackgroundColor;
    }

    static boolean applicationSamsungEdgeBackgroundType;
    static void applicationSamsungEdgeBackgroundType(Context context) {
        applicationSamsungEdgeBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false);
        //return applicationSamsungEdgeBackgroundType;
    }

    static String applicationSamsungEdgeBackgroundColor;
    static void applicationSamsungEdgeBackgroundColor(Context context) {
        applicationSamsungEdgeBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"); // white color
        //return applicationSamsungEdgeBackgroundColor;
    }

    /*
    static boolean applicationEventWifiEnableWifi;
    static boolean applicationEventWifiEnableWifi(Context context) {
        applicationEventWifiEnableWifi = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
        return applicationEventWifiEnableWifi;
    }

    static boolean applicationEventBluetoothEnableBluetooth;
    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        applicationEventBluetoothEnableBluetooth = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
        return applicationEventBluetoothEnableBluetooth;
    }
    */

    static boolean applicationEventWifiScanIfWifiOff;
    static void applicationEventWifiScanIfWifiOff(Context context) {
        applicationEventWifiScanIfWifiOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, true);
        //return applicationEventWifiScanIfWifiOff;
    }

    static boolean applicationEventBluetoothScanIfBluetoothOff;
    static void applicationEventBluetoothScanIfBluetoothOff(Context context) {
        applicationEventBluetoothScanIfBluetoothOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, true);
        //return applicationEventBluetoothScanIfBluetoothOff;
    }

    static boolean applicationEventWifiEnableScanning;
    static void applicationEventWifiEnableScanning(Context context) {
        applicationEventWifiEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
        //return applicationEventWifiEnableScanning;
    }

    static boolean applicationEventBluetoothEnableScanning;
    static void applicationEventBluetoothEnableScanning(Context context) {
        applicationEventBluetoothEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
        //return applicationEventBluetoothEnableScanning;
    }

    static boolean applicationEventLocationEnableScanning;
    static void applicationEventLocationEnableScanning(Context context) {
        applicationEventLocationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
        //return applicationEventLocationEnableScanning;
    }

    static boolean applicationEventMobileCellEnableScanning;
    static void applicationEventMobileCellEnableScanning(Context context) {
        applicationEventMobileCellEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
        //return applicationEventMobileCellEnableScanning;
    }

    static boolean applicationEventOrientationEnableScanning;
    static void applicationEventOrientationEnableScanning(Context context) {
        applicationEventOrientationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
        //return applicationEventOrientationEnableScanning;
    }

    static boolean applicationEventWifiDisabledScannigByProfile;
    static void applicationEventWifiDisabledScannigByProfile(Context context) {
        applicationEventWifiDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
        //return applicationEventWifiDisabledScannigByProfile;
    }

    static boolean applicationEventBluetoothDisabledScannigByProfile;
    static void applicationEventBluetoothDisabledScannigByProfile(Context context) {
        applicationEventBluetoothDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
        //return applicationEventBluetoothDisabledScannigByProfile;
    }

    static boolean applicationEventLocationDisabledScannigByProfile;
    static void applicationEventLocationDisabledScannigByProfile(Context context) {
        applicationEventLocationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
        //return applicationEventLocationDisabledScannigByProfile;
    }

    static boolean applicationEventMobileCellDisabledScannigByProfile;
    static void applicationEventMobileCellDisabledScannigByProfile(Context context) {
        applicationEventMobileCellDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
        //return applicationEventMobileCellDisabledScannigByProfile;
    }

    static boolean applicationEventOrientationDisabledScannigByProfile;
    static void applicationEventOrientationDisabledScannigByProfile(Context context) {
        applicationEventOrientationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
        //return applicationEventOrientationDisabledScannigByProfile;
    }

    static boolean applicationUseAlarmClock;
    static void applicationUseAlarmClock(Context context) {
        applicationUseAlarmClock = getSharedPreferences(context).getBoolean(PREF_APPLICATION_USE_ALARM_CLOCK, false);
        //return applicationUseAlarmClock;
    }

    static boolean notificationShowButtonExit;
    static void notificationShowButtonExit(Context context) {
        notificationShowButtonExit = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false);
        //return notificationShowButtonExit;
    }

    /*
    static boolean applicationWidgetOneRowPrefIndicator;
    static boolean applicationWidgetOneRowPrefIndicator(Context context) {
        applicationWidgetOneRowPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
        return applicationWidgetOneRowPrefIndicator;
    }
    */

    static String applicationWidgetOneRowBackground;
    static void applicationWidgetOneRowBackground(Context context) {
        applicationWidgetOneRowBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25");
        //return applicationWidgetOneRowBackground;
    }

    static String applicationWidgetOneRowLightnessB;
    static void applicationWidgetOneRowLightnessB(Context context) {
        applicationWidgetOneRowLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0");
        //return applicationWidgetOneRowLightnessB;
    }

    static String applicationWidgetOneRowLightnessT;
    static void applicationWidgetOneRowLightnessT(Context context) {
        applicationWidgetOneRowLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100");
        //return applicationWidgetOneRowLightnessT;
    }

    static String applicationWidgetOneRowIconColor;
    static void applicationWidgetOneRowIconColor(Context context) {
        applicationWidgetOneRowIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0");
        //return applicationWidgetOneRowIconColor;
    }

    static String applicationWidgetOneRowIconLightness;
    static void applicationWidgetOneRowIconLightness(Context context) {
        applicationWidgetOneRowIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100");
        //return applicationWidgetOneRowIconLightness;
    }

    static boolean applicationWidgetOneRowRoundedCorners;
    static void applicationWidgetOneRowRoundedCorners(Context context) {
        applicationWidgetOneRowRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
        //return applicationWidgetOneRowRoundedCorners;
    }

    static boolean applicationWidgetOneRowBackgroundType;
    static void applicationWidgetOneRowBackgroundType(Context context) {
        applicationWidgetOneRowBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false);
        //return applicationWidgetOneRowBackgroundType;
    }

    static String  applicationWidgetOneRowBackgroundColor;
    static void applicationWidgetOneRowBackgroundColor(Context context) {
        applicationWidgetOneRowBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"); // white color
        //return  applicationWidgetOneRowBackgroundColor;
    }

    static String applicationWidgetListLightnessBorder;
    static void applicationWidgetListLightnessBorder(Context context) {
        applicationWidgetListLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100");
        //return applicationWidgetListLightnessBorder;
    }

    static String applicationWidgetOneRowLightnessBorder;
    static void applicationWidgetOneRowLightnessBorder(Context context) {
        applicationWidgetOneRowLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100");
        //return applicationWidgetOneRowLightnessBorder;
    }

    static String applicationWidgetIconLightnessBorder;
    static void applicationWidgetIconLightnessBorder(Context context) {
        applicationWidgetIconLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100");
        //return applicationWidgetIconLightnessBorder;
    }

    static boolean applicationWidgetListShowBorder;
    static void applicationWidgetListShowBorder(Context context) {
        applicationWidgetListShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false);
        //return applicationWidgetListShowBorder;
    }

    static boolean applicationWidgetOneRowShowBorder;
    static void applicationWidgetOneRowShowBorder(Context context) {
        applicationWidgetOneRowShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false);
        //return applicationWidgetOneRowShowBorder;
    }

    static boolean applicationWidgetIconShowBorder;
    static void applicationWidgetIconShowBorder(Context context) {
        applicationWidgetIconShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false);
        //return applicationWidgetIconShowBorder;
    }

    static boolean  applicationWidgetListCustomIconLightness;
    static void applicationWidgetListCustomIconLightness(Context context) {
        applicationWidgetListCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false);
        //return  applicationWidgetListCustomIconLightness;
    }

    static boolean applicationWidgetOneRowCustomIconLightness;
    static void applicationWidgetOneRowCustomIconLightness(Context context) {
        applicationWidgetOneRowCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false);
        //return applicationWidgetOneRowCustomIconLightness;
    }

    static boolean applicationWidgetIconCustomIconLightness;
    static void applicationWidgetIconCustomIconLightness(Context context) {
        applicationWidgetIconCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false);
        //return applicationWidgetIconCustomIconLightness;
    }

    static boolean applicationSamsungEdgeCustomIconLightness;
    static void applicationSamsungEdgeCustomIconLightness(Context context) {
        applicationSamsungEdgeCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false);
        //return applicationSamsungEdgeCustomIconLightness;
    }

    /*
    static boolean notificationDarkBackground;
    static boolean notificationDarkBackground(Context context) {
        notificationDarkBackground = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
        return notificationDarkBackground;
    }
    */

    static boolean notificationUseDecoration;
    static void notificationUseDecoration(Context context) {
        notificationUseDecoration = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, true);
        //return notificationUseDecoration;
    }

    static String notificationLayoutType;
    static void notificationLayoutType(Context context) {
        notificationLayoutType = getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, "0");
        //return notificationLayoutType;
    }

    static String notificationBackgroundColor;
    static void notificationBackgroundColor(Context context) {
        notificationBackgroundColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
        //return notificationBackgroundColor;
    }

    /*
    static String applicationNightModeOffTheme;
    static String applicationNightModeOffTheme(Context context) {
        applicationNightModeOffTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        return applicationNightModeOffTheme;
    }
    */

    static boolean applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    static void applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(Context context) {
        applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, true);
        //return applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    }

    static String applicationSamsungEdgeVerticalPosition;
    static void applicationSamsungEdgeVerticalPosition(Context context) {
        applicationSamsungEdgeVerticalPosition = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, "0");
        //return applicationSamsungEdgeVerticalPosition;
    }

    static int notificationBackgroundCustomColor;
    static void notificationBackgroundCustomColor(Context context) {
        notificationBackgroundCustomColor = getSharedPreferences(context).getInt(PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
        //return notificationBackgroundCustomColor;
    }

    static boolean notificationNightMode;
    static void notificationNightMode(Context context) {
        notificationNightMode = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_NIGHT_MODE, false);
        //return notificationNightMode;
    }

    static boolean applicationEditorHideHeaderOrBottomBar;
    static void applicationEditorHideHeaderOrBottomBar(Context context) {
        applicationEditorHideHeaderOrBottomBar = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, true);
        //return applicationEditorHideHeaderOrBottomBar;
    }

    static boolean applicationWidgetIconShowProfileDuration;
    static void applicationWidgetIconShowProfileDuration(Context context) {
        applicationWidgetIconShowProfileDuration = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, true);
        //return applicationWidgetIconShowProfileDuration;
    }


    static void loadStartTargetHelps(Context context) {
        getSharedPreferences(context);
        prefActivatorActivityStartTargetHelps = preferences.getBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, true);
        prefActivatorFragmentStartTragetHelps = preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true);;
        prefActivatorAdapterStartTargetHelps = preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true);;
        prefEditorActivityStartTargetHelps = preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, true);;
        prefEditorActivityStartTargetHelpsDefaultProfile = preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, true);;
        prefEditorActivityStartTargetHelpsFilterSpinner = preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);;
        prefEditorActivityStartTargetHelpsRunStopIndicator = preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, true);;
        prefEditorActivityStartTargetHelpsBottomNavigation = preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, true);;
        prefEditorProfilesFragmentStartTargetHelps = preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true);;
        prefEditorProfilesAdapterStartTargetHelps = preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true);;
        prefEditorProfilesAdapterStartTargetHelpsOrder = preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true);;
        prefEditorProfilesAdapterStartTargetHelpsShowInActivator = preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, true);;
        prefEditorEventsFragmentStartTargetHelps = preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, true);;
        prefEditorEventsFragmentStartTargetHelpsOrderSpinner = preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, true);;
        prefEditorEventsAdapterStartTargetHelps = preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true);;
        prefEditorEventsAdapterStartTargetHelpsOrder = preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true);;
        prefEditorEventsAdapterStartTargetHelpsStatus = preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, true);;
        prefProfilePrefsActivityStartTargetHelps = preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);;
        prefProfilePrefsActivityStartTargetHelpsSave = preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, true);;
        prefEventPrefsActivityStartTargetHelps = preferences.getBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, true);;
    }

}
