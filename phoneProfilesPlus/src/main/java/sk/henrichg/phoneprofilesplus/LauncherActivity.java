package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    private int startupSource;
    private DataWrapper dataWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0);

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
        PPApplication.logE("LauncherActivity.onCreate", "startupSource="+startupSource);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), true))
        {
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            PPApplication.startPPService(this, serviceIntent);
        }
        else
        {
            if (!PhoneProfilesService.getServiceHasFirstStart()) {
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
                PPApplication.startPPService(this, serviceIntent);
            }

            if (startupSource == 0)
            {
                // activity was not started from notification, widget

                PPApplication.showProfileNotification(getApplicationContext());
                ActivateProfileHelper.updateGUI(dataWrapper.context, true);
                startupSource = PPApplication.STARTUP_SOURCE_LAUNCHER;
            }
        }

        if (startupSource == 0)
            startupSource = PPApplication.STARTUP_SOURCE_LAUNCHER;
        endOnStart();
    }

    private void endOnStart()
    {
        //  application is already started - is in PhoneProfilesService
        //PPApplication.setApplicationStarted(getBaseContext(), true);

        Intent intentLaunch;

        PPApplication.logE("LauncherActivity.endOnStart", "startupSource="+startupSource);
        switch (startupSource) {
            case PPApplication.STARTUP_SOURCE_NOTIFICATION:
                PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_NOTIFICATION");
                if (ApplicationPreferences.applicationNotificationLauncher(getApplicationContext()).equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            case PPApplication.STARTUP_SOURCE_WIDGET:
                PPApplication.logE("LauncherActivity.endOnStart", "STARTUP_SOURCE_WIDGET");
                if (ApplicationPreferences.applicationWidgetLauncher(getApplicationContext()).equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            default:
                PPApplication.logE("LauncherActivity.endOnStart", "default");
                if (ApplicationPreferences.applicationHomeLauncher(getApplicationContext()).equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
        }

        finish();

        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
        getApplicationContext().startActivity(intentLaunch);

        // reset startupSource
        startupSource = 0;

    }

    @Override
    protected void onDestroy()
    {
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;

        super.onDestroy();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
