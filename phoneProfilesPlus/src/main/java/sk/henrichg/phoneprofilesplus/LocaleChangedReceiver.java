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

            if (PPApplication.getApplicationStarted(appContext, false)) {
                PPApplication.startHandlerThread("LocaleChangedReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=LocaleChangedReceiver.onReceive");

                        if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                            PPApplication.showProfileNotification(/*appContext*/true);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=LocaleChangedReceiver.onReceive");
                    }
                });
            }
        }
    }

}
