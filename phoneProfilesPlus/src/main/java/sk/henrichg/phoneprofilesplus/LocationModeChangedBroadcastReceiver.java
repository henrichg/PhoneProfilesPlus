package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class LocationModeChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### LocationModeChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "LocationModeChangedBroadcastReceiver.onReceive", "LocationModeChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ LocationModeChangedBroadcastReceiver.onReceive", "xxx");

            final String action = intent.getAction();
            PhoneProfilesService.startHandlerThread();
            final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationModeChangedBroadcastReceiver.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if ((action != null) && action.matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                        //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                         EventsHandler eventsHandler = new EventsHandler(appContext);
                         eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false);
                    }

                    synchronized (PPApplication.geofenceScannerMutex) {
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
                            PhoneProfilesService.getGeofencesScanner().clearAllEventGeofences();
                            PhoneProfilesService.getGeofencesScanner().updateTransitionsByLastKnownLocation(true);
                        }
                    }

                    if ((wakeLock != null) && wakeLock.isHeld())
                        wakeLock.release();
                }
            });
        }

    }

}
