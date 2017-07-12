package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class HeadsetConnectionBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "headsetConnection";

    static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
    static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
    static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";

    public static final String[] HEADPHONE_ACTIONS = {
        Intent.ACTION_HEADSET_PLUG,
        BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED,
        BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### HeadsetConnectionBroadcastReceiver.onReceive","xxx");

        Context appContext = context.getApplicationContext();

        boolean broadcast = false;

        boolean connectedHeadphones = false;
        boolean connectedMicrophone = false;
        boolean bluetoothHeadset = false;

        // Wired headset monitoring
        if (intent.getAction().equals(HEADPHONE_ACTIONS[0]))
        {
            connectedHeadphones = (intent.getIntExtra("state", 0) == 1);
            connectedMicrophone = (intent.getIntExtra("microphone", 0) == 1);
            bluetoothHeadset = false;

            broadcast = true;
        }

        // Bluetooth monitoring
        // Works up to and including Honeycomb
        if (intent.getAction().equals(HEADPHONE_ACTIONS[1]))
        {
            connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED) == BluetoothHeadset.STATE_AUDIO_CONNECTED);
            connectedMicrophone = true;
            bluetoothHeadset = true;
            
            broadcast = true;
        }

        // Works for Ice Cream Sandwich
        if (intent.getAction().equals(HEADPHONE_ACTIONS[2]))
        {
            connectedHeadphones = (intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED) == BluetoothProfile.STATE_CONNECTED);
            connectedMicrophone = true;
            bluetoothHeadset = true;
            
            broadcast = true;
        }

        if (broadcast)
        {
            PPApplication.logE("@@@ HeadsetConnectionBroadcastReceiver.onReceive","xxx");

            ApplicationPreferences.getSharedPreferences(appContext);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_EVENT_HEADSET_CONNECTED, connectedHeadphones);
            editor.putBoolean(PREF_EVENT_HEADSET_MICROPHONE, connectedMicrophone);
            editor.putBoolean(PREF_EVENT_HEADSET_BLUETOOTH, bluetoothHeadset);
            editor.apply();
        }

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);
        
        if (Event.getGlobalEventsRuning(appContext))
        {
            if (broadcast)
            {
                PPApplication.logE("@@@ HeadsetConnectionBroadcastReceiver.onReceive","xxx");

                /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                boolean peripheralEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_PERIPHERAL) > 0;
                dataWrapper.invalidateDataWrapper();

                if (peripheralEventsExists)
                {*/
                    // start service
                    Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                //}
            }

        }
    }
}
