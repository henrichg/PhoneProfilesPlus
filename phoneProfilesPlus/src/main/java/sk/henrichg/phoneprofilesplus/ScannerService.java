package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class ScannerService extends IntentService
{
    Context context;
    DataWrapper dataWrapper;

    private PowerManager.WakeLock wakeLock = null;

    private final WifiScanBroadcastReceiver wifiScanReceiver = new WifiScanBroadcastReceiver();
    private final BluetoothScanBroadcastReceiver bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
    private final BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();

    public static final String PPHELPER_ACTION_RADIOCHANGESTATE = "sk.henrichg.phoneprofileshelper.ACTION_RADIOCHANGESTATE";
    public static final String PPHELPER_EXTRA_RADIOCHANGESTATE = "sk.henrichg.phoneprofileshelper.EXTRA_RADIOCHANGESTATE";

    public static int classicScanDuration = 20; // 20 seconds for classic bluetooth scan

    Handler wifiBluetoothChangeHandler;

    public static BluetoothLeScanner leScanner = null;
    public static BluetoothLEScanCallback18 leScanCallback18 = null;
    public static BluetoothLEScanCallback21 leScanCallback21 = null;

    private boolean classicDevicesScan;
    private boolean leDevicesScan;

    public ScannerService()
    {
        super("ScannerService");

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        setIntentRedelivery(true);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onHandleIntent(Intent intent)
    {
        context = getApplicationContext();

        GlobalData.logE("### ScannerService.onHandleIntent", "-- START ------------");

        String scanType = intent.getStringExtra(GlobalData.EXTRA_SCANNER_TYPE);
        GlobalData.logE("### ScannerService.onHandleIntent", "scanType="+scanType);

        GlobalData.loadPreferences(context);

        // check power save mode
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode) {
            int forceScan = GlobalData.getForceOneWifiScan(context);
            if ((forceScan != GlobalData.FORCE_ONE_SCAN_ENABLED)) {
                if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI) &&
                        GlobalData.applicationEventWifiScanInPowerSaveMode.equals("2"))
                    // not scan wi-fi in power save mode
                    return;
                if (scanType.equals(GlobalData.SCANNER_TYPE_BLUETOOTH) &&
                        GlobalData.applicationEventBluetoothScanInPowerSaveMode.equals("2"))
                    // not scan bluetooth in power save mode
                    return;
            }
        }

        wifiBluetoothChangeHandler = new Handler(getMainLooper());

        GlobalData.logE("$$$ ScannerService.onHandleIntent", "before synchronized block - scanType=" + scanType);

        synchronized (GlobalData.radioChangeStateMutex) {

            GlobalData.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - start - scanType=" + scanType);

        // send broadcast about radio change state to PPHelper
        Intent ppHelperIntent1 = new Intent();
        ppHelperIntent1.setAction(ScannerService.PPHELPER_ACTION_RADIOCHANGESTATE);
        ppHelperIntent1.putExtra(ScannerService.PPHELPER_EXTRA_RADIOCHANGESTATE, true);
        context.sendBroadcast(ppHelperIntent1);

        if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI))
        {
            GlobalData.logE("@@@ ScannerService.onHandleIntent", "getStartScan=false");

            boolean canScan = GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context) == GlobalData.PREFERENCE_ALLOWED;
            if (canScan) {
                canScan = !WifiApManager.isWifiAPEnabled(context);
                GlobalData.logE("$$$ ScannerService.onHandleIntent", "canScan=" + canScan);
                GlobalData.logE("$$$ WifiAP", "ScannerService.onHandleIntent-isWifiAPEnabled="+!canScan);
            }
            if (canScan) {
                canScan = isLocationEnabled(context, scanType);
            }

            if (canScan) {

                dataWrapper = new DataWrapper(context, false, false, 0);

                // check if wifi scan events exists
                lock();
                boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
                unlock();
                GlobalData.logE("$$$ ScannerService.onHandleIntent","wifiEventsExists="+wifiEventsExists);
                int forceScan = GlobalData.getForceOneWifiScan(context);
                boolean scan = (wifiEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED));
                if ((!wifiEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                    scan = false;
                if (!scan) {
                   // wifi scan events not exists
                   WifiScanAlarmBroadcastReceiver.removeAlarm(context, false);
                   WifiScanAlarmBroadcastReceiver.removeAlarm(context, true);
                }
                else {

                    if (WifiScanAlarmBroadcastReceiver.wifi == null)
                        WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                    if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                        // service restarted during scanning, disable wifi
                        GlobalData.logE("$$$ ScannerService.onHandleIntent", "disable wifi - service restarted");
                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                                WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                                WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            }
                        });
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (canScanWifi(dataWrapper)) {

                        WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);

                        WifiScanAlarmBroadcastReceiver.unlock();

                        // start scan

                        lock();

                        IntentFilter intentFilter4 = new IntentFilter();
                        intentFilter4.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                        registerReceiver(wifiScanReceiver, intentFilter4);

                        // enable wifi
                        int wifiState;
                        //if ((android.os.Build.VERSION.SDK_INT >= 18) && WifiScanAlarmBroadcastReceiver.wifi.isScanAlwaysAvailable())
                        //    wifiState = WifiManager.WIFI_STATE_ENABLED;
                        //else
                        wifiState = enableWifi(dataWrapper, WifiScanAlarmBroadcastReceiver.wifi, wifiBluetoothChangeHandler);

                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "startScan");
                            WifiScanAlarmBroadcastReceiver.startScan(context);
                        } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                            WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                            WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            GlobalData.setForceOneWifiScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                        }

                        if ((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                (WifiScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "waiting for scan end");

                            // wait for scan end
                            waitForWifiScanEnd(context, null);

                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "scan ended");
                        }

                        WifiScanAlarmBroadcastReceiver.unlock();
                        unlock();

                        unregisterReceiver(wifiScanReceiver);

                        if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "disable wifi");
                            wifiBluetoothChangeHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                                }
                            });
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        GlobalData.setForceOneWifiScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                        WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                        WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                    }
                }

                WifiScanAlarmBroadcastReceiver.unlock();
                unlock();
            }
            else {
                GlobalData.setForceOneWifiScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
            }
        }
        else
        if (scanType.equals(GlobalData.SCANNER_TYPE_BLUETOOTH)) {

            if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, context) == GlobalData.PREFERENCE_ALLOWED) {

                dataWrapper = new DataWrapper(context, false, false, 0);

                // check if bluetooth scan events exists
                lock();
                boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
                int forceScan = GlobalData.getForceOneBluetoothScan(dataWrapper.context);
                int forceScanLE = GlobalData.getForceOneLEBluetoothScan(context);
                classicDevicesScan = dataWrapper.getDatabaseHandler().getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
                if (bluetoothLESupported(context) && isLocationEnabled(context, scanType))
                    leDevicesScan = dataWrapper.getDatabaseHandler().getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_LE, forceScanLE) > 0;
                else
                    leDevicesScan = false;
                unlock();
                boolean scan = (bluetoothEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED) || (forceScanLE == GlobalData.FORCE_ONE_SCAN_ENABLED));
                if ((!bluetoothEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                    scan = false;
                if ((!bluetoothEventsExists) && (forceScanLE == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                    scan = false;
                if (!scan) {
                    // bluetooth scan events not exists
                    BluetoothScanAlarmBroadcastReceiver.removeAlarm(context, false);
                    BluetoothScanAlarmBroadcastReceiver.removeAlarm(context, true);
                }
                else {

                    if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
                        BluetoothScanAlarmBroadcastReceiver.bluetooth = BluetoothAdapter.getDefaultAdapter();

                    if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                        // service restarted during scanning, disable Bluetooth
                        GlobalData.logE("$$$ ScannerService.onHandleIntent", "disable BT - service restarted");
                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                                BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                            }
                        });
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (canScanBluetooth(dataWrapper)) {
                        BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);

                        BluetoothScanAlarmBroadcastReceiver.unlock();

                        BluetoothScanAlarmBroadcastReceiver.clearScanResults(context);

                        int bluetoothState;

                        if (classicDevicesScan) {
                            ///////// Classic BT scan

                            GlobalData.logE("$$$ ScannerService","classic devices scan");
                            GlobalData.logE("* ScannerService","classic devices scan");

                            lock();

                            IntentFilter intentFilter6 = new IntentFilter();
                            intentFilter6.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                            intentFilter6.addAction(BluetoothDevice.ACTION_FOUND);
                            intentFilter6.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            registerReceiver(bluetoothScanReceiver, intentFilter6);

                            // enable bluetooth
                            bluetoothState = enableBluetooth(dataWrapper,
                                    BluetoothScanAlarmBroadcastReceiver.bluetooth,
                                    wifiBluetoothChangeHandler,
                                    false);

                            if (!((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_TURNING_ON))) {
                                BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                                GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                            }

                            if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "start classic scan");
                                BluetoothScanAlarmBroadcastReceiver.startScan(context);
                            }

                            if ((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                    (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "waiting for classic scan end");

                                // wait for scan end
                                waitForBluetoothScanEnd(context, null);

                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "classic scan ended");

                            }

                            BluetoothScanAlarmBroadcastReceiver.unlock();
                            unlock();

                            unregisterReceiver(bluetoothScanReceiver);

                            GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

                            ///////// Classic BT scan - end
                        }

                        if (leDevicesScan) {
                            ///////// LE BT scan

                            GlobalData.logE("$$$ ScannerService","LE devices scan");
                            GlobalData.logE("* ScannerService","LE devices scan");

                            IntentFilter intentFilter7 = new IntentFilter();
                            registerReceiver(bluetoothLEScanReceiver, intentFilter7);

                            if (android.os.Build.VERSION.SDK_INT < 21)
                                // for old BT LE scan must by acquired lock
                                lock();

                            // enable bluetooth
                            bluetoothState = enableBluetooth(dataWrapper,
                                    BluetoothScanAlarmBroadcastReceiver.bluetooth,
                                    wifiBluetoothChangeHandler,
                                    true);

                            if (!((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_TURNING_ON))) {
                                BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                                GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                            }

                            if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "start LE scan");
                                GlobalData.logE("* ScannerService.onHandleIntent", "start LE scan");
                                BluetoothScanAlarmBroadcastReceiver.startLEScan(context);
                            }

                            if ((BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                                    (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context))) {
                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "waiting for LE scan end");
                                GlobalData.logE("* ScannerService.onHandleIntent", "waiting for LE scan end");

                                // wait for scan end
                                waitForLEBluetoothScanEnd(context, null);

                                // send broadcast for start EventsService
                                Intent btLEIntent = new Intent(context, BluetoothLEScanBroadcastReceiver.class);
                                sendBroadcast(btLEIntent);

                                GlobalData.logE("$$$ ScannerService.onHandleIntent", "LE scan ended");
                                GlobalData.logE("* ScannerService.onHandleIntent", "LE scan ended");

                            }

                            BluetoothScanAlarmBroadcastReceiver.unlock();
                            unlock();

                            unregisterReceiver(bluetoothLEScanReceiver);

                            GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

                            ///////// LE BT scan - end
                        }

                        if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                            GlobalData.logE("$$$ ScannerService.onHandleIntent", "disable bluetooth");
                            wifiBluetoothChangeHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                                }
                            });
                            try {
                                Thread.sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                        GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                        BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

                    }
                }

                BluetoothScanAlarmBroadcastReceiver.unlock();
                unlock();
            }
            else {
                GlobalData.setForceOneBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                GlobalData.setForceOneLEBluetoothScan(context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
            }
        }

        // send broadcast about radio change state to PPHelper
        Intent ppHelperIntent2 = new Intent();
            ppHelperIntent2.setAction(ScannerService.PPHELPER_ACTION_RADIOCHANGESTATE);
            ppHelperIntent2.putExtra(ScannerService.PPHELPER_EXTRA_RADIOCHANGESTATE, false);
            context.sendBroadcast(ppHelperIntent2);

            GlobalData.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - end - scanType=" + scanType);

        }

            GlobalData.logE("$$$ ScannerService.onHandleIntent", "after synchronized block - scanType=" + scanType);

        //GlobalData.setRadioChangeState(context, false);

        GlobalData.logE("### ScannerService.onHandleIntent", "-- END ------------");

    }

    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
            GlobalData.logE("$$$ ScannerService.lock","xxx");
        } catch(Exception e) {
            Log.e("ScannerService.lock", "Error getting Lock: " + e.getMessage());
            GlobalData.logE("$$$ ScannerService.lock", "Error getting Lock: " + e.getMessage());
        }
    }

    private void unlock() {
        GlobalData.logE("$$$ ScannerService.unlock","xxx");
        if ((wakeLock != null) && (wakeLock.isHeld()))
            wakeLock.release();
    }

    private static boolean canScanWifi(DataWrapper dataWrapper)
    {
        int wifiState = WifiScanAlarmBroadcastReceiver.wifi.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED)
        {

            ConnectivityManager connManager = (ConnectivityManager)dataWrapper.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //if (networkInfo.isConnected() && (GlobalData.getForceOneWifiScan(dataWrapper.context) == GlobalData.FORCE_ONE_SCAN_DISABLED))
            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            if ((activeNetwork != null) &&
                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                activeNetwork.isConnected() &&
                (GlobalData.getForceOneWifiScan(dataWrapper.context) == GlobalData.FORCE_ONE_SCAN_DISABLED))
            {
                GlobalData.logE("$$$ ScannerService.canScanWifi","wifi is connected");

                // wifi is connected

                List<WifiSSIDData> wifiConfigurationList = WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(dataWrapper.context);
                List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);

                WifiInfo wifiInfo = WifiScanAlarmBroadcastReceiver.wifi.getConnectionInfo();
                String SSID = WifiScanAlarmBroadcastReceiver.getSSID(wifiInfo, wifiConfigurationList);

                GlobalData.logE("$$$ ScannerService.canScanWifi","connected SSID="+SSID);

                // search for events with connected SSID and connection type INFRONT
                boolean isSSIDScannedInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_INFRONT);
                // search for events with connected SSID and connection type NOTINFRONT
                boolean isSSIDScannedNotInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_NOTINFRONT);

                if ((isSSIDScannedInFront) && (!isSSIDScannedNotInFront) && (scanResults.size() != 0))
                {
                    // INFRONT events exists for connected SSID and
                    // NOTINFRONT events not exists for connected SSID and
                    // scan data exists, then
                    // no scan

                    WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setWaitForResults(dataWrapper.context, false);
                    GlobalData.setForceOneWifiScan(dataWrapper.context, GlobalData.FORCE_ONE_SCAN_DISABLED);

                    GlobalData.logE("$$$ ScannerService.canScanWifi", "connected SSID is scanned, no start scan");

                    return false;
                }
            }
        }

        return true;
    }

    @SuppressLint("NewApi")
    private static int enableWifi(DataWrapper dataWrapper, WifiManager wifi, Handler wifiBluetoothChangeHandler)
    {
        GlobalData.logE("@@@ ScannerService.enableWifi","xxx");

        int wifiState = wifi.getWifiState();
        int forceScan = GlobalData.getForceOneWifiScan(dataWrapper.context);

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
            if (wifiState != WifiManager.WIFI_STATE_ENABLING)
            {
                boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
                boolean isScanAlwaysAvailable = false;
                if (android.os.Build.VERSION.SDK_INT >= 18)
                    isScanAlwaysAvailable = wifi.isScanAlwaysAvailable();
                GlobalData.logE("@@@ ScannerService.enableWifi","isScanAlwaysAvailable="+isScanAlwaysAvailable);
                isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
                if (!isWifiEnabled)
                {
                    if (GlobalData.applicationEventWifiEnableWifi || (forceScan != GlobalData.FORCE_ONE_SCAN_DISABLED))
                    {
                        boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
                        boolean scan = (wifiEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED));
                        if ((!wifiEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                            scan = false;
                        if (scan)
                        {
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, true);
                            WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, true);
                            WifiScanAlarmBroadcastReceiver.lock(dataWrapper.context);
                            final WifiManager _wifi = wifi;
                            wifiBluetoothChangeHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    GlobalData.logE("$$$ ScannerService.onHandleIntent", "before enable wifi");
                                    _wifi.setWifiEnabled(true);
                                    GlobalData.logE("$$$ ScannerService.onHandleIntent", "after enable wifi");
                                }
                            });
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
                    if (isScanAlwaysAvailable) {
                        GlobalData.logE("@@@ ScannerService.enableWifi", "already enabled");
                        wifiState =  WifiManager.WIFI_STATE_ENABLED;
                    }
                    return wifiState;
                }
            }
        //}

        return wifiState;
    }

    private static boolean canScanBluetooth(DataWrapper dataWrapper)
    {
        int bluetoothState = BluetoothScanAlarmBroadcastReceiver.bluetooth.getState();
        if (bluetoothState == BluetoothAdapter.STATE_ON)
        {

            boolean connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(dataWrapper.context, "");
            if (connected &&
                    (GlobalData.getForceOneBluetoothScan(dataWrapper.context) == GlobalData.FORCE_ONE_SCAN_DISABLED) &&
                    (GlobalData.getForceOneLEBluetoothScan(dataWrapper.context) == GlobalData.FORCE_ONE_SCAN_DISABLED))
            {
                GlobalData.logE("$$$ ScannerService.canScanBluetooth","bluetooth is connected");

                // bluetooth is connected

                // search for events with connected bluetooth adapter and connection type INFRONT
                boolean isBluetoothNameScannedInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_INFRONT);
                // search for events with connected bluetooth adapter and connection type NOTINFRONT
                boolean isBluetoothNameScannedNotInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_NOTINFRONT);

                List<BluetoothDeviceData> scanResults = BluetoothScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);
                if ((isBluetoothNameScannedInFront) && (!isBluetoothNameScannedNotInFront) && (scanResults.size() != 0))
                {
                    // INFRONT events exists for connected BT adapter and
                    // NOTINFRONT events not exists for connected BT adapter and
                    // scan data exists, then
                    // no scan

                    BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setWaitForResults(dataWrapper.context, false);
                    BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(dataWrapper.context, false);
                    GlobalData.setForceOneBluetoothScan(dataWrapper.context, GlobalData.FORCE_ONE_SCAN_DISABLED);
                    GlobalData.setForceOneLEBluetoothScan(dataWrapper.context, GlobalData.FORCE_ONE_SCAN_DISABLED);

                    GlobalData.logE("$$$ ScannerService.canScanBluetooth", "connected SSID is scanned, no start scan");

                    return false;
                }
            }
        }

        return true;
    }

    @SuppressLint("NewApi")
    private static int enableBluetooth(DataWrapper dataWrapper,
                                       BluetoothAdapter bluetooth,
                                       Handler wifiBluetoothChangeHandler,
                                       boolean forLE)
    {
        GlobalData.logE("@@@ ScannerService.enableBluetooth","xxx");

        int bluetoothState = bluetooth.getState();
        int forceScan;
        if (!forLE)
            forceScan = GlobalData.getForceOneBluetoothScan(dataWrapper.context);
        else
            forceScan = GlobalData.getForceOneLEBluetoothScan(dataWrapper.context);

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
            boolean isBluetoothEnabled = bluetoothState == BluetoothAdapter.STATE_ON;
            if (!isBluetoothEnabled)
            {
                if (GlobalData.applicationEventBluetoothEnableBluetooth || (forceScan != GlobalData.FORCE_ONE_SCAN_DISABLED))
                {
                    boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
                    boolean scan = (bluetoothEventsExists || (forceScan == GlobalData.FORCE_ONE_SCAN_ENABLED));
                    if ((!bluetoothEventsExists) && (forceScan == GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS))
                        scan = false;
                    if (scan)
                    {
                        GlobalData.logE("@@@ ScannerService.enableBluetooth","set enabled");
                        BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(dataWrapper.context, true);
                        if (!forLE)
                            BluetoothScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, true);
                        else
                            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(dataWrapper.context, true);
                        final BluetoothAdapter _bluetooth = bluetooth;
                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                _bluetooth.enable();
                            }
                        });
                        return BluetoothAdapter.STATE_TURNING_ON;
                    }
                }
            }
            else
            {
                GlobalData.logE("@@@ ScannerService.enableBluetooth","already enabled");
                return bluetoothState;
            }
        //}

        return bluetoothState;
    }

    public static void waitForWifiScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
        for (int i = 0; i < 5 * 60; i++) // 60 seconds for wifi scan (Android 5.0 bug, normally required 5 seconds :-/)
        {
            //GlobalData.logE("$$$ WifiAP", "ScannerService.waitForWifiScanEnd-getScanRequest="+WifiScanAlarmBroadcastReceiver.getScanRequest(context));
            //GlobalData.logE("$$$ WifiAP", "ScannerService.waitForWifiScanEnd-getWaitForResults="+WifiScanAlarmBroadcastReceiver.getWaitForResults(context));

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
        for (int i = 0; i < 5 * classicScanDuration; i++)
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
        BluetoothScanAlarmBroadcastReceiver.stopScan(context);
    }

    public static void waitForLEBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
        if (bluetoothLESupported(context)) {
            for (int i = 0; i < 5 * GlobalData.applicationEventBluetoothLEScanDuration; i++)
            {
                if (!((BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                      (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context))))
                    break;
                if (asyncTask != null) {
                    if (asyncTask.isCancelled())
                        break;
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            BluetoothScanAlarmBroadcastReceiver.stopLEScan(context);
        }
    }

    public static void waitForForceOneBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask) {
        for (int i = 0; i < 5 * classicScanDuration; i++)
        {
            if (GlobalData.getForceOneBluetoothScan(context) == GlobalData.FORCE_ONE_SCAN_DISABLED)
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
        BluetoothScanAlarmBroadcastReceiver.stopScan(context);

        if (asyncTask != null)
        {
            if (asyncTask.isCancelled())
                return;
        }

        for (int i = 0; i < 5 * GlobalData.applicationEventBluetoothLEScanDuration; i++)
        {
            if (GlobalData.getForceOneLEBluetoothScan(context) == GlobalData.FORCE_ONE_SCAN_DISABLED)
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
        BluetoothScanAlarmBroadcastReceiver.stopLEScan(context);

    }

    public static boolean bluetoothLESupported(Context context) {
        return ((android.os.Build.VERSION.SDK_INT >= 18) &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    private static boolean isLocationEnabled(Context context, String scanType) {
        if (Build.VERSION.SDK_INT >= 23) {
            // check for Location Settings

            int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            boolean isScanAlwaysAvailable = true;

            if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI)) {
                if (WifiScanAlarmBroadcastReceiver.wifi == null)
                    WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                isScanAlwaysAvailable = WifiScanAlarmBroadcastReceiver.wifi.isScanAlwaysAvailable();
            }

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {
                // Location settings are not properly set, show notification about it

                Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                String notificationText;
                String notificationBigText;

                if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI)) {
                    notificationText = context.getString(R.string.phone_profiles_pref_eventWiFiScanningSystemSettings);
                    notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiScanningSystemSettings_summary);
                }
                else {
                    notificationText = context.getString(R.string.phone_profiles_pref_eventBluetoothScanningSystemSettings);
                    notificationBigText = context.getString(R.string.phone_profiles_pref_eventBluetoothScanningSystemSettings_summary);
                }

                NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_pphelper_upgrade_notify) // notification icon
                        .setContentTitle(context.getString(R.string.app_name)) // title for notification
                        .setContentText(notificationText) // message for notification
                        .setAutoCancel(true); // clear notification after click
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBigText));

                int requestCode;
                if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI)) {
                    notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "applicationEventWiFiScanningSystemSettings");
                    requestCode = 1;
                }
                else {
                    notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "applicationEventBluetoothScanningSystemSettings");
                    requestCode = 2;
                }
                notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                if (android.os.Build.VERSION.SDK_INT >= 16)
                    mBuilder.setPriority(Notification.PRIORITY_MAX);
                if (android.os.Build.VERSION.SDK_INT >= 21)
                {
                    mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
                NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (scanType.equals(GlobalData.SCANNER_TYPE_WIFI))
                    mNotificationManager.notify(GlobalData.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                else
                    mNotificationManager.notify(GlobalData.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID, mBuilder.build());

                return false;
            }
            else
                return true;

        }
        else
            return true;
    }

}
