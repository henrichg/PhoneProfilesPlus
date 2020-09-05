package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
@SuppressWarnings("unused")
public class DelayedWorksWorker extends Worker {

    public DelayedWorksWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
//        PPApplication.logE("[WORKER CALL]  DelayedWorksWorker.doWork", "xxxx");

        return Result.success();
    }

}
