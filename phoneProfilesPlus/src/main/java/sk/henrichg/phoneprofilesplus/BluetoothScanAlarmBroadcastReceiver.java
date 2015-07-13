package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class BluetoothScanAlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScanAlarm";
    public static final String EXTRA_ONESHOT = "oneshot";

    public static BluetoothAdapter bluetooth = null;
    private static PowerManager.WakeLock wakeLock = null;

    public static List<BluetoothDeviceData> tmpScanResults = null;
    public static List<BluetoothDeviceData> scanResults = null;
    public static List<BluetoothDeviceData> boundedDevicesList = null;

    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("#### BluetoothScanAlarmBroadcastReceiver.onReceive", "xxx");

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19)) {
            int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
            if (oneshot > -1)
                setAlarm(context, oneshot == 1);
        }

        if (scanResults == null)
            scanResults = new ArrayList<BluetoothDeviceData>();

        if (boundedDevicesList == null)
            boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) !=
                GlobalData.HARDWARE_CHECK_ALLOWED) {
            removeAlarm(context, false);
            removeAlarm(context, true);
            return;
        }

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        // disabled for firstStartEvents
        //if (!GlobalData.getApplicationStarted(context))
            // application is not started
        //	return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive", "xxx");

            boolean bluetoothEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.onReceive", "bluetoothEventsExists=" + bluetoothEventsExists);

            if (bluetoothEventsExists || GlobalData.getForceOneBluetoothScan(context))
            {
                startScanner(context);
            }
            else {
                removeAlarm(context, false);
                removeAlarm(context, true);
            }

            dataWrapper.invalidateDataWrapper();
        }

    }

    public static void initialize(Context context)
    {
        setScanRequest(context, false);
        setWaitForResults(context, false);
        setBluetoothEnabledForScan(context, false);

        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) !=
                GlobalData.HARDWARE_CHECK_ALLOWED)
            return;

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        unlock();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(GlobalData.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
        editor.commit();

        if (bluetooth.isEnabled())
        {
            fillBoundedDevicesList(context);
        }

    }

    @SuppressLint("NewApi")
    public static void setAlarm(Context context, boolean oneshot)
    {
        GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot);

        if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context)
                == GlobalData.HARDWARE_CHECK_ALLOWED)
        {
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=true");

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);

            if (oneshot)
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
                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                else
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            }
            else
            {
                removeAlarm(context, false);

                Calendar calendar = Calendar.getInstance();

                //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                //GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19)) {
                    calendar.add(Calendar.MINUTE, GlobalData.applicationEventBluetoothScanInterval);
                    long alarmTime = calendar.getTimeInMillis();

                    intent.putExtra(EXTRA_ONESHOT, 0);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                }
                else {
                    calendar.add(Calendar.SECOND, 10);
                    long alarmTime = calendar.getTimeInMillis();

                    intent.putExtra(EXTRA_ONESHOT, 0);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                            alarmTime,
                            GlobalData.applicationEventBluetoothScanInterval * 60 * 1000,
                            alarmIntent);
                }

            }

            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","alarm is set");

        }
        else
            GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=false");
    }

    public static void removeAlarm(Context context, boolean oneshot)
    {
        GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        if (oneshot)
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
    }

    public static boolean isAlarmSet(Context context, boolean oneshot)
    {
        Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        if (oneshot)
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
        else
            GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm not found");

        return (pendingIntent != null);
    }

    public static void lock(Context context)
    {
         // initialise the locks
        if (wakeLock == null)
            wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothScanWakeLock");

        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
        //	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.lock","xxx");
        } catch(Exception e) {
            Log.e("BluetoothScanAlarmBroadcastReceiver.lock", "Error getting Lock: "+e.getMessage());
        }
    }
 
    public static void unlock()
    {
        if ((wakeLock != null) && (wakeLock.isHeld()))
            wakeLock.release();
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

    static public void startScan(Context context)
    {
        initTmpScanResults();
        lock(context); // lock wakeLock, then scan.
                    // unlock() is then called at the end of the onReceive function of BluetoothScanBroadcastReceiver
        boolean startScan = bluetooth.startDiscovery();
        GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","scanStarted="+startScan);
        if (!startScan)
        {
            unlock();
            if (getBluetoothEnabledForScan(context))
            {
                GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","disable bluetooth");
                bluetooth.disable();
            }
        }
        setWaitForResults(context, startScan);
        setScanRequest(context, false);
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

    static public void initTmpScanResults()
    {
        if (tmpScanResults != null)
            tmpScanResults.clear();
        else
            tmpScanResults = new ArrayList<BluetoothDeviceData>();
    }

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

    static public void fillBoundedDevicesList(Context context)
    {
        if (boundedDevicesList == null)
            boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> boundedDevices = BluetoothScanAlarmBroadcastReceiver.bluetooth.getBondedDevices();
        boundedDevicesList.clear();
        for (BluetoothDevice device : boundedDevices)
        {
            boundedDevicesList.add(new BluetoothDeviceData(device.getName(), device.getAddress()));
        }
        saveBoundedDevicesList(context);
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    public static void getBoundedDevicesList(Context context)
    {
        if (boundedDevicesList == null)
            boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        boundedDevicesList.clear();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);

        int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++)
        {
            String json = preferences.getString(SCAN_RESULT_DEVICE_PREF+i, "");
            if (!json.isEmpty()) {
                BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                boundedDevicesList.add(device);
            }
        }

    }

    private static void saveBoundedDevicesList(Context context)
    {
        if (boundedDevicesList == null)
            boundedDevicesList = new ArrayList<BluetoothDeviceData>();

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

    public static void getScanResults(Context context)
    {
        if (scanResults == null)
            scanResults = new ArrayList<BluetoothDeviceData>();

        scanResults.clear();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);

        int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

        Gson gson = new Gson();

        for (int i = 0; i < count; i++)
        {
            String json = preferences.getString(SCAN_RESULT_DEVICE_PREF+i, "");
            if (!json.isEmpty()) {
                BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                scanResults.add(device);
            }
        }

    }

    public static void saveScanResults(Context context)
    {
        if (scanResults == null)
            scanResults = new ArrayList<BluetoothDeviceData>();

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
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


}
