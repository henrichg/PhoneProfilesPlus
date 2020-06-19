package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
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

@SuppressWarnings("WeakerAccess")
public class SearchCalendarEventsWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "SearchCalendarEventsJob";

    public SearchCalendarEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("SearchCalendarEventsWorker.doWork", "---------------------------------------- START");

            //CallsCounter.logCounter(context, "SearchCalendarEventsWorker.doWork", "SearchCalendarEventsWorker_doWork");

            if (!PPApplication.getApplicationStarted(true)) {
                // application is not started
                //PPApplication.logE("SearchCalendarEventsWorker.doWork", "---------------------------------------- END");
                return Result.success();
            }

            if (Event.getGlobalEventsRunning()) {
                // start events handler
                PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=SearchCalendarEventsWorker.doWork");

                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS);

                PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=SearchCalendarEventsWorker.doWork");
            }

            //PPApplication.logE("SearchCalendarEventsWorker.doWork - handler", "schedule work");
            scheduleWork(false, /*null,*/ false);

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
            Log.e("SearchCalendarEventsWorker.doWork", Log.getStackTraceString(e));
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
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG, ExistingWorkPolicy.KEEP, workRequest);
                    } else {
                        //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "start now work");
                        waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG)
                                .build();
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG, ExistingWorkPolicy.KEEP, workRequest);
                    }

                    //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "---------------------------------------- END");
                }
            }
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void scheduleWork(final boolean useHandler, /*final Handler _handler,*/ final boolean shortInterval) {
        //PPApplication.logE("SearchCalendarEventsWorker.scheduleWork", "shortInterval="+shortInterval);

        if (useHandler/* && (_handler == null)*/) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _scheduleWork(shortInterval);
                }
            });
        }
        else {
            _scheduleWork(shortInterval);
        }
    }

    private static void _cancelWork() {
        if (isWorkScheduled()) {
            try {
                waitForFinish();

                PhoneProfilesService.cancelWork(WORK_TAG);

                //PPApplication.logE("SearchCalendarEventsWorker._cancelWork", "CANCELED");

            } catch (Exception e) {
                Log.e("SearchCalendarEventsWorker._cancelWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private static void waitForFinish() {
        if (!isWorkRunning()) {
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

                        ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(WORK_TAG);
                        boolean allFinished = true;
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
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

                        //try { Thread.sleep(100); } catch (InterruptedException e) { }
                        SystemClock.sleep(100);
                    } while (SystemClock.uptimeMillis() - start < 5 * 1000);

                    //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "END WAIT FOR FINISH");
                }
            }
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
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
                    _cancelWork();
                }
            });
        }
        else {
            _cancelWork();
        }
    }

    private static boolean isWorkRunning() {
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                        //return workInfoList.size() != 0;
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
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
            Log.e("SearchCalendarEventsWorker.isWorkRunning", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

    static boolean isWorkScheduled() {
        //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "xxx");
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                        //return workInfoList.size() != 0;
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
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
            Log.e("SearchCalendarEventsWorker.isWorkScheduled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

}
