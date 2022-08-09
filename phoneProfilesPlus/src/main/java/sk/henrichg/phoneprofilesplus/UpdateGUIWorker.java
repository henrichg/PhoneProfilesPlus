package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
@SuppressWarnings("unused")
public class UpdateGUIWorker extends Worker {

    static final String WORK_TAG = "updateGUIWork";

    public UpdateGUIWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        //this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }

}
