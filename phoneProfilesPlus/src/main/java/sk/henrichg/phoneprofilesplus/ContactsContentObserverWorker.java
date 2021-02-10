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
        try {
//            PPApplication.logE("[IN_WORKER]  ContactsContentObserverWorker.doWork", "xxxx");

            Context appContext = context.getApplicationContext();

//            PPApplication.logE("ContactsContentObserverWorker.doWork", "========> create contacts cache - true");
            // must be first
            PhoneProfilesService.createContactsCache(appContext, true);
            //must be seconds, this ads groups int contacts
            PhoneProfilesService.createContactGroupsCache(appContext, true);

            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_CONTACTS_CACHE_CHANGED);

            return Result.success();
        } catch (Exception e) {
            PPApplication.recordException(e);
            return Result.failure();
        }
    }
}
