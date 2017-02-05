package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BluetoothLEScanBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothLEScan";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive", "----- start");

        //if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
        //    BluetoothScanAlarmBroadcastReceiver.bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        int forceOneScan = PPApplication.getForceOneLEBluetoothScan(context);

        if (PPApplication.getGlobalEventsRuning(context) || (forceOneScan == PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","xxx");

                BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);

                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);

                PPApplication.setForceOneLEBluetoothScan(context, PPApplication.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    // start service
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                }

            }

        }

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","----- end");

    }

}
