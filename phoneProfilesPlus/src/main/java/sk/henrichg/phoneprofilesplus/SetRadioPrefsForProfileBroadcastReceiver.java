package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class SetRadioPrefsForProfileBroadcastReceiver extends WakefulBroadcastReceiver {

	private static final String	ACTION = "sk.henrichg.phoneprofilesplus.SetRadiosForProfile.ACTION";
	
	@Override
	public void onReceive(Context context, Intent intent) {

    	//Log.e("SetRadioPrefsForProfileBroadcastReceiver.onReceive","xxx");
    	
		
		String action = intent.getAction();

    	//Log.e("SetRadioPrefsForProfileBroadcastReceiver.onReceive","action="+action);
		
		if (action.equals (ACTION))
		{
			// start service
			
			long profileId = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
	    	//Log.e("SetRadioPrefsForProfileBroadcastReceiver.onReceive","profileId="+profileId);
			if (profileId != 0)
			{
				Intent radioServiceIntent = new Intent(context, ExecuteRadioProfilePrefsService.class);
				radioServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profileId);
				startWakefulService(context, radioServiceIntent);
			}
		}		
		
	}

}
