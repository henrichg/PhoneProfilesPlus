package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class NotificationEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NotificationEventEndBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NotificationEventEndBroadcastReceiver.onReceive", "NotificationEventEndBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ NotificationEventEndBroadcastReceiver.onReceive","xxx");

            /*boolean notificationEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            notificationEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_NOTIFICATION) > 0;
            PPApplication.logE("NotificationEventEndBroadcastReceiver.onReceive","notificationEventsExists="+notificationEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (notificationEventsExists)
            {*/
                // start job
                //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_NOTIFICATION_EVENT_END);
                PhoneProfilesService.startHandlerThread();
                final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotificationEventEndBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NOTIFICATION_EVENT_END, false);

                        if (wakeLock != null)
                            wakeLock.release();
                    }
                });
            //}

        }

    }

}
