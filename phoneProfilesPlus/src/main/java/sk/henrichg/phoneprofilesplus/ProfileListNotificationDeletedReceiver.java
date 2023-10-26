package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProfileListNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_PROFILE_LIST_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".ProfileListNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] KeepScreenOnNotificationDeletedReceiver.onReceive", "xxx");
        ProfileListNotification.forceDrawNotificationWhenIsDeleted(context.getApplicationContext());
    }

}
