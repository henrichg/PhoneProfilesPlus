package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class DashClockJob extends Job {

    static final String JOB_TAG  = "DashClockJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "DashClockJob.onRunJob", "DashClockJob_onRunJob");

        PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
        if (dashClockExtension != null)
        {
            dashClockExtension.updateExtension();
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
