package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ForegroundApplicationChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "Application";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### ForegroundApplicationChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        if (Event.getGlobalEventsRuning(context))
        {
            String packageName = ForegroundApplicationChangedService.getApplicationInForeground(context);
            PPApplication.logE("@@@ ForegroundApplicationChangedBroadcastReceiver.onReceive","packageName="+packageName);

            /*boolean applicationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            applicationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION) > 0;
            PPApplication.logE("ForegroundApplicationChangedBroadcastReceiver.onReceive","applicationEventsExists="+applicationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, intent.getStringExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, intent.getLongExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, 0));
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }

}
