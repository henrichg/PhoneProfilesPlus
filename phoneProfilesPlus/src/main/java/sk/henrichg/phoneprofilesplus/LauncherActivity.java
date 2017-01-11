package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

    int startupSource;
    DataWrapper dataWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        overridePendingTransition(0, 0);

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

        Intent intent = getIntent();
        startupSource = intent.getIntExtra(GlobalData.EXTRA_STARTUP_SOURCE, 0);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (!GlobalData.getApplicationStarted(getApplicationContext(), false))
        {
            //Log.d("LauncherActivity.onStart","app. not started");

            /*// start service for first start
            Intent firstStartServiceIntent = new Intent(getApplicationContext(), FirstStartService.class);
            startService(firstStartServiceIntent);*/

            // start PhoneProfilesService
            //GlobalData.firstStartServiceStarted = false;
            startService(new Intent(getApplicationContext(), PhoneProfilesService.class));
        }
        else
        {
            //Log.d("LauncherActivity.onStart","app. started");

            if (PhoneProfilesService.instance == null) {
                // start PhoneProfilesService
                //GlobalData.firstStartServiceStarted = false;
                startService(new Intent(getApplicationContext(), PhoneProfilesService.class));
            }

            if (startupSource == 0)
            {
                // aktivita nebola spustena z notifikacie, ani z widgetu

                // pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety
                Profile profile = dataWrapper.getActivatedProfile();
                dataWrapper.getActivateProfileHelper().showNotification(profile);
                dataWrapper.getActivateProfileHelper().updateWidget();
                startupSource = GlobalData.STARTUP_SOURCE_LAUNCHER;
            }
        }

        if (startupSource == 0)
            startupSource = GlobalData.STARTUP_SOURCE_LAUNCHER;
        endOnStart();
    }

    private void endOnStart()
    {
        //  aplikacia uz je 1. krat spustena - is in FirstStartService
        //GlobalData.setApplicationStarted(getBaseContext(), true);

        Intent intentLaunch;

        switch (startupSource) {
            case GlobalData.STARTUP_SOURCE_NOTIFICATION:
                if (GlobalData.applicationNotificationLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            case GlobalData.STARTUP_SOURCE_WIDGET:
                if (GlobalData.applicationWidgetLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
            default:
                if (GlobalData.applicationHomeLauncher.equals("activator"))
                    intentLaunch = new Intent(getApplicationContext(), ActivateProfileActivity.class);
                else
                    intentLaunch = new Intent(getApplicationContext(), EditorProfilesActivity.class);
                break;
        }

        finish();

        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.putExtra(GlobalData.EXTRA_STARTUP_SOURCE, startupSource);
        getApplicationContext().startActivity(intentLaunch);

        // reset, aby sa to dalej chovalo ako normalne spustenie z lauchera
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
        overridePendingTransition(0, 0);
        super.finish();
    }

}
