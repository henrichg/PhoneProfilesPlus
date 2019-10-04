package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.notification.StatusBarNotification;

// Disable action button
public class StartLauncherFromNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### StartLauncherFromNotificationReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "StartLauncherFromNotificationReceiver.onReceive", "StartLauncherFromNotificationReceiver_onReceive");

        if (intent != null) {
            if (intent.getAction().equals(PhoneProfilesService.ACTION_START_LAUNCHER_FROM_NOTIFICATION)) {
                Context appContext = context.getApplicationContext();
                // intent to LauncherActivity, for click on notification
                Intent launcherIntent = new Intent(appContext, LauncherActivity.class);
                // clear all opened activities
                launcherIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                // setup startupSource
                launcherIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_NOTIFICATION);
                appContext.startActivity(launcherIntent);
            }
        }
    }
}
