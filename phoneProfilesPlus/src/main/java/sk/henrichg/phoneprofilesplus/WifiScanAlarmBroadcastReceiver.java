package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WifiScanAlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiScanAlarm";
    public static final String EXTRA_ONESHOT = "oneshot";

    public static WifiManager wifi = null;
    private static WifiLock wifiLock = null;
    //private static PowerManager.WakeLock wakeLock = null;

    //public static List<WifiSSIDData> scanResults = null;
    //public static List<WifiSSIDData> wifiConfigurationList = null;

    static final String PREF_EVENT_WIFI_SCAN_REQUEST = "eventWifiScanRequest";
    static final String PREF_EVENT_WIFI_WAIT_FOR_RESULTS = "eventWifiWaitForResults";
    static final String PREF_EVENT_WIFI_ENABLED_FOR_SCAN = "eventWifiEnabledForScan";


    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiScanAlarmBroadcastReceiver.onReceive", "xxx");

        //PPApplication.loadPreferences(context);

        setAlarm(context, false, false);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context) !=
                PPApplication.PREFERENCE_ALLOWED) {
            removeAlarm(context);
            return;
        }

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Event.getGlobalEventsRuning(context))
        {
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.onReceive", "xxx");

            startScanner(context);
        }

    }

    public static void initialize(Context context)
    {
        setScanRequest(context, false);
        setWaitForResults(context, false);
        setWifiEnabledForScan(context, false);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context) !=
                PPApplication.PREFERENCE_ALLOWED)
            return;

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        unlock();

        clearScanResults(context)
        ;
        /*
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        //if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
        if ((activeNetwork != null) &&
            (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
            activeNetwork.isConnected())
            editor.putInt(PPApplication.PREF_EVENT_WIFI_LAST_STATE, 1);
        else
        //if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED)
            editor.putInt(PPApplication.PREF_EVENT_WIFI_LAST_STATE, 0);
        //else
        //    editor.putInt(PPApplication.PREF_EVENT_WIFI_LAST_STATE, -1);
        editor.commit();
        */

        fillWifiConfigurationList(context);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    public static void setAlarm(Context context, boolean shortInterval, boolean forScreenOn)
    {
        //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context)
                == PPApplication.PREFERENCE_ALLOWED)
        {
            PPApplication.logE("WifiScanAlarmBroadcastReceiver.setAlarm","WifiHardware=true");

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);

            removeAlarm(context);

            Calendar calendar = Calendar.getInstance();

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

            if (shortInterval) {
                if (forScreenOn)
                    calendar.add(Calendar.SECOND, 5);
                else
                    calendar.add(Calendar.SECOND, 5);
            }
            else {
                int interval = ApplicationPreferences.applicationEventWifiScanInterval(context);
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
                if (isPowerSaveMode && ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context).equals("1"))
                    interval = 2 * interval;
                calendar.add(Calendar.MINUTE, interval);
            }
            long alarmTime = calendar.getTimeInMillis();

            intent.putExtra(EXTRA_ONESHOT, 0);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                //alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else if (android.os.Build.VERSION.SDK_INT >= 19)
                //alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm", "oneshot=" + oneshot + "; alarm is set");
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm", "alarm is set");

        }
        else
            PPApplication.logE("WifiScanAlarmBroadcastReceiver.setAlarm","WifiHardware=false");
    }

    public static void removeAlarm(Context context/*, boolean oneshot*/)
    {
        //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","alarm not found");
    }

    public static boolean isAlarmSet(Context context/*, boolean oneshot*/)
    {
        Intent intent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","alarm found");
        else
            //PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm not found");
            PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","alarm not found");

        return (pendingIntent != null);
    }

    public static void lock(Context context)
    {
        //PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //if (android.os.Build.VERSION.SDK_INT >= 23)
        //    PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.lock","idleMode="+powerManager.isDeviceIdleMode());

         // initialise the locks
        if (wifiLock == null)
            wifiLock = wifi.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY , "WifiScanWifiLock");
        //if (wakeLock == null) - moved to ScannerService
        //    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiScanWakeLock");

        try {
        //    if (!wakeLock.isHeld())
        //        wakeLock.acquire();
            if (!wifiLock.isHeld())
                wifiLock.acquire();
            PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.lock","xxx");
        } catch(Exception e) {
            Log.e("WifiScanAlarmBroadcastReceiver.lock", "Error getting Lock: "+e.getMessage());
            PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.lock", "Error getting Lock: " + e.getMessage());
        }
    }
 
    public static void unlock()
    {
        //if ((wakeLock != null) && (wakeLock.isHeld()))
        //    wakeLock.release();
        if ((wifiLock != null) && (wifiLock.isHeld()))
            wifiLock.release();
        PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.unlock", "xxx");
    }
    
    public static void sendBroadcast(Context context)
    {
        Intent broadcastIntent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);
        context.sendBroadcast(broadcastIntent);
    }
    
    static public boolean getScanRequest(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_EVENT_WIFI_SCAN_REQUEST, false);
    }

    static public void setScanRequest(Context context, boolean scanRequest)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_SCAN_REQUEST, scanRequest);
        editor.commit();
        PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setScanRequest","scanRequest="+scanRequest);
    }

    static public boolean getWaitForResults(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_EVENT_WIFI_WAIT_FOR_RESULTS, false);
    }

    static public void setWaitForResults(Context context, boolean waitForResults)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_WAIT_FOR_RESULTS, waitForResults);
        editor.commit();
        PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.setWaitForResults", "waitForResults=" + waitForResults);
    }

    static public void startScan(Context context)
    {
        lock(context); // lock wakeLock and wifiLock, then scan.
                    // unlock() is then called at the end of the onReceive function of WifiScanBroadcastReceiver
        boolean startScan = wifi.startScan();
        PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.startScan","scanStarted="+startScan);
        PPApplication.logE("$$$ WifiAP", "WifiScanAlarmBroadcastReceiver.startScan-startScan="+startScan);
        if (!startScan)
        {
            if (getWifiEnabledForScan(context))
            {
                PPApplication.logE("$$$ WifiScanAlarmBroadcastReceiver.startScan","disable wifi");
                wifi.setWifiEnabled(false);
            }
            unlock();
        }
        setWaitForResults(context, startScan);
        setScanRequest(context, false);
    }

    static public void startScanner(Context context)
    {
        Intent scanServiceIntent = new Intent(context, ScannerService.class);
        scanServiceIntent.putExtra(ScannerService.EXTRA_SCANNER_TYPE, ScannerService.SCANNER_TYPE_WIFI);
        context.startService(scanServiceIntent);
    }

    /*
    static public void stopScan(Context context)
    {
        PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.stopScan","xxx");
        unlock();
        if (getWifiEnabledForScan(context))
            wifi.setWifiEnabled(false);
        setWifiEnabledForScan(context, false);
        setScanRequest(context, false);
        setWaitForResults(context, false);
        PPApplication.setForceOneWifiScan(context, false);
    }
    */

    static public boolean getWifiEnabledForScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_EVENT_WIFI_ENABLED_FOR_SCAN, false);
    }

    static public void setWifiEnabledForScan(Context context, boolean setEnabled)
    {
        PPApplication.logE("@@@ WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan","setEnabled="+setEnabled);
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED_FOR_SCAN, setEnabled);
        editor.commit();
    }

    static public void fillWifiConfigurationList(Context context)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        List<WifiSSIDData> wifiConfigurationList = new ArrayList<>();

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
            // wifi must be enabled for wifi.getConfiguredNetworks()
            return;

        List<WifiConfiguration> _wifiConfigurationList = wifi.getConfiguredNetworks();
        if (_wifiConfigurationList != null)
        {
            wifiConfigurationList.clear();
            for (WifiConfiguration device : _wifiConfigurationList)
            {
                boolean found = false;
                for (WifiSSIDData _device : wifiConfigurationList)
                {
                    //if (_device.bssid.equals(device.BSSID))
                    if ((_device.ssid != null) && (_device.ssid.equals(device.SSID)))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    wifiConfigurationList.add(new WifiSSIDData(device.SSID, device.BSSID, false));
                }
            }
        }
        //saveWifiConfigurationList(context);
        saveWifiConfigurationList(context, wifiConfigurationList);
    }

    @SuppressWarnings("deprecation")
    static public void fillScanResults(Context context)
    {
        List<WifiSSIDData> scanResults = new ArrayList<>();
        boolean save = false;

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Permissions.checkLocation(context)) {
            List<ScanResult> _scanResults = wifi.getScanResults();
            PPApplication.logE("%%%% WifiScanAlarmBroadcastReceiver.fillScanResults", "_scanResults="+_scanResults);
            if (_scanResults != null) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                //boolean isScreenOn = pm.isScreenOn();
                //PPApplication.logE("%%%% WifiScanAlarmBroadcastReceiver.fillScanResults", "isScreenOn="+isScreenOn);
                //if ((android.os.Build.VERSION.SDK_INT < 21) || (_scanResults.size() > 0) || isScreenOn) {
                    save = true;
                    scanResults.clear();
                    for (ScanResult device : _scanResults) {
                        boolean found = false;
                        for (WifiSSIDData _device : scanResults) {
                            if (_device.bssid.equals(device.BSSID)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            scanResults.add(new WifiSSIDData(device.SSID, device.BSSID, false));
                        }
                    }
                //}
            }
        }
        else
            save = true;
        if (save)
            saveScanResults(context, scanResults);
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getWifiConfigurationList(Context context)
    public static List<WifiSSIDData> getWifiConfigurationList(Context context)
    {
        synchronized (PPApplication.scanResultsMutex) {
            //if (wifiConfigurationList == null)
            //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

            //wifiConfigurationList.clear();

            List<WifiSSIDData> wifiConfigurationList = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    WifiSSIDData device = gson.fromJson(json, WifiSSIDData.class);
                    wifiConfigurationList.add(device);
                }
            }

            return wifiConfigurationList;
        }
    }

    //private static void saveWifiConfigurationList(Context context)
    private static void saveWifiConfigurationList(Context context, List<WifiSSIDData> wifiConfigurationList)
    {
        synchronized (PPApplication.scanResultsMutex) {
            //if (wifiConfigurationList == null)
            //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(SCAN_RESULT_COUNT_PREF, wifiConfigurationList.size());

            Gson gson = new Gson();

            for (int i = 0; i < wifiConfigurationList.size(); i++) {
                String json = gson.toJson(wifiConfigurationList.get(i));
                editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
            }

            editor.commit();
        }
    }

    //public static void getScanResults(Context context)
    public static List<WifiSSIDData> getScanResults(Context context)
    {
        synchronized (PPApplication.scanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, -1);

            if (count > -1) {
                List<WifiSSIDData> scanResults = new ArrayList<>();

                Gson gson = new Gson();

                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                    if (!json.isEmpty()) {
                        WifiSSIDData device = gson.fromJson(json, WifiSSIDData.class);
                        scanResults.add(device);
                    }
                }
                return scanResults;
            } else
                return null;
        }
    }

    //private static void saveScanResults(Context context)
    private static void saveScanResults(Context context, List<WifiSSIDData> scanResults)
    {
        synchronized (PPApplication.scanResultsMutex) {
            //if (scanResults == null)
            //    scanResults = new ArrayList<WifiSSIDData>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(SCAN_RESULT_COUNT_PREF, scanResults.size());

            Gson gson = new Gson();

            for (int i = 0; i < scanResults.size(); i++) {
                String json = gson.toJson(scanResults.get(i));
                editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
            }

            editor.commit();
        }
    }

    public static void clearScanResults(Context context) {
        synchronized (PPApplication.scanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();
            editor.putInt(SCAN_RESULT_COUNT_PREF, -1);

            editor.commit();
        }
    }

    public static String getSSID(WifiInfo wifiInfo, List<WifiSSIDData> wifiConfigurationList)
    {
        String SSID = wifiInfo.getSSID();
        if (SSID == null)
            SSID = "";
        SSID = SSID.replace("\"", "");

        if (SSID.isEmpty())
        {
            if (wifiConfigurationList != null)
            {
                for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                {
                    if (wifiConfiguration.bssid.equals(wifiInfo.getBSSID()))
                        return wifiConfiguration.ssid.replace("\"", "");
                }
            }
        }

        return SSID;
    }

    public static boolean compareSSID(WifiInfo wifiInfo, String SSID, List<WifiSSIDData> wifiConfigurationList)
    {
        String wifiInfoSSID = getSSID(wifiInfo, wifiConfigurationList);
        String ssid2 = "\"" + SSID + "\"";
        //return (wifiInfoSSID.equals(SSID) || wifiInfoSSID.equals(ssid2));
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%', true) || Wildcard.match(wifiInfoSSID, ssid2, '_', '%', true));
    }

    public static String getSSID(WifiSSIDData result, List<WifiSSIDData> wifiConfigurationList)
    {
        String SSID;
        if (result.ssid == null)
            SSID = "";
        else
            SSID = result.ssid.replace("\"", "");

        if (SSID.isEmpty())
        {
            if (wifiConfigurationList != null)
            {
                for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                {
                    if ((wifiConfiguration.bssid != null) &&
                            (wifiConfiguration.bssid.equals(result.bssid)))
                        return wifiConfiguration.ssid.replace("\"", "");
                }
            }
        }

        //PPApplication.logE("@@@x WifiScanAlarmBroadcastReceiver.getSSID", "SSID="+SSID);

        return SSID;
    }

    public static boolean compareSSID(WifiSSIDData result, String SSID, List<WifiSSIDData> wifiConfigurationList)
    {
        String wifiInfoSSID = getSSID(result, wifiConfigurationList);
        String ssid2 = "\"" + SSID + "\"";
        //PPApplication.logE("@@@x WifiScanAlarmBroadcastReceiver.compareSSID", "wifiInfoSSID="+wifiInfoSSID);
        //PPApplication.logE("@@@x WifiScanAlarmBroadcastReceiver.compareSSID", "ssid2="+ssid2);

        //return (getSSID(result).equals(SSID) || getSSID(result).equals(ssid2));
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%', true) || Wildcard.match(wifiInfoSSID, ssid2, '_', '%', true));
    }

}
