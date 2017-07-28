package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class NFCEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NFCEventEndBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ NFCEventEndBroadcastReceiver.onReceive","xxx");

            /*boolean smsEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            smsEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SMS) > 0;
            PPApplication.logE("SMSEventEndBroadcastReceiver.onReceive","smsEventsExists="+smsEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (smsEventsExists)
            {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_NFC_EVENT_END);
            WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}

        }

    }

}
