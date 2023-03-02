package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
@SuppressWarnings("unused")
public class ShowProfileNotificationWorker extends Worker {

    //final Context context;

    static final String WORK_TAG = "showProfileNotificationWork";

    public ShowProfileNotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        //this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        /*try {
            long start = System.currentTimeMillis();
            PPApplicationStatic.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "--------------- START");

            synchronized (PPApplication.applicationPreferencesMutex) {
                if (PPApplication.doNotShowProfileNotification) {
//                    long finish = System.currentTimeMillis();
//                    long timeElapsed = finish - start;
                    PPApplicationStatic.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "--------------- END - doNotShowProfileNotification");
                    return Result.success();
                }
            }

            Context appContext = context.getApplicationContext();

            if (PhoneProfilesService.getInstance() != null) {
                try {
//                        PPApplicationStatic.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "call of _showProfileNotification()");
//                        PPApplicationStatic.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "Build.MODEL="+Build.MODEL);

                    PhoneProfilesService.clearOldProfileNotification();

                    if (PhoneProfilesService.getInstance() != null) {
                        synchronized (PPApplication.showPPPNotificationMutex) {
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0, 0f);
                            PhoneProfilesService.getInstance()._showProfileNotification(dataWrapper, false);
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplicationStatic.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);*/
            return Result.success();
        /*} catch (Exception e) {
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }*/
    }
}
