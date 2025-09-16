package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ProfileListNotificationDeletedReceiver extends BroadcastReceiver {

    static final String ACTION_PROFILE_LIST_NOTIFICATION_DELETED = PPApplication.PACKAGE_NAME + ".ProfileListNotificationDeletedReceiver.ACTION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] KeepScreenOnNotificationDeletedReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        Runnable runnable = () -> {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_ProfileListNotificationDeletedReceiver_onReceive);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                ProfileListNotification.forceDrawNotificationWhenIsDeleted(context.getApplicationContext());

            } catch (Exception e) {
//                PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] ProfileListNotificationDeletedReceiver.onReceive", Log.getStackTraceString(e));
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
