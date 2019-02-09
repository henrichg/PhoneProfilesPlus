package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEScanCallback18 implements BluetoothAdapter.LeScanCallback {

    private final Context context;

    BluetoothLEScanCallback18(Context _context) {
        context = _context;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback18.onLeScan", "BluetoothLEScanCallback18_onLeScan");

        final Context appContext = context.getApplicationContext();
        final BluetoothDevice _device = device;

        PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.1");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver18.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

                    if (scanStarted) {
                        //PPApplication.logE("BluetoothLEScanCallback18", "onLeScan - device=" + device.toString());

                        String btName = _device.getName();
                        PPApplication.logE("BluetoothLEScanCallback18", "onLeScan - deviceName=" + btName);

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

}
