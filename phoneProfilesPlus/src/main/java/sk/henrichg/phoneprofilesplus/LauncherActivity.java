package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

	int startupSource;
	DataWrapper dataWrapper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(0, 0);
		
		dataWrapper = new DataWrapper(getBaseContext(), true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getBaseContext());
		
		Intent intent = getIntent();
		startupSource = intent.getIntExtra(GlobalData.EXTRA_START_APP_SOURCE, 0);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();

		if (!GlobalData.getApplicationStarted(getBaseContext()))
		{
			// start service for first start
			Intent firstStartServiceIntent = new Intent(getApplicationContext(), FirstStartService.class);
			startService(firstStartServiceIntent);
		}
		else
		{
			if (startupSource == 0)
			{
				// aktivita nebola spustena z notifikacie, ani z widgetu

                // start ReceiverService
                startService(new Intent(getApplicationContext(), ReceiversService.class));

				// pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety 
				Profile profile = dataWrapper.getActivatedProfile();
				dataWrapper.getActivateProfileHelper().showNotification(profile, "");
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
					intentLaunch = new Intent(getBaseContext(), ActivateProfileActivity.class);
				else
					intentLaunch = new Intent(getBaseContext(), EditorProfilesActivity.class);
				break;
			case GlobalData.STARTUP_SOURCE_WIDGET:
				if (GlobalData.applicationWidgetLauncher.equals("activator"))
					intentLaunch = new Intent(getBaseContext(), ActivateProfileActivity.class);
				else
					intentLaunch = new Intent(getBaseContext(), EditorProfilesActivity.class);
				break;
			default:
				if (GlobalData.applicationHomeLauncher.equals("activator"))
					intentLaunch = new Intent(getBaseContext(), ActivateProfileActivity.class);
				else
					intentLaunch = new Intent(getBaseContext(), EditorProfilesActivity.class);
				break;
		}

		finish();
		
		intentLaunch.putExtra(GlobalData.EXTRA_START_APP_SOURCE, startupSource);
		startActivity(intentLaunch);
		
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
