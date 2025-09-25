package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class LockDeviceActivityFinishBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] LockDeviceActivityFinishBroadcastReceiver.onReceive", "xxx");
        doWork(context);
    }

    static void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, LockDeviceActivityFinishBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                //intent.setClass(context, LockDeviceActivityFinishBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        PPApplicationStatic._cancelWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_WORK_TAG, false);
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        //final Context appContext = context.getApplicationContext();

        int delay = 20; // 20 seconds

        if (!PPApplicationStatic.isIgnoreBatteryOptimizationEnabled(context)) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, LockDeviceActivityFinishBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
                //intent.setClass(context, LockDeviceActivityFinishBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
//                PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "schedule");

                final Context appContext = context.getApplicationContext();
                //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                Runnable runnable = () -> {
                    // wkelock netreba, je to volane z onCreate aktivity

//                    long start = System.currentTimeMillis();
//                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "--------------- START");

                        LockDeviceActivityFinishBroadcastReceiver.doWork(appContext);

//                        long finish = System.currentTimeMillis();
//                        long timeElapsed = finish - start;
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "--------------- END - timeElapsed="+timeElapsed);
                        //worker.shutdown();
                };
                PPApplicationStatic.createDelayedProfileActivationExecutor();
                PPApplication.delayedProfileActivationExecutor.schedule(runnable, delay, TimeUnit.SECONDS);

                /*
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .build();
                try {
                    if (PPApplicationStatic.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                        //if (PPApplicationStatic.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                            PPApplicationStatic.logE("[WORKER_CALL] LockDeviceActivityFinishBroadcastReceiver.setAlarm", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                */
            }
        }
        else {
            //Intent intent = new Intent(_context, LockDeviceActivityFinishBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_LOCK_DEVICE_ACTIVITY_FINISH_BROADCAST_RECEIVER);
            //intent.setClass(context, LockDeviceActivityFinishBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {
//                    PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "schedule");

                    final Context appContext = context.getApplicationContext();
                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
                        // wkelock netreba, je to volane z onCreate aktivity

//                        long start = System.currentTimeMillis();
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "--------------- START");

                        LockDeviceActivityFinishBroadcastReceiver.doWork(appContext);

//                        long finish = System.currentTimeMillis();
//                        long timeElapsed = finish - start;
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** LockDeviceActivityFinishBroadcastReceiver.setAlarm", "--------------- END - timeElapsed="+timeElapsed);
                        //worker.shutdown();
                    };
                    PPApplicationStatic.createDelayedProfileActivationExecutor();
                    PPApplication.delayedProfileActivationExecutor.schedule(runnable, delay, TimeUnit.SECONDS);

                    /*
                    long alarmTime = SystemClock.elapsedRealtime() + delay * 1000;

                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    */
                }
            }
        }
    }

    static void doWork(Context context) {
        //if (PhoneProfilesService.getInstance() != null) {
            //if (PPApplication.lockDeviceActivity != null) {
            if (PPApplication.lockDeviceActivityDisplayed) {
//                PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] LockDeviceActivityFinishBroadcastReceiver.doWork", "xxx");
                Intent finishIntent = new Intent(LockDeviceActivity.ACTION_FINISH_LOCK_DEVICE_ACTIVITY_BROADCAST_RECEIVER);
                LocalBroadcastManager.getInstance(context).sendBroadcast(finishIntent);
            }
        //}
    }

}
