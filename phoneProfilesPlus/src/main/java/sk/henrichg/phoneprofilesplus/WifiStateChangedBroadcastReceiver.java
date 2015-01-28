package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "wifiState";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		GlobalData.logE("#### WifiStateChangedBroadcastReceiver.onReceive","xxx");
	
		if (WifiScanAlarmBroadcastReceiver.wifi == null)
			WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

		if (GlobalData.getGlobalEventsRuning(context))
		{
    		GlobalData.logE("@@@ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

    		if (wifiState == WifiManager.WIFI_STATE_ENABLED)
    		{
    			/*
				*/
    			
    			// start scan
				if ((!GlobalData.getEventsBlocked(context)) || GlobalData.getForceOneWifiScan(context))
				{
					if (WifiScanAlarmBroadcastReceiver.getStartScan(context))
						WifiScanAlarmBroadcastReceiver.startScan(context);
					else
					{
		    			// refresh configured networks list
						WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList(context);
						if (WifiScanAlarmBroadcastReceiver.wifiConfigurationList == null)
							GlobalData.logE("@@@ WifiStateChangedBroadcastReceiver.onReceive","wifiConfigurationList=null");
						else
							GlobalData.logE("@@@ WifiStateChangedBroadcastReceiver.onReceive",
													"wifiConfigurationList="+WifiScanAlarmBroadcastReceiver.wifiConfigurationList.size());
					}
				}
    		}
        }
		
		/*if (wifiState == WifiManager.WIFI_STATE_DISABLED)
		{
			WifiScanAlarmBroadcastReceiver.stopScan(context);
		}*/
		
	}
}
