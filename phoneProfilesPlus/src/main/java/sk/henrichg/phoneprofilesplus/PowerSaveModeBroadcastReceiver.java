package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "PowerSaveModeBroadcastReceiver.onReceive", "PowerSaveModeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PowerSaveModeJob.start(appContext);

        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        appContext.startService(serviceIntent);
        //else
        //    context.startForegroundService(serviceIntent);

        final Handler handler = new Handler(appContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE, false);
            }
        });

    }
}
