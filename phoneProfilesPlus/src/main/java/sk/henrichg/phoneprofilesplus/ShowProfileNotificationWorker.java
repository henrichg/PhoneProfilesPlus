package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("unused")
public class ShowProfileNotificationWorker extends Worker {

//    final Context context;

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
/*        try {
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

                        boolean clear = false;
                        if (Build.MANUFACTURER.equals("HMD Global"))
                            // clear it for redraw icon in "Glance view" for "HMD Global" mobiles
                            clear = true;
                        if (PPApplication.deviceIsLG && (!Build.MODEL.contains("Nexus")) && (Build.VERSION.SDK_INT == 28))
                            // clear it for redraw icon in "Glance view" for LG with Android 9
                            clear = true;
                        if (clear) {
                            // next show will be with startForeground()
                            PhoneProfilesService.getInstance().clearProfileNotification();
                            PPApplication.sleep(100);
                        }

                        if (PhoneProfilesService.getInstance() != null) {
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                            PhoneProfilesService.getInstance()._showProfileNotification(dataWrapper, false);
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
        }*/

        return Result.success();
    }
}
