package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.RemoteViews;

public class OneRowWidgetProvider extends AppWidgetProvider {
	
	private DataWrapper dataWrapper;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		
		GlobalData.loadPreferences(context);

		int monochromeValue = 0xFF;
		if (GlobalData.applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
		if (GlobalData.applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
		if (GlobalData.applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
		if (GlobalData.applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
		if (GlobalData.applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;

		dataWrapper = new DataWrapper(context, true, GlobalData.applicationWidgetListIconColor.equals("1"), monochromeValue);
		
		Profile profile = dataWrapper.getActivatedProfile();

		// ziskanie vsetkych wigetov tejtor triedy na plochach lauchera
		ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
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
				profileName = dataWrapper.getProfileNameWithManualIndicator(profile, true); 
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
			
			RemoteViews remoteViews;
			if (GlobalData.applicationWidgetListPrefIndicator)
				remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
			else
				remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);
			

			// set background
			int red = 0;
			int green = 0;
			int blue = 0;
			if (GlobalData.applicationWidgetListLightnessB.equals("0")) red = 0x00;
			if (GlobalData.applicationWidgetListLightnessB.equals("25")) red = 0x40;
			if (GlobalData.applicationWidgetListLightnessB.equals("50")) red = 0x80;
			if (GlobalData.applicationWidgetListLightnessB.equals("75")) red = 0xC0;
			if (GlobalData.applicationWidgetListLightnessB.equals("100")) red = 0xFF;
			green = red; blue = red;
			int alpha = 0x40;
			if (GlobalData.applicationWidgetListBackground.equals("0")) alpha = 0x00;
			if (GlobalData.applicationWidgetListBackground.equals("25")) alpha = 0x40;
			if (GlobalData.applicationWidgetListBackground.equals("50")) alpha = 0x80;
			if (GlobalData.applicationWidgetListBackground.equals("75")) alpha = 0xC0;
			if (GlobalData.applicationWidgetListBackground.equals("100")) alpha = 0xFF;
			remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
			
			if (isIconResourceID)
	        {
	        	//remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
	        	int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
	        	remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_icon, iconResource);
	        }
	        else
	        {
	    		//Resources resources = context.getResources();
	        	//remoteViews.setImageViewBitmap(R.id.activate_profile_widget_icon, null);
	    		//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
	    		//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
	    		//Bitmap bitmap = BitmapResampler.resample(iconIdentifier, width, height);
	        	//remoteViews.setImageViewBitmap(R.id.activate_profile_widget_icon, bitmap);
	        	remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
	        }
	        //if (GlobalData.applicationWidgetListIconColor.equals("1"))
	        //{
	    		red = 0xFF;
	    		green = 0xFF;
	    		blue = 0xFF;
	    		if (GlobalData.applicationWidgetListLightnessT.equals("0")) red = 0x00;
	    		if (GlobalData.applicationWidgetListLightnessT.equals("25")) red = 0x40;
	    		if (GlobalData.applicationWidgetListLightnessT.equals("50")) red = 0x80;
	    		if (GlobalData.applicationWidgetListLightnessT.equals("75")) red = 0xC0;
	    		if (GlobalData.applicationWidgetListLightnessT.equals("100")) red = 0xFF;
	    		green = red; blue = red;
	    		remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, red, green, blue));
	        //}
	        //else
	        //{
	        //	remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.parseColor("#33b5e5"));
	        //}
			remoteViews.setTextViewText(R.id.widget_one_row_header_profile_name, profileName);
			if (GlobalData.applicationWidgetListPrefIndicator)
			{
				if (profile._preferencesIndicator == null)
					remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_pref_indicator, R.drawable.ic_empty);
				else
					remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_pref_indicator, profile._preferencesIndicator);
			}
	        if (GlobalData.applicationWidgetListIconColor.equals("1"))
	        {
				monochromeValue = 0xFF;
				if (GlobalData.applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
				if (GlobalData.applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
				if (GlobalData.applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
				if (GlobalData.applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
				if (GlobalData.applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
	        	
	        	Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_activated);
	        	bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue, context);
	        	remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_activated, bitmap);
	        }
	        else
	        {
	        	remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_activated, R.drawable.ic_profile_activated);
	        }
			
			
			
			// konfiguracia, ze ma spustit hlavnu aktivitu zoznamu profilov, ked kliknme na widget
			Intent intent = new Intent(context, LauncherActivity.class);
			intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_WIDGET);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header, pendingIntent);
			
			// aktualizacia widgetu
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	
		dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
		
	}
	
}
