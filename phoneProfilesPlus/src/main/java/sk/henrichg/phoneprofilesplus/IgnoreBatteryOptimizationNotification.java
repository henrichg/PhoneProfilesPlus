package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class IgnoreBatteryOptimizationNotification {

    private static final String PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START = "show_ignore_battery_optimization_notification_on_start";

    static void showNotification(Context context, boolean useHandler) {
        //if (Build.VERSION.SDK_INT >= 23) {
            //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "xxx");

            if (useHandler) {
                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThread(/*"IgnoreBatteryOptimizationNotification.showNotification"*/);
                final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=IgnoreBatteryOptimizationNotification.showNotification");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":IgnoreBatteryOptimizationNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

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
                                    //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "pm="+pm);

                                    if (!pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME)) {
                                        //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "optimized");

                                        if (ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart)
                                            showNotification(appContext,
                                                    appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                                    appContext.getString(R.string.ignore_battery_optimization_notification_text));
                                    } else {
                                        //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "not optimized");

                                        // show notification again
                                        setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                            //}

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=IgnoreBatteryOptimizationNotification_showNotification");
                        } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                    final Context appContext = context.getApplicationContext();

                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    try {
                        if (pm != null) {
                            //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "pm="+pm);

                            if (!pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME)) {
                                //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "optimized");

                                if (ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart)
                                    showNotification(appContext,
                                            appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                            appContext.getString(R.string.ignore_battery_optimization_notification_text));
                            }
                            else {
                                //PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "not optimized");

                                // show notification again
                                setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                            }
                        }
                    } catch (Exception ignore) {
                    }
                //}
            }
        //}
    }

    @SuppressLint("BatteryLife")
    static private void showNotification(Context context, String title, String text) {
        //noinspection UnnecessaryLocalVariable
        String nTitle = title;
        //noinspection UnnecessaryLocalVariable
        String nText = text;
//        if (Build.VERSION.SDK_INT < 24) {
//            nTitle = context.getString(R.string.ppp_app_name);
//            nText = title+": "+text;
//        }
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        Intent intent;
        //PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //String packageName = PPApplication.PACKAGE_NAME;
        //if (pm.isIgnoringBatteryOptimizations(packageName)) {
            intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        //}
        //else {
        //    DO NOT USE IT, CHANGE IS NOT DISPLAYED IN SYSTEM SETTINGS
        //    intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        //    intent.setData(Uri.parse("package:" + packageName));
        //    if (!GlobalGUIRoutines.activityIntentExists(intent, context)) {
        //        intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        //    }
        //}

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}
        mBuilder.setOnlyAlertOnce(true);

        Intent disableIntent = new Intent(context, IgnoreBatteryOptimizationDisableActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pDisableIntent = PendingIntent.getActivity(context, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_exit_app,
                context.getString(R.string.ignore_battery_optimization_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());
        mBuilder.setWhen(0);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG,
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("IgnoreBatteryOptimizationNotification.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG,
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

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
}
