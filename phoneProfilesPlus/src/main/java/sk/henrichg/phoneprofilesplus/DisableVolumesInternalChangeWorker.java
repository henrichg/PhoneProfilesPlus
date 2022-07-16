package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisableVolumesInternalChangeWorker extends Worker {

    static final String WORK_TAG = "disableVolumesInternalChangeWork";

    @SuppressWarnings("unused")
    public DisableVolumesInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        /*try {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_WORKER]  DisableVolumesInternalChangeWorker.doWork", "--------------- START");

//            PPApplication.logE("[VOLUMES] DisableVolumesInternalChangeWorker.doWork", "internaChange=FALSE");
            EventPreferencesVolumes.internalChange = false;

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER]  DisableVolumesInternalChangeWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);*/
            return Result.success();
        /*} catch (Exception e) {
            //Log.e("DisableVolumesInternalChangeWorker.doWork", Log.getStackTraceString(e));
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
    }

    static void enqueueWork() {
        PPApplication.logE("[EXECUTOR_CALL]  ***** DisableVolumesInternalChangeWorker.enqueueWork", "schedule");

        final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
            PPApplication.logE("[IN_EXECUTOR]  ***** DisableVolumesInternalChangeWorker.executor", "--------------- START");
            EventPreferencesVolumes.internalChange = false;
            PPApplication.logE("[IN_EXECUTOR]  ***** DisableVolumesInternalChangeWorker.executor", "--------------- END");
            worker.shutdown();
        };
        worker.schedule(runnable, 5, TimeUnit.SECONDS);

        /*
        OneTimeWorkRequest disableInternalChangeWorker =
                new OneTimeWorkRequest.Builder(DisableVolumesInternalChangeWorker.class)
                        .addTag(DisableVolumesInternalChangeWorker.WORK_TAG)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                    //if (PPApplication.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(DisableVolumesInternalChangeWorker.WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[TEST BATTERY] DisableVolumesInternalChangeWorker.enqueueWork", "for=" + DisableInternalChangeWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                    } catch (Exception ignored) {
//                    }
//                    //}

//                    PPApplication.logE("[WORKER_CALL] DisableVolumesInternalChangeWorker.enqueueWork", "xxx");
                    workManager.enqueueUniqueWork(DisableVolumesInternalChangeWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        */
    }
}
