package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class EventsSMSBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "SMSAlarm";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("#### EventsSMSBroadcastReceiver.onReceive","xxx");
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{
			GlobalData.logE("@@@ EventsSMSBroadcastReceiver.onReceive","xxx");

			boolean smsEventsExists = false;
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			smsEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SMS) > 0;
			GlobalData.logE("EventsSMSBroadcastReceiver.onReceive","smsEventsExists="+smsEventsExists);
			dataWrapper.invalidateDataWrapper();

			if (smsEventsExists)
			{
				// start service
				Intent eventsServiceIntent = new Intent(context, EventsService.class);
				eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
				startWakefulService(context, eventsServiceIntent);
			}
			
		}
		
	}

}
