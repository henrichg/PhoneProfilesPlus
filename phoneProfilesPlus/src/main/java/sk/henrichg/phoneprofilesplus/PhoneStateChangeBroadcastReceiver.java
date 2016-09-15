package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class PhoneStateChangeBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "phoneStateChange";

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### PhoneStateChangeBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            GlobalData.logE("@@@ PhoneStateChangeBroadcastReceiver.onReceive", "-----------");

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            }
            dataWrapper.invalidateDataWrapper();

        }

    }

}
