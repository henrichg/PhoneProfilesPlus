package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DeviceIdleModeBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "deviceIdleMode";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/)
            {
                // start service
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(appContext, eventsServiceIntent);

                // rescan
                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                    // send broadcast for one wifi scan
                    WifiScanJob.scheduleJob(context, true, true, false);
                    //WifiScanAlarmBroadcastReceiver.setAlarm(appContext, true, true, false);
                    //WifiScanAlarmBroadcastReceiver.sendBroadcast(appContext);
                }
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                    // send broadcast for one bluetooth scan
                    BluetoothScanJob.scheduleJob(context, true, true);
                    //BluetoothScanAlarmBroadcastReceiver.setAlarm(appContext, true, true);
                    //BluetoothScanAlarmBroadcastReceiver.sendBroadcast(appContext);
                }
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                    // send broadcast for location scan
                    GeofenceScannerAlarmBroadcastReceiver.setAlarm(appContext, true, true);
                    //GeofenceScannerAlarmBroadcastReceiver.sendBroadcast(appContext);
                }
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                    // rescan mobile cells
                    if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateStarted()) {
                        PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                    }
                }
                dataWrapper.invalidateDataWrapper();
            }
        }
    }
}
