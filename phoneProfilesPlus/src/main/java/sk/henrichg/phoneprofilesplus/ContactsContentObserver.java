package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;

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

        //CallsCounter.logCounter(context, "ContactsContentObserver.onChange", "ContactContentObserver_onChange");

        // must be first
        PhoneProfilesService.createContactsCache(context.getApplicationContext());
        //must be seconds, this ads groups int contacts
        PhoneProfilesService.createContactGroupsCache(context.getApplicationContext());
    }

}
