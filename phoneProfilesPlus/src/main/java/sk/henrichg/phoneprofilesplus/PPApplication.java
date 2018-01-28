package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobConfig;
import com.evernote.android.job.JobManager;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

public class PPApplication extends Application {

    static String PACKAGE_NAME;

    static final boolean newExtender = true;
    static final int VERSION_CODE_EXTENDER = 60;

    private static final boolean logIntoLogCat = true;
    private static final boolean logIntoFile = false;
    private static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                         +"|PhoneProfilesService.onCreate"
                                         //+"|PhoneProfilesService.onStartCommand"
                                         //+"|PhoneProfilesService.doForFirstStart"
                                         +"|PhoneProfilesService.showProfileNotification"
                                         +"|PhoneProfilesService.onDestroy"
                                         +"|BootUpReceiver"
                                         +"|PackageReplacedReceiver"
                                         +"|ShutdownBroadcastReceiver"

                                         //+"|GrantPermissionActivity"

                                         //+"|$$$ DataWrapper._activateProfile"

                                         //+"|BillingManager"
                                         //+"|DonationFragment"

                                         //+"|Permissions.grantProfilePermissions"
                                         //+"|Permissions.checkProfileVibrateWhenRinging"
                                         //+"|Permissions.checkVibrateWhenRinging"
                                         /*+"|ActivateProfileHelper.setZenMode"
                                         +"|ActivateProfileHelper.setRingerMode"
                                         +"|ActivateProfileHelper.setVolumes"
                                         +"|ActivateProfileHelper.changeRingerModeForVolumeEqual0"
                                         +"|ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0"
                                         +"|ActivateProfileHelper.setVibrateWhenRinging"*/

                                         //+"|PhoneProfilesPreferencesNestedFragment.onActivityCreated"
                                         //+"|ProfilePreferencesNestedFragment.onActivityCreated"

                                         //+"|Event.notifyEventStart"
                                         //+"|StartEventNotificationBroadcastReceiver"
                                         //+"|StartEventNotificationDeletedReceiver"
                                         //+"|PhoneProfilesService.playNotificationSound"

                                         //+"|ProfileDurationAlarmBroadcastReceiver"
                                         //+"|$$$ DataWrapper._activateProfile"

                                         //+"|PPNotificationListenerService"
                                         //+"|[NOTIF] EventsHandler.handleEvents"
                                         //+"|EventPreferencesNotification"

                                         //+"|[CALL] DataWrapper.doHandleEvents"

                                         //+"|"+CallsCounter.LOG_TAG
                                         //+"|[RJS] PPApplication"
                                         //+"|[RJS] PhoneProfilesService"

                                         //+"|ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.changeRingerModeForVolumeEqual0"
                                         //+"|ActivateProfileHelper.executeForVolumes"
                                         //+"|ActivateProfileHelper.setRingerMode"
                                         //+"|ActivateProfileHelper.setVolumes"
                                         //+"|ActivateProfileHelper.setZenMode"

                                         //+"|ActivateProfileHelper.setAirplaneMode_SDK17"
                                         //+"|ActivateProfileHelper.executeForRadios"
                                         //+"|$$$ WifiAP"


                                         //+"|WifiScanJob"
                                         /*+"|$$$ WifiScanBroadcastReceiver.onReceive"
                                         +"|----- DataWrapper.doHandleEvents"
                                         */
                                         //+"|GeofenceScanner"
                                         //+"|GeofenceScannerJob"

                                         //+"|%%%%%%% DataWrapper.doHandleEvents"
                                         //+"|[BTScan] DataWrapper.doHandleEvents"
                                         //+"|BluetoothConnectedDevices"
                                         //+"|BluetoothConnectionBroadcastReceiver"
                                         //+"|BluetoothStateChangedBroadcastReceiver"
                                         //+"|BluetoothScanBroadcastReceiver"
                                         //+"|BluetoothScanJob"

                                         //+"|[RJS] PhoneProfilesService.registerForegroundApplicationChangedReceiver"
                                         //+"|PhoneProfilesService.runEventsHandlerForOrientationChange"
                                         //+"|PhoneProfilesService.onSensorChanged"
                                         //+"|ForegroundApplicationChangedBroadcastReceiver"

                                         /*
                                         +"|PhoneProfilesService.doSimulatingRingingCall"
                                         +"|PhoneProfilesService.startSimulatingRingingCall"
                                         +"|PhoneProfilesService.stopSimulatingRingingCall"
                                         +"|PhoneProfilesService.onAudioFocusChange"
                                         +"|ActivateProfileHelper.(s)setRingerMode"
                                         +"|ActivateProfileHelper.(s)setZenMode"
                                         //+"|@@@ EventsHandler.handleEvents"
                                         +"|EventsHandler.doEndService"
                                         */

                                         //+"|$$$ WifiAP"

                                         //+"|BatteryBroadcastReceiver.onReceive"
                                         //+"|PowerSaveModeBroadcastReceiver.onReceive"
            ;


    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    private static final String LOG_FILENAME = "log.txt";

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
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    static final int PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE = 3;

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
    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 700432;
    static final int EVENT_START_NOTIFICATION_ID = 700433;
    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 700434;
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 700435;
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 700436;

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";

    //public static final String RESCAN_TYPE_SCREEN_ON = "1";
    public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";


    public static final int PREFERENCE_NOT_ALLOWED = 0;
    public static final int PREFERENCE_ALLOWED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 0;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 2;
    public static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 3;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 4;
    private static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 5;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 6;
    private static final int PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED = 7;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION = 8;

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";


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

    static final int SCANNER_RESTART_ALL_SCANNERS = 50;

    public static HandlerThread handlerThread = null;

    public static Handler toastHandler;
    public static Handler brightnessHandler;
    public static Handler screenTimeoutHandler;

    public static int notAllowedReason;
    public static String notAllowedReasonDetail;

    public static final StartRootCommandMutex startRootCommandMutex = new StartRootCommandMutex();
    //public static final RootMutex rootMutex = new RootMutex();
    public static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    public static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    public static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    public static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    public static final GeofenceScannerLastLocationMutex geofenceScannerLastLocationMutex = new GeofenceScannerLastLocationMutex();
    public static final GeofenceScannerMutex geofenceScannerMutex = new GeofenceScannerMutex();
    public static final WifiBluetoothScannerMutex wifiBluetoothscannerMutex = new WifiBluetoothScannerMutex();
    public static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    public static final PhoneStateScannerMutex phoneStateScannerMutex = new PhoneStateScannerMutex();
    public static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    public static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    public static final BluetoothLEScanResultsMutex bluetoothLEScanResultsMutex = new BluetoothLEScanResultsMutex();

    //public static boolean isPowerSaveMode = false;

    public static boolean startedOnBoot = false;

    public static LockDeviceActivity lockDeviceActivity = null;
    public static int screenTimeoutBeforeDeviceLock = 0;

    // Samsung Look instance
    public static Slook sLook = null;
    public static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    // this refresh GUI, must by called from GUI thread no IntentService, Job
    public static final RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver();
    public static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPApplication.logE("##### PPApplication.onCreate", "xxx");

        if (checkAppReplacingState())
            return;

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(getApplicationContext(), crashlyticsKit);
        // Crashlytics.logException(exception); -- this log will be associated with crash log.

        //if (BuildConfig.DEBUG) {
        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            actualVersionCode = pInfo.versionCode;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(getApplicationContext(), actualVersionCode));
        //}

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        PACKAGE_NAME = this.getPackageName();

        startHandlerThread();

        JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        JobManager.create(this).addJobCreator(new PPJobsCreator());

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

        /*
        toastHandler = new HandlerWithContext(getMainLooper(), getApplicationContext());
        brightnessHandler = new HandlerWithContext(getMainLooper(), getApplicationContext());
        screenTimeoutHandler = new HandlerWithContext(getMainLooper(), getApplicationContext());
        */
        toastHandler = new Handler(getMainLooper());
        brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

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

    static void startHandlerThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread");
            handlerThread.start();
        }
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

    @SuppressWarnings("UnusedAssignment")
    @SuppressLint("SimpleDateFormat")
    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        try
        {
            // warnings when logIntoFile == false
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

    public static String intentToString(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.toString() + " " + bundleToString(intent.getExtras());
    }

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

    //--------------------------------------------------------------

    static public boolean getApplicationStarted(Context context, boolean testService)
    {
        ApplicationPreferences.getSharedPreferences(context);
        if (testService)
            return ApplicationPreferences.preferences.getBoolean(PREF_APPLICATION_STARTED, false) && (PhoneProfilesService.instance != null);
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
            case PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED:
                return context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
            case PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_ANDROID_VERSION:
                return context.getString(R.string.preference_not_allowed_reason_not_supported_android_version) + " (" + notAllowedReasonDetail + ")";
            default: return context.getString(R.string.empty_string);
        }
    }

    // --------------------------------
    // root ------------------------------------------

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
    static private ArrayList<Pair> serviceList = null;

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
            try {
                PPApplication.logE("PPApplication._isRooted", "start isRootAvailable");
                //rootChecking = true;
            /*try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
                //if (RootTools.isRootAvailable()) {
                if (RootToolsSmall.isRooted()) {
                    // device is rooted
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
            } catch (Exception e) {
                Log.e("PPApplication._isRooted", "Error on run su: " + e.toString());
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
                try {
                    synchronized (PPApplication.startRootCommandMutex) {
                        PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                        //grantChecking = true;
                        /*try {
                            RootTools.closeAllShells();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        if (RootTools.isAccessGiven()) {
                            // root is granted
                            PPApplication.logE("PPApplication.isRootGranted", "root granted");
                            return true;
                        } else {
                            // grant denied
                            PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                            return false;
                        }
                    }
                } catch (Exception e) {
                    Log.e("PPApplication.isRootGranted", "Error on run su: " + e.toString());
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

                    /*
                    // 4.4+ builds are enforcing by default, take the gamble
                    if (!enforcing)
                    {
                        enforcing = (android.os.Build.VERSION.SDK_INT >= 19);
                    }
                    */
                //}

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
            //e.printStackTrace();
            return null;
        }
    }

    static void getServicesList() {
        synchronized (PPApplication.startRootCommandMutex) {
            if (serviceList == null)
                serviceList = new ArrayList<>();
            else
                serviceList.clear();

            final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");
            Command command = new Command(0, false, "service list") {
                @Override
                public void commandOutput(int id, String line) {
                    //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - line="+line);
                    Matcher matcher = compile.matcher(line);
                    if (matcher.find()) {
                        //noinspection unchecked
                        serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(1)="+matcher.group(1));
                        //PPApplication.logE("$$$ WifiAP", "PhoneProfilesService.getServicesList - matcher.group(2)="+matcher.group(2));
                    }
                    super.commandOutput(id, line);
                }
            };
            try {
                //RootTools.closeAllShells();
                RootTools.getShell(false).add(command);
                commandWait(command);
            } catch (Exception e) {
                Log.e("PPApplication.getServicesList", "Error on run su");
            }
        }
    }

    static Object getServiceManager(String serviceType) {
        if (serviceList != null) {
            for (Pair pair : serviceList) {
                if (serviceType.equals(pair.first)) {
                    return pair.second;
                }
            }
        }
        return null;
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
                    if (name == null || !name.equals("TRANSACTION_" + method)) {
                        iField++;
                    } else {
                        try {
                            field.setAccessible(true);
                            code = field.getInt(field);
                            break;
                        } catch (Exception e) {
                            Log.e("PPApplication.getTransactionCode", e.toString());
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("PPApplication.getTransactionCode", e.toString());
        }
        return code;
    }

    static String getServiceCommand(String serviceType, int transactionCode, Object... params) {
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

    private static void commandWait(Command cmd) /*throws Exception*/ {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 msec (3200 * 2 - 50)

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
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
            Log.e("PPApplication.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForWifiScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForWifiScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.reregisterReceiversForWifiScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void restartWifiScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.forceRegisterReceiversForBluetoothScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.reregisterReceiversForBluetoothScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void restartBluetoothScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartBluetoothScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    /*
    public static void startGeofenceScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.startGeofenceScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_GEOFENCE_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    /*
    private static void stopGeofenceScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.stopGeofenceScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_GEOFENCE_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_CLEAR_SERVICE_FOREGROUND, true);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    public static void restartGeofenceScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartGeofenceScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_GEOFENCE_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    /*
    public static void startOrientationScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.startOrientationScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_ORIENTATION_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    /*
    private static void stopOrientationScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.stopOrientationScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_ORIENTATION_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_CLEAR_SERVICE_FOREGROUND, true);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    public static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartOrientationScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true/*forScreenOn*/);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    /*
    public static void startPhoneStateScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.startPhoneStateScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_PHONE_STATE_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    /*
    private static void stopPhoneStateScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PPApplication.stopPhoneStateScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_PHONE_STATE_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_CLEAR_SERVICE_FOREGROUND, true);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }
    */

    public static void forceStartPhoneStateScanner(Context context) {
        try {
            PPApplication.logE("[RJS] PhoneProfilesService.forceStartPhoneStateScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_PHONE_STATE_SCANNER);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void restartPhoneStateScanner(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartPhoneStateScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PHONE_STATE_SCANNER);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    public static void restartAllScanners(Context context, boolean forScreenOn) {
        try {
            PPApplication.logE("[RJS] PPApplication.restartWifiScanner", "xxx");
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            lIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            context.startService(lIntent);
            //else
            //    context.startForegroundService(lIntent);
        } catch (Exception ignored) {}
    }

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    public static void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

    public static String getROMManufacturer() {
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

    static boolean hasSystemFeature(Context context, String feature) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    public static void exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown) {
        try {
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.stopAllEventsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (!shutdown) {
                        // stop all events
                        dataWrapper.stopAllEvents(false, false);

                        // remove notifications
                        ImportantInfoNotification.removeNotification(context);
                        Permissions.removeNotifications(context);

                        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONEXIT, null, null, null, 0);

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
                                    ActivateProfileHelper.screenTimeoutUnlock(context);
                                    ActivateProfileHelper.removeBrightnessView(context);

                                }
                            });
                        }

                        PPApplication.initRoot();
                    }

                    ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                    Profile.setActivatedProfileForDuration(context, 0);
                    StartEventNotificationBroadcastReceiver.removeAlarm(context);
                    GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
                    LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

                    context.stopService(new Intent(context, PhoneProfilesService.class));

                    Permissions.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
                    Permissions.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
                    Permissions.setShowRequestDrawOverlaysPermission(context.getApplicationContext(), true);
                    WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true);
                    //ActivateProfileHelper.setScreenUnlocked(context, true);

                    PPApplication.setApplicationStarted(context, false);

                    if ((wakeLock != null) && wakeLock.isHeld())
                        wakeLock.release();
                }
            });

            if (!shutdown) {
                if (activity != null) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            activity.finish();
                        }
                    };
                    _handler.postDelayed(r, 500);
                }
            }
        } catch (Exception ignored) {

        }
    }

}
