package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GeofenceScannerBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "geofenceScanner";

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### GeofenceScannerBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ GeofenceScannerBroadcastReceiver.onReceive", "xxx");

            if ((GlobalData.phoneProfilesService != null) && PhoneProfilesService.isGeofenceScannerStarted())
                GlobalData.phoneProfilesService.geofencesScanner.updateGeofencesInDB();

            // start service
            Intent eventsServiceIntent = new Intent(context, EventsService.class);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(context, eventsServiceIntent);
        }

    }

}
