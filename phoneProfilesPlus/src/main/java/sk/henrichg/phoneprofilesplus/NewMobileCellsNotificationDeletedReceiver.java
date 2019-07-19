package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Delete button (X) or "clear all" in notification
public class NewMobileCellsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NewMobileCellsNotificationDeletedReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NewMobileCellsNotificationDeletedReceiver.onReceive", "NewMobileCellsNotificationDeletedReceiver_onReceive");

        //Permissions.clearMergedPermissions(context.getApplicationContext());

    }

}
