package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class NFCStateChangedBroadcastReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NFCStateChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NFCStateChangedBroadcastReceiver.onReceive", "NFCStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(context)) {
            final String action = intent.getAction();

            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

                if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                    EventsHandlerJob.startForRadioSwitchSensor(EventPreferencesRadioSwitch.RADIO_TYPE_NFC, state == NfcAdapter.STATE_ON);
                }
            }
        }
    }
}
