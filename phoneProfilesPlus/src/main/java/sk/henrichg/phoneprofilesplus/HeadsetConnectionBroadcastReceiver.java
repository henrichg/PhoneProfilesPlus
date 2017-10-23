package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetConnectionBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_HEADSET_PLUG_STATE = "state";
    static final String EXTRA_HEADSET_PLUG_MICROPHONE = "microphone";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### HeadsetConnectionBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "HeadsetConnectionBroadcastReceiver.onReceive", "HeadsetConnectionBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_HEADSET_PLUG))
            HeadsetConnectionJob.startForHeadsetPlug(appContext, intent.getIntExtra("state", -1), intent.getIntExtra("microphone", -1));
        else
        if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))
            HeadsetConnectionJob.startForBluetoothPlug(appContext, BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED));
        else
        if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
            HeadsetConnectionJob.startForBluetoothPlug(appContext, BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED));

    }
}
