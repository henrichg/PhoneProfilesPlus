package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class RestartEventsBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "restartEvents";
    //public static final String INTENT_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.RESTART_EVENTS";

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### RestartEventsBroadcastReceiver.onReceive", "xxx");

        GlobalData.logE("@@@ RestartEventsBroadcastReceiver.onReceive","####");

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            boolean unblockEventsRun = intent.getBooleanExtra(GlobalData.EXTRA_UNBLOCKEVENTSRUN, false);
            boolean interactive = intent.getBooleanExtra(GlobalData.EXTRA_INTERACTIVE, false);

            if (GlobalData.getEventsBlocked(context) && (!unblockEventsRun))
                return;

            GlobalData.logE("$$$ restartEvents","in RestartEventsBroadcastReceiver, unblockEventsRun="+unblockEventsRun);

            /*if (unblockEventsRun)
            {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                GlobalData.setActivatedProfileForDuration(context, 0);

                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                GlobalData.setEventsBlocked(context, false);
                dataWrapper.getDatabaseHandler().unblockAllEvents();
                GlobalData.setForceRunEventRunning(context, false);

                dataWrapper.invalidateDataWrapper();
            }*/

            // start service
            Intent eventsServiceIntent = new Intent(context, EventsService.class);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_UNBLOCKEVENTSRUN, unblockEventsRun);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_INTERACTIVE, interactive);
            startWakefulService(context, eventsServiceIntent);
        }
    }
}
