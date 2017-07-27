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

            if (Event.getGlobalEventsRuning(appContext))
            {
                if (PhoneProfilesService.instance != null) {
                    if (PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.geofencesScanner.resetLocationUpdates(oldPowerSaveMode, false);
                    PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                    if (PhoneProfilesService.isPhoneStateStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                }

                //if (!powerSaveMode)
                //{
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_POWER_SAVE_MODE);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                //}
            }
        }
    }

}
