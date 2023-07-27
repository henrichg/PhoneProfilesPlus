package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.ArrayList;

public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothScanBroadcastReceiver.onReceive","xxx");

        if (intent == null)
            return;

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;
        if (ApplicationPreferences.prefForceOneBluetoothScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            boolean scanningPaused = ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2") &&
                    GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo);
            if ((!ApplicationPreferences.applicationEventBluetoothEnableScanning) || scanningPaused)
                // scanning is disabled
                return;
        }

        final String action = intent.getAction();

        if (action == null)
            return;

        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED) ||
                action.equals(BluetoothDevice.ACTION_FOUND) ||
                action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            // BluetoothScanBroadcastReceiver

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"BluetoothScanBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), device) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothScanBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();
                //BluetoothDevice device = deviceWeakRef.get();

                //if ((appContext != null) && (device != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothScanBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (BluetoothScanWorker.bluetooth == null)
                            BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);

                        if (BluetoothScanWorker.bluetooth != null) {
                            int forceOneScan = ApplicationPreferences.prefForceOneBluetoothScan;

                            if (EventStatic.getGlobalEventsRunning(appContext) || (forceOneScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                                boolean scanStarted = ApplicationPreferences.prefEventBluetoothWaitForResult;

                                if (scanStarted) {
                                    switch (action) {
                                        case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                                            // may be not invoked if not any BT is around

                                            if (!BluetoothScanner.bluetoothDiscoveryStarted) {
                                                BluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }
                                            break;
                                        case BluetoothDevice.ACTION_FOUND:
                                            // When discovery finds a device

                                            if (!BluetoothScanner.bluetoothDiscoveryStarted) {
                                                BluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }

                                            synchronized (PPApplication.bluetoothCLScanMutex) {
                                                @SuppressLint("MissingPermission")
                                                String btName = device.getName();
//                                                PPApplicationStatic.logE("[IN_BROADCAST] BluetoothScanBroadcastReceiver.onReceive","btName="+btName);
//                                                PPApplicationStatic.logE("[IN_BROADCAST] BluetoothScanBroadcastReceiver.onReceive","deviceName="+deviceName);
                                                if ((deviceName != null) && (!deviceName.isEmpty())) {
                                                    //btNameE = deviceName;
                                                    //btName = btNameE;
                                                    btName = deviceName;
                                                }
//                                                PPApplicationStatic.logE("[IN_BROADCAST] BluetoothScanBroadcastReceiver.onReceive","btName="+btName);

                                                if (BluetoothScanner.tmpBluetoothScanResults == null)
                                                    BluetoothScanner.tmpBluetoothScanResults = new ArrayList<>();

                                                boolean found = false;
                                                for (BluetoothDeviceData _device : BluetoothScanner.tmpBluetoothScanResults) {
                                                    if (_device.address.equals(device.getAddress())) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                if (!found) {
                                                    for (BluetoothDeviceData _device : BluetoothScanner.tmpBluetoothScanResults) {
                                                        if (_device.getName().equalsIgnoreCase(btName)) {
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (!found) {
                                                    BluetoothScanner.tmpBluetoothScanResults.add(new BluetoothDeviceData(btName, device.getAddress(),
                                                            BluetoothScanWorker.getBluetoothType(device), false, 0, false, true));
                                                }
                                            }
                                            break;
                                        case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                                            if (!BluetoothScanner.bluetoothDiscoveryStarted) {
                                                BluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }

                                            BluetoothScanner.finishCLScan(appContext);
                                            break;
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);

        }

    }

    /*
    public static boolean isAdapterNameScanned(DataWrapper dataWrapper, int connectionType)
    {
        if (isBluetoothConnected(dataWrapper.context, ""))
        {
            synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
                if (connectedDevices != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if (dataWrapper.getDatabaseHandler().isBluetoothAdapterNameScanned(_device.getName(), connectionType))
                            return true;
                    }
                }
                return false;
            }
        }
        else
            return false;
    }
    */

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<BluetoothDevice> deviceWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       BluetoothDevice device) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.deviceWeakRef = new WeakReference<>(device);
        }

    }*/

}
