package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Build;

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
//            PPApplication.logE("[WORKER CALL] ShowProfileNotificationWorker.doWork", "xxxx");

            Context appContext = context.getApplicationContext();

            if ((!PPApplication.doNotShowProfileNotification) &&
                    PhoneProfilesService.getInstance() != null) {
                try {
                    if (PhoneProfilesService.getInstance() != null) {
//                        PPApplication.logE("[WORKER CALL] ShowProfileNotificationWorker.doWork", "call of _showProfileNotification()");

                        boolean clear = false;
                        if (Build.MANUFACTURER.equals("HMD Global"))
                            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
                            clear = true;
                        if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
                            // clear it for redraw icon in "Glance view" for LG with Android 9
                            clear = true;
                        if (clear) {
                            // next show will be with startForeground()
                            PhoneProfilesService.getInstance().clearProfileNotification(/*getApplicationContext(), true*/);
                            PPApplication.sleep(100);
                        }

                        if (PhoneProfilesService.getInstance() != null) {
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
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
