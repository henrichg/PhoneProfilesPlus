package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("ShutdownBroadcastReceiver.onReceive", "xxx");
        PPApplication.logE("PPApplication.exitApp", "from ShutdownBroadcastReceiver.onReceive shutdown=true");
        // !!! Do not use handler !!!
        PPApplication.exitApp(false, context.getApplicationContext(), null, null, true/*, false, true*/);
    }
}
