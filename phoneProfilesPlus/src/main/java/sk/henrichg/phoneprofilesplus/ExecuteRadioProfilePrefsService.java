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

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("ExecuteRadioProfilePrefsService.onHandleIntent","-- START ----------");

        Context context = getApplicationContext();

        GlobalData.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

        long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(GlobalData.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);

        /*
        // synchronization, wait for end of radio state change
        GlobalData.logE("@@@ ActivateProfileHelper.executeForRadios", "start waiting for radio change");
        GlobalData.waitForRadioChangeState(context);
        GlobalData.logE("@@@ ActivateProfileHelper.executeForRadios", "end waiting for radio change");

        GlobalData.setRadioChangeState(context, true);
        */

        GlobalData.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "before synchronized block");

        synchronized (GlobalData.radioChangeStateMutex) {

        GlobalData.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "in synchronized block - start");

        profile = GlobalData.getMappedProfile(profile, context);
        if (profile != null) {

            if (Permissions.checkProfileRadioPreferences(context, profile)) {
                // run execute radios from ActivateProfileHelper
                ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                aph.initialize(dataWrapper, context);
                aph.executeForRadios(profile);
            }
        }

        GlobalData.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "in synchronized block - end");

        }

        GlobalData.logE("$$$ ExecuteRadioProfilePrefsService.onHandleIntent", "after synchronized block");

        //GlobalData.setRadioChangeState(context, false);

        dataWrapper.invalidateDataWrapper();

        GlobalData.sleep(500);

        GlobalData.logE("ExecuteRadioProfilePrefsService.onHandleIntent","-- END ----------");

    }
}
