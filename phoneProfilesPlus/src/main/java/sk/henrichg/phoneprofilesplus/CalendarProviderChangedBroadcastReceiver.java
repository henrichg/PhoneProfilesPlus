package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class CalendarProviderChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### CalendarProviderChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "CalendarProviderChangedBroadcastReceiver.onReceive", "CalendarProviderChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ CalendarProviderChangedBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("CalendarProviderChangedBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                // start job
                //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED);
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED, false);
                    }
                });
            //}

        }

    }

}
