package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.app.Notification.DEFAULT_VIBRATE;

public class DonationBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] DonationBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "DonationBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (intent != null) {
            doWork(/*true,*/ context);
        }
    }

    static public void setAlarm(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("[DONATION] DonationBroadcastReceiver.setAlarm", "xxx");

        Calendar alarm = Calendar.getInstance();
        //if (DebugVersion.enabled) {
        //    alarm.add(Calendar.MINUTE, 1);
        //} else {
            // each day at 13:30
            if (PPApplication.applicationFullyStarted) {
                alarm.set(Calendar.HOUR_OF_DAY, 13);
                alarm.set(Calendar.MINUTE, 30);
                alarm.add(Calendar.DAY_OF_MONTH, 1);
                alarm.set(Calendar.SECOND, 0);
                alarm.set(Calendar.MILLISECOND, 0);
            }
            else {
                alarm.set(Calendar.HOUR_OF_DAY, 13);
                alarm.set(Calendar.MINUTE, 30);
                alarm.set(Calendar.SECOND, 0);
                alarm.set(Calendar.MILLISECOND, 0);
                if (alarm.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                    alarm.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            if (PPApplication.logEnabled()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarm.getTimeInMillis());
                PPApplication.logE("[DONATION] DonationBroadcastReceiver.setAlarm", "alarm=" + result);
            }
        //}

        long alarmTime = alarm.getTimeInMillis();

        //Intent intent = new Intent(_context, DonationBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_DONATION);
        //intent.setClass(context, DonationBroadcastReceiver.class);

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
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_DONATION_TAG_WORK);
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[DONATION] DonationBroadcastReceiver.doWork", "xxx");

        //if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"DonationBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DonationBroadcastReceiver.doWork");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DonationBroadcastReceiver_doWork");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    try {
                        _doWork(appContext);
                    } catch (Exception ignored) {}

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
            });
        /*}
        else {
            _doWork(appContext);
            setAlarm(appContext);
        }*/
    }

    private static void _doWork(Context appContext) {
        int daysAfterFirstStart = PPApplication.getDaysAfterFirstStart(appContext) + 1;
        int donationNotificationCount = PPApplication.getDonationNotificationCount(appContext);
        int daysForNextNotification = PPApplication.getDaysForNextDonationNotification(appContext);
        boolean donationDonated = PPApplication.getDonationDonated(appContext);

        if (DebugVersion.enabled) {
            donationDonated = false;
            /*if (donationNotificationCount == 5) {
                donationNotificationCount = 3;
                daysAfterFirstStart = 120;
                PPApplication.setDonationNotificationCount(context, donationNotificationCount);
                PPApplication.setDaysAfterFirstStart(context, daysAfterFirstStart);
            }*/
            //donationNotificationCount = 3;
            //daysAfterFirstStart = 1168;
            //PPApplication.setDonationNotificationCount(context, donationNotificationCount);
            //PPApplication.setDaysAfterFirstStart(context, daysAfterFirstStart);
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("DonationBroadcastReceiver._doWork", "daysAfterFirstStart=" + daysAfterFirstStart);
            PPApplication.logE("DonationBroadcastReceiver._doWork", "donationNotificationCount=" + donationNotificationCount);
            PPApplication.logE("DonationBroadcastReceiver._doWork", "daysForNextNotification=" + daysForNextNotification);
            PPApplication.logE("DonationBroadcastReceiver._doWork", "donationDonated=" + donationDonated);
        }*/

        boolean notify = false;
        if (donationNotificationCount == 3) {
            daysForNextNotification = daysAfterFirstStart + 90;
            PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
            //PPApplication.logE("DonationBroadcastReceiver._doWork", "daysForNextNotification=" + daysForNextNotification);

            if (daysAfterFirstStart > 7 + 14 + 21 + 42 + 30) {
                // notify old users after 114 days
                //PPApplication.logE("DonationBroadcastReceiver._doWork", "notify old users after 114 days");
                notify = true;

                daysForNextNotification = daysAfterFirstStart + 90;
                PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                //PPApplication.logE("DonationBroadcastReceiver._doWork", "daysForNextNotification=" + daysForNextNotification);
            } else {
                PPApplication.setDonationNotificationCount(appContext, donationNotificationCount + 1);
                //PPApplication.logE("DonationBroadcastReceiver._doWork", "do not notify when donationNotificationCount is 3");
            }
        } else {
            int daysForOneNotification;
            if (donationNotificationCount > 3) {
                notify = daysAfterFirstStart >= daysForNextNotification;
                if (notify) {
                    daysForNextNotification = daysAfterFirstStart + 90;
                    PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                    //PPApplication.logE("DonationBroadcastReceiver._doWork", "daysForNextNotification=" + daysForNextNotification);
                }
            } else {
                daysForOneNotification = 7;
                for (int i = 1; i <= donationNotificationCount; i++) {
                    daysForOneNotification = daysForOneNotification + 7 * (i + 1);
                }
                //PPApplication.logE("DonationBroadcastReceiver._doWork", "daysForOneNotification=" + daysForOneNotification);

                notify = (daysAfterFirstStart > 0) && (daysAfterFirstStart >= daysForOneNotification);

                if (notify &&
                        ((donationNotificationCount == 0) ||
                                (donationNotificationCount == 1))) {
                    PPApplication.setDonationNotificationCount(appContext, donationNotificationCount + 1);
                    //PPApplication.logE("DonationBroadcastReceiver._doWork", "do not notify when donationNotificationCount is 0, 1");
                    notify = false;
                }
            }
        }
        //PPApplication.logE("DonationBroadcastReceiver._doWork", "notify=" + notify);

        if (!donationDonated/* && (donationNotificationCount < MAX_DONATION_NOTIFICATION_COUNT)*/) {

            if (notify) {
                PPApplication.setDonationNotificationCount(appContext, donationNotificationCount + 1);

                // show notification about "Please donate me."
                PPApplication.createDonationNotificationChannel(appContext);

                NotificationCompat.Builder mBuilder;
                Intent _intent;
                _intent = new Intent(appContext, DonationPayPalActivity.class);

                String nTitle = appContext.getString(R.string.about_application_donate_button);
                String nText = appContext.getString(R.string.donation_description);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = appContext.getString(R.string.ppp_app_name);
                    nText = appContext.getString(R.string.about_application_donate_button) + ": " +
                            appContext.getString(R.string.donation_description);
                }
                mBuilder = new NotificationCompat.Builder(appContext, PPApplication.DONATION_CHANNEL)
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
                            PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_TAG,
                            PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_ID, notification);
                } catch (Exception e) {
                    //Log.e("DonationBroadcastReceiver._doWork", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                }
            }

        }
        /*else {
            PPApplication.setDonationNotificationCount(context, MAX_DONATION_NOTIFICATION_COUNT);
        }*/

        PPApplication.setDaysAfterFirstStart(appContext, daysAfterFirstStart);
    }

}
