package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

class AboutApplicationJob extends Job {

    static final String JOB_TAG  = "AboutApplicationJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("##### AboutApplicationJob.onRunJob", "xxx");

        Context context = getContext();

        if (!PPApplication.getApplicationStarted(context, false))
            // application is not started
            return Result.SUCCESS;

        int daysAfterFirstStart = PPApplication.getDaysAfterFirstStart(context)+1;
        PPApplication.logE("AboutApplicationJob.onRunJob", "daysAfterFirstStart="+daysAfterFirstStart);

        if (daysAfterFirstStart == 7) {
            PPApplication.setDaysAfterFirstStart(context, 8);
            // show notification about "Please donate me."
            NotificationCompat.Builder mBuilder;
            Intent _intent = new Intent(context, AboutApplicationActivity.class);

            String ntitle = context.getString(R.string.about_application_donate_button);
            String ntext = context.getString(R.string.donation_description);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                ntitle = context.getString(R.string.app_name);
                ntext = context.getString(R.string.about_application_donate_button) + ": " +
                        context.getString(R.string.donation_description);
            }
            mBuilder =   new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                    .setContentTitle(ntitle) // title for notification
                    .setContentText(ntext)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(ntext))
                    .setAutoCancel(true); // clear notification after click

            PendingIntent pi = PendingIntent.getActivity(context, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                mBuilder.setCategory(Notification.CATEGORY_EVENT);
                mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_ID, mBuilder.build());
        }
        else
        if (daysAfterFirstStart < 7)
            PPApplication.setDaysAfterFirstStart(context, daysAfterFirstStart);

        scheduleJob();

        return Result.SUCCESS;
    }

    static void scheduleJob() {
        PPApplication.logE("AboutApplicationJob.scheduleJob", "xxx");

        JobRequest.Builder jobBuilder;
        JobManager jobManager = JobManager.instance();
        int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
        PPApplication.logE("AboutApplicationJob.scheduleJob", "requestsForTagSize="+requestsForTagSize);
        if (requestsForTagSize == 0) {
            jobBuilder = new JobRequest.Builder(JOB_TAG);
            // each 24 hours
            jobBuilder.setPeriodic(TimeUnit.DAYS.toMillis(1));
        }
        else
            return;

        PPApplication.logE("AboutApplicationJob.scheduleJob", "build and schedule");

        jobBuilder
                .setPersisted(false)
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .build()
                .schedule();
    }

    static void cancelJob() {
        PPApplication.logE("AboutApplicationJob.cancelJob", "xxx");

        JobManager jobManager = JobManager.instance();
        jobManager.cancelAllForTag(JOB_TAG);
    }

}
