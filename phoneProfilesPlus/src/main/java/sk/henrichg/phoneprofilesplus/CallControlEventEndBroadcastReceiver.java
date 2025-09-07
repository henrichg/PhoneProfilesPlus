package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallControlEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] CallControlEventEndBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] CallControlEventEndBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            final Context appContext = context.getApplicationContext();
            PPApplicationStatic.logE("[EXECUTOR_CALL] CallControlEventEndBroadcastReceiver.doWork", "PPExecutors.handleEvents");
            PPExecutors.handleEvents(appContext,
                    new int[]{EventsHandler.SENSOR_TYPE_CALL_CONTROL_EVENT_END},
                    PPExecutors.SENSOR_NAME_SENSOR_TYPE_CALL_CONTROL_EVENT_END, 0);
        }
    }

}
