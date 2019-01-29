package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.util.ArrayList;

import static android.content.Context.POWER_SERVICE;

public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothScanBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "BluetoothScanBroadcastReceiver.onReceive", "BluetoothScanBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        /*BluetoothJob.startForScanBroadcast(context.getApplicationContext(), intent.getAction(),
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE),
                intent.getStringExtra(BluetoothDevice.EXTRA_NAME));*/

        final String action = intent.getAction();

        PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive","action="+action);

        if (action == null)
            return;


        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED) ||
                action.equals(BluetoothDevice.ACTION_FOUND) ||
                action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            // BluetoothScanBroadcastReceiver

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

            PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive","device="+device);
            PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive","deviceName="+deviceName);

            PPApplication.startHandlerThread("BluetoothScanBroadcastReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothScanBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (BluetoothScanJob.bluetooth == null)
                            BluetoothScanJob.bluetooth = BluetoothScanJob.getBluetoothAdapter(appContext);

                        if (BluetoothScanJob.bluetooth != null) {
                            int forceOneScan = WifiBluetoothScanner.getForceOneBluetoothScan(appContext);

                            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                                boolean scanStarted = (BluetoothScanJob.getWaitForResults(appContext));

                                if (scanStarted) {
                                    PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "action=" + action);

                                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                                        // may be not invoked if not any BT is around

                                        if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                            WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                            BluetoothScanJob.fillBoundedDevicesList(appContext);
                                        }
                                    } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                        // When discovery finds a device

                                        if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                            WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                            BluetoothScanJob.fillBoundedDevicesList(appContext);
                                        }

                                        synchronized (PPApplication.bluetoothScanMutex) {
                                            //noinspection ConstantConditions
                                            String btNameD = device.getName();
                                            String btNameE = "";
                                            String btName = btNameD;
                                            if (deviceName != null) {
                                                btNameE = deviceName;
                                                btName = btNameE;
                                            }

                                            PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "deviceName_d=" + btNameD);
                                            PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "deviceName_e=" + btNameE);
                                            PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "deviceAddress=" + device.getAddress());

                                            if (WifiBluetoothScanner.tmpBluetoothScanResults == null)
                                                WifiBluetoothScanner.tmpBluetoothScanResults = new ArrayList<>();

                                            boolean found = false;
                                            for (BluetoothDeviceData _device : WifiBluetoothScanner.tmpBluetoothScanResults) {
                                                if (_device.address.equals(device.getAddress())) {
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                for (BluetoothDeviceData _device : WifiBluetoothScanner.tmpBluetoothScanResults) {
                                                    if (_device.getName().equalsIgnoreCase(device.getName())) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "found=" + found);
                                            if (!found) {
                                                WifiBluetoothScanner.tmpBluetoothScanResults.add(new BluetoothDeviceData(btName, device.getAddress(),
                                                        BluetoothScanJob.getBluetoothType(device), false, 0, false, true));
                                            }
                                        }
                                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                                        if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                            WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                            BluetoothScanJob.fillBoundedDevicesList(appContext);
                                        }

                                        BluetoothScanJob.finishScan(appContext);
                                    }
                                }
                            }
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

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

}
