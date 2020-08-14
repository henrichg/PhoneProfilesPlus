package sk.henrichg.phoneprofilesplus;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] ScreenOnOffBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "ScreenOnOffBroadcastReceiver.onReceive", "ScreenOnOffBroadcastReceiver_onReceive");

        /*if (intent != null)
            PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
        else
            return;*/
        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        final String action = intent.getAction();
        if (action == null)
            return;

        //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "before start handler");
        PPApplication.startHandlerThread(/*"ScreenOnOffBroadcastReceiver.onReceive"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ScreenOnOffBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //if (PPApplication.logEnabled()) {
//                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=ScreenOnOffBroadcastReceiver.onReceive");
                        //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of handler post");
                    //}

                    switch (action) {
                        case Intent.ACTION_SCREEN_ON: {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                                PPApplication.logE("[XXX] ScreenOnOffBroadcastReceiver.onReceive", "restartAllScanners");
                            }*/
                            PPApplication.isScreenOn = true;

                            /*
                            Profile profile = DatabaseHandler.getInstance(appContext).getActivatedProfile();
                            //if (profile != null)
                            //    PPApplication.logE("******** ScreenOnOffBroadcastReceiver.onReceive", "profile._screenOnPermanent="+profile._screenOnPermanent);
                            if ((profile != null) && (profile._screenOnPermanent == 1))
                                ActivateProfileHelper.createKeepScreenOnView(appContext);
                            else
                                ActivateProfileHelper.removeKeepScreenOnView();
                            */

                            // restart scanners for screen on when any is enabled
                            boolean restart = false;
                            if (ApplicationPreferences.applicationEventLocationEnableScanning)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventMobileCellEnableScanning)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventOrientationEnableScanning)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning)
                                restart = true;
                            if (restart) {
                                //PPApplication.logE("[RJS] ScreenOnOffBroadcastReceiver.onReceive", "restart all scanners for SCREEN_ON");
                                // for screenOn=true -> used only for geofence scanner - start scan with GPS On
                                PPApplication.restartAllScanners(appContext, false);
                            }
                            break;
                        }
                        case Intent.ACTION_SCREEN_OFF: {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
                                PPApplication.logE("[XXX] ScreenOnOffBroadcastReceiver.onReceive", "restartAllScanners");
                            }*/
                            PPApplication.isScreenOn = false;

                            // call this, because device my not be locked immediately after screen off
                            KeyguardManager keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (keyguardManager != null) {
                                boolean secureKeyguard = keyguardManager.isKeyguardSecure();
                                //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "secureKeyguard=" + secureKeyguard);

                                if (secureKeyguard) {
                                    int lockDeviceTime = Settings.Secure.getInt(appContext.getContentResolver(), "lock_screen_lock_after_timeout", 0);
                                    //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "lockDeviceTime="+lockDeviceTime);
                                    if (lockDeviceTime > 0)
                                        LockDeviceAfterScreenOffBroadcastReceiver.setAlarm(lockDeviceTime, appContext);
                                }
                            }

                            //ActivateProfileHelper.removeKeepScreenOnView();

                            //PPApplication.logE("[RJS] ScreenOnOffBroadcastReceiver.onReceive", "restart all scanners for SCREEN_OFF");

                            // for screen off restart scanners only when it is required for any scanner
                            boolean restart = false;
                            if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning &&
                                    ApplicationPreferences.applicationEventBackgroundScanningScanOnlyWhenScreenIsOn)
                                restart = true;
                            if (ApplicationPreferences.applicationEventLocationEnableScanning &&
                                    ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventWifiEnableScanning &&
                                    ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventBluetoothEnableScanning &&
                                    ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventMobileCellEnableScanning &&
                                    ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn)
                                restart = true;
                            else if (ApplicationPreferences.applicationEventOrientationEnableScanning &&
                                    ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)
                                restart = true;
                            if (restart) {
                                // for screenOn=false -> used only for geofence scanner - use last usage of GPS for scan
                                PPApplication.restartAllScanners(appContext, false);
                            }

                            final Handler handler = new Handler(appContext.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
//                                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=ScreenOnOffBroadcastReceiver.onReceive (2)");
                                    //if (PhoneProfilesService.getInstance() != null) {
                                        if (PPApplication.lockDeviceActivity != null) {
                                            try {
                                                PPApplication.lockDeviceActivity.finish();
                                            } catch (Exception e) {
                                                PPApplication.recordException(e);
                                            }
                                        }
                                    //}
                                }
                            });

                            if (!Event.getGlobalEventsRunning()) {
                                //PPApplication.showProfileNotification(/*true*/);
                                if (PhoneProfilesService.getInstance() != null)
                                    PhoneProfilesService.getInstance().showProfileNotification(/*true,*/ false/*, false*/);
                            }
                            break;
                        }
                        case Intent.ACTION_USER_PRESENT:
                            //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");

                            if (Build.VERSION.SDK_INT < 26) {
                                if (ApplicationPreferences.notificationShowInStatusBar &&
                                        ApplicationPreferences.notificationHideInLockScreen) {
                                    //PPApplication.showProfileNotification(/*true*/);
                                    if (PhoneProfilesService.getInstance() != null)
                                        PhoneProfilesService.getInstance().showProfileNotification(/*true,*/ false/*, false*/);
                                }
                            }

                            // change screen timeout
                            final int screenTimeout = ApplicationPreferences.prefActivatedProfileScreenTimeout;
                            //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout=" + screenTimeout);
                            if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                                if (PPApplication.screenTimeoutHandler != null) {
                                    PPApplication.screenTimeoutHandler.post(new Runnable() {
                                        public void run() {
                                            ActivateProfileHelper.setScreenTimeout(screenTimeout, appContext);
                                        }
                                    });
                                }
                            }

                            // enable/disable keyguard
                            try {
                                // start PhoneProfilesService
                                /*Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                                PPApplication.startPPService(appContext, serviceIntent);*/
                                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                                PPApplication.runCommand(appContext, commandIntent);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }

                            /*if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                            return;*/
                            break;
                    }

                    if (Event.getGlobalEventsRunning()) {
                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=ScreenOnOffBroadcastReceiver.onReceive");

//                        PPApplication.logE("[EVENTS_HANDLER] ScreenOnOffBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_SCREEN");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=ScreenOnOffBroadcastReceiver.onReceive");
                    }

                    if (Build.VERSION.SDK_INT < 26) {
                        if (action.equals(Intent.ACTION_SCREEN_ON)) {
                            if (ApplicationPreferences.notificationShowInStatusBar &&
                                    ApplicationPreferences.notificationHideInLockScreen) {
                                //PPApplication.showProfileNotification(/*true*/);
                                if (PhoneProfilesService.getInstance() != null)
                                    PhoneProfilesService.getInstance().showProfileNotification(/*true,*/ false/*, false*/);
                            }
                        }
                    }

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "end of handler post");
                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=ScreenOnOffBroadcastReceiver.onReceive");
                    }*/
                }
                finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
        //PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "after start handler");
    }

}
