package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PhoneStateService extends WakefulIntentService {

    public PhoneStateService() {
        super("PhoneStateService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### PhoneStateService.doWakefulWork", "xxx");

            Context appContext = getApplicationContext();

            if (!PPApplication.getApplicationStarted(appContext, false))
                // application is not started
                return;

            //PPApplication.loadPreferences(appContext);

            if (Event.getGlobalEventsRuning(appContext))
            {
                PPApplication.logE("@@@ PhoneStateService.doWakefulWork", "-----------");

                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                    // start service
                    Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_PHONE_STATE);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                }
                dataWrapper.invalidateDataWrapper();

            }
        }
    }

}
