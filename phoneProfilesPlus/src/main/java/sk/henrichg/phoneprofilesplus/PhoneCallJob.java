package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class PhoneCallJob extends Job {

    static final String JOB_TAG  = "PhoneCallJob";

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    static boolean linkUnlinkExecuted = false;
    static boolean speakerphoneOnExecuted = false;

    static final int CALL_EVENT_UNDEFINED = 0;
    static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    static final int LINKMODE_UNLINK = 2;

    static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "PhoneCallJob.onRunJob", "PhoneCallJob_onRunJob");

        Bundle bundle = params.getTransientExtras();

        int phoneEvent = bundle.getInt(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, 0);
        boolean incoming = bundle.getBoolean(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, true);
        String number = bundle.getString(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER);

        switch (phoneEvent) {
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_START:
                callStarted(incoming, number, appContext);
                break;
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_ANSWER:
                callAnswered(incoming, number, appContext);
                break;
            case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_END:
                callEnded(incoming, number, appContext);
                break;
        }
        
        return Result.SUCCESS;
    }

    static void start(Context context, int phoneEvent, boolean incoming, String number) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putInt(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, phoneEvent);
        bundle.putBoolean(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, incoming);
        bundle.putString(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER, number);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

    private void doCallEvent(int eventType, String phoneNumber, Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PhoneCallJob.PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(PhoneCallJob.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.apply();

        linkUnlinkExecuted = false;
        speakerphoneOnExecuted = false;

        // start events handler
        EventsHandler eventsHandler = new EventsHandler(context);
        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_CALL, false);
    }

    private void callStarted(boolean incoming, String phoneNumber, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming) {
            doCallEvent(CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber, context);
        }
    }

    static void setSpeakerphoneOn(Profile profile, Context context) {
        if (profile != null) {

            if (profile._volumeSpeakerPhone != 0) {

                if (audioManager == null )
                    audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

                savedSpeakerphone = audioManager.isSpeakerphoneOn();
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone
                    audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);
                    speakerphoneSelected = true;
                }

            }
        }
    }

    private void callAnswered(boolean incoming, String phoneNumber, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // Delay 2 seconds mode changed to MODE_IN_CALL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_IN_CALL)
                //try { Thread.sleep(100); } catch (InterruptedException e) {};
                SystemClock.sleep(100);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 2000);

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallJob", "callAnswered audioMode=" + audioManager.getMode());

        // setSpeakerphoneOn() moved to ExecuteVolumeProfilePrefsJob and EventsHandler

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.stopSimulatingRingingCall(true);

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber, context);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber, context);
    }

    private void callEnded(boolean incoming, String phoneNumber, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.stopSimulatingRingingCall(true);

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallJob", "callEnded (before back speaker phone) audioMode="+audioManager.getMode());

        if (speakerphoneSelected)
        {
            audioManager.setSpeakerphoneOn(savedSpeakerphone);
        }

        speakerphoneSelected = false;

        // Delay 2 seconds mode changed to MODE_NORMAL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_NORMAL)
                //try { Thread.sleep(100); } catch (InterruptedException e) {};
                SystemClock.sleep(100);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 2000);

        // audiomode is set to MODE_NORMAL by system
        //Log.e("PhoneCallJob", "callEnded (before unlink/EventsHandler) audioMode="+audioManager.getMode());

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, context);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, context);

    }
    
}
