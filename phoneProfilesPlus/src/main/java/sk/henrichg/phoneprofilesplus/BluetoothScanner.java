package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

@SuppressLint("MissingPermission")
class BluetoothScanner {

    private final Context context;

    static final int CLASSIC_BT_SCAN_DURATION = 25; // 25 seconds for classic bluetooth scan

    //static boolean bluetoothEnabledForScan;
    static volatile List<BluetoothDeviceData> tmpBluetoothScanResults = null;
    static volatile boolean bluetoothDiscoveryStarted = false;
    //static volatile BluetoothLeScanner bluetoothLEScanner = null;

    // constructor has Context as parameter
    // this is OK, because this callback will be set to null after stop of LE scan
    @SuppressLint("StaticFieldLeak")
    static volatile BluetoothLEScanner bluetoothLEScanner = null;

    // this is OK, because this callback will be set to null after stop of LE scan
    //@SuppressLint("StaticFieldLeak")
    //static volatile BluetoothLEScanCallback bluetoothLEScanCallback21 = null;

    //static BluetoothLEScanCallback18 bluetoothLEScanCallback18 = null;
    //static BluetoothLEScanCallback bluetoothLEScanCallback21 = null;

    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH = "show_enable_location_notification_bluetooth";

    public BluetoothScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan(@SuppressWarnings("unused") boolean fromDialog) {
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
                    boolean classicDevicesScan = true; //DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
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
                                //bluetoothChangeHandler.post(() -> {
                                Runnable runnable = () -> {
//                                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.1");
                                    if (Permissions.checkBluetoothForEMUI(context)) {
                                        try {
                                            if (BluetoothScanWorker.bluetooth == null)
                                                BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                            //lock();
                                            //if (Build.VERSION.SDK_INT >= 26)
                                            //    CmdBluetooth.setBluetooth(false);
                                            if (BluetoothScanWorker.bluetooth.isEnabled()) {
//                                                Log.e("BluetoothScanner.doScan", "######## (1) disable bluetooth");
                                                BluetoothScanWorker.bluetooth.disable();
                                            }
                                        } catch (Exception e) {
                                            PPApplicationStatic.recordException(e);
                                        }
                                    }
                                }; //);
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

                                //noinspection ConstantConditions
                                if (classicDevicesScan) {
                                    ///////// Classic BT scan

                                    //lock();

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    //bluetoothChangeHandler,
                                                                    false);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        BluetoothScanWorker.startCLScan(context);
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
                                }

                                if (leDevicesScan && !ApplicationPreferences.prefEventBluetoothScanKilled) {
                                    ///////// LE BT scan

                                /*if (android.os.Build.VERSION.SDK_INT < 21)
                                    // for old BT LE scan must by acquired lock
                                    lock();*/

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    //bluetoothChangeHandler,
                                                                    true);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        BluetoothScanWorker.startLEScan(context);
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
                                        Intent btLEIntent = new Intent(PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver");
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
                                //bluetoothChangeHandler.post(() -> {
                                Runnable runnable = () -> {
    //                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.2");

                                        if (Permissions.checkBluetoothForEMUI(context)) {
                                            try {
                                                if (BluetoothScanWorker.bluetooth == null)
                                                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                                //lock();
                                                //if (Build.VERSION.SDK_INT >= 26)
                                                //    CmdBluetooth.setBluetooth(false);
                                                if (BluetoothScanWorker.bluetooth.isEnabled()) {
//                                                    Log.e("BluetoothScanner.doScan", "######## (2) disable bluetooth");
                                                    BluetoothScanWorker.bluetooth.disable();
                                                }
                                            } catch (Exception e) {
                                                PPApplicationStatic.recordException(e);
                                            }
                                        }

                                }; //);
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
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefForceOneBluetoothScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneBluetoothScan(Context context, int forceScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneBluetoothScan = forceScan;
        }
    }

    static void getForceOneLEBluetoothScan(Context context)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            ApplicationPreferences.prefForceOneBluetoothLEScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneLEBluetoothScan(Context context, int forceScan)
    {
        synchronized (PPApplication.eventBluetoothSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneBluetoothLEScan = forceScan;
        }
    }

    private int enableBluetooth(BluetoothAdapter bluetooth,
                                /*Handler bluetoothChangeHandler,*/
                                boolean forLE)
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
                    final BluetoothAdapter _bluetooth = bluetooth;
                    //bluetoothChangeHandler.post(() -> {
                    Runnable runnable = () -> {

                        if (Permissions.checkBluetoothForEMUI(context)) {
                            //lock(); // lock is required for enabling bluetooth
                            //if (Build.VERSION.SDK_INT >= 26)
                            //    CmdBluetooth.setBluetooth(true);
                            //else
//                            Log.e("BluetoothScanner.enableBluetooth", "######## enable bluetooth");
                                _bluetooth.enable();
                        }

                    }; //);
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

    private static void waitForBluetoothDisabled(BluetoothAdapter bluetooth) {
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

    private static void waitForBluetoothCLScanEnd(Context context)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                    ApplicationPreferences.prefEventBluetoothWaitForResult))
                break;
            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < CLASSIC_BT_SCAN_DURATION * 1000);

        BluetoothScanWorker.finishCLScan(context);
        BluetoothScanWorker.stopCLScan();
    }

    private static void waitForLEBluetoothScanEnd(Context context)
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
            BluetoothScanWorker.finishLEScan(context);
            BluetoothScanWorker.stopLEScan(/*context*/);

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
        return (/*(android.os.Build.VERSION.SDK_INT >= 18) &&*/
                PPApplication.HAS_FEATURE_BLUETOOTH_LE);
    }

}
