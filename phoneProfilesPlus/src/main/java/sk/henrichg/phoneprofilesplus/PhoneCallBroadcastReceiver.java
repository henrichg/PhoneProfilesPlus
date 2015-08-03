package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    public static final String BROADCAST_RECEIVER_TYPE = "phoneCall";

    public static final int CALL_EVENT_UNDEFINED = 0;
    public static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    public static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    public static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    public static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    public static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    public static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;

    public static final int LINKMODE_NONE = 0;
    public static final int LINKMODE_LINK = 1;
    public static final int LINKMODE_UNLINK = 2;

    public static String EXTRA_EVENT_TYPE = "extra_event_type";
    public static String EXTRA_PHONE_NUMBER = "extra_phone_number";

    protected boolean onStartReceive()
    {
        if (!GlobalData.getApplicationStarted(savedContext))
            return false;

        GlobalData.loadPreferences(savedContext);

        return true;
    }

    protected void onEndReceive()
    {
    }

    private void doCallEvent(int eventType, String phoneNumber, DataWrapper dataWrapper)
    {
        SharedPreferences preferences = savedContext.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(GlobalData.PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(GlobalData.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.commit();

        boolean wait = false;
        if (GlobalData.applicationUnlinkRingerNotificationVolumes) {
            if ((eventType == CALL_EVENT_INCOMING_CALL_RINGING) || (eventType == CALL_EVENT_INCOMING_CALL_ENDED)) {
                /// for linked ringer and notification volume:
                //    notification volume in profile activation is set after ringer volume
                //    therefore reset ringer volume
                Profile profile = dataWrapper.getActivatedProfile();
                if (profile != null) {
                    if (eventType == CALL_EVENT_INCOMING_CALL_ENDED) {
                        try {
                            Thread.sleep(500); // Delay 0.5 seconds for mode changed to MODE_NORMAL
                        } catch (InterruptedException e) {
                        }
                        /*
                        if (audioManager == null )
                            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL);*/
                    }
                    Intent volumeServiceIntent = new Intent(savedContext, ExecuteVolumeProfilePrefsService.class);
                    volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                    int linkUnlink = PhoneCallBroadcastReceiver.LINKMODE_UNLINK;
                    if (eventType == CALL_EVENT_INCOMING_CALL_ENDED)
                        linkUnlink = PhoneCallBroadcastReceiver.LINKMODE_LINK;
                    volumeServiceIntent.putExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, linkUnlink);
                    savedContext.startService(volumeServiceIntent);
                    wait = true;
                }
                ///
            }
        }

        boolean callEventsExists = false;
        if (GlobalData.getGlobalEventsRuning(savedContext))
            callEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_CALL) > 0;

        if (callEventsExists)
        {
            if (wait || (eventType == CALL_EVENT_INCOMING_CALL_ENDED)) {
                try {
                    Thread.sleep(500); // // Delay 0.5 seconds for ExecuteVolumeProfilePrefsService or mode changed to MODE_NORMAL
                } catch (InterruptedException e) {
                }
            }
            // start service
            Intent eventsServiceIntent = new Intent(savedContext, EventsService.class);
            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(savedContext, eventsServiceIntent);
        }

    }

    private void callStarted(boolean incoming, String phoneNumber)
    {
        if (audioManager == null )
            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        speakerphoneSelected = false;

        if (incoming) {
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

            doCallEvent(CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber, dataWrapper);

            dataWrapper.invalidateDataWrapper();
        }
        /*else {
            DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

            GlobalData.setSeparateVolumes(savedContext, 0);
            doCallEvent(CALL_EVENT_OUTGOING_CALL_STARTED, phoneNumber, dataWrapper);

            dataWrapper.invalidateDataWrapper();
        }*/
    }

    private void callAnswered(boolean incoming, String phoneNumber)
    {
        DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);

        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber, dataWrapper);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber, dataWrapper);

        Profile profile = dataWrapper.getActivatedProfile();
        profile = GlobalData.getMappedProfile(profile, savedContext);

        if (profile != null) {

            if (profile._volumeSpeakerPhone != 0) {

                if (audioManager == null)
                    audioManager = (AudioManager) savedContext.getSystemService(Context.AUDIO_SERVICE);

                try {
                    Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
                } catch (InterruptedException e) {
                }

                ///  change mode to MODE_IN_CALL
                audioManager.setMode(AudioManager.MODE_IN_CALL);

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

        dataWrapper.invalidateDataWrapper();
    }

    private void callEnded(boolean incoming, String phoneNumber)
    {
        //Deactivate loudspeaker
        if (audioManager == null )
            audioManager = (AudioManager)savedContext.getSystemService(Context.AUDIO_SERVICE);

        //if (audioManager.isSpeakerphoneOn())
        if (speakerphoneSelected)
        {
            audioManager.setSpeakerphoneOn(savedSpeakerphone);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }

        speakerphoneSelected = false;

        DataWrapper dataWrapper = new DataWrapper(savedContext, false, false, 0);


        if (incoming)
            doCallEvent(CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, dataWrapper);
        else
            doCallEvent(CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, dataWrapper);

        dataWrapper.invalidateDataWrapper();
    }

    protected void onIncomingCallStarted(String number, Date start) 
    {
        callStarted(true, number);
    }

    protected void onOutgoingCallStarted(String number, Date start)
    {
        callStarted(false, number);
    }
    
    protected void onIncomingCallAnswered(String number, Date start)
    {
        callAnswered(true, number);
    }

    protected void onOutgoingCallAnswered(String number, Date start)
    {
        callAnswered(false, number);
    }
    
    protected void onIncomingCallEnded(String number, Date start, Date end)
    {
        callEnded(true, number);
    }

    protected void onOutgoingCallEnded(String number, Date start, Date end)
    {
        callEnded(false, number);
    }

    protected void onMissedCall(String number, Date start)
    {
        callEnded(true, number);
    }

}
