package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.PowerManager;

public class LocationModeChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] LocationModeChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context))
        {
            final String action = intent.getAction();
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"LocationModeChangedBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=LocationModeChangedBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_LocationModeChangedBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if ((action != null) && action.matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {

//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] LocationModeChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                        }

                        /*if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isLocationScannerStarted()) {
                            PhoneProfilesService.getInstance().getGeofencesScanner().clearAllEventGeofences();
                        }*/

                        synchronized (PPApplication.locationScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && (PPApplication.locationScanner != null)) {
                                String provider = PPApplication.locationScanner.getProvider(false);
                                PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                            }
                        }

                        GlobalUtils.sleep(10000);

//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] LocationScanner.LocationCallback", "sensorType=SENSOR_TYPE_LOCATION_MODE");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_LOCATION_MODE);

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
            }; //);
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }

    }

}
