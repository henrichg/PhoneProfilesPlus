package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothConnectionBroadcastReceiver.onReceive", "BluetoothConnectionBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        BluetoothJob.startForConnectionBroadcast(appContext, intent.getAction(),
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE),
                intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
    }

}
