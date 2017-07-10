package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;

class SearchCalendarEventsJob extends Job {

    static final String JOB_TAG  = "SearchCalendarEventsJob";
    static final String JOB_TAG_SHORT  = "SearchCalendarEventsJob_short";
    static SearchCalendarEventsBroadcastReceiver broadcastReceiver = new SearchCalendarEventsBroadcastReceiver();

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("SearchCalendarEventsJob.onRunJob", "xxx");

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter("SearchCalendarEventsBroadcastReceiver"));
        Intent intent = new Intent("SearchCalendarEventsBroadcastReceiver");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
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
                jobBuilder.setPeriodic(24 * 60 * 60 * 1000);
            }
            else
                return;
        }
        else {
            jobBuilder = new JobRequest.Builder(JOB_TAG_SHORT);
            jobBuilder.setExact(5 * 1000);
        }

        PPApplication.logE("SearchCalendarEventsJob.scheduleJob", "build and schedule");

        jobBuilder
                .setPersisted(false)
                .setUpdateCurrent(true)
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
