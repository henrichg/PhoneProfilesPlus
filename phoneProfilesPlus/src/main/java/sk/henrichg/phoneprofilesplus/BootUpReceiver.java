package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BootUpReceiver.onReceive", "BootUpReceiver_onReceive");

        if (intent == null)
            return;

        String action = intent.getAction();
        if ((action != null) && (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                action.equals("com.htc.intent.action.QUICKBOOT_POWERON"))) {

            PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

            // if startedOnBoot = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
            PPApplication.startedOnBoot = true;
            PPApplication.startHandlerThread("BootUpReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30000);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));
            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents(context));
            PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+Event.getGlobalEventsRunning(context));

            //PPApplication.setApplicationStarted(context, false);

            if (ApplicationPreferences.applicationStartOnBoot(context)) {
                PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                // start service
                PPApplication.setApplicationStarted(context.getApplicationContext(), true);
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                PPApplication.startPPService(context, serviceIntent);
            }

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

        }

    }

}
