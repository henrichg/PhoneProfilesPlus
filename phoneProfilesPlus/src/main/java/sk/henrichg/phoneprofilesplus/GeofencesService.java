package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class GeofencesService extends WakefulIntentService {

    public GeofencesService() {
        super("GeofencesService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "GeofencesService.doWakefulWork", "GeofencesService_doWakefulWork");

        if (intent != null) {
            PPApplication.logE("##### GeofencesService.doWakefulWork", "xxx");

            Context appContext = getApplicationContext();

            if (!PPApplication.getApplicationStarted(appContext, true))
                // application is not started
                return;

            if (Event.getGlobalEventsRunning(appContext))
            {
                PPApplication.logE("@@@ GeofencesService.doWakefulWork", "xxx");

                if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted())
                    PhoneProfilesService.geofencesScanner.updateGeofencesInDB();

                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER, false);
            }
        }
    }

}
