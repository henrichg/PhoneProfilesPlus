package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/** @noinspection ExtractMethodRecommender*/
public class BootUpReceiver extends BroadcastReceiver {

    //static boolean bootUpCompleted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BootUpReceiver.onReceive", "xxx");

        //PPApplication.cancelAllWorks(true);

        if (intent == null)
            return;

        String action = intent.getAction();
        boolean okAction = false;
        if (action != null) {

            // support for Direct boot
            // okAction = action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED);

            //if (!okAction)
                okAction = action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                        action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                        action.equals("com.htc.intent.action.QUICKBOOT_POWERON");
        }
        if (okAction) {

            PPApplicationStatic.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

            // moved to doForFirstStart - better when PPSerivice is restarted by system
            //PPApplication.setBlockProfileEventActions(true);

            if (PPApplicationStatic.logEnabled()) {
                PPApplicationStatic.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot);
                PPApplicationStatic.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents);
                PPApplicationStatic.logE("BootUpReceiver.onReceive", "globalEventsRunning=" + EventStatic.getGlobalEventsRunning(context));
            }

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BootUpReceiver.onReceive2");

                //synchronized (PPApplication.handleEventsMutex) {

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BootUpReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (ApplicationPreferences.applicationStartOnBoot) {
                            //PPApplicationStatic.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                            PPApplication.deviceBoot = true;

                            PPApplicationStatic.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, null, null, "");

                            boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                            PPApplicationStatic.logE("BootUpReceiver.onReceive", "serviceStarted=" + serviceStarted);

                            //PPApplication.sleep(3000);
                            if (!serviceStarted) {
                                // service is not started

                                //AutostartPermissionNotification.showNotification(appContext, false);

                                PPApplicationStatic.logE("BootUpReceiver.onReceive", "start service");
                                // service is not started, start it
                                PPApplicationStatic.setApplicationStarted(appContext, true);
                                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, true);
                                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, false);
//                                PPApplicationStatic.logE("[START_PP_SERVICE] BootUpReceiver.onReceive", "xxx");
                                PPApplicationStatic.startPPService(appContext, serviceIntent, true);
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

                                if (EventStatic.getGlobalEventsRunning(appContext)) {

//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] BootUpReceiver.onReceive", "SENSOR_TYPE_BOOT_COMPLETED");
                                    PPExecutors.handleEvents(appContext,
                                            new int[]{EventsHandler.SENSOR_TYPE_BOOT_COMPLETED},
                                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_BOOT_COMPLETED, 0);
                                    /*
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_BOOT_COMPLETED});
                                    */
                                }
                            }
                        } else {
                            if (PPApplicationStatic.logEnabled()) {
                                PPApplicationStatic.logE("BootUpReceiver.onReceive", "ApplicationPreferences.applicationStartOnBoot()=false");
                            }
                            PPApplication.deviceBoot = false;

                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                            PPApplicationStatic.exitApp(false, appContext, dataWrapper, null, false, true, false);
                        }

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    //}

                //}
            };
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);

        }

    }

}
