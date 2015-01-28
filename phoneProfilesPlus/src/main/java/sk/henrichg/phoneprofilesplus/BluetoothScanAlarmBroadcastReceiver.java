package sk.henrichg.phoneprofilesplus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class BluetoothScanAlarmBroadcastReceiver extends BroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScanAlarm";

	public static BluetoothAdapter bluetooth = null;
//    private static WakeLock wakeLock = null;

	public static List<BluetoothDeviceData> tmpScanResults = null;
	public static List<BluetoothDeviceData> scanResults = null;
	public static List<BluetoothDeviceData> boundedDevicesList = null;
	
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("#### BluetoothScanAlarmBroadcastReceiver.onReceive","xxx");

		if (bluetooth == null)
			bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();
		
		if (scanResults == null)
			scanResults = new ArrayList<BluetoothDeviceData>();
		
		// disabled for firstStartEvents
		//if (!GlobalData.getApplicationStarted(context))
			// application is not started
		//	return;

		GlobalData.loadPreferences(context);

		if (GlobalData.getGlobalEventsRuning(context))
		{
			GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","xxx");

			boolean bluetoothEventsExists = false;
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
			GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.onReceive","bluetoothEventsExists="+bluetoothEventsExists);

			if (bluetoothEventsExists || GlobalData.getForceOneBluetoothScan(context))
			{
				int bluetoothState = bluetooth.getState();
				if (bluetoothState == BluetoothAdapter.STATE_ON)
			    {

					boolean connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected("");
					if (connected && (!GlobalData.getForceOneBluetoothScan(context)))
					{
						GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","bluetooth is connected");

						// bluetooth is connected

		    			boolean isBluetoothNameScanned = BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper);  
		    			
						boolean noScanData = scanResults.size() == 0;
		    			
		    			if ((isBluetoothNameScanned) && (!noScanData))
		    			{
		    				// connected bluetooth name is scanned
		    				// no scan
		    				
		        			setBluetoothEnabledForScan(context, false);
		    				setStartScan(context, false);
		        			GlobalData.setForceOneBluetoothScan(context, false);

		    				GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","connected SSID is scanned, no start scan");

		    				dataWrapper.invalidateDataWrapper();
		    				
		    				return;
		    			}
					}
			    }	
				
				startScanner(context);
			}
			else
				removeAlarm(context, false);
			
			dataWrapper.invalidateDataWrapper();
		}
		
	}
	
	public static void initialize(Context context)
	{
		if (bluetooth == null)
			bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();
		
    	unlock();
    	setStartScan(context, false);
    	setBluetoothEnabledForScan(context, false);

    	SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(GlobalData.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
		editor.commit();
		
		if (bluetooth.isEnabled())
		{
			fillBoundedDevicesList(context);
		}
		
	}
	
	public static void setAlarm(Context context, boolean oneshot)
	{
		GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot);

		if (GlobalData.hardwareCheck(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) 
				== GlobalData.HARDWARE_CHECK_ALLOWED)
		{
			GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=true");
			
	        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	 			
	 		Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
	 			
	 		if (oneshot)
	 		{
				removeAlarm(context, true);

				Calendar calendar = Calendar.getInstance();
		        //calendar.setTimeInMillis(System.currentTimeMillis());
		        calendar.add(Calendar.SECOND, 2);

		        long alarmTime = calendar.getTimeInMillis(); 
		        		
			    //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
				//GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));
		        
				PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
	 		}
	 		else
	 		{
				removeAlarm(context, false);

				Calendar calendar = Calendar.getInstance();
		        calendar.add(Calendar.SECOND, 10);
		        long alarmTime = calendar.getTimeInMillis(); 

			    //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
				//GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));
		        
				PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
				alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
												alarmTime,
												GlobalData.applicationEventBluetoothScanInterval * 60 * 1000, 
												alarmIntent);
	 		}
			
			GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.setAlarm","alarm is set");

		}
		else
			GlobalData.logE("BluetoothScanAlarmBroadcastReceiver.setAlarm","BluetoothHardware=false");
	}
	
	public static void removeAlarm(Context context, boolean oneshot)
	{
  		GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
		Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
		PendingIntent pendingIntent;
		if (oneshot)
			pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
		else
			pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
       		GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");
        		
        	alarmManager.cancel(pendingIntent);
        	pendingIntent.cancel();
        }
        else
       		GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
    }
	
	public static boolean isAlarmSet(Context context, boolean oneshot)
	{
		Intent intent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
		PendingIntent pendingIntent;
		if (oneshot)
			pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
		else
			pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
        	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
        else
        	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm not found");

        return (pendingIntent != null);
	}

    public static void lock(Context context)
    {
		 // initialise the locks
		/*if (wakeLock == null)
	        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
	                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothScanWakeLock");*/			

    	try {
    		//if (!wakeLock.isHeld())
            //	wakeLock.acquire();
		//	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.lock","xxx");
        } catch(Exception e) {
            Log.e("BluetoothScanAlarmBroadcastReceiver.lock", "Error getting Lock: "+e.getMessage());
        }
    }
 
    public static void unlock()
    {
    	
        /*if ((wakeLock != null) && (wakeLock.isHeld()))
            wakeLock.release();*/
		//GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.unlock","xxx");
    }
    
    public static void sendBroadcast(Context context)
    {
		Intent broadcastIntent = new Intent(context, BluetoothScanAlarmBroadcastReceiver.class);
		context.sendBroadcast(broadcastIntent);
    }
    
	static public boolean getStartScan(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_START_SCAN, false);
	}

	static public void setStartScan(Context context, boolean startScan)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_START_SCAN, startScan);
		editor.commit();
	}
	
	static public void startScan(Context context)
	{
		initTmpScanResults();
		lock(context); // lock wakeLock, then scan.
        			// unlock() is then called at the end of the onReceive function of BluetoothScanBroadcastReceiver
		boolean startScan = bluetooth.startDiscovery();
      	GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","scanStarted="+startScan);
		if (!startScan)
		{
			unlock();
			if (getBluetoothEnabledForScan(context))
			{
				GlobalData.logE("@@@ BluetoothScanAlarmBroadcastReceiver.onReceive","disable bluetooth");
				bluetooth.disable();
			}
		}
		setStartScan(context, startScan);
	}
	
	static public void startScanner(Context context)
	{
		Intent scanServiceIntent = new Intent(context, ScannerService.class);
		scanServiceIntent.putExtra(GlobalData.EXTRA_SCANNER_TYPE, GlobalData.SCANNER_TYPE_BLUETOOTH);
		context.startService(scanServiceIntent);
	}
	
	static public void stopScan(Context context)
	{
		unlock();
		if (getBluetoothEnabledForScan(context)) 
			bluetooth.disable();
		setBluetoothEnabledForScan(context, false);
		setStartScan(context, false);
		GlobalData.setForceOneBluetoothScan(context, false);
	}
	
	static public void initTmpScanResults()
	{
		if (tmpScanResults != null)
			tmpScanResults.clear();
		else
			tmpScanResults = new ArrayList<BluetoothDeviceData>();
	}

	static public boolean getBluetoothEnabledForScan(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(GlobalData.PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, false);
	}

	static public void setBluetoothEnabledForScan(Context context, boolean setEnabled)
	{
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(GlobalData.PREF_EVENT_BLUETOOTH_ENABLED_FOR_SCAN, setEnabled);
		editor.commit();
	}
	
	static public void fillBoundedDevicesList(Context context)
	{
		if (boundedDevicesList == null)
			boundedDevicesList = new ArrayList<BluetoothDeviceData>();
		
		if (bluetooth == null)
			bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();
		
        Set<BluetoothDevice> boundedDevices = BluetoothScanAlarmBroadcastReceiver.bluetooth.getBondedDevices();
        for (BluetoothDevice device : boundedDevices)
        {
        	boundedDevicesList.add(new BluetoothDeviceData(device.getName(), device.getAddress()));
        }
	}
	
}
