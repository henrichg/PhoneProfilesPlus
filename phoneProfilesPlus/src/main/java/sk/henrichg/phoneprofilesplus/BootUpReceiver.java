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
            PhoneProfilesService.startHandlerThread();
            final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("BootUpReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 10000);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));
            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents(context));
            PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+Event.getGlobalEventsRunning(context));

            /*
            BluetoothConnectionBroadcastReceiver.clearConnectedDevices(context, true);
            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(context);
            */

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
