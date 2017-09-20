package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartEventsFromNotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### RestartEventsFromNotificationBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "RestartEventsFromNotificationBroadcastReceiver.onReceive", "RestartEventsFromNotificationBroadcastReceiver_onReceive");

        //Context appContext = context.getApplicationContext();

        try {
            Intent activityIntent = new Intent(context.getApplicationContext(), RestartEventsFromNotificationActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        } catch (Exception ignored) {}

    }
}
