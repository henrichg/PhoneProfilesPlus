package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rikka.shizuku.Shizuku;

/** @noinspection ExtractMethodRecommender*/
public class PhoneProfilesService extends Service
{
    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    //private static boolean isInForeground = false;

    // must be in PPService !!!
    //static boolean startForegroundNotification = true;

    static final String ACTION_COMMAND = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_COMMAND";
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
    //static final String ACTION_RESTART_EVENTS_WITH_DELAY_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".RestartEventsWithDelayBroadcastReceiver";
    static final String ACTION_ACTIVATED_PROFILE_EVENT_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ActivatedProfileEventBroadcastReceiver";
    static final String ACTION_REFRESH_ACTIVITIES_GUI_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver";
    static final String ACTION_DASH_CLOCK_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver";
    static final String ACTION_BLUETOOTHLE_SCAN_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver";
    static final String ACTION_APPLICATION_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ApplicationEventEndBroadcastReceiver";
    static final String ACTION_CALL_CONTROL_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".CallControlEventEndBroadcastReceiver";
    static final String ACTION_ANSWER_CALL_RINGING_LENGTH_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".AnswerCallRingingLengthBroadcastReceiver";
    static final String ACTION_END_CALL_CALL_LENGTH_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EndCallCallLengthBroadcastReceiver";
    static final String ACTION_ACTIVATED_PROFILE_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ActivatedProfileEventEndBroadcastReceiver";

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
    //static final String EXTRA_OLD_RINGTONE = "old_ringtone";
    //static final String EXTRA_OLD_RINGTONE_SIM1 = "old_ringtone_sim1";
    //static final String EXTRA_OLD_RINGTONE_SIM2 = "old_ringtone_sim2";

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
    static final String EXTRA_UNREGISTER_PHONE_CALLS_LISTENER = "unregister_phone_calls_listener";
    static final String EXTRA_FROM_BATTERY_CHANGE = "from_battery_change";
    //static final String EXTRA_START_LOCATION_UPDATES = "start_location_updates";
    //private static final String EXTRA_STOP_LOCATION_UPDATES = "stop_location_updates";
    //static final String EXTRA_RESTART_EVENTS = "restart_events";
    //static final String EXTRA_ALSO_RESCAN = "also_rescan";
    //static final String EXTRA_UNBLOCK_EVENTS_RUN = "unblock_events_run";
    //static final String EXTRA_REACTIVATE_PROFILE = "reactivate_profile";
    static final String EXTRA_MANUAL_RESTART = "manual_restart";
    //static final String EXTRA_LOG_TYPE = "log_type";
    //static final String EXTRA_DELAYED_WORK = "delayed_work";
    static final String EXTRA_SENSOR_TYPE = "sensor_type";
    //static final String EXTRA_ELAPSED_ALARMS_WORK = "elapsed_alarms_work";
    //static final String EXTRA_FROM_DO_FIRST_START = "from_do_first_start";
    static final String EXTRA_START_FOR_EXTERNAL_APPLICATION = "start_for_external_application";
    static final String EXTRA_START_FOR_EXTERNAL_APP_ACTION = "start_for_external_app_action";
    static final String EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE = "start_for_external_app_data_type";
    static final String EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE = "start_for_external_app_data_value";
    static final String EXTRA_RESCAN_SCANNERS = "rescan_scanners";
    //static final String EXTRA_STOP_SIMULATING_RINGING_CALL = "stop_simulating_ringing_call";
    //static final String EXTRA_STOP_SIMULATING_RINGING_CALL_NO_DISABLE_INTERNAL_CHANGE = "stop_simulating_ringing_call_no_disable_internal_change";
    static final String EXTRA_REGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER = "register_ppp_extender_for_sms_call_receivers";
    static final String EXTRA_UNREGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER = "unregister_ppp_extender_for_sms_call_receivers";
    static final String EXTRA_REGISTER_RECEIVERS_FOR_CALL_SENSOR = "register_receivers_for_call_sensor";
    static final String EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SENSOR = "unregister_receivers_for_call_sensor";
    static final String EXTRA_REGISTER_RECEIVERS_FOR_SMS_SENSOR = "register_receivers_for_sms_sensor";
    static final String EXTRA_UNREGISTER_RECEIVERS_FOR_SMS_SENSOR = "unregister_receivers_for_sms_sensor";
    static final String EXTRA_DISABLE_NOT_USED_SCANNERS = "disable_not_used_scanners";
    //static final String EXTRA_REGISTER_RECEIVERS_FOR_CALL_CONTROL_SENSOR = "register_receivers_for_call_control_sensor";
    //static final String EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_CONTROL_SENSOR = "unregister_receivers_for_call_control_sensor";

    static final String EXTRA_START_FOR_SHIZUKU_START = "start_for_shizuku_start";

    //static final String EXTRA_SHOW_TOAST = "show_toast";

    static final int START_FOR_EXTERNAL_APP_PROFILE = 1;
    static final int START_FOR_EXTERNAL_APP_EVENT = 2;

    //--------------------------

    private final BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[IN_BROADCAST] PhoneProfilesService.commandReceiver", "xxx");
            PhoneProfilesServiceStatic.doCommand(intent, context);
        }
    };

    private final Shizuku.OnBinderReceivedListener BINDER_RECEIVED_LISTENER = () -> {
        if (!Shizuku.isPreV11()) {
            PPApplicationStatic.logE("PhoneProfilesService.BINDER_RECEIVED_LISTENER", "*** Shizuku started ***");

            PPApplication.shizukuBinded = true;

            RootUtils.initRoot();

            /*boolean exists = */RootUtils.settingsBinaryExists(false);
            //Log.e("PhoneProfilesService.BINDER_RECEIVED_LISTENER", "settings exists="+exists);
            /*exists = */RootUtils.serviceBinaryExists(false);
            //Log.e("PhoneProfilesService.BINDER_RECEIVED_LISTENER", "service exists="+exists);
            //noinspection Convert2MethodRef
            RootUtils.getServicesList();
            ApplicationPreferences.applicationHyperOsWifiBluetoothDialogs(getApplicationContext());
            Permissions.setHyperOSWifiBluetoothDialogAppOp();

            // do activate profile/restart events aso for first start
            Data workData = new Data.Builder()
                    .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true)

                    .putBoolean(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, false)
                    .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, "")
                    .putInt(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, 0)
                    .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, "")

                    .putBoolean(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, true)

                    //.putBoolean(PhoneProfilesService.EXTRA_SHOW_TOAST, serviceIntent != null)
                    .build();

//            PPApplicationStatic.logE("[MAIN_WORKER_CALL] PhoneProfilesService.doForFirstStart", "xxxxxxxxxxxxxxxxxxxx");

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(PPApplication.AFTER_SHIZUKU_START_WORK_TAG)
                            .setInputData(workData)

                            .setInitialDelay(10, TimeUnit.SECONDS)

                            .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                                        //if (PPApplicationStatic.logEnabled()) {
//                                        ListenableFuture<List<WorkInfo>> statuses;
//                                        statuses = workManager.getWorkInfosForUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG);
//                                        try {
//                                            List<WorkInfo> workInfoList = statuses.get();
//                                        } catch (Exception ignored) {
//                                        }
//                                        //}

//                      PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doFirstStart", "keepResultsForAtLeast");
                    //workManager.enqueue(worker);
                    // !!! MUST BE APPEND_OR_REPLACE FOR EXTRA_START_FOR_EXTERNAL_APPLICATION !!!
                    workManager.enqueueUniqueWork(PPApplication.AFTER_SHIZUKU_START_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

        }
    };

    //--------------------------

    //public static SipManager mSipManager = null;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();

        PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "android.os.Build.VERSION.SDK_INT=" + android.os.Build.VERSION.SDK_INT);

//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.onCreate", "PPApplication.phoneProfilesServiceMutex");
        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = this;
        }
        PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "OLD serviceHasFirstStart=" + serviceHasFirstStart);
        serviceHasFirstStart = false;

        //PPApplication.accessibilityServiceForPPPExtenderConnected = 2;

        //startForegroundNotification = true;
        //isInForeground = false;

        if (PPApplication.getInstance() == null) {
            PPApplicationStatic.loadApplicationPreferences(getApplicationContext());
        }

        PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "before show profile notification");

        //boolean isServiceRunning = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        //PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "------- service is running (in foreground)="+isServiceRunning);

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
//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesService.onCreate", "call of PPAppNotification.showNotification");

        PPAppNotification.showNotification(getApplicationContext(), true, false, true, true);

        PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "after show profile notification");

        if (PPApplication.getInstance() == null) {
            PPApplicationStatic.loadGlobalApplicationData(getApplicationContext());
            PPApplicationStatic.loadProfileActivationData(getApplicationContext());

            PPApplication.sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            PPApplication.accelerometerSensor = PPApplicationStatic.getAccelerometerSensor(getApplicationContext());
            PPApplication.magneticFieldSensor = PPApplicationStatic.getMagneticFieldSensor(getApplicationContext());
            PPApplication.proximitySensor = PPApplicationStatic.getProximitySensor(getApplicationContext());
            PPApplication.lightSensor = PPApplicationStatic.getLightSensor(getApplicationContext());
            PPApplicationStatic.startHandlerThreadOrientationScanner();
        }

        //serviceRunning = false;
        //runningInForeground = false;
        PPApplication.applicationFullyStarted = false;
        PPApplication.normalServiceStart = false;
        PPApplication.showToastForProfileActivation = false;
        //ApplicationPreferences.forceNotUseAlarmClock = false;

        final Context appContext = getApplicationContext();

        Shizuku.addBinderReceivedListenerSticky(BINDER_RECEIVED_LISTENER);
        LocalBroadcastManager.getInstance(appContext).registerReceiver(commandReceiver, new IntentFilter(ACTION_COMMAND));

        int downoladReceiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            downoladReceiverFlags = RECEIVER_EXPORTED;  // !!! it must be exported for DownloadManager
        appContext.registerReceiver(PPApplication.downloadCompletedBroadcastReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), downoladReceiverFlags);

        /*
        if (Build.VERSION.SDK_INT < 31) {
            IntentFilter intentFilter5 = new IntentFilter();
            intentFilter5.addAction(PPAppNotification.ACTION_START_LAUNCHER_FROM_NOTIFICATION);
            int receiverFlags = 0;
            //if (Build.VERSION.SDK_INT >= 34)
            //    receiverFlags = RECEIVER_NOT_EXPORTED;
            appContext.registerReceiver(PPApplication.startLauncherFromNotificationReceiver, intentFilter5, receiverFlags);
        }
        */

        //appContext.registerReceiver(PPApplication.showProfileNotificationBroadcastReceiver, new IntentFilter(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION));
        //appContext.registerReceiver(PPApplication.updateGUIBroadcastReceiver, new IntentFilter(PPApplication.ACTION_UPDATE_GUI));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.refreshActivitiesBroadcastReceiver,
                new IntentFilter(ACTION_REFRESH_ACTIVITIES_GUI_BROADCAST_RECEIVER));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.dashClockBroadcastReceiver,
                new IntentFilter(ACTION_DASH_CLOCK_BROADCAST_RECEIVER));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.iconWidgetBroadcastReceiver,
                new IntentFilter(IconWidgetProvider.ACTION_REFRESH_ICONWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.oneRowWidgetBroadcastReceiver,
                new IntentFilter(OneRowWidgetProvider.ACTION_REFRESH_ONEROWWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.listWidgetBroadcastReceiver,
                new IntentFilter(ProfileListWidgetProvider.ACTION_REFRESH_LISTWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.panelWidgetBroadcastReceiver,
                new IntentFilter(PanelWidgetProvider.ACTION_REFRESH_PANELWIDGET));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.oneRowProfileListWidgetBroadcastReceiver,
                new IntentFilter(OneRowProfileListWidgetProvider.ACTION_REFRESH_ONEROWPROFILELISTWIDGET));

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
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, ApplicationPreferences.applicationEventPeriodicScanningEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, ApplicationPreferences.applicationEventPeriodicScanningScanInterval);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventNotificationEnableScanning);
            PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventNotificationScanInterval);
        //} catch (Exception e) {
            // https://github.com/firebase/firebase-android-sdk/issues/1226
            //PPApplicationStatic.recordException(e);
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

        //ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        //willBeDoRestartEvents = false;

        PPApplicationStatic.logE("$$$ PhoneProfilesService.onCreate", "OK created");

        //PPApplication.startTimeOfApplicationStart = Calendar.getInstance().getTimeInMillis();

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] PhoneProfilesService.onCreate", "xxxxxxxxxxxxxxxxxxxx");

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.APPLICATION_FULLY_STARTED_WORK_TAG)
                        .setInitialDelay(PPApplication.APPLICATION_START_DELAY, TimeUnit.MILLISECONDS)
                        .build();
        try {
            if (PPApplicationStatic.getApplicationStarted(true, false)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
//                    PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.onCreate", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.APPLICATION_FULLY_STARTED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
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

        PPApplicationStatic.logE("PhoneProfilesService.onDestroy", "xxx");

        Context appContext = getApplicationContext();

        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(commandReceiver);
        } catch (Exception ignored) {}

        PlayRingingNotification.stopSimulatingRingingCall(/*true*/true, getApplicationContext());
        //PhoneProfilesServiceStatic.stopSimulatingNotificationTone(true);
        PlayRingingNotification.stopPlayNotificationSound(false, appContext);

        GlobalUtils.reenableKeyguard(getApplicationContext());

        PhoneProfilesServiceStatic.registerAllTheTimeRequiredPPPBroadcastReceivers(false, appContext);
        PhoneProfilesServiceStatic.registerAllTheTimeRequiredSystemReceivers(false, appContext);
        PhoneProfilesServiceStatic.registerAllTheTimeContentObservers(false, appContext);
        PhoneProfilesServiceStatic.registerAllTheTimeCallbacks(false, appContext);
        PhoneProfilesServiceStatic.registerPPPExtenderReceiver(false, null, appContext);
        PhoneProfilesServiceStatic.unregisterEventsReceiversAndWorkers(true, appContext);

        try {
            appContext.unregisterReceiver(PPApplication.downloadCompletedBroadcastReceiver);
        } catch (Exception ignored) {}

        /*
        if (Build.VERSION.SDK_INT < 31) {
            try {
                appContext.unregisterReceiver(PPApplication.startLauncherFromNotificationReceiver);
            } catch (Exception ignored) {}
        }
        */
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.refreshActivitiesBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.dashClockBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.iconWidgetBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.oneRowWidgetBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.listWidgetBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.panelWidgetBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.oneRowProfileListWidgetBroadcastReceiver);
        } catch (Exception ignored) {}

        RootUtils.initRoot();

        //ShowProfileNotificationBroadcastReceiver.removeAlarm(appContext);
        try {
            //isInForeground = false;
            stopForeground(true);

            //PPApplicationStatic.cancelWork(ShowProfileNotificationWorker.WORK_TAG, true);
            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                try {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.onDestroy", "(1) PPApplication.showPPPNotificationMutex");
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                    }
                } catch (Exception ignored) {}
                try {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.onDestroy", "(2) PPApplication.showPPPNotificationMutex");
                    synchronized (PPApplication.showPPPNotificationMutex) {
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_NATIVE_ID);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            //Log.e("PhoneProfilesService.onDestroy", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }

//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.onDestroy", "PPApplication.phoneProfilesServiceMutex");
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

    public static void stop(boolean shutdown, Context context) {
        if (instance != null) {
            try {
                PPApplicationStatic.setApplicationStopping(context, true);
                if (!shutdown) {
                    // this avoid generating exception:
                    //   Context.startForegroundService() did not then call Service.startForeground()
                    //https://stackoverflow.com/a/72754189/12228079
                    GlobalUtils.sleep(5000);
                }
                if (instance != null)
                    // may be null after 5 seconds sleep
                    instance.stopSelf();
            } catch (Exception e) {
                //Log.e("PhoneProfilesService.stop", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                PPApplicationStatic.setApplicationStopping(context, false);
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

    // start service for first start
    private void doForFirstStart(final Intent serviceIntent/*, int flags, int startId*/) {
        PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        final Context appContext = getApplicationContext();

        PPApplicationStatic.setApplicationStopping(getApplicationContext(), false);
        //final boolean oldServiceHasFirstStart = PPApplication.serviceHasFirstStart;
        serviceHasFirstStart = true;
        PPApplicationStatic.setApplicationStarted(getApplicationContext(), true);

        boolean applicationStart = false;
        //boolean deactivateProfile = false;
        boolean activateProfiles = false;
        boolean deviceBoot = false;
        //boolean startOnPackageReplace = false;
        boolean startFromExternalApplication = false;
        String startForExternalAppAction = "";
        int startForExternalAppDataType = 0;
        String startForExternalAppDataValue = "";

        //final Intent serviceIntent = intent;

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
            PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_SYSTEM_RESTART, null, null, "");
        }

        if (PPApplicationStatic.logEnabled()) {
            PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEVICE_BOOT="+deviceBoot);
            PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_APPLICATION_START="+applicationStart);
            //if (startOnPackageReplace)
            //    PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");
            //if (deactivateProfile)
            //    PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEACTIVATE_PROFILE");
            PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_ACTIVATE_PROFILES="+activateProfiles);
            PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APPLICATION="+startFromExternalApplication);
            if (startFromExternalApplication) {
                PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_ACTION="+startForExternalAppAction);
                PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE="+startForExternalAppDataType);
                PPApplicationStatic.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE="+startForExternalAppDataValue);
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

        Runnable runnable = () -> {
            PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart START");

            //Context appContext= appContextWeakRef.get();

            //if (appContext == null)
            //    return;

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PhoneProfilesService_doForFirstStart);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                // is needed beacuse will be changed
                boolean __activateProfiles = _activateProfiles;
                boolean __applicationStart = _applicationStart;

//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.doForFirstStart");

            /*
            // create application directory
            // not working because this requires Storage permission
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory())) {
                boolean created = exportDir.mkdirs();
                try {
                    exportDir.setReadable(true, false);
                } catch (Exception ee) {
                    PPApplicationStatic.recordException(ee);
                }
                try {
                    exportDir.setWritable(true, false);
                } catch (Exception ee) {
                    PPApplicationStatic.recordException(ee);
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
                    ApplicationPreferences.applicationNeverAskForGrantRoot(appContext);
                }*/
                if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                    // grant root
                    RootUtils.isRootGranted();
                } else {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.doForFirstStart", "PPApplication.rootMutex");
                    synchronized (PPApplication.rootMutex) {
                        if (PPApplication.rootMutex.rootChecked) {
                            try {
                                PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                                if (PPApplication.rootMutex.rooted) {
                                    PackageManager packageManager = appContext.getPackageManager();
                                    // SuperSU
                                    Intent _intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                                    if (_intent != null)
                                        PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                                    else {
                                        _intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                                        if (_intent != null)
                                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                                        else
                                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                                    }
                                }
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        } else {
                            //try {
                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, "ask for grant disabled");
                            //} catch (Exception e) {
                            //    PPApplicationStatic.recordException(e);
                            //}
                        }
                    }
                }

                //PPApplication.getSUVersion();
                RootUtils.settingsBinaryExists(false);
                RootUtils.serviceBinaryExists(false);
                RootUtils.getServicesList();
                ApplicationPreferences.applicationHyperOsWifiBluetoothDialogs(getApplicationContext());
                //Permissions.setHyperOSWifiBluetoothDialogAppOp();

                //PhoneProfilesService ppService = PhoneProfilesService.getInstance();
                int savedVersionCode = PPApplicationStatic.getSavedVersionCode(appContext);
                PPApplication.firstStartAfterInstallation = savedVersionCode == 0;

                boolean applicationJustInstalled = PPApplication.firstStartAfterInstallation; //PPApplicationStatic.getSavedVersionCode(appContext) == 0;
                // doForPackageReplaced() save actual version code
                boolean newVersion = doForPackageReplaced(appContext, savedVersionCode);
                if (newVersion) {
                    __activateProfiles = true;
                    __applicationStart = true;
                }

                if (PPApplicationStatic.logEnabled()) {
                    PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "__applicationStart=" + __applicationStart);
                    PPApplicationStatic.logE("PhoneProflesService.doForFirstStart - handler", "__activateProfiles=" + __activateProfiles);
                }

                // TODO remove for release
                /*if (PPApplicationStatic.logEnabled()) {
                    // get list of TRANSACTIONS
                    Object serviceManager = RootUtils.getServiceManager("isub");
                    if (serviceManager != null) {
                        // only log it
                        RootUtils.getTransactionCode(String.valueOf(serviceManager), "");
                    }
                }*/

                PPApplicationStatic.setBlockProfileEventActions(true);

                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                dataWrapper.fillProfileList(false, false);
                dataWrapper.fillEventList();

                // generate predefined profiles and events, for first PPP start (version code is not saved)
                if (PPApplication.firstStartAfterInstallation) {
                    if (dataWrapper.profileList.isEmpty()) {
                        dataWrapper.fillPredefinedProfileList(false, false, appContext);
                    }
                    if (dataWrapper.eventList.isEmpty())
                    {
                        dataWrapper.generatePredefinedEventList(appContext);
                    }
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                        int actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
                        PPApplicationStatic.setSavedVersionCode(appContext, actualVersionCode);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }

                for (Profile profile : dataWrapper.profileList)
                    profile.isAccessibilityServiceEnabled(appContext, true);

                for (Event event : dataWrapper.eventList)
                    event.isAccessibilityServiceEnabled(appContext, true, true);
                //PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(appContext, true, false);

                //GlobalGUIRoutines.setLanguage(appContext);
                GlobalGUIRoutines.switchNightMode(appContext, true);

                DataWrapperStatic.setDynamicLauncherShortcuts(appContext, false);

                PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "application not started, start it");

                //Permissions.clearMergedPermissions(appContext);

                //if (!TonesHandler.isToneInstalled(/*TonesHandler.TONE_ID,*/ appContext))
                //    TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext);
                //DatabaseHandler.getInstance(appContext).fixPhoneProfilesSilentInProfiles();
                DatabaseHandler.getInstance(appContext).disableNotAllowedPreferences();

                //TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);
                ActivateProfileHelper.setMergedRingNotificationVolumes(appContext/*, true*/);

                ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                if (ApplicationPreferences.keepScreenOnPermanent)
                    ActivateProfileHelper.createKeepScreenOnView(appContext);

                AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    RingerModeChangeReceiver.setRingerMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    //PPNotificationListenerService.setZenMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager/*, "PhoneProfilesService.doFirstStart"*/);
                    try {
                        ActivateProfileHelper.setNotificationVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                    } catch (Exception e10) {
                        PPApplicationStatic.recordException(e10);
                    }
                    try {
                        ActivateProfileHelper.setRingerVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_RING));
                    } catch (Exception e10) {
                        PPApplicationStatic.recordException(e10);
                    }
                }

                PPExtenderBroadcastReceiver.setApplicationInForeground(appContext, "");

                EventPreferencesCall.setEventCallEventType(appContext, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                EventPreferencesCall.setEventCallEventTime(appContext, 0, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                EventPreferencesCall.setEventCallPhoneNumber(appContext, "");
                EventPreferencesCall.setEventCallFromSIMSlot(appContext, 0, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);

                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 0, false, false);
                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 1, false, false);
                EventPreferencesRoaming.setEventRoamingInSIMSlot(appContext, 2, false, false);

                //EventPreferencesCallControl.setEventCallControlActive(appContext, false);
                EventPreferencesCallControl.setEventCallControlTime(appContext, 0);
                EventPreferencesCallControl.setEventCallControlPhoneNumber(appContext, "");
                EventPreferencesCallControl.setEventCallControlCallDirection(appContext, 0);

                // set alarm for Alarm clock sensor from last saved time in
                // NextAlarmClockBroadcastReceiver.onReceived()

                // convert old saved alarm clock to new format
                long prefEventAlarmClockTime = ApplicationPreferences.
                        getSharedPreferences(appContext).getLong("eventAlarmClockTime", 0L);
                String prefEventAlarmClockPackageName = ApplicationPreferences.
                        getSharedPreferences(appContext).getString("eventAlarmClockPackageName", "");
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                editor.remove("eventAlarmClockTime");
                editor.remove("eventAlarmClockPackageName");
                editor.apply();
                NextAlarmClockBroadcastReceiver.setEventAlarmClockTime(prefEventAlarmClockPackageName, prefEventAlarmClockTime, appContext);

                //AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
                //if (alarmManager != null) {
                    List<NextAlarmClockData> times = NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes(appContext);
                    if (times != null) {
                        for (NextAlarmClockData _time : times) {
//                            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
//                            String ___time = sdf.format(_time.time);
//                            PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "next alarm clock alarm time="+___time);
//                            PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "next alarm clock package name=" + _time.packageName);

                            NextAlarmClockBroadcastReceiver.setAlarm(
                                    _time.time,
                                    _time.packageName,
                                    appContext);

                        }
                    }
                //}

                // show info notification
                ImportantInfoNotification.showInfoNotification(appContext);

                // do not show it at start of PPP, will be shown for each profile activation.
                //DrawOverAppsPermissionNotification.showNotification(appContext, false);
                //IgnoreBatteryOptimizationNotification.showNotification(appContext, false);

                //if (serviceIntent != null) {
                // it is not restart of service
                // From documentation: This may be null if the service is being restarted after its process has gone away
                if (__applicationStart) {
                    PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "--- initialization for application start");

                    for (Profile profile : dataWrapper.profileList)
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);
                    //Profile.setActivatedProfileForDuration(appContext, 0);
                    //Profile profile = DataWrapper.getNonInitializedProfile(
                    //        getString(R.string.empty_string), Profile.PROFILE_ICON_DEFAULT, 0);
                    //Profile.saveProfileToSharedPreferences(profile, appContext);

                    // DO NOT UNBLOCK EVENTS. AT START MUST MANUALLY ACTIVATED PROFILE, IF WAS ACTIVATED BEFORE PPService start
                    //Event.setEventsBlocked(appContext, false);
                    //synchronized (dataWrapper.eventList) {
                    //    for (Iterator<Event> it = dataWrapper.eventList.iterator(); it.hasNext(); ) {
                    //        Event event = it.next();
                    //        if (event != null)
                    //            event._blocked = false;
                    //    }
                    //}
                    //DatabaseHandler.getInstance(appContext).unblockAllEvents();
                    //Event.setForceRunEventRunning(appContext, false);

//                    PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesService.doForFirstStart", "PPApplication.profileActivationMutex");
                    synchronized (PPApplication.profileActivationMutex) {
                        List<String> activateProfilesFIFO = new ArrayList<>();
                        dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                    }

                    if (PPApplication.prefLastActivatedProfile != 0) {
                        dataWrapper.fifoAddProfile(PPApplication.prefLastActivatedProfile, 0);
                    }
                }

                for (Event event : dataWrapper.eventList)
                    StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(appContext);
                LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                //PPNotificationListenerService.clearNotifiedPackages(appContext);

                DatabaseHandler.getInstance(appContext).deleteAllEventTimelines();
                DatabaseHandler.getInstance(appContext).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

                //if (_startOnPackageReplace) {
                //    MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, true);
                //}
                //else
                MobileCellsScanner.startAutoRegistration(appContext, true);

                WifiScanWorker.setScanRequest(appContext, false);
                WifiScanWorker.setWaitForResults(appContext, false);
                WifiScanWorker.setWifiEnabledForScan(appContext, false);

                BluetoothScanWorker.setScanRequest(appContext, false);
                BluetoothScanWorker.setLEScanRequest(appContext, false);
                BluetoothScanWorker.setWaitForResults(appContext, false);
                BluetoothScanWorker.setWaitForLEResults(appContext, false);
                BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
                BluetoothScanWorker.setScanKilled(appContext, false);

                // !!! registerReceiversAndWorkers moved into MainWorker.doAfterFirstStart
                // in it is not PPP broadcasts registration
                PhoneProfilesServiceStatic.registerAllTheTimeRequiredPPPBroadcastReceivers(true, appContext);

                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(connectedDevices/*appContext, true*/);
                // this also clears shared preferences
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
//                Log.e("PhoneProfilesService.doForFirstStart", "**** START of getConnectedDevices");
                BluetoothConnectedDevicesDetector.getConnectedDevices(appContext, false);

                PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "start donation and check GitHub releases alarms");
                DonationBroadcastReceiver.setAlarm(appContext);
                CheckPPPReleasesBroadcastReceiver.setAlarm(appContext);
                CheckCriticalPPPReleasesBroadcastReceiver.setAlarm(appContext);
                CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm(appContext);
                CheckLatestPPPPSReleasesBroadcastReceiver.setAlarm(appContext);

                PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "application started");

                if ((!_deviceBoot) && (_applicationStart)) {
                    PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START, null, null, "");
                    if (newVersion) {
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            String version = pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
                            if (applicationJustInstalled)
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_INSTALLATION, version, null, "");
                            else
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_UPGRADE, version, null, "");
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }

                // start events

                if (__activateProfiles) {
                    editor = ApplicationPreferences.getEditor(appContext);
                    if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventWifiEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventBluetoothEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventBluetoothEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventMobileCellEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventOrientationEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventNotificationEnableScanning = true;
                    }
                    if (ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, true);
                        ApplicationPreferences.applicationEventPeriodicScanningEnableScanning = true;
                    }
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE, false);
                    editor.apply();
                    ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(appContext);
                    ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(appContext);
                }

                // !! must be after PPApplication.loadApplicationPreferences()
                if (ApplicationPreferences.notificationProfileListDisplayNotification)
                    ProfileListNotification.enable(false, appContext);
                else
                    ProfileListNotification.disable(appContext);

                //boolean packageReplaced = PPApplication.applicationPackageReplaced; //ApplicationPreferences.applicationPackageReplaced(appContext);
                //if (!packageReplaced) {
                //setApplicationFullyStarted(true);

                // work after first start

                /*int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
                int actualVersionCode = 0;
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

                /*if ((oldVersionCode == 0) || (actualVersionCode == 0) || (oldVersionCode < actualVersionCode)) {
                    // block any profile and event actions for package replaced
                    PPApplication.setBlockProfileEventActions(true);

                    // cancel all PPP notification (except PPService notification
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

                    //PPApplication.applicationPackageReplaced = true;

                    //PPApplication.cancelWork(PPApplication.PACKAGE_REPLACED_WORK_TAG);

                    // work for package replaced
                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(PPApplication.PACKAGE_REPLACED_WORK_TAG)
                                    .build();
                    try {
                        // do not test start of PPP, because is not started in this receiver
                        //if (PPApplicationStatic.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                                    //if (PPApplicationStatic.logEnabled()) {
//                                    ListenableFuture<List<WorkInfo>> statuses;
//                                    statuses = workManager.getWorkInfosForUniqueWork(PPApplication.PACKAGE_REPLACED_WORK_TAG);
//                                    try {
//                                        List<WorkInfo> workInfoList = statuses.get();
//                                    } catch (Exception ignored) {
//                                    }
//                                    //}

                            workManager.enqueueUniqueWork(PPApplication.PACKAGE_REPLACED_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                        //}
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                else
                {*/
                PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "start work for first start");

                //PPApplication.cancelWork(PPApplication.AFTER_FIRST_START_WORK_TAG);

                if (!PPApplication.shizukuBinded) {
                    Data workData = new Data.Builder()
                            .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, __activateProfiles)
                            .putBoolean(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, _startFromExternalApplication)
                            .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, _startForExternalAppAction)
                            .putInt(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE, _startForExternalAppDataType)
                            .putString(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, _startForExternalAppDataValue)
                            .putBoolean(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, false)
                            //.putBoolean(PhoneProfilesService.EXTRA_SHOW_TOAST, serviceIntent != null)
                            .build();

//                    PPApplicationStatic.logE("[MAIN_WORKER_CALL] PhoneProfilesService.doForFirstStart", "xxxxxxxxxxxxxxxxxxxx");

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(PPApplication.AFTER_FIRST_START_WORK_TAG)
                                    .setInputData(workData)
                                    //.setInitialDelay(5, TimeUnit.SECONDS)
                                    .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true, false)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

//                                        //if (PPApplicationStatic.logEnabled()) {
//                                        ListenableFuture<List<WorkInfo>> statuses;
//                                        statuses = workManager.getWorkInfosForUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG);
//                                        try {
//                                            List<WorkInfo> workInfoList = statuses.get();
//                                        } catch (Exception ignored) {
//                                        }
//                                        //}

//                                PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doFirstStart", "keepResultsForAtLeast");
                                //workManager.enqueue(worker);
                                // !!! MUST BE APPEND_OR_REPLACE FOR EXTRA_START_FOR_EXTERNAL_APPLICATION !!!
                                workManager.enqueueUniqueWork(PPApplication.AFTER_FIRST_START_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                //}
                //}

                PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart - handler", "END");

                dataWrapper.invalidateDataWrapper();

            } catch (Exception eee) {
                PPApplicationStatic.logException("PhoneProfilesService.doForFirstStart.2 - handler", Log.getStackTraceString(eee));
                //PPApplicationStatic.recordException(eee);
                throw eee;
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        };
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);

        PPApplicationStatic.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
    }

    //TODO - add here change of application preferences after package replaced
    private void _doForPackageReplaced_older(Context appContext, int oldVersionCode, int actualVersionCode) {
        if (actualVersionCode <= 2322) {
            // for old packages use Priority in events
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
            editor.apply();
        }
        if (actualVersionCode <= 2400) {
            PPApplicationStatic.setDaysAfterFirstStart(appContext, 0);
            PPApplicationStatic.setDonationNotificationCount(appContext, 0);
            DonationBroadcastReceiver.setAlarm(appContext);
        }

        if (actualVersionCode <= 2700) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

            editor.putBoolean(PPApplication.PREF_ACTIVATOR_ACTIVITY_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_ACTIVATOR_LIST_FRAGMENT_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_ACTIVATOR_LIST_ADAPTER_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
            editor.putBoolean(PPApplication.PREF_PROFILES_PREFS_ACTIVITY_START_TARGET_HELPS, false);
            editor.putBoolean(PPApplication.PREF_EVENTS_PREFS_ACTIVITY_START_TARGET_HELPS, false);
            editor.apply();
        }
        if (actualVersionCode <= 3200) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(PPApplication.PREF_PROFILES_PREFS_ACTIVITY_START_TARGET_HELPS, true);
            editor.apply();
        }
        if (actualVersionCode <= 3500) {
            if (!ApplicationPreferences.getSharedPreferences(appContext).contains(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT)) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.applicationActivateWithAlert);
                editor.apply();
            }

            // continue donation notification
            if (PPApplicationStatic.getDaysAfterFirstStart(appContext) == 8) {
                PPApplicationStatic.setDonationNotificationCount(appContext, 1);
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
                    //String searchStringNew = "";
                    StringBuilder str = new StringBuilder();
                    String[] searchStringSplits = searchStringOrig.split(StringConstants.STR_SPLIT_REGEX);
                    for (String split : searchStringSplits) {
                        if (!split.isEmpty()) {
                            if (str.length() > 0)
                                str.append("|");
                            if (split.startsWith("!"))
                                str.append("\\");
                            str.append(split);
                        }
                    }
                    event._eventPreferencesCalendar._searchString = str.toString();
                    DatabaseHandler.getInstance(appContext).updateEvent(event);
                }
            }
        }

        if (actualVersionCode <= 4870) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS_FILTER_SPINNER, true);
            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FILTER_SPINNER, true);

            String theme = ApplicationPreferences.applicationTheme(appContext, false);
            if (!(theme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE) ||
                    theme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_DARK) ||
                    theme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_NIGHT_MODE))) {
                String defaultValue = ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE;
                if (Build.VERSION.SDK_INT >= 28)
                    defaultValue = ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_NIGHT_MODE;
                editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue);
                GlobalGUIRoutines.switchNightMode(appContext, true);
            }

            editor.apply();
        }

        if (actualVersionCode <= 5020) {
            if (Build.VERSION.SDK_INT >= 28) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_NIGHT_MODE);
                GlobalGUIRoutines.switchNightMode(appContext, true);
                editor.apply();
            }
        }

        if (actualVersionCode <= 5250) {
            if (oldVersionCode <= 5210) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);

                NotificationManagerCompat manager = NotificationManagerCompat.from(appContext);
                try {
                    NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
                    if (channel != null) {
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED,
                                channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

                int filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
                if (filterEventsSelectedItem == 2)
                    filterEventsSelectedItem++;
                editor.putInt(ApplicationPreferences.PREF_EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
                editor.apply();
                ApplicationPreferences.editorEventsViewSelectedItem(appContext);
            }
        }

        if (actualVersionCode <= 5430) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
            String notificationBackgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            SharedPreferences.Editor editor = preferences.edit();
            if (!preferences.contains(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR))
                editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF);
            if (notificationBackgroundColor.equals("2")) {
                editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "1");
            } else if (notificationBackgroundColor.equals("4")) {
                editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "3");
                editor.apply();
            }
            editor.apply();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private boolean doForPackageReplaced(Context appContext, int oldVersionCode) {
        //int oldVersionCode = PPApplicationStatic.getSavedVersionCode(appContext);
        int actualVersionCode = 0;
        // save version code
        try {
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
            PPApplicationStatic.setSavedVersionCode(appContext, actualVersionCode);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        Permissions.setAllShowRequestPermissions(appContext, true);

        if (PPApplicationStatic.logEnabled()) {
            PPApplicationStatic.logE("PhoneProfilesService.doForPackageReplaced", "----- oldVersionCode=" + oldVersionCode);
            PPApplicationStatic.logE("PhoneProfilesService.doForPackageReplaced", "----- actualVersionCode=" + actualVersionCode);
        }
        try {
            if (oldVersionCode < actualVersionCode) {
                PPApplicationStatic.logE("PhoneProfilesService.doForPackageReplaced", "is new version");

                _doForPackageReplaced_older(appContext, oldVersionCode, actualVersionCode);

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

                if (actualVersionCode <= 6905) {
                    if ((PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT >= 31)) ||
                        (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 33))) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR,
                                ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR_DEFAULT_VALUE);
                        String layoutType = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE,
                                ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE_DEFAULT_VALUE);
                        if (backgroundColor.equals("0") && layoutType.equals("0")) {
                            // is not possible to use decoration when notificication background is not "Native" (0)
                            // and enable decorator only when layout type is "Expandable"
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION,
                                    ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION_DEFAULT_VALUE_PIXEL_SAMSUNG);
                            editor.apply();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 30) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);

                        final String OLD_BROWN_COLOR = String.valueOf(appContext.getColor(R.color.pppColorOldBrownBackground));

                        String backgroundColorNightModeOn = preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                        if (backgroundColorNightModeOn.equalsIgnoreCase(OLD_BROWN_COLOR)) {
                            // color is set to old brown color, this change it to new gray color
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                    ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                            editor.apply();
                        }
                        backgroundColorNightModeOn = preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                        if (backgroundColorNightModeOn.equalsIgnoreCase(OLD_BROWN_COLOR)) {
                            // color is set to old brown color, this change it to new gray color
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                    ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                            editor.apply();
                        }
                        backgroundColorNightModeOn = preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                        if (backgroundColorNightModeOn.equalsIgnoreCase(OLD_BROWN_COLOR)) {
                            // color is set to old brown color, this change it to new gray color
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                    ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                            editor.apply();
                        }
                        backgroundColorNightModeOn = preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                        if (backgroundColorNightModeOn.equalsIgnoreCase(OLD_BROWN_COLOR)) {
                            // color is set to old brown color, this change it to new gray color
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON,
                                    ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE);
                            editor.apply();
                        }
                    }
                }
                if (actualVersionCode <= 6920) {
                    // remove all not used non-named mobile cells
                    DatabaseHandler db = DatabaseHandler.getInstance(appContext);
                    db.deleteNonNamedNotUsedCells();
                }
                if (actualVersionCode <= 6960) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);

                    int bluetoothLEScanDuration = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION,
                            ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE));
                    if (bluetoothLEScanDuration < Integer.parseInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE)) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION,
                                ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE);
                        editor.apply();
                    }
                }

                if (actualVersionCode <= 7080) {
                    if (Build.VERSION.SDK_INT >= 28) {
                        // for Android 9+ use Extender for lock device
                        List<Profile> profileList = DatabaseHandler.getInstance(appContext).getAllProfiles();
                        for (Profile profile : profileList) {
                            if (profile._lockDevice == 2) {
                                profile._lockDevice = 3;
                                DatabaseHandler.getInstance(appContext).updateProfile(profile);
                            }
                        }
                    }
                }

                if (actualVersionCode <= 7145) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);

                    String widgetLauncher = preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER,
                                                ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER_DEFAULT_VALUE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAUNCHER, widgetLauncher);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAUNCHER, widgetLauncher);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LAUNCHER, widgetLauncher);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_DASH_CLOCK_LAUNCHER, widgetLauncher);
                    editor.apply();
                }

                if (actualVersionCode <= 7235) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);

                    boolean prefIndicator = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR,
                            ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_EVENT_DETAILS, !prefIndicator);
                    editor.apply();
                }

                if (actualVersionCode <= 7250) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                    boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(appContext.getApplicationContext());

                    if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE,
                            ApplicationPreferences.applicationWidgetOneRowChangeColorsByNightModeDefaultValue())) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if (nightModeOn) {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87);
                        } else {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12);
                        }
                        editor.apply();
                    }
                    if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE,
                            ApplicationPreferences.applicationWidgetListChangeColorsByNightModeDefaultValue())) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if (nightModeOn) {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87);
                        } else {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12);
                        }
                        editor.apply();
                    }
                    if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE,
                            ApplicationPreferences.applicationWidgetIconChangeColorsByNightModeDefaultValue())) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if (nightModeOn) {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87);
                        } else {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12);
                        }
                        editor.apply();
                    }
                    if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE,
                            ApplicationPreferences.applicationWidgetPanelChangeColorsByNightModeDefaultValue())) {
                        SharedPreferences.Editor editor = preferences.edit();
                        if (nightModeOn) {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87);
                        } else {
                            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12);
                        }
                        editor.apply();
                    }
                }

                if (actualVersionCode <= 7280) {
                    if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy &&
                            (Build.VERSION.SDK_INT >= 35)) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        if (preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION,
                                ApplicationPreferences.notificationUseDecorationDefaultValue())) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, false);
                            editor.apply();
                        }
                        if (!preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                                ApplicationPreferences.notificationShowProfileIconDefaultValue())) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, true);
                            editor.apply();
                        }
                    }
                    if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI &&
                            (Build.VERSION.SDK_INT >= 28)) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        if (!preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                                ApplicationPreferences.notificationShowProfileIconDefaultValue())) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, true);
                            editor.apply();
                        }
                    }
                    if (PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT >= 36)) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                        if (preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION,
                                ApplicationPreferences.notificationUseDecorationDefaultValue())) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, false);
                            editor.apply();
                        }
                        if (!preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                                ApplicationPreferences.notificationShowProfileIconDefaultValue())) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, true);
                            editor.apply();
                        }
                    }
                }
            }

            // Keep this !!! stop tap target for package replaced
            ApplicationPreferences.startStopTargetHelps(appContext, false);

        } catch (Exception ee) {
            PPApplicationStatic.recordException(ee);
        }

        PPApplicationStatic.loadGlobalApplicationData(appContext);
        PPApplicationStatic.loadApplicationPreferences(appContext);
        PPApplicationStatic.loadProfileActivationData(appContext);

        if (oldVersionCode < actualVersionCode) {
            // block any profile and event actions for package replaced
            PPApplicationStatic.setBlockProfileEventActions(true);

            if (PPApplication.mobileCellsScannerEnabledAutoRegistration) {
                MobileCellsScanner.stopAutoRegistration(appContext, true);
                int count = 0;
                while (MobileCellsRegistrationService.serviceStarted && (count < 50)) {
                    GlobalUtils.sleep(100);
                    count++;
                }
            }

        }

        return oldVersionCode < actualVersionCode;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Context appContext = getApplicationContext();

        PPApplicationStatic.logE("PhoneProfilesService.onStartCommand", "intent="+intent);
        PPApplicationStatic.logE("PhoneProfilesService.onStartCommand", "serviceHasFirstStart="+serviceHasFirstStart);

        EditorActivity.itemDragPerformed = false;

        //startForegroundNotification = true;

        boolean isServiceRunning = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
        PPApplicationStatic.logE("PhoneProfilesService.onStartCommand", "------- service is running="+isServiceRunning);

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesService.onStartCommand", "call of PPAppNotification.showNotification");
        PPAppNotification.showNotification(appContext, !isServiceRunning, true, true, true);

        PPApplication.normalServiceStart = (intent != null);
        PPApplication.showToastForProfileActivation = (intent != null);

        if (!serviceHasFirstStart) {
            if (intent != null) {
                //TODO remove for release
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

    // ------------------------------------------------------------------------

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneProfilesService.onConfigurationChanged", "xxx");

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesService.onConfigurationChanged", "call of updateGUI");
        PPApplication.updateGUI(false, false, getApplicationContext());
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
        super.onTaskRemoved(rootIntent);
    }
    */
}
