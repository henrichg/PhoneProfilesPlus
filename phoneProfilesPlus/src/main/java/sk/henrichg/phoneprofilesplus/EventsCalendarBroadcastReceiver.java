package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class EventsCalendarBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "eventsCalendar";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("#### EventsCalendarBroadcastReceiver.onReceive","xxx");
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{
			GlobalData.logE("@@@ EventsCalendarBroadcastReceiver.onReceive","xxx");

			boolean calendarEventsExists = false;
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
			GlobalData.logE("EventsCalendarBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
			dataWrapper.invalidateDataWrapper();

			if (calendarEventsExists)
			{
				// start service
				Intent eventsServiceIntent = new Intent(context, EventsService.class);
				eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
				startWakefulService(context, eventsServiceIntent);
			}
			
		}
		
	}

}
