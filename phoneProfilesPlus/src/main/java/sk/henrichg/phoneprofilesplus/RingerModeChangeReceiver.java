package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("### RingerModeChangeReceiver", "xxx");

        //if (!internalChange) {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context, audioManager);
        //}

        internalChange = false;

    }

    @SuppressWarnings("deprecation")
    public static void setRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        Log.e("RingerModeChangeReceiver",".setRingerMode  ringerMode="+ringerMode);

        int vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        int vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);

        // convert to profile ringerMode
        int pRingerMode = 0;
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_NORMAL:
                if ((vibrateType == AudioManager.VIBRATE_SETTING_ON) || (vibrateWhenRinging == 1))
                    pRingerMode = 2;
                else
                    pRingerMode = 1;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                pRingerMode = 3;
                break;
            case AudioManager.RINGER_MODE_SILENT:
                pRingerMode = 4;
                break;
        }
        if (pRingerMode != 0)
            GlobalData.setRingerMode(context, pRingerMode);
    }

}
