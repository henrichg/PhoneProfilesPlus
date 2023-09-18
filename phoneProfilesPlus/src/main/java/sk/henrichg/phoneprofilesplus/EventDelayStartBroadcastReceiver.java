package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventDelayStartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EventDelayStartBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] EventDelayStartBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            doWork(true, context);
        }
    }

    static void doWork(boolean useHandler, Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            final Context appContext = context.getApplicationContext();
            if (useHandler) {
                PPExecutors.handleEvents(appContext,
                        new int[]{EventsHandler.SENSOR_TYPE_EVENT_DELAY_START},
                        PPExecutors.SENSOR_NAME_SENSOR_TYPE_EVENT_DELAY_START, 0);
            } else {
//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] EventDelayStartBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_EVENT_DELAY_START (2)");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_EVENT_DELAY_START});
            }
        }
    }

}
