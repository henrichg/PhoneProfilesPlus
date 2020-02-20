package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

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
        try {
            //PPApplication.logE("DisableInternalChangeWorker.doWork", "xxx");

            RingerModeChangeReceiver.internalChange = false;

            return Result.success();
        } catch (Exception e) {
            Log.e("DisableInternalChangeWorker.doWork", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
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
