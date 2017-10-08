package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class HeadsetConnectionJob extends Job {

    static final String JOB_TAG  = "HeadsetConnectionJob";

    private static final String EXTRA_ACTION = "action";
    
    static final String PREF_EVENT_HEADSET_CONNECTED = "eventHeadsetConnected";
    static final String PREF_EVENT_HEADSET_MICROPHONE = "eventHeadsetMicrophone";
    static final String PREF_EVENT_HEADSET_BLUETOOTH = "eventHeadsetBluetooth";
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "HeadsetConnectionJob.onRunJob", "HeadsetConnectionJob_onRunJob");

        boolean broadcast = false;

        boolean connectedHeadphones = false;
        boolean connectedMicrophone = false;
        boolean bluetoothHeadset = false;

        Bundle bundle = params.getTransientExtras();
        String action = bundle.getString(EXTRA_ACTION, "");
        CallsCounter.logCounterNoInc(appContext, "HeadsetConnectionJob.onRunJob->action="+action, "HeadsetConnectionJob_onRunJob");

        // Wired headset monitoring
        if (action.equals(Intent.ACTION_HEADSET_PLUG))
        {
            connectedHeadphones = (bundle.getInt(HeadsetConnectionBroadcastReceiver.EXTRA_HEADSET_PLUG_STATE, 0) == 1);
            connectedMicrophone = (bundle.getInt(HeadsetConnectionBroadcastReceiver.EXTRA_HEADSET_PLUG_MICROPHONE, 0) == 1);
            bluetoothHeadset = false;

            broadcast = true;
        }

        // Bluetooth monitoring
        // Works up to and including Honeycomb
        if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))
        {
            connectedHeadphones = (bundle.getInt(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED) == BluetoothHeadset.STATE_AUDIO_CONNECTED);
            connectedMicrophone = true;
            bluetoothHeadset = true;

            broadcast = true;
        }

        // Works for Ice Cream Sandwich
        if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
        {
            connectedHeadphones = (bundle.getInt(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED) == BluetoothProfile.STATE_CONNECTED);
            connectedMicrophone = true;
            bluetoothHeadset = true;

            broadcast = true;
        }

        PPApplication.logE("@@@ HeadsetConnectionJob.onRunJob","broadcast="+broadcast);

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
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_HEADSET_CONNECTION, false);
                //}
            }
        }
        
        return Result.SUCCESS;
    }

    static void startForHeadsetPlug(int state, int microphone) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, Intent.ACTION_HEADSET_PLUG);
        bundle.putInt(HeadsetConnectionBroadcastReceiver.EXTRA_HEADSET_PLUG_STATE, state);
        bundle.putInt(HeadsetConnectionBroadcastReceiver.EXTRA_HEADSET_PLUG_MICROPHONE, microphone);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForBluetoothPlug(String action, int state) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, action);
        bundle.putInt(BluetoothProfile.EXTRA_STATE, state);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }
    
}
