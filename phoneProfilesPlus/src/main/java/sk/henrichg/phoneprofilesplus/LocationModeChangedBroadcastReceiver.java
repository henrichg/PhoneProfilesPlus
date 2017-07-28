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

                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_GPS);
                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, isGpsEnabled);
                WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);

            }

            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
                PhoneProfilesService.geofencesScanner.clearAllEventGeofences();

                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_LOCATION_MODE);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);

            }
        }

    }

}
