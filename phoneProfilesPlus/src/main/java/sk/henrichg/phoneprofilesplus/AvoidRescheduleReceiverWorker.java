package sk.henrichg.phoneprofilesplus;

import android.content.Context;
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

// https://issuetracker.google.com/issues/115575872#comment16
// https://github.com/AndroidDeveloperLB/CommonUtils/blob/main/library/src/main/java/com/lb/common_utils/WorkerManagerUtils.kt

public class AvoidRescheduleReceiverWorker extends Worker {

    public AvoidRescheduleReceiverWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER] AvoidRescheduleReceiverWorker.doWork", "--------------- START");
//            PPApplicationStatic.logE("[MAIN_WORKER_CALL] AvoidRescheduleReceiverWorker.doWork", "**************");

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG)
                            //.setInitialDelay(1500, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplicationStatic.logE("[WORKER_CALL] AvoidRescheduleReceiverWorker.doWork", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER] AvoidRescheduleReceiverWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("AvoidRescheduleReceiverWorker.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static void enqueueWork() {
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

                boolean running = false;
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                        break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("AvoidRescheduleReceiverWorker.enqueueUniqueWork", Log.getStackTraceString(e));
                }

                if (!running) {
                    OneTimeWorkRequest avoidRescheduleReceiverWorker =
                            new OneTimeWorkRequest.Builder(AvoidRescheduleReceiverWorker.class)
                                    .addTag(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG)
                                    .setInitialDelay(30 * 3, TimeUnit.DAYS)
                                    .build();

//                    PPApplicationStatic.logE("[WORKER_CALL] AvoidRescheduleReceiverWorker.enqueueWork", "xxx");
                    workManager.enqueueUniqueWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG, ExistingWorkPolicy.REPLACE, avoidRescheduleReceiverWorker);
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
