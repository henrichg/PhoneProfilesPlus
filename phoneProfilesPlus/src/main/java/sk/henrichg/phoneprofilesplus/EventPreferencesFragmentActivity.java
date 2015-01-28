package sk.henrichg.phoneprofilesplus;
 
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnHideActionModeInEventPreferences;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnShowActionModeInEventPreferences;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnRedrawEventListFragment;
import sk.henrichg.phoneprofilesplus.EventPreferencesFragment.OnRestartEventPreferences;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
 
public class EventPreferencesFragmentActivity extends ActionBarActivity
												implements OnRestartEventPreferences,
	                                                       OnRedrawEventListFragment,
	                                                       OnShowActionModeInEventPreferences,
	                                                       OnHideActionModeInEventPreferences
{
	
	private long event_id = 0; 
	int newEventMode = EditorEventListFragment.EDIT_MODE_UNDEFINED;
			
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		// must by called before super.onCreate() for PreferenceActivity
		GUIData.setTheme(this, false, false); // must by called before super.onCreate()
		GUIData.setLanguage(getBaseContext());

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_event_preferences);


		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_activity_event_preferences);

        event_id = getIntent().getLongExtra(GlobalData.EXTRA_EVENT_ID, 0L);
        newEventMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);

		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putLong(GlobalData.EXTRA_EVENT_ID, event_id);
			arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
			arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
			EventPreferencesFragment fragment = new EventPreferencesFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.activity_event_preferences_container, fragment, "EventPreferencesFragment").commit();
		}
		
    	//Log.d("EventPreferencesFragmentActivity.onCreate", "xxxx");
		
    }
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void finish() {
		
		//Log.e("EventPreferencesFragmentActivity.finish","xxx");

		EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_event_preferences_container);
		if (fragment != null)
			event_id = fragment.event_id;
		
		// for startActivityForResult
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GlobalData.EXTRA_EVENT_ID, event_id);
		returnIntent.putExtra(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
		setResult(RESULT_OK,returnIntent);

	    super.finish();
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
	
	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		GUIData.reloadActivity(this, false);
	}
	*/
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_event_preferences_container);
		if (fragment != null)
			fragment.doOnActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // handle your back button code here
        	EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_event_preferences_container);
        	//Log.e("EventPreferencesFragmentActivity.dispatchKeyEvent","fragment="+fragment);
        	//Log.e("EventPreferencesFragmentActivity.dispatchKeyEvent","isActionModeActive="+fragment.isActionModeActive());
    		if ((fragment != null) && (fragment.isActionModeActive()))
    		{
    			fragment.finishActionMode(EventPreferencesFragment.BUTTON_CANCEL);
	            return true; // consumes the back key event - ActionMode is not finished
    		}
    		else
    		    return super.dispatchKeyEvent(event);
        }
	    return super.dispatchKeyEvent(event);
	}

	public void onRestartEventPreferences(Event event, int newEventMode) {
		if ((newEventMode != EditorEventListFragment.EDIT_MODE_INSERT) &&
		     (newEventMode != EditorEventListFragment.EDIT_MODE_DUPLICATE))
		{
			Bundle arguments = new Bundle();
			arguments.putLong(GlobalData.EXTRA_EVENT_ID, event._id);
			arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
			arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
			EventPreferencesFragment fragment = new EventPreferencesFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.activity_event_preferences_container, fragment, "EventPreferencesFragment").commit();
		}
		else
		{
			Fragment fragment = getFragmentManager().findFragmentById(R.id.activity_event_preferences_container);
			if (fragment != null)
			{
				getFragmentManager().beginTransaction()
					.remove(fragment).commit();
			}
		}
	}

	public void onRedrawEventListFragment(Event event, int newEventMode) {
		// all redraws are in EditorProfilesActivity.onActivityResult()
	}
	
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
	}

	@Override
	public void onHideActionModeInEventPreferences() {
		
	}

	@Override
	public void onShowActionModeInEventPreferences() {
		
	}

}
