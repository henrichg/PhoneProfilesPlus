package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

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
            boolean isWifiAPEnabled;
            if (Build.VERSION.SDK_INT < 28)
                isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
            else
                isWifiAPEnabled = CmdWifiAP.isEnabled();
            if (isWifiAPEnabled) {
                // Wifi AP is enabled - cancel wifi scan job
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                WifiScanJob.cancelJob(appContext, true,null);
            }
            else {
                // Wifi AP is disabled - schedule wifi scan job
                PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().scheduleWifiJob(true,  true, /*false, true, false,*/ false);
            }
        }
    }

}
