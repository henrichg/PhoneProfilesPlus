package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

public class DeviceIdleModeBroadcastReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "DeviceIdleModeBroadcastReceiver.onReceive", "DeviceIdleModeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //DeviceIdleModeJob.start(appContext);

        if (Event.getGlobalEventsRunning(appContext)) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/) {
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE, false);

                        // rescan
                        if (PhoneProfilesService.instance != null) {
                            // schedule job for one wifi scan
                            PhoneProfilesService.instance.scheduleWifiJob(true, true, true, true, false, false, false);
                            // schedule job for one bluetooth scan
                            PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, true, false, false);
                            // schedule job for location scan
                            PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, true, false);
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
                });
            }
        }
    }
}
