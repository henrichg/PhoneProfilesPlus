package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiScanBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiScan";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive", "----- start");

        if (WifiScanAlarmBroadcastReceiver.wifi == null)
            WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        int forceOneScan = PPApplication.getForceOneWifiScan(context);
        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "forceOneScan="+forceOneScan);

        if (PPApplication.getGlobalEventsRuning(context) || (forceOneScan == PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
            //PPApplication.logE("$$$ WifiAP", "WifiScanBroadcastReceiver.onReceive-isWifiAPEnabled="+isWifiAPEnabled);

            //PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

            WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList(context);
            //if ((android.os.Build.VERSION.SDK_INT < 23) || (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)))
                WifiScanAlarmBroadcastReceiver.fillScanResults(context);
            //WifiScanAlarmBroadcastReceiver.unlock();

            /*
            List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(context);
            if (scanResults != null) {
                PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "scanResults.size="+scanResults.size());
                //for (WifiSSIDData result : scanResults) {
                //    PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "result.SSID=" + result.ssid);
                //}
            }
            else
                PPApplication.logE("$$$ WifiScanBroadcastReceiver.onReceive", "scanResults=null");
            */

            boolean scanStarted = (WifiScanAlarmBroadcastReceiver.getWaitForResults(context));

            if (scanStarted)
            {
                PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanStarted");

                /*
                if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context))
                {
                    PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive","disable wifi");
                    WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                    // not call this, due WifiConnectionBroadcastReceiver
                    //WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                }
                */

                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);

                PPApplication.setForceOneWifiScan(context, PPApplication.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                {
                    // start service
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                }
            }

        }

        PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive","----- end");

    }

}
