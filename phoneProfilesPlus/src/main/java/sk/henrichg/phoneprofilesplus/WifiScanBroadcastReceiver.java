package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive","xxx");

        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneWifiScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            boolean scanningPaused = ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2") &&
                    GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo);

//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ scanningPaused="+scanningPaused);
//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ applicationEventWifiEnableScanning="+ApplicationPreferences.applicationEventWifiEnableScanning);

            if ((!ApplicationPreferences.applicationEventWifiEnableScanning) || scanningPaused)
                // scanning is disabled
                return;
        }

        if (intent.getAction() != null) {
//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ intent.getAction()="+intent.getAction());
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                boolean resultsUpdated; // = false;
                if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
//                    PPApplicationStatic.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED exists");
                    resultsUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
//                    PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ resultsUpdated="+resultsUpdated);
                    WifiScanWorker.fillScanResults(appContext);
//                    PPApplicationStatic.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive","fillScanResults - end");
                }
                else {
//                    PPApplicationStatic.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED NOT exists");
                    resultsUpdated = true;
                }

                if (!resultsUpdated)
                    return;

                final int forceOneScan = ApplicationPreferences.prefForceOneWifiScan;
//                PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ forceOneScan="+forceOneScan);

                if (EventStatic.getGlobalEventsRunning(appContext) || (forceOneScan == WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                    if (ApplicationPreferences.prefEventWifiWaitForResult) {

//                        PPApplicationStatic.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "start Worker");

                        WifiScanWorker.setWaitForResults(appContext, false);
                        WifiScanner.setForceOneWifiScan(appContext, WifiScanner.FORCE_ONE_SCAN_DISABLED);

                        if (forceOneScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                        {
//                            PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ handleEvents");
//                            PPApplicationStatic.logE("[BLUETOOTH] WifiScanBroadcastReceiver.onReceive", "^^^^^^^^^ !!! is with delay 5 second !!!");
//                            Log.e("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "handle events for scan results");
                            PPExecutors.handleEvents(appContext,
                                    new int[]{EventsHandler.SENSOR_TYPE_WIFI_SCANNER},
                                    PPExecutors.SENSOR_NAME_SENSOR_TYPE_WIFI_SCANNER, 5);
                        }

                    }
                }
            }
        }
    }

}
