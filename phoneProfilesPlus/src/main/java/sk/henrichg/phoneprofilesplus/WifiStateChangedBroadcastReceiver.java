package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "WifiStateChangedBroadcastReceiver.onReceive", "WifiStateChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
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
//                PPApplication.logE("[APP START] WifiStateChangedBroadcastReceiver.onReceive", "wifiState="+wifiState);

                PPApplication.startHandlerThreadBroadcast(/*"WifiStateChangedBroadcastReceiver.onReceive.1"*/);
                final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.1");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiStateChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                                //PPApplication.logE("WifiStateChangedBroadcastReceiver.onReceive.1", "fillWifiConfigurationList");
                                WifiScanWorker.fillWifiConfigurationList(appContext/*, false*/);

                                if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                                        ApplicationPreferences.prefEventWifiWaitForResult ||
                                        ApplicationPreferences.prefEventWifiEnabledForScan)) {
                                    // ignore for wifi scanning

                                    if (PhoneProfilesService.getInstance() != null) {
                                        if (!PhoneProfilesService.getInstance().connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                            if (wifiManager != null) {
                                                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
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
                            //PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "forceOneScan=" + forceOneScan);

                            if (Event.getGlobalEventsRunning() || (forceOneScan == WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                                //PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "state=" + wifiState);

                                if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED)) {
                                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                        // start scan
                                        if (ApplicationPreferences.prefEventWifiScanRequest) {
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
//                                                            PPApplication.logE("[TEST BATTERY] WifiStateChangedBroadcastReceiver.onReceive", "for=" + WifiScanWorker.WORK_TAG_START_SCAN + " workInfoList.size()=" + workInfoList.size());
//                                                        } catch (Exception ignored) {
//                                                        }
//                                                        //}

//                                                        PPApplication.logE("[WORKER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "xxx");
                                                        workManager.enqueueUniqueWork(WifiScanWorker.WORK_TAG_START_SCAN, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                        }
                                    }

                                    if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                                            ApplicationPreferences.prefEventWifiWaitForResult ||
                                            ApplicationPreferences.prefEventWifiEnabledForScan)) {

                                        // start events handler
                                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=WifiStateChangedBroadcastReceiver.onReceive (1)");

//                                        PPApplication.logE("[EVENTS_HANDLER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=WifiStateChangedBroadcastReceiver.onReceive (1)");

                                        // start events handler
                                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=WifiStateChangedBroadcastReceiver.onReceive (2)");

//                                        PPApplication.logE("[EVENTS_HANDLER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_WIFI_STATE");
                                        eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_STATE);

                                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=WifiStateChangedBroadcastReceiver.onReceive (2)");
                                    }
                                }
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiStateChangedBroadcastReceiver.onReceive.1");
                        } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
        }

    }

}
