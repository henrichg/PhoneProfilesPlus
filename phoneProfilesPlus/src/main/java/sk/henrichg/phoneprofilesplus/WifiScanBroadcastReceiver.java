package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

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

        //PPApplication.loadPreferences(context);

        int forceOneScan = ScannerService.getForceOneWifiScan(context);
        PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "forceOneScan="+forceOneScan);

        if (Event.getGlobalEventsRuning(context) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
            //PPApplication.logE("$$$ WifiAP", "WifiScanBroadcastReceiver.onReceive-isWifiAPEnabled="+isWifiAPEnabled);

            //PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

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
            PPApplication.logE("%%%% WifiScanBroadcastReceiver.onReceive", "scanStarted="+scanStarted);

            if (scanStarted)
            {
                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                ScannerService.setForceOneWifiScan(context, ScannerService.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                {
                    // start service
                    final Context _context = context.getApplicationContext();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LocalBroadcastManager.getInstance(_context).registerReceiver(PPApplication.startEventsServiceBroadcastReceiver, new IntentFilter("StartEventsServiceBroadcastReceiver"));
                            Intent startEventsServiceIntent = new Intent("StartEventsServiceBroadcastReceiver");
                            LocalBroadcastManager.getInstance(_context).sendBroadcast(startEventsServiceIntent);
                        }
                    }, 5000);
                    //setAlarm(context);
                }
            }

        }

        PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive","----- end");

    }

}
