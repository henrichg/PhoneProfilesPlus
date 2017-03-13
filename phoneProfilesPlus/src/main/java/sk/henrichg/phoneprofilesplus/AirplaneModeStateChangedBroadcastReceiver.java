package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        final String action = intent.getAction();

        if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            final boolean state = intent.getBooleanExtra("state", false);

            Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
            broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_AIRPLANE_MODE);
            broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
            context.sendBroadcast(broadcastIntent);

        }
    }
}
