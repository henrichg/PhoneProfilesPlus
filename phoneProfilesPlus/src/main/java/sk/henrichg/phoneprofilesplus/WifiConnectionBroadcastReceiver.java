package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiConnectionBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiConnectionBroadcastReceiver.onReceive", "WifiConnectionBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        WifiJob.startForConnectionBroadcast(appContext, intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
    }
}
