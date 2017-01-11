package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiState";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        if (WifiScanAlarmBroadcastReceiver.wifi == null)
            WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!GlobalData.getApplicationStarted(context, true))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

            if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
            {
                //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                //GlobalData.logE("$$$ WifiAP", "WifiStateChangedBroadcastReceiver.onReceive-isWifiAPEnabled="+isWifiAPEnabled);

                //if (!isWifiAPEnabled) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        // start scan
                        //if ((!dataWrapper.getIsManualProfileActivation()) || GlobalData.getForceOneWifiScan(context))
                        //{
                        if (WifiScanAlarmBroadcastReceiver.getScanRequest(context)) {
                            GlobalData.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "before startScan");
                            GlobalData.sleep(1000);
                            WifiScanAlarmBroadcastReceiver.startScan(context.getApplicationContext());
                            GlobalData.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "after startScan");
                        } else if (!WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) {
                            // refresh configured networks list
                            WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList(context);
                        }
                        //}
                    }

                    if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                            (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                            (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)))) {
                        // required for Wifi ConnectionType="Not connected"



                        /*boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED) > 0;
                        dataWrapper.invalidateDataWrapper();

                        if (wifiEventsExists) {
                            GlobalData.logE("@@@ WifiStateChangedBroadcastReceiver.onReceive", "wifiEventsExists=" + wifiEventsExists);
                        */
                            // start service
                            Intent eventsServiceIntent = new Intent(context, EventsService.class);
                            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                            startWakefulService(context, eventsServiceIntent);
                            //}
                    }
                //}
            }
        }

        /*if (wifiState == WifiManager.WIFI_STATE_DISABLED)
        {
            WifiScanAlarmBroadcastReceiver.stopScan(context);
        }*/

    }
}
