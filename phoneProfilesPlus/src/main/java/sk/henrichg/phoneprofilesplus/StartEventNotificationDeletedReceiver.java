package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class StartEventNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_START_EVENT_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".StartEventNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] StartEventNotificationDeletedReceiver.onReceive", "xxx");

        final long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=StartEventNotificationDeletedReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();

            //if (appContext != null) {
                if (event_id != 0) {
                    synchronized (PPApplication.handleEventsMutex) {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_StartEventNotificationDeletedReceiver_onReceive);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                            Event event = databaseHandler.getEvent(event_id);
                            if (event != null)
                                StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                        } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            //}
        };
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

}
