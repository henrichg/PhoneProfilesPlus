package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SearchCalendarEventsWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "SearchCalendarEventsJob";
    static final String WORK_TAG_SHORT  = "SearchCalendarEventsJobShort";

    public SearchCalendarEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        //noinspection ExtractMethodRecommender
        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER]  SearchCalendarEventsWorker.doWork", "--------------- START");

            if (!PPApplicationStatic.getApplicationStarted(true, true)) {
                // application is not started
                return Result.success();
            }

            if (EventStatic.getGlobalEventsRunning(context)) {
                // start events handler
//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] SearchCalendarEventsWorker.doWork", "SENSOR_TYPE_SEARCH_CALENDAR_EVENTS");
                synchronized (PPApplication.handleEventsMutex) {
                    EventsHandler eventsHandler = new EventsHandler(context);
                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS});
                }
            }


//            PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** SearchCalendarEventsWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG");
            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_SearchCalendarEventsWorker_doWork);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

    //                long start1 = System.currentTimeMillis();
    //                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** SearchCalendarEventsWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG");
                    SearchCalendarEventsWorker.scheduleWork(false);
    //                long finish = System.currentTimeMillis();
    //                long timeElapsed = finish - start1;
    //                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** SearchCalendarEventsWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG - timeElapsed="+timeElapsed);
                    //worker.shutdown();

                } catch (Exception e) {
                    PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] SearchCalendarEventsWorker.doWork", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            };
            PPApplicationStatic.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            /*
            //scheduleWork(false);
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG)
                            .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] SearchCalendarEventsWorker.doWork", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            */

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  SearchCalendarEventsWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }

    public void onStopped () {
//        PPApplicationStatic.logE("[IN_LISTENER] SearchCalendarEventsWorker.onStopped", "xxx");
    }

    private static void _scheduleWork(final boolean shortInterval) {
        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    if (!shortInterval) {
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .setInitialDelay(24, TimeUnit.HOURS)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG)
                                .build();

//                        //if (PPApplicationStatic.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplicationStatic.logE("[WORKER_CALL] SearchCalendarEventsWorker._scheduleWork", "(1)");
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    } else {
                        //waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG_SHORT)
                                .build();

//                        //if (PPApplicationStatic.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplicationStatic.logE("[WORKER_CALL] SearchCalendarEventsWorker._scheduleWork", "(2)");
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    }

                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    // shortInterval = true is called only from PPService.scheduleSearchCalendarEventsWorker and TimeVjangedReceiver.doWork
    static void scheduleWork(final boolean shortInterval) {
        if (shortInterval) {
            _cancelWork(false);
            //PPApplication.sleep(5000);
            _scheduleWork(true);
        }
        else
            _scheduleWork(false);
    }

    private static void _cancelWork(final boolean useHandler) {
        if (isWorkScheduled(false) || isWorkScheduled(true)) {
            try {
                waitForFinish(false);
                waitForFinish(true);

                if (useHandler) {
                    PPApplicationStatic.cancelWork(WORK_TAG, false);
                    PPApplicationStatic.cancelWork(WORK_TAG_SHORT, false);
                } else {
                    PPApplicationStatic._cancelWork(WORK_TAG, false);
                    PPApplicationStatic._cancelWork(WORK_TAG_SHORT, false);
                }

            } catch (Exception e) {
                //Log.e("SearchCalendarEventsWorker._cancelWork", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    private static void waitForFinish(boolean shortWork) {
        if (!isWorkRunning(shortWork)) {
            return;
        }

        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    long start = SystemClock.uptimeMillis();
                    do {

                        ListenableFuture<List<WorkInfo>> statuses;
                        if (shortWork)
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                        else
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                        boolean allFinished = true;
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (state == WorkInfo.State.RUNNING) {
                                    allFinished = false;
                                    break;
                                }
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
                        }
                        if (allFinished) {
                            break;
                        }

                        GlobalUtils.sleep(200);
                    } while (SystemClock.uptimeMillis() - start < 10 * 1000);

                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

    static void cancelWork(final boolean useHandler) {
        _cancelWork(useHandler);
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    private static boolean isWorkRunning(boolean shortWork) {
        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
                            break;
                        }
                        return running;
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.isWorkRunning", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return false;
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static boolean isWorkScheduled(boolean shortWork) {
        try {
            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                            break;
                        }
                        return running;
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.isWorkScheduled", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return false;
        }
    }

}
