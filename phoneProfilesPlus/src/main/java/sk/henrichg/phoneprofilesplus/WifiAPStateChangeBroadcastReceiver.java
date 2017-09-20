package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WifiAPStateChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiAPStateChangeBroadcastReceiver.onReceive", "WifiAPStateChangeBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        try {
            Intent serviceIntent = new Intent(context, WifiAPStateChangeService.class);
            WakefulIntentService.sendWakefulWork(context, serviceIntent);
        } catch (Exception ignored) {}
    }

}
