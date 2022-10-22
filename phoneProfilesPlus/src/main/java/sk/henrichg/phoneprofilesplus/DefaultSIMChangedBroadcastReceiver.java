package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DefaultSIMChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] DefaultSIMChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

//        final Intent _intent = intent;

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_RADIO_SWITCH, "SENSOR_TYPE_RADIO_SWITCH", 0);
        /*
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        __handler.post(() -> {
//          PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DefaultSIMChangedBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DefaultSIMChangedBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (Event.getGlobalEventsRunning()) {
                        if (PhoneProfilesService.getInstance() != null) {

                            // start events handler

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] DefaultSIMChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                        }
                    }


                } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        });
        */
    }

}
