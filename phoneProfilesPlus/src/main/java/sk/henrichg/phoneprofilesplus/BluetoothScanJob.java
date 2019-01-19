package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class BluetoothScanJob extends Job {

    static final String JOB_TAG  = "BluetoothScanJob";
    //static final String JOB_TAG_SHORT  = "BluetoothScanJob_short";

    public static BluetoothAdapter bluetooth = null;

    private static List<BluetoothDeviceData> tmpScanLEResults = null;

    private static final String PREF_EVENT_BLUETOOTH_SCAN_REQUEST = "eventBluetoothScanRequest";
    private static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS = "eventBluetoothWaitForResults";
    private static final String PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST = "eventBluetoothLEScanRequest";
    private static final String PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS = "eventBluetoothWaitForLEResults";
    private static final String PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN = "eventBluetoothEnabledForScan";

    //private static CountDownLatch countDownLatch = null;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        PPApplication.logE("BluetoothScanJob.onRunJob", "xxx");

        Context context = getContext();

        CallsCounter.logCounter(context, "BluetoothScanJob.onRunJob", "BluetoothScanJob_onRunJob");

        //countDownLatch = new CountDownLatch(1);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed !=
                PreferenceAllowed.PREFERENCE_ALLOWED) {
            cancelJob(context, false, null);
            return Result.SUCCESS;
        }

        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("2")) {
            PPApplication.logE("BluetoothScanJob.onRunJob", "update in power save mode is not allowed = cancel job");
            cancelJob(context, false, null);
            return Result.SUCCESS;
        }

        if (bluetooth == null)
            bluetooth = getBluetoothAdapter(context);

        if (Event.getGlobalEventsRunning(context))
        {
            if ((!params.getExtras().getBoolean("shortInterval", false)) ||
                    params.getExtras().getBoolean("notShortIsExact", true))
                startScanner(context, false);
        }

        scheduleJob(context, false, null, false/*, false*/);

        /*try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        countDownLatch = null;*/
        PPApplication.logE("BluetoothScanJob.onRunJob", "return");
        return Result.SUCCESS;
    }

    protected void onCancel() {
        PPApplication.logE("BluetoothScanJob.onCancel", "xxx");

        Context context = getContext();

        CallsCounter.logCounter(context, "BluetoothScanJob.onCancel", "BluetoothScanJob_onCancel");

        setScanRequest(context, false);
        setWaitForResults(context, false);
        setLEScanRequest(context, false);
        setWaitForLEResults(context, false);
        WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
        WifiBluetoothScanner.setForceOneLEBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
    }

    private static void _scheduleJob(final Context context, final boolean shortInterval/*, final boolean forScreenOn*/) {
        JobManager jobManager = null;
        try {
            jobManager = JobManager.instance();
        } catch (Exception ignored) { }

        if (jobManager != null) {
            final JobRequest.Builder jobBuilder;

            int interval = ApplicationPreferences.applicationEventBluetoothScanInterval(context);
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context).equals("1"))
                interval = 2 * interval;

            if (!shortInterval) {
                //jobManager.cancelAllForTag(JOB_TAG_SHORT);

                jobBuilder = new JobRequest.Builder(JOB_TAG);

                if (TimeUnit.MINUTES.toMillis(interval) < JobRequest.MIN_INTERVAL) {
                    jobManager.cancelAllForTag(JOB_TAG);
                    jobBuilder.setExact(TimeUnit.MINUTES.toMillis(interval));
                } else {
                    int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
                    PPApplication.logE("BluetoothScanJob.scheduleJob", "requestsForTagSize=" + requestsForTagSize);
                    if (requestsForTagSize == 0) {
                        if (TimeUnit.MINUTES.toMillis(interval) < JobRequest.MIN_INTERVAL)
                            // must be set min interval because:
                            //   java.lang.IllegalArgumentException: intervalMs is out of range of [900000, 9223372036854775807] (too low)
                            jobBuilder.setPeriodic(JobRequest.MIN_INTERVAL);
                        else
                            jobBuilder.setPeriodic(TimeUnit.MINUTES.toMillis(interval));
                    } else
                        return;
                }
            } else {
                _cancelJob(context);
                jobBuilder = new JobRequest.Builder(JOB_TAG/*_SHORT*/);
                /*if (forScreenOn)
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
                else
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));*/
                jobBuilder.startNow();
            }

            PPApplication.logE("BluetoothScanJob.scheduleJob", "build and schedule");

            try {
                PersistableBundleCompat bundleCompat = new PersistableBundleCompat();
                bundleCompat.putBoolean("shortInterval", shortInterval);
                bundleCompat.putBoolean("notShortIsExact", TimeUnit.MINUTES.toMillis(interval) < JobRequest.MIN_INTERVAL);

                jobBuilder
                        .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                        .setExtras(bundleCompat)
                        .build()
                        .scheduleAsync();
            } catch (Exception ignored) {}
        }
    }

    static void scheduleJob(final Context context, final boolean useHandler, final Handler _handler, final boolean shortInterval/*, final boolean forScreenOn*/) {
        PPApplication.logE("BluetoothScanJob.scheduleJob", "shortInterval="+shortInterval);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (useHandler && (_handler == null)) {
                PPApplication.startHandlerThread("BluetoothScanJob.scheduleJob");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _scheduleJob(context, shortInterval/*, forScreenOn*/);
                        /*if (countDownLatch != null)
                            countDownLatch.countDown();*/
                    }
                });
            }
            else {
                _scheduleJob(context, shortInterval/*, forScreenOn*/);
                /*if (countDownLatch != null)
                    countDownLatch.countDown();*/
            }
        }
        else
            PPApplication.logE("BluetoothScanJob.scheduleJob","BluetoothHardware=false");
    }

    private static void _cancelJob(final Context context) {
        if (isJobScheduled()) {
            try {
                JobManager jobManager = JobManager.instance();


                PPApplication.logE("BluetoothScanJob._cancelJob", "START WAIT FOR FINISH");
                long start = SystemClock.uptimeMillis();
                do {
                    if (!isJobScheduled()) {
                        PPApplication.logE("BluetoothScanJob._cancelJob", "NOT SCHEDULED");
                        break;
                    }

                    Set<Job> jobs = jobManager.getAllJobsForTag(JOB_TAG);
                    boolean allFinished = true;
                    for (Job job : jobs) {
                        if (!job.isFinished()) {
                            allFinished = false;
                            break;
                        }
                    }
                    if (allFinished) {
                        PPApplication.logE("BluetoothScanJob._cancelJob", "FINISHED");
                        break;
                    }

                    //try { Thread.sleep(100); } catch (InterruptedException e) { }
                    SystemClock.sleep(100);
                }
                while (SystemClock.uptimeMillis() - start < WifiBluetoothScanner.classicBTScanDuration * 1000);
                PPApplication.logE("BluetoothScanJob._cancelJob", "END WAIT FOR FINISH");

                setScanRequest(context, false);
                setWaitForResults(context, false);
                setLEScanRequest(context, false);
                setWaitForLEResults(context, false);
                WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
                WifiBluetoothScanner.setForceOneLEBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                //jobManager.cancelAllForTag(JOB_TAG_SHORT);
                jobManager.cancelAllForTag(JOB_TAG);

                PPApplication.logE("BluetoothScanJob._cancelJob", "CANCELED");
            } catch (Exception ignored) {
            }
        }
    }

    static void cancelJob(final Context context, final boolean useHandler, final Handler _handler) {
        PPApplication.logE("BluetoothScanJob.cancelJob", "xxx");

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThread("BluetoothScanJob.cancelJob");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _cancelJob(context);
                    /*if (countDownLatch != null)
                        countDownLatch.countDown();*/
                }
            });
        }
        else {
            _cancelJob(context);
            /*if (countDownLatch != null)
                countDownLatch.countDown();*/
        }
    }

    static boolean isJobScheduled() {
        PPApplication.logE("BluetoothScanJob.isJobScheduled", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            return (jobManager.getAllJobRequestsForTag(JOB_TAG).size() != 0)/* ||
                    (jobManager.getAllJobRequestsForTag(JOB_TAG_SHORT).size() != 0)*/;
        } catch (Exception e) {
            return  false;
        }
    }

    //------------------------------------------------------------

    static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothAdapter adapter;
        /*if (android.os.Build.VERSION.SDK_INT < 18)
            adapter = BluetoothAdapter.getDefaultAdapter();
        else {*/
            BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                adapter = bluetoothManager.getAdapter();
            else
                adapter = null;
        //}
        return adapter;
    }

    public static void initialize(Context context)
    {
        setScanRequest(context, false);
        setLEScanRequest(context, false);
        setWaitForResults(context, false);
        setWaitForLEResults(context, false);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed !=
                PreferenceAllowed.PREFERENCE_ALLOWED)
            return;

        if (bluetooth == null)
            bluetooth = getBluetoothAdapter(context);
        if (bluetooth == null)
            return;

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

    static boolean getScanRequest(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, false);
    }

    static void setScanRequest(Context context, boolean startScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_SCAN_REQUEST, startScan);
        editor.apply();
    }

    static boolean getLEScanRequest(Context context)
    {
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {
            ApplicationPreferences.getSharedPreferences(context);
            return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, false);
        }
        else
            return false;
    }

    static void setLEScanRequest(Context context, boolean startScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_LE_SCAN_REQUEST, startScan);
        editor.apply();
    }

    static boolean getWaitForResults(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, false);
    }

    static void setWaitForResults(Context context, boolean startScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_RESULTS, startScan);
        editor.apply();
    }

    static boolean getWaitForLEResults(Context context)
    {
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {
            ApplicationPreferences.getSharedPreferences(context);
            return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, false);
        }
        else
            return false;
    }

    static void setWaitForLEResults(Context context, boolean startScan)
    {
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {
            ApplicationPreferences.getSharedPreferences(context);
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_EVENT_BLUETOOTH_WAIT_FOR_LE_RESULTS, startScan);
            editor.apply();
        }
    }

    static void startCLScan(Context context)
    {
        if (bluetooth == null)
            bluetooth = getBluetoothAdapter(context);

        if (bluetooth != null) {
            if (bluetooth.isDiscovering())
                bluetooth.cancelDiscovery();

            WifiBluetoothScanner.bluetoothDiscoveryStarted = false;

            if (Permissions.checkLocation(context)) {
                boolean startScan = bluetooth.startDiscovery();
                PPApplication.logE("@@@ BluetoothScanJob.startScan", "scanStarted=" + startScan);

                if (!startScan) {
                    if (WifiBluetoothScanner.bluetoothEnabledForScan) {
                        PPApplication.logE("@@@ BluetoothScanJob.startScan", "disable bluetooth");
                        bluetooth.disable();
                    }
                }
                setWaitForResults(context, startScan);
            }
            setScanRequest(context, false);
        }
    }

    static void stopCLScan(Context context) {
        if (bluetooth == null)
            bluetooth = getBluetoothAdapter(context);
        if (bluetooth != null) {
            if (bluetooth.isDiscovering())
                bluetooth.cancelDiscovery();
        }
    }

    @SuppressLint("NewApi")
    static void startLEScan(Context context)
    {
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {

            synchronized (PPApplication.bluetoothLEScanResultsMutex) {

                if (bluetooth == null)
                    bluetooth = getBluetoothAdapter(context);

                if (bluetooth != null) {
                    if (Permissions.checkLocation(context)) {

                        boolean startScan = false;

                        if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                            if (WifiBluetoothScanner.bluetoothLEScanner == null)
                                WifiBluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                            //if (WifiBluetoothScanner.bluetoothLEScanCallback21 == null)
                            //    WifiBluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback21(context);

                            //WifiBluetoothScanner.leScanner.stopScan(WifiBluetoothScanner.leScanCallback21);

                            ScanSettings.Builder builder = new ScanSettings.Builder();

                            tmpScanLEResults = null;

                            int forceScan = WifiBluetoothScanner.getForceOneBluetoothScan(context);
                            if (forceScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)
                                builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                            else
                                builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

                            if (bluetooth.isOffloadedScanBatchingSupported())
                                builder.setReportDelay(ApplicationPreferences.applicationEventBluetoothLEScanDuration(context) * 1000);
                            ScanSettings settings = builder.build();

                            List<ScanFilter> filters = new ArrayList<>();
                            try {
                                WifiBluetoothScanner.bluetoothLEScanner.startScan(filters, settings, new BluetoothLEScanCallback21(context));
                                startScan = true;
                            } catch (Exception ignored) {
                            }
                        } else {
                            //if (WifiBluetoothScanner.bluetoothLEScanCallback18 == null)
                            //    WifiBluetoothScanner.bluetoothLEScanCallback18 = new BluetoothLEScanCallback18(context);

                            //bluetooth.stopLeScan(WifiBluetoothScanner.leScanCallback18);

                            tmpScanLEResults = null;

                            startScan = bluetooth.startLeScan(new BluetoothLEScanCallback18(context));

                            if (!startScan) {
                                if (WifiBluetoothScanner.bluetoothEnabledForScan) {
                                    bluetooth.disable();
                                }
                            }
                        }

                        setWaitForLEResults(context, startScan);
                    }
                    setLEScanRequest(context, false);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    static void stopLEScan(Context context) {
        if (WifiBluetoothScanner.bluetoothLESupported(context)) {
            if (bluetooth == null)
                bluetooth = getBluetoothAdapter(context);

            if (bluetooth != null) {
                if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                    try {
                        if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                            if (WifiBluetoothScanner.bluetoothLEScanner == null)
                                WifiBluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                            //if (WifiBluetoothScanner.bluetoothLEScanCallback21 == null)
                            //    WifiBluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback21(context);
                            WifiBluetoothScanner.bluetoothLEScanner.stopScan(new BluetoothLEScanCallback21(context));
                        } else {
                            //if (WifiBluetoothScanner.bluetoothLEScanCallback18 == null)
                            //    WifiBluetoothScanner.bluetoothLEScanCallback18 = new BluetoothLEScanCallback18(context);
                            bluetooth.stopLeScan(new BluetoothLEScanCallback18(context));
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    static void finishLEScan(Context context) {
        synchronized (PPApplication.bluetoothLEScanResultsMutex) {
            PPApplication.logE("BluetoothScanJob.finishLEScan", "xxx");

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            if (tmpScanLEResults != null) {

                for (BluetoothDeviceData device : tmpScanLEResults) {
                    scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                }
                tmpScanLEResults = null;
            }

            saveLEScanResults(context, scanResults);
        }
    }

    static void startScanner(Context context, boolean fromDialog)
    {
        PPApplication.logE("$$$ BluetoothScanJob.startScanner", "xxx");
        DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        if (fromDialog || ApplicationPreferences.applicationEventBluetoothEnableScanning(context)) {


            PPApplication.logE("$$$ BluetoothScanJob.startScanner", "scanning enabled");
            if (fromDialog) {
                try {
                    Intent scanServiceIntent = new Intent(context, WifiBluetoothScannerService.class);
                    scanServiceIntent.putExtra(WifiBluetoothScannerService.EXTRA_SCANNER_TYPE, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                    context.startService(scanServiceIntent);
                } catch (Exception ignored) {
                }
            }
            else {
                WifiBluetoothScanner wifiBluetoothScanner = new WifiBluetoothScanner(context);
                wifiBluetoothScanner.doScan(WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            }
        }
        dataWrapper.invalidateDataWrapper();
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
        PPApplication.setForceOneBluetoothScan(context, false);
    }
    */

    /*
    static boolean getBluetoothEnabledForScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, false);
    }

    static void setBluetoothEnabledForScan(Context context, boolean setEnabled)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, setEnabled);
        editor.apply();
    }
    */

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
            bluetooth = getBluetoothAdapter(context);

        if (bluetooth != null) {
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

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getBoundedDevicesList(Context context)
    static List<BluetoothDeviceData> getBoundedDevicesList(Context context)
    {
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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
        synchronized (PPApplication.bluetoothLEScanResultsMutex) {
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

    static void finishScan(final Context context) {
        synchronized (PPApplication.bluetoothScanResultsMutex) {
            PPApplication.logE("BluetoothScanJob.finishScan", "BluetoothScanBroadcastReceiver: discoveryStarted=" + WifiBluetoothScanner.bluetoothDiscoveryStarted);

            if (WifiBluetoothScanner.bluetoothDiscoveryStarted) {

                WifiBluetoothScanner.bluetoothDiscoveryStarted = false;

                List<BluetoothDeviceData> scanResults = new ArrayList<>();

                if (WifiBluetoothScanner.tmpBluetoothScanResults != null) {

                    for (BluetoothDeviceData device : WifiBluetoothScanner.tmpBluetoothScanResults) {
                        scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0, false, true));
                    }
                }

                saveCLScanResults(context, scanResults);

                setWaitForResults(context, false);

                int forceOneScan = WifiBluetoothScanner.getForceOneBluetoothScan(context);
                WifiBluetoothScanner.setForceOneBluetoothScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    /*
                    // start job
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThread("BluetoothScanJob.finishScan");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothScanJob.finishScan.Handler.postDelayed");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                            } finaly (
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                            }
                        }
                    }, 5000);*/
                    PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, 5, context);
                }

                WifiBluetoothScanner.tmpBluetoothScanResults = null;
            }
        }
    }

}
