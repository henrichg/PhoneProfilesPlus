package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunApplicationWithDelayBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_RUN_APPLICATION_DATA = "run_application_data";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### RunApplicationWithDelayBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "RunApplicationWithDelayBroadcastReceiver.onReceive", "RunApplicationWithDelayBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent != null) {
            String runApplicationData = intent.getStringExtra(EXTRA_RUN_APPLICATION_DATA);

            Intent appIntent;
            PackageManager packageManager = context.getPackageManager();

            if (!ApplicationsCache.isShortcut(runApplicationData)) {
                String packageName = ApplicationsCache.getPackageName(runApplicationData);
                appIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (appIntent != null) {
                    appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(appIntent);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                long shortcutId = ApplicationsCache.getShortcutId(runApplicationData);
                if (shortcutId > 0) {
                    Shortcut shortcut = DatabaseHandler.getInstance(context).getShortcut(shortcutId);
                    if (shortcut != null) {
                        try {
                            appIntent = Intent.parseUri(shortcut._intent, 0);
                            if (appIntent != null) {
                                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    context.startActivity(appIntent);
                                } catch (Exception ignored) {
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    static void setDelayAlarm(Context context, int startApplicationDelay, String runApplicationData)
    {
        removeDelayAlarm(context);

        if (startApplicationDelay > 0)
        {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, startApplicationDelay);
            long alarmTime = now.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("RunApplicationWithDelayBroadcastReceiver.setDelayAlarm","startTime="+result);

            Intent intent = new Intent(context, RunApplicationWithDelayBroadcastReceiver.class);
            intent.putExtra(EXTRA_RUN_APPLICATION_DATA, runApplicationData);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        }
    }

    static void removeDelayAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, RunApplicationWithDelayBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

}
