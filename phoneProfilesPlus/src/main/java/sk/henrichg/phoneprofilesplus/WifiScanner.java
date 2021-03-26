package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class WifiScanner {

    private final Context context;

    static final int WIFI_SCAN_DURATION = 25;      // 25 seconds for wifi scan

    //static boolean wifiEnabledForScan;

    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI = "show_enable_location_notification_wifi";

    WifiScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan() {
        synchronized (PPApplication.wifiScannerMutex) {
            //CallsCounter.logCounter(context, "WifiScanner.doScan", "Scanner_doScan");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return;

            //PPApplication.logE("%%%% WifiScanner.doScan", "-- START ------------");

            //DataWrapper dataWrapper;

            //PPApplication.logE("%%%% WifiScanner.doScan", "scannerType=" + scannerType);

            // for Airplane mode ON, no scan
            //if (android.os.Build.VERSION.SDK_INT >= 17) {
                if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
                    //PPApplication.logE("%%%% WifiScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            /*} else {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0) {
                    PPApplication.logE("%%%% WifiScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            }*/

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode) {
                int forceScan = ApplicationPreferences.prefForceOneWifiScan;
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventWifiScanInPowerSaveMode.equals("2")) {
                        // not scan wi-fi in power save mode
                        //PPApplication.logE("%%%% WifiScanner.doScan", "-- END - power save mode ON -------");
                        return;
                    }
                }
            }

            PPApplication.startHandlerThreadPPScanners(/*"WifiScanner.doScan.1"*/);
            final Handler wifiChangeHandler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());

            //synchronized (PPApplication.radioChangeStateMutex) {

                //PPApplication.logE("$$$W WifiScanner.doScan", "start wifi scan");

                WifiScanWorker.fillWifiConfigurationList(context/*, false*/);

                boolean canScan = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                if (canScan) {
                    if (!ApplicationPreferences.applicationEventWifiScanIgnoreHotspot) {
                        if (Build.VERSION.SDK_INT < 28)
                            canScan = !WifiApManager.isWifiAPEnabled(context);
                        else
                            canScan = !CmdWifiAP.isEnabled();
                    }
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$W WifiScanner.doScan", "canScan=" + canScan);
                        PPApplication.logE("$$$W WifiScanner.doScan", "isWifiAPEnabled=" + !canScan);
                    }*/
                }

                //PPApplication.logE("$$$W WifiScanner.doScan", "canScan="+canScan);

                if (canScan) {

                    //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                    // check if wifi scan events exists
                    //lock();
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    //unlock();
                    //int forceScan = ApplicationPreferences.prefForceOneWifiScan;
                    boolean scan; //(wifiEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    //if (scan) {
                    //    if (wifiEventsExists)
                            scan = isLocationEnabled(context/*, scannerType*/);
                    //}
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$W WifiScanner.doScan", "wifiEventsExists=" + wifiEventsExists);
                        PPApplication.logE("$$$W WifiScanner.doScan", "forceScan=" + forceScan);
                    }*/
                    if (!scan) {
                        // wifi scan events not exists
                        //PPApplication.logE("$$$W WifiScanner.doScan", "worker removed");
                        //PPApplication.logE("[RJS] WifiScanner.doScan", "worker removed");
                        WifiScanWorker.cancelWork(context, true/*, null*/);
                    } else {
                        //PPApplication.logE("$$$W WifiScanner.doScan", "can scan");

                        if (WifiScanWorker.wifi == null)
                            WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                        if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                            // service restarted during scanning, disable wifi
                            //PPApplication.logE("$$$W WifiScanner.doScan", "disable wifi - service restarted");
                            wifiChangeHandler.post(() -> {
                                try {
//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.1");
                                    if (WifiScanWorker.wifi == null)
                                        WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    //lock();
                                    //PPApplication.logE("#### setWifiEnabled", "from WifiScanner.doScan 1");
                                    //if (Build.VERSION.SDK_INT >= 29)
                                    //    CmdWifi.setWifi(false);
                                    //else
                                    if (WifiScanWorker.wifi != null)
                                        //noinspection deprecation
                                        WifiScanWorker.wifi.setWifiEnabled(false);
                                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            });
                            PPApplication.sleep(1000);
                            //unlock();
                        }

                        //noinspection ConstantConditions,ConstantIfStatement
                        if (true /*canScanWifi(dataWrapper)*/) { // scan even if wifi is connected

                            //PPApplication.logE("$$$W WifiScanner.doScan", "scan started");

                            WifiScanWorker.setScanRequest(context, false);
                            WifiScanWorker.setWaitForResults(context, false);
                            WifiScanWorker.setWifiEnabledForScan(context, false);

                            WifiScanWorker.unlock();

                            // start scan

                            //lock();

                            // enable wifi
                            int wifiState;
                            wifiState = enableWifi(WifiScanWorker.wifi, wifiChangeHandler);

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                //PPApplication.logE("$$$W WifiScanner.doScan", "startScan");
                                WifiScanWorker.startScan(context);
                            } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                                WifiScanWorker.setScanRequest(context, false);
                                WifiScanWorker.setWaitForResults(context, false);
                                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                            }

                            //PPApplication.logE("$$$W WifiScanner.doScan", "ApplicationPreferences.prefEventWifiScanRequest="+ApplicationPreferences.prefEventWifiScanRequest);
                            //PPApplication.logE("$$$W WifiScanner.doScan", "ApplicationPreferences.prefEventWifiWaitForResult="+ApplicationPreferences.prefEventWifiWaitForResult);

                            if (ApplicationPreferences.prefEventWifiScanRequest ||
                                    ApplicationPreferences.prefEventWifiWaitForResult) {
                                //PPApplication.logE("$$$W WifiScanner.doScan", "waiting for scan end");

                                // wait for scan end
                                waitForWifiScanEnd(/*context*/);

                                //PPApplication.logE("$$$W WifiScanner.doScan", "scan ended");

                                //PPApplication.logE("$$$W WifiScanner.doScan", "ApplicationPreferences.prefEventWifiWaitForResult="+ApplicationPreferences.prefEventWifiWaitForResult);
                                if (ApplicationPreferences.prefEventWifiWaitForResult) {
                                    //PPApplication.logE("$$$W WifiScanner.doScan", "no data received from scanner");
                                    if (ApplicationPreferences.prefForceOneWifiScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {
                                        Data workData = new Data.Builder()
                                                .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                                .build();

                                        OneTimeWorkRequest worker =
                                                new OneTimeWorkRequest.Builder(MainWorker.class)
                                                        .addTag(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG)
                                                        .setInputData(workData)
                                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                        .build();
                                        try {
                                            if (PPApplication.getApplicationStarted(true)) {
                                                WorkManager workManager = PPApplication.getWorkManagerInstance();
                                                if (workManager != null) {

//                                                    //if (PPApplication.logEnabled()) {
//                                                    ListenableFuture<List<WorkInfo>> statuses;
//                                                    statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG);
//                                                    try {
//                                                        List<WorkInfo> workInfoList = statuses.get();
//                                                        PPApplication.logE("[TEST BATTERY] WifiScanner.doScan", "for=" + MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                                    } catch (Exception ignored) {
//                                                    }
//                                                    //}

//                                                    PPApplication.logE("[WORKER_CALL] WifiScanner.doScan", "xxx");
                                                    //workManager.enqueue(worker);
                                                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG, ExistingWorkPolicy./*APPEND_OR_*/REPLACE, worker);
                                                }
                                            }
                                        } catch (Exception e) {
                                            PPApplication.recordException(e);
                                        }

                                        /*PPApplication.startHandlerThread("WifiScanner.doScan");
                                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                                                PowerManager.WakeLock wakeLock = null;
                                                try {
                                                    if (powerManager != null) {
                                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanner_doScan");
                                                        wakeLock.acquire(10 * 60 * 1000);
                                                    }

                                                    // start events handler
                                                    EventsHandler eventsHandler = new EventsHandler(context);
                                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                                                } finally {
                                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                                        try {
                                                            wakeLock.release();
                                                        } catch (Exception ignored) {}
                                                    }
                                                }
                                            }
                                        }, 5000);*/
                                        //PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER, 5, context);
                                    }
                                }
                            }

                            WifiScanWorker.unlock();
                            //unlock();
                        }
                    }

                    wifiChangeHandler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.2");

                        if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                            try {
                                if (WifiScanWorker.wifi == null)
                                    WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                //PPApplication.logE("$$$W WifiScanner.doScan", "disable wifi");
                                //lock();
                                //PPApplication.logE("#### setWifiEnabled", "from WifiScanner.doScan 2");
                                //if (Build.VERSION.SDK_INT >= 29)
                                //    CmdWifi.setWifi(false);
                                //else
                                if (WifiScanWorker.wifi != null)
                                    //noinspection deprecation
                                    WifiScanWorker.wifi.setWifiEnabled(false);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        } //else
                            //PPApplication.logE("$$$W WifiScanner.doScan", "keep enabled wifi");

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                    });
                    PPApplication.sleep(1000);
                    //unlock();
                }

                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                WifiScanWorker.setWaitForResults(context, false);
                WifiScanWorker.setScanRequest(context, false);

                WifiScanWorker.unlock();
                //unlock();

                //PPApplication.logE("$$$ WifiScanner.doScan", "in synchronized block - end - scannerType=" + scannerType);

            //}

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ WifiScanner.doScan", "after synchronized block - scannerType=" + scannerType);

                PPApplication.logE("%%%% WifiScanner.doScan", "-- END ------------");
            }*/
        }
    }

    static void getForceOneWifiScan(Context context)
    {
        synchronized (PPApplication.eventWifiSensorMutex) {
            ApplicationPreferences.prefForceOneWifiScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneWifiScan(Context context, int forceScan)
    {
        synchronized (PPApplication.eventWifiSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneWifiScan = forceScan;
        }
    }

    /*
    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire(10 * 60 * 1000);
            PPApplication.logE("$$$ WifiScanner.lock","xxx");
        } catch(Exception e) {
            Log.e("WifiScanner.lock", "Error getting Lock: ", e);
            PPApplication.logE("$$$ WifiScanner.lock", "Error getting Lock: " + e.getMessage());
        }
    }

    private void unlock() {
        if ((wakeLock != null) && (wakeLock.isHeld())) {
            PPApplication.logE("$$$ WifiScanner.unlock","xxx");
            wakeLock.release();
        }
    }
    */

    @SuppressLint("NewApi")
    private int enableWifi(WifiManager wifi, Handler wifiChangeHandler)
    {
        //PPApplication.logE("@@@ WifiScanner.enableWifi","xxx");

        int wifiState = wifi.getWifiState();
        int forceScan = ApplicationPreferences.prefForceOneWifiScan;

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
        if (wifiState != WifiManager.WIFI_STATE_ENABLING)
        {
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            boolean isScanAlwaysAvailable = false;
            if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                // this must be disabled because scanning not working, when wifi is disabled after disabled WiFi AP
                // Tested and scanning working ;-)
                //if (android.os.Build.VERSION.SDK_INT >= 18)
                    //noinspection deprecation
                    isScanAlwaysAvailable = wifi.isScanAlwaysAvailable();
            }
            //PPApplication.logE("@@@ WifiScanner.enableWifi","isScanAlwaysAvailable="+isScanAlwaysAvailable);
            isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
            if (!isWifiEnabled)
            {
                boolean applicationEventWifiScanIfWifiOff = ApplicationPreferences.applicationEventWifiScanIfWifiOff;
                if (applicationEventWifiScanIfWifiOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
                {
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    boolean scan = ((/*wifiEventsExists &&*/ applicationEventWifiScanIfWifiOff) ||
                            (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    if (scan)
                    {
                        WifiScanWorker.setWifiEnabledForScan(context, true);
                        WifiScanWorker.setScanRequest(context, true);
                        WifiScanWorker.lock(context);
                        final WifiManager _wifi = wifi;
                        //PPApplication.logE("[HANDLER] WifiScanner.enableWifi", "before start handler");
                        wifiChangeHandler.post(() -> {
                            //if (PPApplication.logEnabled()) {
//                                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiScanner.enableWifi");

                                //PPApplication.logE("$$$ WifiScanner.enableWifi", "before enable wifi");
                                //PPApplication.logE("[HANDLER] WifiScanner.enableWifi", "before enable wifi");
                                //PPApplication.logE("#### setWifiEnabled", "from WifiScanner.enableWifi");
                            //}

                            //if (Build.VERSION.SDK_INT >= 29)
                            //    CmdWifi.setWifi(true);
                            //else
                                //noinspection deprecation
                                _wifi.setWifiEnabled(true);

                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("$$$ WifiScanner.enableWifi", "after enable wifi");

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                            }*/
                        });
                        //PPApplication.logE("@@@ WifiScanner.enableWifi","set enabled");
                        return WifiManager.WIFI_STATE_ENABLING;
                    }
                }
            }
            else
            {
                // this is not needed, enableWifi() is called only from doScan and after when hotspot is disabled
                /*boolean isWifiAPEnabled = false;
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(context);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    if (wifiApManager != null)
                        isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                }
                else
                    isWifiAPEnabled = CmdWifiAP.isEnabled();*/

                if (isScanAlwaysAvailable/*  && !isWifiAPEnabled*/) {
                    //PPApplication.logE("@@@ WifiScanner.enableWifi", "scan always available");
                    wifiState =  WifiManager.WIFI_STATE_ENABLED;
                }
                return wifiState;
            }
        }
        //}

        return wifiState;
    }

    private static void waitForWifiScanEnd(/*Context context*//*, AsyncTask<Void, Integer, Void> asyncTask*/)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                    ApplicationPreferences.prefEventWifiWaitForResult)) {
                break;
            }
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            PPApplication.sleep(500);
        } while (SystemClock.uptimeMillis() - start < WIFI_SCAN_DURATION * 1000);
    }

    private static boolean isLocationEnabled(Context context/*, String scanType*/) {
        //if (Build.VERSION.SDK_INT >= 23) {
            // check for Location Settings

            /* isScanAlwaysAvailable() may be disabled for unknown reason :-(
            //boolean isScanAlwaysAvailable = true;
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int wifiState = WifiScanWorker.wifi.getWifiState();
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            isScanAlwaysAvailable = isWifiEnabled || WifiScanWorker.wifi.isScanAlwaysAvailable();
            */

            //noinspection RedundantIfStatement
            if (!PhoneProfilesService.isLocationEnabled(context)/* || (!isScanAlwaysAvailable)*/) {
                // Location settings are not properly set, show notification about it

                /*
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context, scanType)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        notificationText = context.getString(R.string.phone_profiles_pref_category_wifi_scanning);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        if (android.os.Build.VERSION.SDK_INT < 24) {
                            nTitle = context.getString(R.string.ppp_app_name);
                            nText = notificationText + ": " + notificationBigText;
                        }
                        PPApplication.createExclamationNotificationChannel(context);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context, R.color.primary))
                                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                                .setContentTitle(nTitle) // title for notification
                                .setContentText(nText) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                        int requestCode;
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                        requestCode = 1;
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_TAG,
                                                        PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                        }

                        setShowEnableLocationNotification(context, false, scanType);
                    }
                }
                */

                return false;
            }
            else {
                /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true, scanType);*/
                return true;
            }

        /*}
        else {
            //setShowEnableLocationNotification(context, true, scanType);
            return true;
        }*/
    }

}
