package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class NFCStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### NFCStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        final String action = intent.getAction();

        if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
            final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

            if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
                broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_NFC);
                broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, state == NfcAdapter.STATE_ON);
                context.sendBroadcast(broadcastIntent);
            }
        }
    }
}
