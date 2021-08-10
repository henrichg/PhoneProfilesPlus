package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class DockConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] DockConnectionBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "DockConnectionBroadcastReceiver.onReceive", "DockConnectionBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning())
        {
            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            boolean accessoryEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ACCESSORY) > 0;
            dataWrapper.invalidateDataWrapper();

            if (accessoryEventsExists)
            {*/
                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThreadBroadcast(/*"DockConnectionBroadcastReceiver.onReceive"*/);
                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DockConnectionBroadcastReceiver.onReceive");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DockConnectionBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] DockConnectionBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DOCK_CONNECTION");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DOCK_CONNECTION);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DockConnectionBroadcastReceiver.onReceive");
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
            //}

        }
    }
}
