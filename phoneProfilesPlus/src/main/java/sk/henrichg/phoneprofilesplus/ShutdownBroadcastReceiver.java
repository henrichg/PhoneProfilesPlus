package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("ShutdownBroadcastReceiver.onReceive", "xxx");
        //DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, false, 0);
        PPApplication.exitApp(true, context.getApplicationContext(), null, null, true/*, false, true*/);
    }
}
