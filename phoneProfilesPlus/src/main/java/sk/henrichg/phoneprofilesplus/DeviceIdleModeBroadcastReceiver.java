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
        GlobalData.logE("##### DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/)
            {
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);

                // rescan
                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                    // send broadcast for one wifi scan
                    WifiScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                    //WifiScanAlarmBroadcastReceiver.sendBroadcast(context);
                }
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                    // send broadcast for one bluetooth scan
                    BluetoothScanAlarmBroadcastReceiver.setAlarm(context, true, true);
                    //BluetoothScanAlarmBroadcastReceiver.sendBroadcast(context);
                }
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                    // send broadcast for location scan
                    GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, true);
                    //GeofenceScannerAlarmBroadcastReceiver.sendBroadcast(context);
                }
                dataWrapper.invalidateDataWrapper();
            }
        }
    }
}
