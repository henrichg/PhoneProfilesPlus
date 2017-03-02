package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AirplaneModeStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "airplaneModeState";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        final String action = intent.getAction();

        if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            final boolean state = intent.getBooleanExtra("state", false);

            Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
            broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_AIRPLANE_MODE);
            broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
            context.sendBroadcast(broadcastIntent);

        }
    }
}
