package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class BluetoothScanWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "BluetoothScanJob";

    public static BluetoothAdapter bluetooth = null;

    private static List<BluetoothDeviceData> tmpScanLEResults = null;

    private static final String PREF_EVENT_BLUETOOTH_SCAN_REQUEST = "eventBluetoothScanRequest";
    private static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS = "eventBluetoothWaitForResults";
    private static final String PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST = "eventBluetoothLEScanRequest";
    private static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS = "eventBluetoothWaitForLEResults";
    private static final String PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN = "eventBluetoothEnabledForScan";
    private static final String PREF_EVENT_BLUETOOTH_SCAN_KILLED = "eventBluetoothScanKilled";

    public BluetoothScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("BluetoothScanWorker.doWork", "---------------------------------------- START");

            //CallsCounter.logCounter(context, "BluetoothScanWorker.doWork", "BluetoothScanWorker_doWork");

            if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed !=
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                cancelWork(context, false, null);
                //PPApplication.logE("BluetoothScanWorker.doWork", "---------------------------------------- END");
                return Result.success();
            }

            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
                //PPApplication.logE("BluetoothScanWorker.doWork", "update in power save mode is not allowed");
                cancelWork(context, false, null);
                //PPApplication.logE("BluetoothScanWorker.doWork", "---------------------------------------- START");
                return Result.success();
            }

            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (Event.getGlobalEventsRunning()) {
                //PPApplication.logE("BluetoothScanWorker.doWork", "start scanner");
                startScanner(context, false);
            }

            //PPApplication.logE("BluetoothScanWorker.doWork - handler", "schedule work");
            scheduleWork(context.getApplicationContext(), false, null, false/*, false*/);

            /*PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BluetoothScanWorker.doWork - handler", "schedule work");
                    scheduleWork(context, false, null, false);
                }
            }, 500);*/

            //PPApplication.logE("BluetoothScanWorker.doWork", "---------------------------------------- END");

            return Result.success();
        } catch (Exception e) {
            Log.e("BluetoothScanWorker.doWork", Log.getStackTraceString(e));
            Crashlytics.logException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    public void onStopped () {
        //PPApplication.logE("BluetoothScanWorker.onStopped", "xxx");

        //CallsCounter.logCounter(context, "BluetoothScanWorker.onStopped", "BluetoothScanWorker_onStopped");

        setScanRequest(context, false);
        setWaitForResults(context, false);
        setLEScanRequest(context, false);
        setWaitForLEResults(context, false);
        setScanKilled(context, false);
        WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
        WifiBluetoothScanner.setForceOneLEBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
    }

    private static void _scheduleWork(final Context context, final boolean shortInterval/*, final boolean forScreenOn*/) {
        try {
            WorkManager workManager = WorkManager.getInstance(context);

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("BluetoothScanWorker._scheduleWork", "---------------------------------------- START");
                PPApplication.logE("BluetoothScanWorker._scheduleWork", "shortInterval=" + shortInterval);
            }*/

            int interval = ApplicationPreferences.applicationEventBluetoothScanInterval;
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("1"))
                interval = 2 * interval;

            //PPApplication.logE("BluetoothScanWorker._scheduleWork", "interval=" + interval);

            if (!shortInterval) {
                //PPApplication.logE("BluetoothScanWorker._scheduleWork", "exact work");
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BluetoothScanWorker.class)
                        .setInitialDelay(interval, TimeUnit.MINUTES)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            } else {
                //PPApplication.logE("BluetoothScanWorker._scheduleWork", "start now work");
                waitForFinish(context);
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BluetoothScanWorker.class)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            }

            //PPApplication.logE("BluetoothScanWorker._scheduleWork", "---------------------------------------- END");
        } catch (Exception e) {
            Log.e("BluetoothScanWorker._scheduleWork", Log.getStackTraceString(e));
            Crashlytics.logException(e);
        }
    }

    static void scheduleWork(final Context context, final boolean useHandler, final Handler _handler, final boolean shortInterval/*, final boolean forScreenOn*/) {
        //PPApplication.logE("BluetoothScanWorker.scheduleJob", "shortInterval="+shortInterval);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (useHandler && (_handler == null)) {
                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _scheduleWork(context, shortInterval/*, forScreenOn*/);
                    }
                });
            }
            else {
                _scheduleWork(context, shortInterval/*, forScreenOn*/);
            }
        }
        //else
        //    PPApplication.logE("BluetoothScanWorker.scheduleJob","BluetoothHardware=false");
    }

    private static void _cancelWork(final Context context) {
        if (isWorkScheduled(context)) {
            try {
                waitForFinish(context);

                setScanRequest(context, false);
                setWaitForResults(context, false);
                setLEScanRequest(context, false);
                setWaitForLEResults(context, false);
                setScanKilled(context, true);
                WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                WifiBluetoothScanner.setForceOneLEBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                PhoneProfilesService.cancelWork(WORK_TAG, context);

                //PPApplication.logE("BluetoothScanWorker._cancelWork", "CANCELED");

            } catch (Exception e) {
                Log.e("BluetoothScanWorker._cancelWork", Log.getStackTraceString(e));
                Crashlytics.logException(e);
            }
        }
    }

    private static void waitForFinish(Context context) {
        if (!isWorkRunning(context)) {
            //PPApplication.logE("BluetoothScanWorker.waitForFinish", "NOT RUNNING");
            return;
        }

        try {
            WorkManager workManager = WorkManager.getInstance(context);

            //PPApplication.logE("BluetoothScanWorker.waitForFinish", "START WAIT FOR FINISH");
            long start = SystemClock.uptimeMillis();
            do {

                ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(WORK_TAG);
                boolean allFinished = true;
                //noinspection TryWithIdenticalCatches
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        if (!state.isFinished()) {
                            allFinished = false;
                            break;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (allFinished) {
                    //PPApplication.logE("BluetoothScanWorker.waitForFinish", "FINISHED");
                    break;
                }

                //try { Thread.sleep(100); } catch (InterruptedException e) { }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() - start < WifiBluetoothScanner.classicBTScanDuration * 1000);

            //PPApplication.logE("BluetoothScanWorker.waitForFinish", "END WAIT FOR FINISH");
        } catch (Exception e) {
            Log.e("BluetoothScanWorker.waitForFinish", Log.getStackTraceString(e));
            Crashlytics.logException(e);
        }
    }

    static void cancelWork(final Context context, final boolean useHandler, final Handler _handler) {
        //PPApplication.logE("BluetoothScanWorker.cancelWork", "xxx");

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _cancelWork(context);
                }
            });
        }
        else {
            _cancelWork(context);
        }
    }

    private static boolean isWorkRunning(Context context) {
        try {
            WorkManager instance = WorkManager.getInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
                //PPApplication.logE("BluetoothScanWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                //return workInfoList.size() != 0;
                boolean running = false;
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    running = state == WorkInfo.State.RUNNING;
                }
                return running;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e("BluetoothScanWorker.isWorkRunning", Log.getStackTraceString(e));
            Crashlytics.logException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(Context context) {
        //PPApplication.logE("BluetoothScanWorker.isWorkScheduled", "xxx");
        try {
            WorkManager instance = WorkManager.getInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
                //PPApplication.logE("BluetoothScanWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                //return workInfoList.size() != 0;
                boolean running = false;
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
                }
                return running;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e("BluetoothScanWorker.isWorkScheduled", Log.getStackTraceString(e));
            Crashlytics.logException(e);
            return false;
        }
    }

    //------------------------------------------------------------

    /*
    static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothAdapter adapter;
        //if (android.os.Build.VERSION.SDK_INT < 18)
        //    adapter = BluetoothAdapter.getDefaultAdapter();
        //else {
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            adapter = bluetoothManager.getAdapter();
        else
            adapter = null;
        //}
        return adapter;
    }
    */

    public static void initialize(Context context, boolean clearScanResult)
    {
        setScanRequest(context, false);
        setLEScanRequest(context, false);
        setWaitForResults(context, false);
        setWaitForLEResults(context, false);
        setScanKilled(context, false);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed !=
                PreferenceAllowed.PREFERENCE_ALLOWED)
            return;

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);
        if (bluetooth == null)
            return;

        if (clearScanResult)
            clearScanResults(context);

        /*SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PPApplication.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
        editor.commit();*/

        if (bluetooth.isEnabled())
        {
            fillBoundedDevicesList(context);
        }

    }

    static void getScanRequest(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothScanRequest = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, false);
        }
    }
    static void setScanRequest(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothScanRequest = startScan;
        }
    }

    static void getLEScanRequest(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                ApplicationPreferences.prefEventBluetoothLEScanRequest = ApplicationPreferences.
                        getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, false);
            } else
                ApplicationPreferences.prefEventBluetoothLEScanRequest = false;
        }
    }
    static void setLEScanRequest(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putBoolean(PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, startScan);
                editor.apply();
                ApplicationPreferences.prefEventBluetoothLEScanRequest = startScan;
            }
        }
    }

    static void getWaitForResults(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothWaitForResult = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, false);
        }
    }
    static void setWaitForResults(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothWaitForResult = startScan;
        }
    }

    static void getWaitForLEResults(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                ApplicationPreferences.prefEventBluetoothLEWaitForResult = ApplicationPreferences.
                        getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, false);
            } else
                ApplicationPreferences.prefEventBluetoothLEWaitForResult = false;
        }
    }
    static void setWaitForLEResults(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, startScan);
                editor.apply();
                ApplicationPreferences.prefEventBluetoothLEWaitForResult = startScan;
            }
        }
    }

    static void getScanKilled(Context context) {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothScanKilled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_SCAN_KILLED, false);
        }
    }
    static void setScanKilled(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_SCAN_KILLED, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothScanKilled = startScan;
        }
    }

    static void startCLScan(Context context)
    {
        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

        if (bluetooth != null) {
            if (bluetooth.isDiscovering())
                bluetooth.cancelDiscovery();

            WifiBluetoothScanner.bluetoothDiscoveryStarted = false;

            if (Permissions.checkLocation(context)) {
                boolean startScan = bluetooth.startDiscovery();
                //PPApplication.logE("BluetoothScanWorker.startCLScan", "scanStarted=" + startScan);

                if (!startScan) {
                    if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                        //PPApplication.logE("BluetoothScanWorker.startCLScan", "disable bluetooth");
                        if (Permissions.checkBluetoothForEMUI(context))
                            //if (Build.VERSION.SDK_INT >= 26)
                            //    CmdBluetooth.setBluetooth(false);
                            //else
                                bluetooth.disable();
                    }
                }
                setWaitForResults(context, startScan);
            }
            setScanRequest(context, false);
        }
    }

    static void stopCLScan() {
        //PPApplication.logE("BluetoothScanWorker.stopCLScan", "xxx");
        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);
        if (bluetooth != null) {
            if (bluetooth.isDiscovering()) {
                bluetooth.cancelDiscovery();
                //PPApplication.logE("BluetoothScanWorker.stopCLScan", "stopped");
            }
        }
    }

    @SuppressLint("NewApi")
    static void startLEScan(final Context context)
    {
        //PPApplication.logE("BluetoothScanWorker.startLEScan", "xxx");

        if (WifiBluetoothScanner.bluetoothLESupported(context)) {

            synchronized (PPApplication.bluetoothLEScanMutex) {

                if (bluetooth == null)
                    bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

                if (bluetooth != null) {
                    if (Permissions.checkLocation(context)) {
                        try {
                            if (WifiBluetoothScanner.bluetoothLEScanner == null)
                                WifiBluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();

                            ScanSettings.Builder builder = new ScanSettings.Builder();

                            tmpScanLEResults = null;

                            int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                            if (forceScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)
                                builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                            else
                                builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

                            if (bluetooth.isOffloadedScanBatchingSupported())
                                builder.setReportDelay(ApplicationPreferences.applicationEventBluetoothLEScanDuration * 1000);
                            ScanSettings settings = builder.build();

                            List<ScanFilter> filters = new ArrayList<>();

                            WifiBluetoothScanner.bluetoothLEScanner.startScan(filters, settings, new BluetoothLEScanCallback21(context));
                            setWaitForLEResults(context, true);

                            //PPApplication.logE("BluetoothScanWorker.startLEScan", "scanStarted=" + startScan);
                        } catch (Exception ignored) {
                        }
                    }
                    setLEScanRequest(context, false);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    static void stopLEScan(final Context context) {
        //PPApplication.logE("BluetoothScanWorker.stopLEScan", "xxx");
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {
            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (bluetooth != null) {
                if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                    try {
                        //if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                        //PPApplication.logE("BluetoothScanWorker.stopLEScan", "WifiBluetoothScanner.bluetoothLEScanner="+WifiBluetoothScanner.bluetoothLEScanner);
                        if (WifiBluetoothScanner.bluetoothLEScanner == null)
                            WifiBluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();

                        //PPApplication.logE("BluetoothScanWorker.stopLEScan", "WifiBluetoothScanner.bluetoothLEScanner="+WifiBluetoothScanner.bluetoothLEScanner);

                        WifiBluetoothScanner.bluetoothLEScanner.stopScan(new BluetoothLEScanCallback21(context));

                        //PPApplication.logE("BluetoothScanWorker.stopLEScan", "stopped");
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    static void finishLEScan(Context context) {
        synchronized (PPApplication.bluetoothLEScanMutex) {
            //PPApplication.logE("BluetoothScanWorker.finishLEScan", "xxx");

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            if (tmpScanLEResults != null) {
                //PPApplication.logE("BluetoothScanWorker.finishLEScan", "tmpScanLEResults.size="+tmpScanLEResults.size());
                for (BluetoothDeviceData device : tmpScanLEResults) {
                    scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                    //PPApplication.logE("BluetoothScanWorker.finishLEScan", "device="+device.getName());
                }
                //tmpScanLEResults = null;
            }
            //else
            //    PPApplication.logE("BluetoothScanWorker.finishLEScan", "tmpScanLEResults=null");

            saveLEScanResults(context, scanResults);
        }
    }

    static void startScanner(Context context, boolean fromDialog)
    {
        //PPApplication.logE("$$$ BluetoothScanWorker.startScanner", "xxx");
        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        if (fromDialog || ApplicationPreferences.applicationEventBluetoothEnableScanning) {
            setScanKilled(context, false);
            if (fromDialog) {
                setScanRequest(context, true);
                setLEScanRequest(context, true);
            }

            //PPApplication.logE("$$$ BluetoothScanWorker.startScanner", "scanning enabled");
            WifiBluetoothScanner wifiBluetoothScanner = new WifiBluetoothScanner(context);
            wifiBluetoothScanner.doScan(WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
        }
        //dataWrapper.invalidateDataWrapper();
    }

    /*
    static public void stopScan(Context context)
    {
        unlock();
        if (getBluetoothEnabledForScan(context)) {
            if (Build.VERSION.SDK_INT >= 26)
                CmdBluetooth.setBluetooth(false);
            else
                bluetooth.disable();
        }
        setBluetoothEnabledForScan(context, false);
        setScanRequest(context, false);
        setWaitForResults(context, false);
        PPApplication.setForceOneBluetoothScan(context, false);
    }
    */

    static void getBluetoothEnabledForScan(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothEnabledForScan = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, false);
        }
    }

    static void setBluetoothEnabledForScan(Context context, boolean setEnabled)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, setEnabled);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothEnabledForScan = setEnabled;
        }
    }

    static int getBluetoothType(BluetoothDevice device) {
        //if (android.os.Build.VERSION.SDK_INT >= 18)
        return device.getType();
        //else
        //    return 1; // BluetoothDevice.DEVICE_TYPE_CLASSIC
    }

    static void fillBoundedDevicesList(Context context)
    {
        //if (boundedDevicesList == null)
        //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

        List<BluetoothDeviceData> boundedDevicesList  = new ArrayList<>();

        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

        if (bluetooth != null) {
            if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH)) {
                Set<BluetoothDevice> boundedDevices = bluetooth.getBondedDevices();
                boundedDevicesList.clear();
                if (boundedDevices != null) {
                    for (BluetoothDevice device : boundedDevices) {
                        boundedDevicesList.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                                getBluetoothType(device), false, 0, true, false));
                    }
                }
                saveBoundedDevicesList(context, boundedDevicesList);
            }
        }
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getBoundedDevicesList(Context context)
    static List<BluetoothDeviceData> getBoundedDevicesList(Context context)
    {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            //if (boundedDevicesList == null)
            //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

            //boundedDevicesList.clear();

            List<BluetoothDeviceData> boundedDevicesList = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                    device.configured = true;
                    boundedDevicesList.add(device);
                }
            }

            return boundedDevicesList;
        }
    }

    private static void saveBoundedDevicesList(Context context, List<BluetoothDeviceData> boundedDevicesList)
    {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            //if (boundedDevicesList == null)
            //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(SCAN_RESULT_COUNT_PREF, boundedDevicesList.size());

            Gson gson = new Gson();

            for (int i = 0; i < boundedDevicesList.size(); i++) {
                String json = gson.toJson(boundedDevicesList.get(i));
                editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
            }

            editor.apply();
        }
    }

    static List<BluetoothDeviceData> getScanResults(Context context)
    {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, -1);

            if (count >= 0) {
                Gson gson = new Gson();
                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                    if (!json.isEmpty()) {
                        BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                        device.scanned = true;
                        scanResults.add(device);
                    }
                }
            }

            preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            count = preferences.getInt(SCAN_RESULT_COUNT_PREF, -1);

            if (count >= 0) {
                Gson gson = new Gson();
                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                    if (!json.isEmpty()) {
                        BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                        device.scanned = true;
                        scanResults.add(device);
                    }
                }
            }

            if (scanResults.size() == 0)
                return null;
            else
                return scanResults;
        }
    }

    private static void clearScanResults(Context context) {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();
            editor.putInt(SCAN_RESULT_COUNT_PREF, -1);

            editor.apply();

            preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            editor = preferences.edit();

            editor.clear();
            editor.putInt(SCAN_RESULT_COUNT_PREF, -1);

            editor.apply();
        }
    }

    private static void saveCLScanResults(Context context, List<BluetoothDeviceData> scanResults)
    {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(SCAN_RESULT_COUNT_PREF, scanResults.size());

            Gson gson = new Gson();
            for (int i = 0; i < scanResults.size(); i++) {
                String json = gson.toJson(scanResults.get(i));
                editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
            }

            editor.apply();
        }
    }

    private static void saveLEScanResults(Context context, List<BluetoothDeviceData> scanResults)
    {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(SCAN_RESULT_COUNT_PREF, scanResults.size());

            Gson gson = new Gson();
            for (int i = 0; i < scanResults.size(); i++) {
                String json = gson.toJson(scanResults.get(i));
                editor.putString(SCAN_RESULT_DEVICE_PREF + i, json);
            }

            editor.apply();
        }
    }

    /*
    public static void addScanResult(Context context, BluetoothDeviceData device) {
        List<BluetoothDeviceData> savedScanResults = getScanResults(context);
        if (savedScanResults == null)
            savedScanResults = new ArrayList<>();

        boolean found = false;
        for (BluetoothDeviceData _device : savedScanResults) {

            if (_device.address.equals(device.address)) {
                found = true;
                break;
            }
        }
        if (!found) {
            savedScanResults.add(new BluetoothDeviceData(device.name, device.address, device.type, false));
        }

        SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
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
    */

    static void addLEScanResult(BluetoothDeviceData device) {
        synchronized (PPApplication.bluetoothLEScanMutex) {
            if (tmpScanLEResults == null)
                tmpScanLEResults = new ArrayList<>();

            boolean found = false;
            for (BluetoothDeviceData _device : tmpScanLEResults) {
                if (_device.address.equals(device.address)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (BluetoothDeviceData _device : tmpScanLEResults) {
                    if (_device.getName().equalsIgnoreCase(device.getName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (tmpScanLEResults != null) // maybe set to null by startLEScan() or finishLEScan()
                    tmpScanLEResults.add(new BluetoothDeviceData(device.name, device.address, device.type, false, 0, false, true));
            }
        }
    }

    static void finishCLScan(final Context context) {
        synchronized (PPApplication.bluetoothScanMutex) {
            //PPApplication.logE("BluetoothScanWorker.finishCLScan", "BluetoothScanBroadcastReceiver: discoveryStarted=" + WifiBluetoothScanner.bluetoothDiscoveryStarted);

            if (WifiBluetoothScanner.bluetoothDiscoveryStarted) {

                WifiBluetoothScanner.bluetoothDiscoveryStarted = false;

                List<BluetoothDeviceData> scanResults = new ArrayList<>();

                if (WifiBluetoothScanner.tmpBluetoothScanResults != null) {
                    //PPApplication.logE("BluetoothScanWorker.finishCLScan", "WifiBluetoothScanner.tmpBluetoothScanResults.size="+WifiBluetoothScanner.tmpBluetoothScanResults.size());
                    for (BluetoothDeviceData device : WifiBluetoothScanner.tmpBluetoothScanResults) {
                        scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                        //PPApplication.logE("BluetoothScanWorker.finishCLScan", "device="+device.getName());
                    }
                }
                //else
                //    PPApplication.logE("BluetoothScanWorker.finishCLScan", "tmpBluetoothScanResults=null");

                saveCLScanResults(context, scanResults);

                setWaitForResults(context, false);

                int forceOneScan = ApplicationPreferences.prefForceOneBluetoothScan;
                WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    Data workData = new Data.Builder()
                            .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_HANDLE_EVENTS)
                            .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                    .addTag("handleEventsBluetoothCLScannerWork")
                                    .setInputData(workData)
                                    .setInitialDelay(5, TimeUnit.SECONDS)
                                    .build();
                    try {
                        WorkManager workManager = WorkManager.getInstance(context);
                        workManager.enqueueUniqueWork("handleEventsBluetoothCLScannerWork", ExistingWorkPolicy.REPLACE, worker);
                    } catch (Exception ignored) {}

                    /*PPApplication.startHandlerThread("BluetoothScanWorker.finishCLScan");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothScanWorker_finishCLScan");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                // start events handler
                                EventsHandler eventsHandler = new EventsHandler(context);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }, 5000);*/
                    //PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, 5, context);
                }

                WifiBluetoothScanner.tmpBluetoothScanResults = null;
            }
        }
    }

}
