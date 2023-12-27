package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

// This broadcast is needed for start of PPP after package replaced

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplicationStatic.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {

            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PackageReplacedReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PackageReplacedReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // reset GitHub version for critical check releaaes notification
                        CheckCriticalPPPReleasesBroadcastReceiver.setShowCriticalGitHubReleasesNotification(appContext, 0);

                        // reset alarm for month check releaaes notification
                        CheckPPPReleasesBroadcastReceiver.setShowPPPReleasesNotification(context, 0);
                        CheckPPPReleasesBroadcastReceiver.setAlarm(appContext);

                        boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                        PPApplicationStatic.logE("##### PackageReplacedReceiver.onReceive", "serviceStarted=" + serviceStarted);

                        if ((!serviceStarted) && PPApplicationStatic.getApplicationStarted(false, false)) {
                            // service is not started

                            //AutostartPermissionNotification.showNotification(appContext, false);

                            try {
                                PPApplicationStatic.logE("##### PackageReplacedReceiver.onReceive", "start service");
                                // service is not started, start it
                                PPApplicationStatic.setApplicationStarted(appContext, true);
                                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
//                                PPApplicationStatic.logE("[START_PP_SERVICE] PackageReplacedReceiver.onReceive", "xxx");
                                PPApplicationStatic.startPPService(appContext, serviceIntent, true);
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        }

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
                //}
            };
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

}
