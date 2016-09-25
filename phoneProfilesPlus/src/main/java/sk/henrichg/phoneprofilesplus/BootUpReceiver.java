package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### BootUpReceiver.onReceive", "xxx");

        //GlobalData.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

        GlobalData.loadPreferences(context);

        GlobalData.logE("BootUpReceiver.onReceive", "applicationStartOnBoot="+GlobalData.applicationStartOnBoot);
        //GlobalData.logE("BootUpReceiver.onReceive", "globalEventsRunning="+GlobalData.getGlobalEventsRuning(context));

        GlobalData.setApplicationStarted(context, false);

        if (GlobalData.applicationStartOnBoot)
        {
            GlobalData.logE("BootUpReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            // start ReceiverService
            context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
        }

        //GlobalData.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

    }

}
