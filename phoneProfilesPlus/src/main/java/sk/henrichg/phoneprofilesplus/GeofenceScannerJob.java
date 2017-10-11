package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

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

        CallsCounter.logCounter(context, "GeofenceScannerJob.onRunJob", "GeofenceScannerJob_onRunJob");

        if (!PhoneProfilesService.isGeofenceScannerStarted()) {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "geofence scanner is not started = cancel job");
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return Result.SUCCESS;
        }

        boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("2")) {
            PPApplication.logE("GeofenceScannerJob.onRunJob", "update in power save mode is not allowed = cancel job");
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return Result.SUCCESS;
        }

        if (Event.getGlobalEventsRunning(context)) {
            if ((PhoneProfilesService.instance != null) && (PhoneProfilesService.getGeofencesScanner() != null)) {
                if (PhoneProfilesService.getGeofencesScanner().mUpdatesStarted) {
                    PPApplication.logE("GeofenceScannerJob.onRunJob", "location updates started - start EventsHandler");

                    //PhoneProfilesService.geofencesScanner.stopLocationUpdates();

                    if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.getGeofencesScanner().updateGeofencesInDB();

                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER, false);

                } else
                    PPApplication.logE("GeofenceScannerJob.onRunJob", "location updates not started - start it");
                    // Fixed: java.lang.NullPointerException: Calling thread must be a prepared Looper thread.
                    //        com.google.android.gms.internal.zzccb.requestLocationUpdates(Unknown Source)
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 PhoneProfilesService.getGeofencesScanner().startLocationUpdates();
                             }
                         }
                    );
            }
        }

        GeofenceScannerJob.scheduleJob(context, false, false);

        return Result.SUCCESS;
    }

    static void scheduleJob(Context context, boolean startScanning, boolean forScreenOn) {
        PPApplication.logE("GeofenceScannerJob.scheduleJob", "startScanning="+startScanning);

        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {

            if (startScanning)
                PhoneProfilesService.getGeofencesScanner().mUpdatesStarted = false;

            JobManager jobManager = null;
            try {
                jobManager = JobManager.instance();
            } catch (Exception ignored) { }

            if (jobManager != null) {
                JobRequest.Builder jobBuilder;
                if (!startScanning) {

                    jobManager.cancelAllForTag(JOB_TAG_START);

                    PPApplication.logE("GeofenceScannerJob.scheduleJob", "mUpdatesStarted=" + PhoneProfilesService.getGeofencesScanner().mUpdatesStarted);

                    // look at GeofenceScanner:UPDATE_INTERVAL_IN_MILLISECONDS
                    int updateDuration = 30;

                    int interval;
                    if (PhoneProfilesService.getGeofencesScanner().mUpdatesStarted) {
                        interval = ApplicationPreferences.applicationEventLocationUpdateInterval(context) * 60;
                        PPApplication.logE("GeofenceScannerJob.scheduleJob", "interval=" + interval);
                        boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
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
                        .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                        .build()
                        .schedule();
            }
        }
        else
            PPApplication.logE("GeofenceScannerJob.scheduleJob", "scanner is not started");
    }

    static void cancelJob() {
        PPApplication.logE("GeofenceScannerJob.cancelJob", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            jobManager.cancelAllForTag(JOB_TAG_START);
            jobManager.cancelAllForTag(JOB_TAG);
        } catch (Exception ignored) {}
    }

    static boolean isJobScheduled() {
        PPApplication.logE("GeofenceScannerJob.isJobScheduled", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            return (jobManager.getAllJobRequestsForTag(JOB_TAG).size() != 0) ||
                    (jobManager.getAllJobRequestsForTag(JOB_TAG_START).size() != 0);
        } catch (Exception e) {
            return false;
        }
    }

}
