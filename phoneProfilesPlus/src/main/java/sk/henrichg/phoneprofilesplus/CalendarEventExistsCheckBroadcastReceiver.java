package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class CalendarEventExistsCheckBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST]  CalendarEventExistsCheckBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM]  CalendarEventExistsCheckBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            //if (useHandler) {
            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=CalendarEventExistsCheckBroadcastReceiver.doWork");

                synchronized (PPApplication.handleEventsMutex) {

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_CalendarEventExistsCheckBroadcastReceiver_doWork);
                            wakeLock.acquire(10 * 60 * 1000);
                        }


//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] CalendarEventExistsCheckBroadcastReceiver.doWork", "SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK");
//                        Log.e("CalendarEventExistsCheckBroadcastReceiver.doWork", "call of events handler");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK});

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                        dataWrapper.fillEventList();
                        for (Event _event : dataWrapper.eventList) {
                            if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                                _event._eventPreferencesCalendar.setAlarm(/*true,*/ 0, appContext, true);
                            }
                        }
                        dataWrapper.invalidateDataWrapper();
                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    //}
                }
            };
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_TIME);
                }
            }*/
        }
    }

}
