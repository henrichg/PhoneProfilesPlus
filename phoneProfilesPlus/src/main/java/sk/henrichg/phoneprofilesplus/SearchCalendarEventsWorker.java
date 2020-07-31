package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

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

@SuppressWarnings("WeakerAccess")
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
        try {
            //PPApplication.logE("[WORKER CALL]  SearchCalendarEventsWorker.doWork", "xxxx");

            //PPApplication.logE("SearchCalendarEventsWorker.doWork", "---------------------------------------- START");

            //CallsCounter.logCounter(context, "SearchCalendarEventsWorker.doWork", "SearchCalendarEventsWorker_doWork");

            if (!PPApplication.getApplicationStarted(true)) {
                // application is not started
                //PPApplication.logE("SearchCalendarEventsWorker.doWork", "---------------------------------------- END");
                return Result.success();
            }

            if (Event.getGlobalEventsRunning()) {
                // start events handler
                //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=SearchCalendarEventsWorker.doWork");

//                PPApplication.logE("[EVENTS_HANDLER] SearchCalendarEventsWorker.doWork", "sensorType=SENSOR_TYPE_SEARCH_CALENDAR_EVENTS");
                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS);

                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=SearchCalendarEventsWorker.doWork");
            }

            //PPApplication.logE("SearchCalendarEventsWorker.doWork - handler", "schedule work");
            //scheduleWork(false);
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG)
                            //.setInitialDelay(200, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker.doWork", "for=" + MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            /*PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("SearchCalendarEventsWorker.doWork - handler", "schedule work");
                    scheduleWork(context, false, null, false);
                }
            }, 500);*/

            //PPApplication.logE("SearchCalendarEventsWorker.doWork", "---------------------------------------- END");

            return Result.success();
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    public void onStopped () {
        //PPApplication.logE("SearchCalendarEventsWorker.onStopped", "xxx");

        //CallsCounter.logCounter(context, "SearchCalendarEventsWorker.onStopped", "SearchCalendarEventsWorker_onStopped");
    }

    private static void _scheduleWork(final boolean shortInterval) {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "---------------------------------------- START");
                        PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "shortInterval=" + shortInterval);
                    }*/

                    if (!shortInterval) {
                        //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "delay work");
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .setInitialDelay(24, TimeUnit.HOURS)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG)
                                .build();

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker._scheduleWork", "for=" + SearchCalendarEventsWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    } else {
                        //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "start now work");
                        //waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG_SHORT)
                                .build();

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker._scheduleWork", "for=" + SearchCalendarEventsWorker.WORK_TAG_SHORT + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    }

                    //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "---------------------------------------- END");
                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void scheduleWork(final boolean shortInterval) {
        //PPApplication.logE("SearchCalendarEventsWorker.scheduleWork", "shortInterval="+shortInterval);

        PPApplication.startHandlerThreadPPScanners();
        final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        //if (shortInterval) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadPPScanners", "START run - from=SearchCalendarEventsWorker.scheduleWork" + " shortInterval="+shortInterval);
                    _scheduleWork(shortInterval);
                }
            });
        /*}
        else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadPPScanners", "START run - from=SearchCalendarEventsWorker.scheduleWork");
                    _scheduleWork(shortInterval);
                }
            }, 500);
        }*/
    }

    private static void _cancelWork() {
        if (isWorkScheduled(false) || isWorkScheduled(true)) {
            try {
                waitForFinish(false);
                waitForFinish(true);

                PPApplication.cancelWork(WORK_TAG);
                PPApplication.cancelWork(WORK_TAG_SHORT);

                //PPApplication.logE("SearchCalendarEventsWorker._cancelWork", "CANCELED");

            } catch (Exception e) {
                //Log.e("SearchCalendarEventsWorker._cancelWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private static void waitForFinish(boolean shortWork) {
        if (!isWorkRunning(shortWork)) {
            //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "NOT RUNNING");
            return;
        }

        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "START WAIT FOR FINISH");
                    long start = SystemClock.uptimeMillis();
                    do {

                        ListenableFuture<List<WorkInfo>> statuses;
                        if (shortWork)
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                        else
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                        boolean allFinished = true;
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            //PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker.waitForFinish", "workInfoList.size()="+workInfoList.size());
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (!state.isFinished()) {
                                    allFinished = false;
                                    break;
                                }
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (allFinished) {
                            //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "FINISHED");
                            break;
                        }

                        PPApplication.sleep(500);
                    } while (SystemClock.uptimeMillis() - start < 10 * 1000);

                    //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "END WAIT FOR FINISH");
                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void cancelWork(@SuppressWarnings("SameParameterValue") final boolean useHandler/*,
                           final Handler _handler*/) {
        //PPApplication.logE("SearchCalendarEventsWorker.cancelWork", "xxx");

        if (useHandler /*&& (_handler == null)*/) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThreadPPScanners", "START run - from=SearchCalendarEventsWorker.cancelWork");
                    _cancelWork();
                }
            });
        }
        else {
            _cancelWork();
        }
    }

    private static boolean isWorkRunning(boolean shortWork) {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        //PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker.isWorkRunning", "workInfoList.size()="+workInfoList.size());
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
            PPApplication.recordException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(boolean shortWork) {
        //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "xxx");
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        //PPApplication.logE("[TEST BATTERY] SearchCalendarEventsWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
            PPApplication.recordException(e);
            return false;
        }
    }

}
