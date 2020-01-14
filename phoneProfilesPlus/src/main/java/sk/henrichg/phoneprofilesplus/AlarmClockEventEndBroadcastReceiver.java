package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class AlarmClockEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### AlarmClockEventEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "AlarmClockEventEndBroadcastReceiver.onReceive", "AlarmClockEventEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("AlarmClockEventEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[HANDLER] AlarmClockEventEndBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //if (useHandler) {
            PPApplication.startHandlerThread("AlarmClockEventEndBroadcastReceiver.doWork");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AlarmClockEventEndBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=AlarmClockEventEndBroadcastReceiver.doWork");

                        if (Event.getGlobalEventsRunning(appContext)) {
                            //PPApplication.logE("EventDelayStartBroadcastReceiver.doWork", "handle events");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=AlarmClockEventEndBroadcastReceiver.doWork");
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
                PPApplication.logE("AlarmClockEventEndBroadcastReceiver.doWork", "handle events");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END);
            }
        }*/
    }

}
