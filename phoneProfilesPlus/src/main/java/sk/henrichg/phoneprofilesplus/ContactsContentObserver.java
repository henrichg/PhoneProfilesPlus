package sk.henrichg.phoneprofilesplus;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

class ContactsContentObserver extends ContentObserver {

    ContactsContentObserver(Handler handler) {
        super(handler);
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        PPApplication.logE("[OBSERVER CALL] ContactsContentObserver.onChange", "xxx");

        //CallsCounter.logCounter(context, "ContactsContentObserver.onChange", "ContactContentObserver_onChange");

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(ContactsContentObserverWorker.class)
                        .addTag(ContactsContentObserverWorker.WORK_TAG)
                        //.setInitialDelay(1, TimeUnit.SECONDS)
                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_DAYS, TimeUnit.DAYS)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    workManager.enqueueUniqueWork(ContactsContentObserverWorker.WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
