package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### NotificationBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context, true))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            //GlobalData.logE("@@@ NotificationBroadcastReceiver.onReceive","packageName="+intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));

            /*boolean notificationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            notificationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0;
            GlobalData.logE("NotificationBroadcastReceiver.onReceive","notificationEventsExists="+notificationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                //eventsServiceIntent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));
                //eventsServiceIntent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, intent.getLongExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, 0));
                eventsServiceIntent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED, intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED));
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }

}
