package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothStateChangedBroadcastReceiver extends BroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "bluetoothState";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		GlobalData.logE("#### BluetoothStateChangedBroadcastReceiver.onReceive","xxx");
	
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

		if (GlobalData.getGlobalEventsRuning(context))
		{
    		GlobalData.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","state="+bluetoothState);

    		if (bluetoothState == BluetoothAdapter.STATE_ON)
    		{
				if ((!GlobalData.getEventsBlocked(context)) || GlobalData.getForceOneBluetoothScan(context))
				{
					if (BluetoothScanAlarmBroadcastReceiver.getStartScan(context))
					{
						GlobalData.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","startScan");
						BluetoothScanAlarmBroadcastReceiver.startScan(context);
					}
					else
					{
						BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);
		        		GlobalData.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","state=ON");
		        		GlobalData.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","getStartScan="+
		        				BluetoothScanAlarmBroadcastReceiver.getStartScan(context));
		        		GlobalData.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","getEventsBlocked="+
		        				GlobalData.getEventsBlocked(context));
					}
				}
    		}
        }
		
		if (bluetoothState == BluetoothAdapter.STATE_OFF)
		{
			BluetoothScanAlarmBroadcastReceiver.stopScan(context);
		}
		
	}
}
