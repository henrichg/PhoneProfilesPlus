package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BackgroundActivateProfileActivity extends Activity {

    private DataWrapper dataWrapper;

    private int startupSource = 0;
    private long profile_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, 0);
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);

        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getApplicationContext());

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        dataWrapper.activateProfile(profile_id, startupSource, this, "");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
