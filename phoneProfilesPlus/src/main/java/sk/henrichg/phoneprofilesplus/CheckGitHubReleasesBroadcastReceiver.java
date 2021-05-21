package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import static android.app.Notification.DEFAULT_VIBRATE;

public class CheckGitHubReleasesBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] CheckGitHubReleasesBroadcastReceiver.onReceive", "xxx");
//        CallsCounter.logCounter(context, "CheckGitHubReleasesBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (intent != null) {
            doWork(/*true,*/ context);
        }
    }

    static public void setAlarm(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("CheckGitHubReleasesBroadcastReceiver.setAlarm", "xxx");

        Calendar alarm = Calendar.getInstance();
        //if (DebugVersion.enabled) {
        //    alarm.add(Calendar.MINUTE, 1);
            /*alarm.set(Calendar.HOUR_OF_DAY, 13);
            alarm.set(Calendar.MINUTE, 0);
            //alarm.add(Calendar.DAY_OF_MONTH, 30);
            alarm.add(Calendar.DAY_OF_MONTH, 1);
            alarm.set(Calendar.SECOND, 0);
            alarm.set(Calendar.MILLISECOND, 0);

            if (PPApplication.logEnabled()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarm.getTimeInMillis());
                //PPApplication.logE("CheckGitHubReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
            }*/
        //} else {
            // each month at 13:00
            alarm.set(Calendar.HOUR_OF_DAY, 13);
            alarm.set(Calendar.MINUTE, 0);
            alarm.add(Calendar.DAY_OF_MONTH, 30);
            alarm.set(Calendar.SECOND, 0);
            alarm.set(Calendar.MILLISECOND, 0);

//            if (PPApplication.logEnabled()) {
//                @SuppressLint("SimpleDateFormat")
//                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                String result = sdf.format(alarm.getTimeInMillis());
//                PPApplication.logE("CheckGitHubReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
//            }
        //}

        long alarmTime = alarm.getTimeInMillis();

        //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
        //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

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
                //if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        }
    }

    static private void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
                //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_DONATION_TAG_WORK);
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("CheckGitHubReleasesBroadcastReceiver.doWork", "xxx");

        //if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"DonationBroadcastReceiver.onReceive"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            __handler.post(new PPApplication.PPHandlerThreadRunnable(
                    context.getApplicationContext()) {
                @Override
                public void run() {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=CheckGitHubReleasesBroadcastReceiver.doWork");

                    Context appContext= appContextWeakRef.get();
                    if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CheckGitHubReleasesBroadcastReceiver_doWork");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            try {
                                _doWork(appContext);
                            } catch (Exception ignored) {
                            }

                            setAlarm(appContext);

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
                    }
                }
            });
        /*}
        else {
            _doWork(appContext);
            setAlarm(appContext);
        }*/
    }

    private static void _doWork(Context appContext) {
        // show notification for check new release
        PPApplication.createNewReleaseNotificationChannel(appContext);

        NotificationCompat.Builder mBuilder;
        Intent _intent;
        _intent = new Intent(appContext, CheckGitHubReleasesActivity.class);

        String nTitle = appContext.getString(R.string.menu_check_github_releases);
        String nText = appContext.getString(R.string.check_github_releases_notification);
//        if (android.os.Build.VERSION.SDK_INT < 24) {
//            nTitle = appContext.getString(R.string.ppp_app_name);
//            nText = appContext.getString(R.string.menu_check_github_releases) + ": " +
//                    appContext.getString(R.string.check_github_releases_notification);
//        }
        mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                .setAutoCancel(true); // clear notification after click

        PendingIntent pi = PendingIntent.getActivity(appContext, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21) {
        mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        Notification notification = mBuilder.build();
        if (Build.VERSION.SDK_INT < 26) {
            notification.vibrate = null;
            notification.defaults &= ~DEFAULT_VIBRATE;
        }

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(
                    PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            //Log.e("CheckGitHubReleasesBroadcastReceiver._doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

}
