package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.List;

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

//            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothStateChangedBroadcastReceiver.onReceive", "action="+action);

            final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothStateChangedBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothStateChangedBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // remove connected devices list
                        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
//                            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothStateChangedBroadcastReceiver.onReceive", "bluetoothState=STATE_OFF");

                            List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(connectedDevices/*appContext, false*/);
                            // this also clears shared preferences
                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                        }
                        if (bluetoothState == BluetoothAdapter.STATE_ON) {
//                            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothStateChangedBroadcastReceiver.onReceive", "bluetoothState=STATE_ON");

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

//                            PPApplicationStatic.logE("BluetoothStateChangedBroadcastReceiver.onReceive", "BT==ON, call Detector");
                            BluetoothConnectedDevicesDetector.getConnectedDevices(appContext, false);
                        }

                        if (EventStatic.getGlobalEventsRunning(appContext)) {

                            if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                                if (ApplicationPreferences.prefEventBluetoothScanRequest ||
                                        ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                                        ApplicationPreferences.prefEventBluetoothWaitForResult ||
                                        ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                                        ApplicationPreferences.prefEventBluetoothEnabledForScan)
                                    PhoneProfilesServiceStatic.cancelBluetoothWorker(appContext, true, false);


                                    // start events handler

//                                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] BluetoothStateChangedBroadcastReceiver.onReceive", "SENSOR_TYPE_RADIO_SWITCH,SENSOR_TYPE_BLUETOOTH_STATE,SENSOR_TYPE_BLUETOOTH_CONNECTION");
//                                PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothStateChangedBroadcastReceiver.onReceive", "call of handle events");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(new int[]{
                                        EventsHandler.SENSOR_TYPE_RADIO_SWITCH,
                                        EventsHandler.SENSOR_TYPE_BLUETOOTH_STATE,
                                        EventsHandler.SENSOR_TYPE_BLUETOOTH_CONNECTION});

                                PPApplicationStatic.restartBluetoothScanner(appContext);
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
            };
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }

    }
}
