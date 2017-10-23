package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventCalendarBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### EventCalendarBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "EventCalendarBroadcastReceiver.onReceive", "EventCalendarBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ EventCalendarBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("EventCalendarBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                // start job
                EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_CALENDAR);
            //}

        }

    }

}
