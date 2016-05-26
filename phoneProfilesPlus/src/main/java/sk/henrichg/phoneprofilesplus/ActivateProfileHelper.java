package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.NotificationCompat;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

public class ActivateProfileHelper {

    private DataWrapper dataWrapper;

    private Context context;
    private NotificationManager notificationManager;
    private Handler brightnessHandler;

    private int networkType = -1;

    public static boolean lockRefresh = false;

    public static final String ADAPTIVE_BRIGHTNESS_SETTING_NAME = "screen_auto_brightness_adj";

    // Setting.Global "zen_mode"
    public static final int ZENMODE_ALL = 0;
    public static final int ZENMODE_PRIORITY = 1;
    public static final int ZENMODE_NONE = 2;
    public static final int ZENMODE_ALARMS = 3;

    public ActivateProfileHelper()
    {

    }

    public void initialize(DataWrapper dataWrapper, Activity a, Context c)
    {
        this.dataWrapper = dataWrapper;

        initializeNoNotificationManager(c);
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void initializeNoNotificationManager(Context c)
    {
        context = c;
    }

    public void deinitialize()
    {
        dataWrapper = null;
        context = null;
        notificationManager = null;
    }

    public void setBrightnessHandler(Handler handler)
    {
        brightnessHandler = handler;
    }

    @SuppressWarnings("deprecation")
    private void doExecuteForRadios(Profile profile, boolean onlyCheckForPPHelper)
    {

        if (!onlyCheckForPPHelper)
        {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        // nahodenie network type
        if (!onlyCheckForPPHelper) {
            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, context) == GlobalData.PREFERENCE_ALLOWED) {
                if (profile._deviceNetworkType >= 100) {
                    setPreferredNetworkType(context, profile._deviceNetworkType - 100);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }

        // nahodenie mobilnych dat
        if (!onlyCheckForPPHelper) {
            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == GlobalData.PREFERENCE_ALLOWED) {
                boolean _isMobileData = isMobileData(context);
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
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }

        // nahodenie WiFi AP
        boolean canChangeWifi = true;
        if (!onlyCheckForPPHelper) {
            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, context) == GlobalData.PREFERENCE_ALLOWED) {
                WifiApManager wifiApManager = null;
                try {
                    wifiApManager = new WifiApManager(context);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
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
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }

        if (canChangeWifi) {
            // nahodenie WiFi
            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) == GlobalData.PREFERENCE_ALLOWED) {
                boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                if (!isWifiAPEnabled) { // only when wifi AP is not enabled, change wifi
                    GlobalData.logE("$$$ WifiAP", "ActivateProfileHelper.doExecuteForRadios-isWifiAPEnabled=" + isWifiAPEnabled);
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    boolean setWifiState = false;
                    switch (profile._deviceWiFi) {
                        case 1:
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
                            isWifiEnabled = !isWifiEnabled;
                            setWifiState = true;
                            break;
                    }
                    if (setWifiState) {
                        if (!onlyCheckForPPHelper) {
                            try {
                                wifiManager.setWifiEnabled(isWifiEnabled);
                            } catch (Exception e) {
                                wifiManager.setWifiEnabled(isWifiEnabled);
                            }
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                System.out.println(e);
                            }
                        }
                        if (isWifiEnabled)
                            // when wifi is enabled from profile, no disable wifi after scan
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                    }
                }
            }
        }

        // nahodenie bluetooth
        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) == GlobalData.PREFERENCE_ALLOWED)
        {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean isBluetoothEnabled = bluetoothAdapter.isEnabled();
            boolean setBluetoothState = false;
            switch (profile._deviceBluetooth) {
                case 1 :
                    if (!isBluetoothEnabled)
                    {
                        isBluetoothEnabled = true;
                        setBluetoothState = true;
                    }
                    break;
                case 2 :
                    if (isBluetoothEnabled)
                    {
                        isBluetoothEnabled = false;
                        setBluetoothState = true;
                    }
                    break;
                case 3 :
                    isBluetoothEnabled = ! isBluetoothEnabled;
                    setBluetoothState = true;
                    break;
            }
            if (setBluetoothState)
            {
                if (!onlyCheckForPPHelper)
                {
                    if (isBluetoothEnabled)
                        bluetoothAdapter.enable();
                    else
                        bluetoothAdapter.disable();
                }
                if (isBluetoothEnabled)
                    // when bluetooth is enabled from profile, no disable bluetooth after scan
                    BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
            }
        }

        // nahodenie GPS
        if (!onlyCheckForPPHelper) {
            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_GPS, context) == GlobalData.PREFERENCE_ALLOWED)
            {
                //String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                boolean isEnabled;
                if (android.os.Build.VERSION.SDK_INT < 21)
                    isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                }
                GlobalData.logE("ActivateProfileHelper.doExecuteForRadios","isEnabled="+isEnabled);


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
                        } else if (isEnabled) {
                            setGPS(context, false);
                        }
                        break;
                }
            }
        }

        // nahodenie NFC - len v PPHelper

    }

    public void executeForRadios(Profile profile, boolean onlyCheckForPPHelper)
    {
        boolean _isAirplaneMode = false;
        boolean _setAirplaneMode = false;
        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == GlobalData.PREFERENCE_ALLOWED)
        {
            _isAirplaneMode = isAirplaneMode(context);
            switch (profile._deviceAirplaneMode) {
                case 1:
                    if (!_isAirplaneMode)
                    {
                        _isAirplaneMode = true;
                        _setAirplaneMode = true;
                    }
                    break;
                case 2:
                    if (_isAirplaneMode)
                    {
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

        if (_setAirplaneMode && _isAirplaneMode)
            // switch ON airplane mode, set it before executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

        doExecuteForRadios(profile, onlyCheckForPPHelper);

        if (_setAirplaneMode && !(_isAirplaneMode))
            // switch OFF airplane mode, set if after executeForRadios
            setAirplaneMode(context, _isAirplaneMode);

    }

    public void correctVolume0(AudioManager audioManager) {
        int ringerMode, zenMode;
        ringerMode = GlobalData.getRingerMode(context);
        zenMode = GlobalData.getZenMode(context);
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

    @SuppressLint("NewApi")
    public void setVolumes(Profile profile, AudioManager audioManager, int linkUnlink, boolean forProfileActivation)
    {
        if (profile.getVolumeRingtoneChange()) {
            if (forProfileActivation)
                GlobalData.setRingerVolume(context, profile.getVolumeRingtoneValue());
        }
        if (profile.getVolumeNotificationChange()) {
            if (forProfileActivation)
                GlobalData.setNotificationVolume(context, profile.getVolumeNotificationValue());
        }

        int ringerMode = GlobalData.getRingerMode(context);
        int zenMode = GlobalData.getZenMode(context);

        GlobalData.logE("ActivateProfileHelper.setVolumes", "ringerMode=" + ringerMode);
        GlobalData.logE("ActivateProfileHelper.setVolumes", "zenMode=" + zenMode);
        GlobalData.logE("ActivateProfileHelper.setVolumes", "linkUnlink=" + linkUnlink);
        GlobalData.logE("ActivateProfileHelper.setVolumes", "forProfileActivation=" + forProfileActivation);

        // for ringer mode VIBRATE or SILENT (and not for profile activation) or
        // for interruption types NONE and ONLY_ALARMS
        // not set system, ringer, npotification volume
        // (Android 6 - priority mode = ONLY_ALARMS)
        if (!(  (ringerMode == 3) ||
                ((ringerMode == 4) && (android.os.Build.VERSION.SDK_INT < 21)) ||
                ((ringerMode == 4) && (android.os.Build.VERSION.SDK_INT >= 23)) ||
                ((ringerMode == 5) && ((zenMode == 3) || (zenMode == 4) || (zenMode == 5) || (zenMode == 6)))
             )) {

            GlobalData.logE("ActivateProfileHelper.setVolumes", "ringer/notif/system change");

            if (forProfileActivation) {
                if (profile.getVolumeSystemChange()) {
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.getVolumeSystemValue(), 0);
                    //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_SYSTEM, profile.getVolumeSystemValue());
                    correctVolume0(audioManager);
                }
            }

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int callState = telephony.getCallState();

            //if (((!GlobalData.applicationUnlinkRingerNotificationVolumes) || (callState != TelephonyManager.CALL_STATE_RINGING))
            //        && ((linkUnlink == PhoneCallService.LINKMODE_NONE) || forProfileActivation)) {
            if (!GlobalData.applicationUnlinkRingerNotificationVolumes) {
                if (profile.getVolumeRingtoneChange() || profile.getVolumeSystemChange()) {
                    int volume = GlobalData.getRingerVolume(context);
                    GlobalData.logE("ActivateProfileHelper.setVolumes", "no doUnlink  ringer volume=" + volume);
                    if (volume != -999) {
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                        PhoneProfilesService.ringingVolume = volume;
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, volume);
                        correctVolume0(audioManager);
                    }
                }
                if (profile.getVolumeNotificationChange() || profile.getVolumeSystemChange()) {
                    int volume = GlobalData.getNotificationVolume(context);
                    GlobalData.logE("ActivateProfileHelper.setVolumes", "no doUnlink  notification volume=" + volume);
                    if (volume != -999) {
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                        //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, volume);
                        correctVolume0(audioManager);
                    }
                }
            }
            if (GlobalData.applicationUnlinkRingerNotificationVolumes) {

                boolean doUnlink = audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    int zm = Settings.Global.getInt(context.getContentResolver(), "zen_mode", ZENMODE_ALL);
                    doUnlink = (zm != ZENMODE_NONE) && (zm != ZENMODE_ALARMS);
                }
                GlobalData.logE("ActivateProfileHelper.setVolumes", "doUnlink=" + doUnlink);
                //Log.e("ActivateProfileHelper", "setVolumes doUnlink=" + doUnlink);
                //Log.e("ActivateProfileHelper", "setVolumes ringerMode=" + audioManager.getRingerMode());
                //Log.e("ActivateProfileHelper", "setVolumes zenMode=" + Settings.Global.getInt(context.getContentResolver(), "zen_mode", ZENMODE_NONE));

                if (doUnlink) {
                    //if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK) {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        // for separating ringing and notification
                        // in ringing state ringer volumes must by set
                        // and notification volumes must not by set
                        int volume = GlobalData.getRingerVolume(context);
                        GlobalData.logE("ActivateProfileHelper.setVolumes", "doUnlink-RINGING  ringer volume=" + volume);
                        if (volume != -999) {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                            PhoneProfilesService.ringingVolume = volume;
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                            correctVolume0(audioManager);
                        }
                    } else if (linkUnlink == PhoneCallService.LINKMODE_LINK) {
                        // for separating ringing and notification
                        // in not ringing state ringer and notification volume must by change
                        //Log.e("ActivateProfileHelper","setVolumes get audio mode="+audioManager.getMode());
                        int volume = GlobalData.getRingerVolume(context);
                        GlobalData.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING  ringer volume=" + volume);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set ring volume="+volume);
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
                            PhoneProfilesService.ringingVolume = volume;
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_RING, profile.getVolumeRingtoneValue());
                        }
                        volume = GlobalData.getNotificationVolume(context);
                        GlobalData.logE("ActivateProfileHelper.setVolumes", "doUnlink-NOT RINGING  notification volume=" + volume);
                        if (volume != -999) {
                            //Log.e("ActivateProfileHelper","setVolumes set notification volume="+volume);
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
                            //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_NOTIFICATION, profile.getVolumeNotificationValue());
                        }
                        correctVolume0(audioManager);
                    }
                }
                else {
                    if (callState == TelephonyManager.CALL_STATE_RINGING) {
                        int volume = GlobalData.getRingerVolume(context);
                        if (volume == -999)
                            volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        PhoneProfilesService.ringingVolume = volume;
                    }
                }
            }
        }

        if (forProfileActivation) {
            if (profile.getVolumeMediaChange()) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, profile.getVolumeMediaValue(), 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_MUSIC, profile.getVolumeMediaValue());
            }
            if (profile.getVolumeAlarmChange()) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, profile.getVolumeAlarmValue(), 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_ALARM, profile.getVolumeAlarmValue());
            }
            if (profile.getVolumeVoiceChange()) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, profile.getVolumeVoiceValue(), 0);
                //Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_VOICE, profile.getVolumeVoiceValue());
            }
        }

    }

    public static void setZenMode(Context context, int zenMode, AudioManager audioManager, int ringerMode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            int _zenMode = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);
            GlobalData.logE("ActivateProfileHelper.setZenMode", "_zenMode=" + _zenMode);
            int _ringerMode = audioManager.getRingerMode();
            GlobalData.logE("ActivateProfileHelper.setZenMode", "_ringerMode=" + _ringerMode);

            if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                audioManager.setRingerMode(ringerMode);
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        //System.out.println(e);
                    }
                }

                if ((zenMode != _zenMode) || (zenMode == ZENMODE_PRIORITY)) {
                    int interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                    switch (zenMode) {
                        case ZENMODE_ALL:
                            interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALL;
                            break;
                        case ZENMODE_PRIORITY:
                            interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_PRIORITY;
                            break;
                        case ZENMODE_NONE:
                            interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_NONE;
                            break;
                        case ZENMODE_ALARMS:
                            interruptionFilter = NotificationListenerService.INTERRUPTION_FILTER_ALARMS;
                            break;
                    }
                    PPNotificationListenerService.requestInterruptionFilter(context, interruptionFilter);
                    /* if (GlobalData.grantRoot(false) && (GlobalData.settingsBinaryExists()))
                    {
                        String command1 = "settings put global zen_mode " + mode;
                        //if (GlobalData.isSELinuxEnforcing())
                        //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                        Command command = new Command(0, false, command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                            //RootTools.closeAllShells();
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setZenMode", e.getMessage());
                        }
                    }*/
                }
            }
            else {
                switch (zenMode) {
                    case ZENMODE_PRIORITY:
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            //System.out.println(e);
                        }
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        break;
                    case ZENMODE_ALARMS:
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            //System.out.println(e);
                        }
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        break;
                    default:
                        audioManager.setRingerMode(ringerMode);
                }
            }
        }
        else
            audioManager.setRingerMode(ringerMode);
    }

    private void setVibrateWhenRinging(int value) {
        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_VIBRATE_WHEN_RINGING, context)
                == GlobalData.PREFERENCE_ALLOWED) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", value);
            else {
                try {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, value);
                } catch (Exception ee) {
                    String command1 = "settings put system " + Settings.System.VIBRATE_WHEN_RINGING + " " + value;
                    //if (GlobalData.isSELinuxEnforcing())
                    //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                    Command command = new Command(0, false, command1); //, command2);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setVibrateWhenRinging", "Error on run su: " + e.toString());
                    }
                }
            }
        }
    }

    private void setNotificationLed(int value) {
        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_NOTIFICATION_LED, context)
                == GlobalData.PREFERENCE_ALLOWED) {
            if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
                Settings.System.putInt(context.getContentResolver(), "notification_light_pulse", value);
            else {
                String command1 = "settings put system " + "notification_light_pulse" + " " + value;
                //if (GlobalData.isSELinuxEnforcing())
                //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                Command command = new Command(0, false, command1); //, command2);
                try {
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command);
                    //RootTools.closeAllShells();
                } catch (Exception e) {
                    Log.e("ActivateProfileHelper.setNotificationLed", "Error on run su: " + e.toString());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void setRingerMode(Profile profile, AudioManager audioManager, boolean firstCall, int linkUnlink, boolean forProfileActivation)
    {
        //GlobalData.logE("@@@ ActivateProfileHelper.setRingerMode", "andioM.ringerMode=" + audioManager.getRingerMode());

        int ringerMode;
        int zenMode;

        //if ((linkUnlink == PhoneCallService.LINKMODE_NONE) || forProfileActivaton) {
        if (forProfileActivation) {
            if (profile._volumeRingerMode != 0) {
                GlobalData.setRingerMode(context, profile._volumeRingerMode);
                if ((profile._volumeRingerMode == 5) && (profile._volumeZenMode != 0))
                    GlobalData.setZenMode(context, profile._volumeZenMode);
            }
        }

        if (firstCall)
            return;

        ringerMode = GlobalData.getRingerMode(context);
        zenMode = GlobalData.getZenMode(context);

        GlobalData.logE("ActivateProfileHelper.setRingerMode", "ringerMode=" + ringerMode);
        GlobalData.logE("ActivateProfileHelper.setRingerMode", "zenMode=" + zenMode);

        //if ((linkUnlink == PhoneCallService.LINKMODE_NONE) || forProfileActivation) {
        if (forProfileActivation) {

            GlobalData.logE("ActivateProfileHelper.setRingerMode", "ringer mode change");

            switch (ringerMode) {
                case 1:  // Ring
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setVibrateWhenRinging(0);
                    break;
                case 2:  // Ring & Vibrate
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setVibrateWhenRinging(1);
                    break;
                case 3:  // Vibrate
                    setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                    //audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); not needed, called from setZenMode
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setVibrateWhenRinging(1);
                    break;
                case 4:  // Silent
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        setZenMode(context, ZENMODE_ALARMS, audioManager, AudioManager.RINGER_MODE_SILENT);
                    else if (android.os.Build.VERSION.SDK_INT >= 21)
                        setZenMode(context, ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_NORMAL);
                    else {
                        setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_SILENT);
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            audioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    setVibrateWhenRinging(0);
                    break;
                case 5: // Zen mode
                    switch (zenMode) {
                        case 1:
                            setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(0);
                            break;
                        case 2:
                            setZenMode(context, ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_NORMAL);
                            setVibrateWhenRinging(0);
                            break;
                        case 3:
                            setZenMode(context, ZENMODE_NONE, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                        case 4:
                            setZenMode(context, ZENMODE_ALL, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(1);
                            break;
                        case 5:
                            setZenMode(context, ZENMODE_PRIORITY, audioManager, AudioManager.RINGER_MODE_VIBRATE);
                            setVibrateWhenRinging(1);
                            break;
                        case 6:
                            setZenMode(context, ZENMODE_ALARMS, audioManager, AudioManager.RINGER_MODE_SILENT);
                            break;
                    }
                    break;
            }
        }
    }

    public void executeForWallpaper(Profile profile) {
        if (profile._deviceWallpaperChange == 1)
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels << 1; // best wallpaper width is twice screen width
            Bitmap decodedSampleBitmap = BitmapManipulator.resampleBitmap(profile.getDeviceWallpaperIdentifier(), width, height, context);
            if (decodedSampleBitmap != null)
            {
                // set wallpaper
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                try {
                    wallpaperManager.setBitmap(decodedSampleBitmap);
                } catch (IOException e) {
                    Log.e("ActivateProfileHelper.executeForWallpaper", "Cannot set wallpaper. Image="+profile.getDeviceWallpaperIdentifier());
                }
            }
        }
    }

    public void executeForRunApplications(Profile profile) {
        if (profile._deviceRunApplicationChange == 1)
        {
            String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
            Intent intent;
            PackageManager packageManager = context.getPackageManager();

            for (int i = 0; i < splits.length; i++) {
                if (!ApplicationsCache.isShortcut(splits[i])) {
                    if (ApplicationsCache.getActivityName(splits[i]).isEmpty()) {
                        intent = packageManager.getLaunchIntentForPackage(splits[i]);
                        if (intent != null) {
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                context.startActivity(intent);
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                    else {
                        intent = new Intent(context, LaunchShortcutActivity.class);
                        intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, ApplicationsCache.getPackageName(splits[i]));
                        intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, ApplicationsCache.getActivityName(splits[i]));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(intent);
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
                else {
                    intent = new Intent(context, LaunchShortcutActivity.class);
                    intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, ApplicationsCache.getPackageName(splits[i]));
                    intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, ApplicationsCache.getActivityName(splits[i]));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }

    public void execute(Profile _profile, boolean merged, boolean _interactive)
    {
        // rozdelit zvonenie a notifikacie - zial je to oznacene ako @Hide :-(
        //Settings.System.putInt(context.getContentResolver(), Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 0);

        Profile profile = GlobalData.getMappedProfile(_profile, context);

        boolean interactive = _interactive;

        // nahodenie volume
        // run service for execute volumes
        GlobalData.logE("ActivateProfileHelper.execute", "ExecuteVolumeProfilePrefsService");
        Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
        volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        volumeServiceIntent.putExtra(GlobalData.EXTRA_MERGED_PROFILE, merged);
        volumeServiceIntent.putExtra(GlobalData.EXTRA_FOR_PROFILE_ACTIVATION, true);
        context.startService(volumeServiceIntent);
        /*AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        // nahodenie ringer modu - aby sa mohli nastavit hlasitosti
        setRingerMode(profile, audioManager);
        setVolumes(profile, audioManager);
        // nahodenie ringer modu - hlasitosti zmenia silent/vibrate
        setRingerMode(profile, audioManager);*/

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
        if (Permissions.checkProfileRingtones(context, profile)) {
            if (profile._soundRingtoneChange == 1) {
                if (!profile._soundRingtone.isEmpty()) {
                    //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, profile._soundRingtone);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(profile._soundRingtone));
                } else {
                    // selected is None tone
                    //Settings.System.putString(context.getContentResolver(), Settings.System.RINGTONE, null);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, null);
                }
            }
            if (profile._soundNotificationChange == 1) {
                if (!profile._soundNotification.isEmpty()) {
                    //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, profile._soundNotification);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(profile._soundNotification));
                } else {
                    // selected is None tone
                    //Settings.System.putString(context.getContentResolver(), Settings.System.NOTIFICATION_SOUND, null);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, null);
                }
            }
            if (profile._soundAlarmChange == 1) {
                if (!profile._soundAlarm.isEmpty()) {
                    //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, profile._soundAlarm);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, Uri.parse(profile._soundAlarm));
                } else {
                    // selected is None tone
                    //Settings.System.putString(context.getContentResolver(), Settings.System.ALARM_ALERT, null);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, null);
                }
            }
        }

        //// nahodenie radio preferences
        // run service for execute radios
        Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
        radioServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
        radioServiceIntent.putExtra(GlobalData.EXTRA_MERGED_PROFILE, merged);
        context.startService(radioServiceIntent);

        // nahodenie auto-sync
        boolean _isAutosync = ContentResolver.getMasterSyncAutomatically();
        boolean _setAutosync = false;
        switch (profile._deviceAutosync) {
            case 1:
                if (!_isAutosync)
                {
                    _isAutosync = true;
                    _setAutosync = true;
                }
                break;
            case 2:
                if (_isAutosync)
                {
                    _isAutosync = false;
                    _setAutosync = true;
                }
                break;
            case 3:
                _isAutosync = !_isAutosync;
                _setAutosync = true;
                break;
        }
        if (_setAutosync)
            ContentResolver.setMasterSyncAutomatically(_isAutosync);

        // screen timeout
        if (Permissions.checkProfileScreenTimeout(context, profile)) {
            switch (profile._deviceScreenTimeout) {
                case 1:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                    break;
                case 2:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                    break;
                case 3:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
                    break;
                case 4:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
                    break;
                case 5:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000);
                    break;
                case 6:
                    //2147483647 = Integer.MAX_VALUE
                    //18000000   = 5 hours
                    //86400000   = 24 hounrs
                    //43200000   = 12 hours
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 86400000); //18000000);
                    break;
                case 7:
                    screenTimeoutUnlock(context);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000);
                    break;
                case 8:
                    screenTimeoutUnlock(context);
                    //if (android.os.Build.VERSION.SDK_INT < 19)  // not working in Sony
                    //    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
                    //else
                        screenTimeoutLock(context);
                    break;
            }
        }

        // zapnutie/vypnutie lockscreenu
        //GlobalData.logE("$$$ ActivateProfileHelper.execute","keyguard");
        boolean setLockscreen = false;
        switch (profile._deviceKeyguard) {
            case 1:
                // enable lockscreen
                GlobalData.logE("$$$ ActivateProfileHelper.execute","keyguard=ON");
                GlobalData.setLockscreenDisabled(context, false);
                setLockscreen = true;
                break;
            case 2:
                // disable lockscreen
                GlobalData.logE("$$$ ActivateProfileHelper.execute","keyguard=OFF");
                GlobalData.setLockscreenDisabled(context, true);
                setLockscreen = true;
                break;
        }
        if (setLockscreen) {
            boolean isScreenOn;
            //if (android.os.Build.VERSION.SDK_INT >= 20)
            //{
            //	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //	isScreenOn = display.getState() != Display.STATE_OFF;
            //}
            //else
            //{
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            //}
            GlobalData.logE("$$$ ActivateProfileHelper.execute","isScreenOn="+isScreenOn);
            boolean keyguardShowing = false;
            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 16)
                keyguardShowing = kgMgr.isKeyguardLocked();
            else
                keyguardShowing = kgMgr.inKeyguardRestrictedInputMode();
            GlobalData.logE("$$$ ActivateProfileHelper.execute","keyguardShowing="+keyguardShowing);

            if (isScreenOn && !keyguardShowing) {
                Intent keyguardService = new Intent(context.getApplicationContext(), KeyguardService.class);
                context.startService(keyguardService);
            }
        }

        // nahodenie podsvietenia
        if (Permissions.checkProfileScreenBrightness(context, profile)) {
            if (profile.getDeviceBrightnessChange()) {
                GlobalData.logE("ActivateProfileHelper.execute", "set brightness: profile=" + profile._name);
                GlobalData.logE("ActivateProfileHelper.execute", "set brightness: _deviceBrightness=" + profile._deviceBrightness);

                if (profile.getDeviceBrightnessAutomatic()) {
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            profile.getDeviceBrightnessManualValue(context));
                    if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, context)
                            == GlobalData.PREFERENCE_ALLOWED) {
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
                                String command1 = "settings put system " + ADAPTIVE_BRIGHTNESS_SETTING_NAME + " " +
                                        Float.toString(profile.getDeviceBrightnessAdaptiveValue(context));
                                //if (GlobalData.isSELinuxEnforcing())
                                //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                Command command = new Command(0, false, command1); //, command2);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                    //RootTools.closeAllShells();
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.execute", "Error on run su: " + e.toString());
                                }
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

                if (brightnessHandler != null) {
                    final Profile __profile = profile;
                    final Context __context = context;
                    brightnessHandler.post(new Runnable() {
                        public void run() {
                            createBrightnessView(__profile, __context);
                        }
                    });
                } else
                    createBrightnessView(profile, context);
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

        // nahodenie pozadia
        if (Permissions.checkProfileWallpaper(context, profile)) {
            if (profile._deviceWallpaperChange == 1) {
                Intent wallpaperServiceIntent = new Intent(context, ExecuteWallpaperProfilePrefsService.class);
                wallpaperServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                wallpaperServiceIntent.putExtra(GlobalData.EXTRA_MERGED_PROFILE, merged);
                context.startService(wallpaperServiceIntent);
            }
        }

        // set power save mode
        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context) == GlobalData.PREFERENCE_ALLOWED) {
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
                setPowerSaveMode(_isPowerSaveMode);
            }
        }

        if (interactive)
        {
            // preferences, ktore vyzaduju interakciu uzivatela

            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == GlobalData.PREFERENCE_ALLOWED)
            {
                if (profile._deviceMobileDataPrefs == 1)
                {
                    final Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        final ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.Settings");
                        //intent.addCategory(Intent.ACTION_MAIN);
                        intent.setComponent(componentName);
                        context.startActivity(intent);
                    }
                }
            }

            //if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_GPS, context))
            //{  No check only GPS
                if (profile._deviceLocationServicePrefs == 1)
                {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            //}

            if (profile._deviceRunApplicationChange == 1)
            {
                Intent runApplicationsServiceIntent = new Intent(context, ExecuteRunApplicationsProfilePrefsService.class);
                runApplicationsServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                runApplicationsServiceIntent.putExtra(GlobalData.EXTRA_MERGED_PROFILE, merged);
                context.startService(runApplicationsServiceIntent);
            }
        }

    }

    private static void screenTimeoutLock(Context context)
    {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        screenTimeoutUnlock(context);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_TOAST,
                //TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        if (android.os.Build.VERSION.SDK_INT < 17)
            params.gravity = Gravity.RIGHT | Gravity.TOP;
        else
            params.gravity = Gravity.END | Gravity.TOP;
        GUIData.keepScreenOnView = new BrightnessView(context);
        windowManager.addView(GUIData.keepScreenOnView, params);
    }

    public static void screenTimeoutUnlock(Context context)
    {
        if (GUIData.keepScreenOnView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GUIData.keepScreenOnView);
            } catch (Exception e) {
            }
            GUIData.keepScreenOnView = null;
        }

        GlobalData.logE("@@@ screenTimeoutLock.unlock", "xxx");
    }

    @SuppressLint("RtlHardcoded")
    private void createBrightnessView(Profile profile, Context context)
    {
        //if (dataWrapper.context != null)
        //{

            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            if (GUIData.brightneesView != null)
            {
                try {
                    windowManager.removeView(GUIData.brightneesView);
                } catch (Exception e) {
                }
                GUIData.brightneesView = null;
            }
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        1, 1,
                        WindowManager.LayoutParams.TYPE_TOAST,
                        //TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT
                    );
            if (android.os.Build.VERSION.SDK_INT < 17)
                params.gravity = Gravity.RIGHT | Gravity.TOP;
            else
                params.gravity = Gravity.END | Gravity.TOP;
            if (profile.getDeviceBrightnessAutomatic())
                params.screenBrightness = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            else
                params.screenBrightness = profile.getDeviceBrightnessManualValue(context) / (float) 255;
            GUIData.brightneesView = new BrightnessView(context);
            windowManager.addView(GUIData.brightneesView, params);

            RemoveBrightnessViewBroadcastReceiver.setAlarm(context);

        //}
    }

    public static void removeBrightnessView(Context context) {
        if (GUIData.brightneesView != null)
        {
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            try {
                windowManager.removeView(GUIData.brightneesView);
            } catch (Exception e) {
            }
            GUIData.brightneesView = null;
        }
    }

    public void showNotification(Profile profile)
    {
        if (lockRefresh)
            // no refres notification
            return;

        if (GlobalData.notificationStatusBar)
        {
            boolean notificationShowInStatusBar = GlobalData.notificationShowInStatusBar;
            boolean notificationStatusBarPermanent = GlobalData.notificationStatusBarPermanent;
            boolean forceShow = false;

            if (!GlobalData.notificationStatusBar) {
                notificationShowInStatusBar = false;
                notificationStatusBarPermanent = false;
                forceShow = true;
            }

            // close showed notification
            //notificationManager.cancel(GlobalData.NOTIFICATION_ID);

            // vytvorenie intentu na aktivitu, ktora sa otvori na kliknutie na notifikaciu
            Intent intent = new Intent(context, LauncherActivity.class);
            // clear all opened activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            // nastavime, ze aktivita sa spusti z notifikacnej listy
            intent.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_NOTIFICATION);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie intentu na restart events
            Intent intentRE = new Intent(context, RestartEventsFromNotificationActivity.class);
            PendingIntent pIntentRE = PendingIntent.getActivity(context, 0, intentRE, PendingIntent.FLAG_CANCEL_CURRENT);

            // vytvorenie samotnej notifikacie
            NotificationCompat.Builder notificationBuilder;
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_drawer);

            boolean isIconResourceID;
            String iconIdentifier;
            String profileName;
            Bitmap iconBitmap;
            Bitmap preferencesIndicator;

            if (profile != null)
            {
                isIconResourceID = profile.getIsIconResourceID();
                iconIdentifier = profile.getIconIdentifier();
                profileName = dataWrapper.getProfileNameWithManualIndicator(profile, true, false);
                iconBitmap = profile._iconBitmap;
                preferencesIndicator = profile._preferencesIndicator;
            }
            else
            {
                isIconResourceID = true;
                iconIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
                profileName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                iconBitmap = null;
                preferencesIndicator = null;
            }

            notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentIntent(pIntent);

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                if (notificationShowInStatusBar)
                    notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                else
                    notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
                //notificationBuilder.setPriority(Notification.PRIORITY_HIGH); // for heads-up in Android 5.0
            }
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            notificationBuilder.setTicker(profileName);

            if (isIconResourceID)
            {
                int iconSmallResource;
                if (iconBitmap != null) {
                    iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                    notificationBuilder.setSmallIcon(iconSmallResource);

                    contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
                }
                else {
                    if (GlobalData.notificationStatusBarStyle.equals("0") && (android.os.Build.VERSION.SDK_INT < 21)) {
                        //notificationBuilder.setSmallIcon(0);
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify_color", "drawable", context.getPackageName());
                        notificationBuilder.setSmallIcon(iconSmallResource);
                        //contentView.setImageViewResource(R.id.notification_activated_profile_icon, 0);
                        contentView.setImageViewResource(R.id.notification_activated_profile_icon, iconSmallResource);
                    } else {
                        //notificationBuilder.setSmallIcon(0);
                        //contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, null);
                        iconSmallResource = context.getResources().getIdentifier(iconIdentifier + "_notify", "drawable", context.getPackageName());
                        notificationBuilder.setSmallIcon(iconSmallResource);
                        int iconLargeResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResource);
                        contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, largeIcon);
                    }
                }
            }
            else
            {
                int iconSmallResource;
                if (GlobalData.notificationStatusBarStyle.equals("0"))
                    iconSmallResource = R.drawable.ic_profile_default;
                else
                    iconSmallResource = R.drawable.ic_profile_default_notify;

                //notificationBuilder.setSmallIcon(0);
                notificationBuilder.setSmallIcon(iconSmallResource);

                //contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, null);
                contentView.setImageViewBitmap(R.id.notification_activated_profile_icon, iconBitmap);
            }

            GlobalData.phoneProfilesNotification = notificationBuilder.build();

            if (GlobalData.notificationTextColor.equals("1"))
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.BLACK);
            else
            if (GlobalData.notificationTextColor.equals("2"))
                contentView.setTextColor(R.id.notification_activated_profile_name, Color.WHITE);
            contentView.setTextViewText(R.id.notification_activated_profile_name, profileName);

            //contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator,
            //		ProfilePreferencesIndicator.paint(profile, context));
            if ((preferencesIndicator != null) && (GlobalData.notificationPrefIndicator))
                contentView.setImageViewBitmap(R.id.notification_activated_profile_pref_indicator, preferencesIndicator);
            else
                contentView.setImageViewResource(R.id.notification_activated_profile_pref_indicator, R.drawable.ic_empty);

            if (GlobalData.notificationTextColor.equals("1"))
                contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, R.drawable.ic_action_events_restart);
            else
            if (GlobalData.notificationTextColor.equals("2"))
                contentView.setImageViewResource(R.id.notification_activated_profile_restart_events, R.drawable.ic_action_events_restart_dark);
            contentView.setOnClickPendingIntent(R.id.notification_activated_profile_restart_events, pIntentRE);

            GlobalData.phoneProfilesNotification.contentView = contentView;

            if (notificationStatusBarPermanent)
            {
                //notification.flags |= Notification.FLAG_NO_CLEAR;
                GlobalData.phoneProfilesNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            }
            else
            {
                setAlarmForNotificationCancel(forceShow);
            }

            if (GlobalData.phoneProfilesService != null)
                GlobalData.phoneProfilesService.startForeground(GlobalData.PROFILE_NOTIFICATION_ID, GlobalData.phoneProfilesNotification);
            else
                notificationManager.notify(GlobalData.PROFILE_NOTIFICATION_ID, GlobalData.phoneProfilesNotification);
        }
        else
        {
            if (GlobalData.phoneProfilesService != null)
                GlobalData.phoneProfilesService.stopForeground(true);
            else
                notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
        }
    }

    public void removeNotification()
    {
        if (GlobalData.phoneProfilesService != null)
            GlobalData.phoneProfilesService.stopForeground(true);
        else
            notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
    }

    private void setAlarmForNotificationCancel(boolean forceShow)
    {
        if (!forceShow) {
            if (GlobalData.notificationStatusBarCancel.isEmpty() || GlobalData.notificationStatusBarCancel.equals("0"))
                return;
        }

        int notificationStatusBarCancel = Integer.valueOf(GlobalData.notificationStatusBarCancel);
        if (forceShow)
            notificationStatusBarCancel = 3;

        Intent intent = new Intent(context, NotificationCancelAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + notificationStatusBarCancel * 1000;
        // not needed exact for removing notification
        /*if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        else*/
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

    }

    public void setAlarmForRecreateNotification()
    {
        Intent intent = new Intent(context, RecreateNotificationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        long time = now.getTimeInMillis() + 500;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    public void updateWidget()
    {
        if (lockRefresh)
            // no refres widgets
            return;

        // icon widget
        Intent intent = new Intent(context, IconWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        // one row widget
        Intent intent4 = new Intent(context, OneRowWidgetProvider.class);
        intent4.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids4[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
        intent4.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids4);
        context.sendBroadcast(intent4);

        // list widget
        Intent intent2 = new Intent(context, ProfileListWidgetProvider.class);
        intent2.setAction(ProfileListWidgetProvider.INTENT_REFRESH_LISTWIDGET);
        int ids2[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
        context.sendBroadcast(intent2);

        // dashclock extension
        Intent intent3 = new Intent();
        intent3.setAction(DashClockBroadcastReceiver.INTENT_REFRESH_DASHCLOCK);
        context.sendBroadcast(intent3);

        // activities
        Intent intent5 = new Intent();
        intent5.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
        context.sendBroadcast(intent5);

    }

    private boolean isAirplaneMode(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            return Settings.Global.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
        else
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void setAirplaneMode(Context context, boolean mode)
    {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            setAirplaneMode_SDK17(context, mode);
        else
            setAirplaneMode_SDK8(context, mode);
    }

    private boolean isMobileData(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            return Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        else
        {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                final Class<?> connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
                final Method getMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                return (Boolean)getMobileDataEnabledMethod.invoke(connectivityManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        }

        /*
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            int netvorkType = networkInfo.getType(); // 0 = mobile, 1 = wifi
            //String netvorkTypeName = networkInfo.getTypeName(); // "mobile" or "WIFI"
            boolean connected = networkInfo.isConnected();  // true = active connection

            //if (netvorkType == 0)
            //{
                // connected into mobile data
                return connected;
            //}
            //else
            //{
                // conected into Wifi
            //	return false;
            //}
        }
        else
            return false;
        */

    }

    private void setMobileData(Context context, boolean enable)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            if (GlobalData.grantRoot(false))
            {
                int state = 0;
                try {
                    // Get the current state of the mobile network.
                    state = enable ? 1 : 0;
                    // Get the value of the "TRANSACTION_setDataEnabled" field.
                    String transactionCode = GlobalData.getTransactionCode(context, "TRANSACTION_setDataEnabled");
                    // Android 5.1+ (API 22) and later.
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        // Loop through the subscription list i.e. SIM list.
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            if (transactionCode != null && transactionCode.length() > 0) {
                                // Get the active subscription ID for a given SIM card.
                                int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                                String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                                Command command = new Command(0, false, command1);
                                try {
                                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                    commandWait(command);
                                    //RootTools.closeAllShells();
                                } catch (Exception e) {
                                    Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                                }
                            }
                        }
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        // Android 5.0 (API 21) only.
                        if (transactionCode != null && transactionCode.length() > 0) {
                            String command1 = "service call phone " + transactionCode + " i32 " + state;
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                                //RootTools.closeAllShells();
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setMobileData", "Error on run su");
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
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

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (!OK)
            {
                try {
                    Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);

                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(connectivityManager, enable);

                    OK = true;

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getPreferredNetworkType(Context context) {
        if (GlobalData.grantRoot(false))
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = GlobalData.getTransactionCode(context, "TRANSACTION_getPreferredNetworkType");
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
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return networkType;
    }

    private void setPreferredNetworkType(Context context, int networkType)
    {
        if (GlobalData.grantRoot(false))
        {
            try {
                // Get the value of the "TRANSACTION_setPreferredNetworkType" field.
                String transactionCode = GlobalData.getTransactionCode(context, "TRANSACTION_setPreferredNetworkType");
                if (Build.VERSION.SDK_INT >= 23) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    // Loop through the subscription list i.e. SIM list.
                    for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                        if (transactionCode != null && transactionCode.length() > 0) {
                            // Get the active subscription ID for a given SIM card.
                            int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                            String command1 = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + networkType;
                            Command command = new Command(0, false, command1);
                            try {
                                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                                commandWait(command);
                                //RootTools.closeAllShells();
                            } catch (Exception e) {
                                Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                            }
                        }
                    }
                } else  {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        String command1 = "service call phone " + transactionCode + " i32 " + networkType;
                        Command command = new Command(0, false, command1);
                        try {
                            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                            commandWait(command);
                            //RootTools.closeAllShells();
                        } catch (Exception e) {
                            Log.e("ActivateProfileHelper.setPreferredNetworkType", "Error on run su");
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
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
        if (android.os.Build.VERSION.SDK_INT < 21)
            isEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
        else {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        GlobalData.logE("ActivateProfileHelper.setGPS", "isEnabled="+isEnabled);

        //if(!provider.contains(LocationManager.GPS_PROVIDER) && enable)
        if ((!isEnabled)  && enable)
        {
            if (GlobalData.canExploitGPS(context))
            {
                GlobalData.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            else
            if ((android.os.Build.VERSION.SDK_INT >= 17) && GlobalData.grantRoot(false))
            {
                // zariadenie je rootnute
                GlobalData.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                    String newSet;
                    if (provider == "")
                        newSet = LocationManager.GPS_PROVIDER;
                    else
                        newSet = String.format("%s,%s", provider, LocationManager.GPS_PROVIDER);

                    command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                    //if (GlobalData.isSELinuxEnforcing())
                    //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);

                    //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state true";
                    Command command = new Command(0, false, command1); //, command2);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                    }
                }
                else {
                    command1 = "settings put secure location_providers_allowed +gps";
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                    }
                }
            }
            else
            {
                /*GlobalData.logE("ActivateProfileHelper.setGPS", "old method");

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
            }
        }
        else
        //if(provider.contains(LocationManager.GPS_PROVIDER) && (!enable))
        if (isEnabled && (!enable))
        {
            if (GlobalData.canExploitGPS(context))
            {
                GlobalData.logE("ActivateProfileHelper.setGPS", "exploit");

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                context.sendBroadcast(poke);
            }
            else
            if ((android.os.Build.VERSION.SDK_INT >= 17) && GlobalData.grantRoot(false))
            {
                // zariadenie je rootnute
                GlobalData.logE("ActivateProfileHelper.setGPS", "rooted");

                String command1;
                //String command2;

                if (android.os.Build.VERSION.SDK_INT < 23) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                    String[] list = provider.split(",");

                    String newSet = "";
                    int j = 0;
                    for (int i = 0; i < list.length; i++) {

                        if (!list[i].equals(LocationManager.GPS_PROVIDER)) {
                            if (j > 0)
                                newSet += ",";
                            newSet += list[i];
                            j++;
                        }
                    }

                    command1 = "settings put secure location_providers_allowed \"" + newSet + "\"";
                    //if (GlobalData.isSELinuxEnforcing())
                    //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                    //command2 = "am broadcast -a android.location.GPS_ENABLED_CHANGE --ez state false";
                    Command command = new Command(0, false, command1);//, command2);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                    }
                }
                else {
                    command1 = "settings put secure location_providers_allowed -gps";
                    Command command = new Command(0, false, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                        commandWait(command);
                        //RootTools.closeAllShells();
                    } catch (Exception e) {
                        Log.e("ActivateProfileHelper.setGPS", "Error on run su: " + e.toString());
                    }
                }
            }
            else
            {
                //GlobalData.logE("ActivateProfileHelper.setGPS", "old method");

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
            }
        }	    	
    }

    private void setAirplaneMode_SDK17(Context context, boolean mode)
    {
        if (GlobalData.grantRoot(false))
        {
            // zariadenie je rootnute
            String command1;
            String command2;
            if (mode)
            {
                command1 = "settings put global airplane_mode_on 1";
                command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
            }
            else
            {
                command1 = "settings put global airplane_mode_on 0";
                command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
            }
            //if (GlobalData.isSELinuxEnforcing())
            //{
            //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
            //	command2 = GlobalData.getSELinuxEnforceCommand(command2, Shell.ShellContext.SYSTEM_APP);
            //}
            Command command = new Command(0, false, command1, command2);
            try {
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                commandWait(command);
                //RootTools.closeAllShells();
            } catch (Exception e) {
                Log.e("AirPlaneMode_SDK17.setAirplaneMode", "Error on run su");
            }
        }
        else
        {
            // for normal apps it is only possible to open the system settings dialog
        /*	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent); */
        }

    }

    @SuppressWarnings("deprecation")
    private void setAirplaneMode_SDK8(Context context, boolean mode)
    {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, mode ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }

    private void setPowerSaveMode(boolean enable) {
        String command1 = "settings put global low_power " + ((enable) ? 1 : 0);
        Command command = new Command(0, false, command1);
        try {
            RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
            commandWait(command);
            //RootTools.closeAllShells();
        } catch (Exception e) {
            Log.e("ActivateProfileHelper.setPowerSaveMode", "Error on run su: " + e.toString());
        }
    }

    private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("ActivateProfileHelper", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }	

    public static int getMinimumScreenBrightnessSetting (Context context)
    {
        final Resources res = Resources.getSystem();
        int id = res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"); // API17+
        if (id == 0)
            id = res.getIdentifier("config_screenBrightnessDim", "integer", "android"); // lower API levels
        if (id != 0)
        {
            try {
              return res.getInteger(id);
            }
            catch (Resources.NotFoundException e) {
              // ignore
            }
        }
        return 0;
    }

    public static int getMaximumScreenBrightnessSetting (Context context)
    {
        final Resources res = Resources.getSystem();
        final int id = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");  // API17+
        if (id != 0)
        {
            try {
              return res.getInteger(id);
            }
            catch (Resources.NotFoundException e) {
              // ignore
            }
        }
        return 255;
    }

}
