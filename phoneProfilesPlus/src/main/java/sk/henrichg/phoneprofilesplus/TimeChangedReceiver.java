package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null)) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_CHANGED)) {
                PPApplication.logE("##### TimeChangedReceiver.onReceive", "xxx");
                CallsCounter.logCounter(context, "TimeChangedReceiver.onReceive", "TimeChangedReceiver_onReceive");

                Context appContext = context.getApplicationContext();

                if (!PPApplication.getApplicationStarted(appContext, true))
                    return;

                boolean timeChanged = true;

                if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                    timeChanged = false;
                    String isAutoTime = Settings.Global.getString(appContext.getContentResolver(), Settings.Global.AUTO_TIME);
                    if (isAutoTime.equals("0")) {
                        timeChanged = true;
                    }
                }

                if (timeChanged) {
                    if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                            ApplicationPreferences.applicationUseAlarmClock(context)) {
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                        Profile.setActivatedProfileForDuration(context, 0);
                    }

                    SearchCalendarEventsJob.scheduleJob(/*appContext, */true, null, true);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                    dataWrapper.clearSensorsStartTime();
                    dataWrapper.restartEvents(false, true/*, false*/, false, true);
                }
            }
        }
    }
}
