package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationEventEndBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "NotificationAlarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### NotificationEventEndBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        if (PPApplication.getGlobalEventsRuning(context))
        {
            PPApplication.logE("@@@ NotificationEventEndBroadcastReceiver.onReceive","xxx");

            /*boolean notificationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            notificationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0;
            PPApplication.logE("NotificationEventEndBroadcastReceiver.onReceive","notificationEventsExists="+notificationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            //}

        }

    }

}
