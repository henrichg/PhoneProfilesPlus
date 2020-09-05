package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

class BluetoothScanner {

    private final Context context;

    static final int CLASSIC_BT_SCAN_DURATION = 25; // 25 seconds for classic bluetooth scan

    //static boolean bluetoothEnabledForScan;
    static List<BluetoothDeviceData> tmpBluetoothScanResults = null;
    static boolean bluetoothDiscoveryStarted = false;
    static BluetoothLeScanner bluetoothLEScanner = null;
    static BluetoothLEScanCallback21 bluetoothLEScanCallback21 = null;
    //static BluetoothLEScanCallback18 bluetoothLEScanCallback18 = null;
    //static BluetoothLEScanCallback21 bluetoothLEScanCallback21 = null;

    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH = "show_enable_location_notification_bluetooth";

    public BluetoothScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan() {
        synchronized (PPApplication.bluetoothScannerMutex) {
            //CallsCounter.logCounter(context, "BluetoothScanner.doScan", "Scanner_doScan");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return;

            //PPApplication.logE("%%%% BluetoothScanner.doScan", "-- START ------------");

            //DataWrapper dataWrapper;

            //PPApplication.logE("%%%% BluetoothScanner.doScan", "scannerType=" + scannerType);

            // for Airplane mode ON, no scan
            //if (android.os.Build.VERSION.SDK_INT >= 17) {
                if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
                    //PPApplication.logE("%%%% BluetoothScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            /*} else {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0) {
                    PPApplication.logE("%%%% BluetoothScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            }*/

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode) {
                int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode.equals("2")) {
                        // not scan bluetooth in power save mode
                        //PPApplication.logE("%%%% BluetoothScanner.doScan", "-- END - power save mode ON -------");
                        return;
                    }
                }
            }

            PPApplication.startHandlerThreadPPScanners(/*"BluetoothScanner.doScan.1"*/);
            final Handler bluetoothChangeHandler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());

            //synchronized (PPApplication.radioChangeStateMutex) {

                if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                    //PPApplication.logE("$$$B BluetoothScanner.doScan", "start bt scan");

                    //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                    // check if bluetooth scan events exists
                    //lock();
                    //boolean bluetoothEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false) > 0;
                    //int forceScan = ApplicationPreferences.prefForceOneBluetoothScan;
                    //int forceScanLE = ApplicationPreferences.prefForceOneBluetoothLEScan;
                    boolean classicDevicesScan = true; //DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
                    boolean leDevicesScan = bluetoothLESupported(context);
                    /*if (bluetoothLESupported(context))
                        leDevicesScan = DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_LE, forceScanLE) > 0;
                    else
                        leDevicesScan = false;*/
                    /*if (PPApplication.logEnabled()) {
                        //noinspection ConstantConditions
                        PPApplication.logE("$$$B BluetoothScanner.doScan", "classicDevicesScan=" + classicDevicesScan);
                        PPApplication.logE("$$$B BluetoothScanner.doScan", "leDevicesScan=" + leDevicesScan);
                    }*/

                    //unlock();
                    //boolean scan= (bluetoothEventsExists ||
                                   //(forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG) ||
                                   //(forceScanLE == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    //if (scan) {
                        if (leDevicesScan)
                            leDevicesScan = isLocationEnabled(context/*, scannerType*/);
                    //}
                    /*if (!scan) {
                        // bluetooth scan events not exists
                        //PPApplication.logE("$$$B BluetoothScanner.doScan", "no bt scan events");
                        BluetoothScanWorker.cancelWork(context, true, null);
                    } else*/ {
                        //PPApplication.logE("$$$B BluetoothScanner.doScan", "scan=true");

                        if (BluetoothScanWorker.bluetooth == null)
                            BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);

                        if (BluetoothScanWorker.bluetooth != null) {
                            if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                                // service restarted during scanning, disable Bluetooth
                                //PPApplication.logE("$$$B BluetoothScanner.doScan", "disable BT - service restarted");
                                bluetoothChangeHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
//                                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.1");
                                        if (Permissions.checkBluetoothForEMUI(context)) {
                                            try {
                                                if (BluetoothScanWorker.bluetooth == null)
                                                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                                //lock();
                                                //if (Build.VERSION.SDK_INT >= 26)
                                                //    CmdBluetooth.setBluetooth(false);
                                                //else
                                                BluetoothScanWorker.bluetooth.disable();
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                        }
                                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothScanner.doScan.1");
                                    }
                                });
                                PPApplication.sleep(1000);
                                //unlock();
                            }

                            //noinspection ConstantConditions,ConstantIfStatement
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

                                    //PPApplication.logE("$$$BCL BluetoothScanner.doScan", "classic devices scan");

                                    //lock();

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    bluetoothChangeHandler,
                                                                    false);
                                    //PPApplication.logE("$$$BCL BluetoothScanner.doScan", "bluetoothState=" + bluetoothState);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        //PPApplication.logE("$$$BCL BluetoothScanner.doScan", "start classic scan");
                                        BluetoothScanWorker.startCLScan(context);
                                    } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                        BluetoothScanWorker.setScanRequest(context, false);
                                        BluetoothScanWorker.setWaitForResults(context, false);
                                        setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    }

                                    if (ApplicationPreferences.prefEventBluetoothScanRequest ||
                                            ApplicationPreferences.prefEventBluetoothWaitForResult) {
                                        //PPApplication.logE("$$$BCL BluetoothScanner.doScan", "waiting for classic scan end");

                                        // wait for scan end
                                        waitForBluetoothCLScanEnd(context);

                                        //PPApplication.logE("$$$BCL BluetoothScanner.doScan", "classic scan ended");
                                    }

                                    //unlock();

                                    //setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    //BluetoothScanWorker.setWaitForResults(context, false);
                                    //BluetoothScanWorker.setLEScanRequest(context, false);

                                    ///////// Classic BT scan - end
                                }

                                if (leDevicesScan && !ApplicationPreferences.prefEventBluetoothScanKilled) {
                                    ///////// LE BT scan

                                    //PPApplication.logE("%%%%BLE BluetoothScanner.doScan", "LE devices scan");

                                /*if (android.os.Build.VERSION.SDK_INT < 21)
                                    // for old BT LE scan must by acquired lock
                                    lock();*/

                                    // enable bluetooth
                                    bluetoothState = enableBluetooth(BluetoothScanWorker.bluetooth,
                                                                    bluetoothChangeHandler,
                                                                    true);

                                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                        //PPApplication.logE("%%%%BLE BluetoothScanner.doScan", "start LE scan");
                                        BluetoothScanWorker.startLEScan(context);
                                    } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                        BluetoothScanWorker.setLEScanRequest(context, false);
                                        BluetoothScanWorker.setWaitForLEResults(context, false);
                                        setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                    }

                                    if (ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                                            ApplicationPreferences.prefEventBluetoothLEWaitForResult) {
                                        //PPApplication.logE("%%%%BLE BluetoothScanner.doScan", "waiting for LE scan end");

                                        // wait for scan end
                                        waitForLEBluetoothScanEnd(context);

                                        //PPApplication.logE("%%%%BLE BluetoothScanner.doScan", "LE scan ended");

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

                            bluetoothChangeHandler.post(new Runnable() {
                                @Override
                                public void run() {
//                                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.doScan.2");

                                    if (ApplicationPreferences.prefEventBluetoothEnabledForScan) {
                                        //PPApplication.logE("$$$B BluetoothScanner.doScan", "disable bluetooth");
                                        if (Permissions.checkBluetoothForEMUI(context)) {
                                            try {
                                                if (BluetoothScanWorker.bluetooth == null)
                                                    BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);
                                                //lock();
                                                //if (Build.VERSION.SDK_INT >= 26)
                                                //    CmdBluetooth.setBluetooth(false);
                                                //else
                                                BluetoothScanWorker.bluetooth.disable();
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                        }
                                    } //else
                                        //PPApplication.logE("$$$B BluetoothScanner.doScan", "keep enabled bluetooth");

                                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothScanner.doScan.1");
                                }
                            });
                            PPApplication.sleep(1000);
                            //unlock();
                        }
                    }

                    setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanWorker.setWaitForResults(context, false);
                    BluetoothScanWorker.setWaitForLEResults(context, false);
                    BluetoothScanWorker.setScanRequest(context, false);
                    BluetoothScanWorker.setLEScanRequest(context, false);
                    BluetoothScanWorker.setScanKilled(context, false);

                    //unlock();
                }

                //PPApplication.logE("$$$ BluetoothScanner.doScan", "in synchronized block - end - scannerType=" + scannerType);

            //}

            /*if (PPApplication.logEnabled()) {
                //PPApplication.logE("$$$ BluetoothScanner.doScan", "after synchronized block - scannerType=" + scannerType);

                PPApplication.logE("%%%% BluetoothScanner.doScan", "-- END ------------");
            }*/
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

    @SuppressLint("NewApi")
    private int enableBluetooth(BluetoothAdapter bluetooth,
                                Handler bluetoothChangeHandler,
                                boolean forLE)
    {
        //PPApplication.logE("$$$B BluetoothScanner.enableBluetooth","xxx");

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
                    //PPApplication.logE("$$$B BluetoothScanner.enableBluetooth","set enabled");
                    BluetoothScanWorker.setBluetoothEnabledForScan(context, true);
                    if (!forLE)
                        BluetoothScanWorker.setScanRequest(context, true);
                    else
                        BluetoothScanWorker.setLEScanRequest(context, true);
                    final BluetoothAdapter _bluetooth = bluetooth;
                    bluetoothChangeHandler.post(new Runnable() {
                        @Override
                        public void run() {
//                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BluetoothScanner.enableBluetooth");

                            if (Permissions.checkBluetoothForEMUI(context)) {
                                //lock(); // lock is required for enabling bluetooth
                                //if (Build.VERSION.SDK_INT >= 26)
                                //    CmdBluetooth.setBluetooth(true);
                                //else
                                    _bluetooth.enable();
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothScanner.doScan.1");
                        }
                    });
                    return BluetoothAdapter.STATE_TURNING_ON;
                }
            }
        }
        else
        {
            //PPApplication.logE("$$$B BluetoothScanner.enableBluetooth","already enabled");
            return bluetoothState;
        }
        //}

        return bluetoothState;
    }

    private static void waitForBluetoothCLScanEnd(Context context)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                    ApplicationPreferences.prefEventBluetoothWaitForResult))
                break;
            PPApplication.sleep(500);
        } while (SystemClock.uptimeMillis() - start < CLASSIC_BT_SCAN_DURATION * 1000);

        BluetoothScanWorker.finishCLScan(context);
        BluetoothScanWorker.stopCLScan();
    }

    private static void waitForLEBluetoothScanEnd(Context context)
    {
        if (bluetoothLESupported(context)) {
            int applicationEventBluetoothLEScanDuration = ApplicationPreferences.applicationEventBluetoothLEScanDuration;
            long start = SystemClock.uptimeMillis();
            do {
                if (!(ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                        ApplicationPreferences.prefEventBluetoothLEWaitForResult))
                    break;

                PPApplication.sleep(500);
            } while (SystemClock.uptimeMillis() - start < (applicationEventBluetoothLEScanDuration * 5) * 1000);
            //PPApplication.logE("%%%%BLE BluetoothScanner.waitForLEBluetoothScanEnd", "do finishLEScan");
            BluetoothScanWorker.finishLEScan(context);
            //PPApplication.logE("%%%%BLE BluetoothScanner.waitForLEBluetoothScanEnd", "do stopLEScan");
            BluetoothScanWorker.stopLEScan(context);


            // wait for ScanCallback.onBatchScanResults after stop scan
            start = SystemClock.uptimeMillis();
            do {
                PPApplication.sleep(500);
            } while (SystemClock.uptimeMillis() - start < 10 * 1000);
            // save ScanCallback.onBatchScanResults
            BluetoothScanWorker.finishLEScan(context);

        }
    }

    @SuppressLint("InlinedApi")
    static boolean bluetoothLESupported(Context context) {
        return (/*(android.os.Build.VERSION.SDK_INT >= 18) &&*/
                PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH_LE));
    }

    private static boolean isLocationEnabled(Context context/*, String scanType*/) {
        //if (Build.VERSION.SDK_INT >= 23) {
            // check for Location Settings

            //noinspection RedundantIfStatement
            if (!PhoneProfilesService.isLocationEnabled(context)/* || (!isScanAlwaysAvailable)*/) {
                // Location settings are not properly set, show notification about it

                /*
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context, scanType)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        notificationText = context.getString(R.string.phone_profiles_pref_category_bluetooth_scanning);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        if (android.os.Build.VERSION.SDK_INT < 24) {
                            nTitle = context.getString(R.string.ppp_app_name);
                            nText = notificationText + ": " + notificationBigText;
                        }
                        PPApplication.createExclamationNotificationChannel(context);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context, R.color.primary))
                                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                                .setContentTitle(nTitle) // title for notification
                                .setContentText(nText) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                        int requestCode;
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "bluetoothScanningCategory");
                        requestCode = 2;

                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID, mBuilder.build());
                        }

                        setShowEnableLocationNotification(context, false, scanType);
                    }
                }
                */

                return false;
            }
            else {
                /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true, scanType);*/
                return true;
            }

        /*}
        else {
            //setShowEnableLocationNotification(context, true, scanType);
            return true;
        }*/
    }

}
