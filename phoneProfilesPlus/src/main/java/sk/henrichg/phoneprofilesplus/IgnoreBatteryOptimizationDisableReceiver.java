package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.notification.StatusBarNotification;

// Disable action button
public class IgnoreBatteryOptimizationDisableReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### IgnoreBatteryOptimizationDisableReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "IgnoreBatteryOptimizationDisableReceiver.onReceive", "IgnoreBatteryOptimizationDisableReceiver_onReceive");

        ApplicationPreferences.getSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(IgnoreBatteryOptimizationNotification.PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, false);
        editor.apply();

        if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            StatusBarNotification[] notifications = manager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() >= PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID) {
                    manager.cancel(notification.getId());
                }
            }
        }
        /*else {
            int notificationId = intent.getIntExtra("notificationId", 0);
            manager.cancel(notificationId);
        }*/
    }

}
