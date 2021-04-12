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
//        PPApplication.logE("[IN_BROADCAST] DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        //CallsCounter.logCounter(context, "DeviceIdleModeBroadcastReceiver.onReceive", "DeviceIdleModeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if ((powerManager != null) && !powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/) {
                //PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive","NOT in idle mode");
                PPApplication.startHandlerThreadBroadcast(/*"DeviceIdleModeBroadcastReceiver.onReceive"*/);
                final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DeviceIdleModeBroadcastReceiver.onReceive");

                    PowerManager powerManager1 = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager1 != null) {
                            wakeLock = powerManager1.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DeviceIdleModeBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // start events handler
//                            PPApplication.logE("[EVENTS_HANDLER_CALL] DeviceIdleModeBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_IDLE_MODE");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DeviceIdleModeBroadcastReceiver.onReceive");

                        // rescan
                        if (PhoneProfilesService.getInstance() != null) {
                            //PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive", "rescan/reschedule workers");
                            boolean rescan = false;
                            if (ApplicationPreferences.applicationEventLocationEnableScanning)
                                rescan = true;
                            else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                                rescan = true;
                            else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                                rescan = true;
                            else if (ApplicationPreferences.applicationEventMobileCellEnableScanning)
                                rescan = true;
                            else if (ApplicationPreferences.applicationEventOrientationEnableScanning)
                                rescan = true;
                            else if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning)
                                rescan = true;
                            if (rescan) {
                                PPApplication.rescanAllScanners(appContext);
                            }
                        }
                        /*if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                            // rescan mobile cells
                            synchronized (PPApplication.MOBILE_CELLS_SCANNER_MUTEX) {
                                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isMobileCellsScannerStarted()) {
                                    PhoneProfilesService.getInstance().getMobileCellsScanner().rescanMobileCells();
                                }
                            }
                        }*/

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DeviceIdleModeBroadcastReceiver.onReceive");
                    } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                });
            }
            //else
            //    PPApplication.logE("DeviceIdleModeBroadcastReceiver.onReceive","in idle mode");
        }
    }
}
