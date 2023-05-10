package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if (PPApplicationStatic.logEnabled()) {
//            PPApplicationStatic.logE("[IN_BROADCAST] ShutdownBroadcastReceiver.onReceive", "xxx");
//            PPApplicationStatic.logE("PPApplication.exitApp", "from ShutdownBroadcastReceiver.onReceive shutdown=true");
        //}

        // !!! Do not use handler !!!
        Context appContext = context.getApplicationContext();
        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        PPApplicationStatic.exitApp(false, appContext, dataWrapper, null, true, false);
    }
}
