package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class SetRadioPrefsForProfileBroadcastReceiver extends WakefulBroadcastReceiver {

	private static final String	ACTION = "sk.henrichg.phoneprofilesplus.SetRadiosForProfile.ACTION";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (action.equals (ACTION))
		{
			// start service
			
			long profileId = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
			if (profileId != 0)
			{
				Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
				radioServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profileId);
				startWakefulService(context, radioServiceIntent);
			}
		}		
		
	}

}
