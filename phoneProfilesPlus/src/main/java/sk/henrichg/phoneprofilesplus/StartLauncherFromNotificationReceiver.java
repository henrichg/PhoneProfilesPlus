package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.concurrent.TimeUnit;

// Disable action button
public class StartLauncherFromNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] StartLauncherFromNotificationReceiver.onReceive", "xxx");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                if (action.equals(sk.henrichg.phoneprofilesplus.PPAppNotification.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {

//                    PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** StartLauncherFromNotificationReceiver.onReceive", "schedule");

                    final Context appContext = context.getApplicationContext();
                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
//                        long start = System.currentTimeMillis();
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** StartLauncherFromNotificationReceiver", "--------------- START");

                            // intent to LauncherActivity, for click on notification
                            //Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                            Intent launcherIntent = GlobalGUIRoutines.getIntentForStartupSource(context, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            // clear all opened activities
                            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                            // setup startupSource
                            launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                            appContext.startActivity(launcherIntent);

//                            long finish = System.currentTimeMillis();
//                            long timeElapsed = finish - start;
//                            PPApplicationStatic.logE("[IN_EXECUTOR]  ***** StartLauncherFromNotificationReceiver", "--------------- END - timeElapsed="+timeElapsed);
                        //worker.shutdown();
                    };
                    PPApplicationStatic.createDelayedGuiExecutor();
                    if ((Build.VERSION.SDK_INT >= 29) &&
                            ApplicationPreferences.applicationNotificationLauncher.equals("activator")) {
                        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
                            if (Build.VERSION.SDK_INT >= 30)
                                PPApplication.delayedGuiExecutor.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                            else
                                PPApplication.delayedGuiExecutor.schedule(runnable, 1000, TimeUnit.MILLISECONDS);
                        }
                        else
                            PPApplication.delayedGuiExecutor.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                    }
                    else
                        PPApplication.delayedGuiExecutor.submit(runnable);

                    /*
                    final Context appContext = context.getApplicationContext();
                    //Handler _handler = new Handler(appContext.getMainLooper());
                    PPApplication.startHandlerThreadBroadcast();
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //PPApplication.PPHandlerThreadRunnable r = new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    Runnable r = () -> {
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=StartLauncherFromNotificationReceiver.onReceive");

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
