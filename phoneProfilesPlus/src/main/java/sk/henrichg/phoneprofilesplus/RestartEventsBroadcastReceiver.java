package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class RestartEventsBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "restartEvents";
    //public static final String INTENT_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.RESTART_EVENTS";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### RestartEventsBroadcastReceiver.onReceive", "xxx");

        PPApplication.logE("@@@ RestartEventsBroadcastReceiver.onReceive","####");

        PPApplication.loadPreferences(context);

        if (Event.getGlobalEventsRuning(context))
        {
            boolean unblockEventsRun = intent.getBooleanExtra(DataWrapper.EXTRA_UNBLOCKEVENTSRUN, false);
            boolean interactive = intent.getBooleanExtra(DataWrapper.EXTRA_INTERACTIVE, false);

            if (Event.getEventsBlocked(context) && (!unblockEventsRun))
                return;

            PPApplication.logE("$$$ restartEvents","in RestartEventsBroadcastReceiver, unblockEventsRun="+unblockEventsRun);

            /*if (unblockEventsRun)
            {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                PPApplication.setActivatedProfileForDuration(context, 0);

                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                PPApplication.setEventsBlocked(context, false);
                dataWrapper.getDatabaseHandler().unblockAllEvents();
                PPApplication.setForceRunEventRunning(context, false);

                dataWrapper.invalidateDataWrapper();
            }*/

            // start service
            Intent eventsServiceIntent = new Intent(context, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            eventsServiceIntent.putExtra(DataWrapper.EXTRA_UNBLOCKEVENTSRUN, unblockEventsRun);
            eventsServiceIntent.putExtra(DataWrapper.EXTRA_INTERACTIVE, interactive);
            startWakefulService(context, eventsServiceIntent);
        }
    }
}
