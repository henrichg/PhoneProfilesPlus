package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

class IgnoreBatteryOptimizationNotification {

    private static final String PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START = "show_ignore_battery_optimization_notification_on_start";
    static final String IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION = PPApplication.PACKAGE_NAME + ".IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION";

    static void showNotification(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "xxx");

            ApplicationPreferences.getSharedPreferences(context);
            boolean show = ApplicationPreferences.preferences.getBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, true);
            PPApplication.logE("IgnoreBatteryOptimizationNotification.showNotification", "show="+show);

            if (show) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                    showNotification(context,
                            context.getString(R.string.ignore_battery_optimization_notification_title),
                            context.getString(R.string.ignore_battery_optimization_notification_text));
                }
            }
        }
    }

    static private void showNotification(Context context, String title, String text) {
        String nTitle = title;
        String nText = text;
        if (Build.VERSION.SDK_INT < 24) {
            nTitle = context.getString(R.string.app_name);
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

        Intent disableIntent = new Intent(IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_DISABLE_ACTION);
        PendingIntent pDisableIntent = PendingIntent.getBroadcast(context, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_exit_app_white,
                context.getString(R.string.ignore_battery_optimization_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());

        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID, mBuilder.build());
    }

    static void removeNotification(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID);
    }

    static void setShowIgnoreBatteryOptimizationNotificationOnStart(Context context, boolean show)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, show);
        editor.apply();
    }

}
