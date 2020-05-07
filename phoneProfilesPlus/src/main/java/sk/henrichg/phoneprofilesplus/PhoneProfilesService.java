package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
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
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

//import me.drakeet.support.toast.ToastCompat;


public class PhoneProfilesService extends Service
{
    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;

    // must be in PPService !!!
    static boolean startForegroundNotification = true;

    static final String ACTION_COMMAND = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_COMMAND";
    //private static final String ACTION_STOP = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_STOP_SERVICE";
    static final String ACTION_START_LAUNCHER_FROM_NOTIFICATION = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION";
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
    static final String ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".GeofencesScannerSwitchGPSBroadcastReceiver";
    static final String ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".LockDeviceActivityFinishBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".AlarmClockBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".AlarmClockEventEndBroadcastReceiver";
    static final String ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".NotificationEventEndBroadcastReceiver";
    private static final String ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".OrientationEventBroadcastReceiver";
    static final String ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".DeviceBootEventEndBroadcastReceiver";

    //static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    //static final String EXTRA_ONLY_START = "only_start";
    //static final String EXTRA_DEACTIVATE_PROFILE = "deactivate_profile";
    static final String EXTRA_ACTIVATE_PROFILES = "activate_profiles";
    //static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    //static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";
    static final String EXTRA_SIMULATE_RINGING_CALL = "simulate_ringing_call";
    static final String EXTRA_OLD_RINGER_MODE = "old_ringer_mode";
    static final String EXTRA_OLD_SYSTEM_RINGER_MODE = "old_system_ringer_mode";
    static final String EXTRA_OLD_ZEN_MODE = "old_zen_mode";
    static final String EXTRA_OLD_RINGTONE = "old_ringtone";
    //static final String EXTRA_SIMULATE_NOTIFICATION_TONE = "simulate_notification_tone";
    //static final String EXTRA_OLD_NOTIFICATION_TONE = "old_notification_tone";
    static final String EXTRA_OLD_SYSTEM_RINGER_VOLUME = "old_system_ringer_volume";
    static final String EXTRA_REGISTER_RECEIVERS_AND_WORKERS = "register_receivers_and_workers";
    static final String EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS = "unregister_receivers_and_workers";
    static final String EXTRA_REREGISTER_RECEIVERS_AND_WORKERS = "reregister_receivers_and_workers";
    static final String EXTRA_REGISTER_CONTENT_OBSERVERS = "register_content_observers";
    static final String EXTRA_REGISTER_CALLBACKS = "register_callbacks";
    static final String EXTRA_FROM_BATTERY_CHANGE = "from_battery_change";
    //static final String EXTRA_START_LOCATION_UPDATES = "start_location_updates";
    //private static final String EXTRA_STOP_LOCATION_UPDATES = "stop_location_updates";
    //static final String EXTRA_RESTART_EVENTS = "restart_events";
    static final String EXTRA_ALSO_RESCAN = "also_rescan";
    static final String EXTRA_UNBLOCK_EVENTS_RUN = "unblock_events_run";
    //static final String EXTRA_REACTIVATE_PROFILE = "reactivate_profile";
    static final String EXTRA_LOG_TYPE = "log_type";
    static final String EXTRA_DELAYED_WORK = "delayed_work";
    static final String EXTRA_SENSOR_TYPE = "sensor_type";
    static final String EXTRA_ELAPSED_ALARMS_WORK = "elapsed_alarms_work";
    static final String EXTRA_FROM_DO_FIRST_START = "from_do_first_start";

    //------------------------

    private static ContactsCache contactsCache;
    private static ContactGroupsCache contactGroupsCache;

    private AudioManager audioManager = null;
    private boolean ringingCallIsSimulating = false;
    //private boolean notificationToneIsSimulating = false;
    int ringingVolume = 0;
    //public static int notificationVolume = 0;
    private int oldMediaVolume = 0;
    private MediaPlayer ringingMediaPlayer = null;
    //private MediaPlayer notificationMediaPlayer = null;
    //private int mediaRingingVolume = 0;
    //private int mediaNotificationVolume = 0;
    //private int usedRingingStream = AudioManager.STREAM_MUSIC;
    //private int usedNotificationStream = AudioManager.STREAM_MUSIC;

    private MediaPlayer notificationMediaPlayer = null;
    private boolean notificationIsPlayed = false;
    private Timer notificationPlayTimer = null;

    String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;
    boolean connectToSSIDStarted = false;


    //--------------------------

    private final BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doCommand(intent);
        }
    };

    /*
    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //PPApplication.logE("PhoneProfilesService.stopReceiver", "xxx");
            try {
                //noinspection deprecation
                context.removeStickyBroadcast(intent);
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stopReceiver", Log.getStackTraceString(e));
                //PPApplication.recordException(e);
            }
            try {
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
        startForegroundNotification = true;

        PPApplication.logE("PhoneProfilesService.onCreate", "before show profile notification");

        // show empty notification to avoid ANR in api level 26
        showProfileNotification(true, true/*, false*/);

        PPApplication.logE("PhoneProfilesService.onCreate", "after show profile notification");

        if (PPApplication.getInstance() == null) {
            PPApplication.sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            PPApplication.accelerometerSensor = PPApplication.getAccelerometerSensor(getApplicationContext());
            PPApplication.magneticFieldSensor = PPApplication.getMagneticFieldSensor(getApplicationContext());
            PPApplication.proximitySensor = PPApplication.getProximitySensor(getApplicationContext());
            PPApplication.lightSensor = PPApplication.getLightSensor(getApplicationContext());

            PPApplication.loadApplicationPreferences(getApplicationContext());
            PPApplication.loadGlobalApplicationData(getApplicationContext());
            PPApplication.loadProfileActivationData(getApplicationContext());
        }

        serviceHasFirstStart = false;
        //serviceRunning = false;
        //runningInForeground = false;
        PPApplication.applicationFullyStarted = false;
        //ApplicationPreferences.forceNotUseAlarmClock = false;

        final Context appContext = getApplicationContext();

        //appContext.registerReceiver(stopReceiver, new IntentFilter(PhoneProfilesService.ACTION_STOP));
        //LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(commandReceiver, new IntentFilter(ACTION_COMMAND));

        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION);
        appContext.registerReceiver(PPApplication.startLauncherFromNotificationReceiver, intentFilter5);

        appContext.registerReceiver(PPApplication.showProfileNotificationBroadcastReceiver, new IntentFilter(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION));
        appContext.registerReceiver(PPApplication.updateGUIBroadcastReceiver, new IntentFilter(PPApplication.ACTION_UPDATE_GUI));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.dashClockBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver"));

        PPApplication.setNotificationProfileName(appContext, "");
        PPApplication.setWidgetProfileName(appContext, 1, "");
        PPApplication.setWidgetProfileName(appContext, 2, "");
        PPApplication.setWidgetProfileName(appContext, 3, "");
        PPApplication.setWidgetProfileName(appContext, 4, "");
        PPApplication.setWidgetProfileName(appContext, 5, "");
        PPApplication.setActivityProfileName(appContext, 1, "");
        PPApplication.setActivityProfileName(appContext, 2, "");
        PPApplication.setActivityProfileName(appContext, 3, "");

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
            }
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
        } catch (Exception e) {
            // https://github.com/firebase/firebase-android-sdk/issues/1226
            //PPApplication.recordException(e);
        }

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

        if (PPApplication.keyguardManager == null)
            PPApplication.keyguardManager = (KeyguardManager)appContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (PPApplication.keyguardManager != null)
            //noinspection deprecation
            PPApplication.keyguardLock = PPApplication.keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

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
    }

    static void cancelAllWorks(Context appContext) {
        cancelWork("elapsedAlarmsShowProfileNotificationWork", appContext);
        cancelWork("elapsedAlarmsUpdateGUIWork", appContext);
        for (String tag : PPApplication.elapsedAlarmsProfileDurationWork)
            cancelWork(tag, appContext);
        PPApplication.elapsedAlarmsProfileDurationWork.clear();
        for (String tag : PPApplication.elapsedAlarmsRunApplicationWithDelayWork)
            cancelWork(tag, appContext);
        PPApplication.elapsedAlarmsRunApplicationWithDelayWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayStartWork)
            cancelWork(tag, appContext);
        PPApplication.elapsedAlarmsEventDelayStartWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayEndWork)
            cancelWork(tag, appContext);
        PPApplication.elapsedAlarmsEventDelayEndWork.clear();
        for (String tag : PPApplication.elapsedAlarmsStartEventNotificationWork)
            cancelWork(tag, appContext);
        PPApplication.elapsedAlarmsStartEventNotificationWork.clear();
        cancelWork("disableInternalChangeWork", appContext);
        cancelWork("delayedWorkCloseAllApplications", appContext);
        cancelWork("handleEventsBluetoothLEScannerWork", appContext);
        cancelWork(BluetoothScanWorker.WORK_TAG, appContext);
        cancelWork("handleEventsBluetoothCLScannerWork", appContext);
        cancelWork("restartEventsWithDelayWork", appContext);
        cancelWork(GeofenceScanWorker.WORK_TAG, appContext);
        cancelWork("elapsedAlarmsGeofenceScannerSwitchGPSWork", appContext);
        cancelWork(LocationGeofenceEditorActivity.FETCH_ADDRESS_WORK_TAG, appContext);
        cancelWork("elapsedAlarmsLockDeviceFinishActivity", appContext);
        cancelWork("elapsedAlarmsLockDeviceAfterScreenOff", appContext);
        cancelWork("packageReplacedWork",appContext);
        cancelWork("delayedWorkAfterFirstStartWork", appContext);
        cancelWork("setBlockProfileEventsActionWork", appContext);
        cancelWork(SearchCalendarEventsWorker.WORK_TAG, appContext);
        cancelWork("handleEventsWifiScannerFromScannerWork", appContext);
        cancelWork("handleEventsWifiScannerFromReceiverWork", appContext);
        cancelWork(WifiScanWorker.WORK_TAG, appContext);
        cancelWork("startWifiScanWork", appContext);
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


        stopSimulatingRingingCall(/*true*/);
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        unregisterReceiversAndWorkers();

        try {
            appContext.unregisterReceiver(PPApplication.startLauncherFromNotificationReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        try {
            appContext.unregisterReceiver(PPApplication.showProfileNotificationBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
        try {
            appContext.unregisterReceiver(PPApplication.updateGUIBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
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

        PPApplication.initRoot();

        ShowProfileNotificationBroadcastReceiver.removeAlarm(appContext);
        try {
            //if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))

            stopForeground(true);

            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);

            /*else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }*/
        } catch (Exception e) {
            Log.e("PhoneProfilesService.onDestroy", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = null;
        }

        serviceHasFirstStart = false;
        //serviceRunning = false;
        //runningInForeground = false;
        PPApplication.applicationFullyStarted = false;

        // cancel works
        //cancelAllWorks(appContext);
    }

    static void cancelWork(String name, Context context) {
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);
            workManager.cancelAllWorkByTag(name);
        } catch (Exception e) {
            Log.e("------------ PhoneProfilesService.cancelWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);
            workManager.cancelUniqueWork(name);
        } catch (Exception e) {
            Log.e("------------ PhoneProfilesService.cancelWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static PhoneProfilesService getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
            return instance;
        //}
    }

    public static void stop(/*Context context*/) {
        if (instance != null) {
            /*try {
                //noinspection deprecation
                context.sendStickyBroadcast(new Intent(ACTION_STOP));
                //context.sendBroadcast(new Intent(ACTION_STOP));
            } catch (Exception ignored) {
            }*/
            try {
                /*instance.stopForeground(true);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);*/

                instance.stopSelf();
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stop", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

//    boolean getServiceRunning() {
//        return serviceRunning;
//    }

//    boolean getApplicationFullyStarted() {
//        return applicationFullyStarted;
//    }

    private void registerAllTheTimeRequiredReceivers(boolean register) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "xxx");
        if (!register) {
            if (PPApplication.timeChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER time changed", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER time changed");
                try {
                    appContext.unregisterReceiver(PPApplication.timeChangedReceiver);
                    PPApplication.timeChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.timeChangedReceiver = null;
                }
            }
            if (PPApplication.permissionsNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER permissions notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER permissions notification delete");
                try {
                    appContext.unregisterReceiver(PPApplication.permissionsNotificationDeletedReceiver);
                    PPApplication.permissionsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.permissionsNotificationDeletedReceiver = null;
                }
            }
            if (PPApplication.startEventNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER start event notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER start event notification delete");
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationDeletedReceiver);
                    PPApplication.startEventNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationDeletedReceiver = null;
                }
            }
            if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER not used mobile cells notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER not used mobile cells notification delete");
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver);
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDeletedReceiver = null;
                }
            }
            if (PPApplication.shutdownBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER shutdown");
                try {
                    appContext.unregisterReceiver(PPApplication.shutdownBroadcastReceiver);
                    PPApplication.shutdownBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.shutdownBroadcastReceiver = null;
                }
            }
            if (PPApplication.screenOnOffReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER screen on off");
                try {
                    appContext.unregisterReceiver(PPApplication.screenOnOffReceiver);
                    PPApplication.screenOnOffReceiver = null;
                } catch (Exception e) {
                    PPApplication.screenOnOffReceiver = null;
                }
            }
            if (PPApplication.interruptionFilterChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER interruption filter");
                try {
                    appContext.unregisterReceiver(PPApplication.interruptionFilterChangedReceiver);
                    PPApplication.interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    PPApplication.interruptionFilterChangedReceiver = null;
                }
            }
            if (PPApplication.phoneCallBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER phone call");
                try {
                    appContext.unregisterReceiver(PPApplication.phoneCallBroadcastReceiver);
                    PPApplication.phoneCallBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.phoneCallBroadcastReceiver = null;
                }
            }
            if (PPApplication.ringerModeChangeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER ringer mode change");
                try {
                    appContext.unregisterReceiver(PPApplication.ringerModeChangeReceiver);
                    PPApplication.ringerModeChangeReceiver = null;
                } catch (Exception e) {
                    PPApplication.ringerModeChangeReceiver = null;
                }
            }
            if (PPApplication.deviceIdleModeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER device idle mode");
                try {
                    appContext.unregisterReceiver(PPApplication.deviceIdleModeReceiver);
                    PPApplication.deviceIdleModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.deviceIdleModeReceiver = null;
                }
            }
            if (PPApplication.bluetoothConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER bluetooth connection");
                try {
                    appContext.unregisterReceiver(PPApplication.bluetoothConnectionBroadcastReceiver);
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.bluetoothConnectionBroadcastReceiver = null;
                }
            }
            if (PPApplication.eventDelayStartBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayStartBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayStartBroadcastReceiver);
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayStartBroadcastReceiver = null;
                }
            }
            if (PPApplication.eventDelayEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.eventDelayEndBroadcastReceiver);
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.eventDelayEndBroadcastReceiver = null;
                }
            }
            if (PPApplication.profileDurationAlarmBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER profileDurationAlarmBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.profileDurationAlarmBroadcastReceiver);
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.profileDurationAlarmBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered profileDurationAlarmBroadcastReceiver");
            if (PPApplication.runApplicationWithDelayBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER runApplicationWithDelayBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver);
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.runApplicationWithDelayBroadcastReceiver = null;
                }
            }
            if (PPApplication.startEventNotificationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER startEventNotificationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.startEventNotificationBroadcastReceiver);
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.startEventNotificationBroadcastReceiver = null;
                }
            }
            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER lockDeviceActivityFinishBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver);
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceActivityFinishBroadcastReceiver = null;
                }
            }
            if (PPApplication.pppExtenderBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER pppExtenderBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.pppExtenderBroadcastReceiver);
                    PPApplication.pppExtenderBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.pppExtenderBroadcastReceiver = null;
                }
            }
            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER notUsedMobileCellsNotificationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER notUsedMobileCellsNotificationDisableReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver);
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                } catch (Exception e) {
                    PPApplication.notUsedMobileCellsNotificationDisableReceiver = null;
                }
            }
            if (PPApplication.donationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER donationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER donationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.donationBroadcastReceiver);
                    PPApplication.donationBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.donationBroadcastReceiver = null;
                }
            }
            if (PPApplication.ignoreBatteryOptimizationDisableReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER ignoreBatteryOptimizationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER ignoreBatteryOptimizationDisableReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.ignoreBatteryOptimizationDisableReceiver);
                    PPApplication.ignoreBatteryOptimizationDisableReceiver = null;
                } catch (Exception e) {
                    PPApplication.ignoreBatteryOptimizationDisableReceiver = null;
                }
            }
            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER lockDeviceAfterScreenOffBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER lockDeviceAfterScreenOffBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver);
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = null;
                }
            }
            if (PPApplication.wifiStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER wifiStateChangedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER wifiStateChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.wifiStateChangedBroadcastReceiver);
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    PPApplication.wifiStateChangedBroadcastReceiver = null;
                }
            }
            if (PPApplication.powerSaveModeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER powerSaveModeReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.powerSaveModeReceiver);
                    PPApplication.powerSaveModeReceiver = null;
                } catch (Exception e) {
                    PPApplication.powerSaveModeReceiver = null;
                }
            }
        }
        if (register) {
            if (PPApplication.timeChangedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER time changed", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER time changed");
                //PPApplication.lastUptimeTime = SystemClock.elapsedRealtime();
                //PPApplication.lastEpochTime = System.currentTimeMillis();
                PPApplication.timeChangedReceiver = new TimeChangedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                //intentFilter5.addAction(Intent.ACTION_TIME_TICK);
                //intentFilter5.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter5.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                appContext.registerReceiver(PPApplication.timeChangedReceiver, intentFilter5);
            }

            if (PPApplication.permissionsNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER permissions notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER permissions notification delete");
                PPApplication.permissionsNotificationDeletedReceiver = new PermissionsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(GrantPermissionActivity.NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(PPApplication.permissionsNotificationDeletedReceiver, intentFilter5);
            }

            if (PPApplication.startEventNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER start event notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER start event notification delete");
                PPApplication.startEventNotificationDeletedReceiver = new StartEventNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(StartEventNotificationDeletedReceiver.START_EVENT_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(PPApplication.startEventNotificationDeletedReceiver, intentFilter5);
            }

            if (PPApplication.notUsedMobileCellsNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER not used mobile cells notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER not used mobile cells notification delete");
                PPApplication.notUsedMobileCellsNotificationDeletedReceiver = new NotUsedMobileCellsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDeletedReceiver, intentFilter5);
            }

            if (PPApplication.shutdownBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER shutdown");
                PPApplication.shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SHUTDOWN);
                intentFilter5.addAction("android.intent.action.QUICKBOOT_POWEROFF");
                appContext.registerReceiver(PPApplication.shutdownBroadcastReceiver, intentFilter5);
            }

            // required for Lock device, Hide notification in lock screen, screen timeout +
            // screen on/off event + rescan wifi, bluetooth, location, mobile cells
            if (PPApplication.screenOnOffReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER screen on off");
                PPApplication.screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
                appContext.registerReceiver(PPApplication.screenOnOffReceiver, intentFilter5);
            }

            // required for Do not disturb ringer mode
            if (PPApplication.interruptionFilterChangedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER interruption filter");
                //if (android.os.Build.VERSION.SDK_INT >= 23) {
                    boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                    if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                        PPApplication.interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                        IntentFilter intentFilter11 = new IntentFilter();
                        intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                        appContext.registerReceiver(PPApplication.interruptionFilterChangedReceiver, intentFilter11);
                    }
                //}
            }

            // required for unlink ring and notification volume
            if (PPApplication.phoneCallBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER phone call");
                PPApplication.phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
                IntentFilter intentFilter6 = new IntentFilter();
                intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                appContext.registerReceiver(PPApplication.phoneCallBroadcastReceiver, intentFilter6);
            }

            // required for unlink ring and notification volume
            if (PPApplication.ringerModeChangeReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER ringer mode change");
                PPApplication.ringerModeChangeReceiver = new RingerModeChangeReceiver();
                IntentFilter intentFilter7 = new IntentFilter();
                intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.ringerModeChangeReceiver, intentFilter7);
            }

            // required for start EventsHandler in idle maintenance window
            if (PPApplication.deviceIdleModeReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER device idle mode");
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
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER bluetooth connection");

                PPApplication.bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                //intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                appContext.registerReceiver(PPApplication.bluetoothConnectionBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.eventDelayStartBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayStartBroadcastReceiver");

                PPApplication.eventDelayStartBroadcastReceiver = new EventDelayStartBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.eventDelayStartBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.eventDelayEndBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayEndBroadcastReceiver");

                PPApplication.eventDelayEndBroadcastReceiver = new EventDelayEndBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.eventDelayEndBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.profileDurationAlarmBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER profileDurationAlarmBroadcastReceiver");

                PPApplication.profileDurationAlarmBroadcastReceiver = new ProfileDurationAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.profileDurationAlarmBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.runApplicationWithDelayBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER runApplicationWithDelayBroadcastReceiver");

                PPApplication.runApplicationWithDelayBroadcastReceiver = new RunApplicationWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.runApplicationWithDelayBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.startEventNotificationBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER startEventNotificationBroadcastReceiver");

                PPApplication.startEventNotificationBroadcastReceiver = new StartEventNotificationBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.startEventNotificationBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.lockDeviceActivityFinishBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER lockDeviceActivityFinishBroadcastReceiver");

                PPApplication.lockDeviceActivityFinishBroadcastReceiver = new LockDeviceActivityFinishBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                appContext.registerReceiver(PPApplication.lockDeviceActivityFinishBroadcastReceiver, intentFilter14);
            }

            if (PPApplication.pppExtenderBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_pppExtenderBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER pppExtenderBroadcastReceiver");

                PPApplication.pppExtenderBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                appContext.registerReceiver(PPApplication.pppExtenderBroadcastReceiver, intentFilter14,
                        PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
            }

            if (PPApplication.notUsedMobileCellsNotificationDisableReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER notUsedMobileCellsNotificationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER notUsedMobileCellsNotificationDisableReceiver");
                PPApplication.notUsedMobileCellsNotificationDisableReceiver = new NotUsedMobileCellsNotificationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                appContext.registerReceiver(PPApplication.notUsedMobileCellsNotificationDisableReceiver, intentFilter5);
            }

            if (PPApplication.donationBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER donationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER donationBroadcastReceiver");
                PPApplication.donationBroadcastReceiver = new DonationBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_DONATION);
                appContext.registerReceiver(PPApplication.donationBroadcastReceiver, intentFilter5);
            }

            if (PPApplication.ignoreBatteryOptimizationDisableReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER ignoreBatteryOptimizationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER ignoreBatteryOptimizationDisableReceiver");
                PPApplication.ignoreBatteryOptimizationDisableReceiver = new IgnoreBatteryOptimizationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(IgnoreBatteryOptimizationNotification.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION);
                appContext.registerReceiver(PPApplication.ignoreBatteryOptimizationDisableReceiver, intentFilter5);
            }

            if (PPApplication.lockDeviceAfterScreenOffBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER lockDeviceAfterScreenOffBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER lockDeviceAfterScreenOffBroadcastReceiver");
                PPApplication.lockDeviceAfterScreenOffBroadcastReceiver = new LockDeviceAfterScreenOffBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
                appContext.registerReceiver(PPApplication.lockDeviceAfterScreenOffBroadcastReceiver, intentFilter14);
            }
            if (PPApplication.wifiStateChangedBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER wifiStateChangedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER wifiStateChangedBroadcastReceiver");
                PPApplication.wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
                IntentFilter intentFilter8 = new IntentFilter();
                intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                appContext.registerReceiver(PPApplication.wifiStateChangedBroadcastReceiver, intentFilter8);
            }

            if (PPApplication.powerSaveModeReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER powerSaveModeReceiver");
                PPApplication.powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                IntentFilter intentFilter10 = new IntentFilter();
                intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                appContext.registerReceiver(PPApplication.powerSaveModeReceiver, intentFilter10);
                //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "REGISTER powerSaveModeReceiver");
            }

        }
    }

    private void registerContentObservers(boolean register) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerContentObservers", "PhoneProfilesService_registerContentObservers");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerContentObservers", "xxx");
        if (!register) {
            if (PPApplication.settingsContentObserver != null) {
                try {
                    appContext.getContentResolver().unregisterContentObserver(PPApplication.settingsContentObserver);
                    PPApplication.settingsContentObserver = null;
                } catch (Exception e) {
                    PPApplication.settingsContentObserver = null;
                }
            }
            if (PPApplication.contactsContentObserver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER contacts content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER contacts content observer");
                try {
                    appContext.getContentResolver().unregisterContentObserver(PPApplication.contactsContentObserver);
                    PPApplication.contactsContentObserver = null;
                } catch (Exception e) {
                    PPApplication.contactsContentObserver = null;
                }
            }
        }
        if (register) {
            if (PPApplication.settingsContentObserver == null) {
                try {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER settings content observer");
                    //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
                    PPApplication.settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
                    appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, PPApplication.settingsContentObserver);
                } catch (Exception e) {
                    PPApplication.settingsContentObserver = null;
                    PPApplication.recordException(e);
                }
            }
            if (PPApplication.contactsContentObserver == null) {
                try {
                    if (Permissions.checkContacts(appContext)) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER contacts content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER contacts content observer");
                        PPApplication.contactsContentObserver = new ContactsContentObserver(appContext, new Handler());
                        appContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, PPApplication.contactsContentObserver);
                    }
                } catch (Exception e) {
                    PPApplication.contactsContentObserver = null;
                    PPApplication.recordException(e);
                }
            }
        }
    }

    private void registerCallbacks(boolean register) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerCallbacks", "PhoneProfilesService_registerCallbacks");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerCallbacks", "xxx");
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

                        PPApplication.wifiConnectionCallback = new PPWifiNetworkCallback(appContext);
                        connectivityManager.registerNetworkCallback(networkRequest, PPApplication.wifiConnectionCallback);
                    }
                } catch (Exception e) {
                    PPApplication.wifiConnectionCallback = null;
                    PPApplication.recordException(e);
                }
            }
        }
    }

    private void registerBatteryLevelChangedReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryLevelChangedReceiver", "PhoneProfilesService_registerBatteryLevelChangedReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "xxx");
        if (!register) {
            if (PPApplication.batteryLevelChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryLevelChangedReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryLevelChangedReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.batteryLevelChangedReceiver);
                    PPApplication.batteryLevelChangedReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryLevelChangedReceiver", "UNREGISTER");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryLevelChangedReceiver->REGISTER", "PhoneProfilesService_registerBatteryLevelChangedReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryChargingChangedReceiver", "PhoneProfilesService_registerBatteryChargingChangedReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChargingChangedReceiver", "xxx");
        if (!register) {
            if (PPApplication.batteryChargingChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChargingChangedReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryChargingChangedReceiver");
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
                }
            }
            if (allowed) {
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                if (PPApplication.batteryChargingChangedReceiver == null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChargingChangedReceiver->REGISTER", "PhoneProfilesService_registerBatteryChargingChangedReceiver");
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

    private void registerReceiverForPeripheralsSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "xxx");
        if (!register) {
            if (PPApplication.headsetPlugReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                try {
                    appContext.unregisterReceiver(PPApplication.headsetPlugReceiver);
                    PPApplication.headsetPlugReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER headset plug");
                } catch (Exception e) {
                    PPApplication.headsetPlugReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered headset plug");
            if (PPApplication.dockConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                try {
                    appContext.unregisterReceiver(PPApplication.dockConnectionBroadcastReceiver);
                    PPApplication.dockConnectionBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER dock connection");
                } catch (Exception e) {
                    PPApplication.dockConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered dock connection");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER");
            dataWrapper.fillEventList();
            boolean allowed = false;
            boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_PERIPHERAL/*, false*/);
            if (eventsExists)
                allowed = Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (PPApplication.headsetPlugReceiver == null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                    PPApplication.headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
                    IntentFilter intentFilter2 = new IntentFilter();
                    intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                    intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                    appContext.registerReceiver(PPApplication.headsetPlugReceiver, intentFilter2);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER headset plug");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered headset plug");
                if (PPApplication.dockConnectionBroadcastReceiver == null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                    PPApplication.dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
                    IntentFilter intentFilter12 = new IntentFilter();
                    intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
                    intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
                    appContext.registerReceiver(PPApplication.dockConnectionBroadcastReceiver, intentFilter12);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER dock connection");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered dock connection");
            }
            else
                registerReceiverForPeripheralsSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForSMSSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForSMSSensor", "PhoneProfilesService_registerReceiverForSMSSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "xxx");
        if (!register) {
            /*if (smsBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->UNREGISTER SMS", "PhoneProfilesService_registerReceiverForSMSSensor");
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
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->UNREGISTER MMS", "PhoneProfilesService_registerReceiverForSMSSensor");
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
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->UNREGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
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
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER SMS", "PhoneProfilesService_registerReceiverForSMSSensor");
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
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER MMS", "PhoneProfilesService_registerReceiverForSMSSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor", "PhoneProfilesService_registerReceiverForCalendarSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "xxx");
        if (!register) {
            if (PPApplication.calendarProviderChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                try {
                    appContext.unregisterReceiver(PPApplication.calendarProviderChangedBroadcastReceiver);
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER calendarProviderChangedBroadcastReceiver");
                } catch (Exception e) {
                    PPApplication.calendarProviderChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
            if (PPApplication.eventCalendarBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
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
                if (PPApplication.calendarProviderChangedBroadcastReceiver == null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
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
                if (PPApplication.eventCalendarBroadcastReceiver == null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                    PPApplication.eventCalendarBroadcastReceiver = new EventCalendarBroadcastReceiver();
                    IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                    appContext.registerReceiver(PPApplication.eventCalendarBroadcastReceiver, intentFilter23);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER eventCalendarBroadcastReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "xxx");
        if (!register) {
            if (PPApplication.airplaneModeStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                if (PPApplication.nfcStateChangedBroadcastReceiver != null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                    if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
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

    private void registerReceiverForRadioSwitchMobileDataSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");
        if (!register) {
            if (PPApplication.mobileDataStateChangedContentObserver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                    PPApplication.mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler());
                    appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, PPApplication.mobileDataStateChangedContentObserver);
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER mobileDataStateChangedContentObserver");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "registered");
            }
            else
                registerReceiverForRadioSwitchMobileDataSensor(false, dataWrapper);
        }
    }

    private void registerReceiverForAlarmClockSensor(boolean register, DataWrapper dataWrapper) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "xxx");
        if (!register) {
            if (PPApplication.alarmClockBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
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
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor", "PhoneProfilesService_registerReceiverForNotificationSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "xxx");
        if (!register) {
            if (PPApplication.notificationEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor->UNREGISTER notificationEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForNotificationSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor->REGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForNotificationSensor");
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

    private void registerReceiverForOrientationSensor(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "xxx");
        if (!register) {
            if (PPApplication.orientationEventBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor->UNREGISTER registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
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
                    boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
                    if (eventsExists)
                        eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                if (eventAllowed) {
                    if (PPApplication.orientationEventBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor->REGISTER registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
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

    private void registerReceiverForDeviceBootSensor(boolean register, DataWrapper dataWrapper) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForDeviceBootSensor", "PhoneProfilesService_registerReceiverForDeviceBootSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForDeviceBootSensor", "xxx");
        if (!register) {
            if (PPApplication.deviceBootEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForDeviceBootSensor->UNREGISTER registerReceiverForDeviceBootSensor", "PhoneProfilesService_registerReceiverForDeviceBootSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForDeviceBootSensor->REGISTER registerReceiverForDeviceBootSensor", "PhoneProfilesService_registerReceiverForDeviceBootSensor");
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

    private void unregisterPPPPExtenderReceiver(int type) {
        //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER");
        Context appContext = getApplicationContext();
        if (type == PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER) {
            if (PPApplication.pppExtenderForceStopApplicationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
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
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER) {
            if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
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
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER) {
            if (PPApplication.pppExtenderSMSBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
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
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER) {
            if (PPApplication.pppExtenderCallBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
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
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER) {
            // send broadcast to Extender for lock device

            //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER lock device");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
    }

    void registerPPPPExtenderReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver", "PhoneProfilesService_registerPPPPExtenderReceiver");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
                        PPApplication.pppExtenderForceStopApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
                        appContext.registerReceiver(PPApplication.pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER force stop applications");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForceStopApplicationBroadcastReceiver");

                    // send broadcast to Extender for register force stop applications
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);

                if (lockDeviceAllowed) {
                    // send broadcast to Extender for register lock device

                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER lock device");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);

                if ((applicationsAllowed) || (orientationAllowed)) {
                    if (PPApplication.pppExtenderForegroundApplicationBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
                        PPApplication.pppExtenderForegroundApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                        intentFilter23.addAction(PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED);
                        appContext.registerReceiver(PPApplication.pppExtenderForegroundApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "applications and orientation");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForegroundApplicationBroadcastReceiver");

                    // send broadcast to Extender for register foreground application
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);

                if (smsAllowed) {
                    if (PPApplication.pppExtenderSMSBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
                        PPApplication.pppExtenderSMSBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        appContext.registerReceiver(PPApplication.pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER sms");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderSMSBroadcastReceiver");

                    // send broadcast to Extender for register sms
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_SMS_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);

                if (callAllowed) {
                    if (PPApplication.pppExtenderCallBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
                        PPApplication.pppExtenderCallBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        appContext.registerReceiver(PPApplication.pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER call");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderCallBroadcastReceiver");

                    // send broadcast to Extender for register call
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER PPApplication.REGISTRATION_TYPE_CALL_REGISTER");
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "xxx");
        if (!register) {
            if (PPApplication.locationModeChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (!register) {
            if (bluetoothConnectionBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver->REGISTER", "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
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
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothConnectionBroadcastReceiver.onReceive");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers", "PhoneProfilesService_registerBluetoothScannerReceivers");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.bluetoothScanReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
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
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
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
                    if (PPApplication.bluetoothScanReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
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
                    if (PPApplication.bluetoothLEScanReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                        PPApplication.bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();
                        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.bluetoothLEScanReceiver,
                                new IntentFilter(PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver"));
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiAPStateChangeBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver", "PhoneProfilesService_registerPowerSaveModeReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "xxx");
        if (!register) {
            if (PPApplication.powerSaveModeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->UNREGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->REGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (wifiStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiScannerReceiver", "PhoneProfilesService_registerWifiScannerReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (PPApplication.wifiScanReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->UNREGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->REGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForTimeSensor", "PhoneProfilesService_registerReceiverForTimeSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "xxx");
        if (!register) {
            if (PPApplication.eventTimeBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->REGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForNFCSensor", "PhoneProfilesService_registerReceiverForNFCSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.nfcEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
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
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCallSensor", "PhoneProfilesService_registerReceiverForCallSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "xxx");
        if (!register) {
            if (PPApplication.missedCallEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
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
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->REGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
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

    private void registerGeofencesScannerReceiver(boolean register, DataWrapper dataWrapper) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver", "PhoneProfilesService_registerGeofencesScannerReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "xxx");
        if (!register) {
            if (PPApplication.geofencesScannerSwitchGPSBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->UNREGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                try {
                    appContext.unregisterReceiver(PPApplication.geofencesScannerSwitchGPSBroadcastReceiver);
                    PPApplication.geofencesScannerSwitchGPSBroadcastReceiver = null;
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "UNREGISTER");
                } catch (Exception e) {
                    PPApplication.geofencesScannerSwitchGPSBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "not registered");
        }
        if (register) {
            //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "REGISTER");
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
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "eventCount="+eventCount);
                    if (PPApplication.geofencesScannerSwitchGPSBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->REGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                        PPApplication.geofencesScannerSwitchGPSBroadcastReceiver = new GeofencesScannerSwitchGPSBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                        appContext.registerReceiver(PPApplication.geofencesScannerSwitchGPSBroadcastReceiver, intentFilter4);
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "REGISTER geofencesScannerSwitchGPSBroadcastReceiver");
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "registered");
                } else
                    registerGeofencesScannerReceiver(false, dataWrapper);
            } else
                registerGeofencesScannerReceiver(false, dataWrapper);
        }
    }

    private void cancelWifiWorker(final Context context, boolean forSchedule) {
        if ((!forSchedule) || WifiScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelWifiWorker->CANCEL", "PhoneProfilesService_cancelWifiWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelWifiWorker", "CANCEL");
            WifiScanWorker.cancelWork(context, true/*, null*/);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelWifiWorker", "not scheduled");
    }

    void scheduleWifiWorker(/*final boolean schedule,*/ /*final boolean cancel,*/ final DataWrapper dataWrapper,
                            //final boolean forScreenOn, final boolean afterEnableWifi,
                         /*final boolean forceStart,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleWifiWorker", "PhoneProfilesService_scheduleWifiWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "xxx");

        if (/*!forceStart &&*/ WifiSSIDPreferenceX.forceRegister)
            return;

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE");
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
                if (!WifiScanWorker.isWorkScheduled(appContext)) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleWifiWorker->SCHEDULE", "PhoneProfilesService_scheduleWifiWorker");
                    WifiScanWorker.scheduleWork(appContext, true, /*null,*/ true/*, forScreenOn, afterEnableWifi*/);
                    //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE 1");
                } else {
                    if (rescan) {
                        WifiScanWorker.cancelWork(appContext, true/*, null*/);
                        WifiScanWorker.scheduleWork(appContext, true, /*null,*/ true/*, forScreenOn, afterEnableWifi*/);
                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE 2");
                    }
                }
            } else
                cancelWifiWorker(appContext, true);
        } else
            cancelWifiWorker(appContext, true);
        //}
        //else
        //    cancelWifiWorker(appContext, handler);
    }

    private void cancelBluetoothWorker(final Context context, boolean forSchedule) {
        if ((!forSchedule) || BluetoothScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelBluetoothWorker->CANCEL", "PhoneProfilesService_cancelBluetoothWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "CANCEL");
            BluetoothScanWorker.cancelWork(context, true/*, null*/);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "not scheduled");
    }

    private void scheduleBluetoothWorker(/*final boolean schedule,*/ /*final boolean cancel,*/ final DataWrapper dataWrapper
                              /*final boolean forScreenOn, final boolean forceStart,*/ /*final boolean rescan*/) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleBluetoothWorker", "PhoneProfilesService_scheduleBluetoothWorker");
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
                if (!BluetoothScanWorker.isWorkScheduled(appContext)) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleBluetoothWorker->SCHEDULE", "PhoneProfilesService_scheduleBluetoothWorker");
                    BluetoothScanWorker.scheduleWork(appContext, true, /*null,*/ true/*, forScreenOn*/);
                    //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "SCHEDULE 1");
                } else {
                    //if (rescan) {
                    BluetoothScanWorker.cancelWork(appContext, true/*, null*/);
                    BluetoothScanWorker.scheduleWork(appContext, true, /*null,*/ true/*, forScreenOn*/);
                    //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "SCHEDULE 2");
                    //}
                }
            } else
                cancelBluetoothWorker(appContext, true);
        } else
            cancelBluetoothWorker(appContext, true);
        //}
        //else
        //    cancelBluetoothWorker(appContext, handler);
    }

    private void cancelGeofenceWorker(final Context context, boolean forSchedule) {
        if ((!forSchedule) || GeofenceScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelGeofenceWorker->CANCEL", "PhoneProfilesService_cancelGeofenceWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "CANCEL");
            GeofenceScanWorker.cancelWork(context, true/*, null*/);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "not scheduled");
    }

    private void scheduleGeofenceWorker(/*final boolean schedule,*/ /*final boolean cancel,*/ final DataWrapper dataWrapper
                                    /*final boolean forScreenOn,*/ /*final boolean rescan*/) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleGeofenceWorker", "PhoneProfilesService_scheduleGeofenceWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "xxx");

        //if (schedule) {
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "SCHEDULE");
        if (ApplicationPreferences.applicationEventLocationEnableScanning) {
            boolean eventAllowed = false;
            if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)) {
                // start only for screen On
                dataWrapper.fillEventList();
                boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_LOCATION/*, false*/);
                if (eventsExists)
                    eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (eventAllowed) {
                // location scanner is enabled
                if (!GeofenceScanWorker.isWorkScheduled(appContext)/* || rescan*/) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleGeofenceWorker->SCHEDULE", "PhoneProfilesService_scheduleGeofenceWorker");
                    synchronized (PPApplication.geofenceScannerMutex) {
                        if (isGeofenceScannerStarted()) {
                            //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "updateTransitionsByLastKnownLocation");
                            getGeofencesScanner().updateTransitionsByLastKnownLocation(false);
                        }
                    }
                    GeofenceScanWorker.scheduleWork(appContext, false, /*null,*/ true/*, forScreenOn*/);
                    //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "SCHEDULE 1");
                } else {
                    //if (rescan)
                    GeofenceScanWorker.cancelWork(appContext, true/*, null*/);
                    synchronized (PPApplication.geofenceScannerMutex) {
                        if (isGeofenceScannerStarted()) {
                            //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "updateTransitionsByLastKnownLocation");
                            getGeofencesScanner().updateTransitionsByLastKnownLocation(false);
                        }
                    }
                    GeofenceScanWorker.scheduleWork(appContext, false, /*null,*/ true/*, forScreenOn*/);
                    //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "SCHEDULE 2");
                }
            } else
                cancelGeofenceWorker(appContext, true);
        } else
            cancelGeofenceWorker(appContext, true);
        //}
        //else
        //    cancelGeofenceWorker(appContext, handler);
    }

    private void cancelSearchCalendarEventsWorker(final Context context, boolean forSchedule) {
        if ((!forSchedule) || SearchCalendarEventsWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelSearchCalendarEventsWorker->CANCEL", "PhoneProfilesService_cancelSearchCalendarEventsWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "CANCEL");
            SearchCalendarEventsWorker.cancelWork(context, true/*, null*/);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "not scheduled");
    }

    private void scheduleSearchCalendarEventsWorker(/*final boolean schedule,*/ final DataWrapper dataWrapper/*, final boolean rescan*/) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsWorker", "PhoneProfilesService_scheduleSearchCalendarEventsWorker");
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
            if (!SearchCalendarEventsWorker.isWorkScheduled(appContext)/* || rescan*/) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsWorker->SCHEDULE", "PhoneProfilesService_scheduleSearchCalendarEventsWorker");
                //if (rescan)
                SearchCalendarEventsWorker.cancelWork(appContext, true/*, null*/);
                SearchCalendarEventsWorker.scheduleWork(appContext, true, /*null,*/ true);
                //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "SCHEDULE xxx");
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "scheduled");
        } else
            cancelSearchCalendarEventsWorker(appContext, true);
        //}
        //else
        //    cancelSearchCalendarEventsWorker(appContext, handler);
    }

    private void startGeofenceScanner(boolean start, @SuppressWarnings("SameParameterValue") boolean stop, DataWrapper dataWrapper, boolean forScreenOn) {
        synchronized (PPApplication.geofenceScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService_startGeofenceScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "xxx");
            if (stop) {
                if (isGeofenceScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->STOP", "PhoneProfilesService_startGeofenceScanner");
                    stopGeofenceScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "START");
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
                        if (!isGeofenceScannerStarted()) {
                            //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->START", "PhoneProfilesService_startGeofenceScanner");
                            if (forScreenOn && PPApplication.isScreenOn &&
                                    applicationEventLocationScanOnlyWhenScreenIsOn)
                                startGeofenceScanner(true);
                            else
                                startGeofenceScanner(false);
                            //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "START");
                        }
                    } else
                        startGeofenceScanner(false, true, dataWrapper, forScreenOn);
                } else
                    startGeofenceScanner(false, true, dataWrapper, forScreenOn);
            }
        }
    }

    private void startPhoneStateScanner(boolean start, boolean stop, DataWrapper dataWrapper, boolean forceStart,
                                        boolean rescan) {
        synchronized (PPApplication.phoneStateScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startPhoneStateScanner", "PhoneProfilesService_startPhoneStateScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "xxx");
            if (!forceStart && (MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart))
                return;
            if (stop) {
                if (isPhoneStateScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->STOP", "PhoneProfilesService_startPhoneStateScanner");
                    stopPhoneStateScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "START");
                //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || PhoneStateScanner.forceStart) {
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
                    //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "eventAllowed="+eventAllowed);
                    if (eventAllowed) {
                        /*if (PPApplication.logEnabled()) {
                            //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "scanning enabled=" + applicationEventMobileCellEnableScanning);
                            PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "PhoneStateScanner.forceStart=" + PhoneStateScanner.forceStart);
                        }*/
                        if (!isPhoneStateScannerStarted()) {
                            //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->START", "PhoneProfilesService_startPhoneStateScanner");
                            startPhoneStateScanner();
                            //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "START 1");
                        } else {
                            if (rescan) {
                                PPApplication.phoneStateScanner.rescanMobileCells();
                                //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "RESCAN");
                            }
                        }
                    } else
                        startPhoneStateScanner(false, true, dataWrapper, forceStart, rescan);
                } else
                    startPhoneStateScanner(false, true, dataWrapper, forceStart, rescan);
            }
        }
    }

    private void startOrientationScanner(boolean start, boolean stop, DataWrapper dataWrapper) {
        synchronized (PPApplication.orientationScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startOrientationScanner", "PhoneProfilesService_startOrientationScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "xxx");
            if (stop) {
                if (isOrientationScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->STOP", "PhoneProfilesService_startOrientationScanner");
                    stopOrientationScanner();
                    //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "STOP");
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "not started");
            }
            if (start) {
                //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "START");
                if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    boolean eventAllowed = false;
                    if ((PPApplication.isScreenOn) || (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)) {
                        // start only for screen On
                        dataWrapper.fillEventList();
                        boolean eventsExists = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_ORIENTATION/*, false*/);
                        if (eventsExists)
                            eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                    PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    if (eventAllowed) {
                        if (!isOrientationScannerStarted()) {
                            //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->START", "PhoneProfilesService_startOrientationScanner");
                            startOrientationScanner();
                            //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "START");
                        }
                        //else
                        //    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "started");
                    } else
                        startOrientationScanner(false, true, dataWrapper);
                } else
                    startOrientationScanner(false, true, dataWrapper);
            }
        }
    }

    private void startTwilightScanner(boolean start, boolean stop, DataWrapper dataWrapper) {
        synchronized (PPApplication.twilightScannerMutex) {
            //Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startTwilightScanner", "PhoneProfilesService_startTwilightScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "xxx");
            if (stop) {
                if (isTwilightScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startTwilightScanner->STOP", "PhoneProfilesService_startTwilightScanner");
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
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startTwilightScanner->START", "PhoneProfilesService_startTwilightScanner");
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

    private void registerReceiversAndWorkers(boolean fromCommand) {
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiversAndWorkers", "xxx");

        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = getApplicationContext();

        // get actual battery status
        BatteryLevelChangedBroadcastReceiver.initialize(appContext);

        registerAllTheTimeRequiredReceivers(true);
        registerContentObservers(true);
        registerCallbacks(true);

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        // required for battery event
        registerBatteryLevelChangedReceiver(true, dataWrapper);
        registerBatteryChargingChangedReceiver(true, dataWrapper);

        // required for peripherals event
        registerReceiverForPeripheralsSensor(true, dataWrapper);

        // required for sms event
        registerReceiverForSMSSensor(true, dataWrapper);

        // required for calendar event
        registerReceiverForCalendarSensor(true, dataWrapper);

        // required for radio switch event
        registerReceiverForRadioSwitchMobileDataSensor(true, dataWrapper);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper);

        // required for alarm clock event
        registerReceiverForAlarmClockSensor(true, dataWrapper);

        // required for device boot event
        registerReceiverForDeviceBootSensor(true, dataWrapper);

        // required for force stop applications, applications event and orientation event
        registerPPPPExtenderReceiver(true, dataWrapper);

        // required for location and radio switch event
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

        // required for notification event
        registerReceiverForNotificationSensor(true, dataWrapper);

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

        // register receiver for geofences scanner
        registerGeofencesScannerReceiver(true, dataWrapper);

        // required for orientation event
        registerReceiverForOrientationSensor(true, dataWrapper);

        //Log.e("------ PhoneProfilesService.registerReceiversAndWorkers", "fromCommand="+fromCommand);
        WifiScanWorker.initialize(appContext, !fromCommand);
        BluetoothScanWorker.initialize(appContext, !fromCommand);

        startGeofenceScanner(true, true, dataWrapper, false);
        startPhoneStateScanner(true, true, dataWrapper, false, false);
        startOrientationScanner(true, true, dataWrapper);
        startTwilightScanner(true, true, dataWrapper);

        scheduleWifiWorker(/*true,*/  dataWrapper, /*false, false, false,*/ true);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true, */dataWrapper/*, true*/);
        scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);
    }

    private void unregisterReceiversAndWorkers() {
        //PPApplication.logE("[RJS] PhoneProfilesService.unregisterReceiversAndWorkers", "xxx");
        registerAllTheTimeRequiredReceivers(false);
        registerContentObservers(false);
        registerCallbacks(false);
        registerBatteryLevelChangedReceiver(false, null);
        registerBatteryChargingChangedReceiver(false, null);
        registerReceiverForPeripheralsSensor(false, null);
        registerReceiverForSMSSensor(false, null);
        registerReceiverForCalendarSensor(false, null);
        registerReceiverForRadioSwitchMobileDataSensor(false, null);
        registerReceiverForRadioSwitchNFCSensor(false, null);
        registerReceiverForRadioSwitchAirplaneModeSensor(false, null);
        registerReceiverForAlarmClockSensor(false, null);
        registerReceiverForDeviceBootSensor(false, null);
        registerPPPPExtenderReceiver(false, null);
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
        registerGeofencesScannerReceiver(false, null);
        registerReceiverForNotificationSensor(false, null);
        registerReceiverForOrientationSensor(false, null);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        final Context appContext = getApplicationContext();

        startGeofenceScanner(false, true, null, false);
        startPhoneStateScanner(false, true, null, false, false);
        startOrientationScanner(false, true, null);
        startTwilightScanner(false, true, null);

        cancelWifiWorker(appContext, false);
        cancelBluetoothWorker(appContext, false);
        cancelGeofenceWorker(appContext, false);
        cancelSearchCalendarEventsWorker(appContext, false);
    }

    private void reregisterReceiversAndWorkers() {
        //PPApplication.logE("[RJS] PhoneProfilesService.reregisterReceiversAndWorkers", "xxx");

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
        dataWrapper.fillEventList();
        //dataWrapper.fillProfileList(false, false);

        registerAllTheTimeRequiredReceivers(true);
        registerContentObservers(true);
        registerCallbacks(true);

        registerBatteryLevelChangedReceiver(true, dataWrapper);
        registerBatteryChargingChangedReceiver(true, dataWrapper);
        registerReceiverForPeripheralsSensor(true, dataWrapper);
        registerReceiverForSMSSensor(true, dataWrapper);
        registerReceiverForCalendarSensor(true, dataWrapper);
        registerReceiverForRadioSwitchMobileDataSensor(true, dataWrapper);
        registerReceiverForRadioSwitchNFCSensor(true, dataWrapper);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, dataWrapper);
        registerReceiverForAlarmClockSensor(true, dataWrapper);
        registerReceiverForDeviceBootSensor(true, dataWrapper);
        registerPPPPExtenderReceiver(true, dataWrapper);
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
        registerGeofencesScannerReceiver(true, dataWrapper);
        registerReceiverForOrientationSensor(true, dataWrapper);
        registerReceiverForNotificationSensor(true,dataWrapper);

        scheduleWifiWorker(/*true,*/  dataWrapper, /*false, false, false,*/ true);
        scheduleBluetoothWorker(/*true,*/  dataWrapper /*false, false,*/ /*, true*/);
        scheduleSearchCalendarEventsWorker(/*true,*/ dataWrapper /*, true*/);

        startGeofenceScanner(true, true, dataWrapper, false);
        scheduleGeofenceWorker(/*true,*/  dataWrapper /*false,*/ /*, true*/);

        startPhoneStateScanner(true, true, dataWrapper, false, false);
        startOrientationScanner(true, true, dataWrapper);
        startTwilightScanner(true, true, dataWrapper);
    }

    // start service for first start
    private void doForFirstStart(Intent intent/*, int flags, int startId*/) {
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        final Context appContext = getApplicationContext();

        serviceHasFirstStart = true;

        //boolean deactivateProfile = false;
        boolean activateProfiles = false;
        boolean startOnBoot = false;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            //deactivateProfile = intent.getBooleanExtra(EXTRA_DEACTIVATE_PROFILE, false);
            activateProfiles = intent.getBooleanExtra(EXTRA_ACTIVATE_PROFILES, false);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        if (PPApplication.logEnabled()) {
            //if (deactivateProfile)
            //    PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEACTIVATE_PROFILE");
            if (activateProfiles)
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_ACTIVATE_PROFILES");
            if (startOnBoot)
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_BOOT");
            if (startOnPackageReplace)
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");
        }

        final boolean _startOnBoot = startOnBoot;
        final boolean _startOnPackageReplace = startOnPackageReplace;
        final boolean _activateProfiles = activateProfiles;
        //final boolean _deactivateProfile = deactivateProfile;
        PPApplication.startHandlerThread(/*"PhoneProfilesService.doForFirstStart"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart START");

                if (appContext == null)
                    return;

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService_doForFirstStart");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.doForFirstStart");

                    //PhoneProfilesService.cancelWork("delayedWorkAfterFirstStartWork", appContext);

                    /*if (_deactivateProfile) {
                        DatabaseHandler.getInstance(appContext).deactivateProfile();
                        ActivateProfileHelper.updateGUI(appContext, false, true);
                    }*/

                    // is called from PPApplication
                    //PPApplication.initRoot();
                    if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                        // grant root
                        PPApplication.isRootGranted();
                    } else {
                        synchronized (PPApplication.rootMutex) {
                            if (PPApplication.rootMutex.rootChecked) {
                                try {
                                    PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            } else {
                                try {
                                    PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, "ask for grant disabled");
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                    }

                    //PPApplication.getSUVersion();
                    PPApplication.settingsBinaryExists(false);
                    PPApplication.serviceBinaryExists(false);
                    PPApplication.getServicesList();

                    if (PPApplication.logEnabled()) {
                        // get list of TRANSACTIONS for "phone"
                        Object serviceManager = PPApplication.getServiceManager("phone");
                        if (serviceManager != null) {
                            // only log it
                            PPApplication.getTransactionCode(String.valueOf(serviceManager), "");
                        }
                    }

                    //GlobalGUIRoutines.setLanguage(appContext);
                    GlobalGUIRoutines.switchNightMode(getApplicationContext(), true);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                    dataWrapper.setDynamicLauncherShortcuts();

                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "application not started, start it");

                    //Permissions.clearMergedPermissions(appContext);

                    //if (!TonesHandler.isToneInstalled(/*TonesHandler.TONE_ID,*/ appContext))
                    //    TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext);
                    DatabaseHandler.getInstance(appContext).fixPhoneProfilesSilentInProfiles();

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "2");

                    //TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);
                    ActivateProfileHelper.setMergedRingNotificationVolumes(appContext/*, true*/);

                    ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "3");

                    AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
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
                        RingerModeChangeReceiver.setRingerMode(appContext, audioManager);
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                        PPNotificationListenerService.setZenMode(appContext, audioManager);
                        InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager);
                    }

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "4");

                    PPPExtenderBroadcastReceiver.setApplicationInForeground(appContext, "");

                    EventPreferencesCall.setEventCallEventType(appContext, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                    EventPreferencesCall.setEventCallEventTime(appContext, 0);
                    EventPreferencesCall.setEventCallPhoneNumber(appContext, "");

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "5");

                    // show info notification
                    ImportantInfoNotification.showInfoNotification(appContext);

                    // do not show it at start of PPP, will be shown for each profile activation.
                    //DrawOverAppsPermissionNotification.showNotification(appContext, false);
                    //IgnoreBatteryOptimizationNotification.showNotification(appContext, false);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "6");

                    // must be first
                    createContactsCache(appContext, true);
                    //must be seconds, this ads groups int contacts
                    createContactGroupsCache(appContext, true);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "7");

                    dataWrapper.fillProfileList(false, false);
                    for (Profile profile : dataWrapper.profileList)
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);
                    Profile.setActivatedProfileForDuration(appContext, 0);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "8");

                    dataWrapper.fillEventList();
                    for (Event event : dataWrapper.eventList)
                        StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "9");

                    GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(appContext);
                    LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                    //PPNotificationListenerService.clearNotifiedPackages(appContext);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "10");

                    DatabaseHandler.getInstance(appContext).deleteAllEventTimelines();
                    DatabaseHandler.getInstance(appContext).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "11");

                    if (_startOnPackageReplace) {
                        //PPApplication.logE("[REG] PhoneProfilesService.doFirstStart", "setMobileCellsAutoRegistration(true)");
                        MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, true);
                    }
                    else
                        PhoneStateScanner.startAutoRegistration(appContext, true);

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

                    registerReceiversAndWorkers(false);

                    //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "14");

                    DonationBroadcastReceiver.setAlarm(appContext);

                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "application started");

                    if (_startOnBoot)
                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, null, null, null, 0, "");
                    else if (_activateProfiles)
                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START, null, null, null, 0, "");

                    // start events

                    if (_activateProfiles) {
                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
                        editor.apply();
                        ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
                        ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
                        ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
                        ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
                        ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
                    }

                    //boolean packageReplaced = PPApplication.applicationPackageReplaced; //ApplicationPreferences.applicationPackageReplaced(appContext);
                    //PPApplication.logE("******** PhoneProfilesService.doForFirstStart - handler", "package replaced=" + packageReplaced);
                    //if (!packageReplaced) {
                        //setApplicationFullyStarted(true);

                        // work after first start

                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "called work for first start");

                        Data workData = new Data.Builder()
                                .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START)
                                .putBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, true)
                                .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, _activateProfiles)
                                .build();

                        OneTimeWorkRequest worker =
                                new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                        .addTag("afterFirstStartWork")
                                        .setInputData(workData)
                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = PPApplication.getWorkManagerInstance(appContext);
                            workManager.enqueue(worker);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    //}

                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "END");

                    /*if (PPApplication.applicationPackageReplaced) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "called work for package replaced");

                        // work for package replaced
                        Data workData = new Data.Builder()
                                .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_PACKAGE_REPLACED)
                                //.putBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, restartService)
                                .build();

                        OneTimeWorkRequest worker =
                                new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                        .addTag("packageReplacedWork")
                                        .setInputData(workData)
                                        //.setInitialDelay(5, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = PPApplication.getWorkManagerInstance(appContext);
                            workManager.enqueueUniqueWork("packageReplacedWork", ExistingWorkPolicy.REPLACE, worker);
                        } catch (Exception ignored) {
                        }
                    }*/

                    //dataWrapper.invalidateDataWrapper();

                } catch (Exception eee) {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", Log.getStackTraceString(eee));
                    //PPApplication.recordException(eee);
                    throw eee;
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "serviceHasFirstStart="+serviceHasFirstStart);

        startForegroundNotification = true;
        showProfileNotification(true, true/*, false*/);

        if (!serviceHasFirstStart) {
            Context appContext = getApplicationContext();
            String text = appContext.getString(R.string.ppp_app_name) + " " + appContext.getString(R.string.application_is_starting_toast);
            PPApplication.showToast(appContext.getApplicationContext(), text, Toast.LENGTH_SHORT);

            doForFirstStart(intent);
        }

        return START_REDELIVER_INTENT;
    }

    private void doCommand(final Intent intent) {
        //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "xxx");
        if (intent != null) {
            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "intent="+intent.getAction());
            final Context appContext = getApplicationContext();
            PPApplication.startHandlerThreadPPCommand();
            final Handler handler = new Handler(PPApplication.handlerThreadPPCommand.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService_doCommand");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "--- START");

                        /*if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                            // not needed, is already called in start of onStartCommand
                            //showProfileNotification();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                            clearProfileNotification();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                            // not needed, is already called in start of onStartCommand
                            //showProfileNotification();
                        }
                        else*/
                        if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SWITCH_KEYGUARD");

                            //boolean isScreenOn;
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

                            boolean secureKeyguard;
                            if (PPApplication.keyguardManager == null)
                                PPApplication.keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (PPApplication.keyguardManager != null) {
                                secureKeyguard = PPApplication.keyguardManager.isKeyguardSecure();
                                //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "secureKeyguard=" + secureKeyguard);
                                if (!secureKeyguard) {
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "getLockScreenDisabled=" + ActivateProfileHelper.getLockScreenDisabled(appContext));

                                    if (PPApplication.isScreenOn) {
                                        //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "screen on");

                                        if (ApplicationPreferences.prefLockScreenDisabled) {
                                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "disableKeyguard(), START_STICKY");
                                            reenableKeyguard();
                                            disableKeyguard();
                                        } else {
                                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "reenableKeyguard(), stopSelf(), START_NOT_STICKY");
                                            reenableKeyguard();
                                        }
                                    }
                                }
                            }
                        }
                        /*
                        else
                        if (intent.getBooleanExtra(EXTRA_START_LOCATION_UPDATES, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_START_LOCATION_UPDATES");
                            //synchronized (PPApplication.geofenceScannerMutex) {
                                if (PhoneProfilesService.getGeofencesScanner() != null) {
                                    GeofencesScanner.useGPS = true;
                                    PhoneProfilesService.getGeofencesScanner().startLocationUpdates();
                                }
                            //}
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_STOP_LOCATION_UPDATES, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_STOP_LOCATION_UPDATES");
                            //synchronized (PPApplication.geofenceScannerMutex) {
                            if (PhoneProfilesService.getGeofencesScanner() != null)
                                PhoneProfilesService.getGeofencesScanner().stopLocationUpdates();
                            //}
                        }
                        */
                        else
                        if (intent.getBooleanExtra(EXTRA_REGISTER_RECEIVERS_AND_WORKERS, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
                            registerReceiversAndWorkers(true);
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
                            unregisterReceiversAndWorkers();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
                            reregisterReceiversAndWorkers();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_REGISTER_CONTENT_OBSERVERS, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REGISTER_CONTENT_OBSERVERS");
                            registerContentObservers(true);
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_REGISTER_CALLBACKS, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REGISTER_CALLBACKS");
                            registerCallbacks(true);
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SIMULATE_RINGING_CALL");
                            doSimulatingRingingCall(intent);
                        }
                        //else
                        //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                        //    doSimulatingNotificationTone(intent);
                        else
                        if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_START_STOP_SCANNER");
                            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                            dataWrapper.fillEventList();
                            //dataWrapper.fillProfileList(false, false);
                            switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                                /*case PPApplication.SCANNER_START_GEOFENCE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_GEOFENCE_SCANNER");
                                    startGeofenceScanner(true, true, true, false);
                                    scheduleGeofenceWorker(true, true, false);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_GEOFENCE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_GEOFENCE_SCANNER");
                                    startGeofenceScanner(false, true, false, false);
                                    scheduleGeofenceWorker(false, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, true, true);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                                    startOrientationScanner(false, true, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                                    PhoneStateScanner.forceStart = false;
                                    startPhoneStateScanner(true, true, true, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                                    startPhoneStateScanner(false, true, false, false, false);
                                    break;*/
                                /*case PPApplication.SCANNER_START_TWILIGHT_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, true, true);
                                    break;*/
                                /*case PPApplication.SCANNER_STOP_TWILIGHT_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_TWILIGHT_SCANNER");
                                    startTwilightScanner(false, true, false);
                                    break;*/
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                    registerWifiScannerReceiver(true, dataWrapper, false);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, true);
                                    //registerWifiStateChangedBroadcastReceiver(true, false, true);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, true);
                                    registerWifiScannerReceiver(true, dataWrapper, true);
                                    break;
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                    registerBluetoothScannerReceivers(true, dataWrapper, false);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                                    registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, true);
                                    registerBluetoothScannerReceivers(true, dataWrapper, true);
                                    break;
                                case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
                                    //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                    registerWifiScannerReceiver(true, dataWrapper, false);
                                    scheduleWifiWorker(/*true,*/ dataWrapper, /*forScreenOn, false, false,*/ true);
                                    break;
                                case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                    registerBluetoothScannerReceivers(true, dataWrapper, false);
                                    scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
                                    break;
                                case PPApplication.SCANNER_RESTART_PHONE_STATE_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_PHONE_STATE_SCANNER");
                                    //PhoneStateScanner.forceStart = false;
                                    startPhoneStateScanner(true, true, dataWrapper, false, true);
                                    break;
                                case PPApplication.SCANNER_FORCE_START_PHONE_STATE_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                                    //PhoneStateScanner.forceStart = true;
                                    startPhoneStateScanner(true, false, dataWrapper, true, false);
                                    break;
                                case PPApplication.SCANNER_RESTART_GEOFENCE_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_GEOFENCE_SCANNER");
                                    registerLocationModeChangedBroadcastReceiver(true, dataWrapper);
                                    startGeofenceScanner(true, true, dataWrapper, true);
                                    scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                    break;
                                case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, false, dataWrapper);
                                    break;
                                case PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, false, dataWrapper);
                                    break;
                                case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
                                    //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_ALL_SCANNERS");

                                    final boolean fromBatteryChange = intent.getBooleanExtra(EXTRA_FROM_BATTERY_CHANGE, false);
                                    //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "fromBatteryChange="+fromBatteryChange);

                                    // wifi
                                    if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        /*PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn="+ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn);
                                        PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "PPApplication.isScreenOn="+PPApplication.isScreenOn);
                                        PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "wifi - canRestart="+canRestart);*/
                                        if ((!fromBatteryChange) || canRestart) {
                                            //PPApplication.logE("[TEST BATTERY] PhoneProfilesService.doCommand", "wifi - restart");
                                            //registerWifiConnectionBroadcastReceiver(true, dataWrapper, false);
                                            //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                            registerWifiAPStateChangeBroadcastReceiver(true, dataWrapper, false);
                                            registerWifiScannerReceiver(true, dataWrapper, false);
                                            scheduleWifiWorker(/*true,*/ dataWrapper, /*forScreenOn, false, false,*/ true);
                                        }
                                    }

                                    // bluetooth
                                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                            registerBluetoothStateChangedBroadcastReceiver(true, dataWrapper, false);
                                            registerBluetoothScannerReceivers(true, dataWrapper, false);
                                            scheduleBluetoothWorker(/*true,*/ dataWrapper /*forScreenOn, false,*/ /*, true*/);
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
                                            //PhoneStateScanner.forceStart = false;
                                            startPhoneStateScanner(true, true, dataWrapper, false, true);
                                        }
                                    }

                                    // location
                                    if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            registerLocationModeChangedBroadcastReceiver(true, dataWrapper);
                                            startGeofenceScanner(true, true, dataWrapper, true);
                                            scheduleGeofenceWorker(/*true,*/ dataWrapper /*forScreenOn,*/ /*, true*/);
                                        }
                                    }

                                    // orientation
                                    if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                                        boolean canRestart = (!ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) || PPApplication.isScreenOn;
                                        if ((!fromBatteryChange) || canRestart) {
                                            startOrientationScanner(true, true, dataWrapper);
                                        }
                                    }

                                    // twilight
                                    startTwilightScanner(true, true, dataWrapper);

                                    break;
                            }
                        }
                        /*else
                        if (intent.getBooleanExtra(EXTRA_RESTART_EVENTS, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_RESTART_EVENTS");
                            final boolean unblockEventsRun = intent.getBooleanExtra(EXTRA_UNBLOCK_EVENTS_RUN, false);
                            //final boolean reactivateProfile = intent.getBooleanExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, false);
                            final Context appContext = getApplicationContext();
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                            //dataWrapper.restartEvents(unblockEventsRun, true, reactivateProfile, false, false);
                            dataWrapper.restartEventsWithRescan(unblockEventsRun, false, false, false);
                            //dataWrapper.invalidateDataWrapper();
                        }*/

                        //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "--- END");

                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //PPApplication.logE("PhoneProfilesService.onConfigurationChanged", "xxx");
        //PPApplication.showProfileNotification(true, false/*, false*/);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from PhoneProfilesService.obConfigurationChanged");
        PPApplication.updateGUI(getApplicationContext(), true, true);
    }

    //------------------------

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
        contactGroupsCache.getContactGroupList(context);
    }

    public static ContactGroupsCache getContactGroupsCache()
    {
        return contactGroupsCache;
    }

    //------------------------

    // profile notification -------------------

    @SuppressLint("NewApi")
    void _showProfileNotification(Profile profile, boolean inHandlerThread, final DataWrapper dataWrapper,
                                          boolean refresh, boolean forFirstStart/*, boolean cleared*/)
    {
        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "xxx");

        if (PhoneProfilesService.instance == null)
            return;

        /*
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;
        */

        //PPApplication.logE("PhoneProfilesService.showProfileNotification", "no lockRefresh");

        final Context appContext = this; //dataWrapper.context.getApplicationContext();

//        if (Build.VERSION.SDK_INT >= 26) {
//            if (!PPApplication.createProfileNotificationChannel(appContext))
//                return;
//        }

        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "show enabled");

        // intent to LauncherActivity, for click on notification
        Intent launcherIntent = new Intent(ACTION_START_LAUNCHER_FROM_NOTIFICATION);

        int requestCode = 0;
        if (inHandlerThread && (profile != null))
            requestCode = (int)profile._id;

        if (!refresh && inHandlerThread) {
            // not redraw notification when activated profile is not changed
            // activated profile is in requestCode

            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "not redraw notification");

            // get old notification
            PendingIntent oldPIntent = PendingIntent.getBroadcast(appContext, requestCode, launcherIntent, PendingIntent.FLAG_NO_CREATE);
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "oldPIntent="+oldPIntent);
            if (oldPIntent != null) {
                String pNameNotification = PPApplication.prefNotificationProfileName;

                if (!pNameNotification.isEmpty()) {
                    String pName;
                    if (profile != null)
                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
                    else
                        pName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                    if (pName.equals(pNameNotification)) {
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "activated profile NOT changed");
                        return;
                    }
                }
            }
        }

        /*if (PPApplication.logEnabled()) {
            if (refresh)
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "refresh");
            else
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "activated profile changed");
        }*/

        String notificationNotificationStyle;
        boolean notificationShowProfileIcon;
        boolean notificationShowInStatusBar;
        //boolean notificationStatusBarPermanent;
        //boolean notificationDarkBackground;
        boolean notificationUseDecoration;
        boolean notificationPrefIndicator;
        boolean notificationHideInLockScreen;
        String notificationStatusBarStyle;
        String notificationTextColor;
        String notificationBackgroundColor;
        int notificationBackgroundCustomColor;
        boolean notificationNightMode;
        if (forFirstStart) {
            //ApplicationPreferences.notificationNotificationStyle(dataWrapper.context);
            notificationNotificationStyle = "1"; //ApplicationPreferences.notificationNotificationStyle;
            notificationShowProfileIcon = true;// && (Build.VERSION.SDK_INT >= 24);
            notificationShowInStatusBar = true;
            notificationUseDecoration = true;
            notificationPrefIndicator = false;
            notificationHideInLockScreen = false;
            notificationStatusBarStyle = "1";
            notificationTextColor = "0";
            notificationBackgroundColor = "0";
            notificationBackgroundCustomColor = 0xFFFFFFFF;
            notificationNightMode = false;
        }
        else {
            synchronized (PPApplication.applicationPreferencesMutex) {
                notificationNotificationStyle = ApplicationPreferences.notificationNotificationStyle;
                notificationShowProfileIcon = ApplicationPreferences.notificationShowProfileIcon || (Build.VERSION.SDK_INT < 24);
                notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar;
                //notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(appContext);
                //notificationDarkBackground = ApplicationPreferences.notificationDarkBackground(appContext);
                notificationUseDecoration = ApplicationPreferences.notificationUseDecoration;
                notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator;
                notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen;
                notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle;
                notificationTextColor = ApplicationPreferences.notificationTextColor;
                notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor;
                notificationBackgroundCustomColor = ApplicationPreferences.notificationBackgroundCustomColor;
                notificationNightMode = ApplicationPreferences.notificationNightMode;
            }
        }

        Notification.Builder notificationBuilder;

        RemoteViews contentView = null;
        RemoteViews contentViewLarge = null;

        boolean useDecorator;
        int useNightColor = 0;
        boolean profileIconExists = true;
        //boolean preferencesIndicatorExists = true;
        boolean preferencesIndicatorExistsLarge = true;

        if (notificationNotificationStyle.equals("0")) {
            // ----- create content view

            useDecorator = (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) || (Build.VERSION.SDK_INT >= 26);
            useDecorator = useDecorator && notificationUseDecoration;

            int nightModeFlags =
                    appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            //if (notificationNightMode) {
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    useNightColor = 1;
                    // this is possible only when device has option for set background color
                    //if ((Build.VERSION.SDK_INT < 29) && notificationNightMode)
                    //    notificationTextColor = "2";
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    useNightColor = 2;
                    // this is possible only when device has option for set background color
                    //if ((Build.VERSION.SDK_INT < 29) && notificationNightMode)
                    //    notificationTextColor = "1";
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
            //}
            switch (notificationBackgroundColor) {
                case "1":
                case "3":
                    notificationTextColor = "2";
                    break;
            }
            if ((Build.VERSION.SDK_INT >= 24) && (Build.VERSION.SDK_INT < 29))
                useDecorator = useDecorator && (!notificationNightMode) && notificationBackgroundColor.equals("0");

            boolean powerShadeInstalled = false;
            PackageManager pm = getPackageManager();
            try {
                pm.getPackageInfo("com.treydev.pns", PackageManager.GET_ACTIVITIES);
                powerShadeInstalled = true;
            } catch (Exception ignored) {}

            if (powerShadeInstalled) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator) {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_no_decorator);
                        //preferencesIndicatorExists = false;
                    }
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                        //preferencesIndicatorExists = false;
                    }
                } else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "Power Shade installed");
            }
            else
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator) {
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui_no_decorator);
                        preferencesIndicatorExistsLarge = false;
                    }
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                    if (!useDecorator) {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_miui_no_decorator);
                        //preferencesIndicatorExists = false;
                    }
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                        //preferencesIndicatorExists = false;
                    }
                } else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "miui");
            } else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                    if (!useDecorator) {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_emui_no_decorator);
                        //preferencesIndicatorExists = false;
                    }
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                        //preferencesIndicatorExists = false;
                    }
                } else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "emui");
            } else if (PPApplication.deviceIsSamsung) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator) {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_samsung_no_decorator);
                        //preferencesIndicatorExists = false;
                    }
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                        //preferencesIndicatorExists = false;
                    }
                } else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "samsung");
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator) {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_no_decorator);
                        //preferencesIndicatorExists = false;
                    }
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                        //preferencesIndicatorExists = false;
                    }
                } else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "others");
            }
            //}
        }
        else
            useDecorator = true; // for native style use decorator, when is supported by system

        boolean isIconResourceID;
        String iconIdentifier;
        String pName;
        Spannable profileName;
        Bitmap iconBitmap;
        Bitmap preferencesIndicator;

        // ----- get profile icon, preference indicators, profile name
        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "profile="+profile);
        if (profile != null)
        {
            //PPApplication.logE("PhoneProfilesService.showProfileNotification", "profile != null");
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
            profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper);
            //pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, dataWrapper, false, appContext);
            // get string from spannable
            Spannable sbt = new SpannableString(profileName);
            Object[] spansToRemove = sbt.getSpans(0, profileName.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            pName = sbt.toString();

            if (inHandlerThread) {
                //if (notificationStatusBarStyle.equals("0"))
                profile.generateIconBitmap(appContext, false, 0, false);
                if (notificationPrefIndicator && (notificationNotificationStyle.equals("0")))
                    profile.generatePreferencesIndicator(appContext, false, 0);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else {
                iconBitmap = null;
                preferencesIndicator = null;
            }
        }
        else
        {
            isIconResourceID = true;
            iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
            if (inHandlerThread)
                pName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);
            else
                pName = "";
            profileName = new SpannableString(pName);
            iconBitmap = null;
            preferencesIndicator = null;
        }
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneProfilesService._showProfileNotification", "isIconResourceID=" + isIconResourceID);
            PPApplication.logE("PhoneProfilesService._showProfileNotification", "iconBitmap=" + iconBitmap);
        }*/

        PPApplication.setNotificationProfileName(appContext, pName);

        PendingIntent pIntent = PendingIntent.getBroadcast(appContext, requestCode, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // ----- create notificationBuilders
        if (Build.VERSION.SDK_INT >= 26) {
            PPApplication.createProfileNotificationChannel(appContext);
            notificationBuilder = new Notification.Builder(appContext, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
            //notificationBuilder.setSettingsText("Test");
        }
        else {
            notificationBuilder = new Notification.Builder(appContext);
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationShowInStatusBar="+notificationShowInStatusBar);
            if (notificationShowInStatusBar) {
                KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (myKM != null) {
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    //PPApplication.logE("PhoneProfilesService.showProfileNotification", "screenUnlocked="+screenUnlocked);
                    //PPApplication.logE("PhoneProfilesService.showProfileNotification", "hide in lockscreen parameter="+notificationHideInLockScreen);
                    if ((notificationHideInLockScreen && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                    else
                        notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                }
                else
                    notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            }
            else
                notificationBuilder.setPriority(Notification.PRIORITY_MIN);
        }

        notificationBuilder.setContentIntent(pIntent);
        notificationBuilder.setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor));
        notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
        notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

        //notificationBuilder.setTicker(profileName);

        // ----- set icons
        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "inHandlerThread="+inHandlerThread);
        if (inHandlerThread) {
            if (isIconResourceID) {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "profile icon is internal resource");
                int iconSmallResource;
                if (iconBitmap != null) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "icon has changed color");
                    if (notificationStatusBarStyle.equals("0")) {
                        // colorful icon
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "enabled is colorful icon in status bar");

                        // FC in Note 4, 6.0.1 :-/
                        boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
                          /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                           Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                          ) &&*/
                                (android.os.Build.VERSION.SDK_INT == 23);
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "isNote4="+isNote4);
                        if (/*(android.os.Build.VERSION.SDK_INT >= 23) &&*/ (!isNote4)) {
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon from picture");
                            notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                        } else {
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon default icon");
                            iconSmallResource = R.drawable.ic_profile_default_notify_color;
                            try {
                                if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                    Object idx = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                    if (idx != null)
                                        iconSmallResource = (int) idx;
                                }
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }
                    } else {
                        // native icon
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "colorful icon in status bar is disabled");

                        iconSmallResource = R.drawable.ic_profile_default_notify;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object idx = Profile.profileIconNotifyId.get(iconIdentifier);
                                if (idx != null)
                                    iconSmallResource = (int) idx;
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    if (notificationNotificationStyle.equals("0")) {
                        try {
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            if (profileIconExists) {
                                if (contentView != null)
                                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    else {
                        if (notificationShowProfileIcon)
                            notificationBuilder.setLargeIcon(iconBitmap);
                    }
                } else {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "icon has NOT changed color");

                    if (notificationStatusBarStyle.equals("0")) {
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "enabled is colorful icon in status bar");
                        iconSmallResource = R.drawable.ic_profile_default_notify_color;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object idx = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                if (idx != null)
                                    iconSmallResource = (int) idx;
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    } else {
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "colorful icon in status bar is disabled");
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                        try {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                Object idx = Profile.profileIconNotifyId.get(iconIdentifier);
                                if (idx != null)
                                    iconSmallResource = (int) idx;
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    notificationBuilder.setSmallIcon(iconSmallResource);

                    int iconLargeResource = Profile.getIconResource(iconIdentifier);
                    Bitmap largeIcon = BitmapManipulator.getBitmapFromResource(iconLargeResource, true, appContext);
                    if (notificationNotificationStyle.equals("0")) {
                        try {
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if (profileIconExists) {
                                if (contentView != null)
                                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    else {
                        if (notificationShowProfileIcon)
                            notificationBuilder.setLargeIcon(largeIcon);
                    }
                }
            } else {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "profile icon is custom - external picture");
                // FC in Note 4, 6.0.1 :-/
                boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
                    /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                     Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                    ) &&*/
                        (android.os.Build.VERSION.SDK_INT == 23);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "isNote4="+isNote4);
                if (/*(Build.VERSION.SDK_INT >= 23) &&*/ (!isNote4) && (iconBitmap != null)) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon from picture");
                    notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                } else {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon default icon");
                    int iconSmallResource;
                    if (notificationStatusBarStyle.equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }

                if (notificationNotificationStyle.equals("0")) {
                    try {
                        if (iconBitmap != null)
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        else
                            contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                        if (profileIconExists) {
                            if (contentView != null) {
                                if (iconBitmap != null)
                                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                                else
                                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                else {
                    if (notificationShowProfileIcon) {
                        if (iconBitmap != null)
                            notificationBuilder.setLargeIcon(iconBitmap);
                        else
                            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_default));
                    }
                }
            }
        }
        else {
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create empty icon");
            notificationBuilder.setSmallIcon(R.drawable.ic_profile_default_notify);
            if (notificationNotificationStyle.equals("0")) {
                try {
                    contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                    if (profileIconExists) {
                        if (contentView != null)
                            contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            else {
                if (notificationShowProfileIcon)
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty));
            }
        }

        // notification title
        if (notificationNotificationStyle.equals("0")) {
            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if (contentView != null)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            // Maybe this produce: android.app.RemoteServiceException: Bad notification(tag=null, id=700420) posted from package sk.henrichg.phoneprofilesplus, crashing app(uid=10002, pid=13431): Couldn't inflate contentViewsandroid.widget.RemoteViews$ActionException: android.widget.RemoteViews$ActionException: view: android.widget.ImageView doesn't have method: setText(interface java.lang.CharSequence)
            try {
                notificationBuilder.setContentTitle(profileName);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContentTitle()="+profileName);
            } catch (Exception e) {
                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
        else {
            notificationBuilder.setContentTitle(profileName);
        }

        // profile preferences indicator
        try {
            if (notificationNotificationStyle.equals("0")) {
                if (notificationPrefIndicator) {
                    if (preferencesIndicator != null) {
                        if (preferencesIndicatorExistsLarge) {
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
                            contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.VISIBLE);
                        }
                    } else {
                        if (preferencesIndicatorExistsLarge) {
                            //contentViewLarge.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);
                            contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.GONE);
                        }
                    }
                    // Maybe this produce: android.app.RemoteServiceException: Bad notification(tag=null, id=700420) posted from package sk.henrichg.phoneprofilesplus, crashing app(uid=10002, pid=13431): Couldn't inflate contentViewsandroid.widget.RemoteViews$ActionException: android.widget.RemoteViews$ActionException: view: android.widget.ImageView doesn't have method: setText(interface java.lang.CharSequence)
                    try {
                        String indicators = ProfilePreferencesIndicator.getString(profile, 0, appContext);
                        notificationBuilder.setContentText(indicators);
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContentText()="+indicators);
                    } catch (Exception e) {
                        Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    }
                }
                else {
                    if (preferencesIndicatorExistsLarge) {
                        //contentViewLarge.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.GONE);
                    }
                    // Maybe this produce: android.app.RemoteServiceException: Bad notification(tag=null, id=700420) posted from package sk.henrichg.phoneprofilesplus, crashing app(uid=10002, pid=13431): Couldn't inflate contentViewsandroid.widget.RemoteViews$ActionException: android.widget.RemoteViews$ActionException: view: android.widget.ImageView doesn't have method: setText(interface java.lang.CharSequence)
                    try {
                        notificationBuilder.setContentText(null);
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContentText()=null");
                    } catch (Exception e) {
                        Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    }
                }
            }
            else {
                if (notificationPrefIndicator) {
                    String indicators = ProfilePreferencesIndicator.getString(profile, 0, appContext);
                    notificationBuilder.setContentText(indicators);
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContentText()="+indicators);
                }
                else {
                    notificationBuilder.setContentText(null);
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContentText()=null");
                }
            }
        } catch (Exception e) {
            Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        if (inHandlerThread) {
            if (Event.getGlobalEventsRunning() &&
                    PPApplication.getApplicationStarted(true)) {

                PendingIntent pIntentRE; //= null;
                /*if (Event.getGlobalEventsRunning() &&
                        PPApplication.getApplicationStarted(true)) {*/
                // intent for restart events
                Intent intentRE = new Intent(appContext, RestartEventsFromGUIActivity.class);
                intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                pIntentRE = PendingIntent.getActivity(appContext, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
                //}

                if (notificationNotificationStyle.equals("0")) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationBackgroundColor="+notificationBackgroundColor);

                    int restartEventsId;
                    if (notificationBackgroundColor.equals("1") || notificationBackgroundColor.equals("3")) {
                        // dark or black
                        restartEventsId = R.drawable.ic_widget_restart_events_dark;
                    } else if (notificationBackgroundColor.equals("5")) {
                        // custom color
                        if (ColorUtils.calculateLuminance(notificationBackgroundCustomColor) < 0.23)
                            restartEventsId = R.drawable.ic_widget_restart_events_dark;
                        else
                            restartEventsId = R.drawable.ic_widget_restart_events;
                    } else {
                        // native
                        if (Build.VERSION.SDK_INT >= 29) {
                            if (useNightColor == 1)
                                restartEventsId = R.drawable.ic_widget_restart_events_dark;
                            else
                                restartEventsId = R.drawable.ic_widget_restart_events;
                        } else {
                            if (notificationTextColor.equals("1"))
                                restartEventsId = R.drawable.ic_widget_restart_events;
                            else if (notificationTextColor.equals("2"))
                                restartEventsId = R.drawable.ic_widget_restart_events_dark;
                            else
                                restartEventsId = R.drawable.ic_widget_restart_events;
                        }
                    }

                    try {
                        contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                        contentViewLarge.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                        contentViewLarge.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);

                        if (contentView != null) {
                            contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                            contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                            contentView.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                } else {
                    Notification.Action.Builder actionBuilder;
                    //if (Build.VERSION.SDK_INT >= 23)
                    actionBuilder = new Notification.Action.Builder(
                            Icon.createWithResource(appContext, R.drawable.ic_widget_restart_events),
                            appContext.getString(R.string.menu_restart_events),
                            pIntentRE);
                /*else
                    actionBuilder = new Notification.Action.Builder(
                            R.drawable.ic_widget_restart_events,
                            appContext.getString(R.string.menu_restart_events),
                            pIntentRE);*/
                    notificationBuilder.addAction(actionBuilder.build());
                }
            }
        }
        else {
            try {
                if (contentViewLarge != null)
                    contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
                if (contentView != null)
                    contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }

        if (notificationNotificationStyle.equals("0")) {
            //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "background not 2 or 4");
            switch (notificationBackgroundColor) {
                case "3":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    int color = ContextCompat.getColor(this, R.color.notificationBlackBackgroundColor);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    break;
                case "1":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    color = ContextCompat.getColor(this, R.color.notificationDarkBackgroundColor);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                    break;
                case "5":
                    //if (!notificationNightMode || (useNightColor == 1)) {
                    //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "background color 5");
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                    break;
                default:
                    //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "transparent background");
                    //int color = getResources().getColor(R.color.notificationBackground);
                    contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                    if (contentView != null)
                        contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                    break;
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationBackgroundColor=" + notificationBackgroundColor);
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationTextColor=" + notificationTextColor);
                PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "notificationTextColor=" + notificationTextColor);
            }*/
            if (notificationTextColor.equals("1")/* && (!notificationDarkBackground)*/) {
                //if (Build.VERSION.SDK_INT < 25)
                    contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                //else
                //    contentViewLarge.setTextColor(R.id.notification_activated_profile_name,
                //            ContextCompat.getColorStateList(appContext, R.color.widget_text_color_black));
                if (contentView != null) {
                    //if (Build.VERSION.SDK_INT < 25)
                        contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                    //else
                    //    contentView.setTextColor(R.id.notification_activated_profile_name,
                    //            ContextCompat.getColorStateList(appContext, R.color.widget_text_color_black));
                }
            } else if (notificationTextColor.equals("2")/* || notificationDarkBackground*/) {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "before set text color");
                //if (Build.VERSION.SDK_INT < 25)
                    contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                //else
                //    contentViewLarge.setTextColor(R.id.notification_activated_profile_name,
                //            ContextCompat.getColorStateList(appContext, R.color.widget_text_color_white));
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "after set text color");
                if (contentView != null)
                    //if (Build.VERSION.SDK_INT < 25)
                        contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                    //else
                    //    contentView.setTextColor(R.id.notification_activated_profile_name,
                    //            ContextCompat.getColorStateList(appContext, R.color.widget_text_color_white));
            }

            //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "after set text color");
        }

        if (notificationNotificationStyle.equals("0")) {
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                if (useDecorator)
                    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                String layoutType = ApplicationPreferences.notificationLayoutType;
                switch (layoutType) {
                    case "1":
                        // only large layout
                        notificationBuilder.setCustomContentView(contentViewLarge);
                        break;
                    case "2":
                        // only small layout
                        notificationBuilder.setCustomContentView(contentView);
                        break;
                    default:
                        // expandable layout
                        notificationBuilder.setCustomContentView(contentView);
                        notificationBuilder.setCustomBigContentView(contentViewLarge);
                        break;
                }
            } else {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContent");
                notificationBuilder.setContent(contentViewLarge);
            }
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneProfilesService._showProfileNotification", "useDecorator=" + useDecorator);
            PPApplication.logE("PhoneProfilesService._showProfileNotification", "ApplicationPreferences.notificationShowButtonExit(appContext)=" + ApplicationPreferences.notificationShowButtonExit(appContext));
        }*/

        if ((ApplicationPreferences.notificationShowButtonExit) && useDecorator) {
            // add action button to stop application

            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "add action button");

            // intent to LauncherActivity, for click on notification
            Intent exitAppIntent = new Intent(appContext, ExitApplicationActivity.class);
            // clear all opened activities
            exitAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pExitAppIntent = PendingIntent.getActivity(appContext, 0, exitAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action.Builder actionBuilder;
            //if (Build.VERSION.SDK_INT >= 23)
                actionBuilder = new Notification.Action.Builder(
                        Icon.createWithResource(appContext, R.drawable.ic_action_exit_app_white),
                        appContext.getString(R.string.menu_exit),
                        pExitAppIntent);
            /*else
                actionBuilder = new Notification.Action.Builder(
                        R.drawable.ic_action_exit_app_white,
                        appContext.getString(R.string.menu_exit),
                        pExitAppIntent);*/
            notificationBuilder.addAction(actionBuilder.build());
        }

        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setOnlyAlertOnce=true");
        notificationBuilder.setOnlyAlertOnce(true);

        Notification phoneProfilesNotification;
        try {
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "before build");
            phoneProfilesNotification = notificationBuilder.build();
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "after build");
        } catch (Exception e) {
            phoneProfilesNotification = null;
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "build crash="+ Log.getStackTraceString(e));
        }

        if (phoneProfilesNotification != null) {

            if (Build.VERSION.SDK_INT < 26) {
                phoneProfilesNotification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
                phoneProfilesNotification.ledOnMS = 0;
                phoneProfilesNotification.ledOffMS = 0;
                phoneProfilesNotification.sound = null;
                phoneProfilesNotification.vibrate = null;
                phoneProfilesNotification.defaults &= ~DEFAULT_SOUND;
                phoneProfilesNotification.defaults &= ~DEFAULT_VIBRATE;
            }

            //if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                // do not use Notification.FLAG_ONGOING_EVENT,
                // with this flag, is not possible to minimize this notification
                phoneProfilesNotification.flags |= Notification.FLAG_NO_CLEAR;// | Notification.FLAG_ONGOING_EVENT;
            /*} else {
            //    setAlarmForNotificationCancel(appContext);
            }*/

            //if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                if (startForegroundNotification) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "startForeground()");
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                    startForegroundNotification = false;
                }
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "notify()");
                        notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                    }
                }

                //runningInForeground = true;
            /*}
            else {
                NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
            }*/
        }
    }

    void showProfileNotification(final boolean refresh, boolean forServiceStart/*, final boolean cleared*/) {
        //if (Build.VERSION.SDK_INT >= 26) {
            //if (DebugVersion.enabled)
            //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","forServiceStart="+forServiceStart);
        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","refresh="+(clear || refresh));

        //if (!runningInForeground) {
            if (forServiceStart) {
                //if (!isServiceRunningInForeground(appContext, PhoneProfilesService.class)) {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                _showProfileNotification(null, false, dataWrapper, true, true/*, cleared*/);
                //dataWrapper.invalidateDataWrapper();
                return;
            }
        //}

        //if (DebugVersion.enabled)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","before run handler");

        boolean clear = false;
        if (Build.MANUFACTURER.equals("HMD Global"))
            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
            clear = true;
        if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
            // clear it for redraw icon in "Glance view" for LG with Android 9
            clear = true;
        if (clear) {
            // next show will be with startForeground()
            clearProfileNotification(/*getApplicationContext(), true*/);
        }

        long now = SystemClock.elapsedRealtime();

        if (clear || refresh || ((now - PPApplication.lastRefreshOfProfileNotification) >= PPApplication.DURATION_FOR_GUI_REFRESH))
        {
            if (PhoneProfilesService.instance != null) {
                //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","refresh");

                final boolean _clear = clear;
                PPApplication.startHandlerThreadProfileNotification();
                final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                        Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);

                        //boolean fullyStarted = false;
                        //if (PhoneProfilesService.getInstance() != null)
                        //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
                        boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
                        boolean fullyStarted = PPApplication.applicationFullyStarted;
                        if ((!fullyStarted) || applicationPackageReplaced)
                            profile = null;

                        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification", "_showProfileNotification()");
                        _showProfileNotification(profile, true, dataWrapper, _clear || refresh, false/*, cleared*/);
                        //dataWrapper.invalidateDataWrapper();
                    }
                });
            }
        }
        else {
            //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","do not refresh");

            ShowProfileNotificationBroadcastReceiver.setAlarm(getApplicationContext());
        }

        PPApplication.lastRefreshOfProfileNotification = SystemClock.elapsedRealtime();
    }

    private void clearProfileNotification(/*Context context, boolean onlyEmpty*/)
    {
        /*if (onlyEmpty) {
            final Context appContext = getApplicationContext();
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
            _showProfileNotification(null, false, dataWrapper, true);
            dataWrapper.invalidateDataWrapper();
        }
        else {*/
            try {
                //final Context appContext = getApplicationContext();
                //if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                /*if (onlyEmpty) {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                }
                else {*/
                    startForegroundNotification = true;
                    stopForeground(true);
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);

                //}
            } catch (Exception e) {
                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
            //runningInForeground = false;
        //}
    }

    /*
    public static boolean isServiceRunning(Context context, Class<?> serviceClass, boolean inForeground) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    if (inForeground) {
                        PPApplication.logE("PhoneProfilesService.isServiceRunningInForeground", "service.foreground=" + service.foreground);
                        return service.foreground;
                    }
                    else
                        return true;
                }
            }
        }
        PPApplication.logE("PhoneProfilesService.isServiceRunningInForeground", "false");
        return false;
    }
    */

    //--------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        //PPApplication.logE("$$$ disableKeyguard","keyguardLock="+keyguardLock);
        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.disableKeyguard();
            } catch (Exception e) {
                Log.e("PhoneProfilesService", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private void reenableKeyguard()
    {
        //PPApplication.logE("$$$ reenableKeyguard","keyguardLock="+keyguardLock);
        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.reenableKeyguard();
            } catch (Exception e) {
                Log.e("PhoneProfilesService", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    //--------------------------------------

    // Location ----------------------------------------------------------------

    public static boolean isLocationEnabled(Context context) {
        boolean enabled;
        if (Build.VERSION.SDK_INT >= 28) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
                enabled = lm.isLocationEnabled();
            else
                enabled = true;
        }
        else {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                PPApplication.recordException(e);
            }
            enabled = locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        //PPApplication.logE("PhoneProfilesService.isLocationEnabled", "enabled="+enabled);
        return enabled;
    }

    public static boolean isWifiSleepPolicySetToNever(Context context) {
        int wifiSleepPolicy = -1;
        try {
            wifiSleepPolicy = Settings.Global.getInt(context.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY);
        } catch (Settings.SettingNotFoundException e) {
            PPApplication.recordException(e);
        }
        return wifiSleepPolicy == Settings.Global.WIFI_SLEEP_POLICY_NEVER;
    }

    private void startGeofenceScanner(boolean resetUseGPS) {
        //PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "xxx");
        if (PPApplication.geofencesScanner != null) {
            PPApplication.geofencesScanner.disconnect();
            PPApplication.geofencesScanner = null;
        }

        PPApplication.geofencesScanner = new GeofencesScanner(getApplicationContext());
        //PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "geofencesScanner="+geofencesScanner);
        /*if (instance != null) {
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "instance==this? " + (instance == this));
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService.getGeofencesScanner()=" + PhoneProfilesService.getInstance().getGeofencesScanner());
        }*/
        PPApplication.geofencesScanner.connect(resetUseGPS);
    }

    private void stopGeofenceScanner() {
        //PPApplication.logE("PhoneProfilesService.stopGeofenceScanner", "xxx");
        if (PPApplication.geofencesScanner != null) {
            PPApplication.geofencesScanner.disconnect();
            PPApplication.geofencesScanner = null;
        }
    }

    boolean isGeofenceScannerStarted() {
        return (PPApplication.geofencesScanner != null);
    }

    GeofencesScanner getGeofencesScanner() {
        return PPApplication.geofencesScanner;
    }

    //--------------------------------------------------------------------------

    // Phone state ----------------------------------------------------------------

    private void startPhoneStateScanner() {
        if (PPApplication.phoneStateScanner != null) {
            PPApplication.phoneStateScanner.disconnect();
            PPApplication.phoneStateScanner = null;
        }

        PPApplication.phoneStateScanner = new PhoneStateScanner(getApplicationContext());
        PPApplication.phoneStateScanner.connect();
    }

    private void stopPhoneStateScanner() {
        if (PPApplication.phoneStateScanner != null) {
            PPApplication.phoneStateScanner.disconnect();
            PPApplication.phoneStateScanner = null;
        }
    }

    boolean isPhoneStateScannerStarted() {
        return (PPApplication.phoneStateScanner != null);
    }


    PhoneStateScanner getPhoneStateScanner() {
        return PPApplication.phoneStateScanner;
    }

    //--------------------------------------------------------------------------

    // Device orientation ----------------------------------------------------------------

    private void startOrientationScanner() {
        if (PPApplication.mStartedOrientationSensors)
            stopListeningOrientationSensors();

        startListeningOrientationSensors();
    }

    private void stopOrientationScanner() {
        stopListeningOrientationSensors();
    }

    boolean isOrientationScannerStarted() {
        return PPApplication.mStartedOrientationSensors;
    }

    @SuppressLint("NewApi")
    private void startListeningOrientationSensors() {
        //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "mStartedOrientationSensors="+mStartedOrientationSensors);
        if (!PPApplication.mStartedOrientationSensors) {
            PPApplication.orientationScanner = new OrientationScanner();
            PPApplication.startHandlerThreadOrientationScanner();
            Handler handler = new Handler(PPApplication.handlerThreadOrientationScanner.getLooper());
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "orientationScanner="+orientationScanner);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "(PPApplication.handlerThreadOrientationScanner="+PPApplication.handlerThreadOrientationScanner);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "handler="+handler);

            String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(this);
            if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
                // start scanning in power save mode is not allowed
                return;

            //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            //if (DatabaseHandler.getInstance(getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) == 0)
            //    return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
            if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
                interval *= 2;

            if (PPApplication.accelerometerSensor != null) {
                if (PPApplication.accelerometerSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.accelerometerSensor, 1000000 * interval, handler);
            }
            if (PPApplication.magneticFieldSensor != null) {
                if (PPApplication.magneticFieldSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.magneticFieldSensor, 1000000 * interval, handler);
            }

            if (PPApplication.lightSensor != null) {
                PPApplication.handlerThreadOrientationScanner.mMaxLightDistance = PPApplication.lightSensor.getMaximumRange();
                if (PPApplication.lightSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.lightSensor, 1000000 * interval, handler);
            }

            if (PPApplication.proximitySensor != null) {
                PPApplication.handlerThreadOrientationScanner.mMaxProximityDistance = PPApplication.proximitySensor.getMaximumRange();
                if (PPApplication.proximitySensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(PPApplication.orientationScanner, PPApplication.proximitySensor, 1000000 * interval, handler);
            }

            //Sensor orientation = PPApplication.getOrientationSensor(this);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","orientation="+orientation);
            PPApplication.mStartedOrientationSensors = true;

            PPApplication.handlerThreadOrientationScanner.mDisplayUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.mSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.mDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;

            PPApplication.handlerThreadOrientationScanner.tmpSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
            PPApplication.handlerThreadOrientationScanner.tmpSideTimestamp = 0;

            setOrientationSensorAlarm(getApplicationContext());
            Intent intent = new Intent(ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
            sendBroadcast(intent);
        }
    }

    private void stopListeningOrientationSensors() {
        //PPApplication.logE("PhoneProfilesService.stopListeningOrientationSensors", "PPApplication.sensorManager="+PPApplication.sensorManager);
        if (PPApplication.sensorManager != null) {
            PPApplication.sensorManager.unregisterListener(PPApplication.orientationScanner);
            removeOrientationSensorAlarm(getApplicationContext());
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
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsOrientationSensorWork");
            workManager.cancelAllWorkByTag("elapsedAlarmsOrientationSensorWork");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
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

        /*if (PPApplication.logEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("EventPreferencesOrientation.setAlarm", "alarmTime=" + result);
        }*/

        /*if (ApplicationPreferences.applicationUseAlarmClock(context)) {
            //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
            //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
        } else {
            Calendar now = Calendar.getInstance();
            long elapsedTime = (alarmTime + Event.EVENT_ALARM_TIME_OFFSET) - now.getTimeInMillis();

            if (PPApplication.logEnabled()) {
                long allSeconds = elapsedTime / 1000;
                long hours = allSeconds / 60 / 60;
                long minutes = (allSeconds - (hours * 60 * 60)) / 60;
                long seconds = allSeconds % 60;

                PPApplication.logE("EventPreferencesSMS.setAlarm", "elapsedTime=" + hours + ":" + minutes + ":" + seconds);
            }

            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_SMS_EVENT_END_SENSOR)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                            .addTag("elapsedAlarmsOrientationSensorWork")
                            .setInputData(workData)
                            .setInitialDelay(elapsedTime, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context);
                PPApplication.logE("[HANDLER] EventPreferencesSMS.setAlarm", "enqueueUniqueWork - elapsedTime="+elapsedTime);
                //workManager.enqueueUniqueWork("elapsedAlarmsOrientationSensorWork", ExistingWorkPolicy.REPLACE, worker);
                workManager.enqueue(worker);
            } catch (Exception ignored) {}
        }*/

        //Intent intent = new Intent(context, OrientationEventEndBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
        //intent.setClass(context, OrientationEventEndBroadcastReceiver.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
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

    // Twilight scanner ----------------------------------------------------------------

    private void startTwilightScanner() {
        if (PPApplication.twilightScanner != null) {
            PPApplication.twilightScanner.stop();
            PPApplication.twilightScanner = null;
        }

        PPApplication.twilightScanner = new TwilightScanner(getApplicationContext());
        PPApplication.twilightScanner.start();
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
            //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulate ringing call");

            Context context = getApplicationContext();

            ringingCallIsSimulating = false;

            // wait for change ringer mode + volume
            PPApplication.sleep(1500);

            int oldRingerMode = intent.getIntExtra(EXTRA_OLD_RINGER_MODE, 0);
            //int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EXTRA_OLD_ZEN_MODE, 0);
            String oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE);
            //int oldSystemRingerVolume = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_VOLUME, -1);
            int newRingerMode = ApplicationPreferences.prefRingerMode;
            int newZenMode = ApplicationPreferences.prefZenMode;
            //int newRingerVolume = ActivateProfileHelper.getRingerVolume(context);
            String newRingtone = "";
            String phoneNumber = "";
            if (PPPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_3_0))
                phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;

            // get ringtone from contact
            boolean phoneNumberFound = false;
            try {
                Uri contactLookup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                Cursor contactLookupCursor = context.getContentResolver().query(contactLookup, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.CUSTOM_RINGTONE}, null, null, null);
                if (contactLookupCursor != null) {
                    if (contactLookupCursor.moveToNext()) {
                        newRingtone = contactLookupCursor.getString(contactLookupCursor.getColumnIndex(ContactsContract.PhoneLookup.CUSTOM_RINGTONE));
                        if (newRingtone == null)
                            newRingtone = "";
                        //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingtone from contact="+newRingtone);
                        phoneNumberFound = true;
                    }
                    contactLookupCursor.close();
                }
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, true);
                newRingtone = "";
            } catch (Exception e) {
                newRingtone = "";
            }
            if ((!phoneNumberFound) || newRingtone.isEmpty()) {
                try {
                    Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                    if (uri != null)
                        newRingtone = uri.toString();
                    else
                        newRingtone = "";
                    //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingtone from settings="+newRingtone);
                } catch (SecurityException e) {
                    Permissions.grantPlayRingtoneNotificationPermissions(context, false);
                    newRingtone = "";
                } catch (Exception e) {
                    newRingtone = "";
                }
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingerMode=" + oldRingerMode);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldSystemRingerMode=" + oldSystemRingerMode);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldZenMode=" + oldZenMode);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingerMode=" + newRingerMode);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newZenMode=" + newZenMode);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingtone=" + oldRingtone);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldSystemRingerVolume=" + oldSystemRingerVolume);
                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingerVolume=" + newRingerVolume);
            }*/

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {

                //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringing is audible");

                boolean simulateRinging = false;
                int stream = AudioManager.STREAM_RING;

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
                           stream = AudioManager.STREAM_ALARM;
                           //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (1)");
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

                if (oldRingtone.isEmpty() || (!newRingtone.isEmpty() && !newRingtone.equals(oldRingtone)))
                    simulateRinging = true;

                //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulateRinging=" + simulateRinging);

                if (simulateRinging)
                    startSimulatingRingingCall(stream, newRingtone);
            }

        }
    }

    private void startSimulatingRingingCall(int stream, String ringtone) {
        stopSimulatingRingingCall(/*true*/);
        if (!ringingCallIsSimulating) {
            //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "stream="+stream);
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            //stopSimulatingNotificationTone(true);

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

            if ((ringtone != null) && !ringtone.isEmpty()) {
                RingerModeChangeReceiver.internalChange = true;

                // play repeating: default ringtone with ringing volume level
                try {
                    AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    if (am != null) {
                        am.setMode(AudioManager.MODE_NORMAL);
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }

                    //int requestType = AudioManager.AUDIOFOCUS_GAIN;
                    //int requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                    //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                    //int result = audioManager.requestAudioFocus(this, usedRingingStream, requestType);
                    //if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        ringingMediaPlayer = new MediaPlayer();

                        if (stream == AudioManager.STREAM_RING) {
                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            ringingMediaPlayer.setAudioAttributes(attrs);
                        }
                        else {
                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            ringingMediaPlayer.setAudioAttributes(attrs);
                        }
                        //ringingMediaPlayer.setAudioStreamType(stream);

                        ringingMediaPlayer.setDataSource(this, Uri.parse(ringtone));
                        ringingMediaPlayer.prepare();
                        ringingMediaPlayer.setLooping(true);

                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingVolume=" + ringingVolume);

                        int maximumRingValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) ringingVolume / maximumRingValue * 100.0f;
                        int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "mediaRingingVolume=" + mediaRingingVolume);

                        /*if (android.os.Build.VERSION.SDK_INT >= 23)
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                        else
                            audioManager.setStreamMute(AudioManager.STREAM_RING, true);*/
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, 0);

                        ringingMediaPlayer.start();

                        ringingCallIsSimulating = true;
                        //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringing played");
                    //} else
                    //    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "focus not granted");
                }
/*                catch (SecurityException e) {
                    //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", " security exception");
                    ringingMediaPlayer = null;

                    OneTimeWorkRequest disableInternalChangeWorker =
                            new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                    .addTag("disableInternalChangeWork")
                                    .setInitialDelay(3, TimeUnit.SECONDS)
                                    .build();
                    try {
                        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                        workManager.cancelUniqueWork("disableInternalChangeWork");
                        workManager.cancelAllWorkByTag("disableInternalChangeWork");
                        workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                    } catch (Exception ignored) {}

//                    PPApplication.startHandlerThreadInternalChangeToFalse();
//                    final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
//                            RingerModeChangeReceiver.internalChange = false;
//                        }
//                    }, 3000);
                    //PostDelayedBroadcastReceiver.setAlarm(
                    //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);

                    Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                }*/
                catch (Exception e) {
                    //PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", Log.getStackTraceString(e));
                    ringingMediaPlayer = null;

                    OneTimeWorkRequest disableInternalChangeWorker =
                            new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                    .addTag("disableInternalChangeWork")
                                    .setInitialDelay(3, TimeUnit.SECONDS)
                                    .build();
                    try {
                        WorkManager workManager = PPApplication.getWorkManagerInstance(getApplicationContext());
                        workManager.cancelUniqueWork("disableInternalChangeWork");
                        workManager.cancelAllWorkByTag("disableInternalChangeWork");
                        workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                    } catch (Exception ee) {
                        PPApplication.recordException(ee);
                    }

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
                    //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                    Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                }
            }
        }
    }

    public void stopSimulatingRingingCall(/*boolean abandonFocus*/) {
        //if (ringingCallIsSimulating) {
            //PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "xxx");
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

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
                    if (ringingCallIsSimulating)
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldMediaVolume, 0);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                //PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "ringing stopped");
            }
            /*if (abandonFocus) {
                if (audioManager != null)
                    audioManager.abandonAudioFocus(this);
            }*/
        //}
        ringingCallIsSimulating = false;

        OneTimeWorkRequest disableInternalChangeWorker =
                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                        .addTag("disableInternalChangeWork")
                        .setInitialDelay(3, TimeUnit.SECONDS)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(getApplicationContext());
            workManager.cancelUniqueWork("disableInternalChangeWork");
            workManager.cancelAllWorkByTag("disableInternalChangeWork");
            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

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
        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
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
                RingerModeChangeReceiver.removeAlarm(this);
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
                    //int result = audioManager.requestAudioFocus(this, usedNotificationStream, requestType);
                    //if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        notificationMediaPlayer = new MediaPlayer();

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build();
                        notificationMediaPlayer.setAudioAttributes(attrs);
                        //notificationMediaPlayer.setAudioStreamType(usedNotificationStream);

                        notificationMediaPlayer.setDataSource(this, Uri.parse(notificationTone));
                        notificationMediaPlayer.prepare();
                        notificationMediaPlayer.setLooping(false);


                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "notificationVolume=" + notificationVolume);

                        int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                        mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "mediaNotificationVolume=" + mediaNotificationVolume);

                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaNotificationVolume, 0);

                        notificationMediaPlayer.start();

                        notificationToneIsSimulating = true;
                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "notification played");

                        final Context context = this;
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
                    Permissions.grantPlayRingtoneNotificationPermissions(this, true, false);
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(this);
                } catch (Exception e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "exception");
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(this);
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

            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldMediaVolume, 0);
            PPApplication.logE("PhoneProfilesService.stopSimulatingNotificationTone", "notification stopped");
        }
        //if (abandonFocus)
        //    audioManager.abandonAudioFocus(this);
        //}
        notificationToneIsSimulating = false;
        RingerModeChangeReceiver.setAlarmForDisableInternalChange(this);
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
            audioManager.abandonAudioFocus(this);
        }
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
            }
            //if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
            //    if (usedNotificationStream == AudioManager.STREAM_ALARM)
            //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
            //}
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_GAIN");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, 0);
                if (!ringingMediaPlayer.isPlaying())
                    ringingMediaPlayer.start();
            }
            //if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
            //    if (usedNotificationStream == AudioManager.STREAM_ALARM)
            //        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaNotificationVolume, 0);
            //    if (!notificationMediaPlayer.isPlaying())
            //        notificationMediaPlayer.start();
            //}
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS");
            stopSimulatingRingingCall(false);
            //stopSimulatingNotificationTone(false);
            audioManager.abandonAudioFocus(this);
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
            notificationIsPlayed = false;
            notificationMediaPlayer = null;
        }
    }

    public void playNotificationSound (final String notificationSound, final boolean notificationVibrate) {
        if (notificationVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(500);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }

        //PPApplication.logE("PhoneProfilesService.playNotificationSound", "ringingCallIsSimulating="+ringingCallIsSimulating);
        if ((!ringingCallIsSimulating)/* && (!notificationToneIsSimulating)*/) {

            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            stopPlayNotificationSound();

            //PPApplication.logE("PhoneProfilesService.playNotificationSound", "notificationSound="+notificationSound);
            if (!notificationSound.isEmpty())
            {
                //int ringerMode = ApplicationPreferences.prefRingerMode;
                //int zenMode = ApplicationPreferences.prefZenMode;
                //boolean isAudible = ActivateProfileHelper.isAudibleRinging(ringerMode, zenMode/*, false*/);
                boolean isAudible = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, this);
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "isAudible="+isAudible);
                if (isAudible) {

                    Uri notificationUri = Uri.parse(notificationSound);

                    try {
                        RingerModeChangeReceiver.internalChange = true;

                        notificationMediaPlayer = new MediaPlayer();

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        notificationMediaPlayer.setAudioAttributes(attrs);
                        //notificationMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                        notificationMediaPlayer.setDataSource(this, notificationUri);
                        notificationMediaPlayer.prepare();
                        notificationMediaPlayer.setLooping(false);

                        /*
                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        int notificationVolume = ActivateProfileHelper.getNotificationVolume(this);

                        PPApplication.logE("PhoneProfilesService.playNotificationSound", "notificationVolume=" + notificationVolume);

                        int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                        int mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.playNotificationSound", "mediaNotificationVolume=" + mediaNotificationVolume);

                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaNotificationVolume, 0);
                        */

                        notificationMediaPlayer.start();

                        notificationIsPlayed = true;

                        //final Context context = this;
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

                                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
                                    //PPApplication.logE("PhoneProfilesService.playNotificationSound", "notification stopped");
                                }

                                notificationIsPlayed = false;
                                notificationMediaPlayer = null;

                                OneTimeWorkRequest disableInternalChangeWorker =
                                        new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                                .addTag("disableInternalChangeWork")
                                                .setInitialDelay(3, TimeUnit.SECONDS)
                                                .build();
                                try {
                                    WorkManager workManager = PPApplication.getWorkManagerInstance(getApplicationContext());
                                    workManager.cancelUniqueWork("disableInternalChangeWork");
                                    workManager.cancelAllWorkByTag("disableInternalChangeWork");
                                    workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }

                                /*PPApplication.startHandlerThreadInternalChangeToFalse();
                                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);*/
                                //PostDelayedBroadcastReceiver.setAlarm(
                                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, context);

                                notificationPlayTimer = null;
                            }
                        }, notificationMediaPlayer.getDuration());

                    }
/*                    catch (SecurityException e) {
                        //PPApplication.logE("PhoneProfilesService.playNotificationSound", "security exception");
                        stopPlayNotificationSound();

                        OneTimeWorkRequest disableInternalChangeWorker =
                                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                        .addTag("disableInternalChangeWork")
                                        .setInitialDelay(3, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                            workManager.cancelUniqueWork("disableInternalChangeWork");
                            workManager.cancelAllWorkByTag("disableInternalChangeWork");
                            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                        } catch (Exception ignored) {}

//                        PPApplication.startHandlerThreadInternalChangeToFalse();
//                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                RingerModeChangeReceiver.internalChange = false;
//                            }
//                        }, 3000);
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                        Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                    }
*/
                    catch (Exception e) {
                        //PPApplication.logE("PhoneProfilesService.playNotificationSound", "exception");
                        stopPlayNotificationSound();

                        OneTimeWorkRequest disableInternalChangeWorker =
                                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                                        .addTag("disableInternalChangeWork")
                                        .setInitialDelay(3, TimeUnit.SECONDS)
                                        .build();
                        try {
                            WorkManager workManager = PPApplication.getWorkManagerInstance(getApplicationContext());
                            workManager.cancelUniqueWork("disableInternalChangeWork");
                            workManager.cancelAllWorkByTag("disableInternalChangeWork");
                            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                        } catch (Exception ee) {
                            PPApplication.recordException(ee);
                        }

                        /*PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);*/
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                        Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                    }
                }
            }
        }
    }

    //---------------------------

    /*
    DO NOT CALL THIS !!! THIS IS CALLED ALSO WHEN, FOR EXAMPLE, ACTIVATOR GETS DISPLAYED !!!
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxx");

        //if (PPApplication.screenTimeoutHandler != null) {
        //    PPApplication.screenTimeoutHandler.post(new Runnable() {
        //        public void run() {
                    //ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
                    PPApplication.logE("******** PhoneProfilesService.onTaskRemoved", "remove wakelock");
                    ActivateProfileHelper.removeKeepScreenOnView();
        //        }
        //    });
        //}

        super.onTaskRemoved(rootIntent);
    }
    */
}
