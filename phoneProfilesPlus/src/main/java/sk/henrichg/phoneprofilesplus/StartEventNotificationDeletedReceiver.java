package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class StartEventNotificationDeletedReceiver extends BroadcastReceiver {

    static final String START_EVENT_NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".START_EVENT_NOTIFICATION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### StartEventNotificationDeletedReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "StartEventNotificationDeletedReceiver.onReceive", "StartEventNotificationDeletedReceiver_onReceive");

        final Context appContext = context.getApplicationContext();
        final long event_id = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
        PPApplication.startHandlerThread("StartEventNotificationDeletedReceiver.onReceive");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (event_id != 0) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartEventNotificationBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=StartEventNotificationDeletedReceiver.onReceive");

                        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(appContext);
                        Event event = databaseHandler.getEvent(event_id);
                        if (event != null)
                            StartEventNotificationBroadcastReceiver.removeAlarm(event, appContext);

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=StartEventNotificationDeletedReceiver.onReceive");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        });
    }

}
