package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

        //final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;
        if (ApplicationPreferences.prefForceOneBluetoothScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "xxx");

        //boolean scanStarted = (BluetoothScanWorker.getWaitForLEResults(context));
        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "scanStarted=" + scanStarted);

        //if (scanStarted) {
        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - callbackType=" + callbackType);
        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - result=" + result.toString());

        //String btAddress = _device.getAddress();
        String btName = _device.getName();
        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "deviceAddress=" + btAddress);
        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "deviceName=" + btName);

        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, _device.getAddress(),
                BluetoothScanWorker.getBluetoothType(_device), false, 0, false, true);

        BluetoothScanWorker.addLEScanResult(deviceData);

        /*
        PPApplication.startHandlerThreadBluetoothLECallback();
        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothLECallback.getLooper());
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BluetoothLEScanCallback21.onScanResult");

                    //boolean scanStarted = (BluetoothScanWorker.getWaitForLEResults(context));
                    //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "scanStarted=" + scanStarted);

                    //if (scanStarted) {
                        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - callbackType=" + callbackType);
                        //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - result=" + result.toString());

                        //String btAddress = _device.getAddress();
                        String btName = _device.getName();
                        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "deviceAddress=" + btAddress);
                        //PPApplication.logE("BluetoothLEScanCallback21.onScanResult", "deviceName=" + btName);

                        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, _device.getAddress(),
                                BluetoothScanWorker.getBluetoothType(_device), false, 0, false, true);

                        BluetoothScanWorker.addLEScanResult(deviceData);
                    //}

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothLEScanCallback21.onScanResult");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
        */
    }

    public void onBatchScanResults(List<ScanResult> results) {
        //CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onBatchScanResults", "BluetoothLEScanCallback21.onBatchScanResults");

        if ((results == null) || (results.size() == 0))
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneBluetoothLEScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        //PPApplication.logE("BluetoothLEScanCallback21.onBatchScanResults", "xxx");

        final List<ScanResult> _results = new ArrayList<>(results);

        //boolean scanStarted = (BluetoothScanWorker.getWaitForLEResults(context));
        //PPApplication.logE("BluetoothLEScanCallback21.onBatchScanResults", "scanStarted=" + scanStarted);

        /*
        //if (scanStarted) {
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
        //}
        */

        PPApplication.startHandlerThreadBluetoothLECallback();
        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothLECallback.getLooper());
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BluetoothLEScanCallback21.onBatchScanResults");

                    //boolean scanStarted = (BluetoothScanWorker.getWaitForLEResults(context));
                    //PPApplication.logE("BluetoothLEScanCallback21.onBatchScanResults", "scanStarted=" + scanStarted);

                    //if (scanStarted) {
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
                    //}

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
        Log.e("BluetoothLEScanCallback21.onScanFailed", "errorCode=" + errorCode);
        FirebaseCrashlytics.getInstance().log("BluetoothLEScanCallback21.onScanFailed errorCode=" + errorCode);
        //Crashlytics.log("BluetoothLEScanCallback21.onScanFailed errorCode=" + errorCode);
    }

}
