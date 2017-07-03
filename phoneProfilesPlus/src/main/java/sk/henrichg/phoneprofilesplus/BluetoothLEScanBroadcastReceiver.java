package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

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

        //PPApplication.loadPreferences(context);

        int forceOneScan = ScannerService.getForceOneLEBluetoothScan(context);

        if (Event.getGlobalEventsRuning(context) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","xxx");

                BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);

                BluetoothScanAlarmBroadcastReceiver.setWaitForLEResults(context, false);

                ScannerService.setForceOneLEBluetoothScan(context, ScannerService.FORCE_ONE_SCAN_DISABLED);

                if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                {
                    // start service
                    final Context _context = context.getApplicationContext();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LocalBroadcastManager.getInstance(_context).registerReceiver(PPApplication.startEventsServiceBroadcastReceiver, new IntentFilter("StartEventsServiceBroadcastReceiver"));
                            Intent startEventsServiceIntent = new Intent("StartEventsServiceBroadcastReceiver");
                            LocalBroadcastManager.getInstance(_context).sendBroadcast(startEventsServiceIntent);
                        }
                    }, 5000);
                    //setAlarm(context);
                }

            }

        }

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","----- end");

    }

}
