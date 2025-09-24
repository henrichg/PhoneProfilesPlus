package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

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

// BT scaner Worker

/** @noinspection ExtractMethodRecommender*/
public class BluetoothScanWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "BluetoothScanJob";
    static final String WORK_TAG_SHORT  = "BluetoothScanJobShort";

    static volatile BluetoothAdapter bluetooth = null;

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
//            PPApplicationStatic.logE("[IN_WORKER]  BluetoothScanWorker.doWork", "--------------- START");

            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return Result.success();

            if (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, context).preferenceAllowed !=
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                cancelWork(context, false/*, null*/);
//                PPApplicationStatic.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - not enabled bluetooth");
                return Result.success();
            }

            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("2")) {
                cancelWork(context, false/*, null*/);
//                PPApplicationStatic.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - scanInPowerSaveMode == 2");
                return Result.success();
            }
            else {
                if (ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2")) {
                    if (GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo)) {
                        // not scan bluetooth in configured time
                        cancelWork(context, false/*, null*/);
//                        PPApplicationStatic.logE("[IN_WORKER] BluetoothScanWorker.doWork", "---------------------------------------- END - not scan bluetooth in configured time");
                        return Result.success();
                    }
                }
            }

            if (bluetooth == null)
                bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (EventStatic.getGlobalEventsRunning(context)) {
//                Log.e("BluetoothScanWorker.doWork", "startScanner");
                startScanner(context, false);
            }

//            PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** BluetoothScanWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG");
            final Context appContext = context.getApplicationContext();
            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothScanWorker_doWork);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                  long start1 = System.currentTimeMillis();
//                  PPApplicationStatic.logE("[IN_EXECUTOR]  ***** BluetoothScanWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG");
                    BluetoothScanWorker.scheduleWork(appContext, false);
//                  long finish = System.currentTimeMillis();
//                  long timeElapsed = finish - start1;
//                  PPApplicationStatic.logE("[IN_EXECUTOR]  ***** BluetoothScanWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG - timeElapsed="+timeElapsed);
                    //worker.shutdown();

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] BluetoothScanWorker,doWork", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }

            };
//            PPApplicationStatic.logE("[DELAYED_EXECUTOR_CALL] BluetoothScanWorker.doWork", "xxxx");
            PPApplicationStatic.createDelayedEventsHandlerExecutor();
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

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] BluetoothScanWorker.doWork", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            */
//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  BluetoothScanWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }

    public void onStopped () {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothScanWorker.onStopped", "xxx");

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
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
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
//                        if (PPApplicationStatic.getApplicationStarted(true, true)) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(BluetoothScanWorker.WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                            PPApplicationStatic.logE("[WORKER_CALL] BluetoothScanWorker._scheduleWork", "(1)");
                            workManager.enqueueUniqueWork(BluetoothScanWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
//                        }
                    } else {
                        //waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BluetoothScanWorker.class)
                                .addTag(BluetoothScanWorker.WORK_TAG_SHORT)
                                .build();
//                        if (PPApplicationStatic.getApplicationStarted(true, true)) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(BluetoothScanWorker.WORK_TAG_SHORT);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                            PPApplicationStatic.logE("[WORKER_CALL] BluetoothScanWorker._scheduleWork", "(2)");
                            workManager.enqueueUniqueWork(BluetoothScanWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
//                        }
                    }
                }
            }
        } catch (Exception e) {
            //Log.e("BluetoothScanWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    // shortInterval = true is called only from PPService.scheduleBluetoothWorker
    static void scheduleWork(Context context, boolean shortInterval) {
        if (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, context).preferenceAllowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (shortInterval) {
                _cancelWork(context, false);
                //PPApplication.sleep(5000);
                _scheduleWork(context, true);
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
                    PPApplicationStatic.cancelWork(WORK_TAG, false);
                    PPApplicationStatic.cancelWork(WORK_TAG_SHORT, false);
                }
                else {
                    PPApplicationStatic._cancelWork(WORK_TAG, false);
                    PPApplicationStatic._cancelWork(WORK_TAG_SHORT, false);
                }

            } catch (Exception e) {
                //Log.e("BluetoothScanWorker._cancelWork", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    private static void waitForFinish(boolean shortWork) {
        if (!isWorkRunning(shortWork)) {
            return;
        }

        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
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
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (state == WorkInfo.State.RUNNING) {
                                    allFinished = false;
                                    break;
                                }
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            PPApplicationStatic.logException("BluetoothScanWorker.waitForFinish", Log.getStackTraceString(e), false);
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
            PPApplicationStatic.recordException(e);
        }
    }

    static void cancelWork(Context context, boolean useHandler/*, final Handler _handler*/) {
        _cancelWork(context, useHandler);
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    private static boolean isWorkRunning(boolean shortWork) {
        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
                            break;
                        }
                        return running;
                    } catch (ExecutionException | InterruptedException e) {
                        PPApplicationStatic.logException("BluetoothScanWorker.isWorkRunning", Log.getStackTraceString(e), false);
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
            PPApplicationStatic.recordException(e);
            return false;
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static boolean isWorkScheduled(boolean shortWork) {
        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                            break;
                        }
                        return running;
                    } catch (ExecutionException | InterruptedException e) {
                        PPApplicationStatic.logException("BluetoothScanWorker.isWorkScheduled", Log.getStackTraceString(e), false);
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
            PPApplicationStatic.recordException(e);
            return false;
        }
    }

    //------------------------------------------------------------

    /*
    static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothAdapter adapter;
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null)
            adapter = bluetoothManager.getAdapter();
        else
            adapter = null;
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

        if (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, context).preferenceAllowed !=
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getScanRequest", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothScanRequest = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, false);
        }
    }
    static void setScanRequest(Context context, boolean startScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setScanRequest", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothScanRequest = startScan;
        }
    }

    static void getLEScanRequest(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getLEScanRequest", "PPApplication.eventBluetoothSensorMutex");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setLEScanRequest", "PPApplication.eventBluetoothSensorMutex");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getWaitForResults", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothWaitForResult = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, false);
        }
    }
    static void setWaitForResults(Context context, boolean startScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setWaitForResults", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothWaitForResult = startScan;
        }
    }

    static void getWaitForLEResults(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getWaitForLEResults", "PPApplication.eventBluetoothSensorMutex");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setWaitForLEResults", "PPApplication.eventBluetoothSensorMutex");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getScanKilled", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothScanKilled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_SCAN_KILLED, false);
        }
    }
    static void setScanKilled(Context context, boolean startScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setScanKilled", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_SCAN_KILLED, startScan);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothScanKilled = startScan;
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
            bluetoothScanner.doScan(/*fromDialog*/);
        }
        //dataWrapper.invalidateDataWrapper();
    }

    /*
    static public void stopScan(Context context)
    {
        unlock();
        if (getBluetoothEnabledForScan(context)) {
            //    CmdBluetooth.setBluetooth(false);
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getBluetoothEnabledForScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefEventBluetoothEnabledForScan = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, false);
        }
    }

    static void setBluetoothEnabledForScan(Context context, boolean setEnabled)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.setBluetoothEnabledForScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, setEnabled);
            editor.apply();
            ApplicationPreferences.prefEventBluetoothEnabledForScan = setEnabled;
        }
    }

    @SuppressLint("MissingPermission")
    static int getBluetoothType(BluetoothDevice device) {
        return device.getType();
    }

    @SuppressLint("MissingPermission")
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
                    @SuppressLint("MissingPermission")
                    Set<BluetoothDevice> boundedDevices = bluetooth.getBondedDevices();
                    //boundedDevicesList.clear();
                    if (boundedDevices != null) {
                        for (BluetoothDevice device : boundedDevices) {
                            String name = device.getName();
                            if ((name != null) && (!name.isEmpty()))
                                boundedDevicesList.add(new BluetoothDeviceData(name, device.getAddress(),
                                    getBluetoothType(device), false, 0, true, false));
                        }
                    }
                    saveBoundedDevicesList(context, boundedDevicesList);
                }
            }
        }
    }

    private static final String PREF_SCAN_RESULT_COUNT = "count";
    private static final String PREF_SCAN_RESULT_DEVICE = "device";

    //public static void getBoundedDevicesList(Context context)
    static List<BluetoothDeviceData> getBoundedDevicesList(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getBoundedDevicesList", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            //if (boundedDevicesList == null)
            //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

            //boundedDevicesList.clear();

            List<BluetoothDeviceData> boundedDevicesList = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(PREF_SCAN_RESULT_COUNT, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(PREF_SCAN_RESULT_DEVICE + i, "");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.saveBoundedDevicesList", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            //if (boundedDevicesList == null)
            //    boundedDevicesList = new ArrayList<BluetoothDeviceData>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            int size = boundedDevicesList.size();
            editor.putInt(PREF_SCAN_RESULT_COUNT, size);

            Gson gson = new Gson();

            for (int i = 0; i < size; i++) {
                String json = gson.toJson(boundedDevicesList.get(i));
                editor.putString(PREF_SCAN_RESULT_DEVICE + i, json);
            }

            editor.apply();
        }
    }

    static List<BluetoothDeviceData> getScanResults(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.getScanResults", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            int count = preferences.getInt(PREF_SCAN_RESULT_COUNT, -1);

            if (count >= 0) {
                Gson gson = new Gson();
                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(PREF_SCAN_RESULT_DEVICE + i, "");
                    if (!json.isEmpty()) {
                        BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                        device.scanned = true;
                        scanResults.add(device);
                    }
                }
            }

            preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            count = preferences.getInt(PREF_SCAN_RESULT_COUNT, -1);

            if (count >= 0) {
                Gson gson = new Gson();
                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(PREF_SCAN_RESULT_DEVICE + i, "");
                    if (!json.isEmpty()) {
                        BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                        device.scanned = true;
                        scanResults.add(device);
                    }
                }
            }

            if (scanResults.isEmpty())
                return null;
            else
                return scanResults;
        }
    }

    private static void clearScanResults(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.clearScanResults", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();
            editor.putInt(PREF_SCAN_RESULT_COUNT, -1);

            editor.apply();

            preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            editor = preferences.edit();

            editor.clear();
            editor.putInt(PREF_SCAN_RESULT_COUNT, -1);

            editor.apply();
        }
    }

    static void saveCLScanResults(Context context, List<BluetoothDeviceData> scanResults)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.saveCLScanResults", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            int size = scanResults.size();
            editor.putInt(PREF_SCAN_RESULT_COUNT, size);

            Gson gson = new Gson();
            for (int i = 0; i < size; i++) {
                String json = gson.toJson(scanResults.get(i));
                editor.putString(PREF_SCAN_RESULT_DEVICE + i, json);
            }

            editor.apply();
        }
    }

    static void saveLEScanResults(Context context, List<BluetoothDeviceData> scanResults)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.saveLEScanResults", "PPApplication.bluetoothScanResultsMutex");
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            int size = scanResults.size();
            editor.putInt(PREF_SCAN_RESULT_COUNT, size);

            Gson gson = new Gson();
            for (int i = 0; i < size; i++) {
                String json = gson.toJson(scanResults.get(i));
                editor.putString(PREF_SCAN_RESULT_DEVICE + i, json);
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanWorker.addLEScanResult", "PPApplication.bluetoothLEScanMutex");
        synchronized (PPApplication.bluetoothLEScanMutex) {
            if (BluetoothScanner.tmpScanLEResults == null)
                BluetoothScanner.tmpScanLEResults = new ArrayList<>();

            String btName = device.getName();
            if (!btName.isEmpty()) {
                // do not add device without name

                boolean found = false;
                for (BluetoothDeviceData tmpDevice : BluetoothScanner.tmpScanLEResults) {
                    if ((!tmpDevice.getAddress().isEmpty()) &&
                            (!device.getAddress().isEmpty()) &&
                            tmpDevice.getAddress().equals(device.getAddress())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (BluetoothDeviceData tmpDevice : BluetoothScanner.tmpScanLEResults) {
                        if ((!tmpDevice.getName().isEmpty()) &&
                                tmpDevice.getName().equalsIgnoreCase(btName)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    if (BluetoothScanner.tmpScanLEResults != null) // maybe set to null by startLEScan() or finishLEScan()
                        BluetoothScanner.tmpScanLEResults.add(new BluetoothDeviceData(device.getName(), device.getAddress(), device.type, false, 0, false, true));
                }
            }
        }
    }

}
