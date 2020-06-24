package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CheckWorkManagerBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### DonationBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "DonationBroadcastReceiver.onReceive", "DonationBroadcastReceiver_onReceive");

        if (intent != null) {
            PPApplication.logE("CheckWorkManagerBroadcastReceiver.onReceive", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);
            if (!PPApplication.applicationFullyStarted) {

            }
        }
    }

}
