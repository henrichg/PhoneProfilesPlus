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
        GlobalData.logE("##### BatteryEventBroadcastReceiver.onReceive","xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        //boolean batteryEventsExists = false;

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "status=" + status);

        if (status != -1) {
            boolean _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "isCharging=" + isCharging);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int pct = Math.round(level / (float) scale * 100);


            if ((isCharging != _isCharging) || (batteryPct != pct)) {
                GlobalData.logE("@@@ BatteryEventBroadcastReceiver.onReceive", "xxx");

                GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "state changed");
                GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "batteryPct=" + pct);
                GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "level=" + level);
                GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "isCharging=" + isCharging);
                GlobalData.logE("BatteryEventBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);

                isCharging = _isCharging;
                batteryPct = pct;

                boolean oldPowerSaveMode = GlobalData.isPowerSaveMode;
                GlobalData.isPowerSaveMode = false;
                if ((!isCharging) &&
                    ((GlobalData.applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5)) ||
                     (GlobalData.applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15))))
                    GlobalData.isPowerSaveMode = true;
                else {
                    if (isCharging)
                        GlobalData.isPowerSaveMode = false;
                    else
                        GlobalData.isPowerSaveMode = oldPowerSaveMode;
                }

                if (GlobalData.getGlobalEventsRuning(context)) {

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
                    eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                    //}
                }
            }
        }
    }
}
