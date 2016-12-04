package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### LocaleChangedReceiver.onReceive","xxx");

        GlobalData.loadPreferences(context);

        if (GlobalData.applicationLanguage.equals("system"))
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(GlobalData.PROFILE_NOTIFICATION_ID);
            }
        }
    }

}
