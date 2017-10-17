package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class WifiAPStateChangeJob extends Job {

    static final String JOB_TAG  = "WifiAPStateChangeJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "WifiAPStateChangeJob.onRunJob", "WifiAPStateChangeJob_onRunJob");

        if (Event.getGlobalEventsRunning(appContext))
        {
            if (WifiApManager.isWifiAPEnabled(appContext)) {
                // Wifi AP is enabled
                PPApplication.logE("WifiAPStateChangeJob.onRunJob","wifi AP enabled");
                WifiScanJob.cancelJob(appContext);
            }
            else {
                PPApplication.logE("WifiAPStateChangeJob.onRunJob","wifi AP disabled");
                // send broadcast for one wifi scan
                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.scheduleWifiJob(true, true, true, false, true, false, false);
            }
        }

        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .startNow()
                .build()
                .schedule();
    }

}
