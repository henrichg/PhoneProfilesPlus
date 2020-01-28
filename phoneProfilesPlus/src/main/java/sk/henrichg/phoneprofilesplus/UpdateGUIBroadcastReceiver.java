package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class UpdateGUIBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    static final String EXTRA_REFRESH = "refresh";
    static final String EXTRA_FROM_ALARM = "from_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### UpdateGUIBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "UpdateGUIBroadcastReceiver.onReceive", "UpdateGUIBroadcastReceiver_onReceive");

        boolean refresh = intent.getBooleanExtra(EXTRA_REFRESH, true);

        if (!refresh) {
            if (ActivateProfileHelper.lockRefresh || EditorProfilesActivity.doImport)
                // no refresh widgets
                return;
        }

        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);

        if (PPApplication.logEnabled()) {
            boolean fromAlarm = intent.getBooleanExtra(EXTRA_FROM_ALARM, false);
            //PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "ActivateProfileHelper.lockRefresh=" + ActivateProfileHelper.lockRefresh);
            //PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "doImport=" + EditorProfilesActivity.doImport);
            //PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "alsoEditor=" + refreshAlsoEditor);
            //PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "refresh=" + refresh);
            PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "fromAlarm=" + fromAlarm);
        }

        long now = SystemClock.elapsedRealtime();

        if ((now - PPApplication.lastRefreshOfGUI) >= PPApplication.DURATION_FOR_GUI_REFRESH) {
            PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "refresh");

            // icon widget
            try {
                int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, IconWidgetProvider.class));
                IconWidgetProvider myWidget = new IconWidgetProvider();
                myWidget.refreshWidget = refresh;
                myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } catch (Exception ignored) {
            }

            // one row widget
            try {
                int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OneRowWidgetProvider.class));
                OneRowWidgetProvider myWidget = new OneRowWidgetProvider();
                myWidget.refreshWidget = refresh;
                myWidget.onUpdate(context, AppWidgetManager.getInstance(context), ids);
            } catch (Exception ignored) {
            }

            // list widget
            try {
                ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
                myWidget.updateWidgets(context, refresh);
            } catch (Exception ignored) {
            }

            // Samsung edge panel
            if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
                try {
                    SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                    myWidget.updateWidgets(context, refresh);
                } catch (Exception ignored) {
                }
            }

            // dash clock extension
            Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
            intent3.putExtra(DashClockBroadcastReceiver.EXTRA_REFRESH, refresh);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

            // activities
            Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
            intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, refresh);
            intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, refreshAlsoEditor);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);
        }
        else {
            PPApplication.logE("UpdateGUIBroadcastReceiver.onReceive", "do not refresh");

            setAlarm(refreshAlsoEditor, refresh, context.getApplicationContext());
        }

        PPApplication.lastRefreshOfGUI = SystemClock.elapsedRealtime();

    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(boolean alsoEditor, boolean refresh, Context context)
    {
        removeAlarm(context);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MILLISECOND, PPApplication.DURATION_FOR_GUI_REFRESH+100);
        long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

        /*if (ApplicationPreferences.applicationUseAlarmClock) {
            //Intent intent = new Intent(_context, UpdateGUIBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PPApplication.ACTION_UPDATE_GUI);
            //intent.setClass(context, UpdateGUIBroadcastReceiver.class);

            intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, refresh);
            intent.putExtra(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);

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
                    .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_PROFILE_DURATION)
                    .putBoolean(UpdateGUIBroadcastReceiver.EXTRA_REFRESH, refresh)
                    .putBoolean(UpdateGUIBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor)
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
        }*/

        //Intent intent = new Intent(_context, UpdateGUIBroadcastReceiver.class);
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
        }
    }

    static public void removeAlarm(Context context)
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
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsUpdateGUIWork");
            workManager.cancelAllWorkByTag("elapsedAlarmsUpdateGUIWork");
        } catch (Exception ignored) {}
        Profile.setActivatedProfileEndDurationTime(context, 0);
        //PPApplication.logE("[HANDLER] UpdateGUIBroadcastReceiver.removeAlarm", "removed");
    }

}
