package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteRadioProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteRadioProfilePrefsJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ExecuteRadioProfilePrefsJob.onRunJob", "ExecuteRadioProfilePrefsJob_onRunJob");

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

        Bundle bundle = params.getTransientExtras();
        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = bundle.getBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);

        /*
        // synchronization, wait for end of radio state change
        PPApplication.logE("@@@ ActivateProfileHelper.executeForRadios", "start waiting for radio change");
        PPApplication.waitForRadioChangeState(context);
        PPApplication.logE("@@@ ActivateProfileHelper.executeForRadios", "end waiting for radio change");

        PPApplication.setRadioChangeState(context, true);
        */

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsJob.onRunJob", "before synchronized block");

        synchronized (PPApplication.radioChangeStateMutex) {

            PPApplication.logE("$$$ ExecuteRadioProfilePrefsJob.onRunJob", "in synchronized block - start");

            profile = Profile.getMappedProfile(profile, appContext);
            if (profile != null) {

                if (Permissions.checkProfileRadioPreferences(appContext, profile)) {
                    // run execute radios from ActivateProfileHelper
                    ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                    aph.initialize(dataWrapper, appContext);
                    aph.executeForRadios(profile);
                }
            }

            PPApplication.logE("$$$ ExecuteRadioProfilePrefsJob.onRunJob", "in synchronized block - end");

        }

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsJob.onRunJob", "after synchronized block");

        //PPApplication.setRadioChangeState(context, false);

        dataWrapper.invalidateDataWrapper();

        PPApplication.sleep(500);
        
        return Result.SUCCESS;
    }

    static void start(long profile_id, boolean mergedProfile) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        bundle.putBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, mergedProfile);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .setTransientExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
    }
    
}
