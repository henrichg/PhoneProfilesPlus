package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class PPAppNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_PP_APP_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".PPAppNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] StartEventNotificationDeletedReceiver.onReceive", "xxx");
        Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPAppNotificationDeletedReceiver.onReceive");

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DataWrapper_stopAllEventsFromMainThread);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                PPAppNotification.forceDrawNotificationWhenIsDeleted(context.getApplicationContext());

            } catch (Exception e) {
    //                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        };
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

}
