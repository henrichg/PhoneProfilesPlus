package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import static android.content.Context.POWER_SERVICE;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    static boolean linkUnlinkExecuted = false;
    static boolean speakerphoneOnExecuted = false;

    //public static final String EXTRA_SERVICE_PHONE_EVENT = "service_phone_event";
    //public static final String EXTRA_SERVICE_PHONE_INCOMING = "service_phone_incoming";
    //public static final String EXTRA_SERVICE_PHONE_NUMBER = "service_phone_number";

    public static final int SERVICE_PHONE_EVENT_START = 1;
    public static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    public static final int SERVICE_PHONE_EVENT_END = 3;

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

    protected boolean onStartReceive()
    {
        PPApplication.logE("##### PhoneCallBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(savedContext, false))
            return false;

        return true;
    }

    protected void onEndReceive()
    {
    }

    protected void onIncomingCallStarted(String number/*, Date start*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, number);
    }

    protected void onOutgoingCallStarted(String number/*, Date start*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, false, number);
    }
    
    protected void onIncomingCallAnswered(String number/*, Date start*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, number);
    }

    protected void onOutgoingCallAnswered(String number/*, Date start*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, number);
    }
    
    protected void onIncomingCallEnded(String number/*, Date start, Date end*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, number);
    }

    protected void onOutgoingCallEnded(String number/*, Date start, Date end*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, number);
    }

    protected void onMissedCall(String number/*, Date start*/)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, number);
    }

    private void doCall(final Context context, final int phoneEvent, final boolean incoming, final String number) {
        final Context appContext = context.getApplicationContext();
        final Handler handler = new Handler(appContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    private void doCallEvent(int eventType, String phoneNumber, Context context)
    {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhoneCallBroadcastReceiver.doCallEvent");
            wakeLock.acquire(10 * 60 * 1000);
        }

        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PhoneCallBroadcastReceiver.PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(PhoneCallBroadcastReceiver.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.apply();

        linkUnlinkExecuted = false;
        speakerphoneOnExecuted = false;

        // start events handler
        EventsHandler eventsHandler = new EventsHandler(context);
        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_CALL, false);

        if (wakeLock != null)
            wakeLock.release();
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
                if (audioManager != null)
                    savedSpeakerphone = audioManager.isSpeakerphoneOn();
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone
                    if (audioManager != null)
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
        //Log.e("PhoneCallBroadcastReceiver", "callAnswered audioMode=" + audioManager.getMode());

        // setSpeakerphoneOn() moved to ActivateProfileHelper.executeForVolumes

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
        //Log.e("PhoneCallBroadcastReceiver", "callEnded (before back speaker phone) audioMode="+audioManager.getMode());

        if (speakerphoneSelected)
        {
            if (audioManager != null)
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
        //Log.e("PhoneCallBroadcastReceiver", "callEnded (before unlink/EventsHandler) audioMode="+audioManager.getMode());

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, context);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, context);

    }

}
