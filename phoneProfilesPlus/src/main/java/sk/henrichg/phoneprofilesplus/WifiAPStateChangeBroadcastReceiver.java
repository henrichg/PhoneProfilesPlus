package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WifiAPStateChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiAPStateChangeBroadcastReceiver.onReceive", "WifiAPStateChangeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            if (WifiApManager.isWifiAPEnabled(appContext)) {
                // Wifi AP is enabled
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                WifiScanJob.cancelJob(appContext, true,null);
            }
            else {
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                // send broadcast for one wifi scan
                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.scheduleWifiJob(true,  true, /*false, true, false,*/ false);
            }
        }
    }

}
