package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

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
		
		//doOnStart();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
/*		
		//if (!GlobalData.getApplicationStarted(getBaseContext()))
		//{
			// grant root
			Intent eventsServiceIntent = new Intent(getBaseContext(), GrantRootService.class);
			getBaseContext().startService(eventsServiceIntent);
		//}
		
		
		Profile profile = dataWrapper.getActivatedProfile();
		
		boolean actProfile = false;
		//if ((startupSource == 0)
		//{
			
			// aktivita nebola spustena z notifikacie, ani z widgetu
			// lebo v tychto pripadoch sa nesmie spravit aktivacia profilu
			// pri starte aktivity
			
			if (!GlobalData.getApplicationStarted(getBaseContext()))
			{
				// aplikacia este nie je nastartovana

				// start ReceiverService
				startService(new Intent(getApplicationContext(), ReceiversService.class));
				
				// startneme eventy
				if (GlobalData.getGlobalEventsRuning(getBaseContext()))
					dataWrapper.firstStartEvents(true, false);
				else
				{
					// mozeme aktivovat profil, ak je nastavene, ze sa tak ma stat 
					if (GlobalData.applicationActivate)
					{
						// je nastavene, ze pri starte sa ma aktivita aktivovat
						actProfile = true;
						if (profile == null)
						{
							long profileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
							if (profileId == GlobalData.PROFILE_NO_ACTIVATE)
								profileId = 0;
							profile = dataWrapper.getProfileById(profileId);
						}
					}
					else
					{
						// profile sa nema aktivovat, tak ho deaktivujeme
						dataWrapper.getDatabaseHandler().deactivateProfile();
						profile = null;
					}
				}
			}
		//}
		//Log.e("LauncherActivity.onStart", "actProfile="+String.valueOf(actProfile));

		if (actProfile && (profile != null))
		{
			// aktivacia profilu
			activateProfile(profile, GlobalData.STARTUP_SOURCE_LAUNCHER_START);
			endOnStart();
		}
		else
		{
			if (startupSource == 0)
			{
				// aktivita nebola spustena z notifikacie, ani z widgetu
				// pre profil, ktory je prave aktivny, treba aktualizovat notifikaciu a widgety 
				dataWrapper.getActivateProfileHelper().showNotification(profile, "");
				dataWrapper.getActivateProfileHelper().updateWidget();
				startupSource = GlobalData.STARTUP_SOURCE_LAUNCHER;
			}
			endOnStart();
		}
*/		

		if (!GlobalData.getApplicationStarted(getBaseContext()))
		{
			// start service for first start
			Intent firstStartServiceIntent = new Intent(getBaseContext(), FirstStartService.class);
			startService(firstStartServiceIntent);
		}
		else
		{
			if (startupSource == 0)
			{
				// aktivita nebola spustena z notifikacie, ani z widgetu
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
		//Log.e("LauncherActivity.endOnStart","xxx");

		//  aplikacia uz je 1. krat spustena
		GlobalData.setApplicationStarted(getBaseContext(), true);
		
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
	
/*	
	private void activateProfile(Profile profile, int startupSource)
	{
		dataWrapper.activateProfile(profile._id, startupSource, this, "");
	}
*/	
}
