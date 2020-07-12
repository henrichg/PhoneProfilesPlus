package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;
    public static boolean notUnlinkVolumes = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //PPApplication.logE("##### RingerModeChangeReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "RingerModeChangeReceiver.onReceive", "RingerModeChangeReceiver_onReceive");

        /*if (PPApplication.logEnabled()) {
            final AudioManager _audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (_audioManager != null) {
                int ringerMode = _audioManager.getRingerMode();
                PPApplication.logE("********** RingerModeChangeReceiver.onReceive", "ringerMode=" + ringerMode);
            }
        }*/

        if (!internalChange) {
            //PPApplication.logE("RingerModeChangeReceiver.onReceive", "!internalChange");
            notUnlinkVolumes = true;
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context, audioManager/*, "PringerModeChangeReceiver.onReceive"*/);
        }

        //setAlarmForDisableInternalChange(context);
    }

    private static int getRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        //PPApplication.logE("********** RingerModeChangeReceiver.getRingerMode", "ringerMode="+ringerMode);
        // convert to profile ringerMode
        int pRingerMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
        //PPApplication.logE("RingerModeChangeReceiver.getRingerMode", "systemZenMode=" + systemZenMode);
        if (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) {
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, false))
                    //    pRingerMode = Profile.RINGERMODE_RING_AND_VIBRATE;
                    //else
                        pRingerMode = Profile.RINGERMODE_RING;
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    pRingerMode = Profile.RINGERMODE_VIBRATE;
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    pRingerMode = Profile.RINGERMODE_SILENT;
                    break;
            }
        }
        else
            pRingerMode = Profile.RINGERMODE_ZENMODE;

        //PPApplication.logE("RingerModeChangeReceiver.getRingerMode", "pRingerMode=" + pRingerMode);

        return pRingerMode;
    }

    public static void setRingerMode(Context context, AudioManager audioManager/*, String from*/) {
        int pRingerMode = getRingerMode(context, audioManager);
        //PPApplication.logE("********* RingerModeChangeReceiver.setRingerMode", "from="+from+" pRingerMode="+pRingerMode);
        if (pRingerMode != 0) {
            ActivateProfileHelper.saveRingerMode(context, pRingerMode);
        }
    }

}
