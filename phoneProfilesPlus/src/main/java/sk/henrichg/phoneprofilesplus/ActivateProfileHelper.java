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
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.palette.graphics.Palette;

import com.android.internal.telephony.ITelephony;
import com.noob.noobcameraflash.managers.NoobCameraManager;
import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class ActivateProfileHelper {

    static volatile boolean disableScreenTimeoutInternalChange = false;
    static volatile boolean brightnessDialogInternalChange = false;

    // bluetooth calls volume stream
    static final int STREAM_BLUETOOTH_SCO = 6;
    // stream for TTS - from API 24
    //static final int STREAM_TTS = 9;
    // streams for virtual assistant -  from API 30
    // requires android.Manifest.permission.MODIFY_AUDIO_ROUTING
    //static final int STREAM_ASSISTANT = 11;

    //static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    static final int ZENMODE_ALL = 0;
    static final int ZENMODE_PRIORITY = 1;
    static final int ZENMODE_NONE = 2;
    static final int ZENMODE_ALARMS = 3;
    static final int ZENMODE_SILENT = 99;

    static final int SUBSCRIPTRION_VOICE = 1;
    static final int SUBSCRIPTRION_SMS = 2;
    static final int SUBSCRIPTRION_DATA = 3;

    //static final String EXTRA_MERGED_PROFILE = "merged_profile";
    //static final String EXTRA_FOR_PROFILE_ACTIVATION = "for_profile_activation";

    private static final String PREF_RINGER_VOLUME = "ringer_volume";
    private static final String PREF_NOTIFICATION_VOLUME = "notification_volume";
    private static final String PREF_RINGER_MODE = "ringer_mode";
    private static final String PREF_ZEN_MODE = "zen_mode";
    private static final String PREF_LOCKSCREEN_DISABLED = "lockscreenDisabled";
    private static final String PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT_WHEN_SCREEN_OFF = "activated_profile_screen_timeout";
    private static final String PREF_KEEP_SCREEN_ON_PERMANENT = "keep_screen_on_permanent";
    static final String PREF_MERGED_RING_NOTIFICATION_VOLUMES = "merged_ring_notification_volumes";

    //static final int LOCATION_MODE_TOGGLE_OFF_HIGH_ACCURACY = -100;
    //static final int LOCATION_MODE_TOGGLE_BATTERY_SAVING_HIGH_ACCURACY = -110;

    @SuppressLint("MissingPermission")
    private static void doExecuteForRadios(Context context, Profile profile, SharedPreferences executedProfileSharedPreferences)
    {
        if (profile == null)
            return;

        boolean firstSleepCalled = false;

        Context appContext = context.getApplicationContext();

        // switch on/off SIM
        if (Build.VERSION.SDK_INT >= 29) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {

                    if (profile._deviceOnOffSIM1 != 0) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                            //noinspection ConstantConditions
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

                            boolean _setSIM1OnOff = false;
                            boolean _setOn;
                            switch (profile._deviceOnOffSIM1) {
                                case 1:
                                    _setSIM1OnOff = true;
                                    _setOn = true;
                                    break;
                                case 2:
                                    _setSIM1OnOff = true;
                                    _setOn = false;
                                    break;
                                default:
                                    _setOn = true;
                            }
                            if (_setSIM1OnOff) {
                                setSIMOnOff(appContext, _setOn, 1);
                                GlobalUtils.sleep(200);
                            }
                        }
                    }
                    if (profile._deviceOnOffSIM2 != 0) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

                            //boolean _isSIM2On = isSIMOn(appContext, 2);
                            boolean _setSIM2OnOff = false;
                            boolean _setOn;
                            switch (profile._deviceOnOffSIM2) {
                                case 1:
                                    _setSIM2OnOff = true;
                                    _setOn = true;
                                    break;
                                case 2:
                                    _setSIM2OnOff = true;
                                    _setOn = false;
                                    break;
                                default:
                                    _setOn = true;
                            }
                            if (_setSIM2OnOff) {
                                setSIMOnOff(appContext, _setOn, 2);
                                GlobalUtils.sleep(200);
                            }
                        }
                    }

                }
            }
        }

        // change default SIM
        //if (Build.VERSION.SDK_INT >= 26) {
            if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (!firstSleepCalled) {
                        GlobalUtils.sleep(300);
                        firstSleepCalled = true;
                    }

//                    PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.doExecuteForRadios", "profile._deviceDefaultSIMCards="+profile._deviceDefaultSIMCards);
                    String[] splits = profile._deviceDefaultSIMCards.split("\\|");
                    if (splits.length == 3) {
                        try {
                            String voice = splits[0];
                            if (!voice.equals("0")) {
//                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.doExecuteForRadios", "voice value="+Integer.parseInt(voice));
                                setDefaultSimCard(context, SUBSCRIPTRION_VOICE, Integer.parseInt(voice));
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        try {
                            String sms = splits[1];
                            if (!sms.equals("0")) {
//                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.doExecuteForRadios", "sms value="+Integer.parseInt(sms));
                                setDefaultSimCard(context, SUBSCRIPTRION_SMS, Integer.parseInt(sms));
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        try {
                            String data = splits[2];
                            if (!data.equals("0")) {
//                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.doExecuteForRadios", "data value="+Integer.parseInt(data));
                                setDefaultSimCard(context, SUBSCRIPTRION_DATA, Integer.parseInt(data));
                            }
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            }
        //}

        // setup network type
        // in array.xml, networkTypeGSMValues are 100+ values
        if (profile._deviceNetworkType >= 100) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

                // in array.xml, networkTypeGSMValues are 100+ values
                setPreferredNetworkType(appContext, profile._deviceNetworkType - 100, 0);
                GlobalUtils.sleep(200);
            }
        }
        //if (Build.VERSION.SDK_INT >= 26) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    if (profile._deviceNetworkTypeSIM1 >= 100) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

                            // in array.xml, networkTypeGSMValues are 100+ values
                            setPreferredNetworkType(appContext, profile._deviceNetworkTypeSIM1 - 100, 1);
                            GlobalUtils.sleep(200);
                        }
                    }
                    if (profile._deviceNetworkTypeSIM2 >= 100) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

                            // in array.xml, networkTypeGSMValues are 100+ values
                            setPreferredNetworkType(appContext, profile._deviceNetworkTypeSIM2 - 100, 2);
                            GlobalUtils.sleep(200);
                        }
                    }
                }
            }
        //}

        // setup mobile data
        if (profile._deviceMobileData != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

                boolean _isMobileData = isMobileData(appContext, 0);
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
                    setMobileData(appContext, _isMobileData, 0);
                    GlobalUtils.sleep(200);
                }
            }
        }
/*
        //if (Build.VERSION.SDK_INT >= 26) {
            //final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    if (profile._deviceMobileDataSIM1 != 0) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM1");
                            boolean _isMobileData = isMobileData(appContext, 1);
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios","_isMobileData="+_isMobileData);
                            boolean _setMobileData = false;
                            switch (profile._deviceMobileDataSIM1) {
                                case 1:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM1 1");
                                    if (!_isMobileData) {
                                        _isMobileData = true;
                                        _setMobileData = true;
                                    }
                                    break;
                                case 2:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM1 2");
                                    if (_isMobileData) {
                                        _isMobileData = false;
                                        _setMobileData = true;
                                    }
                                    break;
                                case 3:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM1 3");
                                    _isMobileData = !_isMobileData;
                                    _setMobileData = true;
                                    break;
                            }
                            if (_setMobileData) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "setMobileData()");
                                setMobileData(appContext, _isMobileData, 1);
                                GlobalUtils.sleep(200);
                            }
                        }
                    }
                    if (profile._deviceMobileDataSIM2 != 0) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            if (!firstSleepCalled) {
                                GlobalUtils.sleep(300);
                                firstSleepCalled = true;
                            }

//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM2");
                            boolean _isMobileData = isMobileData(appContext, 2);
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios","_isMobileData="+_isMobileData);
                            boolean _setMobileData = false;
                            switch (profile._deviceMobileDataSIM2) {
                                case 1:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM2 1");
                                    if (!_isMobileData) {
                                        _isMobileData = true;
                                        _setMobileData = true;
                                    }
                                    break;
                                case 2:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM2 2");
                                    if (_isMobileData) {
                                        _isMobileData = false;
                                        _setMobileData = true;
                                    }
                                    break;
                                case 3:
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "_deviceMobileDataSIM2 3");
                                    _isMobileData = !_isMobileData;
                                    _setMobileData = true;
                                    break;
                            }
                            if (_setMobileData) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.doExecuteForRadios", "setMobileData()");
                                setMobileData(appContext, _isMobileData, 2);
                                GlobalUtils.sleep(200);
                            }
                        }
                    }

                }
            }
        //}
*/
        // setup WiFi AP
        boolean canChangeWifi = true;
        if (profile._deviceWiFiAP != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

                if (Build.VERSION.SDK_INT < 30) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(appContext);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                    if (wifiApManager != null) {
                        boolean setWifiAPState = false;
                        boolean doNotChangeWifi = false;
                        boolean isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                        switch (profile._deviceWiFiAP) {
                            case 1:
                            case 4:
                                if (!isWifiAPEnabled) {
                                    isWifiAPEnabled = true;
                                    setWifiAPState = true;
                                    doNotChangeWifi = profile._deviceWiFiAP == 4;
                                    canChangeWifi = profile._deviceWiFiAP == 4;
                                }
                                break;
                            case 2:
                                if (isWifiAPEnabled) {
                                    isWifiAPEnabled = false;
                                    setWifiAPState = true;
                                    //noinspection ConstantConditions
                                    canChangeWifi = true;
                                }
                                break;
                            case 3:
                            case 5:
                                isWifiAPEnabled = !isWifiAPEnabled;
                                setWifiAPState = true;
                                doNotChangeWifi = profile._deviceWiFiAP == 5;
                                if (doNotChangeWifi)
                                    //noinspection ConstantConditions
                                    canChangeWifi = true;
                                else
                                    canChangeWifi = !isWifiAPEnabled;
                                break;
                        }
                        if (setWifiAPState) {
                            setWifiAP(wifiApManager, isWifiAPEnabled, doNotChangeWifi, profile, appContext);
                            GlobalUtils.sleep(1000);
                        }
                    }
                }
                else {
                    boolean setWifiAPState = false;
                    boolean doNotChangeWifi = false;
                    boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabledA30(context);
                    switch (profile._deviceWiFiAP) {
                        case 1:
                        case 4:
                            if (!isWifiAPEnabled) {
                                isWifiAPEnabled = true;
                                setWifiAPState = true;
                                doNotChangeWifi = profile._deviceWiFiAP == 4;
                                canChangeWifi = profile._deviceWiFiAP == 4;
                            }
                            break;
                        case 2:
                            if (isWifiAPEnabled) {
                                isWifiAPEnabled = false;
                                setWifiAPState = true;
                                //noinspection ConstantConditions
                                canChangeWifi = true;
                            }
                            break;
                        case 3:
                        case 5:
                            isWifiAPEnabled = !isWifiAPEnabled;
                            setWifiAPState = true;
                            doNotChangeWifi = profile._deviceWiFiAP == 5;
                            if (doNotChangeWifi)
                                //noinspection ConstantConditions
                                canChangeWifi = true;
                            else
                                canChangeWifi = !isWifiAPEnabled;
                            break;
                    }
                    if (setWifiAPState) {
                        //CmdWifiAP.setWifiAP(isWifiAPEnabled, doNotChangeWifi, context, profile._name);
                        setWifiAP(null, isWifiAPEnabled, doNotChangeWifi, profile, appContext);
                        GlobalUtils.sleep(1000);
                    }
                }
            }
        }

        if (canChangeWifi) {
            // setup Wi-Fi
            if (profile._deviceWiFi != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (!firstSleepCalled) {
                        GlobalUtils.sleep(300);
                        firstSleepCalled = true;
                    }

                    boolean isWifiAPEnabled;
                    if (Build.VERSION.SDK_INT < 30)
                        isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
                    else
                        //isWifiAPEnabled = CmdWifiAP.isEnabled(context);
                        isWifiAPEnabled = WifiApManager.isWifiAPEnabledA30(appContext);
                    if ((!isWifiAPEnabled) || (profile._deviceWiFi >= 4)) { // only when wifi AP is not enabled, change wifi
                        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            int wifiState = wifiManager.getWifiState();
                            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                            boolean setWifiState = false;
                            switch (profile._deviceWiFi) {
                                case 1:
                                case 4:
                                case 6:
                                    if (!isWifiEnabled) {
                                        isWifiEnabled = true;
                                        setWifiState = true;
                                    }
                                    break;
                                case 2:
                                case 7:
                                    if (isWifiEnabled) {
                                        isWifiEnabled = false;
                                        setWifiState = true;
                                    }
                                    break;
                                case 3:
                                case 5:
                                case 8:
                                    isWifiEnabled = !isWifiEnabled;
                                    setWifiState = true;
                                    break;
                            }
                            if (isWifiEnabled) {
                                // when wifi is enabled from profile, no disable wifi after scan
                                WifiScanWorker.setWifiEnabledForScan(appContext, false);
                            }
                            if (setWifiState) {
                                try {
                                    if ((profile._deviceWiFi == 6) ||
                                        (profile._deviceWiFi == 7) ||
                                        (profile._deviceWiFi == 8)) {
                                        setWifiInAirplaneMode(/*appContext,*/ isWifiEnabled);
                                    } else {
                                        //if (Build.VERSION.SDK_INT >= 29)
                                        //    CmdWifi.setWifi(isWifiEnabled);
                                        //else
                                        wifiManager.setWifiEnabled(isWifiEnabled);
                                        //CmdWifi.setWifiEnabled(isWifiAPEnabled);
                                    }
                                } catch (Exception e) {
                                    //WTF?: DOOGEE- X5pro - java.lang.SecurityException: Permission Denial: Enable WiFi requires com.mediatek.permission.CTA_ENABLE_WIFI
                                    //Log.e("ActivateProfileHelper.doExecuteForRadios", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);;
                                    //showError(context, profile._name, Profile.PARAMETER_TYPE_WIFI);
                                    PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_WIFI,
                                            null, profile._name, "");
                                }
                                GlobalUtils.sleep(200);
                            }
                        }
                    }
                }
            }

            // connect to SSID
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    if (!firstSleepCalled) {
                        GlobalUtils.sleep(300);
                        firstSleepCalled = true;
                    }

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
                                    PPApplicationStatic.recordException(e);
                                }
                                if (connManager != null) {
                                    boolean wifiConnected; // = false;
                                    //if (Build.VERSION.SDK_INT < 28) {
                                        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
                                    //noinspection deprecation
                                    wifiConnected = (activeNetwork != null) &&
                                                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                                                activeNetwork.isConnected();
                                    /*}
                                    else {
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
                                                PPApplicationStatic.recordException(ee);
                                            }
                                        }
                                    }*/
                                    WifiInfo wifiInfo = null;
                                    if (wifiConnected)
                                        wifiInfo = wifiManager.getConnectionInfo();

                                    List<WifiConfiguration> list = null;

                                    if (Permissions.hasPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION))
                                        list = wifiManager.getConfiguredNetworks();
                                    if (list != null) {
                                        for (WifiConfiguration i : list) {
                                            //noinspection deprecation
                                            if (i.SSID != null && i.SSID.equals(profile._deviceConnectToSSID)) {
                                                if (wifiConnected) {
                                                    //noinspection deprecation
                                                    if (!wifiInfo.getSSID().equals(i.SSID)) {

                                                        PPApplication.connectToSSIDStarted = true;

                                                        // connected to another SSID
                                                        wifiManager.disconnect();
                                                        //noinspection deprecation
                                                        wifiManager.enableNetwork(i.networkId, true);
                                                        wifiManager.reconnect();
                                                    }
                                                } else
                                                    //noinspection deprecation
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
                PPApplication.connectToSSID = profile._deviceConnectToSSID;
            }
        }

        // setup bluetooth
        if (profile._deviceBluetooth != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
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
                    if (isBluetoothEnabled) {
                        // when bluetooth is enabled from profile, no disable bluetooth after scan
                        BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
                    }
                    if (setBluetoothState) {
                        try {
                            //if (Build.VERSION.SDK_INT >= 26)
                            //    CmdBluetooth.setBluetooth(isBluetoothEnabled);
                            //else {
                                if (isBluetoothEnabled) {
//                                    Log.e("ActivateProfileHelper.doExecuteForRadios", "######## enable bluetooth");
                                    bluetoothAdapter.enable();
                                }
                                else {
//                                    Log.e("ActivateProfileHelper.doExecuteForRadios", "######## disable bluetooth");
                                    bluetoothAdapter.disable();
                                }
                            //}
                        } catch (Exception e) {
                            // WTF?: DOOGEE - X5pro -> java.lang.SecurityException: Permission Denial: Enable bluetooth requires com.mediatek.permission.CTA_ENABLE_BT
                            //Log.e("ActivateProfileHelper.doExecuteForRadios", Log.getStackTraceString(e));
                            //PPApplicationStatic.recordException(e);;
                        }
                    }
                }
            }
        }

        // setup location mode
        if (profile._deviceLocationMode != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

                switch (profile._deviceLocationMode) {
                    case 1:
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        break;
                    case 2:
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        //GlobalUtils.sleep(200);
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
                        break;
                    case 3:
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        //GlobalUtils.sleep(200);
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
                        break;
                    case 4:
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        //GlobalUtils.sleep(200);
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                        break;
                    case 5:
                        //Log.e("ActivateProfileHelper.doExecuteForRadios", "mode=LOCATION_MODE_TOGGLE_OFF_HIGH_ACCURACY");
                        int locationMode = getLocationMode(appContext);
                        //Log.e("ActivateProfileHelper.doExecuteForRadios", "actual locationMode="+locationMode);
                        if (locationMode != Settings.Secure.LOCATION_MODE_OFF)
                            locationMode = Settings.Secure.LOCATION_MODE_OFF;
                        else {
                            locationMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
                            setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                            //GlobalUtils.sleep(200);
                        }
                        setLocationMode(appContext, locationMode);
                        break;
                    case 6:
                        //Log.e("ActivateProfileHelper.doExecuteForRadios", "mode=LOCATION_MODE_TOGGLE_BATTERY_SAVING_HIGH_ACCURACY");
                        locationMode = getLocationMode(appContext);
                        //Log.e("ActivateProfileHelper.doExecuteForRadios", "actual locationMode="+locationMode);
                        if (locationMode != Settings.Secure.LOCATION_MODE_BATTERY_SAVING)
                            locationMode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
                        else
                            locationMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
                        setLocationMode(appContext, Settings.Secure.LOCATION_MODE_OFF);
                        //GlobalUtils.sleep(200);
                        setLocationMode(appContext, locationMode);
                        break;
                }
//                int locationMode = getLocationMode(appContext);
//                Log.e("ActivateProfileHelper.doExecuteForRadios", "actual locationMode="+locationMode);
            }
        }

        // setup GPS
        if (profile._deviceGPS != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    firstSleepCalled = true;
                }

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
                //Log.e("ActivateProfileHelper.doExecuteForRadios", "GPS="+isEnabled);
                if (ok) {
                    switch (profile._deviceGPS) {
                        case 1:
                            setGPS(appContext, true);
                            //setLocationMode(appContext, true);
                            break;
                        case 2:
                            setGPS(appContext, false);
                            //setLocationMode(appContext, false);
                            break;
                        case 3:
                            //Log.e("ActivateProfileHelper.doExecuteForRadios", "TOGGLE");
                            //setLocationMode(appContext, true);
                            //setLocationMode(appContext, false);
                            setGPS(appContext, !isEnabled);
                            break;
                    }
                }
            }
        }

        // setup NFC
        if (profile._deviceNFC != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!firstSleepCalled) {
                    GlobalUtils.sleep(300);
                    //noinspection UnusedAssignment
                    firstSleepCalled = true;
                }

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(appContext);
                if (nfcAdapter != null) {
                    switch (profile._deviceNFC) {
                        case 1:
                            setNFC(appContext, true);
                            break;
                        case 2:
                            setNFC(appContext, false);
                            break;
                        case 3:
                            setNFC(appContext, !nfcAdapter.isEnabled());
                            break;
                    }
                }
            }
        }

    }

    @SuppressWarnings("RedundantArrayCreation")
    static boolean isPPPSetAsDefaultAssistant(Context context) {
        boolean assistIsSet = false;

        ComponentName compName = null;
        try {
            Method declaredMethod = UserHandle.class.getDeclaredMethod("myUserId", new Class[0]);
            declaredMethod.setAccessible(true);
            Integer num = (Integer) declaredMethod.invoke(null, new Object[0]);
            if (num != null) {
                @SuppressLint("PrivateApi")
                Object newInstance = Class.forName("com.android.internal.app.AssistUtils").getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
                Method declaredMethod2 = newInstance.getClass().getDeclaredMethod("getAssistComponentForUser", new Class[]{Integer.TYPE});
                declaredMethod2.setAccessible(true);
                compName = (ComponentName) declaredMethod2.invoke(newInstance, new Object[]{num});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (compName != null && compName.getPackageName().equals(context.getPackageName())) {
            assistIsSet = true;
        }
        if (!assistIsSet) {
            String string = Settings.Secure.getString(context.getContentResolver(), "assistant");
            if ((string != null) && (string.startsWith(context.getPackageName()))) {
                assistIsSet = true;
            }
        }

        return assistIsSet;
    }

    private static void executeForRadios(Profile profile, Context context, SharedPreferences executedProfileSharedPreferences)
    {
        if (profile == null)
            return;

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadRadios();
        //final Handler __handler = new Handler(PPApplication.handlerThreadRadios.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), profile, executedProfileSharedPreferences) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadRadios", "START run - from=ActivateProfileHelper.executeForRadios");

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && (profile != null) && (executedProfileSharedPreferences != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForRadios");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    boolean _isAirplaneMode = false;
                    boolean _setAirplaneMode = false;
                    boolean _useAssistant = false;
                    if (profile._deviceAirplaneMode != 0) {
                        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                            _isAirplaneMode = isAirplaneMode(appContext);
                            switch (profile._deviceAirplaneMode) {
                                case 1:
                                case 4:
                                    if (!_isAirplaneMode) {
                                        _isAirplaneMode = true;
                                        _setAirplaneMode = true;
                                        _useAssistant = profile._deviceAirplaneMode == 4;
                                    }
                                    break;
                                case 2:
                                case 5:
                                    if (_isAirplaneMode) {
                                        _isAirplaneMode = false;
                                        _setAirplaneMode = true;
                                        _useAssistant = profile._deviceAirplaneMode == 5;
                                    }
                                    break;
                                case 3:
                                case 6:
                                    _isAirplaneMode = !_isAirplaneMode;
                                    _setAirplaneMode = true;
                                    _useAssistant = profile._deviceAirplaneMode == 6;
                                    break;
                            }
                        }
                    }
                    if (_setAirplaneMode /*&& _isAirplaneMode*/) {
                        // switch ON airplane mode, set it before doExecuteForRadios
                        setAirplaneMode(context, _isAirplaneMode, _useAssistant);
                        GlobalUtils.sleep(1500);
                    }
                    doExecuteForRadios(appContext, profile, executedProfileSharedPreferences);

                    /*if (_setAirplaneMode && (!_isAirplaneMode)) {
                        // 200 milliseconds is in doExecuteForRadios
                        PPApplication.sleep(1800);

                        // switch OFF airplane mode, set if after executeForRadios
                        setAirplaneMode(context, _isAirplaneMode);
                    }*/

                    //PPApplication.sleep(500);

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
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
        int systemRingerMode = audioManager.getRingerMode();
        //int systemZenMode = getSystemZenMode(context/*, -1*/);

        /*return (audibleRingerMode) ||
                ((systemZenMode == ActivateProfileHelper.ZENMODE_PRIORITY) &&
                        (systemRingerMode != AudioManager.RINGER_MODE_VIBRATE));*/

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
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
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
        if (ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes > 0)
            return ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes == 1;
        else
            return ApplicationPreferences.prefMergedRingNotificationVolumes;
    }
    // test if ring and notification volumes are merged
    static void setMergedRingNotificationVolumes(Context context, /*boolean force,*/ SharedPreferences.Editor editor) {
        synchronized (PPApplication.profileActivationMutex) {

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
//                        Log.e("ActivateProfileHelper.setMergedRingNotificationVolumes", "oldRingVolume="+oldRingVolume);
//                        Log.e("ActivateProfileHelper.setMergedRingNotificationVolumes", "oldNotificationVolume="+oldNotificationVolume);
                        if (oldRingVolume == oldNotificationVolume) {
                            int newNotificationVolume;
                            if (oldNotificationVolume == maximumNotificationValue)
                                newNotificationVolume = oldNotificationVolume - 1;
                            else
                                newNotificationVolume = oldNotificationVolume + 1;

                            EventPreferencesVolumes.internalChange = true;
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newNotificationVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                            GlobalUtils.sleep(2000);

                            int newRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                            newNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
//                            Log.e("ActivateProfileHelper.setMergedRingNotificationVolumes", "newRingVolume="+newRingVolume);
//                            Log.e("ActivateProfileHelper.setMergedRingNotificationVolumes", "newNotificationVolume="+newNotificationVolume);

                            merged = newRingVolume == newNotificationVolume;
                        } else
                            merged = false;
                        EventPreferencesVolumes.internalChange = true;
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, oldNotificationVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                        PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

                        audioManager.setRingerMode(ringerMode);

                        editor.putBoolean(PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                        ApplicationPreferences.prefMergedRingNotificationVolumes = merged;
                    }
                } catch (Exception e) {
                    //PPApplicationStatic.recordException(e);
                }
            //}
        }
    }
    static void setMergedRingNotificationVolumes(Context context/*, boolean force*/) {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            setMergedRingNotificationVolumes(context, /*force,*/ editor);
            editor.apply();
        }
    }

    private static void setVolumes(Context context, Profile profile, AudioManager audioManager, int systemZenMode,
                                   int linkUnlink, boolean forProfileActivation, boolean forRingerMode)
    {
        if (profile == null)
            return;

        Context appContext = context.getApplicationContext();

        int ringerMode = ApplicationPreferences.prefRingerMode;

        if (forProfileActivation) {
            if (profile._volumeMuteSound) {
                if (isAudibleSystemRingerMode(audioManager, systemZenMode) || (ringerMode == 0)) {
                    // WARNING mute.unmute must be called only for audible ringer mode
                    //         change of mute state bad affects silent mode (is not working)

                    if (!audioManager.isStreamMute(AudioManager.STREAM_RING)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (!audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (!audioManager.isStreamMute(AudioManager.STREAM_SYSTEM)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (!audioManager.isStreamMute(AudioManager.STREAM_DTMF)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                }
                if (!audioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            } else {
                if (isAudibleSystemRingerMode(audioManager, systemZenMode) || (ringerMode == 0)) {
                    // WARNING mute.unmute must be called only for audible ringer mode
                    //         change of mute state bad affects silent mode (is not working)

                    if (audioManager.isStreamMute(AudioManager.STREAM_RING)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (audioManager.isStreamMute(AudioManager.STREAM_SYSTEM)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if (audioManager.isStreamMute(AudioManager.STREAM_DTMF)) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                }
                if (audioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            }
        }

        // get mute state before set of all volumes; system stream may set mute to true
        boolean ringMuted = audioManager.isStreamMute(AudioManager.STREAM_RING);
        boolean notificationMuted = audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION);

        if (forRingerMode) {
            // get mute state before set of all volumes; system stream may set mute to true

            if (!profile._volumeMuteSound) {
                if (!ringMuted) {
                    if (profile.getVolumeRingtoneChange()) {
                        if (forProfileActivation) {
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            setRingerVolume(appContext, profile.getVolumeRingtoneValue());
                        }
                    }
                }
                if (!notificationMuted) {
                    if (profile.getVolumeNotificationChange()) {
                        if (forProfileActivation) {
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            setNotificationVolume(appContext, profile.getVolumeNotificationValue());
                        }
                    }
                }
            }
        }

        if (forProfileActivation) {
            //if (Build.VERSION.SDK_INT >= 26) {
                if (profile.getVolumeAccessibilityChange()) {
                    try {
                        //EventPreferencesVolumes.internalChange = true;
                        audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY /* 10 */, profile.getVolumeAccessibilityValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        //Settings.System.putInt(getContentResolver(), Settings.System.STREAM_ACCESSIBILITY, profile.getVolumeAccessibilityValue());
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            //}
        }

        if (forRingerMode) {

            if (!profile._volumeMuteSound) {
                //if (isAudibleRinging(ringerMode, zenMode, true) || (ringerMode == 0)) {
                if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/) || (ringerMode == 0)) {
                    // test only system ringer mode

                    //if (Permissions.checkAccessNotificationPolicy(context)) {

                    if (forProfileActivation) {
                        boolean systemMuted = audioManager.isStreamMute(AudioManager.STREAM_SYSTEM);
                        boolean dtmfMuted = audioManager.isStreamMute(AudioManager.STREAM_DTMF);

                        if (!dtmfMuted) {
                            if (profile.getVolumeDTMFChange()) {
                                synchronized (PPApplication.notUnlinkVolumesMutex) {
                                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                                }
                                try {
                                    //EventPreferencesVolumes.internalChange = true;
                                    audioManager.setStreamVolume(AudioManager.STREAM_DTMF /* 8 */, profile.getVolumeDTMFValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_DTMF, profile.getVolumeDTMFValue());
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                        if (!systemMuted) {
                            if (profile.getVolumeSystemChange()) {
                                synchronized (PPApplication.notUnlinkVolumesMutex) {
                                    RingerModeChangeReceiver.notUnlinkVolumes = false;
                                }
                                try {
                                    //EventPreferencesVolumes.internalChange = true;
                                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM /* 1 */, profile.getVolumeSystemValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                                    //correctVolume0(audioManager);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }

                    boolean volumesSet = false;
                    //TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (/*(telephony != null) &&*/ ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {

                        if ((!ringMuted) && (!notificationMuted)) {

                            //int callState = telephony.getCallState();
                            if ((linkUnlink == PhoneCallsListener.LINKMODE_UNLINK)/* ||
                            (callState == TelephonyManager.CALL_STATE_RINGING)*/) {
                                // for separating ringing and notification
                                // in ringing state ringer volumes must by set
                                // and notification volumes must not by set
                                int volume = ApplicationPreferences.prefRingerVolume;
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        PhoneProfilesService.ringingVolume = volume;
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                        //correctVolume0(audioManager);
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                                volumesSet = true;
                            } else if (linkUnlink == PhoneCallsListener.LINKMODE_LINK) {
                                // for separating ringing and notification
                                // in not ringing state ringer and notification volume must by change
                                int volume = ApplicationPreferences.prefRingerVolume;
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        PhoneProfilesService.ringingVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                                volume = ApplicationPreferences.prefNotificationVolume;
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                                //correctVolume0(audioManager);
                                volumesSet = true;
                            } else if ((linkUnlink == PhoneCallsListener.LINKMODE_NONE)/* ||
                                    (callState == TelephonyManager.CALL_STATE_IDLE)*/) {
                                int volume = ApplicationPreferences.prefRingerVolume;
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        PhoneProfilesService.ringingVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                        //correctVolume0(audioManager);
                                        if (!profile.getVolumeNotificationChange())
                                            setNotificationVolume(appContext, volume);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                                volume = ApplicationPreferences.prefNotificationVolume;
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                        //correctVolume0(audioManager);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
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
                                if (volume != -999) {
                                    try {
                                        //EventPreferencesVolumes.internalChange = true;
                                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION /* 5 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                        //PhoneProfilesService.notificationVolume = volume;
                                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                                        //correctVolume0(audioManager);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                        }
                        if (!ringMuted) {
                            volume = ApplicationPreferences.prefRingerVolume;
                            if (volume != -999) {
                                try {
                                    //EventPreferencesVolumes.internalChange = true;
                                    audioManager.setStreamVolume(AudioManager.STREAM_RING /* 2 */, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    PhoneProfilesService.ringingVolume = volume;
                                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                                    //correctVolume0(audioManager);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (forProfileActivation) {
            if (profile.getVolumeBluetoothSCOChange()) {
                try {
                    //EventPreferencesVolumes.internalChange = true;
                    audioManager.setStreamVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO, profile.getVolumeBluetoothSCOValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            if (profile.getVolumeVoiceChange()) {
                try {
                    //EventPreferencesVolumes.internalChange = true;
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL /* 0 */, profile.getVolumeVoiceValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            if (profile.getVolumeAlarmChange()) {
                try {
                    //EventPreferencesVolumes.internalChange = true;
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM /* 4 */, profile.getVolumeAlarmValue(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
            if (!profile._volumeMuteSound) {
                boolean musicMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
                if (!musicMuted) {
                    if (profile.getVolumeMediaChange()) {
                        setMediaVolume(appContext, audioManager, profile.getVolumeMediaValue(), false, true);
                    }
                }
            }
        }
    }

    static void setMediaVolume(Context context, AudioManager audioManager, int value,
                               boolean allowDuringMusicPlay, boolean setMediaVolumeChanged) {
//        Log.e("ActivateProfileHelper.setMediaVolume", "isMusicActive="+audioManager.isMusicActive());

        if (!allowDuringMusicPlay && audioManager.isMusicActive())
            return;

        // Fatal Exception: java.lang.SecurityException: Only SystemUI can disable the safe media volume:
        // Neither user 10118 nor current process has android.permission.STATUS_BAR_SERVICE.
        try {
            //EventPreferencesVolumes.internalChange = true;
            //if (PPApplication.deviceIsOnePlus)
            //    Settings.System.putInt(context.getContentResolver(), Settings.System.VOLUME_MUSIC, value);
            //else
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//            Log.e("ActivateProfileHelper.setMediaVolume", "audioManager.setStreamVolume");

            if (setMediaVolumeChanged)
                EventPreferencesVolumes.mediaVolumeChangeed = true;
        } catch (SecurityException e) {
            //PPApplicationStatic.recordException(e);

            boolean G1OK = false;
            Context appContext = context.getApplicationContext();
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                try {
                    //EventPreferencesVolumes.internalChange = true;
                    Settings.Global.putInt(appContext.getContentResolver(), "audio_safe_volume_state", 2);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    G1OK = true;

                    if (setMediaVolumeChanged)
                        EventPreferencesVolumes.mediaVolumeChangeed = true;
                }
                catch (Exception e2) {
                    Log.e("ActivateProfileHelper.setMediaVolume", Log.getStackTraceString(e2));
                    //PPApplicationStatic.recordException(e2);
                }
            }
            if (!G1OK) {
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                        (RootUtils.isRooted(false))) {
                    synchronized (PPApplication.rootMutex) {
                        String command1 = "settings put global audio_safe_volume_state 2";
                        Command command = new Command(0, /*false,*/ command1);
                        try {
                            //EventPreferencesVolumes.internalChange = true;
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            RootUtils.commandWait(command, "ActivateProfileHelper.setMediaVolume");
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC /* 3 */, value, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                            if (setMediaVolumeChanged)
                                EventPreferencesVolumes.mediaVolumeChangeed = true;
                        } catch (Exception ee) {
                            // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                            //PPApplicationStatic.recordException(e);;
                        }
                    }
                }
            }
        } catch (Exception e3) {
            PPApplicationStatic.recordException(e3);
        }
    }

    /*
    private static void setZenMode(Context context, int zenMode, AudioManager audioManager, int systemZenMode, int ringerMode)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            Context appContext = context.getApplicationContext();

            //int systemZenMode = getSystemZenMode(appContext); //, -1);
            //int systemRingerMode = audioManager.getRingerMode();

            if ((zenMode != ZENMODE_SILENT) && canChangeZenMode(appContext)) {
                try {
                    if (ringerMode != -1) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        audioManager.setRingerMode(ringerMode);

                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                            } catch (Exception ee) {
                                //PPApplicationStatic.recordException(ee);
                            }
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                            } catch (Exception ee) {
                                //PPApplicationStatic.recordException(ee);
                            }
                        }
                        else {
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                            } catch (Exception ee) {
                                //PPApplicationStatic.recordException(ee);
                            }
                            try {
                                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                            } catch (Exception ee) {
                                //PPApplicationStatic.recordException(ee);
                            }
                        }

                        //if (!((zenMode == ZENMODE_PRIORITY) && (ringerMode == AudioManager.RINGER_MODE_VIBRATE))) {
                            PPApplication.sleep(500);
                        //}
                    }

                    if ((zenMode != systemZenMode) || (zenMode == ZENMODE_PRIORITY)) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                        PPNotificationListenerService.requestInterruptionFilter(appContext, zenMode);
                        InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, zenMode);
                    }

                    //if (zenMode == ZENMODE_PRIORITY) {
                    //    PPApplication.sleep(1000);
                    //    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //    audioManager.setRingerMode(ringerMode);
                    //}
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

            } else {
                try {
                    switch (zenMode) {
                        case ZENMODE_SILENT:
                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //PPApplication.sleep(1000);
                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            if ((systemZenMode != ZENMODE_ALL) && canChangeZenMode(appContext)) {
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
                    PPApplicationStatic.recordException(e);
                }
            }
        //}
        //else {
        //    RingerModeChangeReceiver.notUnlinkVolumes = false;
        //    audioManager.setRingerMode(ringerMode);
        //}
    }
    */

    private static void setVibrateWhenRinging(Context context, Profile profile, int value, SharedPreferences executedProfileSharedPreferences) {
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
            Context appContext = context.getApplicationContext();
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, executedProfileSharedPreferences, false, appContext).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (Permissions.checkVibrateWhenRinging(appContext)) {
                     {
                        try {
                            Settings.System.putInt(appContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, lValue);
                            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                                Settings.System.putInt(appContext.getContentResolver(), "vibrate_in_normal", lValue);
                                Settings.System.putInt(appContext.getContentResolver(), "vibrate_in_silent", lValue);
                            }
                            else
                            if (PPApplication.deviceIsOnePlus) {
                                switch (lValue) {
                                    case 1:
                                        Settings.System.putInt(appContext.getContentResolver(), "ring_vibration_intensity", 2);
                                        break;
                                    case 0:
                                        Settings.System.putInt(appContext.getContentResolver(), "ring_vibration_intensity", 0);
                                        break;
                                }
                            }
                        } catch (Exception ee) {
                            // java.lang.IllegalArgumentException: You cannot change private secure settings.
                            //Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(ee));
                            //PPApplicationStatic.recordException(ee);

                            if (isPPPPutSettingsInstalled(appContext) > 0) {
                                if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                                    putSettingsParameter(context, "system", Settings.System.VIBRATE_WHEN_RINGING, String.valueOf(lValue));
                                    putSettingsParameter(context, "system", "vibrate_in_normal", String.valueOf(lValue));
                                    putSettingsParameter(context, "system", "vibrate_in_silent", String.valueOf(lValue));
                                }
                                else
                                if (PPApplication.deviceIsOnePlus) {
                                    putSettingsParameter(context, "system", Settings.System.VIBRATE_WHEN_RINGING, String.valueOf(lValue));
                                    switch (lValue) {
                                        case 1:
                                            putSettingsParameter(context, "system", "ring_vibration_intensity", "2");
                                            break;
                                        case 0:
                                            putSettingsParameter(context, "system", "ring_vibration_intensity", "0");
                                            break;
                                    }
                                } else {
                                    putSettingsParameter(context, "system", Settings.System.VIBRATE_WHEN_RINGING, String.valueOf(lValue));
                                }
                            } else {
                                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                        (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                    synchronized (PPApplication.rootMutex) {
                                        String command1;
                                        Command command;
                                        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                                            command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                            String command2 = "settings put system " + "vibrate_in_normal" + " " + lValue;
                                            String command3 = "settings put system " + "vibrate_in_silent" + " " + lValue;
                                            command = new Command(0, /*false,*/ command1, command2, command3);
                                        }
                                        else
                                        if (PPApplication.deviceIsOnePlus) {
                                            command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                            String command2 = "";
                                            switch (lValue) {
                                                case 1:
                                                    command2 = "settings put system " + "ring_vibration_intensity" + " " + "2";
                                                    break;
                                                case 0:
                                                    command2 = "settings put system " + "ring_vibration_intensity" + " " + "0";
                                                    break;
                                            }
                                            command = new Command(0, /*false,*/ command1, command2);
                                        } else {
                                            command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + lValue;
                                            //if (PPApplication.isSELinuxEnforcing())
                                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                            command = new Command(0, /*false,*/ command1); //, command2);
                                        }
                                        try {
                                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                            RootUtils.commandWait(command, "ActivateProfileHelper.setVibrationWhenRinging");
                                        } catch (Exception e) {
                                            // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                            //Log.e("ActivateProfileHelper.setVibrateWhenRinging", Log.getStackTraceString(e));
                                            //PPApplicationStatic.recordException(e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setVibrateNotification(Context context, Profile profile, int value, SharedPreferences executedProfileSharedPreferences) {
        int lValue = value;
        if (profile != null) {
            switch (profile._vibrateNotifications) {
                case 1:
                    if (Build.VERSION.SDK_INT >= 31)
                        lValue = 3;
                    else
                        lValue = 2;
                    break;
                case 2:
                    lValue = 0;
                    break;
            }
        }
        else {
            switch (value) {
                case 1:
                    if (Build.VERSION.SDK_INT >= 31)
                        lValue = 3;
                    else
                        lValue = 2;
                    break;
                case 2:
                    lValue = 0;
                    break;
            }
        }

        if (lValue != -1) {
            Context appContext = context.getApplicationContext();
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, executedProfileSharedPreferences, false, appContext).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (isPPPPutSettingsInstalled(appContext) > 0) {
                    if (PPApplication.deviceIsPixel) {
                        if (lValue > 0) {
                            putSettingsParameter(context, "system", "vibrate_on", "1");
                            putSettingsParameter(context, "system", "notification_vibration_intensity", String.valueOf(lValue));
                        } else {
                            putSettingsParameter(context, "system", "notification_vibration_intensity", String.valueOf(lValue));
                        }
                    } else {
                        putSettingsParameter(context, "system", "notification_vibration_intensity", String.valueOf(lValue));
                    }
                } else {
                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                            (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                        synchronized (PPApplication.rootMutex) {
                            String command1;
                            Command command;
                            if (PPApplication.deviceIsPixel) {
                                if (lValue > 0) {
                                    command1 = "settings put system " + "vibrate_on" + " 1";
                                    String command2 = "settings put system " + "notification_vibration_intensity" + " " + lValue;
                                    command = new Command(0, /*false,*/ command1, command2);
                                } else {
                                    command1 = "settings put system " + "notification_vibration_intensity" + " " + lValue;
                                    command = new Command(0, /*false,*/ command1);
                                }
                            } else {
                                command1 = "settings put system " + "notification_vibration_intensity" + " " + lValue;
                                //if (PPApplication.isSELinuxEnforcing())
                                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                command = new Command(0, /*false,*/ command1);
                            }
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                RootUtils.commandWait(command, "ActivateProfileHelper.setVibrateNotification");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setVibrateNotification", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void _setVibrationIntensity(Context context,
                                               String preferenceName,
                                               String parameterName,
                                               int value,
                                               SharedPreferences executedProfileSharedPreferences) {
        if (value != -1) {
            Context appContext = context.getApplicationContext();
            if (ProfileStatic.isProfilePreferenceAllowed(preferenceName, null, executedProfileSharedPreferences, false, appContext).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                {
//                    Log.e("ActivateProfileHelper._setVibrationIntensity", "parameterName="+parameterName);
//                    Log.e("ActivateProfileHelper._setVibrationIntensity", "value="+value);

                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                            PPApplication.deviceIsOnePlus) {

                        if (isPPPPutSettingsInstalled(appContext) > 0)
                            putSettingsParameter(context, "system", parameterName, String.valueOf(value));
                        else {
                            synchronized (PPApplication.rootMutex) {
                                String command1;
                                Command command;
                                command1 = "settings put system " + parameterName + " " + value;
                                command = new Command(0, /*false,*/ command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    RootUtils.commandWait(command, "ActivateProfileHelper.setVibrationIntensity");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper._setVibrationIntensity", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    } else {
                        if (isPPPPutSettingsInstalled(appContext) > 0)
                            putSettingsParameter(context, "system", parameterName, String.valueOf(value));
                        else {
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1;
                                    Command command;
                                    if (PPApplication.deviceIsPixel) {
                                        if (value > 0) {
                                            command1 = "settings put system " + "vibrate_on" + " 1";
                                            String command2 = "settings put system " + parameterName + " " + value;
                                            command = new Command(0, /*false,*/ command1, command2);
                                        } else {
                                            command1 = "settings put system " + parameterName + " " + value;
                                            command = new Command(0, /*false,*/ command1);
                                        }
                                    } else {
                                        command1 = "settings put system " + parameterName + " " + value;
                                        //if (PPApplication.isSELinuxEnforcing())
                                        //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                        command = new Command(0, /*false,*/ command1);
                                    }
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        RootUtils.commandWait(command, "ActivateProfileHelper.setVibrationIntensity");
                                    } catch (Exception e) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper._setVibrationIntensity", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setVibrationIntensity(Context context, Profile profile, SharedPreferences executedProfileSharedPreferences) {
        int lValueRinging;
        int lValueNotificaitons;
        int lValueTouchIntensity;

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
            if (profile.getVibrationIntensityRingingChange()) {
                lValueRinging = profile.getVibrationIntensityRingingValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING,
                        "VIB_RECVCALL_MAGNITUDE",
                        lValueRinging,
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityNotificationsChange()) {
                lValueNotificaitons = profile.getVibrationIntensityNotificationsValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS,
                        "SEM_VIBRATION_NOTIFICATION_INTENSITY",
                        lValueNotificaitons,
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityTouchInteractionChange()) {
                lValueTouchIntensity = profile.getVibrationIntensityTouchInteractionValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                        "VIB_FEEDBACK_MAGNITUDE",
                        lValueTouchIntensity,
                        executedProfileSharedPreferences
                );
            }
        } else if (PPApplication.deviceIsOnePlus) {
            if (profile.getVibrationIntensityRingingChange()) {
                lValueRinging = profile.getVibrationIntensityRingingValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING,
                        "ring_stepless_vibration_intensity",
                        lValueRinging + VibrationIntensityPreference.getMinValue("RINGING"),
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityNotificationsChange()) {
                lValueNotificaitons = profile.getVibrationIntensityNotificationsValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS,
                        "notification_stepless_vibration_intensity",
                        lValueNotificaitons + VibrationIntensityPreference.getMinValue("NOTIFICATIONS"),
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityTouchInteractionChange()) {
                lValueTouchIntensity = profile.getVibrationIntensityTouchInteractionValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                        "touch_stepless_vibration_intensity",
                        lValueTouchIntensity + VibrationIntensityPreference.getMinValue("TOUCHINTERACTION"),
                        executedProfileSharedPreferences
                );
            }
        } else {
            lValueRinging = 0;
            lValueNotificaitons = 0;
            lValueTouchIntensity = 0;
            if (profile.getVibrationIntensityRingingChange())
                lValueRinging = profile.getVibrationIntensityRingingValue();
            if (profile.getVibrationIntensityNotificationsChange())
                lValueNotificaitons = profile.getVibrationIntensityNotificationsValue();
            if (profile.getVibrationIntensityTouchInteractionChange())
                lValueTouchIntensity = profile.getVibrationIntensityTouchInteractionValue();

            if ((lValueRinging > 0) || (lValueNotificaitons > 0) || (lValueTouchIntensity > 0))
                putSettingsParameter(context, "system", "vibrate_on", "1");

            if (profile.getVibrationIntensityRingingChange()) {
                lValueRinging = profile.getVibrationIntensityRingingValue();
//                Log.e("ActivateProfileHelper.setVibrationIntensity", "lValueRinging="+lValueRinging);
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING,
                        "ring_vibration_intensity",
                        lValueRinging,
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityNotificationsChange()) {
                lValueNotificaitons = profile.getVibrationIntensityNotificationsValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS,
                        "notification_vibration_intensity",
                        lValueNotificaitons,
                        executedProfileSharedPreferences
                );
            }
            if (profile.getVibrationIntensityTouchInteractionChange()) {
                lValueTouchIntensity = profile.getVibrationIntensityTouchInteractionValue();
                _setVibrationIntensity(context,
                        Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                        "haptic_feedback_intensity",
                        lValueTouchIntensity,
                        executedProfileSharedPreferences
                );
            }
        }
    }

    static final String RINGTONE_SIM1_SAMSUNG = "ringtone";
    static final String RINGTONE_SIM2_SAMSUNG = "ringtone_2";
    static final String RINGTONE_SIM1_HUAWEI = "ringtone";
    static final String RINGTONE_SIM2_HUAWEI = "ringtone2";
    static final String RINGTONE_SIM1_XIAOMI = "ringtone_sound_slot_1";
    static final String RINGTONE_SIM2_XIAOMI = "ringtone_sound_slot_2";
    static final String RINGTONE_FOLLOW_SIM1_XIAOMI = "ringtone_sound_use_uniform";
    static final String RINGTONE_SIM1_ONEPLUS = "ringtone";
    static final String RINGTONE_SIM2_ONEPLUS = "ringtone_sim2";
    static final String RINGTONE_FOLLOW_SIM1_ONEPLUS = "ringtone_follow_sim_one";

    static String getRingtoneFromSystem(Context appContext, int simSlot) {
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
            if (simSlot == 1)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_SAMSUNG);
            if (simSlot == 2)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM2_SAMSUNG);
        }
        if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
            if (simSlot == 1)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_HUAWEI);
            if (simSlot == 2)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM2_HUAWEI);
        }
        if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI)) {
            if (simSlot == 1)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_XIAOMI);

            if (simSlot == 2) {
                int useUniform = Settings.System.getInt(appContext.getContentResolver(), RINGTONE_FOLLOW_SIM1_XIAOMI, 1);
                if (useUniform == 0)
                    return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM2_XIAOMI);
                else
                    return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_XIAOMI);
            }
        }
        if (PPApplication.deviceIsOnePlus) {
            if (simSlot == 1)
                return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_ONEPLUS);

            if (simSlot == 2) {
                int useUniform = Settings.System.getInt(appContext.getContentResolver(), RINGTONE_FOLLOW_SIM1_ONEPLUS, 1);
                if (useUniform == 0)
                    return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM2_ONEPLUS);
                else
                    return Settings.System.getString(appContext.getContentResolver(), RINGTONE_SIM1_ONEPLUS);
            }
        }
        return null;
    }

    private static boolean setTones(Context context, Profile profile, SharedPreferences executedProfileSharedPreferences) {
        boolean noError = true;
        Context appContext = context.getApplicationContext();
        if (Permissions.checkProfileRingtones(appContext, profile, null)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    try {
                        String[] splits = profile._soundRingtone.split("\\|");
                        if (!splits[0].isEmpty()) {
                            //Uri uri = Uri.parse(splits[0]);
                            Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_RINGTONE);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("ActivateProfileHelper.setTones (1)", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }

                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //Log.e("ActivateProfileHelper.setTones (2)", Log.getStackTraceString(e));
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        //Log.e("ActivateProfileHelper.setTones (3)", Log.getStackTraceString(e));
                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE,
                                null, profile._name, "");
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
                                    PPApplicationStatic.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplicationStatic.recordException(e);
                            }
                        } else
                            PPApplicationStatic.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        PPApplicationStatic.recordException(e);
                        noError = false;
                    }
                }
            }
            if (profile._soundNotificationChange == 1) {
                if (!profile._soundNotification.isEmpty()) {
                    try {
                        String[] splits = profile._soundNotification.split("\\|");
                        if (!splits[0].isEmpty()) {
                            //Uri uri = Uri.parse(splits[0]);
                            Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_NOTIFICATION);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }

                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION,
                                null, profile._name, "");
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
                                    PPApplicationStatic.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplicationStatic.recordException(e);
                            }
                        } else
                            PPApplicationStatic.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        PPApplicationStatic.recordException(e);
                        noError = false;
                    }
                }
            }
            if (profile._soundAlarmChange == 1) {
                if (!profile._soundAlarm.isEmpty()) {
                    try {
                        String[] splits = profile._soundAlarm.split("\\|");
                        if (!splits[0].isEmpty()) {
                            //Uri uri = Uri.parse(splits[0]);
                            Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_ALARM);
                            try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                            //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, splits[0]);
                            RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM, uri);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_ALARM,
                                null, profile._name, "");
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
                                    PPApplicationStatic.recordException(e);
                                }
                            } catch (Exception ee) {
                                PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                PPApplicationStatic.recordException(e);
                            }
                        } else
                            PPApplicationStatic.recordException(e);*/
                    }
                } else {
                    // selected is None tone
                    try {
                        //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
                        RingtoneManager.setActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM, null);
                    }
                    catch (IllegalArgumentException e) {
                        // java.lang.IllegalArgumentException: Invalid column: _data
                        //PPApplicationStatic.recordException(e);
                    }
                    catch (Exception e){
                        PPApplicationStatic.recordException(e);
                        noError = false;
                    }
                }
            }

            GlobalUtils.HasSIMCardData hasSIMCardData = null;
            if ((profile._soundRingtoneChangeSIM1 == 1) ||
                    (profile._soundRingtoneChangeSIM2 == 1) ||
                    (profile._soundNotificationChangeSIM1 == 1) ||
                    (profile._soundNotificationChangeSIM2 == 1) ||
                    (profile._soundSameRingtoneForBothSIMCards != 0)
                )
            {
                hasSIMCardData = GlobalUtils.hasSIMCard(appContext);
            }

            if (profile._soundRingtoneChangeSIM1 == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    boolean sim1Exists = hasSIMCardData.hasSIM1;
                    if (sim1Exists) {

                        if (!profile._soundRingtoneSIM1.isEmpty()) {
                            try {
                                String[] splits = profile._soundRingtoneSIM1.split("\\|");
                                if (!splits[0].isEmpty()) {
                                    //Uri uri = Uri.parse(splits[0]);
                                    Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_RINGTONE);
                                    try {
                                        ContentResolver contentResolver = context.getContentResolver();
                                        context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    //                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ring tone granted");
                                    } catch (Exception e) {
                                        // java.lang.SecurityException: UID 10157 does not have permission to
                                        // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                        // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                        //Log.e("ActivateProfileHelper.setTones (1)", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }

                                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) && (uri != null)) {
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

    //                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Samsung uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {}

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_SAMSUNG, uri.toString());

                                    } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI) && (uri != null)) {
    //                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Huawei uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {}

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_HUAWEI, uri.toString());
                                    } else if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI) && (uri != null)) {
    //                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Xiaomi uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {}

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_XIAOMI, uri.toString());
                                    } else if (PPApplication.deviceIsOnePlus && (uri != null)) {
//                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 OnePlus uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {}

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_ONEPLUS, uri.toString());
                                    }
                                }
                            }
                            catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //Log.e("ActivateProfileHelper.setTones (2)", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                            catch (Exception e) {
                                //Log.e("ActivateProfileHelper.setTones (3)", Log.getStackTraceString(e));
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE,
                                        null, profile._name, "");
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
                                            PPApplicationStatic.recordException(e);
                                        }
                                    } catch (Exception ee) {
                                        PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                        PPApplicationStatic.recordException(e);
                                    }
                                } else
                                    PPApplicationStatic.recordException(e);*/
                            }
                        } else {
                            // selected is None tone
                            try {
                                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
    //                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Samsung uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_SAMSUNG, null);
                                }
                                else
                                if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
    //                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Huawei uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_HUAWEI, null);
                                }
                                else
                                if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI)) {
    //                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 Xiaomi uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_XIAOMI, null);
                                }
                                else
                                if (PPApplication.deviceIsOnePlus) {
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM1 OnePlus uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM1_ONEPLUS, null);
                                }
                            }
                            catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            }
                            catch (Exception e){
                                PPApplicationStatic.recordException(e);
                                noError = false;
                            }
                        }
                    }
                }
            }
            if (profile._soundRingtoneChangeSIM2 == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    boolean sim2Exists = hasSIMCardData.hasSIM2;

                    if (sim2Exists) {

                        if (!profile._soundRingtoneSIM2.isEmpty()) {
                            try {
                                String[] splits = profile._soundRingtoneSIM2.split("\\|");
                                if (!splits[0].isEmpty()) {
                                    //Uri uri = Uri.parse(splits[0]);
                                    Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_RINGTONE);
                                    try {
                                        ContentResolver contentResolver = context.getContentResolver();
                                        context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ring tone granted");
                                    } catch (Exception e) {
                                        // java.lang.SecurityException: UID 10157 does not have permission to
                                        // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                        // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                        //Log.e("ActivateProfileHelper.setTones (1)", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }

                                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) && (uri != null)) {
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Samsung uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_SAMSUNG, uri.toString());
                                    } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI) && (uri != null)) {
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Huawei uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_HUAWEI, uri.toString());
                                    } else if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI) && (uri != null)) {
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Xiaomi uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_XIAOMI, uri.toString());
                                    } else if (PPApplication.deviceIsOnePlus && (uri != null)) {
//                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 OnePlus uri=" + uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_ONEPLUS, uri.toString());
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //Log.e("ActivateProfileHelper.setTones (2)", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                //Log.e("ActivateProfileHelper.setTones (3)", Log.getStackTraceString(e));
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE,
                                        null, profile._name, "");
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
                                        PPApplicationStatic.recordException(e);
                                    }
                                } catch (Exception ee) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplicationStatic.recordException(e);
                                }
                            } else
                                PPApplicationStatic.recordException(e);*/
                            }
                        } else {
                            // selected is None tone
                            try {
                                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Samsung uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_SAMSUNG, null);
                                } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Huawei uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_HUAWEI, null);
                                } else if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI)) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 Xiaomi uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_XIAOMI, null);
                                } else if (PPApplication.deviceIsOnePlus) {
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "ringtone SIM2 OnePlus uri=null");

                                    Settings.System.putString(context.getContentResolver(), RINGTONE_SIM2_ONEPLUS, null);
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                                noError = false;
                            }
                        }
                    }
                }
            }
            if (profile._soundNotificationChangeSIM1 == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    boolean sim1Exists = hasSIMCardData.hasSIM1;

                    if (sim1Exists) {

                        if (!profile._soundNotificationSIM1.isEmpty()) {
                            try {
                                String[] splits = profile._soundNotificationSIM1.split("\\|");
                                if (!splits[0].isEmpty()) {
                                    //Uri uri = Uri.parse(splits[0]);
                                    Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_NOTIFICATION);
                                    try {
                                        ContentResolver contentResolver = context.getContentResolver();
                                        context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification tone granted");
                                    } catch (Exception e) {
                                        // java.lang.SecurityException: UID 10157 does not have permission to
                                        // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                        // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }

                                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) && (uri != null)) {
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", " notification SIM1 Samsung uri="+uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        //Settings.System.putString(context.getContentResolver(), "notification_sound", uri.toString());

                                        if (isPPPPutSettingsInstalled(appContext) > 0) {
                                            putSettingsParameter(context, "system", "notification_sound", uri.toString());
                                        } else {
                                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                                synchronized (PPApplication.rootMutex) {
                                                    String command1;
                                                    Command command;
                                                    command1 = "settings put system notification_sound" + " " + uri.toString();
                                                    command = new Command(0, /*false,*/ command1);
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM 1 with root");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        }
                                    } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI) && (uri != null)) {
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification SIM1 Huawei uri="+uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        if (isPPPPutSettingsInstalled(appContext) > 0) {
                                            putSettingsParameter(context, "system", "message", uri.toString());
                                        } else {
                                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                                synchronized (PPApplication.rootMutex) {
                                                    String command1;
                                                    Command command;
                                                    command1 = "settings put system message" + " " + uri.toString();
                                                    command = new Command(0, /*false,*/ command1);
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM 1 with root");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION,
                                        null, profile._name, "");
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
                                        PPApplicationStatic.recordException(e);
                                    }
                                } catch (Exception ee) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplicationStatic.recordException(e);
                                }
                            } else
                                PPApplicationStatic.recordException(e);*/
                            }
                        } else {
                            // selected is None tone
                            try {
                                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                                    //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                    //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", " notification SIM1 Samsung uri=null");

                                    //Settings.System.putString(context.getContentResolver(), "notification_sound", null);

                                    if (isPPPPutSettingsInstalled(appContext) > 0) {
                                        putSettingsParameter(context, "system", "notification_sound", "");
                                    } else {
                                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1;
                                                Command command;
                                                command1 = "settings put system notification_sound" + " \"\"";
                                                command = new Command(0, /*false,*/ command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM1 with root");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification SIM1 Huawei uri=null");

                                    // notifikacie ine ako sms - zvlastna katergoria v Huawei
                                    //Settings.System.putString(context.getContentResolver(), "notification_sound", null);

                                    if (isPPPPutSettingsInstalled(appContext) > 0) {
                                        putSettingsParameter(context, "system", "message", "");
                                    } else {
                                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1;
                                                Command command;
                                                command1 = "settings put system message" + " \"\"";
                                                command = new Command(0, /*false,*/ command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM1 with root");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                                noError = false;
                            }
                        }
                    }
                }
            }
            if (profile._soundNotificationChangeSIM2 == 1) {
//                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification SIM2");
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    boolean sim2Exists = hasSIMCardData.hasSIM2;

                    if (sim2Exists) {

                        if (!profile._soundNotificationSIM2.isEmpty()) {
                            try {
                                String[] splits = profile._soundNotificationSIM2.split("\\|");
                                if (!splits[0].isEmpty()) {
                                    //Uri uri = Uri.parse(splits[0]);
                                    Uri uri = getUriOfSavedTone(context, splits[0], RingtoneManager.TYPE_NOTIFICATION);
                                    try {
                                        ContentResolver contentResolver = context.getContentResolver();
                                        context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification tone granted");
                                    } catch (Exception e) {
                                        // java.lang.SecurityException: UID 10157 does not have permission to
                                        // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                        // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }

                                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) && (uri != null)) {
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                        //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", " notification SIM2 Samsung uri="+uri.toString());

                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        //Settings.System.putString(context.getContentResolver(), "notification_sound_2", uri.toString());

                                        if (isPPPPutSettingsInstalled(appContext) > 0) {
                                            putSettingsParameter(context, "system", "notification_sound_2", uri.toString());
                                        } else {
                                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                                synchronized (PPApplication.rootMutex) {
                                                    String command1;
                                                    Command command;
                                                    command1 = "settings put system notification_sound_2" + " " + uri.toString();
                                                    command = new Command(0, /*false,*/ command1);
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM 2 with root");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        }
                                    } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI) && (uri != null)) {
                                        try {
                                            uri = ContentProvider.maybeAddUserId(uri, context.getUserId());
                                        } catch (Exception ignored) {
                                        }

                                        // not working without root
                                        //if (isPPPPutSSettingsInstalled(appContext)) {
                                        //    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification SIM2 Huawei uri="+uri.toString());
                                        //    putSettingsParameter(context, "system", "messageSub1", uri.toString());
                                        //} else
                                        {
                                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                                synchronized (PPApplication.rootMutex) {
                                                    String command1;
                                                    Command command;
                                                    command1 = "settings put system messageSub1" + " " + uri.toString();
                                                    command = new Command(0, /*false,*/ command1);
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM2 with root");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION,
                                        null, profile._name, "");
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
                                        PPApplicationStatic.recordException(e);
                                    }
                                } catch (Exception ee) {
                                    PPApplication.setCustomKey("ActivateProfileHelper_setTone", splits[0]);
                                    PPApplicationStatic.recordException(e);
                                }
                            } else
                                PPApplicationStatic.recordException(e);*/
                            }
                        } else {
                            // selected is None tone
                            try {
                                if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                                    //Settings.System.putString(context.getContentResolver(), "ringtone_set", "1");
                                    //Settings.System.putString(context.getContentResolver(), "ringtone_2_set", "1");

//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", " notification SIM2 Samsung uri=null");

                                    //Settings.System.putString(context.getContentResolver(), "notification_sound_2", null);

                                    if (isPPPPutSettingsInstalled(appContext) > 0)  {
                                        putSettingsParameter(context, "system", "notification_sound_2", "");
                                    } else {
                                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1;
                                                Command command;
                                                command1 = "settings put system notification_sound_2" + " \"\"";
                                                command = new Command(0, /*false,*/ command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification fro SIM2 with root");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification SIM2 Huawei uri=null");

                                    // not working without root
                                    //if (isPPPPutSSettingsInstalled(appContext)) {
                                    //    putSettingsParameter(context, "system", "messageSub1", "");
                                    //} else
                                    {
                                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1;
                                                Command command;
                                                command1 = "settings put system messageSub1" + " \"\"";
                                                command = new Command(0, /*false,*/ command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "notification for SIM2 with root");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Invalid column: _data
                                //PPApplicationStatic.recordException(e);
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                                noError = false;
                            }
                        }
                    }
                }
            }

            if (profile._soundSameRingtoneForBothSIMCards != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    boolean sim1Exists = hasSIMCardData.hasSIM1;
                    boolean sim2Exists = hasSIMCardData.hasSIM2;

                    if (sim1Exists && sim2Exists) {

                        try {
                            String value = "1";
                            if (profile._soundSameRingtoneForBothSIMCards == 1)
                                value = "1";
                            if (profile._soundSameRingtoneForBothSIMCards == 2)
                                value = "0";

                            if (isPPPPutSettingsInstalled(appContext) > 0) {
                                if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)
                                    putSettingsParameter(context, "system", RINGTONE_FOLLOW_SIM1_XIAOMI, value);
                                else
                                if (PPApplication.deviceIsOnePlus)
                                    putSettingsParameter(context, "system", RINGTONE_FOLLOW_SIM1_ONEPLUS, value);
                            } else {
                                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                        (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                    synchronized (PPApplication.rootMutex) {
                                        String command1 = null;
                                        Command command;
                                        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)
                                            command1 = "settings put system " + RINGTONE_FOLLOW_SIM1_XIAOMI + " " + value;
                                        else
                                        if (PPApplication.deviceIsOnePlus)
                                            command1 = "settings put system " + RINGTONE_FOLLOW_SIM1_ONEPLUS + " " + value;
                                        if (command1 != null) {
                                            command = new Command(0, /*false,*/ command1);
                                            try {
                                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                RootUtils.commandWait(command, "ActivateProfileHelper.setTones");
//                                            bPPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setTones", "same ringtone fro bth sim cards with root");
                                            } catch (Exception e) {
                                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                //Log.e("ActivateProfileHelper.setTones", Log.getStackTraceString(e));
                                                //PPApplicationStatic.recordException(e);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // java.lang.IllegalArgumentException: Invalid column: _data
                            //PPApplicationStatic.recordException(e);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                            noError = false;
                        }
                    }
                }
            }
        }

        return noError;
    }

    static Uri getUriOfSavedTone(Context context, String savedTone, int toneType) {
        Uri toneUri;
        boolean uriFound = false;
        if (savedTone.equals("")) {
            toneUri = null;
            uriFound = true;
        }
        else
        if (savedTone.equals(Settings.System.DEFAULT_RINGTONE_URI.toString())) {
            toneUri = Settings.System.DEFAULT_RINGTONE_URI;
            uriFound = true;
        }
        else
        if (savedTone.equals(Settings.System.DEFAULT_NOTIFICATION_URI.toString())) {
            toneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            uriFound = true;
        }
        else
        if (savedTone.equals(Settings.System.DEFAULT_ALARM_ALERT_URI.toString())) {
            toneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            uriFound = true;
        }
        else {
            toneUri = null;
            RingtoneManager manager = new RingtoneManager(context.getApplicationContext());
            manager.setType(toneType);
            Cursor ringtoneCursor = manager.getCursor();
            while (ringtoneCursor.moveToNext()) {
                Uri _toneUri = manager.getRingtoneUri(ringtoneCursor.getPosition());
                String _uri = ringtoneCursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                String _id = ringtoneCursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                String toneFromCursor = _uri + "/" + _id;
                if (toneFromCursor.equals(savedTone)) {
                    toneUri = _toneUri;
                    uriFound = true;
                    break;
                }
            }
        }
        if (uriFound)
            return toneUri;
        else
            return null;
    }

    static void executeForVolumes(Profile profile, final int linkUnlinkVolumes, final boolean forProfileActivation, Context context, SharedPreferences executedProfileSharedPreferences) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadVolumes();
        //final Handler __handler = new Handler(PPApplication.handlerThreadVolumes.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), profile, executedProfileSharedPreferences) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadVolumes", "START run - from=ActivateProfileHelper.executeForVolumes");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForVolumes");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                int linkUnlink = PhoneCallsListener.LINKMODE_NONE;
                if (ActivateProfileHelper.getMergedRingNotificationVolumes() &&
                        ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {
                    if (Permissions.checkPhone(appContext))
                        linkUnlink = linkUnlinkVolumes;
                }

                if (profile != null) {
                    boolean noErrorSetTone = setTones(appContext, profile, executedProfileSharedPreferences);

                    final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                    if ((profile._volumeRingerMode != 0) ||
                            profile.getVolumeRingtoneChange() ||
                            profile.getVolumeNotificationChange() ||
                            profile.getVolumeSystemChange() ||
                            profile.getVolumeDTMFChange()) {

                        // sleep for change of ringer mode and volumes
                        // because may be changed by another profile or from outside of PPP
                        GlobalUtils.sleep(500);

                        RingerModeChangeReceiver.internalChange = true;

                        if (canChangeZenMode(appContext)) {
                            changeRingerModeForVolumeEqual0(profile, audioManager, appContext);
                            changeNotificationVolumeForVolumeEqual0(/*context,*/ profile);

                            setRingerMode(appContext, profile, audioManager, /*systemZenMode,*/ forProfileActivation, executedProfileSharedPreferences);

                            GlobalUtils.sleep(500);

                            // get actual system zen mode (may be changed in setRingerMode())
                            int systemZenMode = getSystemZenMode(appContext/*, -1*/);

                            setVolumes(appContext, profile, audioManager, systemZenMode, linkUnlink, forProfileActivation, true);

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
                        }
                    } else {

                        RingerModeChangeReceiver.internalChange = true;

                        int systemZenMode = getSystemZenMode(appContext/*, -1*/);

                        setVolumes(appContext, profile, audioManager, systemZenMode, linkUnlink, forProfileActivation, false);
                    }

                    PPExecutors.scheduleDisableInternalChangeExecutor();
                    //DisableVolumesInternalChangeWorker.enqueueWork();

                    if (noErrorSetTone) {
                        setTones(appContext, profile, executedProfileSharedPreferences);
                    }

                }
            } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }; //);
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
    }

    private static void setNotificationLed(Context context, final int value, SharedPreferences executedProfileSharedPreferences) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadProfileActivation();
        //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), null, executedProfileSharedPreferences) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setNotificationLed");

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && /*(profile != null) &&*/ (executedProfileSharedPreferences != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setNotificationLed");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, executedProfileSharedPreferences, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (isPPPPutSettingsInstalled(appContext) > 0)
                            putSettingsParameter(context, "system", "notification_light_pulse", String.valueOf(value));
                        else {
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + "notification_light_pulse"/*Settings.System.NOTIFICATION_LIGHT_PULSE*/ + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, /*false,*/ command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        RootUtils.commandWait(command, "ActivateProfileHelper.setNotificationLed");
                                    } catch (Exception e) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setNotificationLed", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
    }

    private static void setHeadsUpNotifications(Context context, final int value, SharedPreferences executedProfileSharedPreferences) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadProfileActivation();
        //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), null, executedProfileSharedPreferences) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setHeadsUpNotifications");

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && /*(profile != null) &&*/ (executedProfileSharedPreferences != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setHeadsUpNotifications");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, executedProfileSharedPreferences, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        boolean G1OK = false;
                        //if (android.os.Build.VERSION.SDK_INT >= 21) {
                        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                            try {
                                Settings.Global.putInt(appContext.getContentResolver(), "heads_up_notifications_enabled", value);
                                G1OK = true;
                            } catch (Exception ee) {
                                Log.e("ActivateProfileHelper.setHeadsUpNotifications", Log.getStackTraceString(ee));
                            }
                        }
                        if (!G1OK) {
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put global " + "heads_up_notifications_enabled" + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, /*false,*/ command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        RootUtils.commandWait(command, "ActivateProfileHelper.setHeadsUpNotifications");
                                    } catch (Exception e) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setHeadsUpNotifications", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                        }
                        //}
                    }
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
    }

    private static void setAlwaysOnDisplay(Context context, final int value, SharedPreferences executedProfileSharedPreferences) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadProfileActivation();
        //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), null, executedProfileSharedPreferences) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setAlwaysOnDisplay");

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && /*(profile != null) &&*/ (executedProfileSharedPreferences != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setAlwaysOnDisplay");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, executedProfileSharedPreferences, false, appContext).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        boolean G1OK = false;
                        //if (PPApplication.deviceIsOnePlus) {
                        //    if (isPPPPutSettingsInstalled(appContext) > 0) {
                        //        putSettingsParameter(context, "system", "aod_mode", String.valueOf(value));
                        //        G1OK = true;
                        //    }
                        //} else {
                            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                try {
                                    //if (PPApplication.deviceIsOnePlus)
                                    //    Settings.Secure.putInt(appContext.getContentResolver(), "doze_enabled", value);
                                    //else
                                        Settings.Secure.putInt(appContext.getContentResolver(), "doze_always_on", value);
                                    G1OK = true;
                                } catch (Exception ee) {
                                    Log.e("ActivateProfileHelper.setAlwaysOnDisplay", Log.getStackTraceString(ee));
                                }
                            }
                        //}
                        if (!G1OK) {
                            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                    (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                synchronized (PPApplication.rootMutex) {
                                    String command1;
                                    //if (PPApplication.deviceIsOnePlus)
                                        //command1 = "settings put system " + "aod_mode" + " " + value;
                                    //    command1 = "settings put secure " + "doze_enabled" + " " + value;
                                    //else
                                        command1 = "settings put secure " + "doze_always_on" + " " + value;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, /*false,*/ command1); //, command2);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                        RootUtils.commandWait(command, "ActivateProfileHelper.setAlwaysOnDisplay");
                                    } catch (Exception e) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setAlwaysOnDisplay", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
    }

    private static void setScreenOnPermanent(Profile profile, Context context) {
        if (Permissions.checkProfileScreenOnPermanent(context, profile, null)) {
            if (profile._screenOnPermanent == 1)
                createKeepScreenOnView(context);
            else if (profile._screenOnPermanent == 2)
                removeKeepScreenOnView(context);
        }
    }

    private static void changeRingerModeForVolumeEqual0(Profile profile, AudioManager audioManager, Context context) {
        //profile._ringerModeForZenMode = AudioManager.RINGER_MODE_NORMAL;

        if (profile.getVolumeRingtoneChange()) {
            if (profile.getVolumeRingtoneValue() == 0) {
                profile.setVolumeRingtoneValue(1);
                if (!profile._volumeMuteSound) {
                    //profile._ringerModeForZenMode = AudioManager.RINGER_MODE_SILENT;

                    // for profile ringer/zen mode = "only vibrate" do not change ringer mode to Silent
                    if (!isVibrateRingerMode(profile._volumeRingerMode/*, profile._volumeZenMode*/)) {
                        if (isAudibleRinging(profile._volumeRingerMode, profile._volumeZenMode/*, false*/)) {
                            // change ringer mode to Silent
                            profile._volumeRingerMode = Profile.RINGERMODE_SILENT;
                        }
                    }
                }
            } else {
                if (profile._volumeRingerMode == 0) {
                    // ringer mode is not changed by profile, use system ringer and zen mode
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                        int systemZenMode = getSystemZenMode(context/*, -1*/);
                        if (!isAudibleSystemRingerMode(audioManager, systemZenMode/*, context*/)) {
                            // change ringer mode to ringing becaiuse configured is ringing volume
                            // Priority zen mode is audible. DO NOT DISABLE IT !!!
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
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                //if (notCheckAccess)
                //    return true;
                //else
                    return checkAccessNotificationPolicy(appContext);
                    //return Permissions.checkAccessNotificationPolicy(appContext);
            //}
            //else
            //    return PPNotificationListenerService.isNotificationListenerServiceEnabled(appContext, false);
        //}
        //else
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //    return PPNotificationListenerService.isNotificationListenerServiceEnabled(appContext);
        //return false;
    }

    private static void changeNotificationVolumeForVolumeEqual0(Profile profile) {
        if (profile.getVolumeNotificationChange() && ActivateProfileHelper.getMergedRingNotificationVolumes()) {
            if (profile.getVolumeNotificationValue() == 0) {
                profile.setVolumeNotificationValue(1);
            }
        }
    }

    static int getSystemZenMode(Context context/*, int defaultValue*/) {
        Context appContext = context.getApplicationContext();
        //if (android.os.Build.VERSION.SDK_INT >= 23) {
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //boolean activityExists = GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context);
            //if (/*no60 &&*/ activityExists) {
                NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
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
            /*}
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
            }*/
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
        //int vibrateWhenRinging;
        //if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        //else
        //    vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

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
                //PPApplicationStatic.recordException(ee);
            }
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
            } catch (Exception ee) {
                //PPApplicationStatic.recordException(ee);
            }
        }
        else {
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception ee) {
                //PPApplicationStatic.recordException(ee);
            }
            try {
                audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
            } catch (Exception ee) {
                //PPApplicationStatic.recordException(ee);
            }
        }
    }

    private static void setRingerMode(Context context, Profile profile, AudioManager audioManager,
            /*int systemZenMode,*/ boolean forProfileActivation, SharedPreferences executedProfileSharedPreferences)
    {
        Context appContext = context.getApplicationContext();

        int ringerMode;
        int zenMode;

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

        if (forProfileActivation) {

            switch (ringerMode) {
                case Profile.RINGERMODE_RING:
                    synchronized (PPApplication.notUnlinkVolumesMutex) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                    }
                    //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    GlobalUtils.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setVibrateSettings(false, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_NORMAL);

                    // set it by profile
                    setVibrateWhenRinging(appContext, profile, -1, executedProfileSharedPreferences);
                    setVibrateNotification(appContext, profile, -1, executedProfileSharedPreferences);
                    break;
                case Profile.RINGERMODE_RING_AND_VIBRATE:
                    synchronized (PPApplication.notUnlinkVolumesMutex) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                    }
                    //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    GlobalUtils.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setVibrateSettings(true, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_NORMAL);

                    // force vbration
                    setVibrateWhenRinging(appContext, null, 1, executedProfileSharedPreferences);
                    setVibrateNotification(appContext, null, 1, executedProfileSharedPreferences);
                    break;
                case Profile.RINGERMODE_VIBRATE:
                    synchronized (PPApplication.notUnlinkVolumesMutex) {
                        RingerModeChangeReceiver.notUnlinkVolumes = false;
                    }
                    //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                    GlobalUtils.sleep(500);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    setVibrateSettings(true, audioManager);

                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                    // force vbration
                    setVibrateWhenRinging(appContext, null, 1, executedProfileSharedPreferences);
                    setVibrateNotification(appContext, null, 1, executedProfileSharedPreferences);
                    break;
                case Profile.RINGERMODE_SILENT:

                    //setZenMode(appContext, ZENMODE_SILENT, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);

                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                            (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                        }
                        //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                        InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);
                        GlobalUtils.sleep(500);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        setVibrateSettings(true, audioManager);
                    }
                    else {
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = false;
                        }
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        setVibrateSettings(false, audioManager);
                        GlobalUtils.sleep(500);
                        //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALARMS);
                        InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALARMS);
                    }

                    // set it by profile
                    setVibrateWhenRinging(appContext, profile, -1, executedProfileSharedPreferences);
                    setVibrateNotification(appContext, profile, -1, executedProfileSharedPreferences);
                    break;
                case Profile.RINGERMODE_ZENMODE:
                    switch (zenMode) {
                        case Profile.ZENMODE_ALL:
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            GlobalUtils.sleep(500);
                            //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);

                            //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, /*AudioManager.RINGER_MODE_NORMAL*/profile._ringerModeForZenMode);

                            // set it by profile
                            setVibrateWhenRinging(appContext, profile, -1, executedProfileSharedPreferences);
                            setVibrateNotification(appContext, profile, -1, executedProfileSharedPreferences);
                            break;
                        case Profile.ZENMODE_PRIORITY:
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            GlobalUtils.sleep(500);
                            //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, profile._ringerModeForZenMode);

                            // set it by profile
                            setVibrateWhenRinging(appContext, profile, -1, executedProfileSharedPreferences);
                            setVibrateNotification(appContext, profile, -1, executedProfileSharedPreferences);
                            break;
                        case Profile.ZENMODE_NONE:
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            GlobalUtils.sleep(500);
                            //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_NONE);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_NONE);

                            //setZenMode(appContext, ZENMODE_NONE, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);
                            break;
                        case Profile.ZENMODE_ALL_AND_VIBRATE:
                            // this is as Sound mode = Vibrate

                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateSettings(true, audioManager);
                            GlobalUtils.sleep(500);
                            //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALL);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALL);

                            //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                            //setZenMode(appContext, ZENMODE_ALL, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                            // force vbration
                            setVibrateWhenRinging(appContext, null, 1, executedProfileSharedPreferences);
                            setVibrateNotification(appContext, null, 1, executedProfileSharedPreferences);
                            break;
                        case Profile.ZENMODE_PRIORITY_AND_VIBRATE:
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            /*if (Build.VERSION.SDK_INT <= 25) {
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                GlobalUtils.sleep(500);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                //PPApplication.sleep(500);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }
                            else*/
                            //noinspection IfStatementWithIdenticalBranches
                            if (Build.VERSION.SDK_INT <= 28) {
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                //setVibrateSettings(true, audioManager);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                                GlobalUtils.sleep(1000);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }
                            else {
                                // must be set 2x to keep vibraton
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                GlobalUtils.sleep(500);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);

                                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                setVibrateSettings(true, audioManager);
                                //PPApplication.sleep(500);
                                //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                                InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_PRIORITY);
                            }

                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);
                            //setZenMode(appContext, ZENMODE_PRIORITY, audioManager, systemZenMode, AudioManager.RINGER_MODE_VIBRATE);

                            // force vbration
                            setVibrateWhenRinging(appContext, null, 1, executedProfileSharedPreferences);
                            setVibrateNotification(appContext, null, 1, executedProfileSharedPreferences);
                            break;
                        case Profile.ZENMODE_ALARMS:
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = false;
                            }
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            setVibrateSettings(false, audioManager);
                            GlobalUtils.sleep(500);
                            //PPNotificationListenerService.requestInterruptionFilter(appContext, ZENMODE_ALARMS);
                            InterruptionFilterChangedBroadcastReceiver.requestInterruptionFilter(appContext, ZENMODE_ALARMS);

                            //setZenMode(appContext, ZENMODE_ALARMS, audioManager, systemZenMode, AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
            }
        }
    }

    private static void _changeImageWallpaper(Profile profile, String wallpaperUri, Context appContext) {
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

            // for lock screen no double width
            if (/*(Build.VERSION.SDK_INT < 24) ||*/ (profile._deviceWallpaperFor != 2))
                width = width << 1; // best wallpaper width is twice screen width

            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmapUri(wallpaperUri, width, height, false, true, appContext);
            if (decodedSampleBitmap != null) {
                // set wallpaper
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
                try {
                    //if (Build.VERSION.SDK_INT >= 24) {
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
                    wallpaperManager.setBitmap(decodedSampleBitmap, visibleCropHint, true, flags);
                    //} else
                    //    wallpaperManager.setBitmap(decodedSampleBitmap);

                    PPApplicationStatic.setWallpaperChangeTime(appContext);
                } catch (IOException e) {
                    PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER,
                            null, profile._name, "");
                    //Log.e("ActivateProfileHelper._changeImageWallpaper", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } catch (Exception e) {
                    PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER,
                            null, profile._name, "");
                    //PPApplicationStatic.recordException(e);
                }
            } else {
                PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_SET_WALLPAPER,
                        null, profile._name, "");
            }
        }
    }

    private static void changeImageWallpaper(Profile profile, Context context) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadWallpaper();
        //final Handler __handler = new Handler(PPApplication.handlerThreadWallpaper.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), profile, null) {
        //__handler.post(() -> {

        if ((Build.VERSION.SDK_INT < 29) || (Settings.canDrawOverlays(context))) {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWallpaper", "START run - from=ActivateProfileHelper.executeForWallpaper");

                //Context appContext= appContextWeakRef.get();
                //Profile profile = profileWeakRef.get();
                //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

                //if ((appContext != null) && (profile != null) /*&& (executedProfileSharedPreferences != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForWallpaper");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    _changeImageWallpaper(profile, profile._deviceWallpaper, appContext);
                } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
            PPApplicationStatic.createProfileActiationExecutorPool();
            PPApplication.profileActiationExecutorPool.submit(runnable);
        }
    }

    private static void changeWallpaperFromFolder(Profile profile, Context context) {
        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        // next allowed wallpaper change is 50 seconds after last chaange of wallpaper
        final long _time = now.getTimeInMillis() + gmtOffset - (50 * 1000);

        if (PPApplication.applicationFullyStarted &&
                ((PPApplication.wallpaperChangeTime == 0) ||
                 (PPApplication.wallpaperChangeTime <= _time))) {

//            PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "(1)");
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadWallpaper();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWallpaper.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(
            //        context.getApplicationContext(), profile, null) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWallpaper", "START run - from=ActivateProfileHelper.executeForWallpaper");

                //Context appContext= appContextWeakRef.get();
                //Profile profile = profileWeakRef.get();
                //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

                //if ((appContext != null) && (profile != null) /*&& (executedProfileSharedPreferences != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForWallpaper");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "(2)");

                    //----------
                    // test get list of files from folder

                    Uri folderUri = Uri.parse(profile._deviceWallpaperFolder);

//                    PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "folderUri="+folderUri);

                    List<Uri> uriList = new ArrayList<>();

                    Cursor cursor = null;
                    try {
                        appContext.grantUriPermission(PPApplication.PACKAGE_NAME, folderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        appContext.getContentResolver().takePersistableUriPermission(folderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        /*
                        appContext.grantUriPermission(PPApplication.PACKAGE_NAME, folderUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        // persistent permissions
                        final int takeFlags = //data.getFlags() &
                                (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        appContext.getContentResolver().takePersistableUriPermission(folderUri, takeFlags);
                        */

                        // the uri from which we query the files
                        Uri uriFolder = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, DocumentsContract.getTreeDocumentId(folderUri));
//                        PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "uriFolder="+uriFolder);

                        // let's query the files
                        ContentResolver contentResolver = appContext.getContentResolver();
                        cursor = contentResolver.query(uriFolder,
                                new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                                null, null, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                // build the uri for the file
                                Uri uriFile = DocumentsContract.buildDocumentUriUsingTree(folderUri, cursor.getString(0));
//                                PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "mime type="+contentResolver.getType(uriFile));
                                if (contentResolver.getType(uriFile).startsWith("image/")) {
//                                    PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "uriFile="+uriFile);
                                    //add to the list
                                    uriList.add(uriFile);
                                }

                            } while (cursor.moveToNext());
                        }
                    } catch (Exception e) {
//                        PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", Log.getStackTraceString(e));
                    } finally {
                        if (cursor != null) cursor.close();
                    }

                    if (uriList.size() > 0) {
                        Uri wallpaperUri = uriList.get(new Random().nextInt(uriList.size()));
//                        PPApplicationStatic.logE("ActivateProfileHelper.changeWallpaperFromFolder", "wallpaperUri="+wallpaperUri);

                        _changeImageWallpaper(profile, wallpaperUri.toString(), appContext);
                    }

                    //----------------

                } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
            PPApplicationStatic.createProfileActiationExecutorPool();
            PPApplication.profileActiationExecutorPool.submit(runnable);
        }
    }

    private static void executeForRunApplications(Profile profile, Context context) {
        if (profile._deviceRunApplicationChange == 1)
        {
            if ((Build.VERSION.SDK_INT < 29) || (Settings.canDrawOverlays(context))) {
                final Context appContext = context.getApplicationContext();
                //PPApplication.startHandlerThreadRunApplication();
                //final Handler __handler = new Handler(PPApplication.handlerThreadRunApplication.getLooper());
                //__handler.post(new PPHandlerThreadRunnable(
                //        context.getApplicationContext(), profile, null) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadRunApplication", "START run - from=ActivateProfileHelper.executeForRunApplications");

                    if (PPApplication.blockProfileEventActions)
                        // not start applications after boot
                        return;

                    //Context appContext= appContextWeakRef.get();
                    //Profile profile = profileWeakRef.get();
                    //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

                    //if ((appContext != null) && (profile != null) /*&& (executedProfileSharedPreferences != null)*/) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeForRunApplications");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        String[] splits = profile._deviceRunApplicationPackageName.split("\\|");

                        //ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        //List<ActivityManager.RunningAppProcessInfo> procInfo = activityManager.getRunningAppProcesses();

                        for (String split : splits) {
//                            Log.e("ActivateProfileHelper.executeForRunApplications", "split="+split);
                            int startApplicationDelay = Application.getStartApplicationDelay(split);
//                            Log.e("ActivateProfileHelper.executeForRunApplications", "startApplicationDelay="+startApplicationDelay);
                            if (Application.getStartApplicationDelay(split) > 0) {
                                RunApplicationWithDelayBroadcastReceiver.setDelayAlarm(appContext, startApplicationDelay, profile._name, split);
                            } else {
//                                Log.e("ActivateProfileHelper.executeForRunApplications", "call of ActivateProfileHelper.doExecuteForRunApplications");
                                doExecuteForRunApplications(appContext, profile._name, split);
                            }
                            GlobalUtils.sleep(1000);
                        }
                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
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
                PPApplicationStatic.createProfileActiationExecutorPool();
                PPApplication.profileActiationExecutorPool.submit(runnable);
            }
        }
    }

    static void doExecuteForRunApplications(Context context, String profileName, String runApplicationData) {
        Intent appIntent;
        PackageManager packageManager = context.getPackageManager();

        if (Application.isShortcut(runApplicationData)) {
            long shortcutId = Application.getShortcutId(runApplicationData);
            if (shortcutId > 0) {
                Shortcut shortcut = DatabaseHandler.getInstance(context).getShortcut(shortcutId);
                if (shortcut != null) {
                    try {
                        appIntent = Intent.parseUri(shortcut._intent, 0);
                        if (appIntent != null) {
                            //noinspection TryWithIdenticalCatches
                            try {
                                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(appIntent);
                            } catch (ActivityNotFoundException ee) {
                                PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT,
                                        null, profileName, "");
                            } catch (SecurityException e) {
                                PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT,
                                        null, profileName, "");
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        } else {
                            PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT,
                                    null, profileName, "");
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT,
                            null, profileName, "");
                }
            } else {
                PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT,
                        null, profileName, "");
            }
        } else
        if (Application.isIntent(runApplicationData)) {
            long intentId = Application.getIntentId(runApplicationData);
            if (intentId > 0) {
                PPIntent ppIntent = DatabaseHandler.getInstance(context).getIntent(intentId);
                if (ppIntent != null) {
                    appIntent = RunApplicationEditorIntentActivity.createIntent(ppIntent);
                    if (appIntent != null) {
                        if (ppIntent._intentType == 0) {
                            /*boolean vpnConnected = false;
                            if (ppIntent._name.equals("")) {

                            }
                            if (!vpnConnected) {*/
                            //noinspection TryWithIdenticalCatches
                            try {
                                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(appIntent);
                                } catch (ActivityNotFoundException ee) {
                                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT,
                                            null, profileName, "");
                                } catch (SecurityException e) {
                                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT,
                                            null, profileName, "");
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            //}
                        }
                        else {
                            try {
                                context.sendBroadcast(appIntent);
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    } else {
                        PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT,
                                null, profileName, "");
                    }
                } else {
                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT,
                            null, profileName, "");
                }
            } else {
                PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT,
                        null, profileName, "");
            }
        } else {
            String packageName = Application.getPackageName(runApplicationData);
            appIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (appIntent != null) {
                appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                //noinspection TryWithIdenticalCatches
                try {
                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(appIntent);
                } catch (ActivityNotFoundException ee) {
                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION,
                            null, profileName, "");
                } catch (SecurityException e) {
                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION,
                            null, profileName, "");
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            } else {
                PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION,
                        null, profileName, "");
            }
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

            synchronized (PPApplication.rootMutex) {
                processPID = -1;
                String command1 = "pidof sk.henrichg.phoneprofilesplus";
                Command command = new Command(0, false, command1) {
                    @Override
                    public void commandOutput(int id, String line) {
                        super.commandOutput(id, line);
                        try {
                            processPID = Integer.parseInt(line);
                        } catch (Exception e) {
                            processPID = -1;
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
                    roottools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    PPApplication.commandWait(command);
                    //if (processPID != -1) {
                        boolean killed = roottools.killProcess(PPApplication.PACKAGE_NAME);
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
                intent.putExtra(PPApplication.EXTRA_BLOCK_PROFILE_EVENT_ACTION, PPApplication.blockProfileEventActions);
                appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
            }
        //}
    }

    /*
    static void executeRootForAdaptiveBrightness(float adaptiveValue, Context context) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadProfileActivation();
        //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), profile, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.executeRootForAdaptiveBrightness");

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && (profile != null) ) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_executeRootForAdaptiveBrightness");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                            (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ + " " +
                                    adaptiveValue;
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, command1); //, command2);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                RootUtils.commandWait(command, "ActivateProfileHelper.executeRootForAdaptiveBrightness");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.executeRootForAdaptiveBrightness", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplication.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
        //}
    }
    */

    static void executeForInteractivePreferences(final Profile profile, final Context context, SharedPreferences executedProfileSharedPreferences) {
        if (profile == null)
            return;

        if (PPApplication.blockProfileEventActions)
            // not start applications after boot
            return;

        Context appContext = context.getApplicationContext();

        if (profile._deviceRunApplicationChange == 1)
        {
//            Log.e("ActivateProfileHelper.executeForInteractivePreferences", "call of ActivateProfileHelper.executeForRunApplications");
            executeForRunApplications(profile, appContext);
        }

        if ((Build.VERSION.SDK_INT < 29) || (Settings.canDrawOverlays(context))) {

            setVPN(context, profile, executedProfileSharedPreferences);

            //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            KeyguardManager myKM = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, executedProfileSharedPreferences, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (profile._deviceMobileDataPrefs == 1) {
                    if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                        boolean ok = true;
                        Intent intent;
                        if (!(((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) ||
                                PPApplication.deviceIsOnePlus)) {
                            try {
                                intent = new Intent(Intent.ACTION_MAIN, null);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                                appContext.startActivity(intent);
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "(1)");
                            } catch (Exception e) {
                                ok = false;
                                // Xiaomi: android.content.ActivityNotFoundException: Unable to find explicit activity class {com.android.settings/com.android.settings.Settings$DataUsageSummaryActivity}; have you declared this activity in your AndroidManifest.xml?
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "1. ERROR" + Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        } else
                            ok = false;
                        if (!ok) {
                            ok = true;
                            try {
                                intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                                intent.setComponent(componentName);
                                appContext.startActivity(intent);
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "(2)");
                            } catch (Exception e) {
                                ok = false;
                                // Xiaomi: java.lang.SecurityException: Permission Denial: starting Intent { act=android.settings.DATA_ROAMING_SETTINGS flg=0x10000000 cmp=com.android.phone/.Settings } from ProcessRecord{215f88f 16252:sk.henrichg.phoneprofilesplus/u0a231} (pid=16252, uid=10231) not exported from uid 1001
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "2. ERROR" + Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                        if (!ok) {
                            try {
                                intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                appContext.startActivity(intent);
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "(3)");
                            } catch (Exception e) {
                                //Log.e("ActivateProfileHelper.executeForInteractivePreferences", "3. ERROR" + Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    } else {
                        boolean ok = false;
                        Intent intent = null;
                        if (!(((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) ||
                                PPApplication.deviceIsOnePlus)) {
                            intent = new Intent(Intent.ACTION_MAIN, null);
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                            if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                                ok = true;
                            else
                                intent = null;
                        }
                        if (!ok) {
                            intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.Settings"));
                            if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                                ok = true;
                            else
                                intent = null;
                        }
                        if (!ok) {
                            intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                            if (GlobalGUIRoutines.activityIntentExists(intent, appContext))
                                ok = true;
                            else
                                intent = null;
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

            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, executedProfileSharedPreferences, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (profile._deviceNetworkTypePrefs == 1) {
                    if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                        try {
                            final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    } else {
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
            if (profile._deviceLocationServicePrefs == 1) {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
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
                        PPApplicationStatic.recordException(e);
                    }
                } else {
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

            if ((profile._deviceWallpaperChange == 2) && (!profile._deviceLiveWallpaper.isEmpty())) {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                        ComponentName componentName = ComponentName.unflattenFromString(profile._deviceLiveWallpaper);
                        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
                        context.startActivity(intent);
                        PPApplicationStatic.setWallpaperChangeTime(appContext);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                    Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                    if (GlobalGUIRoutines.activityIntentExists(intent, appContext)) {
                        ComponentName componentName = ComponentName.unflattenFromString(profile._deviceLiveWallpaper);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                        String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                                appContext.getString(R.string.profile_preferences_deviceLiveWallpaper);
                        showNotificationForInteractiveParameters(appContext, title, text, intent,
                                PPApplication.PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_ID,
                                PPApplication.PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_TAG);
                    }
                }
            }
            if ((profile._deviceWallpaperChange == 4) && (!profile._deviceWallpaper.isEmpty())) {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_ATTACH_DATA);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(Uri.parse(profile._deviceWallpaper), "image/*");
                        intent.putExtra("mimeType", "image/*");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        PPApplicationStatic.setWallpaperChangeTime(appContext);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_ATTACH_DATA);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    if (GlobalGUIRoutines.activityIntentExists(intent, appContext)) {
                        intent.setDataAndType(Uri.parse(profile._deviceWallpaper), "image/*");
                        intent.putExtra("mimeType", "image/*");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                        String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                                appContext.getString(R.string.array_pref_change_image_wallpaper_with);
                        showNotificationForInteractiveParameters(appContext, title, text, intent,
                                PPApplication.PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_ID,
                                PPApplication.PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_TAG);
                    }
                }
            }

            if (profile._deviceVPNSettingsPrefs == 1) {
                if (PPApplication.isScreenOn && (myKM != null) && !myKM.isKeyguardLocked()) {
                    try {
                    /*String PACKAGE_PREFIX =
                            VpnManager.class.getPackage().getName() + ".";
                    String ACTION_VPN_SETTINGS =
                            PACKAGE_PREFIX + "SETTINGS";*/
                        String ACTION_VPN_SETTINGS = "android.net.vpn.SETTINGS";
                        Intent intent = new Intent(ACTION_VPN_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                } else {
                /*String PACKAGE_PREFIX =
                        VpnManager.class.getPackage().getName() + ".";
                String ACTION_VPN_SETTINGS =
                        PACKAGE_PREFIX + "SETTINGS";*/
                    String ACTION_VPN_SETTINGS = "android.net.vpn.SETTINGS";
                    Intent intent = new Intent(ACTION_VPN_SETTINGS);
                    if (GlobalGUIRoutines.activityIntentExists(intent, appContext)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String title = appContext.getString(R.string.profile_activation_interactive_preference_notification_title) + " " + profile._name;
                        String text = appContext.getString(R.string.profile_activation_interactive_preference_notification_text) + " " +
                                appContext.getString(R.string.profile_preferences_deviceVPNSettingsPrefs);
                        showNotificationForInteractiveParameters(appContext, title, text, intent,
                                PPApplication.PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_ID,
                                PPApplication.PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_TAG);
                    }
                }
            }

        }
    }

    static void cancelNotificationsForInteractiveParameters(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID);

        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID);

        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID);

        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID);

        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_ID);

        notificationManager.cancel(
                PPApplication.PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_TAG,
                PPApplication.PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_ID);
    }

    private static void showNotificationForInteractiveParameters(Context context, String title, String text, Intent intent, int notificationId, String notificationTag) {
        Context appContext = context.getApplicationContext();

        PPApplicationStatic.createInformationNotificationChannel(appContext);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(appContext, PPApplication.INFORMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.error_color))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_exclamation_notification))
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        PendingIntent pi = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP);

        Notification notification = mBuilder.build();
        //notification.vibrate = null;
        //notification.defaults &= ~DEFAULT_VIBRATE;

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(notificationTag, notificationId, notification);
        } catch (SecurityException en) {
            Log.e("ActivateProfileHelper.showNotificationForInteractiveParameters", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("ActivateProfileHelper.showNotificationForInteractiveParameters", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void execute(final Context context, final Profile profile)
    {
        final Context appContext = context.getApplicationContext();

        SharedPreferences executedProfileSharedPreferences = appContext.getSharedPreferences("temp_activateProfileHelper_execute", Context.MODE_PRIVATE);
        profile.saveProfileToSharedPreferences(executedProfileSharedPreferences);

        // unlink ring and notifications - it is @Hide :-(
        //Settings.System.putInt(appContext.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        //final Profile profile = _profile; //Profile.getMappedProfile(_profile, appContext);

        // setup volume
        ActivateProfileHelper.executeForVolumes(profile, PhoneCallsListener.LINKMODE_NONE,
                true, appContext, executedProfileSharedPreferences);

        // set vibration on touch
        if (profile._vibrationOnTouch != 0) {
            if (Permissions.checkProfileVibrationOnTouch(appContext, profile, null)) {
                switch (profile._vibrationOnTouch) {
                    case 1:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                        //Settings.System.putInt(context.getContentResolver(), Settings.Global.CHARGING_SOUNDS_ENABLED, 1);
                        // Settings.System.DTMF_TONE_WHEN_DIALING - working
                        // Settings.System.SOUND_EFFECTS_ENABLED - working
                        // Settings.System.LOCKSCREEN_SOUNDS_ENABLED - private secure settings :-(
                        // Settings.Global.CHARGING_SOUNDS_ENABLED - java.lang.IllegalArgumentException: You cannot keep your settings in the secure settings. :-/
                        //                                           (G1) not working :-/
                        break;
                    case 2:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                        //Settings.System.putInt(context.getContentResolver(), Settings.Global.CHARGING_SOUNDS_ENABLED, 0);
                        break;
                }
            }
        }
        // set dtmf tone when dialing
        if (profile._dtmfToneWhenDialing != 0) {
            if (Permissions.checkProfileDtmfToneWhenDialing(appContext, profile, null)) {
                switch (profile._dtmfToneWhenDialing) {
                    case 1:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1);
                        break;
                    case 2:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 0);
                        break;
                }
            }
        }
        // set sound on touch
        if (profile._soundOnTouch != 0) {
            if (Permissions.checkProfileSoundOnTouch(appContext, profile, null)) {
                switch (profile._soundOnTouch) {
                    case 1:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                        break;
                    case 2:
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                        break;
                }
            }
        }

        //// setup radio preferences
        ActivateProfileHelper.executeForRadios(profile, appContext, executedProfileSharedPreferences);

        // setup auto-sync
        try {
            boolean _isAutoSync = ContentResolver.getMasterSyncAutomatically();
            boolean _setAutoSync = false;
            switch (profile._deviceAutoSync) {
                case 1:
                    if (!_isAutoSync) {
                        _isAutoSync = true;
                        _setAutoSync = true;
                    }
                    break;
                case 2:
                    if (_isAutoSync) {
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
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        // screen on permanent
        //if (Permissions.checkProfileScreenTimeout(context, profile, null)) {
            setScreenOnPermanent(profile, appContext);
        //}

        // screen timeout
        if (Permissions.checkProfileScreenTimeout(appContext, profile, null)) {
            //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (PPApplication.isScreenOn) {
                //Log.d("ActivateProfileHelper.execute","screen on");
                if (PPApplication.screenTimeoutHandler != null) {
                    PPApplication.screenTimeoutHandler.post(() -> setScreenTimeout(profile._deviceScreenTimeout, false, appContext));
                }// else
                //    setScreenTimeout(profile._deviceScreenTimeout);
            }
            else {
                setActivatedProfileScreenTimeoutWhenScreenOff(appContext, profile._deviceScreenTimeout);
            }
        }
        //else
        //    PPApplication.setActivatedProfileScreenTimeout(context, 0);

        // on/off lock screen
        boolean setLockScreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lock screen
                setLockScreenDisabled(appContext, false);
                setLockScreen = true;
                break;
            case 2:
                // disable lock screen
                setLockScreenDisabled(appContext, true);
                setLockScreen = true;
                break;
        }
        if (setLockScreen) {
            //boolean isScreenOn;
            //PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            //if (pm != null) {
                boolean keyguardShowing;
                KeyguardManager kgMgr = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardShowing = kgMgr.isKeyguardLocked();

                    if (PPApplication.isScreenOn && !keyguardShowing) {
                        try {
                            //PhoneProfilesService ppService = PhoneProfilesService.getInstance();
                            //if (ppService != null) {
                                GlobalUtils.switchKeyguard(context);
                            //}
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            //}
        }

        // setup display brightness
        if (profile.getDeviceBrightnessChange()) {
            if (Permissions.checkProfileScreenBrightness(appContext, profile, null)) {
                try {
                    //noinspection IfStatementWithIdenticalBranches
                    if (profile.getDeviceBrightnessAutomatic()) {
                        Settings.System.putInt(appContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        if (profile.getDeviceBrightnessChangeLevel()) {
//                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, executedProfileSharedPreferences, true, appContext).allowed
//                                    == PreferenceAllowed.PREFERENCE_ALLOWED) {

                                Settings.System.putInt(appContext.getContentResolver(),
                                        Settings.System.SCREEN_BRIGHTNESS,
                                        profile.getDeviceBrightnessManualValue(appContext));

//                                try {
//                                    Settings.System.putFloat(appContext.getContentResolver(),
//                                            Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ,
//                                            profile.getDeviceBrightnessAdaptiveValue(appContext));
//                                } catch (Exception ee) {
//                                    // run service for execute radios
//                                    ActivateProfileHelper.executeRootForAdaptiveBrightness(
//                                            profile.getDeviceBrightnessAdaptiveValue(appContext),
//                                            appContext);
//                                }
//                            }
                        }
                    } else {
                        Settings.System.putInt(appContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        if (profile.getDeviceBrightnessChangeLevel()) {
                            Settings.System.putInt(appContext.getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    profile.getDeviceBrightnessManualValue(appContext));
                        }
                    }
//                    PPApplication.brightnessModeBeforeScreenOff = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//                    PPApplication.brightnessBeforeScreenOff = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//                    PPApplication.adaptiveBrightnessBeforeScreenOff = Settings.System.getFloat(appContext.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);

                    /*
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
                                createBrightnessView(profile, context);
                            }
                        });
                    } else
                        createBrightnessView(profile, context);
                    */
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        }

        // setup rotation
        if (profile._deviceAutoRotate != 0) {
            if (Permissions.checkProfileAutoRotation(appContext, profile, null)) {
                switch (profile._deviceAutoRotate) {
                    case 1:
                        // set autorotate on
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                        //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                        break;
                    case 6:
                        // set autorotate off
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        //Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                        break;
                    case 2:
                        // set autorotate off
                        // degree 0
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                        break;
                    case 3:
                        // set autorotate off
                        // degree 90
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                        break;
                    case 4:
                        // set autorotate off
                        // degree 180
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                        break;
                    case 5:
                        // set autorotate off
                        // degree 270
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                        break;
                }
            }
        }

        // set notification led
        if (profile._notificationLed != 0) {
            //if (Permissions.checkProfileNotificationLed(context, profile)) { not needed for Android 6+, because root is required
            switch (profile._notificationLed) {
                case 1:
                    setNotificationLed(appContext, 1, executedProfileSharedPreferences);
                    break;
                case 2:
                    setNotificationLed(appContext, 0, executedProfileSharedPreferences);
                    break;
            }
            //}
        }

        // setup image wallpaper
        if (profile._deviceWallpaperChange == 1) {
            if (Permissions.checkProfileImageWallpaper(appContext, profile, null)) {
                ActivateProfileHelper.changeImageWallpaper(profile, appContext);
            }
        }
        // setup random image wallpaper
        if (profile._deviceWallpaperChange == 3) {
            if (Permissions.checkProfileWallpaperFolder(appContext, profile, null)) {
                ActivateProfileHelper.changeWallpaperFromFolder(profile, appContext);
            }
        }

        // set power save mode
        ActivateProfileHelper.setPowerSaveMode(profile, appContext, executedProfileSharedPreferences);

        if (profile._lockDevice != 0) {
            if (Permissions.checkProfileLockDevice(appContext, profile, null)) {
                boolean keyguardLocked;
                KeyguardManager kgMgr = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr != null) {
                    keyguardLocked = kgMgr.isKeyguardLocked();
                    if (!keyguardLocked) {
                        ActivateProfileHelper.lockDevice(profile, appContext);
                    }
                }
            }
        }

        // enable/disable scanners
        if (profile._applicationDisableWifiScanning != 0) {
            boolean oldApplicationEventWifiEnableScanning = ApplicationPreferences.applicationEventWifiEnableScanning;
            boolean newApplicationEventWifiEnableScanning = profile._applicationDisableWifiScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, profile._applicationDisableWifiScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableWifiScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventWifiEnableScanning(appContext);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
            if (oldApplicationEventWifiEnableScanning != newApplicationEventWifiEnableScanning) {
                PPApplicationStatic.restartWifiScanner(appContext);
            }
        }
        if (profile._applicationDisableBluetoothScanning != 0) {
            boolean oldApplicationEventBluetoothEnableScanning = ApplicationPreferences.applicationEventBluetoothEnableScanning;
            boolean newApplicationEventBluetoothEnableScanning = profile._applicationDisableBluetoothScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, profile._applicationDisableBluetoothScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableBluetoothScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
            if (oldApplicationEventBluetoothEnableScanning != newApplicationEventBluetoothEnableScanning) {
                PPApplicationStatic.restartBluetoothScanner(appContext);
            }
        }
        if (profile._applicationDisableLocationScanning != 0) {
            boolean oldApplicationEventLocationEnableScanning = ApplicationPreferences.applicationEventLocationEnableScanning;
            boolean newApplicationEventLocationEnableScanning = profile._applicationDisableLocationScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, profile._applicationDisableLocationScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableLocationScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventLocationEnableScanning(appContext);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
            if (oldApplicationEventLocationEnableScanning != newApplicationEventLocationEnableScanning) {
                PPApplicationStatic.restartLocationScanner(appContext);
            }
        }
        if (profile._applicationDisableMobileCellScanning != 0) {
            boolean oldApplicationEventMobileCellEnableScanning = ApplicationPreferences.applicationEventMobileCellEnableScanning;
            boolean newApplicationEventMobileCellEnableScanning = profile._applicationDisableMobileCellScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, profile._applicationDisableMobileCellScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableMobileCellScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
            if (oldApplicationEventMobileCellEnableScanning != newApplicationEventMobileCellEnableScanning) {
//                PPApplicationStatic.logE("[TEST BATTERY] ActivateProfileHelper.execute", "******** ### *******");
                PPApplicationStatic.restartMobileCellsScanner(appContext);
            }
        }
        if (profile._applicationDisableOrientationScanning != 0) {
            boolean oldApplicationEventOrientationEnableScanning = ApplicationPreferences.applicationEventOrientationEnableScanning;
            boolean newApplicationEventOrientationEnableScanning = profile._applicationDisableOrientationScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, profile._applicationDisableOrientationScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableOrientationScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventOrientationEnableScanning(appContext);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
            if (oldApplicationEventOrientationEnableScanning != newApplicationEventOrientationEnableScanning) {
//                PPApplicationStatic.logE("[TEST BATTERY] ActivateProfileHelper.execute", "******** ### *******");
                PPApplicationStatic.restartOrientationScanner(appContext);
            }
        }
        if (profile._applicationDisableNotificationScanning != 0) {
            boolean oldApplicationEventNotificationEnableScanning = ApplicationPreferences.applicationEventNotificationEnableScanning;
            boolean newApplicationEventNotificationEnableScanning = profile._applicationDisableNotificationScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, profile._applicationDisableNotificationScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisableNotificationScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventNotificationEnableScanning(appContext);
            ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(appContext);
            if (oldApplicationEventNotificationEnableScanning != newApplicationEventNotificationEnableScanning) {
                PPApplicationStatic.restartNotificationScanner(appContext);
            }
        }
        if (profile._applicationDisablePeriodicScanning != 0) {
            boolean oldApplicationEventPeriodicEnableScanning = ApplicationPreferences.applicationEventPeriodicScanningEnableScanning;
            boolean newApplicationEventPeriodicEnableScanning = profile._applicationDisablePeriodicScanning == 2;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, profile._applicationDisablePeriodicScanning == 2);
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE, profile._applicationDisablePeriodicScanning == 1);
            editor.apply();
            ApplicationPreferences.applicationEventPeriodicScanningEnableScanning(appContext);
            ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(appContext);
            if (oldApplicationEventPeriodicEnableScanning != newApplicationEventPeriodicEnableScanning) {
                PPApplicationStatic.restartPeriodicScanningScanner(appContext);
            }
        }

        // set heads-up notifications
        if (profile._headsUpNotifications != 0) {
            switch (profile._headsUpNotifications) {
                case 1:
                    setHeadsUpNotifications(appContext, 1, executedProfileSharedPreferences);
                    break;
                case 2:
                    setHeadsUpNotifications(appContext, 0, executedProfileSharedPreferences);
                    break;
            }
        }

        // set screen dark mode
        if (profile._screenDarkMode != 0) {
            setScreenDarkMode(context, profile._screenDarkMode, executedProfileSharedPreferences);
        }

        //if (android.os.Build.VERSION.SDK_INT >= 26) {
            // set always on display
            if (profile._alwaysOnDisplay != 0) {
                switch (profile._alwaysOnDisplay) {
                    case 1:
                        setAlwaysOnDisplay(appContext, 1, executedProfileSharedPreferences);
                        break;
                    case 2:
                        setAlwaysOnDisplay(appContext, 0, executedProfileSharedPreferences);
                        break;
                }
            }
        //}

        // close all applications

        if ((Build.VERSION.SDK_INT < 29) || (Settings.canDrawOverlays(context))) {
            if (profile._deviceCloseAllApplications == 1) {
                if (!PPApplication.blockProfileEventActions) {
                    // work for first start events or activate profile on boot

    //                PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** ActivateProfileHelper.execute", "schedule - profile._deviceCloseAllApplications");

                    final String profileName = profile._name;
                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
    //                    long start = System.currentTimeMillis();
    //                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** ActivateProfileHelper.execute", "--------------- START - profile._deviceCloseAllApplications");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_execute_closeAllApplicaitons");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (!PPApplication.blockProfileEventActions) {
                                try {
                                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                                    startMain.addCategory(Intent.CATEGORY_HOME);
                                    startMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //startMain.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    appContext.startActivity(startMain);
                                /*} catch (SecurityException e) {
                                    //Log.e("ActivateProfileHelper.execute", Log.getStackTraceString(e));
                                    String profileName = getInputData().getString(PPApplication.EXTRA_PROFILE_NAME);
                                    ActivateProfileHelper.showError(appContext, profileName, Profile.PARAMETER_CLOSE_ALL_APPLICATION);*/
                                } catch (Exception e) {
                                    //Log.e("ActivateProfileHelper.execute", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                    //ActivateProfileHelper.showError(appContext, profileName, Profile.PARAMETER_CLOSE_ALL_APPLICATION);
                                    PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_CLOSE_ALL_APPLICATIONS,
                                            null, profileName, "");
                                }
                            }

    //                        long finish = System.currentTimeMillis();
    //                        long timeElapsed = finish - start;
    //                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** ActivateProfileHelper.execute", "--------------- END - profile._deviceCloseAllApplications -timeElapsed="+timeElapsed);
                        } catch (Exception e) {
    //                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            //worker.shutdown();
                        }
                    };
                    PPApplicationStatic.createDelayedProfileActivationExecutor();
                    PPApplication.delayedProfileActivationExecutor.schedule(runnable, 1500, TimeUnit.MILLISECONDS);
                    /*
                    Data workData = new Data.Builder()
                            .putString(PPApplication.EXTRA_PROFILE_NAME, profile._name)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG)
                                    .setInputData(workData)
                                    .setInitialDelay(1500, TimeUnit.MILLISECONDS)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

    //                            //if (PPApplicationStatic.logEnabled()) {
    //                            ListenableFuture<List<WorkInfo>> statuses;
    //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG);
    //                            try {
    //                                List<WorkInfo> workInfoList = statuses.get();
    //                            } catch (Exception ignored) {
    //                            }
    //                            //}

    //                            PPApplicationStatic.logE("[WORKER_CALL] ActivateProfileHelper.execute", "xxx");
                                workManager.enqueueUniqueWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                    */
                }
            }
        }

        generateNotifiction(appContext, profile);

        setCameraFlash(appContext, profile, executedProfileSharedPreferences);

        setVibrationIntensity(appContext, profile, executedProfileSharedPreferences);

        if (profile._applicationDisableGloabalEventsRun != 0) {
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0);
            dataWrapper.globalRunStopEvents(profile._applicationDisableGloabalEventsRun == 1);
        }

        if (profile._deviceForceStopApplicationChange == 1) {
            boolean enabled;
            enabled = sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isEnabled(appContext/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, true
                            /*, "ActivateProfileHelper.execute (profile._deviceForceStopApplicationChange)"*/);
            if (enabled) {
                // executeForInteractivePreferences() is called from broadcast receiver PPExtenderBroadcastReceiver
                ActivateProfileHelper.executeForForceStopApplications(profile, appContext);
            }
        }
        else {
//            Log.e("ActivateProfileHelper.execute", "call of ActivateProfileHelper.executeForInteractivePreferences");
            executeForInteractivePreferences(profile, appContext, executedProfileSharedPreferences);
        }
    }

    static void setScreenTimeout(int screenTimeout, boolean forceSet, Context context) {
        Context appContext = context.getApplicationContext();

        disableScreenTimeoutInternalChange = true;
        //Log.d("ActivateProfileHelper.setScreenTimeout", "current="+Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0));
        switch (screenTimeout) {
            case 1:
                //removeScreenTimeoutAlwaysOnView(context);
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 15000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 15000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    } else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                }
                break;
            case 2:
                //removeScreenTimeoutAlwaysOnView(context);
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 30000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 30000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                }
                break;
            case 3:
                //removeScreenTimeoutAlwaysOnView(context);
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 60000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 60000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                }
                break;
            case 4:
                //removeScreenTimeoutAlwaysOnView(context);
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 120000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 120000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                }
                break;
            case 5:
                //removeScreenTimeoutAlwaysOnView(context);
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 600000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 600000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                }
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
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 300000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 300000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                }
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
                //if ((PPApplication.lockDeviceActivity != null) && (!forceSet))
                if (PPApplication.lockDeviceActivityDisplayed && (!forceSet))
                    // in LockDeviceActivity.onDestroy() will be used this value to revert back system screen timeout
                    PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = 1800000;
                else {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 1800000";
                            //if (PPApplication.isSELinuxEnforcing())
                            //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                            Command command = new Command(0, false, command1); //, command2);
                            try {
                                RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                PPApplication.commandWait(command, "ActivateProfileHelper.setScreenTimeout");
                            } catch (Exception e) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                    else*/
                        Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1800000);
                }
                break;
        }
        setActivatedProfileScreenTimeoutWhenScreenOff(appContext, 0);

        PPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor();

        /*PPApplication.startHandlerThreadInternalChangeToFalse();
        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
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

                final Handler handler = new Handler(context.getMainLooper());
                handler.post(() -> {
                    try {
                        windowManager.addView(PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView, params);
                    } catch (Exception e) {
                        PhoneProfilesService.getInstance().screenTimeoutAlwaysOnView = null;
                    }
                });
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
    static void createBrightnessView(Profile profile, Context context)
    {
        //if (PhoneProfilesService.getInstance() != null) {
            final Context appContext = context.getApplicationContext();

            WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                if (PPApplication.brightnessView != null) {
                    try {
                        windowManager.removeView(PPApplication.brightnessView);
                    } catch (Exception ignored) {
                    }
                    PPApplication.brightnessView = null;
                }
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
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT
                );
                if (profile.getDeviceBrightnessAutomatic() || (!profile.getDeviceBrightnessChangeLevel()))
                    params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                else
                    params.screenBrightness = profile.getDeviceBrightnessManualValue(appContext) / (float) 255;
                PPApplication.brightnessView = new BrightnessView(appContext);

                final Handler handler = new Handler(context.getMainLooper());
                handler.post(() -> {
                    try {
                        windowManager.addView(PPApplication.brightnessView, params);
                    } catch (Exception e) {
                        PPApplication.brightnessView = null;
                    }
                });
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (windowManager != null) {
                            if (PPApplication.brightnessView != null) {
                                try {
                                    windowManager.removeView(PPApplication.brightnessView);
                                } catch (Exception ignored) {
                                }
                                PPApplication.brightnessView = null;
                            }
                        }
                    }
                }, 5000);
//                PostDelayedBroadcastReceiver.setAlarm(PostDelayedBroadcastReceiver.ACTION_REMOVE_BRIGHTNESS_VIEW,5, context);
            }
        //}
    }

    static void removeBrightnessView(Context context) {
        //if (PhoneProfilesService.getInstance() != null) {
            if (PPApplication.brightnessView != null) {
                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(PPApplication.brightnessView);
                    } catch (Exception ignored) {
                    }
                    PPApplication.brightnessView = null;
                }
            }
        //}
    }
    */

    static void showKeepScreenOnNotificaiton(Context context) {

        String nTitle = "\"" + context.getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"=" +
                "\"" +context.getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_On) + "\"";
        String nText = "\"" + context.getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"" +
                " " + context.getString(R.string.keep_screen_on_active_notification_title_1) + " " +
                "\"" +context.getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_On) + "\". " +
                context.getString(R.string.keep_screen_on_active_notification_decription_1) +
                " \"" +context.getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_Off) + "\", " +
                context.getString(R.string.keep_screen_on_active_notification_decription_2) +
                " \"" + context.getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"=" +
                "\"" + context.getString(R.string.array_pref_hardwareModeArray_off) + "\".";

        PPApplicationStatic.createKeepScreenOnNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.KEEP_SCREEN_ON_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.information_color))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_information_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_information_notification))
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOngoing(true);

        if (Build.VERSION.SDK_INT >= 33) {
            Intent deleteIntent = new Intent(KeepScreenOnNotificationDeletedReceiver.KEEP_SCREEN_ON_NOTIFICATION_DELETED_ACTION);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setDeleteIntent(deletePendingIntent);
        }

        mBuilder.setGroup(PPApplication.KEEP_SCREEN_ON_NOTIFICATION_GROUP);

        Notification notification = mBuilder.build();
        //notification.vibrate = null;
        //notification.defaults &= ~DEFAULT_VIBRATE;

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.KEEP_SCREEN_ON_NOTIFICATION_TAG,
                    PPApplication.KEEP_SCREEN_ON_NOTIFICATION_ID, notification);
        } catch (SecurityException en) {
            Log.e("ActivateProfileHelper.showKeepScreenOnNotificaiton", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static private void removeKeepScreenOnNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(
                PPApplication.KEEP_SCREEN_ON_NOTIFICATION_TAG,
                PPApplication.KEEP_SCREEN_ON_NOTIFICATION_ID);
    }

    static void createKeepScreenOnView(Context context) {
        //removeKeepScreenOnView();

        final Context appContext = context.getApplicationContext();

        /*
        //if (PhoneProfilesService.getInstance() != null) {

        //PhoneProfilesService service = PhoneProfilesService.getInstance();
        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            try {
                if (PPApplication.keepScreenOnWakeLock == null)
                    PPApplication.keepScreenOnWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_createKeepScreenOnView");
            } catch(Exception e) {
                PPApplication.keepScreenOnWakeLock = null;
                Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
            try {
                if ((PPApplication.keepScreenOnWakeLock != null) && (!PPApplication.keepScreenOnWakeLock.isHeld())) {
                    PPApplication.keepScreenOnWakeLock.acquire();
                }
            } catch (Exception e) {
                Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
        */

        if (PPApplication.keepScreenOnView != null)
            removeKeepScreenOnView(context);
        final WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
//            Log.e("ActivateProfileHelper.createKeepScreenOnView", "xxx");
            int type;
            //if (android.os.Build.VERSION.SDK_INT < 25)
            //    type = WindowManager.LayoutParams.TYPE_TOAST;
            //else
            //if (android.os.Build.VERSION.SDK_INT < 26)
            //    type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY; // add show ACTION_MANAGE_OVERLAY_PERMISSION to Permissions app Settings
            //else
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
            // keep this: it is required to use MainLooper for cal listener
            final Handler handler = new Handler(context.getMainLooper());
            handler.post(() -> {
                try {
                    windowManager.addView(PPApplication.keepScreenOnView, params);
                    setKeepScreenOnPermanent(context, true);
                    showKeepScreenOnNotificaiton(appContext);
                } catch (Exception e) {
//                        Log.e("ActivateProfileHelper.createKeepScreenOnView", Log.getStackTraceString(e));
                    PPApplication.keepScreenOnView = null;
                    setKeepScreenOnPermanent(context, false);
                    removeKeepScreenOnNotification(appContext);
                }
            });

        }
    }

    static void removeKeepScreenOnView(Context context)
    {
        final Context appContext = context.getApplicationContext();

        //if (PhoneProfilesService.getInstance() != null) {
            //final Context appContext = context.getApplicationContext();

            //PhoneProfilesService service = PhoneProfilesService.getInstance();

            /*try {
                if ((PPApplication.keepScreenOnWakeLock != null) && PPApplication.keepScreenOnWakeLock.isHeld())
                    PPApplication.keepScreenOnWakeLock.release();
            } catch (Exception e) {
                Log.e("ActivateProfileHelper.removeKeepScreenOnView", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
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
                    setKeepScreenOnPermanent(context, false);
                    removeKeepScreenOnNotification(appContext);
                }
            }
        //}
    }

    static boolean isAirplaneMode(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        //else
        //    return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private static void setAirplaneMode(Context context, boolean mode, boolean useAssistant)
    {
        boolean isRooted = RootUtils.isRooted(false);
        boolean settingsBinaryExists = RootUtils.settingsBinaryExists(false);
        if (isRooted && (!useAssistant) &&
            (!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
            settingsBinaryExists) {
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
                Command command = new Command(0, /*false,*/ command1, command2);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    RootUtils.commandWait(command, "ActivateProfileHelper.setAirplaneMode");
                } catch (Exception e) {
                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                    //Log.e("ActivateProfileHelper.setAirplaneMode", Log.getStackTraceString(e));
                    //PPApplicationStatic.recordException(e);
                }
            }
        }
        else
        if (useAssistant && isPPPSetAsDefaultAssistant(context)) {
            //if (Build.VERSION.SDK_INT >= 26) {
                Intent intent = new Intent(PPVoiceService.ACTION_ASSISTANT);
                intent.putExtra("ACTION", Settings.ACTION_VOICE_CONTROL_AIRPLANE_MODE);
                intent.putExtra(Settings.EXTRA_AIRPLANE_MODE_ENABLED, mode);
                context.sendBroadcast(intent);
            //}
        }
    }

    static boolean isMobileData(Context context, int simCard)
    {
        Context appContext = context.getApplicationContext();

        /*if (android.os.Build.VERSION.SDK_INT < 28)
        {
            TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                Method getDataEnabledMethod;
                Class<?> telephonyManagerClass;

                try {
                    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    getDataEnabledMethod = telephonyManagerClass.getDeclaredMethod("getDataEnabled");
                    getDataEnabledMethod.setAccessible(true);

                    return (Boolean) getDataEnabledMethod.invoke(telephonyManager);

                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;

        }
        else*/

        try {
            boolean enabled = false;
            if (Permissions.checkPhone(appContext.getApplicationContext())) {
                boolean ok = false;
                ITelephony adapter = ITelephony.Stub.asInterface(ServiceManager.getService("phone")); // service list | grep ITelephony
                if (adapter != null) {
                    if (/*(Build.VERSION.SDK_INT > 23) &&*/ (simCard > 0)) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(appContext);
                        if (mSubscriptionManager != null) {
                            int defaultDataId;// = 0;
                            defaultDataId = SubscriptionManager.getDefaultDataSubscriptionId();

                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            } catch (SecurityException e) {
                                PPApplicationStatic.recordException(e);
                            }
                            if (subscriptionList != null) {
                                for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                    if (subscriptionInfo != null) {
                                        int slotIndex = subscriptionInfo.getSimSlotIndex();
                                        if (simCard == (slotIndex + 1)) {
                                            int subscriptionId = subscriptionInfo.getSubscriptionId();
                                            enabled = adapter.getDataEnabled(subscriptionId) && (subscriptionId == defaultDataId);
                                            ok = true;
                                        }
                                        if (ok)
                                            break;
                                    }
                                }
                            }
                        }
                        if (!ok) {
                            //int dataState = adapter.getDataState();
                            //enabled = dataState == TelephonyManager.DATA_CONNECTED;
                            //enabled = adapter.getDataEnabled(0);

                            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null)
                                //noinspection deprecation
                                enabled = telephonyManager.getDataEnabled();
                        }
                    }
                    else {
                        //int dataState = adapter.getDataState();
                        //enabled = dataState == TelephonyManager.DATA_CONNECTED;

                        //enabled = adapter.getDataEnabled(0);
                        //PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.isMobileData", "enabled=" + enabled);

                        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null)
                            //noinspection deprecation
                            enabled = telephonyManager.getDataEnabled();
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.isMobileData", "enabled=" + enabled);
                    }
                }
            }
            return enabled;
        } catch (Throwable e) {
            //Log.e("ActivateProfileHelper.isMobileData", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return false;
        }
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

    private static void setMobileData(Context context, boolean enable,
                                      @SuppressWarnings("SameParameterValue") int simCard)
    {
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "simCard="+simCard);
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "enable="+enable);

        //Context appContext = context.getApplicationContext();

        GlobalUtils.HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
        if (simCard == 1) {
            boolean sim1Exists = hasSIMCardData.hasSIM1;
            simExists = simExists && sim1Exists;
        }
        else
        if (simCard == 2) {
            boolean sim2Exists = hasSIMCardData.hasSIM2;
            simExists = simExists && sim2Exists;
        }
        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                RootUtils.isRooted(false) &&
            simExists)
        {
            if (Permissions.checkPhone(context.getApplicationContext())) {
//                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "ask for root enabled and is rooted");
                if (/*(Build.VERSION.SDK_INT < 26) ||*/ (simCard == 0)) {
                    synchronized (PPApplication.rootMutex) {
                        String command1 = "svc data " + (enable ? "enable" : "disable");
                        Command command = new Command(0, /*false,*/ command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                            RootUtils.commandWait(command, "ActivateProfileHelper.setMobileData");
                        } catch (Exception e) {
                            //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                        }
                    }
                }/* else {
                    // dual sim temporary removed, Samsung, Xiaomi, Huawei do not have option for this in Settings

                    // dual sim is supported by TelephonyManager from API 26

                    // Get the value of the "TRANSACTION_setDataEnabled" field.
                    //Object serviceManager = PPApplication.getServiceManager("phone");
                    int transactionCode;
                    if (Build.VERSION.SDK_INT >= 28)
                        transactionCode = PPApplication.rootMutex.transactionCode_setUserDataEnabled;
                    else
                        transactionCode = PPApplication.rootMutex.transactionCode_setDataEnabled;

                    int state = enable ? 1 : 0;

                    if (transactionCode != -1) {
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "transactionCode=" + transactionCode);

                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(appContext);
                        if (mSubscriptionManager != null) {
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "mSubscriptionManager != null");
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionList=" + subscriptionList);
                            } catch (SecurityException e) {
                                PPApplicationStatic.recordException(e);
                            }
                            if (subscriptionList != null) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionList.size()=" + subscriptionList.size());
                                for (int i = 0; i < subscriptionList.size(); i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionInfo=" + subscriptionInfo);
                                    if (subscriptionInfo != null) {
                                        int slotIndex = subscriptionInfo.getSimSlotIndex();
                                        // disable mobile data for all  SIM cards
                                        //if (simCard == (slotIndex+1)) {
                                            int subscriptionId = subscriptionInfo.getSubscriptionId();
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionId=" + subscriptionId);
                                            synchronized (PPApplication.rootMutex) {
                                                String command1 = RootUtils.getServiceCommand("phone", transactionCode, subscriptionId, state);
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "command1=" + command1);
                                                if (command1 != null) {
                                                    Command command = new Command(0, command1);
//                                                    {
//                                                        @Override
//                                                        public void commandOutput(int id, String line) {
//                                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "command output -> line=" + line);
//                                                            super.commandOutput(id, line);
//                                                        }
//                                                    };
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setMobileData");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        //}
                                    }
//                                    else
//                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionInfo == null");
                                }
                            }
//                            else
//                               PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "subscriptionList == null");
                        }
//                        else
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "mSubscriptionManager == null");
                    }
//                    else
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setMobileData", "transactionCode == -1");
                }*/
            }
        }
    }

    private static void setWifiInAirplaneMode(/*Context context, */boolean enable)
    {
        //Context appContext = context.getApplicationContext();

        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                RootUtils.isRooted(false)) {
            synchronized (PPApplication.rootMutex) {
                String command1 = "svc wifi " + (enable ? "enable" : "disable");
                Command command = new Command(0, /*false,*/ command1);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                    RootUtils.commandWait(command, "ActivateProfileHelper.setWifiInAirplaneMode");
                } catch (Exception e) {
                    //Log.e("ActivateProfileHelper.setWifiInAirplaneMode", Log.getStackTraceString(e));
                }
            }
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
                        roottools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.getPreferredNetworkType", Log.getStackTraceString(e));
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

    static boolean telephonyServiceExists(String preference) {
        try {
            int transactionCode = -1;
            switch (preference) {
                case Profile.PREF_PROFILE_DEVICE_MOBILE_DATA:
                //case Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1:
                //case Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2:
                    if (Build.VERSION.SDK_INT >= 28)
                        transactionCode = PPApplication.rootMutex.transactionCode_setUserDataEnabled;
                    else
                        transactionCode = PPApplication.rootMutex.transactionCode_setDataEnabled;
                    break;
                case Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE:
                case Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1:
                case Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2:
                    transactionCode = PPApplication.rootMutex.transactionCode_setPreferredNetworkType;
                    break;
                case Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS:
                    int transactionCodeVoice = PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId;
                    int transactionCodeSMS = PPApplication.rootMutex.transactionCode_setDefaultSmsSubId;
                    int transactionCodeData = PPApplication.rootMutex.transactionCode_setDefaultDataSubId;
                    if ((transactionCodeVoice != -1) || (transactionCodeSMS != -1) || (transactionCodeData != -1))
                        transactionCode = 1;
                    break;
                case Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1:
                case Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2:
                    transactionCode = PPApplication.rootMutex.transactionCode_setSubscriptionEnabled;
                    break;
            }

            return transactionCode != -1;
        } catch(Exception e) {
            return false;
        }
    }

    private static void setPreferredNetworkType(Context context, int networkType, int simCard)
    {
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "simCard="+simCard);
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "networkType="+networkType);

        GlobalUtils.HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
        if (simCard == 1) {
            boolean sim1Exists = hasSIMCardData.hasSIM1;
            simExists = simExists && sim1Exists;
        }
        else
        if (simCard == 2) {
            boolean sim2Exists = hasSIMCardData.hasSIM2;
            simExists = simExists && sim2Exists;
        }

        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                RootUtils.isRooted(false) &&
                RootUtils.serviceBinaryExists(false) &&
            simExists)
        {
            if (Permissions.checkPhone(context.getApplicationContext())) {
//                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "ask for root enabled and is rooted");
                try {
                    // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                    int transactionCode = PPApplication.rootMutex.transactionCode_setPreferredNetworkType;
                    if (transactionCode != -1) {
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "transactionCode=" + transactionCode);

                        // Android 5.1?
                        //if (Build.VERSION.SDK_INT >= 22) {
                        Context appContext = context.getApplicationContext();
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //SubscriptionManager.from(context);
                        if (mSubscriptionManager != null) {
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "mSubscriptionManager != null");
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionList=" + subscriptionList);
                            } catch (SecurityException e) {
                                PPApplicationStatic.recordException(e);
                            }
                            if (subscriptionList != null) {
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionList.size()=" + subscriptionList.size());
                                for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionInfo=" + subscriptionInfo);
                                    if (subscriptionInfo != null) {
                                        int slotIndex = subscriptionInfo.getSimSlotIndex();
                                        if (/*(Build.VERSION.SDK_INT < 26) ||*/ (simCard == 0) || (simCard == (slotIndex+1))) {
                                            // dual sim is supported by TelephonyManager from API 26

                                            int subscriptionId = subscriptionInfo.getSubscriptionId();
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionId=" + subscriptionId);
                                            synchronized (PPApplication.rootMutex) {
                                                String command1 = RootUtils.getServiceCommand("phone", transactionCode, subscriptionId, networkType);
//                                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "command1=" + command1);
                                                if (command1 != null) {
                                                    Command command = new Command(0, /*false,*/ command1)/* {
                                                        @Override
                                                        public void commandOutput(int id, String line) {
                                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "command output -> line=" + line);
                                                            super.commandOutput(id, line);
                                                        }
                                                    }*/;
                                                    try {
                                                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                        RootUtils.commandWait(command, "ActivateProfileHelper.setPreferredNetworkType");
                                                    } catch (Exception e) {
                                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                        //Log.e("ActivateProfileHelper.setPreferredNetworkType", Log.getStackTraceString(e));
                                                        //PPApplicationStatic.recordException(e);
                                                    }
                                                }
                                            }
                                        }
                                    }
//                                    else
//                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionInfo == null");
                                }
                            }
//                            else
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "subscriptionList == null");
                        }
//                        else
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "mSubscriptionManager == null");
                    }
//                    else
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setPreferredNetworkType", "transactionCode == -1");
                } catch (Exception ee) {
                    PPApplicationStatic.recordException(ee);
                }
            }
        }
    }

    static boolean wifiServiceExists(/*Context context, */
            @SuppressWarnings("SameParameterValue") String preference) {
        try {
            int transactionCode = -1;
            if (preference.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
                transactionCode = PPApplication.rootMutex.transactionCode_setWifiApEnabled;
            return transactionCode != -1;
        } catch(Exception e) {
            //Log.e("ActivateProfileHelper.wifiServiceExists",Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return false;
        }
    }

    private static void setWifiAP(WifiApManager wifiApManager, boolean enable, boolean doNotChangeWifi,
                                  Profile profile, Context context) {
        try {

            /*if (Build.VERSION.SDK_INT < 26) {
                // for Android 7
                wifiApManager.setWifiApState(enable, doNotChangeWifi);
            }
            else*/
            if (Build.VERSION.SDK_INT < 28) {
                // for Android 8
                Context appContext = context.getApplicationContext();
                if (WifiApManager.canExploitWifiTethering(appContext)) {
                    if (enable)
                        wifiApManager.startTethering(doNotChangeWifi);
                    else
                        wifiApManager.stopTethering();
                }
                else
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                        (RootUtils.isRooted(false) && RootUtils.serviceBinaryExists(false))) {
                    int transactionCode = PPApplication.rootMutex.transactionCode_setWifiApEnabled;

                    if (transactionCode != -1) {
                        if (enable && (!doNotChangeWifi)) {
                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                            if (wifiManager != null) {
                                int wifiState = wifiManager.getWifiState();
                                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                                if (isWifiEnabled) {
                                    wifiManager.setWifiEnabled(false);
                                    GlobalUtils.sleep(1000);
                                }
                            }
                        }
                        synchronized (PPApplication.rootMutex) {
                            String command1 = RootUtils.getServiceCommand("wifi", transactionCode, 0, (enable) ? 1 : 0);
                            if (command1 != null) {
                                Command command = new Command(0, /*false,*/ command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    RootUtils.commandWait(command, "ActivateProfileHelper.setWifiAP");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
                }
            }
            else if (Build.VERSION.SDK_INT < 30) {
                // for Android 9, 10
                //Context appContext = context.getApplicationContext();
                //if (WifiApManager.canExploitWifiTethering(appContext)) {
                    if (enable)
                        wifiApManager.startTethering(doNotChangeWifi);
                    else
                        wifiApManager.stopTethering();
                //}
            }
            else {
                if (enable)
                    WifiApManager.startTethering30(context, doNotChangeWifi);
                else
                    WifiApManager.stopTethering30(context);
            }

        } catch (SecurityException e) {
            //showError(context, profile._name, Profile.PARAMETER_TYPE_WIFIAP);
            PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_WIFIAP,
                    null, profile._name, "");
        } catch (Exception e) {
            //Log.e("ActivateProfileHelper.setWifiAP", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    private static void setNFC(Context context, boolean enable)
    {
        /*
        Not working in debug version of application !!!!
        Test with release version.
        */

        Context appContext = context.getApplicationContext();

        boolean G1OK = false;
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            CmdNfc.setNFC(enable);
            G1OK = true;
        }
        if (!G1OK) {
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                    (RootUtils.isRooted(false))) {
                synchronized (PPApplication.rootMutex) {
                    String command1 = RootUtils.getJavaCommandFile(CmdNfc.class, "nfc", appContext, enable);
                    if (command1 != null) {
                        Command command = new Command(0, /*false,*/ command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                            RootUtils.commandWait(command, "ActivateProfileHelper.setNFC");
                        } catch (Exception e) {
                            // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                            //Log.e("ActivateProfileHelper.setNFC", Log.getStackTraceString(e));
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                    //String command = PPApplication.getJavaCommandFile(CmdNfc.class, "nfc", context, enable);
                    //if (command != null)
                    //  RootToolsSmall.runSuCommand(command);
                }
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
        //Log.e("ActivateProfileHelper.setGPS", "GPS="+isEnabled);
        //Log.e("ActivateProfileHelper.setGPS", "enable="+enable);
        if (!ok)
            return;

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled) && enable)
        {
            boolean G1OK = false;
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                //Log.e("ActivateProfileHelper.setGPS", "(G1) granted");
                try {
                    /* not working :-(
                    if (Build.VERSION.SDK_INT >= 29) {
                        Log.e("ActivateProfileHelper.setGPS", "setProviderEnabledForUser()");
                        locationManager.setProviderEnabledForUser(LocationManager.GPS_PROVIDER, true, android.os.Process.myUserHandle());
                    }
                    else*/ {
                        String newSet;
                        newSet = "+gps";
                        //noinspection deprecation
                        Settings.Secure.putString(appContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
                    }
                    G1OK = true;
                } catch (Exception ee) {
                    Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(ee));
                }
            }
            if (!G1OK) {
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                        (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                    // device is rooted

                    String command1;

                    synchronized (PPApplication.rootMutex) {
                        command1 = "settings put secure location_providers_allowed +gps";
                        Command command = new Command(0, /*false,*/ command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            RootUtils.commandWait(command, "ActivateProfileHelper.setGPS (1)");
                        } catch (Exception e) {
                            // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                            //Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }
                /*else
                if (canExploitGPS(appContext))
                {
                    final Intent poke = new Intent();
                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                    poke.setData(Uri.parse("3"));
                    appContext.sendBroadcast(poke);
                }*/
            }
        }
        else
        //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
        if (isEnabled && (!enable))
        {
            boolean G1OK = false;
            // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
            if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                //Log.e("ActivateProfileHelper.setGPS", "(G1) granted");
                try {
                    /* not working :-(
                    if (Build.VERSION.SDK_INT >= 29) {
                        Log.e("ActivateProfileHelper.setGPS", "setProviderEnabledForUser()");
                        locationManager.setProviderEnabledForUser(LocationManager.GPS_PROVIDER, true, android.os.Process.myUserHandle());
                    }
                    else*/ {
                        String newSet;// = "";
                        newSet = "-gps";
                        //noinspection deprecation
                        Settings.Secure.putString(appContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newSet);
                    }
                    G1OK = true;
                } catch (Exception ee) {
                    Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(ee));
                }
            }
            if (!G1OK) {
                if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                        (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                    // device is rooted

                    String command1;

                    synchronized (PPApplication.rootMutex) {
                        command1 = "settings put secure location_providers_allowed -gps";
                        Command command = new Command(0, /*false,*/ command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            RootUtils.commandWait(command, "ActivateProfileHelper.setGPS (2)");
                        } catch (Exception e) {
                            // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                            //Log.e("ActivateProfileHelper.setGPS", Log.getStackTraceString(e));
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }
                /*else
                if (canExploitGPS(appContext))
                {
                    final Intent poke = new Intent();
                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                    poke.setData(Uri.parse("3"));
                    appContext.sendBroadcast(poke);
                }*/
            }
        }
    }

    static int getLocationMode(Context context) {
        return Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
    }

    private static void setLocationMode(Context context, int mode)
    {
        Context appContext = context.getApplicationContext();

        //boolean G1OK = false;
        // adb shell pm grant sk.henrichg.phoneprofilesplus android.permission.WRITE_SECURE_SETTINGS
        if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            try {
                Settings.Secure.putInt(appContext.getContentResolver(), Settings.Secure.LOCATION_MODE, mode);
                //G1OK = true;
            } catch (Exception ee) {
                Log.e("ActivateProfileHelper.setLocationMode", Log.getStackTraceString(ee));
            }
        }
        /*if (!G1OK) {
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists(false)))
            {
                // device is rooted - NOT WORKING

                String command1;

                synchronized (PPApplication.rootMutex) {
                    command1 = "settings put secure location_mode " + mode;
                    Command command = new Command(0, false, command1);
                    try {
                        roottools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        PPApplication.commandWait(command, "ActivateProfileHelper.setLocationMode");
                    } catch (Exception e) {
                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                        //Log.e("ActivateProfileHelper.setLocationMode", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }*/
    }

    private static void setPowerSaveMode(Profile profile, Context context, SharedPreferences executedProfileSharedPreferences) {
        if (profile._devicePowerSaveMode != 0) {
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadProfileActivation();
            //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(
            //        context.getApplicationContext(), profile, executedProfileSharedPreferences) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.setPowerSaveMode");

                //Context appContext= appContextWeakRef.get();
                //Profile profile = profileWeakRef.get();
                //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

                //if ((appContext != null) && (profile != null) && (executedProfileSharedPreferences != null)) {

                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, executedProfileSharedPreferences, false, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_setPowerSaveMode");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

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
                                    boolean G1OK = false;
                                    if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                                        try {
                                            //if (android.os.Build.VERSION.SDK_INT >= 21)
                                            Settings.Global.putInt(appContext.getContentResolver(), "low_power", ((_isPowerSaveMode) ? 1 : 0));
                                            G1OK = true;
                                        } catch (Exception ee) {
                                            Log.e("ActivateProfileHelper.setPowerSaveMode", Log.getStackTraceString(ee));
                                        }
                                    }
                                    if (!G1OK) {
                                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                                (RootUtils.isRooted(false) && RootUtils.settingsBinaryExists(false))) {
                                            synchronized (PPApplication.rootMutex) {
                                                String command1 = "settings put global low_power " + ((_isPowerSaveMode) ? 1 : 0);
                                                Command command = new Command(0, /*false,*/ command1);
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setPowerSaveMode");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                    //Log.e("ActivateProfileHelper.setPowerSaveMode", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createProfileActiationExecutorPool();
            PPApplication.profileActiationExecutorPool.submit(runnable);
        }
    }

    private static void lockDevice(Profile profile, Context context) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadProfileActivation();
        //final Handler __handler = new Handler(PPApplication.handlerThreadProfileActivation.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context.getApplicationContext(), profile, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadProfileActivation", "START run - from=ActivateProfileHelper.lockDevice");

            if (PPApplication.blockProfileEventActions)
                // not lock device after boot
                return;

            //Context appContext= appContextWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //SharedPreferences executedProfileSharedPreferences = executedProfileSharedPreferencesWeakRef.get();

            //if ((appContext != null) && (profile != null) /*&& (executedProfileSharedPreferences != null)*/) {

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_lockDevice");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                switch (profile._lockDevice) {
                    case 1:
                        if (PhoneProfilesService.getInstance() != null) {
                            //if (Permissions.checkLockDevice(appContext) && (PPApplication.lockDeviceActivity == null)) {
                            if (Permissions.checkLockDevice(appContext) && (!PPApplication.lockDeviceActivityDisplayed)) {
                                try {
                                    Intent intent = new Intent(appContext, LockDeviceActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    appContext.startActivity(intent);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                        break;
                    case 2:
                        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                                (RootUtils.isRooted(false))) {
                            synchronized (PPApplication.rootMutex) {
                                /*String command1 = "input keyevent 26";
                                Command command = new Command(0, false, command1);
                                try {
                                    roottools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                } catch (RootDeniedException e) {
                                    PPApplication.rootMutex.rootGranted = false;
                                    Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                }*/
                                String command1 = RootUtils.getJavaCommandFile(CmdGoToSleep.class, "power", appContext, 0);
                                if (command1 != null) {
                                    Command command = new Command(0, /*false,*/ command1);
                                    try {
                                        RootTools.getShell(true, Shell.ShellContext.NORMAL).add(command);
                                        RootUtils.commandWait(command, "ActivateProfileHelper.lockDevice");
                                    } catch (Exception e) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.lockDevice", Log.getStackTraceString(e));
                                        //CPPApplicationStatic.recordException(e);
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
                                        roottools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
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
                        intent.putExtra(PPApplication.EXTRA_BLOCK_PROFILE_EVENT_ACTION, PPApplication.blockProfileEventActions);
                        appContext.sendBroadcast(intent, PPApplication.PPP_EXTENDER_PERMISSION);
                        break;
                }
            } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createProfileActiationExecutorPool();
        PPApplication.profileActiationExecutorPool.submit(runnable);
    }

    private static void setScreenDarkMode(Context appContext, final int value, SharedPreferences executedProfileSharedPreferences) {
        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, executedProfileSharedPreferences, false, appContext).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (Build.VERSION.SDK_INT >= 29) {
                boolean G1OK = false;
                if (Permissions.hasPermission(appContext, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    try {
                        // Android Q (Tasker: https://www.reddit.com/r/tasker/comments/d2ngcl/trigger_android_10_dark_theme_with_brightness/)
                        if (value == 1)
                            Settings.Secure.putInt(appContext.getContentResolver(), "ui_night_mode", 2);
                        else
                            Settings.Secure.putInt(appContext.getContentResolver(), "ui_night_mode", 1);
                        G1OK = true;
                    }
                    catch (Exception e2) {
                        Log.e("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(e2));
                        //PPApplicationStatic.recordException(e2);
                    }
                }
                if (!G1OK) {
                    if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                            (RootUtils.isRooted(false))) {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "settings put secure ui_night_mode ";
                            if (value == 1)
                                command1 = command1 + "2";
                            else
                                command1 = command1 + "1";
                            Command command = new Command(0, /*false,*/ command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                RootUtils.commandWait(command, "ActivateProfileHelper.setScreenDarkMode");
                            } catch (Exception ee) {
                                // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                //Log.e("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(ee));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                }
                // switch car mode on and off is required !!!
                // but how when car mode is on by device? Opposite switch?
                UiModeManager uiModeManager = (UiModeManager) appContext.getSystemService(Context.UI_MODE_SERVICE);
                if (uiModeManager != null) {
                    if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_NORMAL) {
                        uiModeManager.enableCarMode(0);
                        GlobalUtils.sleep(200);
                        //uiModeManager.disableCarMode(0);
                        uiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
                    }
                    else
                    if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
                        //uiModeManager.disableCarMode(0);
                        uiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
                        GlobalUtils.sleep(200);
                        uiModeManager.enableCarMode(0);
                    }
                }
            }

            /*if (Build.VERSION.SDK_INT > 26) {
                if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                    try {
                        // Not working in Samsung S8 :-(
                        // this not change gui to dark, this is blue filter (???)
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
                        Log.e("ActivateProfileHelper.setScreenDarkMode", Log.getStackTraceString(e2));
                    }
                }
            }
            else {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
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
                }
            }*/
        }
    }

    private static void setDefaultSimCard(Context context, int subscriptionType, int simCard)
    {
//        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionType="+subscriptionType);
//        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "simCard="+simCard);

        Context appContext = context.getApplicationContext();

        switch (subscriptionType) {
            case SUBSCRIPTRION_VOICE:
                switch (simCard) {
                    case 1: // ask for SIM - currently not supported
                        simCard = -1;
                        break;
                    case 2: // SIM 1
                        simCard = 1;
                        break;
                    case 3: // SIM 2
                        simCard = 2;
                        break;
                }
//                PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "new simCard="+simCard);
                break;
            case SUBSCRIPTRION_SMS:
            case SUBSCRIPTRION_DATA:
                break;
        }

        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                RootUtils.isRooted(false)) {

            if (Permissions.checkPhone(context.getApplicationContext())) {
//                PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "ask for root enabled and is rooted");
                //if (Build.VERSION.SDK_INT >= 26) {
//                    if (simCard != -1) {
                        GlobalUtils.HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        if (simCard == 1) {
                            boolean sim1Exists = hasSIMCardData.hasSIM1;
                            simExists = simExists && sim1Exists;
                        }
                        else
                        if (simCard == 2) {
                            boolean sim2Exists = hasSIMCardData.hasSIM2;
                            simExists = simExists && sim2Exists;
                        }

                        if ((simCard == -1) || simExists) {
                            int defaultSubscriptionId = -1;
                            // Get the value of the "TRANSACTION_setDefaultSimCard" field.
                            int transactionCode = -1;
                            switch (subscriptionType) {
                                case SUBSCRIPTRION_VOICE:
                                    defaultSubscriptionId = SubscriptionManager.getDefaultVoiceSubscriptionId();
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "getTransactionCode for setDefaultVoiceSubId");
                                    transactionCode = PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId;
                                    break;
                                case SUBSCRIPTRION_SMS:
                                    defaultSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "getTransactionCode for setDefaultSmsSubId");
                                    transactionCode = PPApplication.rootMutex.transactionCode_setDefaultSmsSubId;
                                    break;
                                case SUBSCRIPTRION_DATA:
                                    defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "getTransactionCode for setDefaultDataSubId");
                                    transactionCode = PPApplication.rootMutex.transactionCode_setDefaultDataSubId;
                                    break;
                            }
//                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "defaultSubscriptionId=" + defaultSubscriptionId);
//                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "transactionCode=" + transactionCode);

                            if (transactionCode != -1) {

                                SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                                //SubscriptionManager.from(appContext);
                                if (mSubscriptionManager != null) {
//                                    PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "mSubscriptionManager != null");
                                    List<SubscriptionInfo> subscriptionList = null;
                                    try {
                                        // Loop through the subscription list i.e. SIM list.
                                        subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionList=" + subscriptionList);
                                    } catch (SecurityException e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                    if (subscriptionList != null) {
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionList.size()=" + subscriptionList.size());
                                        for (int i = 0; i < subscriptionList.size(); i++) {
                                            // Get the active subscription ID for a given SIM card.
                                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionInfo=" + subscriptionInfo);
                                            if (subscriptionInfo != null) {
                                                int slotIndex = subscriptionInfo.getSimSlotIndex();
                                                if ((simCard == -1) || (simCard == (slotIndex+1))) {
                                                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                                                    if ((simCard == -1) || (subscriptionId != defaultSubscriptionId)) {
                                                        // do not call subscription change, when is aleredy set, this cause FC

//                                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionId=" + subscriptionId);
                                                        synchronized (PPApplication.rootMutex) {
                                                            String command1;
                                                            if (simCard == -1)
                                                                command1 = RootUtils.getServiceCommand("isub", transactionCode, 0);
                                                            else
                                                                command1 = RootUtils.getServiceCommand("isub", transactionCode, subscriptionId);
//                                                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "command1=" + command1);

                                                            String command2 = "";
                                                            switch (subscriptionType) {
                                                                case SUBSCRIPTRION_VOICE:
                                                                    if (simCard == -1)
                                                                        command2 = "settings put global " + Settings.Global.MULTI_SIM_VOICE_CALL_SUBSCRIPTION + " 0";
                                                                    else
                                                                        command2 = "settings put global " + Settings.Global.MULTI_SIM_VOICE_CALL_SUBSCRIPTION + " " + subscriptionId;
                                                                    break;
                                                                case SUBSCRIPTRION_SMS:
                                                                    command2 = "settings put global " + Settings.Global.MULTI_SIM_SMS_SUBSCRIPTION + " " + subscriptionId;
                                                                    break;
                                                                case SUBSCRIPTRION_DATA:
                                                                    command2 = "settings put global " + Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION + " " + subscriptionId;
                                                                    break;
                                                            }
//                                                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "command2=" + command2);

                                                            if ((command1 != null)/* && (!command2.isEmpty())*/) {
                                                                Command command = new Command(0, /*false,*/ command2, command1);
                                                                try {
                                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setDefaultSimCard");
                                                                } catch (Exception e) {
                                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                                                    //Log.e("ActivateProfileHelper.setDefaultSimCard", Log.getStackTraceString(e));
                                                                    PPApplicationStatic.recordException(e);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
//                                            else
//                                                PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionInfo == null");

                                            if (simCard == -1)
                                                break;
                                        }
                                    }
//                                    else
//                                        PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "subscriptionList == null");
                                }
//                                else
//                                    PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "mSubscriptionManager == null");
                            }
//                            else
//                                PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "(transactionCode == -1) || (simCard == -1)");
                        }
//                    }
/*                    else {
                        synchronized (PPApplication.rootMutex) {
                            String command1 = "";
                            switch (subscriptionType) {
                                case SUBSCRIPTRION_VOICE:
                                    command1 = "settings put global " + Settings.Global.MULTI_SIM_VOICE_CALL_SUBSCRIPTION + " 0";
                                    break;
                                case SUBSCRIPTRION_SMS:
                                    command1 = "settings put global " + Settings.Global.MULTI_SIM_SMS_SUBSCRIPTION + " 0";
                                    break;
                                case SUBSCRIPTRION_DATA:
                                    command1 = "settings put global " + Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION + " 0";
                                    break;
                            }
                            PPApplicationStatic.logE("[DEFAULT_SIM] ActivateProfileHelper.setDefaultSimCard", "command1=" + command1);

                            if (!command1.isEmpty()) {
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command, "ActivateProfileHelper.setDefaultSimCard");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("ActivateProfileHelper.setDefaultSimCard", Log.getStackTraceString(e));
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
*/
                //}
            }
        }
    }

    private static void setSIMOnOff(Context context, boolean enable, int simCard)
    {
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "simCard="+simCard);
//        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "enable="+enable);

        Context appContext = context.getApplicationContext();

        GlobalUtils.HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
        boolean simExists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
        if (simCard == 1) {
            boolean sim1Exists = hasSIMCardData.hasSIM1;
            simExists = simExists && sim1Exists;
        }
        else
        if (simCard == 2) {
            boolean sim2Exists = hasSIMCardData.hasSIM2;
            simExists = simExists && sim2Exists;
        }

        if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) &&
                RootUtils.isRooted(false) &&
                simExists)
        {
            if (Permissions.checkPhone(context.getApplicationContext())) {
//                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "ask for root enabled and is rooted");
                // Get the value of the "TRANSACTION_setDataEnabled" field.
                int transactionCode = PPApplication.rootMutex.transactionCode_setSubscriptionEnabled;
                    //transactionCode = PPApplication.getTransactionCode(String.valueOf(serviceManager), "setSimPowerStateForSlot");

                int state = enable ? 1 : 0;

                if (transactionCode != -1) {
//                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "transactionCode=" + transactionCode);

                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(appContext);
                    if (mSubscriptionManager != null) {
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "mSubscriptionManager != null");
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionList=" + subscriptionList);
                        } catch (SecurityException e) {
                            PPApplicationStatic.recordException(e);
                        }
                        if (subscriptionList != null) {
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionList.size()=" + subscriptionList.size());
                            for (int i = 0; i < subscriptionList.size(); i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
//                                PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionInfo=" + subscriptionInfo);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    if (simCard == (slotIndex+1)) {
                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
//                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionId=" + subscriptionId);
                                        synchronized (PPApplication.rootMutex) {
                                            String command1 = RootUtils.getServiceCommand("isub", transactionCode, subscriptionId, state);
                                            //String command1 = PPApplication.getServiceCommand("phone", transactionCode, slotIndex, state);
//                                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "command1=" + command1);
                                            if (command1 != null) {
                                                Command command = new Command(0, /*false,*/ command1)/* {
                                                    @Override
                                                    public void commandOutput(int id, String line) {
                                                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "command output -> line=" + line);
                                                        super.commandOutput(id, line);
                                                    }
                                                }*/;
                                                try {
                                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                                    RootUtils.commandWait(command, "ActivateProfileHelper.setSIMOnOff");
                                                } catch (Exception e) {
                                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
//                                                    Log.e("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", Log.getStackTraceString(e));
                                                    //PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
//                                else
//                                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionInfo == null");
                            }
                        }
//                        else
//                            PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "subscriptionList == null");
                    }
//                    else
//                        PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "mSubscriptionManager == null");
                }
//                else
//                    PPApplicationStatic.logE("[DUAL_SIM] ActivateProfileHelper.setSIMOnOff", "transactionCode == -1");
            }
        }
    }

    private static void setCameraFlash(Context appContext, Profile profile, SharedPreferences executedProfileSharedPreferences) {
        if (profile._cameraFlash != 0) {
            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CAMERA_FLASH, null, executedProfileSharedPreferences, true, appContext).allowed
                    == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (Permissions.checkProfileCameraFlash(appContext, profile, null)) {
                    switch (profile._cameraFlash) {
                        case 1:
                            try {
                                // keep this: it is required to use handlerThreadBroadcast for cal listener
                                PPApplicationStatic.startHandlerThreadBroadcast();
                                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                                __handler.post(() -> {
//                                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileHelper.execute");

                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    NoobCameraManager noobCameraManager = NoobCameraManager.getInstance();
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_cameraFlash");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }
                                        if (noobCameraManager != null) {
                                            noobCameraManager.init(appContext);
                                            noobCameraManager.turnOnFlash();
                                        }
                                    } catch (CameraAccessException ce) {
                                        //if (ce.getReason() == CameraAccessException.CAMERA_IN_USE) {}
                                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH,
                                                null, profile._name, "");
                                    } catch (Exception e) {
//                                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                        PPApplicationStatic.recordException(e);
                                    } finally {
                                        if (noobCameraManager != null)
                                            noobCameraManager.release();

                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                            break;
                        case 2:
                            try {
                                // keep this: it is required to use handlerThreadBroadcast for cal listener
                                PPApplicationStatic.startHandlerThreadBroadcast();
                                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                                __handler.post(() -> {
//                                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileHelper.execute");

                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    NoobCameraManager noobCameraManager = NoobCameraManager.getInstance();
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivateProfileHelper_cameraFlash");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }
                                        if (noobCameraManager != null) {
                                            noobCameraManager.init(appContext);
                                            noobCameraManager.turnOffFlash();
                                        }
                                    } catch (CameraAccessException ce) {
                                        //if (ce.getReason() == CameraAccessException.CAMERA_IN_USE) {}
                                        PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_PROFILE_ERROR_CAMERA_FLASH,
                                                null, profile._name, "");
                                    } catch (Exception e) {
//                                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                        PPApplicationStatic.recordException(e);
                                    } finally {
                                        if (noobCameraManager != null)
                                            noobCameraManager.release();

                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                            break;
                    }
                }
            }
        }
    }

    static void setVPN(Context context, Profile profile, SharedPreferences executedProfileSharedPreferences) {
        if (!profile._deviceVPN.isEmpty()) {
            String[] splits = profile._deviceVPN.split("\\|");
            try {
                int vpnApplication = Integer.parseInt(splits[0]);
                if (vpnApplication > 0) {
                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_VPN, null, executedProfileSharedPreferences, true, context).allowed
                            == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        boolean enableVPN = splits[1].equals("0");

                        boolean setVPN = true;

                        boolean doNotSet = false;
                        if (splits.length > 4)
                            doNotSet = splits[4].equals("1");
                        if (doNotSet) {
                            ConnectivityManager connManager = null;
                            try {
                                connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            } catch (Exception e) {
                                // java.lang.NullPointerException: missing IConnectivityManager
                                // Dual SIM?? Bug in Android ???
                                //PPApplicationStatic.recordException(e);
                            }
                            if (connManager != null) {
                                Network activeNetwork = connManager.getActiveNetwork();
                                NetworkCapabilities caps = connManager.getNetworkCapabilities(activeNetwork);
                                if (caps != null) {
                                    boolean vpnInUse = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                                    setVPN = enableVPN != vpnInUse;
                                }
                            }
                        }
                        if (setVPN) {
                            String profileName = "";
                            if (splits.length > 2)
                                profileName = splits[2];
                            String tunnelName = "";
                            if (splits.length > 3)
                                tunnelName = splits[3];

                            Intent intent = null;
                            switch (vpnApplication) {
                                case 1:
                                    intent = new Intent();
                                    intent.setComponent(new ComponentName("net.openvpn.openvpn", "net.openvpn.unified.MainActivity"));
                                    if (enableVPN) {
                                        intent.setAction("net.openvpn.openvpn.CONNECT");
                                        String keyValue = "AS " + profileName;
                                        intent.putExtra("net.openvpn.openvpn.AUTOSTART_PROFILE_NAME", keyValue);
                                        intent.putExtra("net.openvpn.openvpn.AUTOCONNECT", "true");
                                    } else {
                                        intent.setAction("net.openvpn.openvpn.DISCONNECT");
                                        intent.putExtra("net.openvpn.openvpn.STOP", "true");
                                    }
                                    break;
                                case 2:
                                    intent = new Intent();
                                    intent.setComponent(new ComponentName("net.openvpn.openvpn", "net.openvpn.unified.MainActivity"));
                                    if (enableVPN) {
                                        intent.setAction("net.openvpn.openvpn.CONNECT");
                                        String keyValue = "PC " + profileName;
                                        intent.putExtra("net.openvpn.openvpn.AUTOSTART_PROFILE_NAME", keyValue);
                                        intent.putExtra("net.openvpn.openvpn.AUTOCONNECT", "true");
                                    } else {
                                        intent.setAction("net.openvpn.openvpn.DISCONNECT");
                                        intent.putExtra("net.openvpn.openvpn.STOP", "true");
                                    }
                                    break;
                                case 3:
                                    intent = new Intent();
                                    if (enableVPN) {
                                        intent.setComponent(new ComponentName("de.blinkt.openvpn", "de.blinkt.openvpn.api.ConnectVPN"));
                                    } else {
                                        intent.setComponent(new ComponentName("de.blinkt.openvpn", "de.blinkt.openvpn.api.DisconnectVPN"));
                                    }
                                    intent.setAction("android.intent.action.MAIN");
                                    intent.putExtra("de.blinkt.openvpn.api.profileName", profileName);
                                    break;
                                case 4:
                                    if (Permissions.checkProfileWireGuard(context, profile, null)) {
                                        intent = new Intent(enableVPN ? "com.wireguard.android.action.SET_TUNNEL_UP" : "com.wireguard.android.action.SET_TUNNEL_DOWN");
                                        intent.setPackage("com.wireguard.android");
                                        intent.putExtra("tunnel", tunnelName);
                                    }
                                    break;
                            }

                            if (intent != null) {
                                if (vpnApplication < 4) {
                                    //noinspection TryWithIdenticalCatches
                                    try {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    } catch (ActivityNotFoundException ee) {
                                        PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN,
                                                null, profileName, "");
                                    } catch (SecurityException e) {
                                        PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ERROR_SET_VPN,
                                                null, profileName, "");
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                } else {
                                    try {
                                        context.sendBroadcast(intent);
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                        }
                    }

                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
    }

    static void generateNotifiction(Context appContext, Profile profile)  {
        if (profile.getGenerateNotificationGenerate()) {
            PPApplicationStatic.createGeneratedByProfileNotificationChannel(appContext);

            NotificationCompat.Builder mBuilder;

            Intent launcherIntent;
            if (Build.VERSION.SDK_INT < 31) {
                launcherIntent = new Intent(PPAppNotification.ACTION_START_LAUNCHER_FROM_NOTIFICATION);
            } else {
                launcherIntent = GlobalGUIRoutines.getIntentForStartupSource(appContext, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                // clear all opened activities
                launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                // setup startupSource
                launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
            }

            String nTitle = profile.getGenerateNotificationTitle();
            String nText = profile.getGenerateNotificationBody();
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = appContext.getString(R.string.ppp_app_name);
//                nText = profile.getGenerateNotificationTitle() + ": " +
//                        profile.getGenerateNotificationBody();
//            }
            nTitle = nTitle + " (" + profile._name + ")";
            mBuilder = new NotificationCompat.Builder(appContext, PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL)
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                    .setAutoCancel(true) // clear notification after click
                    .setOnlyAlertOnce(true);

            switch (profile.getGenerateNotificationIconType()) {
                case 0:
                    mBuilder.setSmallIcon(R.drawable.ic_information_notify);
                    mBuilder.setColor(ContextCompat.getColor(appContext, R.color.information_color));
                    break;
                case 1:
                    mBuilder.setSmallIcon(R.drawable.ic_exclamation_notify);
                    mBuilder.setColor(ContextCompat.getColor(appContext, R.color.error_color));
                    break;
                case 3:
                    mBuilder.setSmallIcon(R.drawable.ic_ppp_notification);
                    mBuilder.setColor(ContextCompat.getColor(appContext, R.color.notification_color));
                    break;
                default:
                    // not supported colorful ststus bar icon
                    boolean isIconResourceID = profile.getIsIconResourceID();
                    Bitmap  iconBitmap = profile._iconBitmap;
                    String iconIdentifier = profile.getIconIdentifier();

                    int decoratorColor = ContextCompat.getColor(appContext, R.color.notification_color);

                    if (isIconResourceID) {
                        int iconSmallResource;
                        if (iconBitmap != null) {
                            /*
                            if (notificationProfileListStatusBarStyle.equals("0")) {
                                // colorful icon

                                notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
                            } else*/ {
                                // native icon

                                iconSmallResource = R.drawable.ic_profile_default_notify;
                                try {
                                    if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                        Object obj = Profile.profileIconNotifyId.get(iconIdentifier);
                                        if (obj != null)
                                            iconSmallResource = (int) obj;
                                    }
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
//                                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                                }
                                mBuilder.setSmallIcon(iconSmallResource);
                            }
                        } else {
                            /*
                            if (notificationProfileListStatusBarStyle.equals("0")) {
                                // colorful icon

                                iconSmallResource = R.drawable.ic_profile_default_notify_color;
                                try {
                                    if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                        Object idx = Profile.profileIconNotifyColorId.get(iconIdentifier);
                                        if (idx != null)
                                            iconSmallResource = (int) idx;
                                    }
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
//                                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                                }
                            } else*/ {
                                // native icon

                                iconSmallResource = R.drawable.ic_profile_default_notify;
                                try {
                                    if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                        Object idx = Profile.profileIconNotifyId.get(iconIdentifier);
                                        if (idx != null)
                                            iconSmallResource = (int) idx;
                                    }
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
//                                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PPAppNotification._addProfileIconToNotification", Log.getStackTraceString(e));
                                }
                            }
                            mBuilder.setSmallIcon(iconSmallResource);
                        }

                        if (profile.getUseCustomColorForIcon())
                            decoratorColor = profile.getIconCustomColor();
                        else {
                            if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                                decoratorColor = ProfileStatic.getIconDefaultColor(iconIdentifier);
                            }
                        }
                    } else {
                        if (iconBitmap != null) {
                            /*if (notificationProfileListStatusBarStyle.equals("2")) {
                                Bitmap _iconBitmap = BitmapManipulator.monochromeBitmap(iconBitmap, 0xFF);
                                notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(_iconBitmap));
                                //notificationBuilder.setSmallIcon(R.drawable.ic_profile_default_notify);
                            } else*/ {
                                mBuilder.setSmallIcon(IconCompat.createWithBitmap(iconBitmap));
                            }
                        } else {
                            int iconSmallResource;
                            /*if (notificationProfileListStatusBarStyle.equals("0"))
                                iconSmallResource = R.drawable.ic_profile_default;
                            else*/
                                iconSmallResource = R.drawable.ic_profile_default_notify;
                            mBuilder.setSmallIcon(iconSmallResource);
                        }

                        if ((iconIdentifier != null) && (!iconIdentifier.isEmpty())) {
                            if (iconBitmap != null) {
                                // do not use increaseNotificationDecorationBrightness(),
                                // because icon will not be visible in AOD
                                //int color = profile.increaseNotificationDecorationBrightness(appContext);
                                //if (color != 0)
                                //    decoratorColor = color;
                                //else {
                                try {
                                    Palette palette = Palette.from(iconBitmap).generate();
                                    decoratorColor = palette.getDominantColor(ContextCompat.getColor(appContext, R.color.notification_color));
                                } catch (Exception ignored) {}
                            }
                        }
                    }

                    mBuilder.setColor(decoratorColor);
                    break;
            }

            PendingIntent pIntent;
            if (Build.VERSION.SDK_INT < 31)
                pIntent = PendingIntent.getBroadcast(appContext, 0, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            else
                pIntent = PendingIntent.getActivity(appContext, 0, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(pIntent);
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            //}

            mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP);

            Notification notification = mBuilder.build();
            //notification.vibrate = null;
            //notification.defaults &= ~DEFAULT_VIBRATE;

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
            try {
                mNotificationManager.notify(
                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int)profile._id,
                        notification);
            } catch (SecurityException en) {
                Log.e("ActivateProfileHelper.generateNotifiction", Log.getStackTraceString(en));
            } catch (Exception e) {
                //Log.e("ActivateProfileHelper.execute", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        } else {
            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
            try {
                mNotificationManager.cancel(
                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id);
            } catch (SecurityException en) {
                Log.e("ActivateProfileHelper.generateNotifiction", Log.getStackTraceString(en));
            } catch (Exception e) {
                //Log.e("ActivateProfileHelper.execute", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
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
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int systemZenMode = getSystemZenMode(context/*, -1*/);
            if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/)) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putInt(PREF_RINGER_VOLUME, volume);
                editor.apply();
                ApplicationPreferences.prefRingerVolume = volume;
            }
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
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int systemZenMode = getSystemZenMode(context/*, -1*/);
            if (isAudibleSystemRingerMode(audioManager, systemZenMode/*, appContext*/)) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putInt(PREF_NOTIFICATION_VOLUME, volume);
                editor.apply();
                ApplicationPreferences.prefNotificationVolume = volume;
            }
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
        getRingerMode(context);

        synchronized (PPApplication.profileActivationMutex) {

            int savedMode = ApplicationPreferences.prefRingerMode;

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_RINGER_MODE, mode);
            editor.apply();
            ApplicationPreferences.prefRingerMode = mode;

            if (savedMode != mode) {
                PPExecutors.handleEvents(context, EventsHandler.SENSOR_TYPE_SOUND_PROFILE, "SENSOR_TYPE_SOUND_PROFILE", 5);
                /*
                Data workData = new Data.Builder()
                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_SOUND_PROFILE)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG)
                                .setInputData(workData)
                                .setInitialDelay(5, TimeUnit.SECONDS)
                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                .build();
                try {
                    if (PPApplicationStatic.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_SCANNER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}
//
//                            PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                            //workManager.enqueue(worker);
                            workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                */
            }
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
        getZenMode(context);

        synchronized (PPApplication.profileActivationMutex) {
            int savedMode = ApplicationPreferences.prefZenMode;

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_ZEN_MODE, mode);
            editor.apply();
            ApplicationPreferences.prefZenMode = mode;

            if (savedMode != mode) {
                PPExecutors.handleEvents(context, EventsHandler.SENSOR_TYPE_SOUND_PROFILE, "SENSOR_TYPE_SOUND_PROFILE", 5);
                /*
                Data workData = new Data.Builder()
                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_SOUND_PROFILE)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG)
                                .setInputData(workData)
                                .setInitialDelay(5, TimeUnit.SECONDS)
                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                .build();
                try {
                    if (PPApplicationStatic.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_SCANNER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}
//
//                            PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                            //workManager.enqueue(worker);
                            workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                */
            }
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

    static void getActivatedProfileScreenTimeoutWhenScreenOff(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.prefActivatedProfileScreenTimeoutWhenScreenOff = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT_WHEN_SCREEN_OFF, 0);
            //return prefActivatedProfileScreenTimeout;
        }
    }
    static void setActivatedProfileScreenTimeoutWhenScreenOff(Context context, int timeout)
    {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_ACTIVATED_PROFILE_SCREEN_TIMEOUT_WHEN_SCREEN_OFF, timeout);
            editor.apply();
            ApplicationPreferences.prefActivatedProfileScreenTimeoutWhenScreenOff = timeout;
        }
    }

    static void getKeepScreenOnPermanent(Context context)
    {
        synchronized (PPApplication.profileActivationMutex) {
            ApplicationPreferences.keepScreenOnPermanent = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_KEEP_SCREEN_ON_PERMANENT, false);
            //return prefLockScreenDisabled;
        }
    }
    static void setKeepScreenOnPermanent(Context context, boolean keepOnPermanent)
    {
        synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_KEEP_SCREEN_ON_PERMANENT, keepOnPermanent);
            editor.apply();
            ApplicationPreferences.keepScreenOnPermanent = keepOnPermanent;
        }
    }

    static int isPPPPutSettingsInstalled(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_PPPPS, 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return PPApplicationStatic.getVersionCode(pInfo);
            }
            else {
                return 0;
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.isExtenderInstalled", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return 0;
        }
    }

    static String getPPPPutSettingsVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(PPApplication.PACKAGE_NAME_PPPPS, 0);
            boolean installed = appInfo.enabled;
            if (installed) {
                PackageInfo pInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                return pInfo.versionName;
            }
            else {
                return "";
            }
        }
        catch (Exception e) {
            // extender is not installed = package not found
            //Log.e("PPExtenderBroadcastReceiver.getExtenderVersionName", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return "";
        }
    }

    static void putSettingsParameter(Context context,
                                     @SuppressWarnings("SameParameterValue") String settingsType,
                                     String parameterName,
                                     String parameterValue) {
        if ((Build.VERSION.SDK_INT < 29) || (Settings.canDrawOverlays(context))) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("sk.henrichg.pppputsettings", "sk.henrichg.pppputsettings.PutSettingsParameterActivity"));
                intent.putExtra("extra_put_setting_parameter_type", settingsType);
                intent.putExtra("extra_put_setting_parameter_name", parameterName);
                intent.putExtra("extra_put_setting_parameter_value", parameterValue);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("ActivateProfileHelper.putSettingsParameter", Log.getStackTraceString(e));
            }
            // WARNING: do not remove this sleep !!!
            // Is required to set time space between two calls of this method.
            GlobalUtils.sleep(500);
        }
    }
/*
    static void showError(Context context, String profileName, int parameterType) {
        if ((context == null) || (profileName == null))
            return;

        Context appContext = context.getApplicationContext();

        String title = appContext.getString(R.string.profile_activation_activation_error_title) + " " + profileName;
        String text;
        int notificationId;
        String notificationTag;
        switch (parameterType) {
            case Profile.PARAMETER_TYPE_WIFI:
                text = appContext.getString(R.string.profile_activation_activation_error_change_wifi);
                notificationId = PPApplication.PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_ID;
                notificationTag = PPApplication.PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_TAG;
                break;
            case Profile.PARAMETER_TYPE_WIFIAP:
                text = appContext.getString(R.string.profile_activation_activation_error_change_wifi_ap);
                notificationId = PPApplication.PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_ID;
                notificationTag = PPApplication.PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_TAG;
                break;
            case Profile.PARAMETER_CLOSE_ALL_APPLICATION:
                text = appContext.getString(R.string.profile_activation_activation_error_close_all_applications);
                notificationId = PPApplication.PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_ID;
                notificationTag = PPApplication.PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_TAG;
                break;
            default:
                text = appContext.getString(R.string.profile_activation_activation_error);
                notificationId = PPApplication.PROFILE_ACTIVATION_ERROR_NOTIFICATION_ID;
                notificationTag = PPApplication.PROFILE_ACTIVATION_ERROR_NOTIFICATION_TAG;
                break;
        }

        PPApplicationStatic.createExclamationNotificationChannel(appContext);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(appContext, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.notification_color))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        //PendingIntent pi = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        mBuilder.setGroup(PPApplication.PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP);

        Notification notification = mBuilder.build();

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(notificationTag, notificationId, notification);
        } catch (SecurityException en) {
            Log.e("ActivateProfileHelper.showError", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("ActivateProfileHelper.showError", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }

    }
*/

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<Profile> profileWeakRef;
        final WeakReference<SharedPreferences> executedProfileSharedPreferencesWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       Profile profile,
                                       SharedPreferences executedProfileSharedPreferences) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.profileWeakRef = new WeakReference<>(profile);
            this.executedProfileSharedPreferencesWeakRef = new WeakReference<>(executedProfileSharedPreferences);
        }

    }*/

}
