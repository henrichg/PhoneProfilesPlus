package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

class SettingsContentObserver  extends ContentObserver {

    //public static boolean internalChange = false;

    private static int previousVolumeRing = 0;
    private static int previousVolumeNotification = 0;
    //private static int previousVolumeMusic = 0;
    //private static int previousVolumeAlarm = 0;
    //private static int previousVolumeSystem = 0;
    //private static int previousVolumeVoice = 0;
    //private int defaultRingerMode = 0;

    Context context;

    SettingsContentObserver(Context c, Handler handler) {
        super(handler);

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        context=c;

        //Log.e("### SettingsContentObserver", "xxx");

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolumeRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        previousVolumeNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        //previousVolumeMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //previousVolumeAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        //previousVolumeSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        //previousVolumeVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    private int volumeChangeDetect(int volumeStream, int previousVolume, AudioManager audioManager) {

        int currentVolume = audioManager.getStreamVolume(volumeStream);
        PPApplication.logE("### SettingsContentObserver", "channel=" + volumeStream + " currentVolume=" + currentVolume);
        PPApplication.logE("### SettingsContentObserver", "channel=" + volumeStream + " previousVolume=" + previousVolume);
        PPApplication.logE("### SettingsContentObserver", "internalChange="+RingerModeChangeReceiver.internalChange);

        int delta=previousVolume-currentVolume;

        if(delta>0)
        {
            //Log.e("### SettingsContentObserver", "channel="+volumeStream+" Decreased");
            if (!RingerModeChangeReceiver.internalChange) {
                if (volumeStream == AudioManager.STREAM_RING) {
                    PPApplication.setRingerVolume(context, currentVolume);
                    PhoneProfilesService.ringingVolume = currentVolume;
                }
                if (volumeStream == AudioManager.STREAM_NOTIFICATION)
                    PPApplication.setNotificationVolume(context, currentVolume);
            }
        }
        else if(delta<0)
        {
            //Log.e("### SettingsContentObserver", "channel="+volumeStream+" Increased");
            if (!RingerModeChangeReceiver.internalChange) {
                if (volumeStream == AudioManager.STREAM_RING) {
                    PPApplication.setRingerVolume(context, currentVolume);
                    PhoneProfilesService.ringingVolume = currentVolume;
                }
                if (volumeStream == AudioManager.STREAM_NOTIFICATION)
                    PPApplication.setNotificationVolume(context, currentVolume);
            }
        }
        return currentVolume;
    }

    @Override
    public void onChange(boolean selfChange) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onChange(selfChange);

        //Log.e("### SettingsContentObserver", "onChange - internalChange=" + internalChange);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int audioMode = audioManager.getMode();

        if ((audioMode == AudioManager.MODE_NORMAL) || (audioMode == AudioManager.MODE_RINGTONE)) {
            previousVolumeRing = volumeChangeDetect(AudioManager.STREAM_RING, previousVolumeRing, audioManager);
            previousVolumeNotification = volumeChangeDetect(AudioManager.STREAM_NOTIFICATION, previousVolumeNotification, audioManager);
            //previousVolumeMusic = volumeChangeDetect(AudioManager.STREAM_MUSIC, previousVolumeMusic, audioManager);
            //previousVolumeAlarm = volumeChangeDetect(AudioManager.STREAM_ALARM, previousVolumeAlarm, audioManager);
            //previousVolumeSystem = volumeChangeDetect(AudioManager.STREAM_SYSTEM, previousVolumeSystem, audioManager);
        }
        //previousVolumeVoice = volumeChangeDetect(AudioManager.STREAM_VOICE_CALL, previousVolumeVoice, audioManager);
    }

}
