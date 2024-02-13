package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

public class PhoneCallsListener extends PhoneStateListener {

    final Context savedContext;

    int lastState = TelephonyManager.CALL_STATE_IDLE;
    boolean inCall;
    boolean isIncoming;

    boolean networkRoaming;
    boolean dataRoaming;

    final int simSlot;

    private static volatile AudioManager audioManager = null;

    private static volatile boolean savedSpeakerphone = false;
    private static volatile boolean speakerphoneSelected = false;

    //static boolean linkUnlinkExecuted = false;
    //static boolean speakerphoneOnExecuted = false;

    private static final int SERVICE_PHONE_EVENT_START = 1;
    private static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    private static final int SERVICE_PHONE_EVENT_END = 3;

    static final int LINKMODE_NONE = 0;
    static final int LINKMODE_LINK = 1;
    static final int LINKMODE_UNLINK = 2;

    PhoneCallsListener(Context context, int simSlot) {
        this.savedContext = context.getApplicationContext();
        this.simSlot = simSlot;
    }

    @SuppressWarnings("deprecation")
    public void onCallStateChanged (int state, String phoneNumber) {

        if (PPApplicationStatic.getApplicationStarted(true, true)) {
            if(lastState == state){
                //No change, de-bounce extras
                return;
            }

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    inCall = false;
                    isIncoming = true;
                    onIncomingCallStarted(/*incomingNumber, eventTime*/);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->off hook are pickups of incoming calls.  Nothing down on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        inCall = true;
                        isIncoming = false;
                        onOutgoingCallAnswered(/*savedNumber, eventTime*/);
                    }
                    else
                    {
                        inCall = true;
                        isIncoming = true;
                        onIncomingCallAnswered(/*savedNumber, eventTime*/);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(!inCall){
                        //Ring but no pickup-  a miss
                        onMissedCall(/*savedNumber, eventTime*/);
                    }
                    else
                    {
                        if(isIncoming){
                            onIncomingCallEnded(/*savedNumber, eventTime*/);
                        }
                        else{
                            onOutgoingCallEnded(/*savedNumber, eventTime*/);
                        }
                        inCall = false;
                    }
                    break;
            }
            lastState = state;
        }
    }

    @SuppressWarnings("deprecation")
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

        /*
        TelephonyManager telephonyManager;
        if (simSlot == 1)
            telephonyManager = PPApplication.telephonyManagerSIM1;
        else
        if (simSlot == 2)
            telephonyManager = PPApplication.telephonyManagerSIM2;
        else
            telephonyManager = PPApplication.telephonyManagerDefault;

        if (telephonyManager.isNetworkRoaming()) {
            // In Roaming
        } else {
            // Not in Roaming
        }
        if (serviceState.getDataRoaming()) {
        } else {
        }
        */

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            Object ret = HiddenApiBypass.invoke(ServiceState.class, serviceState, "getVoiceRoaming");
            Log.e("PhoneCallsListener.onServiceStateChanged", "ret="+ret);
        }*/
        // You can also check roaming state using this
        if (serviceState.getRoaming()) {
            // In Roaming
            networkRoaming = true;
        } else {
            // Not in Roaming
            networkRoaming = false;
        }
        if (serviceState.getDataRoaming()) {
            dataRoaming = true;
        } else {
            dataRoaming = false;
        }

        /*
        int dataRoaming = Settings.Global.getInt(savedContext.getContentResolver(), Settings.Global.DATA_ROAMING, 0);
        Log.e("PhoneCallsListener.onServiceStateChanged", "dataRoaming="+dataRoaming);
        if (dataRoaming == 1) {
            Log.e("PhoneCallsListener.onServiceStateChanged", "is in roaming - settings global - data");
        } else {
            Log.e("PhoneCallsListener.onServiceStateChanged", "is NOT in roaming - settings global - data");
        }
        */

        EventPreferencesRoaming.getEventRoamingInSIMSlot(savedContext, simSlot);
        boolean oldNetworkRoaming = false;
        boolean oldDataRoaming = false;
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneCallsListener.onServiceStateChanged", "(1) PPApplication.eventRoamingSensorMutex");
        synchronized (PPApplication.eventRoamingSensorMutex) {
            switch (simSlot) {
                case 0:
                    oldNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot0;
                    oldDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot0;
                    break;
                case 1:
                    oldNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot1;
                    oldDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot1;
                    break;
                case 2:
                    oldNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot2;
                    oldDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot2;
                    break;
            }
        }

        EventPreferencesRoaming.setEventRoamingInSIMSlot(savedContext, simSlot, networkRoaming, dataRoaming);
        boolean newNetworkRoaming = false;
        boolean newDataRoaming = false;
        EventPreferencesRoaming.getEventRoamingInSIMSlot(savedContext, simSlot);
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneCallsListener.onServiceStateChanged", "(2) PPApplication.eventRoamingSensorMutex");
        synchronized (PPApplication.eventRoamingSensorMutex) {
            switch (simSlot) {
                case 0:
                    newNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot0;
                    newDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot0;
                    break;
                case 1:
                    newNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot1;
                    newDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot1;
                    break;
                case 2:
                    newNetworkRoaming = ApplicationPreferences.prefEventRoamingNetworkInSIMSlot2;
                    newDataRoaming = ApplicationPreferences.prefEventRoamingDataInSIMSlot2;
                    break;
            }
        }

        if ((newNetworkRoaming != oldNetworkRoaming) || (newDataRoaming != oldDataRoaming)) {
            if (EventStatic.getGlobalEventsRunning(savedContext)) {
                final Context appContext = savedContext.getApplicationContext();
                PPExecutors.handleEvents(appContext,
                        new int[]{EventsHandler.SENSOR_TYPE_ROAMING},
                        PPExecutors.SENSOR_NAME_SENSOR_TYPE_ROAMING, 0);
            }
        }

    }

    protected void onIncomingCallStarted(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallStarted", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true/*, false, number, eventTime*/);
    }

    //protected void onOutgoingCallStarted(/*String number, Date eventTime*/)
    //{
    //    doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false/*, number, eventTime*/);
    //}

    protected void onIncomingCallAnswered(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true/*, false, number, eventTime*/);
    }

    protected void onOutgoingCallAnswered(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onOutgoingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false/*, false, number, eventTime*/);
    }

    protected void onIncomingCallEnded(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true/*, false, number, eventTime*/);
    }

    protected void onOutgoingCallEnded(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onOutgoingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false/*, false, number, eventTime*/);
    }

    protected void onMissedCall(/*String number, Date eventTime*/)
    {
//        PPApplicationStatic.logE("[IN_LISTENER] PhoneCallsListener.onMissedCall", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true/*, true, number, eventTime*/);
    }

    private void doCall(Context context, final int phoneEvent,
                        final boolean incoming/*, final boolean missed,
                            final String number, final Date eventTime*/) {
        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneCallsListener.doCall");

            //Context appContext= appContextWeakRef.get();

            //if (appContext != null) {
//            int simSlot = 0;
//            if (subscriptionInfo != null)
//                simSlot = subscriptionInfo.getSimSlotIndex()+1;

                switch (phoneEvent) {
                    case SERVICE_PHONE_EVENT_START:
                        callStarted(incoming, /*number, eventTime,*/ appContext);
                        break;
                    case SERVICE_PHONE_EVENT_ANSWER:
                        callAnswered(/*incoming,*/ /*number, eventTime,*/ appContext);
                        break;
                    case SERVICE_PHONE_EVENT_END:
                        callEnded(incoming, /*missed,*/ /*number, eventTime,*/ appContext);
                        break;
                }

            //}
        };
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    private static void /*boolean*/ setLinkUnlinkNotificationVolume(final int linkMode, final Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneCallsListener.setLinkUnlinkNotificationVolume", "PPApplication.notUnlinkVolumesMutex");
        synchronized (PPApplication.notUnlinkVolumesMutex) {
            if (!PPApplication.ringerModeNotUnlinkVolumes) {
                boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
                if (unlinkEnabled) {
                    int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                    boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode/*, context*/);
                    if (audibleSystemRingerMode) {
                        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                        final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
                        if (profile != null) {
                            SharedPreferences sharedPreferences = context.getSharedPreferences(PPApplication.TMP_SHARED_PREFS_PHONE_CALL_BROADCAST_RECEIVER, Context.MODE_PRIVATE);
                            profile.saveProfileToSharedPreferences(sharedPreferences);
                            ActivateProfileHelper.executeForVolumes(profile, linkMode, false, context, sharedPreferences);
                            //return true;
                        }
                        //dataWrapper.invalidateDataWrapper();
                    }
                }
            }
            //return false;
        }
    }

    /*
    private static void setVolumesByProfile(Context context) {
        if (!RingerModeChangeReceiver.notUnlinkVolumes) {
            boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
            if (!unlinkEnabled) {
                int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode);
                if (audibleSystemRingerMode) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                    final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
                    if (profile != null) {
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

        speakerphoneSelected = false;

        /*
        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        //profile = Profile.getMappedProfile(profile, context);

        if (profile != null) {
            if (profile._volumeSpeakerPhone != 0) {
                savedSpeakerphone = false; //audioManager.isSpeakerphoneOn();
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone

                    // not working in EMUI :-/
                    audioManager.setMode(AudioManager.MODE_IN_CALL);

                    // Delay 2 seconds mode changed to MODE_IN_CALL
                    long start = SystemClock.uptimeMillis();
                    do {
                        if (audioManager.getMode() != AudioManager.MODE_IN_CALL) {
                            //if (audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                            PPApplication.sleep(500);
                        }
                        else
                            break;
                    } while (SystemClock.uptimeMillis() - start < (5 * 1000));

                    PPApplication.sleep(500);
                    audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);
                    speakerphoneSelected = true;
                }
            }
        }
        */

        if (incoming) {
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK, context);
        }
    }

    private static void callAnswered(/*boolean incoming,*/
            /*String phoneNumber, Date eventTime,*/ Context context)
    {
        speakerphoneSelected = false;

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        PlayRingingNotification.stopSimulatingRingingCall(true, context.getApplicationContext());

        // Delay 2 seconds mode changed to MODE_IN_CALL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_IN_CALL) {
                //if (audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                GlobalUtils.sleep(200);
            }
            else
                break;
        } while (SystemClock.uptimeMillis() - start < (5 * 1000));

        // audio mode is set to MODE_IN_CALL by system

        if (Build.VERSION.SDK_INT < 29) {
            //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
            Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
            //profile = Profile.getMappedProfile(profile, context);

            if (profile != null) {
                if (profile._volumeSpeakerPhone != 0) {
                    savedSpeakerphone = audioManager.isSpeakerphoneOn();
                    boolean changeSpeakerphone = false;
                    if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                        changeSpeakerphone = true;
                    if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                        changeSpeakerphone = true;
                    if (changeSpeakerphone) {
                        /// activate SpeakerPhone
                        // not working in EMUI :-/

                        // set it to MODE_IN_CALL, becaise simulatin ronging call sets it to MODE_NORMAL
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        GlobalUtils.sleep(500);

                        audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);

//                    try {
//                        Class audioSystemClass = Class.forName("android.media.AudioSystem");
//                        Method setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
                        // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
                        // behavior, use FORCE_NONE (0).
                        // usage for setForceUse, must match AudioSystem::force_use
                        // public static final int FOR_COMMUNICATION = 0;
                        // public static final int FOR_MEDIA = 1;
                        // public static final int FOR_RECORD = 2;
                        // public static final int FOR_DOCK = 3;
                        // public static final int FOR_SYSTEM = 4;
                        // speaker on
//                        setForceUse.invoke(null, FOR_COMMUNICATION, FORCE_SPEAKER);
                        // speaker off
//                        setForceUse.invoke(null, FOR_COMMUNICATION, FORCE_NONE);
//                    } catch (Exception e) {
//                        PPApplicationStatic.recordException(e);
//                    }

                        speakerphoneSelected = true;
                    }
                }
            }

            //dataWrapper.invalidateDataWrapper();
        }
    }

    private static void callEnded(boolean incoming,
                                  /*boolean missed,*/
            /*String phoneNumber, Date eventTime,*/ Context context)
    {
        if (audioManager == null)
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        PlayRingingNotification.stopSimulatingRingingCall(false, context.getApplicationContext());

        // audio mode is set to MODE_IN_CALL by system

        if (Build.VERSION.SDK_INT < 29) {
            if (speakerphoneSelected) {
                if (audioManager != null) {
                    //audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(savedSpeakerphone);

//                try {
//                    Class audioSystemClass = Class.forName("android.media.AudioSystem");
//                    Method setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
                    // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
                    // behavior, use FORCE_NONE (0).
                    // usage for setForceUse, must match AudioSystem::force_use
                    // public static final int FOR_COMMUNICATION = 0;
                    // public static final int FOR_MEDIA = 1;
                    // public static final int FOR_RECORD = 2;
                    // public static final int FOR_DOCK = 3;
                    // public static final int FOR_SYSTEM = 4;
//                    setForceUse.invoke(null, 0, 0);
//                } catch (Exception e) {
//                    PPApplicationStatic.recordException(e);
//                }
                }
            }
        }

        speakerphoneSelected = false;

        // Delay 2 seconds mode changed to MODE_NORMAL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_NORMAL)
                GlobalUtils.sleep(200);
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

        PPExecutors.scheduleDisableRingerModeInternalChangeExecutor();
        PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
    }

}
