package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class EventDelayStartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### EventDelayStartBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "EventDelayStartBroadcastReceiver.onReceive", "EventDelayStartBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ EventDelayStartBroadcastReceiver.onReceive","xxx");

            // start job
            //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_EVENT_DELAY_START);
            final Handler handler = new Handler(appContext.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EventDelayStartBroadcastReceiver.onReceive");
                    wakeLock.acquire(10 * 60 * 1000);

                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_EVENT_DELAY_START, false);

                    wakeLock.release();
                }
            });
        }

    }

}
