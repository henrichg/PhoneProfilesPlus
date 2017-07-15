package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class HeadsetConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### HeadsetConnectionBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        String action = intent.getAction();

        Intent serviceIntent = new Intent(context, HeadsetConnectionService.class);
        serviceIntent.setAction(intent.getAction());
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            serviceIntent.putExtra("state", intent.getIntExtra("state", -1));
            serviceIntent.putExtra("microphone", intent.getIntExtra("microphone", -1));
        }
        else
        if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))
            serviceIntent.putExtra(BluetoothProfile.EXTRA_STATE, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED));
        else
        if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
            serviceIntent.putExtra(BluetoothProfile.EXTRA_STATE, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);
    }
}
