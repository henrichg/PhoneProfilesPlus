package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class PhoneStateJob extends Job {

    static final String JOB_TAG  = "PhoneStateJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneStateJob.onRunJob", "PhoneStateJob_onRunJob");

        if (!PPApplication.getApplicationStarted(appContext, false))
            // application is not started
            return Result.SUCCESS;

        if (Event.getGlobalEventsRunning(appContext))
        {
            PPApplication.logE("@@@ PhoneStateJob.onRunJob", "-----------");

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE, false);
            }
            dataWrapper.invalidateDataWrapper();

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
