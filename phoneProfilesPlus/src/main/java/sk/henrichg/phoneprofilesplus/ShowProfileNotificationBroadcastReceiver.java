package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class ShowProfileNotificationBroadcastReceiver extends BroadcastReceiver {

    //static final String EXTRA_FROM_ALARM = "from_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### ShowProfileNotificationBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "ShowProfileNotificationBroadcastReceiver.onReceive", "ShowProfileNotificationBroadcastReceiver_onReceive");

        //boolean fromAlarm = intent.getBooleanExtra(EXTRA_FROM_ALARM, false);

        doWork(true, context/*, fromAlarm*/);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MILLISECOND, PPApplication.DURATION_FOR_GUI_REFRESH+100);
        long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

        if (ApplicationPreferences.applicationUseAlarmClock) {
            //Intent intent = new Intent(_context, ShowProfileNotificationBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION);
            //intent.setClass(context, ShowProfileNotificationBroadcastReceiver.class);

            //intent.putExtra(ShowProfileNotificationBroadcastReceiver.EXTRA_FROM_ALARM, true);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                    .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_SHOW_PROFILE_NOTIFICATION)
                    //.putBoolean(ShowProfileNotificationBroadcastReceiver.EXTRA_FROM_ALARM, true)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                            .addTag("elapsedAlarmsShowProfileNotificationWork")
                            .setInputData(workData)
                            .setInitialDelay(PPApplication.DURATION_FOR_GUI_REFRESH+100, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance(context);
                //workManager.enqueueUniqueWork("elapsedAlarmsShowProfileNotificationWork", ExistingWorkPolicy.REPLACE, worker);
                workManager.enqueue(worker);
            } catch (Exception ignored) {}
        }

        /*//Intent intent = new Intent(_context, ShowProfileNotificationBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION);
        //intent.setClass(context, ShowProfileNotificationBroadcastReceiver.class);

        //intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_FROM_ALARM, true);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                alarmTime = SystemClock.elapsedRealtime() + PPApplication.DURATION_FOR_GUI_REFRESH+100;

                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            }
        }*/
    }

    private static void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, ShowProfileNotificationBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION);
                //intent.setClass(context, ShowProfileNotificationBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        PhoneProfilesService.cancelWork("elapsedAlarmsShowProfileNotificationWork", context.getApplicationContext());
        //PPApplication.logE("[HANDLER] UpdateGUIBroadcastReceiver.removeAlarm", "removed");
    }

    static void doWork(boolean useHandler, Context context/*, final boolean fromAlarm*/) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (useHandler) {
            PPApplication.startHandlerThreadProfileNotification();
            final Handler handler = new Handler(PPApplication.handlerThreadProfileNotification.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ShowProfileNotificationBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doWork(/*true,*/ appContext/*, fromAlarm*/);

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
            _doWork(/*false,*/ appContext/*, fromAlarm*/);
    }

    private static void _doWork(/*boolean useHandler,*/ Context context/*, final boolean fromAlarm*/) {
        if (PhoneProfilesService.getInstance() != null) {
            //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "xxx");
            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
            //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "_showProfileNotification()");
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance()._showProfileNotification(profile, true, dataWrapper, false  /*, cleared*/);
            //dataWrapper.invalidateDataWrapper();
        }
    }

}
