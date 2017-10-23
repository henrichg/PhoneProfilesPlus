package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NotificationEventEndBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NotificationEventEndBroadcastReceiver.onReceive", "NotificationEventEndBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ NotificationEventEndBroadcastReceiver.onReceive","xxx");

            /*boolean notificationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            notificationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0;
            PPApplication.logE("NotificationEventEndBroadcastReceiver.onReceive","notificationEventsExists="+notificationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start job
                EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_NOTIFICATION_EVENT_END);
            //}

        }

    }

}
