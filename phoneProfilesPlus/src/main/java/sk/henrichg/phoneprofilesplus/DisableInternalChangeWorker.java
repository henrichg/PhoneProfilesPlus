package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class DisableInternalChangeWorker extends Worker {

    private final Context context;

    public DisableInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        PPApplication.logE("DisableInternalChangeWorker.doWork", "xxx");

        RingerModeChangeReceiver.internalChange = false;

        return Result.success();
    }

}
