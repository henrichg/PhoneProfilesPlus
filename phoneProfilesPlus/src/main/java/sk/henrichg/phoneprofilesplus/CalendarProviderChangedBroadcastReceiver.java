package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class CalendarProviderChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### CalendarProviderChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ CalendarProviderChangedBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("CalendarProviderChangedBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}

        }

    }

}
