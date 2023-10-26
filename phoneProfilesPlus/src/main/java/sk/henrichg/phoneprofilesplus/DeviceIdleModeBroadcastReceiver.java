package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class DeviceIdleModeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();

        if (EventStatic.getGlobalEventsRunning(appContext)) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if ((powerManager != null) && !powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/) {
                //PPApplication.startHandlerThreadBroadcast(/*"DeviceIdleModeBroadcastReceiver.onReceive"*/);
                //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DeviceIdleModeBroadcastReceiver.onReceive");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager1 = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager1 != null) {
                                wakeLock = powerManager1.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DeviceIdleModeBroadcastReceiver_onReceive);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            // start events handler
//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] DeviceIdleModeBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_IDLE_MODE");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE});

                            // rescan
                            if (PhoneProfilesService.getInstance() != null) {
                                boolean rescan = false;
                                if (ApplicationPreferences.applicationEventLocationEnableScanning)
                                    rescan = true;
                                else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                                    rescan = true;
                                else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                                    rescan = true;
                                else if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                                    PPApplicationStatic.logE("[TEST BATTERY] DeviceIdleModeBroadcastReceiver.onReceive", "******** ### *******");
                                    rescan = true;
                                }
                                else if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                                    PPApplicationStatic.logE("[TEST BATTERY] DeviceIdleModeBroadcastReceiver.onReceive", "******** ### *******");
                                    rescan = true;
                                }
                                else if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
                                    rescan = true;
                                if (rescan) {
                                    PPApplicationStatic.rescanAllScanners(appContext);
                                }
                            }
                            /*if (DatabaseHandler.getInstance(appContext).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                                // rescan mobile cells
                                synchronized (PPApplication.mobileCellsScannerMutex) {
                                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isMobileCellsScannerStarted()) {
                                        PhoneProfilesService.getInstance().getMobileCellsScanner().rescanMobileCells();
                                    }
                                }
                            }*/

                        } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                }; //);
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable);
            }
        }
    }
}
