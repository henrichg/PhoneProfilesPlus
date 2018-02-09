package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CallsCounter.logCounter(context, "LocaleChangedReceiver.onReceive", "LocaleChangedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PPApplication.logE("##### LocaleChangedReceiver.onReceive", "xxx");

            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                        if (PhoneProfilesService.instance != null) {
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                            Profile profile = dataWrapper.getActivatedProfile(true, true);
                            PhoneProfilesService.instance.showProfileNotification(profile, dataWrapper);
                        }
                    }
                }
            });
        }
    }

}
