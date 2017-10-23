package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteRootProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteRootProfilePrefsJob";

    private static final String EXTRA_ACTION = "action";
    static final String ACTION_ADAPTIVE_BRIGHTNESS = "action_adaptive_brightness";
    static final String ACTION_LOCK_DEVICE = "action_lock_device";
    static final String ACTION_POWER_SAVE_MODE = "action_power_save_mode";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "ExecuteRootProfilePrefsJob.onRunJob", "ExecuteRootProfilePrefsJob_onRunJob");

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

        Bundle bundle = params.getTransientExtras();

        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = bundle.getBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);

        // run execute radios from ActivateProfileHelper
        profile = Profile.getMappedProfile(profile, appContext);
        if (profile != null)
        {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, appContext);

            String action = bundle.getString(EXTRA_ACTION, "");
            if (action.equals(ACTION_ADAPTIVE_BRIGHTNESS))
                aph.executeRootForAdaptiveBrightness(profile);
            else
            if (action.equals(ACTION_LOCK_DEVICE))
                aph.lockDevice(profile);
            else
            if (action.equals(ACTION_POWER_SAVE_MODE))
                aph.setPowerSaveMode(profile);
        }

        dataWrapper.invalidateDataWrapper();

        return Result.SUCCESS;
    }

    static void start(Context context, String action, long profile_id, boolean mergedProfile) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, action);
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        bundle.putBoolean(ActivateProfileHelper.EXTRA_MERGED_PROFILE, mergedProfile);

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
