package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class DonationBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DonationBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "DonationBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (PPApplication.getApplicationStarted(context, true)) {

            if (intent != null) {
                final Context appContext = context.getApplicationContext();
                /*PPApplication.startHandlerThread("DonationBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {*/

                        int daysAfterFirstStart = PPApplication.getDaysAfterFirstStart(appContext) + 1;
                        int donationNotificationCount = PPApplication.getDonationNotificationCount(appContext);
                        int daysForNextNotification = PPApplication.getDaysForNextDonationNotification(appContext);
                        boolean donationDonated = PPApplication.getDonationDonated(appContext);

                        if (BuildConfig.DEBUG) {
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

                        PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysAfterFirstStart=" + daysAfterFirstStart);
                        PPApplication.logE("DonationBroadcastReceiver.onReceive", "donationNotificationCount=" + donationNotificationCount);
                        PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysForNextNotification=" + daysForNextNotification);
                        PPApplication.logE("DonationBroadcastReceiver.onReceive", "donationDonated="+donationDonated);

                        boolean notify = false;
                        if (donationNotificationCount == 3) {
                            daysForNextNotification = daysAfterFirstStart + 90;
                            PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                            PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysForNextNotification=" + daysForNextNotification);

                            if (daysAfterFirstStart > 7+14+21+42+30) {
                                // notify old users after 114 days
                                PPApplication.logE("DonationBroadcastReceiver.onReceive", "notify old users after 114 days");
                                notify = true;

                                daysForNextNotification = daysAfterFirstStart + 90;
                                PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                                PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysForNextNotification=" + daysForNextNotification);
                            }
                            else {
                                PPApplication.setDonationNotificationCount(appContext, donationNotificationCount+1);
                                PPApplication.logE("DonationBroadcastReceiver.onReceive", "do not notify when donationNotificationCount is 3");
                            }
                        }
                        else {
                            int daysForOneNotification;
                            if (donationNotificationCount > 3) {
                                notify = daysAfterFirstStart >= daysForNextNotification;
                                if (notify) {
                                    daysForNextNotification = daysAfterFirstStart + 90;
                                    PPApplication.setDaysForNextDonationNotification(appContext, daysForNextNotification);
                                    PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysForNextNotification=" + daysForNextNotification);
                                }
                            } else {
                                daysForOneNotification = 7;
                                for (int i = 1; i <= donationNotificationCount; i++) {
                                    daysForOneNotification = daysForOneNotification + 7 * (i + 1);
                                }
                                PPApplication.logE("DonationBroadcastReceiver.onReceive", "daysForOneNotification=" + daysForOneNotification);

                                notify = (daysAfterFirstStart > 0) && (daysAfterFirstStart >= daysForOneNotification);

                                if (notify &&
                                        ((donationNotificationCount == 0) ||
                                         (donationNotificationCount == 1))) {
                                    PPApplication.setDonationNotificationCount(appContext, donationNotificationCount+1);
                                    PPApplication.logE("DonationBroadcastReceiver.onReceive", "do not notify when donationNotificationCount is 0, 1");
                                    notify = false;
                                }
                            }
                        }
                        PPApplication.logE("DonationBroadcastReceiver.onReceive", "notify=" + notify);

                        if (!donationDonated/* && (donationNotificationCount < MAX_DONATION_NOTIFICATION_COUNT)*/) {

                            if (notify) {
                                PPApplication.setDonationNotificationCount(appContext, donationNotificationCount+1);

                                // show notification about "Please donate me."
                                PPApplication.createDonationNotificationChannel(appContext);

                                NotificationCompat.Builder mBuilder;
                                Intent _intent = new Intent(appContext, DonationActivity.class);

                                String nTitle = appContext.getString(R.string.about_application_donate_button);
                                String nText = appContext.getString(R.string.donation_description);
                                if (android.os.Build.VERSION.SDK_INT < 24) {
                                    nTitle = appContext.getString(R.string.app_name);
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
                                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                                //if (android.os.Build.VERSION.SDK_INT >= 21) {
                                mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
                                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                                //}
                                NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                                if (mNotificationManager != null)
                                    mNotificationManager.notify(PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_ID, mBuilder.build());
                            }

                        }
                        /*else {
                            PPApplication.setDonationNotificationCount(context, MAX_DONATION_NOTIFICATION_COUNT);
                        }*/

                        PPApplication.setDaysAfterFirstStart(appContext, daysAfterFirstStart);

                /*    }
                });*/
            }

            setAlarm(context.getApplicationContext());
        }
    }

    static public void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar now = Calendar.getInstance();
        /*if (BuildConfig.DEBUG) {
            now.add(Calendar.MINUTE, 1);
        } else {*/
            // each day at 13:30
            now.set(Calendar.HOUR_OF_DAY, 13);
            now.set(Calendar.MINUTE, 30);
            now.add(Calendar.DAY_OF_MONTH, 1);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
        //}

        long alarmTime = now.getTimeInMillis();

        PPApplication.logE("DonationBroadcastReceiver.setAlarm",
                "alarmTime=" + DateFormat.getDateFormat(context).format(alarmTime) +
                     " " + DateFormat.getTimeFormat(context).format(alarmTime));

        //Intent intent = new Intent(_context, DonationBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_DONATION);
        //intent.setClass(context, DonationBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                ApplicationPreferences.applicationUseAlarmClock(context)) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        }
    }

    static private void removeAlarm(Context context)
    {
        PPApplication.logE("DonationBroadcastReceiver.removeAlarm", "xxx");

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
    }

}
