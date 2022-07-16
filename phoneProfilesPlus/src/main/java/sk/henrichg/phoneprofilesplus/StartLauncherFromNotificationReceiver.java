package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Disable action button
public class StartLauncherFromNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] StartLauncherFromNotificationReceiver.onReceive", "xxx");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "action="+action);

                if (action.equals(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {

//                    PPApplication.logE("[EXECUTOR_CALL]  ***** StartLauncherFromNotificationReceiver.onReceive", "schedule");

                    final Context appContext = context.getApplicationContext();
                    ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
//                        long start = System.currentTimeMillis();
//                        PPApplication.logE("[IN_EXECUTOR]  ***** StartLauncherFromNotificationReceiver", "--------------- START");

//                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
//                        PowerManager.WakeLock wakeLock = null;
//                        try {
//                            if (powerManager != null) {
//                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartLauncherFromNotificationReceiver_onReceive");
//                                wakeLock.acquire(10 * 60 * 1000);
//                            }

                            // intent to LauncherActivity, for click on notification
                            Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                            // clear all opened activities
                            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                            // setup startupSource
                            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            appContext.startActivity(launcherIntent);

//                            long finish = System.currentTimeMillis();
//                            long timeElapsed = finish - start;
//                            PPApplication.logE("[IN_EXECUTOR]  ***** StartLauncherFromNotificationReceiver", "--------------- END - timeElapsed="+timeElapsed);
//                        } catch (Exception e) {
////                                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
//                            PPApplication.recordException(e);
//                        } finally {
//                            if ((wakeLock != null) && wakeLock.isHeld()) {
//                                try {
//                                    wakeLock.release();
//                                } catch (Exception ignored) {
//                                }
//                            }
//                        }
                    };
                    if ((Build.VERSION.SDK_INT >= 29) &&
                            ApplicationPreferences.applicationNotificationLauncher.equals("activator")) {
                        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                            if (Build.VERSION.SDK_INT >= 30)
                                worker.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                            else
                                worker.schedule(runnable, 1000, TimeUnit.MILLISECONDS);
                        }
                        else
                            worker.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                    }
                    else
                        worker.submit(runnable);
                    worker.shutdown();

                    /*
                    final Context appContext = context.getApplicationContext();
                    //Handler _handler = new Handler(appContext.getMainLooper());
                    PPApplication.startHandlerThreadBroadcast();
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //PPApplication.PPHandlerThreadRunnable r = new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    Runnable r = () -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=StartLauncherFromNotificationReceiver.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                            // intent to LauncherActivity, for click on notification
                            Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                            // clear all opened activities
                            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            // setup startupSource
                            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            appContext.startActivity(launcherIntent);
                        //}
                    };
                    //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "PPApplication.deviceIsSamsung="+PPApplication.deviceIsSamsung);
                    if ((Build.VERSION.SDK_INT >= 29) &&
                            ApplicationPreferences.applicationNotificationLauncher.equals("activator")) {
                        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                            if (Build.VERSION.SDK_INT >= 30)
                                __handler.postDelayed(r, 500);
                            else
                                __handler.postDelayed(r, 1000);
                        }
                        else
                            __handler.postDelayed(r, 500);
                    }
                    else
                        __handler.post(r);
                    */
                }
            }
        }
    }

}
