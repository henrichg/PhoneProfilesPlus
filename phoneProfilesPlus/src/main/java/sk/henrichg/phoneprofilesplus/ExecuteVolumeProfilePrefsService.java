package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class ExecuteVolumeProfilePrefsService extends IntentService
{

    public ExecuteVolumeProfilePrefsService() {
        super("ExecuteRadioProfilePrefsService");
    }

    //@Override
    protected void onHandleIntent(Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### ExecuteVolumeProfilePrefsService.onHandleIntent", "xxx");

        final Context context = getApplicationContext();

        PPApplication.loadPreferences(context);

        PPApplication.setMergedRingNotificationVolumes(getApplicationContext(), false);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
        final ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
        aph.initialize(dataWrapper, context);

        // link, unlink volumes during activation of profile
        // required for phone call events
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        int callEventType = preferences.getInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);
        int linkUnlink = PhoneCallService.LINKMODE_NONE;
        if (PPApplication.getMergedRingNotificationVolumes(context) && PPApplication.applicationUnlinkRingerNotificationVolumes) {
            if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING) ||
                (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED)) {
                linkUnlink = PhoneCallService.LINKMODE_UNLINK;
                if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED)
                    linkUnlink = PhoneCallService.LINKMODE_LINK;
            }
        }

        if (linkUnlink != PhoneCallService.LINKMODE_NONE)
            // link, unlink is executed, not needed do it from EventsService
            PhoneCallService.linkUnlinkExecuted = true;

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);
        profile = Profile.getMappedProfile(profile, context);

        boolean forProfileActivation = intent.getBooleanExtra(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, false);

        if (profile != null)
            PPApplication.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "profile.name="+profile._name);
        else
            PPApplication.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "profile=null");

        if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ANSWERED) ||
            (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ANSWERED)) {
            PhoneCallService.setSpeakerphoneOn(profile, context);
            PhoneCallService.speakerphoneOnExecuted = true;
        }

        if (profile != null)
        {
            aph.setTones(profile);

            if (/*Permissions.checkProfileVolumePreferences(context, profile) &&*/
                Permissions.checkProfileAccessNotificationPolicy(context, profile)) {

                aph.changeRingerModeForVolumeEqual0(profile);
                aph.changeNotificationVolumeForVolumeEqual0(profile);

                RingerModeChangeReceiver.removeAlarm(context);
                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                aph.setRingerMode(profile, audioManager, true, /*linkUnlink,*/ forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "internalChange="+RingerModeChangeReceiver.internalChange);
                aph.setVolumes(profile, audioManager, linkUnlink, forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "internalChange="+RingerModeChangeReceiver.internalChange);
                aph.setRingerMode(profile, audioManager, false, /*linkUnlink,*/ forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "internalChange="+RingerModeChangeReceiver.internalChange);

                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                RingerModeChangeReceiver.setAlarmForDisableInternalChange(context);

            }

            aph.setTones(profile);
        }

        dataWrapper.invalidateDataWrapper();
    }


}
