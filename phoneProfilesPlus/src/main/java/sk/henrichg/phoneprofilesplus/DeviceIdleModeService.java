package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class DeviceIdleModeService extends WakefulIntentService {

    public DeviceIdleModeService() {
        super("DeviceIdleModeService");
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {

            Context appContext = getApplicationContext();

            if (Event.getGlobalEventsRunning(appContext))
            {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                // isLightDeviceIdleMode() is @hide :-(
                if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/)
                {
                    // start service
                    try {
                        Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_DEVICE_IDLE_MODE);
                        WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                    } catch (Exception ignored) {}

                    // rescan
                    DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0) {
                        // schedule job for one wifi scan
                        WifiScanJob.scheduleJob(appContext, true, true, false);
                    }
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHINFRONT) > 0) {
                        // schedule job for one bluetooth scan
                        BluetoothScanJob.scheduleJob(appContext, true, true);
                    }
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
                        // schedule job for location scan
                        PPApplication.logE("GeofenceScannerJob.scheduleJob", "from DeviceIdleModeBroadcastReceiver.onReceive");
                        GeofenceScannerJob.scheduleJob(appContext, true, true);
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

}
