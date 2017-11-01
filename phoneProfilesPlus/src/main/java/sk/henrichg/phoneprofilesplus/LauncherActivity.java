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

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!PPApplication.getApplicationStarted(getApplicationContext(), false))
        {
            //Log.e("LauncherActivity.onStart","app. not started");

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
                startService(serviceIntent);
            //else
            //    startForegroundService(serviceIntent);
        }
        else
        {
            //Log.e("LauncherActivity.onStart","app. started");

            if (PhoneProfilesService.instance == null) {
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
                //TODO Android O
                //if (Build.VERSION.SDK_INT < 26)
                    startService(serviceIntent);
                //else
                //    startForegroundService(serviceIntent);
            }

            if (startupSource == 0)
            {
                // activity was not started from notification, widget

                Profile profile = dataWrapper.getActivatedProfile();
                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.showProfileNotification(profile, dataWrapper);
                dataWrapper.getActivateProfileHelper().updateWidget(true);
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

        switch (startupSource) {
            case PPApplication.STARTUP_SOURCE_NOTIFICATION:
                if (ApplicationPreferences.applicationNotificationLauncher(getApplicationContext()).equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            case PPApplication.STARTUP_SOURCE_WIDGET:
                if (ApplicationPreferences.applicationWidgetLauncher(getApplicationContext()).equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            default:
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
