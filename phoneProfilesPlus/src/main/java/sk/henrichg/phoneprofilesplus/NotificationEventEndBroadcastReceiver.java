package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class NotificationEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] NotificationEventEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "NotificationEventEndBroadcastReceiver.onReceive", "NotificationEventEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("NotificationEventEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[HANDLER] NotificationEventEndBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"NotificationEventEndBroadcastReceiver.doWork"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=NotificationEventEndBroadcastReceiver.doWork");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NotificationEventEndBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] NotificationEventEndBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_NOTIFICATION");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=NotificationEventEndBroadcastReceiver.doWork");
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
                }
            });
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("NotificationEventEndBroadcastReceiver.doWork", "handle events");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION);
                }
            }*/
        }
    }

}
