package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
@SuppressWarnings("unused")
public class GeofenceScanWorker extends Worker {

    static final String WORK_TAG  = "GeofenceScannerJob";
    static final String WORK_TAG_SHORT  = "GeofenceScannerJobShort";

    public GeofenceScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }

}
