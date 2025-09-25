package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

// callbeck for get scanned LE BT
// supported is one device scan  and batch scan
class BluetoothLEScanCallback extends ScanCallback {

    private final Context context;

    BluetoothLEScanCallback(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothLEScanCallback.onScanResult", "xxx");

        BluetoothDevice _device = result.getDevice();

        if (_device == null)
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

        final Context appContext = context.getApplicationContext();
        final WeakReference<BluetoothDevice> deviceWeakRef = new WeakReference<>(_device);
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothLEScanCallback.onScanResult");

            //Context appContext= appContextWeakRef.get();
            BluetoothDevice device = deviceWeakRef.get();

            if (/*(appContext != null) &&*/ (device != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothLEScanCallback21_onScanResult);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    @SuppressLint("MissingPermission")
                    String btName = device.getName();
                    if ((btName != null) && (!btName.isEmpty())) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] BluetoothLEScanCallback.onScanResult", "btName="+btName);

//                        Log.e("BluetoothLEScanCallback.onScanResult", "addLEScanResult()="+btName);

                        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                                BluetoothScanWorker.getBluetoothType(device), false, 0, false, true);

                        BluetoothScanWorker.addLEScanResult(deviceData);
                    }
                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        };
        PPApplicationStatic.createScannersExecutor();
        PPApplication.scannersExecutor.submit(runnable);
    }

    public void onBatchScanResults(List<ScanResult> results) {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothLEScanCallback.onBatchScanResults", "xxx");

        if ((results == null) || (results.isEmpty()))
            return;

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneBluetoothLEScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            boolean scanningPaused = ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2") &&
                    GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo);
            if ((!ApplicationPreferences.applicationEventBluetoothEnableScanning) || scanningPaused)
                // scanning is disabled
                return;
        }

        for (ScanResult result : results) {
            final BluetoothDevice device = result.getDevice();

            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadBluetoothLECallback", "START run - from=BluetoothLEScanCallback.onBatchScanResults");

                //Context appContext= appContextWeakRef.get();
                //BluetoothDevice device = deviceWeakRef.get();

                //if ((appContext != null) && (device != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothLEScanCallback21_onBatchScanResults);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //String btAddress = device.getAddress();
                        @SuppressLint("MissingPermission")
                        String btName = device.getName();

                        if ((btName != null) && (!btName.isEmpty())) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] BluetoothLEScanCallback.onBatchScanResults", "btName="+btName);

//                            Log.e("BluetoothLEScanCallback.onBatchScanResults", "addLEScanResult()="+btName);

                            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                                    BluetoothScanWorker.getBluetoothType(device), false, 0, false, true);

                            BluetoothScanWorker.addLEScanResult(deviceData);
                        }

                    } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            };
            PPApplicationStatic.createScannersExecutor();
            PPApplication.scannersExecutor.submit(runnable);

        }

    }

    public void onScanFailed(int errorCode) {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothLEScanCallback.onScanFailed", "xxx");

        Log.e("BluetoothLEScanCallback.onScanFailed", "errorCode=" + errorCode);
        if (PPApplicationStatic.logEnabled() &&
                PPApplicationStatic.logContainsFilterTag("BluetoothLEScanCallback.onScanFailed")) {
            PPApplicationStatic.logIntoFile("E", "BluetoothLEScanCallback.onScanFailed", "errorCode=" + errorCode, false);
        }
    }

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
