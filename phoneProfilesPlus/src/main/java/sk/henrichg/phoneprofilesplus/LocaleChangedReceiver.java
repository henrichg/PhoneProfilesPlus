package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CallsCounter.logCounter(context, "LocaleChangedReceiver.onReceive", "LocaleChangedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PPApplication.logE("##### LocaleChangedReceiver.onReceive", "xxx");

            if (ApplicationPreferences.applicationLanguage(context).equals("system")) {
                if (PhoneProfilesService.instance != null) {
                    DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), true, false, 0);
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context.getApplicationContext());
                    Profile profile = dataWrapper.getActivatedProfile();
                    PhoneProfilesService.instance.showProfileNotification(profile, dataWrapper);
                }
            }
        }
    }

}
