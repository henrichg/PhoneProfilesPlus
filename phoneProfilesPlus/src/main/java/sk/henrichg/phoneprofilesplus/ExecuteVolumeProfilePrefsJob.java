package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteVolumeProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteVolumeProfilePrefsJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ExecuteVolumeProfilePrefsJob.onRunJob", "ExecuteVolumeProfilePrefsJob_onRunJob");

        ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, false);

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
        final ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
        aph.initialize(dataWrapper, appContext);

        // link, unlink volumes during activation of profile
        // required for phone call events
        ApplicationPreferences.getSharedPreferences(appContext);
        int callEventType = ApplicationPreferences.preferences.getInt(PhoneCallJob.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallJob.CALL_EVENT_UNDEFINED);
        int linkUnlink = PhoneCallJob.LINKMODE_NONE;
        if (ActivateProfileHelper.getMergedRingNotificationVolumes(appContext) && ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(appContext)) {
            if ((callEventType == PhoneCallJob.CALL_EVENT_INCOMING_CALL_RINGING) ||
                    (callEventType == PhoneCallJob.CALL_EVENT_INCOMING_CALL_ENDED)) {
                linkUnlink = PhoneCallJob.LINKMODE_UNLINK;
                if (callEventType == PhoneCallJob.CALL_EVENT_INCOMING_CALL_ENDED)
                    linkUnlink = PhoneCallJob.LINKMODE_LINK;
            }
        }

        if (linkUnlink != PhoneCallJob.LINKMODE_NONE)
            // link, unlink is executed, not needed do it from EventsHandler
            PhoneCallJob.linkUnlinkExecuted = true;

        Bundle bundle = params.getTransientExtras();
        
        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = bundle.getBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);
        profile = Profile.getMappedProfile(profile, appContext);

        boolean forProfileActivation = bundle.getBoolean(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, false);

        if (profile != null)
            PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "profile.name="+profile._name);
        else
            PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "profile=null");

        if ((callEventType == PhoneCallJob.CALL_EVENT_INCOMING_CALL_ANSWERED) ||
                (callEventType == PhoneCallJob.CALL_EVENT_OUTGOING_CALL_ANSWERED)) {
            PhoneCallJob.setSpeakerphoneOn(profile, appContext);
            PhoneCallJob.speakerphoneOnExecuted = true;
        }

        if (profile != null)
        {
            aph.setTones(profile);

            if (/*Permissions.checkProfileVolumePreferences(context, profile) &&*/
                    Permissions.checkProfileAccessNotificationPolicy(appContext, profile)) {

                aph.changeRingerModeForVolumeEqual0(profile);
                aph.changeNotificationVolumeForVolumeEqual0(profile);

                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                aph.setRingerMode(profile, audioManager, true, /*linkUnlink,*/ forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "internalChange="+RingerModeChangeReceiver.internalChange);
                aph.setVolumes(profile, audioManager, linkUnlink, forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "internalChange="+RingerModeChangeReceiver.internalChange);
                aph.setRingerMode(profile, audioManager, false, /*linkUnlink,*/ forProfileActivation);
                PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "internalChange="+RingerModeChangeReceiver.internalChange);

                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("ExecuteVolumeProfilePrefsJob.onRunJob", "disable ringer mode change internal change");
                        RingerModeChangeReceiver.internalChange = false;
                    }
                }, 3000);

            }

            aph.setTones(profile);
        }

        dataWrapper.invalidateDataWrapper();
        
        return Result.SUCCESS;
    }

    static void start(Context context, long profile_id, boolean mergedProfile, boolean forProfileActivation) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        bundle.putBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, mergedProfile);
        bundle.putBoolean(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, forProfileActivation);

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
    
}
