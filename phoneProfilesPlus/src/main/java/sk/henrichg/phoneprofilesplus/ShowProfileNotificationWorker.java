package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ShowProfileNotificationWorker extends Worker {

    final Context context;

    static final String WORK_TAG = "showProfileNotificationWork";

    public ShowProfileNotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            PPApplication.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "xxxx");

            synchronized (PPApplication.applicationPreferencesMutex) {
                if (PPApplication.doNotShowProfileNotification)
                    return Result.success();
            }

            Context appContext = context.getApplicationContext();

            if (PhoneProfilesService.getInstance() != null) {
                try {
                    if (PhoneProfilesService.getInstance() != null) {
//                        PPApplication.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "call of _showProfileNotification()");
//                        PPApplication.logE("[IN_WORKER] ShowProfileNotificationWorker.doWork", "Build.MODEL="+Build.MODEL);

                        PhoneProfilesService.clearOldProfileNotification();

                        if (PhoneProfilesService.getInstance() != null) {
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, DataWrapper.IT_FOR_NOTIFICATION, 0f);
                            PhoneProfilesService.getInstance()._showProfileNotification(/*profile,*/ dataWrapper, false/*, clear*/);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            return Result.success();
        } catch (Exception e) {
            PPApplication.recordException(e);
            return Result.failure();
        }
    }
}
