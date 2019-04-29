package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunApplicationWithDelayBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_RUN_APPLICATION_DATA = "run_application_data";

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

            if (Application.isShortcut(runApplicationData)) {
                long shortcutId = Application.getShortcutId(runApplicationData);
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
            else
            if (Application.isIntent(runApplicationData)) {
                long intentId = Application.getIntentId(runApplicationData);
                if (intentId > 0) {
                    PPIntent ppIntent = DatabaseHandler.getInstance(context).getIntent(intentId);
                    if (ppIntent != null) {
                        appIntent = ApplicationEditorIntentActivityX.createIntent(ppIntent);
                        if (appIntent != null) {
                            if (ppIntent._intentType == 0) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    context.startActivity(appIntent);
                                } catch (Exception ignored) {
                                }
                            }
                            else {
                                try {
                                    context.sendBroadcast(appIntent);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            } else {
                String packageName = Application.getPackageName(runApplicationData);
                appIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (appIntent != null) {
                    appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(appIntent);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    static void setDelayAlarm(Context context, int startApplicationDelay, String runApplicationData)
    {
        //removeDelayAlarm(context);

        if (startApplicationDelay > 0)
        {
            //Intent intent = new Intent(_context, RunApplicationWithDelayBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
            //intent.setClass(context, RunApplicationWithDelayBroadcastReceiver.class);

            intent.putExtra(EXTRA_RUN_APPLICATION_DATA, runApplicationData);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    PPApplication.requestCodeForAlarm.nextInt(), intent, 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                        ApplicationPreferences.applicationUseAlarmClock(context)) {

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, startApplicationDelay);
                    long alarmTime = now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("RunApplicationWithDelayBroadcastReceiver.setDelayAlarm", "startTime=" + result);
                    }

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    long alarmTime = SystemClock.elapsedRealtime() + startApplicationDelay * 1000;

                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                }
            }
        }
    }

    /*
    static private void removeDelayAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Context _context = context;
            if (PhoneProfilesService.getInstance() != null)
                _context = PhoneProfilesService.getInstance();

            //Intent intent = new Intent(_context, RunApplicationWithDelayBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_RUN_APPLICATION_DELAY_BROADCAST_RECEIVER);
            //intent.setClass(context, RunApplicationWithDelayBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
    */

}
