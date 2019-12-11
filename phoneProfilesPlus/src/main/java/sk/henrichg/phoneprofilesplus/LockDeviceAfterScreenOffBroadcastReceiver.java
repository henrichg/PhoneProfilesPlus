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

public class LockDeviceAfterScreenOffBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF = PPApplication.PACKAGE_NAME + ".LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "LockDeviceAfterScreenOffBroadcastReceiver_onReceive");
        CallsCounter.logCounterNoInc(context, "LockDeviceAfterScreenOffBroadcastReceiver.onReceive->action="+intent.getAction(), "LockDeviceAfterScreenOffBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        String action = intent.getAction();
        if (action != null) {

            PPApplication.logE("LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "action="+action);

            if (action.equals(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF)) {
                PPApplication.startHandlerThread("LockDeviceAfterScreenOffBroadcastReceiver.onReceive.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LockDeviceAfterScreenOffBroadcastReceiver_onReceive_ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=LockDeviceAfterScreenOffBroadcastReceiver.onReceive.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF");

                            if (Event.getGlobalEventsRunning(appContext)) {
                                PPApplication.logE("LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "handle events");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=LockDeviceAfterScreenOffBroadcastReceiver.onReceive.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("NewApi")
    static void setAlarm(int lockDelay, Context context)
    {
        final Context appContext = context.getApplicationContext();

        //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
        //intent.setClass(context, PostDelayedBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                    ApplicationPreferences.applicationUseAlarmClock(context)) {

                Calendar now = Calendar.getInstance();
                now.add(Calendar.MILLISECOND, lockDelay);
                long alarmTime = now.getTimeInMillis();

                if (PPApplication.logEnabled()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "alarmTime=" + result);
                }

                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                long alarmTime = SystemClock.elapsedRealtime() + lockDelay;

                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, delayTime, pendingIntent);
            }
        }
    }

}
