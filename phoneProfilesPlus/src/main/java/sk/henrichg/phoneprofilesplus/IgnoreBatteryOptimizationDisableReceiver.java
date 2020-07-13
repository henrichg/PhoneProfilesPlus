package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Disable action button
public class IgnoreBatteryOptimizationDisableReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### IgnoreBatteryOptimizationDisableReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "IgnoreBatteryOptimizationDisableReceiver.onReceive", "IgnoreBatteryOptimizationDisableReceiver_onReceive");

        IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(context.getApplicationContext(), false);
        IgnoreBatteryOptimizationNotification.removeNotification(context.getApplicationContext());

        /*
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            StatusBarNotification[] notifications = manager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if ((notification.getId() == PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID) &&
                    (notification.getTag().equals(PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG))) {
                    manager.cancel(notification.getId());
                }
            }
        }
        */
    }
}