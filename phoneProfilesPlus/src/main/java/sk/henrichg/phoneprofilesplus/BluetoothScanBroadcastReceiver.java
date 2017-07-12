package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

    private static List<BluetoothDeviceData> tmpScanResults = null;
    public static boolean discoveryStarted = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothScanBroadcastReceiver.onReceive","xxx");
        //PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- start");

        if (BluetoothScanJob.bluetooth == null)
            BluetoothScanJob.bluetooth = BluetoothScanJob.getBluetoothAdapter(context);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        int forceOneScan = ScannerService.getForceOneBluetoothScan(context);

        if (Event.getGlobalEventsRuning(context) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanJob.getWaitForResults(context));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","xxx");

                String action = intent.getAction();

                PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","action="+action);

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    // may be not invoked if not any BT is around

                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanJob.fillBoundedDevicesList(context);
                    }
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // When discovery finds a device

                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanJob.fillBoundedDevicesList(context);
                    }

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

                    if (tmpScanResults == null)
                        tmpScanResults = new ArrayList<>();

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
                                BluetoothScanJob.getBluetoothType(device), false, 0));
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanJob.fillBoundedDevicesList(context);
                    }

                    finishScan(context);
                }

            }

        }

        //PPApplication.logE("@@@ BluetoothScanBroadcastReceiver.onReceive","----- end");

    }

    static public void finishScan(Context context) {
        PPApplication.logE("BluetoothScanBroadcastReceiver.finishScan","discoveryStarted="+discoveryStarted);

        if (discoveryStarted) {

            discoveryStarted = false;

            List<BluetoothDeviceData> scanResults = new ArrayList<>();

            if (tmpScanResults != null) {

                for (BluetoothDeviceData device : tmpScanResults) {
                    scanResults.add(new BluetoothDeviceData(device.getName(), device.address, device.type, false, 0));
                }
            }

            BluetoothScanJob.saveCLScanResults(context, scanResults);

            BluetoothScanJob.setWaitForResults(context, false);

            int forceOneScan = ScannerService.getForceOneBluetoothScan(context);
            ScannerService.setForceOneBluetoothScan(context, ScannerService.FORCE_ONE_SCAN_DISABLED);

            if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
            {
                // start service
                final Context _context = context.getApplicationContext();
                new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(_context).registerReceiver(PPApplication.startEventsServiceBroadcastReceiver, new IntentFilter("StartEventsServiceBroadcastReceiver"));
                        Intent startEventsServiceIntent = new Intent("StartEventsServiceBroadcastReceiver");
                        LocalBroadcastManager.getInstance(_context).sendBroadcast(startEventsServiceIntent);
                    }
                }, 5000);
                //setAlarm(context);
            }

            tmpScanResults = null;
        }
    }

}
