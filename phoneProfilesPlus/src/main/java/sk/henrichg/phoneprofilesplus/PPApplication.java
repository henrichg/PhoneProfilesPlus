package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.service.quicksettings.TileService;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkManager;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.drakeet.support.toast.ToastCompat;

public class PPApplication extends Application
                                        //implements Configuration.Provider
                                        //implements Application.ActivityLifecycleCallbacks
{
    // this version code must by <= version code in dependencies.gradle
    static final int PPP_VERSION_CODE_FOR_IMPORTANT_INFO_NEWS = 6941;
    static final boolean SHOW_IMPORTANT_INFO_NEWS = true;
    static final boolean SHOW_IMPORTANT_INFO_NOTIFICATION_NEWS = true;

    //static final int VERSION_CODE_EXTENDER_3_0 = 200;
    //static final int VERSION_CODE_EXTENDER_4_0 = 400;
    //static final int VERSION_CODE_EXTENDER_5_1_3_1 = 540;
    //static final int VERSION_CODE_EXTENDER_5_1_4_1 = 600;
    //static final int VERSION_CODE_EXTENDER_6_0 = 620;
    //static final int VERSION_CODE_EXTENDER_6_2 = 670;
    //static final int VERSION_CODE_EXTENDER_7_0 = 700;
    //static final int VERSION_CODE_EXTENDER_8_0 = 800;
    static final int VERSION_CODE_EXTENDER_LATEST = 870; //865;      // must be <= as in Extender dependencies.gradle
    static final String VERSION_NAME_EXTENDER_LATEST = "8.1.2"; //"8.1.1"; // must be <= as in Extender dependencies.gradle

    static final int VERSION_CODE_PPPPS_LATEST = 50; //45;          // must be <= as in PPPPS dependencies.gradle
    static final String VERSION_NAME_PPPPS_LATEST = "1.0.5"; //"1.0.4";  // must be <= as in PPPPS dependencies.gradle

    static final int pid = Process.myPid();
    static final int uid = Process.myUid();

    // import/export
    static final String DB_FILEPATH = "/data/" + PPApplication.PACKAGE_NAME + "/databases";
    //static final String REMOTE_EXPORT_PATH = "/PhoneProfiles";
    static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    //static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";
    static final String SHARED_EXPORT_FILENAME = "phoneProfilesPlus_backup";
    static final String SHARED_EXPORT_FILEEXTENSION = ".zip";

    static boolean exportIsRunning = false;

    private static volatile PPApplication instance;
    private static volatile WorkManager workManagerInstance;

    static volatile boolean firstStartAfterInstallation = false;
    static volatile boolean applicationFullyStarted = false;
    static volatile boolean normalServiceStart = false;
    static volatile boolean showToastForProfileActivation = false;

    // this for display of alert dialog when works not started at start of app
    //static long startTimeOfApplicationStart = 0;

    static final long APPLICATION_START_DELAY = 2 * 60 * 1000;
    static final int WORK_PRUNE_DELAY_DAYS = 1;
    static final int WORK_PRUNE_DELAY_MINUTES = 60;

    // urls
    static final String CROWDIN_URL = "https://crowdin.com/project/phoneprofilesplus";

    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/privacy_policy.md
    // Used is GitHub Pages, needed is use of html type, because this file is displayed in html browser
    static final String PRIVACY_POLICY_URL = "https://henrichg.github.io/PhoneProfilesPlus/privacy_policy.html";

    static final String GITHUB_PPP_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlus/releases";
    static final String GITHUB_PPP_DOWNLOAD_URL = "https://github.com/henrichg/PhoneProfilesPlus/releases/latest/download/PhoneProfilesPlus.apk";

    static final String GITHUB_PPPE_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
    static final String GITHUB_PPPE_DOWNLOAD_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases/latest/download/PhoneProfilesPlusExtender.apk";

    static final String GITHUB_PPP_URL = "https://github.com/henrichg/PhoneProfilesPlus";
    static final String GITHUB_PPPE_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender";
    static final String GITHUB_PPPPS_URL = "https://github.com/henrichg/PPPPutSettings";
    static final String XDA_DEVELOPERS_PPP_URL = "https://forum.xda-developers.com/t/phoneprofilesplus.3799429/";

    static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U";

    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/releases_debug.md
    // Used is GitHub Pages, not neded to use html type, this file is directly downloaded
    static final String PPP_RELEASES_DEBUG_URL = "https://henrichg.github.io/PhoneProfilesPlus/releases-debug.md";
    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/releases.md
    // Used is GitHub Pages, not neded to use html type, this file is directly downloaded
    static final String PPP_RELEASES_URL = "https://henrichg.github.io/PhoneProfilesPlus/releases.md";

    static final String FDROID_PPP_RELEASES_URL = "https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus";
    static final String FDROID_APPLICATION_URL = "https://www.f-droid.org/";
    static final String FDROID_REPOSITORY_URL = "https://apt.izzysoft.de/fdroid/index/info";

    //static final String AMAZON_APPSTORE_PPP_RELEASES_URL = "https://www.amazon.com/Henrich-Gron-PhoneProfilesPlus/dp/B01N3SM44J/ref=sr_1_1?keywords=phoneprofilesplus&qid=1637084235&qsid=134-9049988-7816540&s=mobile-apps&sr=1-1&sres=B01N3SM44J%2CB078K93HFD%2CB01LXZDPDR%2CB00LBK7OSY%2CB07RX5L3CP%2CB07XM7WVS8%2CB07XWGWPH5%2CB08KXB3R7S%2CB0919N2P7J%2CB08NWD7K8H%2CB01A7MACL2%2CB07XY8YFQQ%2CB07XM8GDWC%2CB07QVYLDRL%2CB09295KQ9Q%2CB01LVZ3JBI%2CB08723759H%2CB09728VTDK%2CB08R7D4KZJ%2CB01BUIGF9K";
    //static final String AMAZON_APPSTORE_APPLICATION_URL = "https://www.amazon.com/gp/mas/get/amazonapp";

    static final String APKPURE_PPP_RELEASES_URL = "https://m.apkpure.com/p/sk.henrichg.phoneprofilesplus";
    static final String APKPURE_APPLICATION_URL = "https://apkpure.com/apkpure/com.apkpure.aegon";

    static final String HUAWEI_APPGALLERY_PPP_RELEASES_URL = "https://appgallery.cloud.huawei.com/ag/n/app/C104501059?channelId=PhoneProfilesPlus+application&id=957ced9f0ca648df8f253a3d1460051e&s=79376612D7DD2C824692C162FB2F957A7AEE81EE1471CDC58034CD5106DAB009&detailType=0&v=&callType=AGDLINK&installType=0000";
    static final String HUAWEI_APPGALLERY_APPLICATION_URL = "https://consumer.huawei.com/en/mobileservices/appgallery/";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/grant_g1_permission.md
    static final String HELP_HOW_TO_GRANT_G1_URL = "https://henrichg.github.io/PhoneProfilesPlus/grant_g1_permission.html";
    static final String HELP_HOW_TO_GRANT_G1_URL_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/grant_g1_permission.md";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/wifi_scan_throttling.md
    static final String HELP_WIFI_SCAN_THROTTLING = "https://henrichg.github.io/PhoneProfilesPlus/wifi_scan_throttling.html";
    static final String HELP_WIFI_SCAN_THROTTLING_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/wifi_scan_throttling.md";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/airplane_mode_radios_config.md
    static final String HELP_AIRPLANE_MODE_RADIOS_CONFIG = "https://henrichg.github.io/PhoneProfilesPlus/airplane_mode_radios_config.html";
    static final String HELP_AIRPLANE_MODE_RADIOS_CONFIG_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/airplane_mode_radios_config.md";

    static final String DROIDIFY_PPP_RELEASES_URL = "https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus";
    static final String DROIDIFY_APPLICATION_URL = "https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify";

    static final String GITHUB_PPPPS_RELEASES_URL = "https://github.com/henrichg/PPPPutSettings/releases";
    static final String GITHUB_PPPPS_DOWNLOAD_URL = "https://github.com/henrichg/PPPPutSettings/releases/latest/download/PPPPutSettings.apk";

    static final String GALAXY_STORE_PPP_RELEASES_URL = "https://galaxystore.samsung.com/detail/sk.henrichg.phoneprofilesplus";

    //static final boolean gitHubRelease = true;
    //static boolean googlePlayInstaller = false;

    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean logIntoLogCat = true && DebugVersion.enabled;
    //TODO change it back to not log crash for releases
    static final boolean logIntoFile = true;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = false && DebugVersion.enabled;
    static final boolean rootToolsDebug = false;
    static final String logFilterTags = "##### PPApplication.onCreate"
                                                //+"|PPApplication.isXiaomi"
                                                //+"|PPApplication.isHuawei"
                                                //+"|PPApplication.isSamsung"
                                                //+"|PPApplication.isLG"
                                                //+"|PPApplication.getEmuiRomName"
                                                //+"|PPApplication.isEMUIROM"
                                                //+"|PPApplication.isMIUIROM"
                                                //+"|PPApplication.attachBaseContext"
                                                //+"|PPApplication.startPPServiceWhenNotStarted"
                                                +"|PPApplication.exitApp"
                                                +"|PPApplication._exitApp"
                                                //+"|PPApplication.createPPPAppNotificationChannel"
                                                //+"|AvoidRescheduleReceiverWorker"
                                                +"|PhoneProfilesService.onCreate"
                                                +"|PhoneProfilesService.onStartCommand"
                                                +"|PhoneProfilesService.doForFirstStart"
                                                +"|PhoneProfilesService.doForPackageReplaced"
                                                +"|MainWorker.doAfterFirstStart"
                                                //+"|GlobalUtils.getServiceInfo"
                                                //+"|PhoneProfilesService.isServiceRunning"
                                                +"|PackageReplacedReceiver.onReceive"
                                                //+"|PhoneProfilesService.doCommand"
                                                //+"|PPAppNotification.showNotification"
                                                //+"|PPAppNotification._showNotification"
                                                //+"|[CUST] PPAppNotification._showNotification"
                                                //+"|PhoneProfilesService.onConfigurationChanged"
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
                                                //+"|PPApplication.updateGUI"
                                                //+"|DatabaseHandler.onCreate"
                                                //+"|DatabaseHandler.createTableColumsWhenNotExists"
                                                //+"|CustomACRAReportingAdministrator.shouldStartCollecting"
                                                //+"|ImportantInfoNotification"
                                                //+"|ImportantInfoHelpFragment"
// this si for get 0, 50 100% level
                                                +"|SettingsContentObserver.onChange"

//                                                +"|[IN_WORKER]"
//                                                +"|[WORKER_CALL]"
//                                                +"|[IN_EXECUTOR]"
//                                                +"|[EXECUTOR_CALL]"
//                                                +"|[IN_THREAD_HANDLER]"
//                                                +"|[IN_BROADCAST]"
//                                                +"|[IN_BROADCAST_ALARM]"
//                                                +"|[LOCAL_BROADCAST_CALL]"
//                                                +"|[IN_OBSERVER]"
//                                                +"|[IN_LISTENER]"
//                                                +"|[IN_EVENTS_HANDLER]"
//                                                +"|[EVENTS_HANDLER_CALL]"
//                                                +"|[TEST BATTERY]"
//                                                +"|[APP_START]"
//                                                +"|[HANDLER]"
                                                //+"|[SHEDULE_WORK]"
                                                //+"|[SHEDULE_SCANNER]"
                                                //+"|[TEST MEDIA VOLUME]"
                                                //+"|[TEST_BLOCK_PROFILE_EVENTS_ACTIONS]"
                                                //+"|[FIFO_TEST]"
                                                //+"|[BLOCK_ACTIONS]"
                                                //+"|[ACTIVATOR]"
                                                //+"|[G1_TEST]"
                                                //+"|[BACKGROUND_ACTIVITY]"
                                                //+"|[START_PP_SERVICE]"
                                                //+"|[BRS]"
                                                //+"|[CONNECTIVITY_TEST]"
                                                //+"|[BRIGHTNESS]"
                                                //+"|[BRSD]"
                                                //+"|[ROOT]"
                                                //+"|[DB_LOCK]"
                                                //+"|[WIFI]"
                                                //+"|[VOLUMES]"
                                                //+"|[PPP_NOTIFICATION]"
                                                //+"|[DUAL_SIM]"
                                                //+"|[APPLICATION_FULLY_STARTED]"

                                                //+"|EventPreferencesOrientation"
                                                //+"|LocationScanner.updateTransitionsByLastKnownLocation"
                                                //+"|ActivateProfileHelper.changeWallpaperFromFolder"
                                                //+"|AutostartPermissionNotification"
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
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE = 57;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE = 58;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS = 59;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE = 60;

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
    static final int ALTYPE_PROFILE_ERROR_SET_VPN = 1007;
    static final int ALTYPE_PROFILE_ERROR_CAMERA_FLASH = 1008;
    static final int ALTYPE_PROFILE_ERROR_WIFI = 1009;
    static final int ALTYPE_PROFILE_ERROR_WIFIAP = 1010;
    static final int ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS = 1011;

    static final int ALTYPE_DATA_EXPORT = 101;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION = 102;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS = 103;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT = 104;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT = 105;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT = 106;
    static final int ALTYPE_APPLICATION_SYSTEM_RESTART = 107;
    static final int ALTYPE_PROFILE_ADDED = 108;
    static final int ALTYPE_EVENT_ADDED = 109;

    //static final String romManufacturer = getROMManufacturer();
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();
    static final boolean deviceIsLG = isLG();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean deviceIsOppo = isOppo();
    static final boolean deviceIsRealme = isRealme();
    static final boolean deviceIsLenovo = isLenovo();
    static final boolean deviceIsPixel = isPixel();
    static final boolean deviceIsSony = isSony();
    static final boolean deviceIsDoogee = isDoogee();
    static final boolean romIsMIUI = isMIUIROM();
    static final boolean romIsEMUI = isEMUIROM();
    static final boolean romIsGalaxy = isGalaxyROM();

    static volatile boolean HAS_FEATURE_BLUETOOTH_LE = false;
    static volatile boolean HAS_FEATURE_WIFI = false;
    static volatile boolean HAS_FEATURE_BLUETOOTH = false;
    static volatile boolean HAS_FEATURE_TELEPHONY = false;
    static volatile boolean HAS_FEATURE_NFC = false;
    static volatile boolean HAS_FEATURE_LOCATION = false;
    static volatile boolean HAS_FEATURE_LOCATION_GPS = false;
    static volatile boolean HAS_FEATURE_CAMERA_FLASH = false;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";
    static final String PACKAGE_NAME_PP = "sk.henrichg.phoneprofiles";
    static final String PACKAGE_NAME_PPPPS = "sk.henrichg.pppputsettings";

    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_PROFILE_NAME = "profile_name";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_EVENT_STATUS = "event_status";
    static final String EXTRA_APPLICATION_START = "application_start";
    static final String EXTRA_DEVICE_BOOT = "device_boot";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_FOR_FIRST_START = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_EVENT = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    //static final int STARTUP_SOURCE_LAUNCHER_START = 10;
    static final int STARTUP_SOURCE_LAUNCHER = 11;
    static final int STARTUP_SOURCE_EVENT_MANUAL = 12;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 13;
    static final int STARTUP_SOURCE_QUICK_TILE = 14;
    static final int STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER = 15;
    static final int STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER = 16;
    static final int STARTUP_SOURCE_EDITOR_WIDGET_HEADER = 17;

    //static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    //static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_activated_profile";
    // will be deleted if exists
    static final String MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_OLD = "phoneProfilesPlus_mobile_cells_registration";
    static final String MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL_SILENT = "phoneProfilesPlus_mobile_cells_registration_silent";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_grant_permission";
    static final String NOTIFY_EVENT_START_NOTIFICATION_CHANNEL = "phoneProfilesPlus_repeat_notify_event_start";
    static final String NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL = "phoneProfilesPlus_new_mobile_cell";
    static final String DONATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_donation";
    static final String NEW_RELEASE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_newRelease";
    //static final String CRASH_REPORT_NOTIFICATION_CHANNEL = "phoneProfilesPlus_crash_report";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_generatedByProfile";
    static final String KEEP_SCREEN_ON_NOTIFICATION_CHANNEL = "phoneProfilesPlus_keepScreenOn";
    static final String PROFILE_LIST_NOTIFICATION_CHANNEL = "phoneProfilesPlus_profileList";

    static final int PROFILE_NOTIFICATION_ID = 100;
    static final int PROFILE_NOTIFICATION_NATIVE_ID = 500;
    static final String PROFILE_NOTIFICATION_GROUP = PACKAGE_NAME+"_ACTIVATED_PROFILE_NOTIFICATION_GROUP";

    static final int IMPORTANT_INFO_NOTIFICATION_ID = 101;
    static final String IMPORTANT_INFO_NOTIFICATION_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION";
    static final String IMPORTANT_INFO_NOTIFICATION_EXTENDER_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION_EXTENDER";
    static final String IMPORTANT_INFO_NOTIFICATION_PPPPS_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION_PPPPS";
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 102;
    static final String GRANT_PROFILE_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PROFILE_PERMISSIONS_NOTIFICATION";
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 104;
    static final String GRANT_EVENT_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_EVENT_PERMISSIONS_NOTIFICATION";
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 108;
    static final String GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION";
    static final String GRANT_PERMISSIONS_NOTIFICATION_GROUP = PACKAGE_NAME+"_GRANT_PERMISSIONS_NOTIFICATION_GROUP";

    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 109;
    static final int MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID = 117;
    static final String MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG = PACKAGE_NAME+"_MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION";
    static final String MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP = PACKAGE_NAME+"_MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP";

    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 110;
    static final String ABOUT_APPLICATION_DONATE_NOTIFICATION_TAG = PACKAGE_NAME+"_ABOUT_APPLICATION_DONATE_NOTIFICATION";

    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 111;
    static final String ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG = PACKAGE_NAME+"_ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION";
    static final String ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_GROUP = PACKAGE_NAME+"_ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_GROUP";

    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 113;
    static final String PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 114;
    static final String PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 115;
    static final String PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 116;
    static final String PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_ID = 140;
    static final String PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION";
    static final int PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_ID = 141;
    static final String PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_ID = 143;
    static final String PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION";
    static final String PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP";

    static final int IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID = 120;
    static final String IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG = PACKAGE_NAME+"_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION";
    static final int DRAW_OVER_APPS_NOTIFICATION_ID = 121;
    static final String DRAW_OVER_APPS_NOTIFICATION_TAG = PACKAGE_NAME+"_DRAW_OVER_APPS_NOTIFICATION";
    static final int EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_ID = 134;
    static final String EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_TAG = PACKAGE_NAME+"EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION";
    static final int AUTOSTART_PERMISSION_NOTIFICATION_ID = 150;
    static final String AUTOSTART_PERMISSION_NOTIFICATION_TAG = PACKAGE_NAME+"_AUTOSTART_PERMISSION_NOTIFICATION";
    static final int LOCATION_NOT_WORKING_NOTIFICATION_ID = 151;
    static final String LOCATION_NOT_WORKING_NOTIFICATION_TAG = PACKAGE_NAME+"_LOCATION_NOT_WORKING_NOTIFICATION_NOTIFICATION";
    static final String SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP = PACKAGE_NAME+"_SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP";

    static final int CHECK_GITHUB_RELEASES_NOTIFICATION_ID = 122;
    static final String CHECK_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_GITHUB_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID = 124;
    static final String CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_ID = 125;
    static final String CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_ID = 126;
    static final String CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG";
    static final String CHECK_RELEASES_GROUP = PACKAGE_NAME+"_CHECK_RELEASES_GROUP";

    //static final int PROFILE_ACTIVATION_ERROR_NOTIFICATION_ID = 130;
    //static final String PROFILE_ACTIVATION_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_ID = 131;
    static final String PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_ID = 132;
    static final String PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_ID = 133;
    static final String PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_RUN_APPLICATION_APPLICATION_ERROR_NOTIFICATION_ID = 160;
    static final String PROFILE_ACTIVATION_RUN_APPLICATION_APPLICATION_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_RUN_APPLICATION_APPLICATION_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_RUN_APPLICATION_SHORTCUT_ERROR_NOTIFICATION_ID = 161;
    static final String PROFILE_ACTIVATION_RUN_APPLICATION_SHORTCUT_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_RUN_APPLICATION_SHORTCUT_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_RUN_APPLICATION_INTENT_ERROR_NOTIFICATION_ID = 162;
    static final String PROFILE_ACTIVATION_RUN_APPLICATION_INTENT_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_RUN_APPLICATION_INTENT_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_SET_TONE_RINGTONE_ERROR_NOTIFICATION_ID = 163;
    static final String PROFILE_ACTIVATION_SET_TONE_RINGTONE_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_SET_TONE_RINGTONE_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_SET_TONE_NOTIFICATION_ERROR_NOTIFICATION_ID = 164;
    static final String PROFILE_ACTIVATION_SET_TONE_NOTIFICATION_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_SET_TONE_NOTIFICATION_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_SET_TONE_ALARM_ERROR_NOTIFICATION_ID = 165;
    static final String PROFILE_ACTIVATION_SET_TONE_ALARM_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_SET_TONE_ALARM_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_SET_WALLPAPER_ERROR_NOTIFICATION_ID = 166;
    static final String PROFILE_ACTIVATION_SET_WALLPAPER_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_SET_WALLPAPER_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_SET_VPN_ERROR_NOTIFICATION_ID = 167;
    static final String PROFILE_ACTIVATION_SET_VPN_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_SET_VPN_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_CAMERA_FLASH_ERROR_NOTIFICATION_ID = 168;
    static final String PROFILE_ACTIVATION_CAMERA_FLASH_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_CAMERA_FLASH_ERROR_NOTIFICATION";
    static final String PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP";

    static final int KEEP_SCREEN_ON_NOTIFICATION_ID = 142;
    static final String KEEP_SCREEN_ON_NOTIFICATION_TAG = PACKAGE_NAME+"_KEEP_SCREEN_ON_NOTIFICATION";
    static final String KEEP_SCREEN_ON_NOTIFICATION_GROUP = PACKAGE_NAME+"_KEEP_SCREEN_ON_NOTIFICATION_GROUP";

    static final int PROFILE_LIST_NOTIFICATION_ID = 550;
    static final String PROFILE_LIST_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_LIST_NOTIFICATION";
    static final String PROFILE_LIST_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_LIST_NOTIFICATION_GROUP";

    //last notification id = 151

    // notifications have also tag, in it is tag name + profile/event/mobile cells id
    static final int PROFILE_ID_NOTIFICATION_ID = 1000;
    static final int EVENT_ID_NOTIFICATION_ID = 1000;

    static final int NOTIFY_EVENT_START_NOTIFICATION_ID = 1000;
    static final String NOTIFY_EVENT_START_NOTIFICATION_GROUP = PACKAGE_NAME+"_NOTIFY_EVENT_START_NOTIFICATION_GROUP";

    static final int NEW_MOBILE_CELLS_NOTIFICATION_ID = 1000;

    static final int GENERATED_BY_PROFILE_NOTIFICATION_ID = 10000;

    static final String DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION";
    static final String DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION";
    static final String NOTIFY_EVENT_START_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NOTIFY_EVENT_START_NOTIFICATION";
    static final String NEW_MOBILE_CELLS_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NEW_MOBILE_CELLS_NOTIFICATION";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_TAG = PACKAGE_NAME+"_GENERATED_BY_PROFILE_NOTIFICATION_TAG";

    // shared preferences names !!! Configure also in res/xml/phoneprofiles_backup_scheme.xml !!!
    //static final String ACRA_PREFS_NAME = "phone_profiles_plus_acra";
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
    static final String PREF_APPLICATION_STARTED = "applicationStarted";
    static final String PREF_APPLICATION_STOPPING = "applicationStopping";
    static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";
    static final String PREF_DONATION_DONATED = "donation_donated";
    //private static final String PREF_NOTIFICATION_PROFILE_NAME = "notification_profile_name";
    //private static final String PREF_WIDGET_PROFILE_NAME = "widget_profile_name";
    //private static final String PREF_ACTIVITY_PROFILE_NAME = "activity_profile_name";
    static final String PREF_LAST_ACTIVATED_PROFILE = "last_activated_profile";
    static final String PREF_WALLPAPER_CHANGE_TIME = "wallpaper_change_time";

    //static final String BUNDLE_WIDGET_TYPE = PACKAGE_NAME +"_BUNDLE_WIDGET_TYPE";
    //static final int WIDGET_TYPE_ICON = 1;
    //static final int WIDGET_TYPE_ONE_ROW = 2;
    //static final int WIDGET_TYPE_LIST = 3;

    // WorkManager tags
    static final String AFTER_FIRST_START_WORK_TAG = "afterFirstStartWork";
    //static final String PACKAGE_REPLACED_WORK_TAG = "packageReplacedWork";
    static final String AVOID_RESCHEDULE_RECEIVER_WORK_TAG = "avoidRescheduleReceiverWorker";

    // scanner start/stop types
    //static final int SCANNER_START_LOCATION_SCANNER = 1;
    //static final int SCANNER_STOP_LOCATION_SCANNER = 2;
    static final int SCANNER_RESTART_LOCATION_SCANNER = 3;

    //static final int SCANNER_START_ORIENTATION_SCANNER = 4;
    //static final int SCANNER_STOP_ORIENTATION_SCANNER = 5;
    //static final int SCANNER_FORCE_START_ORIENTATION_SCANNER = 5;
    static final int SCANNER_RESTART_ORIENTATION_SCANNER = 6;

    //static final int SCANNER_START_MOBILE_CELLS_SCANNER = 7;
    //static final int SCANNER_STOP_MOBILE_CELLS_SCANNER = 8;
    static final int SCANNER_FORCE_START_MOBILE_CELLS_SCANNER = 9;
    static final int SCANNER_RESTART_MOBILE_CELLS_SCANNER = 10;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 11;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 12;
    static final int SCANNER_RESTART_WIFI_SCANNER = 13;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 14;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 15;
    static final int SCANNER_RESTART_BLUETOOTH_SCANNER = 16;

    //static final int SCANNER_START_TWILIGHT_SCANNER = 17;
    //static final int SCANNER_STOP_TWILIGHT_SCANNER = 18;
    static final int SCANNER_RESTART_TWILIGHT_SCANNER = 19;
    static final int SCANNER_RESTART_PERIODIC_SCANNING_SCANNER = 20;
    static final int SCANNER_RESTART_NOTIFICATION_SCANNER = 21;

    static final int SCANNER_RESTART_ALL_SCANNERS = 50;

    //static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";
    static final String EXTENDER_ACCESSIBILITY_PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender";

    static final String ACTION_PPPEXTENDER_STARTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_PPPEXTENDER_STARTED";
    //static final String ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED";
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
    static final String ACTION_CHECK_REQUIRED_EXTENDER_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES";
    static final String ACTION_CHECK_LATEST_PPPPS_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_LATEST_PPPPS_RELEASES";

    static final String EXTRA_WHAT_FINISH = "what_finish";

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
    static final String EXTRA_BLOCK_PROFILE_EVENT_ACTION = "extra_block_profile_event_actions";

    static final String CRASHLYTICS_LOG_DEVICE_ROOTED = "DEVICE_ROOTED";
    static final String CRASHLYTICS_LOG_DEVICE_ROOTED_WITH = "ROOTED_WITH";
//    static final String CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION = "GOOGLE_PLAY_SERVICES_VERSION";
    static final String CRASHLYTICS_LOG_RESTORE_BACKUP_OK = "RESTORE_BACKUP_OK";

    static final String SYS_PROP_MOD_VERSION = "ro.modversion";

    //public static long lastUptimeTime;
    //public static long lastEpochTime;

    //static volatile boolean doNotShowPPPAppNotification = false;
    static volatile boolean applicationStarted = false;
    static volatile boolean globalEventsRunStop = true;
    //static volatile boolean applicationPackageReplaced = false;
    static volatile boolean deviceBoot = false;

    //static final boolean restoreFinished = true;

    static volatile Collator collator = null;

    static volatile boolean lockRefresh = false;
    //static volatile long lastRefreshOfGUI = 0;
    //static volatile long lastRefreshOfPPPAppNotification = 0;

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
    static final ApplicationStartedMutex applicationStartedMutex = new ApplicationStartedMutex();
    static final ProfileActivationMutex profileActivationMutex = new ProfileActivationMutex();
    static final GlobalEventsRunStopMutex globalEventsRunStopMutex = new GlobalEventsRunStopMutex();
    static final EventsRunMutex eventsRunMutex = new EventsRunMutex();
    static final EventCallSensorMutex eventCallSensorMutex = new EventCallSensorMutex();
    static final EventAccessoriesSensorMutex eventAccessoriesSensorMutex = new EventAccessoriesSensorMutex();
    static final EventWifiSensorMutex eventWifiSensorMutex = new EventWifiSensorMutex();
    static final EventBluetoothSensorMutex eventBluetoothSensorMutex = new EventBluetoothSensorMutex();
    static final ContactsCacheMutex contactsCacheMutex = new ContactsCacheMutex();
    static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    static final RootMutex rootMutex = new RootMutex();
    static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    //static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    static final ShowPPPNotificationMutex showPPPNotificationMutex = new ShowPPPNotificationMutex();
    static final LocationScannerLastLocationMutex locationScannerLastLocationMutex = new LocationScannerLastLocationMutex();
    static final LocationScannerMutex locationScannerMutex = new LocationScannerMutex();
    static final WifiScannerMutex wifiScannerMutex = new WifiScannerMutex();
    static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    static final BluetoothScannerMutex bluetoothScannerMutex = new BluetoothScannerMutex();
    static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    static final BluetoothCLScanMutex bluetoothCLScanMutex = new BluetoothCLScanMutex();
    static final BluetoothLEScanMutex bluetoothLEScanMutex = new BluetoothLEScanMutex();
    static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    static final MobileCellsScannerMutex mobileCellsScannerMutex = new MobileCellsScannerMutex();
    static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    static final TwilightScannerMutex twilightScannerMutex = new TwilightScannerMutex();
    static final NotUnlinkVolumesMutex notUnlinkVolumesMutex = new NotUnlinkVolumesMutex();
    static final EventRoamingSensorMutex eventRoamingSensorMutex = new EventRoamingSensorMutex();
    static final ApplicationCacheMutex applicationCacheMutex = new ApplicationCacheMutex();
    static final ProfileListWidgetDatasetChangedMutex profileListWidgetDatasetChangedMutex = new ProfileListWidgetDatasetChangedMutex();
    static final SamsungEdgeDatasetChangedMutex samsungEdgeDatasetChangedMutex = new SamsungEdgeDatasetChangedMutex();
    static final DashClockWidgetMutex dashClockWidgetMutex = new DashClockWidgetMutex();

    //static PowerManager.WakeLock keepScreenOnWakeLock;

    static volatile ApplicationsCache applicationsCache;
    static volatile ContactsCache contactsCache;
    static volatile ContactGroupsCache contactGroupsCache;

    static volatile KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    static volatile KeyguardManager.KeyguardLock keyguardLock = null;

    //BrightnessView brightnessView = null;
    //BrightnessView screenTimeoutAlwaysOnView = null;

    // constructor has Context as parameter
    // this is OK, ActivateProfileHelper.removeKeepScreenOnView()
    // set it to null
    @SuppressLint("StaticFieldLeak")
    static volatile BrightnessView keepScreenOnView = null;

    // constructor has Context as parameter
    // this is OK, activity will be removed and lockDeviceActivity set to null after destroy of
    // LockDeviceActivity
    //static volatile LockDeviceActivity lockDeviceActivity = null;
    static volatile boolean lockDeviceActivityDisplayed = false;

    static volatile int screenTimeoutWhenLockDeviceActivityIsDisplayed = 0;

//    static int brightnessBeforeScreenOff;
//    static float adaptiveBrightnessBeforeScreenOff;
//    static int brightnessModeBeforeScreenOff;

    // 0 = wait for answer from Extender;
    // 1 = Extender is connected,
    // 2 = Extender is disconnected
    static volatile int accessibilityServiceForPPPExtenderConnected = 2;

    //boolean willBeDoRestartEvents = false;

    static final StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = new StartLauncherFromNotificationReceiver();
    //static final UpdateGUIBroadcastReceiver updateGUIBroadcastReceiver = new UpdateGUIBroadcastReceiver();
    //static final ShowPPPAppNotificationBroadcastReceiver showPPPAppNotificationBroadcastReceiver = new ShowPPPAppNotificationBroadcastReceiver();
    static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();
    static final IconWidgetProvider iconWidgetBroadcastReceiver = new IconWidgetProvider();
    static final OneRowWidgetProvider oneRowWidgetBroadcastReceiver = new OneRowWidgetProvider();
    static final ProfileListWidgetProvider listWidgetBroadcastReceiver = new ProfileListWidgetProvider();
    static final SamsungEdgeProvider edgePanelBroadcastReceiver = new SamsungEdgeProvider();
    static final OneRowProfileListWidgetProvider oneRowProfileListWidgetBroadcastReceiver = new OneRowProfileListWidgetProvider();

    static volatile TimeChangedReceiver timeChangedReceiver = null;
    static volatile StartEventNotificationDeletedReceiver startEventNotificationDeletedReceiver = null;
    static volatile NotUsedMobileCellsNotificationDeletedReceiver notUsedMobileCellsNotificationDeletedReceiver = null;
    static volatile ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    static volatile ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    static volatile InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    static volatile PPAppNotificationDeletedReceiver ppAppNotificationDeletedReceiver = null;
    static volatile KeepScreenOnNotificationDeletedReceiver keepScreenOnNotificationDeletedReceiver = null;
    static volatile ProfileListNotificationDeletedReceiver profileListNotificationDeletedReceiver = null;

    static volatile PhoneCallsListener phoneCallsListenerSIM1 = null;
    static volatile PhoneCallsListener phoneCallsListenerSIM2 = null;
    static volatile PhoneCallsListener phoneCallsListenerDefaul = null;
    static volatile TelephonyManager telephonyManagerSIM1 = null;
    static volatile TelephonyManager telephonyManagerSIM2 = null;
    static volatile TelephonyManager telephonyManagerDefault = null;


    static volatile RingerModeChangeReceiver ringerModeChangeReceiver = null;
    static volatile WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;
    static volatile NotUsedMobileCellsNotificationDisableReceiver notUsedMobileCellsNotificationDisableReceiver = null;
    static volatile DonationBroadcastReceiver donationBroadcastReceiver = null;
    static volatile CheckPPPReleasesBroadcastReceiver checkPPPReleasesBroadcastReceiver = null;
    static volatile CheckCriticalPPPReleasesBroadcastReceiver checkCriticalPPPReleasesBroadcastReceiver = null;
    static volatile CheckOnlineStatusBroadcastReceiver checkOnlineStatusBroadcastReceiver = null;
    static volatile SimStateChangedBroadcastReceiver simStateChangedBroadcastReceiver = null;
    static volatile CheckRequiredExtenderReleasesBroadcastReceiver checkRequiredExtenderReleasesBroadcastReceiver = null;
    static volatile CheckLatestPPPPSReleasesBroadcastReceiver checkLatestPPPPSReleasesBroadcastReceiver = null;

    static volatile BatteryChargingChangedBroadcastReceiver batteryChargingChangedReceiver = null;
    static volatile BatteryLevelChangedBroadcastReceiver batteryLevelChangedReceiver = null;
    static volatile HeadsetConnectionBroadcastReceiver headsetPlugReceiver = null;
    static volatile NFCStateChangedBroadcastReceiver nfcStateChangedBroadcastReceiver = null;
    static volatile DockConnectionBroadcastReceiver dockConnectionBroadcastReceiver = null;
    //static volatile WifiConnectionBroadcastReceiver wifiConnectionBroadcastReceiver = null;
    static volatile WifiNetworkCallback wifiConnectionCallback = null;
    static volatile MobileDataNetworkCallback mobileDataConnectionCallback = null;
    static volatile BluetoothConnectionBroadcastReceiver bluetoothConnectionBroadcastReceiver = null;
    static volatile BluetoothStateChangedBroadcastReceiver bluetoothStateChangedBroadcastReceiver = null;
    static volatile WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    static volatile LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    static volatile AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    //static volatile SMSBroadcastReceiver smsBroadcastReceiver = null;
    //static volatile SMSBroadcastReceiver mmsBroadcastReceiver = null;
    static volatile CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;
    static volatile WifiScanBroadcastReceiver wifiScanReceiver = null;
    static volatile BluetoothScanBroadcastReceiver bluetoothScanReceiver = null;
    static volatile BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = null;
    static volatile PPExtenderBroadcastReceiver pppExtenderBroadcastReceiver = null;
    static volatile PPExtenderBroadcastReceiver pppExtenderForceStopApplicationBroadcastReceiver = null;
    static volatile PPExtenderBroadcastReceiver pppExtenderForegroundApplicationBroadcastReceiver = null;
    static volatile PPExtenderBroadcastReceiver pppExtenderSMSBroadcastReceiver = null;
    static volatile PPExtenderBroadcastReceiver pppExtenderCallBroadcastReceiver = null;
    static volatile EventTimeBroadcastReceiver eventTimeBroadcastReceiver = null;
    static volatile EventCalendarBroadcastReceiver eventCalendarBroadcastReceiver = null;
    static volatile EventDelayStartBroadcastReceiver eventDelayStartBroadcastReceiver = null;
    static volatile EventDelayEndBroadcastReceiver eventDelayEndBroadcastReceiver = null;
    static volatile ProfileDurationAlarmBroadcastReceiver profileDurationAlarmBroadcastReceiver = null;
    static volatile SMSEventEndBroadcastReceiver smsEventEndBroadcastReceiver = null;
    static volatile NFCEventEndBroadcastReceiver nfcEventEndBroadcastReceiver = null;
    static volatile RunApplicationWithDelayBroadcastReceiver runApplicationWithDelayBroadcastReceiver = null;
    static volatile MissedCallEventEndBroadcastReceiver missedCallEventEndBroadcastReceiver = null;
    static volatile StartEventNotificationBroadcastReceiver startEventNotificationBroadcastReceiver = null;
    static volatile LocationScannerSwitchGPSBroadcastReceiver locationScannerSwitchGPSBroadcastReceiver = null;
    static volatile LockDeviceActivityFinishBroadcastReceiver lockDeviceActivityFinishBroadcastReceiver = null;
    static volatile AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    static volatile AlarmClockEventEndBroadcastReceiver alarmClockEventEndBroadcastReceiver = null;
    static volatile NotificationEventEndBroadcastReceiver notificationEventEndBroadcastReceiver = null;
    static volatile LockDeviceAfterScreenOffBroadcastReceiver lockDeviceAfterScreenOffBroadcastReceiver = null;
    //static volatile OrientationEventBroadcastReceiver orientationEventBroadcastReceiver = null;
    static volatile PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    static volatile DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;
    static volatile DeviceBootEventEndBroadcastReceiver deviceBootEventEndBroadcastReceiver = null;
    static volatile CalendarEventExistsCheckBroadcastReceiver calendarEventExistsCheckBroadcastReceiver = null;
    static volatile PeriodicEventEndBroadcastReceiver periodicEventEndBroadcastReceiver = null;
    static volatile DefaultSIMChangedBroadcastReceiver defaultSIMChangedBroadcastReceiver = null;
    //static volatile RestartEventsWithDelayBroadcastReceiver restartEventsWithDelayBroadcastReceiver = null;
    static volatile ActivatedProfileEventBroadcastReceiver activatedProfileEventBroadcastReceiver = null;
    static volatile VPNNetworkCallback vpnConnectionCallback = null;

    static volatile SettingsContentObserver settingsContentObserver = null;

    static volatile MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;

    static volatile ContactsContentObserver contactsContentObserver = null;

    static volatile SensorManager sensorManager = null;
    static volatile Sensor accelerometerSensor = null;
    static volatile Sensor magneticFieldSensor = null;
    static volatile Sensor lightSensor = null;
    static volatile Sensor proximitySensor = null;

    static volatile OrientationScanner orientationScanner = null;
    static volatile boolean mStartedOrientationSensors = false;

    static volatile LocationScanner locationScanner = null;
    static volatile MobileCellsScanner mobileCellsScanner = null;
    static volatile TwilightScanner twilightScanner = null;

    static volatile boolean notificationScannerRunning = false;

    static volatile boolean isCharging = false;
    static volatile int batteryPct = -100;
    static volatile int plugged = -1;

    public volatile static boolean isScreenOn;
    //public static boolean isPowerSaveMode;

    static volatile Location lastLocation = null;

    public volatile static ExecutorService basicExecutorPool = null;
    public volatile static ExecutorService profileActiationExecutorPool = null;
    public volatile static ExecutorService eventsHandlerExecutor = null;
    public volatile static ExecutorService scannersExecutor = null;
    public volatile static ExecutorService playToneExecutor = null;
    public volatile static ScheduledExecutorService disableInternalChangeExecutor = null;
    public volatile static ScheduledExecutorService delayedGuiExecutor = null;
    public volatile static ScheduledExecutorService delayedAppNotificationExecutor = null;
    public volatile static ScheduledExecutorService delayedEventsHandlerExecutor = null;
    public volatile static ScheduledExecutorService delayedProfileActivationExecutor = null;

    // required for callbacks, observers, ...
    public volatile static HandlerThread handlerThreadBroadcast = null;
    // required for sensor manager
    public volatile static OrientationScannerHandlerThread handlerThreadOrientationScanner = null;
    // rewuired for location manager
    public volatile static HandlerThread handlerThreadLocation = null;

    //public static HandlerThread handlerThread = null;
    //public static HandlerThread handlerThreadCancelWork = null;
    //public static HandlerThread handlerThreadWidget = null;
    //public static HandlerThread handlerThreadPlayTone = null;
    //public static HandlerThread handlerThreadPPScanners = null;
    //public static HandlerThread handlerThreadPPCommand = null;

    //public static HandlerThread handlerThreadVolumes = null;
    //public static HandlerThread handlerThreadRadios = null;
    //public static HandlerThread handlerThreadWallpaper = null;
    //public static HandlerThread handlerThreadRunApplication = null;

    //public static HandlerThread handlerThreadProfileActivation = null;

    public volatile static Handler toastHandler;
    //public static Handler brightnessHandler;
    public volatile static Handler screenTimeoutHandler;

    public static final PPNotificationListenerService ppNotificationListenerService = new PPNotificationListenerService();

    //public static boolean isPowerSaveMode = false;

    // !! this must be here
    public volatile static boolean blockProfileEventActions = false;

    // Samsung Look instance
    public volatile static Slook sLook = null;
    public volatile static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    //public static final Random requestCodeForAlarm = new Random();

    static final long[] quickTileProfileId = {0, 0, 0, 0, 0, 0};
    static final QuickTileChooseTileBroadcastReceiver[] quickTileChooseTileBroadcastReceiver =
            {null, null, null, null, null, null};

    static boolean prefActivityLogEnabled;
    static long prefLastActivatedProfile;
    static long wallpaperChangeTime;

    static volatile String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;
    static volatile boolean connectToSSIDStarted = false;

    @Override
    public void onCreate()
    {
        PPApplicationStatic.logE("################# PPApplication.onCreate", "onCreate() start");

        /* Hm this resets start, why?!
        if (DebugVersion.enabled) {
            if (!ACRA.isACRASenderServiceProcess()) {
                PPApplicationStatic.logE("##### PPApplication.onCreate", "strict mode");

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
            }
        }*/

        super.onCreate();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPApplication.onCreate", "ACRA.isACRASenderServiceProcess()");
            return;
        }

        synchronized (PPApplication.applicationStartedMutex) {
            PPApplication.exportIsRunning = false;
        }
        applicationFullyStarted = false;
        normalServiceStart = false;
        showToastForProfileActivation = false;
        instance = this;

        //registerActivityLifecycleCallbacks(PPApplication.this);

        /*try {
            //if (!DebugVersion.enabled) {
            // Obtain the FirebaseAnalytics instance.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            //}
            //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } catch (Exception e) {
            Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
        }*/

        if (checkAppReplacingState()) {
            PPApplicationStatic.logE("##### PPApplication.onCreate", "kill PPApplication - not good");
            return;
        }

        PPApplicationStatic.logE("##### PPApplication.onCreate", "continue onCreate()");

        PPApplicationStatic.createBasicExecutorPool();
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplicationStatic.createScannersExecutor();
        PPApplicationStatic.createPlayToneExecutor();
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplicationStatic.createDelayedShowNotificationExecutor();
        PPApplicationStatic.createDelayedEventsHandlerExecutor();
        PPApplicationStatic.createDelayedProfileActivationExecutor();

        // keep this: it is required to use handlerThreadBroadcast for cal listener
        PPApplicationStatic.startHandlerThreadBroadcast();

        PPApplicationStatic.startHandlerThreadOrientationScanner(); // for seconds interval
        //startHandlerThread(/*"PPApplication.onCreate"*/);
        //startHandlerThreadCancelWork();
        //startHandlerThreadPPScanners(); // for minutes interval
        //startHandlerThreadPPCommand();
        PPApplicationStatic.startHandlerThreadLocation();
        //startHandlerThreadWidget();
        //startHandlerThreadPlayTone();
        //startHandlerThreadVolumes();
        //startHandlerThreadRadios();
        //startHandlerThreadWallpaper();
        //startHandlerThreadRunApplication();
        //startHandlerThreadProfileActivation();

        toastHandler = new Handler(getMainLooper());
        //brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

        PackageManager packageManager = getPackageManager();
        HAS_FEATURE_BLUETOOTH_LE = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH_LE);
        HAS_FEATURE_WIFI = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_WIFI);
        HAS_FEATURE_BLUETOOTH = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH);
        HAS_FEATURE_TELEPHONY = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_TELEPHONY);
        HAS_FEATURE_NFC = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_NFC);
        HAS_FEATURE_LOCATION = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION);
        HAS_FEATURE_LOCATION_GPS = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION_GPS);
        HAS_FEATURE_CAMERA_FLASH = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_CAMERA_FLASH);

        PPApplicationStatic.logE("##### PPApplication.onCreate", "end of get features");

        PPApplicationStatic.createNotificationChannels(getApplicationContext());

        PPApplicationStatic.loadGlobalApplicationData(getApplicationContext());
        PPApplicationStatic.loadApplicationPreferences(getApplicationContext());
        PPApplicationStatic.loadProfileActivationData(getApplicationContext());

        workManagerInstance = WorkManager.getInstance(getApplicationContext());
        PPApplicationStatic.logE("##### PPApplication.onCreate", "workManagerInstance="+workManagerInstance);

        /*
        workManagerInstance.pruneWork();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            int size = jobScheduler.getAllPendingJobs().size();
            PPApplicationStatic.logE("##### PPApplication.onCreate", "jobScheduler.getAllPendingJobs().size()="+size);
            jobScheduler.cancelAll();
        }
        */

        // https://issuetracker.google.com/issues/115575872#comment16
        AvoidRescheduleReceiverWorker.enqueueWork();

//        init() moved to ActivateProfileHelpser.execute();
//        try {
//            NoobCameraManager.getInstance().init(this);
//        } catch (Exception e) {
//            PPApplicationStatic.recordException(e);
//        }

        if (keyguardManager == null)
            keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = PPApplicationStatic.getAccelerometerSensor(getApplicationContext());
        magneticFieldSensor = PPApplicationStatic.getMagneticFieldSensor(getApplicationContext());
        proximitySensor = PPApplicationStatic.getProximitySensor(getApplicationContext());
        lightSensor = PPApplicationStatic.getLightSensor(getApplicationContext());

//        if (lastLocation == null) {
//            lastLocation = new Location("GL");
//        }

        if (PPApplicationStatic.logEnabled()) {
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsXiaomi=" + deviceIsXiaomi);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsHuawei=" + deviceIsHuawei);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsSamsung=" + deviceIsSamsung);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsLG=" + deviceIsLG);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsOnePlus=" + deviceIsOnePlus);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsOppo=" + deviceIsOppo);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsRealme=" + deviceIsRealme);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsLenovo=" + deviceIsLenovo);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsPixel=" + deviceIsPixel);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsSony=" + deviceIsSony);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceIsDoogee=" + deviceIsDoogee);

            PPApplicationStatic.logE("##### PPApplication.onCreate", "romIsMIUI=" + romIsMIUI);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "romIsEMUI=" + romIsEMUI);
            //PPApplicationStatic.logE("##### PPApplication.onCreate", "-- romIsEMUI=" + isEMUIROM());
            //PPApplicationStatic.logE("##### PPApplication.onCreate", "-- romIsMIUI=" + isMIUIROM());
            PPApplicationStatic.logE("##### PPApplication.onCreate", "romIsGalaxy=" + romIsGalaxy);

            PPApplicationStatic.logE("##### PPApplication.onCreate", "manufacturer=" + Build.MANUFACTURER);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "model=" + Build.MODEL);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "display=" + Build.DISPLAY);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "brand=" + Build.BRAND);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "fingerprint=" + Build.FINGERPRINT);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "type=" + Build.TYPE);

            PPApplicationStatic.logE("##### PPApplication.onCreate", "modVersion=" + getReadableModVersion());
            PPApplicationStatic.logE("##### PPApplication.onCreate", "osVersion=" + System.getProperty("os.version"));
            PPApplicationStatic.logE("##### PPApplication.onCreate", "api level=" + Build.VERSION.SDK_INT);

            //if (Build.VERSION.SDK_INT >= 25)
                PPApplicationStatic.logE("##### PPApplication.onCreate", "deviceName="+ Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME));
            PPApplicationStatic.logE("##### PPApplication.onCreate", "release="+ Build.VERSION.RELEASE);

            PPApplicationStatic.logE("##### PPApplication.onCreate", "board="+ Build.BOARD);
            PPApplicationStatic.logE("##### PPApplication.onCreate", "product="+ Build.PRODUCT);
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
        /*if (Build.VERSION.SDK_INT >= 28) {
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
                PPApplicationStatic.recordException(e);
            }
        }*/
        //////////////////////////////////////////

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                //Crashlytics.getInstance().core.logException(error);
                PPApplicationStatic.recordException(error);
            }
        });
        anrWatchDog.start();
        */

        PPApplicationStatic.setCustomKey("FROM_GOOGLE_PLAY", false);
        PPApplicationStatic.setCustomKey("DEBUG", DebugVersion.enabled);

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
//        brightnessModeBeforeScreenOff = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//        brightnessBeforeScreenOff = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//        adaptiveBrightnessBeforeScreenOff = Settings.System.getFloat(getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);


        //isPowerSaveMode = DataWrapper.isPowerSaveMode(getApplicationContext());

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        /*
        JobConfig.setApiEnabled(JobApi.WORK_MANAGER, true);
        //JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        //JobConfig.setApiEnabled(JobApi.GCM, false); // is only important for Android 4.X

        JobManager.create(this).addJobCreator(new PPJobsCreator());
        */

        RootUtils.initRoot();

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
            //Log.e("***** PPApplication.onCreate", "sLookCocktailPanelEnabled="+sLookCocktailPanelEnabled);
            // true = The Device supports Edge Immersive Mode feature.
            //sLookCocktailBarEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_BAR);
        } catch (SsdkUnsupportedException e) {
            sLook = null;
            switch (e.getType()) {
                case SsdkUnsupportedException.VENDOR_NOT_SUPPORTED:
                    //Log.e("***** PPApplication.onCreate", "Look not supported: VENDOR_NOT_SUPPORTED");
                    break;
                case SsdkUnsupportedException.DEVICE_NOT_SUPPORTED:
                    //Log.e("***** PPApplication.onCreate", "Look not supported: DEVICE_NOT_SUPPORTED");
                    break;
                case SsdkUnsupportedException.LIBRARY_NOT_INSTALLED:
                    //Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_NOT_INSTALLED");
                    break;
                case SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED:
                    //Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_UPDATE_IS_REQUIRED");
                    break;
                case SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED:
                    //Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_UPDATE_IS_RECOMMENDED");
                    break;
            }
        }

        startPPServiceWhenNotStarted(this);
    }

    static PPApplication getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
        return instance;
        //}
    }

    @Override
    protected void attachBaseContext(Context base) {
        //super.attachBaseContext(base);
        super.attachBaseContext(LocaleHelper.onAttach(base));
        //Reflection.unseal(base);
        if (Build.VERSION.SDK_INT >= 28) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }

        collator = GlobalUtils.getCollator();
        //MultiDex.install(this);

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPApplication.attachBaseContext", "ACRA.isACRASenderServiceProcess()");
            return;
        }

//        PPApplicationStatic.logE("##### PPApplication.attachBaseContext", "ACRA inittialization");

        String packageVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            packageVersion = " - v" + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
        } catch (Exception ignored) {
        }

        String body;
        //if (Build.VERSION.SDK_INT >= 25)
            body = getString(R.string.important_info_email_body_device) + " " +
                    Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                    " (" + Build.MODEL + ")" + " \n";
        /*else {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer))
                body = getString(R.string.important_info_email_body_device) + " " + model + " \n";
            else
                body = getString(R.string.important_info_email_body_device) + " " + manufacturer + " " + model + " \n";
        }*/
        body = body + getString(R.string.important_info_email_body_android_version) + " " + Build.VERSION.RELEASE + " \n\n";
        body = body + getString(R.string.acra_email_body_text);

/*
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST);
        //builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
        //        .setResText(R.string.acra_toast_text)
        //        .setEnabled(true);
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                .withResChannelName(R.string.notification_channel_crash_report)
                .withResChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                .withResIcon(R.drawable.ic_exclamation_notify)
                .withResTitle(R.string.acra_notification_title)
                .withResText(R.string.acra_notification_text)
                .withResSendButtonIcon(0)
                .withResDiscardButtonIcon(0)
                .withSendOnClick(true)
                .withEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .withMailTo("henrich.gron@gmail.com")
                .withSubject("PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.acra_email_subject_text))
                .withBody(body)
                .withReportAsFile(true)
                .withReportFileName("crash_report.txt")
                .withEnabled(true);
*/

        //noinspection ArraysAsListWithZeroOrOneArgument
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST)
                //.withSharedPreferencesName(ACRA_PREFS_NAME)
                .withAdditionalSharedPreferences(Arrays.asList(APPLICATION_PREFS_NAME));

        builder.withPluginConfigurations(
                new NotificationConfigurationBuilder()
                        .withChannelName(getString(R.string.notification_channel_crash_report))
                        .withChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                        .withResIcon(R.drawable.ic_exclamation_notify)
                        .withTitle(getString(R.string.acra_notification_title))
                        .withText(getString(R.string.acra_notification_text))
                        .withResSendButtonIcon(0)
                        .withResDiscardButtonIcon(0)
                        .withSendOnClick(true)
                        .withColor(ContextCompat.getColor(base, R.color.notification_color))
                        .withEnabled(true)
                        .build(),
                new MailSenderConfigurationBuilder()
                        .withMailTo("henrich.gron@gmail.com")
                        .withSubject("PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.acra_email_subject_text))
                        .withBody(body)
                        .withReportAsFile(true)
                        .withReportFileName("crash_report.txt")
                        .withEnabled(false) // must be false because of custom report sender
                        .build()
        );

        ACRA.DEV_LOGGING = false;

        ACRA.init(this, builder);

        /*
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
        } catch (Exception ignored) {}

        // Look at TopExceptionHandler.uncaughtException() for ignored exceptions
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(base, actualVersionCode));
        //}
        */

    }

    private void startPPServiceWhenNotStarted(final Context appContext) {
        // this is for list widget header

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.startPPServiceWhenNotStarted", "--------------- START");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_startPPServiceWhenNotStarted");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                if (!serviceStarted) {
                    //if (!PPApplicationStatic.getApplicationStarted(false)) {
                    if (ApplicationPreferences.applicationStartOnBoot) {
                        //AutostartPermissionNotification.showNotification(appContext, true);

                        // start PhoneProfilesService
                        //PPApplication.firstStartServiceStarted = false;
                        PPApplicationStatic.setApplicationStarted(appContext, true);
                        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                        PPApplicationStatic.logE("[START_PP_SERVICE] PPApplication.startPPServiceWhenNotStarted", "(1)");
                        PPApplicationStatic.startPPService(appContext, serviceIntent);
                    }
                    //}
                }

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.startPPServiceWhenNotStarted", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startPPServiceWhenNotStarted", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
                //worker.shutdown();
            }
            //}
        };
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.schedule(runnable, 1, TimeUnit.SECONDS);
    }

//    @Override
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

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            try {
                android.os.Process.killProcess(pid);
                PPApplicationStatic.logToACRA("E/PPApplication.checkAppReplacingState: app is replacing...kill");
            } catch (Exception e) {
                //Log.e("PPApplication.checkAppReplacingState", Log.getStackTraceString(e));
            }
            return true;
        }
        return false;
    }

    static void forceUpdateGUI(Context context, boolean alsoEditor, boolean alsoNotification, boolean reloadActivity) {
        // update gui even when app is not fully started
        //if (!PPApplication.applicationFullyStarted)
        //    return;

        // icon widget
        try {
            //IconWidgetProvider myWidget = new IconWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            IconWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        // one row widget
        try {
            OneRowWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        // list widget
        try {
            //ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            ProfileListWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        // one row profile list widget
        try {
            OneRowProfileListWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                //SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                //myWidget.updateWidgets(context, refresh);
                SamsungEdgeProvider.updateWidgets(context/*, true*/);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }

        // dash clock extension
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "dash clock extension)");
        Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
        //intent3.putExtra(DashClockBroadcastReceiver.EXTRA_REFRESH, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "activities");
        Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
        //intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
        intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_RELOAD_ACTIVITY, reloadActivity);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        // dynamic shortcuts
        DataWrapperStatic.setDynamicLauncherShortcuts(context);

        // restart tile - this invoke onStartListening()
        // require in manifest file for TileService this meta data:
        //     <meta-data android:name="android.service.quicksettings.ACTIVE_TILE"
        //         android:value="true" />
        TileService.requestListeningState(context, new ComponentName(context, PPTileService1.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService2.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService3.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService4.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService5.class));

        ProfileListNotification.drawNotification(true, context);

        // notifications generated from profile
        NotificationManager _notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (_notificationManager != null) {
            StatusBarNotification[] notifications = _notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                String tag = notification.getTag();
                if ((tag != null) && tag.contains(PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG)) {
                    if (notification.getId() >= PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID) {
                        long profile_id = notification.getId() - PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID;
                        Profile profile = DatabaseHandler.getInstance(context).getProfile(profile_id, false);
                        if (profile != null) {
                            profile.generateIconBitmap(context, false, 0, false);
                            ActivateProfileHelper.generateNotifiction(context, profile);
                        } else {
                            _notificationManager.cancel(
                                    PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                                    PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile_id);
                        }
                    }
                }
            }
        }

        if (alsoNotification) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.forceUpdateGUI", "call of PPAppNotification.drawNotification");
            sk.henrichg.phoneprofilesplus.PPAppNotification.drawNotification(true, context);
        }
    }

    static void updateGUI(final boolean drawImmediattely, final boolean longDelay, final Context context)
    {
        try {
            final Context appContext = context.getApplicationContext();
            LocaleHelper.setApplicationLocale(appContext);

            if (drawImmediattely) {
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (1)", "call of forceUpdateGUI");

                Runnable runnable = () -> {
//                    long start = System.currentTimeMillis();
//                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- START");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_updateGUI_0");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.forceUpdateGUI(appContext, true, true, false);

//                        long finish = System.currentTimeMillis();
//                        long timeElapsed = finish - start;
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- END - timeElapsed="+timeElapsed);
                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                        //worker.shutdown();
                    }
                };
                PPApplication.delayedGuiExecutor.submit(runnable);
                return;
            }

            int delay = 1;
            if (longDelay)
                delay = 10;

//            PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPApplication.updateGUI", "schedule");

            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
//                long start = System.currentTimeMillis();
//                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- START");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_updateGUI");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (2)", "call of forceUpdateGUI");
                    PPApplication.forceUpdateGUI(appContext, true, false, false);
                    if (longDelay) {
//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (1)", "call of PPAppNotification.forceDrawNotification");
                        sk.henrichg.phoneprofilesplus.PPAppNotification.forceDrawNotification(appContext);
                        ProfileListNotification.forceDrawNotification(appContext);
                    }

//                    long finish = System.currentTimeMillis();
//                    long timeElapsed = finish - start;
//                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- END - timeElapsed="+timeElapsed);
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                    //worker.shutdown();
                }
            };
            PPApplication.delayedGuiExecutor.schedule(runnable, delay, TimeUnit.SECONDS);

            /*
            PPApplication.startHandlerThread();
            final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.postDelayed(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.postDelayed(() -> {
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.updateGUI");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_updateGUI");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // for longDelay=true, redraw also notiification
                        // for longDelay=false, notification redraw is called after this postDelayed()
                        PPApplication.forceUpdateGUI(appContext, true, longDelay);

                    } catch (Exception e) {
    //                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }, delay * 1000L);
            */

            if (!longDelay) {
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (2)", "call of PPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(false, context);
                sk.henrichg.phoneprofilesplus.PPAppNotification.drawNotification(false, context);
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    /*
    static void updateNotificationAndWidgets(boolean refresh, boolean forService, Context context)
    {
        PPAppNotification.showNotification(refresh, forService);
        updateGUI(context, true, refresh);
    }
    */

    static void showToast(final Context context, final String text, final int length) {
        final Context appContext = context.getApplicationContext();
        Handler handler = new Handler(context.getApplicationContext().getMainLooper());
        handler.post(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.showToast");
            try {
                LocaleHelper.setApplicationLocale(appContext);

                //ToastCompat msg = ToastCompat.makeText(appContext, text, length);
                ToastCompat msg = ToastCompat.makeCustom(appContext,
                        R.layout.toast_layout, R.drawable.toast_background,
                        R.id.custom_toast_message, text,
                        length);

                /*
                Toast msg = new Toast(appContext);
                View view = LayoutInflater.from(appContext).inflate(R.layout.toast_layout, null);
                TextView txtMsg = view.findViewById(R.id.custom_toast_message);
                txtMsg.setText(text);
                view.setBackgroundResource(R.drawable.toast_background);
                msg.setView(view);
                msg.setDuration(length);
                */

                msg.show();
            } catch (Exception ignored) {
                //PPApplicationStatic.recordException(e);
            }
        });
    }

    //--------------------------------------------------------------

    // others ------------------------------------------------------------------

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
            PPApplicationStatic.recordException(ex);
        }

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
            PPApplicationStatic.recordException(ex);
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

        return (emuiRomName.length() != 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3");// || "EMUI 2.3".equalsIgnoreCase(emuiRomName);
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    private static String getOneUiVersion() throws Exception {
        //if (!isSemAvailable(getApplicationContext())) {
        //    return ""; // was "1.0" originally but probably just a dummy value for one UI devices
        //}
        //noinspection JavaReflectionMemberAccess
        Field semPlatformIntField = Build.VERSION.class.getDeclaredField("SEM_PLATFORM_INT");
        int version = semPlatformIntField.getInt(null) - 90000;
        if (version < 0) {
            // not one ui (could be previous Samsung OS)
            return "";
        }
        return (version / 10000) + "." + ((version % 10000) / 100);
    }

    /*
    private static boolean isSemAvailable(Context context) {
        return context != null &&
                (context.getPackageManager().hasSystemFeature("com.samsung.feature.samsung_experience_mobile") ||
                        context.getPackageManager().hasSystemFeature("com.samsung.feature.samsung_experience_mobile_lite"));
    }
    */

    private static boolean isGalaxyROM() {
        try {
            //noinspection unused
            String romName = getOneUiVersion();
            /*if (romName.isEmpty())
                return true; // old, non-OneUI ROM
            else
                return true; // OneUI ROM*/
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isLG() {
        return Build.BRAND.equalsIgnoreCase("lge") ||
                Build.MANUFACTURER.equalsIgnoreCase("lge") ||
                Build.FINGERPRINT.toLowerCase().contains("lge");
    }

    private static boolean isOnePlus() {
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

    private static boolean isLenovo() {
        return Build.BRAND.equalsIgnoreCase("lenovo") ||
                Build.MANUFACTURER.equalsIgnoreCase("lenovo") ||
                Build.FINGERPRINT.toLowerCase().contains("lenovo");
    }

    private static boolean isPixel() {
        return Build.BRAND.equalsIgnoreCase("google") ||
                Build.MANUFACTURER.equalsIgnoreCase("google") ||
                Build.FINGERPRINT.toLowerCase().contains("google");
    }

    private static boolean isSony() {
        return Build.BRAND.equalsIgnoreCase("sony") ||
                Build.MANUFACTURER.equalsIgnoreCase("sony") ||
                Build.FINGERPRINT.toLowerCase().contains("sony");
    }

    private static boolean isDoogee() {
        return Build.BRAND.equalsIgnoreCase("doogee") ||
                Build.MANUFACTURER.equalsIgnoreCase("doogee") ||
                Build.FINGERPRINT.toLowerCase().contains("doogee");
    }

    private static String getReadableModVersion() {
        String modVer = getSystemProperty(SYS_PROP_MOD_VERSION);
        return (modVer == null || modVer.length() == 0 ? "Unknown" : modVer);
    }

    private static String getSystemProperty(@SuppressWarnings("SameParameterValue") String propName)
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
            PPApplicationStatic.recordException(ex);
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
                    PPApplicationStatic.recordException(e);
                }
            }
        }
        return line;
    }

    private static boolean hasSystemFeature(PackageManager packageManager, String feature) {
        try {
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    //---------------------------------------------------------------------------------------------

}
