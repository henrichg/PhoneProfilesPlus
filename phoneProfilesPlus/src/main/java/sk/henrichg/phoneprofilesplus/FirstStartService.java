package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;


public class FirstStartService extends IntentService {

	public FirstStartService()
	{
		super("FirstStartService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Context context = getBaseContext();
		
		// grant root
		//if (GlobalData.isRooted(false))
		//{
			if (GlobalData.grantRoot(true))
			{
				GlobalData.settingsBinaryExists();
				//GlobalData.getSUVersion();
			}
		//}
		
		if (GlobalData.getApplicationStarted(context))
			return;
		
		//int startType = intent.getStringExtra(GlobalData.EXTRA_FIRST_START_TYPE);
		
		GlobalData.loadPreferences(context);
		GUIData.setLanguage(context);
		
		// start PPHelper
		//PhoneProfilesHelper.startPPHelper(context);
		
		// show notification about upgrade PPHelper
		//if (GlobalData.isRooted(false))
		//{
			if (!PhoneProfilesHelper.isPPHelperInstalled(context, PhoneProfilesHelper.PPHELPER_CURRENT_VERSION))
			{
				// proper PPHelper version is not installed
				if (PhoneProfilesHelper.PPHelperVersion != -1)
				{
					// PPHelper is installed, show notification 
					PhoneProfilesHelper.showPPHelperUpgradeNotification(context);							
				}
			}
		//}
		
		// start ReceiverService
		context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));

		DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);
		dataWrapper.getDatabaseHandler().deleteAllEventTimelines(true);
		
		// create a handler to post messages to the main thread
	    Handler toastHandler = new Handler(getMainLooper());
	    dataWrapper.setToastHandler(toastHandler);
	    Handler brightnessHandler = new Handler(getMainLooper());
	    dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);
		
		// zrusenie notifikacie
		dataWrapper.getActivateProfileHelper().removeNotification();

		// startneme eventy
		if (GlobalData.getGlobalEventsRuning(context))
		{
			// must by false for avoiding starts/pause events before restart events
			GlobalData.setApplicationStarted(context, false); 
			
			dataWrapper.firstStartEvents(true, false);
		}
		else
		{
			GlobalData.setApplicationStarted(context, true);
			
			if (GlobalData.applicationActivate)
			{
				Profile profile = dataWrapper.getDatabaseHandler().getActivatedProfile();
				long profileId = 0;
				if (profile != null)
					profileId = profile._id;
				else
				{
					profileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
					if (profileId == GlobalData.PROFILE_NO_ACTIVATE)
						profileId = 0;
				}
				dataWrapper.activateProfile(profileId, GlobalData.STARTUP_SOURCE_BOOT, null, "");
			}
			else
				dataWrapper.activateProfile(0, GlobalData.STARTUP_SOURCE_BOOT, null, "");
		}
		
		dataWrapper.invalidateDataWrapper();
	}
		
}
