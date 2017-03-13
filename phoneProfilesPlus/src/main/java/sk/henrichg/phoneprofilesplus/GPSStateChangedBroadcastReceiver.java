package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GPSStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### GPSStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        final String action = intent.getAction();

        if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
            broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_GPS);
            broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, isGpsEnabled);
            context.sendBroadcast(broadcastIntent);

        }
    }

}
