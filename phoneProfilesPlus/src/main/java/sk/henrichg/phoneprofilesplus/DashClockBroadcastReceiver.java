package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = "sk.henrichg.phoneprofilesplus.REFRESH_DASHCLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### DashClockBroadcastReceiver.onReceive", "xxx");

        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(PPApplication.dashClockBroadcastReceiver);

        PhoneProfilesDashClockExtension dashClockExtension =
                PhoneProfilesDashClockExtension.getInstance();
        if (dashClockExtension != null)
        {
            //PPApplication.loadPreferences(context);
            dashClockExtension.updateExtension();
        }

    }

}
