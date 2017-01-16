package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BatteryEventBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "batteryEvent";
    private static boolean isCharging = false;
    private static int batteryPct = -100;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### BatteryEventBroadcastReceiver.onReceive","xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        //boolean batteryEventsExists = false;

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "status=" + status);

        if (status != -1) {
            boolean _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "isCharging=" + isCharging);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int pct = Math.round(level / (float) scale * 100);


            if ((isCharging != _isCharging) || (batteryPct != pct)) {
                PPApplication.logE("@@@ BatteryEventBroadcastReceiver.onReceive", "xxx");

                PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "state changed");
                PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "batteryPct=" + pct);
                PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "level=" + level);
                PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "isCharging=" + isCharging);
                PPApplication.logE("BatteryEventBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);

                isCharging = _isCharging;
                batteryPct = pct;

                boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
                PPApplication.isPowerSaveMode = false;
                if ((!isCharging) &&
                    ((PPApplication.applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5)) ||
                     (PPApplication.applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15))))
                    PPApplication.isPowerSaveMode = true;
                else {
                    if (isCharging)
                        PPApplication.isPowerSaveMode = false;
                    else
                        PPApplication.isPowerSaveMode = oldPowerSaveMode;
                }

                if (PPApplication.getGlobalEventsRuning(context)) {

                    if (PhoneProfilesService.instance != null) {
                        if (PhoneProfilesService.isGeofenceScannerStarted())
                            PhoneProfilesService.geofencesScanner.resetLocationUpdates(oldPowerSaveMode, false);
                        PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                        if (PhoneProfilesService.isPhoneStateStarted())
                            PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                    }

                    /*DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                    batteryEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY) > 0;
                    dataWrapper.invalidateDataWrapper();

                    if (batteryEventsExists)
                    {*/
                    // start service
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                    //}
                }
            }
        }
    }
}
