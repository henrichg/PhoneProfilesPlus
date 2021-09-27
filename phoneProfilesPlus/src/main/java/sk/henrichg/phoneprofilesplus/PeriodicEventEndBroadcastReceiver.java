package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class PeriodicEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] PeriodicEventEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "PeriodicEventEndBroadcastReceiver.onReceive", "PeriodicEventEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("[BOOT] PeriodicEventEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[BOOT] PeriodicEventEndBroadcastReceiver.doWork", "xxx");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadBroadcast(/*"PeriodicEventEndBroadcastReceiver.doWork"*/);
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PeriodicEventEndBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PeriodicEventEndBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] PeriodicEventEndBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_PERIODIC_EVENT_END");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENT_END);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PeriodicEventEndBroadcastReceiver.doWork");
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
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("PeriodicEventEndBroadcastReceiver.doWork", "handle events");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENT_END);
                }
            }*/
        }
    }

}
