package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartEventNotificationDeletedReceiver extends BroadcastReceiver {

    static final String START_EVENT_NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".START_EVENT_NOTIFICATION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### StartEventNotificationDeletedReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "StartEventNotificationDeletedReceiver.onReceive", "StartEventNotificationDeletedReceiver_onReceive");

        StartEventNotificationBroadcastReceiver.removeAlarm(context.getApplicationContext());
    }

}
