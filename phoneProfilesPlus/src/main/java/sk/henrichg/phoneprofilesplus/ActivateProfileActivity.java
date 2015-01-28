package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class ActivateProfileActivity extends ActionBarActivity {

	private static ActivateProfileActivity instance;
	
	private float popupWidth;
	private float popupMaxHeight;
	private float popupHeight;
	private int actionBarHeight;
	
	@SuppressLint("NewApi")
	@SuppressWarnings({ "deprecation" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		instance = this;
		
		GUIData.setTheme(this, true, true);
		GUIData.setLanguage(getBaseContext());
		
	// set window dimensions ----------------------------------------------------------
		
		Display display = getWindowManager().getDefaultDisplay();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		LayoutParams params = getWindow().getAttributes();
		params.alpha = 1.0f;
		params.dimAmount = 0.5f;
		getWindow().setAttributes(params);
		
		// display dimensions
		popupWidth = display.getWidth();
		popupMaxHeight = display.getHeight();
		popupHeight = 0;
		actionBarHeight = 0;

		// action bar height
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true))
	        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		
		// set max. dimensions for display orientation
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			//popupWidth = Math.round(popupWidth / 100f * 50f);
			//popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
			popupWidth = popupWidth / 100f * 50f;
			popupMaxHeight = popupMaxHeight / 100f * 90f;
		}
		else
		{
			//popupWidth = Math.round(popupWidth / 100f * 70f);
			//popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
			popupWidth = popupWidth / 100f * 80f;
			popupMaxHeight = popupMaxHeight / 100f * 90f;
		}

		// add action bar height
		popupHeight = popupHeight + actionBarHeight;
		
		final float scale = getResources().getDisplayMetrics().density;
		
		// add header height
		if (GlobalData.applicationActivatorHeader)
			popupHeight = popupHeight + 64f * scale;
		
		// add toolbar height
		popupHeight = popupHeight + (25f + 1f + 3f) * scale;

		DataWrapper dataWrapper = new DataWrapper(getBaseContext(), false, false, 0);
		int profileCount = dataWrapper.getDatabaseHandler().getProfilesCount(true);
		dataWrapper.invalidateDataWrapper();

		if (!GlobalData.applicationActivatorGridLayout)
		{
			// add list items height
			popupHeight = popupHeight + (50f * scale * profileCount); // item
			popupHeight = popupHeight + (5f * scale * (profileCount-1)); // divider
		}
		else
		{
			// add grid items height
			int modulo = profileCount % 3;
			profileCount = profileCount / 3;
			if (modulo > 0)
				++profileCount;
			popupHeight = popupHeight + (75f * scale * profileCount); // item
			popupHeight = popupHeight + (5f * scale * (profileCount-1)); // divider
		}

		popupHeight = popupHeight + (20f * scale); // listview padding
		
		if (popupHeight > popupMaxHeight)
			popupHeight = popupMaxHeight;
	
		// set popup window dimensions
		getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));
		
		
	//-----------------------------------------------------------------------------------

		//Debug.startMethodTracing("phoneprofiles");

	// Layout ---------------------------------------------------------------------------------
		
		//requestWindowFeature(Window.FEATURE_ACTION_BAR);
		
		//long nanoTimeStart = GlobalData.startMeasuringRunTime();
		
		setContentView(R.layout.activity_activate_profile);
		
		//GlobalData.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onCreate - setContnetView");

		Toolbar toolbar = (Toolbar)findViewById(R.id.act_prof_tollbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setTitle(R.string.title_activity_activator);

		refreshGUI();
		
    //-----------------------------------------------------------------------------------------		
		
		//Log.d("PhoneProfileActivity.onCreate", "xxxx");
		
	}
	
	public static ActivateProfileActivity getInstance()
	{
		return instance;
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
		instance = null;
	}
	
	@Override 
	protected void onResume()
	{
		//Debug.stopMethodTracing();
		super.onResume();
		if (instance == null)
		{
			instance = this;
			refreshGUI();
		}
	}
	
	@Override
	protected void onDestroy()
	{
	//	Debug.stopMethodTracing();
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_activate_profile, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// change global events run/stop menu item title 
		MenuItem menuItem = menu.findItem(R.id.menu_restart_events);
		if (menuItem != null)
		{
			menuItem.setVisible(GlobalData.getGlobalEventsRuning(getBaseContext()));
		}
		
		return super.onPrepareOptionsMenu(menu);
	}	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit_profiles:
			//Log.d("ActivateProfileActivity.onOptionsItemSelected", "menu_settings");
			
			Intent intent = new Intent(getBaseContext(), EditorProfilesActivity.class);
			intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR);
			startActivity(intent);
			
			finish();

			return true;
		case R.id.menu_restart_events:
			DataWrapper dataWrapper = new DataWrapper(getBaseContext(), false, false, 0);
			// ignoruj manualnu aktivaciu profilu
			// a odblokuj forceRun eventy
			dataWrapper.restartEventsWithAlert(this);
			dataWrapper.invalidateDataWrapper();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		//setContentView(R.layout.activity_phone_profiles);
		GUIData.reloadActivity(this, false);
	}

	public void refreshGUI()
	{
		setEventsRunStopIndicator();
		
		Fragment fragment = getFragmentManager().findFragmentById(R.id.activate_profile_list);
		if (fragment != null)
		{
			((ActivateProfileListFragment)fragment).refreshGUI();
		}
	}
	
    public void setEventsRunStopIndicator()
    {
		ImageView eventsRunStopIndicator = (ImageView)findViewById(R.id.act_prof_run_stop_indicator);
    	
		if (GlobalData.getGlobalEventsRuning(getBaseContext()))
		{
			if (GlobalData.getEventsBlocked(getBaseContext()))
				eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
			else
				eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
		}
		else
			eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stoppped);
    }

}
