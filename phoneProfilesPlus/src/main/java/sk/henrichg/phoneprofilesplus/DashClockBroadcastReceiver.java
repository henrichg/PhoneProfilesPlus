package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_REFRESH_DASHCLOCK = "sk.henrichg.phoneprofilesplus.REFRESH_DASHCLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### DashClockBroadcastReceiver.onReceive", "xxx");

        PhoneProfilesDashClockExtension dashClockExtension =
                PhoneProfilesDashClockExtension.getInstance();
        if (dashClockExtension != null)
        {
            //PPApplication.loadPreferences(context);
            dashClockExtension.updateExtension();
        }

    }

}
