package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

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
                //EventsHandlerJob.startForSensor(context.getApplicationContext(), EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                final Context appContext = context.getApplicationContext();
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false);
                    }
                });
            }
        }
    }
}
