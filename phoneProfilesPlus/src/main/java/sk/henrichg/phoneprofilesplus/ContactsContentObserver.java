package sk.henrichg.phoneprofilesplus;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static void enqueueContactsContentObserverWorker() {
        if (PPApplicationStatic.getApplicationStarted(true, true)) {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                boolean running = false;
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(ContactsContentObserverWorker.WORK_TAG);
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                        break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("ContactsContentObserver.enqueueContactsContentObserverWorker", Log.getStackTraceString(e));
                }

                OneTimeWorkRequest worker;
                if (running) {
                    // is already running enqueue work with delay
                    worker =
                            new OneTimeWorkRequest.Builder(ContactsContentObserverWorker.class)
                                    .addTag(ContactsContentObserverWorker.WORK_TAG)
                                    .setInitialDelay(1, TimeUnit.MINUTES)
                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                }
                else {
                    // is not running enqueue work without delay
                    worker =
                            new OneTimeWorkRequest.Builder(ContactsContentObserverWorker.class)
                                    .addTag(ContactsContentObserverWorker.WORK_TAG)
                                    //.setInitialDelay(1, TimeUnit.MINUTES)
                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                }
//                PPApplicationStatic.logE("[WORKER_CALL] ContactsContentObserver.enqueueContactsContentObserverWorker", "xxx");
                workManager.enqueueUniqueWork(ContactsContentObserverWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
            }
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
//        if (PPApplicationStatic.logEnabled()) {
//            PPApplicationStatic.logE("[IN_OBSERVER] ContactsContentObserver.onChange", "uri=" + uri);
//
//            PPApplicationStatic.logE("[IN_OBSERVER] ContactsContentObserver.onChange", "ContactsContract.Contacts.CONTENT_URI=" + ContactsContract.Contacts.CONTENT_URI);
//            PPApplicationStatic.logE("[IN_OBSERVER] ContactsContentObserver.onChange", "ContactsContract.CommonDataKinds.Phone.CONTENT_URI=" + ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//            PPApplicationStatic.logE("[IN_OBSERVER] ContactsContentObserver.onChange", "ContactsContract.Groups.CONTENT_SUMMARY_URI=" + ContactsContract.Groups.CONTENT_SUMMARY_URI);
//            PPApplicationStatic.logE("[IN_OBSERVER] ContactsContentObserver.onChange", "ContactsContract.Data.CONTENT_URI=" + ContactsContract.Data.CONTENT_URI);
//        }

        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsContentObserver.onChange", "test of PPApplication.blockContactContentObserver");
        if (PPApplication.blockContactContentObserver)
            // observwer is blocked (for exmple by profile/event preferences activity)
            return;

        enqueueContactsContentObserverWorker();
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
