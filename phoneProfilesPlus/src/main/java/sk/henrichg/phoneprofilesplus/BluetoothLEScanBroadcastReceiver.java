package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothLEScanBroadcastReceiver.onReceive", "BluetoothLEScanBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        BluetoothJob.startForLEScanBroadcast();
    }

}
