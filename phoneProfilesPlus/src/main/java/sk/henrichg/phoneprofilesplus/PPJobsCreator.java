package sk.henrichg.phoneprofilesplus;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case SearchCalendarEventsJob.JOB_TAG:
                return new SearchCalendarEventsJob();
            case SearchCalendarEventsJob.JOB_TAG_SHORT:
                return new SearchCalendarEventsJob();
            default:
                return null;
        }
    }

}
