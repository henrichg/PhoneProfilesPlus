package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
public class DisableBlockProfileEventActionWorker extends Worker {

    static final String WORK_TAG = "setBlockProfileEventsActionWork";

    @SuppressWarnings("unused")
    public DisableBlockProfileEventActionWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
/*        long start = System.currentTimeMillis();
        PPApplication.logE("[IN_WORKER] DisableBlockProfileEventActionWorker.doWork", "--------------- START");

        try {
            PPApplication.blockProfileEventActions = false;

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER] DisableBlockProfileEventActionWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
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
        }
*/
        return Result.success();
}

/*
    static void enqueueWork() {
        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(DisableBlockProfileEventActionWorker.class)
                        .addTag(DisableBlockProfileEventActionWorker.WORK_TAG)
                        .setInitialDelay(30, TimeUnit.SECONDS)
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

//                    PPApplication.logE("[WORKER_CALL] DisableBlockProfileEventActionWorker.enqueueWork", "xxx");
                    workManager.enqueueUniqueWork(DisableBlockProfileEventActionWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
*/
}
