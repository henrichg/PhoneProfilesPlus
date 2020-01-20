package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;

public class HeadsetConnectionBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_EVENT_WIRED_HEADSET_CONNECTED = "eventWiredHeadsetConnected";
    private static final String PREF_EVENT_WIRED_HEADSET_MICROPHONE = "eventWiredHeadsetMicrophone";
    private static final String PREF_EVENT_BLUETOOTH_HEADSET_CONNECTED = "eventBluetoothHeadsetConnected";
    private static final String PREF_EVENT_BLUETOOTH_HEADSET_MICROPHONE = "eventBluetoothHeadsetMicrophone";

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### HeadsetConnectionBroadcastReceiver.onReceive","xxx");

        //CallsCounter.logCounter(context, "HeadsetConnectionBroadcastReceiver.onReceive", "HeadsetConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (intent == null)
            return;

        String action = intent.getAction();

        boolean broadcast = false;

        boolean connectedWiredHeadphones = false;
        boolean connectedWiredMicrophone = false;
        boolean connectedBluetoothHeadphones = false;
        boolean connectedBluetoothMicrophone = false;

        if (action != null) {
            // Wired headset monitoring
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                connectedWiredHeadphones = (intent.getIntExtra("state", -1) == 1);
                connectedWiredMicrophone = (intent.getIntExtra("microphone", -1) == 1);

                broadcast = true;
            }

            // Bluetooth monitoring
            // Works up to and including Honeycomb
            if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                connectedBluetoothHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED) == BluetoothHeadset.STATE_AUDIO_CONNECTED);
                connectedBluetoothMicrophone = true;

                broadcast = true;
            }

            // Works for Ice Cream Sandwich
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                connectedBluetoothHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED) == BluetoothProfile.STATE_CONNECTED);
                connectedBluetoothMicrophone = true;

                broadcast = true;
            }
        }

        //PPApplication.logE("@@@ HeadsetConnectionBroadcastReceiver.onReceive","broadcast="+broadcast);

        if (broadcast)
        {
            setEventHeadsetParameters(appContext, connectedWiredHeadphones, connectedWiredMicrophone,
                    connectedBluetoothHeadphones, connectedBluetoothMicrophone);
        }

        if (Event.getGlobalEventsRunning())
        {
            if (broadcast)
            {
                PPApplication.startHandlerThread("HeadsetConnectionBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":HeadsetConnectionBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=HeadsetConnectionBroadcastReceiver.onReceive");

                            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                            boolean peripheralEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL) > 0;
                            dataWrapper.invalidateDataWrapper();

                            if (peripheralEventsExists)
                            {*/
                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_HEADSET_CONNECTION);
                            //}

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=HeadsetConnectionBroadcastReceiver.onReceive");
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

    static void getEventHeadsetParameters(Context context) {
        synchronized (PPApplication.eventPeripheralsSensorMutex) {
            SharedPreferences preferences = ApplicationPreferencesLoader.getSharedPreferences(context);
            ApplicationPreferences.prefWiredHeadsetConnected = preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_WIRED_HEADSET_CONNECTED, false);
            ApplicationPreferences.prefWiredHeadsetMicrophone = preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_WIRED_HEADSET_MICROPHONE, false);
            ApplicationPreferences.prefBluetoothHeadsetConnected = preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_BLUETOOTH_HEADSET_CONNECTED, false);
            ApplicationPreferences.prefBluetoothHeadsetMicrophone = preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_BLUETOOTH_HEADSET_MICROPHONE, false);
        }
    }
    private static void setEventHeadsetParameters(Context context, boolean connectedWiredHeadphones, boolean connectedWiredMicrophone,
                                                boolean connectedBluetoothHeadphones, boolean connectedBluetoothMicrophone) {
        synchronized (PPApplication.eventPeripheralsSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferencesLoader.getEditor(context);
            editor.putBoolean(PREF_EVENT_WIRED_HEADSET_CONNECTED, connectedWiredHeadphones);
            editor.putBoolean(PREF_EVENT_WIRED_HEADSET_MICROPHONE, connectedWiredMicrophone);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_HEADSET_CONNECTED, connectedBluetoothHeadphones);
            editor.putBoolean(PREF_EVENT_BLUETOOTH_HEADSET_MICROPHONE, connectedBluetoothMicrophone);
            editor.apply();
            ApplicationPreferences.prefWiredHeadsetConnected = connectedWiredHeadphones;
            ApplicationPreferences.prefWiredHeadsetMicrophone = connectedWiredMicrophone;
            ApplicationPreferences.prefBluetoothHeadsetConnected = connectedBluetoothHeadphones;
            ApplicationPreferences.prefBluetoothHeadsetMicrophone = connectedBluetoothMicrophone;
        }
    }

}
