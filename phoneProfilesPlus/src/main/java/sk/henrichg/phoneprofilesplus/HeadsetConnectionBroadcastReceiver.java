package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class HeadsetConnectionBroadcastReceiver extends BroadcastReceiver {

    //static final String EXTRA_HEADSET_PLUG_STATE = "state";
    //static final String EXTRA_HEADSET_PLUG_MICROPHONE = "microphone";

    static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
    static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
    static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### HeadsetConnectionBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "HeadsetConnectionBroadcastReceiver.onReceive", "HeadsetConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        String action = intent.getAction();

        /*if (action.equals(Intent.ACTION_HEADSET_PLUG))
            HeadsetConnectionJob.startForHeadsetPlug(appContext, intent.getIntExtra("state", -1), intent.getIntExtra("microphone", -1));
        else
        if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))
            HeadsetConnectionJob.startForBluetoothPlug(appContext, BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED));
        else
        if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
            HeadsetConnectionJob.startForBluetoothPlug(appContext, BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED, intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED));*/

        boolean broadcast = false;

        boolean connectedHeadphones = false;
        boolean connectedMicrophone = false;
        boolean bluetoothHeadset = false;

        if (action != null) {
            // Wired headset monitoring
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                connectedHeadphones = (intent.getIntExtra("state", -1) == 1);
                connectedMicrophone = (intent.getIntExtra("microphone", -1) == 1);
                bluetoothHeadset = false;

                broadcast = true;
            }

            // Bluetooth monitoring
            // Works up to and including Honeycomb
            if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED) == BluetoothHeadset.STATE_AUDIO_CONNECTED);
                connectedMicrophone = true;
                bluetoothHeadset = true;

                broadcast = true;
            }

            // Works for Ice Cream Sandwich
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED) == BluetoothProfile.STATE_CONNECTED);
                connectedMicrophone = true;
                bluetoothHeadset = true;

                broadcast = true;
            }
        }

        PPApplication.logE("@@@ HeadsetConnectionBroadcastReceiver.onReceive","broadcast="+broadcast);

        if (broadcast)
        {
            ApplicationPreferences.getSharedPreferences(appContext);
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_EVENT_HEADSET_CONNECTED, connectedHeadphones);
            editor.putBoolean(PREF_EVENT_HEADSET_MICROPHONE, connectedMicrophone);
            editor.putBoolean(PREF_EVENT_HEADSET_BLUETOOTH, bluetoothHeadset);
            editor.apply();
        }

        if (Event.getGlobalEventsRunning(appContext))
        {
            if (broadcast)
            {
                PhoneProfilesService.startHandlerThread();
                final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeadsetConnectionBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                        boolean peripheralEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL) > 0;
                        dataWrapper.invalidateDataWrapper();

                        if (peripheralEventsExists)
                        {*/
                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_HEADSET_CONNECTION/*, false*/);
                        //}

                        if ((wakeLock != null) && wakeLock.isHeld())
                            wakeLock.release();
                    }
                });
            }
        }

    }
}
