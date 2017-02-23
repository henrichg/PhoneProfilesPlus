package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class BluetoothScanBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScan";

    private static List<BluetoothDeviceData> tmpScanResults = null;
    public static boolean discoveryStarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- start");

        if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
            BluetoothScanAlarmBroadcastReceiver.bluetooth = BluetoothScanAlarmBroadcastReceiver.getBluetoothAdapter(context);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        int forceOneScan = PPApplication.getForceOneBluetoothScan(context);

        if (PPApplication.getGlobalEventsRuning(context) || (forceOneScan == PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","xxx");

                String action = intent.getAction();

                PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","action="+action);

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    if (!discoveryStarted) {
                        discoveryStarted = true;

                        BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);

                        if (tmpScanResults == null)
                            tmpScanResults = new ArrayList<>();
                        else
                            tmpScanResults.clear();
                    }
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // When discovery finds a device

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String btNameD = device.getName();
                    String btNameE = "";
                    String btName = btNameD;
                    if (intent.hasExtra(BluetoothDevice.EXTRA_NAME)) {
                        btNameE = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                        btName = btNameE;
                    }

                    PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","deviceName_d="+btNameD);
                    PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","deviceName_e="+btNameE);
                    PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","deviceAddress="+device.getAddress());

                    boolean found = false;
                    for (BluetoothDeviceData _device : tmpScanResults)
                    {
                        if (_device.address.equals(device.getAddress()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        tmpScanResults.add(new BluetoothDeviceData(btName, device.getAddress(),
                                BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device), false));
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    finishScan(context);
                }

            }

        }

        //PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- end");

    }

    static public void initTmpScanResults()
    {
        if (tmpScanResults != null)
            tmpScanResults.clear();
        else
            tmpScanResults = new ArrayList<>();
    }

    static public void finishScan(Context context) {
        PPApplication.logE("BluetoothScanBroadcastReceiver.finishScan","discoveryStarted="+discoveryStarted);

        if (discoveryStarted) {

            discoveryStarted = false;

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            for (BluetoothDeviceData device : tmpScanResults) {
                scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false));
            }
            tmpScanResults.clear();

            BluetoothScanAlarmBroadcastReceiver.saveScanResults(context, scanResults);

            /*
            if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
            {
                for (BluetoothDevice device : BluetoothScanAlarmBroadcastReceiver.scanResults)
                {
                    PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive","device.name="+device.getName());
                }
            }
            */

            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);

            int forceOneScan = PPApplication.getForceOneBluetoothScan(context);
            PPApplication.setForceOneBluetoothScan(context, PPApplication.FORCE_ONE_SCAN_DISABLED);

            if (forceOneScan != PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
            {
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            }
        }
    }

}
