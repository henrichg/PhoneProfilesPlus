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
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
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

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import me.drakeet.support.toast.ToastCompat;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;


public class PhoneProfilesService extends Service
{
    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    private boolean waitForEndOfStart = true;

    private KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock = null;

    //BrightnessView brightnessView = null;
    BrightnessView screenTimeoutAlwaysOnView = null;
    //BrightnessView keepScreenOnView = null;

    PowerManager.WakeLock keepScreenOnWakeLock = null;

    LockDeviceActivity lockDeviceActivity = null;
    int screenTimeoutBeforeDeviceLock = 0;

    boolean willBeDoRestartEvents = false;

    private static final StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = new StartLauncherFromNotificationReceiver();
    private static final UpdateGUIBroadcastReceiver updateGUIBroadcastReceiver = new UpdateGUIBroadcastReceiver();
    private static final ShowProfileNotificationBroadcastReceiver showProfileNotificationBroadcastReceiver = new ShowProfileNotificationBroadcastReceiver();
    private static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    private static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();

    private TimeChangedReceiver timeChangedReceiver = null;
    private PermissionsNotificationDeletedReceiver permissionsNotificationDeletedReceiver = null;
    private StartEventNotificationDeletedReceiver startEventNotificationDeletedReceiver = null;
    private NotUsedMobileCellsNotificationDeletedReceiver notUsedMobileCellsNotificationDeletedReceiver = null;
    private ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;
    private NotUsedMobileCellsNotificationDisableReceiver notUsedMobileCellsNotificationDisableReceiver = null;
    private DonationBroadcastReceiver donationBroadcastReceiver = null;
    //private StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = null;
    private IgnoreBatteryOptimizationDisableReceiver ignoreBatteryOptimizationDisableReceiver = null;

    private BatteryBroadcastReceiver batteryEventReceiver = null;
    private BatteryBroadcastReceiver batteryChangeLevelReceiver = null;
    private HeadsetConnectionBroadcastReceiver headsetPlugReceiver = null;
    private NFCStateChangedBroadcastReceiver nfcStateChangedBroadcastReceiver = null;
    private DockConnectionBroadcastReceiver dockConnectionBroadcastReceiver = null;
    private WifiConnectionBroadcastReceiver wifiConnectionBroadcastReceiver = null;
    private BluetoothConnectionBroadcastReceiver bluetoothConnectionBroadcastReceiver = null;
    private BluetoothStateChangedBroadcastReceiver bluetoothStateChangedBroadcastReceiver = null;
    private WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    private LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    private AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    //private SMSBroadcastReceiver smsBroadcastReceiver = null;
    //private SMSBroadcastReceiver mmsBroadcastReceiver = null;
    private CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;
    private WifiScanBroadcastReceiver wifiScanReceiver = null;
    private BluetoothScanBroadcastReceiver bluetoothScanReceiver = null;
    private BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = null;
    private PPPExtenderBroadcastReceiver pppExtenderBroadcastReceiver = null;
    private PPPExtenderBroadcastReceiver pppExtenderForceStopApplicationBroadcastReceiver = null;
    private PPPExtenderBroadcastReceiver pppExtenderForegroundApplicationBroadcastReceiver = null;
    private PPPExtenderBroadcastReceiver pppExtenderSMSBroadcastReceiver = null;
    private PPPExtenderBroadcastReceiver pppExtenderCallBroadcastReceiver = null;
    private EventTimeBroadcastReceiver eventTimeBroadcastReceiver = null;
    private EventCalendarBroadcastReceiver eventCalendarBroadcastReceiver = null;
    private EventDelayStartBroadcastReceiver eventDelayStartBroadcastReceiver = null;
    private EventDelayEndBroadcastReceiver eventDelayEndBroadcastReceiver = null;
    private ProfileDurationAlarmBroadcastReceiver profileDurationAlarmBroadcastReceiver = null;
    private SMSEventEndBroadcastReceiver smsEventEndBroadcastReceiver = null;
    //private NotificationCancelAlarmBroadcastReceiver notificationCancelAlarmBroadcastReceiver = null;
    private NFCEventEndBroadcastReceiver nfcEventEndBroadcastReceiver = null;
    private RunApplicationWithDelayBroadcastReceiver runApplicationWithDelayBroadcastReceiver = null;
    private MissedCallEventEndBroadcastReceiver missedCallEventEndBroadcastReceiver = null;
    private StartEventNotificationBroadcastReceiver startEventNotificationBroadcastReceiver = null;
    private GeofencesScannerSwitchGPSBroadcastReceiver geofencesScannerSwitchGPSBroadcastReceiver = null;
    private LockDeviceActivityFinishBroadcastReceiver lockDeviceActivityFinishBroadcastReceiver = null;
    private AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    private AlarmClockEventEndBroadcastReceiver alarmClockEventEndBroadcastReceiver = null;
    private NotificationEventEndBroadcastReceiver notificationEventEndBroadcastReceiver = null;
    private LockDeviceAfterScreenOffBroadcastReceiver lockDeviceAfterScreenOffBroadcastReceiver = null;
    private OrientationEventBroadcastReceiver orientationEventBroadcastReceiver = null;

    private PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    private DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;

    private SettingsContentObserver settingsContentObserver = null;
    private MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;
    private ContactsContentObserver contactsContentObserver = null;


    static final String ACTION_COMMAND = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_COMMAND";
    private static final String ACTION_STOP = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_STOP_SERVICE";
    static final String ACTION_START_LAUNCHER_FROM_NOTIFICATION = PPApplication.PACKAGE_NAME + ".PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION";
    static final String ACTION_EVENT_TIME_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventTimeBroadcastReceiver";
    static final String ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventCalendarBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventDelayStartBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".EventDelayEndBroadcastReceiver";
    static final String ACTION_PROFILE_DURATION_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ProfileDurationAlarmBroadcastReceiver";
    static final String ACTION_SMS_EVENT_END_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".SMSEventEndBroadcastReceiver";
    //private static final String ACTION_NOTIFICATION_CANCEL_ALARM_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".NotificationCancelAlarmBroadcastReceiver";
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

    //static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    //static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_DEACTIVATE_PROFILE = "deactivate_profile";
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
    static final String EXTRA_FOR_SCREEN_ON = "for_screen_on";
    //static final String EXTRA_START_LOCATION_UPDATES = "start_location_updates";
    //private static final String EXTRA_STOP_LOCATION_UPDATES = "stop_location_updates";
    static final String EXTRA_RESTART_EVENTS = "restart_events";
    static final String EXTRA_UNBLOCK_EVENTS_RUN = "unblock_events_run";
    static final String EXTRA_REACTIVATE_PROFILE = "reactivate_profile";
    static final String EXTRA_LOG_TYPE = "log_type";
    static final String EXTRA_DELAYED_WORK = "delayed_work";
    static final String EXTRA_SENSOR_TYPE = "sensor_type";
    static final String EXTRA_ELAPSED_ALARMS_WORK = "elapsed_alarms_work";

    //-----------------------

    private OrientationScanner orientationScanner = null;

    private boolean mStartedOrientationSensors = false;

    private GeofencesScanner geofencesScanner = null;

    private PhoneStateScanner phoneStateScanner = null;

    private TwilightScanner twilightScanner = null;


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

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PPApplication.logE("PhoneProfilesService.stopReceiver", "xxx");
            try {
                //noinspection deprecation
                context.removeStickyBroadcast(intent);
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stopReceiver", Log.getStackTraceString(e));
            }
            try {
                stopForeground(true);
                stopSelf();
            } catch (Exception e) {
                Log.e("PhoneProfilesService.stopReceiver", Log.getStackTraceString(e));
            }
        }
    };

    //--------------------------

    //public static SipManager mSipManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "android.os.Build.VERSION.SDK_INT=" + android.os.Build.VERSION.SDK_INT);

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = this;
        }

        PPApplication.sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        PPApplication.accelerometerSensor = PPApplication.getAccelerometerSensor(getApplicationContext());
        PPApplication.magneticFieldSensor = PPApplication.getMagneticFieldSensor(getApplicationContext());
        PPApplication.proximitySensor = PPApplication.getProximitySensor(getApplicationContext());
        PPApplication.lightSensor = PPApplication.getLightSensor(getApplicationContext());

        PPApplication.loadApplicationPreferences(getApplicationContext());
        PPApplication.loadGlobalApplicationData(getApplicationContext());
        PPApplication.loadProfileActivationData(getApplicationContext());

        serviceHasFirstStart = false;
        //serviceRunning = false;
        //runningInForeground = false;
        waitForEndOfStart = true;
        //ApplicationPreferences.forceNotUseAlarmClock = false;

        final Context appContext = getApplicationContext();

        appContext.registerReceiver(stopReceiver, new IntentFilter(PhoneProfilesService.ACTION_STOP));
        //LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(commandReceiver, new IntentFilter(ACTION_COMMAND));

        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION);
        appContext.registerReceiver(startLauncherFromNotificationReceiver, intentFilter5);

        appContext.registerReceiver(showProfileNotificationBroadcastReceiver, new IntentFilter(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION));
        appContext.registerReceiver(updateGUIBroadcastReceiver, new IntentFilter(PPApplication.ACTION_UPDATE_GUI));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(refreshActivitiesBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver"));
        LocalBroadcastManager.getInstance(appContext).registerReceiver(dashClockBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver"));

        PPApplication.logE("PhoneProfilesService.onCreate", "before show profile notification");

        //if (Build.VERSION.SDK_INT >= 26)
        // show empty notification to avoid ANR in api level 26
        PPApplication.showProfileNotification(true, true/*, false*/);

        PPApplication.logE("PhoneProfilesService.onCreate", "after show profile notification");


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
                //Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                //Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
            }
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
        } catch (Exception ignored) {}

        /*
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefActivatorActivityStartTargetHelps = true;
        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = true;
        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = true;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelps = true;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile = true;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsFilterSpinner = true;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = true;
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, true);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = true;
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = true;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = true;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = true;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, true);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = true;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = true;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, true);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = true;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = true;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = true;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, true);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = true;
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps = true;
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, true);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelpsSave = true;
        editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, true);
        ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = true;
        //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
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

        keyguardManager = (KeyguardManager)appContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        willBeDoRestartEvents = false;

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OK created");

        PPApplication.startHandlerThread("PhoneProfilesService.doForFirstStart");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler.getInstance(appContext).deactivateProfile();
                ActivateProfileHelper.updateGUI(appContext, false, true);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        Context appContext = getApplicationContext();

        // cancel works
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

        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(commandReceiver);
        } catch (Exception ignored) {}
        try {
            appContext.unregisterReceiver(stopReceiver);
        } catch (Exception ignored) {}

        unregisterReceiversAndWorkers();

        stopSimulatingRingingCall(/*true*/);
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        try {
            //if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))
                stopForeground(true);
            /*else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }*/
        } catch (Exception ignored) {
        }

        try {
            appContext.unregisterReceiver(startLauncherFromNotificationReceiver);
        } catch (Exception ignored) {}
        try {
            appContext.unregisterReceiver(showProfileNotificationBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            appContext.unregisterReceiver(updateGUIBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(refreshActivitiesBroadcastReceiver);
        } catch (Exception ignored) {}
        try {
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(dashClockBroadcastReceiver);
        } catch (Exception ignored) {}

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

        synchronized (PPApplication.phoneProfilesServiceMutex) {
            instance = null;
        }

        serviceHasFirstStart = false;
        //serviceRunning = false;
        //runningInForeground = false;
        waitForEndOfStart = true;
    }

    static void cancelWork(String name, Context context) {
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            workManager.cancelAllWorkByTag(name);
        } catch (Exception ignored) {}
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            workManager.cancelUniqueWork(name);
        } catch (Exception ignored) {}
    }

    static PhoneProfilesService getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
            return instance;
        //}
    }

    public static void stop(Context context) {
        if (instance != null) {
            try {
                //noinspection deprecation
                context.sendStickyBroadcast(new Intent(ACTION_STOP));
                //context.sendBroadcast(new Intent(ACTION_STOP));
            } catch (Exception ignored) {
            }
        }
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

//    boolean getServiceRunning() {
//        return serviceRunning;
//    }

    boolean getWaitForEndOfStart() {
        return waitForEndOfStart;
    }

    void setWaitForEndOfStart(boolean wait) {
        waitForEndOfStart = wait;
        if (!wait) {
            final Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String text = getString(R.string.app_name) + " " + getString(R.string.application_is_started_toast);
                    Toast msg = ToastCompat.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                    msg.show();
                }
            });
        }
    }

    private void registerAllTheTimeRequiredReceivers(boolean register) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "xxx");
        if (!register) {
            if (timeChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER time changed", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER time changed");
                try {
                    appContext.unregisterReceiver(timeChangedReceiver);
                    timeChangedReceiver = null;
                } catch (Exception e) {
                    timeChangedReceiver = null;
                }
            }
            if (permissionsNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER permissions notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER permissions notification delete");
                try {
                    appContext.unregisterReceiver(permissionsNotificationDeletedReceiver);
                    permissionsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    permissionsNotificationDeletedReceiver = null;
                }
            }
            if (startEventNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER start event notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER start event notification delete");
                try {
                    appContext.unregisterReceiver(startEventNotificationDeletedReceiver);
                    startEventNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    startEventNotificationDeletedReceiver = null;
                }
            }
            if (notUsedMobileCellsNotificationDeletedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER not used mobile cells notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER not used mobile cells notification delete");
                try {
                    appContext.unregisterReceiver(notUsedMobileCellsNotificationDeletedReceiver);
                    notUsedMobileCellsNotificationDeletedReceiver = null;
                } catch (Exception e) {
                    notUsedMobileCellsNotificationDeletedReceiver = null;
                }
            }
            if (shutdownBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER shutdown");
                try {
                    appContext.unregisterReceiver(shutdownBroadcastReceiver);
                    shutdownBroadcastReceiver = null;
                } catch (Exception e) {
                    shutdownBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered shutdown");
            if (screenOnOffReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER screen on off");
                try {
                    appContext.unregisterReceiver(screenOnOffReceiver);
                    screenOnOffReceiver = null;
                } catch (Exception e) {
                    screenOnOffReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered screen on off");
            if (interruptionFilterChangedReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER interruption filter");
                try {
                    appContext.unregisterReceiver(interruptionFilterChangedReceiver);
                    interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    interruptionFilterChangedReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered interruption filter");
            if (phoneCallBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER phone call");
                try {
                    appContext.unregisterReceiver(phoneCallBroadcastReceiver);
                    phoneCallBroadcastReceiver = null;
                } catch (Exception e) {
                    phoneCallBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered phone call");
            if (ringerModeChangeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER ringer mode change");
                try {
                    appContext.unregisterReceiver(ringerModeChangeReceiver);
                    ringerModeChangeReceiver = null;
                } catch (Exception e) {
                    ringerModeChangeReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered ringer mode change");
            if (settingsContentObserver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER settings content observer");
                try {
                    appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
                    settingsContentObserver = null;
                } catch (Exception e) {
                    settingsContentObserver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered settings content observer");
            if (deviceIdleModeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER device idle mode");
                try {
                    appContext.unregisterReceiver(deviceIdleModeReceiver);
                    deviceIdleModeReceiver = null;
                } catch (Exception e) {
                    deviceIdleModeReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered device idle mode");
            if (bluetoothConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER bluetooth connection");
                try {
                    appContext.unregisterReceiver(bluetoothConnectionBroadcastReceiver);
                    bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    bluetoothConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayStartBroadcastReceiver");
            if (eventDelayStartBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayStartBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventDelayStartBroadcastReceiver);
                    eventDelayStartBroadcastReceiver = null;
                } catch (Exception e) {
                    eventDelayStartBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayStartBroadcastReceiver");
            if (eventDelayEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventDelayEndBroadcastReceiver);
                    eventDelayEndBroadcastReceiver = null;
                } catch (Exception e) {
                    eventDelayEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayEndBroadcastReceiver");
            if (profileDurationAlarmBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER profileDurationAlarmBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(profileDurationAlarmBroadcastReceiver);
                    profileDurationAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    profileDurationAlarmBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered profileDurationAlarmBroadcastReceiver");
            /*
            if (notificationCancelAlarmBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER notificationCancelAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER notificationCancelAlarmBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(notificationCancelAlarmBroadcastReceiver);
                    notificationCancelAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    notificationCancelAlarmBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered notificationCancelAlarmBroadcastReceiver");
            */
            if (runApplicationWithDelayBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER runApplicationWithDelayBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(runApplicationWithDelayBroadcastReceiver);
                    runApplicationWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    runApplicationWithDelayBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered runApplicationWithDelayBroadcastReceiver");
            if (startEventNotificationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER startEventNotificationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(startEventNotificationBroadcastReceiver);
                    startEventNotificationBroadcastReceiver = null;
                } catch (Exception e) {
                    startEventNotificationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered startEventNotificationBroadcastReceiver");
            if (lockDeviceActivityFinishBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER lockDeviceActivityFinishBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(lockDeviceActivityFinishBroadcastReceiver);
                    lockDeviceActivityFinishBroadcastReceiver = null;
                } catch (Exception e) {
                    lockDeviceActivityFinishBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered postDelayedBroadcastReceiver");
            if (pppExtenderBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER pppExtenderBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderBroadcastReceiver);
                    pppExtenderBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered pppExtenderBroadcastReceiver");
            if (notUsedMobileCellsNotificationDisableReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER notUsedMobileCellsNotificationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER notUsedMobileCellsNotificationDisableReceiver");
                try {
                    appContext.unregisterReceiver(notUsedMobileCellsNotificationDisableReceiver);
                    notUsedMobileCellsNotificationDisableReceiver = null;
                } catch (Exception e) {
                    notUsedMobileCellsNotificationDisableReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered notUsedMobileCellsNotificationDisableReceiver");
            if (donationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER donationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER donationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(donationBroadcastReceiver);
                    donationBroadcastReceiver = null;
                } catch (Exception e) {
                    donationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered donationBroadcastReceiver");
            if (ignoreBatteryOptimizationDisableReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER ignoreBatteryOptimizationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER ignoreBatteryOptimizationDisableReceiver");
                try {
                    appContext.unregisterReceiver(ignoreBatteryOptimizationDisableReceiver);
                    ignoreBatteryOptimizationDisableReceiver = null;
                } catch (Exception e) {
                    ignoreBatteryOptimizationDisableReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered ignoreBatteryOptimizationDisableReceiver");
            if (lockDeviceAfterScreenOffBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER lockDeviceAfterScreenOffBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER lockDeviceAfterScreenOffBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(lockDeviceAfterScreenOffBroadcastReceiver);
                    lockDeviceAfterScreenOffBroadcastReceiver = null;
                } catch (Exception e) {
                    lockDeviceAfterScreenOffBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered lockDeviceAfterScreenOffBroadcastReceiver");
            if (contactsContentObserver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER contacts content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER contacts content observer");
                try {
                    appContext.getContentResolver().unregisterContentObserver(contactsContentObserver);
                    contactsContentObserver = null;
                } catch (Exception e) {
                    contactsContentObserver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered contacts content observer");
            if (wifiStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER wifiStateChangedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER wifiStateChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
                    wifiStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiStateChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered wifiStateChangedBroadcastReceiver");
        }
        if (register) {
            if (timeChangedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER time changed", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER time changed");
                //PPApplication.lastUptimeTime = SystemClock.elapsedRealtime();
                //PPApplication.lastEpochTime = System.currentTimeMillis();
                timeChangedReceiver = new TimeChangedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                //intentFilter5.addAction(Intent.ACTION_TIME_TICK);
                intentFilter5.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter5.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                appContext.registerReceiver(timeChangedReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered time changed");

            if (permissionsNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER permissions notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER permissions notification delete");
                permissionsNotificationDeletedReceiver = new PermissionsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(GrantPermissionActivity.NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(permissionsNotificationDeletedReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered permissions notification delete");

            if (startEventNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER start event notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER start event notification delete");
                startEventNotificationDeletedReceiver = new StartEventNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(StartEventNotificationDeletedReceiver.START_EVENT_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(startEventNotificationDeletedReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered start event notification delete");

            if (notUsedMobileCellsNotificationDeletedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER not used mobile cells notification delete", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER not used mobile cells notification delete");
                notUsedMobileCellsNotificationDeletedReceiver = new NotUsedMobileCellsNotificationDeletedReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                appContext.registerReceiver(notUsedMobileCellsNotificationDeletedReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered not used mobile cells notification delete");

            if (shutdownBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER shutdown");
                shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SHUTDOWN);
                appContext.registerReceiver(shutdownBroadcastReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered shutdown");

            // required for Lock device, Hide notification in lock screen, screen timeout +
            // screen on/off event + rescan wifi, bluetooth, location, mobile cells
            if (screenOnOffReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER screen on off");
                screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
                appContext.registerReceiver(screenOnOffReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered screen on off");

            // required for Do not disturb ringer mode
            if (interruptionFilterChangedReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER interruption filter");
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
                    if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                        interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                        IntentFilter intentFilter11 = new IntentFilter();
                        intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                        appContext.registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
                    }
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered interruption filter");

            // required for unlink ring and notification volume
            if (phoneCallBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER phone call");
                phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
                IntentFilter intentFilter6 = new IntentFilter();
                intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                appContext.registerReceiver(phoneCallBroadcastReceiver, intentFilter6);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered phone call");

            // required for unlink ring and notification volume
            if (ringerModeChangeReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER ringer mode change");
                ringerModeChangeReceiver = new RingerModeChangeReceiver();
                IntentFilter intentFilter7 = new IntentFilter();
                intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                appContext.registerReceiver(ringerModeChangeReceiver, intentFilter7);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered ringer mode change");

            // required for unlink ring and notification volume
            if (settingsContentObserver == null) {
                try {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER settings content observer");
                    //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
                    settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
                    appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
                } catch (Exception ignored) {}
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered settings content observer");

            // required for start EventsHandler in idle maintenance window
            if (deviceIdleModeReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER device idle mode");
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    deviceIdleModeReceiver = new DeviceIdleModeBroadcastReceiver();
                    IntentFilter intentFilter9 = new IntentFilter();
                    intentFilter9.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
                    // is @hide :-(
                    //if (android.os.Build.VERSION.SDK_INT >= 24)
                    //    intentFilter9.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
                    appContext.registerReceiver(deviceIdleModeReceiver, intentFilter9);
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered device idle mode");

            // required for (un)register connected bluetooth devices
            if (bluetoothConnectionBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER bluetooth connection");

                bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                //intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                appContext.registerReceiver(bluetoothConnectionBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered bluetooth connection");

            if (eventDelayStartBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayStartBroadcastReceiver");

                eventDelayStartBroadcastReceiver = new EventDelayStartBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                appContext.registerReceiver(eventDelayStartBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered eventDelayStartBroadcastReceiver");

            if (eventDelayEndBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayEndBroadcastReceiver");

                eventDelayEndBroadcastReceiver = new EventDelayEndBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                appContext.registerReceiver(eventDelayEndBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered eventDelayEndBroadcastReceiver");

            if (profileDurationAlarmBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER profileDurationAlarmBroadcastReceiver");

                profileDurationAlarmBroadcastReceiver = new ProfileDurationAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(profileDurationAlarmBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered profileDurationAlarmBroadcastReceiver");

            /*
            if (notificationCancelAlarmBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER notificationCancelAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER notificationCancelAlarmBroadcastReceiver");

                notificationCancelAlarmBroadcastReceiver = new NotificationCancelAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_NOTIFICATION_CANCEL_ALARM_BROADCAST_RECEIVER);
                appContext.registerReceiver(notificationCancelAlarmBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered notificationCancelAlarmBroadcastReceiver");
            */

            if (runApplicationWithDelayBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER runApplicationWithDelayBroadcastReceiver");

                runApplicationWithDelayBroadcastReceiver = new RunApplicationWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(runApplicationWithDelayBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered runApplicationWithDelayBroadcastReceiver");

            if (startEventNotificationBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER startEventNotificationBroadcastReceiver");

                startEventNotificationBroadcastReceiver = new StartEventNotificationBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(startEventNotificationBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered startEventNotificationBroadcastReceiver");

            if (lockDeviceActivityFinishBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER lockDeviceActivityFinishBroadcastReceiver");

                lockDeviceActivityFinishBroadcastReceiver = new LockDeviceActivityFinishBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                appContext.registerReceiver(lockDeviceActivityFinishBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered lockDeviceActivityFinishBroadcastReceiver");

            if (pppExtenderBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_pppExtenderBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER pppExtenderBroadcastReceiver");

                pppExtenderBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                appContext.registerReceiver(pppExtenderBroadcastReceiver, intentFilter14,
                        PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered pppExtenderBroadcastReceiver");

            if (notUsedMobileCellsNotificationDisableReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER notUsedMobileCellsNotificationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER notUsedMobileCellsNotificationDisableReceiver");
                notUsedMobileCellsNotificationDisableReceiver = new NotUsedMobileCellsNotificationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                appContext.registerReceiver(notUsedMobileCellsNotificationDisableReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered notUsedMobileCellsNotificationDisableReceiver");

            if (donationBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER donationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER donationBroadcastReceiver");
                donationBroadcastReceiver = new DonationBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(PPApplication.ACTION_DONATION);
                appContext.registerReceiver(donationBroadcastReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered donationBroadcastReceiver");

            if (ignoreBatteryOptimizationDisableReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER ignoreBatteryOptimizationDisableReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER ignoreBatteryOptimizationDisableReceiver");
                ignoreBatteryOptimizationDisableReceiver = new IgnoreBatteryOptimizationDisableReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(IgnoreBatteryOptimizationNotification.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION);
                appContext.registerReceiver(ignoreBatteryOptimizationDisableReceiver, intentFilter5);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered ignoreBatteryOptimizationDisableReceiver");

            if (lockDeviceAfterScreenOffBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER lockDeviceAfterScreenOffBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER lockDeviceAfterScreenOffBroadcastReceiver");

                lockDeviceAfterScreenOffBroadcastReceiver = new LockDeviceAfterScreenOffBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
                appContext.registerReceiver(lockDeviceAfterScreenOffBroadcastReceiver, intentFilter14);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered lockDeviceAfterScreenOffBroadcastReceiver");

            if (contactsContentObserver == null) {
                try {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER contacts content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER contacts content observer");
                    contactsContentObserver = new ContactsContentObserver(appContext, new Handler());
                    appContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsContentObserver);
                } catch (Exception ignored) {}
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered settings content observer");

            if (wifiStateChangedBroadcastReceiver == null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER wifiStateChangedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER wifiStateChangedBroadcastReceiver");

                wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
                IntentFilter intentFilter8 = new IntentFilter();
                intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                appContext.registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered lockDeviceAfterScreenOffBroadcastReceiver");
        }
    }

    private void registerBatteryEventReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryEventReceiver", "PhoneProfilesService_registerBatteryEventReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "xxx");
        if (!register) {
            if (batteryEventReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryEventReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryEventReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(batteryEventReceiver);
                    batteryEventReceiver = null;
                } catch (Exception e) {
                    batteryEventReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase/* || (powerSaveModeReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                }
                if (eventCount > 0) {
                    if (batteryEventReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryEventReceiver->REGISTER", "PhoneProfilesService_registerBatteryEventReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "REGISTER");
                        batteryEventReceiver = new BatteryBroadcastReceiver();
                        IntentFilter intentFilter1 = new IntentFilter();
                        intentFilter1.addAction(Intent.ACTION_POWER_CONNECTED);
                        intentFilter1.addAction(Intent.ACTION_POWER_DISCONNECTED);
                        intentFilter1.addAction(Intent.ACTION_BATTERY_LOW);
                        intentFilter1.addAction(Intent.ACTION_BATTERY_OKAY);
                        appContext.registerReceiver(batteryEventReceiver, intentFilter1);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "registered");
                } else {
                    registerBatteryEventReceiver(false, false);
                }
            }
            else
                registerBatteryEventReceiver(false, false);
        }
    }

    private void registerBatteryChangedReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryChangedReceiver", "PhoneProfilesService_registerBatteryChangedReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "xxx");
        if (!register) {
            if (batteryChangeLevelReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChangedReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryChangedReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(batteryChangeLevelReceiver);
                    batteryChangeLevelReceiver = null;
                } catch (Exception e) {
                    batteryChangeLevelReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                int batteryLevelCount = 1;
                if (checkDatabase/* || (batteryChangeLevelReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    //eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                    // get non-stopped events with battery sensor with levels > 0 and < 100
                    batteryLevelCount = DatabaseHandler.getInstance(appContext).getBatteryEventWithLevelCount();
                }
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                String powerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;
                if (powerSaveModeInternal.equals("1") || powerSaveModeInternal.equals("2") || ((batteryLevelCount > 0) && (eventCount > 0))) {
                    if (batteryChangeLevelReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChangedReceiver->REGISTER", "PhoneProfilesService_registerBatteryChangedReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "REGISTER");
                        batteryChangeLevelReceiver = new BatteryBroadcastReceiver();
                        IntentFilter intentFilter1_1 = new IntentFilter();
                        intentFilter1_1.addAction(Intent.ACTION_BATTERY_CHANGED);
                        appContext.registerReceiver(batteryChangeLevelReceiver, intentFilter1_1);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "registered");
                } else {
                    registerBatteryChangedReceiver(false, false);
                }
            }
            else
                registerBatteryChangedReceiver(false, false);
        }
    }

    private void registerReceiverForPeripheralsSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "xxx");
        if (!register) {
            if (headsetPlugReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER headset plug");
                try {
                    appContext.unregisterReceiver(headsetPlugReceiver);
                    headsetPlugReceiver = null;
                } catch (Exception e) {
                    headsetPlugReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered headset plug");
            if (dockConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER dock connection");
                try {
                    appContext.unregisterReceiver(dockConnectionBroadcastReceiver);
                    dockConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    dockConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered dock connection");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (headsetPlugReceiver == null) || (dockConnectionBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL, false);
                if (eventCount > 0) {
                    if (headsetPlugReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER headset plug");
                        headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
                        IntentFilter intentFilter2 = new IntentFilter();
                        intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
                        intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                        intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                        appContext.registerReceiver(headsetPlugReceiver, intentFilter2);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered headset plug");
                    if (dockConnectionBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER dock connection");
                        dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
                        IntentFilter intentFilter12 = new IntentFilter();
                        intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
                        intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
                        appContext.registerReceiver(dockConnectionBroadcastReceiver, intentFilter12);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered dock connection");
                } else {
                    registerReceiverForPeripheralsSensor(false, false);
                }
            }
            else
                registerReceiverForPeripheralsSensor(false, false);
        }
    }

    private void registerReceiverForSMSSensor(boolean register, boolean checkDatabase) {
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
            if (smsEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->UNREGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "UNREGISTER smsEventEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(smsEventEndBroadcastReceiver);
                    smsEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    smsEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered smsEventEndBroadcastReceiver");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (smsBroadcastReceiver == null) || (mmsBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false);
                if (eventCount > 0) {
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
                    if (smsEventEndBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER smsEventEndBroadcastReceiver");
                        smsEventEndBroadcastReceiver = new SMSEventEndBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(smsEventEndBroadcastReceiver, intentFilter22);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered smsEventEndBroadcastReceiver");
                } else {
                    registerReceiverForSMSSensor(false, false);
                }
            }
            else
                registerReceiverForSMSSensor(false, false);
        }
    }

    private void registerReceiverForCalendarSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor", "PhoneProfilesService_registerReceiverForCalendarSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "xxx");
        if (!register) {
            if (calendarProviderChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER calendarProviderChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(calendarProviderChangedBroadcastReceiver);
                    calendarProviderChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    calendarProviderChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
            if (eventCalendarBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER eventCalendarBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventCalendarBroadcastReceiver);
                    eventCalendarBroadcastReceiver = null;
                } catch (Exception e) {
                    eventCalendarBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (calendarProviderChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
                if (eventCount > 0) {
                    if (calendarProviderChangedBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER calendarProviderChangedBroadcastReceiver");
                        calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
                        intentFilter23.addDataScheme("content");
                        intentFilter23.addDataAuthority("com.android.calendar", null);
                        intentFilter23.setPriority(Integer.MAX_VALUE);
                        appContext.registerReceiver(calendarProviderChangedBroadcastReceiver, intentFilter23);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
                    if (eventCalendarBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER eventCalendarBroadcastReceiver");
                        eventCalendarBroadcastReceiver = new EventCalendarBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                        appContext.registerReceiver(eventCalendarBroadcastReceiver, intentFilter23);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
                } else {
                    registerReceiverForCalendarSensor(false, false);
                }
            }
            else
                registerReceiverForCalendarSensor(false, false);
        }
    }

    private void registerReceiverForRadioSwitchAirplaneModeSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "xxx");
        if (!register) {
            if (airplaneModeStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(airplaneModeStateChangedBroadcastReceiver);
                    airplaneModeStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    airplaneModeStateChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (airplaneModeStateChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE, false);
                if (eventCount > 0) {
                    if (airplaneModeStateChangedBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "REGISTER");
                        airplaneModeStateChangedBroadcastReceiver = new AirplaneModeStateChangedBroadcastReceiver();
                        IntentFilter intentFilter19 = new IntentFilter();
                        intentFilter19.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        appContext.registerReceiver(airplaneModeStateChangedBroadcastReceiver, intentFilter19);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchAirplaneModeSensor(false, false);
                }
            }
            else
                registerReceiverForRadioSwitchAirplaneModeSensor(false, false);
        }
    }

    private void registerReceiverForRadioSwitchNFCSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                if (nfcStateChangedBroadcastReceiver != null) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                    //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "UNREGISTER");
                    try {
                        appContext.unregisterReceiver(nfcStateChangedBroadcastReceiver);
                        nfcStateChangedBroadcastReceiver = null;
                    } catch (Exception e) {
                        nfcStateChangedBroadcastReceiver = null;
                    }
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "not registered");
            }
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (nfcStateChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_NFC, false);
                if (eventCount > 0) {
                    if (nfcStateChangedBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "REGISTER");
                        //if (android.os.Build.VERSION.SDK_INT >= 18) {
                            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                                nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                                IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                                appContext.registerReceiver(nfcStateChangedBroadcastReceiver, intentFilter21);
                                //PPApplication.logE("$$$ PhoneProfilesService.onCreate", "registered");
                            }
                        //}
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchNFCSensor(false, false);
                }
            } else
                registerReceiverForRadioSwitchNFCSensor(false, false);
        }
    }

    private void registerReceiverForRadioSwitchMobileDataSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");
        if (!register) {
            if (mobileDataStateChangedContentObserver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "UNREGISTER");
                try {
                    appContext.getContentResolver().unregisterContentObserver(mobileDataStateChangedContentObserver);
                    mobileDataStateChangedContentObserver = null;
                } catch (Exception e) {
                    mobileDataStateChangedContentObserver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (mobileDataStateChangedContentObserver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA, false);
                if (eventCount > 0) {
                    if (mobileDataStateChangedContentObserver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER");
                        mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler());
                        appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchMobileDataSensor(false, false);
                }
            }
            else
                registerReceiverForRadioSwitchMobileDataSensor(false, false);
        }
    }

    private void registerReceiverForAlarmClockSensor(boolean register, boolean checkDatabase) {
        //if (android.os.Build.VERSION.SDK_INT < 21)
        //    return;

        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "xxx");
        if (!register) {
            if (alarmClockBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER ALARM CLOCK");
                try {
                    appContext.unregisterReceiver(alarmClockBroadcastReceiver);
                    alarmClockBroadcastReceiver = null;
                } catch (Exception e) {
                    alarmClockBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered ALARM CLOCK");
            if (alarmClockEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER alarmClockEventEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(alarmClockEventEndBroadcastReceiver);
                    alarmClockEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    alarmClockEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered alarmClockEventEndBroadcastReceiver");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ALARM_CLOCK, false);
                if (eventCount > 0) {
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
                        if (alarmClockBroadcastReceiver == null) {
                            alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
                            IntentFilter intentFilter21 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
                            appContext.registerReceiver(alarmClockBroadcastReceiver, intentFilter21);
                        }
                        //else
                        //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered ALARM CLOCK");
                    //}
                    if (alarmClockEventEndBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER alarmClockEventEndBroadcastReceiver");
                        alarmClockEventEndBroadcastReceiver = new AlarmClockEventEndBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(alarmClockEventEndBroadcastReceiver, intentFilter22);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered alarmClockEventEndBroadcastReceiver");
                } else {
                    registerReceiverForAlarmClockSensor(false, false);
                }
            }
            else
                registerReceiverForAlarmClockSensor(false, false);
        }
    }

    private void registerReceiverForNotificationSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor", "PhoneProfilesService_registerReceiverForNotificationSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "xxx");
        if (!register) {
            if (notificationEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor->UNREGISTER notificationEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForNotificationSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "UNREGISTER notificationEventEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(notificationEventEndBroadcastReceiver);
                    notificationEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    notificationEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "not registered notificationEventEndBroadcastReceiver");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION, false);
                if (eventCount > 0) {
                    if (notificationEventEndBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNotificationSensor->REGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForNotificationSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "REGISTER notificationEventEndBroadcastReceiver");
                        notificationEventEndBroadcastReceiver = new NotificationEventEndBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(notificationEventEndBroadcastReceiver, intentFilter22);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNotificationSensor", "registered notificationEventEndBroadcastReceiver");
                } else {
                    registerReceiverForNotificationSensor(false, false);
                }
            }
            else
                registerReceiverForNotificationSensor(false, false);
        }
    }

    private void registerReceiverForOrientationSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "xxx");
        if (!register) {
            if (orientationEventBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor->UNREGISTER registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "UNREGISTER registerReceiverForOrientationSensor");
                try {
                    appContext.unregisterReceiver(orientationEventBroadcastReceiver);
                    orientationEventBroadcastReceiver = null;
                } catch (Exception e) {
                    orientationEventBroadcastReceiver = null;
                }
            }
            //else
            //   PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "not registered registerReceiverForOrientationSensor");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (smsBroadcastReceiver == null) || (mmsBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                if (eventCount > 0) {
                    if (orientationEventBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForOrientationSensor->REGISTER registerReceiverForOrientationSensor", "PhoneProfilesService_registerReceiverForOrientationSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "REGISTER registerReceiverForOrientationSensor");
                        orientationEventBroadcastReceiver = new OrientationEventBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ORIENTATION_EVENT_BROADCAST_RECEIVER);
                        appContext.registerReceiver(orientationEventBroadcastReceiver, intentFilter22);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForOrientationSensor", "registered registerReceiverForOrientationSensor");
                } else {
                    registerReceiverForOrientationSensor(false, false);
                }
            }
            else
                registerReceiverForOrientationSensor(false, false);
        }
    }

    private void unregisterPPPPExtenderReceiver(int type) {
        Context appContext = getApplicationContext();
        if (type == PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER) {
            if (pppExtenderForceStopApplicationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderForceStopApplicationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderForceStopApplicationBroadcastReceiver);
                    pppExtenderForceStopApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderForceStopApplicationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForceStopApplicationBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER) {
            if (pppExtenderForegroundApplicationBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderForegroundApplicationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderForegroundApplicationBroadcastReceiver);
                    pppExtenderForegroundApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderForegroundApplicationBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForegroundApplicationBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER) {
            if (pppExtenderSMSBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderSMSBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderSMSBroadcastReceiver);
                    pppExtenderSMSBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderSMSBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderSMSBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER) {
            if (pppExtenderCallBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderCallBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderCallBroadcastReceiver);
                    pppExtenderCallBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderCallBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderCallBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER) {
            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
    }

    void registerPPPPExtenderReceiver(boolean register, boolean checkDatabase) {
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
            boolean forceStopAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, null, false, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean applicationsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean orientationAllowed = (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean smsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean callAllowed = (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean lockDeviceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, null, null, false, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED;
            if (forceStopAllowed || applicationsAllowed || orientationAllowed || smsAllowed || callAllowed || lockDeviceAllowed) {
                //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "profile or event allowed");
                int forceStopCount = 0;
                int applicationCount = 0;
                int orientationCount = 0;
                int smsCount = 0;
                int callCount = 0;
                int lockDeviceCount = 0;
                if (checkDatabase) {
                    if (forceStopAllowed) {
                        /*if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_FORCE_STOP, true) > 0) {
                            Profile profile = Profile.getProfileFromSharedPreferences(appContext, PPApplication.SHARED_PROFILE_PREFS_NAME);
                            if (profile._deviceForceStopApplicationChange != 0)
                                ++forceStopCount;
                        }
                        else*/
                            forceStopCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_FORCE_STOP/*, false*/);
                    }
                    if (lockDeviceAllowed) {
                        /*if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_LOCK_DEVICE, true) > 0) {
                            Profile profile = Profile.getProfileFromSharedPreferences(appContext, PPApplication.SHARED_PROFILE_PREFS_NAME);
                            if (profile._lockDevice != 0)
                                ++lockDeviceCount;
                        }
                        else*/
                            lockDeviceCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_LOCK_DEVICE/*, false*/);
                    }
                    if (applicationsAllowed)
                        applicationCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION, false);
                    if (orientationAllowed)
                        orientationCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    if (smsAllowed)
                        smsCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS, false);
                    if (callAllowed)
                        callCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false);
                }
                else {
                    forceStopCount = 1;
                    applicationCount = 1;
                    orientationCount = 1;
                    smsCount = 1;
                    callCount = 1;
                    lockDeviceCount = 1;
                }
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "forceStopCount=" + forceStopCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "applicationCount=" + applicationCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "orientationCount=" + orientationCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "smsCount=" + smsCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "callCount=" + callCount);
                    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "lockDeviceCount=" + lockDeviceCount);
                }*/

                if (forceStopCount > 0) {
                    if (pppExtenderForceStopApplicationBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderForceStopApplicationBroadcastReceiver");
                        pppExtenderForceStopApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
                        appContext.registerReceiver(pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForceStopApplicationBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
                if (lockDeviceCount > 0) {
                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER);

                if ((applicationCount > 0) || (orientationCount > 0)) {
                    if (pppExtenderForegroundApplicationBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderForegroundApplicationBroadcastReceiver");
                        pppExtenderForegroundApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                        intentFilter23.addAction(PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED);
                        appContext.registerReceiver(pppExtenderForegroundApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForegroundApplicationBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);

                if (smsCount > 0) {
                    if (pppExtenderSMSBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderSMSBroadcastReceiver");
                        pppExtenderSMSBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        appContext.registerReceiver(pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderSMSBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);

                if (callCount > 0) {
                    if (pppExtenderCallBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderCallBroadcastReceiver");
                        pppExtenderCallBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        appContext.registerReceiver(pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderCallBroadcastReceiver");

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
            }
        }
    }

    private void registerLocationModeChangedBroadcastReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "xxx");
        if (!register) {
            if (locationModeChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(locationModeChangedBroadcastReceiver);
                    locationModeChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    locationModeChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (locationModeChangedBroadcastReceiver == null)*/) {
                    if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                        // location scanner is enabled
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_GPS, false);
                            eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                        } else {
                            eventScannerCount = 0;
                            eventCount = 0;
                        }
                    } else {
                        eventScannerCount = 0;
                        eventCount = 0;
                    }
                }
                eventCount = eventCount + eventScannerCount;
                if (eventCount > 0) {
                    if (locationModeChangedBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "REGISTER");
                        locationModeChangedBroadcastReceiver = new LocationModeChangedBroadcastReceiver();
                        IntentFilter intentFilter18 = new IntentFilter();
                        intentFilter18.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                        //if (android.os.Build.VERSION.SDK_INT >= 19)
                            intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
                        appContext.registerReceiver(locationModeChangedBroadcastReceiver, intentFilter18);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "registered");
                } else {
                    registerLocationModeChangedBroadcastReceiver(false, false);
                }
            }
            else
                registerLocationModeChangedBroadcastReceiver(false, false);
        }
    }

    private void registerBluetoothStateChangedBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (bluetoothStateChangedBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(bluetoothStateChangedBroadcastReceiver);
                    bluetoothStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    bluetoothStateChangedBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (bluetoothStateChangedBroadcastReceiver == null)*/) {
                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH, false);
                            eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED, false);
                            eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                        } else {
                            eventScannerCount = 0;
                            eventCount = 0;
                        }
                    } else {
                        eventScannerCount = 0;
                        eventCount = 0;
                    }
                }
                eventCount = eventCount + eventScannerCount;
                if (eventCount > 0) {
                    if (bluetoothStateChangedBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
                        //PApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "REGISTER");
                        bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
                        IntentFilter intentFilter15 = new IntentFilter();
                        intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                        appContext.registerReceiver(bluetoothStateChangedBroadcastReceiver, intentFilter15);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "registered");
                } else {
                    registerBluetoothStateChangedBroadcastReceiver(false, false, forceRegister);
                }
            }
            else
                registerBluetoothStateChangedBroadcastReceiver(false, false, forceRegister);
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
                    if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
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

    private void registerBluetoothScannerReceivers(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers", "PhoneProfilesService_registerBluetoothScannerReceivers");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "xxx");
        if (!forceRegister && BluetoothNamePreferenceX.forceRegister)
            return;
        if (!register) {
            if (bluetoothScanReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothScanReceiver");
                try {
                    appContext.unregisterReceiver(bluetoothScanReceiver);
                    bluetoothScanReceiver = null;
                } catch (Exception e) {
                    bluetoothScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothScanReceiver");
            if (bluetoothLEScanReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothLEScanReceiver");
                try {
                    LocalBroadcastManager.getInstance(appContext).unregisterReceiver(bluetoothLEScanReceiver);
                    bluetoothLEScanReceiver = null;
                } catch (Exception e) {
                    bluetoothLEScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothLEScanReceiver");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                if (eventCount > 0) {
                    if (bluetoothScanReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
                        bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_FOUND);
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        appContext.registerReceiver(bluetoothScanReceiver, intentFilter14);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
                    if (bluetoothLEScanReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
                        bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();
                        LocalBroadcastManager.getInstance(appContext).registerReceiver(bluetoothLEScanReceiver,
                                new IntentFilter(PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver"));
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
                } else {
                    registerBluetoothScannerReceivers(false, false, forceRegister);
                }
            }
            else
                registerBluetoothScannerReceivers(false, false, forceRegister);
        }
    }

    private void registerWifiAPStateChangeBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (wifiAPStateChangeBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiAPStateChangeBroadcastReceiver);
                    wifiAPStateChangeBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiAPStateChangeBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                    //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                        // start only for screen On
                        int eventCount = 1;
                        if (checkDatabase/* || (wifiAPStateChangeBroadcastReceiver == null)*/) {
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                        }
                        if (eventCount > 0) {
                            if (wifiAPStateChangeBroadcastReceiver == null) {
                                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
                                //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "REGISTER");
                                wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
                                IntentFilter intentFilter17 = new IntentFilter();
                                intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                                appContext.registerReceiver(wifiAPStateChangeBroadcastReceiver, intentFilter17);
                            }
                            //else
                            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "registered");
                        } else
                            registerWifiAPStateChangeBroadcastReceiver(false, false, forceRegister);
                    } else
                        registerWifiAPStateChangeBroadcastReceiver(false, false, forceRegister);
                } else
                    registerWifiAPStateChangeBroadcastReceiver(false, false, forceRegister);
            }
            else
                registerWifiAPStateChangeBroadcastReceiver(false, false, forceRegister);
        }
    }

    private void registerPowerSaveModeReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver", "PhoneProfilesService_registerPowerSaveModeReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "xxx");
        if (!register) {
            if (powerSaveModeReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->UNREGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(powerSaveModeReceiver);
                    powerSaveModeReceiver = null;
                } catch (Exception e) {
                    powerSaveModeReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED);
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase/* || (powerSaveModeReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                }
                if (eventCount > 0) {
                    //if (android.os.Build.VERSION.SDK_INT >= 21) {
                        if (powerSaveModeReceiver == null) {
                            //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->REGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
                            //PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "REGISTER");
                            powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                            IntentFilter intentFilter10 = new IntentFilter();
                            intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                            appContext.registerReceiver(powerSaveModeReceiver, intentFilter10);
                        }
                        //else
                        //    PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "registered");
                    //}
                } else {
                    registerPowerSaveModeReceiver(false, false);
                }
            }
            else
                registerPowerSaveModeReceiver(false, false);
        }
    }

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
                            if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
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

    private void registerWifiConnectionBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (wifiConnectionBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiConnectionBroadcastReceiver);
                    wifiConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiConnectionBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "not registered");
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
                if (checkDatabase/* || (wifiConnectionBroadcastReceiver == null)*/) {
                    if (profileAllowed) {
                        //profileCount = 0;
                        /*if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, true) > 0) {
                            Profile profile = Profile.getProfileFromSharedPreferences(appContext, PPApplication.SHARED_PROFILE_PREFS_NAME);
                            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                                ++profileCount;
                        }
                        else*/
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID/*, false*/);
                    }
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                                // start only for screen On
                                eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_CONNECTED, false);
                            } else {
                                eventScannerCount = 0;
                                eventCount = 0;
                            }
                        } else {
                            eventScannerCount = 0;
                            eventCount = 0;
                        }
                    }
                    eventCount = eventCount + eventScannerCount;
                }
                if ((profileCount > 0) || (eventCount > 0)) {
                    if (wifiConnectionBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "REGISTER");
                        wifiConnectionBroadcastReceiver = new WifiConnectionBroadcastReceiver();
                        IntentFilter intentFilter13 = new IntentFilter();
                        intentFilter13.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                        appContext.registerReceiver(wifiConnectionBroadcastReceiver, intentFilter13);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "registered");
                } else {
                    registerWifiConnectionBroadcastReceiver(false, false, forceRegister);
                }
            }
            else
                registerWifiConnectionBroadcastReceiver(false, false, forceRegister);
        }
    }

    private void registerWifiScannerReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiScannerReceiver", "PhoneProfilesService_registerWifiScannerReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreferenceX.forceRegister)
            return;
        if (!register) {
            if (wifiScanReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->UNREGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiScanReceiver);
                    wifiScanReceiver = null;
                } catch (Exception e) {
                    wifiScanReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "eventCount="+eventCount);
                if (eventCount > 0) {
                    if (wifiScanReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->REGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "REGISTER");
                        wifiScanReceiver = new WifiScanBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter();
                        intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        appContext.registerReceiver(wifiScanReceiver, intentFilter4);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "registered");
                } else {
                    registerWifiScannerReceiver(false, false, forceRegister);
                }
            }
            else
                registerWifiScannerReceiver(false, false, forceRegister);
        }
    }

    private void registerReceiverForTimeSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForTimeSensor", "PhoneProfilesService_registerReceiverForTimeSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "xxx");
        if (!register) {
            if (eventTimeBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(eventTimeBroadcastReceiver);
                    eventTimeBroadcastReceiver = null;
                } catch (Exception e) {
                    eventTimeBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_TIME, false);
                if (eventCount > 0) {
                    if (eventTimeBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->REGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "REGISTER");
                        eventTimeBroadcastReceiver = new EventTimeBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
                        appContext.registerReceiver(eventTimeBroadcastReceiver, intentFilter23);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "registered");
                } else {
                    registerReceiverForTimeSensor(false, false);
                }
            }
            else
                registerReceiverForTimeSensor(false, false);
        }
    }

    private void registerReceiverForNFCSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForNFCSensor", "PhoneProfilesService_registerReceiverForNFCSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "xxx");
        if (!register) {
            if (nfcEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(nfcEventEndBroadcastReceiver);
                    nfcEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    nfcEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_NFC, false);
                if (eventCount > 0) {
                    if (nfcEventEndBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "REGISTER");
                        nfcEventEndBroadcastReceiver = new NFCEventEndBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(nfcEventEndBroadcastReceiver, intentFilter23);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "registered");
                } else {
                    registerReceiverForNFCSensor(false, false);
                }
            }
            else
                registerReceiverForNFCSensor(false, false);
        }
    }

    private void registerReceiverForCallSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCallSensor", "PhoneProfilesService_registerReceiverForCallSensor");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "xxx");
        if (!register) {
            if (missedCallEventEndBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(missedCallEventEndBroadcastReceiver);
                    missedCallEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    missedCallEventEndBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false);
                if (eventCount > 0) {
                    if (missedCallEventEndBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->REGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "REGISTER");
                        missedCallEventEndBroadcastReceiver = new MissedCallEventEndBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(missedCallEventEndBroadcastReceiver, intentFilter23);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "registered");
                } else {
                    registerReceiverForCallSensor(false, false);
                }
            }
            else
                registerReceiverForCallSensor(false, false);
        }
    }

    private void registerGeofencesScannerReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver", "PhoneProfilesService_registerGeofencesScannerReceiver");
        //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "xxx");
        if (!register) {
            if (geofencesScannerSwitchGPSBroadcastReceiver != null) {
                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->UNREGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(geofencesScannerSwitchGPSBroadcastReceiver);
                    geofencesScannerSwitchGPSBroadcastReceiver = null;
                } catch (Exception e) {
                    geofencesScannerSwitchGPSBroadcastReceiver = null;
                }
            }
            //else
            //    PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "eventCount="+eventCount);
                if (eventCount > 0) {
                    if (geofencesScannerSwitchGPSBroadcastReceiver == null) {
                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->REGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                        //PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "REGISTER");
                        geofencesScannerSwitchGPSBroadcastReceiver = new GeofencesScannerSwitchGPSBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                        appContext.registerReceiver(geofencesScannerSwitchGPSBroadcastReceiver, intentFilter4);
                    }
                    //else
                    //    PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "registered");
                } else {
                    registerGeofencesScannerReceiver(false, false);
                }
            }
            else
                registerGeofencesScannerReceiver(false, false);
        }
    }

    private void cancelWifiWorker(final Context context, final Handler _handler) {
        if (WifiScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelWifiWorker->CANCEL", "PhoneProfilesService_cancelWifiWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelWifiWorker", "CANCEL");
            WifiScanWorker.cancelWork(context, true, _handler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelWifiWorker", "not scheduled");
    }

    void scheduleWifiWorker(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                         //final boolean forScreenOn, final boolean afterEnableWifi,
                         /*final boolean forceStart,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleWifiWorker", "PhoneProfilesService_scheduleWifiWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "xxx");

        if (/*!forceStart &&*/ WifiSSIDPreferenceX.forceRegister)
            return;

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                    cancelWifiWorker(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!WifiScanWorker.isWorkScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false);
                                }
                                if (eventCount > 0) {
                                    if (!WifiScanWorker.isWorkScheduled(appContext)) {
                                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleWifiWorker->SCHEDULE", "PhoneProfilesService_scheduleWifiWorker");
                                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "SCHEDULE");
                                        WifiScanWorker.scheduleWork(appContext, true, handler, true/*, forScreenOn, afterEnableWifi*/);
                                    } else {
                                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiWorker", "scheduled");
                                        if (rescan)
                                            WifiScanWorker.scheduleWork(appContext, true, handler, true/*, forScreenOn, afterEnableWifi*/);
                                    }
                                } else
                                    cancelWifiWorker(appContext, handler);
                            } else
                                cancelWifiWorker(appContext, handler);
                        } else
                            cancelWifiWorker(appContext, handler);
                    }
                    else
                        cancelWifiWorker(appContext, handler);
                }
            }
        });
    }

    private void cancelBluetoothWorker(final Context context, final Handler _handler) {
        if (BluetoothScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelBluetoothWorker->CANCEL", "PhoneProfilesService_cancelBluetoothWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "CANCEL");
            BluetoothScanWorker.cancelWork(context, true, _handler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelBluetoothWorker", "not scheduled");
    }

    private void scheduleBluetoothWorker(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                              /*final boolean forScreenOn, final boolean forceStart,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleBluetoothWorker", "PhoneProfilesService_scheduleBluetoothWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "xxx");

        if (/*!forceStart &&*/ BluetoothNamePreferenceX.forceRegister)
            return;

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                    cancelBluetoothWorker(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!BluetoothScanWorker.isWorkScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false);
                                }
                                if (eventCount > 0) {
                                    if (!BluetoothScanWorker.isWorkScheduled(appContext)) {
                                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleBluetoothWorker->SCHEDULE", "PhoneProfilesService_scheduleBluetoothWorker");
                                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "SCHEDULE");
                                        BluetoothScanWorker.scheduleWork(appContext, true, handler, true/*, forScreenOn*/);
                                    } else {
                                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothWorker", "scheduled");
                                        if (rescan)
                                            BluetoothScanWorker.scheduleWork(appContext, true, handler, true/*, forScreenOn*/);
                                    }
                                } else
                                    cancelBluetoothWorker(appContext, handler);
                            } else
                                cancelBluetoothWorker(appContext, handler);
                        } else
                            cancelBluetoothWorker(appContext, handler);
                    }
                    else
                        cancelBluetoothWorker(appContext, handler);
                }
            }
        });
    }

    private void cancelGeofenceWorker(final Context context, final Handler _handler) {
        if (GeofenceScanWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelGeofenceWorker->CANCEL", "PhoneProfilesService_cancelGeofenceWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "CANCEL");
            GeofenceScanWorker.cancelWork(context, true, _handler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceWorker", "not scheduled");
    }

    private void scheduleGeofenceWorker(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                                    /*final boolean forScreenOn,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleGeofenceWorker", "PhoneProfilesService_scheduleGeofenceWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "xxx");

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                cancelGeofenceWorker(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                            // location scanner is enabled
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!GeofenceScanWorker.isWorkScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                                }
                                if (eventCount > 0) {
                                    if (!GeofenceScanWorker.isWorkScheduled(appContext) || rescan) {
                                        //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleGeofenceWorker->SCHEDULE", "PhoneProfilesService_scheduleGeofenceWorker");
                                        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "SCHEDULE");
                                        synchronized (PPApplication.geofenceScannerMutex) {
                                            if (isGeofenceScannerStarted()) {
                                                //PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceWorker", "updateTransitionsByLastKnownLocation");
                                                getGeofencesScanner().updateTransitionsByLastKnownLocation(false);
                                            }
                                        }
                                        GeofenceScanWorker.scheduleWork(appContext, false, handler, true/*, forScreenOn*/);
                                    }
                                } else
                                    cancelGeofenceWorker(appContext, handler);
                            } else
                                cancelGeofenceWorker(appContext, handler);
                        } else
                            cancelGeofenceWorker(appContext, handler);
                    }
                    else
                        cancelGeofenceWorker(appContext, handler);
                }
            }
        });
    }

    private void cancelSearchCalendarEventsWorker(final Context context, final Handler _handler) {
        if (SearchCalendarEventsWorker.isWorkScheduled(context)) {
            //CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelSearchCalendarEventsWorker->CANCEL", "PhoneProfilesService_cancelSearchCalendarEventsWorker");
            //PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "CANCEL");
            SearchCalendarEventsWorker.cancelWork(context, true, _handler);
        }
        //else
        //    PPApplication.logE("[RJS] PhoneProfilesService.cancelSearchCalendarEventsWorker", "not scheduled");
    }

    private void scheduleSearchCalendarEventsWorker(final boolean schedule, final boolean cancel, final boolean checkDatabase) {
        final Context appContext = getApplicationContext();
        //CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsWorker", "PhoneProfilesService_scheduleSearchCalendarEventsWorker");
        //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "xxx");

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (cancel) {
                    cancelSearchCalendarEventsWorker(appContext, handler);
                }
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        int eventCount = 1;
                        if (checkDatabase/* || (!SearchCalendarEventsWorker.isWorkScheduled())*/) {
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
                        }
                        if (eventCount > 0) {
                            if (!SearchCalendarEventsWorker.isWorkScheduled(appContext)) {
                                //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsWorker->SCHEDULE", "PhoneProfilesService_scheduleSearchCalendarEventsWorker");
                                //PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "SCHEDULE");
                                SearchCalendarEventsWorker.scheduleWork(appContext, true, handler, true);
                            }
                            //else
                            //    PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsWorker", "scheduled");
                        } else {
                            cancelSearchCalendarEventsWorker(appContext, handler);
                        }
                    }
                    else
                        cancelSearchCalendarEventsWorker(appContext, handler);
                }
            }
        });
    }

    private void startGeofenceScanner(boolean start, boolean stop, boolean checkDatabase, boolean forScreenOn) {
        synchronized (PPApplication.geofenceScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService_startGeofenceScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "xxx");
            if (stop) {
                if (isGeofenceScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->STOP", "PhoneProfilesService_startGeofenceScanner");
                    //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "STOP");
                    stopGeofenceScanner();
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    boolean applicationEventLocationScanOnlyWhenScreenIsOn = ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn;
                    if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                        // location scanner is enabled
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !applicationEventLocationScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isGeofenceScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                            }
                            if (eventCount > 0) {
                                if (!isGeofenceScannerStarted()) {
                                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->START", "PhoneProfilesService_startGeofenceScanner");
                                    //PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "START");
                                    if (forScreenOn && PPApplication.isScreenOn &&
                                            applicationEventLocationScanOnlyWhenScreenIsOn)
                                        startGeofenceScanner(true);
                                    else
                                        startGeofenceScanner(false);
                                }
                                //else {
                                //    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "started");
                                //}
                            } else
                                startGeofenceScanner(false, true, false, forScreenOn);
                        } else
                            startGeofenceScanner(false, true, false, forScreenOn);
                    } else
                        startGeofenceScanner(false, true, false, forScreenOn);
                } else
                    startGeofenceScanner(false, true, false, forScreenOn);
            }
        }
    }

    private void startPhoneStateScanner(boolean start, boolean stop, boolean checkDatabase, boolean forceStart,
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
                    //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "STOP");
                    stopPhoneStateScanner();
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED);
                //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "eventAllowed="+eventAllowed);
                if (eventAllowed) {
                    boolean applicationEventMobileCellEnableScanning = ApplicationPreferences.applicationEventMobileCellEnableScanning;
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "scanning enabled=" + applicationEventMobileCellEnableScanning);
                        PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "PhoneStateScanner.forceStart=" + PhoneStateScanner.forceStart);
                    }*/
                    if (applicationEventMobileCellEnableScanning || PhoneStateScanner.forceStart) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isPhoneStateScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                            }
                            //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "eventCount="+eventCount);
                            if (eventCount > 0) {
                                if (!isPhoneStateScannerStarted()) {
                                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->START", "PhoneProfilesService_startPhoneStateScanner");
                                    //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "START");
                                    startPhoneStateScanner();
                                } else {
                                    //PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "started");
                                    if (rescan)
                                        phoneStateScanner.rescanMobileCells();
                                }
                            } else
                                startPhoneStateScanner(false, true, false, forceStart, rescan);
                        } else
                            startPhoneStateScanner(false, true, false, forceStart, rescan);
                    } else
                        startPhoneStateScanner(false, true, false, forceStart, rescan);
                } else
                    startPhoneStateScanner(false, true, false, forceStart, rescan);
            }
        }
    }

    private void startOrientationScanner(boolean start, boolean stop, boolean checkDatabase) {
        synchronized (PPApplication.orientationScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startOrientationScanner", "PhoneProfilesService_startOrientationScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "xxx");
            if (stop) {
                if (isOrientationScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->STOP", "PhoneProfilesService_startOrientationScanner");
                    //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "STOP");
                    stopOrientationScanner();
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isOrientationScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                            }
                            if (eventCount > 0) {
                                if (!isOrientationScannerStarted()) {
                                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->START", "PhoneProfilesService_startOrientationScanner");
                                    //PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "START");
                                    startOrientationScanner();
                                }
                                //else
                                //    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "started");
                            } else
                                startOrientationScanner(false, true, false);
                        } else
                            startOrientationScanner(false, true, false);
                    } else
                        startOrientationScanner(false, true, false);
                } else
                    startOrientationScanner(false, true, false);
            }
        }
    }

    private void startTwilightScanner(boolean start, boolean stop, boolean checkDatabase) {
        synchronized (PPApplication.twilightScannerMutex) {
            Context appContext = getApplicationContext();
            //CallsCounter.logCounter(appContext, "PhoneProfilesService.startTwilightScanner", "PhoneProfilesService_startTwilightScanner");
            //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "xxx");
            if (stop) {
                if (isTwilightScannerStarted()) {
                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startTwilightScanner->STOP", "PhoneProfilesService_startTwilightScanner");
                    //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "STOP");
                    stopTwilightScanner();
                }
                //else
                //    PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "not started");
            }
            if (start) {
                //boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                //        PreferenceAllowed.PREFERENCE_ALLOWED;
                //if (eventAllowed) {
                    //if (ApplicationPreferences.applicationEventOrientationEnableScanning(appContext)) {
                        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        //if ((PPApplication.isScreenOn) || !ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isOrientationScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_TIME_TWILIGHT, false);
                            }
                            if (eventCount > 0) {
                                if (!isTwilightScannerStarted()) {
                                    //CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startTwilightScanner->START", "PhoneProfilesService_startTwilightScanner");
                                    //PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "START");
                                    startTwilightScanner();
                                }
                                //else
                                //    PPApplication.logE("[RJS] PhoneProfilesService.startTwilightScanner", "started");
                            } else
                                startTwilightScanner(false, true, false);
                        //} else
                        //    startOrientationScanner(false, true, false);
                    //} else
                    //    startOrientationScanner(false, true, false);
                //} else
                //    startOrientationScanner(false, true, false);
            }
        }
    }

    private void registerReceiversAndWorkers(boolean fromCommand) {
        //PPApplication.logE("[RJS] PhoneProfilesService.registerReceiversAndWorkers", "xxx");

        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = getApplicationContext();

        // get actual battery status
        Intent batteryStatus = null;
        try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = appContext.registerReceiver(null, filter);
        } catch (Exception ignored) {}
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            BatteryBroadcastReceiver.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            BatteryBroadcastReceiver.batteryPct = Math.round(level / (float) scale * 100);
            BatteryBroadcastReceiver.plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        }

        registerAllTheTimeRequiredReceivers(true);

        // required for battery event
        registerBatteryEventReceiver(true, true);
        registerBatteryChangedReceiver(true, true);

        // required for peripherals event
        registerReceiverForPeripheralsSensor(true, true);

        // required for sms event
        registerReceiverForSMSSensor(true, true);

        // required for calendar event
        registerReceiverForCalendarSensor(true, true);

        // required for radio switch event
        registerReceiverForRadioSwitchMobileDataSensor(true, true);
        registerReceiverForRadioSwitchNFCSensor(true, true);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, true);

        // required for alarm clock event
        registerReceiverForAlarmClockSensor(true, true);

        // required for force stop applications, applications event and orientation event
        registerPPPPExtenderReceiver(true, true);

        // required for location and radio switch event
        registerLocationModeChangedBroadcastReceiver(true, true);

        // required for bluetooth connection type = (dis)connected +
        // radio switch event +
        // bluetooth scanner
        registerBluetoothStateChangedBroadcastReceiver(true, true, false);

        // required for bluetooth connection type = (dis)connected +
        // bluetooth scanner
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);

        // required for bluetooth scanner
        registerBluetoothScannerReceivers(true, true, false);

        // required for wifi scanner
        registerWifiAPStateChangeBroadcastReceiver(true, true, false);

        // required for all scanner events (wifi, bluetooth, location, mobile cells, device orientation) +
        // battery event
        registerPowerSaveModeReceiver(true, true);

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
        registerWifiConnectionBroadcastReceiver(true, true, false);

        // required for wifi scanner
        registerWifiScannerReceiver(true, true, false);

        // required for notification event
        registerReceiverForNotificationSensor(true, true);

        /*
        if (alarmClockBroadcastReceiver != null)
            appContext.unregisterReceiver(alarmClockBroadcastReceiver);
        alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
        IntentFilter intentFilter16 = new IntentFilter();
        // Stock alarms
        // Nexus (?)
        intentFilter16.addAction("com.android.deskclock.ALARM_ALERT");
        //intentFilter16.addAction("com.android.deskclock.ALARM_DISMISS");
        //intentFilter16.addAction("com.android.deskclock.ALARM_DONE");
        //intentFilter16.addAction("com.android.deskclock.ALARM_SNOOZE");
        // stock Android (?)
        intentFilter16.addAction("com.android.alarmclock.ALARM_ALERT");
        // Stock alarm Manufactures
        // Samsung
        intentFilter16.addAction("com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT");
        // HTC
        intentFilter16.addAction("com.htc.android.worldclock.ALARM_ALERT");
        intentFilter16.addAction("com.htc.android.ALARM_ALERT");
        // Sony
        intentFilter16.addAction("com.sonyericsson.alarm.ALARM_ALERT");
        // ZTE
        intentFilter16.addAction("zte.com.cn.alarmclock.ALARM_ALERT");
        // Motorola
        intentFilter16.addAction("com.motorola.blur.alarmclock.ALARM_ALERT");
        // LG
        intentFilter16.addAction("com.lge.clock.ALARM_ALERT");
        // Third-party Alarms
        // Gentle Alarm
        intentFilter16.addAction("com.mobitobi.android.gentlealarm.ALARM_INFO");
        // Sleep As Android
        intentFilter16.addAction("com.urbandroid.sleep.alarmclock.ALARM_ALERT");
        // Alarmdroid (1.13.2)
        intentFilter16.addAction("com.splunchy.android.alarmclock.ALARM_ALERT");
        appContext.registerReceiver(alarmClockBroadcastReceiver, intentFilter16);
        */

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
        registerReceiverForTimeSensor(true, true);

        // register receiver for nfc sensor
        registerReceiverForNFCSensor(true, true);

        // register receiver for call event
        registerReceiverForCallSensor(true, true);

        // register receiver for geofences scanner
        registerGeofencesScannerReceiver(true, true);

        // required for orientation event
        registerReceiverForOrientationSensor(true, true);

        //Log.e("------ PhoneProfilesService.registerReceiversAndWorkers", "fromCommand="+fromCommand);
        WifiScanWorker.initialize(appContext, !fromCommand);
        BluetoothScanWorker.initialize(appContext, !fromCommand);

        scheduleWifiWorker(true,  true, /*false, false, false,*/ false);
        scheduleBluetoothWorker(true,  true, /*false, false,*/ false);
        scheduleSearchCalendarEventsWorker(true, true, true);

        startGeofenceScanner(true, true, true, false);
        scheduleGeofenceWorker(true,  true, /*false,*/ false);

        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
        startTwilightScanner(true, true, true);
    }

    private void unregisterReceiversAndWorkers() {
        //PPApplication.logE("[RJS] PhoneProfilesService.unregisterReceiversAndWorkers", "xxx");
        registerAllTheTimeRequiredReceivers(false);
        registerBatteryEventReceiver(false, false);
        registerBatteryChangedReceiver(false, false);
        registerReceiverForPeripheralsSensor(false, false);
        registerReceiverForSMSSensor(false, false);
        registerReceiverForCalendarSensor(false, false);
        registerReceiverForRadioSwitchMobileDataSensor(false, false);
        registerReceiverForRadioSwitchNFCSensor(false, false);
        registerReceiverForRadioSwitchAirplaneModeSensor(false, false);
        registerReceiverForAlarmClockSensor(false, false);
        registerPPPPExtenderReceiver(false, false);
        registerLocationModeChangedBroadcastReceiver(false, false);
        registerBluetoothStateChangedBroadcastReceiver(false, false, false);
        //registerBluetoothConnectionBroadcastReceiver(false, true, false, false);
        registerBluetoothScannerReceivers(false, false, false);
        registerWifiAPStateChangeBroadcastReceiver(false, false, false);
        registerPowerSaveModeReceiver(false, false);
        //registerWifiStateChangedBroadcastReceiver(false, false, false);
        registerWifiConnectionBroadcastReceiver(false, false, false);
        registerWifiScannerReceiver(false, false, false);
        registerReceiverForTimeSensor(false, false);
        registerReceiverForNFCSensor(false, false);
        registerReceiverForCallSensor(false, false);
        registerGeofencesScannerReceiver(false, false);
        registerReceiverForNotificationSensor(false, false);
        registerReceiverForOrientationSensor(false, false);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        scheduleWifiWorker(false,  false, /*false, false, false,*/ false);
        scheduleBluetoothWorker(false,  false, /*false, false,*/ false);
        scheduleSearchCalendarEventsWorker(false, true, false);

        scheduleGeofenceWorker(false,  false, /*false,*/ false);
        startGeofenceScanner(false, true, false, false);

        startPhoneStateScanner(false, true, false, false, false);
        startOrientationScanner(false, true, false);
        startTwilightScanner(false, true, false);
    }

    private void reregisterReceiversAndWorkers() {
        //PPApplication.logE("[RJS] PhoneProfilesService.reregisterReceiversAndWorkers", "xxx");
        registerBatteryEventReceiver(true, true);
        registerBatteryChangedReceiver(true, true);
        registerReceiverForPeripheralsSensor(true, true);
        registerReceiverForSMSSensor(true, true);
        registerReceiverForCalendarSensor(true, true);
        registerReceiverForRadioSwitchMobileDataSensor(true, true);
        registerReceiverForRadioSwitchNFCSensor(true, true);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, true);
        registerReceiverForAlarmClockSensor(true, true);
        registerPPPPExtenderReceiver(true, true);
        registerLocationModeChangedBroadcastReceiver(true, true);
        registerBluetoothStateChangedBroadcastReceiver(true, true, false);
        //registerBluetoothConnectionBroadcastReceiver(true, true, true, false);
        registerBluetoothScannerReceivers(true, true, false);
        registerWifiAPStateChangeBroadcastReceiver(true, true, false);
        registerPowerSaveModeReceiver(true, true);
        //registerWifiStateChangedBroadcastReceiver(true, true, false);
        registerWifiConnectionBroadcastReceiver(true, true, false);
        registerWifiScannerReceiver(true, true, false);
        registerReceiverForTimeSensor(true, true);
        registerReceiverForNFCSensor(true, true);
        registerReceiverForCallSensor(true, true);
        registerGeofencesScannerReceiver(true, true);
        registerReceiverForOrientationSensor(true, true);

        scheduleWifiWorker(true,  true, /*false, false, false,*/ false);
        scheduleBluetoothWorker(true,  true, /*false, false,*/ false);
        scheduleSearchCalendarEventsWorker(true, false, true);

        startGeofenceScanner(true, true, true, false);
        scheduleGeofenceWorker(true,  true, /*false,*/ false);

        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
        startTwilightScanner(true, true, true);
    }

    // start service for first start
    private void doForFirstStart(Intent intent/*, int flags, int startId*/) {
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        final Context appContext = getApplicationContext();

        serviceHasFirstStart = true;

        boolean deactivateProfile = false;
        boolean activateProfiles = false;
        boolean startOnBoot = false;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            deactivateProfile = intent.getBooleanExtra(EXTRA_DEACTIVATE_PROFILE, false);
            activateProfiles = intent.getBooleanExtra(EXTRA_ACTIVATE_PROFILES, false);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        if (PPApplication.logEnabled()) {
            if (deactivateProfile)
                PPApplication.logE("----- PhoneProfilesService.doForFirstStart", "EXTRA_DEACTIVATE_PROFILE");
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
        final boolean _deactivateProfile = deactivateProfile;
        PPApplication.startHandlerThread("PhoneProfilesService.doForFirstStart");
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

                    PPApplication.createNotificationChannels(appContext);

                    if (_deactivateProfile) {
                        DatabaseHandler.getInstance(appContext).deactivateProfile();
                        ActivateProfileHelper.updateGUI(appContext, false, true);
                    }

                    // is called from PPApplication
                    //PPApplication.initRoot();
                    if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                        // grant root
                        PPApplication.isRootGranted();
                    }
                    else {
                        synchronized (PPApplication.rootMutex) {
                            if (PPApplication.rootMutex.rootChecked) {
                                try {
                                    Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                                } catch (Exception ignored) {}
                            }
                            else {
                                try {
                                    Crashlytics.setString(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, "ask for grant disabled");
                                } catch (Exception ignored) {}
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

                    PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application not started, start it");

                    //Permissions.clearMergedPermissions(appContext);

                    if (!TonesHandler.isToneInstalled(/*TonesHandler.TONE_ID,*/ appContext)) {
                        TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext);
                        DatabaseHandler.getInstance(appContext).fixPhoneProfilesSilentInProfiles();
                    }

                    //TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, appContext, false);
                    ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, true);

                    ActivateProfileHelper.setLockScreenDisabled(appContext, false);

                    AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        ActivateProfileHelper.setRingerVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_RING));
                        ActivateProfileHelper.setNotificationVolume(appContext, audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
                        RingerModeChangeReceiver.setRingerMode(appContext, audioManager);
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                        PPNotificationListenerService.setZenMode(appContext, audioManager);
                        InterruptionFilterChangedBroadcastReceiver.setZenMode(appContext, audioManager);
                    }

                    PPPExtenderBroadcastReceiver.setApplicationInForeground(appContext, "");

                    EventPreferencesCall.setEventCallEventType(appContext, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                    EventPreferencesCall.setEventCallEventTime(appContext, 0);
                    EventPreferencesCall.setEventCallPhoneNumber(appContext, "");

                    // show info notification
                    ImportantInfoNotification.showInfoNotification(appContext);
                    IgnoreBatteryOptimizationNotification.showNotification(appContext);

                    // must be first
                    createContactsCache(appContext, true);
                    //must be seconds, this ads groups int contacts
                    createContactGroupsCache(appContext, true);

                    dataWrapper.fillProfileList(false, false);
                    for (Profile profile : dataWrapper.profileList)
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);
                    Profile.setActivatedProfileForDuration(appContext, 0);

                    dataWrapper.fillEventList();
                    for (Event event : dataWrapper.eventList)
                        StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                    GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(appContext);
                    LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                    //PPNotificationListenerService.clearNotifiedPackages(appContext);

                    DatabaseHandler.getInstance(appContext).deleteAllEventTimelines();
                    DatabaseHandler.getInstance(appContext).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

                    MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, _startOnBoot || _startOnPackageReplace);

                    BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, true);
                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                    // not needed clearConnectedDevices(.., true) call it

                    // duration > 30 seconds because in it is 3 x 10 seconds sleep
                    BluetoothConnectedDevices.getConnectedDevices(appContext);

                    WifiScanWorker.setScanRequest(appContext, false);
                    WifiScanWorker.setWaitForResults(appContext, false);
                    WifiScanWorker.setWifiEnabledForScan(appContext, false);

                    BluetoothScanWorker.setScanRequest(appContext, false);
                    BluetoothScanWorker.setLEScanRequest(appContext, false);
                    BluetoothScanWorker.setWaitForResults(appContext, false);
                    BluetoothScanWorker.setWaitForLEResults(appContext, false);
                    BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
                    BluetoothScanWorker.setScanKilled(appContext, false);

                    registerReceiversAndWorkers(false);
                    DonationBroadcastReceiver.setAlarm(appContext);

                    PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application started");

                    if (_startOnBoot)
                        dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_START_ON_BOOT, null, null, null, 0);
                    else
                    if (_activateProfiles)
                        dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_START, null, null, null, 0);

                    //dataWrapper.invalidateDataWrapper();

                    if (PPApplication.logEnabled()) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart END");
                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneProfilesService.doForFirstStart");
                    }

                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "START");
                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());

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

                    if (Event.getGlobalEventsRunning()) {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "global event run is enabled, first start events");

                        if (_activateProfiles) {
                            if (!DataWrapper.getIsManualProfileActivation(false/*, appContext*/)) {
                                ////// unblock all events for first start
                                //     that may be blocked in previous application run
                                dataWrapper.pauseAllEvents(false, false);
                            }
                        }

                        dataWrapper.firstStartEvents(true, false);
                        dataWrapper.updateNotificationAndWidgets(true, true);
                    } else {
                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "global event run is not enabled, manually activate profile");

                        if (_activateProfiles) {
                            ////// unblock all events for first start
                            //     that may be blocked in previous application run
                            dataWrapper.pauseAllEvents(true, false);
                        }

                        dataWrapper.activateProfileOnBoot();
                        dataWrapper.updateNotificationAndWidgets(true, true);
                    }

                    /*// set waitForEndOfStart to false only when is not enqueued packageReplacedWork
                    try {
                        WorkManager workInstance = WorkManager.getInstance(appContext);
                        ListenableFuture<List<WorkInfo>> statuses = workInstance.getWorkInfosByTag("packageReplacedWork");
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            boolean running = false;
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                running = (state == WorkInfo.State.ENQUEUED) || (state == WorkInfo.State.RUNNING);
                            }

                            PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "packageReplacedWork - running="+running);

                            if (!running) {
                                instance.setWaitForEndOfStart(false);
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());
                            }
                        } catch (ExecutionException e) {
                            Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                            instance.setWaitForEndOfStart(false);
                        } catch (InterruptedException e) {
                            Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                            instance.setWaitForEndOfStart(false);
                        }
                    } catch (Exception e) {
                        Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                        instance.setWaitForEndOfStart(false);
                    }
                    //}*/

                    setWaitForEndOfStart(false);

                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - handler", "END");

                    //if (ApplicationPreferences.applicationPackageReplaced(appContext)) {
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
                            WorkManager workManager = WorkManager.getInstance(appContext);
                            workManager.enqueueUniqueWork("packageReplacedWork", ExistingWorkPolicy.REPLACE, worker);
                        } catch (Exception ignored) {}
                    //}

                    /*// work for first start events or activate profile on boot
                    Data workData = new Data.Builder()
                            .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START)
                            .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, _activateProfiles)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                    .addTag("delayedWorkAfterFirstStartWork")
                                    .setInputData(workData)
                                    //.setInitialDelay(3, TimeUnit.SECONDS)
                                    .build();
                    try {
                        WorkManager workManager = WorkManager.getInstance(appContext);
                        workManager.enqueueUniqueWork("delayedWorkAfterFirstStartWork", ExistingWorkPolicy.REPLACE, worker);
                    } catch (Exception ignored) {}*/

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

        PPApplication.showProfileNotification(true, true/*, false*/);
        if (!serviceHasFirstStart)
            doForFirstStart(intent);

        return START_STICKY;
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
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SWITCH_KEYGUARD");

                            Context appContext = getApplicationContext();

                            //boolean isScreenOn;
                            //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

                            boolean secureKeyguard;
                            if (keyguardManager == null)
                                keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (keyguardManager != null) {
                                secureKeyguard = keyguardManager.isKeyguardSecure();
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
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REGISTER_RECEIVERS_AND_WORKERS");
                            registerReceiversAndWorkers(true);
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS");
                            unregisterReceiversAndWorkers();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_REREGISTER_RECEIVERS_AND_WORKERS");
                            reregisterReceiversAndWorkers();
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_SIMULATE_RINGING_CALL");
                            doSimulatingRingingCall(intent);
                        }
                        //else
                        //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                        //    doSimulatingNotificationTone(intent);
                        else
                        if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_START_STOP_SCANNER");
                            final boolean forScreenOn = intent.getBooleanExtra(EXTRA_FOR_SCREEN_ON, false);
                            //PPApplication.logE("$$$ PhoneProfilesService.doCommand", "forScreenOn="+forScreenOn);
                            switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                                case PPApplication.SCANNER_START_GEOFENCE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_GEOFENCE_SCANNER");
                                    startGeofenceScanner(true, true, true, false);
                                    scheduleGeofenceWorker(true, true, /*false,*/ false);
                                    break;
                                case PPApplication.SCANNER_STOP_GEOFENCE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_GEOFENCE_SCANNER");
                                    startGeofenceScanner(false, true, false, false);
                                    scheduleGeofenceWorker(false, false, /*false,*/ false);
                                    break;
                                case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, true, true);
                                    break;
                                case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                                    startOrientationScanner(false, true, false);
                                    break;
                                case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                                    PhoneStateScanner.forceStart = false;
                                    startPhoneStateScanner(true, true, true, false, false);
                                    break;
                                case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                                    startPhoneStateScanner(false, true, false, false, false);
                                    break;
                                case PPApplication.SCANNER_START_TWILIGHT_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_START_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, true, true);
                                    break;
                                case PPApplication.SCANNER_STOP_TWILIGHT_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_STOP_TWILIGHT_SCANNER");
                                    startTwilightScanner(false, true, false);
                                    break;
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    registerWifiConnectionBroadcastReceiver(true, true, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                    registerWifiScannerReceiver(true, true, false);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                    registerWifiConnectionBroadcastReceiver(true, false, true);
                                    //registerWifiStateChangedBroadcastReceiver(true, false, true);
                                    registerWifiAPStateChangeBroadcastReceiver(true, false, true);
                                    registerWifiScannerReceiver(true, false, true);
                                    break;
                                case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                    registerBluetoothScannerReceivers(true, true, false);
                                    break;
                                case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                                    registerBluetoothStateChangedBroadcastReceiver(true, false, true);
                                    registerBluetoothScannerReceivers(true, false, true);
                                    break;
                                case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_WIFI_SCANNER");
                                    registerWifiConnectionBroadcastReceiver(true, true, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                    registerWifiScannerReceiver(true, true, false);
                                    scheduleWifiWorker(true, true, /*forScreenOn, false, false,*/ true);
                                    break;
                                case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                    registerBluetoothScannerReceivers(true, true, false);
                                    scheduleBluetoothWorker(true, true, /*forScreenOn, false,*/ true);
                                    break;
                                case PPApplication.SCANNER_RESTART_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_PHONE_STATE_SCANNER");
                                    PhoneStateScanner.forceStart = false;
                                    startPhoneStateScanner(true, true, true, false, true);
                                    break;
                                case PPApplication.SCANNER_FORCE_START_PHONE_STATE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                                    PhoneStateScanner.forceStart = true;
                                    startPhoneStateScanner(true, false, false, true, false);
                                    break;
                                case PPApplication.SCANNER_RESTART_GEOFENCE_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_GEOFENCE_SCANNER");
                                    registerLocationModeChangedBroadcastReceiver(true, true);
                                    startGeofenceScanner(true, true, true, forScreenOn);
                                    scheduleGeofenceWorker(true, true, /*forScreenOn,*/ true);
                                    break;
                                case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                                    startOrientationScanner(true, false, true);
                                    break;
                                case PPApplication.SCANNER_RESTART_TWILIGHT_SCANNER:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_TWILIGHT_SCANNER");
                                    startTwilightScanner(true, false, true);
                                    break;
                                case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
                                    PPApplication.logE("$$$ PhoneProfilesService.doCommand", "SCANNER_RESTART_ALL_SCANNERS");
                                    registerWifiConnectionBroadcastReceiver(true, true, false);
                                    //registerWifiStateChangedBroadcastReceiver(true, true, false);
                                    registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                    registerWifiScannerReceiver(true, true, false);
                                    scheduleWifiWorker(true, true, /*forScreenOn, false, false,*/ true);

                                    //registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                    registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                    registerBluetoothScannerReceivers(true, true, false);
                                    scheduleBluetoothWorker(true, true, /*forScreenOn, false,*/ true);

                                    PhoneStateScanner.forceStart = false;
                                    startPhoneStateScanner(true, true, true, false, true);

                                    registerLocationModeChangedBroadcastReceiver(true, true);
                                    startGeofenceScanner(true, false, true, forScreenOn);
                                    scheduleGeofenceWorker(true, true, /*forScreenOn,*/ true);

                                    startOrientationScanner(true, false, true);

                                    startTwilightScanner(true, false, true);
                                    break;
                            }
                        }
                        else
                        if (intent.getBooleanExtra(EXTRA_RESTART_EVENTS, false)) {
                            PPApplication.logE("$$$ PhoneProfilesService.doCommand", "EXTRA_RESTART_EVENTS");
                            final boolean unblockEventsRun = intent.getBooleanExtra(EXTRA_UNBLOCK_EVENTS_RUN, false);
                            //final boolean reactivateProfile = intent.getBooleanExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, false);
                            final Context appContext = getApplicationContext();
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                            //dataWrapper.restartEvents(unblockEventsRun, true, reactivateProfile, false, false);
                            dataWrapper.restartEventsWithRescan(unblockEventsRun, false, false, false);
                            //dataWrapper.invalidateDataWrapper();
                        }

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
        PPApplication.showProfileNotification(true, false/*, false*/);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from PhoneProfilesService.obConfigurationChanged");
        ActivateProfileHelper.updateGUI(getApplicationContext(), true, true);
    }

    //------------------------

    // contacts and contact groups cache -----------------

    public static void createContactsCache(Context context, boolean clear)
    {
        if (clear) {
            if (contactsCache != null)
                contactsCache.clearCache();
            contactsCache = new ContactsCache();
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
            contactGroupsCache = new ContactGroupsCache();
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
                                          boolean refresh/*, boolean cleared*/)
    {
        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "xxx");

        /*
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;
        */

        //PPApplication.logE("PhoneProfilesService.showProfileNotification", "no lockRefresh");

        final Context appContext = this; //dataWrapper.context.getApplicationContext();

        //if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBar(appContext))
        //if (true)
        //{
            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "show enabled");

            // intent to LauncherActivity, for click on notification
            Intent launcherIntent = new Intent(ACTION_START_LAUNCHER_FROM_NOTIFICATION);
            /*Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
            // clear all opened activities
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // setup startupSource
            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);*/
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
                            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper, false, appContext);
                        else
                            pName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                        if (pName.equals(pNameNotification)) {
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "activated profile NOT changed");
                            return;
                        }
                    }
                }
            }

            /* moved to PhoneProfilesService.showProfileNotification()
            if (!cleared) {
                boolean clear = false;
                if (Build.MANUFACTURER.equals("HMD Global"))
                    // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
                    clear = true;
                if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
                    // clear it for redraw icon in "Glance view" for LG with Android 9
                    clear = true;
                if (clear) {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                }
            }
            */

            /*if (PPApplication.logEnabled()) {
                if (refresh)
                    PPApplication.logE("PhoneProfilesService._showProfileNotification", "refresh");
                else
                    PPApplication.logE("PhoneProfilesService._showProfileNotification", "activated profile changed");
            }*/

            PendingIntent pIntentRE = null;
            if (Event.getGlobalEventsRunning() &&
                    PPApplication.getApplicationStarted(true)) {
                // intent for restart events
                Intent intentRE = new Intent(appContext, RestartEventsFromGUIActivity.class);
                intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                pIntentRE = PendingIntent.getActivity(appContext, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            boolean notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar;
            //boolean notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(appContext);
            //boolean notificationDarkBackground = ApplicationPreferences.notificationDarkBackground(appContext);
            boolean notificationUseDecoration = ApplicationPreferences.notificationUseDecoration;
            boolean notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator;
            boolean notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen;
            String notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle;
            String notificationTextColor = ApplicationPreferences.notificationTextColor;
            String notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor;
            int notificationBackgroundCustomColor = ApplicationPreferences.notificationBackgroundCustomColor;
            boolean notificationNightMode = ApplicationPreferences.notificationNightMode;

            Notification.Builder notificationBuilder;

            /*
            boolean miui = (PPApplication.romManufacturer != null) &&
                    (PPApplication.romManufacturer.compareToIgnoreCase("xiaomi") == 0)// &&
                    //(android.os.Build.VERSION.SDK_INT >= 24);
            */
            //boolean miui = PPApplication.romIsMIUI;

            //Log.e("****** PhoneProfilesService.showProfileNotification", "miui="+miui);

            RemoteViews contentView = null;
            RemoteViews contentViewLarge;

            /*UiModeManager uiModeManager = (UiModeManager) appContext.getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager != null) {
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            }*/

            boolean useDecorator = (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) || (Build.VERSION.SDK_INT >= 26);
            useDecorator = useDecorator && notificationUseDecoration;

            int nightModeFlags =
                appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            int useNightColor = 0;
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
            if (Build.VERSION.SDK_INT < 29)
                useDecorator = useDecorator && (!notificationNightMode) && notificationBackgroundColor.equals("0");

            boolean profileIconExists = true;
            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_miui_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "miui");
            }
            else
            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_emui_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "emui");
            }
            else
            if (PPApplication.deviceIsSamsung) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_samsung_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "samsung");
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_no_decorator);
                    else {
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                        profileIconExists = false;
                    }
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "others");
            }
            //}

            boolean isIconResourceID;
            String iconIdentifier;
            String pName;
            Spannable profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "profile="+profile);
            if (profile != null)
            {
                //PPApplication.logE("PhoneProfilesService.showProfileNotification", "profile != null");
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, dataWrapper, false, appContext);
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
                    if (notificationPrefIndicator)
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
                            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon from picture");
                                notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                            } else {
                                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create icon default icon");
                                //iconSmallResource = appContext.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", appContext.getPackageName());
                                //if (iconSmallResource == 0)
                                //    iconSmallResource = R.drawable.ic_profile_default;
                                iconSmallResource = R.drawable.ic_profile_default_notify_color;
                                try {
                                    //noinspection ConstantConditions
                                    iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                } catch (Exception e) {
                                    Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                                }
                                notificationBuilder.setSmallIcon(iconSmallResource);
                            }
                        } else {
                            // native icon
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "colorful icon in status bar is disabled");

                            //iconSmallResource = appContext.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", appContext.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception e) {
                                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                            }
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }

                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        if (profileIconExists) {
                            if (contentView != null)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        }
                    } else {
                        //PPApplication.logE("PhoneProfilesService._showProfileNotification", "icon has NOT changed color");

                        if (notificationStatusBarStyle.equals("0")) {
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "enabled is colorful icon in status bar");
                            //iconSmallResource = appContext.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", appContext.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default;
                            iconSmallResource = R.drawable.ic_profile_default_notify_color;
                            try {
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                            } catch (Exception e) {
                                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                            }
                        }
                        else {
                            //PPApplication.logE("PhoneProfilesService._showProfileNotification", "colorful icon in status bar is disabled");
                            //iconSmallResource = appContext.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", appContext.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                //noinspection ConstantConditions
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception e) {
                                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
                            }
                        }
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        //int iconLargeResource = appContext.getResources().getIdentifier(iconIdentifier, "drawable", appContext.getPackageName());
                        //if (iconLargeResource == 0)
                        //    iconLargeResource = R.drawable.ic_profile_default;
                        int iconLargeResource = Profile.getIconResource(iconIdentifier);
                        //Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                        Bitmap largeIcon = BitmapManipulator.getBitmapFromResource(iconLargeResource, true, appContext);
                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        if (profileIconExists) {
                            if (contentView != null)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
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
                    if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
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
                }
            }
            else {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "create empty icon");
                notificationBuilder.setSmallIcon(R.drawable.ic_profile_default_notify);
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                if (profileIconExists) {
                    if (contentView != null)
                        contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                }
            }

            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if (contentView != null)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);
            notificationBuilder.setContentTitle(profileName);
            notificationBuilder.setContentText(profileName);

            try {
                if ((preferencesIndicator != null) && (notificationPrefIndicator)) {
                    contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
                    contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.VISIBLE);
                }
                else
                    //contentViewLarge.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);
                    contentViewLarge.setViewVisibility(R.id.notification_activated_profile_pref_indicator, View.GONE);
            } catch (Exception e) {
                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
            }

            if (Event.getGlobalEventsRunning() &&
                    PPApplication.getApplicationStarted(true)) {

                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationBackgroundColor="+notificationBackgroundColor);

                int restartEventsId;
                if (notificationBackgroundColor.equals("1") || notificationBackgroundColor.equals("3")) {
                    // dark or black
                    restartEventsId = R.drawable.ic_widget_restart_events_dark;
                }
                else
                if (notificationBackgroundColor.equals("5")) {
                    // custom color
                    if (ColorUtils.calculateLuminance(notificationBackgroundCustomColor) < 0.23)
                        restartEventsId = R.drawable.ic_widget_restart_events_dark;
                    else
                        restartEventsId = R.drawable.ic_widget_restart_events;
                }
                else {
                    // native
                    if (Build.VERSION.SDK_INT >= 29) {
                        if (useNightColor == 1)
                            restartEventsId = R.drawable.ic_widget_restart_events_dark;
                        else
                            restartEventsId = R.drawable.ic_widget_restart_events;
                    }
                    else
                        restartEventsId = R.drawable.ic_widget_restart_events;
                }

                //contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                contentViewLarge.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);

                if (contentView != null) {
                    //contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                    contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, restartEventsId);
                    contentView.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);
                }
            }
            /*else {
                if (contentView != null)
                    contentView.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);

                contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);
            }*/

            //if (Build.VERSION.SDK_INT < 29) {
                //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "background not 2 or 4");
                switch (notificationBackgroundColor) {
                    case "3":
                        //if (!notificationNightMode || (useNightColor == 1)) {
                            int color = ContextCompat.getColor(this, R.color.notificationBlackBackgroundColor);
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                        /*}
                        else {
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                        }*/
                        break;
                    case "1":
                        //if (!notificationNightMode || (useNightColor == 1)) {
                            color = ContextCompat.getColor(this, R.color.notificationDarkBackgroundColor);
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                        /*}
                        else {
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                        }*/
                        break;
                    case "5":
                        //if (!notificationNightMode || (useNightColor == 1)) {
                            //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "background color 5");
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", notificationBackgroundCustomColor);
                        /*}
                        else {
                            contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                            if (contentView != null)
                                contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", Color.TRANSPARENT);
                        }*/
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
                    if (Build.VERSION.SDK_INT < 25)
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                    else
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name,
                                ContextCompat.getColorStateList(appContext, R.color.widget_text_color_black));
                    if (contentView != null) {
                        if (Build.VERSION.SDK_INT < 25)
                            contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                        else
                            contentView.setTextColor(R.id.notification_activated_profile_name,
                                    ContextCompat.getColorStateList(appContext, R.color.widget_text_color_black));
                    }
                } else if (notificationTextColor.equals("2")/* || notificationDarkBackground*/) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "before set text color");
                    if (Build.VERSION.SDK_INT < 25)
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                    else
                        contentViewLarge.setTextColor(R.id.notification_activated_profile_name,
                                ContextCompat.getColorStateList(appContext, R.color.widget_text_color_white));
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "after set text color");
                    if (contentView != null)
                        if (Build.VERSION.SDK_INT < 25)
                            contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                        else
                            contentView.setTextColor(R.id.notification_activated_profile_name,
                                    ContextCompat.getColorStateList(appContext, R.color.widget_text_color_white));
                }

                //PPApplication.logE("[CUST] PhoneProfilesService._showProfileNotification", "after set text color");
            //}

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                if (useDecorator)
                    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                //if (contentView != null) {
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
                //}
                //else
                //    notificationBuilder.setCustomContentView(contentViewLarge);
            }
            else {
                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "setContent");
                notificationBuilder.setContent(contentViewLarge);
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "useDecorator=" + useDecorator);
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "ApplicationPreferences.notificationShowButtonExit(appContext)=" + ApplicationPreferences.notificationShowButtonExit(appContext));
            }*/

            if (/*(Build.VERSION.SDK_INT >= 24) &&*/
                    (ApplicationPreferences.notificationShowButtonExit) &&
                    useDecorator) {
                // add action button to stop application

                //PPApplication.logE("PhoneProfilesService._showProfileNotification", "add action button");

                // intent to LauncherActivity, for click on notification
                Intent exitAppIntent = new Intent(appContext, ExitApplicationActivity.class);
                // clear all opened activities
                exitAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pExitAppIntent = PendingIntent.getActivity(appContext, 0, exitAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Action.Builder actionBuilder;
                actionBuilder = new Notification.Action.Builder(
                        R.drawable.ic_action_exit_app_white,
                        appContext.getString(R.string.menu_exit),
                        pExitAppIntent);
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
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    phoneProfilesNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                /*} else {
                //    setAlarmForNotificationCancel(appContext);
                }*/

                //if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                    //PPApplication.logE("PhoneProfilesService._showProfileNotification", "startForeground()");
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                    //runningInForeground = true;
                /*}
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                }*/
            }
        //}
        /*else
        {
            if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }*/
    }

    void showProfileNotification(final boolean refresh, boolean forServiceStart/*, final boolean cleared*/) {
        //if (Build.VERSION.SDK_INT >= 26) {
            //if (BuildConfig.DEBUG)
            //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","forServiceStart="+forServiceStart);

        boolean clear = false;
        if (Build.MANUFACTURER.equals("HMD Global"))
            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
            clear = true;
        if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
            // clear it for redraw icon in "Glance view" for LG with Android 9
            clear = true;
        if (clear) {
            clearProfileNotification();
        }

        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","refresh="+(clear || refresh));

        //if (!runningInForeground) {
            if (forServiceStart) {
                //if (!isServiceRunningInForeground(appContext, PhoneProfilesService.class)) {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                _showProfileNotification(null, false, dataWrapper, true/*, cleared*/);
                //dataWrapper.invalidateDataWrapper();
            }
        //}

        //if (BuildConfig.DEBUG)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","before run handler");

        long now = SystemClock.elapsedRealtime();

        if (clear || refresh || ((now - PPApplication.lastRefreshOfProfileNotification) >= PPApplication.DURATION_FOR_GUI_REFRESH))
        {
            PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","refresh");

            final boolean _clear = clear;
            PPApplication.startHandlerThreadProfileNotification();
            final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                    Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                    //PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification", "_showProfileNotification()");
                    _showProfileNotification(profile, true, dataWrapper, _clear || refresh  /*, cleared*/);
                    //dataWrapper.invalidateDataWrapper();
                }
            });
        }
        else {
            PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","do not refresh");

            ShowProfileNotificationBroadcastReceiver.setAlarm(getApplicationContext());
        }

        PPApplication.lastRefreshOfProfileNotification = SystemClock.elapsedRealtime();
    }

    void clearProfileNotification(/*boolean onlyEmpty*/)
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
                    stopForeground(true);
                /*else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                }*/
            } catch (Exception e) {
                Log.e("PhoneProfilesService._showProfileNotification", Log.getStackTraceString(e));
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

    /*
    private void setAlarmForNotificationCancel(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
            return;

        String notificationStatusBarCancel = ApplicationPreferences.notificationStatusBarCancel(context);

        if (notificationStatusBarCancel.isEmpty() || notificationStatusBarCancel.equals("0"))
            return;

        //Intent intent = new Intent(_context, NotificationCancelAlarmBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_CANCEL_ALARM_BROADCAST_RECEIVER);
        //intent.setClass(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long time = SystemClock.elapsedRealtime() + Integer.valueOf(notificationStatusBarCancel) * 1000;
            // not needed exact for removing notification
            /if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            //    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
            //if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
            //else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
        }
    }
    */

    //--------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        //PPApplication.logE("$$$ disableKeyguard","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                keyguardLock.disableKeyguard();
            } catch (Exception e) {
                Log.e("PhoneProfilesService", Log.getStackTraceString(e));
            }
        }
    }

    private void reenableKeyguard()
    {
        //PPApplication.logE("$$$ reenableKeyguard","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                keyguardLock.reenableKeyguard();
            } catch (Exception e) {
                Log.e("PhoneProfilesService", Log.getStackTraceString(e));
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
            } catch (Settings.SettingNotFoundException ignored) {
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
        } catch (Settings.SettingNotFoundException ignored) {
        }
        return wifiSleepPolicy == Settings.Global.WIFI_SLEEP_POLICY_NEVER;
    }

    private void startGeofenceScanner(boolean resetUseGPS) {
        //PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "xxx");
        if (geofencesScanner != null) {
            geofencesScanner.disconnect();
            geofencesScanner = null;
        }

        geofencesScanner = new GeofencesScanner(getApplicationContext());
        //PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "geofencesScanner="+geofencesScanner);
        /*if (instance != null) {
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "instance==this? " + (instance == this));
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
            PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService.getGeofencesScanner()=" + PhoneProfilesService.getInstance().getGeofencesScanner());
        }*/
        geofencesScanner.connect(resetUseGPS);
    }

    private void stopGeofenceScanner() {
        //PPApplication.logE("PhoneProfilesService.stopGeofenceScanner", "xxx");
        if (geofencesScanner != null) {
            geofencesScanner.disconnect();
            geofencesScanner = null;
        }
    }

    boolean isGeofenceScannerStarted() {
        return (geofencesScanner != null);
    }

    GeofencesScanner getGeofencesScanner() {
        return geofencesScanner;
    }

    //--------------------------------------------------------------------------

    // Phone state ----------------------------------------------------------------

    private void startPhoneStateScanner() {
        if (phoneStateScanner != null) {
            phoneStateScanner.disconnect();
            phoneStateScanner = null;
        }

        phoneStateScanner = new PhoneStateScanner(getApplicationContext());
        phoneStateScanner.connect();
    }

    private void stopPhoneStateScanner() {
        if (phoneStateScanner != null) {
            phoneStateScanner.disconnect();
            phoneStateScanner = null;
        }
    }

    boolean isPhoneStateScannerStarted() {
        return (phoneStateScanner != null);
    }


    PhoneStateScanner getPhoneStateScanner() {
        return phoneStateScanner;
    }

    //--------------------------------------------------------------------------

    // Device orientation ----------------------------------------------------------------

    private void startOrientationScanner() {
        if (mStartedOrientationSensors)
            stopListeningOrientationSensors();

        startListeningOrientationSensors();
    }

    private void stopOrientationScanner() {
        stopListeningOrientationSensors();
    }

    boolean isOrientationScannerStarted() {
        return mStartedOrientationSensors;
    }

    @SuppressLint("NewApi")
    private void startListeningOrientationSensors() {
        //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors", "mStartedOrientationSensors="+mStartedOrientationSensors);
        if (!mStartedOrientationSensors) {
            orientationScanner = new OrientationScanner();
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
            if (DatabaseHandler.getInstance(getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) == 0)
                return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
            if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
                interval *= 2;

            if (PPApplication.accelerometerSensor != null) {
                if (PPApplication.accelerometerSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.accelerometerSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.accelerometerSensor, 1000000 * interval, handler);
            }
            if (PPApplication.magneticFieldSensor != null) {
                if (PPApplication.magneticFieldSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.magneticFieldSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.magneticFieldSensor, 1000000 * interval, handler);
            }

            if (PPApplication.lightSensor != null) {
                PPApplication.handlerThreadOrientationScanner.mMaxLightDistance = PPApplication.lightSensor.getMaximumRange();
                if (PPApplication.lightSensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.lightSensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.lightSensor, 1000000 * interval, handler);
            }

            if (PPApplication.proximitySensor != null) {
                PPApplication.handlerThreadOrientationScanner.mMaxProximityDistance = PPApplication.proximitySensor.getMaximumRange();
                if (PPApplication.proximitySensor.getFifoMaxEventCount() > 0)
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.proximitySensor, 200000 * interval, 1000000 * interval, handler);
                else
                    PPApplication.sensorManager.registerListener(orientationScanner, PPApplication.proximitySensor, 1000000 * interval, handler);
            }

            //Sensor orientation = PPApplication.getOrientationSensor(this);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","orientation="+orientation);
            mStartedOrientationSensors = true;

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
            PPApplication.sensorManager.unregisterListener(orientationScanner);
            removeOrientationSensorAlarm(getApplicationContext());
            orientationScanner = null;
            //PPApplication.sensorManager = null;
        }
        mStartedOrientationSensors = false;
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
        } catch (Exception ignored) {}
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsOrientationSensorWork");
            workManager.cancelAllWorkByTag("elapsedAlarmsOrientationSensorWork");
        } catch (Exception ignored) {}
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
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            }
        }
    }

    // Twilight scanner ----------------------------------------------------------------

    private void startTwilightScanner() {
        if (twilightScanner != null) {
            twilightScanner.stop();
            twilightScanner = null;
        }

        twilightScanner = new TwilightScanner(getApplicationContext());
        twilightScanner.start();
    }

    private void stopTwilightScanner() {
        if (twilightScanner != null) {
            twilightScanner.stop();
            twilightScanner = null;
        }
    }

    private boolean isTwilightScannerStarted() {
        return (twilightScanner != null);
    }


    TwilightScanner getTwilightScanner() {
        return twilightScanner;
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
            int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
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
                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                       if (!ActivateProfileHelper.isAudibleRinging(oldRingerMode, oldZenMode)) {
                           simulateRinging = true;
                           stream = AudioManager.STREAM_ALARM;
                           //PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (1)");
                       }
                    }

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
                    if (android.os.Build.VERSION.SDK_INT < 23) {
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
                    }

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
                    notificationMediaPlayer.release();
                } catch (Exception ignored) {}
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
                        ringingMediaPlayer.setAudioStreamType(stream);
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
                        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                        workManager.cancelUniqueWork("disableInternalChangeWork");
                        workManager.cancelAllWorkByTag("disableInternalChangeWork");
                        workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                    } catch (Exception ignored) {}

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
                    ringingMediaPlayer.release();
                } catch (Exception ignored) {}
                ringingMediaPlayer = null;

                try {
                    if (ringingCallIsSimulating)
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldMediaVolume, 0);
                } catch (Exception ignored) {
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
            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
            workManager.cancelUniqueWork("disableInternalChangeWork");
            workManager.cancelAllWorkByTag("disableInternalChangeWork");
            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
        } catch (Exception ignored) {}

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
                        notificationMediaPlayer.setAudioStreamType(usedNotificationStream);
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
                notificationMediaPlayer.release();
            } catch (Exception ignored) {}
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
                } catch (Exception ignored) {}
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
                int ringerMode = ApplicationPreferences.prefRingerMode;
                int zenMode = ApplicationPreferences.prefZenMode;
                boolean isAudible = ActivateProfileHelper.isAudibleRinging(ringerMode, zenMode/*, false*/);
                //PPApplication.logE("PhoneProfilesService.playNotificationSound", "isAudible="+isAudible);
                if (isAudible) {

                    Uri notificationUri = Uri.parse(notificationSound);

                    try {
                        RingerModeChangeReceiver.internalChange = true;

                        notificationMediaPlayer = new MediaPlayer();
                        notificationMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
                                        notificationMediaPlayer.release();
                                    } catch (Exception ignored) {
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
                                    WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                                    workManager.cancelUniqueWork("disableInternalChangeWork");
                                    workManager.cancelAllWorkByTag("disableInternalChangeWork");
                                    workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                                } catch (Exception ignored) {}

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
                            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                            workManager.cancelUniqueWork("disableInternalChangeWork");
                            workManager.cancelAllWorkByTag("disableInternalChangeWork");
                            workManager.enqueueUniqueWork("disableInternalChangeWork", ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                        } catch (Exception ignored) {}

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

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxx");

        if (PPApplication.screenTimeoutHandler != null) {
            PPApplication.screenTimeoutHandler.post(new Runnable() {
                public void run() {
                    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
                    ActivateProfileHelper.removeKeepScreenOnView();
                }
            });
        }

        super.onTaskRemoved(rootIntent);
    }

}
