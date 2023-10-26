package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KeepScreenOnNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_KEEP_SCREEN_ON_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".KeepScreenOnNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] KeepScreenOnNotificationDeletedReceiver.onReceive", "xxx");
        ActivateProfileHelper.showKeepScreenOnNotificaiton(context.getApplicationContext());
    }

}
