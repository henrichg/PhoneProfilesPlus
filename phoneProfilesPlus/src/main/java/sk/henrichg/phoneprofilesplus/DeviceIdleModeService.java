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
        CallsCounter.logCounter(getApplicationContext(), "DeviceIdleModeService.doWakefulWork", "DeviceIdleModeService_doWakefulWork");

        if (intent != null) {

            Context appContext = getApplicationContext();

            if (Event.getGlobalEventsRunning(appContext))
            {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                // isLightDeviceIdleMode() is @hide :-(
                if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/)
                {
                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE, false);

                    // rescan
                    if (PhoneProfilesService.instance != null) {
                        // schedule job for one wifi scan
                        PhoneProfilesService.instance.scheduleWifiJob(true, true, true, true, false);
                        // schedule job for one bluetooth scan
                        PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, true);
                        // schedule job for location scan
                        PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, true);
                    }
                    DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                        // rescan mobile cells
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                            PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                        }
                    }
                    dataWrapper.invalidateDataWrapper();
                }
            }
        }
    }

}
