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

                if (action.equals(PPAppNotification.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {

//                    PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** StartLauncherFromNotificationReceiver.onReceive", "schedule");

                    final Context appContext = context.getApplicationContext();
                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                    Runnable runnable = () -> {
//                        long start = System.currentTimeMillis();
//                        PPApplicationStatic.logE("[IN_EXECUTOR]  ***** StartLauncherFromNotificationReceiver", "--------------- START");

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
                }
            }
        }
    }

}
