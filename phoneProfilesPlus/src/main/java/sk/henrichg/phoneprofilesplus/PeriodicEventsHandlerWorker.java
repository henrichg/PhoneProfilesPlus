package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PeriodicEventsHandlerWorker extends Worker {

    final Context context;

    static final String WORK_TAG  = "periodicEventsHandlerWorker";
    static final String WORK_TAG_SHORT  = "periodicEventsHandlerWorkerShort";

    public PeriodicEventsHandlerWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER]  PeriodicEventsHandlerWorker.doWork", "--------------- START");

            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return Result.success();

            boolean scanningPaused = ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2") &&
                    GlobalUtils.isNowTimeBetweenTimes(
                            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo);

            if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning && (!scanningPaused)) {

                //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
                if (isPowerSaveMode) {
                    if (ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode.equals("2")) {
                        PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                        PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
//                        if (PPApplicationStatic.logEnabled()) {
//                            PPApplicationStatic.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "return - update in power save mode is not allowed");
//                            PPApplicationStatic.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "---------------------------------------- END");
//                        }
                        return Result.success();
                    }
                }
                else {
                    if (ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2")) {
                        if (GlobalUtils.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo)) {
                            // not scan in configured time
                            PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                            PPApplicationStatic.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
//                            if (PPApplicationStatic.logEnabled()) {
//                                PPApplicationStatic.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "return - update in configured time is not allowed");
//                                PPApplicationStatic.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "---------------------------------------- END");
//                            }
                            return Result.success();
                        }
                    }
                }

                if (EventStatic.getGlobalEventsRunning(getApplicationContext())) {

                    boolean callEventsHandler = false;
                    Set<String> tags = getTags();
                    for (String tag : tags) {

                        if (tag.equals(WORK_TAG)) {
                            callEventsHandler = true;
                            break;
                        }
                    }

                    if (callEventsHandler) {
//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PeriodicEventsHandlerWorker.doWork", "sensorType=SENSOR_TYPE_PERIODIC_EVENTS_HANDLER");
                        EventsHandler eventsHandler = new EventsHandler(getApplicationContext());
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_PERIODIC_EVENTS_HANDLER});
                    }
                }

//                PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** PeriodicEventsHandlerWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG");
                final Context appContext = context;
                //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                Runnable runnable = () -> {
//                    long start1 = System.currentTimeMillis();
//                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PeriodicEventsHandlerWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG");
                    PeriodicEventsHandlerWorker.enqueueWork(appContext);
//                    long finish = System.currentTimeMillis();
//                    long timeElapsed = finish - start1;
//                    PPApplicationStatic.logE("[IN_EXECUTOR]  ***** PeriodicEventsHandlerWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG - timeElapsed="+timeElapsed);
                    //worker.shutdown();
                };
                PPApplicationStatic.createDelayedEventsHandlerExecutor();
                PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
                /*
                //enqueueWork();
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG)
                                .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                        PPApplicationStatic.logE("[WORKER_CALL] PeriodicEventsHandlerWorker.doWork", "xxx");
                        workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                */
                /*
                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PeriodicEventsHandlerWorker.enqueueWork(context);
                    }
                }, 1500);
                */
            }

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  PeriodicEventsHandlerWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("PeriodicEventsHandlerWorker.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    static void enqueueWork(Context appContext) {
        int interval = ApplicationPreferences.applicationEventPeriodicScanningScanInterval;
        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(appContext);
        if (isPowerSaveMode) {
            if (ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode.equals("1"))
                interval = 2 * interval;
        }
        else {
            if (ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("1")) {
                if (GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo)) {
                    interval = 2 * interval;
                }
            }
        }

        /*int keepResultsDelay = (interval * 5);
        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
        OneTimeWorkRequest periodicEventsHandlerWorker =
                new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                        .addTag(PeriodicEventsHandlerWorker.WORK_TAG)
                        .setInitialDelay(interval, TimeUnit.MINUTES)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

//                                //if (PPApplicationStatic.logEnabled()) {
//                                ListenableFuture<List<WorkInfo>> statuses;
//                                statuses = workManager.getWorkInfosForUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG);
//                                try {
//                                    List<WorkInfo> workInfoList = statuses.get();
//                                } catch (Exception ignored) {
//                                }
//                                //}

//                PPApplicationStatic.logE("[WORKER_CALL] PeriodicEventsHandlerWorker.enqueueWork", "xxx");
                workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
