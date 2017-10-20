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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class PhoneProfilesService extends Service
                                    implements SensorEventListener,
                                    AudioManager.OnAudioFocusChangeListener
{
    public static PhoneProfilesService instance = null;
    private static boolean serviceRunning = false;

    private static KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    private static KeyguardManager.KeyguardLock keyguardLock = null;

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
    //private AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    private WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    private LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    private AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    private SMSBroadcastReceiver smsBroadcastReceiver = null;
    private SMSBroadcastReceiver mmsBroadcastReceiver = null;
    private CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;
    private WifiScanBroadcastReceiver wifiScanReceiver = null;
    private BluetoothScanBroadcastReceiver bluetoothScanReceiver = null;
    private BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = null;

    private PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    private DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;

    private SettingsContentObserver settingsContentObserver = null;
    private MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;

    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_ONLY_START = "only_start";
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

    //-----------------------

    private static GeofencesScanner geofencesScanner = null;

    private static SensorManager mOrientationSensorManager = null;
    private static boolean mStartedOrientationSensors = false;

    private int mEventCountSinceGZChanged = 0;
    private static final int MAX_COUNT_GZ_CHANGE = 5;
    private float mGravity[] = new float[3];
    private float mGeomagnetic[] = new float[3];
    private float mMaxProximityDistance;
    private float mGravityZ = 0;  //gravity acceleration along the z axis

    public static PhoneStateScanner phoneStateScanner = null;

    public static final int DEVICE_ORIENTATION_UNKNOWN = 0;
    public static final int DEVICE_ORIENTATION_RIGHT_SIDE_UP = 3;
    public static final int DEVICE_ORIENTATION_LEFT_SIDE_UP = 4;
    public static final int DEVICE_ORIENTATION_UP_SIDE_UP = 5;
    public static final int DEVICE_ORIENTATION_DOWN_SIDE_UP = 6;
    public static final int DEVICE_ORIENTATION_HORIZONTAL = 9;

    public static final int DEVICE_ORIENTATION_DISPLAY_UP = 1;
    public static final int DEVICE_ORIENTATION_DISPLAY_DOWN = 2;

    public static final int DEVICE_ORIENTATION_DEVICE_IS_NEAR = 7;
    public static final int DEVICE_ORIENTATION_DEVICE_IS_FAR = 8;

    public static int mDisplayUp = DEVICE_ORIENTATION_UNKNOWN;
    public static int mSideUp = DEVICE_ORIENTATION_UNKNOWN;
    public static int mDeviceDistance = DEVICE_ORIENTATION_UNKNOWN;

    private static int tmpSideUp = DEVICE_ORIENTATION_UNKNOWN;
    private static long tmpSideTimestamp = 0;

    //------------------------

    private AudioManager audioManager = null;
    private boolean ringingCallIsSimulating = false;
    //private boolean notificationToneIsSimulating = false;
    public static int ringingVolume = 0;
    //public static int notificationVolume = 0;
    private int oldMediaVolume = 0;
    private MediaPlayer ringingMediaPlayer = null;
    //private MediaPlayer notificationMediaPlayer = null;
    private int mediaRingingVolume = 0;
    //private int mediaNotificationVolume = 0;
    private int usedRingingStream = AudioManager.STREAM_MUSIC;
    //private int usedNotificationStream = AudioManager.STREAM_MUSIC;

    private MediaPlayer eventNotificationMediaPlayer = null;
    private boolean eventNotificationIsPlayed = false;
    private Timer eventNotificationPlayTimer = null;

    public static String connectToSSID = Profile.CONNECTTOSSID_JUSTANY;
    public static boolean connectToSSIDStarted = false;

    //--------------------------

    //public static SipManager mSipManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "android.os.Build.VERSION.SDK_INT=" + android.os.Build.VERSION.SDK_INT);

        instance = this;
        Context appContext = getApplicationContext();

        try {
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(appContext));
        } catch (Exception ignored) {}

        // save version code (is used in PackageReplacedReceiver)
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int actualVersionCode = pInfo.versionCode;
            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
        } catch (Exception e) {
            //e.printStackTrace();
        }

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
        keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        registerReceiversAndJobs();

        AboutApplicationJob.scheduleJob();

        ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OK created");
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxxxx");

        unregisterReceiversAndJobs();

        stopSimulatingRingingCall(true);
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        removeProfileNotification(this);

        instance = null;
        serviceRunning = false;

        super.onDestroy();
    }

    private void registerAllTheTimeRequiredReceivers(boolean register, boolean unregister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
        PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "xxx");
        if (unregister) {
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
        }
        if (register) {
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

            // required for unlink ring and notification volume + call event
            if (phoneCallBroadcastReceiver == null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER phone call", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER phone call");
                phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
                IntentFilter intentFilter6 = new IntentFilter();
                intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
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
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerAllTheTimeRequiredReceivers->REGISTER settings content observer", "PhoneProfilesService_registerAllTheTimeRequiredReceivers");
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "REGISTER settings content observer");
                //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
                settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
                appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerAllTheTimeRequiredReceivers", "registered settings content observer");

            // required for start EventsHandlerJob in idle maintenance window
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
        }
    }

    private void registerBatteryEventReceiver(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryEventReceiver", "PhoneProfilesService_registerBatteryEventReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryEventReceiver", "xxx");
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase/* || (powerSaveModeReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY);
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
                    registerBatteryEventReceiver(false, true, false);
                }
            }
            else
                registerBatteryEventReceiver(false, true, false);
        }
    }

    private void registerBatteryChangedReceiver(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBatteryChangedReceiver", "PhoneProfilesService_registerBatteryChangedReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBatteryChangedReceiver", "xxx");
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                int batteryLevelCount = 1;
                if (checkDatabase/* || (batteryChangeLevelReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION);
                    //eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY);
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
                    registerBatteryChangedReceiver(false, true, false);
                }
            }
            else
                registerBatteryChangedReceiver(false, true, false);
        }
    }

    private void registerReceiverForPeripheralsSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForPeripheralsSensor", "PhoneProfilesService_registerReceiverForPeripheralsSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForPeripheralsSensor", "xxx");
        if (unregister) {
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
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (headsetPlugReceiver == null) || (dockConnectionBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL);
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
                    registerReceiverForPeripheralsSensor(false, true, false);
                }
            }
            else
                registerReceiverForPeripheralsSensor(false, true, false);
        }
    }

    private void registerReceiverForSMSSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForSMSSensor", "PhoneProfilesService_registerReceiverForSMSSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "xxx");
        if (unregister) {
            if (smsBroadcastReceiver != null) {
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
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "not registered MMS");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (smsBroadcastReceiver == null) || (mmsBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SMS);
                if (eventCount > 0) {
                    if (smsBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForSMSSensor->REGISTER SMS", "PhoneProfilesService_registerReceiverForSMSSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "REGISTER SMS");
                        smsBroadcastReceiver = new SMSBroadcastReceiver();
                        IntentFilter intentFilter21 = new IntentFilter();
                        if (android.os.Build.VERSION.SDK_INT >= 19)
                            intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                        else
                            intentFilter21.addAction("android.provider.Telephony.SMS_RECEIVED");
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
                        if (android.os.Build.VERSION.SDK_INT >= 19)
                            intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
                        else
                            intentFilter22 = IntentFilter.create("android.provider.Telephony.WAP_PUSH_RECEIVED", "application/vnd.wap.mms-message");
                        intentFilter22.setPriority(Integer.MAX_VALUE);
                        appContext.registerReceiver(mmsBroadcastReceiver, intentFilter22);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForSMSSensor", "registered MMS");
                } else {
                    registerReceiverForSMSSensor(false, true, false);
                }
            }
            else
                registerReceiverForSMSSensor(false, true, false);
        }
    }

    private void registerReceiverForCalendarSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor", "PhoneProfilesService_registerReceiverForCalendarSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "xxx");
        if (unregister) {
            if (calendarProviderChangedBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->UNREGISTER", "PhoneProfilesService_registerReceiverForCalendarSensor");
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(calendarProviderChangedBroadcastReceiver);
                    calendarProviderChangedBroadcastReceiver = null;
                } catch (Exception e) {
                    calendarProviderChangedBroadcastReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "not registered");
        }
        if (register) {
            if (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (calendarProviderChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR);
                if (eventCount > 0) {
                    if (calendarProviderChangedBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForCalendarSensor->REGISTER", "PhoneProfilesService_registerReceiverForCalendarSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "REGISTER");
                        calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
                        IntentFilter intentFilter23 = new IntentFilter();
                        intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
                        intentFilter23.addDataScheme("content");
                        intentFilter23.addDataAuthority("com.android.calendar", null);
                        intentFilter23.setPriority(Integer.MAX_VALUE);
                        appContext.registerReceiver(calendarProviderChangedBroadcastReceiver, intentFilter23);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForCalendarSensor", "registered");
                } else {
                    registerReceiverForCalendarSensor(false, true, false);
                }
            }
            else
                registerReceiverForCalendarSensor(false, true, false);
        }
    }

    private void registerReceiverForRadioSwitchAirplaneModeSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "PhoneProfilesService_registerReceiverForRadioSwitchAirplaneModeSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchAirplaneModeSensor", "xxx");
        if (unregister) {
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
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (airplaneModeStateChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE);
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
                    registerReceiverForRadioSwitchAirplaneModeSensor(false, true, false);
                }
            }
            else
                registerReceiverForRadioSwitchAirplaneModeSensor(false, true, false);
        }
    }

    private void registerReceiverForRadioSwitchNFCSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "xxx");
        if (unregister) {
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
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (nfcStateChangedBroadcastReceiver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_NFC);
                if (eventCount > 0) {
                    if (nfcStateChangedBroadcastReceiver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchNFCSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "REGISTER");
                        if (android.os.Build.VERSION.SDK_INT >= 18) {
                            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                                nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                                IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                                appContext.registerReceiver(nfcStateChangedBroadcastReceiver, intentFilter21);
                                //PPApplication.logE("$$$ PhoneProfilesService.onCreate", "registered");
                            }
                        }
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchNFCSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchNFCSensor(false, true, false);
                }
            } else
                registerReceiverForRadioSwitchNFCSensor(false, true, false);
        }
    }

    private void registerReceiverForRadioSwitchMobileDataSensor(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "xxx");
        if (unregister) {
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
            if (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED) {
                int eventCount = 1;
                if (checkDatabase/* || (mobileDataStateChangedContentObserver == null)*/)
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA);
                if (eventCount > 0) {
                    if (mobileDataStateChangedContentObserver == null) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor->REGISTER", "PhoneProfilesService_registerReceiverForRadioSwitchMobileDataSensor");
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "REGISTER");
                        mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
                        else
                            appContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiverForRadioSwitchMobileDataSensor", "registered");
                } else {
                    registerReceiverForRadioSwitchMobileDataSensor(false, true, false);
                }
            }
            else
                registerReceiverForRadioSwitchMobileDataSensor(false, true, false);
        }
    }

    private void registerLocationModeChangedBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "PhoneProfilesService_registerLocationModeChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "xxx");
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED);
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (locationModeChangedBroadcastReceiver == null)*/) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    //noinspection deprecation
                    if (pm.isScreenOn() || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_GPS);
                        eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    }
                    else {
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
                        if (android.os.Build.VERSION.SDK_INT >= 19)
                            intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
                        appContext.registerReceiver(locationModeChangedBroadcastReceiver, intentFilter18);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerLocationModeChangedBroadcastReceiver", "registered");
                } else {
                    registerLocationModeChangedBroadcastReceiver(false, true, false);
                }
            }
            else
                registerLocationModeChangedBroadcastReceiver(false, true, false);
        }
    }

    private void registerBluetoothStateChangedBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "PhoneProfilesService_registerBluetoothStateChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED);
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (bluetoothStateChangedBroadcastReceiver == null)*/) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    //noinspection deprecation
                    if (pm.isScreenOn() || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH);
                        eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED);
                        eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    }
                    else {
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
                    registerBluetoothStateChangedBroadcastReceiver(false, true, false, forceRegister);
                }
            }
            else
                registerBluetoothStateChangedBroadcastReceiver(false, true, false, forceRegister);
        }
    }

    private void registerBluetoothConnectionBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (unregister) {
            if (bluetoothConnectionBroadcastReceiver != null) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver->UNREGISTER", "PhoneProfilesService_registerBluetoothConnectionBroadcastReceiver");
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "UNREGISTER");
                try {
                    appContext.unregisterReceiver(bluetoothConnectionBroadcastReceiver);
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
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (bluetoothConnectionBroadcastReceiver == null)*/) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    //noinspection deprecation
                    if (pm.isScreenOn() || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
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
                        bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
                        IntentFilter intentFilter14 = new IntentFilter();
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                        intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                        appContext.registerReceiver(bluetoothConnectionBroadcastReceiver, intentFilter14);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothConnectionBroadcastReceiver", "registered");
                } else {
                    registerBluetoothConnectionBroadcastReceiver(false, true, false, forceRegister);
                }
            }
            else
                registerBluetoothConnectionBroadcastReceiver(false, true, false, forceRegister);
        }
    }

    private void registerBluetoothScannerReceivers(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerBluetoothScannerReceivers", "PhoneProfilesService_registerBluetoothScannerReceivers");
        PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "xxx");
        if (!forceRegister && BluetoothNamePreference.forceRegister)
            return;
        if (unregister) {
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
                    appContext.unregisterReceiver(bluetoothLEScanReceiver);
                    bluetoothLEScanReceiver = null;
                } catch (Exception e) {
                    bluetoothLEScanReceiver = null;
                }
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.registerBluetoothScannerReceivers", "not registered bluetoothLEScanReceiver");
        }
        if (register) {
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    //noinspection deprecation
                    if (pm.isScreenOn() || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    }
                    else
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
                    registerBluetoothScannerReceivers(false, true, false, forceRegister);
                }
            }
            else
                registerBluetoothScannerReceivers(false, true, false, forceRegister);
        }
    }

    private void registerWifiAPStateChangeBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (wifiAPStateChangeBroadcastReceiver == null)*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    }
                    if (eventCount > 0) {
                        if (wifiAPStateChangeBroadcastReceiver == null) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver->REGISTER", "PhoneProfilesService_registerWifiAPStateChangeBroadcastReceiver");
                            PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "REGISTER");
                            wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
                            IntentFilter intentFilter17 = new IntentFilter();
                            intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                            appContext.registerReceiver(wifiAPStateChangeBroadcastReceiver, intentFilter17);
                        }
                        else
                            PPApplication.logE("[RJS] PhoneProfilesService.registerWifiAPStateChangeBroadcastReceiver", "registered");
                    } else
                        registerWifiAPStateChangeBroadcastReceiver(false, true, false, forceRegister);
                }
                else
                    registerWifiAPStateChangeBroadcastReceiver(false, true, false, forceRegister);
            }
            else
                registerWifiAPStateChangeBroadcastReceiver(false, true, false, forceRegister);
        }
    }

    private void registerPowerSaveModeReceiver(boolean register, boolean unregister, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerPowerSaveModeReceiver", "PhoneProfilesService_registerPowerSaveModeReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerPowerSaveModeReceiver", "xxx");
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            allowed = allowed || Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase/* || (powerSaveModeReceiver == null)*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION);
                    eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY);
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
                    registerPowerSaveModeReceiver(false, true, false);
                }
            }
            else
                registerPowerSaveModeReceiver(false, true, false);
        }
    }

    private void registerWifiStateChangedBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.wifiStateChangedBroadcastReceiver", "PhoneProfilesService_wifiStateChangedBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiStateChangedBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (unregister) {
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
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (profileAllowed || eventAllowed) {
                int profileCount = 1;
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (wifiStateChangedBroadcastReceiver == null)*/) {
                    if (profileAllowed) {
                        profileCount = 0;
                        if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, true) > 0) {
                            Profile profile = Profile.getDefaultProfile(appContext);
                            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                                ++profileCount;
                        }
                        else
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, false);
                    }
                    if (eventAllowed) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        //noinspection deprecation
                        if (pm.isScreenOn() || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED);
                            eventCount = eventCount + DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_RADIO_SWITCH_WIFI);
                        }
                        else {
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
                    registerWifiStateChangedBroadcastReceiver(false, true, false, forceRegister);
                }
            }
            else
                registerWifiStateChangedBroadcastReceiver(false, true, false, forceRegister);
        }
    }

    private void registerWifiConnectionBroadcastReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "PhoneProfilesService_registerWifiConnectionBroadcastReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiConnectionBroadcastReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (unregister) {
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
            boolean profileAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (profileAllowed || eventAllowed) {
                int profileCount = 1;
                int eventCount = 1;
                int eventScannerCount = 1;
                if (checkDatabase/* || (wifiConnectionBroadcastReceiver == null)*/) {
                    if (profileAllowed) {
                        profileCount = 0;
                        if (DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, true) > 0) {
                            Profile profile = Profile.getDefaultProfile(appContext);
                            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY))
                                ++profileCount;
                        }
                        else
                            profileCount = DatabaseHandler.getInstance(appContext).getTypeProfilesCount(DatabaseHandler.PTYPE_CONNECT_TO_SSID, false);
                    }
                    if (eventAllowed) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        //noinspection deprecation
                        if (pm.isScreenOn() || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                            // start only for screen On
                            eventScannerCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                            eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED);
                        }
                        else {
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
                    registerWifiConnectionBroadcastReceiver(false, true, false, forceRegister);
                }
            }
            else
                registerWifiConnectionBroadcastReceiver(false, true, false, forceRegister);
        }
    }

    private void registerWifiScannerReceiver(boolean register, boolean unregister, boolean checkDatabase, boolean forceRegister) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.registerWifiScannerReceiver", "PhoneProfilesService_registerWifiScannerReceiver");
        PPApplication.logE("[RJS] PhoneProfilesService.registerWifiScannerReceiver", "xxx");
        if (!forceRegister && WifiSSIDPreference.forceRegister)
            return;
        if (unregister) {
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
            boolean allowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (allowed) {
                int eventCount = 1;
                if (checkDatabase) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    //noinspection deprecation
                    if (pm.isScreenOn() || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                        // start only for screen On
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    }
                    else
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
                    registerWifiScannerReceiver(false, true, false, forceRegister);
                }
            }
            else
                registerWifiScannerReceiver(false, true, false, forceRegister);
        }
    }

    void scheduleWifiJob(boolean schedule, boolean cancel, boolean checkDatabase, boolean forScreenOn, boolean afterEnableWifi,
                         boolean forceStart, boolean rescan) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleWifiJob", "PhoneProfilesService_scheduleWifiJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "xxx");
        if (!forceStart && WifiSSIDPreference.forceRegister)
            return;
        if (cancel) {
            if (WifiScanJob.isJobScheduled()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleWifiJob->CANCEL", "PhoneProfilesService_scheduleWifiJob");
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "CANCEL");
                WifiScanJob.cancelJob(appContext);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "not scheduled");
        }
        if (schedule) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!WifiScanJob.isJobScheduled())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT);
                    }
                    if (eventCount > 0) {
                        if (!WifiScanJob.isJobScheduled()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleWifiJob->SCHEDULE", "PhoneProfilesService_scheduleWifiJob");
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "SCHEDULE");
                            WifiScanJob.scheduleJob(appContext, true, forScreenOn, afterEnableWifi);
                        }
                        else {
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleWifiJob", "scheduled");
                            if (rescan)
                                WifiScanJob.scheduleJob(appContext, true, forScreenOn, afterEnableWifi);
                        }
                    } else
                        scheduleWifiJob(false, true, false, forScreenOn, afterEnableWifi, forceStart, rescan);
                }
                else
                    scheduleWifiJob(false, true, false, forScreenOn, afterEnableWifi, forceStart, rescan);
            }
            else
                scheduleWifiJob(false, true, false, forScreenOn, afterEnableWifi, forceStart, rescan);
        }
    }

    void scheduleBluetoothJob(boolean schedule, boolean cancel, boolean checkDatabase, boolean forScreenOn, boolean forceStart,
                              boolean rescan) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleBluetoothJob", "PhoneProfilesService_scheduleBluetoothJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "xxx");
        if (!forceStart && BluetoothNamePreference.forceRegister)
            return;
        if (cancel) {
            if (BluetoothScanJob.isJobScheduled()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleBluetoothJob->CANCEL", "PhoneProfilesService_scheduleBluetoothJob");
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "CANCEL");
                BluetoothScanJob.cancelJob(appContext);
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "not scheduled");
        }
        if (schedule) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!BluetoothScanJob.isJobScheduled())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT);
                    }
                    if (eventCount > 0) {
                        if (!BluetoothScanJob.isJobScheduled()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleBluetoothJob->SCHEDULE", "PhoneProfilesService_scheduleBluetoothJob");
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "SCHEDULE");
                            BluetoothScanJob.scheduleJob(appContext, true, forScreenOn);
                        }
                        else {
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleBluetoothJob", "scheduled");
                            if (rescan)
                                BluetoothScanJob.scheduleJob(appContext, true, forScreenOn);
                        }
                    } else
                        scheduleBluetoothJob(false, true, false, forScreenOn, forceStart, rescan);
                }
                else
                    scheduleBluetoothJob(false, true, false, forScreenOn, forceStart, rescan);
            }
            else
                scheduleBluetoothJob(false, true, false, forScreenOn, forceStart, rescan);
        }
    }

    void scheduleGeofenceScannerJob(boolean schedule, boolean cancel, boolean checkDatabase, boolean forScreenOn,
                                    boolean rescan) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleGeofenceScannerJob", "PhoneProfilesService_scheduleGeofenceScannerJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "xxx");
        if (cancel) {
            if (GeofenceScannerJob.isJobScheduled()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleGeofenceScannerJob->CANCEL", "PhoneProfilesService_scheduleGeofenceScannerJob");
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "CANCEL");
                GeofenceScannerJob.cancelJob();
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "not scheduled");
        }
        if (schedule) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!GeofenceScannerJob.isJobScheduled())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    }
                    if (eventCount > 0) {
                        if (!GeofenceScannerJob.isJobScheduled()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleGeofenceScannerJob->SCHEDULE", "PhoneProfilesService_scheduleGeofenceScannerJob");
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "SCHEDULE");
                            if (isGeofenceScannerStarted())
                                getGeofencesScanner().updateTransitionsByLastKnownLocation();
                            GeofenceScannerJob.scheduleJob(appContext, true, forScreenOn);
                        }
                        else {
                            PPApplication.logE("[RJS] PhoneProfilesService.scheduleGeofenceScannerJob", "scheduled");
                            if (rescan) {
                                if (isGeofenceScannerStarted())
                                    getGeofencesScanner().updateTransitionsByLastKnownLocation();
                                GeofenceScannerJob.scheduleJob(appContext, true, forScreenOn);
                            }
                        }
                    } else
                        scheduleGeofenceScannerJob(false, true, false, forScreenOn, rescan);
                }
                else
                    scheduleGeofenceScannerJob(false, true, false, forScreenOn, rescan);
            }
            else
                scheduleGeofenceScannerJob(false, true, false, forScreenOn, rescan);
        }
    }

    private void scheduleSearchCalendarEventsJob(boolean schedule, boolean cancel, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsJob", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
        PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "xxx");
        if (cancel) {
            if (SearchCalendarEventsJob.isJobScheduled()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsJob->CANCEL", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "CANCEL");
                SearchCalendarEventsJob.cancelJob();
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "not scheduled");
        }
        if (schedule) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                int eventCount = 1;
                if (checkDatabase/* || (!SearchCalendarEventsJob.isJobScheduled())*/) {
                    eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR);
                }
                if (eventCount > 0) {
                    if (!SearchCalendarEventsJob.isJobScheduled()) {
                        CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.scheduleSearchCalendarEventsJob->SCHEDULE", "PhoneProfilesService_scheduleSearchCalendarEventsJob");
                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "SCHEDULE");
                        SearchCalendarEventsJob.scheduleJob(true);
                    }
                    else
                        PPApplication.logE("[RJS] PhoneProfilesService.scheduleSearchCalendarEventsJob", "scheduled");
                } else {
                    scheduleSearchCalendarEventsJob(false, true, false);
                }
            }
            else
                scheduleSearchCalendarEventsJob(false, true, false);
        }
    }

    private void startGeofenceScanner(boolean start, boolean stop, boolean checkDatabase, boolean forScreenOn, boolean rescan) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.startGeofenceScanner", "PhoneProfilesService_startGeofenceScanner");
        PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "xxx");
        if (stop) {
            if (isGeofenceScannerStarted()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->STOP", "PhoneProfilesService_startGeofenceScanner");
                PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "STOP");
                stopGeofenceScanner();
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "not started");
        }
        if (start) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!isGeofenceScannerStarted())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION);
                    }
                    if (eventCount > 0) {
                        if (!isGeofenceScannerStarted()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startGeofenceScanner->START", "PhoneProfilesService_startGeofenceScanner");
                            PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "START");
                            startGeofenceScanner();
                        }
                        else {
                            PPApplication.logE("[RJS] PhoneProfilesService.startGeofenceScanner", "started");
                            if (rescan)
                                scheduleGeofenceScannerJob(true, stop, checkDatabase, forScreenOn, true);
                        }
                    } else
                        startGeofenceScanner(false, true, false, forScreenOn, rescan);
                }
                else
                    startGeofenceScanner(false, true, false, forScreenOn, rescan);
            }
            else
                startGeofenceScanner(false, true, false, forScreenOn, rescan);
        }
    }

    private void startPhoneStateScanner(boolean start, boolean stop, boolean checkDatabase, boolean forceStart,
                                        boolean rescan) {
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
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "not started");
        }
        if (start) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!isPhoneStateScannerStarted())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS);
                    }
                    if (eventCount > 0) {
                        if (!isPhoneStateScannerStarted()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startPhoneStateScanner->START", "PhoneProfilesService_startPhoneStateScanner");
                            PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "START");
                            startPhoneStateScanner();
                        }
                        else {
                            PPApplication.logE("[RJS] PhoneProfilesService.startPhoneStateScanner", "started");
                            if (rescan)
                                phoneStateScanner.rescanMobileCells();
                        }
                    } else
                        startPhoneStateScanner(false, true, false, forceStart, rescan);
                }
                else
                    startPhoneStateScanner(false, true, false, forceStart, rescan);
            }
            else
                startPhoneStateScanner(false, true, false, forceStart, rescan);
        }
    }

    private void startOrientationScanner(boolean start, boolean stop, boolean checkDatabase) {
        Context appContext = getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneProfilesService.startOrientationScanner", "PhoneProfilesService_startOrientationScanner");
        PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "xxx");
        if (stop) {
            if (isOrientationScannerStarted()) {
                CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->STOP", "PhoneProfilesService_startOrientationScanner");
                PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "STOP");
                stopOrientationScanner();
            }
            else
                PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "not started");
        }
        if (start) {
            boolean eventAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, appContext) ==
                    PPApplication.PREFERENCE_ALLOWED;
            if (eventAllowed) {
                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                //noinspection deprecation
                if (pm.isScreenOn() || !ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(appContext)) {
                    // start only for screen On
                    int eventCount = 1;
                    if (checkDatabase/* || (!isOrientationScannerStarted())*/) {
                        eventCount = DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION);
                    }
                    if (eventCount > 0) {
                        if (!isOrientationScannerStarted()) {
                            CallsCounter.logCounterNoInc(appContext, "PhoneProfilesService.startOrientationScanner->START", "PhoneProfilesService_startOrientationScanner");
                            PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "START");
                            startOrientationScanner();
                        }
                        else
                            PPApplication.logE("[RJS] PhoneProfilesService.startOrientationScanner", "started");
                    } else
                        startOrientationScanner(false, true, false);
                }
                else
                    startOrientationScanner(false, true, false);
            }
            else
                startOrientationScanner(false, true, false);
        }
    }

    private void registerReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.registerReceiversAndJobs", "xxx");

        // --- receivers and content observers for events -- register it only if any event exists

        Context appContext = getApplicationContext();

        WifiScanJob.initialize(appContext);
        BluetoothScanJob.initialize(appContext);

        // get actual battery status
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            BatteryBroadcastReceiver.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            BatteryBroadcastReceiver.batteryPct = Math.round(level / (float) scale * 100);
        }

        registerAllTheTimeRequiredReceivers(true, true);

        // required for battery event
        registerBatteryEventReceiver(true, true, true);
        registerBatteryChangedReceiver(true, true, true);

        // required for peripherals event
        registerReceiverForPeripheralsSensor(true, true, true);

        // required for sms event
        registerReceiverForSMSSensor(true, true, true);

        // required for calendar event
        registerReceiverForCalendarSensor(true, true, true);

        // required for radio switch event
        registerReceiverForRadioSwitchMobileDataSensor(true, true, true);
        registerReceiverForRadioSwitchNFCSensor(true, true, true);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, true, true);

        // required for location and radio switch event
        registerLocationModeChangedBroadcastReceiver(true, true, true);

        // required for bluetooth connection type = (dis)connected +
        // radio switch event +
        // bluetooth scanner
        registerBluetoothStateChangedBroadcastReceiver(true, true, true, false);

        // required for bluetooth connection type = (dis)connected +
        // bluetooth scanner
        registerBluetoothConnectionBroadcastReceiver(true, true, true, false);

        // required for bluetooth scanner
        registerBluetoothScannerReceivers(true, true, true, false);

        // required for wifi scanner
        registerWifiAPStateChangeBroadcastReceiver(true, true, true, false);

        // required for all scanner events (wifi, bluetooth, location, mobile cells, device orientation) +
        // battery event
        registerPowerSaveModeReceiver(true, true, true);

        // required for Connect to SSIS profile preference +
        // wifi connection type = (dis)connected +
        // radio switch event +
        // wifi scanner
        registerWifiStateChangedBroadcastReceiver(true, true, true, false);

        // required for Connect to SSIS profile preference +
        // required for wifi connection type = (dis)connected event +
        // wifi scanner
        registerWifiConnectionBroadcastReceiver(true, true, true, false);

        // required for wifi scanner
        registerWifiScannerReceiver(true, true, true, false);

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
        // Thirdparty Alarms
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */

        scheduleWifiJob(true, true, true, false, false, false, false);
        scheduleBluetoothJob(true, true, true, false, false, false);
        scheduleGeofenceScannerJob(true, true, true, false, false);
        scheduleSearchCalendarEventsJob(true, true, true);

        startGeofenceScanner(true, true, true, false, false);
        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
    }

    private void unregisterReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.unregisterReceiversAndJobs", "xxx");
        registerAllTheTimeRequiredReceivers(false, true);
        registerBatteryEventReceiver(false, true, false);
        registerBatteryChangedReceiver(false, true, false);
        registerReceiverForPeripheralsSensor(false, true, false);
        registerReceiverForSMSSensor(false, true, false);
        registerReceiverForCalendarSensor(false, true, false);
        registerReceiverForRadioSwitchMobileDataSensor(false, true, false);
        registerReceiverForRadioSwitchNFCSensor(false, true, false);
        registerReceiverForRadioSwitchAirplaneModeSensor(false, true, false);
        registerLocationModeChangedBroadcastReceiver(false, true, false);
        registerBluetoothStateChangedBroadcastReceiver(false, true, false, false);
        registerBluetoothConnectionBroadcastReceiver(false, true, false, false);
        registerBluetoothScannerReceivers(false, true, false, false);
        registerWifiAPStateChangeBroadcastReceiver(false, true, false, false);
        registerPowerSaveModeReceiver(false, true, false);
        registerWifiStateChangedBroadcastReceiver(false, true, false, false);
        registerWifiConnectionBroadcastReceiver(false, true, false, false);
        registerWifiScannerReceiver(false, true, false, false);

        //if (alarmClockBroadcastReceiver != null)
        //    appContext.unregisterReceiver(alarmClockBroadcastReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        scheduleWifiJob(false, true, false, false, false, false, false);
        scheduleBluetoothJob(false, true, false, false, false, false);
        scheduleGeofenceScannerJob(false, true, false, false, false);
        scheduleSearchCalendarEventsJob(false, true, false);

        startGeofenceScanner(false, true, false, false, false);
        startPhoneStateScanner(false, true, false, false, false);
        startOrientationScanner(false, true, false);
    }

    private void reregisterReceiversAndJobs() {
        PPApplication.logE("[RJS] PhoneProfilesService.reregisterReceiversAndJobs", "xxx");
        registerBatteryEventReceiver(true, true, true);
        registerBatteryChangedReceiver(true, true, true);
        registerReceiverForPeripheralsSensor(true, true, true);
        registerReceiverForSMSSensor(true, true, true);
        registerReceiverForCalendarSensor(true, true, true);
        registerReceiverForRadioSwitchMobileDataSensor(true, true, true);
        registerReceiverForRadioSwitchNFCSensor(true, true, true);
        registerReceiverForRadioSwitchAirplaneModeSensor(true, true, true);
        registerLocationModeChangedBroadcastReceiver(true, true, true);
        registerBluetoothStateChangedBroadcastReceiver(true, true, true, false);
        registerBluetoothConnectionBroadcastReceiver(true, true, true, false);
        registerBluetoothScannerReceivers(true, true, true, false);
        registerWifiAPStateChangeBroadcastReceiver(true, true, true, false);
        registerPowerSaveModeReceiver(true, true, true);
        registerWifiStateChangedBroadcastReceiver(true, true, true, false);
        registerWifiConnectionBroadcastReceiver(true, true, true, false);
        registerWifiScannerReceiver(true, true, true, false);

        scheduleWifiJob(true, true, true, false, false, false, false);
        scheduleBluetoothJob(true, true, true, false, false, false);
        scheduleGeofenceScannerJob(true, true, true, false, false);
        scheduleSearchCalendarEventsJob(true, false, true);

        startGeofenceScanner(true, true, true, false, false);
        startPhoneStateScanner(true, true, true, false, false);
        startOrientationScanner(true, true, true);
    }

    // start service for first start
    private boolean doForFirstStart(Intent intent/*, int flags, int startId*/) {
        boolean onlyStart = true;
        boolean startOnBoot = false;

        if (intent != null) {
            onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            startOnBoot = intent.getBooleanExtra(EXTRA_START_ON_BOOT, false);
        }

        if (onlyStart)
            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_ONLY_START");
        if (startOnBoot)
            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_START_ON_BOOT");

        Context appContext = getApplicationContext();

        // set service foreground
        final DataWrapper dataWrapper =  new DataWrapper(this, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
        Profile activatedProfile = null;
        if (onlyStart && startOnBoot) {
            if (ApplicationPreferences.applicationActivate(this) &&
                    ApplicationPreferences.applicationStartEvents(this)) {
                activatedProfile = dataWrapper.getActivatedProfile();
            }
            else
            if (ApplicationPreferences.applicationActivate(this))
                activatedProfile = dataWrapper.getActivatedProfile();
        }
        else
            activatedProfile = dataWrapper.getActivatedProfile();
        showProfileNotification(activatedProfile, dataWrapper);

        if (onlyStart) {
            // start FirstStartJob
            FirstStartJob.start(startOnBoot);

            ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, false);
        }

        return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        serviceRunning = true;

        if (!doForFirstStart(intent/*, flags, startId*/)) {
            if (intent != null) {
                if (intent.getBooleanExtra(EXTRA_SET_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SET_SERVICE_FOREGROUND");
                    final DataWrapper dataWrapper =  new DataWrapper(this, true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
                    Profile activatedProfile = dataWrapper.getActivatedProfile();
                    showProfileNotification(activatedProfile, dataWrapper);
                }

                if (intent.getBooleanExtra(EXTRA_CLEAR_SERVICE_FOREGROUND, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_CLEAR_SERVICE_FOREGROUND");
                    removeProfileNotification(this);
                }

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
                    isScreenOn = pm.isScreenOn();
                    //}

                    boolean secureKeyguard;
                    if (keyguardManager == null)
                        keyguardManager = (KeyguardManager)appContext.getSystemService(Activity.KEYGUARD_SERVICE);
                    secureKeyguard = keyguardManager.isKeyguardSecure();
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand","secureKeyguard="+secureKeyguard);
                    if (!secureKeyguard)
                    {
                        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand xxx","getLockScreenDisabled="+ ActivateProfileHelper.getLockScreenDisabled(appContext));

                        if (isScreenOn) {
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "screen on");

                            if (ActivateProfileHelper.getLockScreenDisabled(appContext)) {
                                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "Keyguard.disable(), START_STICKY");
                                reenableKeyguard();
                                disableKeyguard();
                            } else {
                                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                                reenableKeyguard();
                            }
                        }
                    }
                }

                if (intent.getBooleanExtra(EXTRA_REGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_REGISTER_RECEIVERS_AND_JOBS");
                    registerReceiversAndJobs();
                }
                if (intent.getBooleanExtra(EXTRA_UNREGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_UNREGISTER_RECEIVERS_AND_JOBS");
                    unregisterReceiversAndJobs();
                }
                if (intent.getBooleanExtra(EXTRA_REREGISTER_RECEIVERS_AND_JOBS, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_REREGISTER_RECEIVERS_AND_JOBS");
                    reregisterReceiversAndJobs();
                }

                if (intent.getBooleanExtra(EXTRA_SIMULATE_RINGING_CALL, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SIMULATE_RINGING_CALL");
                    doSimulatingRingingCall(intent);
                }
                //if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                //    doSimulatingNotificationTone(intent);

                if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_START_STOP_SCANNER");
                    boolean forScreenOn = intent.getBooleanExtra(EXTRA_FOR_SCREEN_ON, false);
                    switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                        case PPApplication.SCANNER_START_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_GEOFENCE_SCANNER");
                            startGeofenceScanner(true, true, true, false, false);
                            break;
                        case PPApplication.SCANNER_STOP_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_GEOFENCE_SCANNER");
                            startGeofenceScanner(false, true, false, false, false);
                            break;
                        case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_ORIENTATION_SCANNER");
                            startOrientationScanner(true, true, true);
                            break;
                        case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                            startOrientationScanner(false, true, false);
                            break;
                        case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                            startPhoneStateScanner(true, true, true, false, false);
                            break;
                        case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                            startPhoneStateScanner(false, true, false, false, false);
                            break;
                        case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            registerWifiConnectionBroadcastReceiver(true, false, true, false);
                            registerWifiStateChangedBroadcastReceiver(true, false, true, false);
                            registerWifiAPStateChangeBroadcastReceiver(true, false, true, false);
                            registerWifiScannerReceiver(true, false, true, false);
                            break;
                        case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER");
                            registerWifiConnectionBroadcastReceiver(true, false, false, true);
                            registerWifiStateChangedBroadcastReceiver(true, false, false, true);
                            registerWifiAPStateChangeBroadcastReceiver(true, false, false, true);
                            registerWifiScannerReceiver(true, false, false, true);
                            break;
                        case PPApplication.SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                            registerBluetoothStateChangedBroadcastReceiver(true, false, true, false);
                            registerBluetoothScannerReceivers(true, false, true, false);
                            break;
                        case PPApplication.SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER");
                            registerBluetoothConnectionBroadcastReceiver(true, false, false, true);
                            registerBluetoothStateChangedBroadcastReceiver(true, false, false, true);
                            registerBluetoothScannerReceivers(true, false, false, true);
                            break;
                        case PPApplication.SCANNER_RESTART_WIFI_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_WIFI_SCANNER");
                            registerWifiConnectionBroadcastReceiver(true, false, true, false);
                            registerWifiStateChangedBroadcastReceiver(true, false, true, false);
                            registerWifiAPStateChangeBroadcastReceiver(true, false, true, false);
                            registerWifiScannerReceiver(true, false, true, false);
                            scheduleWifiJob(true, true, true, forScreenOn, false, false, true);
                            break;
                        case PPApplication.SCANNER_RESTART_BLUETOOTH_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_BLUETOOTH_SCANNER");
                            registerBluetoothConnectionBroadcastReceiver(true, false, true, false);
                            registerBluetoothStateChangedBroadcastReceiver(true, false, true, false);
                            registerBluetoothScannerReceivers(true, false, true, false);
                            scheduleBluetoothJob(true, true, true, forScreenOn, false, true);
                            break;
                        case PPApplication.SCANNER_RESTART_PHONE_STATE_SCANNER:
                             PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_PHONE_STATE_SCANNER");
                             startPhoneStateScanner(true, true, true, false, true);
                            break;
                        case PPApplication.SCANNER_FORCE_START_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_FORCE_START_PHONE_STATE_SCANNER");
                            startPhoneStateScanner(true, false, false, true, false);
                            break;
                        case PPApplication.SCANNER_RESTART_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_GEOFENCE_SCANNER");
                            registerLocationModeChangedBroadcastReceiver(true, false, true);
                            startGeofenceScanner(true, true, true, forScreenOn, true);
                            break;
                        case PPApplication.SCANNER_RESTART_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_RESTART_ORIENTATION_SCANNER");
                            startOrientationScanner(true, false, true);
                            break;
                    }
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

    //------------------------

    // profile notification -------------------

    @SuppressLint("NewApi")
    void showProfileNotification(Profile profile, DataWrapper dataWrapper)
    {
        if (ActivateProfileHelper.lockRefresh)
            // no refresh notification
            return;

        if (serviceRunning && ApplicationPreferences.notificationStatusBar(dataWrapper.context))
        {
            PPApplication.logE("ActivateProfileHelper.showNotification", "show");

            boolean notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar(dataWrapper.context);
            boolean notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context);

            // close showed notification
            //notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);

            // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
            Intent intent = new Intent(dataWrapper.context, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // nastavime, ze aktivita sa spusti z notifikacnej listy
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(dataWrapper.context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie intentu na restart events
            /*Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
            intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);*/
            Intent intentRE = new Intent(dataWrapper.context, RestartEventsFromNotificationBroadcastReceiver.class);
            PendingIntent pIntentRE = PendingIntent.getBroadcast(dataWrapper.context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);


            // vytvorenie samotnej notifikacie

            Notification.Builder notificationBuilder;

            RemoteViews contentView;
            if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("1"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_dark);
            else
            if (ApplicationPreferences.notificationTheme(dataWrapper.context).equals("2"))
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer_light);
            else
                contentView = new RemoteViews(dataWrapper.context.getPackageName(), R.layout.notification_drawer);

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                profileName = dataWrapper.context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(dataWrapper.context)
                    .setContentIntent(pIntent);

            //TODO Android O
            /*if (Build.VERSION.SDK_INT >= 26) {
                // The id of the channel.
                String channelId = "phoneProfiles_profile_activated";
                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_activated_profile);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_activated_profile_ppp);

                // no sound
                int importance = NotificationManager.IMPORTANCE_LOW;
                if (notificationShowInStatusBar) {
                    KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    if ((ApplicationPreferences.notificationHideInLockScreen(context) && (!screenUnlocked)) ||
                            ((profile != null) && profile._hideStatusBarIcon))
                        importance = NotificationManager.IMPORTANCE_MIN;
                }
                else
                    importance = NotificationManager.IMPORTANCE_MIN;

                NotificationChannel channel = new NotificationChannel(channelId, name, importance);

                // Configure the notification channel.
                channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(false);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(Color.RED);
                channel.enableVibration(false);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                notificationManager.createNotificationChannel(channel);

                notificationBuilder.setChannelId(channelId);
            }
            else {*/
            if (notificationShowInStatusBar) {
                KeyguardManager myKM = (KeyguardManager) dataWrapper.context.getSystemService(Context.KEYGUARD_SERVICE);
                //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                boolean screenUnlocked = !myKM.isKeyguardLocked();
                //boolean screenUnlocked = getScreenUnlocked(context);
                if ((ApplicationPreferences.notificationHideInLockScreen(dataWrapper.context) && (!screenUnlocked)) ||
                        ((profile != null) && profile._hideStatusBarIcon))
                    notificationBuilder.setPriority(Notification.PRIORITY_MIN);
                else
                    notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);
            }
            else
                notificationBuilder.setPriority(Notification.PRIORITY_MIN);
            //}
            if (Build.VERSION.SDK_INT >= 21)
            {
                notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationBuilder.setTicker(profileName);

            if (isIconResourceID)
            {
                int iconSmallResource;
                if (iconBitmap != null) {
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0")) {
                        // colorful icon

                        // FC in Note 4, 6.0.1 :-/
                        String manufacturer = PPApplication.getROMManufacturer();
                        boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                                          /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                                           Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                                          ) &&*/
                                (android.os.Build.VERSION.SDK_INT == 23);
                        //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                        if ((android.os.Build.VERSION.SDK_INT >= 23) && (!isNote4)) {
                            notificationBuilder.setSmallIcon(ColorNotificationIcon.getFromBitmap(iconBitmap));
                        }
                        else {
                            iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                            if (iconSmallResource == 0)
                                iconSmallResource = R.drawable.ic_profile_default;
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }
                    }
                    else {
                        // native icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }
                else {
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0")) {
                        // colorful icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(dataWrapper.context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    } else {
                        // native icon
                        iconSmallResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", dataWrapper.context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = dataWrapper.context.getResources().getIdentifier(iconIdentifier, "drawable", dataWrapper.context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(dataWrapper.context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    }
                }
            }
            else {
                // FC in Note 4, 6.0.1 :-/
                String manufacturer = PPApplication.getROMManufacturer();
                boolean isNote4 = (manufacturer != null) && (manufacturer.compareTo("samsung") == 0) &&
                        /*(Build.MODEL.startsWith("SM-N910") ||  // Samsung Note 4
                         Build.MODEL.startsWith("SM-G900")     // Samsung Galaxy S5
                        ) &&*/
                        (android.os.Build.VERSION.SDK_INT == 23);
                //Log.d("ActivateProfileHelper.showNotification","isNote4="+isNote4);
                if ((Build.VERSION.SDK_INT >= 23) && (!isNote4) && (iconBitmap != null)) {
                    notificationBuilder.setSmallIcon(ColorNotificationIcon.getFromBitmap(iconBitmap));
                }
                else {
                    int iconSmallResource;
                    if (ApplicationPreferences.notificationStatusBarStyle(dataWrapper.context).equals("0"))
                        iconSmallResource = R.drawable.ic_profile_default;
                    else
                        iconSmallResource = R.drawable.ic_profile_default_notify;
                    notificationBuilder.setSmallIcon(iconSmallResource);
                }

                if (iconBitmap != null)
                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                else
                    contentView.setImageViewResource(R.id.notification_activated_profile_icon, R.drawable.ic_profile_default);
            }

            // workaround for LG G4, Android 6.0
            if (Build.VERSION.SDK_INT < 24)
                contentView.setInt(R.id.notification_activated_app_root, "setVisibility", View.GONE);

            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("1")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("2")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.WHITE);
            }
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
            //		ProfilePreferencesIndicator.paint(profile, context));
            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(dataWrapper.context)))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("1"))
                contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, R.drawable.ic_action_events_restart);
            else
            if (ApplicationPreferences.notificationTextColor(dataWrapper.context).equals("2"))
                contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, R.drawable.ic_action_events_restart_dark);
            contentView.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                //notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
                notificationBuilder.setCustomContentView(contentView);
            }
            else
                //noinspection deprecation
                notificationBuilder.setContent(contentView);

            Notification phoneProfilesNotification;
            try {
                phoneProfilesNotification = notificationBuilder.build();
            } catch (Exception e) {
                phoneProfilesNotification = null;
            }

            if (phoneProfilesNotification != null) {
                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26) {
                if (notificationStatusBarPermanent) {
                    //notification.flags |= Notification.FLAG_NO_CLEAR;
                    phoneProfilesNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                } else {
                    setAlarmForNotificationCancel(dataWrapper.context);
                }
                //}

                if (notificationStatusBarPermanent)
                    startForeground(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                else {
                    NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, phoneProfilesNotification);
                }
            }
        }
        else
        {
            if (ApplicationPreferences.notificationStatusBarPermanent(dataWrapper.context))
                stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }
    }

    private void removeProfileNotification(Context context)
    {
        if (ApplicationPreferences.notificationStatusBarPermanent(context))
            stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }
    }

    private void setAlarmForNotificationCancel(Context context)
    {
        if (ApplicationPreferences.notificationStatusBarCancel(context).isEmpty() || ApplicationPreferences.notificationStatusBarCancel(context).equals("0"))
            return;

        int notificationStatusBarCancel = Integer.valueOf(ApplicationPreferences.notificationStatusBarCancel(context));

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + notificationStatusBarCancel * 1000;
        // not needed exact for removing notification
        /*if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time, pendingIntent);
        if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            alarmManager.setExact(AlarmManager.RTC, time, pendingIntent);
        else*/
        alarmManager.set(AlarmManager.RTC, time, pendingIntent);
    }


    //--------------------------

    // switch keyguard ------------------------------------

    private void disableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reenableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.reenableKeyguard();
    }

    //--------------------------------------

    // Location ----------------------------------------------------------------

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException ignored) {
            }
            return  locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else {
            //noinspection deprecation
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return  !TextUtils.isEmpty(locationProviders);
        }
    }

    private void startGeofenceScanner() {

        if (geofencesScanner != null) {
            geofencesScanner.disconnect();
            geofencesScanner = null;
        }

        geofencesScanner = new GeofencesScanner(getApplicationContext());
        geofencesScanner.connect();
    }

    private void stopGeofenceScanner() {
        if (geofencesScanner != null) {
            geofencesScanner.disconnect();
            geofencesScanner = null;
        }
    }

    static boolean isGeofenceScannerStarted() {
        return (geofencesScanner != null);
    }

    static GeofencesScanner getGeofencesScanner() {
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

    public static boolean isPhoneStateScannerStarted() {
        return (phoneStateScanner != null);
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
        return mStartedOrientationSensors;
    }

    public static Sensor getAccelerometerSensor(Context context) {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    public static Sensor getMagneticFieldSensor(Context context) {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    public static Sensor getProximitySensor(Context context) {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }
    /*@SuppressWarnings("deprecation")
    public static Sensor getOrientationSensor(Context context) {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }*/

    @SuppressLint("NewApi")
    private void startListeningOrientationSensors() {
        if (mOrientationSensorManager == null)
            mOrientationSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (!mStartedOrientationSensors) {

            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(this);
            if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(this).equals("2"))
                // start scanning in power save mode is not allowed
                return;

            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) == 0)
                return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval(this);
            if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(this).equals("1"))
                interval *= 2;
            Sensor accelerometer = getAccelerometerSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","accelerometer="+accelerometer);
            if (accelerometer != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (accelerometer.getFifoMaxEventCount() > 0))
                    mOrientationSensorManager.registerListener(this, accelerometer, 200000 * interval, 1000000 * interval);
                else
                    mOrientationSensorManager.registerListener(this, accelerometer, 1000000 * interval);
            }
            Sensor magneticField = getMagneticFieldSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","magneticField="+magneticField);
            if (magneticField != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (magneticField.getFifoMaxEventCount() > 0))
                    mOrientationSensorManager.registerListener(this, magneticField, 200000 * interval, 1000000 * interval);
                else
                    mOrientationSensorManager.registerListener(this, magneticField, 1000000 * interval);
            }
            Sensor proximity = getProximitySensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","proximity="+proximity);
            if (proximity != null) {
                mMaxProximityDistance = proximity.getMaximumRange();
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (proximity.getFifoMaxEventCount() > 0))
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

        //if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        //    return;

        if (sensorType == Sensor.TYPE_PROXIMITY) {
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity value="+event.values[0]);
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
                    DeviceOrientationJob.start();
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

                                        DeviceOrientationJob.start();
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

                                DeviceOrientationJob.start();
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

            int oldRingerMode = intent.getIntExtra(EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EXTRA_OLD_ZEN_MODE, 0);
            String oldRingtone = intent.getStringExtra(EXTRA_OLD_RINGTONE);
            int oldSystemRingerVolume = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_VOLUME, -1);
            int newRingerMode = ActivateProfileHelper.getRingerMode(context);
            int newZenMode = ActivateProfileHelper.getZenMode(context);
            int newRingerVolume = ActivateProfileHelper.getRingerVolume(context);
            String newRingtone = "";
            String phoneNumber = ApplicationPreferences.preferences.getString(PhoneCallJob.PREF_EVENT_CALL_PHONE_NUMBER, "");

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
                Permissions.grantPlayRingtoneNotificationPermissions(context, true, true);
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
                    Permissions.grantPlayRingtoneNotificationPermissions(context, true, true);
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
                    if (!(((newRingerMode == 4) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                            ((newRingerMode == 5) && ((newZenMode == 3) || (newZenMode == 6))))) {
                        // new ringer/zen mode is changed to another then NONE and ONLY_ALARMS
                        // Android 6 - ringerMode=4 = ONLY_ALARMS

                        // test old ringer and zen mode
                        if (((oldRingerMode == 4) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                                ((oldRingerMode == 5) && ((oldZenMode == 3) || (oldZenMode == 6)))) {
                            // old ringer/zen mode is NONE and ONLY_ALARMS
                            simulateRinging = true;
                            stream = AudioManager.STREAM_MUSIC;
                            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (1)");
                        }
                    }

                    if (!simulateRinging) {
                        if (!(((newRingerMode == 4) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                                ((newRingerMode == 5) && (newZenMode == 2)))) {
                            // new ringer/zen mode is changed to another then PRIORITY
                            // Android 5 - ringerMode=4 = PRIORITY
                            if (((oldRingerMode == 4) && (android.os.Build.VERSION.SDK_INT < 23)) ||
                                    ((oldRingerMode == 5) && (oldZenMode == 2))) {
                                // old ringer/zen mode is PRIORITY
                                simulateRinging = true;
                                if (oldSystemRingerMode == AudioManager.RINGER_MODE_SILENT) {
                                    stream = AudioManager.STREAM_MUSIC;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (2)");
                                }
                                else {
                                    //stream = AudioManager.STREAM_RING;
                                    stream = AudioManager.STREAM_MUSIC;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=RING (2)");
                                }
                            }
                        }
                    }

                    /*
                    if (!simulateRinging) {
                        if (!((newRingerMode == 4) ||
                                ((newRingerMode == 5) && ((newZenMode == 3) || (newZenMode == 6))))) {
                            // new ringer/zen mode is changed to another then PRIORITY, NONE and ONLY_ALARMS
                            // Android 5 - ringerMode=4 = PRIORITY
                            // Android 6 - ringerMode=4 = ONLY_ALARMS

                            // test old ringer and zen mode
                            if (!((oldRingerMode == 4) ||
                                    ((oldRingerMode == 5) && ((oldZenMode == 3) || (oldZenMode == 6))))) {
                                // old ringer/zen mode is not PRIORITY, NONE and ONLY_ALARMS

                                if ((oldSystemRingerVolume == 0) && (newRingerVolume > 0)) {
                                    simulateRinging = true;
                                    stream = AudioManager.STREAM_MUSIC;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "stream=MUSIC (3)");
                                }
                            }
                        }
                    }
                    */
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
        stopSimulatingRingingCall(true);
        if (!ringingCallIsSimulating) {
            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "stream="+stream);
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            //stopSimulatingNotificationTone(true);

            if ((eventNotificationMediaPlayer != null) && eventNotificationIsPlayed) {
                try {
                    if (eventNotificationMediaPlayer.isPlaying())
                        eventNotificationMediaPlayer.stop();
                } catch (Exception ignored) {}
                eventNotificationMediaPlayer.release();
                eventNotificationIsPlayed = false;
                eventNotificationMediaPlayer = null;
            }
            if (eventNotificationPlayTimer != null) {
                eventNotificationPlayTimer.cancel();
                eventNotificationPlayTimer = null;
            }

            if ((ringtone != null) && !ringtone.isEmpty()) {
                RingerModeChangeReceiver.internalChange = true;

                usedRingingStream = stream;
                // play repeating: default ringtone with ringing volume level
                try {
                    AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_NORMAL);

                    //int requestType = AudioManager.AUDIOFOCUS_GAIN;
                    int requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                    if (android.os.Build.VERSION.SDK_INT >= 19)
                        requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                    int result = audioManager.requestAudioFocus(this, usedRingingStream, requestType);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        ringingMediaPlayer = new MediaPlayer();
                        ringingMediaPlayer.setAudioStreamType(usedRingingStream);
                        ringingMediaPlayer.setDataSource(this, Uri.parse(ringtone));
                        ringingMediaPlayer.prepare();
                        ringingMediaPlayer.setLooping(true);

                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringingVolume=" + ringingVolume);

                        int maximumRingValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) ringingVolume / maximumRingValue * 100.0f;
                        mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "mediaRingingVolume=" + mediaRingingVolume);

                        /*if (android.os.Build.VERSION.SDK_INT >= 23)
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                        else
                            audioManager.setStreamMute(AudioManager.STREAM_RING, true);*/
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaRingingVolume, 0);

                        ringingMediaPlayer.start();

                        ringingCallIsSimulating = true;
                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "ringing played");
                    } else
                        PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "focus not granted");
                } catch (SecurityException e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", " security exception");
                    Permissions.grantPlayRingtoneNotificationPermissions(this, true, true);
                    ringingMediaPlayer = null;
                    final Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                } catch (Exception e) {
                    PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "exception");
                    ringingMediaPlayer = null;
                    final Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.startSimulatingRingingCall", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                }
            }
        }
    }

    public void stopSimulatingRingingCall(boolean abandonFocus) {
        //if (ringingCallIsSimulating) {
            PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "xxx");
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            if (ringingMediaPlayer != null) {
                try {
                    if (ringingMediaPlayer.isPlaying())
                        ringingMediaPlayer.stop();
                } catch (Exception ignored) {}
                ringingMediaPlayer.release();
                ringingMediaPlayer = null;

                if (ringingCallIsSimulating)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "ringing stopped");
            }
            if (abandonFocus)
                audioManager.abandonAudioFocus(this);
        //}
        ringingCallIsSimulating = false;
        final Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "disable ringer mode change internal change");
                RingerModeChangeReceiver.internalChange = false;
            }
        }, 3000);
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
                            stream = AudioManager.STREAM_MUSIC;
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
                                    stream = AudioManager.STREAM_MUSIC;
                                    PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "stream=MUSIC");
                                }
                                else {
                                    stream = AudioManager.STREAM_NOTIFICATION;
                                    //stream = AudioManager.STREAM_MUSIC;
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
                    int result = audioManager.requestAudioFocus(this, usedNotificationStream, requestType);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        notificationMediaPlayer = new MediaPlayer();
                        notificationMediaPlayer.setAudioStreamType(usedNotificationStream);
                        notificationMediaPlayer.setDataSource(this, Uri.parse(notificationTone));
                        notificationMediaPlayer.prepare();
                        notificationMediaPlayer.setLooping(false);


                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "notificationVolume=" + notificationVolume);

                        int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                        mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "mediaNotificationVolume=" + mediaNotificationVolume);

                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaNotificationVolume, 0);

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

                    } else
                        PPApplication.logE("PhoneProfilesService.startSimulatingNotificationTone", "focus not granted");
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

    public void stopSimulatingNotificationTone(boolean abadonFocus) {
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

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
            PPApplication.logE("PhoneProfilesService.stopSimulatingNotificationTone", "notification stopped");
        }
        if (abadonFocus)
            audioManager.abandonAudioFocus(this);
        //}
        notificationToneIsSimulating = false;
        RingerModeChangeReceiver.setAlarmForDisableInternalChange(this);
    }*/

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (audioManager == null )
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS_TRANSIENT");
            /*if ((ringingMediaPlayer != null) && ringingCallIsSimulating)
                if (ringingMediaPlayer.isPlaying())
                    ringingMediaPlayer.pause();
            if ((notificationMediaPlayer != null) && notificationToneIsSimulating)
                if (notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.pause();*/
            stopSimulatingRingingCall(false);
            //stopSimulatingNotificationTone(false);
            audioManager.abandonAudioFocus(this);
        }
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Lower the volume
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            }
            /*if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
                if (usedNotificationStream == AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            }*/
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_GAIN");
            if ((ringingMediaPlayer != null) && ringingCallIsSimulating) {
                if (usedRingingStream == AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaRingingVolume, 0);
                if (!ringingMediaPlayer.isPlaying())
                    ringingMediaPlayer.start();
            }
            /*if ((notificationMediaPlayer != null) && notificationToneIsSimulating) {
                if (usedNotificationStream == AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaNotificationVolume, 0);
                if (!notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.start();
            }*/
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop playback
            PPApplication.logE("PhoneProfilesService.onAudioFocusChange","AUDIOFOCUS_LOSS");
            stopSimulatingRingingCall(false);
            //stopSimulatingNotificationTone(false);
            audioManager.abandonAudioFocus(this);
        }
    }

    //---------------------------

    public void playEventNotificationSound (final String eventNotificationSound, final boolean eventNotificationVibrate) {
        PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "eventNotificationVibrate=" + eventNotificationVibrate);

        if (eventNotificationVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()) {
                PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "vibration");
                //TODO Android O
                //if (Build.VERSION.SDK_INT >= 26) {
                //    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                //} else {
                    vibrator.vibrate(500);
                //}
            }
        }

        if ((!ringingCallIsSimulating)/* && (!notificationToneIsSimulating)*/) {

            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            if ((eventNotificationMediaPlayer != null) && eventNotificationIsPlayed) {
                try {
                    if (eventNotificationMediaPlayer.isPlaying())
                        eventNotificationMediaPlayer.stop();
                } catch (Exception ignored) {}
                eventNotificationMediaPlayer.release();
                eventNotificationIsPlayed = false;
                eventNotificationMediaPlayer = null;
            }
            if (eventNotificationPlayTimer != null) {
                eventNotificationPlayTimer.cancel();
                eventNotificationPlayTimer = null;
            }

            if (!eventNotificationSound.isEmpty())
            {
                Uri notificationUri = Uri.parse(eventNotificationSound);

                try {
                    RingerModeChangeReceiver.internalChange = true;

                    eventNotificationMediaPlayer = new MediaPlayer();
                    eventNotificationMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    eventNotificationMediaPlayer.setDataSource(this, notificationUri);
                    eventNotificationMediaPlayer.prepare();
                    eventNotificationMediaPlayer.setLooping(false);

                    /*
                    oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                    int notificationVolume = ActivateProfileHelper.getNotificationVolume(this);

                    PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "notificationVolume=" + notificationVolume);

                    int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                    int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                    int mediaEventNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                    PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "mediaEventNotificationVolume=" + mediaEventNotificationVolume);

                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaEventNotificationVolume, 0);
                    */

                    eventNotificationMediaPlayer.start();

                    eventNotificationIsPlayed = true;

                    //final Context context = this;
                    eventNotificationPlayTimer = new Timer();
                    eventNotificationPlayTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            if (eventNotificationMediaPlayer != null) {
                                try {
                                    if (eventNotificationMediaPlayer.isPlaying())
                                        eventNotificationMediaPlayer.stop();
                                } catch (Exception ignored) {}
                                eventNotificationMediaPlayer.release();

                                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
                                PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "event notification stopped");
                            }

                            eventNotificationIsPlayed = false;
                            eventNotificationMediaPlayer = null;
                            eventNotificationPlayTimer = null;

                            final Handler handler = new Handler(getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "disable ringer mode change internal change");
                                    RingerModeChangeReceiver.internalChange = false;
                                }
                            }, 3000);

                        }
                    }, eventNotificationMediaPlayer.getDuration());

                } catch (SecurityException e) {
                    PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "security exception");
                    Permissions.grantPlayRingtoneNotificationPermissions(this, true, false);
                    eventNotificationMediaPlayer = null;
                    eventNotificationIsPlayed = false;
                    final Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                } catch (Exception e) {
                    PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "exception");
                    //e.printStackTrace();
                    eventNotificationMediaPlayer = null;
                    eventNotificationIsPlayed = false;
                    final Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PhoneProfilesService.playEventNotificationSound", "disable ringer mode change internal change");
                            RingerModeChangeReceiver.internalChange = false;
                        }
                    }, 3000);
                }

            }
        }
    }

    //---------------------------

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onTaskRemoved", "xxxxx");

        if (PPApplication.screenTimeoutHandler != null) {
            PPApplication.screenTimeoutHandler.post(new Runnable() {
                public void run() {
                    ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
                }
            });
        }// else
        //    ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());

        super.onTaskRemoved(rootIntent);
    }

}
