package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteRootProfilePrefsService extends IntentService {

    public static final String ACTION_ADAPTIVE_BRIGHTNESS = "action_adaptive_brightness";
    public static final String ACTION_LOCK_DEVICE = "action_lock_device";
    public static final String ACTION_POWER_SAVE_MODE = "action_power_save_mode";

    public ExecuteRootProfilePrefsService() {
        super("ExecuteRootProfilePrefsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PPApplication.logE("$$$ ExecuteRootProfilePrefsService.onHandleIntent", "-- START ----------");

        if (intent == null) return;

        Context context = getApplicationContext();

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);

        // run execute radios from ActivateProfileHelper
        profile = Profile.getMappedProfile(profile, context);
        if (profile != null)
        {
            ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
            aph.initialize(dataWrapper, context);

            if (intent.getAction().equals(ACTION_ADAPTIVE_BRIGHTNESS))
                aph.executeRootForAdaptiveBrightness(profile);
            else
            if (intent.getAction().equals(ACTION_LOCK_DEVICE))
                aph.lockDevice(profile);
            else
            if (intent.getAction().equals(ACTION_POWER_SAVE_MODE))
                aph.setPowerSaveMode(profile);
        }

        dataWrapper.invalidateDataWrapper();

        PPApplication.logE("$$$ ExecuteRootProfilePrefsService.onHandleIntent","-- END ----------");
    }

}
