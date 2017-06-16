package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class RestartEventsFromNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### RestartEventsFromNotificationBroadcastReceiver.onReceive", "xxx");

        //Context appContext = context.getApplicationContext();

        //PPApplication.loadPreferences(appContext);

        Intent activityIntent = new Intent(context.getApplicationContext(), RestartEventsFromNotificationActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);

    }
}
