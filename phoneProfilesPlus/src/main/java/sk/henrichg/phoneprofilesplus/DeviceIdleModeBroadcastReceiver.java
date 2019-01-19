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
                PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive","NOT in idle mode");
                PPApplication.startHandlerThread("DeviceIdleModeBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DeviceIdleModeBroadcastReceiver.onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE);

                        /* Not needed, job is started in maintenance window
                        // rescan
                        if (PhoneProfilesService.getInstance() != null) {
                            PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive", "rescan/reschedule jobs");

                            // schedule job for one wifi scan
                            PhoneProfilesService.getInstance().scheduleWifiJob(true,  true, //true, false, false,
                                    false);
                            // schedule job for one bluetooth scan
                            PhoneProfilesService.getInstance().scheduleBluetoothJob(true,  true, //true, false,
                                    false);
                            // schedule job for location scan
                            PhoneProfilesService.getInstance().scheduleGeofenceScannerJob(true,  true, //true,
                                    false);
                        }*/
                            if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                                // rescan mobile cells
                                synchronized (PPApplication.phoneStateScannerMutex) {
                                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                                        PhoneProfilesService.getInstance().getPhoneStateScanner().rescanMobileCells();
                                    }
                                }
                            }
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
            else
                PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive","in idle mode");
        }
    }
}
