package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class OrientationEventBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] OrientationEventBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "OrientationEventBroadcastReceiver.onReceive", "OrientationEventBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("OrientationEventBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[HANDLER] OrientationEventBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            PPApplication.startHandlerThread(/*"OrientationEventBroadcastReceiver.doWork"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":OrientationEventBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=OrientationEventBroadcastReceiver.doWork");

//                        PPApplication.logE("[EVENTS_HANDLER] OrientationEventBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_DEVICE_ORIENTATION");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=SMSEventEndBroadcastReceiver.doWork");

                        if (PhoneProfilesService.getInstance() != null) {
                            PhoneProfilesService service = PhoneProfilesService.getInstance();
                            if (service.isOrientationScannerStarted())
                                service.setOrientationSensorAlarm(appContext.getApplicationContext());
                        }

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
                PPApplication.logE("SMSEventEndBroadcastReceiver.doWork", "handle events");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SMS_EVENT_END);

                if (PhoneProfilesService.getInstance() != null) {
                    PhoneProfilesService service = PhoneProfilesService.getInstance();
                    if (service.isOrientationScannerStarted())
                        service.setOrientationSensorAlarm(context.getApplicationContext());
                }
            }*/
        }
    }

}
