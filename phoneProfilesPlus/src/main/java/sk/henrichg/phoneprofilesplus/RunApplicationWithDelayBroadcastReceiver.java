package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RunApplicationWithDelayBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_RUN_APPLICATION_DATA = "run_application_data";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] RunApplicationWithDelayBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] RunApplicationWithDelayBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent != null) {
            final String profileName = intent.getStringExtra(PPApplication.EXTRA_PROFILE_NAME);
            final String runApplicationData = intent.getStringExtra(EXTRA_RUN_APPLICATION_DATA);

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"RunApplicationWithDelayBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=RunApplicationWithDelayBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_RunApplicationWithDelayBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        Log.e("RunApplicationWithDelayBroadcastReceiver.onReceive", "call of RunApplicationWithDelayBroadcastReceiver.doWork");
                        doWork(appContext, profileName, runApplicationData);

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createProfileActiationExecutorPool();
            PPApplication.profileActiationExecutorPool.submit(runnable);
        }
    }

    private static int hashData(String runApplicationData) {
        int sLength = runApplicationData.length();
        int sum = 0;
        for(int i = 0 ; i < sLength-1 ; i++){
            sum += runApplicationData.charAt(i)<<(5*i);
        }
        return sum;
    }

    static void setDelayAlarm(Context context, int startApplicationDelay, String profileName, String runApplicationData)
    {
        removeDelayAlarm(context, runApplicationData);

        if (startApplicationDelay > 0)
        {
            int requestCode = hashData(runApplicationData); //PPApplication.requestCodeForAlarm.nextInt();

            if (!PPApplicationStatic.isIgnoreBatteryOptimizationEnabled(context)) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    //Intent intent = new Intent(_context, RunApplicationWithDelayBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                    //intent.setClass(context, RunApplicationWithDelayBroadcastReceiver.class);

                    intent.putExtra(PPApplication.EXTRA_PROFILE_NAME, profileName);
                    intent.putExtra(EXTRA_RUN_APPLICATION_DATA, runApplicationData);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Calendar now = Calendar.getInstance();
                        now.add(Calendar.SECOND, startApplicationDelay);
                        long alarmTime = now.getTimeInMillis();

                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                } else {
                    Data workData = new Data.Builder()
                            .putString(PPApplication.EXTRA_PROFILE_NAME, profileName)
                            .putString(EXTRA_RUN_APPLICATION_DATA, runApplicationData)
                            .build();

                /*int keepResultsDelay = (startApplicationDelay * 5) / 60; // conversion to minutes
                if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG + "_" + requestCode)
                                    .setInputData(workData)
                                    .setInitialDelay(startApplicationDelay, TimeUnit.SECONDS)
                                    .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_DAYS, TimeUnit.DAYS)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.RUN_APPLICATION_WITH_DELAY_TAG_WORK +"_"+requestCode);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                                PPApplicationStatic.logE("[WORKER_CALL] RunApplicationWithDelayBroadcastReceiver.setDelayAlarm", "xxx");
                                //workManager.enqueue(worker);
                                // REPLACE is OK, because at top is called removeDelayAlarm()
                                workManager.enqueueUniqueWork(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG + "_" + requestCode, ExistingWorkPolicy.REPLACE, worker);
                                PPApplication.elapsedAlarmsRunApplicationWithDelayWork.add(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG + "_" + requestCode);
                            }
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
            else {

                //Intent intent = new Intent(_context, RunApplicationWithDelayBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                //intent.setClass(context, RunApplicationWithDelayBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_PROFILE_NAME, profileName);
                intent.putExtra(EXTRA_RUN_APPLICATION_DATA, runApplicationData);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Calendar now = Calendar.getInstance();
                        now.add(Calendar.SECOND, startApplicationDelay);
                        long alarmTime = now.getTimeInMillis();

                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        long alarmTime = SystemClock.elapsedRealtime() + startApplicationDelay * 1000L;

                        //if (android.os.Build.VERSION.SDK_INT >= 23)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    }
                }
            }
        }
    }

    static void removeDelayAlarm(Context context, String runApplicationData)
    {
        int requestCode = hashData(runApplicationData);

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Context _context = context;
                //if (PhoneProfilesService.getInstance() != null)
                //    _context = PhoneProfilesService.getInstance();

                //Intent intent = new Intent(_context, RunApplicationWithDelayBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
                //intent.setClass(context, RunApplicationWithDelayBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context/*_context*/, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        PPApplicationStatic._cancelWork(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG +"_"+requestCode, false);
        // moved to cancelWork
        //PPApplication.elapsedAlarmsRunApplicationWithDelayWork.remove(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG +"_"+requestCode);

    }

    static void doWork(Context context, String profileName, String runApplicationData) {
        //final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

//        Log.e("RunApplicationWithDelayBroadcastReceiver.doWork", "call of ActivateProfileHelper.doExecuteForRunApplications");
        ActivateProfileHelper.doExecuteForRunApplications(context, profileName, runApplicationData);
    }

}
