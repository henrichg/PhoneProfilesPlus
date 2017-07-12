package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PhoneStateChangeBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "phoneStateChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### PhoneStateChangeBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.phoneStateChangeBroadcastReceiver);

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
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            }
            dataWrapper.invalidateDataWrapper();

        }

    }

}
