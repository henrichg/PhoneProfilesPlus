package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class ActivateProfileFromExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private String profileName;
    private long profile_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        profileName = intent.getStringExtra(GlobalData.EXTRA_PROFILE_NAME);

        if (!profileName.isEmpty()) {
            GlobalData.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());

            List<Profile> profileList = dataWrapper.getProfileList();
            for (Profile profile : profileList) {
                if (profile._name.equals(profileName)) {
                    profile_id = profile._id;
                    break;
                }
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id, false);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                    true, false, 0, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true, false)) {
                dataWrapper._activateProfile(profile, false, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true);
            }
        }
        else
            dataWrapper.finishActivity(GlobalData.STARTUP_SOURCE_EXTERNAL_APP, false, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
