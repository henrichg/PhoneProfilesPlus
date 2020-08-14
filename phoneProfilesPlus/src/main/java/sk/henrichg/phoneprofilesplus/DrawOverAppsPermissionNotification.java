package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class DrawOverAppsPermissionNotification {

    static void showNotification(Context context, boolean useHandler) {
        if (Build.VERSION.SDK_INT >= 29) {
            //PPApplication.logE("DrawOverAppsPermissionNotification.showNotification", "xxx");

            final Context appContext = context.getApplicationContext();

            if (useHandler) {
                PPApplication.startHandlerThread(/*"DrawOverAppsPermissionNotification.showNotification"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DrawOverAppsPermissionNotification_showNotification");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DrawOverAppsPermissionNotification.showNotification");

                            try {
                                if (!Settings.canDrawOverlays(appContext)) {
                                    showNotification(appContext,
                                            appContext.getString(R.string.draw_over_apps_permission_notification_title),
                                            appContext.getString(R.string.draw_over_apps_permission_notification_text));
                                }
                            } catch (Exception ignore) {
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DrawOverAppsPermissionNotification");
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
                try {
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

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_TAG,
                    PPApplication.DRAW_OVER_APPS_NOTIFICATION_ID, mBuilder.build());
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
