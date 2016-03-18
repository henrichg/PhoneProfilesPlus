package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class BluetoothScanAlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScanAlarm";
    public static final String EXTRA_ONESHOT = "oneshot";

    public static BluetoothAdapter bluetooth = null;
    //private static PowerManager.WakeLock wakeLock = null;

    //public static List<BluetoothDeviceData> scanResults = null;
    //public static List<BluetoothDeviceData> boundedDevicesList = null;

    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### BluetoothScanAlarmBroadcastReceiver.onReceive", "xxx");

        GlobalData.loadPreferences(context);

        //int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
        //if (oneshot == 0)
            setAlarm(context, /*false,*/ false);

        //if (scanResults == null)
        //    scanResults = new ArrayList<BluetoothDeviceData>();

        //if (boundedDevicesList == null)
        //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) !=
                GlobalData.PREFERENCE_ALLOWED) {
            removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return;
        }

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        // disabled for firstStartEvents
        //if (!GlobalData.getApplicationStarted(context))
            // application is not started
        //	return;

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive", "xxx");

            // moved to ScannerService
            /*boolean bluetoothEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.onReceive", "bluetoothEventsExists=" + bluetoothEventsExists);

            int forceScan = GlobalData.getForceOneBluetoothScan(dataWrapper.context);
            int forceScanLE = GlobalData.getForceOneLEBluetoothScan(context);
            boolean scan = (bluetoothEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED) || (forceScanLE == GlobalData.FORCE_ONE_SCAN_ENABLED));
            if ((!bluetoothEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                scan = false;
            if ((!bluetoothEventsExists) && (forceScanLE == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
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
        setLEScanRequest(context, false);
        setWaitForResults(context, false);
        setWaitForLEResults(context, false);
        setBluetoothEnabledForScan(context, false);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) !=
                GlobalData.PREFERENCE_ALLOWED)
            return;

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null)
            return;

        unlock();

        /*SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(GlobalData.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
        editor.commit();*/

        if (bluetooth.isEnabled())
        {
            fillBoundedDevicesList(context);
        }

    }

    @SuppressLint("NewApi")
    public static void setAlarm(Context context, /*boolean oneshot,*/ boolean shortInterval)
    {
        //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm", "oneshot=" + oneshot);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context)
                == GlobalData.PREFERENCE_ALLOWED)
        {
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=true");

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);

            /*if (oneshot)
            {
                removeAlarm(context, true);

                Calendar calendar = Calendar.getInstance();
                //calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 2);

                long alarmTime = calendar.getTimeInMillis();

                //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

                intent.putExtra(EXTRA_ONESHOT, 1);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                else
                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                else
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

                GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS);
                GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS);

            }
            else
            {*/
                removeAlarm(context/*, false*/);

                Calendar calendar = Calendar.getInstance();

                //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

                int interval = GlobalData.applicationEventBluetoothScanInterval;
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                if (isPowerSaveMode && GlobalData.applicationEventBluetoothScanInPowerSaveMode.equals("1"))
                    interval = 2 * interval;

                if (shortInterval)
                    calendar.add(Calendar.SECOND, 10);
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
            //}

            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm", "alarm is set");

        }
        else
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=false");
    }

    public static void removeAlarm(Context context/*, boolean oneshot*/)
    {
        //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm", "oneshot=" + oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","alarm not found");
    }

    public static boolean isAlarmSet(Context context/*, boolean oneshot*/)
    {
        Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
            //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","alarm found");
        else
            //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet", "oneshot=" + oneshot + "; alarm not found");
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet", "alarm not found");

        return (pendingIntent != null);
    }

    public static void lock(Context context)
    {
         // initialise the locks - moved to ScannerService
        /*if (wakeLock == null)
            wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothScanWakeLock");

        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
        //	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.lock","xxx");
        } catch(Exception e) {
            Log.e("BluetoothScanAlarmBroadcastReceiver.lock", "Error getting Lock: "+e.getMessage());
        }*/
    }
 
    public static void unlock()
    {
        /*if ((wakeLock != null) && (wakeLock.isHeld()))
            wakeLock.release();*/
        //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.unlock","xxx");
    }
    
    public static void sendBroadcast(Context context)
    {
        Intent broadcastIntent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
        context.sendBroadcast(broadcastIntent);
    }
    
    static public boolean getScanRequest(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_SCAN_REQUEST, false);
    }

    static public void setScanRequest(Context context, boolean startScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_SCAN_REQUEST, startScan);
        editor.commit();
    }

    static public boolean getLEScanRequest(Context context)
    {
        if (ScannerService.bluetoothLESupported(context)) {
            SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, false);
        }
        else
            return false;
    }

    static public void setLEScanRequest(Context context, boolean startScan)
    {
        if (ScannerService.bluetoothLESupported(context)) {
            SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, startScan);
            editor.commit();
        }
    }

    static public boolean getWaitForResults(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, false);
    }

    static public void setWaitForResults(Context context, boolean startScan)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, startScan);
        editor.commit();
    }

    static public boolean getWaitForLEResults(Context context)
    {
        if (ScannerService.bluetoothLESupported(context)) {
            SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, false);
        }
        else
            return false;
    }

    static public void setWaitForLEResults(Context context, boolean startScan)
    {
        if (ScannerService.bluetoothLESupported(context)) {
            SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, startScan);
            editor.commit();
        }
    }

    static public void startScan(Context context)
    {
        BluetoothScanBroadcastReceiver.initTmpScanResults();

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth.isDiscovering())
            bluetooth.cancelDiscovery();

        BluetoothScanBroadcastReceiver.discoveryStarted = false;

        if (Permissions.checkLocation(context)) {
            lock(context); // lock wakeLock, then scan.
            // unlock() is then called at the end of the scan from ScannerService

            boolean startScan = bluetooth.startDiscovery();
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.startScan", "scanStarted=" + startScan);

            if (!startScan) {
                unlock();
                if (getBluetoothEnabledForScan(context)) {
                    GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.startScan", "disable bluetooth");
                    bluetooth.disable();
                }
            }
            setWaitForResults(context, startScan);
        }
        setScanRequest(context, false);
    }

    static public void stopScan(Context context) {
        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth.isDiscovering())
            bluetooth.cancelDiscovery();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    static public void startLEScan(Context context)
    {
        if (ScannerService.bluetoothLESupported(context)) {

            //BluetoothScanBroadcastReceiver.initTmpScanResults();

            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter();

            if (Permissions.checkLocation(context)) {
                if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                    if (ScannerService.leScanner == null)
                        ScannerService.leScanner = bluetooth.getBluetoothLeScanner();
                    if (ScannerService.leScanCallback21 == null)
                        ScannerService.leScanCallback21 = new BluetoothLEScanCallback21(context);

                    //ScannerService.leScanner.stopScan(ScannerService.leScanCallback21);

                    lock(context); // lock wakeLock, then scan.
                    // unlock() is then called at the end of the scan from ScannerService

                    ScanSettings.Builder builder = new ScanSettings.Builder();

                    int forceScan = GlobalData.getForceOneBluetoothScan(context);
                    if ((forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED) || (forceScan == GlobalData.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
                        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    else
                        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

                    if (bluetooth.isOffloadedScanBatchingSupported())
                        builder.setReportDelay(GlobalData.applicationEventBluetoothLEScanDuration * 1000);
                    ScanSettings settings = builder.build();

                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
                    ScannerService.leScanner.startScan(filters, settings, ScannerService.leScanCallback21);
                }
                else {
                    if (ScannerService.leScanCallback18 == null)
                        ScannerService.leScanCallback18 = new BluetoothLEScanCallback18(context);

                    //bluetooth.stopLeScan(ScannerService.leScanCallback18);

                    lock(context); // lock wakeLock, then scan.
                    // unlock() is then called at the end of the scan from ScannerService

                    boolean startScan = bluetooth.startLeScan(ScannerService.leScanCallback18);

                    if (!startScan) {
                        unlock();
                        if (getBluetoothEnabledForScan(context)) {
                            bluetooth.disable();
                        }
                    }
                }

                setWaitForLEResults(context, true); //startScan);
            }
            setLEScanRequest(context, false);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    static public void stopLEScan(Context context) {
        if (ScannerService.bluetoothLESupported(context)) {
            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter();

            if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                    if (ScannerService.leScanner == null)
                        ScannerService.leScanner = bluetooth.getBluetoothLeScanner();
                    if (ScannerService.leScanCallback21 == null)
                        ScannerService.leScanCallback21 = new BluetoothLEScanCallback21(context);
                    ScannerService.leScanner.stopScan(ScannerService.leScanCallback21);
                } else {
                    if (ScannerService.leScanCallback18 == null)
                        ScannerService.leScanCallback18 = new BluetoothLEScanCallback18(context);
                    bluetooth.stopLeScan(ScannerService.leScanCallback18);
                }
            }
        }
    }

    static public void startScanner(Context context)
    {
        Intent scanServiceIntent = new Intent(context, ScannerService.class);
        scanServiceIntent.putExtra(GlobalData.EXTRA_SCANNER_TYPE, GlobalData.SCANNER_TYPE_BLUETOOTH);
        context.startService(scanServiceIntent);
    }

    /*
    static public void stopScan(Context context)
    {
        unlock();
        if (getBluetoothEnabledForScan(context))
            bluetooth.disable();
        setBluetoothEnabledForScan(context, false);
        setScanRequest(context, false);
        setWaitForResults(context, false);
        GlobalData.setForceOneBluetoothScan(context, false);
    }
    */

    static public boolean getBluetoothEnabledForScan(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, false);
    }

    static public void setBluetoothEnabledForScan(Context context, boolean setEnabled)
    {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, setEnabled);
        editor.commit();
    }

    static public int getBluetoothType(BluetoothDevice device) {
        if (android.os.Build.VERSION.SDK_INT >= 18)
            return device.getType();
        else
            return 1; // BluetoothDevice.DEVICE_TYPE_CLASSIC
    }

    static public void fillBoundedDevicesList(Context context)
    {
        //if (boundedDevicesList == null)
        //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        List<BluetoothDeviceData> boundedDevicesList  = new ArrayList<BluetoothDeviceData>();

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> boundedDevices = BluetoothScanAlarmBroadcastReceiver.bluetooth.getBondedDevices();
        boundedDevicesList.clear();
        for (BluetoothDevice device : boundedDevices)
        {
            boundedDevicesList.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                    BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device)));
        }
        saveBoundedDevicesList(context, boundedDevicesList);
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getBoundedDevicesList(Context context)
    public static List<BluetoothDeviceData> getBoundedDevicesList(Context context)
    {
        //if (boundedDevicesList == null)
        //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        //boundedDevicesList.clear();

        List<BluetoothDeviceData> boundedDevicesList  = new ArrayList<BluetoothDeviceData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);

        int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++)
        {
            String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
            if (!json.isEmpty()) {
                BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                boundedDevicesList.add(device);
            }
        }

        return boundedDevicesList;
    }

    private static void saveBoundedDevicesList(Context context, List<BluetoothDeviceData> boundedDevicesList)
    {
        //if (boundedDevicesList == null)
        //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, boundedDevicesList.size());

        Gson gson = new Gson();

        for (int i = 0; i < boundedDevicesList.size(); i++)
        {
            String json = gson.toJson(boundedDevicesList.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF+i, json);
        }

        editor.commit();
    }

    //public static void getScanResults(Context context)
    public static List<BluetoothDeviceData> getScanResults(Context context)
    {
        //if (scanResults == null)
        //    scanResults = new ArrayList<BluetoothDeviceData>();

        //scanResults.clear();

        List<BluetoothDeviceData> scanResults = new ArrayList<BluetoothDeviceData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);

        int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++)
        {
            String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
            if (!json.isEmpty()) {
                BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                scanResults.add(device);
            }
        }

        return scanResults;
    }

    public static void clearScanResults(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.putInt(SCAN_RESULT_COUNT_PREF, 0);

        editor.commit();
    }

    public static void saveScanResults(Context context, List<BluetoothDeviceData> scanResults)
    {
        List<BluetoothDeviceData> savedScanResults = getScanResults(context);

        for (BluetoothDeviceData device : scanResults) {
            boolean found = false;
            for (BluetoothDeviceData _device : savedScanResults) {

                if (_device.address.equals(device.address)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                savedScanResults.add(new BluetoothDeviceData(device.name, device.address, device.type));
            }
        }

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, savedScanResults.size());

        Gson gson = new Gson();
        for (int i = 0; i < savedScanResults.size(); i++)
        {
            String json = gson.toJson(savedScanResults.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF+i, json);
        }

        editor.commit();
    }

    public static void addScanResult(Context context, BluetoothDeviceData device) {
        List<BluetoothDeviceData> savedScanResults = getScanResults(context);

        boolean found = false;
        for (BluetoothDeviceData _device : savedScanResults) {

            if (_device.address.equals(device.address)) {
                found = true;
                break;
            }
        }
        if (!found) {
            savedScanResults.add(new BluetoothDeviceData(device.name, device.address, device.type));
        }

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();

        editor.putInt(SCAN_RESULT_COUNT_PREF, savedScanResults.size());

        Gson gson = new Gson();
        for (int i = 0; i < savedScanResults.size(); i++)
        {
            String json = gson.toJson(savedScanResults.get(i));
            editor.putString(SCAN_RESULT_DEVICE_PREF+i, json);
        }

        editor.commit();
    }

}
