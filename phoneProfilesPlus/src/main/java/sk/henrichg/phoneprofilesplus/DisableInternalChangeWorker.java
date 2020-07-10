package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class DisableInternalChangeWorker extends Worker {

    static final String WORK_TAG = "disableInternalChangeWork";

    public DisableInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("DisableInternalChangeWorker.doWork", "xxx");

            /*if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            boolean foundEnqueued = false;

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosByTag(WORK_TAG);
                //PPApplication.logE("DisableInternalChangeWorker.doWork", "statuses="+statuses);
                //noinspection TryWithIdenticalCatches
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    //PPApplication.logE("DisableInternalChangeWorker.doWork", "workInfoList="+workInfoList.size());
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

            //PPApplication.logE("DisableInternalChangeWorker.doWork", "foundEnqueued="+foundEnqueued);

            if (!foundEnqueued)*/
                RingerModeChangeReceiver.internalChange = false;

            return Result.success();
        } catch (Exception e) {
            //Log.e("DisableInternalChangeWorker.doWork", Log.getStackTraceString(e));
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

    static void enqueueWork() {
        //PPApplication.logE("DisableInternalChangeWorker.enqueueWork", "xxx");
        OneTimeWorkRequest disableInternalChangeWorker =
                new OneTimeWorkRequest.Builder(DisableInternalChangeWorker.class)
                        .addTag(DisableInternalChangeWorker.WORK_TAG)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    /*boolean foundEnqueued = false;
                    ListenableFuture<List<WorkInfo>> statuses;
                    statuses = workManager.getWorkInfosByTag(WORK_TAG);
                    //PPApplication.logE("DisableInternalChangeWorker.doWork", "statuses="+statuses);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        //PPApplication.logE("DisableInternalChangeWorker.doWork", "workInfoList="+workInfoList);
                        PPApplication.logE("DisableInternalChangeWorker.enqueueWork", "workInfoList="+workInfoList.size());
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
                    if (foundEnqueued)
                        PPApplication.cancelWork(WORK_TAG);*/
                    //workManager.enqueue(disableInternalChangeWorker);
                    workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, disableInternalChangeWorker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
}
