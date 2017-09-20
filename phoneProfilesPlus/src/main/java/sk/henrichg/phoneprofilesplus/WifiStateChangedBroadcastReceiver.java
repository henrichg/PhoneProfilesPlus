package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiStateChangedBroadcastReceiver.onReceive", "WifiStateChangedBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        try {
            Intent serviceIntent = new Intent(context, WifiService.class);
            serviceIntent.setAction(intent.getAction());
            serviceIntent.putExtra(WifiManager.EXTRA_WIFI_STATE, intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
            WakefulIntentService.sendWakefulWork(context, serviceIntent);
        } catch (Exception ignored) {}

    }

}
