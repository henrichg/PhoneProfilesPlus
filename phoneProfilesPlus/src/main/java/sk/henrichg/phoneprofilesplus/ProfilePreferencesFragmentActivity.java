package sk.henrichg.phoneprofilesplus;
 
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnHideActionModeInProfilePreferences;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnRedrawProfileListFragment;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnRestartProfilePreferences;
import sk.henrichg.phoneprofilesplus.ProfilePreferencesFragment.OnShowActionModeInProfilePreferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
 
public class ProfilePreferencesFragmentActivity extends ActionBarActivity
												implements OnRestartProfilePreferences,
	                                                       OnRedrawProfileListFragment,
	                                                       OnShowActionModeInProfilePreferences,
	                                                       OnHideActionModeInProfilePreferences
{
	private long profile_id = 0;
	int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		// must by called before super.onCreate() for PreferenceActivity
		GUIData.setTheme(this, false, false); // must by called before super.onCreate()
		GUIData.setLanguage(getBaseContext());

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_profile_preferences);


		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_id = getIntent().getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);

        if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
        	getSupportActionBar().setTitle(R.string.title_activity_default_profile_preferences);
        else
        	getSupportActionBar().setTitle(R.string.title_activity_profile_preferences);
        
        //Log.e("ProfilePreferencesFragmentActivity.onCreate","profile_id="+profile_id);
        
		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
			arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
			if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE);
			else
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
			ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.activity_profile_preferences_container, fragment, "ProfilePreferencesFragment").commit();
		}
		
    	//Log.d("ProfilePreferencesFragmentActivity.onCreate", "xxxx");
		
    }
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		
		//Log.e("ProfilePreferencesFragmentActivity.finish","xxx");

		ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
		if (fragment != null)
			profile_id = fragment.profile_id;
		
		// for startActivityForResult
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile_id);
		returnIntent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
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
		GUIData.reloadActivity(this);
	}
	*/
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
		if (fragment != null)
			fragment.doOnActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            // handle your back button code here
        	ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
    		if ((fragment != null) && (fragment.isActionModeActive()))
    		{
    			fragment.finishActionMode(ProfilePreferencesFragment.BUTTON_CANCEL);
	            return true; // consumes the back key event - ActionMode is not finished
    		}
    		else
    		    return super.dispatchKeyEvent(event);
        }
	    return super.dispatchKeyEvent(event);
	}
	
	public void onRestartProfilePreferences(Profile profile, int newProfileMode) {
		if ((newProfileMode != EditorProfileListFragment.EDIT_MODE_INSERT) &&
		    (newProfileMode != EditorProfileListFragment.EDIT_MODE_DUPLICATE))
		{
			Bundle arguments = new Bundle();
			arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile._id);
			arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
			if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE);
			else
				arguments.putInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
			ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.activity_profile_preferences_container, fragment, "ProfilePreferencesFragment").commit();
		}
		else
		{
			ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().findFragmentById(R.id.activity_profile_preferences_container);
			if (fragment != null)
			{
				getFragmentManager().beginTransaction()
					.remove(fragment).commit();
			}
		}
	}

	public void onRedrawProfileListFragment(Profile profile, int newProfileMode) {
		// all redraws are in EditorProfilesActivity.onActivityResult()
	}
	
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
	}

	@Override
	public void onHideActionModeInProfilePreferences() {
		
	}

	@Override
	public void onShowActionModeInProfilePreferences() {
		
	}

}
