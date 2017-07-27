package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### LocaleChangedReceiver.onReceive","xxx");

        //PPApplication.loadPreferences(context);

        if (ApplicationPreferences.applicationLanguage(context).equals("system"))
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
