package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BluetoothStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothState";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothStateChangedBroadcastReceiver.onReceive", "xxx");

        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        // remove connected devices list
        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(context, false);
            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(context);
        }

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        if (Event.getGlobalEventsRuning(context))
        {
            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","state="+bluetoothState);

            if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                if (bluetoothState == BluetoothAdapter.STATE_ON)
                {
                    //if ((!dataWrapper.getIsManualProfileActivation()) || PPApplication.getForceOneBluetoothScan(context))
                    //{
                        if (BluetoothScanAlarmBroadcastReceiver.getScanRequest(context))
                        {
                            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start classic scan");
                            BluetoothScanAlarmBroadcastReceiver.startCLScan(context.getApplicationContext());
                        }
                        else
                        if (BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context))
                        {
                            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start LE scan");
                            BluetoothScanAlarmBroadcastReceiver.startLEScan(context.getApplicationContext());
                        }
                        else
                        if (!(BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context) ||
                              BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context)))
                        {
                            // refresh bounded devices
                            BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);
                        }
                    //}
                }

                if (!((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context)) ||
                        (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context)))) {
                    // required for Bluetooth ConnectionType="Not connected"

                    //if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {
                        /*Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
                        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_BLUETOOTH);
                        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, bluetoothState == BluetoothAdapter.STATE_ON);
                        context.sendBroadcast(broadcastIntent);*/
                        Intent broadcastIntent = new Intent("RadioSwitchBroadcastReceiver");
                        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_BLUETOOTH);
                        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, bluetoothState == BluetoothAdapter.STATE_ON);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

                    //}

                    /*DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                    boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                    dataWrapper.invalidateDataWrapper();

                    if (bluetoothEventsExists) {
                        PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "bluetoothEventsExists=" + bluetoothEventsExists);
                    */
                        // start service
                        Intent eventsServiceIntent = new Intent(context, EventsService.class);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                        startWakefulService(context, eventsServiceIntent);
                        //}
                }

            }
        }

        /*
        if (bluetoothState == BluetoothAdapter.STATE_OFF)
        {
            BluetoothScanAlarmBroadcastReceiver.stopScan(context);
        }
        */

    }
}
