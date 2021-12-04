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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_FOR_RESTART_EVENTS = "for_restart_events";

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "ProfileDurationAlarmBroadcastReceiver.onReceive", "ProfileDurationAlarmBroadcastReceiver_onReceive");

        if (intent != null) {
            final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            final boolean forRestartEvents = intent.getBooleanExtra(EXTRA_FOR_RESTART_EVENTS, false);
            final int startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EVENT_MANUAL);
            doWork(true, context, profileId, forRestartEvents, startupSource);
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, boolean forRestartEvents, int startupSource, Context context)
    {
        removeAlarm(profile, context);

        if (profile == null)
            return;

//        Log.e("sk.henrichg.phoneprofilesplus.ProfileDurationAlarmBroadcastReceiver.setAlarm", "xxx");

        if ((profile._endOfActivationType == 0) &&
                (profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) &&
                (profile._duration > 0)) {

            // duration for start is > 0
            // set alarm

//            Log.e("sk.henrichg.phoneprofilesplus.ProfileDurationAlarmBroadcastReceiver.setAlarm", "duration");

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            // save alarm for generator of profile name with duration
            Profile.setActivatedProfileEndDurationTime(context, alarmTime);

            if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {

                if (ApplicationPreferences.applicationUseAlarmClock) {
                    //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                    //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                } else {
                    Data workData = new Data.Builder()
                            .putLong(PPApplication.EXTRA_PROFILE_ID, profile._id)
                            .putBoolean(EXTRA_FOR_RESTART_EVENTS, forRestartEvents)
                            .putInt(PPApplication.EXTRA_STARTUP_SOURCE, startupSource)
                            .build();

                    //int keepResultsDelay = (profile._duration * 5) / 60; // conversion to minutes
                    //if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    //    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;
                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.PROFILE_DURATION_WORK_TAG + "_" + (int) profile._id)
                                    .setInputData(workData)
                                    .setInitialDelay(profile._duration, TimeUnit.SECONDS)
                                    .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_DAYS, TimeUnit.DAYS)
                                    .build();
                    try {
                        if (PPApplication.getApplicationStarted(true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {
                                //if (PPApplication.logEnabled()) {
                                //    PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - profile._duration=" + profile._duration);
                                //    PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - profile._id=" + profile._id);
                                //    PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - forRestartEvents=" + forRestartEvents);
                                //    PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - startupSource=" + startupSource);
                                //}

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] ProfileDurationAlarmBroadcastReceiver.setAlarm", "for=" + MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                                PPApplication.logE("[WORKER_CALL] ProfileDurationAlarmBroadcastReceiver.setAlarm", "xxx");
                                //workManager.enqueue(worker);
                                workManager.enqueueUniqueWork(MainWorker.PROFILE_DURATION_WORK_TAG + "_" + (int) profile._id, ExistingWorkPolicy.APPEND_OR_REPLACE, worker);
                                PPApplication.elapsedAlarmsProfileDurationWork.add(MainWorker.PROFILE_DURATION_WORK_TAG + "_" + (int) profile._id);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }
            else {
                //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000L;

                        //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    }
                    //this._isInDelay = true;
                }
            }
        }
        else
        if (profile._endOfActivationType == 1) {

//            Log.e("sk.henrichg.phoneprofilesplus.ProfileDurationAlarmBroadcastReceiver.setAlarm", "time (1)");

            Calendar now = Calendar.getInstance();

            Calendar configuredTime = Calendar.getInstance();
            configuredTime.set(Calendar.HOUR_OF_DAY, profile._endOfActivationTime / 60);
            configuredTime.set(Calendar.MINUTE, profile._endOfActivationTime % 60);
            configuredTime.set(Calendar.SECOND, 0);
            configuredTime.set(Calendar.MILLISECOND, 0);

            if (now.getTimeInMillis() < configuredTime.getTimeInMillis()) {
                // configured time is not expired
                // set alarm

//                Log.e("sk.henrichg.phoneprofilesplus.ProfileDurationAlarmBroadcastReceiver.setAlarm", "time (2)");

                long alarmTime = configuredTime.getTimeInMillis();

                // save configured end of activation time for generator of profile name with duration
                Profile.setActivatedProfileEndDurationTime(context, profile._endOfActivationTime);

                //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000L;

                        //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    }
                    //this._isInDelay = true;
                }
            }
        }
    }

    /*
    static boolean alarmIsRunning(Profile profile, Context context) {
        boolean isAlarmRunning = false;
        boolean isWorkRunning = false;
        if (profile != null) {
            try {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                    //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        isAlarmRunning = true;
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                String workName = MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id;
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(workName);
                //noinspection TryWithIdenticalCatches
                try {
                    List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" workInfoList.size()="+workInfoList.size());
                    // cancel only enqueued works
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        //noinspection IfStatementMissingBreakInLoop
                        if (state == WorkInfo.State.ENQUEUED) {
                            // any work is enqueued, cancel it
                            isWorkRunning = true;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        return isAlarmRunning || isWorkRunning;
    }
    */

    static public void removeAlarm(Profile profile, Context context)
    {
        if (profile != null) {
            try {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                    //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }

                    //this._isInDelay = false;
                    //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            PPApplication.cancelWork(MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id, false);
            PPApplication.elapsedAlarmsProfileDurationWork.remove(MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id);
        }
        Profile.setActivatedProfileEndDurationTime(context, 0);
        //PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.removeAlarm", "removed");
    }

    static void doWork(boolean useHandler, Context context, final long profileId, final boolean forRestartEvents, final int startupSource) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //noinspection IfStatementWithIdenticalBranches
        if (useHandler) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadBroadcast(/*"ProfileDurationAlarmBroadcastReceiver.onReceive"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfileDurationAlarmBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ProfileDurationAlarmBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doWork(/*true,*/ appContext, profileId, forRestartEvents, startupSource);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }
        else {
            final Context appContext = context.getApplicationContext();

            _doWork(/*false,*/ appContext, profileId, forRestartEvents, startupSource);
        }
    }

    private static void _doWork(/*boolean useHandler,*/ Context appContext, final long profileId, final boolean forRestartEvents, int startupSource) {
//        PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "profileId=" + profileId);

        if (profileId != 0) {
            //if (useHandler)
            //    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=ProfileDurationAlarmBroadcastReceiver._doWork");

            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

//            PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "getIsManualProfileActivation()=" + DataWrapper.getIsManualProfileActivation(true, appContext));

            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
            if (profile != null) {
                if (DataWrapper.getIsManualProfileActivation(true, appContext) ||
                        (!Event.getGlobalEventsRunning()) ||
                        (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE)) {
                    Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);

                    /*if (PPApplication.logEnabled()) {
                        if (activatedProfile != null)
                            PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "activatedProfile._name" + activatedProfile._name);
                    }*/

                    if ((activatedProfile != null) &&
                            (activatedProfile._id == profile._id) &&
                            (profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING)) {
                        // alarm is from activated profile

                        //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "alarm is from activated profile");

                        if (!profile._durationNotificationSound.isEmpty() || profile._durationNotificationVibrate) {
                            if (PhoneProfilesService.getInstance() != null) {
                                //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "play notification");
                                PhoneProfilesService.getInstance().playNotificationSound(profile._durationNotificationSound, profile._durationNotificationVibrate);
                                //PPApplication.sleep(500);
                            }
                        }

                        long activateProfileId = 0;
                        boolean doActivateProfile = false;

                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_DEFAULT_PROFILE) {
                            doActivateProfile = true;

//                            PPApplication.logE("[APP_START] ProfileDurationAlarmBroadcastReceiver._doWork", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);
                            activateProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE;
                            if (profile._endOfActivationType == 1)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE;
                            PPApplication.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_UNDO_PROFILE) {
                            doActivateProfile = true;

                            //activateProfileId = ApplicationPreferences.prefActivatedProfileForDuration;
//                            PPApplication.logE("[FIFO_TEST] ProfileDurationAlarmBroadcastReceiver._doWork", "#### remove last profile");
                            synchronized (PPApplication.profileActivationMutex) {
                                List<String> activateProfilesFIFO = dataWrapper.fifoGetActivatedProfiles();
                                int size = activateProfilesFIFO.size();
                                if (size > 0) {
                                    //eventTimeline._fkProfileEndActivated = activateProfilesFIFO.get(size - 1);
                                    activateProfilesFIFO.remove(size - 1);
                                    dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                                    size = activateProfilesFIFO.size();
                                    if (size > 0) {
                                        String fromFifo = activateProfilesFIFO.get(size - 1);
                                        String[] splits = fromFifo.split("\\|");
                                        activateProfileId = Long.parseLong(splits[0]);
                                    } else
                                        activateProfileId = 0;
                                } else
                                    //eventTimeline._fkProfileEndActivated = 0;
                                    activateProfileId = 0;

                                if (activateProfileId == activatedProfile._id)
                                    activateProfileId = 0;
                            }

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE;
                            if (profile._endOfActivationType == 1)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE;
                            PPApplication.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE) {
                            doActivateProfile = true;

                            activateProfileId = profile._afterDurationProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE;
                            if (profile._endOfActivationType == 1)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE;
                            PPApplication.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_RESTART_EVENTS) {
                            if (!forRestartEvents) {
                                doActivateProfile = false;

                                int logType = PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS;
                                if (profile._endOfActivationType == 1)
                                    logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS;
                                PPApplication.addActivityLog(appContext, logType,
                                        null,
                                        DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                        "");

                                //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "restart events");
                                dataWrapper.restartEventsWithDelay(3, false, true, true, PPApplication.ALTYPE_UNDEFINED);
                            } else {
                                doActivateProfile = true;

                                //PPApplication.logE("&&&&&&& ProfileDurationAlarmBroadcastReceiver._doWork", "(1) called is DataWrapper.activateProfileAfterDuration");
                                activateProfileId = 0;
                            }
                        }

                        if (doActivateProfile) {
                            //PPApplication.logE("&&&&&&& ProfileDurationAlarmBroadcastReceiver._doWork", "(2) called is DataWrapper.activateProfileAfterDuration");
                            dataWrapper.activateProfileAfterDuration(activateProfileId, startupSource);
                        }

                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS) {
//                            PPApplication.logE("&&&&&&& ProfileDurationAlarmBroadcastReceiver._doWork", "AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS");
                            activateProfileId = profile._afterDurationProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE;
                            if (profile._endOfActivationType == 1)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE;
                            PPApplication.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");

                            dataWrapper.activateProfileAfterDuration(activateProfileId, startupSource);

                            if (!forRestartEvents) {
                                logType = PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS;
                                if (profile._endOfActivationType == 1)
                                    logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS;
                                PPApplication.addActivityLog(appContext, logType,
                                        null,
                                        DataWrapper.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                        "");

//                                PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "restart events");
                                dataWrapper.restartEventsWithDelay(5, false, true, true, PPApplication.ALTYPE_UNDEFINED);
                            }
                        }

                    }
                }
            }

            //dataWrapper.invalidateDataWrapper();

            //if (useHandler)
            //    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ProfileDurationAlarmBroadcastReceiver._doWork");
        }
    }

}
