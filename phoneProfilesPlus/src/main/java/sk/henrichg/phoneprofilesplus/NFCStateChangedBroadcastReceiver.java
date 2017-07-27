package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class NFCStateChangedBroadcastReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NFCStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRuning(context)) {
            final String action = intent.getAction();

            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

                if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_NFC);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, state == NfcAdapter.STATE_ON);
                    WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);
                }
            }
        }
    }
}
