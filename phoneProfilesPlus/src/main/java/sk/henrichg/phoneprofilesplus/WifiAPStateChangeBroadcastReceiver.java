package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiAPStateChangeBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        if (PPApplication.getGlobalEventsRuning(context))
        {
            // get Wi-Fi Hotspot state here
            //int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            //if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
            if (WifiApManager.isWifiAPEnabled(context)) {
                // Wifi AP is enabled
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                // remove broadcast for one wifi scan
                WifiScanAlarmBroadcastReceiver.removeAlarm(context/*, true*/);
            }
            else {
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                // send broadcast for one wifi scan
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0)
                    WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
                dataWrapper.invalidateDataWrapper();
            }

        }

    }

}
