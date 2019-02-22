package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
        boolean okAction = false;
        if (action != null) {
            if (Build.VERSION.SDK_INT >= 24)
                okAction = action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED);
            if (!okAction)
                okAction = action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                        action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                        action.equals("com.htc.intent.action.QUICKBOOT_POWERON");
        }
        if (okAction) {

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
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BootUpReceiver.onReceive2");

                        if (ApplicationPreferences.applicationStartOnBoot(appContext)) {
                            PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                            PPApplication.sleep(3000);
                            if (!PPApplication.getApplicationStarted(appContext, true)) {
                                // service is not started, start it
                                PPApplication.setApplicationStarted(appContext, true);
                                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, true);
                                PPApplication.startPPService(appContext, serviceIntent);
                            }
                            else {
                                // service is started by PPApplication
                                PPApplication.logE("BootUpReceiver.onReceive", "activate profiles");

                                final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_APPLICATIONSTARTONBOOT, null, null, null, 0);

                                // start events
                                if (Event.getGlobalEventsRunning(appContext)) {
                                    PPApplication.logE("BootUpReceiver.onReceive", "global event run is enabled, first start events");

                                    if (!dataWrapper.getIsManualProfileActivation(false)) {
                                        ////// unblock all events for first start
                                        //     that may be blocked in previous application run
                                        dataWrapper.pauseAllEvents(true, false/*, false*/);
                                    }

                                    dataWrapper.firstStartEvents(true, false);
                                    dataWrapper.updateNotificationAndWidgets(true);
                                } else {
                                    PPApplication.logE("BootUpReceiver.onReceive", "global event run is not enabled, manually activate profile");

                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false/*, false*/);

                                    dataWrapper.activateProfileOnBoot();
                                    dataWrapper.updateNotificationAndWidgets(true);
                                }
                            }
                        } else {
                            PPApplication.exitApp(false, appContext, null, null, false/*, true, true*/);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BootUpReceiver.onReceive2");
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
