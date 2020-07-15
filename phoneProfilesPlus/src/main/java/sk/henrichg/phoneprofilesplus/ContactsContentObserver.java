package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

class ContactsContentObserver extends ContentObserver {

    private final Context context;

    ContactsContentObserver(Context c, Handler handler) {
        super(handler);

        context=c;
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

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

}
