package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class EventDelayStartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] EventDelayStartBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "EventDelayStartBroadcastReceiver.onReceive", "EventDelayStartBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("EventDelayStartBroadcastReceiver.onReceive", "action=" + action);
            doWork(true, context);
        }
    }

    static void doWork(boolean useHandler, Context context) {
        //PPApplication.logE("[HANDLER] EventDelayStartBroadcastReceiver.doWork", "useHandler="+useHandler);

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            if (useHandler) {
                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThreadBroadcast(/*"EventDelayStartBroadcastReceiver.doWork"*/);
                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventDelayStartBroadcastReceiver.doWork (1)");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EventDelayStartBroadcastReceiver_doWork");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] EventDelayStartBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_EVENT_DELAY_START (1)");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_START);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=EventDelayStartBroadcastReceiver.doWork (1)");
                        } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            } else {
                //PPApplication.logE("EventDelayStartBroadcastReceiver.doWork", "handle events");
                //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=EventDelayStartBroadcastReceiver.doWork (2)");

                final Context appContext = context.getApplicationContext();


//                PPApplication.logE("[EVENTS_HANDLER_CALL] EventDelayStartBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_EVENT_DELAY_START (2)");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_START);

                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=EventDelayStartBroadcastReceiver.doWork (2)");
            }
        }
    }

}
