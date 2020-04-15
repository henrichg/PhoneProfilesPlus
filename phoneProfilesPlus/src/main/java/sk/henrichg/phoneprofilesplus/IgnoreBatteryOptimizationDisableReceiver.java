package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

// Disable action button
public class IgnoreBatteryOptimizationDisableReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### IgnoreBatteryOptimizationDisableReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "IgnoreBatteryOptimizationDisableReceiver.onReceive", "IgnoreBatteryOptimizationDisableReceiver_onReceive");

        IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(context.getApplicationContext(), false);

        //if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                StatusBarNotification[] notifications = manager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() >= PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID) {
                        manager.cancel(notification.getId());
                    }
                }
            }
        //}
        /*else {
            int notificationId = intent.getIntExtra("notificationId", 0);
            manager.cancel(notificationId);
        }*/
    }

}
