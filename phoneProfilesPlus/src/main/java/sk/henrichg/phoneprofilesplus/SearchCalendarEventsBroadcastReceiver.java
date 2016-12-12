package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

public class SearchCalendarEventsBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "searchCalendarEvents";

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### SearchCalendarEventsBroadcastReceiver.onReceive", "xxx");

        setAlarm(context, false);

        if (!GlobalData.getApplicationStarted(context, true))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ SearchCalendarEventsBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            GlobalData.logE("SearchCalendarEventsBroadcastReceiver.onReceive", "calendarEventsExists=" + calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            //}

        }

    }

    @SuppressLint("NewApi")
    public static void setAlarm(Context context, boolean shortInterval)
    {
        removeAlarm(context);

        GlobalData.logE("SearchCalendarEventsBroadcastReceiver.setAlarm","xxx");

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, SearchCalendarEventsBroadcastReceiver.class);

        Calendar calendar = Calendar.getInstance();
        if (shortInterval)
            calendar.add(Calendar.SECOND, 5);
        else
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        long alarmTime = calendar.getTimeInMillis();

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            //alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        else if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            //alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        else
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    public static void removeAlarm(Context context)
    {
        GlobalData.logE("SearchCalendarEventsBroadcastReceiver.removeAlarm","xxx");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, SearchCalendarEventsBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("SearchCalendarEventsBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

}
