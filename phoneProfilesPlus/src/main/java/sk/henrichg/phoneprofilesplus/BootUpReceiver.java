package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BootUpReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "BootUpReceiver.onReceive", "BootUpReceiver_onReceive");

        if (intent == null)
            return;

        String action = intent.getAction();
        boolean okAction = false;
        if (action != null) {
            if (Build.VERSION.SDK_INT >= 24)
                okAction = action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED);
            if (!okAction)
                okAction = action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                        action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                        action.equals("com.htc.intent.action.QUICKBOOT_POWERON");
        }
        if (okAction) {

            PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- start");

            PPApplication.setBlockProfileEventActions(true);

            if (PPApplication.logEnabled()) {
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartOnBoot=" + ApplicationPreferences.applicationStartOnBoot);
                PPApplication.logE("BootUpReceiver.onReceive", "applicationStartEvents=" + ApplicationPreferences.applicationStartEvents);
                PPApplication.logE("BootUpReceiver.onReceive", "globalEventsRunning=" + Event.getGlobalEventsRunning());
            }

            //PPApplication.setApplicationStarted(context, false);

            final Context appContext = context.getApplicationContext();

            PPApplication.startHandlerThread(/*"BootUpReceiver.onReceive2"*/);
            final Handler handler2 = new Handler(PPApplication.handlerThread.getLooper());
            handler2.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BootUpReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BootUpReceiver.onReceive2");

                        if (ApplicationPreferences.applicationStartOnBoot) {
                            PPApplication.logE("BootUpReceiver.onReceive", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());

                            PPApplication.deviceBoot = true;

                            PPApplication.addActivityLog(appContext, PPApplication.ALTYPE_APPLICATION_START_ON_BOOT, null, null, null, 0, "");

                            //PPApplication.sleep(3000);
                            if (!PPApplication.getApplicationStarted(true)) {
                                // service is not started
                                PPApplication.logE("BootUpReceiver.onReceive", "start service");
                                // service is not started, start it
                                PPApplication.setApplicationStarted(appContext, true);
                                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, true);
                                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                                PPApplication.startPPService(appContext, serviceIntent, true);
                            }
                            else {
                                // service is started
                                PPApplication.logE("BootUpReceiver.onReceive", "activate profiles");

                                //if (PhoneProfilesService.getInstance() != null)
                                //    PhoneProfilesService.getInstance().removeRestartEventsForFirstStartHandler(true);

                                PPApplication.logE("BootUpReceiver.onReceive", "called work for first start");

                                // work after first start
                                Data workData = new Data.Builder()
                                        .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_AFTER_FIRST_START)
                                        .putBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, true)
                                        .putBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true)
                                        .build();

                                OneTimeWorkRequest worker =
                                        new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                .addTag("afterFirstStartWork")
                                                .setInputData(workData)
                                                .setInitialDelay(5, TimeUnit.SECONDS)
                                                .build();
                                try {
                                    if (PPApplication.getApplicationStarted(true)) {
                                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                                        if (workManager != null)
                                            workManager.enqueue(worker);
                                    }
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        } else {
                            if (PPApplication.logEnabled()) {
                                PPApplication.logE("BootUpReceiver.onReceive", "ApplicationPreferences.applicationStartOnBoot()=false");
                                PPApplication.logE("PPApplication.exitApp", "from BootUpReceiver.onReceive shutdown=false");
                            }
                            PPApplication.deviceBoot = false;
                            PPApplication.exitApp(false, appContext, null, null, false/*, true, true*/);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BootUpReceiver.onReceive2");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            });

            //PPApplication.logE("@@@ BootUpReceiver.onReceive", "#### -- end");

        }

    }

}
