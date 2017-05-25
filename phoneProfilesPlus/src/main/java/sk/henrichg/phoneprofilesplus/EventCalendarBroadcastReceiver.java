package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class EventCalendarBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "eventCalendarStart";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### EventCalendarBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ EventCalendarBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("EventCalendarBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(appContext, eventsServiceIntent);
            //}

        }

    }

}
