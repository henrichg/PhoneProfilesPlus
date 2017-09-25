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
        CallsCounter.logCounter(getApplicationContext(), "PhoneStateService.doWakefulWork", "PhoneStateService_doWakefulWork");

        if (intent != null) {
            PPApplication.logE("##### PhoneStateService.doWakefulWork", "xxx");

            Context appContext = getApplicationContext();

            if (!PPApplication.getApplicationStarted(appContext, false))
                // application is not started
                return;

            if (Event.getGlobalEventsRunning(appContext))
            {
                PPApplication.logE("@@@ PhoneStateService.doWakefulWork", "-----------");

                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE, false);
                }
                dataWrapper.invalidateDataWrapper();

            }
        }
    }

}
