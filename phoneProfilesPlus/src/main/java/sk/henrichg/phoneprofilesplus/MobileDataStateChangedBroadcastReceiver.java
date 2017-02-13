package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class MobileDataStateChangedBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "mobileDataState";

    public static final String EXTRA_STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### MobileDataStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        final boolean state = intent.getBooleanExtra("state", false);

        Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
        broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_MOBILE_DATA);
        broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
        context.sendBroadcast(broadcastIntent);

    }

}
