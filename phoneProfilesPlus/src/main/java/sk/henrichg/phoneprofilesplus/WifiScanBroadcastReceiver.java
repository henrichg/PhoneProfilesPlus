package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ WifiScanBroadcastReceiver.onReceive", "----- start");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, WifiService.class);
        serviceIntent.setAction(intent.getAction());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            serviceIntent.putExtra(WifiManager.EXTRA_RESULTS_UPDATED, intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true));
        }
        WakefulIntentService.sendWakefulWork(context, serviceIntent);

    }

}
