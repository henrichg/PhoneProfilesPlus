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
        return Result.success();
}

}
