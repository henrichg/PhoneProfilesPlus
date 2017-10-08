package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ProfileDurationJob extends Job {

    static final String JOB_TAG  = "ProfileDurationJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ProfileDurationJob.onRunJob", "ProfileDurationJob_onRunJob");

        Bundle bundle = params.getTransientExtras();

        long profileId = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        if (profileId != 0) {
            DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);

            if (dataWrapper.getIsManualProfileActivation()) {
                Profile profile = dataWrapper.getProfileById(profileId, false);
                Profile activatedProfile = dataWrapper.getActivatedProfile();

                if ((profile != null) && (activatedProfile != null) &&
                        (activatedProfile._id == profile._id) &&
                        (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING)) {
                    // alarm is from activated profile

                    long activateProfileId = 0;
                    if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE) {
                        activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(appContext));
                        if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                            activateProfileId = 0;

                        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, null,
                                DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper),
                                profile._icon, 0);
                    }
                    if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE) {
                        activateProfileId = Profile.getActivatedProfileForDuration(appContext);

                        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, null,
                                DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper),
                                profile._icon, 0);
                    }
                    if (profile._afterDurationDo == Profile.AFTERDURATIONDO_RESTARTEVENTS) {
                        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, null,
                                DataWrapper.getProfileNameWithManualIndicator(profile, true, true, false, dataWrapper),
                                profile._icon, 0);

                        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                        PPApplication.logE("ProfileDurationJob.onRunJob", "restart events");
                        dataWrapper.restartEventsWithDelay(3, true, false);
                    } else {
                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                        dataWrapper.activateProfileAfterDuration(activateProfileId);
                    }
                }
            }

            dataWrapper.invalidateDataWrapper();
        }

        return Result.SUCCESS;
    }

    static void start(long profile_id) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

}
