package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PowerSaveModeService extends WakefulIntentService {

    public PowerSaveModeService() {
        super("PowerSaveModeService");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "PowerSaveModeService.doWakefulWork", "PowerSaveModeService_doWakefulWork");

        if (intent != null) {
            PPApplication.logE("##### PowerSaveModeService.doWakefulWork", "xxx");

            Context appContext = getApplicationContext();

            boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
            PPApplication.isPowerSaveMode = false;
            if (ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("3")) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PPApplication.isPowerSaveMode = powerManager.isPowerSaveMode();
            }
            else
                PPApplication.isPowerSaveMode = oldPowerSaveMode;

            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            getApplicationContext().startService(serviceIntent);
            //else
            //    context.startForegroundService(serviceIntent);

            if (Event.getGlobalEventsRunning(appContext))
            {
                /*
                if (PhoneProfilesService.instance != null) {
                    if (PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(oldPowerSaveMode, false);
                    PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                    if (PhoneProfilesService.isPhoneStateScannerStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                }
                */

                //if (!powerSaveMode)
                //{
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE, false);
                //}
            }
        }
    }

}
