package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = PPApplication.PACKAGE_NAME + ".REFRESH_DASHCLOCK";

    //static final String EXTRA_REFRESH = "refresh";

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] DashClockBroadcastReceiver.onReceive", "xxx");

        PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
        if (dashClockExtension != null) {
            dashClockExtension.updateExtension();
        }
    }

}
