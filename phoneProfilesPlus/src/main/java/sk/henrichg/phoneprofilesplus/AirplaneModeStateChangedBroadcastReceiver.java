package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "AirplaneModeStateChangedBroadcastReceiver.onReceive", "AirplaneModeStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(context)) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                final boolean state = intent.getBooleanExtra("state", false);

                try {
                    Intent eventsServiceIntent = new Intent(context, EventsHandlerService.class);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_AIRPLANE_MODE);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
                    WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);
                } catch (Exception ignored) {}
            }
        }
    }
}
