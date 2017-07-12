package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.Calendar;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

        //PPApplication.loadPreferences(context);

        // start delayed bootup broadcast
        PPApplication.startedOnBoot = true;
        final Handler handler = new Handler(context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                PPApplication.startedOnBoot = false;
            }
        }, 10000);

        PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot="+ ApplicationPreferences.applicationStartOnBoot(context));
        //PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+PPApplication.getGlobalEventsRuning(context));

        BluetoothConnectionBroadcastReceiver.clearConnectedDevices(context, true);
        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(context);

        PPApplication.setApplicationStarted(context, false);

        if (ApplicationPreferences.applicationStartOnBoot(context))
        {
            PPApplication.logE("BootUpReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            // start ReceiverService
            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
            context.startService(serviceIntent);
        }

        //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

    }

}
