package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;

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

        PPApplication.startHandlerThread(/*"ContactsContentObserver.onChange"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (context == null)
                    return;

                Context appContext = context.getApplicationContext();

                if (appContext == null)
                    return;

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesService_doForFirstStart");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    // must be first
                    PhoneProfilesService.createContactsCache(context.getApplicationContext(), true);
                    //must be seconds, this ads groups int contacts
                    PhoneProfilesService.createContactGroupsCache(context.getApplicationContext(), true);

                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

}
