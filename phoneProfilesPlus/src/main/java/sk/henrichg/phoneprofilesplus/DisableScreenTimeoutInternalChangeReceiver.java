package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class DisableScreenTimeoutInternalChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### DisableScreenTimeoutInternalChangeReceiver.onReceive", "xxx");

        internalChange = false;

    }

    public static void removeAlarm(Context context/*, boolean oneshot*/)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DisableScreenTimeoutInternalChangeReceiver.class);
        PendingIntent pendingIntent =  PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void setAlarm(Context context) {
        removeAlarm(context);

        //Context context = getApplicationContext();
        Intent _intent = new Intent(context, DisableScreenTimeoutInternalChangeReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1, _intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);
        long alarmTime = calendar.getTimeInMillis();

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

}
