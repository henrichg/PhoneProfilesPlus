package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CalendarProviderChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] CalendarProviderChangedBroadcastReceiver.onReceive", "xxx");

        //final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context))
        {
            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                final Context appContext = context.getApplicationContext();
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED, PPExecutors.SENSOR_NAME_SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED, 0);
            //}

        }

    }

}
