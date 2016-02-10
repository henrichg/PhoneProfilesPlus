package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class LocationModeChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "LocationModeChangedBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### LocationModeChangedBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ LocationModeChangedBroadcastReceiver.onReceive", "xxx");

            if (GlobalData.geofencesScanner != null) {
                GlobalData.geofencesScanner.unregisterAllEventGeofences();
                GlobalData.geofencesScanner.registerAllEventGeofences();
            }
        }

    }

}
