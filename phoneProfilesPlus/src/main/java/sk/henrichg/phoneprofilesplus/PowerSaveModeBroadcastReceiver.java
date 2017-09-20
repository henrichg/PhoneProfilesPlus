package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "PowerSaveModeBroadcastReceiver.onReceive", "PowerSaveModeBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        try {
            Intent serviceIntent = new Intent(context, PowerSaveModeService.class);
            WakefulIntentService.sendWakefulWork(context, serviceIntent);
        } catch (Exception ignored) {}
    }
}
