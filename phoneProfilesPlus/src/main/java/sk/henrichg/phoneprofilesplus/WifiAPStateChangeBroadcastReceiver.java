package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiAPStateChangeBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            // get Wi-Fi Hotspot state here
            //int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            //if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
            if (WifiApManager.isWifiAPEnabled(context)) {
                // Wifi AP is enabled
                GlobalData.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                // remove broadcast for one wifi scan
                WifiScanAlarmBroadcastReceiver.removeAlarm(context, true);
            }
            else {
                GlobalData.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                // send broadcast for one wifi scan
                WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
            }

        }

    }

}
