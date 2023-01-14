package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class DrawOverAppsPermissionNotification {

    static void showNotification(Context context, boolean useHandler) {
        if (Build.VERSION.SDK_INT >= 29) {
            // Must be granted because of:
            // https://developer.android.com/guide/components/activities/background-starts

            if (useHandler) {
                final Context appContext = context.getApplicationContext();
                //PPApplication.startHandlerThread(/*"DrawOverAppsPermissionNotification.showNotification"*/);
                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DrawOverAppsPermissionNotification.showNotification");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DrawOverAppsPermissionNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            try {
                                if (!Settings.canDrawOverlays(appContext)) {
                                    showNotification(appContext,
                                            appContext.getString(R.string.draw_over_apps_permission_notification_title),
                                            appContext.getString(R.string.draw_over_apps_permission_notification_text));
                                }
                            } catch (Exception ignore) {
                            }

                        } catch (Exception e) {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                }; //);
                PPApplication.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);
            }
            else {
                try {
                    final Context appContext = context.getApplicationContext();
                    if (!Settings.canDrawOverlays(appContext)) {
                        showNotification(appContext,
                                appContext.getString(R.string.draw_over_apps_permission_notification_title),
                                appContext.getString(R.string.draw_over_apps_permission_notification_text));
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    static private void showNotification(Context context, String title, String text) {
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        final Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + PPApplication.PACKAGE_NAME));
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

        mBuilder.setWhen(0);

        mBuilder.setGroup(PPApplication.SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_TAG,
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_ID, mBuilder.build());
        } catch (SecurityException en) {
            // todo show dialog about grant notificication permission
        } catch (Exception e) {
            //Log.e("DrawOverAppsPermissionNotification.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_TAG,
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
