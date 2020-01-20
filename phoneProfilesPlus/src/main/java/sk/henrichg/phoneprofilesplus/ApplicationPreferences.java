package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

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

    static boolean applicationFirstStart(Context context) {
        return ApplicationPreferencesLoader.getSharedPreferences(context).getBoolean(ApplicationPreferencesLoader.PREF_APPLICATION_FIRST_START, true);
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
                SharedPreferences.Editor editor = ApplicationPreferencesLoader.getSharedPreferences(context).edit();
                editor.putString(ApplicationPreferencesLoader.PREF_APPLICATION_THEME, _applicationTheme);
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
    static boolean applicationNeverAskForGrantRoot;
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
    /*
    static String  applicationLanguage;
    */
    static String applicationTheme;
    /*
    static boolean applicationActivatorPrefIndicator;
    */
    static boolean applicationEditorPrefIndicator;
    /*
    static boolean applicationActivatorHeader;
    static boolean applicationEditorHeader;
    */
    static boolean notificationsToast;
    /*
    static boolean notificationStatusBar;
    static boolean notificationStatusBarPermanent;
    static String notificationStatusBarCancel;
    */
    static String notificationStatusBarStyle;
    static boolean notificationShowInStatusBar;
    static String notificationTextColor;
    static boolean notificationHideInLockScreen;
    /*
    static String notificationTheme;
    */
    static boolean applicationWidgetListPrefIndicator;
    static boolean applicationWidgetListHeader;
    static String applicationWidgetListBackground;
    static String applicationWidgetListLightnessB;
    static String applicationWidgetListLightnessT;
    static String applicationWidgetIconColor;
    static String applicationWidgetIconLightness;
    static String applicationWidgetListIconColor;
    static String applicationWidgetListIconLightness;
    /*
    static boolean applicationEditorAutoCloseDrawer;
    */
    /*
    static boolean applicationEditorSaveEditorState;
    */
    static boolean notificationPrefIndicator;
    static String applicationHomeLauncher;
    static String applicationWidgetLauncher;
    static String applicationNotificationLauncher;
    static int applicationEventWifiScanInterval;
    static String applicationBackgroundProfile;
    static String applicationBackgroundProfileNotificationSound;
    static boolean applicationBackgroundProfileNotificationVibrate;
    static boolean applicationBackgroundProfileUsage;
    static boolean applicationActivatorGridLayout;
    static boolean applicationWidgetListGridLayout;
    static int applicationEventBluetoothScanInterval;
    static String applicationEventWifiRescan;
    static String applicationEventBluetoothRescan;
    static boolean applicationWidgetIconHideProfileName;
    static boolean applicationShortcutEmblem;
    static String applicationEventWifiScanInPowerSaveMode;
    static String applicationEventBluetoothScanInPowerSaveMode;
    static String applicationPowerSaveModeInternal;
    static int applicationEventBluetoothLEScanDuration;
    static int applicationEventLocationUpdateInterval;
    static String applicationEventLocationUpdateInPowerSaveMode;
    static boolean  applicationEventLocationUseGPS;
    static String  applicationEventLocationRescan;
    static int applicationEventOrientationScanInterval;
    static String applicationEventOrientationScanInPowerSaveMode;
    static String applicationEventMobileCellsScanInPowerSaveMode;
    static String applicationEventMobileCellsRescan;
    static int applicationDeleteOldActivityLogs;
    static String applicationWidgetIconBackground;
    static String applicationWidgetIconLightnessB;
    static String applicationWidgetIconLightnessT;
    static boolean applicationEventUsePriority;
    static boolean applicationUnlinkRingerNotificationVolumes;
    static int applicationForceSetMergeRingNotificationVolumes;
    /*
    static boolean applicationSamsungEdgePrefIndicator;
    */
    static boolean applicationSamsungEdgeHeader;
    static String applicationSamsungEdgeBackground;
    static String applicationSamsungEdgeLightnessB;
    static String applicationSamsungEdgeLightnessT;
    static String applicationSamsungEdgeIconColor;
    static String applicationSamsungEdgeIconLightness;
    /*
    static boolean applicationSamsungEdgeGridLayout;
    */
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
    /*
    static boolean applicationEventWifiEnableWifi;
    static boolean applicationEventBluetoothEnableBluetooth;
    */
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
    static boolean applicationUseAlarmClock;
    static boolean notificationShowButtonExit;
    /*
    static boolean applicationWidgetOneRowPrefIndicator;
    */
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
    /*
    static boolean notificationDarkBackground;
    */
    static boolean notificationUseDecoration;
    static String notificationLayoutType;
    static String notificationBackgroundColor;
    /*
    static String applicationNightModeOffTheme;
    */
    static boolean applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    static String applicationSamsungEdgeVerticalPosition;
    static int notificationBackgroundCustomColor;
    static boolean notificationNightMode;
    static boolean applicationEditorHideHeaderOrBottomBar;
    static boolean applicationWidgetIconShowProfileDuration;

}
