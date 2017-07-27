package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;


public class ProfileDurationService extends WakefulIntentService {

    public ProfileDurationService() {
        super("ProfileDurationService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PPApplication.logE("##### ProfileDurationService.doWakefulWork", "xxx");

            Context context = getApplicationContext();

            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            if (profileId != 0)
            {
                DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

                if (dataWrapper.getIsManualProfileActivation())
                {
                    Profile profile = dataWrapper.getProfileById(profileId, false);
                    Profile activatedProfile = dataWrapper.getActivatedProfile();

                    if ((profile != null) && (activatedProfile != null) &&
                            (activatedProfile._id == profile._id) &&
                            (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
                    {
                        // alarm is from activated profile

                        long activateProfileId = 0;
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE)
                        {
                            activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, null,
                                    dataWrapper.getProfileNameWithManualIndicator(profile, true, true, false),
                                    profile._icon, 0);
                        }
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE)
                        {
                            activateProfileId = Profile.getActivatedProfileForDuration(context);

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, null,
                                    dataWrapper.getProfileNameWithManualIndicator(profile, true, true, false),
                                    profile._icon, 0);
                        }
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_RESTARTEVENTS)
                        {
                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, null,
                                    dataWrapper.getProfileNameWithManualIndicator(profile, true, true, false),
                                    profile._icon, 0);

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                            PPApplication.logE("ProfileDurationService.doWakefulWork", "restart events");
                            dataWrapper.restartEventsWithDelay(3, true, false);
                        }
                        else
                        {
                            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                            dataWrapper.activateProfileAfterDuration(activateProfileId);
                        }
                    }
                }

                dataWrapper.invalidateDataWrapper();

            }
        }
    }

}
