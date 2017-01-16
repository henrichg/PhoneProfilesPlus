package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### LocaleChangedReceiver.onReceive","xxx");

        PPApplication.loadPreferences(context);

        if (PPApplication.applicationLanguage.equals("system"))
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.stopForeground(true);
            else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(PPApplication.PROFILE_NOTIFICATION_ID);
            }
        }
    }

}
