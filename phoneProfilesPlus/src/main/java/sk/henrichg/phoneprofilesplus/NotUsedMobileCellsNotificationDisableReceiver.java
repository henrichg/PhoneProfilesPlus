package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.notification.StatusBarNotification;

// Disable action button
public class NotUsedMobileCellsNotificationDisableReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### NotUsedMobileCellsNotificationDisableReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "NotUsedMobileCellsNotificationDisableReceiver.onReceive", "NotUsedMobileCellsNotificationDisableReceiver_onReceive");

        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, false);
        editor.apply();
        ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context.getApplicationContext());

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                StatusBarNotification[] notifications = manager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() >= PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_ID) {
                        manager.cancel(notification.getId());
                    }
                }
            } else {
                int notificationId = intent.getIntExtra("notificationId", 0);
                manager.cancel(notificationId);
            }
        }
    }

}
