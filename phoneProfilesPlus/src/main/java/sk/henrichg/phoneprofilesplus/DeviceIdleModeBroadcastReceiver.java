package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class DeviceIdleModeBroadcastReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "DeviceIdleModeBroadcastReceiver.onReceive", "DeviceIdleModeBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        try {
            Intent serviceIntent = new Intent(context, DeviceIdleModeService.class);
            serviceIntent.setAction(intent.getAction());
            WakefulIntentService.sendWakefulWork(context, serviceIntent);
        } catch (Exception ignored) {}

    }
}
