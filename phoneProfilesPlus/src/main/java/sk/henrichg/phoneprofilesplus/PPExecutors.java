package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.PowerManager;

import java.util.concurrent.TimeUnit;

class PPExecutors {

    static void scheduleDisableBlockProfileEventActionExecutor() {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleDisableBlockProfileEventActionExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableBlockProfileEventActionExecutor", "--------------- START");
            PPApplication.blockProfileEventActions = false;
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableBlockProfileEventActionExecutor", "--------------- END");
            //worker.smallExecutor.shutdown();
        };
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplication.disableInternalChangeExecutor.schedule(runnable, 30, TimeUnit.SECONDS);
    }

    static void scheduleDisableInternalChangeExecutor() {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleDisableInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableInternalChangeExecutor", "--------------- START");
            PPApplication.ringerModeInternalChange = false;
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplication.disableInternalChangeExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void scheduleDisableScreenTimeoutInternalChangeExecutor() {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "--------------- START");
            PPApplication.disableScreenTimeoutInternalChange = false;
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableScreenTimeoutInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplication.disableInternalChangeExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void scheduleDisableVolumesInternalChangeExecutor() {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleDisableVolumesInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableVolumesInternalChangeExecutor", "--------------- START");
            PPApplication.volumesInternalChange = false;
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableVolumesInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplication.disableInternalChangeExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    static void scheduleDisableBrightnessInternalChangeExecutor() {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleDisableBrightnessInternalChangeExecutor", "schedule");

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableBrightnessInternalChangeExecutor", "--------------- START");
            PPApplication.brightnessInternalChange = false;
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleDisableBrightnessInternalChangeExecutor", "--------------- END");
            //worker.shutdown();
        };
        PPApplicationStatic.createNonBlockedExecutor();
        PPApplication.disableInternalChangeExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
    }

/*
    static void doRestartEventsWithDelay(final boolean alsoRescan, final boolean unblockEventsRun, final int logType, Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        if (logType != PPApplication.ALTYPE_UNDEFINED)
            PPApplication.addActivityLog(appContext, logType, null, null, "");
        //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
        dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
        //dataWrapper.invalidateDataWrapper();
    }

    static void scheduleRestartEventsWithDelayExecutor(final boolean alsoRescan, final boolean unblockEventsRun, final int logType, Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true))
            // application is not started
            return;

//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.scheduleRestartEventsWithDelayExecutor", "schedule");

        final Context appContext = context.getApplicationContext();
        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleRestartEventsWithDelayExecutor", "--------------- START");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExecutors_scheduleRestartEventsWithDelayExecutor");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                doRestartEventsWithDelay(alsoRescan, unblockEventsRun, logType, context);

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.scheduleRestartEventsWithDelayExecutor", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
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
*/
    static void handleEvents(Context context, int _sensorType, String _sensorName, int delay) {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.handleEvents", "schedule - " + _sensorName);

        final Context appContext = context.getApplicationContext();
        final int sensorType = _sensorType;
        final String sensorName = _sensorName;

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.handleEvents", "--------------- START - " + sensorName);

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPPExecutors_handleEvents_"+sensorName);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if (EventStatic.getGlobalEventsRunning(appContext) && (sensorType != 0)) {
                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(sensorType);
                }

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.handleEvents", "--------------- END - " + sensorName + " - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
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
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
        else {
            PPApplicationStatic.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
        }
    }

}
