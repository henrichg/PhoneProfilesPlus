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

import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "ProfileDurationAlarmBroadcastReceiver.onReceive", "ProfileDurationAlarmBroadcastReceiver_onReceive");

        if (PPApplication.getApplicationStarted(context, true)) {
            //ProfileDurationJob.start(context.getApplicationContext(), intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0));

            if (intent != null) {
                final Context appContext = context.getApplicationContext();
                final long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                PPApplication.startHandlerThread("ProfileDurationAlarmBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "profileId="+profileId);

                        if (profileId != 0) {

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ProfileDurationAlarmBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                                if (PPApplication.logEnabled())
                                    PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "getIsManualProfileActivation()=" + dataWrapper.getIsManualProfileActivation(true));

                                if (dataWrapper.getIsManualProfileActivation(true)) {
                                    Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
                                    Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);

                                    if (PPApplication.logEnabled()) {
                                        if (activatedProfile != null)
                                            PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "activatedProfile._name" + activatedProfile._name);
                                    }

                                    if ((profile != null) && (activatedProfile != null) &&
                                            (activatedProfile._id == profile._id) &&
                                            (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
                                        // alarm is from activated profile

                                        PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "alarm is from activated profile");

                                        if (!profile._durationNotificationSound.isEmpty() || profile._durationNotificationVibrate) {
                                            if (PhoneProfilesService.getInstance() != null) {
                                                PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "play notification");
                                                PhoneProfilesService.getInstance().playNotificationSound(profile._durationNotificationSound, profile._durationNotificationVibrate);
                                                //PPApplication.sleep(500);
                                            }
                                        }

                                        long activateProfileId = 0;
                                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE) {
                                            activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(appContext));
                                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                                activateProfileId = 0;

                                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, null,
                                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, dataWrapper, false, appContext),
                                                    profile._icon, 0);
                                        }
                                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE) {
                                            activateProfileId = Profile.getActivatedProfileForDuration(appContext);

                                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, null,
                                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, dataWrapper, false, appContext),
                                                    profile._icon, 0);
                                        }
                                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_RESTARTEVENTS) {
                                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, null,
                                                    DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, dataWrapper, false, appContext),
                                                    profile._icon, 0);

                                            PPApplication.logE("ProfileDurationAlarmBroadcastReceiver.onReceive", "restart events");
                                            dataWrapper.restartEventsWithDelay(3, true, false, DatabaseHandler.ALTYPE_UNDEFINED);
                                        } else {
                                            dataWrapper.activateProfileAfterDuration(activateProfileId);
                                        }
                                    }
                                }


                                dataWrapper.invalidateDataWrapper();
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, Context context)
    {
        removeAlarm(context);

        if (profile == null)
            return;

        if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
            (profile._duration > 0))
        {
            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            Profile.setActivatedProfileEndDurationTime(context, alarmTime);

            //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
            //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                        ApplicationPreferences.applicationUseAlarmClock(context)) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
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
        }
        //else
        //	this._isInDelay = false;

        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);

    }

    static public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_PROFILE_DURATION_BROADCAST_RECEIVER);
            //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }

            //this._isInDelay = false;
            //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
        }
        Profile.setActivatedProfileEndDurationTime(context, 0);
    }

}
