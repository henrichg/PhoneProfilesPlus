package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothScanBroadcastReceiver.onReceive","xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, BluetoothService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        serviceIntent.putExtra(BluetoothDevice.EXTRA_NAME, intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);
    }

}
