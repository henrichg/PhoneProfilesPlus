package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateGUIWorker extends Worker {

    final Context context;

    static final String WORK_TAG = "updateGUIWork";

    public UpdateGUIWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context appContext = context.getApplicationContext();

            PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);

            return Result.success();
        } catch (Exception e) {
            PPApplication.recordException(e);
            return Result.failure();
        }
    }

}
