package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class BluetoothStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent == null)
            return;

        String action = intent.getAction();
        if ((action != null) && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            // BluetoothStateChangedBroadcastReceiver

            final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"BluetoothStateChangedBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothStateChangedBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothStateChangedBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // remove connected devices list
                        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
                            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, false);
                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                        }

                        if (EventStatic.getGlobalEventsRunning(appContext)) {

                            if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                                if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                    //if ((!dataWrapper.getIsManualProfileActivation()) || PPApplication.getForceOneBluetoothScan(appContext))
                                    //{
                                    //if (ApplicationPreferences.prefEventBluetoothScanRequest) {
                                    //    BluetoothScanWorker.startCLScan(appContext);
                                    //} else if (ApplicationPreferences.prefEventBluetoothLEScanRequest) {
                                    //    BluetoothScanWorker.startLEScan(appContext);
                                    //} else
                                    if (!(ApplicationPreferences.prefEventBluetoothWaitForResult ||
                                            ApplicationPreferences.prefEventBluetoothLEWaitForResult)) {
                                        // refresh bounded devices
                                        BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                    }
                                    //}
                                }

                                if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                                        ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                                        ApplicationPreferences.prefEventBluetoothWaitForResult ||
                                        ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                                        ApplicationPreferences.prefEventBluetoothEnabledForScan)) {

                                    // start events handler

//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] BluetoothStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                    // start events handler

//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] BluetoothStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_BLUETOOTH_STATE");
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_STATE);

                                    // start events handler

//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] BluetoothStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_BLUETOOTH_CONNECTION");
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_CONNECTION);
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
}
