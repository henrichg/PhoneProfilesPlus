package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class DNDPermissionNotification {

    static void showNotification(Context context, boolean useHandler) {
        if (useHandler) {
            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DrawOverAppsPermissionNotification.showNotification");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DrawOverAppsPermissionNotification_showNotification);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        try {
                            NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                            if (mNotificationManager != null) {
                                if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                                    showNotification(appContext,
                                            appContext.getString(R.string.do_not_disturb_access_permission_notification),
                                            appContext.getString(R.string.do_not_disturb_access_permission_notification_text));
                                }
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
        }
        else {
            try {
                final Context appContext = context.getApplicationContext();
                NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                        showNotification(appContext,
                                appContext.getString(R.string.do_not_disturb_access_permission_notification),
                                appContext.getString(R.string.do_not_disturb_access_permission_notification_text));
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    static private void showNotification(Context context, String title, String text) {
        PPApplicationStatic.createExclamationNotificationChannel(context.getApplicationContext(), false);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.errorColor))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_exclamation_notification))
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        //Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        final Intent intent = new Intent(context, ErrorNotificationActivity.class);
        intent.putExtra(ErrorNotificationActivity.EXTRA_ERROR_TYPE, ErrorNotificationActivity.ERROR_TYPE_DND_ACCESS);
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
                    PPApplication.DO_NOT_DISTURB_ACCESS_NOTIFICATION_TAG,
                    PPApplication.DO_NOT_DISTURB_ACCESS_NOTIFICATION_ID, mBuilder.build());
        } catch (SecurityException en) {
            PPApplicationStatic.logException("DNDPermissionNotification.showNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("DrawOverAppsPermissionNotification.showNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.DO_NOT_DISTURB_ACCESS_NOTIFICATION_TAG,
                    PPApplication.DO_NOT_DISTURB_ACCESS_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
