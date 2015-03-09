package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.Bundle;

public class UpgradePPHelperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GlobalData.loadPreferences(getBaseContext());
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		PhoneProfilesHelper.installPPHelper(this, true);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
}
