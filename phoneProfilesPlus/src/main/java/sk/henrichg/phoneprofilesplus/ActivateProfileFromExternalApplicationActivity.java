package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class ActivateProfileFromExternalApplicationActivity extends Activity {

    private DataWrapper dataWrapper;

    private String profileName;
    private long profile_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "xxx");

        Intent intent = getIntent();
        profileName = intent.getStringExtra(GlobalData.EXTRA_PROFILE_NAME);
        profileName.trim();
        Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profileName="+profileName);

        if (!profileName.isEmpty()) {
            GlobalData.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());

            List<Profile> profileList = dataWrapper.getProfileList();
            for (Profile profile : profileList) {
                if (profile._name.trim().equals(profileName)) {
                    profile_id = profile._id;
                    break;
                }
            }
            Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profile_id="+profile_id);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!GlobalData.getApplicationStarted(getApplicationContext()) || (PhoneProfilesService.instance == null))
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));

        if (profile_id != 0) {
            Profile profile = dataWrapper.getProfileById(profile_id, false);
            Log.d("ActivateProfileFromExternalApplicationActivity.onCreate", "profile="+profile);
            if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                    true, false, 0, GlobalData.STARTUP_SOURCE_EXTERNAL_APP, true, this, true, true)) {
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
