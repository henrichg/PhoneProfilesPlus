package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BatteryService extends WakefulIntentService {

    public BatteryService() {
        super("BatteryService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### BatteryService.doWakefulWork","xxx");

            Context appContext = getApplicationContext();

            //PPApplication.loadPreferences(context);

            boolean isCharging = intent.getBooleanExtra(BatteryBroadcastReceiver.EXTRA_IS_CHARGING, false);
            int batteryPct = intent.getIntExtra(BatteryBroadcastReceiver.EXTRA_BATTERY_PCT, -100);

            PPApplication.logE("BatteryService.doWakefulWork", "batteryPct=" + batteryPct);
            PPApplication.logE("BatteryService.doWakefulWork", "isCharging=" + isCharging);

            boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
            PPApplication.isPowerSaveMode = false;
            if ((!isCharging) &&
                ((ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("1") && (batteryPct <= 5)) ||
                 (ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("2") && (batteryPct <= 15))))
                PPApplication.isPowerSaveMode = true;
            else {
                if (isCharging)
                    PPApplication.isPowerSaveMode = false;
                else
                    PPApplication.isPowerSaveMode = oldPowerSaveMode;
            }

            if (Event.getGlobalEventsRuning(appContext)) {

                if (PhoneProfilesService.instance != null) {
                    if (PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.geofencesScanner.resetLocationUpdates(oldPowerSaveMode, false);
                    PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                    if (PhoneProfilesService.isPhoneStateStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                }

                /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                batteryEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY) > 0;
                dataWrapper.invalidateDataWrapper();

                if (batteryEventsExists)
                {*/
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_BATTERY);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                //}
            }
        }
    }

}
