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

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class StartEventNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] StartEventNotificationBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "StartEventNotificationBroadcastReceiver.onReceive", "StartEventNotificationBroadcastReceiver_onReceive");

        if (intent != null) {
            final long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            doWork(true, context, event_id);
        }
    }

    static void removeAlarm(Event event, Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, StartEventNotificationBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                //intent.setClass(context, StartEventNotificationBroadcastReceiver.class);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("StartEventNotificationBroadcastReceiver.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        PPApplication.cancelWork(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG +"_"+(int)event._id, false);
        PPApplication.elapsedAlarmsStartEventNotificationWork.remove(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG +"_"+(int)event._id);
        //PPApplication.logE("[HANDLER] StartEventNotificationBroadcastReceiver.removeAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Event event, Context context)
    {
        //if (!_permanentRun) {

        if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, StartEventNotificationBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
                //intent.setClass(context, StartEventNotificationBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, event._repeatNotificationIntervalStart);
                    long alarmTime = now.getTimeInMillis();

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("StartEventNotificationBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
                Data workData = new Data.Builder()
                        .putLong(PPApplication.EXTRA_EVENT_ID, event._id)
                        .build();

                /*int keepResultsDelay = (event._repeatNotificationIntervalStart * 5) / 60; // conversion to minutes
                if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG + "_" + (int) event._id)
                                .setInputData(workData)
                                .setInitialDelay(event._repeatNotificationIntervalStart, TimeUnit.SECONDS)
                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_DAYS, TimeUnit.DAYS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("[HANDLER] StartEventNotificationBroadcastReceiver.setAlarm", "enqueueUniqueWork - event._repeatNotificationIntervalStart=" + event._repeatNotificationIntervalStart);
                                PPApplication.logE("[HANDLER] StartEventNotificationBroadcastReceiver.setAlarm", "enqueueUniqueWork - event._id=" + event._id);
                            }*/

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.START_EVENT_NOTIFICATION_TAG_WORK +"_"+(int)event._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] StartEventNotificationBroadcastReceiver.setAlarm", "for=" + MainWorker.START_EVENT_NOTIFICATION_TAG_WORK +"_"+(int)event._id + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                            PPApplication.logE("[WORKER_CALL] StartEventNotificationBroadcastReceiver.setAlarm", "(1)");
                            //workManager.enqueue(worker);
                            workManager.enqueueUniqueWork(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG + "_" + (int) event._id, ExistingWorkPolicy./*APPEND_OR_*/REPLACE, worker);
                            PPApplication.elapsedAlarmsStartEventNotificationWork.add(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG + "_" + (int) event._id);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
        else {

            //Intent intent = new Intent(_context, StartEventNotificationBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_START_EVENT_NOTIFICATION_BROADCAST_RECEIVER);
            //intent.setClass(context, StartEventNotificationBroadcastReceiver.class);

            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, event._repeatNotificationIntervalStart);
                    long alarmTime = now.getTimeInMillis();

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("StartEventNotificationBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {
                    long alarmTime = SystemClock.elapsedRealtime() + event._repeatNotificationIntervalStart * 1000L;

                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
            }
        }
        //}
    }

    static void doWork(boolean useHandler, Context context, final long event_id) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[HANDLER] StartEventNotificationBroadcastReceiver.doWork", "useHandler=" + useHandler);
            PPApplication.logE("[HANDLER] StartEventNotificationBroadcastReceiver.doWork", "event_id=" + event_id);
        }*/

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"StartEventNotificationBroadcastReceiver.doWork"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            __handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
                @Override
                public void run() {
                    if (event_id != 0) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=StartEventNotificationBroadcastReceiver.doWork");

                        Context appContext= appContextWeakRef.get();

                        if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartEventNotificationBroadcastReceiver_doWork");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                                Event event = databaseHandler.getEvent(event_id);
                                if (event != null)
                                    event.notifyEventStart(appContext, true);

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=StartEventNotificationBroadcastReceiver.doWork");
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
                        }
                    }
                }
            });
        }
        else {
            final Context appContext = context.getApplicationContext();
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
            Event event = databaseHandler.getEvent(event_id);
            if (event != null)
                event.notifyEventStart(appContext, true);
        }
    }

}
