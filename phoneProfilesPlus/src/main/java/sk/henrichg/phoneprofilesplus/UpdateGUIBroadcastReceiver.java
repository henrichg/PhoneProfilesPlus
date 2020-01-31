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
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class UpdateGUIBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    static final String EXTRA_REFRESH = "refresh";
    //static final String EXTRA_FROM_ALARM = "from_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### UpdateGUIBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "UpdateGUIBroadcastReceiver.onReceive", "UpdateGUIBroadcastReceiver_onReceive");

        boolean refresh = intent.getBooleanExtra(EXTRA_REFRESH, true);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);
        //boolean fromAlarm = intent.getBooleanExtra(EXTRA_FROM_ALARM, false);

        doWork(true, context, refreshAlsoEditor, refresh/*, fromAlarm*/);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private static void setAlarm(boolean alsoEditor, /*boolean refresh,*/ Context context)
    {
        removeAlarm(context);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MILLISECOND, PPApplication.DURATION_FOR_GUI_REFRESH+100);
        long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

        if (ApplicationPreferences.applicationUseAlarmClock) {
            //Intent intent = new Intent(_context, UpdateGUIBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PPApplication.ACTION_UPDATE_GUI);
            //intent.setClass(context, UpdateGUIBroadcastReceiver.class);

            intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, false/*refresh*/);
            intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
            //intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_FROM_ALARM, true);

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
                    .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_UPDATE_GUI)
                    .putBoolean(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, false/*refresh*/)
                    .putBoolean(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor)
                    //.putBoolean(UpdateGUIBroadcastReceiver.EXTRA_FROM_ALARM, true)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                            .addTag("elapsedAlarmsUpdateGUIWork")
                            .setInputData(workData)
                            .setInitialDelay(PPApplication.DURATION_FOR_GUI_REFRESH+100, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context);
                //if (PPApplication.logEnabled()) {
                //    PPApplication.logE("[HANDLER] UpdateGUIBroadcastReceiver.setAlarm", "enqueueUniqueWork - refresh=" + refresh);
                //    PPApplication.logE("[HANDLER] UpdateGUIBroadcastReceiver.setAlarm", "enqueueUniqueWork - alsoEditor=" + alsoEditor);
                //}
                //workManager.enqueueUniqueWork("elapsedAlarmsUpdateGUIWork", ExistingWorkPolicy.REPLACE, worker);
                workManager.enqueue(worker);
            } catch (Exception ignored) {}
        }

        /*//Intent intent = new Intent(_context, UpdateGUIBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_UPDATE_GUI);
        //intent.setClass(context, UpdateGUIBroadcastReceiver.class);

        intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_FROM_ALARM, true);
        intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, refresh);
        intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);

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
                //Intent intent = new Intent(_context, UpdateGUIBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_UPDATE_GUI);
                //intent.setClass(context, UpdateGUIBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        PhoneProfilesService.cancelWork("elapsedAlarmsUpdateGUIWork", context.getApplicationContext());
        //PPApplication.logE("[HANDLER] UpdateGUIBroadcastReceiver.removeAlarm", "removed");
    }

    static void doWork(boolean useHandler, Context context, final boolean refresh, final boolean alsoEditor/*, final boolean fromAlarm*/) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (useHandler) {
            PPApplication.startHandlerThread("UpdateGUIBroadcastReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":UpdateGUIBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doWork(/*true,*/ appContext, refresh, alsoEditor/*, fromAlarm*/);

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
            _doWork(/*false,*/ appContext, refresh, alsoEditor/*, fromAlarm*/);
    }

    private static void _doWork(/*boolean useHandler,*/ Context context, final boolean refresh, final boolean alsoEditor/*, final boolean fromAlarm*/) {

        if (!refresh) {
            if (ActivateProfileHelper.lockRefresh || EditorProfilesActivity.doImport)
                // no refresh widgets
                return;
        }

        /*if (PPApplication.logEnabled()) {
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "ActivateProfileHelper.lockRefresh=" + ActivateProfileHelper.lockRefresh);
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "doImport=" + EditorProfilesActivity.doImport);
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "alsoEditor=" + refreshAlsoEditor);
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "refresh=" + refresh);
            PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "fromAlarm=" + fromAlarm);
        }*/

        long now = SystemClock.elapsedRealtime();

        if (refresh || (now - PPApplication.lastRefreshOfGUI) >= PPApplication.DURATION_FOR_GUI_REFRESH) {
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "refresh");
            ActivateProfileHelper.forceUpdateGUI(context, alsoEditor, refresh);
        } else {
            //PPApplication.logE("UpdateGUIBroadcastReceiver._doWork", "do not refresh");
            setAlarm(alsoEditor, /*refresh,*/ context);
        }

        PPApplication.lastRefreshOfGUI = SystemClock.elapsedRealtime();
    }

}
