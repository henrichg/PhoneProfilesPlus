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

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, GlobalData.STARTUP_SOURCE_SHORTCUT);
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);

        if ((startupSource == GlobalData.STARTUP_SOURCE_WIDGET) ||
            (startupSource == GlobalData.STARTUP_SOURCE_SHORTCUT)) {

            GlobalData.loadPreferences(getApplicationContext());

            dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!GlobalData.getApplicationStarted(getApplicationContext(), true)) {
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));
        }

        if ((startupSource == GlobalData.STARTUP_SOURCE_WIDGET) ||
            (startupSource == GlobalData.STARTUP_SOURCE_SHORTCUT))
            dataWrapper.activateProfile(profile_id, startupSource, this/*, ""*/);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

}
