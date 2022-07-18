package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
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

    PhoneCallsListener(Context context, int simSlot) {
        this.savedContext = context.getApplicationContext();
        this.simSlot = simSlot;
    }

    public void onCallStateChanged (int state, String phoneNumber) {

        if (PPApplication.getApplicationStarted(true)) {
            if(lastState == state){
                //No change, de-bounce extras
                return;
            }

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
//                    PPApplication.logE("PhoneCallsListener.onCallStateChanged", "state=CALL_STATE_RINGING");
                    //PPPEApplication.logE("PhoneCallsListener.PhoneCallStartEndDetector", "incomingNumber="+incomingNumber);
                    inCall = false;
                    isIncoming = true;
                    onIncomingCallStarted(/*incomingNumber, eventTime*/);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    PPApplication.logE("PhoneCallsListener.onCallStateChanged", "state=CALL_STATE_OFFHOOK");
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
//                    PPApplication.logE("PhoneCallsListener.onCallStateChanged", "state=CALL_STATE_IDLE");
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

    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

//        PPApplication.logE("PhoneCallsListener.onServiceStateChanged", "state="+serviceState.getState());

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
            Log.e("PhoneCallsListener.onServiceStateChanged", "is in roaming - telephony manager - network");
        } else {
            // Not in Roaming
            Log.e("PhoneCallsListener.onServiceStateChanged", "is NOT in roaming - telephony manager - network");
        }
        if (serviceState.getDataRoaming()) {
            Log.e("PhoneCallsListener.onServiceStateChanged", "is in roaming - service state - data");
        } else {
            Log.e("PhoneCallsListener.onServiceStateChanged", "is NOT in roaming - service state - data");
        }
        */

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            Object ret = HiddenApiBypass.invoke(ServiceState.class, serviceState, "getVoiceRoaming");
            Log.e("PhoneCallsListener.onServiceStateChanged", "ret="+ret);
        }*/
        // You can also check roaming state using this
        if (serviceState.getRoaming()) {
            // In Roaming
//            Log.e("PhoneCallsListener.onServiceStateChanged", "is in roaming - service state - network");
            networkRoaming = true;
        } else {
            // Not in Roaming
//            Log.e("PhoneCallsListener.onServiceStateChanged", "is NOT in roaming - service state - network");
            networkRoaming = false;
        }
        if (serviceState.getDataRoaming()) {
//            Log.e("PhoneCallsListener.onServiceStateChanged", "is in roaming - service state - data");
            dataRoaming = true;
        } else {
//            Log.e("PhoneCallsListener.onServiceStateChanged", "is NOT in roaming - service state - data");
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
            if (Event.getGlobalEventsRunning()) {
                //if (useHandler) {
                final Context appContext = savedContext.getApplicationContext();
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_ROAMING, "SENSOR_TYPE_ROAMING", 0);
                /*
                PPApplication.startHandlerThreadBroadcast();
                final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                //        context.getApplicationContext()) {
                __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneCallListener.onServiceStateChanged");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneCallListener_onServiceStateChanged");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] PhoneCallListener.onServiceStateChanged", "sensorType=SENSOR_TYPE_ROAMING");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ROAMING);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PhoneCallListener.onServiceStateChanged");
                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    //}
                });
                */
            }
        }

    }

    protected void onIncomingCallStarted(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallStarted", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, false/*, number, eventTime*/);
    }

    //protected void onOutgoingCallStarted(/*String number, Date eventTime*/)
    //{
    //    doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false/*, number, eventTime*/);
    //}

    protected void onIncomingCallAnswered(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, false/*, number, eventTime*/);
    }

    protected void onOutgoingCallAnswered(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onOutgoingCallAnswered", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, false/*, number, eventTime*/);
    }

    protected void onIncomingCallEnded(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onIncomingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, false/*, number, eventTime*/);
    }

    protected void onOutgoingCallEnded(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onOutgoingCallEnded", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, false/*, number, eventTime*/);
    }

    protected void onMissedCall(/*String number, Date eventTime*/)
    {
//        PPApplication.logE("[IN_LISTENER] PhoneCallsListener.onMissedCall", "xxx");
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, true/*, number, eventTime*/);
    }

    private void doCall(Context context, final int phoneEvent,
                        final boolean incoming, final boolean missed/*,
                            final String number, final Date eventTime*/) {
        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast(/*"PhoneCallsListener.doCall"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneCallsListener.doCall");

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
                        callAnswered(incoming, /*number, eventTime,*/ appContext);
                        break;
                    case SERVICE_PHONE_EVENT_END:
                        callEnded(incoming, missed, /*number, eventTime,*/ appContext);
                        break;
                }

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneCallsListener.doCall");
            //}
        }; //);
        PPApplication.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean setLinkUnlinkNotificationVolume(final int linkMode, final Context context) {
        synchronized (PPApplication.notUnlinkVolumesMutex) {
//            PPApplication.logE("PhoneCallsListener.setLinkUnlinkNotificationVolume", "RingerModeChangeReceiver.notUnlinkVolumes=" + RingerModeChangeReceiver.notUnlinkVolumes);
            if (!RingerModeChangeReceiver.notUnlinkVolumes) {
                boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
//                PPApplication.logE("PhoneCallsListener.setLinkUnlinkNotificationVolume", "unlinkEnabled=" + unlinkEnabled);
                if (unlinkEnabled) {
                    int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                    boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode/*, context*/);
//                    PPApplication.logE("PhoneCallsListener.setLinkUnlinkNotificationVolume", "audibleSystemRingerMode=" + audibleSystemRingerMode);
                    if (audibleSystemRingerMode) {
                        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                        final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
//                        PPApplication.logE("PhoneCallsListener.setLinkUnlinkNotificationVolume", "profile=" + profile);
                        if (profile != null) {
//                            PPApplication.logE("PhoneCallsListener.setLinkUnlinkNotificationVolume", "profile._name=" + profile._name);
                            SharedPreferences sharedPreferences = context.getSharedPreferences("temp_phoneCallBroadcastReceiver", Context.MODE_PRIVATE);
                            profile.saveProfileToSharedPreferences(sharedPreferences);
                            ActivateProfileHelper.executeForVolumes(profile, linkMode, false, context, sharedPreferences);
                            return true;
                        }
                        //dataWrapper.invalidateDataWrapper();
                    }
                }
            }
            return false;
        }
    }

    /*
    private static void setVolumesByProfile(Context context) {
        if (!RingerModeChangeReceiver.notUnlinkVolumes) {
            boolean unlinkEnabled = ActivateProfileHelper.getMergedRingNotificationVolumes() && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes;
            //PPApplication.logE("PhoneCallsListener.setVolumesByProfile", "unlinkEnabled="+unlinkEnabled);
            if (!unlinkEnabled) {
                int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
                boolean audibleSystemRingerMode = ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode);
                //PPApplication.logE("PhoneCallsListener.setVolumesByProfile", "audibleSystemRingerMode="+audibleSystemRingerMode);
                if (audibleSystemRingerMode) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
                    final Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
                    //PPApplication.logE("PhoneCallsListener.setVolumesByProfile", "profile="+profile);
                    if (profile != null) {
                        //PPApplication.logE("PhoneCallsListener.setVolumesByProfile", "profile._name="+profile._name);
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

//        PPApplication.logE("PhoneCallsListener.callStarted", "incoming="+incoming);
        //PPApplication.logE("PhoneCallsListener.callStarted", "phoneNumber="+phoneNumber);

        speakerphoneSelected = false;

        /*
        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        //profile = Profile.getMappedProfile(profile, context);

        if (profile != null) {
            if (profile._volumeSpeakerPhone != 0) {
                savedSpeakerphone = false; //audioManager.isSpeakerphoneOn();
                PPApplication.logE("PhoneCallsListener.callStarted", "savedSpeakerphone="+savedSpeakerphone);
                PPApplication.logE("PhoneCallsListener.callStarted", "profile._volumeSpeakerPhone="+profile._volumeSpeakerPhone);
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
                PPApplication.logE("PhoneCallsListener.callStarted", "changeSpeakerphone="+changeSpeakerphone);
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone

                    // not working in EMUI :-/
                    audioManager.setMode(AudioManager.MODE_IN_CALL);

                    // Delay 2 seconds mode changed to MODE_IN_CALL
                    long start = SystemClock.uptimeMillis();
                    do {
                        if (audioManager.getMode() != AudioManager.MODE_IN_CALL) {
                            //if (audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                            PPApplication.logE("PhoneCallsListener.callStarted", "xxx - audio mode MODE_IN_CALL="+(audioManager.getMode() == AudioManager.MODE_IN_CALL));
                            //PPApplication.logE("PhoneCallsListener.callStarted", "xxx - audio mode MODE_IN_COMMUNICATION="+(audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));
                            PPApplication.sleep(500);
                        }
                        else
                            break;
                        PPApplication.logE("PhoneCallsListener.callStarted", "SystemClock.uptimeMillis() - start="+(SystemClock.uptimeMillis() - start));
                    } while (SystemClock.uptimeMillis() - start < (5 * 1000));
                    PPApplication.logE("PhoneCallsListener.callStarted", "yyy - audio mode MODE_IN_CALL="+(audioManager.getMode() == AudioManager.MODE_IN_CALL));
                    //PPApplication.logE("PhoneCallsListener.callStarted", "yyy - audio mode MODE_IN_COMMUNICATION="+(audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));

                    PPApplication.sleep(500);
                    audioManager.setSpeakerphoneOn(profile._volumeSpeakerPhone == 1);
                    speakerphoneSelected = true;
                    PPApplication.logE("PhoneCallsListener.callStarted", "ACTIVATED SPEAKERPHONE");
                }
            }
        }
        */

        if (incoming) {
            setLinkUnlinkNotificationVolume(LINKMODE_UNLINK, context);
        }
    }

    private static void callAnswered(@SuppressWarnings("unused") boolean incoming, /*String phoneNumber, Date eventTime,*/ Context context)
    {
        speakerphoneSelected = false;

        if (audioManager == null )
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

//        PPApplication.logE("PhoneCallsListener.callAnswered", "incoming="+incoming);

//            PPApplication.logE("PhoneCallsListener.callAnswered", "call of stopSimulatingRingingCall");
            PhoneProfilesService.stopSimulatingRingingCall(true, context.getApplicationContext());

        // Delay 2 seconds mode changed to MODE_IN_CALL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_IN_CALL) {
                //if (audioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
//                PPApplication.logE("PhoneCallsListener.callAnswered", "xxx - audio mode MODE_IN_CALL="+(audioManager.getMode() == AudioManager.MODE_IN_CALL));
                //PPApplication.logE("PhoneCallsListener.callAnswered", "xxx - audio mode MODE_IN_COMMUNICATION="+(audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));
                PPApplication.sleep(200);
            }
            else
                break;
//            PPApplication.logE("PhoneCallsListener.callAnswered", "SystemClock.uptimeMillis() - start="+(SystemClock.uptimeMillis() - start));
        } while (SystemClock.uptimeMillis() - start < (5 * 1000));
//        PPApplication.logE("PhoneCallsListener.callAnswered", "yyy - audio mode MODE_IN_CALL="+(audioManager.getMode() == AudioManager.MODE_IN_CALL));
        //PPApplication.logE("PhoneCallsListener.callAnswered", "yyy - audio mode MODE_IN_COMMUNICATION="+(audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));

        // audio mode is set to MODE_IN_CALL by system
//        PPApplication.logE("PhoneCallsListener.callAnswered", "audio mode="+audioManager.getMode());

        //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        //profile = Profile.getMappedProfile(profile, context);

        if (profile != null) {
            if (profile._volumeSpeakerPhone != 0) {
                savedSpeakerphone = audioManager.isSpeakerphoneOn();
//                PPApplication.logE("PhoneCallsListener.callAnswered", "savedSpeakerphone="+savedSpeakerphone);
//                PPApplication.logE("PhoneCallsListener.callAnswered", "profile._volumeSpeakerPhone="+profile._volumeSpeakerPhone);
                boolean changeSpeakerphone = false;
                if (savedSpeakerphone && (profile._volumeSpeakerPhone == 2)) // 2=speakerphone off
                    changeSpeakerphone = true;
                if ((!savedSpeakerphone) && (profile._volumeSpeakerPhone == 1)) // 1=speakerphone on
                    changeSpeakerphone = true;
//                PPApplication.logE("PhoneCallsListener.callAnswered", "changeSpeakerphone="+changeSpeakerphone);
                if (changeSpeakerphone) {
                    /// activate SpeakerPhone
                    // not working in EMUI :-/
                    //audioManager.setMode(AudioManager.MODE_IN_CALL);
//                    PPApplication.logE("PhoneCallsListener.callAnswered", "audio mode MODE_IN_CALL="+(audioManager.getMode() == AudioManager.MODE_IN_CALL));
                    //PPApplication.logE("PhoneCallsListener.callAnswered", "audio mode MODE_IN_COMMUNICATION="+(audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));
                    PPApplication.sleep(500);

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
//                        setForceUse.invoke(null, 0, 1);
//                    } catch (Exception e) {
//                        PPApplication.recordException(e);
//                    }

                    speakerphoneSelected = true;
//                    PPApplication.logE("PhoneCallsListener.callAnswered", "ACTIVATED SPEAKERPHONE");
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

//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("PhoneCallsListener.callEnded", "incoming=" + incoming);
//            PPApplication.logE("PhoneCallsListener.callEnded", "missed=" + missed);
////            PPApplication.logE("PhoneCallsListener.callEnded", "speakerphoneSelected=" + speakerphoneSelected);
////            PPApplication.logE("PhoneCallsListener.callEnded", "savedSpeakerphone=" + savedSpeakerphone);
//        }

//            PPApplication.logE("PhoneCallsListener.callEnded", "call of stopSimulatingRingingCall");
            PhoneProfilesService.stopSimulatingRingingCall(false, context.getApplicationContext());

        // audio mode is set to MODE_IN_CALL by system

        if (speakerphoneSelected)
        {
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
//                    PPApplication.recordException(e);
//                }
            }
        }

        speakerphoneSelected = false;

        // Delay 2 seconds mode changed to MODE_NORMAL
        long start = SystemClock.uptimeMillis();
        do {
            if (audioManager.getMode() != AudioManager.MODE_NORMAL)
                PPApplication.sleep(200);
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

        PPExecutors.scheduleDisableInternalChangeExecutor();
        PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

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
