package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

class GeofenceScannerJob extends Job {

    static final String JOB_TAG  = "GeofenceScannerJob";
    static final String JOB_TAG_START  = "GeofenceScannerJob_start";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("GeofenceScannerJob.onRunJob", "xxx");

        Context context = getContext();

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
        if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "any events exists = schedule job");
            //int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
            //if (oneshot == 0)
            GeofenceScannerJob.scheduleJob(context, false, false);
            //setAlarm(context, false, false);
            dataWrapper.invalidateDataWrapper();
        }
        else {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "events not exists = cancel job");
            //removeAlarm(context);
            GeofenceScannerJob.cancelJob();
            dataWrapper.invalidateDataWrapper();
            return Result.SUCCESS;
        }

        if (!PhoneProfilesService.isGeofenceScannerStarted()) {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "geofence scanner is not started = cancel job");
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return Result.SUCCESS;
        }

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
        if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("2")) {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "update in power save mode is not allowed = cancel job");
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return Result.SUCCESS;
        }

        if (Event.getGlobalEventsRuning(context)) {
            if ((PhoneProfilesService.instance != null) && (PhoneProfilesService.geofencesScanner != null)) {
                if (PhoneProfilesService.geofencesScanner.mUpdatesStarted) {
                    PPApplication.logE("GeofenceScannerJob.onRunJob", "loaction updates started - start GeofencesService");

                    //PhoneProfilesService.geofencesScanner.stopLocationUpdates();

                    // start service
                    Intent serviceIntent = new Intent(context, GeofencesService.class);
                    WakefulIntentService.sendWakefulWork(context, serviceIntent);
                } else
                    PPApplication.logE("GeofenceScannerJob.onRunJob", "loaction updates not started - start it");
                    // Fixed: java.lang.NullPointerException: Calling thread must be a prepared Looper thread.
                    //        com.google.android.gms.internal.zzccb.requestLocationUpdates(Unknown Source)
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 PhoneProfilesService.geofencesScanner.startLocationUpdates();
                             }
                         }
                    );
            }
        }

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

                PPApplication.logE("GeofenceScannerJob.scheduleJob", "mUpdatesStarted="+PhoneProfilesService.geofencesScanner.mUpdatesStarted);

                // look at GeofenceScanner:UPDATE_INTERVAL_IN_MILLISECONDS
                int updateDuration = 30;

                int interval;
                if (PhoneProfilesService.geofencesScanner.mUpdatesStarted) {
                    interval = ApplicationPreferences.applicationEventLocationUpdateInterval(context) * 60;
                    PPApplication.logE("GeofenceScannerJob.scheduleJob", "interval="+interval);
                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
                    if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("1"))
                        interval = 2 * interval;
                    //interval = interval - updateDuration;
                } else {
                    interval = updateDuration;
                }

                jobBuilder = new JobRequest.Builder(JOB_TAG);

                if (TimeUnit.SECONDS.toMillis(interval) < JobRequest.MIN_INTERVAL) {
                    jobManager.cancelAllForTag(JOB_TAG);
                    jobBuilder.setExact(TimeUnit.SECONDS.toMillis(interval));
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
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
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

}
