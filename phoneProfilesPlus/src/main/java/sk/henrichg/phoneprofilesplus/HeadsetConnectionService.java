package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class HeadsetConnectionService extends WakefulIntentService {

    static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
    static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
    static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";

    public HeadsetConnectionService() {
        super("HeadsetConnectionService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### HeadsetConnectionService.doWakefulWork","xxx");

            Context appContext = getApplicationContext();

            boolean broadcast = false;

            boolean connectedHeadphones = false;
            boolean connectedMicrophone = false;
            boolean bluetoothHeadset = false;

            String action = intent.getAction();

            // Wired headset monitoring
            if (action.equals(Intent.ACTION_HEADSET_PLUG))
            {
                connectedHeadphones = (intent.getIntExtra("state", 0) == 1);
                connectedMicrophone = (intent.getIntExtra("microphone", 0) == 1);
                bluetoothHeadset = false;

                broadcast = true;
            }

            // Bluetooth monitoring
            // Works up to and including Honeycomb
            if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))
            {
                connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED) == BluetoothHeadset.STATE_AUDIO_CONNECTED);
                connectedMicrophone = true;
                bluetoothHeadset = true;

                broadcast = true;
            }

            // Works for Ice Cream Sandwich
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
            {
                connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED) == BluetoothProfile.STATE_CONNECTED);
                connectedMicrophone = true;
                bluetoothHeadset = true;

                broadcast = true;
            }

            PPApplication.logE("@@@ HeadsetConnectionService.doWakefulWork","broadcast="+broadcast);

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
                    /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                    boolean peripheralEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL) > 0;
                    dataWrapper.invalidateDataWrapper();

                    if (peripheralEventsExists)
                    {*/
                    // start service
                    Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_HEADSET_CONNECTION);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                    //}
                }
            }
        }
    }

}
