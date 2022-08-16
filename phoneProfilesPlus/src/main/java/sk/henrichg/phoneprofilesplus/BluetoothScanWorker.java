package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressLint("MissingPermission")
public class BluetoothScanWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "BluetoothScanJob";
    static final String WORK_TAG_SHORT  = "BluetoothScanJobShort";

    public static volatile BluetoothAdapter bluetooth = null;

    private static volatile List<BluetoothDeviceData> tmpScanLEResults = null;

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
//            long start = System.currentTimeMillis();
//            PPApplication.logE("[IN_WORKER]  BluetoothScanWorker.doWork", "--------------- START");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed !=
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                cancelWork(context, false/*, null*/);
//                PPApplication.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - not enabled bluetooth");
                return Result.success();
            }

            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("2")) {
                cancelWork(context, false/*, null*/);
//                PPApplication.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - scanInPowerSaveMode == 2");
                return Result.success();
            }
            else {
                if (ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo)) {
                        // not scan bluetooth in configured time
                        cancelWork(context, false/*, null*/);
//                        PPApplication.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - not scan bluetooth in configured time");
                        return Result.success();
                    }
                }
            }

            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (Event.getGlobalEventsRunning()) {
                startScanner(context, false);
            }

//            PPApplication.logE("[EXECUTOR_CALL]  ***** BluetoothScanWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG");
            final Context appContext = context;
            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
//                long start1 = System.currentTimeMillis();
//                PPApplication.logE("[IN_EXECUTOR]  ***** BluetoothScanWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG");
                BluetoothScanWorker.scheduleWork(appContext, false);
//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start1;
//                PPApplication.logE("[IN_EXECUTOR]  ***** BluetoothScanWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG - timeElapsed="+timeElapsed);
                //worker.shutdown();
            };
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            /*
            //scheduleWork(context.getApplicationContext(), false);
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG)
                            .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplication.logE("[WORKER_CALL] BluetoothScanWorker.doWork", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            */
            /*
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleWork(context, false);
                }
            }, 1500);
            */

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplication.logE("[IN_WORKER]  BluetoothScanWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    public void onStopped () {
//        PPApplication.logE("[IN_LISTENER] BluetoothScanWorker.onStopped", "xxx");

        setScanRequest(context, false);
        setWaitForResults(context, false);
        setLEScanRequest(context, false);
        setWaitForLEResults(context, false);
        setScanKilled(context, false);
        BluetoothScanner.setForceOneBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
        BluetoothScanner.setForceOneLEBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
    }

    private static void _scheduleWork(final Context context, final boolean shortInterval/*, final boolean forScreenOn*/) {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    int interval = ApplicationPreferences.applicationEventBluetoothScanInterval;
                    //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                    boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
                    if (isPowerSaveMode) {
                        if (ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("1"))
                            interval = 2 * interval;
                    }
                    else {
                        if (ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("1")) {
                            if (GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo)) {
                                interval = 2 * interval;
                            }
                        }
                    }

                    if (!shortInterval) {
                        /*int keepResultsDelay = (interval * 5);
                        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BluetoothScanWorker.class)
                                .setInitialDelay(interval, TimeUnit.MINUTES)
                                .addTag(BluetoothScanWorker.WORK_TAG)
                                .build();
                        if (PPApplication.getApplicationStarted(true)) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(BluetoothScanWorker.WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                            PPApplication.logE("[WORKER_CALL] BluetoothScanWorker._scheduleWork", "(1)");
                            workManager.enqueueUniqueWork(BluetoothScanWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                        }
                    } else {
                        //waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BluetoothScanWorker.class)
                                .addTag(BluetoothScanWorker.WORK_TAG_SHORT)
                                .build();
                        if (PPApplication.getApplicationStarted(true)) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(BluetoothScanWorker.WORK_TAG_SHORT);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                            PPApplication.logE("[WORKER_CALL] BluetoothScanWorker._scheduleWork", "(2)");
                            workManager.enqueueUniqueWork(BluetoothScanWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    // shortInterval = true is called only from PPService.scheduleBluetoothWorker
    static void scheduleWork(Context context, boolean shortInterval) {
        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (shortInterval) {
                _cancelWork(context, false);
                //PPApplication.sleep(5000);
                _scheduleWork(context, true);

                /*final Context appContext = context;
                PPApplication.startHandlerThreadPPScanners();
                final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPPScanners", "START run - from=BluetoothScanWorker.scheduleWork" + " shortInterval=true");
                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        _cancelWork(appContext);
                        PPApplication.sleep(5000);
                        _scheduleWork(appContext, true);
                    //}
                });*/
            }
            else
                _scheduleWork(context, false);
        }
    }

    private static void _cancelWork(final Context context, final boolean useHandler) {
        if (isWorkScheduled(false) || isWorkScheduled(true)) {
            try {
                waitForFinish(false);
                waitForFinish(true);

                setScanRequest(context, false);
                setWaitForResults(context, false);
                setLEScanRequest(context, false);
                setWaitForLEResults(context, false);
                setScanKilled(context, true);
                BluetoothScanner.setForceOneBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                BluetoothScanner.setForceOneLEBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                if (useHandler) {
                    PPApplication.cancelWork(WORK_TAG, false);
                    PPApplication.cancelWork(WORK_TAG_SHORT, false);
                }
                else {
                    PPApplication._cancelWork(WORK_TAG, false);
                    PPApplication._cancelWork(WORK_TAG_SHORT, false);
                }

            } catch (Exception e) {
                //Log.e("BluetoothScanWorker._cancelWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private static void waitForFinish(boolean shortWork) {
        if (!isWorkRunning(shortWork)) {
            return;
        }

        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    long start = SystemClock.uptimeMillis();
                    do {

                        ListenableFuture<List<WorkInfo>> statuses;
                        if (shortWork)
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                        else
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                        boolean allFinished = true;
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (state == WorkInfo.State.RUNNING) {
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
                            break;
                        }

                        GlobalUtils.sleep(200);
                    } while (SystemClock.uptimeMillis() - start < BluetoothScanner.CLASSIC_BT_SCAN_DURATION * 1000);

                }
            }
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker.waitForFinish", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void cancelWork(Context context, boolean useHandler/*, final Handler _handler*/) {
        _cancelWork(context, useHandler);

        /*if (useHandler) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadPPScanners();
            final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPPScanners", "START run - from=BluetoothScanWorker.cancelWork");
                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    _cancelWork(appContext);
                //}
            });
        }
        else {
            _cancelWork(context);
        }*/
    }

    private static boolean isWorkRunning(boolean shortWork) {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker.isWorkRunning", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(boolean shortWork) {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker.isWorkScheduled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
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

    static void initialize(Context context, boolean clearScanResult)
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
            if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
                ApplicationPreferences.prefEventBluetoothLEScanRequest = ApplicationPreferences.
                        getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, false);
            } else
                ApplicationPreferences.prefEventBluetoothLEScanRequest = false;
        }
    }
    static void setLEScanRequest(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
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
            if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
                ApplicationPreferences.prefEventBluetoothLEWaitForResult = ApplicationPreferences.
                        getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, false);
            } else
                ApplicationPreferences.prefEventBluetoothLEWaitForResult = false;
        }
    }
    static void setWaitForLEResults(Context context, boolean startScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
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

            BluetoothScanner.bluetoothDiscoveryStarted = false;

            if (Permissions.checkLocation(context)) {
                boolean startScan = bluetooth.startDiscovery();

                if (!startScan) {
                    if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                        if (Permissions.checkBluetoothForEMUI(context)) {
                            //if (Build.VERSION.SDK_INT >= 26)
                            //    CmdBluetooth.setBluetooth(false);
                            //else
                            bluetooth.disable();
                        }
                    }
                }
                setWaitForResults(context, startScan);
            }
            setScanRequest(context, false);
        }
    }

    static void stopCLScan() {
        if (bluetooth == null)
            bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);
        if (bluetooth != null) {
            if (bluetooth.isDiscovering()) {
                bluetooth.cancelDiscovery();
            }
        }
    }

    static void startLEScan(final Context context)
    {
        if (BluetoothScanner.bluetoothLESupported(/*context*/)) {

            synchronized (PPApplication.bluetoothLEScanMutex) {

                if (bluetooth == null)
                    bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

                if (bluetooth != null) {
                    if (Permissions.checkLocation(context)) {
                        try {
                            if (BluetoothScanner.bluetoothLEScanner == null) {
                                BluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                            }
                            if (BluetoothScanner.bluetoothLEScanCallback21 == null) {
                                BluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback21(context);
                            }

                            ScanSettings.Builder builder = new ScanSettings.Builder();

                            tmpScanLEResults = null;

                            int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                            if (forceScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)
                                builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                            else
                                builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

                            if (bluetooth.isOffloadedScanBatchingSupported())
                                builder.setReportDelay(ApplicationPreferences.applicationEventBluetoothLEScanDuration * 1000L);
                            ScanSettings settings = builder.build();

                            List<ScanFilter> filters = new ArrayList<>();

                            if (bluetooth.isEnabled()) {
                                BluetoothScanner.bluetoothLEScanner.startScan(filters, settings, BluetoothScanner.bluetoothLEScanCallback21);
                                setWaitForLEResults(context, true);
                            }

                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    setLEScanRequest(context, false);
                }
            }
        }
    }

    static void stopLEScan(final Context context) {
        if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (bluetooth != null) {
                if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                    try {
                        if (BluetoothScanner.bluetoothLEScanner == null) {
                            BluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                        }
                        if (BluetoothScanner.bluetoothLEScanCallback21 == null) {
                            BluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback21(context);
                        }

                        BluetoothScanner.bluetoothLEScanner.stopScan(BluetoothScanner.bluetoothLEScanCallback21);
                        BluetoothScanner.bluetoothLEScanCallback21 = null;

                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }
        }
    }

    static void finishLEScan(Context context) {
        synchronized (PPApplication.bluetoothLEScanMutex) {

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            if (tmpScanLEResults != null) {
                for (BluetoothDeviceData device : tmpScanLEResults) {
                    scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                }
                //tmpScanLEResults = null;
            }

            saveLEScanResults(context, scanResults);
        }
    }

    static void startScanner(Context context, boolean fromDialog)
    {
        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        if (fromDialog || ApplicationPreferences.applicationEventBluetoothEnableScanning) {
            setScanKilled(context, false);
            if (fromDialog) {
                setScanRequest(context, true);
                setLEScanRequest(context, true);
            }

            BluetoothScanner bluetoothScanner = new BluetoothScanner(context);
            bluetoothScanner.doScan(fromDialog);
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
                if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                    Set<BluetoothDevice> boundedDevices = bluetooth.getBondedDevices();
                    //boundedDevicesList.clear();
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
        synchronized (PPApplication.bluetoothCLScanMutex) {

            if (BluetoothScanner.bluetoothDiscoveryStarted) {

                BluetoothScanner.bluetoothDiscoveryStarted = false;

                List<BluetoothDeviceData> scanResults = new ArrayList<>();

                if (BluetoothScanner.tmpBluetoothScanResults != null) {
                    for (BluetoothDeviceData device : BluetoothScanner.tmpBluetoothScanResults) {
                        scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                    }
                }

                saveCLScanResults(context, scanResults);

                setWaitForResults(context, false);

                int forceOneScan = ApplicationPreferences.prefForceOneBluetoothScan;
                BluetoothScanner.setForceOneBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    PPExecutors.handleEvents(context, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, "SENSOR_TYPE_BLUETOOTH_SCANNER", 5);

                    /*
                    Data workData = new Data.Builder()
                            .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG)
                                    .setInputData(workData)
                                    .setInitialDelay(5, TimeUnit.SECONDS)
                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                    try {
                        if (PPApplication.getApplicationStarted(true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

//                                //if (PPApplication.logEnabled()) {
//                                ListenableFuture<List<WorkInfo>> statuses;
//                                statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG);
//                                try {
//                                    List<WorkInfo> workInfoList = statuses.get();
//                                } catch (Exception ignored) {
//                                }
//                                //}

//                                PPApplication.logE("[WORKER_CALL] BluetoothScanWorker.finishCLScan", "xxx");
                                //workManager.enqueue(worker);
                                workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    */

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

                BluetoothScanner.tmpBluetoothScanResults = null;
            }
        }
    }

}
