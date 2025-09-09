package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//*** vraj Worker bezi vo vlastnom threade, vid:
// https://developer.android.com/develop/background-work/background-tasks/persistent/threading/worker
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
//        PPApplicationStatic.logE("[CONTACTS_OBSERVER] ContactsContentObserverWorker.doWork", "PPApplication.blockContactContentObserver="+PPApplication.blockContactContentObserver);
//        if (PPApplication.blockContactContentObserver)
//            // observwer is blocked (for exmple by profile/event preferences activity)
//            return Result.success();

        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER]  ContactsContentObserverWorker.doWork", "--------------- START");

            Context appContext = context.getApplicationContext();

            synchronized (PPApplication.handleEventsMutex) {

                // must be first
//            PPApplicationStatic.logE("[CONTACTS_OBSERVER] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactsCache()");
//                Log.e("[CONTACTS_OBSERVER] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactsCache()");
                boolean cotactsOK = PPApplicationStatic.createContactsCache(appContext, false, true/*, true*/, true);
                //must be seconds, this ads groups into contacts
//            PPApplicationStatic.logE("[CONTACTS_OBSERVER] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactGroupsCache()");
//                Log.e("[CONTACTS_OBSERVER] ContactsContentObserverWorker.doWork", "PPApplicationStatic.createContactGroupsCache()");
                boolean cotactGroupsOK = PPApplicationStatic.createContactGroupsCache(appContext, false/*, true*//*, true*/, true);

                if (cotactsOK && cotactGroupsOK) {
                    PPApplication.repeatCreateContactCacheIfSQLError = 0;
                    if (EventStatic.getGlobalEventsRunning(appContext)) {
//                      PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] ContactsContentObserverWorker.doWork", "SENSOR_TYPE_CONTACTS_CACHE_CHANGED");
//                        Log.e("[EVENTS_HANDLER_CALL] ContactsContentObserverWorker.doWork", "SENSOR_TYPE_CONTACTS_CACHE_CHANGED");
//                        PPApplicationStatic.logE("[HANDLE_EVENTS_FROM_WORK] ContactsContentObserverWorker.doWork", "SENSOR_TYPE_CONTACTS_CACHE_CHANGED");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_CONTACTS_CACHE_CHANGED});
                    }
                } else {
                    //Log.e("ContactsContentObserverWorker.doWork", "(2) PPApplication.repeatCreateContactCacheIfSQLError="+PPApplication.repeatCreateContactCacheIfSQLError);
                    if (PPApplication.repeatCreateContactCacheIfSQLError < 3) {
                        // repeat
                        ++PPApplication.repeatCreateContactCacheIfSQLError;
                        ContactsContentObserver.enqueueContactsContentObserverWorker();
                    } else
                        // do not repeat
                        PPApplication.repeatCreateContactCacheIfSQLError = 0;
                }

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  ContactsContentObserverWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
                return Result.success();
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }
}
