package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.SystemClock;

public class PhoneCallService extends IntentService {

    private Context context;
    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    public static boolean linkUnlinkExecuted = false;
    public static boolean speakerphoneOnExecuted = false;

    public static final int CALL_EVENT_UNDEFINED = 0;
    public static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //public static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    public static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    public static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    public static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    public static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;

    public static final int LINKMODE_NONE = 0;
    public static final int LINKMODE_LINK = 1;
    public static final int LINKMODE_UNLINK = 2;

    static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";

    public PhoneCallService() {
        super("PhoneCallService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        if (intent != null) {

            context = getApplicationContext();

            int phoneEvent = intent.getIntExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_EVENT, 0);
            boolean incoming = intent.getBooleanExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_INCOMING, true);
            String number = intent.getStringExtra(PhoneCallBroadcastReceiver.EXTRA_SERVICE_PHONE_NUMBER);

            switch (phoneEvent) {
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_START:
                    callStarted(incoming, number);
                    break;
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_ANSWER:
                    callAnswered(incoming, number);
                    break;
                case PhoneCallBroadcastReceiver.SERVICE_PHONE_EVENT_END:
                    callEnded(incoming, number);
                    break;
            }
        }

        /* wait is in EventsService after profile activation
        try {
            Thread.sleep(1000); // // 1 second for EventsService
        } catch (InterruptedException e) {
        }*/

        if (intent != null)
            PhoneCallBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void doCallEvent(int eventType, String phoneNumber)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(PhoneCallService.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.apply();

        linkUnlinkExecuted = false;
        speakerphoneOnExecuted = false;

        // start service
        Intent eventsServiceIntent = new Intent(context, EventsService.class);
        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, PhoneCallBroadcastReceiver.BROADCAST_RECEIVER_TYPE);
        context.startService(eventsServiceIntent);

    }

    private void callStarted(boolean incoming, String phoneNumber)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming) {
            doCallEvent(CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber);
        }
    }

    public static void setSpeakerphoneOn(Profile profile, Context context) {
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

    private void callAnswered(boolean incoming, String phoneNumber)
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
        //Log.e("PhoneCallService", "callAnswered audioMode=" + audioManager.getMode());

        // setSpeakerphoneOn() moved to ExecuteVolumeProfilePrefsService and EventsService

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.stopSimulatingRingingCall();

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber);
    }

    private void callEnded(boolean incoming, String phoneNumber)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.stopSimulatingRingingCall();

        // audiomode is set to MODE_IN_CALL by system
        //Log.e("PhoneCallService", "callEnded (before back speaker phone) audioMode="+audioManager.getMode());

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
        //Log.e("PhoneCallService", "callEnded (before unlink/EventsService) audioMode="+audioManager.getMode());

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber);

    }

}
