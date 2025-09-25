package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventTimeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST]  EventTimeBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM]  EventTimeBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {

            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return;

            if (EventStatic.getGlobalEventsRunning(context)) {
                final Context appContext = context.getApplicationContext();
//                Log.e("EventTimeBroadcastReceiver.onReceive", "call of events handler");
//                PPApplicationStatic.logE("[EXECUTOR_CALL] EventTimeBroadcastReceiver.onReceive", "PPExecutors.handleEvents");
                PPExecutors.handleEvents(appContext,
                        new int[]{EventsHandler.SENSOR_TYPE_TIME},
                        PPExecutors.SENSOR_NAME_SENSOR_TYPE_TIME, 0);
            }

        }
    }

}
