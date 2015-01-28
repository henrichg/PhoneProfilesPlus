package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class BackgroundActivateProfileActivity extends Activity {

	private DataWrapper dataWrapper;
	
	private int startupSource = 0;
	private long profile_id;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log.d("BackgroundActivateProfileActivity.onCreate","xxx");
		
		GlobalData.loadPreferences(getBaseContext());
		
		dataWrapper = new DataWrapper(getBaseContext(), true, false, 0);
		
		intent = getIntent();
		startupSource = intent.getIntExtra(GlobalData.EXTRA_START_APP_SOURCE, 0);
		profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
		//Log.d("BackgroundActivateProfileActivity.onStart", "profile_id="+profile_id);

		dataWrapper.getActivateProfileHelper().initialize(dataWrapper, this, getBaseContext());
		
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		//Log.e("BackgroundActivateProfileActivity.onStart", "startupSource="+startupSource);

		dataWrapper.activateProfile(profile_id, startupSource, this, "");
		
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
	}
	
}
