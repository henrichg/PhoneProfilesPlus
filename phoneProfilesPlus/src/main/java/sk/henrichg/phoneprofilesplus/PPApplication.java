package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.crashlytics.android.Crashlytics;
//import com.evernote.android.job.JobApi;
//import com.evernote.android.job.JobConfig;
//import com.evernote.android.job.JobManager;
//import com.google.firebase.analytics.FirebaseAnalytics;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import dev.doubledot.doki.views.DokiContentView;
import io.fabric.sdk.android.Fabric;

//import com.github.anrwatchdog.ANRError;
//import com.github.anrwatchdog.ANRWatchDog;

public class PPApplication extends Application {

    //static final String romManufacturer = getROMManufacturer();
    static final boolean romIsMIUI = isMIUI();
    static final boolean romIsEMUI = isEMUI();
    static final boolean romIsSamsung = isSamsung();
    static final boolean romIsLG = isLG();

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";

    //static final int VERSION_CODE_EXTENDER_1_0_4 = 60;
    //static final int VERSION_CODE_EXTENDER_2_0 = 100;
    static final int VERSION_CODE_EXTENDER_3_0 = 200;
    static final int VERSION_CODE_EXTENDER_4_0 = 400;
    static final int VERSION_CODE_EXTENDER_LATEST = VERSION_CODE_EXTENDER_4_0;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && BuildConfig.DEBUG;
    private static final boolean logIntoFile = true;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && BuildConfig.DEBUG;
    private static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                         +"|PPApplication.isMIUI"
                                         +"|PPApplication.isEMUI"
                                         +"|PPApplication.isSamsung"
                                         +"|PPApplication.isLG"
                                         +"|PPApplication.exitApp"
                                         +"|PPApplication._exitApp"
                                         +"|PhoneProfilesService.onCreate"
                                         +"|PhoneProfilesService.onStartCommand"
                                         +"|PhoneProfilesService.doForFirstStart"
                                         +"|PhoneProfilesService.doCommand"
                                         //+"|PhoneProfilesService.isServiceRunningInForeground"
                                         //+"|PhoneProfilesService.showProfileNotification"
                                         //+"|PhoneProfilesService._showProfileNotification"
                                         //+"|PPApplication.createProfileNotificationChannel"
                                         +"|PhoneProfilesService.stopReceiver"
                                         +"|PhoneProfilesService.onDestroy"
                                         +"|DataWrapper.firstStartEvents"
                                         +"|BootUpReceiver"
                                         +"|PackageReplacedReceiver"
                                         +"|PhoneProfilesBackupAgent"
                                         +"|ShutdownBroadcastReceiver"

                                         +"|PhoneProfilesService.onConfigurationChanged"

                                         /*+"|DatabaseHandler.onUpgrade"
                                         +"|EditorProfilesActivity.doImportData"
                                         +"|PPApplication.setBlockProfileEventActions"
                                         +"|ImportantInfoHelpFragment.onViewCreated"
                                         +"|ImportantInfoNotification"*/

                                         //+"|TonesHandler"
                                         //+"|TonesHandler.isPhoneProfilesSilent"
                                         //+"|TonesHandler.getToneName"
                                         //+"|DatabaseHandler.fixPhoneProfilesSilentInProfiles"

                                         //+"|[RJS] PPApplication"
                                         //+"|##### ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|@@@ ScreenOnOffBroadcastReceiver.onReceive"
                                         //+"|[XXX] ScreenOnOffBroadcastReceiver.onReceive"

                                         //+"|PPApplication.startHandlerThread"

                                         //+"|DataWrapper.updateNotificationAndWidgets"
                                         //+"|ActivateProfileHelper.updateGUI"
                                         //+"|OneRowWidgetProvider.onUpdate"

                                         //+"|%%%%%%% DataWrapper.doHandleEvents"
                                         //+"|#### EventsHandler.handleEvents"
                                         //+"|[DEFPROF] EventsHandler"
                                         //+"|$$$ EventsHandler.handleEvents"
                                         //+"|[NOTIFY] EventsHandler"
                                         //+"|Profile.mergeProfiles"
                                         //+"|@@@ Event.pauseEvent"
                                         //+"|@@@ Event.stopEvent"
                                         //+"|### DataWrapper._activateProfile"
                                        //+"|$$$ restartEvents"
                                        //+"|DataWrapper._restartEvents"
                                        //+"|DataWrapper.restartEvents"
                                        //+"|PPApplication.startHandlerThread"
                                        //+"|Event.startEvent"
                                        //+"|Event.pauseEvent"
                                        //+"|[DSTART] DataWrapper.doHandleEvents"

                                         //+"|LauncherActivity.onStart"
                                         //+"|EditorProfilesActivity.onCreate"
                                         //+"|EditorProfilesActivity.onStart"
                                         //+"|EditorProfilesActivity.onActivityResult"

                                         //+"|PostDelayedBroadcastReceiver"


                                         /*
                                         +"|DataWrapper.restartEventsWithDelay"
                                         +"|DataWrapper.restartEvents"
                                         +"|DataWrapper._restartEvents"
                                         +"|RefreshActivitiesBroadcastReceiver"
                                         +"|$$$$$ EditorProfilesActivity"
                                         */

                                         //+"|ActivateProfileHelper.doExecuteForRadios"

                                         //+"|[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers"

                                         //+"|PPApplication.startPPService"

                                         //+"|GrantPermissionActivity"
                                         //+"|PhoneProfilesPreferencesNestedFragment.doOnActivityResult"

                                         +"|[****] BatteryBroadcastReceiver.onReceive"
                                         /*
                                         +"|[XXX] PowerSaveModeBroadcastReceiver.onReceive"
                                         +"|[XXX] BatteryBroadcastReceiver.onReceive"
                                         +"|[XXX] ScreenOnOffBroadcastReceiver.onReceive"
                                         */

                                         //+"|DataWrapper.activateProfileFromMainThread"
                                         //+"|ActivateProfileHelper.execute"
                                         //+"|Profile.convertPercentsToBrightnessManualValue"
                                         //+"|SettingsContentObserver"

                                         //+"|$$$ DataWrapper._activateProfile"
                                         //+"|ProfileDurationAlarmBroadcastReceiver.onReceive"
                                         //+"|DataWrapper.activateProfileAfterDuration"
                                         //+"|DataWrapper.getIsManualProfileActivation"

                                         //+"|BillingManager"
                                         //+"|DonationFragment"

                                         //+"|Permissions.grantProfilePermissions"
                                         //+"|Permissions.checkProfileVibrateWhenRinging"
                                         //+"|Permissions.checkVibrateWhenRinging"
                                         //+"|ActivateProfileHelper.executeForVolumes"
                                         //+"|ActivateProfileHelper.setZenMode"
                                         //+"|ActivateProfileHelper.setRingerMode"
                                         //+"|ActivateProfileHelper.setVolumes"
                                         //+"|ActivateProfileHelper.changeRingerModeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.isAudibleSystemRingerMode"
                                         //+"|ActivateProfileHelper.setVibrateWhenRinging"

                                         //+"|PhoneProfilesPreferencesNestedFragment.onActivityCreated"
                                         //+"|PhoneProfilesPrefsActivity"
                                         //+"|PhoneProfilesPrefsFragment.onCreate"
                                         //+"|PhoneProfilesPrefsFragment.onCreatePreferences"
                                         //+"|PhoneProfilesPrefsFragment.updateSharedPreferences"
                                         //+"|PhoneProfilesPrefsFragment.initPreferenceFragment"
                                         //+"|PhoneProfilesPrefsFragment.loadSharedPreferences"
                                         //+"|ProfilesPrefsActivity"
                                         //+"|ProfilesPrefsFragment"
                                         //+"|ProfilesPrefsFragment.onCreate"
                                         //+"|ProfilesPrefsFragment.onDisplayPreferenceDialog"
                                         //+"|ProfilesPrefsFragment.onActivityCreated"
                                         //+"|ProfilesPrefsFragment.setPermissionsPreference"
                                         //+"|ProfilesPrefsActivity.getProfileFromPreferences"
                                         /*+"|EventsPrefsActivity"
                                         +"|EventsPrefsFragment"
                                         +"|PhoneProfilesPrefsNotifications"
                                         +"|LocationGeofencePreferenceX"
                                         +"|ProfilePreferenceX"
                                         +"|RingtonePreferenceX"
                                         +"|VolumeDialogPreferenceX"
                                         +"|VolumeDialogPreferenceFragmentX"
                                         +"|ApplicationsDialogPreferenceX"
                                         +"|ApplicationsDialogPreferenceFragmentX"
                                         +"|LocationGeofencePreferenceX"
                                         +"|LocationGeofencePreferenceFragmentX"
                                         +"|MobileCellsRegistrationDialogPreferenceX"
                                         +"|MobileCellsRegistrationDialogPreferenceFragmentX"
                                         +"|ProfileIconPreferenceX"
                                         +"|ProfileIconPreferenceFragmentX"
                                         +"|TimePreferenceX"
                                         +"|TimePreferenceFragmentX"*/

                                         +"|Event.notifyEventStart"
                                         +"|StartEventNotificationBroadcastReceiver"
                                         +"|StartEventNotificationDeletedReceiver"
                                         //+"|PhoneProfilesService.playNotificationSound"

                                         //+"|PPNotificationListenerService"
                                         //+"|[NOTIF] EventsHandler.handleEvents"
                                         //+"|[NOTIF] DataWrapper.doHandleEvents"
                                         //+"|EventPreferencesNotification.isNotificationActive"
                                         //+"|EventPreferencesNotification.isNotificationVisible"
                                         //+"|NotificationEventEndBroadcastReceiver"

                                         //+"|[CALL] DataWrapper.doHandleEvents"

                                         //+"|"+CallsCounter.LOG_TAG
                                         //+"|[RJS] PPApplication"
                                         //+"|[RJS] PhoneProfilesService"

                                         //+"|ActivateProfileHelper.setAirplaneMode_SDK17"
                                         //+"|ActivateProfileHelper.executeForRadios"
                                         //+"|$$$ WifiAP"


                                         //+"|DeviceIdleModeBroadcastReceiver"

                                         //+"|##### GeofenceScanner"
                                         +"|GeofenceScannerJob"
                                         //+"|GeofenceScannerJob.scheduleJob"
                                         //+"|GeofenceScannerJob.onRunJob"
                                         //+"|LocationGeofenceEditorActivity"
                                         //+"|LocationModeChangedBroadcastReceiver"
                                         //+"|PhoneProfilesService.scheduleGeofenceScannerJob"
                                         //+"|PhoneProfilesService.startGeofenceScanner"
                                         //+"|PhoneProfilesService.stopGeofenceScanner"
                                         //+"|[GeoSensor] DataWrapper.doHandleEvents"
                                         +"|[***] GeofenceScanner"
                                         +"|GeofenceScanWorker"

                                         //+"|WifiStateChangedBroadcastReceiver"
                                         //+"|WifiConnectionBroadcastReceiver"
                                         //+"|%%%% WifiBluetoothScanner.doScan"
                                         //+"|$$$W WifiBluetoothScanner"
                                         //+"|$$$B WifiBluetoothScanner"
                                         //+"|$$$BCL WifiBluetoothScanner"
                                         //+"|$$$BLE WifiBluetoothScanner"
                                         //+"|[WiFi] DataWrapper.doHandleEvents"

                                         /*+"|BluetoothScanWorker.doWork"
                                         +"|BluetoothScanWorker.startScanner"
                                         +"|BluetoothScanWorker.startCLScan"
                                         +"|BluetoothScanWorker.stopCLScan"
                                         +"|BluetoothScanWorker.startLEScan"
                                         +"|BluetoothScanWorker.stopLEScan"
                                         +"|BluetoothScanWorker.doWork"
                                         +"|BluetoothScanWorker.finishCLScan"
                                         +"|BluetoothScanWorker.finishLEScan"
                                         +"|BluetoothScanBroadcastReceiver.onReceive"
                                         +"|@@@ BluetoothScanBroadcastReceiver.onReceive"
                                         +"|BluetoothLEScanCallback21"*/
                                        //+"|[BTScan] DataWrapper.doHandleEvents"
                                        //+"|BluetoothConnectedDevices"
                                        //+"|BluetoothConnectionBroadcastReceiver"
                                        //+"|BluetoothStateChangedBroadcastReceiver"
                                        //+"|BluetoothScanBroadcastReceiver"
                                        //+"|BluetoothScanWorker"
                                        //+"|$$$B WifiBluetoothScanner.doScan"

                                         //+"|PostDelayedBroadcastReceiver.onReceive"

                                         //+"|WifiScanWorker"
                                         //+"|%%%% WifiScanBroadcastReceiver.onReceive"

                                         //+"|WifiSSIDPreference.refreshListView"

                                         //+"|%%%%%%% DataWrapper.doHandleEvents"

                                         //+"|[RJS] PhoneProfilesService.registerForegroundApplicationChangedReceiver"
                                         //+"|PhoneProfilesService.runEventsHandlerForOrientationChange"
                                         //+"|PhoneProfilesService.onSensorChanged"
                                         //+"|PPPExtenderBroadcastReceiver"


                                         //+"|PhoneProfilesService.doSimulatingRingingCall"
                                         //+"|PhoneProfilesService.startSimulatingRingingCall"
                                         //+"|PhoneProfilesService.stopSimulatingRingingCall"
                                         //+"|PhoneProfilesService.onAudioFocusChange"

                                         /*
                                         +"|ActivateProfileHelper.(s)setRingerMode"
                                         +"|ActivateProfileHelper.(s)setZenMode"
                                         +"|ActivateProfileHelper.(s)setRingerVolume"
                                         */
                                         //+"|@@@ EventsHandler.handleEvents"
                                         //+"|EventsHandler.doEndService"

                                         //+"|ActivateProfileHelper.doExecuteForRadios"
                                         //+"|CmdMobileData.isEnabled"
                                         //+"|$$$ WifiAP"

                                         //+"|RunApplicationWithDelayBroadcastReceiver"

                                         //+"|PreferenceFragment"

                                        //+"|PhoneProfilesService.registerAccessibilityServiceReceiver"
                                        //+"|DatabaseHandler.getTypeProfilesCount"
                                        //+"|[RJS] PhoneProfilesService.registerPPPPExtenderReceiver"
                                        //+"|PPPExtenderBroadcastReceiver.onReceive"
                                        //+"|SMSEventEndBroadcastReceiver.onReceive"
                                        //+"|[SMS sensor]"

                                        //+ "|[RJS] PhoneProfilesService.startPhoneStateScanner"
                                        //+ "|PhoneStateScanner"
                                        //+"|MobileCellsPreference"
                                        //+"|MobileCellsPreference.refreshListView"
                                        //+"|PhoneStateScanner.constructor"
                                        //+"|PhoneStateScanner.connect"
                                        //+"|PhoneStateScanner.disconnect"
                                        //+"|PhoneStateScanner.startAutoRegistration"
                                        //+"|PhoneStateScanner.stopAutoRegistration"
                                        //+"|PhoneStateScanner.getAllCellInfo"
                                        //+"|PhoneStateScanner.getCellLocation"
                                        //+"|PhoneStateScanner.doAutoRegistration"
                                        //+"|MobileCellsRegistrationDialogPreference.startRegistration"
                                        //+"|MobileCellsRegistrationService"
                                        //+"|NotUsedMobileCellsNotificationDeletedReceiver.onReceive"

                                        //+"|PermissionsNotificationDeletedReceiver.onReceive"

                                        //+"|[RJS] PhoneProfilesService.registerReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.unregisterReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.reregisterReceiversAndWorkers"
                                        //+"|[RJS] PhoneProfilesService.registerReceiverForTimeSensor"

                                        //+"|EventPreferencesActivity.savePreferences"

                                        //+"|PhoneCallReceiver"
                                        //+"|PhoneCallBroadcastReceiver"
                                        //+"|PhoneCallBroadcastReceiver.callAnswered"

                                        //+"|#### EventsHandler.handleEvents"
                                        //+"|[CALL] EventsHandler.handleEvents"
                                        //+"|%%%%%%% DataWrapper.doHandleEvents"
                                        //+"|[CALL] DataWrapper.doHandleEvents"
                                        //+"|DataWrapper.pauseAllEvents"
                                        //+"|EventPreferencesCall"
                                        //+"|MissedCallEventEndBroadcastReceiver"

                                        //+"|LauncherActivity"

                                        //+"|AlarmClockBroadcastReceiver.onReceive"
                                        //+"|NextAlarmClockBroadcastReceiver"
                                        //+"|TimeChangedReceiver.onReceive"

                                        //+"|@@@ ScreenOnOffBroadcastReceiver"
                                        //+"|LockDeviceActivity"

                                        //+"|DialogHelpPopupWindow.showPopup"

                                        //+"|SMSBroadcastReceiver.onReceive"

                                        //+"|EditorProfilesActivity.changeEventOrder"
                                        //+"|EditorProfilesActivity.selectDrawerItem"

                                        //+"|NFCTagPreference.showEditMenu"

                                        //+"|Profile.generateIconBitmap"

                                        //+"|CalendarProviderChangedBroadcastReceiver"

                                        /*
                                        +"|EventPreferencesTime.computeAlarm"
                                        +"|EventPreferencesTime.removeSystemEvent"
                                        +"|EventPreferencesTime.setSystemEventForStart"
                                        +"|EventPreferencesTime.setSystemEventForPause"
                                        //+"|EventPreferencesTime.removeAlarm"
                                        //+"|EventPreferencesTime.setAlarm"
                                        +"|[TIME] DataWrapper.doHandleEvents"
                                        +"|TwilightScanner"
                                        //+"|EventTimeBroadcastReceiver"
                                        */

                                        //+"|EventPreferencesCalendar.saveStartEndTime"

                                        //+"|DatabaseHandler.importDB"
                                        //+ "|ApplicationsMultiSelectDialogPreference.getValueAMSDP"
                                        //+ "|ApplicationsDialogPreference"
                                        //+ "|ApplicationEditorDialogAdapter"
                                        //+ "|ApplicationEditorDialogViewHolder"
                                        //+ "|ApplicationEditorDialog"
                                        //+ "|ApplicationEditorIntentActivity"
                                        //+ "|ApplicationsCache.cacheApplicationsList"
                                        //+ "|@ Application."

                                        //+ "|BitmapManipulator.resampleBitmapUri"

                                        //+"|CmdGoToSleep"
                                        //+"|CmdNfc"
                                        //+"|CmdWifiAP"
                                        //+"|ActivateProfileHelper.wifiServiceExists"

                                        //+"|ActivateProfileHelper.lockDevice"

                                        //+"|#### setWifiEnabled"

                                        //+"|PPNumberPicker"
                                        //+"|RingtonePreference.setRingtone"
                                        //+"|RingtonePreferenceX"
                                        //+"|PhoneProfilesService.playNotificationSound"

                                        //+"|[RJS] PhoneProfilesService.scheduleWifiWorker"
                                        //+"|[RJS] PhoneProfilesService.cancelWifiWorker"

                                        //+"|EditorProfilesActivity.selectFilterItem"
                                        //+"|EventsPrefsFragment.onResume"
                                        //+"|ActivateProfileHelper.setScreenCarMode"

                                        //+"|DonationBroadcastReceiver"

                                        //+"|FastAccessDurationDialog.updateProfileView"

                                        //+"|NotUsedMobileCellsNotificationDisableReceiver"
                                        //+"|NotUsedMobileCellsNotificationDeletedReceiver"

                                        //+"|ActivateProfileHelper.executeForForceStopApplications"

                                        //+"|DaysOfWeekPreferenceX"
                                        //+"|EventPreferencesTime.getDayOfWeekByLocale"

                                        +"|SearchCalendarEventsJob"
                                        +"|SearchCalendarEventsWorker"
            ;


    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    private static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_EVENT_STATUS = "event_status";

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

    //static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    //static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_activated_profile";
    static final String MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_mobile_cells_registration";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_grant_permission";
    static final String NOTIFY_EVENT_START_NOTIFICATION_CHANNEL = "phoneProfilesPlus_repeat_notify_event_start";
    static final String NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL = "phoneProfilesPlus_new_mobile_cell";
    static final String DONATION_CHANNEL = "phoneProfilesPlus_donation";

    static final int PROFILE_NOTIFICATION_ID = 700420;
    static final int IMPORTANT_INFO_NOTIFICATION_ID = 700422;
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 700423;
    //static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 700424;
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 700425;
    //static final int LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID = 700426;
    //static final int LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID = 700427;
    static final int GEOFENCE_SCANNER_ERROR_NOTIFICATION_ID = 700428;
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 700429;
    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 700430;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 700431;
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 700432;
    //static final int EVENT_START_NOTIFICATION_ID = 700433;
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 700434;
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 700435;
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 700436;
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 700437;
    static final int MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID = 700438;
    static final int GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID = 700439;
    //static final int LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID = 700440;

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    //static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    static final String ACTIVATED_PROFILE_PREFS_NAME = "profile_preferences_activated_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    //static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";

    //public static final String RESCAN_TYPE_SCREEN_ON = "1";
    public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";
    private static final String PREF_DONATION_DONATED = "donation_donated";
    private static final String PREF_NOTIFICATION_PROFILE_NAME = "notification_profile_name";
    private static final String PREF_WIDGET_PROFILE_NAME = "widget_profile_name";
    private static final String PREF_ACTIVITY_PROFILE_NAME = "activity_profile_name";

    // scanner start/stop types
    static final int SCANNER_START_GEOFENCE_SCANNER = 1;
    static final int SCANNER_STOP_GEOFENCE_SCANNER = 2;
    static final int SCANNER_RESTART_GEOFENCE_SCANNER = 3;

    static final int SCANNER_START_ORIENTATION_SCANNER = 4;
    static final int SCANNER_STOP_ORIENTATION_SCANNER = 5;
    static final int SCANNER_RESTART_ORIENTATION_SCANNER = 6;

    static final int SCANNER_START_PHONE_STATE_SCANNER = 7;
    static final int SCANNER_STOP_PHONE_STATE_SCANNER = 8;
    static final int SCANNER_FORCE_START_PHONE_STATE_SCANNER = 9;
    static final int SCANNER_RESTART_PHONE_STATE_SCANNER = 10;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 11;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 12;
    static final int SCANNER_RESTART_WIFI_SCANNER = 13;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 14;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 15;
    static final int SCANNER_RESTART_BLUETOOTH_SCANNER = 16;

    static final int SCANNER_START_TWILIGHT_SCANNER = 17;
    static final int SCANNER_STOP_TWILIGHT_SCANNER = 18;
    static final int SCANNER_RESTART_TWILIGHT_SCANNER = 19;

    static final int SCANNER_RESTART_ALL_SCANNERS = 50;

    static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";

    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    static final String ACTION_FOREGROUND_APPLICATION_CHANGED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FOREGROUND_APPLICATION_CHANGED";
    static final String ACTION_REGISTER_PPPE_FUNCTION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_REGISTER_PPPE_FUNCTION";
    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_SMS_MMS_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_SMS_MMS_RECEIVED";
    static final String ACTION_CALL_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_CALL_RECEIVED";
    static final String ACTION_LOCK_DEVICE = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_LOCK_DEVICE";
    static final String ACCESSIBILITY_SERVICE_PERMISSION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACCESSIBILITY_SERVICE_PERMISSION";
    static final String ACTION_DONATION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_DONATION";

    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER = 2;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER = -2;
    static final int REGISTRATION_TYPE_SMS_REGISTER = 3;
    static final int REGISTRATION_TYPE_SMS_UNREGISTER = -3;
    static final int REGISTRATION_TYPE_CALL_REGISTER = 4;
    static final int REGISTRATION_TYPE_CALL_UNREGISTER = -4;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    static final String EXTRA_APPLICATIONS = "extra_applications";

    static final String CRASHLYTICS_LOG_DEVICE_ROOTED = "DEVICE_ROOTED";

    public static boolean isScreenOn;

//    static private FirebaseAnalytics firebaseAnalytics;

    public static HandlerThread handlerThread = null;
    public static HandlerThread handlerThreadInternalChangeToFalse = null;
    public static HandlerThread handlerThreadWidget = null;
    public static HandlerThread handlerThreadProfileNotification = null;
    public static HandlerThread handlerThreadPlayTone = null;
    public static HandlerThread handlerThreadPPService = null;

    //private static HandlerThread handlerThreadRoot = null;
    public static HandlerThread handlerThreadVolumes = null;
    public static HandlerThread handlerThreadRadios = null;
    public static HandlerThread handlerThreadAdaptiveBrightness = null;
    public static HandlerThread handlerThreadWallpaper = null;
    public static HandlerThread handlerThreadPowerSaveMode = null;
    public static HandlerThread handlerThreadLockDevice = null;
    public static HandlerThread handlerThreadRunApplication = null;
    public static HandlerThread handlerThreadHeadsUpNotifications = null;
    //public static HandlerThread handlerThreadMobileCells = null;
    public static HandlerThread handlerThreadBluetoothConnectedDevices = null;
    public static HandlerThread handlerThreadNotificationLed = null;

    private static HandlerThread handlerThreadRestartEventsWithDelay = null;
    public static Handler restartEventsWithDelayHandler = null;

    public static Handler toastHandler;
    public static Handler brightnessHandler;
    public static Handler screenTimeoutHandler;

    public static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    public static final RootMutex rootMutex = new RootMutex();
    private static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    //public static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    public static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    //public static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    public static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    public static final GeofenceScannerLastLocationMutex geofenceScannerLastLocationMutex = new GeofenceScannerLastLocationMutex();
    public static final GeofenceScannerMutex geofenceScannerMutex = new GeofenceScannerMutex();
    public static final WifiBluetoothScannerMutex wifiBluetoothscannerMutex = new WifiBluetoothScannerMutex();
    public static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    public static final PhoneStateScannerMutex phoneStateScannerMutex = new PhoneStateScannerMutex();
    public static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    public static final BluetoothScanMutex bluetoothScanMutex = new BluetoothScanMutex();
    public static final BluetoothLEScanMutex bluetoothLEScanMutex = new BluetoothLEScanMutex();
    public static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    public static final TwilightScannerMutex twilightScannerMutex = new TwilightScannerMutex();
    public static final PPNotificationListenerService ppNotificationListenerService = new PPNotificationListenerService();

    //public static boolean isPowerSaveMode = false;

    // !! this must be here
    public static boolean blockProfileEventActions = false;

    // Samsung Look instance
    public static Slook sLook = null;
    public static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    private static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    private static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();

    public static final Random requestCodeForAlarm = new Random();

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("##### PPApplication.onCreate", "romManufacturer="+Build.MANUFACTURER);
        PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI="+romIsMIUI);
        PPApplication.logE("##### PPApplication.onCreate", "romIsEMUI="+romIsEMUI);
        PPApplication.logE("##### PPApplication.onCreate", "romIsSamsung="+romIsSamsung);
        PPApplication.logE("##### PPApplication.onCreate", "romIsLG="+romIsLG);

        if (checkAppReplacingState())
            return;

        ///////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // https://github.com/tiann/FreeReflection
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                Object vmRuntime = getRuntime.invoke(null);

                setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
            } catch (Exception e) {
                Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
            }
        }
        //////////////////////////////////////////

        if (logIntoFile || crashIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());

        try {
            //if (!BuildConfig.DEBUG) {
                // Obtain the FirebaseAnalytics instance.
                //firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            //}

            /*
            // Set up Crashlytics, disabled for debug builds
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build();

            Fabric.with(this, crashlyticsKit);
            */
            //if (!BuildConfig.DEBUG) {
                Fabric.with(this, new Crashlytics());
            //}
            // Crashlytics.getInstance().core.logException(exception); -- this log will be associated with crash log.
        } catch (Exception e) {
            /*
            java.lang.IllegalStateException:
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:447)
              at android.app.ContextImpl.getSharedPreferences (ContextImpl.java:432)
              at android.content.ContextWrapper.getSharedPreferences (ContextWrapper.java:174)
              at io.fabric.sdk.android.services.persistence.PreferenceStoreImpl.<init> (PreferenceStoreImpl.java:39)
              at io.fabric.sdk.android.services.common.AdvertisingInfoProvider.<init> (AdvertisingInfoProvider.java:37)
              at io.fabric.sdk.android.services.common.IdManager.<init> (IdManager.java:114)
              at io.fabric.sdk.android.Fabric$Builder.build (Fabric.java:289)
              at io.fabric.sdk.android.Fabric.with (Fabric.java:340)

              This exception occurs, when storage is protected and PPP is started via LOCKED_BOOT_COMPLETED

              Code from android.app.ContextImpl:
                if (getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.O) {
                    if (isCredentialProtectedStorage()
                            && !getSystemService(UserManager.class)
                                    .isUserUnlockingOrUnlocked(UserHandle.myUserId())) {
                        throw new IllegalStateException("SharedPreferences in credential encrypted "
                                + "storage are not available until after user is unlocked");
                    }
                }
            */
            Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
        }

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Crashlytics.getInstance().core.logException(error);
            }
        });
        anrWatchDog.start();
        */

        try {
            Crashlytics.setBool("DEBUG", BuildConfig.DEBUG);
        } catch (Exception ignored) {}

        //if (BuildConfig.DEBUG) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = false;

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.dashClockBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver"));

        startHandlerThread("PPApplication.onCreate");
        startHandlerThreadInternalChangeToFalse();
        startHandlerThreadPPService();
        //startHandlerThreadRoot();
        startHandlerThreadWidget();
        startHandlerThreadProfileNotification();
        startHandlerThreadPlayTone();
        startHandlerThreadVolumes();
        startHandlerThreadRadios();
        startHandlerThreadAdaptiveBrightness();
        startHandlerThreadWallpaper();
        startHandlerThreadPowerSaveMode();
        startHandlerThreadLockDevice();
        startHandlerThreadRunApplication();
        startHandlerThreadHeadsUpNotifications();
        //startHandlerThreadMobileCells();
        startHandlerThreadRestartEventsWithDelay();
        startHandlerThreadBluetoothConnectedDevices();
        startHandlerThreadNotificationLed();

        toastHandler = new Handler(getMainLooper());
        brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

        /*
        JobConfig.setApiEnabled(JobApi.WORK_MANAGER, true);
        //JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        //JobConfig.setApiEnabled(JobApi.GCM, false); // is only important for Android 4.X

        JobManager.create(this).addJobCreator(new PPJobsCreator());
        */

        PPApplication.initRoot();

        /*
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            F field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
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

        // Samsung Look initialization
        sLook = new Slook();
        try {
            sLook.initialize(this);
            // true = The Device supports Edge Single Mode, Edge Single Plus Mode, and Edge Feeds Mode.
            sLookCocktailPanelEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_PANEL);
            // true = The Device supports Edge Immersive Mode feature.
            //sLookCocktailBarEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_BAR);
        } catch (SsdkUnsupportedException e) {
            sLook = null;
        }

        if (PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            try {
                PPApplication.logE("##### PPApplication.onCreate", "start service");
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                startPPService(getApplicationContext(), serviceIntent);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            Log.w("PPApplication.onCreate", "app is replacing...kill");
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

    /*
    static boolean isNewVersion(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        PPApplication.logE("PPApplication.isNewVersion", "oldVersionCode="+oldVersionCode);
        int actualVersionCode;
        try {
            if (oldVersionCode == 0) {
                // save version code
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("PPApplication.isNewVersion", "actualVersionCode=" + actualVersionCode);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        return pInfo.versionCode;
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

        try {
            // warnings when logIntoFile == false
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
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
        } catch (IOException e) {
            Log.e("PPApplication.logIntoFile", Log.getStackTraceString(e));
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] splits = logFilterTags.split("\\|");
        for (String split : splits) {
            if (tag.contains(split)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    static public boolean logEnabled() {
        return (logIntoLogCat || logIntoFile);
    }

    @SuppressWarnings("unused")
    static public void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static public void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
    }

    /*
    public static String intentToString(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.toString() + " " + bundleToString(intent.getExtras());
    }
    */

    /*
    private static String bundleToString(Bundle bundle) {
        StringBuilder out = new StringBuilder("Bundle[");

        if (bundle == null) {
            out.append("null");
        } else {
            boolean first = true;
            for (String key : bundle.keySet()) {
                if (!first) {
                    out.append(", ");
                }

                out.append(key).append('=');

                Object value = bundle.get(key);

                if (value instanceof int[]) {
                    out.append(Arrays.toString((int[]) value));
                } else if (value instanceof byte[]) {
                    out.append(Arrays.toString((byte[]) value));
                } else if (value instanceof boolean[]) {
                    out.append(Arrays.toString((boolean[]) value));
                } else if (value instanceof short[]) {
                    out.append(Arrays.toString((short[]) value));
                } else if (value instanceof long[]) {
                    out.append(Arrays.toString((long[]) value));
                } else if (value instanceof float[]) {
                    out.append(Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    out.append(Arrays.toString((double[]) value));
                } else if (value instanceof String[]) {
                    out.append(Arrays.toString((String[]) value));
                } else if (value instanceof CharSequence[]) {
                    out.append(Arrays.toString((CharSequence[]) value));
                } else if (value instanceof Parcelable[]) {
                    out.append(Arrays.toString((Parcelable[]) value));
                } else if (value instanceof Bundle) {
                    out.append(bundleToString((Bundle) value));
                } else {
                    out.append(value);
                }

                first = false;
            }
        }

        out.append("]");
        return out.toString();
    }
    */

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent) {
        PPApplication.logE("PPApplication.startPPService", "xxx");
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else
            context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
        PPApplication.logE("PPApplication.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static public boolean getApplicationStarted(Context context, boolean testService)
    {
        ApplicationPreferences.getSharedPreferences(context);
        if (testService)
            return ApplicationPreferences.preferences.getBoolean(PREF_APPLICATION_STARTED, false) &&
                    (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
        else
            return ApplicationPreferences.preferences.getBoolean(PREF_APPLICATION_STARTED, false);
    }

    static public void setApplicationStarted(Context context, boolean appStarted)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
        editor.apply();
    }

    static public int getSavedVersionCode(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static public void setSavedVersionCode(Context context, int version)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }

    static public boolean getActivityLogEnabled(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_ACTIVITY_LOG_ENABLED, true);
    }

    static public void setActivityLogEnabled(Context context, boolean enabled)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_ACTIVITY_LOG_ENABLED, enabled);
        editor.apply();
    }

    static public int getDaysAfterFirstStart(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }

    static public void setDaysAfterFirstStart(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static public int getDonationNotificationCount(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DONATION_NOTIFICATION_COUNT, 0);
    }

    static public void setDonationNotificationCount(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static public int getDaysForNextDonationNotification(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }

    static public void setDaysForNextDonationNotification(Context context, int days)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
        editor.apply();
    }

    static public boolean getDonationDonated(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_DONATION_DONATED, false);
    }

    static public void setDonationDonated(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_DONATION_DONATED, true);
        editor.apply();
    }

    static public String getNotificationProfileName(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getString(PREF_NOTIFICATION_PROFILE_NAME, "");
    }

    static public void setNotificationProfileName(Context context, String notificationProfileName)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(PREF_NOTIFICATION_PROFILE_NAME, notificationProfileName);
        editor.apply();
    }

    static public String getWidgetProfileName(Context context, int widgetType)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, "");
    }

    static public void setWidgetProfileName(Context context, int widgetType, String widgetProfileName)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, widgetProfileName);
        editor.apply();
    }

    static public String getActivityProfileName(Context context, int activityType)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, "");
    }

    static public void setActivityProfileName(Context context, int activityType, String activityProfileName)
    {
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, activityProfileName);
        editor.apply();
    }

    // --------------------------------

    // notification channels -------------------------

    static void createProfileNotificationChannel(/*Profile profile, */Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            int importance;
            //PPApplication.logE("PPApplication.createProfileNotificationChannel","show in status bar="+ApplicationPreferences.notificationShowInStatusBar(context));
            //if (ApplicationPreferences.notificationShowInStatusBar(context)) {
                /*KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    if ((ApplicationPreferences.notificationHideInLockScreen(context) && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        importance = NotificationManager.IMPORTANCE_MIN;
                    else
                        importance = NotificationManager.IMPORTANCE_LOW;
                }
                else*/
            //        importance = NotificationManager.IMPORTANCE_DEFAULT;
            //}
            //else
            //    importance = NotificationManager.IMPORTANCE_MIN;
            importance = NotificationManager.IMPORTANCE_LOW;

            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_activated_profile);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_activated_profile_description_ppp);

            NotificationChannel channel = new NotificationChannel(PROFILE_NOTIFICATION_CHANNEL, name, importance);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);
            channel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createMobileCellsRegistrationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_mobile_cells_registration_description);

            NotificationChannel channel = new NotificationChannel(MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);
            channel.setShowBadge(false);

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createInformationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_information);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(INFORMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createExclamationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_exclamation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createGrantPermissionNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_grant_permission);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_grant_permission_description);

            NotificationChannel channel = new NotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createNotifyEventStartNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_notify_event_start);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_notify_event_start_description);

            NotificationChannel channel = new NotificationChannel(NOTIFY_EVENT_START_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setSound(null, null);

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createMobileCellsNewCellNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_not_used_mobile_cell);
            // The user-visible description of the channel.
            String description = context.getString(R.string.notification_channel_not_used_mobile_cell_description);

            NotificationChannel channel = new NotificationChannel(NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    static void createDonationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            // The user-visible name of the channel.
            CharSequence name = context.getString(R.string.notification_channel_donation);
            // The user-visible description of the channel.
            String description = context.getString(R.string.empty_string);

            NotificationChannel channel = new NotificationChannel(DONATION_CHANNEL, name, NotificationManager.IMPORTANCE_LOW);

            // Configure the notification channel.
            //channel.setImportance(importance);
            channel.setDescription(description);
            channel.enableLights(false);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            //channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }


    static void createNotificationChannels(Context appContext) {
        PPApplication.createProfileNotificationChannel(appContext);
        PPApplication.createMobileCellsRegistrationNotificationChannel(appContext);
        PPApplication.createInformationNotificationChannel(appContext);
        PPApplication.createExclamationNotificationChannel(appContext);
        PPApplication.createGrantPermissionNotificationChannel(appContext);
        PPApplication.createNotifyEventStartNotificationChannel(appContext);
        PPApplication.createMobileCellsNewCellNotificationChannel(appContext);
        PPApplication.createDonationNotificationChannel(appContext);
    }

    static void showProfileNotification(/*Context context*/boolean refresh) {
        try {
            PPApplication.logE("PPApplication.showProfileNotification", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SHOW_PROFILE_NOTIFICATION, true);
            PPApplication.startPPService(context, serviceIntent);*/
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(refresh);
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------

    // root ------------------------------------------

    static synchronized void initRoot() {
        synchronized (PPApplication.rootMutex) {
            rootMutex.rootChecked = false;
            rootMutex.rooted = false;
            //rootMutex.grantRootChecked = false;
            //rootMutex.rootGranted = false;
            rootMutex.settingsBinaryChecked = false;
            rootMutex.settingsBinaryExists = false;
            //rootMutex.isSELinuxEnforcingChecked = false;
            //rootMutex.isSELinuxEnforcing = false;
            //rootMutex.suVersion = null;
            //rootMutex.suVersionChecked = false;
            rootMutex.serviceBinaryChecked = false;
            rootMutex.serviceBinaryExists = false;
        }
    }

    private static boolean _isRooted()
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.rootChecked) {
            try {
                Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
            } catch (Exception ignored) {}
            return rootMutex.rooted;
        }

        try {
            PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //if (RootTools.isRootAvailable()) {
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                PPApplication.logE("PPApplication._isRooted", "root available");
                rootMutex.rooted = true;
            } else {
                PPApplication.logE("PPApplication._isRooted", "root NOT available");
                rootMutex.rooted = false;
                //rootMutex.settingsBinaryExists = false;
                //rootMutex.settingsBinaryChecked = false;
                //rootMutex.isSELinuxEnforcingChecked = false;
                //rootMutex.isSELinuxEnforcing = false;
                //rootMutex.suVersionChecked = false;
                //rootMutex.suVersion = null;
                //rootMutex.serviceBinaryExists = false;
                //rootMutex.serviceBinaryChecked = false;
            }
            rootMutex.rootChecked = true;
            try {
                Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Log.e("PPApplication._isRooted", Log.getStackTraceString(e));
        }
        //if (rooted)
        //	getSUVersion();
        return rootMutex.rooted;
    }

    static boolean isRooted(boolean fromUIThread) {
        if (rootMutex.rootChecked)
            return rootMutex.rooted;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            return _isRooted();
        }
    }

    static void isRootGranted(/*boolean onlyCheck*/)
    {
        RootShell.debugMode = rootToolsDebug;

        /*if (onlyCheck && rootMutex.grantRootChecked)
            return rootMutex.rootGranted;*/

        if (isRooted(false)) {
            synchronized (PPApplication.rootMutex) {
                try {
                    PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    } else {
                        // grant denied
                        PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }
                } catch (Exception e) {
                    Log.e("PPApplication.isRootGranted", Log.getStackTraceString(e));
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        } else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
        }
        //return false;
    }

    static boolean settingsBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.settingsBinaryChecked)
            return rootMutex.settingsBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.settingsBinaryChecked) {
                PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                rootMutex.settingsBinaryChecked = true;
            }
            PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists=" + rootMutex.settingsBinaryExists);
            return rootMutex.settingsBinaryExists;
        }
    }

    static boolean serviceBinaryExists(boolean fromUIThread)
    {
        RootShell.debugMode = rootToolsDebug;

        if (rootMutex.serviceBinaryChecked)
            return rootMutex.serviceBinaryExists;

        if (fromUIThread)
            return false;

        synchronized (PPApplication.rootMutex) {
            if (!rootMutex.serviceBinaryChecked) {
                PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                rootMutex.serviceBinaryChecked = true;
            }
            PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists=" + rootMutex.serviceBinaryExists);
            return rootMutex.serviceBinaryExists;
        }
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     * 
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     */
    /*public static boolean isSELinuxEnforcing()
    {
        RootShell.debugMode = rootToolsDebug;

        synchronized (PPApplication.rootMutex) {
            if (!isSELinuxEnforcingChecked)
            {
                boolean enforcing = false;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                //if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                            //noinspection TryFinallyCanBeTryWithResources
                            try {
                                enforcing = (is.read() == '1');
                            } finally {
                                is.close();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                //}

                isSELinuxEnforcing = enforcing;
                isSELinuxEnforcingChecked = true;
            }

            PPApplication.logE("PPApplication.isSELinuxEnforcing", "isSELinuxEnforcing="+isSELinuxEnforcing);

            return isSELinuxEnforcing;
        }
    }*/

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
                RootTools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
            } catch (RootDeniedException e) {
                PPApplication.rootMutex.rootGranted = false;
                Log.e("PPApplication.getSUVersion", Log.getStackTraceString(e));
            } catch (Exception e) {
                Log.e("PPApplication.getSUVersion", Log.getStackTraceString(e));
            }
        }
        return suVersion;
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

            FileOutputStream fos = context.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(name);
            if (!file.setExecutable(true))
                return null;

            return file.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }

    static void getServicesList() {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList == null)
                serviceListMutex.serviceList = new ArrayList<>();
            else
                serviceListMutex.serviceList.clear();
        }
        synchronized (PPApplication.rootMutex) {
            //noinspection RegExpRedundantEscape
            final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Command command = new Command(0, false, "service list") {
                @Override
                public void commandOutput(int id, String line) {
                    //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - line="+line);
                    Matcher matcher = compile.matcher(line);
                    if (matcher.find()) {
                        synchronized (PPApplication.serviceListMutex) {
                            //noinspection unchecked
                            serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                            //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                        }
                    }
                    super.commandOutput(id, line);
                }
            };
            try {
                RootTools.getShell(false).add(command);
                commandWait(command);
            } catch (Exception e) {
                Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
            }
        }
    }

    static Object getServiceManager(String serviceType) {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList != null) {
                for (Pair pair : serviceListMutex.serviceList) {
                    if (serviceType.equals(pair.first)) {
                        return pair.second;
                    }
                }
            }
            return null;
        }
    }

    static int getTransactionCode(String serviceManager, String method) {
        int code = -1;
        try {
            for (Class declaredFields : Class.forName(serviceManager).getDeclaredClasses()) {
                Field[] declaredFields2 = declaredFields.getDeclaredFields();
                int length = declaredFields2.length;
                int iField = 0;
                while (iField < length) {
                    Field field = declaredFields2[iField];
                    String name = field.getName();
                    if (/*name == null ||*/ !name.equals("TRANSACTION_" + method)) {
                        iField++;
                    } else {
                        try {
                            field.setAccessible(true);
                            code = field.getInt(field);
                            break;
                        } catch (Exception e) {
                            Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
        }
        return code;
    }

    static String getServiceCommand(String serviceType, int transactionCode, Object... params) {
        if (params.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service").append(" ").append("call").append(" ").append(serviceType).append(" ").append(transactionCode);
            for (Object param : params) {
                if (param != null) {
                    stringBuilder.append(" ");
                    if (param instanceof Integer) {
                        stringBuilder.append("i32").append(" ").append(param);
                    } else if (param instanceof String) {
                        stringBuilder.append("s16").append(" ").append("'").append(((String) param).replace("'", "'\\''")).append("'");
                    }
                }
            }
            return stringBuilder.toString();
        }
        else
            return null;
    }

    static void commandWait(Command cmd) /*throws Exception*/ {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 milliseconds (3200 * 2 - 50)
        // 1.              50
        // 2. 2 * 50 =    100
        // 3. 2 * 100 =   200
        // 4. 2 * 200 =   400
        // 5. 2 * 400 =   800
        // 6. 2 * 800 =  1600
        // 7. 2 * 1600 = 3200
        // ------------------
        //               6350

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    //if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    //}
                } catch (InterruptedException e) {
                    Log.e("PPApplication.commandWait", Log.getStackTraceString(e));
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PPApplication.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForWifiScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.reregisterReceiversForWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartWifiScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.reregisterReceiversForBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartBluetoothScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartBluetoothScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartGeofenceScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartGeofenceScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_GEOFENCE_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_GEOFENCE_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void forceStartPhoneStateScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PhoneProfilesService.forceStartPhoneStateScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_PHONE_STATE_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_PHONE_STATE_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartPhoneStateScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartPhoneStateScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PHONE_STATE_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PHONE_STATE_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartTwilightScanner(Context context/*, boolean forScreenOn*/) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartAllScanners(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartAllScanners", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    public static void restartEvents(Context context, boolean unblockEventsRun, boolean reactivateProfile) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartEvents", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            commandIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            commandIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    /*
    static boolean isScreenOn(PowerManager powerManager) {
        //if (Build.VERSION.SDK_INT >= 20)
            return powerManager.isInteractive();
        //else
        //    return powerManager.isScreenOn();
    }
    */

    public static void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

    /*
    private static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
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
    */

    private static boolean isMIUI() {
        boolean miuiRom = false;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            miuiRom = prop.getProperty("ro.miui.ui.version.code", null) != null
                    || prop.getProperty("ro.miui.ui.version.name", null) != null
                    || prop.getProperty("ro.miui.internal.storage", null) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        PPApplication.logE("PPApplication.isMIUI", "miuiRom="+miuiRom);
        return miuiRom ||
                Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static String getEmuiRomName() {
        try {
            String line;
            //BufferedReader input = null;
            try {
                /*java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                input.close();*/
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                line = prop.getProperty("ro.build.version.emui", null);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }/* finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e("PPApplication.getSystemProperty", "Exception while closing InputStream", e);
                    }
                }
            }*/
            return line;
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isEMUI() {
        String emuiRomName = getEmuiRomName();
        PPApplication.logE("PPApplication.isEMUI", "emuiRomName="+emuiRomName);
        String romName = "";
        if (emuiRomName != null)
            romName = emuiRomName.toLowerCase();

        return (romName.indexOf("emotionui_") == 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3") || "EMUI 2.3".equalsIgnoreCase(emuiRomName) ||
                Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    private static boolean isLG() {
        //PPApplication.logE("PPApplication.isLG", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isLG", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isLG", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("lge") ||
                Build.MANUFACTURER.equalsIgnoreCase("lge") ||
                Build.FINGERPRINT.toLowerCase().contains("lge");
    }

    static boolean hasSystemFeature(Context context, String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    private static void _exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            PPApplication.logE("PPApplication._exitApp", "shutdown="+shutdown);
            // stop all events
            //if (removeAlarmClock)
            //    ApplicationPreferences.forceNotUseAlarmClock = true;

            if (dataWrapper != null)
                dataWrapper.stopAllEvents(false, false);

            if (!shutdown) {

                // remove notifications
                ImportantInfoNotification.removeNotification(context);
                Permissions.removeNotifications(context);

                if (dataWrapper != null)
                    dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_EXIT, null, null, null, 0);

                if (PPApplication.brightnessHandler != null) {
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeBrightnessView(context);

                        }
                    });
                }
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            ActivateProfileHelper.removeBrightnessView(context);

                        }
                    });
                }

                //PPApplication.initRoot();
            }

            if (dataWrapper != null) {
                if (!dataWrapper.profileListFilled)
                    dataWrapper.fillProfileList(false, false);
                for (Profile profile : dataWrapper.profileList)
                    ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);

                if (!dataWrapper.eventListFilled)
                    dataWrapper.fillEventList();
                for (Event event : dataWrapper.eventList)
                    StartEventNotificationBroadcastReceiver.removeAlarm(event, context);
            }
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
            Profile.setActivatedProfileForDuration(context, 0);
            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            PPApplication.logE("PPApplication._exitApp", "stop service");
            // maybe fixes ANR Context.startForegroundService() did not then call Service.startForeground
            //PhoneProfilesService.getInstance().showProfileNotification(false);
            //context.stopService(new Intent(context, PhoneProfilesService.class));
            PhoneProfilesService.stop(context);

            Permissions.setAllShowRequestPermissions(context.getApplicationContext(), true);

            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            //PhoneStateScanner.setShowEnableLocationNotification(context.getApplicationContext(), true);
            //ActivateProfileHelper.setScreenUnlocked(context, true);

            PPApplication.logE("PPApplication._exitApp", "set application started = false");
            PPApplication.setApplicationStarted(context, false);

            PPApplication.logE("ActivateProfileHelper.updateGUI", "from PPApplication._exitApp");
            ActivateProfileHelper.updateGUI(context, false, true);

            if (!shutdown) {
                Handler _handler = new Handler(context.getMainLooper());
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            if (activity != null)
                                activity.finish();
                        } catch (Exception ignored) {}
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }

        } catch (Exception e) {
            PPApplication.logE("PPApplication._exitApp", Log.getStackTraceString(e));
        }
    }

    static void exitApp(final boolean useHandler, final Context context, final DataWrapper dataWrapper, final Activity activity,
                                 final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            if (useHandler) {
                PPApplication.startHandlerThread("PPApplication.exitApp");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_exitApp");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPApplication.exitApp");

                            _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.exitApp");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
            else
                _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);
        } catch (Exception ignored) {

        }
    }

    static void showDoNotKillMyAppDialog(final Fragment fragment) {
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getActivity());
        dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);

        LayoutInflater inflater = fragment.getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_do_not_kill_my_app_dialog, null);
        dialogBuilder.setView(layout);

        DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
        if (doki != null) {
            doki.setButtonsVisibility(false);
            doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
        }

        dialogBuilder.show();
    }

    static void startHandlerThread(String from) {
        PPApplication.logE("PPApplication.startHandlerThread", "from="+from);
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread");
            handlerThread.start();
        }
    }

    static void startHandlerThreadPPService() {
        if (handlerThreadPPService == null) {
            handlerThreadPPService = new HandlerThread("PPHandlerThreadPPService");
            handlerThreadPPService.start();
        }
    }

    static void startHandlerThreadInternalChangeToFalse() {
        if (handlerThreadInternalChangeToFalse == null) {
            handlerThreadInternalChangeToFalse = new HandlerThread("PPHandlerThreadInternalChangeToFalse");
            handlerThreadInternalChangeToFalse.start();
        }
    }

    /*
    private static void startHandlerThreadRoot() {
        if (handlerThreadRoot == null) {
            handlerThreadRoot = new HandlerThread("PPHandlerThreadRoot");
            handlerThreadRoot.start();
        }
    }
    */

    static void startHandlerThreadWidget() {
        if (handlerThreadWidget == null) {
            handlerThreadWidget = new HandlerThread("PPHandlerThreadWidget");
            handlerThreadWidget.start();
        }
    }

    static void startHandlerThreadProfileNotification() {
        if (handlerThreadProfileNotification == null) {
            handlerThreadProfileNotification = new HandlerThread("PPHandlerThreadProfileNotification");
            handlerThreadProfileNotification.start();
        }
    }

    static void startHandlerThreadPlayTone() {
        if (handlerThreadPlayTone == null) {
            handlerThreadPlayTone = new HandlerThread("PPHandlerThreadPlayTone");
            handlerThreadPlayTone.start();
        }
    }

    static void startHandlerThreadVolumes() {
        if (handlerThreadVolumes == null) {
            handlerThreadVolumes = new HandlerThread("handlerThreadVolumes");
            handlerThreadVolumes.start();
        }
    }

    static void startHandlerThreadRadios() {
        if (handlerThreadRadios == null) {
            handlerThreadRadios = new HandlerThread("handlerThreadRadios");
            handlerThreadRadios.start();
        }
    }

    static void startHandlerThreadAdaptiveBrightness() {
        if (handlerThreadAdaptiveBrightness == null) {
            handlerThreadAdaptiveBrightness = new HandlerThread("handlerThreadAdaptiveBrightness");
            handlerThreadAdaptiveBrightness.start();
        }
    }

    static void startHandlerThreadWallpaper() {
        if (handlerThreadWallpaper == null) {
            handlerThreadWallpaper = new HandlerThread("handlerThreadWallpaper");
            handlerThreadWallpaper.start();
        }
    }

    static void startHandlerThreadPowerSaveMode() {
        if (handlerThreadPowerSaveMode == null) {
            handlerThreadPowerSaveMode = new HandlerThread("handlerThreadPowerSaveMode");
            handlerThreadPowerSaveMode.start();
        }
    }

    static void startHandlerThreadLockDevice() {
        if (handlerThreadLockDevice == null) {
            handlerThreadLockDevice = new HandlerThread("handlerThreadLockDevice");
            handlerThreadLockDevice.start();
        }
    }

    static void startHandlerThreadRunApplication() {
        if (handlerThreadRunApplication == null) {
            handlerThreadRunApplication = new HandlerThread("handlerThreadRunApplication");
            handlerThreadRunApplication.start();
        }
    }

    static void startHandlerThreadHeadsUpNotifications() {
        if (handlerThreadHeadsUpNotifications == null) {
            handlerThreadHeadsUpNotifications = new HandlerThread("handlerThreadHeadsUpNotifications");
            handlerThreadHeadsUpNotifications.start();
        }
    }

    /*
    static void startHandlerThreadMobileCells() {
        if (handlerThreadMobileCells == null) {
            handlerThreadMobileCells = new HandlerThread("handlerThreadMobileCells");
            handlerThreadMobileCells.start();
        }
    }
    */

    static void startHandlerThreadRestartEventsWithDelay() {
        if (handlerThreadRestartEventsWithDelay == null) {
            handlerThreadRestartEventsWithDelay = new HandlerThread("handlerThreadRestartEventsWithDelay");
            handlerThreadRestartEventsWithDelay.start();
            restartEventsWithDelayHandler = new Handler(PPApplication.handlerThreadRestartEventsWithDelay.getLooper());
        }
    }

    static void startHandlerThreadBluetoothConnectedDevices() {
        if (handlerThreadBluetoothConnectedDevices == null) {
            handlerThreadBluetoothConnectedDevices = new HandlerThread("handlerThreadBluetoothConnectedDevices");
            handlerThreadBluetoothConnectedDevices.start();
        }
    }

    static void startHandlerThreadNotificationLed() {
        if (handlerThreadNotificationLed == null) {
            handlerThreadNotificationLed = new HandlerThread("handlerThreadNotificationLed");
            handlerThreadNotificationLed.start();
        }
    }

    static void setBlockProfileEventActions(boolean enable) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            PPApplication.startHandlerThread("PPApplication.setBlockProfileEventActions");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PPApplication.setBlockProfileEventActions");

                    PPApplication.logE("PPApplication.setBlockProfileEventActions", "delayed boot up");
                    PPApplication.blockProfileEventActions = false;

                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.setBlockProfileEventActions");
                }
            }, 30000);
        }
    }

    // Google Analytics ----------------------------------------------------------------------------

    /*
    static void logAnalyticsEvent(String itemId, String itemName, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    */

    //---------------------------------------------------------------------------------------------

}
