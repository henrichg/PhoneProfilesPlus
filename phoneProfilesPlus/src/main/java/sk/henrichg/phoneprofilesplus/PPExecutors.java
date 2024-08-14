package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.PowerManager;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class PPExecutors {

    private static final String WAKELOCK_TAG = PPApplication.PACKAGE_NAME + ":PPPExecutors_handleEvents_";

    static final String SENSOR_NAME_SENSOR_TYPE_SOUND_PROFILE = "SENSOR_TYPE_SOUND_PROFILE";
    static final String SENSOR_NAME_SENSOR_TYPE_RADIO_SWITCH = "SENSOR_TYPE_RADIO_SWITCH";
    static final String SENSOR_NAME_SENSOR_TYPE_ALARM_CLOCK_EVENT_END = "SENSOR_TYPE_ALARM_CLOCK_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_BATTERY = "SENSOR_TYPE_BATTERY";
    static final String SENSOR_NAME_SENSOR_TYPE_BATTERY_WITH_LEVEL = "SENSOR_TYPE_BATTERY_WITH_LEVEL";
    static final String SENSOR_NAME_SENSOR_TYPE_BLUETOOTH_SCANNER = "SENSOR_TYPE_BLUETOOTH_SCANNER";
    static final String SENSOR_NAME_SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED = "SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED";
    static final String SENSOR_NAME_SENSOR_TYPE_DEVICE_BOOT_EVENT_END = "SENSOR_TYPE_DEVICE_BOOT_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_DOCK_CONNECTION = "SENSOR_TYPE_DOCK_CONNECTION";
    static final String SENSOR_NAME_SENSOR_TYPE_CALENDAR = "SENSOR_TYPE_CALENDAR";
    static final String SENSOR_NAME_SENSOR_TYPE_EVENT_DELAY_END = "SENSOR_TYPE_EVENT_DELAY_END";
    static final String SENSOR_NAME_SENSOR_TYPE_EVENT_DELAY_START = "SENSOR_TYPE_EVENT_DELAY_START";
    static final String SENSOR_NAME_SENSOR_TYPE_PERIODIC = "SENSOR_TYPE_PERIODIC";
    static final String SENSOR_NAME_SENSOR_TYPE_TIME = "SENSOR_TYPE_TIME";
    static final String SENSOR_NAME_SENSOR_TYPE_HEADSET_CONNECTION = "SENSOR_TYPE_HEADSET_CONNECTION";
    static final String SENSOR_NAME_SENSOR_TYPE_LOCATION_SCANNER = "SENSOR_TYPE_LOCATION_SCANNER";
    //static final String SENSOR_NAME_SENSOR_TYPE_SCREEN = "SENSOR_TYPE_SCREEN";
    //static final String SENSOR_NAME_SENSOR_TYPE_BRIGHTNESS = "SENSOR_TYPE_BRIGHTNESS";
    static final String SENSOR_NAME_SENSOR_TYPE_SCREEN_BRIGHTNESS = "SENSOR_TYPE_SCREEN_BRIGHTNESS";
    static final String SENSOR_NAME_SENSOR_TYPE_PHONE_CALL_EVENT_END = "SENSOR_TYPE_PHONE_CALL_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_MOBILE_CELLS = "SENSOR_TYPE_MOBILE_CELLS";
    static final String SENSOR_NAME_SENSOR_TYPE_NFC_EVENT_END = "SENSOR_TYPE_NFC_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_NOTIFICATION = "SENSOR_TYPE_NOTIFICATION";
    static final String SENSOR_NAME_SENSOR_TYPE_PERIODIC_EVENT_END = "SENSOR_TYPE_PERIODIC_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_ROAMING = "SENSOR_TYPE_ROAMING";
    static final String SENSOR_NAME_SENSOR_TYPE_POWER_SAVE_MODE = "SENSOR_TYPE_POWER_SAVE_MODE";
    static final String SENSOR_NAME_SENSOR_TYPE_SMS_EVENT_END = "SENSOR_TYPE_SMS_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_WIFI_SCANNER = "SENSOR_TYPE_WIFI_SCANNER";
    static final String SENSOR_NAME_SENSOR_TYPE_APPLICATION_EVENT_END = "SENSOR_TYPE_APPLICATION_EVENT_END";
    static final String SENSOR_NAME_SENSOR_TYPE_CALL_SCREENING_EVENT_END = "SENSOR_TYPE_CALL_SCREENING_EVENT_END";

    private PPExecutors() {
        // private constructor to prevent instantiation
    }

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

    static void scheduleDisableRingerModeInternalChangeExecutor() {
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

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExecutors.scheduleDisableRingerModeInternalChangeExecutor", "xxxxxxxxxxxxxxxxxxxx");

        handleEventsMianWorker(EventsHandler.SENSOR_TYPE_SOUND_PROFILE, MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG/*, 0*/);
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
        //handleEventsMianWorker(EventsHandler.SENSOR_TYPE_SOUND_PROFILE, MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG, 0);
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

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExecutors.scheduleDisableVolumesInternalChangeExecutor", "xxxxxxxxxxxxxxxxxxxx");
        handleEventsMianWorker(EventsHandler.SENSOR_TYPE_VOLUMES, MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG/*, 0*/);
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

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExecutors.scheduleDisableBrightnessInternalChangeExecutor", "xxxxxxxxxxxxxxxxxxxx");
        handleEventsMianWorker(EventsHandler.SENSOR_TYPE_BRIGHTNESS, MainWorker.HANDLE_EVENTS_BRIGHTNESS_WORK_TAG/*, 0*/);
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

    // !!! call this only when is needed partial wakelock or delay > 0
    static void handleEvents(Context context, int[] _sensorType, String _sensorName, int delay) {
//        PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PPExecutors.handleEvents", "schedule - " + _sensorName);

        final Context appContext = context.getApplicationContext();
        final int[] sensorType = _sensorType;
        final String sensorName = _sensorName;

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PPExecutors.handleEvents", "--------------- START - " + sensorName);

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG + sensorName);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if (EventStatic.getGlobalEventsRunning(appContext) && (sensorType.length != 0)) {
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

    static private void handleEventsMianWorker(int _sensorType, String _sensorWorkTag/*, int delay*/) {
        Data workData = new Data.Builder()
                .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, _sensorType)
                .build();

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] PPExecutors.handleEventsMianWorker", "xxxxxxxxxxxxxxxxxxxx");

        OneTimeWorkRequest worker;
        //if (delay == 0)
            worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(_sensorWorkTag)
                        .setInputData(workData)
                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                        .build();
        /*else
            worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(_sensorWorkTag)
                            .setInputData(workData)
                            .setInitialDelay(delay, TimeUnit.SECONDS)
                            //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                            .build();*/
        try {
//                            if (PPApplicationStatic.getApplicationStarted(true, true)) {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}
//
//                PPApplicationStatic.logE("[WORKER_CALL] PPExecutors.handleEventsMianWorker", _sensorWorkTag);
                //workManager.enqueue(worker);
                workManager.enqueueUniqueWork(_sensorWorkTag, ExistingWorkPolicy.REPLACE, worker);
            }
//                            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
