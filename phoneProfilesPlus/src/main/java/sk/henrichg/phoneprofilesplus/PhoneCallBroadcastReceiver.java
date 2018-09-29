package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import java.util.Date;

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

    private static final int SERVICE_PHONE_EVENT_START = 1;
    private static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    private static final int SERVICE_PHONE_EVENT_END = 3;

    static final int CALL_EVENT_UNDEFINED = 0;
    static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;
    static final int CALL_EVENT_MISSED_CALL = 7;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    static final int LINKMODE_UNLINK = 2;

    static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
    static final String PREF_EVENT_CALL_EVENT_TIME = "eventCallEventTime";

    protected boolean onStartReceive()
    {
        PPApplication.logE("##### PhoneCallBroadcastReceiver.onReceive", "xxx");

        return PPApplication.getApplicationStarted(savedContext, true);
    }

    protected void onEndReceive()
    {
    }

    protected void onIncomingCallStarted(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, false, number, eventTime);
    }

    protected void onOutgoingCallStarted(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false, number, eventTime);
    }
    
    protected void onIncomingCallAnswered(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, false, number, eventTime);
    }

    protected void onOutgoingCallAnswered(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, false, number, eventTime);
    }
    
    protected void onIncomingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, false, number, eventTime);
    }

    protected void onOutgoingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, false, number, eventTime);
    }

    protected void onMissedCall(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, true, number, eventTime);
    }

    private void doCall(final Context context, final int phoneEvent,
                            final boolean incoming, final boolean missed,
                            final String number, final Date eventTime) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread("PhoneCallBroadcastReceiver.doCall");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                switch (phoneEvent) {
                    case SERVICE_PHONE_EVENT_START:
                        callStarted(incoming, number, eventTime, appContext);
                        break;
                    case SERVICE_PHONE_EVENT_ANSWER:
                        callAnswered(incoming, number, eventTime, appContext);
                        break;
                    case SERVICE_PHONE_EVENT_END:
                        callEnded(incoming, missed, number, eventTime, appContext);
                        break;
                }
            }
        });
    }

    private static void doCallEvent(int eventType, String phoneNumber, Date eventTime, Context context)
    {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME+":PhoneCallBroadcastReceiver.doCallEvent");
            wakeLock.acquire(10 * 60 * 1000);
        }

        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.putLong(PREF_EVENT_CALL_EVENT_TIME, eventTime.getTime());
        editor.apply();

        linkUnlinkExecuted = false;
        speakerphoneOnExecuted = false;

        // start events handler
        // handlerThread is used in doCall
        EventsHandler eventsHandler = new EventsHandler(context);
        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_CALL/*, false*/);

        if ((wakeLock != null) && wakeLock.isHeld()) {
            try {
                wakeLock.release();
            } catch (Exception ignored) {}
        }
    }

    private static void callStarted(boolean incoming, String phoneNumber, Date eventTime, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming) {
            doCallEvent(CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber, eventTime, context);
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

    private static void callAnswered(boolean incoming, String phoneNumber, Date eventTime, Context context)
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

        // audio mode is set to MODE_IN_CALL by system

        // setSpeakerphoneOn() moved to ActivateProfileHelper.executeForVolumes

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().stopSimulatingRingingCall(/*true*/);

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber, eventTime, context);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber, eventTime, context);
    }

    private static void callEnded(boolean incoming, boolean missed, String phoneNumber, Date eventTime, Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().stopSimulatingRingingCall(/*true*/);

        // audio mode is set to MODE_IN_CALL by system

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

        // audio mode is set to MODE_NORMAL by system

        PPApplication.logE("PhoneCallBroadcastReceiver.callEnded", "incoming="+incoming);
        PPApplication.logE("PhoneCallBroadcastReceiver.callEnded", "missed="+missed);
        PPApplication.logE("PhoneCallBroadcastReceiver.callEnded", "phoneNumber="+phoneNumber);

        if (incoming) {
            if (missed)
                doCallEvent(CALL_EVENT_MISSED_CALL, phoneNumber, eventTime, context);
            else
                doCallEvent(CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, eventTime, context);
        }
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, eventTime, context);

    }

}
