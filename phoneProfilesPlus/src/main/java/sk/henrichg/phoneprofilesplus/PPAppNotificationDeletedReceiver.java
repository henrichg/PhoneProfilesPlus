package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PPAppNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_PP_APP_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".PPAppNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] StartEventNotificationDeletedReceiver.onReceive", "xxx");
        PPAppNotification.forceDrawNotificationWhenIsDeleted(context.getApplicationContext());
    }

}
