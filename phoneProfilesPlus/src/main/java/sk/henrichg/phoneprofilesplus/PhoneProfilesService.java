package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.internal.telephony.TelephonyIntents;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PhoneProfilesService extends Service
{
    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    //private static boolean isInForeground = false;

    // must be in PPService !!!
    //static boolean startForegroundNotification = true;

    static final String ACTION_COMMAND = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_COMMAND";
    //private static final String ACTION_STOP = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_STOP_SERVICE";
    static final String ACTION_EVENT_TIME_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventTimeBroadcastReceiver";
    static final String ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventCalendarBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventDelayStartBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventDelayEndBroadcastReceiver";
    static final String ACTION_PROFILE_DURATION_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ProfileDurationAlarmBroadcastReceiver";
    static final String ACTION_SMS_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".SMSEventEndBroadcastReceiver";
    static final String ACTION_NFC_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".NFCEventEndBroadcastReceiver";
    static final String ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".RunApplicationWithDelayBroadcastReceiver";
    static final String ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".MissedCallEventEndBroadcastReceiver";
    static final String ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".StartEventNotificationBroadcastReceiver";
    static final String ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".LocationScannerSwitchGPSBroadcastReceiver";
    static final String ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".LockDeviceActivityFinishBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".AlarmClockBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".AlarmClockEventEndBroadcastReceiver";
    static final String ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".NotificationEventEndBroadcastReceiver";
    //private static final String ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".OrientationEventBroadcastReceiver";
    static final String ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".DeviceBootEventEndBroadcastReceiver";
    static final String ACTION_CALENDAR_EVENT_EXISTS_CHECK_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".CalendarEventExistsCheckBroadcastReceiver";
    static final String ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".PeriodicEventEndBroadcastReceiver";
    static final String ACTION_RESTART_EVENTS_WITH_DELAY_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".RestartEventsWithDelayBroadcastReceiver";
    static final String ACTION_ACTIVATED_PROFILE_EVENT_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ActivatedProfileEventBroadcastReceiver";

    //static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    //static final String EXTRA_ONLY_START = "only_start";
    //static final String EXTRA_DEACTIVATE_PROFILE = "deactivate_profile";
    static final String EXTRA_ACTIVATE_PROFILES = "activate_profiles";
    //static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    //static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    //static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";

    static final String EXTRA_SIMULATE_RINGING_CALL = "simulate_ringing_call";
    static final String EXTRA_OLD_RINGER_MODE = "old_ringer_mode";
    //static final String EXTRA_OLD_SYSTEM_RINGER_MODE = "old_system_ringer_mode";
    static final String EXTRA_OLD_ZEN_MODE = "old_zen_mode";
    static final String EXTRA_OLD_RINGTONE = "old_ringtone";
    static final String EXTRA_OLD_RINGTONE_SIM1 = "old_ringtone_sim1";
    static final String EXTRA_OLD_RINGTONE_SIM2 = "old_ringtone_sim2";

    static final String EXTRA_NEW_RINGER_MODE = "new_ringer_mode";
    static final String EXTRA_NEW_ZEN_MODE = "new_zen_mode";
    static final String EXTRA_NEW_RINGER_VOLUME = "new_ringer_volume";
    static final String EXTRA_NEW_RINTONE_CHANGE = "new_ringtone_change";
    static final String EXTRA_NEW_RINGTONE = "new_ringtone";
    static final String EXTRA_NEW_RINTONE_CHANGE_SIM1 = "new_ringtone_change_sim1";
    static final String EXTRA_NEW_RINGTONE_SIM1 = "new_ringtone_sim1";
    static final String EXTRA_NEW_RINTONE_CHANGE_SIM2 = "new_ringtone_change_sim1";
    static final String EXTRA_NEW_RINGTONE_SIM2 = "new_ringtone_sim2";

    static final String EXTRA_CALL_FROM_SIM_SLOT = "call_from sim_slot";

    //static final String EXTRA_SIMULATE_NOTIFICATION_TONE = "simulate_notification_tone";
    //static final String EXTRA_OLD_NOTIFICATION_TONE = "old_notification_tone";
    //static final String EXTRA_OLD_SYSTEM_RINGER_VOLUME = "old_system_ringer_volume";
    static final String EXTRA_REGISTER_RECEIVERS_AND_WORKERS = "register_receivers_and_workers";
    static final String EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS = "unregister_receivers_and_workers";
    static final String EXTRA_REREGISTER_RECEIVERS_AND_WORKERS = "reregister_receivers_and_workers";
    static final String EXTRA_REGISTER_CONTENT_OBSERVERS = "register_content_observers";
    static final String EXTRA_REGISTER_CALLBACKS = "register_callbacks";
    static final String EXTRA_REGISTER_PHONE_CALLS_LISTENER = "register_phone_calls_listener";
    static final String EXTRA_FROM_BATTERY_CHANGE = "from_battery_change";
    //static final String EXTRA_START_LOCATION_UPDATES = "start_location_updates";
    //private static final String EXTRA_STOP_LOCATION_UPDATES = "stop_location_updates";
    //static final String EXTRA_RESTART_EVENTS = "restart_events";
    static final String EXTRA_ALSO_RESCAN = "also_rescan";
    static final String EXTRA_UNBLOCK_EVENTS_RUN = "unblock_events_run";
    //static final String EXTRA_REACTIVATE_PROFILE = "reactivate_profile";
    static final String EXTRA_LOG_TYPE = "log_type";
    //static final String EXTRA_DELAYED_WORK = "delayed_work";
    static final String EXTRA_SENSOR_TYPE = "sensor_type";
    //static final String EXTRA_ELAPSED_ALARMS_WORK = "elapsed_alarms_work";
    //static final String EXTRA_FROM_DO_FIRST_START = "from_do_first_start";
    static final String EXTRA_START_FOR_EXTERNAL_APPLICATION = "start_for_external_application";
    static final String EXTRA_START_FOR_EXTERNAL_APP_ACTION = "start_for_external_app_action";
    static final String EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE = "start_for_external_app_data_type";
    static final String EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE = "start_for_external_app_data_value";
    static final String EXTRA_RESCAN_SCANNERS = "rescan_scanners";
    //static final String EXTRA_SHOW_TOAST = "show_toast";

    static final int START_FOR_EXTERNAL_APP_PROFILE = 1;
    static final int START_FOR_EXTERNAL_APP_EVENT = 2;

    //------------------------

    private static volatile ApplicationsCache applicationsCache;
    static private volatile ContactsCache contactsCache;
    static private volatile ContactGroupsCache contactGroupsCache;

    private AudioManager audioManager = null;
    static private volatile boolean ringingCallIsSimulating = false;
    //private boolean notificationToneIsSimulating = false;
    int ringingVolume = 0;
    static volatile int ringingMuted = 0;
    //public static int notificationVolume = 0;
    static private volatile int oldVolumeForRingingSimulation = -1;
    static private volatile MediaPlayer ringingMediaPlayer = null;
    //private MediaPlayer notificationMediaPlayer = null;
    //private int mediaRingingVolume = 0;
    //private int mediaNotificationVolume = 0;
    //private int usedRingingStream = AudioManager.STREAM_MUSIC;
    //private int usedNotificationStream = AudioManager.STREAM_MUSIC;

    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    //private int oldNotificationVolume = 0;
    private Timer notificationPlayTimer = null;
    //private int oldVolumeForPlayNotificationSound = 0;

    String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;
    boolean connectToSSIDStarted = false;

    //--------------------------

    private final BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplication.logE("[IN_BROADCAST] PhoneProfilesService.commandReceiver", "xxx");
            doCommand(intent);
        }
    };

    /*
    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //PPApplication.logE("PhoneProfilesService.stopReceiver", "xxx");
            try {
                context.removeStickyBroadcast(intent);
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stopReceiver", Log.getStackTraceString(e));
                //PPApplication.recordException(e);
            }
            try {
                isInForeground = false;
                stopForeground(true);
                stopSelf();
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stopReceiver", Log.getStackTraceString(e));
                //PPApplication.recordException(e);
            }
        }
    };
    */

    //--------------------------

    //public static SipManager mSipManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "android.os.Build.VERSION.SDK_INT=" + android.os.Build.VERSION.SDK_INT);

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = this;
        }
        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OLD serviceHasFirstStart=" + serviceHasFirstStart);
        serviceHasFirstStart = false;

        //PPApplication.accessibilityServiceForPPPExtenderConnected = 2;

        //startForegroundNotification = true;
        //isInForeground = false;

        if (PPApplication.getInstance() == null) {
            PPApplication.loadApplicationPreferences(getApplicationContext());
        }

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "before show profile notification");

        boolean isServiceRunning = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, true);
        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "------- service is running (in foreground)="+isServiceRunning);

        /*
        // delete notification if is displayed
        PPApplication.cancelWork(ShowProfileNotificationWorker.WORK_TAG, true);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            } catch (Exception ignored) {}
            try {
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_NATIVE_ID);
            } catch (Exception ignored) {}
        }*/
        // show notification to avoid ANR in api level 26+
        PhoneProfilesNotification.showProfileNotification(getApplicationContext(),
                !isServiceRunning, isServiceRunning, true);

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "after show profile notification");

        if (PPApplication.getInstance() == null) {
            PPApplication.loadGlobalApplicationData(getApplicationContext());
            PPApplication.loadProfileActivationData(getApplicationContext());

            PPApplication.sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            PPApplication.accelerometerSensor = PPApplication.getAccelerometerSensor(getApplicationContext());
            PPApplication.magneticFieldSensor = PPApplication.getMagneticFieldSensor(getApplicationContext());
            PPApplication.proximitySensor = PPApplication.getProximitySensor(getApplicationContext());
            PPApplication.lightSensor = PPApplication.getLightSensor(getApplicationContext());
            PPApplication.startHandlerThreadOrientationScanner();
        }

        //serviceRunning = false;
        //runningInForeground = false;
        PPApplication.applicationFullyStarted = false;
        PPApplication.normalServiceStart = false;
        PPApplication.showToastForProfileActivation = false;
        //ApplicationPreferences.forceNotUseAlarmClock = false;

        final Context appContext = getApplicationContext();

        //appContext.registerReceiver(stopReceiver, new IntentFilter(PhoneProfilesService.ACTION_STOP));
        //LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(commandReceiver, new IntentFilter(ACTION_COMMAND));

        if (Build.VERSION.SDK_INT < 31) {
            IntentFilter intentFilter5 = new IntentFilter();
            intentFilter5.addAction(PhoneProfilesNotification.ACTION_START_LAUNCHER_FROM_NOTIFICATION);
            appContext.registerReceiver(PPApplication.startLauncherFromNotificationReceiver, intentFilter5);
        }

        //appContext.registerReceiver(PPApplication.showProfileNotificationBroadcastReceiver, new IntentFilter(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION));
        //appContext.registerReceiver(PPApplication.updateGUIBroadcastReceiver, new IntentFilter(PPApplication.ACTION_UPDATE_GUI));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.dashClockBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver"));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.iconWidgetBroadcastReceiver,
                new IntentFilter(IconWidgetProvider.ACTION_REFRESH_ICONWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.oneRowWidgetBroadcastReceiver,
                new IntentFilter(OneRowWidgetProvider.ACTION_REFRESH_ONEROWWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.listWidgetBroadcastReceiver,
                new IntentFilter(ProfileListWidgetProvider.ACTION_REFRESH_LISTWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.edgePanelBroadcastReceiver,
                new IntentFilter(SamsungEdgeProvider.ACTION_REFRESH_EDGEPANEL));

        /*
        PPApplication.setNotificationProfileName(appContext, "");
        PPApplication.setWidgetProfileName(appContext, 1, "");
        PPApplication.setWidgetProfileName(appContext, 2, "");
        PPApplication.setWidgetProfileName(appContext, 3, "");
        PPApplication.setWidgetProfileName(appContext, 4, "");
        PPApplication.setWidgetProfileName(appContext, 5, "");
        PPApplication.setActivityProfileName(appContext, 1, "");
        PPApplication.setActivityProfileName(appContext, 2, "");
        PPApplication.setActivityProfileName(appContext, 3, "");
        */

        //try {
            if ((Build.VERSION.SDK_INT < 26)) {
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
            }
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, ApplicationPreferences.applicationEventPeriodicScanningEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, ApplicationPreferences.applicationEventPeriodicScanningScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventNotificationEnableScanning);
        //} catch (Exception e) {
            // https://github.com/firebase/firebase-android-sdk/issues/1226
            //PPApplication.recordException(e);
        //}

        /*
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
        editor.apply();
        */

        /* moved to doFirstStart after PPApplication.getServiceList()
        // get list of TRANSACTIONS for "phone"
        Object serviceManager = PPApplication.getServiceManager("phone");
        if (serviceManager != null) {
            PPApplication.getTransactionCode(String.valueOf(serviceManager), "");
        }
       */

        //PPApplication.initPhoneProfilesServiceMessenger(appContext);

        // moved to PPApplication.onCreate()
        //if (PPApplication.keyguardManager == null)
        //    PPApplication.keyguardManager = (KeyguardManager)appContext.getSystemService(Context.KEYGUARD_SERVICE);
        //if (PPApplication.keyguardManager != null)
        //    PPApplication.keyguardLock = PPApplication.keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        //willBeDoRestartEvents = false;

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OK created");

        /*PPApplication.startHandlerThread("PhoneProfilesService.doForFirstStart");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler.getInstance(appContext).deactivateProfile();
                ActivateProfileHelper.updateGUI(appContext, false, true);
            }
        });*/

        //PPApplication.startTimeOfApplicationStart = Calendar.getInstance().getTimeInMillis();

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.APPLICATION_FULLY_STARTED_WORK_TAG)
                        .setInitialDelay(PPApplication.APPLICATION_START_DELAY, TimeUnit.MILLISECONDS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                //PPApplication.logE("PhoneProfilesService.onCreate", "workManager="+workManager);
                if (workManager != null) {
                    workManager.enqueueUniqueWork(MainWorker.APPLICATION_FULLY_STARTED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        Context appContext = getApplicationContext();

        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(commandReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        /*try {
            appContext.unregisterReceiver(stopReceiver);
        } catch (Exception ignored) {}*/


        stopSimulatingRingingCall(/*true*/true, getApplicationContext());
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        registerAllTheTimeRequiredPPPBroadcastReceivers(false);
        registerAllTheTimeRequiredSystemReceivers(false);
        registerAllTheTimeContentObservers(false);
        registerAllTheTimeCallbacks(false);
        registerPPPPExtenderReceiver(false, null);
        unregisterEventsReceiversAndWorkers(true);

        if (Build.VERSION.SDK_INT < 31) {
            try {
                appContext.unregisterReceiver(PPApplication.startLauncherFromNotificationReceiver);
            } catch (Exception e) {
                //PPApplication.recordException(e);
            }
        }
        /*try {
            appContext.unregisterReceiver(PPApplication.showProfileNotificationBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }*/
        /*try {
            appContext.unregisterReceiver(PPApplication.updateGUIBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }*/
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.refreshActivitiesBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.dashClockBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }

        /*
        if (startLauncherFromNotificationReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(startLauncherFromNotificationReceiver);
                startLauncherFromNotificationReceiver = null;
            } catch (Exception e) {
                startLauncherFromNotificationReceiver = null;
            }
        }
        */

        RootUtils.initRoot();

        //ShowProfileNotificationBroadcastReceiver.removeAlarm(appContext);
        try {
            //if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))

            //isInForeground = false;
            stopForeground(true);

            PPApplication.cancelWork(ShowProfileNotificationWorker.WORK_TAG, true);
            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                try {
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                    }
                } catch (Exception ignored) {}
                try {
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_NATIVE_ID);
                    }
                } catch (Exception ignored) {}
            }

            /*else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }*/
        } catch (Exception e) {
            //Log.e("PhoneProfilesService.onDestroy", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = null;
        }

        serviceHasFirstStart = false;
        //serviceRunning = false;
        //runningInForeground = false;
        PPApplication.applicationFullyStarted = false;
        PPApplication.normalServiceStart = false;
        PPApplication.showToastForProfileActivation = false;

        // cancel works
        //PPApplication.cancelAllWorks(appContext);
    }

    static PhoneProfilesService getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
            return instance;
        //}
    }

    public static void stop(/*Context context*/) {
        if (instance != null) {
            /*try {
                context.sendStickyBroadcast(new Intent(ACTION_STOP));
                //context.sendBroadcast(new Intent(ACTION_STOP));
            } catch (Exception ignored) {
            }*/
            try {
                /*
                isInForeground = false;
                instance.stopForeground(true);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);*/

                instance.stopSelf();
            } catch (Exception e) {
                //Log.e("PhoneProfilesService.stop", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

//    void setServiceHasFirstStart(boolean value) {
//        serviceHasFirstStart = value;
//    }

//    boolean getServiceRunning() {
//        return serviceRunning;
//    }

//    boolean getApplicationFullyStarted() {
//        return applicationFullyStarted;
//    }

    void registerAllTheTimeRequiredPPPBroadcastReceivers(boolean register) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "xxx");
        if (!register) {
            if (PPApplication.startEventNotificationDeletedReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER start event notification delete");
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationDeletedReceiver);
                    PPApplication.startEventNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationDeletedReceiver = null;
                }
            }
            if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER not used mobile cells notification delete");
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver);
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                }
            }
            if (PPApplication.eventDelayStartBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER eventDelayStartBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayStartBroadcastReceiver);
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                }
            }
            if (PPApplication.eventDelayEndBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER eventDelayEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayEndBroadcastReceiver);
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                }
            }
            if (PPApplication.profileDurationAlarmBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER profileDurationAlarmBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.profileDurationAlarmBroadcastReceiver);
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "not registered profileDurationAlarmBroadcastReceiver");
            if (PPApplication.runApplicationWithDelayBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER runApplicationWithDelayBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver);
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                }
            }
            if (PPApplication.startEventNotificationBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER startEventNotificationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationBroadcastReceiver);
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                }
            }
            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER lockDeviceActivityFinishBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver);
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                }
            }
            if (PPApplication.pppExtenderBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER pppExtenderBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderBroadcastReceiver);
                    PPApplication.pppExtenderBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderBroadcastReceiver = null;
                }
            }
            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER notUsedMobileCellsNotificationDisableReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver);
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                }
            }
            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER lockDeviceAfterScreenOffBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver);
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                }
            }

            if (PPApplication.donationBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER donationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.donationBroadcastReceiver);
                    PPApplication.donationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.donationBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkPPPReleasesBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER checkGitHubReleasesBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.checkPPPReleasesBroadcastReceiver);
                    PPApplication.checkPPPReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkPPPReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkCriticalPPPReleasesBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER checkCriticalGitHubReleasesBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.checkCriticalPPPReleasesBroadcastReceiver);
                    PPApplication.checkCriticalPPPReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkCriticalPPPReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.checkRequiredExtenderReleasesBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER checkRequiredExtenderReleasesBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.checkRequiredExtenderReleasesBroadcastReceiver);
                    PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = null;
                }
            }
            if (PPApplication.restartEventsWithDelayBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "UNREGISTER restartEventsWithDelayBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.restartEventsWithDelayBroadcastReceiver);
                    PPApplication.restartEventsWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.restartEventsWithDelayBroadcastReceiver = null;
                }
            }
        }
        if (register) {

            if (PPApplication.startEventNotificationDeletedReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER start event notification delete");
                PPApplication.startEventNotificationDeletedReceiver = new StartEventNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(StartEventNotificationDeletedReceiver.START_EVENT_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(PPApplication.startEventNotificationDeletedReceiver, intentFilter5);
            }

            if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER not used mobile cells notification delete");
                PPApplication.notUsedMobileCellsNotificationDeletedReceiver = new NotUsedMobileCellsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver, intentFilter5);
            }

            if (PPApplication.eventDelayStartBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER eventDelayStartBroadcastReceiver");

                PPApplication.eventDelayStartBroadcastReceiver = new EventDelayStartBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.eventDelayStartBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.eventDelayEndBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER eventDelayEndBroadcastReceiver");

                PPApplication.eventDelayEndBroadcastReceiver = new EventDelayEndBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.eventDelayEndBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.profileDurationAlarmBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER profileDurationAlarmBroadcastReceiver");

                PPApplication.profileDurationAlarmBroadcastReceiver = new ProfileDurationAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.profileDurationAlarmBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.runApplicationWithDelayBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER runApplicationWithDelayBroadcastReceiver");

                PPApplication.runApplicationWithDelayBroadcastReceiver = new RunApplicationWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.startEventNotificationBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER startEventNotificationBroadcastReceiver");

                PPApplication.startEventNotificationBroadcastReceiver = new StartEventNotificationBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.startEventNotificationBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER lockDeviceActivityFinishBroadcastReceiver");

                PPApplication.lockDeviceActivityFinishBroadcastReceiver = new LockDeviceActivityFinishBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.pppExtenderBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER pppExtenderBroadcastReceiver");

                PPApplication.pppExtenderBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PPApplication.ACTION_PPPEXTENDER_STARTED);
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                appContext.registerReceiver(PPApplication.pppExtenderBroadcastReceiver, intentFilter14,
                        PPApplication.PPP_EXTENDER_PERMISSION, null);
            }

            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER notUsedMobileCellsNotificationDisableReceiver");
                PPApplication.notUsedMobileCellsNotificationDisableReceiver = new NotUsedMobileCellsNotificationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver, intentFilter5);
            }
            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER lockDeviceAfterScreenOffBroadcastReceiver");
                PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = new LockDeviceAfterScreenOffBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
                appContext.registerReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.donationBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER donationBroadcastReceiver");
                PPApplication.donationBroadcastReceiver = new DonationBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_DONATION);
                appContext.registerReceiver(PPApplication.donationBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.checkPPPReleasesBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER checkGitHubReleasesBroadcastReceiver");
                PPApplication.checkPPPReleasesBroadcastReceiver = new CheckPPPReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
                appContext.registerReceiver(PPApplication.checkPPPReleasesBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.checkCriticalPPPReleasesBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER checkCriticalGitHubReleasesBroadcastReceiver");
                PPApplication.checkCriticalPPPReleasesBroadcastReceiver = new CheckCriticalPPPReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
                appContext.registerReceiver(PPApplication.checkCriticalPPPReleasesBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.checkRequiredExtenderReleasesBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER checkRequiredExtenderReleasesBroadcastReceiver");
                PPApplication.checkRequiredExtenderReleasesBroadcastReceiver = new CheckRequiredExtenderReleasesBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES);
                appContext.registerReceiver(PPApplication.checkRequiredExtenderReleasesBroadcastReceiver, intentFilter5);
            }
            if (PPApplication.restartEventsWithDelayBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredPPPBroadcastReceivers", "REGISTER restartEventsWithDelayBroadcastReceiver");

                PPApplication.restartEventsWithDelayBroadcastReceiver = new RestartEventsWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RESTART_EVENTS_WITH_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.restartEventsWithDelayBroadcastReceiver, intentFilter14);
            }
        }
    }

    void registerAllTheTimeRequiredSystemReceivers(boolean register) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "xxx");
        if (!register) {
            if (PPApplication.timeChangedReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER time changed");
                try {
                    appContext.unregisterReceiver(PPApplication.timeChangedReceiver);
                    PPApplication.timeChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.timeChangedReceiver = null;
                }
            }
            if (PPApplication.shutdownBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER shutdown");
                try {
                    appContext.unregisterReceiver(PPApplication.shutdownBroadcastReceiver);
                    PPApplication.shutdownBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.shutdownBroadcastReceiver = null;
                }
            }
            if (PPApplication.screenOnOffReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER screen on off");
                try {
                    appContext.unregisterReceiver(PPApplication.screenOnOffReceiver);
                    PPApplication.screenOnOffReceiver = null;
                } catch (Exception e) {
                    PPApplication.screenOnOffReceiver = null;
                }
            }
            if (PPApplication.interruptionFilterChangedReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER interruption filter");
                try {
                    appContext.unregisterReceiver(PPApplication.interruptionFilterChangedReceiver);
                    PPApplication.interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.interruptionFilterChangedReceiver = null;
                }
            }

            registerPhoneCallsListener(false, appContext);

            if (PPApplication.ringerModeChangeReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER ringer mode change");
                try {
                    appContext.unregisterReceiver(PPApplication.ringerModeChangeReceiver);
                    PPApplication.ringerModeChangeReceiver = null;
                } catch (Exception e) {
                    PPApplication.ringerModeChangeReceiver = null;
                }
            }
            if (PPApplication.deviceIdleModeReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER device idle mode");
                try {
                    appContext.unregisterReceiver(PPApplication.deviceIdleModeReceiver);
                    PPApplication.deviceIdleModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.deviceIdleModeReceiver = null;
                }
            }
            if (PPApplication.bluetoothConnectionBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER bluetooth connection");
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothConnectionBroadcastReceiver);
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                }
            }
            if (PPApplication.wifiStateChangedBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER wifiStateChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.wifiStateChangedBroadcastReceiver);
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                }
            }
            if (PPApplication.powerSaveModeReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER powerSaveModeReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.powerSaveModeReceiver);
                    PPApplication.powerSaveModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.powerSaveModeReceiver = null;
                }
            }
            if (PPApplication.checkOnlineStatusBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER checkOnlineStatusBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.checkOnlineStatusBroadcastReceiver);
                    PPApplication.checkOnlineStatusBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.checkOnlineStatusBroadcastReceiver = null;
                }
            }
            if (PPApplication.simStateChangedBroadcastReceiver != null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "UNREGISTER simStateChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.simStateChangedBroadcastReceiver);
                    PPApplication.simStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.simStateChangedBroadcastReceiver = null;
                }
            }
        }
        if (register) {
            if (PPApplication.timeChangedReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER time changed");
                //PPApplication.lastUptimeTime = SystemClock.elapsedRealtime();
                //PPApplication.lastEpochTime = System.currentTimeMillis();
                PPApplication.timeChangedReceiver = new TimeChangedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                //intentFilter5.addAction(Intent.ACTION_TIME_TICK);
                //intentFilter5.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter5.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                appContext.registerReceiver(PPApplication.timeChangedReceiver, intentFilter5);
            }

            if (PPApplication.shutdownBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER shutdown");
                PPApplication.shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SHUTDOWN);
                intentFilter5.addAction("android.intent.action.QUICKBOOT_POWEROFF");
                appContext.registerReceiver(PPApplication.shutdownBroadcastReceiver, intentFilter5);
            }

            // required for Lock device, Hide notification in lock screen, screen timeout +
            // screen on/off event + rescan wifi, bluetooth, location, mobile cells
            if (PPApplication.screenOnOffReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER screen on off");
                PPApplication.screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
                appContext.registerReceiver(PPApplication.screenOnOffReceiver, intentFilter5);
            }

            // required for Do not disturb ringer mode
            if (PPApplication.interruptionFilterChangedReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER interruption filter");
                //if (android.os.Build.VERSION.SDK_INT >= 23) {
                //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                    PPApplication.interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                    IntentFilter intentFilter11 = new IntentFilter();
                    intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                    appContext.registerReceiver(PPApplication.interruptionFilterChangedReceiver, intentFilter11);
                //}
                //}
            }

            // required for unlink ring and notification volume
            registerPhoneCallsListener(true, appContext);

            // required for unlink ring and notification volume
            if (PPApplication.ringerModeChangeReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER ringer mode change");
                PPApplication.ringerModeChangeReceiver = new RingerModeChangeReceiver();
                IntentFilter intentFilter7 = new IntentFilter();
                intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.ringerModeChangeReceiver, intentFilter7);
            }

            // required for start EventsHandler in idle maintenance window
            if (PPApplication.deviceIdleModeReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER device idle mode");
                //if (android.os.Build.VERSION.SDK_INT >= 23) {
                PPApplication.deviceIdleModeReceiver = new DeviceIdleModeBroadcastReceiver();
                IntentFilter intentFilter9 = new IntentFilter();
                intentFilter9.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
                // is @hide :-(
                //if (android.os.Build.VERSION.SDK_INT >= 24)
                //    intentFilter9.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
                appContext.registerReceiver(PPApplication.deviceIdleModeReceiver, intentFilter9);
                //}
            }

            // required for (un)register connected bluetooth devices
            if (PPApplication.bluetoothConnectionBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER bluetooth connection");

                PPApplication.bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                appContext.registerReceiver(PPApplication.bluetoothConnectionBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.wifiStateChangedBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER wifiStateChangedBroadcastReceiver");
                PPApplication.wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
                IntentFilter intentFilter8 = new IntentFilter();
                intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.wifiStateChangedBroadcastReceiver, intentFilter8);
            }

            if (PPApplication.powerSaveModeReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER powerSaveModeReceiver");
                PPApplication.powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                appContext.registerReceiver(PPApplication.powerSaveModeReceiver, intentFilter10);
            }

            if (PPApplication.checkOnlineStatusBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER checkOnlineStatusBroadcastReceiver");
                PPApplication.checkOnlineStatusBroadcastReceiver = new CheckOnlineStatusBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                intentFilter10.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                appContext.registerReceiver(PPApplication.checkOnlineStatusBroadcastReceiver, intentFilter10);
            }

            if (PPApplication.simStateChangedBroadcastReceiver == null) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "REGISTER simStateChangedBroadcastReceiver");
                PPApplication.simStateChangedBroadcastReceiver = new SimStateChangedBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                // https://android.googlesource.com/platform/frameworks/base/+/84303f5/telephony/java/com/android/internal/telephony/TelephonyIntents.java
                // this requires READ_PHONE_STATE
                intentFilter10.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED); //"android.intent.action.SIM_STATE_CHANGED");
                appContext.registerReceiver(PPApplication.simStateChangedBroadcastReceiver, intentFilter10);
            }
        }
    }

    static void registerPhoneCallsListener(boolean register, Context context) {
        //PPApplication.logE("[RJS] PhoneProfilesService.registerPhoneCallsListener", "xxx");

        // keep this: it is required to use handlerThreadBroadcast for cal listener
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.phoneCallsListenerSIM1 != null) {
                    try {
                        if (PPApplication.telephonyManagerSIM1 != null)
                            PPApplication.telephonyManagerSIM1.listen(PPApplication.phoneCallsListenerSIM1, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerSIM1 = null;
                        PPApplication.telephonyManagerSIM1 = null;
                    } catch (Exception ignored) {
                    }
                }
                if (PPApplication.phoneCallsListenerSIM2 != null) {
                    try {
                        if (PPApplication.telephonyManagerSIM2 != null)
                            PPApplication.telephonyManagerSIM2.listen(PPApplication.phoneCallsListenerSIM2, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerSIM2 = null;
                        PPApplication.telephonyManagerSIM2 = null;
                    } catch (Exception ignored) {
                    }
                }
                if (PPApplication.phoneCallsListenerDefaul != null) {
                    try {
                        if (PPApplication.telephonyManagerDefault != null)
                            PPApplication.telephonyManagerDefault.listen(PPApplication.phoneCallsListenerDefaul, PhoneStateListener.LISTEN_NONE);
                        PPApplication.phoneCallsListenerDefaul = null;
                        PPApplication.telephonyManagerDefault = null;
                    } catch (Exception ignored) {
                    }
                }
            } else {
                PPApplication.telephonyManagerDefault = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (PPApplication.telephonyManagerDefault != null) {
                    int simCount = PPApplication.telephonyManagerDefault.getPhoneCount();
                    if (simCount > 1) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(appContext);
                        if (mSubscriptionManager != null) {
//                        PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "mSubscriptionManager != null");
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList=" + subscriptionList);
                            } catch (SecurityException e) {
                                //PPApplication.recordException(e);
                            }
                            if (subscriptionList != null) {
//                            PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList.size()=" + subscriptionList.size());
                                for (int i = 0; i < subscriptionList.size(); i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionInfo=" + subscriptionInfo);
                                    if (subscriptionInfo != null) {
                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                        if (subscriptionInfo.getSimSlotIndex() == 0) {
                                            if (PPApplication.telephonyManagerSIM1 == null) {
//                                            PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionId=" + subscriptionId);
                                                PPApplication.telephonyManagerSIM1 = PPApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                                PPApplication.phoneCallsListenerSIM1 = new PhoneCallsListener(context, 1);
                                                PPApplication.telephonyManagerSIM1.listen(PPApplication.phoneCallsListenerSIM1,
                                                        PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                                            }
                                        }
                                        if ((subscriptionInfo.getSimSlotIndex() == 1)) {
                                            if (PPApplication.telephonyManagerSIM2 == null) {
//                                            PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionId=" + subscriptionId);
                                                PPApplication.telephonyManagerSIM2 = PPApplication.telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                                PPApplication.phoneCallsListenerSIM2 = new PhoneCallsListener(context, 2);
                                                PPApplication.telephonyManagerSIM2.listen(PPApplication.phoneCallsListenerSIM2,
                                                        PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                                            }
                                        }
                                    }
//                                else
//                                    PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionInfo == null");
                                }
                            }
//                        else
//                            PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "subscriptionList == null");
                        }
//                    else
//                        PPApplication.logE("PhoneProfilesService.registerAllTheTimeRequiredSystemReceivers", "mSubscriptionManager == null");
                    } else {
                        PPApplication.phoneCallsListenerDefaul = new PhoneCallsListener(context, 0);
                        PPApplication.telephonyManagerDefault.listen(PPApplication.phoneCallsListenerDefaul,
                                PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SERVICE_STATE);
                    }
                }
            }
        });
    }

    void registerAllTheTimeContentObservers(boolean register) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeContentObservers", "xxx");

        // keep this: it is required to use handlerThreadBroadcast observers
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.settingsContentObserver != null) {
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.settingsContentObserver);
                        PPApplication.settingsContentObserver = null;
                    } catch (Exception e) {
                        PPApplication.settingsContentObserver = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.settingsContentObserver == null) {
                    try {
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeContentObservers", "REGISTER settings content observer");
                        //settingsContentObserver = new SettingsContentObserver(appContext, new Handler(getMainLooper()));
                        PPApplication.settingsContentObserver = new SettingsContentObserver(appContext, new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, PPApplication.settingsContentObserver);
                    } catch (Exception e) {
                        PPApplication.settingsContentObserver = null;
                        //PPApplication.recordException(e);
                    }
                }
            }
        });
    }

    private void registerContactsContentObservers(boolean register) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerContentObservers", "xxx");

        // keep this: it is required to use handlerThreadBroadcast for observers
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.contactsContentObserver != null) {
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerContactsContentObservers", "UNREGISTER contacts content observer");
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.contactsContentObserver);
                        PPApplication.contactsContentObserver = null;
                    } catch (Exception e) {
                        PPApplication.contactsContentObserver = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.contactsContentObserver == null) {
                    try {
                        if (Permissions.checkContacts(appContext)) {
                            //PPApplication.logE("[RJS] PhoneProfilesService.registerContactsContentObservers", "REGISTER contacts content observer");
                            PPApplication.contactsContentObserver = new ContactsContentObserver(new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                            appContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, PPApplication.contactsContentObserver);
                        }
                    } catch (Exception e) {
                        PPApplication.contactsContentObserver = null;
                        //PPApplication.recordException(e);
                    }
                }
            }
        });
    }

    void registerAllTheTimeCallbacks(boolean register) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeCallbacks", "xxx");

        // keep this: it is required to use handlerThreadBroadcast for callbacks
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.wifiConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.wifiConnectionCallback);
                        }
                        PPApplication.wifiConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.wifiConnectionCallback = null;
                    }
                }
                if (PPApplication.mobileDataConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.mobileDataConnectionCallback);
                        }
                        PPApplication.mobileDataConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.mobileDataConnectionCallback = null;
                    }
                }
            }
            if (register) {
                if (PPApplication.wifiConnectionCallback == null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            NetworkRequest networkRequest = new NetworkRequest.Builder()
                                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                    .build();

                            PPApplication.wifiConnectionCallback = new WifiNetworkCallback(appContext);
                            if (Build.VERSION.SDK_INT >= 26)
                                connectivityManager.registerNetworkCallback(networkRequest, PPApplication.wifiConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                            else {
                                connectivityManager.registerNetworkCallback(networkRequest, PPApplication.wifiConnectionCallback);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.wifiConnectionCallback = null;
                        //PPApplication.recordException(e);
                    }
                }
                if (PPApplication.mobileDataConnectionCallback == null) {
    //                PPApplication.logE("PhoneProfilesService.registerAllTheTimeCallbacks", "mobileDataConnectionCallback (1)");
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
    //                        PPApplication.logE("PhoneProfilesService.registerAllTheTimeCallbacks", "mobileDataConnectionCallback (2)");
                            NetworkRequest networkRequest = new NetworkRequest.Builder()
                                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                                    .build();

                            PPApplication.mobileDataConnectionCallback = new MobileDataNetworkCallback(appContext);
                            if (Build.VERSION.SDK_INT >= 26)
                                connectivityManager.registerNetworkCallback(networkRequest, PPApplication.mobileDataConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                            else
                                connectivityManager.registerNetworkCallback(networkRequest, PPApplication.mobileDataConnectionCallback);
                        }
                    } catch (Exception e) {
    //                    PPApplication.logE("PhoneProfilesService.registerAllTheTimeCallbacks", Log.getStackTraceString(e));
                        PPApplication.mobileDataConnectionCallback = null;
                        //PPApplication.recordException(e);
                    }
                }
            }
        });
    }

    private void registerBatteryLevelChangedReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "xxx");
        if (!register) {
            if (PPApplication.batteryLevelChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.batteryLevelChangedReceiver);
                    PPApplication.batteryLevelChangedReceiver = null;
//                    PPApplication.logE("[RJS] ---- PhoneProfilesService.registerBatteryLevelChangedReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.batteryLevelChangedReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "REGISTER");
            boolean allowed = false;

            dataWrapper.fillEventList();
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
//            PPApplication.logE("[RJS] ---- PhoneProfilesService.registerBatteryLevelChangedReceiver", "allowed="+allowed);
            /*if (!allowed) {
                String powerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;
                if (powerSaveModeInternal.equals("1") || powerSaveModeInternal.equals("2")) {
                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALL_SENSORS);
                    if (eventsExists) {
                        allowed = ApplicationPreferences.applicationEventWifiEnableScanning &&
                                (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                        if (!allowed)
                            allowed = ApplicationPreferences.applicationEventBluetoothEnableScanning &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                            PreferenceAllowed.PREFERENCE_ALLOWED);
                        if (!allowed)
                            allowed = ApplicationPreferences.applicationEventLocationEnableScanning &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                            PreferenceAllowed.PREFERENCE_ALLOWED);
                        if (!allowed)
                            allowed = ApplicationPreferences.applicationEventMobileCellEnableScanning &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                                            PreferenceAllowed.PREFERENCE_ALLOWED);
                        if (!allowed)
                            allowed = ApplicationPreferences.applicationEventOrientationEnableScanning &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                            PreferenceAllowed.PREFERENCE_ALLOWED);
                    }
                }
            }*/
            if (allowed) {
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                if (PPApplication.batteryLevelChangedReceiver == null) {
                    PPApplication.batteryLevelChangedReceiver = new BatteryLevelChangedBroadcastReceiver();
                    IntentFilter intentFilter1 = new IntentFilter();
                    intentFilter1.addAction(Intent.ACTION_BATTERY_CHANGED);
                    appContext.registerReceiver(PPApplication.batteryLevelChangedReceiver, intentFilter1);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "REGISTER registerBatteryLevelChangedReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "registered");
            }
            else
                registerBatteryLevelChangedReceiver(false, dataWrapper);
        }
    }

    private void registerBatteryChargingChangedReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "xxx");
        if (!register) {
            if (PPApplication.batteryChargingChangedReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.batteryChargingChangedReceiver);
                    PPApplication.batteryChargingChangedReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.batteryChargingChangedReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "REGISTER");
            boolean allowed = false;

            dataWrapper.fillEventList();
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BATTERY/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (!eventsExists) {
                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALL_SCANNER_SENSORS/*, false*/);
                if (eventsExists) {
                    allowed = ApplicationPreferences.applicationEventWifiEnableScanning &&
                            (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventBluetoothEnableScanning &&
                                (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventLocationEnableScanning &&
                                (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventMobileCellEnableScanning &&
                                (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventOrientationEnableScanning &&
                                (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                    if (!allowed)
                        allowed = ApplicationPreferences.applicationEventPeriodicScanningEnableScanning;/* &&
                                (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);*/
                }
            }
            if (allowed) {
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                if (PPApplication.batteryChargingChangedReceiver == null) {
                    PPApplication.batteryChargingChangedReceiver = new BatteryChargingChangedBroadcastReceiver();
                    IntentFilter intentFilter1 = new IntentFilter();
                    intentFilter1.addAction(Intent.ACTION_POWER_CONNECTED);
                    intentFilter1.addAction(Intent.ACTION_POWER_DISCONNECTED);
                    appContext.registerReceiver(PPApplication.batteryChargingChangedReceiver, intentFilter1);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "REGISTER registerBatteryChargingChangedReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "registered");
            }
            else
                registerBatteryChargingChangedReceiver(false, dataWrapper);
        }
    }

    private void registerReceiverForAccessoriesSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "xxx");
        if (!register) {
            if (PPApplication.headsetPlugReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.headsetPlugReceiver);
                    PPApplication.headsetPlugReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "UNREGISTER headset plug");
                } catch (Exception e) {
                    PPApplication.headsetPlugReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "not registered headset plug");
            if (PPApplication.dockConnectionBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.dockConnectionBroadcastReceiver);
                    PPApplication.dockConnectionBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "UNREGISTER dock connection");
                } catch (Exception e) {
                    PPApplication.dockConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "not registered dock connection");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ACCESSORY/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.headsetPlugReceiver == null) {
                    PPApplication.headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
                    IntentFilter intentFilter2 = new IntentFilter();
                    intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                    appContext.registerReceiver(PPApplication.headsetPlugReceiver, intentFilter2);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "REGISTER headset plug");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "registered headset plug");
                if (PPApplication.dockConnectionBroadcastReceiver == null) {
                    PPApplication.dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
                    IntentFilter intentFilter12 = new IntentFilter();
                    intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
                    intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
                    appContext.registerReceiver(PPApplication.dockConnectionBroadcastReceiver, intentFilter12);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "REGISTER dock connection");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAccessoriesSensor", "registered dock connection");
            }
            else
                registerReceiverForAccessoriesSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForSMSSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "xxx");
        if (!register) {
            /*if (smsBroadcastReceiver != null) {
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "UNREGISTER SMS");
                try {
                    appContext.unregisterReceiver(smsBroadcastReceiver);
                    smsBroadcastReceiver = null;
                } catch (Exception e) {
                    smsBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered SMS");
            if (mmsBroadcastReceiver != null) {
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "UNREGISTER MMS");
                try {
                    appContext.unregisterReceiver(mmsBroadcastReceiver);
                    mmsBroadcastReceiver = null;
                } catch (Exception e) {
                    mmsBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered MMS");*/
            if (PPApplication.smsEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.smsEventEndBroadcastReceiver);
                    PPApplication.smsEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "UNREGISTER smsEventEndBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.smsEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered smsEventEndBroadcastReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                /*if (smsBroadcastReceiver == null) {
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER SMS");
                    smsBroadcastReceiver = new SMSBroadcastReceiver();
                    IntentFilter intentFilter21 = new IntentFilter();
                    //if (android.os.Build.VERSION.SDK_INT >= 19)
                        intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                    //else
                    //    intentFilter21.addAction("android.provider.Telephony.SMS_RECEIVED");
                    intentFilter21.setPriority(Integer.MAX_VALUE);
                    appContext.registerReceiver(smsBroadcastReceiver, intentFilter21);
                }
                else
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered SMS");
                if (mmsBroadcastReceiver == null) {
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER MMS");
                    mmsBroadcastReceiver = new SMSBroadcastReceiver();
                    IntentFilter intentFilter22;
                    //if (android.os.Build.VERSION.SDK_INT >= 19)
                        intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
                    //else
                    //    intentFilter22 = IntentFilter.create("android.provider.Telephony.WAP_PUSH_RECEIVED", "application/vnd.wap.mms-message");
                    intentFilter22.setPriority(Integer.MAX_VALUE);
                    appContext.registerReceiver(mmsBroadcastReceiver, intentFilter22);
                }
                else
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered MMS");*/
                if (PPApplication.smsEventEndBroadcastReceiver == null) {
                    PPApplication.smsEventEndBroadcastReceiver = new SMSEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.smsEventEndBroadcastReceiver, intentFilter22);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER smsEventEndBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered smsEventEndBroadcastReceiver");
            }
            else
                registerReceiverForSMSSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForCalendarSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "xxx");
        if (!register) {
            if (PPApplication.calendarProviderChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.calendarProviderChangedBroadcastReceiver);
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER calendarProviderChangedBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered calendarProviderChangedBroadcastReceiver");
            if (PPApplication.eventCalendarBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventCalendarBroadcastReceiver);
                    PPApplication.eventCalendarBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER eventCalendarBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.eventCalendarBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
            if (PPApplication.calendarEventExistsCheckBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.calendarEventExistsCheckBroadcastReceiver);
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER calendarEventExistsCheckBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered calendarEventExistsCheckBroadcastReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALENDAR/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.eventCalendarBroadcastReceiver == null) {
                    PPApplication.eventCalendarBroadcastReceiver = new EventCalendarBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.eventCalendarBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER eventCalendarBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
                if (PPApplication.calendarEventExistsCheckBroadcastReceiver == null) {
                    PPApplication.calendarEventExistsCheckBroadcastReceiver = new CalendarEventExistsCheckBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_CALENDAR_EVENT_EXISTS_CHECK_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.calendarEventExistsCheckBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER calendarEventExistsCheckBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarEventExistsCheckBroadcastReceiver");
                if (PPApplication.calendarProviderChangedBroadcastReceiver == null) {
                    PPApplication.calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter();
                    intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
                    intentFilter23.addDataScheme("content");
                    intentFilter23.addDataAuthority("com.android.calendar", null);
                    intentFilter23.setPriority(Integer.MAX_VALUE);
                    appContext.registerReceiver(PPApplication.calendarProviderChangedBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER calendarProviderChangedBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
            }
            else
                registerReceiverForCalendarSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForRadioSwitchAirplaneModeSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "xxx");
        if (!register) {
            if (PPApplication.airplaneModeStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.airplaneModeStateChangedBroadcastReceiver);
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.airplaneModeStateChangedBroadcastReceiver == null) {
                    PPApplication.airplaneModeStateChangedBroadcastReceiver = new AirplaneModeStateChangedBroadcastReceiver();
                    IntentFilter intentFilter19 = new IntentFilter();
                    intentFilter19.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    appContext.registerReceiver(PPApplication.airplaneModeStateChangedBroadcastReceiver, intentFilter19);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "REGISTER airplaneModeStateChangedBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "registered");
            }
            else
                registerReceiverForRadioSwitchAirplaneModeSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForRadioSwitchNFCSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.HAS_FEATURE_NFC) {
                if (PPApplication.nfcStateChangedBroadcastReceiver != null) {
                    try {
                        appContext.unregisterReceiver(PPApplication.nfcStateChangedBroadcastReceiver);
                        PPApplication.nfcStateChangedBroadcastReceiver = null;
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "UNREGISTER");
                    } catch (Exception e) {
                        PPApplication.nfcStateChangedBroadcastReceiver = null;
                    }
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "not registered");
            }
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_NFC/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.nfcStateChangedBroadcastReceiver == null) {
                    if (PPApplication.HAS_FEATURE_NFC) {
                        PPApplication.nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                        IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                        appContext.registerReceiver(PPApplication.nfcStateChangedBroadcastReceiver, intentFilter21);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "REGISTER nfcStateChangedBroadcastReceiver");
                    }
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "registered");
            } else
                registerReceiverForRadioSwitchNFCSensor(false, dataWrapper);
        }
    }

    private void registerObserverForRadioSwitchMobileDataSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");

        // keep this: it is required to use handlerThreadBroadcast for observers
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.mobileDataStateChangedContentObserver != null) {
                    try {
                        appContext.getContentResolver().unregisterContentObserver(PPApplication.mobileDataStateChangedContentObserver);
                        PPApplication.mobileDataStateChangedContentObserver = null;
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "UNREGISTER");
                    } catch (Exception e) {
                        PPApplication.mobileDataStateChangedContentObserver = null;
                    }
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "not registered");
            }
            if (register) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER");
                dataWrapper.fillEventList();
                boolean allowed = false;
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA/*, false*/);
                if (eventsExists)
                    allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                if (allowed) {
                    if (PPApplication.mobileDataStateChangedContentObserver == null) {
                        PPApplication.mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler(PPApplication.handlerThreadBroadcast.getLooper()));
                        appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, PPApplication.mobileDataStateChangedContentObserver);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER mobileDataStateChangedContentObserver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "registered");
                } else
                    registerObserverForRadioSwitchMobileDataSensor(false, dataWrapper);
            }
        });
    }

    @SuppressLint("InlinedApi")
    private void registerReceiverForRadioSwitchDefaultSIMSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");
        if (!register) {
            if (PPApplication.defaultSIMChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.defaultSIMChangedBroadcastReceiver);
                    PPApplication.defaultSIMChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchDefaultSIMSensor", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.defaultSIMChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchDefaultSIMSensor", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchDefaultSIMSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = false;
            if (Build.VERSION.SDK_INT >= 26) {
                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS/*, false*/);
                eventsExists = eventsExists || dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS/*, false*/);
            }
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.defaultSIMChangedBroadcastReceiver == null) {
                    PPApplication.defaultSIMChangedBroadcastReceiver = new DefaultSIMChangedBroadcastReceiver();
                    IntentFilter intentFilter10 = new IntentFilter();
                    intentFilter10.addAction(SubscriptionManager.ACTION_DEFAULT_SUBSCRIPTION_CHANGED);
                    intentFilter10.addAction(SubscriptionManager.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED);
                    appContext.registerReceiver(PPApplication.defaultSIMChangedBroadcastReceiver, intentFilter10);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchDefaultSIMSensor", "REGISTER registerReceiverForRadioSwitchDefaultSIMSensor");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchDefaultSIMSensor", "registered");
            }
            else
                registerReceiverForRadioSwitchDefaultSIMSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForAlarmClockSensor(boolean register, DataWrapper dataWrapper) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "xxx");
        if (!register) {
            if (PPApplication.alarmClockBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.alarmClockBroadcastReceiver);
                    PPApplication.alarmClockBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER ALARM CLOCK");
                } catch (Exception e) {
                    PPApplication.alarmClockBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered ALARM CLOCK");
            if (PPApplication.alarmClockEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.alarmClockEventEndBroadcastReceiver);
                    PPApplication.alarmClockEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER alarmClockEventEndBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.alarmClockEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered alarmClockEventEndBroadcastReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALARM_CLOCK/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                /*if (android.os.Build.VERSION.SDK_INT < 21) {
                    if (alarmClockBroadcastReceiver == null) {
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER ALARM CLOCK");
                        alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
                        IntentFilter intentFilter21 = new IntentFilter();
                        // AOSP
                        intentFilter21.addAction("com.android.deskclock.ALARM_ALERT");
                        intentFilter21.addAction("com.android.alarmclock.ALARM_ALERT");
                        // Samsung
                        intentFilter21.addAction("com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT");
                        // HTC
                        intentFilter21.addAction("com.htc.android.worldclock.ALARM_ALERT");
                        intentFilter21.addAction("com.htc.android.ALARM_ALERT");
                        // Sony
                        intentFilter21.addAction("com.sonyericsson.alarm.ALARM_ALERT");
                        // ZTE
                        intentFilter21.addAction("zte.com.cn.alarmclock.ALARM_ALERT");
                        // Motorola
                        intentFilter21.addAction("com.motorola.blur.alarmclock.ALARM_ALERT");
                        // LG - not working :-/
                        intentFilter21.addAction("com.lge.clock.ALARM_ALERT");
                        intentFilter21.addAction("com.lge.alarm.alarmclocknew");
                        // Xiaomi - not working :-/
                        //08-23 17:02:00.006 1535-1646/? W/ActivityManager: Sending non-protected broadcast null from system uid 1000 pkg android. Callers=com.android.server.am.ActivityManagerService.broadcastIntentLocked:19078 com.android.server.am.ActivityManagerService.broadcastIntentInPackage:19192 com.android.server.am.PendingIntentRecord.sendInner:311 com.android.server.am.PendingIntentRecord.sendWithResult:205 com.android.server.am.ActivityManagerService.sendIntentSender:7620
                        //08-23 17:02:00.049 12506-12612/? I/AlarmClock: AlarmReceiver, action: com.android.deskclock.ALARM_ALERT
                        //08-23 17:02:00.081 12506-12612/? I/AlarmClock: enableAlarmInternal id:4, enabled:false, skip:false
                        //08-23 17:02:00.093 12506-12612/? I/AlarmClock: Settings, saveNextAlarmTime(), and the timeString is

                        // Gentle Alarm
                        intentFilter21.addAction("com.mobitobi.android.gentlealarm.ALARM_INFO");
                        // Sleep As Android
                        intentFilter21.addAction("com.urbandroid.sleep.alarmclock.ALARM_ALERT");
                        //  Alarmdroid (1.13.2)
                        intentFilter21.addAction("com.splunchy.android.alarmclock.ALARM_ALERT");

                        //intentFilter21.setPriority(Integer.MAX_VALUE);
                        registerReceiver(alarmClockBroadcastReceiver, intentFilter21);
                    } else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered ALARM CLOCK");
                }
                else {*/
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER ALARM CLOCK");
                    if (PPApplication.alarmClockBroadcastReceiver == null) {
                        PPApplication.alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
                        IntentFilter intentFilter21 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
                        appContext.registerReceiver(PPApplication.alarmClockBroadcastReceiver, intentFilter21);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER alarmClockBroadcastReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered ALARM CLOCK");
                //}
                if (PPApplication.alarmClockEventEndBroadcastReceiver == null) {
                    PPApplication.alarmClockEventEndBroadcastReceiver = new AlarmClockEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.alarmClockEventEndBroadcastReceiver, intentFilter22);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER alarmClockEventEndBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered alarmClockEventEndBroadcastReceiver");
            }
            else
                registerReceiverForAlarmClockSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForNotificationSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "xxx");
        if (!register) {
            if (PPApplication.notificationEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.notificationEventEndBroadcastReceiver);
                    PPApplication.notificationEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "UNREGISTER notificationEventEndBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.notificationEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "not registered notificationEventEndBroadcastReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NOTIFICATION/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.notificationEventEndBroadcastReceiver == null) {
                    PPApplication.notificationEventEndBroadcastReceiver = new NotificationEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.notificationEventEndBroadcastReceiver, intentFilter22);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "REGISTER notificationEventEndBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "registered notificationEventEndBroadcastReceiver");
            }
            else
                registerReceiverForNotificationSensor(false, dataWrapper);
        }
    }

    /*
    private void registerReceiverForOrientationSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "xxx");
        if (!register) {
            if (PPApplication.orientationEventBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.orientationEventBroadcastReceiver);
                    PPApplication.orientationEventBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "UNREGISTER registerReceiverForOrientationSensor");
                } catch (Exception e) {
                    PPApplication.orientationEventBroadcastReceiver = null;
                }
            }
            //else
            //   PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "not registered registerReceiverForOrientationSensor");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "REGISTER");
            if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                boolean eventAllowed = false;
                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)) {
                    // start only for screen On
                    dataWrapper.fillEventList();
                    boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION);
                    if (eventsExists)
                        eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                if (eventAllowed) {
                    if (PPApplication.orientationEventBroadcastReceiver == null) {
                        PPApplication.orientationEventBroadcastReceiver = new OrientationEventBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                        appContext.registerReceiver(PPApplication.orientationEventBroadcastReceiver, intentFilter22);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "REGISTER orientationEventBroadcastReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "registered registerReceiverForOrientationSensor");
                } else
                    registerReceiverForOrientationSensor(false, dataWrapper);
            } else
                registerReceiverForOrientationSensor(false, dataWrapper);
        }
    }
    */

    private void registerReceiverForDeviceBootSensor(boolean register, DataWrapper dataWrapper) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "xxx");
        if (!register) {
            if (PPApplication.deviceBootEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.deviceBootEventEndBroadcastReceiver);
                    PPApplication.deviceBootEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[BOOT] PhoneProfilesService.registerReceiverForDeviceBootSensor", "UNREGISTER registerReceiverForDeviceBootSensor");
                } catch (Exception e) {
                    PPApplication.deviceBootEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "not registered registerReceiverForDeviceBootSensor");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_DEVICE_BOOT/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "REGISTER DEVICE BOOT");
                if (PPApplication.deviceBootEventEndBroadcastReceiver == null) {
                    PPApplication.deviceBootEventEndBroadcastReceiver = new DeviceBootEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.deviceBootEventEndBroadcastReceiver, intentFilter22);
                    //PPApplication.logE("[BOOT] PhoneProfilesService.registerReceiverForDeviceBootSensor", "REGISTER registerReceiverForDeviceBootSensor");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "registered registerReceiverForDeviceBootSensor");
            }
            else
                registerReceiverForDeviceBootSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForPeriodicSensor(boolean register, DataWrapper dataWrapper) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeriodicSensor", "xxx");
        if (!register) {
            if (PPApplication.periodicEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.periodicEventEndBroadcastReceiver);
                    PPApplication.periodicEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[BOOT] PhoneProfilesService.registerReceiverForPeriodicSensor", "UNREGISTER registerReceiverForPeriodicSensor");
                } catch (Exception e) {
                    PPApplication.periodicEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeriodicSensor", "not registered registerReceiverForPeriodicSensor");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeriodicSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_PERIODIC/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeriodicSensor", "REGISTER DEVICE BOOT");
                if (PPApplication.periodicEventEndBroadcastReceiver == null) {
                    PPApplication.periodicEventEndBroadcastReceiver = new PeriodicEventEndBroadcastReceiver();
                    IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.periodicEventEndBroadcastReceiver, intentFilter22);
                    //PPApplication.logE("[BOOT] PhoneProfilesService.registerReceiverForPeriodicSensor", "REGISTER registerReceiverForPeriodicSensor");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeriodicSensor", "registered registerReceiverForPeriodicSensor");
            }
            else
                registerReceiverForPeriodicSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForActivatedProfileSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "xxx");
        if (!register) {
            if (PPApplication.activatedProfileEventBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.activatedProfileEventBroadcastReceiver);
                    PPApplication.activatedProfileEventBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "UNREGISTER calendarEventExistsCheckBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.activatedProfileEventBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "not registered calendarEventExistsCheckBroadcastReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ACTIVATED_PROFILE/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.activatedProfileEventBroadcastReceiver == null) {
                    PPApplication.activatedProfileEventBroadcastReceiver = new ActivatedProfileEventBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_ACTIVATED_PROFILE_EVENT_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.activatedProfileEventBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "REGISTER calendarEventExistsCheckBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForActivatedProfileSensor", "registered calendarEventExistsCheckBroadcastReceiver");
            }
            else
                registerReceiverForActivatedProfileSensor(false, dataWrapper);
        }
    }

    private void unregisterPPPPExtenderReceiver(int type) {
        //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER");
        Context appContext = getApplicationContext();

        if (type == PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER) {
            if (PPApplication.pppExtenderForceStopApplicationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderForceStopApplicationBroadcastReceiver);
                    PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER force stop applications");
                } catch (Exception e) {
                    PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForceStopApplicationBroadcastReceiver");

            // send broadcast to Extender for unregister of force stop
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER) {
            if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderForegroundApplicationBroadcastReceiver);
                    PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER foreground applications");
                } catch (Exception e) {
                    PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForegroundApplicationBroadcastReceiver");

            // send broadcast to Extender for unregister foreground application
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER) {
            if (PPApplication.pppExtenderSMSBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderSMSBroadcastReceiver);
                    PPApplication.pppExtenderSMSBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER SMS");
                } catch (Exception e) {
                    PPApplication.pppExtenderSMSBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderSMSBroadcastReceiver");

            // send broadcast to Extender for unregister sms
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER) {
            if (PPApplication.pppExtenderCallBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderCallBroadcastReceiver);
                    PPApplication.pppExtenderCallBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER call");
                } catch (Exception e) {
                    PPApplication.pppExtenderCallBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderCallBroadcastReceiver");

            // send broadcast to Extender for unregister call
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER) {
            // send broadcast to Extender for lock device

            //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER lock device");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
            sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
        }
    }

    void registerPPPPExtenderReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "xxx");
        if (!register) {
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER");
            boolean forceStopAllowed = false;
            boolean applicationsAllowed = false;
            boolean orientationAllowed = false;
            boolean smsAllowed = false;
            boolean callAllowed = false;
            boolean lockDeviceAllowed = false;
            dataWrapper.fillProfileList(false, false);
            dataWrapper.fillEventList();
            boolean forceStopExists = dataWrapper.profileTypeExists(DatabaseHandler.PTYPE_FORCE_STOP/*, false*/);
            if (forceStopExists)
                forceStopAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean lockDeviceExists = dataWrapper.profileTypeExists(DatabaseHandler.PTYPE_LOCK_DEVICE/*, false*/);
            if (lockDeviceExists)
                lockDeviceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, null, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean applicationExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_APPLICATION/*, false*/);
            if (applicationExists)
                applicationsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean orientationExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
            if (orientationExists)
                orientationAllowed = (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean smsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_SMS/*, false*/);
            if (smsExists)
                smsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean callExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL/*, false*/);
            if (callExists)
                callAllowed = (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            if (forceStopAllowed || applicationsAllowed || orientationAllowed || smsAllowed || callAllowed || lockDeviceAllowed) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "forceStopCount=" + forceStopCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "applicationCount=" + applicationCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "orientationCount=" + orientationCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "smsCount=" + smsCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "callCount=" + callCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "lockDeviceCount=" + lockDeviceCount);
                }*/

                if (forceStopAllowed) {
                    if (PPApplication.pppExtenderForceStopApplicationBroadcastReceiver == null) {
                        PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
                        appContext.registerReceiver(PPApplication.pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER force stop applications");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForceStopApplicationBroadcastReceiver");

                    // send broadcast to Extender for register force stop applications
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                    sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);

                if (lockDeviceAllowed) {
                    // send broadcast to Extender for register lock device

                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER lock device");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER);
                    sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);

                if ((applicationsAllowed) || (orientationAllowed)) {
                    if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver == null) {
                        PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED);
                        appContext.registerReceiver(PPApplication.pppExtenderForegroundApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "applications and orientation");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForegroundApplicationBroadcastReceiver");

                    // send broadcast to Extender for register foreground application
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER);
                    sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);

                if (smsAllowed) {
                    if (PPApplication.pppExtenderSMSBroadcastReceiver == null) {
                        PPApplication.pppExtenderSMSBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        appContext.registerReceiver(PPApplication.pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER sms");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderSMSBroadcastReceiver");

                    // send broadcast to Extender for register sms
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_SMS_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);

                if (callAllowed) {
                    if (PPApplication.pppExtenderCallBroadcastReceiver == null) {
                        PPApplication.pppExtenderCallBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        appContext.registerReceiver(PPApplication.pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.PPP_EXTENDER_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER call");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderCallBroadcastReceiver");

                    // send broadcast to Extender for register call
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_CALL_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_REGISTER);
                    sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            }
            else {
                unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
                unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
                unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
                unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
                unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
            }
        }
    }

    private void registerLocationModeChangedBroadcastReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "xxx");
        if (!register) {
            if (PPApplication.locationModeChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.locationModeChangedBroadcastReceiver);
                    PPApplication.locationModeChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.locationModeChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_GPS/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (!eventsExists) {
                if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                    // location scanner is enabled
                    //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                        if (eventsExists)
                            allowed =  Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                }
            }
            if (allowed) {
                if (PPApplication.locationModeChangedBroadcastReceiver == null) {
                    PPApplication.locationModeChangedBroadcastReceiver = new LocationModeChangedBroadcastReceiver();
                    IntentFilter intentFilter18 = new IntentFilter();
                    intentFilter18.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                    //if (android.os.Build.VERSION.SDK_INT >= 19)
                        intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
                    appContext.registerReceiver(PPApplication.locationModeChangedBroadcastReceiver, intentFilter18);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "REGISTER locationModeChangedBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "registered");
            }
            else
                registerLocationModeChangedBroadcastReceiver(false, dataWrapper);
        }
    }

    private void registerBluetoothStateChangedBroadcastReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothStateChangedBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver);
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.bluetoothStateChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            if (BluetoothNamePreferenceX.forceRegister)
                allowed = true;
            else {
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH/*, false*/);
                if (eventsExists)
                    allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                if (!eventsExists) {
                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                            // start only for screen On
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED/*, false*/);
                            if (!eventsExists)
                                eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
                            if (eventsExists)
                                allowed = (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                        }
                    }
                }
            }
            if (allowed) {
                if (PPApplication.bluetoothStateChangedBroadcastReceiver == null) {
                    PPApplication.bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
                    IntentFilter intentFilter15 = new IntentFilter();
                    intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                    appContext.registerReceiver(PPApplication.bluetoothStateChangedBroadcastReceiver, intentFilter15);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "REGISTER bluetoothStateChangedBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "registered");
            }
            else
                registerBluetoothStateChangedBroadcastReceiver(false, dataWrapper, forceRegister);
        }
    }

    /*
    private void registerBluetoothConnectionBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        final Context appContext = getApplicationContext();
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (!register) {
            if (bluetoothConnectionBroadcastReceiver != null) {
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "UNREGISTER");
                try {
                    unregisterReceiver(bluetoothConnectionBroadcastReceiver);
                    bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    bluetoothConnectionBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase) {
                    //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext))) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED);
                        eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY);
                    }
                    else {
                        eventScannerCount = 0;
                        eventCount = 0;
                    }
                }
                eventCount = eventCount + eventScannerCount;
                if (eventCount > 0) {
                    if (bluetoothConnectionBroadcastReceiver == null) {
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "REGISTER");

                        // add not registered bluetooth devices (but not watches :-( )
                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                                List<BluetoothDeviceData> connectedDevices = BluetoothConnectedDevices.getConnectedDevices(appContext);
                                BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                } finally (
                                if ((wakeLock != null) && wakeLock.isHeld())
                                    wakeLock.release();
                                }
                                }
                        });

                        bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                        registerReceiver(bluetoothConnectionBroadcastReceiver, intentFilter14);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "registered");
                } else {
                    registerBluetoothConnectionBroadcastReceiver(false, false, forceRegister);
                }
            }
            else
                registerBluetoothConnectionBroadcastReceiver(false, false, forceRegister);
        }
    }
    */

    private void registerBluetoothScannerReceivers(boolean register, DataWrapper dataWrapper, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothScanReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothScanReceiver);
                    PPApplication.bluetoothScanReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothScanReceiver");
                } catch (Exception e) {
                    PPApplication.bluetoothScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothScanReceiver");
            if (PPApplication.bluetoothLEScanReceiver != null) {
                try {
                    LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.bluetoothLEScanReceiver);
                    PPApplication.bluetoothLEScanReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothLEScanReceiver");
                } catch (Exception e) {
                    PPApplication.bluetoothLEScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothLEScanReceiver");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER");
            if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                boolean allowed = false;
                if (BluetoothNamePreferenceX.forceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
                        if (eventsExists)
                            allowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                }
                if (allowed) {
                    if (PPApplication.bluetoothLEScanReceiver == null) {
                        PPApplication.bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();
                        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.bluetoothLEScanReceiver,
                                new IntentFilter(PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver"));
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
                    if (PPApplication.bluetoothScanReceiver == null) {
                        PPApplication.bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_FOUND);
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        appContext.registerReceiver(PPApplication.bluetoothScanReceiver, intentFilter14);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothScanReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
                } else
                    registerBluetoothScannerReceivers(false, dataWrapper, forceRegister);
            } else
                registerBluetoothScannerReceivers(false, dataWrapper, forceRegister);
        }
    }

    private void registerWifiAPStateChangeBroadcastReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiAPStateChangeBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiAPStateChangeBroadcastReceiver);
                    PPApplication.wifiAPStateChangeBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.wifiAPStateChangeBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "REGISTER");
            if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                boolean allowed = false;
                if (WifiSSIDPreferenceX.forceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                        if (eventsExists)
                            allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                }
                if (allowed) {
                    if (PPApplication.wifiAPStateChangeBroadcastReceiver == null) {
                        PPApplication.wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
                        IntentFilter intentFilter17 = new IntentFilter();
                        intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                        appContext.registerReceiver(PPApplication.wifiAPStateChangeBroadcastReceiver, intentFilter17);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "REGISTER wifiAPStateChangeBroadcastReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "registered");
                }
                else
                    registerWifiAPStateChangeBroadcastReceiver(false, dataWrapper, forceRegister);
            }
            else
                registerWifiAPStateChangeBroadcastReceiver(false, dataWrapper, forceRegister);
        }
    }

    /*
    private void registerPowerSaveModeReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "xxx");
        if (!register) {
            if (PPApplication.powerSaveModeReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.powerSaveModeReceiver);
                    PPApplication.powerSaveModeReceiver = null;
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.powerSaveModeReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "REGISTER");
            //String powerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;

            dataWrapper.fillEventList();
            boolean allowed = false; //powerSaveModeInternal.equals("3");
            //if (!allowed) {
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BATTERY);
                if (eventsExists)
                    allowed = Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
//                if (!eventsExists) {
//                    eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ALL_SENSORS);
//                    if (eventsExists) {
//                        allowed = ApplicationPreferences.applicationEventWifiEnableScanning &&
//                                (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
//                                        PreferenceAllowed.PREFERENCE_ALLOWED);
//                        if (!allowed)
//                            allowed = ApplicationPreferences.applicationEventBluetoothEnableScanning &&
//                                    (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
//                                            PreferenceAllowed.PREFERENCE_ALLOWED);
//                        if (!allowed)
//                            allowed = ApplicationPreferences.applicationEventLocationEnableScanning &&
//                                    (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
//                                            PreferenceAllowed.PREFERENCE_ALLOWED);
//                        if (!allowed)
//                            allowed = ApplicationPreferences.applicationEventMobileCellEnableScanning &&
//                                    (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
//                                            PreferenceAllowed.PREFERENCE_ALLOWED);
//                        if (!allowed)
//                            allowed = ApplicationPreferences.applicationEventOrientationEnableScanning &&
//                                    (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
//                                            PreferenceAllowed.PREFERENCE_ALLOWED);
//                    }
//                }
            //}
            if (allowed) {
                //if (android.os.Build.VERSION.SDK_INT >= 21) {
                    if (PPApplication.powerSaveModeReceiver == null) {
                        PPApplication.powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                        IntentFilter intentFilter10 = new IntentFilter();
                        intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                        appContext.registerReceiver(PPApplication.powerSaveModeReceiver, intentFilter10);
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "REGISTER powerSaveModeReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "registered");
                //}
            }
            else
                registerPowerSaveModeReceiver(false, dataWrapper);
        }
    }
    */

    /*private void registerWifiStateChangedBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (wifiStateChangedBroadcastReceiver != null) {
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
                    wifiStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiStateChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (profileAllowed || eventAllowed) {
                int profileCount = 1;
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase) { // || (wifiStateChangedBroadcastReceiver == null)) {
                    if (profileAllowed) {
                        //profileCount = 0;
                        profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID);
                    }
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext))) {
                                // start only for screen On
                                eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_CONNECTED, false);
                                eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_WIFI, false);
                            } else {
                                eventCount = 0;
                                eventScannerCount = 0;
                            }
                        } else {
                            eventCount = 0;
                            eventScannerCount = 0;
                        }
                    }
                    eventCount = eventCount + eventScannerCount;
                }
                if ((profileCount > 0) || (eventCount > 0)) {
                    if (wifiStateChangedBroadcastReceiver == null) {
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "REGISTER");
                        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
                        IntentFilter intentFilter8 = new IntentFilter();
                        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                        appContext.registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "registered");
                } else {
                    registerWifiStateChangedBroadcastReceiver(false, false, forceRegister);
                }
            }
            else
                registerWifiStateChangedBroadcastReceiver(false, false, forceRegister);
        }
    }*/

    /*
    private void registerWifiConnectionBroadcastReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiConnectionBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiConnectionBroadcastReceiver);
                    PPApplication.wifiConnectionBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.wifiConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "REGISTER");
            boolean profileAllowed = false;
            boolean eventAllowed = false;

            if (WifiSSIDPreferenceX.forceRegister) {
                profileAllowed = true;
                eventAllowed = true;
            }
            else {
                dataWrapper.fillProfileList(false, false);
                boolean profileExists = dataWrapper.profileTypeExists(DatabaseHandler.PTYPE_CONNECT_TO_SSID);
                if (profileExists)
                    profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, null, false, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;

                if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY);
                        if (!eventsExists)
                            eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_CONNECTED);
                        if (eventsExists)
                            eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                }
            }
            if (profileAllowed || eventAllowed) {
                if (PPApplication.wifiConnectionBroadcastReceiver == null) {
                    PPApplication.wifiConnectionBroadcastReceiver = new WifiConnectionBroadcastReceiver();
                    IntentFilter intentFilter13 = new IntentFilter();
                    intentFilter13.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                    appContext.registerReceiver(PPApplication.wifiConnectionBroadcastReceiver, intentFilter13);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "REGISTER wifiConnectionBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "registered");
            }
            else
                registerWifiConnectionBroadcastReceiver(false, dataWrapper, forceRegister);
        }
    }
    */

    private void registerWifiScannerReceiver(boolean register, DataWrapper dataWrapper, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiScanReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.wifiScanReceiver);
                    PPApplication.wifiScanReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.wifiScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "REGISTER");
            if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                boolean allowed = false;
                if (WifiSSIDPreferenceX.forceRegister)
                    allowed = true;
                else {
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                        if (eventsExists)
                            allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                }
                if (allowed) {
                    //}
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "eventCount="+eventCount);
                    if (PPApplication.wifiScanReceiver == null) {
                        PPApplication.wifiScanReceiver = new WifiScanBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter();
                        intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        appContext.registerReceiver(PPApplication.wifiScanReceiver, intentFilter4);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "REGISTER wifiScanReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "registered");
                } else
                    registerWifiScannerReceiver(false, dataWrapper, forceRegister);
            } else
                registerWifiScannerReceiver(false, dataWrapper, forceRegister);
        }
    }

    private void registerReceiverForTimeSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "xxx");
        if (!register) {
            if (PPApplication.eventTimeBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.eventTimeBroadcastReceiver);
                    PPApplication.eventTimeBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.eventTimeBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_TIME/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, getApplicationContext()).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.eventTimeBroadcastReceiver == null) {
                    PPApplication.eventTimeBroadcastReceiver = new EventTimeBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.eventTimeBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "REGISTER eventTimeBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "registered");
            }
            else
                registerReceiverForTimeSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForNFCSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.nfcEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.nfcEventEndBroadcastReceiver);
                    PPApplication.nfcEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.nfcEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NFC/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, getApplicationContext()).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.nfcEventEndBroadcastReceiver == null) {
                    PPApplication.nfcEventEndBroadcastReceiver = new NFCEventEndBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.nfcEventEndBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "REGISTER nfcEventEndBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "registered");
            }
            else
                registerReceiverForNFCSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForCallSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "xxx");
        if (!register) {
            if (PPApplication.missedCallEventEndBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.missedCallEventEndBroadcastReceiver);
                    PPApplication.missedCallEventEndBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.missedCallEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALL/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, getApplicationContext()).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.missedCallEventEndBroadcastReceiver == null) {
                    PPApplication.missedCallEventEndBroadcastReceiver = new MissedCallEventEndBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.missedCallEventEndBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "REGISTER missedCallEventEndBroadcastReceiver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "registered");
            }
            else
                registerReceiverForCallSensor(false, dataWrapper);
        }
    }

    void registerVPNCallback(boolean register, DataWrapper dataWrapper) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerVPNCallbacks", "xxx");

        // keep this: it is required to use handlerThreadBroadcast for callbacks
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            if (!register) {
                if (PPApplication.vpnConnectionCallback != null) {
                    try {
                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager != null) {
                            connectivityManager.unregisterNetworkCallback(PPApplication.vpnConnectionCallback);
                        }
                        PPApplication.vpnConnectionCallback = null;
                    } catch (Exception e) {
                        PPApplication.vpnConnectionCallback = null;
                    }
                }
            }
            if (register) {
                boolean allowed = false;

                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_VPN/*, false*/);
                if (eventsExists)
                    allowed = Event.isEventPreferenceAllowed(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                if (allowed) {
                    if (PPApplication.vpnConnectionCallback == null) {
                        try {
                            ConnectivityManager connectivityManager =
                                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            if (connectivityManager != null) {
                                NetworkRequest networkRequest = new NetworkRequest.Builder()
                                        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                                        .build();

                                PPApplication.vpnConnectionCallback = new VPNNetworkCallback(appContext);
                                if (Build.VERSION.SDK_INT >= 26)
                                    connectivityManager.registerNetworkCallback(networkRequest, PPApplication.vpnConnectionCallback, PPApplication.handlerThreadBroadcast.getThreadHandler());
                                else
                                    connectivityManager.registerNetworkCallback(networkRequest, PPApplication.vpnConnectionCallback);
                            }
                        } catch (Exception e) {
                            PPApplication.vpnConnectionCallback = null;
                            //PPApplication.recordException(e);
                        }
                    }
                } else
                    registerVPNCallback(false, dataWrapper);
            }
        });
    }

    private void registerLocationScannerReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "xxx");
        if (!register) {
            if (PPApplication.locationScannerSwitchGPSBroadcastReceiver != null) {
                try {
                    appContext.unregisterReceiver(PPApplication.locationScannerSwitchGPSBroadcastReceiver);
                    PPApplication.locationScannerSwitchGPSBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.locationScannerSwitchGPSBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "REGISTER");
            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                boolean allowed = false;
                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                    // start only for screen On
                    dataWrapper.fillEventList();
                    boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                    if (eventsExists)
                        allowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                if (allowed) {
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "eventCount="+eventCount);
                    if (PPApplication.locationScannerSwitchGPSBroadcastReceiver == null) {
                        PPApplication.locationScannerSwitchGPSBroadcastReceiver = new LocationScannerSwitchGPSBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                        appContext.registerReceiver(PPApplication.locationScannerSwitchGPSBroadcastReceiver, intentFilter4);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "REGISTER locationScannerSwitchGPSBroadcastReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationScannerReceiver", "registered");
                } else
                    registerLocationScannerReceiver(false, dataWrapper);
            } else
                registerLocationScannerReceiver(false, dataWrapper);
        }
    }

    private void cancelPeriodicScanningWorker(/*boolean useHandler*/) {
        //PPApplication.logE("[RJS] PhoneProfilesService.cancelPeriodicScanningWorker", "xxx");
        PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false/*, useHandler*/);
        PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false/*, useHandler*/);
    }

    // this is called from ThreadHanlder
    void schedulePeriodicScanningWorker() {
        //final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.schedulePeriodicScanningWorker", "xxx");

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.schedulePeriodicScanningWorker", "SCHEDULE");
        if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                eventAllowed = true;
            }
            if (eventAllowed) {
                //PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                //PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
                PPApplication._cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                PPApplication._cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
                //PPApplication.sleep(5000);
                OneTimeWorkRequest periodicEventsHandlerWorker =
                        new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                                .addTag(PeriodicEventsHandlerWorker.WORK_TAG_SHORT)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] PhoneProfilesService.schedulePeriodicScanningWorker", "for=" + PeriodicEventsHandlerWorker.WORK_TAG_SHORT + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplication.logE("[WORKER_CALL] PhoneProfilesService.schedulePeriodicScanningWorker", "xxx");
                        workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            } else
                cancelPeriodicScanningWorker(/*false*/);
        } else
            cancelPeriodicScanningWorker(/*false*/);
        //}
        //else
        //    cancelPeriodicScanningWorker();
    }

    private void cancelWifiWorker(final Context context, boolean forSchedule, boolean useHandler) {
//        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.cancelWifiWorker", "forSchedule="+forSchedule);
        if ((!forSchedule) ||
                (WifiScanWorker.isWorkScheduled(false) || WifiScanWorker.isWorkScheduled(true))) {
//            PPApplication.logE("[FIFO_TEST] PhoneProfilesService.cancelWifiWorker", "CANCEL");
            WifiScanWorker.cancelWork(context, useHandler/*, null*/);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelWifiWorker", "not scheduled");

        WifiScanWorker.setScanRequest(context, false);
        WifiScanWorker.setWaitForResults(context, false);
        WifiScanWorker.setWifiEnabledForScan(context, false);
    }

    // this is called from ThreadHanlder
    void scheduleWifiWorker(final DataWrapper dataWrapper) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "rescan="+rescan);

        if (/*!forceStart &&*/ WifiSSIDPreferenceX.forceRegister)
            return;

        //if (schedule) {
//        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE");
        if (ApplicationPreferences.applicationEventWifiEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_WIFI_NEARBY/*, false*/);
                if (eventsExists)
                    eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (eventAllowed) {
                //if (!(WifiScanWorker.isWorkScheduled(false) || WifiScanWorker.isWorkScheduled(true))) {
                //    WifiScanWorker.scheduleWork(appContext, true);
                //    PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE 1");
                //} else {
                //    if (rescan) {
                        WifiScanWorker.scheduleWork(appContext, true);
//                        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE 2");
                //    }
                //}
            } else {
//                PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "cancelWifiWorker (1)");
                cancelWifiWorker(appContext, true, false);
            }
        } else {
//            PPApplication.logE("[FIFO_TEST] PhoneProfilesService.scheduleWifiWorker", "cancelWifiWorker (2)");
            cancelWifiWorker(appContext, true, false);
        }
        //}
        //else
        //    cancelWifiWorker(appContext, handler);
    }

    private void cancelBluetoothWorker(final Context context, boolean forSchedule, boolean useHandler) {
        if ((!forSchedule) ||
                (BluetoothScanWorker.isWorkScheduled(false) || BluetoothScanWorker.isWorkScheduled(true))) {
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "CANCEL");
            BluetoothScanWorker.cancelWork(context, useHandler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "not scheduled");

        BluetoothScanWorker.setScanRequest(context, false);
        BluetoothScanWorker.setLEScanRequest(context, false);
        BluetoothScanWorker.setWaitForResults(context, false);
        BluetoothScanWorker.setWaitForLEResults(context, false);
        BluetoothScanWorker.setBluetoothEnabledForScan(context, false);
        BluetoothScanWorker.setScanKilled(context, false);
//        PPApplication.logE("$$$B PhoneProfilesService.cancelBluetoothWorker", "(1) prefEventBluetoothEnabledForScan="+ApplicationPreferences.prefEventBluetoothEnabledForScan);
    }

    // this is called from ThreadHanlder
    private void scheduleBluetoothWorker(final DataWrapper dataWrapper) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "xxx");

        if (/*!forceStart &&*/ BluetoothNamePreferenceX.forceRegister)
            return;

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "SCHEDULE");
        if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY/*, false*/);
                if (eventsExists)
                    eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (eventAllowed) {
                /*if (BluetoothScanWorker.isWorkScheduled(false) || BluetoothScanWorker.isWorkScheduled(true)) {
                    BluetoothScanWorker.cancelWork(appContext, true);
                }*/
                BluetoothScanWorker.scheduleWork(appContext, true);
            } else
                cancelBluetoothWorker(appContext, true, false);
        } else
            cancelBluetoothWorker(appContext, true, false);
        //}
        //else
        //    cancelBluetoothWorker(appContext, handler);
    }

    /*
    private void cancelGeofenceWorker(boolean forSchedule) {
        if ((!forSchedule) ||
                (GeofenceScanWorker.isWorkScheduled(false) || GeofenceScanWorker.isWorkScheduled(true))) {
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "CANCEL");
            GeofenceScanWorker.cancelWork(true);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "not scheduled");
    }

    private void scheduleGeofenceWorker(final DataWrapper dataWrapper) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "xxx");

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "SCHEDULE");
        if (ApplicationPreferences.applicationEventLocationEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION);
                if (eventsExists)
                    eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (eventAllowed) {
                // location scanner is enabled
                //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "updateTransitionsByLastKnownLocation");
                if (GeofenceScanWorker.isWorkScheduled(false) || GeofenceScanWorker.isWorkScheduled(true)) {
                    GeofenceScanWorker.cancelWork(true);
                }
                synchronized (PPApplication.locationScannerMutex) {
                    if (isLocationScannerStarted()) {
                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "updateTransitionsByLastKnownLocation");
                        getGeofencesScanner().updateTransitionsByLastKnownLocation();
                    }
                }
                GeofenceScanWorker.scheduleWork(appContext, true);
            } else
                cancelGeofenceWorker(true);
        } else
            cancelGeofenceWorker(true);
        //}
        //else
        //    cancelGeofenceWorker(appContext, handler);
    }
    */

    private void cancelSearchCalendarEventsWorker(boolean forSchedule, boolean useHandler) {
        if ((!forSchedule) ||
                (SearchCalendarEventsWorker.isWorkScheduled(false) || SearchCalendarEventsWorker.isWorkScheduled(true))) {
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "CANCEL");
            SearchCalendarEventsWorker.cancelWork(useHandler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "not scheduled");
    }

    // this is called from ThreadHanlder
    private void scheduleSearchCalendarEventsWorker(final DataWrapper dataWrapper) {
        final Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "xxx");

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "SCHEDULE");
        dataWrapper.fillEventList();
        boolean eventAllowed = false;
        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_CALENDAR/*, false*/);
        if (eventsExists)
            eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                PreferenceAllowed.PREFERENCE_ALLOWED;
        if (eventAllowed) {
            //if (!(SearchCalendarEventsWorker.isWorkScheduled(false) || SearchCalendarEventsWorker.isWorkScheduled(true))) {
                //if (rescan)
                SearchCalendarEventsWorker.scheduleWork(true);
                //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "SCHEDULE xxx");
            //}
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "scheduled");
        } else
            cancelSearchCalendarEventsWorker(true, false);
        //}
        //else
        //    cancelSearchCalendarEventsWorker(appContext, handler);
    }

    private void startLocationScanner(boolean start, @SuppressWarnings("SameParameterValue") boolean stop, DataWrapper dataWrapper, boolean forScreenOn) {
        synchronized (PPApplication.locationScannerMutex) {
            Context appContext = getApplicationContext();
            //PPApplication.logE("[RJS] PhoneProfilesService.startLocationScanner", "xxx");
            if (stop) {
                if (isLocationScannerStarted()) {
                    stopLocationScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startLocationScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startLocationScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[RJS] PhoneProfilesService.startLocationScanner", "START");
                if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                    boolean eventAllowed = false;
                    boolean applicationEventLocationScanOnlyWhenScreenIsOn = ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn;
                    if ((PPApplication.isScreenOn) || (!applicationEventLocationScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                        if (eventsExists)
                            eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    if (eventAllowed) {
                        if (!isLocationScannerStarted()) {
                            startLocationScanner(forScreenOn && PPApplication.isScreenOn &&
                                    applicationEventLocationScanOnlyWhenScreenIsOn);
                            //PPApplication.logE("[RJS] PhoneProfilesService.startLocationScanner", "START");
                        }
                    } else
                        startLocationScanner(false, true, dataWrapper, forScreenOn);
                } else
                    startLocationScanner(false, true, dataWrapper, forScreenOn);
            }
        }
    }

    private void startMobileCellsScanner(boolean start, boolean stop, DataWrapper dataWrapper, boolean forceStart,
                                         boolean rescan) {
        synchronized (PPApplication.mobileCellsScannerMutex) {
            Context appContext = getApplicationContext();
            //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "xxx");
            if (!forceStart && (MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart))
                return;
            if (stop) {
                if (isMobileCellsScannerStarted()) {
                    stopMobileCellsScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "START");
                //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || MobileCellsScanner.forceStart) {
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning ||
                        MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart) {
                    boolean eventAllowed = false;
                    if (MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart)
                        eventAllowed = true;
                    else {
                        if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn)) {
                            // start only for screen On
                            dataWrapper.fillEventList();
                            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_MOBILE_CELLS/*, false*/);
                            if (eventsExists)
                                eventAllowed = (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
                        }
                    }
                    //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "eventAllowed="+eventAllowed);
                    if (eventAllowed) {
                        /*if (PPApplication.logEnabled()) {
                            //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "scanning enabled=" + applicationEventMobileCellEnableScanning);
                            PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "MobileCellsScanner.forceStart=" + MobileCellsScanner.forceStart);
                        }*/
                        if (!isMobileCellsScannerStarted()) {
                            startMobileCellsScanner();
                            //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "START 1");
                        } else {
                            if (rescan) {
                                PPApplication.mobileCellsScanner.rescanMobileCells();
                                //PPApplication.logE("[RJS] PhoneProfilesService.startMobileCellsScanner", "RESCAN");
                            }
                        }
                    } else
                        startMobileCellsScanner(false, true, dataWrapper, forceStart, rescan);
                } else
                    startMobileCellsScanner(false, true, dataWrapper, forceStart, rescan);
            }
        }
    }

    private void startOrientationScanner(boolean start, boolean stop, DataWrapper dataWrapper/*, boolean forceStart*/) {
        synchronized (PPApplication.orientationScannerMutex) {
            Context appContext = getApplicationContext();
            //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "xxx");
            //if (!forceStart && EventsPrefsFragment.forceStart)
            //    return;
            if (stop) {
                if (isOrientationScannerStarted()) {
                    stopOrientationScanner();
//                    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "START");
                if (ApplicationPreferences.applicationEventOrientationEnableScanning /*||
                        EventsPrefsFragment.forceStart*/) {
                    boolean eventAllowed = false;
                    /*if (EventsPrefsFragment.forceStart)
                        eventAllowed = true;
                    else*/ {
                        if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)) {
                            // start only for screen On
                            dataWrapper.fillEventList();
                            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
                            if (eventsExists)
                                eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED;
                        }
                    }
                    if (eventAllowed) {
                        if (!isOrientationScannerStarted()) {
                            startOrientationScanner();
//                            PPApplication.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "START");
                        }
//                        else
//                            PPApplication.logE("[SHEDULE_SCANNER] PhoneProfilesService.startOrientationScanner", "started");
                    } else
                        startOrientationScanner(false, true, dataWrapper/*, forceStart*/);
                } else
                    startOrientationScanner(false, true, dataWrapper/*, forceStart*/);
            }
        }
    }

    private void startTwilightScanner(boolean start, boolean stop, DataWrapper dataWrapper) {
        synchronized (PPApplication.twilightScannerMutex) {
            //Context appContext = getApplicationContext();
            //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "xxx");
            if (stop) {
                if (isTwilightScannerStarted()) {
                    stopTwilightScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "not started");
            }
            if (start) {
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_TIME_TWILIGHT/*, false*/);
                if (eventsExists) {
                    if (!isTwilightScannerStarted()) {
                        startTwilightScanner();
                        //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "START");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "started");
                } else {
                    startTwilightScanner(false, true, dataWrapper);
                    //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "STOP");
                }
            }
        }
    }

    private void startNotificationScanner(boolean start, boolean stop, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "xxx");
        if (stop) {
            if (PPApplication.notificationScannerRunning) {
                PPApplication.notificationScannerRunning = false;
                //PPApplication.logE("[RJS] PhoneProfilesService.startNotificationScanner", "STOP");
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.startNotificationScanner", "not started");
        }
        if (start) {
            if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                boolean eventAllowed = false;
                if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn)) {
                    // start only for screen On
                    dataWrapper.fillEventList();
                    boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_NOTIFICATION/*, false*/);
                    if (eventsExists)
                        eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                if (eventAllowed) {
                    if (!PPApplication.notificationScannerRunning) {
                        PPApplication.notificationScannerRunning = true;
                        //PPApplication.logE("[RJS] PhoneProfilesService.startNotificationScanner", "START");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.startNotificationScanner", "started");
                } else
                    startNotificationScanner(false, true, dataWrapper);
            } else
                startNotificationScanner(false, true, dataWrapper);
        }
    }

    void registerEventsReceiversAndWorkers(boolean fromCommand) {
//        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.registerReceiversAndWorkers", "xxx");

        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = getApplicationContext();

        // get actual battery status
        BatteryLevelChangedBroadcastReceiver.initialize(appContext);

        registerContactsContentObservers(true);

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        // required for battery sensor
        registerBatteryLevelChangedReceiver(true, dataWrapper);
        registerBatteryChargingChangedReceiver(true, dataWrapper);

        // required for accessories sensor
        registerReceiverForAccessoriesSensor(true, dataWrapper);

        // required for sms/mms sensor
        registerReceiverForSMSSensor(true, dataWrapper);

        // required for calendar sensor
        registerReceiverForCalendarSensor(true, dataWrapper);

        // required for radio switch sensor
        registerObserverForRadioSwitchMobileDataSensor(true, dataWrapper);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper);
        registerReceiverForRadioSwitchDefaultSIMSensor(true, dataWrapper);

        // required for alarm clock sensor
        registerReceiverForAlarmClockSensor(true, dataWrapper);

        // required for device boot sensor
        registerReceiverForDeviceBootSensor(true, dataWrapper);

        // required for periodic sensor
        registerReceiverForPeriodicSensor(true, dataWrapper);

        // required for location and radio switch sensor
        registerLocationModeChangedBroadcastReceiver(true, dataWrapper);

        // required for bluetooth connection type = (dis)connected +
        // radio switch event +
        // bluetooth scanner
        registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);

        // required for bluetooth connection type = (dis)connected +
        // bluetooth scanner
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);

        // required for bluetooth scanner
        registerBluetoothScannerReceivers(true, dataWrapper, false);

        // required for wifi scanner
        registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);

        // required for all scanner events (wifi, bluetooth, location, mobile cells, device orientation) +
        // battery event
        // moved to all the time
        //registerPowerSaveModeReceiver(true, dataWrapper);

        /*
        // required for Connect to SSID profile preference +
        // wifi connection type = (dis)connected +
        // radio switch event +
        // wifi scanner
        registerWifiStateChangedBroadcastReceiver(true, true, false);
        */

        // required for Connect to SSID profile preference +
        // required for wifi connection type = (dis)connected event +
        // wifi scanner
        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);

        // required for wifi scanner
        registerWifiScannerReceiver(true, dataWrapper, false);

        // required for notification sensor
        registerReceiverForNotificationSensor(true, dataWrapper);

        // required for VPN sensor
        registerVPNCallback(true, dataWrapper);

        //SMSBroadcastReceiver.registerSMSContentObserver(appContext);
        //SMSBroadcastReceiver.registerMMSContentObserver(appContext);

        // ----------------------------------------------

        /*
        if (mSipManager != null) {
            mSipManager = SipManager.newInstance(appContext);

            SipProfile sipProfile = null;
            try {
                SipProfile.Builder builder = new SipProfile.Builder("henrichg", "domain");
                builder.setPassword("password");
                sipProfile = builder.build();

                Intent intent = new Intent();
                intent.setAction(PPApplication.PACKAGE_NAME + ".INCOMING_SIPCALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, Intent.FILL_IN_DATA);
                mSipManager.open(sipProfile, pendingIntent, null);

            } catch (Exception ignored) {
            }
        }
        */

        // register receiver for time sensor
        registerReceiverForTimeSensor(true, dataWrapper);

        // register receiver for nfc sensor
        registerReceiverForNFCSensor(true, dataWrapper);

        // register receiver for call event
        registerReceiverForCallSensor(true, dataWrapper);

        // register receiver for Location scanner
        registerLocationScannerReceiver(true, dataWrapper);

        // required for orientation event
        //registerReceiverForOrientationSensor(true, dataWrapper);

        // required for calendar event
        registerReceiverForActivatedProfileSensor(true, dataWrapper);

        //Log.e("------ PhoneProfilesService.registerReceiversAndWorkers", "fromCommand="+fromCommand);
        WifiScanWorker.initialize(appContext, !fromCommand);
        BluetoothScanWorker.initialize(appContext, !fromCommand);

        startLocationScanner(true, true, dataWrapper, false);
        startMobileCellsScanner(true, true, dataWrapper, false, false);
        startOrientationScanner(true, true, dataWrapper/*, false*/);
        startTwilightScanner(true, true, dataWrapper);
        startNotificationScanner(true, true, dataWrapper);

        schedulePeriodicScanningWorker(/*dataWrapper, true*/);
        scheduleWifiWorker(/*true,*/  dataWrapper/*, false, false, false, true*/);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true, */dataWrapper/*, true*/);
        //scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);

        AvoidRescheduleReceiverWorker.enqueueWork();
    }

    private void unregisterEventsReceiversAndWorkers(boolean useHandler) {
//         PPApplication.logE("[FIFO_TEST] PhoneProfilesService.unregisterReceiversAndWorkers", "xxx");

        Context appContext = getApplicationContext();

        registerContactsContentObservers(false);

        registerBatteryLevelChangedReceiver(false, null);
        registerBatteryChargingChangedReceiver(false, null);
        registerReceiverForAccessoriesSensor(false, null);
        registerReceiverForSMSSensor(false, null);
        registerReceiverForCalendarSensor(false, null);
        registerObserverForRadioSwitchMobileDataSensor(false, null);
        registerReceiverForRadioSwitchNFCSensor(false, null);
        registerReceiverForRadioSwitchAirplaneModeSensor(false, null);
        registerReceiverForRadioSwitchDefaultSIMSensor(false, null);
        registerReceiverForAlarmClockSensor(false, null);
        registerReceiverForDeviceBootSensor(false, null);
        registerReceiverForPeriodicSensor(false, null);
        registerLocationModeChangedBroadcastReceiver(false, null);
        registerBluetoothStateChangedBroadcastReceiver(false, null, false);
        //registerBluetoothConnectionBroadcastReceiver(false, true, false, false);
        registerBluetoothScannerReceivers(false, null, false);
        registerWifiAPStateChangeBroadcastReceiver(false, null, false);
        //registerPowerSaveModeReceiver(false, null);
        //registerWifiStateChangedBroadcastReceiver(false, false, false);
        //registerWifiConnectionBroadcastReceiver(false, null, false);
        registerWifiScannerReceiver(false, null, false);
        registerReceiverForTimeSensor(false, null);
        registerReceiverForNFCSensor(false, null);
        registerReceiverForCallSensor(false, null);
        registerLocationScannerReceiver(false, null);
        registerReceiverForNotificationSensor(false, null);
        //registerReceiverForOrientationSensor(false, null);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        registerReceiverForActivatedProfileSensor(false, null);
        registerVPNCallback(false, null);

        startLocationScanner(false, true, null, false);
        startMobileCellsScanner(false, true, null, false, false);
        startOrientationScanner(false, true, null/*, false*/);
        startTwilightScanner(false, true, null);
        startNotificationScanner(false, true, null);

        cancelPeriodicScanningWorker(/*useHandler*/);
        cancelWifiWorker(appContext, false, useHandler);
        cancelBluetoothWorker(appContext, false, useHandler);
        //cancelGeofenceWorker(false);
        cancelSearchCalendarEventsWorker(false, useHandler);

    }

    private void reregisterEventsReceiversAndWorkers() {
//        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.reregisterReceiversAndWorkers", "xxx");

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        //final Context appContext = getApplicationContext();

        registerContactsContentObservers(true);

        registerBatteryLevelChangedReceiver(true, dataWrapper);
        registerBatteryChargingChangedReceiver(true, dataWrapper);
        registerReceiverForAccessoriesSensor(true, dataWrapper);
        registerReceiverForSMSSensor(true, dataWrapper);
        registerReceiverForCalendarSensor(true, dataWrapper);
        registerObserverForRadioSwitchMobileDataSensor(true, dataWrapper);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper);
        registerReceiverForRadioSwitchDefaultSIMSensor(true, dataWrapper);
        registerReceiverForAlarmClockSensor(true, dataWrapper);
        registerReceiverForDeviceBootSensor(true, dataWrapper);
        registerReceiverForPeriodicSensor(true, dataWrapper);
        registerLocationModeChangedBroadcastReceiver(true, dataWrapper);
        registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);
        registerBluetoothScannerReceivers(true, dataWrapper, false);
        registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
        //registerPowerSaveModeReceiver(true, dataWrapper);
        //registerWifiStateChangedBroadcastReceiver(true, true, false);
        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
        registerWifiScannerReceiver(true, dataWrapper, false);
        registerReceiverForTimeSensor(true, dataWrapper);
        registerReceiverForNFCSensor(true, dataWrapper);
        registerReceiverForCallSensor(true, dataWrapper);
        registerLocationScannerReceiver(true, dataWrapper);
        //registerReceiverForOrientationSensor(true, dataWrapper);
        registerReceiverForNotificationSensor(true,dataWrapper);
        registerReceiverForActivatedProfileSensor(true, dataWrapper);
        registerVPNCallback(true, dataWrapper);

        schedulePeriodicScanningWorker(/*dataWrapper, true*/);
        scheduleWifiWorker(/*true,*/  dataWrapper/*, false, false, false, true*/);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true,*/ dataWrapper /*, true*/);

        startLocationScanner(true, true, dataWrapper, false);
        //scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);

        startMobileCellsScanner(true, true, dataWrapper, false, false);
        startOrientationScanner(true, true, dataWrapper/*, false*/);
        startTwilightScanner(true, true, dataWrapper);
        startNotificationScanner(true, true, dataWrapper);

        AvoidRescheduleReceiverWorker.enqueueWork();
    }

    // start service for first start
    private void doForFirstStart(Intent intent/*, int flags, int startId*/) {
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        final Context appContext = getApplicationContext();

        //final boolean oldServiceHasFirstStart = PPApplication.serviceHasFirstStart;
        serviceHasFirstStart = true;
        PPApplication.setApplicationStarted(getApplicationContext(), true);

        boolean applicationStart = false;
        //boolean deactivateProfile = false;
        boolean activateProfiles = false;
        boolean deviceBoot = false;
        //boolean startOnPackageReplace = false;
        boolean startFromExternalApplication = false;
        String startForExternalAppAction = "";
        int startForExternalAppDataType = 0;
        String startForExternalAppDataValue = "";

        //noinspection UnnecessaryLocalVariable
        final Intent serviceIntent = intent;

        if (serviceIntent != null) {
            applicationStart = serviceIntent.getBooleanExtra(PPApplication.EXTRA_APPLICATION_START, false);
            //applicationStart = true;
            //deactivateProfile = serviceIntent.getBooleanExtra(EXTRA_DEACTIVATE_PROFILE, false);
            activateProfiles = serviceIntent.getBooleanExtra(EXTRA_ACTIVATE_PROFILES, false);
            deviceBoot = serviceIntent.getBooleanExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            //startOnPackageReplace = serviceIntent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
            startFromExternalApplication = serviceIntent.getBooleanExtra(EXTRA_START_FOR_EXTERNAL_APPLICATION, false);
            startForExternalAppAction = serviceIntent.getStringExtra(EXTRA_START_FOR_EXTERNAL_APP_ACTION);
            startForExternalAppDataType = serviceIntent.getIntExtra(EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, 0);
            startForExternalAppDataValue = serviceIntent.getStringExtra(EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE);
        }
        else {
            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_SYSTEM_RESTART, null, null, "");
        }

        if (PPApplication.logEnabled()) {
            PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEVICE_BOOT="+deviceBoot);
            PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_APPLICATION_START="+applicationStart);
            //if (startOnPackageReplace)
            //    PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");
            //if (deactivateProfile)
            //    PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEACTIVATE_PROFILE");
            PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_ACTIVATE_PROFILES="+activateProfiles);
            PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APPLICATION="+startFromExternalApplication);
            if (startFromExternalApplication) {
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_ACTION="+startForExternalAppAction);
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE="+startForExternalAppDataType);
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE="+startForExternalAppDataValue);
            }
        }

        final boolean _applicationStart = applicationStart;
        final boolean _deviceBoot = deviceBoot;
        //final boolean _startOnPackageReplace = startOnPackageReplace;
        final boolean _activateProfiles = activateProfiles;
        //final boolean _deactivateProfile = deactivateProfile;
        final boolean _startFromExternalApplication = startFromExternalApplication;
        final String _startForExternalAppAction = startForExternalAppAction;
        final int _startForExternalAppDataType = startForExternalAppDataType;
        final String _startForExternalAppDataValue = startForExternalAppDataValue;

        //PPApplication.startHandlerThread(/*"PhoneProfilesService.doForFirstStart"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(appContext) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
            PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart START");

            //Context appContext= appContextWeakRef.get();

            //if (appContext == null)
            //    return;

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService_doForFirstStart");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                // is needed beacuse will be changed
                boolean __activateProfiles = _activateProfiles;
                boolean __applicationStart = _applicationStart;

//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.doForFirstStart");

            /*
            // create application directory
            // not working because this requires Storage permission
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory())) {
                Log.e("PhoneProfilesService.doForFirstStart", "create PPP folder - start");
                boolean created = exportDir.mkdirs();
                Log.e("PhoneProfilesService.doForFirstStart", "created="+created);
                try {
                    exportDir.setReadable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }
                try {
                    exportDir.setWritable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }
            }
            */

                //PhoneProfilesService.cancelWork(DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START_WORK_TAG, appContext);

            /*if (_deactivateProfile) {
                DatabaseHandler.getInstance(appContext).deactivateProfile();
                ActivateProfileHelper.updateGUI(appContext, false, true);
            }*/

                // is called from PPApplication
                //PPApplication.initRoot();
                /*if (PPApplication.isRooted(false)) {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                    editor.apply();
                    ApplicationPreferences.applicationNeverAskForGrantRoot(appContext.getApplicationContext());
                }*/
                if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                    // grant root
                    RootUtils.isRootGranted();
                } else {
                    synchronized (PPApplication.rootMutex) {
                        if (PPApplication.rootMutex.rootChecked) {
                            try {
                                PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                                if (PPApplication.rootMutex.rooted) {
                                    PackageManager packageManager = appContext.getPackageManager();
                                    // SuperSU
                                    Intent _intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                                    if (_intent != null)
                                        PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                                    else {
                                        _intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                        if (_intent != null)
                                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                                        else
                                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                                    }
                                }
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        } else {
                            //try {
                            PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, "ask for grant disabled");
                            //} catch (Exception e) {
                            //    PPApplication.recordException(e);
                            //}
                        }
                    }
                }

                //PPApplication.getSUVersion();
                RootUtils.settingsBinaryExists(false);
                RootUtils.serviceBinaryExists(false);
                RootUtils.getServicesList();

                PhoneProfilesService ppService = PhoneProfilesService.getInstance();

                boolean newVersion = false;
                if (ppService != null)
                    newVersion = ppService.doForPackageReplaced(appContext);
                if (newVersion) {
                    __activateProfiles = true;
                    __applicationStart = true;
                }

                if (PPApplication.logEnabled()) {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "__applicationStart=" + __applicationStart);
                    PPApplication.logE("PhoneProflesService.doForFirstStart - handler", "__activateProfiles=" + __activateProfiles);
                }

                /*if (PPApplication.logEnabled()) {
                    // get list of TRANSACTIONS for "phone"
                    Object serviceManager = PPApplication.getServiceManager("phone");
                    if (serviceManager != null) {
                        // only log it
                        PPApplication.getTransactionCode(String.valueOf(serviceManager), "");
                    }
                }*/

                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);

                dataWrapper.fillProfileList(false, false);
                for (Profile profile : dataWrapper.profileList)
                    profile.isAccessibilityServiceEnabled(appContext, true);
                dataWrapper.fillEventList();
                for (Event event : dataWrapper.eventList)
                    event.isAccessibilityServiceEnabled(appContext, true, true);
                //PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(appContext, true, false);

                //GlobalGUIRoutines.setLanguage(appContext);
                GlobalGUIRoutines.switchNightMode(appContext, true);

                DataWrapperStatic.setDynamicLauncherShortcuts(appContext);

                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "application not started, start it");

                //Permissions.clearMergedPermissions(appContext);

                //if (!TonesHandler.isToneInstalled(/*TonesHandler.TONE_ID,*/ appContext))
                //    TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext);
                //DatabaseHandler.getInstance(appContext).fixPhoneProfilesSilentInProfiles();
                DatabaseHandler.getInstance(appContext).disableNotAllowedPreferences();

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "2");

                //TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);
                ActivateProfileHelper.setMergedRingNotificationVolumes(appContext/*, true*/);

                ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                if (ApplicationPreferences.keepScreenOnPermanent)
                    ActivateProfileHelper.createKeepScreenOnView(appContext);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "3");

                AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    RingerModeChangeReceiver.setRingerMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    //PPNotificationListenerService.setZenMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    try {
                        ActivateProfileHelper.setNotificationVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                    } catch (Exception e10) {
                        PPApplication.recordException(e10);
                    }
                    try {
                        ActivateProfileHelper.setRingerVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_RING));
                    } catch (Exception e10) {
                        PPApplication.recordException(e10);
                    }
                }

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "4");

                PPPExtenderBroadcastReceiver.setApplicationInForeground(appContext, "");

                EventPreferencesCall.setEventCallEventType(appContext, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                EventPreferencesCall.setEventCallEventTime(appContext, 0);
                EventPreferencesCall.setEventCallPhoneNumber(appContext, "");
                EventPreferencesCall.setEventCallFromSIMSlot(appContext, 0);

                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 0, false, false);
                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 1, false, false);
                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 2, false, false);

                // set alarm for Alarm clock sensor from last saved time in
                // NextAlarmClockBroadcastReceiver.onReceived()
                AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    NextAlarmClockBroadcastReceiver.setAlarm(
                            ApplicationPreferences.prefEventAlarmClockTime,
                            ApplicationPreferences.prefEventAlarmClockPackageName,
                            alarmManager, appContext);
                }

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "5");

                // show info notification
                ImportantInfoNotification.showInfoNotification(appContext);

                // do not show it at start of PPP, will be shown for each profile activation.
                //DrawOverAppsPermissionNotification.showNotification(appContext, false);
                //IgnoreBatteryOptimizationNotification.showNotification(appContext, false);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "6");

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "7");

                //if (serviceIntent != null) {
                // it is not restart of service
                // From documentation: This may be null if the service is being restarted after its process has gone away
                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "it is not restart of service");
                if (__applicationStart) {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "--- initialization for application start");

                    dataWrapper.fillProfileList(false, false);
                    for (Profile profile : dataWrapper.profileList)
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);
                    //Profile.setActivatedProfileForDuration(appContext, 0);
                    //Profile profile = DataWrapper.getNonInitializedProfile(
                    //        getString(R.string.empty_string), Profile.PROFILE_ICON_DEFAULT, 0);
                    //Profile.saveProfileToSharedPreferences(profile, appContext);

                    // DO NOT UNBLOCK EVENTS. AT START MUST MANUALLY ACTIVATED PROFILE, IF WAS ACTIVATED BEFORE PPService start
                    //Event.setEventsBlocked(appContext, false);
                    //dataWrapper.fillEventList();
                    //synchronized (dataWrapper.eventList) {
                    //    for (Iterator<Event> it = dataWrapper.eventList.iterator(); it.hasNext(); ) {
                    //        Event event = it.next();
                    //        if (event != null)
                    //            event._blocked = false;
                    //    }
                    //}
                    //DatabaseHandler.getInstance(appContext).unblockAllEvents();
                    //Event.setForceRunEventRunning(appContext, false);

//                        PPApplication.logE("[FIFO_TEST] PhoneProfilesService.doFirstStart", "#### clear");
                    synchronized (PPApplication.profileActivationMutex) {
                        List<String> activateProfilesFIFO = new ArrayList<>();
                        dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                    }

                    if (PPApplication.prefLastActivatedProfile != 0) {
//                            PPApplication.logE("[FIFO_TEST] PhoneProfilesService.doFirstStart", "#### add PPApplication.prefLastActivatedProfile - profileId=" + PPApplication.prefLastActivatedProfile);
                        dataWrapper.fifoAddProfile(PPApplication.prefLastActivatedProfile, 0);
                    }

                }

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "8");

                dataWrapper.fillEventList();
                for (Event event : dataWrapper.eventList)
                    StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "9");

                LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(appContext);
                LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                //PPNotificationListenerService.clearNotifiedPackages(appContext);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "10");

                DatabaseHandler.getInstance(appContext).deleteAllEventTimelines();
                DatabaseHandler.getInstance(appContext).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "11");

                //if (_startOnPackageReplace) {
                //PPApplication.logE("[REG] PhoneProfilesService.doFirstStart", "setMobileCellsAutoRegistration(true)");
                //    MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, true);
                //}
                //else
                MobileCellsScanner.startAutoRegistration(appContext, true);

                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, true);
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                // not needed clearConnectedDevices(.., true) call it

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "BluetoothConnectedDevices.getConnectedDevices()");
                // duration > 30 seconds because in it is 3 x 10 seconds sleep
                BluetoothConnectedDevices.getConnectedDevices(appContext);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "12");

                WifiScanWorker.setScanRequest(appContext, false);
                WifiScanWorker.setWaitForResults(appContext, false);
                WifiScanWorker.setWifiEnabledForScan(appContext, false);

                BluetoothScanWorker.setScanRequest(appContext, false);
                BluetoothScanWorker.setLEScanRequest(appContext, false);
                BluetoothScanWorker.setWaitForResults(appContext, false);
                BluetoothScanWorker.setWaitForLEResults(appContext, false);
                BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
                BluetoothScanWorker.setScanKilled(appContext, false);

                //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "13");

                // !!! registerReceiversAndWorkers moved into MainWorker.doAfterFirstStart
                // in it is not PPP brioadcasts registration
                if (ppService != null)
                    ppService.registerAllTheTimeRequiredPPPBroadcastReceivers(true);

                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "start donation and check GitHub releases alarms");
                DonationBroadcastReceiver.setAlarm(appContext);
                CheckPPPReleasesBroadcastReceiver.setAlarm(appContext);
                CheckCriticalPPPReleasesBroadcastReceiver.setAlarm(appContext);
                CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm(appContext);

                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "application started");

                if ((!_deviceBoot) && (_applicationStart)) {
                    PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START, null, null, "");
                    if (newVersion) {
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            String version = pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_UPGRADE, version, null, "");
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }

                // start events

                if (__activateProfiles) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.apply();
                    ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(appContext);
                }

                //boolean packageReplaced = PPApplication.applicationPackageReplaced; //ApplicationPreferences.applicationPackageReplaced(appContext);
                //PPApplication.logE("******** PhoneProfilesService.doForFirstStart - handler", "package replaced=" + packageReplaced);
                //if (!packageReplaced) {
                //setApplicationFullyStarted(true);

                // work after first start

                /*int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                int actualVersionCode = 0;
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "oldVersionCode=" + oldVersionCode);
                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "actualVersionCode=" + actualVersionCode);*/

                /*if ((oldVersionCode == 0) || (actualVersionCode == 0) || (oldVersionCode < actualVersionCode)) {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "start work for package replaced");

                    // block any profile and event actions for package replaced
                    PPApplication.setBlockProfileEventActions(true);

                    // cancel all PPP notification (except PPService notification
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "cancel notifications - start");
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        StatusBarNotification[] notitications = notificationManager.getActiveNotifications();
                        for (StatusBarNotification notification : notitications) {
                            if (notification.getId() != PPApplication.PROFILE_NOTIFICATION_ID) {
                                if (notification.getTag().isEmpty())
                                    notificationManager.cancel(notification.getId());
                                else
                                    notificationManager.cancel(notification.getTag(), notification.getId());
                            }
                        }
                    }
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "cancel notifications - end");

                    //PPApplication.applicationPackageReplaced = true;

                    //PPApplication.cancelWork(PPApplication.PACKAGE_REPLACED_WORK_TAG);

                    // work for package replaced
                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(PPApplication.PACKAGE_REPLACED_WORK_TAG)
                                    .build();
                    try {
                        // do not test start of PPP, because is not started in this receiver
                        //if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                                    //if (PPApplication.logEnabled()) {
//                                    ListenableFuture<List<WorkInfo>> statuses;
//                                    statuses = workManager.getWorkInfosForUniqueWork(PPApplication.PACKAGE_REPLACED_WORK_TAG);
//                                    try {
//                                        List<WorkInfo> workInfoList = statuses.get();
//                                        PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doForFirstStart", "for=" + PPApplication.PACKAGE_REPLACED_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                    } catch (Exception ignored) {
//                                    }
//                                    //}

                            workManager.enqueueUniqueWork(PPApplication.PACKAGE_REPLACED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                        //}
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                else*/
                {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "start work for first start");

                    //PPApplication.cancelWork(PPApplication.AFTER_FIRST_START_WORK_TAG);

                    Data workData = new Data.Builder()
                            .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, __activateProfiles)
                            .putBoolean(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, _startFromExternalApplication)
                            .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, _startForExternalAppAction)
                            .putInt(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, _startForExternalAppDataType)
                            .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, _startForExternalAppDataValue)
                            //.putBoolean(PhoneProfilesService.EXTRA_SHOW_TOAST, serviceIntent != null)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(PPApplication.AFTER_FIRST_START_WORK_TAG)
                                    .setInputData(workData)
                                    //.setInitialDelay(5, TimeUnit.SECONDS)
                                    .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                    try {
                        if (PPApplication.getApplicationStarted(true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "workManager="+workManager);
                            if (workManager != null) {

//                                        //if (PPApplication.logEnabled()) {
//                                        ListenableFuture<List<WorkInfo>> statuses;
//                                        statuses = workManager.getWorkInfosForUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG);
//                                        try {
//                                            List<WorkInfo> workInfoList = statuses.get();
//                                            PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doForFirstStart", "for=" + PPApplication.AFTER_FIRST_START_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                        } catch (Exception ignored) {
//                                        }
//                                        //}

//                                        PPApplication.logE("[WORKER_CALL] PhoneProfilesService.doFirstStart", "xxx");
                                //workManager.enqueue(worker);
                                // !!! MUST BE APPEND_OR_REPLACE FOR EXTRA_START_FOR_EXTERNAL_APPLICATION !!!
                                workManager.enqueueUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                //}

                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "END");

                //dataWrapper.invalidateDataWrapper();

            } catch (Exception eee) {
                PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", Log.getStackTraceString(eee));
                //PPApplication.recordException(eee);
                throw eee;
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
    }

    private boolean doForPackageReplaced(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        int actualVersionCode = 0;
        // save version code
        try {
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        Permissions.setAllShowRequestPermissions(appContext, true);

        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
        //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
        //MobileCellsScanner.setShowEnableLocationNotification(appContext, true);
        //ActivateProfileHelper.setScreenUnlocked(appContext, true);

        if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "----- oldVersionCode=" + oldVersionCode);
            PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "----- actualVersionCode=" + actualVersionCode);
        }
        try {
            if (oldVersionCode < actualVersionCode) {
                PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "is new version");

                //PhoneProfilesService.cancelWork(DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START_WORK_TAG, appContext);

                if (actualVersionCode <= 2322) {
                    // for old packages use Priority in events
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                    //PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "applicationEventUsePriority=true");
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                    editor.apply();
                }
                if (actualVersionCode <= 2400) {
                    //PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "donation alarm restart");
                    PPApplication.setDaysAfterFirstStart(appContext, 0);
                    PPApplication.setDonationNotificationCount(appContext, 0);
                    DonationBroadcastReceiver.setAlarm(appContext);
                }

                //if (actualVersionCode <= 2500) {
                //    // for old packages hide profile notification from status bar if notification is disabled
                //    ApplicationPreferences.getSharedPreferences(appContext);
                //    if (Build.VERSION.SDK_INT < 26) {
                //        if (!ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true)) {
                //            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                //            PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "notificationShowInStatusBar=false");
                //            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                //            editor.apply();
                //        }
                //    }
                //}

                if (actualVersionCode <= 2700) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

                    //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);

                    editor.putBoolean(ActivatorActivity.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                    editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, false);
                    //editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, false);
                    editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, false);
                    editor.apply();
                }
                if (actualVersionCode <= 3200) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                    editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
                    editor.apply();
                }
                if (actualVersionCode <= 3500) {
                    if (!ApplicationPreferences.getSharedPreferences(appContext).contains(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT)) {
                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.applicationActivateWithAlert);

                        /*String rescan;
                        rescan = ApplicationPreferences.applicationEventLocationRescan;
                        if (rescan.equals("0"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
                        if (rescan.equals("2"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "3");
                        rescan = ApplicationPreferences.applicationEventWifiRescan;
                        if (rescan.equals("0"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
                        if (rescan.equals("2"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "3");
                        rescan = ApplicationPreferences.applicationEventBluetoothRescan;
                        if (rescan.equals("0"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
                        if (rescan.equals("2"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "3");
                        rescan = ApplicationPreferences.applicationEventMobileCellsRescan;
                        if (rescan.equals("0"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
                        if (rescan.equals("2"))
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "3");*/

                        editor.apply();
                    }

                    // continue donation notification
                    if (PPApplication.getDaysAfterFirstStart(appContext) == 8) {
                        PPApplication.setDonationNotificationCount(appContext, 1);
                    }
                }

                if (actualVersionCode <= 3900) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF,
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true));
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF,
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true));
                    editor.apply();
                }

                //if (actualVersionCode <= 4100) {
                //    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                //    if ((preferences.getInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0) == 3) &&
                //            (Build.VERSION.SDK_INT >= 26)) {
                //        // Toggle is not supported for wifi AP in Android 8+
                //        SharedPreferences.Editor editor = preferences.edit();
                //        editor.putInt(Profile.PREF_PROFILE_DEVICE_WIFI_AP, 0);
                //        editor.apply();
                //    }
                //}

                //if (actualVersionCode <= 4200) {
                //    ApplicationPreferences.getSharedPreferences(appContext);
                //    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                //    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                //    editor.apply();

                //    SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Context.MODE_PRIVATE);
                //    if (preferences.getInt(Profile.PREF_PROFILE_LOCK_DEVICE, 0) == 3) {
                //        editor = preferences.edit();
                //        editor.putInt(Profile.PREF_PROFILE_LOCK_DEVICE, 1);
                //        editor.apply();
                //    }
                //}

                //if (actualVersionCode <= 4400) {
                //    ApplicationPreferences.getSharedPreferences(appContext);
                //    if (!ApplicationPreferences.preferences.contains(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR)) {
                //        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetOneRowPrefIndicator(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness(appContext));
                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners(appContext));
                //        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType(appContext));
                //        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor(appContext));
                //        editor.apply();
                //    }
                //}

                if (actualVersionCode <= 4550) {
                    if (Build.VERSION.SDK_INT < 29) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        boolean darkBackground = preferences.getBoolean("notificationDarkBackground", false);
                        if (darkBackground) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                            editor.apply();
                        }
                    }
                }

                if (actualVersionCode <= 4600) {
                    List<Event> eventList = DatabaseHandler.getInstance(appContext).getAllEvents();
                    for (Event event : eventList) {
                        if (!event._eventPreferencesCalendar._searchString.isEmpty()) {
                            String searchStringOrig = event._eventPreferencesCalendar._searchString;
                            String searchStringNew = "";
                            String[] searchStringSplits = searchStringOrig.split("\\|");
                            for (String split : searchStringSplits) {
                                if (!split.isEmpty()) {
                                    String searchPattern = split;
                                    if (searchPattern.startsWith("!")) {
                                        searchPattern = "\\" + searchPattern;
                                    }
                                    if (!searchStringNew.isEmpty())
                                        //noinspection StringConcatenationInLoop
                                        searchStringNew = searchStringNew + "|";
                                    //noinspection StringConcatenationInLoop
                                    searchStringNew = searchStringNew + searchPattern;
                                }
                            }
                            event._eventPreferencesCalendar._searchString = searchStringNew;
                            DatabaseHandler.getInstance(appContext).updateEvent(event);
                        }
                    }
                }

                if (actualVersionCode <= 4870) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                    editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);
                    editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);

                    String theme = ApplicationPreferences.applicationTheme(appContext, false);
                    if (!(theme.equals("white") || theme.equals("dark") || theme.equals("night_mode"))) {
                        String defaultValue = "white";
                        if (Build.VERSION.SDK_INT >= 28)
                            defaultValue = "night_mode";
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue);
                        GlobalGUIRoutines.switchNightMode(appContext, true);
                    }

                    editor.apply();
                }

                if (actualVersionCode <= 5020) {
                    //PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "set \"night_mode\" theme");
                    if (Build.VERSION.SDK_INT >= 28) {
                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "night_mode");
                        GlobalGUIRoutines.switchNightMode(appContext, true);
                        editor.apply();
                    }
                }

                if (actualVersionCode <= 5250) {
                    if (oldVersionCode <= 5210) {
                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

                        if (Build.VERSION.SDK_INT >= 26) {
                            NotificationManagerCompat manager = NotificationManagerCompat.from(appContext);
                            try {
                                NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
                                if (channel != null) {
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED,
                                            channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
                                }
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }

                        int filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
                        if (filterEventsSelectedItem == 2)
                            filterEventsSelectedItem++;
                        editor.putInt(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
                        editor.apply();
                        ApplicationPreferences.editorEventsViewSelectedItem(appContext);
                    }
                }

                if (actualVersionCode <= 5330) {
                    if (oldVersionCode <= 5300) {
                        // for old packages hide profile notification from status bar if notification is disabled
                        if (Build.VERSION.SDK_INT < 26) {
                            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
                            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
                            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                                SharedPreferences.Editor editor = preferences.edit();
                                //PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "status bar is not permanent, set it!!");
                                editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, false);
                                editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, false);
                                editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, "2");
                                editor.apply();
                            }
                        }
                    }
                }

                if (actualVersionCode <= 5430) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    String notificationBackgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
                    SharedPreferences.Editor editor = preferences.edit();
                    if (!preferences.contains(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR))
                        editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
                    //if (!preferences.contains(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE))
                    //    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, false);
                    if (notificationBackgroundColor.equals("2")) {
                        //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, true);
                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
                    } else if (notificationBackgroundColor.equals("4")) {
                        //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, true);
                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "3");
                        editor.apply();
                    }
                    editor.apply();
                }

                /*
                if (actualVersionCode <= 5700) {
                    // restart service for move screen timeout 24hr and permanent to Keep screen on
                }
                */

                if (actualVersionCode <= 5910) {
                    ApplicationPreferences.startStopTargetHelps(appContext, false);
                }

                if (actualVersionCode <= 6200) {
                    if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        if (preferences != null) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, true);
                            editor.apply();
                        }
                    }
                }

                if (actualVersionCode <= 6700) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR,
                            preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, "0"));
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS,
                            preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100));
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS,
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false));
                    editor.apply();
                }

                if (actualVersionCode <= 6730) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT, false)) {
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT, "1");
                        editor.apply();
                    }
                }

                if (actualVersionCode <= 6800) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, false)) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, "1");
                        editor.apply();
                    }
                    if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, false)) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS, "1");
                        editor.apply();
                    }
                    if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, false)) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, "1");
                        editor.apply();
                    }
                }

            }
        } catch (Exception ee) {
            PPApplication.recordException(ee);
        }

        PPApplication.loadGlobalApplicationData(appContext);
        PPApplication.loadApplicationPreferences(appContext);
        PPApplication.loadProfileActivationData(appContext);

        if (oldVersionCode < actualVersionCode) {
            // block any profile and event actions for package replaced
//            PPApplication.logE("[BLOCK_ACTIONS] PhoneProfilesService.doForPackageReplaced", "true");
            PPApplication.setBlockProfileEventActions(true);

//            PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "MobileCellsScanner.enabledAutoRegistration=" + MobileCellsScanner.enabledAutoRegistration);
            if (MobileCellsScanner.enabledAutoRegistration) {
                MobileCellsScanner.stopAutoRegistration(appContext, true);
//                PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "start of wait for end of autoregistration");
                int count = 0;
                while (MobileCellsRegistrationService.serviceStarted && (count < 50)) {
                    GlobalUtils.sleep(100);
                    count++;
                }
//                PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "end of autoregistration");
            }

    //        // cancel all PPP notification (except PPService notification
    //        PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "cancel notifications - start");
    //        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
    //        if (notificationManager != null) {
    //            StatusBarNotification[] notitications = notificationManager.getActiveNotifications();
    //            for (StatusBarNotification notification : notitications) {
    //                if (notification.getId() != PPApplication.PROFILE_NOTIFICATION_ID) {
    //                    if (notification.getTag().isEmpty())
    //                        notificationManager.cancel(notification.getId());
    //                    else
    //                        notificationManager.cancel(notification.getTag(), notification.getId());
    //                }
    //            }
    //        }
    //        PPApplication.logE("PhoneProfilesService.doForPackageReplaced", "cancel notifications - end");

        }

        return oldVersionCode < actualVersionCode;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Context appContext = getApplicationContext();

//        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);
//        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "serviceHasFirstStart="+serviceHasFirstStart);
//        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "isServiceRunning (foreground)="+isServiceRunning(appContext, PhoneProfilesService.class, true));

        //startForegroundNotification = true;

        boolean isServiceRunning = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, true);
        PhoneProfilesNotification.showProfileNotification(appContext, !isServiceRunning, true, true);

        PPApplication.normalServiceStart = (intent != null);
        PPApplication.showToastForProfileActivation = (intent != null);

        if (!serviceHasFirstStart) {
            if (intent != null) {
                //ApplicationPreferences.startStopTargetHelps(appContext, false);

                String text = appContext.getString(R.string.ppp_app_name) + " " + appContext.getString(R.string.application_is_starting_toast);
                PPApplication.showToast(appContext, text, Toast.LENGTH_SHORT);
            }

            doForFirstStart(intent);
        }

        super.onStartCommand(intent, flags, startId);

        // do not use START_REDELIVER_INTENT because this remains intent and this is not good for me
        return START_STICKY;
    }

    void switchKeyguard() {
//        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_SWITCH_KEYGUARD");

        //boolean isScreenOn;
        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

        boolean secureKeyguard;
        //if (PPApplication.keyguardManager == null)
        //    KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (PPApplication.keyguardManager != null) {
            secureKeyguard = PPApplication.keyguardManager.isKeyguardSecure();
//            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "secureKeyguard=" + secureKeyguard);
            if (!secureKeyguard) {
//                PPApplication.logE("$$$ PhoneProfilesService.doCommand", "getLockScreenDisabled=" + ApplicationPreferences.prefLockScreenDisabled);

                if (PPApplication.isScreenOn) {
//                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "screen on");

                    if (ApplicationPreferences.prefLockScreenDisabled) {
//                        PPApplication.logE("$$$ PhoneProfilesService.doCommand", "disableKeyguard()");
                        reenableKeyguard();
                        disableKeyguard();
                    } else {
//                        PPApplication.logE("$$$ PhoneProfilesService.doCommand", "reenableKeyguard()");
                        reenableKeyguard();
                    }
                }
            }
        }
    }

    private void doCommand(Intent _intent) {
//        PPApplication.logE("*************** PhoneProfilesService.doCommand", "xxxxxx");
        if (_intent != null) {
//            if (_intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_SWITCH_KEYGUARD");
//            } else if (_intent.getBooleanExtra(EXTRA_REGISTER_RECEIVERS_AND_WORKERS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
//            } else if (_intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
//            } else if (_intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
//            } else if (_intent.getBooleanExtra(EXTRA_REGISTER_CONTENT_OBSERVERS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_REGISTER_CONTENT_OBSERVERS");
//            } else if (_intent.getBooleanExtra(EXTRA_REGISTER_CALLBACKS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_REGISTER_CALLBACKS");
//            } else if (_intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "******** EXTRA_SIMULATE_RINGING_CALL ********");
//            } else if (_intent.getBooleanExtra(EXTRA_RESCAN_SCANNERS, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_RESCAN_SCANNERS");
//            } else if (_intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "EXTRA_START_STOP_SCANNER");
//
//                switch (_intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
//                    case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_PERIODIC_SCANNING_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_PERIODIC_SCANNING_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_MOBILE_CELLS_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_MOBILE_CELLS_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_FORCE_START_MOBILE_CELLS_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_FORCE_START_MOBILE_CELLS_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_LOCATION_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_LOCATION_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_FORCE_START_ORIENTATION_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_FORCE_START_ORIENTATION_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_TWILIGHT_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_NOTIFICATION_SCANNER:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_NOTIFICATION_SCANNER");
//                        break;
//                    case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
//                        PPApplication.logE("-=***********=- PhoneProfilesService.doCommand", "SCANNER_RESTART_ALL_SCANNERS");
//                        break;
//                }
//
//            } else
//                PPApplication.logE("*************** PhoneProfilesService.doCommand", "???? OTHER ????");

            final Context appContext = getApplicationContext();
            final Intent intent = _intent;

            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
//            __handler.post(new DoCommandRunnable(
//                    getApplicationContext(), _intent) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.doCommand (1)");

                //Context appContext= appContextWeakRef.get();
                //Intent intent = intentWeakRef.get();
                //Profile profile = profileWeakRef.get();
                //Activity activity = activityWeakRef.get();

                //if (appContext == null)
                //    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadBroadcast", "!!! appContext == null !!!");
                //if (intent == null)
                //    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadBroadcast", "!!! intent == null !!!");

                //if ((appContext != null) && (intent != null)) {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService_doCommand");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[IN_THREAD_HANDLER]  PhoneProfilesService.doCommand", "--- START");

                        PhoneProfilesService ppService = PhoneProfilesService.getInstance();

                        if (ppService != null) {
                            /*if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                                // not needed, is already called in start of onStartCommand
                                //showProfileNotification();
                            }
                            else
                            if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                                clearProfileNotification();
                            }
                            else
                            if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                                // not needed, is already called in start of onStartCommand
                                //showProfileNotification();
                            }
                            else*/
                            /*if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                                //boolean isScreenOn;
                                //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

                                boolean secureKeyguard;
                                //if (PPApplication.keyguardManager == null)
                                //    PPApplication.keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                                if (PPApplication.keyguardManager != null) {
                                    secureKeyguard = PPApplication.keyguardManager.isKeyguardSecure();
                                    if (!secureKeyguard) {

                                        if (PPApplication.isScreenOn) {

                                            if (ApplicationPreferences.prefLockScreenDisabled) {
                                                ppService.reenableKeyguard();
                                                ppService.disableKeyguard();
                                            } else {
                                                ppService.reenableKeyguard();
                                            }
                                        }
                                    }
                                }
                            }*/
                            /*
                            else
                            if (intent.getBooleanExtra(EXTRA_START_LOCATION_UPDATES, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_START_LOCATION_UPDATES");
                                //synchronized (PPApplication.locationScannerMutex) {
                                    if (PhoneProfilesService.getLocationScanner() != null) {
                                        LocationScanner.useGPS = true;
                                        PhoneProfilesService.getLocationScanner().startLocationUpdates();
                                    }
                                //}
                            }
                            else
                            if (intent.getBooleanExtra(EXTRA_STOP_LOCATION_UPDATES, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_STOP_LOCATION_UPDATES");
                                //synchronized (PPApplication.locationScannerMutex) {
                                if (PhoneProfilesService.getLocationScanner() != null)
                                    PhoneProfilesService.getLocationScanner().stopLocationUpdates();
                                //}
                            }
                            */
                            /*else*/ if (intent.getBooleanExtra(EXTRA_REGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
                                ppService.registerEventsReceiversAndWorkers(true);
                            } else if (intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
                                ppService.unregisterEventsReceiversAndWorkers(false);
                            } else if (intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                ppService.registerPPPPExtenderReceiver(true, dataWrapper);
                                ppService.reregisterEventsReceiversAndWorkers();
                            } else if (intent.getBooleanExtra(EXTRA_REGISTER_CONTENT_OBSERVERS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_REGISTER_CONTENT_OBSERVERS");
                                ppService.registerAllTheTimeContentObservers(true);
                                ppService.registerContactsContentObservers(true);
                            } else if (intent.getBooleanExtra(EXTRA_REGISTER_CALLBACKS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_REGISTER_CALLBACKS");
                                ppService.registerAllTheTimeCallbacks(true);
                            } else if (intent.getBooleanExtra(EXTRA_REGISTER_PHONE_CALLS_LISTENER, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_REGISTER_PHONE_CALLS_LISTENER");
                                registerPhoneCallsListener(true, appContext);
                            } else if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "******** EXTRA_SIMULATE_RINGING_CALL ********");
                                ppService.doSimulatingRingingCall(intent);
                            } else if (intent.getBooleanExtra(EXTRA_RESCAN_SCANNERS, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_RESCAN_SCANNERS");
                                if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                                    if (PPApplication.locationScanner != null) {
                                        String provider = PPApplication.locationScanner.getProvider();
                                        PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                                    }
                                }

                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                boolean eventsFilled = false;
                                if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                                    dataWrapper.fillEventList();
                                    eventsFilled = true;
                                    ppService.scheduleWifiWorker(dataWrapper);
                                }
                                if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                    if (!eventsFilled) {
                                        dataWrapper.fillEventList();
                                    }
                                    ppService.scheduleBluetoothWorker(dataWrapper);
                                }

                                if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                                    if (PPApplication.mobileCellsScanner != null)
                                        PPApplication.mobileCellsScanner.rescanMobileCells();
                                }
                                if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                                    if (PPApplication.orientationScanner != null) {
                                        PPApplication.startHandlerThreadOrientationScanner();
                                        if (PPApplication.handlerThreadOrientationScanner != null)
                                            PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
                                    }

                                    //setOrientationSensorAlarm(appContext);
                                    //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                                    //sendBroadcast(intent);
                                }
                                if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                                    ppService.schedulePeriodicScanningWorker();
                                }

                                if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                                    if (PPApplication.notificationScannerRunning) {
                                        PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_NOTIFICATION, "SENSOR_TYPE_NOTIFICATION", 5);

                                        /*
                                        Data workData = new Data.Builder()
                                                .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_NOTIFICATION)
                                                .build();

                                        OneTimeWorkRequest worker =
                                                new OneTimeWorkRequest.Builder(MainWorker.class)
                                                        .addTag(MainWorker.HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG)
                                                        .setInputData(workData)
                                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                        .build();
                                        try {
                                            if (PPApplication.getApplicationStarted(true)) {
                                                WorkManager workManager = PPApplication.getWorkManagerInstance();
                                                if (workManager != null) {

//                                                //if (PPApplication.logEnabled()) {
//                                                ListenableFuture<List<WorkInfo>> statuses;
//                                                statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_SCANNER_WORK_TAG);
//                                                try {
//                                                    List<WorkInfo> workInfoList = statuses.get();
//                                                    PPApplication.logE("[TEST BATTERY] PPNotificationListenerService.onNotificationRemoved", "for=" + MainWorker.HANDLE_EVENTS_NOTIFICATION_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                                } catch (Exception ignored) {
//                                                }
//                                                //}

//                                                PPApplication.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                                                    //workManager.enqueue(worker);
                                                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                                }
                                            }
                                        } catch (Exception e) {
                                            PPApplication.recordException(e);
                                        }
                                        */
                                    }
                                }
                            }
                            //else
                            //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                            //    doSimulatingNotificationTone(intent);
                            else if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_START_STOP_SCANNER");
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                dataWrapper.fillEventList();
                                //dataWrapper.fillProfileList(false, false);
                                switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                                    /*case PPApplication.SCANNER_START_LOCATION_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_START_LOCATION_SCANNER");
                                        startLocationScanner(true, true, true, false);
                                        scheduleGeofenceWorker(true, true, false);
                                        break;*/
                                    /*case PPApplication.SCANNER_STOP_LOCATION_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_STOP_LOCATION_SCANNER");
                                        startLocationScanner(false, true, false, false);
                                        scheduleGeofenceWorker(false, false, false);
                                        break;*/
                                    /*case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_START_ORIENTATION_SCANNER");
                                        startOrientationScanner(true, true, true);
                                        break;*/
                                    /*case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                                        startOrientationScanner(false, true, false);
                                        break;*/
                                    /*case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                                        MobileCellsScanner.forceStart = false;
                                        startMobileCellsScanner(true, true, true, false, false);
                                        break;*/
                                    /*case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                                        startMobileCellsScanner(false, true, false, false, false);
                                        break;*/
                                    /*case PPApplication.SCANNER_START_TWILIGHT_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_START_TWILIGHT_SCANNER");
                                        startTwilightScanner(true, true, true);
                                        break;*/
                                    /*case PPApplication.SCANNER_STOP_TWILIGHT_SCANNER:
                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_STOP_TWILIGHT_SCANNER");
                                        startTwilightScanner(false, true, false);
                                        break;*/
                                    case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                        //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                        ppService.registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                        ppService.registerWifiScannerReceiver(true, dataWrapper, false);
                                        break;
                                    case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, true);
                                        //registerWifiStateChangedBroadcastReceiver(true, false, true);
                                        ppService.registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, true);
                                        ppService.registerWifiScannerReceiver(true, dataWrapper, true);
                                        break;
                                    case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                        //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                        ppService.registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                        ppService.registerBluetoothScannerReceivers(true, dataWrapper, false);
                                        break;
                                    case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                        //registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                                        ppService.registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, true);
                                        ppService.registerBluetoothScannerReceivers(true, dataWrapper, true);
                                        break;
                                    case PPApplication.SCANNER_RESTART_PERIODIC_SCANNING_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_PERIODIC_SCANNING_SCANNER");
                                        ppService.schedulePeriodicScanningWorker(/*dataWrapper, true*/);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
                                        //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                        //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                        ppService.registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                        ppService.registerWifiScannerReceiver(true, dataWrapper, false);
                                        ppService.scheduleWifiWorker(/*true,*/ dataWrapper/*, forScreenOn, false, false, true*/);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                                        //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                        ppService.registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                        ppService.registerBluetoothScannerReceivers(true, dataWrapper, false);
                                        ppService.scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_MOBILE_CELLS_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_MOBILE_CELLS_SCANNER");
                                        //MobileCellsScanner.forceStart = false;
                                        ppService.startMobileCellsScanner(true, true, dataWrapper, false, true);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_FORCE_START_MOBILE_CELLS_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_FORCE_START_MOBILE_CELLS_SCANNER");
                                        //MobileCellsScanner.forceStart = true;
                                        ppService.startMobileCellsScanner(true, false, dataWrapper, true, false);
                                        AvoidRescheduleReceiverWorker.enqueueWork();

                                        if (MobileCellsPreferenceX.forceStart) {
                                            Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView");
                                            LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                        }

                                        break;
                                    case PPApplication.SCANNER_RESTART_LOCATION_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_LOCATION_SCANNER");
                                        ppService.registerLocationModeChangedBroadcastReceiver(true, dataWrapper);
                                        ppService.startLocationScanner(true, true, dataWrapper, true);
                                        //scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                                        ppService.startOrientationScanner(true, false, dataWrapper/*, false*/);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    /*case PPApplication.SCANNER_FORCE_START_ORIENTATION_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_FORCE_START_ORIENTATION_SCANNER");
                                        //MobileCellsScanner.forceStart = true;
                                        ppService.startOrientationScanner(true, false, dataWrapper, true);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;*/
                                    case PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER]  PhoneProfilesService.doCommand", "SCANNER_RESTART_TWILIGHT_SCANNER");
                                        ppService.startTwilightScanner(true, false, dataWrapper);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_NOTIFICATION_SCANNER:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_NOTIFICATION_SCANNER");
                                        ppService.startNotificationScanner(true, false, dataWrapper);
                                        AvoidRescheduleReceiverWorker.enqueueWork();
                                        break;
                                    case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "SCANNER_RESTART_ALL_SCANNERS");

                                        final boolean fromBatteryChange = intent.getBooleanExtra(EXTRA_FROM_BATTERY_CHANGE, false);
                                        //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "fromBatteryChange="+fromBatteryChange);

                                        // background
                                        if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        /*PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn="+ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn);
                                        PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "PPApplication.isScreenOn="+PPApplication.isScreenOn);
                                        PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "background scanning - canRestart="+canRestart);*/
                                            if ((!fromBatteryChange) || canRestart) {
                                                //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "background scanning - restart");
                                                ppService.schedulePeriodicScanningWorker(/*dataWrapper, true*/);
                                            }
                                        }

                                        // wifi
                                        if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn="+ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn);
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "PPApplication.isScreenOn="+PPApplication.isScreenOn);
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "wifi - canRestart="+canRestart);
                                            if ((!fromBatteryChange) || canRestart) {
//                                            PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "wifi - restart");
                                                //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                                //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                                ppService.registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                                ppService.registerWifiScannerReceiver(true, dataWrapper, false);
                                                ppService.scheduleWifiWorker(/*true,*/ dataWrapper/*, forScreenOn, false, false, true*/);
                                            }
                                        }

                                        // bluetooth
                                        if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                            if ((!fromBatteryChange) || canRestart) {
                                                //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                                ppService.registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                                ppService.registerBluetoothScannerReceivers(true, dataWrapper, false);
                                                ppService.scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
                                            }
                                        }

                                        // mobile cells
                                        if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                            //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn="+ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn);
                                            //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "PPApplication.isScreenOn="+PPApplication.isScreenOn);
                                            //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "mobile cells - canRestart="+canRestart);
                                            if ((!fromBatteryChange) || canRestart) {
                                                //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "mobile cells - restart");
                                                //MobileCellsScanner.forceStart = false;
                                                ppService.startMobileCellsScanner(true, true, dataWrapper, false, true);
                                            }
                                        }

                                        // location
                                        if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                            if ((!fromBatteryChange) || canRestart) {
                                                ppService.registerLocationModeChangedBroadcastReceiver(true, dataWrapper);
                                                ppService.startLocationScanner(true, true, dataWrapper, true);
                                                //scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                            }
                                        }

                                        // orientation
                                        if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                            if ((!fromBatteryChange) || canRestart) {
                                                ppService.startOrientationScanner(true, true, dataWrapper/*, false*/);
                                            }
                                        }

                                        // notification
                                        if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                                            boolean canRestart = (!ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                            if ((!fromBatteryChange) || canRestart) {
                                                ppService.startNotificationScanner(true, true, dataWrapper);
                                            }
                                        }

                                        // twilight - DO NOT RESTART BECAUSE THIS MISS ACTUAL LOCATION
                                        //startTwilightScanner(true, false, dataWrapper);

                                        AvoidRescheduleReceiverWorker.enqueueWork();

                                        break;
                                }
                            }
                            /*else
                            if (intent.getBooleanExtra(EXTRA_RESTART_EVENTS, false)) {
                                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesService.doCommand", "EXTRA_RESTART_EVENTS");
                                final boolean unblockEventsRun = intent.getBooleanExtra(EXTRA_UNBLOCK_EVENTS_RUN, false);
                                //final boolean reactivateProfile = intent.getBooleanExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, false);
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                //dataWrapper.restartEvents(unblockEventsRun, true, reactivateProfile, false, false);
                                dataWrapper.restartEventsWithRescan(unblockEventsRun, false, false, false);
                                //dataWrapper.invalidateDataWrapper();
                            }*/
//                            else
//                                PPApplication.logE("[IN_THREAD_HANDLER]  PhoneProfilesService.doCommand", "???? OTHER ????");

                        }

//                        PPApplication.logE("[IN_THREAD_HANDLER]  PhoneProfilesService.doCommand", "--- END");

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
//        else
//            PPApplication.logE("*************** PhoneProfilesService.doCommand", "intent=null");
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        PPApplication.logE("[IN_LISTENER] PhoneProfilesService.onConfigurationChanged", "xxx");

        PPApplication.updateGUI(false, false, getApplicationContext());
//
//        int nightModeFlags =
//                newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switch (nightModeFlags) {
//            case Configuration.UI_MODE_NIGHT_YES:
//                Log.e("PhoneProfilesService.onConfigurationChanged", "UI_MODE_NIGHT_YES");
//                break;
//            case Configuration.UI_MODE_NIGHT_NO:
//            case Configuration.UI_MODE_NIGHT_UNDEFINED:
//                Log.e("PhoneProfilesService.onConfigurationChanged", "UI_MODE_NIGHT_NO");
//                break;
//        }

    }

    //------------------------

    // application cache -----------------

    public static void createApplicationsCache(boolean clear)
    {
        if (clear) {
            if (applicationsCache != null) {
                applicationsCache.clearCache(true);
            }
            applicationsCache = null;
        }
        if (applicationsCache != null)
            applicationsCache.clearCache(true);
        applicationsCache =  new ApplicationsCache();
    }

    public static ApplicationsCache getApplicationsCache()
    {
        return applicationsCache;
    }

    // contacts and contact groups cache -----------------

    public static void createContactsCache(Context context, boolean clear)
    {
        if (clear) {
            if (contactsCache != null)
                contactsCache.clearCache();
        }
        if (contactsCache == null)
            contactsCache = new ContactsCache();
        contactsCache.getContactList(context);
    }

    public static ContactsCache getContactsCache()
    {
        return contactsCache;
    }

    public static void createContactGroupsCache(Context context, boolean clear)
    {
        if (clear) {
            if (contactGroupsCache != null)
                contactGroupsCache.clearCache();
        }
        if (contactGroupsCache == null)
            contactGroupsCache = new ContactGroupsCache();
        contactGroupsCache.getContactGroupListX(context);
    }

    public static ContactGroupsCache getContactGroupsCache()
    {
        return contactGroupsCache;
    }

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
//        PPApplication.logE("$$$ PhoneProfilesService.disableKeyguard","keyguardLock="+PPApplication.keyguardLock);

        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.disableKeyguard();
            } catch (Exception e) {
                //Log.e("PhoneProfilesService", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private void reenableKeyguard()
    {
//        PPApplication.logE("$$$ PhoneProfilesService.reenableKeyguard","keyguardLock="+PPApplication.keyguardLock);

        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.reenableKeyguard();
            } catch (Exception e) {
                //Log.e("PhoneProfilesService", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    //--------------------------------------

    // Location ----------------------------------------------------------------

    private void startLocationScanner(boolean resetUseGPS) {
        //PPApplication.logE("PhoneProfilesService.startLocationScanner", "xxx");
        /*if (PPApplication.locationScanner != null) {
            PPApplication.locationScanner.disconnect();
            PPApplication.locationScanner = null;
        }*/

        if (PPApplication.locationScanner == null) {
            PPApplication.locationScanner = new LocationScanner(getApplicationContext());
            //PPApplication.logE("PhoneProfilesService.startLocationScanner", "locationScanner="+locationScanner);
            /*if (instance != null) {
                PPApplication.logE("PhoneProfilesService.startLocationScanner", "instance==this? " + (instance == this));
                PPApplication.logE("PhoneProfilesService.startLocationScanner", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
                PPApplication.logE("PhoneProfilesService.startLocationScanner", "PhoneProfilesService.getLocationScanner()=" + PhoneProfilesService.getInstance().getGeofencesScanner());
            }*/
            PPApplication.locationScanner.connect(resetUseGPS);
        }
        else {
            String provider = PPApplication.locationScanner.getProvider();
            PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
        }
    }

    private void stopLocationScanner() {
        //PPApplication.logE("PhoneProfilesService.stopLocationScanner", "xxx");
        if (PPApplication.locationScanner != null) {
            PPApplication.locationScanner.disconnect();
            PPApplication.locationScanner = null;
        }
    }

    boolean isLocationScannerStarted() {
        return (PPApplication.locationScanner != null);
    }

    LocationScanner getLocationScanner() {
        return PPApplication.locationScanner;
    }

    //--------------------------------------------------------------------------

    // Phone state ----------------------------------------------------------------

    private void startMobileCellsScanner() {
        /*if (PPApplication.mobileCellsScanner != null) {
            PPApplication.mobileCellsScanner.disconnect();
            PPApplication.mobileCellsScanner = null;
        }*/

        if (PPApplication.mobileCellsScanner == null) {
//            Log.e("PhoneProfilesService.startMobileCellsScanner", "START");
            PPApplication.mobileCellsScanner = new MobileCellsScanner(getApplicationContext());
            PPApplication.mobileCellsScanner.connect();
        }
        else {
            PPApplication.mobileCellsScanner.rescanMobileCells();
        }
    }

    private void stopMobileCellsScanner() {
        if (PPApplication.mobileCellsScanner != null) {
//            Log.e("PhoneProfilesService.stopMobileCellsScanner", "STOP");
            PPApplication.mobileCellsScanner.disconnect();
            PPApplication.mobileCellsScanner = null;
        }
    }

    boolean isMobileCellsScannerStarted() {
        return (PPApplication.mobileCellsScanner != null);
    }


    MobileCellsScanner getMobileCellsScanner() {
        return PPApplication.mobileCellsScanner;
    }

    //--------------------------------------------------------------------------

    // Device orientation ----------------------------------------------------------------

    private void startOrientationScanner() {
        //if (PPApplication.mStartedOrientationSensors)
        //    stopListeningOrientationSensors();

        if (!PPApplication.mStartedOrientationSensors)
            startListeningOrientationSensors();
        else {
            if (PPApplication.orientationScanner != null) {
                PPApplication.startHandlerThreadOrientationScanner();
                if (PPApplication.handlerThreadOrientationScanner != null)
                    PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
            }

            //setOrientationSensorAlarm(getApplicationContext());
            //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
            //sendBroadcast(intent);
        }
    }

    private void stopOrientationScanner() {
        stopListeningOrientationSensors();
    }

    boolean isOrientationScannerStarted() {
        return PPApplication.mStartedOrientationSensors;
    }

    private void startListeningOrientationSensors() {
        //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "mStartedOrientationSensors="+mStartedOrientationSensors);
        if (!PPApplication.mStartedOrientationSensors) {
            PPApplication.orientationScanner = new OrientationScanner();
            PPApplication.startHandlerThreadOrientationScanner();
            Handler orentationScannerHandler = new Handler(PPApplication.handlerThreadOrientationScanner.getLooper());
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "orientationScanner="+orientationScanner);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "(PPApplication.handlerThreadOrientationScanner="+PPApplication.handlerThreadOrientationScanner);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "handler="+handler);

            String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(getApplicationContext());
            if (isPowerSaveMode) {
                if (applicationEventOrientationScanInPowerSaveMode.equals("2"))
                    // start scanning in power save mode is not allowed
                    return;
            }
            else {
                if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                        // not scan in configured time
//                        PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "-- END - scan in time = 2 -------");
                        return;
                    }
                }
            }

            //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            //if (DatabaseHandler.getInstance(getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) == 0)
            //    return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
            if (isPowerSaveMode) {
                if (applicationEventOrientationScanInPowerSaveMode.equals("1"))
                    interval = 2 * interval;
            }
            else {
                if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("1")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                        interval = 2 * interval;
//                        PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "scan in time - 2x interval");
                    }
                }
            }

            if (PPApplication.accelerometerSensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.accelerometerSensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 1000000 * interval, handler);
            }
            if (PPApplication.magneticFieldSensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.magneticFieldSensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 1000000 * interval, handler);
            }

            if (PPApplication.proximitySensor != null) {
                PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                //if (PPApplication.proximitySensor.getFifoMaxEventCount() > 0)
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 200000 * interval, 1000000 * interval, handler);
                //else
                //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 1000000 * interval, handler);
            }

            if (PPApplication.lightSensor != null) {
                boolean registerLight = /*EventsPrefsFragment.forceStart ||*/
                        (DatabaseHandler.getInstance(getApplicationContext()).getOrientationWithLightSensorEventsCount() != 0);
                if (registerLight) {
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, SensorManager.SENSOR_DELAY_NORMAL, 1000000 * interval, orentationScannerHandler);
                    //if (PPApplication.lightSensor.getFifoMaxEventCount() > 0)
                    //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 200000 * interval, 1000000 * interval, handler);
                    //else
                    //    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 1000000 * interval, handler);
                }
            }

            //Sensor orientation = PPApplication.getOrientationSensor(getApplicationContext());
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","orientation="+orientation);
            PPApplication.mStartedOrientationSensors = true;

            PPApplication.handlerThreadOrientationScanner.tmpSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.tmpSideTimestamp = 0;

            PPApplication.handlerThreadOrientationScanner.previousResultDisplayUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.previousResultLight = 0;

            PPApplication.handlerThreadOrientationScanner.resultDisplayUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.resultLight = 0;

            if (PPApplication.orientationScanner != null) {
                PPApplication.startHandlerThreadOrientationScanner();
                if (PPApplication.handlerThreadOrientationScanner != null)
                    PPApplication.orientationScanner.runEventsHandlerForOrientationChange(PPApplication.handlerThreadOrientationScanner);
            }

            //setOrientationSensorAlarm(getApplicationContext());
            //Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
            //sendBroadcast(intent);
        }
    }

    private void stopListeningOrientationSensors() {
        //PPApplication.logE("PhoneProfilesService.stopListeningOrientationSensors", "PPApplication.sensorManager="+PPApplication.sensorManager);
        if (PPApplication.sensorManager != null) {
            PPApplication.sensorManager.unregisterListener(PPApplication.orientationScanner);
            //removeOrientationSensorAlarm(getApplicationContext());
            PPApplication.orientationScanner = null;
            //PPApplication.sensorManager = null;
        }
        PPApplication.mStartedOrientationSensors = false;
    }

    /*
    public void resetListeningOrientationSensors(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            stopListeningOrientationSensors();
            startListeningOrientationSensors();
        }
    }
    */

    /*
    private void removeOrientationSensorAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("EventPreferencesSMS.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_ORIENTATION_EVENT_SENSOR_TAG_WORK);
    }

    void setOrientationSensorAlarm(Context context)
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return;

        int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
            interval *= 2;

        calEndTime.setTimeInMillis((calEndTime.getTimeInMillis() - gmtOffset) + (interval * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        //if (PPApplication.logEnabled()) {
        //    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
        //    String result = sdf.format(alarmTime);
        //    PPApplication.logE("EventPreferencesOrientation.setAlarm", "alarmTime=" + result);
        //}

        //Intent intent = new Intent(context, OrientationEventEndBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
        //intent.setClass(context, OrientationEventEndBroadcastReceiver.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                //if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            }
        }
    }
    */

    // Twilight scanner ----------------------------------------------------------------

    private void startTwilightScanner() {
        /*if (PPApplication.twilightScanner != null) {
            PPApplication.twilightScanner.stop();
            PPApplication.twilightScanner = null;
        }*/

        if (PPApplication.twilightScanner == null) {
            PPApplication.twilightScanner = new TwilightScanner(getApplicationContext());
            PPApplication.twilightScanner.start();
        }
        else {
            PPApplication.twilightScanner.getTwilightState(/*true*/);
        }
    }

    private void stopTwilightScanner() {
        if (PPApplication.twilightScanner != null) {
            PPApplication.twilightScanner.stop();
            PPApplication.twilightScanner = null;
        }
    }

    private boolean isTwilightScannerStarted() {
        return (PPApplication.twilightScanner != null);
    }


    TwilightScanner getTwilightScanner() {
        return PPApplication.twilightScanner;
    }

    //---------------------------

    private void doSimulatingRingingCall(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false))
        {
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulate ringing call");

            Context context = getApplicationContext();

            ringingCallIsSimulating = false;

            // wait for change ringer mode + volume
            GlobalUtils.sleep(1500);

            int oldRingerMode = intent.getIntExtra(EXTRA_OLD_RINGER_MODE, 0);
            //int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EXTRA_OLD_ZEN_MODE, 0);
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingerMode="+oldRingerMode);
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldZenMode="+oldZenMode);

            int fromSIMSlot = intent.getIntExtra(EXTRA_CALL_FROM_SIM_SLOT, 0);
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "fromSIMSlot="+fromSIMSlot);

            Context appContext = context.getApplicationContext();
            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);

            // get ringtone configured in system at start call of EventsHanlder.handleEvents()
            String oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    switch (fromSIMSlot) {
                        case 0:
                            oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE);
                            break;
                        case 1:
                            oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE_SIM1);
                            break;
                        case 2:
                            oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE_SIM2);
                            break;
                    }
                }
            }
            if (oldRingtone == null)
                oldRingtone = "";
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingtone="+oldRingtone);

            int newRingerMode;
            int newZenMode;
            int ringerModeFromProfile = intent.getIntExtra(EXTRA_NEW_RINGER_MODE, 0);
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringerModeFromProfile="+ringerModeFromProfile);
            if (ringerModeFromProfile != 0) {
                newRingerMode = ringerModeFromProfile;
                newZenMode = Profile.ZENMODE_ALL;
                if (ringerModeFromProfile == Profile.RINGERMODE_ZENMODE) {
                    newZenMode = intent.getIntExtra(EXTRA_NEW_RINGER_MODE, Profile.ZENMODE_ALL);
//                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "zenModeFromProfile="+newZenMode);
                }
            }
            else {
                newRingerMode = ApplicationPreferences.prefRingerMode;
                newZenMode = ApplicationPreferences.prefZenMode;
            }
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingerMode="+newRingerMode);
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newZenMode="+newZenMode);

            String phoneNumber = "";
            if (PPPExtenderBroadcastReceiver.isEnabled(context/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, true
                    /*, "PhoneProfilesService.doSimulatingRingingCall"*/))
                phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "phoneNumber="+phoneNumber);

            // get ringtone from contact
            //boolean phoneNumberFound = false;
            String _ringtoneFromContact = "";
            if (!phoneNumber.isEmpty()) {
                try {
                    Uri contactLookup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    Cursor contactLookupCursor = context.getContentResolver().query(contactLookup, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.CUSTOM_RINGTONE}, null, null, null);
                    if (contactLookupCursor != null) {
                        if (contactLookupCursor.moveToNext()) {
                            _ringtoneFromContact = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.CUSTOM_RINGTONE));
                            if (_ringtoneFromContact == null)
                                _ringtoneFromContact = "";
//                        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromContact from contact="+_ringtoneFromContact);
                            //phoneNumberFound = true;
                        }
                        contactLookupCursor.close();
                    }
                } catch (SecurityException e) {
                    Permissions.grantPlayRingtoneNotificationPermissions(context, true);
                    _ringtoneFromContact = "";
                } catch (Exception e) {
                    _ringtoneFromContact = "";
                }
            }

            String _ringtoneFromProfile = "";
            String _ringtoneFromSystem = "";
            if (_ringtoneFromContact.isEmpty()) {
                // ringtone is not from ringing contact

                int ringtoneChangeFromProfile = intent.getIntExtra(EXTRA_NEW_RINTONE_CHANGE, 0);
//                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringtoneChangeFromProfile="+ringtoneChangeFromProfile);
                if (ringtoneChangeFromProfile != 0) {
                    String __ringtoneFromProfile = intent.getStringExtra(EXTRA_NEW_RINGTONE);
//                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "__ringtoneFromProfile="+__ringtoneFromProfile);
                    String[] splits = __ringtoneFromProfile.split("\\|");
                    if (!splits[0].isEmpty()) {
                        _ringtoneFromProfile = splits[0];
                    }
                }
//                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "fromSIMSlot="+fromSIMSlot);
                if (fromSIMSlot > 0) {
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            if (fromSIMSlot == 1) {
                                ringtoneChangeFromProfile = intent.getIntExtra(EXTRA_NEW_RINTONE_CHANGE_SIM1, 0);
//                                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringtoneChangeFromProfile (sim1)="+ringtoneChangeFromProfile);
                                if (ringtoneChangeFromProfile != 0) {
                                    String __ringtoneFromProfile = intent.getStringExtra(EXTRA_NEW_RINGTONE_SIM1);
//                                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "__ringtoneFromProfile (sim1)="+__ringtoneFromProfile);
                                    String[] splits = __ringtoneFromProfile.split("\\|");
                                    if (!splits[0].isEmpty()) {
                                        _ringtoneFromProfile = splits[0];
                                    }
                                }
                            }
                            if (fromSIMSlot == 2) {
                                ringtoneChangeFromProfile = intent.getIntExtra(EXTRA_NEW_RINTONE_CHANGE_SIM2, 0);
//                                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringtoneChangeFromProfile (sim2)="+ringtoneChangeFromProfile);
                                if (ringtoneChangeFromProfile != 0) {
                                    String __ringtoneFromProfile = intent.getStringExtra(EXTRA_NEW_RINGTONE_SIM2);
//                                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "__ringtoneFromProfile (sim2)="+__ringtoneFromProfile);
                                    String[] splits = __ringtoneFromProfile.split("\\|");
                                    if (!splits[0].isEmpty()) {
                                        _ringtoneFromProfile = splits[0];
                                    }
                                }
                            }
                        }
                    }
                }

                if (_ringtoneFromProfile.isEmpty()) {
                    // in profile is not change of ringtone
                    // get it from system
                    try {
                        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                        if (uri != null)
                            _ringtoneFromSystem = uri.toString();
                        else
                            _ringtoneFromSystem = "";
//                        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings="+_ringtoneFromSystem);

                        if (fromSIMSlot > 0) {
                            if (telephonyManager != null) {
                                int phoneCount = telephonyManager.getPhoneCount();
                                if (phoneCount > 1) {
                                    if (PPApplication.deviceIsSamsung) {
                                        if (fromSIMSlot == 1) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim1)="+_ringtoneFromSystem);
                                        }
                                        if (fromSIMSlot == 2) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_2");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim2)="+_ringtoneFromSystem);
                                        }
                                    } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                                        if (fromSIMSlot == 1) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim1)="+_ringtoneFromSystem);
                                        }
                                        if (fromSIMSlot == 2) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone2");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim2)="+_ringtoneFromSystem);
                                        }
                                    } else if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI)) {
                                        int useUniform = Settings.System.getInt(appContext.getContentResolver(), "ringtone_sound_use_uniform", 1);

                                        if ((fromSIMSlot == 1) || (useUniform == 1)) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_sound_slot_1");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim1)="+_ringtoneFromSystem);
                                        }

                               if ((fromSIMSlot == 2) || (useUniform == 0)) {
                                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_sound_slot_2");
                                            if (_uri != null)
                                                _ringtoneFromSystem = _uri;
//                                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "_ringtoneFromSystem from settings (sim2)="+_ringtoneFromSystem);
                                        }
                                    }
                                }
                            }
                        }

                    } catch (SecurityException e) {
                        Permissions.grantPlayRingtoneNotificationPermissions(context, false);
                        _ringtoneFromSystem = "";
                    } catch (Exception e) {
                        _ringtoneFromSystem = "";
                    }
                }
            }

            String newRingtone;
            // PPP do not support chnage of tone in contacts
            // for this contact ringtone has highest priority
            if (!_ringtoneFromContact.isEmpty())
                newRingtone = _ringtoneFromContact;
            else
            if (!_ringtoneFromProfile.isEmpty())
                newRingtone = _ringtoneFromProfile;
            else
                newRingtone = _ringtoneFromSystem;

//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingtone="+newRingtone);

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {

//                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "new sound mode is audible");

                boolean simulateRinging = false;
                //int stream = AudioManager.STREAM_RING;

                //if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                    //if (!(((newRingerMode == Profile.RINGERMODE_SILENT) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                    //        ((newRingerMode == Profile.RINGERMODE_ZENMODE) &&
                    //                ((newZenMode == Profile.ZENMODE_NONE) || (newZenMode == Profile.ZENMODE_ALARMS))))) {
                    //    // new ringer/zen mode is changed to another then NONE and ONLY_ALARMS
                    //    // Android 6 - ringerMode=Profile.RINGERMODE_SILENT = ONLY_ALARMS
                    //
                    //    // test old ringer and zen mode
                    //    if (((oldRingerMode == Profile.RINGERMODE_SILENT) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                    //            ((oldRingerMode == Profile.RINGERMODE_ZENMODE) &&
                    //                    ((oldZenMode == Profile.ZENMODE_NONE) || (oldZenMode == Profile.ZENMODE_ALARMS)))) {
                    //        // old ringer/zen mode is NONE and ONLY_ALARMS
                    //        simulateRinging = true;
                    //        stream = AudioManager.STREAM_ALARM;
                    //        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (1)");
                    //    }
                    //}
                    //if (android.os.Build.VERSION.SDK_INT >= 23) {
                    if (!ActivateProfileHelper.isAudibleRinging(oldRingerMode, oldZenMode)) {
                           simulateRinging = true;
                           //stream = AudioManager.STREAM_ALARM;
//                           PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "old sound mode is not audible");
                       }
                    //}

                    //if (!simulateRinging) {
                    //    if (!(((newRingerMode == Profile.RINGERMODE_SILENT) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                    //            ((newRingerMode == Profile.RINGERMODE_ZENMODE) && (newZenMode == Profile.ZENMODE_PRIORITY)))) {
                    //        // new ringer/zen mode is changed to another then PRIORITY
                    //        // Android 5 - ringerMode=Profile.RINGERMODE_SILENT = PRIORITY
                    //        if (((oldRingerMode == Profile.RINGERMODE_SILENT) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                    //                ((oldRingerMode == Profile.RINGERMODE_ZENMODE) && (oldZenMode == Profile.ZENMODE_PRIORITY))) {
                    //            // old ringer/zen mode is PRIORITY
                    //            simulateRinging = true;
                    //            if (oldSystemRingerMode == AudioManager.RINGER_MODE_SILENT) {
                    //                stream = AudioManager.STREAM_ALARM;
                    //                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (2)");
                    //            }
                    //            else {
                    //                //stream = AudioManager.STREAM_RING;
                    //                stream = AudioManager.STREAM_ALARM;
                    //                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=RING (2)");
                    //            }
                    //        }
                    //    }
                    //}
                    /*if (android.os.Build.VERSION.SDK_INT < 23) {
                        if (!ActivateProfileHelper.isAudibleRinging(oldRingerMode, oldZenMode)) {
                            simulateRinging = true;
                            if (oldSystemRingerMode == AudioManager.RINGER_MODE_SILENT) {
                                stream = AudioManager.STREAM_ALARM;
                                //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (2)");
                            }
                            else {
                                //stream = AudioManager.STREAM_RING;
                                stream = AudioManager.STREAM_ALARM;
                                //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=RING (2)");
                            }
                        }
                    }*/

                //}

                // simulate rnging when in profile is change of tone
                // STREAM_RING will be mutted, for this, will not be played both by system and PPP
                if (oldRingtone.isEmpty() || (!newRingtone.isEmpty() && !newRingtone.equals(oldRingtone)))
                    simulateRinging = true;

//                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulateRinging=" + simulateRinging);

                if (simulateRinging) {
                    int _ringingVolume;
                    String ringtoneVolumeFromProfile = intent.getStringExtra(EXTRA_NEW_RINGER_VOLUME);
//                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringtoneVolumeFromProfile=" + ringtoneVolumeFromProfile);
                    if (Profile.getVolumeChange(ringtoneVolumeFromProfile)) {
//                        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringing volume from profile");
                        _ringingVolume = Profile.getVolumeRingtoneValue(ringtoneVolumeFromProfile);
                    }
                    else {
//                        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringing volume from system");
                        _ringingVolume = ringingVolume;
                    }
                    startSimulatingRingingCall(/*stream,*/ newRingtone, _ringingVolume);
                }
            }

        }
//        else
//            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "**** it is not EXTRA_SIMULATE_RINGING_CALL ****");
    }

    private void startSimulatingRingingCall(/*int stream,*/ String ringtone, int ringingVolume) {
        stopSimulatingRingingCall(/*true*/true, getApplicationContext());
//        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingCallIsSimulating="+ringingCallIsSimulating);
        if (!ringingCallIsSimulating) {
            //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "stream="+stream);
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            //stopSimulatingNotificationTone(true);

            // stop playing notification sound, becaiuse must be played ringtone
            if (notificationPlayTimer != null) {
                notificationPlayTimer.cancel();
                notificationPlayTimer = null;
            }
            if ((notificationMediaPlayer != null) && notificationIsPlayed) {
                try {
                    if (notificationMediaPlayer.isPlaying())
                        notificationMediaPlayer.stop();
                } catch (Exception e) {
                    //PPApplication.recordException(e);
                }
                try {
                    notificationMediaPlayer.release();
                } catch (Exception e) {
                    //PPApplication.recordException(e);
                }

                notificationIsPlayed = false;
                notificationMediaPlayer = null;
            }
            // ----------

            /*// do not simulate ringing when ring or stream is muted
            if (audioManager != null) {
                if (audioManager.isStreamMute(AudioManager.STREAM_RING))
                    return;
            }*/

//            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringtone="+ringtone);

            if ((ringtone != null) && !ringtone.isEmpty()) {
//                PPApplication.logE("[VOLUMES] PhoneProfilesService.startSimulatingRingingCall", "internaChange=true");
                EventPreferencesVolumes.internalChange = true;
                RingerModeChangeReceiver.internalChange = true;

                // play repeating: default ringtone with ringing volume level
                try {
                    //AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);

                    if (audioManager != null) {
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }

                    //int requestType = AudioManager.AUDIOFOCUS_GAIN;
                    //int requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                    //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                    //int result = audioManager.requestAudioFocus(getApplicationContext(), usedRingingStream, requestType);
                    //if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        ringingMediaPlayer = new MediaPlayer();

                        /*if (stream == AudioManager.STREAM_RING) {
                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            ringingMediaPlayer.setAudioAttributes(attrs);
                            ringingMuted = 0;
                        }
                        else*/ {

                            // mute STREAM_RING, ringtone will be played via STREAM_ALARM
                            ringingMuted = (audioManager.isStreamMute(AudioManager.STREAM_RING)) ? 1 : -1;
                            if (ringingMuted == -1) {
                                EventPreferencesVolumes.internalChange = true;
                                audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            }

                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            ringingMediaPlayer.setAudioAttributes(attrs);
                        }
                        //ringingMediaPlayer.setAudioStreamType(stream);

                        ringingMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(ringtone));
                        ringingMediaPlayer.prepare();
                        ringingMediaPlayer.setLooping(true);

                        oldVolumeForRingingSimulation = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

//                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingVolume=" + ringingVolume);

                        int maximumRingValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

//                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingVolume="+ringingVolume);
                        float percentage = (float) ringingVolume / maximumRingValue * 100.0f;
                        int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * percentage);

//                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "mediaRingingVolume=" + mediaRingingVolume);

                        EventPreferencesVolumes.internalChange = true;
                        /*if (android.os.Build.VERSION.SDK_INT >= 23)
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        else
                            audioManager.setStreamMute(AudioManager.STREAM_RING, true);*/
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                        ringingMediaPlayer.start();

                        ringingCallIsSimulating = true;
//                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringing played");
                    //} else
                    //    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "focus not granted");
                }
                catch (Exception e) {
//                    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", Log.getStackTraceString(e));
                    ringingMediaPlayer = null;

                    PPExecutors.scheduleDisableInternalChangeExecutor();
                    PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

                    /*PPApplication.startHandlerThreadInternalChangeToFalse();
                    final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);*/
                    //PostDelayedBroadcastReceiver.setAlarm(
                    //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, getApplicationContext());
                    Permissions.grantPlayRingtoneNotificationPermissions(getApplicationContext(), false);
                }
            }
        }
    }

    static void stopSimulatingRingingCall(/*boolean abandonFocus*/boolean disableInternalChange, Context context) {
        //if (ringingCallIsSimulating) {
//            PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "xxx");
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            if (ringingMediaPlayer != null) {
                try {
                    if (ringingMediaPlayer.isPlaying())
                        ringingMediaPlayer.stop();
                } catch (Exception e) {
                    //PPApplication.recordException(e);
                }
                try {
                    ringingMediaPlayer.release();
                } catch (Exception e) {
                    //PPApplication.recordException(e);
                }
                ringingMediaPlayer = null;

                try {
                    if (ringingCallIsSimulating) {
                        EventPreferencesVolumes.internalChange = true;
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldVolumeForRingingSimulation, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        if (ringingMuted == -1) {
                            // ringing was not mutted at start of simulation and was mutted by simuation
                            // result: must be unmutted
                            if (audioManager.isStreamMute(AudioManager.STREAM_RING)) {
                                EventPreferencesVolumes.internalChange = true;
                                audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            }
                            // 0 = not detected by simulation
                            ringingMuted = 0;
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

//                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "ringing stopped");
            }
            /*if (abandonFocus) {
                if (audioManager != null)
                    audioManager.abandonAudioFocus(getApplicationContext());
            }*/
        //}
        ringingCallIsSimulating = false;

        if (disableInternalChange) {
            PPExecutors.scheduleDisableInternalChangeExecutor();
            PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

            /*PPApplication.startHandlerThreadInternalChangeToFalse();
            final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "disable ringer mode change internal change");
                    RingerModeChangeReceiver.internalChange = false;
                }
            }, 3000);*/
                //PostDelayedBroadcastReceiver.setAlarm(
                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, getApplicationContext());
        }
    }

    /*private void doSimulatingNotificationTone(Intent intent) {
        if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false) &&
                !ringingCallIsSimulating)
        {
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "simulate notification tone");

            Context context = getApplicationContext();

            notificationToneIsSimulating = false;

            int oldRingerMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_ZEN_MODE, 0);
            String oldNotificationTone = intent.getStringExtra(EventsHandler.EXTRA_OLD_NOTIFICATION_TONE);
            int newRingerMode = ActivateProfileHelper.getRingerMode(context);
            int newZenMode = ActivateProfileHelper.getZenMode(context);
            String newNotificationTone;

            try {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                if (uri != null)
                    newNotificationTone = uri.toString();
                else
                    newNotificationTone = "";
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, true, false);
                newNotificationTone = "";
            } catch (Exception e) {
                newNotificationTone = "";
            }

            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "oldRingerMode=" + oldRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "oldSystemRingerMode=" + oldSystemRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "oldZenMode=" + oldZenMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "newRingerMode=" + newRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "newZenMode=" + newZenMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "oldNotificationTone=" + oldNotificationTone);
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "newNotificationTone=" + newNotificationTone);

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {

                PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "notification is audible");

                boolean simulateNotificationTone = false;
                int stream = AudioManager.STREAM_NOTIFICATION;

                if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                    if (!(((newRingerMode == 4) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                            ((newRingerMode == 5) && ((newZenMode == 3) || (newZenMode == 6))))) {
                        // actual ringer/zen mode is changed to another then NONE and ONLY_ALARMS
                        // Android 6 - ringerMode=4 = ONLY_ALARMS

                        // test old ringer and zen mode
                        if (((oldRingerMode == 4) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                                ((oldRingerMode == 5) && ((oldZenMode == 3) || (oldZenMode == 6)))) {
                            // old ringer/zen mode is NONE and ONLY_ALARMS
                            simulateNotificationTone = true;
                            stream = AudioManager.STREAM_ALARM;
                            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "stream=MUSIC");
                        }
                    }

                    if (!simulateNotificationTone) {
                        if (!(((newRingerMode == 4) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                                ((newRingerMode == 5) && (newZenMode == 2)))) {
                            // actual ringer/zen mode is changed to another then PRIORITY
                            // Android 5 - ringerMode=4 = PRIORITY
                            if (((oldRingerMode == 4) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                                    ((oldRingerMode == 5) && (oldZenMode == 2))) {
                                // old ringer/zen mode is PRIORITY
                                simulateNotificationTone = true;
                                if (oldSystemRingerMode == AudioManager.RINGER_MODE_SILENT) {
                                    stream = AudioManager.STREAM_ALARM;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "stream=MUSIC");
                                }
                                else {
                                    stream = AudioManager.STREAM_NOTIFICATION;
                                    //stream = AudioManager.STREAM_ALARM;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "stream=NOTIFICATION");
                                }
                            }
                        }
                    }
                }

                if (oldNotificationTone.isEmpty() || (!newNotificationTone.isEmpty() && !newNotificationTone.equals(oldNotificationTone)))
                    simulateNotificationTone = true;

                PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "simulateNotificationTone=" + simulateNotificationTone);

                if (simulateNotificationTone)
                    startSimulatingNotificationTone(stream, newNotificationTone);
            }

        }
    }

    private void startSimulatingNotificationTone(int stream, String notificationTone) {
        stopSimulatingNotificationTone(true);
        if ((!ringingCallIsSimulating) && (!notificationToneIsSimulating)) {
            PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "stream="+stream);
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            if ((eventNotificationPlayTimer != null) && eventNotificationIsPlayed) {
                eventNotificationPlayTimer.cancel();
                eventNotificationPlayTimer = null;
            }
            if ((eventNotificationMediaPlayer != null) && eventNotificationIsPlayed) {
                if (eventNotificationMediaPlayer.isPlaying())
                    eventNotificationMediaPlayer.stop();
                eventNotificationIsPlayed = false;
                eventNotificationMediaPlayer = null;
            }

            if ((notificationTone != null) && !notificationTone.isEmpty()) {
                RingerModeChangeReceiver.removeAlarm(getApplicationContext());
                RingerModeChangeReceiver.internalChange = true;

                usedNotificationStream = stream;
                // play repeating: default ringtone with ringing volume level
                try {
                    AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_NORMAL);

                    //int requestType = AudioManager.AUDIOFOCUS_GAIN;
                    int requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                    if (android.os.Build.VERSION.SDK_INT >= 19)
                        requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                    //int result = audioManager.requestAudioFocus(getApplicationContext(), usedNotificationStream, requestType);
                    //if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        notificationMediaPlayer = new MediaPlayer();

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build();
                        notificationMediaPlayer.setAudioAttributes(attrs);
                        //notificationMediaPlayer.setAudioStreamType(usedNotificationStream);

                        notificationMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(notificationTone));
                        notificationMediaPlayer.prepare();
                        notificationMediaPlayer.setLooping(false);


                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "notificationVolume=" + notificationVolume);

                        int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                        mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "mediaNotificationVolume=" + mediaNotificationVolume);

                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaNotificationVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                        notificationMediaPlayer.start();

                        notificationToneIsSimulating = true;
                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "notification played");

                        final Context context = getApplicationContext();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                stopSimulatingNotificationTone(true);
                            }
                        }, notificationMediaPlayer.getDuration());

                    //} else
                    //    PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "focus not granted");
                } catch (SecurityException e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", " security exception");
                    Permissions.grantPlayRingtoneNotificationPermissions(getApplicationContext(), true, false);
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
                } catch (Exception e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "exception");
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
                }
            }
        }
    }

    public void stopSimulatingNotificationTone(boolean abandonFocus) {
        //if (notificationToneIsSimulating) {
        PPApplication.logE("PhoneProfilesService.stopSimulatingNotificationTone", "xxx");
        if (audioManager == null )
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (notificationMediaPlayer != null) {
            try {
                if (notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.stop();
            } catch (Exception ignored) {};
            notificationMediaPlayer.release();
            notificationMediaPlayer = null;

            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            PPApplication.logE("PhoneProfilesService.stopSimulatingNotificationTone", "notification stopped");
        }
        //if (abandonFocus)
        //    audioManager.abandonAudioFocus(getApplicationContext());
        //}
        notificationToneIsSimulating = false;
        RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
    }*/

    /*
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (audioManager == null )
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS_TRANSIENT");
            //if ((ringingMediaPlayer != null) && ringingCallIsSimulating)
            //    if (ringingMediaPlayer.isPlaying())
            //        ringingMediaPlayer.pause();
            //if ((notificationMediaPlayer != null) && notificationToneIsSimulating)
            //    if (notificationMediaPlayer.isPlaying())
            //        notificationMediaPlayer.pause();
            stopSimulatingRingingCall(false);
            //stopSimulatingNotificationTone(false);
            audioManager.abandonAudioFocus(getApplicationContext());
        }
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
            //if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
            //    if (usedNotificationStream == AudioManager.STREAM_ALARM)
            //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            //}
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_GAIN");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                if (!ringingMediaPlayer.isPlaying())
                    ringingMediaPlayer.start();
            }
            //if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
            //    if (usedNotificationStream == AudioManager.STREAM_ALARM)
            //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaNotificationVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            //    if (!notificationMediaPlayer.isPlaying())
            //        notificationMediaPlayer.start();
            //}
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS");
            stopSimulatingRingingCall(false);
            //stopSimulatingNotificationTone(false);
            audioManager.abandonAudioFocus(getApplicationContext());
        }
    }
    */

    //---------------------------

    private void stopPlayNotificationSound() {
        if (notificationPlayTimer != null) {
            notificationPlayTimer.cancel();
            notificationPlayTimer = null;
        }
        if ((notificationMediaPlayer != null) && notificationIsPlayed) {
            try {
                if (notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.stop();
            } catch (Exception e) {
                //PPApplication.recordException(e);
            }
            try {
                notificationMediaPlayer.release();
            } catch (Exception e) {
                //PPApplication.recordException(e);
            }

            //if (oldVolumeForPlayNotificationSound != -1) {
            //    try {
            //        EventPreferencesVolumes.internalChange = true;
            //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldVolumeForPlayNotificationSound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            //        DisableVolumesInternalChangeWorker.enqueueWork();
            //    } catch (Exception e) {
            //        //PPApplication.recordException(e);
            //    }
            //}

            notificationIsPlayed = false;
            notificationMediaPlayer = null;
        }
    }

    public void playNotificationSound(final String notificationSound,
                                       final boolean notificationVibrate/*,
                                       final boolean playAlsoInSilentMode*/) {

        //final Context appContext = getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast();
        //final Handler __handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        //__handler.post(() -> {
        Runnable runnable = () -> {

            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            //int ringerMode = ApplicationPreferences.prefRingerMode;
            //int zenMode = ApplicationPreferences.prefZenMode;
            //boolean isAudible = ActivateProfileHelper.isAudibleRinging(ringerMode, zenMode/*, false*/);
            int systemZenMode = ActivateProfileHelper.getSystemZenMode(getApplicationContext());
            boolean isAudible =
                    ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode/*, getApplicationContext()*/);
            //PPApplication.logE("PhoneProfilesService.playNotificationSound", "isAudible="+isAudible);

            if (notificationVibrate || ((!isAudible) /*&& (!playAlsoInSilentMode)*/ && (!notificationSound.isEmpty()))) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if ((vibrator != null) && vibrator.hasVibrator()) {
                    //PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                    try {
                        if (Build.VERSION.SDK_INT >= 26)
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        else
                            vibrator.vibrate(300);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }

            //PPApplication.logE("PhoneProfilesService.playNotificationSound", "ringingCallIsSimulating="+ringingCallIsSimulating);
            if ((!ringingCallIsSimulating)/* && (!notificationToneIsSimulating)*/) {

                stopPlayNotificationSound();

                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "notificationSound="+notificationSound);
                if (!notificationSound.isEmpty())
                {
                    if (isAudible/* || playAlsoInSilentMode*/) {

                        Uri notificationUri = Uri.parse(notificationSound);
                        try {
                            ContentResolver contentResolver = getContentResolver();
                            grantUriPermission(PPApplication.PACKAGE_NAME, notificationUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            contentResolver.takePersistableUriPermission(notificationUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            // java.lang.SecurityException: UID 10157 does not have permission to
                            // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                            // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                            //Log.e("PhoneProfilesService.playNotificationSound", Log.getStackTraceString(e));
                            //PPApplication.recordException(e);
                        }

                        try {
                            notificationMediaPlayer = new MediaPlayer();

                            int usage = AudioAttributes.USAGE_NOTIFICATION;
                            //if (!isAudible)
                            //    usage = AudioAttributes.USAGE_ALARM;

                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(usage)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            notificationMediaPlayer.setAudioAttributes(attrs);

                            notificationMediaPlayer.setDataSource(getApplicationContext(), notificationUri);
                            notificationMediaPlayer.prepare();
                            notificationMediaPlayer.setLooping(false);

                            //if (!isAudible) {
                            //    oldVolumeForPlayNotificationSound = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                            //    int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                            //    int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * 75.0f);
                            //    EventPreferencesVolumes.internalChange = true;
                            //    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            //}
                            //else
                            //    oldVolumeForPlayNotificationSound = -1;

                            notificationMediaPlayer.start();

                            notificationIsPlayed = true;

                            notificationPlayTimer = new Timer();
                            notificationPlayTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {

                                    if (notificationMediaPlayer != null) {
                                        try {
                                            if (notificationMediaPlayer.isPlaying())
                                                notificationMediaPlayer.stop();
                                        } catch (Exception e) {
                                            //PPApplication.recordException(e);
                                        }
                                        try {
                                            notificationMediaPlayer.release();
                                        } catch (Exception e) {
                                            //PPApplication.recordException(e);
                                        }

                                        //if ((notificationIsPlayed) && (oldVolumeForPlayNotificationSound != -1)) {
                                        //    try {
                                        //        EventPreferencesVolumes.internalChange = true;
                                        //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldVolumeForPlayNotificationSound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        //    } catch (Exception e) {
                                        //        //PPApplication.recordException(e);
                                        //    }
                                        //}

                                    }

                                    notificationIsPlayed = false;
                                    notificationMediaPlayer = null;

                                    notificationPlayTimer = null;
                                }
                            }, notificationMediaPlayer.getDuration());

                        }
                        catch (Exception e) {
                            //PPApplication.logE("PhoneProfilesService.playNotificationSound", "exception");
                            stopPlayNotificationSound();

                            Permissions.grantPlayRingtoneNotificationPermissions(getApplicationContext(), false);
                        }
                    }
                }
            }

        }; //);
        PPApplication.createPlayToneExecutor();
        PPApplication.playToneExecutor.submit(runnable);
    }


    //---------------------------

    static final String EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION = "from_red_text_preferences_notification";

    static boolean displayPreferencesErrorNotification(Profile profile, Event event, boolean againCheckAccessibilityInDelay, Context context) {
        if ((profile == null) && (event == null))
            return false;

        if (!PPApplication.getApplicationStarted(true))
            return false;

        if ((profile != null) && (!ProfilesPrefsFragment.isRedTextNotificationRequired(profile, againCheckAccessibilityInDelay, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id,
                        PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }
        if ((event != null) && (!EventsPrefsFragment.isRedTextNotificationRequired(event, againCheckAccessibilityInDelay, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                        PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }

        int notificationID = 0;
        String notificationTag = null;

        String nTitle = "";
        String nText = "";

        Intent intent = null;

        if (profile != null) {
            intent = new Intent(context, ProfilesPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(EditorActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EditorActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
        }
        if (event != null) {
            intent = new Intent(context, EventsPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            intent.putExtra(EditorActivity.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EditorActivity.EXTRA_PREDEFINED_EVENT_INDEX, 0);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (profile != null) {
            nTitle = context.getString(R.string.profile_preferences_red_texts_title);
            nText = context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                    "\"" + profile._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = context.getString(R.string.ppp_app_name);
//                nText = context.getString(R.string.profile_preferences_red_texts_title) + ": " +
//                        context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
//                        "\"" + profile._name + "\" " +
//                        context.getString(R.string.preferences_red_texts_text_2) + " " +
//                        context.getString(R.string.preferences_red_texts_text_click);
//            }

            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            notificationID = PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id;
        }

        if (event != null) {
            nTitle = context.getString(R.string.event_preferences_red_texts_title);
            nText = context.getString(R.string.event_preferences_red_texts_text_1) + " " +
                    "\"" + event._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = context.getString(R.string.ppp_app_name);
//                nText = context.getString(R.string.event_preferences_red_texts_title) + ": " +
//                        context.getString(R.string.event_preferences_red_texts_text_1) + " " +
//                        "\"" + event._name + "\" " +
//                        context.getString(R.string.preferences_red_texts_text_2) + " " +
//                        context.getString(R.string.preferences_red_texts_text_click);
//            }

            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            notificationID = PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id;
        }

        intent.putExtra(EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, true);

        PPApplication.createGrantPermissionNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            // do not cancel, mBuilder.setOnlyAlertOnce(true); will not be working
            // mNotificationManager.cancel(notificationID);
            mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("EditorActivity.displayNotGrantedPermissionsNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        return true;
    }

    //--------------------------

/*
    private static abstract class DoCommandRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<Intent> intentWeakRef;

        DoCommandRunnable(Context appContext, Intent intent) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.intentWeakRef = new WeakReference<>(intent);
        }

    }
*/

    //---------------------------

    //DO NOT CALL THIS !!! THIS IS CALLED ALSO WHEN, FOR EXAMPLE, ACTIVATOR GETS DISPLAYED !!!
    /*
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "rootIntent="+rootIntent.toString());

        //if (PPApplication.screenTimeoutHandler != null) {
        //    PPApplication.screenTimeoutHandler.post(new Runnable() {
        //        public void run() {
        //            //ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
        //            PPApplication.logE("******** PhoneProfilesService.onTaskRemoved", "remove wakelock");
        //            ActivateProfileHelper.removeKeepScreenOnView();
        //        }
        //    });
        //}

        super.onTaskRemoved(rootIntent);
    }
    */
}
