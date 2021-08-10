package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class StartEventNotificationDeletedReceiver extends BroadcastReceiver {

    static final String START_EVENT_NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".StartEventNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] StartEventNotificationDeletedReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "StartEventNotificationDeletedReceiver.onReceive", "StartEventNotificationDeletedReceiver_onReceive");

        final long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
        final Context appContext = context;
        PPApplication.startHandlerThreadBroadcast(/*"StartEventNotificationDeletedReceiver.onReceive"*/);
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
        __handler.post(new Runnable() {
            @Override
            public void run() {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=StartEventNotificationDeletedReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    if (event_id != 0) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartEventNotificationDeletedReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                            Event event = databaseHandler.getEvent(event_id);
                            if (event != null)
                                StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=StartEventNotificationDeletedReceiver.onReceive");
                        } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                //}
            }
        });
    }

}
