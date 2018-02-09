package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null)) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                PPApplication.logE("##### TimeChangedReceiver.onReceive", "xxx");
                CallsCounter.logCounter(context, "TimeChangedReceiver.onReceive", "TimeChangedReceiver_onReceive");

                Context appContext = context.getApplicationContext();

                if (!PPApplication.getApplicationStarted(appContext, true))
                    return;

                SearchCalendarEventsJob.scheduleJob(/*appContext, */true, null, true);
                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                dataWrapper.restartEvents(false, true/*, false*/);
            }
        }
    }
}
