package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;

public class CheckLatestPPPPSReleasesBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_LATEST_PPPPS_RELEASE_ALARM = "latest_pppps_release_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] CheckLatestPPPPSReleasesBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] CheckLatestPPPPSReleasesBroadcastReceiver.onReceive", "xxx");

        if (intent != null) {
            Context appContext = context.getApplicationContext();

            try {
                CheckLatestPPPPSReleasesBroadcastReceiver.doWork(appContext);
            } catch (Exception ignored) {
            }

            CheckLatestPPPPSReleasesBroadcastReceiver.setAlarm(appContext);
        }
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar alarm = Calendar.getInstance();

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_LATEST_PPPPS_RELEASE_ALARM, 0);

        long alarmTime;

        //TODO remove for release
        /*if (DebugVersion.enabled) {
            alarm.add(Calendar.MINUTE, 1);

            alarmTime = alarm.getTimeInMillis();
        } else*/
        {
            if ((lastAlarm == 0) || (lastAlarm <= alarm.getTimeInMillis())) {
                // saved alarm is less then actual time

                // each day at 12:30
                //if (PPApplication.applicationFullyStarted) {
                    alarm.set(Calendar.HOUR_OF_DAY, 12);
                    alarm.set(Calendar.MINUTE, 25);
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

                alarmTime = alarm.getTimeInMillis();

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putLong(PREF_LATEST_PPPPS_RELEASE_ALARM, alarmTime);
                editor.apply();
            } else {
                alarmTime = lastAlarm;
            }
        }

        //Intent intent = new Intent(_context, CheckLatestPPPPSReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_LATEST_PPPPS_RELEASES);
        //intent.setClass(context, CheckLatestPPPPSReleasesBroadcastReceiver.class);

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
                //Intent intent = new Intent(_context, CheckLatestPPPPSReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_LATEST_PPPPS_RELEASES);
                //intent.setClass(context, CheckLatestPPPPSReleasesBroadcastReceiver.class);

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

    /*
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
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
        final int ppppsVersion = ActivateProfileHelper.isPPPPutSettingsInstalled(appContext);
        if (ppppsVersion != 0) {
            if (Build.VERSION.SDK_INT == 33) {
                // check IzzyOnDroid repo
                // because fo Android 13 is required to install apk from app stores
                // but for Android 14 is required to istall with adb command

                RequestQueue queueIzzyRepo = Volley.newRequestQueue(appContext);
                String izzyRepoURL = PPApplication.IZZY_PPPPS_LATEST_APK_RELEASE_URL_BEGIN;
                izzyRepoURL = izzyRepoURL + PPApplication.VERSION_CODE_PPPPS_LATEST + ".apk";
//                Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", "izzyRepoURL=" + izzyRepoURL);
                StringRequest stringRequestIzzyRepo = new StringRequest(Request.Method.GET,
                        izzyRepoURL,
                        response1 -> {
                            if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_REQUIRED) {
                                // latest exists in IzzyOnDroid, but is not installed
//                                Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", "latest NOT installed - xxxxxxxxxxxxxxxx");
                                try {
                                    showNotification(appContext);
                                } catch (Exception e) {
//                                    Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                                }
                            }
//                            else
//                            Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", "latest installed - xxxxxxxxxxxxxxxx");
                        },
                        error -> {
                            // latest not exists in IzzyOnDroid, is not possible to install it
                            //  in this situation do not show notification
                            /*if ((error.networkResponse != null) && (error.networkResponse.statusCode == 404)) {
//                                Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", "error.networkResponse.statusCode=" + error.networkResponse.statusCode);
//                                Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", "latest NOT installed - xxxxxxxxxxxxxxxx");
                                try {
                                    showNotification(appContext);
                                } catch (Exception e) {
//                                    Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                                }
                            }*/
                        });
                queueIzzyRepo.add(stringRequestIzzyRepo);

            } else {
                if (ppppsVersion < PPApplication.VERSION_CODE_PPPPS_REQUIRED)
                    // latest is not installed
                    showNotification(appContext);
            }
        }
    }

    private static void showNotification(Context appContext) {
        removeNotification(appContext);

        // show notification for check new release
        PPApplicationStatic.createNewReleaseNotificationChannel(appContext, false);

        NotificationCompat.Builder mBuilder;
        Intent _intent;
        _intent = new Intent(appContext, CheckLatestPPPPSReleasesActivity.class);
        _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String nTitle;
        String nText;
        nTitle = appContext.getString(R.string.latest_pppps_release);
        nText = appContext.getString(R.string.latest_pppps_release_notification);

        mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL)
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

        mBuilder.setGroup(PPApplication.CHECK_RELEASES_GROUP);

        Notification notification = mBuilder.build();

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(
                    PPApplication.CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_ID, notification);
        } catch (SecurityException en) {
            PPApplicationStatic.logException("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("CheckLatestPPPPSReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
