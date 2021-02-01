package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.rootshell.RootShell;
import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.doubledot.doki.views.DokiContentView;
import me.drakeet.support.toast.ToastCompat;

import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

//import org.acra.annotation.*;
//import org.acra.config.ToastConfigurationBuilder;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;

@SuppressWarnings("WeakerAccess")
public class PPApplication extends Application
                                        //implements Configuration.Provider
                                        //implements Application.ActivityLifecycleCallbacks
{
    //static final int VERSION_CODE_EXTENDER_1_0_4 = 60;
    //static final int VERSION_CODE_EXTENDER_2_0 = 100;
    static final int VERSION_CODE_EXTENDER_3_0 = 200;
    static final int VERSION_CODE_EXTENDER_4_0 = 400;
    //static final int VERSION_CODE_EXTENDER_5_1_2 = 465;
    static final int VERSION_CODE_EXTENDER_5_1_3_1 = 540;
    //static final int VERSION_CODE_EXTENDER_5_1_3_5 = 580;
    static final int VERSION_CODE_EXTENDER_5_1_4 = 590;
    static final int VERSION_CODE_EXTENDER_LATEST = VERSION_CODE_EXTENDER_5_1_4;

    static final int pid = Process.myPid();
    static final int uid = Process.myUid();

    private static PPApplication instance;
    private static WorkManager workManagerInstance;

    static boolean applicationFullyStarted = false;

    // this for display of alert dialog when works not started at start of app
    static long startTimeOfApplicationStart = 0;

    static final long APPLICATION_START_DELAY = 2 * 60 * 1000;
    static final int WORK_PRUNE_DELAY_DAYS = 1;
    static final int WORK_PRUNE_DELAY_MINUTES = 60;

    // urls
    static final String CROWDIN_URL = "https://crowdin.com/project/phoneprofilesplus";
    static final String PRIVACY_POLICY_URL = "https://sites.google.com/site/phoneprofilesplus/home/privacy-policy";
    static final String GITHUB_PPP_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlus/releases";
    static final String GITHUB_PPP_URL = "https://github.com/henrichg/PhoneProfilesPlus";
    static final String GITHUB_PPPE_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender";
    static final String XDA_DEVELOPERS_PPP_URL = "https://forum.xda-developers.com/android/apps-games/phone-profile-plus-t3799429";
    static final String PPP_RLEASES_DEBUG_URL = "https://sites.google.com/site/phoneprofilesplus/releases-debug";
    static final String PPP_RLEASES_URL = "https://sites.google.com/site/phoneprofilesplus/releases";
    static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U";
    static final String GITHUB_PPPE_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";



    //static final boolean gitHubRelease = true;
    //static boolean googlePlayInstaller = false;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && DebugVersion.enabled;
    static final boolean logIntoFile = true;
    //TODO change it back to not log crash for releases
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = true && DebugVersion.enabled;
    private static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                                //+"|PPApplication.isXiaomi"
                                                //+"|PPApplication.isHuawei"
                                                //+"|PPApplication.isSamsung"
                                                //+"|PPApplication.isLG"
                                                //+"|PPApplication.getEmuiRomName"
                                                //+"|PPApplication.isEMUIROM"
                                                //+"|PPApplication.isMIUIROM"
                                                +"|PPApplication.exitApp"
                                                +"|PPApplication._exitApp"
                                                //+"|PPApplication.createProfileNotificationChannel"
                                                //+"|AvoidRescheduleReceiverWorker"
                                                +"|PhoneProfilesService.onCreate"
                                                +"|PhoneProfilesService.onStartCommand"
                                                +"|PhoneProfilesService.doForFirstStart"
                                                +"|PhoneProfilesService.doForPackageReplaced"
                                                +"|MainWorker.doAfterFirstStart"
                                                //+"|PhoneProfilesService.getServiceInfo"
                                                //+"|PhoneProfilesService.isServiceRunning"
                                                +"|PackageReplacedReceiver.onReceive"
                                                //+"|PhoneProfilesService.doCommand"
                                                //+"|PhoneProfilesService.showProfileNotification"
                                                //+"|PhoneProfilesService._showProfileNotification"
                                                //+"|ShowProfileNotificationBroadcastReceiver"
                                                //+"|PhoneProfilesService._showProfileNotification"
                                                //+"|[CUST] PhoneProfilesService._showProfileNotification"
                                                //+"|PhoneProfilesService.onConfigurationChanged"
                                                //+"|PhoneProfilesService.stopReceiver"
                                                //+"|PhoneProfilesService.onTaskRemoved"
                                                +"|PhoneProfilesService.onDestroy"
                                                //+"|PhoneProfilesService.cancelWork"
                                                +"|DataWrapper.firstStartEvents"
                                                //+"|DataWrapper.setProfileActive"
                                                //+"|DataWrapper.activateProfileOnBoot"
                                                +"|BootUpReceiver"
                                                //+"|PhoneProfilesBackupAgent"
                                                +"|ShutdownBroadcastReceiver"
                                                +"|DatabaseHandler.onUpgrade"
                                                //+"|IgnoreBatteryOptimizationNotification"
                                                +"|LauncherActivity.startPPServiceWhenNotStarted"

                                                //+"|DatabaseHandler.onCreate"
                                                //+"|DatabaseHandler.createTableColumsWhenNotExists"

                                                //+"|%%%%% PhoneStateScanner.doAutoRegistration"
                                                //+"|CmdWifiAP.isEnabled"
                                                //+"|CmdWifiAP.setWifiAP"
                                                //+"|WifiApManager.setWifiApState"

                                                //+"|[***] EventsHandler.doHandleEvents"
                                                //+"|[***] Event.startEvent"
                                                //+"|%%%% BluetoothScanner.doScan"
                                                //+"|%%%%BLE BluetoothScanner.doScan"
                                                //+"|%%%%BLE BluetoothScanner.waitForLEBluetoothScanEnd"
                                                //+"|%%%%BLE BluetoothScanWorker.startLEScan"
                                                //+"|%%%%BLE BluetoothScanWorker.stopLEScan"


//                                                +"|[IN_WORKER]"
//                                                +"|[WORKER_CALL]"
//                                                +"|[IN_THREAD_HANDLER]"
//                                                +"|[IN_BROADCAST]"
//                                                +"|[LOCAL_BROADCAST_CALL]"
//                                                +"|[IN_OBSERVER]"
//                                                +"|[IN_LISTENER]"
//                                                +"|[IN_EVENTS_HANDLER]"
//                                                +"|[EVENTS_HANDLER_CALL]"

//                                                +"|[TEST BATTERY]"

                                                //+"|[APP START]"
                                                //+"|[SHEDULE_WORK]"
                                                //+"|[SHEDULE_SCANNER]"

                                                //+"|[TEST MEDIA VOLUME]"
                                                //+"|[TEST_BLOCK_PROFILE_EVENTS_ACTIONS]"

                                                //+"|[FIFO_TEST]"
                                                //+"|[BLOCK_ACTIONS]"

                                                //+"|[ACTIVATOR]"
                                                //+"|[G1_TEST]"

                                                //+"|[BACKGROUND_ACTIVITY]"

                                                //+"|PhoneProfilesService.registerReceiverForCalendarSensor"
                                                //+"|EventPreferencesCalendar"
                                                //+"|CalendarEventExistsCheckBroadcastReceiver"
                                                //+"|ActivateProfileHelper.setScreenTimeout"
                                                //+"|ContactGroupsCache"
                                                //+"|EventsPrefsFragment"
                                                ;

    static final int ACTIVATED_PROFILES_FIFO_SIZE = 20;

    // activity log types
    static final int ALTYPE_UNDEFINED = 0;

    static final int ALTYPE_PROFILE_ACTIVATION = 1;
    static final int ALTYPE_APPLICATION_EXIT = 10;
    static final int ALTYPE_DATA_IMPORT = 11;
    static final int ALTYPE_PAUSED_LOGGING = 12;
    static final int ALTYPE_STARTED_LOGGING = 13;
    static final int ALTYPE_EVENT_END_DELAY = 14;
    static final int ALTYPE_EVENT_STOP = 15;
    static final int ALTYPE_APPLICATION_START_ON_BOOT = 16;
    static final int ALTYPE_EVENT_PREFERENCES_CHANGED = 17;
    static final int ALTYPE_EVENT_DELETED = 18;
    static final int ALTYPE_PROFILE_DELETED = 19;

    static final int ALTYPE_MERGED_PROFILE_ACTIVATION = 2;
    static final int ALTYPE_MANUAL_RESTART_EVENTS = 20;
    static final int ALTYPE_AFTER_DURATION_UNDO_PROFILE = 21;
    static final int ALTYPE_AFTER_DURATION_DEFAULT_PROFILE = 22;
    static final int ALTYPE_AFTER_DURATION_RESTART_EVENTS = 23;

    static final int ALTYPE_EVENT_START = 3;
    static final int ALTYPE_PROFILE_PREFERENCES_CHANGED = 30;
    static final int ALTYPE_SHARED_PROFILE_PREFERENCES_CHANGED = 31;
    static final int ALTYPE_ALL_EVENTS_DELETED = 32;
    static final int ALTYPE_ALL_PROFILES_DELETED = 33;
    static final int ALTYPE_APPLICATION_UPGRADE = 34;
    static final int ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE = 35;

    static final int ALTYPE_EVENT_START_DELAY = 4;

    static final int ALTYPE_EVENT_END_NONE = 51;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE = 52;
    static final int ALTYPE_EVENT_END_UNDO_PROFILE = 53;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE = 54;
    static final int ALTYPE_EVENT_END_RESTART_EVENTS = 55;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS = 56;

    static final int ALTYPE_RESTART_EVENTS = 6;
    static final int ALTYPE_RUN_EVENTS_DISABLE = 7;
    static final int ALTYPE_RUN_EVENTS_ENABLE = 8;
    static final int ALTYPE_APPLICATION_START = 9;

    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION = 1000;
    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT = 1001;
    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT = 1002;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE = 1003;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION = 1004;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_ALARM = 1005;
    static final int ALTYPE_PROFILE_ERROR_SET_WALLPAPER = 1006;

    static final int ALTYPE_DATA_IMPORT_FROM_PP = 100;
    static final int ALTYPE_DATA_EXPORT = 101;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION = 102;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS = 103;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT = 104;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT = 105;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT = 106;
    static final int ALTYPE_APPLICATION_SYSTEM_RESTART = 107;

    static boolean doNotShowProfileNotification = false;
    private static boolean applicationStarted = false;
    static boolean globalEventsRunStop = true;
    //static boolean applicationPackageReplaced = false;
    static boolean deviceBoot = false;

    static final boolean restoreFinished = true;

    static Collator collator = null;

    static boolean lockRefresh = false;
    //static long lastRefreshOfGUI = 0;
    //static long lastRefreshOfProfileNotification = 0;

    //static final int DURATION_FOR_GUI_REFRESH = 500;
    //static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    //static final String EXTRA_REFRESH = "refresh";

    static final List<String> elapsedAlarmsProfileDurationWork = new ArrayList<>();
    static final List<String> elapsedAlarmsRunApplicationWithDelayWork = new ArrayList<>();
    static final List<String> elapsedAlarmsEventDelayStartWork = new ArrayList<>();
    static final List<String> elapsedAlarmsEventDelayEndWork = new ArrayList<>();
    static final List<String> elapsedAlarmsStartEventNotificationWork = new ArrayList<>();

    static final ApplicationPreferencesMutex applicationPreferencesMutex = new ApplicationPreferencesMutex();
    static final ApplicationGlobalPreferencesMutex applicationGlobalPreferencesMutex = new ApplicationGlobalPreferencesMutex();
    private static final ApplicationStartedMutex applicationStartedMutex = new ApplicationStartedMutex();
    static final ProfileActivationMutex profileActivationMutex = new ProfileActivationMutex();
    static final GlobalEventsRunStopMutex globalEventsRunStopMutex = new GlobalEventsRunStopMutex();
    static final EventsRunMutex eventsRunMutex = new EventsRunMutex();
    static final EventCallSensorMutex eventCallSensorMutex = new EventCallSensorMutex();
    static final EventAccessoriesSensorMutex EVENT_ACCESSORIES_SENSOR_MUTEX = new EventAccessoriesSensorMutex();
    static final EventWifiSensorMutex eventWifiSensorMutex = new EventWifiSensorMutex();
    static final EventBluetoothSensorMutex eventBluetoothSensorMutex = new EventBluetoothSensorMutex();
    static final ContactsCacheMutex contactsCacheMutex = new ContactsCacheMutex();
    static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    static final RootMutex rootMutex = new RootMutex();
    private static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    //static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    //static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    static final GeofenceScannerLastLocationMutex geofenceScannerLastLocationMutex = new GeofenceScannerLastLocationMutex();
    static final GeofenceScannerMutex geofenceScannerMutex = new GeofenceScannerMutex();
    static final WifiScannerMutex wifiScannerMutex = new WifiScannerMutex();
    static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    static final BluetoothScannerMutex bluetoothScannerMutex = new BluetoothScannerMutex();
    static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    static final BluetoothCLScanMutex bluetoothCLScanMutex = new BluetoothCLScanMutex();
    static final BluetoothLEScanMutex bluetoothLEScanMutex = new BluetoothLEScanMutex();
    static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    static final PhoneStateScannerMutex phoneStateScannerMutex = new PhoneStateScannerMutex();
    static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    static final TwilightScannerMutex twilightScannerMutex = new TwilightScannerMutex();
    static final NotUnlinkVolumesMutex notUnlinkVolumesMutex = new NotUnlinkVolumesMutex();

    //static PowerManager.WakeLock keepScreenOnWakeLock;

    //static final String romManufacturer = getROMManufacturer();
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();
    static final boolean deviceIsLG = isLG();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean deviceIsOppo = isOppo();
    static final boolean deviceIsRealme = isRealme();
    static final boolean romIsMIUI = isMIUIROM();
    static final boolean romIsEMUI = isEMUIROM();

    static boolean HAS_FEATURE_BLUETOOTH_LE = false;
    static boolean HAS_FEATURE_WIFI = false;
    static boolean HAS_FEATURE_BLUETOOTH = false;
    static boolean HAS_FEATURE_TELEPHONY = false;
    static boolean HAS_FEATURE_NFC = false;
    static boolean HAS_FEATURE_LOCATION = false;
    static boolean HAS_FEATURE_LOCATION_GPS = false;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";
    static final String PACKAGE_NAME_PP = "sk.henrichg.phoneprofiles";

    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_EVENT_STATUS = "event_status";
    static final String EXTRA_APPLICATION_START = "application_start";
    static final String EXTRA_DEVICE_BOOT = "device_boot";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_BOOT = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_SERVICE = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    //static final int STARTUP_SOURCE_LAUNCHER_START = 10;
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
    static final String NEW_RELEASE_CHANNEL = "phoneProfilesPlus_newRelease";
    //static final String CRASH_REPORT_NOTIFICATION_CHANNEL = "phoneProfilesPlus_crash_report";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_generatedByProfile";

    static final int PROFILE_NOTIFICATION_ID = 100;
    static final int PROFILE_NOTIFICATION_NATIVE_ID = 500;

    static final int IMPORTANT_INFO_NOTIFICATION_ID = 101;
    static final String IMPORTANT_INFO_NOTIFICATION_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION";
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 102;
    static final String GRANT_PROFILE_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PROFILE_PERMISSIONS_NOTIFICATION";
    //static final int GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_ID = 103;
    //static final String GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_INSTALL_TONE_PERMISSIONS_NOTIFICATION";
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 104;
    static final String GRANT_EVENT_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_EVENT_PERMISSIONS_NOTIFICATION";
    //static final int LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID = 105;
    //static final String LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_TAG = PACKAGE_NAME+"_LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION";
    //static final int LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID = 106;
    //static final String LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_TAG = PACKAGE_NAME+"_LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION";
    //static final int GEOFENCE_SCANNER_ERROR_NOTIFICATION_ID = 107;
    //static final String GEOFENCE_SCANNER_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_GEOFENCE_SCANNER_ERROR_NOTIFICATION";
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 108;
    static final String GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION";
    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 109;
    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 110;
    static final String ABOUT_APPLICATION_DONATE_NOTIFICATION_TAG = PACKAGE_NAME+"_ABOUT_APPLICATION_DONATE_NOTIFICATION";
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 111;
    static final String ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG = PACKAGE_NAME+"_ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION";
    //static final int EVENT_START_NOTIFICATION_ID = 112;
    //static final String EVENT_START_NOTIFICATION_TAG = PACKAGE_NAME+"_EVENT_START_NOTIFICATION";
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 113;
    static final String PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 114;
    static final String PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 115;
    static final String PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 116;
    static final String PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION";
    static final int MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID = 117;
    static final String MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG = PACKAGE_NAME+"_MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION";
    //static final int GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_ID = 118;
    //static final String GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_LOG_TO_FILE_PERMISSIONS_NOTIFICATION";
    //static final int LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID = 119;
    //static final String LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_TAG = PACKAGE_NAME+"_LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION";
    static final int IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID = 120;
    static final String IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG = PACKAGE_NAME+"_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION";
    static final int DRAW_OVER_APPS_NOTIFICATION_ID = 121;
    static final String DRAW_OVER_APPS_NOTIFICATION_TAG = PACKAGE_NAME+"_DRAW_OVER_APPS_NOTIFICATION";
    static final int CHECK_GITHUB_RELEASES_NOTIFICATION_ID = 122;
    static final String CHECK_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_GITHUB_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID = 124;
    static final String CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG";

    static final int PROFILE_ACTIVATION_ERROR_NOTIFICATION_ID = 130;
    static final String PROFILE_ACTIVATION_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_ID = 131;
    static final String PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_ID = 132;
    static final String PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_ID = 133;
    static final String PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION";

    // notifications have also tag, in it is tag name + profile/event/mobile cells id
    static final int PROFILE_ID_NOTIFICATION_ID = 1000;
    static final int EVENT_ID_NOTIFICATION_ID = 1000;
    static final int NOTIFY_EVENT_START_NOTIFICATION_ID = 1000;
    static final int NEW_MOBILE_CELLS_NOTIFICATION_ID = 1000;

    static final int GENERATED_BY_PROFILE_NOTIFICATION_ID = 10000;

    static final String DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION";
    static final String DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION";
    static final String NOTIFY_EVENT_START_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NOTIFY_EVENT_START_NOTIFICATION";
    static final String NEW_MOBILE_CELLS_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NEW_MOBILE_CELLS_NOTIFICATION";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_TAG = PACKAGE_NAME+"_GENERATED_BY_PROFILE_NOTIFICATION_TAG";

    // shared preferences names !!! Configure also in res/xml/phoneprofiles_backup_scheme.xml !!!
    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    //static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    //static final String ACTIVATED_PROFILE_PREFS_NAME = "profile_preferences_activated_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    //static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";
    static final String ACTIVATED_PROFILES_FIFO_PREFS_NAME = "activated_profiles_fifo";

    //public static final String RESCAN_TYPE_SCREEN_ON = "1";
    //public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";
    private static final String PREF_DONATION_DONATED = "donation_donated";
    //private static final String PREF_NOTIFICATION_PROFILE_NAME = "notification_profile_name";
    //private static final String PREF_WIDGET_PROFILE_NAME = "widget_profile_name";
    //private static final String PREF_ACTIVITY_PROFILE_NAME = "activity_profile_name";
    private static final String PREF_LAST_ACTIVATED_PROFILE = "last_activated_profile";

    // WorkManager tags
    static final String AFTER_FIRST_START_WORK_TAG = "afterFirstStartWork";
    static final String PACKAGE_REPLACED_WORK_TAG = "packageReplacedWork";
    static final String AVOID_RESCHEDULE_RECEIVER_WORK_TAG = "avoidRescheduleReceiverWorker";

    // scanner start/stop types
    //static final int SCANNER_START_GEOFENCE_SCANNER = 1;
    //static final int SCANNER_STOP_GEOFENCE_SCANNER = 2;
    static final int SCANNER_RESTART_GEOFENCE_SCANNER = 3;

    //static final int SCANNER_START_ORIENTATION_SCANNER = 4;
    //static final int SCANNER_STOP_ORIENTATION_SCANNER = 5;
    static final int SCANNER_FORCE_START_ORIENTATION_SCANNER = 5;
    static final int SCANNER_RESTART_ORIENTATION_SCANNER = 6;

    //static final int SCANNER_START_PHONE_STATE_SCANNER = 7;
    //static final int SCANNER_STOP_PHONE_STATE_SCANNER = 8;
    static final int SCANNER_FORCE_START_PHONE_STATE_SCANNER = 9;
    static final int SCANNER_RESTART_PHONE_STATE_SCANNER = 10;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 11;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 12;
    static final int SCANNER_RESTART_WIFI_SCANNER = 13;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 14;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 15;
    static final int SCANNER_RESTART_BLUETOOTH_SCANNER = 16;

    //static final int SCANNER_START_TWILIGHT_SCANNER = 17;
    //static final int SCANNER_STOP_TWILIGHT_SCANNER = 18;
    static final int SCANNER_RESTART_TWILIGHT_SCANNER = 19;
    static final int SCANNER_RESTART_BACKGROUND_SCANNING_SCANNER = 20;
    static final int SCANNER_RESTART_NOTIFICATION_SCANNER = 21;

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
    static final String PPP_EXTENDER_PERMISSION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACCESSIBILITY_SERVICE_PERMISSION";

    //static final String ACTION_SHOW_PROFILE_NOTIFICATION = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION";
    //static final String ACTION_UPDATE_GUI = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_UPDATE_GUI";
    static final String ACTION_DONATION = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_DONATION";
    static final String ACTION_CHECK_GITHUB_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_GITHUB_RELEASES";
    static final String ACTION_CHECK_CRITICAL_GITHUB_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES";
    static final String ACTION_FINISH_ACTIVITY = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_FINISH_ACTIVITY";
    static final String EXTRA_WHAT_FINISH = "what_finish";

    static final String ACTION_EXPORT_PP_DATA_START_FROM_PPP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_START_FROM_PPP";
    static final String ACTION_EXPORT_PP_DATA_STOP_FROM_PPP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STOP_FROM_PPP";
    static final String ACTION_EXPORT_PP_DATA_STOP_FROM_PP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STOP_FROM_PP";
    static final String ACTION_EXPORT_PP_DATA_STARTED = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STARTED";
    static final String ACTION_EXPORT_PP_DATA_ENDED = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_ENDED";
    static final String ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES";
    //static final String ACTION_EXPORT_PP_DATA_PROFILES_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_PROFILES_COUNT";
    static final String ACTION_EXPORT_PP_DATA_PROFILES = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_PROFILES";
    //static final String ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT";
    static final String ACTION_EXPORT_PP_DATA_SHORTCUTS = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_SHORTCUTS";
    //static final String ACTION_EXPORT_PP_DATA_INTENTS_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_INTENTS_COUNT";
    static final String ACTION_EXPORT_PP_DATA_INTENTS = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_INTENTS";
    static final String EXTRA_PP_APPLICATION_DATA = "extra_pp_application_data";
    //static final String EXTRA_PP_PROFILES_COUNT = "extra_pp_profiles_count";
    static final String EXTRA_PP_PROFILE_DATA = "extra_pp_profile_data";
    //static final String EXTRA_PP_SHORTCUTS_COUNT = "extra_pp_shortcuts_count";
    static final String EXTRA_PP_SHORTCUT_DATA = "extra_pp_shortcut_data";
    //static final String EXTRA_PP_INTENTS_COUNT = "extra_pp_intents_count";
    static final String EXTRA_PP_INTENT_DATA = "extra_pp_intent_data";
    static final String EXPORT_PP_DATA_PERMISSION = PPApplication.PACKAGE_NAME_PP + ".EXPORT_PP_DATA_PERMISSION";


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
    static final String CRASHLYTICS_LOG_DEVICE_ROOTED_WITH = "ROOTED_WITH";
    static final String CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION = "GOOGLE_PLAY_SERVICES_VERSION";
    static final String CRASHLYTICS_LOG_RESTORE_BACKUP_OK = "RESTORE_BACKUP_OK";
    static final String CRASHLYTICS_LOG_IMPORT_FROM_PP_OK = "IMPORT_FROM_PP_OK";

    private static final String SYS_PROP_MOD_VERSION = "ro.modversion";

    //public static long lastUptimeTime;
    //public static long lastEpochTime;

    static KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    static KeyguardManager.KeyguardLock keyguardLock = null;

    //BrightnessView brightnessView = null;
    //BrightnessView screenTimeoutAlwaysOnView = null;
    static BrightnessView keepScreenOnView = null;

    static LockDeviceActivity lockDeviceActivity = null;
    static int screenTimeoutBeforeDeviceLock = 0;

    //boolean willBeDoRestartEvents = false;

    static final StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = new StartLauncherFromNotificationReceiver();
    //static final UpdateGUIBroadcastReceiver updateGUIBroadcastReceiver = new UpdateGUIBroadcastReceiver();
    //static final ShowProfileNotificationBroadcastReceiver showProfileNotificationBroadcastReceiver = new ShowProfileNotificationBroadcastReceiver();
    static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();
    static final IconWidgetProvider iconWidgetBroadcastReceiver = new IconWidgetProvider();
    static final OneRowWidgetProvider oneRowWidgetBroadcastReceiver = new OneRowWidgetProvider();
    static final ProfileListWidgetProvider listWidgetBroadcastReceiver = new ProfileListWidgetProvider();
    static final SamsungEdgeProvider edgePanelBroadcastReceiver = new SamsungEdgeProvider();

    static TimeChangedReceiver timeChangedReceiver = null;
    static PermissionsNotificationDeletedReceiver permissionsNotificationDeletedReceiver = null;
    static StartEventNotificationDeletedReceiver startEventNotificationDeletedReceiver = null;
    static NotUsedMobileCellsNotificationDeletedReceiver notUsedMobileCellsNotificationDeletedReceiver = null;
    static ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    static ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    static InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    static PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    static RingerModeChangeReceiver ringerModeChangeReceiver = null;
    static WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;
    static NotUsedMobileCellsNotificationDisableReceiver notUsedMobileCellsNotificationDisableReceiver = null;
    static DonationBroadcastReceiver donationBroadcastReceiver = null;
    static CheckGitHubReleasesBroadcastReceiver checkGitHubReleasesBroadcastReceiver = null;
    static CheckCriticalGitHubReleasesBroadcastReceiver checkCriticalGitHubReleasesBroadcastReceiver = null;
    //static StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = null;
    static CheckOnlineStatusBroadcastReceiver checkOnlineStatusBroadcastReceiver = null;

    static BatteryChargingChangedBroadcastReceiver batteryChargingChangedReceiver = null;
    static BatteryLevelChangedBroadcastReceiver batteryLevelChangedReceiver = null;
    static HeadsetConnectionBroadcastReceiver headsetPlugReceiver = null;
    static NFCStateChangedBroadcastReceiver nfcStateChangedBroadcastReceiver = null;
    static DockConnectionBroadcastReceiver dockConnectionBroadcastReceiver = null;
    //static WifiConnectionBroadcastReceiver wifiConnectionBroadcastReceiver = null;
    static WifiNetworkCallback wifiConnectionCallback = null;
    static BluetoothConnectionBroadcastReceiver bluetoothConnectionBroadcastReceiver = null;
    static BluetoothStateChangedBroadcastReceiver bluetoothStateChangedBroadcastReceiver = null;
    static WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    static LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    static AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    //static SMSBroadcastReceiver smsBroadcastReceiver = null;
    //static SMSBroadcastReceiver mmsBroadcastReceiver = null;
    static CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;
    static WifiScanBroadcastReceiver wifiScanReceiver = null;
    static BluetoothScanBroadcastReceiver bluetoothScanReceiver = null;
    static BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = null;
    static PPPExtenderBroadcastReceiver pppExtenderBroadcastReceiver = null;
    static PPPExtenderBroadcastReceiver pppExtenderForceStopApplicationBroadcastReceiver = null;
    static PPPExtenderBroadcastReceiver pppExtenderForegroundApplicationBroadcastReceiver = null;
    static PPPExtenderBroadcastReceiver pppExtenderSMSBroadcastReceiver = null;
    static PPPExtenderBroadcastReceiver pppExtenderCallBroadcastReceiver = null;
    static EventTimeBroadcastReceiver eventTimeBroadcastReceiver = null;
    static EventCalendarBroadcastReceiver eventCalendarBroadcastReceiver = null;
    static EventDelayStartBroadcastReceiver eventDelayStartBroadcastReceiver = null;
    static EventDelayEndBroadcastReceiver eventDelayEndBroadcastReceiver = null;
    static ProfileDurationAlarmBroadcastReceiver profileDurationAlarmBroadcastReceiver = null;
    static SMSEventEndBroadcastReceiver smsEventEndBroadcastReceiver = null;
    static NFCEventEndBroadcastReceiver nfcEventEndBroadcastReceiver = null;
    static RunApplicationWithDelayBroadcastReceiver runApplicationWithDelayBroadcastReceiver = null;
    static MissedCallEventEndBroadcastReceiver missedCallEventEndBroadcastReceiver = null;
    static StartEventNotificationBroadcastReceiver startEventNotificationBroadcastReceiver = null;
    static GeofencesScannerSwitchGPSBroadcastReceiver geofencesScannerSwitchGPSBroadcastReceiver = null;
    static LockDeviceActivityFinishBroadcastReceiver lockDeviceActivityFinishBroadcastReceiver = null;
    static AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    static AlarmClockEventEndBroadcastReceiver alarmClockEventEndBroadcastReceiver = null;
    static NotificationEventEndBroadcastReceiver notificationEventEndBroadcastReceiver = null;
    static LockDeviceAfterScreenOffBroadcastReceiver lockDeviceAfterScreenOffBroadcastReceiver = null;
    //static OrientationEventBroadcastReceiver orientationEventBroadcastReceiver = null;
    static PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    static DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;
    static DeviceBootEventEndBroadcastReceiver deviceBootEventEndBroadcastReceiver = null;
    static CalendarEventExistsCheckBroadcastReceiver calendarEventExistsCheckBroadcastReceiver = null;

    static SettingsContentObserver settingsContentObserver = null;
    static MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;
    static ContactsContentObserver contactsContentObserver = null;

    static SensorManager sensorManager = null;
    static Sensor accelerometerSensor = null;
    static Sensor magneticFieldSensor = null;
    static Sensor lightSensor = null;
    static Sensor proximitySensor = null;

    static OrientationScanner orientationScanner = null;
    static boolean mStartedOrientationSensors = false;
    static GeofencesScanner geofencesScanner = null;
    static PhoneStateScanner phoneStateScanner = null;
    static TwilightScanner twilightScanner = null;

    static boolean notificationScannerRunning = false;

    static boolean isCharging = false;
    static int batteryPct = -100;
    static int plugged = -1;

    public static boolean isScreenOn;
    //public static boolean isPowerSaveMode;

    static Location lastLocation = null;

    public static HandlerThread handlerThread = null;
    public static HandlerThread handlerThreadCancelWork = null;
    public static HandlerThread handlerThreadBroadcast = null;
    public static HandlerThread handlerThreadWidget = null;
    public static HandlerThread handlerThreadPlayTone = null;
    public static HandlerThread handlerThreadPPScanners = null;
    public static OrientationScannerHandlerThread handlerThreadOrientationScanner = null;
    public static HandlerThread handlerThreadPPCommand = null;

    public static HandlerThread handlerThreadVolumes = null;
    public static HandlerThread handlerThreadRadios = null;
    public static HandlerThread handlerThreadWallpaper = null;
    public static HandlerThread handlerThreadRunApplication = null;

    public static HandlerThread handlerThreadProfileActivation = null;

    public static Handler toastHandler;
    //public static Handler brightnessHandler;
    public static Handler screenTimeoutHandler;

    public static final PPNotificationListenerService ppNotificationListenerService = new PPNotificationListenerService();

    //public static boolean isPowerSaveMode = false;

    // !! this must be here
    public static boolean blockProfileEventActions = false;

    // Samsung Look instance
    public static Slook sLook = null;
    public static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    //public static final Random requestCodeForAlarm = new Random();


    @Override
    public void onCreate()
    {
        /* Hm this resets start, why?!
        if (DebugVersion.enabled) {
            PPApplication.logE("##### PPApplication.onCreate", "strict mode");

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    //.detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }*/

        super.onCreate();

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.KEY_VALUE_LIST);
        /*builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
                .setResText(R.string.acra_toast_text)
                .setEnabled(true);*/
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                .setResChannelName(R.string.notification_channel_crash_report)
                .setResChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .setResIcon(R.drawable.ic_exclamation_notify)
                .setResTitle(R.string.acra_notification_title)
                .setResText(R.string.acra_notification_text)
                .setResSendButtonText(R.string.acra_notification_send_button)
                .setResDiscardButtonText(R.string.acra_notification_discard_button)
                .setSendOnClick(true)
                .setEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .setMailTo("henrich.gron@gmail.com")
                .setResSubject(R.string.acra_email_subject_text)
                .setResBody(R.string.acra_email_body_text)
                .setReportAsFile(true)
                .setReportFileName("crash_report.txt")
                .setEnabled(true);

        ACRA.init(this, builder);

        // don't schedule anything in crash reporter process
        if (ACRA.isACRASenderServiceProcess())
            return;

        applicationFullyStarted = false;
        instance = this;

        PPApplication.logE("##### PPApplication.onCreate", "xxx");

        //if (DebugVersion.enabled) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {}
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

        //registerActivityLifecycleCallbacks(PPApplication.this);

        /*try {
            //if (!DebugVersion.enabled) {
            // Obtain the FirebaseAnalytics instance.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            //}
            //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } catch (Exception e) {
            Log.e("PPPEApplication.onCreate", Log.getStackTraceString(e));
        }*/

        if (checkAppReplacingState()) {
            PPApplication.logE("##### PPApplication.onCreate", "kill PPApplication - not good");
            return;
        }

        PPApplication.logE("##### PPApplication.onCreate", "continue onCreate()");

        PackageManager packageManager = getPackageManager();
        HAS_FEATURE_BLUETOOTH_LE = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH_LE);
        HAS_FEATURE_WIFI = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_WIFI);
        HAS_FEATURE_BLUETOOTH = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH);
        HAS_FEATURE_TELEPHONY = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_TELEPHONY);
        HAS_FEATURE_NFC = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_NFC);
        HAS_FEATURE_LOCATION = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION);
        HAS_FEATURE_LOCATION_GPS = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION_GPS);

        PPApplication.logE("##### PPApplication.onCreate", "end of get features");

        loadGlobalApplicationData(getApplicationContext());
        loadApplicationPreferences(getApplicationContext());
        loadProfileActivationData(getApplicationContext());

        workManagerInstance = WorkManager.getInstance(getApplicationContext());
        PPApplication.logE("##### PPApplication.onCreate", "workManagerInstance="+workManagerInstance);

        /*
        workManagerInstance.pruneWork();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            int size = jobScheduler.getAllPendingJobs().size();
            PPApplication.logE("##### PPApplication.onCreate", "jobScheduler.getAllPendingJobs().size()="+size);
            jobScheduler.cancelAll();
        }
        */

        // https://issuetracker.google.com/issues/115575872#comment16
        PPApplication.logE("##### PPApplication.onCreate", "avoidRescheduleReceiverWorker START of enqueue");
        AvoidRescheduleReceiverWorker.enqueueWork();
        PPApplication.logE("##### PPApplication.onCreate", "avoidRescheduleReceiverWorker END of enqueue");

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = getAccelerometerSensor(getApplicationContext());
        magneticFieldSensor = getMagneticFieldSensor(getApplicationContext());
        proximitySensor = getProximitySensor(getApplicationContext());
        lightSensor = getLightSensor(getApplicationContext());

        if (lastLocation == null) {
            //PPApplication.logE("##### GeofenceScanner", "lastLocation update");
            lastLocation = new Location("GL");
        }

        if (logEnabled()) {
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsXiaomi=" + deviceIsXiaomi);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsHuawei=" + deviceIsHuawei);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsSamsung=" + deviceIsSamsung);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsLG=" + deviceIsLG);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOnePlus=" + deviceIsOnePlus);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOppo=" + deviceIsOppo);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsRealme=" + deviceIsRealme);

            PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI=" + romIsMIUI);
            PPApplication.logE("##### PPApplication.onCreate", "romIsEMUI=" + romIsEMUI);
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsEMUI=" + isEMUIROM());
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsMIUI=" + isMIUIROM());

            PPApplication.logE("##### PPApplication.onCreate", "manufacturer=" + Build.MANUFACTURER);
            PPApplication.logE("##### PPApplication.onCreate", "model=" + Build.MODEL);
            PPApplication.logE("##### PPApplication.onCreate", "display=" + Build.DISPLAY);
            PPApplication.logE("##### PPApplication.onCreate", "brand=" + Build.BRAND);
            PPApplication.logE("##### PPApplication.onCreate", "fingerprint=" + Build.FINGERPRINT);
            PPApplication.logE("##### PPApplication.onCreate", "type=" + Build.TYPE);

            PPApplication.logE("##### PPApplication.onCreate", "modVersion=" + getReadableModVersion());
            PPApplication.logE("##### PPApplication.onCreate", "osVersion=" + System.getProperty("os.version"));

            PPApplication.logE("##### PPApplication.onCreate", "deviceName="+ Settings.System.getString(getContentResolver(), "device_name"));
            PPApplication.logE("##### PPApplication.onCreate", "release="+ Build.VERSION.RELEASE);

            PPApplication.logE("##### PPApplication.onCreate", "board="+ Build.BOARD);
            PPApplication.logE("##### PPApplication.onCreate", "product="+ Build.PRODUCT);
        }

        // Fix for FC: java.lang.IllegalArgumentException: register too many Broadcast Receivers
        //LoadedApkHuaWei.hookHuaWeiVerifier(this);

        /*
        if (logIntoFile || crashIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());
        */

        ////////////////////////////////////////////////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // !!! WARNING - this is required also for android.jar from android-hidden-api !!!
        // https://github.com/tiann/FreeReflection
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                if (getRuntime != null) {
                    Object vmRuntime = getRuntime.invoke(null);
                    if (setHiddenApiExemptions != null)
                        setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
                }
            } catch (Exception e) {
                //Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
        //////////////////////////////////////////

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                //Crashlytics.getInstance().core.logException(error);
                PPApplication.recordException(error);
            }
        });
        anrWatchDog.start();
        */

        PPApplication.setCustomKey("FROM_GOOGLE_PLAY", false);
        PPApplication.setCustomKey("DEBUG", DebugVersion.enabled);

        //lastUptimeTime = SystemClock.elapsedRealtime();
        //lastEpochTime = System.currentTimeMillis();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = false;
        /*DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null)
            isScreenOn = false;
        else {
            Display[] displays = displayManager.getDisplays();
            if ((displays == null) || (displays.length == 0))
                isScreenOn = false;
            else {
                int state = displays[0].getState();
                if ((state == Display.STATE_ON) || (state == Display.STATE_ON_SUSPEND))
                    isScreenOn = true;
            }
        }*/

        //isPowerSaveMode = DataWrapper.isPowerSaveMode(getApplicationContext());

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        startHandlerThread(/*"PPApplication.onCreate"*/);
        startHandlerThreadCancelWork();
        startHandlerThreadBroadcast();
        startHandlerThreadPPScanners(); // for minutes interval
        startHandlerThreadOrientationScanner(); // for seconds interval
        startHandlerThreadPPCommand();
        startHandlerThreadWidget();
        startHandlerThreadPlayTone();
        startHandlerThreadVolumes();
        startHandlerThreadRadios();
        startHandlerThreadWallpaper();
        startHandlerThreadRunApplication();
        startHandlerThreadProfileActivation();

        toastHandler = new Handler(getMainLooper());
        //brightnessHandler = new Handler(getMainLooper());
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

        // do not start service - is started itself, when system restarts it
        // or from Default activity (LauncherActivity) when PPP is started from Android Studio
        // or another activities or BotUpReceiver or PackageReplacedReceiver.
        // !!! Must be in Android Studio configured in Edit configuration:
        //     - General/Launch: Default activity
        //     - Miscellaneous/Skip installation when APK has not chabger = DISABLED
        //     - Miscellaneous/Force stop running application before launching activity
        //    This configuration force call of PackageReplacedReceiver even when verson cocde is not changed
        /*if (PPApplication.getApplicationStarted(false)) {
            try {
                PPApplication.logE("##### PPApplication.onCreate", "start service");
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, false);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                startPPService(getApplicationContext(), serviceIntent);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }*/
        //}
        //else
        if (!PPApplication.getApplicationStarted(false))
            PPApplication.logE("##### PPApplication.onCreate", "application is not started");
    }

    static PPApplication getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
        return instance;
        //}
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        collator = getCollator();
        MultiDex.install(this);
    }

//    @NonNull
//    public Configuration getWorkManagerConfiguration() {
//        Configuration.Builder builder = new Configuration.Builder()
//                .setMinimumLoggingLevel(Log.DEBUG);
//
//        return builder.build();
//    }

    static WorkManager getWorkManagerInstance() {
        if (instance != null) {
            // get WorkManager instance only when PPApplication is created
            //if (workManagerInstance == null)
            return workManagerInstance;
        }
        else
            return null;
    }

    static void cancelWork(final String name, final boolean forceCancel) {
        // cancel only enqueued works
        PPApplication.startHandlerThreadCancelWork();
        final Handler handler = new Handler(PPApplication.handlerThreadCancelWork.getLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name);

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(name);
                //noinspection TryWithIdenticalCatches
                try {
                    List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" workInfoList.size()="+workInfoList.size());
                    // cancel only enqueued works
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        if (forceCancel || (state == WorkInfo.State.ENQUEUED)) {
                            // any work is enqueued, cancel it
                            workManager.cancelWorkById(workInfo.getId());
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void cancelAllWorks(@SuppressWarnings("SameParameterValue") boolean atStart) {
        //PPApplication.logE("------------ PPApplication.cancelAllWorks", "atStart="+atStart);
        if (atStart) {
            cancelWork(ShowProfileNotificationWorker.WORK_TAG, false);
            cancelWork(UpdateGUIWorker.WORK_TAG, false);
        }
        if (!atStart)
            cancelWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        for (String tag : PPApplication.elapsedAlarmsProfileDurationWork)
            cancelWork(tag, false);
        PPApplication.elapsedAlarmsProfileDurationWork.clear();
        for (String tag : PPApplication.elapsedAlarmsRunApplicationWithDelayWork)
            cancelWork(tag, false);
        PPApplication.elapsedAlarmsRunApplicationWithDelayWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayStartWork)
            cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayStartWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayEndWork)
            cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayEndWork.clear();
        for (String tag : PPApplication.elapsedAlarmsStartEventNotificationWork)
            cancelWork(tag, false);
        PPApplication.elapsedAlarmsStartEventNotificationWork.clear();
        if (atStart) {
            cancelWork(DisableInternalChangeWorker.WORK_TAG, false);
            cancelWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, false);
        }
        cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
        cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
        cancelWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG, false);
        cancelWork(BluetoothScanWorker.WORK_TAG, false);
        cancelWork(BluetoothScanWorker.WORK_TAG_SHORT, false);
        cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG, false);
        cancelWork(RestartEventsWithDelayWorker.WORK_TAG, false);
        cancelWork(GeofenceScanWorker.WORK_TAG, false);
        cancelWork(GeofenceScanWorker.WORK_TAG_SHORT, false);
        cancelWork(MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK, false);
        cancelWork(LocationGeofenceEditorActivity.FETCH_ADDRESS_WORK_TAG, false);
        if (atStart)
            cancelWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, false);
        cancelWork(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK, false);
        if (atStart) {
            cancelWork(PACKAGE_REPLACED_WORK_TAG, false);
            cancelWork(AFTER_FIRST_START_WORK_TAG, false);
            cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        }
        cancelWork(SearchCalendarEventsWorker.WORK_TAG, false);
        cancelWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, false);
        cancelWork(WifiScanWorker.WORK_TAG, false);
        cancelWork(WifiScanWorker.WORK_TAG_SHORT, false);
        cancelWork(WifiScanWorker.WORK_TAG_START_SCAN, false);
        cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_POSTED_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_REMOVED_SCANNER_WORK_TAG, false);
        cancelWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG, false);
        cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG, false);
        //cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_GEOFENCE_WORK_TAG, false);
        cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, false);
        cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, false);
    }

    /*
    static void setWorkManagerInstance(Context context) {
        workManagerInstance = WorkManager.getInstance(context);
    }
    */

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            try {
                android.os.Process.killProcess(pid);
                PPApplication.logToCrashlytics("E/PPApplication.checkAppReplacingState: app is replacing...kill");
            } catch (Exception e) {
                //Log.e("PPApplication.checkAppReplacingState", Log.getStackTraceString(e));
            }
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
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.logE("PPApplication.isNewVersion", "actualVersionCode=" + actualVersionCode);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    static void setApplicationFullyStarted(Context context, boolean showToast) {
        applicationFullyStarted = true; //started;

        final Context appContext = context.getApplicationContext();

        updateGUI(0/*appContext, true, true*/);

        if (showToast) {
            String text = context.getString(R.string.ppp_app_name) + " " + context.getString(R.string.application_is_started_toast);
            showToast(appContext, text, Toast.LENGTH_SHORT);
        }
    }

    //--------------------------------------------------------------

    static void addActivityLog(final Context context, final int logType, final String eventName,
                               final String profileName, final String profileIcon,
                               final int durationDelay, final String profilesEventsCount) {
        if (PPApplication.prefActivityLogEnabled) {
            PPApplication.startHandlerThread(/*"AlarmClockBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.addActivityLog");

                //if (ApplicationPreferences.preferences == null)
                //    ApplicationPreferences.preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                //ApplicationPreferences.setApplicationDeleteOldActivityLogs(context, Integer.valueOf(preferences.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7")));
                DatabaseHandler.getInstance(context).addActivityLog(ApplicationPreferences.applicationDeleteOldActivityLogs,
                       logType, eventName, profileName, profileIcon, durationDelay, profilesEventsCount);
            });
        }
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        */

        File path = instance.getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, LOG_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        if (instance == null)
            return;

        try {
            //Log.e("PPApplication.logIntoFile", "--- START");

            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
            */

            File path = instance.getApplicationContext().getExternalFilesDir(null);
            File logFile = new File(path, LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (Exception e) {
            //Log.e("PPApplication.logIntoFile", Log.getStackTraceString(e));
            //PPApplication.recordException(e);
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] filterTags = logFilterTags.split("\\|");
        for (String filterTag : filterTags) {
            if (!filterTag.contains("!")) {
                if (tag.contains(filterTag)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    static public boolean logEnabled() {
        //noinspection ConstantConditions
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

    static void startPPService(Context context, Intent serviceIntent/*, boolean isPPService*/) {
        //if (isPPService)
        //    PhoneProfilesService.startForegroundNotification = true;
        //PPApplication.logE("PPApplication.startPPService", "xxx");
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else
            context.getApplicationContext().startForegroundService(serviceIntent);
    }

    static void runCommand(Context context, Intent intent) {
        //PPApplication.logE("PPApplication.runCommand", "xxx");
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static void forceUpdateGUI(Context context, boolean alsoEditor, boolean alsoNotification/*, boolean refresh*/) {
        //PPApplication.logE("##### PPApplication.forceUpdateGUI", "xxx");
        //PPApplication.logE("##### PPApplication.forceUpdateGUI", "alsoEditor="+alsoEditor);
        //PPApplication.logE("##### PPApplication.forceUpdateGUI", "alsoNotification="+alsoNotification);
        //PPApplication.logE("##### PPApplication.forceUpdateGUI", "refresh="+refresh);

        // update gui even when app is not fully started
        //if (!PPApplication.applicationFullyStarted)
        //    return;

        // icon widget
        try {
            //IconWidgetProvider myWidget = new IconWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            IconWidgetProvider.updateWidgets(context/*, true*/);
//            PPApplication.logE("##### PPApplication.forceUpdateGUI", "icon widget");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // one row widget
        try {
            OneRowWidgetProvider.updateWidgets(context/*, true*/);
//            PPApplication.logE("##### PPApplication.forceUpdateGUI", "one row widget");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // list widget
        try {
            //ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            ProfileListWidgetProvider.updateWidgets(context/*, true*/);
//            PPApplication.logE("##### PPApplication.forceUpdateGUI", "list widget widget");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                //SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                //myWidget.updateWidgets(context, refresh);
                SamsungEdgeProvider.updateWidgets(context/*, true*/);
//                PPApplication.logE("##### PPApplication.forceUpdateGUI", "samsung edge panel");
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }

        // dash clock extension
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "dash clock extension)");
        Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
        //intent3.putExtra(DashClockBroadcastReceiver.EXTRA_REFRESH, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "activities");
        Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
        //intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
        intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        if (alsoNotification) {
            // KEEP IT AS WORK !!!
            // update immediate (without initialDelay())
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(ShowProfileNotificationWorker.class)
                            .addTag(ShowProfileNotificationWorker.WORK_TAG)
                            .build();
            try {
                if (PPApplication.getApplicationStarted(true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(ShowProfileNotificationWorker.WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] PPApplication.forceUpdateGUI", "for=" + ShowProfileNotificationWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplication.logE("[WORKER_CALL] PPApplication.forceUpdateGUI", "PPP notification");
                        workManager.enqueueUniqueWork(ShowProfileNotificationWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void updateGUI(int delay/*Context context, boolean alsoEditor, boolean refresh*/)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PPApplication.updateGUI", "lockRefresh=" + lockRefresh);
            PPApplication.logE("PPApplication.updateGUI", "doImport=" + EditorProfilesActivity.doImport);
            PPApplication.logE("PPApplication.updateGUI", "alsoEditor=" + alsoEditor);
            PPApplication.logE("PPApplication.updateGUI", "refresh=" + refresh);
        }*/

        /*
        if (!refresh) {
            if (lockRefresh || EditorProfilesActivity.doImport)
                // no refresh widgets
                return;
        }

        //PPApplication.logE("PPApplication.updateGUI", "send broadcast");
        Intent intent5 = new Intent(PPApplication.ACTION_UPDATE_GUI);
        intent5.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, refresh);
        intent5.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        context.sendBroadcast(intent5);
        */

//        PPApplication.logE("------- PPApplication.updateGUI", "delay=" + delay);

        OneTimeWorkRequest worker;
        if (delay == 0) {
            worker =
                    new OneTimeWorkRequest.Builder(UpdateGUIWorker.class)
                            .addTag(UpdateGUIWorker.WORK_TAG)
                            .build();
        }
        else {
            worker =
                    new OneTimeWorkRequest.Builder(UpdateGUIWorker.class)
                            .addTag(UpdateGUIWorker.WORK_TAG)
                            .setInitialDelay(delay, TimeUnit.SECONDS)
                            .build();
        }
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    /*boolean enqueue = immediate;
                    if (!enqueue) {
                        ListenableFuture<List<WorkInfo>> statuses;
                        statuses = workManager.getWorkInfosForUniqueWork(ElapsedAlarmsWorker.ELAPSED_ALARMS_UPDATE_GUI_TAG_WORK);
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            //PPApplication.logE("PPApplication.updateGUI", "workInfoList="+workInfoList);
                            //PPApplication.logE("PPApplication.updateGUI", "workInfoList="+workInfoList.size());
                            //boolean foundRunning = false;
                            //for (WorkInfo workInfo : workInfoList) {
                            //    WorkInfo.State state = workInfo.getState();
                            //    if (state == WorkInfo.State.RUNNING) {
                            //        // any work is running, equueue also new
                            //        foundRunning = true;
                            //        break;
                            //    }
                            //}
                            boolean foundEnqueued = false;
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (state == WorkInfo.State.ENQUEUED) {
                                    // any work is already enqueued, is not needed to enqueue new
                                    foundEnqueued = true;
                                    break;
                                }
                            }
                            enqueue = !foundEnqueued;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            enqueue = true;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            enqueue = true;
                        }
                    }

                    //PPApplication.logE("PPApplication.updateGUI", "immediate=" + immediate);
                    //PPApplication.logE("PPApplication.updateGUI", "enqueue=" + enqueue);

                    if (enqueue)*/

//                    //if (PPApplication.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(UpdateGUIWorker.WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[TEST BATTERY] PPApplication.updateGUI", "for=" + UpdateGUIWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                    } catch (Exception ignored) {
//                    }
//                    //}

//                    PPApplication.logE("[WORKER_CALL] PPApplication.updateGUI", "xxx");
                    workManager.enqueueUniqueWork(UpdateGUIWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

    }

    /*
    static void updateNotificationAndWidgets(boolean refresh, boolean forService, Context context)
    {
        PPApplication.showProfileNotification(refresh, forService);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.updateNotificationAndWidgets");
        updateGUI(context, true, refresh);
    }
    */

    static void showToast(final Context context, final String text, final int length) {
        final Context appContext = context.getApplicationContext();
        Handler handler = new Handler(context.getApplicationContext().getMainLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.showToast");
            try {
                Toast msg = ToastCompat.makeText(appContext, text, length);
                //Toast msg = Toast.makeText(appContext, text, length);
                msg.show();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
    }

    //--------------------------------------------------------------

    static void loadGlobalApplicationData(Context context) {
        synchronized (applicationStartedMutex) {
            applicationStarted = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_APPLICATION_STARTED, false);
        }
        synchronized (globalEventsRunStopMutex) {
            globalEventsRunStop = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, true);
        }

        IgnoreBatteryOptimizationNotification.getShowIgnoreBatteryOptimizationNotificationOnStart(context);
        CheckCriticalGitHubReleasesBroadcastReceiver.getShowCriticalGitHubReleasesNotification(context);
        getActivityLogEnabled(context);
        //getNotificationProfileName(context);
        //getWidgetProfileName(context);
        //getActivityProfileName(context);
        getLastActivatedProfile(context);
        Event.getEventsBlocked(context);
        Event.getForceRunEventRunning(context);
        PPPExtenderBroadcastReceiver.getApplicationInForeground(context);
        EventPreferencesCall.getEventCallEventType(context);
        EventPreferencesCall.getEventCallEventTime(context);
        EventPreferencesCall.getEventCallPhoneNumber(context);
        HeadsetConnectionBroadcastReceiver.getEventHeadsetParameters(context);
        WifiScanner.getForceOneWifiScan(context);
        BluetoothScanner.getForceOneBluetoothScan(context);
        BluetoothScanner.getForceOneLEBluetoothScan(context);
        BluetoothScanWorker.getBluetoothEnabledForScan(context);
        BluetoothScanWorker.getScanRequest(context);
        BluetoothScanWorker.getLEScanRequest(context);
        BluetoothScanWorker.getWaitForResults(context);
        BluetoothScanWorker.getWaitForLEResults(context);
        BluetoothScanWorker.getScanKilled(context);
        WifiScanWorker.getWifiEnabledForScan(context);
        WifiScanWorker.getScanRequest(context);
        WifiScanWorker.getWaitForResults(context);
        ApplicationPreferences.loadStartTargetHelps(context);
    }

    static void loadApplicationPreferences(Context context) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            ApplicationPreferences.editorOrderSelectedItem(context);
            ApplicationPreferences.editorSelectedView(context);
            ApplicationPreferences.editorProfilesViewSelectedItem(context);
            ApplicationPreferences.editorEventsViewSelectedItem(context);
            //ApplicationPreferences.applicationFirstStart(context);
            ApplicationPreferences.applicationStartOnBoot(context);
            ApplicationPreferences.applicationActivate(context);
            ApplicationPreferences.applicationStartEvents(context);
            ApplicationPreferences.applicationActivateWithAlert(context);
            ApplicationPreferences.applicationClose(context);
            ApplicationPreferences.applicationLongClickActivation(context);
            //ApplicationPreferences.applicationLanguage(context);
            ApplicationPreferences.applicationTheme(context);
            //ApplicationPreferences.applicationActivatorPrefIndicator(context);
            ApplicationPreferences.applicationEditorPrefIndicator(context);
            //ApplicationPreferences.applicationActivatorHeader(context);
            //ApplicationPreferences.applicationEditorHeader(context);
            ApplicationPreferences.notificationsToast(context);
            //ApplicationPreferences.notificationStatusBar(context);
            //ApplicationPreferences.notificationStatusBarPermanent(context);
            //ApplicationPreferences.notificationStatusBarCancel(context);
            ApplicationPreferences.notificationStatusBarStyle(context);
            ApplicationPreferences.notificationShowInStatusBar(context);
            ApplicationPreferences.notificationTextColor(context);
            ApplicationPreferences.notificationHideInLockScreen(context);
            //ApplicationPreferences.notificationTheme(context);
            ApplicationPreferences.applicationWidgetListPrefIndicator(context);
            ApplicationPreferences.applicationWidgetListHeader(context);
            ApplicationPreferences.applicationWidgetListBackground(context);
            ApplicationPreferences.applicationWidgetListLightnessB(context);
            ApplicationPreferences.applicationWidgetListLightnessT(context);
            ApplicationPreferences.applicationWidgetIconColor(context);
            ApplicationPreferences.applicationWidgetIconLightness(context);
            ApplicationPreferences.applicationWidgetListIconColor(context);
            ApplicationPreferences.applicationWidgetListIconLightness(context);
            //ApplicationPreferences.applicationEditorAutoCloseDrawer(context);
            //ApplicationPreferences.applicationEditorSaveEditorState(context);
            ApplicationPreferences.notificationPrefIndicator(context);
            ApplicationPreferences.applicationHomeLauncher(context);
            ApplicationPreferences.applicationWidgetLauncher(context);
            ApplicationPreferences.applicationNotificationLauncher(context);
            ApplicationPreferences.applicationEventWifiScanInterval(context);
            ApplicationPreferences.applicationDefaultProfile(context);
            ApplicationPreferences.applicationDefaultProfileNotificationSound(context);
            ApplicationPreferences.applicationDefaultProfileNotificationVibrate(context);
            //ApplicationPreferences.applicationDefaultProfileUsage(context);
            ApplicationPreferences.applicationActivatorGridLayout(context);
            ApplicationPreferences.applicationWidgetListGridLayout(context);
            ApplicationPreferences.applicationEventBluetoothScanInterval(context);
            //ApplicationPreferences.applicationEventWifiRescan(context);
            //ApplicationPreferences.applicationEventBluetoothRescan(context);
            ApplicationPreferences.applicationWidgetIconHideProfileName(context);
            ApplicationPreferences.applicationShortcutEmblem(context);
            ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationPowerSaveModeInternal(context);
            ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context);
            ApplicationPreferences.applicationEventLocationUseGPS(context);
            //ApplicationPreferences.applicationEventLocationRescan(context);
            ApplicationPreferences.applicationEventOrientationScanInterval(context);
            ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationEventMobileCellsRescan(context);
            ApplicationPreferences.applicationDeleteOldActivityLogs(context);
            ApplicationPreferences.applicationWidgetIconBackground(context);
            ApplicationPreferences.applicationWidgetIconLightnessB(context);
            ApplicationPreferences.applicationWidgetIconLightnessT(context);
            ApplicationPreferences.applicationEventUsePriority(context);
            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context);
            ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
            //ApplicationPreferences.applicationSamsungEdgePrefIndicator(context);
            ApplicationPreferences.applicationSamsungEdgeHeader(context);
            ApplicationPreferences.applicationSamsungEdgeBackground(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            ApplicationPreferences.applicationSamsungEdgeIconColor(context);
            ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
            //ApplicationPreferences.applicationSamsungEdgeGridLayout(context);
            ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationRestartEventsWithAlert(context);
            ApplicationPreferences.applicationWidgetListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
            ApplicationPreferences.applicationWidgetListBackgroundType(context);
            ApplicationPreferences.applicationWidgetListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetIconBackgroundType(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColor(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundType(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context);
            //ApplicationPreferences.applicationEventWifiEnableWifi(context);
            //ApplicationPreferences.applicationEventBluetoothEnableBluetooth(context);
            ApplicationPreferences.applicationEventWifiScanIfWifiOff(context);
            ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff(context);
            ApplicationPreferences.applicationEventWifiEnableScanning(context);
            ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
            ApplicationPreferences.applicationEventOrientationEnableScanning(context);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNeverAskForEnableRun(context);
            ApplicationPreferences.applicationUseAlarmClock(context);
            ApplicationPreferences.applicationNeverAskForGrantRoot(context);
            ApplicationPreferences.applicationNeverAskForGrantG1Permission(context);
            ApplicationPreferences.notificationShowButtonExit(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
            ApplicationPreferences.applicationWidgetOneRowBackground(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
            ApplicationPreferences.applicationWidgetOneRowIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
            ApplicationPreferences.applicationWidgetListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
            ApplicationPreferences.applicationWidgetIconLightnessBorder(context);
            ApplicationPreferences.applicationWidgetListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
            ApplicationPreferences.applicationWidgetIconShowBorder(context);
            ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetIconCustomIconLightness(context);
            ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context);
            //ApplicationPreferences.notificationDarkBackground(context);
            ApplicationPreferences.notificationUseDecoration(context);
            ApplicationPreferences.notificationLayoutType(context);
            ApplicationPreferences.notificationBackgroundColor(context);
            //ApplicationPreferences.applicationNightModeOffTheme(context);
            ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context);
            ApplicationPreferences.applicationSamsungEdgeVerticalPosition(context);
            ApplicationPreferences.notificationBackgroundCustomColor(context);
            ApplicationPreferences.notificationNightMode(context);
            ApplicationPreferences.applicationEditorHideHeaderOrBottomBar(context);
            ApplicationPreferences.applicationWidgetIconShowProfileDuration(context);
            ApplicationPreferences.notificationNotificationStyle(context);
            ApplicationPreferences.notificationShowProfileIcon(context);
            ApplicationPreferences.applicationEventBackgroundScanningEnableScanning(context);
            ApplicationPreferences.applicationEventBackgroundScanningScanInterval(context);
            ApplicationPreferences.applicationEventBackgroundScanningScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventBackgroundScanningScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanIgnoreHotspot(context);
            ApplicationPreferences.applicationEventNotificationEnableScanning(context);
            ApplicationPreferences.applicationEventNotificationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn(context);
        }
    }

    static void loadProfileActivationData(Context context) {
        ActivateProfileHelper.getRingerVolume(context);
        ActivateProfileHelper.getNotificationVolume(context);
        ActivateProfileHelper.getRingerMode(context);
        ActivateProfileHelper.getZenMode(context);
        ActivateProfileHelper.getLockScreenDisabled(context);
        ActivateProfileHelper.getActivatedProfileScreenTimeout(context);
        ActivateProfileHelper.getMergedRingNotificationVolumes(context);
        //Profile.getActivatedProfileForDuration(context);
        Profile.getActivatedProfileEndDurationTime(context);
    }

    //--------------------------------------------------------------

    static boolean getApplicationStarted(boolean testService)
    {
        synchronized (applicationStartedMutex) {
            if (testService) {
                try {
                    return applicationStarted &&
                            (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return applicationStarted;
        }
    }

    static void setApplicationStarted(Context context, boolean appStarted)
    {
        synchronized (applicationStartedMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
            editor.apply();
            applicationStarted = appStarted;
        }
    }

    static public int getSavedVersionCode(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static public void setSavedVersionCode(Context context, int version)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }

    static boolean prefActivityLogEnabled;
    private static void getActivityLogEnabled(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefActivityLogEnabled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_ACTIVITY_LOG_ENABLED, true);
            //return prefActivityLogEnabled;
        }
    }
    static void setActivityLogEnabled(Context context, boolean enabled)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_ACTIVITY_LOG_ENABLED, enabled);
            editor.apply();
            prefActivityLogEnabled = enabled;
        }
    }

    /*
    static String prefNotificationProfileName;
    private static void getNotificationProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefNotificationProfileName = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_NAME, "");
            //return prefNotificationProfileName;
        }
    }
    static public void setNotificationProfileName(Context context, String notificationProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_NOTIFICATION_PROFILE_NAME, notificationProfileName);
            editor.apply();
            prefNotificationProfileName = notificationProfileName;
        }
    }
     */

    /*
    static String prefWidgetProfileName1;
    static String prefWidgetProfileName2;
    static String prefWidgetProfileName3;
    static String prefWidgetProfileName4;
    static String prefWidgetProfileName5;
    private static void getWidgetProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefWidgetProfileName1 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_1", "");
            prefWidgetProfileName2 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_2", "");
            prefWidgetProfileName3 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_3", "");
            prefWidgetProfileName4 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_4", "");
            prefWidgetProfileName5 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_5", "");
            //return prefNotificationProfileName;
        }
    }
    static void setWidgetProfileName(Context context, int widgetType, String widgetProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, widgetProfileName);
            editor.apply();
            switch (widgetType) {
                case 1:
                    prefWidgetProfileName1 = widgetProfileName;
                    break;
                case 2:
                    prefWidgetProfileName2 = widgetProfileName;
                    break;
                case 3:
                    prefWidgetProfileName3 = widgetProfileName;
                    break;
                case 4:
                    prefWidgetProfileName4 = widgetProfileName;
                    break;
                case 5:
                    prefWidgetProfileName5 = widgetProfileName;
                    break;
            }
        }
    }

    static String prefActivityProfileName1;
    static String prefActivityProfileName2;
    static String prefActivityProfileName3;
    private static void getActivityProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefActivityProfileName1 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_1", "");
            prefActivityProfileName2 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_2", "");
            prefActivityProfileName3 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_3", "");
            //return prefActivityProfileName;
        }
    }
    static void setActivityProfileName(Context context, int activityType, String activityProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, activityProfileName);
            editor.apply();
            switch (activityType) {
                case 1:
                    prefActivityProfileName1 = activityProfileName;
                    break;
                case 2:
                    prefActivityProfileName2 = activityProfileName;
                    break;
                case 3:
                    prefActivityProfileName3 = activityProfileName;
                    break;
            }
        }
    }
    */

    static long prefLastActivatedProfile;
    private static void getLastActivatedProfile(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefLastActivatedProfile = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PREF_LAST_ACTIVATED_PROFILE, 0);
            //return prefLastActivatedProfile;
        }
    }
    static public void setLastActivatedProfile(Context context, long profileId)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_LAST_ACTIVATED_PROFILE, profileId);
            editor.apply();
            prefLastActivatedProfile = profileId;
        }
    }

    static public int getDaysAfterFirstStart(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }
    static public void setDaysAfterFirstStart(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static public int getDonationNotificationCount(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DONATION_NOTIFICATION_COUNT, 0);
    }
    static public void setDonationNotificationCount(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static public int getDaysForNextDonationNotification(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }
    static public void setDaysForNextDonationNotification(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
        editor.apply();
    }

    static public boolean getDonationDonated(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PREF_DONATION_DONATED, false);
    }
    static public void setDonationDonated(Context context)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_DONATION_DONATED, true);
        editor.apply();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isIgnoreBatteryOptimizationEnabled(Context appContext) {
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        try {
            if (pm != null) {
                //PPApplication.logE("PPApplication.isIgnoreBatteryOptimizationEnabled", "pm="+pm);
                return pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME);
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    // --------------------------------

    // notification channels -------------------------

    static void createProfileNotificationChannel(/*Profile profile, */Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(PROFILE_NOTIFICATION_CHANNEL) != null)
                    return;// true;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
                NotificationChannel newChannel = notificationManager.getNotificationChannel(PROFILE_NOTIFICATION_CHANNEL);

                if (newChannel == null)
                    throw new RuntimeException("PPApplication.createProfileNotificationChannel - NOT CREATED - newChannel=null");
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        //return true;
    }

    static void createMobileCellsRegistrationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL) != null)
                    return;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createInformationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(INFORMATION_NOTIFICATION_CHANNEL) != null)
                    return;

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
                //channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createExclamationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL) != null)
                    return;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createGrantPermissionNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL) != null)
                    return;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createNotifyEventStartNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NOTIFY_EVENT_START_NOTIFICATION_CHANNEL) != null)
                    return;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createMobileCellsNewCellNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL) != null)
                    return;

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
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createDonationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(DONATION_CHANNEL) != null)
                    return;

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
                //channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createNewReleaseNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NEW_RELEASE_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_new_release);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_new_release_description);

                NotificationChannel channel = new NotificationChannel(NEW_RELEASE_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(false);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(Color.RED);
                channel.enableVibration(false);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(false);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    /*
    static void createCrashReportNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(CRASH_REPORT_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_crash_report);
                // The user-visible description of the channel.
                String description = context.getString(R.string.empty_string);

                NotificationChannel channel = new NotificationChannel(CRASH_REPORT_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }
    */

    static void createGeneratedByProfileNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_generated_by_profile);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_generated_by_profile_description);

                NotificationChannel channel = new NotificationChannel(GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
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
        PPApplication.createNewReleaseNotificationChannel(appContext);
        //PPApplication.createCrashReportNotificationChannel(appContext);
        PPApplication.createGeneratedByProfileNotificationChannel(appContext);
    }

    /*
    static void showProfileNotification() {
        try {
            //PPApplication.logE("PPApplication.showProfileNotification", "xxx");

            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(false);

        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
    */

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
                PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
                if (PPApplication.rootMutex.rooted) {
                    PackageManager packageManager = PPApplication.getInstance().getPackageManager();
                    // SuperSU
                    Intent intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                    if (intent != null)
                        PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                    else {
                        intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                        if (intent != null)
                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                        else
                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                    }
                }
            } catch (Exception e) {
                // https://github.com/firebase/firebase-android-sdk/issues/1226
                //PPApplication.recordException(e);
            }
            return rootMutex.rooted;
        }

        try {
            //PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
            //if (roottools.isRootAvailable()) {
            //noinspection RedundantIfStatement
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                //PPApplication.logE("PPApplication._isRooted", "root available");
                rootMutex.rooted = true;
            } else {
                //PPApplication.logE("PPApplication._isRooted", "root NOT available");
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
                PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(rootMutex.rooted));
                if (PPApplication.rootMutex.rooted) {
                    PackageManager packageManager = PPApplication.getInstance().getPackageManager();
                    // SuperSU
                    Intent intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                    if (intent != null)
                        PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                    else {
                        intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                        if (intent != null)
                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                        else
                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                    }
                }
            } catch (Exception e) {
                // https://github.com/firebase/firebase-android-sdk/issues/1226
                //PPApplication.recordException(e);
            }
        } catch (Exception e) {
            //Log.e("PPApplication._isRooted", Log.getStackTraceString(e));
            PPApplication.recordException(e);
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
                    //PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    //noinspection StatementWithEmptyBody
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        //PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    }/* else {
                        // grant denied
                        PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }*/
                } catch (Exception e) {
                    //Log.e("PPApplication.isRootGranted", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        } /*else {
            PPApplication.logE("PPApplication.isRootGranted", "not rooted");
        }*/
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
                //PPApplication.logE("PPApplication.settingsBinaryExists", "start");
                rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                rootMutex.settingsBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.settingsBinaryExists", "settingsBinaryExists=" + rootMutex.settingsBinaryExists);
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
                //PPApplication.logE("PPApplication.serviceBinaryExists", "start");
                rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                rootMutex.serviceBinaryChecked = true;
            }
            //PPApplication.logE("PPApplication.serviceBinaryExists", "serviceBinaryExists=" + rootMutex.serviceBinaryExists);
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
        rootshell.debugMode = rootToolsDebug;

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
                roottools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
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
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            /*String dir = context.getPackageManager().getApplicationInfo(context.PPApplication.PACKAGE_NAME, 0).dataDir;
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

        if (isRooted(false)) {
            synchronized (PPApplication.rootMutex) {
                //noinspection RegExpRedundantEscape
                final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");

                Command command = new Command(0, false, "service list") {
                    @Override
                    public void commandOutput(int id, String line) {
                        //PPApplication.logE("PPApplication.getServicesList", "line=" + line);
                        Matcher matcher = compile.matcher(line);
                        if (matcher.find()) {
                            synchronized (PPApplication.serviceListMutex) {
                                //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                                serviceListMutex.serviceList.add(Pair.create(matcher.group(1), matcher.group(2)));
                                //PPApplication.logE("PPApplication.getServicesList", "matcher.group(1)=" + matcher.group(1));
                                //PPApplication.logE("PPApplication.getServicesList", "matcher.group(2)=" + matcher.group(2));
                            }
                        }
                        super.commandOutput(id, line);
                    }
                };

                try {
                    //roottools.getShell(false).add(command);
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command, "PPApplication.getServicesList");
                } catch (Exception e) {
                    //Log.e("PPApplication.getServicesList", Log.getStackTraceString(e));
                }
            }
        }
    }

    static Object getServiceManager(String serviceType) {
        synchronized (PPApplication.serviceListMutex) {
            if (serviceListMutex.serviceList != null) {
                //noinspection rawtypes
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
            //noinspection rawtypes
            for (Class declaredFields : Class.forName(serviceManager).getDeclaredClasses()) {
                Field[] declaredFields2 = declaredFields.getDeclaredFields();
                int length = declaredFields2.length;
                int iField = 0;
                while (iField < length) {
                    Field field = declaredFields2[iField];
                    String name = field.getName();
                    if (method.isEmpty()) {
                        //if (name.contains("TRANSACTION_"))
                        //    PPApplication.logE("[LIST] PPApplication.getTransactionCode", "field.getName()="+name);
                        iField++;
                    }
                    else {
                        if (/*name == null ||*/ !name.equals("TRANSACTION_" + method)) {
                            iField++;
                        } else {
                            try {
                                field.setAccessible(true);
                                code = field.getInt(field);
                                break;
                            } catch (Exception e) {
                                //Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //Log.e("PPApplication.getTransactionCode", Log.getStackTraceString(e));
            //PPApplication.recordException(e);
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

    static void commandWait(Command cmd, String calledFrom) /*throws Exception*/ {
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
                    //Log.e("PPApplication.commandWait", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            }
        }
        if (!cmd.isFinished()){
            //Log.e("PPApplication.commandWait", "Called from: " + calledFrom + "; Could not finish root command in " + (waitTill/waitTillMultiplier));
            PPApplication.logToCrashlytics("E/PPApplication.commandWait: Called from: " + calledFrom + "; Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void registerContentObservers(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.registerContentObservers", "xxx");
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CONTENT_OBSERVERS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void registerCallbacks(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.registerContentObservers", "xxx");
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CALLBACKS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartBackgroundScanningScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BACKGROUND_SCANNING_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BACKGROUND_SCANNING_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForWifiScanner", "xxx");
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
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void reregisterReceiversForWifiScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.reregisterReceiversForWifiScanner", "xxx");
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
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartWifiScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForBluetoothScanner", "xxx");
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
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.reregisterReceiversForBluetoothScanner", "xxx");
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
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartBluetoothScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartBluetoothScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartGeofenceScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartGeofenceScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void forceStartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PhoneProfilesService.forceStartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void forceStartPhoneStateScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PhoneProfilesService.forceStartPhoneStateScanner", "xxx");
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
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartPhoneStateScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartPhoneStateScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartTwilightScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
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
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartNotificationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_NOTIFICATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_NOTIFICATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void restartAllScanners(Context context, boolean fromBatteryChange) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartAllScanners", "xxx");
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
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FROM_BATTERY_CHANGE, fromBatteryChange);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    public static void rescanAllScanners(Context context) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartAllScanners", "xxx");
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESCAN_SCANNERS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

/*
    public static void restartEvents(Context context, boolean unblockEventsRun, boolean reactivateProfile) {
        try {
            //PPApplication.logE("[RJS] PPApplication.restartEvents", "xxx");
//            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
//            PPApplication.startPPService(context, serviceIntent);
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }
*/
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
        catch (Exception ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Exception e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
    */

    private static boolean isXiaomi() {
        return Build.BRAND.equalsIgnoreCase("xiaomi") ||
               Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
               Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static boolean isMIUIROM() {
        boolean miuiRom1 = false;
        boolean miuiRom2 = false;
        boolean miuiRom3 = false;

        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            miuiRom1 = line.length() != 0;
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = line.length() != 0;
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = line.length() != 0;
                input.close();
            }

        } catch (Exception ex) {
            //Log.e("PPApplication.isMIUIROM", Log.getStackTraceString(ex));
            PPApplication.recordException(ex);
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom1=" + miuiRom1);
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom2=" + miuiRom2);
            PPApplication.logE("PPApplication.isMIUIROM", "miuiRom3=" + miuiRom3);
        }*/

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    private static String getEmuiRomName() {
        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.build.version.emui");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        } catch (Exception ex) {
            //Log.e("PPApplication.getEmuiRomName", Log.getStackTraceString(ex));
            PPApplication.recordException(ex);
            return "";
        }
    }

    private static boolean isHuawei() {
        return Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isEMUIROM() {
        String emuiRomName = getEmuiRomName();
        //PPApplication.logE("PPApplication.isEMUIROM", "emuiRomName="+emuiRomName);

        return (emuiRomName.length() != 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3");// || "EMUI 2.3".equalsIgnoreCase(emuiRomName);
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

    private static boolean isOnePlus() {
        //PPApplication.logE("PPApplication.isOnePlus", "brand="+Build.BRAND);
        //PPApplication.logE("PPApplication.isOnePlus", "manufacturer="+Build.MANUFACTURER);
        //PPApplication.logE("PPApplication.isOnePlus", "fingerprint="+Build.FINGERPRINT);
        return Build.BRAND.equalsIgnoreCase("oneplus") ||
                Build.MANUFACTURER.equalsIgnoreCase("oneplus") ||
                Build.FINGERPRINT.toLowerCase().contains("oneplus");
    }

    private static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo") ||
                Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                Build.FINGERPRINT.toLowerCase().contains("oppo");
    }

    private static boolean isRealme() {
        return Build.BRAND.equalsIgnoreCase("realme") ||
                Build.MANUFACTURER.equalsIgnoreCase("realme") ||
                Build.FINGERPRINT.toLowerCase().contains("realme");
    }

    private static String getReadableModVersion() {
        String modVer = getSystemProperty(SYS_PROP_MOD_VERSION);
        return (modVer == null || modVer.length() == 0 ? "Unknown" : modVer);
    }

    @SuppressWarnings("SameParameterValue")
    private static String getSystemProperty(String propName)
    {
        String line;
        BufferedReader input = null;
        try
        {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (Exception ex)
        {
            //Log.e("PPApplication.getSystemProperty", "Unable to read sysprop " + propName, ex);
            PPApplication.recordException(ex);
            return null;
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch (Exception e)
                {
                    //Log.e("PPApplication.getSystemProperty", "Exception while closing InputStream", e);
                    PPApplication.recordException(e);
                }
            }
        }
        return line;
    }

    static boolean hasSystemFeature(PackageManager packageManager, String feature) {
        try {
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    private static void _exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            PPApplication.logE("PPApplication._exitApp", "shutdown="+shutdown);

            if (!shutdown)
                PPApplication.cancelAllWorks(false);

            if (dataWrapper != null)
                dataWrapper.stopAllEvents(false, false, false, false);

            if (!shutdown) {

                // remove notifications
                ImportantInfoNotification.removeNotification(context);
                DrawOverAppsPermissionNotification.removeNotification(context);
                IgnoreBatteryOptimizationNotification.removeNotification(context);
                Permissions.removeNotifications(context);

                addActivityLog(context, PPApplication.ALTYPE_APPLICATION_EXIT, null, null, null, 0, "");

                /*if (PPApplication.brightnessHandler != null) {
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            ActivateProfileHelper.removeBrightnessView(context);
                        }
                    });
                }*/
                //if (PPApplication.screenTimeoutHandler != null) {
                //    PPApplication.screenTimeoutHandler.post(new Runnable() {
                //        public void run() {
                            //ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            //ActivateProfileHelper.removeBrightnessView(context);
                            //PPApplication.logE("******** PPApplication._exitApp()", "remove wakelock");
                            ActivateProfileHelper.removeKeepScreenOnView(context);
                //        }
                //    });
                //}

                //PPApplication.initRoot();

                if (dataWrapper != null) {
                    synchronized (dataWrapper.profileList) {
                        if (!dataWrapper.profileListFilled)
                            dataWrapper.fillProfileList(false, false);
                        for (Profile profile : dataWrapper.profileList)
                            ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
                    }

                    synchronized (dataWrapper.eventList) {
                        if (!dataWrapper.eventListFilled)
                            dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList)
                            StartEventNotificationBroadcastReceiver.removeAlarm(event, context);
                    }
                }


                //Profile.setActivatedProfileForDuration(context, 0);
    //            PPApplication.logE("[FIFO_TEST] PPApplication._exitApp", "#### clear");
                if (dataWrapper != null) {
                    synchronized (PPApplication.profileActivationMutex) {
                        List<String> activateProfilesFIFO = new ArrayList<>();
                        dataWrapper.saveActivatedProfilesFIFO(activateProfilesFIFO);
                    }
                }
            }

            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            PPApplication.logE("PPApplication._exitApp", "stop service");
            //PhoneProfilesService.getInstance().showProfileNotification(false);
            //context.stopService(new Intent(context, PhoneProfilesService.class));
            PhoneProfilesService.stop(/*context*/);
            //if (PhoneProfilesService.getInstance() != null)
            //    PhoneProfilesService.getInstance().setApplicationFullyStarted(false, false);

            Permissions.setAllShowRequestPermissions(context.getApplicationContext(), true);

            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            //PhoneStateScanner.setShowEnableLocationNotification(context.getApplicationContext(), true);
            //ActivateProfileHelper.setScreenUnlocked(context, true);

            if (!shutdown) {
                //PPApplication.logE("PPApplication._exitApp", "forceUpdateGUI");
                //ActivateProfileHelper.updateGUI(context, false, true);
                //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=PPApplication._exitApp");
                PPApplication.forceUpdateGUI(context.getApplicationContext(), false, false/*, true*/);

                Handler _handler = new Handler(context.getMainLooper());
                Runnable r = () -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication._exitApp");
                    try {
                        if (activity != null)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(PPApplication.pid);
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }

            //workManagerInstance.pruneWork();

            PPApplication.logE("PPApplication._exitApp", "set application started = false");
            PPApplication.setApplicationStarted(context, false);

        } catch (Exception e) {
            //Log.e("PPApplication._exitApp", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void exitApp(final boolean useHandler, final Context context, final DataWrapper dataWrapper, final Activity activity,
                                 final boolean shutdown/*, final boolean killProcess*//*, final boolean removeAlarmClock*/) {
        try {
            if (useHandler) {
                PPApplication.startHandlerThread(/*"PPApplication.exitApp"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.exitApp");

                    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_exitApp");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                        _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPApplication.exitApp");
                    } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                });
            }
            else
                _exitApp(context, dataWrapper, activity, shutdown/*, killProcess*/);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void showDoNotKillMyAppDialog(final Fragment fragment) {
        if (fragment.getActivity() != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getActivity());
            dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);

            LayoutInflater inflater = fragment.getActivity().getLayoutInflater();
            @SuppressLint("InflateParams")
            View layout = inflater.inflate(R.layout.dialog_do_not_kill_my_app, null);
            dialogBuilder.setView(layout);

            DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
            if (doki != null) {
                doki.setButtonsVisibility(false);
                doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
            }

            AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if (!fragment.getActivity().isFinishing())
                dialog.show();
        }
    }

    static void startHandlerThread(/*String from*/) {
        //PPApplication.logE("PPApplication.startHandlerThread", "from="+from);
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThread.start();
        }
    }

    static void startHandlerThreadCancelWork() {
        //PPApplication.logE("PPApplication.startHandlerThreadCancelWork", "from="+from);
        if (handlerThreadCancelWork == null) {
            handlerThreadCancelWork = new HandlerThread("PPHandlerThreadCancelWork", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadCancelWork.start();
        }
    }

    static void startHandlerThreadBroadcast(/*String from*/) {
        if (handlerThreadBroadcast == null) {
            handlerThreadBroadcast = new HandlerThread("PPHandlerThreadBroadcast", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadBroadcast.start();
        }
    }

    static void startHandlerThreadPPScanners() {
        if (handlerThreadPPScanners == null) {
            handlerThreadPPScanners = new HandlerThread("PPHandlerThreadPPScanners", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPScanners.start();
        }
    }

    static void startHandlerThreadOrientationScanner() {
        if (handlerThreadOrientationScanner == null) {
            handlerThreadOrientationScanner = new OrientationScannerHandlerThread("PPHandlerThreadOrientationScanner", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadOrientationScanner.start();
            if (PPApplication.proximitySensor != null)
                PPApplication.handlerThreadOrientationScanner.maxProximityDistance = PPApplication.proximitySensor.getMaximumRange();
            if (PPApplication.lightSensor != null)
                PPApplication.handlerThreadOrientationScanner.maxLightDistance = PPApplication.lightSensor.getMaximumRange();
        }
    }

    static void startHandlerThreadPPCommand() {
        if (handlerThreadPPCommand == null) {
            handlerThreadPPCommand = new HandlerThread("PPHandlerThreadPPCommand", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPCommand.start();
        }
    }

    static void startHandlerThreadWidget() {
        if (handlerThreadWidget == null) {
            handlerThreadWidget = new HandlerThread("PPHandlerThreadWidget", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWidget.start();
        }
    }

    static void startHandlerThreadPlayTone() {
        if (handlerThreadPlayTone == null) {
            handlerThreadPlayTone = new HandlerThread("PPHandlerThreadPlayTone", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPlayTone.start();
        }
    }

    static void startHandlerThreadVolumes() {
        if (handlerThreadVolumes == null) {
            handlerThreadVolumes = new HandlerThread("handlerThreadVolumes", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadVolumes.start();
        }
    }

    static void startHandlerThreadRadios() {
        if (handlerThreadRadios == null) {
            handlerThreadRadios = new HandlerThread("handlerThreadRadios", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRadios.start();
        }
    }

    static void startHandlerThreadWallpaper() {
        if (handlerThreadWallpaper == null) {
            handlerThreadWallpaper = new HandlerThread("handlerThreadWallpaper", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWallpaper.start();
        }
    }

    static void startHandlerThreadRunApplication() {
        if (handlerThreadRunApplication == null) {
            handlerThreadRunApplication = new HandlerThread("handlerThreadRunApplication", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRunApplication.start();
        }
    }

    static void startHandlerThreadProfileActivation() {
        if (handlerThreadProfileActivation == null) {
            handlerThreadProfileActivation = new HandlerThread("handlerThreadProfileActivation", THREAD_PRIORITY_MORE_FAVORABLE); //);;
            handlerThreadProfileActivation.start();
        }
    }

    static void setBlockProfileEventActions(boolean enable) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            DisableBlockProfileEventActionWorker.enqueueWork();
        }
        else {
            PPApplication.cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        }
    }

    //--------------------

    static Collator getCollator(/*Context context*/)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {
        // get application Locale
//            String lang = ApplicationPreferences.applicationLanguage(context);
        Locale appLocale;
//            if (!lang.equals("system")) {
//                String[] langSplit = lang.split("-");
//                if (langSplit.length == 1)
//                    appLocale = new Locale(lang);
//                else
//                    appLocale = new Locale(langSplit[0], langSplit[1]);
//            } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            appLocale = Resources.getSystem().getConfiguration().locale;
        }
//            }
        // get collator for application locale
        return Collator.getInstance(appLocale);
//        }
//        else {
//            //Log.d("GlobalGUIRoutines.getCollator", java.util.Locale.getDefault().toString());
//            return Collator.getInstance();
//        }
    }


/*    //-----------------------------

    private static WeakReference<Activity> foregroundEditorActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof EditorProfilesActivity)
            foregroundEditorActivity = null;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    static Activity getEditorActivity() {
        if (foregroundEditorActivity != null && foregroundEditorActivity.get() != null) {
            return foregroundEditorActivity.get();
        }
        return null;
    }
*/

    // Sensor manager ------------------------------------------------------------------------------

    static Sensor getAccelerometerSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
            return null;
    }

    static Sensor getMagneticFieldSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else
            return null;
    }

    static Sensor getProximitySensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        else
            return null;
    }

    /*
    private Sensor getOrientationSensor(Context context) {
        synchronized (PPApplication.orientationScannerMutex) {
            if (mOrientationSensorManager == null)
                mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }*/

    static Sensor getLightSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        else
            return null;
    }

    // Firebase Crashlytics -------------------------------------------------------------------------

    static void recordException(Throwable ex) {
        try {
            //FirebaseCrashlytics.getInstance().recordException(ex);
            ACRA.getErrorReporter().handleException(ex);
            //ACRA.getErrorReporter().putCustomData("NON-FATAL_EXCEPTION", Log.getStackTraceString(ex));
        } catch (Exception ignored) {}
    }

    static void logToCrashlytics(String s) {
        try {
            //FirebaseCrashlytics.getInstance().log(s);
            ACRA.getErrorReporter().putCustomData("Log", s);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, int value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, String value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, value);
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("SameParameterValue")
    static void setCustomKey(String key, boolean value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    /*
    static void logAnalyticsEvent(Context context, String itemId, String itemName, String contentType) {
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception e) {
            //recordException(e);
        }
    }
    */

    //---------------------------------------------------------------------------------------------

}
