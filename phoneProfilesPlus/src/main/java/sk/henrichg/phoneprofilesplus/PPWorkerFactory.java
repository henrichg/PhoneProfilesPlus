package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

public class PPWorkerFactory extends WorkerFactory {

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {
        //Log.e("PPWorkerFactory.createWorker", "workerClassName="+workerClassName);
        if (workerClassName.contains("ElapsedAlarmsWorker"))
            return new MainWorker(appContext, workerParameters);
        if (workerClassName.contains("DelayedWorksWorker"))
            return new MainWorker(appContext, workerParameters);
        if (workerClassName.contains("WorkerWithData"))
            return new MainWorker(appContext, workerParameters);
        if (workerClassName.contains("WorkerWithoutData"))
            return new MainWorker(appContext, workerParameters);
        else
            return null;
    }

}
