package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class EventDelayEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### EventDelayEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "EventDelayEndBroadcastReceiver.onReceive", "EventDelayEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("EventDelayEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(true, context);
        }
    }

    static void doWork(boolean useHandler, Context context) {
        //PPApplication.logE("[HANDLER] EventDelayEndBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            if (useHandler) {
                PPApplication.startHandlerThread(/*"EventDelayEndBroadcastReceiver.doWork"*/);
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EventDelayEndBroadcastReceiver_doWork");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=EventDelayEndBroadcastReceiver.doWork (1)");

                            //PPApplication.logE("EventDelayEndBroadcastReceiver.doWork", "handle events");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_END);

                            PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=EventDelayEndBroadcastReceiver.doWork (1)");
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
            } else {
                //PPApplication.logE("EventDelayEndBroadcastReceiver.doWork", "handle events");
                PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=EventDelayEndBroadcastReceiver.doWork (2)");

                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_END);

                PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=EventDelayEndBroadcastReceiver.doWork (2)");
            }
        }
    }

}
