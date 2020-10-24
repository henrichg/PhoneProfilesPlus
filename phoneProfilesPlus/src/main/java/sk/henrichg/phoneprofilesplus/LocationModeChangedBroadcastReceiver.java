package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;

public class LocationModeChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] LocationModeChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "LocationModeChangedBroadcastReceiver.onReceive", "LocationModeChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning())
        {
            //PPApplication.logE("@@@ LocationModeChangedBroadcastReceiver.onReceive", "xxx");

            final String action = intent.getAction();
            PPApplication.startHandlerThreadBroadcast(/*"LocationModeChangedBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LocationModeChangedBroadcastReceiver.onReceive");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LocationModeChangedBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if ((action != null) && action.matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=LocationModeChangedBroadcastReceiver.onReceive");

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] LocationModeChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=LocationModeChangedBroadcastReceiver.onReceive");
                        }

                        /*if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                            PhoneProfilesService.getInstance().getGeofencesScanner().clearAllEventGeofences();
                            //PPApplication.logE("LocationModeChangedBroadcastReceiver.onReceive", "updateTransitionsByLastKnownLocation");
                        }*/

                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                            PhoneProfilesService.getInstance().getGeofencesScanner().updateTransitionsByLastKnownLocation();

                        PPApplication.sleep(10000);

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] GeofenceScanner.LocationCallback", "sensorType=SENSOR_TYPE_LOCATION_MODE");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_LOCATION_MODE);

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
                }
            });
        }

    }

}
