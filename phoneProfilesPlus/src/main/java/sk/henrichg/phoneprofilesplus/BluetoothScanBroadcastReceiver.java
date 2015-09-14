package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class BluetoothScanBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothScan";

    private static List<BluetoothDeviceData> tmpScanResults = null;
    public static boolean discoveryStarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        //GlobalData.logE("#### BluetoothScanBroadcastReceiver.onReceive","xxx");
        GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- start");

        if (BluetoothScanAlarmBroadcastReceiver.bluetooth == null)
            BluetoothScanAlarmBroadcastReceiver.bluetooth = (BluetoothAdapter) BluetoothAdapter.getDefaultAdapter();

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {

            boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context));

            if (scanStarted)
            {
                GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","xxx");

                String action = intent.getAction();

                GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","action="+action);

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    if (!discoveryStarted) {
                        discoveryStarted = true;

                        BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);

                        if (tmpScanResults == null)
                            tmpScanResults = new ArrayList<BluetoothDeviceData>();
                        else
                            tmpScanResults.clear();
                    }
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // When discovery finds a device

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String btName = device.getName();
                    if (intent.hasExtra(BluetoothDevice.EXTRA_NAME))
                        btName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

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
                        tmpScanResults.add(new BluetoothDeviceData(btName, device.getAddress()));
                        GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","deviceName="+btName);
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    discoveryStarted = false;

                    List<BluetoothDeviceData> scanResults = new ArrayList<BluetoothDeviceData>();

                    for (BluetoothDeviceData device : tmpScanResults)
                    {
                        scanResults.add(new BluetoothDeviceData(device.getName(), device.address));
                    }
                    tmpScanResults.clear();

                    BluetoothScanAlarmBroadcastReceiver.saveScanResults(context, scanResults);

                    /*
                    if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
                    {
                        for (BluetoothDevice device : BluetoothScanAlarmBroadcastReceiver.scanResults)
                        {
                            GlobalData.logE("BluetoothScanBroadcastReceiver.onReceive","device.name="+device.getName());
                        }
                    }
                    */

                    BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);

                    boolean forceOneScan = GlobalData.getForceOneBluetoothScan(context);
                    GlobalData.setForceOneBluetoothScan(context, false);

                    if (!forceOneScan) // not start service for force scan
                    {
                        // start service
                        Intent eventsServiceIntent = new Intent(context, EventsService.class);
                        eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                        startWakefulService(context, eventsServiceIntent);
                    }

                }

            }

        }

        GlobalData.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- end");

    }

    static public void initTmpScanResults()
    {
        if (tmpScanResults != null)
            tmpScanResults.clear();
        else
            tmpScanResults = new ArrayList<BluetoothDeviceData>();
    }

}
