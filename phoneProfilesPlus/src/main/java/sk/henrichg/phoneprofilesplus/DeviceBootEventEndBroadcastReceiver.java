package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class DeviceBootEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### [BOOT] DeviceBootEventEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "DeviceBootEventEndBroadcastReceiver.onReceive", "DeviceBootEventEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("[BOOT] DeviceBootEventEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[BOOT] DeviceBootEventEndBroadcastReceiver.doWork", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            PPApplication.startHandlerThread(/*"DeviceBootEventEndBroadcastReceiver.doWork"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DeviceBootEventEndBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DeviceBootEventEndBroadcastReceiver.doWork");

                        //PPApplication.logE("[BOOT] DeviceBootEventEndBroadcastReceiver.doWork", "handle events");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_BOOT_EVENT_END);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DeviceBootEventEndBroadcastReceiver.doWork");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("DeviceBootEventEndBroadcastReceiver.doWork", "handle events");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_BOOT_EVENT_END);
                }
            }*/
        }
    }

}
