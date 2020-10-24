package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LockDeviceActivityFinishBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] LockDeviceActivityFinishBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "LockDeviceActivityFinishBroadcastReceiver.onReceive", "LockDeviceActivityFinishBroadcastReceiver_onReceive");
        doWork();
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
                    //PPApplication.logE("LockDeviceActivityFinishBroadcastReceiver.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        PPApplication.cancelWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, false);
        //PPApplication.logE("[HANDLER] LockDeviceActivityFinishBroadcastReceiver.removeAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        removeAlarm(context);

        int delay = 20; // 20 seconds

        if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {
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

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("[HANDLER] LockDeviceActivityFinishBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
            /*int keepResultsDelay = (delay * 5) / 60; // conversion to minutes
            //noinspection ConstantConditions
            if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] LockDeviceActivityFinishBroadcastReceiver.setAlarm", "for=" + MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                            PPApplication.logE("[WORKER_CALL] LockDeviceActivityFinishBroadcastReceiver.setAlarm", "xxx");
                            //PPApplication.logE("[HANDLER] LockDeviceActivityFinishBroadcastReceiver.setAlarm", "enqueueUniqueWork - alarmTime=" + delay);
                            workManager.enqueueUniqueWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
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

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("LockDeviceActivityFinishBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {
                    long alarmTime = SystemClock.elapsedRealtime() + delay * 1000;

                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
            }
        }
    }

    static void doWork() {
        //PPApplication.logE("[HANDLER] LockDeviceActivityFinishBroadcastReceiver.doWork", "xxx");
        //if (PhoneProfilesService.getInstance() != null) {
            if (PPApplication.lockDeviceActivity != null) {
                PPApplication.lockDeviceActivity.finish();
            }
        //}
    }

}
