package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.PowerManager;

import java.util.concurrent.TimeUnit;

class PPPExecutors {

    static void scheduleDisableBlockProfileEventActionExecutor() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.scheduleDisableBlockProfileEventActionExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableBlockProfileEventActionExecutor", "--------------- START");
            PPApplication.blockProfileEventActions = false;
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableBlockProfileEventActionExecutor", "--------------- END");
            //worker.smallExecutor.shutdown();
        };
        PPApplication.createNonBlockedExecutor();
        PPApplication.nonBlockedExecutor.schedule(runnable, 30, TimeUnit.SECONDS);
    }

    static void scheduleDisableInternalChangeExecutor() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.scheduleDisableInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableInternalChangeExecutor", "--------------- START");
            RingerModeChangeReceiver.internalChange = false;
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplication.createNonBlockedExecutor();
        PPApplication.nonBlockedExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void scheduleDisableScreenTimeoutInternalChangeExecutor() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "--------------- START");
            ActivateProfileHelper.disableScreenTimeoutInternalChange = false;
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplication.createNonBlockedExecutor();
        PPApplication.nonBlockedExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void scheduleDisableVolumesInternalChangeExecutor() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.scheduleDisableVolumesInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableVolumesInternalChangeExecutor", "--------------- START");
            EventPreferencesVolumes.internalChange = false;
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleDisableVolumesInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplication.createNonBlockedExecutor();
        PPApplication.nonBlockedExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void doRestartEventsWithDelay(final boolean alsoRescan, final boolean unblockEventsRun, final int logType, Context context) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        if (logType != PPApplication.ALTYPE_UNDEFINED)
            PPApplication.addActivityLog(appContext, logType, null, null, "");
        //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
//            PPApplication.logE("[APP_START] PPPExecutors.doRestartEventsWithDelay", "xxx");
        dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
        //dataWrapper.invalidateDataWrapper();
    }

    static void scheduleRestartEventsWithDelayExecutor(final boolean alsoRescan, final boolean unblockEventsRun, final int logType, Context context) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.scheduleRestartEventsWithDelayExecutor", "schedule");

        final Context appContext = context.getApplicationContext();
        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleRestartEventsWithDelayExecutor", "--------------- START");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExecutors_scheduleRestartEventsWithDelayExecutor");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                doRestartEventsWithDelay(alsoRescan, unblockEventsRun, logType, context);

                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.scheduleRestartEventsWithDelayExecutor", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
                //worker.shutdown();
            }
        };
        PPApplication.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    static void handleEvents(Context context, int _sensorType, String _sensorName, int delay) {
        PPApplication.logE("[EXECUTOR_CALL]  ***** PPPExecutors.handleEvents", "schedule - " + _sensorType);

        final Context appContext = context.getApplicationContext();
        final int sensorType = _sensorType;
        final String sensorName = _sensorName;

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.handleEvents", "--------------- START - " + sensorName);

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExecutors_handleEvents_"+sensorName);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if (Event.getGlobalEventsRunning() && (sensorType != 0)) {
                    //PPApplication.logE("PPPExecutors.handleEvents", "sensorType="+sensorType);
                    // start events handler
                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PPPExecutors.handleEvents: sensorType="+sensorType);

                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(sensorType);

//                    PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPPExecutors.handleEvents");
                }

                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                PPApplication.logE("[IN_EXECUTOR]  ***** PPPExecutors.handleEvents", "--------------- END - " + sensorName + " - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
                //worker.shutdown();
            }
        };
        if (delay == 0) {
            PPApplication.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
        else {
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
        }
    }

}
