package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;

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

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        setIntentRedelivery (true);

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
            GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=false");

            dataWrapper = new DataWrapper(context, false, false, 0);

            if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                // service restarted during scanning, disable wifi
                if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) ==
                        GlobalData.HARDWARE_CHECK_ALLOWED) {
                    GlobalData.logE("$$$ ScannerService.onHandleIntent", "before disable wifi - service restarted");
                    WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                    GlobalData.logE("$$$ ScannerService.onHandleIntent", "after disable wifi - service restarted");
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (canScanWifi(dataWrapper)) {

                WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);

                if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) ==
                        GlobalData.HARDWARE_CHECK_ALLOWED) {

                    if (WifiScanAlarmBroadcastReceiver.wifi == null)
                        WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    WifiScanAlarmBroadcastReceiver.unlock();

                    // start scan
                    if (!(dataWrapper.getIsManualProfileActivation() && (!GlobalData.getForceOneWifiScan(context)))) {
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
                        } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                            WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                            WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            GlobalData.setForceOneWifiScan(context, false);
                        }

                        if ((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                (WifiScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "waiting for scan end");

                            // wait for scan end
                            waitForWifiScanEnd(context, null);

                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "scan ended");

                            if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "before disable wifi");
                                WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "after disable wifi");
                                try {
                                    Thread.sleep(700);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            WifiScanAlarmBroadcastReceiver.unlock();

                            GlobalData.setForceOneWifiScan(context, false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        }

                        unregisterReceiver(wifiScanReceiver);
                    }

                }
            }
		}
		else
		if (scanType.equals(GlobalData.SCANNER_TYPE_BLUETOOTH))
		{
            GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=false");

            dataWrapper = new DataWrapper(context, false, false, 0);

            if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                // service restarted during scanning, disable Bluetooth
                if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) ==
                        GlobalData.HARDWARE_CHECK_ALLOWED) {
                    GlobalData.logE("@@@ ScannerService.onHandleIntent", "disable bluetooth - service restarted");
                    BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (canScanBluetooth(dataWrapper))
            {
                BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);

                if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) ==
                        GlobalData.HARDWARE_CHECK_ALLOWED) {

                    if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
                        BluetoothScanAlarmBroadcastReceiver.bluetooth = BluetoothAdapter.getDefaultAdapter();

                    BluetoothScanAlarmBroadcastReceiver.unlock();

                    // start scan
                    if (!(dataWrapper.getIsManualProfileActivation() && (!GlobalData.getForceOneBluetoothScan(context)))) {
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
                        else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                            BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                            GlobalData.setForceOneBluetoothScan(context, false);
                        }

                        if ((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                            GlobalData.logE("@@@ ScannerService.onHandleIntent", "waiting for scan end");

                            // wait for scan end
                            waitForBluetoothScanEnd(context, null);

                            GlobalData.logE("@@@ ScannerService.onHandleIntent", "scan ended");

                            if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                                GlobalData.logE("@@@ ScannerService.onHandleIntent", "disable bluetooth");
                                BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                                try {
                                    Thread.sleep(700);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            BluetoothScanAlarmBroadcastReceiver.unlock();

                            GlobalData.setForceOneBluetoothScan(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        }

                        unregisterReceiver(bluetoothScanReceiver);
                    }
                }
            }
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

    private static boolean canScanWifi(DataWrapper dataWrapper)
    {
        int wifiState = WifiScanAlarmBroadcastReceiver.wifi.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED)
        {
            ConnectivityManager connManager = (ConnectivityManager)dataWrapper.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected() && (!GlobalData.getForceOneWifiScan(dataWrapper.context)))
            {
                GlobalData.logE("$$$ ScannerService.canScanWifi","wifi is connected");

                // wifi is connected

                WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(dataWrapper.context);
                WifiScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);

                WifiInfo wifiInfo = WifiScanAlarmBroadcastReceiver.wifi.getConnectionInfo();
                String SSID = DataWrapper.getSSID(wifiInfo);

                GlobalData.logE("$$$ ScannerService.canScanWifi","connected SSID="+SSID);

                // search for events with connected SSID and connection type INFRONT
                boolean isSSIDScannedInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_INFRONT);
                // search for events with connected SSID and connection type NOTINFRONT
                boolean isSSIDScannedNotInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_NOTINFRONT);

                if ((isSSIDScannedInFront) && (!isSSIDScannedNotInFront) && (WifiScanAlarmBroadcastReceiver.scanResults.size() != 0))
                {
                    // INFRONT events exists for connected SSID and
                    // NOTINFRONT events not exists for connected SSID and
                    // scan data exists, then
                    // no scan

                    WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setWaitForResults(dataWrapper.context, false);
                    GlobalData.setForceOneWifiScan(dataWrapper.context, false);

                    GlobalData.logE("$$$ ScannerService.canScanWifi","connected SSID is scanned, no start scan");

                    dataWrapper.invalidateDataWrapper();

                    return false;
                }
            }
        }

        return true;
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
		    		return wifiState;
		    	}
    		}
    	}

    	return wifiState;
    }

    private static boolean canScanBluetooth(DataWrapper dataWrapper)
    {
        int bluetoothState = BluetoothScanAlarmBroadcastReceiver.bluetooth.getState();
        if (bluetoothState == BluetoothAdapter.STATE_ON)
        {

            boolean connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(dataWrapper.context, "");
            if (connected && (!GlobalData.getForceOneBluetoothScan(dataWrapper.context)))
            {
                GlobalData.logE("$$$ ScannerService.canScanBluetooth","bluetooth is connected");

                // bluetooth is connected

                // search for events with connected bluetooth adapter and connection type INFRONT
                boolean isBluetoothNameScannedInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_INFRONT);
                // search for events with connected bluetooth adapter and connection type NOTINFRONT
                boolean isBluetoothNameScannedNotInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_NOTINFRONT);

                BluetoothScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);
                if ((isBluetoothNameScannedInFront) && (!isBluetoothNameScannedNotInFront) && (BluetoothScanAlarmBroadcastReceiver.scanResults.size() != 0))
                {
                    // INFRONT events exists for connected BT adapter and
                    // NOTINFRONT events not exists for connected BT adapter and
                    // scan data exists, then
                    // no scan

                    BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setWaitForResults(dataWrapper.context, false);
                    GlobalData.setForceOneBluetoothScan(dataWrapper.context, false);

                    GlobalData.logE("$$$ ScannerService.canScanBluetooth","connected SSID is scanned, no start scan");

                    dataWrapper.invalidateDataWrapper();

                    return false;
                }
            }
        }

        return true;
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
	    		return bluetoothState;
	    	}
    	}

    	return bluetoothState;
    }
	
    public static void waitForWifiScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
    	for (int i = 0; i < 5 * 60; i++) // 60 seconds for wifi scan (Android 5.0 bug, normally required 5 seconds :-/)
    	{
        	if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                  (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)))) {
                break;
            }
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
