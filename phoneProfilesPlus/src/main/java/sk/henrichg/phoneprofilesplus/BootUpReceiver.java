package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

        PPApplication.loadPreferences(context);

        PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot="+ PPApplication.applicationStartOnBoot);
        //PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+PPApplication.getGlobalEventsRuning(context));

        PPApplication.setApplicationStarted(context, false);

        if (PPApplication.applicationStartOnBoot)
        {
            PPApplication.logE("BootUpReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            // start ReceiverService
            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PPApplication.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_START_ON_BOOT, true);
            context.startService(serviceIntent);
        }

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

    }

}
