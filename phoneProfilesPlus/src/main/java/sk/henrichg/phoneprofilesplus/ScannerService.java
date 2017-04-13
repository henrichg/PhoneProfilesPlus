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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ScannerService extends IntentService
{
    Context context;
    DataWrapper dataWrapper;

    private PowerManager.WakeLock wakeLock = null;

    private final WifiScanBroadcastReceiver wifiScanReceiver = new WifiScanBroadcastReceiver();
    private final BluetoothScanBroadcastReceiver bluetoothScanReceiver = new BluetoothScanBroadcastReceiver();
    private final BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = new BluetoothLEScanBroadcastReceiver();

    public static int wifiScanDuration = 20;      // 20 seconds for wifi scan
    public static int classicBTScanDuration = 20; // 20 seconds for classic bluetooth scan

    Handler wifiBluetoothChangeHandler;

    public static BluetoothLeScanner leScanner = null;
    public static BluetoothLEScanCallback18 leScanCallback18 = null;
    public static BluetoothLEScanCallback21 leScanCallback21 = null;

    static final String EXTRA_SCANNER_TYPE = "scanner_type";
    public static final String SCANNER_TYPE_WIFI = "wifi";
    public static final String SCANNER_TYPE_BLUETOOTH = "bluetooth";

    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";
    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";

    public static final int FORCE_ONE_SCAN_DISABLED = 0;
    public static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION = "show_enable_location_notification";

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
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        context = getApplicationContext();

        PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- START ------------");

        String scanType = intent.getStringExtra(EXTRA_SCANNER_TYPE);
        PPApplication.logE("%%%% ScannerService.onHandleIntent", "scanType="+scanType);

        //PPApplication.loadPreferences(context);

        // for Airplane mode ON, no scan
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
                PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- END - airplane mode ON -------");
                return;
            }
        }
        else {
            //noinspection deprecation
            if (Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0) {
                PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- END - airplane mode ON -------");
                return;
            }
        }

        // check power save mode
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
        if (isPowerSaveMode) {
            int forceScan = getForceOneWifiScan(context);
            if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                if (scanType.equals(SCANNER_TYPE_WIFI) &&
                        ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context).equals("2")) {
                    // not scan wi-fi in power save mode
                    PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- END - power save mode ON -------");
                    return;
                }
                if (scanType.equals(SCANNER_TYPE_BLUETOOTH) &&
                        ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context).equals("2")) {
                    // not scan bluetooth in power save mode
                    PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- END - power save mode ON -------");
                    return;
                }
            }
        }

        wifiBluetoothChangeHandler = new Handler(getMainLooper());

        PPApplication.logE("$$$ ScannerService.onHandleIntent", "before synchronized block - scanType=" + scanType);

        synchronized (PPApplication.radioChangeStateMutex) {

            PPApplication.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - start - scanType=" + scanType);

        if (scanType.equals(SCANNER_TYPE_WIFI))
        {
            PPApplication.logE("$$$W ScannerService.onHandleIntent", "start wifi scan");

            boolean canScan = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED;
            if (canScan) {
                canScan = !WifiApManager.isWifiAPEnabled(context);
                PPApplication.logE("$$$W ScannerService.onHandleIntent", "canScan=" + canScan);
                PPApplication.logE("$$$W ScannerService.onHandleIntent", "isWifiAPEnabled="+!canScan);
            }

            if (canScan) {

                dataWrapper = new DataWrapper(context, false, false, 0);

                // check if wifi scan events exists
                lock();
                boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
                unlock();
                int forceScan = getForceOneWifiScan(context);
                boolean scan = (wifiEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                if (scan) {
                    if (wifiEventsExists)
                        scan = isLocationEnabled(context, scanType);
                }
                PPApplication.logE("$$$W ScannerService.onHandleIntent", "wifiEventsExists=" + wifiEventsExists);
                PPApplication.logE("$$$W ScannerService.onHandleIntent", "forceScan=" + forceScan);
                if (!scan) {
                   // wifi scan events not exists
                   PPApplication.logE("$$$W ScannerService.onHandleIntent","alarms removed");
                   WifiScanAlarmBroadcastReceiver.removeAlarm(context/*, false*/);
                   //WifiScanAlarmBroadcastReceiver.removeAlarm(context/*, true*/);
                }
                else {
                    PPApplication.logE("$$$W ScannerService.onHandleIntent","can scan");

                    if (WifiScanAlarmBroadcastReceiver.wifi == null)
                        WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                        // service restarted during scanning, disable wifi
                        PPApplication.logE("$$$W ScannerService.onHandleIntent", "disable wifi - service restarted");
                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                                WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                                WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                                WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            }
                        });
                        //try { Thread.sleep(700); } catch (InterruptedException e) { }
                        //SystemClock.sleep(700);
                        PPApplication.sleep(700);
                    }

                    if (true /*canScanWifi(dataWrapper)*/) { // scan even if wifi is connected

                        PPApplication.logE("$$$W ScannerService.onHandleIntent","scan started");

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
                        wifiState = enableWifi(dataWrapper, WifiScanAlarmBroadcastReceiver.wifi, wifiBluetoothChangeHandler);

                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "startScan");
                            WifiScanAlarmBroadcastReceiver.startScan(context);
                        } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                            WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);
                            WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                            setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                        }

                        if ((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                (WifiScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "waiting for scan end");

                            // wait for scan end
                            waitForWifiScanEnd(context, null);

                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "scan ended");
                        }

                        WifiScanAlarmBroadcastReceiver.unlock();
                        unlock();

                        unregisterReceiver(wifiScanReceiver);
                    }
                }

                /*
                wifiBluetoothChangeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "disable wifi");
                            WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                        }
                        else
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "keep enabled wifi");
                    }
                }, 3000);
                //try { Thread.sleep(700); } catch (InterruptedException e) { }
                //SystemClock.sleep(700);
                //PPApplication.sleep(700);*/
                wifiBluetoothChangeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)) {
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "disable wifi");
                            WifiScanAlarmBroadcastReceiver.wifi.setWifiEnabled(false);
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
                        }
                        else
                            PPApplication.logE("$$$W ScannerService.onHandleIntent", "keep enabled wifi");
                    }
                });
                //try { Thread.sleep(700); } catch (InterruptedException e) { }
                //SystemClock.sleep(700);
                PPApplication.sleep(700);
            }

            setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
            //WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(context, false);
            WifiScanAlarmBroadcastReceiver.setWaitForResults(context, false);
            WifiScanAlarmBroadcastReceiver.setScanRequest(context, false);

            WifiScanAlarmBroadcastReceiver.unlock();
            unlock();

        }
        else
        if (scanType.equals(SCANNER_TYPE_BLUETOOTH)) {

            if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {

                PPApplication.logE("$$$B ScannerService.onHandleIntent", "start bt scan");

                dataWrapper = new DataWrapper(context, false, false, 0);

                // check if bluetooth scan events exists
                lock();
                boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
                int forceScan = getForceOneBluetoothScan(dataWrapper.context);
                int forceScanLE = getForceOneLEBluetoothScan(context);
                boolean classicDevicesScan = dataWrapper.getDatabaseHandler().getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
                boolean leDevicesScan;
                if (bluetoothLESupported(context))
                    leDevicesScan = dataWrapper.getDatabaseHandler().getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_LE, forceScanLE) > 0;
                else
                    leDevicesScan = false;
                unlock();
                boolean scan = (bluetoothEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG) ||
                                                         (forceScanLE == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                if (scan) {
                    if (leDevicesScan)
                        leDevicesScan = isLocationEnabled(context, scanType);
                }
                if (!scan) {
                    // bluetooth scan events not exists
                    PPApplication.logE("$$$B ScannerService.onHandleIntent", "no bt scan events");
                    BluetoothScanAlarmBroadcastReceiver.removeAlarm(context/*, false*/);
                    //BluetoothScanAlarmBroadcastReceiver.removeAlarm(context/*, true*/);
                }
                else {
                    PPApplication.logE("$$$B ScannerService.onHandleIntent", "scan=true");

                    if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
                        BluetoothScanAlarmBroadcastReceiver.bluetooth = BluetoothScanAlarmBroadcastReceiver.getBluetoothAdapter(context);

                    if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                        // service restarted during scanning, disable Bluetooth
                        PPApplication.logE("$$$B ScannerService.onHandleIntent", "disable BT - service restarted");
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
                        //try { Thread.sleep(700); } catch (InterruptedException e) { }
                        //SystemClock.sleep(700);
                        PPApplication.sleep(700);
                    }

                    if (true /*canScanBluetooth(dataWrapper)*/) {  // scan even if bluetooth is connected
                        BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                        BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);

                        BluetoothScanAlarmBroadcastReceiver.unlock();

                        //BluetoothScanAlarmBroadcastReceiver.clearScanResults(context);

                        int bluetoothState;

                        if (classicDevicesScan) {
                            ///////// Classic BT scan

                            PPApplication.logE("$$$BCL ScannerService.onHandleIntent","classic devices scan");

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
                            PPApplication.logE("$$$BCL ScannerService.onHandleIntent","bluetoothState="+bluetoothState);

                            if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                PPApplication.logE("$$$BCL ScannerService.onHandleIntent", "start classic scan");
                                BluetoothScanAlarmBroadcastReceiver.startCLScan(context);
                            }
                            else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                                setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                            }

                            if ((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                                    (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context))) {
                                PPApplication.logE("$$$BCL ScannerService.onHandleIntent", "waiting for classic scan end");

                                // wait for scan end
                                waitForBluetoothScanEnd(context, null);

                                PPApplication.logE("$$$BCL ScannerService.onHandleIntent", "classic scan ended");

                            }

                            BluetoothScanAlarmBroadcastReceiver.unlock();
                            unlock();

                            unregisterReceiver(bluetoothScanReceiver);

                            setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

                            ///////// Classic BT scan - end
                        }

                        if (leDevicesScan) {
                            ///////// LE BT scan

                            PPApplication.logE("$$$BLE ScannerService","LE devices scan");

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

                            if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                PPApplication.logE("$$$BLE ScannerService.onHandleIntent", "start LE scan");
                                BluetoothScanAlarmBroadcastReceiver.startLEScan(context);
                            }
                            else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                                setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                            }

                            if ((BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                                    (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context))) {
                                PPApplication.logE("$$$BLE ScannerService.onHandleIntent", "waiting for LE scan end");

                                // wait for scan end
                                waitForLEBluetoothScanEnd(context, null);

                                // send broadcast for start EventsService
                                Intent btLEIntent = new Intent(context, BluetoothLEScanBroadcastReceiver.class);
                                sendBroadcast(btLEIntent);

                                PPApplication.logE("$$$BLE ScannerService.onHandleIntent", "LE scan ended");
                            }

                            BluetoothScanAlarmBroadcastReceiver.unlock();
                            unlock();

                            unregisterReceiver(bluetoothLEScanReceiver);

                            setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                            BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
                            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

                            ///////// LE BT scan - end
                        }
                    }

                    /*
                    wifiBluetoothChangeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                                PPApplication.logE("$$$B ScannerService.onHandleIntent", "disable bluetooth");
                                BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                            }
                            else
                                PPApplication.logE("$$$B ScannerService.onHandleIntent", "keep enabled bluetooth");
                        }
                    }, 3000);
                    //try { Thread.sleep(700); } catch (InterruptedException e) { }
                    //SystemClock.sleep(700);
                    //PPApplication.sleep(3000);
                    */
                    wifiBluetoothChangeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)) {
                                PPApplication.logE("$$$B ScannerService.onHandleIntent", "disable bluetooth");
                                BluetoothScanAlarmBroadcastReceiver.bluetooth.disable();
                                BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
                            }
                            else
                                PPApplication.logE("$$$B ScannerService.onHandleIntent", "keep enabled bluetooth");
                        }
                    });
                    //try { Thread.sleep(700); } catch (InterruptedException e) { }
                    //SystemClock.sleep(700);
                    PPApplication.sleep(700);
                }
            }

            setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
            setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
            //BluetoothScanAlarmBroadcastReceiver.setBluetoothEnabledForScan(context, false);
            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);
            BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);
            BluetoothScanAlarmBroadcastReceiver.setScanRequest(context, false);
            BluetoothScanAlarmBroadcastReceiver.setLEScanRequest(context, false);

            BluetoothScanAlarmBroadcastReceiver.unlock();
            unlock();
        }

        PPApplication.logE("$$$ ScannerService.onHandleIntent", "in synchronized block - end - scanType=" + scanType);

        }

        PPApplication.logE("$$$ ScannerService.onHandleIntent", "after synchronized block - scanType=" + scanType);

        PPApplication.logE("%%%% ScannerService.onHandleIntent", "-- END ------------");

    }

    static public int getForceOneWifiScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneWifiScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
        editor.apply();
    }

    static public int getForceOneBluetoothScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneBluetoothScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
        editor.apply();
    }

    static public int getForceOneLEBluetoothScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static public void setForceOneLEBluetoothScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, forceScan);
        editor.apply();
    }

    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
            PPApplication.logE("$$$ ScannerService.lock","xxx");
        } catch(Exception e) {
            Log.e("ScannerService.lock", "Error getting Lock: " + e.getMessage());
            PPApplication.logE("$$$ ScannerService.lock", "Error getting Lock: " + e.getMessage());
        }
    }

    private void unlock() {
        PPApplication.logE("$$$ ScannerService.unlock","xxx");
        if ((wakeLock != null) && (wakeLock.isHeld()))
            wakeLock.release();
    }

    /*
    private boolean canScanWifi(DataWrapper dataWrapper)
    {
        int wifiState = WifiScanAlarmBroadcastReceiver.wifi.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED)
        {

            ConnectivityManager connManager = (ConnectivityManager)dataWrapper.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //if (networkInfo.isConnected() && (PPApplication.getForceOneWifiScan(dataWrapper.context) == PPApplication.FORCE_ONE_SCAN_DISABLED))
            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            if ((activeNetwork != null) &&
                (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) &&
                activeNetwork.isConnected() &&
                (PPApplication.getForceOneWifiScan(dataWrapper.context) == PPApplication.FORCE_ONE_SCAN_DISABLED))
            {
                PPApplication.logE("$$$ ScannerService.canScanWifi","wifi is connected");

                // wifi is connected

                List<WifiSSIDData> wifiConfigurationList = WifiScanAlarmBroadcastReceiver.getWifiConfigurationList(dataWrapper.context);
                List<WifiSSIDData> scanResults = WifiScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);

                WifiInfo wifiInfo = WifiScanAlarmBroadcastReceiver.wifi.getConnectionInfo();
                String SSID = WifiScanAlarmBroadcastReceiver.getSSID(wifiInfo, wifiConfigurationList);

                PPApplication.logE("$$$ ScannerService.canScanWifi","connected SSID="+SSID);

                lock();
                // search for events with connected SSID and connection type INFRONT
                boolean isSSIDScannedInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_INFRONT);
                // search for events with connected SSID and connection type NOTINFRONT
                boolean isSSIDScannedNotInFront = dataWrapper.getDatabaseHandler().isSSIDScanned(SSID, EventPreferencesWifi.CTYPE_NOTINFRONT);
                unlock();

                if ((isSSIDScannedInFront) && (!isSSIDScannedNotInFront) && (scanResults != null) && (scanResults.size() != 0))
                {
                    // INFRONT events exists for connected SSID and
                    // NOTINFRONT events not exists for connected SSID and
                    // scan data exists, then
                    // no scan

                    WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, false);
                    WifiScanAlarmBroadcastReceiver.setWaitForResults(dataWrapper.context, false);
                    PPApplication.setForceOneWifiScan(dataWrapper.context, PPApplication.FORCE_ONE_SCAN_DISABLED);

                    PPApplication.logE("$$$ ScannerService.canScanWifi", "connected SSID is scanned, no start scan");

                    return false;
                }
            }
        }

        return true;
    }
    */

    @SuppressLint("NewApi")
    private int enableWifi(DataWrapper dataWrapper, WifiManager wifi, Handler wifiBluetoothChangeHandler)
    {
        PPApplication.logE("@@@ ScannerService.enableWifi","xxx");

        int wifiState = wifi.getWifiState();
        int forceScan = getForceOneWifiScan(dataWrapper.context);

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
            if (wifiState != WifiManager.WIFI_STATE_ENABLING)
            {
                boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
                boolean isScanAlwaysAvailable = false;
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    // from dialog preference wifi must be enabled for saved wifi configuration
                    // (see WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList)

                    // this must be disabled because scanning not working, when wifi is disabled after disabled WiFi AP
                    // Tested and scanning working ;-)
                    if (android.os.Build.VERSION.SDK_INT >= 18)
                        isScanAlwaysAvailable = wifi.isScanAlwaysAvailable();
                }
                PPApplication.logE("@@@ ScannerService.enableWifi","isScanAlwaysAvailable="+isScanAlwaysAvailable);
                isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
                if (!isWifiEnabled)
                {
                    if (ApplicationPreferences.applicationEventWifiEnableWifi(dataWrapper.context) || (forceScan != FORCE_ONE_SCAN_DISABLED))
                    {
                        boolean wifiEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0;
                        boolean scan = ((wifiEventsExists && ApplicationPreferences.applicationEventWifiEnableWifi(dataWrapper.context)) ||
                                            (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                        if (scan)
                        {
                            WifiScanAlarmBroadcastReceiver.setWifiEnabledForScan(dataWrapper.context, true);
                            WifiScanAlarmBroadcastReceiver.setScanRequest(dataWrapper.context, true);
                            WifiScanAlarmBroadcastReceiver.lock(dataWrapper.context);
                            final WifiManager _wifi = wifi;
                            wifiBluetoothChangeHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PPApplication.logE("$$$ ScannerService.enableWifi", "before enable wifi");
                                    _wifi.setWifiEnabled(true);
                                    PPApplication.logE("$$$ ScannerService.enableWifi", "after enable wifi");
                                }
                            });
                            PPApplication.logE("@@@ ScannerService.enableWifi","set enabled");

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
                        PPApplication.logE("@@@ ScannerService.enableWifi", "scan always available");
                        wifiState =  WifiManager.WIFI_STATE_ENABLED;
                    }
                    return wifiState;
                }
            }
        //}

        return wifiState;
    }

    /*
    private boolean canScanBluetooth(DataWrapper dataWrapper)
    {
        int bluetoothState = BluetoothScanAlarmBroadcastReceiver.bluetooth.getState();
        if (bluetoothState == BluetoothAdapter.STATE_ON)
        {

            boolean connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(dataWrapper.context, "");
            if (connected &&
                    (PPApplication.getForceOneBluetoothScan(dataWrapper.context) == PPApplication.FORCE_ONE_SCAN_DISABLED) &&
                    (PPApplication.getForceOneLEBluetoothScan(dataWrapper.context) == PPApplication.FORCE_ONE_SCAN_DISABLED))
            {
                PPApplication.logE("$$$ ScannerService.canScanBluetooth","bluetooth is connected");

                // bluetooth is connected

                lock();
                // search for events with connected bluetooth adapter and connection type INFRONT
                boolean isBluetoothNameScannedInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_INFRONT);
                // search for events with connected bluetooth adapter and connection type NOTINFRONT
                boolean isBluetoothNameScannedNotInFront =
                        BluetoothConnectionBroadcastReceiver.isAdapterNameScanned(dataWrapper, EventPreferencesBluetooth.CTYPE_NOTINFRONT);
                unlock();

                List<BluetoothDeviceData> scanResults = BluetoothScanAlarmBroadcastReceiver.getScanResults(dataWrapper.context);
                if ((isBluetoothNameScannedInFront) && (!isBluetoothNameScannedNotInFront) &&
                        (scanResults != null) && (scanResults.size() != 0))
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
                    PPApplication.setForceOneBluetoothScan(dataWrapper.context, PPApplication.FORCE_ONE_SCAN_DISABLED);
                    PPApplication.setForceOneLEBluetoothScan(dataWrapper.context, PPApplication.FORCE_ONE_SCAN_DISABLED);

                    PPApplication.logE("$$$ ScannerService.canScanBluetooth", "connected SSID is scanned, no start scan");

                    return false;
                }
            }
        }

        return true;
    }
    */

    @SuppressLint("NewApi")
    private int enableBluetooth(DataWrapper dataWrapper,
                                       BluetoothAdapter bluetooth,
                                       Handler wifiBluetoothChangeHandler,
                                       boolean forLE)
    {
        PPApplication.logE("@@@ ScannerService.enableBluetooth","xxx");

        int bluetoothState = bluetooth.getState();
        int forceScan;
        if (!forLE)
            forceScan = getForceOneBluetoothScan(dataWrapper.context);
        else
            forceScan = getForceOneLEBluetoothScan(dataWrapper.context);

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
            boolean isBluetoothEnabled = bluetoothState == BluetoothAdapter.STATE_ON;
            if (!isBluetoothEnabled)
            {
                if (ApplicationPreferences.applicationEventBluetoothEnableBluetooth(dataWrapper.context) || (forceScan != FORCE_ONE_SCAN_DISABLED))
                {
                    boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0;
                    boolean scan = ((bluetoothEventsExists && ApplicationPreferences.applicationEventBluetoothEnableBluetooth(dataWrapper.context)) ||
                                        (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    if (scan)
                    {
                        PPApplication.logE("@@@ ScannerService.enableBluetooth","set enabled");
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
                PPApplication.logE("@@@ ScannerService.enableBluetooth","already enabled");
                return bluetoothState;
            }
        //}

        return bluetoothState;
    }

    public static void waitForWifiScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
        long start = SystemClock.uptimeMillis();
        do {
            //PPApplication.logE("$$$ WifiAP", "ScannerService.waitForWifiScanEnd-getScanRequest="+WifiScanAlarmBroadcastReceiver.getScanRequest(context));
            //PPApplication.logE("$$$ WifiAP", "ScannerService.waitForWifiScanEnd-getWaitForResults="+WifiScanAlarmBroadcastReceiver.getWaitForResults(context));

            if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                    (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)))) {
                break;
            }
            if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < wifiScanDuration * 1000);
    }

    public static void waitForBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                    (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context))))
                break;
            if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < classicBTScanDuration * 1000);

        BluetoothScanBroadcastReceiver.finishScan(context);
        BluetoothScanAlarmBroadcastReceiver.stopCLScan(context);
    }

    public static void waitForLEBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask)
    {
        if (bluetoothLESupported(context)) {
            long start = SystemClock.uptimeMillis();
            do {
                if (!((BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context))))
                    break;
                if (asyncTask != null) {
                    if (asyncTask.isCancelled())
                        break;
                }

                //try { Thread.sleep(100); } catch (InterruptedException e) { }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() - start < ApplicationPreferences.applicationEventBluetoothLEScanDuration(context) * 1000);

            BluetoothScanAlarmBroadcastReceiver.finishLEScan(context);
            BluetoothScanAlarmBroadcastReceiver.stopLEScan(context);
        }
    }

    public static void waitForForceOneBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask) {
        long start = SystemClock.uptimeMillis();
        do {
            if (getForceOneBluetoothScan(context) == FORCE_ONE_SCAN_DISABLED)
                break;
            if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < classicBTScanDuration * 1000);
        BluetoothScanBroadcastReceiver.finishScan(context);
        BluetoothScanAlarmBroadcastReceiver.stopCLScan(context);

        if (asyncTask != null)
        {
            if (asyncTask.isCancelled())
                return;
        }

        start = SystemClock.uptimeMillis();
        do {
            if (getForceOneLEBluetoothScan(context) == FORCE_ONE_SCAN_DISABLED)
                break;
            if (asyncTask != null) {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ApplicationPreferences.applicationEventBluetoothLEScanDuration(context) * 1000);
        BluetoothScanAlarmBroadcastReceiver.finishLEScan(context);
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

            if (scanType.equals(SCANNER_TYPE_WIFI)) {
                if (WifiScanAlarmBroadcastReceiver.wifi == null)
                    WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                isScanAlwaysAvailable = WifiScanAlarmBroadcastReceiver.wifi.isScanAlwaysAvailable();
            }

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {
                // Location settings are not properly set, show notification about it

                if (getShowEnableLocationNotification(context)) {
                    Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    String notificationText;
                    String notificationBigText;

                    if (scanType.equals(SCANNER_TYPE_WIFI)) {
                        notificationText = context.getString(R.string.phone_profiles_pref_eventWiFiScanningSystemSettings);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiScanningSystemSettings_summary);
                    } else {
                        notificationText = context.getString(R.string.phone_profiles_pref_eventBluetoothScanningSystemSettings);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventBluetoothScanningSystemSettings_summary);
                    }

                    String ntitle = notificationText;
                    String ntext = notificationBigText;
                    if (android.os.Build.VERSION.SDK_INT < 24) {
                        ntitle = context.getString(R.string.app_name);
                        ntext = notificationText+": "+notificationBigText;
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                            .setContentTitle(ntitle) // title for notification
                            .setContentText(ntext) // message for notification
                            .setAutoCancel(true); // clear notification after click
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ntext));

                    int requestCode;
                    if (scanType.equals(SCANNER_TYPE_WIFI)) {
                        notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                        requestCode = 1;
                    } else {
                        notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "bluetoothScanninCategory");
                        requestCode = 2;
                    }
                    //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                    PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pi);
                    if (android.os.Build.VERSION.SDK_INT >= 16)
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                    }
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (scanType.equals(SCANNER_TYPE_WIFI))
                        mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                    else
                        mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID, mBuilder.build());

                    setShowEnableLocationNotification(context, false);
                }

                return false;
            }
            else {
                setShowEnableLocationNotification(context, true);
                return true;
            }

        }
        else {
            setShowEnableLocationNotification(context, true);
            return true;
        }
    }

    static public boolean getShowEnableLocationNotification(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION, true);
    }

    static public void setShowEnableLocationNotification(Context context, boolean show)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION, show);
        editor.apply();
    }


}
