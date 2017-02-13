package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GPSStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "GPSState";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### GPSStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        final String action = intent.getAction();

        if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
            broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_GPS);
            broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, isGpsEnabled);
            context.sendBroadcast(broadcastIntent);

        }
    }

}
