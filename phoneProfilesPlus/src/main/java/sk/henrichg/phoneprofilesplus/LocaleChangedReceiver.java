package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] LocaleChangedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            //final Context appContext = context.getApplicationContext();

            if (PPApplicationStatic.getApplicationStarted(false, false)) {

                PPApplication.collator = GlobalUtils.getCollator();

                PPApplicationStatic.createNotificationChannels(context.getApplicationContext(), true);

                //if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                //PPApplication.showProfileNotification(/*true*/);
                //if (PhoneProfilesService.getInstance() != null)
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] LocaleChangedReceiver.onReceive", "call of PPAppNotification.showNotification");

                PPAppNotification.showNotification(context.getApplicationContext(),false, true, false, false);
                ProfileListNotification.showNotification(context.getApplicationContext(), false);
                //}
            }
        }
    }

}
