package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] LocaleChangedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            final Context appContext = context.getApplicationContext();

            if (PPApplicationStatic.getApplicationStarted(false, false)) {

                Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=LocaleChangedReceiver.onReceive");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DataWrapper_stopAllEventsFromMainThread);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.collator = GlobalUtils.getCollator();

                        PPApplicationStatic.createNotificationChannels(context.getApplicationContext(), true);

                        //if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                        //PPApplication.showProfileNotification(/*true*/);
                        //if (PhoneProfilesService.getInstance() != null)
//                      PPApplicationStatic.logE("[PPP_NOTIFICATION] LocaleChangedReceiver.onReceive", "call of PPAppNotification.showNotification");

                        PPAppNotification.showNotification(context.getApplicationContext(),false, true, false, false);
                        ProfileListNotification.showNotification(context.getApplicationContext(), false);
                        //}

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
    }

}
