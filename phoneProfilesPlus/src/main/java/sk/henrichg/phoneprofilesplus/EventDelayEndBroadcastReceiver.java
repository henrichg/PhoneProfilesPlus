package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class EventDelayEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### EventDelayEndBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "EventDelayEndBroadcastReceiver.onReceive", "EventDelayEndBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ EventDelayEndBroadcastReceiver.onReceive","xxx");

            // start job
            //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_EVENT_DELAY_END);
            final Handler handler = new Handler(appContext.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_END, false);
                }
            });
        }

    }

}
