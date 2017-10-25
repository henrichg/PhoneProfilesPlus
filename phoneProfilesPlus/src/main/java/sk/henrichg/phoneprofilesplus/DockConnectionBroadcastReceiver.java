package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class DockConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DockConnectionBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "DockConnectionBroadcastReceiver.onReceive", "DockConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            boolean peripheralEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL) > 0;
            dataWrapper.invalidateDataWrapper();

            if (peripheralEventsExists)
            {*/
                // start job
                //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_DOCK_CONNECTION);
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DOCK_CONNECTION, false);
                    }
                });
            //}

        }
    }
}
