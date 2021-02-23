package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

// Disable action button
public class StartLauncherFromNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] StartLauncherFromNotificationReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "StartLauncherFromNotificationReceiver.onReceive", "StartLauncherFromNotificationReceiver_onReceive");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "action="+action);

                if (action.equals(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {
                    final Context appContext = context.getApplicationContext();
                    //Handler _handler = new Handler(appContext.getMainLooper());
                    PPApplication.startHandlerThreadBroadcast(/*"WifiAPStateChangeBroadcastReceiver.onReceive"*/);
                    final Handler _handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    Runnable r = () -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=StartLauncherFromNotificationReceiver.onReceive");

                        // intent to LauncherActivity, for click on notification
                        Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                        // clear all opened activities
                        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK/*|Intent.FLAG_ACTIVITY_NO_ANIMATION*/);
                        // setup startupSource
                        launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                        appContext.startActivity(launcherIntent);
                    };
                    //PPApplication.logE("StartLauncherFromNotificationReceiver.onReceive", "PPApplication.deviceIsSamsung="+PPApplication.deviceIsSamsung);
                    if ((Build.VERSION.SDK_INT >= 29) &&
                            ApplicationPreferences.applicationNotificationLauncher.equals("activator")) {
                        if (PPApplication.deviceIsSamsung) {
                            if (Build.VERSION.SDK_INT >= 30)
                                _handler.postDelayed(r, 500);
                            else
                                _handler.postDelayed(r, 1000);
                        }
                        else
                            _handler.postDelayed(r, 500);
                    }
                    else
                        _handler.post(r);
                }
            }
        }
    }

}
