package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class RestartEventsBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "restartEvents";
	public static final String INTENT_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.RESTART_EVENTS";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//GlobalData.logE("#### RestartEventsBroadcastReceiver.onReceive","xxx");
		GlobalData.logE("@@@ RestartEventsBroadcastReceiver.onReceive","####");
		
		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{
			// start service
			Intent eventsServiceIntent = new Intent(context, EventsService.class);
			eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
			startWakefulService(context, eventsServiceIntent);
		}
	}
}
