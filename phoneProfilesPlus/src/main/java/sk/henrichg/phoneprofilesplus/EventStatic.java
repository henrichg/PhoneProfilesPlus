package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
class EventStatic {

    private EventStatic() {
        // private constructor to prevent instantiation
    }

    static PreferenceAllowed isEventPreferenceAllowed(String preferenceKey, boolean notCheckPreferences, Context context)
    {
        Context appContext = context.getApplicationContext();

        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();

        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;

        //boolean checked = false;

        if (preferenceKey.equals(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED)) {
            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }

        if (preferenceKey.equals(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_WIFI) {
                // device has Wifi
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_BLUETOOTH) {
                // device has bluetooth
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetooth != null) {
                    if (notCheckPreferences)
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    else {
                        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH) &&
                                Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_ADMIN))
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                            preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_bluetooth_permission);
                        }
                    }
                } else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED))
        {
            //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            /*else {
                PPApplication.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }*/
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED))
        {
            //if (PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED))
        {
            boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
            //boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
            boolean hasProximity = PPApplication.proximitySensor != null;
            boolean hasLight = PPApplication.lightSensor != null;

            boolean enabled = hasAccelerometer;
            enabled = enabled || hasProximity || hasLight;

            if (enabled) {
                //if (PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                //else
                //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED) ||
                preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED)) {
//                        Log.e("EventStatic.isEventPreferenceAllowed", "("+preferenceKey+") called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        boolean simExists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        boolean simExists = telephonyManager.getPhoneCount() > 0;
//                        Log.e("EventStatic.isEventPreferenceAllowed", "simExists="+simExists);
                        if (simExists)
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (notCheckPreferences)
                                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            else {
                                if (!Permissions.checkReadPhoneState(appContext)) {
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                                } else
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        }
                    }
                    else
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_NFC) {
                // device has nfc
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED) ||
                preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED)) {
//                        Log.e("EventStatic.isEventPreferenceAllowed", "("+preferenceKey+") called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        boolean simExists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        boolean simExists = telephonyManager.getPhoneCount() > 0;
                        if (simExists)
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (notCheckPreferences)
                                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            else {
                                if (!Permissions.checkReadPhoneState(appContext)) {
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                                } else
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        }
                    }
                    else
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED) ||
                preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED)) {
//                        Log.e("EventStatic.isEventPreferenceAllowed", "("+preferenceKey+") called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        boolean simExists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        boolean simExists = telephonyManager.getPhoneCount() > 0;
                        if (simExists)
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (notCheckPreferences)
                                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            else {
                                boolean notSetPermission = false;
                                if (!Permissions.checkReadPhoneState(appContext)) {
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                                    notSetPermission = true;
                                }
                                if (notSetPermission)
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        }
                    }
                    else
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_LOCATION) {
                // device has location
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        /*
        if (preferenceKey.equals(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED))
        {
            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;
        */

        if (preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED) ||
                preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED)) {
//                        Log.e("EventStatic.isEventPreferenceAllowed", "("+preferenceKey+") called hasSIMCard");
//                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
//                        boolean simExists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                        boolean simExists = telephonyManager.getPhoneCount() > 0;
                        if (simExists)
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (notCheckPreferences)
                                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            else {
                                if (!Permissions.checkReadPhoneState(appContext)) {
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                                } else
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        }
                    }
                    else
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //if (checked)
        //    return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_WIFI)) {
            if (PPApplication.HAS_FEATURE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH)) {
            if (PPApplication.HAS_FEATURE_BLUETOOTH) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetoothAdapter == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF) ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM) ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA)/* ||
                preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM)*/) {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                // device has telephony

                ConnectivityManager connManager = null;
                try {
                    connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                } catch (Exception e) {
                    //noinspection UnusedAssignment
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                }
                if (connManager == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else {
                    TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        //if (!preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM)) {
                        boolean simExists;
                        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM)) {
//                            Log.e("EventStatic.isEventPreferenceAllowed", "("+preferenceKey+") called hasSIMCard");
                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                            simExists = hasSIMCardData.hasSIM1 && hasSIMCardData.hasSIM2;
                        } else {
//                            simExists = hasSIMCardData.simCount > 0;//hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                            simExists = telephonyManager.getPhoneCount() > 0;
                        }
                        if (simExists)
                            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else {
                            if (notCheckPreferences)
                                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            else {
                                if (!Permissions.checkReadPhoneState(appContext)) {
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_PHONE_PERMISSION;
                                    preferenceAllowed.notAllowedReasonDetail = appContext.getString(R.string.preference_not_allowed_reason_detail_not_granted_phone_permission);
                                } else
                                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                            }
                        }
                        //}
                        //else
                        //    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                    } else
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                }
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_GPS)) {
            if (PPApplication.HAS_FEATURE_LOCATION_GPS) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NFC)) {
            if (PPApplication.HAS_FEATURE_NFC) {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter == null)
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                else
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }
        //noinspection IfStatementWithIdenticalBranches
        if (preferenceKey.equals(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE)) {
            preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED)) {
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null)
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            else
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED)) {
            KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (kgMgr == null)
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            else
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            return preferenceAllowed;
        }
        if (preferenceKey.equals(EventPreferencesCallControl.PREF_EVENT_CALL_CONTROL_ENABLED))
        {
            if (PPApplication.HAS_FEATURE_TELEPHONY) {
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            return preferenceAllowed;
        }

        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
        return preferenceAllowed;
    }

    static boolean getGlobalEventsRunning(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.getGlobalEventsRunning", "PPApplication.globalEventsRunStopMutex");
        synchronized (PPApplication.globalEventsRunStopMutex) {
            if (Build.VERSION.SDK_INT >= 33) {
                try {
                    ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(context.getApplicationContext(), PhoneProfilesService.class);
                    if (serviceInfo == null) {
                        // service is not running
                        return false;
                    }
                } catch (Exception ignored) {}
            }
            return PPApplication.globalEventsRunStop;
        }
    }

    static void setGlobalEventsRunning(Context context, boolean globalEventsRunning)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.setGlobalEventsRunning", "PPApplication.globalEventsRunStopMutex");
        synchronized (PPApplication.globalEventsRunStopMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, globalEventsRunning);
            editor.apply();
            PPApplication.globalEventsRunStop = globalEventsRunning;
        }
    }

    static boolean getEventsBlocked(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.getEventsBlocked", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            //ApplicationPreferences.prefEventsBlocked = ApplicationPreferences.
            //        getSharedPreferences(context).getBoolean(PREF_EVENTS_BLOCKED, false);
            //return prefEventsBlocked;
            return ApplicationPreferences.getSharedPreferences(context).getBoolean(Event.PREF_EVENTS_BLOCKED, false);
        }
    }
    static void setEventsBlocked(Context context, boolean eventsBlocked)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.setEventsBlocked", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_EVENTS_BLOCKED, eventsBlocked);
            editor.apply();
            //ApplicationPreferences.prefEventsBlocked = eventsBlocked;
        }
    }

    static boolean getForceRunEventRunning(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.getForceRunEventRunning", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            //ApplicationPreferences.prefForceRunEventRunning = ApplicationPreferences.
            //        getSharedPreferences(context).getBoolean(PREF_FORCE_RUN_EVENT_RUNNING, false);
            //return prefForceRunEventRunning;
            return ApplicationPreferences.getSharedPreferences(context).getBoolean(Event.PREF_FORCE_RUN_EVENT_RUNNING, false);
        }
    }
    static void setForceRunEventRunning(Context context, boolean forceRunEventRunning)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.setForceRunEventRunning", "PPApplication.eventsRunMutex");
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(Event.PREF_FORCE_RUN_EVENT_RUNNING, forceRunEventRunning);
            editor.apply();
            //ApplicationPreferences.prefForceRunEventRunning = forceRunEventRunning;
        }
    }

    static boolean runStopEvent(DataWrapper _dataWrapper,
                                Event _event,
                                EditorActivity editor) {
        if (EventStatic.getGlobalEventsRunning(_dataWrapper.context)) {
            // events are not globally stopped

            _dataWrapper.getEventTimelineList(true);
            if (_event.getStatusFromDB(_dataWrapper.context) == Event.ESTATUS_STOP) {
                if (!EventStatic.isRedTextNotificationRequired(_event, false, _dataWrapper.context)) {
                    // pause event
                    //IgnoreBatteryOptimizationNotification.showNotification(activityDataWrapper.context);

                    //final DataWrapper dataWrapper = activityDataWrapper;
                    final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(_dataWrapper);
                    final WeakReference<Event> eventWeakRef = new WeakReference<>(_event);
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.1");

                        DataWrapper dataWrapper = dataWrapperWeakRef.get();
                        Event event = eventWeakRef.get();

                        if ((dataWrapper != null) && (event != null)) {
                            synchronized (PPApplication.handleEventsMutex) {

                                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EditorEventListFragment_runStopEvent_1);
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    //                            PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.runStopEvent", "(1) PPApplication.eventsHandlerMutex");
                                    event.pauseEvent(dataWrapper, false, false,
                                            false, true, null, false, false, false, true);

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
                                }
                            }
                        }
                    };
                    PPApplicationStatic.createBasicExecutorPool();
                    PPApplication.eventsHandlerExecutor.submit(runnable);

                }
                else {
                    if (editor != null)
                        GlobalGUIRoutines.showDialogAboutRedText(null, _event, false, false, false, true, editor);
                    else
                        DataWrapperStatic.displayPreferencesErrorNotification(null, _event, false, _dataWrapper.context);
                    return false;
                }
            } else {
                // stop event

                //final DataWrapper dataWrapper = activityDataWrapper;
                final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(_dataWrapper);
                final WeakReference<Event> eventWeakRef = new WeakReference<>(_event);
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.2");

                    DataWrapper dataWrapper = dataWrapperWeakRef.get();
                    Event event = eventWeakRef.get();

                    if ((dataWrapper != null) && (event != null)) {
                        synchronized (PPApplication.handleEventsMutex) {
                            PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EditorEventListFragment_runStopEvent_2);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //                        PPApplicationStatic.logE("[SYNCHRONIZED] EventStatic.runStopEvent", "(2) PPApplication.eventsHandlerMutex");
                                event.stopEvent(dataWrapper, false, false,
                                        true, true, true); // activate return profile
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
                    }
                };
                PPApplicationStatic.createBasicExecutorPool();
                PPApplication.eventsHandlerExecutor.submit(runnable);

            }

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (editor != null)
                editor.redrawEventListFragment(_event, PPApplication.EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            _dataWrapper.restartEventsWithRescan(true, false, true, true, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(_dataWrapper.context, commandIntent);

//            PPApplicationStatic.logE("[MAIN_WORKER_CALL] EventStatic.runStopEvent", "(1) xxxxxxxxxxxxxxxxxxxx");

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG)
                            .setInitialDelay(30, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] EventStatic.runStopEvent", "(1)");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

        } else {
            if (_event.getStatusFromDB(_dataWrapper.context) == Event.ESTATUS_STOP) {
                // pause event
                _event.setStatus(Event.ESTATUS_PAUSE);
            } else {
                // stop event
                _event.setStatus(Event.ESTATUS_STOP);
            }

            // update event in DB
            DatabaseHandler.getInstance(_dataWrapper.context).updateEvent(_event);

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (editor != null)
                editor.redrawEventListFragment(_event, PPApplication.EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            _dataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(_dataWrapper.context, commandIntent);

//            PPApplicationStatic.logE("[MAIN_WORKER_CALL] EventStatic.runStopEvent", "(2) xxxxxxxxxxxxxxxxxxxx");

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG)
                            .setInitialDelay(30, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] EventStatic.runStopEvent", "(2)");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
        return true;
    }

    static boolean isRedTextNotificationRequired(Event event, boolean againCheckInDelay, Context context) {
        Context appContext = context.getApplicationContext();

        //(event._status <> Event.ESTATUS_STOP) &&

        boolean enabledSomeSensor;
        boolean grantedAllPermissions;
        boolean accessibilityEnabled;
        boolean eventIsRunnable;

        if (event._status == Event.ESTATUS_STOP) {
            enabledSomeSensor = true;
            grantedAllPermissions = true;
            accessibilityEnabled =  true;
            eventIsRunnable = true;
        } else {
            enabledSomeSensor = event.isEnabledSomeSensor(appContext);
            grantedAllPermissions = Permissions.checkEventPermissions(appContext, event, null, EventsHandler.SENSOR_TYPE_ALL).isEmpty();
            /*if (Build.VERSION.SDK_INT >= 29) {
                if (!Settings.canDrawOverlays(context))
                    grantedAllPermissions = false;
            }*/
            accessibilityEnabled =  event.isAccessibilityServiceEnabled(appContext, false, againCheckInDelay) == 1;

            eventIsRunnable = event.isRunnable(appContext, false) &&
                                event.isAllConfigured(appContext, false);
        }

        return ((!enabledSomeSensor) || (!grantedAllPermissions) || (!accessibilityEnabled) || (!eventIsRunnable));
    }

}

