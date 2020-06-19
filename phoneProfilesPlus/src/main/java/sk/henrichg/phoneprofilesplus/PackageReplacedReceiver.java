package sk.henrichg.phoneprofilesplus;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class PackageReplacedReceiver extends BroadcastReceiver {

    //static final String EXTRA_RESTART_SERVICE = "restart_service";

    static final String PACKAGE_REPLACED_WORK_TAG = "packageReplacedWork";

    @Override
    public void onReceive(Context context, Intent intent) {
        //CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            int size = jobScheduler.getAllPendingJobs().size();
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "jobScheduler.getAllPendingJobs().size()="+size);
            //jobScheduler.cancelAll();
        }

        PhoneProfilesService.cancelAllWorks(true);

        // https://issuetracker.google.com/issues/115575872#comment16
        PPApplication.logE("##### PackageReplacedReceiver.onReceive", "avoidRescheduleReceiverWorker START of enqueue");
        PhoneProfilesService.cancelWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
        OneTimeWorkRequest avoidRescheduleReceiverWorker =
                new OneTimeWorkRequest.Builder(AvoidRescheduleReceiverWorker.class)
                        .addTag(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG)
                        .setInitialDelay(365 * 10, TimeUnit.DAYS)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            //PPApplication.logE("##### PPApplication.onCreate", "workManager="+workManager);
            if (workManager != null)
                workManager.enqueueUniqueWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG, ExistingWorkPolicy.KEEP, avoidRescheduleReceiverWorker);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        PPApplication.logE("##### PackageReplacedReceiver.onReceive", "avoidRescheduleReceiverWorker END of enqueue");


        PPApplication.applicationPackageReplaced = true;

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            PPApplication.setBlockProfileEventActions(true);

            //final Context appContext = context.getApplicationContext();

            /*SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(appContext);
            if (sharedPreferences != null) {
                PPApplication.logE("--------------- PackageReplacedReceiver.onReceive", "package replaced set to true");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, true);
                editor.apply();
            }*/

            //final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

            PPApplication.logE("PackageReplacedReceiver.onReceive", "start work for package replaced");
            //PPApplication.logE("PackageReplacedReceiver.onReceive", "start of delayed work");

            PhoneProfilesService.cancelWork(PackageReplacedReceiver.PACKAGE_REPLACED_WORK_TAG);

            // work for package replaced
            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_PACKAGE_REPLACED)
                    //.putBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, restartService)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                            .addTag(PackageReplacedReceiver.PACKAGE_REPLACED_WORK_TAG)
                            .setInputData(workData)
                            //.setInitialDelay(5, TimeUnit.SECONDS)
                            .build();
            try {
                // do not test start of PPP, because is not started in this receiver
                //if (PPApplication.getApplicationStarted(true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null)
                        workManager.enqueueUniqueWork(PackageReplacedReceiver.PACKAGE_REPLACED_WORK_TAG, ExistingWorkPolicy.KEEP, worker);
                //}
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

}
