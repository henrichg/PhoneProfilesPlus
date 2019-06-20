package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

class WifiScanJob extends Job {

    static final String JOB_TAG  = "WifiScanJob";
    //static final String JOB_TAG_SHORT  = "WifiScanJob_short";

    public static WifiManager wifi = null;
    private static WifiManager.WifiLock wifiLock = null;

    private static final String PREF_EVENT_WIFI_SCAN_REQUEST = "eventWifiScanRequest";
    private static final String PREF_EVENT_WIFI_WAIT_FOR_RESULTS = "eventWifiWaitForResults";
    private static final String PREF_EVENT_WIFI_ENABLED_FOR_SCAN = "eventWifiEnabledForScan";

    //private static CountDownLatch countDownLatch = null;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        PPApplication.logE("WifiScanJob.onRunJob", "xxx");

        Context context = getContext();

        CallsCounter.logCounter(context, "WifiScanJob.onRunJob", "WifiScanJob_onRunJob");

        //countDownLatch = new CountDownLatch(1);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed !=
                PreferenceAllowed.PREFERENCE_ALLOWED) {
            cancelJob(context, false, null);
            PPApplication.logE("WifiScanJob.onRunJob", "return - not allowed wifi scanning");
            return Result.SUCCESS;
        }

        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("2")) {
            cancelJob(context, false, null);
            PPApplication.logE("WifiScanJob.onRunJob", "return - update in power save mode is not allowed");
            return Result.SUCCESS;
        }

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Event.getGlobalEventsRunning(context))
        {
            PPApplication.logE("WifiScanJob.onRunJob", "global events running=true");
            PPApplication.logE("WifiScanJob.onRunJob", "shortInterval="+params.getExtras().getBoolean("shortInterval", false));
            PPApplication.logE("WifiScanJob.onRunJob", "notShortIsExact="+params.getExtras().getBoolean("notShortIsExact", true));

            if ((!params.getExtras().getBoolean("shortInterval", false)) ||
                    params.getExtras().getBoolean("notShortIsExact", true)) {
                PPApplication.logE("WifiScanJob.onRunJob", "start scanner");
                startScanner(context, false);
            }
        }

        PPApplication.logE("WifiScanJob.onRunJob", "schedule job");
        scheduleJob(context, false, null, false/*, false, false*/);

        /*try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        countDownLatch = null;*/
        PPApplication.logE("WifiScanJob.onRunJob", "return");
        return Result.SUCCESS;
    }

    protected void onCancel() {
        PPApplication.logE("WifiScanJob.onCancel", "xxx");

        Context context = getContext();

        CallsCounter.logCounter(context, "WifiScanJob.onCancel", "WifiScanJob_onCancel");

        setScanRequest(context, false);
        setWaitForResults(context, false);
        WifiBluetoothScanner.setForceOneWifiScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);
    }

    private static void _scheduleJob(final Context context, final boolean shortInterval/*, final boolean forScreenOn, final boolean afterEnableWifi*/) {
        JobManager jobManager = null;
        try {
            jobManager = JobManager.instance();
        } catch (Exception ignored) { }

        if (jobManager != null) {
            final JobRequest.Builder jobBuilder;

            int interval = ApplicationPreferences.applicationEventWifiScanInterval(context);
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context).equals("1"))
                interval = 2 * interval;

            if (!shortInterval) {
                //jobManager.cancelAllForTag(JOB_TAG_SHORT);

                jobBuilder = new JobRequest.Builder(JOB_TAG);

                if (TimeUnit.MINUTES.toMillis(interval) < JobRequest.MIN_INTERVAL) {
                    jobManager.cancelAllForTag(JOB_TAG);
                    jobBuilder.setExact(TimeUnit.MINUTES.toMillis(interval));
                } else {
                    int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
                    PPApplication.logE("WifiScanJob.scheduleJob", "requestsForTagSize=" + requestsForTagSize);
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
                /*if (afterEnableWifi)
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(2));
                else if (forScreenOn)
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
                else
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));*/
                jobBuilder.startNow();
            }

            PPApplication.logE("WifiScanJob.scheduleJob", "build and schedule");

            try {
                PersistableBundleCompat bundleCompat = new PersistableBundleCompat();
                bundleCompat.putBoolean("shortInterval", shortInterval);
                bundleCompat.putBoolean("notShortIsExact", TimeUnit.MINUTES.toMillis(interval) < JobRequest.MIN_INTERVAL);

                jobBuilder
                        .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                        .setExtras(bundleCompat)
                        .build()
                        .scheduleAsync();
            } catch (Exception e) {
                PPApplication.logE("WifiScanJob.scheduleJob",e.toString());
            }
        }
    }

    static void scheduleJob(final Context context, final boolean useHandler, final Handler _handler, final boolean shortInterval/*, final boolean forScreenOn, final boolean afterEnableWifi*/) {
        PPApplication.logE("WifiScanJob.scheduleJob", "shortInterval="+shortInterval);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed
                == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (useHandler && (_handler == null)) {
                PPApplication.startHandlerThreadPPService();
                final Handler handler = new Handler(PPApplication.handlerThreadPPService.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _scheduleJob(context, shortInterval/*, forScreenOn,afterEnableWifi*/);
                        /*if (countDownLatch != null)
                            countDownLatch.countDown();*/
                    }
                });
            }
            else {
                _scheduleJob(context, shortInterval/*, forScreenOn, afterEnableWifi*/);
                /*if (countDownLatch != null)
                    countDownLatch.countDown();*/
            }
        }
        else
            PPApplication.logE("WifiScanJob.scheduleJob","WifiHardware=false");
    }

    private static void _cancelJob(final Context context) {
        if (isJobScheduled()) {
            try {
                JobManager jobManager = JobManager.instance();

                PPApplication.logE("WifiScanJob._cancelJob", "START WAIT FOR FINISH");
                long start = SystemClock.uptimeMillis();
                do {
                    if (!isJobScheduled()) {
                        PPApplication.logE("WifiScanJob._cancelJob", "NOT SCHEDULED");
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
                        PPApplication.logE("WifiScanJob._cancelJob", "FINISHED");
                        break;
                    }

                    //try { Thread.sleep(100); } catch (InterruptedException e) { }
                    SystemClock.sleep(100);
                } while (SystemClock.uptimeMillis() - start < WifiBluetoothScanner.wifiScanDuration * 1000);
                PPApplication.logE("WifiScanJob._cancelJob", "END WAIT FOR FINISH");

                setScanRequest(context, false);
                setWaitForResults(context, false);
                WifiBluetoothScanner.setForceOneWifiScan(context, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                //jobManager.cancelAllForTag(JOB_TAG_SHORT);
                jobManager.cancelAllForTag(JOB_TAG);

                PPApplication.logE("WifiScanJob._cancelJob", "CANCELED");

            } catch (Exception ignored) {
            }
        }
    }

    static void cancelJob(final Context context, final boolean useHandler, final Handler _handler) {
        PPApplication.logE("WifiScanJob.cancelJob", "xxx");

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPService();
            final Handler handler = new Handler(PPApplication.handlerThreadPPService.getLooper());
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
        //PPApplication.logE("WifiScanJob.isJobScheduled", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            return (jobManager.getAllJobRequestsForTag(JOB_TAG).size() != 0)/* ||
                    (jobManager.getAllJobRequestsForTag(JOB_TAG_SHORT).size() != 0)*/;
        } catch (Exception e) {
            return false;
        }
    }

    //---------------------------------------------------------------

    public static void initialize(Context context, boolean clearScanResult)
    {
        setScanRequest(context, false);
        setWaitForResults(context, false);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed !=
                PreferenceAllowed.PREFERENCE_ALLOWED)
            return;

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        unlock();

        if (clearScanResult)
            clearScanResults(context);

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

    public static void lock()
    {
        //if (android.os.Build.VERSION.SDK_INT >= 23)
        //    PPApplication.logE("$$$ WifiScanJob.lock","idleMode="+powerManager.isDeviceIdleMode());

        // initialise the locks
        if (wifiLock == null)
            wifiLock = wifi.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY , "WifiScanWifiLock");

        try {
            if (!wifiLock.isHeld())
                wifiLock.acquire();
            PPApplication.logE("$$$ WifiScanJob.lock","xxx");
        } catch(Exception e) {
            Log.e("WifiScanJob.lock", Log.getStackTraceString(e));
        }
    }

    public static void unlock()
    {
        try {
            if ((wifiLock != null) && (wifiLock.isHeld()))
                wifiLock.release();
            PPApplication.logE("$$$ WifiScanJob.unlock", "xxx");
        } catch(Exception e) {
            Log.e("WifiScanJob.unlock", Log.getStackTraceString(e));
        }
    }

    static boolean getScanRequest(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_WIFI_SCAN_REQUEST, false);
    }

    static void setScanRequest(Context context, boolean scanRequest)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_SCAN_REQUEST, scanRequest);
        editor.apply();
        PPApplication.logE("@@@ WifiScanJob.setScanRequest","scanRequest="+scanRequest);
    }

    static public boolean getWaitForResults(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_WIFI_WAIT_FOR_RESULTS, false);
    }

    static void setWaitForResults(Context context, boolean waitForResults)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_WAIT_FOR_RESULTS, waitForResults);
        editor.apply();
        PPApplication.logE("$$$ WifiScanJob.setWaitForResults", "waitForResults=" + waitForResults);
    }

    static void startScan(Context context)
    {
        lock(); // lock wakeLock and wifiLock, then scan.
        // unlock() is then called at the end of the onReceive function of WifiScanBroadcastReceiver
        try {
            //TODO from SDK documentation: The ability for apps to trigger scan requests will be removed in a future release. :-/
            boolean startScan = wifi.startScan();
            PPApplication.logE("$$$ WifiScanJob.startScan", "scanStarted=" + startScan);
            PPApplication.logE("$$$ WifiAP", "WifiScanJob.startScan-startScan=" + startScan);
            if (!startScan) {
                if (getWifiEnabledForScan(context)) {
                    PPApplication.logE("$$$ WifiScanJob.startScan", "disable wifi");
                    PPApplication.logE("#### setWifiEnabled", "from WifiScanJob.startScan 1");
                    wifi.setWifiEnabled(false);
                }
                unlock();
            }
            setWaitForResults(context, startScan);
            setScanRequest(context, false);
        } catch (Exception e) {
            if (getWifiEnabledForScan(context)) {
                PPApplication.logE("$$$ WifiScanJob.startScan", "disable wifi");
                PPApplication.logE("#### setWifiEnabled", "from WifiScanJob.startScan 2");
                wifi.setWifiEnabled(false);
            }
            unlock();
            setWaitForResults(context, false);
            setScanRequest(context, false);
        }
    }

    static void startScanner(Context context, boolean fromDialog)
    {
        PPApplication.logE("$$$ WifiScanJob.startScanner", "xxx");
        DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        if (fromDialog || ApplicationPreferences.applicationEventWifiEnableScanning(context)) {
            if (fromDialog)
                setScanRequest(context, true);

            if (fromDialog) {
                try {
                    Intent scanServiceIntent = new Intent(context, WifiBluetoothScannerService.class);
                    scanServiceIntent.putExtra(WifiBluetoothScannerService.EXTRA_SCANNER_TYPE, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                    context.startService(scanServiceIntent);
                } catch (Exception ignored) {
                }
            }
            else {
                WifiBluetoothScanner wifiBluetoothScanner = new WifiBluetoothScanner(context);
                wifiBluetoothScanner.doScan(WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            }
        }
        dataWrapper.invalidateDataWrapper();
    }

    /*
    static public void stopScan(Context context)
    {
        PPApplication.logE("@@@ WifiScanJob.stopScan","xxx");
        unlock();
        if (getWifiEnabledForScan(context))
            wifi.setWifiEnabled(false);
        setWifiEnabledForScan(context, false);
        setScanRequest(context, false);
        setWaitForResults(context, false);
        PPApplication.setForceOneWifiScan(context, false);
    }
    */

    static boolean getWifiEnabledForScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_EVENT_WIFI_ENABLED_FOR_SCAN, false);
    }

    static void setWifiEnabledForScan(Context context, boolean setEnabled)
    {
        PPApplication.logE("@@@ WifiScanJob.setWifiEnabledForScan","setEnabled="+setEnabled);
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED_FOR_SCAN, setEnabled);
        editor.apply();
    }

    static void fillWifiConfigurationList(Context context)
    {
        //if (wifiConfigurationList == null)
        //    wifiConfigurationList = new ArrayList<WifiSSIDData>();

        List<WifiSSIDData> wifiConfigurationList = new ArrayList<>();

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null)
            return;

        if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
            // wifi must be enabled for wifi.getConfiguredNetworks()
            return;

        List<WifiConfiguration> _wifiConfigurationList = wifi.getConfiguredNetworks();
        if (_wifiConfigurationList != null)
        {
            wifiConfigurationList.clear();
            for (WifiConfiguration device : _wifiConfigurationList)
            {
                if (device.SSID != null) {
                    boolean found = false;
                    for (WifiSSIDData _device : wifiConfigurationList) {
                        //if (_device.bssid.equals(device.BSSID))
                        if ((_device.ssid != null) && (_device.ssid.equals(device.SSID))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        wifiConfigurationList.add(new WifiSSIDData(device.SSID, device.BSSID, false, true, false));
                    }
                }
            }
        }
        //saveWifiConfigurationList(context);
        saveWifiConfigurationList(context, wifiConfigurationList);
    }

    static void fillScanResults(Context context)
    {
        List<WifiSSIDData> scanResults = new ArrayList<>();
        boolean save = false;

        if (wifi == null)
            wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Permissions.checkLocation(context)) {
            List<ScanResult> _scanResults = wifi.getScanResults();
            PPApplication.logE("%%%% WifiScanJob.fillScanResults", "_scanResults="+_scanResults);
            if (_scanResults != null) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                //boolean isScreenOn = PPApplication.isScreenOn(pm);
                //PPApplication.logE("%%%% WifiScanJob.fillScanResults", "isScreenOn="+isScreenOn);
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
                        scanResults.add(new WifiSSIDData(device.SSID, device.BSSID, false, false, true));
                    }
                }
                //}
            }
            else
                PPApplication.logE("%%%% WifiScanJob.fillScanResults", "_scanResults=null");
        }
        else
            save = true;
        if (save)
            saveScanResults(context, scanResults);
    }

    private static final String SCAN_RESULT_COUNT_PREF = "count";
    private static final String SCAN_RESULT_DEVICE_PREF = "device";

    //public static void getWifiConfigurationList(Context context)
    static List<WifiSSIDData> getWifiConfigurationList(Context context)
    {
        synchronized (PPApplication.wifiScanResultsMutex) {
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
                    device.configured = true;
                    wifiConfigurationList.add(device);
                }
            }

            return wifiConfigurationList;
        }
    }

    //private static void saveWifiConfigurationList(Context context)
    private static void saveWifiConfigurationList(Context context, List<WifiSSIDData> wifiConfigurationList)
    {
        synchronized (PPApplication.wifiScanResultsMutex) {
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

            editor.apply();
        }
    }

    //public static void getScanResults(Context context)
    static List<WifiSSIDData> getScanResults(Context context)
    {
        synchronized (PPApplication.wifiScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            int count = preferences.getInt(SCAN_RESULT_COUNT_PREF, -1);

            if (count > -1) {
                List<WifiSSIDData> scanResults = new ArrayList<>();

                Gson gson = new Gson();

                for (int i = 0; i < count; i++) {
                    String json = preferences.getString(SCAN_RESULT_DEVICE_PREF + i, "");
                    if (!json.isEmpty()) {
                        WifiSSIDData device = gson.fromJson(json, WifiSSIDData.class);
                        device.scanned = true;
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
        synchronized (PPApplication.wifiScanResultsMutex) {
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

            editor.apply();
        }
    }

    private static void clearScanResults(Context context) {
        synchronized (PPApplication.wifiScanResultsMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.WIFI_SCAN_RESULTS_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();
            editor.putInt(SCAN_RESULT_COUNT_PREF, -1);

            editor.apply();
        }
    }

    static String getSSID(WifiManager wifiManager, WifiInfo wifiInfo, List<WifiSSIDData> wifiConfigurationList)
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
                    if ((wifiConfiguration.bssid != null) &&
                            (wifiConfiguration.bssid.equals(wifiInfo.getBSSID())))
                        return wifiConfiguration.ssid.replace("\"", "");
                }
            }
        }

        if (SSID.equals("<unknown ssid>")) {
            List<WifiConfiguration> listOfConfigurations = wifiManager.getConfiguredNetworks();

            for (int index = 0; index < listOfConfigurations.size(); index++) {
                WifiConfiguration configuration = listOfConfigurations.get(index);
                if (configuration.networkId == wifiInfo.getNetworkId()) {
                    return configuration.SSID;
                }
            }
        }

        return SSID;
    }

    static boolean compareSSID(WifiManager wifiManager, WifiInfo wifiInfo, String SSID, List<WifiSSIDData> wifiConfigurationList)
    {
        String wifiInfoSSID = getSSID(wifiManager, wifiInfo, wifiConfigurationList);
        String ssid2 = "\"" + SSID + "\"";
        //return (wifiInfoSSID.equals(SSID) || wifiInfoSSID.equals(ssid2));
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%', true) || Wildcard.match(wifiInfoSSID, ssid2, '_', '%', true));
    }

    static String getSSID(WifiSSIDData result, List<WifiSSIDData> wifiConfigurationList)
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

        //PPApplication.logE("@@@x WifiScanJob.getSSID", "SSID="+SSID);

        return SSID;
    }

    static boolean compareSSID(WifiSSIDData result, String SSID, List<WifiSSIDData> wifiConfigurationList)
    {
        String wifiInfoSSID = getSSID(result, wifiConfigurationList);
        String ssid2 = "\"" + SSID + "\"";
        //PPApplication.logE("@@@x WifiScanJob.compareSSID", "wifiInfoSSID="+wifiInfoSSID);
        //PPApplication.logE("@@@x WifiScanJob.compareSSID", "ssid2="+ssid2);

        //return (getSSID(result).equals(SSID) || getSSID(result).equals(ssid2));
        return (Wildcard.match(wifiInfoSSID, SSID, '_', '%', true) || Wildcard.match(wifiInfoSSID, ssid2, '_', '%', true));
    }

}
