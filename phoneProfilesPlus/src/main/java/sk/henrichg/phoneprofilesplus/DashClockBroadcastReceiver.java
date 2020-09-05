package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = PPApplication.PACKAGE_NAME + ".REFRESH_DASHCLOCK";

    //static final String EXTRA_REFRESH = "refresh";

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] DashClockBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "DashClockBroadcastReceiver.onReceive", "DashClockBroadcastReceiver_onReceive");

        PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
        if (dashClockExtension != null) {
            dashClockExtension.updateExtension();
        }

        //final boolean refresh = (intent == null) || intent.getBooleanExtra(EXTRA_REFRESH, true);

        /*PPApplication.startHandlerThreadBroadcast();
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DashClockBroadcastReceiver.onReceive");

                PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
                if (dashClockExtension != null)
                {
                    dashClockExtension.updateExtension();
                }
                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DashClockBroadcastReceiver.onReceive");
            }
        });*/

    }

}
