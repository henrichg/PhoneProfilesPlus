package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

class DonationNotificationJob extends Job {

    static final String JOB_TAG  = "DonationNotificationJob";

    private static final int MAX_DONATION_NOTIFICATION_COUNT = 3;

    //private static CountDownLatch countDownLatch = null;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        PPApplication.logE("##### DonationNotificationJob.onRunJob", "xxx");
        Context context = getContext();

        CallsCounter.logCounter(context, "DonationNotificationJob.onRunJob", "AboutApplicationJob_onRunJob");

        if (!PPApplication.getApplicationStarted(context, false))
            // application is not started
            return Result.SUCCESS;

        //countDownLatch = new CountDownLatch(1);

        int daysAfterFirstStart = PPApplication.getDaysAfterFirstStart(context)+1;
        PPApplication.logE("DonationNotificationJob.onRunJob", "daysAfterFirstStart="+daysAfterFirstStart);
        int donationNotificationCount = PPApplication.getDonationNotificationCount(context);
        PPApplication.logE("DonationNotificationJob.onRunJob", "donationNotificationCount="+donationNotificationCount);
        boolean donationDonated = PPApplication.getDonationDonated(context);
        PPApplication.logE("DonationNotificationJob.onRunJob", "donationDonated="+donationDonated);

        if (!donationDonated && (donationNotificationCount < MAX_DONATION_NOTIFICATION_COUNT)) {
            int daysForOneNotification = 7;
            switch (donationNotificationCount) {
                case 1:
                    daysForOneNotification = daysAfterFirstStart + 14;
                    break;
                case 2:
                    daysForOneNotification = daysAfterFirstStart + 21;
                    break;
            }

            if (daysAfterFirstStart == daysForOneNotification) {
                PPApplication.setDonationNotificationCount(context, donationNotificationCount+1);

                // show notification about "Please donate me."
                PPApplication.createInformationNotificationChannel(context);

                NotificationCompat.Builder mBuilder;
                Intent _intent = new Intent(context, DonationActivity.class);

                String nTitle = context.getString(R.string.about_application_donate_button);
                String nText = context.getString(R.string.donation_description);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.about_application_donate_button) + ": " +
                            context.getString(R.string.donation_description);
                }
                mBuilder = new NotificationCompat.Builder(context, PPApplication.INFORMATION_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(true); // clear notification after click

                PendingIntent pi = PendingIntent.getActivity(context, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                //if (android.os.Build.VERSION.SDK_INT >= 21) {
                    mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
                    mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                //}
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null)
                    mNotificationManager.notify(PPApplication.ABOUT_APPLICATION_DONATE_NOTIFICATION_ID, mBuilder.build());
            }

            PPApplication.setDaysAfterFirstStart(context, daysAfterFirstStart);

            scheduleJob(context, false);

            /*
            try {
                countDownLatch.await();
            } catch (InterruptedException ignored) {
            }
            countDownLatch = null;
            PPApplication.logE("DonationNotificationJob.onRunJob", "return");*/
        }
        else {
            PPApplication.setDonationNotificationCount(context, MAX_DONATION_NOTIFICATION_COUNT);
        }

        return Result.SUCCESS;
    }

    protected void onCancel() {
        PPApplication.logE("DonationNotificationJob.onCancel", "xxx");

        Context context = getContext();

        CallsCounter.logCounter(context, "DonationNotificationJob.onCancel", "AboutApplicationJob_onCancel");
    }

    private static void _scheduleJob(/*final Context context*/) {
        JobManager jobManager = null;
        try {
            jobManager = JobManager.instance();
        } catch (Exception ignored) { }

        if (jobManager != null) {
            final JobRequest.Builder jobBuilder;
            int requestsForTagSize = jobManager.getAllJobRequestsForTag(JOB_TAG).size();
            PPApplication.logE("DonationNotificationJob.scheduleJob", "requestsForTagSize=" + requestsForTagSize);
            if (requestsForTagSize == 0) {
                jobBuilder = new JobRequest.Builder(JOB_TAG);
                // each 24 hours
                jobBuilder.setPeriodic(TimeUnit.DAYS.toMillis(1));
            } else
                return;

            PPApplication.logE("DonationNotificationJob.scheduleJob", "build and schedule");

            try {
                jobBuilder
                        .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                        .build()
                        .scheduleAsync();
            } catch (Exception ignored) { }
        }
    }

    static void scheduleJob(final Context context, final boolean useHandler) {
        PPApplication.logE("DonationNotificationJob.scheduleJob", "xxx");

        boolean donationDonated = PPApplication.getDonationDonated(context);
        PPApplication.logE("DonationNotificationJob.scheduleJob", "donationDonated="+donationDonated);
        if (donationDonated)
            return;

        if (useHandler) {
            PPApplication.startHandlerThread("DonationNotificationJob.scheduleJob");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DonationNotificationJob.scheduleJob");

                    _scheduleJob();
                    /*if (countDownLatch != null)
                        countDownLatch.countDown();*/

                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DonationNotificationJob.scheduleJob");
                }
            });
        }
        else {
            _scheduleJob();
            /*if (countDownLatch != null)
                countDownLatch.countDown();*/
        }
    }

    /*
    static void cancelJob(final Context context, final Handler _handler) {
        PPApplication.logE("DonationNotificationJob.cancelJob", "xxx");

        final Handler handler;
        if (_handler == null)
            handler = new Handler(context.getMainLooper());
        else
            handler = _handler;
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JobManager jobManager = JobManager.instance();
                    jobManager.cancelAllForTag(JOB_TAG);
                } catch (Exception ignored) {}
            }
        });
    }
    */
}
