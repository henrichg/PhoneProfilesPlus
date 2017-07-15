package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.BatteryManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BatteryService extends WakefulIntentService {

    private static boolean isCharging = false;
    private static int batteryPct = -100;

    public BatteryService() {
        super("BatteryService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### BatteryService.doWakefulWork","xxx");

            Context appContext = getApplicationContext();

            //PPApplication.loadPreferences(context);

            //boolean batteryEventsExists = false;

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            PPApplication.logE("BatteryService.doWakefulWork", "status=" + status);

            if (status != -1) {
                boolean _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                PPApplication.logE("BatteryService.doWakefulWork", "isCharging=" + isCharging);

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int pct = Math.round(level / (float) scale * 100);


                if ((isCharging != _isCharging) || (batteryPct != pct)) {
                    PPApplication.logE("BatteryService.doWakefulWork", "state changed");
                    PPApplication.logE("BatteryService.doWakefulWork", "batteryPct=" + pct);
                    PPApplication.logE("BatteryService.doWakefulWork", "level=" + level);
                    PPApplication.logE("BatteryService.doWakefulWork", "isCharging=" + isCharging);
                    PPApplication.logE("BatteryService.doWakefulWork", "_isCharging=" + _isCharging);

                    isCharging = _isCharging;
                    batteryPct = pct;

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
    }

}
