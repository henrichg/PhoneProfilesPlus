package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

class SearchCalendarEventsJob extends Job {

    static final String JOB_TAG  = "SearchCalendarEventsJob";
    static final String JOB_TAG_SHORT  = "SearchCalendarEventsJob_short";

    public static final String BROADCAST_RECEIVER_TYPE = "searchCalendarEvents";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("SearchCalendarEventsJob.onRunJob", "xxx");

        Context appContext = getContext().getApplicationContext();

        SearchCalendarEventsJob.scheduleJob(false);
        //setAlarm(appContext, false);

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return Result.SUCCESS;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            /*boolean calendarEventsExists = false;

            DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
            calendarEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR) > 0;
            PPApplication.logE("SearchCalendarEventsBroadcastReceiver.onReceive", "calendarEventsExists=" + calendarEventsExists);
            dataWrapper.invalidateDataWrapper();

            if (calendarEventsExists)
            {*/
            // start service
            Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}

        }

        return Result.SUCCESS;
    }

    static void scheduleJob(boolean shortInterval) {
        PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "shortInterval="+shortInterval);

        JobRequest.Builder jobBuilder;
        if (!shortInterval) {
            JobManager jobManager = JobManager.instance();
            jobManager.cancelAllForTag(JOB_TAG_SHORT);
            int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
            PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "requestsForTagSize="+requestsForTagSize);
            if (requestsForTagSize == 0) {
                jobBuilder = new JobRequest.Builder(JOB_TAG);
                // each 24 hours
                jobBuilder.setPeriodic(TimeUnit.HOURS.toMillis(24));
            }
            else
                return;
        }
        else {
            cancelJob();
            jobBuilder = new JobRequest.Builder(JOB_TAG_SHORT);
            jobBuilder.setExact(TimeUnit.SECONDS.toMillis(5));
        }

        PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "build and schedule");

        jobBuilder
                .setPersisted(false)
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .build()
                .schedule();
    }

    static void cancelJob() {
        PPApplication.logE("SearchCalendarEventsJob.cancelJob", "xxx");

        JobManager jobManager = JobManager.instance();
        jobManager.cancelAllForTag(JOB_TAG_SHORT);
        jobManager.cancelAllForTag(JOB_TAG);
    }

}
