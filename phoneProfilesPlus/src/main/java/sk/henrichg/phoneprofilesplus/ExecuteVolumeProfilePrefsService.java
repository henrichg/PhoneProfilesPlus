package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

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

        int linkUnlink = intent.getIntExtra(GlobalData.EXTRA_LINKUNLINK_VOLUMES, PhoneCallService.LINKMODE_NONE);
        long profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        boolean merged = intent.getBooleanExtra(GlobalData.EXTRA_MERGED_PROFILE, false);
        Profile profile = dataWrapper.getProfileById(profile_id, merged);
        profile = GlobalData.getMappedProfile(profile, context);

        if (profile != null)
        {
            if (Permissions.checkProfileVolumePreferences(context, profile)) {

                RingerModeChangeReceiver.internalChange = true;

                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                // set ringer mode to Ring for proper change ringer mode to Silent
                //aph.setRingerMode(profile, audioManager, true, linkUnlink);

                GlobalData.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "audioMode="+audioManager.getMode());

                aph.setVolumes(profile, audioManager, linkUnlink);

                /*
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }
                */

                GlobalData.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "audioMode="+audioManager.getMode());

                // set ringer mode to Ring for proper change ringer mode to Silent
                aph.setRingerMode(profile, audioManager, true, linkUnlink);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                GlobalData.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "audioMode="+audioManager.getMode());

                // set ringer mode after volume because volumes change silent/vibrate
                aph.setRingerMode(profile, audioManager, false, linkUnlink);

                //RingerModeChangeReceiver.setAlarmForDisableInternalChange(context);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //System.out.println(e);
                }

                GlobalData.logE("ExecuteVolumeProfilePrefsService.onHandleIntent", "audioMode="+audioManager.getMode());

                RingerModeChangeReceiver.internalChange = false;

            }

        }

    }


}
