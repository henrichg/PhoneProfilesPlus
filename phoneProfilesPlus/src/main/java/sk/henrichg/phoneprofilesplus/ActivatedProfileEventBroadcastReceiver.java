package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ActivatedProfileEventBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_ACTIVATED_PROFILE = "activated_profile";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST]  ActivatedProfileEventBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            long profileId = intent.getLongExtra(EXTRA_ACTIVATED_PROFILE, 0);
            if (profileId != 0)
                doWork(profileId, context);
        }
    }

    private void doWork(long _profileId, Context context) {
        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            final long profileId = _profileId;

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ActivatedProfileEventBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActivatedProfileEventBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                        dataWrapper.fillEventList();
                        //dataWrapper.fillProfileList(false, false);

                        Profile activatedProfile = dataWrapper.getProfileById(profileId, false, false, false);
                        if (activatedProfile != null) {

                            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                            for (Event _event : dataWrapper.eventList) {
                                if ((_event._eventPreferencesActivatedProfile._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                                    if (_event._eventPreferencesActivatedProfile.isRunnable(context)) {
                                        int oldRunning = _event._eventPreferencesActivatedProfile._running;

                                        long startProfile = _event._eventPreferencesActivatedProfile._startProfile;
                                        if (activatedProfile._id == startProfile) {
                                            _event._eventPreferencesActivatedProfile._running =
                                                    EventPreferencesActivatedProfile.RUNNING_RUNNING;
                                            // save running to database
                                            databaseHandler.
                                                    updateActivatedProfileSensorRunningParameter(_event);
                                        }
                                        long endProfile = _event._eventPreferencesActivatedProfile._endProfile;
                                        if (activatedProfile._id == endProfile) {
                                            _event._eventPreferencesActivatedProfile._running =
                                                    EventPreferencesActivatedProfile.RUNNING_NOTRUNNING;
                                            // save running to database
                                            databaseHandler.
                                                    updateActivatedProfileSensorRunningParameter(_event);
                                        }

                                        if (oldRunning != _event._eventPreferencesActivatedProfile._running) {
                                            // running was changed, call EventsHandler
                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE);
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }; //);
            PPApplication.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

}
