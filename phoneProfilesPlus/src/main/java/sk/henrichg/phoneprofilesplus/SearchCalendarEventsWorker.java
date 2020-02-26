package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS);
            }

            //PPApplication.logE("SearchCalendarEventsWorker.doWork - handler", "schedule work");
            scheduleWork(context.getApplicationContext(), false, null, false);

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
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
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

    private static void _scheduleWork(final Context context, final boolean shortInterval) {
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "---------------------------------------- START");
                PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "shortInterval=" + shortInterval);
            }*/

            if (!shortInterval) {
                //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "delay work");
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                        .setInitialDelay(24, TimeUnit.HOURS)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            } else {
                //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "start now work");
                waitForFinish(context);
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            }

            //PPApplication.logE("SearchCalendarEventsWorker._scheduleWork", "---------------------------------------- END");
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker._scheduleWork", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
        }
    }

    static void scheduleWork(final Context context, final boolean useHandler, final Handler _handler, final boolean shortInterval) {
        //PPApplication.logE("SearchCalendarEventsWorker.scheduleWork", "shortInterval="+shortInterval);

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _scheduleWork(context, shortInterval);
                }
            });
        }
        else {
            _scheduleWork(context, shortInterval);
        }
    }

    private static void _cancelWork(final Context context) {
        if (isWorkScheduled(context)) {
            try {
                waitForFinish(context);

                PhoneProfilesService.cancelWork(WORK_TAG, context);

                //PPApplication.logE("SearchCalendarEventsWorker._cancelWork", "CANCELED");

            } catch (Exception e) {
                Log.e("SearchCalendarEventsWorker._cancelWork", Log.getStackTraceString(e));
                //FirebaseCrashlytics.getInstance().recordException(e);
                Crashlytics.logException(e);
            }
        }
    }

    private static void waitForFinish(Context context) {
        if (!isWorkRunning(context)) {
            //PPApplication.logE("SearchCalendarEventsWorker.waitForFinish", "NOT RUNNING");
            return;
        }

        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);

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
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
        }
    }

    static void cancelWork(final Context context,
                           @SuppressWarnings("SameParameterValue") final boolean useHandler,
                           final Handler _handler) {
        //PPApplication.logE("SearchCalendarEventsWorker.cancelWork", "xxx");

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _cancelWork(context);
                }
            });
        }
        else {
            _cancelWork(context);
        }
    }

    private static boolean isWorkRunning(Context context) {
        try {
            WorkManager instance = PPApplication.getWorkManagerInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
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
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker.isWorkRunning", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(Context context) {
        //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "xxx");
        try {
            WorkManager instance = PPApplication.getWorkManagerInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
                //PPApplication.logE("SearchCalendarEventsWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                //return workInfoList.size() != 0;
                boolean running = false;
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED;
                }
                return running;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e("SearchCalendarEventsWorker.isWorkScheduled", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
            return false;
        }
    }

}
