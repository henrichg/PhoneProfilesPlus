package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// https://issuetracker.google.com/issues/115575872#comment16

@SuppressWarnings("WeakerAccess")
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
            //PPApplication.logE("AvoidRescheduleReceiverWorker.doWork", "xxx");
            return Result.success();
        } catch (Exception e) {
            //Log.e("AvoidRescheduleReceiverWorker.doWork", Log.getStackTraceString(e));
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

}
