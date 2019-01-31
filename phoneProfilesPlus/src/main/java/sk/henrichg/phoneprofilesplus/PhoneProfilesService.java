package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Timer;
import java.util.TimerTask;


public class PhoneProfilesService extends Service
                                    implements SensorEventListener/*,
                                    AudioManager.OnAudioFocusChangeListener*/
{
    private static volatile PhoneProfilesService instance = null;
    private boolean serviceHasFirstStart = false;
    private boolean serviceRunning = false;
    private boolean runningInForeground = false;

    private KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock = null;

    BrightnessView brightnessView = null;
    BrightnessView keepScreenOnView = null;

    LockDeviceActivity lockDeviceActivity = null;
    int screenTimeoutBeforeDeviceLock = 0;

    private ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;

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
    private NotificationCancelAlarmBroadcastReceiver notificationCancelAlarmBroadcastReceiver = null;
    private NFCEventEndBroadcastReceiver nfcEventEndBroadcastReceiver = null;
    private RunApplicationWithDelayBroadcastReceiver runApplicationWithDelayBroadcastReceiver = null;
    private MissedCallEventEndBroadcastReceiver missedCallEventEndBroadcastReceiver = null;
    private StartEventNotificationBroadcastReceiver startEventNotificationBroadcastReceiver = null;
    private GeofencesScannerSwitchGPSBroadcastReceiver geofencesScannerSwitchGPSBroadcastReceiver = null;
    private LockDeviceActivityFinishBroadcastReceiver lockDeviceActivityFinishBroadcastReceiver = null;
    private PostDelayedBroadcastReceiver postDelayedBroadcastReceiver = null;
    private AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    private AlarmClockEventEndBroadcastReceiver alarmClockEventEndBroadcastReceiver = null;

    private PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    private DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;

    private SettingsContentObserver settingsContentObserver = null;
    private MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;

    static final String ACTION_EVENT_TIME_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.EventTimeBroadcastReceiver";
    static final String ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.EventCalendarBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.EventDelayStartBroadcastReceiver";
    static final String ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.EventDelayEndBroadcastReceiver";
    static final String ACTION_PROFILE_DURATION_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.ProfileDurationAlarmBroadcastReceiver";
    static final String ACTION_SMS_EVENT_END_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.SMSEventEndBroadcastReceiver";
    private static final String ACTION_NOTIFICATION_CANCEL_ALARM_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.NotificationCancelAlarmBroadcastReceiver";
    static final String ACTION_NFC_EVENT_END_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.NFCEventEndBroadcastReceiver";
    static final String ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.RunApplicationWithDelayBroadcastReceiver";
    static final String ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.MissedCallEventEndBroadcastReceiver";
    static final String ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.StartEventNotificationBroadcastReceiver";
    static final String ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.GeofencesScannerSwitchGPSBroadcastReceiver";
    static final String ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.LockDeviceActivityFinishBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.AlarmClockBroadcastReceiver";
    static final String ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER = "sk.henrichg.phoneprofilesplus.AlarmClockEventEndBroadcastReceiver";

    //static final String EXTRA_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_START_ON_PACKAGE_REPLACE = "start_on_package_replace";
    static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_INITIALIZE_START = "initialize_start";
    static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";
    static final String EXTRA_SIMULATE_RINGING_CALL = "simulate_ringing_call";
    static final String EXTRA_OLD_RINGER_MODE = "old_ringer_mode";
    static final String EXTRA_OLD_SYSTEM_RINGER_MODE = "old_system_ringer_mode";
    static final String EXTRA_OLD_ZEN_MODE = "old_zen_mode";
    static final String EXTRA_OLD_RINGTONE = "old_ringtone";
    //static final String EXTRA_SIMULATE_NOTIFICATION_TONE = "simulate_notification_tone";
    //static final String EXTRA_OLD_NOTIFICATION_TONE = "old_notification_tone";
    static final String EXTRA_OLD_SYSTEM_RINGER_VOLUME = "old_system_ringer_volume";
    static final String EXTRA_REGISTER_RECEIVERS_AND_JOBS = "register_receivers_and_jobs";
    static final String EXTRA_UNREGISTER_RECEIVERS_AND_JOBS = "unregister_receivers_and_jobs";
    static final String EXTRA_REREGISTER_RECEIVERS_AND_JOBS = "reregister_receivers_and_jobs";
    static final String EXTRA_FOR_SCREEN_ON = "for_screen_on";
    //static final String EXTRA_START_LOCATION_UPDATES = "start_location_updates";
    //private static final String EXTRA_STOP_LOCATION_UPDATES = "stop_location_updates";
    static final String EXTRA_RESTART_EVENTS = "restart_events";

    //-----------------------

    private GeofencesScanner geofencesScanner = null;

    private SensorManager mOrientationSensorManager = null;
    private boolean mStartedOrientationSensors = false;

    private int mEventCountSinceGZChanged = 0;
    private static final int MAX_COUNT_GZ_CHANGE = 5;
    private float mGravity[] = new float[3];
    private float mGeomagnetic[] = new float[3];
    private float mMaxProximityDistance;
    private float mGravityZ = 0;  //gravity acceleration along the z axis

    private PhoneStateScanner phoneStateScanner = null;

    private static final int DEVICE_ORIENTATION_UNKNOWN = 0;
    private static final int DEVICE_ORIENTATION_RIGHT_SIDE_UP = 3;
    private static final int DEVICE_ORIENTATION_LEFT_SIDE_UP = 4;
    private static final int DEVICE_ORIENTATION_UP_SIDE_UP = 5;
    private static final int DEVICE_ORIENTATION_DOWN_SIDE_UP = 6;
    public static final int DEVICE_ORIENTATION_HORIZONTAL = 9;

    private static final int DEVICE_ORIENTATION_DISPLAY_UP = 1;
    private static final int DEVICE_ORIENTATION_DISPLAY_DOWN = 2;

    private static final int DEVICE_ORIENTATION_DEVICE_IS_NEAR = 7;
    private static final int DEVICE_ORIENTATION_DEVICE_IS_FAR = 8;

    int mDisplayUp = DEVICE_ORIENTATION_UNKNOWN;
    int mSideUp = DEVICE_ORIENTATION_UNKNOWN;
    int mDeviceDistance = DEVICE_ORIENTATION_UNKNOWN;

    private int tmpSideUp = DEVICE_ORIENTATION_UNKNOWN;
    private long tmpSideTimestamp = 0;

    //------------------------

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

    //public static SipManager mSipManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "android.os.Build.VERSION.SDK_INT=" + android.os.Build.VERSION.SDK_INT);

        synchronized (PhoneProfilesService.class) {
            instance = this;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        runningInForeground = false;
        //ApplicationPreferences.forceNotUseAlarmClock = false;

        if (Build.VERSION.SDK_INT >= 26)
            // show empty notification to avoid ANR
            showProfileNotification();

        Context appContext = getApplicationContext();

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(this));
            }
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval(appContext));
        } catch (Exception ignored) {}

        /*
        ApplicationPreferences.getSharedPreferences(appContext);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(ActivateProfileActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ActivateProfileListFragment.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ActivateProfileListAdapter.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true);
        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS, true);
        editor.putBoolean(ProfilePreferencesActivity.PREF_START_TARGET_HELPS_SAVE, true);
        editor.putBoolean(EventPreferencesActivity.PREF_START_TARGET_HELPS, true);
        editor.apply();
        */

        //PPApplication.initPhoneProfilesServiceMessenger(appContext);

        keyguardManager = (KeyguardManager)appContext.getSystemService(Activity.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OK created");

        /*if (PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            PPApplication.logE("$$$ PhoneProfilesService.onCreate", "doForFirstStart");
            doForFirstStart(null);
        }
        else {
            showProfileNotification();
            stopSelf();
        }*/
        //if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
            showProfileNotification();
        //    stopSelf();
        //}
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxx");

        unregisterReceiversAndJobs();

        stopSimulatingRingingCall(/*true*/);
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        try {
            if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(getApplicationContext()))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        } catch (Exception ignored) {
        }

        synchronized (PhoneProfilesService.class) {
            instance = null;
        }

        serviceHasFirstStart = false;
        serviceRunning = false;
        runningInForeground = false;

        super.onDestroy();
    }

    static PhoneProfilesService getInstance() {
        return instance;
    }

    boolean getServiceHasFirstStart() {
        return serviceHasFirstStart;
    }

    private void registerAllTheTimeRequiredReceivers(boolean register) {
        final Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
        PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "xxx");
        if (!register) {
            if (shutdownBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER shutdown");
                try {
                    appContext.unregisterReceiver(shutdownBroadcastReceiver);
                    shutdownBroadcastReceiver = null;
                } catch (Exception e) {
                    shutdownBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered shutdown");
            if (screenOnOffReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER screen on off");
                try {
                    appContext.unregisterReceiver(screenOnOffReceiver);
                    screenOnOffReceiver = null;
                } catch (Exception e) {
                    screenOnOffReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered screen on off");
            if (interruptionFilterChangedReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER interruption filter");
                try {
                    appContext.unregisterReceiver(interruptionFilterChangedReceiver);
                    interruptionFilterChangedReceiver = null;
                } catch (Exception e) {
                    interruptionFilterChangedReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered interruption filter");
            if (phoneCallBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER phone call");
                try {
                    appContext.unregisterReceiver(phoneCallBroadcastReceiver);
                    phoneCallBroadcastReceiver = null;
                } catch (Exception e) {
                    phoneCallBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered phone call");
            if (ringerModeChangeReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER ringer mode change");
                try {
                    appContext.unregisterReceiver(ringerModeChangeReceiver);
                    ringerModeChangeReceiver = null;
                } catch (Exception e) {
                    ringerModeChangeReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered ringer mode change");
            if (settingsContentObserver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER settings content observer");
                try {
                    appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
                    settingsContentObserver = null;
                } catch (Exception e) {
                    settingsContentObserver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered settings content observer");
            if (deviceIdleModeReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER device idle mode");
                try {
                    appContext.unregisterReceiver(deviceIdleModeReceiver);
                    deviceIdleModeReceiver = null;
                } catch (Exception e) {
                    deviceIdleModeReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered device idle mode");
            if (bluetoothConnectionBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER bluetooth connection");
                try {
                    appContext.unregisterReceiver(bluetoothConnectionBroadcastReceiver);
                    bluetoothConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    bluetoothConnectionBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayStartBroadcastReceiver");
            if (eventDelayStartBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayStartBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventDelayStartBroadcastReceiver);
                    eventDelayStartBroadcastReceiver = null;
                } catch (Exception e) {
                    eventDelayStartBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayStartBroadcastReceiver");
            if (eventDelayEndBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER eventDelayEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventDelayEndBroadcastReceiver);
                    eventDelayEndBroadcastReceiver = null;
                } catch (Exception e) {
                    eventDelayEndBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered eventDelayEndBroadcastReceiver");
            if (profileDurationAlarmBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER profileDurationAlarmBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(profileDurationAlarmBroadcastReceiver);
                    profileDurationAlarmBroadcastReceiver = null;
                } catch (Exception e) {
                    profileDurationAlarmBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered profileDurationAlarmBroadcastReceiver");
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
            if (runApplicationWithDelayBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER runApplicationWithDelayBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(runApplicationWithDelayBroadcastReceiver);
                    runApplicationWithDelayBroadcastReceiver = null;
                } catch (Exception e) {
                    runApplicationWithDelayBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered runApplicationWithDelayBroadcastReceiver");
            if (startEventNotificationBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER startEventNotificationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(startEventNotificationBroadcastReceiver);
                    startEventNotificationBroadcastReceiver = null;
                } catch (Exception e) {
                    startEventNotificationBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered startEventNotificationBroadcastReceiver");
            if (lockDeviceActivityFinishBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER lockDeviceActivityFinishBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(lockDeviceActivityFinishBroadcastReceiver);
                    lockDeviceActivityFinishBroadcastReceiver = null;
                } catch (Exception e) {
                    lockDeviceActivityFinishBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered postDelayedBroadcastReceiver");
            if (postDelayedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER postDelayedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER postDelayedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(postDelayedBroadcastReceiver);
                    postDelayedBroadcastReceiver = null;
                } catch (Exception e) {
                    postDelayedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered pppExtenderBroadcastReceiver");
            if (pppExtenderBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->UNREGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "UNREGISTER pppExtenderBroadcastReceiverr");
                try {
                    appContext.unregisterReceiver(pppExtenderBroadcastReceiver);
                    pppExtenderBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "not registered pppExtenderBroadcastReceiver");
        }
        if (register) {
            if (shutdownBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER shutdown", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER shutdown");
                shutdownBroadcastReceiver = new ShutdownBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SHUTDOWN);
                appContext.registerReceiver(shutdownBroadcastReceiver, intentFilter5);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered shutdown");

            // required for Lock device, Hide notification in lock screen, screen timeout +
            // screen on/off event + rescan wifi, bluetooth, location, mobile cells
            if (screenOnOffReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER screen on off", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER screen on off");
                screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
                IntentFilter intentFilter5 = new IntentFilter();
                intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
                appContext.registerReceiver(screenOnOffReceiver, intentFilter5);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered screen on off");

            // required for Do not disturb ringer mode
            if (interruptionFilterChangedReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER interruption filter", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER interruption filter");
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
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered interruption filter");

            // required for unlink ring and notification volume
            if (phoneCallBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER phone call");
                phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
                IntentFilter intentFilter6 = new IntentFilter();
                intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                appContext.registerReceiver(phoneCallBroadcastReceiver, intentFilter6);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered phone call");

            // required for unlink ring and notification volume
            if (ringerModeChangeReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER ringer mode change", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER ringer mode change");
                ringerModeChangeReceiver = new RingerModeChangeReceiver();
                IntentFilter intentFilter7 = new IntentFilter();
                intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                appContext.registerReceiver(ringerModeChangeReceiver, intentFilter7);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered ringer mode change");

            // required for unlink ring and notification volume
            if (settingsContentObserver == null) {
                try {
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                    PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER settings content observer");
                    //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
                    settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
                    appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
                } catch (Exception ignored) {}
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered settings content observer");

            // required for start EventsHandler in idle maintenance window
            if (deviceIdleModeReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER device idle mode", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER device idle mode");
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
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered device idle mode");

            // required for (un)register connected bluetooth devices
            if (bluetoothConnectionBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER bluetooth connection", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER bluetooth connection");

                bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                //intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                appContext.registerReceiver(bluetoothConnectionBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered bluetooth connection");

            if (eventDelayStartBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayStartBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayStartBroadcastReceiver");

                eventDelayStartBroadcastReceiver = new EventDelayStartBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                appContext.registerReceiver(eventDelayStartBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered eventDelayStartBroadcastReceiver");

            if (eventDelayEndBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER eventDelayEndBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER eventDelayEndBroadcastReceiver");

                eventDelayEndBroadcastReceiver = new EventDelayEndBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                appContext.registerReceiver(eventDelayEndBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered eventDelayEndBroadcastReceiver");

            if (profileDurationAlarmBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER profileDurationAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER profileDurationAlarmBroadcastReceiver");

                profileDurationAlarmBroadcastReceiver = new ProfileDurationAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(profileDurationAlarmBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered profileDurationAlarmBroadcastReceiver");

            if (notificationCancelAlarmBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER notificationCancelAlarmBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER notificationCancelAlarmBroadcastReceiver");

                notificationCancelAlarmBroadcastReceiver = new NotificationCancelAlarmBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_NOTIFICATION_CANCEL_ALARM_BROADCAST_RECEIVER);
                appContext.registerReceiver(notificationCancelAlarmBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered notificationCancelAlarmBroadcastReceiver");

            if (runApplicationWithDelayBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER runApplicationWithDelayBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER runApplicationWithDelayBroadcastReceiver");

                runApplicationWithDelayBroadcastReceiver = new RunApplicationWithDelayBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                appContext.registerReceiver(runApplicationWithDelayBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered runApplicationWithDelayBroadcastReceiver");

            if (startEventNotificationBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER startEventNotificationBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER startEventNotificationBroadcastReceiver");

                startEventNotificationBroadcastReceiver = new StartEventNotificationBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                appContext.registerReceiver(startEventNotificationBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered startEventNotificationBroadcastReceiver");

            if (lockDeviceActivityFinishBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER lockDeviceActivityFinishBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER lockDeviceActivityFinishBroadcastReceiver");

                lockDeviceActivityFinishBroadcastReceiver = new LockDeviceActivityFinishBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                appContext.registerReceiver(lockDeviceActivityFinishBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered lockDeviceActivityFinishBroadcastReceiver");

            if (postDelayedBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER postDelayedBroadcastReceiver", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER postDelayedBroadcastReceiver");

                postDelayedBroadcastReceiver = new PostDelayedBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_REMOVE_BRIGHTNESS_VIEW);
                //intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE);
                //intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_DISABLE_SCREEN_TIMEOUT_INTERNAL_CHANGE_TO_FALSE);
                intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_HANDLE_EVENTS);
                intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_RESTART_EVENTS);
                intentFilter14.addAction(PostDelayedBroadcastReceiver.ACTION_START_WIFI_SCAN);
                appContext.registerReceiver(postDelayedBroadcastReceiver, intentFilter14);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered postDelayedBroadcastReceiver");

            if (pppExtenderBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER pppExtenderBroadcastReceiver", "PhoneProfilesService_pppExtenderBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER pppExtenderBroadcastReceiver");

                pppExtenderBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                IntentFilter intentFilter14 = new IntentFilter();
                intentFilter14.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_CONNECTED);
                appContext.registerReceiver(pppExtenderBroadcastReceiver, intentFilter14,
                        PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered pppExtenderBroadcastReceiver");
        }
    }

    private void registerBatteryEventReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryEventReceiver", "PhoneProfilesService_registerBatteryEventReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "xxx");
        if (!register) {
            if (batteryEventReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryEventReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryEventReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(batteryEventReceiver);
                    batteryEventReceiver = null;
                } catch (Exception e) {
                    batteryEventReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "not registered");
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
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                }
                if (eventCount > 0) {
                    if (batteryEventReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryEventReceiver->REGISTER", "PhoneProfilesService_registerBatteryEventReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "REGISTER");
                        batteryEventReceiver = new BatteryBroadcastReceiver();
                        IntentFilter intentFilter1 = new IntentFilter();
                        intentFilter1.addAction(Intent.ACTION_POWER_CONNECTED);
                        intentFilter1.addAction(Intent.ACTION_POWER_DISCONNECTED);
                        intentFilter1.addAction(Intent.ACTION_BATTERY_LOW);
                        intentFilter1.addAction(Intent.ACTION_BATTERY_OKAY);
                        appContext.registerReceiver(batteryEventReceiver, intentFilter1);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryChangedReceiver", "PhoneProfilesService_registerBatteryChangedReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "xxx");
        if (!register) {
            if (batteryChangeLevelReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChangedReceiver->UNREGISTER", "PhoneProfilesService_registerBatteryChangedReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(batteryChangeLevelReceiver);
                    batteryChangeLevelReceiver = null;
                } catch (Exception e) {
                    batteryChangeLevelReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "not registered");
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
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    //eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                    // get non-stopped events with battery sensor with levels > 0 and < 100
                    batteryLevelCount = DatabaseHandler.getInstance(appContext).getBatteryEventWithLevelCount();
                }
                // get power save mode from PPP settings (tested will be value "1" = 5%, "2" = 15%)
                String powerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal(appContext);
                if (powerSaveModeInternal.equals("1") || powerSaveModeInternal.equals("2") || ((batteryLevelCount > 0) && (eventCount > 0))) {
                    if (batteryChangeLevelReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBatteryChangedReceiver->REGISTER", "PhoneProfilesService_registerBatteryChangedReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "REGISTER");
                        batteryChangeLevelReceiver = new BatteryBroadcastReceiver();
                        IntentFilter intentFilter1_1 = new IntentFilter();
                        intentFilter1_1.addAction(Intent.ACTION_BATTERY_CHANGED);
                        appContext.registerReceiver(batteryChangeLevelReceiver, intentFilter1_1);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "xxx");
        if (!register) {
            if (headsetPlugReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER headset plug");
                try {
                    appContext.unregisterReceiver(headsetPlugReceiver);
                    headsetPlugReceiver = null;
                } catch (Exception e) {
                    headsetPlugReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered headset plug");
            if (dockConnectionBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->UNREGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "UNREGISTER dock connection");
                try {
                    appContext.unregisterReceiver(dockConnectionBroadcastReceiver);
                    dockConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    dockConnectionBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "not registered dock connection");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (headsetPlugReceiver == null) || (dockConnectionBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL, false);
                if (eventCount > 0) {
                    if (headsetPlugReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER headset plug", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER headset plug");
                        headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
                        IntentFilter intentFilter2 = new IntentFilter();
                        intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
                        intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
                        intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
                        appContext.registerReceiver(headsetPlugReceiver, intentFilter2);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered headset plug");
                    if (dockConnectionBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor->REGISTER dock connection", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "REGISTER dock connection");
                        dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
                        IntentFilter intentFilter12 = new IntentFilter();
                        intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
                        intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
                        appContext.registerReceiver(dockConnectionBroadcastReceiver, intentFilter12);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "registered dock connection");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForSMSSensor", "PhoneProfilesService_registerReceiverForSMSSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "xxx");
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
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->UNREGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "UNREGISTER smsEventEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(smsEventEndBroadcastReceiver);
                    smsEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    smsEventEndBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered smsEventEndBroadcastReceiver");
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER smsEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER smsEventEndBroadcastReceiver");
                        smsEventEndBroadcastReceiver = new SMSEventEndBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(smsEventEndBroadcastReceiver, intentFilter22);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered smsEventEndBroadcastReceiver");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor", "PhoneProfilesService_registerReceiverForCalendarSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "xxx");
        if (!register) {
            if (calendarProviderChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER calendarProviderChangedBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(calendarProviderChangedBroadcastReceiver);
                    calendarProviderChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    calendarProviderChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
            if (eventCalendarBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER eventCalendarBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(eventCalendarBroadcastReceiver);
                    eventCalendarBroadcastReceiver = null;
                } catch (Exception e) {
                    eventCalendarBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered eventCalendarBroadcastReceiver");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (calendarProviderChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
                if (eventCount > 0) {
                    if (calendarProviderChangedBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER calendarProviderChangedBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER calendarProviderChangedBroadcastReceiver");
                        calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
                        intentFilter23.addDataScheme("content");
                        intentFilter23.addDataAuthority("com.android.calendar", null);
                        intentFilter23.setPriority(Integer.MAX_VALUE);
                        appContext.registerReceiver(calendarProviderChangedBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
                    if (eventCalendarBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER eventCalendarBroadcastReceiver", "PhoneProfilesService_registerReceiverForCalendarSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER eventCalendarBroadcastReceiver");
                        eventCalendarBroadcastReceiver = new EventCalendarBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                        appContext.registerReceiver(eventCalendarBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered calendarProviderChangedBroadcastReceiver");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "xxx");
        if (!register) {
            if (airplaneModeStateChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(airplaneModeStateChangedBroadcastReceiver);
                    airplaneModeStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    airplaneModeStateChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (airplaneModeStateChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE, false);
                if (eventCount > 0) {
                    if (airplaneModeStateChangedBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "REGISTER");
                        airplaneModeStateChangedBroadcastReceiver = new AirplaneModeStateChangedBroadcastReceiver();
                        IntentFilter intentFilter19 = new IntentFilter();
                        intentFilter19.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        appContext.registerReceiver(airplaneModeStateChangedBroadcastReceiver, intentFilter19);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "xxx");
        if (!register) {
            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                if (nfcStateChangedBroadcastReceiver != null) {
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "UNREGISTER");
                    try {
                        appContext.unregisterReceiver(nfcStateChangedBroadcastReceiver);
                        nfcStateChangedBroadcastReceiver = null;
                    } catch (Exception e) {
                        nfcStateChangedBroadcastReceiver = null;
                    }
                }
                else
                    PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "not registered");
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "REGISTER");
                        //if (android.os.Build.VERSION.SDK_INT >= 18) {
                            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                                nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                                IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                                appContext.registerReceiver(nfcStateChangedBroadcastReceiver, intentFilter21);
                                //PPApplication.logE("$$$ PhoneProfilesService.onCreate", "registered");
                            }
                        //}
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchNFCSensor(false, false);
                }
            } else
                registerReceiverForRadioSwitchNFCSensor(false, false);
        }
    }

    private void registerReceiverForRadioSwitchMobileDataSensor(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");
        if (!register) {
            if (mobileDataStateChangedContentObserver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "UNREGISTER");
                try {
                    appContext.getContentResolver().unregisterContentObserver(mobileDataStateChangedContentObserver);
                    mobileDataStateChangedContentObserver = null;
                } catch (Exception e) {
                    mobileDataStateChangedContentObserver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (mobileDataStateChangedContentObserver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA, false);
                if (eventCount > 0) {
                    if (mobileDataStateChangedContentObserver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER");
                        mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler());
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
                        //else
                        //    appContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchMobileDataSensor(false, false);
                }
            }
            else
                registerReceiverForRadioSwitchMobileDataSensor(false, false);
        }
    }

    private void registerReceiverForAlarmClockSensor(boolean register, boolean checkDatabase) {
        if (android.os.Build.VERSION.SDK_INT < 21)
            return;

        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "xxx");
        if (!register) {
            if (alarmClockBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER ALARM CLOCK");
                try {
                    appContext.unregisterReceiver(alarmClockBroadcastReceiver);
                    alarmClockBroadcastReceiver = null;
                } catch (Exception e) {
                    alarmClockBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered ALARM CLOCK");
            if (alarmClockEventEndBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->UNREGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "UNREGISTER alarmClockEventEndBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(alarmClockEventEndBroadcastReceiver);
                    alarmClockEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    alarmClockEventEndBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "not registered alarmClockEventEndBroadcastReceiver");
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER ALARM CLOCK", "PhoneProfilesService_registerReceiverForAlarmClockSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER ALARM CLOCK");
                        if (alarmClockBroadcastReceiver == null) {
                            alarmClockBroadcastReceiver = new AlarmClockBroadcastReceiver();
                            IntentFilter intentFilter21 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
                            appContext.registerReceiver(alarmClockBroadcastReceiver, intentFilter21);
                        } else
                            PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered ALARM CLOCK");
                    //}
                    if (alarmClockEventEndBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForAlarmClockSensor->REGISTER alarmClockEventEndBroadcastReceiver", "PhoneProfilesService_registerReceiverForSMSSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "REGISTER alarmClockEventEndBroadcastReceiver");
                        alarmClockEventEndBroadcastReceiver = new AlarmClockEventEndBroadcastReceiver();
                        IntentFilter intentFilter22 = new IntentFilter(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(alarmClockEventEndBroadcastReceiver, intentFilter22);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForAlarmClockSensor", "registered alarmClockEventEndBroadcastReceiver");
                } else {
                    registerReceiverForAlarmClockSensor(false, false);
                }
            }
            else
                registerReceiverForAlarmClockSensor(false, false);
        }
    }

    private void unregisterPPPPExtenderReceiver(int type) {
        Context appContext = getApplicationContext();
        if (type == PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER) {
            if (pppExtenderForceStopApplicationBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderForceStopApplicationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderForceStopApplicationBroadcastReceiver);
                    pppExtenderForceStopApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderForceStopApplicationBroadcastReceiver = null;
                }
            } else
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForceStopApplicationBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER) {
            if (pppExtenderForegroundApplicationBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderForegroundApplicationBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderForegroundApplicationBroadcastReceiver);
                    pppExtenderForegroundApplicationBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderForegroundApplicationBroadcastReceiver = null;
                }
            } else
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderForegroundApplicationBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER) {
            if (pppExtenderSMSBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderSMSBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderSMSBroadcastReceiver);
                    pppExtenderSMSBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderSMSBroadcastReceiver = null;
                }
            } else
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderSMSBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
        if (type == PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER) {
            if (pppExtenderCallBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.unregisterPPPPExtenderReceiver->UNREGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "UNREGISTER pppExtenderCallBroadcastReceiver");
                try {
                    appContext.unregisterReceiver(pppExtenderCallBroadcastReceiver);
                    pppExtenderCallBroadcastReceiver = null;
                } catch (Exception e) {
                    pppExtenderCallBroadcastReceiver = null;
                }
            } else
                PPApplication.logE("[RJS] PhoneProfilesService.unregisterPPPPExtenderReceiver", "not registered pppExtenderCallBroadcastReceiver");

            Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
            intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
            sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
    }

    void registerPPPPExtenderReceiver(boolean register, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver", "PhoneProfilesService_registerPPPPExtenderReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "xxx");
        if (!register) {
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);
            unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_CALL_UNREGISTER);
        }
        if (register) {
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, false, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean applicationsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean orientationAllowed = (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean smsAllowed = (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            boolean callAllowed = (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, appContext).allowed ==
                                        PreferenceAllowed.PREFERENCE_ALLOWED);
            if (profileAllowed || applicationsAllowed || orientationAllowed || smsAllowed || callAllowed) {
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "profile or event allowed");
                int profileCount = 0;
                int applicationCount = 0;
                int orientationCount = 0;
                int smsCount = 0;
                int callCount = 0;
                if (checkDatabase) {
                    if (profileAllowed) {
                        if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_FORCE_STOP, true) > 0) {
                            Profile profile = Profile.getSharedProfile(appContext);
                            if (profile._deviceForceStopApplicationChange != 0)
                                ++profileCount;
                        }
                        else
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_FORCE_STOP, false);
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
                    profileCount = 1;
                    applicationCount = 1;
                    orientationCount = 1;
                    smsCount = 1;
                    callCount = 1;
                }
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "profileCount="+profileCount);
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "applicationCount="+applicationCount);
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "orientationCount="+orientationCount);
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "smsCount="+smsCount);
                PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "callCount="+callCount);

                if (profileCount > 0) {
                    if (pppExtenderForceStopApplicationBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForceStopApplicationBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderForceStopApplicationBroadcastReceiver");
                        pppExtenderForceStopApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_END);
                        appContext.registerReceiver(pppExtenderForceStopApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForceStopApplicationBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER);

                if ((applicationCount > 0) || (orientationCount > 0)) {
                    if (pppExtenderForegroundApplicationBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderForegroundApplicationBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderForegroundApplicationBroadcastReceiver");
                        pppExtenderForegroundApplicationBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_ACCESSIBILITY_SERVICE_UNBIND);
                        intentFilter23.addAction(PPApplication.ACTION_FOREGROUND_APPLICATION_CHANGED);
                        appContext.registerReceiver(pppExtenderForegroundApplicationBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderForegroundApplicationBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER);

                if (smsCount > 0) {
                    if (pppExtenderSMSBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderSMSBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderSMSBroadcastReceiver");
                        pppExtenderSMSBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_SMS_MMS_RECEIVED);
                        appContext.registerReceiver(pppExtenderSMSBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderSMSBroadcastReceiver");

                    Intent intent = new Intent(PPApplication.ACTION_REGISTER_PPPE_FUNCTION);
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_APP, "PhoneProfilesPlus");
                    intent.putExtra(PPApplication.EXTRA_REGISTRATION_TYPE, PPApplication.REGISTRATION_TYPE_SMS_REGISTER);
                    sendBroadcast(intent, PPApplication.ACCESSIBILITY_SERVICE_PERMISSION);
                }
                else
                    unregisterPPPPExtenderReceiver(PPApplication.REGISTRATION_TYPE_SMS_UNREGISTER);

                if (callCount > 0) {
                    if (pppExtenderCallBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPPPPExtenderReceiver->REGISTER", "PhoneProfilesService_pppExtenderCallBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "REGISTER pppExtenderCallBroadcastReceiver");
                        pppExtenderCallBroadcastReceiver = new PPPExtenderBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(PPApplication.ACTION_CALL_RECEIVED);
                        appContext.registerReceiver(pppExtenderCallBroadcastReceiver, intentFilter23,
                                PPApplication.ACCESSIBILITY_SERVICE_PERMISSION, null);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerPPPPExtenderReceiver", "registered pppExtenderCallBroadcastReceiver");

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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "xxx");
        if (!register) {
            if (locationModeChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(locationModeChangedBroadcastReceiver);
                    locationModeChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    locationModeChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "not registered");
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
                    if (ApplicationPreferences.applicationEventLocationEnableScanning(appContext)) {
                        // location scanner is enabled
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "REGISTER");
                        locationModeChangedBroadcastReceiver = new LocationModeChangedBroadcastReceiver();
                        IntentFilter intentFilter18 = new IntentFilter();
                        intentFilter18.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                        //if (android.os.Build.VERSION.SDK_INT >= 19)
                            intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
                        appContext.registerReceiver(locationModeChangedBroadcastReceiver, intentFilter18);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (!register) {
            if (bluetoothStateChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(bluetoothStateChangedBroadcastReceiver);
                    bluetoothStateChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    bluetoothStateChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "not registered");
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
                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext)) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH, false);
                            eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED, false);
                            eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "REGISTER");
                        bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
                        IntentFilter intentFilter15 = new IntentFilter();
                        intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                        appContext.registerReceiver(bluetoothStateChangedBroadcastReceiver, intentFilter15);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "registered");
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
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED);
                        eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
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
                                } finaly (
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers", "PhoneProfilesService_registerBluetoothScannerReceivers");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (!register) {
            if (bluetoothScanReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothScanReceiver");
                try {
                    appContext.unregisterReceiver(bluetoothScanReceiver);
                    bluetoothScanReceiver = null;
                } catch (Exception e) {
                    bluetoothScanReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothScanReceiver");
            if (bluetoothLEScanReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->UNREGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "UNREGISTER bluetoothLEScanReceiver");
                try {
                    LocalBroadcastManager.getInstance(appContext).unregisterReceiver(bluetoothLEScanReceiver);
                    bluetoothLEScanReceiver = null;
                } catch (Exception e) {
                    bluetoothLEScanReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothLEScanReceiver");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext)) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                if (eventCount > 0) {
                    if (bluetoothScanReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
                        bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_FOUND);
                        intentFilter14.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        appContext.registerReceiver(bluetoothScanReceiver, intentFilter14);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
                    if (bluetoothLEScanReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers->REGISTER bluetoothLEScanReceiver", "PhoneProfilesService_registerBluetoothScannerReceivers");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "REGISTER bluetoothLEScanReceiver");
                        bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();
                        LocalBroadcastManager.getInstance(appContext).registerReceiver(bluetoothLEScanReceiver, new IntentFilter("BluetoothLEScanBroadcastReceiver"));
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "registered bluetoothLEScanReceiver");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (!register) {
            if (wifiAPStateChangeBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiAPStateChangeBroadcastReceiver);
                    wifiAPStateChangeBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiAPStateChangeBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        int eventCount = 1;
                        if (checkDatabase/* || (wifiAPStateChangeBroadcastReceiver == null)*/) {
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                        }
                        if (eventCount > 0) {
                            if (wifiAPStateChangeBroadcastReceiver == null) {
                                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
                                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "REGISTER");
                                wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
                                IntentFilter intentFilter17 = new IntentFilter();
                                intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                                appContext.registerReceiver(wifiAPStateChangeBroadcastReceiver, intentFilter17);
                            } else
                                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver", "PhoneProfilesService_registerPowerSaveModeReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "xxx");
        if (!register) {
            if (powerSaveModeReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->UNREGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(powerSaveModeReceiver);
                    powerSaveModeReceiver = null;
                } catch (Exception e) {
                    powerSaveModeReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "not registered");
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
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY, false);
                }
                if (eventCount > 0) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        if (powerSaveModeReceiver == null) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver->REGISTER", "PhoneProfilesService_registerPowerSaveModeReceiver");
                            PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "REGISTER");
                            powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
                            IntentFilter intentFilter10 = new IntentFilter();
                            intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                            appContext.registerReceiver(powerSaveModeReceiver, intentFilter10);
                        }
                        else
                            PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "registered");
                    }
                } else {
                    registerPowerSaveModeReceiver(false, false);
                }
            }
            else
                registerPowerSaveModeReceiver(false, false);
        }
    }

    private void registerWifiStateChangedBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (!register) {
            if (wifiStateChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver->UNREGISTER", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
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
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (profileAllowed || eventAllowed) {
                int profileCount = 1;
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (wifiStateChangedBroadcastReceiver == null)*/) {
                    if (profileAllowed) {
                        profileCount = 0;
                        if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, true) > 0) {
                            Profile profile = Profile.getSharedProfile(appContext);
                            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                                ++profileCount;
                        }
                        else
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, false);
                    }
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                                // start only for screen On
                                eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED, false);
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver->REGISTER", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
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
    }

    private void registerWifiConnectionBroadcastReceiver(boolean register, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (!register) {
            if (wifiConnectionBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiConnectionBroadcastReceiver);
                    wifiConnectionBroadcastReceiver = null;
                } catch (Exception e) {
                    wifiConnectionBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "not registered");
        }
        if (register) {
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, false, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (profileAllowed || eventAllowed) {
                int profileCount = 1;
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (wifiConnectionBroadcastReceiver == null)*/) {
                    if (profileAllowed) {
                        profileCount = 0;
                        if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, true) > 0) {
                            Profile profile = Profile.getSharedProfile(appContext);
                            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                                ++profileCount;
                        }
                        else
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, false);
                    }
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                                // start only for screen On
                                eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED, false);
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
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "REGISTER");
                        wifiConnectionBroadcastReceiver = new WifiConnectionBroadcastReceiver();
                        IntentFilter intentFilter13 = new IntentFilter();
                        intentFilter13.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                        appContext.registerReceiver(wifiConnectionBroadcastReceiver, intentFilter13);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiScannerReceiver", "PhoneProfilesService_registerWifiScannerReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (!register) {
            if (wifiScanReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->UNREGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(wifiScanReceiver);
                    wifiScanReceiver = null;
                } catch (Exception e) {
                    wifiScanReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "eventCount="+eventCount);
                if (eventCount > 0) {
                    if (wifiScanReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiScannerReceiver->REGISTER", "PhoneProfilesService_registerWifiScannerReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "REGISTER");
                        wifiScanReceiver = new WifiScanBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter();
                        intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        appContext.registerReceiver(wifiScanReceiver, intentFilter4);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForTimeSensor", "PhoneProfilesService_registerReceiverForTimeSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "xxx");
        if (!register) {
            if (eventTimeBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(eventTimeBroadcastReceiver);
                    eventTimeBroadcastReceiver = null;
                } catch (Exception e) {
                    eventTimeBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_TIME, false);
                if (eventCount > 0) {
                    if (eventTimeBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForTimeSensor->REGISTER", "PhoneProfilesService_registerReceiverForTimeSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "REGISTER");
                        eventTimeBroadcastReceiver = new EventTimeBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
                        appContext.registerReceiver(eventTimeBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForTimeSensor", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForNFCSensor", "PhoneProfilesService_registerReceiverForNFCSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "xxx");
        if (!register) {
            if (nfcEventEndBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(nfcEventEndBroadcastReceiver);
                    nfcEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    nfcEventEndBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_NFC, false);
                if (eventCount > 0) {
                    if (nfcEventEndBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForNFCSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "REGISTER");
                        nfcEventEndBroadcastReceiver = new NFCEventEndBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(nfcEventEndBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForNFCSensor", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCallSensor", "PhoneProfilesService_registerReceiverForCallSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "xxx");
        if (!register) {
            if (missedCallEventEndBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(missedCallEventEndBroadcastReceiver);
                    missedCallEventEndBroadcastReceiver = null;
                } catch (Exception e) {
                    missedCallEventEndBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, getApplicationContext()).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase)
                    eventCount = DatabaseHandler.getInstance(getBaseContext()).getTypeEventsCount(DatabaseHandler.ETYPE_CALL, false);
                if (eventCount > 0) {
                    if (missedCallEventEndBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCallSensor->REGISTER", "PhoneProfilesService_registerReceiverForCallSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "REGISTER");
                        missedCallEventEndBroadcastReceiver = new MissedCallEventEndBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                        appContext.registerReceiver(missedCallEventEndBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCallSensor", "registered");
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
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver", "PhoneProfilesService_registerGeofencesScannerReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "xxx");
        if (!register) {
            if (geofencesScannerSwitchGPSBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->UNREGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(geofencesScannerSwitchGPSBroadcastReceiver);
                    geofencesScannerSwitchGPSBroadcastReceiver = null;
                } catch (Exception e) {
                    geofencesScannerSwitchGPSBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "not registered");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                    PreferenceAllowed.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    if (ApplicationPreferences.applicationEventLocationEnableScanning(appContext)) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                        } else
                            eventCount = 0;
                    } else
                        eventCount = 0;
                }
                PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "eventCount="+eventCount);
                if (eventCount > 0) {
                    if (geofencesScannerSwitchGPSBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerGeofencesScannerReceiver->REGISTER", "PhoneProfilesService_registerGeofencesScannerReceiver");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "REGISTER");
                        geofencesScannerSwitchGPSBroadcastReceiver = new GeofencesScannerSwitchGPSBroadcastReceiver();
                        IntentFilter intentFilter4 = new IntentFilter(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                        appContext.registerReceiver(geofencesScannerSwitchGPSBroadcastReceiver, intentFilter4);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerGeofencesScannerReceiver", "registered");
                } else {
                    registerGeofencesScannerReceiver(false, false);
                }
            }
            else
                registerGeofencesScannerReceiver(false, false);
        }
    }

    private void cancelWifiJob(final Context context, final Handler _handler) {
        if (WifiScanJob.isJobScheduled()) {
            CallsCounter.logCounterNoInc(context, "PhoneProfilesService.scheduleWifiJob->CANCEL", "PhoneProfilesService_scheduleWifiJob");
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "CANCEL");
            WifiScanJob.cancelJob(context, true, _handler);
        }
        else
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "not scheduled");
    }

    void scheduleWifiJob(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                         //final boolean forScreenOn, final boolean afterEnableWifi,
                         /*final boolean forceStart,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleWifiJob", "PhoneProfilesService_scheduleWifiJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "xxx");

        if (/*!forceStart &&*/ WifiSSIDPreference.forceRegister)
            return;

        PPApplication.startHandlerThread("PhoneProfilesService.scheduleWifiJob");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                    cancelWifiJob(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) {
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!WifiScanJob.isJobScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT, false);
                                }
                                if (eventCount > 0) {
                                    if (!WifiScanJob.isJobScheduled()) {
                                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleWifiJob->SCHEDULE", "PhoneProfilesService_scheduleWifiJob");
                                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "SCHEDULE");
                                        WifiScanJob.scheduleJob(appContext, true, handler, true/*, forScreenOn, afterEnableWifi*/);
                                    } else {
                                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "scheduled");
                                        if (rescan)
                                            WifiScanJob.scheduleJob(appContext, true, handler, true/*, forScreenOn, afterEnableWifi*/);
                                    }
                                } else
                                    cancelWifiJob(appContext, handler);
                            } else
                                cancelWifiJob(appContext, handler);
                        } else
                            cancelWifiJob(appContext, handler);
                    }
                    else
                        cancelWifiJob(appContext, handler);
                }
            }
        });
    }

    private void cancelBluetoothJob(final Context context, final Handler _handler) {
        if (BluetoothScanJob.isJobScheduled()) {
            CallsCounter.logCounterNoInc(context, "PhoneProfilesService.scheduleBluetoothJob->CANCEL", "PhoneProfilesService_scheduleBluetoothJob");
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "CANCEL");
            BluetoothScanJob.cancelJob(context, true, _handler);
        }
        else
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "not scheduled");
    }

    private void scheduleBluetoothJob(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                              /*final boolean forScreenOn, final boolean forceStart,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleBluetoothJob", "PhoneProfilesService_scheduleBluetoothJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "xxx");

        if (/*!forceStart &&*/ BluetoothNamePreference.forceRegister)
            return;

        PPApplication.startHandlerThread("PhoneProfilesService.scheduleBluetoothJob");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                    cancelBluetoothJob(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext)) {
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!BluetoothScanJob.isJobScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT, false);
                                }
                                if (eventCount > 0) {
                                    if (!BluetoothScanJob.isJobScheduled()) {
                                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleBluetoothJob->SCHEDULE", "PhoneProfilesService_scheduleBluetoothJob");
                                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "SCHEDULE");
                                        BluetoothScanJob.scheduleJob(appContext, true, handler, true/*, forScreenOn*/);
                                    } else {
                                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "scheduled");
                                        if (rescan)
                                            BluetoothScanJob.scheduleJob(appContext, true, handler, true/*, forScreenOn*/);
                                    }
                                } else
                                    cancelBluetoothJob(appContext, handler);
                            } else
                                cancelBluetoothJob(appContext, handler);
                        } else
                            cancelBluetoothJob(appContext, handler);
                    }
                    else
                        cancelBluetoothJob(appContext, handler);
                }
            }
        });
    }

    private void cancelGeofenceScannerJob(final Context context, final Handler _handler) {
        if (GeofenceScannerJob.isJobScheduled()) {
            CallsCounter.logCounterNoInc(context, "PhoneProfilesService.cancelGeofenceScannerJob->CANCEL", "PhoneProfilesService_scheduleGeofenceScannerJob");
            PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceScannerJob", "CANCEL");
            GeofenceScannerJob.cancelJob(true, _handler);
        }
        else
            PPApplication.logE("[RJS] PhoneProfilesService.cancelGeofenceScannerJob", "not scheduled");
    }

    private void scheduleGeofenceScannerJob(final boolean schedule, /*final boolean cancel,*/ final boolean checkDatabase,
                                    /*final boolean forScreenOn,*/ final boolean rescan) {
        final Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleGeofenceScannerJob", "PhoneProfilesService_scheduleGeofenceScannerJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "xxx");

        PPApplication.startHandlerThread("PhoneProfilesService.scheduleGeofenceScannerJob");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //if (cancel) {
                    cancelGeofenceScannerJob(appContext, handler);
                //}
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        if (ApplicationPreferences.applicationEventLocationEnableScanning(appContext)) {
                            // location scanner is enabled
                            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
                                // start only for screen On
                                int eventCount = 1;
                                if (checkDatabase/* || (!GeofenceScannerJob.isJobScheduled())*/) {
                                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                                }
                                if (eventCount > 0) {
                                    if (!GeofenceScannerJob.isJobScheduled() || rescan) {
                                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleGeofenceScannerJob->SCHEDULE", "PhoneProfilesService_scheduleGeofenceScannerJob");
                                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "SCHEDULE");
                                        synchronized (PPApplication.geofenceScannerMutex) {
                                            if (isGeofenceScannerStarted()) {
                                                PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "updateTransitionsByLastKnownLocation");
                                                getGeofencesScanner().updateTransitionsByLastKnownLocation(false);
                                            }
                                        }
                                        GeofenceScannerJob.scheduleJob(appContext, false, handler, true/*, forScreenOn*/);
                                    }
                                } else
                                    cancelGeofenceScannerJob(appContext, handler);
                            } else
                                cancelGeofenceScannerJob(appContext, handler);
                        } else
                            cancelGeofenceScannerJob(appContext, handler);
                    }
                    else
                        cancelGeofenceScannerJob(appContext, handler);
                }
            }
        });
    }

    private void cancelSearchCalendarEventsJob(final Context context, final Handler _handler) {
        if (SearchCalendarEventsJob.isJobScheduled()) {
            CallsCounter.logCounterNoInc(context, "PhoneProfilesService.scheduleSearchCalendarEventsJob->CANCEL", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "CANCEL");
            SearchCalendarEventsJob.cancelJob(true, _handler);
        }
        else
            PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "not scheduled");
    }

    private void scheduleSearchCalendarEventsJob(final boolean schedule, final boolean cancel, final boolean checkDatabase) {
        final Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsJob", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "xxx");

        PPApplication.startHandlerThread("PhoneProfilesService.scheduleSearchCalendarEventsJob");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (cancel) {
                    cancelSearchCalendarEventsJob(appContext, handler);
                }
                if (schedule) {
                    boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext).allowed ==
                            PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (eventAllowed) {
                        int eventCount = 1;
                        if (checkDatabase/* || (!SearchCalendarEventsJob.isJobScheduled())*/) {
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
                        }
                        if (eventCount > 0) {
                            if (!SearchCalendarEventsJob.isJobScheduled()) {
                                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsJob->SCHEDULE", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
                                PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "SCHEDULE");
                                SearchCalendarEventsJob.scheduleJob(/*appContext, */true, handler, true);
                            }
                            else
                                PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "scheduled");
                        } else {
                            cancelSearchCalendarEventsJob(appContext, handler);
                        }
                    }
                    else
                        cancelSearchCalendarEventsJob(appContext, handler);
                }
            }
        });
    }

    private void startGeofenceScanner(boolean start, boolean stop, boolean checkDatabase, boolean forScreenOn) {
        synchronized (PPApplication.geofenceScannerMutex) {
            Context appContext = getApplicationContext();
            CallsCounter.logCounter(appContext, "PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService_startGeofenceScanner");
            PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "xxx");
            if (stop) {
                if (isGeofenceScannerStarted()) {
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->STOP", "PhoneProfilesService_startGeofenceScanner");
                    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "STOP");
                    stopGeofenceScanner();
                } else
                    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    boolean applicationEventLocationScanOnlyWhenScreenIsOn = ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext);
                    if (ApplicationPreferences.applicationEventLocationEnableScanning(appContext)) {
                        // location scanner is enabled
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !applicationEventLocationScanOnlyWhenScreenIsOn) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isGeofenceScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION, false);
                            }
                            if (eventCount > 0) {
                                if (!isGeofenceScannerStarted()) {
                                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->START", "PhoneProfilesService_startGeofenceScanner");
                                    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "START");
                                    if (forScreenOn && (pm != null) && pm.isScreenOn() &&
                                            applicationEventLocationScanOnlyWhenScreenIsOn)
                                        startGeofenceScanner(true);
                                    else
                                        startGeofenceScanner(false);
                                } else {
                                    PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "started");
                                }
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
            CallsCounter.logCounter(appContext, "PhoneProfilesService.startPhoneStateScanner", "PhoneProfilesService_startPhoneStateScanner");
            PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "xxx");
            if (!forceStart && (MobileCellsPreference.forceStart || MobileCellsRegistrationService.forceStart))
                return;
            if (stop) {
                if (isPhoneStateScannerStarted()) {
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->STOP", "PhoneProfilesService_startPhoneStateScanner");
                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "STOP");
                    stopPhoneStateScanner();
                } else
                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED);
                PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "eventAllowed="+eventAllowed);
                if (eventAllowed) {
                    boolean applicationEventMobileCellEnableScanning = ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext);
                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "scanning enabled="+applicationEventMobileCellEnableScanning);
                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "PhoneStateScanner.forceStart="+PhoneStateScanner.forceStart);
                    if (applicationEventMobileCellEnableScanning || PhoneStateScanner.forceStart) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isPhoneStateScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false);
                            }
                            PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "eventCount="+eventCount);
                            if (eventCount > 0) {
                                if (!isPhoneStateScannerStarted()) {
                                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->START", "PhoneProfilesService_startPhoneStateScanner");
                                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "START");
                                    startPhoneStateScanner();
                                } else {
                                    PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "started");
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
            CallsCounter.logCounter(appContext, "PhoneProfilesService.startOrientationScanner", "PhoneProfilesService_startOrientationScanner");
            PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "xxx");
            if (stop) {
                if (isOrientationScannerStarted()) {
                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->STOP", "PhoneProfilesService_startOrientationScanner");
                    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "STOP");
                    stopOrientationScanner();
                } else
                    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "not started");
            }
            if (start) {
                boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED;
                if (eventAllowed) {
                    if (ApplicationPreferences.applicationEventOrientationEnableScanning(appContext)) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        if (((pm != null) && pm.isScreenOn()) || !ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            int eventCount = 1;
                            if (checkDatabase/* || (!isOrientationScannerStarted())*/) {
                                eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false);
                            }
                            if (eventCount > 0) {
                                if (!isOrientationScannerStarted()) {
                                    CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->START", "PhoneProfilesService_startOrientationScanner");
                                    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "START");
                                    startOrientationScanner();
                                } else
                                    PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "started");
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

    private void registerReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiversAndJobs", "xxx");

        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = getApplicationContext();

        WifiScanJob.initialize(appContext);
        BluetoothScanJob.initialize(appContext);

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

        // required for Connect to SSID profile preference +
        // wifi connection type = (dis)connected +
        // radio switch event +
        // wifi scanner
        registerWifiStateChangedBroadcastReceiver(true, true, false);

        // required for Connect to SSID profile preference +
        // required for wifi connection type = (dis)connected event +
        // wifi scanner
        registerWifiConnectionBroadcastReceiver(true, true, false);

        // required for wifi scanner
        registerWifiScannerReceiver(true, true, false);

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
                intent.setAction("sk.henrichg.phoneprofilesplus.INCOMING_SIPCALL");
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

        scheduleWifiJob(true,  true, /*false, false, false,*/ false);
        scheduleBluetoothJob(true,  true, /*false, false,*/ false);
        scheduleGeofenceScannerJob(true,  true, /*false,*/ false);
        scheduleSearchCalendarEventsJob(true, true, true);

        startGeofenceScanner(true, true, true, false);
        scheduleGeofenceScannerJob(true,  true, /*false,*/ false);
        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
    }

    private void unregisterReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.unregisterReceiversAndJobs", "xxx");
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
        registerWifiStateChangedBroadcastReceiver(false, false, false);
        registerWifiConnectionBroadcastReceiver(false, false, false);
        registerWifiScannerReceiver(false, false, false);
        registerReceiverForTimeSensor(false, false);
        registerReceiverForNFCSensor(false, false);
        registerReceiverForCallSensor(false, false);
        registerGeofencesScannerReceiver(false, false);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        scheduleWifiJob(false,  false, /*false, false, false,*/ false);
        scheduleBluetoothJob(false,  false, /*false, false,*/ false);
        scheduleGeofenceScannerJob(false,  false, /*false,*/ false);
        scheduleSearchCalendarEventsJob(false, true, false);

        startGeofenceScanner(false, true, false, false);
        scheduleGeofenceScannerJob(false,  false, /*false,*/ false);
        startPhoneStateScanner(false, true, false, false, false);
        startOrientationScanner(false, true, false);
    }

    private void reregisterReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.reregisterReceiversAndJobs", "xxx");
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
        registerWifiStateChangedBroadcastReceiver(true, true, false);
        registerWifiConnectionBroadcastReceiver(true, true, false);
        registerWifiScannerReceiver(true, true, false);
        registerReceiverForTimeSensor(true, true);
        registerReceiverForNFCSensor(true, true);
        registerReceiverForCallSensor(true, true);
        registerGeofencesScannerReceiver(true, true);

        scheduleWifiJob(true,  true, /*false, false, false,*/ false);
        scheduleBluetoothJob(true,  true, /*false, false,*/ false);
        scheduleGeofenceScannerJob(true,  true, /*false,*/ false);
        scheduleSearchCalendarEventsJob(true, false, true);

        startGeofenceScanner(true, true, true, false);
        scheduleGeofenceScannerJob(true,  true, /*false,*/ false);
        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
    }

    // start service for first start
    private boolean doForFirstStart(Intent intent/*, int flags, int startId*/) {
        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart START");

        boolean onlyStart = true;
        boolean initializeStart = false;
        boolean startOnBoot = false;
        boolean startOnPackageReplace = false;

        if (intent != null) {
            onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            initializeStart = intent.getBooleanExtra(EXTRA_INITIALIZE_START, false);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
            startOnPackageReplace = intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false);
        }

        if (onlyStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_ONLY_START");
        if (initializeStart)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_INITIALIZE_START");
        if (startOnBoot)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_BOOT");
        if (startOnPackageReplace)
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "EXTRA_START_ON_PACKAGE_REPLACE");

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "serviceRunning="+serviceRunning);

        if (serviceRunning && onlyStart && !startOnBoot && !startOnPackageReplace && !initializeStart) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "only EXTRA_ONLY_START, service already running");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return true;
        }

        /*
        if ((!startOnPackageReplace) && PPApplication.isNewVersion(getApplicationContext())) {
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "is new version but not EXTRA_START_ON_PACKAGE_REPLACE");
            PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");
            return true;
        }
        */

        serviceRunning = true;

        final Context appContext = getApplicationContext();

        if (onlyStart) {
            /*
            registerReceiversAndJobs();
            DonationNotificationJob.scheduleJob(getApplicationContext(), true);
            */

            final boolean _startOnBoot = startOnBoot;
            final boolean _startOnPackageReplace = startOnPackageReplace;
            final boolean _initializeStart = initializeStart;
            PPApplication.startHandlerThread("PhoneProfilesService.doForFirstStart.2");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 START");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.doForFirstStart.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // is called from PPApplication
                        //PPApplication.initRoot();
                        if (!ApplicationPreferences.applicationNeverAskForGrantRoot(appContext)) {
                            // grant root
                            PPApplication.isRootGranted();
                        }
                        //PPApplication.getSUVersion();
                        PPApplication.settingsBinaryExists(false);
                        PPApplication.serviceBinaryExists(false);
                        PPApplication.getServicesList();

                        GlobalGUIRoutines.setLanguage(appContext);

                        if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                            // restart first start
                            serviceHasFirstStart = false;
                        }

                        if (serviceHasFirstStart) {
                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application already started");
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");
                            return;
                        }

                        serviceHasFirstStart = true;

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                        PPApplication.createNotificationChannels(appContext);
                        dataWrapper.setDynamicLauncherShortcuts();

                        if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application not started, start it");

                            //Permissions.clearMergedPermissions(appContext);

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

                            ApplicationPreferences.getSharedPreferences(appContext);
                            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                            editor.putInt(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
                            editor.putString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, "");
                            editor.putLong(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TIME, 0);
                            editor.apply();

                            // show info notification
                            ImportantInfoNotification.showInfoNotification(appContext);

                            ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                            Profile.setActivatedProfileForDuration(appContext, 0);

                            StartEventNotificationBroadcastReceiver.removeAlarm(appContext);
                            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(appContext);
                            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

                            PPNotificationListenerService.clearNotifiedPackages(appContext);

                            DatabaseHandler.getInstance(appContext).deleteAllEventTimelines();
                            DatabaseHandler.getInstance(appContext).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

                            MobileCellsRegistrationService.setMobileCellsAutoRegistration(appContext, true);

                            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, true);
                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                            // not needed clearConnectedDevices(.., true) call it
                            //BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                            BluetoothConnectedDevices.getConnectedDevices(appContext);
                        }

                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().registerReceiversAndJobs();
                        DonationNotificationJob.scheduleJob(appContext, false);

                        if (_startOnBoot || _startOnPackageReplace || _initializeStart) {
                            if (_startOnBoot)
                                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTARTONBOOT, null, null, null, 0);
                            else
                                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTART, null, null, null, 0);

                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "application started");

                            // start events
                            if (Event.getGlobalEventsRunning(appContext)) {
                                PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "global event run is enabled, first start events");

                                if (!dataWrapper.getIsManualProfileActivation(false)) {
                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false/*, false*/);
                                }

                                dataWrapper.firstStartEvents(true, false);
                            } else {
                                PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "global event run is not enabled, manually activate profile");

                                ////// unblock all events for first start
                                //     that may be blocked in previous application run
                                dataWrapper.pauseAllEvents(true, false/*, false*/);

                                dataWrapper.activateProfileOnBoot();
                            }
                        }

                        if (!_startOnBoot && !_startOnPackageReplace && !_initializeStart) {
                            PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "###### not initialize start ######");
                            if (Event.getGlobalEventsRunning(appContext)) {
                                PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "global event run is enabled, start events");
                                dataWrapper.startEventsOnBoot(true, false);
                            } else {
                                PPApplication.logE("$$$ PhoneProfilesService.doForFirstStart - handler", "global event run is not enabled, manually activate profile");
                                dataWrapper.activateProfileOnBoot();
                            }
                        }

                        dataWrapper.invalidateDataWrapper();

                        PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "PhoneProfilesService.doForFirstStart.2 END");
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

        PPApplication.logE("PhoneProfilesService.doForFirstStart", "PhoneProfilesService.doForFirstStart END");

        return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        //if ((intent == null) || (!intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false))) {
            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "showProfileNotification()");
            // do not call this from handlerThread. In Android 8 handlerThread is not called
            // when for service is not displayed foreground notification
            showProfileNotification();
        //}

        //if (!PPApplication.getApplicationStarted(getApplicationContext(), false)) {
        //    stopSelf();
        //    return START_NOT_STICKY;
        //}

        /*if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_START_ON_PACKAGE_REPLACE, false)) {
                unregisterReceiversAndJobs();

                stopSimulatingRingingCall();
                //stopSimulatingNotificationTone(true);

                reenableKeyguard();

                serviceHasFirstStart = false;
                serviceRunning = false;
                runningInForeground = false;
            }
        }*/

        if (!doForFirstStart(intent/*, flags, startId*/)) {
            if (intent != null) {
                /*if (intent.getBooleanExtra(EXTRA_SHOW_PROFILE_NOTIFICATION, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SHOW_PROFILE_NOTIFICATION");
                    // not needed, is already called in start of onStartCommand
                    //showProfileNotification();
                }
                else*/
                if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                    clearProfileNotification(/*this*/);
                }
                else
                if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                    // not needed, is already called in start of onStartCommand
                    //showProfileNotification();
                }
                else
                if (intent.getBooleanExtra(EXTRA_SWITCH_KEYGUARD, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SWITCH_KEYGUARD");

                    Context appContext = getApplicationContext();

                    boolean isScreenOn;
                    //if (android.os.Build.VERSION.SDK_INT >= 20)
                    //{
                    //    Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    //    isScreenOn = display.getState() == Display.STATE_ON;
                    //}
                    //else
                    //{
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    isScreenOn = ((pm != null) && pm.isScreenOn());
                    //}

                    boolean secureKeyguard;
                    if (keyguardManager == null)
                        keyguardManager = (KeyguardManager) appContext.getSystemService(Activity.KEYGUARD_SERVICE);
                    if (keyguardManager != null) {
                        secureKeyguard = keyguardManager.isKeyguardSecure();
                        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "secureKeyguard=" + secureKeyguard);
                        if (!secureKeyguard) {
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand xxx", "getLockScreenDisabled=" + ActivateProfileHelper.getLockScreenDisabled(appContext));

                            if (isScreenOn) {
                                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "screen on");

                                if (ActivateProfileHelper.getLockScreenDisabled(appContext)) {
                                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "disableKeyguard(), START_STICKY");
                                    reenableKeyguard();
                                    disableKeyguard();
                                } else {
                                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "reenableKeyguard(), stopSelf(), START_NOT_STICKY");
                                    reenableKeyguard();
                                }
                            }
                        }
                    }
                }

                /*
                else
                if (intent.getBooleanExtra(EXTRA_START_LOCATION_UPDATES, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_START_LOCATION_UPDATES");
                    //synchronized (PPApplication.geofenceScannerMutex) {
                        if (PhoneProfilesService.getGeofencesScanner() != null) {
                            GeofencesScanner.useGPS = true;
                            PhoneProfilesService.getGeofencesScanner().startLocationUpdates();
                        }
                    //}
                }
                else
                if (intent.getBooleanExtra(EXTRA_STOP_LOCATION_UPDATES, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_STOP_LOCATION_UPDATES");
                    //synchronized (PPApplication.geofenceScannerMutex) {
                    if (PhoneProfilesService.getGeofencesScanner() != null)
                        PhoneProfilesService.getGeofencesScanner().stopLocationUpdates();
                    //}
                }
                */

                else
                if (intent.getBooleanExtra(EXTRA_REGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_REGISTER_RECEIVERS_AND_JOBS");
                    final Context appContext = getApplicationContext();
                    PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.EXTRA_REGISTER_RECEIVERS_AND_JOBS");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.EXTRA_REGISTER_RECEIVERS_AND_JOBS");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if (PhoneProfilesService.getInstance() != null)
                                    PhoneProfilesService.getInstance().registerReceiversAndJobs();
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
                else
                if (intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_JOBS");
                    final Context appContext = getApplicationContext();
                    PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.EXTRA_UNREGISTER_RECEIVERS_AND_JOBS");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":PhoneProfilesService.onStartCommand.EXTRA_UNREGISTER_RECEIVERS_AND_JOBS");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if (PhoneProfilesService.getInstance() != null)
                                    PhoneProfilesService.getInstance().unregisterReceiversAndJobs();
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
                else
                if (intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_REREGISTER_RECEIVERS_AND_JOBS");
                    final Context appContext = getApplicationContext();
                    PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.EXTRA_REREGISTER_RECEIVERS_AND_JOBS");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.EXTRA_REREGISTER_RECEIVERS_AND_JOBS");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if (PhoneProfilesService.getInstance() != null)
                                    PhoneProfilesService.getInstance().reregisterReceiversAndJobs();
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
                else
                if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SIMULATE_RINGING_CALL");
                    doSimulatingRingingCall(intent);
                }
                //else
                //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                //    doSimulatingNotificationTone(intent);
                else
                if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_START_STOP_SCANNER");
                    final boolean forScreenOn = intent.getBooleanExtra(EXTRA_FOR_SCREEN_ON, false);
                    final Context appContext = getApplicationContext();
                    switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                        case PPApplication.SCANNER_START_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_GEOFENCE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_START_GEOFENCE_SCANNER");
                            final Handler handler1 = new Handler(PPApplication.handlerThread.getLooper());
                            handler1.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_START_GEOFENCE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startGeofenceScanner(true, true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleGeofenceScannerJob(true, true, /*false,*/ false);
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
                            break;
                        case PPApplication.SCANNER_STOP_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_GEOFENCE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_STOP_GEOFENCE_SCANNER");
                            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
                            handler2.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_STOP_GEOFENCE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startGeofenceScanner(false, true, false, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleGeofenceScannerJob(false, false, /*false,*/ false);
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
                            break;
                        case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_ORIENTATION_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_START_ORIENTATION_SCANNER");
                            final Handler handler3 = new Handler(PPApplication.handlerThread.getLooper());
                            handler3.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_START_ORIENTATION_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startOrientationScanner(true, true, true);
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
                            break;
                        case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_STOP_ORIENTATION_SCANNER");
                            final Handler handler4 = new Handler(PPApplication.handlerThread.getLooper());
                            handler4.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_STOP_ORIENTATION_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startOrientationScanner(false, true, false);
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
                            break;
                        case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_START_PHONE_STATE_SCANNER");
                            final Handler handler5 = new Handler(PPApplication.handlerThread.getLooper());
                            handler5.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_START_PHONE_STATE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null) {
                                            PhoneStateScanner.forceStart = false;
                                            PhoneProfilesService.getInstance().startPhoneStateScanner(true, true, true, false, false);
                                        }
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
                            break;
                        case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_STOP_PHONE_STATE_SCANNER");
                            final Handler handler6 = new Handler(PPApplication.handlerThread.getLooper());
                            handler6.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_STOP_PHONE_STATE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startPhoneStateScanner(false, true, false, false, false);
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
                            break;
                        case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            final Handler handler7 = new Handler(PPApplication.handlerThread.getLooper());
                            handler7.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiConnectionBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiScannerReceiver(true, true, false);
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
                            break;
                        case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            final Handler handler8 = new Handler(PPApplication.handlerThread.getLooper());
                            handler8.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiConnectionBroadcastReceiver(true, false, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiStateChangedBroadcastReceiver(true, false, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiAPStateChangeBroadcastReceiver(true, false, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiScannerReceiver(true, false, true);
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
                            break;
                        case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            final Handler handler9 = new Handler(PPApplication.handlerThread.getLooper());
                            handler9.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //if (instance != null)
                                        //    PhoneProfilesService.getInstance().registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothScannerReceivers(true, true, false);
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
                            break;
                        case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            final Handler handler10 = new Handler(PPApplication.handlerThread.getLooper());
                            handler10.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //if (instance != null)
                                        //    PhoneProfilesService.getInstance().registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothStateChangedBroadcastReceiver(true, false, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothScannerReceivers(true, false, true);
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
                            break;
                        case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_WIFI_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_WIFI_SCANNER");
                            final Handler handler11 = new Handler(PPApplication.handlerThread.getLooper());
                            handler11.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_WIFI_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiConnectionBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiScannerReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleWifiJob(true, true, /*forScreenOn, false, false,*/ true);
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
                            break;
                        case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_BLUETOOTH_SCANNER");
                            final Handler handler12 = new Handler(PPApplication.handlerThread.getLooper());
                            handler12.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_BLUETOOTH_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        //if (instance != null)
                                        //    PhoneProfilesService.getInstance().registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothScannerReceivers(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleBluetoothJob(true, true, /*forScreenOn, false,*/ true);
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
                            break;
                        case PPApplication.SCANNER_RESTART_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_PHONE_STATE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_PHONE_STATE_SCANNER");
                            final Handler handler13 = new Handler(PPApplication.handlerThread.getLooper());
                            handler13.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!MobileCellsRegistrationService.serviceStarted) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_PHONE_STATE_SCANNER");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            if (PhoneProfilesService.getInstance() != null) {
                                                PhoneStateScanner.forceStart = false;
                                                PhoneProfilesService.getInstance().startPhoneStateScanner(true, true, true, false, true);
                                            }
                                        } finally {
                                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                                try {
                                                    wakeLock.release();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            break;
                        case PPApplication.SCANNER_FORCE_START_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                            final Handler handler14 = new Handler(PPApplication.handlerThread.getLooper());
                            handler14.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null) {
                                            PhoneStateScanner.forceStart = true;
                                            PhoneProfilesService.getInstance().startPhoneStateScanner(true, false, false, true, false);
                                        }
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
                            break;
                        case PPApplication.SCANNER_RESTART_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_GEOFENCE_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_GEOFENCE_SCANNER");
                            final Handler handler15 = new Handler(PPApplication.handlerThread.getLooper());
                            handler15.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_GEOFENCE_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerLocationModeChangedBroadcastReceiver(true, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startGeofenceScanner(true, true, true, forScreenOn);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleGeofenceScannerJob(true, true, /*forScreenOn,*/ true);
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
                            break;
                        case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_ORIENTATION_SCANNER");
                            final Handler handler16 = new Handler(PPApplication.handlerThread.getLooper());
                            handler16.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_ORIENTATION_SCANNER");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startOrientationScanner(true, false, true);
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
                            break;
                        case PPApplication.SCANNER_RESTART_ALL_SCANNERS:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_ALL_SCANNERS");
                            PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_ALL_SCANNERS");
                            final Handler handler17 = new Handler(PPApplication.handlerThread.getLooper());
                            handler17.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_ALL_SCANNERS");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiConnectionBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiAPStateChangeBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerWifiScannerReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleWifiJob(true, true, /*forScreenOn, false, false,*/ true);

                                        //if (instance != null)
                                        //    PhoneProfilesService.getInstance().registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothStateChangedBroadcastReceiver(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerBluetoothScannerReceivers(true, true, false);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleBluetoothJob(true, true, /*forScreenOn, false,*/ true);

                                        PhoneStateScanner.forceStart = false;
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startPhoneStateScanner(true, true, true, false, true);

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().registerLocationModeChangedBroadcastReceiver(true, true);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startGeofenceScanner(true, false, true, forScreenOn);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().scheduleGeofenceScannerJob(true, true, /*forScreenOn,*/ true);

                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().startOrientationScanner(true, false, true);
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
                            break;
                    }
                }
                else
                if (intent.getBooleanExtra(EXTRA_RESTART_EVENTS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_RESTART_EVENTS");
                    final boolean unblockEventsRun = intent.getBooleanExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, false);
                    final Context appContext = getApplicationContext();
                    PPApplication.startHandlerThread("PhoneProfilesService.onStartCommand.SCANNER_RESTART_PHONE_STATE_SCANNER");
                    final Handler handler13 = new Handler(PPApplication.handlerThread.getLooper());
                    handler13.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!MobileCellsRegistrationService.serviceStarted) {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.onStartCommand.SCANNER_RESTART_PHONE_STATE_SCANNER");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    if (PhoneProfilesService.getInstance() != null) {
                                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                        dataWrapper.restartEvents(unblockEventsRun, true, true, false, false);
                                        dataWrapper.invalidateDataWrapper();
                                    }
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showProfileNotification();
        ActivateProfileHelper.updateGUI(getApplicationContext(), true);
    }

    //------------------------

    // profile notification -------------------

    @SuppressLint("NewApi")
    private void _showProfileNotification(Profile profile, boolean inHandlerThread, final DataWrapper dataWrapper)
    {
        PPApplication.logE("PhoneProfilesService._showProfileNotification", "xxx");

        /*
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;
        */

        //PPApplication.logE("PhoneProfilesService.showProfileNotification", "no lockRefresh");

        final Context appContext = getApplicationContext();

        if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBar(appContext))
        {
            PPApplication.logE("PhoneProfilesService._showProfileNotification", "show");

            boolean notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar(appContext);
            boolean notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(appContext);
            //boolean notificationDarkBackground = ApplicationPreferences.notificationDarkBackground(appContext);
            boolean notificationUseDecoration = ApplicationPreferences.notificationUseDecoration(appContext);
            boolean notificationPrefIndicator = ApplicationPreferences.notificationPrefIndicator(appContext);
            boolean notificationHideInLockScreen = ApplicationPreferences.notificationHideInLockScreen(appContext);
            String notificationStatusBarStyle = ApplicationPreferences.notificationStatusBarStyle(appContext);
            String notificationTextColor = ApplicationPreferences.notificationTextColor(appContext);
            String notificationBackgroundColor = ApplicationPreferences.notificationBackgroundColor(appContext);

            // intent to LauncherActivity, for click on notification
            Intent intent = new Intent(appContext, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // setup startupSource
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            int requestCode = 0;
            if (inHandlerThread && (profile != null))
                requestCode = (int)profile._id;
            PendingIntent pIntent = PendingIntent.getActivity(appContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pIntentRE = null;
            if (Event.getGlobalEventsRunning(getBaseContext().getApplicationContext()) &&
                    PPApplication.getApplicationStarted(getBaseContext().getApplicationContext(), true)) {
                // intent for restart events
                Intent intentRE = new Intent(appContext, RestartEventsFromNotificationActivity.class);
                intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                pIntentRE = PendingIntent.getActivity(appContext, 2, intentRE, PendingIntent.FLAG_UPDATE_CURRENT);
            }

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

            boolean notificationDarkBackground = false;
            if (notificationBackgroundColor.equals("1")) {
                notificationDarkBackground = true;
            }
            else
            if (notificationBackgroundColor.equals("2")) {
                int nightModeFlags =
                        appContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        notificationDarkBackground = true;
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        break;
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        break;
                }
            }

            boolean useDecorator = (!PPApplication.romIsMIUI) || (Build.VERSION.SDK_INT >= 26);
            useDecorator = useDecorator && notificationUseDecoration;
            useDecorator = useDecorator && (!notificationDarkBackground);

            if (PPApplication.romIsMIUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_miui_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_miui);
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "miui");
            }
            else
            if (PPApplication.romIsEMUI) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_emui_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_emui);
            }
            else
            if (PPApplication.romIsSamsung) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_samsung_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_samsung_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    if (!useDecorator)
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_no_decorator);
                    else
                        contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
                    if (!useDecorator)
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact_no_decorator);
                    else
                        contentView = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer_compact);
                }
                else
                    contentViewLarge = new RemoteViews(appContext.getPackageName(), R.layout.notification_drawer);
            }
            //}

            boolean isIconResourceID;
            String iconIdentifier;
            Spannable profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                //PPApplication.logE("PhoneProfilesService.showProfileNotification", "profile != null");
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, dataWrapper, false);

                if (inHandlerThread) {
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
                String pName;
                if (inHandlerThread)
                    pName = appContext.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                else
                    pName = appContext.getResources().getString(R.string.empty_string);
                profileName = new SpannableString(pName);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(appContext);
            notificationBuilder.setContentIntent(pIntent);

            if (Build.VERSION.SDK_INT >= 21)
                notificationBuilder.setColor(ContextCompat.getColor(appContext, R.color.primary));

            if (Build.VERSION.SDK_INT >= 26) {
                PPApplication.createProfileNotificationChannel(/*profile, */appContext);
                notificationBuilder.setChannelId(PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                //notificationBuilder.setSettingsText("Test");
            }
            else {
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "notificationShowInStatusBar="+notificationShowInStatusBar);
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
            if (Build.VERSION.SDK_INT >= 21)
            {
                notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            //notificationBuilder.setTicker(profileName);

            if (inHandlerThread) {
                if (isIconResourceID) {
                    int iconSmallResource;
                    if (iconBitmap != null) {
                        if (notificationStatusBarStyle.equals("0")) {
                            // colorful icon

                            // FC in Note 4, 6.0.1 :-/
                            boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
                                  /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                                   Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                                  ) &&*/
                                    (android.os.Build.VERSION.SDK_INT == 23);
                            //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                            if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                                notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                            } else {
                                //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                                //if (iconSmallResource == 0)
                                //    iconSmallResource = R.drawable.ic_profile_default;
                                iconSmallResource = R.drawable.ic_profile_default_notify_color;
                                try {
                                    iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                } catch (Exception ignored) {
                                }
                                notificationBuilder.setSmallIcon(iconSmallResource);
                            }
                        } else {
                            // native icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {
                            }
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }

                        contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                    } else {
                        if (notificationStatusBarStyle.equals("0")) {
                            // colorful icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default;
                            iconSmallResource = R.drawable.ic_profile_default_notify_color;
                            try {
                                iconSmallResource = Profile.profileIconNotifyColorId.get(iconIdentifier);
                            } catch (Exception ignored) {
                            }
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        } else {
                            // native icon
                            //iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                            //if (iconSmallResource == 0)
                            //    iconSmallResource = R.drawable.ic_profile_default_notify;
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                            try {
                                iconSmallResource = Profile.profileIconNotifyId.get(iconIdentifier);
                            } catch (Exception ignored) {
                            }
                            notificationBuilder.setSmallIcon(iconSmallResource);

                            //int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                            //if (iconLargeResource == 0)
                            //    iconLargeResource = R.drawable.ic_profile_default;
                            int iconLargeResource = Profile.getIconResource(iconIdentifier);
                            Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), iconLargeResource);
                            contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                            if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                        }
                    }
                } else {
                    // FC in Note 4, 6.0.1 :-/
                    boolean isNote4 = (Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) &&
                            /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                             Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                            ) &&*/
                            (android.os.Build.VERSION.SDK_INT == 23);
                    //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                    if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
                        notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                    } else {
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
                    if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/) {
                        if (iconBitmap != null)
                            contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                        else
                            contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
                    }
                }
            }
            else {
                notificationBuilder.setSmallIcon(R.drawable.ic_empty);
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
                if ((android.os.Build.VERSION.SDK_INT >= 24) && (!useDecorator)/* && (contentView != null)*/)
                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_empty);
            }

            if (notificationDarkBackground) {
                int color = getResources().getColor(R.color.notificationBackground_dark);
                contentViewLarge.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setInt(R.id.notification_activated_profile_root, "setBackgroundColor", color);
            }

            if (notificationTextColor.equals("1") && (!notificationDarkBackground)) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            }
            else
            if (notificationTextColor.equals("2") || notificationDarkBackground) {
                contentViewLarge.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                    contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            }

            contentViewLarge.setTextViewText(R.id.notification_activated_profile_name, profileName);
            if ((Build.VERSION.SDK_INT >= 24)/* && (contentView != null)*/)
                contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);
            notificationBuilder.setContentTitle(profileName);
            notificationBuilder.setContentText(profileName);

            if ((preferencesIndicator != null) && (notificationPrefIndicator))
                contentViewLarge.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentViewLarge.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (Event.getGlobalEventsRunning(getBaseContext().getApplicationContext()) &&
                    PPApplication.getApplicationStarted(getBaseContext().getApplicationContext(), true)) {
                contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.VISIBLE);
                contentViewLarge.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);
            }
            else
                contentViewLarge.setViewVisibility(R.id.notification_activated_profile_restart_events, View.GONE);

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                if (useDecorator)
                    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                //if (contentView != null) {
                    String layoutType = ApplicationPreferences.notificationLayoutType(appContext);
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
            else
                notificationBuilder.setContent(contentViewLarge);

            if ((Build.VERSION.SDK_INT >= 24) &&
                    (ApplicationPreferences.notificationShowButtonExit(appContext)) &&
                    useDecorator) {
                // add action button to stop application

                // intent to LauncherActivity, for click on notification
                Intent exitAppIntent = new Intent(appContext, ExitApplicationActivity.class);
                // clear all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pExitAppIntent = PendingIntent.getActivity(appContext, 0, exitAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Action.Builder actionBuilder = new Notification.Action.Builder(
                            Icon.createWithResource(appContext, R.drawable.ic_action_exit_app_white),
                            appContext.getString(R.string.menu_exit),
                            pExitAppIntent);
                notificationBuilder.addAction(actionBuilder.build());
            }

            Notification phoneProfilesNotification;
            try {
                phoneProfilesNotification = notificationBuilder.build();
            } catch (Exception e) {
                phoneProfilesNotification = null;
                PPApplication.logE("PhoneProfilesService._showProfileNotification", "build crash="+ Log.getStackTraceString(e));
            }

            if (phoneProfilesNotification != null) {

                if (Build.VERSION.SDK_INT < 26) {
                    phoneProfilesNotification.flags &= ~Notification.FLAG_SHOW_LIGHTS;
                    phoneProfilesNotification.ledOnMS = 0;
                    phoneProfilesNotification.ledOffMS = 0;
                }

                if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    phoneProfilesNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(appContext);
                }

                if ((Build.VERSION.SDK_INT >= 26) || notificationStatusBarPermanent) {
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                    runningInForeground = true;
                }
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                }
            }
        }
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

    void showProfileNotification() {
        //if (Build.VERSION.SDK_INT >= 26) {
            //if (BuildConfig.DEBUG)
            //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

            PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","runningInForeground="+runningInForeground);

            if (!runningInForeground) {
            //if (!isServiceRunningInForeground(appContext, PhoneProfilesService.class)) {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
                _showProfileNotification(null, false, dataWrapper);
                dataWrapper.invalidateDataWrapper();
            }
        //}

        //if (BuildConfig.DEBUG)
        //    isServiceRunningInForeground(appContext, PhoneProfilesService.class);

        PPApplication.startHandlerThreadProfileNotification();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification","instance="+PhoneProfilesService.getInstance());
                synchronized (PhoneProfilesService.class) {
                    if (instance != null) {
                        DataWrapper dataWrapper = new DataWrapper(instance.getApplicationContext(), false, 0, false);
                        Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                        PPApplication.logE("$$$ PhoneProfilesService.showProfileNotification", "_showProfileNotification()");
                        instance._showProfileNotification(profile, true, dataWrapper);
                        dataWrapper.invalidateDataWrapper();
                    }
                }
            }
        });
    }

    private void clearProfileNotification(/*Context context, boolean onlyEmpty*/)
    {
        /*if (onlyEmpty) {
            final Context appContext = getApplicationContext();
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
            _showProfileNotification(null, false, dataWrapper);
            dataWrapper.invalidateDataWrapper();
        }
        else*/ {
            try {
                final Context appContext = getApplicationContext();
                if ((Build.VERSION.SDK_INT >= 26) || ApplicationPreferences.notificationStatusBarPermanent(appContext))
                    stopForeground(true);
                else {
                    NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null)
                        notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
                }
            } catch (Exception ignored) {
            }
            runningInForeground = false;
        }
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
            /*if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
            else*/
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, time, pendingIntent);
        }
    }


    //--------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        PPApplication.logE("$$$ disableKeyguard","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reenableKeyguard()
    {
        PPApplication.logE("$$$ reenableKeyguard","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.reenableKeyguard();
    }

    //--------------------------------------

    // Location ----------------------------------------------------------------

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        //String locationProviders;

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException ignored) {
            }
            return  locationMode != Settings.Secure.LOCATION_MODE_OFF;
        /*}
        else {
            //noinspection deprecation
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return  !TextUtils.isEmpty(locationProviders);
        }*/
    }

    private void startGeofenceScanner(boolean resetUseGPS) {
        PPApplication.logE("PhoneProfilesService.startGeofenceScanner", "xxx");
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
        PPApplication.logE("PhoneProfilesService.stopGeofenceScanner", "xxx");
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

    public static boolean isOrientationScannerStarted() {
        return (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().mStartedOrientationSensors;
    }

    public static Sensor getAccelerometerSensor(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            synchronized (PPApplication.orientationScannerMutex) {
                if (PhoneProfilesService.getInstance().mOrientationSensorManager == null)
                    PhoneProfilesService.getInstance().mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
                if (PhoneProfilesService.getInstance().mOrientationSensorManager != null)
                    return PhoneProfilesService.getInstance().mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                else
                    return null;
            }
        }
        else
            return null;
    }

    public static Sensor getMagneticFieldSensor(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            synchronized (PPApplication.orientationScannerMutex) {
                if (PhoneProfilesService.getInstance().mOrientationSensorManager == null)
                    PhoneProfilesService.getInstance().mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
                if (PhoneProfilesService.getInstance().mOrientationSensorManager != null)
                    return PhoneProfilesService.getInstance().mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                else
                    return null;
            }
        }
        else
            return null;
    }

    public static Sensor getProximitySensor(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            synchronized (PPApplication.orientationScannerMutex) {
                if (PhoneProfilesService.getInstance().mOrientationSensorManager == null)
                    PhoneProfilesService.getInstance().mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
                if (PhoneProfilesService.getInstance().mOrientationSensorManager != null)
                    return PhoneProfilesService.getInstance().mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                else
                    return null;
            }
        }
        else
            return null;
    }
    /*
    public static Sensor getOrientationSensor(Context context) {
        synchronized (PPApplication.orientationScannerMutex) {
            if (mOrientationSensorManager == null)
                mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }*/

    @SuppressLint("NewApi")
    private void startListeningOrientationSensors() {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (!mStartedOrientationSensors) {

            String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(this);

            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(this);
            if (/*PPApplication.*/isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
                // start scanning in power save mode is not allowed
                return;

            //DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            if (DatabaseHandler.getInstance(getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION, false) == 0)
                return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval(this);
            if (/*PPApplication.*/isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
                interval *= 2;
            Sensor accelerometer = getAccelerometerSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","accelerometer="+accelerometer);
            if (accelerometer != null) {
                if (/*(android.os.Build.VERSION.SDK_INT >= 19) &&*/ (accelerometer.getFifoMaxEventCount() > 0))
                    mOrientationSensorManager.registerListener(this, accelerometer, 200000 * interval, 1000000 * interval);
                else
                    mOrientationSensorManager.registerListener(this, accelerometer, 1000000 * interval);
            }
            Sensor magneticField = getMagneticFieldSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","magneticField="+magneticField);
            if (magneticField != null) {
                if (/*(android.os.Build.VERSION.SDK_INT >= 19) &&*/ (magneticField.getFifoMaxEventCount() > 0))
                    mOrientationSensorManager.registerListener(this, magneticField, 200000 * interval, 1000000 * interval);
                else
                    mOrientationSensorManager.registerListener(this, magneticField, 1000000 * interval);
            }
            Sensor proximity = getProximitySensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","proximity="+proximity);
            if (proximity != null) {
                mMaxProximityDistance = proximity.getMaximumRange();
                if (/*(android.os.Build.VERSION.SDK_INT >= 19) &&*/ (proximity.getFifoMaxEventCount() > 0))
                    mOrientationSensorManager.registerListener(this, proximity, 200000 * interval, 1000000 * interval);
                else
                    mOrientationSensorManager.registerListener(this, proximity, 1000000 * interval);
            }
            //Sensor orientation = PPApplication.getOrientationSensor(this);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","orientation="+orientation);
            mStartedOrientationSensors = true;

            mDisplayUp = DEVICE_ORIENTATION_UNKNOWN;
            mSideUp = DEVICE_ORIENTATION_UNKNOWN;
            mDeviceDistance = DEVICE_ORIENTATION_UNKNOWN;

            tmpSideUp = DEVICE_ORIENTATION_UNKNOWN;
            tmpSideTimestamp = 0;
        }
    }

    private void stopListeningOrientationSensors() {
        if (mOrientationSensorManager != null) {
            mOrientationSensorManager.unregisterListener(this);
            mOrientationSensorManager = null;
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

    private void runEventsHandlerForOrientationChange(final Context context) {
        if (Event.getGlobalEventsRunning(context)) {
            PPApplication.startHandlerThread("PhoneProfilesService.runEventsHandlerForOrientationChange");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService.runEventsHandlerForOrientationChange");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("@@@ PhoneProfilesService.runEventsHandlerForOrientationChange", "-----------");

                        if (mDeviceDistance == DEVICE_ORIENTATION_DEVICE_IS_NEAR)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "now device is NEAR.");
                        else if (mDeviceDistance == DEVICE_ORIENTATION_DEVICE_IS_FAR)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "now device is FAR");
                        else if (mDeviceDistance == DEVICE_ORIENTATION_UNKNOWN)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "unknown distance");

                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) now screen is facing up.");
                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) now screen is facing down.");
                        if (mDisplayUp == DEVICE_ORIENTATION_UNKNOWN)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) unknown display orientation.");

                        if (mSideUp == DEVICE_ORIENTATION_DISPLAY_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now screen is facing up.");
                        if (mSideUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now screen is facing down.");

                        if (mSideUp == mDisplayUp)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now device is horizontal.");
                        if (mSideUp == DEVICE_ORIENTATION_UP_SIDE_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now up side is facing up.");
                        if (mSideUp == DEVICE_ORIENTATION_DOWN_SIDE_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now down side is facing up.");
                        if (mSideUp == DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now right side is facing up.");
                        if (mSideUp == DEVICE_ORIENTATION_LEFT_SIDE_UP)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now left side is facing up.");
                        if (mSideUp == DEVICE_ORIENTATION_UNKNOWN)
                            PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) unknown side.");

                        PPApplication.logE("@@@ PhoneProfilesService.runEventsHandlerForOrientationChange", "-----------");

                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(context);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
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
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Context appContext = getApplicationContext();

        int sensorType = event.sensor.getType();

        //if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        //    return;

        if (sensorType == Sensor.TYPE_PROXIMITY) {
            PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity value="+event.values[0]);
            PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity mMaxProximityDistance="+mMaxProximityDistance);
            //if ((event.values[0] == 0) || (event.values[0] == mMaxProximityDistance)) {
            //if (event.timestamp - tmpDistanceTimestamp >= 250000000L /*1000000000L*/) {
            //    tmpDistanceTimestamp = event.timestamp;
                float mProximity = event.values[0];
                //if (mProximity == 0)
                int tmpDeviceDistance;
                if (mProximity < mMaxProximityDistance)
                    tmpDeviceDistance = DEVICE_ORIENTATION_DEVICE_IS_NEAR;
                else
                    tmpDeviceDistance = DEVICE_ORIENTATION_DEVICE_IS_FAR;

                if (tmpDeviceDistance != mDeviceDistance) {
                    PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity - send broadcast");
                    mDeviceDistance = tmpDeviceDistance;
                    //DeviceOrientationJob.start(appContext);
                    runEventsHandlerForOrientationChange(appContext);
                }
            //}
            return;
        }
        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_MAGNETIC_FIELD)) {
            if (getMagneticFieldSensor(this) != null) {
                if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                    mGravity = exponentialSmoothing(event.values, mGravity, 0.2f);
                }
                if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = exponentialSmoothing(event.values, mGeomagnetic, 0.5f);
                }
                if (event.timestamp - tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    tmpSideTimestamp = event.timestamp;
                    /*if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                        // Isolate the force of gravity with the low-pass filter.
                        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
                    }
                    if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                        mGeomagnetic[0] = event.values[0];
                        mGeomagnetic[1] = event.values[1];
                        mGeomagnetic[2] = event.values[2];
                    }*/
                    if (mGravity != null && mGeomagnetic != null) {
                        float R[] = new float[9];
                        float I[] = new float[9];
                        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                        if (success) {
                            float orientation[] = new float[3];
                            //orientation[0]: azimuth, rotation around the -Z axis, i.e. the opposite direction of Z axis.
                            //orientation[1]: pitch, rotation around the -X axis, i.e the opposite direction of X axis.
                            //orientation[2]: roll, rotation around the Y axis.

                            //noinspection SuspiciousNameCombination
                            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, I);
                            SensorManager.getOrientation(I, orientation);

                            //float azimuth = (float)Math.toDegrees(orientation[0]);
                            float pitch = (float) Math.toDegrees(orientation[1]);
                            float roll = (float) Math.toDegrees(orientation[2]);

                            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "pitch=" + pitch);
                            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "roll=" + roll);

                            int side = DEVICE_ORIENTATION_UNKNOWN;
                            if (pitch > -30 && pitch < 30) {
                                if (roll > -60 && roll < 60)
                                    side = DEVICE_ORIENTATION_DISPLAY_UP;
                                if (roll > 150 && roll < 180)
                                    side = DEVICE_ORIENTATION_DISPLAY_DOWN;
                                if (roll > -180 && roll < -150)
                                    side = DEVICE_ORIENTATION_DISPLAY_DOWN;
                                if (roll > 65 && roll < 115)
                                    side = DEVICE_ORIENTATION_UP_SIDE_UP;
                                if (roll > -115 && roll < -65)
                                    side = DEVICE_ORIENTATION_DOWN_SIDE_UP;
                            }
                            if (pitch > 30 && pitch < 90) {
                                side = DEVICE_ORIENTATION_LEFT_SIDE_UP;
                            }
                            if (pitch > -90 && pitch < -30) {
                                side = DEVICE_ORIENTATION_RIGHT_SIDE_UP;
                            }

                            if ((tmpSideUp == DEVICE_ORIENTATION_UNKNOWN) || (/*(side != DEVICE_ORIENTATION_UNKNOWN) &&*/ (side != tmpSideUp))) {
                                mEventCountSinceGZChanged = 0;

                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "azimuth="+azimuth);
                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "pitch=" + pitch);
                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "roll=" + roll);

                                tmpSideUp = side;
                            } else {
                                ++mEventCountSinceGZChanged;
                                if (mEventCountSinceGZChanged == MAX_COUNT_GZ_CHANGE) {

                                    if (tmpSideUp != mSideUp) {
                                        PPApplication.logE("PhoneProfilesService.onSensorChanged", "magnetic+accelerometer - send broadcast");

                                        mSideUp = tmpSideUp;

                                        if ((mSideUp == DEVICE_ORIENTATION_DISPLAY_UP) || (mSideUp == DEVICE_ORIENTATION_DISPLAY_DOWN))
                                            mDisplayUp = mSideUp;

                                        /*
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing up.");
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing down.");

                                        if (mSideUp == DEVICE_ORIENTATION_UP_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now up side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_DOWN_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now down side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now right side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_LEFT_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now left side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_UNKNOWN)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "unknown side.");
                                        */

                                        //DeviceOrientationJob.start(appContext);
                                        runEventsHandlerForOrientationChange(appContext);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                if (event.timestamp - tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    tmpSideTimestamp = event.timestamp;

                    float gravityZ = event.values[2];
                    if (mGravityZ == 0) {
                        mGravityZ = gravityZ;
                    } else {
                        if ((mGravityZ * gravityZ) < 0) {
                            mEventCountSinceGZChanged++;
                            if (mEventCountSinceGZChanged == MAX_COUNT_GZ_CHANGE) {
                                PPApplication.logE("PhoneProfilesService.onSensorChanged", "accelerometer - send broadcast");

                                mGravityZ = gravityZ;
                                mEventCountSinceGZChanged = 0;

                                if (gravityZ > 0) {
                                    //PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing up.");
                                    mSideUp = DEVICE_ORIENTATION_DISPLAY_UP;
                                } else if (gravityZ < 0) {
                                    //PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing down.");
                                    mSideUp = DEVICE_ORIENTATION_DISPLAY_DOWN;
                                }

                                if ((mSideUp == DEVICE_ORIENTATION_DISPLAY_UP) || (mSideUp == DEVICE_ORIENTATION_DISPLAY_DOWN))
                                    mDisplayUp = mSideUp;

                                //DeviceOrientationJob.start(appContext);
                                runEventsHandlerForOrientationChange(appContext);
                            }
                        } else {
                            if (mEventCountSinceGZChanged > 0) {
                                mGravityZ = gravityZ;
                                mEventCountSinceGZChanged = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private float[] exponentialSmoothing(float[] input, float[] output, float alpha) {
        if (output == null)
            return input;
        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //---------------------------

    private void doSimulatingRingingCall(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false))
        {
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulate ringing call");

            Context context = getApplicationContext();

            ringingCallIsSimulating = false;

            // wait for change ringer mode + volume
            PPApplication.sleep(1500);

            int oldRingerMode = intent.getIntExtra(EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EXTRA_OLD_ZEN_MODE, 0);
            String oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE);
            int oldSystemRingerVolume = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_VOLUME, -1);
            int newRingerMode = ActivateProfileHelper.getRingerMode(context);
            int newZenMode = ActivateProfileHelper.getZenMode(context);
            int newRingerVolume = ActivateProfileHelper.getRingerVolume(context);
            String newRingtone = "";
            String phoneNumber = ApplicationPreferences.preferences.getString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, "");

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
                        PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingtone="+newRingtone);
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
                } catch (SecurityException e) {
                    Permissions.grantPlayRingtoneNotificationPermissions(context, false);
                    newRingtone = "";
                } catch (Exception e) {
                    newRingtone = "";
                }
            }

            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingerMode=" + oldRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldSystemRingerMode=" + oldSystemRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldZenMode=" + oldZenMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingerMode=" + newRingerMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newZenMode=" + newZenMode);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldRingtone=" + oldRingtone);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "oldSystemRingerVolume=" + oldSystemRingerVolume);
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "newRingerVolume=" + newRingerVolume);

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {

                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "ringing is audible");

                boolean simulateRinging = false;
                int stream = AudioManager.STREAM_RING;

                if ((android.os.Build.VERSION.SDK_INT >= 21)) {
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
                           PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (1)");
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
                                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (2)");
                            }
                            else {
                                //stream = AudioManager.STREAM_RING;
                                stream = AudioManager.STREAM_ALARM;
                                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=RING (2)");
                            }
                        }
                    }

                }

                if (oldRingtone.isEmpty() || (!newRingtone.isEmpty() && !newRingtone.equals(oldRingtone)))
                    simulateRinging = true;

                PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulateRinging=" + simulateRinging);

                if (simulateRinging)
                    startSimulatingRingingCall(stream, newRingtone);
            }

        }
    }

    private void startSimulatingRingingCall(int stream, String ringtone) {
        stopSimulatingRingingCall(/*true*/);
        if (!ringingCallIsSimulating) {
            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "stream="+stream);
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

                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingVolume=" + ringingVolume);

                        int maximumRingValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) ringingVolume / maximumRingValue * 100.0f;
                        int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "mediaRingingVolume=" + mediaRingingVolume);

                        /*if (android.os.Build.VERSION.SDK_INT >= 23)
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                        else
                            audioManager.setStreamMute(AudioManager.STREAM_RING, true);*/
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, 0);

                        ringingMediaPlayer.start();

                        ringingCallIsSimulating = true;
                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringing played");
                    //} else
                    //    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "focus not granted");
                } catch (SecurityException e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", " security exception");
                    ringingMediaPlayer = null;
                    PPApplication.startHandlerThreadInternalChangeToFalse();
                    final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                    //PostDelayedBroadcastReceiver.setAlarm(
                    //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                    Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                } catch (Exception e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", Log.getStackTraceString(e));
                    ringingMediaPlayer = null;
                    PPApplication.startHandlerThreadInternalChangeToFalse();
                    final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                    //PostDelayedBroadcastReceiver.setAlarm(
                    //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                    Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                }
            }
        }
    }

    public void stopSimulatingRingingCall(/*boolean abandonFocus*/) {
        //if (ringingCallIsSimulating) {
            PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "xxx");
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
                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "ringing stopped");
            }
            /*if (abandonFocus) {
                if (audioManager != null)
                    audioManager.abandonAudioFocus(this);
            }*/
        //}
        ringingCallIsSimulating = false;
        PPApplication.startHandlerThreadInternalChangeToFalse();
        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "disable ringer mode change internal change");
                RingerModeChangeReceiver.internalChange = false;
            }
        }, 3000);
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
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "vibration");
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        }

        PPApplication.logE("PhoneProfilesService.playNotificationSound", "ringingCallIsSimulating="+ringingCallIsSimulating);
        if ((!ringingCallIsSimulating)/* && (!notificationToneIsSimulating)*/) {

            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            stopPlayNotificationSound();

            PPApplication.logE("PhoneProfilesService.playNotificationSound", "notificationSound="+notificationSound);
            if (!notificationSound.isEmpty())
            {
                int ringerMode = ActivateProfileHelper.getRingerMode(getApplicationContext());
                int zenMode = ActivateProfileHelper.getZenMode(getApplicationContext());
                boolean isAudible = ActivateProfileHelper.isAudibleRinging(ringerMode, zenMode/*, false*/);
                PPApplication.logE("PhoneProfilesService.playNotificationSound", "isAudible="+isAudible);
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
                                    PPApplication.logE("PhoneProfilesService.playNotificationSound", "notification stopped");
                                }

                                notificationIsPlayed = false;
                                notificationMediaPlayer = null;

                                PPApplication.startHandlerThreadInternalChangeToFalse();
                                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);
                                //PostDelayedBroadcastReceiver.setAlarm(
                                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, context);

                                notificationPlayTimer = null;
                            }
                        }, notificationMediaPlayer.getDuration());

                    } catch (SecurityException e) {
                        PPApplication.logE("PhoneProfilesService.playNotificationSound", "security exception");
                        stopPlayNotificationSound();
                        PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
                        //PostDelayedBroadcastReceiver.setAlarm(
                        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);
                        Permissions.grantPlayRingtoneNotificationPermissions(this, false);
                    } catch (Exception e) {
                        PPApplication.logE("PhoneProfilesService.playNotificationSound", "exception");
                        stopPlayNotificationSound();
                        PPApplication.startHandlerThreadInternalChangeToFalse();
                        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RingerModeChangeReceiver.internalChange = false;
                            }
                        }, 3000);
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
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxx");

        if (PPApplication.screenTimeoutHandler != null) {
            PPApplication.screenTimeoutHandler.post(new Runnable() {
                public void run() {
                    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());
                }
            });
        }// else
        //    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());

        super.onTaskRemoved(rootIntent);
    }

}
