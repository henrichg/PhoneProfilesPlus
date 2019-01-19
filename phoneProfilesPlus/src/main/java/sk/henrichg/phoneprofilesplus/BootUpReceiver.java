package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

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

            PPApplication.setBlockProfileEventActions(true);

            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot(context));
            PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents(context));
            PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning="+Event.getGlobalEventsRunning(context));

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();

            PPApplication.startHandlerThread("BootUpReceiver.onReceive2");
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver.onReceive.2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (ApplicationPreferences.applicationStartOnBoot(appContext)) {
                            PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                            // start service
                            PPApplication.setApplicationStarted(appContext, true);
                            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                            PPApplication.startPPService(appContext, serviceIntent);
                        } else {
                            PPApplication.exitApp(false, appContext, null, null, false/*, true, true*/);
                        }
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

        }

    }

}
