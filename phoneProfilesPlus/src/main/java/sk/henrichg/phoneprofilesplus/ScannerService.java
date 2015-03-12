package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class ScannerService extends IntentService
{
	Context context;
	DataWrapper dataWrapper;
	
	private final WifiScanBroadcastReceiver wifiScanReceiver = new WifiScanBroadcastReceiver();
	private final BluetoothScanBroadcastReceiver bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();

	public static final String PPHELPER_ACTION_RADIOCHANGESTATE = "sk.henrichg.phoneprofileshelper.ACTION_RADIOCHANGESTATE";
	public static final String PPHELPER_EXTRA_RADIOCHANGESTATE = "sk.henrichg.phoneprofileshelper.EXTRA_RADIOCHANGESTATE";
	
	public ScannerService()
	{
		super("ScannerService");
	}

	@SuppressLint("NewApi")
	@Override
	protected void onHandleIntent(Intent intent)
	{
		context = getApplicationContext();

		GlobalData.logE("### ScannerService.onHandleIntent", "-- START ------------");

		/*
		// synchronization, wait for end of radio state change
		GlobalData.logE("@@@ ScannerService.onHandleIntent", "start waiting for radio change");
		GlobalData.waitForRadioChangeState(context);
		GlobalData.logE("@@@ ScannerService.onHandleIntent", "end waiting for radio change");
		
		GlobalData.setRadioChangeState(context, true);
		*/

        GlobalData.logE("$$$ ScannerService.onHandleIntent", "before synchronized block");

		synchronized (GlobalData.radioChangeStateMutex) {

            GlobalData.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - start");

      	// send broadcast about radio change state to PPHelper
		Intent ppHelperIntent1 = new Intent();
		ppHelperIntent1.setAction(ScannerService.PPHELPER_ACTION_RADIOCHANGESTATE);
		ppHelperIntent1.putExtra(ScannerService.PPHELPER_EXTRA_RADIOCHANGESTATE, true);
	    context.sendBroadcast(ppHelperIntent1);
		
		String scanType = intent.getStringExtra(GlobalData.EXTRA_SCANNER_TYPE);
		GlobalData.logE("### ScannerService.onHandleIntent", "scanType="+scanType);
		
		if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI))
		{
			//if (!WifiScanAlarmBroadcastReceiver.getStartScan(context))
			//{
				GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=false");
	
				dataWrapper = new DataWrapper(context, false, false, 0);
				
				if (WifiScanAlarmBroadcastReceiver.wifi == null)
					WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				//WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	
				// start scan
				if (dataWrapper.getIsManualProfileActivation() && (!GlobalData.getForceOneWifiScan(context)))
				{
					WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                    WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
					WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
				}
				else
				{
					// register scan result receiver
					IntentFilter intentFilter4 = new IntentFilter();
					intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
					registerReceiver(wifiScanReceiver, intentFilter4);
					
					// enable wifi
					int wifiState;
					if ((android.os.Build.VERSION.SDK_INT >= 18) && WifiScanAlarmBroadcastReceiver.wifi.isScanAlwaysAvailable())
						wifiState = WifiManager.WIFI_STATE_ENABLED;
					else
						wifiState = enableWifi(dataWrapper, WifiScanAlarmBroadcastReceiver.wifi);
					
					if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        GlobalData.logE("$$$ ScannerService.onHandleIntent", "before startScan");
                        WifiScanAlarmBroadcastReceiver.startScan(context.getApplicationContext());
                        GlobalData.logE("$$$ ScannerService.onHandleIntent", "after startScan");
                    }
					else
					if (wifiState != WifiManager.WIFI_STATE_ENABLING)
					{
						WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
						WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
		    			GlobalData.setForceOneWifiScan(context, false);
				    }
					
					if ((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)))
					{
						GlobalData.logE("$$$ ScannerService.onHandleIntent", "waiting for scan end");

						// wait for scan end
						waitForWifiScanEnd(context, null);
		    		    
						GlobalData.logE("$$$ ScannerService.onHandleIntent", "scan ended");

                        if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context))
                        {
                            /*
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            */

                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "before disable wifi");
                            WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "after disable wifi");
                        }

                        WifiScanAlarmBroadcastReceiver.unlock();

				    	GlobalData.setForceOneWifiScan(context, false);
				    	WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
				    	WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
					}
					
					unregisterReceiver(wifiScanReceiver);
					
					try {
			        	Thread.sleep(700);
				    } catch (InterruptedException e) {
                        e.printStackTrace();
				    }
					
				}
			//}
			//else
			//	GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=true");
		}
		else
		if (scanType.equals(GlobalData.SCANNER_TYPE_BLUETOOTH))
		{
			//if (!BluetoothScanAlarmBroadcastReceiver.getStartScan(context))
			//{
				GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=false");
	
				dataWrapper = new DataWrapper(context, false, false, 0);
				
				if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
					BluetoothScanAlarmBroadcastReceiver.bluetooth = BluetoothAdapter.getDefaultAdapter();
	
				// start scan
				if (dataWrapper.getIsManualProfileActivation() && (!GlobalData.getForceOneBluetoothScan(context)))
				{
					BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                    BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
					BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
				}
				else
				{
					// register scan result receiver
					IntentFilter intentFilter6 = new IntentFilter();		
					intentFilter6.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
					intentFilter6.addAction(BluetoothDevice.ACTION_FOUND);
					intentFilter6.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
					registerReceiver(bluetoothScanReceiver, intentFilter6);
					
					// enable bluetooth
					int bluetoothState = enableBluetooth(dataWrapper, BluetoothScanAlarmBroadcastReceiver.bluetooth);

					if (bluetoothState == BluetoothAdapter.STATE_ON)
						BluetoothScanAlarmBroadcastReceiver.startScan(context.getApplicationContext());
					else
					if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON)
					{
						BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
						BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
	        			GlobalData.setForceOneBluetoothScan(context, false);
				    }

                    if ((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context)))
					{
						GlobalData.logE("@@@ ScannerService.onHandleIntent", "waiting for scan end");
						
						// wait for scan end
						waitForBluetoothScanEnd(context, null);
	
						GlobalData.logE("@@@ ScannerService.onHandleIntent", "scan ended");

                        if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context))
                        {
                            GlobalData.logE("@@@ ScannerService.onHandleIntent","disable bluetooth");
                            BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                        }

                        BluetoothScanAlarmBroadcastReceiver.unlock();

				    	GlobalData.setForceOneBluetoothScan(context, false);
				    	BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
				    	BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
					}
					
					unregisterReceiver(bluetoothScanReceiver);		
					
				}
			//}
			//else
			//	GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=true");
		}

      	// send broadcast about radio change state to PPHelper
		Intent ppHelperIntent2 = new Intent();
		ppHelperIntent2.setAction(ScannerService.PPHELPER_ACTION_RADIOCHANGESTATE);
		ppHelperIntent2.putExtra(ScannerService.PPHELPER_EXTRA_RADIOCHANGESTATE, false);
	    context.sendBroadcast(ppHelperIntent2);

            GlobalData.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - end");

		}

        GlobalData.logE("$$$ ScannerService.onHandleIntent", "after synchronized block");

		//GlobalData.setRadioChangeState(context, false);
		
		GlobalData.logE("### ScannerService.onHandleIntent", "-- END ------------");
		
	}

    @SuppressLint("NewApi")
	private static int enableWifi(DataWrapper dataWrapper, WifiManager wifi)
    {
    	GlobalData.logE("@@@ ScannerService.enableWifi","xxx");

    	int wifiState = wifi.getWifiState();
    	boolean forceScan = GlobalData.getForceOneWifiScan(dataWrapper.context);
    	
    	if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
    	{
    		if (wifiState != WifiManager.WIFI_STATE_ENABLING)
    		{
				boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
				boolean isScanAlwaisAvailable = false;
		    	if (android.os.Build.VERSION.SDK_INT >= 18)
		    		isScanAlwaisAvailable = wifi.isScanAlwaysAvailable();
	        	GlobalData.logE("@@@ ScannerService.enableWifi","isScanAlwaisAvailable="+isScanAlwaisAvailable);
	    		isWifiEnabled = isWifiEnabled || isScanAlwaisAvailable;
		    	if (!isWifiEnabled)
		    	{
		        	if (GlobalData.applicationEventWifiEnableWifi || forceScan)
		        	{
						boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
		
						if (wifiEventsExists || forceScan)
						{
							WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, true);
							WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, true);
                            WifiScanAlarmBroadcastReceiver.lock(dataWrapper.context);
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "before enable wifi");
							wifi.setWifiEnabled(true);
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "after enable wifi");
				        	GlobalData.logE("@@@ ScannerService.enableWifi","set enabled");

                            /*
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            */

							return WifiManager.WIFI_STATE_ENABLING;
						}
		        	}
		    	}
		    	else
		    	{
		        	GlobalData.logE("@@@ ScannerService.enableWifi","already enabled");
		    		//if (isScanAlwaisAvailable)
		    		//	setWifiEnabledForScan(dataWrapper.context, false);
		    		return wifiState;
		    	}
    		}
    	}

		//setWifiEnabledForScan(dataWrapper.context, false);
    	return wifiState;
    }
	
    @SuppressLint("NewApi")
	private static int enableBluetooth(DataWrapper dataWrapper, BluetoothAdapter bluetooth)
    {
    	GlobalData.logE("@@@ ScannerService.enableBluetooth","xxx");

    	int bluetoothState = bluetooth.getState();
    	boolean forceScan = GlobalData.getForceOneBluetoothScan(dataWrapper.context);
    	
    	if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
    	{
    		boolean isBluetoothEnabled = bluetoothState == BluetoothAdapter.STATE_ON;
			if (!isBluetoothEnabled)
	    	{
	        	if (GlobalData.applicationEventBluetoothEnableBluetooth || forceScan)
	        	{
					boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
	
					if (bluetoothEventsExists || forceScan)
					{
			        	GlobalData.logE("@@@ ScannerService.enableBluetooth","set enabled");
			        	BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(dataWrapper.context, true);
						BluetoothScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, true);
			        	bluetooth.enable();
						return BluetoothAdapter.STATE_TURNING_ON;
					}
	        	}
	    	}
	    	else
	    	{
	        	GlobalData.logE("@@@ ScannerService.enableBluetooth","already enabled");
        		//setBluetoothEnabledForScan(dataWrapper.context, false);
	    		return bluetoothState;
	    	}
    	}

   		//setBluetoothEnabledForScan(dataWrapper.context, false);
    	return bluetoothState;
    }
	
    public static void waitForWifiScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
    	for (int i = 0; i < 5 * 60; i++) // 60 seconds for wifi scan (Android 5.0 bug, normally required 5 seconds :-/)
    	{
        	if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                  (WifiScanAlarmBroadcastReceiver.getWaitForResults(context))))
        		break;
    		if (asyncTask != null)
    		{
        		if (asyncTask.isCancelled())
        			break;
    		}
    		
	        try {
	        	Thread.sleep(200);
		    } catch (InterruptedException e) {
                e.printStackTrace();
		    }
    	}
    }
    
    public static void waitForBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
    	for (int i = 0; i < 5 * 20; i++) // 20 seconds for bluetooth scan
    	{
            if (!((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                  (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context))))
        		break;
    		if (asyncTask != null)
    		{
        		if (asyncTask.isCancelled())
        			break;
    		}
    		
	        try {
	        	Thread.sleep(200);
		    } catch (InterruptedException e) {
                e.printStackTrace();
		    }
    	}
    }
    
}
