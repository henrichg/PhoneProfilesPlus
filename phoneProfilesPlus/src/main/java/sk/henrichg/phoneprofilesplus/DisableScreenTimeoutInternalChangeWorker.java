package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisableScreenTimeoutInternalChangeWorker extends Worker {

    static final String WORK_TAG = "disableScreenTimeoutInternalChangeWork";

    public DisableScreenTimeoutInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_WORKER]  DisableScreenTimeoutInternalChangeWorker.doWork", "--------------- START");

            /*if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            boolean foundEnqueued = false;

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        if (state == WorkInfo.State.ENQUEUED) {
                            // any work is already enqueued, is not needed to enqueue new
                            foundEnqueued = true;
                            break;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //PPApplication.logE("DisableScreenTimeoutInternalChangeWorker.doWork", "foundEnqueued="+foundEnqueued);

            if (!foundEnqueued)*/
                ActivateProfileHelper.disableScreenTimeoutInternalChange = false;

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER]  DisableScreenTimeoutInternalChangeWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("DisableScreenTimeoutInternalChangeWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
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

    static void enqueueWork() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** DisableScreenTimeoutInternalChangeWorker.enqueueWork", "schedule");

        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("[IN_EXECUTOR]  ***** DisableScreenTimeoutInternalChangeWorker.executor", "--------------- START");
                ActivateProfileHelper.disableScreenTimeoutInternalChange = false;
                PPApplication.logE("[IN_EXECUTOR]  ***** DisableScreenTimeoutInternalChangeWorker.executor", "--------------- END");
            }
        };
        worker.schedule(runnable, 5, TimeUnit.SECONDS);
        worker.shutdown();

        /*
        OneTimeWorkRequest disableInternalChangeWorker =
                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                        .addTag(DisableInternalChangeWorker.WORK_TAG)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                    //if (PPApplication.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(DisableInternalChangeWorker.WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[TEST BATTERY] DisableInternalChangeWorker.enqueueWork", "for=" + DisableInternalChangeWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                    } catch (Exception ignored) {
//                    }
//                    //}

//                    PPApplication.logE("[WORKER_CALL] DisableInternalChangeWorker.enqueueWork", "xxx");
                    workManager.enqueueUniqueWork(DisableInternalChangeWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        */
    }

}
