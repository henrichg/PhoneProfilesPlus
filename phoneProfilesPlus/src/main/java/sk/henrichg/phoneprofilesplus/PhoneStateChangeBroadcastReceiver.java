package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class PhoneStateChangeBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "phoneStateChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### PhoneStateChangeBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, false))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ PhoneStateChangeBroadcastReceiver.onReceive", "-----------");

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(appContext, eventsServiceIntent);
            }
            dataWrapper.invalidateDataWrapper();

        }

    }

}
