package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class RestartEventsBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "restartEvents";
    //public static final String INTENT_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.RESTART_EVENTS";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### RestartEventsBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(PPApplication.restartEventsBroadcastReceiver);

        PPApplication.logE("@@@ RestartEventsBroadcastReceiver.onReceive","####");

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            boolean unblockEventsRun = intent.getBooleanExtra(DataWrapper.EXTRA_UNBLOCKEVENTSRUN, false);
            boolean interactive = intent.getBooleanExtra(DataWrapper.EXTRA_INTERACTIVE, false);

            if (Event.getEventsBlocked(appContext) && (!unblockEventsRun))
                return;

            PPApplication.logE("$$$ restartEvents","in RestartEventsBroadcastReceiver, unblockEventsRun="+unblockEventsRun);

            /*if (unblockEventsRun)
            {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                PPApplication.setActivatedProfileForDuration(appContext, 0);

                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

                PPApplication.setEventsBlocked(appContext, false);
                dataWrapper.getDatabaseHandler().unblockAllEvents();
                PPApplication.setForceRunEventRunning(appContext, false);

                dataWrapper.invalidateDataWrapper();
            }*/

            // start service
            Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            eventsServiceIntent.putExtra(DataWrapper.EXTRA_UNBLOCKEVENTSRUN, unblockEventsRun);
            eventsServiceIntent.putExtra(DataWrapper.EXTRA_INTERACTIVE, interactive);
            WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
        }
    }
}
