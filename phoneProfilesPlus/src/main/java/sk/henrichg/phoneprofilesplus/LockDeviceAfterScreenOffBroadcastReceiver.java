package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LockDeviceAfterScreenOffBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF = PPApplication.PACKAGE_NAME + ".LockDeviceAfterScreenOffBroadcastReceiver.ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent != null)
//            PPApplication.logE("[IN_BROADCAST] LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
//        else
//            PPApplication.logE("[IN_BROADCAST] LockDeviceAfterScreenOffBroadcastReceiver.onReceive", "xxx");

//        if (PPApplication.logEnabled()) {
//            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//            if (keyguardManager != null) {
//                boolean keyguardShowing = keyguardManager.isKeyguardLocked();
//            }
//        }

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF)) {
                doWork(true, context);
            }
        }
    }

    static void setAlarm(int lockDelay, Context context)
    {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
                //intent.setClass(context, PostDelayedBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.MILLISECOND, lockDelay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
//                PPApplication.logE("[EXECUTOR_CALL]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "schedule");

                //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                Runnable runnable = () -> {
//                    long start = System.currentTimeMillis();
//                    PPApplication.logE("[IN_EXECUTOR]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "--------------- START");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LockDeviceAfterScreenOffBroadcastReceiver_executor_1");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);

//                        long finish = System.currentTimeMillis();
//                        long timeElapsed = finish - start;
//                        PPApplication.logE("[IN_EXECUTOR]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "--------------- END - timeElapsed="+timeElapsed);
                    } catch (Exception e) {
//                                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                        //worker.shutdown();
                    }
                };
                PPApplication.createDelayedProfileActivationExecutor();
                PPApplication.delayedProfileActivationExecutor.schedule(runnable, lockDelay, TimeUnit.MILLISECONDS);

                /*
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK)
                                .setInitialDelay(lockDelay, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                            PPApplication.logE("[WORKER_CALL] LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                */
            }
        }
        else {
            //final Context appContext = context.getApplicationContext();

            //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(ACTION_LOCK_DEVICE_AFTER_SCREEN_OFF);
            //intent.setClass(context, PostDelayedBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.MILLISECOND, lockDelay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {

//                    PPApplication.logE("[EXECUTOR_CALL]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "schedule");

                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
//                        long start = System.currentTimeMillis();
//                        PPApplication.logE("[IN_EXECUTOR]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "--------------- START");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_restartEventsWithDelay_2");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);

//                            long finish = System.currentTimeMillis();
//                            long timeElapsed = finish - start;
//                            PPApplication.logE("[IN_EXECUTOR]  ***** LockDeviceAfterScreenOffBroadcastReceiver.setAlarm", "--------------- END - timeElapsed="+timeElapsed);
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            //worker.shutdown();
                        }
                    };
                    PPApplication.createDelayedProfileActivationExecutor();
                    PPApplication.delayedProfileActivationExecutor.schedule(runnable, lockDelay, TimeUnit.MILLISECONDS);

                    /*
                    long alarmTime = SystemClock.elapsedRealtime() + lockDelay;

                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, delayTime, pendingIntent);
                    */
                }
            }
        }
    }

    static void doWork(boolean useHandler, Context context) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            final Context appContext = context.getApplicationContext();
            if (useHandler) {
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_SCREEN, "SENSOR_TYPE_SCREEN", 0);
                /*
                PPApplication.startHandlerThreadBroadcast();
                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LockDeviceAfterScreenOffBroadcastReceiver.doWork (1)");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LockDeviceAfterScreenOffBroadcastReceiver_doWork");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] LockDeviceAfterScreenOffBroadcastReceiver", "sensorType=SENSOR_TYPE_SCREEN (1)");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN);

                        } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                });
                */
            } else {
                //                PPApplication.logE("[EVENTS_HANDLER_CALL] LockDeviceAfterScreenOffBroadcastReceiver", "sensorType=SENSOR_TYPE_SCREEN (2)");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN);
            }
        }
    }

}
