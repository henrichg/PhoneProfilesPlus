package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "AirplaneModeStateChangedBroadcastReceiver.onReceive", "AirplaneModeStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThread(/*"AirplaneModeStateChangedBroadcastReceiver.onReceive"*/);
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AirplaneModeStateChangedBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

//                                PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=AirplaneModeStateChangedBroadcastReceiver.onReceive");

//                                PPApplication.logE("[EVENTS_HANDLER] AirplaneModeStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=AirplaneModeStateChangedBroadcastReceiver.onReceive");
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
        }
    }
}
