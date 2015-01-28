package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
		int myUid = android.os.Process.myUid();
		if (intentUid == myUid)
		{
			GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "####");
		
			if (GlobalData.getApplicationStarted(context))
			{
				// must by false for avoiding starts/pause events before restart events
				GlobalData.setApplicationStarted(context, false); 
				
				// start ReceiverService
				context.startService(new Intent(context.getApplicationContext(), ReceiversService.class));
				
				// start service for first start
				//Intent firstStartServiceIntent = new Intent(context, FirstStartService.class);
				//context.startService(firstStartServiceIntent);
				
				
			/*	
				GlobalData.loadPreferences(context);
				
				// grant root
				Intent eventsServiceIntent = new Intent(context, GrantRootService.class);
				context.startService(eventsServiceIntent);

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
				
				*/
			}
		}		
	}
	
}
