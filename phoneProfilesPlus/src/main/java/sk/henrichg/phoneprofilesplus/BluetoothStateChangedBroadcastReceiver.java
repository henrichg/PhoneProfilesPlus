package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class BluetoothStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothStateChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "BluetoothStateChangedBroadcastReceiver.onReceive", "BluetoothStateChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        String action = intent.getAction();
        if ((action != null) && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            // BluetoothStateChangedBroadcastReceiver

            final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            PPApplication.startHandlerThread("BluetoothStateChangedBroadcastReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothStateChangedBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BluetoothStateChangedBroadcastReceiver.onReceive");

                        // remove connected devices list
                        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
                            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, false);
                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                        }

                        if (Event.getGlobalEventsRunning(appContext)) {
                            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "state=" + bluetoothState);

                            if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                                if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                    //if ((!dataWrapper.getIsManualProfileActivation()) || PPApplication.getForceOneBluetoothScan(appContext))
                                    //{
                                    if (BluetoothScanWorker.getScanRequest(appContext)) {
                                        PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start classic scan");
                                        BluetoothScanWorker.startCLScan(appContext);
                                    } else if (BluetoothScanWorker.getLEScanRequest(appContext)) {
                                        PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start LE scan");
                                        BluetoothScanWorker.startLEScan(appContext);
                                    } else if (!(BluetoothScanWorker.getWaitForResults(appContext) ||
                                            BluetoothScanWorker.getWaitForLEResults(appContext))) {
                                        // refresh bounded devices
                                        BluetoothScanWorker.fillBoundedDevicesList(appContext);
                                    }
                                    //}
                                }

                                if (!(BluetoothScanWorker.getScanRequest(appContext) ||
                                        BluetoothScanWorker.getLEScanRequest(appContext) ||
                                        BluetoothScanWorker.getWaitForResults(appContext) ||
                                        BluetoothScanWorker.getWaitForLEResults(appContext) ||
                                        BluetoothScanWorker.getBluetoothEnabledForScan(appContext))) {

                                    // start events handler
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                    //}

                                /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                                boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                                dataWrapper.invalidateDataWrapper();

                                if (bluetoothEventsExists) {
                                */

                                    // start events handler
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_STATE);

                                    //}
                                }

                            }
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothStateChangedBroadcastReceiver.onReceive");
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
