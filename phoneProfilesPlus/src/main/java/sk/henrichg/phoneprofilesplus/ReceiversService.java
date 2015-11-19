package sk.henrichg.phoneprofilesplus;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;


public class ReceiversService extends Service {

    private final BatteryEventBroadcastReceiver batteryEventReceiver = new BatteryEventBroadcastReceiver();
    private final HeadsetConnectionBroadcastReceiver headsetPlugReceiver = new HeadsetConnectionBroadcastReceiver();
    //private final RestartEventsBroadcastReceiver restartEventsReceiver = new RestartEventsBroadcastReceiver();
    //private final WifiStateChangedBroadcastReceiver wifiStateChangedReceiver = new WifiStateChangedBroadcastReceiver();
    //private final WifiConnectionBroadcastReceiver wifiConnectionReceiver = new WifiConnectionBroadcastReceiver();
    private final ScreenOnOffBroadcastReceiver screenOnOffReceiver = new ScreenOnOffBroadcastReceiver();
    //private final BluetoothStateChangedBroadcastReceiver bluetoothStateChangedReceiver = new BluetoothStateChangedBroadcastReceiver();
    private DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;

    private static SettingsContentObserver settingsContentObserver = null;

    @Override
    public void onCreate()
    {
        GlobalData.logE("$$$ ReceiversService.onCreate", "xxxxx");

        // start service for first start
        Intent eventsServiceIntent = new Intent(getApplicationContext(), FirstStartService.class);
        getApplicationContext().startService(eventsServiceIntent);
        
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(Intent.ACTION_BATTERY_CHANGED);
        getApplicationContext().registerReceiver(batteryEventReceiver, intentFilter1);

        IntentFilter intentFilter2 = new IntentFilter();
        for (String action: HeadsetConnectionBroadcastReceiver.HEADPHONE_ACTIONS) {
            intentFilter2.addAction(action);
        }
        getApplicationContext().registerReceiver(headsetPlugReceiver, intentFilter2);

        /*
        IntentFilter intentFilter7 = new IntentFilter();
        intentFilter7.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateChangedReceiver, intentFilter7);

        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiConnectionReceiver, intentFilter3);
        */

        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter5.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter5.addAction(Intent.ACTION_USER_PRESENT);
        getApplicationContext().registerReceiver(screenOnOffReceiver, intentFilter5);

        /*
        IntentFilter intentFilter8 = new IntentFilter();
        intentFilter8.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateChangedReceiver, intentFilter8);
        */

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            deviceIdleModeReceiver = new DeviceIdleModeBroadcastReceiver();
            IntentFilter intentFilter9 = new IntentFilter();
            intentFilter9.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
            getApplicationContext().registerReceiver(deviceIdleModeReceiver, intentFilter5);
        }

        /*
        // receivers for system date and time change
        // events must by restarted
        IntentFilter intentFilter99 = new IntentFilter();
        intentFilter99.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter99.addAction(Intent.ACTION_TIME_CHANGED);
        getApplicationContext().registerReceiver(restartEventsReceiver, intentFilter99);
        */

        //SMSBroadcastReceiver.registerSMSContentObserver(this);
        //SMSBroadcastReceiver.registerMMSContentObserver(this);

        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);
        settingsContentObserver = new SettingsContentObserver(this, new Handler(getMainLooper()));
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
    }

    @Override
    public void onDestroy()
    {
        GlobalData.logE("ReceiversService.onDestroy", "xxxxx");

        getApplicationContext().unregisterReceiver(batteryEventReceiver);
        getApplicationContext().unregisterReceiver(headsetPlugReceiver);
        //unregisterReceiver(wifiStateChangedReceiver);
        //unregisterReceiver(wifiConnectionReceiver);
        getApplicationContext().unregisterReceiver(screenOnOffReceiver);
        //unregisterReceiver(bluetoothStateChangedReceiver);
        //getApplicationContext().unregisterReceiver(restartEventsReceiver);
        getApplicationContext().unregisterReceiver(deviceIdleModeReceiver);

        //SMSBroadcastReceiver.unregisterSMSContentObserver(this);
        //SMSBroadcastReceiver.unregisterMMSContentObserver(this);

        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        GlobalData.logE("$$$ ReceiversService.onStartCommand", "xxxxx");

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        GlobalData.logE("$$$ ReceiversService.onTaskRemoved", "xxxxx");

        ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
