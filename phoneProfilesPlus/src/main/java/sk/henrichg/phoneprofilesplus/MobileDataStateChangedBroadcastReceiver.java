package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class MobileDataStateChangedBroadcastReceiver extends BroadcastReceiver {

    public static final String EXTRA_STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### MobileDataStateChangedBroadcastReceiver.onReceive", "xxx");

        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(PPApplication.mobileDataStateChangedBroadcastReceiver);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(context);

        final boolean state = intent.getBooleanExtra("state", false);

        /*Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_MOBILE_DATA);
        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
        context.sendBroadcast(broadcastIntent);*/
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(PPApplication.radioSwitchBroadcastReceiver, new IntentFilter("RadioSwitchBroadcastReceiver"));
        Intent broadcastIntent = new Intent("RadioSwitchBroadcastReceiver");
        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_MOBILE_DATA);
        broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, state);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);

    }

}
