package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WifiConnectionBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "wifiConnection";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		GlobalData.logE("#### WifiConnectionBroadcastReceiver.onReceive","xxx");

		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);

	    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (info != null)
        {
		    //int lastState = -1;
		    //int currState = -1;
		    
	    	if ((info.getState() == NetworkInfo.State.CONNECTED) ||
	        	(info.getState() == NetworkInfo.State.DISCONNECTED))
	    	{
				/*SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
				lastState = preferences.getInt(GlobalData.PREF_EVENT_WIFI_LAST_STATE, -1);
				currState = -1;
		    	if (info.getState() == NetworkInfo.State.CONNECTED)
		    		currState = 1;
		    	if (info.getState() == NetworkInfo.State.DISCONNECTED)
		    		currState = 0;
				Editor editor = preferences.edit();
				editor.putInt(GlobalData.PREF_EVENT_WIFI_LAST_STATE, currState);
				editor.commit();*/
	    	}
	    	else
	    		return;

			if (GlobalData.getGlobalEventsRuning(context))
			{
	    		//GlobalData.logE("@@@ WifiConnectionBroadcastReceiver.onReceive","state="+info.getState());
	
	        	/*if (((info.getState() == NetworkInfo.State.CONNECTED) ||
	        		(info.getState() == NetworkInfo.State.DISCONNECTED)) &&
	        		(lastState != currState))*/
	        	if ((info.getState() == NetworkInfo.State.CONNECTED) ||
	        		(info.getState() == NetworkInfo.State.DISCONNECTED))
	        	{
		    		GlobalData.logE("@@@ WifiConnectionBroadcastReceiver.onReceive","state="+info.getState());

	        		if (!WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context))
	        		{
		    			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
		    			boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFICONNECTED) > 0;
		    			dataWrapper.invalidateDataWrapper();
		    	
		    			if (wifiEventsExists)
		    			{
			        		GlobalData.logE("@@@ WifiConnectionBroadcastReceiver.onReceive","wifiEventsExists="+wifiEventsExists);
	
		    				// start service
		    				Intent eventsServiceIntent = new Intent(context, EventsService.class);
		    				eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
		    				startWakefulService(context, eventsServiceIntent);
		    			}
	        		}
	        		
	        	}
			}
			
    		/*if ((info.getState() == NetworkInfo.State.DISCONNECTED) &&
    			(lastState != currState))*/
       		/*if (info.getState() == NetworkInfo.State.DISCONNECTED)
    		{
    			WifiScanAlarmBroadcastReceiver.stopScan(context);
    		}*/
			
        }
	}
}
