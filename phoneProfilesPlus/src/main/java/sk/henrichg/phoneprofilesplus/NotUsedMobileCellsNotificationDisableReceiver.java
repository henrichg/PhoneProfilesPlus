package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;

// Disable action button
public class NotUsedMobileCellsNotificationDisableReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] NotUsedMobileCellsNotificationDisableReceiver.onReceive", "xxx");

        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context.getApplicationContext());
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, false);
        editor.apply();
        ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context.getApplicationContext());

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
                StatusBarNotification[] notifications = manager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    String tag = notification.getTag();
                    if ((tag != null) && tag.contains(PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG+"_")) {
                        if (notification.getId() >= PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID) {
                            manager.cancel(notification.getTag(), notification.getId());
                        }
                    }
                }
        }
    }

}
