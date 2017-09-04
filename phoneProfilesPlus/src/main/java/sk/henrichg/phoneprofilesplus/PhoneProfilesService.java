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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.commonsware.cwac.wakeful.WakefulIntentService;
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

    private KeyguardManager keyguardManager;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock;

    private ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    private InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;
    private PhoneCallBroadcastReceiver phoneCallBroadcastReceiver = null;
    private RingerModeChangeReceiver ringerModeChangeReceiver = null;
    private WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;

    private BatteryBroadcastReceiver batteryEventReceiver = null;
    private HeadsetConnectionBroadcastReceiver headsetPlugReceiver = null;
    private NFCStateChangedBroadcastReceiver nfcStateChangedBroadcastReceiver = null;
    private DockConnectionBroadcastReceiver dockConnectionBroadcastReceiver = null;
    private WifiConnectionBroadcastReceiver wifiConnectionBroadcastReceiver = null;
    private BluetoothConnectionBroadcastReceiver bluetoothConnectionBroadcastReceiver = null;
    private BluetoothStateChangedBroadcastReceiver bluetoothStateChangedBroadcastReceiver = null;
    private AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    private WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    private LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    private AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    private SMSBroadcastReceiver smsBroadcastReceiver = null;
    private SMSBroadcastReceiver mmsBroadcastReceiver = null;
    private CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;

    private PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    private DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;
    private static MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;

    static final String EXTRA_START_STOP_SCANNER = "start_stop_scanner";
    static final String EXTRA_START_STOP_SCANNER_TYPE = "start_stop_scanner_type";
    static final String EXTRA_START_ON_BOOT = "start_on_boot";
    static final String EXTRA_ONLY_START = "only_start";
    static final String EXTRA_SET_SERVICE_FOREGROUND = "set_service_foreground";
    static final String EXTRA_CLEAR_SERVICE_FOREGROUND = "clear_service_foreground";
    static final String EXTRA_SWITCH_KEYGUARD = "switch_keyguard";

    //-----------------------

    public static GeofencesScanner geofencesScanner = null;

    private static SensorManager mSensorManager = null;
    private static boolean mStartedSensors = false;

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
    Timer eventNotificationPlayTimer = null;

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

        // --- receivers and content observers for profiles/events -- must be registered permanently

        // required for Lock device, Hide notification in lock screen, screen timeout +
        // screen on/off event + rescan wifi, bluetooth, location, mobile cells
        if (screenOnOffReceiver != null)
            appContext.unregisterReceiver(screenOnOffReceiver);
        screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        appContext.registerReceiver(screenOnOffReceiver, intentFilter5);

        // required for Do not disturb ringer mode
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, appContext)) {
                if (interruptionFilterChangedReceiver != null)
                    appContext.unregisterReceiver(interruptionFilterChangedReceiver);
                interruptionFilterChangedReceiver = new InterruptionFilterChangedBroadcastReceiver();
                IntentFilter intentFilter11 = new IntentFilter();
                intentFilter11.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                appContext.registerReceiver(interruptionFilterChangedReceiver, intentFilter11);
            }
        }

        // required for unlink ring and notification volume + call event
        if (phoneCallBroadcastReceiver != null)
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
        phoneCallBroadcastReceiver = new PhoneCallBroadcastReceiver();
        IntentFilter intentFilter6 = new IntentFilter();
        intentFilter6.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter6.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        appContext.registerReceiver(phoneCallBroadcastReceiver, intentFilter6);

        // required for unlink ring and notification volume
        if (ringerModeChangeReceiver != null)
            appContext.unregisterReceiver(ringerModeChangeReceiver);
        ringerModeChangeReceiver = new RingerModeChangeReceiver();
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        appContext.registerReceiver(ringerModeChangeReceiver, intentFilter7);

        // required for unlink ring and notification volume
        if (settingsContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);
        //settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
        settingsContentObserver = new SettingsContentObserver(appContext, new Handler());
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);

        // required for Connect to SSIS profile preference +
        // wifi connection type = (dis)connected, radio switch event +
        // wifi scanner
        if (wifiStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
        wifiStateChangedBroadcastReceiver = new WifiStateChangedBroadcastReceiver();
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        appContext.registerReceiver(wifiStateChangedBroadcastReceiver, intentFilter8);

        // required for start EventsService in idle maintenance window
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (deviceIdleModeReceiver != null)
                appContext.unregisterReceiver(deviceIdleModeReceiver);
            deviceIdleModeReceiver = new DeviceIdleModeBroadcastReceiver();
            IntentFilter intentFilter9 = new IntentFilter();
            intentFilter9.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
            // is @hide :-(
            //if (android.os.Build.VERSION.SDK_INT >= 24)
            //    intentFilter9.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
            appContext.registerReceiver(deviceIdleModeReceiver, intentFilter9);
        }

        // --------------------------------

        // --- receivers and content observers for events -- register it only if any event exists

        // required for battery event
        if (batteryEventReceiver != null)
            appContext.unregisterReceiver(batteryEventReceiver);
        batteryEventReceiver = new BatteryBroadcastReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(Intent.ACTION_BATTERY_CHANGED);
        appContext.registerReceiver(batteryEventReceiver, intentFilter1);

        // required for peripherals event
        if (headsetPlugReceiver != null)
            appContext.unregisterReceiver(headsetPlugReceiver);
        headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter2.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        intentFilter2.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        appContext.registerReceiver(headsetPlugReceiver, intentFilter2);

        // required for peripherals event
        if (dockConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(dockConnectionBroadcastReceiver);
        dockConnectionBroadcastReceiver = new DockConnectionBroadcastReceiver();
        IntentFilter intentFilter12 = new IntentFilter();
        intentFilter12.addAction(Intent.ACTION_DOCK_EVENT);
        intentFilter12.addAction("android.intent.action.ACTION_DOCK_EVENT");
        appContext.registerReceiver(dockConnectionBroadcastReceiver, intentFilter12);

        // required for wifi connection type = (dis)connected event
        if (wifiConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiConnectionBroadcastReceiver);
        wifiConnectionBroadcastReceiver = new WifiConnectionBroadcastReceiver();
        IntentFilter intentFilter13 = new IntentFilter();
        intentFilter13.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        appContext.registerReceiver(wifiConnectionBroadcastReceiver, intentFilter13);

        // required for bluetooth connection type = (dis)connected
        if (bluetoothConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(bluetoothConnectionBroadcastReceiver);
        bluetoothConnectionBroadcastReceiver = new BluetoothConnectionBroadcastReceiver();
        IntentFilter intentFilter14 = new IntentFilter();
        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter14.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter14.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        appContext.registerReceiver(bluetoothConnectionBroadcastReceiver, intentFilter14);

        // required for radio switch event
        if (airplaneModeStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(airplaneModeStateChangedBroadcastReceiver);
        airplaneModeStateChangedBroadcastReceiver = new AirplaneModeStateChangedBroadcastReceiver();
        IntentFilter intentFilter19 = new IntentFilter();
        intentFilter19.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        appContext.registerReceiver(airplaneModeStateChangedBroadcastReceiver, intentFilter19);

        // required for radio switch event
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC)) {
                if (nfcStateChangedBroadcastReceiver != null)
                    appContext.unregisterReceiver(nfcStateChangedBroadcastReceiver);
                nfcStateChangedBroadcastReceiver = new NFCStateChangedBroadcastReceiver();
                IntentFilter intentFilter21 = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
                appContext.registerReceiver(nfcStateChangedBroadcastReceiver, intentFilter21);
                //PPApplication.logE("$$$ PhoneProfilesService.onCreate", "registered");
            }
        }

        // required for radio switch event
        if (mobileDataStateChangedContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(mobileDataStateChangedContentObserver);
        mobileDataStateChangedContentObserver = new MobileDataStateChangedContentObserver(appContext, new Handler());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);
        else
            appContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("mobile_data"), true, mobileDataStateChangedContentObserver);

        // required for radio switch event + bluetooth scanner
        if (bluetoothStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(bluetoothStateChangedBroadcastReceiver);
        bluetoothStateChangedBroadcastReceiver = new BluetoothStateChangedBroadcastReceiver();
        IntentFilter intentFilter15 = new IntentFilter();
        intentFilter15.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        appContext.registerReceiver(bluetoothStateChangedBroadcastReceiver, intentFilter15);

        // required for wifi scanner
        if (wifiAPStateChangeBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiAPStateChangeBroadcastReceiver);
        wifiAPStateChangeBroadcastReceiver = new WifiAPStateChangeBroadcastReceiver();
        IntentFilter intentFilter17 = new IntentFilter();
        intentFilter17.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        appContext.registerReceiver(wifiAPStateChangeBroadcastReceiver, intentFilter17);

        // required for location and radio switch event
        if (locationModeChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(locationModeChangedBroadcastReceiver);
        locationModeChangedBroadcastReceiver = new LocationModeChangedBroadcastReceiver();
        IntentFilter intentFilter18 = new IntentFilter();
        intentFilter18.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        if (android.os.Build.VERSION.SDK_INT >= 19)
            intentFilter18.addAction(LocationManager.MODE_CHANGED_ACTION);
        appContext.registerReceiver(locationModeChangedBroadcastReceiver, intentFilter18);

        // required for sms event
        if (smsBroadcastReceiver != null)
            appContext.unregisterReceiver(smsBroadcastReceiver);
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter intentFilter21 = new IntentFilter();
        if (android.os.Build.VERSION.SDK_INT >= 19)
            intentFilter21.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        else
            intentFilter21.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter21.setPriority(Integer.MAX_VALUE);
        appContext.registerReceiver(smsBroadcastReceiver, intentFilter21);

        // required for sms event
        if (mmsBroadcastReceiver != null)
            appContext.unregisterReceiver(mmsBroadcastReceiver);
        mmsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter intentFilter22;
        if (android.os.Build.VERSION.SDK_INT >= 19)
            intentFilter22 = IntentFilter.create(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION, "application/vnd.wap.mms-message");
        else
            intentFilter22 = IntentFilter.create("android.provider.Telephony.WAP_PUSH_RECEIVED", "application/vnd.wap.mms-message");
        intentFilter22.setPriority(Integer.MAX_VALUE);
        appContext.registerReceiver(mmsBroadcastReceiver, intentFilter22);

        // required for calendar event
        if (calendarProviderChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(calendarProviderChangedBroadcastReceiver);
        calendarProviderChangedBroadcastReceiver = new CalendarProviderChangedBroadcastReceiver();
        IntentFilter intentFilter23 = new IntentFilter();
        intentFilter23.addAction(Intent.ACTION_PROVIDER_CHANGED);
        intentFilter23.addDataScheme("content");
        intentFilter23.addDataAuthority("com.android.calendar", null);
        intentFilter23.setPriority(Integer.MAX_VALUE);
        appContext.registerReceiver(calendarProviderChangedBroadcastReceiver, intentFilter23);

        // required for all scanner events (wifi, bluetooth, location, mobile cells, device orientation)
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (powerSaveModeReceiver != null)
                appContext.unregisterReceiver(powerSaveModeReceiver);
            powerSaveModeReceiver = new PowerSaveModeBroadcastReceiver();
            IntentFilter intentFilter10 = new IntentFilter();
            intentFilter10.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            appContext.registerReceiver(powerSaveModeReceiver, intentFilter10);
        }

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

        WifiScanJob.initialize(appContext);
        BluetoothScanJob.initialize(appContext);

        WifiScanJob.scheduleJob(appContext, true, false, false);
        BluetoothScanJob.scheduleJob(appContext, true, false);
        GeofenceScannerJob.scheduleJob(appContext, true, false);
        SearchCalendarEventsJob.scheduleJob(true);

        startGeofenceScanner();
        startPhoneStateScanner();
        startOrientationScanner();

        AboutApplicationJob.scheduleJob();

        ringingMediaPlayer = null;
        //notificationMediaPlayer = null;

        PPApplication.logE("$$$ PhoneProfilesService.onCreate", "OK created");
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("PhoneProfilesService.onDestroy", "xxxxx");

        Context appContext = getApplicationContext();

        if (batteryEventReceiver != null)
            appContext.unregisterReceiver(batteryEventReceiver);
        if (headsetPlugReceiver != null)
            appContext.unregisterReceiver(headsetPlugReceiver);
        if (screenOnOffReceiver != null)
            appContext.unregisterReceiver(screenOnOffReceiver);
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (deviceIdleModeReceiver != null)
                appContext.unregisterReceiver(deviceIdleModeReceiver);
        if (android.os.Build.VERSION.SDK_INT >= 21)
            if (powerSaveModeReceiver != null)
                appContext.unregisterReceiver(powerSaveModeReceiver);
        if (android.os.Build.VERSION.SDK_INT >= 23)
            if (interruptionFilterChangedReceiver != null)
                appContext.unregisterReceiver(interruptionFilterChangedReceiver);
        if (PPApplication.hasSystemFeature(this, PackageManager.FEATURE_NFC))
            if (nfcStateChangedBroadcastReceiver != null) {
                //try {
                appContext.unregisterReceiver(nfcStateChangedBroadcastReceiver);
                //} catch (Exception ignored) {
                //}
            }
        if (phoneCallBroadcastReceiver != null)
            appContext.unregisterReceiver(phoneCallBroadcastReceiver);
        if (ringerModeChangeReceiver != null)
            appContext.unregisterReceiver(ringerModeChangeReceiver);
        if (wifiStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiStateChangedBroadcastReceiver);
        if (dockConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(dockConnectionBroadcastReceiver);
        if (wifiConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiConnectionBroadcastReceiver);
        if (bluetoothConnectionBroadcastReceiver != null)
            appContext.unregisterReceiver(bluetoothConnectionBroadcastReceiver);
        if (bluetoothStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(bluetoothStateChangedBroadcastReceiver);
        if (alarmClockBroadcastReceiver != null)
            appContext.unregisterReceiver(alarmClockBroadcastReceiver);
        if (wifiAPStateChangeBroadcastReceiver != null)
            appContext.unregisterReceiver(wifiAPStateChangeBroadcastReceiver);
        if (locationModeChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(locationModeChangedBroadcastReceiver);
        if (airplaneModeStateChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(airplaneModeStateChangedBroadcastReceiver);
        if (smsBroadcastReceiver != null)
            appContext.unregisterReceiver(smsBroadcastReceiver);
        if (mmsBroadcastReceiver != null)
            appContext.unregisterReceiver(mmsBroadcastReceiver);
        if (calendarProviderChangedBroadcastReceiver != null)
            appContext.unregisterReceiver(calendarProviderChangedBroadcastReceiver);

        if (settingsContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(settingsContentObserver);

        if (mobileDataStateChangedContentObserver != null)
            appContext.getContentResolver().unregisterContentObserver(mobileDataStateChangedContentObserver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(appContext);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(appContext);

        stopGeofenceScanner();
        stopOrientationScanner();
        stopPhoneStateScanner();

        stopSimulatingRingingCall(true);
        //stopSimulatingNotificationTone(true);

        reenableKeyguard();

        removeProfileNotification(this);

        instance = null;
        serviceRunning = false;

        super.onDestroy();
    }

    // start service for first start
    private boolean doForFirstStart(Intent intent, int flags, int startId) {
        boolean onlyStart = true;

        Context appContext = getApplicationContext();

        // set service foreground
        final DataWrapper dataWrapper =  new DataWrapper(this, true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
        Profile activatedProfile = dataWrapper.getActivatedProfile();
        showProfileNotification(activatedProfile, dataWrapper);

        Intent serviceIntent = new Intent(appContext, FirstStartService.class);

        if (intent != null) {
            onlyStart = intent.getBooleanExtra(EXTRA_ONLY_START, true);
            if (onlyStart)
                PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_ONLY_START");
            serviceIntent.putExtra(EXTRA_START_ON_BOOT, intent.getBooleanExtra(EXTRA_START_ON_BOOT, false));
        }

        if (onlyStart) {
            // start FirstStartService
            WakefulIntentService.sendWakefulWork(appContext, serviceIntent);

            ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, false);
        }

        return onlyStart;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "intent="+intent);

        serviceRunning = true;

        if (!doForFirstStart(intent, flags, startId)) {
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

                if (intent.getBooleanExtra(EventsService.EXTRA_SIMULATE_RINGING_CALL, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_SIMULATE_RINGING_CALL");
                    doSimulatingRingingCall(intent);
                }
                //if (intent.getBooleanExtra(EventsService.EXTRA_SIMULATE_NOTIFICATION_TONE, false))
                //    doSimulatingNotificationTone(intent);

                if (intent.getBooleanExtra(EXTRA_START_STOP_SCANNER, false)) {
                    PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "EXTRA_START_STOP_SCANNER");
                    switch (intent.getIntExtra(EXTRA_START_STOP_SCANNER_TYPE, 0)) {
                        case PPApplication.SCANNER_START_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_GEOFENCE_SCANNER");
                            startGeofenceScanner();
                            break;
                        case PPApplication.SCANNER_STOP_GEOFENCE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_GEOFENCE_SCANNER");
                            stopGeofenceScanner();
                            break;
                        case PPApplication.SCANNER_START_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_ORIENTATION_SCANNER");
                            startOrientationScanner();
                            break;
                        case PPApplication.SCANNER_STOP_ORIENTATION_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_ORIENTATION_SCANNER");
                            stopOrientationScanner();
                            break;
                        case PPApplication.SCANNER_START_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_START_PHONE_STATE_SCANNER");
                            startPhoneStateScanner();
                            break;
                        case PPApplication.SCANNER_STOP_PHONE_STATE_SCANNER:
                            PPApplication.logE("$$$ PhoneProfilesService.onStartCommand", "SCANNER_STOP_PHONE_STATE_SCANNER");
                            stopPhoneStateScanner();
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

            //if (android.os.Build.VERSION.SDK_INT >= 24) {
            //    notificationBuilder.setStyle(new Notification.DecoratedCustomViewStyle());
            //    notificationBuilder.setCustomContentView(contentView);
            //}
            //else
            //noinspection deprecation
            notificationBuilder.setContent(contentView);
            //notificationBuilder.setAutoCancel(true);

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
                    phoneProfilesNotification.flags |= Notification.FLAG_ONGOING_EVENT;
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

        if (PPApplication.getApplicationStarted(this, false)) {
            geofencesScanner = new GeofencesScanner(getApplicationContext());
            geofencesScanner.connect();
        }
    }

    private void stopGeofenceScanner() {
        if (geofencesScanner != null) {
            geofencesScanner.disconnect();
            geofencesScanner = null;
        }
    }

    public static boolean isGeofenceScannerStarted() {
        return (geofencesScanner != null);
    }

    //--------------------------------------------------------------------------

    // Phone state ----------------------------------------------------------------

    private void startPhoneStateScanner() {
        if (phoneStateScanner != null) {
            phoneStateScanner.disconnect();
            //phoneStateScanner = null;
        }

        if (PPApplication.getApplicationStarted(this, false)) {
            if (phoneStateScanner == null)
                phoneStateScanner = new PhoneStateScanner(getApplicationContext());
            phoneStateScanner.connect();
        }
    }

    private void stopPhoneStateScanner() {
        if (phoneStateScanner != null) {
            phoneStateScanner.disconnect();
            //phoneStateScanner = null;
        }
    }

    public static boolean isPhoneStateStarted() {
        return (phoneStateScanner != null);
    }

    //--------------------------------------------------------------------------

    // Device orientation ----------------------------------------------------------------

    private void startOrientationScanner() {
        if (mStartedSensors)
            stopListeningOrientationSensors();

        if (PPApplication.getApplicationStarted(this, false))
            startListeningOrientationSensors();
    }

    private void stopOrientationScanner() {
        stopListeningOrientationSensors();
    }

    public static boolean isOrientationScannerStarted() {
        return mStartedSensors;
    }

    public static Sensor getAccelerometerSensor(Context context) {
        if (mSensorManager == null)
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    public static Sensor getMagneticFieldSensor(Context context) {
        if (mSensorManager == null)
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    public static Sensor getProximitySensor(Context context) {
        if (mSensorManager == null)
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }
    /*@SuppressWarnings("deprecation")
    public static Sensor getOrientationSensor(Context context) {
        if (mSensorManager == null)
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }*/

    @SuppressLint("NewApi")
    private void startListeningOrientationSensors() {
        if (mSensorManager == null)
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (!mStartedSensors) {

            if (PPApplication.isPowerSaveMode && ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(this).equals("2"))
                // start scanning in power save mode is not allowed
                return;

            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, false, 0);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) == 0)
                return;

            int interval = ApplicationPreferences.applicationEventOrientationScanInterval(this);
            if (PPApplication.isPowerSaveMode && ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(this).equals("1"))
                interval *= 2;
            Sensor accelerometer = getAccelerometerSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","accelerometer="+accelerometer);
            if (accelerometer != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (accelerometer.getFifoMaxEventCount() > 0))
                    mSensorManager.registerListener(this, accelerometer, 200000 * interval, 1000000 * interval);
                else
                    mSensorManager.registerListener(this, accelerometer, 1000000 * interval);
            }
            Sensor magneticField = getMagneticFieldSensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","magneticField="+magneticField);
            if (magneticField != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (magneticField.getFifoMaxEventCount() > 0))
                    mSensorManager.registerListener(this, magneticField, 200000 * interval, 1000000 * interval);
                else
                    mSensorManager.registerListener(this, magneticField, 1000000 * interval);
            }
            Sensor proximity = getProximitySensor(getApplicationContext());
            PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","proximity="+proximity);
            if (proximity != null) {
                mMaxProximityDistance = proximity.getMaximumRange();
                if ((android.os.Build.VERSION.SDK_INT >= 19) && (proximity.getFifoMaxEventCount() > 0))
                    mSensorManager.registerListener(this, proximity, 200000 * interval, 1000000 * interval);
                else
                    mSensorManager.registerListener(this, proximity, 1000000 * interval);
            }
            //Sensor orientation = PPApplication.getOrientationSensor(this);
            //PPApplication.logE("PhoneProfilesService.startListeningOrientationSensors","orientation="+orientation);
            mStartedSensors = true;
        }
    }

    private void stopListeningOrientationSensors() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
        mStartedSensors = false;
    }

    public void resetListeningOrientationSensors(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            stopListeningOrientationSensors();
            startListeningOrientationSensors();
        }
    }

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
                    try {
                        Intent serviceIntent = new Intent(getApplicationContext(), DeviceOrientationService.class);
                        WakefulIntentService.sendWakefulWork(getApplicationContext(), serviceIntent);
                    } catch (Exception ignored) {}
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

                                        try {
                                            Intent serviceIntent = new Intent(getApplicationContext(), DeviceOrientationService.class);
                                            WakefulIntentService.sendWakefulWork(getApplicationContext(), serviceIntent);
                                        } catch (Exception ignored) {}

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

                                try {
                                    Intent serviceIntent = new Intent(getApplicationContext(), DeviceOrientationService.class);
                                    WakefulIntentService.sendWakefulWork(getApplicationContext(), serviceIntent);
                                } catch (Exception ignored) {}
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
        if (intent.getBooleanExtra(EventsService.EXTRA_SIMULATE_RINGING_CALL, false))
        {
            PPApplication.logE("PhoneProfilesService.doSimulatingRingingCall", "simulate ringing call");

            Context context = getApplicationContext();

            ringingCallIsSimulating = false;

            int oldRingerMode = intent.getIntExtra(EventsService.EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EventsService.EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EventsService.EXTRA_OLD_ZEN_MODE, 0);
            String oldRingtone = intent.getStringExtra(EventsService.EXTRA_OLD_RINGTONE);
            int oldSystemRingerVolume = intent.getIntExtra(EventsService.EXTRA_OLD_SYSTEM_RINGER_VOLUME, -1);
            int newRingerMode = ActivateProfileHelper.getRingerMode(context);
            int newZenMode = ActivateProfileHelper.getZenMode(context);
            int newRingerVolume = ActivateProfileHelper.getRingerVolume(context);
            String newRingtone = "";
            String phoneNumber = ApplicationPreferences.preferences.getString(PhoneCallService.PREF_EVENT_CALL_PHONE_NUMBER, "");

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

                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldMediaVolume, 0);
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
        if (intent.getBooleanExtra(EventsService.EXTRA_SIMULATE_NOTIFICATION_TONE, false) &&
                !ringingCallIsSimulating)
        {
            PPApplication.logE("PhoneProfilesService.doSimulatingNotificationTone", "simulate notification tone");

            Context context = getApplicationContext();

            notificationToneIsSimulating = false;

            int oldRingerMode = intent.getIntExtra(EventsService.EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EventsService.EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EventsService.EXTRA_OLD_ZEN_MODE, 0);
            String oldNotificationTone = intent.getStringExtra(EventsService.EXTRA_OLD_NOTIFICATION_TONE);
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

            if ((eventNotificationPlayTimer != null) && eventNotificationIsPlayed) {
                eventNotificationPlayTimer.cancel();
                eventNotificationPlayTimer = null;
            }
            if ((eventNotificationMediaPlayer != null) && eventNotificationIsPlayed) {
                if (eventNotificationMediaPlayer.isPlaying())
                    eventNotificationMediaPlayer.stop();
                eventNotificationIsPlayed = true;
                eventNotificationMediaPlayer = null;
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
                                if (eventNotificationMediaPlayer.isPlaying())
                                    eventNotificationMediaPlayer.stop();

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
