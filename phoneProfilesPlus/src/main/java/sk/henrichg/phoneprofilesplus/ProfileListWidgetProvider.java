package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

@SuppressLint("NewApi")
public class ProfileListWidgetProvider extends AppWidgetProvider {

	private DataWrapper dataWrapper;
	
	public static final String INTENT_REFRESH_LISTWIDGET = "sk.henrichg.phoneprofilesplus.REFRESH_LISTWIDGET";
	
	private boolean isLargeLayout;
	private boolean isKeyguard;
	
	@SuppressWarnings("deprecation")
    private RemoteViews buildLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean largeLayout)
	{
		Intent svcIntent=new Intent(context, ProfileListWidgetService.class);
		      
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		      
		RemoteViews widget;
		
		if (largeLayout)
		{
			if (GlobalData.applicationWidgetListHeader)
			{
				if (!GlobalData.applicationWidgetListGridLayout)
				{
					if (GlobalData.applicationWidgetListPrefIndicator)
						widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget);
					else
						widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_indicator);
				}
				else
				{
					if (GlobalData.applicationWidgetListPrefIndicator)
						widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget);
					else
						widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_indicator);
				}
			}
			else
			{
				if (!GlobalData.applicationWidgetListGridLayout)
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_no_header);
				else
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_grid_widget_no_header);
			}
		}
		else
		{
			if (isKeyguard)
			{
				if (GlobalData.applicationWidgetListPrefIndicator)
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_keyguard);
				else
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator_keyguard);
			}
			else
			{
				if (GlobalData.applicationWidgetListPrefIndicator)
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small);
				else
					widget=new RemoteViews(context.getPackageName(), R.layout.profile_list_widget_small_no_indicator);
			}
		}

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
		widget.setInt(R.id.widget_profile_list_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
		
		
		// header
		if (GlobalData.applicationWidgetListHeader || (!largeLayout))
		{
			int monochromeValue = 0xFF;
			if (GlobalData.applicationWidgetListIconLightness.equals("0")) monochromeValue = 0x00;
			if (GlobalData.applicationWidgetListIconLightness.equals("25")) monochromeValue = 0x40;
			if (GlobalData.applicationWidgetListIconLightness.equals("50")) monochromeValue = 0x80;
			if (GlobalData.applicationWidgetListIconLightness.equals("75")) monochromeValue = 0xC0;
			if (GlobalData.applicationWidgetListIconLightness.equals("100")) monochromeValue = 0xFF;
			
			Profile profile = dataWrapper.getDatabaseHandler().getActivatedProfile();

			boolean isIconResourceID;
			String iconIdentifier;
			String profileName;
			if (profile != null)
			{
				profile.generateIconBitmap(context, 
						GlobalData.applicationWidgetListIconColor.equals("1"), 
						monochromeValue);
				profile.generatePreferencesIndicator(context, 
						GlobalData.applicationWidgetListIconColor.equals("1"), 
						monochromeValue);
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
				profile.generatePreferencesIndicator(context, 
						GlobalData.applicationWidgetListIconColor.equals("1"), 
						monochromeValue);
				isIconResourceID = profile.getIsIconResourceID();
				iconIdentifier = profile.getIconIdentifier();
				profileName = profile._name;
			}
	        if (isIconResourceID)
	        {
	        	int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
	        	
	        	widget.setImageViewResource(R.id.widget_profile_list_header_profile_icon, iconResource);
	        }
	        else
	        {
        		widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_icon, profile._iconBitmap);
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
	        	widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.argb(0xFF, red, green, blue));
	        //}
	        //else
	        //{
			//	widget.setTextColor(R.id.widget_profile_list_header_profile_name, Color.parseColor("#33b5e5"));
	        //}
			widget.setTextViewText(R.id.widget_profile_list_header_profile_name, profileName);
			if (GlobalData.applicationWidgetListPrefIndicator)
			{
        		widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_pref_indicator, profile._preferencesIndicator);
			}
			if (largeLayout)
			{	
				red = 0xFF;
				green = 0xFF;
				blue = 0xFF;
				if (GlobalData.applicationWidgetListLightnessT.equals("0")) red = 0x00;
				if (GlobalData.applicationWidgetListLightnessT.equals("25")) red = 0x40;
				if (GlobalData.applicationWidgetListLightnessT.equals("50")) red = 0x80;
				if (GlobalData.applicationWidgetListLightnessT.equals("75")) red = 0xC0;
				if (GlobalData.applicationWidgetListLightnessT.equals("100")) red = 0xFF;
				green = red; blue = red;
				widget.setInt(R.id.widget_profile_list_header_separator, "setBackgroundColor", Color.argb(alpha, red, green, blue));
			}
	        if (GlobalData.applicationWidgetListIconColor.equals("1"))
	        {
	        	Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_activated);
	        	bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue, context);
				widget.setImageViewBitmap(R.id.widget_profile_list_header_profile_activated, bitmap);
	        }
	        else
	        {
	        	widget.setImageViewResource(R.id.widget_profile_list_header_profile_activated, R.drawable.ic_profile_activated);
	        }
		}
		////////////////////////////////////////////////
		
		// clicks
		if (largeLayout)
		{
			Intent intent = new Intent(context, EditorProfilesActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, 
                    									PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);

			if (!GlobalData.applicationWidgetListGridLayout)
				widget.setRemoteAdapter(appWidgetId, R.id.widget_profile_list, svcIntent);
			else
				widget.setRemoteAdapter(appWidgetId, R.id.widget_profile_grid, svcIntent);
			
			// The empty view is displayed when the collection has no items. 
	        // It should be in the same layout used to instantiate the RemoteViews
	        // object above.
			if (!GlobalData.applicationWidgetListGridLayout)
				widget.setEmptyView(R.id.widget_profile_list, R.id.widget_profiles_list_empty);
			else
				widget.setEmptyView(R.id.widget_profile_grid, R.id.widget_profiles_list_empty);
			
			Intent clickIntent=new Intent(context, BackgroundActivateProfileActivity.class);
			clickIntent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_WIDGET);
			PendingIntent clickPI=PendingIntent.getActivity(context, 0,
			                                            clickIntent,
			                                            PendingIntent.FLAG_UPDATE_CURRENT);
			      
			if (!GlobalData.applicationWidgetListGridLayout)
				widget.setPendingIntentTemplate(R.id.widget_profile_list, clickPI);
			else
				widget.setPendingIntentTemplate(R.id.widget_profile_grid, clickPI);
		}
		else
		{
			Intent intent = new Intent(context, LauncherActivity.class);
			intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_WIDGET);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,  
														PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(R.id.widget_profile_list_header, pendingIntent);
		}
		
		return widget;
	}
	
	public void createProfilesDataWrapper(Context context)
	{
		GlobalData.loadPreferences(context);
		if (dataWrapper == null)
		{
			dataWrapper = new DataWrapper(context, false, false, 0); 
		}
	}
	
	private void doOnUpdate(Context ctxt, AppWidgetManager appWidgetManager, int appWidgetId)
	{
		Bundle myOptions;
		if (android.os.Build.VERSION.SDK_INT >= 16)
			myOptions = appWidgetManager.getAppWidgetOptions (appWidgetId);
		else
			myOptions = null;
        setLayoutParams(ctxt, appWidgetManager, appWidgetId, myOptions);
    	RemoteViews widget = buildLayout(ctxt, appWidgetManager, appWidgetId, isLargeLayout);
    	appWidgetManager.updateAppWidget(appWidgetId, widget);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		//Log.e("ProfileListWidgetProvider.onUpdate","xxx");
		
		createProfilesDataWrapper(context);

		for (int i=0; i<appWidgetIds.length; i++)
		{
			doOnUpdate(context, appWidgetManager, appWidgetIds[i]);
		}
			    
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String action = intent.getAction();
		
		//Log.e("ProfileListWidgetProvider.onReceive","action="+action);

		int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		
		createProfilesDataWrapper(context);

		if ((action != null) &&
		    (action.equalsIgnoreCase("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")))
		{
			int spanX = intent.getIntExtra("spanX", 1);
			int spanY = intent.getIntExtra("spanY", 1);
			//Log.e("ProfileListWidgetProvider.onReceive","spanX="+spanX);
			//Log.e("ProfileListWidgetProvider.onReceive","spanY="+spanY);
			//Log.e("ProfileListWidgetProvider.onReceive","appWidgetId="+appWidgetId);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	        setLayoutParamsMotorola(context, spanX, spanY, appWidgetId);
	       	RemoteViews layout;
	       	layout = buildLayout(context, appWidgetManager, appWidgetId, isLargeLayout);
	       	appWidgetManager.updateAppWidget(appWidgetId, layout);
		}
		else
		if ((action != null) &&
		    (action.equalsIgnoreCase(INTENT_REFRESH_LISTWIDGET)))
			updateWidgets(context);
		
		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
		
	}
	
	private void setLayoutParams(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions)
	{
		String preferenceKey = "isLargeLayout_"+appWidgetId;
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId); 
		
		int minHeight;
		if (newOptions != null)
		{
			// Get the value of OPTION_APPWIDGET_HOST_CATEGORY
			int category = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
			// If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen widget
			isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
	
	        //int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
	        //int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
	        minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
	        //int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

			if ((minHeight == 0) && (appWidgetProviderInfo != null))
			{
				minHeight = appWidgetProviderInfo.minHeight;
			}

			//Log.e("ProfileListWidgetProvider.setLayoutParams","minHeight="+minHeight);
			
		}
		else
		{
			isKeyguard = false;
			if (appWidgetProviderInfo != null)
				minHeight = appWidgetProviderInfo.minHeight;
			else
				minHeight = 0;
			//Log.e("ProfileListWidgetProvider.setLayoutParams"," null minHeight="+minHeight);

			//if (minHeight == 0)
			//	return;
		}

        if (isKeyguard)
        {
	        if (minHeight < 250) {
	           isLargeLayout = false;
	        } else {
	            isLargeLayout = true;
	        }
        }
        else
        {
	        if (minHeight < 110) {
	            isLargeLayout = false;
	        } else {
	            isLargeLayout = true;
	        }
    	}
        
		if (preferences.contains(preferenceKey))
			isLargeLayout = preferences.getBoolean(preferenceKey, true);
		else
		{
			Editor editor = preferences.edit();
			editor.putBoolean(preferenceKey, isLargeLayout);
			editor.commit();
		}
        
	}

	private void setLayoutParamsMotorola(Context context, int spanX, int spanY, int appWidgetId)
	{
		isKeyguard = false;
        if (spanY == 1) {
            isLargeLayout = false;
        } else {
            isLargeLayout = true;
        }
        
		String preferenceKey = "isLargeLayout_"+appWidgetId;
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

		Editor editor = preferences.edit();
		editor.putBoolean(preferenceKey, isLargeLayout);
		editor.commit();
	}
	
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) 
    {
		//Log.e("ProfileListWidgetProvider.onAppWidgetOptionsChanged","xxx");

		createProfilesDataWrapper(context);
		
		String preferenceKey = "isLargeLayout_"+appWidgetId;
		SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
		
		// remove preference, will by reseted in setLayoutParams
		Editor editor = preferences.edit();
		editor.remove(preferenceKey);
		editor.commit();
		
		
		updateWidget(context, appWidgetId);
		
		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
		
    }	

	private void updateWidget(Context context, int appWidgetId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

	    doOnUpdate(context, appWidgetManager, appWidgetId);
	    if (isLargeLayout)
	    {
			if (!GlobalData.applicationWidgetListGridLayout)
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_list);
			else
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_profile_grid);
	    }
	}	
	
	private void updateWidgets(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, ProfileListWidgetProvider.class));

		for (int i=0; i<appWidgetIds.length; i++)
		{
			updateWidget(context, appWidgetIds[i]);
		}
	}	
	
}
