package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;

import static android.content.Context.POWER_SERVICE;

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

            if ((action != null) && action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

                if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                    //EventsHandlerJob.startForSensor(context.getApplicationContext(), EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                    final Context appContext = context.getApplicationContext();
                    PhoneProfilesService.startHandlerThread();
                    final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NFCStateChangedBroadcastReceiver.onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false);

                            if ((wakeLock != null) && wakeLock.isHeld())
                                wakeLock.release();
                        }
                    });
                }
            }
        }
    }
}
