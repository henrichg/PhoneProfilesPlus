package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
//import me.drakeet.support.toast.ToastCompat;

public class BackgroundActivateProfileActivity extends AppCompatActivity {

    private boolean activityStarted = false;

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("BackgroundActivateProfileActivity.onCreate", "xxx");

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

        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (activityStarted) {
            if ((startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
                    (startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT)) {
                if (profile_id == Profile.RESTART_EVENTS_PROFILE_ID) {
                    if (Event.getGlobalEventsRunning()) {
                        // set theme and language for dialog alert ;-)
                        // not working on Android 2.3.x
                        GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false);
                        //GlobalGUIRoutines.setLanguage(this);

                        dataWrapper.restartEventsWithAlert(this);
                    } else
                        finish();
                } else
                    dataWrapper.activateProfile(profile_id, startupSource, this/*, ""*/);
            }
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    private boolean showNotStartedToast() {
        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = true;
        if (applicationStarted) {
            PhoneProfilesService instance = PhoneProfilesService.getInstance();
            fullyStarted = instance.getApplicationFullyStarted();
            applicationStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);
        }
        if (!applicationStarted) {
            String text = getString(R.string.app_name) + " " + getString(R.string.application_is_not_started);
            if (!fullyStarted)
                text = getString(R.string.app_name) + " " + getString(R.string.application_is_starting_toast);
            GlobalGUIRoutines.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        return false;
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

}
