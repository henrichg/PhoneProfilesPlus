package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.DEVICE_POLICY_SERVICE;

//import android.app.NotificationChannel;

public class ActivateProfileHelper {

    private DataWrapper dataWrapper;

    private Context context;
    private NotificationManager notificationManager;

    //private int networkType = -1;

    static boolean lockRefresh = false;
    static boolean disableScreenTimeoutInternalChange = false;

    static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    static final int ZENMODE_ALL = 0;
    static final int ZENMODE_PRIORITY = 1;
    static final int ZENMODE_NONE = 2;
    static final int ZENMODE_ALARMS = 3;
    @SuppressWarnings("WeakerAccess")
    static final int ZENMODE_SILENT = 99;

    static final String EXTRA_MERGED_PROFILE = "merged_profile";
    static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";
    static final String EXTRA_STARTED_FROM_BROADCAST = "started_from_broadcast";

    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    //private static final String PREF_SCREEN_UNLOCKED = "screen_unlocked";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";
    static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";


    public ActivateProfileHelper()
    {

    }

    public void initialize(DataWrapper dataWrapper, Context c)
    {
        this.dataWrapper = dataWrapper;

        initializeNoNotificationManager(c);
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initializeNoNotificationManager(Context c)
    {
        context = c;
    }

    void deinitialize()
    {
        dataWrapper = null;
        context = null;
        notificationManager = null;
    }

    @SuppressWarnings("deprecation")
    private void doExecuteForRadios(Profile profile)
    {
        PPApplication.sleep(300);

        // nahodenie network type
        if (profile._deviceNetworkType >= 100) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, context) == PPApplication.PREFERENCE_ALLOWED) {
                setPreferredNetworkType(context, profile._deviceNetworkType - 100);
                //try { Thread.sleep(200); } catch (InterruptedException e) { }
                //SystemClock.sleep(200);
                PPApplication.sleep(200);
            }
        }

        // nahodenie mobilnych dat
        if (profile._deviceMobileData != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == PPApplication.PREFERENCE_ALLOWED) {
                boolean _isMobileData = isMobileData(context);
                //PPApplication.logE("ActivateProfileHelper.doExecuteForRadios","_isMobileData="+_isMobileData);
                boolean _setMobileData = false;
                switch (profile._deviceMobileData) {
                    case 1:
                        if (!_isMobileData) {
                            _isMobileData = true;
                            _setMobileData = true;
                        }
                        break;
                    case 2:
                        if (_isMobileData) {
                            _isMobileData = false;
                            _setMobileData = true;
                        }
                        break;
                    case 3:
                        _isMobileData = !_isMobileData;
                        _setMobileData = true;
                        break;
                }
                if (_setMobileData) {
                    setMobileData(context, _isMobileData);
                    //try { Thread.sleep(200); } catch (InterruptedException e) { }
                    //SystemClock.sleep(200);
                    PPApplication.sleep(200);
                }
            }
        }

        // nahodenie WiFi AP
        boolean canChangeWifi = true;
        if (profile._deviceWiFiAP != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, context) == PPApplication.PREFERENCE_ALLOWED) {
                WifiApManager wifiApManager = null;
                try {
                    wifiApManager = new WifiApManager(context);
                } catch (Exception ignored) {
                }
                if (wifiApManager != null) {
                    boolean setWifiAPState = false;
                    boolean isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                    switch (profile._deviceWiFiAP) {
                        case 1:
                            if (!isWifiAPEnabled) {
                                isWifiAPEnabled = true;
                                setWifiAPState = true;
                                canChangeWifi = false;
                            }
                            break;
                        case 2:
                            if (isWifiAPEnabled) {
                                isWifiAPEnabled = false;
                                setWifiAPState = true;
                                canChangeWifi = true;
                            }
                            break;
                        case 3:
                            isWifiAPEnabled = !isWifiAPEnabled;
                            setWifiAPState = true;
                            canChangeWifi = !isWifiAPEnabled;
                            break;
                    }
                    if (setWifiAPState) {
                        wifiApManager.setWifiApState(isWifiAPEnabled);
                        //try { Thread.sleep(200); } catch (InterruptedException e) { }
                        //SystemClock.sleep(200);
                        PPApplication.sleep(200);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // nahodenie WiFi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, context) == PPApplication.PREFERENCE_ALLOWED) {
                    boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                    if ((!isWifiAPEnabled) || (profile._deviceWiFi == 4)) { // only when wifi AP is not enabled, change wifi
                        PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.doExecuteForRadios-isWifiAPEnabled=false");
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        int wifiState = wifiManager.getWifiState();
                        boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                        boolean setWifiState = false;
                        switch (profile._deviceWiFi) {
                            case 1:
                            case 4:
                                if (!isWifiEnabled) {
                                    isWifiEnabled = true;
                                    setWifiState = true;
                                }
                                break;
                            case 2:
                                if (isWifiEnabled) {
                                    isWifiEnabled = false;
                                    setWifiState = true;
                                }
                                break;
                            case 3:
                            case 5:
                                isWifiEnabled = !isWifiEnabled;
                                setWifiState = true;
                                break;
                        }
                        if (isWifiEnabled)
                            // when wifi is enabled from profile, no disable wifi after scan
                            WifiScanJob.setWifiEnabledForScan(context, false);
                        if (setWifiState) {
                            try {
                                wifiManager.setWifiEnabled(isWifiEnabled);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.doExecuteForRadios", e.toString());
                            }
                            //try { Thread.sleep(200); } catch (InterruptedException e) { }
                            //SystemClock.sleep(200);
                            PPApplication.sleep(200);
                        }
                    }
                }
            }

            // connect to SSID
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, context) == PPApplication.PREFERENCE_ALLOWED) {
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    if  (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                        // check if wifi is connected
                        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
                        boolean wifiConnected = (activeNetwork != null) &&
                                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                                activeNetwork.isConnected();
                        WifiInfo wifiInfo = null;
                        if (wifiConnected)
                            wifiInfo = wifiManager.getConnectionInfo();

                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        if (list != null) {
                            for (WifiConfiguration i : list) {
                                if (i.SSID != null && i.SSID.equals(profile._deviceConnectToSSID)) {
                                    if (wifiConnected) {
                                        if (!wifiInfo.getSSID().equals(i.SSID)) {

                                            PhoneProfilesService.connectToSSIDStarted = true;

                                            // connected to another SSID
                                            wifiManager.disconnect();
                                            wifiManager.enableNetwork(i.networkId, true);
                                            wifiManager.reconnect();
                                        }
                                    } else
                                        wifiManager.enableNetwork(i.networkId, true);
                                    break;
                                }
                            }
                        }
                    }
                }
                //else {
                //    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //    int wifiState = wifiManager.getWifiState();
                //    if  (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                //        wifiManager.disconnect();
                //        wifiManager.reconnect();
                //    }
                //}
                PhoneProfilesService.connectToSSID = profile._deviceConnectToSSID;
            }
        }

        // nahodenie bluetooth
        if (profile._deviceBluetooth != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, context) == PPApplication.PREFERENCE_ALLOWED) {
                PPApplication.logE("ActivateProfileHelper.doExecuteForRadios","setBluetooth");
                BluetoothAdapter bluetoothAdapter = BluetoothScanJob.getBluetoothAdapter(context);
                if (bluetoothAdapter != null) {
                    boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();
                    boolean setBluetoothState = false;
                    switch (profile._deviceBluetooth) {
                        case 1:
                            if (!isBluetoothEnabled) {
                                isBluetoothEnabled = true;
                                setBluetoothState = true;
                            }
                            break;
                        case 2:
                            if (isBluetoothEnabled) {
                                isBluetoothEnabled = false;
                                setBluetoothState = true;
                            }
                            break;
                        case 3:
                            isBluetoothEnabled = !isBluetoothEnabled;
                            setBluetoothState = true;
                            break;
                    }
                    PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "setBluetoothState="+setBluetoothState);
                    PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isBluetoothEnabled="+isBluetoothEnabled);
                    if (isBluetoothEnabled) {
                        // when bluetooth is enabled from profile, no disable bluetooth after scan
                        PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isBluetoothEnabled=true; setBluetoothEnabledForScan=false");
                        BluetoothScanJob.setBluetoothEnabledForScan(context, false);
                    }
                    if (setBluetoothState) {
                        if (isBluetoothEnabled)
                            bluetoothAdapter.enable();
                        else
                            bluetoothAdapter.disable();
                    }
                }
            }
        }

        // nahodenie GPS
        if (profile._deviceGPS != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, context) == PPApplication.PREFERENCE_ALLOWED) {
                //String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                boolean isEnabled;
                if (android.os.Build.VERSION.SDK_INT < 19)
                    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                }
                PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isEnabled=" + isEnabled);


                switch (profile._deviceGPS) {
                    case 1:
                        setGPS(context, true);
                        break;
                    case 2:
                        setGPS(context, false);
                        break;
                    case 3:
                        if (!isEnabled) {
                            setGPS(context, true);
                        } else {
                            setGPS(context, false);
                        }
                        break;
                }
            }
        }

        // nahodenie NFC
        if (profile._deviceNFC != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, context) == PPApplication.PREFERENCE_ALLOWED) {
                //Log.e("ActivateProfileHelper.doExecuteForRadios", "allowed");
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter != null) {
                    switch (profile._deviceNFC) {
                        case 1:
                            setNFC(context, true);
                            break;
                        case 2:
                            setNFC(context, false);
                            break;
                        case 3:
                            if (!nfcAdapter.isEnabled()) {
                                setNFC(context, true);
                            } else if (nfcAdapter.isEnabled()) {
                                setNFC(context, false);
                            }
                            break;
                    }
                }
            }
            //else
            //    Log.e("ActivateProfileHelper.doExecuteForRadios", "not allowed");
        }

    }

    void executeForRadios(Profile profile)
    {
        boolean _isAirplaneMode = false;
        boolean _setAirplaneMode = false;
        if (profile._deviceAirplaneMode != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                _isAirplaneMode = isAirplaneMode(context);
                switch (profile._deviceAirplaneMode) {
                    case 1:
                        if (!_isAirplaneMode) {
                            _isAirplaneMode = true;
                            _setAirplaneMode = true;
                        }
                        break;
                    case 2:
                        if (_isAirplaneMode) {
                            _isAirplaneMode = false;
                            _setAirplaneMode = true;
                        }
                        break;
                    case 3:
                        _isAirplaneMode = !_isAirplaneMode;
                        _setAirplaneMode = true;
                        break;
                }
            }
        }

        if (_setAirplaneMode /*&& _isAirplaneMode*/) {
            // switch ON airplane mode, set it before executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

            PPApplication.sleep(2000);
        }

        doExecuteForRadios(profile);

        /*if (_setAirplaneMode && (!_isAirplaneMode)) {
            // 200 miliseconds is in doExecuteForRadios
            PPApplication.sleep(1800);

            // switch OFF airplane mode, set if after executeForRadios
            setAirplaneMode(context, _isAirplaneMode);
        }*/

    }

    static boolean isAudibleRinging(int ringerMode, int zenMode) {
        return (!((ringerMode == 3) || (ringerMode == 4) ||
                  ((ringerMode == 5) && ((zenMode == 3) || (zenMode == 4) || (zenMode == 5) || (zenMode == 6)))
                 ));
    }

    private boolean isVibrateRingerMode(int ringerMode/*, int zenMode*/) {
        return (ringerMode == 3);

    }

    /*
    private void correctVolume0(AudioManager audioManager) {
        int ringerMode, zenMode;
        ringerMode = PPApplication.getRingerMode(context);
        zenMode = PPApplication.getZenMode(context);
        if ((ringerMode == 1) || (ringerMode == 2) || (ringerMode == 4) ||
            ((ringerMode == 5) && ((zenMode == 1) || (zenMode == 2)))) {
            // any "nonVIBRATE" ringer mode is selected
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                // actual system ringer mode = vibrate
                // volume changed it to vibrate
                //RingerModeChangeReceiver.internalChange = true;
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                PhoneProfilesService.ringingVolume = 1;
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, 1);
            }
        }
    }
    */

    static boolean getMergedRingNotificationVolumes(Context context) {
        ApplicationPreferences.getSharedPreferences(context);
        PPApplication.logE("ActivateProfileHelper.getMergedRingNotificationVolumes", "force set="+ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context));
        PPApplication.logE("ActivateProfileHelper.getMergedRingNotificationVolumes", "merged="+ApplicationPreferences.preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true));
        if (ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context) > 0)
            return ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context) == 1;
        else
            return ApplicationPreferences.preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
    }

    // test if ring and notification volumes are merged
    static void setMergedRingNotificationVolumes(Context context, boolean force) {
        ApplicationPreferences.getSharedPreferences(context);

        PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        setMergedRingNotificationVolumes(context, force, editor);
        editor.apply();
    }

    static void setMergedRingNotificationVolumes(Context context, boolean force, SharedPreferences.Editor editor) {
        ApplicationPreferences.getSharedPreferences(context);

        PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

        if (!ApplicationPreferences.preferences.contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
            try {
                boolean merged;
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = audioManager.getRingerMode();
                int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                int oldRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                int oldNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                if (oldRingVolume == oldNotificationVolume) {
                    int newNotificationVolume;
                    if (oldNotificationVolume == maximumNotificationValue)
                        newNotificationVolume = oldNotificationVolume - 1;
                    else
                        newNotificationVolume = oldNotificationVolume + 1;
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, 0);
                    PPApplication.sleep(1000);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume)
                        merged = true;
                    else
                        merged = false;
                } else
                    merged = false;
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                audioManager.setRingerMode(ringerMode);

                PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "merged="+merged);

                editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
            } catch (Exception ignored) {}
        }
    }

    @SuppressLint("NewApi")
    void setVolumes(Profile profile, AudioManager audioManager, int linkUnlink, boolean forProfileActivation)
    {
        if (profile.getVolumeRingtoneChange()) {
            if (forProfileActivation)
                setRingerVolume(context, profile.getVolumeRingtoneValue());
        }
        if (profile.getVolumeNotificationChange()) {
            if (forProfileActivation)
                setNotificationVolume(context, profile.getVolumeNotificationValue());
        }

        int ringerMode = getRingerMode(context);
        int zenMode = getZenMode(context);

        PPApplication.logE("ActivateProfileHelper.setVolumes", "ringerMode=" + ringerMode);
        PPApplication.logE("ActivateProfileHelper.setVolumes", "zenMode=" + zenMode);
        PPApplication.logE("ActivateProfileHelper.setVolumes", "linkUnlink=" + linkUnlink);
        PPApplication.logE("ActivateProfileHelper.setVolumes", "forProfileActivation=" + forProfileActivation);

        // for ringer mode VIBRATE or SILENT or
        // for interruption types NONE and ONLY_ALARMS
        // not set system, ringer, notification volume
        // (Android 6 - priority mode = ONLY_ALARMS)
        if (isAudibleRinging(ringerMode, zenMode)) {

            PPApplication.logE("ActivateProfileHelper.setVolumes", "ringer/notif/system change");

            //if (Permissions.checkAccessNotificationPolicy(context)) {

                if (forProfileActivation) {
                    if (profile.getVolumeSystemChange()) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.getVolumeSystemValue(), 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                            //correctVolume0(audioManager);
                        } catch (Exception ignored) { }
                    }
                }

                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int callState = telephony.getCallState();

                boolean volumesSet = false;
                if (getMergedRingNotificationVolumes(context) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
                    //if (doUnlink) {
                    //if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK) {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        // for separating ringing and notification
                        // in ringing state ringer volumes must by set
                        // and notification volumes must not by set
                        int volume = getRingerVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-RINGING  ringer volume=" + volume);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                PhoneProfilesService.ringingVolume = volume;
                                //PhoneProfilesService.notificationVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                //correctVolume0(audioManager);
                            } catch (Exception ignored) { }
                        }
                        volumesSet = true;
                    } else if (linkUnlink == PhoneCallService.LINKMODE_LINK) {
                        // for separating ringing and notification
                        // in not ringing state ringer and notification volume must by change
                        //Log.e("ActivateProfileHelper","setVolumes get audio mode="+audioManager.getMode());
                        int volume = getRingerVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-link  ringer volume=" + volume);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set ring volume="+volume);
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                PhoneProfilesService.ringingVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                            } catch (Exception ignored) { }
                        }
                        volume = getNotificationVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-link  notification volume=" + volume);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set notification volume="+volume);
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //PhoneProfilesService.notificationVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                            } catch (Exception ignored) { }
                        }
                        //correctVolume0(audioManager);
                        volumesSet = true;
                    } else {
                        int volume = getRingerVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING  ringer volume=" + volume);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                                PhoneProfilesService.ringingVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                //correctVolume0(audioManager);
                            } catch (Exception ignored) { }
                        }
                        volume = getNotificationVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING  notification volume=" + volume);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //PhoneProfilesService.notificationVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                //correctVolume0(audioManager);
                            } catch (Exception ignored) { }
                        }
                        volumesSet = true;
                    }
                /*}
                else {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        int volume = PPApplication.getRingerVolume(context);
                        if (volume == -999)
                            volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        PhoneProfilesService.ringingVolume = volume;
                    }
                }*/
                }
                if (!volumesSet) {
                    // reverted order for disabled unlink
                    int volume;
                    if (!getMergedRingNotificationVolumes(context)) {
                        volume = getNotificationVolume(context);
                        PPApplication.logE("ActivateProfileHelper.setVolumes", "no doUnlink  notification volume=" + volume);
                        if (volume != -999) {
                            try {
                                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                                //PhoneProfilesService.notificationVolume = volume;
                                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                //correctVolume0(audioManager);
                            } catch (Exception ignored) { }
                        }
                    }
                    volume = getRingerVolume(context);
                    PPApplication.logE("ActivateProfileHelper.setVolumes", "no doUnlink  ringer volume=" + volume);
                    if (volume != -999) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                            PhoneProfilesService.ringingVolume = volume;
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                            //correctVolume0(audioManager);
                        } catch (Exception ignored) { }
                    }
                }
            //}
            //else
            //    PPApplication.logE("ActivateProfileHelper.setVolumes", "not granted");
        }

        if (forProfileActivation) {
            if (profile.getVolumeMediaChange()) {
                // Fatal Exception: java.lang.SecurityException: Only SystemUI can disable the safe media volume:
                // Neither user 10118 nor current process has android.permission.STATUS_BAR_SERVICE.
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
                } catch (SecurityException e) {
                    // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
                    if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                        try {
                            Settings.Global.putInt(context.getContentResolver(), "audio_safe_volume_state", 2);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                        }
                        catch (Exception ignored) {}
                    }
                    else {
                        synchronized (PPApplication.startRootCommandMutex) {
                            String command1 = "settings put global audio_safe_volume_state 2";
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                            } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (profile.getVolumeAlarmChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, profile.getVolumeAlarmValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
                } catch (Exception ignored) {}
            }
            if (profile.getVolumeVoiceChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, profile.getVolumeVoiceValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception ignored) {}
            }
        }

    }

    private void setZenMode(int zenMode, AudioManager audioManager, int ringerMode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            PPApplication.logE("ActivateProfileHelper.setZenMode", "zenMode=" + zenMode);
            PPApplication.logE("ActivateProfileHelper.setZenMode", "ringerMode=" + ringerMode);

            int _zenMode = getSystemZenMode(context, -1);
            PPApplication.logE("ActivateProfileHelper.setZenMode", "_zenMode=" + _zenMode);
            int _ringerMode = audioManager.getRingerMode();
            PPApplication.logE("ActivateProfileHelper.setZenMode", "_ringerMode=" + _ringerMode);

            if ((zenMode != ZENMODE_SILENT) && canChangeZenMode(context, false)) {
                audioManager.setRingerMode(ringerMode);
                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                if ((zenMode != _zenMode) || (zenMode == ZENMODE_PRIORITY)) {
                    PPNotificationListenerService.requestInterruptionFilter(context, zenMode);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(context, zenMode);
                }
            } else {
                try {
                    switch (zenMode) {
                        case ZENMODE_SILENT:
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                            //SystemClock.sleep(1000);
                            PPApplication.sleep(1000);
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                        default:
                            audioManager.setRingerMode(ringerMode);
                    }
                } catch (Exception ignored) {
                    // may be produced this exception:
                    //
                    // java.lang.SecurityException: Not allowed to change Do Not Disturb state
                    //
                    // when changed is ringer mode in activated Do not disturb and
                    // GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context) returns false.
                }
            }
        }
        else
            audioManager.setRingerMode(ringerMode);
    }

    private void setVibrateWhenRinging(Profile profile, int value) {
        int lValue = value;
        if (profile != null) {
            switch (profile._vibrateWhenRinging) {
                case 1:
                    lValue = 1;
                    break;
                case 2:
                    lValue = 0;
                    break;
            }
        }

        if (lValue != -1) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, context)
                    == PPApplication.PREFERENCE_ALLOWED) {
                if (Permissions.checkProfileVibrateWhenRinging(context, profile)) {
                    if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                        Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", lValue);
                    else {
                        try {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, lValue);
                        } catch (Exception ee) {
                            Log.e("ActivateProfileHelper.setVibrateWhenRinging", ee.toString());

                            if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                                synchronized (PPApplication.startRootCommandMutex) {
                                    String command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        //RootTools.closeAllShells();
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        commandWait(command);
                                    } catch (Exception e) {
                                        Log.e("ActivateProfileHelper.setVibrateWhenRinging", "Error on run su: " + e.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void setTones(Profile profile) {
        if (Permissions.checkProfileRingtones(context, profile)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, profile._soundRingtone);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(profile._soundRingtone));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, null);
                    }
                    catch (Exception ignored){ }
                }
            }
            if (profile._soundNotificationChange == 1) {
                if (!profile._soundNotification.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, profile._soundNotification);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(profile._soundNotification));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, null);
                    }
                    catch (Exception ignored){ }
                }
            }
            if (profile._soundAlarmChange == 1) {
                if (!profile._soundAlarm.isEmpty()) {
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, profile._soundAlarm);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, Uri.parse(profile._soundAlarm));
                    }
                    catch (Exception ignored){ }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
                        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, null);
                    }
                    catch (Exception ignored){ }
                }
            }
        }
    }

    private void setNotificationLed(int value) {
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, context)
                == PPApplication.PREFERENCE_ALLOWED) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", value);
            else {
                /* not working (private secure settings) :-/
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", value);
                }
                else*/
                if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                    synchronized (PPApplication.startRootCommandMutex) {
                        String command1 = "settings put system " + "notification_light_pulse" + " " + value;
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                        Command command = new Command(0, false, command1); //, command2);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setNotificationLed", "Error on run su: " + e.toString());
                        }
                    }
                }
            }
        }
    }

    void changeRingerModeForVolumeEqual0(Profile profile) {
        if (profile.getVolumeRingtoneChange()) {
            //int ringerMode = PPApplication.getRingerMode(context);
            //int zenMode = PPApplication.getZenMode(context);

            //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "ringerMode=" + ringerMode);
            //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "zenMode=" + zenMode);

            if (profile.getVolumeRingtoneValue() == 0) {
                profile.setVolumeRingtoneValue(1);

                // for profile ringer/zen mode = "only vibrate" do not change ringer mode to Silent
                if (!isVibrateRingerMode(profile._volumeRingerMode/*, profile._volumeZenMode*/)) {
                    // for ringer mode VIBRATE or SILENT or
                    // for interruption types NONE and ONLY_ALARMS
                    // not change ringer mode
                    // (Android 6 - priority mode = ONLY_ALARMS)
                    if (isAudibleRinging(profile._volumeRingerMode, profile._volumeZenMode)) {
                        // change ringer mode to Silent
                        PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "changed to silent");
                        profile._volumeRingerMode = 4;
                    }
                }
            }
        }
    }

    void changeNotificationVolumeForVolumeEqual0(Profile profile) {
        if (profile.getVolumeNotificationChange() && getMergedRingNotificationVolumes(context)) {
            if (profile.getVolumeNotificationValue() == 0) {
                PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "changed notification value to 1");
                profile.setVolumeNotificationValue(1);
            }
        }
    }

    static boolean canChangeZenMode(Context context, boolean notCheckAccess) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                if (notCheckAccess)
                    return true;
                else
                    return Permissions.checkAccessNotificationPolicy(context);
            }
            else
                return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23))
            return PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
        return false;
    }

    @SuppressLint("SwitchIntDef")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static int getSystemZenMode(Context context, int defaultValue) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                switch (interruptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        return ActivateProfileHelper.ZENMODE_ALL;
                }
            }
            else {
                int interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
                switch (interruptionFilter) {
                    case 0:
                        return ActivateProfileHelper.ZENMODE_ALL;
                    case 1:
                        return ActivateProfileHelper.ZENMODE_PRIORITY;
                    case 2:
                        return ActivateProfileHelper.ZENMODE_NONE;
                    case 3:
                        return ActivateProfileHelper.ZENMODE_ALARMS;
                }
            }
        }
        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 23)) {
            int interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
            switch (interruptionFilter) {
                case 0:
                    return ActivateProfileHelper.ZENMODE_ALL;
                case 1:
                    return ActivateProfileHelper.ZENMODE_PRIORITY;
                case 2:
                    return ActivateProfileHelper.ZENMODE_NONE;
                case 3:
                    return ActivateProfileHelper.ZENMODE_ALARMS;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("deprecation")
    static boolean vibrationIsOn(/*Context context, */AudioManager audioManager, boolean testRingerMode) {
        int ringerMode = -999;
        if (testRingerMode)
            ringerMode = audioManager.getRingerMode();
        int vibrateType = -999;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        //int vibrateWhenRinging;
        //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        //else
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        PPApplication.logE("PPApplication.vibrationIsOn", "ringerMode="+ringerMode);
        PPApplication.logE("PPApplication.vibrationIsOn", "vibrateType="+vibrateType);
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateWhenRinging="+vibrateWhenRinging);

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT);// ||
        //(vibrateWhenRinging == 1);
    }

    @SuppressWarnings("deprecation")
    void setRingerMode(Profile profile, AudioManager audioManager, boolean firstCall, boolean forProfileActivation)
    {
        //PPApplication.logE("@@@ ActivateProfileHelper.setRingerMode", "audioM.ringerMode=" + audioManager.getRingerMode());

        int ringerMode;
        int zenMode;

        if (forProfileActivation) {
            if (profile._volumeRingerMode != 0) {
                setRingerMode(context, profile._volumeRingerMode);
                if ((profile._volumeRingerMode == 5) && (profile._volumeZenMode != 0))
                    setZenMode(context, profile._volumeZenMode);
            }
        }

        if (firstCall)
            return;

        ringerMode = getRingerMode(context);
        zenMode = getZenMode(context);

        PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringerMode=" + ringerMode);
        PPApplication.logE("ActivateProfileHelper.setRingerMode", "zenMode=" + zenMode);

        if (forProfileActivation) {

            PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode change");

            switch (ringerMode) {
                case 1:  // Ring
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 0);
                    break;
                case 2:  // Ring & Vibrate
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 1);
                    break;
                case 3:  // Vibrate
                    setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception ignored) {
                    }
                    setVibrateWhenRinging(null, 1);
                    break;
                case 4:  // Silent
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        //setZenMode(ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_SILENT);
                        setZenMode(ZENMODE_SILENT, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    }
                    else {
                        setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_SILENT);
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception ignored) {
                        }
                    }
                    setVibrateWhenRinging(null, 0);
                    break;
                case 5: // Zen mode
                    switch (zenMode) {
                        case 1:
                            setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(profile, -1);
                            break;
                        case 2:
                            setZenMode(ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(profile, -1);
                            break;
                        case 3:
                            // must be AudioManager.RINGER_MODE_SILENT, because, ZENMODE_NONE set it to silent
                            // without this, duplicate set this zen mode not working
                            setZenMode(ZENMODE_NONE, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                        case 4:
                            setZenMode(ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(null, 1);
                            break;
                        case 5:
                            setZenMode(ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(null, 1);
                            break;
                        case 6:
                            // must be AudioManager.RINGER_MODE_SILENT, because, ZENMODE_ALARMS set it to silent
                            // without this, duplicate set this zen mode not working
                            setZenMode(ZENMODE_ALARMS, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
            }
        }
    }

    void executeForWallpaper(Profile profile) {
        if (profile._deviceWallpaperChange == 1)
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (android.os.Build.VERSION.SDK_INT >= 17)
                display.getRealMetrics(displayMetrics);
            else
                display.getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //noinspection SuspiciousNameCombination
                height = displayMetrics.widthPixels;
                //noinspection SuspiciousNameCombination
                width = displayMetrics.heightPixels;
            }
            //Log.d("ActivateProfileHelper.executeForWallpaper", "height="+height);
            //Log.d("ActivateProfileHelper.executeForWallpaper", "width="+width);
            // for lock screen no double width
            if ((android.os.Build.VERSION.SDK_INT < 24) || (profile._deviceWallpaperFor != 2))
                width = width << 1; // best wallpaper width is twice screen width

            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmapUri(profile._deviceWallpaper, width, height, context);
            if (decodedSampleBitmap != null)
            {
                // set wallpaper
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                try {
                    if (android.os.Build.VERSION.SDK_INT >= 24) {
                        int flags = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;
                        Rect visibleCropHint = null;
                        if (profile._deviceWallpaperFor == 1)
                            flags = WallpaperManager.FLAG_SYSTEM;
                        if (profile._deviceWallpaperFor == 2) {
                            flags = WallpaperManager.FLAG_LOCK;
                            int left = 0;
                            int right = decodedSampleBitmap.getWidth();
                            if (decodedSampleBitmap.getWidth() > width) {
                                left = (decodedSampleBitmap.getWidth() / 2) - (width / 2);
                                right = (decodedSampleBitmap.getWidth() / 2) + (width / 2);
                            }
                            visibleCropHint = new Rect(left, 0, right, decodedSampleBitmap.getHeight());
                        }
                        //noinspection WrongConstant
                        wallpaperManager.setBitmap(decodedSampleBitmap, visibleCropHint, true, flags);
                    }
                    else
                        wallpaperManager.setBitmap(decodedSampleBitmap);
                } catch (IOException e) {
                    Log.e("ActivateProfileHelper.executeForWallpaper", "Cannot set wallpaper. Image="+profile._deviceWallpaper);
                }
            }
        }
    }

    // not working, returns only calling process :-/
    // http://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag
    /*private boolean isRunning(List<ActivityManager.RunningAppProcessInfo> procInfos, String packageName) {
        PPApplication.logE("ActivateProfileHelper.executeForRunApplications", "procInfos.size()="+procInfos.size());
        for(int i = 0; i < procInfos.size(); i++)
        {
            ActivityManager.RunningAppProcessInfo procInfo = procInfos.get(i);
            PPApplication.logE("ActivateProfileHelper.executeForRunApplications", "procInfo.processName="+procInfo.processName);
            if (procInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                PPApplication.logE("ActivateProfileHelper.executeForRunApplications", "procInfo.importance=IMPORTANCE_FOREGROUND");
                for (String pkgName : procInfo.pkgList) {
                    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", "pkgName="+pkgName);
                    if (pkgName.equals(packageName))
                        return true;
                }
            }
        }
        return false;
    }*/

    void executeForRunApplications(Profile profile) {
        if (profile._deviceRunApplicationChange == 1)
        {
            String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
            Intent intent;
            PackageManager packageManager = context.getPackageManager();

            //ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();

            for (String split : splits) {
                //Log.d("ActivateProfileHelper.executeForRunApplications","app data="+splits[i]);
                if (!ApplicationsCache.isShortcut(split)) {
                    //Log.d("ActivateProfileHelper.executeForRunApplications","no shortcut");
                    String packageName = ApplicationsCache.getPackageName(split);
                    intent = packageManager.getLaunchIntentForPackage(packageName);
                    if (intent != null) {
                        //if (!isRunning(procInfos, packageName)) {
                        //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName+": not running");
                        //Log.d("ActivateProfileHelper.executeForRunApplications","intent="+intent);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(intent);
                        } catch (Exception ignored) {
                        }
                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                        //SystemClock.sleep(1000);
                        PPApplication.sleep(1000);
                        //}
                        //else
                        //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName+": running");
                    }
                } else {
                    //Log.d("ActivateProfileHelper.executeForRunApplications","shortcut");
                    long shortcutId = ApplicationsCache.getShortcutId(split);
                    //Log.d("ActivateProfileHelper.executeForRunApplications","shortcutId="+shortcutId);
                    if (shortcutId > 0) {
                        Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                        if (shortcut != null) {
                            try {
                                intent = Intent.parseUri(shortcut._intent, 0);
                                if (intent != null) {
                                    //String packageName = intent.getPackage();
                                    //if (!isRunning(procInfos, packageName)) {
                                    //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName + ": not running");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //Log.d("ActivateProfileHelper.executeForRunApplications","intent="+intent);
                                    try {
                                        context.startActivity(intent);
                                    } catch (Exception ignored) {
                                    }
                                    //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                    //SystemClock.sleep(1000);
                                    PPApplication.sleep(1000);
                                    //} else
                                    //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName + ": running");
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }
    }

    void executeRootForAdaptiveBrightness(Profile profile) {
        /* not working (private secure settings) :-/
        if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            Settings.System.putFloat(context.getContentResolver(), ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                    profile.getDeviceBrightnessAdaptiveValue(context));
        }
        else {*/
            if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                synchronized (PPApplication.startRootCommandMutex) {
                    String command1 = "settings put system " + ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " +
                            Float.toString(profile.getDeviceBrightnessAdaptiveValue(context));
                    //if (PPApplication.isSELinuxEnforcing())
                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                    Command command = new Command(0, false, command1); //, command2);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.execute", "Error on run su: " + e.toString());
                    }
                }
            }
        //}
    }

    public void execute(Profile _profile, boolean merged, boolean _interactive)
    {
        PPApplication.logE("##### ActivateProfileHelper.execute", "xxx");

        // rozdelit zvonenie a notifikacie - zial je to oznacene ako @Hide :-(
        //Settings.System.putInt(context.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        final Profile profile = Profile.getMappedProfile(_profile, context);

        // nahodenie volume
        // run service for execute volumes
        PPApplication.logE("ActivateProfileHelper.execute", "ExecuteVolumeProfilePrefsService");
        Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
        volumeServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        volumeServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
        volumeServiceIntent.putExtra(EXTRA_FOR_PROFILE_ACTIVATION, true);
        WakefulIntentService.sendWakefulWork(context, volumeServiceIntent);

        // set vibration on touch
        if (Permissions.checkProfileVibrationOnTouch(context, profile)) {
            switch (profile._vibrationOnTouch) {
                case 1:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                    break;
                case 2:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                    break;
            }
        }

        // nahodenie  tonov
        // moved to ExecuteVolumeProfilePrefsService
        //setTones(profile);

        //// nahodenie radio preferences
        // run service for execute radios
        Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
        radioServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        radioServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
        WakefulIntentService.sendWakefulWork(context, radioServiceIntent);

        // nahodenie auto-sync
        boolean _isAutoSync = ContentResolver.getMasterSyncAutomatically();
        boolean _setAutoSync = false;
        switch (profile._deviceAutoSync) {
            case 1:
                if (!_isAutoSync)
                {
                    _isAutoSync = true;
                    _setAutoSync = true;
                }
                break;
            case 2:
                if (_isAutoSync)
                {
                    _isAutoSync = false;
                    _setAutoSync = true;
                }
                break;
            case 3:
                _isAutoSync = !_isAutoSync;
                _setAutoSync = true;
                break;
        }
        if (_setAutoSync)
            ContentResolver.setMasterSyncAutomatically(_isAutoSync);

        // screen timeout
        if (Permissions.checkProfileScreenTimeout(context, profile)) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            if (pm.isScreenOn()) {
                //Log.d("ActivateProfileHelper.execute","screen on");
                if (PPApplication.screenTimeoutHandler != null) {
                    final Context _context = context;
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            setScreenTimeout(profile._deviceScreenTimeout, _context);
                        }
                    });
                }// else
                //    setScreenTimeout(profile._deviceScreenTimeout);
            }
            else {
                //Log.d("ActivateProfileHelper.execute","screen off");
                setActivatedProfileScreenTimeout(context, profile._deviceScreenTimeout);
            }
        }
        //else
        //    PPApplication.setActivatedProfileScreenTimeout(context, 0);

        // zapnutie/vypnutie lockscreenu
        //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard");
        boolean setLockScreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lockscreen
                PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard=ON");
                setLockScreenDisabled(context, false);
                setLockScreen = true;
                break;
            case 2:
                // disable lockscreen
                PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard=OFF");
                setLockScreenDisabled(context, true);
                setLockScreen = true;
                break;
        }
        if (setLockScreen) {
            boolean isScreenOn;
            //if (android.os.Build.VERSION.SDK_INT >= 20)
            //{
            //	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //	isScreenOn = display.getState() != Display.STATE_OFF;
            //}
            //else
            //{
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            isScreenOn = pm.isScreenOn();
            //}
            PPApplication.logE("$$$ ActivateProfileHelper.execute","isScreenOn="+isScreenOn);
            boolean keyguardShowing;
            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            keyguardShowing = kgMgr.isKeyguardLocked();
            PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguardShowing="+keyguardShowing);

            if (isScreenOn && !keyguardShowing) {
                Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
                context.startService(keyguardService);
            }
        }

        // nahodenie podsvietenia
        if (Permissions.checkProfileScreenBrightness(context, profile)) {
            if (profile.getDeviceBrightnessChange()) {
                PPApplication.logE("ActivateProfileHelper.execute", "set brightness: profile=" + profile._name);
                PPApplication.logE("ActivateProfileHelper.execute", "set brightness: _deviceBrightness=" + profile._deviceBrightness);

                if (profile.getDeviceBrightnessAutomatic()) {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, context)
                            == PPApplication.PREFERENCE_ALLOWED) {
                        if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                            Settings.System.putFloat(context.getContentResolver(),
                                    ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                                    profile.getDeviceBrightnessAdaptiveValue(context));
                        else {
                            try {
                                Settings.System.putFloat(context.getContentResolver(),
                                        ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                                        profile.getDeviceBrightnessAdaptiveValue(context));
                            } catch (Exception ee) {
                                // run service for execute radios
                                Intent rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
                                rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_ADAPTIVE_BRIGHTNESS);
                                rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                                rootServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
                                context.startService(rootServiceIntent);
                            }
                        }
                    }
                } else {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
                }

                if (PPApplication.brightnessHandler != null) {
                    final Context __context = context;
                    PPApplication.brightnessHandler.post(new Runnable() {
                        public void run() {
                            createBrightnessView(__context);
                        }
                    });
                }// else
                //    createBrightnessView(context);
            }
        }

        // nahodenie rotate
        if (Permissions.checkProfileAutoRotation(context, profile)) {
            switch (profile._deviceAutoRotate) {
                case 1:
                    // set autorotate on
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 2:
                    // set autorotate off
                    // degree 0
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 3:
                    // set autorotate off
                    // degree 90
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    break;
                case 4:
                    // set autorotate off
                    // degree 180
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    break;
                case 5:
                    // set autorotate off
                    // degree 270
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    break;
            }
        }

        // set notification led
        if (profile._notificationLed != 0) {
            //if (Permissions.checkProfileNotificationLed(context, profile)) { not needed for Android 6+, because root is required
            switch (profile._notificationLed) {
                case 1:
                    setNotificationLed(1);
                    break;
                case 2:
                    setNotificationLed(0);
                    break;
            }
            //}
        }

        // nahodenie pozadia
        if (Permissions.checkProfileWallpaper(context, profile)) {
            if (profile._deviceWallpaperChange == 1) {
                Intent wallpaperServiceIntent = new Intent(context, ExecuteWallpaperProfilePrefsService.class);
                wallpaperServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                wallpaperServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
                context.startService(wallpaperServiceIntent);
            }
        }

        // set power save mode
        Intent rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
        rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_POWER_SAVE_MODE);
        rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        rootServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
        context.startService(rootServiceIntent);

        if (Permissions.checkProfileLockDevice(context, profile)) {
            if (profile._lockDevice != 0) {
                boolean keyguardLocked;
                KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                keyguardLocked = kgMgr.isKeyguardLocked();
                PPApplication.logE("---$$$ ActivateProfileHelper.execute","keyguardLocked="+keyguardLocked);
                if (!keyguardLocked) {
                    rootServiceIntent = new Intent(context, ExecuteRootProfilePrefsService.class);
                    rootServiceIntent.setAction(ExecuteRootProfilePrefsService.ACTION_LOCK_DEVICE);
                    rootServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    rootServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
                    context.startService(rootServiceIntent);
                }
            }
        }

        if (_interactive)
        {
            // preferences, ktore vyzaduju interakciu uzivatela

            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == PPApplication.PREFERENCE_ALLOWED)
            {
                if (profile._deviceMobileDataPrefs == 1)
                {
                    /*try {
                        final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                        intent.setComponent(componentName);
                        context.startActivity(intent);
                        PPApplication.logE("#### ActivateProfileHelper.execute","mobile data prefs. 1");
                    } catch (Exception e) {
                        PPApplication.logE("#### ActivateProfileHelper.execute","mobile data prefs. 1 E="+e);
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            PPApplication.logE("#### ActivateProfileHelper.execute","mobile data prefs. 2");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            PPApplication.logE("#### ActivateProfileHelper.execute","mobile data prefs. 2 E="+e2);
                        }
                    }*/
                    try {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            }

            //if (PPApplication.hardwareCheck(PPApplication.PREF_PROFILE_DEVICE_GPS, context))
            //{  No check only GPS
                if (profile._deviceLocationServicePrefs == 1)
                {
                    try {
                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            //}

            if (profile._deviceRunApplicationChange == 1)
            {
                Intent runApplicationsServiceIntent = new Intent(context, ExecuteRunApplicationsProfilePrefsService.class);
                runApplicationsServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                runApplicationsServiceIntent.putExtra(EXTRA_MERGED_PROFILE, merged);
                context.startService(runApplicationsServiceIntent);
            }
        }

//        throw new RuntimeException("test Crashlytics + TopExceptionHandler");
    }

    void setScreenTimeout(int screenTimeout, Context context) {
        PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "xxx");
        disableScreenTimeoutInternalChange = true;
        //Log.d("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        switch (screenTimeout) {
            case 1:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 15000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                break;
            case 2:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 30000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                break;
            case 3:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 60000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                break;
            case 4:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 120000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                break;
            case 5:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 600000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                break;
            case 6:
                //2147483647 = Integer.MAX_VALUE
                //18000000   = 5 hours
                //86400000   = 24 hours
                //43200000   = 12 hours
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 86400000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000); //18000000);
                break;
            case 7:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity != null)
                    PPApplication.screenTimeoutBeforeDeviceLock = 300000;
                else
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                break;
            case 8:
                screenTimeoutUnlock(context);
                if (PPApplication.lockDeviceActivity == null)
                    screenTimeoutLock(context);
                break;
        }
        setActivatedProfileScreenTimeout(context, 0);
        final Handler handler = new Handler(context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "disable screen timeout internal change");
                disableScreenTimeoutInternalChange = false;
            }
        }, 3000);
    }

    private static void screenTimeoutLock(Context context)
    {
        //Log.d("ActivateProfileHelper.screenTimeoutLock","xxx");
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        screenTimeoutUnlock(context);

        int type;
        if (android.os.Build.VERSION.SDK_INT < 25)
            //noinspection deprecation
            type = WindowManager.LayoutParams.TYPE_TOAST;
        else
        //if (android.os.Build.VERSION.SDK_INT < 26)
            //noinspection deprecation
            type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
        //else
        //    type = LayoutParams.TYPE_APPLICATION_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | /*WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |*/ WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        /*if (android.os.Build.VERSION.SDK_INT < 17)
            params.gravity = Gravity.RIGHT | Gravity.TOP;
        else
            params.gravity = Gravity.END | Gravity.TOP;*/
        GlobalGUIRoutines.keepScreenOnView = new BrightnessView(context);
        try {
            windowManager.addView(GlobalGUIRoutines.keepScreenOnView, params);
        } catch (Exception e) {
            GlobalGUIRoutines.keepScreenOnView = null;
            //e.printStackTrace();
        }
        //Log.d("ActivateProfileHelper.screenTimeoutLock","-- end");
    }

    static void screenTimeoutUnlock(Context context)
    {
        //Log.d("ActivateProfileHelper.screenTimeoutUnlock","xxx");

        if (GlobalGUIRoutines.keepScreenOnView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GlobalGUIRoutines.keepScreenOnView);
            } catch (Exception ignored) {
            }
            GlobalGUIRoutines.keepScreenOnView = null;
        }

        //Log.d("ActivateProfileHelper.screenTimeoutUnlock","-- end");
    }

    @SuppressLint("RtlHardcoded")
    private void createBrightnessView(Context context)
    {
        PPApplication.logE("ActivateProfileHelper.createBrightnessView", "xxx");

        //if (dataWrapper.context != null)
        //{

            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            if (GlobalGUIRoutines.brightnessView != null)
            {
                //Log.d("ActivateProfileHelper.createBrightnessView","GlobalGUIRoutines.brightnessView != null");
                try {
                    windowManager.removeView(GlobalGUIRoutines.brightnessView);
                } catch (Exception ignored) {
                }
                GlobalGUIRoutines.brightnessView = null;
            }
            int type;
            if (android.os.Build.VERSION.SDK_INT < 25)
                //noinspection deprecation
                type = WindowManager.LayoutParams.TYPE_TOAST;
            else
            //if (android.os.Build.VERSION.SDK_INT < 26)
                //noinspection deprecation
                type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            //else
            //    type = LayoutParams.TYPE_APPLICATION_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE /*| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE*/,
                        PixelFormat.TRANSLUCENT
                    );
            GlobalGUIRoutines.brightnessView = new BrightnessView(context);
            try {
                windowManager.addView(GlobalGUIRoutines.brightnessView, params);
            } catch (Exception e) {
                GlobalGUIRoutines.brightnessView = null;
                //e.printStackTrace();
            }

            final Handler handler = new Handler(context.getMainLooper());
            final Context _context = context;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("ActivateProfileHelper.createBrightnessView", "remove brightness view");

                    WindowManager windowManager = (WindowManager)_context.getSystemService(Context.WINDOW_SERVICE);
                    if (GlobalGUIRoutines.brightnessView != null)
                    {
                        try {
                            windowManager.removeView(GlobalGUIRoutines.brightnessView);
                        } catch (Exception ignored) {
                        }
                        GlobalGUIRoutines.brightnessView = null;
                    }
                }
            }, 5000);

        //Log.d("ActivateProfileHelper.createBrightnessView","-- end");

        //}
    }

    static void removeBrightnessView(Context context) {
        if (GlobalGUIRoutines.brightnessView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GlobalGUIRoutines.brightnessView);
            } catch (Exception ignored) {
            }
            GlobalGUIRoutines.brightnessView = null;
        }
    }

    @SuppressLint("NewApi")
    public void showNotification(Profile profile)
    {
        if (lockRefresh)
            // no refresh notification
            return;

        if (ApplicationPreferences.notificationStatusBar(context))
        {
            PPApplication.logE("ActivateProfileHelper.showNotification", "show");

            boolean notificationShowInStatusBar = ApplicationPreferences.notificationShowInStatusBar(context);
            boolean notificationStatusBarPermanent = ApplicationPreferences.notificationStatusBarPermanent(context);

            // close showed notification
            //notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);

            // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
            Intent intent = new Intent(context, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // nastavime, ze aktivita sa spusti z notifikacnej listy
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie intentu na restart events
            /*Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
            intentRE.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);*/
            Intent intentRE = new Intent(context, RestartEventsFromNotificationBroadcastReceiver.class);
            PendingIntent pIntentRE = PendingIntent.getBroadcast(context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);


            // vytvorenie samotnej notifikacie

            Notification.Builder notificationBuilder;

            RemoteViews contentView;
            if (ApplicationPreferences.notificationTheme(context).equals("1"))
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer_dark);
            else
            if (ApplicationPreferences.notificationTheme(context).equals("2"))
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer_light);
            else
                contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer);

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = dataWrapper.getProfileNameWithManualIndicator(profile, true, true, false);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                profileName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new Notification.Builder(context)
                    .setContentIntent(pIntent);

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
                    KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    //boolean screenUnlocked = !myKM.inKeyguardRestrictedInputMode();
                    boolean screenUnlocked = !myKM.isKeyguardLocked();
                    //boolean screenUnlocked = getScreenUnlocked(context);
                    if ((ApplicationPreferences.notificationHideInLockScreen(context) && (!screenUnlocked)) ||
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
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0")) {
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
                            notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                        }
                        else {
                            iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                            if (iconSmallResource == 0)
                                iconSmallResource = R.drawable.ic_profile_default;
                            notificationBuilder.setSmallIcon(iconSmallResource);
                        }
                    }
                    else {
                        // native icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);
                    }

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }
                else {
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0")) {
                        // colorful icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    } else {
                        // native icon
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        if (iconSmallResource == 0)
                            iconSmallResource = R.drawable.ic_profile_default_notify;
                        notificationBuilder.setSmallIcon(iconSmallResource);

                        int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                        if (iconLargeResource == 0)
                            iconLargeResource = R.drawable.ic_profile_default;
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
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
                    notificationBuilder.setSmallIcon(Icon.createWithBitmap(iconBitmap));
                }
                else {
                    int iconSmallResource;
                    if (ApplicationPreferences.notificationStatusBarStyle(context).equals("0"))
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

            if (ApplicationPreferences.notificationTextColor(context).equals("1")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
                if (Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.BLACK);
            }
            else
            if (ApplicationPreferences.notificationTextColor(context).equals("2")) {
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
                if (Build.VERSION.SDK_INT >= 24)
                    contentView.setTextColor(R.id.notification_activated_app_name, Color.WHITE);
            }
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
            //		ProfilePreferencesIndicator.paint(profile, context));
            if ((preferencesIndicator != null) && (ApplicationPreferences.notificationPrefIndicator(context)))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (ApplicationPreferences.notificationTextColor(context).equals("1"))
                contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, R.drawable.ic_action_events_restart);
            else
            if (ApplicationPreferences.notificationTextColor(context).equals("2"))
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

            try {
                PPApplication.phoneProfilesNotification = notificationBuilder.build();
            } catch (Exception e) {
                PPApplication.phoneProfilesNotification = null;
            }

            if (PPApplication.phoneProfilesNotification != null) {
                //if (Build.VERSION.SDK_INT < 26) {
                    if (notificationStatusBarPermanent) {
                        //notification.flags |= Notification.FLAG_NO_CLEAR;
                        PPApplication.phoneProfilesNotification.flags |= Notification.FLAG_ONGOING_EVENT;
                    } else {
                        setAlarmForNotificationCancel();
                    }
                //}

                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.startForeground(PPApplication.PROFILE_NOTIFICATION_ID, PPApplication.phoneProfilesNotification);
                else
                    notificationManager.notify(PPApplication.PROFILE_NOTIFICATION_ID, PPApplication.phoneProfilesNotification);
            }
        }
        else
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.stopForeground(true);
            else
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
        }
    }

    void removeNotification()
    {
        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.stopForeground(true);
        else
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
    }

    private void setAlarmForNotificationCancel()
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

    void updateWidget(boolean alsoEditor)
    {
        if (lockRefresh)
            // no refresh widgets
            return;

        // icon widget
        try {
            Intent intent = new Intent(context, IconWidgetProvider.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        } catch (Exception ignored) {}

        // one row widget
        try {
            Intent intent4 = new Intent(context, OneRowWidgetProvider.class);
            intent4.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids4[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
            intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
            context.sendBroadcast(intent4);
        } catch (Exception ignored) {}

        // list widget
        try {
            Intent intent2 = new Intent(context, ProfileListWidgetProvider.class);
            intent2.setAction(ProfileListWidgetProvider.INTENT_REFRESH_LISTWIDGET);
            int ids2[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
            context.sendBroadcast(intent2);
        } catch (Exception ignored) {}

        // dashclock extension
        LocalBroadcastManager.getInstance(context).registerReceiver(PPApplication.dashClockBroadcastReceiver, new IntentFilter("DashClockBroadcastReceiver"));
        Intent intent3 = new Intent("DashClockBroadcastReceiver");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
        LocalBroadcastManager.getInstance(context).registerReceiver(PPApplication.refreshGUIBroadcastReceiver, new IntentFilter("RefreshGUIBroadcastReceiver"));
        Intent intent5 = new Intent("RefreshGUIBroadcastReceiver");
        intent5.putExtra(RefreshGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                Intent intent2 = new Intent(context, SamsungEdgeProvider.class);
                intent2.setAction(SamsungEdgeProvider.INTENT_REFRESH_EDGEPANEL);
                context.sendBroadcast(intent2);
            } catch (Exception ignored) {
            }
        }
    }

    static boolean isAirplaneMode(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
        else
            //noinspection deprecation
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneMode(Context context, boolean mode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            setAirplaneMode_SDK17(/*context, */mode);
        else
            setAirplaneMode_SDK8(context, mode);
    }

    static boolean isMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT < 21)
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return (Boolean)getMobileDataEnabledMethod.invoke(connectivityManager);
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        if (android.os.Build.VERSION.SDK_INT < 22)
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;
            Object ITelephonyStub;
            Class<?> ITelephonyClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                getDataEnabledMethod = ITelephonyClass.getDeclaredMethod("getDataEnabled");

                getDataEnabledMethod.setAccessible(true);

                return (Boolean)getDataEnabledMethod.invoke(ITelephonyStub);

            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                getDataEnabledMethod.setAccessible(true);

                return (Boolean)getDataEnabledMethod.invoke(telephonyManager);

            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }

    }

    static boolean canSetMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 22)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                getDataEnabledMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        else
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void setMobileData(Context context, boolean enable)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
            // not working :-/
            if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
                if (android.os.Build.VERSION.SDK_INT == 21)
                {
                    Method dataConnSwitchMethod;
                    Class<?> telephonyManagerClass;
                    Object ITelephonyStub;
                    Class<?> ITelephonyClass;

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);

                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                        getITelephonyMethod.setAccessible(true);
                        ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                        ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
                        dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);

                        dataConnSwitchMethod.setAccessible(true);
                        dataConnSwitchMethod.invoke(ITelephonyStub, enable);

                    } catch (Exception ignored) {
                    }
                }
                else
                {
                    Method setDataEnabledMethod;
                    Class<?> telephonyManagerClass;

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);

                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        setDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                        setDataEnabledMethod.setAccessible(true);

                        setDataEnabledMethod.invoke(telephonyManager, enable);

                    } catch (Exception ignored) {
                    }
                }
            }
            else
            if (PPApplication.isRooted()/*PPApplication.isRootGranted()*/)
            {
                synchronized (PPApplication.startRootCommandMutex) {
                    String command1 = "svc data " + (enable ? "enable" : "disable");
                    PPApplication.logE("ActivateProfileHelper.setMobileData", "command=" + command1);
                    Command command = new Command(0, false, command1)/* {
                    @Override
                    public void commandOutput(int id, String line) {
                        super.commandOutput(id, line);
                        PPApplication.logE("ActivateProfileHelper.setMobileData","shell output="+line);
                    }

                    @Override
                    public void commandTerminated(int id, String reason) {
                        super.commandTerminated(id, reason);
                        PPApplication.logE("ActivateProfileHelper.setMobileData","terminated="+reason);
                    }

                    @Override
                    public void commandCompleted(int id, int exitcode) {
                        super.commandCompleted(id, exitcode);
                        PPApplication.logE("ActivateProfileHelper.setMobileData","completed="+exitcode);
                    }
                    }*/;
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                        commandWait(command);
                        //RootToolsSmall.runSuCommand(command1);
                        PPApplication.logE("ActivateProfileHelper.setMobileData", "after wait");
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                    }
                }

                /*
                int state = 0;
                try {
                    // Get the current state of the mobile network.
                    state = enable ? 1 : 0;
                    // Get the value of the "TRANSACTION_setDataEnabled" field.
                    String transactionCode = PPApplication.getTransactionCode(context, "TRANSACTION_setDataEnabled");
                    //Log.e("ActivateProfileHelper.setMobileData", "transactionCode="+transactionCode);
                    // Android 5.1+ (API 22) and later.
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        //Log.e("ActivateProfileHelper.setMobileData", "dual SIM?");
                        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                        // Loop through the subscription list i.e. SIM list.
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            if (transactionCode != null && transactionCode.length() > 0) {
                                // Get the active subscription ID for a given SIM card.
                                int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                                //Log.e("ActivateProfileHelper.setMobileData", "subscriptionId="+subscriptionId);
                                String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.closeAllShells();
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                                }
                            }
                        }
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        //Log.e("ActivateProfileHelper.setMobileData", "NO dual SIM?");
                        // Android 5.0 (API 21) only.
                        if (transactionCode != null && transactionCode.length() > 0) {
                            String command1 = "service call phone " + transactionCode + " i32 " + state;
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                */
            }
        }
        else
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            boolean OK = false;
            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Field iConnectivityManagerField = connectivityManagerClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);
                final Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
                final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                setMobileDataEnabledMethod.setAccessible(true);

                setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);

                OK = true;

            } catch (Exception ignored) {
            }

            if (!OK)
            {
                try {
                    Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);

                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(connectivityManager, enable);

                    //OK = true;

                } catch (Exception ignored) {
                }
            }
        }
    }

    /*
    private int getPreferredNetworkType(Context context) {
        if (PPApplication.isRooted())
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = PPApplication.getTransactionCode(context, "TRANSACTION_getPreferredNetworkType");
                if (transactionCode != null && transactionCode.length() > 0) {
                    String command1 = "service call phone " + transactionCode + " i32";
                    Command command = new Command(0, false, command1) {
                        @Override
                        public void commandOutput(int id, String line) {
                            super.commandOutput(id, line);
                            String splits[] = line.split(" ");
                            try {
                                networkType = Integer.parseInt(splits[2]);
                            } catch (Exception e) {
                                networkType = -1;
                            }
                        }

                        @Override
                        public void commandTerminated(int id, String reason) {
                            super.commandTerminated(id, reason);
                        }

                        @Override
                        public void commandCompleted(int id, int exitcode) {
                            super.commandCompleted(id, exitcode);
                        }
                    };
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        else
            networkType = -1;
        return networkType;
    }
    */

    private static String getTransactionCode(Context context, String fieldName) throws Exception {
        //try {
        final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
        final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
        mTelephonyMethod.setAccessible(true);
        final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
        final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
        final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
        final Field field = mClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return String.valueOf(field.getInt(null));
        //} catch (Exception e) {
        // The "TRANSACTION_setDataEnabled" field is not available,
        // or named differently in the current API level, so we throw
        // an exception and inform users that the method is not available.
        //e.printStackTrace();
        //    throw e;
        //}
    }

    /*static public String getTransactionCode(String className, String methodName) throws Exception {
        //try {
        final String stubName = className + "$Stub";
        final String fieldName = "TRANSACTION_" + methodName;

        final Class<?> cls = Class.forName(stubName);
        final Field declaredField = cls.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return String.valueOf(declaredField.getInt(cls));
        //} catch (Exception e) {
        // The "TRANSACTION_setDataEnabled" field is not available,
        // or named differently in the current API level, so we throw
        // an exception and inform users that the method is not available.
        //e.printStackTrace();
        //    throw e;
        //}
    }*/

    static boolean telephonyServiceExists(Context context, String preference) {
        try {
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                getTransactionCode(context, "TRANSACTION_setDataEnabled");
            }
            else
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private void setPreferredNetworkType(Context context, int networkType)
    {
        if (PPApplication.isRooted() && PPApplication.serviceBinaryExists())
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
                // Android 6?
                if (Build.VERSION.SDK_INT >= 23) {
                    SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                    // Loop through the subscription list i.e. SIM list.
                    List<SubscriptionInfo> subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    if (subscriptionList != null) {
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            if (transactionCode.length() > 0) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                    synchronized (PPApplication.startRootCommandMutex) {
                                        String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + networkType;
                                        Command command = new Command(0, false, command1);
                                        try {
                                            //RootTools.closeAllShells();
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            commandWait(command);
                                        } catch (Exception e) {
                                            Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else  {
                    if (transactionCode.length() > 0) {
                        synchronized (PPApplication.startRootCommandMutex) {
                            String command1 = "service call phone " + transactionCode + " i32 " + networkType;
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                            }
                        }
                    }
                }
            } catch(Exception ignored) {
            }
        }
    }

    private void setNFC(Context context, boolean enable)
    {
        /*
        Not working in debug version of application !!!!
        Test with release version.
        */

        //Log.e("ActivateProfileHelper.setNFC", "xxx");
        /*if (Permissions.checkNFC(context)) {
            Log.e("ActivateProfileHelper.setNFC", "permission granted!!");
            CmdNfc.run(enable);
        }
        else */
        if (PPApplication.isRooted()/*PPApplication.isRootGranted()*/) {
            synchronized (PPApplication.startRootCommandMutex) {
                String command1 = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                //Log.e("ActivateProfileHelper.setNFC", "command1="+command1);
                if (command1 != null) {
                    Command command = new Command(0, false, command1);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setNFC", "Error on run su");
                    }
                }
                //String command = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                //if (command != null)
                //  RootToolsSmall.runSuCommand(command);
            }
        }
    }

    static boolean canExploitGPS(Context context)
    {
        // test expoiting power manager widget
        PackageManager pacMan = context.getPackageManager();
        try {
            PackageInfo pacInfo = pacMan.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);

            if(pacInfo != null){
                for(ActivityInfo actInfo : pacInfo.receivers){
                    //test if receiver is exported. if so, we can toggle GPS.
                    if(actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported){
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false; //package not found
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void setGPS(Context context, boolean enable)
    {
        //boolean isEnabled;
        //int locationMode = -1;
        //if (android.os.Build.VERSION.SDK_INT < 19)
        //    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        /*else {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, -1);
            isEnabled = (locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) ||
                        (locationMode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        }*/

        boolean isEnabled;
        if (android.os.Build.VERSION.SDK_INT < 19)
            isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        PPApplication.logE("ActivateProfileHelper.setGPS", "isEnabled="+isEnabled);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled) && enable)
        {
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                String newSet;
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (provider.equals(""))
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);
                }
                else
                    newSet = "+gps";
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
            }
            else
            if (PPApplication.isRooted() && PPApplication.settingsBinaryExists())
            {
                // zariadenie je rootnute
                PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);

                    String newSet;
                    if (provider.isEmpty())
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);
                    PPApplication.logE("ActivateProfileHelper.setGPS", "newSet="+newSet);

                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);

                        //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state true";
                        Command command = new Command(0, false, command1); //, command2);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            //Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                            PPApplication.logE("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
                else {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);

                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed +gps";
                        Command command = new Command(0, false, command1);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            PPApplication.logE("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
            }
            else
            if (canExploitGPS(context))
            {
                PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            //else
            //{
                /*PPApplication.logE("ActivateProfileHelper.setGPS", "old method");

                try {
                    Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                    intent.putExtra("enabled", enable);
                    context.sendBroadcast(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }*/

                // for normal apps it is only possible to open the system settings dialog
            /*	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); */
            //}
        }
        else
        //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
        if (isEnabled && (!enable))
        {
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                String newSet = "";
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    String[] list = provider.split(",");
                    int j = 0;
                    for (String aList : list) {
                        if (!aList.equals(LocationManager.GPS_PROVIDER)) {
                            if (j > 0)
                                newSet += ",";
                            newSet += aList;
                            j++;
                        }
                    }
                }
                else
                    newSet = "-gps";
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
            }
            else
            if (PPApplication.isRooted() && PPApplication.settingsBinaryExists())
            {
                // zariadenie je rootnute
                PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);

                    String[] list = provider.split(",");

                    String newSet = "";
                    int j = 0;
                    for (String aList : list) {

                        if (!aList.equals(LocationManager.GPS_PROVIDER)) {
                            if (j > 0)
                                newSet += ",";
                            newSet += aList;
                            j++;
                        }
                    }
                    PPApplication.logE("ActivateProfileHelper.setGPS", "newSet="+newSet);

                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                        //if (PPApplication.isSELinuxEnforcing())
                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                        //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state false";
                        Command command = new Command(0, false, command1);//, command2);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            //Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                            PPApplication.logE("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
                else {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);

                    synchronized (PPApplication.startRootCommandMutex) {
                        command1 = "settings put secure location_providers_allowed -gps";
                        Command command = new Command(0, false, command1);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            PPApplication.logE("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                        }
                    }
                }
            }
            else
            if (canExploitGPS(context))
            {
                PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            //else
            //{
                //PPApplication.logE("ActivateProfileHelper.setGPS", "old method");

                /*try {
                    Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                    intent.putExtra("enabled", enable);
                    context.sendBroadcast(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }*/

                // for normal apps it is only possible to open the system settings dialog
            /*	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); */
            //}
        }	    	
    }

    private void setAirplaneMode_SDK17(/*Context context, */boolean mode)
    {
        if (PPApplication.isRooted() && PPApplication.settingsBinaryExists())
        {
            // zariadenie je rootnute
            synchronized (PPApplication.startRootCommandMutex) {
                String command1;
                String command2;
                if (mode) {
                    command1 = "settings put global airplane_mode_on 1";
                    command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
                } else {
                    command1 = "settings put global airplane_mode_on 0";
                    command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
                }
                //if (PPApplication.isSELinuxEnforcing())
                //{
                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                //	command2 = PPApplication.getSELinuxEnforceCommand(command2, Shell.ShellContext.SYSTEM_APP);
                //}
                Command command = new Command(0, false, command1, command2);
                try {
                    //RootTools.closeAllShells();
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                } catch (Exception e) {
                    Log.e("AirPlaneMode_SDK17.setAirplaneMode", "Error on run su");
                }
            }
        }
        //else
        //{
            // for normal apps it is only possible to open the system settings dialog
        /*	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent); */
        //}
    }

    @SuppressWarnings("deprecation")
    private void setAirplaneMode_SDK8(Context context, boolean mode)
    {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }

    void setPowerSaveMode(Profile profile) {
        if (profile._devicePowerSaveMode != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context) == PPApplication.PREFERENCE_ALLOWED) {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean _isPowerSaveMode = false;
                if (Build.VERSION.SDK_INT >= 21)
                    _isPowerSaveMode = powerManager.isPowerSaveMode();
                boolean _setPowerSaveMode = false;
                switch (profile._devicePowerSaveMode) {
                    case 1:
                        if (!_isPowerSaveMode) {
                            _isPowerSaveMode = true;
                            _setPowerSaveMode = true;
                        }
                        break;
                    case 2:
                        if (_isPowerSaveMode) {
                            _isPowerSaveMode = false;
                            _setPowerSaveMode = true;
                        }
                        break;
                    case 3:
                        _isPowerSaveMode = !_isPowerSaveMode;
                        _setPowerSaveMode = true;
                        break;
                }
                if (_setPowerSaveMode) {
                    if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                        if (android.os.Build.VERSION.SDK_INT >= 21)
                            Settings.Global.putInt(context.getContentResolver(), "low_power", ((_isPowerSaveMode) ? 1 : 0));
                    }
                    else
                    if (PPApplication.isRooted() && PPApplication.settingsBinaryExists()) {
                        synchronized (PPApplication.startRootCommandMutex) {
                            String command1 = "settings put global low_power " + ((_isPowerSaveMode) ? 1 : 0);
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setPowerSaveMode", "Error on run su: " + e.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    void lockDevice(Profile profile) {
        if (PPApplication.startedOnBoot)
            // not lock device after boot
            return;

        switch (profile._lockDevice) {
            case 3:
                DevicePolicyManager manager = (DevicePolicyManager)context.getSystemService(DEVICE_POLICY_SERVICE);
                final ComponentName component = new ComponentName(context, PPDeviceAdminReceiver.class);
                if (manager.isAdminActive(component))
                    manager.lockNow();
                break;
            case 2:
                /*if (PPApplication.isRooted()) {
                    //String command1 = "input keyevent 26";
                    Command command = new Command(0, false, command1);
                    try {
                        //RootTools.closeAllShells();
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.lockDevice", "Error on run su: " + e.toString());
                    }
                }*/
                if (PPApplication.isRooted())
                {
                    synchronized (PPApplication.startRootCommandMutex) {
                        String command1 = PPApplication.getJavaCommandFile(CmdGoToSleep.class, "power", context, 0);
                        if (command1 != null) {
                            Command command = new Command(0, false, command1);
                            try {
                                //RootTools.closeAllShells();
                                RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                                commandWait(command);
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.lockDevice", "Error on run su");
                            }
                        }
                    }
                }
                /*if (PPApplication.isRooted() && PPApplication.serviceBinaryExists()) {
                    try {
                        // Get the value of the "TRANSACTION_goToSleep" field.
                        String transactionCode = PPApplication.getTransactionCode("android.os.IPowerManager", "TRANSACTION_goToSleep");
                        String command1 = "service call power " + transactionCode + " i64 " + SystemClock.uptimeMillis();
                        Command command = new Command(0, false, command1);
                        try {
                            //RootTools.closeAllShells();
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.lockDevice", "Error on run su");
                        }
                    } catch(Exception ignored) {
                    }
                */
                break;
            case 1:
                if (Permissions.checkLockDevice(context) && (PPApplication.lockDeviceActivity == null)) {
                    Intent intent = new Intent(context, LockDeviceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    context.startActivity(intent);
                }
                break;
        }
    }

    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("ActivateProfileHelper", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }


    static int getRingerVolume(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_RINGER_VOLUME, -999);
    }

    static void setRingerVolume(Context context, int volume)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_RINGER_VOLUME, volume);
        editor.apply();
    }

    static int getNotificationVolume(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_NOTIFICATION_VOLUME, -999);
    }

    static void setNotificationVolume(Context context, int volume)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
        editor.apply();
    }

    static int getRingerMode(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_RINGER_MODE, 0);
    }

    static void setRingerMode(Context context, int mode)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_RINGER_MODE, mode);
        editor.apply();
    }

    static int getZenMode(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_ZEN_MODE, 0);
    }

    static void setZenMode(Context context, int mode)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_ZEN_MODE, mode);
        editor.apply();
    }

    static boolean getLockScreenDisabled(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_LOCKSCREEN_DISABLED, false);
    }

    static void setLockScreenDisabled(Context context, boolean disabled)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_LOCKSCREEN_DISABLED, disabled);
        editor.apply();
    }

    /*
    private static boolean getScreenUnlocked(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SCREEN_UNLOCKED, true);
    }

    static void setScreenUnlocked(Context context, boolean unlocked)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SCREEN_UNLOCKED, unlocked);
        editor.apply();
    }
    */

    static int getActivatedProfileScreenTimeout(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, 0);
    }

    static void setActivatedProfileScreenTimeout(Context context, int timeout)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, timeout);
        editor.apply();
    }

}
