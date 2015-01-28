package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.Bundle;

public class RestartEventsFromNotificationActivity extends Activity
{
	
	private DataWrapper dataWrapper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		GlobalData.loadPreferences(getBaseContext());
		
		dataWrapper = new DataWrapper(getBaseContext(), false, false, 0);
	 
		//Log.e("RestartEventsFromNotificationActivity,onCreate","xxx");
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		//Log.e("RestartEventsFromNotificationActivity.onStart", "xxx");

		// set theme and language for dialog alert ;-)
		// not working on Android 2.3.x
		GUIData.setTheme(this, true, false);
		GUIData.setLanguage(getBaseContext());
		
		dataWrapper.restartEventsWithAlert(this);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
	}

}
