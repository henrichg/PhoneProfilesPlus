package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

class AutostartPermissionNotification {

/*
    static void showNotification(Context context, @SuppressWarnings("SameParameterValue") boolean useHandler) {
//        PPApplicationStatic.logE("AutostartPermissionNotification.showNotification", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);

        if (!PPApplication.applicationFullyStarted) {
            final Context appContext = context.getApplicationContext();

            if (useHandler) {
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=AutostartPermissionNotification.showNotification");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {

                    //boolean isServiceRunning = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                    //PPApplicationStatic.logE("AutostartPermissionNotification.showNotification", "isServiceRunning="+isServiceRunning);
                    //if (!isServiceRunning) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AutostartPermissionNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            try {
                                final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
                                if (autoStartPermissionHelper.isAutoStartPermissionAvailable(appContext)) {
                                    showNotification(appContext,
                                            appContext.getString(R.string.autostart_permission_notification_title),
                                            appContext.getString(R.string.autostart_permission_notification_text));
                                }
                            } catch (Exception ignore) {
                            }

                        } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            } else {
                //boolean isServiceRunning = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                //PPApplicationStatic.logE("AutostartPermissionNotification.showNotification", "isServiceRunning="+isServiceRunning);
                //if (!isServiceRunning) {
                    try {
                        final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
                        if (autoStartPermissionHelper.isAutoStartPermissionAvailable(appContext)) {
                            showNotification(appContext,
                                    appContext.getString(R.string.autostart_permission_notification_title),
                                    appContext.getString(R.string.autostart_permission_notification_text));
                        }
                    } catch (Exception ignore) {
                    }
                //}
            }
        }
    }

    static private void showNotification(Context context, String title, String text) {
        PPApplicationStatic.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.notification_color))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        Intent intent;
        intent = new Intent(context, AutostartPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        mBuilder.setWhen(0);

        mBuilder.setGroup(PPApplication.SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_TAG,
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_ID, mBuilder.build());
        } catch (SecurityException en) {
            Log.e("AutostartPermissionNotification.showNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("AutostartPermissionNotification.showNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }
*/
    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_TAG,
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
