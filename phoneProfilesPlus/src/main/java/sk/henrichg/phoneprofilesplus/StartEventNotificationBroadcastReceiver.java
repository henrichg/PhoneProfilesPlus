package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class StartEventNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### StartEventNotificationBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "StartEventNotificationBroadcastReceiver.onReceive", "StartEventNotificationBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();
        final long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
        PPApplication.startHandlerThread("StartEventNotificationBroadcastReceiver.onReceive");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (event_id != 0) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartEventNotificationBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                        Event event = databaseHandler.getEvent(event_id);
                        if (event != null)
                            event.notifyEventStart(appContext);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        });
    }

    static void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            //Intent intent = new Intent(_context, StartEventNotificationBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
            //intent.setClass(context, StartEventNotificationBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("StartEventNotificationBroadcastReceiver.removeAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Event event, Context context)
    {
        //if (!_permanentRun) {

            //Intent intent = new Intent(_context, StartEventNotificationBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
            //intent.setClass(context, StartEventNotificationBroadcastReceiver.class);

            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                        ApplicationPreferences.applicationUseAlarmClock(context)) {

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, event._repeatNotificationIntervalStart);
                    long alarmTime = now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("StartEventNotificationBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    long alarmTime = SystemClock.elapsedRealtime() + event._repeatNotificationIntervalStart * 1000;

                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
            }
        //}
    }

}
