package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class DisableInternalChangeWorker extends Worker {

    public DisableInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        PPApplication.logE("[HANDLER] DisableInternalChangeWorker.doWork", "xxx");

        RingerModeChangeReceiver.internalChange = false;

        return Result.success();
    }

}
