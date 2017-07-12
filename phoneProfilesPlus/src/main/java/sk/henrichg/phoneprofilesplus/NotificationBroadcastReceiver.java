package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NotificationBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.notificationBroadcastReceiver);


        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            //PPApplication.logE("@@@ NotificationBroadcastReceiver.onReceive","packageName="+intent.getStringExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));

            /*boolean notificationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            notificationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0;
            PPApplication.logE("NotificationBroadcastReceiver.onReceive","notificationEventsExists="+notificationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, intent.getStringExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, intent.getLongExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, 0));
                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED, intent.getStringExtra(EventsService.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED));
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}
        }
    }

}
