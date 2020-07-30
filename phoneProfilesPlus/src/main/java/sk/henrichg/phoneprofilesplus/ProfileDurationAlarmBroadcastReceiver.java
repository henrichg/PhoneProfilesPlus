package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_FOR_RESTART_EVENTS = "for_restart_events";

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "ProfileDurationAlarmBroadcastReceiver.onReceive", "ProfileDurationAlarmBroadcastReceiver_onReceive");

        if (intent != null) {
            final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            final boolean forRestartEvents = intent.getBooleanExtra(EXTRA_FOR_RESTART_EVENTS, false);
            final int startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SERVICE_MANUAL);
            doWork(true, context, profileId, forRestartEvents, startupSource);
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, boolean forRestartEvents, int startupSource, Context context)
    {
        removeAlarm(profile, context);

        if (profile == null)
            return;

        if ((profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) &&
            (profile._duration > 0))
        {
            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            Profile.setActivatedProfileEndDurationTime(context, alarmTime);

            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
                //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            }
            else {
                Data workData = new Data.Builder()
                        .putLong(PPApplication.EXTRA_PROFILE_ID, profile._id)
                        .putBoolean(EXTRA_FOR_RESTART_EVENTS, forRestartEvents)
                        .putInt(PPApplication.EXTRA_STARTUP_SOURCE, startupSource)
                        .build();

                /*int keepResultsDelay = (profile._duration * 5) / 60; // conversion to minutes
                if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id)
                                .setInputData(workData)
                                .setInitialDelay(profile._duration, TimeUnit.SECONDS)
                                .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_DAYS, TimeUnit.DAYS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - profile._duration=" + profile._duration);
                                PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - profile._id=" + profile._id);
                                PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - forRestartEvents=" + forRestartEvents);
                                PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.setAlarm", "enqueueUniqueWork - startupSource=" + startupSource);
                            }*/

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] ProfileDurationAlarmBroadcastReceiver.setAlarm", "for=" + MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

                            //workManager.enqueue(worker);
                            workManager.enqueueUniqueWork(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int)profile._id, ExistingWorkPolicy.APPEND_OR_REPLACE, worker);
                            PPApplication.elapsedAlarmsProfileDurationWork.add(MainWorker.PROFILE_DURATION_TAG_WORK +"_" + (int) profile._id);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            /*//Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
            //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(EXTRA_FOR_RESTART_EVENTS, forRestartEvents);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) profile._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock(context)) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    alarmTime = SystemClock.elapsedRealtime() + profile._duration * 1000;

                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
                //this._isInDelay = true;
            }
             */
        }
        //else
        //	this._isInDelay = false;

        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);

    }

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

            PPApplication.cancelWork(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int) profile._id);
            PPApplication.elapsedAlarmsProfileDurationWork.remove(MainWorker.PROFILE_DURATION_TAG_WORK +"_"+(int) profile._id);
        }
        Profile.setActivatedProfileEndDurationTime(context, 0);
        //PPApplication.logE("[HANDLER] ProfileDurationAlarmBroadcastReceiver.removeAlarm", "removed");
    }

    static void doWork(boolean useHandler, Context context, final long profileId, final boolean forRestartEvents, final int startupSource) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (useHandler) {
            PPApplication.startHandlerThread(/*"ProfileDurationAlarmBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ProfileDurationAlarmBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=ProfileDurationAlarmBroadcastReceiver.doWork");

                        _doWork(/*true,*/ appContext, profileId, forRestartEvents, startupSource);

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
        else
            _doWork(/*false,*/ appContext, profileId, forRestartEvents, startupSource);
    }

    private static void _doWork(/*boolean useHandler,*/ Context appContext, final long profileId, final boolean forRestartEvents, int startupSource) {
        //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "profileId=" + profileId);

        if (profileId != 0) {
            //if (useHandler)
            //    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=ProfileDurationAlarmBroadcastReceiver._doWork");

            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

            //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "getIsManualProfileActivation()=" + DataWrapper.getIsManualProfileActivation(true, appContext));

            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
            if (profile != null) {
                if (DataWrapper.getIsManualProfileActivation(true/*, appContext*/) ||
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
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_DEFAULT_PROFILE) {
                            activateProfileId = ApplicationPreferences.applicationDefaultProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_AFTER_DURATION_DEFAULT_PROFILE, null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper),
                                    profile._icon, 0, "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_UNDO_PROFILE) {
                            activateProfileId = ApplicationPreferences.prefActivatedProfileForDuration;
                            if (activateProfileId == activatedProfile._id)
                                activateProfileId = 0;

                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_AFTER_DURATION_UNDO_PROFILE, null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper),
                                    profile._icon, 0, "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE) {
                            activateProfileId = profile._afterDurationProfile;
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE, null,
                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper),
                                    profile._icon, 0, "");
                        }
                        if (profile._afterDurationDo == Profile.AFTER_DURATION_DO_RESTART_EVENTS) {
                            if (!forRestartEvents) {
                                PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_AFTER_DURATION_RESTART_EVENTS, null,
                                        DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper),
                                        profile._icon, 0, "");

                                //PPApplication.logE("ProfileDurationAlarmBroadcastReceiver._doWork", "restart events");
                                dataWrapper.restartEventsWithDelay(3, false, true, /*false,*/ PPApplication.ALTYPE_UNDEFINED);
                            } else {
                                //PPApplication.logE("&&&&&&& ProfileDurationAlarmBroadcastReceiver._doWork", "(1) called is DataWrapper.activateProfileAfterDuration");
                                dataWrapper.activateProfileAfterDuration(0, startupSource);
                            }
                        } else {
                            //PPApplication.logE("&&&&&&& ProfileDurationAlarmBroadcastReceiver._doWork", "(2) called is DataWrapper.activateProfileAfterDuration");
                            dataWrapper.activateProfileAfterDuration(activateProfileId, startupSource);
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
