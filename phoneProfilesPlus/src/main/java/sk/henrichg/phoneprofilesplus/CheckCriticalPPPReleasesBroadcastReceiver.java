package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import static android.app.Notification.DEFAULT_VIBRATE;

public class CheckCriticalPPPReleasesBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION = "show_critical_github_release_code_notification";
    private static final String PREF_CRITICAL_PPP_RELEASE_ALARM = "critical_github_release_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] CheckCriticalGitHubReleasesBroadcastReceiver.onReceive", "xxx");
//        CallsCounter.logCounter(context, "CheckGitHubReleasesBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (intent != null) {
            doWork(/*true,*/ context);
        }
    }

    static public void setAlarm(Context context)
    {
        removeAlarm(context);

//        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "xxx");

        Calendar alarm = Calendar.getInstance();
//        if (PPApplication.logEnabled()) {
//            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//            String result = sdf.format(alarm.getTimeInMillis());
//            Log.e("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "now=" + result);
//        }

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_CRITICAL_PPP_RELEASE_ALARM, 0);
//        if (PPApplication.logEnabled()) {
//            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//            String result = sdf.format(lastAlarm);
//            Log.e("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "lastAlarm=" + result);
//        }

        long alarmTime;

        /*if (DebugVersion.enabled) {
            alarm.add(Calendar.MINUTE, 1);

            if (PPApplication.logEnabled()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarm.getTimeInMillis());
                Log.e("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
            }

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

//                if (PPApplication.logEnabled()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                    String result = sdf.format(alarm.getTimeInMillis());
//                    Log.e("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "alarm=" + result);
//                }

                alarmTime = alarm.getTimeInMillis();

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
                editor.putLong(PREF_CRITICAL_PPP_RELEASE_ALARM, alarmTime);
                editor.apply();
            } else {
                alarmTime = lastAlarm;

//                if (PPApplication.logEnabled()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
//                    String result = sdf.format(alarmTime);
//                    Log.e("CheckCriticalGitHubReleasesBroadcastReceiver.setAlarm", "alarm 2=" + result);
//                }
            }
        }

        //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
        //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

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
                //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES);
                //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

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

    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

//        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver.doWork", "xxx");

        //if (useHandler) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadBroadcast(/*"DonationBroadcastReceiver.onReceive"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=CheckGitHubReleasesBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CheckCriticalGitHubReleasesBroadcastReceiver_doWork");
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
                //}
            });
        /*}
        else {
            _doWork(appContext);
            setAlarm(appContext);
        }*/
    }

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

    private static void _doWork(Context appContext) {
        boolean showNotification = false;
        boolean critical = true;
        int versionCodeInReleases = 0;
        try {
            String contents;// = "";
            URLConnection conn;
            if (DebugVersion.enabled)
                conn = new URL(PPApplication.PPP_RELEASES_DEBUG_URL).openConnection();
            else
                conn = new URL(PPApplication.PPP_RELEASES_URL).openConnection();
//            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "conn.getUrl()="+conn.getURL());
            InputStream in = conn.getInputStream();
            contents = convertStreamToString(in);
//            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "contents="+contents);

            if (!contents.isEmpty()) {
                int startIndex = contents.indexOf("@@@ppp-release:");
                int endIndex = contents.indexOf("***@@@");
                if ((startIndex >= 0) && (endIndex > startIndex)) {
                    String version = contents.substring(startIndex, endIndex);
                    startIndex = version.indexOf(":");
                    version = version.substring(startIndex + 1);
//                    PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "version="+version);
                    String[] splits = version.split(":");
                    if (splits.length >= 2) {
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "newVersionName=" + splits[0]);
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "newVersionCode=" + splits[1]);
                        int versionCode = 0;
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            versionCode = PPApplication.getVersionCode(pInfo);
                        } catch (Exception ignored) {
                        }
                        versionCodeInReleases = Integer.parseInt(splits[1]);
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "versionCodeInReleases=" + versionCodeInReleases);
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification=" + ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification);
                        if (ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification < versionCodeInReleases) {
                            if ((versionCode > 0) && (versionCode < versionCodeInReleases))
                                showNotification = true;
                        }
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "showNotification=" + showNotification);
                    }
/*                    if (splits.length == 2) {
                        // old check, always critical update
                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "OLD CHECK");
                        //critical = true;
                    }*/
                    if (splits.length == 3) {
                        // new, better check
//                        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "NEW CHECK");
                        // last parameter:
                        //  "normal" - normal update
                        //  "critical" - critical update
                        critical = splits[2].equals("critical");
                    }
                }
            }

        } catch (IOException ignored) {}

//        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "showNotification="+showNotification);
//        PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "critical="+critical);

        if (showNotification) {
            removeNotification(appContext);

            // show notification for check new release
            PPApplication.createNewReleaseNotificationChannel(appContext);

            NotificationCompat.Builder mBuilder;
            Intent _intent;
            _intent = new Intent(appContext, CheckPPPReleasesActivity.class);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, true);

            String nTitle;
            String nText;
            if (critical) {
                nTitle = appContext.getString(R.string.critical_github_release);
                nText = appContext.getString(R.string.critical_github_release_notification);
//                if (android.os.Build.VERSION.SDK_INT < 24) {
//                    nTitle = appContext.getString(R.string.ppp_app_name);
//                    nText = appContext.getString(R.string.critical_github_release) + ": " +
//                            appContext.getString(R.string.critical_github_release_notification);
//                }
            }
            else {
                nTitle = appContext.getString(R.string.normal_github_release);
                nText = appContext.getString(R.string.normal_github_release_notification);
//                if (android.os.Build.VERSION.SDK_INT < 24) {
//                    nTitle = appContext.getString(R.string.ppp_app_name);
//                    nText = appContext.getString(R.string.normal_github_release) + ": " +
//                            appContext.getString(R.string.normal_github_release_notification);
//                }
            }
            mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_CHANNEL)
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

//            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "putExtra - versionCodeInReleases=" + versionCodeInReleases);
//            PPApplication.logE("CheckCriticalGitHubReleasesBroadcastReceiver._doWork", "putExtra - critical=" + critical);
            Intent disableIntent = new Intent(appContext, CheckCriticalPPPReleasesDisableActivity.class);
            disableIntent.putExtra(CheckCriticalPPPReleasesDisableActivity.EXTRA_PPP_RELEASE_CODE, versionCodeInReleases);
            disableIntent.putExtra(CheckCriticalPPPReleasesDisableActivity.EXTRA_PPP_RELEASE_CRITICAL, critical);

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pDisableIntent = PendingIntent.getActivity(appContext, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                    R.drawable.ic_action_exit_app,
                    appContext.getString(R.string.critical_github_release_notification_disable_button),
                    pDisableIntent);
            mBuilder.addAction(actionBuilder.build());

            Notification notification = mBuilder.build();
            if (Build.VERSION.SDK_INT < 26) {
                notification.vibrate = null;
                notification.defaults &= ~DEFAULT_VIBRATE;
            }

            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
            try {
                mNotificationManager.notify(
                        PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG,
                        PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                //Log.e("CheckGitHubReleasesBroadcastReceiver._doWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
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
            PPApplication.recordException(e);
        }
    }

    static void getShowCriticalGitHubReleasesNotification(Context context)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION, 0);
            //return prefRingerVolume;
        }
    }

    static void setShowCriticalGitHubReleasesNotification(Context context, int versionCode)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_SHOW_CRITICAL_PPP_RELEASE_CODE_NOTIFICATION, versionCode);
            editor.apply();
            ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification = versionCode;
        }
    }

}
