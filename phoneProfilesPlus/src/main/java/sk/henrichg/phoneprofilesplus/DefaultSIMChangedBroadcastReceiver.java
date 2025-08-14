package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DefaultSIMChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] DefaultSIMChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

//        final Intent _intent = intent;

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        PPExecutors.handleEvents(appContext,
                new int[]{EventsHandler.SENSOR_TYPE_RADIO_SWITCH},
                PPExecutors.SENSOR_NAME_SENSOR_TYPE_RADIO_SWITCH, 0, false);
    }

}
