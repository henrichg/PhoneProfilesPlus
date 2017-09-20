package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "ScreenOnOffBroadcastReceiver.onReceive", "ScreenOnOffBroadcastReceiver_onReceive");

        if (intent != null)
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());
        else
            return;

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        try {
            Intent serviceIntent = new Intent(appContext, ScreenOnOffService.class);
            serviceIntent.setAction(intent.getAction());
            context.startService(serviceIntent);
        } catch (Exception ignored) {}
    }

}
