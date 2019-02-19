package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = "sk.henrichg.phoneprofilesplus.REFRESH_DASHCLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DashClockBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "DashClockBroadcastReceiver.onReceive", "DashClockBroadcastReceiver_onReceive");

        //DashClockJob.start(context.getApplicationContext());
        //final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread("DashClockBroadcastReceiver.onReceive");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DashClockBroadcastReceiver.onReceive");

                PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
                if (dashClockExtension != null)
                {
                    dashClockExtension.updateExtension();
                }

                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DashClockBroadcastReceiver.onReceive");
            }
        });

    }

}
