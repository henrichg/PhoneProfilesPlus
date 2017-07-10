package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

class GeofenceScannerJob extends Job {

    static final String JOB_TAG  = "GeofenceScannerJob";
    static final String JOB_TAG_START  = "GeofenceScannerJob_start";
    private static GeofenceScannerAlarmBroadcastReceiver broadcastReceiver = new GeofenceScannerAlarmBroadcastReceiver();
    private static boolean isBroadcastSend = false;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("GeofenceScannerJob.onRunJob", "xxx");

        sendBroadcast(getContext());
        return Result.SUCCESS;
    }

    static void scheduleJob(Context context, boolean startScanning, boolean forScreenOn) {
        PPApplication.logE("GeofenceScannerJob.scheduleJob", "startScanning="+startScanning);

        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {

            if (startScanning)
                PhoneProfilesService.geofencesScanner.mUpdatesStarted = false;

            JobRequest.Builder jobBuilder;
            if (!startScanning) {
                JobManager jobManager = JobManager.instance();
                jobManager.cancelAllForTag(JOB_TAG_START);

                int updateDuration = 30;
                int interval;
                if (PhoneProfilesService.geofencesScanner.mUpdatesStarted) {
                    interval = ApplicationPreferences.applicationEventLocationUpdateInterval(context) * 60;
                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
                    if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("1"))
                        interval = 2 * interval;
                    interval = interval - updateDuration;
                } else {
                    interval = updateDuration;
                }

                jobBuilder = new JobRequest.Builder(JOB_TAG);

                if (TimeUnit.SECONDS.toMillis(interval) < JobRequest.MIN_INTERVAL) {
                    jobManager.cancelAllForTag(JOB_TAG);
                    jobBuilder.setExact(TimeUnit.MINUTES.toMillis(interval));
                } else {
                    int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
                    PPApplication.logE("GeofenceScannerJob.scheduleJob", "requestsForTagSize=" + requestsForTagSize);
                    if (requestsForTagSize == 0) {
                        if (TimeUnit.SECONDS.toMillis(interval) < JobRequest.MIN_INTERVAL)
                            jobBuilder.setPeriodic(JobRequest.MIN_INTERVAL);
                        else
                            jobBuilder.setPeriodic(TimeUnit.SECONDS.toMillis(interval));
                    } else
                        return;
                }
            } else {
                cancelJob();
                jobBuilder = new JobRequest.Builder(JOB_TAG_START);
                if (forScreenOn)
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
                else
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
            }

            PPApplication.logE("GeofenceScannerJob.scheduleJob", "build and schedule");

            jobBuilder
                    .setPersisted(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();

        }
        else
            PPApplication.logE("GeofenceScannerJob.scheduleJob", "scanner is not started");
    }

    static void cancelJob() {
        PPApplication.logE("GeofenceScannerJob.cancelJob", "xxx");

        JobManager jobManager = JobManager.instance();
        jobManager.cancelAllForTag(JOB_TAG_START);
        jobManager.cancelAllForTag(JOB_TAG);
    }

    static boolean isJobScheduled() {
        PPApplication.logE("GeofenceScannerJob.isJobScheduled", "xxx");

        JobManager jobManager = JobManager.instance();
        return (jobManager.getAllJobRequestsForTag(JOB_TAG).size() != 0) ||
                (jobManager.getAllJobRequestsForTag(JOB_TAG_START).size() != 0);
    }

    static void sendBroadcast(Context context)
    {
        PPApplication.logE("GeofenceScannerJob.sendBroadcast", "xxx");

        if (!isBroadcastSend) {
            isBroadcastSend = true;
            LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("GeofenceScannerAlarmBroadcastReceiver"));
            Intent intent = new Intent("GeofenceScannerAlarmBroadcastReceiver");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    static void unregisterReceiver(Context context) {
        PPApplication.logE("GeofenceScannerJob.unregisterReceiver", "xxx");

        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(broadcastReceiver);
        isBroadcastSend = false;
    }

}
