package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BackgroundActivateProfileActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] BackgroundActivateProfileActivity.onCreate", "xxx");

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
        profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);

        if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {

            dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (activityStarted) {
            if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
                    (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {
                if (profile_id == Profile.RESTART_EVENTS_PROFILE_ID) {
                    //if (Event.getGlobalEventsRunning()) {
                        // set theme and language for dialog alert ;-)
                        GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false);
                        //GlobalGUIRoutines.setLanguage(this);

                        dataWrapper.restartEventsWithAlert(this);
                    //} else
                    //    finish();
                } else {
                    PPApplication.showToastForProfileActivation = true;
                    dataWrapper.activateProfile(profile_id, startupSource, this, true);
                }
            }
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
//        PPApplication.logE("[APP_START] BackgroundActivateProfileActivity.showNotStartedToast", "setApplicationFullyStarted");
        PPApplication.setApplicationFullyStarted(getApplicationContext());
        return false;
/*        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted;
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        if (!fullyStarted) {
            if ((PPApplication.startTimeOfApplicationStart > 0) &&
                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_cannot_be_started);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            else {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            return true;
        }
        return false;*/
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false, mergedProfile);
                    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private boolean startPPServiceWhenNotStarted() {
        // this is for list widget header
        boolean serviceStarted = PhoneProfilesService.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("EditorProfilesActivity.onStart", "application is not started");
                PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
            }*/
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplication.logE("[START_PP_SERVICE] ActivateProfileActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplication.startPPService(this, serviceIntent);
            return true;
        } else {
            //noinspection RedundantIfStatement
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EditorProfilesActivity.onStart", "application is started");
                    PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
                }*/
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;

                /*
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.logE("[START_PP_SERVICE] ActivateProfileActivity.startPPServiceWhenNotStarted", "(2)");
                PPApplication.startPPService(this, serviceIntent);
                */

                return true;
            }
            //else {
            //    PPApplication.logE("EditorProfilesActivity.onStart", "application and service is started");
            //}
        }

        return false;
    }

}
