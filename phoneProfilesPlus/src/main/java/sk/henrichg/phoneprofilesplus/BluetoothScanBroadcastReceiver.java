package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

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
                    // may be not invoked if not any BT is around

                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);
                    }
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // When discovery finds a device

                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);
                    }

                    if (tmpScanResults == null)
                        tmpScanResults = new ArrayList<>();

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
                                BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device), false, 0));
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    if (!discoveryStarted) {
                        discoveryStarted = true;
                        BluetoothScanAlarmBroadcastReceiver.fillBoundedDevicesList(context);
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
                tmpScanResults = null;
            }

            /*
            if (BluetoothScanAlarmBroadcastReceiver.scanResults != null)
            {
                for (BluetoothDevice device : BluetoothScanAlarmBroadcastReceiver.scanResults)
                {
                    PPApplication.logE("BluetoothScanBroadcastReceiver.onReceive","device.name="+device.getName());
                }
            }
            */

            BluetoothScanAlarmBroadcastReceiver.saveCLScanResults(context, scanResults);

            BluetoothScanAlarmBroadcastReceiver.setWaitForResults(context, false);

            int forceOneScan = PPApplication.getForceOneBluetoothScan(context);
            PPApplication.setForceOneBluetoothScan(context, PPApplication.FORCE_ONE_SCAN_DISABLED);

            if (forceOneScan != PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
            {
                // start service
                //setAlarm(context);
                Intent _intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);
                context.sendBroadcast(_intent);
            }
        }
    }

    static private void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint("NewApi")
    static private void setAlarm(Context context)
    {
        removeAlarm(context);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);
        long alarmTime = calendar.getTimeInMillis();

        if (android.os.Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else if (android.os.Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

}
