package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;

public class StartEventNotificationDeletedReceiver extends BroadcastReceiver {

    static final String START_EVENT_NOTIFICATION_DELETED_ACTION = "sk.henrichg.phoneprofilesplus.START_EVENT_NOTIFICATION_DELETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### StartEventNotificationDeletedReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "StartEventNotificationDeletedReceiver.onReceive", "StartEventNotificationDeletedReceiver_onReceive");

        StartEventNotificationBroadcastReceiver.removeAlarm(context.getApplicationContext());
    }

}
