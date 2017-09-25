package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class CalendarProviderChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### CalendarProviderChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "CalendarProviderChangedBroadcastReceiver.onReceive", "CalendarProviderChangedBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

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
                // start service
                try {
                    Intent eventsServiceIntent = new Intent(appContext, EventsHandlerService.class);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                } catch (Exception ignored) {}
            //}

        }

    }

}
