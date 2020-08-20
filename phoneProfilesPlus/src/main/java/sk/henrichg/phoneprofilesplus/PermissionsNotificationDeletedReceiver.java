package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Delete button (X) or "clear all" in notification
public class PermissionsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[BROADCAST CALL] PermissionsNotificationDeletedReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "PermissionsNotificationDeletedReceiver.onReceive", "PermissionsNotificationDeletedReceiver_onReceive");

        //Permissions.clearMergedPermissions(context.getApplicationContext());

    }

}
