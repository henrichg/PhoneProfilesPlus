package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnOffBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "screenOnOff";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### ScreenOnOffBroadcastReceiver.onReceive", "xxx");
        if (intent != null)
            PPApplication.logE("ScreenOnOffBroadcastReceiver.onReceive", "intent.getAction()="+intent.getAction());

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(appContext, ScreenOnOffService.class);
        serviceIntent.setAction(intent.getAction());
        context.startService(serviceIntent);
    }

}
