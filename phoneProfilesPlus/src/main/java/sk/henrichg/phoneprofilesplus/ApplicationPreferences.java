package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.CheckResult;

class ApplicationPreferences {

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
    //static boolean prefEventsBlocked;
    //static boolean prefForceRunEventRunning;
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

    static boolean applicationEventNeverAskForEnableRun;
    static boolean applicationNeverAskForGrantRoot;
    static boolean applicationNeverAskForGrantG1Permission;
    static int editorOrderSelectedItem;
    static int editorSelectedView;
    static int editorProfilesViewSelectedItem;
    static int editorEventsViewSelectedItem;
    static boolean applicationStartOnBoot;
    static boolean applicationActivate;
    static boolean applicationStartEvents;
    static boolean applicationActivateWithAlert;
    static boolean applicationClose;
    static boolean applicationLongClickActivation;
    //static String  applicationLanguage;
    static String applicationTheme;
    //static boolean applicationActivatorPrefIndicator;
    static boolean applicationEditorPrefIndicator;
    //static boolean applicationActivatorHeader;
    //static boolean applicationEditorHeader;
    static boolean notificationsToast = true;
    //static boolean notificationStatusBar;
    //static boolean notificationStatusBarPermanent;
    //static String notificationStatusBarCancel;
    static String notificationStatusBarStyle;
    static boolean notificationShowInStatusBar;
    static String notificationTextColor;
    static boolean notificationHideInLockScreen;
    //static String notificationTheme;
    static boolean applicationWidgetListPrefIndicator;
    static boolean applicationWidgetListHeader;
    static String applicationWidgetListBackground;
    static String applicationWidgetListLightnessB;
    static String applicationWidgetListLightnessT;
    static String applicationWidgetIconColor;
    static String applicationWidgetIconLightness;
    static String applicationWidgetListIconColor;
    static String applicationWidgetListIconLightness;
    //static boolean applicationEditorAutoCloseDrawer;
    //static boolean applicationEditorSaveEditorState;
    static boolean notificationPrefIndicator;
    static String applicationHomeLauncher;
    static String applicationWidgetLauncher;
    static String applicationNotificationLauncher;
    static int applicationEventWifiScanInterval;
    static long applicationDefaultProfile;
    static String applicationDefaultProfileNotificationSound;
    static boolean applicationDefaultProfileNotificationVibrate;
    //static boolean applicationDefaultProfileUsage;
    static boolean applicationActivatorGridLayout;
    static boolean applicationWidgetListGridLayout;
    static int applicationEventBluetoothScanInterval;
    //static String applicationEventWifiRescan;
    //static String applicationEventBluetoothRescan;
    static boolean applicationWidgetIconHideProfileName;
    static boolean applicationShortcutEmblem;
    static String applicationEventWifiScanInPowerSaveMode;
    static String applicationEventBluetoothScanInPowerSaveMode;
    //static String applicationPowerSaveModeInternal;
    static int applicationEventBluetoothLEScanDuration;
    static int applicationEventLocationUpdateInterval;
    static String applicationEventLocationUpdateInPowerSaveMode;
    static boolean  applicationEventLocationUseGPS;
    //static String  applicationEventLocationRescan;
    static int applicationEventOrientationScanInterval;
    static String applicationEventOrientationScanInPowerSaveMode;
    static String applicationEventMobileCellsScanInPowerSaveMode;
    //static String applicationEventMobileCellsRescan;
    static int applicationDeleteOldActivityLogs;
    static String applicationWidgetIconBackground;
    static String applicationWidgetIconLightnessB;
    static String applicationWidgetIconLightnessT;
    static boolean applicationEventUsePriority;
    static boolean applicationUnlinkRingerNotificationVolumes;
    static int applicationForceSetMergeRingNotificationVolumes;
    //static boolean applicationSamsungEdgePrefIndicator;
    static boolean applicationSamsungEdgeHeader;
    static String applicationSamsungEdgeBackground;
    static String applicationSamsungEdgeLightnessB;
    static String applicationSamsungEdgeLightnessT;
    static String applicationSamsungEdgeIconColor;
    static String applicationSamsungEdgeIconLightness;
    //static boolean applicationSamsungEdgeGridLayout;
    static boolean applicationEventLocationScanOnlyWhenScreenIsOn;
    static boolean applicationEventWifiScanOnlyWhenScreenIsOn;
    static boolean applicationEventBluetoothScanOnlyWhenScreenIsOn;
    static boolean applicationEventMobileCellScanOnlyWhenScreenIsOn;
    static boolean  applicationEventOrientationScanOnlyWhenScreenIsOn;
    static boolean applicationRestartEventsWithAlert;
    static boolean applicationWidgetListRoundedCorners;
    static boolean applicationWidgetIconRoundedCorners;
    static boolean applicationWidgetListBackgroundType;
    static String applicationWidgetListBackgroundColor;
    static boolean applicationWidgetIconBackgroundType;
    static String applicationWidgetIconBackgroundColor;
    static boolean applicationSamsungEdgeBackgroundType;
    static String applicationSamsungEdgeBackgroundColor;
    //static boolean applicationEventWifiEnableWifi;
    //static boolean applicationEventBluetoothEnableBluetooth;
    static boolean applicationEventWifiScanIfWifiOff;
    static boolean applicationEventBluetoothScanIfBluetoothOff;
    static boolean applicationEventWifiEnableScanning;
    static boolean applicationEventBluetoothEnableScanning;
    static boolean applicationEventLocationEnableScanning;
    static boolean applicationEventMobileCellEnableScanning;
    static boolean applicationEventOrientationEnableScanning;
    static boolean applicationEventWifiDisabledScannigByProfile;
    static boolean applicationEventBluetoothDisabledScannigByProfile;
    static boolean applicationEventLocationDisabledScannigByProfile;
    static boolean applicationEventMobileCellDisabledScannigByProfile;
    static boolean applicationEventOrientationDisabledScannigByProfile;
    static boolean applicationEventNotificationDisabledScannigByProfile;
    static boolean applicationUseAlarmClock;
    static boolean notificationShowButtonExit;
    static boolean applicationWidgetOneRowPrefIndicator;
    static String applicationWidgetOneRowBackground;
    static String applicationWidgetOneRowLightnessB;
    static String applicationWidgetOneRowLightnessT;
    static String applicationWidgetOneRowIconColor;
    static String applicationWidgetOneRowIconLightness;
    static boolean applicationWidgetOneRowRoundedCorners;
    static boolean applicationWidgetOneRowBackgroundType;
    static String  applicationWidgetOneRowBackgroundColor;
    static String applicationWidgetListLightnessBorder;
    static String applicationWidgetOneRowLightnessBorder;
    static String applicationWidgetIconLightnessBorder;
    static boolean applicationWidgetListShowBorder;
    static boolean applicationWidgetOneRowShowBorder;
    static boolean applicationWidgetIconShowBorder;
    static boolean  applicationWidgetListCustomIconLightness;
    static boolean applicationWidgetOneRowCustomIconLightness;
    static boolean applicationWidgetIconCustomIconLightness;
    static boolean applicationSamsungEdgeCustomIconLightness;
    //static boolean notificationDarkBackground;
    static boolean notificationUseDecoration;
    static String notificationLayoutType;
    static String notificationBackgroundColor;
    //static String applicationNightModeOffTheme;
    static boolean applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    static String applicationSamsungEdgeVerticalPosition;
    static int notificationBackgroundCustomColor;
    static boolean notificationNightMode;
    static boolean applicationEditorHideHeaderOrBottomBar;
    static boolean applicationWidgetIconShowProfileDuration;
    static String notificationNotificationStyle;
    static boolean notificationShowProfileIcon;
    static boolean applicationEventBackgroundScanningEnableScanning;
    static int applicationEventBackgroundScanningScanInterval;
    static String applicationEventBackgroundScanningScanInPowerSaveMode;
    static boolean applicationEventBackgroundScanningScanOnlyWhenScreenIsOn;
    static boolean applicationEventWifiScanIgnoreHotspot;
    static boolean applicationEventNotificationEnableScanning;
    static String applicationEventNotificationScanInPowerSaveMode;
    static boolean applicationEventNotificationScanOnlyWhenScreenIsOn;

    static boolean prefActivatorActivityStartTargetHelps;
    static boolean prefActivatorFragmentStartTargetHelps;
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
    
    
    private static SharedPreferences preferences = null;

    //static final String PREF_APPLICATION_PACKAGE_REPLACED = "applicationPackageReplaced";
    static final String PREF_APPLICATION_FIRST_START = "applicationFirstStart";
    static final String PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN = "applicationEventNeverAskForEnableRun";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT = "applicationNeverAskForGrantRoot";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION = "applicationNeverAskForGrantG1Permission";

    static final String PREF_EDITOR_PROFILES_FIRST_START = "editorProfilesFirstStart";
    static final String PREF_EDITOR_EVENTS_FIRST_START = "editorEventsFirstStart";

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
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventNotificationDisabledScannigByProfile";

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
    static final String PREF_APPLICATION_DEFAULT_PROFILE = "applicationBackgroundProfile";
    static final String PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND = "applicationBackgroundProfileNotificationSound";
    static final String PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE = "applicationBackgroundProfileNotificationVibrate";
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT = "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT = "applicationWidgetListGridLayout";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    //static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    //static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    //static final String PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO = "applicationRingerNotificationVolumesUnlinkedInfo";
    static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE = "applicationEventWifiScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE = "applicationEventBluetoothScanInPowerSaveMode";
    //static final String PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION = "applicationEventBluetoothLEScanDuration";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL = "applicationEventLocationUpdateInterval";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE = "applicationEventLocationUpdateInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_LOCATION_USE_GPS = "applicationEventLocationUseGPS";
    //static final String PREF_APPLICATION_EVENT_LOCATION_RESCAN = "applicationEventLocationRescan";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL = "applicationEventOrientationScanInterval";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventOrientationScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE = "applicationEventMobileCellScanInPowerSaveMode";
    //static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN = "applicationEventMobileCellsRescan";
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
    //static final String PREF_APPLICATION_DEFAULT_PROFILE_USAGE = "applicationBackgroundProfileUsage";
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
    static final String PREF_NOTIFICATION_NIGHT_MODE = "notificationNightMode";
    static final String PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR = "applicationEditorHideHeaderOrBottomBar";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION = "applicationWidgetIconShowProfileDuration";
    static final String PREF_NOTIFICATION_NOTIFICATION_STYLE = "notificationNotificationStyle";
    static final String PREF_NOTIFICATION_SHOW_PROFILE_ICON = "notificationShowProfileIcon";
    static final String PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_ENABLE_SCANNING = "applicationEventBackgroundScanningEnableScannig";
    static final String PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_INTERVAL = "applicationEventBackgroundScanningScanInterval";
    static final String PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_IN_POWER_SAVE_MODE = "applicationEventBackgroundScanningScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventBackgroundScanningScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT = "applicationEventWifiScanIgnoreHotspot";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING = "applicationEventNotificationEnableScannig";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventNotificationScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventNotificationScanOnlyWhenScreenIsOn";

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

    /*
    static boolean applicationPackageReplaced(Context context) {
        return ApplicationPreferences.getSharedPreferences(context).getBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, false);
    }
    */
    /*
    static boolean applicationFirstStart(Context context) {
        return ApplicationPreferences.getSharedPreferences(context).getBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, true);
    }
    */

    static String applicationTheme(Context context, boolean useNightMode) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            if (applicationTheme == null)
                applicationTheme(context);
            String _applicationTheme = applicationTheme;
            if (_applicationTheme.equals("light") ||
                    _applicationTheme.equals("material") ||
                    _applicationTheme.equals("color") ||
                    _applicationTheme.equals("dlight")) {
                String defaultValue = "white";
                if (Build.VERSION.SDK_INT >= 28)
                    defaultValue = "night_mode";
                _applicationTheme = defaultValue;
                SharedPreferences.Editor editor = ApplicationPreferences.getSharedPreferences(context).edit();
                editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, _applicationTheme);
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

    static void applicationEventNeverAskForEnableRun(Context context) {
        applicationEventNeverAskForEnableRun = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
    }

    static void applicationNeverAskForGrantRoot(Context context) {
        applicationNeverAskForGrantRoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
    }

    static void applicationNeverAskForGrantG1Permission(Context context) {
        applicationNeverAskForGrantG1Permission = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
    }

    static void editorOrderSelectedItem(Context context) {
        editorOrderSelectedItem = getSharedPreferences(context).getInt(EDITOR_ORDER_SELECTED_ITEM, 0);
    }

    static void editorSelectedView(Context context) {
        editorSelectedView = getSharedPreferences(context).getInt(EDITOR_SELECTED_VIEW, 0);
    }

    static void editorProfilesViewSelectedItem(Context context) {
        editorProfilesViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
    }

    static void editorEventsViewSelectedItem(Context context) {
        editorEventsViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
    }

    static void applicationStartOnBoot(Context context) {
        applicationStartOnBoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, true);
    }

    static void applicationActivate(Context context) {
        applicationActivate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, true);
    }

    static void applicationStartEvents(Context context) {
        applicationStartEvents = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_EVENTS, true);
    }

    static void applicationActivateWithAlert(Context context) {
        applicationActivateWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, true);
    }

    static void applicationClose(Context context) {
        applicationClose = getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, true);
    }

    static void applicationLongClickActivation(Context context) {
        applicationLongClickActivation = getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, false);
    }

    /*
    static String applicationLanguage(Context context) {
         applicationLanguage = getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
         return  applicationLanguage;
    }
    */

    static void applicationTheme(Context context) {
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, defaultValue);
    }

    /*
    static boolean applicationActivatorPrefIndicator(Context context) {
        applicationActivatorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        return applicationActivatorPrefIndicator;
    }
    */

    static void applicationEditorPrefIndicator(Context context) {
        applicationEditorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
    }

    /*
    static boolean applicationActivatorHeader(Context context) {
        applicationActivatorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        return applicationActivatorHeader;
    }

    static boolean applicationEditorHeader(Context context) {
        applicationEditorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        return applicationEditorHeader;
    }
    */

    static void notificationsToast(Context context) {
        notificationsToast = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, true);
    }

    /*
    static boolean notificationStatusBar(Context context) {
        notificationStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        return notificationStatusBar;
    }

    static boolean notificationStatusBarPermanent(Context context) {
        notificationStatusBarPermanent = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        return notificationStatusBarPermanent;
    }

    static String notificationStatusBarCancel(Context context) {
        notificationStatusBarCancel = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        return notificationStatusBarCancel;
    }
    */

    static void notificationStatusBarStyle(Context context) {
        notificationStatusBarStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, "1");
    }

    static void notificationShowInStatusBar(Context context) {
        notificationShowInStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
    }

    static void notificationTextColor(Context context) {
        notificationTextColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, "0");
    }

    static void notificationHideInLockScreen(Context context) {
        notificationHideInLockScreen = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false);
    }

    /*
    static String notificationTheme(Context context) {
        notificationTheme = getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
        return notificationTheme;
    }
    */

    static void applicationWidgetListPrefIndicator(Context context) {
        applicationWidgetListPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
    }

    static void applicationWidgetListHeader(Context context) {
        applicationWidgetListHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, true);
    }

    static void applicationWidgetListBackground(Context context) {
        applicationWidgetListBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25");
    }

    static void applicationWidgetListLightnessB(Context context) {
        applicationWidgetListLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0");
    }

    static void applicationWidgetListLightnessT(Context context) {
        applicationWidgetListLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100");
    }

    static void applicationWidgetIconColor(Context context) {
        applicationWidgetIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, "0");
    }

    static void applicationWidgetIconLightness(Context context) {
        applicationWidgetIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100");
    }

    static void applicationWidgetListIconColor(Context context) {
        applicationWidgetListIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0");
    }

    static void applicationWidgetListIconLightness(Context context) {
        applicationWidgetListIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationEditorAutoCloseDrawer(Context context) {
        applicationEditorAutoCloseDrawer = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
        return applicationEditorAutoCloseDrawer;
    }
    */
    /*
    static boolean applicationEditorSaveEditorState(Context context) {
        applicationEditorSaveEditorState = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
        return applicationEditorSaveEditorState;
    }
    */

    static void notificationPrefIndicator(Context context) {
        notificationPrefIndicator = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, true);
    }

    static void applicationHomeLauncher(Context context) {
        applicationHomeLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_HOME_LAUNCHER, "activator");
    }

    static void applicationWidgetLauncher(Context context) {
        applicationWidgetLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LAUNCHER, "activator");
    }

    static void applicationNotificationLauncher(Context context) {
        applicationNotificationLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator");
    }

    static void applicationEventWifiScanInterval(Context context) {
        applicationEventWifiScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
    }

    static void applicationDefaultProfile(Context context) {
        applicationDefaultProfile = Long.parseLong(getSharedPreferences(context).getString(PREF_APPLICATION_DEFAULT_PROFILE, "-999"));
    }

    static void applicationDefaultProfileNotificationSound(Context context) {
        applicationDefaultProfileNotificationSound = getSharedPreferences(context).getString(PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, "");
    }

    static void applicationDefaultProfileNotificationVibrate(Context context) {
        applicationDefaultProfileNotificationVibrate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, false);
    }

    /*
    static void applicationDefaultProfileUsage(Context context) {
        applicationDefaultProfileUsage = getSharedPreferences(context).getBoolean(PREF_APPLICATION_DEFAULT_PROFILE_USAGE, false);
    }
    */

    static void applicationActivatorGridLayout(Context context) {
        applicationActivatorGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true);
    }

    static void applicationWidgetListGridLayout(Context context) {
        applicationWidgetListGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true);
    }

    static void applicationEventBluetoothScanInterval(Context context) {
        applicationEventBluetoothScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
    }

    /*static void applicationEventWifiRescan(Context context) {
        applicationEventWifiRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
    }*/

    /*static void applicationEventBluetoothRescan(Context context) {
        applicationEventBluetoothRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
    }*/

    static void applicationWidgetIconHideProfileName(Context context) {
        applicationWidgetIconHideProfileName = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
    }

    static void applicationShortcutEmblem(Context context) {
        applicationShortcutEmblem = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, true);
    }

    static void applicationEventWifiScanInPowerSaveMode(Context context) {
        applicationEventWifiScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventBluetoothScanInPowerSaveMode(Context context) {
        applicationEventBluetoothScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    /*
    static void applicationPowerSaveModeInternal(Context context) {
        applicationPowerSaveModeInternal = getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3");
    }
    */

    static void applicationEventBluetoothLEScanDuration(Context context) {
        applicationEventBluetoothLEScanDuration = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
    }

    static void applicationEventLocationUpdateInterval(Context context) {
        applicationEventLocationUpdateInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
    }

    static void applicationEventLocationUpdateInPowerSaveMode(Context context) {
        applicationEventLocationUpdateInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventLocationUseGPS(Context context) {
        applicationEventLocationUseGPS = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false);
    }

    /*static void applicationEventLocationRescan(Context context) {
        applicationEventLocationRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
    }*/

    static void applicationEventOrientationScanInterval(Context context) {
        applicationEventOrientationScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));
    }

    static void applicationEventOrientationScanInPowerSaveMode(Context context) {
        applicationEventOrientationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        applicationEventMobileCellsScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    /*static void applicationEventMobileCellsRescan(Context context) {
        applicationEventMobileCellsRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
    }*/

    static void applicationDeleteOldActivityLogs(Context context) {
        applicationDeleteOldActivityLogs = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
    }

    static void applicationWidgetIconBackground(Context context) {
        applicationWidgetIconBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25");
    }

    static void applicationWidgetIconLightnessB(Context context) {
        applicationWidgetIconLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0");
    }

    static void applicationWidgetIconLightnessT(Context context) {
        applicationWidgetIconLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100");
    }

    static void applicationEventUsePriority(Context context) {
        applicationEventUsePriority = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, false);
    }

    static void applicationUnlinkRingerNotificationVolumes(Context context) {
        applicationUnlinkRingerNotificationVolumes = getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false);
    }

    static void applicationForceSetMergeRingNotificationVolumes(Context context) {
        applicationForceSetMergeRingNotificationVolumes = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        applicationSamsungEdgePrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
        return applicationSamsungEdgePrefIndicator;
    }
    */

    static void applicationSamsungEdgeHeader(Context context) {
        applicationSamsungEdgeHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true);
    }

    static void applicationSamsungEdgeBackground(Context context) {
        applicationSamsungEdgeBackground = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25");
    }

    static void applicationSamsungEdgeLightnessB(Context context) {
        applicationSamsungEdgeLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0");
    }

    static void applicationSamsungEdgeLightnessT(Context context) {
        applicationSamsungEdgeLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100");
    }

    static void applicationSamsungEdgeIconColor(Context context) {
        applicationSamsungEdgeIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0");
        //return applicationSamsungEdgeIconColor;
    }

    static void applicationSamsungEdgeIconLightness(Context context) {
        applicationSamsungEdgeIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100");
    }

    /*
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        applicationSamsungEdgeGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
        return applicationSamsungEdgeGridLayout;
    }
    */

    static void applicationEventLocationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventLocationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventWifiScanOnlyWhenScreenIsOn(Context context) {
        applicationEventWifiScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventBluetoothScanOnlyWhenScreenIsOn(Context context) {
        applicationEventBluetoothScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventMobileCellScanOnlyWhenScreenIsOn(Context context) {
        applicationEventMobileCellScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventOrientationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventOrientationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, true);
    }

    static void applicationRestartEventsWithAlert(Context context) {
        applicationRestartEventsWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_RESTART_EVENTS_ALERT, true);
    }

    static void applicationWidgetListRoundedCorners(Context context) {
        applicationWidgetListRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetIconRoundedCorners(Context context) {
        applicationWidgetIconRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetListBackgroundType(Context context) {
        applicationWidgetListBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetListBackgroundColor(Context context) {
        applicationWidgetListBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationWidgetIconBackgroundType(Context context) {
        applicationWidgetIconBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetIconBackgroundColor(Context context) {
        applicationWidgetIconBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationSamsungEdgeBackgroundType(Context context) {
        applicationSamsungEdgeBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false);
    }

    static void applicationSamsungEdgeBackgroundColor(Context context) {
        applicationSamsungEdgeBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"); // white color
    }

    /*
    static boolean applicationEventWifiEnableWifi(Context context) {
        applicationEventWifiEnableWifi = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
        return applicationEventWifiEnableWifi;
    }

    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        applicationEventBluetoothEnableBluetooth = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
        return applicationEventBluetoothEnableBluetooth;
    }
    */

    static void applicationEventWifiScanIfWifiOff(Context context) {
        applicationEventWifiScanIfWifiOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, true);
    }

    static void applicationEventBluetoothScanIfBluetoothOff(Context context) {
        applicationEventBluetoothScanIfBluetoothOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, true);
    }

    static void applicationEventWifiEnableScanning(Context context) {
        applicationEventWifiEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
    }

    static void applicationEventBluetoothEnableScanning(Context context) {
        applicationEventBluetoothEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
    }

    static void applicationEventLocationEnableScanning(Context context) {
        applicationEventLocationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
    }

    static void applicationEventMobileCellEnableScanning(Context context) {
        applicationEventMobileCellEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
    }

    static void applicationEventOrientationEnableScanning(Context context) {
        applicationEventOrientationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
    }

    static void applicationEventWifiDisabledScannigByProfile(Context context) {
        applicationEventWifiDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventBluetoothDisabledScannigByProfile(Context context) {
        applicationEventBluetoothDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventLocationDisabledScannigByProfile(Context context) {
        applicationEventLocationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventMobileCellDisabledScannigByProfile(Context context) {
        applicationEventMobileCellDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventOrientationDisabledScannigByProfile(Context context) {
        applicationEventOrientationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationEventNotificationDisabledScannigByProfile(Context context) {
        applicationEventNotificationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, false);
    }

    static void applicationUseAlarmClock(Context context) {
        applicationUseAlarmClock = getSharedPreferences(context).getBoolean(PREF_APPLICATION_USE_ALARM_CLOCK, false);
    }

    static void notificationShowButtonExit(Context context) {
        notificationShowButtonExit = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false);
    }

    static void applicationWidgetOneRowPrefIndicator(Context context) {
        applicationWidgetOneRowPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
    }

    static void applicationWidgetOneRowBackground(Context context) {
        applicationWidgetOneRowBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25");
    }

    static void applicationWidgetOneRowLightnessB(Context context) {
        applicationWidgetOneRowLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0");
    }

    static void applicationWidgetOneRowLightnessT(Context context) {
        applicationWidgetOneRowLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100");
    }

    static void applicationWidgetOneRowIconColor(Context context) {
        applicationWidgetOneRowIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0");
    }

    static void applicationWidgetOneRowIconLightness(Context context) {
        applicationWidgetOneRowIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100");
    }

    static void applicationWidgetOneRowRoundedCorners(Context context) {
        applicationWidgetOneRowRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
    }

    static void applicationWidgetOneRowBackgroundType(Context context) {
        applicationWidgetOneRowBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false);
    }

    static void applicationWidgetOneRowBackgroundColor(Context context) {
        applicationWidgetOneRowBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"); // white color
    }

    static void applicationWidgetListLightnessBorder(Context context) {
        applicationWidgetListLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetOneRowLightnessBorder(Context context) {
        applicationWidgetOneRowLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetIconLightnessBorder(Context context) {
        applicationWidgetIconLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100");
    }

    static void applicationWidgetListShowBorder(Context context) {
        applicationWidgetListShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false);
    }

    static void applicationWidgetOneRowShowBorder(Context context) {
        applicationWidgetOneRowShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false);
    }

    static void applicationWidgetIconShowBorder(Context context) {
        applicationWidgetIconShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false);
    }

    static void applicationWidgetListCustomIconLightness(Context context) {
        applicationWidgetListCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationWidgetOneRowCustomIconLightness(Context context) {
        applicationWidgetOneRowCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationWidgetIconCustomIconLightness(Context context) {
        applicationWidgetIconCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false);
    }

    static void applicationSamsungEdgeCustomIconLightness(Context context) {
        applicationSamsungEdgeCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false);
    }

    /*
    static boolean notificationDarkBackground(Context context) {
        notificationDarkBackground = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
        return notificationDarkBackground;
    }
    */

    static void notificationUseDecoration(Context context) {
        notificationUseDecoration = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, true);
    }

    static void notificationLayoutType(Context context) {
        notificationLayoutType = getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, "0");
    }

    static void notificationBackgroundColor(Context context) {
        notificationBackgroundColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
    }

    /*
    static String applicationNightModeOffTheme(Context context) {
        applicationNightModeOffTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        return applicationNightModeOffTheme;
    }
    */

    static void applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(Context context) {
        applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, true);
    }

    static void applicationSamsungEdgeVerticalPosition(Context context) {
        applicationSamsungEdgeVerticalPosition = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, "0");
    }

    static void notificationBackgroundCustomColor(Context context) {
        notificationBackgroundCustomColor = getSharedPreferences(context).getInt(PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
    }

    static void notificationNightMode(Context context) {
        notificationNightMode = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_NIGHT_MODE, false);
    }

    static void applicationEditorHideHeaderOrBottomBar(Context context) {
        applicationEditorHideHeaderOrBottomBar = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, true);
    }

    static void applicationWidgetIconShowProfileDuration(Context context) {
        applicationWidgetIconShowProfileDuration = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, true);
    }

    static void notificationNotificationStyle(Context context) {
        notificationNotificationStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_NOTIFICATION_STYLE, "0");
    }

    static void notificationShowProfileIcon(Context context) {
        notificationShowProfileIcon = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_PROFILE_ICON, true);
    }

    static void applicationEventBackgroundScanningEnableScanning(Context context) {
        applicationEventBackgroundScanningEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_ENABLE_SCANNING, false);
    }

    static void applicationEventBackgroundScanningScanInterval(Context context) {
        applicationEventBackgroundScanningScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_INTERVAL, "15"));
    }

    static void applicationEventBackgroundScanningScanInPowerSaveMode(Context context) {
        applicationEventBackgroundScanningScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventBackgroundScanningScanOnlyWhenScreenIsOn(Context context) {
        applicationEventBackgroundScanningScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BACKGROUND_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void applicationEventWifiScanIgnoreHotspot(Context context) {
        applicationEventWifiScanIgnoreHotspot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT, false);
    }

    static void applicationEventNotificationEnableScanning(Context context) {
        applicationEventNotificationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, false);
    }

    static void applicationEventNotificationScanInPowerSaveMode(Context context) {
        applicationEventNotificationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE, "1");
    }

    static void applicationEventNotificationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventNotificationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false);
    }

    static void loadStartTargetHelps(Context context) {
        SharedPreferences _preferences = getSharedPreferences(context);
        prefActivatorActivityStartTargetHelps = _preferences.getBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, false);
        prefActivatorFragmentStartTargetHelps = _preferences.getBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, false);
        prefActivatorAdapterStartTargetHelps = _preferences.getBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, false);
        prefEditorActivityStartTargetHelps = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, false);
        prefEditorActivityStartTargetHelpsDefaultProfile = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, false);
        prefEditorActivityStartTargetHelpsFilterSpinner = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, false);
        prefEditorActivityStartTargetHelpsRunStopIndicator = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, false);
        prefEditorActivityStartTargetHelpsBottomNavigation = _preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, false);
        prefEditorProfilesFragmentStartTargetHelps = _preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
        prefEditorProfilesAdapterStartTargetHelps = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
        prefEditorProfilesAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
        prefEditorProfilesAdapterStartTargetHelpsShowInActivator = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
        prefEditorEventsFragmentStartTargetHelps = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
        prefEditorEventsFragmentStartTargetHelpsOrderSpinner = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, false);
        prefEditorEventsAdapterStartTargetHelps = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
        prefEditorEventsAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
        prefEditorEventsAdapterStartTargetHelpsStatus = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);
        prefProfilePrefsActivityStartTargetHelps = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, false);
        prefProfilePrefsActivityStartTargetHelpsSave = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, false);
        prefEventPrefsActivityStartTargetHelps = _preferences.getBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, false);
    }

    static void startStopTargetHelps(Context context, boolean start) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorActivityStartTargetHelps = start;
        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = start;
        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = start;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelps = start;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile = start;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsFilterSpinner = start;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = start;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = start;
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = start;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = start;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = start;
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps = start;
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, start);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelpsSave = start;
        editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = start;
        editor.apply();
    }
    
}
