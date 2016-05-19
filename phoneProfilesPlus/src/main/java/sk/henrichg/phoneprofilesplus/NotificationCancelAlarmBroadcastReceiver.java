package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationCancelAlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### NotificationCancelAlarmBroadcastReceiver.onReceive", "xxx");

        if (GlobalData.phoneProfilesService != null)
            GlobalData.phoneProfilesService.stopForeground(true);
        else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
        }
        
    }

}
