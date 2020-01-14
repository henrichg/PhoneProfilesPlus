package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "WifiStateChangedBroadcastReceiver.onReceive", "WifiStateChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
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

                PPApplication.startHandlerThread("WifiStateChangedBroadcastReceiver.onReceive.1");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiStateChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.1");

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                                //PPApplication.logE("WifiStateChangedBroadcastReceiver.onReceive.1", "fillWifiConfigurationList");
                                WifiScanWorker.fillWifiConfigurationList(appContext/*, false*/);

                                if (!(WifiScanWorker.getScanRequest(appContext) ||
                                        WifiScanWorker.getWaitForResults(appContext) ||
                                        WifiScanWorker.getWifiEnabledForScan(appContext))) {
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

                            int forceOneScan = WifiBluetoothScanner.getForceOneWifiScan(appContext);
                            //PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "forceOneScan=" + forceOneScan);

                            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                                //PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "state=" + wifiState);

                                if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED)) {
                                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                        // start scan
                                        if (WifiScanWorker.getScanRequest(appContext)) {
                                            Data workData = new Data.Builder()
                                                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_START_WIFI_SCAN)
                                                    .build();

                                            OneTimeWorkRequest worker =
                                                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                            .addTag("startWifiScanWork")
                                                            .setInputData(workData)
                                                            .setInitialDelay(5, TimeUnit.SECONDS)
                                                            .build();
                                            try {
                                                WorkManager workManager = WorkManager.getInstance(appContext);
                                                workManager.enqueueUniqueWork("startWifiScanWork", ExistingWorkPolicy.REPLACE, worker);
                                            } catch (Exception ignored) {}

                                            /*PPApplication.startHandlerThread("WifiStateChangedBroadcastReceiver.onReceive.2");
                                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                                    PowerManager.WakeLock wakeLock = null;
                                                    try {
                                                        if (powerManager != null) {
                                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiStateChangedBroadcastReceiver_onReceive_Handler_2");
                                                            wakeLock.acquire(10 * 60 * 1000);
                                                        }

                                                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.2");

                                                        WifiScanWorker.startScan(appContext);

                                                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiStateChangedBroadcastReceiver.onReceive.2");
                                                    } finally {
                                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                                            try {
                                                                wakeLock.release();
                                                            } catch (Exception ignored) {}
                                                        }
                                                    }
                                                }
                                            }, 5000);*/
                                            //PostDelayedBroadcastReceiver.setAlarm(
                                            //        PostDelayedBroadcastReceiver.ACTION_START_WIFI_SCAN, 5, appContext);

                                        }/* else if (!WifiScanWorker.getWaitForResults(appContext)) {
                                            // refresh configured networks list
                                            PPApplication.startHandlerThread("WifiStateChangedBroadcastReceiver.onReceive.3");
                                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                                    PowerManager.WakeLock wakeLock = null;
                                                    try {
                                                        if (powerManager != null) {
                                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiStateChangedBroadcastReceiver_onReceive_Handler_3");
                                                            wakeLock.acquire(10 * 60 * 1000);
                                                        }

                                                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.3");

                                                        WifiScanWorker.fillWifiConfigurationList(appContext, false);

                                                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiStateChangedBroadcastReceiver.onReceive.3");
                                                    } finally {
                                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                                            try {
                                                                wakeLock.release();
                                                            } catch (Exception ignored) {
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }*/
                                    }

                                    if (!(WifiScanWorker.getScanRequest(appContext) ||
                                            WifiScanWorker.getWaitForResults(appContext) ||
                                            WifiScanWorker.getWifiEnabledForScan(appContext))) {

                                        // start events handler
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                        // start events handler
                                        eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_STATE);
                                    }
                                }
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiStateChangedBroadcastReceiver.onReceive.1");
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
