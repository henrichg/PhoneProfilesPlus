package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class DonationBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_NOTIFY_DONATION_ALARM = "notify_donation_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] DonationBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] DonationBroadcastReceiver.onReceive", "xxx");

        if (intent != null) {
            doWork(/*true,*/ context);
        }
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar alarm = Calendar.getInstance();

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_NOTIFY_DONATION_ALARM, 0);

        long alarmTime;

        /*if (DebugVersion.enabled) {
            alarm.add(Calendar.MINUTE, 1);

            alarmTime = alarm.getTimeInMillis();
        } else*/
        {
            if ((lastAlarm == 0) || (lastAlarm <= alarm.getTimeInMillis())) {
                // saved alarm is less then actual time

                // each day at 13:30
                //if (PPApplication.applicationFullyStarted) {
                    alarm.set(Calendar.HOUR_OF_DAY, 13);
                    alarm.set(Calendar.MINUTE, 30);
                    alarm.add(Calendar.DAY_OF_MONTH, 1);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                /*} else {
                    alarm.set(Calendar.HOUR_OF_DAY, 13);
                    alarm.set(Calendar.MINUTE, 30);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                    if (alarm.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                        alarm.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }*/

                alarmTime = alarm.getTimeInMillis();

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putLong(PREF_NOTIFY_DONATION_ALARM, alarmTime);
                editor.apply();

            } else {
                alarmTime = lastAlarm;
            }
        }

        //Intent intent = new Intent(_context, DonationBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_DONATION);
        //intent.setClass(context, DonationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        }
    }

    static private void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, ProfileDurationAlarmBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_DONATION);
                //intent.setClass(context, ProfileDurationAlarmBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_DONATION_TAG_WORK);
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        //if (useHandler) {
        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DonationBroadcastReceiver.doWork");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DonationBroadcastReceiver_doWork);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    try {
                        _doWork(appContext);
                    } catch (Exception ignored) {
                    }

                    setAlarm(appContext);

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
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
        /*}
        else {
            _doWork(appContext);
            setAlarm(appContext);
        }*/
    }

    private static void _doWork(Context appContext) {
//        Log.e("DonationBroadcastReceiver._doWork", "xxx");

//        if (DebugVersion.enabled) {
//            PPApplicationStatic.setDaysAfterFirstStart(appContext, 0);
//            PPApplicationStatic.setDonationNotificationCount(appContext, 0);
//            PPApplicationStatic.setDaysForNextDonationNotification(appContext, 0);

//            PPApplicationStatic.setDaysAfterFirstStart(appContext, 90);
//            PPApplicationStatic.setDonationNotificationCount(appContext, 3);
//            PPApplicationStatic.setDaysForNextDonationNotification(appContext, 90);
//        }

        // incrementation of daysAfterFirstStart, is increased each day, because alarm is set to each day at 13:30
        int daysAfterFirstStart = PPApplicationStatic.getDaysAfterFirstStart(appContext) + 1;

        int donationNotificationCount = PPApplicationStatic.getDonationNotificationCount(appContext);
        int daysForNextNotification = PPApplicationStatic.getDaysForNextDonationNotification(appContext);
        boolean donationDonated = PPApplicationStatic.getDonationDonated(appContext);

        if (DebugVersion.enabled) {
            donationDonated = false;
        }

//        Log.e("DonationBroadcastReceiver._doWork", "daysAfterFirstStart="+daysAfterFirstStart);
//        Log.e("DonationBroadcastReceiver._doWork", "donationNotificationCount="+donationNotificationCount);
//        Log.e("DonationBroadcastReceiver._doWork", "daysForNextNotification="+daysForNextNotification);

        boolean notify;// = false;
        if (donationNotificationCount > 3) {
            notify = daysAfterFirstStart >= daysForNextNotification;
//            Log.e("DonationBroadcastReceiver._doWork", "(1) notify="+notify);
            if (notify) {
                daysForNextNotification = daysAfterFirstStart + 90;
//                Log.e("DonationBroadcastReceiver._doWork", "daysForNextNotification="+daysForNextNotification);
                PPApplicationStatic.setDaysForNextDonationNotification(appContext, daysForNextNotification);
            }
        } else {
            int daysForOneNotification = 9;
            for (int i = 1; i <= donationNotificationCount; i++) {
                daysForOneNotification = daysForOneNotification + 9 * (i + 1);
            }
//            Log.e("DonationBroadcastReceiver._doWork", "daysForOneNotification="+daysForOneNotification);

            notify = (daysAfterFirstStart > 0) && (daysAfterFirstStart >= daysForOneNotification);
//            Log.e("DonationBroadcastReceiver._doWork", "(2) notify="+notify);

            if (notify) {
                PPApplicationStatic.setDaysForNextDonationNotification(appContext, daysForOneNotification);

                if (donationNotificationCount == 3) {
                    // increase daysForNextNotification, without this will be displayed 2x notification
                    daysForNextNotification = daysAfterFirstStart + 90;
//                    Log.e("DonationBroadcastReceiver._doWork", "daysForNextNotification="+daysForNextNotification);
                    PPApplicationStatic.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                }
            }
        }

        if ((!donationDonated) && notify) {

            // increased domationNotificationCount
            PPApplicationStatic.setDonationNotificationCount(appContext, donationNotificationCount + 1);

            // show notification about "Please donate me."
            PPApplicationStatic.createDonationNotificationChannel(appContext, false);

            NotificationCompat.Builder mBuilder;
            Intent _intent;
            _intent = new Intent(appContext, DonationPayPalActivity.class);

            String nTitle = appContext.getString(R.string.about_application_donate_button);
            String nText = appContext.getString(R.string.donation_description);
            mBuilder = new NotificationCompat.Builder(appContext, PPApplication.DONATION_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(appContext, R.color.information_color))
                    .setSmallIcon(R.drawable.ic_ppp_notification/*ic_information_notify*/) // notification icon
                    .setLargeIcon(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_information_notification))
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                    .setAutoCancel(true); // clear notification after click

            PendingIntent pi = PendingIntent.getActivity(appContext, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            Notification notification = mBuilder.build();

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
            try {
                mNotificationManager.notify(
                        PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_TAG,
                        PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_ID, notification);
            } catch (SecurityException en) {
                PPApplicationStatic.logException("DonationBroadcastReceiver._doWork", Log.getStackTraceString(en));
            } catch (Exception e) {
                //Log.e("DonationBroadcastReceiver._doWork", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }

        // save incremented daysAfterFirstStart
        PPApplicationStatic.setDaysAfterFirstStart(appContext, daysAfterFirstStart);
    }

}
