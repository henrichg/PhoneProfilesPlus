package sk.henrichg.phoneprofilesplus;

import static android.app.Notification.DEFAULT_VIBRATE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class CheckRequiredExtenderReleasesBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_REQUIRED_EXTENDER_RELEASE_ALARM = "required_extender_release_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] CheckRequiredExtenderReleasesBroadcastReceiver.onReceive", "xxx");
//        CallsCounter.logCounter(context, "CheckRequiredExtenderReleasesBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (intent != null) {

            Context appContext = context.getApplicationContext();

            try {
                CheckRequiredExtenderReleasesBroadcastReceiver.doWork(appContext);
            } catch (Exception ignored) {
            }

            CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm(appContext);
        }
    }

    static public void setAlarm(Context context)
    {
        removeAlarm(context);

//        PPApplication.logE("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "xxx");

        Calendar alarm = Calendar.getInstance();
//        if (PPApplication.logEnabled()) {
//            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//            String result = sdf.format(alarm.getTimeInMillis());
//            Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "now=" + result);
//        }

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_REQUIRED_EXTENDER_RELEASE_ALARM, 0);
//        if (PPApplication.logEnabled()) {
//            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//            String result = sdf.format(lastAlarm);
//            Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "lastAlarm=" + result);
//        }

        long alarmTime;

        // TODO remove for release
        if (DebugVersion.enabled) {
            alarm.add(Calendar.MINUTE, 1);

//            if (PPApplication.logEnabled()) {
//                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                String result = sdf.format(alarm.getTimeInMillis());
//                Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
//            }

            alarmTime = alarm.getTimeInMillis();
        } else
        {
            if ((lastAlarm == 0) || (lastAlarm <= alarm.getTimeInMillis())) {
                // saved alarm is less then actual time

                // each day at 12:30
                //if (PPApplication.applicationFullyStarted) {
                    alarm.set(Calendar.HOUR_OF_DAY, 12);
                    alarm.set(Calendar.MINUTE, 20);
                    alarm.add(Calendar.DAY_OF_MONTH, 1);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                /*} else {
                    alarm.set(Calendar.HOUR_OF_DAY, 12);
                    alarm.set(Calendar.MINUTE, 20);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                    if (alarm.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                        alarm.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }*/

//                if (PPApplication.logEnabled()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                    String result = sdf.format(alarm.getTimeInMillis());
//                    Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
//                }

                alarmTime = alarm.getTimeInMillis();

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putLong(PREF_REQUIRED_EXTENDER_RELEASE_ALARM, alarmTime);
                editor.apply();
            } else {
                alarmTime = lastAlarm;

//                if (PPApplication.logEnabled()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                    String result = sdf.format(alarmTime);
//                    Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm", "alarm 2=" + result);
//                }
            }
        }

        //Intent intent = new Intent(_context, CheckRequiredExtenderReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES);
        //intent.setClass(context, CheckRequiredExtenderReleasesBroadcastReceiver.class);

        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                @SuppressLint("UnspecifiedImmutableFlag")
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
                //Intent intent = new Intent(_context, CheckRequiredExtenderReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES);
                //intent.setClass(context, CheckRequiredExtenderReleasesBroadcastReceiver.class);

                @SuppressLint("UnspecifiedImmutableFlag")
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

    /*
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                //noinspection StringConcatenationInsideStringBufferAppend
                sb.append(line + "n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    */

    static void doWork(final Context appContext) {
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(appContext);
//        PPApplication.logE("CheckRequiredExtenderReleasesBroadcastReceiver.showInfoNotification", "extenderVersion="+extenderVersion);
        if ((extenderVersion != 0) && (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)) {
            removeNotification(appContext);

            // show notification for check new release
            PPApplication.createNewReleaseNotificationChannel(appContext);

            NotificationCompat.Builder mBuilder;
            Intent _intent;
            _intent = new Intent(appContext, CheckRequiredExtenderReleasesActivity.class);
            _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            String nTitle;
            String nText;
            nTitle = appContext.getString(R.string.required_extender_release);
            nText = appContext.getString(R.string.required_extender_release_notification);

            mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL)
                    .setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor))
                    .setSmallIcon(R.drawable.ic_information_notify) // notification icon
                    .setContentTitle(nTitle) // title for notification
                    .setContentText(nText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                    .setAutoCancel(true); // clear notification after click

            @SuppressLint("UnspecifiedImmutableFlag")
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
                        PPApplication.CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG,
                        PPApplication.CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                //Log.e("CheckRequiredExtenderReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
