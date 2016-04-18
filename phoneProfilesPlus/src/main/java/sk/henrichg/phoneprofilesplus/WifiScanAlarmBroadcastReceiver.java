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
import android.os.PowerManager;
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

    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### WifiScanAlarmBroadcastReceiver.onReceive", "xxx");

        GlobalData.loadPreferences(context);

        //int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
        //if (oneshot == 0)
            setAlarm(context, false, false);

        //if (scanResults == null)
        //    scanResults = new ArrayList<WifiSSIDData>();

        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) !=
                GlobalData.PREFERENCE_ALLOWED) {
            removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return;
        }

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // disabled fro firstStartEvents
        //if (!GlobalData.getApplicationStarted(context))
            // application is not started
        //	return;

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.onReceive", "xxx");

            //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
            //GlobalData.logE("$$$ WifiAP", "WifiScanAlarmBroadcastReceiver.onReceive-isWifiAPEnabled="+isWifiAPEnabled);

            // moved to ScannerService
            /*boolean wifiEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
            GlobalData.logE("WifiScanAlarmBroadcastReceiver.onReceive","wifiEventsExists="+wifiEventsExists);

            int forceScan = GlobalData.getForceOneWifiScan(context);
            boolean scan = (wifiEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED));
            if ((!wifiEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                scan = false;
            if (scan)
            {*/
                startScanner(context);
            /*}
            else {
                removeAlarm(context, false);
                removeAlarm(context, true);
            }

            dataWrapper.invalidateDataWrapper();*/
        }

    }

    public static void initialize(Context context)
    {
        setScanRequest(context, false);
        setWaitForResults(context, false);
        setWifiEnabledForScan(context, false);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) !=
                GlobalData.PREFERENCE_ALLOWED)
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

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        //if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
        if ((activeNetwork != null) &&
            (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
            activeNetwork.isConnected())
            editor.putInt(GlobalData.PREF_EVENT_WIFI_LAST_STATE, 1);
        else
        //if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED)
            editor.putInt(GlobalData.PREF_EVENT_WIFI_LAST_STATE, 0);
        //else
        //    editor.putInt(GlobalData.PREF_EVENT_WIFI_LAST_STATE, -1);
        editor.commit();
        */

        fillWifiConfigurationList(context);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    public static void setAlarm(Context context, boolean shortInterval, boolean forScreenOn)
    {
        //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context)
                == GlobalData.PREFERENCE_ALLOWED)
        {
            GlobalData.logE("WifiScanAlarmBroadcastReceiver.setAlarm","WifiHardware=true");

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);

            removeAlarm(context);

            Calendar calendar = Calendar.getInstance();

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

            int interval = GlobalData.applicationEventWifiScanInterval;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && GlobalData.applicationEventWifiScanInPowerSaveMode.equals("1"))
                interval = 2 * interval;

            if (shortInterval) {
                if (forScreenOn)
                    calendar.add(Calendar.SECOND, 2);
                else
                    calendar.add(Calendar.SECOND, 5);
            }
            else
                calendar.add(Calendar.MINUTE, interval);
            long alarmTime = calendar.getTimeInMillis();

            intent.putExtra(EXTRA_ONESHOT, 0);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm", "oneshot=" + oneshot + "; alarm is set");
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setAlarm", "alarm is set");

        }
        else
            GlobalData.logE("WifiScanAlarmBroadcastReceiver.setAlarm","WifiHardware=false");
    }

    public static void removeAlarm(Context context/*, boolean oneshot*/)
    {
        //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.removeAlarm","alarm not found");
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
            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","alarm found");
        else
            //GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm not found");
            GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.isAlarmSet","alarm not found");

        return (pendingIntent != null);
    }

    public static void lock(Context context)
    {
        //PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //if (android.os.Build.VERSION.SDK_INT >= 23)
        //    GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.lock","idleMode="+powerManager.isDeviceIdleMode());

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
            GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.lock","xxx");
        } catch(Exception e) {
            Log.e("WifiScanAlarmBroadcastReceiver.lock", "Error getting Lock: "+e.getMessage());
            GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.lock", "Error getting Lock: " + e.getMessage());
        }
    }
 
    public static void unlock()
    {
        //if ((wakeLock != null) && (wakeLock.isHeld()))
        //    wakeLock.release();
        if ((wifiLock != null) && (wifiLock.isHeld()))
            wifiLock.release();
        GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.unlock", "xxx");
    }
    
    public static void sendBroadcast(Context context)
    {
        Intent broadcastIntent = new Intent(context, WifiScanAlarmBroadcastReceiver.class);
        context.sendBroadcast(broadcastIntent);
    }
    
    static public boolean getScanRequest(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_WIFI_SCAN_REQUEST, false);
    }

    static public void setScanRequest(Context context, boolean scanRequest)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_WIFI_SCAN_REQUEST, scanRequest);
        editor.commit();
        GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setScanRequest","scanRequest="+scanRequest);
    }

    static public boolean getWaitForResults(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_WIFI_WAIT_FOR_RESULTS, false);
    }

    static public void setWaitForResults(Context context, boolean waitForResults)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_WIFI_WAIT_FOR_RESULTS, waitForResults);
        editor.commit();
        GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.setWaitForResults", "waitForResults=" + waitForResults);
    }

    static public void startScan(Context context)
    {
        lock(context); // lock wakeLock and wifiLock, then scan.
                    // unlock() is then called at the end of the onReceive function of WifiScanBroadcastReceiver
        boolean startScan = wifi.startScan();
        GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.startScan","scanStarted="+startScan);
        GlobalData.logE("$$$ WifiAP", "WifiScanAlarmBroadcastReceiver.startScan-startScan="+startScan);
        if (!startScan)
        {
            if (getWifiEnabledForScan(context))
            {
                GlobalData.logE("$$$ WifiScanAlarmBroadcastReceiver.startScan","disable wifi");
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
        scanServiceIntent.putExtra(GlobalData.EXTRA_SCANNER_TYPE, GlobalData.SCANNER_TYPE_WIFI);
        context.startService(scanServiceIntent);
    }

    /*
    static public void stopScan(Context context)
    {
        GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.stopScan","xxx");
        unlock();
        if (getWifiEnabledForScan(context))
            wifi.setWifiEnabled(false);
        setWifiEnabledForScan(context, false);
        setScanRequest(context, false);
        setWaitForResults(context, false);
        GlobalData.setForceOneWifiScan(context, false);
    }
    */

    static public boolean getWifiEnabledForScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_WIFI_ENABLED_FOR_SCAN, false);
    }

    static public void setWifiEnabledForScan(Context context, boolean setEnabled)
    {
        GlobalData.logE("@@@ WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan","setEnabled="+setEnabled);
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_WIFI_ENABLED_FOR_SCAN, setEnabled);
        editor.commit();
    }

    static public void fillWifiConfigurationList(Context context)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        List<WifiSSIDData> wifiConfigurationList = new ArrayList<WifiSSIDData>();

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
                    wifiConfigurationList.add(new WifiSSIDData(device.SSID, device.BSSID));
                }
            }
        }
        //saveWifiConfigurationList(context);
        saveWifiConfigurationList(context, wifiConfigurationList);
    }

    static public void fillScanResults(Context context)
    {
        //if (scanResults == null)
        //    scanResults = new ArrayList<WifiSSIDData>();

        List<WifiSSIDData> scanResults = new ArrayList<WifiSSIDData>();

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Permissions.checkLocation(context)) {
            List<ScanResult> _scanResults = wifi.getScanResults();
            if (_scanResults != null) {
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
                        scanResults.add(new WifiSSIDData(device.SSID, device.BSSID));
                    }
                }
            }
        }
        //saveScanResults(context);
        saveScanResults(context, scanResults);
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getWifiConfigurationList(Context context)
    public static List<WifiSSIDData> getWifiConfigurationList(Context context)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        //wifiConfigurationList.clear();

        List<WifiSSIDData> wifiConfigurationList = new ArrayList<WifiSSIDData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);

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

    //private static void saveWifiConfigurationList(Context context)
    private static void saveWifiConfigurationList(Context context, List<WifiSSIDData> wifiConfigurationList)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.WIFI_CONFIGURATION_LIST_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, wifiConfigurationList.size());

        Gson gson = new Gson();

        for (int i = 0; i < wifiConfigurationList.size(); i++) {
            String json = gson.toJson(wifiConfigurationList.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF+i, json);
        }

        editor.commit();
    }

    //public static void getScanResults(Context context)
    public static List<WifiSSIDData> getScanResults(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, -1);

        if (count > -1) {
            List<WifiSSIDData> scanResults = new ArrayList<WifiSSIDData>();

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    WifiSSIDData device = gson.fromJson(json, WifiSSIDData.class);
                    scanResults.add(device);
                }
            }
            return scanResults;
        }
        else
            return null;
    }

    //private static void saveScanResults(Context context)
    private static void saveScanResults(Context context, List<WifiSSIDData> scanResults)
    {
        //if (scanResults == null)
        //    scanResults = new ArrayList<WifiSSIDData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, scanResults.size());

        Gson gson = new Gson();

        for (int i = 0; i < scanResults.size(); i++)
        {
            String json = gson.toJson(scanResults.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF+i, json);
        }

        editor.commit();
    }

    public static void clearScanResults(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.putInt(SCAN_RESULT_COUNT_PREF, -1);

        editor.commit();
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
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%') || Wildcard.match(wifiInfoSSID, ssid2, '_', '%'));
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

        return SSID;
    }

    public static boolean compareSSID(WifiSSIDData result, String SSID, List<WifiSSIDData> wifiConfigurationList)
    {
        String wifiInfoSSID = getSSID(result, wifiConfigurationList);
        String ssid2 = "\"" + SSID + "\"";
        //return (getSSID(result).equals(SSID) || getSSID(result).equals(ssid2));
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%') || Wildcard.match(wifiInfoSSID, ssid2, '_', '%'));
    }

}
