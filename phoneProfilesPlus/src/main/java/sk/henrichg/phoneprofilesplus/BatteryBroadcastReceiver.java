package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BatteryBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, BatteryService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(BatteryManager.EXTRA_STATUS, intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
        serviceIntent.putExtra(BatteryManager.EXTRA_LEVEL, intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
        serviceIntent.putExtra(BatteryManager.EXTRA_SCALE, intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);
    }
}
