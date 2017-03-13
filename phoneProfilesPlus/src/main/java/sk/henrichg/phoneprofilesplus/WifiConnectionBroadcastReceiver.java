package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiConnectionBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiConnection";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiConnectionBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (info != null)
        {
            PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive","state="+info.getState());

            if (PhoneProfilesService.connectToSSIDStarted) {
                // connect to SSID is not started

                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifiInfo.getSSID()="+wifiInfo.getSSID());
                    PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "PhoneProfilesService.connectToSSID="+PhoneProfilesService.connectToSSID);
                    //if ((PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) ||
                    //    (wifiInfo.getSSID().equals(PhoneProfilesService.connectToSSID)))
                        PhoneProfilesService.connectToSSIDStarted = false;
                }
            }

            if (PPApplication.getGlobalEventsRuning(context))
            {
                if ((info.getState() == NetworkInfo.State.CONNECTED) ||
                    (info.getState() == NetworkInfo.State.DISCONNECTED))
                {
                    if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                            (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                            (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)))) {
                        // wifi is not scanned

                        PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is not scanned");

                        if (!PhoneProfilesService.connectToSSIDStarted) {
                            // connect to SSID is not started

                            // start service
                            Intent eventsServiceIntent = new Intent(context, EventsService.class);
                            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                            startWakefulService(context, eventsServiceIntent);

                        }
                    } else
                        PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is scanned");
                }
            }
        }
    }
}
