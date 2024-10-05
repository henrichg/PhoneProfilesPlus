package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ContactsContentObserverWorker extends Worker {

    final Context context;

    static final String WORK_TAG = "contactsContentObserverWork";

    public ContactsContentObserverWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsContentObserverWorker.doWork", "test of PPApplication.blockContactContentObserver");
        if (PPApplication.blockContactContentObserver)
            // observwer is blocked (for exmple by profile/event preferences activity)
            return Result.success();

        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER]  ContactsContentObserverWorker.doWork", "--------------- START");

            Context appContext = context.getApplicationContext();

            // must be first
            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactsCache()");
            PPApplicationStatic.createContactsCache(appContext, false, true/*, true*/);
            //must be seconds, this ads groups into contacts
            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactGroupsCache()");
            PPApplicationStatic.createContactGroupsCache(appContext, false/*, true*//*, true*/);

            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_CONTACTS_CACHE_CHANGED});

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  ContactsContentObserverWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }
}
