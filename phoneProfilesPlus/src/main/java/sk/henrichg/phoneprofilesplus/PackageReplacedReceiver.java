package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class PackageReplacedReceiver extends BroadcastReceiver {

    //static final String EXTRA_RESTART_SERVICE = "restart_service";

    @Override
    public void onReceive(Context context, Intent intent) {
        //CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            String text = context.getString(R.string.app_name) + " " + context.getString(R.string.application_is_starting_toast);
            PPApplication.showToast(context.getApplicationContext(), text, Toast.LENGTH_SHORT);

            PPApplication.setBlockProfileEventActions(true, context);

            final Context appContext = context.getApplicationContext();

            /*SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(appContext);
            if (sharedPreferences != null) {
                PPApplication.logE("--------------- PackageReplacedReceiver.onReceive", "package replaced set to true");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, true);
                editor.apply();
            }*/
            PPApplication.applicationPackageReplaced = true;

            //final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

            PPApplication.logE("PackageReplacedReceiver.onReceive", "called work for package replaced");
            PPApplication.logE("PackageReplacedReceiver.onReceive", "start of delayed work");

            // work for package replaced
            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_PACKAGE_REPLACED)
                    //.putBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, restartService)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                            .addTag("packageReplacedWork")
                            .setInputData(workData)
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance(appContext);
                workManager.enqueueUniqueWork("packageReplacedWork", ExistingWorkPolicy.REPLACE, worker);
            } catch (Exception ignored) {
            }

            /*
            PPApplication.startHandlerThread("PackageReplacedReceiver.onReceive");
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedReceiver.onReceive - handler", "xxx");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver_onReceive_1");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PackageReplacedReceiver.onReceive");

                        //PhoneProfilesService instance = PhoneProfilesService.getInstance();
                        //if (instance != null) {
                        //    if (!instance.getWaitForEndOfStart()) {
                            //if (ApplicationPreferences.applicationPackageReplaced(appContext)) {
                            if (PPApplication.applicationPackageReplaced) {
                                PPApplication.logE("PackageReplacedReceiver.onReceive", "called work for package replaced");

                                // work for package replaced
                                Data workData = new Data.Builder()
                                        .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_PACKAGE_REPLACED)
                                        //.putBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, restartService)
                                        .build();

                                OneTimeWorkRequest worker =
                                        new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                .addTag("packageReplacedWork")
                                                .setInputData(workData)
                                                .setInitialDelay(5, TimeUnit.SECONDS)
                                                .build();
                                try {
                                    WorkManager workManager = PPApplication.getWorkManagerInstance(appContext);
                                    workManager.enqueueUniqueWork("packageReplacedWork", ExistingWorkPolicy.REPLACE, worker);
                                } catch (Exception ignored) {
                                }
                            }
                        //}
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });
            */
        }
    }

}
