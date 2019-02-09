package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothLEScanBroadcastReceiver.onReceive", "BluetoothLEScanBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //BluetoothJob.startForLEScanBroadcast(context.getApplicationContext());

        final int forceOneScan = WifiBluetoothScanner.getForceOneLEBluetoothScan(appContext);

        if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.1");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(appContext));

                        if (scanStarted) {
                            PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive", "xxx");


                            BluetoothScanJob.fillBoundedDevicesList(appContext);

                            BluetoothScanJob.setWaitForLEResults(appContext, false);

                            WifiBluetoothScanner.setForceOneLEBluetoothScan(appContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                            if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                            {
                            /*// start job
                            PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.2");
                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothLEScanBroadcastReceiver.onReceive.Handler.postDelayed");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                                    // start events handler
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                                    } finaly {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }, 5000);*/
                                PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, 5, appContext);
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

}
