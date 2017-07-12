package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class ForegroundApplicationChangedBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "Application";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ForegroundApplicationChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.foregroundApplicationChangedBroadcastReceiver);

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            String packageName = ForegroundApplicationChangedService.getApplicationInForeground(appContext);
            PPApplication.logE("@@@ ForegroundApplicationChangedBroadcastReceiver.onReceive","packageName="+packageName);

            /*boolean applicationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            applicationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_APPLICATION) > 0;
            PPApplication.logE("ForegroundApplicationChangedBroadcastReceiver.onReceive","applicationEventsExists="+applicationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME, intent.getStringExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME));
                //eventsServiceIntent.putExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, intent.getLongExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, 0));
            WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}
        }
    }

}
