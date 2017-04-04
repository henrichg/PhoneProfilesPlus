package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

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

                ScannerService.setForceOneWifiScan(context, ScannerService.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                {
                    // start service
                    //Intent _intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);
                    //context.sendBroadcast(_intent);
                    setAlarm(context);
                }
            }

        }

        PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive","----- end");

    }

    private void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint("NewApi")
    private void setAlarm(Context context)
    {
        removeAlarm(context);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);
        long alarmTime = calendar.getTimeInMillis();

        if (android.os.Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else if (android.os.Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

}
