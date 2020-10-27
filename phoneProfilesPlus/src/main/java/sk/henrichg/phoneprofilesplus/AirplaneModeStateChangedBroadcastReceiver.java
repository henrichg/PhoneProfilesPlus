package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "AirplaneModeStateChangedBroadcastReceiver.onReceive", "AirplaneModeStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThreadBroadcast(/*"AirplaneModeStateChangedBroadcastReceiver.onReceive"*/);
                    final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=AirplaneModeStateChangedBroadcastReceiver.onReceive");

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AirplaneModeStateChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] AirplaneModeStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=AirplaneModeStateChangedBroadcastReceiver.onReceive");
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }
        }
    }
}
