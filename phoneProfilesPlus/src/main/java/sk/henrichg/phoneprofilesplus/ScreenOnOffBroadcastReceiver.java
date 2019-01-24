package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "ScreenOnOffBroadcastReceiver.onReceive", "ScreenOnOffBroadcastReceiver_onReceive");

        if (intent != null)
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
        else
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //ScreenOnOffJob.start(appContext, intent.getAction());

        final String action = intent.getAction();

        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "before start handler");
        PPApplication.startHandlerThread("ScreenOnOffBroadcastReceiver.onReceive");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ScreenOnOffBroadcastReceiver.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "start of handler post");

                    if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen on");
                        PPApplication.logE("[XXX] ScreenOnOffBroadcastReceiver.onReceive", "restartAllScanners");
                        PPApplication.restartAllScanners(appContext, true);
                    } else if ((action != null) && action.equals(Intent.ACTION_SCREEN_OFF)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen off");
                        PPApplication.logE("[XXX] ScreenOnOffBroadcastReceiver.onReceive", "restartAllScanners");
                        PPApplication.restartAllScanners(appContext, true);

                        final Handler handler = new Handler(appContext.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (PhoneProfilesService.getInstance() != null) {
                                    if (PhoneProfilesService.getInstance().lockDeviceActivity != null) {
                                        PhoneProfilesService.getInstance().lockDeviceActivity.finish();
                                    }
                                }
                            }
                        });

                        if (!Event.getGlobalEventsRunning(appContext)) {
                            PPApplication.showProfileNotification(/*appContext*/);
                        }
                    } else if ((action != null) && action.equals(Intent.ACTION_USER_PRESENT)) {
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screen unlock");

                        if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                                ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                            PPApplication.showProfileNotification(/*appContext*/);
                        }

                        // change screen timeout
                        final int screenTimeout = ActivateProfileHelper.getActivatedProfileScreenTimeout(appContext);
                        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "screenTimeout=" + screenTimeout);
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
                            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_SWITCH_KEYGUARD, true);
                            PPApplication.startPPService(appContext, serviceIntent);
                        } catch (Exception ignored) {
                        }

                    /*if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                    return;*/
                    }

                    if (Event.getGlobalEventsRunning(appContext)) {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SCREEN);
                    }

                    if ((action != null) && action.equals(Intent.ACTION_SCREEN_ON)) {
                        if (ApplicationPreferences.notificationShowInStatusBar(appContext) &&
                                ApplicationPreferences.notificationHideInLockScreen(appContext)) {
                            PPApplication.showProfileNotification(/*appContext*/);
                        }
                    }

                    PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "end of handler post");
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
        PPApplication.logE("@@@ ScreenOnOffBroadcastReceiver.onReceive", "after start handler");
    }

}
