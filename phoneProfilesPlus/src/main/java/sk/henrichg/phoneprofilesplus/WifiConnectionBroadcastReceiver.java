package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiConnectionBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, WifiService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(WifiManager.EXTRA_NETWORK_INFO, intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);

    }
}
