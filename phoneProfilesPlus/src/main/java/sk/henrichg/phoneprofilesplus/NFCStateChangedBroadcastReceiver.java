package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.PowerManager;

public class NFCStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("[BROADCAST CALL] NFCStateChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "NFCStateChangedBroadcastReceiver.onReceive", "NFCStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            final String action = intent.getAction();

            if ((action != null) && action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

                if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThread(/*"NFCStateChangedBroadcastReceiver.onReceive"*/);
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NFCStateChangedBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=NFCStateChangedBroadcastReceiver.onReceive");

                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=NFCStateChangedBroadcastReceiver.onReceive");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
