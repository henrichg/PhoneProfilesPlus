package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BluetoothLEScanCallback21 extends ScanCallback {

    private final Context context;

    BluetoothLEScanCallback21(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onScanResult", "BluetoothLEScanCallback21.onScanResult");

        final Context appContext = context.getApplicationContext();
        final BluetoothDevice _device = result.getDevice();

        PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.1");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver21_onScanResult");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

                    if (scanStarted) {
                        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - callbackType=" + callbackType);
                        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - result=" + result.toString());

                        String btName = _device.getName();
                        PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - deviceName=" + btName);

                        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, _device.getAddress(),
                                BluetoothScanJob.getBluetoothType(_device), false, 0, false, true);

                        BluetoothScanJob.addLEScanResult(deviceData);
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

    public void onBatchScanResults(List<ScanResult> results) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onBatchScanResults", "BluetoothLEScanCallback21.onBatchScanResults");

        final Context appContext = context.getApplicationContext();
        final List<ScanResult> _results = new ArrayList<>(results);

        PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.1");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanCallback21_onBatchScanResults");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

                    if (scanStarted) {
                        for (ScanResult result : _results) {
                            //PPApplication.logE("BluetoothLEScanCallback21", "onBatchScanResults - result=" + result.toString());

                            BluetoothDevice device = result.getDevice();
                            String btName = device.getName();
                            PPApplication.logE("BluetoothLEScanCallback21", "onBatchScanResults - deviceName=" + btName);

                            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                                    BluetoothScanJob.getBluetoothType(device), false, 0, false, true);

                            BluetoothScanJob.addLEScanResult(deviceData);
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

    public void onScanFailed(int errorCode) {
        PPApplication.logE("BluetoothLEScanCallback21", "onScanFailed - errorCode=" + errorCode);
    }

}
