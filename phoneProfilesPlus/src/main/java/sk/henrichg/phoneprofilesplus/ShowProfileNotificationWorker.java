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
            //PPApplication.logE("[WORKER CALL]  ShowProfileNotificationWorker.doWork", "xxxx");

            Context appContext = context.getApplicationContext();

            if ((!PPApplication.doNotShowProfileNotification) &&
                    PhoneProfilesService.getInstance() != null) {
                try {
                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                    Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                    if (PhoneProfilesService.getInstance() != null) {
                        //PPApplication.logE("[APP START] ShowProfileNotificationWorker.doWork", "xxx");
                        PhoneProfilesService.getInstance()._showProfileNotification(profile, dataWrapper, /*false,*/ false/*, cleared*/);
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
