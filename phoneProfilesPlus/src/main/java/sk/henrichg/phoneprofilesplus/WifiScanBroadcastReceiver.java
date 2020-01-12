package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive", "----- start");
        //CallsCounter.logCounter(context, "WifiScanBroadcastReceiver.onReceive", "WifiScanBroadcastReceiver_onReceive");

        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (WifiBluetoothScanner.getForceOneWifiScan(appContext) != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventWifiEnableScanning(appContext))
                // scanning is disabled
                return;
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // WifiScanBroadcastReceiver

                boolean resultsUpdated = true;
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED exists");
                        resultsUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
                    }
                    else {
                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED NOT exists");
                        resultsUpdated = false;
                    }
                }
                final boolean scanResultsUpdated = resultsUpdated;
                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanResultsUpdated=" + scanResultsUpdated);

                if (WifiScanWorker.wifi == null)
                    WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                final int forceOneScan = WifiBluetoothScanner.getForceOneWifiScan(appContext);
                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "forceOneScan=" + forceOneScan);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                    PPApplication.startHandlerThread("WifiScanBroadcastReceiver.onReceive.1");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanBroadcastReceiver_onReceive_1");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiScanBroadcastReceiver.onReceive.1");

                                boolean scanStarted = (WifiScanWorker.getWaitForResults(appContext));
                                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanStarted=" + scanStarted);

                                //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                                //PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "isWifiAPEnabled="+isWifiAPEnabled);

                                //PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

                                if (scanResultsUpdated)
                                    WifiScanWorker.fillScanResults(appContext);

                                List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(appContext);
                                if (PPApplication.logEnabled()) {
                                    if (scanResults != null) {
                                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanResults.size=" + scanResults.size());
                                        for (WifiSSIDData result : scanResults) {
                                            PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "result.SSID=" + result.ssid);
                                        }
                                    } else
                                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanResults=null");
                                }

                                if (scanStarted) {
                                    WifiScanWorker.setWaitForResults(appContext, false);
                                    WifiBluetoothScanner.setForceOneWifiScan(appContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                                    if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {
                                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "start EventsHandler (1)");

                                        Data workData = new Data.Builder()
                                                .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_HANDLE_EVENTS)
                                                .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                                .build();

                                        OneTimeWorkRequest worker =
                                                new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                        .addTag("handleEventsWifiScannerFromReceiverWork")
                                                        .setInputData(workData)
                                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                                        .build();
                                        try {
                                            WorkManager workManager = WorkManager.getInstance(appContext);
                                            workManager.enqueueUniqueWork("handleEventsWifiScannerFromReceiverWork", ExistingWorkPolicy.REPLACE, worker);
                                        } catch (Exception ignored) {}

                                        /*PPApplication.startHandlerThread("WifiScanBroadcastReceiver.onReceive.2");
                                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                                PowerManager.WakeLock wakeLock = null;
                                                try {
                                                    if (powerManager != null) {
                                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanBroadcastReceiver_onReceive_2");
                                                        wakeLock.acquire(10 * 60 * 1000);
                                                    }

                                                    // start events handler
                                                    EventsHandler eventsHandler = new EventsHandler(appContext);
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
                                        //PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER, 5, appContext);
                                    }
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiScanBroadcastReceiver.onReceive.1");
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

}
