package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BatteryService extends WakefulIntentService {

    public BatteryService() {
        super("BatteryService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "BatteryService.doWakefulWork", "BatteryService_doWakefulWork");

        if (intent != null) {
            PPApplication.logE("##### BatteryService.doWakefulWork","xxx");

            Context appContext = getApplicationContext();

            boolean statusReceived = intent.getBooleanExtra(BatteryBroadcastReceiver.EXTRA_STATUS_RECEIVED, false);
            boolean levelReceived = intent.getBooleanExtra(BatteryBroadcastReceiver.EXTRA_LEVEL_RECEIVED, false);
            boolean isCharging;
            int batteryPct;
            isCharging = intent.getBooleanExtra(BatteryBroadcastReceiver.EXTRA_IS_CHARGING, false);
            batteryPct = intent.getIntExtra(BatteryBroadcastReceiver.EXTRA_BATTERY_PCT, -100);
            if (!(statusReceived && levelReceived)) {
                // get battery status
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = registerReceiver(null, filter);

                if (batteryStatus != null) {
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    batteryPct = Math.round(level / (float) scale * 100);
                }
            }

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

            if (Event.getGlobalEventsRunning(appContext)) {

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
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BATTERY, false);
                //}
            }
        }
    }

}
