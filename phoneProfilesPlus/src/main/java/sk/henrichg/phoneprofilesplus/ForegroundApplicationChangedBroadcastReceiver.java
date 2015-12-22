package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ForegroundApplicationChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "Application";

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### ForegroundApplicationChangedBroadcastReceiver.onReceive", "xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            String packageName = GlobalData.getApplicationInForeground(context);
            GlobalData.logE("@@@ ForegroundApplicationChangedBroadcastReceiver.onReceive","packageName="+packageName);

            /*boolean applicationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
            applicationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION) > 0;
            GlobalData.logE("ForegroundApplicationChangedBroadcastReceiver.onReceive","applicationEventsExists="+applicationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                //eventsServiceIntent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));
                //eventsServiceIntent.putExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, intent.getLongExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, 0));
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }

}
