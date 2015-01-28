package sk.henrichg.phoneprofilesplus;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BluetoothScanBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScan";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//GlobalData.logE("#### BluetoothScanBroadcastReceiver.onReceive","xxx");
		GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- start");

		if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
			BluetoothScanAlarmBroadcastReceiver.bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();
		
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{

			boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getStartScan(context));
			
			if (scanStarted)
			{
				GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","xxx");

				String action = intent.getAction();

				GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","action="+action);
				
	            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
	            {
	            	BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);

	            	if (BluetoothScanAlarmBroadcastReceiver.tmpScanResults == null)
	            		BluetoothScanAlarmBroadcastReceiver.tmpScanResults = new ArrayList<BluetoothDeviceData>();
	            	else
	            		BluetoothScanAlarmBroadcastReceiver.tmpScanResults.clear();
	            }
	            else if (BluetoothDevice.ACTION_FOUND.equals(action))
	            {
					// When discovery finds a device

	            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

	            	if (device.getName() != null)
	            	{
						boolean found = false;
						for (BluetoothDeviceData _device : BluetoothScanAlarmBroadcastReceiver.tmpScanResults)
						{
							if (_device.address.equals(device.getAddress()))
							{
								found = true;
								break;
							}
						}
						if (!found)
						{
							BluetoothScanAlarmBroadcastReceiver.tmpScanResults.add(new BluetoothDeviceData(device.getName(), device.getAddress()));
							GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","deviceName="+device.getName());
						}
	            	}
	            }
	            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
	            {
					BluetoothScanAlarmBroadcastReceiver.unlock();
					
					if (BluetoothScanAlarmBroadcastReceiver.scanResults == null)
						BluetoothScanAlarmBroadcastReceiver.scanResults = new ArrayList<BluetoothDeviceData>();
					
					BluetoothScanAlarmBroadcastReceiver.scanResults.clear();
					for (BluetoothDeviceData device : BluetoothScanAlarmBroadcastReceiver.tmpScanResults)
					{
						BluetoothScanAlarmBroadcastReceiver.scanResults.add(new BluetoothDeviceData(device.name, device.address));
					}
					//BluetoothScanAlarmBroadcastReceiver.scanResults.addAll(BluetoothScanAlarmBroadcastReceiver.tmpScanResults);
					BluetoothScanAlarmBroadcastReceiver.tmpScanResults.clear();

					/*
					if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
					{
						for (BluetoothDevice device : BluetoothScanAlarmBroadcastReceiver.scanResults)
						{
							GlobalData.logE("BluetoothScanBroadcastReceiver.onReceive","device.name="+device.getName());
						}
					}
					*/

					if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context))
					{
						GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","disable bluetooth");
						BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
						// not call this, due BluetoothConnectionBroadcastReceiver
						//BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
					}

					BluetoothScanAlarmBroadcastReceiver.setStartScan(context, false);
					
					boolean forceOneScan = GlobalData.getForceOneBluetoothScan(context); 
					GlobalData.setForceOneBluetoothScan(context, false);
					
					if (!forceOneScan) // not start service for force scan
					{
						// start service
						Intent eventsServiceIntent = new Intent(context, EventsService.class);
						eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
						startWakefulService(context, eventsServiceIntent);
					}
					
	            }				
				
			}

		}

		GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- end");
		
	}
	
}
