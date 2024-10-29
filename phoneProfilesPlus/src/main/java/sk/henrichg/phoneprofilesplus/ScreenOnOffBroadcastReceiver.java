package sk.henrichg.phoneprofilesplus;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/** @noinspection ExtractMethodRecommender*/
public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent != null)
//            PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
//        else
//            PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive", "xxx");

        /*if (intent != null)
        else
            return;*/
        if (intent == null)
            return;

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final String action = intent.getAction();
        if (action == null)
            return;

        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ScreenOnOffBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();

            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_ScreenOnOffBroadcastReceiver_onReceive);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    switch (action) {
                        case Intent.ACTION_SCREEN_ON: {
                            PPApplication.isScreenOn = true;

                            if (!ApplicationPreferences.prefLockScreenDisabled) {
                                // enable/disable keyguard
                                try {
                                    //PhoneProfilesService ppService = PhoneProfilesService.getInstance();
                                    //if (ppService != null) {
                                        GlobalUtils.switchKeyguard(appContext);
                                    //}
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }

                            // reset brightness
                            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                                if (ApplicationPreferences.applicationForceSetBrightnessAtScreenOn) {
                                    final Profile profile = DatabaseHandler.getInstance(appContext).getActivatedProfile();
                                    if (profile != null) {
                                        if (profile.getDeviceBrightnessChange()) {
                                            if (Permissions.checkProfileScreenBrightness(appContext, profile, null)) {
                                                //noinspection UnreachableCode
                                                try {
//                                                    if (PPApplicationStatic.logEnabled()) {
//                                                        int brightnessMode = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//                                                        int brightness = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//                                                        float adaptiveBrightness = Settings.System.getFloat(appContext.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "brightness mode=" + brightnessMode);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "manual brightness value=" + brightness);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "adaptive brightness value=" + adaptiveBrightness);
//                                                    }
//
//                                                    if (PPApplicationStatic.logEnabled()) {
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (2)", "brightness mode=" + profile.getDeviceBrightnessAutomatic());
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (2)", "manual brightness value=" + profile.getDeviceBrightnessManualValue(appContext));
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (2)", "adaptive brightness value=" + profile.getDeviceBrightnessAdaptiveValue(appContext));
//                                                    }
                                                    try {
                                                        PPApplication.brightnessInternalChange = true;
                                                        if (profile.getDeviceBrightnessAutomatic()) {
                                                            if (Settings.System.getInt(appContext.getContentResolver(),
                                                                    Settings.System.SCREEN_BRIGHTNESS_MODE, -1) != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                                                                Settings.System.putInt(appContext.getContentResolver(),
                                                                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                                                            if (profile.getDeviceBrightnessChangeLevel()) {
                                                                int newBrightness = profile.getDeviceBrightnessManualValue(appContext);
                                                                if (Settings.System.getInt(appContext.getContentResolver(),
                                                                        Settings.System.SCREEN_BRIGHTNESS, -1) != newBrightness)
                                                                    Settings.System.putInt(appContext.getContentResolver(),
                                                                            Settings.System.SCREEN_BRIGHTNESS, newBrightness);
                                                            }
                                                        } else {
                                                            if (Settings.System.getInt(appContext.getContentResolver(),
                                                                    Settings.System.SCREEN_BRIGHTNESS_MODE, -1) !=
                                                                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                                                                Settings.System.putInt(appContext.getContentResolver(),
                                                                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                                            if (profile.getDeviceBrightnessChangeLevel()) {
                                                                int newBrightness = profile.getDeviceBrightnessManualValue(appContext);
                                                                if (Settings.System.getInt(appContext.getContentResolver(),
                                                                        Settings.System.SCREEN_BRIGHTNESS, -1) != newBrightness)
                                                                    Settings.System.putInt(appContext.getContentResolver(),
                                                                            Settings.System.SCREEN_BRIGHTNESS, newBrightness);
                                                            }
                                                        }
                                                        PPExecutors.scheduleDisableBrightnessInternalChangeExecutor();
                                                    } catch (Exception ignored) {
                                                    }
//                                                    if (PPApplicationStatic.logEnabled()) {
//                                                        int brightnessMode = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//                                                        int brightness = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//                                                        float adaptiveBrightness = Settings.System.getFloat(appContext.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (3)", "brightness mode=" + brightnessMode);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (3)", "manual brightness value=" + brightness);
//                                                        PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (3)", "adaptive brightness value=" + adaptiveBrightness);
//                                                    }
                                                } catch (Exception e) {
                                                    PPApplicationStatic.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // is not needed to call event hander, is called from doScreenOnOff()
                            //BluetoothConnectedDevicesDetector.getConnectedDevices(appContext, false);

                            break;
                        }
                        case Intent.ACTION_SCREEN_OFF: {
                            PPApplication.isScreenOn = false;
//                            PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive", "isScreenOn="+PPApplication.isScreenOn);

//                            if (PPApplicationStatic.logEnabled()) {
//                                PPApplication.brightnessModeBeforeScreenOff = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//                                PPApplication.brightnessBeforeScreenOff = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//                                PPApplication.adaptiveBrightnessBeforeScreenOff = Settings.System.getFloat(appContext.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
//                                PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "brightness mode=" + PPApplication.brightnessModeBeforeScreenOff);
//                                PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "manual brightness value=" + PPApplication.brightnessBeforeScreenOff);
//                                PPApplicationStatic.logE("[IN_BROADCAST] ScreenOnOffBroadcastReceiver.onReceive (1)", "adaptive brightness value=" + PPApplication.adaptiveBrightnessBeforeScreenOff);
//                            }

                            // call this, because device may not be locked immediately after screen off
                            KeyguardManager keyguardManager = (KeyguardManager) appContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (keyguardManager != null) {
                                boolean keyguardShowing = keyguardManager.isKeyguardLocked();

                                if (!keyguardShowing) {
                                    int lockDeviceTime = Settings.Secure.getInt(appContext.getContentResolver(), "lock_screen_lock_after_timeout", 0);
                                    if (lockDeviceTime > 0) {
                                        if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_SCREEN) > 0) {
                                            // call this only when any event with screen sensor exists
                                            // it is not needed to call it again for calendar sensor, because was called for screen off
                                            LockDeviceAfterScreenOffBroadcastReceiver.setAlarm(lockDeviceTime, appContext);
                                        }
                                    }
                                }
                            }

                            if (PPApplication.lockDeviceActivityDisplayed) {
//                                PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] ScreenOnOffBroadcastReceiver.onReceive", "xxx");
                                Intent finishIntent = new Intent(LockDeviceActivity.ACTION_FINISH_LOCK_DEVICE_ACTIVITY_BROADCAST_RECEIVER);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(finishIntent);
                            }

                            break;
                        }
                        case Intent.ACTION_USER_PRESENT: {
                            PPApplication.isScreenOn = true;

                            if (ApplicationPreferences.prefLockScreenDisabled) {
                                // enable/disable keyguard
                                try {
                                    //PhoneProfilesService ppService = PhoneProfilesService.getInstance();
                                    //if (ppService != null) {
                                    GlobalUtils.switchKeyguard(appContext);
                                    //}
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }

                            break;
                        }
                    }

                    doScreenOnOff(action, appContext);

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception e) {
//                            Log.e("@@@ ScreenOnOffBroadcastReceiver.onReceive", Log.getStackTraceString(e));
                        }
                    }
                }
            //}

//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "END run - from=ScreenOnOffBroadcastReceiver.onReceive");

        };
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

    private void setProfileScreenTimeoutSavedWhenScreenOff(final Context appContext) {
        final int screenTimeout = ApplicationPreferences.prefActivatedProfileScreenTimeoutWhenScreenOff;
        if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
            if (PPApplication.screenTimeoutHandler != null) {
                PPApplication.screenTimeoutHandler.post(() -> ActivateProfileHelper.setScreenTimeout(screenTimeout, false, appContext));
            }
        }
    }

    private void doScreenOnOff(String action, Context appContext) {
        switch (action) {
            case Intent.ACTION_SCREEN_ON: {
                // change screen timeout
                // WARNING: must be called after PPApplication.isScreenOn = true;
                setProfileScreenTimeoutSavedWhenScreenOff(appContext);

                // restart scanners for screen on when any is enabled
                boolean restart = false;
                if (ApplicationPreferences.applicationEventLocationEnableScanning)
                    restart = true;
                else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                    restart = true;
                else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                    restart = true;
                else if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                                PPApplicationStatic.logE("[TEST BATTERY] ScreenOnOffBroadcastReceiver.onReceive", "******** ### ******* (1)");
                    restart = true;
                }
                else if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                                PPApplicationStatic.logE("[TEST BATTERY] ScreenOnOffBroadcastReceiver.onReceive", "******** ### ******* (1)");
                    restart = true;
                }
                else if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
                    restart = true;
                if (restart) {
                    // for screenOn=true -> used only for Location scanner - start scan with GPS On
//                    PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] ScreenOnOffBroadcastReceiver.onReceive", "ACTION_SCREEN_ON");
                    PPApplicationStatic.restartAllScanners(appContext, false);
                }

//                PPApplicationStatic.logE("[PPP_NOTIFICATION] ScreenOnOffBroadcastReceiver.onReceive", "call of PPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(false, appContext);
                PPAppNotification.drawNotification(false, appContext);

                if (EventStatic.getGlobalEventsRunning(appContext)) {
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ScreenOnOffBroadcastReceiver.doScreenOnOff", "SENSOR_TYPE_SCREEN,SENSOR_TYPE_BRIGHTNESS,SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(new int[]{
                            EventsHandler.SENSOR_TYPE_SCREEN,
                            EventsHandler.SENSOR_TYPE_BRIGHTNESS,
                            EventsHandler.SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK});
                }

                break;
            }
            case Intent.ACTION_SCREEN_OFF: {
                // for screen off restart scanners only when it is required for any scanner
                boolean restart = false;
                if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning &&
                        ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn)
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
                        ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
//                                PPApplicationStatic.logE("[TEST BATTERY] ScreenOnOffBroadcastReceiver.onReceive", "******** ### ******* (2)");
                    restart = true;
                }
                else if (ApplicationPreferences.applicationEventOrientationEnableScanning &&
                        ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
//                                PPApplicationStatic.logE("[TEST BATTERY] ScreenOnOffBroadcastReceiver.onReceive", "******** ### ******* (2)");
                    restart = true;
                }
                if (restart) {
                    // for screenOn=false -> used only for Location scanner - use last usage of GPS for scan
                    //PPApplication.setBlockProfileEventActions(true);
//                    PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] ScreenOnOffBroadcastReceiver.onReceive", "ACTION_SCREEN_OFF");
                    PPApplicationStatic.restartAllScanners(appContext, false);
                }

                // Is needed this for "Always on display" to change these notifications in it. Hm.
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] ScreenOnOffBroadcastReceiver.onReceive", "call of PPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(false, appContext);
                PPAppNotification.drawNotification(false, appContext);

                if (EventStatic.getGlobalEventsRunning(appContext)) {
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ScreenOnOffBroadcastReceiver.doScreenOnOff", "SENSOR_TYPE_SCREEN");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_SCREEN});
                    // do not call this when screen is off
                    //handleEventsForBrightnessSensor();
                }

                break;
            }
            case Intent.ACTION_USER_PRESENT: {
                // change screen timeout
                // WARNING: must be called after PPApplication.isScreenOn = true;
                setProfileScreenTimeoutSavedWhenScreenOff(appContext);

                if (EventStatic.getGlobalEventsRunning(appContext)) {
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ScreenOnOffBroadcastReceiver.doScreenOnOff", "SENSOR_TYPE_SCREEN,SENSOR_TYPE_BRIGHTNESS");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(new int[]{
                            EventsHandler.SENSOR_TYPE_SCREEN,
                            EventsHandler.SENSOR_TYPE_BRIGHTNESS});
                }

                break;
            }
        }
    }

}
