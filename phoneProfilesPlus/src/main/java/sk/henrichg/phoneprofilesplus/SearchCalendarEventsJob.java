package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

class SearchCalendarEventsJob extends Job {

    static final String JOB_TAG  = "SearchCalendarEventsJob";
    static final String JOB_TAG_SHORT  = "SearchCalendarEventsJob_short";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("SearchCalendarEventsJob.onRunJob", "xxx");
        CallsCounter.logCounter(getContext(), "SearchCalendarEventsJob.onRunJob", "SearchCalendarEventsJob_onRunJob");

        Context appContext = getContext().getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return Result.SUCCESS;

        SearchCalendarEventsJob.scheduleJob(false);

        if (Event.getGlobalEventsRunning(appContext))
        {
            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("SearchCalendarEventsBroadcastReceiver.onReceive", "calendarEventsExists=" + calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
            // start events handler
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS, false);
            //}

        }

        return Result.SUCCESS;
    }

    static void scheduleJob(boolean shortInterval) {
        PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "shortInterval="+shortInterval);

        JobManager jobManager = null;
        try {
            jobManager = JobManager.instance();
        } catch (Exception ignored) { }

        if (jobManager != null) {
            JobRequest.Builder jobBuilder;
            if (!shortInterval) {
                jobManager.cancelAllForTag(JOB_TAG_SHORT);
                int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
                PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "requestsForTagSize=" + requestsForTagSize);
                if (requestsForTagSize == 0) {
                    jobBuilder = new JobRequest.Builder(JOB_TAG);
                    // each 24 hours
                    jobBuilder.setPeriodic(TimeUnit.HOURS.toMillis(24));
                } else
                    return;
            } else {
                cancelJob();
                jobBuilder = new JobRequest.Builder(JOB_TAG_SHORT);
                jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
            }

            PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "build and schedule");

            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .build()
                    .schedule();
        }
    }

    static void cancelJob() {
        PPApplication.logE("SearchCalendarEventsJob.cancelJob", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            jobManager.cancelAllForTag(JOB_TAG_SHORT);
            jobManager.cancelAllForTag(JOB_TAG);
        } catch (Exception ignored) {}
    }

    static boolean isJobScheduled() {
        PPApplication.logE("SearchCalendarEventsJob.isJobScheduled", "xxx");

        try {
            JobManager jobManager = JobManager.instance();
            return (jobManager.getAllJobRequestsForTag(JOB_TAG).size() != 0) ||
                    (jobManager.getAllJobRequestsForTag(JOB_TAG_SHORT).size() != 0);
        } catch (Exception e) {
            return false;
        }
    }

}
