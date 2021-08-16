package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "PowerSaveModeBroadcastReceiver.onReceive", "PowerSaveModeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);

        // restart scanners when any is enabled
        // required for reschedule workers for power save mode
        boolean restart = false;
        if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventLocationEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventWifiEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventOrientationEnableScanning)
            restart = true;
        if (restart) {
            //PPApplication.logE("[XXX] PowerSaveModeBroadcastReceiver.onReceive", "restartAllScanners");
            //PPApplication.logE("[RJS] PowerSaveModeBroadcastReceiver.onReceive", "restart all scanners");
            // for screenOn=true -> used only for Location scanner - start scan with GPS On
            PPApplication.restartAllScanners(appContext, true);
        }

        if (Event.getGlobalEventsRunning()) {
            PPApplication.startHandlerThreadBroadcast(/*"PowerSaveModeBroadcastReceiver.onReceive"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PowerSaveModeBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PowerSaveModeBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // start events handler
//                        PPApplication.logE("[EVENTS_HANDLER_CALL] PowerSaveModeBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_POWER_SAVE_MODE");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PowerSaveModeBroadcastReceiver.onReceive");
                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            });
        }

    }
}
