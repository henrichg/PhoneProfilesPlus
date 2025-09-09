package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    final Context appContext = context.getApplicationContext();
//                    PPApplicationStatic.logE("[EXECUTOR_CALL] ActivatedProfileEventEndBroadcastReceiver.doWork", "PPExecutors.handleEvents");
                    PPExecutors.handleEvents(appContext,
                            new int[]{EventsHandler.SENSOR_TYPE_RADIO_SWITCH},
                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_RADIO_SWITCH, 0);
                }
            }
        }
    }
}
