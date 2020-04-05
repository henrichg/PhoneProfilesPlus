package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class PackageReplacedReceiver extends BroadcastReceiver {

    //static final String EXTRA_RESTART_SERVICE = "restart_service";

    @Override
    public void onReceive(Context context, Intent intent) {
        //CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        PPApplication.applicationPackageReplaced = true;

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            PPApplication.setBlockProfileEventActions(true, context);

            final Context appContext = context.getApplicationContext();

            /*SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(appContext);
            if (sharedPreferences != null) {
                PPApplication.logE("--------------- PackageReplacedReceiver.onReceive", "package replaced set to true");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, true);
                editor.apply();
            }*/

            //final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

            PPApplication.logE("PackageReplacedReceiver.onReceive", "called work for package replaced");
            //PPApplication.logE("PackageReplacedReceiver.onReceive", "start of delayed work");

            // work for package replaced
            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_PACKAGE_REPLACED)
                    //.putBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, restartService)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                            .addTag("packageReplacedWork")
                            .setInputData(workData)
                            //.setInitialDelay(5, TimeUnit.SECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance(appContext);
                workManager.enqueueUniqueWork("packageReplacedWork", ExistingWorkPolicy.REPLACE, worker);
            } catch (Exception ignored) {
            }
        }
    }

}
