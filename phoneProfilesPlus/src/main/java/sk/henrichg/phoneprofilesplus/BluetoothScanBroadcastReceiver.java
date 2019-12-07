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

        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;
        if (WifiBluetoothScanner.getForceOneBluetoothScan(appContext) != WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext))
                // scanning is disabled
                return;
        }

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

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive", "device=" + device);
                PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive", "deviceName=" + deviceName);
            }

            PPApplication.startHandlerThread("BluetoothScanBroadcastReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothScanBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BluetoothScanBroadcastReceiver.onReceive");

                        if (BluetoothScanWorker.bluetooth == null)
                            BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(appContext);

                        if (BluetoothScanWorker.bluetooth != null) {
                            int forceOneScan = WifiBluetoothScanner.getForceOneBluetoothScan(appContext);

                            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                                boolean scanStarted = (BluetoothScanWorker.getWaitForResults(appContext));

                                if (scanStarted) {
                                    PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive", "action=" + action);

                                    switch (action) {
                                        case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                                            // may be not invoked if not any BT is around

                                            if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                                WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }
                                            break;
                                        case BluetoothDevice.ACTION_FOUND:
                                            // When discovery finds a device

                                            if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                                WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }

                                            synchronized (PPApplication.bluetoothScanMutex) {
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
                                                            BluetoothScanWorker.getBluetoothType(device), false, 0, false, true));
                                                }
                                            }
                                            break;
                                        case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                                            if (!WifiBluetoothScanner.bluetoothDiscoveryStarted) {
                                                WifiBluetoothScanner.bluetoothDiscoveryStarted = true;
                                                BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                            }

                                            BluetoothScanWorker.finishCLScan(appContext);
                                            break;
                                    }
                                }
                            }
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothScanBroadcastReceiver.onReceive");
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
