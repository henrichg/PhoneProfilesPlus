package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class BootUpReceiver extends BroadcastReceiver {

    //static boolean bootUpCompleted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] BootUpReceiver.onReceive", "xxx");

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

            // moved to doForFirstStart - better when PPSerivice is restarted by system
            //PPApplication.setBlockProfileEventActions(true);

            if (PPApplication.logEnabled()) {
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot);
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents);
                PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning=" + Event.getGlobalEventsRunning(context));
            }

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"BootUpReceiver.onReceive2"*/);
            //final Handler __handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            //__handler2.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BootUpReceiver.onReceive2");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (ApplicationPreferences.applicationStartOnBoot) {
                            PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                            PPApplication.deviceBoot = true;

                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, null, null, "");

                            boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                            PPApplication.logE("BootUpReceiver.onReceive", "serviceStarted=" + serviceStarted);

                            //PPApplication.sleep(3000);
                            if (!serviceStarted) {
                                // service is not started

                                //AutostartPermissionNotification.showNotification(appContext, false);

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
//                                PPApplication.logE("[START_PP_SERVICE] BootUpReceiver.onReceive", "xxx");
                                PPApplication.startPPService(appContext, serviceIntent);
                            } else {
                                // start events handler

                                /*
                                PPApplication.registerPhoneCallsListener(false, appContext);
                                PPApplication.registerPPPExtenderReceiverForSMSCall(false, appContext);
                                PPApplication.registerReceiversForCallSensor(false, appContext);
                                PPApplication.registerReceiversForSMSSensor(false, appContext);
                                GlobalUtils.sleep(1000);
                                PPApplication.registerPhoneCallsListener(true, appContext);
                                PPApplication.registerPPPExtenderReceiverForSMSCall(true, appContext);
                                PPApplication.registerReceiversForCallSensor(true, appContext);
                                PPApplication.registerReceiversForSMSSensor(true, appContext);

                                PPApplication.restartMobileCellsScanner(appContext);
                                */

                                if (Event.getGlobalEventsRunning(appContext)) {

//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] BootUpReceiver.onReceive", "sensorType=SENSOR_TYPE_BOOT_COMPLETED");
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BOOT_COMPLETED);
                                }
                            }
                        } else {
                            if (PPApplication.logEnabled()) {
                                PPApplication.logE("BootUpReceiver.onReceive", "ApplicationPreferences.applicationStartOnBoot()=false");
                            }
                            PPApplication.deviceBoot = false;

                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            PPApplication.exitApp(false, appContext, dataWrapper, null, false, true);
                        }

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);

        }

    }

}
