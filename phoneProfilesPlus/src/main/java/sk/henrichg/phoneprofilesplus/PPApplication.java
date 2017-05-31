package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.stericson.RootShell.RootShell;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class PPApplication extends Application {

    static String PACKAGE_NAME;

    private static boolean logIntoLogCat = true;
    private static boolean logIntoFile = false;
    private static boolean rootToolsDebug = false;
    public static String logFilterTags =  "PhoneProfilesHelper.doUninstallPPHelper"
                                         +"|PhoneProfilesHelper.isPPHelperInstalled"

                                         //+"|##### PPApplication.onCreate"
                                         +"|PPApplication._isRooted"
                                         +"|PPApplication.isRootGranted"

                                         +"|PreferenceFragment"

                                         //+"|----- DataWrapper.doEventService"
                                         +"|@@@ PackageReplacedReceiver.onReceive"

                                         /*
                                         +"|PhoneProfilesService.doSimulatingRingingCall"
                                         +"|PhoneProfilesService.startSimulatingRingingCall"
                                         +"|PhoneProfilesService.stopSimulatingRingingCall"
                                         +"|PhoneProfilesService.onAudioFocusChange"
                                         +"|PhoneProfilesService.doSimulatingNotificationTone"
                                         +"|PhoneProfilesService.startSimulatingNotificationTone"
                                         +"|PhoneProfilesService.stopSimulatingNotificationTone"
                                         */

                                         //+"|DatabaseHandler.changePictureFilePathToUri"
                                         //+"|ImageViewPreference.getImageContentUri"

                                         //+"|PhoneProfilesService.doSimulatingRingingCall"
                                        /*
                                         +"|WifiScanAlarmBroadcastReceiver.onReceive"
                                         +"|WifiScanAlarmBroadcastReceiver.setAlarm"
                                         +"|WifiScanAlarmBroadcastReceiver.removeAlarm"
                                         +"|WifiScanAlarmBroadcastReceiver.isAlarmSet"
                                         +"|WifiScanAlarmBroadcastReceiver.startScan"
                                         +"|WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan"
                                         +"|WifiScanAlarmBroadcastReceiver.fillScanResults"
                                         +"|WifiScanBroadcastReceiver.onReceive"
                                         +"|WifiStateChangedBroadcastReceiver.onReceive"
                                         +"|$$$W ScannerService.onHandleIntent"
                                         +"|ScannerService.enableWifi"
                                         +"|StartEventsServiceBroadcastReceiver.onReceive"
                                         +"|#### EventsService.onHandleIntent"
                                         +"|@@@ ScreenOnOffBroadcastReceiver.onReceive"
                                         +"|$$$ DataWrapper.restartEventsWithRescan"
                                         +"|$$$ ScannerService.lock"
                                         +"|$$$ ScannerService.unlock"
                                        */
                                         //+"|Profile.convertPercentsToBrightnessManualValue"

                                         //+"|##### RefreshGUIBroadcastReceiver.onReceive"

                                         /*
                                         +"|PhoneProfilesService.doSimulatingRingingCall"
                                         +"|PhoneProfilesService.startSimulatingRingingCall"
                                         +"|PhoneProfilesService.stopSimulatingRingingCall"
                                         */
            ;


    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    public static final String LOG_FILENAME = "log.txt";

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
    static final int PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE = 3;

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

    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    static final String DEFAULT_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";

    //public static final String RESCAN_TYPE_NONE = "0";
    public static final String RESCAN_TYPE_SCREEN_ON = "1";
    public static final String RESCAN_TYPE_RESTART_EVENTS = "2";
    public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";


    public static final int PREFERENCE_NOT_ALLOWED = 0;
    public static final int PREFERENCE_ALLOWED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_NO_HARDWARE = 0;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_ROOTED = 1;
    public static final int PREFERENCE_NOT_ALLOWED_SETTINGS_NOT_FOUND = 2;
    public static final int PREFERENCE_NOT_ALLOWED_SERVICE_NOT_FOUND = 3;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM = 4;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_CONFIGURED_IN_SYSTEM_SETTINGS = 5;
    public static final int PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_APPLICATION = 6;

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";


    // scanner start/stop types
    static final int SCANNER_START_GEOFENCE_SCANNER = 1;
    static final int SCANNER_STOP_GEOFENCE_SCANNER = 2;
    static final int SCANNER_START_ORIENTATION_SCANNER = 3;
    static final int SCANNER_STOP_ORIENTATION_SCANNER = 4;
    static final int SCANNER_START_PHONE_STATE_SCANNER = 5;
    static final int SCANNER_STOP_PHONE_STATE_SCANNER = 6;

    public static int notAllowedReason;
    public static String notAllowedReasonDetail;

    public static final StartRootCommandMutex startRootCommandMutex = new StartRootCommandMutex();
    public static final RefreshActivatorListMutex refreshActivatorListMutex = new RefreshActivatorListMutex();
    public static final RefreshEditorProfilesListMutex refreshEditorProfilesListMutex = new RefreshEditorProfilesListMutex();
    public static final RefreshEditorEventsListMutex refreshEditorEventsListMutex = new RefreshEditorEventsListMutex();
    //public static final RootMutex rootMutex = new RootMutex();
    public static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    public static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    public static final NotificationsChangeMutex notificationsChangeMutex = new NotificationsChangeMutex();
    public static final ScanResultsMutex scanResultsMutex = new ScanResultsMutex();

    public static boolean isPowerSaveMode = false;

    public static Notification phoneProfilesNotification = null;

    public static boolean startedOnBoot = false;

    public static LockDeviceActivity lockDeviceActivity = null;
    public static int screenTimeoutBeforeDeviceLock = 0;

    @Override
    public void onCreate()
    {
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(getApplicationContext(), crashlyticsKit);
        // Crashlytics.logException(exception); -- this log will be associated with crash log.

        if (BuildConfig.DEBUG) {
            int actualVersionCode = 0;
            try {
                PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }
            Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(/*getApplicationContext(), */actualVersionCode));
        }

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        super.onCreate();

        PPApplication.logE("##### PPApplication.onCreate", "xxx");

        //firstStartServiceStarted = false;

        PACKAGE_NAME = this.getPackageName();

        // initialization
        //loadPreferences(this);

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

        try
        {
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

    static public void logI(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.i(tag, text);
            logIntoFile("I", tag, text);
        }
    }

    static public void logW(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.w(tag, text);
            logIntoFile("W", tag, text);
        }
    }

    static public void logE(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.e(tag, text);
            logIntoFile("E", tag, text);
        }
    }

    static public void logD(String tag, String text)
    {
        if (!(logIntoLogCat || logIntoFile))
            return;

        if (logContainsFilterTag(tag))
        {
            if (logIntoLogCat) Log.d(tag, text);
            logIntoFile("D", tag, text);
        }
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

    static public int getDaysAfterFirtsStart(Context context)
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
                    // zariadenie je rootnute
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
                    PPApplication.logE("PPApplication.isRootGranted", "start isAccessGiven");
                    //grantChecking = true;
                    /*try {
                        RootTools.closeAllShells();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    if (RootTools.isAccessGiven()) {
                        // root grantnuty
                        PPApplication.logE("PPApplication.isRootGranted", "root granted");
                        return true;
                    } else {
                        // grant odmietnuty
                        PPApplication.logE("PPApplication.isRootGranted", "root NOT granted");
                        return false;
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
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
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
                }

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
    
    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; // 6350 msec (3200 * 2 - 50)

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
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
            Log.e("GlobaData.commandWait", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
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

    //------------------------------------------------------------

    // scanners ------------------------------------------

    public static void startGeofenceScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_GEOFENCE_SCANNER);
        context.startService(lIntent);
    }

    public static void stopGeofenceScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_GEOFENCE_SCANNER);
        context.startService(lIntent);
    }

    public static void startOrientationScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_ORIENTATION_SCANNER);
        context.startService(lIntent);
    }

    public static void stopOrientationScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_ORIENTATION_SCANNER);
        context.startService(lIntent);
    }

    public static void startPhoneStateScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_START_PHONE_STATE_SCANNER);
        context.startService(lIntent);
    }

    public static void stopPhoneStateScanner(Context context) {
        Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
        lIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_STOP_PHONE_STATE_SCANNER);
        context.startService(lIntent);
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
            Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
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

}
