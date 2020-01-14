package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class CalendarProviderChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### CalendarProviderChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "CalendarProviderChangedBroadcastReceiver.onReceive", "CalendarProviderChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(appContext))
        {
            //PPApplication.logE("@@@ CalendarProviderChangedBroadcastReceiver.onReceive","xxx");

            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("CalendarProviderChangedBroadcastReceiver.onReceive","calendarEventsExists="+calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
                PPApplication.startHandlerThread("CalendarProviderChangedBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CalendarProviderChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=CalendarProviderChangedBroadcastReceiver.onReceive");

                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=CalendarProviderChangedBroadcastReceiver.onReceive");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            //}

        }

    }

}
