package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;

public class CheckCriticalPPPReleasesBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION = "show_critical_github_release_code_notification";
    private static final String PREF_CRITICAL_PPP_RELEASE_ALARM = "critical_github_release_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] CheckCriticalPPPReleasesBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] CheckCriticalPPPReleasesBroadcastReceiver.onReceive", "xxx");

        if (intent != null) {

            Context appContext = context.getApplicationContext();

            try {
                CheckCriticalPPPReleasesBroadcastReceiver.doWork(appContext);
            } catch (Exception ignored) {
            }

            CheckCriticalPPPReleasesBroadcastReceiver.setAlarm(appContext);
        }
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar alarm = Calendar.getInstance();

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_CRITICAL_PPP_RELEASE_ALARM, 0);
//        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
//        String time = sdf.format(lastAlarm);
//        PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.lastAlarm", "lastAlarm="+time);
//        Log.e("CheckCriticalPPPReleasesBroadcastReceiver.lastAlarm", "lastAlarm="+time);

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
                    alarm.set(Calendar.MINUTE, 30);
                    alarm.add(Calendar.DAY_OF_MONTH, 1);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                /*} else {
                    alarm.set(Calendar.HOUR_OF_DAY, 12);
                    alarm.set(Calendar.MINUTE, 30);
                    alarm.set(Calendar.SECOND, 0);
                    alarm.set(Calendar.MILLISECOND, 0);
                    if (alarm.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                        alarm.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }*/

                alarmTime = alarm.getTimeInMillis();
//                sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
//                time = sdf.format(alarmTime);
//                PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.setAlarm", "alarmTime="+time);

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putLong(PREF_CRITICAL_PPP_RELEASE_ALARM, alarmTime);
                editor.apply();
            } else {
                alarmTime = lastAlarm;
            }
        }

        //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
        //intent.setClass(context, CheckCriticalPPPReleasesBroadcastReceiver.class);

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
                //Intent intent = new Intent(_context, CheckCriticalPPPReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
                //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

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
        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(appContext);
            String url;
            if (DebugVersion.enabled)
                url = PPApplication.PPP_RELEASES_MD_DEBUG_URL;
            else
                url = PPApplication.PPP_RELEASES_MD_URL;
//            PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "url="+url);
//            Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "url="+url);
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                    url,
                    response -> {

                        //String contents = response;

                        boolean forceDoData = false;

                        //TODO remove for release
                        //if (DebugVersion.enabled)
                        //    forceDoData = true;

                        final PPPReleaseData pppReleaseData =
                                PPApplicationStatic.getReleaseData(response, forceDoData, appContext);
//                        Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "pppReleaseData="+pppReleaseData);

                        if (pppReleaseData != null) {
                            // istalled PPP version is less then version in releases.md file
                            if (Build.VERSION.SDK_INT >= 33) {
                                // check IzzyOnDroid repo
                                // because from Android 13 is required to install apk from app stores

                                RequestQueue queueIzzyRepo = Volley.newRequestQueue(appContext);
                                String izzyRepoURL = PPApplication.IZZY_PPP_LATEST_APK_RELEASE_URL_BEGIN;
                                izzyRepoURL = izzyRepoURL + pppReleaseData.versionCodeInReleases + ".apk";
//                                PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "izzyRepoURL=" + izzyRepoURL);
//                                Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "izzyRepoURL=" + izzyRepoURL);
                                StringRequest stringRequestIzzyRepo = new StringRequest(Request.Method.GET,
                                        izzyRepoURL,
                                        response1 -> {
                                            // version in releases.md file exists in IzzyOnDroid, but is not installed
                                            // (installed PPP versionCode < pppReleaseData.versionCodeInReleases => pppReleaseData != null)
//                                            PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "IzzyOnDroid version not installed - xxxxxxxxxxxxxxxx");
//                                            Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "IzzyOnDroid version not installed - xxxxxxxxxxxxxxxx");
                                            try {
                                                boolean critical = pppReleaseData.critical;
                                                String versionNameInReleases = pppReleaseData.versionNameInReleases;
                                                int versionCodeInReleases = pppReleaseData.versionCodeInReleases;

                                                showNotification(appContext,
                                                        versionNameInReleases,
                                                        versionCodeInReleases,
                                                        critical);

                                            } catch (Exception e) {
//                                                Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                                            }
                                        },
                                        error -> {
                                            // version in releases.md file not exists in IzzyOnDroid, is not possible to install it
                                            //  in this situation do not show notification
                                            /*
                                            if ((error.networkResponse != null) && (error.networkResponse.statusCode == 404)) {
//                                                PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "latest NOT installed - xxxxxxxxxxxxxxxx");
                                                try {
                                                    boolean critical = pppReleaseData.critical;
                                                    String versionNameInReleases = pppReleaseData.versionNameInReleases;
                                                    int versionCodeInReleases = pppReleaseData.versionCodeInReleases;

                                                    showNotification(appContext,
                                                            versionNameInReleases,
                                                            versionCodeInReleases,
                                                            critical);

                                                } catch (Exception e) {
//                                                    Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                                                }
                                            }
                                            */
                                        });
                                queueIzzyRepo.add(stringRequestIzzyRepo);
                            } else {
//                                Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "yyyyyyyyyyyy");

                                try {
                                    boolean critical = pppReleaseData.critical;
                                    String versionNameInReleases = pppReleaseData.versionNameInReleases;
                                    int versionCodeInReleases = pppReleaseData.versionCodeInReleases;

                                    showNotification(appContext,
                                            versionNameInReleases,
                                            versionCodeInReleases,
                                            critical);

                                } catch (Exception e) {
//                                    Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
                                }
                            }
                        }// else
//                            PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", "pppReleaseData=null");
                    },
                    error -> {
//                        PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(error));
                    });
            queue.add(stringRequest);

        } catch (Exception e) {
//            Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
        }

    }

    private static void showNotification(Context appContext,
                                         String versionNameInReleases,
                                         int versionCodeInReleases,
                                         boolean critical) {

        // remove non-critical notification
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                notificationManager.cancel(
                        PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_TAG,
                        PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_ID);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }

        removeNotification(appContext);

        // show notification for check new release
        PPApplicationStatic.createNewReleaseNotificationChannel(appContext, false);

        NotificationCompat.Builder mBuilder;
        Intent _intent;
        _intent = new Intent(appContext, CheckPPPReleasesActivity.class);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, true);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_NAME, versionNameInReleases);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CODE, versionCodeInReleases);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CRITICAL, critical);

        String nTitle;
        String nText;
        if (critical) {
            nTitle = appContext.getString(R.string.ppp_app_name) + StringConstants.STR_COLON_WITH_SPACE + appContext.getString(R.string.critical_github_release);
            nText = appContext.getString(R.string.critical_github_release_notification);
        }
        else {
            nTitle = appContext.getString(R.string.ppp_app_name) + StringConstants.STR_COLON_WITH_SPACE + appContext.getString(R.string.normal_github_release);
            nText = appContext.getString(R.string.normal_github_release_notification);
        }
        //mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL)
        mBuilder = new NotificationCompat.Builder(appContext, PPApplication.PROFILE_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.informationColor))
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

        Intent disableIntent = new Intent(appContext, CheckCriticalPPPReleasesDisableActivity.class);
        disableIntent.putExtra(CheckCriticalPPPReleasesDisableActivity.EXTRA_PPP_RELEASE_CODE, versionCodeInReleases);
        disableIntent.putExtra(CheckCriticalPPPReleasesDisableActivity.EXTRA_PPP_RELEASE_CRITICAL, critical);

        PendingIntent pDisableIntent = PendingIntent.getActivity(appContext, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                //R.drawable.ic_action_exit_app,
                R.drawable.ic_empty,
                appContext.getString(R.string.critical_github_release_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());

        mBuilder.setGroup(PPApplication.CHECK_RELEASES_GROUP);

        Notification notification = mBuilder.build();

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(
                    PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID, notification);
//            PPApplicationStatic.logE("CheckCriticalPPPReleasesBroadcastReceiver.showNotification", "notification displayed - xxxxxxxxxxxxxxxx");
        } catch (SecurityException en) {
//            PPApplicationStatic.logException("CheckCriticalPPPReleasesBroadcastReceiver.showNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("CheckCriticalPPPReleasesBroadcastReceiver.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }

    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    static void getShowCriticalGitHubReleasesNotification(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] CheckCriticalPPPReleasesBroadcastReceiver.getShowCriticalGitHubReleasesNotification", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION, 0);
            //return prefRingerVolume;
        }
    }

    static void setShowCriticalGitHubReleasesNotification(Context context, int versionCode)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] CheckCriticalPPPReleasesBroadcastReceiver.setShowCriticalGitHubReleasesNotification", "PPApplication.applicationGlobalPreferencesMutex");
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION, versionCode);
            editor.apply();
            ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification = versionCode;
        }
    }

}
