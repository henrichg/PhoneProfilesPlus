package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import java.util.List;

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

        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            // WifiScanBroadcastReceiver

            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

            final int forceOneScan = Scanner.getForceOneWifiScan(appContext);
            PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "forceOneScan="+forceOneScan);

            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
            {
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean scanStarted = (WifiScanJob.getWaitForResults(appContext));
                        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanStarted="+scanStarted);

                        //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                        //PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "isWifiAPEnabled="+isWifiAPEnabled);

                        //PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

                        //if ((android.os.Build.VERSION.SDK_INT < 23) || (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)))
                        WifiScanJob.fillScanResults(appContext);
                        //WifiScanJobBroadcastReceiver.unlock();

                        List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(appContext);
                        if (scanResults != null) {
                            //PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "scanResults.size="+scanResults.size());
                            for (WifiSSIDData result : scanResults) {
                                PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "result.SSID=" + result.ssid);
                            }
                        }
                        else
                            PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "scanResults=null");

                        if (scanStarted)
                        {
                            WifiScanJob.setWaitForResults(appContext, false);
                            Scanner.setForceOneWifiScan(appContext, Scanner.FORCE_ONE_SCAN_DISABLED);

                            if (forceOneScan != Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                            {
                                PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "start EventsHandler (1)");
                                // start job
                                new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "start EventsHandler (2)");
                                        //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER, false);
                                    }
                                }, 5000);
                            }
                        }
                    }
                });
            }
        }
    }

}
