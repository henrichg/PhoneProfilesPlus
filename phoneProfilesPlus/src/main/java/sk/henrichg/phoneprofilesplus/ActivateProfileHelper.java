package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.DEFAULT_VIBRATE;

class ActivateProfileHelper {

    static boolean disableScreenTimeoutInternalChange = false;
    static boolean brightnessDialogInternalChange = false;

    // bluetooth calls volume stream
    static final int STREAM_BLUETOOTH_SCO = 6;

    //static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    static final int ZENMODE_ALL = 0;
    static final int ZENMODE_PRIORITY = 1;
    static final int ZENMODE_NONE = 2;
    static final int ZENMODE_ALARMS = 3;
    @SuppressWarnings("WeakerAccess")
    static final int ZENMODE_SILENT = 99;

    //static final String EXTRA_MERGED_PROFILE = "merged_profile";
    //static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";

    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT = "activated_profile_screen_timeout";
    static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";

    private static void doExecuteForRadios(Context context, Profile profile)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "profile=" + profile);
            if (profile != null)
                PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "profile._name=" + profile._name);
        }*/

        if (profile == null)
            return;

        PPApplication.sleep(300);

        Context appContext = context.getApplicationContext();

        // setup network type
        // in array.xml, networkTypeGSMValues are 100+ values
        if (profile._deviceNetworkType >= 100) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceNetworkType");
                // in array.xml, networkTypeGSMValues are 100+ values
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setPreferredNetworkType()");
                setPreferredNetworkType(appContext, profile._deviceNetworkType - 100);
                PPApplication.sleep(200);
            }
        }

        // setup mobile data
        if (profile._deviceMobileData != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileData");
                boolean _isMobileData = isMobileData(appContext);
                //PPApplication.logE("ActivateProfileHelper.doExecuteForRadios","_isMobileData="+_isMobileData);
                boolean _setMobileData = false;
                switch (profile._deviceMobileData) {
                    case 1:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileData 1");
                        if (!_isMobileData) {
                            _isMobileData = true;
                            _setMobileData = true;
                        }
                        break;
                    case 2:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileData 2");
                        if (_isMobileData) {
                            _isMobileData = false;
                            _setMobileData = true;
                        }
                        break;
                    case 3:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileData 3");
                        _isMobileData = !_isMobileData;
                        _setMobileData = true;
                        break;
                }
                if (_setMobileData) {
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setMobileData()");
                    setMobileData(appContext, _isMobileData);
                    PPApplication.sleep(200);
                }
            }
            //else
            //    PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "_deviceMobileData NOT ALLOWED");
        }

        // setup WiFi AP
        boolean canChangeWifi = true;
        if (profile._deviceWiFiAP != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP");
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(appContext);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    if (wifiApManager != null) {
                        boolean setWifiAPState = false;
                        boolean doNotChangeWifi = false;
                        boolean isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                        switch (profile._deviceWiFiAP) {
                            case 1:
                            case 4:
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 1");
                                if (!isWifiAPEnabled) {
                                    isWifiAPEnabled = true;
                                    setWifiAPState = true;
                                    doNotChangeWifi = profile._deviceWiFiAP == 4;
                                    canChangeWifi = profile._deviceWiFiAP == 4;
                                }
                                break;
                            case 2:
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 2");
                                if (isWifiAPEnabled) {
                                    isWifiAPEnabled = false;
                                    setWifiAPState = true;
                                    canChangeWifi = true;
                                }
                                break;
                            case 3:
                            case 5:
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 3");
                                isWifiAPEnabled = !isWifiAPEnabled;
                                setWifiAPState = true;
                                doNotChangeWifi = profile._deviceWiFiAP == 5;
                                if (doNotChangeWifi)
                                    canChangeWifi = true;
                                else
                                    canChangeWifi = !isWifiAPEnabled;
                                break;
                        }
                        if (setWifiAPState) {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setWifiAP()");
                            setWifiAP(wifiApManager, isWifiAPEnabled, doNotChangeWifi, appContext);
                            PPApplication.sleep(3000);
                        }
                    }
                }
                else {
                    boolean setWifiAPState = false;
                    boolean doNotChangeWifi = false;
                    boolean isWifiAPEnabled = CmdWifiAP.isEnabled();
                    switch (profile._deviceWiFiAP) {
                        case 1:
                        case 4:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 1");
                            if (!isWifiAPEnabled) {
                                isWifiAPEnabled = true;
                                setWifiAPState = true;
                                doNotChangeWifi = profile._deviceWiFiAP == 4;
                                canChangeWifi = profile._deviceWiFiAP == 4;
                            }
                            break;
                        case 2:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 2");
                            if (isWifiAPEnabled) {
                                isWifiAPEnabled = false;
                                setWifiAPState = true;
                                canChangeWifi = true;
                            }
                            break;
                        case 3:
                        case 5:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFiAP 3");
                            isWifiAPEnabled = !isWifiAPEnabled;
                            setWifiAPState = true;
                            doNotChangeWifi = profile._deviceWiFiAP == 5;
                            if (doNotChangeWifi)
                                canChangeWifi = true;
                            else
                                canChangeWifi = !isWifiAPEnabled;
                            break;
                    }
                    if (setWifiAPState) {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "CmdWifiAP.setWifiAP()");
                        CmdWifiAP.setWifiAP(isWifiAPEnabled, doNotChangeWifi);
                        PPApplication.sleep(1000);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // setup Wi-Fi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFi");
                    boolean isWifiAPEnabled;
                    if (Build.VERSION.SDK_INT < 28)
                        isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
                    else
                        isWifiAPEnabled = CmdWifiAP.isEnabled();
                    //PPApplication.logE("[WIFI] ActivateProfileHelper.doExecuteForRadios", "isWifiAPEnabled="+isWifiAPEnabled);
                    if ((!isWifiAPEnabled) || (profile._deviceWiFi >= 4)) { // only when wifi AP is not enabled, change wifi
                        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            int wifiState = wifiManager.getWifiState();
                            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                            boolean setWifiState = false;
                            switch (profile._deviceWiFi) {
                                case 1:
                                case 4:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFi 1,4");
                                    if (!isWifiEnabled) {
                                        isWifiEnabled = true;
                                        setWifiState = true;
                                    }
                                    break;
                                case 2:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFi 2");
                                    if (isWifiEnabled) {
                                        isWifiEnabled = false;
                                        setWifiState = true;
                                    }
                                    break;
                                case 3:
                                case 5:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceWiFi 3,5");
                                    isWifiEnabled = !isWifiEnabled;
                                    setWifiState = true;
                                    break;
                            }
                            //PPApplication.logE("[WIFI] ActivateProfileHelper.doExecuteForRadios", "isWifiEnabled="+isWifiEnabled);
                            if (isWifiEnabled) {
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setWifiEnabledForScan()");
                                // when wifi is enabled from profile, no disable wifi after scan
                                WifiScanWorker.setWifiEnabledForScan(appContext, false);
                            }
                            if (setWifiState) {
                                try {
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setWifiEnabled()");
                                    //PPApplication.logE("#### setWifiEnabled", "from ActivateProfileHelper.doExecuteForRadio");
                                    //if (Build.VERSION.SDK_INT >= 29)
                                    //    CmdWifi.setWifi(isWifiEnabled);
                                    //else
                                        wifiManager.setWifiEnabled(isWifiEnabled);
                                        //CmdWifi.setWifiEnabled(isWifiAPEnabled);
                                } catch (Exception e) {
                                    //WTF?: DOOGEE- X5pro - java.lang.SecurityException: Permission Denial: Enable WiFi requires com.mediatek.permission.CTA_ENABLE_WIFI
                                    //Log.e("ActivateProfileHelper.doExecuteForRadios", Log.getStackTraceString(e));
                                    //PPApplication.recordException(e);;
                                }
                                PPApplication.sleep(200);
                            }
                        }
                    }
                }
            }

            // connect to SSID
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceConnectToSSID");
                    if (Permissions.checkLocation(appContext)) {
                        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            int wifiState = wifiManager.getWifiState();
                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                                // check if wifi is connected
                                ConnectivityManager connManager = null;
                                try {
                                    connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                                } catch (Exception e) {
                                    // java.lang.NullPointerException: missing IConnectivityManager
                                    // Dual SIM?? Bug in Android ???
                                    PPApplication.recordException(e);
                                }
                                if (connManager != null) {
                                    boolean wifiConnected = false;
                                    /*if (Build.VERSION.SDK_INT < 28) {
                                        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
                                        wifiConnected = (activeNetwork != null) &&
                                                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                                                activeNetwork.isConnected();
                                    }
                                    else*/ {
                                        Network[] activeNetworks=connManager.getAllNetworks();
                                        for(Network network : activeNetworks){
                                            try {
                                                //NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                                //if ((networkInfo != null) && networkInfo.isConnected()) {
                                                    NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                    if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                        wifiConnected = WifiNetworkCallback.connected;
                                                        break;
                                                    }
                                                //}
                                            } catch (Exception ee) {
                                                PPApplication.recordException(ee);
                                            }
                                        }
                                    }
                                    WifiInfo wifiInfo = null;
                                    if (wifiConnected)
                                        wifiInfo = wifiManager.getConnectionInfo();

                                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                    if (list != null) {
                                        for (WifiConfiguration i : list) {
                                            if (i.SSID != null && i.SSID.equals(profile._deviceConnectToSSID)) {
                                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceConnectToSSID wifiConnected="+wifiConnected);
                                                if (wifiConnected) {
                                                    if (!wifiInfo.getSSID().equals(i.SSID)) {

                                                        if (PhoneProfilesService.getInstance() != null)
                                                            PhoneProfilesService.getInstance().connectToSSIDStarted = true;

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
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().connectToSSID = profile._deviceConnectToSSID;
            }
        }

        // setup bluetooth
        if (profile._deviceBluetooth != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceBluetooth");
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetoothAdapter != null) {
                    boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();
                    boolean setBluetoothState = false;
                    switch (profile._deviceBluetooth) {
                        case 1:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceBluetooth 1");
                            if (!isBluetoothEnabled) {
                                isBluetoothEnabled = true;
                                setBluetoothState = true;
                            }
                            break;
                        case 2:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceBluetooth 2");
                            if (isBluetoothEnabled) {
                                isBluetoothEnabled = false;
                                setBluetoothState = true;
                            }
                            break;
                        case 3:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceBluetooth 3");
                            isBluetoothEnabled = !isBluetoothEnabled;
                            setBluetoothState = true;
                            break;
                    }
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "setBluetoothState=" + setBluetoothState);
                        PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isBluetoothEnabled=" + isBluetoothEnabled);
                    }*/
                    if (isBluetoothEnabled) {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "setBluetoothEnabledForScan()");
                        // when bluetooth is enabled from profile, no disable bluetooth after scan
                        //PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isBluetoothEnabled=true; setBluetoothEnabledForScan=false");
                        BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
                    }
                    if (setBluetoothState) {
                        try {
                            //if (Build.VERSION.SDK_INT >= 26)
                            //    CmdBluetooth.setBluetooth(isBluetoothEnabled);
                            //else {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "enable/disable bluetooth adapter");
                                if (isBluetoothEnabled)
                                    bluetoothAdapter.enable();
                                else
                                    bluetoothAdapter.disable();
                            //}
                        } catch (Exception e) {
                            // WTF?: DOOGEE - X5pro -> java.lang.SecurityException: Permission Denial: Enable bluetooth requires com.mediatek.permission.CTA_ENABLE_BT
                            //Log.e("ActivateProfileHelper.doExecuteForRadio", Log.getStackTraceString(e));
                            //PPApplication.recordException(e);;
                        }
                    }
                }
            }
        }

        // setup location mode
        if (profile._deviceLocationMode != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceLocationMode");

                switch (profile._deviceLocationMode) {
                    case 1:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceLocationMode 1");
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        break;
                    case 2:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceLocationMode 2");
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
                        break;
                    case 3:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceLocationMode 3");
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
                        break;
                    case 4:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceLocationMode 4");
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                        break;
                }
            }
        }

        // setup GPS
        if (profile._deviceGPS != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceGPS");
                //String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                boolean isEnabled = false;
                boolean ok = true;
                /*if (android.os.Build.VERSION.SDK_INT < 19)
                    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {*/
                    LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null)
                        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    else
                        ok = false;
                //}
                if (ok) {
                    //PPApplication.logE("ActivateProfileHelper.doExecuteForRadios", "isEnabled=" + isEnabled);
                    switch (profile._deviceGPS) {
                        case 1:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceGPS 1");
                            setGPS(appContext, true);
                            //setLocationMode(appContext, true);
                            break;
                        case 2:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceGPS 2");
                            setGPS(appContext, false);
                            //setLocationMode(appContext, false);
                            break;
                        case 3:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceGPS 3");
                            if (!isEnabled) {
                                setGPS(appContext, true);
                                //setLocationMode(appContext, true);
                            } else {
                                setGPS(appContext, false);
                                //setLocationMode(appContext, false);
                            }
                            break;
                    }
                }
            }
        }

        // setup NFC
        if (profile._deviceNFC != 0) {
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceNFC");
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(appContext);
                if (nfcAdapter != null) {
                    switch (profile._deviceNFC) {
                        case 1:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceNFC 1");
                            setNFC(appContext, true);
                            break;
                        case 2:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceNFC 2");
                            setNFC(appContext, false);
                            break;
                        case 3:
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.doExecuteForRadios", "_deviceNFC 3");
                            if (!nfcAdapter.isEnabled()) {
                                setNFC(appContext, true);
                            } else {
                                setNFC(appContext, false);
                            }
                            break;
                    }
                }
            }
        }

    }

    private static void executeForRadios(final Profile profile, Context context)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "profile=" + profile);
            if (profile != null)
                PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "profile._name=" + profile._name);
        }*/

        if (profile == null)
            return;

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadRadios();
        final Handler handler = new Handler(PPApplication.handlerThreadRadios.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForRadios");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadRadios", "START run - from=ActivateProfileHelper.executeForRadios");

                    boolean _isAirplaneMode = false;
                    boolean _setAirplaneMode = false;
                    if (profile._deviceAirplaneMode != 0) {
                        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            _isAirplaneMode = isAirplaneMode(appContext);
                            switch (profile._deviceAirplaneMode) {
                                case 1:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "_deviceAirplaneMode 1");
                                    if (!_isAirplaneMode) {
                                        _isAirplaneMode = true;
                                        _setAirplaneMode = true;
                                    }
                                    break;
                                case 2:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "_deviceAirplaneMode 2");
                                    if (_isAirplaneMode) {
                                        _isAirplaneMode = false;
                                        _setAirplaneMode = true;
                                    }
                                    break;
                                case 3:
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "_deviceAirplaneMode 3");
                                    _isAirplaneMode = !_isAirplaneMode;
                                    _setAirplaneMode = true;
                                    break;
                            }
                        }
                    }
                    if (_setAirplaneMode /*&& _isAirplaneMode*/) {
                        // switch ON airplane mode, set it before doExecuteForRadios
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "setAirplaneMode()");
                        setAirplaneMode(/*context,*/ _isAirplaneMode);
                        PPApplication.sleep(2500);
                        //PPApplication.logE("ActivateProfileHelper.executeForRadios", "after sleep");
                    }
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "doExecuteForRadios()");
                    doExecuteForRadios(appContext, profile);

                    /*if (_setAirplaneMode && (!_isAirplaneMode)) {
                        // 200 milliseconds is in doExecuteForRadios
                        PPApplication.sleep(1800);

                        // switch OFF airplane mode, set if after executeForRadios
                        setAirplaneMode(context, _isAirplaneMode);
                    }*/

                    //PPApplication.sleep(500);

                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForRadios", "end");
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

    static boolean isAudibleRinging(int ringerMode, int zenMode/*, boolean onlyVibrateSilent*/) {
        /*if (onlyVibrateSilent)
            return (!((ringerMode == Profile.RINGERMODE_VIBRATE) || (ringerMode == Profile.RINGERMODE_SILENT) ||
                    ((ringerMode == Profile.RINGERMODE_ZENMODE) &&
                            ((zenMode == Profile.ZENMODE_ALL_AND_VIBRATE) || (zenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE)))
            ));
        else*/
            return (!((ringerMode == Profile.RINGERMODE_VIBRATE) || (ringerMode == Profile.RINGERMODE_SILENT) ||
                      ((ringerMode == Profile.RINGERMODE_ZENMODE) &&
                              ((zenMode == Profile.ZENMODE_NONE) || (zenMode == Profile.ZENMODE_ALL_AND_VIBRATE) ||
                               (zenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE) || (zenMode == Profile.ZENMODE_ALARMS)))
                     ));
    }

    private static boolean isVibrateRingerMode(int ringerMode/*, int zenMode*/) {
        return (ringerMode == Profile.RINGERMODE_VIBRATE);
    }


    static boolean isAudibleSystemRingerMode(AudioManager audioManager, int systemZenMode/*, Context context*/) {
        /*int ringerMode = audioManager.getRingerMode();
        PPApplication.logE("ActivateProfileHelper.isAudibleSystemRingerMode", "ringerMode="+ringerMode);
        if (ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                int zenMode = getSystemZenMode(context, -1);
                return (zenMode == ActivateProfileHelper.ZENMODE_PRIORITY);
            }
            else
                return false;
        }
        else
            return true;*/

        int systemRingerMode = audioManager.getRingerMode();
        //int systemZenMode = getSystemZenMode(context/*, -1*/);

        /*return (audibleRingerMode) ||
                ((systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY) &&
                        (systemRingerMode != AudioManager.RINGER_MODE_VIBRATE));*/

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.isAudibleSystemRingerMode", "systemRingerMode=" + systemRingerMode);
            PPApplication.logE("ActivateProfileHelper.isAudibleSystemRingerMode", "systemZenMode=" + systemZenMode);
        }*/

        boolean audibleRingerMode;
        // In AOSP, when systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY, systemRingerMode == AudioManager.RINGER_MODE_SILENT :-/
        if (systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY) {
            audibleRingerMode = (systemRingerMode == AudioManager.RINGER_MODE_NORMAL) ||
                                    (systemRingerMode == AudioManager.RINGER_MODE_SILENT);
        }
        else
            audibleRingerMode = systemRingerMode == AudioManager.RINGER_MODE_NORMAL;
        boolean audibleZenMode = (systemZenMode == -1) ||
                                 (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) ||
                                 (systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY);
        return audibleRingerMode && audibleZenMode;
    }

    /*
    private static void correctVolume0(AudioManager audioManager) {
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

    static void getMergedRingNotificationVolumes(Context context) {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefMergedRingNotificationVolumes =
                    ApplicationPreferences.getSharedPreferences(context).getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
        }
    }
    static boolean getMergedRingNotificationVolumes() {
        //PPApplication.logE("ActivateProfileHelper.getMergedRingNotificationVolumes", "force set="+ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context));
        //PPApplication.logE("ActivateProfileHelper.getMergedRingNotificationVolumes", "merged="+ApplicationPreferences.preferences.getBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, true));
        if (ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes > 0)
            return ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes == 1;
        else
            return ApplicationPreferences.prefMergedRingNotificationVolumes;
    }
    // test if ring and notification volumes are merged
    static void setMergedRingNotificationVolumes(Context context/*, boolean force*/) {
        synchronized (PPApplication.profileActivationMutex) {
            //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            setMergedRingNotificationVolumes(context, /*force,*/ editor);
            editor.apply();
        }
    }
    static void setMergedRingNotificationVolumes(Context context, /*boolean force,*/ SharedPreferences.Editor editor) {
        synchronized (PPApplication.profileActivationMutex) {

            //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "xxx");

            Context appContext = context.getApplicationContext();
            //if (!ApplicationPreferences.getSharedPreferences(appContext).contains(PREF_MERGED_RING_NOTIFICATION_VOLUMES) || force) {
                try {
                    boolean merged;
                    AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        //RingerModeChangeReceiver.internalChange = true;

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
                            PPApplication.sleep(2000);
                            merged = audioManager.getStreamVolume(AudioManager.STREAM_RING) == newNotificationVolume;
                        } else
                            merged = false;
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, 0);
                        audioManager.setRingerMode(ringerMode);

                        //PPApplication.logE("ActivateProfileHelper.setMergedRingNotificationVolumes", "merged=" + merged);

                        editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                        ApplicationPreferences.prefMergedRingNotificationVolumes = merged;
                    }
                } catch (Exception e) {
                    //PPApplication.recordException(e);
                }
            //}
        }
    }

    @SuppressLint("NewApi")
    private static void setVolumes(Context context, Profile profile, AudioManager audioManager, int systemZenMode,
                                   int linkUnlink, boolean forProfileActivation, boolean forRingerMode)
    {
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile=" + profile);
        if (profile == null)
            return;
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile._name=" + profile._name);
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "linkUnlink=" + linkUnlink);
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "forProfileActivation=" + forProfileActivation);
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "forRingerMode=" + forRingerMode);

        Context appContext = context.getApplicationContext();

        int ringerMode = ApplicationPreferences.prefRingerMode;
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "ringerMode=" + ringerMode);

        //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile._volumeMuteSound=" + profile._volumeMuteSound);

        if (profile._volumeMuteSound) {
            if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/) || (ringerMode == 0)) {
                if (!audioManager.isStreamMute(AudioManager.STREAM_RING))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                if (!audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
                if (!audioManager.isStreamMute(AudioManager.STREAM_SYSTEM))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
                if (!audioManager.isStreamMute(AudioManager.STREAM_DTMF))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_MUTE, 0);
            }
            if (!audioManager.isStreamMute(AudioManager.STREAM_MUSIC))
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        }
        else {
            if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/) || (ringerMode == 0)) {
                if (audioManager.isStreamMute(AudioManager.STREAM_RING))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
                if (audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
                if (audioManager.isStreamMute(AudioManager.STREAM_SYSTEM))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
                if (audioManager.isStreamMute(AudioManager.STREAM_DTMF))
                    audioManager.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_UNMUTE, 0);
            }
            if (audioManager.isStreamMute(AudioManager.STREAM_MUSIC))
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }

        // get mute state before set of all volumes; system stream may set mute to true
        boolean ringMuted = audioManager.isStreamMute(AudioManager.STREAM_RING);
        boolean notificationMuted = audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION);
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "ring mute status="+ringMuted);
        //PPApplication.logE("ActivateProfileHelper.setVolumes", "notification mute status="+notificationMuted);
        boolean systemMuted = audioManager.isStreamMute(AudioManager.STREAM_SYSTEM);
        boolean dtmfMuted = audioManager.isStreamMute(AudioManager.STREAM_DTMF);
        boolean musicMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

        if (forRingerMode) {
            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeRingtoneChange()=" + profile.getVolumeRingtoneChange());
            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeRingtoneValue()=" + profile.getVolumeRingtoneValue());
            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeNotificationChange()=" + profile.getVolumeNotificationChange());
            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeNotificationValue()=" + profile.getVolumeNotificationValue());
            if (!profile._volumeMuteSound) {
                if (!ringMuted) {
                    if (profile.getVolumeRingtoneChange()) {
                        if (forProfileActivation) {
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            setRingerVolume(appContext, profile.getVolumeRingtoneValue());
                        }
                    }
                }
                if (!notificationMuted) {
                    if (profile.getVolumeNotificationChange()) {
                        if (forProfileActivation) {
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            setNotificationVolume(appContext, profile.getVolumeNotificationValue());
                        }
                    }
                }
            }

            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeAccessibilityChange()=" + profile.getVolumeAccessibilityChange());
            //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeAccessibilityValue()=" + profile.getVolumeAccessibilityValue());

            if (forProfileActivation) {
                if (Build.VERSION.SDK_INT >= 26) {
                    if (profile.getVolumeAccessibilityChange()) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY /* 10 */, profile.getVolumeAccessibilityValue(), 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.STREAM_ACCESSIBILITY, profile.getVolumeAccessibilityValue());
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
            }

            //PPApplication.logE("ActivateProfileHelper.setVolumes", "isAudibleSystemRingerMode=" + isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/));

            if (!profile._volumeMuteSound) {
                //if (isAudibleRinging(ringerMode, zenMode, true) || (ringerMode == 0)) {
                if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/) || (ringerMode == 0)) {
                    // test only system ringer mode

                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "ringer/notification/system change");

                    //if (Permissions.checkAccessNotificationPolicy(context)) {

                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeDTMFChange()=" + profile.getVolumeDTMFChange());
                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeDTMFValue()=" + profile.getVolumeDTMFValue());
                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeSystemChange()=" + profile.getVolumeSystemChange());
                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "profile.getVolumeSystemValue()=" + profile.getVolumeSystemValue());

                    if (forProfileActivation) {
                        if (!dtmfMuted) {
                            if (profile.getVolumeDTMFChange()) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                                try {
                                    audioManager.setStreamVolume(AudioManager.STREAM_DTMF /* 8 */, profile.getVolumeDTMFValue(), 0);
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_DTMF, profile.getVolumeDTMFValue());
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                        if (!systemMuted) {
                            if (profile.getVolumeSystemChange()) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                                try {
                                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM /* 1 */, profile.getVolumeSystemValue(), 0);
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                                    //correctVolume0(audioManager);
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                    }

                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "ActivateProfileHelper.getMergedRingNotificationVolumes()=" + ActivateProfileHelper.getMergedRingNotificationVolumes());
                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "ApplicationPreferences.applicationUnlinkRingerNotificationVolumes=" + ApplicationPreferences.applicationUnlinkRingerNotificationVolumes);

                    boolean volumesSet = false;
                    //TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (/*(telephony != null) &&*/ ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {

                        //PPApplication.logE("ActivateProfileHelper.setVolumes", "do unlink volumes");

                        if ((!ringMuted) && (!notificationMuted)) {

                            //PPApplication.logE("ActivateProfileHelper.setVolumes", "linkUnlink=" + linkUnlink);

                            //int callState = telephony.getCallState();
                            if ((linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK)/* ||
                            (callState == TelephonyManager.CALL_STATE_RINGING)*/) {
                                // for separating ringing and notification
                                // in ringing state ringer volumes must by set
                                // and notification volumes must not by set
                                int volume = ApplicationPreferences.prefRingerVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-RINGING-unlink  ringer volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, 0);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().ringingVolume = volume;
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, 0);
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                        //correctVolume0(audioManager);
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                volumesSet = true;
                            } else if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_LINK) {
                                // for separating ringing and notification
                                // in not ringing state ringer and notification volume must by change
                                int volume = ApplicationPreferences.prefRingerVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-link  ringer volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, 0);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().ringingVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                volume = ApplicationPreferences.prefNotificationVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-link  notification volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, 0);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                //correctVolume0(audioManager);
                                volumesSet = true;
                            } else if ((linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_NONE)/* ||
                                    (callState == TelephonyManager.CALL_STATE_IDLE)*/) {
                                int volume = ApplicationPreferences.prefRingerVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-none  ringer volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, 0);
                                        if (PhoneProfilesService.getInstance() != null)
                                            PhoneProfilesService.getInstance().ringingVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                        //correctVolume0(audioManager);
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                volume = ApplicationPreferences.prefNotificationVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING-none  notification volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, 0);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                        //correctVolume0(audioManager);
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                volumesSet = true;
                            }
                        }
                    }
                    if (!volumesSet) {
                        // reverted order for disabled unlink
                        int volume;
                        if (!ActivateProfileHelper.getMergedRingNotificationVolumes()) {
                            if (!notificationMuted) {
                                volume = ApplicationPreferences.prefNotificationVolume;
                                //PPApplication.logE("ActivateProfileHelper.setVolumes", "no doUnlink  notification volume=" + volume);
                                if (volume != -999) {
                                    try {
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, 0);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                        //correctVolume0(audioManager);
                                        //PPApplication.logE("ActivateProfileHelper.setVolumes", "notification volume set");
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                            }
                        }
                        if (!ringMuted) {
                            volume = ApplicationPreferences.prefRingerVolume;
                            //PPApplication.logE("ActivateProfileHelper.setVolumes", "no doUnlink  ringer volume=" + volume);
                            if (volume != -999) {
                                try {
                                    audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, 0);
                                    if (PhoneProfilesService.getInstance() != null)
                                        PhoneProfilesService.getInstance().ringingVolume = volume;
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                    //correctVolume0(audioManager);
                                    //PPApplication.logE("ActivateProfileHelper.setVolumes", "ringer volume set");
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                    }
                    //}
                    //else
                    //    PPApplication.logE("ActivateProfileHelper.setVolumes", "not granted");
                }
            }
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeMediaChange()=" + profile.getVolumeMediaChange());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeMediaValue()=" + profile.getVolumeMediaValue());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeAlarmChange()=" + profile.getVolumeAlarmChange());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeAlarmValue()=" + profile.getVolumeAlarmValue());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeVoiceChange()=" + profile.getVolumeVoiceChange());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeVoiceValue()=" + profile.getVolumeVoiceValue());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeBluetoothSCOChange()=" + profile.getVolumeBluetoothSCOChange());
            PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setVolumes", "profile.getVolumeBluetoothSCOValue()=" + profile.getVolumeBluetoothSCOValue());
        }*/

        if (forProfileActivation) {
            if (profile.getVolumeBluetoothSCOChange()) {
                try {
                    audioManager.setStreamVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO, profile.getVolumeBluetoothSCOValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            if (profile.getVolumeVoiceChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL /* 0 */, profile.getVolumeVoiceValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            if (profile.getVolumeAlarmChange()) {
                try {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM /* 4 */, profile.getVolumeAlarmValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            if (!profile._volumeMuteSound) {
                if (!musicMuted) {
                    if (profile.getVolumeMediaChange()) {
                        setMediaVolume(appContext, audioManager, profile.getVolumeMediaValue());
                    }
                }
            }
        }

        //int value = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        //PPApplication.logE("[VOL] ActivateProfileHelper.setVolumes", "STREAM_VOICE_CALL="+value);

    }

    static void setMediaVolume(Context context, AudioManager audioManager, int value) {
        //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "value="+value);

        // Fatal Exception: java.lang.SecurityException: Only SystemUI can disable the safe media volume:
        // Neither user 10118 nor current process has android.permission.STATUS_BAR_SERVICE.
        try {
            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (1)");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, 0);
            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
        } catch (SecurityException e) {
            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (1) - SecurityException");
            //PPApplication.recordException(e);
            Context appContext = context.getApplicationContext();
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "WRITE_SECURE_SETTINGS granted");
                try {
                    //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "disable safe volume without root");
                    Settings.Global.putInt(appContext.getContentResolver(), "audio_safe_volume_state", 2);
                    //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (2)");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, 0);
                }
                catch (Exception e2) {
                    //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (2) - Exception");
                    PPApplication.recordException(e);
                }
            }
            else {
                //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "WRITE_SECURE_SETTINGS NOT granted");
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                        (PPApplication.isRooted(false))) {
                    synchronized (PPApplication.rootMutex) {
                        String command1 = "settings put global audio_safe_volume_state 2";
                        Command command = new Command(0, false, command1);
                        try {
                            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "disable safe volume with root");
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            PPApplication.commandWait(command, "ActivateProfileHelper.setMediaVolume");
                            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (3)");
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, 0);
                        } catch (Exception ee) {
                            // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                            //PPApplication.recordException(e);;
                            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (3) - Exception");
                        }
                    }
                }
            }
        } catch (Exception e3) {
            PPApplication.recordException(e3);
            //PPApplication.logE("[TEST MEDIA VOLUME] ActivateProfileHelper.setMediaVolume", "set media volume (1) - Exception");
        }
    }

    /*
    private static void setZenMode(Context context, int zenMode, AudioManager audioManager, int systemZenMode, int ringerMode)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            //if (PPApplication.logEnabled()) {
            //    PPApplication.logE("ActivateProfileHelper.setZenMode", "zenMode=" + zenMode);
            //    PPApplication.logE("ActivateProfileHelper.setZenMode", "ringerMode=" + ringerMode);
            //}

            Context appContext = context.getApplicationContext();

            //int systemZenMode = getSystemZenMode(appContext); //, -1);
            //PPApplication.logE("ActivateProfileHelper.setZenMode", "systemZenMode=" + systemZenMode);
            //int systemRingerMode = audioManager.getRingerMode();
            //PPApplication.logE("ActivateProfileHelper.setZenMode", "systemRingerMode=" + systemRingerMode);

            if ((zenMode != ZENMODE_SILENT) && canChangeZenMode(appContext)) {
                //PPApplication.logE("ActivateProfileHelper.setZenMode", "not ZENMODE_SILENT and can change zen mode");

                try {
                    if (ringerMode != -1) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        audioManager.setRingerMode(ringerMode);

                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                            } catch (Exception ee) {
                                //PPApplication.recordException(ee);
                            }
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                            } catch (Exception ee) {
                                //PPApplication.recordException(ee);
                            }
                        }
                        else {
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                            } catch (Exception ee) {
                                //PPApplication.recordException(ee);
                            }
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                            } catch (Exception ee) {
                                //PPApplication.recordException(ee);
                            }
                        }

                        //if (!((zenMode == ZENMODE_PRIORITY) && (ringerMode == AudioManager.RINGER_MODE_VIBRATE))) {
                            PPApplication.sleep(500);
                        //}
                    }

                    if ((zenMode != systemZenMode) || (zenMode == ZENMODE_PRIORITY)) {
                        //PPApplication.logE("ActivateProfileHelper.setZenMode", "change zen mode");
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        PPNotificationListenerService.requestInterruptionFilter(appContext, zenMode);
                        InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, zenMode);
                    }

                    //if (zenMode == ZENMODE_PRIORITY) {
                    //    PPApplication.logE("ActivateProfileHelper.setZenMode", "change ringer mode");
                    //    PPApplication.sleep(1000);
                    //    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //    audioManager.setRingerMode(ringerMode);
                    //}
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

            } else {
                //PPApplication.logE("ActivateProfileHelper.setZenMode", "ZENMODE_SILENT or not can change zen mode");
                try {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (zenMode) {
                        case ZENMODE_SILENT:
                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //PPApplication.sleep(1000);
                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            if ((systemZenMode != ZENMODE_ALL) && canChangeZenMode(appContext)) {
                                //PPApplication.logE("ActivateProfileHelper.setZenMode", "change zen mode");
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                                PPApplication.sleep(500);
                            }
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            break;
                        default:
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(ringerMode);
                    }
                } catch (Exception e) {
                    // may be produced this exception:
                    //
                    // java.lang.SecurityException: Not allowed to change Do Not Disturb state
                    //
                    // when changed is ringer mode in activated Do not disturb and
                    // GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context) returns false.
                    PPApplication.recordException(e);
                }
            }
        //}
        //else {
        //    RingerModeChangeReceiver.notUnlinkVolumes = false;
        //    audioManager.setRingerMode(ringerMode);
        //}
    }
    */

    private static void setVibrateWhenRinging(Context context, Profile profile, int value) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "profile=" + profile);
            PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "value=" + value);
        }*/
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

        //PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "lValue="+lValue);
        if (lValue != -1) {
            Context appContext = context.getApplicationContext();
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, null, false, appContext).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (Permissions.checkVibrateWhenRinging(appContext)) {
                    /*if (android.os.Build.VERSION.SDK_INT < 23) {    // Not working in Android M (exception)
                        Settings.System.putInt(appContext.getContentResolver(), "vibrate_when_ringing", lValue);
                        //PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "vibrate when ringing set (API < 23)");
                    }
                    else {*/
                        try {
                            Settings.System.putInt(appContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, lValue);
                            //PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "vibrate when ringing set (API >= 23)");
                        } catch (Exception ee) {
                            // java.lang.IllegalArgumentException: You cannot change private secure settings.
                            //Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(ee));
                            //PPApplication.recordException(ee);

                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "ActivateProfileHelper.setVibrationWhenRinging");
                                        //PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "vibrate when ringing set (API >= 23 with root)");
                                    } catch (Exception e) {
                                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(e));
                                        //PPApplication.recordException(e);
                                    }
                                }
                            }
                            //else
                                //PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "not rooted");
                        }
                    //}
                }
                //else
                //    PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "not permission granted");
            }
            //else
            //    PPApplication.logE("ActivateProfileHelper.setVibrateWhenRinging", "not profile preferences allowed");
        }
    }

    private static boolean setTones(Context context, Profile profile) {
        boolean noError = true;
        Context appContext = context.getApplicationContext();
        if (Permissions.checkProfileRingtones(appContext, profile, null)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    try {
                        String[] splits = profile._soundRingtone.split("\\|");
                        if (!splits[0].isEmpty()) {
                            Uri uri = Uri.parse(splits[0]);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                //PPApplication.logE("ActivateProfileHelper.setTones", "ring tone granted");
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("BitmapManipulator.resampleBitmapUri", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                            }
                            //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, splits[0]);
                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE, null,
                                profile._name, profile._icon, 0, "");
                        noError = false;
                        /*String[] splits = profile._soundRingtone.split("\\|");
                        if (!splits[0].isEmpty()) {
                            try {
                                boolean found = false;
                                RingtoneManager manager = new RingtoneManager(context);
                                Cursor cursor = manager.getCursor();
                                while (cursor.moveToNext()) {
                                    String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                                    if (_uri.equals(splits[0])) {
                                        // uri exists in RingtoneManager
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplication.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplication.recordException(e);
                            }
                        } else
                            PPApplication.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, null);
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.recordException(e);
                        noError = false;
                    }
                }
            }
            if (profile._soundNotificationChange == 1) {
                if (!profile._soundNotification.isEmpty()) {
                    try {
                        String[] splits = profile._soundNotification.split("\\|");
                        if (!splits[0].isEmpty()) {
                            Uri uri = Uri.parse(splits[0]);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                //PPApplication.logE("ActivateProfileHelper.setTones", "notification tone granted");
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("BitmapManipulator.resampleBitmapUri", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                            }
                            //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, splits[0]);
                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION, null,
                                profile._name, profile._icon, 0, "");
                        noError = false;
                        /*String[] splits = profile._soundNotification.split("\\|");
                        if (!splits[0].isEmpty()) {
                            try {
                                boolean found = false;
                                RingtoneManager manager = new RingtoneManager(context);
                                Cursor cursor = manager.getCursor();
                                while (cursor.moveToNext()) {
                                    String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                                    if (_uri.equals(splits[0])) {
                                        // uri exists in RingtoneManager
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplication.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplication.recordException(e);
                            }
                        } else
                            PPApplication.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, null);
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.recordException(e);
                        noError = false;
                    }
                }
            }
            if (profile._soundAlarmChange == 1) {
                if (!profile._soundAlarm.isEmpty()) {
                    try {
                        String[] splits = profile._soundAlarm.split("\\|");
                        if (!splits[0].isEmpty()) {
                            Uri uri = Uri.parse(splits[0]);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                //PPApplication.logE("ActivateProfileHelper.setTones", "alarm tone granted");
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("BitmapManipulator.resampleBitmapUri", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                            }
                            //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, splits[0]);
                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM, null,
                                profile._name, profile._icon, 0, "");
                        noError = false;
                        /*String[] splits = profile._soundAlarm.split("\\|");
                        if (!splits[0].isEmpty()) {
                            try {
                                boolean found = false;
                                RingtoneManager manager = new RingtoneManager(context);
                                Cursor cursor = manager.getCursor();
                                while (cursor.moveToNext()) {
                                    String _uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                                    if (_uri.equals(splits[0])) {
                                        // uri exists in RingtoneManager
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplication.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplication.recordException(e);
                            }
                        } else
                            PPApplication.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplication.recordException(e);
                    }
                    catch (Exception e){
                        PPApplication.recordException(e);
                        noError = false;
                    }
                }
            }
        }

        return noError;
    }

    static void executeForVolumes(final Profile profile, final int linkUnlinkVolumes, final boolean forProfileActivation, Context context) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "profile=" + profile);
            if (profile != null)
                PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "profile._name=" + profile._name);
            PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "linkUnlinkVolumes=" + linkUnlinkVolumes);
            PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "forProfileActivation=" + forProfileActivation);
        }*/

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadVolumes();
        final Handler handler = new Handler(PPApplication.handlerThreadVolumes.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForVolumes");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadVolumes", "START run - from=ActivateProfileHelper.executeForVolumes");

                    int linkUnlink = PhoneCallBroadcastReceiver.LINKMODE_NONE;
                    if (ActivateProfileHelper.getMergedRingNotificationVolumes() &&
                            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {
                        if (Permissions.checkPhone(appContext))
                            linkUnlink = linkUnlinkVolumes;
                    }
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "linkUnlink=" + linkUnlink);

                    if (profile != null) {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "setTones() 1");
                        boolean noErrorSetTone = setTones(appContext, profile);

                        final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                        if ((profile._volumeRingerMode != 0) ||
                            profile.getVolumeRingtoneChange() ||
                            profile.getVolumeNotificationChange() ||
                            profile.getVolumeSystemChange() ||
                            profile.getVolumeDTMFChange()) {

                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "change ringer mode");

                            //if (Permissions.checkProfileAccessNotificationPolicy(context, profile, null)) {
                            if (canChangeZenMode(appContext)) {
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "can change zen mode");

                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "changeRingerModeForVolumeEqual0()");
                                changeRingerModeForVolumeEqual0(profile, audioManager, appContext);
                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "changeNotificationVolumeForVolumeEqual0()");
                                changeNotificationVolumeForVolumeEqual0(/*context,*/ profile);

                                RingerModeChangeReceiver.internalChange = true;

                                //int systemZenMode = getSystemZenMode(appContext/*, -1*/);

                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "setRingerMode()");
                                setRingerMode(appContext, profile, audioManager, /*systemZenMode,*/ forProfileActivation);

                                // get actual system zen mode (may be changed in setRingerMode())
                                int systemZenMode = getSystemZenMode(appContext/*, -1*/);

                                //PPApplication.logE("ActivateProfileHelper.executeForVolumes", "internalChange=" + RingerModeChangeReceiver.internalChange);
                                PPApplication.sleep(500);

                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "setVolumes()");
                                setVolumes(appContext, profile, audioManager, systemZenMode, linkUnlink, forProfileActivation, true);

                                //PPApplication.logE("ActivateProfileHelper.executeForVolumes", "internalChange=" + RingerModeChangeReceiver.internalChange);

                                PPApplication.sleep(500);

                                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "start internal change work");
                                DisableInternalChangeWorker.enqueueWork();

                                /*PPApplication.startHandlerThreadInternalChangeToFalse();
                                final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("ActivateProfileHelper.executeForVolumes", "disable ringer mode change internal change");
                                        RingerModeChangeReceiver.internalChange = false;
                                    }
                                }, 3000);*/
                                //PostDelayedBroadcastReceiver.setAlarm(
                                //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, context);
                            }
                        }
                        else
                        /*if (profile.getVolumeMediaChange() ||
                            profile.getVolumeAlarmChange() ||
                            profile.getVolumeVoiceChange() ||
                            profile.getVolumeAccessibilityChange() ||
                            profile.getVolumeBluetoothSCOChange())*/ {
                            // call setVolume() for "Mute sound"

                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "do not change ringer mode");

                            int systemZenMode = getSystemZenMode(appContext/*, -1*/);

                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "setVolumes()");
                            setVolumes(appContext, profile, audioManager, systemZenMode, linkUnlink, forProfileActivation, false);
                        }
                        /*else {
                            PPApplication.logE("ActivateProfileHelper.executeForVolumes", "ringer mode and volumes are not configured");
                        }*/

                        if (noErrorSetTone) {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "setTones() 2");
                            setTones(appContext, profile);
                        }

                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.executeForVolumes", "end");

                    }
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

    private static void setNotificationLed(Context context, final int value) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadProfileActivation();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setNotificationLed");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setNotificationLed");

                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, null, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                        //    Settings.System.putInt(appContext.getContentResolver(), "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/, value);
                        //else {
                        /* not working (private secure settings) :-/
                        if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                            Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", value);
                        }
                        else*/
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/ + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "ActivateProfileHelper.setNotificationLed");
                                    } catch (Exception e) {
                                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setNotificationLed", Log.getStackTraceString(e));
                                        //PPApplication.recordException(e);;
                                    }
                                }
                            }
                        //}
                    }
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

    private static void setHeadsUpNotifications(Context context, final int value) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadProfileActivation();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setHeadsUpNotifications");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setHeadsUpNotifications");

                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, null, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        //if (android.os.Build.VERSION.SDK_INT >= 21) {
                            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                Settings.Global.putInt(appContext.getContentResolver(), "heads_up_notifications_enabled", value);
                            } else {
                                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                        (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                    synchronized (PPApplication.rootMutex) {
                                        String command1 = "settings put global " + "heads_up_notifications_enabled" + " " + value;
                                        //if (PPApplication.isSELinuxEnforcing())
                                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                        Command command = new Command(0, false, command1); //, command2);
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            PPApplication.commandWait(command, "ActivateProfileHelper.setHeadsUpNotifications");
                                        } catch (Exception e) {
                                            // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                            //Log.e("ActivateProfileHelper.setHeadsUpNotifications", Log.getStackTraceString(e));
                                            //PPApplication.recordException(e);
                                        }
                                    }
                                }
                            }
                        //}
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
    }

    private static void setAlwaysOnDisplay(Context context, final int value) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadProfileActivation();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setAlwaysOnDisplay");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setAlwaysOnDisplay");

                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, null, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        /* not working (private secure settings) :-/
                        if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                            Settings.System.putInt(context.getContentResolver(), "aod_mode", value);
                        }
                        else*/
                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                            synchronized (PPApplication.rootMutex) {
                                String command1 = "settings put system " + "aod_mode" + " " + value;
                                //if (PPApplication.isSELinuxEnforcing())
                                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                Command command = new Command(0, false, command1); //, command2);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command, "ActivateProfileHelper.setAlwaysOnDisplay");
                                } catch (Exception e) {
                                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper.setAlwaysOnDisplay", Log.getStackTraceString(e));
                                    //PPApplication.recordException(e);
                                }
                            }
                        }
                    }
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

    private static void setScreenOnPermanent(Profile profile, Context context) {
        //PPApplication.logE("******** ActivateProfileHelper.setScreenOnPermanent", "profile._screenOnPermanent="+profile._screenOnPermanent);
        if (Permissions.checkProfileScreenOnPermanent(context, profile, null)) {
            if (profile._screenOnPermanent == 1)
                createKeepScreenOnView(context);
            else if (profile._screenOnPermanent == 2)
                removeKeepScreenOnView(context);
        }
    }

    private static void changeRingerModeForVolumeEqual0(Profile profile, AudioManager audioManager, Context context) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "volumeRingtoneChange=" + profile.getVolumeRingtoneChange());
            PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "volumeRingtoneValue=" + profile.getVolumeRingtoneValue());
        }*/

        //profile._ringerModeForZenMode = AudioManager.RINGER_MODE_NORMAL;

        if (profile.getVolumeRingtoneChange()) {
            if (profile.getVolumeRingtoneValue() == 0) {
                profile.setVolumeRingtoneValue(1);
                //profile._ringerModeForZenMode = AudioManager.RINGER_MODE_SILENT;

                // for profile ringer/zen mode = "only vibrate" do not change ringer mode to Silent
                if (!isVibrateRingerMode(profile._volumeRingerMode/*, profile._volumeZenMode*/)) {
                    if (isAudibleRinging(profile._volumeRingerMode, profile._volumeZenMode/*, false*/)) {
                        // change ringer mode to Silent
                        //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "changed to silent");
                        profile._volumeRingerMode = Profile.RINGERMODE_SILENT;
                    }// else
                    //    PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "not audible ringer mode in profile");
                }// else
                 //   PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "vibrate ringer mode in profile");
            } else {
                if (profile._volumeRingerMode == 0) {
                    // ringer mode is not changed by profile, use system ringer and zen mode
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                        int systemZenMode = getSystemZenMode(context/*, -1*/);
                        if (!isAudibleSystemRingerMode(audioManager, systemZenMode/*, context*/)) {
                            // change ringer mode to ringing becaiuse configured is ringing volume
                            // Priority zen mode is audible. DO NOT DISABLE IT !!!
                            //PPApplication.logE("ActivateProfileHelper.changeRingerModeForVolumeEqual0", "system ringer mode is not audible - changed to ringing");
                            profile._volumeRingerMode = Profile.RINGERMODE_RING;
                        }
                    }
                }
            }
        }
    }

    private static boolean checkAccessNotificationPolicy(Context context) {
        Context appContext = context.getApplicationContext();
        try {
            NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean granted = false;
            if (mNotificationManager != null)
                granted = mNotificationManager.isNotificationPolicyAccessGranted();
            //if (granted)
            //    setShowRequestAccessNotificationPolicyPermission(context, true);
            return granted;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean canChangeZenMode(Context context/*, boolean notCheckAccess*/) {
        Context appContext = context.getApplicationContext();
        //if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                //if (notCheckAccess)
                //    return true;
                //else
                    return checkAccessNotificationPolicy(appContext);
                    //return Permissions.checkAccessNotificationPolicy(appContext);
            }
            else
                return PPNotificationListenerService.isNotificationListenerServiceEnabled(appContext);
        //}
        //else
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //    return PPNotificationListenerService.isNotificationListenerServiceEnabled(appContext);
        //return false;
    }

    private static void changeNotificationVolumeForVolumeEqual0(Profile profile) {
        //if (PPApplication.logEnabled()) {
            //PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "volumeNotificationChange=" + profile.getVolumeNotificationChange());
            //PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "mergedRingNotificationVolumes=" + getMergedRingNotificationVolumes(context));
            //PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "volumeNotificationValue=" + profile.getVolumeNotificationValue());
        //}
        if (profile.getVolumeNotificationChange() && ActivateProfileHelper.getMergedRingNotificationVolumes()) {
            if (profile.getVolumeNotificationValue() == 0) {
                //PPApplication.logE("ActivateProfileHelper.changeNotificationVolumeForVolumeEqual0", "changed notification value to 1");
                profile.setVolumeNotificationValue(1);
            }
        }
    }

    @SuppressLint("SwitchIntDef")
    static int getSystemZenMode(Context context/*, int defaultValue*/) {
        Context appContext = context.getApplicationContext();
        //if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //PPApplication.logE("ActivateProfileHelper.getSystemZenMode", "no60="+no60);
            boolean activityExists = GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context);
            //PPApplication.logE("ActivateProfileHelper.getSystemZenMode", "activityExists="+activityExists);
            if (no60 && activityExists) {
                NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                //PPApplication.logE("ActivateProfileHelper.getSystemZenMode", "mNotificationManager="+mNotificationManager);
                if (mNotificationManager != null) {
                    int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
                    //PPApplication.logE("ActivateProfileHelper.getSystemZenMode", "interruptionFilter="+interruptionFilter);
                    switch (interruptionFilter) {
                        case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                            return ActivateProfileHelper.ZENMODE_ALARMS;
                        case NotificationManager.INTERRUPTION_FILTER_NONE:
                            return ActivateProfileHelper.ZENMODE_NONE;
                        case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                            return ActivateProfileHelper.ZENMODE_PRIORITY;
                        case NotificationManager.INTERRUPTION_FILTER_ALL:
                        case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        default:
                            return ActivateProfileHelper.ZENMODE_ALL;
                    }
                }
            }
            else {
                ContentResolver resolver = appContext.getContentResolver();
                if (resolver != null) {
                    int interruptionFilter = Settings.Global.getInt(resolver, "zen_mode", -1);
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
        /*}
        if (android.os.Build.VERSION.SDK_INT < 23) {
            int interruptionFilter = Settings.Global.getInt(appContext.getContentResolver(), "zen_mode", -1);
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
        }*/
        return -1; //defaultValue;
    }

    /*
    static boolean vibrationIsOn(AudioManager audioManager, boolean testRingerMode) {
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

        //if (PPApplication.logEnabled()) {
        //    PPApplication.logE("PPApplication.vibrationIsOn", "ringerMode=" + ringerMode);
        //    PPApplication.logE("PPApplication.vibrationIsOn", "vibrateType=" + vibrateType);
        //}
        //PPApplication.logE("PPApplication.vibrationIsOn", "vibrateWhenRinging="+vibrateWhenRinging);

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT);// ||
        //(vibrateWhenRinging == 1);
    }
    */

    private static void setVibrateSettings(boolean vibrate, AudioManager audioManager) {
        if (vibrate) {
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception ee) {
                //PPApplication.recordException(ee);
            }
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception ee) {
                //PPApplication.recordException(ee);
            }
        }
        else {
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception ee) {
                //PPApplication.recordException(ee);
            }
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception ee) {
                //PPApplication.recordException(ee);
            }
        }
    }

    private static void setRingerMode(Context context, Profile profile, AudioManager audioManager, /*int systemZenMode,*/ boolean forProfileActivation)
    {
        //PPApplication.logE("@@@ ActivateProfileHelper.setRingerMode", "audioM.ringerMode=" + audioManager.getRingerMode());

        Context appContext = context.getApplicationContext();

        int ringerMode;
        int zenMode;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.setRingerMode", "profile._volumeRingerMode=" + profile._volumeRingerMode);
            PPApplication.logE("ActivateProfileHelper.setRingerMode", "profile._volumeZenMode=" + profile._volumeZenMode);
        }*/

        if (forProfileActivation) {
            if (profile._volumeRingerMode != 0) {
                saveRingerMode(appContext, profile._volumeRingerMode);
                if ((profile._volumeRingerMode == Profile.RINGERMODE_ZENMODE) && (profile._volumeZenMode != 0))
                    saveZenMode(appContext, profile._volumeZenMode);
            }
        }

        //if (firstCall)
        //    return;

        ringerMode = ApplicationPreferences.prefRingerMode;
        zenMode = ApplicationPreferences.prefZenMode;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringerMode=" + ringerMode);
            PPApplication.logE("ActivateProfileHelper.setRingerMode", "zenMode=" + zenMode);
            PPApplication.logE("ActivateProfileHelper.setRingerMode", "_ringerModeForZenMode=" + profile._ringerModeForZenMode);
        }*/

        if (forProfileActivation) {

            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode change");

            switch (ringerMode) {
                case Profile.RINGERMODE_RING:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=RING");

                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                    PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    PPApplication.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setVibrateSettings(false, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_NORMAL);

                    setVibrateWhenRinging(appContext, profile, -1);
                    break;
                case Profile.RINGERMODE_RING_AND_VIBRATE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=RING & VIBRATE");

                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                    PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    PPApplication.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setVibrateSettings(true, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_NORMAL);

                    setVibrateWhenRinging(appContext, null, 1);
                    break;
                case Profile.RINGERMODE_VIBRATE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=VIBRATE");

                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                    PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    PPApplication.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    setVibrateSettings(true, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                    setVibrateWhenRinging(appContext, null, 1);
                    break;
                case Profile.RINGERMODE_SILENT:

                    //setZenMode(appContext, ZENMODE_SILENT, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);

                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setVibrateSettings(false, audioManager);
                    PPApplication.sleep(500);
                    PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALARMS);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALARMS);

                    setVibrateWhenRinging(appContext, profile, -1);
                    break;
                case Profile.RINGERMODE_ZENMODE:
                    //PPApplication.logE("ActivateProfileHelper.setRingerMode", "ringer mode=ZEN MODE");
                    switch (zenMode) {
                        case Profile.ZENMODE_ALL:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALL");

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            PPApplication.sleep(500);
                            PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);

                            //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);

                            setVibrateWhenRinging(appContext, profile, -1);
                            break;
                        case Profile.ZENMODE_PRIORITY:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=PRIORITY");

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            PPApplication.sleep(500);
                            PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, profile._ringerModeForZenMode);

                            setVibrateWhenRinging(appContext, profile, -1);
                            break;
                        case Profile.ZENMODE_NONE:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=NONE");

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            PPApplication.sleep(500);
                            PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_NONE);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_NONE);

                            //setZenMode(appContext, ZENMODE_NONE, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);
                            break;
                        case Profile.ZENMODE_ALL_AND_VIBRATE:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALL & VIBRATE");
                            // this is as Sound mode = Vibrate

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateSettings(true, audioManager);
                            PPApplication.sleep(500);
                            PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);

                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                            setVibrateWhenRinging(appContext, null, 1);
                            break;
                        case Profile.ZENMODE_PRIORITY_AND_VIBRATE:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=PRIORITY & VIBRATE");

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            if (Build.VERSION.SDK_INT <= 25) {
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                PPApplication.sleep(500);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                //PPApplication.sleep(500);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }
                            else
                            if (Build.VERSION.SDK_INT <= 28) {
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                //setVibrateSettings(true, audioManager);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                                PPApplication.sleep(1000);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }
                            else {
                                // must be set 2x to keep vibraton
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                PPApplication.sleep(500);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                //PPApplication.sleep(500);
                                PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }

                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);
                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                            setVibrateWhenRinging(appContext, null, 1);
                            break;
                        case Profile.ZENMODE_ALARMS:
                            //PPApplication.logE("ActivateProfileHelper.setRingerMode", "zen mode=ALARMS");

                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            PPApplication.sleep(500);
                            PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALARMS);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALARMS);

                            //setZenMode(appContext, ZENMODE_ALARMS, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
            }
        }
    }

    private static void executeForWallpaper(final Profile profile, Context context) {
        if (profile._deviceWallpaperChange == 1)
        {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadWallpaper();
            final Handler handler = new Handler(PPApplication.handlerThreadWallpaper.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForWallpaper");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadWallpaper", "START run - from=ActivateProfileHelper.executeForWallpaper");

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        WindowManager wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (wm != null) {
                            Display display = wm.getDefaultDisplay();
                            //if (android.os.Build.VERSION.SDK_INT >= 17)
                            display.getRealMetrics(displayMetrics);
                            //else
                            //    display.getMetrics(displayMetrics);
                            int height = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels;
                            if (appContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                //noinspection SuspiciousNameCombination
                                height = displayMetrics.widthPixels;
                                //noinspection SuspiciousNameCombination
                                width = displayMetrics.heightPixels;
                            }
                            //PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadWallpaper", "height="+height);
                            //PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadWallpaper", "width="+width);

                            // for lock screen no double width
                            if ((android.os.Build.VERSION.SDK_INT < 24) || (profile._deviceWallpaperFor != 2))
                                width = width << 1; // best wallpaper width is twice screen width
                            //PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadWallpaper", "width (2)="+width);

                            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmapUri(profile._deviceWallpaper, width, height, false, true, appContext);
                            if (decodedSampleBitmap != null) {
                                // set wallpaper
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
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
                                    } else
                                        wallpaperManager.setBitmap(decodedSampleBitmap);
                                } catch (IOException e) {
                                    PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, null,
                                            profile._name, profile._icon, 0, "");
                                    //Log.e("ActivateProfileHelper.executeForWallpaper", Log.getStackTraceString(e));
                                    PPApplication.recordException(e);
                                } catch (Exception e) {
                                    PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, null,
                                            profile._name, profile._icon, 0, "");
                                    //PPApplication.recordException(e);
                                }
                            }
                            else {
                                PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER, null,
                                        profile._name, profile._icon, 0, "");
                            }
                        }
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

    private static void executeForRunApplications(final Profile profile, Context context) {
        if (profile._deviceRunApplicationChange == 1)
        {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadRunApplication();
            final Handler handler = new Handler(PPApplication.handlerThreadRunApplication.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (PPApplication.blockProfileEventActions)
                        // not start applications after boot
                        return;

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForRunApplications");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadRunApplication", "START run - from=ActivateProfileHelper.executeForRunApplications");

                        String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
                        Intent intent;
                        PackageManager packageManager = appContext.getPackageManager();

                        //ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        //List<ActivityManager.RunningAppProcessInfo> procInfo = activityManager.getRunningAppProcesses();

                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","profile._name="+profile._name);
                        for (String split : splits) {
                            //Log.d("ActivateProfileHelper.executeForRunApplications","app data="+splits[i]);
                            int startApplicationDelay = Application.getStartApplicationDelay(split);
                            if (Application.getStartApplicationDelay(split) > 0) {
                                //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","run with delay");
                                RunApplicationWithDelayBroadcastReceiver.setDelayAlarm(appContext, startApplicationDelay, profile._name, split);
                            } else {
                                if (Application.isShortcut(split)) {
                                    long shortcutId = Application.getShortcutId(split);
                                    //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - shortcutId="+shortcutId);
                                    if (shortcutId > 0) {
                                        //Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                                        Shortcut shortcut = DatabaseHandler.getInstance(appContext).getShortcut(shortcutId);
                                        if (shortcut != null) {
                                            try {
                                                intent = Intent.parseUri(shortcut._intent, 0);
                                                if (intent != null) {
                                                    //String packageName = intent.getPackage();
                                                    //if (!isRunning(procInfo, packageName)) {
                                                    //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName + ": not running");
                                                    //Log.d("ActivateProfileHelper.executeForRunApplications","intent="+intent);
                                                    //noinspection TryWithIdenticalCatches
                                                    try {
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        appContext.startActivity(intent);
                                                    } catch (ActivityNotFoundException e) {
                                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - ERROR (01)");
                                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, null,
                                                                profile._name, profile._icon, 0, "");
                                                    } catch (SecurityException e) {
                                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - ERROR (02)");
                                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, null,
                                                                profile._name, profile._icon, 0, "");
                                                    } catch (Exception e) {
                                                        PPApplication.recordException(e);
                                                    }
                                                    //} else
                                                    //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName + ": running");
                                                } else {
                                                    //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - ERROR (1)");
                                                    PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, null,
                                                            profile._name, profile._icon, 0, "");
                                                }
                                            } catch (Exception ee) {
                                                PPApplication.recordException(ee);
                                            }
                                        } else {
                                            //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - ERROR (2)");
                                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, null,
                                                    profile._name, profile._icon, 0, "");
                                        }
                                    } else {
                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","shortcut - ERROR (3)");
                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT, null,
                                                profile._name, profile._icon, 0, "");
                                    }
                                } else if (Application.isIntent(split)) {
                                    long intentId = Application.getIntentId(split);
                                    //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - intentId="+intentId);
                                    if (intentId > 0) {
                                        PPIntent ppIntent = DatabaseHandler.getInstance(appContext).getIntent(intentId);
                                        if (ppIntent != null) {
                                            intent = ApplicationEditorIntentActivityX.createIntent(ppIntent);
                                            if (intent != null) {
                                                if (ppIntent._intentType == 0) {
                                                    //noinspection TryWithIdenticalCatches
                                                    try {
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        appContext.startActivity(intent);
                                                    } catch (ActivityNotFoundException e) {
                                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - ERROR (01)");
                                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, null,
                                                                profile._name, profile._icon, 0, "");
                                                    } catch (SecurityException e) {
                                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - ERROR (02)");
                                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, null,
                                                                profile._name, profile._icon, 0, "");
                                                    } catch (Exception e) {
                                                        PPApplication.recordException(e);
                                                    }
                                                } else {
                                                    try {
                                                        appContext.sendBroadcast(intent);
                                                    } catch (Exception e) {
                                                        PPApplication.recordException(e);
                                                    }
                                                }
                                            } else {
                                                //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - ERROR (1)");
                                                PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, null,
                                                        profile._name, profile._icon, 0, "");
                                            }
                                        } else {
                                            //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - ERROR (2)");
                                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, null,
                                                    profile._name, profile._icon, 0, "");
                                        }
                                    } else {
                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent - ERROR (3)");
                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT, null,
                                                profile._name, profile._icon, 0, "");
                                    }
                                } else {
                                    String packageName = Application.getPackageName(split);
                                    intent = packageManager.getLaunchIntentForPackage(packageName);
                                    //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","application - intent="+intent);
                                    if (intent != null) {
                                        //if (!isRunning(procInfo, packageName)) {
                                        //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName+": not running");
                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","intent="+intent);
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        //noinspection TryWithIdenticalCatches
                                        try {
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            appContext.startActivity(intent);
                                            //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","application started");
                                        } catch (ActivityNotFoundException e) {
                                            //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","application - ERROR (01)");
                                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, null,
                                                    profile._name, profile._icon, 0, "");
                                        } catch (SecurityException e) {
                                            //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","application - ERROR (02)");
                                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, null,
                                                    profile._name, profile._icon, 0, "");
                                        } catch (Exception e) {
                                            //Log.e("ActivateProfileHelper.executeForRunApplications", Log.getStackTraceString(e));
                                        }
                                        //}
                                        //else
                                        //    PPApplication.logE("ActivateProfileHelper.executeForRunApplications", packageName+": running");
                                    }
                                    else {
                                        //PPApplication.logE("ActivateProfileHelper.executeForRunApplications","application - ERROR (1)");
                                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION, null,
                                                profile._name, profile._icon, 0, "");
                                    }
                                }
                            }
                            PPApplication.sleep(1000);
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
        }
    }

    //private static int processPID = -1;
    private static void executeForForceStopApplications(final Profile profile, Context context) {
        if (PPApplication.blockProfileEventActions)
            // not force stop applications after boot
            return;

        Context appContext = context.getApplicationContext();

        /*if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted(false))) {

            PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications","do force stop via root");

            synchronized (PPApplication.rootMutex) {
                processPID = -1;
                String command1 = "pidof sk.henrichg.phoneprofilesplus";
                Command command = new Command(0, false, command1) {
                    @Override
                    public void commandOutput(int id, String line) {
                        super.commandOutput(id, line);
                        PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications","shell output="+line);
                        try {
                            processPID = Integer.parseInt(line);
                        } catch (Exception e) {
                            processPID = -1;
                        }
                    }

                    @Override
                    public void commandTerminated(int id, String reason) {
                        super.commandTerminated(id, reason);
                        PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications","terminated="+reason);
                    }

                    @Override
                    public void commandCompleted(int id, int exitCode) {
                        super.commandCompleted(id, exitCode);
                        PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications","completed="+exitCode);
                    }
                };


                try {
                    PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications", "force stop application with root");
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    PPApplication.commandWait(command);
                    PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications", "processPID="+processPID);
                    //if (processPID != -1) {
                        PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications", "call RootTools.killProcess");
                        boolean killed = RootTools.killProcess(PPApplication.PACKAGE_NAME);
                        PPApplication.logE("ActivateProfileHelper.executeForForceStopApplications", "killed="+killed);
                    //}
                } catch (Exception ee) {
                    Log.e("ActivateProfileHelper.executeForForceStopApplications", Log.getStackTraceString(ee));
                }
            }
        } else {*/
            if (profile._lockDevice != 0)
                // not force stop if profile has lock device enabled
                return;

            String applications = profile._deviceForceStopApplicationPackageName;
            if (!(applications.isEmpty() || (applications.equals("-")))) {
                Intent intent = new Intent(PPApplication.ACTION_FORCE_STOP_APPLICATIONS_START);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(PPApplication.EXTRA_APPLICATIONS, applications);
                appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
            }
        //}
    }

    private static void executeRootForAdaptiveBrightness(final Profile profile, Context context) {
        /* not working (private secure settings) :-/
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            Settings.System.putFloat(appContext.getContentResolver(), ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                    profile.getDeviceBrightnessAdaptiveValue(appContext));
        }
        else {*/
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadProfileActivation();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeRootForAdaptiveBrightness");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.executeRootForAdaptiveBrightness");

                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ + " " +
                                    profile.getDeviceBrightnessAdaptiveValue(appContext);
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.executeRootForAdaptiveBrightness");
                            } catch (Exception e) {
                                // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.executeRootForAdaptiveBrightness", Log.getStackTraceString(e));
                                //PPApplication.recordException(e);
                            }
                        }
                    }
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
        //}
    }

    static void executeForInteractivePreferences(final Profile profile, final Context context) {
        if (profile == null)
            return;

        Context appContext = context.getApplicationContext();

        if (profile._deviceRunApplicationChange == 1)
        {
            executeForRunApplications(profile, appContext);
        }

        //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            if (profile._deviceMobileDataPrefs == 1)
            {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    boolean ok = true;
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                        appContext.startActivity(intent);
                        //PPApplication.logE("ActivateProfileHelper.executeForInteractivePreferences", "1. OK");
                    } catch (Exception e) {
                        ok = false;
                        // Xiaomi: android.content.ActivityNotFoundException: Unable to find explicit activity class {com.android.settings/com.android.settings.Settings$DataUsageSummaryActivity}; have you declared this activity in your AndroidManifest.xml?
                        //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "1. ERROR" + Log.getStackTraceString(e));
                        //PPApplication.recordException(e);
                    }
                    if (!ok) {
                        ok = true;
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                            intent.setComponent(componentName);
                            appContext.startActivity(intent);
                            //PPApplication.logE("ActivateProfileHelper.executeForInteractivePreferences", "2. OK");
                        } catch (Exception e) {
                            ok = false;
                            // Xiaomi: java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.DATA_ROAMING_SETTINGS flg=0x10000000 cmp=com.android.phone/.Settings } from ProcessRecord{215f88f 16252:sk.henrichg.phoneprofilesplus/u0a231} (pid=16252, uid=10231) not exported from uid 1001
                            //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "2. ERROR" + Log.getStackTraceString(e));
                            //PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        try {
                            final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(intent);
                            //PPApplication.logE("ActivateProfileHelper.executeForInteractivePreferences", "3. OK");
                        } catch (Exception e) {
                            //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "3. ERROR" + Log.getStackTraceString(e));
                            //PPApplication.recordException(e);
                        }
                    }
                }
                else {
                    boolean ok = false;
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                    if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                        ok = true;
                    if (!ok) {
                        intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.Settings"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                            ok = true;
                    }
                    if (!ok) {
                        intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                        if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                            ok = true;
                    }
                    if (ok) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                        String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                                appContext.getString(R.string.profile_preferences_deviceMobileDataPrefs);
                        showNotificationForInteractiveParameters(appContext, title, text, intent,
                                PPApplication.PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID,
                                PPApplication.PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_TAG);
                    }
                }
            }
        }

        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            if (profile._deviceNetworkTypePrefs == 1)
            {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                        final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            appContext.getString(R.string.profile_preferences_deviceNetworkTypePrefs);
                    showNotificationForInteractiveParameters(appContext, title, text, intent,
                            PPApplication.PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID,
                            PPApplication.PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_TAG);
                }
            }
        }

        //if (PPApplication.hardwareCheck(PPApplication.PREF_PROFILE_DEVICE_GPS, context))
        //{  No check only GPS
        if (profile._deviceLocationServicePrefs == 1)
        {
            if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                try {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            else {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                if (GlobalGUIRoutines.activityIntentExists(intent, appContext)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            appContext.getString(R.string.profile_preferences_deviceLocationServicePrefs);
                    showNotificationForInteractiveParameters(appContext, title, text, intent,
                            PPApplication.PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID,
                            PPApplication.PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_TAG);
                }
            }
        }
        //}
        if (profile._deviceWiFiAPPrefs == 1) {
            if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
                    appContext.startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            else {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
                if (GlobalGUIRoutines.activityIntentExists(intent, appContext)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                    String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                            appContext.getString(R.string.profile_preferences_deviceWiFiAPPrefs);
                    showNotificationForInteractiveParameters(appContext, title, text, intent,
                            PPApplication.PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID,
                            PPApplication.PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_TAG);
                }
            }
        }
    }

    static void execute(final Context context, final Profile profile/*, boolean merged, *//*boolean _interactive*/)
    {
        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "xxx");

        final Context appContext = context.getApplicationContext();

        // unlink ring and notifications - it is @Hide :-(
        //Settings.System.putInt(appContext.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        //final Profile profile = _profile; //Profile.getMappedProfile(_profile, appContext);

        // setup volume
        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeForVolumes()");
        ActivateProfileHelper.executeForVolumes(profile, PhoneCallBroadcastReceiver.LINKMODE_NONE,true, appContext);

        // set vibration on touch
        if (Permissions.checkProfileVibrationOnTouch(appContext, profile, null)) {
            switch (profile._vibrationOnTouch) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_vibrationOnTouch 1");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                    //Settings.System.putInt(context.getContentResolver(), Settings.Global.CHARGING_SOUNDS_ENABLED, 1);
                    // Settings.System.DTMF_TONE_WHEN_DIALING - working
                    // Settings.System.SOUND_EFFECTS_ENABLED - working
                    // Settings.System.LOCKSCREEN_SOUNDS_ENABLED - private secure settings :-(
                    // Settings.Global.CHARGING_SOUNDS_ENABLED - java.lang.IllegalArgumentException: You cannot keep your settings in the secure settings. :-/
                    //                                           (G1) not working :-/
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_vibrationOnTouch 2");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                    //Settings.System.putInt(context.getContentResolver(), Settings.Global.CHARGING_SOUNDS_ENABLED, 0);
                    break;
            }
        }
        // set dtmf tone when dialing
        if (Permissions.checkProfileDtmfToneWhenDialing(appContext, profile, null)) {
            switch (profile._dtmfToneWhenDialing) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_dtmfToneWhenDialing 1");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1);
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_dtmfToneWhenDialing 2");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 0);
                    break;
            }
        }
        // set sound on touch
        if (Permissions.checkProfileSoundOnTouch(appContext, profile, null)) {
            switch (profile._soundOnTouch) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_soundOnTouch 1");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_soundOnTouch 2");
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    break;
            }
        }

        //// setup radio preferences
        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeForRadios()");
        ActivateProfileHelper.executeForRadios(profile, appContext);

        // setup auto-sync
        try {
            boolean _isAutoSync = ContentResolver.getMasterSyncAutomatically();
            boolean _setAutoSync = false;
            switch (profile._deviceAutoSync) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoSync 1");
                    if (!_isAutoSync) {
                        _isAutoSync = true;
                        _setAutoSync = true;
                    }
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoSync 2");
                    if (_isAutoSync) {
                        _isAutoSync = false;
                        _setAutoSync = true;
                    }
                    break;
                case 3:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoSync 3");
                    _isAutoSync = !_isAutoSync;
                    _setAutoSync = true;
                    break;
            }
            if (_setAutoSync)
                ContentResolver.setMasterSyncAutomatically(_isAutoSync);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // screen on permanent
        //if (Permissions.checkProfileScreenTimeout(context, profile, null)) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "setScreenOnPermanent()");
            setScreenOnPermanent(profile, appContext);
        //}

        // screen timeout
        if (Permissions.checkProfileScreenTimeout(appContext, profile, null)) {
            //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (PPApplication.isScreenOn) {
                //Log.d("ActivateProfileHelper.execute","screen on");
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                        public void run() {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "setScreenTimeout()");
                            setScreenTimeout(profile._deviceScreenTimeout, appContext);
                        }
                    });
                }// else
                //    setScreenTimeout(profile._deviceScreenTimeout);
            }
            else {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "setActivatedProfileScreenTimeout()");
                setActivatedProfileScreenTimeout(appContext, profile._deviceScreenTimeout);
            }
        }
        //else
        //    PPApplication.setActivatedProfileScreenTimeout(context, 0);

        // on/off lock screen
        //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard");
        boolean setLockScreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceKeyguard 1");
                // enable lock screen
                //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard=ON");
                setLockScreenDisabled(appContext, false);
                setLockScreen = true;
                break;
            case 2:
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceKeyguard 2");
                // disable lock screen
                //PPApplication.logE("$$$ ActivateProfileHelper.execute","keyguard=OFF");
                setLockScreenDisabled(appContext, true);
                setLockScreen = true;
                break;
        }
        if (setLockScreen) {
            //boolean isScreenOn;
            //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            //if (pm != null) {
                //PPApplication.logE("$$$ ActivateProfileHelper.execute", "isScreenOn=" + PPApplication.isScreenOn);
                boolean keyguardShowing;
                KeyguardManager kgMgr = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardShowing = kgMgr.isKeyguardLocked();
                    //PPApplication.logE("$$$ ActivateProfileHelper.execute", "keyguardShowing=" + keyguardShowing);

                    if (PPApplication.isScreenOn && !keyguardShowing) {
                        try {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "switch keyguard");
                            // start PhoneProfilesService
                            //PPApplication.firstStartServiceStarted = false;
                            /*Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                            PPApplication.startPPService(context, serviceIntent);*/
                            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            commandIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                            PPApplication.runCommand(appContext, commandIntent);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
            //}
        }

        // setup display brightness
        if (Permissions.checkProfileScreenBrightness(appContext, profile, null)) {
            if (profile.getDeviceBrightnessChange()) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("----- ActivateProfileHelper.execute", "set brightness: profile=" + profile._name);
                    PPApplication.logE("----- ActivateProfileHelper.execute", "set brightness: _deviceBrightness=" + profile._deviceBrightness);
                }*/
                try {
                    if (profile.getDeviceBrightnessAutomatic()) {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set automatic brightness");
                        Settings.System.putInt(appContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        if (profile.getDeviceBrightnessChangeLevel()) {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set brightness 1");
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, null, true, appContext).allowed
                                    == PreferenceAllowed.PREFERENCE_ALLOWED) {

                                Settings.System.putInt(appContext.getContentResolver(),
                                        Settings.System.SCREEN_BRIGHTNESS,
                                        profile.getDeviceBrightnessManualValue(appContext));

                                /*if (android.os.Build.VERSION.SDK_INT < 23) {   // Not working in Android M (exception)
                                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set adaptive brightness 1");
                                    Settings.System.putFloat(appContext.getContentResolver(),
                                            ADAPTIVE_BRIGHTNESS_SETTING_NAME,
                                            profile.getDeviceBrightnessAdaptiveValue(appContext));
                                } else*/ {
                                    try {
                                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set adaptive brightness 2");
                                        Settings.System.putFloat(appContext.getContentResolver(),
                                                Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ,
                                                profile.getDeviceBrightnessAdaptiveValue(appContext));
                                    } catch (Exception ee) {
                                        // run service for execute radios
                                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeRootForAdaptiveBrightness()");
                                        ActivateProfileHelper.executeRootForAdaptiveBrightness(profile, appContext);
                                    }
                                }
                            }
                        }
                    } else {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set manual brightness");
                        Settings.System.putInt(appContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        if (profile.getDeviceBrightnessChangeLevel()) {
                            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "set brightness 2");
                            Settings.System.putInt(appContext.getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    profile.getDeviceBrightnessManualValue(appContext));
                        }
                    }

                    /*
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "start BackgroundBrightnessActivity");
                    Intent intent = new Intent(appContext, BackgroundBrightnessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //this is important

                    float brightnessValue;
                    //if (profile.getDeviceBrightnessAutomatic() || (!profile.getDeviceBrightnessChangeLevel()))
                        brightnessValue = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                    //else
                    //    brightnessValue= profile.getDeviceBrightnessManualValue(context) / (float) 255;
                    intent.putExtra(BackgroundBrightnessActivity.EXTRA_BRIGHTNESS_VALUE, brightnessValue);

                    appContext.startActivity(intent);
                    */

                    /*
                    if (PPApplication.brightnessHandler != null) {
                        PPApplication.brightnessHandler.post(new Runnable() {
                            public void run() {
                                PPApplication.logE("ActivateProfileHelper.execute", "brightnessHandler");
                                createBrightnessView(profile, context);
                            }
                        });
                    }// else
                    //    createBrightnessView(context);
                    */
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }

        // setup rotation
        if (Permissions.checkProfileAutoRotation(appContext, profile, null)) {
            switch (profile._deviceAutoRotate) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 1");
                    // set autorotate on
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 6:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 6");
                    // set autorotate off
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 2");
                    // set autorotate off
                    // degree 0
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    break;
                case 3:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 3");
                    // set autorotate off
                    // degree 90
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    break;
                case 4:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 4");
                    // set autorotate off
                    // degree 180
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    break;
                case 5:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_deviceAutoRotate 5");
                    // set autorotate off
                    // degree 270
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    break;
            }
        }

        // set notification led
        if (profile._notificationLed != 0) {
            //if (Permissions.checkProfileNotificationLed(context, profile)) { not needed for Android 6+, because root is required
            switch (profile._notificationLed) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_notificationLed 1");
                    setNotificationLed(appContext, 1);
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_notificationLed 2");
                    setNotificationLed(appContext, 0);
                    break;
            }
            //}
        }

        // setup wallpaper
        if (Permissions.checkProfileWallpaper(appContext, profile, null)) {
            if (profile._deviceWallpaperChange == 1) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeForWallpaper()");
                ActivateProfileHelper.executeForWallpaper(profile, appContext);
            }
        }

        // set power save mode
        ActivateProfileHelper.setPowerSaveMode(profile, appContext);

        if (Permissions.checkProfileLockDevice(appContext, profile, null)) {
            if (profile._lockDevice != 0) {
                boolean keyguardLocked;
                KeyguardManager kgMgr = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardLocked = kgMgr.isKeyguardLocked();
                    //PPApplication.logE("---$$$ ActivateProfileHelper.execute", "keyguardLocked=" + keyguardLocked);
                    if (!keyguardLocked) {
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "lockDevice()");
                        ActivateProfileHelper.lockDevice(profile, appContext);
                    }
                }
            }
        }

        // enable/disable scanners
        if (profile._applicationDisableWifiScanning != 0) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_applicationDisableWifiScanning");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, profile._applicationDisableWifiScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableWifiScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventWifiEnableScanning(appContext);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
            //PPApplication.logE("[RJS] ActivateProfileHelper.execute", "_applicationDisableWifiScanning");
            PPApplication.restartWifiScanner(appContext);
        }
        if (profile._applicationDisableBluetoothScanning != 0) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_applicationDisableBluetoothScanning");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, profile._applicationDisableBluetoothScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableBluetoothScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
            //PPApplication.logE("[RJS] ActivateProfileHelper.execute", "_applicationDisableBluetoothScanning");
            PPApplication.restartBluetoothScanner(appContext);
        }
        if (profile._applicationDisableLocationScanning != 0) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_applicationDisableLocationScanning");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, profile._applicationDisableLocationScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableLocationScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventLocationEnableScanning(appContext);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
            //PPApplication.logE("[RJS] ActivateProfileHelper.execute", "_applicationDisableLocationScanning");
            PPApplication.restartGeofenceScanner(appContext);
        }
        if (profile._applicationDisableMobileCellScanning != 0) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_applicationDisableMobileCellScanning");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, profile._applicationDisableMobileCellScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableMobileCellScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
            //PPApplication.logE("[RJS] ActivateProfileHelper.execute", "_applicationDisableMobileCellScanning");
            PPApplication.restartPhoneStateScanner(appContext);
        }
        if (profile._applicationDisableOrientationScanning != 0) {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_applicationDisableOrientationScanning");
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, profile._applicationDisableOrientationScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableOrientationScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventOrientationEnableScanning(appContext);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
            //PPApplication.logE("[RJS] ActivateProfileHelper.execute", "_applicationDisableOrientationScanning");
            PPApplication.restartOrientationScanner(appContext);
        }

        // set heads-up notifications
        if (profile._headsUpNotifications != 0) {
            switch (profile._headsUpNotifications) {
                case 1:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_headsUpNotifications 1");
                    setHeadsUpNotifications(appContext, 1);
                    break;
                case 2:
                    //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_headsUpNotifications 2");
                    setHeadsUpNotifications(appContext, 0);
                    break;
            }
        }

        // set screen dark mode
        if (profile._screenDarkMode != 0) {
            setScreenDarkMode(context, profile._screenDarkMode);
        }

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            // set always on display
            if (profile._alwaysOnDisplay != 0) {
                switch (profile._alwaysOnDisplay) {
                    case 1:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_alwaysOnDisplay 1");
                        setAlwaysOnDisplay(appContext, 1);
                        break;
                    case 2:
                        //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "_alwaysOnDisplay 2");
                        setAlwaysOnDisplay(appContext, 0);
                        break;
                }
            }
        }

        // close all applications
        if (profile._deviceCloseAllApplications == 1) {
            if (!PPApplication.blockProfileEventActions) {
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "start work for close all applications");
                // work for first start events or activate profile on boot
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG)
                                .setInitialDelay(200, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] ActivateProfileHelper.execute", "for=" + MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

                            workManager.enqueueUniqueWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }

        if (profile._deviceForceStopApplicationChange == 1) {
            boolean enabled;
            if (Build.VERSION.SDK_INT >= 28)
                enabled = PPPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_5_1_3_1);
            else
                enabled = PPPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_3_0);
            if (enabled) {
                // executeForInteractivePreferences() is called from broadcast receiver PPPExtenderBroadcastReceiver
                //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeForForceStopApplications()");
                ActivateProfileHelper.executeForForceStopApplications(profile, appContext);
            }
        }
        else {
            //PPApplication.logE("[ACTIVATOR] ActivateProfileHelper.execute", "executeForInteractivePreferences()");
            executeForInteractivePreferences(profile, appContext);
        }
    }

    private static void showNotificationForInteractiveParameters(Context context, String title, String text, Intent intent, int notificationId, String notificationTag) {
        Context appContext = context.getApplicationContext();

        String nTitle = title;
        String nText = text;
        if (android.os.Build.VERSION.SDK_INT < 24) {
            nTitle = appContext.getString(R.string.ppp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createInformationNotificationChannel(appContext);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(appContext, PPApplication.INFORMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        PendingIntent pi = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        notification.vibrate = null;
        notification.defaults &= ~DEFAULT_VIBRATE;

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(notificationTag, notificationId, notification);
        } catch (Exception e) {
            //Log.e("ActivateProfileHelper.showNotificationForInteractiveParameters", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void setScreenTimeout(int screenTimeout, Context context) {
        //PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "xxx");

        Context appContext = context.getApplicationContext();

        disableScreenTimeoutInternalChange = true;
        //Log.d("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        switch (screenTimeout) {
            case 1:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 15000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                break;
            case 2:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 30000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                break;
            case 3:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 60000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                break;
            case 4:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 120000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                break;
            case 5:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 600000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                break;
            /*case 6:
                //2147483647 = Integer.MAX_VALUE
                //18000000   = 5 hours
                //86400000   = 24 hours
                //43200000   = 12 hours
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 86400000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000);
                break;*/
            case 7:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 300000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                break;
            /*case 8:
                removeScreenTimeoutAlwaysOnView(context);
                if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock = 86400000; //1800000;
                else
                    createScreenTimeoutAlwaysOnView(appContext);
                break;*/
            case 9:
                //removeScreenTimeoutAlwaysOnView(context);
                if (/*(PhoneProfilesService.getInstance() != null) &&*/ (PPApplication.lockDeviceActivity != null))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutBeforeDeviceLock = 1800000;
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1800000);
                break;
        }
        setActivatedProfileScreenTimeout(appContext, 0);

        OneTimeWorkRequest disableInternalChangeWorker =
                new OneTimeWorkRequest.Builder(DisableScreenTimeoutInternalChangeWorker.class)
                        .addTag(DisableScreenTimeoutInternalChangeWorker.WORK_TAG)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                    //if (PPApplication.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[TEST BATTERY] ActivateProfileHelper.setScreenTimeout", "for=" + DisableScreenTimeoutInternalChangeWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                    } catch (Exception ignored) {
//                    }
//                    //}

                    workManager.enqueueUniqueWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        /*PPApplication.startHandlerThreadInternalChangeToFalse();
        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("ActivateProfileHelper.setScreenTimeout", "disable screen timeout internal change");
                disableScreenTimeoutInternalChange = false;
            }
        }, 3000);*/
        //PostDelayedBroadcastReceiver.setAlarm(
        //        PostDelayedBroadcastReceiver.ACTION_DISABLE_SCREEN_TIMEOUT_INTERNAL_CHANGE_TO_FALSE, 3, context);
    }

    /*private static void createScreenTimeoutAlwaysOnView(Context context)
    {
        removeScreenTimeoutAlwaysOnView(context);

        if (PhoneProfilesService.getInstance() != null) {
            final Context appContext = context.getApplicationContext();

            // Put 30 minutes screen timeout. Required for SettingsContentObserver.OnChange to call removeScreenTimeoutAlwaysOnView
            // when user change system setting.
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000);

            WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                int type;
                //if (android.os.Build.VERSION.SDK_INT < 25)
                //    type = WindowManager.LayoutParams.TYPE_TOAST;
                //else
                if (android.os.Build.VERSION.SDK_INT < 26)
                    type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                else
                    type = LayoutParams.TYPE_APPLICATION_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        PixelFormat.TRANSLUCENT
                );
//                if (android.os.Build.VERSION.SDK_INT < 17)
//                    params.gravity = Gravity.RIGHT | Gravity.TOP;
//                else
//                    params.gravity = Gravity.END | Gravity.TOP;
                PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView = new BrightnessView(appContext);
                try {
                    windowManager.addView(PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView, params);
                } catch (Exception e) {
                    PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView = null;
                }
            }
        }
    }

    static void removeScreenTimeoutAlwaysOnView(Context context)
    {
        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView != null) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView);
                    } catch (Exception ignored) {
                    }
                    PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView = null;
                }
            }
        }
    }
    */

    /*
    @SuppressLint("RtlHardcoded")
    private static void createBrightnessView(Profile profile, Context context)
    {
        PPApplication.logE("ActivateProfileHelper.createBrightnessView", "xxx");

        if (PhoneProfilesService.getInstance() != null) {
            final Context appContext = context.getApplicationContext();

            WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                if (PhoneProfilesService.getInstance().brightnessView != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                    } catch (Exception ignored) {
                    }
                    PhoneProfilesService.getInstance().brightnessView = null;
                }
                int type;
                //if (android.os.Build.VERSION.SDK_INT < 25)
                //    type = WindowManager.LayoutParams.TYPE_TOAST;
                //else
                if (android.os.Build.VERSION.SDK_INT < 26)
                    type = LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                else
                    type = LayoutParams.TYPE_APPLICATION_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        type,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT
                );
                if (profile.getDeviceBrightnessAutomatic() || (!profile.getDeviceBrightnessChangeLevel()))
                    params.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                else
                    params.screenBrightness = profile.getDeviceBrightnessManualValue(appContext) / (float) 255;
                PhoneProfilesService.getInstance().brightnessView = new BrightnessView(appContext);
                try {
                    windowManager.addView(PhoneProfilesService.getInstance().brightnessView, params);
                } catch (Exception e) {
                    PhoneProfilesService.getInstance().brightnessView = null;
                }

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("ActivateProfileHelper.createBrightnessView", "remove brightness view");

                        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (windowManager != null) {
                            if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().brightnessView != null)) {
                                try {
                                    windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                                } catch (Exception ignored) {
                                }
                                PhoneProfilesService.getInstance().brightnessView = null;
                            }
                        }
                    }
                }, 5000);
//                PostDelayedBroadcastReceiver.setAlarm(PostDelayedBroadcastReceiver.ACTION_REMOVE_BRIGHTNESS_VIEW,5, context);
            }
        }
    }

    static void removeBrightnessView(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().brightnessView != null) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                    } catch (Exception ignored) {
                    }
                    PhoneProfilesService.getInstance().brightnessView = null;
                }
            }
        }
    }
    */

    private static void createKeepScreenOnView(Context context) {
        //removeKeepScreenOnView();

        final Context appContext = context.getApplicationContext();

        /*
        //if (PhoneProfilesService.getInstance() != null) {

        //PhoneProfilesService service = PhoneProfilesService.getInstance();
        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            try {
                Log.e("ActivateProfileHelper.createKeepScreenOnView", "keepScreenOnWakeLock="+PPApplication.keepScreenOnWakeLock);
                if (PPApplication.keepScreenOnWakeLock == null)
                    PPApplication.keepScreenOnWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_createKeepScreenOnView");
            } catch(Exception e) {
                PPApplication.keepScreenOnWakeLock = null;
                Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
            try {
                if ((PPApplication.keepScreenOnWakeLock != null) && (!PPApplication.keepScreenOnWakeLock.isHeld())) {
                    Log.e("ActivateProfileHelper.createKeepScreenOnView", "acquire");
                    PPApplication.keepScreenOnWakeLock.acquire();
                }
            } catch (Exception e) {
                Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
        */

        if (PPApplication.keepScreenOnView != null)
            removeKeepScreenOnView(context);
        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            int type;
            //if (android.os.Build.VERSION.SDK_INT < 25)
            //    type = WindowManager.LayoutParams.TYPE_TOAST;
            //else
            if (android.os.Build.VERSION.SDK_INT < 26)
                type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            else
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    1, 1,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON/* |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | // deprecated in API level 26
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | // deprecated in API level 27
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON // deprecated in API level 27*/
                    , PixelFormat.TRANSLUCENT
            );
            //if (android.os.Build.VERSION.SDK_INT < 17)
            //    params.gravity = Gravity.RIGHT | Gravity.TOP;
            //else
            //    params.gravity = Gravity.END | Gravity.TOP;
            PPApplication.keepScreenOnView = new BrightnessView(appContext);
            try {
                windowManager.addView(PPApplication.keepScreenOnView, params);
            } catch (Exception e) {
                PPApplication.keepScreenOnView = null;
            }
        }
    }

    static void removeKeepScreenOnView(Context context)
    {
        final Context appContext = context.getApplicationContext();

        //if (PhoneProfilesService.getInstance() != null) {
            //final Context appContext = context.getApplicationContext();

            //PhoneProfilesService service = PhoneProfilesService.getInstance();

            /*try {
                Log.e("ActivateProfileHelper.removeKeepScreenOnView", "keepScreenOnWakeLock="+PPApplication.keepScreenOnWakeLock);
                if ((PPApplication.keepScreenOnWakeLock != null) && PPApplication.keepScreenOnWakeLock.isHeld())
                    PPApplication.keepScreenOnWakeLock.release();
            } catch (Exception e) {
                Log.e("ActivateProfileHelper.removeKeepScreenOnView", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
            PPApplication.keepScreenOnWakeLock = null;*/

            if (PPApplication.keepScreenOnView != null) {
                WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PPApplication.keepScreenOnView);
                    } catch (Exception ignored) {
                    }
                    PPApplication.keepScreenOnView = null;
                }
            }
        //}
    }

    static boolean isAirplaneMode(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getApplicationContext().getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
        //else
        //    return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private static void setAirplaneMode(boolean mode)
    {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
            // device is rooted
            synchronized (PPApplication.rootMutex) {
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
                Command command = new Command(0, true, command1, command2);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    PPApplication.commandWait(command, "ActivateProfileHelper.setAirplaneMode");
                } catch (Exception e) {
                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                    //Log.e("ActivateProfileHelper.setAirplaneMode", Log.getStackTraceString(e));
                    //PPApplication.recordException(e);
                }
                //PPApplication.logE("ActivateProfileHelper.setAirplaneMode", "done");
            }
        }
    }

    static boolean isMobileData(Context context)
    {
        Context appContext = context.getApplicationContext();

        /*
        if (android.os.Build.VERSION.SDK_INT < 21)
        {
            ConnectivityManager connectivityManager = null;
            try {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception ignored) {
                // java.lang.NullPointerException: missing IConnectivityManager
                // Dual SIM?? Bug in Android ???
            }
            if (connectivityManager != null) {
                try {
                    final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                    final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else*/
        /*if (android.os.Build.VERSION.SDK_INT < 22)
        {
            Method getDataEnabledMethod;
            Class<?> telephonyManagerClass;
            Object ITelephonyStub;
            Class<?> ITelephonyClass;

            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephonyMethod.setAccessible(true);
                    ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                    if (ITelephonyStub != null) {
                        ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                        getDataEnabledMethod = ITelephonyClass.getDeclaredMethod("getDataEnabled");

                        getDataEnabledMethod.setAccessible(true);

                        //noinspection ConstantConditions
                        return (Boolean) getDataEnabledMethod.invoke(ITelephonyStub);
                    }
                    else
                        return false;

                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else*/
        if (android.os.Build.VERSION.SDK_INT < 28)
        {
            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                Method getDataEnabledMethod;
                Class<?> telephonyManagerClass;

                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                    getDataEnabledMethod.setAccessible(true);

                    //noinspection ConstantConditions
                    return (Boolean) getDataEnabledMethod.invoke(telephonyManager);

                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;

        }
        else
            return CmdMobileData.isEnabled(appContext);
    }

    static boolean canSetMobileData(Context context)
    {
        Context appContext = context.getApplicationContext();
        if (android.os.Build.VERSION.SDK_INT >= 28)
            return true;
        else
        //if (android.os.Build.VERSION.SDK_INT >= 22)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                    getDataEnabledMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        /*else
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            Class<?> telephonyManagerClass;

            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephonyMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }*/
        /*else
        {
            ConnectivityManager connectivityManager = null;
            try {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception ignored) {
                // java.lang.NullPointerException: missing IConnectivityManager
                // Dual SIM?? Bug in Android ???
            }
            if (connectivityManager != null) {
                try {
                    final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                    final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }*/
    }

    private static void setMobileData(@SuppressWarnings("unused") Context context, boolean enable)
    {
        //PPApplication.logE("ActivateProfileHelper.setMobileData", "xxx");

        //Context appContext = context.getApplicationContext();

        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.MODIFY_PHONE_STATE
        // not working :-/
        /*if (Permissions.hasPermission(context, Manifest.permission.MODIFY_PHONE_STATE)) {
            if (android.os.Build.VERSION.SDK_INT == 21)
            {
                Method dataConnSwitchMethod;
                Class<?> telephonyManagerClass;
                Object ITelephonyStub;
                Class<?> ITelephonyClass;

                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
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
            }
            else
            {
                Method setDataEnabledMethod;
                Class<?> telephonyManagerClass;

                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    try {
                        telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                        setDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                        setDataEnabledMethod.setAccessible(true);

                        setDataEnabledMethod.invoke(telephonyManager, enable);

                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else*/
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                (PPApplication.isRooted(false)))
        {
            //PPApplication.logE("ActivateProfileHelper.setMobileData", "ask for root enabled and is rooted");

            synchronized (PPApplication.rootMutex) {
                String command1 = "svc data " + (enable ? "enable" : "disable");
                //PPApplication.logE("ActivateProfileHelper.setMobileData", "command=" + command1);
                Command command = new Command(0, false, command1);// {
//                @Override
//                public void commandOutput(int id, String line) {
//                    super.commandOutput(id, line);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","shell output="+line);
//                }
//
//                @Override
//                public void commandTerminated(int id, String reason) {
//                    super.commandTerminated(id, reason);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","terminated="+reason);
//                }
//
//                @Override
//                public void commandCompleted(int id, int exitCode) {
//                    super.commandCompleted(id, exitCode);
//                    PPApplication.logE("ActivateProfileHelper.setMobileData","completed="+exitCode);
//                }
//                };
                try {
                    RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                    PPApplication.commandWait(command, "ActivateProfileHelper.setMobileData");
                    //PPApplication.logE("ActivateProfileHelper.setMobileData", "after wait");
                } catch (Exception e) {
                    //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                }
            }

/*
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            Object serviceManager = PPApplication.getServiceManager("phone");
            int transactionCode = -1;
            if (serviceManager != null) {
                if (Build.VERSION.SDK_INT >= 28)
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setUserDataEnabled");
                else
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setDataEnabled");
            }

            int state = enable ? 1 : 0;

            if (transactionCode != -1) {
                //PPApplication.logE("ActivateProfileHelper.setMobileData", "transactionCode="+transactionCode);

                // Android 5.1?
                //if (Build.VERSION.SDK_INT >= 22) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager)appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        //PPApplication.logE("ActivateProfileHelper.setMobileData", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionList="+subscriptionList);
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
                            //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionList.size()="+subscriptionList.size());
                            for (int i = 0; i < subscriptionList.size(); i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionInfo="+subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                    //PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionId="+subscriptionId);
                                    synchronized (PPApplication.rootMutex) {
                                        String command1 = PPApplication.getServiceCommand("phone", transactionCode, subscriptionId, state);
                                        //PPApplication.logE("ActivateProfileHelper.setMobileData", "command1="+command1);
                                        if (command1 != null) {
                                            Command command = new Command(0, false, command1);
                                            try {
                                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                PPApplication.commandWait(command);
                                            } catch (Exception e) {
                                                // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                                //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                                                //PPApplication.recordException(e);
                                            }
                                        }
                                    }
                                }
                                //else
                                //    PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionInfo == null");
                            }
                        }
                        //else
                        //    PPApplication.logE("ActivateProfileHelper.setMobileData", "subscriptionList == null");
                    }
                    //else
                    //    PPApplication.logE("ActivateProfileHelper.setMobileData", "mSubscriptionManager == null");
//                } else {
//                    synchronized (PPApplication.rootMutex) {
//                        String command1 = PPApplication.getServiceCommand("phone", transactionCode, state);
//                        //PPApplication.logE("ActivateProfileHelper.setMobileData", "command1="+command1);
//                        if (command1 != null) {
//                            Command command = new Command(0, false, command1);
//                            try {
//                                RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
//                                PPApplication.commandWait(command);
//                            } catch (Exception e) {
//                                // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
//                                //og.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
//                                //PPApplication.recordException(e);
//                            }
//                        }
//                    }
//                }
            }
            //else
            //    PPApplication.logE("ActivateProfileHelper.setMobileData", "transactionCode == -1");
 */
        }
    }

    /*
    private int getPreferredNetworkType(Context context) {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                (PPApplication.isRooted()))
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
                        public void commandCompleted(int id, int exitCode) {
                            super.commandCompleted(id, exitCode);
                        }
                    };
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                    }
                }

            } catch(Exception e) {
                Log.e("ActivateProfileHelper.getPreferredNetworkType", Log.getStackTraceString(e));
            }
        }
        else
            networkType = -1;
        return networkType;
    }
    */

    static boolean telephonyServiceExists(/*Context context, */
            @SuppressWarnings("SameParameterValue") String preference) {
        try {
            Object serviceManager = PPApplication.getServiceManager("phone");
            if (serviceManager != null) {
                int transactionCode = -1;
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                    if (Build.VERSION.SDK_INT >= 28)
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setUserDataEnabled");
                    else
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setDataEnabled");
                }
                else
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE))
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setPreferredNetworkType");
                return transactionCode != -1;
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }

    private static void setPreferredNetworkType(Context context, int networkType)
    {
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                (PPApplication.isRooted(false) && PPApplication.serviceBinaryExists(false)))
        {
            if (Permissions.checkPhone(context.getApplicationContext())) {
                try {
                    // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                    Object serviceManager = PPApplication.getServiceManager("phone");
                    int transactionCode = -1;
                    if (serviceManager != null) {
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setPreferredNetworkType");
                    }

                    if (transactionCode != -1) {
                        // Android 5.1?
                        //if (Build.VERSION.SDK_INT >= 22) {
                        Context appContext = context.getApplicationContext();
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(context);
                        if (mSubscriptionManager != null) {
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            } catch (SecurityException e) {
                                PPApplication.recordException(e);
                            }
                            if (subscriptionList != null) {
                                for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                    if (subscriptionInfo != null) {
                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                        synchronized (PPApplication.rootMutex) {
                                            String command1 = PPApplication.getServiceCommand("phone", transactionCode, subscriptionId, networkType);
                                            if (command1 != null) {
                                                Command command = new Command(0, false, command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    PPApplication.commandWait(command, "ActivateProfileHelper.setPreferredNetworkType");
                                                } catch (Exception e) {
                                                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                                                    //PPApplication.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    /*} else {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = PPApplication.getServiceCommand("phone", transactionCode, networkType);
                            if (command1 != null) {
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command);
                                } catch (Exception e) {
                                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                                    //PPApplication.recordException(e);
                                }
                            }
                        }
                    }*/
                    }
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }
            }
        }
    }

    static boolean wifiServiceExists(/*Context context, */
            @SuppressWarnings("SameParameterValue") String preference) {
        try {
            Object serviceManager = PPApplication.getServiceManager("wifi");
            if (serviceManager != null) {
                int transactionCode = -1;
                if (preference.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
                    transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setWifiApEnabled");
                return transactionCode != -1;
            }
            return false;
        } catch(Exception e) {
            //Log.e("ActivateProfileHelper.wifiServiceExists",Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

    private static void setWifiAP(WifiApManager wifiApManager, boolean enable, boolean doNotChangeWifi, Context context) {
        //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-enable="+enable);

        if (Build.VERSION.SDK_INT < 26) {
            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-API < 26");
            wifiApManager.setWifiApState(enable, doNotChangeWifi);
        }
        else
        if (Build.VERSION.SDK_INT < 28) {
            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-API >= 26");
            Context appContext = context.getApplicationContext();
            if (WifiApManager.canExploitWifiTethering(appContext)) {
                if (enable)
                    wifiApManager.startTethering(doNotChangeWifi);
                else
                    wifiApManager.stopTethering();
            }
            else
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                    (PPApplication.isRooted(false) && PPApplication.serviceBinaryExists(false))) {
                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-rooted");
                try {
                    Object serviceManager = PPApplication.getServiceManager("wifi");
                    int transactionCode = -1;
                    if (serviceManager != null) {
                        transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setWifiApEnabled");
                    }
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-serviceManager=" + serviceManager);
                        PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-transactionCode=" + transactionCode);
                    }*/

                    if (transactionCode != -1) {
                        if (enable && (!doNotChangeWifi)) {
                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-wifiManager=" + wifiManager);
                            if (wifiManager != null) {
                                int wifiState = wifiManager.getWifiState();
                                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-isWifiEnabled=" + isWifiEnabled);
                                if (isWifiEnabled) {
                                    //PPApplication.logE("#### setWifiEnabled", "from ActivateProfileHelper.setWifiAP");
                                    wifiManager.setWifiEnabled(false);
                                    PPApplication.sleep(1000);
                                }
                            }
                        }
                        synchronized (PPApplication.rootMutex) {
                            //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-start root command");
                            String command1 = PPApplication.getServiceCommand("wifi", transactionCode, 0, (enable) ? 1 : 0);
                            if (command1 != null) {
                                //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-command1=" + command1);
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command, "ActivateProfileHelper.setWifiAP");
                                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-root command end");
                                } catch (Exception e) {
                                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
                                    //PPApplication.recordException(e);
                                    //PPApplication.logE("$$$ WifiAP", "ActivateProfileHelper.setWifiAP-root command error");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                    //PPApplication.logE("$$$ WifiAP", Log.getStackTraceString(e));
                }
            }
        }
        else {
            if (enable)
                wifiApManager.startTethering(doNotChangeWifi);
            else
                wifiApManager.stopTethering();
        }
    }

    private static void setNFC(Context context, boolean enable)
    {
        /*
        Not working in debug version of application !!!!
        Test with release version.
        */

        Context appContext = context.getApplicationContext();

        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            CmdNfc.setNFC(enable);
        }
        else
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                (PPApplication.isRooted(false))) {
            synchronized (PPApplication.rootMutex) {
                String command1 = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", appContext, enable);
                if (command1 != null) {
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                        PPApplication.commandWait(command, "ActivateProfileHelper.setNFC");
                    } catch (Exception e) {
                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                        //Log.e("ActivateProfileHelper.setNFC", Log.getStackTraceString(e));
                        //PPApplication.recordException(e);
                    }
                }
                //String command = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                //if (command != null)
                //  RootToolsSmall.runSuCommand(command);
            }
        }
    }

    /*
    static boolean canExploitGPS(Context context)
    {
        Context appContext = context.getApplicationContext();
        // test exploiting power manager widget
        PackageManager pacMan = appContext.getPackageManager();
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
        } catch (Exception e) {
            return false; //package not found
        }
        return false;
    }
    */

    private static void setGPS(Context context, boolean enable)
    {
        Context appContext = context.getApplicationContext();

        //boolean isEnabled;
        //int locationMode = -1;
        //if (android.os.Build.VERSION.SDK_INT < 19)
        //    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        /*else {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, -1);
            isEnabled = (locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) ||
                        (locationMode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        }*/

        boolean isEnabled = false;
        boolean ok = true;
        LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        else
            ok = false;
        if (!ok)
            return;

        //PPApplication.logE("ActivateProfileHelper.setGPS", "isEnabled="+isEnabled);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled) && enable)
        {
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                String newSet;
                newSet = "+gps";
                //noinspection deprecation
                Settings.Secure.putString(appContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
            }
            else
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
            {
                // device is rooted
                //PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;

                /*
                String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);
                */
                synchronized (PPApplication.rootMutex) {
                    command1 = "settings put secure location_providers_allowed +gps";
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        PPApplication.commandWait(command, "ActivateProfileHelper.setGPS (1)");
                    } catch (Exception e) {
                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                        //Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                        //PPApplication.recordException(e);
                    }
                }
            }
            /*else
            if (canExploitGPS(appContext))
            {
                //PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                appContext.sendBroadcast(poke);
            }*/
        }
        else
        //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
        if (isEnabled && (!enable))
        {
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                String newSet;// = "";
                newSet = "-gps";
                //noinspection deprecation
                Settings.Secure.putString(appContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
            }
            else
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
            {
                // device is rooted
                //PPApplication.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;

                /*
                String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                PPApplication.logE("ActivateProfileHelper.setGPS", "provider="+provider);
                */
                synchronized (PPApplication.rootMutex) {
                    command1 = "settings put secure location_providers_allowed -gps";
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        PPApplication.commandWait(command, "ActivateProfileHelper.setGPS (2)");
                    } catch (Exception e) {
                        // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                        //Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                        //PPApplication.recordException(e);
                    }
                }
            }
            /*else
            if (canExploitGPS(appContext))
            {
                //PPApplication.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                appContext.sendBroadcast(poke);
            }*/
        }
    }

    private static void setLocationMode(Context context, int mode)
    {
        Context appContext = context.getApplicationContext();

        //PPApplication.logE("ActivateProfileHelper.setLocationMode", "mode="+mode);

        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            Settings.Secure.putInt(appContext.getContentResolver(), Settings.Secure.LOCATION_MODE, mode);
        }
        /*else
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
        {
            // device is rooted - NOT WORKING
            PPApplication.logE("ActivateProfileHelper.setLocationMode", "rooted");

            String command1;

            synchronized (PPApplication.rootMutex) {
                command1 = "settings put secure location_mode " + mode;
                Command command = new Command(0, false, command1);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    PPApplication.commandWait(command, "ActivateProfileHelper.setLocationMode");
                } catch (Exception e) {
                    // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                    //Log.e("ActivateProfileHelper.setLocationMode", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            }
        }*/
    }

    private static void setPowerSaveMode(final Profile profile, Context context) {
        if (profile._devicePowerSaveMode != 0) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadProfileActivation();
            final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, null, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setPowerSaveMode");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setPowerSaveMode");

                            if (powerManager != null) {
                                //PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                boolean _isPowerSaveMode;
                                //if (Build.VERSION.SDK_INT >= 21)
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
                                    if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                        //if (android.os.Build.VERSION.SDK_INT >= 21)
                                            Settings.Global.putInt(appContext.getContentResolver(), "low_power", ((_isPowerSaveMode) ? 1 : 0));
                                    } else if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false))) {
                                        synchronized (PPApplication.rootMutex) {
                                            String command1 = "settings put global low_power " + ((_isPowerSaveMode) ? 1 : 0);
                                            Command command = new Command(0, false, command1);
                                            try {
                                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                PPApplication.commandWait(command, "ActivateProfileHelper.setPowerSaveMode");
                                            } catch (Exception e) {
                                                // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                                //Log.e("ActivateProfileHelper.setPowerSaveMode", Log.getStackTraceString(e));
                                                //PPApplication.recordException(e);
                                            }
                                        }
                                    }
                                }
                            }
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            });
        }
    }

    private static void lockDevice(final Profile profile, Context context) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadProfileActivation();
        final Handler handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //PPApplication.logE("ActivateProfileHelper.lockDevice", "in handler");

                //PPApplication.logE("[TEST_BLOCK_PROFILE_EVENTS_ACTIONS] ActivateProfileHelper.lockDevice", "PPApplication.blockProfileEventActions="+PPApplication.blockProfileEventActions);
                if (PPApplication.blockProfileEventActions)
                    // not lock device after boot
                    return;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("ActivateProfileHelper.lockDevice", "not blocked");
                    PPApplication.logE("ActivateProfileHelper.lockDevice", "profile._lockDevice=" + profile._lockDevice);
                }*/

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_lockDevice");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.lockDevice");

                    switch (profile._lockDevice) {
                        case 1:
                            if (PhoneProfilesService.getInstance() != null) {
                                if (Permissions.checkLockDevice(appContext) && (PPApplication.lockDeviceActivity == null)) {
                                    try {
                                        Intent intent = new Intent(appContext, LockDeviceActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        appContext.startActivity(intent);
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                            }
                            break;
                        case 2:
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (PPApplication.isRooted(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    /*String command1 = "input keyevent 26";
                                    Command command = new Command(0, false, command1);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        commandWait(command);
                                    } catch (RootDeniedException e) {
                                        PPApplication.rootMutex.rootGranted = false;
                                        Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                    } catch (Exception e) {
                                        Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                    }*/
                                    String command1 = PPApplication.getJavaCommandFile(CmdGoToSleep.class, "power", appContext, 0);
                                    if (command1 != null) {
                                        Command command = new Command(0, false, command1);
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                                            PPApplication.commandWait(command, "ActivateProfileHelper.lockDevice");
                                        } catch (Exception e) {
                                            // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                            //Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                            //CPPApplication.recordException(e);
                                        }
                                    }
                                }
                            }
                            /*if ((!ApplicationPreferences.applicationNeverAskForGrantRoot(context)) &&
                                    (PPApplication.isRooted() && PPApplication.serviceBinaryExists())) {
                                synchronized (PPApplication.rootMutex) {
                                    try {
                                        // Get the value of the "TRANSACTION_goToSleep" field.
                                        String transactionCode = PPApplication.getTransactionCode("android.os.IPowerManager", "TRANSACTION_goToSleep");
                                        String command1 = "service call power " + transactionCode + " i64 " + SystemClock.uptimeMillis();
                                        Command command = new Command(0, false, command1);
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            commandWait(command);
                                        } catch (RootDeniedException e) {
                                            PPApplication.rootMutex.rootGranted = false;
                                            Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                        } catch (Exception e) {
                                            Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                        }
                                    } catch(Exception ignored) {
                                    }
                                }
                            */
                            break;
                        case 3:
                            Intent intent = new Intent(PPApplication.ACTION_LOCK_DEVICE);
                            appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                            break;
                    }
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

    private static void setScreenDarkMode(Context appContext, final int value) {
        //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "xxx");
        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, null, false, appContext).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (Build.VERSION.SDK_INT >= 29) {
                //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "allowed");

                if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "G1 granted");

                    try {
                        //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "value="+value);
                        // Android Q (Tasker: https://www.reddit.com/r/tasker/comments/d2ngcl/trigger_android_10_dark_theme_with_brightness/)
                        if (value == 1)
                            Settings.Secure.putInt(appContext.getContentResolver(), "ui_night_mode", 2);
                        else
                            Settings.Secure.putInt(appContext.getContentResolver(), "ui_night_mode", 1);
                    }
                    catch (Exception e2) {
                        //Log.e("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(e2));
                        PPApplication.recordException(e2);
                    }
                }
                else {
                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                            (PPApplication.isRooted(false))) {
                        //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "root granted");

                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put secure ui_night_mode ";
                            if (value == 1)
                                command1 = command1 + "2";
                            else
                                command1 = command1 + "1";
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenDarkMode");
                            } catch (Exception ee) {
                                // com.stericson.RootShell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(ee));
                                //PPApplication.recordException(e);
                            }
                        }
                    }
                }
                // switch car mode on and off is required !!!
                // but how when car mode is on by device? Opposite switch?
                UiModeManager uiModeManager = (UiModeManager) appContext.getSystemService(Context.UI_MODE_SERVICE);
                //PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "uiModeManager=" + uiModeManager);
                if (uiModeManager != null) {
                    if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_NORMAL) {
                        uiModeManager.enableCarMode(0);
                        PPApplication.sleep(200);
                        uiModeManager.disableCarMode(0);
                    }
                    else
                    if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
                        uiModeManager.disableCarMode(0);
                        PPApplication.sleep(200);
                        uiModeManager.enableCarMode(0);
                    }
                }
            }

            /*if (Build.VERSION.SDK_INT > 26) {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    try {
                        // Not working in Samsung S8 :-(
                        // this not change gui to dark, this is blue filter (???)
                        PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "(G1)");
                        if (value == 1)
                            Settings.Global.putInt(context.getContentResolver(), "night_mode_enabled", 1);
                        else
                            Settings.Global.putInt(context.getContentResolver(), "night_mode_enabled", 0);
                        // Android Q (Tasker: https://www.reddit.com/r/tasker/comments/d2ngcl/trigger_android_10_dark_theme_with_brightness/)
                        if (value == 1)
                            Settings.Secure.putInt(context.getContentResolver(), "ui_night_mode", 2);
                        else
                            Settings.Secure.putInt(context.getContentResolver(), "ui_night_mode", 1);
                    }
                    catch (Exception e2) {
                        PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(e2));
                    }
                }
            }
            else {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
                PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "uiModeManager=" + uiModeManager);
                if (uiModeManager != null) {
                    switch (value) {
                        case 1:
                            //uiModeManager.enableCarMode(0);
                            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                            break;
                        case 2:
                            //uiModeManager.enableCarMode(0);
                            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                            break;
                        case 3:
                            //uiModeManager.disableCarMode(0);
                            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                            break;
                    }
                    PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "currentModeType=" + uiModeManager.getCurrentModeType());
                    PPApplication.logE("ActivateProfileHelper.setScreenDarkMode", "nightMode=" + uiModeManager.getNightMode());
                }
            }*/
        }
    }

    static void getRingerVolume(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefRingerVolume = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_RINGER_VOLUME, -999);
            //return prefRingerVolume;
        }
    }
    static void setRingerVolume(Context context, int volume)
    {
        synchronized (PPApplication.profileActivationMutex) {
            //PPApplication.logE("ActivateProfileHelper.(s)setRingerVolume","volume="+volume);
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_RINGER_VOLUME, volume);
            editor.apply();
            ApplicationPreferences.prefRingerVolume = volume;
        }
    }

    static void getNotificationVolume(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefNotificationVolume = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_NOTIFICATION_VOLUME, -999);
            //return prefNotificationVolume;
        }
    }
    static void setNotificationVolume(Context context, int volume)
    {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
            editor.apply();
            ApplicationPreferences.prefNotificationVolume = volume;
        }
    }

    // called only from PPApplication.loadProfileActivationData()
    static void getRingerMode(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefRingerMode = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_RINGER_MODE, 0);
            //return prefRingerMode;
        }
    }
    static void saveRingerMode(Context context, int mode)
    {
        synchronized (PPApplication.profileActivationMutex) {
            //PPApplication.logE("ActivateProfileHelper.(s)saveRingerMode","mode="+mode);
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_RINGER_MODE, mode);
            editor.apply();
            ApplicationPreferences.prefRingerMode = mode;
        }
    }

    // called only from PPApplication.loadProfileActivationData()
    static void getZenMode(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefZenMode = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_ZEN_MODE, 0);
            //return prefZenMode;
        }
    }
    static void saveZenMode(Context context, int mode)
    {
        synchronized (PPApplication.profileActivationMutex) {
            //PPApplication.logE("ActivateProfileHelper.(s)saveZenMode","mode="+mode);
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_ZEN_MODE, mode);
            editor.apply();
            ApplicationPreferences.prefZenMode = mode;
        }
    }

    static void getLockScreenDisabled(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefLockScreenDisabled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_LOCKSCREEN_DISABLED, false);
            //return prefLockScreenDisabled;
        }
    }
    static void setLockScreenDisabled(Context context, boolean disabled)
    {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_LOCKSCREEN_DISABLED, disabled);
            editor.apply();
            ApplicationPreferences.prefLockScreenDisabled = disabled;
        }
    }

    static void getActivatedProfileScreenTimeout(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefActivatedProfileScreenTimeout = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, 0);
            //return prefActivatedProfileScreenTimeout;
        }
    }
    static void setActivatedProfileScreenTimeout(Context context, int timeout)
    {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT, timeout);
            editor.apply();
            ApplicationPreferences.prefActivatedProfileScreenTimeout = timeout;
        }
    }

}
