package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

// result broadcast for LE BT scanning
public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        Runnable runnable = () -> {

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothLEScanBroadcastReceiver_onReceive);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                BluetoothScanWorker.fillBoundedDevicesList(appContext);

                final int forceOneScan = ApplicationPreferences.prefForceOneBluetoothLEScan;

                if (EventStatic.getGlobalEventsRunning(context) || (forceOneScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                    //if (scanStarted) {


                    BluetoothScanWorker.setWaitForLEResults(appContext, false);
                    BluetoothScanner.setForceOneLEBluetoothScan(appContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                    if (forceOneScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                    {
    //                Log.e("BluetoothLEScanBroadcastReceiver.onReceive", "call event handler SENSOR_TYPE_BLUETOOTH_SCANNER");
//                        PPApplicationStatic.logE("[DELAYED_EXECUTOR_CALL] BluetoothLEScanBroadcastReceiver.onReceive", "PPExecutors.handleEvents");
                        PPExecutors.handleEvents(appContext,
                                new int[]{EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER},
                                PPExecutors.SENSOR_NAME_SENSOR_TYPE_BLUETOOTH_SCANNER, 5);
                    }
                    //}

                }

            } catch (Exception e) {
//                PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] BluetoothLEScanBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }

        };
//        PPApplicationStatic.logE("[EXECUTOR_CALL] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

}
