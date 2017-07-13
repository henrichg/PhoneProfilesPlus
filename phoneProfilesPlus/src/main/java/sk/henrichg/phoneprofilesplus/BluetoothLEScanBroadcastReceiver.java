package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive", "----- start");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        int forceOneScan = ScannerService.getForceOneLEBluetoothScan(context);

        if (Event.getGlobalEventsRuning(context) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","xxx");

                BluetoothScanJob.fillBoundedDevicesList(context);

                BluetoothScanJob.setWaitForLEResults(context, false);

                ScannerService.setForceOneLEBluetoothScan(context, ScannerService.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    // start service
                    final Context _context = context.getApplicationContext();
                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent eventsServiceIntent = new Intent(_context, EventsService.class);
                            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_BLUETOOTH_SCANNER);
                            WakefulIntentService.sendWakefulWork(_context, eventsServiceIntent);
                        }
                    }, 5000);
                }

            }

        }

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","----- end");

    }

}
