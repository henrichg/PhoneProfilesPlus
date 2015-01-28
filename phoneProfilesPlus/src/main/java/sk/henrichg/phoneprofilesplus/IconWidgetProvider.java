package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class IconWidgetProvider extends AppWidgetProvider {
	
	private DataWrapper dataWrapper;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		
		GlobalData.loadPreferences(context);
		
		int monochromeValue = 0xFF;
		if (GlobalData.applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
		if (GlobalData.applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
		if (GlobalData.applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
		if (GlobalData.applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
		if (GlobalData.applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;

		dataWrapper = new DataWrapper(context, true,  
												GlobalData.applicationWidgetIconColor.equals("1"), 
												monochromeValue);
		
		Profile profile = dataWrapper.getActivatedProfile();

		// ziskanie vsetkych wigetov tejtor triedy na plochach lauchera
		ComponentName thisWidget = new ComponentName(context, IconWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// prechadzame vsetky ziskane widgety
		for (int widgetId : allWidgetIds)
		{
			boolean isIconResourceID;
			String iconIdentifier;
			String profileName;
			if (profile != null)
			{
				isIconResourceID = profile.getIsIconResourceID();
				iconIdentifier = profile.getIconIdentifier();
				profileName = dataWrapper.getProfileNameWithManualIndicator(profile, false); 
			}
			else
			{
				// create empty profile and set icon resource
				profile = new Profile();
				profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
				profile._icon = GlobalData.PROFILE_ICON_DEFAULT+"|1";

				profile.generateIconBitmap(context, 
						GlobalData.applicationWidgetListIconColor.equals("1"), 
						monochromeValue);
				isIconResourceID = profile.getIsIconResourceID();
				iconIdentifier = profile.getIconIdentifier();
				profileName = profile._name;
			}
			
			// priprava view-u na aktualizacia widgetu
			RemoteViews remoteViews;
			if (GlobalData.applicationWidgetIconHideProfileName)
				remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget_no_profile_name);
			else
				remoteViews = new RemoteViews(context.getPackageName(), R.layout.icon_widget);
	        if (isIconResourceID)
	        {
	        	//remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
	        	int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
	        	remoteViews.setImageViewResource(R.id.icon_widget_icon, iconResource);
	        }
	        else
	        {
	    		//Resources resources = context.getResources();
	        	//remoteViews.setImageViewBitmap(R.id.activate_profile_widget_icon, null);
	    		//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
	    		//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
	    		//Bitmap bitmap = BitmapResampler.resample(iconIdentifier, width, height);
	        	//remoteViews.setImageViewBitmap(R.id.activate_profile_widget_icon, bitmap);
	        	remoteViews.setImageViewBitmap(R.id.icon_widget_icon, profile._iconBitmap);
	        }
			if (!GlobalData.applicationWidgetIconHideProfileName)
				remoteViews.setTextViewText(R.id.icon_widget_name, profileName);
			
			// konfiguracia, ze ma spustit hlavnu aktivitu zoznamu profilov, ked kliknme na widget
			Intent intent = new Intent(context, LauncherActivity.class);
			intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_WIDGET);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.icon_widget_icon, pendingIntent);
			remoteViews.setOnClickPendingIntent(R.id.icon_widget_name, pendingIntent);
			
			// aktualizacia widgetu
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

		dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
		
	}
}
