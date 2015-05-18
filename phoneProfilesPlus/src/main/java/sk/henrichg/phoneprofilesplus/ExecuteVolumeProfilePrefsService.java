package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

public class ExecuteVolumeProfilePrefsService extends IntentService
{
	
	public ExecuteVolumeProfilePrefsService() {
		super("ExecuteRadioProfilePrefsService");
	}

	//@Override
	protected void onHandleIntent(Intent intent) {
		
		final Context context = getApplicationContext();
		
		GlobalData.loadPreferences(context);
		
		DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
		final ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
		aph.initialize(dataWrapper, null, context);
		
		long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(GlobalData.EXTRA_MERGED_PROFILE, false);
		Profile profile = dataWrapper.getProfileById(profile_id, merged);
		profile = GlobalData.getMappedProfile(profile, context);

		if (profile != null)
		{
            final Profile _profile = profile;

            //Handler audioChangeHandler = new Handler(getMainLooper());

			final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            //int oldNURM = Settings.System.getInt(context.getContentResolver(), "notifications_use_ring_volume", -10);

            //audioChangeHandler.post(new Runnable() {
            //    @Override
            //    public void run() {
                    // set ringer mode for proper volume change
                    Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
                    aph.setRingerMode(_profile, audioManager);

                    if (!PhoneCallBroadcastReceiver.separateVolumes) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            //System.out.println(e);
                        }
                    }

                    Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
                    aph.setVolumes(_profile, audioManager);

                    // set ringer mode because volumes change silent/vibrate
                    Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", 0);
                    aph.setRingerMode(_profile, audioManager);
            //    }
            //});

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //System.out.println(e);
            }

            //if (oldNURM != -10)
            //    Settings.System.putInt(context.getContentResolver(), "notifications_use_ring_volume", oldNURM);

            /*
            boolean rechangeRingerMode = false;
            int savedProfileRingerMode = profile._volumeRingerMode;

            // for Android 5.0 set ringer mode again
            if ((android.os.Build.VERSION.SDK_INT >= 21))
            {
                rechangeRingerMode = true;
            }

            // when ringer mode is changed to VIBRATE
            // and profile ringer mode is RING or SILENT,
            // change ringer mode to SILENT
            if ((profile._volumeRingerMode == 1) ||  // ring
                (profile._volumeRingerMode == 4))    // silent
            {
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
                {
                    // ringer mode is changed to VIBRATE, set it to SILENT
                    profile._volumeRingerMode = 4;
                    rechangeRingerMode = true;
                }
            }

            // for Android 5.0 and profile ringer mode ZEN_MODE,
            // when ringer mode is changed to VIBRATE,
            // change ringer mode to SILENT twice
            if ((android.os.Build.VERSION.SDK_INT >= 21)) {
                if ((profile._volumeRingerMode == 5) && (profile._volumeZenMode != 3)) // NONE
                {
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        profile._volumeRingerMode = 4;
                        rechangeRingerMode = true;
                        aph.setRingerMode(profile, audioManager);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            //System.out.println(e);
                        }
                        aph.setVolumes(profile, audioManager);
                    }
                }
            }

            if (rechangeRingerMode) {
                aph.setRingerMode(profile, audioManager);
                profile._volumeRingerMode = savedProfileRingerMode;
            }
            */

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

	}
	
	
}
