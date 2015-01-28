package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class EventsTimeBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "eventsTime";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("#### EventsTimeBroadcastReceiver.onReceive","xxx");
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{
			GlobalData.logE("@@@ EventsTimeBroadcastReceiver.onReceive","xxx");

			boolean timeEventsExists = false;
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			timeEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_TIME) > 0;
			GlobalData.logE("EventsTimeBroadcastReceiver.onReceive","timeEventsExists="+timeEventsExists);
			dataWrapper.invalidateDataWrapper();

			if (timeEventsExists)
			{
				// start service
				Intent eventsServiceIntent = new Intent(context, EventsService.class);
				eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
				startWakefulService(context, eventsServiceIntent);
			}
			
		}
		
	}

}
