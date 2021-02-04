package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

// This broadcast is needed for start of PPP after package replaced

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");
        //CallsCounter.logCounterNoInc(context, "PackageReplacedReceiver.onReceive->action="+intent.getAction(), "PackageReplacedReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {

            PPApplication.startHandlerThreadBroadcast(/*"PackageReplacedReceiver.onReceive"*/);
            final Handler handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler2.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PackageReplacedReceiver.onReceive");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PackageReplacedReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    CheckCriticalGitHubReleasesBroadcastReceiver.setShowCriticalGitHubReleasesNotification(appContext, 0);

                    boolean serviceStarted = PhoneProfilesService.isServiceRunning(appContext, PhoneProfilesService.class, false);
                    PPApplication.logE("##### PackageReplacedReceiver.onReceive", "serviceStarted="+serviceStarted);

                    if ((!serviceStarted) && PPApplication.getApplicationStarted(false)) {
                        // service is not started
                        try {
                            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "start service");
                            // service is not started, start it
                            PPApplication.setApplicationStarted(appContext, true);
                            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
                            PPApplication.logE("[START_PP_SERVICE] PackageReplacedReceiver.onReceive", "xxx");
                            PPApplication.startPPService(appContext, serviceIntent);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PackageReplacedReceiver.onReceive");
                } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }

}
