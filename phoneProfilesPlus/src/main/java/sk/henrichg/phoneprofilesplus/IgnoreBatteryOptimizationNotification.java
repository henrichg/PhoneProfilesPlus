package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

class IgnoreBatteryOptimizationNotification {

    //private static final String PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START = "show_ignore_battery_optimization_notification_on_start";
    //static final String IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION = PPApplication.PACKAGE_NAME + ".IgnoreBatteryOptimizationNotification.DISABLE_ACTION";

    static void showNotification(Context context, boolean useHandler) {
        //if (Build.VERSION.SDK_INT >= 23) {
            //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "xxx");

            final Context appContext = context.getApplicationContext();

            if (useHandler) {
                PPApplication.startHandlerThread(/*"IgnoreBatteryOptimizationNotification.showNotification"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":IgnoreBatteryOptimizationNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=IgnoreBatteryOptimizationNotification.showNotification");

                            //boolean show = ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart;
                            //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 1=" + show);
                            /*if (Event.getGlobalEventsRunning()) {
                                //show = show && DataWrapper.getIsManualProfileActivation(false, appContext);
                                //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 2=" + show);
                                DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                                show = show && (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_ALL, false) != 0);
                                //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 3=" + show);
                            }
                            else
                            show = false;*/

                            //if (show) {
                                PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                try {
                                    if (pm != null) {
                                        if (!pm.isIgnoringBatteryOptimizations(appContext.getPackageName())) {
                                            showNotification(appContext,
                                                    appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                                    appContext.getString(R.string.ignore_battery_optimization_notification_text));
                                        }
                                    }
                                } catch (Exception ignore) {
                                }
                            //}

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=IgnoreBatteryOptimizationNotification_showNotification");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });

            }
            else {
                //boolean show = ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart;
                //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 1=" + show);
                /*if (Event.getGlobalEventsRunning()) {
                    //show = show && DataWrapper.getIsManualProfileActivation(false, appContext);
                    //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 2=" + show);
                    DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                    show = show && (databaseHandler.getTypeEventsCount(DatabaseHandler.ETYPE_ALL, false) != 0);
                    //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show 3=" + show);
                }
                else
                show = false;*/

                //if (show) {
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    try {
                        if (pm != null) {
                            if (!pm.isIgnoringBatteryOptimizations(appContext.getPackageName())) {
                                showNotification(appContext,
                                        appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                        appContext.getString(R.string.ignore_battery_optimization_notification_text));
                            }
                        }
                    } catch (Exception ignore) {
                    }
                //}
            }
        //}
    }

    static private void showNotification(Context context, String title, String text) {
        String nTitle = title;
        String nText = text;
        if (Build.VERSION.SDK_INT < 24) {
            nTitle = context.getString(R.string.ppp_app_name);
            nText = title+": "+text;
        }
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        mBuilder.setOnlyAlertOnce(true);

        /*
        Intent disableIntent = new Intent(IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION);
        PendingIntent pDisableIntent = PendingIntent.getBroadcast(context, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_exit_app_white,
                context.getString(R.string.ignore_battery_optimization_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());
        */

        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            try {
                mNotificationManager.notify(PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
                Log.e("IgnoreBatteryOptimizationNotification.showNotification", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID);
    }

    /*
    static void getShowIgnoreBatteryOptimizationNotificationOnStart(Context context)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, true);
            //return prefRingerVolume;
        }
    }
    static void setShowIgnoreBatteryOptimizationNotificationOnStart(Context context, boolean show)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, show);
            editor.apply();
            ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart = show;
        }
    }
    */
}
