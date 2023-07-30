package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        BluetoothScanWorker.fillBoundedDevicesList(appContext);

        final int forceOneScan = ApplicationPreferences.prefForceOneBluetoothLEScan;

        if (EventStatic.getGlobalEventsRunning(context) || (forceOneScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            //if (scanStarted) {


            BluetoothScanWorker.setWaitForLEResults(appContext, false);
            BluetoothScanner.setForceOneLEBluetoothScan(appContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

            if (forceOneScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
            {
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, PPExecutors.SENSOR_NAME_SENSOR_TYPE_BLUETOOTH_SCANNER, 5);
            }
            //}

        }
    }

}
