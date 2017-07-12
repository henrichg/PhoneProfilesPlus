package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class GeofenceScannerBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "geofenceScanner";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### GeofenceScannerBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.geofenceScannerBroadcastReceiver);

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ GeofenceScannerBroadcastReceiver.onReceive", "xxx");

            if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted())
                PhoneProfilesService.geofencesScanner.updateGeofencesInDB();

            // start service
            Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
        }

    }

}
