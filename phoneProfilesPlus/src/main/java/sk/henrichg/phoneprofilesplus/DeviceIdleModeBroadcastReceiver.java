package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

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
            if ((powerManager != null) && !powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/) {
                PhoneProfilesService.startHandlerThread();
                final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DeviceIdleModeBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE/*, false*/);

                        // rescan
                        if (PhoneProfilesService.instance != null) {
                            // schedule job for one wifi scan
                            PhoneProfilesService.instance.scheduleWifiJob(true,  true, true, false, false, false);
                            // schedule job for one bluetooth scan
                            PhoneProfilesService.instance.scheduleBluetoothJob(true,  true, true, false, false);
                            // schedule job for location scan
                            PhoneProfilesService.instance.scheduleGeofenceScannerJob(true,  true, true, false);
                        }
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                        if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                            // rescan mobile cells
                            synchronized (PPApplication.phoneStateScannerMutex) {
                                if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                                    PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                                }
                            }
                        }
                        dataWrapper.invalidateDataWrapper();

                        if ((wakeLock != null) && wakeLock.isHeld())
                            wakeLock.release();
                    }
                });
            }
        }
    }
}
