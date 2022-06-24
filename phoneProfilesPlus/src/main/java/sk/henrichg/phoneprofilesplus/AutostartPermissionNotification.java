package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class AutostartPermissionNotification {

    static void showNotification(Context context, @SuppressWarnings("SameParameterValue") boolean useHandler) {
        //PPApplication.logE("AutostartPermissionNotification.showNotification", "xxx");

        if (PPApplication.applicationFullyStarted) {
            final Context appContext = context.getApplicationContext();

            if (useHandler) {
                PPApplication.startHandlerThread();
                final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=AutostartPermissionNotification.showNotification");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {

                    boolean isServiceRunning = PhoneProfilesService.isServiceRunning(appContext, PhoneProfilesService.class, false);
                    if (!isServiceRunning) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AutostartPermissionNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            try {
                                //PPApplication.logE("AutostartPermissionNotification.showNotification", "pm="+pm);

                                final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
                                if (autoStartPermissionHelper.isAutoStartPermissionAvailable(appContext)) {
                                    //PPApplication.logE("AutostartPermissionNotification.showNotification", "optimized");

                                    showNotification(appContext,
                                            appContext.getString(R.string.autostart_permission_notification_title),
                                            appContext.getString(R.string.autostart_permission_notification_text));
                                }
                            } catch (Exception ignore) {
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=AutostartPermissionNotification_showNotification");
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
                    }
                });

            } else {
                boolean isServiceRunning = PhoneProfilesService.isServiceRunning(appContext, PhoneProfilesService.class, false);
                if (!isServiceRunning) {
                    try {
                        //PPApplication.logE("AutostartPermissionNotification.showNotification", "pm="+pm);

                        final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
                        if (autoStartPermissionHelper.isAutoStartPermissionAvailable(appContext)) {
                            //PPApplication.logE("AutostartPermissionNotification.showNotification", "optimized");

                            showNotification(appContext,
                                    appContext.getString(R.string.autostart_permission_notification_title),
                                    appContext.getString(R.string.autostart_permission_notification_text));
                        }
                    } catch (Exception ignore) {
                    }

                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    static private void showNotification(Context context, String title, String text) {
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        Intent intent;
        intent = new Intent(context, AutostartPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("UnspecifiedImmutableFlag")
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
        } catch (Exception e) {
            //Log.e("AutostartPermissionNotification.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_TAG,
                    PPApplication.AUTOSTART_PERMISSION_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
