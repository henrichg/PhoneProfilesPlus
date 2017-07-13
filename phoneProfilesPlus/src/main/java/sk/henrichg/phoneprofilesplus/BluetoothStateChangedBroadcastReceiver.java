package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BluetoothStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothStateChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        // remove connected devices list
        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, false);
            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
        }

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive","state="+bluetoothState);

            if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                if (bluetoothState == BluetoothAdapter.STATE_ON)
                {
                    //if ((!dataWrapper.getIsManualProfileActivation()) || PPApplication.getForceOneBluetoothScan(appContext))
                    //{
                        if (BluetoothScanJob.getScanRequest(appContext))
                        {
                            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start classic scan");
                            BluetoothScanJob.startCLScan(appContext);
                        }
                        else
                        if (BluetoothScanJob.getLEScanRequest(appContext))
                        {
                            PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "start LE scan");
                            BluetoothScanJob.startLEScan(appContext);
                        }
                        else
                        if (!(BluetoothScanJob.getWaitForResults(appContext) ||
                              BluetoothScanJob.getWaitForLEResults(appContext)))
                        {
                            // refresh bounded devices
                            BluetoothScanJob.fillBoundedDevicesList(appContext);
                        }
                    //}
                }

                if (!((BluetoothScanJob.getScanRequest(appContext)) ||
                        (BluetoothScanJob.getLEScanRequest(appContext)) ||
                        (BluetoothScanJob.getWaitForResults(appContext)) ||
                        (BluetoothScanJob.getWaitForLEResults(appContext)) ||
                        (BluetoothScanJob.getBluetoothEnabledForScan(appContext)))) {
                    // required for Bluetooth ConnectionType="Not connected"

                    //if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                        Intent eventsServiceIntent = new Intent(context, EventsService.class);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_BLUETOOTH);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, bluetoothState == BluetoothAdapter.STATE_ON);
                        WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);

                    //}

                    /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                    boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                    dataWrapper.invalidateDataWrapper();

                    if (bluetoothEventsExists) {
                        PPApplication.logE("@@@ BluetoothStateChangedBroadcastReceiver.onReceive", "bluetoothEventsExists=" + bluetoothEventsExists);
                    */
                        // start service
                        Intent eventsServiceIntent2 = new Intent(appContext, EventsService.class);
                        eventsServiceIntent2.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                        WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent2);
                        //}
                }

            }
        }
    }
}
