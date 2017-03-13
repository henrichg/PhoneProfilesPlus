package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

        PPApplication.loadPreferences(context);

        // start delayed bootup broadcast
        PPApplication.startedOnBoot = true;
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent delayedBootUpIntent = new Intent(context, DelayedBootUpReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, delayedBootUpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot="+ PPApplication.applicationStartOnBoot);
        //PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+PPApplication.getGlobalEventsRuning(context));

        BluetoothConnectionBroadcastReceiver.clearConnectedDevices(context, true);
        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(context);

        PPApplication.setApplicationStarted(context, false);

        if (PPApplication.applicationStartOnBoot)
        {
            PPApplication.logE("BootUpReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            // start ReceiverService
            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PPApplication.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_START_ON_BOOT, true);
            context.startService(serviceIntent);
        }

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

    }

}
