package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "PowerSaveModeBroadcastReceiver.onReceive", "PowerSaveModeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        // required for reschedule workers for power save mode
        //PPApplication.logE("[XXX] PowerSaveModeBroadcastReceiver.onReceive", "restartAllScanners");
        //PPApplication.logE("[RJS] PowerSaveModeBroadcastReceiver.onReceive", "restart all scanners");
        PPApplication.restartAllScanners(appContext, true);
        /*PPApplication.restartWifiScanner(appContext, true);
        PPApplication.restartBluetoothScanner(appContext, true);
        PPApplication.restartGeofenceScanner(appContext, true);
        PPApplication.restartPhoneStateScanner(appContext, true);
        PPApplication.restartOrientationScanner(appContext);*/

        PPApplication.startHandlerThread("PowerSaveModeBroadcastReceiver.onReceive");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PowerSaveModeBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PowerSaveModeBroadcastReceiver.onReceive");

                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PowerSaveModeBroadcastReceiver.onReceive");
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
}
