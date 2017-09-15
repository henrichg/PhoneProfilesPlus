package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### TimeChangedReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            return;

        SearchCalendarEventsJob.scheduleJob(true);
        DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);
        dataWrapper.restartEvents(true, true, false);

    }
}
