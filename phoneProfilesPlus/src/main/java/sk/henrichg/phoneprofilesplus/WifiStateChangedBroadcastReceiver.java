package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent == null)
            return;

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                // WifiStateChangedBroadcastReceiver

                if (WifiScanWorker.wifi == null)
                    WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                final int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                //PPApplication.startHandlerThreadBroadcast(/*"WifiStateChangedBroadcastReceiver.onReceive.1"*/);
                //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
                //__handler.post(() -> {
                Runnable __runnable = () -> {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.1");

                    //Context appContext= appContextWeakRef.get();

                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiStateChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                                WifiScanWorker.fillWifiConfigurationList(appContext/*, false*/);

                                if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                                        ApplicationPreferences.prefEventWifiWaitForResult ||
                                        ApplicationPreferences.prefEventWifiEnabledForScan)) {
                                    // ignore for wifi scanning

                                    if (PhoneProfilesService.getInstance() != null) {
                                        if (!PhoneProfilesService.getInstance().connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                            if (wifiManager != null) {
                                                List<WifiConfiguration> list = null;
                                                if (Permissions.hasPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION))
                                                    list = wifiManager.getConfiguredNetworks();
                                                if (list != null) {
                                                    for (WifiConfiguration i : list) {
                                                        if (i.SSID != null && i.SSID.equals(PhoneProfilesService.getInstance().connectToSSID)) {
                                                            //wifiManager.disconnect();
                                                            wifiManager.enableNetwork(i.networkId, true);
                                                            //wifiManager.reconnect();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //else {
                                        //    WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                        //    wifiManager.disconnect();
                                        //    wifiManager.reconnect();
                                        //}
                                    }
                                }
                            }

                            int forceOneScan = ApplicationPreferences.prefForceOneWifiScan;

                            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                                if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED)) {
                                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                        // start scan
                                        if (ApplicationPreferences.prefEventWifiScanRequest) {
//                                            PPApplication.logE("[EXECUTOR_CALL]  ***** WifiStateChangedBroadcastReceiver.onReceive", "schedule");

                                            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                                            Runnable runnable = () -> {
//                                                long start = System.currentTimeMillis();
//                                                PPApplication.logE("[IN_EXECUTOR]  ***** WifiStateChangedBroadcastReceiver.onReceive", "--------------- START");
                                                WifiScanWorker.startScan(appContext);
//                                                long finish = System.currentTimeMillis();
//                                                long timeElapsed = finish - start;
//                                                PPApplication.logE("[IN_EXECUTOR]  ***** WifiStateChangedBroadcastReceiver.onReceive", "--------------- END - timeElapsed="+timeElapsed);
                                                //worker.shutdown();
                                            };
                                            PPApplication.createDelayedEventsHandlerExecutor();
                                            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);


                                            /*
                                            OneTimeWorkRequest worker =
                                                    new OneTimeWorkRequest.Builder(MainWorker.class)
                                                            .addTag(WifiScanWorker.WORK_TAG_START_SCAN)
                                                            .setInitialDelay(5, TimeUnit.SECONDS)
                                                            .build();
                                            try {
                                                if (PPApplication.getApplicationStarted(true)) {
                                                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                                                    if (workManager != null) {

//                                                        //if (PPApplication.logEnabled()) {
//                                                        ListenableFuture<List<WorkInfo>> statuses;
//                                                        statuses = workManager.getWorkInfosForUniqueWork(WifiScanWorker.WORK_TAG_START_SCAN);
//                                                        try {
//                                                            List<WorkInfo> workInfoList = statuses.get();
//                                                        } catch (Exception ignored) {
//                                                        }
//                                                        //}

//                                                        PPApplication.logE("[WORKER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "xxx");
                                                        workManager.enqueueUniqueWork(WifiScanWorker.WORK_TAG_START_SCAN, ExistingWorkPolicy.REPLACE, worker);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                            */
                                        }
                                    }

                                    if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                                            ApplicationPreferences.prefEventWifiWaitForResult ||
                                            ApplicationPreferences.prefEventWifiEnabledForScan)) {

                                        // start events handler

//                                        PPApplication.logE("[EVENTS_HANDLER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                        // start events handler

//                                        PPApplication.logE("[EVENTS_HANDLER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_WIFI_STATE");
                                        eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_STATE);
                                    }
                                }
                            }

                        } catch (Exception e) {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                }; //);
                PPApplication.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(__runnable);
            }
        }

    }

}
