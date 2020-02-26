package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class WifiAPStateChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "WifiAPStateChangeBroadcastReceiver.onReceive", "WifiAPStateChangeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning())
        {
            boolean isWifiAPEnabled;
            if (Build.VERSION.SDK_INT < 28)
                isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
            else
                isWifiAPEnabled = CmdWifiAP.isEnabled();
            if (isWifiAPEnabled) {
                // Wifi AP is enabled - cancel wifi scan work
                //PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                WifiScanWorker.cancelWork(appContext, true,null);
            }
            else {
                // Wifi AP is disabled - schedule wifi scan work
                //PPApplication.logE("[RJS] WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().scheduleWifiWorker(true,  true, /*false, true, false,*/ false);
            }
        }
    }

}
