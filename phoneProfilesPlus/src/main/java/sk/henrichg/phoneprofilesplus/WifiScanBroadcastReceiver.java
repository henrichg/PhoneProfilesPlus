package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;

public class WifiScanBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiScan";

    @Override
    public void onReceive(Context context, Intent intent) {

        //GlobalData.logE("#### WifiScanBroadcastReceiver.onReceive","xxx");
        GlobalData.logE("@@@ WifiScanBroadcastReceiver.onReceive","----- start");

        if (WifiScanAlarmBroadcastReceiver.wifi == null)
            WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {

            boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
            GlobalData.logE("$$$ WifiAP", "WifiScanBroadcastReceiver.onReceive-isWifiAPEnabled="+isWifiAPEnabled);

            boolean scanStarted = (WifiScanAlarmBroadcastReceiver.getWaitForResults(context));

            if (scanStarted)
            {
                GlobalData.logE("$$$ WifiScanBroadcastReceiver.onReceive","xxx");

                WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList(context);
                WifiScanAlarmBroadcastReceiver.fillScanResults(context);
                //WifiScanAlarmBroadcastReceiver.unlock();

                /*
                List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(context);
                if (scanResults != null)
                {
                    for (WifiSSIDData result : scanResults)
                    {
                        GlobalData.logE("@@@ WifiScanBroadcastReceiver.onReceive","result.SSID="+result.ssid);
                    }
                }
                */

                /*
                if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context))
                {
                    GlobalData.logE("@@@ WifiScanBroadcastReceiver.onReceive","disable wifi");
                    WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                    // not call this, due WifiConnectionBroadcastReceiver
                    //WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                }
                */

                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);

                boolean forceOneScan = GlobalData.getForceOneWifiScan(context);
                GlobalData.setForceOneWifiScan(context, false);

                if (!forceOneScan) // not start service for force scan
                {
                    // start service
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                }

            }

        }

        GlobalData.logE("@@@ WifiScanBroadcastReceiver.onReceive","----- end");

    }

}
