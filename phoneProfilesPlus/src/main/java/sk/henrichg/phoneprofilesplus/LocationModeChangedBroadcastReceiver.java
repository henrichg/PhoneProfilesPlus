package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class LocationModeChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### LocationModeChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "LocationModeChangedBroadcastReceiver.onReceive", "LocationModeChangedBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ LocationModeChangedBroadcastReceiver.onReceive", "xxx");

            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                EventsHandlerJob.startForSensor(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
            }

            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
                PhoneProfilesService.getGeofencesScanner().clearAllEventGeofences();
                PhoneProfilesService.getGeofencesScanner().updateTransitionsByLastKnownLocation();

                // start job
                EventsHandlerJob.startForSensor(EventsHandler.SENSOR_TYPE_LOCATION_MODE);
            }
        }

    }

}
