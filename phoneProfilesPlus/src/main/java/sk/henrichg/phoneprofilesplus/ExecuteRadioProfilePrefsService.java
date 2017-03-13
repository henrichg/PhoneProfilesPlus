package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ExecuteRadioProfilePrefsService extends IntentService 
{

    public ExecuteRadioProfilePrefsService() {
        super("ExecuteRadioProfilePrefsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("ExecuteRadioProfilePrefsService.onHandleIntent","-- START ----------");

        Context context = getApplicationContext();

        PPApplication.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);

        /*
        // synchronization, wait for end of radio state change
        PPApplication.logE("@@@ ActivateProfileHelper.executeForRadios", "start waiting for radio change");
        PPApplication.waitForRadioChangeState(context);
        PPApplication.logE("@@@ ActivateProfileHelper.executeForRadios", "end waiting for radio change");

        PPApplication.setRadioChangeState(context, true);
        */

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "before synchronized block");

        synchronized (PPApplication.radioChangeStateMutex) {

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "in synchronized block - start");

        profile = Profile.getMappedProfile(profile, context);
        if (profile != null) {

            if (Permissions.checkProfileRadioPreferences(context, profile)) {
                // run execute radios from ActivateProfileHelper
                ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                aph.initialize(dataWrapper, context);
                aph.executeForRadios(profile);
            }
        }

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "in synchronized block - end");

        }

        PPApplication.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "after synchronized block");

        //PPApplication.setRadioChangeState(context, false);

        dataWrapper.invalidateDataWrapper();

        PPApplication.sleep(500);

        PPApplication.logE("ExecuteRadioProfilePrefsService.onHandleIntent","-- END ----------");

    }
}
