package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                action.equals("com.htc.intent.action.QUICKBOOT_POWERON")) {

            PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

            // start delayed boot up broadcast
            PPApplication.startedOnBoot = true;
            final Handler handler = new Handler(context.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 10000);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));
            //PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+PPApplication.getGlobalEventsRunning(context));

            BluetoothService.clearConnectedDevices(context, true);
            BluetoothService.saveConnectedDevices(context);

            PPApplication.setApplicationStarted(context, false);

            if (ApplicationPreferences.applicationStartOnBoot(context)) {
                PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.instance=" + PhoneProfilesService.instance);

                // start ReceiverService
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26)
                    context.startService(serviceIntent);
                //else
                //    context.startForegroundService(serviceIntent);
            }

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

        }

    }

}
