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
import android.view.WindowManager;

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
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PostDelayedBroadcastReceiver_onReceive_ACTION_REMOVE_BRIGHTNESS_VIEW");
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

        long delayTime = SystemClock.elapsedRealtime() + lockDelay;

        //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
        //intent.setClass(context, PostDelayedBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, delayTime, pendingIntent);
            else //if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, delayTime, pendingIntent);
            //else
            //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, delayTime, pendingIntent);
        }
    }

}
