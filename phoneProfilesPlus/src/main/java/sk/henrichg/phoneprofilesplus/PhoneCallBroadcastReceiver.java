package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static AudioManager audioManager = null;

    private static boolean savedSpeakerphone = false;
    private static boolean speakerphoneSelected = false;

    //static boolean linkUnlinkExecuted = false;
    //static boolean speakerphoneOnExecuted = false;

    private static final int SERVICE_PHONE_EVENT_START = 1;
    private static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    private static final int SERVICE_PHONE_EVENT_END = 3;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    static final int LINKMODE_UNLINK = 2;

    protected boolean onStartReceive()
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onStartReceive", "xxx");

        return PPApplication.getApplicationStarted(true);
    }

    protected void onEndReceive()
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onEndReceive", "xxx");
    }

    protected void onIncomingCallStarted(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onIncomingCallStarted", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, false/*, number, eventTime*/);
    }

    //protected void onOutgoingCallStarted(/*String number, Date eventTime*/)
    //{
    //    doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false/*, number, eventTime*/);
    //}

    protected void onIncomingCallAnswered(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onIncomingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, false/*, number, eventTime*/);
    }

    protected void onOutgoingCallAnswered(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onOutgoingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, false/*, number, eventTime*/);
    }

    protected void onIncomingCallEnded(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onIncomingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, false/*, number, eventTime*/);
    }

    protected void onOutgoingCallEnded(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onOutgoingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, false/*, number, eventTime*/);
    }

    protected void onMissedCall(/*String number, Date eventTime*/)
    {
        PPApplication.logE("[IN_LISTENER] PhoneCallBroadcastReceiver.onMissedCall", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, true/*, number, eventTime*/);
    }

    private void doCall(final Context context, final int phoneEvent,
                            final boolean incoming, final boolean missed/*,
                            final String number, final Date eventTime*/) {
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast(/*"PhoneCallBroadcastReceiver.doCall"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneCallBroadcastReceiver.doCall");

                switch (phoneEvent) {
                    case SERVICE_PHONE_EVENT_START:
                        callStarted(incoming, /*number, eventTime,*/ appContext);
                        break;
                    case SERVICE_PHONE_EVENT_ANSWER:
                        callAnswered(incoming, /*number, eventTime,*/ appContext);
                        break;
                    case SERVICE_PHONE_EVENT_END:
                        callEnded(incoming, missed, /*number, eventTime,*/ appContext);
                        break;
                }

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneCallBroadcastReceiver.doCall");
            }
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean setLinkUnlinkNotificationVolume(final int linkMode, final Context context) {
        //PPApplication.logE("PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume", "RingerModeChangeReceiver.notUnlinkVolumes="+RingerModeChangeReceiver.notUnlinkVolumes);
        if (!RingerModeChangeReceiver.notUnlinkVolumes) {
            boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
            //PPApplication.logE("PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume", "unlinkEnabled="+unlinkEnabled);
            if (unlinkEnabled) {
                int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode/*, context*/);
                //PPApplication.logE("PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume", "audibleSystemRingerMode="+audibleSystemRingerMode);
                if (audibleSystemRingerMode) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                    final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
                    //PPApplication.logE("PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume", "profile="+profile);
                    if (profile != null) {
                        //PPApplication.logE("PhoneCallBroadcastReceiver.setLinkUnlinkNotificationVolume", "profile._name="+profile._name);
                        ActivateProfileHelper.executeForVolumes(profile, linkMode, false, context);
                        return true;
                    }
                    //dataWrapper.invalidateDataWrapper();
                }
            }
        }
        return false;
    }

    /*
    private static void setVolumesByProfile(Context context) {
        if (!RingerModeChangeReceiver.notUnlinkVolumes) {
            boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
            //PPApplication.logE("PhoneCallBroadcastReceiver.setVolumesByProfile", "unlinkEnabled="+unlinkEnabled);
            if (!unlinkEnabled) {
                int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode);
                //PPApplication.logE("PhoneCallBroadcastReceiver.setVolumesByProfile", "audibleSystemRingerMode="+audibleSystemRingerMode);
                if (audibleSystemRingerMode) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                    final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
                    //PPApplication.logE("PhoneCallBroadcastReceiver.setVolumesByProfile", "profile="+profile);
                    if (profile != null) {
                        //PPApplication.logE("PhoneCallBroadcastReceiver.setVolumesByProfile", "profile._name="+profile._name);
                        ActivateProfileHelper.executeForVolumes(profile, LINKMODE_NONE, false, context);
                    }
                    //dataWrapper.invalidateDataWrapper();
                }
            }
        }
    }
    */

    private static void callStarted(boolean incoming, /*String phoneNumber, Date eventTime,*/ Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        //PPApplication.logE("PhoneCallBroadcastReceiver.callStarted", "incoming="+incoming);
        //PPApplication.logE("PhoneCallBroadcastReceiver.callStarted", "phoneNumber="+phoneNumber);

        if (incoming) {
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK, context);
        }
    }

    private static void callAnswered(@SuppressWarnings("unused") boolean incoming, /*String phoneNumber, Date eventTime,*/ Context context)
    {
        speakerphoneSelected = false;

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        //PPApplication.logE("PhoneCallBroadcastReceiver.callAnswered", "incoming="+incoming);

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().stopSimulatingRingingCall(true);

        // Delay 2 seconds mode changed to MODE_IN_CALL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_IN_CALL)
                PPApplication.sleep(500);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 5 * 1000);

        // audio mode is set to MODE_IN_CALL by system
        //PPApplication.logE("PhoneCallBroadcastReceiver.callAnswered", "audio mode="+audioManager.getMode());

        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        //profile = Profile.getMappedProfile(profile, context);

        if (profile != null) {
            if (profile._volumeSpeakerPhone != 0) {
                savedSpeakerphone = audioManager.isSpeakerphoneOn();
                //PPApplication.logE("PhoneCallBroadcastReceiver.callAnswered", "savedSpeakerphone="+savedSpeakerphone);
                //PPApplication.logE("PhoneCallBroadcastReceiver.callAnswered", "profile._volumeSpeakerPhone="+profile._volumeSpeakerPhone);
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                //PPApplication.logE("PhoneCallBroadcastReceiver.callAnswered", "changeSpeakerphone="+changeSpeakerphone);
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone
                    // not working in EMUI :-/
                    //audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);
                    speakerphoneSelected = true;
                }
            }
        }

        //dataWrapper.invalidateDataWrapper();

        // setSpeakerphoneOn() moved to ActivateProfileHelper.executeForVolumes
    }

    private static void callEnded(boolean incoming, @SuppressWarnings("unused") boolean missed, /*String phoneNumber, Date eventTime,*/ Context context)
    {
        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneCallBroadcastReceiver.callEnded", "incoming=" + incoming);
            PPApplication.logE("PhoneCallBroadcastReceiver.callEnded", "missed=" + missed);
        }*/

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().stopSimulatingRingingCall(false);

        // audio mode is set to MODE_IN_CALL by system

        if (speakerphoneSelected)
        {
            if (audioManager != null) {
                //audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(savedSpeakerphone);
            }
        }

        speakerphoneSelected = false;

        // Delay 2 seconds mode changed to MODE_NORMAL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_NORMAL)
                PPApplication.sleep(500);
            else
                break;
        } while (SystemClock.uptimeMillis() - start < 5 * 1000);

        // audio mode is set to MODE_NORMAL by system

        if (incoming) {
            /*boolean linkUnlink =*/ setLinkUnlinkNotificationVolume(LINKMODE_LINK, context);

            //if (!linkUnlink) {
            //    setVolumesByProfile(context);
            //}
        }

        DisableInternalChangeWorker.enqueueWork();

        /*PPApplication.startHandlerThreadInternalChangeToFalse();
        final Handler handler = new Handler(PPApplication.handlerThreadInternalChangeToFalse.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PhoneProfilesService.stopSimulatingRingingCall", "disable ringer mode change internal change");
                RingerModeChangeReceiver.internalChange = false;
            }
        }, 3000);*/
        //PostDelayedBroadcastReceiver.setAlarm(
        //        PostDelayedBroadcastReceiver.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE, 3, this);

    }

}
