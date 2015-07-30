package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!internalChange) {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int ringerMode = audioManager.getRingerMode();

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

        internalChange = false;

    }
}
