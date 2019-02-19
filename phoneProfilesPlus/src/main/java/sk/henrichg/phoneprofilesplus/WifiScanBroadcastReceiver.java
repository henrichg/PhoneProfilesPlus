package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive", "----- start");
        CallsCounter.logCounter(context, "WifiScanBroadcastReceiver.onReceive", "WifiScanBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (intent == null)
            return;

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            WifiJob.startForScanBroadcast(context.getApplicationContext(), intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true));
        else
            WifiJob.startForScanBroadcast(context.getApplicationContext());*/

        final Context appContext = context.getApplicationContext();

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

                if (WifiScanJob.wifi == null)
                    WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                final int forceOneScan = WifiBluetoothScanner.getForceOneWifiScan(appContext);
                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "forceOneScan=" + forceOneScan);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                    PPApplication.startHandlerThread("WifiScanBroadcastReceiver.onReceive.1");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiScanBroadcastReceiver.onReceive.1");

                                boolean scanStarted = (WifiScanJob.getWaitForResults(appContext));
                                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanStarted=" + scanStarted);

                                //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                                //PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "isWifiAPEnabled="+isWifiAPEnabled);

                                //PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

                                if (scanResultsUpdated)
                                    WifiScanJob.fillScanResults(appContext);

                                //WifiScanJobBroadcastReceiver.unlock();

                                List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(appContext);
                                if (scanResults != null) {
                                    PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanResults.size=" + scanResults.size());
                                    for (WifiSSIDData result : scanResults) {
                                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "result.SSID=" + result.ssid);
                                    }
                                } else
                                    PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanResults=null");

                                if (scanStarted) {
                                    WifiScanJob.setWaitForResults(appContext, false);
                                    WifiBluetoothScanner.setForceOneWifiScan(appContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                                    if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {
                                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "start EventsHandler (1)");
                                    /*// start job
                                    PPApplication.startHandlerThread("WifiScanBroadcastReceiver.onReceive.2");
                                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                            PowerManager.WakeLock wakeLock = null;
                                            try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiScanBroadcastReceiver.onReceive.Handler.postDelayed");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "start EventsHandler (2)");
                                            //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                                            } finaly (
                                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                                try {
                                                    wakeLock.release();
                                                } catch (Exception ignored) {}
                                            }
                                            }
                                        }
                                    }, 5000);*/
                                        PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER, 5, appContext);
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
