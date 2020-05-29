package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class DisableScreenTimeoutInternalChangeWorker extends Worker {

    public DisableScreenTimeoutInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("DisableInternalChangeWorker.doWork", "xxx");
            Log.e("DisableScreenTimeoutInternalChangeWorker.doWork", "xxx");

            ActivateProfileHelper.disableScreenTimeoutInternalChange = false;

            return Result.success();
        } catch (Exception e) {
            Log.e("DisableScreenTimeoutInternalChangeWorker.doWork", Log.getStackTraceString(e));
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
