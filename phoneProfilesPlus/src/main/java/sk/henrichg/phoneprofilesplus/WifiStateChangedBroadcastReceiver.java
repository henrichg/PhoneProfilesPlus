package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, WifiService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(WifiManager.EXTRA_WIFI_STATE, intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);

    }

}
