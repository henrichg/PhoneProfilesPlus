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
import java.util.List;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_FOR_RESTART_EVENTS = "for_restart_events";

    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");

        if (intent != null) {
            final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            final boolean forRestartEvents = intent.getBooleanExtra(EXTRA_FOR_RESTART_EVENTS, false);
            final boolean manualRestart = intent.getBooleanExtra(PhoneProfilesService.EXTRA_MANUAL_RESTART, false);
            final int startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EVENT_MANUAL);
            doWork(true, context, profileId, forRestartEvents, manualRestart, startupSource);
        }
    }

    /** @noinspection SameParameterValue*/
    static void setAlarm(Profile profile, boolean forRestartEvents, boolean manualRestart, int startupSource, Context context)
    {
        removeAlarm(profile, context);

        if (profile == null)
            return;

        if ((profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) &&
                (profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) &&
                (profile._duration > 0)) {

            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            // save alarm for generator of profile name with duration
            ProfileStatic.setActivatedProfileEndDurationTime(context, profile._id, alarmTime);

            if (!PPApplicationStatic.isIgnoreBatteryOptimizationEnabled(context)) {

                if (ApplicationPreferences.applicationUseAlarmClock) {
                    //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                    //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                    intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                    intent.putExtra(PhoneProfilesService.EXTRA_MANUAL_RESTART, manualRestart);
                    intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                } else {
                    Data workData = new Data.Builder()
                            .putLong(PPApplication.EXTRA_PROFILE_ID, profile._id)
                            .putBoolean(EXTRA_FOR_RESTART_EVENTS, forRestartEvents)
                            .putBoolean(PhoneProfilesService.EXTRA_MANUAL_RESTART, manualRestart)
                            .putInt(PPApplication.EXTRA_STARTUP_SOURCE, startupSource)
                            .build();

//                    PPApplicationStatic.logE("[MAIN_WORKER_CALL] ProfileDurationAlarmBroadcastReceiver.setAlarm", "xxxxxxxxxxxxxxxxxxxx");

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
                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                                PPApplicationStatic.logE("[WORKER_CALL] ProfileDurationAlarmBroadcastReceiver.setAlarm", "keepResultsForAtLeast");
                                //workManager.enqueue(worker);
                                // REPLACE is OK, because at top is called removeAlarm()
                                workManager.enqueueUniqueWork(MainWorker.PROFILE_DURATION_WORK_TAG + "_" + (int) profile._id, ExistingWorkPolicy.REPLACE, worker);
                                PPApplication.elapsedAlarmsProfileDurationWork.add(MainWorker.PROFILE_DURATION_WORK_TAG + "_" + (int) profile._id);
                            }
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
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
                intent.putExtra(PhoneProfilesService.EXTRA_MANUAL_RESTART, manualRestart);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000L;

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    }
                    //this._isInDelay = true;
                }
            }
        }
        else
        if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) {

            Calendar now = Calendar.getInstance();

            Calendar configuredTime = Calendar.getInstance();
            configuredTime.set(Calendar.HOUR_OF_DAY, profile._endOfActivationTime / 60);
            configuredTime.set(Calendar.MINUTE, profile._endOfActivationTime % 60);
            configuredTime.set(Calendar.SECOND, 0);
            configuredTime.set(Calendar.MILLISECOND, 0);

            if (now.getTimeInMillis() < configuredTime.getTimeInMillis()) {
                // configured time is not expired
                // set alarm

                long alarmTime = configuredTime.getTimeInMillis();

                // save configured end of activation time for generator of profile name with duration
                ProfileStatic.setActivatedProfileEndDurationTime(context, profile._id, profile._endOfActivationTime);

                //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                intent.putExtra(PhoneProfilesService.EXTRA_MANUAL_RESTART, manualRestart);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000L;

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
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
                PPApplicationStatic.recordException(e);
            }

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                String workName = MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id;
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(workName);
                try {
                    List<WorkInfo> workInfoList = statuses.get();
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" workInfoList.size()="+workInfoList.size());
                    // cancel only enqueued works
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
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

    static void removeAlarm(Profile profile, Context context)
    {
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
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }

                    //this._isInDelay = false;
                    //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            PPApplicationStatic._cancelWork(MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id, false);
            // moved to cancelWork
            //PPApplication.elapsedAlarmsProfileDurationWork.remove(MainWorker.PROFILE_DURATION_WORK_TAG +"_"+(int) profile._id);
            ProfileStatic.setActivatedProfileEndDurationTime(context, profile._id, 0);
        }
    }

    static void doWork(boolean useHandler, Context context, final long profileId,
                       final boolean forRestartEvents, final boolean manualRestart, final int startupSource) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        if (useHandler) {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ProfileDurationAlarmBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_ProfileDurationAlarmBroadcastReceiver_doWork);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doWork(/*true,*/ appContext, profileId, forRestartEvents, manualRestart, startupSource);

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
            };
            PPApplicationStatic.createProfileActiationExecutorPool();
            PPApplication.profileActiationExecutorPool.submit(runnable);
        }
        else {
            _doWork(/*false,*/ appContext, profileId, forRestartEvents, manualRestart, startupSource);
        }
    }

    private static void _doWork(Context appContext, final long profileId,
                                final boolean forRestartEvents, final boolean manualRestart, int startupSource) {
        if (profileId != 0) {

//            Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "xxxx");

            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
            dataWrapper.fillProfileList(false, false);

            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
            if (profile != null) {
//                Allowed is duration also for events
//                if (DataWrapperStatic.getIsManualProfileActivation(true, appContext) ||
//                        (!EventStatic.getGlobalEventsRunning(appContext)) ||
//                        (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE)) {
//                    long _activatedProfileId = dataWrapper.getActivatedProfileId();

                    removeAlarm(profile, appContext);
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfileDurationAlarmBroadcastReceiver._doWork", "call of updateGUI");
                    PPApplication.updateGUI(true, false, appContext);

                    /* it doesn't matter what profile the alarm is for
                    if ((_activatedProfileId != -1) &&
                            (_activatedProfileId == profile._id) &&
                            (profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING)) {
                        // alarm is from activated profile
                    */
                    if (profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) {

                        if (!profile._durationNotificationSound.isEmpty() || profile._durationNotificationVibrate) {
                            PlayRingingNotification.playNotificationSound(
                                    profile._durationNotificationSound,
                                    profile._durationNotificationVibrate,
                                    false, appContext);
                            //PPApplication.sleep(500);
                        }

                        long activateProfileId = 0;
                        boolean doActivateProfile = false;

                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_DEFAULT_PROFILE) {
                            doActivateProfile = true;

                            activateProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE;
                            if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE;
                            PPApplicationStatic.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_UNDO_PROFILE) {
                            //if (DataWrapperStatic.getIsManualProfileActivation(true, appContext) ||
                            //   (!EventStatic.getGlobalEventsRunning(appContext))) {
                                //doActivateProfile = true;

                                //activateProfileId = ApplicationPreferences.prefActivatedProfileForDuration;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] ProfileDurationAlarmBroadcastReceiver._doWork", "PPApplication.profileActivationMutex");
                                /*
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
                                            String[] splits = fromFifo.split(StringConstants.STR_SPLIT_REGEX);
                                            activateProfileId = Long.parseLong(splits[0]);
                                        } else
                                            activateProfileId = 0;
                                    } else
                                        //eventTimeline._fkProfileEndActivated = 0;
                                        activateProfileId = 0;

                                    if (activateProfileId == _activatedProfileId)
                                        activateProfileId = 0;
                                }
                                */

//                                Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "UNDO start");
                                synchronized (PPApplication.profileActivationMutex) {
                                    List<String> activateProfilesFIFO = dataWrapper.fifoGetActivatedProfiles();
                                    int size = activateProfilesFIFO.size();
//                                    Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "size (1)="+size);
//                                    for (String item : activateProfilesFIFO) {
//                                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "item="+item);
//                                    }
                                    if (size > 0) {
                                        // get profile which will be undoed
                                        int index = size - 2;
                                        if (index >= 0) {
                                            String fromFifo = activateProfilesFIFO.get(index);
                                            String[] splits = fromFifo.split(StringConstants.STR_SPLIT_REGEX);
                                            activateProfileId = Long.parseLong(splits[0]);
                                        } else
                                            activateProfileId = 0;
//                                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "activateProfileId="+activateProfileId);
                                    } else
                                        activateProfileId = 0;
                                }

                                // undo only to profile which is not the same as profile for which alarm
                                // is executed, to avoid infinite loop
//                                Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "profileId="+profileId);
//                                Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "profile="+profile._name);
                                doActivateProfile = (profileId != activateProfileId);
//                                Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "doActivateProfile="+doActivateProfile);

                                if (doActivateProfile) {
                                    // remove from FIFO last activated profile
                                    synchronized (PPApplication.profileActivationMutex) {
                                        List<String> activateProfilesFIFO = dataWrapper.fifoGetActivatedProfiles();
                                        int size = activateProfilesFIFO.size();
//                                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "size (2)="+size);
                                        if (size > 0) {
                                            activateProfilesFIFO.remove(size - 1);
                                            dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                                        }
                                    }

                                    int logType = PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE;
                                    if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                        logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE;
                                    PPApplicationStatic.addActivityLog(appContext, logType,
                                            null,
                                            DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                            "");
                                }
                            //}
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE) {
                            doActivateProfile = true;

                            activateProfileId = profile._afterDurationProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE;
                            if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE;
                            PPApplicationStatic.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");
                        }

                        boolean canRestart =
                                (!forRestartEvents) ||
                                manualRestart ||
                                (startupSource == PPApplication.STARTUP_SOURCE_NOTIFICATION) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
                                //(startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_QUICK_TILE) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_EVENT_MANUAL) ||
                                (startupSource == PPApplication.STARTUP_SOURCE_EXTERNAL_APP);

//                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "startupSource="+startupSource);
//                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "manualRestart="+manualRestart);
//                        Log.e("ProfileDurationAlarmBroadcastReceiver._doWork", "canRestart="+canRestart);

                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_RESTART_EVENTS) {

                            if (canRestart) {
                                doActivateProfile = false;

                                int logType = PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS;
                                if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                    logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS;
                                PPApplicationStatic.addActivityLog(appContext, logType,
                                        null,
                                        DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                        "");

                                // manualRestart must be false to avoid infinite loop
                                dataWrapper.restartEventsWithDelay(/*false,*/ false, true, false, PPApplication.ALTYPE_UNDEFINED);
                            } else {
                                doActivateProfile = true;

                                activateProfileId = 0;
                            }
                        }

                        if (doActivateProfile) {
                            dataWrapper.activateProfileAfterDuration(activateProfileId, startupSource);
                        }

                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS) {
                            activateProfileId = profile._afterDurationProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            int logType = PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE;
                            if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE;
                            PPApplicationStatic.addActivityLog(appContext, logType,
                                    null,
                                    DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                    "");

                            dataWrapper.activateProfileAfterDuration(activateProfileId, startupSource);

                            if (canRestart) {
                                logType = PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS;
                                if (profile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)
                                    logType = PPApplication.ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS;
                                PPApplicationStatic.addActivityLog(appContext, logType,
                                        null,
                                        DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, false, "", false, false, false, dataWrapper),
                                        "");

                                // manualRestart must be false to avoid infinite loop
                                dataWrapper.restartEventsWithDelay(/*false,*/ false, true, false, PPApplication.ALTYPE_UNDEFINED);
                            }
                        }

                    }
//                }
            }

            //dataWrapper.invalidateDataWrapper();

        }
    }

}
