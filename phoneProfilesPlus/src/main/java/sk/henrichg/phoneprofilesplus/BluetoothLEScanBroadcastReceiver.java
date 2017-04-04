package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
                    setAlarm(context);
                    //Intent _intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);
                    //context.sendBroadcast(_intent);
                }

            }

        }

        PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","----- end");

    }

    private void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint("NewApi")
    private void setAlarm(Context context)
    {
        removeAlarm(context);

        Intent intent = new Intent(context, StartEventsServiceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

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
