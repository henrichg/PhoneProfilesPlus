package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BluetoothLEScanCallback21 extends ScanCallback {

    private final Context context;

    BluetoothLEScanCallback21(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        //CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onScanResult", "BluetoothLEScanCallback21.onScanResult");

        final BluetoothDevice _device = result.getDevice();

        if (_device == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;
        if (ApplicationPreferences.prefForceOneBluetoothScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver21_onScanResult");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BluetoothLEScanCallback21.onScanResult");

                    String btName = _device.getName();

                    BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, _device.getAddress(),
                            BluetoothScanWorker.getBluetoothType(_device), false, 0, false, true);

                    BluetoothScanWorker.addLEScanResult(deviceData);
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

    public void onBatchScanResults(List<ScanResult> results) {
        //CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onBatchScanResults", "BluetoothLEScanCallback21.onBatchScanResults");

        if ((results == null) || (results.size() == 0))
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneBluetoothLEScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        final List<ScanResult> _results = new ArrayList<>(results);

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanCallback21_onBatchScanResults");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadBluetoothLECallback", "START run - from=BluetoothLEScanCallback21.onBatchScanResults");

                    for (ScanResult result : _results) {
                        //PPApplication.logE("BluetoothLEScanCallback21", "onBatchScanResults - result=" + result.toString());

                        BluetoothDevice device = result.getDevice();
                        //String btAddress = device.getAddress();
                        String btName = device.getName();
                        //PPApplication.logE("BluetoothLEScanCallback21.onBatchScanResults", "deviceAddress=" + btAddress);
                        //PPApplication.logE("BluetoothLEScanCallback21.onBatchScanResults", "deviceName=" + btName);

                        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                                BluetoothScanWorker.getBluetoothType(device), false, 0, false, true);

                        BluetoothScanWorker.addLEScanResult(deviceData);
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothLEScanCallback21.onBatchScanResults");
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

    public void onScanFailed(int errorCode) {
        //Log.e("BluetoothLEScanCallback21.onScanFailed", "errorCode=" + errorCode);
        PPApplication.logToCrashlytics("E/BluetoothLEScanCallback21.onScanFailed: errorCode=" + errorCode);
    }

}
