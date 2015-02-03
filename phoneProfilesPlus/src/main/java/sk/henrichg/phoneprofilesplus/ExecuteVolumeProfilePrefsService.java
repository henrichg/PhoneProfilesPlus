package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class ExecuteVolumeProfilePrefsService extends IntentService //WakefulIntentService 
{
	
	public ExecuteVolumeProfilePrefsService() {
		super("ExecuteRadioProfilePrefsService");
	}

	//@Override
	//protected void doWakefulWork(Intent intent) {
	protected void onHandleIntent(Intent intent) {
		
		//Log.e("ExecuteVolumeProfilePrefsService.onHandleIntent","---- start");
		
		Context context = getBaseContext();
		
		GlobalData.loadPreferences(context);
		
		DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
		ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
		aph.initialize(dataWrapper, null, context);
		
		long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
		Profile profile = dataWrapper.getProfileById(profile_id);
		profile = GlobalData.getMappedProfile(profile, context);
		if (profile != null)
		{
			AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                // simulate silent mode for Android 5.0
                if (profile._volumeRingerMode == 4) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        //System.out.println(e);
                    }

                    aph.setRingerMode(profile, audioManager);
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //System.out.println(e);
            }

            aph.setVolumes(profile, audioManager);


            /*
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //System.out.println(e);
            }
            */


			//if (!aph.setVolumes(profile, audioManager))
			//{
				// nahodenie ringer modu - hlasitosti zmenia silent/vibrate
				aph.setRingerMode(profile, audioManager);
			//}
			
		/*	if (intent.getBooleanExtra(GlobalData.EXTRA_SECOND_SET_VOLUMES, false))
			{
				// run service for execute volumes - second set
				Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
				volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
				volumeServiceIntent.putExtra(GlobalData.EXTRA_SECOND_SET_VOLUMES, false);
				//WakefulIntentService.sendWakefulWork(context, radioServiceIntent);
				context.startService(volumeServiceIntent);
			} */
		}
		dataWrapper.invalidateDataWrapper();
		aph = null;
		dataWrapper = null;
		
		//Log.e("ExecuteVolumeProfilePrefsService.onHandleIntent","---- end");
		
	}
	
	
}
