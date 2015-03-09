package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class PhoneProfilesPreferencesActivity extends ActionBarActivity
{

	private SharedPreferences preferences;
	
	private boolean showEditorPrefIndicator;
	private boolean showEditorHeader;
	private String activeLanguage;
	private String activeTheme;
	private int wifiScanInterval;
	private int bluetoothScanInterval;
	//private String activeBackgroundProfile;

	private boolean invalidateEditor = false;
	 
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {

		// must by called before super.onCreate() for PreferenceActivity
		GUIData.setTheme(this, false, false);
		GUIData.setLanguage(getBaseContext());
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_phone_profiles_preferences);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }

		invalidateEditor = false;
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_activity_phone_profiles_preferences);
		
		
        preferences = getBaseContext().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, MODE_PRIVATE);
        activeLanguage = preferences.getString(GlobalData.PREF_APPLICATION_LANGUAGE, "system");
        activeTheme = preferences.getString(GlobalData.PREF_APPLICATION_THEME, "material");
        showEditorPrefIndicator = preferences.getBoolean(GlobalData.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        showEditorHeader = preferences.getBoolean(GlobalData.PREF_APPLICATION_EDITOR_HEADER, true);
        wifiScanInterval = Integer.valueOf(preferences.getString(GlobalData.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "10"));
        bluetoothScanInterval = Integer.valueOf(preferences.getString(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "10"));
		
		if (savedInstanceState == null) {
			PhoneProfilesPreferencesFragment fragment = new PhoneProfilesPreferencesFragment();
			getFragmentManager().beginTransaction()
					.replace(R.id.activity_phone_profiles_preferences_container, fragment, "PhoneProfilesPreferencesFragment").commit();
		}
		
        
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		GUIData.reloadActivity(this, false);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		PhoneProfilesPreferencesFragment fragment = (PhoneProfilesPreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_phone_profiles_preferences_container);
		if (fragment != null)
			fragment.doOnActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		DataWrapper dataWrapper =  new DataWrapper(this.getBaseContext(), true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getBaseContext());
		dataWrapper.getActivateProfileHelper().showNotification(dataWrapper.getActivatedProfileFromDB(), "");
		dataWrapper.getActivateProfileHelper().updateWidget();
		dataWrapper.invalidateDataWrapper();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void finish() {
		
		GlobalData.loadPreferences(getBaseContext());
		
		if (activeLanguage != GlobalData.applicationLanguage)
		{
			GUIData.setLanguage(getBaseContext());
			invalidateEditor = true;
		}
		else
		if (activeTheme != GlobalData.applicationTheme)
		{
    		//EditorProfilesActivity.setTheme(this, false);
			invalidateEditor = true;
		}
		else
   		if (showEditorPrefIndicator != GlobalData.applicationEditorPrefIndicator)
   		{
   			invalidateEditor = true;
   		}
		else
   		if (showEditorHeader != GlobalData.applicationEditorHeader)
   		{
   			invalidateEditor = true;
   		}
		
		if (wifiScanInterval != GlobalData.applicationEventWifiScanInterval)
		{
			if (WifiScanAlarmBroadcastReceiver.isAlarmSet(getApplicationContext(), false))
				WifiScanAlarmBroadcastReceiver.setAlarm(getApplicationContext(), false);
		}

		if (bluetoothScanInterval != GlobalData.applicationEventBluetoothScanInterval)
		{
			if (BluetoothScanAlarmBroadcastReceiver.isAlarmSet(getApplicationContext(), false))
				BluetoothScanAlarmBroadcastReceiver.setAlarm(getApplicationContext(), false);
		}
		
		/*
		if (activeBackgroundProfile != GlobalData.applicationBackgroundProfile)
   		{
   			long lApplicationBackgroundProfile = Long.valueOf(GlobalData.applicationBackgroundProfile);
   			if (lApplicationBackgroundProfile != GlobalData.PROFILE_NO_ACTIVATE)
   			{
   				DataWrapper dataWrapper = new DataWrapper(getBaseContext(), true, false, 0);
   				if (dataWrapper.getActivatedProfile() == null)
   				{
   					dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, getBaseContext());
   					dataWrapper.activateProfile(lApplicationBackgroundProfile, GlobalData.STARTUP_SOURCE_SERVICE, null, "");
   				}
   				//invalidateEditor = true;
   			}
   		}
   		*/
		
		
		// for startActivityForResult
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GlobalData.EXTRA_RESET_EDITOR, invalidateEditor);
		setResult(RESULT_OK,returnIntent);

	    super.finish();
	}

	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		
	}

}
