package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class BootUpReceiver extends BroadcastReceiver {

    //static boolean bootUpCompleted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] BootUpReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "BootUpReceiver.onReceive", "BootUpReceiver_onReceive");

        //PPApplication.cancelAllWorks(true);

        if (intent == null)
            return;

        String action = intent.getAction();
        boolean okAction = false;
        if (action != null) {

            // support for Direct boot
            //if (Build.VERSION.SDK_INT >= 24)
            //    okAction = action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED);

            //if (!okAction)
                okAction = action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                        action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                        action.equals("com.htc.intent.action.QUICKBOOT_POWERON");
        }
        if (okAction) {

            PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

//            PPApplication.logE("[BLOCK_ACTIONS] BootUpReceiver.onReceive", "true");
            PPApplication.setBlockProfileEventActions(true);

            if (PPApplication.logEnabled()) {
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot);
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents);
                PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning=" + Event.getGlobalEventsRunning());
            }

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();

            PPApplication.startHandlerThreadBroadcast(/*"BootUpReceiver.onReceive2"*/);
            final Handler handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler2.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=BootUpReceiver.onReceive2");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (ApplicationPreferences.applicationStartOnBoot) {
                        PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());
                        //PPApplication.logE("BootUpReceiver.onReceive", "bootUpCompleted="+bootUpCompleted);

                        PPApplication.deviceBoot = true;

                        PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, null, null, null, 0, "");

                        boolean serviceStarted = PhoneProfilesService.isServiceRunning(appContext, PhoneProfilesService.class, false);
                        PPApplication.logE("BootUpReceiver.onReceive", "serviceStarted="+serviceStarted);

                        //PPApplication.sleep(3000);
                        if (!serviceStarted) {
                            // service is not started
                            PPApplication.logE("BootUpReceiver.onReceive", "start service");
                            // service is not started, start it
                            PPApplication.setApplicationStarted(appContext, true);
                            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, true);
                            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                            PPApplication.logE("[START_PP_SERVICE] BootUpReceiver.onReceive", "xxx");
                            PPApplication.startPPService(appContext, serviceIntent/*, true*/);
                        }
                    } else {
                        if (PPApplication.logEnabled()) {
                            PPApplication.logE("BootUpReceiver.onReceive", "ApplicationPreferences.applicationStartOnBoot()=false");
                            //PPApplication.logE("PPApplication.exitApp", "from BootUpReceiver.onReceive shutdown=false");
                        }
                        PPApplication.deviceBoot = false;

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                        PPApplication.exitApp(false, appContext, dataWrapper, null, false/*, true, true*/);
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BootUpReceiver.onReceive2");
                } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

        }

    }

}
