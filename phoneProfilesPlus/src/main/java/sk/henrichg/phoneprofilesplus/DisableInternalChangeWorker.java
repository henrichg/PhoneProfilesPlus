package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisableInternalChangeWorker extends Worker {

    static final String WORK_TAG = "disableInternalChangeWork";

    @SuppressWarnings("unused")
    public DisableInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        /*try {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_WORKER]  DisableInternalChangeWorker.doWork", "--------------- START");

//            PPApplication.logE("[VOLUMES] DisableInternalChangeWorker.doWork", "internaChange=FALSE");
            RingerModeChangeReceiver.internalChange = false;

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER]  DisableInternalChangeWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("DisableInternalChangeWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            //Handler _handler = new Handler(getApplicationContext().getMainLooper());
            //Runnable r = new Runnable() {
            //    public void run() {
            //        android.os.Process.killProcess(PPApplication.pid);
            //    }
            //};
            //_handler.postDelayed(r, 1000);

            return Result.failure();
        }*/
        return Result.success();
    }

    static void enqueueWork() {
//        PPApplication.logE("[EXECUTOR_CALL]  ***** DisableInternalChangeWorker.enqueueWork", "schedule");

        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            PPApplication.logE("[IN_EXECUTOR]  ***** DisableInternalChangeWorker.executor", "--------------- START");
            RingerModeChangeReceiver.internalChange = false;
//            PPApplication.logE("[IN_EXECUTOR]  ***** DisableInternalChangeWorker.executor", "--------------- END");
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
