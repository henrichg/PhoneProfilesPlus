package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    private int startupSource;
    private DataWrapper dataWrapper;

    private static final int REQUEST_CODE_IMPORTANT_INFO = 1620;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

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
            PPApplication.logE("LauncherActivity.onStart", "application is not started");
            PPApplication.logE("LauncherActivity.onStart", "service instance="+PhoneProfilesService.getInstance());
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("LauncherActivity.onStart", "service hasFirstStart="+PhoneProfilesService.getInstance().getServiceHasFirstStart());
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(this, serviceIntent);
        }
        else
        {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                PPApplication.logE("LauncherActivity.onStart", "application is started");
                PPApplication.logE("LauncherActivity.onStart", "service instance="+PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("LauncherActivity.onStart", "service hasFirstStart="+PhoneProfilesService.getInstance().getServiceHasFirstStart());
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                PPApplication.startPPService(this, serviceIntent);
            }
            else {
                PPApplication.logE("LauncherActivity.onStart", "application and service is started");
            }

            if (startupSource == 0)
            {
                // activity was not started from notification, widget

                PPApplication.showProfileNotification(/*getApplicationContext()*/true);
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

        //PPApplication.logE("LauncherActivity.endOnStart", "applicationFirstStart="+ApplicationPreferences.applicationFirstStart(getApplicationContext()));
        if (ApplicationPreferences.applicationFirstStart(getApplicationContext())) {
            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                editor.apply();
            }
            intentLaunch = new Intent(getApplicationContext(), ImportantInfoActivity.class);
            intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, true);
            startActivityForResult(intentLaunch, REQUEST_CODE_IMPORTANT_INFO);
        }
        else {
            finish();

            intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentLaunch.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            getApplicationContext().startActivity(intentLaunch);
            // reset startupSource
            startupSource = 0;
        }
    }

    @Override
    protected void onDestroy()
    {
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_IMPORTANT_INFO)
        {
            endOnStart();
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
