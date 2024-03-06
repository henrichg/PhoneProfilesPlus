package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class BluetoothScanner {

    private final Context context;

    static final int CLASSIC_BT_SCAN_DURATION = 25; // 25 seconds for classic bluetooth scan

    //static boolean bluetoothEnabledForScan;
    static volatile List<BluetoothDeviceData> tmpBluetoothScanResults = null;
    static volatile boolean bluetoothDiscoveryStarted = false;
    static volatile List<BluetoothDeviceData> tmpScanLEResults = null;

    BluetoothLEScanner bluetoothLEScanner = null;

    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH = "show_enable_location_notification_bluetooth";

    public BluetoothScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan(/*boolean fromDialog*/) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.doScan", "PPApplication.bluetoothScannerMutex");
        synchronized (PPApplication.bluetoothScannerMutex) {
            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return;

            //DataWrapper dataWrapper;

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
            int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
            if (isPowerSaveMode) {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("2")) {
                        // not scan bluetooth in power save mode
                        return;
                    }
                }
            }
            else {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2")) {
                        if (GlobalUtils.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo)) {
                            // not scan bluetooth in configured time
                            return;
                        }
                    }
                }
            }

            //PPApplication.startHandlerThreadPPScanners(/*"BluetoothScanner.doScan.1"*/);
            //final Handler bluetoothChangeHandler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());

            //synchronized (PPApplication.radioChangeStateMutex) {

                if (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                    // check if bluetooth scan events exists
                    //lock();
                    //boolean bluetoothEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false) > 0;
                    //int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                    //int forceScanLE = ApplicationPreferences.prefForceOneBluetoothLEScan;
                    //boolean classicDevicesScan = true; //DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
                    boolean leDevicesScan = bluetoothLESupported(/*context*/);
                    /*if (bluetoothLESupported(context))
                        leDevicesScan = DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_LE, forceScanLE) > 0;
                    else
                        leDevicesScan = false;*/
                    //unlock();
                    //boolean scan= (bluetoothEventsExists ||
                                   //(forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG) ||
                                   //(forceScanLE == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    //if (scan) {
                        if (leDevicesScan)
                            leDevicesScan = GlobalUtils.isLocationEnabled(context);
                    //}
                    /*if (!scan) {
                        // bluetooth scan events not exists
                        BluetoothScanWorker.cancelWork(context, true, null);
                    } else*/ {

                        if (BluetoothScanWorker.bluetooth != null) {
                            if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                                // service restarted during scanning (prefEventBluetoothEnabledForScan is set to false at end of scan),
                                // dislabe Bluetooth
                                final Context appContext = context.getApplicationContext();
                                @SuppressLint("MissingPermission")
                                Runnable runnable = () -> {
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.1");
                                    if (Permissions.checkBluetoothForEMUI(appContext)) {
                                        try {
                                            if (BluetoothScanWorker.bluetooth == null)
                                                BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                            //lock();
                                            //    CmdBluetooth.setBluetooth(false);
                                            if (BluetoothScanWorker.bluetooth.isEnabled()) {
//                                                Log.e("BluetoothScanner.doScan", "######## (1) disable bluetooth");
                                                BluetoothScanWorker.bluetooth.disable();
                                            }
                                        } catch (Exception e) {
                                            PPApplicationStatic.recordException(e);
                                        }
                                    }
                                };
                                PPApplicationStatic.createScannersExecutor();
                                PPApplication.scannersExecutor.submit(runnable);
                                //PPApplication.sleep(1000);
                                if (BluetoothScanWorker.bluetooth == null)
                                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                                waitForBluetoothDisabled(BluetoothScanWorker.bluetooth);
                                //unlock();
                            }

                            //noinspection ConstantConditions
                            if (true /*canScanBluetooth(dataWrapper)*/) {  // scan even if bluetooth is connected
                                BluetoothScanWorker.setScanRequest(context, false);
                                BluetoothScanWorker.setLEScanRequest(context, false);
                                BluetoothScanWorker.setWaitForResults(context, false);
                                BluetoothScanWorker.setWaitForLEResults(context, false);
                                BluetoothScanWorker.setBluetoothEnabledForScan(context, false);
                                BluetoothScanWorker.setScanKilled(context, false);

                                int bluetoothState;

                                //if (classicDevicesScan) {
                                    ///////// Classic BT scan

                                    //lock();

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    //bluetoothChangeHandler,
                                                                    false);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        /*BluetoothScanWorker.*/startCLScan(context);
                                    } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                        BluetoothScanWorker.setScanRequest(context, false);
                                        BluetoothScanWorker.setWaitForResults(context, false);
                                        setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    }

                                    if (ApplicationPreferences.prefEventBluetoothScanRequest ||
                                            ApplicationPreferences.prefEventBluetoothWaitForResult) {
                                        // wait for scan end
                                        waitForBluetoothCLScanEnd(context);
                                    }

                                    //unlock();

                                    //setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    //BluetoothScanWorker.setWaitForResults(context, false);
                                    //BluetoothScanWorker.setLEScanRequest(context, false);

                                    ///////// Classic BT scan - end
                                //}

                                if (leDevicesScan && !ApplicationPreferences.prefEventBluetoothScanKilled) {
                                    ///////// LE BT scan

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    //bluetoothChangeHandler,
                                                                    true);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        /*BluetoothScanWorker.*/startLEScan(context);
                                    } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                        BluetoothScanWorker.setLEScanRequest(context, false);
                                        BluetoothScanWorker.setWaitForLEResults(context, false);
                                        setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    }

                                    if (ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                                            ApplicationPreferences.prefEventBluetoothLEWaitForResult) {

                                        // wait for scan end
                                        waitForLEBluetoothScanEnd(context);

//                                        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] BluetoothScanner.doScan", "xxx");
                                        // send broadcast for start EventsHandler
                                        /*Intent btLEIntent = new Intent(context, BluetoothLEScanBroadcastReceiver.class);
                                        sendBroadcast(btLEIntent);*/
//                                        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] BluetoothScanner.doScan", "xxx");
                                        Intent btLEIntent = new Intent(PhoneProfilesService.ACTION_BLUETOOTHLE_SCAN_BROADCAST_RECEIVER);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(btLEIntent);
                                    }

                                    //unlock();

                                    //setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    //BluetoothScanWorker.setWaitForLEResults(context, false);
                                    //BluetoothScanWorker.setLEScanRequest(context, false);

                                    ///////// LE BT scan - end
                                }
                            }

                            if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                                final Context appContext = context.getApplicationContext();
                                @SuppressLint("MissingPermission")
                                Runnable runnable = () -> {
    //                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.2");

                                        if (Permissions.checkBluetoothForEMUI(appContext)) {
                                            try {
                                                if (BluetoothScanWorker.bluetooth == null)
                                                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                                //lock();
                                                //    CmdBluetooth.setBluetooth(false);
                                                if (BluetoothScanWorker.bluetooth.isEnabled()) {
//                                                    Log.e("BluetoothScanner.doScan", "######## (2) disable bluetooth");
                                                    BluetoothScanWorker.bluetooth.disable();
                                                }
                                            } catch (Exception e) {
                                                PPApplicationStatic.recordException(e);
                                            }
                                        }

                                };
                                PPApplicationStatic.createScannersExecutor();
                                PPApplication.scannersExecutor.submit(runnable);
                            } //else
                            //PPApplication.sleep(1000);
                            if (BluetoothScanWorker.bluetooth == null)
                                BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                            waitForBluetoothDisabled(BluetoothScanWorker.bluetooth);
                            //unlock();
                        }
                    }

                    setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanWorker.setBluetoothEnabledForScan(context, false);
                    BluetoothScanWorker.setWaitForResults(context, false);
                    BluetoothScanWorker.setWaitForLEResults(context, false);
                    BluetoothScanWorker.setScanRequest(context, false);
                    BluetoothScanWorker.setLEScanRequest(context, false);
                    BluetoothScanWorker.setScanKilled(context, false);

                    //unlock();
                }

            //}

        }
    }

    static void getForceOneBluetoothScan(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.getForceOneBluetoothScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefForceOneBluetoothScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneBluetoothScan(Context context, int forceScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.setForceOneBluetoothScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneBluetoothScan = forceScan;
        }
    }

    static void getForceOneLEBluetoothScan(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.getForceOneLEBluetoothScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefForceOneBluetoothLEScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneLEBluetoothScan(Context context, int forceScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.getForceOneLEBluetoothScan", "PPApplication.eventBluetoothSensorMutex");
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneBluetoothLEScan = forceScan;
        }
    }

    @SuppressLint("MissingPermission")
    private void startCLScan(Context context)
    {
        if (BluetoothScanWorker.bluetooth == null)
            BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

        if (BluetoothScanWorker.bluetooth != null) {
            if (BluetoothScanWorker.bluetooth.isDiscovering()) {
//                Log.e("BluetoothScanWorker.startCLScan", "######## cancelDiscovery");
                BluetoothScanWorker.bluetooth.cancelDiscovery();
            }

            BluetoothScanner.bluetoothDiscoveryStarted = false;

            if (Permissions.checkLocation(context)) {
//                Log.e("BluetoothScanWorker.startCLScan", "######## startDiscovery");
                boolean startScan = BluetoothScanWorker.bluetooth.startDiscovery();

                if (!startScan) {
                    if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                        if (Permissions.checkBluetoothForEMUI(context)) {
                            //    CmdBluetooth.setBluetooth(false);
                            if (BluetoothScanWorker.bluetooth.isEnabled()) {
//                                Log.e("BluetoothScanWorker.startCLScan", "######## disable bluetooth");
                                BluetoothScanWorker.bluetooth.disable();
                            }
                        }
                    }
                }
                BluetoothScanWorker.setWaitForResults(context, startScan);
            }
            BluetoothScanWorker.setScanRequest(context, false);
        }
    }

    @SuppressLint("MissingPermission")
    private void stopCLScan() {
        if (BluetoothScanWorker.bluetooth == null)
            BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);
        if (BluetoothScanWorker.bluetooth != null) {
            if (BluetoothScanWorker.bluetooth.isDiscovering()) {
//                Log.e("BluetoothScanWorke,stopCLScanr", "######## cancelDiscovery");
                BluetoothScanWorker.bluetooth.cancelDiscovery();
            }
        }
    }

    static void finishCLScan(final Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.finishCLScan", "PPApplication.bluetoothCLScanMutex");
        synchronized (PPApplication.bluetoothCLScanMutex) {

            if (BluetoothScanner.bluetoothDiscoveryStarted) {

                BluetoothScanner.bluetoothDiscoveryStarted = false;

                List<BluetoothDeviceData> scanResults = new ArrayList<>();

                if (BluetoothScanner.tmpBluetoothScanResults != null) {
                    for (BluetoothDeviceData device : BluetoothScanner.tmpBluetoothScanResults) {
                        scanResults.add(new BluetoothDeviceData(device.getName(), device.getAddress(), device.type, false, 0, false, true));
                    }
                }

                BluetoothScanWorker.saveCLScanResults(context, scanResults);

                BluetoothScanWorker.setWaitForResults(context, false);

                int forceOneScan = ApplicationPreferences.prefForceOneBluetoothScan;
                BluetoothScanner.setForceOneBluetoothScan(context, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    PPExecutors.handleEvents(context,
                            new int[]{EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER},
                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_BLUETOOTH_SCANNER, 5);
                }

                BluetoothScanner.tmpBluetoothScanResults = null;
            }
        }
    }

    // scanning working only when screen is on :-(
    @SuppressLint("MissingPermission")
    private void startLEScan(final Context context)
    {
        if (BluetoothScanner.bluetoothLESupported(/*context*/)) {

//            PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.startLEScan", "PPApplication.bluetoothLEScanMutex");
            synchronized (PPApplication.bluetoothLEScanMutex) {

                /*Context context = PhoneProfilesService.getInstance();
                if (context == null)
                    return;*/

                if (BluetoothScanWorker.bluetooth == null)
                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

                if (BluetoothScanWorker.bluetooth != null) {
                    if (Permissions.checkLocation(context)) {
                        try {
//                            Log.e("BluetoothScanWorker.startLEScan", "BluetoothScanner.bluetoothLEScanner="+BluetoothScanner.bluetoothLEScanner);
                            if (/*BluetoothScanner.*/bluetoothLEScanner == null) {
                                //BluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                                /*BluetoothScanner.*/bluetoothLEScanner = new BluetoothLEScanner(context);
                            }
                            //if (BluetoothScanner.bluetoothLEScanCallback21 == null) {
                            //    BluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback(context);
                            //}

                            ScanSettings.Builder builder = new ScanSettings.Builder();

                            BluetoothScanner.tmpScanLEResults = null;

                            int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                            if (forceScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)
                                builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                            else
                                builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
                            //builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

                            if (BluetoothScanWorker.bluetooth.isOffloadedScanBatchingSupported())
                                builder.setReportDelay(ApplicationPreferences.applicationEventBluetoothLEScanDuration * 1000L);
                            ScanSettings settings = builder.build();

                            List<ScanFilter> filters = new ArrayList<>();

                            if (BluetoothScanWorker.bluetooth.isEnabled()) {
                                /*BluetoothScanner.*/bluetoothLEScanner.bluetoothLeScanner.startScan(
                                        filters, settings,
                                        /*BluetoothScanner.*/bluetoothLEScanner.bluetoothLEScanCallback);
                                BluetoothScanWorker.setWaitForLEResults(context, true);
                            }

                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    BluetoothScanWorker.setLEScanRequest(context, false);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void stopLEScan(/*final Context context*/) {
        if (BluetoothScanner.bluetoothLESupported(/*context*/)) {
            if (BluetoothScanWorker.bluetooth == null)
                BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);

            if (BluetoothScanWorker.bluetooth != null) {
                if (BluetoothScanWorker.bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                    try {
                        /*if (BluetoothScanner.bluetoothLEScanner == null) {
                            BluetoothScanner.bluetoothLEScanner = bluetooth.getBluetoothLeScanner();
                        }
                        if (BluetoothScanner.bluetoothLEScanCallback21 == null) {
                            BluetoothScanner.bluetoothLEScanCallback21 = new BluetoothLEScanCallback(context);
                        }

                        BluetoothScanner.bluetoothLEScanner.stopScan(BluetoothScanner.bluetoothLEScanCallback21);
                        BluetoothScanner.bluetoothLEScanCallback21 = null;*/

//                        Log.e("BluetoothScanWorker.stopLEScan", "BluetoothScanner.bluetoothLEScanner="+BluetoothScanner.bluetoothLEScanner);
                        if (/*BluetoothScanner.*/bluetoothLEScanner != null) {
                            /*BluetoothScanner.*/bluetoothLEScanner.bluetoothLeScanner.stopScan(/*BluetoothScanner.*/bluetoothLEScanner.bluetoothLEScanCallback);
                            /*BluetoothScanner.*/bluetoothLEScanner = null;
                        }

                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }
    }

    private void finishLEScan(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothScanner.finishLEScan", "PPApplication.bluetoothLEScanMutex");
        synchronized (PPApplication.bluetoothLEScanMutex) {

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            if (BluetoothScanner.tmpScanLEResults != null) {
                for (BluetoothDeviceData device : BluetoothScanner.tmpScanLEResults) {
                    scanResults.add(new BluetoothDeviceData(device.getName(), device.getAddress(), device.type, false, 0, false, true));
                }
                //tmpScanLEResults = null;
            }

            BluetoothScanWorker.saveLEScanResults(context, scanResults);
        }
    }

    private int enableBluetooth(BluetoothAdapter bluetooth,
                                /*Handler bluetoothChangeHandler,*/
                                final boolean forLE)
    {
        int bluetoothState = bluetooth.getState();
        int forceScan;
        if (!forLE)
            forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
        else
            forceScan = ApplicationPreferences.prefForceOneBluetoothLEScan;

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
        boolean isBluetoothEnabled = bluetoothState == BluetoothAdapter.STATE_ON;
        if (!isBluetoothEnabled)
        {
            boolean applicationEventBluetoothScanIfBluetoothOff = ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff;
            if (applicationEventBluetoothScanIfBluetoothOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
            {
                //boolean bluetoothEventsExists = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false) > 0;
                boolean scan = ((/*bluetoothEventsExists &&*/ applicationEventBluetoothScanIfBluetoothOff) ||
                        (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                if (scan)
                {
                    BluetoothScanWorker.setBluetoothEnabledForScan(context, true);
                    if (!forLE)
                        BluetoothScanWorker.setScanRequest(context, true);
                    else
                        BluetoothScanWorker.setLEScanRequest(context, true);

                    final Context appContext = context.getApplicationContext();
                    final WeakReference<BluetoothAdapter> bluetoothWeakRef = new WeakReference<>(bluetooth);
                    final WeakReference<BluetoothScanner> scannerWeakRef = new WeakReference<>(this);
                    @SuppressLint("MissingPermission")
                    Runnable runnable = () -> {
                        BluetoothAdapter _bluetooth = bluetoothWeakRef.get();
                        BluetoothScanner scanner = scannerWeakRef.get();

                        if ((_bluetooth != null) && (scanner != null)) {
                            if (Permissions.checkBluetoothForEMUI(appContext)) {
                                //lock(); // lock is required for enabling bluetooth
                                //    CmdBluetooth.setBluetooth(true);
//                            Log.e("BluetoothScanner.enableBluetooth", "######## enable bluetooth");
                                _bluetooth.enable();

                                long start = SystemClock.uptimeMillis();
                                do {
                                    if (!ApplicationPreferences.prefEventBluetoothScanRequest)
                                        break;
                                    if (_bluetooth.getState() == BluetoothAdapter.STATE_ON) {
                                        GlobalUtils.sleep(5000);
                                        if (forLE)
                                            scanner.startLEScan(appContext);
                                        else
                                            scanner.startCLScan(appContext);
                                        break;
                                    }
                                    GlobalUtils.sleep(200);
                                } while (SystemClock.uptimeMillis() - start < 30 * 1000);
                            }
                        }
                    };
                    PPApplicationStatic.createScannersExecutor();
                    PPApplication.scannersExecutor.submit(runnable);
                    return BluetoothAdapter.STATE_TURNING_ON;
                }
            }
        }
        else
        {
            return bluetoothState;
        }
        //}

        return bluetoothState;
    }

    private void waitForBluetoothDisabled(BluetoothAdapter bluetooth) {
        long start = SystemClock.uptimeMillis();
        do {
            int bluetoothState = bluetooth.getState();
            if (bluetoothState == BluetoothAdapter.STATE_OFF)
                break;
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < 5 * 1000);
    }

    private void waitForBluetoothCLScanEnd(Context context)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                    ApplicationPreferences.prefEventBluetoothWaitForResult))
                break;
            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < CLASSIC_BT_SCAN_DURATION * 1000);

        /*BluetoothScanWorker.*/finishCLScan(context);
        /*BluetoothScanWorker.*/stopCLScan();
    }

    private void waitForLEBluetoothScanEnd(Context context)
    {
        if (bluetoothLESupported(/*context*/)) {
            int applicationEventBluetoothLEScanDuration = ApplicationPreferences.applicationEventBluetoothLEScanDuration;
//            Log.e("BluetoothScanner.waitForLEBluetoothScanEnd", "applicationEventBluetoothLEScanDuration="+applicationEventBluetoothLEScanDuration);

            // Wait 12 seconds for ScanCallback.onBatchScanResults after atart scan.
            // This must be, becausa first data are received after approx 12 seconds after start scan.
            long start = SystemClock.uptimeMillis();
            do {
                GlobalUtils.sleep(200);
            } while (SystemClock.uptimeMillis() - start < 12 * 1000);

            // Wait for configured duration in PPP Settings
            start = SystemClock.uptimeMillis();
            do {
                if (!(ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                        ApplicationPreferences.prefEventBluetoothLEWaitForResult))
                    break;

                GlobalUtils.sleep(200);
            } while (SystemClock.uptimeMillis() - start < (applicationEventBluetoothLEScanDuration /* * 5*/) * 1000L);
            /*BluetoothScanWorker.*/finishLEScan(context);
            /*BluetoothScanWorker.*/stopLEScan(/*context*/);

            /*
            // wait 10 seconds for ScanCallback.onBatchScanResults after stop scan
            start = SystemClock.uptimeMillis();
            do {
                GlobalUtils.sleep(200);
            } while (SystemClock.uptimeMillis() - start < 10 * 1000);
            // save ScanCallback.onBatchScanResults
            BluetoothScanWorker.finishLEScan(context);*/

        }
    }

    static boolean bluetoothLESupported(/*Context context*/) {
        return (PPApplication.HAS_FEATURE_BLUETOOTH_LE);
    }

}
