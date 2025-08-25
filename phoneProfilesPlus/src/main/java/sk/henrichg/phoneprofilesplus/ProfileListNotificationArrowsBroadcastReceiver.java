package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class ProfileListNotificationArrowsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfileListNotificationArrowsBroadcastReceiver.onReceive", "xxx");
        final String action = intent.getAction();
        final Context appContext = context.getApplicationContext();
        if (action != null) {
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfileListNotificationArrowsBroadcastReceiver.onReceive", "action="+action);

            Runnable runnable = () -> {
                synchronized (PPApplication.showPPPNotificationMutex) {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_ProfileListNotificationArrowsBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (action.equalsIgnoreCase(ProfileListNotification.ACTION_RIGHT_ARROW_CLICK)) {
                            int displayedPage = ProfileListNotification.displayedPage;
                            int profileCount = ProfileListNotification.profileCount;
                            if ((displayedPage < profileCount / ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage) &&
                                    (profileCount > ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage)) {
                                ++displayedPage;
                                ProfileListNotification.displayedPage = displayedPage;
                                ProfileListNotification._showNotification(appContext/*, false*/);
                            }
                        } else if (action.equalsIgnoreCase(ProfileListNotification.ACTION_LEFT_ARROW_CLICK)) {
                            int displayedPage = ProfileListNotification.displayedPage;
                            if (displayedPage > 0) {
                                --displayedPage;
                                ProfileListNotification.displayedPage = displayedPage;
                                ProfileListNotification._showNotification(appContext/*, false*/);
                            }
                        }

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] ProfileListNotificationArrowsBroadcastReceiver.onReceive", Log.getStackTraceString(e));
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
            };
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }
}

