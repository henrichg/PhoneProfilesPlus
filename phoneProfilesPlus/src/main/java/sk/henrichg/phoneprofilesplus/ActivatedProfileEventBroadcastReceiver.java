package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ActivatedProfileEventBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_ACTIVATED_PROFILE = "activated_profile";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST]  ActivatedProfileEventBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            long profileId = intent.getLongExtra(EXTRA_ACTIVATED_PROFILE, 0);
            if (profileId != 0)
                doWork(profileId, context);
        }
    }

    private void doWork(long _profileId, Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            //if (useHandler) {
            final long profileId = _profileId;

            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ActivatedProfileEventBroadcastReceiver.doWork");

                synchronized (PPApplication.handleEventsMutex) {
                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_ActivatedProfileEventBroadcastReceiver_doWork);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                        dataWrapper.fillEventList();
                        //dataWrapper.fillProfileList(false, false);

                        boolean profileExists = dataWrapper.profileExists(profileId);
                        if (profileExists) {
                            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                            for (Event _event : dataWrapper.eventList) {
                                if ((_event._eventPreferencesActivatedProfile._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                                    if (_event._eventPreferencesActivatedProfile.isRunnable(appContext) &&
                                            _event._eventPreferencesActivatedProfile.isAllConfigured(appContext)) {

                                        if (_event._eventPreferencesActivatedProfile._useDuration) {
                                            databaseHandler.getActivatedProfileStartTime(_event);
                                            if (_event._eventPreferencesActivatedProfile._startTime == 0) {
                                                // alarm is  not started
                                                long startProfile = _event._eventPreferencesActivatedProfile._startProfile;
                                                long detectedProfile = _event._eventPreferencesActivatedProfile._detectedProfile;
                                                if ((detectedProfile != profileId) && (profileId == startProfile)) {
                                                    // activated profile changed
//                                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ActivatedProfileEventBroadcastReceiver.doWork", "SENSOR_TYPE_ACTIVATED_PROFILE");
                                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE});
                                                }
                                            }
                                        } else {
                                            int oldRunning = _event._eventPreferencesActivatedProfile._running;

                                            long startProfile = _event._eventPreferencesActivatedProfile._startProfile;
                                            if (profileId == startProfile) {
                                                _event._eventPreferencesActivatedProfile._running =
                                                        EventPreferencesActivatedProfile.RUNNING_RUNNING;
                                                // save running to database
                                                databaseHandler.
                                                        updateActivatedProfileSensorRunningParameter(_event);
                                            }
                                            long endProfile = _event._eventPreferencesActivatedProfile._endProfile;
                                            if (profileId == endProfile) {
                                                _event._eventPreferencesActivatedProfile._running =
                                                        EventPreferencesActivatedProfile.RUNNING_NOTRUNNING;
                                                // save running to database
                                                databaseHandler.
                                                        updateActivatedProfileSensorRunningParameter(_event);
                                            }
                                            if (oldRunning != _event._eventPreferencesActivatedProfile._running) {
                                                // running was changed, call EventsHandler
//                                                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ActivatedProfileEventBroadcastReceiver.doWork", "SENSOR_TYPE_ACTIVATED_PROFILE");
                                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE});
                                            }
                                        }
                                    }
                                }
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
//            PPApplicationStatic.logE("[EXECUTOR_CALL] ActivatedProfileEventBroadcastReceiver.doWork", "xxx");
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

}
