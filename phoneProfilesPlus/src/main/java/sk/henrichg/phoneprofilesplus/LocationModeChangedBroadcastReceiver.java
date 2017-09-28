package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

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
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                //boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                try {
                    Intent eventsServiceIntent = new Intent(context, EventsHandlerService.class);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_GPS);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_EVENT_RADIO_SWITCH_STATE, isGpsEnabled);
                    WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);
                } catch (Exception ignored) {}

            }

            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
                PhoneProfilesService.getGeofencesScanner().clearAllEventGeofences();

                // start service
                try {
                    Intent eventsServiceIntent = new Intent(appContext, EventsHandlerService.class);
                    eventsServiceIntent.putExtra(EventsHandlerService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_LOCATION_MODE);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                } catch (Exception ignored) {}

            }
        }

    }

}
